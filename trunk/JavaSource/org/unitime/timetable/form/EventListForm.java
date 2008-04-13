/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.EventType;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.dao.EventTypeDAO;
import org.unitime.timetable.util.CalendarUtils;

public class EventListForm extends ActionForm {
	private String iOp;
	private String iEventNameSubstring;
	private String iEventMainContactSubstring;
	private String iEventDateFrom;
	private String iEventDateTo;
	private String[] iEventTypes = null;
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		
		ActionErrors errors = new ActionErrors();

		String df = "MM/dd/yyyy";
		Date start = null;
		if (iEventDateFrom==null || iEventDateFrom.trim().length()==0)
			errors.add("eventDateFrom", new ActionMessage("errors.required", "Date (From)"));
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
		iEventTypes = new String[] {
				EventType.sEventTypeEveningExam,
				EventType.sEventTypeFinalExam
		};
		iOp = null;
	}
	
	public void load(HttpSession session) {
		String eventTypes = UserData.getProperty(session, "EventList.EventTypes", EventType.sEventTypeEveningExam+","+EventType.sEventTypeFinalExam);
		StringTokenizer stk = new StringTokenizer(eventTypes,",");
		iEventTypes = new String[stk.countTokens()];
		int idx = 0;
		while (stk.hasMoreTokens()) iEventTypes[idx++] = stk.nextToken();
		
		iEventMainContactSubstring = (String)session.getAttribute("EventList.EventMainContactSubstring");
	}
	
	public void save(HttpSession session) {
		String eventTypes = "";
		if (iEventTypes!=null)
			for (int idx=0; idx<iEventTypes.length; idx++)
				eventTypes += (idx>0?",":"") + iEventTypes[idx];
		UserData.setProperty(session, "EventList.EventTypes", eventTypes);
		
		if (iEventMainContactSubstring==null)
			session.removeAttribute("EventList.EventMainContactSubstring");
		else
			session.setAttribute("EventList.EventMainContactSubstring", iEventMainContactSubstring);
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

	public String[] getEventTypes() { return iEventTypes; }
	public void setEventTypes(String[] types) { iEventTypes = types; }
	public List getAllEventTypes() {
		return new EventTypeDAO().findAll();
	}
}
