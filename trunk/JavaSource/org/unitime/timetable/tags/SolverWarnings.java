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
package org.unitime.timetable.tags;

import java.util.Set;

import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public class SolverWarnings extends TagSupport {
	private static final long serialVersionUID = 7947787141769725429L;

    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }
	
	public String getSolverWarningCheckSolution() {
		if (!getSessionContext().hasPermission(Right.ClassAssignment)) return null; // no permission
		
		try {
			SolverProxy proxy = WebSolver.getSolver(pageContext.getSession());
			if (proxy!=null)
				return proxy.getProperties().getProperty("General.SolverWarnings");
		} catch (Exception e){}

		String id = (String)pageContext.getSession().getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			String warn = new String();
			for (String solutionId: id.split(",")) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
				for (SolverParameter p: solution.getParameters()) {
					if ("General.SolverWarnings".equals(p.getDefinition().getName())) {
						if (!warn.isEmpty()) warn += "<br>";
						warn += p.getValue();
					}
				}
 			}
			return warn.isEmpty() ? null : warn;
		}
		
		Set<SolverGroup> solverGroups = SolverGroup.getUserSolverGroups(getSessionContext().getUser());
		if (solverGroups.isEmpty()) return null; // no solver groups
		
		String warn = new String();
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
				warn += (dept ? " departmental" : "") + " classes are not considered.";
				nrWarns++;
				if (nrWarns >= 3) {
					warn += "<br>...";
					break;
				}
			}
		}
		
		return warn.isEmpty() ? null : warn;
	}

	public int doStartTag() {
		try {
			String warns = getSolverWarningCheckSolution();
			if (warns != null && !warns.isEmpty()) {
				pageContext.getOut().println("<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr>");
				pageContext.getOut().println("<td class=\"reqWarn\" width='5'>&nbsp;</td>");
				pageContext.getOut().println("<td class=\"reqWarn\" >");
				pageContext.getOut().println(warns);
				pageContext.getOut().println("</td></tr></table>");
				pageContext.getRequest().setAttribute(Constants.REQUEST_WARN, warns);
			}
			return SKIP_BODY;
		} catch (Exception e) {
			Debug.error(e);
			return SKIP_BODY;
		}
	}
	
	public int doEndTag() {
		return EVAL_PAGE;
	}


}
