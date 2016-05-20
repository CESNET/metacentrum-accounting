package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.Application;
import cz.cesnet.meta.accounting.server.util.LocalizationHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AppManagerImpl extends JdbcDaoSupport implements AppManager {

    @Autowired
    LocalizationHelper messages;
    @Autowired
    DbUtilsManager dbUtilsManager;

    @Override
    public List<Application> getAllApps() {
        logger.debug("loading apps with regexps");
        List<Application> apps = getJdbcTemplate().query(
                "SELECT acct_app_id AS id, app_name AS name, app_order AS appOrder, binary_regex AS regex " +
                        "  FROM acct_app " +
                        "  ORDER BY appOrder ",
                new RowMapper<Application>() {
                    @Override
                    public Application mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Application(rs.getLong("id"),
                                rs.getString("name"),
                                rs.getLong("appOrder"),
                                rs.getString("regex"));
                    }
                }
        );
        logger.debug("Loaded " + apps.size() + " apps.");
        return apps;
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public void deleteApp(Long id) {
        logger.debug("deleting app with id " + id);
        getJdbcTemplate().update("DELETE FROM acct_app WHERE acct_app_id = ? ", id);
        logger.debug("app with id " + id + " deleted");
    }

    @Override
    public Application getAppById(Long id) {
        logger.debug("loading app with id " + id);
        if (id == null) {
            return new Application();
        }

        Map<String, Object> res = getJdbcTemplate().queryForMap(
                " SELECT acct_app_id AS id, app_name AS name, app_order AS appOrder, binary_regex AS regex " +
                        " FROM acct_app WHERE acct_app_id = ? ", id
        );
        Application app = new Application((Long) res.get("id"), (String) res.get("name"),
                (Long) res.get("appOrder"), (String) res.get("regex"));
        logger.debug("app with id " + id + " loaded");
        return app;
    }

    @Override
    public Application getAppByOrder(Long order) {
        logger.debug("loading app with app_order " + order);
        if (order == null) {
            throw new NullPointerException("parameter order cannot be null");
        }

        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                " SELECT acct_app_id AS id, app_name AS name, app_order AS appOrder, binary_regex AS regex " +
                        " FROM acct_app WHERE app_order = ? ", order
        );
        if (res != null && !res.isEmpty()) {
            Map<String, Object> item = res.get(0);
            logger.debug("app with app_order " + order + " loaded");
            return new Application((Long) item.get("id"), (String) item.get("name"),
                    (Long) item.get("appOrder"), (String) item.get("regex"));
        } else {
            logger.debug("app with app_order " + order + " doesnt exist");
            return null;
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRED)
    public void saveApp(Application app) {
        logger.debug("saving app with id " + app.getId());
        if (app.getId() == null) {
            getJdbcTemplate().update(
                    "INSERT INTO acct_app (acct_app_id, app_name, app_order, binary_regex)" +
                            " VALUES (?, ?, ?, ?)",
                    dbUtilsManager.getNextVal("acct_app", "acct_app_id"), app.getName(), app.getOrder(), app.getRegex()
            );
        } else {
            getJdbcTemplate().update(
                    "UPDATE acct_app SET " +
                            "  app_name = ?, " +
                            "  app_order = ?, " +
                            "  binary_regex = ? " +
                            " WHERE acct_app_id = ? ", app.getName(), app.getOrder(), app.getRegex(), app.getId()
            );
        }

        logger.debug("app with id " + app.getId() + " saved");
    }

}
