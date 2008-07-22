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
package org.unitime.timetable.action;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Query;
import org.unitime.commons.web.Web;
import org.unitime.timetable.action.EventGridAction.TableModel.MeetingCell;
import org.unitime.timetable.action.EventGridAction.TableModel.TableCell;
import org.unitime.timetable.form.EventGridForm;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 *
 */

public class EventGridAction extends Action{

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        EventGridForm myForm = (EventGridForm) form;
        
        if (!Web.isLoggedIn(request.getSession())) {
            throw new Exception("Access Denied.");
        }

        String op = myForm.getOp();

        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");
        
        if ("Change".equals(op)) {
            myForm.loadDates(request);
            myForm.save(request.getSession());
        }
        
        if (request.getParameter("backId")!=null) {
            request.setAttribute("hash", request.getParameter("backId"));
        }
        
        if (request.getSession().getAttribute("EventGrid.StartTime")!=null) {
            myForm.load(request.getSession());
        }

        ActionMessages errors = myForm.validate(mapping, request);
        if (!errors.isEmpty()) { 
            saveErrors(request, errors);
            return mapping.findForward("show");
        }
        
        if ("Add Event".equals(op)) {
            if (request.getParameter("select")!=null) {
                String[] select = (String[])request.getParameterValues("select");
                for (int i=0;i<select.length;i++) {
                    StringTokenizer stk = new StringTokenizer(select[i],":");
                    System.out.println("Meeting "+LocationDAO.getInstance().get(Long.valueOf(stk.nextToken())).getLabel()+" "+new SimpleDateFormat("MM/dd").format(new Date(Long.parseLong(stk.nextToken())))+" "+stk.nextToken()+" ... "+stk.nextToken());
                }
            } else {
                errors.add("select", new ActionMessage("errors.generic", "No available time/room selected."));
                saveErrors(request, errors);
            }
        }
        
        return mapping.findForward("show");
    }
    
    public static void printTable(EventGridForm form, JspWriter out) throws IOException {
        Vector<Date> dates = new Vector(form.getMeetingDates());
        if (dates.isEmpty()) return;
        TreeSet<TableModel> model = new TreeSet();
        int step = 3;
        for (Enumeration<Location> e = form.getPossibleLocations().elements();e.hasMoreElements();) {
            Location location = e.nextElement();
            model.add(new TableModel(location, dates, form.getStartTime(), form.getStopTime(), step, 
                    form.isAdmin() || location.getRoomType().getOption(location.getSession()).canScheduleEvents(), form.getMode(),
                    form.isAdmin() || (form.getManagingDepartments()!=null && form.getManagingDepartments().contains(location.getControllingDepartment()))));
        }
        if (model.isEmpty()) return;
        
        out.println("<table border='0' cellpadding='2' cellspacing='0'>");
        if (dates.size()>1) {
            for (TableModel m : model) {
                boolean split = false;
                for (Date date : dates) 
                    if (m.getColSpan(date)>1) split = true;
                out.println("<tr valign='top' align='center'>");
                out.println("<td class='TimetableHeadCell"+(split?"EOD":"")+"'><b>"+m.getLocation().getLabel()+"</b><br><i>("+m.getLocation().getCapacity()+" seats)</i></td>");
                DateFormat df1 = new SimpleDateFormat("EEEE");
                DateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
                DateFormat df3 = new SimpleDateFormat("MM/dd");
                for (Date date : dates) {
                    boolean last = dates.lastElement().equals(date);
                    out.println("<td colspan='"+m.getColSpan(date)+"' class='TimetableHeadCell"+(last?"EOL":split?"EOD":"")+"' id='b"+m.getLocation().getUniqueId()+"."+dates.indexOf(date)+"'><b>"+df1.format(date)+"<br>"+df2.format(date)+"</b></td>");
                }
                out.println("</tr>");
                HashSet<Meeting> rendered = new HashSet();
                int lastCol = (form.getStopTime()-form.getStartTime())/step;
                TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
                for (int col = 0; col<lastCol; col++) {
                    int start = form.getStartTime() + col*step;
                    out.println("<tr valign='top' align='center'><td class='TimetableCell"+(split?"EOD":"")+"' id='a"+m.getLocation().getUniqueId()+"."+col+"'>");
                    int min = start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                    int startHour = min/60;
                    int startMin = min%60;
                    out.println("<b>"+(startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a")+"</b>");
                    out.println("</td>");
                    TreeSet<Integer> blank = new TreeSet<Integer>();
                    for (int row = 0; row < dates.size(); row++ ) {
                        boolean last = (row+1==dates.size());
                        Date date = dates.elementAt(row);
                        TableCell cell = m.getTable()[row][col];
                        int span = m.getColSpan(date);
                        int idx = 0;
                        TreeSet<Integer> cols = new TreeSet();
                        for (int i=0;i<span;i++) cols.add(i);
                        TreeSet<MeetingCell> todo = new TreeSet();
                        for (MeetingCell mc: cell.getMeetings()) {
                            Meeting meeting = mc.getMeeting();
                            if (rendered.add(meeting)) { 
                                todo.add(mc);
                            } else {
                                idx++; 
                                cols.remove(mc.getCol());
                            }
                        }
                        for (MeetingCell mc: todo) {
                            Meeting meeting = mc.getMeeting();
                            int mcol = cols.first(); cols.remove(mcol); mc.setCol(mcol);
                            out.println("<td rowspan='"+mc.getLength()+"' nowrap class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' "+
                                (aboveBlank.contains(mcol)?"style='border-top:#646464 1px solid;'":"")+
                                " onMouseOver=\"evOver(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                                " onMouseOut=\"evOut(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                                " onClick=\"evClick(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                                " title=\""+df3.format(dates.elementAt(row))+" "+meeting.startTime()+" - "+meeting.stopTime()+" "+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")"+"\""+
                                "><a name='A"+meeting.getEvent().getUniqueId()+"'>");
                            if (mc.getLength()>=2) out.println(meeting.startTime()+" - "+meeting.stopTime()+"<br>");
                            if (meeting.getApprovedDate()!=null) out.println("<b>");
                            out.println(meeting.getEvent().getEventName());
                            if (meeting.getApprovedDate()!=null) out.println("</b>");
                            if (mc.getLength()>=3) out.println("<br><i>"+meeting.getEvent().getEventTypeLabel().replaceAll("Event", "")+"</i>");
                            out.println("</a></td>");
                            idx++;
                        }
                        boolean isAvailable = (idx==0);
                        int prev = -2;
                        while (idx<span) {
                            int mcol = cols.first(); cols.remove(mcol);
                            if (!isAvailable) {
                                out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;"+
                                    (col+1<lastCol?"border-bottom:none;":"")+
                                    (prev+1==mcol?"border-left:none;":"")+
                                    "'>&nbsp;</td>");
                                blank.add(mcol);
                                prev = mcol;
                            } else if (m.isEditable() && cell.isEditable()) {
                                out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"'"+
                                        " onMouseOver=\"avOver(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onMouseOut=\"avOut(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onMouseDown=\"avDown(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+"); return false;\" "+
                                        " onMouseUp=\"avUp(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onClick=\"avClick(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\"" +
                                        " onSelectStart=\"return false;\" "+
                                        " id='d"+m.getLocation().getUniqueId()+"."+row+"."+col+"'>");
                                out.println("<input type='checkbox' name='select' value='"+m.getLocation().getUniqueId()+":"+date.getTime()+":"+start+":"+(start+step)+"' id='c"+m.getLocation().getUniqueId()+"."+row+"."+col+"' style='display:none;'>");
                                out.println("&nbsp;");
                                out.println("</td>");
                            } else {
                                out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;'>" +
                                		"&nbsp;</td>");
                            }
                            idx++;
                        }
                    }
                    aboveBlank = blank;
                    out.println("</tr>");
                }
            }
        } else {
            Date date = dates.firstElement();
            boolean split = false;
            for (TableModel m : model) 
                if (m.getColSpan(date)>1) split = true;
            DateFormat df1 = new SimpleDateFormat("EEEE");
            DateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
            DateFormat df3 = new SimpleDateFormat("MM/dd");
            out.println("<tr valign='top' align='center'>");
            out.println("<td class='TimetableHeadCell"+(split?"EOD":"")+"'><b>"+df1.format(date)+"<br>"+df2.format(date)+"</b></td>");
            int row = 0;
            for (TableModel m : model) {
                boolean last = model.last().equals(m);
                out.println("<td colspan='"+m.getColSpan(date)+"' class='TimetableHeadCell"+(last?"EOL":split?"EOD":"")+"' id='b0."+row+"'><b>"+m.getLocation().getLabel()+"</b><br><i>("+m.getLocation().getCapacity()+" seats)</i></td>");
                row++;
            }
            out.println("</tr>");
            HashSet<Meeting> rendered = new HashSet();
            int lastCol = (form.getStopTime()-form.getStartTime())/step;
            TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
            for (int col = 0; col<lastCol; col++) {
                int start = form.getStartTime() + col*step;
                out.println("<tr valign='top' align='center'><td class='TimetableCell"+(split?"EOD":"")+"' id='a0."+col+"'>");
                int min = start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                int startHour = min/60;
                int startMin = min%60;
                out.println("<b>"+(startHour>12?startHour-12:startHour)+":"+(startMin<10?"0":"")+startMin+(startHour>=12?"p":"a")+"</b>");
                out.println("</td>");
                TreeSet<Integer> blank = new TreeSet<Integer>();
                row = 0;
                for (TableModel m : model) {
                    TableCell cell = m.getTable()[0][col];
                    boolean last = m.equals(model.last());
                    int span = m.getColSpan(date);
                    int idx = 0;
                    TreeSet<Integer> cols = new TreeSet();
                    for (int i=0;i<span;i++) cols.add(i);
                    TreeSet<MeetingCell> todo = new TreeSet();
                    for (MeetingCell mc: cell.getMeetings()) {
                        Meeting meeting = mc.getMeeting();
                        if (rendered.add(meeting)) { 
                            todo.add(mc);
                        } else {
                            idx++; 
                            cols.remove(mc.getCol());
                        }
                    }
                    for (MeetingCell mc: todo) {
                        Meeting meeting = mc.getMeeting();
                        int mcol = cols.first(); cols.remove(mcol); mc.setCol(mcol);
                        out.println("<td rowspan='"+mc.getLength()+"' nowrap class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' "+
                            (aboveBlank.contains(mcol)?"style='border-top:#646464 1px solid;'":"")+
                            " onMouseOver=\"evOver(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                            " onMouseOut=\"evOut(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                            " onClick=\"evClick(this,event,"+meeting.getEvent().getUniqueId()+","+meeting.getUniqueId()+");\""+
                            " title=\""+df3.format(date)+" "+meeting.startTime()+" - "+meeting.stopTime()+" "+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")"+"\""+
                            "><a name='A"+meeting.getEvent().getUniqueId()+"'>");
                        if (mc.getLength()>=2) out.println(meeting.startTime()+" - "+meeting.stopTime()+"<br>");
                        if (meeting.getApprovedDate()!=null) out.println("<b>");
                        out.println(meeting.getEvent().getEventName());
                        if (meeting.getApprovedDate()!=null) out.println("</b>");
                        if (mc.getLength()>=3) out.println("<br><i>"+meeting.getEvent().getEventTypeLabel().replaceAll("Event", "")+"</i>");
                        out.println("</a></td>");
                        idx++;
                    }
                    boolean isAvailable = (idx==0);
                    int prev = -2;
                    while (idx<span) {
                        int mcol = cols.first(); cols.remove(mcol);
                        if (!isAvailable) {
                            out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;"+
                                (col+1<lastCol?"border-bottom:none;":"")+
                                (prev+1==mcol?"border-left:none;":"")+
                                "'>&nbsp;</td>");
                            blank.add(mcol);
                            prev = mcol;
                        } else if (m.isEditable() && cell.isEditable()) {
                            out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"'"+
                            " onMouseOver=\"avOver(this,event,0,"+row+","+col+");\" "+
                            " onMouseOut=\"avOut(this,event,0,"+row+","+col+");\" "+
                            " onMouseDown=\"avDown(this,event,0,"+row+","+col+"); return false;\" "+
                            " onMouseUp=\"avUp(this,event,0,"+row+","+col+");\" "+
                            " onClick=\"avClick(this,event,0,"+row+","+col+");\"" +
                            " onSelectStart=\"return false;\" "+
                            " id='d0."+row+"."+col+"'>");
                            out.println("<input type='checkbox' name='select' value='"+m.getLocation().getUniqueId()+":"+date.getTime()+":"+start+":"+(start+step)+"' id='c0."+row+"."+col+"' style='display:none;'>");
                            out.println("&nbsp;");
                            out.println("</td>");
                        } else {
                            out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;'>" +
                                    "&nbsp;</td>");
                        }
                        idx++;
                    }
                    row++;
                }
                aboveBlank = blank;
                out.println("</tr>");
            }
        }
        out.println("</table>");
    }
    
    public static class TableModel implements Comparable<TableModel> {
        private Location iLocation = null;
        private Vector<Date> iDates = null;
        private List<Meeting> iMeetings = null;
        private int iStartSlot, iEndSlot, iCellLength;
        private TableCell[][] iTable;
        private boolean iEdit;
        public TableModel(Location location, Vector<Date> dates, int startSlot, int endSlot, int cellLength, boolean edit, String mode, boolean manager) {
            iEdit = edit;
            iLocation = location;
            iDates = dates;
            iStartSlot = startSlot; iEndSlot = endSlot; iCellLength = cellLength;
            String q = "select m from Meeting m where "+
                "m.locationPermanentId = :locationId and "+
                "m.startPeriod < :endSlot and :startSlot < m.stopPeriod and "+
                "m.meetingDate in (";
            int idx = 0;
            for (Date date : iDates) {
                if (idx>0) q+=",";
                q+=":d"+(idx++);
            }
            q += ")";
            Query query = MeetingDAO.getInstance().getSession().createQuery(q);
            query.setLong("locationId", iLocation.getPermanentId());
            query.setInteger("startSlot", iStartSlot);
            query.setInteger("endSlot", iEndSlot);
            idx = 0;
            for (Date date : iDates) {
                query.setDate("d"+(idx++), date);
            }
            iMeetings = query.setCacheable(true).list();
            iTable = new TableCell[iDates.size()][(endSlot-startSlot)/cellLength];
            for (int row = 0; row<iTable.length; row++) {
                for (int col = 0; col<iTable[row].length; col++) {
                    iTable[row][col] = new TableCell();
                }
            }
            for (Meeting meeting : iMeetings) {
                TableCell[] row = iTable[iDates.indexOf(new Date(meeting.getMeetingDate().getTime()))];
                boolean skip = false;
                if (EventGridForm.sModeApproved.equals(mode) && meeting.getApprovedDate()==null) {
                    skip = true;
                }
                if (EventGridForm.sModeWaiting.equals(mode) && meeting.getApprovedDate()!=null) {
                    skip = true;
                }
                if (skip) {
                    if (!manager) {
                        for (int col = 0; col<row.length; col++) {
                            int start = iStartSlot + col*iCellLength;
                            if (meeting.getStartPeriod()<start+iCellLength && start<meeting.getStopPeriod()) {
                                row[col].setEditable(false);
                            }
                        }
                    }
                    continue;
                }
                MeetingCell mc = new MeetingCell(meeting);
                for (int col = 0; col<row.length; col++) {
                    int start = iStartSlot + col*iCellLength;
                    if (meeting.getStartPeriod()<start+iCellLength && start<meeting.getStopPeriod()) {
                        row[col].getMeetings().add(mc);
                        mc.setLength(mc.getLength()+1);
                        if (mc.getStart()<0) { mc.setStart(start); }
                    }
                }
            }
        }
        public boolean isEditable() {
            return iEdit;
        }
        public int compareTo(TableModel t) {
            return iLocation.compareTo(t.iLocation);
        }
        public int getColSpan(Date date) {
            TableCell[] row = iTable[iDates.indexOf(date)];
            int max = 1;
            for (TableCell cell : row) {
                max = Math.max(max, cell.getMeetings().size());
            }
            return max;
        }
        public Location getLocation() { return iLocation; }
        public TableCell[][] getTable() { return iTable; }
        
        public class TableCell {
            private boolean iEdit = true;
            public TreeSet<MeetingCell> iMeetings = new TreeSet();
            
            public TreeSet<MeetingCell> getMeetings() { return iMeetings; }
            public boolean isEditable() { return iEdit; }
            public void setEditable(boolean edit) { iEdit = edit; }
        }
        
        public class MeetingCell implements Comparable<MeetingCell> {
            private Meeting iMeeting;
            private int iStart = -1, iLength = 0, iCol = -1;
            private boolean iEdit = false;
            
            public MeetingCell(Meeting meeting) {
                iMeeting = meeting;
            }
            public Meeting getMeeting() { return iMeeting; }
            
            public int getStart() { return iStart; }
            public void setStart(int start) { iStart = start; }
            public int getLength() { return iLength; }
            public void setLength(int length) { iLength = length; }
            public int compareTo(MeetingCell mc) {
                int cmp = Double.compare(getStart(), mc.getStart());
                if (cmp!=0) return cmp;
                cmp = Double.compare(getLength(), mc.getLength());
                if (cmp!=0) return cmp;
                return getMeeting().compareTo(mc.getMeeting());
            }
            public int getCol() { return iCol; }
            public void setCol(int col) { iCol = col; }
        }
    }
    
    
}
