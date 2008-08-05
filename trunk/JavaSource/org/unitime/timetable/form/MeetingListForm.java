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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

/**
 * @author Tomas Muller
 */
public class MeetingListForm extends EventListForm {
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
        return errors;
    }
    
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        iOrderBy = sOrderByName;
        iLocation = null;
    }
    
    public void load(HttpSession session) {
        super.load(session);
        iOrderBy = (String)session.getAttribute("MeetingList.OrderBy");
        iLocation = (String)session.getAttribute("MeetingList.Location");
        if (iOrderBy==null) iOrderBy = sOrderByName;
    }
    
    public void save(HttpSession session) {
        super.save(session);

        if (iOrderBy==null)
            session.removeAttribute("MeetingList.OrderBy");
        else
            session.setAttribute("MeetingList.OrderBy", iOrderBy);

        if (iLocation==null)
            session.removeAttribute("MeetingList.Location");
        else
            session.setAttribute("MeetingList.Location", iLocation);
    }
    
    public String getOrderBy() { return iOrderBy; }
    public void setOrderBy(String orderBy) { iOrderBy = orderBy; }
    public String[] getOrderBys() { return sOrderBys; }
    
    public String getLocation() { return iLocation; }
    public void setLocation(String loc) { iLocation = loc; }
}
