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
import java.util.Locale;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorPreferencesEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.InstructorPreferencesEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefGroupEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PreferenceType;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Preferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.ClassEditBackend;
import org.unitime.timetable.webutil.BackTracker;

@GwtRpcImplements(InstructorPreferencesEditRequest.class)
public class InstructorPreferencesBackend implements GwtRpcImplementation<InstructorPreferencesEditRequest, InstructorPreferencesEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	@Override
	public InstructorPreferencesEditResponse execute(InstructorPreferencesEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(request.getId());
		context.checkPermission(instructor, Right.InstructorPreferences);
		
		if (instructor == null)
			throw new GwtRpcException(MSG.errorNoInstructorId());

		if (request.getOperation() != null) {
			InstructorPreferencesEditResponse ret;
			switch (request.getOperation()) {
			case CLEAR_CLASS_PREFS:
				context.checkPermission(instructor, Right.InstructorEditClearPreferences);

				ClassEditBackend.doClear(instructor.getPreferences(),
						Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING,
						Preference.Type.DISTRIBUTION);

				if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Preferences".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()))
					instructor.setUnavailableDays(null);
					instructor.setUnavailableOffset(null);

				hibSession.merge(instructor);
				hibSession.flush();

	            ChangeLog.addChange(
	                    null,
	                    context,
	                    instructor,
	                    ChangeLog.Source.INSTRUCTOR_PREF_EDIT,
	                    ChangeLog.Operation.CLEAR_PREF,
	                    null,
	                    instructor.getDepartment());
				
				ret = new InstructorPreferencesEditResponse();
				ret.setUrl("instructor?id=" + instructor.getUniqueId());
				return ret;
			case UPDATE:
			case NEXT:
			case PREVIOUS:
				Transaction tx = hibSession.beginTransaction();
				try {
					InstructorPreferencesEditResponse data = request.getPayLoad();
					if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Preferences".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value())) {
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
							Preference.Type.TIME, Preference.Type.ROOM, Preference.Type.ROOM_FEATURE, Preference.Type.ROOM_GROUP, Preference.Type.BUILDING,
							Preference.Type.DISTRIBUTION);

			        hibSession.merge(instructor);
			        
		            ChangeLog.addChange(
		                    null,
		                    context,
		                    instructor,
		                    ChangeLog.Source.INSTRUCTOR_PREF_EDIT,
		                    ChangeLog.Operation.UPDATE,
		                    null,
		                    instructor.getDepartment());
					
					tx.commit();
					tx = null;
				} catch (Exception e) {
					if (tx != null) { tx.rollback(); }
					throw new GwtRpcException(e.getMessage(), e);
				}
				
				ret = new InstructorPreferencesEditResponse();
				if (request.getOperation() == Operation.PREVIOUS && request.getPayLoad().getPreviousId() != null)
					ret.setUrl("instructorPrefs?id=" + request.getPayLoad().getPreviousId());
				else if (request.getOperation() == Operation.NEXT  && request.getPayLoad().getNextId() != null)
					ret.setUrl("instructorPrefs?id=" + request.getPayLoad().getNextId());
				else
					ret.setUrl("instructor?id=" + instructor.getUniqueId());
				return ret;
			}
		}
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		
		InstructorPreferencesEditResponse ret = new InstructorPreferencesEditResponse();
		ret.setId(request.getId());
		ret.setName(instructor.getName(nameFormat));
		ret.setNbrRooms(1);
		
		if ("Enabled".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value()) || "Preferences".equalsIgnoreCase(ApplicationProperty.InstructorUnavailbeDays.value())) {
			ret.setInstructorUnavailability(instructor.getUnavailablePattern());
		}
		
		DepartmentalInstructor next = instructor.getNextDepartmentalInstructor(context, Right.InstructorPreferences); 
        ret.setNextId(next==null ? null : next.getUniqueId());
        DepartmentalInstructor previous = instructor.getPreviousDepartmentalInstructor(context, Right.InstructorPreferences); 
        ret.setPreviousId(previous == null ? null : previous.getUniqueId());
        ret.setCanClearPrefs(context.hasPermission(instructor, Right.InstructorEditClearPreferences));
		
        ClassEditBackend.fillInPreferences(ret, instructor, context);
        fillInDistributionPreferences(ret, instructor, context, true);
        
        BackTracker.markForBack(
        		context,
        		"instructor?id="+ret.getId(),
        		MSG.backInstructor(ret.getName()),
        		true, false);

		return ret;
	}
	
	protected static void fillInDistributionPreferences(PrefGroupEditResponse response, PreferenceGroup pg, SessionContext context, boolean fillPrefs) {
		Preferences distPrefs = new Preferences(PreferenceType.DISTRIBUTION);
		distPrefs.setAllowHard(context.hasPermission(pg, Right.CanUseHardDistributionPrefs));
		for (DistributionType type: DistributionType.findApplicable(pg.getDepartment(), true, false)) {
			IdLabel id = distPrefs.addItem(type.getUniqueId(), type.getLabel(), type.getDescr());
			id.setAllowedPrefs(type.getAllowedPref());
		}
		if (fillPrefs)
			for (DistributionPref dp: pg.effectivePreferences(DistributionPref.class)) {
				Selection selection = new Selection(dp.getDistributionType().getUniqueId(), dp.getPrefLevel().getUniqueId());
				distPrefs.addSelection(selection);
			}
		response.setDistributionPreferences(distPrefs);
	}

}
