package org.unitime.timetable.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.RequiredTimeTable;

public class ExamEditAction extends PreferencesAction {
    
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
            
            // Read parameters
            String examId = request.getParameter("examId");
            String op = frm.getOp();            
            String reloadCause = request.getParameter("reloadCause");
            String deleteType = request.getParameter("deleteType");
            
            // Read subpart id from form
            if(op.equals(rsc.getMessage("button.reload"))
                    || op.equals(rsc.getMessage("button.addObject"))
                    || op.equals(rsc.getMessage("button.addPeriod"))
                    || op.equals(rsc.getMessage("button.addRoomPref"))
                    || op.equals(rsc.getMessage("button.addBldgPref"))
                    || op.equals(rsc.getMessage("button.addRoomFeaturePref"))
                    || op.equals(rsc.getMessage("button.addDistPref")) 
                    || op.equals(rsc.getMessage("button.addRoomGroupPref"))
                    || op.equals(rsc.getMessage("button.addInstructor"))
                    || op.equals(rsc.getMessage("button.updateExam")) 
                    || op.equals(rsc.getMessage("button.cancel")) 
                    || op.equals(rsc.getMessage("button.clearExamPrefs"))                 
                    || op.equals(rsc.getMessage("button.delete"))
                    || op.equals(rsc.getMessage("button.saveExam"))
                    || op.equals(rsc.getMessage("button.deleteExam"))
                    || op.equals(rsc.getMessage("button.addExam"))
                    || op.equals(rsc.getMessage("button.returnToDetail"))
                    || op.equals(rsc.getMessage("button.nextExam"))
                    || op.equals(rsc.getMessage("button.previousExam"))) {
                examId = frm.getExamId();
            }
            
            // Determine if initial load
            if(op==null || op.trim().length()==0 
                    || ( op.equals(rsc.getMessage("button.reload")) 
                         && (reloadCause==null || reloadCause.trim().length()==0) )) {
                op = "init";
            }
            
            // Check op exists
            if(op==null || op.trim()=="") 
                throw new Exception ("Null Operation not supported.");
            
            Exam exam = (examId==null || examId.trim().length()==0 ? null: new ExamDAO().get(Long.valueOf(examId)));
            
            // Cancel - Go back to Instructors Detail Screen
            if(op.equals(rsc.getMessage("button.returnToDetail"))) {
                if (examId!=null && examId.trim()!="") {
                    request.setAttribute("examId", examId);
                    request.setAttribute("fromChildScreen", "true");
                    return mapping.findForward("showDetail");
                } else {
                    return mapping.findForward("showList");
                }
            }
            
            // Clear all preferences
            if(exam!=null && op.equals(rsc.getMessage("button.clearExamPrefs"))) { 
                Set s = exam.getPreferences();
                s.clear();
                exam.setPreferences(s);            
                new ExamDAO().update(exam);
                op = "init";                
                request.setAttribute("examId", examId);
                
                ChangeLog.addChange(
                        null, 
                        request,
                        exam, 
                        ChangeLog.Source.EXAM_EDIT, 
                        ChangeLog.Operation.CLEAR_PREF,
                        exam.firstSubjectArea(),
                        exam.firstDepartment());
                
                return mapping.findForward("showDetail");
            }
            
            // Reset form for initial load
            if(op.equals("init")) { 
                frm.reset(mapping, request);
                // Load form attributes that are constant
                doLoad(request, frm, exam);
            }
            
            frm.setLabel(exam==null?"New Exam":exam.getLabel());
            
            if (op.equals(rsc.getMessage("button.addInstructor"))) {
                List lst = frm.getInstructors();
                for (int i=0; i<frm.PREF_ROWS_ADDED; i++) {
                    frm.getInstructors().add(Preference.BLANK_PREF_VALUE);
                }
            }
            
            if (op.equals(rsc.getMessage("button.addObject"))) {
                for (int i=0; i<frm.PREF_ROWS_ADDED; i++) {
                    frm.addExamOwner(null);
                }
                request.setAttribute("hash", "objects");
            }
            
            int deleteId = -1;
            try {
                deleteId = Integer.parseInt(request.getParameter("deleteId"));
            } catch(Exception e) { deleteId = -1; }
           
            if ("instructor".equals(deleteType)  && deleteId>=0) {
                frm.getInstructors().remove(deleteId);
            } else if ("objects".equals(deleteType)  && deleteId>=0) {
                frm.deleteExamOwner(deleteId);
            } else if ("exam".equals(deleteType) && exam!=null) {
                ChangeLog.addChange(
                        null, 
                        request,
                        exam, 
                        ChangeLog.Source.EXAM_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        exam.firstSubjectArea(), 
                        exam.firstDepartment());
                new ExamDAO().delete(exam);
                return mapping.findForward("showList");
            }
            
            if(op.equals(rsc.getMessage("button.updateExam")) ||  op.equals(rsc.getMessage("button.saveExam"))
                     || op.equals(rsc.getMessage("button.nextExam")) || op.equals(rsc.getMessage("button.previousExam"))
                    ) {  
                // Validate input prefs
                errors = frm.validate(mapping, request);
                
                // No errors - save
                if(errors.size()==0) {
                    doUpdate(request, frm, exam);
                    
                    request.setAttribute("examId", frm.getExamId());
                    request.setAttribute("fromChildScreen", "true");
                    
                    if (op.equals(rsc.getMessage("button.nextExam")))
                        response.sendRedirect(response.encodeURL("examEdit.do?examId="+frm.getNextId()));
                    
                    if (op.equals(rsc.getMessage("button.previousExam")))
                        response.sendRedirect(response.encodeURL("examEdit.do?examId="+frm.getPreviousId()));
                    
                    //response.sendRedirect(response.encodeURL("examDetail.do?examId="+examId));
                    
                    return mapping.findForward("showDetail");
                }
                else {
                    saveErrors(request, errors);
                }
            }
            
            // Initialize Preferences for initial load 
            frm.setAvailableTimePatterns(null);
            if(op.equals("init")) {
                initPrefs(user, frm, exam, null, true);
            }
            boolean timeVertical = RequiredTimeTable.getTimeGridVertical(Web.getUser(httpSession));
            generateExamPeriodGrid(request, frm, exam, op, timeVertical, true);
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            setupInstructors(request, frm, exam);
            
            if (exam!=null) {
                LookupTables.setupRooms(request, exam);      // Room Prefs
                LookupTables.setupBldgs(request, exam);      // Building Prefs
                LookupTables.setupRoomFeatures(request, exam); // Preference Levels
                LookupTables.setupRoomGroups(request, exam);   // Room Groups
            } else {
                Exam dummy = new Exam(); dummy.setSession(Session.getCurrentAcadSession(user));
                LookupTables.setupRooms(request, dummy);      // Room Prefs
                LookupTables.setupBldgs(request, dummy);      // Building Prefs
                LookupTables.setupRoomFeatures(request, dummy); // Preference Levels
                LookupTables.setupRoomGroups(request, dummy);   // Room Groups
            }
            
            frm.setSubjectAreas(TimetableManager.getSubjectAreas(user));
        
            if (exam!=null) {
                BackTracker.markForBack(
                    request,
                    "examDetail.do?examId="+frm.getExamId(),
                    "Exam ("+ (frm.getName()==null || frm.getName().length()==0?frm.getLabel().trim():frm.getName().trim()) +")",
                    true, false);
            }

            return (exam==null?mapping.findForward("showAdd"):mapping.findForward("showEdit"));
            
        } catch (Exception e) {
            Debug.error(e);
            throw e;
        }
    }
    
    protected void doLoad(HttpServletRequest request, ExamEditForm frm, Exam exam) {
        if (exam!=null) {
            frm.setExamId(exam.getUniqueId().toString());
            
            frm.setName(exam.getName());
            frm.setNote(exam.getNote());
            frm.setLength(exam.getLength());
            frm.setSeatingType(Exam.sSeatingTypes[exam.getSeatingType()]);
            frm.setMaxNbrRooms(exam.getMaxNbrRooms());
            
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
            
            for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();)
                frm.addExamOwner((ExamOwner)i.next());
        } else {
            try {
                TreeSet periods = ExamPeriod.findAll(request);
                if (!periods.isEmpty())
                    frm.setLength(Constants.SLOT_LENGTH_MIN*((ExamPeriod)periods.first()).getLength());
            } catch (Exception e) {}
        }
        
        for (int i=0;i<frm.PREF_ROWS_ADDED;i++) {
            frm.addExamOwner(null);
            frm.getInstructors().add(Constants.BLANK_OPTION_VALUE);
        }
    }

    protected void setupInstructors(HttpServletRequest request, ExamEditForm frm, Exam exam) throws Exception {

        List instructors = frm.getInstructors();
        if(instructors.size()==0) return;
        
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
            for (int i=0;i<frm.getSubjectAreaList().size();i++) {
                ExamOwner own = frm.getExamOwner(i);
                if (own!=null) deptIds.add(own.getCourse().getDepartment().getUniqueId());
            }
            if (deptIds.isEmpty()) {
                for (Iterator i = TimetableManager.getManager(Web.getUser(request.getSession())).getDepartments().iterator();i.hasNext();) {
                    Department dept = (Department)i.next();
                    deptIds.add(dept.getUniqueId());
                }
            }
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
    
    protected void doUpdate(HttpServletRequest request, ExamEditForm frm, Exam exam) throws Exception {
        boolean add = false;
        if (exam==null) {
            add = true;
            exam = new Exam();
            exam.setSession(Session.getCurrentAcadSession(Web.getUser(request.getSession())));
        }
        
        Set s = exam.getPreferences();
        if (s==null) s = new HashSet();
        
        // Clear all old prefs
        s.clear();                

        super.doUpdate(request, frm, exam, s, false);
        
        exam.setName(frm.getName()==null || frm.getName().trim().length()==0?null:frm.getName().trim());
        exam.setNote(frm.getNote());
        exam.setSeatingType(frm.getSeatingTypeIdx());
        exam.setLength(Integer.valueOf(frm.getLength()));
        exam.setMaxNbrRooms(Integer.valueOf(frm.getMaxNbrRooms()));
        
        if (exam.getInstructors()==null) exam.setInstructors(new HashSet());
        exam.getInstructors().clear();
        for (Iterator i=frm.getInstructors().iterator();i.hasNext();) {
            String instructorId = (String)i.next();
            if (!Constants.BLANK_OPTION_VALUE.equals(instructorId) && !Preference.BLANK_PREF_VALUE.equals(instructorId)) {
                DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(Long.valueOf(instructorId));
                if (instructor!=null) exam.getInstructors().add(instructor);
           }
        }
        
        frm.setExamOwners(exam);
        
        new ExamDAO().saveOrUpdate(exam);

        ChangeLog.addChange(
                null, 
                request,
                exam, 
                ChangeLog.Source.EXAM_EDIT, 
                (add?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                exam.firstSubjectArea(), 
                exam.firstDepartment());

        if (add) frm.setExamId(exam.getUniqueId().toString());
    }
}
