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
package org.unitime.timetable.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class RoomAvailabilityService implements RoomAvailabilityInterface {
    private static Log sLog = LogFactory.getLog(RoomAvailabilityInterface.class);
    private static DecimalFormat sDf = new DecimalFormat("0.0");
    private boolean iStop = false;
    private RefreshThread iRefreshThread = null;
    
    private long iMaxAge = 1000*Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.maxage", "600"));
    private long iRefreshRate = 1000*Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.refresh", "60"));
    private long iTimeToLive = 1000*Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.timetolive", "3600"));
    private long iTimeout = 1000*Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability.timeout", "60"));
    
    private File iRequestFile = new File(ApplicationProperties.getProperty("tmtbl.room.availability.request",ApplicationProperties.getDataFolder()+File.separator+"request.xml"));
    private File iResponseFile = new File(ApplicationProperties.getProperty("tmtbl.room.availability.response",ApplicationProperties.getDataFolder()+File.separator+"response.xml"));
    private boolean iDelete = "true".equals(ApplicationProperties.getProperty("tmtbl.room.availability.delete","true")); 
    
    private Vector<CacheElement> iCache = new Vector<CacheElement>();
    
    public RoomAvailabilityService() {
    }
    
    public Collection<TimeBlock> getRoomAvailability(Location location, Date startTime, Date endTime, String excludeType) {
        if (location instanceof org.unitime.timetable.model.Room) {
            org.unitime.timetable.model.Room room = (org.unitime.timetable.model.Room)location;
            return getRoomAvailability(
                    room.getExternalUniqueId(),
                    room.getBuildingAbbv(),
                    room.getRoomNumber(),
                    startTime, endTime, excludeType);
        }
        return null;
        
    }
    
    public Collection<TimeBlock> getRoomAvailability(String roomExternalId, String buildingAbbv, String roomNbr, Date startTime, Date endTime, String excludeType) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        sLog.debug("Get: "+time+" ("+buildingAbbv+" "+roomNbr+")");
        CacheElement cache = get(time);
        if (cache==null) {
            sLog.error("Cache covering "+time+" not found.");
            return null;
        }
        synchronized (cache) {
            if (!cache.isActive() && !cache.isDirty()) {
                sLog.warn("Cache "+cache+" not active.");
                return null;
            } else if (iMaxAge>0 && cache.getAge()>iMaxAge) {
                sLog.info("Cache "+cache+" too old, waiting for an update...");
                cache.markDirty();
                try {
                    iRefreshThread.notify();
                } catch (IllegalMonitorStateException e) {}
                try {
                    cache.wait(iTimeout);
                } catch (InterruptedException e) {
                    sLog.warn("Wait for an update of "+cache+" got timed out.");
                    cache.deactivate();
                }
                if (!cache.isActive()) {
                    sLog.warn("Cache "+cache+" is not active.");
                    return null;
                }
                sLog.debug("Return: "+cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeType));
                return cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeType);
            } else {
                sLog.debug("Return: "+cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeType)+" from "+cache+".");
                return cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeType);
            }
        }
    }
    
    public String getTimeStamp(Date startTime, Date endTime, String excludeType) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        CacheElement cache = get(time);
        return (cache==null?null:cache.getTimestamp());
    }
    
    public CacheElement get(TimeFrame time) {
        synchronized (iCache) {
            for (CacheElement cache : iCache) if (cache.cover(time)) return cache;
        }
        return null;
    }
    
    public void activate(Session session, Date startTime, Date endTime, String excludeType, boolean waitForSync) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        sLog.debug("Activate: "+time);
        CacheElement cache = get(time);
        if (cache==null) {
            cache = new CacheElement(session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(), time);
            iCache.add(cache);
        } else {
            synchronized (cache) {
                cache.markDirty();
            }
        }
        synchronized (iRefreshThread) {
            iRefreshThread.notify();
        }
        if (waitForSync) {
            synchronized (cache) {
                sLog.warn("Activate: waiting for update of "+time);
                try {
                    cache.wait(iTimeout);
                } catch (InterruptedException e) {
                    sLog.warn("Wait for an update of "+cache+" got timed out.");
                    cache.deactivate();
                }
                if (!cache.isActive()) {
                    sLog.warn("Cache "+cache+" is not active.");
                }
            }
        }
    }
    
    public void startService() {
        sLog.info("Starting room availability service");
        iRefreshThread = new RefreshThread();
        iRefreshThread.start();
    }
    
    public void stopService() {
        sLog.info("Stopping room availability service");
        iStop = true;
        iRefreshThread.interrupt();
    }
    
    protected Document createRequest(CacheElement cache) {
        Document request = DocumentHelper.createDocument();
        Element params = request.addElement("parameters");
        params.addAttribute("created", new Date().toString());
        params.addElement("year").addAttribute("value", cache.getYear());
        params.addElement("term").addAttribute("value", cache.getTerm());
        params.addElement("campus").addAttribute("value", cache.getCampus());
        params.addElement("beginDate").addAttribute("value", new SimpleDateFormat("MM/dd/yyyy").format(cache.getTimeFrame().getStartTime()));
        params.addElement("endDate").addAttribute("value", new SimpleDateFormat("MM/dd/yyyy").format(cache.getTimeFrame().getEndTime()));
        params.addElement("startTime").addAttribute("value", new SimpleDateFormat("HH:mm").format(cache.getTimeFrame().getStartTime()));
        params.addElement("endTime").addAttribute("value", new SimpleDateFormat("HH:mm").format(cache.getTimeFrame().getEndTime()));
        return request;
    }
    
    protected void sendRequest(Document request) throws IOException {
        FileOutputStream fos = null;
        try {
            if (iDelete && iResponseFile.exists()) iResponseFile.delete();
            fos = new FileOutputStream(iRequestFile);
            (new XMLWriter(fos,OutputFormat.createPrettyPrint())).write(request);
            fos.flush();fos.close();fos=null;
        } finally {
            if (fos!=null) fos.close();
        }
    }
    
    protected Hashtable<Room, HashSet<TimeBlock>> readResponse(Document response) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(response.getRootElement().attributeValue("dateFormat","MM/dd/yyyy"), Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat(response.getRootElement().attributeValue("timeFormat","h:mm a"), Locale.US);
        Hashtable<Room, HashSet<TimeBlock>> availability = new Hashtable<Room, HashSet<TimeBlock>>();
        for (Iterator i=response.getRootElement().elementIterator("room");i.hasNext();) {
            Element roomElement = (Element)i.next();
            Room room = new Room(roomElement);
            HashSet<TimeBlock> roomAvailability = availability.get(room);
            if (roomAvailability==null) {
                roomAvailability = new HashSet<TimeBlock>();
                availability.put(room, roomAvailability);
            }
            for (Iterator j=roomElement.elementIterator("event");j.hasNext();) {
                Element eventElement = (Element)j.next();
                EventTimeBlock event = new EventTimeBlock(eventElement, dateFormat, timeFormat);
                roomAvailability.add(event);
            }
        }
        return availability;
    }
    
    protected Document receiveResponse() throws IOException, DocumentException {
        FileInputStream fis = null;
        try {
            if (!iResponseFile.exists() || !iResponseFile.canRead()) return null;
            fis = new FileInputStream(iResponseFile);
            Document document = (new SAXReader()).read(fis);
            fis.close(); fis=null; if (iDelete) iResponseFile.delete();
            return document;
        } finally {
            if (fis!=null) fis.close();
        }
    }
    
    public class RefreshThread extends Thread {
        public RefreshThread() {
            setName("Room Refresh");
            setDaemon(true);
        }
        
        public void update(CacheElement cache) throws InterruptedException {
            try {
                sLog.debug("Updating "+cache);
                long t0 = System.currentTimeMillis();
                sendRequest(createRequest(cache));
                sLog.debug("Request "+iRequestFile+" created.");
                Document response = null;
                long waited = 0;
                while ((response = receiveResponse())==null) {
                    sLog.debug("Waiting for response ("+(waited/1000)+"s waited so far)...");
                    sleep(5000);
                    waited+=5000;
                    if (waited>iTimeout) {
                        sLog.error("No response received after "+(iTimeout/1000)+"s.");
                        throw new Exception("Timeout");
                    }
                }
                sLog.debug("Reading response...");
                synchronized (cache) {
                    Hashtable<Room, HashSet<TimeBlock>> availability = readResponse(response);
                    long dt = System.currentTimeMillis() - t0;
                    String ts = response.getRootElement().attributeValue("created");
                    if (ts==null) ts = new Date().toString();
                    if (dt>100 && dt<60000)
                        ts += " (retrieved in "+sDf.format(dt/1000.0)+" sec)";
                    else if (dt>=60000) {
                        ts += " (retrieved in "+sDf.format(dt/60000.0)+" min)";
                    }
                    cache.update(availability, ts);
                }
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                sLog.error("Unable to query room availability, reason:"+e.getMessage(),e);
                cache.deactivate();
            }
            synchronized (cache) {
                cache.notifyAll();
            }
        }
        
        public void run() {
            while (true) {
                try {
                    if (iStop) break;
                    try {
                        synchronized (this) {
                            this.wait(iRefreshRate);
                        }
                        Vector<CacheElement> dirty = new Vector();
                        for (CacheElement cache : iCache) {
                            if (cache.isDirty()) { dirty.add(cache); continue; }
                            if (!cache.isActive()) continue;
                            if (iTimeToLive>=0 && cache.getUse()>iTimeToLive) {
                                cache.deactivate(); continue;
                            }
                            dirty.add(cache);
                        }
                        for (CacheElement cache : dirty) update(cache);
                    } catch (InterruptedException e) {
                        if (iStop) break;
                    }
                } catch (Exception e) {
                    sLog.error("Room availability refresh is failing: "+e.getMessage());
                }
            }
            sLog.info("Room availability refresh thread stopped.");
        }
    }
    
    public static class TimeFrame {
        private Date iStart, iEnd;
        public TimeFrame(Date start, Date end) {
            iStart = start; iEnd = end; 
        }
        public Date getStartTime() { return iStart; }
        public Date getEndTime() { return iEnd; }
        public int hashCode() {
            return iStart.hashCode() ^ iEnd.hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof TimeFrame)) return false;
            TimeFrame t = (TimeFrame)o;
            return getStartTime().equals(t.getStartTime()) && getEndTime().equals(t.getEndTime());
        }
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
            return df.format(getStartTime())+" - "+df.format(getEndTime());
        }
    }
    
    public static class CacheElement{
        private boolean iDirty = true, iActive = false;
        private TimeFrame iTime;
        private Hashtable<Room, HashSet<TimeBlock>> iAvailability = new Hashtable();
        private long iLastAccess, iLastUpdate;
        private String iTerm, iYear, iCampus;
        private String iTimestamp = null;
        public CacheElement(String year, String term, String campus, TimeFrame time) {
            iYear = year; iTerm = term; iCampus = campus;
            iTime = time; iLastAccess = System.currentTimeMillis();
        };
        public void update(Hashtable<Room, HashSet<TimeBlock>> availability, String timestamp) {
            iAvailability = availability; iLastUpdate = System.currentTimeMillis(); iDirty = false; iActive = true; iTimestamp = timestamp;
        }
        public HashSet<TimeBlock> get(Room room, String excludeType) {
            iLastAccess = System.currentTimeMillis();
            HashSet<TimeBlock> roomAvailability = iAvailability.get(room);
            if (roomAvailability==null || excludeType==null) return roomAvailability;
            HashSet<TimeBlock> ret = new HashSet(roomAvailability.size());
            for (TimeBlock block : roomAvailability) {
            	if (excludeType.equals(block.getEventType())) continue;
            	ret.add(block);
            }
            return ret;
        }
        public boolean isDirty() { return iDirty; }
        public boolean isActive() { return iActive; }
        public void markDirty() { iDirty = true; iLastAccess = System.currentTimeMillis(); }
        public void markActive() {
            if (!iActive) { iActive = true; iDirty = true; } 
        }
        public void deactivate() {
            iActive = false; iDirty = false; iAvailability.clear();
        }
        public long getAge() { return System.currentTimeMillis() - iLastUpdate; }
        public long getUse() { return System.currentTimeMillis() - iLastAccess; }
        public TimeFrame getTimeFrame() { return iTime; }
        public boolean cover(TimeFrame time) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
    		SimpleDateFormat sdfTime = new SimpleDateFormat("HHmm");
    		String startDay = sdfDate.format(iTime.getStartTime());
    		String endDay = sdfDate.format(iTime.getEndTime());
    		String startTime = sdfTime.format(iTime.getStartTime());
   		    String endTime = sdfTime.format(iTime.getEndTime());
    		String givenStartDay = sdfDate.format(time.getStartTime());
    		String givenEndDay = sdfDate.format(time.getEndTime());
    		String givenStartTime = sdfTime.format(time.getStartTime());
   		    String givenEndTime = sdfTime.format(time.getEndTime());
   		    return(startDay.compareTo(givenStartDay) <=0 && givenEndDay.compareTo(endDay) <= 0 && startTime.compareTo(givenStartTime) <= 0 && givenEndTime.compareTo(endTime) <= 0);
       }
        public String getYear() { return iYear; }
        public String getTerm() { return iTerm; }
        public String getCampus() { return iCampus; }
        public String getTimestamp() { return iTimestamp; }
        public String toString() {
            return iTime+" (updated "+(getAge()/1000)+"s ago, used "+(getUse()/1000)+"s ago"+(iActive?", active":"")+(iDirty?", dirty":"")+")";
        }
    }
    
    public static class Room {
        private String iExternalId, iBuildingAbbv, iRoomNbr;
        public Room(String externalId, String buildingAbbv, String roomNbr) {
            iExternalId = externalId; iBuildingAbbv = buildingAbbv; iRoomNbr = roomNbr; 
        }
        public Room(Element roomElement) {
            iExternalId = roomElement.attributeValue("externalId");
            iBuildingAbbv = roomElement.attributeValue("building");
            iRoomNbr = roomElement.attributeValue("roomNbr");
        }
        public String getExternalId() { return iExternalId; }
        public boolean hasExternalId() { return iExternalId!=null; }
        public boolean hasName() { return iBuildingAbbv!=null && iRoomNbr!=null; }
        public String getBuildingAbbv() { return iBuildingAbbv; }
        public String getRoomNbr() { return iRoomNbr; }
        public int hashCode() {
            if (hasName()) return getBuildingAbbv().hashCode() ^ getRoomNbr().hashCode();
            return getExternalId().hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof Room)) return false;
            Room r = (Room)o;
            if (hasExternalId() && r.hasExternalId()) return getExternalId().equals(r.getExternalId());
            return getBuildingAbbv().equals(r.getBuildingAbbv()) && getRoomNbr().equals(r.getRoomNbr());
        }
        public String toString() {
            return getBuildingAbbv()+" "+getRoomNbr();
        }
    }
    
    public static class EventTimeBlock implements TimeBlock {
		private static final long serialVersionUID = -2466335111767360325L;
		private String iEventName, iEventType;
        private Date iStartTime, iEndTime;
        
        public EventTimeBlock(Element eventElement, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat) throws ParseException {
            iEventName = eventElement.attributeValue("name");
            iEventType = eventElement.attributeValue("type");
            Calendar c = Calendar.getInstance(Locale.US);
            c.setTime(dateFormat.parse(eventElement.attributeValue("date")));
            int start = Integer.parseInt(new SimpleDateFormat("HHmm").format(timeFormat.parse(eventElement.attributeValue("startTime"))));
            int end = Integer.parseInt(new SimpleDateFormat("HHmm").format(timeFormat.parse(eventElement.attributeValue("endTime"))));
            c.set(Calendar.HOUR, start/100);
            c.set(Calendar.MINUTE, start%100);
            iStartTime = c.getTime();
            c.setTime(dateFormat.parse(eventElement.attributeValue("date")));
            c.set(Calendar.HOUR, end/100);
            c.set(Calendar.MINUTE, end%100);
            iEndTime = c.getTime();
            if (iEndTime.compareTo(iStartTime)<0) {
                sLog.info("Event "+iEventName+" ("+iEventType+") goes over midnight ("+eventElement.attributeValue("date")+" "+eventElement.attributeValue("startTime")+" - "+eventElement.attributeValue("endTime")+").");
                c.add(Calendar.DAY_OF_YEAR, 1);
                iEndTime = c.getTime();
            }
        }
        
        public String getEventName() { return iEventName; }
        public String getEventType() { return iEventType; }
        public Date getStartTime() { return iStartTime; }
        public Date getEndTime() { return iEndTime; }
        public String toString() {
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm");
            SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
            return getEventName()+" ("+getEventType()+") "+df.format(getStartTime())+" - "+df2.format(getEndTime());
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof TimeBlock)) return false;
            TimeBlock t = (TimeBlock)o;
            return getEventName().equals(t.getEventName()) &&
                   getEventType().equals(t.getEventType()) &&
                   getStartTime().equals(t.getStartTime()) &&
                   getEndTime().equals(t.getEndTime());
        }
        public int hashCode() {
            return getEventName().hashCode() ^ getEventType().hashCode() ^ getStartTime().hashCode();
        }
    }

	@Override
	public Collection<TimeBlock> getInstructorAvailability(DepartmentalInstructor instructor, Date startTime, Date endTime, String excludeType) {
		return null;
	}
}
