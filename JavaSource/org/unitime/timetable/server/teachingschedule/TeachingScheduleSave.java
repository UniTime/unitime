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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Attribute;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Clazz;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroup;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.MeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.SaveTeachingSchedule;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.TeachingScheduleAssignment;
import org.unitime.timetable.model.TeachingScheduleDivision;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(TeachingScheduleAPI.SaveTeachingSchedule.class)
public class TeachingScheduleSave implements GwtRpcImplementation<TeachingScheduleAPI.SaveTeachingSchedule, GwtRpcResponseNull> {

	@Override
	public GwtRpcResponseNull execute(SaveTeachingSchedule request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(request.getOffering().getOfferingId(), hibSession);
		context.checkPermission(io.getControllingCourseOffering().getDepartment(), Right.EditTeachingSchedule);
		
		List<TeachingScheduleDivision> divisions = (List<TeachingScheduleDivision>)hibSession.createQuery("from TeachingScheduleDivision where offering = :offeringId order by ord").setLong("offeringId", io.getUniqueId()).list();
		int ord = 0;
		if (request.getOffering().hasGroups())
			for (CourseGroup cg: request.getOffering().getGroups()) {
				for (CourseDivision cd: cg.getDivisions()) {
					TeachingScheduleDivision division = null;
					for (Iterator<TeachingScheduleDivision> i = divisions.iterator(); i.hasNext(); ) {
						TeachingScheduleDivision d = i.next();
						if (d.getConfig().getUniqueId().equals(cg.getConfigId()) && d.getItype().getItype().equals(cg.getTypeId())) {
							division = d;
							i.remove();
							break;
						}
					}
					if (division == null) {
						division = new TeachingScheduleDivision();
						division.setConfig(InstrOfferingConfigDAO.getInstance().get(cg.getConfigId(), hibSession));
						division.setItype(ItypeDescDAO.getInstance().get(cg.getTypeId(), hibSession));
						division.setOffering(io);
						division.setAssignments(new HashSet<TeachingScheduleAssignment>());
					}
					division.setOrder(ord++);
					division.setHours(cd.getHours());
					division.setGroups(cg.getNrGroups());
					division.setName(cd.getName());
					division.setParallels(cd.getNrParalel());
					hibSession.saveOrUpdate(division);
					Attribute a = request.getOffering().getAttribute(cd.getAttributeRef());
					division.setAttribute(a == null ? null : InstructorAttributeDAO.getInstance().get(a.getId(), hibSession));
					Set<TeachingScheduleAssignment> remove = new HashSet<TeachingScheduleAssignment>(division.getAssignments());
					for (Clazz clazz: request.getOffering().getClasses()) {
						if (clazz.getConfigId().equals(cg.getConfigId()) && clazz.getTypeId().equals(cg.getTypeId())) {
							for (MeetingAssignment ma: clazz.getMeetingAssignments()) {
								if (!ma.hasDivision() || !ma.getDivision().equals(cd)) continue;
								TeachingScheduleAssignment meeting = null;
								for (Iterator<TeachingScheduleAssignment> i = remove.iterator(); i.hasNext(); ) { 
									TeachingScheduleAssignment m = i.next();
									if (ma.getClassMeetingId().equals(m.getMeeting().getUniqueId()) && clazz.getClassIndex() == m.getClassIndex() && clazz.getGroupIndex() == m.getGroupIndex() && ma.getFirstHour() == m.getFirstHour()) {
										meeting = m;
										i.remove();
										break;
									}
								}
								if (meeting == null) {
									meeting = new TeachingScheduleAssignment();
									meeting.setDivision(division);
									meeting.setClassIndex(clazz.getClassIndex());
									meeting.setGroupIndex(clazz.getGroupIndex());
									meeting.setMeeting(MeetingDAO.getInstance().get(ma.getClassMeetingId(), hibSession));
									meeting.setInstructors(new HashSet<DepartmentalInstructor>());
									division.getAssignments().add(meeting);
								}
								meeting.setFirstHour(ma.getFirstHour());
								meeting.setLastHour(ma.getLastHour());
								meeting.setNote(ma.getNote());
								Set<DepartmentalInstructor> inst = new HashSet<DepartmentalInstructor>(meeting.getInstructors());
								if (ma.hasInstructors())
									for (Long id: ma.getInstructor()) {
										boolean found = false;
										for (Iterator<DepartmentalInstructor> i = inst.iterator(); i.hasNext();) {
											DepartmentalInstructor di = i.next();
											if (di.getUniqueId().equals(id)) {
												found = true;
												i.remove();
											}
										}
										if (!found)
											meeting.getInstructors().add(DepartmentalInstructorDAO.getInstance().get(id, hibSession));
									}
								for (DepartmentalInstructor di: inst)
									meeting.getInstructors().remove(di);
								hibSession.saveOrUpdate(meeting);
							}
						}
					}
					for (TeachingScheduleAssignment m: remove) {
						division.getAssignments().remove(m);
						hibSession.delete(m);
					}
				}
			}
		
		hibSession.flush();
		return null;
	}
}
