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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.GradeMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.VariableTitleCourseInfo;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.VariableTitleCourseProvider;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class PurdueVariableTitleCourseProvider implements VariableTitleCourseProvider {
	private static Log sLog = LogFactory.getLog(PurdueVariableTitleCourseProvider.class);
	private ExternalTermProvider iExternalTermProvider;
	
	public PurdueVariableTitleCourseProvider() {
		try {
			String clazz = ApplicationProperty.CustomizationExternalTerm.value();
			if (clazz == null || clazz.isEmpty())
				iExternalTermProvider = new BannerTermProvider();
			else
				iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
		} catch (Exception e) {
			sLog.error("Failed to create external term provider, using the default one instead.", e);
			iExternalTermProvider = new BannerTermProvider();
		}
	}
	
	protected String getVariableTitleCourseSQL(AcademicSessionInfo session) {
		return ApplicationProperties.getProperty("purdue.vt.variableTitleCourseSQL." + session.getCampus(),
				ApplicationProperties.getProperty("purdue.vt.variableTitleCourseSQL",
						"select c.subj_code, c.crse_numb, c.crse_title, c.credit_hr_ind, c.credit_hr_low, c.credit_hr_high, c.gmod_code, c.gmod_desc, c.gmod_default_ind " +
						"from timetable.szgv_reg_vartl_course c, timetable.subject_area sa where " +
						"(concat(concat(c.subj_code, ' '), c.crse_numb) like :query or concat(concat(c.subj_code, ' '), concat(c.crse_numb, concat(' - ', c.crse_title))) like :query) and "+
						"c.attr_code = 'VART' and " +
						"c.course_effective_term <= :term and :term < c.course_end_term and " +
						"c.attr_effective_term <= :term and :term < c.attr_end_term and " +
						"c.gmod_effective_term <= :term and :term < c.gmod_end_term and " +
						"c.subj_code = sa.subject_area_abbreviation and sa.session_id = :sessionId and " +
						":studentId is not null " +
						"order by c.subj_code, c.crse_numb, c.gmod_code")
				);
	}
	
	protected String getInstructorNameFormat() {
		return ApplicationProperties.getProperty("purdue.vt.instructorNameFormat", "last-first-middle");
	}
	
	protected String getDisclaimer() {
		return ApplicationProperties.getProperty("purdue.vt.disclaimer", null);
	}

	@Override
	public Collection<VariableTitleCourseInfo> getVariableTitleCourses(String query, int limit, Long studentId, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		org.hibernate.query.Query q = helper.getHibSession().createNativeQuery(getVariableTitleCourseSQL(server.getAcademicSession()));
		q.setParameter("query", query == null ? "%" : query.toUpperCase() + "%");
		q.setParameter("term", iExternalTermProvider.getExternalTerm(server.getAcademicSession()));
		q.setParameter("sessionId", server.getAcademicSession().getUniqueId());
		q.setParameter("studentId", studentId);
		if (limit > 0)
			q.setMaxResults(5 * limit);
		
		Map<String, VariableTitleCourseInfo> courses = new HashMap<String, VariableTitleCourseInfo>();
		
		for (Object[] line: (List<Object[]>)q.list()) {
			String subject = (String)line[0];
			String courseNbr = (String)line[1];
			String title = (String)line[2];
			String credInd = (String)line[3];
			Number credLo = (Number)line[4];
			Number credHi = (Number)line[5];
			String gmCode = (String)line[6];
			String gmDesc = (String)line[7];
			String gmInd = (String)line[8];
			VariableTitleCourseInfo info = courses.get(subject + " " + courseNbr);
			if (info == null) {
				if (limit > 0 && courses.size() >= limit) break;
				info = new VariableTitleCourseInfo();
				info.setSubject(subject);
				info.setCourseNbr(courseNbr);
				info.setTitle(title);
				info.setStartDate(server.getAcademicSession().getDefaultStartDate());
				info.setEndDate(server.getAcademicSession().getDefaultEndDate());
				if ("TO".equals(credInd)) {
					float min = credLo.floatValue();
					float max = credHi.floatValue();
					if ((min - Math.floor(min)) == 0.5f || (max - Math.floor(max)) == 0.5f) {
						for (float credit = min; credit <= max + 0.001f; credit += 0.5f) {
							info.addAvailableCredit(credit);
						}
					} else {
						for (float credit = min; credit <= max + 0.001f; credit += 1f) {
							info.addAvailableCredit(credit);
						}
					}
				} else if ("OR".equals(credInd)) {
					info.addAvailableCredit(credLo.floatValue());
					info.addAvailableCredit(credHi.floatValue());
				} else {
					info.addAvailableCredit(credLo.floatValue());
				}
				courses.put(subject + " " + courseNbr, info);
			}
			if (gmCode != null) {
				info.addGradeMode(new GradeMode(gmCode, gmDesc, false));
				if ("D".equals(gmInd))
					info.setDefaultGradeModeCode(gmCode);
			}
		}
		return new TreeSet<VariableTitleCourseInfo>(courses.values());
	}
	
	@Override
	public VariableTitleCourseInfo getVariableTitleCourse(String query, Long studentId, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		org.hibernate.query.Query q = helper.getHibSession().createNativeQuery(getVariableTitleCourseSQL(server.getAcademicSession()));
		q.setParameter("query", query.toUpperCase());
		q.setParameter("term", iExternalTermProvider.getExternalTerm(server.getAcademicSession()));
		q.setParameter("sessionId", server.getAcademicSession().getUniqueId());
		q.setParameter("studentId", studentId);
		
		NameFormat nameFormat = NameFormat.fromReference(getInstructorNameFormat());

		VariableTitleCourseInfo info = null;
		for (Object[] line: (List<Object[]>)q.list()) {
			String subject = (String)line[0];
			String courseNbr = (String)line[1];
			String title = (String)line[2];
			String credInd = (String)line[3];
			Number credLo = (Number)line[4];
			Number credHi = (Number)line[5];
			String gmCode = (String)line[6];
			String gmDesc = (String)line[7];
			String gmInd = (String)line[8];
			if (info == null) {
				info = new VariableTitleCourseInfo();
				info.setSubject(subject);
				info.setCourseNbr(courseNbr);
				info.setTitle(title);
				info.setStartDate(server.getAcademicSession().getDefaultStartDate());
				info.setEndDate(server.getAcademicSession().getDefaultEndDate());
				if ("TO".equals(credInd)) {
					float min = credLo.floatValue();
					float max = credHi.floatValue();
					if ((min - Math.floor(min)) == 0.5f || (max - Math.floor(max)) == 0.5f) {
						for (float credit = min; credit <= max + 0.001f; credit += 0.5f) {
							info.addAvailableCredit(credit);
						}
					} else {
						for (float credit = min; credit <= max + 0.001f; credit += 1f) {
							info.addAvailableCredit(credit);
						}
					}
				} else if ("OR".equals(credInd)) {
					info.addAvailableCredit(credLo.floatValue());
					info.addAvailableCredit(credHi.floatValue());
				} else {
					info.addAvailableCredit(credLo.floatValue());
				}
				for (DepartmentalInstructor di: helper.getHibSession().createQuery(
						"select i from DepartmentalInstructor i inner join i.department.subjectAreas sa where " +
						"i.department.session.uniqueId = :sessionId and sa.subjectAreaAbbreviation = :subject and i.externalUniqueId is not null", DepartmentalInstructor.class
						).setCacheable(true).setParameter("sessionId", server.getAcademicSession().getUniqueId()).setParameter("subject", subject).list()) {
					info.addInstructor(di.getUniqueId(), nameFormat.format(di));
				}
				
				if (Customization.CourseDetailsProvider.hasProvider()) {
					CourseDetailsProvider dp = Customization.CourseDetailsProvider.getProvider();
					try {
						info.setDetails(dp.getDetails(server.getAcademicSession(), subject, courseNbr));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				info.setDisclaimer(getDisclaimer());
				
				for (String suggestion: ApplicationProperties.getProperty("purdue.specreg.vt.requestorNoteSuggestions", "").split("[\r\n]+"))
					if (!suggestion.isEmpty()) info.addSuggestion(suggestion);
			}
			if (gmCode != null) {
				info.addGradeMode(new GradeMode(gmCode, gmDesc, false));
				if ("D".equals(gmInd))
					info.setDefaultGradeModeCode(gmCode);
			}
		}
		return info;
	}

	@Override
	public void dispose() {
	}
}
