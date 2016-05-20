package cz.cesnet.meta.accounting.server.data;

import java.io.Serializable;

public class KernelRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String command;
    private long userTime;
    private long systemTime;
    private long elapsedTime;
    private long createTime;
    private String username;
    private long exitcode;
    private long mem;
    private long rw;
    private long swaps;
    private long ppid;
    private long pid;
    private PBSHost host;
    private String app;

    public KernelRecord() {
    }

    public KernelRecord(long id, String command, long userTime, long systemTime, long elapsedTime, long createTime, String username, long exitcode, long mem, long rw, long swaps, String app) {
        this.id = id;
        this.command = command;
        this.userTime = userTime;
        this.systemTime = systemTime;
        this.elapsedTime = elapsedTime;
        this.createTime = createTime;
        this.username = username;
        this.exitcode = exitcode;
        this.mem = mem;
        this.rw = rw;
        this.swaps = swaps;
        this.app = app;
    }


    public KernelRecord(long id, String command, String app, long userTime, long systemTime, long elapsedTime, long createTime,
                        long exitcode, long hostId, String hostName) {
        this.id = id;
        this.command = command;
        this.app = app;
        this.userTime = userTime;
        this.systemTime = systemTime;
        this.elapsedTime = elapsedTime;
        this.createTime = createTime;
        this.exitcode = exitcode;
        this.host = new PBSHost(hostId, hostName);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public long getExitcode() {
        return exitcode;
    }

    public void setExitcode(long exitcode) {
        this.exitcode = exitcode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMem() {
        return mem;
    }

    public void setMem(long mem) {
        this.mem = mem;
    }

    public long getRw() {
        return rw;
    }

    public void setRw(long rw) {
        this.rw = rw;
    }

    public long getSwaps() {
        return swaps;
    }

    public void setSwaps(long swaps) {
        this.swaps = swaps;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getUserTime() {
        return userTime;
    }

    public void setUserTime(long userTime) {
        this.userTime = userTime;
    }

    public PBSHost getHost() {
        return host;
    }

    public void setHost(PBSHost host) {
        this.host = host;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public long getPpid() {
        return ppid;
    }

    public void setPpid(long ppid) {
        this.ppid = ppid;
    }

    public String getHostname() {
        return host.getHostName();
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
