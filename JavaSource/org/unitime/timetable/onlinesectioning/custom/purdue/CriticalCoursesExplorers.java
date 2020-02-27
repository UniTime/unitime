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
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action.Builder;
import org.unitime.timetable.onlinesectioning.model.XAreaClassificationMajor;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XStudentId;
import org.unitime.timetable.onlinesectioning.model.XStudent.XGroup;

/**
 * @author Tomas Muller
 */
public class CriticalCoursesExplorers extends CriticalCoursesQuery {
	protected DegreeWorksCourseRequests iDGW;
	
	public CriticalCoursesExplorers() throws ServletException, IOException {
		iDGW = new DegreeWorksCourseRequests();
	}
	
	protected boolean isFallBackToDegreeWorks() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.unex.useDgwFallback", "true"));
	}
	
	protected String getGroupType() {
		return ApplicationProperties.getProperty("banner.unex.groupType", "1st Choice");
	}
	
	@Override
	protected List<XAreaClassificationMajor> getAreaClasfMajors(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		List<XAreaClassificationMajor> ret = (isFallBackToDegreeWorks() ? new ArrayList<XAreaClassificationMajor>() : null);
		String gType = getGroupType();
		for (XGroup g: student.getGroups()) {
			if (gType.equals(g.getType()) && g.getAbbreviation().contains("-")) {
				String area = g.getAbbreviation().substring(0, g.getAbbreviation().indexOf('-'));
				String major = g.getAbbreviation().substring(g.getAbbreviation().indexOf('-') + 1);
				if (ret == null) ret = new ArrayList<XAreaClassificationMajor>(student.getMajors());
				ret.add(new XAreaClassificationMajor(area, "01", major));
			}
		}
		return ret != null ? ret : student.getMajors();
	}
	
	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId student) {
		return getCriticalCourses(server, helper, student, helper.getAction());
	}

	@Override
	public CriticalCourses getCriticalCourses(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudentId studentId, Builder action) {
		if (isFallBackToDegreeWorks()) {
			CriticalCourses critQuery = super.getCriticalCourses(server, helper, studentId, action);
			if (critQuery != null && !critQuery.isEmpty()) {
				CriticalCourses critDgw = iDGW.getCriticalCourses(server, helper, studentId, action);
				if (critDgw == null || critDgw.isEmpty()) return critQuery;
				return new CombinedCriticals(critDgw, critQuery);
			} else {
				return iDGW.getCriticalCourses(server, helper, studentId, action);
			}
		} else {
			return super.getCriticalCourses(server, helper, studentId, action);
		}
	}
	
	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) throws SectioningException {
		if (isFallBackToDegreeWorks()) {
			List<DegreePlanInterface> plans = super.getDegreePlans(server, helper, student);
			if (plans == null || plans.isEmpty())
				return iDGW.getDegreePlans(server, helper, student);
			else {
				List<DegreePlanInterface> dgw = iDGW.getDegreePlans(server, helper, student);
				if (dgw != null) plans.addAll(dgw);
				return plans;
			}
		} else {
			return super.getDegreePlans(server, helper, student);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		iDGW.dispose();
	}
	
	public static class CombinedCriticals implements CriticalCourses {
		private CriticalCourses iPrimary, iSecondary;
		
		public CombinedCriticals(CriticalCourses primary, CriticalCourses secondary) {
			iPrimary = primary;
			iSecondary = secondary;
		}

		@Override
		public boolean isEmpty() {
			return iPrimary.isEmpty() && iSecondary.isEmpty();
		}

		@Override
		public int isCritical(CourseOffering course) {
			int crit = iPrimary.isCritical(course);
			if (crit > 0) return crit;
			return iSecondary.isCritical(course);
		}

		@Override
		public int isCritical(XCourseId course) {
			int crit = iPrimary.isCritical(course);
			if (crit > 0) return crit;
			return iSecondary.isCritical(course);
		}
		
	}
}
