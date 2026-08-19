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
package org.unitime.timetable.onlinesectioning.status;

import java.io.Serializable;
import java.util.Set;

import org.unitime.timetable.gwt.server.Query.AmbigousTermMatcher;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XOffering;

public class CourseInfoMatcher implements AmbigousTermMatcher, Serializable {
	private static final long serialVersionUID = 1L;
	private XCourse iInfo;
	private boolean iConsentToDoCourse;
	private CourseLookup iLookup;
	private OnlineSectioningServer iServer;
	
	public CourseInfoMatcher(XCourse course, boolean isConsentToDoCourse, CourseLookup lookup, OnlineSectioningServer server) {
		iInfo = course;
		iConsentToDoCourse = isConsentToDoCourse;
		iLookup = lookup;
		iServer = server;
	}
	
	public XCourse info() { return iInfo; }
	
	public boolean isConsentToDoCourse() { return iConsentToDoCourse; }
	
	public OnlineSectioningServer server() { return iServer; }
	
	@Override
	public Boolean match(String attr, String term) {
		if (term.isEmpty()) return true;
		if ("limit".equals(attr)) return true;
		if ("lookup".equals(attr)) {
			Set<Long> courseIds = iLookup.getCourses(term);
			return (courseIds != null && courseIds.contains(info().getCourseId()));
		}
		if (attr == null || "name".equals(attr) || "course".equals(attr)) {
			return info().getSubjectArea().equalsIgnoreCase(term) || info().getCourseNumber().equalsIgnoreCase(term) || (info().getSubjectArea() + " " + info().getCourseNumber()).equalsIgnoreCase(term);
		}
		if ((attr == null && term.length() > 2) || "title".equals(attr)) {
			return info().getTitle().toLowerCase().contains(term.toLowerCase());
		}
		if (attr == null || "subject".equals(attr)) {
			return info().getSubjectArea().equalsIgnoreCase(term);
		}
		if (attr == null || "number".equals(attr)) {
			return info().getCourseNumber().equalsIgnoreCase(term);
		}
		if ("department".equals(attr)) {
			return info().getDepartment().equalsIgnoreCase(term);
			
		}
		if ("consent".equals(attr)) {
			if ("none".equalsIgnoreCase(term) || "No Consent".equalsIgnoreCase(term))
				return info().getConsentLabel() == null;
			else if ("todo".equalsIgnoreCase(term) || "To Do".equalsIgnoreCase(term))
				return isConsentToDoCourse();
			else
				return info().getConsentLabel() != null;
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
				XOffering offering = server().getOffering(info().getOfferingId());
				return offering != null && offering.isWaitList();
			} else {
				return null;
			}
		}
		if ("im".equals(attr)) {
			XOffering offering = server().getOffering(info().getOfferingId());
			if (offering != null)
				for (XConfig config: offering.getConfigs()) {
					if (config.getInstructionalMethod() == null && term.equals(server().getAcademicSession().getDefaultInstructionalMethod()))
						return true;
					if (config.getInstructionalMethod() != null && term.equals(config.getInstructionalMethod().getReference()))
						return true;
				}
			return false;
		}
		return null; // pass unknown attributes lower
	}
}