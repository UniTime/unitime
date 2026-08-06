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
package org.unitime.timetable.util.queue;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrorLogger;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrors;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.util.SessionRollForward;
import org.unitime.timetable.util.SessionRollForwardValidators;

public class RollForwardQueueItem extends QueueItem {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	private static final long serialVersionUID = 1L;

	private RollForwardSessionInterface iForm;
	private int iProgress = 0;
	private RollForwardErrors iErrors = new RollForwardErrors();
	
	public RollForwardQueueItem(Session session, UserContext owner, RollForwardSessionInterface form) {
		super(session, owner);
		iForm = form;
	}
	
	public RollForwardErrors getErrors() {
		return iErrors;
	}
	
	public RollForwardSessionInterface getForm() {
		return iForm;
	}
	
	@Override
	public void error(Object message, Throwable t) {
		super.error(message, t);
		setError(t);
	}
	
	@Override
	protected void execute() throws Exception {
		RollForwardErrorLogger logger = new RollForwardErrorLogger() {
			@Override
			public void addFieldError(String type, String message) {
				iErrors.addFieldError(type, message);
				error(message);
			}
			public boolean isEmpty() {
				return iErrors.isEmpty();
			}
		};
		SessionRollForward sessionRollForward = new SessionRollForward(this);
        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
		if (toAcadSession == null){
			logger.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
		}
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		SessionRollForwardValidators validator = new SessionRollForwardValidators(iForm, logger);

    	if (logger.isEmpty() && iForm.getRollForwardDepartments()) {
			Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardDepartments() + " ...");
				if (validator.validateDepartmentRollForward(toAcadSession))
					sessionRollForward.rollDepartmentsForward(logger, iForm);	
		        tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardDepartments()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
        }
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardSessionConfig()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardSessionConfiguration() + " ...");
				sessionRollForward.rollSessionConfigurationForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardSessionConfiguration()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardManagers()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardManagers() + " ...");
				if (validator.validateManagerRollForward(toAcadSession))
					sessionRollForward.rollManagersForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardManagers()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardRoomData()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardRooms() + " ...");
				if (validator.validateBuildingAndRoomRollForward(toAcadSession))
					sessionRollForward.rollBuildingAndRoomDataForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardRooms()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;
        
        if (logger.isEmpty() && iForm.getRollForwardDatePatterns()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardDatePatterns() + " ...");
				if (validator.validateDatePatternRollForward(toAcadSession))
					sessionRollForward.rollDatePatternsForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardDatePatterns()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
        }
        iProgress++;
        
        if (logger.isEmpty() && iForm.getRollForwardTimePatterns()) {
        	Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardTimePatterns() + " ...");
				if (validator.validateTimePatternRollForward(toAcadSession))
					sessionRollForward.rollTimePatternsForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardTimePatterns()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
        }
        iProgress++;

        if (logger.isEmpty() && iForm.getRollForwardLearningManagementSystems()) {
        	Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardLMSInfo() + " ...");
				if (validator.validateLearningManagementSystemRollForward(toAcadSession))
					sessionRollForward.rollLearningManagementSystemInfoForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardLMSInfo()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
        }
        iProgress++;
        
        if (logger.isEmpty() && iForm.getRollForwardSubjectAreas()) {
        	Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardSubjectAreas() + " ...");
				if (validator.validateSubjectAreaRollForward(toAcadSession))
					sessionRollForward.rollSubjectAreasForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardSubjectAreas()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardInstructorData()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardInstructors() + " ...");
				sessionRollForward.rollInstructorDataForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardInstructors()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

		if (logger.isEmpty() && iForm.getRollForwardCourseOfferings()) {
			setStatus(MSG.rollForwardCourseOfferings() + " ...");
			sessionRollForward.rollCourseOfferingsForward(logger, iForm);
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardClassInstructors()) {
    		setStatus(MSG.rollForwardClassInstructors() + " ...");
    		sessionRollForward.rollClassInstructorsForward(logger, iForm);
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardOfferingCoordinators()) {
			setStatus(MSG.rollForwardOfferingCoordinators() + " ...");
			sessionRollForward.rollOfferingCoordinatorsForward(logger, iForm);
    	}
		iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardTeachingRequests()) {
			setStatus(MSG.rollForwardTeachingRequests() + " ...");
			sessionRollForward.rollTeachingRequestsForward(logger, iForm);
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getAddNewCourseOfferings()) {
    		setStatus(MSG.rollForwardNewCourses() + " ...");
    		sessionRollForward.addNewCourseOfferings(logger, iForm);
    	}
        iProgress++;

		if (logger.isEmpty() && iForm.getRollForwardExamConfiguration()) {
			Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardExamConfiguration() + " ...");
				if (validator.validateExamConfigurationRollForward(toAcadSession))
					sessionRollForward.rollExamConfigurationDataForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardExamConfiguration()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardMidtermExams()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardMidtermExams() + " ...");
	    		if (validator.validateMidtermExamRollForward(toAcadSession))
	    			sessionRollForward.rollMidtermExamsForward(logger, iForm);
	    		tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardMidtermExams()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardFinalExams()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardFinalExams() + " ...");
				if (validator.validateFinalExamRollForward(toAcadSession))
					sessionRollForward.rollFinalExamsForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardFinalExams()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

		if (logger.isEmpty() && iForm.getRollForwardStudents()) {
			Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardStudents() + " ...");
				if (validator.validateLastLikeDemandRollForward(toAcadSession))
					sessionRollForward.rollStudentsForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardStudents()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardCurricula()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardCurricula() + " ...");
				if (validator.validateCurriculaRollForward(toAcadSession))
					sessionRollForward.rollCurriculaForward(logger, iForm);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardCurricula()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

    	if (logger.isEmpty() && iForm.getRollForwardReservations()) {
    		Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardReservations() + " ...");
	    	    sessionRollForward.rollReservationsForward(logger, iForm);
	    	    tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardReservations()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

        if (logger.isEmpty() && iForm.getRollForwardPeriodicTasks()) {
        	Transaction tx = hibSession.beginTransaction();
			try {
				setStatus(MSG.rollForwardScheduledTasks() + " ...");
	    	    sessionRollForward.rollPeriodicTasksForward(logger, iForm);
	    	    tx.commit();
			} catch (Exception e) {
				tx.rollback();
				error(MSG.errorRollForwardFailedAll(MSG.rollForwardScheduledTasks()), e);
				logger.addFieldError("rollForward", e.getMessage());
			}
			hibSession.clear();
    	}
        iProgress++;

        if (!iErrors.isEmpty()) {
        	String lastError = iErrors.get(iErrors.size() - 1).getMessage();
        	setError(new Exception(lastError));
        } else {
        	log(MSG.logAllDone());
        }
	}

	@Override
	public String name() {
		List<String> names = new ArrayList<String>();
    	if (iForm.getRollForwardDepartments()) names.add(MSG.rollForwardDepartments());
		if (iForm.getRollForwardSessionConfig()) names.add(MSG.rollForwardConfiguration());
    	if (iForm.getRollForwardManagers()) names.add(MSG.rollForwardManagers());
    	if (iForm.getRollForwardRoomData()) names.add(MSG.rollForwardRooms());
		if (iForm.getRollForwardDatePatterns()) names.add(MSG.rollForwardDatePatterns());
        if (iForm.getRollForwardTimePatterns()) names.add(MSG.rollForwardTimePatterns());
        if (iForm.getRollForwardLearningManagementSystems()) names.add(MSG.rollForwardLMS());
    	if (iForm.getRollForwardSubjectAreas()) names.add(MSG.rollForwardSubjectAreas());
    	if (iForm.getRollForwardInstructorData()) names.add(MSG.rollForwardInstructors());
    	if (iForm.getRollForwardCourseOfferings()) names.add(MSG.rollForwardCourseOfferings());
    	if (iForm.getRollForwardClassInstructors()) names.add(MSG.rollForwardClassInstructors());
    	if (iForm.getRollForwardOfferingCoordinators()) names.add(MSG.rollForwardOfferingCoordinators());
    	if (iForm.getRollForwardTeachingRequests()) names.add(MSG.rollForwardTeachingRequests());
    	if (iForm.getAddNewCourseOfferings()) names.add(MSG.rollForwardNewCourses());
    	if (iForm.getRollForwardExamConfiguration()) names.add(MSG.rollForwardExamConfiguration());
    	if (iForm.getRollForwardMidtermExams()) names.add(MSG.rollForwardMidtermExams());
    	if (iForm.getRollForwardFinalExams()) names.add(MSG.rollForwardFinalExams());
    	if (iForm.getRollForwardStudents()) names.add(MSG.rollForwardStudents());
    	if (iForm.getRollForwardCurricula()) names.add(MSG.rollForwardCurricula());
    	if (iForm.getRollForwardReservations()) names.add(MSG.rollForwardReservations());
    	if (iForm.getRollForwardPeriodicTasks()) names.add(MSG.rollForwardScheduledTasks());
    	String name = names.toString().replace("[", "").replace("]", "");
    	if (name.length() > 50) name = name.substring(0, 47) + "...";
    	return name;
	}

	@Override
	public double progress() {
		return 100 * iProgress / 21;
	}

	@Override
	public String type() {
		return "Roll Forward";
	}
	
}