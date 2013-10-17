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
package org.unitime.timetable.solver;

import java.util.Date;

import javax.servlet.http.HttpSession;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class WebSolver {
	public static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
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
    
    private static AssignmentService<ClassAssignmentProxy> getClassAssignmentService(HttpSession session) {
		WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(session.getServletContext());
		return (AssignmentService<ClassAssignmentProxy>)applicationContext.getBean("classAssignmentService");
	}
    
    public static ClassAssignmentProxy getClassAssignmentProxy(HttpSession session) {
    	return getClassAssignmentService(session).getAssignment();
	}
}
