package org.unitime.timetable.dataexchange;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.model.dao._RootDAO;

public class DataExchangeHelper {
    protected static Log sLog = LogFactory.getLog(DataExchangeHelper.class);
    public static String sLogLevelDebug = "DEBUG";
    public static String sLogLevelInfo = "INFO";
    public static String sLogLevelWarn = "WARN";
    public static String sLogLevelError = "ERROR";
    public static String sLogLevelFatal = "FATAL";
    protected PrintWriter iTextLog;
    protected org.hibernate.Session iHibSession = null;
    protected org.hibernate.Transaction iTx = null;
    protected int iFlushIfNeededCounter = 0;
    protected static int sBatchSize = 100;
    
    public DataExchangeHelper() {
    }
    
    public void setLog(PrintWriter out) {
        iTextLog = out;
    }
    public PrintWriter getLog() {
        return iTextLog;
    }
    public void log(String level, String message, Throwable t) {
        if (iTextLog==null) return;
        if (message!=null)
            iTextLog.println(level+": "+message);
        if (t!=null)
            t.printStackTrace(iTextLog);
    }

    public void debug(String msg) {
        log(sLogLevelDebug, msg, null);
        sLog.debug(msg);
    }
    public void info(String msg) {
        log(sLogLevelInfo, msg, null);
        sLog.info(msg);
    }
    public void warn(String msg) {
        log(sLogLevelWarn, msg, null);
        sLog.warn(msg);
    }
    public void error(String msg) {
        log(sLogLevelError, msg, null);
        sLog.error(msg);
    }
    public void fatal(String msg) {
        log(sLogLevelFatal, msg, null);
        sLog.fatal(msg);
    }
    public void debug(String msg, Throwable t) {
        log(sLogLevelDebug, msg, t);
        sLog.debug(msg, t);
    }
    public void info(String msg, Throwable t) {
        log(sLogLevelInfo, msg, t);
        sLog.info(msg, t);
    }
    public void warn(String msg, Throwable t) {
        log(sLogLevelWarn, msg, t);
        sLog.warn(msg, t);
    }
    public void error(String msg, Throwable t) {
        log(sLogLevelError, msg, t);
        sLog.error(msg, t);
    }
    public void fatal(String msg, Throwable t) {
        log(sLogLevelFatal, msg, t);
        sLog.fatal(msg, t);
    }
    
    public org.hibernate.Session getHibSession() {
        return iHibSession;
    }
    
    public boolean beginTransaction() {
        try {
            iHibSession = new _RootDAO().getSession();
            iTx = iHibSession.beginTransaction();
            debug("Transaction started.");
            return true;
        } catch (Exception e) {
            fatal("Unable to begin transaction, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean commitTransaction() {
        try {
            iTx.commit();
            debug("Transaction committed.");
            return true;
        } catch (Exception e) {
            fatal("Unable to commit transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
        }
    }

    public boolean rollbackTransaction() {
        try {
            iTx.rollback();
            info("Transaction rollbacked.");
            return true;
        } catch (Exception e) {
            fatal("Unable to rollback transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
        }
    }
    
    public boolean flush(boolean commit) {
        try {
            getHibSession().flush(); getHibSession().clear();
            if (commit && iTx!=null) {
                iTx.commit();
                iTx = getHibSession().beginTransaction();
            }
            return true;
        } catch (Exception e) {
            fatal("Unable to flush current session, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean flushIfNeeded(boolean commit) {
        iFlushIfNeededCounter++;
        if (iFlushIfNeededCounter>=sBatchSize) {
            iFlushIfNeededCounter = 0;
            return flush(commit);
        }
        return true;
    }

}
