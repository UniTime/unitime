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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ReloadAllStudents extends ReloadAllData {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Lock lock = server.lockAll();
		try {
			helper.beginTransaction();
			try {
				server.clearAllStudents();
				
		        Map<Long, List<XCourseRequest>> requestMap = new HashMap<Long, List<XCourseRequest>>();
				List<org.unitime.timetable.model.Student> students = helper.getHibSession().createQuery(
	                    "select distinct s from Student s " +
	                    "left join fetch s.courseDemands as cd " +
	                    "left join fetch cd.courseRequests as cr " +
	                    "left join fetch cr.classWaitLists as cwl " + 
	                    "left join fetch s.classEnrollments as e " +
	                    "left join fetch s.academicAreaClassifications as a " +
	                    "left join fetch s.posMajors as mj " +
	                    "left join fetch s.waitlists as w " +
	                    "left join fetch s.groups as g " +
	                    "where s.session.uniqueId=:sessionId").
	                    setLong("sessionId",server.getAcademicSession().getUniqueId()).list();
	            for (org.unitime.timetable.model.Student student: students) {
	            	XStudent s = loadStudent(student, requestMap, server, helper);
	            	if (s != null)
	            		server.update(s, true);
	            }

				helper.commitTransaction();
				return true;
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
		} finally {
			lock.release();
		}		
	}
	
	@Override
    public String name() { return "reload-all-students"; }

}
