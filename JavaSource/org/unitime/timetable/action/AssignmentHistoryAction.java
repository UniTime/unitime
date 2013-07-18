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
package org.unitime.timetable.action;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.AssignmentHistoryForm;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver.AssignmentRecord;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.Suggestion;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/assignmentHistory")
public class AssignmentHistoryAction extends Action {
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP_SHORT);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		AssignmentHistoryForm myForm = (AssignmentHistoryForm) form;

        // Check Access
        sessionContext.checkPermission(Right.AssignmentHistory);

        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        SuggestionsModel model = (SuggestionsModel)request.getSession().getAttribute("Suggestions.model");
        if (model==null) {
        	model = new SuggestionsModel();
        	model.load(sessionContext.getUser());
        	request.getSession().setAttribute("Suggestions.model", model);
        }
        
        if ("Apply".equals(op) || "Export PDF".equals(op)) {
        	myForm.save(model);
        	model.save(sessionContext.getUser());
        }
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping, request);
        }
        
        myForm.load(model);
        
        SolverProxy solver = courseTimetablingSolverService.getSolver();
        if (solver!=null) {
        	String historyTable = getHistoryTable(model.getSimpleMode(),request,sessionContext,courseTimetablingSolverService.getSolver(),"History",solver.getAssignmentRecords());
            if (historyTable!=null) {
            	request.setAttribute("AssignmentHistory.table",historyTable);
            	request.setAttribute("AssignmentHistory.table.colspan",new Integer(model.getSimpleMode()?7:17));
            } else
            	request.setAttribute("AssignmentHistory.message","No assignment history available at the moment."); 
        } else {
    		request.setAttribute("AssignmentHistory.message","No timetable is loaded. However, you can load one <a href=\"listSolutions.do\">here</a>.");
        }
		
        if ("Export PDF".equals(op)) {
        	ExportUtils.exportPDF(
        			exportPdf(model.getSimpleMode(),request,sessionContext,courseTimetablingSolverService.getSolver(),"History",solver.getAssignmentRecords()),
        			WebTable.getOrder(sessionContext,"assignmentHistory.ord"),
        			response, "history");
        	return null;
        }

        return mapping.findForward("showAssignmentHistory");
	}
	
    public String getHistoryTable(boolean simple, HttpServletRequest request, SessionContext context, SolverProxy solver, String name, Vector history) {
    	if (history==null || history.isEmpty()) return null;
		WebTable.setOrder(context,"assignmentHistory.ord",request.getParameter("ord"),1);
        WebTable webTable =
        	(simple?
       			new WebTable( 7,
       	        	name, "assignmentHistory.do?ord=%%",
       				new String[] {"Time", "Score", "Class", "Date", "Time", "Room", "Students"},
       				new String[] {"left", "left", "left", "left", "left", "left", "left"},
       				null )
        	:	new WebTable( 17,
        			name, "assignmentHistory.do?ord=%%",
        			new String[] {"Time", "Score", "Class", "Date", "Time", "Room", "Conf","Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "left", "left","right","right","right","right","right","right","right","right","right","right","right"},
        			null ));
        webTable.setRowStyle("white-space:nowrap");
        try {
        	int idx = 0;
        	boolean hasBefore = false;
        	for (Enumeration e=history.elements();e.hasMoreElements();idx++) {
        		AssignmentRecord record = (AssignmentRecord)e.nextElement();
        		StringBuffer classes = new StringBuffer("<table colspan='0' rowspan='0' border='0'>");
        	    StringBuffer rooms = new StringBuffer("<table colspan='0' rowspan='0' border='0'>");
        	    StringBuffer times = new StringBuffer("<table colspan='0' rowspan='0' border='0'>");
        	    StringBuffer roomsSort = new StringBuffer();
        	    StringBuffer timesSort = new StringBuffer();
        	    StringBuffer dates = new StringBuffer("<table colspan='0' rowspan='0' border='0'>");
        	    StringBuffer datesSort = new StringBuffer();
        	    boolean first = true;
        	    for (Enumeration f=record.getAssignments().elements();f.hasMoreElements();) {
        	    	RecordedAssignment assignment = (RecordedAssignment)f.nextElement();
        	    	if (assignment.getBefore()!=null)
        	    		hasBefore=true;
        	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
        	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
        	    	if (before == null && after == null) continue;
        	    	if (!first) {
        	    		roomsSort.append(":");
        	    		timesSort.append(":");
        	    	}
        	    	classes.append("<tr valign='top' height='25'><td nowrap>");
        	        rooms.append("<tr valign='top' height='25'><td nowrap>");
        	        times.append("<tr valign='top' height='25'><td nowrap>");
        	        dates.append("<tr valign='top' height='25'><td nowrap>");
        	        classes.append((before==null?after.getClazz().toHtml(true,true):before.getClazz().toHtml(true,true)));
        	        times.append(ClassAssignmentDetails.dispTime2((before==null?null:before.getAssignedTime()),(after==null?null:after.getAssignedTime())));
        	        for (int i=0;i<(before==null?after.getAssignedRoom().length:before.getAssignedRoom().length);i++) {
        	        	if (i>0) rooms.append(", ");
        	        	rooms.append(ClassAssignmentDetails.dispRoom2((before==null?null:before.getAssignedRoom()[i]),(after==null?null:after.getAssignedRoom()[i])));
        	        }
        	        dates.append((before == null || before.getAssignedTime() == null ? "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>" : before.getAssignedTime().getDatePatternName()) +
            	        	(after == null || after.getAssignedTime() == null ? " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>" :
            	        		before != null && before.getAssignedTime().getDatePatternName().equals(after.getAssignedTime().getDatePatternName()) ? "" : " &rarr; " + after.getAssignedTime().getDatePatternName()));
        	        timesSort.append(before==null?after.getTimeName():before.getTimeName());
        	        roomsSort.append(before==null?after.getRoomName():before.getRoomName());
        	        datesSort.append(before==null?after.getDaysName():before.getDaysName());
        	        classes.append("</td></tr>");
        	        rooms.append("</td></tr>");
        	        times.append("</td></tr>");
        	        dates.append("</td></tr>");
        	        first = false;
        	    }
        	    if (first) continue;
        	    classes.append("</table>");
        	    rooms.append("</table>");
        	    times.append("</table>");
        	    dates.append("</table>");
        	    
        	    Suggestion bSg = record.getBefore();
        	    Suggestion aSg = record.getAfter();
        	    
        	    StringBuffer sb = new StringBuffer();
        	    if (aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("c",aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()));
        	    }
        	    if (aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("d",aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()));
        	    }
        	    if (aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("h",aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()));
        	    }
        	    if (sb.length()>0) sb.append(")");
        	    
        	    if (simple)
            	    webTable.addLine((hasBefore?"onClick=\"showGwtDialog('Suggestions', 'suggestions.do?hist="+idx+"&op=ShowHistory','900','90%');\"":null),
            	    		new String[] {
            	    			sDF.format(record.getTimeStamp()),
            	    			ClassAssignmentDetails.dispNumber(aSg.getValue()-bSg.getValue()),
            	    			classes.toString(),
            	    			dates.toString(),
            	    			times.toString(),
            	    			rooms.toString(),
            	    			ClassAssignmentDetails.dispNumber(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())+sb
            	    			},
            	             new Comparable[] {
            	             	record.getTimeStamp(),
            	                new Double(aSg.getValue()-bSg.getValue()),
            	                classes.toString(),
            	                datesSort.toString(),
            	                timesSort.toString(),
            	                roomsSort.toString(),
            	                new Long(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())
            	             });
        	    else
            	    webTable.addLine((hasBefore?"onClick=\"onClick=\"showGwtDialog('Suggestions', 'suggestions.do?hist="+idx+"&op=ShowHistory','900','90%');\"":null),
            	    		new String[] {
            	    			sDF.format(record.getTimeStamp()),
            	    			ClassAssignmentDetails.dispNumber(aSg.getValue()-bSg.getValue()),
            	    			classes.toString(),
            	    			dates.toString(),
            	    			times.toString(),
            	    			rooms.toString(),
            	    			ClassAssignmentDetails.dispNumber(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
            	                ClassAssignmentDetails.dispNumber(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())+sb,
            	                ClassAssignmentDetails.dispNumber(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()),
            	                ClassAssignmentDetails.dispNumber(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()),
            	                ClassAssignmentDetails.dispNumber(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()),
            	                ClassAssignmentDetails.dispNumber(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()),
            	                ClassAssignmentDetails.dispNumber(aSg.getUselessSlots()-bSg.getUselessSlots()),
            	                ClassAssignmentDetails.dispNumber(aSg.getTooBigRooms()-bSg.getTooBigRooms()),
            	                ClassAssignmentDetails.dispNumber(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()),
            	                ClassAssignmentDetails.dispNumber(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()),
            	                ClassAssignmentDetails.dispNumber(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty())
            	             },
            	             new Comparable[] {
        	             		record.getTimeStamp(),
            	                new Double(aSg.getValue()-bSg.getValue()),
            	                classes.toString(),
            	                datesSort.toString(),
            	                timesSort.toString(),
            	                roomsSort.toString(),
            	                new Integer(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
            	                new Long(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts()),
            	                new Double(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()),
            	                new Long(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()),
            	                new Long(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()),
            	                new Long(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()),
            	                new Long(aSg.getUselessSlots()-bSg.getUselessSlots()),
            	                new Long(aSg.getTooBigRooms()-bSg.getTooBigRooms()),
            	                new Double(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()),
            	                new Double(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()),
            	                new Double(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty())
            	             });
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable.printTable(WebTable.getOrder(context, "assignmentHistory.ord"));
    }	

    public PdfWebTable exportPdf(boolean simple, HttpServletRequest request, SessionContext context, SolverProxy solver, String name, Vector history) throws Exception {
    	if (history==null || history.isEmpty()) return null;
        PdfWebTable webTable =
        	(simple?
       			new PdfWebTable( 7,
       	        	name, null,
       				new String[] {"Time", "Score", "Class", "Date", "Time", "Room", "Students"},
       				new String[] {"left", "left", "left", "left", "left", "left", "left"},
       				null )
        	:	new PdfWebTable( 17,
        			name, null,
        			new String[] {"Time", "Score", "Class", "Date", "Time", "Room", "Conf","Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "left", "left","right","right","right","right","right","right","right","right","right","right","right"},
        			null ));

        for (Enumeration e=history.elements();e.hasMoreElements();) {
    		AssignmentRecord record = (AssignmentRecord)e.nextElement();
    		StringBuffer classes = new StringBuffer();
    	    StringBuffer rooms = new StringBuffer();
    	    StringBuffer times = new StringBuffer();
    	    StringBuffer roomsSort = new StringBuffer();
    	    StringBuffer timesSort = new StringBuffer();
    	    StringBuffer dates = new StringBuffer();
    	    StringBuffer datesSort = new StringBuffer();
    	    boolean first = true;
    	    for (Enumeration f=record.getAssignments().elements();f.hasMoreElements();) {
    	    	RecordedAssignment assignment = (RecordedAssignment)f.nextElement();
    	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context,solver,false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context,solver,false));
    	    	if (!first) {
    	    		roomsSort.append(":");
    	    		timesSort.append(":");
        	    	classes.append("\n");
        	        rooms.append("\n");
        	        times.append("\n");
        	        dates.append("\n");
    	    	}
    	        classes.append((before==null?after.getClazz().getName():before.getClazz().getName()));
    	        times.append(ClassAssignmentDetails.dispTimeNoHtml((before==null?null:before.getAssignedTime()),(after==null?null:after.getAssignedTime())));
    	        for (int i=0;i<(before==null?after.getAssignedRoom().length:before.getAssignedRoom().length);i++) {
    	        	if (i>0) rooms.append(", ");
    	        	rooms.append(ClassAssignmentDetails.dispRoomNoHtml((before==null?null:before.getAssignedRoom()[i]),(after==null?null:after.getAssignedRoom()[i])));
    	        }
    	        dates.append((before == null || before.getAssignedTime() == null ? "not-assigned" : before.getAssignedTime().getDatePatternName()) +
    	        		(after == null || after.getAssignedTime() == null ? " -> not-assigned" : before != null &&
    	        		before.getAssignedTime().getDatePatternName().equals(after.getAssignedTime().getDatePatternName()) ? "" : " -> " + after.getAssignedTime().getDatePatternName()));
    	        timesSort.append(before==null?after.getTimeName():before.getTimeName());
    	        roomsSort.append(before==null?after.getRoomName():before.getRoomName());
    	        datesSort.append(before==null?after.getDaysName():before.getDaysName());
    	        first = false;
    	    }
    	    if (first) continue;
    	    
    	    Suggestion bSg = record.getBefore();
    	    Suggestion aSg = record.getAfter();
    	    
    	    StringBuffer sb = new StringBuffer();
    	    if (aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("c",aSg.getCommitedStudentConflicts()-bSg.getCommitedStudentConflicts()));
    	    }
    	    if (aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("d",aSg.getDistanceStudentConflicts()-bSg.getDistanceStudentConflicts()));
    	    }
    	    if (aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("h",aSg.getHardStudentConflicts()-bSg.getHardStudentConflicts()));
    	    }
    	    if (sb.length()>0) sb.append(")");
    	    
    	    if (simple)
        	    webTable.addLine(null,
        	    		new String[] {
        	    			sDF.format(record.getTimeStamp()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getValue()-bSg.getValue()),
        	    			classes.toString(),
        	    			dates.toString(),
        	    			times.toString(),
        	    			rooms.toString(),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())+sb.toString()
        	    			},
        	             new Comparable[] {
        	             	record.getTimeStamp(),
        	                new Double(aSg.getValue()-bSg.getValue()),
        	                classes.toString(),
        	                datesSort.toString(),
        	                timesSort.toString(),
        	                roomsSort.toString(),
        	                new Long(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())
        	             });
    	    else
        	    webTable.addLine(null,
        	    		new String[] {
        	    			sDF.format(record.getTimeStamp()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getValue()-bSg.getValue()),
        	    			classes.toString(),
        	    			dates.toString(),
        	    			times.toString(),
        	    			rooms.toString(),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts())+sb,
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getUselessSlots()-bSg.getUselessSlots()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getTooBigRooms()-bSg.getTooBigRooms()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()),
        	    			ClassAssignmentDetails.dispNumberNoHtml(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty())
        	             },
        	             new Comparable[] {
    	             		record.getTimeStamp(),
        	                new Double(aSg.getValue()-bSg.getValue()),
        	                classes.toString(),
        	                datesSort.toString(),
        	                timesSort.toString(),
        	                roomsSort.toString(),
        	                new Integer(aSg.getUnassignedVariables()-bSg.getUnassignedVariables()),
        	                new Long(aSg.getViolatedStudentConflicts()-bSg.getViolatedStudentConflicts()),
        	                new Double(aSg.getGlobalTimePreference()-bSg.getGlobalTimePreference()),
        	                new Long(aSg.getGlobalRoomPreference()-bSg.getGlobalRoomPreference()),
        	                new Long(aSg.getGlobalGroupConstraintPreference()-bSg.getGlobalGroupConstraintPreference()),
        	                new Long(aSg.getInstructorDistancePreference()-bSg.getInstructorDistancePreference()),
        	                new Long(aSg.getUselessSlots()-bSg.getUselessSlots()),
        	                new Long(aSg.getTooBigRooms()-bSg.getTooBigRooms()),
        	                new Double(aSg.getDepartmentSpreadPenalty()-bSg.getDepartmentSpreadPenalty()),
        	                new Double(aSg.getSpreadPenalty()-bSg.getSpreadPenalty()),
        	                new Double(aSg.getPerturbationPenalty()-bSg.getPerturbationPenalty())
        	             });
    	}
        
        return webTable; 
    }

}

