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
package org.unitime.timetable.tags;

import java.io.IOException;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;


/**
 * @author Tomas Muller
 */
public class SolverWarnings extends BodyTagSupport {
	private static final long serialVersionUID = 7947787141769725429L;

    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }
	
	public String getSolverWarningCheckSolution() {
		if (!getSessionContext().hasPermission(Right.CourseTimetabling)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			SolverProxy proxy = WebSolver.getSolver(pageContext.getSession());
			if (proxy!=null)
				return proxy.getProperties().getProperty("General.SolverWarnings");
		} catch (Exception e){}

		// Return warning of the selected solution (warning that was present when the solution was saved, if there is a solution loaded in memory)
		String id = (String)pageContext.getSession().getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			String warn = "";
			for (String solutionId: id.split(",")) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
				for (SolverParameter p: solution.getParameters()) {
					if ("General.SolverWarnings".equals(p.getDefinition().getName()) && p.getValue() != null) {
						if (!warn.isEmpty()) warn += "<br>";
						warn += p.getValue();
					}
				}
 			}
			return warn.isEmpty() ? null : warn;
		}
		
		// Compute warning from solver groups
		Set<SolverGroup> solverGroups = SolverGroup.getUserSolverGroups(getSessionContext().getUser());
		if (solverGroups.isEmpty()) return null; // no solver groups
		
		String warn = "";
		int maxDistPriority = Integer.MIN_VALUE;
		int nrWarns = 0;
		for (SolverGroup sg: solverGroups)
			maxDistPriority = Math.max(maxDistPriority, sg.getMaxDistributionPriority());
		
		for (SolverGroup sg: SolverGroup.findBySessionId(getSessionContext().getUser().getCurrentAcademicSessionId())) {
			if (solverGroups.contains(sg)) continue;
			if (sg.getMinDistributionPriority() < maxDistPriority && sg.getCommittedSolution() == null) {
				if (nrWarns > 0) warn += "<br>";
				warn += "There is no "+sg.getAbbv()+" solution committed";
				boolean dept = false;
				for (Department d: sg.getDepartments()) {
					if (d.isExternalManager().booleanValue()) {
						warn += ", " + d.getExternalMgrAbbv();
					} else {
						dept = true;
						for (SubjectArea sa: d.getSubjectAreas())
							warn += ", " + sa.getSubjectAreaAbbreviation();
					}
				}
				warn += (dept ? ", departmental" : "") + " classes are not considered.";
				nrWarns++;
				if (nrWarns >= 3) {
					warn += "<br>...";
					break;
				}
			}
		}
		
		return warn.isEmpty() ? null : warn;
	}
	
	public String[] getCurrentAssignmentWarning() {
		if (!getSessionContext().hasPermission(Right.Timetables)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			SolverProxy proxy = WebSolver.getSolver(pageContext.getSession());
			if (proxy != null) {
				Long[] solverGroupId = proxy.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				String names = "";
				boolean interactive = proxy.getProperties().getPropertyBoolean("General.InteractiveMode", false);
				if (solverGroupId != null) {
					for (int i = 0; i < solverGroupId.length; i++) {
						SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId[i]);
						if (i > 0 && i + 1 == solverGroupId.length)
							names += (solverGroupId.length == 2 ? " and " : ", and ");
						else if (i > 0)
							names += ", ";
						names += (sg == null ? "N/A" : solverGroupId.length <= 3 ? sg.getName() : sg.getAbbv());
				   }
				}
				if (names == null || names.isEmpty()) names = "N/A";
				return new String[] {
						interactive ? "listSolutions.do" : "solver.do",
						"Showing an in-memory solution for " + names + "."
					};
			}
		} catch (Exception e) {}

		// Return warning of the selected solution (warning that was present when the solution was saved, if there is a solution loaded in memory)
		String id = (String)pageContext.getSession().getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			String names = "";
			String[] solutionIds = id.split(",");
			for (int i = 0; i < solutionIds.length; i++) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionIds[i]));
				if (i > 0 && i + 1 == solutionIds.length)
					names += (solutionIds.length == 2 ? " and " : ", and ");
				else if (i > 0)
					names += ", ";
				names += (solutionIds.length <= 3 ? solution.getOwner().getName() : solution.getOwner().getAbbv());
			}
			return new String[] {
					"listSolutions.do",
					names.isEmpty() ? null : solutionIds.length == 1 ? "Showing a selected solution for " + names + "." : "Showing selected solutions for " + names + "."
				};
		}

		return null;
	}
	
	public String[] getExamAssignmentWarning(boolean checkExamType) {
		if (!getSessionContext().hasPermission(Right.ExaminationSolver)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			ExamSolverProxy proxy = WebSolver.getExamSolver(pageContext.getSession());
			if (proxy != null) {
				ExamType type = ExamTypeDAO.getInstance().get(proxy.getExamTypeId());
				if (type != null) {
					if (checkExamType) {
						Long selectedExamTypeId = (Long)getSessionContext().getAttribute(SessionAttribute.ExamType);
						if (selectedExamTypeId != null && !selectedExamTypeId.equals(type.getUniqueId())) return null;
					}
					return new String[] {
						"examSolver.do",
						"Showing an in-memory solution for " + type.getLabel() + " Examinations."
					};
				}
			}
		} catch (Exception e) {}
		
		return null;
	}
	
	private void printWarning(String style, String message, String link) throws IOException {
		if (message != null && !message.isEmpty()) {
			pageContext.getOut().println("<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr>");
			pageContext.getOut().println("<td class=\"" + style + "\" style='padding-left:10px' " +
					(link == null ? "" : "onMouseOver=\"this.style.backgroundColor='#BBCDD0';\" onMouseOut=\"this.style.backgroundColor='#DFE7F2';\"") +
					">");
			if (link != null)
				pageContext.getOut().print("<a class='noFancyLinks' href=\"" + link + "\">");
			pageContext.getOut().println(message);
			if (link != null)
				pageContext.getOut().print("</a>");
			pageContext.getOut().println("</td></tr></table>");
		}
	}
	
	@Override
    public int doStartTag() {
        return EVAL_BODY_BUFFERED;
    }

	@Override
	public int doEndTag() throws JspException {
		try {
			String body = (getBodyContent() == null ? null : getBodyContent().getString().trim());
			if (body == null || body.isEmpty()) return EVAL_PAGE;
			
			if ("solver".equals(body)) {
				
				String warn = getSolverWarningCheckSolution();
				if (warn != null)
					printWarning("unitime-MessageYellow", warn, null);
				
				String[] awarn = getCurrentAssignmentWarning();
				if (awarn != null)
					printWarning("unitime-MessageBlue", awarn[1], awarn[0]);
				
			} else if ("assignment".equals(body)) {
				
				String[] awarn = getCurrentAssignmentWarning();
				if (awarn != null) {
					printWarning("unitime-MessageBlue", awarn[1], awarn[0]);
				}
				
				String[] xwarn = getExamAssignmentWarning(false);
				if (xwarn != null)
					printWarning("unitime-MessageBlue", xwarn[1], xwarn[0]);
				
			} else if ("exams".equals(body)) {
				
				String[] awarn = getExamAssignmentWarning(true);
				if (awarn != null)
					printWarning("unitime-MessageBlue", awarn[1], awarn[0]);
				
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}
		return EVAL_PAGE;
	}
}
