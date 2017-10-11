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
package org.unitime.timetable.gwt.resources;

import java.util.Map;

/**
 * @author Tomas Muller
 */
public interface CPSolverMessages extends Messages, Constants {

	@DefaultStringMapValue({})
	Map<String, String> courseInfoMessages();
	
	@DefaultStringMapValue({})
	Map<String, String> courseObjectives();
	
	@DefaultStringMapValue({})
	Map<String, String> examInfoMessages();
	
	@DefaultStringMapValue({})
	Map<String, String> studentInfoMessages();
	
	@DefaultStringMapValue({})
	Map<String, String> instructorInfoMessages();
	
	@DefaultMessage("loading class {0}")
	String debugLoadingClass(String className);
	
	@DefaultMessage("Instructor {0} mapped with student {1}")
	String debugStudentInstructorPair(String extId, Long studentId);
	
	@DefaultMessage("Not created constraint {0} between {1} (all variables are committed).")
	String debugDistributionAllCommitted(String name, String classes);
	
	@DefaultMessage("Posted precedence constraint between {0} ({1}).")
	String infoAutomaticPrecedence(String classes, String preference);
	
	@DefaultMessage("Cross-listed course {0} does not have any course reservation.")
	String infoCrosslistNoCourseReservations(String course);
	
	@DefaultMessage("No student enrollments for course {0}.")
	String infoNoStudentInCourse(String course);
	
	@DefaultMessage("Loaded {0} enrollments of {1} students.")
	String infoEnrollmentsLoaded(int enrollments, int students);
	
	@DefaultMessage("Model successfully loaded.")
	String infoModelLoaded();
	
	@DefaultMessage("Using room availability that was updated on {0}.")
	String infoUsingRoomAvailability(String timeStamp);
	
	@DefaultMessage("Posted {0} constraint between {1} ({2})")
	String infoPostedConstraint(String type, String classes, String prefernce);
	
	@DefaultMessage("Solution successfully saved.")
	String infoSolutionSaved();
	
	@DefaultMessage("Class {0} is cancelled (class not loaded).")
	String warnCancelledClass(String className);
	
	@DefaultMessage("Class {0} has no time pattern selected (class not loaded). <i>If not changed, this class will be treated as Arrange {1} Hours.</i>")
	String warnNoTimePattern(String className, int arrangeHours);
	
	@DefaultMessage("Class {0} has no date pattern selected (class not loaded).")
	String warnNoDatePattern(String className);
	
	@DefaultMessage("Class {0} has no available room (class not loaded).")
	String warnNoRoom(String className);
	
	@DefaultMessage("Class {0} has no available rooms (class not loaded).")
	String warnNoRooms(String className);
	
	@DefaultMessage("Class {0} requires no room (number of rooms is set to zero), but it contains some room preferences.")
	String warnZeroRoomsButPref(String className);
	
	@DefaultMessage("Class {0} has {1} {2}, but {3} time pattern selected.")
	String warnWrongTimePattern(String classLabel, int minsPerWeek, String durationType, String timePattern);
	
	@DefaultMessage("minutes per week")
	String defaultDurationTypeName();
	
	@DefaultMessage("Time pattern {0} that is used by {1} has a time that goes over midnight. This is not allowed and the time {2} will be ignored.")
	String warnTimeOverMidnight(String timePattern, String classLabel, String startTime);
	
	@DefaultMessage("Class {0} has no available time (class not loaded).")
	String warnNoTime(String className);
	
	@DefaultMessage("Class {0} has too many possible placements ({1}). The class was not loaded in order to prevent out of memory exception. Please restrict the number of available rooms and/or times for this class.")
	String warnHugeDomain(String className, long domainSize);
	
	@DefaultMessage("Class {0} has quite a lot of possible placements ({1}). Solver may run too slow. If possible, please restrict the number of available rooms and/or times for this class.")
	String warnBigDomain(String className, long domainSize);
	
	@DefaultMessage("Class {0} has no available placement (class not loaded).")
	String warnNoPlacement(String className);
	
	@DefaultMessage("Class {0} has no available placement.")
	String warnNoPlacementInteractive(String className);
	
	@DefaultMessage("Unable to assign committed class {0} &larr; {1}.")
	String warnCannotAssignCommitted(String className, String placement);
		
	@DefaultMessage("Class {0} has no available placement (after enforcing consistency between the problem and committed solutions, class not loaded).")
	String warnNoPlacementAfterCommit(String className);
	
	@DefaultMessage("Class {0} has no available placement (after enforcing consistency between the problem and committed solutions).")
	String warnNoPlacementAfterCommitInteractive(String className);
	
	@DefaultMessage("Unable to assign {0} &larr; {1} (placement not valid).")
	String warnPlacementNotValid(String className, String placement);

	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;Reason:")
	String warnReasonFirstLine();
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;{0} = {1}")
	String warnReasonConflict(String className, String placement);
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;in constraint {0}")
	String warnReasonConstraint(String constraintName);
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;conflict with committed assignment {0} = {1} (in constraint {2})")
	String warnReasonConstraintCommitedAssignment(String committedClass, String placement, String constraintName);
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;{0}")
	String warnReasonNotValid(String reason);
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;instructor {0} not available")
	String warnReasonInstructorNotAvailable(String instructor);
	
	@DefaultMessage("<br>&nbsp;&nbsp;&nbsp;&nbsp;room {0} not available")
	String warnReasonRoomNotAvailable(String room);
	
	@DefaultMessage("Unable to assign class {0} &larr; {1}.")
	String warnCannotAssignClass(String className, String placement);
	
	@DefaultMessage("Unable to assign class {0} &larr; {1}: {2}")
	String warnCannotAssignClassWithReason(String className, String placement, String reason);
	
	@DefaultMessage("Constraint {0} was not loaded. Inconsistent values.")
	String warnFlexibleConstraintNotLoaded(String reference);
	
	@DefaultMessage("Constraint {0} was not recognized.")
	String warnDistributionConstraintNotKnown(String reference);
	
	@DefaultMessage("Minimize number of used rooms constraint not loaded due to the interactive mode of the solver.")
	String warnMinRoomUseInteractive();
	
	@DefaultMessage("Minimize number of used groups of time constraint not loaded due to the interactive mode of the solver.")
	String warnMinGroupUseInteractive();
	
	@DefaultMessage("Distribution constraint {0} is not implemented.")
	String warnDistributionConstraintNotImplemented(String reference);
	
	@DefaultMessage("Class {0} not found/loaded, but used in distribution preference {1}.")
	String warnClassNotLoadedButUsedInDistPref(String className, String preference);
	
	@DefaultMessage("Distribution preference {0} refers to a scheduling subpart {1} with no classes.")
	String warnBadDistributionObject(String constraint, String subpart);
	
	@DefaultMessage("Distribution preference {0} refers to unsupported object {1}.")
	String warnBadDistributionObjectNotSupported(String constraint, String subpart);
	
	@DefaultMessage("Distribution preference {0} refers to less than two classes.")
	String warnBadDistributionIncomplete(String constraint);
	
	@DefaultMessage("Inconsistent course reservations for course {0}.")
	String warnBadCourseReservations(String course);
	
	@DefaultMessage("Manager of class {0} has no solver group ({1}).")
	String warnNoSolverGroup(String className, String department);
	
	@DefaultMessage("Preference {0} not recognized.")
	String warnPrecedenceNotRecognized(String constraint);
	
	@DefaultMessage("Total number of course reservations is below the offering limit for instructional offering {0} ({1}<{2}).")
	String warnReservationBelowLimit(String course, int total, int limit);
	
	@DefaultMessage("Total number of course reservations exceeds the offering limit for instructional offering {0} ({1}>{2}).")
	String warnReservationsOverLimit(String course, int total, int limit);
	
	@DefaultMessage("No reserved space for students of course {0}.")
	String warnNoReservedSpaceForCourse(String course);
	
	@DefaultMessage("Too little space reserved in for course {0} ({1}<{2}).")
	String warnTooLittleSpaceInCourse(String course, int limit, int courseLimit);
	
	@DefaultMessage("Student {0} is supposed to be enrolled to {1}.")
	String warnStudentShouldBeInClass(Long id, String className);
	
	@DefaultMessage("Student {0} is NOT supposed to be enrolled to {1}, he/she should have {2} instead.")
	String warnStudentShouldNotBeInClassShouldBeInOther(Long id, String badClass, String goodClass);
	
	@DefaultMessage("Student {0} is NOT supposed to be enrolled to {1}.")
	String warnStudentShouldNotBeInClass(Long id, String className);
	
	@DefaultMessage("No class for course {0}.")
	String warnCourseWithNoClasses(String course);
	
	@DefaultMessage("Invalid student enrollment of student {0} in class {1} found.")
	String warnBadStudentEnrollment(Long id, String course);

	@DefaultMessage("Same instructor and overlapping time required:<br>&nbsp;&nbsp;&nbsp;&nbsp;{0} &larr; {1}<br>&nbsp;&nbsp;&nbsp;&nbsp;{2} &larr; {3}")
	String warnSameInstructorTimeConflict(String class1, String plac1, String class2, String plac2);
	
	@DefaultMessage("Same instructor, back-to-back time and rooms too far (distance={0}m) required:<br>&nbsp;&nbsp;&nbsp;&nbsp;{0} &larr; {1}<br>&nbsp;&nbsp;&nbsp;&nbsp;{2} &larr; {3}")
	String warnSameInstructorBackToBack(long distance, String class1, String plac1, String class2, String plac2);
	
	@DefaultMessage("Same room and overlapping time required:<br>&nbsp;&nbsp;&nbsp;&nbsp;{0} &larr; {1}<br>&nbsp;&nbsp;&nbsp;&nbsp;{2} &larr; {3}")
	String warnSameRoomTimeConflict(String class1, String plac1, String class2, String plac2);
	
	@DefaultMessage("Class {0} requires an invalid placement {1}.")
	String warnRequiresInvalidPlacement(String classLabel, String placement);
	
	@DefaultMessage("Class {0} requires an invalid placement {1}: {2}")
	String warnRequiresInvalidPlacementWithReason(String classLabel, String placement, String reason);
	
	@DefaultMessage("Unable to access room availability service, reason:{0}")
	String warnRoomAvailableServiceFailed(String error);
	
	@DefaultMessage("Unable to load room availability, reason: no dates")
	String warnRoomAvailableServiceNoDates();
	
	@DefaultMessage("Room availability is not available.")
	String warnRoomAvailableServiceNotAvailable();
	
	@DefaultMessage("Failed to parse automatic hierarchical constraint preference {0}")
	String warnFailedToParseAutomaticHierarchicalConstraint(String term);
	
	@DefaultMessage("Failed to parse automatic hierarchical constraint preference: unknown date pattern {0}")
	String warnFailedToParseAutomaticHierarchicalConstraintBadDatePattern(String constraint);
	
	@DefaultMessage("Failed to parse automatic student constraint preference {0}")
	String warnFailedToParseAutomaticStudentConstraint(String term);
	
	@DefaultMessage("Failed to load custom student course demands class, using last-like course demands instead.")
	String warnFailedLoadCustomStudentDemands();
	
	@DefaultMessage("Failed to load custom class weight provider, using the default one instead.")
	String warnFauledLoadCustomClassWeights();
	
	@DefaultMessage("Wrong jenrl between {0} and {1} (constraint={2} != computed={3}).<br>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{0} has students: {4}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{1} has students: {5}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;intersection: {6}")
	String warnWrongJenrl(String class1, String class2, long jenrl, long computed, String students1, String students2, String jenrlStudents);
	
	@DefaultMessage("Missing jenrl between {0} and {1} (computed={2}).<br>" +
			"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{0} has students: {3}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{1} has students: {4}<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;intersection: {5}")
	String warnMissingJenrl(String class1, String class2, long computed, String students1, String students2, String jenrlStudents);
	
	@DefaultMessage("Unexpected exception: {0}")
	String warnUnexpectedException(String message);
	
	@DefaultMessage("Student {0} enrolled in multiple classes of the same subpart {1}, {2}.")
	String warnStudentInMultipleClasses(Long id, String c1, String c2);
	
	@DefaultMessage("Student {0} not enrolled in any class of subpart {1}.")
	String warnStudentInNoClasses(Long id, String subpart);
	
	@DefaultMessage("Inconsistent number of student conflits (counter={0}, actual={1}).")
	String warnWrongStudentConflictCount(String counter, String actual);
	
	@DefaultMessage("Inconsistent number of committed student conflits (counter={0}, actual={1}).")
	String warnWrongCommittedStudentConflictCount(String counter, String actual);
	
	@DefaultMessage("Inconsistent number of distance student conflits (counter={0}, actual={1}).")
	String warnWrongDistanceStudentConflictCount(String counter, String actual);
	
	@DefaultMessage("Inconsistent number of hard student conflits (counter={0}, actual={1}).")
	String warnWrongHardStudentConflictCount(String counter, String actual);
	
	@DefaultMessage("Class limit exceeded for class {0} ({1}>{2}).")
	String warnClassLimitOver(String className, String students, int limit);
	
	@DefaultMessage("Student {0} enrolled to invalid class {1}.")
	String warnStudentInInvalidClass(Long id, String className);
	
	@DefaultMessage("Student {0} demands offerings {1}, but got {2}.")
	String warnStudentInWrongCourses(Long id, String requested, String got);
	
	@DefaultMessage("WARNING: Time {0} is no longer valid for class {1}.")
	String warnTimeNoLongerValid(String time, String className);
	
	@DefaultMessage("WARNING: Room(s) {0} are no longer valid for class {1}.")
	String warnRoomNoLongerValid(String room, String className);
	
	@DefaultMessage("Unable to refresh solution {0}, reason: {1}")
	String warnUnableToRefreshSolution(Long id, String message);
	
	@DefaultMessage("Solution {0} ignored -- it does not match with the owner(s) of the problem")
	String warnSolutionIgnored(Long id);
	
	@DefaultMessage("Unable to save assignment for class {0} ({1}) -- class (id:{2}) does not exist.")
	String warnUnableToSaveClassAssignmentNotExist(String className, String placement, Long id);
	
	@DefaultMessage("Unable to save assignment for class {0} ({1}) -- room (id:{2}) does not exist.")
	String warnUnableToSaveClassAssignmentRoomNotExist(String className, String placement, Long id);
	
	@DefaultMessage("Unable to save assignment for class {0} ({1}) -- time pattern (id:{2}) does not exist.")
	String warnUnableToSaveClassAssignmentTimePatternNotExist(String className, String placement, Long id);
	
	@DefaultMessage("Unable to save assignment for class {0} ({1}) -- none or wrong solution group assigned to the class.")
	String warnUnableToSaveClassAssignmentWrongOwner(String className, String placement);
	
	@DefaultMessage("Unable to save student enrollments for class {0}  -- none or wrong solution group assigned to the class")
	String warnUnableToSaveClassEnrollmentsWrongOwner(String className);
	
	@DefaultMessage("Unable to commit: {0}")
	String errorUnableToCommit(String message);
	
	@DefaultMessage("Unable to load input data, reason: {0}")
	String fatalLoadFailed(String error);
	
	@DefaultMessage("Unable to load solver group {0}.")
	String fatalUnableToLoadSolverGroup(Long id);
	
	@DefaultMessage("No solver group loaded.")
	String fatalNoSolverGroupLoaded();
	
	@DefaultMessage("Unable to load solution {0}.")
	String fatalUnableToLoadSolution(Long id);
	
	@DefaultMessage("No session loaded.")
	String fatalNoSessionLoaded();
	
	@DefaultMessage("No classes to load.")
	String fatalNoClassesToLoad();
	
	@DefaultMessage("Hibernate session not open.")
	String fatalHibernateSessionClosed();
	
	@DefaultMessage("The load was interrupted.")
	String fatalLoadInterrupted();
	
	@DefaultMessage("The save was interrupted.")
	String fatalSaveInterrupted();
	
	@DefaultMessage("Failed to restore previous assignments: {0}")
	String fataFailedToRestore(String message);
	
	@DefaultMessage("Unable to save timetable, reason: {0}")
	String fatalUnableToSaveTimetable(String message);
	
	@DefaultMessage("Loading input data ...")
	String statusLoadingInputData();
	
	@DefaultMessage("Awaiting commands ...")
	String statusReady();
	
	@DefaultMessage("Saving solution ...")
	String statusSavingSolution();
	
	@DefaultMessage("Assigning committed classes ...")
	String phaseAssignCommitted();
	
	@DefaultMessage("Purging invalid placements ...")
	String phasePurgeInvalidValues();
	
	@DefaultMessage("Loading instructor availabilities ...")
	String phaseLoadInstructorAvailabilities();
	
	@DefaultMessage("Loading instructor student conflicts ...")
	String phaseLoadInstructorStudentConflicts();
	
	@DefaultMessage("Loading room availabilities ...")
	String phaseLoadRoomAvailabilities();
	
	@DefaultMessage("Loading instructor distr. constraints for {0} ...")
	String phaseLoadInstructorGroupConstraints(String department);
	
	@DefaultMessage("Loading student conflicts with commited solutions ...")
	String phaseLoadCommittedStudentConflicts();
	
	@DefaultMessage("Creating student conflicts with commited solutions ...")
	String phaseMakeupCommittedStudentConflicts();
	
	@DefaultMessage("Loading classes ...")
	String phaseLoadingClasses();
	
	@DefaultMessage("Loading offerings ...")
	String phaseLoadingOfferings();
	
	@DefaultMessage("Loading distribution preferences ...")
	String phaseLoadingDistributions();
	
	@DefaultMessage("Posting automatic same_students constraints ...")
	String phasePostingSameStudents();
	
	@DefaultMessage("Posting automatic precedence constraints ...")
	String phasePostingAutomaticPrecedences();
	
	@DefaultMessage("Posting class limit constraints ...")
	String phasePostingClassLimits();
	
	@DefaultMessage("Loading students ...")
	String phaseLoadingStudents();
	
	@DefaultMessage("Loading student enrolments [{0}] ...")
	String phaseLoadingStudentEnrollemntsPhase(int phase);
	
	@DefaultMessage("Loading current student enrolments  ...")
	String phaseLoadingStudentEnrollemnts();
	
	@DefaultMessage("Loading other committed student enrolments  ...")
	String phaseLoadingOtherStudentEnrollments();
	
	@DefaultMessage("Initial sectioning ...")
	String phaseInitialSectioning();
	
	@DefaultMessage("Checking loaded enrollments ....")
	String phaseCheckingLoadedEnrollments();
	
	@DefaultMessage("Computing joined enrollments ...")
	String phaseComputingJenrl();
	
	@DefaultMessage("Creating initial assignment [{0}] ...")
	String phaseCreatingInitialAssignment(int phase);
	
	@DefaultMessage("Creating initial assignment ...")
	String phaseCreatingCommittedAssignment();
	
	@DefaultMessage("Posting automatic spread constraints ...")
	String phasePostingAutoSpreads();
	
	@DefaultMessage("Creating dept. spread constraints ...")
	String phasePostingDeptSpreads();
	
	@DefaultMessage("Creating subject spread constraints ...")
	String phasePostingSubjectSpreads();
	
	@DefaultMessage("Checking for inconsistencies...")
	String phaseCheckingForInconsistencies();
	
	@DefaultMessage("Loading room availability...")
	String phaseLoadingRoomAvailability();
	
	@DefaultMessage("Loading instructor availability...")
	String phaseLoadingInstructorAvailability();
	
	@DefaultMessage("Posting automatic {0} {1} constraints...")
	String phasePostingAutomaticConstraint(String preference, String type);
	
	@DefaultMessage("Posting automatic {0} {1} constraints between classes of pattern {2}...")
	String phasePostingAutomaticConstraintDatePattern(String preference, String type, String datePattern);
	
	@DefaultMessage("Posting automatic {0} {1} constraints for students...")
	String phasePostingAutomaticStudentConstraints(String preference, String type);
	
	@DefaultMessage("Creating best assignment ...")
	String phaseCreatingBestAssignment();
	
	@DefaultMessage("Creating current assignment ...")
	String phaseCreatingCurrentAssignment();
	
	@DefaultMessage("Creating initial assignment ...")
	String phaseCreatingInitialAssignment();
	
	@DefaultMessage("Done")
	String phaseDone();
	
	@DefaultMessage("Checking joined enrollments ...")
	String phaseCheckingJenrl();
	
	@DefaultMessage("Checking class limits...")
	String phaseCheckingClassLimits();
	
	@DefaultMessage("Checking enrollments...")
	String phaseCheckingEnrollments();
	
	@DefaultMessage("Committing solution ...")
	String phaseCommittingSolution();
	
	@DefaultMessage("Refreshing solution ...")
	String phaseRefreshingSolution();
	
	@DefaultMessage("Saving solver parameters ...")
	String phaseSavingSolverParameters();
	
	@DefaultMessage("Saving assignments ...")
	String phaseSavingAssignments();
	
	@DefaultMessage("Saving student enrollments ...")
	String phaseSavingStudentEnrollments();
	
	@DefaultMessage("Saving global info ...")
	String phaseSavingGlobalInfo();
	
	@DefaultMessage("Saving conflict-based statistics ...")
	String phaseSavingCBS();
	
	@DefaultMessage("Saving variable infos ...")
	String phaseSavingVariableInfos();
	
	@DefaultMessage("Saving btb instructor infos ...")
	String phaseSavingInstructorBTBInfos();
	
	@DefaultMessage("Saving distribution constraint infos ...")
	String phaseSavingGroupConstraintInfos();
	
	@DefaultMessage("Saving student enrollment infos ...")
	String phaseSavingStudentEnrollmentInfos();
	
	@DefaultMessage("Saving student group infos ...")
	String phaseSavingStudentGroupInfos();
	
	@DefaultMessage("Saving committed student enrollment infos ...")
	String phaseSavingCommittedStudentEnrollmentInfos();
	
	@DefaultMessage("Instructor {0}")
	String constraintInstructor(String name);
	
	@DefaultMessage("Room {0}")
	String constraintRoom(String name);
	
	@DefaultMessage("Distribution {0}")
	String constraintDistribution(String name);
	
	@DefaultMessage("Distribution {0}")
	String constraintFlexible(String name);
	
	@DefaultMessage("Balancing of department {0}")
	String constraintDeptBalancing(String name);
	
	@DefaultMessage("Same subpart spread {0}")
	String constraintSubpartBalancing(String name);
	
	@DefaultMessage("Class limit {0}")
	String constraintClassLimit(String name);
	
	@DefaultMessage("Joint enrollment {0}")
	String constraintJointEnrollment(String name);
	
	@DefaultMessage("Unknown")
	String constraintWithNoName();
	
	@DefaultMessage("spread")
	String nameSpreadConstraint();
	
	@DefaultMessage("class-limit")
	String nameClassLimitConstraint();
	
	@DefaultMessage("{0} conflicts with {1} {2} due to constraint {3}")
	String reasonConflictsWithCommittedClass(String placement, String other, String value, String constraint);
	
	@DefaultMessage("{0} is not valid due to constraint {1}")
	String reasonConflictConstraint(String placement, String constraint);
	
	@DefaultMessage("instructor {0} not available at {1} due to {2}")
	String reasonInstructorNotAvailableEvent(String instructor, String time, String event);
	
	@DefaultMessage("instructor {0} not available at {1}")
	String reasonInstructorNotAvailable(String instructor, String time);
	
	@DefaultMessage("placement {0} {1} is too far for instructor {2}")
	String reasonInstructorTooFar(String time, String room, String instructor);
	
	@DefaultMessage("room {0} not available at {1} due to {2}")
	String reasonRoomNotAvailableEvent(String instructor, String time, String event);
	
	@DefaultMessage("room {0} not available at {1}")
	String reasonRoomNotAvailable(String instructor, String time);
	
	@DefaultMessage("room or instructor not avaiable")
	String reasonNotKnown();
}
