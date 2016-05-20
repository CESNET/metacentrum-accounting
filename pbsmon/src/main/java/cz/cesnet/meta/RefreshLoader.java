package cz.cesnet.meta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Loader that returns immediately old data and starts loading new data in a new thread.
 * <p/>
 * Usage:
 * <pre>
 *     class MyLoader extends RefreshLoader {
 *
 *         private Spam spam;
 *
 *         public Spam getSpam() {
 *             checkLoad();
 *             return spam;
 *         }
 *
 *         protected load() {
 *             //load somehow
 *             spam = ...;
 *         }
 *     }
 * </pre>
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public abstract class RefreshLoader implements TimeStamped {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private AtomicBoolean loadStarted = new AtomicBoolean(false);
    private Date timeLoadStarted;
    private Date timeLoaded;
    private long dataMaxAgeInMilliseconds = 60000;
    private long loadRunTimeInMilliseconds = 5000;

    /**
     * Implements loading. Should assign loaded data atomically.
     */
    protected abstract void load();

    /**
     * Gets the time the data were loaded.
     *
     * @return time as Date
     */
    @Override
    public Date getTimeLoaded() {
        checkLoad();
        return timeLoaded;
    }

    /**
     * Sets maximum data age after which a new load will start.
     * Default is 60 seconds.
     *
     * @param millis milliseconds
     */
    public void setDataMaxAgeInMilliseconds(long millis) {
        this.dataMaxAgeInMilliseconds = millis;
    }

    /**
     * Sets average time to load. Just an estimate of needed refresh time.
     * Default is 5 seconds.
     *
     * @param millis milliseconds
     */
    public void setLoadRunTimeInMilliseconds(long millis) {
        this.loadRunTimeInMilliseconds = millis;
    }

    /**
     * Call this method in every public method first.
     *
     * @return refresh time in seconds
     */
    protected int checkLoad() {
        if (timeLoaded == null) {
            if (loadStarted.compareAndSet(false, true)) {
                log.debug("loading data on the very first request");
                //if first attempt, load in this thread to wait for its finish
                timeLoadStarted = new Date();
                new LoadRunner().run();
            } else {
                //other attempts wait for the first one to finish
                while (loadStarted.get()) {
                    try {
                        Thread.sleep(loadRunTimeInMilliseconds);
                    } catch (InterruptedException e) {
                        log.error("interrupted", e);
                    }
                }
            }
        }
        //old data are available here
        Date now = new Date();
        long refresh = 0;
        long maxTime = timeLoaded.getTime() + dataMaxAgeInMilliseconds;
        if (now.getTime() > maxTime) {
            if(log.isTraceEnabled()) {
                log.trace("data are too old - loaded={}, dataMaxAge={}, maxTime={}", timeLoaded,dataMaxAgeInMilliseconds,new Date(maxTime));
            }
            if (loadStarted.compareAndSet(false, true)) {
                log.trace("starting new load");
                //load not started, start it in a new thread
                timeLoadStarted = new Date();
                new LoadRunner().start();
                refresh = loadRunTimeInMilliseconds;
            } else {
                log.trace("load already started by another thread, do nothing");
                //load already started by another thread
                refresh = loadRunTimeInMilliseconds - (now.getTime() - timeLoadStarted.getTime());
                if (refresh < 0) {
                    refresh = loadRunTimeInMilliseconds;
                }
            }
        }
        return (int) (refresh / 1000l);
    }

    private int threadCounter = 1;

    private class LoadRunner extends Thread {

        public LoadRunner() {
            setDaemon(true);
            setName(RefreshLoader.this.getClass().getSimpleName() + "/LoadRunner-"+(threadCounter++));
        }

        @Override
        public void run() {
            log.debug("starting data load");
            loadStarted.set(true);
            try {
                load();
                timeLoaded = new Date();
            } catch (Exception ex) {
                log.warn("caught "+ex.getClass().getSimpleName()+": "+ex.getMessage());
                if(timeLoaded==null) timeLoaded = new Date();
            } finally {
                loadStarted.set(false);
            }
            long timeNeeded = timeLoaded.getTime() - timeLoadStarted.getTime();
            if (timeNeeded > loadRunTimeInMilliseconds) {
                log.warn("data not generated in time, needed {}ms instead of {}ms", timeNeeded, loadRunTimeInMilliseconds);
            }
            log.debug("data load finished");
        }
    }


}
