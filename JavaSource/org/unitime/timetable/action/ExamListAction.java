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

import java.awt.Image;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamListForm;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@Service("/examList")
public class ExamListAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamListForm myForm = (ExamListForm) form;
        
        sessionContext.checkPermission(Right.Examinations);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (op==null && sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea)!=null) {
        	String subject = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (subject.indexOf(',') >= 0) subject = subject.substring(0, subject.indexOf(','));
        	myForm.setSubjectAreaId(subject);
        	myForm.setCourseNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));
        }
        if (op==null && sessionContext.getAttribute(SessionAttribute.ExamType)!=null) {
        	myForm.setExamType((Long)sessionContext.getAttribute(SessionAttribute.ExamType));
        }
        if (myForm.getExamType() == null) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty())
				myForm.setExamType(types.first().getUniqueId());
        }
        
        WebTable.setOrder(sessionContext, "ExamList.ord", request.getParameter("ord"), 1);

        if ("Search".equals(op) || "Export PDF".equals(op) || "Export CSV".equals(op)) {
            if (myForm.getSubjectAreaId()!=null) {
            	sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, myForm.getSubjectAreaId());
                sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, myForm.getCourseNbr());
                sessionContext.setAttribute(SessionAttribute.ExamType, myForm.getExamType());
            }
            
            if ("Export PDF".equals(op)) {
            	ExportUtils.exportPDF(
            			getExamTable(myForm, false, true),
            			WebTable.getOrder(sessionContext, "ExamList.ord"),
            			response, "exams");
            	return null;
            }

            if ("Export CSV".equals(op)) {
            	ExportUtils.exportCSV(
            			getExamTable(myForm, false, false),
            			WebTable.getOrder(sessionContext, "ExamList.ord"),
            			response, "exams");
            	return null;
            }
        }
        
        if ("Add Examination".equals(op)) {
            return mapping.findForward("addExam");
        }
        
        myForm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser(), false));
        if (myForm.getSubjectAreas().size()==1) {
            SubjectArea firstSubjectArea = (SubjectArea)myForm.getSubjectAreas().iterator().next();
            myForm.setSubjectAreaId(firstSubjectArea.getUniqueId().toString());
        }
        
        if (myForm.getSubjectAreaId()!=null && myForm.getSubjectAreaId().length()!=0) {
            PdfWebTable table = getExamTable(myForm, true, false);
            if (table!=null) {
                request.setAttribute("ExamList.table", table.printTable(WebTable.getOrder(sessionContext, "ExamList.ord")));
                Vector ids = new Vector();
                for (Enumeration e=table.getLines().elements();e.hasMoreElements();) {
                    WebTable.WebTableLine line = (WebTable.WebTableLine)e.nextElement();
                    ids.add(Long.parseLong(line.getUniqueId()));
                }
                Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
            } else {
                ActionMessages errors = new ActionMessages();
                errors.add("exams", new ActionMessage("errors.generic", "No examination matching the above criteria was found."));
                saveErrors(request, errors);
            }
        }
        
        String subjectAreaName = "";
        try {
            subjectAreaName = new SubjectAreaDAO().get(new Long(myForm.getSubjectAreaId())).getSubjectAreaAbbreviation();
        } catch (Exception e) {}
        
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        if (myForm.getExamType() != null)
            BackTracker.markForBack(
                    request, 
                    "examList.do?op=Search&examType="+myForm.getExamType()+"&subjectAreaId="+myForm.getSubjectAreaId()+"&courseNbr="+myForm.getCourseNbr(),
                    ExamTypeDAO.getInstance().get(myForm.getExamType()).getLabel()+" Exams ("+(Constants.ALL_OPTION_VALUE.equals(myForm.getSubjectAreaId())?"All":subjectAreaName+
                        (myForm.getCourseNbr()==null || myForm.getCourseNbr().length()==0?"":" "+myForm.getCourseNbr()))+
                        ")", 
                    true, true);
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());

        return mapping.findForward("list");
    }
    
    public PdfWebTable getExamTable(ExamListForm form, boolean html, boolean color) {
    	ExamAssignmentProxy examAssignment = examinationSolverService.getSolver();
    	
        Collection exams = (form.getSubjectAreaId()==null || form.getSubjectAreaId().trim().length()==0 || "null".equals(form.getSubjectAreaId())
        		? null : Constants.ALL_OPTION_VALUE.equals(form.getSubjectAreaId())
        		? Exam.findAll(sessionContext.getUser().getCurrentAcademicSessionId(),form.getExamType()) : Exam.findExamsOfCourse(Long.valueOf(form.getSubjectAreaId()), form.getCourseNbr(),form.getExamType()));
        
        if (exams==null || exams.isEmpty()) return null;
        
        if (examAssignment!=null && !examAssignment.getExamTypeId().equals(form.getExamType())) examAssignment = null;
        
        String nl = (html?"<br>":"\n");
        
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(sessionContext.getUser());
        boolean timeText = RequiredTimeTable.getTimeGridAsText(sessionContext.getUser());
        String instructorNameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
        ExamType type = ExamTypeDAO.getInstance().get(form.getExamType());
        
        PdfWebTable table = new PdfWebTable(
                11,
                type.getLabel()+" Examinations", "examList.do?ord=%%",
                new String[] {"Classes / Courses", "Length", "Seating"+nl+"Type", "Size", "Max"+nl+"Rooms", 
                        "Instructor", "Period"+nl+"Preferences", "Room"+nl+"Preferences", "Distribution"+nl+"Preferences",
                        "Assigned"+nl+"Period", "Assigned"+nl+"Room"},
                new String[] {"left", "right", "center", "right", "right", "left", 
                        "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true}
                );
        
        
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            String objects = "", perPref = "", roomPref = "", distPref = "", per = "", rooms = "";
            Comparable perCmp = null;
            
            for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                ExamOwner owner = (ExamOwner)j.next();
                if (objects.length()>0) objects+=nl;
                objects += owner.getLabel();
            }
            
            ExamAssignment ea = (examAssignment!=null?examAssignment.getAssignment(exam.getUniqueId()):exam.getAssignedPeriod()!=null?new ExamAssignment(exam):null);
            if (ea!=null) {
                per = (html?ea.getPeriodAbbreviationWithPref():(ea.getPeriodPref() == null || !color ? "" : "@@COLOR " + PreferenceLevel.prolog2color(ea.getPeriodPref()) + " ") + ea.getPeriodAbbreviation());
                perCmp = ea.getPeriodOrd();
                rooms = "";
                for (ExamRoomInfo room : ea.getRooms()) {
                    if (rooms.length()>0) rooms += nl;
                    rooms += (html ? room.toString(): (color ? "@@COLOR " + PreferenceLevel.prolog2color(PreferenceLevel.int2prolog(room.getPreference())) + " " : "") + room.getName());
                }
            }
            
            if (html) {
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomPref.class);
                if (roomPref.length()>0 && !roomPref.endsWith(nl)) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(BuildingPref.class);
                if (roomPref.length()>0 && !roomPref.endsWith(nl)) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
                if (roomPref.length()>0 && !roomPref.endsWith(nl)) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
                if (roomPref.endsWith(nl)) roomPref = roomPref.substring(0, roomPref.length()-nl.length());
                if (timeText || ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
                	if (ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
                    	MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType());
                    	epx.load(exam);
                    	perPref+=epx.toString(true);
                	} else {
                		perPref += exam.getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                	}
                } else {
                    PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), ea, exam.getExamType().getUniqueId());
                    RequiredTimeTable rtt = new RequiredTimeTable(px);
                    px.load(exam);
                    String hint = rtt.print(false, timeVertical).replace(");\n</script>", "").replace("<script language=\"javascript\">\ndocument.write(", "").replace("\n", " ");
                    perPref = "<img border='0' src='" +
                    	"pattern?v=" + (timeVertical ? 1 : 0) + "&x="+exam.getUniqueId() + (ea == null ? "" : "&ap=" + ea.getPeriodId()) +
            			"' onmouseover=\"showGwtHint(this, " + hint + ");\" onmouseout=\"hideGwtHint();\">";
                }
                distPref += exam.getEffectivePrefHtmlForPrefType(DistributionPref.class);
            } else {
                for (Iterator j=exam.effectivePreferences(RoomPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") + PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(BuildingPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomFeaturePref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomGroupPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                if (ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
                    MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType());
                    epx.load(exam);
                    perPref+=epx.toString(false, color);
                } else {
                	if (timeText || !color) {
						for (Iterator j=exam.effectivePreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
	                        Preference pref = (Preference)j.next();
	                        if (perPref.length()>0) perPref+=nl;
	                        perPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
						}
                	} else {
                        PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), ea, exam.getExamType().getUniqueId());
                        px.load(exam);
                        RequiredTimeTable rtt = new RequiredTimeTable(px);
                        Image image = rtt.createBufferedImage(timeVertical);
    					if (image != null) {
    						table.addImage(exam.getUniqueId().toString(), image);
    						perPref += "@@IMAGE "+exam.getUniqueId().toString()+" ";
    					} else {
    						for (Iterator j=exam.effectivePreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
    	                        Preference pref = (Preference)j.next();
    	                        if (perPref.length()>0) perPref+=nl;
    	                        perPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
    						}
                        }
                	}
                }
                for (Iterator j=exam.effectivePreferences(DistributionPref.class).iterator();j.hasNext();) {
                    DistributionPref pref = (DistributionPref)j.next();
                    if (distPref.length()>0) distPref+=nl;
                    distPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">");
                }
            }
            
            int nrStudents = exam.getSize();
            String instructors = "";
            for (Iterator j=new TreeSet(exam.getInstructors()).iterator();j.hasNext();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)j.next();
                if (instructors.length()>0) instructors+=nl;
                instructors+=instructor.getName(instructorNameFormat);
            }
            
            table.addLine(
                    "onClick=\"document.location='examDetail.do?examId="+exam.getUniqueId()+"';\"",
                    new String[] {
                        (html?"<a name='"+exam.getUniqueId()+"'>":"")+objects+(html?"</a>":""),
                        exam.getLength().toString(),
                        (Exam.sSeatingTypeNormal==exam.getSeatingType()?"Normal":"Exam"),
                        String.valueOf(nrStudents),
                        exam.getMaxNbrRooms().toString(),
                        instructors,
                        perPref,
                        roomPref,
                        distPref,
                        per,
                        rooms
                    },
                    new Comparable[] {
                        exam.firstOwner(),
                        exam.getLength(),
                        exam.getSeatingType(),
                        nrStudents,
                        exam.getMaxNbrRooms(),
                        instructors,
                        perPref,
                        roomPref,
                        distPref,
                        perCmp,
                        rooms
                    },
                    exam.getUniqueId().toString());
        }
        
        return table;
                
    }
}
