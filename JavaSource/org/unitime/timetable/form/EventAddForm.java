/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Zuzana Mullerova
 */

public class EventAddForm extends ActionForm {

//	private EventModel iModel;
	private String iOp;
	private String iEventType;
	private Long iSessionId;
	private int iStartTime;
	private int iStopTime;
//	private String iLocationType;
//	private Vector<String> iLocationTypes = new Vector<String>();
	private Long iBuildingId;
	private String iRoomNumber;
	private TreeSet<Date> iMeetingDates = new TreeSet();
	private String iMinCapacity;
	private String iMaxCapacity;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		if (iStartTime>iStopTime)
			errors.add("stopDate", new ActionMessage("errors.generic", "Start Time must be earlier than Stop Time. It is not possible to enter overnight events."));
		
		if (iSessionId==null) {
			errors.add("session", new ActionMessage("errors.generic", "No academic session is selected."));
		}
		
		if (iMeetingDates.isEmpty()) {
			errors.add("dates", new ActionMessage("errors.generic", "No event dates are selected."));
		}

		if (iBuildingId == -1) {
			errors.add("building", new ActionMessage("errors.generic", "No building has been selected."));
		}

		int min = 0;
		if (iMinCapacity!=null && iMinCapacity.length()>0) {
			try {
				min = Integer.parseInt(iMinCapacity);
			} catch (NumberFormatException nfe) {
				errors.add("minCapacity", new ActionMessage("errors.generic", "Minimum room capacity should be a number or blank (no lower limit)."));
			}
		}
		
		int max = Integer.MAX_VALUE;
		if (iMaxCapacity!=null && iMaxCapacity.length()>0) {
			try {
				min = Integer.parseInt(iMaxCapacity);
			} catch (NumberFormatException nfe) {
				errors.add("maxCapacity", new ActionMessage("errors.generic", "Maximum room capacity should be a number or blank (no upper limit)."));
			}
		}
		
		if (min>max) {
			errors.add("minMaxCapacity", new ActionMessage("errors.generic", "Maximum room capacity should not be smaller than minimum room capacity."));
		}
		
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
//		iModel = null;
		iOp = null;
		iEventType = Event.sEventTypes[Event.sEventTypeSpecial];
		iSessionId = null;
		try {
			iSessionId = Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId();
		} catch (Exception e) {}
		iStartTime = 90;
		iStopTime = 210;
//		iLocationType = null;
		iMeetingDates.clear();
		iMinCapacity = null;
		iMaxCapacity = null;
	}
	
	// load event info from session attribute Event
	public void load (HttpSession session) {
		iEventType = (String) session.getAttribute("Event.EventType");
		iSessionId = (Long) session.getAttribute("Event.SessionId");		
		iStartTime = (Integer) session.getAttribute("Event.StartTime");
		iStopTime = (Integer) session.getAttribute("Event.StopTime");
//		iLocationType = (String) session.getAttribute("Event.LocationType");
//		iLocationTypes = (Vector<String>) session.getAttribute("Event.LocationTypes");
		iMeetingDates = (TreeSet<Date>) session.getAttribute("Event.MeetingDates");
		iMinCapacity = (String) session.getAttribute("Event.MinCapacity");
		iMaxCapacity = (String) session.getAttribute("Event.MaxCapacity");
		iBuildingId = (Long) session.getAttribute("Event.BuildingId");
		iRoomNumber = (String) session.getAttribute("Event.RoomNumber");

	}
	
	// save event parameters to session attribute Event
	public void save (HttpSession session) {
		session.setAttribute("Event.EventType", iEventType);
		session.setAttribute("Event.SessionId", iSessionId);		
		session.setAttribute("Event.StartTime", iStartTime);
		session.setAttribute("Event.StopTime", iStopTime);
//		session.setAttribute("Event.LocationType", iLocationType);
//		session.setAttribute("Event.LocationTypes", iLocationTypes);
		session.setAttribute("Event.MeetingDates", iMeetingDates);
		session.setAttribute("Event.MinCapacity", iMinCapacity);
		session.setAttribute("Event.MaxCapacity", iMaxCapacity);
		session.setAttribute("Event.BuildingId", iBuildingId);
		session.setAttribute("Event.RoomNumber", iRoomNumber);
//		session.setAttribute("Event.");
//		session.setAttribute("Event.");
	}
	
	
	// load event dates selected by user
	public void loadDates(HttpServletRequest request) {
        iMeetingDates.clear();
        if (iSessionId==null) return;
		Session s = Session.getSessionById(iSessionId);
        int startMonth = s.getStartMonth(); 
        int endMonth = s.getEndMonth();
        int year = s.getYear();
        Calendar today = Calendar.getInstance();
        today.setTime(s.getSessionBeginDateTime());
        today.set(Calendar.DAY_OF_MONTH,1);
        today.set(Calendar.MONTH, (startMonth+12)%12);
        today.set(Calendar.YEAR, year+(startMonth<0?-1:0)+(startMonth>=12?1:0));
        for (int m=startMonth;m<=endMonth;m++) {
            int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
            for (int d=1;d<=daysOfMonth;d++) {
                if ("1".equals(request.getParameter("cal_val_"+((12+m)%12)+"_"+d))) {
                    iMeetingDates.add(today.getTime());
                }
                today.add(Calendar.DAY_OF_YEAR,1);
            }
        }
	}
	
	public TreeSet<Date> getMeetingDates() { return iMeetingDates; }
	
	// display calendar for event dates
	public String getDatesTable() {
        if (iSessionId==null) return null;
		Session s = Session.getSessionById(iSessionId);
        int startMonth = s.getStartMonth(); 
        int endMonth = s.getEndMonth();
        int year = s.getYear();
        Calendar today = Calendar.getInstance();
        today.setTime(s.getSessionBeginDateTime());
        today.set(Calendar.DAY_OF_MONTH,1);
        today.set(Calendar.MONTH, (startMonth+12)%12);
        today.set(Calendar.YEAR, year+(startMonth<0?-1:0)+(startMonth>=12?1:0));
        String pattern = "[", border = "[";
        for (int m=startMonth;m<=endMonth;m++) {
            if (m!=startMonth) {pattern+=","; border+=","; }
             pattern+="["; border+="[";
             int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
             for (int d=1;d<=daysOfMonth;d++) {
            	 if (d>1) {pattern+=","; border+=","; }
            	 pattern+=(iMeetingDates.contains(today.getTime())?"'1'":"'0'");
            	 today.add(Calendar.DAY_OF_YEAR,1);
            	 border += s.getBorder(d, m);
             }
             pattern+="]"; border+="]";
        }
        pattern+="]"; border+="]";
        return "<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>"+
        	"<script language='JavaScript'>"+
        	"calGenerate("+year+","+startMonth+","+endMonth+","+
            pattern+","+"['1','0'],"+
            "['Selected','Not Selected'],"+
            "['rgb(240,240,50)','rgb(240,240,240)'],"+
            "'1',"+border+",true,true);"+
            "</script>";
	}
	
//	public EventModel getModel() {return iModel;}
//	public void setModel(EventModel model) {iModel=model;}
	
	public String getOp (){return iOp;}
	public void setOp (String op) {iOp = op;}
	
	public String[] getEventTypes() {
		String[] types = new String[] {Event.sEventTypes[Event.sEventTypeCourse], Event.sEventTypes[Event.sEventTypeSpecial]}; 
		return types;
	}
	
	public String getEventType() {return iEventType;}
	public void setEventType(String eventType) {iEventType = eventType;}
	
	public Vector<ComboBoxLookup> getAcademicSessions() {
		Vector<ComboBoxLookup> aSessions = new Vector();
		Date today = new Date();
		for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
			Session session = (Session)i.next();
			//if (!session.getStatusType().canOwnerView()) continue;
			//if (session.getClassesEndDateTime().compareTo(today)<0) continue;
			aSessions.add(new ComboBoxLookup(session.getLabel(),session.getUniqueId().toString()));
		}
		return aSessions;
	}
		
	public Long getSessionId() {return iSessionId;}
	public void setSessionId(Long sessionId) {iSessionId = sessionId;}
	
    public int getStartTime() {return iStartTime; }
    public void setStartTime(int startTime) {iStartTime = startTime;}

    public int getStopTime() {return iStopTime; }
    public void setStopTime(int stopTime) {iStopTime = stopTime;}

	public Vector<ComboBoxLookup> getLocationTypes() {
		Vector<ComboBoxLookup> ltypes = new Vector();
		ltypes.add(new ComboBoxLookup("N/A yet", "1"));
		return ltypes;
	}

//	public String getLocationType() {return iLocationType; }
//    public void setLocationType(String locationType) {iLocationType = locationType;}
    
    //the index i goes in five minute increments, but displayed are 15 minute increments 
    public Vector<ComboBoxLookup> getTimes() {
    	Vector<ComboBoxLookup> times = new Vector();
    	int hour;
    	int minute;
    	String ampm;
    	for (int i=0; i<288; i=i+3) {
    		hour = (i/12)%12;
    		if (hour==0) hour=12; 
    		minute = i%12*5;
    		if (i/144==0) ampm="am"; 
    			else ampm = "pm";
    		times.add(new ComboBoxLookup(hour+":"+(minute<10?"0":"")+minute+" "+ampm, String.valueOf(i)));
    	}
    	return times;
    }
    
    public List getBuildings() {
    	return (iSessionId==null?null:Building.findAll(iSessionId)); 
    }
    
    public Long getBuildingId() { return iBuildingId;}
    public void setBuildingId(Long id) {iBuildingId = id;}
    
    public String getRoomNumber() {return iRoomNumber;}
    public void setRoomNumber(String roomNr) {iRoomNumber = roomNr;}
   
    public String getMinCapacity() {return iMinCapacity;}
    public void setMinCapacity (String minCapacity) {iMinCapacity = minCapacity;}
    
    public String getMaxCapacity() {return iMaxCapacity;}
    public void setMaxCapacity (String minCapacity) {iMaxCapacity = minCapacity;}
    
}
