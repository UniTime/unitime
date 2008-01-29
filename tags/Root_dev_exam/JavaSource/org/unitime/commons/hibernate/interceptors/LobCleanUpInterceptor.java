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
package org.unitime.commons.hibernate.interceptors;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.type.Type;
import org.unitime.commons.Debug;


/**
 * @author Tomas Muller
 */
public class LobCleanUpInterceptor extends EmptyInterceptor {
	protected static Log sLog = LogFactory.getLog(LobCleanUpInterceptor.class);
    private boolean iDoAudit = false;
    
    public LobCleanUpInterceptor(Configuration configuration) {
        iDoAudit = "true".equals(configuration.getProperty("tmtbl.hibernate.audit"));
    }

	// a thread local set to store temperary LOBs
    private static final ThreadLocal sThreadTempLobs = new ThreadLocal() {
        protected Object initialValue() {
            return new HashSet();
        }
    };

    public void cleanup() {
        if (doAudit()) Debug.info("!!! Audit: Cleaning LOBs ");
        Set tempLobs = (Set)sThreadTempLobs.get();

        try {
            for (Iterator iter = tempLobs.iterator(); iter.hasNext();) {
                Object lob = iter.next();
                Method freeTemporary = lob.getClass().getMethod("freeTemporary", new Class[0]);
                freeTemporary.invoke(lob, new Object[0]);
            }
        } catch (SecurityException e) {
        	sLog.error("clean LOB failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
        	sLog.error("clean LOB failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IllegalArgumentException e) {
        	sLog.error("clean LOB failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
        	sLog.error("clean LOB failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
        	sLog.error("clean LOB failed: " + e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            tempLobs.clear();
        }
    }
    
    // register oracle temperary BLOB/CLOB into 
    // a thread-local set, this should be called at
    // the end of nullSafeSet(...) in BinaryBlobType
    // or StringClobType
    public static void registerTempLobs(Object lob) {
        ((Set)sThreadTempLobs.get()).add(lob);
    }
    
    /*
     * Audit Events
     */
    public void postFlush(Iterator arg0) {
        if (doAudit()) Debug.info("!!! Audit: Post Flush ");
        cleanup();
        super.postFlush(arg0);
    }
    public void afterTransactionBegin(Transaction arg0) {
        if (doAudit()) Debug.info("!!! Audit: Transaction Begun");
        super.afterTransactionBegin(arg0);
    }
    public void afterTransactionCompletion(Transaction arg0) {         
        if (doAudit()) Debug.info("!!! Audit: Transaction Complete");        
        super.afterTransactionCompletion(arg0);
    }
    public void onCollectionUpdate(Object arg0, Serializable arg1)
            throws CallbackException {
        if (doAudit()) Debug.info("!!! Audit: Collection Update ... " + arg0.getClass().getName());
        super.onCollectionUpdate(arg0, arg1);
    }
    public boolean onFlushDirty(Object arg0, Serializable arg1, Object[] arg2,
            Object[] arg3, String[] arg4, Type[] arg5) {
        if (doAudit()) Debug.info("!!! Audit: Flush Dirty ... " + arg0.getClass().getName());
        return super.onFlushDirty(arg0, arg1, arg2, arg3, arg4, arg5);
    }
    public boolean onLoad(Object arg0, Serializable arg1, Object[] arg2,
            String[] arg3, Type[] arg4) {
        if (doAudit()) Debug.info("!!! Audit: Loading ... " + arg0.getClass().getName());
        return super.onLoad(arg0, arg1, arg2, arg3, arg4);
    }
    public boolean onSave(Object arg0, Serializable arg1, Object[] arg2,
            String[] arg3, Type[] arg4) {
        if (doAudit()) Debug.info("!!! Audit: Saving ... " + arg0.getClass().getName());
        return super.onSave(arg0, arg1, arg2, arg3, arg4);
    }
    
    private boolean doAudit() {
        return iDoAudit;
    }
}
