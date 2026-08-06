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
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.CancelledClassAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.DistributionMode;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrorLogger;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.StudentEnrollmentMode;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PitStudentClassEnrollment;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ClassInstructorDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CourseRequestDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LastLikeCourseDemandDAO;
import org.unitime.timetable.model.dao.OfferingCoordinatorDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.StudentClassEnrollmentDAO;
import org.unitime.timetable.model.dao.TeachingRequestDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;

public class SessionRollForwardValidators {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	private RollForwardSessionInterface iForm;
	private RollForwardErrorLogger iErrors;
	
	public SessionRollForwardValidators(RollForwardSessionInterface form, RollForwardErrorLogger errors) {
		iForm = form;
		iErrors = errors;
	}
	
	public boolean validateCourseOfferingRollForward(Session toAcadSession, SubjectArea toSubjectArea){
		boolean ret = true;
		if (iForm.getRollForwardCourseOfferings()) {
			if (iForm.getSubpartLocationPrefsAction() != null 
					&& iForm.getSubpartLocationPrefsAction() != RollAction.DO_NOT_ROLL_ACTION
					&& iForm.getSubpartLocationPrefsAction() != RollAction.ROLL_PREFS_ACTION){
				iErrors.addFieldError("invalidSubpartLocationAction", MSG.errorRollForwardInvalidSubpartLocationAction(iForm.getSubpartLocationPrefsAction().name()));
				ret = false;
			}
			if (iForm.getSubpartTimePrefsAction() != null 
					&& iForm.getSubpartTimePrefsAction() != RollAction.DO_NOT_ROLL_ACTION
					&& iForm.getSubpartTimePrefsAction() != RollAction.ROLL_PREFS_ACTION){
				iErrors.addFieldError("invalidSubpartTimeAction", MSG.errorRollForwardInvalidSubpartTimeAction(iForm.getSubpartLocationPrefsAction().name()));
				ret = false;
			}
			if (iForm.getClassPrefsAction() != null 
					&& iForm.getClassPrefsAction() != RollAction.DO_NOT_ROLL_ACTION
					&& iForm.getClassPrefsAction() != RollAction.PUSH_UP_ACTION
					&& iForm.getClassPrefsAction() != RollAction.ROLL_PREFS_ACTION){
				iErrors.addFieldError("invalidClassAction", MSG.errorRollForwardInvalidClassAction(iForm.getClassPrefsAction().name()));
				ret = false;
			}
			if (iForm.getRollForwardDistributions() != null
					&& iForm.getRollForwardDistributions() != DistributionMode.ALL
					&& iForm.getRollForwardDistributions() != DistributionMode.MIXED
					&& iForm.getRollForwardDistributions() != DistributionMode.SUBPART
					&& iForm.getRollForwardDistributions() != DistributionMode.NONE) {
				iErrors.addFieldError("invalidDistributionAction", MSG.errorRollForwardInvalidDistributionAction(iForm.getRollForwardDistributions().name()));
				ret = false;
			}
			if (iForm.getCancelledClassAction() != null
					&& iForm.getCancelledClassAction() != CancelledClassAction.KEEP
					&& iForm.getCancelledClassAction() != CancelledClassAction.REOPEN
					&& iForm.getCancelledClassAction() != CancelledClassAction.SKIP){
						iErrors.addFieldError("invalidCancelAction", MSG.errorRollForwardInvalidCancelAction(iForm.getCancelledClassAction().name()));
				ret = false;
			}
			if (!validateRollForward(toAcadSession, iForm.getSessionToRollCourseOfferingsForwardFrom(), MSG.rollForwardCourseOfferings(), new ArrayList<CourseOffering>()))
				ret = false;
			CourseOfferingDAO coDao = CourseOfferingDAO.getInstance();
			if (toSubjectArea == null) {
				for (Long id: iForm.getRollForwardSubjectAreaIds()) {
					String queryStr = "from CourseOffering co where co.subjectArea.session.uniqueId = "
						+ toAcadSession.getUniqueId().toString()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + id;
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardCourseOfferings() + ": " + id), coDao.getSession().createQuery(queryStr, CourseOffering.class).list()))
						ret = false;
				}
			} else {
				String queryStr = "from CourseOffering co where co.subjectArea.session.uniqueId = "
						+ toAcadSession.getUniqueId()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + toSubjectArea.getUniqueId();
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardCourseOfferings() + ": " + toSubjectArea.getSubjectAreaAbbreviation()), coDao.getSession().createQuery(queryStr, CourseOffering.class).list()))
						ret = false;
			}
		}
		return ret;
	}
	
	protected boolean validateRollForward(Session sessionToRollForwardTo, Long sessionIdToRollForwardFrom, String rollForwardType, Collection checkCollection){
		if (!validateRollForwardSessionHasNoDataOfType(sessionToRollForwardTo, rollForwardType,  checkCollection))
			return false;
		Session sessionToRollForwardFrom = Session.getSessionById(sessionIdToRollForwardFrom);
		if (sessionToRollForwardFrom == null){
			iErrors.addFieldError("mustSelectSession", MSG.errorRollForwardMissingFromSession(rollForwardType));
			return false;
		}
		if (sessionToRollForwardFrom.equals(sessionToRollForwardTo)){
			iErrors.addFieldError("sessionsMustBeDifferent", MSG.errorRollForwardSessionsMustBeDifferent(rollForwardType, sessionToRollForwardTo.getLabel()));
			return false;
		}
		return true;
	}
	
	private boolean validateRollForwardSessionHasNoDataOfType(Session sessionToRollForwardTo, String rollForwardType, Collection checkCollection){
		if (checkCollection != null && !checkCollection.isEmpty()){
			iErrors.addFieldError("sessionHasData", MSG.errorRollForwardNoData(rollForwardType, sessionToRollForwardTo.getLabel()));
			return false;
		}
		return true;
	}
	
	public boolean validateClassInstructorRollForward(Session toAcadSession, SubjectArea toSubjectArea) {
		if (iForm.getRollForwardClassInstructors()){
			boolean ret = true;
			// if (!validateRollForward(toAcadSession, iForm.getSessionToRollCourseOfferingsForwardFrom(), MSG.rollForwardClassInstructors(), new ArrayList<ClassInstructor>()))
			// ret = false;
			ClassInstructorDAO ciDao = ClassInstructorDAO.getInstance();
			if (toSubjectArea == null) {
				for (Long subjectId: iForm.getRollForwardClassInstrSubjectIds()) {
					String queryStr = "from ClassInstructor c  inner join c.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co where c.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = "
						+ toAcadSession.getUniqueId().toString()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + subjectId;
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardClassInstructors() + ": " + subjectId), ciDao.getSession().createQuery(queryStr, ClassInstructor.class).list()))
						ret = false;
				}
			} else {
				String queryStr = "from ClassInstructor c  inner join c.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co where c.classInstructing.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = "
						+ toAcadSession.getUniqueId().toString()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + toSubjectArea.getUniqueId();
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardClassInstructors() + ": " + toSubjectArea.getSubjectAreaAbbreviation()), ciDao.getSession().createQuery(queryStr, ClassInstructor.class).list()))
						ret = false;
			}
			return ret;
		} else {
			return true;
		}
	}
	
	public boolean validateOfferingCoordinatorsRollForward(Session toAcadSession, SubjectArea toSubjectArea){
		if (iForm.getRollForwardOfferingCoordinators()){
			boolean ret = true;
			// if (!validateRollForward(toAcadSession, iForm.getSessionToRollCourseOfferingsForwardFrom(), MSG.rollForwardOfferingCoordinators(), new ArrayList<OfferingCoordinator>()))
			// ret = false;
			OfferingCoordinatorDAO ocDao = OfferingCoordinatorDAO.getInstance();
			if (toSubjectArea == null) {
				for (Long subjectId: iForm.getRollForwardOfferingCoordinatorsSubjectIds()) {
					String queryStr = "from OfferingCoordinator c inner join c.offering.courseOfferings as co where c.offering.session.uniqueId = "
						+ toAcadSession.getUniqueId().toString()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + subjectId;
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardOfferingCoordinators() + ": " + subjectId), ocDao.getSession().createQuery(queryStr, OfferingCoordinator.class).list()))
						ret = false;
				}
			} else {
				String queryStr = "from OfferingCoordinator c inner join c.offering.courseOfferings as co where c.offering.session.uniqueId = "
						+ toAcadSession.getUniqueId().toString()
						+ " and co.isControl = true and co.subjectArea.uniqueId  = "
					    + toSubjectArea.getUniqueId();
					if (!validateRollForwardSessionHasNoDataOfType(toAcadSession, (MSG.rollForwardOfferingCoordinators() + ": " + toSubjectArea.getSubjectAreaAbbreviation()), ocDao.getSession().createQuery(queryStr, OfferingCoordinator.class).list()))
						ret = false;
			}
			return ret;
		} else {
			return true;
		}
	}
	
	public boolean validateTeachingRequestsRollForward(Session toAcadSession, SubjectArea toSubjectArea){
		if (iForm.getRollForwardTeachingRequests()) {
			if (iForm.getRollForwardTeachingRequestsSubjectIds() == null || iForm.getRollForwardTeachingRequestsSubjectIds().size() == 0) {
				iErrors.addFieldError("mustSelectDepartment", MSG.errorRollForwardGeneric(MSG.rollForwardTeachingRequests(), MSG.infoNoSubjectAreaSelected()));
				return false;
			} else if (toSubjectArea == null) {
				return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardTeachingRequests(),
						TeachingRequestDAO.getInstance().getSession().createQuery(
								"select tr from TeachingRequest tr inner join tr.offering.courseOfferings co where co.isControl = true and co.subjectArea.uniqueId in :subjectIds",
								TeachingRequest.class)
					.setParameterList("subjectIds", iForm.getRollForwardTeachingRequestsSubjectIds(), Long.class).list());
			} else {
				return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardTeachingRequests(),
						TeachingRequestDAO.getInstance().getSession().createQuery(
								"select tr from TeachingRequest tr inner join tr.offering.courseOfferings co where co.isControl = true and co.subjectArea.uniqueId = :subjectId",
								TeachingRequest.class)
					.setParameter("subjectId", toSubjectArea.getUniqueId()).list());
			}
		} else {
			return true;
		}
	}
	
	public boolean validateLearningManagementSystemRollForward(Session toAcadSession){
		if (iForm.getRollForwardLearningManagementSystems()){
			return validateRollForward(toAcadSession, iForm.getSessionToRollLearningManagementSystemsForwardFrom(), MSG.rollForwardLMSInfo(), LearningManagementSystemInfo.findAll(toAcadSession.getUniqueId()));			
 		} else {
 			return true;
 		}
	}
	
	public boolean validateDatePatternRollForward(Session toAcadSession){
		if (iForm.getRollForwardDatePatterns()){
			return validateRollForward( toAcadSession, iForm.getSessionToRollDatePatternsForwardFrom(), MSG.rollForwardDatePatterns(), DatePattern.findAll(toAcadSession, true, null, null));			
 		} else {
 			return true;
 		}
	}
	
	public boolean validateTimePatternRollForward(Session toAcadSession){
		if (iForm.getRollForwardTimePatterns()){
			return validateRollForward(toAcadSession, iForm.getSessionToRollTimePatternsForwardFrom(), MSG.rollForwardTimePatterns(), TimePattern.findAll(toAcadSession, null));			
 		} else {
 			return true;
 		}
	}
	
	public boolean validateDepartmentRollForward(Session toAcadSession) {
		if (iForm.getRollForwardDepartments()) {
			return validateRollForward(toAcadSession, iForm.getSessionToRollDeptsFowardFrom(), MSG.rollForwardDepartments(), Department.findAll(toAcadSession.getUniqueId()));			
		} else {
			return true;
		}
	}
	
	public boolean validateManagerRollForward(Session toAcadSession) {
		if (iForm.getRollForwardManagers()){
			TimetableManagerDAO tmDao = TimetableManagerDAO.getInstance();
			return validateRollForward(toAcadSession, iForm.getSessionToRollManagersForwardFrom(), MSG.rollForwardManagers(), tmDao.getSession().createQuery("from TimetableManager tm inner join tm.departments d where d.session.uniqueId =" + toAcadSession.getUniqueId().toString(), TimetableManager.class).list());
		} else {
			return true;
		}
	}
	
	public boolean validateBuildingAndRoomRollForward(Session toAcadSession) {
		if (iForm.getRollForwardRoomData()){
			boolean vbf = validateRollForward(toAcadSession, iForm.getSessionToRollRoomDataForwardFrom(), MSG.rollForwardBuildings(), new ArrayList<Building>());
			boolean vb = validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardBuildings(), Building.findAll(toAcadSession.getUniqueId()));
			boolean vrf = validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardRooms(), Location.findAll(toAcadSession.getUniqueId()));
			RoomFeatureDAO rfDao = RoomFeatureDAO.getInstance();
			boolean vr = validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardRoomsFeatures(), rfDao.getSession().createQuery("from RoomFeature rf where rf.department.session.uniqueId = " + toAcadSession.getUniqueId().toString(), RoomFeature.class).list());
			RoomGroupDAO rgDao = RoomGroupDAO.getInstance();
			boolean vn = validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardRoomsGroups(), rgDao.getSession().createQuery("from RoomGroup rg where rg.session.uniqueId = " + toAcadSession.getUniqueId().toString() + " and rg.global = false", RoomGroup.class).list());
			return vbf && vb && vrf && vr && vn;
		} else {
			return true;
		}
	}
	
	public boolean validateSubjectAreaRollForward(Session toAcadSession){
		if (iForm.getRollForwardSubjectAreas()){
			return validateRollForward(toAcadSession, iForm.getSessionToRollSubjectAreasForwardFrom(), MSG.rollForwardSubjectAreas(), SubjectArea.getSubjectAreaList(toAcadSession.getUniqueId()));			
		} else {
			return true;
		}
	}
		
	public void validateInstructorDataRollForward(Session toAcadSession){
		if (iForm.getRollForwardInstructorData()){
			DepartmentalInstructorDAO diDao = DepartmentalInstructorDAO.getInstance();
			validateRollForward(toAcadSession, iForm.getSessionToRollInstructorDataForwardFrom(), MSG.rollForwardInstructors(), diDao.getSession().createQuery("from DepartmentalInstructor di where di.department.session.uniqueId = " + toAcadSession.getUniqueId().toString(), DepartmentalInstructor.class).list());			
		}		
	}
	
	public boolean validateExamConfigurationRollForward(Session toAcadSession){
		if (iForm.getRollForwardExamConfiguration()){
			ExamPeriodDAO epDao = ExamPeriodDAO.getInstance();
			return validateRollForward(toAcadSession, iForm.getSessionToRollExamConfigurationForwardFrom(), MSG.rollForwardExamConfiguration(), epDao.getSession().createQuery("from ExamPeriod ep where ep.session.uniqueId = " + toAcadSession.getUniqueId().toString(), ExamPeriod.class).list());			
		} else {
			return true;
		}
	}

	public boolean validateMidtermExamRollForward(Session toAcadSession){
		if (iForm.getRollForwardMidtermExams()){
			ExamDAO eDao = ExamDAO.getInstance();
			return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardMidtermExams(), eDao.getSession().createQuery("from Exam e where e.session.uniqueId = " + toAcadSession.getUniqueId().toString() +" and e.examType.type = " + ExamType.sExamTypeMidterm, Exam.class).list());			
		} else {
			return true;
		}
	}

	public boolean validateFinalExamRollForward(Session toAcadSession){
		if (iForm.getRollForwardFinalExams()){
			ExamDAO epDao = ExamDAO.getInstance();
			return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardFinalExams(), epDao.getSession().createQuery("from Exam e where e.session.uniqueId = " + toAcadSession.getUniqueId().toString() +" and e.examType.type = " + ExamType.sExamTypeFinal, Exam.class).list());			
		} else {
			return true;
		}
	}

	public boolean validateLastLikeDemandRollForward(Session toAcadSession){
		if (iForm.getRollForwardStudents()) {
		    if (iForm.getRollForwardStudentsMode() == StudentEnrollmentMode.LAST_LIKE) {
		        return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardLastLikeStudentCourseRequests(), 
		                LastLikeCourseDemandDAO.getInstance().getSession().createQuery("from LastLikeCourseDemand d where d.subjectArea.session.uniqueId = " + toAcadSession.getUniqueId().toString(), LastLikeCourseDemand.class).list());
		    } else if (iForm.getRollForwardStudentsMode() == StudentEnrollmentMode.STUDENT_CLASS_ENROLLMENTS) {
		    	return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardStudentClassEnrollments(), 
		                StudentClassEnrollmentDAO.getInstance().getSession().createQuery("from StudentClassEnrollment d where d.courseOffering.subjectArea.session.uniqueId = " + toAcadSession.getUniqueId().toString(), StudentClassEnrollment.class).list());
		    } else if (iForm.getRollForwardStudentsMode() == StudentEnrollmentMode.STUDENT_COURSE_REQUESTS) {
		    	return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardCourseRequests(), 
                        CourseRequestDAO.getInstance().getSession().createQuery("from CourseRequest r where r.courseOffering.subjectArea.session.uniqueId = " + toAcadSession.getUniqueId().toString(), CourseRequest.class).list());
		    } else if (iForm.getRollForwardStudentsMode() == StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS) {
		    	return validateRollForwardSessionHasNoDataOfType(toAcadSession, MSG.rollForwardPITStudentClassEnrollments(), 
		                StudentClassEnrollmentDAO.getInstance().getSession().createQuery("from PitStudentClassEnrollment d where d.pitCourseOffering.subjectArea.session.uniqueId = " + toAcadSession.getUniqueId().toString(), PitStudentClassEnrollment.class).list());
		    } else {
				iErrors.addFieldError("invalidCancelAction", MSG.errorRollForwardInvalidCourseDemandAction(iForm.getRollForwardStudentsMode() == null ? "NULL" : iForm.getRollForwardStudentsMode().name()));
				return false;
		    }
		} else {
			return true;
		}
	}

	public boolean validateCurriculaRollForward(Session toAcadSession){
		if (iForm.getRollForwardCurricula()){
			CurriculumDAO curDao = CurriculumDAO.getInstance();
			return validateRollForward(toAcadSession, iForm.getSessionToRollCurriculaForwardFrom(), MSG.rollForwardCurricula(), curDao.getSession().createQuery("from Curriculum c where c.department.session.uniqueId = " + toAcadSession.getUniqueId().toString(), Curriculum.class).list());			
		} else {
			return true;
		}
	}
	
	public void validateSessionToRollForwardTo(){
		Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
		if (toAcadSession == null){
   			iErrors.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
   			return;
		}
		
		validateDepartmentRollForward(toAcadSession);
		validateManagerRollForward(toAcadSession);
		validateBuildingAndRoomRollForward(toAcadSession);
		validateDatePatternRollForward(toAcadSession);
		validateTimePatternRollForward(toAcadSession);
		validateLearningManagementSystemRollForward(toAcadSession);
		validateSubjectAreaRollForward(toAcadSession);
		validateCourseOfferingRollForward(toAcadSession, null);
		validateTeachingRequestsRollForward(toAcadSession, null);
		validateClassInstructorRollForward(toAcadSession, null);
		validateOfferingCoordinatorsRollForward(toAcadSession, null);
		validateExamConfigurationRollForward(toAcadSession);
		validateMidtermExamRollForward(toAcadSession);
		validateFinalExamRollForward(toAcadSession);
		validateLastLikeDemandRollForward(toAcadSession);
		validateCurriculaRollForward(toAcadSession);
		validatePeriodicTasksForward(toAcadSession);
	}
	
	public void validatePeriodicTasksForward(Session toAcadSession){
		if (iForm.getRollForwardPeriodicTasks()){
			validateRollForward(toAcadSession, iForm.getSessionToRollCurriculaForwardFrom(), MSG.rollForwardScheduledTasks(), null);			
		}
	}
}
