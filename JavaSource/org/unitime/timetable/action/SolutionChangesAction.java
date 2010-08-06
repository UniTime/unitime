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

import java.io.File;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.SolutionChangesForm;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class SolutionChangesAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolutionChangesForm myForm = (SolutionChangesForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        SuggestionsModel model = (SuggestionsModel)request.getSession().getAttribute("Suggestions.model");
        if (model==null) {
        	model = new SuggestionsModel();
        	model.load(request.getSession());
        	request.getSession().setAttribute("Suggestions.model", model);
        }

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Apply".equals(op) || "Export PDF".equals(op)) {
        	UserData.setPropertyInt(request.getSession(),"SolutionChanges.reference",myForm.getReferenceInt());
        	myForm.save(model);
        	model.save(request.getSession());
        }
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping, request);
        }
        
        myForm.load(model);

        SolverProxy solver = WebSolver.getSolver(request.getSession());
        if (solver==null) {
        	request.setAttribute("SolutionChanges.message","No timetable is loaded. However, you can load one <a target='__idContentFrame' href='listSolutions.do' onclick='window.close();'>here</a>.");
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
    			request.setAttribute("SolutionChanges.message","No solution selected. However, you can select one <a target='__idContentFrame' href='listSolutions.do' onclick='window.close();'>here");
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
    	String changeTable = getChangesTable(model.getSimpleMode(),model.getReversedMode(),request,"Changes",changes);
        if (changeTable!=null) {
        	request.setAttribute("SolutionChanges.table",changeTable);
        	request.setAttribute("SolutionChanges.table.colspan",new Integer(model.getSimpleMode()?5:14));
        } else
        	request.setAttribute("SolutionChanges.message","No changes."); 
        
        if ("Export PDF".equals(op)) {
        	File f = exportPdf(model.getSimpleMode(),model.getReversedMode(),request,"Changes",changes);
        	if (f!=null)
        		request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+f.getName());
        		//response.sendRedirect("temp/"+f.getName());
        }

        return mapping.findForward("showSolutionChanges");
	}

    public String getChangesTable(boolean simple, boolean reversed, HttpServletRequest request, String name, Vector changes) {
    	if (changes==null || changes.isEmpty()) return null;
		WebTable.setOrder(request.getSession(),"solutionChanges.ord",request.getParameter("ord"),1);
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
        	int idx = 0;
        	for (Enumeration e=changes.elements();e.hasMoreElements();idx++) {
        		RecordedAssignment assignment = (RecordedAssignment)e.nextElement();
    	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(request.getSession(),false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(request.getSession(),false));
    	    	if (reversed) {
    	    		ClassAssignmentDetails x = after; after = before; before = x;
    	    	}
    	    	String className = (after==null?before.getClazz().toHtml(true,true):after.getClazz().toHtml(true,true));
    	    	ClassAssignmentDetails classSort = (after==null?before:after);
    	    	String time = ClassAssignmentDetails.dispTime2((before==null?null:before.getTime()),(after==null?null:after.getTime()));
        		String rooms = "";
        		String link = (before==null?null:"id="+before.getClazz().getClassId()+"&days="+before.getTime().getDays()+"&slot="+before.getTime().getStartSlot()+"&pattern="+before.getTime().getPatternId());
        		for (int i=0;i<(before==null?(after.getRoom()==null?0:after.getRoom().length):(before.getRoom()==null?0:before.getRoom().length));i++) {
    	        	if (i>0) rooms += ", ";
    	        	rooms += (ClassAssignmentDetails.dispRoom2((before==null || before.getRoom().length<=i?null:before.getRoom()[i]),(after==null  || after.getRoom().length<=i?null:after.getRoom()[i])));
    	        	if (before!=null)
    	        		link += "&room"+i+"="+before.getRoom()[i].getId();
    	        }
    	        String dates = (before==null?after.getDaysHtml():before.getDaysHtml());
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
            	    webTable.addLine((link!=null?"onClick=\"window.open('suggestions.do?"+link+"&op=Try','suggestions','width=1024,height=768,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no').focus();\"":null),
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
            	    webTable.addLine((link!=null?"onClick=\"window.open('suggestions.do?"+link+"&op=Try','suggestions','width=1024,height=768,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no').focus();\"":null),
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
        return webTable.printTable(WebTable.getOrder(request.getSession(),"solutionChanges.ord"));
    }	

    public File exportPdf(boolean simple, boolean reversed, HttpServletRequest request, String name, Vector changes) {
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
        try {
        	int idx = 0;
        	for (Enumeration e=changes.elements();e.hasMoreElements();idx++) {
        		RecordedAssignment assignment = (RecordedAssignment)e.nextElement();
    	    	ClassAssignmentDetails before = (assignment.getBefore()==null?null:assignment.getBefore().getDetails(request.getSession(),false));
    	    	ClassAssignmentDetails after = (assignment.getAfter()==null?null:assignment.getAfter().getDetails(request.getSession(),false));
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
    	        String dates = (before==null?after.getDaysName():before.getDaysName());
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
        	File file = ApplicationProperties.getTempFile("changes", "pdf");
        	webTable.exportPdf(file, WebTable.getOrder(request.getSession(),"solutionChanges.ord"));
        	return file;
        } catch (Exception e) {
        	Debug.error(e);
        }
        return null;
    }	
}

