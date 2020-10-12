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

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Attribute;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetInstructorTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingTime;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.InstructorMeetingAssignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

@GwtRpcImplements(TeachingScheduleAPI.GetInstructorTeachingSchedule.class)
public class TeachingScheduleGetInstructorSchedule implements GwtRpcImplementation<GetInstructorTeachingSchedule, Instructor> {
	static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public Instructor execute(GetInstructorTeachingSchedule request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		Instructor ret = new Instructor();
		
		DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(request.getInstructorId(), hibSession);
		context.checkPermission(instructor.getDepartment(), Right.TeachingSchedules);
		int year = instructor.getDepartment().getSession().getSessionStartYear();
		Format<Date> df = Formats.getDateFormat(CONSTANTS.meetingDateFormat());
		
		int hourMin = ApplicationProperty.TeachingScheduleHour.intValue();
		int breakTime = ApplicationProperty.TeachingScheduleBreak.intValue();
		int hourSlots = (hourMin + breakTime) / 5;
		
		String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		ret.setInstructorId(instructor.getUniqueId());
		ret.setMaxLoad(instructor.getMaxLoad() == null ? 0 : Math.round(instructor.getMaxLoad()));
		ret.setName(instructor.getName(instructorNameFormat));

		for (TeachingScheduleAssignment cm: (List<TeachingScheduleAssignment>)hibSession.createQuery(
				"select cm from TeachingScheduleAssignment cm inner join cm.instructors i where i.uniqueId = :id"
				).setLong("id", request.getInstructorId()).setCacheable(true).list()) {
			InstructorMeetingAssignment o = new InstructorMeetingAssignment();
			Meeting m = cm.getMeeting();
			o.setName(cm.getDivision().getOffering().getCourseName());
			o.setType(cm.getDivision().getItype().getDesc().trim());
			o.setDivision(cm.getDivision().getName());
			o.setGroup(cm.getGroupName());
			o.setNote(cm.getNote());
			o.setClassMeetingId(m.getUniqueId());
			o.setDayOfYear(CalendarUtils.date2dayOfYear(year, m.getMeetingDate()));
			o.setLoad(1 + cm.getLastHour() - cm.getFirstHour());
			o.setLocation(m.getLocation() == null ? "" : m.getLocation().getLabel());
			o.setMeetingTime(cm.getStartTime((hourMin + breakTime) * cm.getFirstHour()));
			o.setMeetingDate(df.format(m.getMeetingDate()));
			o.setHours(new MeetingTime(Constants.getDayOfWeek(m.getMeetingDate()), m.getStartPeriod() + hourSlots * cm.getFirstHour(), m.getStartPeriod() + hourSlots * cm.getLastHour() + (hourMin / 5)));
			if (cm.getDivision().getAttribute() != null)
				o.setAttributeRef(cm.getDivision().getAttribute().getCode());
			ret.addAssignment(o);
		}
		
		for (InstructorAttribute ia: instructor.getAttributes()) {
			Attribute a = new Attribute();
			a.setId(ia.getUniqueId());
			a.setReference(ia.getCode());
			a.setLabel(ia.getNameWithType());
			ret.addAttribute(a);
		}
		
		if (instructor.hasUnavailabilities()) {
			Calendar cal = Calendar.getInstance(Locale.US);
			Date start = instructor.getUnavailableStartDate();
			int idx = -1;
			while ((idx = instructor.getUnavailableDays().indexOf('1', idx + 1)) >= 0) {
				cal.setTime(start);
				cal.add(Calendar.DAY_OF_YEAR, idx);
				ret.addUnavailableDay(cal.getTime(), CalendarUtils.date2dayOfYear(year, cal.getTime()));
			}
		}
		if (instructor.getTimePreferences() != null) {
			for (Iterator j = instructor.getTimePreferences().iterator(); j.hasNext();) {
				TimePref tp = (TimePref) j.next();
				TimePatternModel m = tp.getTimePatternModel();
				for (int d = 0; d < m.getNrDays(); d++)
					for (int t = 0; t < m.getNrTimes(); t++) {
						if (!PreferenceLevel.sProhibited.equals(m.getPreference(d, t))) continue;
						int first = t;
						while (t + 1 < m.getNrTimes() && PreferenceLevel.sProhibited.equals(m.getPreference(d, t + 1))) t++;
						int last = t;
						ret.addProhibitedTime(d, m.getStartSlot(first), m.getStartSlot(last) + m.getSlotsPerMtg());
					}
			}
		}
		Collection<ClassInstructor> classes = instructor.getClasses();
		if (instructor.getExternalUniqueId() != null && !instructor.getExternalUniqueId().isEmpty())
			classes = (List<ClassInstructor>)hibSession.createQuery(
					"from ClassInstructor ci where ci.instructor.department.session = :sessionId and ci.instructor.externalUniqueId = :id"
					).setLong("sessionId", instructor.getDepartment().getSessionId()).setString("id", instructor.getExternalUniqueId()).setCacheable(true).list();
		for (ClassInstructor ci: classes) {
			if (!ci.isLead() || ci.getClassInstructing().getEvent() == null) continue;
			for (Meeting m: ci.getClassInstructing().getEvent().getMeetings()) {
				InstructorMeetingAssignment o = new InstructorMeetingAssignment();
				o.setName(ci.getClassInstructing().getClassLabel());
				o.setClassMeetingId(m.getUniqueId());
				o.setDayOfYear(CalendarUtils.date2dayOfYear(year, m.getMeetingDate()));
				o.setLoad(0);
				o.setLocation(m.getLocation() == null ? "" : m.getLocation().getLabel());
				o.setMeetingTime(m.getStartTime());
				o.setMeetingDate(df.format(m.getMeetingDate()));
				o.setHours(new MeetingTime(Constants.getDayOfWeek(m.getMeetingDate()), m.getStartPeriod(), m.getStopPeriod()));
				ret.addAssignment(o);
			}
		}
		
		if (ret.hasAssignments())
			Collections.sort(ret.getAssignmetns());
		
		return ret;
	}
}
