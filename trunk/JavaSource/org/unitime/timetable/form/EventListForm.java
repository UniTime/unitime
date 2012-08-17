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
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
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
		iMode = sModeAllEvents;
		iSponsorOrgId = null;
		iConf = false;
		
		iStartTime = -1;
		iStopTime = -1;
		iDayMon = false; iDayTue = false; iDayWed = false; iDayThu = false; iDayFri = false; iDaySat = false; iDaySun = false;
	}
	
	public void load(SessionContext context) {
		String eventTypes = context.getUser().getProperty("EventList.EventTypesInt", Event.sEventTypeFinalExam+","+Event.sEventTypeMidtermExam);
		StringTokenizer stk = new StringTokenizer(eventTypes,",");
		iEventTypes = new Integer[stk.countTokens()];
		int idx = 0;
		while (stk.hasMoreTokens()) iEventTypes[idx++] = Integer.valueOf(stk.nextToken());
		
		iEventNameSubstring = (String)context.getAttribute("EventList.EventNameSubstring");
 		iEventMainContactSubstring = (String)context.getAttribute("EventList.EventMainContactSubstring");
 		if (context.getAttribute("EventList.EventDateFrom")!=null)
 			iEventDateFrom = (String)context.getAttribute("EventList.EventDateFrom");
		iEventDateTo = (String)context.getAttribute("EventList.EventDateTo");
		if (context.getAttribute("EventList.Mode")!=null)
		    iMode = (Integer)context.getAttribute("EventList.Mode");
		else {
			if (context.getUser().getCurrentAuthority() != null && context.getUser().getCurrentAuthority().hasRight(Right.EventMeetingApprove)) {
				iMode = sModeEvents4Approval;
			} else if (context.hasPermission(Right.HasRole)) { 
				iMode = sModeAllEvents;
			} else {
				iMode = sModeMyEvents;
			}
		}
		iSponsorOrgId = (Long)context.getAttribute("EventList.SponsoringOrganizationId");
		if (context.getAttribute("EventList.Conf")!=null) {
		    iConf = (Boolean) context.getAttribute("EventList.Conf");
		}
		
		if (context.getAttribute("EventList.StartTime")!=null) {
			iStartTime = (Integer)context.getAttribute("EventList.StartTime");
		}
		if (context.getAttribute("EventList.StopTime")!=null) {
			iStopTime = (Integer)context.getAttribute("EventList.StopTime");
		}
		if (context.getAttribute("EventList.DayMon")!=null) {
			iDayMon = (Boolean)context.getAttribute("EventList.DayMon");
		}
		if (context.getAttribute("EventList.DayTue")!=null) {
			iDayTue = (Boolean)context.getAttribute("EventList.DayTue");
		}
		if (context.getAttribute("EventList.DayWed")!=null) {
			iDayWed = (Boolean)context.getAttribute("EventList.DayWed");
		}
		if (context.getAttribute("EventList.DayThu")!=null) {
			iDayThu = (Boolean)context.getAttribute("EventList.DayThu");
		}
		if (context.getAttribute("EventList.DayFri")!=null) {
			iDayFri = (Boolean)context.getAttribute("EventList.DayFri");
		}
		if (context.getAttribute("EventList.DaySat")!=null) {
			iDaySat = (Boolean)context.getAttribute("EventList.DaySat");
		}
		if (context.getAttribute("EventList.DaySun")!=null) {
			iDaySun = (Boolean)context.getAttribute("EventList.DaySun");
		}
	}
	
	public void save(SessionContext context) {
		String eventTypes = "";
		if (iEventTypes!=null)
			for (int idx=0; idx<iEventTypes.length; idx++)
				eventTypes += (idx>0?",":"") + iEventTypes[idx];
		context.getUser().setProperty("EventList.EventTypesInt", eventTypes);
		
		if (iEventNameSubstring==null)
			context.removeAttribute("EventList.EventNameSubstring");
		else
			context.setAttribute("EventList.EventNameSubstring", iEventNameSubstring);

		if (iEventMainContactSubstring==null)
			context.removeAttribute("EventList.EventMainContactSubstring");
		else
			context.setAttribute("EventList.EventMainContactSubstring", iEventMainContactSubstring);
		
		if (iEventDateFrom==null)
			context.removeAttribute("EventList.EventDateFrom");
		else
			context.setAttribute("EventList.EventDateFrom", iEventDateFrom);		

		if (iEventDateTo==null)
			context.removeAttribute("EventList.EventDateTo");
		else
			context.setAttribute("EventList.EventDateTo", iEventDateTo);
		
		if (iSponsorOrgId==null)
		    context.removeAttribute("EventList.SponsoringOrganizationId");
		else
		    context.setAttribute("EventList.SponsoringOrganizationId", iSponsorOrgId);
		
		context.setAttribute("EventList.Mode", iMode);  
		context.setAttribute("EventList.Conf", iConf);
		
		context.setAttribute("EventList.StartTime", iStartTime);
		context.setAttribute("EventList.StopTime", iStopTime);
		context.setAttribute("EventList.DayMon", iDayMon);
		context.setAttribute("EventList.DayTue", iDayTue);
		context.setAttribute("EventList.DayWed", iDayWed);
		context.setAttribute("EventList.DayThu", iDayThu);
		context.setAttribute("EventList.DayFri", iDayFri);
		context.setAttribute("EventList.DaySat", iDaySat);
		context.setAttribute("EventList.DaySun", iDaySun);
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
	
	public int getMode() { return iMode; }
	public void setMode(int mode) { iMode = mode; }
	
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
