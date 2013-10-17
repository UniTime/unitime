/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;

/**
 * @author Tomas Muller
 */
public class DefaultCurriculumEnrollmentPriorityProvider implements CurriculumEnrollmentPriorityProvider {
	private float iThreshold = 0.95f;
	private String iGroupMatch = null;
	
	public DefaultCurriculumEnrollmentPriorityProvider(DataProperties config) {
		iThreshold = config.getPropertyFloat("CurriculumEnrollmentPriority.Threshold", iThreshold);
		iGroupMatch = config.getProperty("CurriculumEnrollmentPriority.GroupMatch");
	}

	@Override
	public Double getEnrollmentPriority(CurriculumCourse course) {
		if (course.getPercShare() >= iThreshold) return 1.0; // required course -- important
		for (CurriculumCourseGroup group: course.getGroups()) {
			if (iGroupMatch != null && !iGroupMatch.isEmpty() && group.getName().matches(iGroupMatch)) {
				return 1.0; // matching group name
			}
			if (group.getType() == 0) { // optional group
				float totalShare = 0.0f;
				for (CurriculumCourse other: course.getClassification().getCourses())
					if (other.getGroups().contains(group)) totalShare += other.getPercShare();
				if (totalShare >= iThreshold) return 1.0; // it is required to take one of the courses of the group
			} else if (group.getType() == 1) {
				for (CurriculumCourse other: course.getClassification().getCourses())
					if (other.getGroups().contains(group) && other.getPercShare() >= iThreshold)
						return 1.0; // it is required to take courses from this group
			}
		}
		return null;
	}

}
