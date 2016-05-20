package cz.cesnet.meta.acct.hw;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: LoadMapping.java,v 1.1 2009/09/29 14:25:55 makub Exp $
 */
public class LoadMapping {

    final static Logger log = LoggerFactory.getLogger(LoadMapping.class);

    public static void main(String[] args) throws IOException {
        log.debug("starting ...");
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml");
        Accounting acct = springCtx.getBean("acct",Accounting.class);
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("/home/makub/map.txt")));
        String line;
        while((line=in.readLine())!=null) {
            String[] strings = line.split("\t");
            String virt = strings[0];
            String fyz = strings[2];
            acct.insertMapping(virt,fyz);
        }
        in.close();
        System.out.println("done");
    }
}
