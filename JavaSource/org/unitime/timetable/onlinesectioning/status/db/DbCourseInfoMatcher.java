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
package org.unitime.timetable.onlinesectioning.status.db;

import java.io.Serializable;
import java.util.Set;

import org.unitime.timetable.gwt.server.Query.AmbigousTermMatcher;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.onlinesectioning.status.CourseLookup;

public class DbCourseInfoMatcher implements AmbigousTermMatcher, Serializable {
	private static final long serialVersionUID = 1L;
	private CourseOffering iCourse;
	private boolean iConsentToDoCourse;
	private CourseLookup iLookup;
	
	public DbCourseInfoMatcher(CourseOffering course, boolean isConsentToDoCourse, CourseLookup lookup) {
		iCourse = course;
		iConsentToDoCourse = isConsentToDoCourse;
		iLookup = lookup;
	}
	
	public CourseOffering course() { return iCourse; }
	
	public boolean isConsentToDoCourse() { return iConsentToDoCourse; }
	
	@Override
	public Boolean match(String attr, String term) {
		if (term.isEmpty()) return true;
		if ("limit".equals(attr)) return true;
		if ("lookup".equals(attr)) {
			Set<Long> courseIds = iLookup.getCourses(term);
			return (courseIds != null && courseIds.contains(course().getUniqueId()));
		}
		if (attr == null || "name".equals(attr) || "course".equals(attr)) {
			return course().getSubjectAreaAbbv().equalsIgnoreCase(term) || course().getCourseNbr().equalsIgnoreCase(term) || (course().getSubjectAreaAbbv() + " " + course().getCourseNbr()).equalsIgnoreCase(term);
		}
		if ((attr == null && term.length() > 2) || "title".equals(attr)) {
			return (course().getTitle() == null ? "" : course().getTitle()).toLowerCase().contains(term.toLowerCase());
		}
		if (attr == null || "subject".equals(attr)) {
			return course().getSubjectAreaAbbv().equalsIgnoreCase(term);
		}
		if (attr == null || "number".equals(attr)) {
			return course().getCourseNbr().equalsIgnoreCase(term);
		}
		if ("department".equals(attr)) {
			return (course().getSubjectArea().getDepartment().getDeptCode() == null ? course().getSubjectArea().getDepartment().getAbbreviation() : course().getSubjectArea().getDepartment().getDeptCode()).equalsIgnoreCase(term);
			
		}
		if ("consent".equals(attr)) {
			if ("none".equalsIgnoreCase(term) || "No Consent".equalsIgnoreCase(term))
				return course().getConsentType() == null;
			else if ("todo".equalsIgnoreCase(term) || "To Do".equalsIgnoreCase(term))
				return isConsentToDoCourse();
			else
				return course().getConsentType() != null;
		}
		if ("mode".equals(attr)) {
			return true;
		}
		if ("registered".equals(attr)) {
			if ("true".equalsIgnoreCase(term) || "1".equalsIgnoreCase(term))
				return true;
			else
				return false;
		}
		if ("assignment".equals(attr)) {
			if ("Wait-Listed".equals(term)) {
				return course().getInstructionalOffering().effectiveWaitList();
			} else {
				return true;
			}
		}
		if ("im".equals(attr)) {
			for (InstrOfferingConfig config: course().getInstructionalOffering().getInstrOfferingConfigs()) {
				InstructionalMethod im = config.getEffectiveInstructionalMethod();
				if (im != null && term.equals(im.getReference()))
					return true;
			}
			return false;
		}
		return null; // pass unknown attributes lower
	}
}