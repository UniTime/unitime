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
package org.unitime.timetable.tags;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.HttpSessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;


/**
 * @author Tomas Muller, Zuzana Mullerova
 */
public class Exams extends BodyTagSupport {
	private static final long serialVersionUID = -666904499562226756L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	private String iType = null;
    private boolean iAdd = true;
    private String iId = null;
    
    public Exams() {
        super();
    }
    
    public SessionContext getSessionContext() {
    	return HttpSessionContext.getSessionContext(pageContext.getServletContext());
    }

    public void setType(String type) {
        iType = type;
    }
    public String getType() {
        return iType;
    }

    public void setAdd(boolean add) {
        iAdd = add;
    }
    public boolean isAdd() {
        return iAdd;
    }

    public void setId(String id) {
        iId = id;
    }
    public String getId() {
        return iId;
    }

    public int doStartTag() throws JspException {
        return EVAL_BODY_BUFFERED;
    }
    
    public int doEndTag() throws JspException {
        try {
        	if (!getSessionContext().hasPermission(Right.Examinations) && !getSessionContext().hasPermission(Right.ExaminationSchedule))
        		return EVAL_PAGE;
        	
            String objectIdStr = (getBodyContent()==null?null:getBodyContent().getString().trim());
            if (objectIdStr==null || objectIdStr.length()==0) objectIdStr = (getId()==null?null:getId().trim());
            if (objectIdStr==null || objectIdStr.length()==0) return EVAL_PAGE;
            Long objectId = Long.parseLong(objectIdStr);
            
            List exams = Exam.findAllRelated(getType(),objectId);
            if (exams==null || exams.isEmpty()) {
                if (!iAdd || !getSessionContext().hasPermission(Right.ExaminationAdd)) return EVAL_PAGE;
            }
            
            String title = (exams.size()==1?MSG.sectionTitleExamination():MSG.sectionTitleExaminations());
            if (iAdd && getSessionContext().hasPermission(Right.ExaminationAdd)) 
                title = "<table width='100%'><tr><td width='100%'>" + 
                    "<DIV class=\"WelcomeRowHeadNoLine\">"+MSG.sectionTitleExaminations()+"</DIV>"+
                    "</td><td style='padding-bottom: 2px'>"+
                    "<input type=\"button\" onclick=\"document.location='examEdit.do?firstType="+getType()+"&firstId="+objectId+"';\" " +
                    		"class=\"btn\" accesskey='"+ MSG.accessAddExamination() +"' title='" + 
                    			MSG.titleAddExamination(MSG.accessAddExamination())+"' value='" + MSG.actionAddExamination()+"'>"+
                    "</td></tr></table>";
            
            WebTable table = new WebTable(10, title,
                    new String[] { MSG.columnExamClassesCourses(), MSG.columnExamType(), 
            			MSG.columnExamLength(), MSG.columnExamSeatingType(), MSG.columnExamSize(), 
            			MSG.columnExamMaxRooms(), MSG.columnExamInstructor(), 
            			MSG.columnExamPeriodPreferences(), MSG.columnExamRoomPreferences(),
            			MSG.columnExamDistributionPreferences()},
                    new String[] {"left", "left", "right", "center", "right", "right", "left", 
                        "left", "left", "left"},
                        new boolean[] {true, true, true, true, true, true, true, true, true}
                    );

            boolean timeVertical = CommonValues.VerticalGrid.eq(getSessionContext().getUser().getProperty(UserProperty.GridOrientation));
            boolean timeText = CommonValues.TextGrid.eq(getSessionContext().getUser().getProperty(UserProperty.GridOrientation));
            String instructorNameFormat = getSessionContext().getUser().getProperty(UserProperty.NameFormat);
            
            String backId = null;
            if ("PreferenceGroup".equals(pageContext.getRequest().getParameter("backType")))
                backId = pageContext.getRequest().getParameter("backId");
            if (pageContext.getRequest().getAttribute("examId")!=null)
                backId = pageContext.getRequest().getAttribute("examId").toString(); 
            boolean hasExamHash = false;
            
            if (exams!=null)  {
                ExamSolverProxy solver = WebSolver.getExamSolver(pageContext.getSession());
                Long solverType = (solver == null ? null : solver.getExamTypeId());
                
                boolean hasSolution = false;
                for (Iterator i=new TreeSet(exams).iterator();i.hasNext();) {
                    Exam exam = (Exam)i.next();
                    ExamAssignment assignment = null;
                    if (exam.getExamType().getUniqueId().equals(solverType))
                        assignment = solver.getAssignment(exam.getUniqueId());
                    else if (exam.getAssignedPeriod()!=null)
                        assignment = new ExamAssignment(exam);
                    if (assignment!=null && assignment.getPeriodId()!=null) {
                        hasSolution = true; break;
                    }
                }
                if (getSessionContext().hasPermission(Right.Examinations)) {
                    if (hasSolution)
                        table = new WebTable(10, title,
                                new String[] { MSG.columnExamClassesCourses(), MSG.columnExamType(), 
                        		MSG.columnExamLength(), MSG.columnExamSeatingType(), MSG.columnExamSize(),
                        		MSG.columnExamMaxRooms(), MSG.columnExamInstructor(), 
                                MSG.columnExamAssignedPeriod(), MSG.columnExamAssignedRoom(), 
                                MSG.columnExamStudentConflicts()},
                            new String[] {"left", "left", "right", "center", "right", "right", "left", 
                                "left", "left", "left"},
                                new boolean[] {true, true, true, true, true, true, true, true, true}
                            );
                
                    for (Iterator i=new TreeSet(exams).iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                        boolean view = getSessionContext().hasPermission(exam, Right.ExaminationDetail);
                    
                        String objects = "", instructors = "", perPref = "", roomPref = "", distPref = "";
                        
                        ExamAssignmentInfo assignment = null;
                        if (exam.getExamType().getUniqueId().equals(solverType))
                            assignment = solver.getAssignmentInfo(exam.getUniqueId());
                        else if (exam.getAssignedPeriod()!=null)
                            assignment = new ExamAssignmentInfo(exam);
                        
                        for (Enumeration e=exam.getOwnerObjects().elements();e.hasMoreElements();) {
                            Object object = e.nextElement();
                            if (objects.length()>0) objects+="<br>";
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
                        
                        if (!hasSolution || assignment==null || assignment.getPeriodId()==null) {
                            roomPref += exam.getEffectivePrefHtmlForPrefType(RoomPref.class);
                            if (roomPref.length()>0 && !roomPref.endsWith("<br>")) roomPref+="<br>";
                            roomPref += exam.getEffectivePrefHtmlForPrefType(BuildingPref.class);
                            if (roomPref.length()>0 && !roomPref.endsWith("<br>")) roomPref+="<br>";
                            roomPref += exam.getEffectivePrefHtmlForPrefType(RoomFeaturePref.class);
                            if (roomPref.length()>0 && !roomPref.endsWith("<br>")) roomPref+="<br>";
                            roomPref += exam.getEffectivePrefHtmlForPrefType(RoomGroupPref.class);
                            if (roomPref.endsWith("<br>")) roomPref = roomPref.substring(0, roomPref.length()-"<br>".length());
                            if (exam.getExamType().getType() == ExamType.sExamTypeMidterm) {
                                MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(exam.getSession(), exam.getExamType());
                                epx.load(exam);
                                perPref+=epx.toString(true);
                            } else if (timeText) {
                            	perPref += exam.getEffectivePrefHtmlForPrefType(ExamPeriodPref.class);
                            } else {
                                PeriodPreferenceModel px = new PeriodPreferenceModel(exam.getSession(), exam.getExamType().getUniqueId());
                                px.load(exam);
                                perPref = "<img border='0' src='pattern?v=" + (timeVertical ? 1 : 0) + "&x="+exam.getUniqueId()+"' title='"+px.toString()+"'>";
                            }
                            if (!hasSolution)
                                distPref += exam.getEffectivePrefHtmlForPrefType(DistributionPref.class);
                            else
                                distPref = "<i>Not Assigned</i>";
                        } else {
                            perPref = (view?assignment.getPeriodAbbreviationWithPref():assignment.getPeriodAbbreviation());
                            roomPref = (view?assignment.getRoomsNameWithPref("<br>"):assignment.getRoomsName("<br>")); 
                            int dc = assignment.getNrDirectConflicts();
                            String dcStr = (dc<=0?"<font color='gray'>0</font>":"<font color='"+PreferenceLevel.prolog2color("P")+"'>"+dc+"</font>");
                            int m2d = assignment.getNrMoreThanTwoConflicts();
                            String m2dStr = (m2d<=0?"<font color='gray'>0</font>":"<font color='"+PreferenceLevel.prolog2color("2")+"'>"+m2d+"</font>");
                            int btb = assignment.getNrBackToBackConflicts();
                            int dbtb = assignment.getNrDistanceBackToBackConflicts();
                            String btbStr = (btb<=0 && dbtb<=0?"<font color='gray'>0</font>":"<font color='"+PreferenceLevel.prolog2color("1")+"'>"+btb+(dbtb>0?" (d:"+dbtb+")":"")+"</font>");
                            distPref = (view?dcStr+", "+m2dStr+", "+btbStr:"<i>N/A</i>"); 
                        }
                        
                        for (Iterator j=new TreeSet(exam.getInstructors()).iterator();j.hasNext();) {
                            DepartmentalInstructor instructor = (DepartmentalInstructor)j.next();
                            if (instructors.length()>0) instructors+="<br>";
                            instructors += instructor.getName(instructorNameFormat);
                        }
                        
                        int nrStudents = exam.getSize();
                        
                        if (exam.getUniqueId().toString().equals(backId)) {
                            objects = "<A name='examHash'>"+objects+"</A>";
                            hasExamHash = true;
                        }
                        
                        table.addLine(
                                (view ? "onClick=\"document.location='examDetail.do?examId="+exam.getUniqueId()+"';\"":null),
                                new String[] {
                                    objects,
                                    exam.getExamType().getLabel(),
                                    exam.getLength().toString(),
                                    (Exam.sSeatingTypeNormal==exam.getSeatingType()?MSG.examSeatingTypeNormal():MSG.examSeatingTypeExam()),
                                    String.valueOf(nrStudents),
                                    exam.getMaxNbrRooms().toString(),
                                    instructors,
                                    perPref,
                                    roomPref,
                                    distPref
                                },
                                null,
                                exam.getUniqueId().toString());
                    }
                } else {
                    if (!hasSolution) return EVAL_PAGE;
                    table = new WebTable(5, title,
                            new String[] { MSG.columnExamClassesCourses(), MSG.columnExamType(), 
                    			MSG.columnExamInstructor(), MSG.columnExamPeriod(), MSG.columnExamRoom()},
                            new String[] {"left", "left", "left", "left", "left"},
                            new boolean[] {true, true, true, true, true}
                        );
                    
                    for (Iterator i=new TreeSet(exams).iterator();i.hasNext();) {
                        Exam exam = (Exam)i.next();
                    
                        String objects = "", instructors = "", perPref = "", roomPref = "";
                        
                        ExamAssignmentInfo assignment = null;
                        if (exam.getExamType().getUniqueId().equals(solverType))
                            assignment = solver.getAssignmentInfo(exam.getUniqueId());
                        else if (exam.getAssignedPeriod()!=null)
                            assignment = new ExamAssignmentInfo(exam);
                        
                        for (Enumeration e=exam.getOwnerObjects().elements();e.hasMoreElements();) {
                            Object object = e.nextElement();
                            if (objects.length()>0) objects+="<br>";
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
                        
                        if (assignment==null || assignment.getPeriodId()==null) continue;

                        perPref = assignment.getPeriodName();
                        roomPref = assignment.getRoomsName("<br>"); 
                        
                        for (Iterator j=new TreeSet(exam.getInstructors()).iterator();j.hasNext();) {
                            DepartmentalInstructor instructor = (DepartmentalInstructor)j.next();
                            if (instructors.length()>0) instructors+="<br>";
                            instructors += instructor.getName(instructorNameFormat);
                        }
                        
                        if (exam.getUniqueId().toString().equals(backId)) {
                            objects = "<A name='examHash'>"+objects+"</A>";
                            hasExamHash = true;
                        }
                        
                        table.addLine(
                                null,
                                new String[] {
                                    objects,
                                    exam.getExamType().getLabel(),
                                    instructors,
                                    perPref,
                                    roomPref},
                                null,
                                exam.getUniqueId().toString());
                    }
                }
            }
            
            pageContext.getOut().println("<table width='100%' border='0' cellspacing='0' cellpadding='3'>");
            if ("Exam".equals(pageContext.getRequest().getParameter("backType"))) {
                pageContext.getOut().println("<tr><td colpan='9'><A name='examHash'>&nbsp;</A></td></tr>");
                hasExamHash = true;
            } else 
                pageContext.getOut().println("<tr><td colpan='9'>&nbsp;</td></tr>");
            pageContext.getOut().println(table.printTable());
            pageContext.getOut().println("</table>");
            if (hasExamHash)
                pageContext.getOut().println("<SCRIPT type='text/javascript' language='javascript'>location.hash = 'examHash';</SCRIPT>");
        } catch (Exception e) {
            Debug.error(e);
            try {
                pageContext.getOut().print("<font color='red'>ERROR: "+e.getMessage()+"</font>");
            } catch (IOException io) {}
        }
        
        return EVAL_PAGE;
    }
    
}
