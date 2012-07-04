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
package org.unitime.timetable.solver;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
public class WebSolver {
	public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");

    private static SolverService<SolverProxy> getCourseTimetablingSolverService(HttpSession session) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		return (SolverService<SolverProxy>)applicationContext.getBean("courseTimetablingSolverService");
	}

    public static SolverProxy getSolver(javax.servlet.http.HttpSession session) {
    	return getCourseTimetablingSolverService(session).getSolver();
    }
    
    private static SolverService<ExamSolverProxy> getExaminationSolverService(HttpSession session) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		return (SolverService<ExamSolverProxy>)applicationContext.getBean("examinationSolverService");
	}

    public static ExamSolverProxy getExamSolver(javax.servlet.http.HttpSession session) {
    	return getExaminationSolverService(session).getSolver();
    }
    
    private static SolverService<StudentSolverProxy> getStudentSectioningSolverService(HttpSession session) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		return (SolverService<StudentSolverProxy>)applicationContext.getBean("studentSectioningSolverService");
	}

    public static StudentSolverProxy getStudentSolver(javax.servlet.http.HttpSession session) {
    	return getStudentSectioningSolverService(session).getSolver();
    }
    
    public static ClassAssignmentProxy getClassAssignmentProxy(HttpSession session) {
		SolverProxy solver = getSolver(session);
		if (solver!=null) return new CachedClassAssignmentProxy(solver);
		
		String solutionIdsStr = (String)session.getAttribute("Solver.selectedSolutionId");
		Set<Long> solutionIds = new HashSet<Long>();
		if (solutionIdsStr != null) {
			for (StringTokenizer s = new StringTokenizer(solutionIdsStr, ","); s.hasMoreTokens(); )
				solutionIds.add(Long.valueOf(s.nextToken()));
		}
		
		SolutionClassAssignmentProxy cachedProxy = (SolutionClassAssignmentProxy)session.getAttribute("LastSolutionClassAssignmentProxy");
		if (cachedProxy != null && cachedProxy.equals(solutionIds)) {
			return cachedProxy;
		}
		
		SolutionClassAssignmentProxy newProxy = new SolutionClassAssignmentProxy(solutionIds);
		session.setAttribute("LastSolutionClassAssignmentProxy",newProxy);
		return newProxy;
	}
}
