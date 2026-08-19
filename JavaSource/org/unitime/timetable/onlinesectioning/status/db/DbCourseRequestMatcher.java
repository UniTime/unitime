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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;
import org.unitime.timetable.model.CourseDemand.Critical;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.status.CourseLookup;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction.Credit;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.NameFormat;

public class DbCourseRequestMatcher extends DbCourseInfoMatcher {
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private static final long serialVersionUID = 1L;
	private AcademicSessionInfo iSession;
	private Student iStudent;
	private CourseRequest iRequest;
	private InstructionalOffering iOffering;
	private String iDefaultStatus;
	private List<StudentClassEnrollment> iEnrollment = null;
	private NameFormat iFormat = null;
	private Reservation iReservation = null;
	private boolean iReservationGuessed = false;
	private boolean iMyStudent = false;
	private Class_ iClazz = null;
	
	public DbCourseRequestMatcher(AcademicSessionInfo session, CourseRequest request, boolean isConsentToDoCourse, boolean myStudent, NameFormat format, CourseLookup lookup) {
		this(session, request, isConsentToDoCourse, myStudent, format, lookup, null);
	}
	
	public DbCourseRequestMatcher(AcademicSessionInfo session, CourseRequest request, boolean isConsentToDoCourse, boolean myStudent, NameFormat format, CourseLookup lookup, Class_ clazz) {
		super(request.getCourseOffering(), isConsentToDoCourse, lookup);
		iSession = session;
		iStudent = request.getCourseDemand().getStudent();
		iRequest = request;
		iDefaultStatus = session.getDefaultSectioningStatus();
		iOffering = request.getCourseOffering().getInstructionalOffering();
		iFormat = format;
		iMyStudent = myStudent;
		iClazz = clazz;
	}
	
	public AcademicSessionInfo session() { return iSession; }
	public CourseRequest request() { return iRequest; }
	public List<StudentClassEnrollment> enrollment() {
		if (iEnrollment == null) {
			iEnrollment = new ArrayList<StudentClassEnrollment>();
	    	for (StudentClassEnrollment e: student().getClassEnrollments()) {
	    		for (CourseRequest cr: request().getCourseDemand().getCourseRequests()) {
	    			if (cr.getCourseOffering().equals(e.getCourseOffering()))
	    				iEnrollment.add(e);
	    		}
	    	}
		}
		return iEnrollment;
	}
	public Student student() { return iStudent; }
	public String status() { return student().getSectioningStatus() == null ? iDefaultStatus : student().getSectioningStatus().getReference(); }
	public InstructionalOffering offering() { return iOffering; }
	public String approval() {
		if (enrollment().isEmpty()) return null;
		Set<String> approval = new HashSet<String>();
		String ret = "";
		for (StudentClassEnrollment e: enrollment()) {
			if (e.getApprovedBy() == null || e.getApprovedDate() == null) return null;
			if (approval.add(e.getApprovedBy()))
				ret += (ret.isEmpty() ? "" : "|") + e.getApprovedBy();
		}
		return ret;
	}
	public Reservation reservation() {
		if (!iReservationGuessed) {
			iReservation = guessReservation();
			iReservationGuessed = true;
		}
		return iReservation;
	}
	public InstrOfferingConfig config() {
		for (StudentClassEnrollment e: enrollment()) {
			return e.getClazz().getSchedulingSubpart().getInstrOfferingConfig();
		}	
		if (iClazz != null) return iClazz.getSchedulingSubpart().getInstrOfferingConfig();
		return null;
	}
	
	protected Reservation guessReservation() {
		List<StudentClassEnrollment> enrollment = enrollment();
    	if (enrollment.isEmpty()) return null;

    	Reservation best = null;
    	boolean mustBeUsed = false;
		for (Reservation reservation: iOffering.getReservations()) {
			if (reservation.isApplicable(iStudent, iRequest) && reservation.isMatching(enrollment)) {
				if (!mustBeUsed && reservation.isMustBeUsed()) { best = null; mustBeUsed = true; }
    			if (mustBeUsed && !reservation.isMustBeUsed()) continue;
    			if (best == null || reservation.compareTo(best) < 0.0)
    				best = reservation;
			}
		}
		
		return best;
	}
	
	public boolean canAssign() {
		if (!enrollment().isEmpty()) return true;
		if (!request().getCourseDemand().isAlternative() && request().getCourseDemand().effectiveWaitList()) return true;
		int alt = 0;
		for (CourseDemand demand: student().getCourseDemands()) {
			boolean course = (!demand.getCourseRequests().isEmpty());
			boolean assigned = !course;
			if (course)
				for (CourseRequest request: demand.getCourseRequests())
					if (!request.getClassEnrollments().isEmpty()) { assigned = true; break; }
			if (demand.equals(request().getCourseDemand())) {
				if (assigned) return false;
				else assigned = true;
			}
			boolean waitlist = (course && (demand.effectiveWaitList() || demand.effectiveNoSub()));
			if (demand.isAlternative()) {
				if (assigned)
					alt --;
			} else {
				if (course && !waitlist && !assigned)
					alt ++;
			}
		}
		return alt >= 0;
	}
	
	@Override
	public Boolean match(String attr, String term) {
		if (attr == null || "name".equals(attr) || "title".equals(attr) || "subject".equals(attr) || "number".equals(attr) || "course".equals(attr) || "lookup".equals(attr) || "department".equals(attr) || "registered".equals(attr))
			return super.match(attr, term);
		
		if ("limit".equals(attr)) return true;
		
		if ("area".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
			for (StudentAreaClassificationMinor acm: student().getAreaClasfMinors())
				if (like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
		}
		
		if ("clasf".equals(attr) || "classification".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getAcademicClassification().getCode(), term)) return true;
		}
		
		if ("major".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getMajor().getCode(), term)) return true;
		}
		if ("concentration".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getConcentration() != null && like(acm.getConcentration().getCode(), term)) return true;
		}
		if ("degree".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getDegree() != null && like(acm.getDegree().getReference(), term)) return true;
		}
		if ("program".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getProgram() != null && like(acm.getProgram().getReference(), term)) return true;
		}
		if ("campus".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getCampus() != null && like(acm.getCampus().getReference(), term)) return true;
		}
		
		if ("primary-area".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
		}
		
		if ("primary-clasf".equals(attr) || "primary-classification".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getAcademicClassification().getCode(), term)) return true;
		}
		
		if ("primary-major".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getMajor().getCode(), term)) return true;
		}
		if ("primary-concentration".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getConcentration() != null && like(acm.getConcentration().getCode(), term)) return true;
		}
		if ("primary-degree".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getDegree() != null && like(acm.getDegree().getReference(), term)) return true;
		}
		if ("primary-program".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getProgram() != null && like(acm.getProgram().getReference(), term)) return true;
		}
		if ("primary-campus".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getCampus() != null && like(acm.getCampus().getReference(), term)) return true;
		}
		
		if ("minor".equals(attr)) {
			for (StudentAreaClassificationMinor acm: student().getAreaClasfMinors())
				if (like(acm.getMinor().getCode(), term)) return true;
		}
		
		if ("group".equals(attr)) {
			for (StudentGroup group: student().getGroups())
				if (like(group.getGroupAbbreviation(), term)) return true;
		} else {
			for (StudentGroup group: student().getGroups())
				if (group.getType() != null && attr != null && eq(group.getType().getReference(), attr.replace('_', ' ')) && like(group.getGroupAbbreviation(), term)) return true;
		}
		
		if ("accommodation".equals(attr)) {
			for (StudentAccomodation acc: student().getAccomodations())
				if (like(acc.getAbbreviation(), term)) return true;
		}
		
		if ("student".equals(attr)) {
			if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue() && term.startsWith("0")) {
				return has(iFormat.format(student()), term) || eq(student().getExternalUniqueId(), term.replaceFirst("^0+(?!$)", "")) || eq(iFormat.format(student()), term);
			} else {
				return has(iFormat.format(student()), term) || eq(student().getExternalUniqueId(), term) || eq(iFormat.format(student()), term);
			}
		}

		if ("advisor".equals(attr)) {
			if (ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue() && term.startsWith("0")) {
				for (Advisor a: student().getAdvisors())
					if (eq(a.getExternalUniqueId(), term.replaceFirst("^0+(?!$)", ""))) return true;
			} else {
				for (Advisor a: student().getAdvisors())
					if (eq(a.getExternalUniqueId(), term)) return true;
			}
			return false;
		}

		if ("assignment".equals(attr)) {
			if (eq("Assigned", term)) {
				return !enrollment().isEmpty();
			} else if (eq("Reserved", term)) {
				return !enrollment().isEmpty() && reservation() != null;
			} else if (eq("Not Assigned", term)) {
				if (enrollment().isEmpty() && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("Wait-Listed", term)) {
				if (!enrollment().isEmpty() && request().getCourseDemand().effectiveWaitList() && enrollment().get(0).getCourseOffering().equals(request().getCourseDemand().getWaitListSwapWithCourseOffering()))
					return true;
				return enrollment().isEmpty() && request().getCourseDemand().effectiveWaitList();
			} else if (eq("Critical", term)) {
				return request().getCourseDemand().getEffectiveCritical() == Critical.CRITICAL;
			} else if (eq("Assigned Critical", term)) {
				if (enrollment().isEmpty()) return false;
				return request().getCourseDemand().getEffectiveCritical() == Critical.CRITICAL;
			} else if (eq("Not Assigned Critical", term)) {
				if (enrollment().isEmpty() && request().getCourseDemand().getEffectiveCritical() == Critical.CRITICAL && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("Important", term)) {
				return request().getCourseDemand().getEffectiveCritical() == Critical.IMPORTANT;
			} else if (eq("Assigned Important", term)) {
				if (enrollment().isEmpty()) return false;
				return request().getCourseDemand().getEffectiveCritical() == Critical.IMPORTANT;
			} else if (eq("Not Assigned Important", term)) {
				if (enrollment().isEmpty() && request().getCourseDemand().getEffectiveCritical() == Critical.IMPORTANT && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("Vital", term)) {
				return request().getCourseDemand().getEffectiveCritical() == Critical.VITAL;
			} else if (eq("Assigned Vital", term)) {
				if (enrollment().isEmpty()) return false;
				return request().getCourseDemand().getEffectiveCritical() == Critical.VITAL;
			} else if (eq("Not Assigned Vital", term)) {
				if (enrollment().isEmpty() && request().getCourseDemand().getEffectiveCritical() == Critical.VITAL && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("LC", term)) {
				return request().getCourseDemand().getEffectiveCritical() == Critical.LC;
			} else if (eq("Assigned LC", term)) {
				if (enrollment().isEmpty()) return false;
				return request().getCourseDemand().getEffectiveCritical() == Critical.LC;
			} else if (eq("Not Assigned LC", term)) {
				if (enrollment().isEmpty() && request().getCourseDemand().getEffectiveCritical() == Critical.LC && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("Visiting F2F", term)) {
				return request().getCourseDemand().getEffectiveCritical() == Critical.VISITING_F2F;
			} else if (eq("Assigned Visiting F2F", term)) {
				if (enrollment().isEmpty()) return false;
				return request().getCourseDemand().getEffectiveCritical() == Critical.VISITING_F2F;
			} else if (eq("Not Assigned Visiting F2F", term)) {
				if (enrollment().isEmpty() && request().getCourseDemand().getEffectiveCritical() == Critical.VISITING_F2F && !request().getCourseDemand().isAlternative()) {
					for (StudentClassEnrollment e: student().getClassEnrollments())
						if (e.getCourseRequest() != null && e.getCourseRequest().getCourseDemand().equals(request().getCourseDemand())) return false;
					return true;
				}
				return false;
			} else if (eq("No-Subs", term) || eq("No-Substitutes", term)) {
				return request().getCourseDemand().effectiveNoSub();
			} else if (eq("Assigned No-Subs", term) || eq("Assigned  No-Substitutes", term)) {
				return !enrollment().isEmpty() && request().getCourseDemand().effectiveNoSub();
			} else if (eq("Not Assigned No-Subs", term) || eq("Not Assigned No-Substitutes", term)) {
				return enrollment().isEmpty() && request().getCourseDemand().effectiveNoSub();
			}
		}
		
		if ("assigned".equals(attr) || "scheduled".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return !enrollment().isEmpty();
			else
				return enrollment().isEmpty();
		}
		
		if ("waitlisted".equals(attr) || "waitlist".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return enrollment().isEmpty() && request().getCourseDemand().effectiveWaitList();
			else
				return !enrollment().isEmpty();
		}
		
		if ("no-sub".equals(attr) || "no-substitution".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return enrollment().isEmpty() && request().getCourseDemand().effectiveNoSub();
			else
				return !enrollment().isEmpty();
		}
		
		if ("reservation".equals(attr) || "reserved".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return !enrollment().isEmpty() && reservation() != null;
			else
				return !enrollment().isEmpty() && reservation() == null;
		}
		
		if ("consent".equals(attr)) {
			if (eq("none", term) || eq("No Consent", term)) {
				return course().getConsentType() == null;
			} else if (eq("Required", term) || eq("Consent", term)) {
				return course().getConsentType() != null && !enrollment().isEmpty();
			} else if (eq("Approved", term)) {
				return course().getConsentType() != null && !enrollment().isEmpty() && approval() != null;
			} else if (eq("Waiting", term)) {
				return course().getConsentType() != null && !enrollment().isEmpty() && approval() == null;
			} else if (eq("todo", term) || eq("To Do", term)) {
				return isConsentToDoCourse() && course().getConsentType() != null && !enrollment().isEmpty() && approval() == null;
			} else {
				return course().getConsentType() != null && (!enrollment().isEmpty() && ((approval() != null && has(approval(), term)) || eq(course().getConsentType().getAbbv(), term)));
			}
		}
		
		if ("mode".equals(attr)) {
			if (eq("My Students", term)) {
				return iMyStudent;
			}
			if (eq("My Advised", term)) {
				return iMyStudent && !student().getAdvisorCourseRequests().isEmpty();
			}
			if (eq("My Not Advised", term)) {
				return iMyStudent && student().getAdvisorCourseRequests().isEmpty();
			}
			if (eq("Advised", term)) {
				return !student().getAdvisorCourseRequests().isEmpty();
			}
			if (eq("Not Advised", term)) {
				return student().getAdvisorCourseRequests().isEmpty();
			}
			if (eq("PIN Released", term)) {
				return Boolean.TRUE.equals(student().getPinReleased());
			}
			if (eq("PIN Suppressed", term)) {
				return !Boolean.TRUE.equals(student().getPinReleased());
			}
			if (eq("My PIN Released", term)) {
				return iMyStudent && Boolean.TRUE.equals(student().getPinReleased());
			}
			if (eq("My PIN Suppressed", term)) {
				return iMyStudent &&  !Boolean.TRUE.equals(student().getPinReleased());
			}
			return true;
		}
		
		if ("approver".equals(attr)) {
			return course().getConsentType() != null && !enrollment().isEmpty() && approval() != null && has(approval(), term);
		}
		
		if ("status".equals(attr)) {
			if ("default".equalsIgnoreCase(term) || "Not Set".equalsIgnoreCase(term))
				return student().getSectioningStatus() == null;
			return like(status(), term);
		}
		
		if ("credit".equals(attr)) {
			float min = 0, max = Float.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			String im = null;
			try {
				float a = Float.parseFloat(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 0.0001f; break; // < a
					case gt: min = a + 0.0001f; break; // > a
				}
			} catch (NumberFormatException e) {
				Matcher m = Pattern.compile("([0-9]+\\.?[0-9]*)([^0-9\\.].*)").matcher(number);
				if (m.matches()) {
					float a = Float.parseFloat(m.group(1));
					im = m.group(2).trim();
					switch (prefix) {
						case eq: min = max = a; break; // = a
						case le: max = a; break; // <= a
						case ge: min = a; break; // >= a
						case lt: max = a - 0.0001f; break; // < a
						case gt: min = a + 0.0001f; break; // > a
					}
				}
			}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Float.parseFloat(a); max = Float.parseFloat(b);
				} catch (NumberFormatException e) {
					Matcher m = Pattern.compile("([0-9]+\\.?[0-9]*)\\.\\.([0-9]+\\.?[0-9]*)([^0-9].*)").matcher(term);
					if (m.matches()) {
						min = Float.parseFloat(m.group(1));
						max = Float.parseFloat(m.group(2));
						im = m.group(3).trim();
					}
				}
			}
			float credit = 0;
			Set<Long> courseIds = new HashSet<Long>();
			for (StudentClassEnrollment e: student().getClassEnrollments()) {
				if (courseIds.add(e.getCourseOffering().getUniqueId())) {
					CourseCreditUnitConfig config = e.getCourseOffering().getCredit();
					if ("!".equals(im) && e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() != null) continue;
					if (im != null && !"!".equals(im) && (e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getEffectiveInstructionalMethod() == null || !im.equalsIgnoreCase(e.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getEffectiveInstructionalMethod().getReference())))
						continue;
					if (config != null)
						credit += config.getMinCredit();
				}
			}
			return min <= credit && credit <= max;
		}
		
		if ("rc".equals(attr) || "requested-credit".equals(attr)) {
			float min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				float a = Float.parseFloat(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 0.0001f; break; // < a
					case gt: min = a + 0.0001f; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Float.parseFloat(a); max = Float.parseFloat(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			float studentMinTot = 0f, studentMaxTot = 0f;
			int nrCoursesTot = 0;
			List<Float> minsTot = new ArrayList<Float>();
			List<Float> maxsTot = new ArrayList<Float>();
			Set<Long> advisorWaitListedCourseIds = student().getAdvisorWaitListedCourseIds(null);
			for (CourseDemand demand: student().getCourseDemands()) {
				if (!demand.getCourseRequests().isEmpty()) {
					Float minTot = null, maxTot = null;
					for (CourseRequest r: demand.getCourseRequests()) {
						CourseCreditUnitConfig c = r.getCourseOffering().getCredit();
						if (c != null) {
							if (minTot == null || minTot > c.getMinCredit()) minTot = c.getMinCredit();
							if (maxTot == null || maxTot < c.getMaxCredit()) maxTot = c.getMaxCredit();
						}
					}
					boolean isWaitList = false;
					if (!demand.isAlternative()) {
						if (demand.isWaitListOrNoSub(student().getWaitListMode())) {
							isWaitList = true;
						} else if (advisorWaitListedCourseIds != null && !advisorWaitListedCourseIds.isEmpty()) {
							for (CourseRequest r: demand.getCourseRequests())
								if (advisorWaitListedCourseIds.contains(r.getCourseOffering().getUniqueId())) {
									isWaitList = true; break;
								}
						}
					}
					if (isWaitList) {
						if (minTot != null) {
							studentMinTot += minTot; studentMaxTot += maxTot;
						}
					} else {
						if (minTot != null) {
							minsTot.add(minTot); maxsTot.add(maxTot); 
							if (!demand.isAlternative()) nrCoursesTot ++;
						}
					}
				}
			}
			Collections.sort(minsTot);
			Collections.sort(maxsTot);
			for (int i = 0; i < nrCoursesTot; i++) {
				studentMinTot += minsTot.get(i);
				studentMaxTot += maxsTot.get(maxsTot.size() - i - 1);
			}
			return min <= studentMaxTot && studentMinTot <= max;
		}
		
		if ("fc".equals(attr) || "first-choice-credit".equals(attr)) {
			float min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				float a = Float.parseFloat(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 0.0001f; break; // < a
					case gt: min = a + 0.0001f; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Float.parseFloat(a); max = Float.parseFloat(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			float credit = 0f;
			for (CourseDemand demand: student().getCourseDemands()) {
				if (!demand.getCourseRequests().isEmpty() && !demand.isAlternative()) {
					for (CourseRequest r: new TreeSet<CourseRequest>(demand.getCourseRequests())) {
						CourseCreditUnitConfig c = r.getCourseOffering().getCredit();
						if (c != null) {
							credit += c.getMinCredit();
							break;
						}
					}
				}
			}
			return min <= credit && credit <= max;
		}
		
		if ("rp".equals(attr)) {
			if ("subst".equalsIgnoreCase(term)) return request().getCourseDemand().isAlternative();
			int min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			return !request().getCourseDemand().isAlternative() && min <= request().getCourseDemand().getPriority() + 1 && request().getCourseDemand().getPriority() + 1 <= max;
		}
		
		if ("ft".equals(attr) || "free-time".equals(attr)) {
			int min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			for (CourseDemand cd: student().getCourseDemands())
				if (cd.getFreeTime() != null && min <= cd.getPriority() + 1 && cd.getPriority() + 1 <= max) return true;
			return false;
		}
		
		if ("choice".equals(attr) || "ch".equals(attr)) {
			int min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			if (enrollment() != null) {
				int choice = request().getOrder() + 1;
				return min <= choice && choice <= max;
			} else if (!request().getCourseDemand().isAlternative()) {
				int choice = request().getCourseDemand().getCourseRequests().size();
				return min <= choice && choice <= max;
			} else {
				return false;
			}
		}
		
		if ("btb".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_PREFERRED;
			else if ("disc".equalsIgnoreCase(term) || "discourage".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_DISCOURAGED;
			else
				return student().getBackToBackPreference() == BackToBackPreference.NO_PREFERENCE;
		}
		if ("online".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_PREFERRED;
			else if ("require".equalsIgnoreCase(term) || "required".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_REQUIRED;
			else if ("disc".equalsIgnoreCase(term) || "discourage".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONILNE_DISCOURAGED;
			else if ("no".equalsIgnoreCase(term) || "no-preference".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.NO_PREFERENCE;
		}
		
		if ("online".equals(attr) || "face-to-face".equals(attr) || "f2f".equals(attr) || "no-time".equals(attr) || "has-time".equals(attr)) {
			int min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			boolean perc = false;
			if (number.endsWith("%")) { perc = true; number = number.substring(0, number.length() - 1).trim(); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			if (min == 0 && max == Integer.MAX_VALUE) return true;
			int match = 0, total = 0;
			for (StudentClassEnrollment section: student().getClassEnrollments()) {
				Assignment assignment = section.getClazz().getCommittedAssignment();
				boolean online = true;
		        if (assignment != null) {
		        	for (Location loc: assignment.getRooms())
		        		if (!loc.isIgnoreRoomCheck()) {
		        			online = false; break;
		        		}
		        }
				if ("online".equals(attr) && online) {
					match ++;
				} else if (("face-to-face".equals(attr) || "f2f".equals(attr)) && !online)
					match ++;
				else if ("no-time".equals(attr) && assignment == null)
					match ++;
				else if ("has-time".equals(attr) && assignment != null)
					match ++;
				total ++;
			}
			if (total == 0) return false;
			if (perc) {
				double percentage = 100.0 * match / total;
				return min <= percentage && percentage <= max;
			} else {
				return min <= match && match <= max;
			}
		}
		
		if ("overlap".equals(attr)) {
			int min = 0, max = Integer.MAX_VALUE;
			Credit prefix = Credit.eq;
			String number = term;
			if (number.startsWith("<=")) { prefix = Credit.le; number = number.substring(2); }
			else if (number.startsWith(">=")) { prefix =Credit.ge; number = number.substring(2); }
			else if (number.startsWith("<")) { prefix = Credit.lt; number = number.substring(1); }
			else if (number.startsWith(">")) { prefix = Credit.gt; number = number.substring(1); }
			else if (number.startsWith("=")) { prefix = Credit.eq; number = number.substring(1); }
			try {
				int a = Integer.parseInt(number);
				switch (prefix) {
					case eq: min = max = a; break; // = a
					case le: max = a; break; // <= a
					case ge: min = a; break; // >= a
					case lt: max = a - 1; break; // < a
					case gt: min = a + 1; break; // > a
				}
			} catch (NumberFormatException e) {}
			if (term.contains("..")) {
				try {
					String a = term.substring(0, term.indexOf(".."));
					String b = term.substring(term.indexOf("..") + 2);
					min = Integer.parseInt(a); max = Integer.parseInt(b);
				} catch (NumberFormatException e) {}
			}
			int share = 0;
			for (StudentClassEnrollment section: student().getClassEnrollments()) {
				Assignment assignment = section.getClazz().getCommittedAssignment();
				if (assignment == null) continue;
				for (StudentClassEnrollment otherSection: student().getClassEnrollments()) {
					if (section.equals(otherSection)) continue;
					Assignment otherAssignment = otherSection.getClazz().getCommittedAssignment();
					if (otherAssignment == null) continue;
					if (assignment.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation()) && !section.getClazz().isToIgnoreStudentConflictsWith(otherSection.getClazz()) && section.getClazz().getUniqueId() < otherSection.getClazz().getUniqueId()) {
						int sh = assignment.getTimeLocation().nrSharedDays(otherAssignment.getTimeLocation()) * assignment.getTimeLocation().nrSharedHours(otherAssignment.getTimeLocation()) * Constants.SLOT_LENGTH_MIN;
						share += sh;
					}
				}
			}
			return min <= share && share <= max;
		}
		
		if ("override".equals(attr)) {
			if ("null".equalsIgnoreCase(term) || "None".equalsIgnoreCase(term))
				return request().getOverrideStatus() == null;
			CourseRequestOverrideStatus status = null;
			for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values()) {
				if (s.name().equalsIgnoreCase(term)) { status = s; break; }
			}
			if (status == null) return false;
			// if (student().getOverrideStatus() != null && student().getOverrideStatus() == status.ordinal()) return true;
			if (request().getOverrideStatus() != null && request().getOverrideStatus() == status.ordinal()) return true;
			return false;
		}
		
		if ("prefer".equals(attr)) {
			if (eq("Any Preference", term)) return !request().getPreferences().isEmpty();
			if (eq("Met Preference", term) || eq("Unmet Preference", term)) {
				if (enrollment() == null || enrollment().isEmpty()) {
					if (eq("Unmet Preference", term)) return !request().getPreferences().isEmpty();
					return false;
				}
				boolean hasPref = false, hasIm = false, im = false;
				Set<String> allSubpart = new HashSet<String>();
				Set<String> selectedSubparts = new HashSet<String>();
				for (StudentSectioningPref p: request().getPreferences()) {
					hasPref = true;
					if (p instanceof StudentInstrMthPref) {
						hasIm = true;
						StudentInstrMthPref imp = (StudentInstrMthPref)p;
						InstructionalMethod method = enrollment().get(0).getClazz().getSchedulingSubpart().getInstrOfferingConfig().getEffectiveInstructionalMethod();
						if (method != null && method.equals(imp.getInstructionalMethod())) { im = true; }
					}
					if (p instanceof StudentClassPref) {
						StudentClassPref scp = (StudentClassPref)p;
						allSubpart.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
						for (StudentClassEnrollment section: enrollment()) {
							if (scp.getClazz().equals(section.getClazz()))
								selectedSubparts.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
						}
					}
				}
				if (eq("Met Preference", term))
					return hasPref && (hasIm == im) && (selectedSubparts.size() == allSubpart.size());
				else
					return hasPref && (hasIm != im || selectedSubparts.size() != allSubpart.size());
			}
			for (StudentSectioningPref p: request().getPreferences())
				if (eq(p.getLabel(), term)) return true;
			return false;
		}
		
		if ("require".equals(attr)) {
			if (eq("Any Requirement", term)) {
				for (StudentSectioningPref p: request().getPreferences()) {
					if (!p.isRequired()) continue;
					return true;
				}
				return false;
			}
			if (eq("Met Requirement", term)) {
				if (enrollment() == null || enrollment().isEmpty()) return false;
				boolean hasPref = false, hasIm = false, im = false;
				Set<String> allSubpart = new HashSet<String>();
				Set<String> selectedSubparts = new HashSet<String>();
				for (StudentSectioningPref p: request().getPreferences()) {
					if (!p.isRequired()) continue;
					hasPref = true;
					if (p instanceof StudentInstrMthPref) {
						hasIm = true;
						StudentInstrMthPref imp = (StudentInstrMthPref)p;
						InstructionalMethod method = enrollment().get(0).getClazz().getSchedulingSubpart().getInstrOfferingConfig().getEffectiveInstructionalMethod();
						if (method != null && method.equals(imp.getInstructionalMethod())) { im = true; }
					}
					if (p instanceof StudentClassPref) {
						StudentClassPref scp = (StudentClassPref)p;
						allSubpart.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
						for (StudentClassEnrollment section: enrollment()) {
							if (scp.getClazz().equals(section.getClazz()))
								selectedSubparts.add(scp.getClazz().getSchedulingSubpart().getItypeDesc());
						}
					}
				}
				return hasPref && (hasIm == im) && (selectedSubparts.size() == allSubpart.size());
			}
			if (eq("Unmet Requirement", term)) {
				if (enrollment() != null) return false;
				for (StudentSectioningPref p: request().getPreferences()) {
					if (p.isRequired()) return true;
				}
				return false;
			}
			for (StudentSectioningPref p: request().getPreferences()) {
				if (!p.isRequired()) continue;
				if (eq(p.getLabel(), term)) return true;
			}
			return false;
		}
		
		if ("im".equals(attr)) {
			InstrOfferingConfig thisConfig = config();
			if (thisConfig != null) {
				if (thisConfig.getInstructionalMethod() == null) {
					return term.equals(session().getDefaultInstructionalMethod());
				} else {
					return term.equals(thisConfig.getInstructionalMethod().getReference());
				}
			} else {
				for (InstrOfferingConfig config: offering().getInstrOfferingConfigs()) {
					if (config.getInstructionalMethod() == null && term.equals(session().getDefaultInstructionalMethod()))
						return true;
					if (config.getInstructionalMethod() != null && term.equals(config.getInstructionalMethod().getReference()))
						return true;
				}
				return false;
			}
		}
		
		if (!enrollment().isEmpty()) {
			for (StudentClassEnrollment e: enrollment()) {
				if (attr == null || attr.equals("crn") || attr.equals("id") || attr.equals("externalId") || attr.equals("exid") || attr.equals("name")) {
					if (e.getClazz().getClassSuffix(e.getCourseOffering()).toLowerCase().startsWith(term.toLowerCase()))
						return true;
				}
				if (attr == null || attr.equals("day")) {
					Assignment assignment = e.getClazz().getCommittedAssignment();
					if (assignment == null && term.equalsIgnoreCase("none")) return true;
					if (assignment != null) {
						int day = parseDay(term);
						if (day > 0 && (assignment.getDays() & day) == day) return true;
					}
				}
				if (attr == null || attr.equals("time")) {
					Assignment assignment = e.getClazz().getCommittedAssignment();
					if (assignment == null && term.equalsIgnoreCase("none")) return true;
					if (assignment != null) {
						int start = parseStart(term);
						if (start >= 0 && assignment.getStartSlot() == start) return true;
					}
				}
				if (attr != null && attr.equals("before")) {
					Assignment assignment = e.getClazz().getCommittedAssignment();
					if (assignment != null) {
						int end = parseStart(term);
						if (end >= 0 && assignment.getStartSlot() + assignment.getSlotPerMtg() - assignment.getBreakTime() / 5 <= end) return true;
					}
				}
				if (attr != null && attr.equals("after")) {
					Assignment assignment = e.getClazz().getCommittedAssignment();
					if (assignment != null) {
						int start = parseStart(term);
						if (start >= 0 && assignment.getStartSlot() >= start) return true;
					}
				}
				if (attr == null || attr.equals("date")) {
					ClassEvent event = e.getClazz().getEvent();
					if (event == null && term.equalsIgnoreCase("none")) return true;
					if (event != null) {
						Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
						for (Meeting m: event.getMeetings()) {
							if (eq(df.format(m.getMeetingDate()), term)) return true;
						}
					}
				}
				if (attr == null || attr.equals("room")) {
					Assignment assignment = e.getClazz().getCommittedAssignment();
					if ((assignment == null || assignment.getRooms().isEmpty()) && term.equalsIgnoreCase("none")) return true;
					if (assignment != null) {
						for (Location room: assignment.getRooms()) {
							if (has(room.getLabel(), term)) return true;
						}
					}
				}
				if (attr == null || attr.equals("instr") || attr.equals("instructor")) {
					if (attr != null && e.getClazz().getClassInstructors().isEmpty() && term.equalsIgnoreCase("none")) return true;
					for (ClassInstructor instuctor: e.getClazz().getClassInstructors()) {
						if (has(iFormat.format(instuctor.getInstructor()), term) || eq(instuctor.getInstructor().getExternalUniqueId(), term)) return true;
						if (instuctor.getInstructor().getEmail() != null) {
							String email = instuctor.getInstructor().getEmail();
							if (email.indexOf('@') >= 0) email = email.substring(0, email.indexOf('@'));
							if (eq(email, term)) return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	private boolean eq(String name, String term) {
		if (name == null) return false;
		return name.equalsIgnoreCase(term);
	}
	
	private boolean like(String name, String term) {
		if (name == null) return false;
		if (term.indexOf('%') >= 0) {
			return name.matches("(?i)" + term.replaceAll("%", ".*"));
		} else {
			return name.equalsIgnoreCase(term);
		}
	}

	private boolean has(String name, String term) {
		if (name == null) return false;
		if (eq(name, term)) return true;
		for (String t: name.split(" |,"))
			if (t.equalsIgnoreCase(term)) return true;
		return false;
	}
	
	private int parseDay(String token) {
		int days = 0;
		boolean found = false;
		do {
			found = false;
			for (int i=0; i<CONSTANTS.longDays().length; i++) {
				if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
					days |= DayCode.values()[i].getCode(); 
					token = token.substring(CONSTANTS.longDays()[i].length());
					while (token.startsWith(" ")) token = token.substring(1);
					found = true;
				}
			}
			for (int i=0; i<CONSTANTS.days().length; i++) {
				if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
					days |= DayCode.values()[i].getCode(); 
					token = token.substring(CONSTANTS.days()[i].length());
					while (token.startsWith(" ")) token = token.substring(1);
					found = true;
				}
			}
			for (int i=0; i<CONSTANTS.days().length; i++) {
				if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
					days |= DayCode.values()[i].getCode(); 
					token = token.substring(2);
					while (token.startsWith(" ")) token = token.substring(1);
					found = true;
				}
			}
			for (int i=0; i<CONSTANTS.shortDays().length; i++) {
				if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
					days |= DayCode.values()[i].getCode(); 
					token = token.substring(CONSTANTS.shortDays()[i].length());
					while (token.startsWith(" ")) token = token.substring(1);
					found = true;
				}
			}
			for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
				if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
					days |= DayCode.values()[i].getCode(); 
					token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
					while (token.startsWith(" ")) token = token.substring(1);
					found = true;
				}
			}
		} while (found);
		return (token.isEmpty() ? days : 0);
	}
	
	private int parseStart(String token) {
		int startHour = 0, startMin = 0;
		String number = "";
		while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
		if (number.isEmpty()) return -1;
		if (number.length() > 2) {
			startHour = Integer.parseInt(number) / 100;
			startMin = Integer.parseInt(number) % 100;
		} else {
			startHour = Integer.parseInt(number);
		}
		while (token.startsWith(" ")) token = token.substring(1);
		if (token.startsWith(":")) {
			token = token.substring(1);
			while (token.startsWith(" ")) token = token.substring(1);
			number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) return -1;
			startMin = Integer.parseInt(number);
		}
		while (token.startsWith(" ")) token = token.substring(1);
		boolean hasAmOrPm = false;
		if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; }
		if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; }
		if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
		if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
		if (startHour < 7 && !hasAmOrPm) startHour += 12;
		if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
		if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
		return (60 * startHour + startMin) / 5;
	}
}