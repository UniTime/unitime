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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;

/**
 * @author Tomas Muller
 */
public class ExamInfoForm extends ActionForm {
    private String iOp;
    private ExamInfoModel iModel;
    private String iMessage;

    private String iMinRoomSize = null, iMaxRoomSize = null;
    private String iRoomFilter = null;
    private boolean iAllowRoomConflict = false;
    private String iRoomOrder = null;
    public static String sRoomOrdNameAsc = "Name [asc]";
    public static String sRoomOrdNameDesc = "Name [desc]";
    public static String sRoomOrdSizeAsc = "Size [asc]";
    public static String sRoomOrdSizeDesc = "Size [desc]";
    public static String[] sRoomOrds = new String[] { sRoomOrdNameAsc, sRoomOrdNameDesc, sRoomOrdSizeAsc, sRoomOrdSizeDesc };
    
    private boolean iComputeSuggestions = false;
    private String iFilter = null;
    private int iDepth = 2;
    private long iTimeout = 5000;
    private int iLimit = 30;

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
        iRoomOrder = sRoomOrdNameAsc;
        iComputeSuggestions = false;
        iFilter = null;
        iDepth = 2;
        iTimeout = 5000;
        iLimit = 30;
    }
    
    public void load(HttpSession session) {
        iRoomOrder = (String)session.getAttribute("ExamInfo.RoomOrd");
        iMinRoomSize = (String)session.getAttribute("ExamInfo.MinRoomSize");
        iMaxRoomSize = (String)session.getAttribute("ExamInfo.MaxRoomSize");
        iRoomFilter = (String)session.getAttribute("ExamInfo.RoomFilter");
        iAllowRoomConflict = "true".equals(session.getAttribute("ExamInfo.AllowRoomConflict"));
        iFilter = (String)session.getAttribute("ExamInfo.Filter");
        if (session.getAttribute("ExamInfo.Limit")!=null)
            iLimit = (Integer)session.getAttribute("ExamInfo.Limit");
        if (session.getAttribute("ExamInfo.Depth")!=null)
            iDepth = (Integer)session.getAttribute("ExamInfo.Depth");
        if (session.getAttribute("ExamInfo.Timeout")!=null)
            iTimeout = (Long)session.getAttribute("ExamInfo.Timeout");
        if (session.getAttribute("ExamInfo.ComputeSuggestions")!=null)
            iComputeSuggestions = (Boolean)session.getAttribute("ExamInfo.ComputeSuggestions");
    }
    
    public void save(HttpSession session) {
        if (iRoomOrder==null)
            session.removeAttribute("ExamInfo.RoomOrd");
        else
            session.setAttribute("ExamInfo.RoomOrd", iRoomOrder);
        if (iMinRoomSize==null)
            session.removeAttribute("ExamInfo.MinRoomSize");
        else
            session.setAttribute("ExamInfo.MinRoomSize", iMinRoomSize);
        if (iMaxRoomSize==null)
            session.removeAttribute("ExamInfo.MaxRoomSize");
        else
            session.setAttribute("ExamInfo.MaxRoomSize", iMaxRoomSize);
        if (iRoomFilter==null)
            session.removeAttribute("ExamInfo.RoomFilter");
        else
            session.setAttribute("ExamInfo.RoomFilter", iRoomFilter);
        session.setAttribute("ExamInfo.AllowRoomConflict", (iAllowRoomConflict?"true":"false"));
        if (iFilter==null)
            session.removeAttribute("ExamInfo.Filter");
        else
            session.setAttribute("ExamInfo.Filter", iFilter);
        session.setAttribute("ExamInfo.Depth", iDepth);
        session.setAttribute("ExamInfo.Timeout", iTimeout);
        session.setAttribute("ExamInfo.Limit", iLimit);
        session.setAttribute("ExamInfo.ComputeSuggestions", iComputeSuggestions);
    }
    
    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public ExamInfoModel getModel() { return iModel; }
    public void setModel(ExamInfoModel model) { iModel = model; }
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
    public String getRoomOrder() { return iRoomOrder; }
    public void setRoomOrder(String ord) { iRoomOrder = ord; }
    public String[] getRoomOrders() { return sRoomOrds; }
    public boolean getComputeSuggestions() { return iComputeSuggestions; }
    public void setComputeSuggestions(boolean computeSuggestions) { iComputeSuggestions = computeSuggestions; }
    public int getLimit() { return iLimit; }
    public void setLimit(int limit) { iLimit = limit; }
    public int getDepth() { return iDepth; }
    public void setDepth(int depth) { iDepth = depth; }
    public long getTimeout() { return iTimeout; }
    public void setTimeout(long timeout) { iTimeout = timeout; }
    public String getFilter() { return iFilter; }
    public void setFilter(String filter) { iFilter = filter; }
}
