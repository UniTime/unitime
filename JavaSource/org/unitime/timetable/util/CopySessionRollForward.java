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

import org.apache.commons.logging.Log;
import org.hibernate.Transaction;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrorLogger;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public class CopySessionRollForward extends SessionRollForward {
	CopyBetweenSessionHelper iCopyBetweenSessionHelper = null;

	public CopySessionRollForward(Log log, RollForwardSessionInterface form) {
		super(log);
		iCopyBetweenSessionHelper = new CopyBetweenSessionHelper(this, log, form);
	}
	
	public CopySessionRollForward(Log log, CopyBetweenSessionHelper helper) {
		super(log);
		iCopyBetweenSessionHelper = helper;
	}
	
	@Override
	public void rollBuildingAndRoomDataForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollRoomDataForwardFrom());
		
		iCopyBetweenSessionHelper.copyMergeRoomFeaturesToSession(fromSession, null);
		iCopyBetweenSessionHelper.copyMergeRoomGroupsToSession(fromSession, null);
		iCopyBetweenSessionHelper.copyMergeBuildingsToSession(fromSession);
		iCopyBetweenSessionHelper.copyMergeLocationsToSession(fromSession, null);
		iCopyBetweenSessionHelper.copyMergeTravelTimesToSession(fromSession);
		iCopyBetweenSessionHelper.copyMergeRoomPartitionsToSession(fromSession);

	}

	@Override
	public void rollManagersForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollManagersForwardFrom());
		iCopyBetweenSessionHelper.copyMergeTimetableManagersToSession(fromSession, null);
		getHibSession().flush();
	}


	@Override
	public void rollDepartmentsForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDeptsFowardFrom());
		try {
			iCopyBetweenSessionHelper.copyMergeDepartmentsToSession(fromSession, null);
		} catch (Exception e) {
			iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardDepartments()), e);
			errors.addFieldError("rollForward", e.getMessage());
		}
	}
	
	public void rollDatePatternsForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDatePatternsForwardFrom());
		iCopyBetweenSessionHelper.copyMergeDatePatternsToSession(fromSession, null);
		getHibSession().flush();
	}

	@Override
	public void rollSubjectAreasForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollSubjectAreasForwardFrom());
		iCopyBetweenSessionHelper.copyMergeSubjectAreasToSession(fromSession, null);
		getHibSession().flush();
	}

	@Override
	public void rollInstructorDataForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollInstructorDataForwardFrom());
		iCopyBetweenSessionHelper.copyMergeInstructorDataToSession(fromSession, null, rollForwardSessionForm.getRollForwardDepartmentIds());
		getHibSession().flush();
	}
	
	@Override
	public void rollCourseOfferingsForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		iCopyBetweenSessionHelper.copyMergeCourseOfferingsToSession(errors);
		rollForwardParentOfferingsIfNeeded(errors, rollForwardSessionForm);
	}
	
	@Override
	public void rollTimePatternsForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollTimePatternsForwardFrom());
		iCopyBetweenSessionHelper.copyMergeTimePatternsToSession(fromSession, null);
		getHibSession().flush();
	}
		
	@Override
	public void rollSessionConfigurationForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
        Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
        Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollSessionConfigForwardFrom());
        
    	iCopyBetweenSessionHelper.copyMergeConfigurationToSession(fromSession, null);
                
        getHibSession().flush();
        
        ApplicationProperties.clearSessionProperties(toSession.getUniqueId());
	}

	@Override
	public void rollLearningManagementSystemInfoForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDatePatternsForwardFrom());
		iCopyBetweenSessionHelper.copyMergeLearningManagementSystemInfoToSession(fromSession);
		getHibSession().flush();		
	}
	
	@Override
	public void rollClassInstructorsForward(RollForwardErrorLogger errors, RollForwardSessionInterface rollForwardSessionForm) {
		for (Long subjectId: rollForwardSessionForm.getRollForwardClassInstrSubjectIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(subjectId);
				iLog.info("Rolling " + subjectArea.getLabel() + " class instructors forward...");
				iCopyBetweenSessionHelper.copyMergeClassInstructorsForASubjectArea(subjectArea.getSubjectAreaAbbreviation());
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardClassInstructors()), e);
				errors.addFieldError("rollForward", e.getMessage());
				break;
			}
			getHibSession().clear();
		}
	}
}
