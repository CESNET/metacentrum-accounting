package cz.cesnet.meta.pbscache;

import com.fasterxml.jackson.databind.JsonNode;
import com.sdicons.json.mapper.JSONMapper;
import com.sdicons.json.model.*;
import com.sdicons.json.parser.JSONParser;
import cz.cesnet.meta.RefreshLoader;
import cz.cesnet.meta.pbs.FairshareConfig;
import cz.cesnet.meta.pbs.Node;
import cz.cesnet.meta.pbs.PbsServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
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

    @Override
    public PbsAccess getUserAccess(String userName) {
        PbsAccess p = new PbsAccess();
        p.setQueues(new ArrayList<>(20));
        p.setQueueToHostsMap(new HashMap<>());
        for (PbsServerConfig server : pbsServers) loadPbsAccess(p, server, userName);
        return p;
    }

    private void loadPbsAccess(PbsAccess pbsAccess, PbsServerConfig serverConfig, String userName) {
        String server = serverConfig.getHost();
        log.debug("loadPbsAccess({},{})", server, userName);
        try {
            HttpURLConnection uc = (HttpURLConnection)
                    new URL("http://" + server + ":6666/pbsaccess?user=" + userName).openConnection();
            PbsAccess pb2 = (PbsAccess) JSONMapper.toJava(new JSONParser(uc.getInputStream()).nextValue(), PbsAccess.class);

            if (serverConfig.isMain()) {
                //spojit fronty
                pbsAccess.getQueues().addAll(pb2.getQueues());
                //spojit mapovani fronty na uzly
                pbsAccess.getQueueToHostsMap().putAll(pb2.getQueueToHostsMap());
            } else {
                //String pripona = "@" + PbsUtils.substringBefore(server, '.');
                String pripona = "@" + server;
                for (String queue : pb2.getQueues()) {
                    pbsAccess.getQueues().add(queue + pripona);
                }
                for (String queue : pb2.getQueueToHostsMap().keySet()) {
                    pbsAccess.getQueueToHostsMap().put(queue + pripona, pb2.getQueueToHostsMap().get(queue));
                }
            }
        } catch (Exception ex) {
            log.error("cannot load user access for " + userName + " from " + server);
            log.error(ex.getClass().getCanonicalName() + ": " + ex.getMessage());
        }
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

    final static Logger loggpu = LoggerFactory.getLogger(PbsCacheImpl.class.getName()+".gpu");
    /**
     * Maps hostname to map(gpu,jobId).
     */
    private Map<String,Map<String,String>> gpuAllocMap = new HashMap<>();

    @Override
    public Map<String, String> getGpuAlloc(Node node) {
        return gpuAllocMap.get(node.getName());
    }

    private void loadGpuAllocation() {
        loggpu.debug("loadGpuAllocation()");
        Map<String,Map<String,String>> allocMap = new HashMap<>();
        // "value": "unallocated",    "key": "gram4.zcu.cz:/dev/nvidia2"
        // "value": "4688478.arien.ics.muni.cz","key": "gram1.zcu.cz:/dev/nvidia2"
        List<PbsCacheEntry> entries = new ArrayList<>();
        for (PbsServerConfig server : pbsServers) {
            loadMetrics(server, "gpu_allocation", entries);
        }
        Pattern p = Pattern.compile("([^:]+):(.*)");
        for(PbsCacheEntry pce : entries) {
            String jobId = pce.getValue();
            if("unallocated".equals(jobId)) continue;
            Matcher m = p.matcher(pce.getKey());
            if(m.matches()) {
                String hostname = m.group(1);
                String gpu = m.group(2);

                Map<String, String> gpu2jobIdMap = allocMap.get(hostname);
                if(gpu2jobIdMap==null) {
                    gpu2jobIdMap = new HashMap<>();
                    allocMap.put(hostname,gpu2jobIdMap);
                }
                gpu2jobIdMap.put(gpu,jobId);
            } else {
                loggpu.warn("key "+pce.getKey()+" not matching regex");
            }
        }
        if (loggpu.isDebugEnabled()) {
            for(Map.Entry<String,Map<String,String>> me : allocMap.entrySet()) {
                loggpu.debug("gpu alloc host {} : {}",me.getKey(), me.getValue());
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
                Collections.sort(ftups, new Comparator<FairShareTuple>() {
                    @Override
                    public int compare(FairShareTuple t1, FairShareTuple t2) {
                        return t1.getFairshare().compareTo(t2.getFairshare());
                    }
                });
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

    private static boolean loadMetrics(PbsServerConfig serverConfig, String metrics, List<PbsCacheEntry> entries) {
        String server = serverConfig.getHost();
        log.trace("loadMetrics({},{})", server, metrics);
        HttpURLConnection uc;
        try {
            uc = (HttpURLConnection) new URL("http://" + server + ":6666/pbs_cache/" + metrics).openConnection();
            JSONArray ja = (JSONArray) new JSONParser(uc.getInputStream()).nextValue();
            for (JSONValue js : ja.getValue()) {
                JSONObject jo = (JSONObject) js;
                JSONInteger ji = (JSONInteger) jo.get("timestamp");
                long timestamp = ji.getValue().longValue();
                timestamp *= 1000;//prevod na ms
                String key = ((JSONString) jo.get("key")).getValue();
                String value = ((JSONString) jo.get("value")).getValue();
                entries.add(new PbsCacheEntry(key, value, timestamp));
            }
            return true;
        } catch (Exception ex) {
            log.error("PbsCacheImpl.loadMetrics() cannot read " + metrics + " from " + server, ex);
        }
        return false;
    }



    static enum ScratchType {local, ssd, pool}

    private void loadScratchSizes() {
        Map<String, Scratch> scratchSizes = new HashMap<>();
        for (PbsServerConfig server : pbsServers) {
            for (ScratchType scratchType : ScratchType.values()) {
                loadScratchSizes(server, scratchSizes, scratchType);
            }
        }
        this.scratchSizes = scratchSizes;
    }

    @SuppressWarnings("Java8ReplaceMapGet")
    //TODO replace with direct call to " list_cache SERVER scratch_TYPE"
    private boolean loadScratchSizes(PbsServerConfig serverConfig, Map<String, Scratch> scratchSizes, ScratchType type) {
        String server = serverConfig.getHost();
        log.trace("loadScratchSizes({},{})", server, type);
        Map<String, Long> pools = null;
        if (type == ScratchType.pool) {
            pools = loadNetworkScratchSizes(serverConfig);
        }
        try {
            RestTemplate rt = new RestTemplate();
            long mez = System.currentTimeMillis() - (4 * 60 * 60 * 1000L);
            for (JsonNode jn : rt.getForObject("http://" + server + ":6666/pbs_cache/scratch_" + type, JsonNode.class)) {
                if (type == ScratchType.pool || jn.path("timestamp").asLong() * 1000 > mez) {
                    String nodename = jn.path("key").asText();
                    Scratch scratch = scratchSizes.get(nodename);
                    if (scratch == null) {
                        scratch = new Scratch();
                        scratchSizes.put(nodename, scratch);
                    }
                    if (type == ScratchType.local) {
                        scratch.setLocalFreeKiB(parseScratchCacheValue(jn.path("value").asText()));
                    } else if (type == ScratchType.ssd) {
                        scratch.setSsdFreeKiB(parseScratchCacheValue(jn.path("value").asText()));
                    } else if (type == ScratchType.pool) {
                        Long size = pools.get(jn.path("value").asText());
                        if (size != null) {
                            scratch.setSharedFreeKiB(size);
                        }
                    }
                }
            }
            return true;
        } catch (Exception ex) {
            log.warn("PbsCacheImpl.loadScratchSizes() cannot read scratch_" + type + " from " + server, ex);
        }
        return false;
    }

    private static long parseScratchCacheValue(String s) {
        int idx = s.indexOf(';');
        if(idx>=0) {
            s = s.substring(0,idx);
        }
        return Long.parseLong(s);
    }

    private Map<String, Long> loadNetworkScratchSizes(PbsServerConfig serverConfig) {
        Map<String, Long> map = new HashMap<>();
        try {
            log.trace("loadNetworkScratchSizes({})", serverConfig.getHost());
            RestTemplate rt = new RestTemplate();
            long mez = System.currentTimeMillis() - (4 * 60 * 60 * 1000L);
            JsonNode rootNode = rt.getForObject("http://" + serverConfig.getHost() + ":6666/pbs_cache/dynamic_resources", JsonNode.class);
            for (JsonNode jn : rootNode) {
                if (jn.path("timestamp").asLong() * 1000 > mez) {
                    String key = jn.path("key").asText();
                    if (key.contains("scratch")) {
                        map.put(key, jn.path("value").asLong());
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("PbsCacheImpl.loadNetworkScratchSizes() cannot read nfs scratch sizes from " + serverConfig.getHost(), ex);
        }
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