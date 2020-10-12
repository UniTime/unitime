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
package org.unitime.timetable.server.teachingschedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.dao.TeachingScheduleAssignmentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.custom.CustomClassAttendanceProvider;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

public class TeachingScheduleClassEventAttendance implements CustomClassAttendanceProvider {
	static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public StudentClassAttendance getCustomClassAttendanceForStudent(Student student, OnlineSectioningHelper helper, SessionContext context) {
		return null;
	}

	@Override
	public InstructorClassAttendance getCustomClassAttendanceForInstructor(String externalUniqueId, Long sessionId, OnlineSectioningHelper helper, SessionContext context) {
		if (externalUniqueId == null || externalUniqueId.isEmpty()) return null;
		org.hibernate.Session hibSession = (helper == null ? TeachingScheduleAssignmentDAO.getInstance().getSession() : helper.getHibSession());
		List<TeachingScheduleAssignment> assignments = (List<TeachingScheduleAssignment>)hibSession.createQuery(
				"select cm from TeachingScheduleAssignment cm inner join cm.instructors i where i.externalUniqueId = :id and i.department.session = :sessionId"
				).setString("id", externalUniqueId).setLong("sessionId", sessionId).setCacheable(true).list();
		
		if (!assignments.isEmpty()) return new InstructorTeachingAttendance(assignments, context.getUser().getProperty(UserProperty.NameFormat));
		return null;
	}
	
	public static class InstructorTeachingAttendance implements InstructorClassAttendance {
		Map<Long, List<TeachingScheduleAssignment>> iAttendance = new HashMap<Long, List<TeachingScheduleAssignment>>();
		int iHourMin = ApplicationProperty.TeachingScheduleHour.intValue();
		int iBreakTime = ApplicationProperty.TeachingScheduleBreak.intValue();
		int iHourSlots = (iHourMin + iBreakTime) / 5;
		Format<Date> iDF = Formats.getDateFormat(CONSTANTS.eventDateFormatShort());
		String iInstructorNameFormat = "last-first";
		
		public InstructorTeachingAttendance(List<TeachingScheduleAssignment> assignments, String nameFormat) {
			for (TeachingScheduleAssignment a: assignments)
				if (a.getMeeting() != null) {
					List<TeachingScheduleAssignment> tsa = iAttendance.get(a.getMeeting().getUniqueId());
					if (tsa == null) {
						tsa = new ArrayList<TeachingScheduleAssignment>();
						iAttendance.put(a.getMeeting().getUniqueId(), tsa);
					}
					tsa.add(a);
				}
			iInstructorNameFormat = nameFormat;
		}

		@Override
		public void updateAttendance(EventInterface classEvent) {
			long id = 0;
			Date ts = new Date();
			if (classEvent.hasMeetings()) {
				List<MeetingInterface> meetings = new ArrayList<MeetingInterface>(classEvent.getMeetings()); 
				for (MeetingInterface m: meetings) {
					List<TeachingScheduleAssignment> assignments = iAttendance.get(m.getId());
					if (assignments == null || assignments.isEmpty()) continue;
					if (classEvent.hasNotes())
						for (Iterator<NoteInterface> i = classEvent.getNotes().iterator(); i.hasNext(); ) {
							NoteInterface n = i.next();
							if (n.getType() != null) i.remove();
						}
					int startSlot = m.getStartSlot();
					Set<Integer> added = new HashSet<Integer>();
					for (int i = 0; i < assignments.size(); i++) {
						TeachingScheduleAssignment a = assignments.get(i);
						String note = (meetings.size() == 1 ? "" : iDF.format(m.getMeetingDate()) + " " + Constants.slot2str(startSlot + a.getFirstHour() * iHourSlots) + ": ") +
								a.getGroupName() + (a.getNote() != null ? " " + a.getNote() : a.getDivision().getName() != null ? a.getDivision().getName() : "");
						NoteInterface n = new NoteInterface();
						n.setDate(ts); ts = new Date(ts.getTime() + 1);
						n.setNote(note);
						n.setId(--id);
						n.setMeetingId(m.getId());
						n.setMeetings(iDF.format(m.getMeetingDate()) + " " + Constants.slot2str(startSlot + a.getFirstHour() * iHourSlots) );
						classEvent.addNote(n);
						if (i == 0) {
							m.setStartSlot(startSlot + a.getFirstHour() * iHourSlots);
							m.setEndSlot(startSlot + (1 + a.getLastHour()) * iHourSlots);
							m.setEndOffset(-iBreakTime);
							added.add(a.getFirstHour() * 24 + a.getLastHour());
							for (DepartmentalInstructor di: a.getInstructors()) {
								ContactInterface contact = new ContactInterface();
								contact.setFirstName(di.getFirstName());
								contact.setMiddleName(di.getMiddleName());
								contact.setLastName(di.getLastName());
								contact.setAcademicTitle(di.getAcademicTitle());
								contact.setEmail(di.getEmail());
								contact.setFormattedName(di.getName(iInstructorNameFormat));
								m.addMeetingContact(contact);
							}
						} else if (added.add(a.getFirstHour() * 24 + a.getLastHour()))  {
							MeetingInterface other = new MeetingInterface(m);
							other.setStartSlot(startSlot + a.getFirstHour() * iHourSlots);
							other.setEndSlot(startSlot + (1 + a.getLastHour()) * iHourSlots);
							other.setId(--id);
							n.setMeetingId(other.getId());
							classEvent.addMeeting(other);
							for (DepartmentalInstructor di: a.getInstructors()) {
								ContactInterface contact = new ContactInterface();
								contact.setFirstName(di.getFirstName());
								contact.setMiddleName(di.getMiddleName());
								contact.setLastName(di.getLastName());
								contact.setAcademicTitle(di.getAcademicTitle());
								contact.setEmail(di.getEmail());
								contact.setFormattedName(di.getName(iInstructorNameFormat));
								other.addMeetingContact(contact);
							}
						}
					}
				}
			}
		}
	}
}
