/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.hibernate.HibernateException;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.base.BaseSolverInfo;
import org.unitime.timetable.solver.ui.FileInfo;
import org.unitime.timetable.solver.ui.TimetableInfo;
import org.unitime.timetable.solver.ui.TimetableInfoFileProxy;
import org.unitime.timetable.solver.ui.TimetableInfoUtil;


/**
 * @author Tomas Muller
 */
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

	public TimetableInfo getInfo() {
		return getInfo(TimetableInfoUtil.getInstance());
	}
	
	public TimetableInfo getInfo(TimetableInfoFileProxy proxy) {
		try {
			TimetableInfo info = getCached(getUniqueId());
			if (info!=null) return info;
			
			if (getData()==null) return null;
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
		} catch (Exception e) {
			Debug.warning("Failed to retrieve info: " + e.getMessage());
			return null;
		}
	}
	
	public String generateId() {
		throw new RuntimeException("This should never happen.");
	}
	
	public void setInfo(TimetableInfo info) {
		setInfo(info, TimetableInfoUtil.getInstance());
	}

	public void setInfo(TimetableInfo info, TimetableInfoFileProxy proxy) {
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
		if (getData()!=null) {
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
	
	public Document getValue() {
		try {
			SAXReader reader = new SAXReader();
			GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(getData()));
			Document document = reader.read(gzipInput);
			gzipInput.close();
			return document;
		} catch (IOException e) {
			throw new HibernateException(e.getMessage(),e);
		} catch (DocumentException e) {
			throw new HibernateException(e.getMessage(),e);
		}
	}
	
	public void setValue(Document document) {
		try {
			if (document == null) {
				setData(null);
			} else {
				 ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	             XMLWriter writer = new XMLWriter(new GZIPOutputStream(bytes),OutputFormat.createCompactFormat());
	             writer.write(document);
	             writer.flush(); writer.close();
	             setData(bytes.toByteArray());
			}
		} catch (IOException e) {
			throw new HibernateException(e.getMessage(),e);
		}
	}
	
}
