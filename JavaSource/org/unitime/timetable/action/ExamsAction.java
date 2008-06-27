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
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.MultiComparable;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.authenticate.jaas.UserPasswordHandler;
import org.unitime.timetable.form.ExamsForm;
import org.unitime.timetable.model.ApplicationConfig;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamInfo.ExamSectionInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;

/** 
 * @author Tomas Muller
 */
public class ExamsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
	    ExamsForm myForm = (ExamsForm)form;

        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Apply".equals(op)) {
            myForm.save(request.getSession());
            if (myForm.getUsername()!=null && myForm.getUsername().length()>0 && myForm.getPassword()!=null && myForm.getPassword().length()>0) {
                try {
                    UserPasswordHandler handler = new UserPasswordHandler(myForm.getUsername(), myForm.getPassword());
                    LoginContext lc = new LoginContext("Timetabling", handler);
                    lc.login();
                    
                    Set creds = lc.getSubject().getPublicCredentials();
                    if (creds==null || creds.size()==0) {
                        myForm.setMessage("Authentication failed");
                    } else {
                        for (Iterator i=creds.iterator(); i.hasNext(); ) {
                            Object o = i.next();
                            if (o instanceof User) {
                                User user = (User) o;
                                HttpSession session = request.getSession();
                                session.setAttribute("loggedOn", "true");
                                session.setAttribute("hdnCallingScreen", "main.jsp");
                                Web.setUser(session, user);
                                
                                String appStatus = ApplicationConfig.getConfigValue(Constants.CFG_APP_ACCESS_LEVEL, Constants.APP_ACL_ALL);
                                session.setAttribute(Constants.CFG_APP_ACCESS_LEVEL, appStatus);
                                
                                session.setAttribute("authUserExtId", user.getId());
                                session.setAttribute("loginPage", "exams");
                                return mapping.findForward("personal");
                                //response.sendRedirect("selectPrimaryRole.do"); break;
                            }
                        }
                    }
                } catch (LoginException le) {
                    myForm.setMessage("Authentication failed");
                }
            }
        }
        myForm.load(request.getSession());
        
        WebTable.setOrder(request.getSession(),"exams.order",request.getParameter("ord"),1);
        
        if (myForm.getSession()!=null && myForm.getSubjectArea()!=null && myForm.getSubjectArea().length()>0) {
            org.unitime.timetable.model.Session session = new SessionDAO().get(myForm.getSession());
            if ((myForm.getExamType()==Exam.sExamTypeFinal && session.getStatusType().canNoRoleReportExamFinal()) ||
                (myForm.getExamType()==Exam.sExamTypeMidterm && session.getStatusType().canNoRoleReportExamMidterm())) {
                List exams = null;
                if ("--ALL--".equals(myForm.getSubjectArea())) 
                    exams = Exam.findAll(myForm.getSession(), myForm.getExamType());
                else {
                    SubjectArea sa = SubjectArea.findByAbbv(myForm.getSession(), myForm.getSubjectArea());
                    if (sa!=null) exams = Exam.findExamsOfSubjectArea(sa.getUniqueId(), myForm.getExamType());
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
                            myForm.setTable(table.printTable(WebTable.getOrder(request.getSession(),"exams.order")), table.getNrColumns(), table.getLines().size());
                    }
                }
            }
        }
		
        return mapping.findForward("show");
	}
	
	private PdfWebTable getTable(boolean html, ExamsForm form, Vector<ExamAssignment> exams) {
	    String nl = (html?"<br>":"\n");
        PdfWebTable table = new PdfWebTable( 7,
                form.getSessionLabel()+" "+form.getExamTypeLabel()+" examinations"+("--ALL--".equals(form.getSubjectArea())?"":" ("+form.getSubjectArea()+")"), "exams.do?ord=%%",
                new String[] {
                    "Subject",
                    "Course",
                    ("true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.external","false"))?ApplicationProperties.getProperty("tmtbl.exam.report.external.name","External Id"):"Instruction Type"),
                    "Section",
                    "Date",
                    "Time",
                    "Room"},
                new String[] {"left", "left", "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true, true} );
        table.setRowStyle("white-space:nowrap");
        String noRoom = ApplicationProperties.getProperty("tmtbl.exam.report.noroom","");
        for (ExamAssignment exam : exams) {
            for (ExamSectionInfo section : exam.getSections()) {
                if (!"--ALL--".equals(form.getSubjectArea()) && !section.getSubject().equals(form.getSubjectArea())) continue;
                table.addLine(
                        new String[] {
                                section.getSubject(),
                                section.getCourseNbr(),
                                section.getItype(),
                                section.getSection(),
                                exam.getDate(false),
                                exam.getTime(false),
                                (exam.getNrRooms()==0?noRoom:exam.getRoomsName(", "))
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

