/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.form;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import net.sf.cpsolver.coursett.model.TimeLocation;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.dom4j.Element;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.IdValue;


/** 
 * @author Tomas Muller
 */
public class SectioningDemoForm extends ActionForm {
    public static final Long sTypeNone = new Long(0);
    public static final Long sTypeCourse = new Long(1);
    public static final Long sTypeFreeTime = new Long(2);
    private static DecimalFormat sTwoNumbersDF = new DecimalFormat("00"); 

    private String iOp;
    private String iRequestFile = null;
    private String iResponseFile = null;
    private int iNrRequests = 5;
    private int iNrAltRequests = 3;
    private String iStudentId;
    private boolean iStudentLoaded = false;

    private Vector iRequests = new Vector();
    private Vector iMessages = new Vector();
    private Vector iCourseAssignments = new Vector();
    
    private Vector iTimePatterns = new Vector();
    private Vector iSubjectAreas = new Vector();
    
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();        
        
        return errors;
    }


    public void reset(ActionMapping mapping, HttpServletRequest request) {
        iRequests.clear();
        iCourseAssignments.clear();
        iMessages.clear();
        iNrRequests = (request.getParameter("nrRequests")==null?3:Integer.parseInt(request.getParameter("nrRequests"))); 
        iNrAltRequests = (request.getParameter("nrAltRequests")==null?0:Integer.parseInt(request.getParameter("nrAltRequests")));
        iStudentId = null;
        iStudentLoaded = false;
        for (int i=0;i<iNrRequests;i++)
            iRequests.add(new RequestBean());
        for (int i=0;i<iNrAltRequests;i++)
            iRequests.add(new RequestBean());
        iOp = null;
        iRequestFile = iResponseFile = null;

        iTimePatterns.clear();
        try {
            for (Iterator i=TimePattern.findAll(request, Boolean.TRUE).iterator();i.hasNext();) {
                TimePattern t = (TimePattern)i.next();
                if (t.getType().intValue()==TimePattern.sTypeStandard) 
                    iTimePatterns.add(new IdValue(t.getUniqueId(),t.getName()));
            }
        } catch (Exception e){}
        iSubjectAreas.clear();
        try {
            Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
            Vector v = new Vector();
            for (Iterator i=session.getSubjectAreas().iterator();i.hasNext();) {
                SubjectArea sa = (SubjectArea)i.next();
                iSubjectAreas.add(new IdValue(sa.getUniqueId(), sa.getSubjectAreaAbbreviation()));
            }
        } catch (Exception e){}
    }

    public String getOp() { return iOp; }
    public void setOp(String op) { iOp = op; }
    public Collection getTimePatterns() { return iTimePatterns; }
    public Collection getSubjectAreas() { return iSubjectAreas; }
    
    public static Collection getCourseNumbers(String subjectAreaId) {
        if (subjectAreaId==null || subjectAreaId.length()==0) return new Vector();
        return new InstructionalOfferingDAO().
            getSession().
            createQuery("select co from InstructionalOffering as io , CourseOffering co "+
                    "where co.uniqueCourseNbr.subjectArea.uniqueId = :subjectAreaId "+
                    "and io.uniqueId = co.instructionalOffering.uniqueId "+
                    "and co.instructionalOffering.notOffered = false "+
                    "and io.notOffered = false order by co.courseNbr ").
            setFetchSize(200).
            setCacheable(true).
            setLong("subjectAreaId", Long.parseLong(subjectAreaId)).
            list();
    }
    
    public static Collection getFreeTimeTimes(String timePatternId) {
        if (timePatternId==null || timePatternId.length()==0) return new Vector();
        TimePattern tp = new TimePatternDAO().get(Long.valueOf(timePatternId));
        if (tp==null) return new Vector();
        TimePatternModel m = tp.getTimePatternModel();
        Vector ret = new Vector();
        for (int i=0;i<m.getNrTimes();i++)
            ret.add(new IdValue(new Long(i),m.getStartTime(i)+" - "+m.getEndTime(i)));
        return ret;
    }    
    public static Collection getFreeTimeDays(String timePatternId) {
        if (timePatternId==null || timePatternId.length()==0) return new Vector();
        TimePattern tp = new TimePatternDAO().get(Long.valueOf(timePatternId));
        if (tp==null) return new Vector();
        TimePatternModel m = tp.getTimePatternModel();
        Vector ret = new Vector();
        for (int i=0;i<m.getNrDays();i++)
            ret.add(new IdValue(new Long(i),m.getDayHeader(i)));
        return ret;
    }
    
    public int getNrRequests() { return iNrRequests; }
    public void setNrRequests(int nrRequests) { 
        iNrRequests = nrRequests;
        while (iRequests.size()>iNrRequests+iNrAltRequests)
            iRequests.removeElementAt(iNrRequests-1);
        while (iRequests.size()<iNrRequests+iNrAltRequests)
            iRequests.insertElementAt(new RequestBean(), iNrRequests-1);
    }
    public int getNrAltRequests() { return iNrAltRequests; }
    public void setNrAltRequests(int nrAltRequests) { 
        iNrAltRequests = nrAltRequests; 
        while (iRequests.size()>iNrRequests+iNrAltRequests)
            iRequests.removeElementAt(iRequests.size()-1);
        while (iRequests.size()<iNrRequests+iNrAltRequests)
            iRequests.addElement(new RequestBean());
    }
    public int getNrAllRequests() { return iNrRequests+iNrAltRequests; }
    public void removeRequest(int idx) {
        iRequests.removeElementAt(idx);
        if (idx<iNrRequests)
            iNrRequests--;
        else
            iNrAltRequests--;
    }
    public void moveRequest(int idx, int inc) {
        if (idx>=0 && idx<iRequests.size() && idx+inc>=0 && idx+inc<iRequests.size()) {
            Object a = iRequests.elementAt(idx+inc);
            Object b = iRequests.elementAt(idx);
            iRequests.setElementAt(b, idx+inc);
            iRequests.setElementAt(a, idx);
        }
    }
    
    public Collection getRequests() {
        return iRequests;
    }
    public RequestBean getRequest(int idx) {
        return (RequestBean)iRequests.elementAt(idx);
    }
    
    public Collection getMessages() {
        return iMessages;
    }
    public Collection getCourseAssignments() {
        return iCourseAssignments;
    }
    
    public class RequestBean {
        Long iType = sTypeNone;
        String iFreeTimePattern = null;
        String iFreeTimeDay = null;
        String iFreeTimeTime = null;
        String iSubjectArea = null;
        String iCourseNbr = null;
        String iAlt1SubjectArea = null;
        String iAlt1CourseNbr = null;
        String iAlt2SubjectArea = null;
        String iAlt2CourseNbr = null;
        Boolean iWait = null;
        
        public Long getType() { return iType; }
        public void setType(Long type) { iType = type; }
        public String getFreeTimePattern() { return iFreeTimePattern; }
        public void setFreeTimePattern(String freeTimePattern) { iFreeTimePattern=freeTimePattern; }
        public String getFreeTimeDay() { return iFreeTimeDay; }
        public void setFreeTimeDay(String freeTimeDay) { iFreeTimeDay=freeTimeDay; }
        public String getFreeTimeTime() { return iFreeTimeTime; }
        public void setFreeTimeTime(String freeTimeTime) { iFreeTimeTime=freeTimeTime; }
        public String getSubjectArea() { return iSubjectArea; }
        public void setSubjectArea(String subjectArea) { iSubjectArea=subjectArea; }
        public String getCourseNbr() { return iCourseNbr; }
        public void setCourseNbr(String courseNbr) { iCourseNbr=courseNbr; }
        public String getAlt1SubjectArea() { return iAlt1SubjectArea; }
        public void setAlt1SubjectArea(String alt1subjectArea) { iAlt1SubjectArea=alt1subjectArea; }
        public String getAlt1CourseNbr() { return iAlt1CourseNbr; }
        public void setAlt1CourseNbr(String alt1courseNbr) { iAlt1CourseNbr=alt1courseNbr; }
        public String getAlt2SubjectArea() { return iAlt2SubjectArea; }
        public void setAlt2SubjectArea(String alt2subjectArea) { iAlt2SubjectArea=alt2subjectArea; }
        public String getAlt2CourseNbr() { return iAlt2CourseNbr; }
        public void setAlt2CourseNbr(String alt2courseNbr) { iAlt2CourseNbr=alt2courseNbr; }
        public Boolean getWait() { return iWait; }
        public void setWait(Boolean wait) { iWait = wait; }
        
        public Collection getFreeTimeTimes() {
            return SectioningDemoForm.getFreeTimeTimes(iFreeTimePattern);
        }    
        public Collection getFreeTimeDays() {
            return SectioningDemoForm.getFreeTimeDays(iFreeTimePattern);
        }
        public Collection getCourseNumbers() {
            return SectioningDemoForm.getCourseNumbers(iSubjectArea);
        }
        public Collection getAlt1CourseNumbers() {
            return SectioningDemoForm.getCourseNumbers(iAlt1SubjectArea);
        }
        public Collection getAlt2CourseNumbers() {
            return SectioningDemoForm.getCourseNumbers(iAlt2SubjectArea);
        }
        public CourseOffering getCourseOffering() {
            if (getCourseNbr()==null || getCourseNbr().length()==0) return null;
            return new CourseOfferingDAO().get(Long.valueOf(getCourseNbr()));
        }
        public CourseOffering getAlt1CourseOffering() {
            if (getAlt1CourseNbr()==null || getAlt1CourseNbr().length()==0) return null;
            return new CourseOfferingDAO().get(Long.valueOf(getAlt1CourseNbr()));
        }
        public CourseOffering getAlt2CourseOffering() {
            if (getAlt2CourseNbr()==null || getAlt2CourseNbr().length()==0) return null;
            return new CourseOfferingDAO().get(Long.valueOf(getAlt2CourseNbr()));
        }
        public boolean isAlternative() {
            return iRequests.indexOf(this)>=iNrRequests;
        }
        public Collection getTypes() {
            Vector ret = new Vector();
            ret.add(new IdValue(sTypeNone,""));
            ret.add(new IdValue(sTypeCourse,"Course"));
            if (!isAlternative()) ret.add(new IdValue(sTypeFreeTime,"Free Time"));
            return ret;
        }        
        public TimeLocation getFreeTime(Session session) {
            if (iFreeTimePattern==null || iFreeTimePattern.length()==0) return null;
            if (iFreeTimeDay==null || iFreeTimeDay.length()==0) return null;
            if (iFreeTimeTime==null || iFreeTimeTime.length()==0) return null;
            TimePattern tp = new TimePatternDAO().get(Long.valueOf(iFreeTimePattern));
            if (tp==null) return null;
            TimePatternModel model = tp.getTimePatternModel();
            int day = Integer.parseInt(iFreeTimeDay);
            int time = Integer.parseInt(iFreeTimeTime);
            TimeLocation loc = new TimeLocation(
                    model.getDayCode(day),
                    model.getStartSlot(time),
                    model.getSlotsPerMtg(),
                    PreferenceLevel.prolog2int(model.getPreference(day, time)),
                    model.getNormalizedPreference(day,time,1.0),
                    session.getDefaultDatePattern().getUniqueId(),
                    session.getDefaultDatePattern().getName(),
                    session.getDefaultDatePattern().getPatternBitSet(),
                    model.getBreakTime());
            loc.setTimePatternId(model.getTimePattern().getUniqueId());
            return loc;
        }
    }
    
    public RequestBean getFreeTimeBean(Session session, String daysStr, String startTimeStr, String endTimeStr) {
        try {
            for (Iterator i=TimePattern.findAll(session, Boolean.TRUE).iterator();i.hasNext();) {
                TimePattern t = (TimePattern)i.next();
                TimePatternModel m = t.getTimePatternModel();
                int day = -1;
                for (int j=0;j<m.getNrDays();j++) {
                    int dayCode = m.getDayCode(j);
                    String days = "";
                    for (int k=0;k<Constants.NR_DAYS;k++)
                        if ((dayCode & Constants.DAY_CODES[k])!=0)
                            days += Constants.DAY_NAMES_SHORT[k];
                    if (days.equals(daysStr)) {
                        day = j; break;
                    }
                }
                if (day<0) continue;
                int time = -1;
                int sHour = Integer.parseInt(startTimeStr)/100;
                int sMin = Integer.parseInt(startTimeStr)%100;
                int eHour = Integer.parseInt(endTimeStr)/100;
                int eMin = Integer.parseInt(endTimeStr)%100;
                for (int j=0;j<m.getNrTimes();j++) {
                    int startHour = m.getHour(j);
                    int startMinute = m.getMinute(j);
                    if (startHour!=sHour || startMinute!=sMin) continue;
                    int endTime = ( 60 * startHour + startMinute) + m.getSlotsPerMtg() * Constants.SLOT_LENGTH_MIN - m.getBreakTime();
                    int endHour = endTime / 60;
                    int endMinute = endTime % 60;
                    if (endHour!=eHour || endMinute!=eMin) continue;
                    time = j; break;
                }
                if (time<0) continue;
                RequestBean request = new RequestBean();
                request.setType(sTypeFreeTime);
                request.setFreeTimePattern(t.getUniqueId().toString());
                request.setFreeTimeDay(String.valueOf(day));
                request.setFreeTimeTime(String.valueOf(time));
                return request;
            }
            return null;
        } catch (Exception e) {
            Debug.error(e); return null;
        }
    }
    
    public static class MessageBean {
        String iType;
        String iMessage;
        public MessageBean() {}
        public String getMessage() {
            return iMessage;
        }
        public void setMessage(String message) {
            iMessage = message;
        }
        public String getType() {
            return iType;
        }
        public void setType(String type) {
            iType = type;
        }
        public String getHtml() {
            if ("WARN".equals(getType()))
                return "<font color='orange'>"+getMessage()+"</font>";
            if ("ERROR".equals(getType()))
                return "<font color='red'>"+getMessage()+"</font>";
            return getMessage();
        }
    }
    
    public static class CourseAssignmentBean {
        Vector iClassAssignments = new Vector();
        String iSubjectArea = null;
        String iCourseNumber = null;
        public CourseAssignmentBean() {}
        public Vector getClassAssignments() {
            return iClassAssignments;
        }
        public String getSubjectArea() {
            return iSubjectArea;
        }
        public void setSubjectArea(String subjectArea) {
            iSubjectArea = subjectArea;
        }
        public String getCourseNumber() {
            return iCourseNumber;
        }
        public void setCourseNumber(String courseNumber) {
            iCourseNumber = courseNumber;
        }
    }
    
    public static class ClassAssignmentBean {
        String iId;
        String iParentId;
        String iAssignmentId;
        String iName;
        String iTime;
        String iDate;
        String iLocation;
        String iInstructor;
        Vector iChoices = new Vector();
        String iDays;
        String iStartTime;
        String iEndTime;
        String iLength;
        CourseAssignmentBean iCourse;
        public ClassAssignmentBean(CourseAssignmentBean course) {
            iCourse = course;
        }
        public CourseAssignmentBean getCourse() {
            return iCourse;
        }
        public String getId() {
            return iId;
        }
        public void setId(String id) {
            iId = id;
        }
        public String getParentId() {
            return iParentId;
        }
        public void setParentId(String parentId) {
            iParentId = parentId;
        }
        public String getAssignmentId() {
            return iAssignmentId;
        }
        public void setAssignmentId(String assignmentId) {
            iAssignmentId = assignmentId;
        }
        public String getName() {
            return iName;
        }
        public void setName(String name) {
            iName = name;
        }
        public String getTime() {
            return iTime;
        }
        public void setTime(String time) {
            iTime = time;
        }
        public String getDate() {
            return iDate;
        }
        public void setDate(String date) {
            iDate = date;
        }
        public String getLocation() {
            return iLocation;
        }
        public void setLocation(String location) {
            iLocation = location;
        }
        public String getInstructor() {
            return iInstructor;
        }
        public void setInstructor(String instructor) {
            iInstructor = instructor;
        }
        public String getDays() {
            return iDays;
        }
        public void setDays(String days) {
            iDays = days;
        }
        public String getStartTime() {
            return iStartTime;
        }
        public void setStartTime(String startTime) {
            iStartTime = startTime;
        }
        public String getEndTime() {
            return iEndTime;
        }
        public void setEndTime(String endTime) {
            iEndTime = endTime;
        }
        public String getLength() {
            return iLength;
        }
        public void setLength(String length) {
            iLength = length;
        }
        public Vector getChoices() {
            return iChoices;
        }
        public ClassAssignmentBean getParentBean() {
            if (getParentId()==null) return null;
            for (Enumeration e=getCourse().getClassAssignments().elements();e.hasMoreElements();) {
                ClassAssignmentBean clazz = (ClassAssignmentBean)e.nextElement();
                if (clazz.getId().equals(getParentId()))
                    return clazz;
            }
            return null;
        }
        public int getIndent() {
            int indent = 0;
            ClassAssignmentBean clazz = getParentBean();
            while (clazz!=null) {
                indent++; clazz = clazz.getParentBean();
            }
            return indent;
        }
    }
    
    public static class ChoiceBean {
        String iId;
        boolean iAvailable;
        String iTime;
        String iDate;
        String iInstructor;
        boolean iSelected = false;
        boolean iWaitlisted = false;
        ClassAssignmentBean iClazz;
        Vector iDepends = new Vector();
        public ChoiceBean(ClassAssignmentBean clazz) {
            iClazz = clazz;
        }
        public ClassAssignmentBean getClazz() {
            return iClazz;
        }
        public String getId() {
            return iId;
        }
        public void setId(String id) {
            iId = id;
        }
        public boolean isAvailable() {
            return iAvailable;
        }
        public void setAvailable(boolean available) {
            iAvailable = available;
        }
        public String getTime() {
            return iTime;
        }
        public void setTime(String time) {
            iTime = time;
        }
        public String getDate() {
            return iDate;
        }
        public void setDate(String date) {
            iDate = date;
        }
        public String getInstructor() {
            return iInstructor;
        }
        public void setInstructor(String instructor) {
            iInstructor = instructor;
        }
        public boolean isSelected() {
            return iSelected;
        }
        public void setSelected(boolean selected) {
            iSelected = selected;
        }
        public boolean isDefault() {
            return (getClazz().getTime()==null?"":getClazz().getTime()).equals(getTime()==null?"":getTime()) && 
            (getClazz().getInstructor()==null?"":getClazz().getInstructor()).equals(getInstructor()==null?"":getInstructor());
        }
        public boolean isWaitlisted() {
            return iWaitlisted;
        }
        public void setWaitlisted(boolean waitlisted) {
            iWaitlisted = waitlisted;
        }
        public Vector getDepends() {
            return iDepends;
        }
        public String getParent() {
            StringBuffer sb = new StringBuffer();
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean dep = (DependsBean)e.nextElement();
                ChoiceBean choice = dep.getDepChoice();
                if (sb.length()>0) {
                    if (!e.hasMoreElements())
                        sb.append(" or ");
                    else
                        sb.append(", ");
                }
                sb.append(choice.getTime()+(choice.getInstructor()==null?"":" "+choice.getInstructor()));
            }
            return sb.toString();
        }
        public boolean dependsOn(ChoiceBean ch) {
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean dep = (DependsBean)e.nextElement();
                if (dep.dependsOn(ch)) return true;
            }
            return false;
        }
        public String getSelectCondition() {
            String con = null;
            for (Enumeration g=getDepends().elements();g.hasMoreElements();) {
                DependsBean dep = (DependsBean)g.nextElement();
                if (con==null) 
                    con = "";
                else
                    con+= " || ";
                con += dep.getSelectCondition();
            }
            return con;
        }
        public String getWaitlistCondition() {
            String con = null;
            for (Enumeration g=getDepends().elements();g.hasMoreElements();) {
                DependsBean dep = (DependsBean)g.nextElement();
                if (con==null) 
                    con = "";
                else
                    con+= " || ";
                con += dep.getWaitlistCondition();
            }
            return con;
        }
        public void printOnChangeScript(JspWriter out, String indent) throws IOException {
            //this choice was changed
            for (Enumeration e=getClazz().getCourse().getClassAssignments().elements();e.hasMoreElements();) {
                ClassAssignmentBean clazz = (ClassAssignmentBean)e.nextElement();
                for (Enumeration f=clazz.getChoices().elements();f.hasMoreElements();) {
                    ChoiceBean ch = (ChoiceBean)f.nextElement();
                    if (ch.dependsOn(this)) {
                        out.println(indent+"// "+ch.getClazz().getName()+" "+ch.getTime()+" depends on this choice");
                        out.println(indent+"changed = false;");
                        out.println(indent+"select = getSelect('"+
                                ch.getClazz().getCourse().getSubjectArea()+"','"+
                                ch.getClazz().getCourse().getCourseNumber()+"','"+
                                ch.getClazz().getId()+"','"+ch.getId()+"');");
                        out.println(indent+"canSelect = "+ch.getSelectCondition()+";");
                        out.println(indent+"waitlist = getWaitlist('"+
                                ch.getClazz().getCourse().getSubjectArea()+"','"+
                                ch.getClazz().getCourse().getCourseNumber()+"','"+
                                ch.getClazz().getId()+"','"+ch.getId()+"');");
                        out.println(indent+"canWaitlist = (select==null || !canSelect) && ("+ch.getWaitlistCondition()+");");
                        out.println(indent+"if (select!=null && (select.checked!=canSelect || select.disabled==canSelect)) {");
                        out.println(indent+"\tselect.disabled=!canSelect; changed=true;");
                        out.println(indent+"\twaitlist.checked=false; waitlist.disabled=false;");
                        out.println(indent+"\tcurrentSelection = getSelectedChoice('"+
                                ch.getClazz().getCourse().getSubjectArea()+"','"+
                                ch.getClazz().getCourse().getCourseNumber()+"','"+
                                ch.getClazz().getId()+"');");
                        out.println(indent+"\tif (canSelect && currentSelection.value=='') select.checked=true;");
                        out.println(indent+"\tif (!canSelect) select.checked=false;");
                        out.println(indent+"}");
                        out.println(indent+"if (waitlist.checked!=canWaitlist || waitlist.disabled==canWaitlist) {");
                        out.println(indent+"\twaitlist.checked=canWaitlist; waitlist.disabled=!canWaitlist; changed=true;");
                        out.println(indent+"}");
                        out.println(indent+"if (changed) {");
                        out.println(indent+"\tgetChoiceTR('"+
                                ch.getClazz().getCourse().getSubjectArea()+"','"+
                                ch.getClazz().getCourse().getCourseNumber()+"','"+
                                ch.getId()+"').style.display = ((select==null?true:select.disabled) && waitlist.disabled ? 'none' : 'table-row');");
                        out.println(indent+"\tchoiceChanged('"+
                                ch.getClazz().getCourse().getSubjectArea()+"','"+
                                ch.getClazz().getCourse().getCourseNumber()+"','"+
                                ch.getClazz().getId()+"','"+ch.getId()+"', type);");
                        out.println(indent+"}");
                        out.println();
                    }
                }
                
            }
        }
        public boolean isSelectDisabled() {
            if (!isAvailable()) return true;
            if (getDepends().isEmpty()) return false;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean d = (DependsBean)e.nextElement();
                if (d.isSelected()) return false;
            }
            return true;
        }
        public boolean isWaitDisabled() {
            if (!isSelectDisabled()) return true;
            if (getDepends().isEmpty()) return false;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean d = (DependsBean)e.nextElement();
                if (d.canBeWaitlisted()) return false;
            }
            return true;
        }
        public String getDisplay() {
            if (isWaitDisabled() && isSelectDisabled()) 
                return "none";
            else
                return "table-row";
        }
    }
    
    public class DependsBean {
        String iClassId;
        String iChoiceId;
        ChoiceBean iChoice;
        Vector iDepends = new Vector();
        public DependsBean(ChoiceBean choice) {
            iChoice = choice;
        }
        public ChoiceBean getChoice() {
            return iChoice;
        }
        public String getChoiceId() {
            return iChoiceId;
        }
        public void setChoiceId(String choiceId) {
            iChoiceId=choiceId;
        }
        public String getClassId() {
            return iClassId;
        }
        public void setClassId(String classId) {
            iClassId = classId;
        }
        public Vector getDepends() {
            return iDepends;
        }
        public ChoiceBean getDepChoice() {
            for (Enumeration e=getChoice().getClazz().getCourse().getClassAssignments().elements();e.hasMoreElements();) {
                ClassAssignmentBean clazz = (ClassAssignmentBean)e.nextElement();
                if (!clazz.getId().equals(getClassId())) continue;
                for (Enumeration f=clazz.getChoices().elements();f.hasMoreElements();) {
                    ChoiceBean choice = (ChoiceBean)f.nextElement();
                    if (choice.getId().equals(getChoiceId())) return choice;
                }
            }
            return null;
        }
        public boolean dependsOn(ChoiceBean ch) {
            if (getChoiceId().equals(ch.getId())) return true;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean dep = (DependsBean)e.nextElement();
                if (dep.dependsOn(ch)) return true;
            }
            return false;
        }
        public String getSelectCondition() {
            String con = "(isSelected('"+
                getChoice().getClazz().getCourse().getSubjectArea()+"','"+
                getChoice().getClazz().getCourse().getCourseNumber()+"','"+
                getClassId()+"','"+getChoiceId()+"')";
            if (!getDepends().isEmpty()) {
                con +=" && (";
                for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                    DependsBean dep = (DependsBean)e.nextElement();
                    con += dep.getSelectCondition();
                }
                con += " )";
            }
            return con + ")";
        }
        public String getWaitlistCondition() {
            String con = "((isWaitlisted('"+
                getChoice().getClazz().getCourse().getSubjectArea()+"','"+
                getChoice().getClazz().getCourse().getCourseNumber()+"','"+
                getClassId()+"','"+getChoiceId()+"') || isSelected('"+
                getChoice().getClazz().getCourse().getSubjectArea()+"','"+
                getChoice().getClazz().getCourse().getCourseNumber()+"','"+
                getClassId()+"','"+getChoiceId()+"'))";
            if (!getDepends().isEmpty()) {
                con += " && (";
                for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                    DependsBean dep = (DependsBean)e.nextElement();
                    con += dep.getWaitlistCondition();
                }
                con += ")";
            }
            return con + ")";
        }
        public boolean isSelected() {
            if (!getDepChoice().isSelected()) return false;
            if (getDepends().isEmpty()) return true;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean d = (DependsBean)e.nextElement();
                if (d.isSelected()) return true;
            }
            return false;
        }
        public boolean isWaitlisted() {
            if (!getDepChoice().isWaitlisted()) return false;
            if (getDepends().isEmpty()) return true;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean d = (DependsBean)e.nextElement();
                if (d.isWaitlisted()) return true;
            }
            return false;
        }
        public boolean canBeWaitlisted() {
            if (!getDepChoice().isWaitlisted() && !getDepChoice().isSelected()) return false;
            if (getDepends().isEmpty()) return true;
            for (Enumeration e=getDepends().elements();e.hasMoreElements();) {
                DependsBean d = (DependsBean)e.nextElement();
                if (d.canBeWaitlisted()) return true;
            }
            return false;
        }
    }
    
    public void importDepends(DependsBean dep, Element depEl) {
        for (Iterator i=depEl.elementIterator("depends");i.hasNext();) {
            Element innerDepEl = (Element)i.next();
            DependsBean innerDep = new DependsBean(dep.getChoice());
            innerDep.setChoiceId(innerDepEl.attributeValue("choice"));
            innerDep.setClassId(innerDepEl.attributeValue("class"));
            dep.getDepends().add(innerDep);
            importDepends(innerDep, innerDepEl);
        }
    }
    
    public void load(Session session, Element studentElement, boolean includeCourseRequests, HttpServletRequest httpRequest) {
        if (includeCourseRequests) {
            iRequests.clear();
            iNrRequests = 0; 
            iNrAltRequests = 0;

            Element courseRequestsElement = studentElement.element("courseRequests");
            if (courseRequestsElement==null) return;

            for (Iterator i=courseRequestsElement.elementIterator();i.hasNext();) {
                Element requestElement = (Element)i.next();
                boolean alternative = "true".equals(requestElement.attributeValue("alternative"));
                if ("freeTime".equals(requestElement.getName())) {
                    RequestBean request = getFreeTimeBean(session, 
                            requestElement.attributeValue("days"),
                            requestElement.attributeValue("startTime"), 
                            requestElement.attributeValue("endTime"));
                    iRequests.add(request);
                } else if ("courseOffering".equals(requestElement.getName())) {
                    String subjectArea = requestElement.attributeValue("subjectArea");
                    String courseNumber = requestElement.attributeValue("courseNumber");
                    boolean waitlist = "true".equals(requestElement.attributeValue("waitlist"));
                    CourseOffering co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), subjectArea, courseNumber);
                    if (co==null) continue;
                    Vector courses = new Vector();
                    RequestBean request = new RequestBean();
                    request.setType(sTypeCourse);
                    request.setSubjectArea(co.getSubjectArea().getUniqueId().toString());
                    request.setCourseNbr(co.getUniqueId().toString());
                    request.setWait(new Boolean(waitlist));
                    int idx = 0;
                    for (Iterator j=requestElement.elementIterator("alternative");j.hasNext();idx++) {
                        Element altElement = (Element)j.next();
                        String altSubjectArea = altElement.attributeValue("subjectArea");
                        String altCourseNumber = altElement.attributeValue("courseNumber");
                        CourseOffering aco = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(session.getUniqueId(), altSubjectArea, altCourseNumber);
                        if (aco==null) continue;
                        if (idx==0) {
                            request.setAlt1SubjectArea(aco.getSubjectArea().getUniqueId().toString());
                            request.setAlt1CourseNbr(aco.getUniqueId().toString());
                        } else if (idx==1) {
                            request.setAlt2SubjectArea(aco.getSubjectArea().getUniqueId().toString());
                            request.setAlt2CourseNbr(aco.getUniqueId().toString());
                        }
                    }
                    iRequests.add(request);
                }
                if (alternative)
                    iNrAltRequests++;
                else
                    iNrRequests++;
            }
        }
        
        iCourseAssignments.clear();
        Element scheduleElement = studentElement.element("schedule");
        if (scheduleElement!=null)
            for (Iterator i=scheduleElement.elementIterator();i.hasNext();) {
                Element element = (Element)i.next();
                if ("courseOffering".equals(element.getName())) {
                    CourseAssignmentBean course = new CourseAssignmentBean();
                    course.setSubjectArea(element.attributeValue("subjectArea"));
                    course.setCourseNumber(element.attributeValue("courseNumber"));
                    for (Iterator j=element.elementIterator("class");j.hasNext();) {
                        Element classElement = (Element)j.next();
                        ClassAssignmentBean clazz = new ClassAssignmentBean(course);
                        clazz.setId(classElement.attributeValue("id"));
                        clazz.setParentId(classElement.attributeValue("parent"));
                        clazz.setAssignmentId(classElement.attributeValue("assignmentId"));
                        clazz.setName(classElement.attributeValue("name"));
                        clazz.setTime(classElement.attributeValue("time"));
                        clazz.setStartTime(classElement.attributeValue("startTime"));
                        clazz.setEndTime(classElement.attributeValue("endTime"));
                        clazz.setDays(classElement.attributeValue("days"));
                        clazz.setDate(classElement.attributeValue("date"));
                        clazz.setLocation(classElement.attributeValue("location"));
                        clazz.setInstructor(classElement.attributeValue("instructor"));
                        for (Iterator k=classElement.elementIterator("choice");k.hasNext();) {
                            Element choiceElement = (Element)k.next();
                            ChoiceBean choice = new ChoiceBean(clazz);
                            choice.setId(choiceElement.attributeValue("id"));
                            choice.setAvailable(choiceElement.attributeValue("available")==null || "true".equals(choiceElement.attributeValue("available")));
                            choice.setTime(choiceElement.attributeValue("time"));
                            choice.setDate(choiceElement.attributeValue("date"));
                            choice.setInstructor(choiceElement.attributeValue("instructor"));
                            if ("select".equals(choiceElement.attributeValue("selection"))) {
                                choice.setSelected(true);
                            } else if ("wait".equals(choiceElement.attributeValue("selection"))) {
                                choice.setWaitlisted(true);
                            }
                            clazz.getChoices().add(choice);
                            for (Iterator l=choiceElement.elementIterator("depends");l.hasNext();) {
                                Element depEl = (Element)l.next();
                                DependsBean dep = new DependsBean(choice);
                                dep.setClassId(depEl.attributeValue("class"));
                                dep.setChoiceId(depEl.attributeValue("choice"));
                                importDepends(dep, depEl);
                                choice.getDepends().add(dep);
                            }
                        }
                        course.getClassAssignments().add(clazz);
                    }
                    iCourseAssignments.add(course);
                } else if ("freeTime".equals(element.getName())) {
                    CourseAssignmentBean course = new CourseAssignmentBean();
                    course.setSubjectArea("Free");
                    course.setCourseNumber("");
                    ClassAssignmentBean clazz = new ClassAssignmentBean(course);
                    clazz.setName("");
                    clazz.setTime(element.attributeValue("time"));
                    clazz.setStartTime(element.attributeValue("startTime"));
                    clazz.setEndTime(element.attributeValue("endTime"));
                    clazz.setDays(element.attributeValue("days"));
                    clazz.setStartTime(element.attributeValue("length"));
                    course.getClassAssignments().add(clazz);
                    iCourseAssignments.add(course);
                }
            }
        
        iMessages.clear();
        Element ackElement = studentElement.element("acknowledgement");
        if (ackElement!=null)
            for (Iterator i=ackElement.elementIterator("message");i.hasNext();) {
                Element msgElement = (Element)i.next();
                MessageBean message = new MessageBean();
                message.setType(msgElement.attributeValue("type"));
                message.setMessage(msgElement.getText());
                iMessages.add(message);
            }
        
        if (httpRequest!=null) {
            for (Enumeration e=iCourseAssignments.elements();e.hasMoreElements();) {
                CourseAssignmentBean course = (CourseAssignmentBean)e.nextElement();
                for (Enumeration f=course.getClassAssignments().elements();f.hasMoreElements();) {
                    ClassAssignmentBean clazz = (ClassAssignmentBean)f.nextElement();
                    String[] selectedChoices = httpRequest.getParameterValues("chs_"+course.getSubjectArea()+":"+course.getCourseNumber()+":"+clazz.getId());
                    String[] waitlistedChoices = httpRequest.getParameterValues("chwl_"+course.getSubjectArea()+":"+course.getCourseNumber()+":"+clazz.getId());
                    for (Enumeration g=clazz.getChoices().elements();g.hasMoreElements();) {
                        ChoiceBean choice = (ChoiceBean)g.nextElement();
                        boolean selected = false, waitlisted = false;
                        if (selectedChoices!=null)
                            for (int i=0;i<selectedChoices.length;i++)
                                if (choice.getId().equals(selectedChoices[i])) {
                                    selected = true; break;
                                }
                        if (waitlistedChoices!=null)
                            for (int i=0;i<waitlistedChoices.length;i++)
                                if (choice.getId().equals(waitlistedChoices[i])) {
                                    waitlisted = true; break;
                                }
                        choice.setSelected(selected); choice.setWaitlisted(waitlisted);
                    }
                }
            }
        }
    }
    
    public static String startSlot2startTime(int startSlot) {
        int minHrs = startSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }
    
    public static String timeLocation2endTime(TimeLocation time) {
        int minHrs = (time.getStartSlot()+time.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - time.getBreakTime();
        return sTwoNumbersDF.format(minHrs/60)+sTwoNumbersDF.format(minHrs%60);
    }

    public void save(Session session, Element studentElement, boolean commit) {
        Element courseRequestsElement = studentElement.addElement("updateCourseRequests");
        courseRequestsElement.addAttribute("commit", (commit?"true":"false"));
        courseRequestsElement.addAttribute("distribution", "spread");
        for (Enumeration e=iRequests.elements();e.hasMoreElements();) {
            RequestBean request = (RequestBean)e.nextElement();
            if (new Long(sTypeFreeTime).equals(request.getType())) {
                Element reqElement = courseRequestsElement.addElement("freeTime");
                TimeLocation time = request.getFreeTime(session);
                reqElement.addAttribute("days", time.getDayHeader());
                reqElement.addAttribute("startTime", startSlot2startTime(time.getStartSlot()));
                reqElement.addAttribute("endTime", timeLocation2endTime(time));
                reqElement.addAttribute("length", String.valueOf(time.getNrSlotsPerMeeting()*Constants.SLOT_LENGTH_MIN - time.getBreakTime()));
            } else {
                CourseOffering course = request.getCourseOffering();
                if (course==null) continue;
                Element reqElement = courseRequestsElement.addElement("courseOffering");
                reqElement.addAttribute("subjectArea", course.getSubjectAreaAbbv());
                reqElement.addAttribute("courseNumber", course.getCourseNbr());
                reqElement.addAttribute("waitlist", (request.getWait()==null?"false":request.getWait().booleanValue()?"true":"false"));
                CourseOffering alt1Course = request.getAlt1CourseOffering();
                if (alt1Course!=null) {
                    Element element = reqElement.addElement("alternative");
                    element.addAttribute("subjectArea", alt1Course.getSubjectAreaAbbv());
                    element.addAttribute("courseNumber", alt1Course.getCourseNbr());
                }
                CourseOffering alt2Course = request.getAlt2CourseOffering();
                if (alt2Course!=null) {
                    Element element = reqElement.addElement("alternative");
                    element.addAttribute("subjectArea", alt2Course.getSubjectAreaAbbv());
                    element.addAttribute("courseNumber", alt2Course.getCourseNbr());
                }
                if (request.isAlternative()) reqElement.addAttribute("alternative", "true");
            }
        }
        Element scheduleElement = studentElement.addElement("requestSchedule");
        scheduleElement.addAttribute("type", "request");
        for (Enumeration e=iCourseAssignments.elements();e.hasMoreElements();) {
            CourseAssignmentBean course = (CourseAssignmentBean)e.nextElement();
            if ("Free".equals(course.getSubjectArea()) && "".equals(course.getCourseNumber())) {
                Element freeTimeElement = scheduleElement.addElement("freeTime");
                ClassAssignmentBean clazz = (ClassAssignmentBean)course.getClassAssignments().firstElement();
                freeTimeElement.addAttribute("days", clazz.getDays());
                freeTimeElement.addAttribute("startTime", clazz.getStartTime());
                freeTimeElement.addAttribute("endTime", clazz.getEndTime());
                freeTimeElement.addAttribute("length", clazz.getLength());
            } else {
                Element courseElement = scheduleElement.addElement("courseOffering");
                courseElement.addAttribute("subjectArea", course.getSubjectArea());
                courseElement.addAttribute("courseNumber", course.getCourseNumber());
                if (course.getClassAssignments().isEmpty())
                    courseElement.addAttribute("waitlist","true");
                for (Enumeration f=course.getClassAssignments().elements();f.hasMoreElements();) {
                    ClassAssignmentBean clazz = (ClassAssignmentBean)f.nextElement();
                    Element classElement = courseElement.addElement("class");
                    classElement.addAttribute("id", clazz.getId());
                    classElement.addAttribute("assignmentId", clazz.getAssignmentId());
                    for (Enumeration g=clazz.getChoices().elements();g.hasMoreElements();) {
                        ChoiceBean choice = (ChoiceBean)g.nextElement();
                        if (choice.isSelected() || choice.isWaitlisted()) {
                            Element choiceElement = classElement.addElement("choice");
                            choiceElement.addAttribute("id", choice.getId());
                            if (choice.isSelected())
                                choiceElement.addAttribute("selection", "select");
                            else
                                choiceElement.addAttribute("selection", "wait");
                        }
                    }
                }
            }
            
        }
    }
    
    public String getRequestFile() {
        return iRequestFile;
    }
    public void setRequestFile(String requestFile) {
        iRequestFile = requestFile;
    }

    public String getResponseFile() {
        return iResponseFile;
    }
    public void setResponseFile(String responseFile) {
        iResponseFile = responseFile;
    }
    
    public void printOnChangeScript(JspWriter out) throws IOException {
        for (Enumeration e=iCourseAssignments.elements();e.hasMoreElements();) {
            CourseAssignmentBean course = (CourseAssignmentBean)e.nextElement();
            out.println("\tvar canSelect=false;");
            out.println("\tvar canWaitlist=false;");
            out.println("\tvar changed=false;");
            out.println("\tvar select=null;");
            out.println("\tvar waitlist=null;");
            out.println("\tvar currentSelection=null;");
            out.println("\tif (subjectArea=='"+course.getSubjectArea()+"' && courseNumber=='"+course.getCourseNumber()+"') { // "+course.getSubjectArea()+" "+course.getCourseNumber());
            for (Enumeration f=course.getClassAssignments().elements();f.hasMoreElements();) {
                ClassAssignmentBean clazz =(ClassAssignmentBean)f.nextElement();
                out.println("\t\tif (classId=='"+clazz.getId()+"') { // "+course.getSubjectArea()+" "+course.getCourseNumber()+" "+clazz.getName());
                for (Enumeration g=clazz.getChoices().elements();g.hasMoreElements();) {
                    ChoiceBean choice = (ChoiceBean)g.nextElement();
                    out.println("\t\t\tif (chId=='"+choice.getId()+"') { // "+choice.getTime()+" "+(choice.getInstructor()==null?"":choice.getInstructor()));
                    choice.printOnChangeScript(out,"\t\t\t\t");
                    out.println("\t\t\t}\n");
                }
                out.println("\t\t}\n");
            }
            out.println("\t}\n");
        }
    }
    
    public String getStudentId() {
        return iStudentId;
    }
    public void setStudentId(String studentId) {
        iStudentId = studentId;
    }
    public boolean isStudentLoaded() {
        return iStudentLoaded;
    }
    public void setStudentLoaded(boolean studentLoaded) {
        iStudentLoaded = studentLoaded;
    }
}
