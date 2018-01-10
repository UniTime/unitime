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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.model.dao.OnlineSectioningLogDAO;
import org.unitime.timetable.model.dao.StudentAccomodationDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.model.dao.StudentSectioningStatusDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction.Credit;
import org.unitime.timetable.server.lookup.PeopleLookupBackend;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class StatusPageSuggestionsAction implements OnlineSectioningAction<List<String[]>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private String iQuery;
	private int iLimit;
	private String iUserId, iUserName;
	
	public StatusPageSuggestionsAction withParams(String userId, String userName, String query, int limit) {
		iUserId = userId; iUserName = userName;
		iQuery = (query == null ? "" : query);
		iLimit = limit;
		return this;
	}

	@Override
	public List<String[]> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		try {
			helper.beginTransaction();

			AcademicSessionInfo session = server.getAcademicSession();
			Long sessionId = session.getUniqueId();

			List<String[]> ret = new ArrayList<String[]>();
			Matcher m = Pattern.compile("^(.*\\W?subject:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (SubjectArea subject: (List<SubjectArea>)SubjectAreaDAO.getInstance().getSession().createQuery(
						"select a from SubjectArea a where" +
						" (lower(a.subjectAreaAbbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.title) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.subjectAreaAbbreviation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (subject.getSubjectAreaAbbreviation().indexOf(' ') >= 0 ? "\"" + subject.getSubjectAreaAbbreviation() + "\"" : subject.getSubjectAreaAbbreviation()),
							subject.getSubjectAreaAbbreviation() + " - " + (subject.getTitle())
					});
				}
			}
			m = Pattern.compile("^(.*\\W?department:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (Department dept: (List<Department>)DepartmentDAO.getInstance().getSession().createQuery(
						"select a from Department a where" +
						" (lower(a.abbreviation) like :q || '%' or lower(a.deptCode) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(name) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.deptCode"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (dept.getDeptCode().indexOf(' ') >= 0 ? "\"" + dept.getDeptCode() + "\"" : dept.getDeptCode()),
							dept.getDeptCode() + " - " + dept.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?area:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (AcademicArea area: (List<AcademicArea>)AcademicAreaDAO.getInstance().getSession().createQuery(
						"select a from AcademicArea a where " +
						" (lower(a.academicAreaAbbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.title) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.academicAreaAbbreviation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (area.getAcademicAreaAbbreviation().indexOf(' ') >= 0 ? "\"" + area.getAcademicAreaAbbreviation() + "\"" : area.getAcademicAreaAbbreviation()),
							area.getAcademicAreaAbbreviation() + " - " + area.getTitle()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?classification:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (AcademicClassification clasf: (List<AcademicClassification>)AcademicClassificationDAO.getInstance().getSession().createQuery(
						"select a from AcademicClassification a where " +
						" (lower(a.code) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.name) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.code"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (clasf.getCode().indexOf(' ') >= 0 ? "\"" + clasf.getCode() + "\"" : clasf.getCode()),
							clasf.getCode() + " - " + clasf.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?clasf:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (AcademicClassification clasf: (List<AcademicClassification>)AcademicClassificationDAO.getInstance().getSession().createQuery(
						"select a from AcademicClassification a where " +
						" (lower(a.code) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.name) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.code"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (clasf.getCode().indexOf(' ') >= 0 ? "\"" + clasf.getCode() + "\"" : clasf.getCode()),
							clasf.getCode() + " - " + clasf.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?major:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				String area = null;
				Matcher x = Pattern.compile("area:[ ]?\"([^\\\"]*)\"|area:[ ]?(\\w*)").matcher(iQuery);
				if (x.find()) area = (x.group(1) == null ? x.group(2) : x.group(1));
				for (PosMajor major: (List<PosMajor>)AcademicClassificationDAO.getInstance().getSession().createQuery(
						"select distinct a from PosMajor a " + (area == null ? "" : "inner join a.academicAreas x ") + "where " +
						" (lower(a.code) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.name) like '%' || :q || '%'") + ")" +
						(area == null ? "" : " and lower(x.academicAreaAbbreviation) = '" + area.toLowerCase() + "'") +
						" and a.session.uniqueId = :sessionId order by a.code"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (major.getCode().indexOf(' ') >= 0 ? "\"" + major.getCode() + "\"" : major.getCode()),
							major.getCode() + " - " + major.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?course:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				String subject = null;
				Matcher x = Pattern.compile("subject:[ ]?\"([^\\\"]*)\"|subject:[ ]?(\\w*)").matcher(iQuery);
				if (x.find()) subject = (x.group(1) == null ? x.group(2) : x.group(1));
				for (CourseOffering course: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
						"select c from CourseOffering c where " +
						" (lower(c.courseNbr) like :q || '%' or lower(c.subjectArea.subjectAreaAbbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(c.title) like '%' || :q || '%'") + ")" +
						(subject == null ? "" : " and lower(c.subjectArea.subjectAreaAbbreviation) = '" + subject.toLowerCase() + "'") +
						" and c.subjectArea.session.uniqueId = :sessionId order by c.subjectArea.subjectAreaAbbreviation, c.courseNbr"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + "\"" + course.getCourseName() + "\"",
							course.getCourseNameWithTitle()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?number:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				String subject = null;
				Matcher x = Pattern.compile("subject:[ ]?\"([^\\\"]*)\"|subject:[ ]?(\\w*)").matcher(iQuery);
				if (x.find()) subject = (x.group(1) == null ? x.group(2) : x.group(1));
				for (CourseOffering course: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
						"select c from CourseOffering c where " +
						" (lower(c.courseNbr) like :q || '%' or lower(c.subjectArea.subjectAreaAbbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(c.title) like '%' || :q || '%'") + ")" +
						(subject == null ? "" : " and lower(c.subjectArea.subjectAreaAbbreviation) = '" + subject.toLowerCase() + "'") +
						" and c.subjectArea.session.uniqueId = :sessionId order by c.subjectArea.subjectAreaAbbreviation, c.courseNbr"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (course.getCourseNbr().indexOf(' ') >= 0 ? "\"" + course.getCourseNbr() + "\"" : course.getCourseNbr()) +
							(subject == null ? " subject: " + (course.getSubjectArea().getSubjectAreaAbbreviation().indexOf(' ') >= 0 ? "\"" + course.getSubjectArea().getSubjectAreaAbbreviation() + "\"" : course.getSubjectArea().getSubjectAreaAbbreviation()) : ""),
							course.getCourseNameWithTitle()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?group:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (StudentGroup group: (List<StudentGroup>)StudentGroupDAO.getInstance().getSession().createQuery(
						"select a from StudentGroup a where " +
						" (lower(a.groupAbbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.groupName) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.groupAbbreviation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (group.getGroupAbbreviation().indexOf(' ') >= 0 ? "\"" + group.getGroupAbbreviation() + "\"" : group.getGroupAbbreviation()),
							group.getGroupAbbreviation() + " - " + group.getGroupName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?accommodation:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (StudentAccomodation accommodation: (List<StudentAccomodation>)StudentAccomodationDAO.getInstance().getSession().createQuery(
						"select a from StudentAccomodation a where " +
						" (lower(a.abbreviation) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.name) like '%' || :q || '%'") + ")" +
						" and a.session.uniqueId = :sessionId order by a.abbreviation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (accommodation.getAbbreviation().indexOf(' ') >= 0 ? "\"" + accommodation.getAbbreviation() + "\"" : accommodation.getAbbreviation()),
							accommodation.getAbbreviation() + " - " + accommodation.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?student:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches() && m.group(2).length() > 0) {
				for (PersonInterface person: new PeopleLookupBackend().execute(
						new PersonInterface.LookupRequest(m.group(2), "mustHaveExternalId,source=students,session=" + sessionId + ",maxResults=" + iLimit), null)) {
					ret.add(new String[] {
							m.group(1) + (person.getId().indexOf(' ') >= 0 ? "\"" + person.getId() + "\"" : person.getId()),
							person.getName()
					});
				}
			}
			m = Pattern.compile("^(.*\\W?assigned:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Assigned enrollments"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Wait-listed course requests"
					});
			}
			m = Pattern.compile("^(.*\\W?scheduled:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Assigned enrollments"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Wait-listed course requests"
					});
			}
			m = Pattern.compile("^(.*\\W?waitlist:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Wait-listed course requests"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Assigned enrollments"
					});
			}
			m = Pattern.compile("^(.*\\W?waitlisted:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Wait-listed course requests"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Assigned enrollments"
					});
			}
			m = Pattern.compile("^(.*\\W?reservation:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Enrollments with a reservation"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Enrollments without a reservation"
					});
			}
			m = Pattern.compile("^(.*\\W?reserved:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("true".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "true",
							"true - Enrollments with a reservation"
					});
				if ("false".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "false",
							"false - Enrollments without a reservation"
					});
			}
			m = Pattern.compile("^(.*\\W?consent:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("none".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "none",
							"none - Courses with no consent"
					});
				if ("required".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "required",
							"required - Courses requiring a consent"
					});
				for (OfferingConsentType consent: OfferingConsentTypeDAO.getInstance().findAll())
					if (consent.getAbbv().toLowerCase().startsWith(m.group(2).toLowerCase()))
						ret.add(new String[] {
								m.group(1) + (consent.getAbbv().indexOf(' ') >= 0 ? "\"" + consent.getAbbv() + "\"" : consent.getAbbv()).toLowerCase(),
								consent.getAbbv().toLowerCase() + " - " + consent.getLabel() + " required"
						});
				if ("waiting".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "waiting",
							"waiting - Enrollments waiting for a consent"
					});
				if ("todo".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "todo",
							"todo - Enrollments waiting for my consent"
					});
				if ("approved".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "approved",
							"approved - Enrollments with an approved consent"
					});
				if (m.group(2).length() > 0) {
					for (TimetableManager manager: (List<TimetableManager>)TimetableManagerDAO.getInstance().getSession().createQuery(
							"select distinct m from TimetableManager m inner join m.managerRoles r inner join m.departments d where " +
							" (lower(m.externalUniqueId) like :q || '%' or lower(m.emailAddress) like :q || '%' or lower(m.lastName) || ' ' || lower(m.firstName) like :q || '%')" +
							" and r.role.reference in ('Administrator', 'Dept Sched Mgr') and d.session.uniqueId = :sessionId order by m.lastName, m.firstName, m.middleName"
							).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
						ret.add(new String[] {
								m.group(1) + (manager.getExternalUniqueId().indexOf(' ') >= 0 ? "\"" + manager.getExternalUniqueId() + "\"" : manager.getExternalUniqueId()),
								manager.getLastName().toLowerCase() + " - Enrollments approved by " + manager.getName()
						});
					}
				} else {
					ret.add(new String[] {
							m.group(1) + iUserId,
							(iUserName.contains(",") ? iUserName.substring(0, iUserName.indexOf(',')).toLowerCase() : iUserName.toLowerCase()) + " - " + "Enrollments approved by " + iUserName
					});
				}
			}
			m = Pattern.compile("^(.*\\W?user:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if (m.group(2).length() > 0) {
					for (TimetableManager manager: (List<TimetableManager>)TimetableManagerDAO.getInstance().getSession().createQuery(
							"select distinct m from TimetableManager m inner join m.managerRoles r inner join m.departments d where " +
							" (lower(m.externalUniqueId) like :q || '%' or lower(m.emailAddress) like :q || '%' or lower(m.lastName) || ' ' || lower(m.firstName) like :q || '%')" +
							" and r.role.reference in ('Administrator', 'Dept Sched Mgr') and d.session.uniqueId = :sessionId order by m.lastName, m.firstName, m.middleName"
							).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
						ret.add(new String[] {
								m.group(1) + (manager.getExternalUniqueId().indexOf(' ') >= 0 ? "\"" + manager.getExternalUniqueId() + "\"" : manager.getExternalUniqueId()),
								manager.getLastName().toLowerCase() + " - Enrollments approved by " + manager.getName()
						});
					}
				} else {
					ret.add(new String[] {
							m.group(1) + iUserId,
							(iUserName.contains(",") ? iUserName.substring(0, iUserName.indexOf(',')).toLowerCase() : iUserName.toLowerCase()) + " - " + "Enrollments approved by " + iUserName
					});
				}
			}
			m = Pattern.compile("^(.*\\W?operation:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (String op: (List<String>)OnlineSectioningLogDAO.getInstance().getSession().createQuery(
						"select distinct operation from OnlineSectioningLog where " +
						"lower(operation) like :q || '%' and session.uniqueId = :sessionId order by operation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + op,
							Constants.toInitialCase(op.replace('-', ' '))
					});
				}
			}
			m = Pattern.compile("^(.*\\W?op:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (String op: (List<String>)OnlineSectioningLogDAO.getInstance().getSession().createQuery(
						"select distinct operation from OnlineSectioningLog where " +
						"lower(operation) like :q || '%' and session.uniqueId = :sessionId order by operation"
						).setString("q", m.group(2).toLowerCase()).setLong("sessionId", sessionId).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + op,
							Constants.toInitialCase(op.replace('-', ' '))
					});
				}
			}
			m = Pattern.compile("^(.*\\W?result:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				for (OnlineSectioningLog.Action.ResultType t: OnlineSectioningLog.Action.ResultType.values())
					if (t.name().toLowerCase().startsWith(m.group(2).toLowerCase())) {
						ret.add(new String[] {
								m.group(1) + t.name().toLowerCase(),
								Constants.toInitialCase(t.name().toLowerCase())
						});
					}
			}
			if (ret.isEmpty() && !iQuery.isEmpty()) {
				for (XCourseId c: server.findCourses(iQuery, iLimit, null)) {
					ret.add(new String[] {
							c.getCourseName(),
							c.getCourseName() + (c.getTitle() == null ? "" : " - " + c.getTitle())
					});
				}
			}
			m = Pattern.compile("^(.*\\W?status:[ ]?)(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("default".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							m.group(1) + "Default",
							"Default - Academic session default (" + (session.getDefaultSectioningStatus() == null ? "No Restrictions" : session.getDefaultSectioningStatus()) + ")"
					});
				for (StudentSectioningStatus status: (List<StudentSectioningStatus>)StudentSectioningStatusDAO.getInstance().getSession().createQuery(
						"select a from StudentSectioningStatus a where " +
						" (lower(a.reference) like :q || '%'" + (m.group(2).length() <= 2 ? "" : " or lower(a.label) like '%' || :q || '%'") + ")" +
						" order by a.reference"
						).setString("q", m.group(2).toLowerCase()).setMaxResults(iLimit).list()) {
					ret.add(new String[] {
							m.group(1) + (status.getReference().indexOf(' ') >= 0 ? "\"" + status.getReference() + "\"" : status.getReference()),
							status.getReference() + " - " + status.getLabel()
					});
				}
			}
			
			m = Pattern.compile("^(.*[^: ][ ]+)?(\\w*)$", Pattern.CASE_INSENSITIVE).matcher(iQuery);
			if (m.matches()) {
				if ("area".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "area:",
							"area: Academic Area"
					});
				if ("classification".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "classification:",
							"classification: Academic Classification"
					});
				if ("consent".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "consent:",
							"consent: Courses with consent"
					});
				if ("course".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "course:",
							"course: Course Offering"
					});
				if ("department".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "department:",
							"department: Department"
					});
				if ("group".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "group:",
							"group: Student Group"
					});
				if ("accommodation".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "accommodation:",
							"accommodation: Student Accommodation"
					});
				if ("major".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "major:",
							"major: Major"
					});
				if ("reservation".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "reservation:",
							"reservation: Enrollments with a reservation"
					});
				if ("status".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "status:",
							"status: Student Scheduling Status"
					});
				if ("student".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "student:",
							"student: Student"
					});
				if ("subject".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "subject:",
							"subject: Subject Area"
					});
				if ("waitlist".startsWith(m.group(2).toLowerCase()))
					ret.add(new String[] {
							(m.group(1) == null ? "" : m.group(1)) + "waitlist:",
							"waitlist: Wait-Listed Course Requests"
					});
			}
			
			helper.commitTransaction();
			return ret;
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	public static class CourseInfoMatcher implements TermMatcher, Serializable {
		private static final long serialVersionUID = 1L;
		private XCourse iInfo;
		private boolean iConsentToDoCourse;
		
		public CourseInfoMatcher(XCourse course, boolean isConsentToDoCourse) {
			iInfo = course;
			iConsentToDoCourse = isConsentToDoCourse;
		}
		
		public XCourse info() { return iInfo; }
		
		public boolean isConsentToDoCourse() { return iConsentToDoCourse; }
		
		@Override
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if ("limit".equals(attr)) return true;
			if (attr == null || "name".equals(attr) || "course".equals(attr)) {
				return info().getSubjectArea().equalsIgnoreCase(term) || info().getCourseNumber().equalsIgnoreCase(term) || (info().getSubjectArea() + " " + info().getCourseNumber()).equalsIgnoreCase(term);
			}
			if ((attr == null && term.length() > 2) || "title".equals(attr)) {
				return info().getTitle().toLowerCase().contains(term.toLowerCase());
			}
			if (attr == null || "subject".equals(attr)) {
				return info().getSubjectArea().equalsIgnoreCase(term);
			}
			if (attr == null || "number".equals(attr)) {
				return info().getCourseNumber().equalsIgnoreCase(term);
			}
			if ("department".equals(attr)) {
				return info().getDepartment().equalsIgnoreCase(term);
				
			}
			if ("consent".equals(attr)) {
				if ("none".equalsIgnoreCase(term) || "No Consent".equalsIgnoreCase(term))
					return info().getConsentLabel() == null;
				else if ("todo".equalsIgnoreCase(term) || "To Do".equalsIgnoreCase(term))
					return isConsentToDoCourse();
				else
					return info().getConsentLabel() != null;
			}
			if ("mode".equals(attr)) {
				return true;
			}
			if ("registered".equals(attr)) {
				if ("true".equalsIgnoreCase(term) || "1".equalsIgnoreCase(term))
					return true;
				else
					return false;
			}
			return attr != null; // pass unknown attributes lower
		}
	}
	
	public static class CourseRequestMatcher extends CourseInfoMatcher {
		private static final long serialVersionUID = 1L;
		private XStudent iStudent;
		private XCourseRequest iRequest;
		private XOffering iOffering;
		private Date iFirstDate;
		private String iDefaultStatus;
		private OnlineSectioningServer iServer;
		private boolean iMyStudent;
		
		public CourseRequestMatcher(AcademicSessionInfo session, XCourse info, XStudent student, XOffering offering, XCourseRequest request, boolean isConsentToDoCourse, boolean isMyStudent, OnlineSectioningServer server) {
			super(info, isConsentToDoCourse);
			iFirstDate = session.getDatePatternFirstDate();
			iStudent = student;
			iRequest = request;
			iDefaultStatus = session.getDefaultSectioningStatus();
			iOffering = offering;
			iServer = server;
			iMyStudent = isMyStudent;
		}
		
		public XCourseRequest request() { return iRequest; }
		public XEnrollment enrollment() { return iRequest.getEnrollment(); }
		public XStudent student() { return iStudent; }
		public String status() { return student().getStatus() == null ? iDefaultStatus : student().getStatus(); }
		public OnlineSectioningServer server() { return iServer; }
		public XCourseId course() {
			if (enrollment() != null) return enrollment();
			for (XCourseId course: request().getCourseIds())
				if (course.getCourseId().equals(info().getCourseId())) return course;
			return request().getCourseIds().get(0);
		}
		public XOffering offering() {
			return iOffering;
		}

		@Override
		public boolean match(String attr, String term) {
			if (attr == null || "name".equals(attr) || "title".equals(attr) || "subject".equals(attr) || "number".equals(attr) || "course".equals(attr) || "department".equals(attr) || "registered".equals(attr))
				return super.match(attr, term);
			
			if ("limit".equals(attr)) return true;
			
			if ("area".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getArea(), term)) return true;
			}
			
			if ("clasf".equals(attr) || "classification".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getClassification(), term)) return true;
			}
			
			if ("major".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getMajor(), term)) return true;
			}
			
			if ("group".equals(attr)) {
				for (String group: student().getGroups())
					if (eq(group, term)) return true;
			}
			
			if ("accommodation".equals(attr)) {
				for (String acc: student().getAccomodations())
					if (eq(acc, term)) return true;
			}
			
			if ("student".equals(attr)) {
				return has(student().getName(), term) || eq(student().getExternalId(), term) || eq(student().getName(), term);
			}
			
			if ("assignment".equals(attr)) {
				if (eq("Assigned", term)) {
					return enrollment() != null;
				} else if (eq("Reserved", term)) {
					return enrollment() != null && enrollment().getReservation() != null;
				} else if (eq("Not Assigned", term)) {
					return enrollment() == null;
				} else if (eq("Wait-Listed", term)) {
					return enrollment() == null && request().isWaitlist();
				}
			}
			
			if ("assigned".equals(attr) || "scheduled".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() != null;
				else
					return enrollment() == null;
			}
			
			if ("waitlisted".equals(attr) || "waitlist".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() == null && request().isWaitlist();
				else
					return enrollment() != null;
			}
			
			if ("reservation".equals(attr) || "reserved".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return enrollment() != null && enrollment().getReservation() != null;
				else
					return enrollment() != null && enrollment().getReservation() == null;
			}
			
			if ("consent".equals(attr)) {
				if (eq("none", term) || eq("No Consent", term)) {
					return info().getConsentLabel() == null;
				} else if (eq("Required", term) || eq("Consent", term)) {
					return info().getConsentLabel() != null && enrollment() != null;
				} else if (eq("Approved", term)) {
					return info().getConsentLabel() != null && enrollment() != null && enrollment().getApproval() != null;
				} else if (eq("Waiting", term)) {
					return info().getConsentLabel() != null && enrollment() != null && enrollment().getApproval() == null;
				} else if (eq("todo", term) || eq("To Do", term)) {
					return isConsentToDoCourse() && enrollment() != null && enrollment().getApproval() == null;
				} else {
					return info().getConsentLabel() != null && (enrollment() != null && ((enrollment().getApproval() != null && (has(enrollment().getApproval().getExternalId(), term) || eq(enrollment().getApproval().getName(), term))) || eq(info().getConsentAbbv(), term)));
				}
			}
			
			if ("mode".equals(attr)) {
				if (eq("My Students", term)) {
					return iMyStudent;
				}
				return true;
			}
			
			if ("approver".equals(attr)) {
				return info().getConsentLabel() != null && ((enrollment() != null && enrollment().getApproval() != null && (has(enrollment().getApproval().getExternalId(), term) || eq(enrollment().getApproval().getName(), term))));
			}
			
			if ("status".equals(attr)) {
				if ("default".equalsIgnoreCase(term) || "Not Set".equalsIgnoreCase(term))
					return student().getStatus() == null;
				return term.equalsIgnoreCase(status());
			}
			
			if ("credit".equals(attr)) {
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
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				float credit = 0;
				for (XRequest r: student().getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.getEnrollment() == null) continue;
						XOffering o = server().getOffering(cr.getEnrollment().getOfferingId());
						XConfig g = (o == null ? null : o.getConfig(cr.getEnrollment().getConfigId()));
						if (g != null) {
							for (XSubpart xs: g.getSubparts())
								credit += FindStudentInfoAction.guessCredit(xs.getCreditAbbv(cr.getEnrollment().getCourseId()));
						}
					}
				}
				return min <= credit && credit <= max;
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
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				int share = 0;
				for (XRequest r: student().getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.getEnrollment() == null) continue;
						XOffering o = server().getOffering(cr.getEnrollment().getOfferingId());
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
			}
			
			if (enrollment() != null) {
				
				for (XSection section: offering().getSections(enrollment())) {
					if (attr == null || attr.equals("crn") || attr.equals("id") || attr.equals("externalId") || attr.equals("exid") || attr.equals("name")) {
						if (section.getName(info().getCourseId()) != null && section.getName(info().getCourseId()).toLowerCase().startsWith(term.toLowerCase()))
							return true;
					}
					if (attr == null || attr.equals("day")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null) {
							int day = parseDay(term);
							if (day > 0 && (section.getTime().getDays() & day) == day) return true;
						}
					}
					if (attr == null || attr.equals("time")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null) {
							int start = parseStart(term);
							if (start >= 0 && section.getTime().getSlot() == start) return true;
						}
					}
					if (attr != null && attr.equals("before")) {
						if (section.getTime() != null) {
							int end = parseStart(term);
							if (end >= 0 && section.getTime().getSlot() + section.getTime().getLength() - section.getTime().getBreakTime() / 5 <= end) return true;
						}
					}
					if (attr != null && attr.equals("after")) {
						if (section.getTime() != null) {
							int start = parseStart(term);
							if (start >= 0 && section.getTime().getSlot() >= start) return true;
						}
					}
					if (attr == null || attr.equals("date")) {
						if (section.getTime() == null && term.equalsIgnoreCase("none")) return true;
						if (section.getTime() != null && !section.getTime().getWeeks().isEmpty()) {
							Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
					    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
					    	cal.setTime(iFirstDate);
					    	for (int i = 0; i < section.getTime().getWeeks().size(); i++) {
					    		if (section.getTime().getWeeks().get(i)) {
					    			DayCode day = null;
					    			switch (cal.get(Calendar.DAY_OF_WEEK)) {
					    			case Calendar.MONDAY:
					    				day = DayCode.MON; break;
					    			case Calendar.TUESDAY:
					    				day = DayCode.TUE; break;
					    			case Calendar.WEDNESDAY:
					    				day = DayCode.WED; break;
					    			case Calendar.THURSDAY:
					    				day = DayCode.THU; break;
					    			case Calendar.FRIDAY:
					    				day = DayCode.FRI; break;
					    			case Calendar.SATURDAY:
					    				day = DayCode.SAT; break;
					    			case Calendar.SUNDAY:
					    				day = DayCode.SUN; break;
					    			}
					    			if ((section.getTime().getDays() & day.getCode()) == day.getCode()) {
						    			int d = cal.get(Calendar.DAY_OF_MONTH);
						    			int m = cal.get(Calendar.MONTH) + 1;
						    			if (df.format(cal.getTime()).equalsIgnoreCase(term) || eq(d + "." + m + ".",term) || eq(m + "/" + d, term)) return true;
					    			}
					    		}
					    		cal.add(Calendar.DAY_OF_YEAR, 1);
					    	}
						}
					}
					if (attr == null || attr.equals("room")) {
						if ((section.getRooms() == null || section.getRooms().isEmpty()) && term.equalsIgnoreCase("none")) return true;
						if (section.getRooms() != null) {
							for (XRoom r: section.getRooms()) {
								if (has(r.getName(), term)) return true;
							}
						}
					}
					if (attr == null || attr.equals("instr") || attr.equals("instructor")) {
						if (attr != null && section.getInstructors().isEmpty() && term.equalsIgnoreCase("none")) return true;
						for (XInstructor instuctor: section.getInstructors()) {
							if (has(instuctor.getName(), term) || eq(instuctor.getExternalId(), term)) return true;
							if (instuctor.getEmail() != null) {
								String email = instuctor.getEmail();
								if (email.indexOf('@') >= 0) email = email.substring(0, email.indexOf('@'));
								if (eq(email, term)) return true;
							}
						}
					}
					if (attr != null && section.getTime() != null) {
						int start = parseStart(attr + ":" + term);
						if (start >= 0 && section.getTime().getSlot() == start) return true;
					}
				}
			}
			
			return false;
		}

		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
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
	
	public static class StudentMatcher implements TermMatcher {
		private XStudent iStudent;
		private String iDefaultStatus;
		private OnlineSectioningServer iServer;
		
		public StudentMatcher(XStudent student, String defaultStatus, OnlineSectioningServer server) {
			iStudent = student;
			iDefaultStatus = defaultStatus;
			iServer = server;
		}

		public XStudent student() { return iStudent; }
		public String status() {  return (iStudent.getStatus() == null ? iDefaultStatus : iStudent.getStatus()); }
		public OnlineSectioningServer server() { return iServer; }
		
		@Override
		public boolean match(String attr, String term) {
			if (attr == null && term.isEmpty()) return true;
			if ("limit".equals(attr)) return true;
			if ("area".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getArea(), term)) return true;
			} else if ("clasf".equals(attr) || "classification".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getClassification(), term)) return true;
			} else if ("major".equals(attr)) {
				for (XAreaClassificationMajor acm: student().getMajors())
					if (eq(acm.getMajor(), term)) return true;
			} else if ("group".equals(attr)) {
				for (String group: student().getGroups())
					if (eq(group, term)) return true;
			} else if ("accommodation".equals(attr)) {
				for (String acc: student().getAccomodations())
					if (eq(acc, term)) return true;
			} else if  ("student".equals(attr)) {
				return has(student().getName(), term) || eq(student().getExternalId(), term) || eq(student().getName(), term);
			} else if ("registered".equals(attr)) {
				if (eq("true", term) || eq("1",term))
					return false;
				else
					return true;
			} else if ("status".equals(attr)) {
				if ("default".equalsIgnoreCase(term) || "Not Set".equalsIgnoreCase(term))
					return student().getStatus() == null;
				return term.equalsIgnoreCase(status());
			} else if ("credit".equals(attr)) {
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
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				float credit = 0;
				for (XRequest r: student().getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.getEnrollment() == null) continue;
						XOffering o = server().getOffering(cr.getEnrollment().getOfferingId());
						XConfig g = (o == null ? null : o.getConfig(cr.getEnrollment().getConfigId()));
						if (g != null) {
							for (XSubpart xs: g.getSubparts())
								credit += FindStudentInfoAction.guessCredit(xs.getCreditAbbv(cr.getEnrollment().getCourseId()));
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
						String a = term.substring(0, term.indexOf('.'));
						String b = term.substring(term.indexOf("..") + 2);
						min = Integer.parseInt(a); max = Integer.parseInt(b);
					} catch (NumberFormatException e) {}
				}
				int share = 0;
				for (XRequest r: student().getRequests()) {
					if (r instanceof XCourseRequest) {
						XCourseRequest cr = (XCourseRequest)r;
						if (cr.getEnrollment() == null) continue;
						XOffering o = server().getOffering(cr.getEnrollment().getOfferingId());
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
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			if (eq(name, term)) return true;
			for (String t: name.split(" |,"))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
	}

	@Override
	public String name() {
		return "status-suggestions";
	}

}
