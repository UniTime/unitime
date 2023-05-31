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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.InstructorSurvey;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public class InstructorSurveyImport extends PreferencesImport {
	private SimpleDateFormat iTimestampFormat;
	
	public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("instructorSurveys")) {
        	throw new Exception("Given XML file is not an instructor surveys file.");
        }
        try {
            beginTransaction();
            
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            iTimestampFormat = new SimpleDateFormat(root.attributeValue("timeStampFormat", "yyyy/M/d HH:mm:ss"), Locale.US);
            iSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (iSession == null)
                throw new Exception("No session found for the given campus, year, and term.");
            
            Map<String, InstructorCourseRequirementType> types = new HashMap<String, InstructorCourseRequirementType>();
            for (InstructorCourseRequirementType type: getHibSession().createQuery("from InstructorCourseRequirementType", InstructorCourseRequirementType.class).list()) {
            	types.put(type.getReference(), type);
            }

            for (Iterator i = root.elementIterator("survey"); i.hasNext(); ) {
            	Element element = (Element)i.next();
            	String externalUniqueId = element.attributeValue("externalId");
            	InstructorSurvey survey = getHibSession().createQuery(
        				"from InstructorSurvey where session.uniqueId = :sessionId and externalUniqueId = :externalId", InstructorSurvey.class
        				).setParameter("sessionId", iSession.getUniqueId())
        				.setParameter("externalId", externalUniqueId)
        				.setMaxResults(1).uniqueResult();
            	if (survey == null) {
            		survey = new InstructorSurvey();
            		survey.setExternalUniqueId(externalUniqueId);
            		survey.setPreferences(new HashSet<Preference>());
            		survey.setCourseRequirements(new HashSet<InstructorCourseRequirement>());
            	} else {
            		survey.getPreferences().clear();
            		for (InstructorCourseRequirement req: survey.getCourseRequirements())
            			getHibSession().remove(req);
            		survey.getCourseRequirements().clear();
            	}
            	survey.setEmail(element.attributeValue("email"));
            	survey.setNote(getNote(element));
            	survey.setChangedBy(element.attributeValue("changedBy"));
            	survey.setAppliedDeptCode(element.attributeValue("appliedDeptCode"));
            	survey.setSubmitted(toDate(element.attributeValue("submitted")));
            	survey.setChanged(toDate(element.attributeValue("changed")));
            	survey.setApplied(toDate(element.attributeValue("applied")));
            	
            	if (survey.getUniqueId() == null)
            		getHibSession().persist(survey);
            	else
            		getHibSession().merge(survey);
            	
            	for (Iterator j = element.elementIterator(); j.hasNext(); ) {
                	Element prefElement = (Element)j.next();
                	if ("courseReq".equals(prefElement.getName())) {
                		InstructorCourseRequirement req = createCourseReq(prefElement, types);
                		if (req == null || req.getNotes().isEmpty()) continue;
                		req.setInstructorSurvey(survey);
                		survey.getCourseRequirements().add(req);
                		getHibSession().persist(req);
                	} else {
                		Preference preference = createPreference(prefElement, survey);
                		if (preference == null) continue;
                		preference.setOwner(survey);
                		survey.getPreferences().add(preference);
                		getHibSession().persist(preference);
                	}
                }
            	
            }
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
	
	protected InstructorCourseRequirement createCourseReq(Element element, Map<String, InstructorCourseRequirementType> types) {
		InstructorCourseRequirement req = new InstructorCourseRequirement();
		req.setCourse(element.attributeValue("course"));
		req.setCourseOffering(
				getHibSession().createQuery(
						"from CourseOffering where instructionalOffering.session.uniqueId = :sessionId and " +
						"(subjectArea.subjectAreaAbbreviation || ' ' || courseNbr) = :course",
						CourseOffering.class)
						.setParameter("sessionId", iSession.getUniqueId())
						.setParameter("course", req.getCourse())
						.setMaxResults(1).uniqueResult());
		req.setNotes(new HashSet<InstructorCourseRequirementNote>());
		for (Element noteEl: element.elements("note")) {
			InstructorCourseRequirementType type = types.get(noteEl.attributeValue("type"));
			if (type != null) {
				InstructorCourseRequirementNote n = new InstructorCourseRequirementNote();
				n.setType(type);
				n.setRequirement(req);
				n.setNote(noteEl.getText());
				req.getNotes().add(n);
			}
		}
		return req;
	}
	
	protected Date toDate(String date) {
		if (date == null || date.isEmpty()) return null;
		try {
			return iTimestampFormat.parse(date);
		} catch (ParseException e) {
			warn("Failed to parse date: " + date, e);
			return null;
		}
	}
}
