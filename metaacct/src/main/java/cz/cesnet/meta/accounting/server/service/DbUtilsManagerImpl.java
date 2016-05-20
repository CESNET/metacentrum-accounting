package cz.cesnet.meta.accounting.server.service;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


public class DbUtilsManagerImpl extends JdbcDaoSupport implements DbUtilsManager {

    private static Logger logger = Logger.getLogger(DbUtilsManagerImpl.class);


    @Transactional(propagation = Propagation.MANDATORY, isolation = Isolation.SERIALIZABLE)
    public Long getNextVal(String tableName, String primaryKeyName) {
        String query = "select nextval('" + tableName + "_" + primaryKeyName + "_" + "seq');";
        Long nextval = getJdbcTemplate().queryForObject(query, Long.class);
        if (logger.isDebugEnabled()) {
            logger.debug(query + ": " + nextval);
        }
        return nextval;
    }

}
