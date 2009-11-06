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
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.course.ui.ClassInfoModel;

/**
 * @author Tomas Muller
 */
public class ClassInfoForm extends ActionForm {
    private String iOp;
    private ClassInfoModel iModel;
    private String iMessage;

    private String iMinRoomSize = null, iMaxRoomSize = null;
    private String iRoomFilter = null;
    private boolean iAllowRoomConflict = false;
    private String iRoomOrder = null;
    private boolean iAllRooms = false;
    private boolean iKeepConflictingAssignments = false;
    public static String sRoomOrdNameAsc = "Name [asc]";
    public static String sRoomOrdNameDesc = "Name [desc]";
    public static String sRoomOrdSizeAsc = "Size [asc]";
    public static String sRoomOrdSizeDesc = "Size [desc]";
    public static String[] sRoomOrds = new String[] { sRoomOrdNameAsc, sRoomOrdNameDesc, sRoomOrdSizeAsc, sRoomOrdSizeDesc };
    
    private String iFilter = null;
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        return errors;
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iOp = null;
        iModel = null;
        iMessage = null;
        iMinRoomSize = null;
        iMaxRoomSize = null;
        iRoomFilter = null;
        iAllowRoomConflict = false;
        iAllRooms = false;
        iRoomOrder = sRoomOrdNameAsc;
        iFilter = null;
        iKeepConflictingAssignments = false;
    }
    
    public void load(HttpSession session) {
        iRoomOrder = (String)session.getAttribute("ClassInfo.RoomOrd");
        iMinRoomSize = (String)session.getAttribute("ClassInfo.MinRoomSize");
        iMaxRoomSize = (String)session.getAttribute("ClassInfo.MaxRoomSize");
        iRoomFilter = (String)session.getAttribute("ClassInfo.RoomFilter");
        iAllowRoomConflict = "true".equals(session.getAttribute("ClassInfo.AllowRoomConflict"));
        iAllRooms = "true".equals(session.getAttribute("ClassInfo.AllRooms"));
        iFilter = (String)session.getAttribute("ClassInfo.Filter");
        iKeepConflictingAssignments = "true".equals(session.getAttribute("ClassInfo.KeepConflictingAssignments"));
    }
    
    public void save(HttpSession session) {
        if (iRoomOrder==null)
            session.removeAttribute("ClassInfo.RoomOrd");
        else
            session.setAttribute("ClassInfo.RoomOrd", iRoomOrder);
        if (iMinRoomSize==null)
            session.removeAttribute("ClassInfo.MinRoomSize");
        else
            session.setAttribute("ClassInfo.MinRoomSize", iMinRoomSize);
        if (iMaxRoomSize==null)
            session.removeAttribute("ClassInfo.MaxRoomSize");
        else
            session.setAttribute("ClassInfo.MaxRoomSize", iMaxRoomSize);
        if (iRoomFilter==null)
            session.removeAttribute("ClassInfo.RoomFilter");
        else
            session.setAttribute("ClassInfo.RoomFilter", iRoomFilter);
        session.setAttribute("ClassInfo.AllowRoomConflict", (iAllowRoomConflict?"true":"false"));
        session.setAttribute("ClassInfo.AllRooms", (iAllRooms?"true":"false"));
        if (!iKeepConflictingAssignments)
        	session.removeAttribute("ClassInfo.KeepConflictingAssignments");
        else
        	session.setAttribute("ClassInfo.KeepConflictingAssignments", (iKeepConflictingAssignments?"true":"false"));

        if (iFilter==null)
            session.removeAttribute("ClassInfo.Filter");
        else
            session.setAttribute("ClassInfo.Filter", iFilter);
    }
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public ClassInfoModel getModel() { return iModel; }
    public void setModel(ClassInfoModel model) { iModel = model; }
    public String getMessage() { return iMessage; }
    public void setMessage(String message) { iMessage = message; }
    
    public String getMinRoomSize() { return iMinRoomSize; }
    public void setMinRoomSize(String minRoomSize) { iMinRoomSize = minRoomSize; }
    public String getMaxRoomSize() { return iMaxRoomSize; }
    public void setMaxRoomSize(String maxRoomSize) { iMaxRoomSize = maxRoomSize; }
    public String getRoomFilter() { return iRoomFilter; }
    public void setRoomFilter(String roomFilter) { iRoomFilter = roomFilter; }
    public boolean getAllowRoomConflict() { return iAllowRoomConflict; }
    public void setAllowRoomConflict(boolean allowRoomConflict) { iAllowRoomConflict = allowRoomConflict; }
    public boolean getAllRooms() { return iAllRooms; }
    public void setAllRooms(boolean allRooms) { iAllRooms = allRooms; }
    public String getRoomOrder() { return iRoomOrder; }
    public void setRoomOrder(String ord) { iRoomOrder = ord; }
    public String[] getRoomOrders() { return sRoomOrds; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }

	public boolean getKeepConflictingAssignments() {
		return iKeepConflictingAssignments;
	}

	public void setKeepConflictingAssignments(
			boolean unassignConflictingAssignments) {
		iKeepConflictingAssignments = unassignConflictingAssignments;
	}
}
