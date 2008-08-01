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

import java.text.SimpleDateFormat;
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
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.ComboBoxLookup;

/**
 * @author Zuzana Mullerova
 */
public class EventListForm extends ActionForm {
	private String iOp;
	private String iEventNameSubstring;
	private String iEventMainContactSubstring;
	private String iEventDateFrom;
	private String iEventDateTo;
	private Integer[] iEventTypes = null;
	
	public static final int sModeMyEvents = 0;
	public static final int sModeEvents4Approval = 1;
	public static final int sModeAllEvents = 2;
	public static final int sModeAllApprovedEvents = 3;
	public static final int sModeAllEventsWaitingApproval = 4;
	
	private int iMode = sModeMyEvents;
	private boolean iAdmin = false;
	private boolean iEventMgr = false;
	private boolean iNoRole = false;
	private String iUserId = null;
	private Set iManagingDepts = null;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		String df = "MM/dd/yyyy";
		Date start = null;
		if (iEventDateFrom==null || iEventDateFrom.trim().length()==0)
			;
		else if (!CalendarUtils.isValidDate(iEventDateFrom, df))
			errors.add("eventDateFrom", new ActionMessage("errors.invalidDate", "Date '"+iEventDateFrom+"' (From)"));
		else
			start = CalendarUtils.getDate(iEventDateFrom, df);
		
		Date end = null;
		if (iEventDateTo==null || iEventDateTo.trim().length()==0) {
			//no end
		} else if (!CalendarUtils.isValidDate(iEventDateTo, df))
			errors.add("eventDateTo", new ActionMessage("errors.invalidDate", "Date '"+iEventDateTo+"' (To)"));
		else
			end = CalendarUtils.getDate(iEventDateTo, df);
		
		if (end!=null && !start.equals(end) && !start.before(end))
			errors.add("eventDateTo", new ActionMessage("errors.generic", "Date From cannot occur after Date To"));

		return errors;
	}
	
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iEventDateFrom = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		iEventDateTo = null;
		iEventTypes = new Integer[] {
				Event.sEventTypeFinalExam,
				Event.sEventTypeMidtermExam
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
		
		session.setAttribute("EventList.Mode", iMode);      
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
	    if (isEventManager()) modes.add(new ComboBoxLookup("Events Waiting My Approval", String.valueOf(sModeEvents4Approval)));
	    modes.add(new ComboBoxLookup("All Events", String.valueOf(sModeAllEvents)));
	    if (!isNoRole()) {
	        modes.add(new ComboBoxLookup("All Approved Events", String.valueOf(sModeAllApprovedEvents)));
	        modes.add(new ComboBoxLookup("All Events Waiting Approval", String.valueOf(sModeAllEventsWaitingApproval)));
	    }
	    return modes;
	}

	public Set<Department> getManagingDepartments() {
	    return iManagingDepts;
	}

}
