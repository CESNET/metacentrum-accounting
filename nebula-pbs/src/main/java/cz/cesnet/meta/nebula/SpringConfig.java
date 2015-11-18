package cz.cesnet.meta.nebula;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
@Configuration
@EnableTransactionManagement
@PropertySource("classpath:config.properties")
@ComponentScan(basePackages = "cz.cesnet.meta.nebula")
public class SpringConfig {

    @Autowired
    Environment env;

    @Bean
    public DataSource dataSource() {
        BasicDataSource bds = new BasicDataSource();
        bds.setDriverClassName(env.getProperty("jdbc.driver"));
        bds.setUrl(env.getProperty("jdbc.url"));
        bds.setUsername(env.getProperty("jdbc.user"));
        bds.setPassword(env.getProperty("jdbc.password"));
        return bds;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }


}
