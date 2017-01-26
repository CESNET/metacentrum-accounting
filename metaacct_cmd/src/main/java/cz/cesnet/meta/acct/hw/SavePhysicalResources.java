package cz.cesnet.meta.acct.hw;

import cz.cesnet.meta.acct.hw.perun.ComputingResource;
import cz.cesnet.meta.acct.hw.perun.Machine;
import cz.cesnet.meta.perun.api.PerunUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;
import java.util.Set;

public class SavePhysicalResources {

    final static Logger log = LoggerFactory.getLogger(SavePhysicalResources.class);

    public static void main(String[] args) {



        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml","perun-config.xml");
        Perun perun = springCtx.getBean("perun", Perun.class);

        List<ComputingResource> computingResources = perun.getComputingResources();

        List<PerunUser> allUsers = perun.getAllUsers();

        boolean dryRun = false;

        if (args.length > 0) {
            if (args[0].equals("-printonly")) {
                dryRun = true;
            }
        }

        if (dryRun) {
            //printResources(computingResources);
            printUsers(allUsers);
        } else {
            Accounting acct = springCtx.getBean("acct", Accounting.class);
            Set<String> frontends = perun.getFrontendNames();
            Set<String> reserved = perun.getReservedMachinesNames();
            acct.checkMachinesTablesConsistency();
            log.info(".. saving snapshot of physical machines to accounting database");
            acct.saveComputingResources(computingResources, frontends, reserved);
            log.info(".. updating user data in accounting database");
            acct.updateUsers(allUsers);
        }

    }

    private static void printUsers(List<PerunUser> allUsers) {
        for(PerunUser user : allUsers) {
            System.out.println(user.toString());
        }
    }

    private static void printResources(List<ComputingResource> computingResources) {
        int cpus = 0;
        for (ComputingResource cr : computingResources) {
            if (cr.isCluster()) {
                System.out.println("cluster " + cr.getName());
                for (Machine m : cr.getMachines()) {
                    System.out.println("         " + m.getName() + " cpu=" + m.getCpuNum());
                    cpus += m.getCpuNum();
                }
            } else {
                Machine m = cr.getMachine();
                System.out.println("machine " + m.getName());
                System.out.println("         " + m.getName() + " cpu=" + m.getCpuNum());
                cpus += m.getCpuNum();
            }
        }
        System.out.println("");
        System.out.println("celkem cpu: " + cpus);
    }

    private static int countCPUs(List<ComputingResource> computingResources) {
        int cpus = 0;
        for (ComputingResource cr : computingResources) {
            if (cr.isCluster()) {
                System.out.println("cluster " + cr.getName());
                for (Machine m : cr.getMachines()) {
                    cpus += m.getCpuNum();
                }
            } else {
                Machine m = cr.getMachine();
                cpus += m.getCpuNum();
            }
        }
        return cpus;
    }
}