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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.ExamDistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.Navigation;

/**
 * @author Tomas Muller
 */
@Action(value = "examDetail", results = {
		@Result(name = "showExamDetail", type = "tiles", location = "examDetail.tiles"),
		@Result(name = "addDistributionPrefs", type = "redirect", location = "/examDistributionPrefs.do",
				params = { "examId", "${form.examId}", "op", "${op}"}),
		@Result(name = "showList", type = "redirect", location = "/examList.action")
	})
@TilesDefinition(name = "examDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Detail"),
		@TilesPutAttribute(name = "body", value = "/user/examDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamDetailAction extends PreferencesAction2<ExamEditForm> {
	private static final long serialVersionUID = 2704790729386013602L;
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static ExaminationMessages EXMSG = Localization.create(ExaminationMessages.class);
	protected String examId = null;
	protected String op2 = null;
	
	public String getExamId() { return examId; }
	public void setExamId(String examId) { this.examId = examId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	

	public String execute() throws Exception {
		if (form == null) form = new ExamEditForm();
		
		super.execute();
            
        //Read parameters
        if (examId == null && request.getAttribute("examId") != null)
            examId = (String)request.getAttribute("examId");
        
        if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        
        //Check op exists
        // if (op==null) throw new Exception("Null Operation not supported.");
        
        // Read exam id from form
        if ("Edit".equals(op) || "Clone".equals(op) || "Add Distribution Preference".equals(op) || "Next".equals(op) || "Previous".equals(op) || "Delete".equals(op)) {
            examId = form.getExamId();
        } else if (EXMSG.accessExamEdit().equals(op) || EXMSG.accessExamClone().equals(op) || EXMSG.accessExamAddDistributionPref().equals(op) || EXMSG.accessExamNext().equals(op) || EXMSG.accessExamPrevious().equals(op) || EXMSG.accessExamDelete().equals(op)) {
            examId = form.getExamId();
        } else {
        	form.reset();
        	if (request.getSession().getAttribute("Exam.Type")!=null)
            	form.setExamType((Long)request.getSession().getAttribute("Exam.Type"));
            if (form.getExamType() == null) {
            	List<ExamType> types = ExamType.findAllUsedApplicable(HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser(), DepartmentStatusType.Status.ExamEdit, DepartmentStatusType.Status.ExamTimetable);
            	if (!types.isEmpty()) form.setExamType(types.get(0).getUniqueId());
            }
        }
        
        Debug.debug("op: " + op);
        Debug.debug("exam: " + examId);
        
        //Check exam exists
        if (examId == null || examId.trim().isEmpty()) throw new Exception(EXMSG.errorNoExamId());
        
        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        Exam exam = new ExamDAO().get(Long.valueOf(examId));
        
        //After delete -> one more back
        if (exam==null && BackTracker.hasBack(request, 1)) {
            if (request.getParameter("backType")!=null)
                request.setAttribute("backType", request.getParameter("backType"));
            if (request.getParameter("backId")!=null)
                request.setAttribute("backId", request.getParameter("backId"));
            BackTracker.doBack(request, response);
            return null;
        }
        
        sessionContext.checkPermission(examId, "Exam", Right.ExaminationDetail);

        //Edit Information - Redirect to info edit screen
        if (("Edit".equals(op) || EXMSG.accessExamEdit().equals(op)) && examId!=null && examId.trim()!="") {
        	sessionContext.checkPermission(exam, Right.ExaminationEdit);
        	response.sendRedirect( response.encodeURL("examEdit.action?examId="+examId) );
            return null;
        }

        if (("Clone".equals(op) || EXMSG.accessExamClone().equals(op)) && examId!=null && examId.trim()!="") {
        	sessionContext.checkPermission(exam, Right.ExaminationClone);
            response.sendRedirect( response.encodeURL("examEdit.action?examId="+examId+"&clone=true") );
            return null;
        }
        
        if ("Next".equals(op) || EXMSG.accessExamNext().equals(op)) {
            response.sendRedirect(response.encodeURL("examDetail.action?examId="+form.getNextId()));
            return null;
        }
        
        if ("Previous".equals(op) || EXMSG.accessExamPrevious().equals(op)) {
            response.sendRedirect(response.encodeURL("examDetail.action?examId="+form.getPreviousId()));
            return null;
        }
        
        if ("Delete".equals(op) || EXMSG.accessExamDelete().equals(op)) {
        	sessionContext.checkPermission(exam, Right.ExaminationDelete);
            org.hibernate.Session hibSession = new ExamDAO().getSession();
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext,
                        exam, 
                        ChangeLog.Source.EXAM_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
                exam.deleteDependentObjects(hibSession, false);
                for (Iterator<ExamConflict> j=exam.getConflicts().iterator();j.hasNext();) {
                    ExamConflict conf = j.next();
                    for (Iterator<Exam> i=conf.getExams().iterator();i.hasNext();) {
                        Exam x = i.next();
                        if (!x.equals(exam)) x.getConflicts().remove(conf);
                    }
                    hibSession.delete(conf);
                    j.remove();
                }
                hibSession.delete(exam);
                tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                throw e;
            }
            if (BackTracker.hasBack(request, 1)) {
                request.setAttribute("backType", "Exam");
                request.setAttribute("backId", "-1");
                BackTracker.doBack(request, response);
                return null;
            }
            return "showList";
        }

        
        // Add Distribution Preference - Redirect to dist prefs screen
        if ("Add Distribution Preference".equals(op) || EXMSG.accessExamAddDistributionPref().equals(op)) {
        	sessionContext.checkPermission(exam, Right.DistributionPreferenceExam);
            request.setAttribute("examId", examId);
            return "addDistributionPrefs";
        }

        
        // Load form attributes that are constant
        doLoad(exam);
        
        // Display distribution Prefs
        ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
        String html = tbl.getDistPrefsTable(request, sessionContext, exam);
        if (html!=null)
            request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
        
        if (!exam.getOwners().isEmpty()) {
            WebTable table = new WebTable(7, null, new String[] {
            		EXMSG.colExamOwnerObject(), EXMSG.colExamOwnerType(), EXMSG.colExamOwnerTitle(), EXMSG.colExamOwnerManager(),
            		EXMSG.colExamOwnerStudents(), EXMSG.colExamOwnerLimit(), EXMSG.colExamOwnerAssignment()},
            		new String[] {"left", "center", "left", "left", "right", "right", "left"},
            		new boolean[] {true, true, true, true, true, true, true});
            for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();) {
                ExamOwner owner = (ExamOwner)i.next();
                String onclick = null, name = null, type = null, students = String.valueOf(owner.countStudents()), limit = String.valueOf(owner.getLimit()), manager = null, assignment = null, title = null;
                String rowStyle = null, rowTitle = null;
                switch (owner.getOwnerType()) {
                    case ExamOwner.sOwnerTypeClass :
                        Class_ clazz = (Class_)owner.getOwnerObject();
                        if (sessionContext.hasPermission(clazz, Right.ClassDetail))
                            onclick = "onClick=\"document.location='classDetail.action?cid="+clazz.getUniqueId()+"';\"";
                        name = owner.getLabel();//clazz.getClassLabel();
                        type = EXMSG.examTypeClass();
                        manager = clazz.getManagingDept().getShortLabel();
                        if (clazz.getCommittedAssignment()!=null)
                            assignment = clazz.getCommittedAssignment().getPlacement().getLongName(CONSTANTS.useAmPm());
                        title = clazz.getSchedulePrintNote();
                        if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                        if (clazz.isCancelled()) {
                        	rowStyle = "color: gray; font-style: italic;";
                        	rowTitle = MSG.classNoteCancelled(clazz.getClassLabel());
                        }
                        break;
                    case ExamOwner.sOwnerTypeConfig :
                        InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                        if (sessionContext.hasPermission(config.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
                        name = owner.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
                        type = EXMSG.examTypeConfig();
                        manager = config.getInstructionalOffering().getControllingCourseOffering().getDepartment().getShortLabel();
                        title = config.getControllingCourseOffering().getTitle();
                        break;
                    case ExamOwner.sOwnerTypeOffering :
                        InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                        if (sessionContext.hasPermission(offering, Right.InstructionalOfferingDetail))
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
                        name = owner.getLabel();//offering.getCourseName();
                        type = EXMSG.examTypeOffering();
                        manager = offering.getControllingCourseOffering().getDepartment().getShortLabel();
                        title = offering.getControllingCourseOffering().getTitle();
                        break;
                    case ExamOwner.sOwnerTypeCourse :
                        CourseOffering course = (CourseOffering)owner.getOwnerObject();
                        if (sessionContext.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
                        name = owner.getLabel();//course.getCourseName();
                        type = EXMSG.examTypeCourse();
                        manager = course.getDepartment().getShortLabel();
                        title = course.getTitle();
                        break;
                            
                }
                WebTableLine line = table.addLine(onclick, new String[] { name, type, title, manager, students, limit, assignment}, null);
                if (rowStyle != null) line.setStyle(rowStyle);
                if (rowTitle != null) line.setTitle(rowTitle);
            }
            request.setAttribute("table",table.printTable());
        }
        
        ExamAssignmentInfo ea = null;
        
        ExamAssignmentProxy examAssignment = WebSolver.getExamSolver(request.getSession());
        if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
            ea = examAssignment.getAssignmentInfo(exam.getUniqueId());
        } else if (exam.getAssignedPeriod()!=null)
            ea = new ExamAssignmentInfo(exam);
        
        if (ea!=null && ea.getPeriod()!=null) {
            String assignment = "<tr><td>" + EXMSG.propExamAssignedPeriod() + "</td><td>"+ea.getPeriodNameWithPref()+"</td></tr>";
            if (!ea.getRooms().isEmpty()) {
                assignment += "<tr><td>" + (ea.getRooms().size() > 1 ? EXMSG.propExamAssignedRooms() : EXMSG.propExamAssignedRoom()) + "</td><td>";
                assignment += ea.getRoomsNameWithPref("<br>");
                assignment += "</td></tr>";
            }
            if (ea.getNrDistributionConflicts()>0) {
                assignment += "<tr><td>" + EXMSG.propExamViolatedDistConstraints() + "</td><td>";
                assignment += ea.getDistributionConflictTable();
                assignment += "</td></tr>";
            }
            if (ea.getHasConflicts()) {
                assignment += "<tr><td>" + EXMSG.propExamStudentConflicts() + "</td><td>";
                assignment += ea.getConflictTable();
                assignment += "</td></tr>";
            }
            if (ea.getHasInstructorConflicts()) {
                assignment += "<tr><td>" + EXMSG.propExamInstructorConflicts() + "</td><td>";
                assignment += ea.getInstructorConflictTable();
                assignment += "</td></tr>";
            }
            request.setAttribute("assignment",assignment);
        }
        
        BackTracker.markForBack(
                request,
                "examDetail.action?examId=" + examId,
                EXMSG.backToExam(form.getName()==null?form.getLabel().trim():form.getName().trim()),
                true, false);
        
        // Initialize Preferences for initial load
        form.setAvailableTimePatterns(null);
        initPrefs(exam, null, false);
        generateExamPeriodGrid(exam, "init", timeVertical, false);
        
        // Process Preferences Action
        processPrefAction();
        
        setupInstructors(exam);
        
        LookupTables.setupRooms(request, exam);      // Room Prefs
        LookupTables.setupBldgs(request, exam);      // Building Prefs
        LookupTables.setupRoomFeatures(request, exam); // Preference Levels
        LookupTables.setupRoomGroups(request, exam);   // Room Groups
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable, DepartmentStatusType.Status.ExamView);
        
        return "showExamDetail";
	}
    
    protected void doLoad(Exam exam) {
        form.setExamId(exam.getUniqueId().toString());
        
        form.setLabel(exam.getLabel());
        form.setName(exam.generateName().equals(exam.getName())?null:exam.getName());
        form.setNote(exam.getNote()==null?null:exam.getNote().replaceAll("\n", "<br>"));
        form.setLength(exam.getLength());
        form.setSize(String.valueOf(exam.getSize()));
        form.setPrintOffset(exam.getPrintOffset()==null || exam.getPrintOffset()==0 ? null: (exam.getPrintOffset()>0?"+":"")+exam.getPrintOffset());
        form.setSeatingType(Exam.sSeatingTypes[exam.getSeatingType()]);
        form.setMaxNbrRooms(exam.getMaxNbrRooms());
        form.setExamType(exam.getExamType().getUniqueId());
        form.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(exam)));
        
        TreeSet instructors = new TreeSet(exam.getInstructors());

        for (Iterator i = instructors.iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            form.getInstructors().add(instr.getUniqueId().toString());
        }
        
        Long nextId = Navigation.getNext(sessionContext, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        Long prevId = Navigation.getPrevious(sessionContext, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        form.setPreviousId(prevId==null?null:prevId.toString());
        form.setNextId(nextId==null?null:nextId.toString());
        
        ExamPeriod avgPeriod = exam.getAveragePeriod();
        form.setAvgPeriod(avgPeriod==null?null:avgPeriod.getName());
    }

    protected void setupInstructors(Exam exam) throws Exception {

        List instructors = form.getInstructors();
        if(instructors.size()==0) return;
        
        HashSet deptIds = new HashSet();
        
        for (Iterator i = exam.getInstructors().iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            deptIds.add(instr.getDepartment().getUniqueId());
        }
        for (Iterator i = exam.getOwners().iterator(); i.hasNext(); ) {
            ExamOwner own = (ExamOwner)i.next();
            deptIds.add(own.getCourse().getDepartment().getUniqueId());
        }
        
        Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
        for (Iterator i=deptIds.iterator();i.hasNext();)
            deptsIdsArray[idx++]=(Long)i.next();

        LookupTables.setupInstructors(request, sessionContext, deptsIdsArray);
    }
}
