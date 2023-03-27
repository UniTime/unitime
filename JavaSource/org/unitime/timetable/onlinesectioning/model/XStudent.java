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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface.ClassModality;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface.ScheduleGaps;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest.XPreference;

/**
 * @author Tomas Muller
 */
public class XStudent extends XStudentId implements Externalizable {
	private static final long serialVersionUID = 1L;
    private List<XAreaClassificationMajor> iMajors = new ArrayList<XAreaClassificationMajor>();
    private List<XAreaClassificationMajor> iMinors = new ArrayList<XAreaClassificationMajor>();
    private List<XGroup> iGroups = new ArrayList<XGroup>();
    private List<XGroup> iAccomodations = new ArrayList<XGroup>();
    private List<XRequest> iRequests = new ArrayList<XRequest>();
    private String iStatus = null;
    private String iEmail = null;
    private Date iEmailTimeStamp = null;
    private List<XInstructorAssignment> iInstructorAssignments = new ArrayList<XInstructorAssignment>();
    private XStudentNote iLastNote = null;
    private Float iMaxCredit = null;
    private XOverride iMaxCreditOverride = null;
    private boolean iAllowDisabled = false;
    private List<XAdvisor> iAdvisors = new ArrayList<XAdvisor>();
    private Date iLastStudentChange = null;
    private List<XAdvisorRequest> iAdvisorRequests = null;
    private String iPin;
    private boolean iPinReleased = false;
    private Map<Long, Long> iFailedWaitLists = new HashMap<Long, Long>();
    private Integer iClassStartDate, iClassEndDate;
    private ModalityPreference iModalityPreference = ModalityPreference.NO_PREFERENCE;
    private BackToBackPreference iBackToBackPreference = BackToBackPreference.NO_PREFERENCE;

    public XStudent() {
    	super();
    }
    
    public XStudent(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XStudent(Long studentId, String externalId, String name) {
    	super(studentId, externalId, name);
    }

    public XStudent(Student student, OnlineSectioningHelper helper, BitSet freeTimePattern, Date firstDay) {
    	super(student, helper);
    	iStatus = student.getSectioningStatus() == null ? null : student.getSectioningStatus().getReference();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate();
    	iLastStudentChange = student.getLastChangedByStudent();
    	for (StudentAreaClassificationMajor acm: student.getAreaClasfMajors()) {
        	iMajors.add(new XAreaClassificationMajor(acm));
        }
    	if (iMajors.size() > 1) Collections.sort(iMajors);
    	for (StudentAreaClassificationMinor acm: student.getAreaClasfMinors()) {
        	iMinors.add(new XAreaClassificationMajor(acm));
        }
    	if (iMinors.size() > 1) Collections.sort(iMinors);
        for (StudentGroup group: student.getGroups()) {
        	iGroups.add(new XGroup(group));
        	StudentGroupType type = group.getType();
        	if (type != null && type.getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.AlwaysAllowed)
        		iAllowDisabled = true;
        }
        for (StudentAccomodation accomodation: student.getAccomodations())
        	iAccomodations.add(new XGroup(accomodation));
        for (Advisor advisor: student.getAdvisors())
        	iAdvisors.add(new XAdvisor(advisor.getExternalUniqueId(), advisor.getLastName() == null ? null : helper.getInstructorNameFormat().format(advisor), advisor.getEmail()));
        
		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		demands.addAll(student.getCourseDemands());
        for (CourseDemand cd: demands) {
            if (cd.getFreeTime() != null) {
            	iRequests.add(new XFreeTimeRequest(cd, freeTimePattern));
            } else if (!cd.getCourseRequests().isEmpty()) {
            	iRequests.add(new XCourseRequest(cd, helper));
            }
        }
        
        Map<CourseOffering, List<StudentClassEnrollment>> unmatchedCourses = new HashMap<CourseOffering, List<StudentClassEnrollment>>();
        for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
        	if (getRequestForCourse(enrollment.getCourseOffering().getUniqueId()) != null) continue;
        	List<StudentClassEnrollment> classes = unmatchedCourses.get(enrollment.getCourseOffering());
        	if (classes == null) {
        		classes = new ArrayList<StudentClassEnrollment>();
        		unmatchedCourses.put(enrollment.getCourseOffering(), classes);
        	}
        	classes.add(enrollment);
        }
        if (!unmatchedCourses.isEmpty()) {
        	int priority = 0;
        	for (XRequest request: iRequests)
        		if (!request.isAlternative() && request.getPriority() > priority) priority = request.getPriority();
            for (CourseOffering course: new TreeSet<CourseOffering>(unmatchedCourses.keySet())) {
            	List<StudentClassEnrollment> classes = unmatchedCourses.get(course);
            	iRequests.add(new XCourseRequest(student, course, ++priority, helper, classes));
            }
        }
        
        Collections.sort(iRequests);
        
        StudentNote note = null;
        if (student.getNotes() != null)
            for (StudentNote n: student.getNotes()) {
            	if (note == null || note.compareTo(n) < 0) note = n;
            }
        if (note != null)
        	iLastNote = new XStudentNote(note);
        
        iMaxCredit = student.getMaxCredit();
        if (student.getOverrideMaxCredit() != null)
        	iMaxCreditOverride = new XOverride(student.getOverrideExternalId(), student.getOverrideTimeStamp(), student.getOverrideStatus(), student.getOverrideMaxCredit());
        
        setAdvisorRequests(student, helper, freeTimePattern);
        
        iPin = student.getPin();
        iPinReleased = (student.isPinReleased() != null && student.isPinReleased().booleanValue());
        updatePreferences(student, firstDay);
    }
    
    public void updatePreferences(Student student, Date firstDay) {
        if (student.getClassStartDate() != null)
        	iClassStartDate = Days.daysBetween(new LocalDate(firstDay), new LocalDate(student.getClassStartDate())).getDays();
        else
        	iClassStartDate = null;
        if (student.getClassEndDate() != null)
        	iClassEndDate = Days.daysBetween(new LocalDate(firstDay), new LocalDate(student.getClassEndDate())).getDays();
        else
        	iClassEndDate = null;
        iModalityPreference = student.getModalityPreference();
        iBackToBackPreference = student.getBackToBackPreference();    	
    }
    
    public void setAdvisorRequests(Student student, OnlineSectioningHelper helper, BitSet freeTimePattern) {
        if (student.getAdvisorCourseRequests() != null && !student.getAdvisorCourseRequests().isEmpty()) {
        	iAdvisorRequests = new ArrayList<XAdvisorRequest>();
        	for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests())
        		iAdvisorRequests.add(new XAdvisorRequest(acr, helper, freeTimePattern));
        	Collections.sort(iAdvisorRequests);
        } else {
        	iAdvisorRequests = null;
        }
    }
    
    public void setAdvisorRequests(List<AdvisorCourseRequest> acrs, OnlineSectioningHelper helper, BitSet freeTimePattern) {
        if (acrs != null && !acrs.isEmpty()) {
        	iAdvisorRequests = new ArrayList<XAdvisorRequest>();
        	for (AdvisorCourseRequest acr: acrs)
        		iAdvisorRequests.add(new XAdvisorRequest(acr, helper, freeTimePattern));
        	Collections.sort(iAdvisorRequests);
        } else {
        	iAdvisorRequests = null;
        }
    }
    
    public XStudent(XStudent student) {
    	super(student);
    	iStatus = student.getStatus();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getEmailTimeStamp();
    	iLastStudentChange = student.getLastStudentChange();
    	iMajors.addAll(student.getMajors());
    	iMinors.addAll(student.getMinors());
    	iGroups.addAll(student.getGroups());
    	iAccomodations.addAll(student.getAccomodations());
    	iRequests.addAll(student.getRequests());
    	iAllowDisabled = student.iAllowDisabled;
    	iAdvisors.addAll(student.getAdvisors());
    	if (student.hasAdvisorRequests())
    		iAdvisorRequests = new ArrayList<XAdvisorRequest>(student.getAdvisorRequests());
    	iInstructorAssignments.addAll(student.getInstructorAssignments());
    	iPin = student.iPin;
    	iPinReleased = student.iPinReleased;
    	iMaxCredit = student.iMaxCredit;
    	iMaxCreditOverride = student.iMaxCreditOverride;
    	iLastNote = student.iLastNote;
    	iClassStartDate = student.iClassStartDate;
    	iClassEndDate = student.iClassEndDate;
    	iBackToBackPreference = student.iBackToBackPreference;
    	iModalityPreference = student.iModalityPreference;
    }
    
    public XStudent(XStudent student, Collection<CourseDemand> demands, OnlineSectioningHelper helper, BitSet freeTimePattern) {
    	super(student);
    	iStatus = student.getStatus();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getEmailTimeStamp();
    	iLastStudentChange = student.getLastStudentChange();
    	iMajors.addAll(student.getMajors());
    	iMinors.addAll(student.getMinors());
    	iGroups.addAll(student.getGroups());
    	iAccomodations.addAll(student.getAccomodations());
    	iMaxCredit = student.getMaxCredit();
    	iMaxCreditOverride = student.getMaxCreditOverride();
    	iAllowDisabled = student.iAllowDisabled;
    	iAdvisors.addAll(student.getAdvisors());
    	if (student.hasAdvisorRequests())
    		iAdvisorRequests = new ArrayList<XAdvisorRequest>(student.getAdvisorRequests());
    	iInstructorAssignments.addAll(student.getInstructorAssignments());
    	iPin = student.iPin;
    	iPinReleased = student.iPinReleased;
    	iMaxCredit = student.iMaxCredit;
    	iMaxCreditOverride = student.iMaxCreditOverride;
    	iLastNote = student.iLastNote;
    	iClassStartDate = student.iClassStartDate;
    	iClassEndDate = student.iClassEndDate;
    	iBackToBackPreference = student.iBackToBackPreference;
    	iModalityPreference = student.iModalityPreference;
    	

    	if (demands != null)
        	for (CourseDemand cd: demands) {
                if (cd.getFreeTime() != null) {
                	iRequests.add(new XFreeTimeRequest(cd, freeTimePattern));
                } else if (!cd.getCourseRequests().isEmpty()) {
                	iRequests.add(new XCourseRequest(cd, helper));
                }
            }
    	
    	Collections.sort(iRequests);
    }
    
    public static List<XRequest> loadRequests(Student student, OnlineSectioningHelper helper, BitSet freeTimePattern) {
    	List<XRequest> requests = new ArrayList<XRequest>();
		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		demands.addAll(student.getCourseDemands());
    	for (CourseDemand cd: demands) {
            if (cd.getFreeTime() != null) {
            	requests.add(new XFreeTimeRequest(cd, freeTimePattern));
            } else if (!cd.getCourseRequests().isEmpty()) {
            	requests.add(new XCourseRequest(cd, helper));
            }
        }
        Collections.sort(requests);
        return requests;
    }
    
    public XStudent(org.cpsolver.studentsct.model.Student student, Assignment<Request, Enrollment> assignment) {
    	super(student);
    	iStatus = student.getStatus();
    	iAllowDisabled = student.isAllowDisabled();
    	iEmailTimeStamp = (student.getEmailTimeStamp() == null ? null : new Date(student.getEmailTimeStamp()));
    	if (student.hasMaxCredit())
    		iMaxCredit = student.getMaxCredit();
    	for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
    		iMajors.add(new XAreaClassificationMajor(acm));
    	}
    	if (iMajors.size() > 1) Collections.sort(iMajors);
    	for (AreaClassificationMajor acm: student.getAreaClassificationMinors()) {
    		iMinors.add(new XAreaClassificationMajor(acm));
    	}
    	if (iMinors.size() > 1) Collections.sort(iMinors);
    	for (org.cpsolver.studentsct.model.StudentGroup gr: student.getGroups())
    		iGroups.add(new XGroup(gr));
    	for (String acc: student.getAccommodations())
    		iAccomodations.add(new XGroup(acc, null));
    	for (Instructor advisor: student.getAdvisors())
    		iAdvisors.add(new XAdvisor(advisor.getExternalId(), advisor.getName(), advisor.getEmail()));
    	for (Request request: student.getRequests()) {
    		if (request instanceof FreeTimeRequest) {
    			iRequests.add(new XFreeTimeRequest((FreeTimeRequest)request));
    		} else if (request instanceof CourseRequest) {
    			iRequests.add(new XCourseRequest((CourseRequest)request, assignment == null ? null : assignment.getValue(request)));
    		}
    	}
    	iClassStartDate = student.getClassFirstDate();
    	iClassEndDate = student.getClassLastDate();
    	iBackToBackPreference = student.getBackToBackPreference();
    	iModalityPreference = student.getModalityPreference();
    }
    
    public String getPin() { return iPin; }
    public void setPin(String pin) { iPin = pin; }
    public boolean isPinReleased() { return iPinReleased; }
    public void setPinReleased(boolean released) { iPinReleased = released; }
    public boolean hasReleasedPin() { return isPinReleased() && getPin() != null && !getPin().isEmpty(); }
    public String getReleasedPin() { return (hasReleasedPin() ? getPin() : null); }
    
    public List<XInstructorAssignment> getInstructorAssignments() { return iInstructorAssignments; }
    public boolean hasInstructorAssignments() { return iInstructorAssignments != null && !iInstructorAssignments.isEmpty(); }
    
    public XStudentNote getLastNote() { return iLastNote; }
    public boolean hasLastNote() { return iLastNote != null && iLastNote.hasNote(); }
    public void setLastNote(XStudentNote note) { iLastNote = note; }

    public XCourseRequest getRequestForCourse(Long courseId) {
    	for (XRequest request: iRequests)
    		if (request instanceof XCourseRequest && ((XCourseRequest)request).hasCourse(courseId))
    			return (XCourseRequest)request;
    	return null;
    }
    
    public XFreeTimeRequest getRequestForFreeTime(CourseRequestInterface.FreeTime ft) {
    	for (XRequest request: iRequests) {
    		if (request instanceof XFreeTimeRequest) {
    			XFreeTimeRequest ftRequest = (XFreeTimeRequest)request;
    			if (ftRequest.getTime() != null && ftRequest.getTime().equals(ft)) return ftRequest; 
    		}
    	}
    	return null;
    }
    
    public XCourseRequest getRequestForCourseName(String courseName) {
    	for (XRequest request: iRequests)
    		if (request instanceof XCourseRequest && ((XCourseRequest)request).hasCourseName(courseName))
    			return (XCourseRequest)request;
    	return null;
    }
    
    public XAdvisorRequest getAdvisorRequestForCourse(Long courseId) {
    	if (iAdvisorRequests == null) return null;
    	for (XAdvisorRequest request: iAdvisorRequests)
    		if (request.hasCourseId() && request.getCourseId().getCourseId().equals(courseId))
    			return request;
    	return null;
    }
    
    public XAdvisorRequest getAdvisorRequestForFreeTime(CourseRequestInterface.FreeTime ft) {
    	if (iAdvisorRequests == null) return null;
    	for (XAdvisorRequest request: iAdvisorRequests)
    		if (request.hasFreeTime() && request.getFreeTime().equals(ft))
    			return request;
    	return null;
    }
    
    public Float getMaxCredit() { return iMaxCredit; }
    public boolean hasMaxCredit() { return iMaxCredit != null; }
    public void setMaxCredit(Float maxCredit) { iMaxCredit = maxCredit; }
    public XOverride getMaxCreditOverride() { return iMaxCreditOverride; }
    public boolean isMaxCreditOverridePending() {
    	return (iMaxCreditOverride == null || iMaxCreditOverride.getStatus() == null ? false : iMaxCreditOverride.getStatus().intValue() == CourseRequestOverrideStatus.PENDING.ordinal());
    }
    public void setMaxCreditOverride(XOverride maxCreditOverride) {
    	iMaxCreditOverride = maxCreditOverride;
    }
    public boolean isAllowDisabled() { return iAllowDisabled; }
    public void setAllowDisabled(boolean allowDisabled) { iAllowDisabled = allowDisabled; }
    
    public Integer getClassStartDate() { return iClassStartDate; }
    public Integer getClassEndDate() { return iClassEndDate; }
    public ModalityPreference getModalityPreference() { return iModalityPreference; }
    public BackToBackPreference getBackToBackPreference() { return iBackToBackPreference; }
    public ClassModality getPreferredClassModality() {
    	if (iModalityPreference == null) return ClassModality.NoPreference;
    	switch (iModalityPreference) {
    	case ONILNE_DISCOURAGED: return ClassModality.DiscouragedOnline;
    	case ONLINE_PREFERRED: return ClassModality.PreferredOnline;
    	case ONLINE_REQUIRED: return ClassModality.RequiredOnline;
    	default: return ClassModality.NoPreference;
    	}
    }
    public ScheduleGaps getPreferredScheduleGaps() {
    	if (iBackToBackPreference == null) return ScheduleGaps.NoPreference;
    	switch (iBackToBackPreference) {
    	case BTB_DISCOURAGED: return ScheduleGaps.DiscourageBackToBack;
    	case BTB_PREFERRED: return ScheduleGaps.PreferBackToBack;
    	default: return ScheduleGaps.NoPreference;
    	}
    }
    public Date getClassStartDate(Date datePatternFirstDate) {
    	if (iClassStartDate == null) return null;
    	return new LocalDate(datePatternFirstDate).plusDays(iClassStartDate).toDate();
    }
    public Date getClassEndDate(Date datePatternFirstDate) {
    	if (iClassEndDate == null) return null;
    	return new LocalDate(datePatternFirstDate).plusDays(iClassEndDate).toDate();
    }
    
    public boolean hasRequirements(XCourseId course) {
		if (iClassStartDate != null) return true;
		if (iClassEndDate != null) return true;
		if (iModalityPreference == ModalityPreference.ONLINE_REQUIRED) return true;
		if (course != null) {
			XCourseRequest cr = getRequestForCourse(course.getCourseId());
			if (cr != null) {
				List<XPreference> prefs = cr.getPreferences(course);
				if (prefs != null)
					for (XPreference p: prefs)
						if (p.isRequired()) return true;
			}
		}
		return false;
	}

    /**
     * List of academic area, classification, and major codes ({@link XAreaClassificationMajor}) for the given student
     */
    public List<XAreaClassificationMajor> getMajors() {
        return iMajors;
    }
    
    public XAreaClassificationMajor getPrimaryMajor() {
    	if (iMajors == null) return null;
    	XAreaClassificationMajor major = null;
    	for (XAreaClassificationMajor m: iMajors) {
    		if (major == null || m.compareTo(major) < 0)
    			major = m;
    	}
    	return major;
    }
    
    /**
     * List of academic area, classification, and minor codes ({@link XAreaClassificationMajor}) for the given student
     */
    public List<XAreaClassificationMajor> getMinors() {
        return iMinors;
    }

    /**
     * List of group codes for the given student
     */
    public List<XGroup> getGroups() {
        return iGroups;
    }

    /**
     * List of group codes for the given student
     */
    public List<XGroup> getAccomodations() {
        return iAccomodations;
    }
    
    public boolean hasAccomodation(String accomodation) {
    	if (accomodation != null)
        	for (XGroup acc: iAccomodations)
        		if (accomodation.equals(acc.getAbbreviation())) return true;
    	return false;
    }
    
    public List<XAdvisor> getAdvisors() {
        return iAdvisors;
    }
    
    public boolean hasAdvisorRequests() { return iAdvisorRequests != null && !iAdvisorRequests.isEmpty(); }
    public List<XAdvisorRequest> getAdvisorRequests() { return iAdvisorRequests; }
        
    /**
     * Get student status (online sectioning only)
     */
    public String getStatus() { return iStatus; }
    
    public WaitListMode getWaitListMode(OnlineSectioningHelper helper) {
    	Student student = StudentDAO.getInstance().get(getStudentId(), helper.getHibSession());
    	if (student == null) return WaitListMode.None;
    	StudentSectioningStatus status = student.getEffectiveStatus();
    	if (status == null) return WaitListMode.WaitList;
    	if (status.hasOption(Option.waitlist)) return WaitListMode.WaitList;
    	if (status.hasOption(Option.nosubs)) return WaitListMode.NoSubs;
    	return WaitListMode.None;
    }
    /**
     * Set student status
     */
    public void setStatus(String status) { iStatus = status; }
    
    /**
     * Get last email time stamp (online sectioning only)
     */
    public Date getEmailTimeStamp() { return iEmailTimeStamp; }
    /**
     * Set last email time stamp
     */
    public void setEmailTimeStamp(Date emailTimeStamp) { iEmailTimeStamp = emailTimeStamp; }
    
    public Date getLastStudentChange() { return iLastStudentChange; }
    public void setLastStudentChange(Date lastStudentChange) { iLastStudentChange = lastStudentChange; }
    
    public List<XRequest> getRequests() { return iRequests; }
    
    public String getEmail() { return iEmail; }
    
    /**
     * True if the given request can be assigned to the student. A request
     * cannot be assigned to a student when the student already has the desired
     * number of requests assigned (i.e., number of non-alternative course
     * requests).
     **/
    public boolean canAssign(XCourseRequest request, WaitListMode mode) {
        if (request.getEnrollment() != null)
            return true;
        if (!request.isAlternative() && request.isWaitlist() && mode == WaitListMode.WaitList)
        	return true;
        int alt = 0;
        boolean found = false;
        for (XRequest r : iRequests) {
            if (r.equals(request)) found = true;
            boolean course = (r instanceof XCourseRequest);
            boolean assigned = (!course || ((XCourseRequest)r).getEnrollment() != null || r.equals(request));
            boolean waitlist = (course && ((XCourseRequest)r).isWaitListOrNoSub(mode));
            if (r.isAlternative()) {
                if (assigned || (!found && waitlist))
                    alt--;
            } else {
                if (course && !waitlist && !assigned)
                    alt++;
            }
        }
        return (alt >= 0);
    }
    
    public Set<Long> getRequestedCourseIds() {
    	Set<Long> courseIds = new HashSet<Long>();
    	for (XRequest request: getRequests())
    		if (request instanceof XCourseRequest)
    			for (XCourseId course: ((XCourseRequest)request).getCourseIds())
    				courseIds.add(course.getCourseId());
    	if (hasAdvisorRequests())
    		for (XAdvisorRequest request: getAdvisorRequests())
    			if (request.hasCourseId())
    				courseIds.add(request.getCourseId().getCourseId());
    	return courseIds;
    }
    
    public Set<Long> getAdvisorWaitListedCourseIds(boolean useWaitList, boolean useNoSubs) {
    	if (!useWaitList && !useNoSubs) return null;
    	if (!hasAdvisorRequests()) return null;
    	Set<Long> courseIds = new HashSet<Long>();
    	for (XAdvisorRequest request: getAdvisorRequests()) {
    			if (useWaitList && request.hasCourseId() && request.isWaitList() && !request.isSubstitute())
    				courseIds.add(request.getCourseId().getCourseId());
    			if (useNoSubs && request.hasCourseId() && request.isNoSub() && !request.isSubstitute())
    				courseIds.add(request.getCourseId().getCourseId());
    	}
    	return courseIds;
    }
    
    public Set<Long> getAdvisorWaitListedCourseIds(OnlineSectioningServer server) {
    	return getAdvisorWaitListedCourseIds(
    			server.getConfig().getPropertyBoolean("Load.UseAdvisorWaitLists", false),
    			server.getConfig().getPropertyBoolean("Load.UseAdvisorNoSubs", false)
    			);
    }
    
    public boolean isEnrolled(XCourseId courseId) {
    	if (courseId == null) return false;
    	for (XRequest r: iRequests) {
    		if (r instanceof XCourseRequest) {
    			XCourseRequest cr = (XCourseRequest)r;
    			if (courseId.equals(cr.getEnrollment())) return true;
    		}
    	}
    	return false;
    }
    
    
    
    @Override
    public String toString() {
    	return getName() + " (" + getExternalId() + ")";
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		int nrMajors = in.readInt();
		iMajors.clear();
		for (int i = 0; i < nrMajors; i++)
			iMajors.add(new XAreaClassificationMajor(in));
		
		int nrMinors = in.readInt();
		iMinors.clear();
		for (int i = 0; i < nrMinors; i++)
			iMinors.add(new XAreaClassificationMajor(in));
		
		int nrGroups = in.readInt();
		iGroups.clear();
		for (int i = 0; i < nrGroups; i++)
			iGroups.add(new XGroup(in));
		
		int nrAccomodations = in.readInt();
		iAccomodations.clear();
		for (int i = 0; i < nrAccomodations; i++)
			iAccomodations.add(new XGroup(in));
		
		int nrRequests = in.readInt();
		iRequests.clear();
		for (int i = 0; i < nrRequests; i++)
			iRequests.add(in.readBoolean() ? new XCourseRequest(in) : new XFreeTimeRequest(in));
		
		iStatus = (String)in.readObject();
		iEmail = (String)in.readObject();
		iEmailTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		iLastStudentChange = (in.readBoolean() ? new Date(in.readLong()) : null);
		
		if (in.readBoolean())
			iLastNote = new XStudentNote(in);
		iAllowDisabled = in.readBoolean();
		
		int nrAdvisors = in.readInt();
		iAdvisors.clear();
		for (int i = 0; i < nrAdvisors; i++)
			iAdvisors.add(new XAdvisor(in));
		
		int nrAdvisorRequests = in.readInt();
		if (nrAdvisorRequests < 0) {
			iAdvisorRequests = null;
		} else {
			iAdvisorRequests = new ArrayList<XAdvisorRequest>(nrAdvisorRequests);
			for (int i = 0; i < nrAdvisorRequests; i++)
				iAdvisorRequests.add(new XAdvisorRequest(in));
		}
		iPin = (String)in.readObject();
		iPinReleased = in.readBoolean();
		iClassStartDate = (Integer)in.readObject();
		iClassEndDate = (Integer)in.readObject();
		iBackToBackPreference = BackToBackPreference.values()[in.readInt()];
		iModalityPreference = ModalityPreference.values()[in.readInt()];
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iMajors.size());
		for (XAreaClassificationMajor major: iMajors)
			major.writeExternal(out);
		
		out.writeInt(iMinors.size());
		for (XAreaClassificationMajor minor: iMinors)
			minor.writeExternal(out);
		
		out.writeInt(iGroups.size());
		for (XGroup group: iGroups)
			group.writeExternal(out);
		
		out.writeInt(iAccomodations.size());
		for (XGroup accomodation: iAccomodations)
			accomodation.writeExternal(out);
		
		out.writeInt(iRequests.size());
		for (XRequest request: iRequests)
			if (request instanceof XCourseRequest) {
				out.writeBoolean(true);
				((XCourseRequest)request).writeExternal(out);
			} else {
				out.writeBoolean(false);
				((XFreeTimeRequest)request).writeExternal(out);
			}
		
		out.writeObject(iStatus);
		out.writeObject(iEmail);
		
		out.writeBoolean(iEmailTimeStamp != null);
		if (iEmailTimeStamp != null)
			out.writeLong(iEmailTimeStamp.getTime());
		
		out.writeBoolean(iLastStudentChange != null);
		if (iLastStudentChange != null)
			out.writeLong(iLastStudentChange.getTime());
		
		out.writeBoolean(iLastNote != null);
		if (iLastNote != null)
			iLastNote.writeExternal(out);
		out.writeBoolean(iAllowDisabled);
		
		out.writeInt(iAdvisors.size());
		for (XAdvisor advisor: iAdvisors)
			advisor.writeExternal(out);
		
		if (iAdvisorRequests == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(iAdvisorRequests.size());
			for (XAdvisorRequest ar: iAdvisorRequests)
				ar.writeExternal(out);
		}
		
		out.writeObject(iPin);
		out.writeBoolean(iPinReleased);
		
		out.writeObject(iClassStartDate);
		out.writeObject(iClassEndDate);
		out.writeInt(iBackToBackPreference.ordinal());
		out.writeInt(iModalityPreference.ordinal());
	}
	
	public static class XGroup implements Externalizable {
		public String iType, iAbbreaviation, iTitle;
		
		public XGroup(StudentGroup g) {
			iType = (g.getType() == null ? null: g.getType().getReference());
			iAbbreaviation = g.getGroupAbbreviation();
			iTitle = (g.getGroupName() == null ? null : g.getGroupName());
		}
		
		public XGroup(StudentAccomodation a) {
			iAbbreaviation = a.getAbbreviation();
			iTitle = a.getName();
		}
		
		public XGroup(org.cpsolver.studentsct.model.StudentGroup g) {
			iType = (g.getType() == null || g.getType().isEmpty() ? null : g.getType());
			iAbbreaviation = g.getReference();
			iTitle = (g.getName() == null ? null : g.getName());
		}
		
		public XGroup(String abbv, String title) {
			iAbbreaviation = abbv;
			iTitle = title;
		}
		
		public XGroup(ObjectInput in) throws IOException, ClassNotFoundException {
	    	super();
	    	readExternal(in);
	    }
		
		public String getType() { return iType; }
		public String getAbbreviation() { return iAbbreaviation; }
		public String getTitle() { return iTitle; }
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			if (in.readBoolean())
				iType = (String)in.readObject();
			else
				iType = null;
			iAbbreaviation = (String)in.readObject();
			iTitle = (String)in.readObject();
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			if (iType != null) {
				out.writeBoolean(true);
				out.writeObject(iType);			
			} else {
				out.writeBoolean(false);
			}
			out.writeObject(iAbbreaviation);
			out.writeObject(iTitle);
		}
		
		@Override
		public String toString() { return getAbbreviation(); }
		
		@Override
		public int hashCode() { return toString().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof XGroup)) return false;
			return getAbbreviation().equals(((XGroup)o).getAbbreviation());
		}
	}
	
	public static class XAdvisor implements Externalizable {
		public String iExternalId, iName, iEmail;
		
		public XAdvisor(String externalId, String name, String email) {
			iExternalId = externalId;
			iName = name;
			iEmail = email;
		}
		
		public XAdvisor(ObjectInput in) throws IOException, ClassNotFoundException {
	    	super();
	    	readExternal(in);
	    }
		
		public String getExternalId() { return iExternalId; }
		public String getEmail() { return iEmail; }
		public String getName() { return iName; }
		
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			iExternalId = (String)in.readObject();
			iName = (String)in.readObject();
			iEmail = (String)in.readObject();
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(iExternalId);
			out.writeObject(iName);
			out.writeObject(iEmail);
		}
		
		@Override
		public String toString() { return iExternalId; }
		
		@Override
		public int hashCode() { return toString().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof XAdvisor)) return false;
			return getExternalId().equals(((XAdvisor)o).getExternalId());
		}
	}
	
	public void markFailedWaitList(XCourseId courseId) {
		if (courseId != null)
			iFailedWaitLists.put(courseId.getCourseId(), System.currentTimeMillis());
	}
	
	public boolean isFailedWaitlist(XCourseId courseId) {
		return isFailedWaitlist(courseId, ApplicationProperty.FailedWaitListDelayInSeconds.intValue());
	}
	
	public boolean isFailedWaitlist(XCourseId courseId, Integer delayInSeconds) {
		if (courseId == null || delayInSeconds == null || delayInSeconds <= 0) return false;
		Long last = iFailedWaitLists.get(courseId.getCourseId());
		return last != null && System.currentTimeMillis() - last < 1000l * delayInSeconds;
	}
}