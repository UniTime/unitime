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
package org.unitime.timetable.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamReportForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.PdfWebTable;

/**
 * @author Tomas Muller
 */
public class RoomAvailabilityAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamReportForm myForm = (ExamReportForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(request.getSession());
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        myForm.load(request.getSession());
        
        Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
        Date[] bounds = ExamPeriod.getBounds(session, myForm.getExamType());
        
        if (bounds!=null && RoomAvailability.getInstance()!=null) {
            RoomAvailability.getInstance().activate(session, bounds[0], bounds[1], "Refresh".equals(op));
        }
        
        WebTable.setOrder(request.getSession(),"roomAvailability.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(request, session.getUniqueId(), true, myForm);
        
        if ("Export PDF".equals(op) && table!=null) {
            PdfWebTable pdfTable = getTable(request, session.getUniqueId(), false, myForm);
            File file = ApplicationProperties.getTempFile("roomavail", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"roomAvailability.ord"));
            request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(request.getSession(),"roomAvailability.ord")), 6, table.getLines().size());
        
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));

        return mapping.findForward("showReport");
    }
    
    public PdfWebTable getTable(HttpServletRequest request, Long sessionId, boolean html, ExamReportForm form) {
        RoomAvailabilityInterface ra = RoomAvailability.getInstance();
        if (ra==null) return null;
        String nl = (html?"<br>":"\n");
        PdfWebTable table =
            new PdfWebTable( 8,
                    "Room Availability", "roomAvailability.do?ord=%%",
                    new String[] {"Room", "Capacity", "Examination"+nl+"Capacity", "Event", "Event Type", "Date", "Start Time", "End Time"},
                    new String[] {"left", "right", "right", "left", "left", "left", "left", "left"},
                    new boolean[] {true, true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        table.setBlankWhenSame(true);
        TreeSet periods = ExamPeriod.findAll(sessionId, form.getExamType());
        if (periods.isEmpty()) {
            table.addLine(new String[] {"<font color='orange'>WARN: No examination periods.</font>"},null);
            return table;
        }
        Date[] bounds = ExamPeriod.getBounds(new SessionDAO().get(sessionId), form.getExamType());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MM/dd/yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mmaa");
        String ts = null;
        try {
            for (Iterator i=Location.findAllExamLocations(sessionId, form.getExamType()).iterator();i.hasNext();) {
                Location location = (Location)i.next();
                Collection<TimeBlock> events = ra.getRoomAvailability(location, bounds[0], bounds[1], 
                        new String[]{(form.getExamType()==Exam.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType)});
                if (events==null) continue;
                if (ts==null) ts = ra.getTimeStamp(bounds[0], bounds[1]);
                for (TimeBlock event : events) {
                    boolean overlaps = false;
                    for (Iterator j=periods.iterator();j.hasNext();) {
                        ExamPeriod period = (ExamPeriod)j.next();
                        if (period.overlap(event)) { overlaps = true; break; }
                    }
                    if (!overlaps) continue;
                    table.addLine(
                            null,
                            new String[] {
                                location.getLabel(),
                                location.getCapacity().toString(),
                                location.getExamCapacity().toString(),
                                event.getEventName(),
                                event.getEventType(),
                                dateFormat.format(event.getStartTime()),
                                timeFormat.format(event.getStartTime()),
                                timeFormat.format(event.getEndTime()),
                            },
                            new Comparable[] {
                                new MultiComparable(location.getLabel(),event.getStartTime()),
                                new MultiComparable(-location.getCapacity(),location.getLabel(),event.getStartTime()),
                                new MultiComparable(-location.getExamCapacity(),location.getLabel(),event.getStartTime()),
                                new MultiComparable(event.getEventName(),location.getLabel(),event.getStartTime()),
                                new MultiComparable(event.getEventType(),event.getEventName(),location.getLabel(),event.getStartTime()),
                                new MultiComparable(event.getStartTime(),location.getLabel()),
                                new MultiComparable(event.getStartTime().getTime() % 86400000,location.getLabel()),
                                new MultiComparable(event.getEndTime().getTime() % 86400000,location.getLabel())
                            },
                            location.getUniqueId().toString());
                }
            }
            if (ts!=null) request.setAttribute("timestamp", ts);
        } catch (Exception e) {
            Debug.error(e);
            table.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return table;
    }   
}
