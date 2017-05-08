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
package org.unitime.timetable.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.base.BaseTeachingResponsibility;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;

public class TeachingResponsibility extends BaseTeachingResponsibility {
	private static final long serialVersionUID = 1L;
	
	public static enum Option {
		auxiliary("Do not report"),
		noexport("Do not export"),
		noevents("Do not show in events"),
		isdefault("Default responsibility"),
		;
		
		private String iName;
		
		Option(String name) {
			iName = name;
		}
		
		public String getName() { return iName; }
		
		public int toggle() { return 1 << ordinal(); }
	}

	public TeachingResponsibility() {
		super();
	}
	
	public static List<TeachingResponsibility> getInstructorTeachingResponsibilities() {
		return (List<TeachingResponsibility>)TeachingResponsibilityDAO.getInstance().getSession().createQuery(
				"from TeachingResponsibility where instructor = true order by label"
				).setCacheable(true).list();
    }
	
	public static TeachingResponsibility getDefaultInstructorTeachingResponsibility() {
		for (TeachingResponsibility r: getInstructorTeachingResponsibilities())
			if (r.hasOption(Option.isdefault)) return r;
		return null;
	}
	
	public static List<TeachingResponsibility> getCoordinatorTeachingResponsibilities() {
		return (List<TeachingResponsibility>)TeachingResponsibilityDAO.getInstance().getSession().createQuery(
				"from TeachingResponsibility where coordinator = true order by label"
				).setCacheable(true).list();
    }
	
	public static TeachingResponsibility getDefaultCoordinatorTeachingResponsibility() {
		for (TeachingResponsibility r: getCoordinatorTeachingResponsibilities())
			if (r.hasOption(Option.isdefault)) return r;
		return null;
	}
    
	public static TeachingResponsibility getTeachingResponsibility(String reference, org.hibernate.Session hibSession) {
		if (reference == null || reference.isEmpty()) return null;
		return (TeachingResponsibility)hibSession.createQuery(
				"from TeachingResponsibility where reference = :reference")
				.setString("reference", reference).setMaxResults(1).setCacheable(true).uniqueResult();
	}
	
	public boolean hasOption(Option option) {
		return getOptions() != null && (getOptions() & option.toggle()) != 0;
	}
	
	public void addOption(Option option) {
		if (!hasOption(option)) setOptions((getOptions() == null ? 0 : getOptions()) + option.toggle());
	}

	public void removeOption(Option option) {
		if (hasOption(option)) setOptions(getOptions() - option.toggle());
	}

	public static boolean hasOption(Option option, String reference, org.hibernate.Session hibSession) {
		TeachingResponsibility responsibility = getTeachingResponsibility(reference, hibSession);
		return responsibility != null && responsibility.hasOption(option);
	}
	
	public static Set<String> getMatchingResponsibilities(Option option) {
		org.hibernate.Session hibSession = TeachingResponsibilityDAO.getInstance().createNewSession();
		try {
			Set<String> responsibilities = new HashSet<String>();
			for (TeachingResponsibility responsibility: TeachingResponsibilityDAO.getInstance().findAll(hibSession)) {
				if (responsibility.hasOption(option))
					responsibilities.add(responsibility.getReference());
			}
			return responsibilities;
		} finally {
			hibSession.close();
		}
	}
	
	public boolean isUsed() {
		if (((Number)TeachingResponsibilityDAO.getInstance().getSession().createQuery("select count(ci) from ClassInstructor ci where ci.responsibility.uniqueId = :responsibilityId")
			.setLong("responsibilityId", getUniqueId()).uniqueResult()).intValue() > 0) return true;
		if (((Number)TeachingResponsibilityDAO.getInstance().getSession().createQuery("select count(oc) from OfferingCoordinator oc where oc.responsibility.uniqueId = :responsibilityId")
				.setLong("responsibilityId", getUniqueId()).uniqueResult()).intValue() > 0) return true;
		return false;
	}
}
