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
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

/**
 * @author Tomas Muller
 */
@Action(value = "examEdit", results = {
		@Result(name = "showAdd", type = "tiles", location = "examAdd.tiles"),
		@Result(name = "showEdit", type = "tiles", location = "examEdit.tiles"),
		@Result(name = "showDetail", type = "redirect", location = "/examDetail.action",
				params = { "examId", "${form.examId}", "op", "${op}"}),
		@Result(name = "showList", type = "redirect", location = "/examList.action")
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "examEdit.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Edit Examination"),
				@TilesPutAttribute(name = "body", value = "/user/examEdit.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
			}),
		@TilesDefinition(name = "examAdd.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Add Examination"),
				@TilesPutAttribute(name = "body", value = "/user/examEdit.jsp"),
				@TilesPutAttribute(name = "showNavigation", value = "true")
			})
})
public class ExamEditAction extends PreferencesAction2<ExamEditForm> {
	private static final long serialVersionUID = -6628177736452722156L;
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static ExaminationMessages EXMSG = Localization.create(ExaminationMessages.class);
	protected String examId = null;
	protected String op2 = null;
	protected String deleteType = null;
	protected Boolean clone = null;
	protected Long deleteId = null;
	protected Long firstId = null;
	protected String firstType = null;
	
	public String getExamId() { return examId; }
	public void setExamId(String examId) { this.examId = examId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	public String getDeleteType() { return deleteType; }
	public void setDeleteType(String deleteType) { this.deleteType = deleteType; }
	public Boolean isClone() { return clone; }
	public void setClone(Boolean clone) { this.clone = clone; }
	public Long getDeleteId() { return deleteId; }
	public void setDeleteId(Long deleteId) { this.deleteId = deleteId; }
	public Long getFirstId() { return firstId; }
	public void setFirstId(Long firstId) { this.firstId = firstId; }
	public String getFirstType() { return firstType; }
	public void setFirstType(String firstType) { this.firstType = firstType; }
	

	public String execute() throws Exception {
		if (form == null) {
			form = new ExamEditForm();
			form.reset();
        	if (request.getSession().getAttribute("Exam.Type")!=null)
            	form.setExamType((Long)request.getSession().getAttribute("Exam.Type"));
            if (form.getExamType() == null) {
            	List<ExamType> types = ExamType.findAllUsedApplicable(HttpSessionContext.getSessionContext(request.getSession().getServletContext()).getUser(), DepartmentStatusType.Status.ExamEdit, DepartmentStatusType.Status.ExamTimetable);
            	if (!types.isEmpty()) form.setExamType(types.get(0).getUniqueId());
            }
		}
		
		super.execute();
        
        // Read parameters
		if (op == null) op = form.getOp();
        
		if (examId == null && form.getExamId() != null)
			examId = form.getExamId();
        
        // Determine if initial load
        if (op == null || op.trim().isEmpty()) {
        	if (deleteType != null && !deleteType.isEmpty())
        		op = "delete";
        	else
        		op = "init";
        }
        
        // Check op exists
        if (op==null || op.trim().isEmpty()) 
        	throw new Exception(EXMSG.errorNoExamId());
        
        Exam exam = (examId==null || examId.trim().isEmpty() ? null: ExamDAO.getInstance().get(Long.valueOf(examId)));

        if (exam != null) {
        	sessionContext.checkPermission(examId, "Exam", Right.ExaminationEdit);
        	form.setExamId(exam.getUniqueId().toString());
        } else if (exam == null) {
        	sessionContext.checkPermission(Right.ExaminationAdd);
        	form.setExamId(null);
        }

        boolean timeVertical = CommonValues.VerticalGrid.eq(sessionContext.getUser().getProperty(UserProperty.GridOrientation));

        // Cancel - Go back to Instructors Detail Screen
        if ("Back".equals(op) || EXMSG.actionBatckToDetail().equals(op)) {
            if (BackTracker.hasBack(request, 1)) {
                BackTracker.doBack(request, response);
                return null;
            }
            if (examId != null && !examId.trim().isEmpty()) {
                return "showDetail";
            } else {
            	return "showList";
            }
        }
        
        // Clear all preferences
        if (exam != null && ("Clear Preferences".equals(op) || EXMSG.actionClearExamPreferences().equals(op))) { 
        	sessionContext.checkPermission(exam, Right.ExaminationEditClearPreferences);
            Set s = exam.getPreferences();
            s.clear();
            exam.setPreferences(s);            
            ExamDAO.getInstance().getSession().merge(exam);
            ExamDAO.getInstance().getSession().flush();
            op = "init";                
            
            ChangeLog.addChange(
                    null, 
                    sessionContext,
                    exam, 
                    ChangeLog.Source.EXAM_EDIT, 
                    ChangeLog.Operation.CLEAR_PREF,
                    exam.firstSubjectArea(),
                    exam.firstDepartment());
            
            return "showDetail";
        }
        
        // Reset form for initial load
        if ("init".equals(op)) { 
            doLoad(exam);
            if (Boolean.TRUE.equals(clone)) {
                form.setExamId(null);
                form.setClone(true);
            }
        }
        
        ExamType type = ExamTypeDAO.getInstance().get(form.getExamType());
        if (ApplicationProperty.ExaminationSizeUseLimitInsteadOfEnrollment.isTrue(type.getReference(), type.getType() != ExamType.sExamTypeFinal))
            form.setSizeNote("A number of enrolled students or a total limit of selected classes/courses (whichever is bigger) is used when blank");
        else
            form.setSizeNote("A number of enrolled students is used when blank");
        
        form.setLabel(form.getClone() || exam==null?"New Examination":exam.getLabel());
        
        if ("Add Instructor".equals(op) || EXMSG.actionAddInstructor().equals(op)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
                form.getInstructors().add(Preference.BLANK_PREF_VALUE);
            }
        }
        
        if ("Add Object".equals(op) || EXMSG.actionAddObject().equals(op)) {
            for (int i=0; i<Constants.PREF_ROWS_ADDED; i++) {
                form.addExamOwner(null);
            }
            request.setAttribute("hash", "objects");
        }
        
        if ("instructor".equals(deleteType) && deleteId != null) {
            form.getInstructors().remove(deleteId.intValue());
        } else if ("examType".equals(deleteType) && deleteId != null) {
            form.setExamType(deleteId);
        } else if ("objects".equals(deleteType) && deleteId != null) {
            form.deleteExamOwner(deleteId.intValue());
        }
        
        if ("Update".equals(op) || EXMSG.actionExamUpdate().equals(op) || 
        	"Save".equals(op) || EXMSG.actionExamSave().equals(op) ||
        	"Previous".equals(op) || EXMSG.actionExamPrevious().equals(op) || 
        	"Next".equals(op) || EXMSG.actionExamNext().equals(op)) {
        	form.validate(this);
        	if (!hasFieldErrors()) {
                doUpdate(exam);
                
                if ("Next".equals(op) || EXMSG.actionExamNext().equals(op)) {
                    response.sendRedirect(response.encodeURL("examEdit.action?examId="+form.getNextId()));
                    return null;
                }
                
                if ("Previous".equals(op) || EXMSG.actionExamPrevious().equals(op)) {
                    response.sendRedirect(response.encodeURL("examEdit.action?examId="+form.getPreviousId()));
                    return null;
                }
                
                if (("Save".equals(op) || EXMSG.actionExamSave().equals(op)) && BackTracker.hasBack(request, 2) && !form.getClone()) {
                    request.setAttribute("backType", "PreferenceGroup");
                    request.setAttribute("backId", form.getExamId());
                    BackTracker.doBack(request, response);
                    return null;
                }
                
                return "showDetail";
            }
        }
        
        // Initialize Preferences for initial load 
        form.setAvailableTimePatterns(null);
        if("init".equals(op)) {
            initPrefs(exam, null, true);
        }
        generateExamPeriodGrid((form.getClone() ? null : exam), op, timeVertical, true);
        
        // Process Preferences Action
        processPrefAction();
        
        setupInstructors(exam);
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable, DepartmentStatusType.Status.ExamEdit);
        
        if (exam!=null) {
            LookupTables.setupRooms(request, exam);      // Room Prefs
            LookupTables.setupBldgs(request, exam);      // Building Prefs
            LookupTables.setupRoomFeatures(request, exam); // Preference Levels
            LookupTables.setupRoomGroups(request, exam);   // Room Groups
        } else {
            Exam dummy = new Exam(); 
            dummy.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
            dummy.setExamType(ExamTypeDAO.getInstance().get(form.getExamType()));
            LookupTables.setupRooms(request, dummy);      // Room Prefs
            LookupTables.setupBldgs(request, dummy);      // Building Prefs
            LookupTables.setupRoomFeatures(request, dummy); // Preference Levels
            LookupTables.setupRoomGroups(request, dummy);   // Room Groups
        }
        
        form.setAllowHardPrefs(sessionContext.hasPermission(exam, Right.CanUseHardPeriodPrefs));
        
        form.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser(), false));
    
        if (!form.getClone() && exam!=null) {
            BackTracker.markForBack(
                request,
                "examDetail.action?examId="+form.getExamId(),
                EXMSG.backExam(form.getName()==null || form.getName().length()==0?form.getLabel().trim():form.getName().trim()),
                true, false);
        }

        return (form.getClone() || exam==null ? "showAdd" : "showEdit");
	}
        
    protected void doLoad(Exam exam) {
        if (exam!=null) {
            form.setExamId(exam.getUniqueId().toString());
            form.setExamType(exam.getExamType().getUniqueId());
            
            form.setName(exam.generateName().equals(exam.getName())?null:exam.getName());
            form.setNote(exam.getNote());
            form.setLength(exam.getLength());
            form.setSize(exam.getExamSize()==null?null:exam.getExamSize().toString());
            form.setPrintOffset(exam.getPrintOffset()==null || exam.getPrintOffset()==0?null:exam.getPrintOffset().toString());
            form.setSeatingType(Exam.getSeatingTypeLabel(exam.getSeatingType()));
            form.setMaxNbrRooms(exam.getMaxNbrRooms());
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
            
            for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();)
                form.addExamOwner((ExamOwner)i.next());
        } else {
            try {
                TreeSet periods = ExamPeriod.findAll(sessionContext.getUser().getCurrentAcademicSessionId(), form.getExamType());
                if (!periods.isEmpty())
                    form.setLength(Constants.SLOT_LENGTH_MIN*((ExamPeriod)periods.first()).getLength());
                SolverParameterDef maxRoomsParam = SolverParameterDef.findByNameType("Exams.MaxRooms", SolverParameterGroup.SolverType.EXAM);
                if (maxRoomsParam!=null && maxRoomsParam.getDefault()!=null) 
                    form.setMaxNbrRooms(Integer.valueOf(maxRoomsParam.getDefault()));
            } catch (Exception e) {}
        }
        
        if (firstId != null && firstType != null) {
            if ("Class_".equals(firstType)) {
                Class_ clazz = new Class_DAO().get(firstId);
                form.getSubjectArea().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getSubjectArea().getUniqueId());
                form.getCourseNbr().add(clazz.getSchedulingSubpart().getControllingCourseOffering().getUniqueId());
                form.getItype().add(clazz.getSchedulingSubpart().getUniqueId());
                form.getClassNumber().add(clazz.getUniqueId());
            } else if ("SchedulingSubpart".equals(firstType)) {
                SchedulingSubpart subpart = SchedulingSubpartDAO.getInstance().get(firstId);
                InstrOfferingConfig config = subpart.getInstrOfferingConfig();
                form.getSubjectArea().add(config.getControllingCourseOffering().getSubjectArea().getUniqueId());
                form.getCourseNbr().add(config.getControllingCourseOffering().getUniqueId());
                form.getItype().add(-config.getUniqueId());
                form.getClassNumber().add(Long.valueOf(-1));
            } else if ("InstrOfferingConfig".equals(firstType)) {
                InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(firstId);
                form.getSubjectArea().add(config.getControllingCourseOffering().getSubjectArea().getUniqueId());
                form.getCourseNbr().add(config.getControllingCourseOffering().getUniqueId());
                form.getItype().add(-config.getUniqueId());
                form.getClassNumber().add(Long.valueOf(-1));
            } else if ("InstructionalOffering".equals(firstType)) {
                InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(firstId);
                form.getSubjectArea().add(offering.getControllingCourseOffering().getSubjectArea().getUniqueId());
                form.getCourseNbr().add(offering.getControllingCourseOffering().getUniqueId());
                form.getItype().add(Long.MIN_VALUE+1);
                form.getClassNumber().add(Long.valueOf(-1));
            } else if ("CourseOffering".equals(firstType)) {
                CourseOffering course = CourseOfferingDAO.getInstance().get(firstId);
                form.getSubjectArea().add(course.getSubjectArea().getUniqueId());
                form.getCourseNbr().add(course.getUniqueId());
                form.getItype().add(Long.MIN_VALUE);
                form.getClassNumber().add(Long.valueOf(-1));
            }
        }
        
        for (int i=0;i<Constants.PREF_ROWS_ADDED;i++) {
            form.addExamOwner(null);
            form.getInstructors().add(Constants.BLANK_OPTION_VALUE);
        }
    }

    protected void setupInstructors(Exam exam) throws Exception {

        List instructors = form.getInstructors();
        if (instructors == null || instructors.size()==0) return;
        
        HashSet deptIds = new HashSet();
        
        if (exam!=null) {
            for (Iterator i = exam.getInstructors().iterator(); i.hasNext(); ) {
                DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
                deptIds.add(instr.getDepartment().getUniqueId());
            }
            for (Iterator i = exam.getOwners().iterator(); i.hasNext(); ) {
                ExamOwner own = (ExamOwner)i.next();
                deptIds.add(own.getCourse().getDepartment().getUniqueId());
            }
        } else {
            for (int i=0;i<form.getSubjectAreaList().size();i++) {
                ExamOwner own = form.getExamOwner(i);
                if (own!=null) deptIds.add(own.getCourse().getDepartment().getUniqueId());
            }
            if (deptIds.isEmpty()) {
            	for (Department dept: Department.getUserDepartments(sessionContext.getUser())) {
                    deptIds.add(dept.getUniqueId());
                }
            }
        }
        
        Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
        for (Iterator i=deptIds.iterator();i.hasNext();)
            deptsIdsArray[idx++]=(Long)i.next();

        LookupTables.setupInstructors(request, sessionContext, deptsIdsArray);
    }
    
    protected void doUpdate(Exam exam) throws Exception {
        boolean add = false;
        if (exam==null) {
            add = true;
            exam = new Exam();
            exam.setSession(SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()));
            exam.setExamType(ExamTypeDAO.getInstance().get(form.getExamType()));
        }
        
        Set s = exam.getPreferences();
        if (s==null) s = new HashSet();
        
        // Clear all old prefs
        for (Iterator i=s.iterator();i.hasNext();) {
            Preference p = (Preference)i.next();
            if (p instanceof DistributionPref) {
                //skip distribution preferences
            } else
                i.remove();
        }

        super.doUpdate(exam, s, false,
        		Preference.Type.PERIOD, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING);
        
        exam.setNote(form.getNote());
        exam.setSeatingType(form.getSeatingTypeIdx());
        Integer oldLength = exam.getLength();
        exam.setLength(Integer.valueOf(form.getLength()));
        if (form.getSize()==null || form.getSize().length()==0) {
            exam.setExamSize(null);
        } else {
            exam.setExamSize(Integer.valueOf(form.getSize()));
        }
        int oldPrintOffset = (exam.getPrintOffset()==null?0:exam.getPrintOffset());
        if (form.getPrintOffset()==null || form.getPrintOffset().length()==0) {
            exam.setPrintOffset(null);
        } else {
            exam.setPrintOffset(Integer.valueOf(form.getPrintOffset()));
        }
        exam.setMaxNbrRooms(Integer.valueOf(form.getMaxNbrRooms()));
        
        if (exam.getInstructors()==null) exam.setInstructors(new HashSet());
        for (Iterator i=exam.getInstructors().iterator();i.hasNext();) {
            DepartmentalInstructor instructor = (DepartmentalInstructor)i.next();
            instructor.getExams().remove(exam);
            i.remove();
        }
        for (Iterator i=form.getInstructors().iterator();i.hasNext();) {
            String instructorId = (String)i.next();
            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
                DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instructorId));
                if (instructor!=null) {
                    exam.getInstructors().add(instructor);
                    instructor.getExams().add(exam);
                }
           }
        }
        
        form.setExamOwners(exam);

        if (form.getName()==null || form.getName().trim().length()==0) {
            exam.setName(exam.generateName());            
        } else {
            exam.setName(form.getName());
        }
        ExamEvent event = exam.getEvent();
        if (event!=null) {
            event.setEventName(exam.getName());
            if (exam.getAssignedPeriod()!=null && (!exam.getLength().equals(oldLength) || oldPrintOffset!=(exam.getPrintOffset()==null?0:exam.getPrintOffset()))) {
                for (Iterator i=event.getMeetings().iterator();i.hasNext();) {
                    Meeting m = (Meeting)i.next();
                    m.setStartOffset(Integer.valueOf(exam.getAssignedPeriod().getExamEventStartOffsetForExam(exam)));
                    m.setStopOffset(Integer.valueOf(exam.getAssignedPeriod().getExamEventStopOffsetForExam(exam)));
                }
            }
        }
        
        exam.generateDefaultPreferences(false);
        
        if (exam.getUniqueId() == null)
        	ExamDAO.getInstance().getSession().persist(exam);
        else
        	ExamDAO.getInstance().getSession().merge(exam);
        
        ChangeLog.addChange(
                null, 
                sessionContext,
                exam, 
                ChangeLog.Source.EXAM_EDIT, 
                (add?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                exam.firstSubjectArea(), 
                exam.firstDepartment());

        if (add) form.setExamId(exam.getUniqueId().toString());
    }
}
