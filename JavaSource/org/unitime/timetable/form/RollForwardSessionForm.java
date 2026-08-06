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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.CancelledClassAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.DistributionMode;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrors;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.StudentEnrollmentMode;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.util.Formats;


/** 
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class RollForwardSessionForm implements UniTimeForm {
	private static final long serialVersionUID = 7553214589949959977L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private Collection<SubjectArea> subjectAreas;
	private String[] subjectAreaIds; 
	private String buttonAction;
	private Collection<Session> toSessions;
	private Collection<Session> fromSessions;
	private Collection<PointInTimeData> fromPointInTimeDataSnapshots;
	private Long sessionToRollForwardTo;
	private Boolean rollForwardDatePatterns;
	private Long sessionToRollDatePatternsForwardFrom;
	private Boolean rollForwardTimePatterns;
	private Long sessionToRollTimePatternsForwardFrom;
	private Boolean rollForwardDepartments;
	private Long sessionToRollDeptsFowardFrom;
	private Boolean rollForwardManagers;
	private Long sessionToRollManagersForwardFrom;
	private Boolean rollForwardRoomData;
	private Long sessionToRollRoomDataForwardFrom;
	private Collection<Department> departments;
	private String[] rollForwardDepartmentIds;
	private Boolean rollForwardSubjectAreas;
	private Long sessionToRollSubjectAreasForwardFrom;
	private Boolean rollForwardInstructorData;
	private Long sessionToRollInstructorDataForwardFrom;
	private Boolean rollForwardCourseOfferings;
	private Long sessionToRollCourseOfferingsForwardFrom;
	private String[] rollForwardSubjectAreaIds;
	private Boolean rollForwardClassInstructors;
	private String[] rollForwardClassInstrSubjectIds;
	private Boolean addNewCourseOfferings;
	private String[] addNewCourseOfferingsSubjectIds;
	private Boolean rollForwardExamConfiguration;
	private Long sessionToRollExamConfigurationForwardFrom;
	private Boolean rollForwardMidtermExams;
	private Boolean rollForwardFinalExams;
	private Boolean rollForwardStudents;
	private String rollForwardStudentsMode;
	private Long pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom;
	private String subpartLocationPrefsAction;
	private String subpartTimePrefsAction;
	private String classPrefsAction;
	private String rollForwardDistributions;
	private String cancelledClassAction;
	private Boolean rollForwardCurricula;
	private Long sessionToRollCurriculaForwardFrom;
	private String midtermExamsPrefsAction, finalExamsPrefsAction;
	private Boolean rollForwardSessionConfig;
	private Long sessionToRollSessionConfigForwardFrom;
	private Boolean rollForwardLearningManagementSystems;
	private Long sessionToRollLearningManagementSystemsForwardFrom;
	private Boolean rollForwardWaitListsProhibitedOverrides;
	private Boolean rollForwardParentOfferings;
	
	private Boolean rollForwardReservations;
	private Long sessionToRollReservationsForwardFrom;
	private String[] rollForwardReservationsSubjectIds;
	private Boolean rollForwardCourseReservations;
	private Boolean rollForwardCurriculumReservations;
	private Boolean rollForwardGroupReservations;
	private Boolean rollForwardUniversalReservations;
	private String expirationCourseReservations;
	private String expirationCurriculumReservations;
	private String expirationGroupReservations;
	private String expirationUniversalReservations;
	private Boolean createStudentGroupsIfNeeded;
	private Boolean rollForwardOfferingCoordinators;
	private String[] rollForwardOfferingCoordinatorsSubjectIds;
	private Boolean rollForwardTeachingRequests;
	private String[] rollForwardTeachingRequestsSubjectIds;
	private Boolean rollForwardPeriodicTasks;
	private Long sessionToRollPeriodicTasksFrom;
	private String startDateCourseReservations;
	private String startDateCurriculumReservations;
	private String startDateGroupReservations;
	private String startDateUniversalReservations;
	
	public RollForwardSessionForm() {
		reset();
	}
	
	@Override
	public void validate(UniTimeAction action) {
	}

	@Override
	public void reset() {
		subjectAreas = new ArrayList<SubjectArea>();
		subjectAreaIds = new String[0];
		fromSessions = null;
		toSessions = null;
		sessionToRollForwardTo = null;
		rollForwardDatePatterns = Boolean.valueOf(false);
		sessionToRollDatePatternsForwardFrom = null;
		rollForwardTimePatterns = Boolean.valueOf(false);
		sessionToRollTimePatternsForwardFrom = null;
		rollForwardDepartments = Boolean.valueOf(false);
		sessionToRollDeptsFowardFrom = null;
		rollForwardManagers = Boolean.valueOf(false);
		sessionToRollManagersForwardFrom = null;
		rollForwardRoomData = Boolean.valueOf(false);
		sessionToRollRoomDataForwardFrom = null;
		setDepartments(new ArrayList<Department>());
		setRollForwardDepartmentIds(new String[0]);
		rollForwardSubjectAreas = Boolean.valueOf(false);
		sessionToRollSubjectAreasForwardFrom = null;
		rollForwardInstructorData = Boolean.valueOf(false);
		sessionToRollInstructorDataForwardFrom = null;
		rollForwardCourseOfferings = Boolean.valueOf(false);
		sessionToRollCourseOfferingsForwardFrom = null;
		rollForwardSubjectAreaIds = new String[0];
		rollForwardClassInstructors = Boolean.valueOf(false);
		rollForwardClassInstrSubjectIds = new String[0];
		addNewCourseOfferings = Boolean.valueOf(false);
		addNewCourseOfferingsSubjectIds = new String[0];
		rollForwardExamConfiguration = Boolean.valueOf(false);
		sessionToRollExamConfigurationForwardFrom = null;
		rollForwardMidtermExams = Boolean.valueOf(false);
		rollForwardFinalExams = Boolean.valueOf(false);
		rollForwardStudents = Boolean.valueOf(false);
		rollForwardStudentsMode = null;
		pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom = null;
		setFromPointInTimeDataSnapshots(new ArrayList<PointInTimeData>());
		subpartLocationPrefsAction = null;
		subpartTimePrefsAction = null;
		classPrefsAction = null;
		rollForwardDistributions = null;
		cancelledClassAction = null;
		rollForwardCurricula = false;
		sessionToRollCurriculaForwardFrom = null;
		finalExamsPrefsAction = null;
		midtermExamsPrefsAction = null;
		rollForwardSessionConfig = false;
		sessionToRollSessionConfigForwardFrom = null;
		rollForwardReservations = false;
		sessionToRollReservationsForwardFrom = null;
		rollForwardReservationsSubjectIds = new String[0];
		rollForwardCurriculumReservations = false;
		rollForwardCourseReservations = false;
		rollForwardGroupReservations = false;
		rollForwardUniversalReservations = false;
		expirationCourseReservations = null;
		expirationCurriculumReservations = null;
		expirationGroupReservations = null;
		expirationUniversalReservations = null;
		createStudentGroupsIfNeeded = false;
		rollForwardTeachingRequests = false;
		rollForwardTeachingRequestsSubjectIds = new String[0];
		rollForwardOfferingCoordinators = Boolean.valueOf(false);
		rollForwardOfferingCoordinatorsSubjectIds = new String[0];
		rollForwardPeriodicTasks = false;
		sessionToRollPeriodicTasksFrom = null;
		startDateCourseReservations = null;
		startDateCurriculumReservations = null;
		startDateGroupReservations = null;
		startDateUniversalReservations = null;
		rollForwardLearningManagementSystems = Boolean.valueOf(false);
		sessionToRollLearningManagementSystemsForwardFrom = null;
		rollForwardWaitListsProhibitedOverrides = false;
		rollForwardParentOfferings = false;
	}

	public String getButtonAction() {
		return buttonAction;
	}

	public void setButtonAction(String buttonAction) {
		this.buttonAction = buttonAction;
	}

	public String[] getSubjectAreaIds() {
		return subjectAreaIds;
	}

	public void setSubjectAreaIds(String[] subjectAreaIds) {
		this.subjectAreaIds = subjectAreaIds;
	}

	public Collection<SubjectArea> getSubjectAreas() {
		return subjectAreas;
	}

	public void setSubjectAreas(Collection<SubjectArea> subjectAreas) {
		this.subjectAreas = subjectAreas;
	}

	public Boolean getRollForwardCourseOfferings() {
		return rollForwardCourseOfferings;
	}

	public void setRollForwardCourseOfferings(Boolean rollForwardCourseOfferings) {
		this.rollForwardCourseOfferings = rollForwardCourseOfferings;
	}

	public Boolean getRollForwardDatePatterns() {
		return rollForwardDatePatterns;
	}

	public void setRollForwardDatePatterns(Boolean rollForwardDatePatterns) {
		this.rollForwardDatePatterns = rollForwardDatePatterns;
	}

	public Boolean getRollForwardDepartments() {
		return rollForwardDepartments;
	}

	public void setRollForwardDepartments(Boolean rollForwardDepartments) {
		this.rollForwardDepartments = rollForwardDepartments;
	}

	public Boolean getRollForwardInstructorData() {
		return rollForwardInstructorData;
	}

	public void setRollForwardInstructorData(Boolean rollForwardInstructorData) {
		this.rollForwardInstructorData = rollForwardInstructorData;
	}

	public Boolean getRollForwardManagers() {
		return rollForwardManagers;
	}

	public void setRollForwardManagers(Boolean rollForwardManagers) {
		this.rollForwardManagers = rollForwardManagers;
	}

	public Boolean getRollForwardRoomData() {
		return rollForwardRoomData;
	}

	public void setRollForwardRoomData(Boolean rollForwardRoomData) {
		this.rollForwardRoomData = rollForwardRoomData;
	}

	public String[] getRollForwardSubjectAreaIds() {
		return rollForwardSubjectAreaIds;
	}

	public void setRollForwardSubjectAreaIds(String[] rollForwardSubjectAreaIds) {
		this.rollForwardSubjectAreaIds = rollForwardSubjectAreaIds;
	}

	public Boolean getRollForwardSubjectAreas() {
		return rollForwardSubjectAreas;
	}

	public void setRollForwardSubjectAreas(Boolean rollForwardSubjectAreas) {
		this.rollForwardSubjectAreas = rollForwardSubjectAreas;
	}

	public Long getSessionToRollCourseOfferingsForwardFrom() {
		return sessionToRollCourseOfferingsForwardFrom;
	}

	public void setSessionToRollCourseOfferingsForwardFrom(
			Long sessionToRollCourseOfferingsForwardFrom) {
		this.sessionToRollCourseOfferingsForwardFrom = sessionToRollCourseOfferingsForwardFrom;
	}

	public Long getSessionToRollDatePatternsForwardFrom() {
		return sessionToRollDatePatternsForwardFrom;
	}

	public void setSessionToRollDatePatternsForwardFrom(
			Long sessionToRollDatePatternsForwardFrom) {
		this.sessionToRollDatePatternsForwardFrom = sessionToRollDatePatternsForwardFrom;
	}

	public Long getSessionToRollDeptsFowardFrom() {
		return sessionToRollDeptsFowardFrom;
	}

	public void setSessionToRollDeptsFowardFrom(Long sessionToRollDeptsFowardFrom) {
		this.sessionToRollDeptsFowardFrom = sessionToRollDeptsFowardFrom;
	}

	public Long getSessionToRollForwardTo() {
		return sessionToRollForwardTo;
	}

	public void setSessionToRollForwardTo(Long sessionToRollForwardTo) {
		this.sessionToRollForwardTo = sessionToRollForwardTo;
	}

	public Long getSessionToRollInstructorDataForwardFrom() {
		return sessionToRollInstructorDataForwardFrom;
	}

	public void setSessionToRollInstructorDataForwardFrom(
			Long sessionToRollInstructorDataForwardFrom) {
		this.sessionToRollInstructorDataForwardFrom = sessionToRollInstructorDataForwardFrom;
	}

	public Long getSessionToRollManagersForwardFrom() {
		return sessionToRollManagersForwardFrom;
	}

	public void setSessionToRollManagersForwardFrom(
			Long sessionToRollManagersForwardFrom) {
		this.sessionToRollManagersForwardFrom = sessionToRollManagersForwardFrom;
	}

	public Long getSessionToRollRoomDataForwardFrom() {
		return sessionToRollRoomDataForwardFrom;
	}

	public void setSessionToRollRoomDataForwardFrom(
			Long sessionToRollRoomDataForwardFrom) {
		this.sessionToRollRoomDataForwardFrom = sessionToRollRoomDataForwardFrom;
	}

	public Collection<Department> getDepartments() {
		return departments;
	}

	public void setFromPointInTimeDataSnapshots(
			Collection<PointInTimeData> fromPointInTimeDataSnapshots) {
		this.fromPointInTimeDataSnapshots = fromPointInTimeDataSnapshots;
	}

	public Collection<PointInTimeData> getFromPointInTimeDataSnapshots() {
		return fromPointInTimeDataSnapshots;
	}

	public void setDepartments(
			Collection<Department> departments) {
		this.departments = departments;
	}

	public String[] getRollForwardDepartmentIds() {
		return rollForwardDepartmentIds;
	}

	public void setRollForwardDepartmentIds(String[] rollForwardDepartmentIds) {
		this.rollForwardDepartmentIds = rollForwardDepartmentIds;
	}

	public Long getSessionToRollSubjectAreasForwardFrom() {
		return sessionToRollSubjectAreasForwardFrom;
	}

	public void setSessionToRollSubjectAreasForwardFrom(
			Long sessionToRollSubjectAreasForwardFrom) {
		this.sessionToRollSubjectAreasForwardFrom = sessionToRollSubjectAreasForwardFrom;
	}

	public Collection<Session> getFromSessions() {
		return fromSessions;
	}

	public void setFromSessions(Collection<Session> fromSessions) {
		this.fromSessions = fromSessions;
	}


	public Boolean getRollForwardTimePatterns() {
		return rollForwardTimePatterns;
	}


	public void setRollForwardTimePatterns(Boolean rollForwardTimePatterns) {
		this.rollForwardTimePatterns = rollForwardTimePatterns;
	}


	public Long getSessionToRollTimePatternsForwardFrom() {
		return sessionToRollTimePatternsForwardFrom;
	}


	public void setSessionToRollTimePatternsForwardFrom(
			Long sessionToRollTimePatternsForwardFrom) {
		this.sessionToRollTimePatternsForwardFrom = sessionToRollTimePatternsForwardFrom;
	}


	public Boolean getRollForwardClassInstructors() {
		return rollForwardClassInstructors;
	}


	public void setRollForwardClassInstructors(Boolean rollForwardClassInstructors) {
		this.rollForwardClassInstructors = rollForwardClassInstructors;
	}


	public String[] getRollForwardClassInstrSubjectIds() {
		return rollForwardClassInstrSubjectIds;
	}


	public void setRollForwardClassInstrSubjectIds(
			String[] rollForwardClassInstrSubjectIds) {
		this.rollForwardClassInstrSubjectIds = rollForwardClassInstrSubjectIds;
	}
	
	public Boolean getRollForwardOfferingCoordinators() {
		return rollForwardOfferingCoordinators;
	}


	public void setRollForwardOfferingCoordinators(Boolean rollForwardOfferingCoordinators) {
		this.rollForwardOfferingCoordinators = rollForwardOfferingCoordinators;
	}


	public String[] getRollForwardOfferingCoordinatorsSubjectIds() {
		return rollForwardOfferingCoordinatorsSubjectIds;
	}


	public void setRollForwardOfferingCoordinatorsSubjectIds(
			String[] rollForwardOfferingCoordinatorsSubjectIds) {
		this.rollForwardOfferingCoordinatorsSubjectIds = rollForwardOfferingCoordinatorsSubjectIds;
	}


	public Collection<Session> getToSessions() {
		return toSessions;
	}


	public void setToSessions(Collection<Session> toSessions) {
		this.toSessions = toSessions;
	}


	public Boolean getAddNewCourseOfferings() {
		return addNewCourseOfferings;
	}


	public void setAddNewCourseOfferings(Boolean addNewCourseOfferings) {
		this.addNewCourseOfferings = addNewCourseOfferings;
	}


	public String[] getAddNewCourseOfferingsSubjectIds() {
		return addNewCourseOfferingsSubjectIds;
	}


	public void setAddNewCourseOfferingsSubjectIds(
			String[] addNewCourseOfferingsSubjectIds) {
		this.addNewCourseOfferingsSubjectIds = addNewCourseOfferingsSubjectIds;
	}


	public Boolean getRollForwardExamConfiguration() {
		return rollForwardExamConfiguration;
	}


	public void setRollForwardExamConfiguration(Boolean rollForwardExamConfiguration) {
		this.rollForwardExamConfiguration = rollForwardExamConfiguration;
	}


	public Boolean getRollForwardMidtermExams() {
		return rollForwardMidtermExams;
	}


	public void setRollForwardMidtermExams(Boolean rollForwardMidtermExams) {
		this.rollForwardMidtermExams = rollForwardMidtermExams;
	}


	public Boolean getRollForwardFinalExams() {
		return rollForwardFinalExams;
	}


	public void setRollForwardFinalExams(Boolean rollForwardFinalExams) {
		this.rollForwardFinalExams = rollForwardFinalExams;
	}


	public Long getSessionToRollExamConfigurationForwardFrom() {
		return sessionToRollExamConfigurationForwardFrom;
	}


	public void setSessionToRollExamConfigurationForwardFrom(
			Long sessionToRollExamConfigurationForwardFrom) {
		this.sessionToRollExamConfigurationForwardFrom = sessionToRollExamConfigurationForwardFrom;
	}
	
	public Boolean getRollForwardStudents() {
	    return rollForwardStudents;
	}
	
	public void setRollForwardStudents(Boolean rollForwardStudents) {
	    this.rollForwardStudents = rollForwardStudents;
	}
	
    public String getRollForwardStudentsMode() {
        return rollForwardStudentsMode;
    }
    
    public void setRollForwardStudentsMode(String rollForwardStudentsMode) {
        this.rollForwardStudentsMode = rollForwardStudentsMode;
    }
    
    public Long getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom() {
    	return(this.pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom);
    }
    
    public void setPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom(Long pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom) {
    	this.pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom = pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom;
    }
    
    public Boolean getRollForwardCurricula() {
    	return rollForwardCurricula;
    }
    
    public void setRollForwardCurricula(Boolean rollForwardCurricula) {
    	this.rollForwardCurricula = rollForwardCurricula;
    }
    
    public Long getSessionToRollCurriculaForwardFrom() {
    	return sessionToRollCurriculaForwardFrom;
    }
    
    public void setSessionToRollCurriculaForwardFrom(Long sessionToRollCurriculaForwardFrom) {
    	this.sessionToRollCurriculaForwardFrom = sessionToRollCurriculaForwardFrom;
    }
    
    public Boolean getRollForwardSessionConfig() {
    	return rollForwardSessionConfig;
    }
    
    public void setRollForwardSessionConfig(Boolean rollForwardSessionConfig) {
    	this.rollForwardSessionConfig = rollForwardSessionConfig;
    }
    
    public Long getSessionToRollSessionConfigForwardFrom() {
    	return sessionToRollSessionConfigForwardFrom;
    }
    
    public void setSessionToRollSessionConfigForwardFrom(Long sessionToRollSessionConfigForwardFrom) {
    	this.sessionToRollSessionConfigForwardFrom = sessionToRollSessionConfigForwardFrom;
    }


	/**
	 * @return the subpartLocationPrefsAction
	 */
	public String getSubpartLocationPrefsAction() {
		return subpartLocationPrefsAction;
	}


	/**
	 * @param subpartLocationPrefsAction the subpartLocationPrefsAction to set
	 */
	public void setSubpartLocationPrefsAction(String subpartLocationPrefsAction) {
		this.subpartLocationPrefsAction = subpartLocationPrefsAction;
	}


	/**
	 * @return the subpartTimePrefsAction
	 */
	public String getSubpartTimePrefsAction() {
		return subpartTimePrefsAction;
	}


	/**
	 * @param subpartTimePrefsAction the subpartTimePrefsAction to set
	 */
	public void setSubpartTimePrefsAction(String subpartTimePrefsAction) {
		this.subpartTimePrefsAction = subpartTimePrefsAction;
	}


	/**
	 * @return the classPrefsAction
	 */
	public String getClassPrefsAction() {
		return classPrefsAction;
	}


	/**
	 * @param classPrefsAction the classPrefsAction to set
	 */
	public void setClassPrefsAction(String classPrefsAction) {
		this.classPrefsAction = classPrefsAction;
	}
	
	public String getRollForwardDistributions() { return rollForwardDistributions; }
	public void setRollForwardDistributions(String rollForwardDistributions) { this.rollForwardDistributions = rollForwardDistributions; }

	public String getCancelledClassAction() { return cancelledClassAction; }
	public void setCancelledClassAction(String cancelledClassAction) { this.cancelledClassAction = cancelledClassAction; }

	public String getMidtermExamsPrefsAction() { return midtermExamsPrefsAction; }
	public void setMidtermExamsPrefsAction(String midtermExamsPrefsAction) { this.midtermExamsPrefsAction = midtermExamsPrefsAction; }

	public String getFinalExamsPrefsAction() { return finalExamsPrefsAction; }
	public void setFinalExamsPrefsAction(String finalExamsPrefsAction) { this.finalExamsPrefsAction = finalExamsPrefsAction; }
	
	public boolean getRollForwardReservations() { return rollForwardReservations; }
	public void setRollForwardReservations(boolean rollForwardReservations) { this.rollForwardReservations = rollForwardReservations; }
	
	public Long getSessionToRollReservationsForwardFrom() { return sessionToRollReservationsForwardFrom; }
	public void setSessionToRollReservationsForwardFrom(Long sessionToRollReservationsForwardFrom) { this.sessionToRollReservationsForwardFrom = sessionToRollReservationsForwardFrom; }
	
	public String[] getRollForwardReservationsSubjectIds() { return rollForwardReservationsSubjectIds; }
	public void setRollForwardReservationsSubjectIds(String[] rollForwardReservationsSubjectIds) { this.rollForwardReservationsSubjectIds = rollForwardReservationsSubjectIds; }

	public boolean getRollForwardCourseReservations() { return rollForwardCourseReservations; }
	public void setRollForwardCourseReservations(boolean rollForwardCourseReservations) { this.rollForwardCourseReservations = rollForwardCourseReservations; }
	
	public boolean getRollForwardGroupReservations() { return rollForwardGroupReservations; }
	public void setRollForwardGroupReservations(boolean rollForwardGroupReservations) { this.rollForwardGroupReservations = rollForwardGroupReservations; }

	public boolean getRollForwardUniversalReservations() { return rollForwardUniversalReservations; }
	public void setRollForwardUniversalReservations(boolean rollForwardUniversalReservations) { this.rollForwardUniversalReservations = rollForwardUniversalReservations; }
	
	public boolean getRollForwardCurriculumReservations() { return rollForwardCurriculumReservations; }
	public void setRollForwardCurriculumReservations(boolean rollForwardCurriculumReservations) { this.rollForwardCurriculumReservations = rollForwardCurriculumReservations; }
	
	public String getExpirationCourseReservations() { return expirationCourseReservations; }
	public void setExpirationCourseReservations(String expirationCourseReservations) { this.expirationCourseReservations = expirationCourseReservations; }
	
	public String getExpirationCurriculumReservations() { return expirationCurriculumReservations; }
	public void setExpirationCurriculumReservations(String expirationCurriculumReservations) { this.expirationCurriculumReservations = expirationCurriculumReservations; }
	
	public String getExpirationGroupReservations() { return expirationGroupReservations; }
	public void setExpirationGroupReservations(String expirationGroupReservations) { this.expirationGroupReservations = expirationGroupReservations; }
	
	public String getExpirationUniversalReservations() { return expirationUniversalReservations; }
	public void setExpirationUniversalReservations(String expirationUniversalReservations) { this.expirationUniversalReservations = expirationUniversalReservations; }
	
	public boolean getCreateStudentGroupsIfNeeded() { return createStudentGroupsIfNeeded; }
	public void setCreateStudentGroupsIfNeeded(boolean createStudentGroupsIfNeeded) { this.createStudentGroupsIfNeeded = createStudentGroupsIfNeeded; }
	
	public String getStartDateCourseReservations() { return startDateCourseReservations; }
	public void setStartDateCourseReservations(String startDateCourseReservations) { this.startDateCourseReservations = startDateCourseReservations; }
	
	public String getStartDateCurriculumReservations() { return startDateCurriculumReservations; }
	public void setStartDateCurriculumReservations(String startDateCurriculumReservations) { this.startDateCurriculumReservations = startDateCurriculumReservations; }
	
	public String getStartDateGroupReservations() { return startDateGroupReservations; }
	public void setStartDateGroupReservations(String startDateGroupReservations) { this.startDateGroupReservations = startDateGroupReservations; }
	
	public String getStartDateUniversalReservations() { return startDateUniversalReservations; }
	public void setStartDateUniversalReservations(String startDateUniversalReservations) { this.startDateUniversalReservations = startDateUniversalReservations; }


	public void copyTo(RollForwardSessionForm form) {
		// form.subjectAreas = subjectAreas;
		form.subjectAreaIds = subjectAreaIds;
		form.buttonAction = buttonAction;
		// form.toSessions = toSessions;
		// form.fromSessions = fromSessions;
		form.sessionToRollForwardTo = sessionToRollForwardTo;
		form.rollForwardDatePatterns = rollForwardDatePatterns;
		form.sessionToRollDatePatternsForwardFrom = sessionToRollDatePatternsForwardFrom;
		form.rollForwardTimePatterns = rollForwardTimePatterns;
		form.sessionToRollTimePatternsForwardFrom = sessionToRollTimePatternsForwardFrom;
		form.rollForwardDepartments = rollForwardDepartments;
		form.sessionToRollDeptsFowardFrom = sessionToRollDeptsFowardFrom;
		form.rollForwardManagers = rollForwardManagers;
		form.sessionToRollManagersForwardFrom = sessionToRollManagersForwardFrom;
		form.rollForwardRoomData = rollForwardRoomData;
		// form.departments = departments;
		form.rollForwardDepartmentIds = rollForwardDepartmentIds;
		form.sessionToRollRoomDataForwardFrom = sessionToRollRoomDataForwardFrom;
		form.rollForwardSubjectAreas = rollForwardSubjectAreas;
		form.sessionToRollSubjectAreasForwardFrom = sessionToRollSubjectAreasForwardFrom;
		form.rollForwardInstructorData = rollForwardInstructorData;
		form.sessionToRollInstructorDataForwardFrom = sessionToRollInstructorDataForwardFrom;
		form.rollForwardCourseOfferings = rollForwardCourseOfferings;
		form.sessionToRollCourseOfferingsForwardFrom = sessionToRollCourseOfferingsForwardFrom;
		form.rollForwardSubjectAreaIds = rollForwardSubjectAreaIds;
		form.rollForwardClassInstructors = rollForwardClassInstructors;
		form.rollForwardClassInstrSubjectIds = rollForwardClassInstrSubjectIds;
		form.addNewCourseOfferings = addNewCourseOfferings;
		form.addNewCourseOfferingsSubjectIds = addNewCourseOfferingsSubjectIds;
		form.rollForwardExamConfiguration = rollForwardExamConfiguration;
		form.sessionToRollExamConfigurationForwardFrom = sessionToRollExamConfigurationForwardFrom;
		form.rollForwardMidtermExams = rollForwardMidtermExams;
		form.rollForwardFinalExams = rollForwardFinalExams;
		form.rollForwardStudents = rollForwardStudents;
		form.rollForwardStudentsMode = rollForwardStudentsMode;
		form.pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom = pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom;
		// form.fromPointInTimeDataSnapshots = fromPointInTimeDataSnapshots;
		form.subpartLocationPrefsAction = subpartLocationPrefsAction;
		form.subpartTimePrefsAction = subpartTimePrefsAction;
		form.classPrefsAction = classPrefsAction;
		form.cancelledClassAction = cancelledClassAction;
		form.rollForwardCurricula = rollForwardCurricula;
		form.sessionToRollCurriculaForwardFrom = sessionToRollCurriculaForwardFrom;
		form.midtermExamsPrefsAction = midtermExamsPrefsAction;
		form.finalExamsPrefsAction = finalExamsPrefsAction;
		form.rollForwardSessionConfig = rollForwardSessionConfig;
		form.sessionToRollSessionConfigForwardFrom = sessionToRollSessionConfigForwardFrom;
		form.rollForwardReservations = rollForwardReservations;
		form.sessionToRollReservationsForwardFrom = sessionToRollReservationsForwardFrom;
		form.rollForwardReservationsSubjectIds = rollForwardReservationsSubjectIds;
		form.rollForwardCurriculumReservations = rollForwardCurriculumReservations;
		form.rollForwardCourseReservations = rollForwardCourseReservations;
		form.rollForwardGroupReservations = rollForwardGroupReservations;
		form.expirationCourseReservations = expirationCourseReservations;
		form.expirationCurriculumReservations = expirationCurriculumReservations;
		form.expirationGroupReservations = expirationGroupReservations;
		form.createStudentGroupsIfNeeded = createStudentGroupsIfNeeded;
		form.rollForwardOfferingCoordinators = rollForwardOfferingCoordinators;
		form.rollForwardOfferingCoordinatorsSubjectIds = rollForwardOfferingCoordinatorsSubjectIds; 
		form.rollForwardTeachingRequests = rollForwardTeachingRequests;
		form.rollForwardTeachingRequestsSubjectIds = rollForwardTeachingRequestsSubjectIds;
		form.rollForwardDistributions = rollForwardDistributions;
		form.rollForwardPeriodicTasks = rollForwardPeriodicTasks;
		form.sessionToRollPeriodicTasksFrom = sessionToRollPeriodicTasksFrom;
		form.startDateCourseReservations = startDateCourseReservations;
		form.startDateCurriculumReservations = startDateCurriculumReservations;
		form.startDateGroupReservations = startDateGroupReservations;
		form.rollForwardLearningManagementSystems = rollForwardLearningManagementSystems;
		form.sessionToRollLearningManagementSystemsForwardFrom = sessionToRollLearningManagementSystemsForwardFrom;
		form.rollForwardWaitListsProhibitedOverrides = rollForwardWaitListsProhibitedOverrides;
		form.rollForwardParentOfferings = rollForwardParentOfferings;
		form.rollForwardUniversalReservations = rollForwardUniversalReservations;
		form.expirationUniversalReservations = expirationUniversalReservations;
		form.startDateUniversalReservations = startDateUniversalReservations;
	}
	
	public Boolean getRollForwardTeachingRequests() {
		return rollForwardTeachingRequests;
	}

	public void setRollForwardTeachingRequests(Boolean rollForwardTeachingRequests) {
		this.rollForwardTeachingRequests = rollForwardTeachingRequests;
	}
	
	public String[] getRollForwardTeachingRequestsSubjectIds() {
		return rollForwardTeachingRequestsSubjectIds;
	}

	public void setRollForwardTeachingRequestsSubjectIds(String[] rollForwardTeachingRequestsSubjectIds) {
		this.rollForwardTeachingRequestsSubjectIds = rollForwardTeachingRequestsSubjectIds;
	}
	
	public Boolean getRollForwardPeriodicTasks() { return rollForwardPeriodicTasks; }
	public void setRollForwardPeriodicTasks(Boolean rollForwardPeriodicTasks) { this.rollForwardPeriodicTasks = rollForwardPeriodicTasks; }
	
	public Long getSessionToRollPeriodicTasksFrom() { return sessionToRollPeriodicTasksFrom; }
	public void setSessionToRollPeriodicTasksFrom(Long sessionToRollPeriodicTasksFrom) { this.sessionToRollPeriodicTasksFrom = sessionToRollPeriodicTasksFrom; }
	
	public Boolean getRollForwardLearningManagementSystems() {
		return rollForwardLearningManagementSystems;
	}


	public void setRollForwardLearningManagementSystems(Boolean rollForwardLearningManagementSystems) {
		this.rollForwardLearningManagementSystems = rollForwardLearningManagementSystems;
	}


	public Long getSessionToRollLearningManagementSystemsForwardFrom() {
		return sessionToRollLearningManagementSystemsForwardFrom;
	}


	public void setSessionToRollLearningManagementSystemsForwardFrom(
			Long sessionToRollLearningManagementSystemsForwardFrom) {
		this.sessionToRollLearningManagementSystemsForwardFrom = sessionToRollLearningManagementSystemsForwardFrom;
	}
	
	public Boolean getRollForwardWaitListsProhibitedOverrides() { return rollForwardWaitListsProhibitedOverrides; }
	public void setRollForwardWaitListsProhibitedOverrides(Boolean rollForwardWaitListsProhibitedOverrides) { this.rollForwardWaitListsProhibitedOverrides = rollForwardWaitListsProhibitedOverrides; }
	
	public Boolean getRollForwardParentOfferings() { return rollForwardParentOfferings; }
	public void setRollForwardParentOfferings(Boolean roolForwardParentOfferings) { this.rollForwardParentOfferings = roolForwardParentOfferings; }


	public Object clone() {
		RollForwardSessionForm form = new RollForwardSessionForm();
		copyTo(form);
		return form;
	}
	
	public int getDepartmentsListSize() {
		return Math.min(7,getDepartments().size());
	}
	
	public int getSubjectAreasListSize() {
		return Math.min(7,getSubjectAreas().size());
	}
	
	public static List<Long> toList(String[] data) {
		if (data == null) return null;
		List<Long> ret = new ArrayList<Long>();
		for (String id: data)
			ret.add(Long.valueOf(id));
		return ret;
	}
	
	public static Date toDate(String date) {
		if (date == null || date.isEmpty()) return null;
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		try {
			return df.parse(date);
		} catch (Exception e) {
			return null;
		}
	}
	
	public RollForwardSessionInterface toRollForwardSessionInterface() {
		RollForwardSessionInterface form = new RollForwardSessionInterface();
		// form.setSubjectAreas(getSubjectAreas());
		// form.setSubjectAreaIds(toSet(getSubjectAreaIds())); -- NOT USED
		// form.setButtonAction(getButtonAction());
		// form.setToSessions(getToSessions());
		// form.setFromSessions(getFromSessions());
		form.setSessionToRollForwardTo(getSessionToRollForwardTo());
		form.setRollForwardDatePatterns(getRollForwardDatePatterns());
		form.setSessionToRollDatePatternsForwardFrom(getSessionToRollDatePatternsForwardFrom());
		form.setRollForwardTimePatterns(getRollForwardTimePatterns());
		form.setSessionToRollTimePatternsForwardFrom(getSessionToRollTimePatternsForwardFrom());
		form.setRollForwardDepartments(getRollForwardDepartments());
		form.setSessionToRollDeptsFowardFrom(getSessionToRollDeptsFowardFrom());
		form.setRollForwardManagers(getRollForwardManagers());
		form.setSessionToRollManagersForwardFrom(getSessionToRollManagersForwardFrom());
		form.setRollForwardRoomData(getRollForwardRoomData());
		// form.setDepartments(getDepartments());
		form.setRollForwardDepartmentIds(toList(getRollForwardDepartmentIds()));
		form.setSessionToRollRoomDataForwardFrom(getSessionToRollRoomDataForwardFrom());
		form.setRollForwardSubjectAreas(getRollForwardSubjectAreas());
		form.setSessionToRollSubjectAreasForwardFrom(getSessionToRollSubjectAreasForwardFrom());
		form.setRollForwardInstructorData(getRollForwardInstructorData());
		form.setSessionToRollInstructorDataForwardFrom(getSessionToRollInstructorDataForwardFrom());
		form.setRollForwardCourseOfferings(getRollForwardCourseOfferings());
		form.setSessionToRollCourseOfferingsForwardFrom(getSessionToRollCourseOfferingsForwardFrom());
		form.setRollForwardSubjectAreaIds(toList(getRollForwardSubjectAreaIds()));
		form.setRollForwardClassInstructors(getRollForwardClassInstructors());
		form.setRollForwardClassInstrSubjectIds(toList(getRollForwardClassInstrSubjectIds()));
		form.setAddNewCourseOfferings(getAddNewCourseOfferings());
		form.setAddNewCourseOfferingsSubjectIds(toList(getAddNewCourseOfferingsSubjectIds()));
		form.setRollForwardExamConfiguration(getRollForwardExamConfiguration());
		form.setSessionToRollExamConfigurationForwardFrom(getSessionToRollExamConfigurationForwardFrom());
		form.setRollForwardMidtermExams(getRollForwardMidtermExams());
		form.setRollForwardFinalExams(getRollForwardFinalExams());
		form.setRollForwardStudents(getRollForwardStudents());
		form.setRollForwardStudentsMode(StudentEnrollmentMode.fromString(getRollForwardStudentsMode()));
		form.setPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom(getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom());
		// form.setFromPointInTimeDataSnapshots(getFromPointInTimeDataSnapshots());
		form.setSubpartLocationPrefsAction(RollAction.fromLegacy(getSubpartLocationPrefsAction()));
		form.setSubpartTimePrefsAction(RollAction.fromLegacy(getSubpartTimePrefsAction()));
		form.setClassPrefsAction(RollAction.fromLegacy(getClassPrefsAction()));
		form.setCancelledClassAction(CancelledClassAction.fromString(getCancelledClassAction()));
		form.setRollForwardCurricula(getRollForwardCurricula());
		form.setSessionToRollCurriculaForwardFrom(getSessionToRollCurriculaForwardFrom());
		form.setMidtermExamsPrefsAction(RollAction.fromLegacy(getMidtermExamsPrefsAction()));
		form.setFinalExamsPrefsAction(RollAction.fromLegacy(getFinalExamsPrefsAction()));
		form.setRollForwardSessionConfig(getRollForwardSessionConfig());
		form.setSessionToRollSessionConfigForwardFrom(getSessionToRollSessionConfigForwardFrom());
		form.setRollForwardReservations(getRollForwardReservations());
		form.setSessionToRollReservationsForwardFrom(getSessionToRollReservationsForwardFrom());
		form.setRollForwardReservationsSubjectIds(toList(getRollForwardReservationsSubjectIds()));
		form.setRollForwardCurriculumReservations(getRollForwardCurriculumReservations());
		form.setRollForwardCourseReservations(getRollForwardCourseReservations());
		form.setRollForwardGroupReservations(getRollForwardGroupReservations());
		form.setExpirationCourseReservations(toDate(getExpirationCourseReservations()));
		form.setExpirationCurriculumReservations(toDate(getExpirationCurriculumReservations()));
		form.setExpirationGroupReservations(toDate(getExpirationGroupReservations()));
		form.setCreateStudentGroupsIfNeeded(getCreateStudentGroupsIfNeeded());
		form.setRollForwardOfferingCoordinators(getRollForwardOfferingCoordinators());
		form.setRollForwardOfferingCoordinatorsSubjectIds(toList(getRollForwardOfferingCoordinatorsSubjectIds())); 
		form.setRollForwardTeachingRequests(getRollForwardTeachingRequests());
		form.setRollForwardTeachingRequestsSubjectIds(toList(getRollForwardTeachingRequestsSubjectIds()));
		form.setRollForwardDistributions(DistributionMode.fromString(getRollForwardDistributions()));
		form.setRollForwardPeriodicTasks(getRollForwardPeriodicTasks());
		form.setSessionToRollPeriodicTasksFrom(getSessionToRollPeriodicTasksFrom());
		form.setStartDateCourseReservations(toDate(getStartDateCourseReservations()));
		form.setStartDateCurriculumReservations(toDate(getStartDateCurriculumReservations()));
		form.setStartDateGroupReservations(toDate(getStartDateGroupReservations()));
		form.setRollForwardLearningManagementSystems(getRollForwardLearningManagementSystems());
		form.setSessionToRollLearningManagementSystemsForwardFrom(getSessionToRollLearningManagementSystemsForwardFrom());
		form.setRollForwardWaitListsProhibitedOverrides(getRollForwardWaitListsProhibitedOverrides());
		form.setRollForwardParentOfferings(getRollForwardParentOfferings());
		form.setRollForwardUniversalReservations(getRollForwardUniversalReservations());
		form.setExpirationUniversalReservations(toDate(getExpirationUniversalReservations()));
		form.setStartDateUniversalReservations(toDate(getStartDateUniversalReservations()));
		return form;
	}
	
	public static String[] fromList(List<Long> data) {
		if (data == null) return null;
		String[] ret = new String[data.size()];
		int i = 0;
		for (Long id: data)
			ret[i++] = id.toString();
		return ret;
	}
	
	public static String fromDate(Date date) {
		if (date == null) return null;
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		return df.format(date);
	}
	
	public static String toString(RollAction a) {
		return a == null ? null : a.toLegacyConstant();
	}
	
	public static String toString(DistributionMode a) {
		return a == null ? null : a.name();
	}
	
	public static String toString(StudentEnrollmentMode a) {
		return a == null ? null : a.name();
	}
	
	public static String toString(CancelledClassAction a) {
		return a == null ? null : a.name();
	}
	
	public void copyFromRollForwardSessionInterface(RollForwardSessionInterface form) {
		// setSubjectAreas(form.getSubjectAreas());
		// setSubjectAreaIds(fromSet(form.getSubjectAreaIds()));
		setButtonAction(MSG.actionRollForward());
		// setToSessions(form.getToSessions());
		// setFromSessions(form.getFromSessions());
		setSessionToRollForwardTo(form.getSessionToRollForwardTo());
		setRollForwardDatePatterns(form.getRollForwardDatePatterns());
		setSessionToRollDatePatternsForwardFrom(form.getSessionToRollDatePatternsForwardFrom());
		setRollForwardTimePatterns(form.getRollForwardTimePatterns());
		setSessionToRollTimePatternsForwardFrom(form.getSessionToRollTimePatternsForwardFrom());
		setRollForwardDepartments(form.getRollForwardDepartments());
		setSessionToRollDeptsFowardFrom(form.getSessionToRollDeptsFowardFrom());
		setRollForwardManagers(form.getRollForwardManagers());
		setSessionToRollManagersForwardFrom(form.getSessionToRollManagersForwardFrom());
		setRollForwardRoomData(form.getRollForwardRoomData());
		// setDepartments(form.getDepartments());
		setRollForwardDepartmentIds(fromList(form.getRollForwardDepartmentIds()));
		setSessionToRollRoomDataForwardFrom(form.getSessionToRollRoomDataForwardFrom());
		setRollForwardSubjectAreas(form.getRollForwardSubjectAreas());
		setSessionToRollSubjectAreasForwardFrom(form.getSessionToRollSubjectAreasForwardFrom());
		setRollForwardInstructorData(form.getRollForwardInstructorData());
		setSessionToRollInstructorDataForwardFrom(form.getSessionToRollInstructorDataForwardFrom());
		setRollForwardCourseOfferings(form.getRollForwardCourseOfferings());
		setSessionToRollCourseOfferingsForwardFrom(form.getSessionToRollCourseOfferingsForwardFrom());
		setRollForwardSubjectAreaIds(fromList(form.getRollForwardSubjectAreaIds()));
		setRollForwardClassInstructors(form.getRollForwardClassInstructors());
		setRollForwardClassInstrSubjectIds(fromList(form.getRollForwardClassInstrSubjectIds()));
		setAddNewCourseOfferings(form.getAddNewCourseOfferings());
		setAddNewCourseOfferingsSubjectIds(fromList(form.getAddNewCourseOfferingsSubjectIds()));
		setRollForwardExamConfiguration(form.getRollForwardExamConfiguration());
		setSessionToRollExamConfigurationForwardFrom(form.getSessionToRollExamConfigurationForwardFrom());
		setRollForwardMidtermExams(form.getRollForwardMidtermExams());
		setRollForwardFinalExams(form.getRollForwardFinalExams());
		setRollForwardStudents(form.getRollForwardStudents());
		setRollForwardStudentsMode(toString(form.getRollForwardStudentsMode()));
		setPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom(form.getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom());
		// setFromPointInTimeDataSnapshots(form.getFromPointInTimeDataSnapshots());
		setSubpartLocationPrefsAction(toString(form.getSubpartLocationPrefsAction()));
		setSubpartTimePrefsAction(toString(form.getSubpartTimePrefsAction()));
		setClassPrefsAction(toString(form.getClassPrefsAction()));
		setCancelledClassAction(toString(form.getCancelledClassAction()));
		setRollForwardCurricula(form.getRollForwardCurricula());
		setSessionToRollCurriculaForwardFrom(form.getSessionToRollCurriculaForwardFrom());
		setMidtermExamsPrefsAction(toString(form.getMidtermExamsPrefsAction()));
		setFinalExamsPrefsAction(toString(form.getFinalExamsPrefsAction()));
		setRollForwardSessionConfig(form.getRollForwardSessionConfig());
		setSessionToRollSessionConfigForwardFrom(form.getSessionToRollSessionConfigForwardFrom());
		setRollForwardReservations(form.getRollForwardReservations());
		setSessionToRollReservationsForwardFrom(form.getSessionToRollReservationsForwardFrom());
		setRollForwardReservationsSubjectIds(fromList(form.getRollForwardReservationsSubjectIds()));
		setRollForwardCurriculumReservations(form.getRollForwardCurriculumReservations());
		setRollForwardCourseReservations(form.getRollForwardCourseReservations());
		setRollForwardGroupReservations(form.getRollForwardGroupReservations());
		setExpirationCourseReservations(fromDate(form.getExpirationCourseReservations()));
		setExpirationCurriculumReservations(fromDate(form.getExpirationCurriculumReservations()));
		setExpirationGroupReservations(fromDate(form.getExpirationGroupReservations()));
		setCreateStudentGroupsIfNeeded(form.getCreateStudentGroupsIfNeeded());
		setRollForwardOfferingCoordinators(form.getRollForwardOfferingCoordinators());
		setRollForwardOfferingCoordinatorsSubjectIds(fromList(form.getRollForwardOfferingCoordinatorsSubjectIds())); 
		setRollForwardTeachingRequests(form.getRollForwardTeachingRequests());
		setRollForwardTeachingRequestsSubjectIds(fromList(form.getRollForwardTeachingRequestsSubjectIds()));
		setRollForwardDistributions(toString(form.getRollForwardDistributions()));
		setRollForwardPeriodicTasks(form.getRollForwardPeriodicTasks());
		setSessionToRollPeriodicTasksFrom(form.getSessionToRollPeriodicTasksFrom());
		setStartDateCourseReservations(fromDate(form.getStartDateCourseReservations()));
		setStartDateCurriculumReservations(fromDate(form.getStartDateCurriculumReservations()));
		setStartDateGroupReservations(fromDate(form.getStartDateGroupReservations()));
		setRollForwardLearningManagementSystems(form.getRollForwardLearningManagementSystems());
		setSessionToRollLearningManagementSystemsForwardFrom(form.getSessionToRollLearningManagementSystemsForwardFrom());
		setRollForwardWaitListsProhibitedOverrides(form.getRollForwardWaitListsProhibitedOverrides());
		setRollForwardParentOfferings(form.getRollForwardParentOfferings());
		setRollForwardUniversalReservations(form.getRollForwardUniversalReservations());
		setExpirationUniversalReservations(fromDate(form.getExpirationUniversalReservations()));
		setStartDateUniversalReservations(fromDate(form.getStartDateUniversalReservations()));
	}
	
	protected boolean validateRollForward(RollForwardErrors action, Session sessionToRollForwardTo, Long sessionIdToRollForwardFrom, String rollForwardType, Collection checkCollection){
		if (!validateRollForwardSessionHasNoDataOfType(action, sessionToRollForwardTo, rollForwardType,  checkCollection))
			return false;
		Session sessionToRollForwardFrom = Session.getSessionById(sessionIdToRollForwardFrom);
		if (sessionToRollForwardFrom == null){
			action.addFieldError("mustSelectSession", MSG.errorRollForwardMissingFromSession(rollForwardType));
			return false;
		}
		if (sessionToRollForwardFrom.equals(sessionToRollForwardTo)){
			action.addFieldError("sessionsMustBeDifferent", MSG.errorRollForwardSessionsMustBeDifferent(rollForwardType, sessionToRollForwardTo.getLabel()));
			return false;
		}
		return true;
	}
	
	private boolean validateRollForwardSessionHasNoDataOfType(RollForwardErrors action, Session sessionToRollForwardTo, String rollForwardType, Collection checkCollection){
		if (checkCollection != null && !checkCollection.isEmpty()){
			action.addFieldError("sessionHasData", MSG.errorRollForwardNoData(rollForwardType, sessionToRollForwardTo.getLabel()));
			return false;
		}
		return true;
	}
}
