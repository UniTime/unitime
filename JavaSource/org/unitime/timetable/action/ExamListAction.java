package org.unitime.timetable.action;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
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
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamListForm;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.PdfWebTable;
import org.unitime.timetable.webutil.RequiredTimeTable;

public class ExamListAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamListForm myForm = (ExamListForm) form;
        
        User user = Web.getUser(request.getSession()); 
        TimetableManager manager = (user==null?null:TimetableManager.getManager(user)); 
        Session session = (user==null?null:Session.getCurrentAcadSession(user));
        if (user==null || session==null || !manager.canSeeExams(session, user)) throw new Exception ("Access Denied.");
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (op==null && request.getSession().getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME)!=null) {
            myForm.setSubjectAreaId((String)request.getSession().getAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME));
            myForm.setCourseNbr((String)request.getSession().getAttribute(Constants.CRS_NBR_ATTR_NAME));
        }

        WebTable.setOrder(request.getSession(), "ExamList.ord", request.getParameter("ord"), 1);

        if ("Search".equals(op) || "Export PDF".equals(op)) {
            if (myForm.getSubjectAreaId()!=null) {
                request.getSession().setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, myForm.getSubjectAreaId());
                request.getSession().setAttribute(Constants.CRS_NBR_ATTR_NAME, myForm.getCourseNbr());
            }
            
            if ("Export PDF".equals(op)) {
                PdfWebTable table = getExamTable(user, manager, session, myForm, false);
                if (table!=null) {
                    File file = ApplicationProperties.getTempFile("exams", "pdf");
                    table.exportPdf(file, WebTable.getOrder(request.getSession(), "ExamList.ord"));
                    request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
                }

            }
        }
        
        if ("Add Examination".equals(op)) {
            return mapping.findForward("addExam");
        }
        
        myForm.setSubjectAreas(new TreeSet(SubjectArea.getSubjectAreaList(session.getUniqueId())));
        
        if (myForm.getSubjectAreaId()!=null && myForm.getSubjectAreaId().length()!=0) {
            PdfWebTable table = getExamTable(user, manager, session, myForm, true);
            if (table!=null) {
                request.setAttribute("ExamList.table", table.printTable(WebTable.getOrder(request.getSession(), "ExamList.ord")));
                Vector ids = new Vector();
                for (Enumeration e=table.getLines().elements();e.hasMoreElements();) {
                    WebTable.WebTableLine line = (WebTable.WebTableLine)e.nextElement();
                    ids.add(Long.parseLong(line.getUniqueId()));
                }
                Navigation.set(request.getSession(), Navigation.sInstructionalOfferingLevel, ids);
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
        
        BackTracker.markForBack(
                request, 
                "examList.do?op=Search&subjectAreaId="+myForm.getSubjectAreaId()+"&courseNbr="+myForm.getCourseNbr(), 
                "Exams ("+(Constants.ALL_OPTION_VALUE.equals(myForm.getSubjectAreaId())?"All":subjectAreaName+
                    (myForm.getCourseNbr()==null || myForm.getCourseNbr().length()==0?"":" "+myForm.getCourseNbr()))+
                    ")", 
                true, true);

        return mapping.findForward("list");
    }
    
    public PdfWebTable getExamTable(User user, TimetableManager manager, Session session, ExamListForm form, boolean html) {
        Collection exams = (Constants.ALL_OPTION_VALUE.equals(form.getSubjectAreaId())?Exam.findAll(session.getUniqueId()):Exam.findExamsOfCourse(Long.valueOf(form.getSubjectAreaId()), form.getCourseNbr()));
        
        if (exams.isEmpty()) return null;
        
        String nl = (html?"<br>":"\n");
        
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
        boolean timeText = RequiredTimeTable.getTimeGridAsText(user);
        
        PdfWebTable table = new PdfWebTable(
                11,
                "Examinations", "examList.do?ord=%%",
                new String[] {"Classes / Courses", "Length", "Seating"+nl+"Type", "Students", "Max"+nl+"Rooms", 
                        "Instructor", "Period"+nl+"Preferences", "Room"+nl+"Preferences", "Distribution"+nl+"Preferences",
                        "Assigned"+nl+"Period", "Assigned"+nl+"Room"},
                new String[] {"left", "right", "center", "right", "right", "left", 
                        "left", "left", "left", "left", "left"},
                new boolean[] {true, true, true, true, true, true, true, true, true, true}
                );
        
        String instructorNameFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);

        
        for (Iterator i=exams.iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            String objects = "", instructors = "", perPref = "", roomPref = "", distPref = "", per = "", rooms = "";
            
            for (Enumeration e=exam.getOwnerObjects().elements();e.hasMoreElements();) {
                Object object = e.nextElement();
                if (objects.length()>0) objects+=nl;
                if (object instanceof Class_)
                    objects += ((Class_)object).getClassLabel();
                else if (object instanceof InstrOfferingConfig)
                    objects += ((InstrOfferingConfig)object).toString();
                else if (object instanceof InstructionalOffering)
                    objects += ((InstructionalOffering)object).getCourseName();
                else if (object instanceof CourseOffering)
                    objects += ((CourseOffering)object).getCourseName();
                else
                    objects += object.toString();
            }
            
            if (html) {
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomPref.class);
                if (roomPref.length()>0) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(BuildingPref.class);
                if (roomPref.length()>0) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
                if (roomPref.length()>0) roomPref+=nl;
                roomPref += exam.getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
                if (roomPref.endsWith(nl)) roomPref = roomPref.substring(0, roomPref.length()-nl.length());
                if (timeText) {
                    perPref += exam.getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                } else {
                    PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession());
                    px.load(exam);
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
                        perPref += exam.getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                }
                distPref += exam.getEffectivePrefHtmlForPrefType(DistributionPref.class);
            } else {
                for (Iterator j=exam.effectivePreferences(RoomPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(BuildingPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomFeaturePref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(RoomGroupPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (roomPref.length()>0) roomPref+=nl;
                    roomPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(ExamPeriodPref.class).iterator();j.hasNext();) {
                    Preference pref = (Preference)j.next();
                    if (perPref.length()>0) perPref+=nl;
                    perPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText();
                }
                for (Iterator j=exam.effectivePreferences(DistributionPref.class).iterator();j.hasNext();) {
                    DistributionPref pref = (DistributionPref)j.next();
                    if (distPref.length()>0) distPref+=nl;
                    distPref += PreferenceLevel.prolog2abbv(pref.getPrefLevel().getPrefProlog())+" "+pref.preferenceText(true, true, " (", ", ",")").replaceAll("&lt;","<").replaceAll("&gt;",">");
                }
            }
            
            for (Iterator j=new TreeSet(exam.getInstructors()).iterator();j.hasNext();) {
                DepartmentalInstructor instructor = (DepartmentalInstructor)j.next();
                if (instructors.length()>0) instructors+=nl;
                instructors += instructor.getName(instructorNameFormat);
            }
            
            if (exam.getAssignedPeriod()!=null) {
                per = exam.getAssignedPeriod().getAbbreviation();
            }
            
            for (Iterator j=new TreeSet(exam.getAssignedRooms()).iterator();j.hasNext();) {
                Location location = (Location)j.next();
                if (rooms.length()>0) rooms+=nl;
                rooms += location.getLabel();
            }
            
            int nrStudents = exam.getStudents().size();
            
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
                        (exam.getAssignedPeriod()==null?new Date(0):exam.getAssignedPeriod().getStartTime()),
                        rooms
                    },
                    exam.getUniqueId().toString());
        }
        
        return table;
                
    }
}
