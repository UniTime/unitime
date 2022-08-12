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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ConstantsMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamListForm;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.DepartmentStatusType;
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
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.IdValue;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@Action(value="examList", results = {
		@Result(name = "list", type = "tiles", location = "examList.tiles"),
		@Result(name = "addExam", type = "redirect", location = "/examEdit.action")
	})
@TilesDefinition(name = "examList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examinations"),
		@TilesPutAttribute(name = "body", value = "/user/examList.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamListAction extends UniTimeAction<ExamListForm> {
	private static final long serialVersionUID = 1752223672961857285L;
	protected static final ConstantsMessages CONST = Localization.create(ConstantsMessages.class);
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
    private String ord;
    
    public String getOrd() { return ord; }
	public void setOrd(String ord) { this.ord = ord; }

    public String execute() throws Exception {
        sessionContext.checkPermission(Right.Examinations);
        
        if (getForm() == null) setForm(new ExamListForm());
        
        // Read operation to be performed
        String op = (getForm().getOp() != null ? getForm().getOp( ) :request.getParameter("op"));
        
        if (op==null && sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea) != null) {
        	String subject = (String)sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
        	if (subject.indexOf(',') >= 0) subject = subject.substring(0, subject.indexOf(','));
        	if ("All".equalsIgnoreCase(subject))
        		getForm().setSubjectAreaId(-1l);
        	else
        		getForm().setSubjectAreaId(Long.valueOf(subject));
        	getForm().setCourseNbr((String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber));
        }
        if (op==null && sessionContext.getAttribute(SessionAttribute.ExamType)!=null) {
        	getForm().setExamType((Long)sessionContext.getAttribute(SessionAttribute.ExamType));
        }
        if (getForm().getExamType() == null) {
			List<ExamType> types = ExamType.findAllUsedApplicable(sessionContext.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
			if (!types.isEmpty())
				getForm().setExamType(types.get(0).getUniqueId());
        }
        
        WebTable.setOrder(sessionContext, "ExamList.ord", getOrd(), 1);

        if ("Search".equals(op) || "Export PDF".equals(op) || "Export CSV".equals(op) ||
        	MSG.buttonSearch().equals(op) || MSG.buttonExportPDF().equals(op) || MSG.buttonExportCSV().equals(op)) {
            if (getForm().getSubjectAreaId()!=null) {
            	sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, getForm().getSubjectAreaId() < 0 ? "All" : getForm().getSubjectAreaId().toString());
                sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, getForm().getCourseNbr());
                sessionContext.setAttribute(SessionAttribute.ExamType, getForm().getExamType());
            }
            
            if ("Export PDF".equals(op) || MSG.buttonExportPDF().equals(op)) {
            	ExportUtils.exportPDF(
            			getExamTable(false, true),
            			WebTable.getOrder(sessionContext, "ExamList.ord"),
            			response, "exams");
            	return null;
            }

            if ("Export CSV".equals(op) || MSG.buttonExportCSV().equals(op)) {
            	ExportUtils.exportCSV(
            			getExamTable(false, false),
            			WebTable.getOrder(sessionContext, "ExamList.ord"),
            			response, "exams");
            	return null;
            }
        }
        
        if ("Add Examination".equals(op) || MSG.buttonAddExamination().equals(op)) {
            return "addExam";
        }
        
        List<IdValue> subjects = new ArrayList<IdValue>();
        subjects.add(new IdValue(null, CONST.select()));
        if (sessionContext.hasPermission(Right.DepartmentIndependent)) {
        	subjects.add(new IdValue(-1l, CONST.all()));
        }
        TreeSet<SubjectArea> userSubjectAreas = SubjectArea.getUserSubjectAreas(sessionContext.getUser(), false);
        for (SubjectArea sa: userSubjectAreas)
        	subjects.add(new IdValue(sa.getUniqueId(), sa.getSubjectAreaAbbreviation()));
        getForm().setSubjectAreas(subjects);
        if (userSubjectAreas.size() == 1) {
            getForm().setSubjectAreaId(userSubjectAreas.first().getUniqueId());
        }
        
        if (getForm().getSubjectAreaId() != null) {
            PdfWebTable table = getExamTable(true, false);
            if (table!=null) {
                request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext, "ExamList.ord")));
                Vector ids = new Vector();
                for (Enumeration e=table.getLines().elements();e.hasMoreElements();) {
                    WebTable.WebTableLine line = (WebTable.WebTableLine)e.nextElement();
                    ids.add(Long.parseLong(line.getUniqueId()));
                }
                Navigation.set(sessionContext, Navigation.sInstructionalOfferingLevel, ids);
            } else {
            	addActionError(MSG.errorNoMatchingExam());
            }
        }
        
        String subjectAreaName = "";
        try {
            subjectAreaName = new SubjectAreaDAO().get(getForm().getSubjectAreaId()).getSubjectAreaAbbreviation();
        } catch (Exception e) {}
        
        if (request.getParameter("backId")!=null)
            request.setAttribute("hash", request.getParameter("backId"));
        
        if (getForm().getExamType() != null && getForm().getSubjectAreaId() != null && getForm().getCourseNbr() != null)
            BackTracker.markForBack(
                    request, 
                    "examList.action?form.op=" + MSG.buttonSearch() + "&form.examType="+getForm().getExamType()+"&form.subjectAreaId="+getForm().getSubjectAreaId()+"&form.courseNbr="+URLEncoder.encode(getForm().getCourseNbr(), "utf-8"),
                    MSG.backExams(
                    		ExamTypeDAO.getInstance().get(getForm().getExamType()).getLabel(),
                    		getForm().getSubjectAreaId() < 0l ? CONST.all() :
                    		subjectAreaName + (getForm().getCourseNbr()==null || getForm().getCourseNbr().length() == 0 ? "" : " "+getForm().getCourseNbr())), 
                    true, true);
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);

        return "list";
    }
    
    public PdfWebTable getExamTable(boolean html, boolean color) {
    	ExamAssignmentProxy examAssignment = getExaminationSolverService().getSolver();
    	
        Collection exams = (getForm().getSubjectAreaId() == null ? null :
        	getForm().getSubjectAreaId() < 0 ? Exam.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), getForm().getExamType()) :
        	Exam.findExamsOfCourse(getForm().getSubjectAreaId(), getForm().getCourseNbr(), getForm().getExamType()));
        
        if (exams==null || exams.isEmpty()) return null;
        
        if (examAssignment!=null && !examAssignment.getExamTypeId().equals(getForm().getExamType())) examAssignment = null;
        
        String nl = (html?"<br>":"\n");
        
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(sessionContext.getUser());
        boolean timeText = RequiredTimeTable.getTimeGridAsText(sessionContext.getUser());
        String instructorNameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
        ExamType type = ExamTypeDAO.getInstance().get(getForm().getExamType());
        
        PdfWebTable table = new PdfWebTable(
                11,
                MSG.tableExaminations(type.getLabel()), "examList.action?ord=%%",
                new String[] {
                		MSG.colExamOwner().replace("|", nl), MSG.colExamLength().replace("|", nl), MSG.colExamSeatingType().replace("|", nl), 
                		MSG.colExamSize().replace("|", nl), MSG.colExamMaxRooms().replace("|", nl), MSG.colExamInstructor().replace("|", nl),
                		MSG.colExamPeriodPrefs().replace("|", nl), MSG.colExamRoomPrefs().replace("|", nl), MSG.colExamDistributionPrefs().replace("|", nl),
                		MSG.colExamAssignedPeriod().replace("|", nl), MSG.colExamAssignedRoom().replace("|", nl)},
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
                if (ApplicationProperty.LegacyPeriodPreferences.isTrue()) {
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
                } else {
                    if (timeText || ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
                    	if (ExamType.sExamTypeMidterm==exam.getExamType().getType()) {
                        	MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType());
                        	epx.load(exam);
                        	perPref += "<span onmouseover=\"showGwtExamPeriodPreferencesHint(this, '" + exam.getUniqueId() + "', null);\" onmouseout=\"hideGwtPeriodPreferencesHint();\">" + epx.toString(true) + "</span>";
                    	} else {
                    		perPref += "<span onmouseover=\"showGwtExamPeriodPreferencesHint(this, '" + exam.getUniqueId() + "', null);\" onmouseout=\"hideGwtPeriodPreferencesHint();\">" + exam.getEffectivePrefHtmlForPrefType(ExamPeriodPref.class) + "</span>";
                    	}
                    } else {
                        perPref = "<img border='0' src='pattern?v=" + (timeVertical ? 1 : 0) + "&x="+exam.getUniqueId() + (ea == null ? "" : "&ap=" + ea.getPeriodId()) +
                			"' onmouseover=\"showGwtExamPeriodPreferencesHint(this, '" + exam.getUniqueId() + "', null);\" onmouseout=\"hideGwtPeriodPreferencesHint();\">";
                    }                	
                }
                distPref += exam.getEffectivePrefHtmlForPrefType(DistributionPref.class);
            } else {
                for (Iterator j=exam.effectivePreferences(RoomPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") + pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(BuildingPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomFeaturePref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomGroupPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
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
	                        perPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
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
    	                        perPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText();
    						}
                        }
                	}
                }
                for (Iterator j=exam.effectivePreferences(DistributionPref.class).iterator();j.hasNext();) {
                    DistributionPref pref = (DistributionPref)j.next();
                    if (distPref.length()>0) distPref+=nl;
                    distPref += (color ? "@@COLOR " + PreferenceLevel.prolog2color(pref.getPrefLevel().getPrefProlog()) + " " : "") +pref.getPrefLevel().getAbbreviation()+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">");
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
                    "onClick=\"document.location='examDetail.action?examId="+exam.getUniqueId()+"';\"",
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
