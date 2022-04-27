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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.hibernate.Query;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface.ClassModality;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface.ScheduleGaps;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.base.BaseStudent;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class Student extends BaseStudent implements Comparable<Student>, NameInterface, Qualifiable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Student () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Student (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public static List findAll(Long sessionId) {
        return new StudentDAO().
            getSession().
            createQuery("select s from Student s where s.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
            list();
    }
    
    public static Student findByExternalId(Long sessionId, String externalId) {
        return (Student)new StudentDAO().
            getSession().
            createQuery("select s from Student s where "+
                    "s.session.uniqueId=:sessionId and "+
                    "s.externalUniqueId=:externalId").
            setLong("sessionId", sessionId.longValue()).
            setString("externalId",externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public static Student findByExternalIdBringBackEnrollments(org.hibernate.Session hibSession, Long sessionId, String externalId) {
        return (Student)hibSession.
            createQuery("select s from Student s " +
            		"left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.areaClasfMajors " +
                    "left join fetch s.areaClasfMinors " +
            		"where "+
                    "s.session.uniqueId=:sessionId and "+
                    "s.externalUniqueId=:externalId").
            setLong("sessionId", sessionId.longValue()).
            setString("externalId",externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public void removeAllEnrollments(org.hibernate.Session hibSession){
		HashSet<StudentClassEnrollment> enrollments = new HashSet<StudentClassEnrollment>();
    	if (getClassEnrollments() != null){
    		enrollments.addAll(getClassEnrollments());
    	}
       	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments) {
    			getClassEnrollments().remove(enrollment);
    			hibSession.delete(enrollment);
    		}
    	}

    }

    @Deprecated
    public Set<Exam> getExams(Integer examType) {
        HashSet exams = new HashSet();
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.instructionalOffering.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        return exams;
    }
    
    public Set<Exam> getExams(ExamType examType) {
        HashSet exams = new HashSet();
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        return exams;
    }
    
    public String getName(String instructorNameFormat) {
    	return NameFormat.fromReference(instructorNameFormat).format(this);
    }
    
    public int compareTo(Student student) {
        int cmp = NameFormat.LAST_FIRST.format(this).compareTo(NameFormat.LAST_FIRST.format(student));
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(student.getUniqueId() == null ? -1 : student.getUniqueId());
    }
    
    public static Hashtable<Long,Set<Long>> findConflictingStudents(Long classId, int startSlot, int length, List<Date> dates) {
    	Hashtable<Long,Set<Long>> table = new Hashtable();
    	if (dates.isEmpty()) return table;
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Query q = LocationDAO.getInstance().getSession()
    	    .createQuery("select distinct e.clazz.uniqueId, e.student.uniqueId "+
    	        	"from StudentClassEnrollment e, ClassEvent c inner join c.meetings m, StudentClassEnrollment x "+
    	        	"where x.clazz.uniqueId=:classId and x.student=e.student and " + // only look among students of the given class 
    	        	"e.clazz=c.clazz and " + // link ClassEvent c with StudentClassEnrollment e
            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
            		"m.meetingDate in ("+datesStr+") and m.approvalStatus = 1")
            .setLong("classId",classId)
            .setInteger("startSlot", startSlot)
            .setInteger("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setDate("date"+i, dates.get(i));
    	}
        for (Iterator i = q.setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Set<Long> set = table.get((Long)o[0]);
            if (set==null) {
            	set = new HashSet<Long>();
            	table.put((Long)o[0], set);
            }
            set.add((Long)o[1]);
        }
        return table;
    }
    
    public boolean hasSectioningStatusOption(StudentSectioningStatus.Option option) {
    	StudentSectioningStatus status = getEffectiveStatus();
    	return status != null && status.hasOption(option);
    }

	@Override
	public String getAcademicTitle() { return null; }
	
	@Override
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	public String getQualifierType() {
		return getClass().getSimpleName();
	}

	@Override
	public String getQualifierReference() {
		return getExternalUniqueId();
	}

	@Override
	public String getQualifierLabel() {
		return NameFormat.LAST_FIRST_MIDDLE.format(this);
	}
	
	public CourseRequestOverrideStatus getMaxCreditOverrideStatus() {
    	if (getOverrideStatus() == null) return CourseRequestOverrideStatus.APPROVED;
    	return CourseRequestOverrideStatus.values()[getOverrideStatus()];
    }
    
    public void setMaxCreditOverrideStatus(CourseRequestOverrideStatus status) {
    	setOverrideStatus(status == null ? null : Integer.valueOf(status.ordinal()));
    }
    
    public boolean isRequestApproved() {
    	return getOverrideStatus() == null || getOverrideStatus().intValue() == CourseRequestOverrideStatus.APPROVED.ordinal();
    }
    
    public boolean isRequestPending() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.PENDING.ordinal();
    }
    
    public boolean isRequestCancelled() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.CANCELLED.ordinal();
    }
    
    public boolean isRequestRejected() {
    	return getOverrideStatus() != null && getOverrideStatus().intValue() == CourseRequestOverrideStatus.REJECTED.ordinal();
    }
    
    public StudentSectioningStatus getEffectiveStatus() {
    	if (getSectioningStatus() != null) {
			if (getSectioningStatus().isEffectiveNow())
				return getSectioningStatus();
			StudentSectioningStatus fallback = getSectioningStatus().getFallBackStatus();
			int depth = 10;
			while (fallback != null && depth -- > 0) {
				if (fallback.isEffectiveNow())
					return fallback;
				else
					fallback = fallback.getFallBackStatus();
			}
		}
    	if (getSession().getDefaultSectioningStatus() != null) {
    		if (getSession().getDefaultSectioningStatus().isEffectiveNow())
				return getSession().getDefaultSectioningStatus();
    		StudentSectioningStatus fallback = getSession().getDefaultSectioningStatus().getFallBackStatus();
			int depth = 10;
			while (fallback != null && depth -- > 0) {
				if (fallback.isEffectiveNow())
					return fallback;
				else
					fallback = fallback.getFallBackStatus();
			}
    	}
    	return null;
    }
    
    public WaitListMode getWaitListMode() {
    	StudentSectioningStatus status = getEffectiveStatus();
		if (CustomStudentEnrollmentHolder.isAllowWaitListing() && (status == null || status.hasOption(Option.waitlist))) {
			return WaitListMode.WaitList;
		} else if (status != null && status.hasOption(Option.nosubs)) {
			return WaitListMode.NoSubs;
		}
		return WaitListMode.None;
    }
    
    public Date getLastChangedByStudent() {
    	Date ret = null;
    	if (getCourseDemands() != null) {
    		for (CourseDemand cd: getCourseDemands()) {
    			if (getExternalUniqueId() != null && getExternalUniqueId().equals(cd.getChangedBy())) {
    				if (ret == null || ret.before(cd.getTimestamp())) ret = cd.getTimestamp();
    				if (cd.getWaitlistedTimeStamp() != null && (ret == null || ret.before(cd.getWaitlistedTimeStamp()))) ret = cd.getWaitlistedTimeStamp();
    			}
    		}
    	}
    	if (getClassEnrollments() != null) {
    		for (StudentClassEnrollment e: getClassEnrollments()) {
    			if (getExternalUniqueId() != null && getExternalUniqueId().equals(e.getChangedBy())) {
    				if (ret == null || ret.before(e.getTimestamp())) ret = e.getTimestamp();
    			}
    		}
    	}
    	return ret;
    }
    
    public boolean hasReleasedPin() { return getPin() != null && !getPin().isEmpty() && isPinReleased() != null && isPinReleased().booleanValue(); }
    public String getReleasedPin() { return (hasReleasedPin() ? getPin() : null); }
    
    public Set<Long> getAdvisorWaitListedCourseIds(boolean useWaitLists, boolean useNoSubs) {
    	if (!useWaitLists && !useNoSubs) return null;
    	if (getAdvisorCourseRequests() == null || getAdvisorCourseRequests().isEmpty()) return null;
    	Set<Long> courseIds = new HashSet<Long>();
    	for (AdvisorCourseRequest acr: getAdvisorCourseRequests()) {
    		if (useWaitLists && acr.getWaitlist() != null && acr.getWaitlist().booleanValue() && acr.getCourseOffering() != null) {
    			courseIds.add(acr.getCourseOffering().getUniqueId());
    		}
    		if (useNoSubs && acr.getNoSub() != null && acr.getNoSub().booleanValue() && acr.getCourseOffering() != null) {
    			courseIds.add(acr.getCourseOffering().getUniqueId());
    		}
    	}
    	return courseIds;
    }
    
    public Set<Long> getAdvisorWaitListedCourseIds(OnlineSectioningServer server) {
    	if (server == null) {
    		return getAdvisorWaitListedCourseIds(
    				ApplicationProperty.OnlineSchedulingParameter.isTrue("Load.UseAdvisorWaitLists"),
    				ApplicationProperty.OnlineSchedulingParameter.isTrue("Load.UseAdvisorNoSubs")
    				);
    	} else {
    		return getAdvisorWaitListedCourseIds(
    				server.getConfig().getPropertyBoolean("Load.UseAdvisorWaitLists", false),
    				server.getConfig().getPropertyBoolean("Load.UseAdvisorNoSubs", false)
    				);
    	}
    }
    
    public void setMaxCreditOverrideIntent(CourseRequestOverrideIntent intent) {
    	if (intent == null)
    		setOverrideIntent(null);
    	else
    		setOverrideIntent(intent.ordinal());
    }
    
    public CourseRequestOverrideIntent getMaxCreditOverrideIntent() {
    	return (getOverrideIntent() == null ? null : CourseRequestOverrideIntent.values()[getOverrideIntent()]); 
    }
    
    public StudentAreaClassificationMajor getPrimaryAreaClasfMajor() {
    	if (getAreaClasfMajors() == null) return null;
    	StudentAreaClassificationMajor major = null;
    	for (StudentAreaClassificationMajor m: getAreaClasfMajors()) {
    		if (major == null || m.compareTo(major) < 0)
    			major = m;
    	}
    	return major;
    }
    
    public CourseRequest getCourseRequest(CourseOffering co) {
    	for (CourseDemand cd: getCourseDemands())
    		for (CourseRequest cr: cd.getCourseRequests())
    			if (cr.getCourseOffering().equals(co)) return cr;
    	return null;
    }
    
    public boolean isEnrolled(CourseOffering co) {
    	for (StudentClassEnrollment e: getClassEnrollments())
    		if (e.getCourseOffering().equals(co)) return true;
    	return false;
    }
    
    public WaitList addWaitList(CourseOffering co, WaitList.WaitListType type, boolean waitListed, String changedBy, Date timeStamp, org.hibernate.Session hibSession) {
    	if (ApplicationProperty.WaitListLogging.isFalse()) return null;
    	if (co == null) return null;
    	WaitList last = null;
    	CourseRequest cr = getCourseRequest(co);
    	if (cr != null && getWaitlists() != null && !getWaitlists().isEmpty()) {
    		for (WaitList wl: getWaitlists()) {
    			if ((last == null || last.compareTo(wl) < 0) && wl.hasMatchingCourse(cr.getCourseDemand()))
    				last = wl;
    		}
    	}
    	boolean lastWaitListed = (last == null ? false : last.isWaitListed());
    	CourseOffering lastEnrolled = (last == null ? null : last.getEnrolledCourse());
    	CourseOffering enrolled = (cr == null ? null : cr.getCourseDemand().getEnrolledCourse());
    	if (enrolled != null && cr != null && enrolled.equals(cr.getCourseDemand().getWaitListSwapWithCourseOffering()) && !cr.isRequired()) 
    		enrolled = null; // if section swap and the enrollment does not meet requirements -> not enrolled
    	CourseOffering lastSwap = (last == null ? null : last.getSwapCourseOffering());
    	CourseOffering swap = (cr == null ? null : cr.getCourseDemand().getWaitListSwapWithCourseOffering());
    	if (lastWaitListed != waitListed || !(lastEnrolled == null ? enrolled == null : lastEnrolled.equals(enrolled)) || !(lastSwap == null ? swap == null : lastSwap.equals(swap))) {
    		WaitList wl = new WaitList();
    		wl.setChangedBy(changedBy);
    		wl.setCourseOffering(cr == null ? co : cr.getCourseDemand().getFirstChoiceCourseOffering());
    		wl.setEnrolledCourse(enrolled);
    		wl.setTimestamp(timeStamp);
    		wl.setWaitListType(type);
    		wl.setWaitListed(waitListed);
    		wl.setWaitListedTimeStamp(cr == null ? null : cr.getCourseDemand().getWaitlistedTimeStamp());
    		wl.setStudent(this);
    		wl.setCourseDemand(cr == null ? null : cr.getCourseDemand());
    		wl.setSwapCourseOffering(cr == null ? null : cr.getCourseDemand().getWaitListSwapWithCourseOffering());
    		wl.fillInNotes();
    		addTowaitlists(wl);
    		if (hibSession != null) hibSession.save(wl);
    		return wl;
    	} else {
    		return null;
    	}
    }
    
    public void resetWaitLists(WaitList.WaitListType type, String changedBy, Date timeStamp, org.hibernate.Session hibSession) {
    	if (ApplicationProperty.WaitListLogging.isFalse()) return;
    	if (timeStamp == null) timeStamp = new Date();
    	Map<CourseOffering, WaitList> waitlists = new HashMap<CourseOffering, WaitList>();
		if (getWaitlists() != null)
			for (WaitList wl: getWaitlists()) {
				WaitList other = waitlists.get(wl.getCourseOffering());
				if (other == null || other.compareTo(wl) < 0)
					waitlists.put(wl.getCourseOffering(), wl);
			}
		for (StudentClassEnrollment e: getClassEnrollments()) {
			WaitList wl = waitlists.remove(e.getCourseOffering());
			if (wl != null && wl.isWaitListed()) {
				// wait-list enrolled
				if (e.getCourseOffering().equals(wl.getSwapCourseOffering())) { // is section swap
					CourseRequest cr = getCourseRequest(e.getCourseOffering());
					if (cr != null && !cr.isRequired()) {
						// requirements not met -> put back (it is considered not enrolled)
						waitlists.put(e.getCourseOffering(), wl);
						continue;
					}
				}
				wl = new WaitList();
				wl.setChangedBy(changedBy);
				wl.setTimestamp(timeStamp);
				CourseRequest cr = getCourseRequest(e.getCourseOffering());
				wl.setCourseOffering(cr != null ? cr.getCourseDemand().getFirstChoiceCourseOffering() : e.getCourseOffering());
				wl.setCourseDemand(cr == null ? null : cr.getCourseDemand());
				wl.setEnrolledCourse(e.getCourseOffering());
				wl.setWaitListType(type);
				wl.setWaitListed(false);
				wl.setStudent(this);
				wl.setWaitListedTimeStamp(cr == null ? null : cr.getCourseDemand().getWaitlistedTimeStamp());
				wl.setSwapCourseOffering(cr == null ? null : cr.getCourseDemand().getWaitListSwapWithCourseOffering());
				wl.fillInNotes();
				addTowaitlists(wl);
				if (hibSession != null) hibSession.save(wl);
			}
		}
		for (CourseDemand cd: getCourseDemands()) {
			if (cd.isWaitlist() && !cd.isEnrolled(true)) {
				CourseOffering co = cd.getFirstChoiceCourseOffering();
				if (co != null) {
					WaitList old = waitlists.remove(co);
					if (old == null || !old.isWaitListed() || !WaitList.computeRequest(cd).equals(old.getRequest()) || 
							!(old.getSwapCourseOffering() == null ? cd.getWaitListSwapWithCourseOffering() == null : old.getSwapCourseOffering().equals(cd.getWaitListSwapWithCourseOffering()))) {
						// new wait-lists
						WaitList wl = new WaitList();
						wl.setChangedBy(changedBy);
						wl.setTimestamp(timeStamp);
						wl.setCourseOffering(co);
						wl.setCourseDemand(cd);
						wl.setWaitListType(type);
						wl.setWaitListed(true);
						wl.setStudent(this);
						wl.setWaitListedTimeStamp(cd.getWaitlistedTimeStamp());
						wl.setSwapCourseOffering(cd.getWaitListSwapWithCourseOffering());
						wl.fillInNotes();
						addTowaitlists(wl);
						if (hibSession != null) hibSession.save(wl);
					}
				}
			}
		}
		for (Map.Entry<CourseOffering, WaitList> e: waitlists.entrySet()) {
			CourseOffering co = e.getKey();
			WaitList old = e.getValue();
			if (old.isWaitListed()) {
				// removed wait-list
				WaitList wl = new WaitList();
				wl.setChangedBy(changedBy);
				wl.setTimestamp(timeStamp);
				wl.setCourseOffering(co);
				CourseRequest cr = getCourseRequest(co);
				wl.setCourseDemand(cr == null ? null : cr.getCourseDemand());
				wl.setWaitListType(type);
				wl.setWaitListed(false);
				wl.setStudent(this);
				wl.setWaitListedTimeStamp(old.getWaitListedTimeStamp());
				wl.setSwapCourseOffering(old.getSwapCourseOffering());
				wl.fillInNotes();
				addTowaitlists(wl);
				if (hibSession != null) hibSession.save(wl);
			}
		}
    }
    
    public ClassModality getPreferredClassModality() {
    	if (getSchedulePreference() == null) return ClassModality.NoPreference;
    	return ClassModality.values()[getSchedulePreference()];
    }
    public void setPreferredClassModality(ClassModality modality) {
    	if (modality == null)
    		setSchedulePreference(null);
    	else
    		setSchedulePreference(modality.ordinal());
    }
    public ModalityPreference getModalityPreference() {
    	switch(getPreferredClassModality()) {
    	case DiscouragedOnline: return ModalityPreference.ONILNE_DISCOURAGED;
    	case PreferredOnline: return ModalityPreference.ONLINE_PREFERRED;
    	case RequiredOnline: return ModalityPreference.ONLINE_REQUIRED;
    	default: return ModalityPreference.NO_PREFERENCE;
    	}
    }
    
    public ScheduleGaps getPreferredScheduleGaps() {
    	if (getFreeTimeCategory() == null) return ScheduleGaps.NoPreference;
    	return ScheduleGaps.values()[getFreeTimeCategory()];
    }
    public void setPreferredScheduleGaps(ScheduleGaps gaps) {
    	if (gaps == null)
    		setFreeTimeCategory(null);
    	else
    		setFreeTimeCategory(gaps.ordinal());
    }
    public BackToBackPreference getBackToBackPreference() {
    	switch (getPreferredScheduleGaps()) {
    	case DiscourageBackToBack: return BackToBackPreference.BTB_DISCOURAGED;
    	case PreferBackToBack: return BackToBackPreference.BTB_PREFERRED;
    	default: return BackToBackPreference.NO_PREFERENCE;
    	}
    }
}
