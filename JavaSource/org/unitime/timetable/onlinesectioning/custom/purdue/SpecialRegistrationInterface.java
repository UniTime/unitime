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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationInterface {
	
	public static class SpecialRegistrationRequest {
		public String requestId;
		public String studentId;
		public String term;
		public String campus;
		public String status;
		public String mode;
		public List<Change> changes;
		public DateTime dateCreated;
		public Float maxCredit;
		public String requestorId;
		public String requestorRole;
		public List<CourseCredit> courseCreditHrs;
		public List<CourseCredit> alternateCourseCreditHrs;
		public String notes;
		public String requestorNotes;
		public String completionStatus;
	}
	
	public static class SpecialRegistrationEligibility {
		public String studentId;
		public String term;
		public String campus;
		public Boolean eligible;
		public List<EligibilityProblem> eligibilityProblems;
	}
	
	public static enum RequestStatus {
		 mayEdit, mayNotEdit, maySubmit, newRequest,
		 draft, inProgress, approved, denied, cancelled,
		 completed, deferred, escalated,
		 ;
	}
	
	public static class SpecialRegistrationResponse {
		public SpecialRegistrationRequest data;
		public String status;
		public String message;
	}
	
	public static class SpecialRegistrationResponseList {
		public List<SpecialRegistrationRequest> data;
		public String status;
		public String message;
	}
	
	public static class SpecialRegistrationStatusResponse {
		public SpecialRegistrationStatus data;
		public String status;
		public String message;
	}
	
	public static class SpecialRegistrationEligibilityResponse {
		public SpecialRegistrationEligibility data;
		public String status;
		public String message;
	}

	public static class SpecialRegistrationMultipleStatusResponse {
		public SpecialRegistrationMultipleStatus data;
		public String status;
		public String message;
	}
	
	public static class SpecialRegistrationMultipleStatus {
		public Set<String> overrides;
		public List<SpecialRegistrationStatus> students;
	}
	
	public static class SpecialRegistrationStatus {
		public Set<String> overrides;
		public List<SpecialRegistrationRequest> requests;
		public Float maxCredit;
		public String studentId;
	}

	public static enum ResponseStatus {
		success, failure;
	}
	
	public static enum ChangeOperation {
		ADD, DROP, KEEP,
	}
	
	public static class Change {
		public String subject;
		public String courseNbr;
		public String crn;
		public String operation;
		public List<ChangeError> errors;
		public List<Override> overrides;
		public String status;
		public String notes;
	}
	
	public static class ChangeError {
		String code;
		String message;
	}
	
	public static class EligibilityProblem {
		String code;
		String message;
	}
	
	public static class Override {
		String code;
		String message;
		String needsAction;
		String needsOverride;
		String overrideApplied;
	}

	public static class ValidationCheckRequest {
		public String studentId;
		public String term;
		public String campus;
		public String includeReg;
		public String mode;
		public List<Schedule> schedule;
		public List<Schedule> alternatives;
	}
	
	public static class Schedule {
		public String subject;
		public String courseNbr;
		public Set<String> crns;
	}
	
	public static class ValidationCheckResponse {
		public ScheduleRestrictions scheduleRestrictions;
		public ScheduleRestrictions alternativesRestrictions;
	}
	
	public static class ScheduleRestrictions {
		public List<Problem> problems;
		public String sisId;
		public String status;
		public String term;
	}
	
	public static class Problem {
		String code;
		String crn;
		String message;
	}
	
	public static class CourseCredit {
		public String subject;
		public String courseNbr;
		public String title;
		public Float creditHrs;
		public List<CourseCredit> alternatives;
	}
	
	public static class RestrictionsCheckRequest {
		public String sisId;
		public String term;
		public String campus;
		public String includeReg;
		public String mode;
		public Map<String, List<Crn>> actions;
		public void addOperation(String op, String crn) {
			if (actions == null) actions = new HashMap<String, List<Crn>>();
			List<Crn> crns = actions.get(op);
			if (crns == null) {
				crns = new ArrayList<Crn>();
				actions.put(op, crns);
			}
			Crn c = new Crn(); c.crn = crn;
			crns.add(c);
		}
		public void add(String crn) { addOperation("ADD", crn); }
		public void drop(String crn) { addOperation("DROP", crn); }
	}
	
	public static class Crn {
		String crn;
	}
	
	public static class RestrictionsCheckResponse extends ScheduleRestrictions {
	}
	
	public static class SpecialRegistrationCancelResponse {
		public String data;
		public String status;
		public String message;
	}
}
