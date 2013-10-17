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
package org.unitime.timetable.action;

import java.util.Enumeration;
import java.util.StringTokenizer;
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
import org.unitime.timetable.form.SolutionChangesForm;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/solutionChanges")
public class SolutionChangesAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolutionChangesForm myForm = (SolutionChangesForm) form;

        // Check Access
		sessionContext.checkPermission(Right.SolutionChanges);
        
        SuggestionsModel model = (SuggestionsModel)request.getSession().getAttribute("Suggestions.model");
        if (model==null) {
        	model = new SuggestionsModel();
        	model.load(sessionContext.getUser());
        	request.getSession().setAttribute("Suggestions.model", model);
        }

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Apply".equals(op) || "Export PDF".equals(op)) {
        	sessionContext.getUser().setProperty("SolutionChanges.reference", String.valueOf(myForm.getReferenceInt()));
        	myForm.save(model);
        	model.save(sessionContext.getUser());
        }
        if ("Refresh".equals(op) || op == null) {
        	myForm.reset(mapping, request);
        }
        
        myForm.load(model);
    	myForm.setReferenceInt(Integer.parseInt(sessionContext.getUser().getProperty("SolutionChanges.reference", String.valueOf(myForm.getReferenceInt()))));

        SolverProxy solver = courseTimetablingSolverService.getSolver();
        if (solver==null) {
        	request.setAttribute("SolutionChanges.message","No timetable is loaded. However, you can load one <a href='listSolutions.do'>here</a>.");
        	return mapping.findForward("showSolutionChanges");
        }
        
    	Vector changes = null;
    	if (myForm.getReferenceInt()==SolutionChangesForm.sReferenceBest) {
    		if (solver.bestSolutionInfo()==null) {
    			request.setAttribute("SolutionChanges.message","No best solution saved so far.");
    			return mapping.findForward("showSolutionChanges");
    		}
    		changes = solver.getChangesToBest();
    	} else if (myForm.getReferenceInt()==SolutionChangesForm.sReferenceInitial) {
    		changes = solver.getChangesToInitial();
    	} else if (myForm.getReferenceInt()==SolutionChangesForm.sReferenceSelected) {
    		String solutionIdsStr = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
    		if (solutionIdsStr==null || solutionIdsStr.length()==0) {
    			request.setAttribute("SolutionChanges.message","No solution selected. However, you can select one <a href='listSolutions.do'>here");
    			return mapping.findForward("showSolutionChanges");
    		} 
    		changes = new Vector();
			for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
				Long solutionId = Long.valueOf(s.nextToken());
				Vector ch = solver.getChangesToSolution(solutionId);
				if (ch!=null)
					changes.addAll(ch);
			}
    	}
    	String changeTable = getChangesTable(model.getSimpleMode(),model.getReversedMode(),request,sessionContext,courseTimetablingSolverService.getSolver(),"Changes",changes);
        if (changeTable!=null) {
        	request.setAttribute("SolutionChanges.table",changeTable);
        	request.setAttribute("SolutionChanges.table.colspan",new Integer(model.getSimpleMode()?5:14));
        } else
        	request.setAttribute("SolutionChanges.message","No changes."); 
        
        if ("Export PDF".equals(op)) {
        	PdfWebTable table = exportPdf(model.getSimpleMode(),model.getReversedMode(),sessionContext,courseTimetablingSolverService.getSolver(),"Changes",changes);
        	if (table != null) {
            	ExportUtils.exportPDF(
            			table,
            			WebTable.getOrder(sessionContext,"solutionChanges.ord"),
            			response, "changes");
            	return null;
        	}
        }

        return mapping.findForward("showSolutionChanges");
	}

    public String getChangesTable(boolean simple, boolean reversed, HttpServletRequest request, SessionContext context, SolverProxy solver, String name, Vector changes) {
    	if (changes==null || changes.isEmpty()) return null;
		WebTable.setOrder(context, "solutionChanges.ord",request.getParameter("ord"),1);
        WebTable webTable =
        	(simple?
       			new WebTable( 5,
       	        	name, "solutionChanges.do?ord=%%",
       				new String[] {"Class", "Date", "Time", "Room", "Students"},
       				new String[] {"left", "left", "left", "left", "right"},
       				null )
        	:	new WebTable( 14,
        			name, "solutionChanges.do?ord=%%",
        			new String[] {"Class", "Date", "Time", "Room", "Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "right","right","right","right","right","right","right","right","right","right","right"},
        			null ));
        webTable.setRowStyle("white-space:nowrap");
        try {
        	for (Enumeration e=changes.elements();e.hasMoreElements();) {
        		RecordedAssignment assignment = (RecordedAssignment)e.nextElement();
    	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
    	    	if (reversed) {
    	    		ClassAssignmentDetails x = after; after = before; before = x;
    	    	}
    	    	String className = (after==null?before.getClazz().toHtml(true,true):after.getClazz().toHtml(true,true));
    	    	ClassAssignmentDetails classSort = (after==null?before:after);
    	    	String time = ClassAssignmentDetails.dispTime2((before==null?null:before.getTime()),(after==null?null:after.getTime()));
        		String rooms = "";
        		String link = (before==null?null:"id="+before.getClazz().getClassId()+"&days="+before.getTime().getDays()+"&slot="+before.getTime().getStartSlot()
        				+"&pattern="+before.getTime().getPatternId()+"&dates="+before.getTime().getDatePatternId()+"&reset=1");
        		int nrRooms = Math.max(before == null || before.getRoom() == null ? 0 : before.getRoom().length, after == null || after.getRoom() == null ? 0 : after.getRoom().length);
        		for (int i=0;i<nrRooms;i++) {
    	        	if (i>0) rooms += ", ";
    	        	rooms += (ClassAssignmentDetails.dispRoom2(
    	        			(before==null || before.getRoom()==null || before.getRoom().length<=i ? null : before.getRoom()[i]),
    	        			(after==null || after.getRoom()==null || after.getRoom().length<=i ? null : after.getRoom()[i])));
    	        	if (before!=null && before.getRoom()!=null && before.getRoom().length>i)
    	        		link += "&room"+i+"="+before.getRoom()[i].getId();
    	        }
    	        String dates = (before == null ? "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>" : before.getDaysName()) +
    	        	(after == null ? " &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>" :
    	        		before != null && before.getDaysName().equals(after.getDaysName()) ? "" : " &rarr; " + after.getDaysName());
    	        String timesSort = (before==null?after.getTimeName():before.getTimeName());
    	        String roomsSort = (before==null?after.getRoomName():before.getRoomName());
    	        String datesSort = (before==null?after.getDaysName():before.getDaysName());
    	        
    	        AssignmentPreferenceInfo bInf = (before==null?null:before.getInfo());
    	        AssignmentPreferenceInfo aInf = (after==null?null:after.getInfo());
    	        if (aInf==null) aInf = new AssignmentPreferenceInfo();
    	        if (bInf==null) bInf = new AssignmentPreferenceInfo();

        	    StringBuffer sb = new StringBuffer();
        	    if (aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("c",aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()));
        	    }
        	    if (aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("d",bInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()));
        	    }
        	    if (aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("h",aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()));
        	    }
        	    if (sb.length()>0) sb.append(")");
        	    
        	    if (simple)
            	    webTable.addLine((link!=null?"onClick=\"showGwtDialog('Suggestions', 'suggestions.do?"+link+"&op=Try','900','90%');\"":null),
            	    		new String[] {
            	    			className,
            	    			dates,
            	    			time,
            	    			rooms,
            	    			ClassAssignmentDetails.dispNumber(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())+sb
            	    			},
            	             new Comparable[] {
            	    			classSort,
            	                datesSort,
            	                timesSort,
            	                roomsSort,
            	                new Long(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())
            	             });
        	    else
            	    webTable.addLine((link!=null?"onClick=\"showGwtDialog('Suggestions', 'suggestions.do?"+link+"&op=Try','900','90%');\"":null),
            	    		new String[] {
        	    				className,
        	    				dates,
        	    				time,
        	    				rooms,
            	    			ClassAssignmentDetails.dispNumber(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())+sb,
            	                ClassAssignmentDetails.dispNumber(aInf.getTimePreference()-bInf.getTimePreference()),
            	                ClassAssignmentDetails.dispNumber(aInf.sumRoomPreference()-bInf.sumRoomPreference()),
            	                ClassAssignmentDetails.dispNumber(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()),
            	                ClassAssignmentDetails.dispNumber(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()),
            	                ClassAssignmentDetails.dispNumber(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()),
            	                ClassAssignmentDetails.dispNumber(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()),
            	                ClassAssignmentDetails.dispNumber(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()),
            	                ClassAssignmentDetails.dispNumber(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()),
            	                ClassAssignmentDetails.dispNumber(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty())
            	             },
            	             new Comparable[] {
            	    			classSort,
        	             		datesSort,
        	                	timesSort,
        	                	roomsSort,
            	                new Long(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts()),
            	                new Double(aInf.getTimePreference()-bInf.getTimePreference()),
            	                new Long(aInf.sumRoomPreference()-bInf.sumRoomPreference()),
            	                new Long(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()),
            	                new Long(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()),
            	                new Long(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()),
            	                new Long(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()),
            	                new Double(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()),
            	                new Double(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()),
            	                new Double(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty())
            	             });
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable.printTable(WebTable.getOrder(context, "solutionChanges.ord"));
    }	

    public PdfWebTable exportPdf(boolean simple, boolean reversed, SessionContext context, SolverProxy solver, String name, Vector changes) throws Exception {
    	if (changes==null || changes.isEmpty()) return null;
        PdfWebTable webTable =
        	(simple?
       			new PdfWebTable( 5,
       	        	name, "solutionChanges.do?ord=%%",
       				new String[] {"Class", "Date", "Time", "Room", "Students"},
       				new String[] {"left", "left", "left", "left", "right"},
       				null )
        	:	new PdfWebTable( 14,
        			name, "solutionChanges.do?ord=%%",
        			new String[] {"Class", "Date", "Time", "Room", "Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "right","right","right","right","right","right","right","right","right","right","right"},
        			null ));
    	for (Enumeration e=changes.elements();e.hasMoreElements();) {
    		RecordedAssignment assignment = (RecordedAssignment)e.nextElement();
	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(context, solver, false));
	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(context, solver, false));
	    	if (reversed) {
	    		ClassAssignmentDetails x = after; after = before; before = x;
	    	}
	    	String className = (after==null?before.getClazz().getName():after.getClazz().getName());
	    	ClassAssignmentDetails classSort = (after==null?before:after);
	    	String time = ClassAssignmentDetails.dispTimeNoHtml((before==null?null:before.getTime()),(after==null?null:after.getTime()));
    		String rooms = "";
    		for (int i=0;i<(before==null?(after.getRoom()==null?0:after.getRoom().length):(before.getRoom()==null?0:before.getRoom().length));i++) {
	        	if (i>0) rooms += ", ";
	        	rooms += (ClassAssignmentDetails.dispRoomNoHtml((before==null?null:before.getRoom()[i]),(after==null?null:after.getRoom()[i])));
	        }
	        String dates = (before == null ? "not-assigned" : before.getDaysName()) + (after == null ? " -> not-assigned" : before != null && before.getDaysName().equals(after.getDaysName()) ? "" : " -> " + after.getDaysName());
	        String timesSort = (before==null?after.getTimeName():before.getTimeName());
	        String roomsSort = (before==null?after.getRoomName():before.getRoomName());
	        String datesSort = (before==null?after.getDaysName():before.getDaysName());
	        
	        AssignmentPreferenceInfo bInf = (before==null?null:before.getInfo());
	        AssignmentPreferenceInfo aInf = (after==null?null:after.getInfo());
	        if (aInf==null) aInf = new AssignmentPreferenceInfo();
	        if (bInf==null) bInf = new AssignmentPreferenceInfo();

    	    StringBuffer sb = new StringBuffer();
    	    if (aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("c",aInf.getNrCommitedStudentConflicts()-bInf.getNrCommitedStudentConflicts()));
    	    }
    	    if (aInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("d",bInf.getNrDistanceStudentConflicts()-bInf.getNrDistanceStudentConflicts()));
    	    }
    	    if (aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()!=0) {
    	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
    	    	sb.append(ClassAssignmentDetails.dispNumberNoHtml("h",aInf.getNrHardStudentConflicts()-bInf.getNrHardStudentConflicts()));
    	    }
    	    if (sb.length()>0) sb.append(")");
    	    
    	    if (simple)
        	    webTable.addLine(null,
        	    		new String[] {
        	    			className,
        	    			dates,
        	    			time,
        	    			rooms,
        	    			ClassAssignmentDetails.dispNumberNoHtml(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())+sb
        	    			},
        	             new Comparable[] {
        	    			classSort,
        	                datesSort,
        	                timesSort,
        	                roomsSort,
        	                new Long(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())
        	             });
    	    else
        	    webTable.addLine(null,
        	    		new String[] {
    	    				className,
    	    				dates,
    	    				time,
    	    				rooms,
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts())+sb,
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getTimePreference()-bInf.getTimePreference()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.sumRoomPreference()-bInf.sumRoomPreference()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()),
    	    				ClassAssignmentDetails.dispNumberNoHtml(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty())
        	             },
        	             new Comparable[] {
        	    			classSort,
    	             		datesSort,
    	                	timesSort,
    	                	roomsSort,
        	                new Long(aInf.getNrStudentConflicts()-bInf.getNrStudentConflicts()),
        	                new Double(aInf.getTimePreference()-bInf.getTimePreference()),
        	                new Long(aInf.sumRoomPreference()-bInf.sumRoomPreference()),
        	                new Long(aInf.getGroupConstraintPref()-bInf.getGroupConstraintPref()),
        	                new Long(aInf.getBtbInstructorPreference()-bInf.getBtbInstructorPreference()),
        	                new Long(aInf.getUselessHalfHours()-bInf.getUselessHalfHours()),
        	                new Long(aInf.getTooBigRoomPreference()-bInf.getTooBigRoomPreference()),
        	                new Double(aInf.getDeptBalancPenalty()-bInf.getDeptBalancPenalty()),
        	                new Double(aInf.getSpreadPenalty()-bInf.getSpreadPenalty()),
        	                new Double(aInf.getPerturbationPenalty()-bInf.getPerturbationPenalty())
        	             });
    	}
    	return webTable;
    }	
}

