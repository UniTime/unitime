/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamReportForm;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * @author Tomas Muller
 */
public class UnassignedExamsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamReportForm myForm = (ExamReportForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if ("Export PDF".equals(op) || "Apply".equals(op)) {
            myForm.save(request.getSession());
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        myForm.load(request.getSession());
        
        ExamSolverProxy solver = WebSolver.getExamSolver(request.getSession());
        Collection<ExamInfo> unassignedExams = null;
        if (myForm.getSubjectArea()!=null && myForm.getSubjectArea()!=0) {
            if (solver!=null && solver.getExamType()==myForm.getExamType())
                unassignedExams = solver.getUnassignedExams(myForm.getSubjectArea());
            else
                unassignedExams = Exam.findUnassignedExams(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId(), myForm.getSubjectArea(),myForm.getExamType());
        }
        
        WebTable.setOrder(request.getSession(),"unassignedExams.ord",request.getParameter("ord"),1);
        
        WebTable table = getTable(Web.getUser(request.getSession()), true, myForm, unassignedExams);
        
        if ("Export PDF".equals(op) && table!=null) {
            PdfWebTable pdfTable = getTable(Web.getUser(request.getSession()), false, myForm, unassignedExams);
            File file = ApplicationProperties.getTempFile("unassigned", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"unassignedExams.ord"));
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if (table!=null)
            myForm.setTable(table.printTable(WebTable.getOrder(request.getSession(),"unassignedExams.ord")), 9, unassignedExams.size());

        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        return mapping.findForward("showReport");
	}
	
    public PdfWebTable getTable(org.unitime.commons.User user, boolean html, ExamReportForm form, Collection<ExamInfo> exams) {
        if (exams==null || exams.isEmpty()) return null;
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
        boolean timeText = RequiredTimeTable.getTimeGridAsText(user);
        
        String nl = (html?"<br>":"\n");
		PdfWebTable table =
            new PdfWebTable( 9,
                    "Not-assigned Examinations", "unassignedExams.do?ord=%%",
                    new String[] {(form.getShowSections()?"Classes / Courses":"Examination"), "Length", "Seating"+nl+"Type", "Size", "Max"+nl+"Rooms",
                                 "Instructor", "Period"+nl+"Preferences", "Room"+nl+"Preferences", "Distribution"+nl+"Preferences"},
       				new String[] {"left", "right", "center", "right", "right", "left", "left", "left", "left"},
       				new boolean[] {true, true, true, false, false, true, true, true, true} );
		table.setRowStyle("white-space:nowrap");
        try {
        	for (ExamInfo exam : exams) {
        	    String perPref = "", roomPref = "", distPref = "";
        	    
                if (html) {
                    roomPref += exam.getExam().getEffectivePrefHtmlForPrefType(RoomPref.class);
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += exam.getExam().getEffectivePrefHtmlForPrefType(BuildingPref.class);
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += exam.getExam().getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += exam.getExam().getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
                    if (roomPref.endsWith(nl)) roomPref = roomPref.substring(0, roomPref.length()-nl.length());
                    if (timeText) {
                        perPref += exam.getExam().getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                    } else {
                        if (exam.getExamType()==Exam.sExamTypeMidterm) {
                            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getExam().getSession());
                            if (epx.canDo()) {
                                epx.load(exam.getExam());
                                perPref += epx.toString(true);
                            } else {
                                PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getExam().getSession(), exam.getExamType());
                                px.load(exam.getExam());
                                RequiredTimeTable rtt = new RequiredTimeTable(px);
                                File imageFileName = null;
                                try {
                                    imageFileName = rtt.createImage(timeVertical);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                String title = rtt.getModel().toString();
                                if (imageFileName!=null)
                                    perPref = "<img border='0' src='temp/"+(imageFileName.getName())+"' title='"+title+"'>";
                                else
                                    perPref += exam.getExam().getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                            }
                        } else {
                            PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getExam().getSession(), exam.getExamType());
                            px.load(exam.getExam());
                            RequiredTimeTable rtt = new RequiredTimeTable(px);
                            File imageFileName = null;
                            try {
                                imageFileName = rtt.createImage(timeVertical);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            String title = rtt.getModel().toString();
                            if (imageFileName!=null)
                                perPref = "<img border='0' src='temp/"+(imageFileName.getName())+"' title='"+title+"'>";
                            else
                                perPref += exam.getExam().getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                        }
                    }
                    distPref += exam.getExam().getEffectivePrefHtmlForPrefType(DistributionPref.class);
                } else {
                    for (Iterator j=exam.getExam().effectivePreferences(RoomPref.class).iterator();j.hasNext();) {
                        Preference pref = (Preference)j.next();
                        if (roomPref.length()>0) roomPref+=nl;
                        roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                    }
                    for (Iterator j=exam.getExam().effectivePreferences(BuildingPref.class).iterator();j.hasNext();) {
                        Preference pref = (Preference)j.next();
                        if (roomPref.length()>0) roomPref+=nl;
                        roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                    }
                    for (Iterator j=exam.getExam().effectivePreferences(RoomFeaturePref.class).iterator();j.hasNext();) {
                        Preference pref = (Preference)j.next();
                        if (roomPref.length()>0) roomPref+=nl;
                        roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                    }
                    for (Iterator j=exam.getExam().effectivePreferences(RoomGroupPref.class).iterator();j.hasNext();) {
                        Preference pref = (Preference)j.next();
                        if (roomPref.length()>0) roomPref+=nl;
                        roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                    }
                    if (exam.getExamType()==Exam.sExamTypeMidterm) {
                        MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getExam().getSession());
                        if (epx.canDo()) {
                            epx.load(exam.getExam());
                            perPref += epx.toString();
                        } else {
                            for (Iterator j=exam.getExam().effectivePreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
                                Preference pref = (Preference)j.next();
                                if (perPref.length()>0) perPref+=nl;
                                perPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                            }
                        }
                    } else {
                        for (Iterator j=exam.getExam().effectivePreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
                            Preference pref = (Preference)j.next();
                            if (perPref.length()>0) perPref+=nl;
                            perPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                        }
                    }
                    for (Iterator j=exam.getExam().effectivePreferences(DistributionPref.class).iterator();j.hasNext();) {
                        DistributionPref pref = (DistributionPref)j.next();
                        if (distPref.length()>0) distPref+=nl;
                        distPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">");
                    }
                }
                
                String instructors = exam.getInstructorName(", ");
                
        	    table.addLine(
        	            "onClick=\"window.open('examInfo.do?examId="+exam.getExamId()+"','exams','width=1000,height=600,resizable=yes,scrollbars=yes,toolbar=no,location=no,directories=no,status=yes,menubar=no,copyhistory=no');\"",
                        new String[] {
                            (html?"<a name='"+exam.getExamId()+"'>":"")+(form.getShowSections()?exam.getSectionName(nl):exam.getExamName())+(html?"</a>":""),
                            String.valueOf(exam.getLength()),
                            (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                            String.valueOf(exam.getNrStudents()),
                            String.valueOf(exam.getMaxRooms()),
                            instructors,
                            perPref,
                            roomPref,
                            distPref
                        },
                        new Comparable[] {
                            exam,
                            exam.getLength(),
                            exam.getSeatingType(),
                            exam.getNrStudents(),
                            exam.getMaxRooms(),
                            instructors,
                            perPref,
                            roomPref,
                            distPref
                        },
                        exam.getExamId().toString());
        	}
        } catch (Exception e) {
        	Debug.error(e);
        	table.addLine(new String[] {"<font color='red'>ERROR:"+e.getMessage()+"</font>"},null);
        }
        return table;
    }	
}

