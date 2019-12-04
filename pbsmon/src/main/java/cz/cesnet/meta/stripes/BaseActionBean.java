package cz.cesnet.meta.stripes;

import cz.cesnet.meta.cloud.Cloud;
import cz.cesnet.meta.pbs.PBS;
import cz.cesnet.meta.pbs.Pbsky;
import cz.cesnet.meta.pbscache.PbsCache;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.integration.spring.SpringBean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class BaseActionBean implements ActionBean {
    protected ActionBeanContext ctx;

    public void setContext(ActionBeanContext context) {
        this.ctx = context;
    }

    public ActionBeanContext getContext() {
        return this.ctx;
    }

    public Date getNow() {
        return new Date();
    }

    @SpringBean("pbsky")
    protected Pbsky pbsky;

    @SpringBean("pbsCache")
    protected PbsCache pbsCache;

    @SpringBean("cloud")
    protected Cloud cloud;

    public List<TimeLoaded> getTimesLoaded() {
        Date oldTime = new Date(System.currentTimeMillis() - (6l * 60000l)); //hranice zastaralych dat, pro zvyrazneni
        //pbs servers
        List<PBS> pbsList = pbsky.getListOfPBS();
        List<TimeLoaded> tl = new ArrayList<TimeLoaded>(pbsList.size() + 1);
        for (PBS pbs : pbsList) {
            Date createdTime = pbs.getTimeLoaded();
            tl.add(new TimeLoaded(createdTime.before(oldTime), pbs.getServer().getShortName(), "PBS server", createdTime));
        }
        //pbs_cache
        Date cacheLoaded = pbsCache.getTimeLoaded();
        tl.add(new TimeLoaded(cacheLoaded.before(oldTime), null, "PBS cache", cacheLoaded));
        //cloud
        Date cloudLoaded = cloud.getTimeLoaded();
        tl.add(new TimeLoaded(cloudLoaded.before(oldTime), null, "MetaCentrum Cloud", cloudLoaded));
        return tl;
    }

    public static class TimeLoaded {

        private boolean old = false;
        private String server;
        private String service;
        private Date time;

        public TimeLoaded(boolean old, String server, String service, Date time) {
            this.old = old;
            this.server = server;
            this.service = service;
            this.time = time;
        }

        public boolean isOld() {
            return old;
        }

        public String getServer() {
            return server;
        }

        public String getService() {
            return service;
        }

        public Date getTime() {
            return time;
        }
    }
}