/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseSolverInfo;
import org.unitime.timetable.solver.ui.FileInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.solver.ui.TimetableInfoUtil;


public class SolverInfo extends BaseSolverInfo {
	private static final long serialVersionUID = 1L;
	
/*[CONSTRUCTOR MARKER BEGIN]*/
	public SolverInfo () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SolverInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}


/*[CONSTRUCTOR MARKER END]*/
	
	public SolverInfo (Long uniqueId, Document value) {
		super(uniqueId);
		setValue(value);
	}

	public TimetableInfo getInfo() throws Exception {
		return getInfo(TimetableInfoUtil.getInstance());
	}
	
	public TimetableInfo getInfo(TimetableInfoFileProxy proxy) throws Exception {
		TimetableInfo info = getCached(getUniqueId());
		if (info!=null) return info;
		
		if (getValue()==null) return null;
		Element root = getValue().getRootElement();
        Class infoClass = null;
        try {
            infoClass = Class.forName(root.getName());
        } catch (ClassNotFoundException ex) {
            infoClass = Class.forName(getDefinition().getImplementation());
        }
		info = (TimetableInfo)infoClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
		info.load(root);
		if (info instanceof FileInfo) {
			info = ((FileInfo)info).loadInfo(proxy);
		}
		
		if (info!=null) setCached(getUniqueId(),info);
		return info;
	}
	
	public String generateId() {
		throw new RuntimeException("This should never happen.");
	}
	
	public void setInfo(TimetableInfo info) throws Exception {
		setInfo(info, TimetableInfoUtil.getInstance());
	}

	public void setInfo(TimetableInfo info, TimetableInfoFileProxy proxy) throws Exception {
		if (info.saveToFile()) {
			FileInfo fInfo = new FileInfo();
			String defName = null;
			if (getDefinition()!=null) {
				defName = getDefinition().getName();
			} else {
				defName = info.getClass().getName();
				if (defName.indexOf('.')>=0)
					defName = defName.substring(defName.lastIndexOf('.')+1);
			}
			fInfo.setName(defName+"_"+generateId()+".zxml");
			fInfo.saveInfo(info, proxy);
			info = fInfo;
		}
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(info.getClass().getName());
		info.save(root);
		setValue(document);
		setCached(getUniqueId(), info);
	}
	
    public void delete(org.hibernate.Session hibSession) throws Exception {
        delete(hibSession, TimetableInfoUtil.getInstance());
    }
	
	public void delete(org.hibernate.Session hibSession, TimetableInfoFileProxy proxy) throws Exception {
		if (getValue()!=null) {
			Element root = getValue().getRootElement();
            Class infoClass = null;
            try {
                infoClass = Class.forName(root.getName());
            } catch (ClassNotFoundException ex) {
                infoClass = Class.forName(getDefinition().getImplementation());
            }
			TimetableInfo info = (TimetableInfo)infoClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
			if (info instanceof FileInfo) {
				info.load(root);
				((FileInfo)info).deleteFile(proxy);
			}
		}
		removeCached(getUniqueId());
		hibSession.delete(this);
	}
	
	public static TimetableInfo getCached(Long uniqueId) {
		if (uniqueId==null) return null;
		synchronized (sInfoCache) {
			CachedTimetableInfo cInfo = (CachedTimetableInfo)sInfoCache.get(uniqueId);
			if (cInfo==null) return null;
			cInfo.mark();
			return cInfo.getInfo();
		}
	}
	
	public static void setCached(Long uniqueId, TimetableInfo info) {
		if (uniqueId==null) return;
		synchronized (sInfoCache) {
			sInfoCache.put(uniqueId, new CachedTimetableInfo(info));
		}		
	}
	
	public static void removeCached(Long uniqueId) {
		if (uniqueId==null) return;
		synchronized (sInfoCache) {
			sInfoCache.remove(uniqueId);
		}		
	}

	protected static Hashtable sInfoCache = null;
	protected static InfoCacheCleanup sCleanupThread = null;   
	protected static long sInfoCacheTimeToLive = 600000; //10 minutes
	protected static long sInfoCacheCleanupInterval = 30000; //30 secs
	
	static {
		sInfoCache = new Hashtable();
		sCleanupThread = new InfoCacheCleanup();
		sCleanupThread.start();
	}
	
	public static void stopInfoCacheCleanup() {
		sInfoCache = null;
		if (sCleanupThread!=null && sCleanupThread.isAlive())
			sCleanupThread.interrupt();
	}

	protected static class InfoCacheCleanup extends Thread {
		public InfoCacheCleanup() {
			setDaemon(true);
			setName("InfoCacheCleanup");
		}
		
		public void run() {
			Debug.info("InfoCache cleanup thread started.");
			try {
				while (true) {
					sleep(sInfoCacheCleanupInterval);
					if (sInfoCache==null) return;
					synchronized (sInfoCache) {
						if (sInfoCache.isEmpty()) continue;
						for (Iterator i=sInfoCache.entrySet().iterator();i.hasNext();) {
							Map.Entry entry = (Map.Entry)i.next();
							CachedTimetableInfo cInfo = (CachedTimetableInfo)entry.getValue();
							if (cInfo.getAge()>sInfoCacheTimeToLive) {
								i.remove();
							}
						}
					}
				}
			} catch (InterruptedException ex) {
				Debug.info("InfoCache cleanup thread interrupted.");
			}
			Debug.info("InfoCache cleanup thread finished.");
		}
	}

	protected static class CachedTimetableInfo {
    	private TimetableInfo iInfo = null;
    	private long iTimeStamp = System.currentTimeMillis();
    	public CachedTimetableInfo(TimetableInfo info) {
    		iInfo = info;
    	}
    	public TimetableInfo getInfo() {
    		return iInfo;
    	}
    	public long getAge() {
    		return System.currentTimeMillis()-iTimeStamp;
    	}
    	public void mark() {
    		iTimeStamp = System.currentTimeMillis();
    	}
    }
	
}
