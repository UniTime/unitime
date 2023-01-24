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
package org.unitime.timetable.server.instructor.survey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CourseRequirement;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.CustomField;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorRequirementData;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorRequirementsRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorCourseRequirement;
import org.unitime.timetable.model.InstructorCourseRequirementNote;
import org.unitime.timetable.model.InstructorCourseRequirementType;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.InstructorCourseRequirementTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorRequirementsRequest.class)
public class InstructorRequirementsBackend implements GwtRpcImplementation<InstructorRequirementsRequest, InstructorRequirementData>{

	@Override
	public InstructorRequirementData execute(InstructorRequirementsRequest request, SessionContext context) {
		context.checkPermission(request.getOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);
		
		InstructionalOffering io = InstructionalOfferingDAO.getInstance().get(request.getOfferingId());
		
		InstructorRequirementData response = new InstructorRequirementData();
		response.setCrossList(io.getCourseOfferings().size() > 1);
		response.setAdmin(context.hasPermission(io.getDepartment(), Right.InstructorSurveyAdmin));
		List<InstructorCourseRequirementType> types = (List<InstructorCourseRequirementType>)InstructorCourseRequirementTypeDAO.getInstance().getSession().createQuery(
				"from InstructorCourseRequirementType order by sortOrder").list();
		Map<Long, CustomField> customFields = new HashMap<Long, CustomField>();
		for (InstructorCourseRequirementType type: types) {
			CustomField cf = new CustomField(type.getUniqueId(), type.getReference(), type.getLength());
			customFields.put(type.getUniqueId(), cf);
			response.addCustomField(cf);
		}
		
		
		List<InstructorCourseRequirement> requirements = InstructorCourseRequirement.getRequirementsForOffering(io);
		if (!requirements.isEmpty()) {
			String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
			boolean timeVertical = false;//RequiredTimeTable.getTimeGridVertical(context.getUser());
			boolean gridAsText = false;//RequiredTimeTable.getTimeGridAsText(context.getUser());
			for (InstructorCourseRequirement req: new TreeSet<InstructorCourseRequirement>(requirements)) {
        		DepartmentalInstructor di = req.getInstructorSurvey().getInstructor(io);
        		CourseRequirement line  = new CourseRequirement();
        		line.setExternalId(req.getInstructorSurvey().getExternalUniqueId());
        		if (di != null)
        			line.setInstructorId(di.getUniqueId());
        		line.setInstructorName(req.getInstructorSurvey().getExternalUniqueId());
        		if (di != null)
        			line.setInstructorName(di.getName(instructorNameFormat));
        		
        		line.setCourseName(req.getCourseOffering() == null ? req.getCourse() : req.getCourseOffering().getCourseName());
        		line.setId(req.getCourseOffering() != null ? req.getCourseOffering().getUniqueId() : null);
        		line.setCourseTitle(req.getCourseOffering() != null ? req.getCourseOffering().getTitle() : null);
        		for (InstructorCourseRequirementNote note: req.getNotes())
        			line.setCustomField(note.getType().getUniqueId(), note.getNote());
        		
        		line.setNote(req.getInstructorSurvey().getNote());

        		for (Preference p: req.getInstructorSurvey().getPreferences()) {
        			if (p instanceof TimePref) {
        				TimePref tp = (TimePref)p;
						RequiredTimeTable rtt = tp.getRequiredTimeTable();
						if (gridAsText) {
							line.setTimeHtml(rtt.getModel().toString().replaceAll(", ","\n"));
						} else {
							((TimePatternModel)rtt.getModel()).setMode("|" + propertyValue(io.getDepartment(), ApplicationProperty.InstructorSurveyTimePreferencesDept, ApplicationProperty.InstructorSurveyTimePreferences));
							line.setTimeHtml("<img border='0' " +
									"onmouseover=\"showGwtInstructorAvailabilityHint(this, 'IS#" + req.getInstructorSurvey().getUniqueId() + "');\" onmouseout=\"hideGwtInstructorAvailabilityHint();\" " +
									"src='pattern?v=" + (timeVertical ? 1 : 0) + "&d=" + io.getDepartment().getUniqueId() + "&p=" + rtt.getModel().getPreferences() + "' title='"+rtt.getModel().toString()+"' >&nbsp;"
									);
						}
						//line.setTime(rtt.getModel().toString());
        			} else if (p instanceof DistributionPref) {
        				line.addDist(p.preferenceText(instructorNameFormat));
        				line.addDistHtml(p.preferenceHtml(instructorNameFormat));
        			} else {
        				line.addRoom(p.preferenceText(instructorNameFormat));
        				line.addRoomHtml(p.preferenceHtml(instructorNameFormat));
        			}
        		}
        		response.addInstructorRequirement(line);
			}
		}
		return response;
	}
	
	protected String propertyValue(Department dept, ApplicationProperty departmentalProperty, ApplicationProperty globalProperty) {
		if (dept != null) {
			String value = departmentalProperty.value(dept.getDeptCode());
			if (value != null) return value;
		}
		return globalProperty.value();
	}
}
