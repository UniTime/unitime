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
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingStatusExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
    protected static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("studentStatuses");
	        root.addAttribute("created", new Date().toString());
	        root.addAttribute("dateFormat", sDateFormat.toPattern());
            root.addAttribute("timeFormat", sTimeFormat.toPattern());
            root.addAttribute("incremental", "true");
	        
	        document.addDocType("studentStatuses", "-//UniTime//UniTime Student Scheduling Statuses DTD/EN", "http://www.unitime.org/interface/StudentStatuses.dtd");
	        
	        for (StudentSectioningStatus status: (List<StudentSectioningStatus>)getHibSession().createQuery("from StudentSectioningStatus order by reference").list()) {
	        	Element statusElement = root.addElement("status");
	        	statusElement.addAttribute("reference", status.getReference());
	        	statusElement.addAttribute("name", status.getLabel());
	        	Element permissionsEl = statusElement.addElement("permissions");
	        	for (StudentSectioningStatus.Option option: StudentSectioningStatus.Option.values())
	        		permissionsEl.addAttribute(getAttribute(option), status.hasOption(option) ? "true" : "false");
	        	Element datesEl = null;
	        	if (status.getEffectiveStartDate() != null) {
	        		if (datesEl == null) datesEl = statusElement.addElement("effective-dates");
	        		datesEl.addAttribute("startDate", sDateFormat.format(status.getEffectiveStartDate()));
	        	}
	        	if (status.getEffectiveStartPeriod() != null) {
	        		if (datesEl == null) datesEl = statusElement.addElement("effective-dates");
	        		int hour = status.getEffectiveStartPeriod() / 12;
	        	    int min = 5 * (status.getEffectiveStartPeriod() % 12);
	        		datesEl.addAttribute("startPeriod", sTwoNumbersDF.format(hour) + sTwoNumbersDF.format(min));
	        	}
	        	if (status.getEffectiveStopDate() != null) {
	        		if (datesEl == null) datesEl = statusElement.addElement("effective-dates");
	        		datesEl.addAttribute("stopDate", sDateFormat.format(status.getEffectiveStopDate()));
	        	}
	        	if (status.getEffectiveStopPeriod() != null) {
	        		if (datesEl == null) datesEl = statusElement.addElement("effective-dates");
	        		int hour = status.getEffectiveStopPeriod() / 12;
	        	    int min = 5 * (status.getEffectiveStopPeriod() % 12);
	        		datesEl.addAttribute("stopPeriod", sTwoNumbersDF.format(hour) + sTwoNumbersDF.format(min));
	        	}
	        	if (status.getMessage() != null)
	        		statusElement.addElement("message").setText(status.getMessage());
	        	for (CourseType type: status.getTypes())
	        		statusElement.addElement("course").addAttribute("type", type.getReference());
	        	if (status.getFallBackStatus() != null)
	        		statusElement.addElement("fallback").addAttribute("reference", status.getFallBackStatus().getReference());
	        }

            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
		}
	}
	
	protected static String getAttribute(StudentSectioningStatus.Option option) {
		switch (option) {
		case enabled: return "assistantEnabled";
		case admin: return "assistantAdminEdit";
		case advisor: return "assistantAdvisorEdit";
		case enrollment: return "assistantStudentEdit";
		case regenabled: return "requestsEnabled";
		case regadmin: return "requestsAdminEdit";
		case regadvisor: return "requestsAdvisorEdit";
		case registration: return "requestsStudentEdit";
		case email: return "emaiNotifications";
		case nobatch: return "doNotScheduleInBatch";
		case notype: return "mustHaveCourseType";
		case waitlist: return "waitListing";
		case advcanset: return "advisorCanSetStatus";
		case specreg: return "specialRegistration";
		case reqval: return "requestValidation";
		default: return option.name();
		}
	}
}