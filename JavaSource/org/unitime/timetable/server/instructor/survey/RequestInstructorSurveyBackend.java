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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.type.LongType;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorDepartment;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorTimePreferencesModel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.PrefLevel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Problem;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
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
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorCourseRequirementTypeDAO;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.AccessDeniedException;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveyRequest.class)
public class RequestInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveyRequest, InstructorSurveyData> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static final CourseMessages CMSG = Localization.create(CourseMessages.class);

	@Override
	public InstructorSurveyData execute(InstructorSurveyRequest request, SessionContext context) {
		if (!context.isAuthenticated() || context.getUser() == null || context.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();

		DepartmentalInstructor instructor = null;
		Long sessionId = null;
		if (request.getInstructorId() != null) {
			instructor = DepartmentalInstructorDAO.getInstance().get(request.getInstructorId());
			if (instructor != null) {
				request.setExternalId(instructor.getExternalUniqueId());
				sessionId = instructor.getDepartment().getSessionId();
			}
		}

		if (sessionId == null) {
			sessionId = context.getUser().getCurrentAcademicSessionId();
			if (request.hasSession()) {
				try {
					sessionId = Long.valueOf(request.getSession());
				} catch (NumberFormatException e) {
					Number id = (Number)SessionDAO.getInstance().getSession().createQuery(
							"select uniqueId from Session where (academicTerm || academicYear) = :session or (academicTerm || academicYear || academicInitiative) = :session"
							).setString("session", request.getSession()).setMaxResults(1).setCacheable(true).uniqueResult();
					if (id == null) throw new GwtRpcException(MESSAGES.errorSessionNotFound(request.getSession()));
					sessionId = id.longValue();
				}
			}
		}
		
		boolean admin = context.hasPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
		String externalId = context.getUser().getExternalUserId();
		if (request.getExternalId() != null && !request.getExternalId().isEmpty() && !externalId.equals(request.getExternalId())) {
			context.hasPermissionAnySession(Right.InstructorSurveyAdmin, new Qualifiable[] { new SimpleQualifier("Session", sessionId)}); 
			externalId = request.getExternalId();
		}
		boolean editable = true;
		InstructorSurvey is = (InstructorSurvey)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
				).setLong("sessionId", sessionId)
				.setString("externalId", externalId).setMaxResults(1).uniqueResult();

		if (!admin) {
			editable = context.hasPermissionAnyAuthority(Right.InstructorSurvey, new Qualifiable[] { new SimpleQualifier("Session", sessionId)});
			if (is != null && is.getSubmitted() != null)
				editable = false;
			if (!editable && is == null)
				throw new GwtRpcException(MESSAGES.errorInstructorSurveyNotAllowed());
		}
		
		final InstructorSurveyData survey = new InstructorSurveyData();
		survey.setExternalId(externalId);
		survey.setEditable(editable);
		survey.setAdmin(admin);
		survey.setSessionId(sessionId);
		survey.setCanApply(is != null && is.getSubmitted() != null && instructor != null && context.hasPermission(instructor, Right.InstructorPreferences) && !is.getPreferences().isEmpty());
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			if (pref.getPrefProlog().equals(PreferenceLevel.sNeutral)) continue;
			survey.addPrefLevel(new PrefLevel(pref.getUniqueId(), pref.getPrefProlog(), pref.getAbbreviation(), pref.getPrefName(), pref.prefcolorNeutralBlack()));
		}
		
		if (instructor == null) {
			Set<Long> sessionIds = new HashSet<Long>();
			for (UserAuthority ua: context.getUser().getAuthorities()) {
				if (ua.getRole().equals(context.getUser().getCurrentAuthority().getRole())) {
					InstructorSurvey x = InstructorSurvey.getInstructorSurvey(externalId, (Long)ua.getAcademicSession().getQualifierId());
					if (x != null || context.hasPermissionAnySession(Right.InstructorSurvey, ua.getAcademicSession()))
						sessionIds.add((Long)ua.getAcademicSession().getQualifierId());
				}
			}
			if (!sessionIds.isEmpty()) {
				for (Session session: (List<Session>)SessionDAO.getInstance().getSession().createQuery(
						"from Session where uniqueId in :ids order by academicInitiative, sessionBeginDateTime")
						.setParameterList("ids", sessionIds, LongType.INSTANCE)
						.setCacheable(true).list()) {
					survey.addSession(new AcademicSessionInfo(
							session.getUniqueId(), session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
							session.getLabel(), session.getSessionBeginDateTime()));
					if (!session.getUniqueId().equals(sessionId) && survey.isEditable()) {
						InstructorSurvey x = InstructorSurvey.getInstructorSurvey(survey.getExternalId(), session.getUniqueId());
						if (x != null) {
							if ((x.getNote() != null && !x.getNote().isEmpty()) || !x.getPreferences().isEmpty()) {
								survey.addSessionWithPreferences(new AcademicSessionInfo(
										session.getUniqueId(), session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
										session.getLabel(), session.getSessionBeginDateTime()));
							}
							if (!x.getCourseRequirements().isEmpty()) {
								survey.addSessionWithCourses(new AcademicSessionInfo(
										session.getUniqueId(), session.getAcademicYear(), session.getAcademicTerm(), session.getAcademicInitiative(),
										session.getLabel(), session.getSessionBeginDateTime()));
							}
						}
					}
				}
			}
		}
		
		Preferences roomPrefs = new Preferences(-4l, CMSG.propertyRooms());
		Preferences buildingPrefs = new Preferences(-1l, CMSG.propertyBuildings());
		Preferences groupPrefs = new Preferences(-2l, CMSG.propertyRoomGroups());
		Preferences featurePrefs = new Preferences(-3l, CMSG.propertyRoomFeatures());
		Map<Long, Preferences> typedFeaturePrefs = new HashMap<Long, Preferences>();
		
		for (RoomGroup g: RoomGroup.getAllGlobalRoomGroups(sessionId))
			groupPrefs.addItem(g.getUniqueId(), g.getName(), g.getDescription());
		for (RoomFeature f: RoomFeature.getAllGlobalRoomFeatures(sessionId)) {
			if (f.getFeatureType() != null) {
				if (!f.getFeatureType().isShowInInstructorSurvey()) continue;
				Preferences fp = typedFeaturePrefs.get(f.getFeatureType().getUniqueId());
				if (fp == null) {
					fp = new Preferences(f.getFeatureType().getUniqueId(), f.getFeatureType().getLabel() + ":");
					typedFeaturePrefs.put(f.getFeatureType().getUniqueId(), fp);
				}
				fp.addItem(f.getUniqueId(), f.getLabel(), f.getDescription());;
			} else {
				featurePrefs.addItem(f.getUniqueId(), f.getLabel(), f.getDescription());
			}
		}
		
		List<DepartmentalInstructor> instructors = (List<DepartmentalInstructor>)DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where externalUniqueId=:id and department.session=:sessionId")
				.setString("id", externalId)
				.setLong("sessionId", sessionId)
				.setCacheable(true).list();

		for (DepartmentalInstructor di: instructors) {
			if (survey.getFormattedName() == null)
				survey.setFormattedName(di.getName(nameFormat));
			if (!survey.hasEmail())
				survey.setEmail(di.getEmail());
			survey.addDepartment(new InstructorDepartment(
					di.getDepartment().getUniqueId(),
					di.getDepartment().getDeptCode(),
					di.getDepartment().getLabel(),
					di.getPositionType() == null ? null : new IdLabel(di.getPositionType().getUniqueId(), di.getPositionType().getLabel(), null)
							));
			if (ApplicationProperty.InstructorSurveyRoomPreferencesDept.isTrue(di.getDepartment().getDeptCode(), true))
				for (Location location: di.getAvailableRooms()) {
					roomPrefs.addItem(location.getUniqueId(), location.getLabel(), location.getDisplayName());
				}
			if (ApplicationProperty.InstructorSurveyBuildingPreferencesDept.isTrue(di.getDepartment().getDeptCode(), true))
				for (Building bldg: di.getAvailableBuildings()) {
					buildingPrefs.addItem(bldg.getUniqueId(), bldg.getAbbrName(), null);
				}
			if (ApplicationProperty.InstructorSurveyRoomGroupPreferencesDept.isTrue(di.getDepartment().getDeptCode(), true))
				for (RoomGroup g: RoomGroup.getAllDepartmentRoomGroups(di.getDepartment())) {
					groupPrefs.addItem(g.getUniqueId(), g.getName() + " (" + g.getDepartment().getDeptCode() + ")", g.getDescription());
				}
			for (DepartmentRoomFeature f: RoomFeature.getAllDepartmentRoomFeatures(di.getDepartment())) {
				if (f.getFeatureType() != null) {
					Preferences fp = typedFeaturePrefs.get(f.getFeatureType().getUniqueId());
					if (fp == null) {
						fp = new Preferences(f.getFeatureType().getUniqueId(), f.getFeatureType().getLabel() + ":");
						typedFeaturePrefs.put(f.getFeatureType().getUniqueId(), fp);
					}
					fp.addItem(f.getUniqueId(), f.getLabel() + " (" + f.getDeptCode() + ")", f.getDescription());;
				} else if (ApplicationProperty.InstructorSurveyRoomFeaturePreferencesDept.isTrue(di.getDepartment().getDeptCode(), true)) {
					featurePrefs.addItem(f.getUniqueId(), f.getLabel() + " (" + f.getDeptCode() + ")", f.getDescription());
				}
			}
		}
		if (survey.getFormattedName() == null)
			survey.setFormattedName(context.getUser().getName());
		if (!survey.hasEmail())
			survey.setEmail(context.getUser().getEmail());
		
		List<InstructorCourseRequirementType> types = (List<InstructorCourseRequirementType>)InstructorCourseRequirementTypeDAO.getInstance().getSession().createQuery(
				"from InstructorCourseRequirementType order by sortOrder").list();
		Map<Long, CustomField> customFields = new HashMap<Long, CustomField>();
		for (InstructorCourseRequirementType type: types) {
			CustomField cf = new CustomField(type.getUniqueId(), type.getReference(), type.getLength());
			customFields.put(type.getUniqueId(), cf);
			survey.addCustomField(cf);
		}
		
		InstructorTimePreferencesModel timePref = new InstructorTimePreferencesModel();
		timePref.addMode(new RoomInterface.RoomSharingDisplayMode("|" + propertyValue(survey, ApplicationProperty.InstructorSurveyTimePreferencesDept, ApplicationProperty.InstructorSurveyTimePreferences)));
		timePref.setDefaultMode(0);
		timePref.setDefaultEditable(editable);
		boolean noProhobitedTimes = false;
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			RoomSharingOption option = new RoomSharingOption(timePref.char2id(PreferenceLevel.prolog2char(pref.getPrefProlog())), pref.prefcolor(), "", pref.getPrefName(), true);
			if (instructor == null && editable && PreferenceLevel.sProhibited.equals(pref.getPrefProlog()) && !isAllowed(survey, ApplicationProperty.InstructorSurveyTimePreferencesDeptHard, ApplicationProperty.InstructorSurveyTimePreferencesHard)) {
				noProhobitedTimes = true;
				continue;
			}
			if (!PreferenceLevel.sRequired.equals(pref.getPrefProlog()))
				timePref.addOption(option);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog())) {
				timePref.setDefaultOption(option);
				continue;
			}
		}
		timePref.setDefaultHorizontal(true);
		timePref.setNoteEditable(false);
		survey.setTimePrefs(timePref);
		
		if (buildingPrefs.hasItems() && isAllowed(survey, ApplicationProperty.InstructorSurveyBuildingPreferencesDept, ApplicationProperty.InstructorSurveyBuildingPreferences)) {
			if (!isAllowed(survey, ApplicationProperty.InstructorSurveyBuildingPreferencesDeptHard, ApplicationProperty.InstructorSurveyBuildingPreferencesHard))
				for (IdLabel item: buildingPrefs.getItems())
					for (PrefLevel p: survey.getPrefLevels())
						if (!p.isHard()) item.addAllowedPref(p.getId());
			survey.addRoomPreference(buildingPrefs);
		}
		if (roomPrefs.hasItems() && isAllowed(survey, ApplicationProperty.InstructorSurveyRoomPreferencesDept, ApplicationProperty.InstructorSurveyRoomPreferences)) {
			if (!isAllowed(survey, ApplicationProperty.InstructorSurveyRoomPreferencesDeptHard, ApplicationProperty.InstructorSurveyRoomPreferencesHard))
				for (IdLabel item: roomPrefs.getItems())
					for (PrefLevel p: survey.getPrefLevels())
						if (!p.isHard()) item.addAllowedPref(p.getId());
			survey.addRoomPreference(roomPrefs);
		}
		if (groupPrefs.hasItems() && isAllowed(survey, ApplicationProperty.InstructorSurveyRoomGroupPreferencesDept, ApplicationProperty.InstructorSurveyRoomGroupPreferences)) {
			if (!isAllowed(survey, ApplicationProperty.InstructorSurveyRoomGroupPreferencesDeptHard, ApplicationProperty.InstructorSurveyRoomGroupPreferencesHard))
				for (IdLabel item: groupPrefs.getItems())
					for (PrefLevel p: survey.getPrefLevels())
						if (!p.isHard()) item.addAllowedPref(p.getId());
			survey.addRoomPreference(groupPrefs);
		}
		if (featurePrefs.hasItems() && isAllowed(survey, ApplicationProperty.InstructorSurveyRoomFeaturePreferencesDept, ApplicationProperty.InstructorSurveyRoomFeaturePreferences)) {
			if (!isAllowed(survey, ApplicationProperty.InstructorSurveyRoomFeaturePreferencesDeptHard, ApplicationProperty.InstructorSurveyRoomFeaturePreferencesHard))
				for (IdLabel item: featurePrefs.getItems())
					for (PrefLevel p: survey.getPrefLevels())
						if (!p.isHard()) item.addAllowedPref(p.getId());
			survey.addRoomPreference(featurePrefs);
		}
		if (!typedFeaturePrefs.isEmpty()) {
			if (!isAllowed(survey, ApplicationProperty.InstructorSurveyRoomFeaturePreferencesDeptHard, ApplicationProperty.InstructorSurveyRoomFeaturePreferencesHard))
				for (Preferences prefs: typedFeaturePrefs.values())
					if (prefs.hasItems())
						for (IdLabel item: prefs.getItems())
							for (PrefLevel p: survey.getPrefLevels())
								if (!p.isHard()) item.addAllowedPref(p.getId());
			for (Preferences p: new TreeSet<Preferences>(typedFeaturePrefs.values()))
				survey.addRoomPreference(p);
		}
		
		Preferences distPref = new Preferences(-1l, CMSG.propertyDistribution());
		boolean hardDistPref = isAllowed(survey, ApplicationProperty.InstructorSurveyDistributionPreferencesDeptHard, ApplicationProperty.InstructorSurveyDistributionPreferencesHard);
		for (DistributionType dt: DistributionType.findAll(true, false, true)) {
			if (dt.getDepartments() != null && !dt.getDepartments().isEmpty()) {
				boolean hasDept = false;
				for (Department d: dt.getDepartments())
					if (survey.hasDepartment(d.getUniqueId())) { hasDept = true; break; }
				if (!hasDept) continue;
			}
			IdLabel dp = distPref.addItem(dt.getUniqueId(), dt.getLabel(), dt.getDescr());
			boolean hasPref = false;
			if (dt.getAllowedPref() != null && dt.getAllowedPref().length() > 0)
				for (int i = 0; i < dt.getAllowedPref().length(); i++) {
					PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.char2prolog(dt.getAllowedPref().charAt(i)));
					if (pref.isHard() && !hardDistPref) continue;
					dp.addAllowedPref(pref.getUniqueId());
					if (!pref.getPrefProlog().equals(PreferenceLevel.sNeutral))
						hasPref = true;
				}
			if (!hasPref)
				distPref.removeItem(dt.getUniqueId());
		}
		if (distPref.hasItems() && isAllowed(survey, ApplicationProperty.InstructorSurveyDistributionPreferencesDept, ApplicationProperty.InstructorSurveyDistributionPreferences))
			survey.setDistributionPreferences(distPref);
		
		if (is != null) {
			Set<Building> deptBuildings = (instructor == null ? null : instructor.getAvailableBuildings());
			Set<Location> deptRooms = (instructor == null ? null : instructor.getAvailableRooms());
			for (Preference p: is.getPreferences()) {
				if (p instanceof TimePref) {
					TimePref tp = (TimePref)p;
					if (noProhobitedTimes && tp.getPreference() != null)
						timePref.setPattern(tp.getPreference().replace(PreferenceLevel.sCharLevelProhibited, PreferenceLevel.sCharLevelStronglyDiscouraged));
					else
						timePref.setPattern(tp.getPreference());
					timePref.setNote(tp.getNote());
				} else if (p instanceof BuildingPref) {
					BuildingPref bp = (BuildingPref)p;
					Problem prob = (deptBuildings == null || deptBuildings.contains(bp.getBuilding()) ? Problem.NOT_APPLIED : Problem.DIFFERENT_DEPT);
					buildingPrefs.addSelection(new Selection(bp.getBuilding().getUniqueId(), bp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
				} else if (p instanceof RoomGroupPref) {
					RoomGroupPref gp = (RoomGroupPref)p;
					Problem prob = Problem.NOT_APPLIED;
					if (gp.getRoomGroup().getDepartment() != null && instructor != null && !instructor.getDepartment().equals(gp.getRoomGroup().getDepartment()))
						prob = Problem.DIFFERENT_DEPT;
					groupPrefs.addSelection(new Selection(gp.getRoomGroup().getUniqueId(), gp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
				} else if (p instanceof RoomFeaturePref) {
					RoomFeaturePref fp = (RoomFeaturePref)p;
					Problem prob = Problem.NOT_APPLIED;
					if (instructor != null && fp.getRoomFeature() instanceof DepartmentRoomFeature && !instructor.getDepartment().equals(((DepartmentRoomFeature)fp.getRoomFeature()).getDepartment()))
						prob = Problem.DIFFERENT_DEPT;
					if (fp.getRoomFeature().getFeatureType() != null) {
						Preferences prefs = typedFeaturePrefs.get(fp.getRoomFeature().getFeatureType().getUniqueId());
						if (prefs != null)
							prefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
					} else {
						featurePrefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
					}
				} else if (p instanceof DistributionPref) {
					DistributionPref dp = (DistributionPref)p;
					distPref.addSelection(new Selection(dp.getDistributionType().getUniqueId(), dp.getPrefLevel().getUniqueId(), p.getNote()));
				} else if (p instanceof RoomPref) {
					RoomPref rp = (RoomPref)p;
					Problem prob = (deptRooms == null || deptRooms.contains(rp.getRoom()) ? Problem.NOT_APPLIED : Problem.DIFFERENT_DEPT);
					roomPrefs.addSelection(new Selection(rp.getRoom().getUniqueId(), rp.getPrefLevel().getUniqueId(), p.getNote()).withProblem(prob));
				}
			}
			if (instructor != null) {
				for (Preference p: instructor.getPreferences()) {
					if (p instanceof TimePref) {
						TimePref tp = (TimePref)p;
						if (timePref.getPattern().equals(tp.getPreference())) {
							timePref.setProblem(null);
						} else {
							timePref.setProblem(Problem.LEVEL_CHANGED);
							timePref.setInstructorPattern(tp.getPreference());
						}
					} else if (p instanceof BuildingPref) {
						BuildingPref bp = (BuildingPref)p;
						buildingPrefs.addInstructorSelection(new Selection(bp.getBuilding().getUniqueId(), bp.getPrefLevel().getUniqueId(), p.getNote())); 
					} else if (p instanceof RoomGroupPref) {
						RoomGroupPref gp = (RoomGroupPref)p;
						groupPrefs.addInstructorSelection(new Selection(gp.getRoomGroup().getUniqueId(), gp.getPrefLevel().getUniqueId(), p.getNote()));
					} else if (p instanceof RoomFeaturePref) {
						RoomFeaturePref fp = (RoomFeaturePref)p;
						if (fp.getRoomFeature().getFeatureType() != null) {
							Preferences prefs = typedFeaturePrefs.get(fp.getRoomFeature().getFeatureType().getUniqueId());
							if (prefs != null)
								prefs.addInstructorSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()));
						} else {
							featurePrefs.addInstructorSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()));
						}
					} else if (p instanceof DistributionPref) {
						DistributionPref dp = (DistributionPref)p;
						distPref.addInstructorSelection(new Selection(dp.getDistributionType().getUniqueId(), dp.getPrefLevel().getUniqueId(), p.getNote()));
					} else if (p instanceof RoomPref) {
						RoomPref rp = (RoomPref)p;
						roomPrefs.addInstructorSelection(new Selection(rp.getRoom().getUniqueId(), rp.getPrefLevel().getUniqueId(), p.getNote()));
					}
				}
				
			}
		}
		
		Set<Long> courseIds = new HashSet<Long>();
		if (is != null) {
			survey.setSubmitted(is.getSubmitted());
			if (is.getEmail() != null && !is.getEmail().isEmpty())
				survey.setEmail(is.getEmail());
			survey.setApplied(is.getApplied());
			survey.setAppliedDeptCode(is.getAppliedDeptCode());
			if (is.getAppliedDeptCode() != null && !instructors.isEmpty()) {
				for (DepartmentalInstructor di: instructors)
					if (di.getDepartment().getDeptCode().equals(is.getAppliedDeptCode()))
						survey.setAppliedDeptCode(di.getDepartment().getLabel());
			}
			survey.setChanged(is.getChanged());
			if (is.getChangedBy() != null) {
				TimetableManager manager = TimetableManager.findByExternalId(is.getChangedBy());
				if (manager != null)
					survey.setChangedBy(manager.getName(nameFormat));
				else if (!instructors.isEmpty())
					survey.setChangedBy(instructors.get(0).getName(nameFormat));
				else
					survey.setChangedBy(is.getChangedBy());
			}
			survey.setNote(is.getNote());
			for (InstructorCourseRequirement r: is.getCourseRequirements()) {
				Course ci = new Course();
				ci.setReqId(r.getUniqueId());
				ci.setId(r.getCourseOffering() == null ? null : r.getCourseOffering().getUniqueId());
				ci.setCourseName(r.getCourseOffering() == null ? r.getCourse() : r.getCourseOffering().getCourseName());
				ci.setCourseTitle(r.getCourseOffering() == null ? null : r.getCourseOffering().getTitle());
				if (r.getCourseOffering() != null)
					courseIds.add(r.getCourseOffering().getUniqueId());
				for (InstructorCourseRequirementNote n: r.getNotes()) {
					CustomField cf = customFields.get(n.getType().getUniqueId());
					if (cf != null)
						ci.setCustomField(cf, n.getNote());
				}
				survey.addCourse(ci);
			}
		}
		for (CourseOffering co: (List<CourseOffering>)CourseOfferingDAO.getInstance().getSession().createQuery(
				"select distinct co from CourseOffering co, " +
				"DepartmentalInstructor i inner join i.classes ci inner join ci.classInstructing c " +
				"inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co " +
				"where co.isControl = true and io.notOffered = false and io.session = :sessionId and i.externalUniqueId=:id " +
				"and ci.lead = true and c.schedulingSubpart.itype.organized = true"
				)
				.setString("id", externalId)
				.setLong("sessionId", sessionId)
				.setCacheable(true).list()) {
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
		return survey;
	}
	
	protected String propertyValue(InstructorSurveyData survey, ApplicationProperty departmentalProperty, ApplicationProperty globalProperty) {
		if (survey.hasDepartments()) {
			for (InstructorDepartment dept: survey.getDepartments()) {
				String value = departmentalProperty.valueOfSession(survey.getSessionId(), dept.getDeptCode());
				if (value != null) return value;
			}
		}
		return globalProperty.valueOfSession(survey.getSessionId());
	}
	
	protected boolean isAllowed(InstructorSurveyData survey, ApplicationProperty departmentalProperty, ApplicationProperty globalProperty) {
		if (survey.hasDepartments()) {
			boolean hasFalse = false;
			for (InstructorDepartment dept: survey.getDepartments()) {
				String value = departmentalProperty.valueOfSession(survey.getSessionId(), dept.getDeptCode());
				if ("true".equalsIgnoreCase(value)) return true;
				if ("false".equalsIgnoreCase(value)) hasFalse = true;
			}
			if (hasFalse) return false;
		}
		return "true".equalsIgnoreCase(globalProperty.valueOfSession(survey.getSessionId()));
	}

}
