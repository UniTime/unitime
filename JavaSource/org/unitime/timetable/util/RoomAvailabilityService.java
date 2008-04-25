package org.unitime.timetable.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
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

public class RoomAvailabilityService implements RoomAvailabilityInterface {
    private static Log sLog = LogFactory.getLog(RoomAvailabilityInterface.class);
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
    
    public Collection<TimeBlock> getRoomAvailability(String roomExternalId, String buildingAbbv, String roomNbr, Date startTime, Date endTime, String[] excludeTypes) {
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
                    cache.wait(iTimeout);
                } catch (InterruptedException e) {
                    sLog.warn("Wait for an update of "+cache+" got timed out.");
                    cache.deactivate();
                }
                if (!cache.isActive()) {
                    sLog.warn("Cache "+cache+" is not active.");
                    return null;
                }
                sLog.debug("Return: "+cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeTypes));
                return cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeTypes);
            } else {
                sLog.debug("Return: "+cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeTypes)+" from "+cache+".");
                return cache.get(new Room(roomExternalId, buildingAbbv, roomNbr), excludeTypes);
            }
        }
    }
    
    public CacheElement get(TimeFrame time) {
        synchronized (iCache) {
            for (CacheElement cache : iCache) if (cache.cover(time)) return cache;
        }
        return null;
    }
    
    public void activate(Date startTime, Date endTime) {
        TimeFrame time = new TimeFrame(startTime, endTime);
        sLog.debug("Activate: "+time);
        CacheElement cache = get(time);
        if (cache==null) {
            iCache.add(new CacheElement(time));
        } else {
            synchronized (cache) {
                cache.markDirty();
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
    
    protected Document createRequest(TimeFrame time) {
        Document request = DocumentHelper.createDocument();
        Element params = request.addElement("parameters");
        params.addElement("beginDate").addAttribute("value", new SimpleDateFormat("MM/dd/yyyy").format(time.getStartTime()));
        params.addElement("endDate").addAttribute("value", new SimpleDateFormat("MM/dd/yyyy").format(time.getEndTime()));
        params.addElement("startTime").addAttribute("value", new SimpleDateFormat("HH:mm").format(time.getStartTime()));
        params.addElement("endTime").addAttribute("value", new SimpleDateFormat("HH:mm").format(time.getEndTime()));
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
    
    protected Hashtable<Room, Vector<TimeBlock>> readResponse(Document response) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(response.getRootElement().attributeValue("dateFormat","MM/dd/yyyy"));
        SimpleDateFormat timeFormat = new SimpleDateFormat(response.getRootElement().attributeValue("timeFormat","h:mm a"));
        Hashtable<Room, Vector<TimeBlock>> availability = new Hashtable<Room, Vector<TimeBlock>>();
        for (Iterator i=response.getRootElement().elementIterator("room");i.hasNext();) {
            Element roomElement = (Element)i.next();
            Room room = new Room(roomElement);
            Vector<TimeBlock> roomAvailability = availability.get(room);
            if (roomAvailability==null) {
                roomAvailability = new Vector<TimeBlock>();
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
    
    protected Document recieveResponse() throws IOException, DocumentException {
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
        
        public synchronized void update(CacheElement cache) throws InterruptedException {
            try {
                sLog.debug("Updating "+cache);
                sendRequest(createRequest(cache.getTimeFrame()));
                sLog.debug("Request "+iRequestFile+" created.");
                Document response = null;
                long waited = 0;
                while ((response = recieveResponse())==null) {
                    sLog.debug("Waiting for response ("+(waited/1000)+"s waited so far)...");
                    sleep(500);
                    waited+=500;
                    if (waited>iTimeout) {
                        sLog.error("No response recieved after "+(iTimeout/1000)+"s.");
                        throw new Exception("Timeout");
                    }
                }
                sLog.debug("Reading response "+iResponseFile+"...");
                synchronized (cache) {
                    cache.update(readResponse(response));
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
                        sleep(iRefreshRate);
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
        private Hashtable<Room, Vector<TimeBlock>> iAvailability = new Hashtable();
        private long iLastAccess, iLastUpdate;
        public CacheElement(TimeFrame time) {
            iTime = time; iLastAccess = System.currentTimeMillis();
        };
        public void update(Hashtable<Room, Vector<TimeBlock>> availability) {
            iAvailability = availability; iLastUpdate = System.currentTimeMillis(); iDirty = false; iActive = true;
        }
        public Vector<TimeBlock> get(Room room, String[] excludeTypes) {
            iLastAccess = System.currentTimeMillis();
            Vector<TimeBlock> roomAvailability = iAvailability.get(room);
            if (roomAvailability==null || excludeTypes==null || excludeTypes.length==0) return roomAvailability;
            Vector<TimeBlock> ret = new Vector(roomAvailability.size());
            blocks: for (TimeBlock block : roomAvailability) {
                for (int i=0;i<excludeTypes.length;i++)
                    if (excludeTypes[i].equals(block.getEventType())) continue blocks;
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
            long startDay = iTime.getStartTime().getTime() / 86400000;
            long endDay = iTime.getEndTime().getTime() / 86400000;
            long startTime = iTime.getStartTime().getTime() % 86400000;
            long endTime = iTime.getEndTime().getTime() % 86400000;
            long givenStartDay = iTime.getStartTime().getTime() / 86400000;
            long givenEndDay = iTime.getEndTime().getTime() / 86400000;
            long givenStartTime = iTime.getStartTime().getTime() % 86400000;
            long givenEndTime = iTime.getEndTime().getTime() % 86400000;
            return (startDay<=givenStartDay && givenEndDay<=endDay && startTime<=givenStartTime && givenEndTime<=endTime);
        }
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
    
    public class EventTimeBlock implements TimeBlock{
        private String iEventName, iEventType;
        private Date iStartTime, iEndTime;
        
        public EventTimeBlock(Element eventElement, SimpleDateFormat dateFormat, SimpleDateFormat timeFormat) throws ParseException {
            iEventName = eventElement.attributeValue("name");
            iEventType = eventElement.attributeValue("type");
            Date date = dateFormat.parse(eventElement.attributeValue("date"));
            Date start = timeFormat.parse(eventElement.attributeValue("startTime"));
            Date end = timeFormat.parse(eventElement.attributeValue("endTime"));
            iStartTime = new Date(date.getTime() + start.getTime());
            iEndTime = new Date(date.getTime() + end.getTime());
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
    }
}
