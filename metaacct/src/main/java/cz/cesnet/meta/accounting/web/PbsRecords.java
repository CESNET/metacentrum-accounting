package cz.cesnet.meta.accounting.web;

import cz.cesnet.meta.accounting.displaytag.PaginationFilter;
import cz.cesnet.meta.accounting.server.service.PbsRecordManager;
import cz.cesnet.meta.accounting.server.service.UserManager;
import cz.cesnet.meta.accounting.server.util.Page;
import cz.cesnet.meta.accounting.web.filter.PbsRecordsFilter;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.integration.spring.SpringBean;
import net.sourceforge.stripes.validation.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@SuppressWarnings("UnusedDeclaration")
public class PbsRecords extends AccountingWebBase implements ValidationErrorHandler {


    static private final Logger log = LoggerFactory.getLogger(PbsRecords.class);

    @SpringBean
    PbsRecordManager pbsRecordManager;
    @SpringBean
    UserManager userManager;

    @SpringBean
    PbsRecordsFilter pbsRecordsFilter;

    private Page pbsRecords;

    private long userId;
    private String username;

    private int periodInDays = 0;
    @Validate(converter = DateTypeConverter.class)
    private Date fromDate;
    @Validate(converter = DateTypeConverter.class)
    private Date toDate;
    private int number;

    public Resolution clear() {
        log.debug("clear()");
        pbsRecordsFilter.clear();
        return new RedirectResolution("/PbsRecords.action").addParameter("userId", userId);
    }

    @DefaultHandler
    public Resolution view() {
        log.debug("view()");
        if (number != 0) {
            pbsRecordsFilter.clear();
            log.debug("getting records for number");
            pbsRecords = pbsRecordManager.getPbsRecordsForUserId(userId, number, getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());
        } else {
            if (periodInDays != 0) {
                //pbsRecordsFilter.getDateTimeTo() == null) {
                pbsRecordsFilter.setDateTimeTo(new LocalDate().toDateTimeAtStartOfDay().toDate());
                pbsRecordsFilter.setDateTimeFrom(new DateTime(pbsRecordsFilter.getDateTimeTo().getTime()).minusDays(periodInDays).toDate());
                periodInDays = 0;
            }
            log.debug("getting records for period");
            pbsRecords = pbsRecordManager.getPbsRecordsForUserId(userId, getFilter().getSearchCriteria(), getPageNumber(), PaginationFilter.DEFAULT_PAGE_SIZE_SHORT, getPageSize(), getSortColumn(), isAscending());
        }

        username = userManager.getUserName(userId);
        log.debug("forwarding to /viewPbsRecords.jsp");
        return new ForwardResolution("/viewPbsRecords.jsp");
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public int getPeriodInDays() {
        return periodInDays;
    }

    public void setPeriodInDays(int periodInDays) {
        this.periodInDays = periodInDays;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Page getPbsRecords() {
        return pbsRecords;
    }

    public void setPbsRecords(Page pbsRecords) {
        this.pbsRecords = pbsRecords;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    @Override
    public PbsRecordsFilter getFilter() {
        return pbsRecordsFilter;
    }


    public void setFilter(PbsRecordsFilter filter) {
        this.pbsRecordsFilter = filter;
    }

    @ValidationMethod(on = "view")
    public void checkSafeFormValues(ValidationErrors errors) {
        checkMask(errors,getFilter().getWalltimeFrom(),"(\\d+:\\d\\d:\\d\\d)?","filter.walltimeFrom");
        checkMask(errors,getFilter().getWalltimeTo(),"(\\d+:\\d\\d:\\d\\d)?","filter.walltimeTo");
        checkMask(errors,getFilter().getQueue(),"[a-z0-9_]+","filter.queue");
        checkMask(errors,getFilter().getPbsServer(),"[a-z0-9_.]+","filter.pbsServer");
        checkMask(errors,getFilter().getJobname(),"[a-zA-Z0-9_.]+","filter.jobname");
        checkMask(errors,getFilter().getIdString(),"[a-zA-Z0-9_.]+","filter.idString");
        checkMask(errors,getFilter().getUsername(),"[a-zA-Z0-9_]+","filter.username");
    }

    private void checkMask(ValidationErrors errors, String value, String mask, String field) {
        if (value != null && !value.matches(mask)) {
            errors.add(field, new LocalizableError(field+".errorMessage"));
        }
    }

    @Override
    public Resolution handleValidationErrors(ValidationErrors errors) throws Exception {
        if (userId != 0) {
            username = userManager.getUserName(userId);
        } else {
            log.debug("userId = 0");
        }
        Page page = getPbsRecords();
        if (page == null) {
            page = new Page();
            setPbsRecords(page);
            log.debug("created new Page instance");
        }
        page.setPageSize(20);
        return new ForwardResolution("/viewPbsRecords.jsp");
    }
}
