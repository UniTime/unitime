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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.CancelledClassAction;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.DistributionMode;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollAction;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;


public class AcademicSessionMerge {
	
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private Session iMergedSession; 
	private Session iPrimarySession;
	private Session iSecondarySession;
	
	public AcademicSessionMerge(Long mergedSessionId, 
			Long primarySessionId, 
			Long secondarySessionId, 
			boolean useCampusPrefixForDepartments, 
			boolean useCampusPrefixForSubjectAreas,
			String prefixSeparator,
			String primarySessionDefaultPrefix,
			String secondarySessionDefaultPrefix,			
			HashMap<String, String> departmentCodesWithDifferentPrefix,
			String classPrefsAction,
			String subpartLocationPrefsAction,
			String subpartTimePrefsAction,
			boolean mergeWaitListsProhibitedOverrides,
			DistributionMode distributionPrefMode, 
			CancelledClassAction cancelledClassAction,
			org.hibernate.Session hibSession,
			Log log
			) {
		
		if (hibSession.getTransaction() != null && hibSession.getTransaction().isActive()) {
			hibSession.getTransaction().commit();
		}
		CopyBetweenSessionHelper copyBetweenSessionHelper 
		         = new CopyBetweenSessionHelper(mergedSessionId, primarySessionId, secondarySessionId, 
		        		 useCampusPrefixForDepartments, useCampusPrefixForSubjectAreas, prefixSeparator, 
		        		 primarySessionDefaultPrefix, secondarySessionDefaultPrefix, departmentCodesWithDifferentPrefix, 
		        		 classPrefsAction, subpartLocationPrefsAction, subpartTimePrefsAction, 
		        		 mergeWaitListsProhibitedOverrides, distributionPrefMode, cancelledClassAction, hibSession, log);
		try {
			// Pull the departments from both sessions together into set of departments
			log.info("Pull the departments from both sessions together into set of departments");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Departments: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeDepartmentsToSession(iPrimarySession, primarySessionDefaultPrefix);
			
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Departments: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeDepartmentsToSession(iSecondarySession, secondarySessionDefaultPrefix);
			
			//Use the session roll forward to roll use the session configuration from the primary session
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Use the session roll forward to roll use the session configuration from the primary session");
			copyBetweenSessionHelper.copyMergeConfigurationToSession(iPrimarySession, primarySessionDefaultPrefix);

			// Pull the timetable managers from both sessions together
			log.info("Pull the timetable managers from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Managers: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeTimetableManagersToSession(iPrimarySession, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Managers: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeTimetableManagersToSession(iSecondarySession, secondarySessionDefaultPrefix);
	
			// Pull the room features from both sessions together
			log.info("Pull the room features from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Room Features: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeRoomFeaturesToSession(iPrimarySession, primarySessionDefaultPrefix);

 			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
 			copyBetweenSessionHelper.copyMergeRoomFeaturesToSession(iSecondarySession, secondarySessionDefaultPrefix);
			
			// Pull the room groups from both sessions together
			log.info("Pull the room groups from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Room Groups: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeRoomGroupsToSession(iPrimarySession, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Room Groups: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeRoomGroupsToSession(iSecondarySession, secondarySessionDefaultPrefix);
			
			
			// Pull the buildings from both sessions together
			log.info("Pull the buildings from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Buildings: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeBuildingsToSession(iPrimarySession);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Buildings: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeBuildingsToSession(iSecondarySession);

			// Pull the locations from both sessions together
			log.info("Pull the locations from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Locations: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeLocationsToSession(iPrimarySession, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Locations: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeLocationsToSession(iSecondarySession, secondarySessionDefaultPrefix);

			
			// Pull the travel times from both sessions together
			log.info("Pull the travel times from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Travel Times: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeTravelTimesToSession(iPrimarySession);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Travel Times: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeTravelTimesToSession(iSecondarySession);

			// Pull the date patterns from the primary session into the session
			log.info("Pull the date patterns from the primary session into the session");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Date Patterns: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeDatePatternsToSession(iPrimarySession, primarySessionDefaultPrefix);

			// Pull the time patterns from the primary session into the session
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Pull the time patterns from the primary session into the session");
			copyBetweenSessionHelper.copyMergeTimePatternsToSession(iPrimarySession, primarySessionDefaultPrefix);

			// Pull the learning management system info from both sessions together
			log.info("Pull the learning management system info from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Learning Management System Info: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeLearningManagementSystemInfoToSession(iPrimarySession);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Learning Management System Info: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeLearningManagementSystemInfoToSession(iSecondarySession);

			// Pull the subjectAreas from both sessions together
			log.info("Pull the subjectAreas from both sessions together");

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Subject Areas: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeSubjectAreasToSession(iPrimarySession, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Subject Areas: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeSubjectAreasToSession(iSecondarySession, secondarySessionDefaultPrefix);
		
			// Pull the departmental instructors from both sessions together
			log.info("Pull the departmental instructors from both sessions together");
			
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Primary Session Departmental Instructors: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeInstructorDataToSession(iPrimarySession, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Departmental Instructors: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeInstructorDataToSession(iSecondarySession, secondarySessionDefaultPrefix);
			
			// Pull the courses from both sessions together
			log.info("Pull the courses from both sessions together");
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);

			log.info("Copying Primary Session Course Offerings: " + iPrimarySession.getLabel());
			copyBetweenSessionHelper.copyMergeCourseOfferingsToSession(iPrimarySession, RollAction.fromLegacy(subpartTimePrefsAction), 
					RollAction.fromLegacy(subpartLocationPrefsAction),
					RollAction.fromLegacy(subpartTimePrefsAction), mergeWaitListsProhibitedOverrides, distributionPrefMode, 
					cancelledClassAction, primarySessionDefaultPrefix);

			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Secondary Session Course Offerings: " + iSecondarySession.getLabel());
			copyBetweenSessionHelper.copyMergeCourseOfferingsToSession(iSecondarySession, RollAction.fromLegacy(classPrefsAction), 
					RollAction.fromLegacy(subpartLocationPrefsAction),
					RollAction.fromLegacy(subpartTimePrefsAction), mergeWaitListsProhibitedOverrides, distributionPrefMode, 
					cancelledClassAction, secondarySessionDefaultPrefix);

			// Pull the instructors onto their classes
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);
			log.info("Copying Session Instructors onto Classes");
			copyBetweenSessionHelper.copyMergeClassInstructorsToSession();
			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);

//			log.info("Copying Secondary Session Instructors onto Classes: " + iSecondarySession.getLabel());
//			copyBetweenSessionHelper.copyMergeClassInstructorsToSession(iSecondarySession);
//			resetHibSession(hibSession, mergedSessionId, primarySessionId, secondarySessionId);

		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}

	}
	
	private void resetHibSession(org.hibernate.Session hibSession, 
			                     Long mergedSessionId, 
			                     Long primarySessionId, 
			                     Long secondarySessionId) {
		org.hibernate.Session hs = null;
		Long msi = null;
		Long psi = null;
		Long ssi = null;
		
		if (hibSession != null) {
			hs = hibSession;
		} else {
			hs = SessionDAO.getInstance().getSession();
		}
		if (mergedSessionId != null) {
			msi = mergedSessionId;
		} else if (iMergedSession != null) {
			msi = iMergedSession.getUniqueId();
		} 
		if (primarySessionId != null) {
			psi = primarySessionId;
		} else if (iPrimarySession != null) {
			psi = iPrimarySession.getUniqueId();
		} 
		if (secondarySessionId != null) {
			ssi = secondarySessionId;
		} else if (iSecondarySession != null) {
			ssi = iSecondarySession.getUniqueId();
		}
		
		if (msi != null && psi != null && ssi != null) {
			if (hs.getTransaction() != null && hs.getTransaction().isActive()) {
				hs.getTransaction().commit();
			}
			hs = SessionDAO.getInstance().getSession();
			hs.flush();
			hs.clear();
			hs.close();
			iMergedSession = Session.getSessionById(msi);
			iPrimarySession = Session.getSessionById(psi);
			iSecondarySession = Session.getSessionById(ssi);	
		}
	}
	
	
	
}
