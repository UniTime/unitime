/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.action;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.RoomAvailabilityForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.PdfWebTable;

/**
 * @author Tomas Muller
 */
@Service("/roomAvailability")
public class RoomAvailabilityAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        RoomAvailabilityForm myForm = (RoomAvailabilityForm) form;
        
        // Check Access
        sessionContext.checkPermission(Right.RoomAvailability);
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(sessionContext);
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        
        myForm.load(sessionContext);
        
        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        
        if (myForm.getExamType() != null && myForm.getExamType() >= 0) {
            
            Date[] bounds = ExamPeriod.getBounds(session, myForm.getExamType());
            String exclude = (myForm.getIncludeExams()?
                    null :
                    (ExamTypeDAO.getInstance().get(myForm.getExamType()).getType() == ExamType.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType));
            
            if (bounds!=null && RoomAvailability.getInstance()!=null) {
                RoomAvailability.getInstance().activate(session, bounds[0], bounds[1], exclude, "Refresh".equals(op));
            }
            
            WebTable.setOrder(sessionContext,(myForm.getCompare()?"roomAvailability.cord":"roomAvailability.ord"),request.getParameter("ord"),1);
            
            WebTable table = (myForm.getCompare()?getCompareTable(request, session.getUniqueId(), true, myForm):getTable(request, session.getUniqueId(), true, myForm));
            
            if ("Export PDF".equals(op) && table!=null) {
                ExportUtils.exportPDF(
                		(myForm.getCompare()?getCompareTable(request, session.getUniqueId(), false, myForm):getTable(request, session.getUniqueId(), false, myForm)),
                		WebTable.getOrder(sessionContext,(myForm.getCompare()?"roomAvailability.cord":"roomAvailability.ord")),
                		response, "roomavail");
                return null;
            }
            
            if (table!=null)
                myForm.setTable(table.printTable(WebTable.getOrder(sessionContext,(myForm.getCompare()?"roomAvailability.cord":"roomAvailability.ord"))), 6, table.getLines().size());
            
            RoomAvailability.setAvailabilityWarning(request, session, myForm.getExamType(), false, true);
        }
        
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

        return mapping.findForward("showReport");
    }
    
    public boolean match(RoomAvailabilityForm form, String name) {
        if (form.getFilter()==null || form.getFilter().trim().length()==0) return true;
        String n = name.toUpperCase();
        StringTokenizer stk1 = new StringTokenizer(form.getFilter().toUpperCase(),";");
        while (stk1.hasMoreTokens()) {
            StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
            boolean match = true;
            while (match && stk2.hasMoreTokens()) {
                String token = stk2.nextToken().trim();
                if (token.length()==0) continue;
                if (token.indexOf('*')>=0 || token.indexOf('?')>=0) {
                    try {
                        String tokenRegExp = "\\s+"+token.replaceAll("\\.", "\\.").replaceAll("\\?", ".+").replaceAll("\\*", ".*")+"\\s";
                        if (!Pattern.compile(tokenRegExp).matcher(" "+n+" ").find()) match = false;
                    } catch (PatternSyntaxException e) { match = false; }
                } else if (n.indexOf(token)<0) match = false;
            }
            if (match) return true;
        }
        return false;
    }

    
    public PdfWebTable getTable(HttpServletRequest request, Long sessionId, boolean html, RoomAvailabilityForm form) {
        RoomAvailabilityInterface ra = RoomAvailability.getInstance();
        if (ra==null) return null;
        String nl = (html?"<br>":"\n");
        PdfWebTable table =
            new PdfWebTable( 8,
                    "Room Availability", "roomAvailability.do?ord=%%",
                    new String[] {"Room", "Capacity", "Examination"+nl+"Capacity", "Event", "Event Type", "Date", "Start Time", "End Time"},
                    new String[] {"left", "right", "right", "left", "left", "left", "left", "left"},
                    new boolean[] {true, true, true, true, true, true, true, true} );
        table.setBlankWhenSame(true);
        TreeSet periods = ExamPeriod.findAll(sessionId, form.getExamType());
        if (periods.isEmpty()) {
            table.addLine(new String[] {"<font color='orange'>WARN: No examination periods.</font>"},null);
            return table;
        }
        Date[] bounds = ExamPeriod.getBounds(new SessionDAO().get(sessionId), form.getExamType());
        Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_MEETING);
        Formats.Format<Date> timeFormat = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
        String ts = null;
        try {
            for (Iterator i=Location.findAllExamLocations(sessionId, form.getExamType()).iterator();i.hasNext();) {
                Location location = (Location)i.next();
                if (!match(form, location.getLabel())) continue;
                String exclude = (form.getIncludeExams()?
                        null :
                        (ExamTypeDAO.getInstance().get(form.getExamType()).getType()==ExamType.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType));
                Collection<TimeBlock> events = ra.getRoomAvailability(location.getUniqueId(), bounds[0], bounds[1], exclude);
                if (events==null) continue;
                if (ts==null) ts = ra.getTimeStamp(bounds[0], bounds[1], exclude);
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
                                timeFormat.format(event.getStartTime()).replaceAll("AM", "a").replaceAll("PM", "p"),
                                timeFormat.format(event.getEndTime()).replaceAll("AM", "a").replaceAll("PM", "p"),
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
    
    public PdfWebTable getCompareTable(HttpServletRequest request, Long sessionId, boolean html, RoomAvailabilityForm form) {
        RoomAvailabilityInterface ra = RoomAvailability.getInstance();
        if (ra==null) return null;
        String nl = (html?"<br>":"\n");
        PdfWebTable table =
            new PdfWebTable( 9,
                    "Examination Comparison", "roomAvailability.do?ord=%%",
                    new String[] {"Room", "Capacity", "Examination"+nl+"Capacity", 
                        "Examination", "Examination"+nl+"Date", "Examination"+nl+"Time",
                        "Event", "Event"+nl+"Date", "Event"+nl+"Time", 
                        },
                    new String[] {"left", "right", "right", 
                        "left", "left", "left", 
                        "left", "left", "left"},
                    new boolean[] {true, true, true, true, true, true, true, true, true} );
        table.setBlankWhenSame(true);
        TreeSet periods = ExamPeriod.findAll(sessionId, form.getExamType());
        if (periods.isEmpty()) {
            table.addLine(new String[] {"<font color='orange'>WARN: No examination periods.</font>"},null);
            return table;
        }
        Date[] bounds = ExamPeriod.getBounds(new SessionDAO().get(sessionId), form.getExamType());
        Formats.Format<Date> dateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
        Formats.Format<Date> timeFormat = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
        String ts = null;
        String eventType = (ExamTypeDAO.getInstance().get(form.getExamType()).getType()==ExamType.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType);
        ExamAssignmentProxy examAssignment = WebSolver.getExamSolver(request.getSession());
        if (examAssignment!=null && !examAssignment.getExamTypeId().equals(form.getExamType())) examAssignment = null;
        try {
            for (Iterator i=Location.findAllExamLocations(sessionId, form.getExamType()).iterator();i.hasNext();) {
                Location location = (Location)i.next();
                if (!match(form, location.getLabel())) continue;
                Collection<TimeBlock> events = ra.getRoomAvailability(location.getUniqueId(), bounds[0], bounds[1], null);
                if (ts==null) ts = ra.getTimeStamp(bounds[0], bounds[1], null);
                TreeSet<ExamAssignment> exams = null;
                if (examAssignment!=null)
                    exams = examAssignment.getExamsOfRoom(location.getUniqueId());
                else {
                    exams = new TreeSet();
                    for (Iterator j=new ExamDAO().getSession().createQuery(
                            "select x from Exam x inner join x.assignedRooms r where x.examType.uniqueId=:examTypeId and r.uniqueId=:locationId").
                            setLong("examTypeId", form.getExamType()).
                            setLong("locationId", location.getUniqueId()).
                            setCacheable(true).
                            list().iterator();j.hasNext();) {
                        exams.add(new ExamAssignment((Exam)j.next()));
                    }
                }
                if (exams==null) exams = new TreeSet();
                if (events==null) events = new TreeSet();
                Hashtable<TimeBlock,ExamAssignment> mapping = new Hashtable<TimeBlock, ExamAssignment>();
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (event.getEventName().trim().equalsIgnoreCase(exam.getExamName().trim()) && exam.getPeriod().overlap(event)) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (event.getEventName().trim().equalsIgnoreCase(exam.getExamName().trim())) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = null;
                    for (ExamAssignment exam : exams) {
                        if (exam.getPeriod().overlap(event)) { match = exam; break; }
                    }
                    if (match!=null) {
                        mapping.put(event, match); exams.remove(match);
                    }
                }
                for (TimeBlock event : events) {
                    if (!eventType.equals(event.getEventType())) continue;
                    ExamAssignment match = mapping.get(event);
                    if (match==null) {
                        table.addLine(
                                null,
                                new String[] {
                                    location.getLabel(),
                                    location.getCapacity().toString(),
                                    location.getExamCapacity().toString(),
                                    "",
                                    "",
                                    "",
                                    (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        event.getEventName()+
                                    (html?"</span>":" @@END_BGCOLOR "),
                                    (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        (html?dateFormat.format(event.getStartTime()).replaceAll(" ","&nbsp;"):dateFormat.format(event.getStartTime()))+
                                    (html?"</span>":" @@END_BGCOLOR "),
                                    (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(event.getStartTime()).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (html?"</span>":" @@END_BGCOLOR ")+
                                    (html?"&nbsp;-&nbsp;":" - ")+
                                    (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(event.getEndTime()).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (html?"</span>":" @@END_BGCOLOR ")
                                },
                                new Comparable[] {
                                    new MultiComparable(location.getLabel(),event.getStartTime()),
                                    new MultiComparable(-location.getCapacity(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable(-location.getExamCapacity(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable("",location.getLabel(),new Date(0)),
                                    new MultiComparable(new Date(0),location.getLabel()),
                                    new MultiComparable(0,location.getLabel()),
                                    new MultiComparable(event.getEventName(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable(event.getStartTime(),location.getLabel()),
                                    new MultiComparable(event.getStartTime().getTime() % 86400000,location.getLabel())
                                },
                                location.getUniqueId().toString());
                    } else {
                        Calendar c = Calendar.getInstance(); 
                        c.setTime(match.getPeriod().getStartTime()); 
                        c.add(Calendar.MINUTE, match.getPrintOffset());
                        Date startTime = c.getTime();
                        c.add(Calendar.MINUTE, match.getLength());
                        Date endTime = c.getTime();
                        boolean nameMatch = event.getEventName().trim().equalsIgnoreCase(match.getExamName().trim());
                        boolean dateMatch = dateFormat.format(event.getStartTime()).equals(dateFormat.format(match.getPeriod().getStartDate()));
                        Date start = event.getStartTime();
                        int breakTimeStart = match.getPeriod().getEventStartOffset().intValue() * Constants.SLOT_LENGTH_MIN;
                        c = Calendar.getInstance(Locale.US); 
                        c.setTime(start);
                        c.add(Calendar.MINUTE, breakTimeStart);
                        start = c.getTime();

                        Date stop = event.getEndTime();
                        int breakTimeStop = match.getPeriod().getEventStopOffset().intValue() * Constants.SLOT_LENGTH_MIN;
                        c = Calendar.getInstance(Locale.US); 
                        c.setTime(stop);
                        c.add(Calendar.MINUTE, -breakTimeStop);
                        stop = c.getTime();
                        boolean startMatch = start.equals(startTime);
                        boolean endMatch = stop.equals(endTime);
                        if (nameMatch && dateMatch && startMatch && endMatch) continue;
                        table.addLine(
                                null,
                                new String[] {
                                    location.getLabel(),
                                    location.getCapacity().toString(),
                                    location.getExamCapacity().toString(),
                                    (nameMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        match.getExamName()+
                                    (nameMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                    (dateMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        (html?dateFormat.format(match.getPeriod().getStartDate()).replaceAll(" ","&nbsp;"):dateFormat.format(match.getPeriod().getStartDate()))+
                                    (dateMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                    (startMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(startTime).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (startMatch?"":html?"</span>":" @@END_BGCOLOR ")+
                                    (html?"&nbsp;-&nbsp;":" - ")+
                                    (endMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(endTime).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (endMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                    (nameMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        event.getEventName()+
                                    (nameMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                    (dateMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        (html?dateFormat.format(event.getStartTime()).replaceAll(" ","&nbsp;"):dateFormat.format(event.getStartTime()))+
                                    (dateMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                    (startMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(event.getStartTime()).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (startMatch?"":html?"</span>":" @@END_BGCOLOR ")+
                                    (html?"&nbsp;-&nbsp;":" - ")+
                                    (endMatch?"":html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                        timeFormat.format(event.getEndTime()).replaceAll("AM", "a").replaceAll("PM", "p")+
                                    (endMatch?"":html?"</span>":" @@END_BGCOLOR "),
                                },
                                new Comparable[] {
                                    new MultiComparable(location.getLabel(),event.getStartTime()),
                                    new MultiComparable(-location.getCapacity(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable(-location.getExamCapacity(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable(match.getExamName(),location.getLabel(),match.getPeriod().getStartTime()),
                                    new MultiComparable(match.getPeriod().getStartTime(),location.getLabel()),
                                    new MultiComparable(match.getPeriod().getStartTime().getTime() % 86400000,location.getLabel()),
                                    new MultiComparable(event.getEventName(),location.getLabel(),event.getStartTime()),
                                    new MultiComparable(event.getStartTime(),location.getLabel()),
                                    new MultiComparable(event.getStartTime().getTime() % 86400000,location.getLabel())
                                },
                                location.getUniqueId().toString());
                    }
                }
                for (ExamAssignment exam : exams) {
                    Calendar c = Calendar.getInstance(); 
                    c.setTime(exam.getPeriod().getStartTime()); 
                    c.add(Calendar.MINUTE, exam.getLength());
                    Date endTime = c.getTime();
                    table.addLine(
                            null,
                            new String[] {
                                location.getLabel(),
                                location.getCapacity().toString(),
                                location.getExamCapacity().toString(),
                                (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                    exam.getExamName()+
                                (html?"</span>":" @@END_BGCOLOR "),
                                (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                    (html?dateFormat.format(exam.getPeriod().getStartDate()).replaceAll(" ","&nbsp;"):dateFormat.format(exam.getPeriod().getStartDate()))+
                                (html?"</span>":" @@END_BGCOLOR "),
                                (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                    timeFormat.format(exam.getPeriod().getStartTime()).replaceAll("AM", "a").replaceAll("PM", "p")+
                                (html?"</span>":" @@END_BGCOLOR ")+
                                (html?"&nbsp;-&nbsp;":" - ")+
                                (html?"<span style='background-color:yellow;'>":"@@BGCOLOR FFFF00 ")+
                                    timeFormat.format(endTime).replaceAll("AM", "a").replaceAll("PM", "p")+
                                (html?"</span>":" @@END_BGCOLOR "),
                                "",
                                "",
                                ""
                            },
                            new Comparable[] {
                                new MultiComparable(location.getLabel(),exam.getPeriod().getStartTime()),
                                new MultiComparable(-location.getCapacity(),location.getLabel(),exam.getPeriod().getStartTime()),
                                new MultiComparable(-location.getExamCapacity(),location.getLabel(),exam.getPeriod().getStartTime()),
                                new MultiComparable(exam.getExamName(),location.getLabel(),exam.getPeriod().getStartTime()),
                                new MultiComparable(exam.getPeriod().getStartTime(),location.getLabel()),
                                new MultiComparable(exam.getPeriod().getStartTime().getTime() % 86400000,location.getLabel()),
                                new MultiComparable("",location.getLabel(),new Date(0)),
                                new MultiComparable(new Date(0),location.getLabel()),
                                new MultiComparable(0,location.getLabel())
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
