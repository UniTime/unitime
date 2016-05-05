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
package org.unitime.timetable.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.resources.GwtMessages;
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
import org.unitime.timetable.server.solver.SolverPageBackend;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;


/**
 * @author Tomas Muller
 */
public class SolverWarnings extends BodyTagSupport {
	private static final long serialVersionUID = 7947787141769725429L;
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

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
				List<String> subjects = new ArrayList<String>();
				boolean dept = false;
				for (Department d: sg.getDepartments()) {
					if (d.isExternalManager().booleanValue()) {
						subjects.add(d.getExternalMgrAbbv());
					} else {
						dept = true;
						for (SubjectArea sa: d.getSubjectAreas())
							subjects.add(sa.getSubjectAreaAbbreviation());
					}
				}
				if (dept)
					warn += MESSAGES.warnSolverNoCommittedSolutionDepartmental(sg.getAbbv(), SolverPageBackend.toString(subjects));
				else
					warn += MESSAGES.warnSolverNoCommittedSolutionExternal(sg.getAbbv(), SolverPageBackend.toString(subjects));
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
				List<String> names = new ArrayList<String>();
				boolean interactive = proxy.getProperties().getPropertyBoolean("General.InteractiveMode", false);
				if (solverGroupId != null) {
					for (int i = 0; i < solverGroupId.length; i++) {
						SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId[i]);
						names.add(sg == null ? MESSAGES.notApplicable() : solverGroupId.length <= 3 ? sg.getName() : sg.getAbbv());
				   }
				}
				if (names == null || names.isEmpty()) names.add(MESSAGES.notApplicable());
				return new String[] {
						interactive ? "listSolutions.do" : ApplicationProperty.LegacySolver.isTrue() ? "solver.do" : "gwt.jsp?page=solver&type=course",
						MESSAGES.infoSolverShowingSolution(SolverPageBackend.toString(names))
					};
			}
		} catch (Exception e) {}

		// Return warning of the selected solution (warning that was present when the solution was saved, if there is a solution loaded in memory)
		String id = (String)pageContext.getSession().getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			List<String> names = new ArrayList<String>();
			String[] solutionIds = id.split(",");
			for (int i = 0; i < solutionIds.length; i++) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionIds[i]));
				names.add(solutionIds.length <= 3 ? solution.getOwner().getName() : solution.getOwner().getAbbv());
			}
			return new String[] {
					"listSolutions.do",
					names.isEmpty() ? null : names.size() == 1 ? MESSAGES.infoSolverShowingSelectedSolution(names.get(0)) : MESSAGES.infoSolverShowingSelectedSolutions(SolverPageBackend.toString(names))
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
						ApplicationProperty.LegacySolver.isTrue() ? "examSolver.do" : "gwt.jsp?page=solver&type=exam",
						MESSAGES.infoExamSolverShowingSolution(type.getLabel())
					};
				}
			}
		} catch (Exception e) {}
		
		return null;
	}
	
	private void printWarning(String style, String message, String link) throws IOException {
		if (message != null && !message.isEmpty()) {
			pageContext.getOut().println("<div class=\"" + style + "\"" + 
					(link == null ? "" : "onMouseOver=\"this.style.backgroundColor='#BBCDD0';\" onMouseOut=\"this.style.backgroundColor='#DFE7F2';\"") +
					">");
			if (link != null)
				pageContext.getOut().print("<a class='noFancyLinks' href=\"" + link + "\">");
			pageContext.getOut().println(message);
			if (link != null)
				pageContext.getOut().print("</a>");
			pageContext.getOut().println("</div>");
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
					printWarning("unitime-PageWarn", warn, null);
				
				String[] awarn = getCurrentAssignmentWarning();
				if (awarn != null)
					printWarning("unitime-PageMessage", awarn[1], awarn[0]);
				
			} else if ("assignment".equals(body)) {
				
				String[] awarn = getCurrentAssignmentWarning();
				if (awarn != null) {
					printWarning("unitime-PageMessage", awarn[1], awarn[0]);
				}
				
				String[] xwarn = getExamAssignmentWarning(false);
				if (xwarn != null)
					printWarning("unitime-PageMessage", xwarn[1], xwarn[0]);
				
			} else if ("exams".equals(body)) {
				
				String[] awarn = getExamAssignmentWarning(true);
				if (awarn != null)
					printWarning("unitime-PageMessage", awarn[1], awarn[0]);
				
			}
			
		} catch (Exception e) {
			Debug.error(e);
		}
		return EVAL_PAGE;
	}
}
