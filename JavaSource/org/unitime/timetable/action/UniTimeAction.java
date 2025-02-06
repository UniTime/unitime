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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.struts2.StrutsStatics;
import org.apache.struts2.action.ServletRequestAware;
import org.apache.struts2.action.ServletResponseAware;
import org.springframework.context.ApplicationContext;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.ExaminationSolverService;
import org.unitime.timetable.solver.service.InstructorSchedulingSolverService;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.service.StudentSectioningSolverService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.apache.struts2.ActionContext;
import org.apache.struts2.ActionSupport;

/**
 * Base action for Struts2 actions
 * @author Tomas Muller
 */
public abstract class UniTimeAction<T extends UniTimeForm> extends ActionSupport implements ServletRequestAware, ServletResponseAware {
	private static final long serialVersionUID = 3596810266703379946L;
	protected SessionContext sessionContext;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected T form;
	protected String op;
	
	public T getForm() { return form; }
	public void setForm(T form) { this.form = form; }
	
	public String getOp() { return op; }
	public void setOp(String op) { this.op = op; }
	
	@Override
	public void withServletRequest(HttpServletRequest request) {
		this.request = request;
		this.sessionContext = new HttpSessionContext(request.getSession());
	}
	
	@Override
	public void withServletResponse(HttpServletResponse response) {
		this.response = response;
	}
	
	public HttpServletRequest getRequest() { return request; }
	public HttpServletResponse getResponse() { return response; }
	
	protected SolverServerService getSolverServerService() {
		return (SolverServerService)SpringApplicationContextHolder.getBean("solverServerService");
	}
	
	protected ExaminationSolverService getExaminationSolverService() {
		return (ExaminationSolverService)SpringApplicationContextHolder.getBean("examinationSolverService");
	}
	
	protected StudentSectioningSolverService getStudentSectioningSolverService() {
		return (StudentSectioningSolverService)SpringApplicationContextHolder.getBean("studentSectioningSolverService");
	}
	
	protected InstructorSchedulingSolverService getInstructorSchedulingSolverService() {
		return (InstructorSchedulingSolverService)SpringApplicationContextHolder.getBean("instructorSchedulingSolverService");
	}
	
	protected AssignmentService<ClassAssignmentProxy> getClassAssignmentService() {
		return (AssignmentService<ClassAssignmentProxy>)SpringApplicationContextHolder.getBean("classAssignmentService");
	}
	
	protected SolverService<SolverProxy> getCourseTimetablingSolverService() {
		return (SolverService<SolverProxy>)SpringApplicationContextHolder.getBean("courseTimetablingSolverService");
	}
	
	protected ApplicationContext getApplicationContext() {
		return SpringApplicationContextHolder.getContext();
	}
	
	protected PageContext getPageContext() {
		return (PageContext)ActionContext.getContext().get(StrutsStatics.PAGE_CONTEXT);
	}
	
	protected <X> Permission<X> getPermission(String name) {
		return (Permission<X>)SpringApplicationContextHolder.getBean(name);
	}
	
	public SessionContext getSessionContext() {
		return sessionContext;
	}
	
	private static RegExp sAcessKeyRegExp = RegExp.compile("<u>(\\w)</u>", "i");
    public static String guessAccessKey(String name) {
		if (name == null || name.isEmpty()) return null;
		MatchResult result = sAcessKeyRegExp.exec(name);
		return (result == null ? "" : result.getGroup(1).toLowerCase());
	}
    
    private static RegExp sStripAcessKeyRegExp = RegExp.compile("(.*)<u>(\\w)</u>(.*)", "i");
    public static String stripAccessKey(String name) {
		if (name == null || name.isEmpty()) return "";
		MatchResult result = sStripAcessKeyRegExp.exec(name);
		return (result == null ? name : (result.getGroup(1) + result.getGroup(2) + result.getGroup(3))).replace("&nbsp;", " ");
	}
}
