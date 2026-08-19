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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction.Credit;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.NameFormat;

public class DbStudentMatcher implements TermMatcher {
	private Student iStudent;
	private String iDefaultStatus;
	private NameFormat iFormat = null;
	private boolean iMyStudent = false;
	
	public DbStudentMatcher(Student student, String defaultStatus, NameFormat format, boolean myStudent) {
		iStudent = student;
		iDefaultStatus = defaultStatus;
		iFormat = format;
		iMyStudent = myStudent;
	}
	
	public DbStudentMatcher(Student student) {
		iStudent = student;
		iDefaultStatus = (student.getSession().getDefaultSectioningStatus() == null ? null : student.getSession().getDefaultSectioningStatus().getReference());
		iFormat = NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
	}

	public Student student() { return iStudent; }
	public String status() {  return (iStudent.getSectioningStatus() == null ? iDefaultStatus : iStudent.getSectioningStatus().getReference()); }
	
	@Override
	public boolean match(String attr, String term) {
		if (attr == null && term.isEmpty()) return true;
		if ("limit".equals(attr)) return true;
		if ("area".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
			for (StudentAreaClassificationMinor acm: student().getAreaClasfMinors())
				if (like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
		} else if ("clasf".equals(attr) || "classification".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getAcademicClassification().getCode(), term)) return true;
		} else if ("major".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (like(acm.getMajor().getCode(), term)) return true;
		} else if ("concentration".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getConcentration() != null && like(acm.getConcentration().getCode(), term)) return true;
		} else if ("degree".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getDegree() != null && like(acm.getDegree().getReference(), term)) return true;
		} else if ("program".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getProgram() != null && like(acm.getProgram().getReference(), term)) return true;
		} else if ("campus".equals(attr)) {
			for (StudentAreaClassificationMajor acm: student().getAreaClasfMajors())
				if (acm.getCampus() != null && like(acm.getCampus().getReference(), term)) return true;
		} else if ("primary-area".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getAcademicArea().getAcademicAreaAbbreviation(), term)) return true;
		} else if ("primary-clasf".equals(attr) || "primary-classification".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getAcademicClassification().getCode(), term)) return true;
		} else if ("primary-major".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && like(acm.getMajor().getCode(), term)) return true;
		} else if ("primary-concentration".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getConcentration() != null && like(acm.getConcentration().getCode(), term)) return true;
		} else if ("primary-degree".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getDegree() != null && like(acm.getDegree().getReference(), term)) return true;
		} else if ("primary-program".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getProgram() != null && like(acm.getProgram().getReference(), term)) return true;
		} else if ("primary-campus".equals(attr)) {
			StudentAreaClassificationMajor acm = student().getPrimaryAreaClasfMajor();
			if (acm != null && acm.getCampus() != null && like(acm.getCampus().getReference(), term)) return true;
		} else if ("minor".equals(attr)) {
			for (StudentAreaClassificationMinor acm: student().getAreaClasfMinors())
				if (like(acm.getMinor().getCode(), term)) return true;
		} else if ("group".equals(attr)) {
			for (StudentGroup group: student().getGroups())
				if (like(group.getGroupAbbreviation(), term)) return true;
		} else if ("accommodation".equals(attr)) {
			for (StudentAccomodation acc: student().getAccomodations())
				if (like(acc.getAbbreviation(), term)) return true;
		} else if  ("student".equals(attr)) {
			return has(iFormat.format(student()), term) || eq(student().getExternalUniqueId(), term) || eq(iFormat.format(student()), term);
		} else if  ("advisor".equals(attr)) {
			for (Advisor a: student().getAdvisors())
				if (eq(a.getExternalUniqueId(), term)) return true;
			return false;
		} else if ("registered".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return false;
			else
				return true;
		} else if ("status".equals(attr)) {
			if ("default".equalsIgnoreCase(term) || "Not Set".equalsIgnoreCase(term))
				return iStudent.getSectioningStatus() == null;
			return like(status(), term);
		}  else if ("credit".equals(attr)) {
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
		} else if ("overlap".equals(attr)) {
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
		} else if ("override".equals(attr)) {
			if ("null".equalsIgnoreCase(term) || "None".equalsIgnoreCase(term)) {
				for (CourseDemand cd: student().getCourseDemands()) {
					for (CourseRequest cr: cd.getCourseRequests()) {
						if (cr.getOverrideStatus() == null) return true;
					}
				}
				return false;
			}
			CourseRequestOverrideStatus status = null;
			for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values()) {
				if (s.name().equalsIgnoreCase(term)) { status = s; break; }
			}
			if (status == null) return false;
			if (student().getOverrideStatus() != null && student().getOverrideStatus() == status.ordinal()) return true;
			for (CourseDemand cd: student().getCourseDemands()) {
				for (CourseRequest cr: cd.getCourseRequests()) {
					if (cr.getOverrideStatus() != null && cr.getOverrideStatus() == status.ordinal()) return true;
				}
			}
			return false;
		} else if ("mode".equals(attr)) {
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
		} else if ("btb".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_PREFERRED;
			else if ("disc".equalsIgnoreCase(term) || "discourage".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_DISCOURAGED;
			else
				return student().getBackToBackPreference() == BackToBackPreference.NO_PREFERENCE;
		} else if ("online".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_PREFERRED;
			else if ("require".equalsIgnoreCase(term) || "required".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_REQUIRED;
			else if ("disc".equalsIgnoreCase(term) || "discourage".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONILNE_DISCOURAGED;
			else if ("no".equalsIgnoreCase(term) || "no-preference".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.NO_PREFERENCE;
		} else if (attr != null) {
			for (StudentGroup group: student().getGroups())
				if (group.getType() != null && eq(group.getType().getReference(), attr.replace('_', ' ')) && like(group.getGroupAbbreviation(), term)) return true;
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
}