package cz.cesnet.meta.pbscache;

import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.parser.JSONParser;
import cz.cesnet.meta.RefreshLoader;
import cz.cesnet.meta.pbs.FairshareConfig;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.PbsServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PbsCacheImpl extends RefreshLoader implements PbsCache {

    final static Logger log = LoggerFactory.getLogger(PbsCacheImpl.class);


    private List<PbsServerConfig> pbsServers;
    private Map<String, List<String>> fairshareIdToOrderedUsersMap;
    private List<FairshareConfig> fairshareConfigs;

    @Override
    public List<FairshareConfig> getFairshareConfigs() {
        return fairshareConfigs;
    }

    /**
     * Gets fairshare ranks for existing users.
     *
     * @param fairshareId id of fairshare
     * @param userNames   existing users from PBS
     * @return map from user names to their fairshare rank
     */
    @Override
    public Map<String, Integer> getRankMapForFairshareIdAndExistingUsers(String fairshareId, Set<String> userNames) {
        //assign ranks only to existing users
        List<String> usersRanked = this.getFairshareOrderedUsers(fairshareId);
        Map<String, Integer> rankMap = new HashMap<>(usersRanked.size());
        int rank = 1;
        for (String userName : usersRanked) {
            if (userNames.contains(userName)) rankMap.put(userName, rank++);
        }
        return rankMap;
    }

    /**
     * For a given fairshare id returns ordered list of users.
     *
     * @param fairshareId id of fairshare
     * @return list of usernames, lower index means lower rank/priority
     */
    private List<String> getFairshareOrderedUsers(String fairshareId) {
        checkLoad();
        return fairshareIdToOrderedUsersMap.get(fairshareId);
    }

    public void setPbsServers(List<PbsServerConfig> pbsServers) {
        this.pbsServers = pbsServers;
    }

    protected void load() {
        log.debug("load()");
        loadMapping();//only static mapping now
        loadScratchSizes();
        loadFairshare();
        loadGpuAllocation();
    }


    private Mapping mapping;
    private Map<String, Scratch> scratchSizes;

    public Mapping getMapping() {
        checkLoad();
        return this.mapping;
    }

    @Override
    public Scratch getScratchForNode(Node node) {
        checkLoad();
        return this.scratchSizes.get(node.getName());
    }


    public PbsCacheImpl() {
    }

    private static class FairShareTuple {
        String user;
        Double fairshare;

        private FairShareTuple(String user, String value) {
            this.user = user;
            if ("inf".equals(value)) {
                value = "Infinity";
            } else if ("nan".equals(value)) {
                value = "NaN";
            }
            this.fairshare = Double.valueOf(value);
        }

        public String getUser() {
            return user;
        }

        public Double getFairshare() {
            return fairshare;
        }
    }

    final static Logger loggpu = LoggerFactory.getLogger(PbsCacheImpl.class.getName() + ".gpu");
    /**
     * Maps hostname to map(gpu,jobId).
     */
    private Map<String, Map<String, String>> gpuAllocMap = new HashMap<>();

    @Override
    public Map<String, String> getGpuAlloc(Node node) {
        return gpuAllocMap.get(node.getName());
    }

    private void loadGpuAllocation() {
        loggpu.debug("loadGpuAllocation()");
        Map<String, Map<String, String>> allocMap = new HashMap<>();
        // "value": "unallocated",    "key": "gram4.zcu.cz:/dev/nvidia2"
        // "value": "4688478.arien.ics.muni.cz","key": "gram1.zcu.cz:/dev/nvidia2"
        List<PbsCacheEntry> entries = new ArrayList<>();
        for (PbsServerConfig server : pbsServers) {
            loadMetrics(server, "gpu_allocation", entries);
        }
        Pattern p = Pattern.compile("([^:]+):(.*)");
        for (PbsCacheEntry pce : entries) {
            String jobId = pce.getValue();
            if ("unallocated".equals(jobId)) continue;
            Matcher m = p.matcher(pce.getKey());
            if (m.matches()) {
                String hostname = m.group(1);
                String gpu = m.group(2);

                Map<String, String> gpu2jobIdMap = allocMap.get(hostname);
                if (gpu2jobIdMap == null) {
                    gpu2jobIdMap = new HashMap<>();
                    allocMap.put(hostname, gpu2jobIdMap);
                }
                gpu2jobIdMap.put(gpu, jobId);
            } else {
                loggpu.warn("key " + pce.getKey() + " not matching regex");
            }
        }
        if (loggpu.isDebugEnabled()) {
            for (Map.Entry<String, Map<String, String>> me : allocMap.entrySet()) {
                loggpu.debug("gpu alloc host {} : {}", me.getKey(), me.getValue());
            }
        }
        gpuAllocMap = allocMap;
    }

    private void loadFairshare() {
        Map<String, List<String>> fairshareRank = new HashMap<>();
        List<FairshareConfig> fairshareConfigs = new ArrayList<>();
        for (PbsServerConfig server : pbsServers) {
            for (FairshareConfig fairshareConfig : server.getFairshares()) {
                fairshareConfigs.add(fairshareConfig);
                List<PbsCacheEntry> entries = new ArrayList<>();
                loadMetrics(server, fairshareConfig.getMetrics(), entries);
                ArrayList<FairShareTuple> ftups = new ArrayList<>(entries.size());
                for (PbsCacheEntry entry : entries) {
                    ftups.add(new FairShareTuple(entry.getKey(), entry.getValue()));
                }
                Collections.sort(ftups, (t1, t2) -> t1.getFairshare().compareTo(t2.getFairshare()));
                List<String> ranked = new ArrayList<>(ftups.size());
                for (FairShareTuple ft : ftups) {
                    ranked.add(ft.getUser());
                }
                fairshareRank.put(fairshareConfig.getId(), ranked);
            }
        }
        this.fairshareIdToOrderedUsersMap = fairshareRank;
        this.fairshareConfigs = fairshareConfigs;
    }


    private static void loadMetrics(PbsServerConfig serverConfig, String metrics, List<PbsCacheEntry> entries) {
        String server = serverConfig.getHost();
        log.trace("loadMetrics({},{})", server, metrics);
        File tempFile=null;
        try {
            tempFile = File.createTempFile("pbscache", ".txt");
            Process p = new ProcessBuilder("list_cache", server, metrics)
                    .inheritIO()
                    .redirectOutput(tempFile)
                    .redirectError(tempFile)
                    .directory(Paths.get(System.getProperty("java.io.tmpdir", "/tmp")).toFile())
                    .start();
            int exit = p.waitFor();
            if(exit!=0) {
                log.error("list_cache {} {} failed with exit status {}",server,metrics,exit);
                return;
            }

            List<String> lines = Files.readAllLines(tempFile.toPath(), Charset.defaultCharset());
            Pattern linePattern = Pattern.compile("^([^\\t]+)\\t(\\d+)\\t(.+)");
            for (String line : lines) {
                Matcher m = linePattern.matcher(line);
                if (m.matches()) {
                    String key = m.group(1);
                    long timestamp = Long.parseLong(m.group(2)) * 1000L;
                    String value = m.group(3);
                    entries.add(new PbsCacheEntry(key, value, timestamp));
                } else {
                    log.error("Line from {} {} does not parse: '{}'",server,metrics,line);
                    return;
                }
            }
        } catch (Exception ex) {
            log.error("cannot read pbs_cache metrics " + metrics + " from server " + server, ex);
        } finally {
            if (tempFile!=null && !tempFile.delete()) {
                log.warn("Cannot delete file " + tempFile);
            }
        }
    }


    enum ScratchType {local, ssd, pool}

    private void loadScratchSizes() {
        Map<String, Scratch> scratchSizes = new HashMap<>();
        for (PbsServerConfig server : pbsServers) {
            if (server.isTorque()) {
                for (ScratchType scratchType : ScratchType.values()) {
                    loadScratchSizes(server, scratchSizes, scratchType);
                }
            }
        }
        this.scratchSizes = scratchSizes;
    }

    private boolean loadScratchSizes(PbsServerConfig serverConfig, Map<String, Scratch> scratchSizes, ScratchType type) {
        String server = serverConfig.getHost();
        //log.trace("loadScratchSizes({},{})", server, type);
        Map<String, Long> pools = null;
        if (type == ScratchType.pool) {
            pools = loadNetworkScratchSizes(serverConfig);
        }
        long mez = System.currentTimeMillis() - (4 * 60 * 60 * 1000L);
        List<PbsCacheEntry> entries = new ArrayList<>();
        loadMetrics(serverConfig, "scratch_" + type, entries);
        for (PbsCacheEntry pce : entries) {
            if (type == ScratchType.pool || pce.getTimestamp() > mez) {
                String nodename = pce.getKey();
                Scratch scratch = scratchSizes.get(nodename);
                if (scratch == null) {
                    scratch = new Scratch();
                    scratchSizes.put(nodename, scratch);
                }
                if (type == ScratchType.local) {
                    scratch.setLocalFreeKiB(parseScratchCacheValue(pce.getValue()));
                } else if (type == ScratchType.ssd) {
                    scratch.setSsdFreeKiB(parseScratchCacheValue(pce.getValue()));
                } else if (type == ScratchType.pool) {
                    Long size = pools.get(pce.getValue());
                    if (size != null) {
                        scratch.setSharedFreeKiB(size);
                    }
                }
            }
        }
        return true;
    }

    private static long parseScratchCacheValue(String s) {
        int idx = s.indexOf(';');
        if (idx >= 0) {
            s = s.substring(0, idx);
        }
        return Long.parseLong(s);
    }

    private Map<String, Long> loadNetworkScratchSizes(PbsServerConfig serverConfig) {
        long mez = System.currentTimeMillis() - (4 * 60 * 60 * 1000L);
        Map<String, Long> map = new HashMap<>();
        log.trace("loadNetworkScratchSizes({})", serverConfig.getHost());
        List<PbsCacheEntry> entries = new ArrayList<>();
        loadMetrics(serverConfig, "dynamic_resources", entries);
        entries.stream()
                .filter(x -> x.getKey().startsWith("scratch"))
                .filter(x -> x.getTimestamp() > mez)
                .forEach(x -> map.put(x.getKey(), Long.parseLong(x.getValue())));
        return map;
    }

    private void loadMapping() {
        Mapping m = new Mapping();
        m.setPhysical2virtual(new HashMap<>());
        m.setVirtual2physical(new HashMap<>());
        for (Map.Entry<String, List<String>> me : moreMappings.entrySet()) {
            m.getPhysical2virtual().put(me.getKey(), me.getValue());
            for (String vm : me.getValue()) {
                m.getVirtual2physical().put(vm, me.getKey());
            }
        }
        this.mapping = m;
    }

    private Map<String, List<String>> moreMappings = new HashMap<>();

    public void setMoreMappings(Map<String, List<String>> moreMappings) {
        this.moreMappings = moreMappings;
    }

}