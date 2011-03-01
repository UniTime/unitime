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
import java.util.Iterator;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction.DatabaseAction;

/**
 * @author Tomas Muller
 */
public class ClassAssignmentChanged extends DatabaseAction<Boolean> {
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

		for (Long classId: getClassIds()) {
			Section section = server.getSection(classId);
			Class_ clazz = Class_DAO.getInstance().get(classId, helper.getHibSession());
			if (section != null && clazz != null) {
				// class updated
				helper.info("Reloading " + clazz.getClassLabel());
                org.unitime.timetable.model.Assignment a = clazz.getCommittedAssignment();
                Placement p = (a == null ? null : a.getPlacement());
                if (p != null && p.getTimeLocation() != null) {
                	p.getTimeLocation().setDatePattern(
                			p.getTimeLocation().getDatePatternId(),
                			ReloadAllData.datePatternName(p.getTimeLocation(), server.getAcademicSession()),
                			p.getTimeLocation().getWeekCode());
                }
                section.setPlacement(p);
				helper.info("  -- placement: " + p);

                int minLimit = clazz.getExpectedCapacity();
            	int maxLimit = clazz.getMaxExpectedCapacity();
            	int limit = maxLimit;
            	if (minLimit < maxLimit && p != null) {
            		int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
            		limit = Math.min(Math.max(minLimit, roomLimit), maxLimit);
            	}
                if (clazz.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment() || limit >= 9999) limit = -1;
                section.setLimit(limit);
				helper.info("  -- limit: " + limit);

                String instructorIds = "";
                String instructorNames = "";
                for (Iterator<ClassInstructor> k = clazz.getClassInstructors().iterator(); k.hasNext(); ) {
                	ClassInstructor ci = k.next();
                	if (!ci.isLead()) continue;
                	if (!instructorIds.isEmpty()) {
                		instructorIds += ":"; instructorNames += ":";
                	}
                	instructorIds += ci.getInstructor().getUniqueId().toString();
                	instructorNames += ci.getInstructor().getName(DepartmentalInstructor.sNameFormatShort) + "|"  + (ci.getInstructor().getEmail() == null ? "" : ci.getInstructor().getEmail());
                }
                section.getChoice().setInstructor(instructorIds, instructorNames);
				helper.info("  -- instructor: " + instructorNames);

                section.setName(clazz.getExternalUniqueId() == null ? clazz.getClassSuffix() : clazz.getExternalUniqueId());
			} else {
				// class added or removed
				helper.warn((section == null ? "Adding" : "Deleting") + " " + clazz.getClassLabel() + " not supported.");
			}
		}
		
		return true;
	}

	@Override
	public String name() {
		return "class-reassigned";
	}
}
