/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.security.SessionContext;


/** 
 * @author Tomas Muller
 */
public class RoomAvailabilityForm extends ExamReportForm {
	private static final long serialVersionUID = -7604226806875981047L;
	private String iFilter = null;
    private boolean iIncludeExams = false;
    private boolean iCompare = false;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
	    ActionErrors errors = super.validate(mapping, request);
	    if (getExamType()<0) errors.add("examType", new ActionMessage("errors.required", ""));
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    super.reset(mapping, request);
	    iIncludeExams = false;
	    iFilter = null;
	    iCompare = false;
	    setExamType(null);
	}
	
	public String getFilter() { return iFilter; }
	public void setFilter(String filter) { iFilter = filter; }
	public boolean getIncludeExams() { return iIncludeExams; }
	public void setIncludeExams(boolean exams) { iIncludeExams = exams; }
    public boolean getCompare() { return iCompare; }
    public void setCompare(boolean compare) { iCompare = compare; }
	
	public void load(SessionContext session) {
	    super.load(session);
        setFilter((String)session.getAttribute("RoomAvailability.Filter"));
        setIncludeExams(Boolean.TRUE.equals(session.getAttribute("RoomAvailability.Exams")));
        setCompare(Boolean.TRUE.equals(session.getAttribute("RoomAvailability.Compare")));
    }
	
    public void save(SessionContext session) {
        super.save(session);
        if (getFilter()==null)
            session.removeAttribute("RoomAvailability.Filter");
        else
            session.setAttribute("RoomAvailability.Filter", getFilter());
        session.setAttribute("RoomAvailability.Exams", getIncludeExams());
        session.setAttribute("RoomAvailability.Compare", getCompare());
    }
}

