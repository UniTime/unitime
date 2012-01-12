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

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Transaction;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.UnassignedForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.ui.SolutionUnassignedClassesModel;
import org.unitime.timetable.solver.ui.UnassignedClassRow;
import org.unitime.timetable.solver.ui.UnassignedClassesModel;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class UnassignedAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		UnassignedForm myForm = (UnassignedForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        try {
        	myForm.setSubjectAreas(new TreeSet(SubjectArea.getSubjectAreaList(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId())));
        } catch (Exception e) {}
        
        if ("Apply".equals(op) || "Export PDF".equals(op)) {
        	if (myForm.getSubjectArea() == null)
        		request.getSession().removeAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
        	else if (myForm.getSubjectArea() < 0)
        		request.getSession().setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, Constants.ALL_OPTION_VALUE);
        	else
        		request.getSession().setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, myForm.getSubjectArea().toString());
        } else {
        	try {
        		Object sa = request.getSession().getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME);
        		if (Constants.ALL_OPTION_VALUE.equals(sa))
        			myForm.setSubjectArea(-1l);
        		else if (sa != null)
        			myForm.setSubjectArea(Long.valueOf(sa.toString()));
        	} catch (Exception e) {}
        }
        if (myForm.getSubjectArea() == null && myForm.getSubjectAreas().size() == 1) {
        	myForm.setSubjectArea(((SubjectArea)myForm.getSubjectAreas().iterator().next()).getUniqueId());
        }
        
        if ("Export PDF".equals(op)) {
        	File f = exportPdf(request, myForm.getSubjectArea());
        	if (f!=null)
        		request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+f.getName());
        		//response.sendRedirect("temp/"+f.getName());
        }

        getUnassigned(request, myForm.getSubjectArea());
        return mapping.findForward("showUnassigned");
	}
	
    private void getUnassigned(HttpServletRequest request, Long subjectArea) throws Exception {
		Transaction tx = null;
		
		try {
			WebTable.setOrder(request.getSession(),"unassigned.ord",request.getParameter("ord"),1);
			
			UnassignedClassesModel model = null;
			boolean noSubject = false;
			String prefix = null;
	        if (subjectArea != null && subjectArea != 0) {
	        	prefix = (subjectArea < 0 ? null : new SubjectAreaDAO().get(subjectArea).getSubjectAreaAbbreviation() + " ");
			
				SolverProxy solver = WebSolver.getSolver(request.getSession());
				if (solver!=null) {
					model = solver.getUnassignedClassesModel(prefix);
				} else {
					SolutionDAO dao = new SolutionDAO();
					org.hibernate.Session hibSession = dao.getSession();
	            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	            		tx = hibSession.beginTransaction();

	            	String solutionIdsStr = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
					if (solutionIdsStr!=null) {
						Set solutions = new HashSet();
						for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
							Long solutionId = Long.valueOf(s.nextToken());
							Solution solution = dao.get(solutionId, hibSession);
							if (solution!=null)
								solutions.add(solution);
						}
						if (!solutions.isEmpty())
							model = new SolutionUnassignedClassesModel(solutions, hibSession, Settings.getSettingValue(Web.getUser(request.getSession()), Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT), prefix);
					}
					if (tx!=null) tx.commit();
				}
	        } else {
	        	noSubject = true;
	        }
			
			WebTable webTable = new WebTable( 4,
					(prefix == null ? "" : prefix) + "Not-assigned Classes", "unassigned.do?ord=%%",
					new String[] {"Class", "Instructor", "Students", "Initial Assignment"},
					new String[] {"left", "left", "left", "left"},
					null );
			if (noSubject) {
				webTable.addLine(null, new String[] {"<i>No subject area is selected.</i>"}, null, null );
			} else if (model==null) {
				webTable.addLine(null, new String[] {"<i>Neither a solver is started nor solution is selected.</i>"}, null, null );
			} else if (model.getNrRows()==0) {
				webTable.addLine(null, new String[] {"<i>All variables are assigned.</i>"}, null, null );
			} else {
				for (int i=0;i<model.getNrRows();i++) {
					UnassignedClassRow row = model.getRow(i);
					String onClick = row.getOnClick();
					webTable.addLine((onClick==null?"":"onclick=\""+row.getOnClick()+"\""), new String[] {
						row.getName(),
						row.getInstructor(),
						String.valueOf(row.getNrStudents()),
						row.getInitial()},
						new Comparable[] {
						row,
						row.getInstructor(),
						new Integer(row.getNrStudents()),
						row.getInitial()});
				}
			}
			
			request.setAttribute("Unassigned.table",webTable.printTable(WebTable.getOrder(request.getSession(),"unassigned.ord")));
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;

	    }
	}	

    private File exportPdf(HttpServletRequest request, Long subjectArea) throws Exception {
		UnassignedClassesModel model = null;
		boolean noSubject = false;
		String prefix = null;
        if (subjectArea != null && subjectArea != 0) {
        	prefix = (subjectArea < 0 ? null : new SubjectAreaDAO().get(subjectArea).getSubjectAreaAbbreviation() + " ");

        	SolverProxy solver = WebSolver.getSolver(request.getSession());
    		if (solver!=null) {
    			model = solver.getUnassignedClassesModel(prefix);
    		} else {
    			SolutionDAO dao = new SolutionDAO();
    			org.hibernate.Session hibSession = dao.getSession();

            	String solutionIdsStr = (String)request.getSession().getAttribute("Solver.selectedSolutionId");
    			if (solutionIdsStr!=null) {
    				Set solutions = new HashSet();
    				for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
    					Long solutionId = Long.valueOf(s.nextToken());
    					Solution solution = dao.get(solutionId, hibSession);
    					if (solution!=null)
    						solutions.add(solution);
    				}
    				if (!solutions.isEmpty())
    					model = new SolutionUnassignedClassesModel(solutions, hibSession, Settings.getSettingValue(Web.getUser(request.getSession()), Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT), prefix);
    			}
    		}
        } else {
        	noSubject = true;
        }
        
		PdfWebTable webTable = new PdfWebTable( 4,
				(prefix == null ? "" : prefix) + "Not-assigned Classes", null,
				new String[] {"Class", "Instructor", "Students", "Initial Assignment"},
				new String[] {"left", "left", "left", "left"},
				null );
		if (noSubject) {
			webTable.addLine(null, new String[] {"@@ITALIC No subject area is selected.", "", "", ""}, null, null );
		} else if (model==null) {
			webTable.addLine(null, new String[] {"@@ITALIC Neither a solver is started nor solution is selected.", "", "", ""}, null, null );
		} else if (model.getNrRows()==0) {
			webTable.addLine(null, new String[] {"@@ITALIC All variables are assigned.", "", "", ""}, null, null );
		} else {
			for (int i=0;i<model.getNrRows();i++) {
				UnassignedClassRow row = model.getRow(i);
				webTable.addLine(null, new String[] {
					row.getName(),
					row.getInstructor(),
					String.valueOf(row.getNrStudents()),
					row.getInitial()},
					new Comparable[] {
					row,
					row.getInstructor(),
					new Integer(row.getNrStudents()),
					row.getInitial()});
			}
		}
		
		File file = ApplicationProperties.getTempFile("unassigned", "pdf");
    	webTable.exportPdf(file, WebTable.getOrder(request.getSession(),"unassigned.ord"));
    	return file;
	}	

}

