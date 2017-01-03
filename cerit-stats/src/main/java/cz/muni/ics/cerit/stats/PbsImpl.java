package cz.muni.ics.cerit.stats;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class PbsImpl implements Pbs {

    final static Logger log = LoggerFactory.getLogger(PbsImpl.class);
    List<String> pbsURLs = Collections.singletonList("https://wiki.metacentrum.cz/pbsmon2/api/nodes?serverName=wagap.cerit-sc.cz");

    public void setPbsURLs(List<String> pbsURLs) {
        this.pbsURLs = pbsURLs;
    }

    @Override
    public Map<String, PbsNode> getPbsNodesMap() {
        //possible states:      ["down,offline", "job-exclusive", "free", "down"]
        Map<String, PbsNode> pbsNodesMap = new HashMap<String, PbsNode>();
        Set<String> states = new HashSet<String>(20);
        for (String pbsURL : pbsURLs) {
            RestTemplate rt = new RestTemplate();
            JsonNode root = rt.getForObject(pbsURL, JsonNode.class);
            JsonNode nodes = root.path("nodes");
            for (Iterator<Map.Entry<String, JsonNode>> fields = nodes.fields(); fields.hasNext(); ) {
                JsonNode node = fields.next().getValue();
                String nodeName = node.path("name").asText();
                JsonNode attributes = node.path("attributes");
                String ntype = attributes.path("ntype").asText();
                String queue = attributes.path("queue").asText();
                String state = attributes.path("state").asText();
                String jobs = attributes.path("jobs").asText();
                String note = attributes.path("note").asText();
                PbsNode pbsNode = new PbsNode(nodeName, ntype, queue, state, jobs, note);
                pbsNodesMap.put(nodeName, pbsNode);
                states.add(state);
            }
        }
        if (log.isDebugEnabled()) log.debug("states: " + states);
        return pbsNodesMap;
    }
}
