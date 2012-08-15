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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.webutil.WebTextValidation;

/**
 * @author Tomas Muller
 */
public class MeetingListForm extends EventListForm {
	private static final long serialVersionUID = -3703482482732848853L;
	private String iOrderBy = null;
    private String iLocation = null;
    public static final String sOrderByName = "Event Name";
    public static final String sOrderByLocation = "Location";
    public static final String sOrderByTime = "Time";
    public static final String[] sOrderBys = new String[] {
        sOrderByName, sOrderByLocation, sOrderByTime
    };
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = super.validate(mapping, request);
		if (iLocation !=null && iLocation.length() > 50) {
			errors.add("location", new ActionMessage("errors.generic", "The event location cannot exceed 50 characters."));
		}
		if (!WebTextValidation.isTextValid(iLocation, true)){
			iLocation = "";
			errors.add("location", new ActionMessage("errors.invalidCharacters", "Event Location"));
		}
		
		if (!(iOrderBy.equals(sOrderByName) || iOrderBy.equals(sOrderByLocation) || iOrderBy.equals(sOrderByTime))){
			iOrderBy = sOrderByName;
		}

        return errors;
    }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iOrderBy = sOrderByName;
        iLocation = null;
    }
    
    public void load(SessionContext context) {
        super.load(context);
        iOrderBy = (String)context.getAttribute("MeetingList.OrderBy");
        iLocation = (String)context.getAttribute("MeetingList.Location");
        if (iOrderBy==null) iOrderBy = sOrderByName;
    }
    
    public void save(SessionContext context) {
        super.save(context);

        if (iOrderBy==null)
        	context.removeAttribute("MeetingList.OrderBy");
        else
        	context.setAttribute("MeetingList.OrderBy", iOrderBy);

        if (iLocation==null)
        	context.removeAttribute("MeetingList.Location");
        else
        	context.setAttribute("MeetingList.Location", iLocation);
    }
    
    public String getOrderBy() { return iOrderBy; }
    public void setOrderBy(String orderBy) { iOrderBy = orderBy; }
    public String[] getOrderBys() { return sOrderBys; }
    
    public String getLocation() { return iLocation; }
    public void setLocation(String loc) { iLocation = loc; }
}
