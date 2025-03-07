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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.ResultType;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.StudentPinsProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class ReleaseStudentPin implements OnlineSectioningAction<Map<Long, String>> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private Collection<Long> iStudentIds = null;
	private boolean iRelease = true;
	
	public ReleaseStudentPin forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public ReleaseStudentPin setRelease(boolean release) {
		iRelease = release;
		return this;
	}
	
	public boolean isRelease() { return iRelease; }

	public Collection<Long> getStudentIds() { return iStudentIds; }

	@Override
	public Map<Long, String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<Long, String> pins = new HashMap<Long, String>();
		StudentPinsProvider provider = Customization.StudentPinsProvider.getProvider();
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, name());
			try {
				XStudent student = server.getStudent(studentId);
				helper.beginTransaction();
				try {
					Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null && dbStudent != null) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession(), true);
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getStudentId())
							.setExternalId(student.getExternalId())
							.setName(student.getName()));
						if (isRelease()) {
							action.addOptionBuilder().setKey("Mode").setValue("RELEASE");
							if ((dbStudent.getPin() == null || dbStudent.getPin().isEmpty()) && provider != null) {
								try {
									String pin = provider.retriveStudentPin(server, helper, student);
									dbStudent.setPin(pin);
									student.setPin(pin);
								} catch (SectioningException e) {
									if (action.getApiException() == null)
										action.setApiException("Failed to retrieve PIN: " + e.getMessage());
									action.setResult(ResultType.FAILURE);
									continue;
								}
							}
							if (dbStudent.getPin() != null && !dbStudent.getPin().isEmpty()) {
								dbStudent.setPinReleased(true);
								student.setPinReleased(true);
								pins.put(dbStudent.getUniqueId(), dbStudent.getPin());
								helper.getHibSession().merge(dbStudent);
								server.update(student, false);
								action.setResult(ResultType.TRUE);
							} else {
								action.setResult(ResultType.NULL);
							}
						} else {
							action.addOptionBuilder().setKey("Mode").setValue("SUPPRESS");
							dbStudent.setPinReleased(false);
							student.setPinReleased(false);
							pins.put(dbStudent.getUniqueId(), (dbStudent.getPin() == null ? "" : dbStudent.getPin()));
							helper.getHibSession().merge(dbStudent);
							server.update(student, false);
							action.setResult(ResultType.FALSE);
						}
					}
					helper.commitTransaction();
				} catch (Exception e) {
					helper.rollbackTransaction();
					if (e instanceof SectioningException) throw (SectioningException)e;
					throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
				}

			} finally {
				lock.release();
			}
		}
		return pins;			
	}

	@Override
	public String name() {
		return "pin-release";
	}

}
