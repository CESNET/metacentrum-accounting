package cz.cesnet.meta.nebula;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Main {

    public static void main(String[] args) throws IOException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        PbsNebula nebula = ctx.getBean(PbsNebula.class);

        //nebula.fixNodeAssignment();

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        List<VMHost> vmHosts
                = mapper.readValue(new File("/home/makub/pbs_vms.yaml"), new TypeReference<List<VMHost>>() {});
        nebula.markNodes(vmHosts);

    }

    @JsonPropertyOrder({":name", ":history_records"})
    public static class VMHost {

        @JsonProperty(":name")
        private String name;

        @JsonProperty(":history_records")
        private HistoryRecord[] historyRecords;

        public String getName() {
            return name;
        }

        public HistoryRecord[] getHistoryRecords() {
            return historyRecords;
        }

        @Override
        public String toString() {
            return "VMHost{" +
                    "name='" + name + '\'' +
                    ", historyRecords=" + Arrays.toString(historyRecords) +
                    '}';
        }
    }

    public static class HistoryRecord {
        @JsonProperty(":hostname")
        private String hostname;

        @JsonProperty(":start_time")
        private long startTime;

        @JsonProperty(":end_time")
        private long endTime;

        public String getHostname() {
            return hostname;
        }

        public Timestamp getStartTime() {
            return new Timestamp(startTime * 1000L);
        }

        public Timestamp getEndTime() {
            return endTime == 0L ? null : new Timestamp(endTime * 1000L);
        }

        @Override
        public String toString() {
            return "HistoryRecord{" +
                    "hostname='" + hostname + '\'' +
                    ", startTime=" + getStartTime() +
                    ", endTime=" + getEndTime() +
                    '}';
        }
    }
}
