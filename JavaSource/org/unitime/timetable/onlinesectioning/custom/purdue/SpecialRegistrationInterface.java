/*
< * Licensed to The Apereo Foundation under one or more contributor license
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationInterface {
	
	/** Possible values of the status field in the Spec Reg API responses */
	// FIXME: Are there any other values that can be returned?
	public static enum ResponseStatus {
		success, // call succeeded, data field is filled in
		error, // there was an error, see message for details 
		failure, //TODO: is this status used? 
		Failed, // present in outJson.status when the validation failed
		;
	}
	
	/** Possible values for the Spec Reg API mode */
	public static enum ApiMode {
		PREREG, // pre-registration (Course Requests page is used)
		REG, // registration (Scheduling Assistant page is used)
		;
	}
	
	/** Possible values for the requestor role */
	public static enum RequestorRole {
		STUDENT, // request is done by the student
		MANAGER, // request is done by the admin or the advisor on behalf of the student
		;
	}
	
	/** Completion status of a special registration request */
	public static enum CompletionStatus {
		inProgress, // there is at least one change (order) that has not been approved, denied or canceled
		completed, // all the changes are either approved or denied
		cancelled, // all the changes are either approved, denied, or canceled and there is at least one that is canceled
		;
	}
	
	/** Possible status values of a single override request ({@link Change}, work order in the Spec Reg terms) */
	// FIXME: Could there be any other values returned?
	public static enum ChangeStatus {
		 inProgress, // override has been posted, no change has been done to it yet
		 approved, // override has been approved
		 denied, // override has been denied
		 cancelled, // override has been cancelled
		 deferred, // override has been deferred (it is in progress for UniTime)
		 escalated, // override has been escalated (it is in progress for UniTime)
		 ;
	}
	
	/** Basic format of (mostly all) responses from Spec Reg API */
	public static class Response<T> {
		/** Response data, depends on the call */
		public T data;
		/** Success / error message */
		public String message;
		/** Response status, success or error */
		public ResponseStatus status;
	}
	
	/** Base representation of a special registration request that is used both during pre-registration and registration */
	// submitRegistration request (PREREG, REG)
	public static class SpecialRegistration {
		/** Special registration request id, only returned */
		public String regRequestId;
		/** Student PUID including the leading zero */
		public String studentId;
		/** Banner term (e.g., 201910 for Fall 2018) */
		public String term;
		/** Banner campus (e.g., PWL) */
		public String campus;
		/** Special Registration API mode (REG or PREREG) */
		public ApiMode mode;
		/** List of changes that the student needs overrides for */
		public List<Change> changes;
		/** Request creation date */
		public DateTime dateCreated;
		/** Max credit needed (only filled in when max override needs to be increased!) */
		public Float maxCredit;
		/** Student message provided with the request */
		public String requestorNotes;
		/** Request completion status (only read, never sent) */
		public CompletionStatus completionStatus;
	}
	
	/** Possible operations for a change (work order) */
	public static enum ChangeOperation {
		ADD, // sections (or the course in case of pre-reg) are being added
		DROP, // sections are being dropped (only during registration)
		KEEP, // sections are being unchanged, but a registration error was reported on them (e.g., co-requisite, only during registration)
		;
	}
	
	/** Class representing one change (in a signle course) */
	public static class Change {
		/** Subject area (can be null in case of the MAXI error) */
		public String subject;
		/** Course number (can be null in case of the MAXI error) */
		public String courseNbr;
		/** Comma separated list of crns (only used during registration) */
		public String crn;
		/** Change operation */
		public ChangeOperation operation;
		/** Registration errors */
		public List<ChangeError> errors;
		/** Status of the change */
		public ChangeStatus status;
		/** Notes attached to the change (only the last one is needed by UniTime) */
		public List<ChangeNote> notes;
		/** Credit value associated with the course / change, populated by UniTime (only during registration) */
		public String credit;
	}
	

	/** Registration error for which there needs to be an override */
	public static class ChangeError {
		/** Error code */
		String code;
		/** error message (shown to the student) */
		String message;
	}

	/** Change note */
	public static class ChangeNote {
		/** Time stamp of the note */
		public DateTime dateCreated;
		/** Name of the person that provided the note (not used by UniTime) */
		public String fullName;
		/** Text note */
		public String notes;
		/** Purpose of the note (not used by UniTime) */
		public String purpose;
	}
	
	/* - Sumbit Registration -------------------------------------------------- */
	// POST /submitRegistration
	// Request: SpecialRegistrationRequest
	// Returns: SubmitRegistrationResponse
	
	/**
	 * Payload message for the /submitRegistration request
	 */
	public static class SpecialRegistrationRequest extends SpecialRegistration {
		/**
		 * PUID (including the leading zero) of the user posting the request (may be a student or an admin/advisor).
		 * Only sent, not needed in the response.
		 */
		public String requestorId;
		/**
		 * Role of the requestor (STUDENT or MANAGER).
		 * Only sent, not needed in the response.
		 */
		public RequestorRole requestorRole;
		/**
		 * Course requests at the time when the override request was created (only used when mode = PREREG, only sent, not needed in the response)
		 */
		public List<CourseCredit> courseCreditHrs;
		/**
		 * Alternate course requests at the time when the override request was created (only used when mode = PREREG, only sent, not needed in the response)
		 */
		public List<CourseCredit> alternateCourseCreditHrs;
	}
	
	/**
	 * Course or alternate requests during pre-registration.
	 */
	public static class CourseCredit {
		/** Subject area */
		public String subject;
		/** Course number */
		public String courseNbr;
		/** Course title */
		public String title;
		/** Lower bound on the credit */
		public Float creditHrs;
		/** Alternatives, if provided */
		public List<CourseCredit> alternatives;
	}
	
	/** Class representing a special registration that has been cancelled */
	public static class CancelledRequest {
		/** Subject area */
		public String subject;
		/** Course number */
		public String courseNbr;
		/** Comma separated list of CRNs */
		public String crn;
		/** Special registration request id */
		public String regRequestId;
	}
	
	/**
	 * Special registrations for the /submitRegistration response
	 *
	 */
	public static class SubmitRegistrationResponse extends SpecialRegistration {
		/**
		 * List of special registrations that have been cancelled (to create this request).
		 * (only read, never sent; only used in submitRegistration response during registration)
		 */
		public List<CancelledRequest> cancelledRequests;
	}

	/**
	 * Response message for the /submitRegistration call
	 * In pre-registration, a separate special registration request is created for each course (plus one for the max credit increase, if needed).
	 * In registration, a single special registration request 
	 */
	public static class SpecialRegistrationResponseList extends Response<List<SubmitRegistrationResponse>> {
	}
	
	/* - Check Special Registration Status ------------------------------------ */
	// GET /checkSpecialRegistrationStatus?term=<TERM>&campus=<CAMPUS>&studentId=<PUID>&mode=<REG|PREREG>
	// Returns: SpecialRegistrationStatusResponse

	
	/** 
	 * Special registration status for the /checkSpecialRegistrationStatus response
	 */
	public static class SpecialRegistrationStatus {
		/** List of special registrations of the student (of given mode) */
		public List<SpecialRegistration> requests;
		/** Max credits that the student is allowed at the moment */
		public Float maxCredit;
		/** Student PUID including the leading zero (needed only in /checkAllSpecialRegistrationStatus) */
		public String studentId;
	}

	
	/**
	 * Response message for the /checkSpecialRegistrationStatus call
	 */
	public static class SpecialRegistrationStatusResponse extends Response<SpecialRegistrationStatus> {
	}
	
	/* - Check All Special Registration Status -------------------------------- */
	// GET /checkAllSpecialRegistrationStatus?term=<TERM>&campus=<CAMPUS>&studentIds=<PUID1,PUID2,...>&mode=PREREG
	// Returns: SpecialRegistrationMultipleStatusResponse
	
	/** Data message for the /checkAllSpecialRegistrationStatus call */
	public static class SpecialRegistrationMultipleStatus {
		public List<SpecialRegistrationStatus> students;
	}
	
	/**
	 * Response message for the /checkAllSpecialRegistrationStatus call (used only during pre-registration)
	 */
	public static class SpecialRegistrationMultipleStatusResponse extends Response<SpecialRegistrationMultipleStatus>{
	}

	/* - Check Eligibility ---------------------------------------------------- */
	// GET /checkEligibility?term=<TERM>&campus=<CAMPUS>&studentId=<PUID>&mode=<REG|PREREG>
	// Returns: CheckEligibilityResponse
	
	/**
	 * Student eligibility response for the /checkEligibility response
	 */
	public static class SpecialRegistrationEligibility {
		/** Student PUID including the leading zero */
		public String studentId;
		/** Banner term */
		public String term;
		/** Banner campus */
		public String campus;
		/** Is student eligible to register (pre-reg). Is student eligible to request overrides (reg). */
		public Boolean eligible;
		/** Detected eligibility problems (in pre-reg: e.g., student has a HOLD) */
		public List<EligibilityProblem> eligibilityProblems;
	}
	
	/**
	 * Detected student eligibility problem
	 */
	public static class EligibilityProblem {
		/** Problem code (e.g., HOLD) */
		String code;
		/** Problem message (description) that can bedisplayed to the student */
		String message;
	}
	
	/**
	 * Response message for the /checkSpecialRegistrationStatus call
	 */
	public static class CheckEligibilityResponse extends Response<SpecialRegistrationEligibility> {
		/** Registration errors for which overrides can be requested (only used during registration) */
		public Set<String> overrides;
		/** Student max credit (only used during registration) */
		public Float maxCredit;
		/** Are there any not-cancelled requests for the student (only used during registration. indication that the Requested Overrides table should be shown) */
		public Boolean hasNonCanceledRequest;
	}
	
	
	/* - Schedule Validation -------------------------------------------------- */
	// POST /checkRestrictions
	// request: CheckRestrictionsRequest
	// returns: CheckRestrictionsResponse

	/** Registrationn error */
	public static class Problem {
		/** Error code */
		String code;
		/** Error message */
		String message;
		/** Section affected */
		String crn;
	}

	/** Data returned from the schedule validation */
	public static class ScheduleRestrictions {
		/** List of detected problems */
		public List<Problem> problems;
		/** Student PUID including the leading zero */
		public String sisId;
		/** Validation status */
		// FIXME: what are the possible values?
		public ResponseStatus status;
		/** Banner term */
		public String term;
		/** Computed credit hours */
		public Float maxHoursCalc;
		/** Error message */
		public String message;
	}
	
	/** Possible values for the includeReg parameter */
	public enum IncludeReg {
		Y, // do include students current schedule in the validation
		N, // do NOT include students current schedule in the validation
		;
	}
	
	/** Possible values for the validation mode */
	public static enum ValidationMode {
		REG, // registration changes
		ALT, // alternate changes
		;
	}
	
	/** Possible values for the validation operation */
	public static enum ValidationOperation {
		ADD, // add CRNs
		DROP, // drop CRNs
		;
	}
	
	/** Representation of a single CRN */
	public static class Crn {
		/** CRN */
		String crn;
	}
	
	/** Data provided to the schedule validation */
	public static class RestrictionsCheckRequest {
		/** Student PUID including the leading zero */
		public String sisId;
		/** Banner term */
		public String term;
		/** Banner campus */
		public String campus;
		/** Include the current student's schedule in the validation */
		public IncludeReg includeReg;
		/** Validation mode (REG or ALT) */
		public ValidationMode mode;
		/** Schedule changes */
		public Map<ValidationOperation, List<Crn>> actions;
	}
	
	/** Overrides that have been denied for the student (matching the validation request) */
	public static class DeniedRequest {
		/** Subject area */
		public String subject;
		/** Course number */
		public String courseNbr;
		/** Comma separated lists of CRNs */
		public String crn;
		/** Registration error code */
		public String code;
		/** Registration error message */
		public String errorMessage;
		/** Special Registration API mode (REG or PREREG) */
		public ApiMode mode;
	}
	
	/** Max credit override that have been denied for the student */
	public static class DeniedMaxCredit {
		/** Registration error code */
		public String code;
		/** Registration error message */
		public String errorMessage;
		/** Max credit denied */
		public Float maxCredit;
		/** Special Registration API mode (REG or PREREG) */
		public ApiMode mode;
	}
	
	/** Request message for the /checkRestrictions call */
	public static class CheckRestrictionsRequest {
		/** Student PUID including the leading zero */
		public String studentId;
		/** Banner term */
		public String term;
		/** Banner campus */
		public String campus;
		/** Special Registration API mode (REG or PREREG) */
		public ApiMode mode;
		/** Schedule changes */
		public RestrictionsCheckRequest changes;
		/** Alternatives (only used in pre-registration) */
		public RestrictionsCheckRequest alternatives;
	}
	
	/** Response message for the /checkRestrictions call */
	public static class CheckRestrictionsResponse {
		/** Special registrations that would be cancelled if such a request is submitted (used only during registration) */
		public List<SpecialRegistration> cancelRegistrationRequests;
		/** Matching special registrations that has been denied already (used only during registration) */
		public List<DeniedRequest> deniedRequests;
		/** Max credit requests that have been denied (student should request that much credit) */
		public List<DeniedMaxCredit> deniedMaxCreditRequests;
		/** Student eligibility check (used only during registration) */
		public SpecialRegistrationEligibility eligible;
		/** Student's current max credit (used only during registration) */
		public Float maxCredit;
		/** Validation response for the schedule changes */
		public ScheduleRestrictions outJson;
		/** Validation response for the alternative schedule changes (only used in pre-registration) */
		public ScheduleRestrictions outJsonAlternatives;
		/** List of registrations errors for which the student is allowed to request overrides (used only during registration) */
		public Set<String> overrides;
		/** Response status, success or error */
		public ResponseStatus status;
		/** Error message when the validation request fails */
		public String message;
	}
	
	/* - Cancel Registration Request ------------------------------------------ */
	// GET /cancelRegistrationRequestFromUniTime?term=<TERM>&campus=<CAMPUS>&studentId=<PUID>&mode=REG
	// Returns: SpecialRegistrationCancelResponse
	
	/**
	 * Response message for the /cancelRegistrationRequestFromUniTime call
	 */
	public static class SpecialRegistrationCancelResponse extends Response<String> {
	}
}
