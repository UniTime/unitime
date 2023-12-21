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
package org.unitime.timetable.server.instructor.survey;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyCopyRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Problem;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveyCopyRequest.class)
public class CopyInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveyCopyRequest, InstructorSurveyData> {

	@Override
	public InstructorSurveyData execute(InstructorSurveyCopyRequest request, SessionContext context) {
		if (!context.isAuthenticated() || context.getUser() == null || context.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();

		Long sessionId = request.getData().getSessionId();
		if (sessionId == null)
			sessionId = context.getUser().getCurrentAcademicSessionId();

		InstructorSurveyData survey = request.getData();
		if (context.getUser() == null || survey.getExternalId().equals(context.getUser().getExternalUserId())) {
			context.checkPermissionAnyAuthority(Right.InstructorSurvey, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		} else {
			context.checkPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		}
		
		if (request.getPreferencesSessionId() != null) {
			InstructorSurvey is = InstructorSurvey.getInstructorSurvey(survey.getExternalId(), request.getPreferencesSessionId());
			if (is != null) {
				survey.setNote(is.getNote());
				survey.getDistributionPreferences().clearSelections();
				survey.getTimePrefs().setPattern("");
				if (survey.hasRoomPreferences())
					for (Preferences p: survey.getRoomPreferences())
						p.clearSelections();
				for (Preference p: is.getPreferences()) {
					if (p instanceof TimePref) {
						TimePref tp = (TimePref)p;
						survey.getTimePrefs().setPattern(tp.getPreference());
						survey.getTimePrefs().setNote(tp.getNote());
					} else if (p instanceof BuildingPref) {
						BuildingPref bp = (BuildingPref)p;
						Problem prob = Problem.NOT_APPLIED;
						Preferences buildingPrefs = survey.getRoomPreference(-1l);
						if (buildingPrefs != null)
							buildingPrefs.addSelection(new Selection(bp.getBuilding().getUniqueId(), bp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
					} else if (p instanceof RoomGroupPref) {
						RoomGroupPref gp = (RoomGroupPref)p;
						Preferences groupPref = survey.getRoomPreference(-2l);
						Problem prob = Problem.NOT_APPLIED;
						if (groupPref != null)
							groupPref.addSelection(new Selection(gp.getRoomGroup().getUniqueId(), gp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
					} else if (p instanceof RoomFeaturePref) {
						RoomFeaturePref fp = (RoomFeaturePref)p;
						Problem prob = Problem.NOT_APPLIED;
						if (fp.getRoomFeature().getFeatureType() != null) {
							Preferences prefs = survey.getRoomPreference(fp.getRoomFeature().getFeatureType().getUniqueId());
							if (prefs != null)
								prefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
						} else {
							Preferences featurePrefs = survey.getRoomPreference(-3l);
							if (featurePrefs != null)
								featurePrefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
						}
					} else if (p instanceof DistributionPref) {
						DistributionPref dp = (DistributionPref)p;
						if (dp.getDistributionType().effectiveSurvey())
							survey.getDistributionPreferences().addSelection(new Selection(dp.getDistributionType().getUniqueId(), dp.getPrefLevel().getUniqueId(), p.getNote()));
					} else if (p instanceof RoomPref) {
						RoomPref rp = (RoomPref)p;
						Problem prob = Problem.NOT_APPLIED;
						Preferences roomPrefs = survey.getRoomPreference(-4l);
						if (roomPrefs != null)
							roomPrefs.addSelection(new Selection(rp.getRoom().getUniqueId(), rp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
					}
				}
			}
		}
		if (request.getCoursesSessionId() != null) {
			InstructorSurvey is = InstructorSurvey.getInstructorSurvey(survey.getExternalId(), request.getCoursesSessionId());
			if (is != null) {
				survey.clearCourses();
				Set<Long> courseIds = new HashSet<Long>();
				for (InstructorCourseRequirement r: is.getCourseRequirements()) {
					Course ci = new Course();
					ci.setReqId(r.getUniqueId());
					CourseOffering course = null;
					if (r.getCourseOffering() != null) {
						course = CourseOffering.findBySubjectAreaCourseNbr(sessionId, r.getCourseOffering().getSubjectAreaAbbv(), r.getCourseOffering().getCourseNbr());
						if (course == null) continue;
					}
					ci.setId(course == null ? null : course.getUniqueId());
					ci.setCourseName(course == null ? r.getCourse() : course.getCourseName());
					ci.setCourseTitle(course == null ? null : course.getTitle());
					for (InstructorCourseRequirementNote n: r.getNotes()) {
						CustomField cf = survey.getCustomField(n.getType().getUniqueId());
						if (cf != null)
							ci.setCustomField(cf, n.getNote());
					}
					if (ci.hasCustomFields()) {
						if (course != null)
							courseIds.add(course.getUniqueId());
						survey.addCourse(ci);
					}
				}
				
				boolean excludeAuxiliary = ApplicationProperty.InstructorSurveyExcludeAuxiliaryAssignments.isTrue();
				String excludeCourseType = ApplicationProperty.InstructorSurveyExcludeCourseTypes.value();
				for (CourseOffering co: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
						"select distinct co from " +
						"DepartmentalInstructor i inner join i.classes ci inner join ci.classInstructing c " +
						"inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co " +
						(excludeAuxiliary ? "left outer join ci.responsibility r " : "") +
						"where co.isControl = true and io.notOffered = false and io.session.uniqueId = :sessionId and i.externalUniqueId=:id " +
						"and ci.lead = true and c.schedulingSubpart.itype.organized = true" +
						(excludeAuxiliary ? " and (r is null or bit_and(r.options, " + TeachingResponsibility.Option.auxiliary.toggle() + ") = 0)" : "")
						)
						.setString("id", survey.getExternalId())
						.setLong("sessionId", sessionId)
						.setCacheable(true).list()) {
					if (excludeCourseType != null && !excludeCourseType.isEmpty() && co.getCourseType() != null && 
							co.getCourseType().getReference().matches(excludeCourseType)) continue;
					if (courseIds.add(co.getUniqueId())) {
						Course ci = new Course();
						ci.setId(co.getUniqueId());
						ci.setCourseName(co.getCourseName());
						ci.setCourseTitle(co.getTitle());
						survey.addCourse(ci);
					}
				}
				if (survey.hasCourses())
					Collections.sort(survey.getCourses(), new Comparator<Course>() {
						@Override
						public int compare(Course co1, Course co2) {
							int cmp = co1.getCourseName().compareTo(co2.getCourseName());
							if (cmp != 0) return cmp;
							for (CustomField f: survey.getCustomFields()) {
								String cf1 = co1.getCustomField(f);
								String cf2 = co2.getCustomField(f);
								cmp = (cf1 == null ? "" : cf1).compareTo(cf2 == null ? "" : cf2);
								if (cmp != 0) return cmp;
							}
							return 0;
						}
					});
			}
		}
		return survey;
	}

}
