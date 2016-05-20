package cz.cesnet.meta.acct;

import cz.cesnet.meta.pbs.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Martin Kuba makub@ics.muni.cz
 * @version $Id: AccountingImpl.java,v 1.10 2014/09/11 11:50:56 makub Exp $
 */
public class AccountingImpl implements Accounting {
    final static Logger log = LoggerFactory.getLogger(AccountingImpl.class);

    private JdbcTemplate jdbc;

    private static final RowMapper<OutageRecord> OUTAGE_MAPPER = new RowMapper<OutageRecord>() {
        @Override
        public OutageRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new OutageRecord(
                    rs.getString("hostname"),
                    rs.getString("type"),
                    rs.getTimestamp("start_time"),
                    rs.getTimestamp("end_time"),
                    rs.getString("comment"));
        }
    };

    public void setDataSource(DataSource dataSource) {
        if (log.isDebugEnabled()) {
            log.debug("[" + super.toString() + "].setDataSource(" + dataSource + ")");
            try {
                Connection connection = dataSource.getConnection();
                DatabaseMetaData md = connection.getMetaData();
                log.debug("db: " + md.getDatabaseProductName() + " " + md.getDatabaseProductVersion());
                connection.close();
            } catch (SQLException e) {
                log.error("Nemohu zjistit databazi", e);
            }
        }
        this.jdbc = new JdbcTemplate(dataSource);
    }


    @Override
    public UserInfo getUserInfoByName(String userName) {
        log.debug("getUserInfoByName({})", userName);

        UserInfo ui = jdbc.queryForObject("select count(*),sum(used_walltime*used_ncpus) from acct_user u,acct_pbs_record p where user_name=? and u.acct_user_id=p.acct_user_id",
                new RowMapper<UserInfo>() {
                    @Override
                    public UserInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new UserInfo(rs.getInt(1), rs.getLong(2));
                    }
                }, userName);
        List<UserInfo.Usage> usages
                = jdbc.query("select count(*),sum((j.end_time-j.start_time)*j.used_ncpus),extract( year from to_timestamp(j.end_time)) as yearnum " +
                "from acct_user u,acct_pbs_record j " +
                "where u.user_name=? and u.acct_user_id=j.acct_user_id group by yearnum order by yearnum",
                new RowMapper<UserInfo.Usage>() {
                    @Override
                    public UserInfo.Usage mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new UserInfo.Usage(rs.getInt("yearnum"), rs.getLong(1), rs.getLong(2));
                    }
                }, userName);
        ui.setUsages(usages);
        return ui;
    }

    @Override
    public List<OutageRecord> getOutagesForNode(Node node) {
        final String nodeName = node.getName();
        return jdbc.query("select hostname,type,start_time,end_time,comment from acct_host h,acct_outages o where h.hostname=? and h.acct_host_id=o.acct_host_id order by o.start_time desc",
                OUTAGE_MAPPER,
                nodeName);
    }

    @Override
    public List<String> getStartedJobIds() {
        log.debug("getStartedJobIds()");
        return jdbc.queryForList("select acct_id_string from acct_pbs_record_started order by acct_id_string",String.class);
    }
}
