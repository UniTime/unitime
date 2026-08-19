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
package org.unitime.timetable.onlinesectioning.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cpsolver.studentsct.model.Student.BackToBackPreference;
import org.cpsolver.studentsct.model.Student.ModalityPreference;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction.Credit;

public class StudentMatcher implements TermMatcher {
	private XStudent iStudent;
	private String iDefaultStatus;
	private OnlineSectioningServer iServer;
	private boolean iMyStudent;
	
	public StudentMatcher(XStudent student, String defaultStatus, OnlineSectioningServer server, boolean myStudent) {
		iStudent = student;
		iDefaultStatus = defaultStatus;
		iServer = server;
		iMyStudent = myStudent;
	}

	public XStudent student() { return iStudent; }
	public String status() {  return (iStudent == null || iStudent.getStatus() == null ? iDefaultStatus : iStudent.getStatus()); }
	public OnlineSectioningServer server() { return iServer; }
	
	@Override
	public boolean match(String attr, String term) {
		if (attr == null && term.isEmpty()) return true;
		if ("limit".equals(attr)) return true;
		if ("area".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getArea(), term)) return true;
			for (XAreaClassificationMajor acm: student().getMinors())
				if (like(acm.getArea(), term)) return true;
		} else if ("clasf".equals(attr) || "classification".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getClassification(), term)) return true;
		} else if ("major".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getMajor(), term)) return true;
		} else if ("concentration".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getConcentration(), term)) return true;
		} else if ("degree".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getDegree(), term)) return true;
		} else if ("program".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getProgram(), term)) return true;
		} else if ("campus".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMajors())
				if (like(acm.getCampus(), term)) return true;
		} else if ("primary-area".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getArea(), term)) return true;
		} else if ("primary-clasf".equals(attr) || "primary-classification".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getClassification(), term)) return true;
		} else if ("primary-major".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getMajor(), term)) return true;
		} else if ("primary-concentration".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getConcentration(), term)) return true;
		} else if ("primary-degree".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getDegree(), term)) return true;
		} else if ("primary-program".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getProgram(), term)) return true;
		} else if ("primary-campus".equals(attr)) {
			XAreaClassificationMajor acm = student().getPrimaryMajor();
			if (acm != null && like(acm.getCampus(), term)) return true;
		} else if ("minor".equals(attr)) {
			for (XAreaClassificationMajor acm: student().getMinors())
				if (like(acm.getMajor(), term)) return true;
		} else if ("group".equals(attr)) {
			for (XStudent.XGroup group: student().getGroups())
				if (like(group.getAbbreviation(), term)) return true;
		} else if ("accommodation".equals(attr)) {
			for (XStudent.XGroup acc: student().getAccomodations())
				if (like(acc.getAbbreviation(), term)) return true;
		} else if  ("student".equals(attr)) {
			return has(student().getName(), term) || eq(student().getExternalId(), term) || eq(student().getName(), term);
		} else if  ("advisor".equals(attr)) {
			for (XStudent.XAdvisor a: student().getAdvisors())
				if (eq(a.getExternalId(), term)) return true;
			return false;
		} else if ("registered".equals(attr)) {
			if (eq("true", term) || eq("1",term))
				return false;
			else
				return true;
		} else if ("status".equals(attr)) {
			if ("default".equalsIgnoreCase(term) || "Not Set".equalsIgnoreCase(term))
				return student().getStatus() == null;
			return like(status(), term);
		} else if ("credit".equals(attr)) {
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
			for (XRequest r: student().getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					if (cr.getEnrollment() == null) continue;
					XOffering o = (server() == null ? null : server().getOffering(cr.getEnrollment().getOfferingId()));
					XConfig g = (o == null ? null : o.getConfig(cr.getEnrollment().getConfigId()));
					if (g != null) {
						if ("!".equals(im) && g.getInstructionalMethod() != null && !g.getInstructionalMethod().getReference().equals(iServer.getAcademicSession().getDefaultInstructionalMethod())) continue;
						if (im != null && !"!".equals(im) && (g.getInstructionalMethod() == null || !im.equalsIgnoreCase(g.getInstructionalMethod().getReference()))) continue;
						for (XSubpart xs: g.getSubparts())
							credit += xs.getCreditValue(cr.getEnrollment().getCourseId());
					}
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
			for (XRequest r: student().getRequests()) {
				if (r instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)r;
					if (cr.getEnrollment() == null) continue;
					XOffering o = (server() == null ? null : server().getOffering(cr.getEnrollment().getOfferingId()));
					if (o != null)
						for (XSection section: o.getSections(cr.getEnrollment())) {
							if (section.getTime() == null) continue;
							for (XRequest q: student().getRequests()) {
								if (q instanceof XCourseRequest) {
									XEnrollment otherEnrollment = ((XCourseRequest)q).getEnrollment();
									if (otherEnrollment == null) continue;
									XOffering otherOffering = server().getOffering(otherEnrollment.getOfferingId());
									for (XSection otherSection: otherOffering.getSections(otherEnrollment)) {
										if (otherSection.equals(section) || otherSection.getTime() == null) continue;
										if (section.getTime().hasIntersection(otherSection.getTime()) && !section.isToIgnoreStudentConflictsWith(o.getDistributions(), otherSection.getSectionId()) && section.getSectionId() < otherSection.getSectionId()) {
											share += section.getTime().share(otherSection.getTime());
										}
									}
								}
							}
						}
				}
			}
			return min <= share && share <= max;
		} else if ("override".equals(attr)) {
			if ("null".equalsIgnoreCase(term) || "None".equalsIgnoreCase(term)) {
				for (XRequest request: student().getRequests()) {
					if (request instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)request;
						for (XCourseId course: cr.getCourseIds()) {
							XOverride o = cr.getOverride(course);
							if (o == null) return true;
						}
					}
				}
				return false;
			}
			CourseRequestOverrideStatus status = null;
			for (CourseRequestOverrideStatus s: CourseRequestOverrideStatus.values()) {
				if (s.name().equalsIgnoreCase(term)) { status = s; break; }
			}
			if (status == null) return false;
			if (student().getMaxCreditOverride() != null && student().getMaxCreditOverride().getStatus() != null && student().getMaxCreditOverride().getStatus() == status.ordinal()) return true;
			for (XRequest request: student().getRequests()) {
				if (request instanceof XCourseRequest) {
					XCourseRequest cr = (XCourseRequest)request;
					for (XCourseId course: cr.getCourseIds()) {
						XOverride o = cr.getOverride(course);
						if (o != null && o.getStatus() != null && o.getStatus() == status.ordinal()) return true;
					}
				}
			}
			return false;
			
		} else if ("mode".equals(attr)) {
			if (eq("My Students", term)) {
				return iMyStudent;
			}
			if (eq("My Advised", term)) {
				return iMyStudent && student().hasAdvisorRequests();
			}
			if (eq("My Not Advised", term)) {
				return iMyStudent && !student().hasAdvisorRequests();
			}
			if (eq("Advised", term)) {
				return student().hasAdvisorRequests();
			}
			if (eq("Not Advised", term)) {
				return !student().hasAdvisorRequests();
			}
			if (eq("PIN Released", term)) {
				return student().isPinReleased();
			}
			if (eq("PIN Suppressed", term)) {
				return !student().isPinReleased();
			}
			if (eq("My PIN Released", term)) {
				return iMyStudent && student().isPinReleased();
			}
			if (eq("My PIN Suppressed", term)) {
				return iMyStudent && !student().isPinReleased();
			}
			return true;
		} else if ("btb".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_PREFERRED;
			else if ("disc".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getBackToBackPreference() == BackToBackPreference.BTB_DISCOURAGED;
			else
				return student().getBackToBackPreference() == BackToBackPreference.NO_PREFERENCE;
		} else if ("online".equals(attr)) {
			if ("prefer".equalsIgnoreCase(term) || "preferred".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_PREFERRED;
			else if ("require".equalsIgnoreCase(term) || "required".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONLINE_REQUIRED;
			else if ("disc".equalsIgnoreCase(term) || "discouraged".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.ONILNE_DISCOURAGED;
			else if ("no".equalsIgnoreCase(term) || "no-preference".equalsIgnoreCase(term))
				return student().getModalityPreference() == ModalityPreference.NO_PREFERENCE;
		} else if (attr != null) {
			for (XStudent.XGroup group: student().getGroups())
				if (eq(group.getType(), attr.replace('_', ' ')) && like(group.getAbbreviation(), term)) return true;
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