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
