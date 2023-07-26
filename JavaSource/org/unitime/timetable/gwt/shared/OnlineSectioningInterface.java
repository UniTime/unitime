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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

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
		private GradeModes iGradeModes = null;
		private Float iMaxCredit = null;
		private Set<Long> iAdvisorWaitListedCourseIds = null;
		
		public static enum EligibilityFlag implements IsSerializable {
			IS_ADMIN, IS_ADVISOR, IS_GUEST,
			CAN_USE_ASSISTANT,
			CAN_ENROLL,
			PIN_REQUIRED,
			CAN_WAITLIST, CAN_NO_SUBS,
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
			CAN_REQUIRE, CAN_CHANGE_GRADE_MODE, CAN_CHANGE_VAR_CREDIT, CAN_REQUEST_VAR_TITLE_COURSE,
			SR_CHANGE_NOTE,
			HAS_ADVISOR_REQUESTS,
			SR_LINK_CONF, SR_EXTENDED,
			WAIT_LIST_VALIDATION,
			SHOW_SCHEDULING_PREFS,
			SR_NOTE_PER_COURSE,
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
		
		public boolean hasMaxCredit() { return iMaxCredit != null && iMaxCredit > 0f; }
		public void setMaxCredit(Float maxCredit) { iMaxCredit = maxCredit; }
		public Float getMaxCredit() { return iMaxCredit; }
		
		public boolean hasCurrentCredit() {
			return iGradeModes != null && iGradeModes.hasCurrentCredit();
		}
		public void setCurrentCredit(Float curCredit) {
			if (iGradeModes == null) iGradeModes = new GradeModes();
			iGradeModes.setCurrentCredit(curCredit);
		}
		public Float getCurrentCredit() {
			if (iGradeModes == null) return null;
			return iGradeModes.getCurrentCredit();
		}
		
		public boolean hasOverride(String errorCode) {
			if (errorCode == null || errorCode.isEmpty()) return true;
			return iOverrides != null && iOverrides.contains(errorCode);
		}
		public void setOverrides(Collection<String> overrides) {
			iOverrides = (overrides == null ? null : new HashSet<String>(overrides));
		}
		
		public boolean hasGradeModes() {
			return iGradeModes != null && iGradeModes.hasGradeModes();
		}
		public void addGradeMode(String sectionId, String code, String label, boolean honor) {
			if (iGradeModes == null) iGradeModes = new GradeModes();
			iGradeModes.addGradeMode(sectionId, new GradeMode(code, label, honor));
		}
		public GradeMode getGradeMode(ClassAssignmentInterface.ClassAssignment section) {
			if (iGradeModes == null) return null;
			return iGradeModes.getGradeMode(section);
		}
		public GradeModes getGradeModes() { return iGradeModes; }
		
		public boolean hasCreditHours() {
			return iGradeModes != null && iGradeModes.hasCreditHours();
		}
		public Float getCreditHour(ClassAssignmentInterface.ClassAssignment section) {
			if (iGradeModes == null || section == null) return null;
			return iGradeModes.getCreditHour(section);
		}
		public void addCreditHour(String sectionId, Float credit) {
			if (iGradeModes == null) iGradeModes = new GradeModes();
			iGradeModes.addCreditHour(sectionId, credit);
		}
		
		public Set<Long> getAdvisorWaitListedCourseIds() { return iAdvisorWaitListedCourseIds; }
		public void setAdvisorWaitListedCourseIds(Set<Long> advisorWaitListedCourseIds) { iAdvisorWaitListedCourseIds = advisorWaitListedCourseIds; } 
	}
	
	public static class SectioningProperties implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iSessionId = null;
		private boolean iAdmin = false, iAdvisor = false;
		private boolean iEmail = false, iMassCancel = false, iChangeStatus = false;
		private boolean iRequestUpdate = false;
		private boolean iReloadStudent = false;
		private boolean iChangeLog = false;
		private boolean iCheckStudentOverrides = false;
		private boolean iValidateStudentOverrides = false;
		private boolean iRecheckCriticalCourses = false;
		private boolean iAdvisorCourseRequests = false;
		private Set<StudentGroupInfo> iEditableGroups = null;
		private String iEmailOptionalToggleCaption = null;
		private boolean iEmailOptionalToggleDefault = false;
		
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
		
		public void setReloadStudent(boolean reloadStudent) { iReloadStudent = reloadStudent; }
		public boolean isReloadStudent() { return iReloadStudent; }
		
		public void setCheckStudentOverrides(boolean checkOverrides) { iCheckStudentOverrides = checkOverrides; }
		public boolean isCheckStudentOverrides() { return iCheckStudentOverrides; }
		
		public void setValidateStudentOverrides(boolean validateOverrides) { iValidateStudentOverrides = validateOverrides; }
		public boolean isValidateStudentOverrides() { return iValidateStudentOverrides; }
		
		public void setRecheckCriticalCourses(boolean recheckCriticalCourses) { iRecheckCriticalCourses = recheckCriticalCourses; }
		public boolean isRecheckCriticalCourses() { return iRecheckCriticalCourses; }
		
		public void setAdvisorCourseRequests(boolean advisorCourseRequests) { iAdvisorCourseRequests = advisorCourseRequests; }
		public boolean isAdvisorCourseRequests() { return iAdvisorCourseRequests; }
		
		public void setChangeLog(boolean changeLog) { iChangeLog = changeLog; }
		public boolean isChangeLog() { return iChangeLog; }
		
		public boolean isCanSelectStudent() {
			return iEmail || iMassCancel || iChangeStatus || iRequestUpdate || iCheckStudentOverrides || iValidateStudentOverrides || iReloadStudent;
		}
		
		public boolean hasEditableGroups() { return iEditableGroups != null && !iEditableGroups.isEmpty(); }
		public Set<StudentGroupInfo> getEditableGroups() { return iEditableGroups; }
		public void setEditableGroups(Set<StudentGroupInfo> groups) { iEditableGroups = groups; }
		public void addEditableGroup(StudentGroupInfo group) {
			if (iEditableGroups == null) iEditableGroups = new TreeSet<StudentGroupInfo>();
			iEditableGroups.add(group);
		}
		
		public boolean hasEmailOptionalToggleCaption() { return iEmailOptionalToggleCaption != null && !iEmailOptionalToggleCaption.isEmpty(); }
		public String getEmailOptionalToggleCaption() { return iEmailOptionalToggleCaption; }
		public void setEmailOptionalToggleCaption(String captionIfOptional) { iEmailOptionalToggleCaption = captionIfOptional; }
		
		public boolean getEmailOptionalToggleDefault() { return iEmailOptionalToggleDefault; }
		public void setEmailOptionalToggleDefault(boolean defaultValue) { iEmailOptionalToggleDefault = defaultValue; }
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
		private boolean iWaitList = false, iNoSubs = false, iEmail = false, iCanRequire = false;
		private boolean iSpecReg = false, iReqValidation = false, iNoSchedule = false;
		private boolean iReSchedule = false;
		private String iCourseTypes;
		private String iEffectiveStart, iEffectiveStop;
		private String iMessage;
		private String iFallback;
		private boolean iCanUseAssitant = false, iCanRegister = false;
		
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
		public void setNoSubs(boolean noSubs) { iNoSubs = noSubs; }
		public boolean isNoSubs() { return iNoSubs; }
		public void setEmail(boolean email) { iEmail = email; }
		public boolean isEmail() { return iEmail; }
		public void setCanRequire(boolean canRequire) { iCanRequire = canRequire; }
		public boolean isCanRequire() { return iCanRequire; }
		
		public void setSpecialRegistration(boolean specReg) { iSpecReg = specReg; }
		public boolean isSpecialRegistration() { return iSpecReg; }
		public void setRequestValiadtion(boolean reqVal) { iReqValidation = reqVal; }
		public boolean isRequestValiadtion() { return iReqValidation; }
		public void setNoSchedule(boolean noSchedule) { iNoSchedule = noSchedule; }
		public boolean isNoSchedule() { return iNoSchedule; }
		public void setReSchedule(boolean reSchedule) { iReSchedule = reSchedule; }
		public boolean isReSchedule() { return iReSchedule; }
		
		public void setAllEnabled() {
			iAssistantPage = true;
			iRequestsPage = true;
			iRegStudent = true; iRegAdvisor = true; iRegAdmin = true;
			iEnrlStudent = true; iEnrlAdvisor = true; iEnrlAdmin = true;
			iWaitList = true; iEmail = true; iCanRequire = true;
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
		
		public void setFallback(String fallback) { iFallback = fallback; }
		public boolean hasFallback() { return iFallback != null && !iFallback.isEmpty(); }
		public String getFallback() { return iFallback; }
		
		public void setCanUseAssistant(boolean canUseAssistant) { iCanUseAssitant = canUseAssistant; }
		public boolean isCanUseAssistant() { return iCanUseAssitant; }
		
		public void setCanRegister(boolean canRegister) { iCanRegister = canRegister; }
		public boolean isCanRegister() { return iCanRegister; }
		
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
			StudentStatusInfo s = (StudentStatusInfo)o;
			return (getUniqueId() == null ? -1l : getUniqueId()) == (s.getUniqueId() == null ? -1l : s.getUniqueId());
		}
	}
	
	public static class GradeModes implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		Map<String, GradeMode> iModes = new HashMap<String, GradeMode>();
		private Map<String, Float> iCreditHours = new HashMap<String, Float>();
		private Float iCurrentCredit = null;
		
		public GradeModes() {}
		
		public boolean hasGradeModes() { return !iModes.isEmpty(); }
		
		public void addGradeMode(String sectionId, GradeMode mode) {
			iModes.put(sectionId, mode);
		}
		
		public GradeMode getGradeMode(ClassAssignmentInterface.ClassAssignment a) {
			if (a.getExternalId() == null) return null;
			if (a.getParentSection() != null && a.getParentSection().equals(a.getSection())) return null;
			return iModes.get(a.getExternalId());
		}
		
		public GradeMode getGradeMode(String sectionId) {
			if (iModes == null) return null;
			return iModes.get(sectionId);
		}
		
		public Map<String, GradeMode> toMap() { return iModes; }
		
		public Map<String, Float> getCreditHours() { return iCreditHours; }
		
		public boolean hasCreditHours() {
			return iCreditHours != null && !iCreditHours.isEmpty();
		}
		
		public Float getCreditHour(ClassAssignmentInterface.ClassAssignment a) {
			if (a.getExternalId() == null) return null;
			if (a.getParentSection() != null && a.getParentSection().equals(a.getSection())) return null;
			return iCreditHours.get(a.getExternalId());
		}
		
		public Float getCreditHour(String sectionId) {
			if (iCreditHours == null) return null;
			return iCreditHours.get(sectionId);
		}
		
		public void addCreditHour(String sectionId, Float credit) {
			if (credit == null)
				iCreditHours.remove(sectionId);
			else
				iCreditHours.put(sectionId, credit);
		}

		public boolean hasCurrentCredit() { return iCurrentCredit != null && iCurrentCredit > 0f; }
		public void setCurrentCredit(Float curCredit) { iCurrentCredit = curCredit; }
		public Float getCurrentCredit() { return iCurrentCredit; }
	}
		
	public static class GradeMode implements IsSerializable, Serializable, Comparable<GradeMode> {
		private static final long serialVersionUID = 1L;
		private String iCode;
		private String iLabel;
		private boolean iHonor;
		
		public GradeMode() {}
		public GradeMode(String code, String label, boolean honor) {
			iCode = code; iLabel = label;
			iHonor = honor;
		}
		
		public void setCode(String code) { iCode = code; }
		public String getCode() { return iCode; }
		public void setLabel(String label) { iLabel = label; }
		public String getLabel() { return iLabel; }
		public void setHonor(boolean honor) { iHonor = honor; }
		public boolean isHonor() { return iHonor; }
		
		@Override
		public int hashCode() { return getCode().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof GradeMode)) return false;
			return getCode().equals(((GradeMode)o).getCode());
		}

		@Override
		public int compareTo(GradeMode m) {
			return getLabel().compareToIgnoreCase(m.getLabel());
		}
	}
	
	public static class StudentInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iStudentId;
		private Long iSessionId;
		private String iStudentName;
		private String iStudentExternalId;
		private String iStudentEmail;
		private String iSessionName;
		
		public StudentInfo() {}
		
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String getStudentName() { return iStudentName; }
		public void setStudentName(String name) { iStudentName = name; }
		public String getStudentExternalId() { return iStudentExternalId; }
		public void setStudentExternalId(String id) { iStudentExternalId = id; }
		public String getStudentEmail() { return iStudentEmail; }
		public void setStudentEmail(String email) { iStudentEmail = email; }
		
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String name) { iSessionName = name; }
	}
	
	public static class AdvisingStudentDetails implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iStudentId;
		private Long iSessionId;
		private String iStudentName;
		private String iStudentExternalId;
		private String iStudentEmail;
		private String iSessionName;
		private String iAdvisorEmail;
		private StudentStatusInfo iCurrentStatus;
		private Set<StudentStatusInfo> iAvailableStatuses;
		private boolean iCanUpdate, iDegreePlan, iCanEmail = false;
		private CourseRequestInterface iRequest = null;
		private CourseRequestInterface iStudentRequest = null;
		private String iEmailOptionalToggleCaption = null;
		private boolean iEmailOptionalToggleDefault = false;
		private WaitListMode iMode = null;
		private boolean iCanRequire = false;
		private Set<Long> iAdvisorWaitListedCourseIds = null;
		private Integer iCriticalCheck = null;
		
		public AdvisingStudentDetails() {}
		public AdvisingStudentDetails(AdvisingStudentDetails clone) {
			iSessionId = clone.iSessionId;
			iStudentId = clone.iStudentId;
			iStudentName = clone.iStudentName;
			iStudentExternalId = clone.iStudentExternalId;
			iStudentEmail = clone.iStudentEmail;
			iSessionName = clone.iSessionName;
			iAdvisorEmail = clone.iAdvisorEmail;
			iCurrentStatus = clone.iCurrentStatus;
			iCanUpdate = clone.iCanUpdate;
			iDegreePlan = clone.iDegreePlan;
			iCanEmail = clone.iCanEmail;
			iEmailOptionalToggleCaption = clone.iEmailOptionalToggleCaption;
			iEmailOptionalToggleDefault = clone.iEmailOptionalToggleDefault;
			iMode = clone.iMode;
			iCanRequire = clone.iCanRequire;
			iAvailableStatuses = clone.iAvailableStatuses; 
			iStudentRequest = clone.iStudentRequest;
			iAdvisorWaitListedCourseIds = clone.iAdvisorWaitListedCourseIds;
			iCriticalCheck = clone.iCriticalCheck;
		}
		
		public Long getStudentId() { return iStudentId; }
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public String getStudentName() { return iStudentName; }
		public void setStudentName(String name) { iStudentName = name; }
		public String getStudentExternalId() { return iStudentExternalId; }
		public void setStudentExternalId(String id) { iStudentExternalId = id; }
		public String getStudentEmail() { return iStudentEmail; }
		public void setStudentEmail(String email) { iStudentEmail = email; }
		
		public String getSessionName() { return iSessionName; }
		public void setSessionName(String name) { iSessionName = name; }
		
		public boolean hasAdvisorEmail() { return iAdvisorEmail != null && !iAdvisorEmail.isEmpty(); }
		public String getAdvisorEmail() { return iAdvisorEmail; }
		public void setAdvisorEmail(String email) { iAdvisorEmail = email; }
		
		public StudentStatusInfo getStatus() { return iCurrentStatus; }
		public void setStatus(StudentStatusInfo status) { iCurrentStatus = status; }
		public StudentStatusInfo getStatus(String reference) {
			if (reference == null) return null;
			if (iCurrentStatus != null && iCurrentStatus.getReference().equals(reference)) return iCurrentStatus;
			if (iAvailableStatuses != null)
				for (StudentStatusInfo info: iAvailableStatuses)
					if (reference.equals(info.getReference())) return info;
			return null;
		}
		
		public boolean hasStatuses() { return iAvailableStatuses != null && !iAvailableStatuses.isEmpty(); }
		public void addStatus(StudentStatusInfo status) {
			if (iAvailableStatuses == null) iAvailableStatuses = new TreeSet<StudentStatusInfo>();
			iAvailableStatuses.add(status);
		}
		public Set<StudentStatusInfo> getStatuses() { return iAvailableStatuses; }
		
		public boolean isCanUpdate() { return iCanUpdate; }
		public void setCanUpdate(boolean canUpdate) { iCanUpdate = canUpdate; }
		
		public boolean isCanEmail() { return iCanEmail; }
		public void setCanEmail(boolean canEmail) { iCanEmail = canEmail; }
		
		public boolean isDegreePlan() { return iDegreePlan; }
		public void setDegreePlan(boolean dp) { iDegreePlan = dp; }
		
		public CourseRequestInterface getRequest() { return iRequest; }
		public void setRequest(CourseRequestInterface request) { iRequest = request; }
		
		public CourseRequestInterface getStudentRequest() { return iStudentRequest; }
		public boolean hasStudentRequest() { return iStudentRequest != null && !iStudentRequest.isEmpty(); }
		public void setStudentRequest(CourseRequestInterface request) { iStudentRequest = request; }
		
		public boolean hasEmailOptionalToggleCaption() { return iEmailOptionalToggleCaption != null && !iEmailOptionalToggleCaption.isEmpty(); }
		public String getEmailOptionalToggleCaption() { return iEmailOptionalToggleCaption; }
		public void setEmailOptionalToggleCaption(String captionIfOptional) { iEmailOptionalToggleCaption = captionIfOptional; }
		
		public boolean getEmailOptionalToggleDefault() { return iEmailOptionalToggleDefault; }
		public void setEmailOptionalToggleDefault(boolean defaultValue) { iEmailOptionalToggleDefault = defaultValue; }
		
		public WaitListMode getWaitListMode() {
			if (iMode == null) return WaitListMode.None;
			return iMode;
		}
		public void setWaitListMode(WaitListMode mode) { iMode = mode; }
		
		public boolean isCanRequire() { return iCanRequire; }
		public void setCanRequire(boolean canRequire) { iCanRequire = canRequire; }
		
		public Set<Long> getAdvisorWaitListedCourseIds() { return iAdvisorWaitListedCourseIds; }
		public void setAdvisorWaitListedCourseIds(Set<Long> advisorWaitListedCourseIds) { iAdvisorWaitListedCourseIds = advisorWaitListedCourseIds; }
		
		public void setCriticalCheck(Integer check) { iCriticalCheck = check; }
		public Integer getCriticalCheck() { return iCriticalCheck; }
		public boolean hasCriticalCheck() { return iCriticalCheck != null && iCriticalCheck > 0; }
		public boolean isCriticalCheckCritical() { return iCriticalCheck != null && iCriticalCheck.intValue() == 1; }
		public boolean isCriticalCheckImportant() { return iCriticalCheck != null && iCriticalCheck.intValue() == 2; }
		public boolean isCriticalCheckVital() { return iCriticalCheck != null && iCriticalCheck.intValue() == 3; }
	}
	
	public static class AdvisorCourseRequestSubmission implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private byte[] iPdf;
		private boolean iUpdated = false;
		private String iName = null;
		private String iLink = null;
		
		public AdvisorCourseRequestSubmission() {}
		
		public byte[] getPdf() { return iPdf; }
		public void setPdf(byte[] pdf) { iPdf = pdf; }
		
		public String getName() { return iName; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public void setName(String name) { iName = name; }
		
		public String getLink() { return iLink; }
		public boolean hasLink() { return iLink != null && !iLink.isEmpty(); }
		public void setLink(String link) { iLink = link; }
		
		public boolean isUpdated() { return iUpdated; }
		public void setUpdated(boolean updated) { iUpdated = updated; }
	}
	
	public static class StudentSectioningContext implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private Boolean iOnline = null, iSectioning = null;
		private Long iSessionId = null, iStudentId = null;
		private String iPin = null;
		private List<SessionMonth> iSessionDates = null;
		
		public StudentSectioningContext() {}
		public StudentSectioningContext(StudentSectioningContext cx) {
			iOnline = cx.iOnline;
			iSectioning = cx.iSectioning;
			iSessionId = cx.iSessionId;
			iStudentId = cx.iStudentId;
			iPin = cx.iPin;
		}
		
		public void setSessionId(Long sessionId) { iSessionId = sessionId; iSessionDates = null; }
		public Long getSessionId() { return iSessionId; }
		public Long getAcademicSessionId() { return iSessionId; }
		public void setAcademicSessionId(Long sessionId) { iSessionId = sessionId; }
		
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public Long getStudentId() { return iStudentId; }
		
		public boolean hasOnline() { return iOnline != null; }
		public boolean isOnline() { return iOnline != null && iOnline.booleanValue(); }
		public void setOnline(boolean online) { iOnline = online; }
		
		public boolean hasSectioning() { return iSectioning != null; }
		public boolean isSectioning() { return iSectioning != null && iSectioning.booleanValue(); }
		public void setSectioning(boolean sectioning) { iSectioning = sectioning; }
		
		public void setPin(String pin) { iPin = pin; }
		public String getPin() { return iPin; }
		public boolean hasPin() { return iPin != null && !iPin.isEmpty(); }
		
		public void setSessionDates(List<SessionMonth> sessionDates) { iSessionDates = sessionDates; }
		public boolean hasSessionDates() { return iSessionDates != null; }
		public List<SessionMonth> getSessionDates() { return iSessionDates; }
	}
	
	public static enum WaitListMode implements IsSerializable, Serializable {
		WaitList, NoSubs, None
	};
	
	public static class AdvisorNote implements IsSerializable, Serializable  {
		private static final long serialVersionUID = 1L;
		private String iDisplayString;
		private String iReplaceString;
		private int iCount;
		private Date iTimeStamp;
		
		public AdvisorNote() {}
		
		public String getDisplayString() { return iDisplayString; }
		public void setDisplayString(String displayString) { iDisplayString = displayString; }
		public String getReplaceString() { return iReplaceString; }
		public void setReplaceString(String replaceString) { iReplaceString = replaceString; }
		
		public int getCount() { return iCount; }
		public void setCount(int count) { iCount = count; }
		
		public Date getTimeStamp() { return iTimeStamp; }
		public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }
	}
}
