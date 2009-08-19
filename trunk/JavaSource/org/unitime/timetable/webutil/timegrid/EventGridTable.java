package org.unitime.timetable.webutil.timegrid;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;

import org.hibernate.Query;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.EventGridForm;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.ClassEventDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.util.Constants;

public class EventGridTable {
    protected TreeSet<TableModel> iModel = null;
    protected Vector<Date> iDates;
    protected int iStep = 3;
    protected int iStartSlot, iEndSlot;
    
    public EventGridTable(EventGridForm form) {
        if (form.getStartTime()>=form.getStopTime()) return;
        iDates = new Vector(form.getMeetingDates());
        if (iDates.isEmpty()) return;
        iModel = new TreeSet();
        iStartSlot = form.getStartTime();
        iEndSlot = form.getStopTime();
        for (Enumeration<Location> e = form.getPossibleLocations().elements();e.hasMoreElements();) {
            Location location = e.nextElement();
            iModel.add(new TableModel(location,  
                    form.isAdmin() || location.getRoomType().getOption(location.getSession()).canScheduleEvents(), form.getMode(),
                    form.isAdmin() || (form.getManagingDepartments()!=null && form.getManagingDepartments().contains(location.getControllingDepartment()))));
        }
    }
    
    public void printTable(JspWriter out) throws IOException {
        if (iModel==null || iModel.isEmpty()) return;
        
        out.println("<table border='0' cellpadding='2' cellspacing='0'>");
        if (iDates.size()>1) {
            for (TableModel m : iModel) {
                boolean split = false;
                for (Date date : iDates) 
                    if (m.getColSpan(date)>1) split = true;
                out.println("<tr valign='top' align='center'>");
                out.println("<td class='TimetableHeadCell"+(split?"EOD":"")+"'><b>"+m.getLocation().getLabel()+"</b><br><i>("+m.getLocation().getCapacity()+" seats)</i><br><font size=\"-1\">"+m.getLocation().getRoomTypeLabel()+"</font></td>");
                DateFormat df1 = new SimpleDateFormat("EEEE");
                DateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
                DateFormat df3 = new SimpleDateFormat("MM/dd");
                for (Date date : iDates) {
                    boolean last = iDates.lastElement().equals(date);
                    out.println("<td colspan='"+m.getColSpan(date)+"' class='TimetableHeadCell"+(last?"EOL":split?"EOD":"")+"' id='b"+m.getLocation().getUniqueId()+"."+iDates.indexOf(date)+"'><b>"+df1.format(date)+"<br>"+df2.format(date)+"</b></td>");
                }
                out.println("</tr>");
                HashSet<Meeting> rendered = new HashSet();
                int lastCol = (iEndSlot-iStartSlot)/iStep;
                TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
                for (int col = 0; col<lastCol; col++) {
                    int start = iStartSlot + col*iStep;
                    out.println("<tr valign='top' align='center'><td class='TimetableCell"+(split?"EOD":"")+"' id='a"+m.getLocation().getUniqueId()+"."+col+"'>");
                    out.println("<b>"+Constants.toTime(start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN)+"</b>");
                    out.println("</td>");
                    TreeSet<Integer> blank = new TreeSet<Integer>();
                    for (int row = 0; row < iDates.size(); row++ ) {
                        boolean last = (row+1==iDates.size());
                        Date date = iDates.elementAt(row);
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
                                " title=\""+df3.format(iDates.elementAt(row))+" "+(meeting.isAllDay()?"All Day":meeting.startTime()+" - "+meeting.stopTime())+" "+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")"+"\""+
                                "><a name='A"+meeting.getEvent().getUniqueId()+"'>");
                            if (mc.getLength()>=2) out.println((meeting.isAllDay()?"All Day":meeting.startTime()+" - "+meeting.stopTime())+"<br>");
                            if (meeting.getApprovedDate()!=null) out.println("<b>");
                            out.println(meeting.getEvent().getEventName());
                            if (meeting.getApprovedDate()!=null) out.println("</b>");
                            if (mc.getLength()>=3) out.println("<br><i>"+meeting.getEvent().getEventTypeAbbv()+"</i>");
                            if (mc.getLength()>= 4 && meeting.getEvent().getEventType() == Event.sEventTypeClass){
                        		ClassEvent ce = new ClassEventDAO().get(Long.valueOf(meeting.getEvent().getUniqueId()));
                            	out.println("<br><i>" + (ce.getClazz().getEnrollment() == null?"0":ce.getClazz().getEnrollment().toString()) +" enrl, " + ce.getClazz().getClassLimit()+ " limit</i>");
                            }
                            if (mc.getLength()>= 4 && meeting.getEvent().getEventType() != Event.sEventTypeClass && meeting.getEvent().getMinCapacity() != null){
                            	out.println("<br><i>" + meeting.getEvent().eventCapacityDisplayString()+ " expect attend</i>");
                            }
                            out.println("</a></td>");
                            idx++;
                        }
                        boolean isAvailable = (idx==0);
                        int prev = -2;
                        while (idx<span) {
                            int mcol = cols.first(); cols.remove(mcol);
                            if (!isAvailable) {
                                out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;"+
                                    (col+1<lastCol && !m.getTable()[row][col+1].getMeetings().isEmpty()?"border-bottom:none;":"")+
                                    (prev+1==mcol?"border-left:none;":"")+
                                    "'>&nbsp;</td>");
                                blank.add(mcol);
                                prev = mcol;
                            } else if (m.isEditable() && cell.isEditable()) {
                                out.println("<td colspan='"+span+"' class='TimetableCell"+(last?"EOL":split?"EOD":"")+"'"+
                                        " onMouseOver=\"avOver(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onMouseOut=\"avOut(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onMouseDown=\"avDown(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+"); return false;\" "+
                                        " onMouseUp=\"avUp(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\" "+
                                        " onClick=\"avClick(this,event,"+m.getLocation().getUniqueId()+","+row+","+col+");\"" +
                                        " onSelectStart=\"return false;\" "+
                                        " id='d"+m.getLocation().getUniqueId()+"."+row+"."+col+"'>");
                                out.println("<input type='checkbox' name='select' value='"+m.getLocation().getUniqueId()+":"+date.getTime()+":"+start+":"+(start+iStep)+"' id='c"+m.getLocation().getUniqueId()+"."+row+"."+col+"' style='display:none;'>");
                                out.println("&nbsp;");
                                out.println("</td>");
                            } else {
                                out.println("<td colspan='"+span+"' class='TimetableCell"+(last?"EOL":split?"EOD":"")+"' style='background-color:#E0E0E0;'>" +
                                        "&nbsp;</td>");
                            }
                            if (isAvailable) break;
                            idx++;
                        }
                    }
                    aboveBlank = blank;
                    out.println("</tr>");
                }
            }
        } else {
            Date date = iDates.firstElement();
            boolean split = false;
            for (TableModel m : iModel) 
                if (m.getColSpan(date)>1) split = true;
            DateFormat df1 = new SimpleDateFormat("EEEE");
            DateFormat df2 = new SimpleDateFormat("MMM dd, yyyy");
            DateFormat df3 = new SimpleDateFormat("MM/dd");
            out.println("<tr valign='top' align='center'>");
            out.println("<td class='TimetableHeadCell"+(split?"EOD":"")+"'><b>"+df1.format(date)+"<br>"+df2.format(date)+"</b></td>");
            int row = 0;
            for (TableModel m : iModel) {
                boolean last = iModel.last().equals(m);
                out.println("<td colspan='"+m.getColSpan(date)+"' class='TimetableHeadCell"+(last?"EOL":split?"EOD":"")+"' id='b0."+row+"'><b>"+m.getLocation().getLabel()+"</b><br><i>("+m.getLocation().getCapacity()+" seats)</i><br><font size=\"-1\">"+m.getLocation().getRoomTypeLabel()+"</font></td>");
                row++;
            }
            out.println("</tr>");
            HashSet<Meeting> rendered = new HashSet();
            int lastCol = (iEndSlot-iStartSlot)/iStep;
            TreeSet<Integer> aboveBlank = new TreeSet<Integer>();
            for (int col = 0; col<lastCol; col++) {
                int start = iStartSlot + col*iStep;
                out.println("<tr valign='top' align='center'><td class='TimetableCell"+(split?"EOD":"")+"' id='a0."+col+"'>");
                out.println("<b>"+Constants.toTime(start*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN)+"</b>");
                out.println("</td>");
                TreeSet<Integer> blank = new TreeSet<Integer>();
                row = 0;
                for (TableModel m : iModel) {
                    TableCell cell = m.getTable()[0][col];
                    boolean last = m.equals(iModel.last());
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
                            " title=\""+df3.format(date)+" "+(meeting.isAllDay()?"All Day":meeting.startTime()+" - "+meeting.stopTime())+" "+meeting.getEvent().getEventName()+" ("+meeting.getEvent().getEventTypeLabel()+")"+"\""+
                            "><a name='A"+meeting.getEvent().getUniqueId()+"'>");
                        if (mc.getLength()>=2) out.println((meeting.isAllDay()?"All Day":meeting.startTime()+" - "+meeting.stopTime())+"<br>");
                        if (meeting.getApprovedDate()!=null) out.println("<b>");
                        out.println(meeting.getEvent().getEventName());
                        if (meeting.getApprovedDate()!=null) out.println("</b>");
                        if (mc.getLength()>=3) out.println("<br><i>"+meeting.getEvent().getEventTypeAbbv()+"</i>");
                        if (mc.getLength()>= 4 && meeting.getEvent().getEventType() == Event.sEventTypeClass){
                    		ClassEvent ce = new ClassEventDAO().get(Long.valueOf(meeting.getEvent().getUniqueId()));
                        	out.println("<br><i>" + (ce.getClazz().getEnrollment() == null?"0":ce.getClazz().getEnrollment().toString()) +" enrl, " + ce.getClazz().getClassLimit()+ " limit</i>");
                        }
                        if (mc.getLength()>= 4 && meeting.getEvent().getEventType() != Event.sEventTypeClass && meeting.getEvent().getMinCapacity() != null){
                        	out.println("<br><i>" + meeting.getEvent().eventCapacityDisplayString()+ " expect attend</i>");
                        }
                        out.println("</a></td>");
                        idx++;
                    }
                    boolean isAvailable = (idx==0);
                    int prev = -2;
                    while (idx<span) {
                        int mcol = cols.first(); cols.remove(mcol);
                        if (!isAvailable) {
                            out.println("<td class='TimetableCell"+(mcol+1==span?(last?"EOL":split?"EOD":""):"")+"' style='background-color:#E0E0E0;"+
                                (col+1<lastCol && !m.getTable()[0][col+1].getMeetings().isEmpty()?"border-bottom:none;":"")+
                                (prev+1==mcol?"border-left:none;":"")+
                                "'>&nbsp;</td>");
                            blank.add(mcol);
                            prev = mcol;
                        } else if (m.isEditable() && cell.isEditable()) {
                            out.println("<td colspan='"+span+"' class='TimetableCell"+(last?"EOL":split?"EOD":"")+"'"+
                            " onMouseOver=\"avOver(this,event,0,"+row+","+col+");\" "+
                            " onMouseOut=\"avOut(this,event,0,"+row+","+col+");\" "+
                            " onMouseDown=\"avDown(this,event,0,"+row+","+col+"); return false;\" "+
                            " onMouseUp=\"avUp(this,event,0,"+row+","+col+");\" "+
                            " onClick=\"avClick(this,event,0,"+row+","+col+");\"" +
                            " onSelectStart=\"return false;\" "+
                            " id='d0."+row+"."+col+"'>");
                            out.println("<input type='checkbox' name='select' value='"+m.getLocation().getUniqueId()+":"+date.getTime()+":"+start+":"+(start+iStep)+"' id='c0."+row+"."+col+"' style='display:none;'>");
                            out.println("&nbsp;");
                            out.println("</td>");
                        } else {
                            out.println("<td colspan='"+span+"' class='TimetableCell"+(last?"EOL":split?"EOD":"")+"' style='background-color:#E0E0E0;'>" +
                                    "&nbsp;</td>");
                        }
                        if (isAvailable) break;
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
    
    public class TableModel implements Comparable<TableModel> {
        private Location iLocation = null;
        private List<Meeting> iMeetings = null;
        private TableCell[][] iTable;
        private boolean iEdit;
        public TableModel(Location location, boolean edit, String mode, boolean manager) {
            iEdit = edit;
            iLocation = location;
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
            iTable = new TableCell[iDates.size()][(iEndSlot-iStartSlot)/iStep];
            Date now = new Date();
            boolean allowPast = "true".equals(ApplicationProperties.getProperty("tmtbl.event.allowEditPast","false"));
            for (int row = 0; row<iTable.length; row++) {
                Calendar date = Calendar.getInstance(Locale.US);
                date.setTime(iDates.elementAt(row));
                boolean cellEdit = allowPast || !date.before(now);
                for (int col = 0; col<iTable[row].length; col++) {
                    iTable[row][col] = new TableCell();
                    int stopTime = (iStartSlot + (1+col)*iStep)*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
                    date.set(Calendar.HOUR_OF_DAY, stopTime/60);
                    date.set(Calendar.MINUTE, stopTime%60);
                    if (!allowPast && date.getTime().before(now)) iTable[row][col].setEditable(false);
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
                            int start = iStartSlot + col*iStep;
                            if (meeting.getStartPeriod()<start+iStep && start<meeting.getStopPeriod()) {
                                row[col].setEditable(false);
                            }
                        }
                    }
                    continue;
                }
                MeetingCell mc = new MeetingCell(meeting);
                for (int col = 0; col<row.length; col++) {
                    int start = iStartSlot + col*iStep;
                    if (meeting.getStartPeriod()<start+iStep && start<meeting.getStopPeriod()) {
                        row[col].addMeeting(mc);
                        mc.setLength(mc.getLength()+1);
                        if (mc.getStart()<0) { mc.setStart(start); }
                    }
                }
            }
            for (int row = 0; row<iTable.length; row++) {
                for (int col = 0; col<iTable[row].length; col++) {
                    iTable[row][col].checkMeetings();
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
    }
    
    public static class TableCell {
        private boolean iEdit = true;
        public TreeSet<MeetingCell> iMeetingCells = new TreeSet();
        
        public TreeSet<MeetingCell> getMeetings() { return iMeetingCells; }
        public boolean addMeeting(MeetingCell meeting) {
            return iMeetingCells.add(meeting);
        }
        public boolean isEditable() { return iEdit; }
        public void setEditable(boolean edit) { iEdit = edit; }
        public void checkMeetings() {
            if (iMeetingCells.size()<2) return;
            Vector<MeetingCell> bad = new Vector();
            Comparator<MeetingCell> cmp = new Comparator<MeetingCell>() {
                public int compare(MeetingCell m1, MeetingCell m2) {
                    int cmp = m1.getMeeting().getStartPeriod().compareTo(m2.getMeeting().getStartPeriod());
                    if (cmp!=0) return cmp;
                    return m1.getMeeting().getUniqueId().compareTo(m2.getMeeting().getUniqueId());
                }
            };
            for (MeetingCell m1 : iMeetingCells) {
                for (MeetingCell m2 : iMeetingCells) {
                    if (cmp.compare(m1,m2)>=0) continue;
                    if (!m1.getMeeting().overlaps(m2.getMeeting())) {
                        bad.add(m2.getLength()==1?m1:m2);
                    }
                }
            }
            for (MeetingCell m : bad) {
                if (m.getLength()<=1) continue;
                iMeetingCells.remove(m);
                m.setLength(m.getLength()-1);
            }
        }
    }
    
    public static class MeetingCell implements Comparable<MeetingCell> {
        private Meeting iMeeting;
        private int iStart = -1, iLength = 0, iCol = -1;
        private int iPrinted = 0;
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
            int cmp = getMeeting().getStartTime().compareTo(mc.getMeeting().getStartTime());
            if (cmp!=0) return cmp;
            cmp = getMeeting().getStopTime().compareTo(mc.getMeeting().getStopTime());
            if (cmp!=0) return cmp;
            return getMeeting().getUniqueId().compareTo(mc.getMeeting().getUniqueId());
        }
        public int getCol() { return iCol; }
        public void setCol(int col) { iCol = col; }
        public String toString() {
            return new SimpleDateFormat("MM/dd").format(getMeeting().getMeetingDate())+" "+
                (getMeeting().isAllDay()?"All Day":getMeeting().startTime()+" - "+getMeeting().stopTime())+" "+
                getMeeting().getEvent().getEventName()+" ("+getMeeting().getEvent().getEventTypeLabel()+")";
        }
        public int getPrinted() { return iPrinted; }
        public void setPrinted(int printed) { iPrinted = printed;} 
    }
}
