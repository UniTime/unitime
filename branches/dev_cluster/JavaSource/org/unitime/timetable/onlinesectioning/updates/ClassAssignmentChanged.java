/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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

/**
 * @author Tomas Muller
 */
public class ClassAssignmentChanged implements OnlineSectioningAction<Boolean> {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private Collection<Long> iClassIds = null;
	
	public ClassAssignmentChanged(Long... classIds) {
		iClassIds = new ArrayList<Long>();
		for (Long classId: classIds)
			iClassIds.add(classId);
	}
	
	public ClassAssignmentChanged(Collection<Long> classIds) {
		iClassIds = classIds;
	}

	
	public Collection<Long> getClassIds() { return iClassIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.info(getClassIds().size() + " class assignments changed.");
		helper.beginTransaction();
		try {
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
				} finally {
					lock.release();
				}
			}
			
			helper.getAction().addEnrollment(previous);
			helper.getAction().addEnrollment(stored);
			
			helper.commitTransaction();
			return true;
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}		
	}

	@Override
	public String name() {
		return "class-reassigned";
	}
	
}
