/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

package org.unitime.timetable.form;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Query;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;

/**
 * @author Zuzana Mullerova
 */
public class EventRoomAvailabilityForm extends ActionForm {

	// data loaded from Add Event
//	private String iLocationType; 
	private int iStartTime; // start time from Add Event
	private int iStopTime; // stop time from Add Event
	private TreeSet<Date> iMeetingDates = new TreeSet(); //meeting dates selected in Add Event
	private Long iBuildingId; // building selected in Add Event
	private String iRoomNumber; // room number entered in Add Event (can include wild cards such as "1*")
	private String iMinCapacity; // minimum required room capacity entered in Add Event
	private String iMaxCapacity; // maximum required room capacity entered in Add Event
	private String iOp; 
	private Long iSessionId;
	private boolean iLookAtNearLocations;
	private Long iEventId; //if adding meetings to an existing event
	private boolean iIsAddMeetings; //if adding meetings to an existing event
	private Long[] iRoomFeatures;
	
	//data calculated 
	private Hashtable<Long, Location> iLocations; 
	private Hashtable<Long, Hashtable<Date, TreeSet<Meeting>>> iOverlappingMeetings; // meetings that are in conflict with desired times/dates	
	private String iStartTimeString;
	private String iStopTimeString;
	private Set<Long> iStudentIds = null;
	
	//data collected in this screen (form)
	private TreeSet<DateLocation> iDateLocations = new TreeSet();	
	
	//other
	boolean iIsAdmin = false;
			
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iLocations = null;
		iOp = null;
		iStartTime = 90;
		iStopTime = 210;
//		iLocationType = null;
		iMeetingDates.clear();
		iMinCapacity = null;
		iMaxCapacity = null;
		iDateLocations.clear();
		iSessionId = null;
		User user = Web.getUser(request.getSession());
		iIsAdmin = user.isAdmin();
	}
	
	public void load (HttpSession session) throws Exception {
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
//		iLocationType = (String) session.getAttribute("Event.LocationType");
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iMinCapacity = (String) session.getAttribute("Event.MinCapacity");
		iMaxCapacity = (String) session.getAttribute("Event.MaxCapacity");
		iBuildingId = (Long) session.getAttribute("Event.BuildingId");
		iRoomNumber = (String) session.getAttribute("Event.RoomNumber");
		iLookAtNearLocations = ((Boolean) session.getAttribute("Event.LookAtNearLocations")).booleanValue();
		iRoomFeatures = (Long[]) session.getAttribute("Event.RoomFeatures");
		iLocations = getPossibleLocations();
		if (iLocations.isEmpty()) throw new Exception("No room is matching your criteria.");
		iOverlappingMeetings = getOverlappingMeetings(iLocations, iMeetingDates, iStartTime, iStopTime);
		iDateLocations = (TreeSet)session.getAttribute("Event.DateLocations");
		if (iDateLocations==null) iDateLocations = new TreeSet();
		iSessionId = (Long) session.getAttribute("Event.SessionId");
		iStudentIds = (Set<Long>)session.getAttribute("Event.StudentIds");
		iIsAddMeetings = (Boolean) (session.getAttribute("Event.IsAddMeetings"));
		iEventId = (Long) (session.getAttribute("Event.EventId"));
	}
	
	// collect date/location combinations selected by the user in this screen
	public void loadData (HttpServletRequest request) {
		iDateLocations.clear();
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		for (Date date : iMeetingDates) {
			for (Enumeration<Location> e=iLocations.elements();e.hasMoreElements();) {
				Location location = (Location)e.nextElement();
				if ("1".equals(request.getParameter("x"+df2.format(date)+"_"+location.getPermanentId())))
					iDateLocations.add(new DateLocation(date,location));
			}
		}
	}
	
	public void save (HttpSession session) {
		session.setAttribute("Event.DateLocations", iDateLocations);
	}
	
	// apply parameters from the Add Event screen to get possible locations for the event
	public Hashtable<Long, Location> getPossibleLocations() {
		Hashtable<Long, Location> locations = new Hashtable();
		String query;
		String a = "", b = "";
		if (iRoomFeatures!=null && iRoomFeatures.length>0) {
			for (int i=0;i<iRoomFeatures.length;i++) {
				a+= ", GlobalRoomFeature f"+i;
				b+= " and f"+i+".uniqueId="+iRoomFeatures[i]+" and f"+i+" in elements(r.features)";
			}
		}
	
		if (iLookAtNearLocations) {
			query = "select r from Room r, Building b"+a+" where b.uniqueId = :buildingId and " +
					"(r.building=b or ((((r.coordinateX - b.coordinateX)*(r.coordinateX - b.coordinateX)) +" +
					"((r.coordinateY - b.coordinateY)*(r.coordinateY - b.coordinateY)))" +
					"< 67*67))";
		} else {
			query = "select r from Room r"+a+" where r.building.uniqueId = :buildingId";
		}
			
		if (iMinCapacity!=null && iMinCapacity!="") { query+= " and r.capacity>= :minCapacity";	}
		if (iMaxCapacity!=null && iMaxCapacity!="") { query+= " and r.capacity<= :maxCapacity";	}
		if (iRoomNumber!=null && iRoomNumber.length()>0) { query+=" and r.roomNumber like (:roomNumber)"; }
		query += b;
		
		Query hibQuery = new LocationDAO().getSession().createQuery(query);

		hibQuery.setLong("buildingId", iBuildingId);
		if (iMinCapacity!=null && iMinCapacity!="") { hibQuery.setInteger("minCapacity", Integer.valueOf(iMinCapacity)); }
		if (iMaxCapacity!=null && iMaxCapacity!="") { hibQuery.setInteger("maxCapacity", Integer.valueOf(iMaxCapacity)); }
		if (iRoomNumber!=null && iRoomNumber.length()>0) { 
			hibQuery.setString("roomNumber", iRoomNumber.replaceAll("\\*", "%")); 
		}
		
		for (Iterator i=hibQuery.setCacheable(true).iterate();i.hasNext();) {
			Location location = (Location)i.next();
			if (location.getPermanentId()!=null)
				locations.put(location.getPermanentId(), location);
		}
		return locations;
	}

	// get events that have been already scheduled in the possible locations during requested times/dates
	public Hashtable<Long, Hashtable<Date, TreeSet<Meeting>>> getOverlappingMeetings (Hashtable<Long,Location> locations, TreeSet<Date> meetingDates, int startTime, int stopTime) {
		
		// get meetings 
		String locIds = "";
		for (Long permId : locations.keySet()) {
			if (locIds.length()>0) locIds += ",";
			locIds += permId;
		}
		String dates = "";
		for (int idx=0;idx<meetingDates.size();idx++) {
			if (dates.length()>0) dates += ",";
			dates += ":md"+idx;
		}
		String query = "Select m from Meeting m where " +
				"m.startPeriod<=:stopTime and 	" +
				"m.stopPeriod>=:startTime and " +
				"m.locationPermanentId in ("+locIds+") and "+
				"m.meetingDate in ("+dates+")";
		Query hibQuery = new MeetingDAO().getSession().createQuery(query);
		hibQuery.setInteger("startTime", iStartTime);
		hibQuery.setInteger("stopTime", iStopTime);
		int idx = 0;
		for (Date md : meetingDates) {
			hibQuery.setDate("md"+idx, md); idx++;
		}
		List<Meeting> meetings = (List<Meeting>) hibQuery.setCacheable(true).list();
		
		// sort meetings by location and date
		Hashtable<Long, Hashtable<Date, TreeSet<Meeting>>> locationDateMeetings = new Hashtable<Long, Hashtable<Date, TreeSet<Meeting>>>();
		for (Meeting meeting : meetings) {
			Hashtable<Date, TreeSet<Meeting>> dateMeetings = locationDateMeetings.get(meeting.getLocationPermanentId());
			if (dateMeetings == null) {
				dateMeetings = new Hashtable();
				locationDateMeetings.put(meeting.getLocationPermanentId(), dateMeetings);
			}
			TreeSet<Meeting> myMeetings = dateMeetings.get(new Date(meeting.getMeetingDate().getTime()));
			if (myMeetings == null) {
				myMeetings = new TreeSet();
				dateMeetings.put(new Date(meeting.getMeetingDate().getTime()), myMeetings);
			}
			myMeetings.add(meeting);
		}
		return locationDateMeetings;
	}
	
	// return overlapping meetings for a specific date and location (to be displayed in the table)
	public TreeSet<Meeting> getOverlappingMeetings(Location location, Date meetingDate) {
		Hashtable<Date, TreeSet<Meeting>> dateMeetings = iOverlappingMeetings.get(location.getPermanentId());
		return (dateMeetings==null?null:dateMeetings.get(meetingDate));
	}
	
	// draw the table with locations on top and dates on the left
	public String getAvailabilityTable() {
		String ret = "";
		ret +="<table border='1'>";

		TreeSet<Location> locations = new TreeSet<Location>(new Comparator<Location>() { 
			// sort locations first by number of available slots, then by name
			public int compare(Location l1, Location l2) {
				int availableL1=0;
				int availableL2=0;
				for (Date meetingDate : iMeetingDates) {
					if (getOverlappingMeetings(l1, meetingDate)==null) availableL1++;
					if (getOverlappingMeetings(l2, meetingDate)==null) availableL2++;
				}
				if (availableL1<availableL2) return 1;
				else if (availableL1>availableL2) return -1;
				else return l1.compareTo(l2);
			}
		});

		for (Enumeration<Location> e=iLocations.elements();e.hasMoreElements();) locations.add(e.nextElement());
		ret+="<tr align='middle'><td></td>";
		String jsLocations = "";
		for (Location location : locations) {
			ret += "<td onClick=\"tAll('"+location.getPermanentId()+"',false);\" " +
					"onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\">" +
					"<b>"+location.getLabel()+"</b><br>("+location.getCapacity().toString()+")</td>";
			jsLocations += (jsLocations.length()>0?",":"")+"'"+location.getPermanentId()+"'";
		}
		ret+="</tr>";
		String jsDates = "";
		SimpleDateFormat df1 = new SimpleDateFormat("EEE, MMM d");
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		for (Date date:iMeetingDates) {
			jsDates += (jsDates.length()>0?",":"")+"'"+df2.format(date)+"'";
            Hashtable<Event,Set<Long>> conflicts = Event.findStudentConflicts(date, iStartTime, iStopTime, iStudentIds);
            HashSet<Long> studentsInConflict = new HashSet();
            String conflictsTitle = "";
            TreeSet<Map.Entry<Event,Set<Long>>> conflictEntries = new TreeSet(new Comparator<Map.Entry<Event,Set<Long>>>() {
               public int compare(Map.Entry<Event,Set<Long>> e1, Map.Entry<Event,Set<Long>> e2) {
                   int cmp = Double.compare(e1.getValue().size(),e2.getValue().size());
                   if (cmp!=0) return -cmp;
                   cmp = e1.getKey().getEventName().compareTo(e2.getKey().getEventName());
                   if (cmp!=0) return cmp;
                   return e1.getKey().getUniqueId().compareTo(e2.getKey().getUniqueId());
               }
            });
            conflictEntries.addAll(conflicts.entrySet());
            int idx = 0;
            for (Map.Entry<Event,Set<Long>> entry : conflictEntries) {
                studentsInConflict.addAll(entry.getValue());
                if (idx<3) {
                    if (idx>0) conflictsTitle+="; ";
                    conflictsTitle+=entry.getValue().size()+" &times; "+entry.getKey().getEventName();
                } else if (idx==3)
                    conflictsTitle+=";...";
                idx++;
            }
			ret += "<tr><td align='center' title=\""+conflictsTitle+"\"><b>"+df1.format(date)+"</b>"+
                (!studentsInConflict.isEmpty()?"<br><i>("+studentsInConflict.size()+" conflicts)</i>":"")+
                "</td>";
			for (Location location : locations) {
				boolean selected = iDateLocations.contains(new DateLocation(date,location));
				ret += "<input type='hidden' name='x"+df2.format(date)+"_"+location.getPermanentId()+"' id='x"+df2.format(date)+"_"+location.getPermanentId()+"' value='"+(selected?"1":"0")+"'>";
				TreeSet<Meeting> meetings = getOverlappingMeetings(location, date);
				if (meetings==null || meetings.isEmpty()) {
					ret += "<td style='background-color:"+(selected?"yellow":"transparent")+";' align='center' valign='top' id='td"+df2.format(date)+"_"+location.getPermanentId()+"' "+
							"onClick=\"tClick('"+df2.format(date)+"','"+location.getPermanentId()+"');\" "+
							"onMouseOver=\"tOver(this,'"+df2.format(date)+"','"+location.getPermanentId()+"');\" "+
							"onMouseOut=\"tOut('"+df2.format(date)+"','"+location.getPermanentId()+"');\">&nbsp;</td>";
				} else {
					ret += "<td style='background-color:"+(selected?"yellow":"rgb(200,200,200)")+";' valign='top' align='center' id='td"+df2.format(date)+"_"+location.getPermanentId()+"' ";
					if (iIsAdmin) {ret+=
							"onClick=\"tClick('"+df2.format(date)+"','"+location.getPermanentId()+"');\" "+
							"onMouseOver=\"tOver(this,'"+df2.format(date)+"','"+location.getPermanentId()+"');\" "+
							"onMouseOut=\"tOut('"+df2.format(date)+"','"+location.getPermanentId()+"');\" ";
					}
					ret += ">";
					idx=0;
					for (Meeting meeting : meetings) {
						if (idx>2) { ret+="<br>..."; break; }
						ret += (idx>0?"<br>":"")+
							"<span title='"+meeting.getTimeLabel()+"'>"+
							meeting.getEvent().getEventName()+
							"</span>";
						idx++;
					}
					ret += "</td>";
				}
			}
			ret += "</tr>";
		}
		ret += "</table>";
		ret += "<script language='javascript'>";
		ret += "var tDates=["+jsDates+"];";
		ret += "var tLocations=["+jsLocations+"];";
		ret += "</script>";
		return ret;
	}
	
	public Hashtable<Long, Location> getLocations() {return iLocations;}
	public void setLocations(Hashtable<Long, Location> locations) {iLocations=locations;}
	
	public int getStartTime() {return iStartTime;}
	public void setStartTime(int startTime) {iStartTime = startTime;}
	
	public int getStopTime() {return iStopTime;}
	public void setStopTime(int stopTime) {iStopTime = stopTime;}
	
	public String getOp() {return iOp;}
	public void setOp(String op) {iOp = op;}
	
	public String getTimeString(int time) {
	    int hour = (time/12)%12;
    	int minute = time%12*5;
    	String ampm = (time/144==0?"am":"pm");
		return hour+":"+(minute<10?"0":"")+minute+" "+ampm;
	}
	
	public String getStartTimeString() {return getTimeString(iStartTime);}
	
	public String getStopTimeString() {	return getTimeString(iStopTime);}
	
	public boolean getIsAddMeetings() {return iIsAddMeetings;}

	// a class for storing selected date/location combinations
	public static class DateLocation implements Serializable, Comparable<DateLocation> {
		private Date iDate;
		private Long iLocation; //permanentId
		private String iLocationLabel;
		private SimpleDateFormat sdf = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US);
		
		public DateLocation(Date date, Location location) {
			iDate = date; 
			iLocation = location.getPermanentId(); 
			iLocationLabel = location.getLabel();
		}

		public Date getDate() {
			return iDate;
		}
		
		public String getDateLabel() {
			return sdf.format(iDate);
		}

		public Long getLocation() {
			return iLocation;
		}

		public String getLocationLabel() {
			return iLocationLabel;
		}
		
		public int hashCode() {
			return getDate().hashCode() ^ getLocation().hashCode();
		}

		public boolean equals(Object o) {
			if (o==null || !(o instanceof DateLocation)) return false;
			DateLocation dl = (DateLocation)o;
			return getDate().equals(dl.getDate()) && getLocation().equals(dl.getLocation());
		}
		
		public int compareTo(DateLocation dl) {
			int cmp = iDate.compareTo(dl.getDate());
			if (cmp!=0) return cmp;
			cmp = iLocationLabel.compareTo(dl.getLocationLabel());
			if (cmp!=0) return cmp;
			return iLocation.compareTo(dl.getLocation());
		}
	}
}
