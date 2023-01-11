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
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
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
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorCourseRequirementTypeDAO;
import org.unitime.timetable.model.dao.InstructorSurveyDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
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
		if (!context.isAuthenticated() || context.getUser().getCurrentAuthority() == null)
        	throw new AccessDeniedException();
		boolean admin = context.hasPermission(Right.InstructorSurveyAdmin);
		String externalId = context.getUser().getExternalUserId();
		if (request.getExternalId() != null && !request.getExternalId().isEmpty() && !externalId.equals(request.getExternalId())) {
			context.checkPermission(Right.InstructorSurveyAdmin);
			externalId = request.getExternalId();
		}
		boolean editable = true;
		InstructorSurvey is = (InstructorSurvey)InstructorSurveyDAO.getInstance().getSession().createQuery(
				"from InstructorSurvey where session = :sessionId and externalUniqueId = :externalId"
				).setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
				.setString("externalId", externalId).setMaxResults(1).uniqueResult();

		if (!admin) {
			editable = context.hasPermissionAnyAuthority(Right.InstructorSurvey, new Qualifiable[] { new SimpleQualifier("Session", context.getUser().getCurrentAcademicSessionId())});
			if (is != null && is.getSubmitted() != null)
				editable = false;
			if (!editable && is == null)
				throw new GwtRpcException(MESSAGES.errorInstructorSurveyNotAllowed());
		}
		
		final InstructorSurveyData survey = new InstructorSurveyData();
		survey.setExternalId(externalId);
		survey.setEditable(editable);
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			if (pref.getPrefProlog().equals(PreferenceLevel.sNeutral)) continue;
			survey.addPrefLevel(new PrefLevel(pref.getUniqueId(), pref.getPrefProlog(), pref.getAbbreviation(), pref.getPrefName(), pref.prefcolorNeutralBlack()));
		}
		
		Preferences roomPrefs = new Preferences(-4l, CMSG.propertyRooms());
		for (DepartmentalInstructor di: (List<DepartmentalInstructor>)DepartmentalInstructorDAO.getInstance().getSession().createQuery(
				"from DepartmentalInstructor where externalUniqueId=:id and department.session=:sessionId")
				.setString("id", externalId)
				.setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
				.setCacheable(true).list()) {
			if (survey.getFormattedName() == null)
				survey.setFormattedName(di.getName(nameFormat));
			if (!survey.hasEmail())
				survey.setEmail(di.getEmail());
			survey.addDepartment(new InstructorDepartment(
					di.getDepartment().getUniqueId(), di.getDepartment().getLabel(),
					di.getPositionType() == null ? null : new IdLabel(di.getPositionType().getUniqueId(), di.getPositionType().getLabel(), null)
							));
			for (RoomDept rd: di.getDepartment().getRoomDepts()) {
				if (rd.getPreference() != null && rd.getPreference().getPrefProlog().equals(PreferenceLevel.sProhibited)) continue;
				Location location = rd.getRoom();
				IdLabel rp = roomPrefs.addItem(location.getUniqueId(), location.getLabel(), location.getDisplayName());
				for (PrefLevel pl: survey.getPrefLevels())
					if (!pl.isHard()) rp.addAllowedPref(pl.getId());
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
		timePref.addMode(new RoomInterface.RoomSharingDisplayMode("|" + ApplicationProperty.InstructorSurveyTimePreferences.value()));
		timePref.setDefaultMode(0);
		timePref.setDefaultEditable(editable);
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			RoomSharingOption option = new RoomSharingOption(timePref.char2id(PreferenceLevel.prolog2char(pref.getPrefProlog())), pref.prefcolor(), "", pref.getPrefName(), true);
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
		
		Preferences buildingPrefs = new Preferences(-1l, CMSG.propertyBuildings());
		Preferences groupPrefs = new Preferences(-2l, CMSG.propertyRoomGroups());
		Preferences featurePrefs = new Preferences(-3l, CMSG.propertyRoomFeatures());
		Map<Long, Preferences> typedFeaturePrefs = new HashMap<Long, Preferences>();

		boolean includeExtDepts = true;
		boolean hasDepts = survey.hasDepartments();
		for (Location location: (List<Location>)RoomDAO.getInstance().getSession().createQuery(
				"select distinct r from Location r inner join r.roomDepts rd" + (hasDepts ? ", DepartmentalInstructor i":"") + " where " +
				(!hasDepts ? "rd.department.externalManager = true and rd.department.inheritInstructorPreferences = true" : 
						includeExtDepts ? "(rd.department = i.department or (rd.department.externalManager = true and rd.department.inheritInstructorPreferences = true))"
						: "rd.department = i.department") +
				(hasDepts ? " and i.externalUniqueId=:id and i.department.session=:sessionId"
						: " and :id = :id") +
				" and r.session = :sessionId")
				.setString("id", externalId)
				.setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
				.setCacheable(true).list()) {
			if (location instanceof Room) {
				Building bldg = ((Room)location).getBuilding();
				buildingPrefs.addItem(bldg.getUniqueId(), bldg.getAbbrName(), null);
			}
			for (RoomGroup g: location.getRoomGroups()) {
				if (g.isGlobal())
					groupPrefs.addItem(g.getUniqueId(), g.getName(), g.getDescription());
				else if (survey.hasDepartment(g.getDepartment().getUniqueId())) {
					groupPrefs.addItem(g.getUniqueId(), g.getName() + " (" + g.getDepartment().getDeptCode() + ")", g.getDescription());
				}
			}
			for (RoomFeature f: location.getGlobalRoomFeatures()) {
				if (f.getFeatureType() != null) {
					Preferences fp = typedFeaturePrefs.get(f.getFeatureType().getUniqueId());
					if (fp == null) {
						fp = new Preferences(f.getFeatureType().getUniqueId(), f.getFeatureType().getLabel());
						typedFeaturePrefs.put(f.getFeatureType().getUniqueId(), fp);
					}
					fp.addItem(f.getUniqueId(), f.getLabel(), f.getDescription());;
				} else {
					featurePrefs.addItem(f.getUniqueId(), f.getLabel(), f.getDescription());
				}
			}
			for (DepartmentRoomFeature f: location.getDepartmentRoomFeatures()) {
				if (f.getFeatureType() != null) {
					Preferences fp = typedFeaturePrefs.get(f.getFeatureType().getUniqueId());
					if (fp == null) {
						fp = new Preferences(f.getFeatureType().getUniqueId(), f.getFeatureType().getLabel() + ":");
						typedFeaturePrefs.put(f.getFeatureType().getUniqueId(), fp);
					}
					fp.addItem(f.getUniqueId(), f.getLabel() + " (" + f.getDeptCode() + ")", f.getDescription());;
				} else {
					featurePrefs.addItem(f.getUniqueId(), f.getLabel() + " (" + f.getDeptCode() + ")", f.getDescription());;
				}
			}
		}
		if (buildingPrefs.hasItems())
			survey.addRoomPreference(buildingPrefs);
		if (roomPrefs.hasItems())
			survey.addRoomPreference(roomPrefs);
		if (groupPrefs.hasItems())
			survey.addRoomPreference(groupPrefs);
		if (featurePrefs.hasItems())
			survey.addRoomPreference(featurePrefs);
		if (!typedFeaturePrefs.isEmpty())
			for (Preferences p: new TreeSet<Preferences>(typedFeaturePrefs.values()))
				survey.addRoomPreference(p);
		
		Preferences distPref = new Preferences(-1l, CMSG.propertyDistribution());
		for (DistributionType dt: DistributionType.findAll(true, false, true)) {
			if (dt.getDepartments() != null && !dt.getDepartments().isEmpty()) {
				boolean hasDept = false;
				for (Department d: dt.getDepartments())
					if (survey.hasDepartment(d.getUniqueId())) { hasDept = true; break; }
				if (!hasDept) continue;
			}
			IdLabel dp = distPref.addItem(dt.getUniqueId(), dt.getLabel(), dt.getDescr());
			if (dt.getAllowedPref() != null && dt.getAllowedPref().length() > 0)
				for (int i = 0; i < dt.getAllowedPref().length(); i++) {
					dp.addAllowedPref(
							PreferenceLevel.getPreferenceLevel(PreferenceLevel.char2prolog(dt.getAllowedPref().charAt(i))).getUniqueId()
							);
				}
		}
		if (distPref.hasItems())
			survey.setDistributionPreferences(distPref);
		
		if (is != null) {
			for (Preference p: is.getPreferences()) {
				if (p instanceof TimePref) {
					TimePref tp = (TimePref)p;
					timePref.setPattern(tp.getPreference());
					timePref.setNote(tp.getNote());
				} else if (p instanceof BuildingPref) {
					BuildingPref bp = (BuildingPref)p;
					buildingPrefs.addSelection(new Selection(bp.getBuilding().getUniqueId(), bp.getPrefLevel().getUniqueId(), p.getNote()));
				} else if (p instanceof RoomGroupPref) {
					RoomGroupPref gp = (RoomGroupPref)p;
					groupPrefs.addSelection(new Selection(gp.getRoomGroup().getUniqueId(), gp.getPrefLevel().getUniqueId(), p.getNote()));
				} else if (p instanceof RoomFeaturePref) {
					RoomFeaturePref fp = (RoomFeaturePref)p;
					if (fp.getRoomFeature().getFeatureType() != null) {
						Preferences prefs = typedFeaturePrefs.get(fp.getRoomFeature().getFeatureType().getUniqueId());
						if (prefs != null)
							prefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()));
					} else {
						featurePrefs.addSelection(new Selection(fp.getRoomFeature().getUniqueId(), fp.getPrefLevel().getUniqueId(), p.getNote()));
					}
				} else if (p instanceof DistributionPref) {
					DistributionPref dp = (DistributionPref)p;
					distPref.addSelection(new Selection(dp.getDistributionType().getUniqueId(), dp.getPrefLevel().getUniqueId(), p.getNote()));
				} else if (p instanceof RoomPref) {
					RoomPref rp = (RoomPref)p;
					roomPrefs.addSelection(new Selection(rp.getRoom().getUniqueId(), rp.getPrefLevel().getUniqueId(), p.getNote()));
				}
			}
		}
		
		Set<Long> courseIds = new HashSet<Long>();
		if (is != null) {
			survey.setSubmitted(is.getSubmitted());
			if (is.getEmail() != null && !is.getEmail().isEmpty())
				survey.setEmail(is.getEmail());
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
				.setLong("sessionId", context.getUser().getCurrentAcademicSessionId())
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

}
