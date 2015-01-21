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
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.security.SessionContext;
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
@Service("/examDetail")
public class ExamDetailAction extends PreferencesAction {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Autowired SessionContext sessionContext;
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamEditForm frm = (ExamEditForm) form;
        try {
            
            // Set common lookup tables
            super.execute(mapping, form, request, response);
            
            MessageResources rsc = getResources(request);
            ActionMessages errors = new ActionMessages();
            
            //Read parameters
            String examId = null;
            if (request.getParameter("examId")!=null && request.getParameter("examId").trim().length()!=0)
                examId = request.getParameter("examId");
            if (examId==null && request.getAttribute("examId")!=null && request.getAttribute("examId").toString().trim().length()!=0)
                examId = request.getAttribute("examId").toString();
            
            String op = frm.getOp();
            
            if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
                op = request.getParameter("op2");
            
            //Check op exists
            if (op==null) throw new Exception ("Null Operation not supported.");
            
            // Read exam id from form
            if (op.equals(rsc.getMessage("button.editExam")) || op.equals(rsc.getMessage("button.cloneExam")) || op.equals(rsc.getMessage("button.addDistPref")) || op.equals(rsc.getMessage("button.nextExam")) || op.equals(rsc.getMessage("button.previousExam"))) {
                examId = frm.getExamId();
            } else {
                frm.reset(mapping, request);
            }
            
            Debug.debug("op: " + op);
            Debug.debug("exam: " + examId);
            
            //Check exam exists
            if (examId==null || examId.trim()=="") throw new Exception ("Exam Info not supplied.");
            
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
            if (op.equals(rsc.getMessage("button.editExam")) && examId!=null && examId.trim()!="") {
            	sessionContext.checkPermission(exam, Right.ExaminationEdit);
            	response.sendRedirect( response.encodeURL("examEdit.do?examId="+examId) );
                return null;
            }

            if (op.equals(rsc.getMessage("button.cloneExam")) && examId!=null && examId.trim()!="") {
            	sessionContext.checkPermission(exam, Right.ExaminationClone);
                response.sendRedirect( response.encodeURL("examEdit.do?examId="+examId+"&clone=true") );
                return null;
            }
            
            if (op.equals(rsc.getMessage("button.nextExam"))) {
                response.sendRedirect(response.encodeURL("examDetail.do?examId="+frm.getNextId()));
                return null;
            }
            
            if (op.equals(rsc.getMessage("button.previousExam"))) {
                response.sendRedirect(response.encodeURL("examDetail.do?examId="+frm.getPreviousId()));
                return null;
            }
            
            if (op.equals(rsc.getMessage("button.deleteExam"))) {
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
                return mapping.findForward("showList");
            }

            
            // Add Distribution Preference - Redirect to dist prefs screen
            if(op.equals(rsc.getMessage("button.addDistPref"))) {
            	sessionContext.checkPermission(exam, Right.DistributionPreferenceExam);
                request.setAttribute("examId", examId);
                return mapping.findForward("addDistributionPrefs");
            }

            
            // Load form attributes that are constant
            doLoad(request, frm, exam);
            
            // Display distribution Prefs
            ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
            String html = tbl.getDistPrefsTable(request, sessionContext, exam);
            if (html!=null)
                request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
            
            if (!exam.getOwners().isEmpty()) {
                WebTable table = new WebTable(7, null, new String[] {"Object", "Type", "Title", "Manager", "Students", "Limit", "Assignment"}, new String[] {"left", "center", "left", "left", "right", "right", "left"}, new boolean[] {true, true, true, true, true, true, true});
                for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();) {
                    ExamOwner owner = (ExamOwner)i.next();
                    String onclick = null, name = null, type = null, students = String.valueOf(owner.countStudents()), limit = String.valueOf(owner.getLimit()), manager = null, assignment = null, title = null;
                    switch (owner.getOwnerType()) {
                        case ExamOwner.sOwnerTypeClass :
                            Class_ clazz = (Class_)owner.getOwnerObject();
                            if (sessionContext.hasPermission(clazz, Right.ClassDetail))
                                onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
                            name = owner.getLabel();//clazz.getClassLabel();
                            type = "Class";
                            manager = clazz.getManagingDept().getShortLabel();
                            if (clazz.getCommittedAssignment()!=null)
                                assignment = clazz.getCommittedAssignment().getPlacement().getLongName(CONSTANTS.useAmPm());
                            title = clazz.getSchedulePrintNote();
                            if (title==null || title.length()==0) title=clazz.getSchedulingSubpart().getControllingCourseOffering().getTitle();
                            break;
                        case ExamOwner.sOwnerTypeConfig :
                            InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                            if (sessionContext.hasPermission(config.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = owner.getLabel();//config.getCourseName()+" ["+config.getName()+"]";
                            type = "Configuration";
                            manager = config.getInstructionalOffering().getControllingCourseOffering().getDepartment().getShortLabel();
                            title = config.getControllingCourseOffering().getTitle();
                            break;
                        case ExamOwner.sOwnerTypeOffering :
                            InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                            if (sessionContext.hasPermission(offering, Right.InstructionalOfferingDetail))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
                            name = owner.getLabel();//offering.getCourseName();
                            type = "Offering";
                            manager = offering.getControllingCourseOffering().getDepartment().getShortLabel();
                            title = offering.getControllingCourseOffering().getTitle();
                            break;
                        case ExamOwner.sOwnerTypeCourse :
                            CourseOffering course = (CourseOffering)owner.getOwnerObject();
                            if (sessionContext.hasPermission(course.getInstructionalOffering(), Right.InstructionalOfferingDetail))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = owner.getLabel();//course.getCourseName();
                            type = "Course";
                            manager = course.getDepartment().getShortLabel();
                            title = course.getTitle();
                            break;
                                
                    }
                    table.addLine(onclick, new String[] { name, type, title, manager, students, limit, assignment}, null);
                }
                request.setAttribute("ExamDetail.table",table.printTable());
            }
            
            ExamAssignmentInfo ea = null;
            
            ExamAssignmentProxy examAssignment = WebSolver.getExamSolver(request.getSession());
            if (examAssignment!=null && examAssignment.getExamTypeId().equals(exam.getExamType().getUniqueId())) {
                ea = examAssignment.getAssignmentInfo(exam.getUniqueId());
            } else if (exam.getAssignedPeriod()!=null)
                ea = new ExamAssignmentInfo(exam);
            
            if (ea!=null && ea.getPeriod()!=null) {
                String assignment = "<tr><td>Examination Period:</td><td>"+ea.getPeriodNameWithPref()+"</td></tr>";
                if (!ea.getRooms().isEmpty()) {
                    assignment += "<tr><td>Room"+(ea.getRooms().size()>1?"s":"")+":</td><td>";
                    assignment += ea.getRoomsNameWithPref("<br>");
                    assignment += "</td></tr>";
                }
                if (ea.getNrDistributionConflicts()>0) {
                    assignment += "<tr><td>Violated Distributions:</td><td>";
                    assignment += ea.getDistributionConflictTable();
                    assignment += "</td></tr>";
                }
                if (ea.getHasConflicts()) {
                    assignment += "<tr><td>Student Conflicts</td><td>";
                    assignment += ea.getConflictTable();
                    assignment += "</td></tr>";
                }
                if (ea.getHasInstructorConflicts()) {
                    assignment += "<tr><td>Instructor Conflicts</td><td>";
                    assignment += ea.getInstructorConflictTable();
                    assignment += "</td></tr>";
                }
                request.setAttribute("ExamDetail.assignment",assignment);
            }
            
            BackTracker.markForBack(
                    request,
                    "examDetail.do?examId=" + examId,
                    "Exam ("+ (frm.getName()==null?frm.getLabel().trim():frm.getName().trim()) +")",
                    true, false);
            
            // Initialize Preferences for initial load
            frm.setAvailableTimePatterns(null);
            initPrefs(frm, exam, null, false);
            generateExamPeriodGrid(request, frm, exam, "init", timeVertical, false);
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            setupInstructors(request, frm, exam);
            
            LookupTables.setupRooms(request, exam);      // Room Prefs
            LookupTables.setupBldgs(request, exam);      // Building Prefs
            LookupTables.setupRoomFeatures(request, exam); // Preference Levels
            LookupTables.setupRoomGroups(request, exam);   // Room Groups
            
            LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
            
            return mapping.findForward("showExamDetail");
            
        } catch (Exception e) {
            Debug.error(e);
            throw e;
        }
    }
    
    protected void doLoad(HttpServletRequest request, ExamEditForm frm, Exam exam) {
        frm.setExamId(exam.getUniqueId().toString());
        
        frm.setLabel(exam.getLabel());
        frm.setName(exam.generateName().equals(exam.getName())?null:exam.getName());
        frm.setNote(exam.getNote()==null?null:exam.getNote().replaceAll("\n", "<br>"));
        frm.setLength(exam.getLength());
        frm.setSize(String.valueOf(exam.getSize()));
        frm.setPrintOffset(exam.getPrintOffset()==null || exam.getPrintOffset()==0 ? null: (exam.getPrintOffset()>0?"+":"")+exam.getPrintOffset());
        frm.setSeatingType(Exam.sSeatingTypes[exam.getSeatingType()]);
        frm.setMaxNbrRooms(exam.getMaxNbrRooms());
        frm.setExamType(exam.getExamType().getUniqueId());
        frm.setAccommodation(StudentAccomodation.toHtml(StudentAccomodation.getAccommodations(exam)));
        
        TreeSet instructors = new TreeSet(exam.getInstructors());

        for (Iterator i = instructors.iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            frm.getInstructors().add(instr.getUniqueId().toString());
        }
        
        Long nextId = Navigation.getNext(sessionContext, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        Long prevId = Navigation.getPrevious(sessionContext, Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        frm.setPreviousId(prevId==null?null:prevId.toString());
        frm.setNextId(nextId==null?null:nextId.toString());
        
        ExamPeriod avgPeriod = exam.getAveragePeriod();
        frm.setAvgPeriod(avgPeriod==null?null:avgPeriod.getName());
    }

    protected void setupInstructors(HttpServletRequest request, ExamEditForm frm, Exam exam) throws Exception {

        List instructors = frm.getInstructors();
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
        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

        // For each instructor set the instructor list
        for (int i=0; i<instructors.size(); i++) {
            request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
        }
    }
}
