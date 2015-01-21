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

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.AssignedClassesForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/assignedClasses")
public class AssignedClassesAction extends Action {
	public static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		AssignedClassesForm myForm = (AssignedClassesForm) form;

        // Check Access
		sessionContext.checkPermission(Right.AssignedClasses);
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        SuggestionsModel model = (SuggestionsModel)request.getSession().getAttribute("Suggestions.model");
        if (model==null) {
        	model = new SuggestionsModel();
        	model.load(sessionContext.getUser());
        	request.getSession().setAttribute("Suggestions.model", model);
        }

        if ("Apply".equals(op) || "Export PDF".equals(op) || "Export CSV".equals(op)) {
        	myForm.save(model);
        	model.save(sessionContext.getUser());
        }
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping, request);
        }
        
        myForm.load(model);
        try {
        	myForm.setSubjectAreas(new TreeSet(SubjectArea.getSubjectAreaList(sessionContext.getUser().getCurrentAcademicSessionId())));
        } catch (Exception e) {}
        
        if ("Apply".equals(op) || "Export PDF".equals(op) || "Export CSV".equals(op)) {
        	if (myForm.getSubjectArea() == null)
        		sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
        	else if (myForm.getSubjectArea() < 0)
        		sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, Constants.ALL_OPTION_VALUE);
        	else
        		sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, myForm.getSubjectArea().toString());
        } else {
        	try {
        		String sa = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        		if (Constants.ALL_OPTION_VALUE.equals(sa))
        			myForm.setSubjectArea(-1l);
        		else if (sa != null) {
        			if (sa.indexOf(',') >= 0) sa = sa.substring(0, sa.indexOf(','));
        			myForm.setSubjectArea(Long.valueOf(sa));
        		}
        	} catch (Exception e) {}
        }
        if (myForm.getSubjectArea() == null && myForm.getSubjectAreas().size() == 1) {
        	myForm.setSubjectArea(((SubjectArea)myForm.getSubjectAreas().iterator().next()).getUniqueId());
        }
        
        Vector assignedClasses = null;
        if (myForm.getSubjectArea() != null && myForm.getSubjectArea() != 0) {
        	String prefix = myForm.getSubjectArea() > 0 ? myForm.getSubjectAreaAbbv() + " " : null;
            SolverProxy solver = courseTimetablingSolverService.getSolver();
            if (solver!=null) {
            	assignedClasses = solver.getAssignedClasses(prefix);
            } else {
            	String instructorNameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
            	String solutionIdsStr = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
            	assignedClasses = new Vector();
    			if (solutionIdsStr!=null && solutionIdsStr.length()>0) {
    				SolutionDAO dao = new SolutionDAO();
    				org.hibernate.Session hibSession = dao.getSession();
    				for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
    					Long solutionId = Long.valueOf(s.nextToken());
    					Solution solution = dao.get(solutionId, hibSession);
    					try {
    						for (Iterator i=solution.getAssignments().iterator();i.hasNext();) {
    							Assignment a = (Assignment)i.next();
    							if (prefix != null && !a.getClassName().startsWith(prefix)) continue;
    							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
    						}
    					} catch (ObjectNotFoundException e) {
    						hibSession.refresh(solution);
    						for (Iterator i=solution.getAssignments().iterator();i.hasNext();) {
    							Assignment a = (Assignment)i.next();
    							assignedClasses.add(new ClassAssignmentDetails(solution, a, false, hibSession, instructorNameFormat));
    						}
    					}
    				}
    			} else {
        			request.setAttribute("AssignedClasses.message","Neither a solver is started nor solution is selected.");
        			return mapping.findForward("showAssignedClasses");
    			}
            }
        } else {
			request.setAttribute("AssignedClasses.message","No subject area is selected.");
			return mapping.findForward("showAssignedClasses");
        }
        	

        
        String assignedTable = getAssignmentTable(model.getSimpleMode(),request,"Assigned Classes",assignedClasses);
        if (assignedTable!=null) {
        	request.setAttribute("AssignedClasses.table",assignedTable);
        	request.setAttribute("AssignedClasses.table.colspan",new Integer(model.getSimpleMode()?6:15));
        } else
        	request.setAttribute("AssignedClasses.message","No assigned class.");
        
        if ("Export PDF".equals(op)) {
        	PdfWebTable table = exportPdf(model.getSimpleMode(),request,"Assigned Classes",assignedClasses);
        	if (table != null) {
        		ExportUtils.exportPDF(table, WebTable.getOrder(sessionContext,"assignedClasses.ord"), response, "assigned");
        		return null;
        	}
        }
        if ("Export CSV".equals(op)) {
        	PdfWebTable table = exportPdf(model.getSimpleMode(),request,"Assigned Classes",assignedClasses);
        	if (table != null) {
        		ExportUtils.exportCSV(table, WebTable.getOrder(sessionContext,"assignedClasses.ord"), response, "assigned");
        		return null;
        	}
        }
		
        return mapping.findForward("showAssignedClasses");
	}
	
	private static String dispNumberPdf(int number) {
		return dispNumberPdf("", number);
	}
	
	private static String dispNumberPdf(String prefix, int number) {
		if (number>0) return "@@COLOR FF0000 " + prefix + "+" + number + " @@END_COLOR ";
	    if (number<0) return "@@COLOR 00FF00 " + prefix + number  + " @@END_COLOR ";
	    return prefix + "0";
	}
	
	private static String dispNumberPdf(double number) {
		return dispNumberPdf("", number);
	}
	
	private static String dispNumberPdf(String prefix, double number) {
		if (number>0) return "@@COLOR FF0000 " + prefix + "+" + sDF.format(number) + " @@END_COLOR ";
	    if (number<0) return "@@COLOR 00FF00 " + prefix + sDF.format(number)  + " @@END_COLOR ";
	    return prefix + sDF.format(0.0);
	}
	
	public PdfWebTable exportPdf(boolean simple, HttpServletRequest request, String name, Vector assignedClasses) {
    	if (assignedClasses==null || assignedClasses.isEmpty()) return null;
        PdfWebTable webTable =
        	(simple?
       			new PdfWebTable( 6,
       	        	name, null,
       				new String[] {"Class", "Date", "Time", "Room", "Instructor", "Students"},
       				new String[] {"left", "left", "left", "left", "left", "left"},
       				null )
        	:	new PdfWebTable( 15,
        			name, null,
        			new String[] {"Class", "Date", "Time", "Room", "Instructor", "Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "left","right","right","right","right","right","right","right","right","right","right"},
        			null ));
        
        try {
        	for (Enumeration e=assignedClasses.elements();e.hasMoreElements();) {
        		ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        		AssignmentPreferenceInfo ci = ca.getInfo(); 

        		StringBuffer sb = new StringBuffer();
        	    if (ci.getNrCommitedStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(dispNumberPdf("c",ci.getNrCommitedStudentConflicts()));
        	    }
        	    if (ci.getNrDistanceStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(dispNumberPdf("d",ci.getNrDistanceStudentConflicts()));
        	    }
        	    if (ci.getNrHardStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(dispNumberPdf("h",ci.getNrHardStudentConflicts()));
        	    }
        	    if (sb.length()>0) sb.append(")");
        	    
        	    String rooms = "";
        	    if (ca.getAssignedRoom() != null) {
        	    	for (int i=0;i<ca.getAssignedRoom().length;i++) {
        	    		if (i>0) rooms += "@@COLOR 000000 , ";
        	    		rooms += "@@COLOR " + PreferenceLevel.int2color(ca.getAssignedRoom()[i].getPref()) + " " + ca.getAssignedRoom()[i].getName();
        	    	}
        	    } else if (ca.getRoom()!=null) {
        	    	for (int i=0;i<ca.getRoom().length;i++) {
        	    		if (i>0) rooms += "@@COLOR 000000 , ";
        	    		rooms += "@@COLOR " + PreferenceLevel.int2color(ca.getRoom()[i].getPref()) + " " + ca.getRoom()[i].getName();
        	    	}
        	    }
        	    
        	    if (simple)
            	    webTable.addLine(null,
            	    		new String[] {
            	    			"@@COLOR " + PreferenceLevel.prolog2color(ca.getClazz().getPref()) + " " + ca.getClazz().getName(),
            	    			(ca.getTime()==null?ca.getAssignedTime()==null?"":
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getAssignedTime().getDatePatternPreference()) + " " + ca.getAssignedTime().getDatePatternName():
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getTime().getDatePatternPreference()) + " " +	ca.getTime().getDatePatternName()),
            	    			(ca.getTime()==null?ca.getAssignedTime()==null?"":
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getAssignedTime().getPref()) + " " + ca.getAssignedTime().getDaysName()+" "+ca.getTime().getStartTime()+" - "+ca.getTime().getEndTime():
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getTime().getPref()) + " " +	ca.getTime().getDaysName()+" "+ca.getTime().getStartTime()+" - "+ca.getTime().getEndTime()),
            	    			rooms,
            	    			ca.getInstructorName(),
            	    			dispNumberPdf(ci.getNrStudentConflicts())+sb
            	    			},
            	             new Comparable[] {
            	             	ca,
            	                ca.getDaysName(),
            	                ca.getTimeName(),
            	                ca.getRoomName(),
            	                ca.getInstructorName(),
            	                new Long(ci.getNrStudentConflicts())
            	             });
        	    else
            	    webTable.addLine("onClick=\"showGwtDialog('Suggestions', 'suggestions.do?id="+ca.getClazz().getClassId()+"&op=Reset','900','90%');\"",
            	    		new String[] {
            	    			"@@COLOR " + PreferenceLevel.prolog2color(ca.getClazz().getPref()) + " " + ca.getClazz().getName(),
            	    			(ca.getTime()==null?ca.getAssignedTime()==null?"":
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getAssignedTime().getDatePatternPreference()) + " " + ca.getAssignedTime().getDatePatternName():
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getTime().getDatePatternPreference()) + " " +	ca.getTime().getDatePatternName()),
            	    			(ca.getTime()==null?ca.getAssignedTime()==null?"":
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getAssignedTime().getPref()) + " " + ca.getAssignedTime().getDaysName()+" "+ca.getTime().getStartTime()+" - "+ca.getTime().getEndTime():
            	    				"@@COLOR " + PreferenceLevel.int2color(ca.getTime().getPref()) + " " +	ca.getTime().getDaysName()+" "+ca.getTime().getStartTime()+" - "+ca.getTime().getEndTime()),
    	    					rooms,
    	    					ca.getInstructorName(),
    	    					dispNumberPdf(ci.getNrStudentConflicts())+sb,
    	    					dispNumberPdf(ci.getTimePreference()),
    	    					dispNumberPdf(ci.sumRoomPreference()),
    	    					dispNumberPdf(ci.getGroupConstraintPref()),
    	    					dispNumberPdf(ci.getBtbInstructorPreference()),
    	    					dispNumberPdf(ci.getUselessHalfHours()),
    	    					dispNumberPdf(ci.getTooBigRoomPreference()),
    	    					dispNumberPdf(ci.getDeptBalancPenalty()),
    	    					dispNumberPdf(ci.getSpreadPenalty()),
    	    					dispNumberPdf(ci.getPerturbationPenalty())
            	             },
            	             new Comparable[] {
    	             			ca,
    	             			ca.getDaysName(),
    	                		ca.getTimeName(),
    	                		ca.getRoomName(),
    	                		ca.getInstructorName(),
    	                		new Long(ci.getNrStudentConflicts()),
            	                new Double(ci.getTimePreference()),
            	                new Long(ci.sumRoomPreference()),
            	                new Long(ci.getGroupConstraintPref()),
            	                new Long(ci.getBtbInstructorPreference()),
            	                new Long(ci.getUselessHalfHours()),
            	                new Long(ci.getTooBigRoomPreference()),
            	                new Double(ci.getDeptBalancPenalty()),
            	                new Double(ci.getSpreadPenalty()),
            	                new Double(ci.getPerturbationPenalty())
            	             });
        	}
        	return webTable;
        } catch (Exception e) {
        	Debug.error(e);
        }
        return null;
	}

    public String getAssignmentTable(boolean simple, HttpServletRequest request, String name, Vector assignedClasses) {
    	if (assignedClasses==null || assignedClasses.isEmpty()) return null;
		WebTable.setOrder(sessionContext,"assignedClasses.ord",request.getParameter("ord"),1);
        WebTable webTable =
        	(simple?
       			new WebTable( 6,
       	        	name, "assignedClasses.do?ord=%%",
       				new String[] {"Class", "Date", "Time", "Room", "Instructor", "Students"},
       				new String[] {"left", "left", "left", "left", "left", "left"},
       				null )
        	:	new WebTable( 15,
        			name, "assignedClasses.do?ord=%%",
        			new String[] {"Class", "Date", "Time", "Room", "Instructor", "Std","Tm","Rm","Gr","Ins","Usl","Big","Dept","Subp","Pert"},
        			new String[] {"left", "left", "left", "left", "left","right","right","right","right","right","right","right","right","right","right"},
        			null ));
        webTable.setRowStyle("white-space:nowrap");
        try {
        	for (Enumeration e=assignedClasses.elements();e.hasMoreElements();) {
        		ClassAssignmentDetails ca = (ClassAssignmentDetails)e.nextElement();
        		AssignmentPreferenceInfo ci = ca.getInfo(); 

        		StringBuffer sb = new StringBuffer();
        	    if (ci.getNrCommitedStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("c",ci.getNrCommitedStudentConflicts()));
        	    }
        	    if (ci.getNrDistanceStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("d",ci.getNrDistanceStudentConflicts()));
        	    }
        	    if (ci.getNrHardStudentConflicts()!=0) {
        	    	if (sb.length()==0) sb.append(" ("); else sb.append(",");
        	    	sb.append(ClassAssignmentDetails.dispNumber("h",ci.getNrHardStudentConflicts()));
        	    }
        	    if (sb.length()>0) sb.append(")");
        	    
        	    String rooms = "";
        	    if (ca.getAssignedRoom() != null) {
        	    	for (int i=0;i<ca.getAssignedRoom().length;i++) {
        	    		if (i>0) rooms += ", ";
        	    		rooms += ca.getAssignedRoom()[i].toHtml(false,false,true);
        	    	}
        	    } else if (ca.getRoom()!=null) {
        	    	for (int i=0;i<ca.getRoom().length;i++) {
        	    		if (i>0) rooms += ", ";
        	    		rooms += ca.getRoom()[i].toHtml(false,false,true);
        	    	}
        	    }
        	    
        	    if (simple)
            	    webTable.addLine("onClick=\"showGwtDialog('Suggestions', 'suggestions.do?id="+ca.getClazz().getClassId()+"&op=Reset','900','90%');\"",
            	    		new String[] {
            	    			ca.getClazz().toHtml(true,true),
            	    			ca.getDaysHtml(),
            	    			ca.getTime().toHtml(false,false,true,true),
            	    			rooms,
            	    			ca.getInstructorName(),
            	    			ClassAssignmentDetails.dispNumber(ci.getNrStudentConflicts())+sb
            	    			},
            	             new Comparable[] {
            	             	ca,
            	                ca.getDaysName(),
            	                ca.getTimeName(),
            	                ca.getRoomName(),
            	                ca.getInstructorName(),
            	                new Long(ci.getNrStudentConflicts())
            	             });
        	    else
            	    webTable.addLine("onClick=\"showGwtDialog('Suggestions', 'suggestions.do?id="+ca.getClazz().getClassId()+"&op=Reset','900','90%');\"",
            	    		new String[] {
    	    					ca.getClazz().toHtml(true,true),
    	    					ca.getDaysHtml(),
    	    					ca.getTime().toHtml(false,false,true,true),
    	    					rooms,
    	    					ca.getInstructorName(),
    	    					ClassAssignmentDetails.dispNumber(ci.getNrStudentConflicts())+sb,
            	                ClassAssignmentDetails.dispNumber(ci.getTimePreference()),
            	                ClassAssignmentDetails.dispNumber(ci.sumRoomPreference()),
            	                ClassAssignmentDetails.dispNumber(ci.getGroupConstraintPref()),
            	                ClassAssignmentDetails.dispNumber(ci.getBtbInstructorPreference()),
            	                ClassAssignmentDetails.dispNumber(ci.getUselessHalfHours()),
            	                ClassAssignmentDetails.dispNumber(ci.getTooBigRoomPreference()),
            	                ClassAssignmentDetails.dispNumber(ci.getDeptBalancPenalty()),
            	                ClassAssignmentDetails.dispNumber(ci.getSpreadPenalty()),
            	                ClassAssignmentDetails.dispNumber(ci.getPerturbationPenalty())
            	             },
            	             new Comparable[] {
    	             			ca,
    	             			ca.getDaysName(),
    	                		ca.getTimeName(),
    	                		ca.getRoomName(),
    	                		ca.getInstructorName(),
    	                		new Long(ci.getNrStudentConflicts()),
            	                new Double(ci.getTimePreference()),
            	                new Long(ci.sumRoomPreference()),
            	                new Long(ci.getGroupConstraintPref()),
            	                new Long(ci.getBtbInstructorPreference()),
            	                new Long(ci.getUselessHalfHours()),
            	                new Long(ci.getTooBigRoomPreference()),
            	                new Double(ci.getDeptBalancPenalty()),
            	                new Double(ci.getSpreadPenalty()),
            	                new Double(ci.getPerturbationPenalty())
            	             });
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	webTable.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return webTable.printTable(WebTable.getOrder(sessionContext,"assignedClasses.ord"));
    }	
}

