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

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamsForm;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LoginManager;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.PdfWebTable;

/** 
 * @author Tomas Muller, Zuzana Mullerova, Stephanie Schluttenhofer
 */
@Service("/exams")
public class ExamsAction extends Action {
	
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired AuthenticationManager authenticationManager;
	
	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    ExamsForm myForm = (ExamsForm)form;

        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (request.getParameter("select")!=null) {
            myForm.load(request.getSession());
            if (request.getParameter("subject")!=null) {
                myForm.setSubjectArea(request.getParameter("subject"));
            } else {
            	if (myForm.canDisplayAllSubjectsAtOnce()){
            		myForm.setSubjectArea(Constants.ALL_OPTION_VALUE);
            	}
            }
            if (request.getParameter("year")!=null && request.getParameter("term")!=null && request.getParameter("campus")!=null) {
                Session session = Session.getSessionUsingInitiativeYearTerm(
                        request.getParameter("campus"), 
                        request.getParameter("year"), 
                        request.getParameter("term"));
                if (session!=null) myForm.setSession(session.getUniqueId());
            }
            if (request.getParameter("type")!=null) {
            	try {
            		myForm.setExamType(Long.valueOf(request.getParameter("type")));
            	} catch (NumberFormatException e) {
            		ExamType type = ExamType.findByReference(request.getParameter("type"));
            		if (type != null) myForm.setExamType(type.getUniqueId()); 
            	}
            } else {
                myForm.setExamType(null);
            }
            op = "Apply";
        }
        
        if ("Apply".equals(op)) {
            myForm.save(request.getSession());
            if (myForm.getUsername()!=null && myForm.getUsername().length()>0 && myForm.getPassword()!=null && myForm.getPassword().length()>0) {
            	try {
            		Authentication authRequest = new UsernamePasswordAuthenticationToken(myForm.getUsername(), myForm.getPassword());
            		Authentication authResult = authenticationManager.authenticate(authRequest);
            		SecurityContextHolder.getContext().setAuthentication(authResult);
            		UserContext user = (UserContext)authResult.getPrincipal();
            		if (user.getCurrentAuthority() == null || !user.getCurrentAuthority().hasRight(Right.PersonalSchedule))
            			for (UserAuthority auth: user.getAuthorities()) {
            				if (auth.getAcademicSession() != null && auth.getAcademicSession().getQualifierId().equals(myForm.getSession()) && auth.hasRight(Right.PersonalSchedule)) {
            					user.setCurrentAuthority(auth); break;
            				}
            			}
            		request.getSession().setAttribute("loginPage", "exams");
            		LoginManager.loginSuceeded(authResult.getName());
            		if (user.getCurrentAuthority() == null) {
            			response.sendRedirect("selectPrimaryRole.do");
            			return null;
            		}
            		return mapping.findForward("personal");
            	} catch (Exception e) {
            		myForm.setMessage("Authentication failed: " + e.getMessage());
            		LoginManager.addFailedLoginAttempt(myForm.getUsername(), new Date());
            	}
            }
        }
        myForm.load(request.getSession());
        
        WebTable.setOrder(sessionContext,"exams.order",request.getParameter("ord"),1);
        
        if (myForm.getSession()!=null && myForm.getSubjectArea()!=null && myForm.getSubjectArea().length()>0 && myForm.getExamType() != null) {
            org.unitime.timetable.model.Session session = new SessionDAO().get(myForm.getSession());
            if ((myForm.isFinals() && session.getStatusType().canNoRoleReportExamFinal()) ||
                (!myForm.isFinals() && session.getStatusType().canNoRoleReportExamMidterm())) {
                List exams = null;
                if ("--ALL--".equals(myForm.getSubjectArea())) 
                    exams = Exam.findAll(myForm.getSession(), myForm.getExamType());
                else {
                    SubjectArea sa = SubjectArea.findByAbbv(myForm.getSession(), myForm.getSubjectArea());
                    if (sa!=null) exams = Exam.findExamsOfSubjectAreaIncludeCrossLists(sa.getUniqueId(), myForm.getExamType());
                }
                if (exams!=null && !exams.isEmpty()) { 
                    Vector<ExamAssignment> assignments = new Vector();
                    for (Iterator i=exams.iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        if (exam.getAssignedPeriod()!=null) assignments.add(new ExamAssignment(exam));
                    }
                    if (!assignments.isEmpty()) {
                        PdfWebTable table = getTable(true, myForm, assignments);
                        if (table!=null)
                            myForm.setTable(table.printTable(WebTable.getOrder(sessionContext,"exams.order")), table.getNrColumns(), table.getLines().size());
                    }
                }
            }
        }
		
        String msg = ApplicationProperty.ExamsMessage.value();
        if (msg!=null && msg.length()>0)
            request.setAttribute(Constants.REQUEST_MSSG, msg);
        
        LookupTables.setupExamTypes(request, null);

        return mapping.findForward("show");
	}
		
	private PdfWebTable getTable(boolean html, ExamsForm form, Vector<ExamAssignment> exams) {
		String itype = MSG.columnExamInstructionalType();
		if (ApplicationProperty.ExaminationReportsExternalId.isTrue()) {
			itype = ApplicationProperty.ExaminationReportsExternalIdName.value();
			if (itype == null)
				itype = MSG.columnExamExternalId();
		}
	    PdfWebTable table = new PdfWebTable( 7,
                form.getSessionLabel()+" "+form.getExamTypeLabel().toLowerCase()+" "+ MSG.examinations() + 
                	("--ALL--".equals(form.getSubjectArea())?"":" ("+
                	form.getSubjectArea()+")"), "exams.do?ord=%%",
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
}

