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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ChangeStudentGroup implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	private Collection<Long> iStudentIds = null;
	private Long iGroupId = null;
	private boolean iRemove = true;
	
	public ChangeStudentGroup forStudents(Collection<Long> studentIds) {
		iStudentIds = studentIds;
		return this;
	}
	
	public ChangeStudentGroup withGroup(Long groupId, boolean remove) {
		iGroupId = groupId;
		iRemove = remove;
		return this;
	}
	
	public Collection<Long> getStudentIds() { return iStudentIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		StudentGroup group = (StudentGroup)helper.getHibSession().createQuery("from StudentGroup where uniqueId = :groupId").setLong("groupId", iGroupId).uniqueResult();
		if (group == null || group.getType() == null || !group.getType().isAdvisorsCanSet()) return false;
		for (Long studentId: getStudentIds()) {
			Lock lock = server.lockStudent(studentId, null, name());
			try {
				XStudent student = server.getStudent(studentId);
				helper.beginTransaction();
				try {
					Student dbStudent = StudentDAO.getInstance().get(studentId, helper.getHibSession());
					if (student != null && dbStudent != null) {
						OnlineSectioningLog.Action.Builder action = helper.addAction(this, server.getAcademicSession());
						action.setStudent(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(student.getStudentId())
							.setExternalId(student.getExternalId())
							.setName(student.getName()));
						action.addOther(OnlineSectioningLog.Entity.newBuilder()
								.setUniqueId(group.getUniqueId())
								.setName(group.getGroupAbbreviation())
								.setExternalId(group.getGroupName())
								.setType(OnlineSectioningLog.Entity.EntityType.OTHER));
						action.addOptionBuilder().setKey("action").setValue(iRemove ? "remove" : "add");
						action.addMessageBuilder().setLevel(OnlineSectioningLog.Message.Level.INFO).setText(group.getGroupAbbreviation() + " - " + group.getGroupName() + " (" + (iRemove ? "remove" : "add") + ")");
						
						boolean changed = false;
						if (iRemove) {
							changed = group.getStudents().remove(dbStudent);
							dbStudent.getGroups().remove(group);
							student.getGroups().remove(group.getGroupAbbreviation());
						} else {
							changed = group.getStudents().add(dbStudent);
							dbStudent.getGroups().add(group);
							student.getGroups().add(group.getGroupAbbreviation());
						}
						helper.getHibSession().saveOrUpdate(group);
						server.update(student, false);
						
						action.setResult(changed ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
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
		return "group-change";
	}

}
