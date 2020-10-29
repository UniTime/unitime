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
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Attribute;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroup;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingTime;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.InstructorMeetingAssignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ClassEvent;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.TeachingScheduleDivision;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.CalendarUtils;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

@GwtRpcImplements(TeachingScheduleAPI.GetTeachingSchedule.class)
public class TeachingScheduleGet implements GwtRpcImplementation<TeachingScheduleAPI.GetTeachingSchedule, TeachingScheduleAPI.TeachingSchedule> {
	static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Override
	public TeachingSchedule execute(GetTeachingSchedule request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		
		CourseOffering co = CourseOfferingDAO.getInstance().get(request.getCourseId(), hibSession);
		InstructionalOffering io = co.getInstructionalOffering(); co = io.getControllingCourseOffering();
		context.checkPermission(io.getControllingCourseOffering().getDepartment(), Right.TeachingSchedules);
		int year = io.getSession().getSessionStartYear();
		
		int hourMin = ApplicationProperty.TeachingScheduleHour.intValue();
		int breakTime = ApplicationProperty.TeachingScheduleBreak.intValue();
		int hourSlots = (hourMin + breakTime) / 5;
		
		TeachingSchedule ret = new TeachingSchedule();
		ret.setOfferingId(io.getUniqueId());
		ret.setCourseId(io.getControllingCourseOffering().getUniqueId());
		ret.setSubjectAreaId(io.getControllingCourseOffering().getSubjectArea().getUniqueId());
		ret.setCourseName(co.getCourseNameWithTitle());
		
		Format<Date> df = Formats.getDateFormat(CONSTANTS.meetingDateFormat());
		for (InstrOfferingConfig config: sorted(io.getInstrOfferingConfigs(), new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()))) {
			for (SchedulingSubpart ss: sorted(config.getSchedulingSubparts(), new SchedulingSubpartComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()))) {
				CourseGroup group = ret.getGroup(config.getUniqueId(), ss.getItype().getItype());
				if (group == null) {
					group = new CourseGroup();
					group.setConfigId(config.getUniqueId());
					if (io.getInstrOfferingConfigs().size() > 1) {
						group.setConfigName(config.getName());
					} else {
						group.setConfigName("");
					}
					group.setTypeId(ss.getItype().getItype());
					group.setType(ss.getItype().getDesc());
					group.setNrClasses(ss.getClasses().size());
					group.setHours(0);
					ret.addGroup(group);
				}
				float mins = 0f;
				for (Class_ c: ss.getClasses()) {
					mins += ss.getMinutesPerWk() * c.effectiveDatePattern().getEffectiveNumberOfWeeks();
				}
				group.setHours(group.getHours() + Math.round(mins / ss.getClasses().size() / hourMin));
				int classIndex = 0;
				for (Class_ c: TeachingScheduleGet.sorted(ss.getClasses(), new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY))) {
					ClassEvent e = c.getEvent();
					if (e != null) {
						for (Meeting m : new TreeSet<Meeting>(e.getMeetings())) {
							if (m.getApprovalStatus() != Meeting.Status.APPROVED.ordinal()) continue;
							TeachingScheduleAPI.TeachingMeeting meeting = new TeachingScheduleAPI.TeachingMeeting();
							meeting.setClassId(c.getUniqueId());
							meeting.setClassMeetingId(m.getUniqueId());
							meeting.setMeetingTime(m.getStartTime());
							meeting.setMeetingDate(df.format(m.getMeetingDate()));
							meeting.setDayOfYear(CalendarUtils.date2dayOfYear(year, m.getMeetingDate()));
							meeting.setLocation(m.getLocation() == null ? "" : m.getLocation().getLabel());
							meeting.setClassIndex(classIndex);
							int hours = (2 + m.getStopPeriod() - m.getStartPeriod()) / hourSlots;
							for (int h = 0; h < hours; h++) {
								meeting.addHour(
										Constants.getDayOfWeek(m.getMeetingDate()),
										m.getStartPeriod() + hourSlots * h, m.getStartPeriod() + hourSlots * h + (hourMin / 5));
							}
							group.addMeeting(meeting);
						}
					}
					if (c.getClassSuffix() != null && !c.getClassSuffix().isEmpty())
						group.setClassSuffix(classIndex, c.getClassSuffix());
					classIndex++;
				}
			}
		}
		
		for (InstructorAttribute ia: InstructorAttribute.getAllGlobalAttributes(io.getSessionId())) {
			if (request.hasAttributeType() && (ia.getType() == null || !ia.getType().getReference().equals(request.getAttributeType()))) continue;
			Attribute a = new Attribute();
			a.setId(ia.getUniqueId());
			a.setReference(ia.getCode());
			a.setLabel(ia.getName());
			ret.addAttribute(a);
		}
		for (InstructorAttribute ia: InstructorAttribute.getAllDepartmentalAttributes(co.getSubjectArea().getDepartment().getUniqueId())) {
			if (request.hasAttributeType() && (ia.getType() == null || !ia.getType().getReference().equals(request.getAttributeType()))) continue;
			Attribute a = new Attribute();
			a.setId(ia.getUniqueId());
			a.setReference(ia.getCode());
			a.setLabel(ia.getNameWithType());
			ret.addAttribute(a);
		}
		
		String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		for (DepartmentalInstructor i: (List<DepartmentalInstructor>)hibSession.createQuery(
				"from DepartmentalInstructor where department.uniqueId = :departmentId order by lastName, firstName, middleName"
				).setLong("departmentId", co.getSubjectArea().getDepartment().getUniqueId()).setCacheable(true).list()) {
			if (i.getMaxLoad() == null || Math.round(i.getMaxLoad()) <= 0) continue;
			Instructor instructor = new Instructor();
			instructor.setInstructorId(i.getUniqueId());
			instructor.setMaxLoad(Math.round(i.getMaxLoad()));
			instructor.setName(i.getName(instructorNameFormat));
			if (i.hasUnavailabilities()) {
				Calendar cal = Calendar.getInstance(Locale.US);
				Date start = i.getUnavailableStartDate();
				int idx = -1;
				while ((idx = i.getUnavailableDays().indexOf('1', idx + 1)) >= 0) {
					cal.setTime(start);
					cal.add(Calendar.DAY_OF_YEAR, idx);
					instructor.addUnavailableDay(cal.getTime(), CalendarUtils.date2dayOfYear(year, cal.getTime()));
				}
			}
			if (i.getTimePreferences() != null) {
				for (Iterator j = i.getTimePreferences().iterator(); j.hasNext();) {
					TimePref tp = (TimePref) j.next();
					TimePatternModel m = tp.getTimePatternModel();
					for (int d = 0; d < m.getNrDays(); d++)
						for (int t = 0; t < m.getNrTimes(); t++) {
							if (!PreferenceLevel.sProhibited.equals(m.getPreference(d, t))) continue;
							int first = t;
							while (t + 1 < m.getNrTimes() && PreferenceLevel.sProhibited.equals(m.getPreference(d, t + 1))) t++;
							int last = t;
							instructor.addProhibitedTime(d, m.getStartSlot(first), m.getStartSlot(last) + m.getSlotsPerMtg());
						}
				}
			}
			Collection<ClassInstructor> classes = i.getClasses();
			if (i.getExternalUniqueId() != null && !i.getExternalUniqueId().isEmpty())
				classes = (List<ClassInstructor>)hibSession.createQuery(
						"from ClassInstructor ci where ci.instructor.department.session = :sessionId and ci.instructor.externalUniqueId = :id"
						).setLong("sessionId", i.getDepartment().getSessionId()).setString("id", i.getExternalUniqueId()).setCacheable(true).list();
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
					instructor.addAssignment(o);
				}
			}
			for (TeachingScheduleAssignment cm: (List<TeachingScheduleAssignment>)hibSession.createQuery(
					"select cm from TeachingScheduleAssignment cm inner join cm.instructors i where i.uniqueId = :id and cm.division.offering != :offeringId"
					).setLong("id", i.getUniqueId()).setLong("offeringId", io.getUniqueId()).setCacheable(true).list()) {
				InstructorMeetingAssignment o = new InstructorMeetingAssignment();
				Meeting m = cm.getMeeting();
				o.setName(m.getEvent().getEventName());
				o.setClassMeetingId(m.getUniqueId());
				o.setDayOfYear(CalendarUtils.date2dayOfYear(year, m.getMeetingDate()));
				o.setLoad(1 + cm.getLastHour() - cm.getFirstHour());
				o.setLocation(m.getLocation() == null ? "" : m.getLocation().getLabel());
				o.setMeetingTime(cm.getStartTime((hourMin + breakTime) * cm.getFirstHour()));
				o.setMeetingDate(df.format(m.getMeetingDate()));
				o.setHours(new MeetingTime(Constants.getDayOfWeek(m.getMeetingDate()), m.getStartPeriod() + hourSlots * cm.getFirstHour(), m.getStartPeriod() + hourSlots * cm.getLastHour() + (hourMin / 5)));
				if (cm.getDivision().getAttribute() != null)
					o.setAttributeRef(cm.getDivision().getAttribute().getCode());
				instructor.addAssignment(o);
			}
			for (InstructorAttribute ia: i.getAttributes()) {
				if (request.hasAttributeType() && (ia.getType() == null || !ia.getType().getReference().equals(request.getAttributeType()))) continue;
				Attribute a = new Attribute();
				a.setId(ia.getUniqueId());
				a.setReference(ia.getCode());
				a.setLabel(ia.getNameWithType());
				instructor.addAttribute(a);
			}
			ret.addInstructor(instructor);
		}
		
		List<TeachingScheduleDivision> divisions = (List<TeachingScheduleDivision>)hibSession.createQuery("from TeachingScheduleDivision where offering = :offeringId order by ord").setLong("offeringId", io.getUniqueId()).list();
		boolean updateClasses = false;
		for (TeachingScheduleDivision division: divisions) {
			CourseGroup group = ret.getGroup(division.getConfig().getUniqueId(), division.getItype().getItype());
			if (group == null) continue;
			CourseDivision cd = new CourseDivision();
			if (division.getAttribute() != null) cd.setAttributeRef(division.getAttribute().getCode());
			cd.setHours(division.getHours());
			cd.setName(division.getName());
			cd.setNrParalel(division.getParallels());
			group.setNrGroups(division.getGroups());
			group.addDivision(cd);
			for (TeachingScheduleAssignment cm: sorted(division.getAssignments(), new Comparator<TeachingScheduleAssignment>() {
				@Override
				public int compare(TeachingScheduleAssignment m1, TeachingScheduleAssignment m2) {
					if (m1.getClassIndex() != m2.getClassIndex()) return (m1.getClassIndex() < m2.getClassIndex() ? -1 : 1);
					if (m1.getGroupIndex() != m2.getGroupIndex()) return (m1.getGroupIndex() < m2.getGroupIndex() ? -1 : 1);
					int cmp = m1.getMeeting().getStartTime().compareTo(m2.getMeeting().getStartTime());
					if (cmp != 0) return cmp;
					return Integer.compare(m1.getFirstHour(), m2.getFirstHour());
				}
			})) {
				org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting meeting = group.getMeeting(cm.getMeeting().getUniqueId());
				if (meeting == null) continue;
				MeetingAssignment ma = new MeetingAssignment();
				ma.setClassMeetingId(meeting.getClassMeetingId());
				ma.setDivision(cd);
				ma.setHours(cm.getFirstHour(), cm.getLastHour());
				ma.setNote(cm.getNote());
				Clazz clazz = ret.getClass(group, cm.getClassIndex(), cm.getGroupIndex());
				if (clazz == null) {
					clazz = new Clazz(group, cm.getClassIndex(), cm.getGroupIndex(), group.getClassSuffix(cm.getClassIndex()));
					if (clazz.hasMeetingAssignments())
						clazz.getMeetingAssignments().clear();
					ret.addClass(clazz);
					updateClasses = true;
				}
				for (DepartmentalInstructor di: cm.getInstructors()) {
					ma.addInstructor(di.getUniqueId());
				}
				clazz.addMeetingAssignment(ma);
			}
		}
		if (updateClasses) {
			ret.updateClasses(true);
			for (Clazz clazz: ret.getClasses()) {
				CourseGroup g = ret.getGroup(clazz.getConfigId(), clazz.getTypeId());
				if (g != null && g.hasMeetings())
					for (org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting m: g.getMeetings()) {
						if (m.getClassIndex() == clazz.getClassIndex()) {
							MeetingAssignment prev = null;
							for (int hour = 0; hour < m.getHours().size(); hour ++) {
								MeetingAssignment ma = clazz.getMeetingAssignment(m, hour);
								if (ma == null) {
									if (prev != null) {
										prev.setHours(prev.getFirstHour(), hour);
									} else {
										prev = new MeetingAssignment(m);
										prev.setHours(hour, hour);
										clazz.addMeetingAssignment(prev);
									}
								} else {
									prev = null;
								}
							}
						}
					}
				Collections.sort(clazz.getMeetingAssignments(), new Comparator<MeetingAssignment>() {
					@Override
					public int compare(MeetingAssignment ma1, MeetingAssignment ma2) {
						org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting m1 = ret.getMeeting(ma1);
						org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingMeeting m2 = ret.getMeeting(ma2);
						if (m1.getDayOfYear() != m2.getDayOfYear())
							return m1.getDayOfYear() < m2.getDayOfYear() ? -1 : 1;
						if (m1.getStartSlot() != m2.getStartSlot())
							return m1.getStartSlot() < m2.getStartSlot() ? -1 : 1;
						int cmp = m1.getClassMeetingId().compareTo(m2.getClassMeetingId());
						if (cmp != 0) return cmp;
						return Integer.compare(ma1.getFirstHour(), ma2.getFirstHour());
					}
				});
			}
		}
		
		return ret;
	}
	
	static <T> TreeSet<T> sorted(Collection<T> data, Comparator cmp) {
		TreeSet<T> ret = new TreeSet<T>(cmp);
		ret.addAll(data);
		return ret;
	}

}
