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

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

public class RollForwardSessionInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1656335026565427380L;
	private Long iSessionToRollForwardTo;
	private boolean iRollForwardDepartments = false;
	private Long iSessionToRollDeptsFowardFrom;
	private boolean iRollForwardSessionConfig = false;
	private Long iSessionToRollSessionConfigForwardFrom;
	private boolean iRollForwardManagers = false;
	private Long iSessionToRollManagersForwardFrom;
	private boolean iRollForwardRoomData = false;
	private Long iSessionToRollRoomDataForwardFrom;
	private boolean iRollForwardDatePatterns = false;
	private Long iSessionToRollDatePatternsForwardFrom;
	private boolean iRollForwardTimePatterns = false;
	private Long iSessionToRollTimePatternsForwardFrom;
	private boolean iRollForwardLearningManagementSystems = false;
	private Long iSessionToRollLearningManagementSystemsForwardFrom;
	private boolean iRollForwardSubjectAreas = false;
	private Long iSessionToRollSubjectAreasForwardFrom;
	private boolean iRollForwardInstructorData = false;
	private Long iSessionToRollInstructorDataForwardFrom;
	private List<Long> iRollForwardDepartmentIds = new ArrayList<Long>();
	private boolean iRollForwardCourseOfferings = false;
	private Long iSessionToRollCourseOfferingsForwardFrom;
	private List<Long> iRollForwardSubjectAreaIds = new ArrayList<Long>();
	private boolean iRollForwardWaitListsProhibitedOverrides = false;
	private boolean iRollForwardParentOfferings = false;
	private RollAction iSubpartTimePrefsAction = RollAction.ROLL_PREFS_ACTION;
	private RollAction iSubpartLocationPrefsAction = RollAction.ROLL_PREFS_ACTION;
	private RollAction iClassPrefsAction = RollAction.DO_NOT_ROLL_ACTION;
	private DistributionMode iRollForwardDistributions = DistributionMode.MIXED;
	private CancelledClassAction iCancelledClassAction = CancelledClassAction.REOPEN;
	private boolean iRollForwardClassInstructors = false;
	private List<Long> iRollForwardClassInstrSubjectIds = new ArrayList<Long>();
	private boolean iRollForwardOfferingCoordinators = false;
	private List<Long> iRollForwardOfferingCoordinatorsSubjectIds = new ArrayList<Long>();
	private boolean iRollForwardTeachingRequests = false;
	private List<Long> iRollForwardTeachingRequestsSubjectIds = new ArrayList<Long>();
	private boolean iAddNewCourseOfferings = false;
	private List<Long> iAddNewCourseOfferingsSubjectIds = new ArrayList<Long>();
	private boolean iRollForwardExamConfiguration = false;
	private Long iSessionToRollExamConfigurationForwardFrom;
	private boolean iRollForwardMidtermExams = false;
	private RollAction iMidtermExamsPrefsAction = RollAction.EXAMS_ROOM_PREFS;
	private boolean iRollForwardFinalExams = false;
	private RollAction iFinalExamsPrefsAction = RollAction.EXAMS_ROOM_PREFS;
	private boolean iRollForwardStudents = false;
	private StudentEnrollmentMode iRollForwardStudentsMode = StudentEnrollmentMode.STUDENT_COURSE_REQUESTS;
	private Long iPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom;
	private boolean iRollForwardCurricula = false;
	private Long iSessionToRollCurriculaForwardFrom;
	private boolean iRollForwardReservations = false;
	private Long iSessionToRollReservationsForwardFrom;
	private List<Long> iRollForwardReservationsSubjectIds = new ArrayList<Long>();
	private boolean iRollForwardCourseReservations = false;
	private Date iStartDateCourseReservations;
	private Date iExpirationCourseReservations;
	private boolean iRollForwardCurriculumReservations = false;
	private Date iStartDateCurriculumReservations;
	private Date iExpirationCurriculumReservations;
	private boolean iRollForwardGroupReservations = false;
	private Date iStartDateGroupReservations;
	private Date iExpirationGroupReservations;
	private boolean iCreateStudentGroupsIfNeeded = false;
	private boolean iRollForwardUniversalReservations = false;
	private Date iStartDateUniversalReservations;
	private Date iExpirationUniversalReservations;
	private boolean iRollForwardPeriodicTasks = false;
	private Long iSessionToRollPeriodicTasksFrom;
	
	public Long getSessionToRollForwardTo() { return iSessionToRollForwardTo; }
	public void setSessionToRollForwardTo(Long sessionToRollForwardTo) { iSessionToRollForwardTo = sessionToRollForwardTo; }
	
	public boolean getRollForwardDepartments() { return iRollForwardDepartments; }
	public void setRollForwardDepartments(boolean rollForwardDepartments) { iRollForwardDepartments = rollForwardDepartments; }
	public Long getSessionToRollDeptsFowardFrom() { return iSessionToRollDeptsFowardFrom; }
	public void setSessionToRollDeptsFowardFrom(Long sessionToRollDeptsFowardFrom) { iSessionToRollDeptsFowardFrom = sessionToRollDeptsFowardFrom; }
	
	public boolean getRollForwardSessionConfig() { return iRollForwardSessionConfig; }
    public void setRollForwardSessionConfig(boolean rollForwardSessionConfig) { iRollForwardSessionConfig = rollForwardSessionConfig; }
    public Long getSessionToRollSessionConfigForwardFrom() { return iSessionToRollSessionConfigForwardFrom; }
    public void setSessionToRollSessionConfigForwardFrom(Long sessionToRollSessionConfigForwardFrom) { iSessionToRollSessionConfigForwardFrom = sessionToRollSessionConfigForwardFrom; }

	public boolean getRollForwardManagers() { return iRollForwardManagers; }
	public void setRollForwardManagers(boolean rollForwardManagers) { iRollForwardManagers = rollForwardManagers; }
	public Long getSessionToRollManagersForwardFrom() { return iSessionToRollManagersForwardFrom; }
	public void setSessionToRollManagersForwardFrom(Long sessionToRollManagersForwardFrom) {
		iSessionToRollManagersForwardFrom = sessionToRollManagersForwardFrom;
	}

	public boolean getRollForwardRoomData() { return iRollForwardRoomData; }
	public void setRollForwardRoomData(boolean rollForwardRoomData) { iRollForwardRoomData = rollForwardRoomData; }
	public Long getSessionToRollRoomDataForwardFrom() { return iSessionToRollRoomDataForwardFrom; }
	public void setSessionToRollRoomDataForwardFrom(Long sessionToRollRoomDataForwardFrom) {
		iSessionToRollRoomDataForwardFrom = sessionToRollRoomDataForwardFrom;
	}

	public boolean getRollForwardDatePatterns() { return iRollForwardDatePatterns; }
	public void setRollForwardDatePatterns(boolean rollForwardDatePatterns) { iRollForwardDatePatterns = rollForwardDatePatterns; }
	public Long getSessionToRollDatePatternsForwardFrom() { return iSessionToRollDatePatternsForwardFrom; }
	public void setSessionToRollDatePatternsForwardFrom(Long sessionToRollDatePatternsForwardFrom) {
		iSessionToRollDatePatternsForwardFrom = sessionToRollDatePatternsForwardFrom;
	}

	public boolean getRollForwardTimePatterns() { return iRollForwardTimePatterns;}
	public void setRollForwardTimePatterns(boolean rollForwardTimePatterns) { iRollForwardTimePatterns = rollForwardTimePatterns; }
	public Long getSessionToRollTimePatternsForwardFrom() { return iSessionToRollTimePatternsForwardFrom; }
	public void setSessionToRollTimePatternsForwardFrom(Long sessionToRollTimePatternsForwardFrom) {
		iSessionToRollTimePatternsForwardFrom = sessionToRollTimePatternsForwardFrom;
	}
	
	public boolean getRollForwardLearningManagementSystems() { return iRollForwardLearningManagementSystems; }
	public void setRollForwardLearningManagementSystems(boolean rollForwardLearningManagementSystems) { iRollForwardLearningManagementSystems = rollForwardLearningManagementSystems; }
	public Long getSessionToRollLearningManagementSystemsForwardFrom() { return iSessionToRollLearningManagementSystemsForwardFrom; }
	public void setSessionToRollLearningManagementSystemsForwardFrom(Long sessionToRollLearningManagementSystemsForwardFrom) {
		iSessionToRollLearningManagementSystemsForwardFrom = sessionToRollLearningManagementSystemsForwardFrom;
	}
	
	public boolean getRollForwardSubjectAreas() { return iRollForwardSubjectAreas; }
	public void setRollForwardSubjectAreas(boolean rollForwardSubjectAreas) { iRollForwardSubjectAreas = rollForwardSubjectAreas; }
	public Long getSessionToRollSubjectAreasForwardFrom() { return iSessionToRollSubjectAreasForwardFrom; }
	public void setSessionToRollSubjectAreasForwardFrom(Long sessionToRollCourseOfferingsForwardFrom) {
		iSessionToRollSubjectAreasForwardFrom = sessionToRollCourseOfferingsForwardFrom;
	}
	
	public boolean getRollForwardInstructorData() { return iRollForwardInstructorData; }
	public void setRollForwardInstructorData(boolean rollForwardInstructorData) { iRollForwardInstructorData = rollForwardInstructorData; }
	public Long getSessionToRollInstructorDataForwardFrom() { return iSessionToRollInstructorDataForwardFrom; }
	public void setSessionToRollInstructorDataForwardFrom(Long sessionToRollInstructorDataForwardFrom) {
		iSessionToRollInstructorDataForwardFrom = sessionToRollInstructorDataForwardFrom;
	}
	public boolean hasRollForwardDepartmentIds() { return iRollForwardDepartmentIds != null && !iRollForwardDepartmentIds.isEmpty(); }
	public boolean hasRollForwardDepartmentId(Long id) { return iRollForwardDepartmentIds != null && iRollForwardDepartmentIds.contains(id); }
	public List<Long> getRollForwardDepartmentIds() { return iRollForwardDepartmentIds; }
	public void addRollForwardDepartmentId(Long id) {
		if (iRollForwardDepartmentIds == null) iRollForwardDepartmentIds = new ArrayList<Long>();
		iRollForwardDepartmentIds.add(id);
	}
	public void removeRollForwardDepartmentId(Long id) {
		if (iRollForwardDepartmentIds == null) iRollForwardDepartmentIds = new ArrayList<Long>();
		iRollForwardDepartmentIds.remove(id);
	}
	public void setRollForwardDepartmentIds(Collection<Long> rollForwardDepartmentIds) {
		if (iRollForwardDepartmentIds == null) iRollForwardDepartmentIds = new ArrayList<Long>();
		iRollForwardDepartmentIds.clear();
		if (rollForwardDepartmentIds != null) iRollForwardDepartmentIds.addAll(rollForwardDepartmentIds);
	}
	
	public boolean getRollForwardCourseOfferings() { return iRollForwardCourseOfferings; }
	public void setRollForwardCourseOfferings(boolean rollForwardCourseOfferings) { iRollForwardCourseOfferings = rollForwardCourseOfferings; }
	public Long getSessionToRollCourseOfferingsForwardFrom() { return iSessionToRollCourseOfferingsForwardFrom; }
	public void setSessionToRollCourseOfferingsForwardFrom(Long sessionToRollCourseOfferingsForwardFrom) {
		iSessionToRollCourseOfferingsForwardFrom = sessionToRollCourseOfferingsForwardFrom;
	}
	public boolean hasRollForwardSubjectAreaIds() { return iRollForwardSubjectAreaIds != null && !iRollForwardSubjectAreaIds.isEmpty(); }
	public boolean hasRollForwardSubjectAreaId(Long id) { return iRollForwardSubjectAreaIds != null && iRollForwardSubjectAreaIds.contains(id); }
	public List<Long> getRollForwardSubjectAreaIds() { return iRollForwardSubjectAreaIds; }
	public void addRollForwardSubjectAreaId(Long id) {
		if (iRollForwardSubjectAreaIds == null) iRollForwardSubjectAreaIds = new ArrayList<Long>();
		iRollForwardSubjectAreaIds.add(id);
	}
	public void removeRollForwardSubjectAreaId(Long id) {
		if (iRollForwardSubjectAreaIds == null) iRollForwardSubjectAreaIds = new ArrayList<Long>();
		iRollForwardSubjectAreaIds.remove(id);
	}
	public void setRollForwardSubjectAreaIds(List<Long> rollForwardSubjectAreaIds) {
		iRollForwardSubjectAreaIds = rollForwardSubjectAreaIds;
	}
	public boolean getRollForwardWaitListsProhibitedOverrides() { return iRollForwardWaitListsProhibitedOverrides; }
	public void setRollForwardWaitListsProhibitedOverrides(boolean rollForwardWaitListsProhibitedOverrides) {
		iRollForwardWaitListsProhibitedOverrides = rollForwardWaitListsProhibitedOverrides;
	}
	public boolean getRollForwardParentOfferings() { return iRollForwardParentOfferings; }
	public void setRollForwardParentOfferings(boolean rollForwardParentOfferings) { iRollForwardParentOfferings = rollForwardParentOfferings; }

	public RollAction getSubpartTimePrefsAction() { return iSubpartTimePrefsAction; }
	public void setSubpartTimePrefsAction(RollAction subpartTimePrefsAction) { iSubpartTimePrefsAction = subpartTimePrefsAction; }
	public RollAction getSubpartLocationPrefsAction() { return iSubpartLocationPrefsAction; }
	public void setSubpartLocationPrefsAction(RollAction subpartLocationPrefsAction) { iSubpartLocationPrefsAction = subpartLocationPrefsAction; }
	public RollAction getClassPrefsAction() { return iClassPrefsAction; }
	public void setClassPrefsAction(RollAction classPrefsAction) { iClassPrefsAction = classPrefsAction; }
	public DistributionMode getRollForwardDistributions() { return iRollForwardDistributions; }
	public void setRollForwardDistributions(DistributionMode rollForwardDistributions) { iRollForwardDistributions = rollForwardDistributions; }
	public CancelledClassAction getCancelledClassAction() { return iCancelledClassAction; }
	public void setCancelledClassAction(CancelledClassAction cancelledClassAction) { iCancelledClassAction = cancelledClassAction; }
	
	public boolean getRollForwardClassInstructors() { return iRollForwardClassInstructors; }
	public void setRollForwardClassInstructors(boolean rollForwardClassInstructors) { iRollForwardClassInstructors = rollForwardClassInstructors; }
	public List<Long> getRollForwardClassInstrSubjectIds() { return iRollForwardClassInstrSubjectIds; }
	public void setRollForwardClassInstrSubjectIds(List<Long> rollForwardClassInstrSubjectIds) {
		iRollForwardClassInstrSubjectIds = rollForwardClassInstrSubjectIds;
	}

	public boolean getRollForwardOfferingCoordinators() { return iRollForwardOfferingCoordinators; }
	public void setRollForwardOfferingCoordinators(boolean rollForwardOfferingCoordinators) { iRollForwardOfferingCoordinators = rollForwardOfferingCoordinators; }
	public List<Long> getRollForwardOfferingCoordinatorsSubjectIds() { return iRollForwardOfferingCoordinatorsSubjectIds; }
	public void setRollForwardOfferingCoordinatorsSubjectIds(List<Long> rollForwardOfferingCoordinatorsSubjectIds) {
		iRollForwardOfferingCoordinatorsSubjectIds = rollForwardOfferingCoordinatorsSubjectIds;
	}
	
	public boolean getRollForwardTeachingRequests() { return iRollForwardTeachingRequests; }
	public void setRollForwardTeachingRequests(boolean rollForwardTeachingRequests) { iRollForwardTeachingRequests = rollForwardTeachingRequests; }
	public List<Long> getRollForwardTeachingRequestsSubjectIds() { return iRollForwardTeachingRequestsSubjectIds; }
	public void setRollForwardTeachingRequestsSubjectIds(List<Long> rollForwardTeachingRequestsSubjectIds) {
		iRollForwardTeachingRequestsSubjectIds = rollForwardTeachingRequestsSubjectIds;
	}
	
	public boolean getAddNewCourseOfferings() { return iAddNewCourseOfferings; }
	public void setAddNewCourseOfferings(boolean addNewCourseOfferings) { iAddNewCourseOfferings = addNewCourseOfferings; }
	public List<Long> getAddNewCourseOfferingsSubjectIds() { return iAddNewCourseOfferingsSubjectIds; }
	public void setAddNewCourseOfferingsSubjectIds(List<Long> addNewCourseOfferingsSubjectIds) { iAddNewCourseOfferingsSubjectIds = addNewCourseOfferingsSubjectIds; }
	
	public boolean getRollForwardExamConfiguration() { return iRollForwardExamConfiguration; }
	public void setRollForwardExamConfiguration(boolean rollForwardExamConfiguration) { iRollForwardExamConfiguration = rollForwardExamConfiguration; }
	public Long getSessionToRollExamConfigurationForwardFrom() { return iSessionToRollExamConfigurationForwardFrom; }
	public void setSessionToRollExamConfigurationForwardFrom(Long sessionToRollExamConfigurationForwardFrom) {
		iSessionToRollExamConfigurationForwardFrom = sessionToRollExamConfigurationForwardFrom;
	}
	
	public boolean getRollForwardMidtermExams() { return iRollForwardMidtermExams; }
	public void setRollForwardMidtermExams(boolean rollForwardMidtermExams) { iRollForwardMidtermExams = rollForwardMidtermExams; }
	public RollAction getMidtermExamsPrefsAction() { return iMidtermExamsPrefsAction; }
	public void setMidtermExamsPrefsAction(RollAction midtermExamsPrefsAction) { iMidtermExamsPrefsAction = midtermExamsPrefsAction; }
	public boolean getRollForwardFinalExams() { return iRollForwardFinalExams; }
	public void setRollForwardFinalExams(boolean rollForwardFinalExams) { iRollForwardFinalExams = rollForwardFinalExams; }
	public RollAction getFinalExamsPrefsAction() { return iFinalExamsPrefsAction; }
	public void setFinalExamsPrefsAction(RollAction finalExamsPrefsAction) { iFinalExamsPrefsAction = finalExamsPrefsAction; }

	public boolean getRollForwardStudents() { return iRollForwardStudents; }
	public void setRollForwardStudents(boolean rollForwardStudents) { iRollForwardStudents = rollForwardStudents; }
	public StudentEnrollmentMode getRollForwardStudentsMode() { return iRollForwardStudentsMode; }
	public void setRollForwardStudentsMode(StudentEnrollmentMode rollForwardStudentsMode) {
		iRollForwardStudentsMode = rollForwardStudentsMode;
	}
	public Long getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom() { return iPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom; }
	public void setPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom(Long pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom) {
		iPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom = pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom;
	}
	
	public boolean getRollForwardCurricula() { return iRollForwardCurricula; }
	public void setRollForwardCurricula(boolean rollForwardCurricula) { iRollForwardCurricula = rollForwardCurricula; }
	public Long getSessionToRollCurriculaForwardFrom() { return iSessionToRollCurriculaForwardFrom; }
	public void setSessionToRollCurriculaForwardFrom(Long sessionToRollCurriculaForwardFrom) { iSessionToRollCurriculaForwardFrom = sessionToRollCurriculaForwardFrom; }

	public boolean getRollForwardReservations() { return iRollForwardReservations; }
	public void setRollForwardReservations(boolean rollForwardReservations) { iRollForwardReservations = rollForwardReservations; }
	public Long getSessionToRollReservationsForwardFrom() { return iSessionToRollReservationsForwardFrom; }
	public void setSessionToRollReservationsForwardFrom(Long sessionToRollReservationsForwardFrom) { iSessionToRollReservationsForwardFrom = sessionToRollReservationsForwardFrom; }
	public List<Long> getRollForwardReservationsSubjectIds() { return iRollForwardReservationsSubjectIds; }
	public boolean hasRollForwardReservationsSubjectId(Long id) { return iRollForwardReservationsSubjectIds != null && iRollForwardReservationsSubjectIds.contains(id); }
	public void setRollForwardReservationsSubjectIds(List<Long> rollForwardReservationsSubjectIds) { iRollForwardReservationsSubjectIds = rollForwardReservationsSubjectIds; }

	public boolean getRollForwardCourseReservations() { return iRollForwardCourseReservations; }
	public void setRollForwardCourseReservations(boolean rollForwardCourseReservations) { iRollForwardCourseReservations = rollForwardCourseReservations; }
	public Date getStartDateCourseReservations() { return iStartDateCourseReservations; }
	public void setStartDateCourseReservations(Date startDateCourseReservations) { iStartDateCourseReservations = startDateCourseReservations; }
	public Date getExpirationCourseReservations() { return iExpirationCourseReservations; }
	public void setExpirationCourseReservations(Date expirationCourseReservations) { iExpirationCourseReservations = expirationCourseReservations; }

	public boolean getRollForwardCurriculumReservations() { return iRollForwardCurriculumReservations; }
	public void setRollForwardCurriculumReservations(boolean rollForwardCurriculumReservations) { iRollForwardCurriculumReservations = rollForwardCurriculumReservations; }
	public Date getStartDateCurriculumReservations() { return iStartDateCurriculumReservations; }
	public void setStartDateCurriculumReservations(Date startDateCurriculumReservations) { iStartDateCurriculumReservations = startDateCurriculumReservations; }
	public Date getExpirationCurriculumReservations() { return iExpirationCurriculumReservations; }
	public void setExpirationCurriculumReservations(Date expirationCurriculumReservations) { iExpirationCurriculumReservations = expirationCurriculumReservations; }

	public boolean getRollForwardGroupReservations() { return iRollForwardGroupReservations; }
	public void setRollForwardGroupReservations(boolean rollForwardGroupReservations) { iRollForwardGroupReservations = rollForwardGroupReservations; }
	public Date getStartDateGroupReservations() { return iStartDateGroupReservations; }
	public void setStartDateGroupReservations(Date startDateGroupReservations) { iStartDateGroupReservations = startDateGroupReservations; }
	public Date getExpirationGroupReservations() { return iExpirationGroupReservations; }
	public void setExpirationGroupReservations(Date expirationGroupReservations) { iExpirationGroupReservations = expirationGroupReservations; }
	public boolean getCreateStudentGroupsIfNeeded() { return iCreateStudentGroupsIfNeeded; }
	public void setCreateStudentGroupsIfNeeded(boolean createStudentGroupsIfNeeded) { iCreateStudentGroupsIfNeeded = createStudentGroupsIfNeeded; }

	public boolean getRollForwardUniversalReservations() { return iRollForwardUniversalReservations; }
	public void setRollForwardUniversalReservations(boolean rollForwardUniversalReservations) { iRollForwardUniversalReservations = rollForwardUniversalReservations; }
	public Date getStartDateUniversalReservations() { return iStartDateUniversalReservations; }
	public void setStartDateUniversalReservations(Date startDateUniversalReservations) { iStartDateUniversalReservations = startDateUniversalReservations; }
	public Date getExpirationUniversalReservations() { return iExpirationUniversalReservations; }
	public void setExpirationUniversalReservations(Date expirationUniversalReservations) { iExpirationUniversalReservations = expirationUniversalReservations; }

	public boolean getRollForwardPeriodicTasks() { return iRollForwardPeriodicTasks; }
	public void setRollForwardPeriodicTasks(boolean rollForwardPeriodicTasks) { iRollForwardPeriodicTasks = rollForwardPeriodicTasks; }
	public Long getSessionToRollPeriodicTasksFrom() { return iSessionToRollPeriodicTasksFrom; }
	public void setSessionToRollPeriodicTasksFrom(Long sessionToRollPeriodicTasksFrom) { iSessionToRollPeriodicTasksFrom = sessionToRollPeriodicTasksFrom; }
	
	
	public static enum RollAction implements IsSerializable{
		ROLL_PREFS_ACTION("rollUnchanged"),
		DO_NOT_ROLL_ACTION("doNotRoll"),
		PUSH_UP_ACTION("pushUp"),
		EXAMS_NO_PREF("doNotRoll"),
		EXAMS_ROOM_PREFS("rollRoomPrefs"), 
		EXAMS_ALL_PREF("rollAllPrefs"),
		;
		
		private String iLegacyName;
		RollAction(String leacyName) { iLegacyName = leacyName; }
		public String toLegacyConstant() { return iLegacyName; }
		public static RollAction fromLegacy(String legacy) {
			if (legacy == null || legacy.isEmpty()) return null;
			for (RollAction action: RollAction.values())
				if (action.toLegacyConstant().equals(legacy))
					return action;
			for (RollAction action: RollAction.values())
				if (action.name().equals(legacy))
					return action;
			return null;
		}
	}
	
	public static enum CancelledClassAction implements IsSerializable {
		REOPEN,
		KEEP,
		SKIP,
		;
		public static CancelledClassAction fromString(String option) {
			if (option == null || option.isEmpty()) return null;
			return CancelledClassAction.valueOf(option);
		}
	}
	
	public static enum DistributionMode implements IsSerializable {
		ALL,
		MIXED,
		SUBPART,
		NONE,
		;
		public static DistributionMode fromString(String option) {
			if (option == null || option.isEmpty()) return null;
			return DistributionMode.valueOf(option);
		}
	}
	
	public static enum StudentEnrollmentMode implements IsSerializable {
		LAST_LIKE,
		STUDENT_CLASS_ENROLLMENTS,
		STUDENT_COURSE_REQUESTS,
		POINT_IN_TIME_CLASS_ENROLLMENTS,
		;
		public static StudentEnrollmentMode fromString(String option) {
			if (option == null || option.isEmpty()) return null;
			return StudentEnrollmentMode.valueOf(option);
		}
	}
	
	public static class RollForwardSessionRequest implements GwtRpcRequest<RollForwardSessionResponse>{
		private RollForwardSessionInterface iData;
		private Operation iOperation;
		private String iQueueId;
		
		public RollForwardSessionRequest() {}
		public RollForwardSessionRequest(Operation operation, RollForwardSessionInterface data) {
			iOperation = operation;
			iData = data;
		}
		public RollForwardSessionRequest(Operation operation, String queueId) {
			iOperation = operation;
			iQueueId = queueId;
		}
		
		public RollForwardSessionInterface getData() { return iData; }
		public void setData(RollForwardSessionInterface data) { iData = data; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public String getQueueId() { return iQueueId; }
		public void setQueueId(String queueId) { iQueueId = queueId; }
	}
	
	public static enum Operation implements IsSerializable {
		LOAD,
		EXECUTE,
		POPULATE,
		;
	}
	
	public static class RollForwardSessionResponse implements GwtRpcResponse {
		private List<IdLabel> iDepartments, iToSessions, iFromSessions, iSubjects, iPointInTimes;
		private boolean iParentCourses, iAllowClassPrefs;
		private String iQueueId;
		private RollForwardSessionInterface iData;
		private ReservationInterface.DefaultExpirationDates iDates;
		private Long iToSessionId;
		private RollForwardErrors iErrors = null;
		
		public String getQueueId() { return iQueueId; }
		public void setQueueId(String queueId) { iQueueId = queueId; }
		
		public void addDepartment(Long id, String label) {
			if (iDepartments == null) iDepartments = new ArrayList<IdLabel>();
			iDepartments.add(new IdLabel(id, label));
		}
		public List<IdLabel> getDepartments() { return iDepartments; }
		public boolean hasDepartments() { return iDepartments != null && !iDepartments.isEmpty(); }
		public IdLabel getDepartment(Long id) {
			if (iDepartments == null || id == null) return null;
			for (IdLabel item: iDepartments)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addToSession(Long id, String label) {
			if (iToSessions == null) iToSessions = new ArrayList<IdLabel>();
			iToSessions.add(new IdLabel(id, label));
		}
		public List<IdLabel> getToSessions() { return iToSessions; }
		public boolean hasToSessions() { return iToSessions != null && !iToSessions.isEmpty(); }
		public IdLabel getToSession(Long id) {
			if (iToSessions == null || id == null) return null;
			for (IdLabel item: iToSessions)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addFromSession(Long id, String label) {
			if (iFromSessions == null) iFromSessions = new ArrayList<IdLabel>();
			iFromSessions.add(new IdLabel(id, label));
		}
		public List<IdLabel> getFromSessions() { return iFromSessions; }
		public boolean hasFromSessions() { return iFromSessions != null && !iFromSessions.isEmpty(); }
		public IdLabel getFromSession(Long id) {
			if (iFromSessions == null || id == null) return null;
			for (IdLabel item: iFromSessions)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addSubject(Long id, String label) {
			if (iSubjects == null) iSubjects = new ArrayList<IdLabel>();
			iSubjects.add(new IdLabel(id, label));
		}
		public List<IdLabel> getSubjects() { return iSubjects; }
		public boolean hasSubjects() { return iSubjects != null && !iSubjects.isEmpty(); }
		public IdLabel getSubject(Long id) {
			if (iSubjects == null || id == null) return null;
			for (IdLabel item: iSubjects)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public void addPointInTime(Long id, String label) {
			if (iPointInTimes == null) iPointInTimes = new ArrayList<IdLabel>();
			iPointInTimes.add(new IdLabel(id, label));
		}
		public List<IdLabel> getPointInTimes() { return iPointInTimes; }
		public boolean hasPointInTimes() { return iPointInTimes != null && !iPointInTimes.isEmpty(); }
		public IdLabel getPointInTime(Long id) {
			if (iPointInTimes == null || id == null) return null;
			for (IdLabel item: iPointInTimes)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean getParentCourses() { return iParentCourses; }
		public void setParentCourses(boolean parentCourses) { iParentCourses = parentCourses; }
		public boolean getAllowClassPrefs() { return iAllowClassPrefs; }
		public void setAllowClassPrefs(boolean allowClassPrefs) { iAllowClassPrefs = allowClassPrefs; }
		
		public RollForwardSessionInterface getData() { return iData; }
		public void setData(RollForwardSessionInterface data) { iData = data; }
		
		public ReservationInterface.DefaultExpirationDates getDates() { return iDates; }
		public void setDates(ReservationInterface.DefaultExpirationDates dates) { iDates = dates; }
		public Long getToSessionId() { return iToSessionId; }
		public void setToSessionId(Long toSessionId) { iToSessionId = toSessionId; }
		
		public boolean hasErrors() { return iErrors != null && !iErrors.isEmpty(); }
		public RollForwardErrors getErrors() { return iErrors; }
		public void setErrors(RollForwardErrors errors) { iErrors = errors; }
	}
	
	public static class IdLabel implements IsSerializable, Comparable<IdLabel> {
		private Long iId;
		private String iLabel;

		public IdLabel() {}
		public IdLabel(Long id, String label) {
			iId = id; iLabel = label;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdLabel)) return false;
			return getId().equals(((IdLabel)o).getId());
		}
		
		@Override
		public int compareTo(IdLabel other) {
			return NaturalOrderComparator.compare(getLabel(), other.getLabel());
		}
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}
	
	public static class RollForwardError implements Serializable, IsSerializable {
		private static final long serialVersionUID = -8383522549018220760L;
		private String iType, iMessage;
		public RollForwardError() {}
		public RollForwardError(String type, String message) {
			iType = type; iMessage = message;
		}
		public String getType() { return iType; }
		public String getMessage() { return iMessage; }
	}
	
	public static interface RollForwardErrorLogger {
		public void addFieldError(String type, String message);
		public boolean isEmpty();
	}

	public static class RollForwardErrors extends ArrayList<RollForwardError> implements RollForwardErrorLogger, Serializable, IsSerializable {
		private static final long serialVersionUID = 6152383035137322209L;

		public void addFieldError(String type, String message) {
			add(new RollForwardError(type, message));
		}
	}
}
