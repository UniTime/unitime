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
	
	static class Registration {
		String subject;
		String subjectDescription;
		String courseNumber;
		String courseReferenceNumber;
		String courseTitle;
		
		String courseRegistrationStatus;
		String courseRegistrationStatusDescription;
		Double creditHour;
		
		String gradingMode;
		String gradingModeDescription;
		
		String level;
		String levelDescription;
		
		DateTime registrationStatusDate;
		String scheduleDescription;
		String scheduleType;
		String sequenceNumber;
		
		String statusDescription;
		// R for registered, F for failure, D for deleted
		String statusIndicator;
		
		List<CrnError> crnErrors;
	}
	
	static class CrnError {
		String errorFlag;
		String message;
		String messageType;
	}
	
	static class RegistrationAction {
		String courseRegistrationStatus;
		String description;
		Boolean remove;
		String voiceType;
	}
	
	static class TimeTicket {
		DateTime beginDate;
		DateTime endDate;
		String startTime;
		String endTime;
	}
	
	static class FailedRegistration {
		String failedCRN;
		String failure;
		Registration registration;
	}
	
	public static class RegisterResponse {
		public static final Type TYPE_LIST = new TypeToken<ArrayList<RegisterResponse>>() {}.getType();
		
		List<FailedRegistration> failedRegistrations;
		List<String> failureReasons;
		List<Registration> registrations;
		List<TimeTicket> timeTickets;
		Boolean validStudent;
	}
	
	public static class CourseReferenceNumber {
		String courseReferenceNumber;
		
		public CourseReferenceNumber() {}
		public CourseReferenceNumber(String crn) {
			this.courseReferenceNumber = crn;
		}
	}
	
	public static class RegisterAction {
		String courseReferenceNumber;
		String selectedAction;
		String selectedLevel;
		String selectedGradingMode;
		String selectedStudyPath;
		String selectedCreditHour;
		
		public RegisterAction(String action, String crn) {
			selectedAction = action;
			courseReferenceNumber = crn;
		}
	}
	
	public static class RegisterRequest {
		String bannerId;
		String term;
		String altPin;
		List<CourseReferenceNumber> courseReferenceNumbers;
		List<RegisterAction> actionsAndOptions;
		
		public RegisterRequest(String term, String bannerId, String pin) {
			this.term = term; this.bannerId = bannerId; this.altPin = pin;
		}
		
		public RegisterRequest drop(String crn) {
			if (actionsAndOptions == null) actionsAndOptions = new ArrayList<RegisterAction>();
			actionsAndOptions.add(new RegisterAction("DW", crn));
			return this;
		}
		
		public RegisterRequest add(String crn) {
			if (courseReferenceNumbers == null)
				courseReferenceNumbers = new ArrayList<XEInterface.CourseReferenceNumber>();
			courseReferenceNumbers.add(new CourseReferenceNumber(crn));
			return this;
		}
	}

}
