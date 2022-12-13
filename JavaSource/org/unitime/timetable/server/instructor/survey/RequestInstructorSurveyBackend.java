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
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Course;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorDepartment;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurvey;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorSurveyRequest;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorTimePreferencesModel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.PrefLevel;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.Preferences;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.CurriculumInterface.CourseInterface;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorSurveyRequest.class)
public class RequestInstructorSurveyBackend implements GwtRpcImplementation<InstructorSurveyRequest, InstructorSurvey> {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Override
	public InstructorSurvey execute(InstructorSurveyRequest request, SessionContext context) {
		String externalId = request.getExternalId();
		if (externalId == null) externalId = context.getUser().getExternalUserId();
		
		InstructorSurvey survey = new InstructorSurvey();
		survey.setExternalId(externalId);
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		
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
		}
		if (survey.getFormattedName() == null)
			survey.setFormattedName(context.getUser().getName());
		if (!survey.hasEmail())
			survey.setEmail(context.getUser().getEmail());
		
		InstructorTimePreferencesModel timePref = new InstructorTimePreferencesModel();
		timePref.addMode(new RoomInterface.RoomSharingDisplayMode("|0|4|90|246|12"));
		timePref.setDefaultMode(0);
		timePref.setDefaultEditable(true);
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(false)) {
			// if (PreferenceLevel.sStronglyPreferred.equals(pref.getPrefProlog())) continue;
			// if (PreferenceLevel.sStronglyDiscouraged.equals(pref.getPrefProlog())) continue;
			RoomSharingOption option = new RoomSharingOption(timePref.char2id(PreferenceLevel.prolog2char(pref.getPrefProlog())), pref.prefcolor(), "", pref.getPrefName(), true);
			if (!PreferenceLevel.sRequired.equals(pref.getPrefProlog()))
				timePref.addOption(option);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog())) {
				timePref.setDefaultOption(option);
				continue;
			}
			// if (PreferenceLevel.sRequired.equals(pref.getPrefProlog())) continue;

			/*
			else if (PreferenceLevel.sPreferred.equals(pref.getPrefProlog()))
				survey.addPrefLevel(new PrefLevel(pref.getUniqueId(), pref.getPrefName(), pref.getPrefName(), pref.prefcolorNeutralBlack()));
			else if (PreferenceLevel.sDiscouraged.equals(pref.getPrefProlog()))
				survey.addPrefLevel(new PrefLevel(pref.getUniqueId(), pref.getPrefName(), pref.getPrefName(), pref.prefcolorNeutralBlack()));
				*/
			else
				survey.addPrefLevel(new PrefLevel(pref.getUniqueId(), pref.getPrefProlog(), pref.getAbbreviation(), pref.getPrefName(), pref.prefcolorNeutralBlack()));
		}
		timePref.setDefaultHorizontal(true);
		timePref.setNoteEditable(false);
		survey.setTimePrefs(timePref);
		
		Preferences buildingPrefs = new Preferences(-1l, MESSAGES.colBuilding());
		Preferences groupPrefs = new Preferences(-2l, MESSAGES.colRoomGroups());
		Preferences featurePrefs = new Preferences(-3l, MESSAGES.colRoomFeatures());
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
						fp = new Preferences(f.getFeatureType().getUniqueId(), f.getFeatureType().getLabel());
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
		if (groupPrefs.hasItems())
			survey.addRoomPreference(groupPrefs);
		if (featurePrefs.hasItems())
			survey.addRoomPreference(featurePrefs);
		if (!typedFeaturePrefs.isEmpty())
			for (Preferences p: new TreeSet<Preferences>(typedFeaturePrefs.values()))
				survey.addRoomPreference(p);
		
		Preferences distPref = new Preferences(-1l, MESSAGES.colDistributions());
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
			Course ci = new Course();
			ci.setId(co.getUniqueId());
			ci.setCourseName(co.getCourseName());
			ci.setCoruseTitle(co.getTitle());
			survey.addCourse(ci);
		}
		if (survey.hasCourses())
			Collections.sort(survey.getCourses(), new Comparator<CourseInterface>() {
				@Override
				public int compare(CourseInterface co1, CourseInterface co2) {
					return co1.getCourseName().compareTo(co2.getCourseName());
				}
			});
		
		/*
		survey.addCustomField(new CustomField(1l, "Est.\nLimit", 5));
		survey.addCustomField(new CustomField(2l, "Estim. %\nAttendance", 8));
		survey.addCustomField(new CustomField(3l, "Instructor\nUCO", 10));
		survey.addCustomField(new CustomField(4l, "Time\nPreferences", 20));
		survey.addCustomField(new CustomField(5l, "Room\nPreferences", 20));
		survey.addCustomField(new CustomField(6l, "Additional\nNotes", 40));
		*/
		survey.addCustomField(new CustomField(6l, "Notes", 82));
		return survey;
	}

}
