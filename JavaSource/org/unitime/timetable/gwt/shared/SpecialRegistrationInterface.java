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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	
	public static class SpecialRegistrationContext implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private boolean iSpecReg = false;
		private String iSpecRegRequestId = null;
		private String iSpecRegRequestKey = null;
		private boolean iSpecRegRequestKeyValid = false;
		private boolean iSpecRegDisclaimerAccepted = false;
		private boolean iSpecRegTimeConfs = false;
		private boolean iSpecRegSpaceConfs = false;
		private SpecialRegistrationStatus iSpecRegStatus = null;
		private String iNote;

		public SpecialRegistrationContext() {}
		public SpecialRegistrationContext(SpecialRegistrationContext cx) {
			copy(cx);
		}
		public void copy(SpecialRegistrationContext cx) {
			iSpecReg = cx.iSpecReg;
			iSpecRegRequestId = cx.iSpecRegRequestId;
			iSpecRegRequestKey = cx.iSpecRegRequestKey;
			iSpecRegDisclaimerAccepted = cx.iSpecRegDisclaimerAccepted;
			iSpecRegTimeConfs = cx.iSpecRegTimeConfs;
			iSpecRegSpaceConfs = cx.iSpecRegSpaceConfs;
			iSpecRegStatus = cx.iSpecRegStatus;
		}
		
		public boolean isEnabled() { return iSpecReg; }
		public void setEnabled(boolean specReg) { iSpecReg = specReg; }
		public String getRequestKey() { return iSpecRegRequestKey; }
		public void setRequestKey(String key) { iSpecRegRequestKey = key; }
		public boolean hasRequestKey() { return iSpecRegRequestKey != null && !iSpecRegRequestKey.isEmpty(); }
		public boolean isSpecRegRequestKeyValid() { return iSpecRegRequestKeyValid; }
		public void setSpecRegRequestKeyValid(boolean valid) { iSpecRegRequestKeyValid = valid; }
		public boolean hasRequestId() { return iSpecRegRequestId != null; }
		public String getRequestId() { return iSpecRegRequestId; }
		public void setRequestId(String id) { iSpecRegRequestId = id; }
		public boolean isCanSubmit() { return iSpecRegStatus == null || iSpecRegStatus == SpecialRegistrationStatus.Draft; }
		public boolean isDisclaimerAccepted() { return iSpecRegDisclaimerAccepted; }
		public void setDisclaimerAccepted(boolean accepted) { iSpecRegDisclaimerAccepted = accepted; }
		public boolean areTimeConflictsAllowed() { return iSpecRegTimeConfs; }
		public void setTimeConflictsAllowed(boolean allow) { iSpecRegTimeConfs = allow; }
		public boolean areSpaceConflictsAllowed() { return iSpecRegSpaceConfs; }
		public void setSpaceConflictsAllowed(boolean allow) { iSpecRegSpaceConfs = allow; }
		public SpecialRegistrationStatus getStatus() { return iSpecRegStatus; }
		public void setStatus(SpecialRegistrationStatus status) { iSpecRegStatus = status; }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		public void update(EligibilityCheck check) {
			iSpecRegTimeConfs = check != null && check.hasFlag(EligibilityFlag.SR_TIME_CONF);
			iSpecRegSpaceConfs = check != null && check.hasFlag(EligibilityFlag.SR_LIMIT_CONF);
			iSpecReg = check != null && check.hasFlag(EligibilityFlag.CAN_SPECREG);
		}
		public void reset() {
			iNote = null;
			iSpecReg = false;
			iSpecRegRequestId = null;
			iSpecRegRequestKeyValid = false;
			iSpecRegDisclaimerAccepted = false;
			iSpecRegTimeConfs = false;
			iSpecRegSpaceConfs = false;
			iSpecRegStatus = null;
		}
		public void reset(EligibilityCheck check) {
			reset();
			if (check != null) update(check);
		}
	}
	
	public static class SpecialRegistrationEligibilityRequest implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private Long iStudentId;
		private Collection<ClassAssignmentInterface.ClassAssignment> iClassAssignments;
		private ArrayList<ErrorMessage> iErrors = null;
		
		public SpecialRegistrationEligibilityRequest() {}
		public SpecialRegistrationEligibilityRequest(Long sessionId, Long studentId, Collection<ClassAssignmentInterface.ClassAssignment> assignments, Collection<ErrorMessage> errors) {
			iClassAssignments = assignments;
			iStudentId = studentId;
			iSessionId = sessionId;
			if (errors != null)
				iErrors = new ArrayList<ErrorMessage>(errors);
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public Collection<ClassAssignmentInterface.ClassAssignment> getClassAssignments() { return iClassAssignments; }
		public void setClassAssignments(Collection<ClassAssignmentInterface.ClassAssignment> assignments) { iClassAssignments = assignments; }
		public void addError(ErrorMessage error) {
			if (iErrors == null) iErrors = new ArrayList<ErrorMessage>();
			iErrors.add(error);
		}
		public boolean hasErrors() {
			return iErrors != null && !iErrors.isEmpty();
		}
		public ArrayList<ErrorMessage> getErrors() { return iErrors; }
	}
	
	public static class SpecialRegistrationEligibilityResponse implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iMessage;
		private boolean iCanSubmit;
		private ArrayList<ErrorMessage> iErrors = null;
		
		public SpecialRegistrationEligibilityResponse() {}
		public SpecialRegistrationEligibilityResponse(boolean canSubmit, String message) {
			iCanSubmit = canSubmit; iMessage = message;
		}
	
		public boolean isCanSubmit() { return iCanSubmit; }
		public void setCanSubmit(boolean canSubmit) { iCanSubmit = canSubmit; }
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public void addError(ErrorMessage error) {
			if (iErrors == null) iErrors = new ArrayList<ErrorMessage>();
			iErrors.add(error);
		}
		public boolean hasErrors() {
			return iErrors != null && !iErrors.isEmpty();
		}
		public ArrayList<ErrorMessage> getErrors() { return iErrors; }
		public void setErrors(Collection<ErrorMessage> messages) {
			if (messages == null)
				iErrors = null;
			else
				iErrors = new ArrayList<ErrorMessage>(messages);
		}
	}
	
	public static class RetrieveSpecialRegistrationRequest implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private Long iStudentId;
		private String iRequestKey;
		
		public RetrieveSpecialRegistrationRequest() {}
		public RetrieveSpecialRegistrationRequest(Long sessionId, Long studentId, String requestKey) {
			iRequestKey = requestKey;
			iStudentId = studentId;
			iSessionId = sessionId;
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public String getRequestKey() { return iRequestKey; }
		public void setRequestKey(String requestKey) { iRequestKey = requestKey; }
	}
	
	public static enum SpecialRegistrationStatus implements IsSerializable, Serializable {
		Draft, Pending, Approved, Rejected, Cancelled,
		;
	}
	
	public static enum SpecialRegistrationOperation implements IsSerializable, Serializable {
		Add, Drop, Keep,
		;
	}
	
	public static class RetrieveSpecialRegistrationResponse implements IsSerializable, Serializable, Comparable<RetrieveSpecialRegistrationResponse> {
		private static final long serialVersionUID = 1L;
		private ClassAssignmentInterface iClassAssignment;
		private SpecialRegistrationStatus iStatus;
		private Date iSubmitDate;
		private String iRequestId;
		private String iDescription;
		private String iNote;
		private List<ClassAssignmentInterface.ClassAssignment> iChanges;
		private boolean iCanCancel = false;
		private boolean iHasTimeConflict, iHasSpaceConflict;
		
		public RetrieveSpecialRegistrationResponse() {}
		
		public boolean hasClassAssignments() { return iClassAssignment != null; }
		public ClassAssignmentInterface getClassAssignments() { return iClassAssignment; }
		public void setClassAssignments(ClassAssignmentInterface assignments) { iClassAssignment = assignments; }

		public Date getSubmitDate() { return iSubmitDate; }
		public void setSubmitDate(Date date) { iSubmitDate = date; }
		
		public String getRequestId() { return iRequestId; }
		public void setRequestId(String requestId) { iRequestId = requestId; }
		
		public String getDescription() { return iDescription; }
		public void setDescription(String description) { iDescription = description; }
		
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		
		public SpecialRegistrationStatus getStatus() { return iStatus; }
		public void setStatus(SpecialRegistrationStatus status) { iStatus = status; }
		
		public boolean hasChanges() { return iChanges != null && !iChanges.isEmpty(); }
		public List<ClassAssignmentInterface.ClassAssignment> getChanges() { return iChanges; }
		public void addChange(ClassAssignmentInterface.ClassAssignment ca) {
			if (iChanges == null) iChanges = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
			iChanges.add(ca);
		}
		
		public boolean canCancel() { return iCanCancel; }
		public void setCanCancel(boolean canCancel) { iCanCancel = canCancel; }
		
		public boolean hasTimeConflict() { return iHasTimeConflict; }
		public void setHasTimeConflict(boolean hasTimeConflict) { iHasTimeConflict = hasTimeConflict; }
		
		public boolean hasSpaceConflict() { return iHasSpaceConflict; }
		public void setHasSpaceConflict(boolean hasSpaceConflict) { iHasSpaceConflict = hasSpaceConflict; }
		
		@Override
		public int compareTo(RetrieveSpecialRegistrationResponse o) {
			int cmp = getSubmitDate().compareTo(o.getSubmitDate());
			if (cmp != 0) return -cmp;
			return getRequestId().compareTo(o.getRequestId());
		}
		
		public int hashCode() {
			return getRequestId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof RetrieveSpecialRegistrationResponse)) return false;
			return getRequestId().equals(((RetrieveSpecialRegistrationResponse)o).getRequestId());
		}
	}
	
	public static class SubmitSpecialRegistrationRequest implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private Long iStudentId;
		private String iRequestKey;
		private String iRequestId;
		private CourseRequestInterface iCourses;
		private Collection<ClassAssignmentInterface.ClassAssignment> iClassAssignments;
		private ArrayList<ErrorMessage> iErrors = null;
		private String iNote;
		
		public SubmitSpecialRegistrationRequest() {}
		public SubmitSpecialRegistrationRequest(Long sessionId, Long studentId, String requestKey, String requestId, CourseRequestInterface courses, Collection<ClassAssignmentInterface.ClassAssignment> assignments, Collection<ErrorMessage> errors, String note) {
			iRequestKey = requestKey;
			iRequestId = requestId;
			iStudentId = studentId;
			iSessionId = sessionId;
			iCourses = courses;
			iClassAssignments = assignments;
			if (errors != null)
				iErrors = new ArrayList<ErrorMessage>(errors);
			iNote = note;
		}
		
		public Collection<ClassAssignmentInterface.ClassAssignment> getClassAssignments() { return iClassAssignments; }
		public void setClassAssignments(Collection<ClassAssignmentInterface.ClassAssignment> assignments) { iClassAssignments = assignments; }
		public CourseRequestInterface getCourses() { return iCourses; }
		public void setCourses(CourseRequestInterface courses) { iCourses = courses; }
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public String getRequestId() { return iRequestId; }
		public void setRequestId(String requestId) { iRequestId = requestId; }
		public String getRequestKey() { return iRequestKey; }
		public void setRequestKey(String requestKey) { iRequestKey = requestKey; }
		public void addError(ErrorMessage error) {
			if (iErrors == null) iErrors = new ArrayList<ErrorMessage>();
			iErrors.add(error);
		}
		public boolean hasErrors() {
			return iErrors != null && !iErrors.isEmpty();
		}
		public ArrayList<ErrorMessage> getErrors() { return iErrors; }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
	}
	
	public static class SubmitSpecialRegistrationResponse implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iRequestId;
		private String iMessage;
		private boolean iSuccess;
		private SpecialRegistrationStatus iStatus = null;
		
		public SubmitSpecialRegistrationResponse() {}
		
		public String getRequestId() { return iRequestId; }
		public void setRequestId(String requestId) { iRequestId = requestId; }
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
		
		public boolean isSuccess() { return iSuccess; }
		public boolean isFailure() { return !iSuccess; }
		public void setSuccess(boolean success) { iSuccess = success; }
		
		public SpecialRegistrationStatus getStatus() { return iStatus; }
		public void setStatus(SpecialRegistrationStatus status) { iStatus = status; }
	}
	
	public static class RetrieveAllSpecialRegistrationsRequest implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private Long iStudentId;
		
		public RetrieveAllSpecialRegistrationsRequest() {}
		public RetrieveAllSpecialRegistrationsRequest(Long sessionId, Long studentId) {
			iStudentId = studentId;
			iSessionId = sessionId;
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
	}
	
	public static class CancelSpecialRegistrationRequest implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId;
		private Long iStudentId;
		private String iRequestKey;
		private String iRequestId;
		
		public CancelSpecialRegistrationRequest() {}
		public CancelSpecialRegistrationRequest(Long sessionId, Long studentId, String requestKey, String requestId) {
			iRequestKey = requestKey;
			iRequestId = requestId;
			iStudentId = studentId;
			iSessionId = sessionId;
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public String getRequestId() { return iRequestId; }
		public void setRequestId(String requestId) { iRequestId = requestId; }
		public String getRequestKey() { return iRequestKey; }
		public void setRequestKey(String requestKey) { iRequestKey = requestKey; }
	}
	
	public static class CancelSpecialRegistrationResponse implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private boolean iSuccess;
		private String iMessage;
		
		public CancelSpecialRegistrationResponse() {}
		
		public boolean isSuccess() { return iSuccess; }
		public boolean isFailure() { return !iSuccess; }
		public void setSuccess(boolean success) { iSuccess = success; }
		
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		public void setMessage(String message) { iMessage = message; }
	}
}
