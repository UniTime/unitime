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

import java.util.Collections;
import java.util.List;

import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseDivision;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.CourseGroup;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.ListTeachingSchedules;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.TeachingSchedule;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TeachingScheduleDivision;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(TeachingScheduleAPI.ListTeachingSchedules.class)
public class TeachingScheduleList implements GwtRpcImplementation<ListTeachingSchedules, GwtRpcResponseList<TeachingSchedule>> {

	@Override
	public GwtRpcResponseList<TeachingSchedule> execute(ListTeachingSchedules request, SessionContext context) {
		context.checkPermission(Right.TeachingSchedules);
		GwtRpcResponseList<TeachingSchedule> ret = new GwtRpcResponseList<TeachingSchedule>();
		
		org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
		
		List<InstructionalOffering> offerings = null;
		if (request.getSubjectAreaId() == null) {
			offerings = hibSession.createQuery("select distinct io from TeachingScheduleDivision cd inner join cd.offering io where io.session = :sessionId and io.notOffered = false"
					).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setCacheable(true).list();
		} else {
			offerings = hibSession.createQuery("select distinct io from TeachingScheduleDivision cd inner join cd.offering io inner join io.courseOfferings co " +
					"where co.isControl = true and co.subjectArea = :subjectId and io.notOffered = false"
					).setLong("subjectId", request.getSubjectAreaId()).setCacheable(true).list();
		}
		
		for (InstructionalOffering io: offerings) {
			TeachingSchedule offering = new TeachingSchedule();
			offering.setOfferingId(io.getUniqueId());
			offering.setCourseId(io.getControllingCourseOffering().getUniqueId());
			offering.setSubjectAreaId(io.getControllingCourseOffering().getSubjectArea().getUniqueId());
			offering.setCourseName(io.getCourseName());
			for (TeachingScheduleDivision division: (List<TeachingScheduleDivision>)hibSession.createQuery("from TeachingScheduleDivision where offering = :offering order by ord"
					).setLong("offering", io.getUniqueId()).setCacheable(true).list()) {
				InstrOfferingConfig config = division.getConfig();
				ItypeDesc itype = division.getItype();
				CourseGroup group = offering.getGroup(config.getUniqueId(), itype.getItype());
				if (group == null) {
					group = new CourseGroup();
					group.setConfigId(config.getUniqueId());
					if (io.getInstrOfferingConfigs().size() > 1) {
						group.setConfigName(config.getName());
					} else {
						group.setConfigName("");
					}
					group.setTypeId(itype.getItype());
					group.setType(itype.getDesc());
					group.setHours(0);
					offering.addGroup(group);
					for (SchedulingSubpart ss: config.getSchedulingSubparts()) {
						if (ss.getItype().equals(itype)) {
							if (group.getNrClasses() == 0)
								group.setNrClasses(ss.getClasses().size());
							float mins = 0f;
							for (Class_ c: ss.getClasses()) {
								mins += ss.getMinutesPerWk() * c.effectiveDatePattern().getEffectiveNumberOfWeeks();
							}
							group.setHours(group.getHours() + Math.round(mins / ss.getClasses().size() / 45));
						}
					}
				}
				CourseDivision cd = new CourseDivision();
				if (division.getAttribute() != null) cd.setAttributeRef(division.getAttribute().getCode());
				cd.setHours(division.getHours());
				cd.setName(division.getName());
				cd.setNrParalel(division.getParallels());
				group.setNrGroups(division.getGroups());
				group.addDivision(cd);
			}
			ret.add(offering);
		}
		Collections.sort(ret);
		return ret;
	}
}
