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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

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
	
	public Collection<Long> getCourseIds(OnlineSectioningServer server) {
		Set<Long> courseIds = new HashSet<Long>();
		for (Long classId: getClassIds()) {
			Section section = server.getSection(classId);
			if (section != null) {
				for (Course course: section.getSubpart().getConfig().getOffering().getCourses()) {
					courseIds.add(course.getId());
				}
			}
		}
		return courseIds;			
	}

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
				Lock lock = server.lockClass(classId,
						(List<Long>)helper.getHibSession().createQuery(
								"select e.student.uniqueId from StudentClassEnrollment e where "+
				                "e.clazz.uniqueId = :classId").setLong("classId", classId).list());
				try {
					Section section = server.getSection(clazz.getUniqueId());
					if (section == null) {
						helper.warn("Class " + clazz.getClassLabel() + " was added -- unsupported operation (use reload offering instead).");
						continue;
					}
					previous.addSection(OnlineSectioningHelper.toProto(section));
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
	            		int roomLimit = (int) Math.floor(p.getRoomSize() / (clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()));
	            		// int roomLimit = Math.round((clazz.getRoomRatio() == null ? 1.0f : clazz.getRoomRatio()) * p.getRoomSize());
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

	                section.setName(clazz.getExternalUniqueId() == null ? clazz.getClassSuffix() == null ? clazz.getSectionNumberString(helper.getHibSession()) : clazz.getClassSuffix() : clazz.getExternalUniqueId());
					stored.addSection(OnlineSectioningHelper.toProto(section));
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
