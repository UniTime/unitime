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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

/**
 * @author Tomas Muller
 */
public class CustomCourseRequestsValidationHolder {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private static CourseRequestsValidationProvider sProvider = null;
	
	public synchronized static CourseRequestsValidationProvider getProvider() {
		if (sProvider == null) {
			try {
				sProvider = ((CourseRequestsValidationProvider)Class.forName(ApplicationProperty.CustomizationCourseRequestsValidation.value()).newInstance());
			} catch (Exception e) {
				throw new SectioningException(MSG.exceptionCourseRequestValidationProvider(e.getMessage()), e);
			}
		}
		return sProvider;
	}
	
	public synchronized static void release() {
		if (sProvider != null) {
			sProvider.dispose();
			sProvider = null;
		}
	}
	
	public synchronized static boolean hasProvider() {
		return sProvider != null || ApplicationProperty.CustomizationCourseRequestsValidation.value() != null;
	}
	
	public static class Check implements OnlineSectioningAction<CourseRequestInterface> {
		private static final long serialVersionUID = 1L;
		private CourseRequestInterface iRequest;
		
		public Check withRequest(CourseRequestInterface request) {
			iRequest = request;
			return this;
		}

		public CourseRequestInterface getRequest() { return iRequest; }
		
		@Override
		public CourseRequestInterface execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
			CourseRequestInterface request = getRequest();
			
			if (CustomCourseRequestsValidationHolder.hasProvider())
				CustomCourseRequestsValidationHolder.getProvider().check(server, helper, request);
			
			return request;
		}

		@Override
		public String name() {
			return "check-overrides";
		}

	}
	
	public static class Update implements OnlineSectioningAction<Boolean> {
		private static final long serialVersionUID = 1L;
		private Collection<Long> iStudentIds = null;
		
		public Update forStudents(Long... studentIds) {
			iStudentIds = new ArrayList<Long>();
			for (Long studentId: studentIds)
				iStudentIds.add(studentId);
			return this;
		}
		
		public Update forStudents(Collection<Long> studentIds) {
			iStudentIds = studentIds;
			return this;
		}

		
		public Collection<Long> getStudentIds() { return iStudentIds; }
		
		@Override
		public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
			if (!CustomCourseRequestsValidationHolder.hasProvider()) return false;
			List<Long> reloadIds = new ArrayList<Long>();
			
			helper.beginTransaction();
			try {
				for (Long studentId: getStudentIds()) {
					helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(studentId)
							.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
					
					Student student = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					
					if (student != null) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(studentId)
								.setExternalId(student.getExternalUniqueId())
								.setName(helper.getStudentNameFormat().format(student))
								.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
					
						if (CustomCourseRequestsValidationHolder.getProvider().updateStudent(server, helper, student, action))
							reloadIds.add(studentId);
					}
				}
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				if (e instanceof SectioningException)
					throw (SectioningException)e;
				throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
			}
			
			if (!reloadIds.isEmpty())
				server.execute(server.createAction(ReloadStudent.class).forStudents(reloadIds), helper.getUser());

			return !reloadIds.isEmpty();
		}

		@Override
		public String name() {
			return "update-overrides";
		}

	}
}
