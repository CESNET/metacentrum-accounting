package cz.cesnet.meta.accounting.server.data;

import cz.cesnet.meta.accounting.web.util.PbsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje prosttředky přidělené na jednom stroji.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class NodeSpec {
    final static Logger log = LoggerFactory.getLogger(NodeSpec.class);

    private NodeSpec() {
    }

    public NodeSpec(String spec) {
        try {
            for (String attr : spec.split(":")) {
                String[] kv = attr.split("=", 2);
                if(kv.length!=2) continue; // like :cl_doom
                String key = kv[0];
                String value = kv[1];
                switch (key) {
                    case "host":
                        hostname = value;
                        break;
                    case "ppn":
                        ppn = Integer.parseInt(value);
                        break;
                    case "mem":
                        mem = PbsUtils.parsePbsBytes(value);
                        break;
                    case "gpu":
                        gpu = Integer.parseInt(value);
                        break;
                    case "scratch_type":
                        scratchType = value;
                        break;
                    case "scratch_volume":
                        scratchVolume = PbsUtils.parsePbsBytes(value);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("problem parsing "+spec,e);
        }
    }

    private String hostname;
    private int ppn;
    private int gpu;
    private long mem;
    private String scratchType;
    private long scratchVolume;

    public String getHostname() {
        return hostname;
    }

    public int getPpn() {
        return ppn;
    }


    public int getGpu() {
        return gpu;
    }

    public String getScratchType() {
        return scratchType;
    }

    public long getMem() {
        return mem;
    }

    public long getScratchVolume() {
        return scratchVolume;
    }

    public static List<NodeSpec> parseExecVnodePart(String exec_vnode) {
        String[] split1 = exec_vnode.split("\\+");
        List<NodeSpec> nodeSpecs = new ArrayList<>(split1.length);
        for (String spec : split1) {
            // (hildor3:ncpus=3:mem=819200kb:scratch_shared= 1048576kb)
            //remove () around
            spec = spec.substring(1, spec.length() - 1);
            try {
                NodeSpec nodeSpec = new NodeSpec();
                String[] split = spec.split(":");
                for (int i = 0; i < split.length; i++) {
                    String attr = split[i];
                    if (i == 0) {
                        nodeSpec.hostname = attr;
                        continue;
                    }
                    String[] kv = attr.split("=", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0];
                    String value = kv[1];
                    switch (key) {
                        case "ncpus":
                            nodeSpec.ppn = Integer.parseInt(value);
                            continue;
                        case "mem":
                            nodeSpec.mem = PbsUtils.parsePbsBytes(value);
                            continue;
                        case "ngpus":
                            nodeSpec.gpu = Integer.parseInt(value);
                            continue;
                    }
                    if (key.startsWith("scratch_")) {
                        nodeSpec.scratchType = key.substring("scratch_".length());
                        nodeSpec.scratchVolume = PbsUtils.parsePbsBytes(value);
                    }
                }
                nodeSpecs.add(nodeSpec);
            } catch (Exception e) {
                log.error("problem parsing " + spec, e);
            }
        }
        return nodeSpecs;
    }
}
