package cz.cesnet.meta.accounting.server.service;

import cz.cesnet.meta.accounting.server.data.User;
import cz.cesnet.meta.accounting.server.util.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.*;

public class UserManagerImpl extends JdbcDaoSupport implements UserManager {
    private static Logger logger = LoggerFactory.getLogger(UserManagerImpl.class);

    @Autowired
    DbUtilsManager dbUtilsManager;

    public Map<String, Long> saveUsernames(Collection<String> usernames) {
        logger.debug("Saving " + usernames.size() + " usernames");
        Map<String, Long> users = loadUsers();

        SimpleJdbcInsert insertUser = new SimpleJdbcInsert(getDataSource())
                .withTableName("acct_user")
                .usingColumns("acct_user_id", "user_name");

        for (String s : usernames) {
            if (!users.containsKey(s)) {
                Map<String, Object> params = new HashMap<>(2);
                Long nextVal = dbUtilsManager.getNextVal("acct_user", "acct_user_id");
                params.put("acct_user_id", nextVal);
                params.put("user_name", s);
                insertUser.execute(params);

                users.put(s, nextVal);
            }
        }
        return users;
    }

    public Map<String, Long> loadUsers() {
        logger.debug("Loading users into Map");
        Map<String, Long> users = new HashMap<>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "SELECT acct_user_id, user_name FROM acct_user;");

        for (Map<String, Object> item : res) {
            users.put((String) item.get("user_name"), (Long) item.get("acct_user_id"));
        }

        logger.debug("Loaded " + users.size() + " users.");
        return users;
    }

    /**
     * nacte uzivatele a pocet jejich pbs zaznamu
     *
     * @return seznam uzivatelu
     */
    public Page getAllUsersWithPbsRecordNumber(Integer pageNumber, Integer defaultPageSize, Integer pageSize,
                                               String sortColumn, Boolean ascending) {
        logger.debug("loading users with number of his pbs records");
        if (pageSize == null) {
            pageSize = defaultPageSize;
        }
        if (sortColumn == null) {
            ascending = true;
            sortColumn = "username";
        }
        Integer offset = (pageNumber - 1) * pageSize;
        Page page = new Page();
        page.setPageSize(pageSize);
        page.setPageNumber(pageNumber);
        page.setSortColumn(sortColumn);
        page.setFullListSize((int) getSize());
        page.setAscending(ascending);

        List<User> users = new ArrayList<>();
        List<Map<String, Object>> res = getJdbcTemplate().queryForList(
                "select u.acct_user_id, u.user_name as username, count(*) as  numberOfPbsRecords " +
                        "  from acct_user u, acct_pbs_record p " +
                        "  where u.acct_user_id = p.acct_user_id " +
                        "  group by u.acct_user_id, username " +
                        "  order by " + sortColumn + " " + (ascending ? "ASC" : "DESC") +
                        "  limit " + pageSize + " offset " + offset + " "
        );

        for (Map<String, Object> item : res) {
            users.add(new User(
                    (Long) item.get("acct_user_id"),
                    (String) item.get("username"),
                    (Long) item.get("numberOfPbsRecords")
            ));
        }
        logger.debug("Loaded " + users.size() + " users.");
        page.setList(users);
        return page;
    }

    /**
     * @return vrati pocet uzivatelu, kteri maji nejakou pbs  ulohu
     */
    public long getSize() {
        long size = getJdbcTemplate().queryForObject("SELECT count(DISTINCT p.acct_user_id) " +
                "  FROM acct_pbs_record p ", Long.class);
        logger.debug("size of users : " + size);
        return size;
    }

    /**
     * @param userId id uzivatele
     * @return uzivatelske jmeno pro zadane useriD
     */
    public String getUserName(long userId) {
        return getJdbcTemplate().queryForObject(
                "SELECT u.user_name FROM acct_user u WHERE u.acct_user_id = ?", String.class, userId);
    }

    /**
     * @param username uzivatelske jmeno
     * @return id uzivatele pro dane uzivatelske jmeno
     */
    public Long getUserId(String username) {

        Long userId = null;
        try {
            userId = getJdbcTemplate().queryForObject(
                    "SELECT u.acct_user_id FROM acct_user u WHERE u.user_name = ?", Long.class, username);
        } catch (EmptyResultDataAccessException e) {
            logger.info("bad username without user id");
        }
        return userId;
    }
}
