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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	
	public static class EligibilityCheck implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private int iFlags = 0;
		private String iMessage = null;
		private String iCheckboxMessage = null;
		private Long iSessionId = null, iStudentId = null;
		private Set<String> iOverrides = null;
		private String iOverrideRequestDisclaimer = null;
		
		public static enum EligibilityFlag implements IsSerializable {
			IS_ADMIN, IS_ADVISOR, IS_GUEST,
			CAN_USE_ASSISTANT,
			CAN_ENROLL,
			PIN_REQUIRED,
			CAN_WAITLIST,
			RECHECK_AFTER_ENROLLMENT,
			RECHECK_BEFORE_ENROLLMENT,
			CAN_RESET,
			CONFIRM_DROP,
			QUICK_ADD_DROP,
			ALTERNATIVES_DROP,
			GWT_CONFIRMATIONS,
			DEGREE_PLANS,
			CAN_REGISTER,
			NO_REQUEST_ARROWS,
			CAN_SPECREG, HAS_SPECREG, SR_TIME_CONF, SR_LIMIT_CONF,
			;
			
			public int flag() { return 1 << ordinal(); }
			
		}
		
		public EligibilityCheck() {}
		
		public EligibilityCheck(String message, EligibilityFlag... flags) {
			iMessage = message;
			for (EligibilityFlag flag: flags)
				setFlag(flag, true);
		}
		
		public EligibilityCheck(EligibilityFlag... flags) {
			for (EligibilityFlag flag: flags)
				setFlag(flag, true);
		}
		
		public void setFlag(EligibilityFlag flag, boolean set) {
			if (set && !hasFlag(flag))
				iFlags += flag.flag();
			if (!set && hasFlag(flag))
				iFlags -= flag.flag();
		}
		
		public boolean hasFlag(EligibilityFlag flag) {
			return (iFlags & flag.flag()) != 0;
		}
		
		public boolean hasFlag(EligibilityFlag... flags) {
			for (EligibilityFlag flag: flags)
				if ((iFlags & flag.flag()) != 0) return true;
			return false;
		}
		
		public void setMessage(String message) { iMessage = message; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		
		public void setCheckboxMessage(String message) { iCheckboxMessage = message; }
		public boolean hasCheckboxMessage() { return iCheckboxMessage != null && !iCheckboxMessage.isEmpty(); }
		public String getCheckboxMessage() { return iCheckboxMessage; }
		
		public void setOverrideRequestDisclaimer(String message) { iOverrideRequestDisclaimer = message; }
		public String getOverrideRequestDisclaimer() { return iOverrideRequestDisclaimer; }
		public boolean hasOverrideRequestDisclaimer() { return iOverrideRequestDisclaimer != null && !iOverrideRequestDisclaimer.isEmpty(); }

		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public Long getStudentId() { return iStudentId; }
		
		public boolean hasOverride(String errorCode) {
			if (errorCode == null || errorCode.isEmpty()) return true;
			return iOverrides != null && iOverrides.contains(errorCode);
		}
		public void setOverrides(Collection<String> overrides) {
			iOverrides = (overrides == null ? null : new HashSet<String>(overrides));
		}
	}
	
	public static class SectioningProperties implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId = null;
		private boolean iAdmin = false, iAdvisor = false;
		private boolean iEmail = false, iMassCancel = false, iChangeStatus = false;
		private boolean iRequestUpdate = false;
		private boolean iChangeLog = false;
		private boolean iCheckStudentOverrides = false;
		private boolean iValidateStudentOverrides = false;
		private Set<StudentGroupInfo> iEditableGroups = null;
		
		public SectioningProperties() {
		}
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
		
		public boolean isAdmin() { return iAdmin; }
		public boolean isAdvisor() { return iAdvisor; }
		public boolean isAdminOrAdvisor() { return iAdmin || iAdvisor; }
		
		public void setAdmin(boolean admin) { iAdmin = admin; }
		public void setAdvisor(boolean advisor) { iAdvisor = advisor; }
		
		public void setEmail(boolean email) { iEmail = email; }
		public boolean isEmail() { return iEmail; }
		
		public void setMassCancel(boolean massCancel) { iMassCancel = massCancel; }
		public boolean isMassCancel() { return iMassCancel; }
		
		public void setChangeStatus(boolean changeStatus) { iChangeStatus = changeStatus; }
		public boolean isChangeStatus() { return iChangeStatus; }
		
		public void setRequestUpdate(boolean requestUpdate) { iRequestUpdate = requestUpdate; }
		public boolean isRequestUpdate() { return iRequestUpdate; }
		
		public void setCheckStudentOverrides(boolean checkOverrides) { iCheckStudentOverrides = checkOverrides; }
		public boolean isCheckStudentOverrides() { return iCheckStudentOverrides; }
		
		public void setValidateStudentOverrides(boolean ValidateOverrides) { iValidateStudentOverrides = ValidateOverrides; }
		public boolean isValidateStudentOverrides() { return iValidateStudentOverrides; }
		
		public void setChangeLog(boolean changeLog) { iChangeLog = changeLog; }
		public boolean isChangeLog() { return iChangeLog; }
		
		public boolean isCanSelectStudent() {
			return iEmail || iMassCancel || iChangeStatus || iRequestUpdate || iCheckStudentOverrides || iValidateStudentOverrides;
		}
		
		public boolean hasEditableGroups() { return iEditableGroups != null && !iEditableGroups.isEmpty(); }
		public Set<StudentGroupInfo> getEditableGroups() { return iEditableGroups; }
		public void setEditableGroups(Set<StudentGroupInfo> groups) { iEditableGroups = groups; }
		public void addEditableGroup(StudentGroupInfo group) {
			if (iEditableGroups == null) iEditableGroups = new TreeSet<StudentGroupInfo>();
			iEditableGroups.add(group);
		}
	}
	
	public static class StudentGroupInfo implements IsSerializable, Serializable, Comparable<StudentGroupInfo> {
		private static final long serialVersionUID = 1L;
		private Long iUniqueId;
		private String iReference, iLabel;
		private String iType;
		
		public StudentGroupInfo() {}
		public StudentGroupInfo(Long id, String reference, String label, String type) {
			iUniqueId = id; iReference = reference; iLabel = label; iType = type;
		}
		
		public void setUniqueId(Long id) { iUniqueId = id; }
		public Long getUniqueId() { return iUniqueId; }
		
		public void setReference(String reference) { iReference = reference; }
		public String getReference() { return iReference; }
		public void setLabel(String label) { iLabel = label; }
		public String getLabel() { return iLabel; }
		
		public void setType(String type) { iType = type; }
		public boolean hasType() { return iType != null && !iType.isEmpty(); }
		public String getType() { return iType; }

		@Override
		public String toString() { return getReference(); }
		
		@Override
		public int hashCode() { return getReference().hashCode(); }
		
		@Override
		public int compareTo(StudentGroupInfo status) {
			int cmp = (hasType() ? getType() : "").compareTo(status.hasType() ? status.getType() : "");
			if (cmp != 0) return cmp;
			return getReference().compareTo(status.getReference());
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof StudentGroupInfo)) return false;
			return getUniqueId().equals(((StudentGroupInfo)o).getUniqueId());
		}
	}
	
	public static class StudentStatusInfo implements IsSerializable, Serializable, Comparable<StudentStatusInfo> {
		private static final long serialVersionUID = 1L;
		private Long iUniqueId;
		private String iReference, iLabel;
		private boolean iAssistantPage = false, iRequestsPage = false;
		private boolean iRegStudent = false, iRegAdvisor = false, iRegAdmin = false;
		private boolean iEnrlStudent = false, iEnrlAdvisor = false, iEnrlAdmin = false;
		private boolean iWaitList = false, iEmail = false;
		private boolean iSpecReg = false, iReqValidation = false;
		private String iCourseTypes;
		private String iEffectiveStart, iEffectiveStop;
		private String iMessage;
		
		public StudentStatusInfo() {}
		
		public void setUniqueId(Long id) { iUniqueId = id; }
		public Long getUniqueId() { return iUniqueId; }
		
		public void setReference(String reference) { iReference = reference; }
		public String getReference() { return iReference; }
		public void setLabel(String label) { iLabel = label; }
		public String getLabel() { return iLabel; }
		
		public void setCanAccessAssistantPage(boolean assistantPage) { iAssistantPage = assistantPage; }
		public boolean isCanAccessAssistantPage() { return iAssistantPage; }
		public void setCanAccessRequestsPage(boolean requestPage) { iRequestsPage = requestPage; }
		public boolean isCanAccessRequestsPage() { return iRequestsPage; }
		
		public void setCanStudentRegister(boolean regStudent) { iRegStudent = regStudent; }
		public boolean isCanStudentRegister() { return iRegStudent; }
		public void setCanAdvisorRegister(boolean regAdvisor) { iRegAdvisor = regAdvisor; }
		public boolean isCanAdvisorRegister() { return iRegAdvisor; }
		public void setCanAdminRegister(boolean regAdmin) { iRegAdmin = regAdmin; }
		public boolean isCanAdminRegister() { return iRegAdmin; }
		
		public void setCanStudentEnroll(boolean enrlStudent) { iEnrlStudent = enrlStudent; }
		public boolean isCanStudentEnroll() { return iEnrlStudent; }
		public void setCanAdvisorEnroll(boolean enrlAdvisor) { iEnrlAdvisor = enrlAdvisor; }
		public boolean isCanAdvisorEnroll() { return iEnrlAdvisor; }
		public void setCanAdminEnroll(boolean enrlAdmin) { iEnrlAdmin = enrlAdmin; }
		public boolean isCanAdminEnroll() { return iEnrlAdmin; }
		
		public void setWaitList(boolean waitlist) { iWaitList = waitlist; }
		public boolean isWaitList() { return iWaitList; }
		public void setEmail(boolean email) { iEmail = email; }
		public boolean isEmail() { return iEmail; }
		
		public void setSpecialRegistration(boolean specReg) { iSpecReg = specReg; }
		public boolean isSpecialRegistration() { return iSpecReg; }
		public void setRequestValiadtion(boolean reqVal) { iReqValidation = reqVal; }
		public boolean isRequestValiadtion() { return iReqValidation; }
		
		public void setAllEnabled() {
			iAssistantPage = true;
			iRequestsPage = true;
			iRegStudent = true; iRegAdvisor = true; iRegAdmin = true;
			iEnrlStudent = true; iEnrlAdvisor = true; iEnrlAdmin = true;
			iWaitList = true; iEmail = true;
		}
		
		public void setCourseTypes(String courseTypes) { iCourseTypes = courseTypes; }
		public boolean hasCourseTypes() { return iCourseTypes != null && !iCourseTypes.isEmpty(); }
		public String getCourseTypes() { return iCourseTypes; }
		
		public void setEffectiveStart(String start) { iEffectiveStart = start; }
		public boolean hasEffectiveStart() { return iEffectiveStart != null && !iEffectiveStart.isEmpty(); }
		public String getEffectiveStart() { return iEffectiveStart; }
		
		public void setEffectiveStop(String stop) { iEffectiveStop = stop; }
		public boolean hasEffectiveStop() { return iEffectiveStop != null && !iEffectiveStop.isEmpty(); }
		public String getEffectiveStop() { return iEffectiveStop; }
		
		public void setMessage(String message) { iMessage = message; }
		public boolean hasMessage() { return iMessage != null && !iMessage.isEmpty(); }
		public String getMessage() { return iMessage; }
		
		@Override
		public String toString() { return getReference(); }
		
		@Override
		public int hashCode() { return getReference().hashCode(); }
		
		@Override
		public int compareTo(StudentStatusInfo status) {
			return getReference().compareTo(status.getReference());
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof StudentStatusInfo)) return false;
			return getUniqueId().equals(((StudentStatusInfo)o).getUniqueId());
		}
	}

}
