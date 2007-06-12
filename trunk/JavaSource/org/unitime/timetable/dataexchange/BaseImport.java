/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.dataexchange;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * 
 * @author Tomas Muller
 *
 */

public abstract class BaseImport {
    protected static Log sLog = LogFactory.getLog(BaseImport.class);
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
    
    public BaseImport() {}
    
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
    
    public void loadXml(String fileName) throws Exception {
        debug("Loading "+fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            loadXml(fis);
        } catch (IOException e) {
            fatal("Unable to read file "+fileName+", reason:"+e.getMessage(),e);
            throw e;
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (IOException e) {}
            }
        }
    }
    
    public void loadXml(InputStream inputStream) throws Exception {
        try {
            Document document = (new SAXReader()).read(inputStream);
            loadXml(document.getRootElement());
        } catch (DocumentException e) {
            fatal("Unable to parse given XML, reason:"+e.getMessage(), e);
        }
    }    
    
    public abstract void loadXml(Element rootElement) throws Exception;
    
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
