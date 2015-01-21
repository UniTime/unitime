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
import java.util.Collections;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ClassAssignmentChanged implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Collection<Long> iClassIds = null;
	
	public ClassAssignmentChanged forClasses(Long... classIds) {
		iClassIds = new ArrayList<Long>();
		for (Long classId: classIds)
			iClassIds.add(classId);
		return this;
	}
	
	public ClassAssignmentChanged forClasses(Collection<Long> classIds) {
		iClassIds = classIds;
		return this;
	}

	
	public Collection<Long> getClassIds() { return iClassIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.info(getClassIds().size() + " class assignments changed.");
		OnlineSectioningLog.Enrollment.Builder previous = OnlineSectioningLog.Enrollment.newBuilder()
			.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
		OnlineSectioningLog.Enrollment.Builder stored = OnlineSectioningLog.Enrollment.newBuilder()
			.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
		for (Long classId: getClassIds()) {
			Class_ clazz = Class_DAO.getInstance().get(classId, helper.getHibSession());
			if (clazz == null) {
				helper.warn("Class " + classId + " wos deleted -- unsupported operation (use reload offering instead).");
				continue;
			}
			Lock lock = server.lockOffering(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId(),
					(List<Long>)helper.getHibSession().createQuery(
							"select e.student.uniqueId from StudentClassEnrollment e where "+
			                "e.clazz.uniqueId = :classId").setLong("classId", classId).list(),
			                true);
			try {
				helper.beginTransaction();
				try {
					XOffering offering = server.getOffering(clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getUniqueId());
					XSection oldSection = (offering == null ? null : offering.getSection(clazz.getUniqueId()));
					if (oldSection == null) {
						helper.warn("Class " + clazz.getClassLabel() + " was added -- unsupported operation (use reload offering instead).");
						continue;
					}
					previous.addSection(OnlineSectioningHelper.toProto(oldSection));
					helper.info("Reloading " + clazz.getClassLabel());
					XSection newSection = new XSection(clazz, helper);
					XSubpart subpart = offering.getSubpart(newSection.getSubpartId());
					
					subpart.getSections().remove(oldSection);
					subpart.getSections().add(newSection);
					Collections.sort(subpart.getSections());
					
					server.update(offering);
					
					stored.addSection(OnlineSectioningHelper.toProto(newSection));
				helper.commitTransaction();
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
		
		helper.getAction().addEnrollment(previous);
		helper.getAction().addEnrollment(stored);
		
		return true;
	}

	@Override
	public String name() {
		return "class-reassigned";
	}
	
}
