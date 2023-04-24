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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamsForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.PdfWebTable;

/** 
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Action(value = "exams", results = {
		@Result(name = "show", type = "tiles", location = "exams.tiles"),
		@Result(name = "personal", type = "redirect", location = "/personalSchedule.action")
	})
@TilesDefinition(name = "exams.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Schedule"),
		@TilesPutAttribute(name = "body", value = "/exam/exams.jsp"),
		@TilesPutAttribute(name = "checkLogin", value = "false"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class ExamsAction extends UniTimeAction<ExamsForm> {
	private static final long serialVersionUID = 252256673838259727L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String year, term, campus, type, subject, select;
	
	public String getYear() { return year; }
	public void setYear(String year) { this.year = year; }
	public String getTerm() { return term; }
	public void setTerm(String term) { this.term = term; }
	public String getCampus() { return campus; }
	public void setCampus(String campus) { this.campus = campus; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getSubject() { return subject; }
	public void setSubject(String subject) { this.subject = subject; }
	public String getSelect() { return select; }
	public void setSelect(String select) { this.select = select; }

	public String execute() throws Exception {
		if (form == null) {
	    	form = new ExamsForm();
	    	form.reset();
	    }
		
    	if (form.getOp() != null) op = form.getOp();
	    
        if (subject != null || select != null) {
            form.load(request.getSession());
            if (subject != null) {
                form.setSubjectArea(subject);
            } else {
            	if (form.canDisplayAllSubjectsAtOnce()){
            		form.setSubjectArea("--ALL--");
            	}
            }
            if (year!=null && term!=null && campus!=null) {
                Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
                if (session!=null) form.setSession(session.getUniqueId());
            }
            if (type!=null) {
            	try {
            		form.setExamType(Long.valueOf(type));
            	} catch (NumberFormatException e) {
            		ExamType examType = ExamType.findByReference(type);
            		if (examType != null) form.setExamType(examType.getUniqueId()); 
            	}
            } else {
                form.setExamType(null);
            }
            op = MSG.buttonApply();
        }
        
        if ("Change".equals(op)) {
        	form.save(request.getSession());
        }
        
        if (MSG.buttonLogIn().equals(op)) {
        	if (form.getUsername()!=null && form.getUsername().length()>0 && form.getPassword()!=null && form.getPassword().length()>0) {
            	try {
            		Authentication authRequest = new UsernamePasswordAuthenticationToken(form.getUsername(), form.getPassword());
            		Authentication authResult = getAuthenticationManager().authenticate(authRequest);
            		SecurityContextHolder.getContext().setAuthentication(authResult);
            		UserContext user = (UserContext)authResult.getPrincipal();
            		if (user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.PersonalSchedule))
            			for (UserAuthority auth: user.getAuthorities()) {
            				if (auth.getAcademicSession() != null && auth.getAcademicSession().getQualifierId().equals(form.getSession()) && auth.hasRight(Right.PersonalSchedule)) {
            					user.setCurrentAuthority(auth); break;
            				}
            			}
            		request.getSession().setAttribute("loginPage", "exams");
            		LoginManager.loginSuceeded(authResult.getName());
            		if (user.getCurrentAuthority() == null) {
            			response.sendRedirect("selectPrimaryRole.action");
            			return null;
            		}
            		return "personal";
            	} catch (Exception e) {
            		form.setMessage("Authentication failed: " + e.getMessage());
            		LoginManager.addFailedLoginAttempt(form.getUsername(), new Date());
            	}
            }
        	op = MSG.buttonApply();
        }
        
        if (MSG.buttonApply().equals(op)) {
            form.save(request.getSession());
        }
        form.load(request.getSession());
        
        WebTable.setOrder(sessionContext,"exams.order",request.getParameter("ord"),1);
        
        if (form.getSession()!=null && form.getSubjectArea()!=null && form.getSubjectArea().length()>0 && form.getExamType() != null) {
            org.unitime.timetable.model.Session session = SessionDAO.getInstance().get(form.getSession());
            ExamStatus status = ExamStatus.findStatus(form.getSession(), form.getExamType());
            DepartmentStatusType type = (status == null || status.getStatus() == null ? session.getStatusType() : status.getStatus());
            if (type != null && type.can(form.isFinals() ? DepartmentStatusType.Status.ReportExamsFinal : DepartmentStatusType.Status.ReportExamsMidterm)) {
                List exams = null;
                if ("--ALL--".equals(form.getSubjectArea())) 
                    exams = Exam.findAll(form.getSession(), form.getExamType());
                else {
                    SubjectArea sa = SubjectArea.findByAbbv(form.getSession(), form.getSubjectArea());
                    if (sa!=null) exams = Exam.findExamsOfSubjectAreaIncludeCrossLists(sa.getUniqueId(), form.getExamType());
                }
                if (exams!=null && !exams.isEmpty()) { 
                    List<ExamAssignment> assignments = new ArrayList<ExamAssignment>();
                    for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        if (exam.getAssignedPeriod()!=null) assignments.add(new ExamAssignment(exam));
                    }
                    if (!assignments.isEmpty()) {
                        PdfWebTable table = getTable(true, assignments);
                        if (table!=null)
                            form.setTable(table.printTable(WebTable.getOrder(sessionContext,"exams.order")), table.getNrColumns(), table.getLines().size());
                    }
                }
            }
        }
		
        LookupTables.setupExamTypes(request, null);

        return "show";
	}
		
	private PdfWebTable getTable(boolean html, List<ExamAssignment> exams) {
		String itype = MSG.columnExamInstructionalType();
		if (ApplicationProperty.ExaminationReportsExternalId.isTrue()) {
			itype = ApplicationProperty.ExaminationReportsExternalIdName.value();
			if (itype == null)
				itype = MSG.columnExamExternalId();
		}
	    PdfWebTable table = new PdfWebTable( 7,
                form.getSessionLabel()+" "+form.getExamTypeLabel().toLowerCase()+" "+ MSG.examinations() + 
                	("--ALL--".equals(form.getSubjectArea())?"":" ("+
                	form.getSubjectArea()+")"), "exams.action?ord=%%",
                new String[] {
                    MSG.columnExamSubject(),
                    MSG.columnExamCourse(),
                    itype,
                    MSG.columnExamSection(),
                    MSG.columnExamDate(),
                    MSG.columnExamTime(),
                    MSG.columnExamRoom()},
                new String[] {"left", "left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperty.ExaminationsNoRoomText.value();
        for (ExamAssignment exam : exams) {
            for (ExamSectionInfo section : exam.getSectionsIncludeCrosslistedDummies()) {
                if (!"--ALL--".equals(form.getSubjectArea()) && !section.getSubject().equals(form.getSubjectArea())) continue;
                table.addLine(
                        new String[] {
                                section.getSubject(),
                                section.getCourseNbr(),
                                section.getItype(),
                                section.getSection(),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0 ? noRoom : html ? exam.getRoomsNameWithHint(false, ", ") : exam.getRoomsName(", "))
                            },
                            new Comparable[] {
                                new MultiComparable(section.getSubject(), section.getCourseNbr(), section.getItype(), section.getSection(), exam.getPeriodOrd()),
                                new MultiComparable(section.getSubject(), section.getCourseNbr(), section.getItype(), section.getSection(), exam.getPeriodOrd()),
                                new MultiComparable(section.getSubject(), section.getItype(), section.getCourseNbr(), section.getSection(), exam.getPeriodOrd()),
                                new MultiComparable(section.getSubject(), section.getCourseNbr(), section.getSection(), section.getItype(), exam.getPeriodOrd()),
                                new MultiComparable(exam.getPeriodOrd(), section.getSubject(), section.getCourseNbr(), section.getSection(), section.getItype()),
                                new MultiComparable(exam.getPeriod().getStartSlot(), section.getSubject(), section.getCourseNbr(), section.getSection(), section.getItype(), exam.getPeriodOrd()),
                                new MultiComparable(exam.getRoomsName(":"), section.getSubject(), section.getCourseNbr(), section.getItype(), section.getSection(), exam.getPeriodOrd()),
                            });
                }
        }
        return table;	    
	}
	
	protected AuthenticationManager getAuthenticationManager() {
		return (AuthenticationManager)SpringApplicationContextHolder.getBean("authenticationManager");
	}
}

