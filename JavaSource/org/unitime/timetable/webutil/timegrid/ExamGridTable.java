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
package org.unitime.timetable.webutil.timegrid;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;

import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamGridForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamInstructorInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.timegrid.ExamGridTable.ExamGridModel.ExamGridCell;

/**
 * @author Tomas Muller
 */
public class ExamGridTable {
	public static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);
	
    public static final int sResourceRoom = 0;
    public static final int sResourceInstructor = 1;
    public static final int sResourceSubject = 2;
    public static final String[] sResources = new String[] {"Room", "Instructor", "Subject Area"};
    public static final int sBgNone = 0;
    public static final int sBgStudentConfs = 1;
    public static final int sBgDirectStudentConfs = 2;
    public static final int sBgMoreThanTwoADayStudentConfs = 3;
    public static final int sBgBackToBackStudentConfs = 4;
    public static final int sBgInstructorConfs = 5;
    public static final int sBgDirectInstructorConfs = 6;
    public static final int sBgMoreThanTwoADayInstructorConfs = 7;
    public static final int sBgBackToBackInstructorConfs = 8;
    public static final int sBgPeriodPref = 9;
    public static final int sBgRoomPref = 10;
    public static final int sBgDistPref = 11;
    public static final String[] sBackgrounds = new String[] {
        "No Background",
        "Student Conflicts",
        "Student Direct Conflicts",
        "Student >2 A Day Conlicts",
        "Student Back-To-Back Conlicts",
        "Instructor Conflicts",
        "Instructor Direct Conflicts",
        "Instructor >2 A Day Conlicts",
        "Instructor Back-To-Back Conlicts",
        "Period Preferences",
        "Room Preferences",
        "Distribution Preferences"
    };
    public static final int sDispModeInRowHorizontal   = 0;
    public static final int sDispModeInRowVertical   = 1;
    public static final int sDispModePerDayHorizontal = 2;
    public static final int sDispModePerDayVertical = 3;
    public static final int sDispModePerWeekHorizontal = 4;
    public static final int sDispModePerWeekVertical = 5;
    public static String[] sDispModes = new String[] {
        "In Row [horizontal]",
        "In Row [vertical]",
        "Per Day [horizontal]",
        "Per Day [vertical]",
        "Per Week [horizontal]",
        "Per Week [vertical]"
    };
    public static final int sOrderByNameAsc = 0;
    public static final int sOrderByNameDesc = 1;
    public static final int sOrderBySizeAsc = 2;
    public static final int sOrderBySizeDesc = 3;
    public static String[] sOrders = new String[] {
        "name [asc]", "name [desc]", "size [asc]", "size [desc]"
    };

    public static String sBgColorEmpty = "rgb(255,255,255)";
    public static String sBgColorRequired = "rgb(80,80,200)";
    public static String sBgColorStronglyPreferred = "rgb(40,180,60)"; 
    public static String sBgColorPreferred = "rgb(170,240,60)";
    public static String sBgColorNeutral = "rgb(240,240,240)";
    public static String sBgColorDiscouraged = "rgb(240,210,60)";
    public static String sBgColorStronglyDiscouraged = "rgb(240,120,60)";
    public static String sBgColorProhibited = "rgb(220,50,40)";
    public static String sBgColorNotAvailable = "rgb(200,200,200)";
    public static String sBgColorNotAvailableButAssigned = sBgColorProhibited;

    Vector<ExamGridModel> iModels = new Vector<ExamGridModel>();
    ExamGridForm iForm = null;
    TreeSet<Integer> iDates = new TreeSet();
    TreeSet<Integer> iStartsSlots = new TreeSet();
    Hashtable<Integer,Hashtable<Integer,ExamPeriod>> iPeriods = new Hashtable();

	public ExamGridTable(ExamGridForm form, SessionContext context, ExamSolverProxy solver) throws Exception {
	    iForm = form;
	    for (Iterator i=iForm.getPeriods(iForm.getExamType().toString()).iterator();i.hasNext();) {
	        ExamPeriod period = (ExamPeriod)i.next();
	        iDates.add(period.getDateOffset());
	        iStartsSlots.add(period.getStartSlot());
	        Hashtable<Integer,ExamPeriod> periodsThisDay = iPeriods.get(period.getDateOffset());
	        if (periodsThisDay==null) {
	            periodsThisDay = new Hashtable<Integer,ExamPeriod>();
	            iPeriods.put(period.getDateOffset(), periodsThisDay);
	        }
	        periodsThisDay.put(period.getStartSlot(), period);
	    }
	    if (iForm.getResource()==sResourceRoom) {
	        Date[] bounds = ExamPeriod.getBounds(form.getSessionId(),form.getExamBeginDate(), form.getExamType());
	        for (Iterator i=Location.findAllExamLocations(iForm.getSessionId(), iForm.getExamType()).iterator();i.hasNext();) {
	            Location location = (Location)i.next();
	            if (match(location.getLabel())) {
	                if (solver!=null && solver.getExamTypeId().equals(iForm.getExamType()))
	                    iModels.add(new RoomExamGridModel(location,
	                            solver.getAssignedExamsOfRoom(location.getUniqueId()),bounds));
	                else
                        iModels.add(new RoomExamGridModel(location,
                                Exam.findAssignedExamsOfLocation(location.getUniqueId(), iForm.getExamType()),bounds));
	            }
	        }
	    } else if (iForm.getResource()==sResourceInstructor) {
	        String instructorNameFormat = UserProperty.NameFormat.get(context.getUser());
	        Hashtable<String,ExamGridModel> models = new Hashtable<String,ExamGridModel> ();
            for (Iterator i=DepartmentalInstructor.findAllExamInstructors(iForm.getSessionId(), iForm.getExamType()).iterator();i.hasNext();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
                if (match(instructor.getName(instructorNameFormat))) {
                    Collection<ExamAssignmentInfo> assignments = null;
                    if (solver!=null  && solver.getExamTypeId().equals(iForm.getExamType()))
                        assignments = solver.getAssignedExamsOfInstructor(instructor.getUniqueId());
                    else
                        assignments = Exam.findAssignedExamsOfInstructor(instructor.getUniqueId(), iForm.getExamType());
                    if (instructor.getExternalUniqueId()==null) {
                        iModels.add(new ExamGridModel(
                                instructor.getUniqueId(),
                                instructor.getName(instructorNameFormat),
                                -1,
                                assignments));
                    } else {
                        ExamGridModel m = models.get(instructor.getExternalUniqueId());
                        if (m==null) {
                            m = new ExamGridModel(
                                    instructor.getUniqueId(),
                                    instructor.getName(instructorNameFormat),
                                    -1,
                                    assignments);
                            iModels.add(m);
                            models.put(instructor.getExternalUniqueId(),m);
                        } else
                            m.addAssignments(assignments);
                    }
                }
            }
	    } else if (iForm.getResource()==sResourceSubject) {
	        for (Iterator i=SubjectArea.getSubjectAreaList(iForm.getSessionId()).iterator();i.hasNext();) {
	            SubjectArea subject = (SubjectArea)i.next();
	            if (match(subject.getSubjectAreaAbbreviation())) {
                    if (solver!=null && solver.getExamTypeId().equals(iForm.getExamType()))
                        iModels.add(new ExamGridModel(
                                subject.getUniqueId(),
                                subject.getSubjectAreaAbbreviation(),
                                -1,
                                solver.getAssignedExams(subject.getUniqueId())));
                    else
                        iModels.add(new ExamGridModel(
                                subject.getUniqueId(),
                                subject.getSubjectAreaAbbreviation(),
                                -1,
                                Exam.findAssignedExams(iForm.getSessionId(),subject.getUniqueId(),iForm.getExamType()))); 
	                
	            }
	        }
	    }
	    Collections.sort(iModels);
	}
	
	public ExamPeriod getPeriod(int day, Integer time) {
	    if (time==null) return null;
	    Hashtable<Integer,ExamPeriod>  periods = iPeriods.get(day);
	    return (periods==null?null:periods.get(time));
	}

	public void printToHtml(JspWriter jsp) {
		PrintWriter out = new PrintWriter(jsp);
		printToHtml(out);
		out.flush();
	}
	
    public int getMaxIdx(ExamGridModel model, int startDay, int endDay, int firstSlot, int lastSlot) {
        int max = 0;
        for (Iterator i=iForm.getPeriods(iForm.getExamType().toString()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (period.getDateOffset()<startDay || period.getDateOffset()>endDay) continue;
            if (period.getStartSlot()<firstSlot || period.getStartSlot()>lastSlot) continue;
            max = Math.max(max, model.getAssignments(period).size()-1);
        }
        return max;
    }
    
    public int getMaxIdx(ExamGridModel model, int dayOfWeek, int firstSlot, int lastSlot) {
        int max = 0;
        for (Iterator i=iForm.getPeriods(iForm.getExamType().toString()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (getDayOfWeek(period.getDateOffset())!=dayOfWeek) continue;
            if (period.getStartSlot()<firstSlot || period.getStartSlot()>lastSlot) continue;
            max = Math.max(max, model.getAssignments(period).size()-1);
        }
        return max;
    }

    public int getMaxIdx(ExamGridModel model, int week, int slot) {
        int max = 0;
        for (Iterator i=iForm.getPeriods(iForm.getExamType().toString()).iterator();i.hasNext();) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (getWeek(period.getDateOffset())!=week) continue;
            if (period.getStartSlot()!=slot) continue;
            max = Math.max(max, model.getAssignments(period).size()-1);
        }
        return max;
    }

    public int getMaxIdx(int day, int time) {
        int max = 0;
        ExamPeriod period = getPeriod(day, time);
        if (period==null) return max;
        for (ExamGridModel model : models()) {
            max = Math.max(max, model.getAssignments(period).size()-1);
        }
        return max;
    }

    public String getDayName(int day) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(iForm.getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, day);
        return sDF.format(c.getTime());
    }
    
    public String getDayOfWeekName(int dayOfWeek) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        return Formats.getDateFormat(Formats.Pattern.DATE_DAY_OF_WEEK).format(c.getTime());
    }
    
    public String getWeekName(int week) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(iForm.getSessionBeginDate());
        c.setLenient(true);
        c.add(Calendar.WEEK_OF_YEAR, week-1);
        Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
        while (c.get(Calendar.DAY_OF_WEEK)!=Calendar.MONDAY) c.add(Calendar.DAY_OF_YEAR, -1);
        String first = df.format(c.getTime());
        while (c.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY) c.add(Calendar.DAY_OF_YEAR, 1);
        String end = df.format(c.getTime());
        return "Week "+week+"<br>"+first+" - "+end;
    }
    
    public String getSlotName(int slot) {
        return Constants.toTime(slot*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
    }
    
    public void printHeaderCell(PrintWriter out, String name, boolean vertical, boolean eod, boolean eol) {
        String style = "TimetableHead" + "Cell" + (eol?"EOL":eod?"EOD":"");
        out.println("<th nowrap width='130' height='40' class='"+style+"'>");
        out.println(name==null?"":name);
        out.println("</th>");
    }
    
    public boolean isVertical() {
        return (iForm.getDispMode()==sDispModeInRowVertical || iForm.getDispMode()==sDispModePerDayVertical || iForm.getDispMode()==sDispModePerWeekVertical);
    }
    
    public void printHeader(PrintWriter out, String name) {
		out.println("<tr valign='top'>");
		boolean vertical = isVertical();
		printHeaderCell(out, name, vertical, false, false);
		TreeSet<Integer> days = days(), slots = slots(), weeks = weeks(), daysOfWeek = daysOfWeek();
		if (iForm.getDispMode()==sDispModeInRowHorizontal) {
		    for (Integer day : days) {
		        for (Integer slot : slots()) {
	                boolean eod = (slot==slots.last());
	                boolean eol = (eod && day==days.last());
	                printHeaderCell(out, getDayName(day)+"<br>"+getSlotName(slot), vertical, eod, eol);
		        }
		    }
		} else if (iForm.getDispMode()==sDispModeInRowVertical) {
		    for (ExamGridModel m : models()) {
                boolean eol = m.equals(models().lastElement());
                printHeaderCell(out, m.getName()+(m.getSize()>0?" ("+m.getSize()+")":""), vertical, false, eol);
		    }
		} else if (iForm.getDispMode()==sDispModePerDayHorizontal) {
            for (Integer slot : slots()) {
                boolean eol = (slot==slots.last());
                printHeaderCell(out, getSlotName(slot), vertical, false, eol);
            }
		} else if (iForm.getDispMode()==sDispModePerDayVertical) {
            for (Integer day : days) {
                boolean eol = (day==days.last());
                printHeaderCell(out, getDayName(day), vertical, false, eol);
            }
		} else if (iForm.getDispMode()==sDispModePerWeekHorizontal) {
		    for (Integer week : weeks) {
		        for (Integer slot : slots) {
		            boolean eod = (slot==slots.last());
		            boolean eol = eod && (week==weeks.last());
		            printHeaderCell(out, getWeekName(week)+"<br>"+getSlotName(slot), vertical, eod, eol);
		        }
            }
		} else if (iForm.getDispMode()==sDispModePerWeekVertical) {
		    for (Integer dow : daysOfWeek) {
                boolean eol = (dow==daysOfWeek.last());
                printHeaderCell(out, getDayOfWeekName(dow), vertical, false, eol);
		    }
		}
		out.println("</tr>");
	}
	
    private void getMouseOverAndMouseOut(StringBuffer onMouseOver, StringBuffer onMouseOut, ExamGridCell cell, String bgColor, boolean changeMouse) {
    	if (cell==null) return;
        ExamAssignmentInfo info = cell.getInfo();
        if (info==null) return;
    	onMouseOver.append(" onmouseover=\"");
        onMouseOut.append(" onmouseout=\"");
        if (iForm.getResource()==sResourceRoom) {
            for (ExamRoomInfo room : info.getRooms()) {
                Long roomId = room.getLocationId();
                onMouseOver.append("if (document.getElementById('"+info.getExamId()+"."+roomId+"')!=null) document.getElementById('"+info.getExamId()+"."+roomId+"').style.backgroundColor='rgb(223,231,242)';");
                onMouseOut.append("if (document.getElementById('"+info.getExamId()+"."+roomId+"')!=null) document.getElementById('"+info.getExamId()+"."+roomId+"').style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';");
            }
        } else if (iForm.getResource()==sResourceInstructor) {
            for (Enumeration e=info.getInstructors().elements();e.hasMoreElements();) {
                Long instructorId = ((ExamInstructorInfo)e.nextElement()).getId();
                onMouseOver.append("if (document.getElementById('"+info.getExamId()+"."+instructorId+"')!=null) document.getElementById('"+info.getExamId()+"."+instructorId+"').style.backgroundColor='rgb(223,231,242)';");
                onMouseOut.append("if (document.getElementById('"+info.getExamId()+"."+instructorId+"')!=null) document.getElementById('"+info.getExamId()+"."+instructorId+"').style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';");
            }
        } else {
            onMouseOver.append("if (document.getElementById('"+info.getExamId()+"')!=null) document.getElementById('"+info.getExamId()+"').style.backgroundColor='rgb(223,231,242)';");
            onMouseOut.append("if (document.getElementById('"+info.getExamId()+"')!=null) document.getElementById('"+info.getExamId()+"').style.backgroundColor='"+(bgColor==null?"transparent":bgColor)+"';");
        }
        if (changeMouse)
        	onMouseOver.append("this.style.cursor='hand';this.style.cursor='pointer';");
        onMouseOver.append("\" ");
        onMouseOut.append("\" ");
    }
    
    public int getWeek(int day) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(iForm.getExamBeginDate());
        cal.setLenient(true);
        cal.add(Calendar.DAY_OF_YEAR, day);
        int week = 1;
        while (cal.getTime().after(iForm.getSessionBeginDate()) && cal.get(Calendar.WEEK_OF_YEAR) != iForm.getSessionBeginWeek()) {
        	cal.add(Calendar.DAY_OF_YEAR, -7); week ++;
        }
        while (cal.getTime().before(iForm.getSessionBeginDate()) && cal.get(Calendar.WEEK_OF_YEAR) != iForm.getSessionBeginWeek()) {
        	cal.add(Calendar.DAY_OF_WEEK, 7); week --;
        }
        return week;
    }
    
    public int getDayOfWeek(int day) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(iForm.getExamBeginDate());
        cal.setLenient(true);
        cal.add(Calendar.DAY_OF_YEAR, day);
        return cal.get(Calendar.DAY_OF_WEEK);
    }
    
    public int getDay(int week, int dayOfWeek) {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(iForm.getSessionBeginDate());
        c.setLenient(true);
        c.add(Calendar.WEEK_OF_YEAR, week-1);
        c.add(Calendar.DAY_OF_WEEK, dayOfWeek - c.get(Calendar.DAY_OF_WEEK));
        Calendar ec = Calendar.getInstance(Locale.US);
        ec.setTime(iForm.getExamBeginDate());
        return c.get(Calendar.DAY_OF_YEAR)-ec.get(Calendar.DAY_OF_YEAR);
    }
    
    public TreeSet<Integer> days() {
        if (iForm.isAllDates(iForm.getExamType().toString())) return iDates;
        TreeSet<Integer> days = new TreeSet();
        if (iForm.getDate(iForm.getExamType().toString())>500) {
            for (Integer day:iDates) {
                if (1000+getWeek(day)==iForm.getDate(iForm.getExamType().toString())) days.add(day);
            }
        } else {
            days.add(iForm.getDate(iForm.getExamType().toString()));
        }
        return days;
    }
	
    public TreeSet<Integer> daysOfWeek() {
        TreeSet<Integer> daysOfWeek = new TreeSet();
        for (Integer day:days()) {
            daysOfWeek.add(getDayOfWeek(day));
        }
        return daysOfWeek;
    }

    public TreeSet<Integer> weeks() {
        TreeSet<Integer> weeks = new TreeSet();
        for (Integer day:days()) {
            weeks.add(getWeek(day));
        }
        return weeks;
    }

    public TreeSet<Integer> days(int week) {
        TreeSet<Integer> days= new TreeSet();
        for (Integer day:days()) {
            if (getWeek(day)==week)
                days.add(day);
        }
        return days;
    }

    public TreeSet<Integer> slots() {
        TreeSet<Integer> slots = new TreeSet();
        for (Integer slot:iStartsSlots) {
            if (slot<iForm.getStartTime(iForm.getExamType().toString()) || slot>iForm.getEndTime(iForm.getExamType().toString())) continue;
            slots.add(slot);
        }
        return slots;
    }
    
    public Integer prev(int slot) {
        Integer prev = null;
        for (Integer s:iStartsSlots) {
            if (s<iForm.getStartTime(iForm.getExamType().toString()) || s>=slot) continue;
            if (prev==null) prev = s;
            else prev = Math.max(prev,s);
        }
        return prev;
    }
    
    public Integer next(int slot) {
        Integer next = null;
        for (Integer s:iStartsSlots) {
            if (s<=slot || s>iForm.getEndTime(iForm.getExamType().toString())) continue;
            if (next==null) next = s;
            else next = Math.min(next,s);
        }
        return next;
    }
    
    public void printCell(PrintWriter out, ExamGridModel model, int day, int slot, int idx, int maxIdx, boolean head, boolean vertical, boolean in, boolean eod, boolean eol) {
        ExamPeriod period = getPeriod(day, slot);
        ExamGridCell cell = model.getAssignment(period,idx);
        String style = "Timetable"+(head || (!in && !vertical) ? "Head":"")+"Cell" + (!head && in && vertical?"In":"") + (vertical?"Vertical":"") + (eol?"EOL":eod?"EOD":"");
        if (cell==null) {
            String bgColor = model.getBackground(period);
            if (bgColor==null && !model.isAvailable(period)) bgColor=sBgColorNotAvailable;
            if (period==null) bgColor=sBgColorNotAvailable;
            if (idx>0 && model.getAssignment(day, slot, idx-1)==null) return;
            int rowspan = 1 + maxIdx - idx;
            out.println("<td rowspan='"+rowspan+"' class='"+style+"' "+(bgColor==null?"":"style='background-color:"+bgColor+"'")+">&nbsp;</td>");
        } else {
            String bgColor = cell.getBackground();
            if (iForm.getBackground()==sBgNone && !sBgColorNotAvailable.equals(bgColor)) {
                if (!model.isAvailable(period))
                    bgColor = sBgColorNotAvailableButAssigned;
            }
            StringBuffer onMouseOver = new StringBuffer();
            StringBuffer onMouseOut = new StringBuffer();
            getMouseOverAndMouseOut(onMouseOver, onMouseOut, cell, bgColor, cell.getOnClick()!=null);
            out.println("<td nowrap "+(bgColor==null?"":"style='background-color:"+bgColor+"' ")+
                    " class='"+style+"' align='center' "+
                    (cell.getOnClick()==null?"":"onclick=\""+cell.getOnClick()+"\" ")+
                    (cell.getId()!=null?"id='"+cell.getId()+"' ":"")+
                    onMouseOver + 
                    onMouseOut +
                    (cell.getTitle()==null?"":"title=\""+cell.getTitle()+"\" ")+
                    ">");
            out.print(cell.getName());
            if (iForm.getResource()!=sResourceRoom)
                out.print("<BR>"+cell.getRoomName());
            else
                out.print(cell.getShortComment()==null?"":"<BR>"+cell.getShortComment());
            out.println("</td>");            
        }
    }
    
    public String getModelName(ExamGridModel model) {
        return model.getName()+(model.getSize()>0?" ("+model.getSize()+")":"");
    }
    
    public void printRowHeaderCell(PrintWriter out, String name, int maxIdx, boolean vertical, boolean head, boolean in) {
        String style = "Timetable"+(head || (!in && !vertical)?"Head":"")+"Cell"+(!head && in && vertical?"In":"")+(vertical?"Vertical":"");
        out.println("<th nowrap width='130' height='40' rowspan='"+(1+maxIdx)+"' class='"+style+"'>");
        out.println(name);
        out.println("</th>");
        
    }
    
    public void printToHtml(PrintWriter out) {
        boolean vertical = isVertical();
        out.println("<table border='0' cellpadding='2' cellspacing='0'>");
        TreeSet<Integer> days = days(), slots = slots(), weeks = weeks(), daysOfWeek = daysOfWeek();
        int rowNumber=0; 
        if (iForm.getDispMode()==sDispModeInRowVertical) {
            int globalMaxIdx = 0;
            for (Integer day:days) 
                for (Integer slot:slots) {
                    globalMaxIdx = Math.max(globalMaxIdx,getMaxIdx(day, slot));
                }
            int week = -1;
            for (Integer day:days) {
                boolean head = false;
                if (week!=getWeek(day)) {
                    week = getWeek(day);
                    head = true;
                    printHeader(out, getWeekName(week));
                }
                for (Integer slot:slots) {
                    if (getPeriod(day, slot)==null) continue;
                    out.println("<tr valign='top'>");
                    int maxIdx = getMaxIdx(day, slot);
                    printRowHeaderCell(out, getDayName(day)+"<br>"+getSlotName(slot), maxIdx, vertical, head && slot==slots.first(), globalMaxIdx==0);
                    for (int idx=0;idx<=maxIdx;idx++) {
                        if (idx>0) out.println("</tr><tr valign='top'>");
                        for (ExamGridModel model : models()) {
                            printCell(out,
                                    model,
                                    day,
                                    slot,
                                    idx, maxIdx,
                                    head && slot==slots.first() && idx==0, vertical, globalMaxIdx==0 || idx>0,
                                    false, model.equals(models().lastElement()));
                        }
                    }
                    out.println("</tr>");
                    rowNumber++;
                }
            }
        } else {
            int tmx = 0;
            for (ExamGridModel m : models())
                tmx = Math.max(tmx,getMaxIdx(m, days.first(),days.last(),slots.first(),slots.last()));
            for (ExamGridModel model : models()) {
                if (iForm.getDispMode()==sDispModeInRowHorizontal) {
                    if (rowNumber%10==0) printHeader(out, null);
                    int maxIdx = getMaxIdx(model, days.first(),days.last(),slots.first(),slots.last());
                    out.println("<tr valign='top'>");
                    printRowHeaderCell(out, model.getName()+(model.getSize()>0?" ("+model.getSize()+")":""), maxIdx, vertical, (rowNumber%10==0), tmx==0);
                    for (int idx=0;idx<=maxIdx;idx++) {
                        if (idx>0) out.println("</tr><tr valign='top'>");
                        for (Integer day:days) {
                            for (Integer slot:slots) {
                                boolean eod = (slot==slots.last());
                                boolean eol = (eod && day==days.last());
                                printCell(out,
                                        model,
                                        day,
                                        slot,
                                        idx, maxIdx,
                                        rowNumber%10==0 && idx==0, vertical, tmx==0 || idx>0,
                                        eod, eol);
                            }
                        }
                    }
                    out.println("</tr>");
                } else if (iForm.getDispMode()==sDispModePerDayVertical) {
                    printHeader(out, getModelName(model));
                    int gmx = getMaxIdx(model, days.first(),days.last(),slots.first(),slots.last());
                    for (Integer slot:slots) {
                        out.println("<tr valign='top'>");
                        int maxIdx = getMaxIdx(model, days.first(), days.last(), slot, slot);
                        printRowHeaderCell(out, getSlotName(slot), maxIdx, vertical, slot==slots.first(), gmx==0);
                        for (int idx=0;idx<=maxIdx;idx++) {
                            if (idx>0) out.println("</tr><tr valign='top'>");
                            for (Integer day:days) {
                                printCell(out,
                                        model,
                                        day,
                                        slot,
                                        idx, maxIdx,
                                        slot==slots.first() && idx==0, vertical, gmx==0 || idx>0,
                                        false, (day==days.last()));
                            }
                        }
                        out.println("</tr>");
                    }
                } else if (iForm.getDispMode()==sDispModePerDayHorizontal) {
                    printHeader(out, getModelName(model));
                    int gmx = getMaxIdx(model, days.first(),days.last(),slots.first(),slots.last());
                    for (Integer day:days) {
                        out.println("<tr valign='top'>");
                        int maxIdx = getMaxIdx(model, day, day,slots.first(),slots.last());
                        printRowHeaderCell(out, getDayName(day), maxIdx, vertical, day==days.first(), gmx==0);
                        for (int idx=0;idx<=maxIdx;idx++) {
                            if (idx>0) out.println("</tr><tr valign='top'>");
                            for (Integer slot:slots) {
                                printCell(out,
                                        model,
                                        day,
                                        slot,
                                        idx, maxIdx,
                                        day==days.first() && idx==0, vertical, gmx==0 || idx>0,
                                        false, (slot==slots.last()));
                            }
                        }
                        out.println("</tr>");
                    }
                } else if (iForm.getDispMode()==sDispModePerWeekHorizontal) {
                    printHeader(out, getModelName(model));
                    int gmx = getMaxIdx(model, days.first(), days.last(), slots.first(),slots.last());
                    for (Integer dow:daysOfWeek()) {
                        out.println("<tr valign='top'>");
                        int maxIdx = getMaxIdx(model, dow,slots.first(),slots.last());
                        printRowHeaderCell(out, getDayOfWeekName(dow), maxIdx, vertical, dow==daysOfWeek.first(), gmx==0);
                        for (int idx=0;idx<=maxIdx;idx++) {
                            if (idx>0) out.println("</tr><tr valign='top'>");
                            for (Integer week : weeks) {
                                for (Integer slot:slots) {
                                    printCell(out,
                                            model,
                                            getDay(week,dow),
                                            slot,
                                            idx, maxIdx,
                                            dow==daysOfWeek.first() && idx==0, vertical, gmx==0 || idx>0,
                                            (slot==slots.last()), (slot==slots.last() && week==weeks.last()));
                                }
                            }
                        }
                        out.println("</tr>");    
                     }
                } else if (iForm.getDispMode()==sDispModePerWeekVertical) {
                    printHeader(out, getModelName(model));
                    int gmx = getMaxIdx(model, days.first(), days.last(), slots.first(),slots.last());
                    for (Integer week : weeks) {
                        for (Integer slot:slots) {
                            out.println("<tr valign='top'>");
                            int maxIdx = getMaxIdx(model, week,slot);
                            printRowHeaderCell(out, getWeekName(week) +"<br>"+ getSlotName(slot), maxIdx, vertical, slot==slots.first(), gmx==0);
                            for (int idx=0;idx<=maxIdx;idx++) {
                                if (idx>0) out.println("</tr><tr valign='top'>");
                                for (Integer dow : daysOfWeek) {
                                    printCell(out, 
                                            model, 
                                            getDay(week,dow), 
                                            slot, 
                                            idx, 
                                            maxIdx, 
                                            slot==slots.first() && idx==0, vertical, gmx==0 || idx>0, 
                                            false, (dow==daysOfWeek.last()));
                                }
                            }                            
                            out.println("</tr>");
                        }
                    }
                }
                rowNumber++;
            }
        }
        out.println("</table>");
    }

	private boolean match(String name) {
		if (iForm.getFilter()==null || iForm.getFilter().trim().length()==0) return true;
        String n = name.toUpperCase();
		StringTokenizer stk1 = new StringTokenizer(iForm.getFilter().toUpperCase(),";");
		while (stk1.hasMoreTokens()) {
		    StringTokenizer stk2 = new StringTokenizer(stk1.nextToken()," ,");
		    boolean match = true;
		    while (match && stk2.hasMoreTokens()) {
		        String token = stk2.nextToken().trim();
		        if (token.length()==0) continue;
		        if (n.indexOf(token)<0) match = false;
		    }
		    if (match) return true;
		}
		return false;
	}
	
	public void printLegend(JspWriter jsp) {
		PrintWriter out = new PrintWriter(jsp);
		printLegend(out);
		out.flush();
	}

	public void printLegend(PrintWriter out) {
		if (iForm.getBackground()!=sBgNone) {
			out.println("<tr><td colspan='2'>Assigned examinations:</td></tr>");
		}
        if (iForm.getBackground()==sBgPeriodPref) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No period preference</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited period</td><td></td></tr>");
        } else if (iForm.getBackground()==sBgRoomPref) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sRequired)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No room preference</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged room</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited room</td><td></td></tr>");
        } else if (iForm.getBackground()==sBgInstructorConfs) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No instructor conflict</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more instructor back-to-back conflicts</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more instructor three or more exams a day conflicts</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more instructor direct conflicts</td><td></td></tr>");
        } else if (iForm.getBackground()==sBgStudentConfs) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No student conflict</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more student back-to-back conflicts</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more student three or more exams a day student conflicts</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>One or more student direct conflicts</td><td></td></tr>");
        } else if (iForm.getBackground()==sBgDirectInstructorConfs) {
            for (int nrConflicts=0;nrConflicts<=6;nrConflicts++) {
                String color = lessConflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==6?"or more ":"")+"instructor direct conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgMoreThanTwoADayInstructorConfs) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"instructor more than two exams a day conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgBackToBackInstructorConfs) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"instructor back to back conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgDirectStudentConfs) {
            for (int nrConflicts=0;nrConflicts<=6;nrConflicts++) {
                String color = lessConflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==6?"or more ":"")+"student direct conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgMoreThanTwoADayStudentConfs) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"student more than two exams a day conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgBackToBackStudentConfs) {
            for (int nrConflicts=0;nrConflicts<=15;nrConflicts++) {
                String color = conflicts2color(nrConflicts);
                out.println("<tr><td width=40 style='background-color:"+color+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>"+nrConflicts+" "+(nrConflicts==15?"or more ":"")+"student back to back conflicts</td><td></td></tr>");
            }
        } else if (iForm.getBackground()==sBgDistPref) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No violated constraint<i>(distance=0)</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged/preferred constraint violated</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged/preferred constraint violated</i></td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Required/prohibited constraint violated</i></td><td></td></tr>");
        }
        out.println("<tr><td colspan='2'>Free times:</td></tr>");
        out.println("<tr><td width=40 style='background-color:"+sBgColorNotAvailable+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Period not available</td><td></td></tr>");
        if (iForm.getBgPreferences() && iForm.getBackground()==sBgPeriodPref) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly preferred period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sPreferred)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Preferred period</td><td></td></tr>");
        }
        out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sNeutral)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>No period preference</td><td></td></tr>");
        if (iForm.getBgPreferences() && iForm.getBackground()==sBgPeriodPref) {
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Discouraged period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sStronglyDiscouraged)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Strongly discouraged period</td><td></td></tr>");
            out.println("<tr><td width=40 style='background-color:"+pref2color(PreferenceLevel.sProhibited)+";border:1px solid rgb(0,0,0)'>&nbsp;</td><td>Prohibited period</td><td></td></tr>");
        }
    }
	
    public static String pref2color(String pref) {
        if (pref==null) return null;
        return PreferenceLevel.prolog2bgColor(pref);
    }
    
    public static String pref2color(int pref) {
        return PreferenceLevel.prolog2bgColor(PreferenceLevel.int2prolog(pref));
    }
    
    public static String conflicts2color(int nrConflicts) {
        if (nrConflicts>15) nrConflicts = 15;
        String color = null;
        if (nrConflicts==0) {
            color = "rgb(240,240,240)";
        } else if (nrConflicts<5) {
            color = "rgb(240,"+(240-(30*nrConflicts/5))+","+(240-(180*nrConflicts/5))+")";
        } else if (nrConflicts<10) {
            color = "rgb(240,"+(210-(90*(nrConflicts-5)/5))+",60)";
        } else {
            color = "rgb("+(240-(20*(nrConflicts-10)/5))+","+(120-(70*(nrConflicts-10)/5))+","+(60-(20*(nrConflicts-10)/5))+")";
        }
        return color;
    }

    public static String lessConflicts2color(int nrConflicts) {
        if (nrConflicts>6) nrConflicts = 6;
        String color = null;
        if (nrConflicts==0) {
            color = "rgb(240,240,240)";
        } else if (nrConflicts<2) {
            color = "rgb(240,"+(240-(30*nrConflicts/2))+","+(240-(180*nrConflicts/2))+")";
        } else if (nrConflicts<4) {
            color = "rgb(240,"+(210-(90*(nrConflicts-2)/2))+",60)";
        } else {
            color = "rgb("+(240-(20*(nrConflicts-4)/2))+","+(120-(70*(nrConflicts-4)/2))+","+(60-(20*(nrConflicts-4)/2))+")";
        }
        return color;
    }
	
	public class ExamGridModel implements Comparable<ExamGridModel>{
	    private Long iId = null;
	    private String iName = null;
	    private int iSize = 0;
	    private int iNrAssignments = 0;
	    private Hashtable<ExamPeriod, Vector<ExamAssignmentInfo>> iAssignments = new Hashtable<ExamPeriod, Vector<ExamAssignmentInfo>>();
	    
	    ExamGridModel(Long id, String name, int size, Collection<ExamAssignmentInfo> assignments) {
	        iId = id;
	        iName = name;
	        iSize = size;
	        for (Iterator i=assignments.iterator();i.hasNext();) {
	            ExamAssignmentInfo exam = (ExamAssignmentInfo)i.next();
	            Vector<ExamAssignmentInfo> a = iAssignments.get(exam.getPeriod());
	            if (a==null) {
	                a = new Vector<ExamAssignmentInfo>();
	                iAssignments.put(exam.getPeriod(), a);
	            }
	            a.add(exam); iNrAssignments++;
	        }
	    }
	    
	    public void addAssignments(Collection<ExamAssignmentInfo> assignments) {
            for (Iterator i=assignments.iterator();i.hasNext();) {
                ExamAssignmentInfo exam = (ExamAssignmentInfo)i.next();
                Vector<ExamAssignmentInfo> a = iAssignments.get(exam.getPeriodId());
                if (a==null) {
                    a = new Vector<ExamAssignmentInfo>();
                    iAssignments.put(exam.getPeriod(), a);
                }
                a.add(exam); iNrAssignments++;
            }
	    }
	    
	    public Long getId() {
	        return iId;
	    }
	    
	    public int getSize() {
            if (iSize<0) return iNrAssignments;
	        return iSize;
	    }
	    
	    public String getName() {
	        return iName;
	    }
	    
	    public Vector<ExamAssignmentInfo> getAssignments(ExamPeriod period) {
	        if (period==null) return new Vector<ExamAssignmentInfo>();
	        Vector<ExamAssignmentInfo> ret = iAssignments.get(period);
	        return (ret==null?new Vector<ExamAssignmentInfo>():ret);
	    }
	    
	    public ExamGridCell getAssignment(int day, int slot, int idx) {
	        return getAssignment(getPeriod(day, slot), idx);
	    }

	    public ExamGridCell getAssignment(ExamPeriod period, int idx) {
	        if (period==null) return null;
	        Vector<ExamAssignmentInfo> assignments = iAssignments.get(period);
	        if (assignments==null || assignments.size()<=idx) return null;
	        ExamAssignmentInfo info = assignments.elementAt(idx);
	        return info==null?null:new ExamGridCell(info);
	    }
	    
	    public boolean isAvailable(ExamPeriod period) {
	        return period!=null && !PreferenceLevel.sProhibited.equals(period.getPrefLevel().getPrefProlog());
	    }
	    
	    public String getBackground(ExamPeriod period) {
	        if (period==null) return null;
	        if (iForm.getBgPreferences() && iForm.getBackground()==sBgPeriodPref) {
	            if (period.getPrefLevel()!=null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog()))
	                return pref2color(period.getPrefLevel().getPrefProlog());
	        }
	        return null;
	    }
	    
	    public int compareTo(ExamGridModel model) {
            switch (iForm.getOrder()) {
            case sOrderByNameAsc :
                return getName().compareTo(model.getName());
            case sOrderByNameDesc :
                return -getName().compareTo(model.getName());
            case sOrderBySizeAsc:
                return Double.compare(getSize(), model.getSize());
            case sOrderBySizeDesc :
                return -Double.compare(getSize(), model.getSize());
            }
            return getId().compareTo(model.getId());
	    }


	    public class ExamGridCell {
	        private ExamAssignmentInfo iInfo = null;
	        public ExamGridCell() {}
	        public ExamGridCell(ExamAssignmentInfo info) {
	            iInfo = info;
	        }
	        private ExamAssignmentInfo getInfo() {
	            return iInfo;
	        }
	        public String getBackground() {
	            switch (iForm.getBackground()) {
	            case sBgPeriodPref :
	                return pref2color(getInfo().getPeriodPref());
                case sBgRoomPref :
                    if (iForm.getResource()==sResourceRoom)
                        return pref2color(getInfo().getRoomPref(ExamGridModel.this.getId()));
                    else
                        return pref2color(getInfo().getRoomPref());
                case sBgDistPref :
                    return pref2color(getInfo().getDistributionPref());
                case sBgStudentConfs :
                    if (getInfo().getNrDirectConflicts()>0)
                        return pref2color(PreferenceLevel.sProhibited);
                    if (getInfo().getNrMoreThanTwoConflicts()>0)
                        return pref2color(PreferenceLevel.sStronglyDiscouraged);
                    if (getInfo().getNrBackToBackConflicts()>0)
                        return pref2color(PreferenceLevel.sDiscouraged);
                    return pref2color(PreferenceLevel.sNeutral);
                case sBgDirectStudentConfs :
                    return lessConflicts2color(getInfo().getNrDirectConflicts());
                case sBgMoreThanTwoADayStudentConfs :
                    return conflicts2color(getInfo().getNrMoreThanTwoConflicts());
                case sBgBackToBackStudentConfs :
                    return conflicts2color(getInfo().getNrBackToBackConflicts());
                case sBgInstructorConfs :
                    if (getInfo().getNrInstructorDirectConflicts()>0)
                        return pref2color(PreferenceLevel.sProhibited);
                    if (getInfo().getNrInstructorMoreThanTwoConflicts()>0)
                        return pref2color(PreferenceLevel.sStronglyDiscouraged);
                    if (getInfo().getNrInstructorBackToBackConflicts()>0)
                        return pref2color(PreferenceLevel.sDiscouraged);
                    return pref2color(PreferenceLevel.sNeutral);
                case sBgDirectInstructorConfs :
                    return lessConflicts2color(getInfo().getNrInstructorDirectConflicts());
                case sBgMoreThanTwoADayInstructorConfs :
                    return conflicts2color(getInfo().getNrInstructorMoreThanTwoConflicts());
                case sBgBackToBackInstructorConfs :
                    return conflicts2color(getInfo().getNrInstructorBackToBackConflicts());
	            }
	            return null;
	        }

	        public String getOnClick() {
	            return "showGwtDialog('Examination Assignment', 'examInfo.do?examId="+getInfo().getExamId()+"','900','90%');";
	        }
	        
	        public String getId() {
	            String id = getInfo().getExamId().toString();
	            if (iForm.getResource()==sResourceRoom || iForm.getResource()==sResourceInstructor)
	                id += "."+ExamGridModel.this.getId();
	            return id;
	        }
	        
	        public String getTitle() {
	            return getInfo().toString();
	        }
	        
	        public String getName() {
	            return (iForm.getShowSections()?getInfo().getSectionName("<br>"):getInfo().getExamName());
	        }
	        
	        public String getRoomName() {
	            return getInfo().getRoomsName(",");
	        }
	        
            public String getShortComment() {
                int dc = getInfo().getNrDirectConflicts();
                int m2d = getInfo().getNrMoreThanTwoConflicts();
                int btb = getInfo().getNrBackToBackConflicts();
                return
                    "<font color='"+(dc>0?PreferenceLevel.prolog2color("P"):"gray")+"'>"+dc+"</font>, "+
                    "<font color='"+(m2d>0?PreferenceLevel.prolog2color("2"):"gray")+"'>"+m2d+"</font>, "+
                    "<font color='"+(btb>0?PreferenceLevel.prolog2color("1"):"gray")+"'>"+btb+"</font>";
            }
            
            public String getShortCommentNoColors() {
                int dc = getInfo().getNrDirectConflicts();
                int m2d = getInfo().getNrMoreThanTwoConflicts();
                int btb = getInfo().getNrBackToBackConflicts();
                return dc+", "+m2d+", "+btb;
            }

	    }
        public class BlockGridCell extends ExamGridCell {
            private TimeBlock iBlock = null;
            public BlockGridCell(TimeBlock block) {
                iBlock = block;
            }
            public String getBackground() {
                return sBgColorNotAvailable;
            }

            public String getOnClick() {
                return null;
            }
            
            public String getId() {
                return null;
            }
            
            public String getTitle() {
                return iBlock.getEventName()+" ("+iBlock.getEventType()+")";
            }
            
            public String getName() {
                return iBlock.getEventName();
            }
            
            public String getRoomName() {
                return iBlock.getEventType();
            }
            
            public String getShortComment() {
                return "";
            }
            
            public String getShortCommentNoColors() {
                return "";
            }
        }
	}
	
	public class RoomExamGridModel extends ExamGridModel {
	    private Hashtable iExamPrefs = new Hashtable();
	    private Collection<TimeBlock> iUnavailabilities = null;
	    
	    RoomExamGridModel(Location location, Collection<ExamAssignmentInfo> assignments, Date[] bounds) {
	        super(location.getUniqueId(), location.getLabel(), location.getCapacity(), assignments);
	        iExamPrefs = location.getExamPreferences(iForm.getExamType());
	        if (RoomAvailability.getInstance()!=null) {
	            iUnavailabilities = RoomAvailability.getInstance().getRoomAvailability(
	                    location.getUniqueId(), 
	                    bounds[0], bounds[1], 
                        (ExamTypeDAO.getInstance().get(iForm.getExamType()).getType()==ExamType.sExamTypeFinal?RoomAvailabilityInterface.sFinalExamType:RoomAvailabilityInterface.sMidtermExamType));
	        }
	    }
	    
	    public TimeBlock getBlock(ExamPeriod period) {
	        if (period==null || iUnavailabilities==null || iUnavailabilities.isEmpty()) return null;
	        for (TimeBlock block : iUnavailabilities)
	            if (period.overlap(block)) return block;
	        return null;
	    }
	    
	    public PreferenceLevel getPreference(ExamPeriod period) {
	        return (iExamPrefs==null?null:(PreferenceLevel)iExamPrefs.get(period));
	    }

        public boolean isAvailable(ExamPeriod period) {
            if (!super.isAvailable(period)) return false;
            if (getBlock(period)!=null) return false;
            PreferenceLevel pref = getPreference(period);
            return (pref==null || !PreferenceLevel.sProhibited.equals(pref.getPrefProlog()));
        }
        
        public String getBackground(ExamPeriod period) {
            if (period==null) return null;
            if (iForm.getBgPreferences() && iForm.getBackground()==sBgPeriodPref) {
                PreferenceLevel pref = getPreference(period);
                if (pref!=null && !PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
                    return pref2color(pref.getPrefProlog());
                if (period.getPrefLevel()!=null && !PreferenceLevel.sNeutral.equals(period.getPrefLevel().getPrefProlog()))
                    return pref2color(period.getPrefLevel().getPrefProlog());
            }
            return null;
        }
        
        public ExamGridCell getAssignment(ExamPeriod period, int idx) {
            ExamGridCell cell = super.getAssignment(period, idx);
            if (cell!=null) return cell;
            if (idx==getAssignments(period).size()) {
                TimeBlock block = getBlock(period);
                if (block!=null) return new BlockGridCell(block);
            }
            return null;
        }

	}
	
	public Vector<ExamGridModel> models() {
	    return iModels;
	}
	
	public ExamGridForm getForm() {
	    return iForm;
	}
}
