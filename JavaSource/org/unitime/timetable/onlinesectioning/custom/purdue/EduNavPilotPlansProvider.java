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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.DegreePlansProvider;
import org.unitime.timetable.onlinesectioning.match.CourseMatcher;
import org.unitime.timetable.onlinesectioning.model.XStudent;

public class EduNavPilotPlansProvider implements DegreePlansProvider {
	private static Log sLog = LogFactory.getLog(EduNavPilotPlansProvider.class);
	
	private DegreePlansProvider iEduNav;
	private DegreePlansProvider iDGW;
	private DegreePlansProvider iFallback;
	
	public EduNavPilotPlansProvider() {
		iEduNav = new EduNavPlansProvider();
		iDGW = new DegreeWorksCourseRequests();
		iFallback = iDGW;
		try {
			String clazz = getEduNavFallbackProvider();
			if (clazz != null && !clazz.isEmpty()) {
				iFallback = (CriticalCoursesExplorers)Class.forName(clazz).getConstructor().newInstance();
			}
		} catch (Exception e) {
			sLog.error("Failed to create fallback degree plan provider.");
		}
	}
	
	protected String getEduNavFallbackProvider() {
		return ApplicationProperties.getProperty("edunav.pilot.fallbackProvider");
	}
	
	protected String getCriticalPlaceholdersSQL() {
		return ApplicationProperties.getProperty("edunav.pilotSQL", "select count(*) from timetable.szvensa where szvensa_id = :externalId");
	}
	
	protected boolean isMarkEduNavPlanActive() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("edunav.pilot.markActive", "true"));
	}
	
	protected boolean isMarkAllEduNavPlansActive() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("edunav.pilot.markAllActive", "false"));
	}
	
	protected boolean getDegreeWorksActiveOnly() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.dgw.activeOnly", "true"));
	}
	
	protected String getBannerId(XStudent student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}

	protected boolean isStudentInPilot(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student) {
		org.hibernate.Query query = helper.getHibSession().createSQLQuery(getCriticalPlaceholdersSQL());
		query.setString("externalId", getBannerId(student));
		return ((Number)query.uniqueResult()).intValue() > 0;
	}

	@Override
	public List<DegreePlanInterface> getDegreePlans(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, CourseMatcher matcher) throws SectioningException {
		if (isStudentInPilot(server, helper, student)) {
			boolean activeOnly = getDegreeWorksActiveOnly();
			List<DegreePlanInterface> plans = new ArrayList<DegreePlanInterface>();
			
			List<DegreePlanInterface> eduNavPlans = null;
			SectioningException eduNavErr = null;
			try {
				eduNavPlans = iEduNav.getDegreePlans(server, helper, student, matcher);
			} catch (SectioningException e) {
				eduNavErr = e;
			}
			List<DegreePlanInterface> dgwPlans = null;
			SectioningException dgwErr = null;
			try {
				dgwPlans = iDGW.getDegreePlans(server, helper, student, matcher);
			} catch (SectioningException e) {
				dgwErr = e;	
			}
			if (eduNavPlans != null) {
				for (DegreePlanInterface plan: eduNavPlans) {
					plan.setName("EduNav: " + plan.getName());
					plans.add(plan);
				}
				if (!activeOnly && dgwPlans != null && !dgwPlans.isEmpty()) {
					if (plans.size() == 1 && isMarkEduNavPlanActive()) plans.get(0).setActive(true);
					if (plans.size() > 1 && isMarkAllEduNavPlansActive())
						for (DegreePlanInterface plan: plans)
							plan.setActive(true);
				}
			}
			if (dgwPlans != null) {
				for (DegreePlanInterface plan: dgwPlans) {
					plan.setName("DegreeWorks: " + plan.getName());
					if (activeOnly) plan.setActive(false);
					plans.add(plan);
				}
			}
			if (!plans.isEmpty())
				return plans;
			if (eduNavErr != null)
				throw eduNavErr;
			if (dgwErr != null)
				throw dgwErr;
			return plans;
		} else {
			return iFallback.getDegreePlans(server, helper, student, matcher);
		}
	}

	@Override
	public void dispose() {
		iEduNav.dispose();
		iDGW.dispose();
	}
}
