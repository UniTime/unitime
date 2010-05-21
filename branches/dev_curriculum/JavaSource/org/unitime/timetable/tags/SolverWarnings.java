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
package org.unitime.timetable.tags;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.TagSupport;

import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;


/**
 * @author Tomas Muller
 */
public class SolverWarnings extends TagSupport {
	
	public static String getSolverWarning(HttpSession session, Long[] solverGroupIds) {
		try {
			User user = Web.getUser(session);
			if (user==null) return null;
			if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return null;
			if (user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) return null;
			TimetableManager manager = TimetableManager.getManager(user);
			if (manager==null) return null;
			Session acadSession = Session.getCurrentAcadSession(user);
			if (acadSession==null) return null;
			
			if (solverGroupIds==null || solverGroupIds.length==0) {
				if (user.isAdmin()) return null;
				return getSolverWarning(acadSession, manager.getSolverGroups());
			} else {
				Vector solverGroups = new Vector(solverGroupIds.length);
				for (int i=0;i<solverGroupIds.length;i++)
					solverGroups.add((new SolverGroupDAO()).get(solverGroupIds[i]));
				return getSolverWarning(acadSession,solverGroups);
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getSolverWarning(Session session, Collection solverGroups) {
		StringBuffer warn = new StringBuffer();
		int maxDistPriority = Integer.MIN_VALUE;
		int nrWarns = 0;
		boolean noSolverGroup = true;
		for (Iterator i=solverGroups.iterator();i.hasNext();) {
			SolverGroup sg = (SolverGroup)i.next();
			if (!sg.getSession().equals(session)) continue;
			noSolverGroup = false;
			maxDistPriority = Math.max(maxDistPriority, sg.getMaxDistributionPriority());
		}
		if (noSolverGroup) {
			nrWarns++;
			warn.append("No solver group associated with the user.");
		} else {
			for (Iterator i=SolverGroup.findBySessionId(session.getUniqueId()).iterator();i.hasNext();) {
				SolverGroup sg = (SolverGroup) i.next();
				if (solverGroups.contains(sg)) continue;
				if (sg.getMinDistributionPriority()<maxDistPriority && sg.getCommittedSolution()==null) {
					if (nrWarns>0) warn.append("<BR>");
					warn.append("There is no "+sg.getAbbv()+" solution committed");
					boolean dept = false;
					for (Iterator j=sg.getDepartments().iterator();j.hasNext();) {
						Department d = (Department)j.next();
						if (d.isExternalManager().booleanValue()) {
							warn.append(", ");
							warn.append(d.getExternalMgrAbbv());
						} else {
							dept = true;
							for (Iterator k=d.getSubjectAreas().iterator();k.hasNext();) {
								SubjectArea sa = (SubjectArea)k.next();
								warn.append(", ");
								warn.append(sa.getSubjectAreaAbbreviation());
							}
						}
					}
					warn.append((dept?" departmental":"")+" classes are not considered.");
					nrWarns++;
				}
				if (nrWarns>=3) {
					warn.append("<BR>...");
					break;
				}
			}
		}
		return (warn.length()==0?null:warn.toString());
	}
	
	public String getSolverWarningCheckSolution(User user, Session session, TimetableManager manager) {
		if (user.getCurrentRole().equals(Roles.VIEW_ALL_ROLE)) return null;
		if (user.getCurrentRole().equals(Roles.EXAM_MGR_ROLE)) return null;
		try {
			SolverProxy proxy = WebSolver.getSolver(pageContext.getSession());
			if (proxy!=null) {
				return proxy.getProperties().getProperty("General.SolverWarnings");
			}
		} catch (Exception e){}
		String id = (String)pageContext.getSession().getAttribute("Solver.selectedSolutionId");
		if (id!=null && id.length()>0) {
			StringBuffer warn = new StringBuffer();
 			for (StringTokenizer s = new StringTokenizer(id,",");s.hasMoreTokens();) {
 				Solution solution = (new SolutionDAO()).get(Long.valueOf(s.nextToken()));
				for (Iterator j=solution.getParameters().iterator();j.hasNext();) {
					SolverParameter p = (SolverParameter)j.next();
					if ("General.SolverWarnings".equals(p.getDefinition().getName())) {
						if (warn.length()>0) warn.append("<BR>");
						warn.append(p.getValue());
					}
				}
 			}
 			return (warn.length()==0?null:warn.toString());
		}
		if (user.isAdmin()) return null;
		return getSolverWarning(session, manager.getSolverGroups());
	}

	public int doStartTag() {
		try {
			User user = Web.getUser(pageContext.getSession());
			if (user==null) return SKIP_BODY;
			TimetableManager manager = TimetableManager.getManager(user);
			if (manager==null) return SKIP_BODY;
			Session acadSession = Session.getCurrentAcadSession(user);
			if (acadSession==null) return SKIP_BODY;
			String warns = getSolverWarningCheckSolution(user, acadSession, manager);
			if (warns!=null) {
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
