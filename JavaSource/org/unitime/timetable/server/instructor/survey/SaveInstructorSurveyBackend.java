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
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyRequest;
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
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveySaveRequest.class)
public class SaveInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveySaveRequest, InstructorSurveyData> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public InstructorSurveyData execute(InstructorSurveySaveRequest request, SessionContext context) {
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
		
		org.hibernate.Session hibSession = InstructorSurveyDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			InstructorSurvey is = (InstructorSurvey)hibSession.createQuery(
					"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
					).setParameter("sessionId", sessionId, org.hibernate.type.LongType.INSTANCE)
					.setParameter("externalId", survey.getExternalId(), org.hibernate.type.StringType.INSTANCE).setMaxResults(1).uniqueResult();
			if (is == null && !request.isChanged()) {
				throw new GwtRpcException(MESSAGES.errorNoInstructorSurvey());
			}
			
			if (is == null) {
				is = new InstructorSurvey();
				is.setExternalUniqueId(survey.getExternalId());
				is.setSession(SessionDAO.getInstance().get(sessionId, hibSession));
				is.setPreferences(new HashSet<Preference>());
				is.setCourseRequirements(new HashSet<InstructorCourseRequirement>());
			} else if (request.isChanged()) {
				is.getPreferences().clear();
				for (InstructorCourseRequirement icr: is.getCourseRequirements()) {
					hibSession.delete(icr);
				}
				is.getCourseRequirements().clear();
			}
			is.setEmail(survey.getEmail());
			is.setNote(survey.getNote());
			Date ts = new Date();
			if (request.isSubmit()) {
				is.setSubmitted(ts);
				survey.setSubmitted(is.getSubmitted());
				survey.setEditable(context.hasPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)}));
			} else if (request.isUnsubmit()) {
				is.setSubmitted(null);
			}
			if (request.isChanged()) {
				is.setChanged(ts);
				is.setChangedBy(context.getUser().getTrueExternalUserId());
				
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
								} else if (p.getId() == -4l) {
									Location r = LocationDAO.getInstance().get(selection.getItem(), hibSession);
									if (r != null && pl != null) {
										RoomPref rp = new RoomPref();
										rp.setRoom(r);
										rp.setPrefLevel(pl);
										rp.setNote(selection.getNote());
										rp.setOwner(is);is.getPreferences().add(rp);
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
				if (survey.getTimePrefs() != null && !survey.getTimePrefs().isEmpty()) {
					TimePref tp = new TimePref();
					tp.setNote(survey.getTimePrefs().getNote());
					tp.setPreference(survey.getTimePrefs().getPattern());
					tp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
					tp.setOwner(is); is.getPreferences().add(tp);
				}
			}
			
			hibSession.saveOrUpdate(is);
			survey.setId(is.getUniqueId());
			
			tx.commit(); tx = null;
			if (!request.isChanged())
				if (request.getInstructorId() != null)
					return new RequestInstructorSurveyBackend().execute(new InstructorSurveyRequest(request.getInstructorId()), context);
				else
					return new RequestInstructorSurveyBackend().execute(new InstructorSurveyRequest(request.getData().getExternalId(), sessionId.toString()), context);
			return survey;
		} catch (Exception ex) {
			if (tx != null) tx.rollback();
			if (ex instanceof GwtRpcException) throw (GwtRpcException)ex;
			throw new GwtRpcException(ex.getMessage(), ex);
		}
	}
}
