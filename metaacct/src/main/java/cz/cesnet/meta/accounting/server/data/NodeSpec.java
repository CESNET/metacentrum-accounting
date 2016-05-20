package cz.cesnet.meta.accounting.server.data;

import cz.cesnet.meta.accounting.web.util.PbsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reprezentuje prosttředky přidělené na jednom stroji.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class NodeSpec {
    final static Logger log = LoggerFactory.getLogger(NodeSpec.class);

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
}
