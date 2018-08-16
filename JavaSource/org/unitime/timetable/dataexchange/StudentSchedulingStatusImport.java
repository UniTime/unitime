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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Element;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.dao.CourseTypeDAO;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingStatusImport extends BaseImport {
	protected Formats.Format<Date> h24 = Formats.getDateFormat("HHmm");
	protected Formats.Format<Date> timeFormat = null;
	protected Formats.Format<Date> dateFormat = null;

	@Override
	public void loadXml(Element root) throws Exception {
		if (!root.getName().equalsIgnoreCase("studentStatuses")) {
        	throw new Exception("Given XML file is not a Student Scheduling Statuses load file.");
        }
		try {
			beginTransaction();
			
			Session session = null;
            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            if (campus != null && year != null && term != null) {
            	session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            	if (session == null)
    	           	throw new Exception("No session found for the given campus, year, and term.");
            }
			
			boolean incremental = "true".equalsIgnoreCase(root.attributeValue("incremental", "true"));
			dateFormat = Formats.getDateFormat(root.attributeValue("dateFormat", "yyyy/M/d"));
			timeFormat = Formats.getDateFormat(root.attributeValue("timeFormat", "HHmm"));
			
			Map<String, StudentSectioningStatus> statuses = new HashMap<String, StudentSectioningStatus>();
			for (StudentSectioningStatus status: StudentSectioningStatus.findAll(session == null ? null : session.getUniqueId())) {
				statuses.put(status.getReference(), status);
			}
			Map<String, CourseType> courseTypes = new HashMap<String, CourseType>();
			for (CourseType type: CourseTypeDAO.getInstance().findAll(getHibSession())) {
				courseTypes.put(type.getReference(), type);
			}
			Map<StudentSectioningStatus, String> fallbacks = new HashMap<StudentSectioningStatus, String>();
			Map<String, StudentSectioningStatus> all = new HashMap<String, StudentSectioningStatus>();
			
			for (Iterator i = root.elementIterator("status"); i.hasNext(); ) {
                Element statusEl = (Element) i.next();
                String ref = statusEl.attributeValue("reference");
                
                StudentSectioningStatus status = statuses.remove(ref);
                if (status == null) {
                	status = new StudentSectioningStatus();
                	status.setReference(ref);
                	status.setTypes(new HashSet<CourseType>());
                }
                status.setLabel(statusEl.attributeValue("name"));
                status.setSession("true".equalsIgnoreCase(statusEl.attributeValue("session","false")) ? session : null);
                
                Element permissionsEl = statusEl.element("permissions");
                status.setStatus(0);
                if (permissionsEl != null) {
                	for (StudentSectioningStatus.Option option: StudentSectioningStatus.Option.values())
                		if ("true".equalsIgnoreCase(permissionsEl.attributeValue(getAttribute(option), "false")))
                			status.addOption(option);
                }
                
                Element datesEl = statusEl.element("effective-dates");
                if (datesEl == null) {
                	status.setEffectiveStartDate(null);
                	status.setEffectiveStartPeriod(null);
                	status.setEffectiveStopDate(null);
                	status.setEffectiveStopPeriod(null);
                } else {
                	String startDate = datesEl.attributeValue("startDate");
                	if (startDate != null) {
                		try {
                			status.setEffectiveStartDate(dateFormat.parse(startDate));
                		} catch (ParseException e) {
                			status.setEffectiveStartDate(null);
                			warn("Failed to parse start date " + startDate + " (status " + ref + ")");
                		}
                	}
                	String startPeriod = datesEl.attributeValue("startPeriod");
                	if (startPeriod != null) {
                		try {
                			int time = Integer.parseInt(h24.format(timeFormat.parse(startPeriod)));
                			status.setEffectiveStartPeriod(12 * (time / 100) + ((time % 100) / 5));
                		} catch (ParseException e) {
                			status.setEffectiveStartPeriod(null);
                			warn("Failed to parse start period " + startPeriod + " (status " + ref + ")");
                		}
                	}
                	String stopDate = datesEl.attributeValue("stopDate");
                	if (stopDate != null) {
                		try {
                			status.setEffectiveStopDate(dateFormat.parse(stopDate));
                		} catch (ParseException e) {
                			status.setEffectiveStopDate(null);
                			warn("Failed to parse stop date " + stopDate + " (status " + ref + ")");
                		}
                	}
                	String stopPeriod = datesEl.attributeValue("stopPeriod");
                	if (stopPeriod != null) {
                		try {
                			int time = Integer.parseInt(h24.format(timeFormat.parse(stopPeriod)));
                			status.setEffectiveStopPeriod(12 * (time / 100) + ((time % 100) / 5));
                		} catch (ParseException e) {
                			status.setEffectiveStopPeriod(null);
                			warn("Failed to parse stop period " + stopPeriod + " (status " + ref + ")");
                		}
                	}
                }
                
                status.getTypes().clear();
                for (Iterator j = statusEl.elementIterator("course"); j.hasNext();) {
                	Element courseEl = (Element)j.next();
                	CourseType type = courseTypes.get(courseEl.attributeValue("type"));
                	if (type == null) {
                		warn("Unknown course type " + courseEl.attributeValue("type") + " (status " + ref + ")");
                	} else {
                		status.getTypes().add(type);
                	}
                }
                
                Element messageEl = statusEl.element("message");
                if (messageEl == null)
                	status.setMessage(null);
                else
                	status.setMessage(messageEl.getText());
                
                Element fallbackEl = statusEl.element("fallback");
                if (fallbackEl == null) {
                	status.setFallBackStatus(null);
                } else
                	fallbacks.put(status, fallbackEl.attributeValue("reference"));
                
                all.put(ref, status);
            	getHibSession().saveOrUpdate(status);
			}
			
			if (!incremental) {
				for (StudentSectioningStatus status: statuses.values()) {
					getHibSession().delete(status);
				}
			} else {
				for (StudentSectioningStatus status: statuses.values())
					all.put(status.getReference(), status);
			}
			
			for (Map.Entry<StudentSectioningStatus, String> e: fallbacks.entrySet()) {
				StudentSectioningStatus fallback = all.get(e.getValue());
				if (fallback == null) {
					warn("Unknown fallback status " + e.getValue() + " (status " + e.getKey().getReference() + ")");
				} else {
					e.getKey().setFallBackStatus(fallback);
				}
				getHibSession().saveOrUpdate(e.getKey());
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