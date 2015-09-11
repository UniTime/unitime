/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.custom.purdue;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
		
		public boolean canDrop() {
			if (registrationActions != null)
				for (RegistrationAction action: registrationActions) {
					if ("DW".equals(action.courseRegistrationStatus))
						return true;
				}
			return false;
		}
		
		public boolean canAdd() {
			// return !"DD".equals(courseRegistrationStatus);
			if (registrationActions != null)
				for (RegistrationAction action: registrationActions) {
					if ("RW".equals(action.courseRegistrationStatus))
						return true;
				}
			return false;
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
			actionsAndOptions.add(new RegisterAction("DW", crn));
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
				actionsAndOptions.add(new RegisterAction("RW", crn));
			} else {
				if (courseReferenceNumbers == null)
					courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
				if ("SB".equals(systemIn))
					courseReferenceNumbers.add(new CourseReferenceNumber(crn, "RW"));
				else
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
