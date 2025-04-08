package cz.cesnet.meta.perun.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdicons.json.model.*;
import com.sdicons.json.parser.JSONParser;
import cz.cesnet.meta.perun.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementace čtoucí JSON data. Oproti předchozí PerunJdbcImpl nepotřebuje žádné on-line spojení na Peruna,
 * jen čte soubory ve formátu JSON vygenerované Perunem.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PerunJsonImpl extends PerunAbstractImpl {

    final static Logger log = LoggerFactory.getLogger(PerunJsonImpl.class);

    //keys for pbsmon_users.json structure
    public static final String LOGNAME = "logname";
    public static final String NAME = "name";
    public static final String ORG = "org";
    public static final String STATISTIC_GROUPS = "statistic_groups";
    public static final String STATUS = "status";
    public static final String EXPIRES = "expires";
    public static final String PUBLICATIONS = "publications";
    public static final String META_CENTRUM_VO_NAME = "MetaCentrum";

    static Map<String, Map<String, String>> texts;
    List<File> machineFiles;
    List<File> userFiles;
    Map<File, Long> loadedTimes = new HashMap<>();
    List<OwnerOrganisation> ownerOrganisations;
    Map<String, PerunUser> users;
    List<PerunMachine> allMachines;
    Map<String, PerunMachine> allMachinesMap;
    Map<String, PerunComputingResource> resourcesMap;
    ReservedMachinesFinder reservedMachinesFinder;
    FrontendFinder frontendFinder;
    private long lastCheckTime = 0L;

    public PerunJsonImpl(List<String> machineFiles, List<String> userFiles) {
        log.debug("init machineFiles={}, userFiles={}", machineFiles, userFiles);
        this.machineFiles = new ArrayList<>(machineFiles.size());
        for (String s : machineFiles) {
            File f = new File(s);
            if (!f.exists()) {
                log.error("file {} does not exist !", f);
            }
            this.machineFiles.add(f);
        }
        this.userFiles = new ArrayList<>(userFiles.size());
        for (String s : userFiles) {
            File f = new File(s);
            if (!f.exists()) {
                log.error("file {} does not exist !", f);
            }
            this.userFiles.add(f);
        }
        checkFiles();
    }

    //tohle je trochu prasárna, ale jak jinak se mám k tomu dostat ze třídy PerunResourceBundle ?
    public static Map<String, Map<String, String>> getTexts() {
        return texts;
    }

    private static String getString(JSONObject jsonObject, String key) {
        JSONValue value = jsonObject.get(key);
        if (value == null || value.isNull()) return "";
        if (value.isString()) {
            return ((JSONString) value).getValue();
        }
        throw new RuntimeException("value for key " + key + " must be string, it is " + value);
    }

    @SuppressWarnings("SameParameterValue")
    private static int getInt(JSONObject jsonObject, String key) {
        JSONValue value = jsonObject.get(key);
        if (value == null || value.isNull()) return 0;
        if (value.isInteger()) {
            return ((JSONInteger) value).getValue().intValue();
        }
        throw new RuntimeException("value for key " + key + " must be integer, it is " + value);
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean getBoolean(JSONObject jsonObject, String key) {
        JSONValue value = jsonObject.get(key);
        if (value == null) {
            throw new NullPointerException("cannot load boolean value for key '" + key + "'");
        }
        if (value.isBoolean()) {
            return ((JSONBoolean) value).getValue();
        }
        if (value.isString()) {
            return Boolean.parseBoolean(((JSONString) value).getValue());
        }
        throw new RuntimeException("value for key " + key + " must be boolean or string, i.e. true or \"true\", it is " + value.toString());
    }

    private static void loadTexts(String position, Map<String, Map<String, String>> texts, JSONObject jsonObject, String bundleKey, String jsonKey) {
        JSONValue jsonValue = jsonObject.get(jsonKey);
        if (jsonValue != null && jsonValue.isObject()) {
            JSONObject jtexty = (JSONObject) jsonValue;
            if (log.isDebugEnabled()) log.debug("Loading " + position + " jtexty=" + jtexty);
            for (String lang : jtexty.getValue().keySet()) {
                Map<String, String> map = texts.get(lang.trim());
                if (map == null) {
                    map = new HashMap<>(100);
                    texts.put(lang.trim(), map);
                    log.warn("Created text bundle for language '{}'", lang.trim());
                }
                map.put(bundleKey, getString(jtexty, lang));
            }
        } else {
            log.warn("for " + position + " value for key " + jsonKey + " is not a map of i18n texts, it is " + (jsonValue == null ? "null" : jsonValue.render(false)));
        }
        texts.get("cs").putIfAbsent(bundleKey, "");
        texts.get("en").putIfAbsent(bundleKey, "");
    }

    private static <T> Iterable<T> iterable(final Iterator<T> it) {
        return () -> it;
    }

    private PerunComputingResource loadComputingResource(Map<String, Map<String, String>> texts, JSONObject jres, List<PerunMachine> allMachines, HashMap<String, PerunComputingResource> resourcesMap) {
        String zid = getString(jres, "id");
        boolean clust = getBoolean(jres, "cluster");
        log.debug("loading resource {}", zid);
        PerunComputingResource perunComputingResource = new PerunComputingResource(zid, getString(jres, "name"), clust);

        JSONArray vos = (JSONArray) jres.get("vos");
        if(vos!=null)  {
            for(JSONValue jv : vos.getValue()) {
                if(jv.isString()) perunComputingResource.getVoNames().add(((JSONString) jv).getValue());
            }
        }

        loadTexts("resource " + zid, texts, jres, perunComputingResource.getDescriptionKey(), "desc");
        loadTexts("resource " + zid, texts, jres, perunComputingResource.getSpecKey(), "spec");

        perunComputingResource.setPhoto(getString(jres, "photo"));
        perunComputingResource.setThumbnail(getString(jres, "thumbnail"));
        perunComputingResource.setCpuDesc(getString(jres, "cpudesc"));
        perunComputingResource.setGpuDesc(getString(jres, "gpudesc"));
        perunComputingResource.setMemory(getString(jres, "memory"));
        loadTexts("resource " + zid, texts, jres, perunComputingResource.getDiskKey(), "disk");
        loadTexts("resource " + zid, texts, jres, perunComputingResource.getNetworkKey(), "network");
        loadTexts("resource " + zid, texts, jres, perunComputingResource.getCommentKey(), "comment");
        loadTexts("resource " + zid, texts, jres, perunComputingResource.getOwnerKey(), "owner");



        if (perunComputingResource.isCluster()) {
            perunComputingResource.setPerunMachines(new ArrayList<>());
            for (JSONValue jv2 : ((JSONArray) jres.get("machines")).getValue()) {
                JSONObject jstroj = (JSONObject) jv2;
                PerunMachine perunMachine = new PerunMachine(perunComputingResource, getString(jstroj, "name"), getInt(jstroj, "cpu"));
                perunComputingResource.getPerunMachines().add(perunMachine);
                allMachines.add(perunMachine);
            }
            perunComputingResource.getPerunMachines().sort(PerunMachine.NAME_COMPARATOR);
        } else {
            PerunMachine perunMachine = new PerunMachine(perunComputingResource, perunComputingResource.getName(), getInt(jres, "cpu"));
            perunComputingResource.setPerunMachine(perunMachine);
            perunComputingResource.setPerunMachines(Collections.singletonList(perunMachine));
            allMachines.add(perunMachine);
        }
        resourcesMap.put(perunComputingResource.getId(), perunComputingResource);
        return perunComputingResource;
    }

    private synchronized void checkFiles() {
        long now = System.currentTimeMillis();
        if (now > lastCheckTime + 60000) {
            log.debug("checking files");
            //musím načíst všechny, protože data se spojují dohromady
            if (modified(this.userFiles)) loadUserFiles();
            if (modified(this.machineFiles)) loadMachineFiles();
            lastCheckTime = now;
        }
    }

    private boolean modified(List<File> files) {
        boolean modified = false;
        for (File f : files) {
            Long time = loadedTimes.get(f);
            if (time == null) time = 0L;
            if (f.lastModified() > time) modified = true;
        }
        return modified;
    }

    private void loadUserFiles() {
        Map<String, PerunUser> users = new HashMap<>(500);
        for (File f : userFiles) {
            log.info("loading {}", f);
            try {
                ObjectMapper mapper = new ObjectMapper();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                for (JsonNode juser : mapper.readValue(f, JsonNode.class).path("users")) {
                    PerunUser perunUser = new PerunUser();
                    //attributes independent of VO
                    perunUser.setLogname(juser.get(LOGNAME).textValue());
                    perunUser.setName(juser.get(NAME).textValue());
                    perunUser.setOrganization(juser.get(ORG).asText());
                    JsonNode jpubls = juser.path(PUBLICATIONS);
                    if (jpubls.isObject()) {
                        for (String key : iterable(jpubls.fieldNames())) {
                            perunUser.getPublications().put(key, jpubls.get(key).asInt());
                        }
                    }
                    //VO dependent attributes - expires, status, statistic_groups
                    JsonNode vos = juser.get("vos");
                    if(vos!=null&&vos.isObject()) {
                        for(String voName : iterable(vos.fieldNames())) {
                            JsonNode jvo = vos.get(voName);
                            PerunUser.Vo vo = new PerunUser.Vo(voName);
                            //status
                            vo.setStatus(jvo.get(STATUS).asText());
                            //organization
                            JsonNode o = jvo.get(ORG);
                            vo.setOrganization(o.isNull() ? null : o.asText());
                            //expires
                            try {
                                vo.setExpires(sdf.parse(jvo.path(EXPIRES).asText()));
                            } catch (ParseException ex) {
                                vo.setExpires(new GregorianCalendar(3000, Calendar.JANUARY, 1).getTime());
                            }
                            //stats groups
                            JsonNode statistic_groups = jvo.get(STATISTIC_GROUPS);
                            if(statistic_groups!=null&&statistic_groups.isObject()) {
                                for (String groupName : iterable(statistic_groups.fieldNames())) {
                                    int groupWeight = statistic_groups.get(groupName).asInt();
                                    vo.getStatsGroups().put(groupName,groupWeight);
                                }
                            }
                            if(vo.getStatsGroups().size()>1) {
                                log.debug("user {} has more than 1 statistics group",perunUser.getLogname());
                            }
                            perunUser.getVos().put(voName,vo);
                        }
                    }
                    //choose main Vo, MetaCentrum if available
                    PerunUser.Vo mainVo = perunUser.getVos().get(META_CENTRUM_VO_NAME);
                    if(mainVo==null) mainVo = new ArrayList<>(perunUser.getVos().values()).get(0);
                    perunUser.setMainVo(mainVo);

                    //store into collection
                    users.put(perunUser.getLogname(), perunUser);
                }
                loadedTimes.put(f, System.currentTimeMillis());
            } catch (Exception e) {
                log.error("error parsing " + f, e);
            }
        }
        this.users = users;
    }

    private void loadMachineFiles() {
        List<OwnerOrganisation> ownerOrganisations = new ArrayList<>();
        List<PerunMachine> allMachines = new ArrayList<>(500);
        HashSet<String> reservedNames = new HashSet<>();
        HashSet<String> frontendNames = new HashSet<>();
        HashMap<String, Map<String, String>> texts = new HashMap<>(2);
        HashMap<String, PerunComputingResource> computingResourcesMap = new LinkedHashMap<>(40);
        texts.put("cs", new HashMap<>(100));
        texts.put("en", new HashMap<>(100));

        for (File f : machineFiles) {
            log.info("loading {}", f);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
                final JSONParser jsonParser = new JSONParser(in);
                JSONObject perun_machines = (JSONObject) jsonParser.nextValue();
                in.close();
                //fyzicke stroje
                JSONArray physical_machines = (JSONArray) perun_machines.get("physical_machines");
                loadOwnerOrganisations(ownerOrganisations, texts, physical_machines, allMachines, computingResourcesMap);
                //vyhrazene
                JSONArray reserved_machines = (JSONArray) perun_machines.get("reserved_machines");
                for (JSONValue jv : reserved_machines.getValue()) {
                    reservedNames.add(((JSONString) jv).getValue());
                }
                //frontendy
                JSONArray frontends = (JSONArray) perun_machines.get("frontends");
                for (JSONValue jv : frontends.getValue()) {
                    frontendNames.add(((JSONString) jv).getValue());
                }
            } catch (Exception e) {
                log.error("error parsing " + f, e);
            }
            loadedTimes.put(f, System.currentTimeMillis());
        }
        //mapa pro rychlejší vyhledávání strojů podle jména
        Map<String, PerunMachine> allMachinesMap = new HashMap<>(allMachines.size() * 2);
        for (PerunMachine perunMachine : allMachines) {
            allMachinesMap.put(perunMachine.getName(), perunMachine);
        }
        allMachines.sort(PerunMachine.NAME_COMPARATOR);
        this.reservedMachinesFinder = new JsonReservedMachinesFinder(reservedNames);
        this.frontendFinder = new JsonFrontendFinder(frontendNames);
        PerunJsonImpl.texts = texts;
        this.ownerOrganisations = ownerOrganisations;
        this.allMachines = allMachines;
        this.allMachinesMap = allMachinesMap;
        this.resourcesMap = computingResourcesMap;
        PerunResourceBundle.refresh();
    }

    private void loadOwnerOrganisations(List<OwnerOrganisation> owners, HashMap<String, Map<String, String>> texts, JSONArray physical_machines, List<PerunMachine> allMachines, HashMap<String, PerunComputingResource> resourcesMap) {

        for (JSONValue cv : physical_machines.getValue()) {
            JSONObject jorg = (JSONObject) cv;
            String id = getString(jorg, "id");
            OwnerOrganisation ownerOrganisation = new OwnerOrganisation(id, "centrum-" + id + "-name", "centrum-" + id + "-url");
            loadTexts("centrum " + id, texts, jorg, ownerOrganisation.getNameKey(), "name");
            loadTexts("centrum " + id, texts, jorg, ownerOrganisation.getUrlKey(), "url");
            loadTexts("centrum " + id, texts, jorg, ownerOrganisation.getSpecKey(), "spec");

            ownerOrganisation.setPerunComputingResources(new ArrayList<>());
            for (JSONValue jv : ((JSONArray) jorg.get("resources")).getValue()) {
                PerunComputingResource perunComputingResource = loadComputingResource(texts, (JSONObject) jv, allMachines, resourcesMap);
                ownerOrganisation.getPerunComputingResources().add(perunComputingResource);
            }
            if(!ownerOrganisation.getPerunComputingResources().isEmpty()) {
                owners.add(ownerOrganisation);
            }
        }
    }

    @Override
    public List<OwnerOrganisation> findOwnerOrganisations() {
        checkFiles();
        return ownerOrganisations;
    }

    @Override
    public List<PerunMachine> getPerunMachines() {
        return allMachines;
    }

    @Override
    public PerunUser getUserByName(String userName) {
        return users.get(userName);
    }

    @Override
    public boolean isNodeVirtual(String nodeName) {
        return !isNodePhysical(nodeName);
    }

    @Override
    public boolean isNodePhysical(String nodeName) {
        return getMachineByName(nodeName) != null;
    }

    @Override
    public PerunMachine getMachineByName(String machineName) {
        return allMachinesMap.get(machineName);
    }

    @Override
    public PerunComputingResource getPerunComputingResourceByName(String name) {
        return resourcesMap.get(name);
    }

    /**
     * Vrací třídu, která rozhoduje, zda má být daný stroj označen jako modrý protože je mimo PBS záměrně.
     *
     * @return vyhledavac
     */
    @Override
    public ReservedMachinesFinder getReservedMachinesFinder() {
        return reservedMachinesFinder;
    }

    @Override
    public FrontendFinder getFrontendFinder() {
        return frontendFinder;
    }

    @Override
    public List<PerunUser> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private static class JsonReservedMachinesFinder extends NameFinder implements ReservedMachinesFinder {
        public JsonReservedMachinesFinder(Set<String> names) {
            super(names);
        }

        @Override
        public boolean isMachineReserved(PerunMachine perunMachine) {
            return this.found(perunMachine.getName());
        }
    }

    private static class JsonFrontendFinder extends NameFinder implements FrontendFinder {
        public JsonFrontendFinder(Set<String> mnozinaJmen) {
            super(mnozinaJmen);
        }

        @Override
        public boolean isFrontend(String longName) {
            return this.found(longName);
        }
    }
}
