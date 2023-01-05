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

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveySaveRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveySaveRequest.class)
public class SaveInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveySaveRequest, InstructorSurveyData> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public InstructorSurveyData execute(InstructorSurveySaveRequest request, SessionContext context) {
		InstructorSurveyData survey = request.getData();
		if (context.getUser() == null || survey.getExternalId().equals(context.getUser().getExternalUserId())) {
			context.checkPermission(Right.InstructorSurvey);
		} else {
			context.checkPermission(Right.InstructorSurveyAdmin);
		}
		
		org.hibernate.Session hibSession = InstructorSurveyDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			InstructorSurvey is = (InstructorSurvey)hibSession.createQuery(
					"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
					).setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
					.setString("externalId", survey.getExternalId()).setMaxResults(1).uniqueResult();
			if (is == null) {
				is = new InstructorSurvey();
				is.setExternalUniqueId(survey.getExternalId());
				is.setSession(SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession));
				is.setPreferences(new HashSet<Preference>());
				is.setCourseRequirements(new HashSet<InstructorCourseRequirement>());
			} else {
				is.getPreferences().clear();
				for (InstructorCourseRequirement icr: is.getCourseRequirements()) {
					hibSession.delete(icr);
				}
				is.getCourseRequirements().clear();
			}
			is.setEmail(survey.getEmail());
			is.setNote(survey.getNote());
			if (request.isSubmit()) {
				is.setSubmitted(new Date());
				survey.setSubmitted(is.getSubmitted());
			}
			
			if (survey.hasCourses()) {
				List<InstructorCourseRequirementType> types = (List<InstructorCourseRequirementType>)hibSession.createQuery(
						"from InstructorCourseRequirementType order by sortOrder").list();
				for (Course ci: survey.getCourses()) {
					if (!ci.hasCustomFields()) continue;
					CourseOffering co = null;
					if (ci.getId() != null)
						co = CourseOfferingDAO.getInstance().get(ci.getId(), hibSession);
					if (co != null || ci.hasCourseName()) {
						InstructorCourseRequirement icr = new InstructorCourseRequirement();
						icr.setCourseOffering(co);
						icr.setCourse(co == null ? ci.getCourseName() : co.getCourseName());
						icr.setInstructorSurvey(is);
						icr.setNotes(new HashSet<InstructorCourseRequirementNote>());
						for (InstructorCourseRequirementType type: types) {
							String note = ci.getCustomField(type.getUniqueId());
							if (note != null) {
								InstructorCourseRequirementNote n = new InstructorCourseRequirementNote();
								n.setType(type);
								n.setRequirement(icr);
								n.setNote(note);
								icr.getNotes().add(n);
							}
						}
						is.getCourseRequirements().add(icr);
					}
				}
			}
			
			if (survey.hasDistributionPreferences()) {
				Preferences p = survey.getDistributionPreferences();
				if (p.hasSelections()) {
					for (Selection selection: p.getSelections()) {
						DistributionType dt = DistributionTypeDAO.getInstance().get(selection.getItem(), hibSession);
						PreferenceLevel pl = PreferenceLevelDAO.getInstance().get(selection.getLevel(), hibSession);
						if (dt != null && pl != null) {
							DistributionPref dp = new DistributionPref();
							dp.setDistributionType(dt);
							dp.setPrefLevel(pl);
							dp.setNote(selection.getNote());
							dp.setOwner(is);
							is.getPreferences().add(dp);
						}
					}
				}
			}
			if (survey.hasRoomPreferences()) {
				for (Preferences p: survey.getRoomPreferences()) {
					if (p.hasSelections()) {
						for (Selection selection: p.getSelections()) {
							PreferenceLevel pl = PreferenceLevelDAO.getInstance().get(selection.getLevel(), hibSession);
							if (p.getId() == -1l) {
								Building b = BuildingDAO.getInstance().get(selection.getItem(), hibSession);
								if (b != null && pl != null) {
									BuildingPref bp = new BuildingPref();
									bp.setBuilding(b);
									bp.setPrefLevel(pl);
									bp.setNote(selection.getNote());
									bp.setOwner(is);is.getPreferences().add(bp);
								}
							} else if (p.getId() == -2l) {
								RoomGroup rg = RoomGroupDAO.getInstance().get(selection.getItem(), hibSession);
								if (rg != null && pl != null) {
									RoomGroupPref gp = new RoomGroupPref();
									gp.setRoomGroup(rg);
									gp.setPrefLevel(pl);
									gp.setNote(selection.getNote());
									gp.setOwner(is);is.getPreferences().add(gp);
								}
							} else {
								RoomFeature rf = RoomFeatureDAO.getInstance().get(selection.getItem(), hibSession);
								if (rf != null && pl != null) {
									RoomFeaturePref fp = new RoomFeaturePref();
									fp.setRoomFeature(rf);
									fp.setPrefLevel(pl);
									fp.setNote(selection.getNote());
									fp.setOwner(is);is.getPreferences().add(fp);
								}
							}
						}
					}
				}
			}
			if (survey.getTimePrefs() != null) {
				TimePref tp = new TimePref();
				tp.setNote(survey.getTimePrefs().getNote());
				tp.setPreference(survey.getTimePrefs().getPattern());
				tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				tp.setOwner(is); is.getPreferences().add(tp);
			}
			
			hibSession.saveOrUpdate(is);
			survey.setId(is.getUniqueId());
			
			tx.commit(); tx = null;
			return survey;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
}
