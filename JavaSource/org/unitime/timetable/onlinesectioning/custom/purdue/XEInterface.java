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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import com.google.gson.reflect.TypeToken;

public class XEInterface {
	
	public static class Registration {
		public String subject;
		public String subjectDescription;
		public String courseNumber;
		public String courseReferenceNumber;
		public String courseTitle;
		
		/**
		 * 40	CEC 40% refund
		 * 60	CEC 60% refund
		 * 80	CEC 80% refund
		 * AA	Auditor Access
		 * AU	Audit
		 * CA	Cancel Administratively
		 * DB	Boiler Gold Rush Drop Course
		 * DC	Drop Course
		 * DD	Drop/Delete
		 * DT	Drop Course-TSW
		 * DW	Drop (Web)
		 * RC	**ReAdd Course**
		 * RE	**Registered**
		 * RT	**Web Registered**
		 * RW	**Web Registered**
		 * W	Withdrawn-W
		 * W1	Withdrawn
		 * W2	Withdrawn
		 * W3	Withdrawn
		 * W4	Withdrawn
		 * W5	Withdrawn
		 * WF	Withdrawn-WF
		 * WG	Withdrawn-pending grade
		 * WN	Withdrawn-WN
		 * WT	Withdrawn-W
		 * WU	Withdrawn-WU
		 * WL	Waitlist
		 */
		public String courseRegistrationStatus;
		public String courseRegistrationStatusDescription;
		public Double creditHour;
		
		public String gradingMode;
		public String gradingModeDescription;
		
		public String level;
		public String levelDescription;
		
		public DateTime registrationStatusDate;
		public String scheduleDescription;
		public String scheduleType;
		public String sequenceNumber;
		
		public String statusDescription;
		/**
		 * P = pending
		 * R = registered
		 * D = dropped
		 * L = waitlisted
		 * F = fatal error prevented registration
		 * W = withdrawn 
		 */
		public String statusIndicator;
		
		public List<CrnError> crnErrors;
		
		public String term;
		public String campus;
		
		public List<RegistrationAction> registrationActions;
		
		public boolean can(String status) {
			if (registrationActions != null)
				for (RegistrationAction action: registrationActions) {
					if (status.equals(action.courseRegistrationStatus))
						return true;
				}
			return false;
		}

		public boolean canDrop(boolean admin) {
			return can(admin ? "DDD" : "DW");
		}
		
		public boolean canAdd(boolean admin) {
			return can(admin ? "RE" : "RW");
		}
		
		public boolean isRegistered() {
			return "R".equals(statusIndicator);
		}
	}
	
	public static class CrnError {
		public String errorFlag;
		public String message;
		public String messageType;
	}
	
	public static class RegistrationAction {
		public String courseRegistrationStatus;
		public String description;
		public Boolean remove;
		public String voiceType;
	}
	
	public static class TimeTicket {
		public DateTime beginDate;
		public DateTime endDate;
		public String startTime;
		public String endTime;
	}
	
	public static class FailedRegistration {
		public String failedCRN;
		public String failure;
		public Registration registration;
	}
	
	public static class RegisterResponse {
		public static final Type TYPE_LIST = new TypeToken<ArrayList<RegisterResponse>>() {}.getType();
		
		public List<FailedRegistration> failedRegistrations;
		public List<String> failureReasons;
		public List<Registration> registrations;
		public List<TimeTicket> timeTickets;
		public Boolean validStudent;
		public String registrationException;

		public Float minHours;
		public Float maxHours;
	}
	
	public static class CourseReferenceNumber {
		public String courseReferenceNumber;
		public String courseRegistrationStatus;
		
		public CourseReferenceNumber() {}
		public CourseReferenceNumber(String crn) {
			this.courseReferenceNumber = crn;
		}
		public CourseReferenceNumber(String crn, String status) {
			this.courseReferenceNumber = crn;
			this.courseRegistrationStatus = status;
		}
	}
	
	public static class RegisterAction {
		public String courseReferenceNumber;
		public String selectedAction;
		public String selectedLevel;
		public String selectedGradingMode;
		public String selectedStudyPath;
		public String selectedCreditHour;
		public Boolean remove;
		
		public RegisterAction(String action, String crn) {
			selectedAction = action;
			courseReferenceNumber = crn;
		}
	}
	
	public static class RegisterRequest {
		public String bannerId;
		public String term;
		public String altPin;
		public String systemIn;
		public List<CourseReferenceNumber> courseReferenceNumbers;
		public List<RegisterAction> actionsAndOptions;
		
		public RegisterRequest(String term, String bannerId, String pin, boolean admin) {
			this.term = term; this.bannerId = bannerId; this.altPin = pin; this.systemIn = (admin ? "SB" : "WA");
		}
		
		public RegisterRequest drop(String crn) {
			if (actionsAndOptions == null) actionsAndOptions = new ArrayList<RegisterAction>();
			actionsAndOptions.add(new RegisterAction("SB".equals(systemIn) ? "DDD" : "DW", crn));
			return this;
		}
		
		public RegisterRequest keep(String crn) {
			if (courseReferenceNumbers == null)
				courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
			courseReferenceNumbers.add(new CourseReferenceNumber(crn));
			return this;
		}
		
		public RegisterRequest add(String crn, boolean changeStatus) {
			if (changeStatus) {
				if (actionsAndOptions == null) actionsAndOptions = new ArrayList<RegisterAction>();
				actionsAndOptions.add(new RegisterAction("SB".equals(systemIn) ? "RE" : "RW", crn));
			} else {
				if (courseReferenceNumbers == null)
					courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
				// if ("SB".equals(systemIn)) courseReferenceNumbers.add(new CourseReferenceNumber(crn, "RW")); else
				courseReferenceNumbers.add(new CourseReferenceNumber(crn));
			}
			return this;
		}
		
		public RegisterRequest empty() {
			if (courseReferenceNumbers == null)
				courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
			courseReferenceNumbers.add(new CourseReferenceNumber());
			return this;
		}
		
		public boolean isEmpty() {
			return (actionsAndOptions == null || actionsAndOptions.isEmpty()) && (courseReferenceNumbers == null || courseReferenceNumbers.isEmpty());
		}
	}
	
	public static class DegreePlan {
		public static final Type TYPE_LIST = new TypeToken<ArrayList<DegreePlan>>() {}.getType();
		public String id;
		public ValueDescription isActive, isLocked;
		public String description;
		public Person student;
		public CodeDescription degree;
		public CodeDescription school;
		public CodeDescription officialTrackingStatus;
		public Date createDate, modifyDate;
		public Person createdWho, modifyWho;
		
		public List<Year> years;
	}
	
	public static class Person {
		public String id;
		public String name;
	}
	
	public static class CodeDescription {
		public String code;
		public String description;
	}
	
	public static class ValueDescription {
		public boolean value;
		public String description;
	}
	
	public static class Year extends CodeDescription {
		public List<Term> terms;
	}
	
	public static class Term {
		public String id;
		public CodeDescription term;
		public Group group;
		
	}
	
	public static class Group {
		public String id;
		public CodeDescription groupType;
		public List<Course> plannedClasses;
		public List<Group> groups;
		public List<PlaceHolder> plannedPlaceholders;
		public String summaryDescription;
		public boolean isGroupSelection;
	}
	
	public static class Course {
		public String id;
		public String title;
		public String courseNumber;
		public String courseDiscipline;
		public boolean isGroupSelection;
	}
	
	public static class PlaceHolder {
		public String id;
		public CodeDescription placeholderType;
		public String placeholderValue;
	}
	
	public static class ErrorResponse {
		public List<Error> errors;
		
		public Error getError() {
			return (errors == null || errors.isEmpty() ? null : errors.get(0));
		}
	}
	
	public static class Error {
		public String code;
		public String message;
		public String description;
		public String type;
		public String errorMessage;
	}

}
