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
import java.util.Iterator;
import java.util.List;

import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Offering;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SectioningInfo;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction.DatabaseAction;

/**
 * @author Tomas Muller
 */
public class ReloadOfferingAction extends DatabaseAction<Boolean> {
	private List<Long> iOfferingIds;
	
	public ReloadOfferingAction(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
	}
	
	public ReloadOfferingAction(List<Long> offeringIds) {
		iOfferingIds = offeringIds;
	}
	
	public List<Long> getOfferingIds() { return iOfferingIds; }

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		for (Long offeringId: getOfferingIds()) {
			// Unload offering
			Offering oldOffering = server.getOffering(offeringId);
			List<Enrollment> oldEnrollments = new ArrayList<Enrollment>();
			if (oldOffering != null) {
				server.remove(oldOffering);
				for (Config oldConfig: oldOffering.getConfigs())
					for (Subpart oldSubpart: oldConfig.getSubparts())
						for (Section oldSection: oldSubpart.getSections()) {
							oldEnrollments.addAll(oldSection.getEnrollments());
						}
				for (Enrollment enrollment: oldEnrollments)
					enrollment.variable().unassign(0);
			}
			
			// Load offering
			InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId, helper.getHibSession());
			Offering newOffering = null;
			
			if (offering != null) {
				newOffering = ReloadAllData.loadOffering(offering, server, helper);
				server.update(newOffering);
				for (CourseOffering co: offering.getCourseOfferings())
					server.update(new CourseInfo(co));
				
				// Load sectioning info
	        	List<SectioningInfo> infos = helper.getHibSession().createQuery(
	        			"select i from SectioningInfo i where i.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :offeringId")
	        			.setLong("offeringId", offeringId).list();
	        	for (SectioningInfo info : infos) {
	        		Section section = server.getSection(info.getClazz().getUniqueId());
	        		if (section != null) {
	        			section.setSpaceExpected(info.getNbrExpectedStudents());
	        			section.setSpaceHeld(info.getNbrHoldingStudents());
	        		}
	        	}
	        	
			}
			
			// Transfer course requests
			if (oldOffering != null) {
				for (Enrollment enrollment: oldEnrollments) {
					for (Iterator<Request> i = enrollment.getStudent().getRequests().iterator(); i.hasNext();) {
						Request request = i.next();
						if (request instanceof CourseRequest) {
							CourseRequest cr = (CourseRequest)request;
							List<Course> newCourses = new ArrayList<Course>(); boolean changed = false;
							for (Course course: cr.getCourses())
								if (course.getOffering().equals(oldOffering)) {
									changed = true;
									if (newOffering != null)
										for (Course newCourse: newOffering.getCourses())
											if (newCourse.getId() == course.getId()) {
												newCourses.add(newCourse);
												break;
											}
									newCourses.add(course);
								}
							if (changed) {
								cr.getCourses().clear(); cr.getCourses().addAll(newCourses);
							}
							if (cr.getCourses().isEmpty())
								i.remove();
							if (cr.getAssignment() != null && cr.getAssignment().getCourse().getOffering().equals(oldOffering)) {
								// do it some other way
							}
						}
					}
				}
			}
			
			// Migrate enrollments
			if (newOffering != null && oldOffering == null) {
				for (Enrollment enrollment: oldEnrollments) {
					
				}
			}
		}
		
		return true;
	}
	
	@Override
    public String name() { return "reload-offering"; }
}
