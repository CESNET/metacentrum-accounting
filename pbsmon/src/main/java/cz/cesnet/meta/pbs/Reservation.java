package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a reservation in PBSPro.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Reservation extends PbsInfoObject {

    public Reservation(PBS pbs, String name) {
        super(pbs, name);
    }

    public String getQueueName() {
        return attrs.get("queue");
    }

    private Queue queue;

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public Queue getQueue() {
        return queue;
    }

    private Date reserveStart;

    public Date getReserveStart() {
        if (reserveStart == null) {
            reserveStart = PbsUtils.getJavaTime(attrs.get("reserve_start"));
        }
        return reserveStart;
    }

    private Date reserveEnd;

    public Date getReserveEnd() {
        if (reserveEnd == null) {
            reserveEnd = PbsUtils.getJavaTime(attrs.get("reserve_end"));
        }
        return reserveEnd;
    }

    private Date ctime;

    public Date getCreatedTime() {
        if (ctime == null) {
            ctime = PbsUtils.getJavaTime(attrs.get("ctime"));
        }
        return ctime;
    }

    public String getOwner() {
        return PbsUtils.substringBefore(attrs.get("Reserve_Owner"), '@');
    }

    public String getReserveName() {
        return attrs.get("Reserve_Name");
    }

    public String getSelect() {
        return attrs.get("Resource_List.select");
    }

    private List<Node> nodes;

    public List<Node> getNodes() {
        if (nodes == null) {
            String resv_nodes = attrs.get("resv_nodes");
            if (resv_nodes == null) return null;
            List<Node> nodes = new ArrayList<>();
            for (String spec : resv_nodes.split("\\+")) {
                ReservedNode reservedNode = new ReservedNode();
                spec = spec.substring(1, spec.length() - 1);
                String[] strings = spec.split(":");
                for (int i = 0, n = strings.length; i < n; i++) {
                    if (i == 0) {
                        reservedNode.setName(strings[0]);
                        continue;
                    }
                    String[] kv = strings[i].split("=", 2);
                    if (kv.length != 2) continue;
                    String key = kv[0];
                    String value = kv[1];
                    switch (key) {
                        case "ncpus":
                            reservedNode.setNcpus(Integer.parseInt(value));
                            break;
                        case "mem":
                            reservedNode.setMem(PbsUtils.formatInPbsUnits(PbsUtils.parsePbsBytes(value)));
                            break;
                        case "ngpus":
                            reservedNode.setNgpus(Integer.parseInt(value));
                            break;
                    }
                }
                Node node = this.getPbs().getNodes().get(reservedNode.getName());
                nodes.add(node);
            }
            this.nodes = nodes;
        }
        return this.nodes;
    }

    public class ReservedNode {
        private String name;
        private int ncpus;
        private int ngpus;
        private String mem;

        public ReservedNode() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getNcpus() {
            return ncpus;
        }

        public void setNcpus(int ncpus) {
            this.ncpus = ncpus;
        }

        public int getNgpus() {
            return ngpus;
        }

        public void setNgpus(int ngpus) {
            this.ngpus = ngpus;
        }

        public String getMem() {
            return mem;
        }

        public void setMem(String mem) {
            this.mem = mem;
        }

        @Override
        public String toString() {
            return "ReservedNode{" +
                    "name='" + name + '\'' +
                    ", ncpus=" + ncpus +
                    ", ngpus=" + ngpus +
                    ", mem='" + mem + '\'' +
                    '}';
        }
    }
}
