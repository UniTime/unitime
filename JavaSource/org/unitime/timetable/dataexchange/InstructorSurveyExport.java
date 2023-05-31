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
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyExport extends PreferencesExport {
    protected static Formats.Format<Date> sTimestampFormat = Formats.getDateFormat("yyyy/M/d HH:mm:ss");
	
	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("instructorSurveys");
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("campus", session.getAcademicInitiative());
	        root.addAttribute("timeStampFormat", sTimestampFormat.toPattern());
	        root.addAttribute("created", new Date().toString());
	        
	        for (InstructorSurvey survey: getHibSession().createQuery(
	        		"from InstructorSurvey s where s.session.uniqueId = :sessionId",
	        		InstructorSurvey.class)
	        		.setParameter("sessionId", session.getUniqueId()).list()) {
				exportSurvey(root, survey);
			}
			
			commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected void exportSurvey(Element parent, InstructorSurvey survey) {
		Element el = parent.addElement("survey");
		el.addAttribute("externalId", survey.getExternalUniqueId());
		if (survey.getEmail() != null && !survey.getEmail().isEmpty())
			el.addAttribute("email", survey.getEmail());
		if (survey.getNote() != null && !survey.getNote().isEmpty())
			el.addElement("note")
				.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve")
				.setText(survey.getNote());
		if (survey.getSubmitted() != null)
			el.addAttribute("submitted", sTimestampFormat.format(survey.getSubmitted()));
		if (survey.getChanged() != null)
			el.addAttribute("changed", sTimestampFormat.format(survey.getChanged()));
		if (survey.getChangedBy() != null && !survey.getChangedBy().isEmpty())
			el.addAttribute("changedBy", survey.getChangedBy());
		if (survey.getApplied() != null)
			el.addAttribute("applied", sTimestampFormat.format(survey.getApplied()));
		if (survey.getAppliedDeptCode() != null && !survey.getAppliedDeptCode().isEmpty())
			el.addAttribute("appliedDeptCode", survey.getAppliedDeptCode());
		for (Preference preference: survey.getPreferences())
			exportPreference(el, preference);
		for (InstructorCourseRequirement req: survey.getCourseRequirements())
			exportCourseRequirement(el, req);
	}
	
	protected void exportCourseRequirement(Element parent, InstructorCourseRequirement req) {
		Element el = parent.addElement("courseReq");
		if (req.getCourseOffering() != null)
			el.addAttribute("course", req.getCourseOffering().getCourseName());
		else
			el.addAttribute("course", req.getCourse());
		for (InstructorCourseRequirementNote note: req.getNotes())
			if (note.getNote() != null && !note.getNote().isEmpty()) {
				Element noteEl = el.addElement("note");
				noteEl.addAttribute("type", note.getType().getReference());
				noteEl.setText(note.getNote());
				noteEl.addAttribute(QName.get("space", Namespace.XML_NAMESPACE), "preserve");
			}
	}
}
