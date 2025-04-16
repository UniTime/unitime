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
package org.unitime.timetable.server.instructor;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorAssignmentPreferencesEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorAssignmentPreferencesEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefGroupEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PreferenceType;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Preferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.PreferenceLevelDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassEditBackend;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(InstructorAssignmentPreferencesEditRequest.class)
public class InstructorAssignmentPreferencesBackend implements GwtRpcImplementation<InstructorAssignmentPreferencesEditRequest, InstructorAssignmentPreferencesEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public InstructorAssignmentPreferencesEditResponse execute(InstructorAssignmentPreferencesEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(request.getId());

		if (instructor == null)
			throw new GwtRpcException(MSG.errorNoInstructorId());

		context.checkPermission(instructor.getDepartment(), Right.InstructorAssignmentPreferences);

		if (request.getOperation() != null) {
			InstructorAssignmentPreferencesEditResponse ret;
			switch (request.getOperation()) {
			case CLEAR_CLASS_PREFS:
				context.checkPermission(instructor, Right.InstructorClearAssignmentPreferences);

				ClassEditBackend.doClear(instructor.getPreferences(),
						Preference.Type.TIME,
						Preference.Type.DISTRIBUTION,
						Preference.Type.COURSE
						);

				if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Assignments".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
					instructor.setUnavailableDays(null);
					instructor.setUnavailableOffset(null);

				hibSession.merge(instructor);
				hibSession.flush();

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    instructor,
	                    ChangeLog.Source.INSTRUCTOR_ASSIGNMENT_PREF_EDIT,
	                    ChangeLog.Operation.CLEAR_PREF,
	                    null,
	                    instructor.getDepartment());
				
				ret = new InstructorAssignmentPreferencesEditResponse();
				ret.setUrl("instructor?id=" + instructor.getUniqueId());
				return ret;
			case UPDATE:
			case NEXT:
			case PREVIOUS:
				Transaction tx = hibSession.beginTransaction();
				try {
					InstructorAssignmentPreferencesEditResponse data = request.getPayLoad();
					if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Assignments".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value())) {
						if (data.hasInstructorUnavailability() && data.getInstructorUnavailability().indexOf('1') >= 0) {
							String unavailability = data.getInstructorUnavailability();
							int startMonth = instructor.getDepartment().getSession().getPatternStartMonth();
							int firstOne = unavailability.indexOf('1') +
									instructor.getDepartment().getSession().getDayOfYear(1,startMonth);
							Calendar cal = Calendar.getInstance(Locale.US);
							cal.setTime(instructor.getDepartment().getSession().getSessionBeginDateTime());
							instructor.setUnavailableDays(unavailability.substring(unavailability.indexOf('1'), unavailability.lastIndexOf('1') + 1));
							instructor.setUnavailableOffset(Integer.valueOf(cal.get(Calendar.DAY_OF_YEAR)-firstOne-1));
						} else {
							instructor.setUnavailableDays(null);
							instructor.setUnavailableOffset(null);
						}
					}
					
					ClassEditBackend.doUpdate(instructor, instructor.getPreferences(), data,
							Preference.Type.TIME,
							Preference.Type.DISTRIBUTION,
							Preference.Type.COURSE);
					
					instructor.setTeachingPreference(data.getTeachingPrefId() == null ? null : PreferenceLevelDAO.getInstance().get(data.getTeachingPrefId()));
					instructor.setMaxLoad(data.getMaxTeachingLoad());
					
					Set<InstructorAttribute> attributes = new HashSet<InstructorAttribute>(instructor.getAttributes());
					if (data.hasInstructorAttributeIds()) {
						attributeId: for (Long attributeId: data.getInstructorAttributeIds()) {
							for (Iterator<InstructorAttribute> i = attributes.iterator(); i.hasNext();) {
								InstructorAttribute a = i.next();
								if (a.getUniqueId().equals(attributeId)) {
									i.remove();
									continue attributeId;
								}
							}
							instructor.getAttributes().add(InstructorAttributeDAO.getInstance().get(attributeId));
						}
					}
					instructor.getAttributes().removeAll(attributes);
					
			        hibSession.merge(instructor);
			        
		            ChangeLog.addChange(
		                    null,
		                    context,
		                    instructor,
		                    ChangeLog.Source.INSTRUCTOR_ASSIGNMENT_PREF_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    null,
		                    instructor.getDepartment());
					
					tx.commit();
					tx = null;
				} catch (Exception e) {
					if (tx != null) { tx.rollback(); }
					throw new GwtRpcException(e.getMessage(), e);
				}
				
				ret = new InstructorAssignmentPreferencesEditResponse();
				if (request.getOperation() == Operation.PREVIOUS && request.getPayLoad().getPreviousId() != null)
					ret.setUrl("instrAssignmentPrefs?id=" + request.getPayLoad().getPreviousId());
				else if (request.getOperation() == Operation.NEXT  && request.getPayLoad().getNextId() != null)
					ret.setUrl("instrAssignmentPrefs?id=" + request.getPayLoad().getNextId());
				else
					ret.setUrl("instructor?id=" + instructor.getUniqueId());
				return ret;
			}
		}
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		
		InstructorAssignmentPreferencesEditResponse ret = new InstructorAssignmentPreferencesEditResponse();
		ret.setId(request.getId());
		ret.setName(instructor.getName(nameFormat));
		ret.setNbrRooms(1);
		if (instructor.getTeachingPreference() == null)
			ret.setTeachingPrefId(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited).getUniqueId());
		else
			ret.setTeachingPrefId(instructor.getTeachingPreference().getUniqueId());
		ret.setMaxTeachingLoad(instructor.getMaxLoad());
		
		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Assignments".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value())) {
			ret.setInstructorUnavailability(instructor.getUnavailablePattern());
		}
		
		DepartmentalInstructor next = instructor.getNextDepartmentalInstructor(context, Right.InstructorAssignmentPreferences); 
        ret.setNextId(next==null ? null : next.getUniqueId());
        DepartmentalInstructor previous = instructor.getPreviousDepartmentalInstructor(context, Right.InstructorAssignmentPreferences); 
        ret.setPreviousId(previous == null ? null : previous.getUniqueId());
        ret.setCanClearPrefs(context.hasPermission(instructor, Right.InstructorClearAssignmentPreferences));
		
        ClassEditBackend.fillInPreferenceLevels(ret, instructor, context);
        ClassEditBackend.fillInTimePreferences(ret, instructor, null, null, context, true);
        InstructorPreferencesBackend.fillInDistributionPreferences(ret, instructor, context, true);
        fillInCoursePreferences(ret, instructor, context, true);
        
        for (InstructorAttribute attribute: instructor.getDepartment().getAvailableAttributes()) {
        	AttributeInterface a = new AttributeInterface();
        	a.setId(attribute.getUniqueId());
			a.setParentId(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getUniqueId());
			a.setParentName(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getName());
			a.setCode(attribute.getCode());
			a.setName(attribute.getName());
			if (attribute.getType() != null) {
				AttributeTypeInterface t = new AttributeTypeInterface();
				t.setId(attribute.getType().getUniqueId());
				t.setAbbreviation(attribute.getType().getReference());
				t.setLabel(attribute.getType().getLabel());
				t.setConjunctive(attribute.getType().isConjunctive());
				t.setRequired(attribute.getType().isRequired());
				a.setType(t);
			}
			if (attribute.getDepartment() != null) {
				DepartmentInterface d = new DepartmentInterface();
				d.setId(attribute.getDepartment().getUniqueId());
				d.setAbbreviation(attribute.getDepartment().getAbbreviation());
				d.setDeptCode(attribute.getDepartment().getDeptCode());
				d.setLabel(attribute.getDepartment().getName());
				d.setTitle(attribute.getDepartment().getLabel());
				a.setDepartment(d);
			}
        	ret.addAttribute(a);
        }
        
        for (InstructorAttribute a: instructor.getAttributes())
        	ret.addInstructorAttribute(a.getUniqueId());
        
        BackTracker.markForBack(
        		context,
        		"instructor?id="+ret.getId(),
        		MSG.backInstructor(ret.getName()),
        		true, false);

		return ret;
	}
	
	protected static void fillInCoursePreferences(PrefGroupEditResponse response, PreferenceGroup pg, SessionContext context, boolean fillPrefs) {
		Preferences coursePrefs = new Preferences(PreferenceType.COURSE);
		coursePrefs.setAllowHard(true);
		for (Object o: pg.getAvailableCourses()) {
			CourseOffering course = (CourseOffering)o;
			coursePrefs.addItem(course.getUniqueId(), course.getCourseNameWithTitle(), course.getScheduleBookNote());
		}
		if (fillPrefs)
			for (InstructorCoursePref dp: pg.effectivePreferences(InstructorCoursePref.class)) {
				Selection selection = new Selection(dp.getCourse().getUniqueId(), dp.getPrefLevel().getUniqueId());
				coursePrefs.addSelection(selection);
			}
		response.setCoursePreferences(coursePrefs);
	}

}
