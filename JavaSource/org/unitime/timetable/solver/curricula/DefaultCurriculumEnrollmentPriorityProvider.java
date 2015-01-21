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
package org.unitime.timetable.solver.curricula;


import org.cpsolver.ifs.util.DataProperties;
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
