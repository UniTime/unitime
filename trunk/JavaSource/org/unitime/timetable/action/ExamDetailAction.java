package org.unitime.timetable.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamAssignmentProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.ExamDistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.RequiredTimeTable;

public class ExamDetailAction extends PreferencesAction {
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamEditForm frm = (ExamEditForm) form;
        try {
            
            // Set common lookup tables
            super.execute(mapping, form, request, response);
            
            HttpSession httpSession = request.getSession();
            User user = Web.getUser(httpSession);
            Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
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
            
            if (request.getAttribute("fromChildScreen")!=null
                    && request.getAttribute("fromChildScreen").toString().equals("true") ) {
                op = "";
                frm.setOp(op);
            }
            // Read exam id from form
            if (op.equals(rsc.getMessage("button.editExam")) || op.equals(rsc.getMessage("button.addDistPref")) || op.equals(rsc.getMessage("button.nextExam")) || op.equals(rsc.getMessage("button.previousExam"))) {
                examId = frm.getExamId();
            } else {
                frm.reset(mapping, request);
            }
            
            //Check op exists
            if (op==null) throw new Exception ("Null Operation not supported.");
            
            Debug.debug("op: " + op);
            Debug.debug("exam: " + examId);
            
            //Check exam exists
            if (examId==null || examId.trim()=="") throw new Exception ("Exam Info not supplied.");
            
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

            //Edit Information - Redirect to info edit screen
            if (op.equals(rsc.getMessage("button.editExam")) && examId!=null && examId.trim()!="") {
                response.sendRedirect( response.encodeURL("examEdit.do?examId="+examId) );
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
                org.hibernate.Session hibSession = new ExamDAO().getSession();
                Transaction tx = null;
                try {
                    tx = hibSession.beginTransaction();
                    ChangeLog.addChange(
                            hibSession, 
                            request,
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
                request.setAttribute("examId", examId);
                return mapping.findForward("addDistributionPrefs");
            }

            
            // Load form attributes that are constant
            doLoad(request, frm, exam);
            
            // Display distribution Prefs
            ExamDistributionPrefsTableBuilder tbl = new ExamDistributionPrefsTableBuilder();
            String html = tbl.getDistPrefsTable(request, exam);
            if (html!=null)
                request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
            
            if (!exam.getOwners().isEmpty()) {
                WebTable table = new WebTable(4, null, new String[] {"Object", "Type", "Manager", "Students", "Assignment"}, new String[] {"left", "center", "left", "center", "left"}, new boolean[] {true, true, true, true, true});
                for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();) {
                    ExamOwner owner = (ExamOwner)i.next();
                    String onclick = null, name = null, type = null, students = String.valueOf(owner.countStudents()), manager = null, assignment = null;
                    switch (owner.getOwnerType()) {
                        case ExamOwner.sOwnerTypeClass :
                            Class_ clazz = (Class_)owner.getOwnerObject();
                            if (clazz.isViewableBy(user))
                                onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
                            name = clazz.getClassLabel();
                            type = "Class";
                            manager = clazz.getManagingDept().getShortLabel();
                            if (clazz.getCommittedAssignment()!=null)
                                assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
                            break;
                        case ExamOwner.sOwnerTypeConfig :
                            InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                            if (config.isViewableBy(user))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = config.getCourseName()+" ["+config.getName()+"]";
                            type = "Configuration";
                            manager = config.getInstructionalOffering().getControllingCourseOffering().getDepartment().getShortLabel();
                            break;
                        case ExamOwner.sOwnerTypeOffering :
                            InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                            if (offering.isViewableBy(user))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
                            name = offering.getCourseName();
                            type = "Offering";
                            manager = offering.getControllingCourseOffering().getDepartment().getShortLabel();
                            break;
                        case ExamOwner.sOwnerTypeCourse :
                            CourseOffering course = (CourseOffering)owner.getOwnerObject();
                            if (course.isViewableBy(user))
                                onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = course.getCourseName();
                            type = "Course";
                            manager = course.getDepartment().getShortLabel();
                            break;
                                
                    }
                    table.addLine(onclick, new String[] { name, type, manager, students, assignment}, null);
                }
                request.setAttribute("ExamDetail.table",table.printTable());
            }
            
            ExamAssignmentInfo ea = null;
            
            ExamAssignmentProxy examAssignment = WebSolver.getExamSolver(request.getSession());
            if (examAssignment!=null) {
                ea = examAssignment.getAssignmentInfo(exam.getUniqueId());
            } else if (exam.getAssignedPeriod()!=null)
                ea = new ExamAssignmentInfo(exam);
            
            if (ea!=null) {
                String assignment = "<tr><td>Examination Period:</td><td>"+ea.getPeriodNameWithPref()+"</td></tr>";
                if (!ea.getRoomIds().isEmpty()) {
                    assignment += "<tr><td>Room"+(ea.getRoomIds().size()>1?"s":"")+":</td><td>";
                    assignment += ea.getRoomsNameWithPref("<br>");
                    assignment += "</td></tr>";
                }
                if (ea.hasConflicts()) {
                    assignment += "<tr><td>Conflicts</td><td>";
                    assignment += ea.getConflictTable(true);
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
            initPrefs(user, frm, exam, null, false);
            boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));
            generateExamPeriodGrid(request, frm, exam, "init", timeVertical, false);
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            setupInstructors(request, frm, exam);
            
            LookupTables.setupRooms(request, exam);      // Room Prefs
            LookupTables.setupBldgs(request, exam);      // Building Prefs
            LookupTables.setupRoomFeatures(request, exam); // Preference Levels
            LookupTables.setupRoomGroups(request, exam);   // Room Groups
            
            return mapping.findForward("showExamDetail");
            
        } catch (Exception e) {
            Debug.error(e);
            throw e;
        }
    }
    
    protected void doLoad(HttpServletRequest request, ExamEditForm frm, Exam exam) {
        frm.setExamId(exam.getUniqueId().toString());
        
        frm.setLabel(exam.getLabel());
        frm.setName(exam.getName());
        frm.setNote(exam.getNote()==null?null:exam.getNote().replaceAll("\n", "<br>"));
        frm.setLength(exam.getLength());
        frm.setSeatingType(Exam.sSeatingTypes[exam.getSeatingType()]);
        frm.setMaxNbrRooms(exam.getMaxNbrRooms());
        frm.setExamType(exam.getExamType());
        
        TreeSet instructors = new TreeSet(exam.getInstructors());

        for (Iterator i = instructors.iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            frm.getInstructors().add(instr.getUniqueId().toString());
        }
        
        User user = Web.getUser(request.getSession());

        frm.setEditable(exam.isEditableBy(user));

        Long nextId = Navigation.getNext(request.getSession(), Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        Long prevId = Navigation.getPrevious(request.getSession(), Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
        frm.setPreviousId(prevId==null?null:prevId.toString());
        frm.setNextId(nextId==null?null:nextId.toString());
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

        LookupTables.setupInstructors(request, deptsIdsArray);
        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

        // For each instructor set the instructor list
        for (int i=0; i<instructors.size(); i++) {
            request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
        }
    }
}
