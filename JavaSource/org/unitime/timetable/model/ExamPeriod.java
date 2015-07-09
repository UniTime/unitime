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
package org.unitime.timetable.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.base.BaseExamPeriod;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ExamPeriod extends BaseExamPeriod implements Comparable<ExamPeriod> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamPeriod () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamPeriod (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static String PERIOD_ATTR_NAME = "periodList";

	public Date getStartDate() {
	    Calendar c = Calendar.getInstance(Locale.US);
	    c.setTime(getSession().getExamBeginDate());
	    c.add(Calendar.DAY_OF_YEAR, getDateOffset());
	    return c.getTime();
	}
	
   public void setStartDate(Date startDate) {
       long diff = startDate.getTime()-getSession().getExamBeginDate().getTime();
       setDateOffset((int)Math.round(diff/(1000.0 * 60 * 60 * 24)));
    }
	
	public int getStartHour() {
	    return (Constants.SLOT_LENGTH_MIN*getStartSlot()+Constants.FIRST_SLOT_TIME_MIN) / 60;
	}
	
    public int getStartMinute() {
        return (Constants.SLOT_LENGTH_MIN*getStartSlot()+Constants.FIRST_SLOT_TIME_MIN) % 60;
    }
    
    public Date getStartTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        c.set(Calendar.HOUR, getStartHour());
        c.set(Calendar.MINUTE, getStartMinute());
        return c.getTime();
    }
    
    public int getEndSlot() {
        return getStartSlot() + getLength();
    }
    
    public int getEndHour() {
        return (Constants.SLOT_LENGTH_MIN*getEndSlot()+Constants.FIRST_SLOT_TIME_MIN) / 60;
    }
    
    public int getEndMinute() {
        return (Constants.SLOT_LENGTH_MIN*getEndSlot()+Constants.FIRST_SLOT_TIME_MIN) % 60;
    }
    
    public Date getEndTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        c.set(Calendar.HOUR, getEndHour());
        c.set(Calendar.MINUTE, getEndMinute());
        return c.getTime();
    }
    
    public String getStartDateLabel() {
        return Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD).format(getStartDate());
    }
    
    public String getStartTimeLabel() {
        int min = getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return Constants.toTime(min);
    }

    public String getStartTimeLabel(int printOffset) {
        int min = getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + printOffset;
        return Constants.toTime(min);
    }

    public String getEndTimeLabel() {
        int min = (getStartSlot()+getLength())*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        return Constants.toTime(min);
    }

    public String getEndTimeLabel(int length, int printOffset) {
        int min = getStartSlot()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + length + printOffset;
        return Constants.toTime(min);
    }

    public String getName() {
        return getStartDateLabel()+" "+getStartTimeLabel()+" - "+getEndTimeLabel();
    }

    public String getAbbreviation() {
        return getStartDateLabel()+" "+getStartTimeLabel();
    }

    public int compareTo(ExamPeriod period) {
    	int cmp = getExamType().compareTo(period.getExamType());
    	if (cmp!=0) return cmp;
	    cmp = getDateOffset().compareTo(period.getDateOffset());
	    if (cmp!=0) return cmp;
	    return getStartSlot().compareTo(period.getStartSlot());
	}
    
    public static TreeSet<ExamPeriod> findAll(Long sessionId, ExamType type) {
    	return findAll(sessionId, type == null ? null : type.getUniqueId());
    }
    
    public static TreeSet<ExamPeriod> findAll(Long sessionId, Long examTypeId) {
    	TreeSet<ExamPeriod> ret = new TreeSet<ExamPeriod>();
    	if (examTypeId==null)
    		ret.addAll(new ExamPeriodDAO().getSession().
                createQuery("select ep from ExamPeriod ep where ep.session.uniqueId=:sessionId").
                setLong("sessionId", sessionId).
                setCacheable(true).
                list());
    	else
    		ret.addAll(new ExamPeriodDAO().getSession().
                    createQuery("select ep from ExamPeriod ep where ep.session.uniqueId=:sessionId and ep.examType.uniqueId=:typeId").
                    setLong("sessionId", sessionId).
                    setLong("typeId", examTypeId).
                    setCacheable(true).
                    list());
        return ret;
    }
    
    public static ExamPeriod findByDateStart(Long sessionId, int dateOffset, int startSlot) {
        return (ExamPeriod)new ExamPeriodDAO().getSession().createQuery(
                "select ep from ExamPeriod ep where " +
                "ep.session.uniqueId=:sessionId and ep.dateOffset=:dateOffset and ep.startSlot=:startSlot").
                setLong("sessionId", sessionId).
                setInteger("dateOffset", dateOffset).
                setInteger("startSlot", startSlot).setCacheable(true).uniqueResult();
    }
    
    public static ExamPeriod findByIndex(Long sessionId, ExamType type, Integer idx) {
        if (idx==null || idx<0) return null;
        int x = 0;
        TreeSet periods = findAll(sessionId, type);
        for (Iterator i=periods.iterator();i.hasNext();x++) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (x==idx) return period;
        }
        return (periods.isEmpty()?null:(ExamPeriod)periods.last());
    }
    
    public String toString() {
        return getAbbreviation();
    }
    
    public boolean isBackToBack(ExamPeriod period, boolean isDayBreakBackToBack) {
        if (!isDayBreakBackToBack && !period.getDateOffset().equals(getDateOffset())) return false;
        for (Iterator i=findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            ExamPeriod p = (ExamPeriod)i.next();
            if (compareTo(p)<0 && p.compareTo(period)<0) return false;
            if (compareTo(p)>0 && p.compareTo(period)>0) return false;
        }
        return true;
    }
    
    public boolean overlap(Assignment assignment) {
        return overlap(assignment, ApplicationProperty.ExaminationTravelTimeClass.intValue());
    }
    
    public boolean overlap(Assignment assignment, int nrTravelSlots) {
        //check date pattern
        DatePattern dp = assignment.getDatePattern();
        int dpIndex = getDateOffset()-getSession().getExamBeginOffset()-(dp.getOffset()==null?0:dp.getOffset());
        if (dp.getPattern()==null || dpIndex<0 || dpIndex>=dp.getPattern().length() || dp.getPattern().charAt(dpIndex)!='1') return false;
        
        //check day of week
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(getSession().getExamBeginDate());
        cal.add(Calendar.DAY_OF_YEAR, getDateOffset());
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_MON])==0) return false; break;
            case Calendar.TUESDAY   : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_TUE])==0) return false; break;
            case Calendar.WEDNESDAY : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_WED])==0) return false; break;
            case Calendar.THURSDAY  : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_THU])==0) return false; break;
            case Calendar.FRIDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_FRI])==0) return false; break;
            case Calendar.SATURDAY  : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_SAT])==0) return false; break;
            case Calendar.SUNDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_SUN])==0) return false; break;
        }
        
        //check time
        return getStartSlot() - nrTravelSlots < assignment.getStartSlot() + assignment.getSlotPerMtg() && assignment.getStartSlot() < getStartSlot() + getLength() + nrTravelSlots;
    }
    
    public boolean overlap(Meeting meeting) {
        return overlap(meeting, ApplicationProperty.ExaminationTravelTimeClass.intValue());
    }
    
    public boolean overlap(Meeting meeting, int nrTravelSlots) {
        if (!meeting.getMeetingDate().equals(getStartDate())) return false;
        return getStartSlot() - nrTravelSlots < meeting.getStopPeriod() && meeting.getStartPeriod() < getStartSlot() + getLength() + nrTravelSlots;
    }
    
    public List<Meeting> findOverlappingClassMeetings() {
        return findOverlappingClassMeetings(ApplicationProperty.ExaminationTravelTimeClass.intValue());
    }

    public List<Meeting> findOverlappingClassMeetings(int nrTravelSlots) {
        return new ExamPeriodDAO().getSession().createQuery(
                "select m from ClassEvent e inner join e.meetings m where " +
                "m.meetingDate=:startDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot")
                .setDate("startDate", getStartDate())
                .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                .setCacheable(true)
                .list();
    } 
    
    public List<Meeting> findOverlappingClassMeetings(Long classId) {
        return findOverlappingClassMeetings(classId, ApplicationProperty.ExaminationTravelTimeClass.intValue());
    }
    
    public List<Meeting> findOverlappingClassMeetings(Long classId, int nrTravelSlots) {
        return new ExamPeriodDAO().getSession().createQuery(
                "select m from ClassEvent e inner join e.meetings m where " +
                "m.meetingDate=:startDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and " +
                "e.clazz.uniqueId=:classId")
                .setDate("startDate", getStartDate())
                .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                .setLong("classId", classId)
                .setCacheable(true)
                .list();
    }
    
    public Hashtable<Meeting,Set<Long>> findOverlappingCourseMeetingsWithReqAttendence(Set<Long> studentIds) {
        return findOverlappingCourseMeetingsWithReqAttendence(studentIds, ApplicationProperty.ExaminationTravelTimeCourse.intValue());
    }

    public Hashtable<Meeting,Set<Long>> findOverlappingCourseMeetingsWithReqAttendence(Set<Long> studentIds, int nrTravelSlots) {
        Hashtable<Meeting,Set<Long>> ret = new Hashtable();
        if (studentIds==null || studentIds.isEmpty()) return ret;

        String students = "";
        int nrStudents = 0;
        
        for (Long studentId: studentIds) {
            students += (students.length()==0?"":",")+studentId;
            nrStudents++;
            if (nrStudents==1000) {
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select m, s.student.uniqueId from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                        "o.ownerType=:classType and s.clazz.uniqueId=o.ownerId")
                        .setDate("meetingDate", getStartDate())
                        .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                        .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                        .setInteger("classType", ExamOwner.sOwnerTypeClass)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Meeting meeting = (Meeting)o[0];
                    long xstudentId = (Long)o[1];
                    Set<Long> conf = ret.get(meeting);
                    if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                    conf.add(xstudentId);
                }
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select m, s.student.uniqueId from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                        "o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId")
                        .setDate("meetingDate", getStartDate())
                        .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                        .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                        .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Meeting meeting = (Meeting)o[0];
                    long xstudentId = (Long)o[1];
                    Set<Long> conf = ret.get(meeting);
                    if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                    conf.add(xstudentId);
                }
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select m, s.student.uniqueId from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                        "o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId")
                        .setDate("meetingDate", getStartDate())
                        .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                        .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                        .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Meeting meeting = (Meeting)o[0];
                    long xstudentId = (Long)o[1];
                    Set<Long> conf = ret.get(meeting);
                    if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                    conf.add(xstudentId);
                }
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select m, s.student.uniqueId from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                        "o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId")
                        .setDate("meetingDate", getStartDate())
                        .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                        .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                        .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Meeting meeting = (Meeting)o[0];
                    long xstudentId = (Long)o[1];
                    Set<Long> conf = ret.get(meeting);
                    if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                    conf.add(xstudentId);
                }
                students = ""; nrStudents = 0;
            }
        }

        if (nrStudents > 0 && students.trim().length() > 0) {
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select m, s.student.uniqueId from "+
                    "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                    "o.ownerType=:classType and s.clazz.uniqueId=o.ownerId")
                    .setDate("meetingDate", getStartDate())
                    .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                    .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Meeting meeting = (Meeting)o[0];
                long xstudentId = (Long)o[1];
                Set<Long> conf = ret.get(meeting);
                if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                conf.add(xstudentId);
            }
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select m, s.student.uniqueId from "+
                    "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                    "o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId")
                    .setDate("meetingDate", getStartDate())
                    .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                    .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                    .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Meeting meeting = (Meeting)o[0];
                long xstudentId = (Long)o[1];
                Set<Long> conf = ret.get(meeting);
                if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                conf.add(xstudentId);
            }
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select m, s.student.uniqueId from "+
                    "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                    "o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId")
                    .setDate("meetingDate", getStartDate())
                    .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                    .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                    .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Meeting meeting = (Meeting)o[0];
                long xstudentId = (Long)o[1];
                Set<Long> conf = ret.get(meeting);
                if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                conf.add(xstudentId);
            }
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select m, s.student.uniqueId from "+
                    "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and "+
                    "o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId")
                    .setDate("meetingDate", getStartDate())
                    .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                    .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                    .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Meeting meeting = (Meeting)o[0];
                long xstudentId = (Long)o[1];
                Set<Long> conf = ret.get(meeting);
                if (conf==null) { conf = new HashSet(); ret.put(meeting, conf); }
                conf.add(xstudentId);
            }
        }
        return ret;
    } 
    
    public int getIndex() {
        int index = 0;
        for (Iterator i=findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            if (compareTo((ExamPeriod)i.next())>0) index++;
        }
        return index;
    }
    
    public Object clone(){
    	ExamPeriod newExamPeriod = new ExamPeriod();
    	newExamPeriod.setExamType(getExamType());
    	newExamPeriod.setDateOffset(getDateOffset());
    	newExamPeriod.setLength(getLength());
    	newExamPeriod.setPrefLevel(getPrefLevel());
    	newExamPeriod.setStartSlot(getStartSlot());
    	newExamPeriod.setSession(getSession());
    	newExamPeriod.setEventStartOffset(getEventStartOffset());
    	newExamPeriod.setEventStopOffset(getEventStopOffset());
    	return(newExamPeriod);
    }
    
    public ExamPeriod findSameExamPeriodInSession(Session session){
		if (session == null) {
			return(null);
		}
    	return((ExamPeriod)(new ExamPeriodDAO()).getQuery("select distinct ep from ExamPeriod ep where ep.session.uniqueId = :sessionId" +
    			" and ep.examType.uniqueId = :examTypeId" +
    			" and ep.dateOffset = :dateOffset" +
    			" and ep.length = :length" +
    			" and ep.prefLevel.uniqueId = :prefLevelId" +
    			" and ep.startSlot = :startSlot")
    			.setLong("sessionId", session.getUniqueId().longValue())
    			.setLong("examTypeId", getExamType().getUniqueId())
    			.setInteger("dateOffset", getDateOffset().intValue())
    			.setInteger("length", getLength().intValue())
    			.setLong("prefLevelId", getPrefLevel().getUniqueId().longValue())
    			.setInteger("startSlot", getStartSlot().intValue())
    			.setCacheable(true)
    			.uniqueResult());
    			
    }
    
    public int getDayOfWeek() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        return c.get(Calendar.DAY_OF_WEEK);
    }

    
    public boolean weakOverlap(Meeting meeting) {
        return getDayOfWeek()==meeting.getDayOfWeek() && getStartSlot() < meeting.getStopPeriod() && meeting.getStartPeriod() < getStartSlot() + getLength();
    }
    
    public boolean overlap(TimeBlock time) {
//        int breakTimeStart = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability."+Exam.sExamTypes[getExamType()].toLowerCase()+".breakTime.start", "0"));
//        int breakTimeStop = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.room.availability."+Exam.sExamTypes[getExamType()].toLowerCase()+".breakTime.stop", "0"));
        int breakTimeStart = getEventStartOffset().intValue() * Constants.SLOT_LENGTH_MIN;
        int breakTimeStop = getEventStopOffset().intValue() * Constants.SLOT_LENGTH_MIN;
        Date start = time.getStartTime();
        if (breakTimeStart!=0) {
            Calendar c = Calendar.getInstance(Locale.US); 
            c.setTime(start);
            c.add(Calendar.MINUTE, -breakTimeStart);
            start = c.getTime();
        }
        Date stop = time.getEndTime();
        if (breakTimeStop!=0) {
            Calendar c = Calendar.getInstance(Locale.US); 
            c.setTime(stop);
            c.add(Calendar.MINUTE, breakTimeStop);
            stop = c.getTime();
        }
        return getStartTime().compareTo(stop)<0 && start.compareTo(getEndTime()) < 0;
    }
    
    public static Date[] getBounds(Session session, Long examTypeId) {
        return getBounds(session.getUniqueId(), session.getExamBeginDate(), examTypeId);
    }
    
    public static Date[] getBounds(Long sessionId, Date examBeginDate, Long examTypeId) {
        Object[] bounds = (Object[])new ExamPeriodDAO().getQuery("select min(ep.dateOffset), min(ep.startSlot - ep.eventStartOffset), max(ep.dateOffset), max(ep.startSlot+ep.length+ep.eventStopOffset) " +
        		"from ExamPeriod ep where ep.session.uniqueId = :sessionId and ep.examType.uniqueId = :examTypeId")
        		.setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setCacheable(true).uniqueResult();
        if (bounds == null || bounds[0] == null) return null;
        int minDateOffset = ((Number)bounds[0]).intValue();
        int minSlot = ((Number)bounds[1]).intValue();
        int minHour = (Constants.SLOT_LENGTH_MIN*minSlot+Constants.FIRST_SLOT_TIME_MIN) / 60;
        int minMin = (Constants.SLOT_LENGTH_MIN*minSlot+Constants.FIRST_SLOT_TIME_MIN) % 60;
        int maxDateOffset = ((Number)bounds[2]).intValue();
        int maxSlot = ((Number)bounds[3]).intValue();
        int maxHour = (Constants.SLOT_LENGTH_MIN*maxSlot+Constants.FIRST_SLOT_TIME_MIN) / 60;
        int maxMin = (Constants.SLOT_LENGTH_MIN*maxSlot+Constants.FIRST_SLOT_TIME_MIN) % 60;
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(examBeginDate);
        c.add(Calendar.DAY_OF_YEAR, minDateOffset);
        c.set(Calendar.HOUR, minHour);
        c.set(Calendar.MINUTE, minMin);
        Date min = c.getTime();
        c.setTime(examBeginDate);
        c.add(Calendar.DAY_OF_YEAR, maxDateOffset);
        c.set(Calendar.HOUR, maxHour);
        c.set(Calendar.MINUTE, maxMin);
        Date max = c.getTime();
        return new Date[] {min, max};
    }

    public int getExamEventStartSlot(){
     	return(getStartSlot().intValue() - getEventStartOffset().intValue());
    }
    
    public int getExamEventStopSlot(){
    	return(getEndSlot() + getEventStopOffset().intValue());
    }
    
    public int getExamEventStartOffsetForExam(Exam exam){
    	int startOffset = getEventStartOffset() * Constants.SLOT_LENGTH_MIN;
    	if (exam.getPrintOffset() != null && exam.getPrintOffset().intValue() > 0){
    		startOffset += exam.getPrintOffset().intValue();
    	}
    	return(startOffset);
    }
    
    public boolean isUsed() {
    	return ((Number)ExamPeriodDAO.getInstance().getSession().createQuery("select count(x) from Exam x where x.assignedPeriod.uniqueId = :id").setLong("id", getUniqueId()).setCacheable(true).uniqueResult()).intValue() > 0;
    }
    
    public int getExamEventStopOffsetForExam(Exam exam){
    	return(exam.getLength()
    			- (Constants.SLOT_LENGTH_MIN*getLength())
    			- (getEventStopOffset()*Constants.SLOT_LENGTH_MIN)
    			+ exam.examOffset());
    }
}
