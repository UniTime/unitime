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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Query;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Zuzana Mullerova
 */
public class EventRoomAvailabilityForm extends EventAddForm {
	
	//data calculated 
	private Hashtable<Long, Location> iLocations; 
	private Hashtable<Long, Hashtable<Date, TreeSet<Meeting>>> iOverlappingMeetings; // meetings that are in conflict with desired times/dates	
	private Set<Long> iStudentIds = null;
	
	//data collected in this screen (form)
	private TreeSet<DateLocation> iDateLocations = new TreeSet();	
	
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();

        if (iDateLocations.isEmpty()) {
            errors.add("dateLocations", new ActionMessage("errors.generic", "No meeting selected."));
        }
        
        return errors;
    }	
			
	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    super.reset(mapping, request);
		iLocations = null;
		iDateLocations.clear();
	}
	
	public void load (HttpSession session) throws Exception {
	    super.load(session);
		iLocations = getPossibleLocations();
		if (iLocations.isEmpty()) throw new Exception("No room is matching your criteria.");
		iOverlappingMeetings = getOverlappingMeetings(iLocations, getMeetingDates(), getStartTime(), getStopTime());
		iDateLocations = (TreeSet)session.getAttribute("Event.DateLocations");
		if (iDateLocations==null) iDateLocations = new TreeSet();
		iStudentIds = (Set<Long>)session.getAttribute("Event.StudentIds");
	}
	
	// collect date/location combinations selected by the user in this screen
	public void loadData (HttpServletRequest request) {
		iDateLocations.clear();
		SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		for (Date date : getMeetingDates()) {
			for (Enumeration<Location> e=iLocations.elements();e.hasMoreElements();) {
				Location location = (Location)e.nextElement();
				if ("1".equals(request.getParameter("x"+df2.format(date)+"_"+location.getPermanentId())))
					iDateLocations.add(new DateLocation(date,location));
			}
		}
	}
	
	public void save (HttpSession session) {
		session.setAttribute("Event.DateLocations", iDateLocations);
		session.setAttribute("Event.MaxRooms", getMaxRooms());
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
				"m.startPeriod<:stopTime and 	" +
				"m.stopPeriod>:startTime and " +
				"m.locationPermanentId in ("+locIds+") and "+
				"m.meetingDate in ("+dates+")";
		Query hibQuery = new MeetingDAO().getSession().createQuery(query);
		hibQuery.setInteger("startTime", getStartTime());
		hibQuery.setInteger("stopTime", getStopTime());
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
		
		int maxRooms = -1;
		try {
		    maxRooms = Integer.parseInt(getMaxRooms());
		} catch (Exception e) {}

		TreeSet<Location> locations = new TreeSet<Location>(new Comparator<Location>() { 
			// sort locations first by number of available slots, then by name
			public int compare(Location l1, Location l2) {
				int availableL1=0;
				int availableL2=0;
				for (Date meetingDate : getMeetingDates()) {
					if (getOverlappingMeetings(l1, meetingDate)==null) availableL1++;
					if (getOverlappingMeetings(l2, meetingDate)==null) availableL2++;
				}
				if (availableL1<availableL2) return 1;
				else if (availableL1>availableL2) return -1;
				else return l1.compareTo(l2);
			}
		});
		
		for (Enumeration<Location> e=iLocations.elements();e.hasMoreElements();) locations.add(e.nextElement());
		
		while (maxRooms>0 && locations.size()>maxRooms)
		    locations.remove(locations.last());

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
		for (Date date:getMeetingDates()) {
			jsDates += (jsDates.length()>0?",":"")+"'"+df2.format(date)+"'";
            Hashtable<Event,Set<Long>> conflicts = Event.findStudentConflicts(date, getStartTime(), getStopTime(), iStudentIds);
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
					if (isAdmin() || (getManagingDepartments()!=null && location.getControllingDepartment()!=null && getManagingDepartments().contains(location.getControllingDepartment()))) {
					    ret+=
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
	
	public String getTimeString() {
	    return (getStartTime()==0 && getStopTime()==Constants.SLOTS_PER_DAY?
	            "All Day":
	            Constants.toTime(Constants.SLOT_LENGTH_MIN*getStartTime()+Constants.FIRST_SLOT_TIME_MIN)+" - "+
	            Constants.toTime(Constants.SLOT_LENGTH_MIN*getStopTime()+Constants.FIRST_SLOT_TIME_MIN));
	}
	
	// a class for storing selected date/location combinations
	public static class DateLocation implements Serializable, Comparable<DateLocation> {
		private Date iDate;
		private Long iLocation; //permanentId
		private Long iLocUniqueId;
		private String iLocationLabel;
		private SimpleDateFormat sdf = new SimpleDateFormat("EEE MM/dd, yyyy", Locale.US);
		private int iStartTime = -1, iStopTime = -1;
		
		public DateLocation(Date date, Location location) {
		    this(date, location, -1, -1);
		}
		
		public DateLocation(Date date, Location location, int startTime, int stopTime) {
			iDate = date; 
			iLocation = location.getPermanentId();
			iLocUniqueId = location.getUniqueId();
			iLocationLabel = location.getLabel();
			iStartTime = startTime; iStopTime = stopTime;
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

		public Long getLocUniqueId() {
			return iLocUniqueId;
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
			return getDate().equals(dl.getDate()) && getLocation().equals(dl.getLocation()) && getStartTime()==dl.getStartTime() && getStopTime()==dl.getStopTime();
		}
		
		public int getStartTime() { return iStartTime; }
		public void setStartTime(int startTime) { iStartTime = startTime; }
        public int getStopTime() { return iStopTime; }
        public void setStopTime(int stopTime) { iStopTime = stopTime; }
		
		public int compareTo(DateLocation dl) {
			int cmp = iDate.compareTo(dl.getDate());
			if (cmp!=0) return cmp;
            cmp = Double.compare(iStartTime,dl.getStartTime());
            if (cmp!=0) return cmp;
			cmp = iLocationLabel.compareTo(dl.getLocationLabel());
			if (cmp!=0) return cmp;
			return iLocation.compareTo(dl.getLocation());
		}
	}
}
