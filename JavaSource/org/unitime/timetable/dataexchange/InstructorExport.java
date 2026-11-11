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
package org.unitime.timetable.dataexchange;

import java.util.Date;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.DateUtils;

public class InstructorExport extends PreferencesExport {
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("instructors");
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("created", new Date().toString());
	        root.addAttribute("dateFormat", sDateFormat.toPattern());
	        boolean includePrefs = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.preferences", "true")));
	        root.addAttribute("incremental", "false");
	        root.addAttribute("preferences", includePrefs ? "true" : "false");
	        
	        for (DepartmentalInstructor di: getHibSession().createQuery(
	        		"from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId " +
	        		"order by di.department.deptCode, di.lastName, di.firstName", DepartmentalInstructor.class)
	        		.setParameter("sessionId", session.getUniqueId()).list()) {
	        	exportInstructor(root, di, includePrefs);
	        }
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected void exportInstructor(Element root, DepartmentalInstructor di, boolean includePrefs) {
		Element e = root.addElement("instructor");
		e.addAttribute("department", di.getDepartment().getDeptCode());
		if (di.getExternalUniqueId() != null)
			e.addAttribute("externalId", di.getExternalUniqueId());
		if (di.getLastName() != null)
			e.addAttribute("lastName", di.getLastName());
		if (di.getMiddleName() != null)
			e.addAttribute("middleName", di.getMiddleName());
		if (di.getFirstName() != null)
			e.addAttribute("firstName", di.getFirstName());
		if (di.getAcademicTitle() != null)
			e.addAttribute("title", di.getAcademicTitle());
		if (di.getEmail() != null)
			e.addAttribute("email", di.getEmail());
		if (di.getCareerAcct() != null)
			e.addAttribute("careerAcct", di.getCareerAcct());
		if (di.getNote() != null && !di.getNote().isEmpty())
			e.addElement("note").add(new DOMCDATA(di.getNote()));
		if (Boolean.TRUE.equals(di.getIgnoreToFar()))
			e.addAttribute("ignoreTooFar", "true");
		if (di.getPositionType() != null)
			e.addAttribute("position", di.getPositionType().getReference());
		if (di.getRole() != null)
			e.addAttribute("role", di.getRole().getReference());
		for (InstructorAttribute a: di.getAttributes()) {
			Element ae = e.addElement("attribute");
			ae.addAttribute("code", a.getCode());
			if (a.getName() != null)
				ae.addAttribute("name", a.getName());
			if (a.getType() != null)
				ae.addAttribute("type", a.getType().getReference());
			if (a.getParentAttribute() != null)
				ae.addAttribute("parent", a.getParentAttribute().getCode());
			if (a.getDepartment() != null)
				ae.addAttribute("department", a.getDepartment().getDeptCode());
		}
		if (includePrefs) {
			if (di.getMaxLoad() != null && di.getTeachingPreference() != null)
				e.addElement("teachingPref").addAttribute("maxLoad", di.getMaxLoad().toString())
				.addAttribute("level", di.getTeachingPreference().getPrefProlog());
			if (di.hasUnavailabilities()) {
				Element unavEl = e.addElement("unavailabilities");
				int startMonth = di.getDepartment().getSession().getPatternStartMonth();
				int endMonth = di.getDepartment().getSession().getPatternEndMonth();
				int year = di.getDepartment().getSession().getSessionStartYear();
				Date firstDate = null;
				Date yesterday = null;
				for (int m = startMonth; m <= endMonth; m++) {
					int daysOfMonth = DateUtils.getNrDaysOfMonth(m, year);
					for (int d = 1; d <= daysOfMonth; d++) {
						Date today = DateUtils.getDate(d, m, year);
						if (di.isUnavailable(d, m)) {
							if (firstDate == null) firstDate = today;
						} else {
							if (firstDate != null) {
								if (firstDate.equals(yesterday))
									unavEl.addElement("dates").addAttribute("date", sDateFormat.format(firstDate));
								else
									unavEl.addElement("dates").addAttribute("fromDate", sDateFormat.format(firstDate)).addAttribute("toDate", sDateFormat.format(yesterday));
								firstDate = null;
							}
						}
						yesterday = today;
					}
				}
				if (firstDate != null) {
					if (firstDate.equals(yesterday))
						unavEl.addElement("dates").addAttribute("date", sDateFormat.format(firstDate));
					else
						unavEl.addElement("dates").addAttribute("fromDate", sDateFormat.format(firstDate)).addAttribute("toDate", sDateFormat.format(yesterday));
				}
			}
			for (Preference preference: di.getPreferences())
				exportPreference(e, preference);
		}
	}

}
