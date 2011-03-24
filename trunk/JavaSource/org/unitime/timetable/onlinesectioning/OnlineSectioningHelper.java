/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningHelper {
    protected static Log sLog = LogFactory.getLog(OnlineSectioningHelper.class);
    public static enum LogLevel {
    	DEBUG,
    	INFO,
    	WARN,
    	ERROR,
    	FATAL
    };
    protected List<MessageHandler> iMessageHandlers = new ArrayList<MessageHandler>();
    protected org.hibernate.Session iHibSession = null;
    protected org.hibernate.Transaction iTx = null;
    protected int iFlushIfNeededCounter = 0;
    protected static int sBatchSize = 100;
    
    public OnlineSectioningHelper() {
    }
    
    public OnlineSectioningHelper(org.hibernate.Session hibSession) {
    	iHibSession = hibSession;
    }

    public void log(Message m) {
    	for (MessageHandler h: iMessageHandlers)
    		h.onMessage(m);
    }

    public void debug(String msg) {
        log(new Message(LogLevel.DEBUG, msg));
    }
    
    public void info(String msg) {
        log(new Message(LogLevel.INFO, msg));
    }
    
    public void warn(String msg) {
        log(new Message(LogLevel.WARN, msg));
    }
    
    public void error(String msg) {
        log(new Message(LogLevel.ERROR, msg));
    }
    
    public void fatal(String msg) {
        log(new Message(LogLevel.FATAL, msg));
    }
    
    public void debug(String msg, Throwable t) {
        log(new Message(LogLevel.DEBUG, msg, t));
    }
    
    public void info(String msg, Throwable t) {
        log(new Message(LogLevel.INFO, msg, t));
    }
    
    public void warn(String msg, Throwable t) {
        log(new Message(LogLevel.WARN, msg, t));
    }
    
    public void error(String msg, Throwable t) {
        log(new Message(LogLevel.ERROR, msg, t));
    }
    
    public void fatal(String msg, Throwable t) {
        log(new Message(LogLevel.FATAL, msg, t));
    }

    public org.hibernate.Session getHibSession() {
        return iHibSession;
    }
    
    public boolean beginTransaction() {
        try {
            iHibSession = new _RootDAO().createNewSession();
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
    
    public interface MessageHandler {
    	public void onMessage(Message message);
    }
    
    public void addMessageHandler(MessageHandler h) {
    	iMessageHandlers.add(h);
    }
    
    public static class Message {
    	private LogLevel iLevel;
    	private String iMessage;
    	private Throwable iThrowable;
    	
    	public Message(LogLevel level, String message) {
    		this(level, message, null);
    	}
    	
    	public Message(LogLevel level, String message, Throwable t) {
    		iLevel = level; iMessage = message; iThrowable = t;
    	}
    	
    	public String toString() {
    		return iLevel.name() + ": " + iMessage + (iThrowable == null ? "": " (" + iThrowable.getMessage() + ")");
    	}
    	
    	public LogLevel getLevel() { return iLevel; }
    	public String getMessage() { return iMessage; }
    	public Throwable getThrowable() { return iThrowable; }
    	
    	public String toHtml() {
    		switch (iLevel) {
			case DEBUG:
	        	return "<font color='gray'>&nbsp;&nbsp;--" + iMessage + "</font>";
			case INFO:
				return iMessage;
			case WARN:
				return "<font color='orange'>" + iMessage + "</font>";
			case ERROR:
				return "<font color='red'>" + iMessage + "</font>";
			case FATAL:
				return "<font color='red'><b>" + iMessage + "</b></font>";
			default:
				return iMessage;
    		}
    	}
    }
    
    public static class DefaultMessageLogger implements MessageHandler {
    	private Log iLog;
    	
    	public DefaultMessageLogger(Log log) {
    		iLog = log;
    	}
    	
		@Override
		public void onMessage(Message message) {
			switch (message.getLevel()) {
			case DEBUG:
				iLog.debug(message.getMessage(), message.getThrowable());
				break;
			case INFO:
				iLog.info(message.getMessage(), message.getThrowable());
				break;
			case WARN:
				iLog.warn(message.getMessage(), message.getThrowable());
				break;
			case ERROR:
				iLog.error(message.getMessage(), message.getThrowable());
				break;
			case FATAL:
				iLog.fatal(message.getMessage(), message.getThrowable());
				break;
			default:
				iLog.info(message.getMessage(), message.getThrowable());
			}
		}
    }
}
