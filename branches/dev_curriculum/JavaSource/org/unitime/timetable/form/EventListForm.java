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

package org.unitime.timetable.form;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.ComboBoxLookup;
import org.unitime.timetable.webutil.WebTextValidation;

/**
 * @author Zuzana Mullerova
 */
public class EventListForm extends ActionForm {
	private static final long serialVersionUID = -5206194674045902244L;
	private String iOp;
	private String iEventNameSubstring;
	private String iEventMainContactSubstring;
	private String iEventDateFrom;
	private String iEventDateTo;
	private Integer[] iEventTypes = null;
	private Long iSponsorOrgId = null;
	
	public static final int sModeMyEvents = 0;
	public static final int sModeEvents4Approval = 1;
	public static final int sModeAllEvents = 2;
	public static final int sModeAllApprovedEvents = 3;
	public static final int sModeAllEventsWaitingApproval = 4;
	public static final int sModeAllConflictingEvents = 5;
	
	private int iMode = sModeMyEvents;
	private boolean iAdmin = false;
	private boolean iEventMgr = false;
	private boolean iNoRole = false;
	private String iUserId = null;
	private Set iManagingDepts = null;
	private boolean iConf = false;
	
	private boolean iDayMon, iDayTue, iDayWed, iDayThu, iDayFri, iDaySat, iDaySun;
	private int iStartTime;
	private int iStopTime;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();
		if (iOp != null && !("Search".equals(iOp) || "Export PDF".equals(iOp)
				|| "Add Event".equals(iOp) || "iCalendar".equals(iOp)
				|| "Cancel Event".equals(iOp) || "Cancel".equals(iOp)
				|| "Export CSV".equals(iOp)
		)){
			errors.add("op", new ActionMessage("errors.generic", "Invalid Operation."));
			iOp = null;
		}		

		if (iEventNameSubstring !=null && iEventNameSubstring.length() > 50) {
			errors.add("eventName", new ActionMessage("errors.generic", "The event name cannot exceed 50 characters."));
		}
		if (!WebTextValidation.isTextValid(iEventNameSubstring, true)){
			iEventNameSubstring = "";
			errors.add("eventName", new ActionMessage("errors.invalidCharacters", "Event Name"));
		}

		if (iEventMainContactSubstring !=null && iEventMainContactSubstring.length() > 50) {
			errors.add("mainContact", new ActionMessage("errors.generic", "The event name cannot exceed 50 characters."));
		}
		if (!WebTextValidation.isTextValid(iEventMainContactSubstring, true)){
			iEventMainContactSubstring = "";
			errors.add("mainContact", new ActionMessage("errors.invalidCharacters", "Requested by"));
		}
		
		if (iStartTime >= 0 && iStopTime >= 0 && iStartTime>=iStopTime)
			errors.add("stopTime", new ActionMessage("errors.generic", "From Time must be earlier than To Time."));

		String df = "MM/dd/yyyy";
		Date start = null;
		if (iEventDateFrom==null || iEventDateFrom.trim().length()==0){
			iEventDateFrom = null;
		} else {
			if (WebTextValidation.isTextValid(iEventDateFrom, true)){
				if (!CalendarUtils.isValidDate(iEventDateFrom, df)){
					iEventDateFrom = "";
					errors.add("eventDateFrom", new ActionMessage("errors.invalidDate", "From date invalid"));
				}
				else {
					request.setAttribute("eventDateFrom", iEventDateFrom);
					start = CalendarUtils.getDate(iEventDateFrom, df);
				}
			} else {
				iEventDateFrom = "";
				errors.add("eventDateFrom", new ActionMessage("errors.invalidDate", "From date invalid"));		
			}
		}
		Date end = null;
		if (iEventDateTo==null || iEventDateTo.trim().length()==0) {
			iEventDateTo = null;
		} else {
			if (WebTextValidation.isTextValid(iEventDateTo, true)){
				if (!CalendarUtils.isValidDate(iEventDateTo, df)) {
					iEventDateTo = "";
					errors.add("eventDateTo", new ActionMessage("errors.invalidDate", "To date invalid"));
				}
				else {
					request.setAttribute("eventDateTo", iEventDateTo);
					end = CalendarUtils.getDate(iEventDateTo, df);
				}
			} else {
				iEventDateTo = "";
				errors.add("eventDateTo", new ActionMessage("errors.invalidDate", "To date invalid"));
			}
		}
		
		if (end!=null && start!=null && !start.equals(end) && !start.before(end))
			errors.add("eventDateTo", new ActionMessage("errors.generic", "Date From cannot occur after Date To"));

		return errors;
	}
	
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iEventDateFrom = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		iEventDateTo = null;
		iEventTypes = new Integer[] {
				Event.sEventTypeSpecial
		};
		iOp = null;
		User user = Web.getUser(request.getSession());
		iAdmin = false; iEventMgr = false; iNoRole = false;
		if (user==null || user.getRole()==null) {
		    iMode = sModeMyEvents; iNoRole = true;
		} else if (Roles.EVENT_MGR_ROLE.equals(user.getRole())) {
		    iMode = sModeEvents4Approval; iEventMgr = true;
		    TimetableManager mgr = (user==null?null:TimetableManager.getManager(user));
            if (mgr!=null) iManagingDepts = mgr.getDepartments();
		} else if (user.isAdmin()) {
		    iMode = sModeAllEvents;
		    iAdmin = true;
		}
		iUserId = (user==null?null:user.getId());
		iSponsorOrgId = null;
		iConf = false;
		
		iStartTime = -1;
		iStopTime = -1;
		iDayMon = false; iDayTue = false; iDayWed = false; iDayThu = false; iDayFri = false; iDaySat = false; iDaySun = false;
	}
	
	public void load(HttpSession session) {
		String eventTypes = UserData.getProperty(session, "EventList.EventTypesInt", Event.sEventTypeFinalExam+","+Event.sEventTypeMidtermExam);
		StringTokenizer stk = new StringTokenizer(eventTypes,",");
		iEventTypes = new Integer[stk.countTokens()];
		int idx = 0;
		while (stk.hasMoreTokens()) iEventTypes[idx++] = Integer.valueOf(stk.nextToken());
		
		iEventNameSubstring = (String)session.getAttribute("EventList.EventNameSubstring");
 		iEventMainContactSubstring = (String)session.getAttribute("EventList.EventMainContactSubstring");
 		if (session.getAttribute("EventList.EventDateFrom")!=null)
 			iEventDateFrom = (String)session.getAttribute("EventList.EventDateFrom");
		iEventDateTo = (String)session.getAttribute("EventList.EventDateTo");		
		if (session.getAttribute("EventList.Mode")!=null)
		    iMode = (Integer)session.getAttribute("EventList.Mode");
		iSponsorOrgId = (Long)session.getAttribute("EventList.SponsoringOrganizationId");
		if (session.getAttribute("EventList.Conf")!=null) {
		    iConf = (Boolean) session.getAttribute("EventList.Conf");
		}
		
		if (session.getAttribute("EventList.StartTime")!=null) {
			iStartTime = (Integer)session.getAttribute("EventList.StartTime");
		}
		if (session.getAttribute("EventList.StopTime")!=null) {
			iStopTime = (Integer)session.getAttribute("EventList.StopTime");
		}
		if (session.getAttribute("EventList.DayMon")!=null) {
			iDayMon = (Boolean)session.getAttribute("EventList.DayMon");
		}
		if (session.getAttribute("EventList.DayTue")!=null) {
			iDayTue = (Boolean)session.getAttribute("EventList.DayTue");
		}
		if (session.getAttribute("EventList.DayWed")!=null) {
			iDayWed = (Boolean)session.getAttribute("EventList.DayWed");
		}
		if (session.getAttribute("EventList.DayThu")!=null) {
			iDayThu = (Boolean)session.getAttribute("EventList.DayThu");
		}
		if (session.getAttribute("EventList.DayFri")!=null) {
			iDayFri = (Boolean)session.getAttribute("EventList.DayFri");
		}
		if (session.getAttribute("EventList.DaySat")!=null) {
			iDaySat = (Boolean)session.getAttribute("EventList.DaySat");
		}
		if (session.getAttribute("EventList.DaySun")!=null) {
			iDaySun = (Boolean)session.getAttribute("EventList.DaySun");
		}
	}
	
	public void save(HttpSession session) {
		String eventTypes = "";
		if (iEventTypes!=null)
			for (int idx=0; idx<iEventTypes.length; idx++)
				eventTypes += (idx>0?",":"") + iEventTypes[idx];
		UserData.setProperty(session, "EventList.EventTypesInt", eventTypes);
		
		if (iEventNameSubstring==null)
			session.removeAttribute("EventList.EventNameSubstring");
		else
			session.setAttribute("EventList.EventNameSubstring", iEventNameSubstring);

		if (iEventMainContactSubstring==null)
			session.removeAttribute("EventList.EventMainContactSubstring");
		else
			session.setAttribute("EventList.EventMainContactSubstring", iEventMainContactSubstring);
		
		if (iEventDateFrom==null)
			session.removeAttribute("EventList.EventDateFrom");
		else
			session.setAttribute("EventList.EventDateFrom", iEventDateFrom);		

		if (iEventDateTo==null)
			session.removeAttribute("EventList.EventDateTo");
		else
			session.setAttribute("EventList.EventDateTo", iEventDateTo);
		
		if (iSponsorOrgId==null)
		    session.removeAttribute("EventList.SponsoringOrganizationId");
		else
		    session.setAttribute("EventList.SponsoringOrganizationId", iSponsorOrgId);
		
		session.setAttribute("EventList.Mode", iMode);  
		session.setAttribute("EventList.Conf", iConf);
		
		session.setAttribute("EventList.StartTime", iStartTime);
		session.setAttribute("EventList.StopTime", iStopTime);
		session.setAttribute("EventList.DayMon", iDayMon);
		session.setAttribute("EventList.DayTue", iDayTue);
		session.setAttribute("EventList.DayWed", iDayWed);
		session.setAttribute("EventList.DayThu", iDayThu);
		session.setAttribute("EventList.DayFri", iDayFri);
		session.setAttribute("EventList.DaySat", iDaySat);
		session.setAttribute("EventList.DaySun", iDaySun);
	}

	public String getEventNameSubstring () {
		return iEventNameSubstring;
	}
	
	public void setEventNameSubstring (String substring) {
		iEventNameSubstring = substring;
	}

	public String getEventMainContactSubstring () {
		return iEventMainContactSubstring;
	}
	
	public void setEventMainContactSubstring (String substring) {
		iEventMainContactSubstring = substring;
	}
	
	public String getEventDateFrom() { return iEventDateFrom; }
	public void setEventDateFrom(String date) { iEventDateFrom = date; }
	
	public String getEventDateTo() { return iEventDateTo; }
	public void setEventDateTo(String date) { iEventDateTo = date; }

	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

	public Integer[] getEventTypes() { return iEventTypes; }
	public void setEventTypes(Integer[] types) { iEventTypes = types; }
	public String[] getAllEventTypes() {
	    return Event.sEventTypes;
	}
	
	public boolean isAdmin() { return iAdmin; }
	public boolean isNoRole() { return iNoRole; }
	public boolean isEventManager() { return iEventMgr; }
	
	public int getMode() { return iMode; }
	public void setMode(int mode) { iMode = mode; }
	
	public String getUserId() { return iUserId; }
	
	public Vector<ComboBoxLookup> getModes() {
	    Vector<ComboBoxLookup> modes = new Vector();
	    if (!isAdmin()) modes.add(new ComboBoxLookup("My Events", String.valueOf(sModeMyEvents)));
	    if (isEventManager()) modes.add(new ComboBoxLookup("Events Awaiting My Approval", String.valueOf(sModeEvents4Approval)));
	    modes.add(new ComboBoxLookup("All Events", String.valueOf(sModeAllEvents)));
	    if (!isNoRole()) {
	        modes.add(new ComboBoxLookup("All Approved Events", String.valueOf(sModeAllApprovedEvents)));
	        modes.add(new ComboBoxLookup("All Events Awaiting Approval", String.valueOf(sModeAllEventsWaitingApproval)));
	        modes.add(new ComboBoxLookup("All Conflicting Events", String.valueOf(sModeAllConflictingEvents)));
	    }
	    return modes;
	}

	public Set<Department> getManagingDepartments() {
	    return iManagingDepts;
	}
	
    public Long getSponsoringOrganization() { return iSponsorOrgId; }
    public void setSponsoringOrganization(Long org) { iSponsorOrgId = org; }

    public Collection<SponsoringOrganization> getSponsoringOrganizations() {
	    return SponsoringOrganization.findAll();
	}
    
    public boolean getDispConflicts() { return iConf; }
    public void setDispConflicts(boolean conf) { iConf = conf; }

    public int getStartTime() {return iStartTime; }
    public void setStartTime(int startTime) {iStartTime = startTime;}

    public int getStopTime() {return iStopTime; }
    public void setStopTime(int stopTime) {iStopTime = stopTime;}
    
    public boolean isDayMon() { return iDayMon; }
    public void setDayMon(boolean dayMon) { iDayMon = dayMon; }
    public boolean isDayTue() { return iDayTue; }
    public void setDayTue(boolean dayTue) { iDayTue = dayTue; }
    public boolean isDayWed() { return iDayWed; }
    public void setDayWed(boolean dayWed) { iDayWed = dayWed; }
    public boolean isDayThu() { return iDayThu; }
    public void setDayThu(boolean dayThu) { iDayThu = dayThu; }
    public boolean isDayFri() { return iDayFri; }
    public void setDayFri(boolean dayFri) { iDayFri = dayFri; }
    public boolean isDaySat() { return iDaySat; }
    public void setDaySat(boolean daySat) { iDaySat = daySat; }
    public boolean isDaySun() { return iDaySun; }
    public void setDaySun(boolean daySun) { iDaySun = daySun; }

    public Vector<ComboBoxLookup> getTimes() {
    	Vector<ComboBoxLookup> times = new Vector();
    	times.add(new ComboBoxLookup("", "-1"));
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
    
    public Vector<ComboBoxLookup> getStopTimes() {
        Vector<ComboBoxLookup> times = new Vector();
    	times.add(new ComboBoxLookup("", "-1"));
        int hour;
        int minute;
        String ampm;
        for (int i=3; i<=288; i=i+3) {
            hour = (i/12)%12;
            if (hour==0) hour=12; 
            minute = i%12*5;
            if ((i/144)%2==0) ampm="am"; 
                else ampm = "pm";
            times.add(new ComboBoxLookup(hour+":"+(minute<10?"0":"")+minute+" "+ampm, String.valueOf(i)));
        }
        return times;
    }
}
