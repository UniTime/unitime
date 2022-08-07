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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.StudentSchedulingPreferencesInterface;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ChangeStudentPreferences implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static StudentSectioningConstants CONST = Localization.create(StudentSectioningConstants.class);

	private Collection<Long> iStudentIds = null;
	private StudentSchedulingPreferencesInterface iPreferences = null;
	
	public ChangeStudentPreferences forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	public ChangeStudentPreferences forStudent(Long studentId) {
		if (iStudentIds == null) iStudentIds = new ArrayList<Long>();
		iStudentIds.add(studentId);
		return this;
	}
	
	public ChangeStudentPreferences withPreferences(StudentSchedulingPreferencesInterface preferences) {
		iPreferences = preferences;
		return this;
	}

	public Collection<Long> getStudentIds() { return iStudentIds; }
	public StudentSchedulingPreferencesInterface getPreference() { return iPreferences; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, name());
			try {
				XStudent student = server.getStudent(studentId);
				helper.beginTransaction();
				Date ts = new Date();
				try {
					Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null && dbStudent != null) {
						
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getStudentId())
							.setExternalId(student.getExternalId())
							.setName(student.getName()));
						
						dbStudent.setPreferredClassModality(iPreferences.getClassModality());
						dbStudent.setPreferredScheduleGaps(iPreferences.getScheduleGaps());
						dbStudent.setClassStartDate(iPreferences.getClassDateFrom());
						dbStudent.setClassEndDate(iPreferences.getClassDateTo());
						
						if (iPreferences.getClassDateFrom() != null)
							action.addOptionBuilder().setKey("class-first").setValue(Formats.getDateFormat(CONST.requestDateFormat()).format(iPreferences.getClassDateFrom()));
						if (iPreferences.getClassDateTo() != null)
							action.addOptionBuilder().setKey("class-last").setValue(Formats.getDateFormat(CONST.requestDateFormat()).format(iPreferences.getClassDateTo()));
						if (iPreferences.getClassModality() != null)
							action.addOptionBuilder().setKey("modality").setValue(iPreferences.getClassModality().name());
						if (iPreferences.getScheduleGaps() != null)
							action.addOptionBuilder().setKey("gaps").setValue(iPreferences.getScheduleGaps().name());
						
						
						StudentDAO.getInstance().update(dbStudent);

						helper.getHibSession().saveOrUpdate(dbStudent);

						student.updatePreferences(dbStudent, server.getAcademicSession().getDatePatternFirstDate());
						server.update(student, false);
						
						String pref = FindStudentInfoAction.getStudentSchedulingPreference(student, server, helper);
						if (pref != null)
							action.addMessage(OnlineSectioningLog.Message.newBuilder().setText(pref).setTimeStamp(ts.getTime()).setLevel(OnlineSectioningLog.Message.Level.INFO));
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
		return true;			
	}

	@Override
	public String name() {
		return "schedule-prefs";
	}

}
