package cz.cesnet.meta.acct.hw;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class FixDuplicateUsers {

    public static void main(String[] args) {
        ApplicationContext springCtx = new ClassPathXmlApplicationContext("spring-context.xml","perun-config.xml");

        Accounting acct = springCtx.getBean("acct", Accounting.class);

        acct.fixDuplicateUsers();
    }
}
