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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.hibernate.Transaction;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ArrangeCreditUnitConfig;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCatalog;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseSubpartCredit;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.OverrideType;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StandardEventNoteDepartment;
import org.unitime.timetable.model.StandardEventNoteSession;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.VariableRangeCreditUnitConfig;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseCatalogDAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.DistributionPrefDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.model.dao.LearningManagementSystemInfoDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationPictureDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.RoomPictureDAO;
import org.unitime.timetable.model.dao.SchedulingSubpartDAO;
import org.unitime.timetable.model.dao.SessionConfigDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.model.dao.TravelTimeDAO;
import org.unitime.timetable.util.SessionRollForward.CancelledClassAction;
import org.unitime.timetable.util.SessionRollForward.DistributionMode;

public class AcademicSessionMerge {
	
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private SessionRollForward iSessionRollForward;
	private Log iLog;
	private RoomFeatureType iCampusRoomFeatureType;
	private HashMap<String, GlobalRoomFeature> iPrefixRoomFeatureMap = new HashMap<String, GlobalRoomFeature>();
	private HashMap<Object, Set<Location>> iRoomList;
	private Session iMergedSession; 
	private boolean iUseCampusPrefixForDepartments; 
	private boolean iUseCampusPrefixForSubjectAreas;
	private String iPrefixSeparator;
	private HashMap<String, String> iDepartmentCodesWithDifferentPrefix;
	private boolean iResetClassSuffix;
	
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
		iMergedSession = Session.getSessionById(mergedSessionId);
		Session primarySession = Session.getSessionById(primarySessionId);
		Session secondarySession = Session.getSessionById(secondarySessionId);
		iUseCampusPrefixForDepartments = useCampusPrefixForDepartments;
		iUseCampusPrefixForSubjectAreas = useCampusPrefixForSubjectAreas;
		iPrefixSeparator = prefixSeparator;
		iDepartmentCodesWithDifferentPrefix = departmentCodesWithDifferentPrefix;
		iLog = log;
		iResetClassSuffix = ApplicationProperty.RollForwardResetClassSuffix.isTrue();
		iSessionRollForward = new SessionRollForward(log);
		try {
			// Pull the departments from both sessions together into set of departments
			log.info("Pull the departments from both sessions together into set of departments");
			log.info("Copying Primary Session Departments: " + primarySession.getLabel());
			copyMergeDepartmentsToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Departments: " + secondarySession.getLabel());
			copyMergeDepartmentsToSession(secondarySession, secondarySessionDefaultPrefix);
			
			//Use the session roll forward to roll use the session configuration from the primary session
			log.info("Use the session roll forward to roll use the session configuration from the primary session");
			copyMergeConfigurationToSession(primarySession, primarySessionDefaultPrefix);

			// Pull the timetable managers from both sessions together
			log.info("Pull the timetable managers from both sessions together");
			log.info("Copying Primary Session Managers: " + primarySession.getLabel());
			copyMergeTimetableManagersToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Managers: " + secondarySession.getLabel());
			copyMergeTimetableManagersToSession(secondarySession, secondarySessionDefaultPrefix);
	
			// Pull the room features from both sessions together
			log.info("Pull the room features from both sessions together");
			log.info("Copying Primary Session Room Features: " + primarySession.getLabel());
			copyMergeRoomFeaturesToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Room Features: " + secondarySession.getLabel());
			copyMergeRoomFeaturesToSession(secondarySession, secondarySessionDefaultPrefix);
			
			// Pull the room groups from both sessions together
			log.info("Pull the room groups from both sessions together");
			log.info("Copying Primary Session Room Groups: " + primarySession.getLabel());
			copyMergeRoomGroupsToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Room Groups: " + secondarySession.getLabel());
			copyMergeRoomGroupsToSession(secondarySession, secondarySessionDefaultPrefix);
			
			
			// Pull the buildings from both sessions together
			log.info("Pull the buildings from both sessions together");
			log.info("Copying Primary Session Buildings: " + primarySession.getLabel());
			copyMergeBuildingsToSession(primarySession);
			log.info("Copying Secondary Session Buildings: " + secondarySession.getLabel());
			copyMergeBuildingsToSession(secondarySession);

			// Pull the locations from both sessions together
			log.info("Pull the locations from both sessions together");
			log.info("Copying Primary Session Locations: " + primarySession.getLabel());
			copyMergeLocationsToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Locations: " + secondarySession.getLabel());
			copyMergeLocationsToSession(secondarySession, secondarySessionDefaultPrefix);

			
			// Pull the travel times from both sessions together
			log.info("Pull the travel times from both sessions together");
			log.info("Copying Primary Session Travel Times: " + primarySession.getLabel());
			copyMergeTravelTimesToSession(primarySession);
			log.info("Copying Secondary Session Travel Times: " + secondarySession.getLabel());
			copyMergeTravelTimesToSession(secondarySession);

			// Pull the date patterns from the primary session into the session
			log.info("Pull the date patterns from the primary session into the session");
			log.info("Copying Primary Session Date Patterns: " + primarySession.getLabel());
			copyMergeDatePatternsToSession(primarySession, primarySessionDefaultPrefix);

			// Pull the time patterns from the primary session into the session
			log.info("Pull the time patterns from the primary session into the session");
			copyMergeTimePatternsToSession(primarySession, primarySessionDefaultPrefix);

			// Pull the learning management system info from both sessions together
			log.info("Pull the learning management system info from both sessions together");
			log.info("Copying Primary Session Learning Management System Info: " + primarySession.getLabel());
			copyMergeLearningManagementSystemInfoToSession(primarySession);
			log.info("Copying Secondary Session Learning Management System Info: " + secondarySession.getLabel());
			copyMergeLearningManagementSystemInfoToSession(secondarySession);

			// Pull the subjectAreas from both sessions together
			log.info("Pull the subjectAreas from both sessions together");
			log.info("Copying Primary Session Subject Areas: " + primarySession.getLabel());
			copyMergeSubjectAreasToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Subject Areas: " + secondarySession.getLabel());
			copyMergeSubjectAreasToSession(secondarySession, secondarySessionDefaultPrefix);
		
			// Pull the departmental instructors from both sessions together
			log.info("Pull the departmental instructors from both sessions together");
			log.info("Copying Primary Session Departmental Instructors: " + primarySession.getLabel());
			copyMergeInstructorDataToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Departmental Instructors: " + secondarySession.getLabel());
			copyMergeInstructorDataToSession(secondarySession, secondarySessionDefaultPrefix);
			
			// Pull the courses from both sessions together
			log.info("Pull the courses from both sessions together");
			log.info("Copying Primary Session Course Offerings: " + primarySession.getLabel());
			copyMergeCourseOfferingsToSession(primarySession, classPrefsAction, subpartLocationPrefsAction,
					subpartTimePrefsAction, mergeWaitListsProhibitedOverrides, distributionPrefMode, 
					cancelledClassAction, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Course Offerings: " + secondarySession.getLabel());
			copyMergeCourseOfferingsToSession(secondarySession, classPrefsAction, subpartLocationPrefsAction,
					subpartTimePrefsAction, mergeWaitListsProhibitedOverrides, distributionPrefMode, 
					cancelledClassAction, secondarySessionDefaultPrefix);

			// Pull the instructors onto their classes for each session
			log.info("Copying Primary Session Instructors onto Classes: " + primarySession.getLabel());
			copyMergeClassInstructorsToSession(primarySession, primarySessionDefaultPrefix);
			log.info("Copying Secondary Session Instructors onto Classes: " + secondarySession.getLabel());
			copyMergeClassInstructorsToSession(secondarySession, secondarySessionDefaultPrefix);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	private void addPrefixToDeptFields(Department department, String suffix) {
		department.setDeptCode(suffix + iPrefixSeparator + department.getDeptCode());							
		department.setAbbreviation(suffix + iPrefixSeparator + department.getAbbreviation());
		department.setName(suffix + iPrefixSeparator + department.getName());
		if (department.isExternalManager()) {
			department.setExternalMgrAbbv(suffix + iPrefixSeparator + department.getExternalMgrAbbv());
			department.setExternalMgrLabel(suffix + iPrefixSeparator + department.getExternalMgrLabel());
		}	
	}
	
	private String findPrefix(String deptCode, String defaultPrefix) {
		String prefix = null;
		if (defaultPrefix != null && iPrefixSeparator != null) {
			prefix = defaultPrefix;
			if (iDepartmentCodesWithDifferentPrefix != null && iDepartmentCodesWithDifferentPrefix.containsKey(deptCode)) {
				prefix = iDepartmentCodesWithDifferentPrefix.get(deptCode);
			}
		}

		return prefix;
	}
	
	
	private void mergeGlobalInstructorAttributesToSession(Session fromSession) {
		Map<Long, InstructorAttribute> attributes = new HashMap<Long, InstructorAttribute>();
		for (InstructorAttribute oldAttribute: InstructorAttribute.getAllGlobalAttributes(iMergedSession.getUniqueId())) {
			InstructorAttributeDAO.getInstance().getSession().remove(oldAttribute);
		}
		List<InstructorAttribute> globalAttributes = InstructorAttribute.getAllGlobalAttributes(fromSession.getUniqueId());
		for (InstructorAttribute fromAttribute: globalAttributes) {
			InstructorAttribute toAttribute = new InstructorAttribute();
			toAttribute.setSession(iMergedSession);
			toAttribute.setCode(fromAttribute.getCode());
			toAttribute.setName(fromAttribute.getName());
			toAttribute.setType(fromAttribute.getType());
			toAttribute.setInstructors(new HashSet<DepartmentalInstructor>());
			toAttribute.setChildAttributes(new HashSet<InstructorAttribute>());
			attributes.put(fromAttribute.getUniqueId(), toAttribute);
			InstructorAttributeDAO.getInstance().getSession().persist(toAttribute);
		}
		for (InstructorAttribute fromChildAttribute: globalAttributes) {
			if (fromChildAttribute.getParentAttribute() != null) {
				InstructorAttribute toChildAttribute = attributes.get(fromChildAttribute.getUniqueId());
				InstructorAttribute toParentAttribute = attributes.get(fromChildAttribute.getParentAttribute().getUniqueId());
				if (toParentAttribute != null) {
					toChildAttribute.setParentAttribute(toParentAttribute);
					toParentAttribute.getChildAttributes().add(toChildAttribute);
					InstructorAttributeDAO.getInstance().getSession().merge(toChildAttribute);
				}
			}
		}
	}

	
	public void copyMergeConfigurationToSession(Session fromSession, String defaultPrefix) {
        org.hibernate.Session hibSession = SessionConfigDAO.getInstance().getSession();
        // remove old configuration
        for (SessionConfig config: hibSession.createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId", SessionConfig.class
        		).setParameter("sessionId", iMergedSession.getUniqueId()).list()) {
        	hibSession.remove(config);
        }
        
        // create new configuration
        for (SessionConfig config: hibSession.createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId", SessionConfig.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	
        	SessionConfig newConfig = new SessionConfig();
        	newConfig.setKey(config.getKey());
        	newConfig.setDescription(config.getDescription());
        	newConfig.setValue(config.getValue());
        	newConfig.setSession(iMergedSession);
        	
        	hibSession.persist(newConfig);
        }
        
        // remove old notes
        for (StandardEventNoteSession note: hibSession.createQuery(
        		"from StandardEventNoteSession where session.uniqueId = :sessionId", StandardEventNoteSession.class
        		).setParameter("sessionId", iMergedSession.getUniqueId()).list()) {
        	hibSession.remove(note);
        }
        
        for (StandardEventNoteDepartment note: hibSession.createQuery(
        		"from StandardEventNoteDepartment where department.session.uniqueId = :sessionId", StandardEventNoteDepartment.class
        		).setParameter("sessionId", iMergedSession.getUniqueId()).list()) {
        	hibSession.remove(note);
        }
        
        // create new notes
        for (StandardEventNoteSession note: hibSession.createQuery(
        		"from StandardEventNoteSession where session.uniqueId = :sessionId", StandardEventNoteSession.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	StandardEventNoteSession newNote = new StandardEventNoteSession();
        	newNote.setNote(note.getNote());
        	newNote.setReference(note.getReference());
        	newNote.setSession(iMergedSession);
        	hibSession.persist(newNote);
        }
        
        for (StandardEventNoteDepartment note: hibSession.createQuery(
        		"from StandardEventNoteDepartment where department.session.uniqueId = :sessionId", StandardEventNoteDepartment.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	Department newDepartment = findToDepartment(note.getDepartment(), defaultPrefix);
        	if (newDepartment != null) {
            	StandardEventNoteDepartment newNote = new StandardEventNoteDepartment();
            	newNote.setNote(note.getNote());
            	newNote.setReference(note.getReference());
            	newNote.setDepartment(newDepartment);
            	hibSession.persist(newNote);
        	}
        }
        
        // remove room type options
        for (RoomTypeOption option: hibSession.createQuery(
        		"from RoomTypeOption where department.session.uniqueId = :sessionId", RoomTypeOption.class
        		).setParameter("sessionId", iMergedSession.getUniqueId()).list()) {
        	hibSession.remove(option);
        }
        
        // create new room type options
        for (RoomTypeOption option: hibSession.createQuery(
        		"from RoomTypeOption where department.session.uniqueId = :sessionId", RoomTypeOption.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	Department newDepartment = findToDepartment(option.getDepartment(), defaultPrefix);
        	if (newDepartment != null) {
        		RoomTypeOption newOption = new RoomTypeOption();
        		newOption.setBreakTime(option.getBreakTime());
        		newOption.setDepartment(newDepartment);
        		newOption.setMessage(option.getMessage());
        		newOption.setRoomType(option.getRoomType());
        		newOption.setStatus(RoomTypeOption.getDefaultStatus());
        		newOption.setEventEmail(option.getEventEmail());
        		hibSession.persist(newOption);
        	}
        }
        
        // remove old service providers
        for (EventServiceProvider provider: hibSession.createQuery(
        		"from EventServiceProvider where session.uniqueId = :sessionId", EventServiceProvider.class
        		).setParameter("sessionId", iMergedSession.getUniqueId()).list()) {
        	hibSession.remove(provider);
        }
        
        // create new service providers
        for (EventServiceProvider provider: hibSession.createQuery(
        		"from EventServiceProvider where session.uniqueId = :sessionId", EventServiceProvider.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	if (!provider.isVisible()) continue; // do not roll-forward providers that are marked as not visible
        	EventServiceProvider newProvider = new EventServiceProvider();
        	newProvider.setReference(provider.getReference());
        	newProvider.setLabel(provider.getLabel());
        	newProvider.setEmail(provider.getEmail());
        	newProvider.setNote(provider.getNote());
        	newProvider.setAllRooms(provider.getAllRooms());
        	newProvider.setVisible(provider.getVisible());
        	newProvider.setSession(iMergedSession);
        	if (provider.getDepartment() != null) {
        		Department newDepartment = findToDepartment(provider.getDepartment(), defaultPrefix);        				//provider.getDepartment().findSameDepartmentInSession(toSession);
        		if (newDepartment == null) continue;
        		newProvider.setDepartment(newDepartment);
        	}
        	hibSession.persist(newProvider);
        }
        
        // merge global instructor attributes to session
        mergeGlobalInstructorAttributesToSession(fromSession);
               
        ApplicationProperties.clearSessionProperties(iMergedSession.getUniqueId());
        hibSession.flush();
	}

	
	public void copyMergeDepartmentsToSession(Session fromSession, String defaultPrefix) throws Exception {
		Department toDepartment = null;
		DepartmentDAO dDao = DepartmentDAO.getInstance();
		SolverGroup sg = null;
		String prefix = null;


		List<Department> departments = DepartmentDAO.getInstance().findBySession(dDao.getSession(), fromSession.getUniqueId());
		dDao.getSession().refresh(iMergedSession);
		for(Department fromDepartment : departments){
			if (fromDepartment != null){
				if (!iUseCampusPrefixForDepartments) {
					toDepartment = Department.findByDeptCode(fromDepartment.getDeptCode(), iMergedSession.getUniqueId(), dDao.getSession());
				} else {
					prefix = findPrefix(fromDepartment.getDeptCode(), defaultPrefix);
				}
				if (toDepartment == null) {
					toDepartment = (Department) fromDepartment.clone();
					toDepartment.setStatusType(null);
					if (iUseCampusPrefixForDepartments && prefix != null) {
						addPrefixToDeptFields(toDepartment, prefix);
					}
					toDepartment.setSession(iMergedSession);
					iMergedSession.addToDepartments(toDepartment);
					dDao.getSession().persist(toDepartment);
				}
				if(fromDepartment.getSolverGroup() != null && toDepartment.getSolverGroup() == null) {
					sg = findToSolverGroup(fromDepartment.getSolverGroup(), fromDepartment, defaultPrefix);
					if (sg == null){
						sg = (SolverGroup)fromDepartment.getSolverGroup().clone();
						if (prefix != null) {
							sg.setAbbv(prefix + iPrefixSeparator + sg.getAbbv());
							sg.setName(prefix + iPrefixSeparator + sg.getName());
						}
						sg.setSession(iMergedSession);
					}
					if (sg != null){
						if (null == sg.getDepartments()){
							sg.setDepartments(new java.util.HashSet<Department>());
						}
						sg.getDepartments().add(toDepartment);
						toDepartment.setSolverGroup(sg);
						SolverGroupDAO sgDao = SolverGroupDAO.getInstance();
						if (sg.getUniqueId() == null)
							sgDao.getSession().persist(sg);
						else
							sgDao.getSession().merge(sg);
					}
				}
				
				dDao.getSession().merge(toDepartment);
				DistributionTypeDAO dtDao = DistributionTypeDAO.getInstance();
				@SuppressWarnings("unchecked")
				List<DistributionType> l = dtDao.getSession().createQuery("select dt from DistributionType dt inner join dt.departments as d where d.uniqueId = " + fromDepartment.getUniqueId().toString(), DistributionType.class).list();
				if (l != null && !l.isEmpty()){
					for (DistributionType distributionType : l){
							distributionType.getDepartments().add(toDepartment);
						dtDao.getSession().merge(distributionType);
					}
				}
			}
			dDao.getSession().flush();
			dDao.getSession().evict(toDepartment);
			dDao.getSession().evict(fromDepartment);

		}
		dDao.getSession().flush();
		dDao.getSession().clear();
		
	}

    private Department findToDepartment(Department fromDepartment, String defaultPrefix) {
		String prefix = null;
    	Department toDepartment = null;
		if (!iUseCampusPrefixForDepartments) {
			toDepartment = fromDepartment.findSameDepartmentInSession(iMergedSession);
		} else {
			prefix = findPrefix(fromDepartment.getDeptCode(), defaultPrefix);
			if (prefix != null) {
				toDepartment = Department.findByDeptCode(prefix + iPrefixSeparator + fromDepartment.getDeptCode(), iMergedSession.getUniqueId(), DepartmentDAO.getInstance().getSession());
			}
		}
		return toDepartment;
    }
    
    private SolverGroup findToSolverGroup(SolverGroup fromSolverGroup, Department fromDepartment, String defaultPrefix) {
		String prefix = null;
		if (iUseCampusPrefixForDepartments) {
			prefix = findPrefix(fromDepartment.getDeptCode(), defaultPrefix);
		}
		return SolverGroup.findBySessionIdAbbv(iMergedSession.getUniqueId(), (prefix == null ? fromSolverGroup.getAbbv() : prefix + iPrefixSeparator + fromSolverGroup.getAbbv()));
    }
	
	public void copyMergeTimetableManagersToSession(Session fromSession, String defaultPrefix) {

		Department toDepartment = null;
		TimetableManagerDAO tmDao = TimetableManagerDAO.getInstance();
		SolverGroupDAO sgDao = SolverGroupDAO.getInstance();
		try {
			List<Department> departments = DepartmentDAO.getInstance().findBySession(tmDao.getSession(), fromSession.getUniqueId());
			for(Department fromDepartment : departments){
				tmDao.getSession().refresh(fromDepartment);
				if (fromDepartment != null && fromDepartment.getTimetableManagers() != null){
					toDepartment = findToDepartment(fromDepartment, defaultPrefix);

					if (toDepartment != null){
						if (toDepartment.getTimetableManagers() == null){
							toDepartment.setTimetableManagers(new java.util.HashSet<TimetableManager>());
						}
						for (TimetableManager tm : fromDepartment.getTimetableManagers()){
							if (tm != null){
								tmDao.getSession().refresh(tm);
								toDepartment.getTimetableManagers().add(tm);
								tm.getDepartments().add(toDepartment);
								tmDao.getSession().merge(tm);
								if (tm.getSolverGroups(iMergedSession).isEmpty()){
									for(Iterator sgIt = tm.getSolverGroups(fromSession).iterator(); sgIt.hasNext();){
										SolverGroup fromSg = (SolverGroup) sgIt.next();
										SolverGroup toSg = findToSolverGroup(fromSg, fromDepartment, defaultPrefix);
										if (toSg != null && !tm.getSolverGroups().contains(toSg)){
											toSg.addToTimetableManagers(tm);
											tm.addToSolverGroups(toSg);
											sgDao.getSession().merge(toSg);
										}
									}
									tmDao.getSession().merge(tm);
								}
							}
						}
					}
				}
				tmDao.getSession().flush();
				tmDao.getSession().clear();			
			}
		} catch (Exception e) {
			//TODO: handle errors
			iLog.error("Failed to merge all timetable managers to session.", e);
		}
	
	}
	
	private DepartmentRoomFeature findToDeptRoomFeature(DepartmentRoomFeature fromDeptRoomFeature, String defaultPrefix) {
		Department toDepartment = findToDepartment(fromDeptRoomFeature.getDepartment(), defaultPrefix);
		String query = "from DepartmentRoomFeature rf where rf.department.uniqueId = :deptId and rf.label = :label";
		return DepartmentRoomFeatureDAO.getInstance().getSession().createQuery(query, DepartmentRoomFeature.class)
				.setParameter("deptId", toDepartment.getUniqueId()).setParameter("label", fromDeptRoomFeature.getLabel()).uniqueResult();	
	}
	
	private RoomGroup findToDeptRoomGroup(RoomGroup fromDeptRoomGroup, String defaultPrefix) {
		Department toDepartment = findToDepartment(fromDeptRoomGroup.getDepartment(), defaultPrefix);
		String query = "from RoomGroup rg where rg.global = false and rg.department.uniqueId = :deptId and rg.name = :name";
		return DepartmentRoomFeatureDAO.getInstance().getSession().createQuery(query, RoomGroup.class)
				.setParameter("deptId", toDepartment.getUniqueId()).setParameter("name", fromDeptRoomGroup.getName()).uniqueResult();	
	}

	
	private RoomFeatureType getCampusRoomFeatureType() {
		if (iCampusRoomFeatureType == null) {
			RoomFeatureTypeDAO rftDao = RoomFeatureTypeDAO.getInstance();
			iCampusRoomFeatureType =  rftDao.getSession().createQuery("from RoomFeatureType rft where rft.reference = 'campus'", RoomFeatureType.class).uniqueResult();
			if (iCampusRoomFeatureType == null) {
				iCampusRoomFeatureType = new RoomFeatureType();
				iCampusRoomFeatureType.setReference("campus");
				iCampusRoomFeatureType.setLabel(MESSAGES.labelCampus());
				iCampusRoomFeatureType.setShowInEventManagement(true);
				rftDao.getSession().persist(iCampusRoomFeatureType);
			}
		}
		return iCampusRoomFeatureType;
	}
	
	private GlobalRoomFeature getCampusRoomFeature(String campusPrefix) {
		if (campusPrefix == null) {
			return null;
		}
		GlobalRoomFeature rf = iPrefixRoomFeatureMap.get(campusPrefix);
		if (rf == null) {
			rf = GlobalRoomFeature.findGlobalRoomFeatureForAbbv(iMergedSession, campusPrefix);
			if (rf == null) {
				rf = new GlobalRoomFeature();
				rf.setAbbv(campusPrefix);
				rf.setLabel(campusPrefix);
				rf.setFeatureType(getCampusRoomFeatureType());
				rf.setSession(iMergedSession);
				GlobalRoomFeatureDAO.getInstance().getSession().persist(rf);
				iPrefixRoomFeatureMap.put(campusPrefix, rf);
			}
		}
		return rf;
	}
	
	public void copyMergeRoomFeaturesToSession(Session fromSession, String defaultPrefix) {

		DepartmentRoomFeature fromRoomFeature = null;
		DepartmentRoomFeature toRoomFeature = null;
		RoomFeatureDAO rfDao = RoomFeatureDAO.getInstance();
		Collection fromRoomFeatures = DepartmentRoomFeature.getAllRoomFeaturesForSession(fromSession);
		try{
			if (fromRoomFeatures != null && !fromRoomFeatures.isEmpty()){
				for(Iterator it = fromRoomFeatures.iterator(); it.hasNext();){
					fromRoomFeature = (DepartmentRoomFeature) it.next();
					
					if (fromRoomFeature != null){
						toRoomFeature = findToDeptRoomFeature(fromRoomFeature, defaultPrefix);
						if (toRoomFeature == null) {
							toRoomFeature = (DepartmentRoomFeature)fromRoomFeature.clone();
							toRoomFeature.setDepartment(findToDepartment(fromRoomFeature.getDepartment(), defaultPrefix));
							rfDao.getSession().persist(toRoomFeature);
						}
					}
				}
				rfDao.getSession().flush();
			}
			for (GlobalRoomFeature fromRoomFeatureGlobal: GlobalRoomFeature.getAllGlobalRoomFeatures(fromSession)) {
				GlobalRoomFeature toRoomFeatureGlobal = GlobalRoomFeature.findGlobalRoomFeatureForLabel(iMergedSession, fromRoomFeatureGlobal.getLabel());
				if (toRoomFeatureGlobal == null) {
					toRoomFeatureGlobal = (GlobalRoomFeature)fromRoomFeatureGlobal.clone();
					toRoomFeatureGlobal.setSession(iMergedSession);
					rfDao.getSession().persist(toRoomFeatureGlobal);
				}
			}
			rfDao.getSession().flush();
		} catch (Exception e) {
			iLog.error("Failed to merge all room features to session.", e);
		}	
	
	}

	public void copyMergeRoomGroupsToSession(Session fromSession, String defaultPrefix) {

		RoomGroup toRoomGroup = null;
		RoomGroupDAO rgDao = RoomGroupDAO.getInstance();
		Collection<RoomGroup> fromRoomGroups = RoomGroup.getAllRoomGroupsForSession(fromSession);
		try {
			if (fromRoomGroups != null && !fromRoomGroups.isEmpty()){
				for (RoomGroup fromRoomGroup : fromRoomGroups){
					if (fromRoomGroup != null){
						if (fromRoomGroup.isGlobal()) {
							toRoomGroup = RoomGroup.findGlobalRoomGroupForName(iMergedSession, fromRoomGroup.getName());
						} else {
						    toRoomGroup = findToDeptRoomGroup(fromRoomGroup, defaultPrefix);
						}
						if (toRoomGroup == null) {
							toRoomGroup = (RoomGroup) fromRoomGroup.clone();
							toRoomGroup.setSession(iMergedSession);
							if (fromRoomGroup.getDepartment() != null)
								toRoomGroup.setDepartment(findToDepartment(fromRoomGroup.getDepartment(), defaultPrefix));
							rgDao.getSession().persist(toRoomGroup);
						}	
					}
				}
				rgDao.getSession().flush();

			}
		} catch (Exception e) {
			//TODO: handle errors
			iLog.error("Failed to merge all room groups to session.", e);
		}
	
	}
	
	
	public void copyMergeBuildingsToSession(Session fromSession){

		BuildingDAO bDao = BuildingDAO.getInstance();
		List<Building> fromBuildings = bDao.findBySession(bDao.getSession(), fromSession.getUniqueId());
		bDao.getSession().refresh(iMergedSession);
		if (fromBuildings != null && !fromBuildings.isEmpty()){
			try{
				ExternalBuilding toExternalBuilding = null;
				for (Building fromBldg : fromBuildings){
					Building toBldg = null;
					if (fromBldg.getExternalUniqueId() != null) {
						toBldg = Building.findByExternalIdAndSession(fromBldg.getExternalUniqueId(), iMergedSession);
					}
					if (toBldg == null) {
						toBldg = Building.findByBldgAbbv(fromBldg.getAbbreviation(), iMergedSession.getUniqueId());
					} else {
						continue;
					}
					if (toBldg == null) {
						if (fromBldg.getExternalUniqueId() != null && iSessionRollForward.sessionHasExternalBuildingList(iMergedSession)){
							toExternalBuilding = ExternalBuilding.findExternalBuildingForSession(fromBldg.getExternalUniqueId(), iMergedSession);
							if (toExternalBuilding != null){
								toBldg = new Building();
								toBldg.setAbbreviation(toExternalBuilding.getAbbreviation());
								toBldg.setCoordinateX(toExternalBuilding.getCoordinateX());
								toBldg.setCoordinateY(toExternalBuilding.getCoordinateY());
								toBldg.setExternalUniqueId(toExternalBuilding.getExternalUniqueId());
								toBldg.setName(toExternalBuilding.getDisplayName());
							} else {
								continue;
							}
						} else {
							toBldg = (Building) fromBldg.clone();
						}
						toBldg.setSession(iMergedSession);
						iMergedSession.addToBuildings(toBldg);
						bDao.getSession().persist(toBldg);
					} 
					bDao.getSession().flush();
					bDao.getSession().evict(toBldg); // -- Previously commented out to prevent NonUniqueObjectException
					bDao.getSession().evict(fromBldg);	
				}
			} catch (Exception e) {
				//TODO: handle errors
				iLog.error("Failed to merge all buildings to session.", e);
			}
		}
		
	}
	
	private void copyMergeRoomFeaturesForLocation(Location fromLocation, Location toLocation, HashMap<RoomFeature, RoomFeature> roomFeatureCache, String defaultPrefix) {

		if(fromLocation.getFeatures() != null && !fromLocation.getFeatures().isEmpty()){
			GlobalRoomFeature toGlobalFeature = null;
			RoomFeature toFeature = null;
			boolean mergeGlobalFeaturesFromFromLocationToSession = true;
			if (toLocation instanceof Room) {
				Room toRoom = (Room) toLocation;
				if (toRoom.getExternalUniqueId() != null){
					ExternalRoom er = ExternalRoom.findExternalRoomForSession(toRoom.getExternalUniqueId(), iMergedSession);
					if (er != null){
						mergeGlobalFeaturesFromFromLocationToSession = false;
						if (er.getRoomFeatures() != null){
							for (ExternalRoomFeature erf : er.getRoomFeatures()){
								toGlobalFeature = GlobalRoomFeature.findGlobalRoomFeatureForLabel(iMergedSession, erf.getValue());
								toLocation.addTofeatures(toGlobalFeature);
							}
						}
					}
				}
			}
			for(RoomFeature fromFeature : fromLocation.getFeatures()){
				if (fromFeature instanceof GlobalRoomFeature && !mergeGlobalFeaturesFromFromLocationToSession) continue;
				toFeature = (RoomFeature) roomFeatureCache.get(fromFeature);
				if (toFeature == null){
					toFeature = fromFeature.findSameFeatureInSession(iMergedSession);
					if (toFeature != null){
						roomFeatureCache.put(fromFeature, toFeature);
						toLocation.addTofeatures(toFeature);
						if (toFeature.getRooms() == null){
							toFeature.setRooms(new java.util.HashSet<Location>());
						}
						toFeature.getRooms().add(toLocation);
					}
				}
			}
		}
		if (defaultPrefix != null) {
			for (RoomDept rd : fromLocation.getRoomDepts()) {
				String prefix = findPrefix(rd.getDepartment().getDeptCode(), defaultPrefix);
				if (prefix != null) {
					toLocation.addTofeatures(getCampusRoomFeature(prefix));
				}
			}
		}	
	}
	
	private void copyMergeRoomGroupsForLocation(Location fromLocation, Location toLocation, HashMap<RoomGroup, RoomGroup> roomGroupCache) {
		if(fromLocation.getRoomGroups() != null && !fromLocation.getRoomGroups().isEmpty()){
			RoomGroup toRoomGroup = null;
			for(RoomGroup fromRoomGroup : fromLocation.getRoomGroups()){
				toRoomGroup = (RoomGroup) roomGroupCache.get(fromRoomGroup);
				if (toRoomGroup == null)
					toRoomGroup = fromRoomGroup.findSameRoomGroupInSession(iMergedSession);
				if (toRoomGroup != null) {
					roomGroupCache.put(fromRoomGroup, toRoomGroup);
					if (toLocation.getRoomGroups() == null)
						toLocation.setRoomGroups(new java.util.HashSet<RoomGroup>());
					toLocation.getRoomGroups().add(toRoomGroup);
					if (toRoomGroup.getRooms() == null)
						toRoomGroup.setRooms(new java.util.HashSet<Location>());
					toRoomGroup.getRooms().add(toLocation);
				}
			}
		}
		
	}
	
	public void copyMergeLocationsToSession(Session fromSession, String defaultPrefix){
		
		LocationDAO lDao = LocationDAO.getInstance();
		List<Location> fromLocations = lDao.findBySession(lDao.getSession(), fromSession.getUniqueId());

		if (fromLocations != null && !fromLocations.isEmpty()){
			for (Location location : fromLocations){
				if (location instanceof Room) {
					copyMergeRoomToSession(location, defaultPrefix);
				} else if (location instanceof NonUniversityLocation){
					copyMergeNonUniversityLocationToSession(location, defaultPrefix);
				}
				LocationDAO.getInstance().getSession().flush();
			}
		}
		if (iSessionRollForward.sessionHasExternalRoomList(iMergedSession)){
			Room.addNewExternalRoomsToSession(iMergedSession);
		}
	
	}
	
	private Room findRoomInMergeSession(Room fromRoom) {
		Room toRoom = null;
		try {
			toRoom = fromRoom.findSameRoomInSession(iMergedSession);
		} catch (Exception e1) {
			// treat as room not found leave value null
		}
		if (toRoom == null) {
			Building b = null;
			try {
				b = fromRoom.getBuilding().findSameBuildingInSession(iMergedSession);
			} catch (Exception e) {
				// treat as building not found leave value null
			}
			if (b != null) {
				toRoom = Room.findByBldgIdRoomNbr(b.getUniqueId(), fromRoom.getRoomNumber(), iMergedSession.getUniqueId());
			}
		}
		return toRoom;
		
	}
	
	private NonUniversityLocation findNonUniversityLocationInMergeSession(NonUniversityLocation fromNonUniversityLocation) {
		NonUniversityLocation toNonUniversityLocation = null;
		try {
			toNonUniversityLocation = fromNonUniversityLocation.findSameNonUniversityLocationInSession(iMergedSession);
		} catch (Exception e1) {
			// treat as room not found leave value null
		}
		if (toNonUniversityLocation == null) {
			Location location = NonUniversityLocation.findByName(NonUniversityLocationDAO.getInstance().getSession(), iMergedSession.getUniqueId(), fromNonUniversityLocation.getName());
			if (location instanceof NonUniversityLocation) {
				toNonUniversityLocation = (NonUniversityLocation) location;
			}
		}
		return toNonUniversityLocation;		
	}
	
	private void copyMergeRoomDept(RoomDept fromRoomDept, Location toLocation, Location fromLocation, String defaultPrefix){		
		Department toDept = findToDepartment(fromRoomDept.getDepartment(), defaultPrefix);
		RoomDept toRoomDept = null;
		RoomDeptDAO rdDao = RoomDeptDAO.getInstance();
		if (toDept != null){
			toRoomDept = new RoomDept();
			toRoomDept.setRoom(toLocation);
			toRoomDept.setControl(fromRoomDept.isControl());
			toRoomDept.setDepartment(toDept);
			toLocation.addToRoomDepts(toRoomDept);
			toDept.addToRoomDepts(toRoomDept);
			rdDao.getSession().persist(toRoomDept);
			PreferenceLevel fromRoomPrefLevel = fromLocation.getRoomPreferenceLevel(fromRoomDept.getDepartment());
			if (!fromRoomPrefLevel.getPrefProlog().equals(PreferenceLevel.sNeutral)){
				RoomPref toRoomPref = new RoomPref();
				toRoomPref.setOwner(toDept);
				toRoomPref.setPrefLevel(fromRoomPrefLevel);
				toRoomPref.setRoom(toLocation);
				toDept.addToPreferences(toRoomPref);
				rdDao.getSession().merge(toDept);
			}
		}
	}

	public void copyMergeRoomToSession(Location location, String defaultPrefix) {

		Room fromRoom = null;
		Room toRoom = null;
		RoomDAO rDao = RoomDAO.getInstance();
		DepartmentDAO dDao = DepartmentDAO.getInstance();
		Building toBuilding = null;
		Department toDept = null;
		Department fromDept = null;
		HashMap<RoomFeature, RoomFeature> roomFeatureCache = new HashMap<RoomFeature, RoomFeature>();
		HashMap<RoomGroup, RoomGroup> roomGroupCache = new HashMap<RoomGroup, RoomGroup>();

		try {
			fromRoom = (Room) location;		
			toRoom = findRoomInMergeSession(fromRoom);
			
			if (toRoom == null) {
				if (fromRoom.getExternalUniqueId() != null && iSessionRollForward.sessionHasExternalRoomList(iMergedSession)){
					ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), iMergedSession);
					if (toExternalRoom != null) {
						toRoom = new Room();
						toRoom.setCapacity(toExternalRoom.getCapacity());
						toRoom.setExamCapacity(toExternalRoom.getExamCapacity());
						toRoom.setClassification(toExternalRoom.getClassification());
						toRoom.setCoordinateX(toExternalRoom.getCoordinateX());
						toRoom.setCoordinateY(toExternalRoom.getCoordinateY());
						toRoom.setArea(toExternalRoom.getArea());
						toRoom.setDisplayName(toExternalRoom.getDisplayName());
						toRoom.setExternalUniqueId(toExternalRoom.getExternalUniqueId());
						toRoom.setIgnoreRoomCheck(fromRoom.isIgnoreRoomCheck());
						toRoom.setIgnoreTooFar(fromRoom.isIgnoreTooFar());
						toRoom.setPattern(fromRoom.getPattern());
						toRoom.setRoomNumber(toExternalRoom.getRoomNumber());
						toRoom.setRoomType(toExternalRoom.getRoomType());
						toRoom.setExamTypes(new HashSet<ExamType>(fromRoom.getExamTypes()));
						toRoom.setEventStatus(null);
						toRoom.setBreakTime(fromRoom.getBreakTime());
						toRoom.setNote(fromRoom.getNote());
						toRoom.setEventEmail(fromRoom.getEventEmail());
						toRoom.setEventAvailability(fromRoom.getEventAvailability());
						LocationPermIdGenerator.setPermanentId(toRoom);
					} else {
						return;
					}
				} else {
					toRoom = (Room)fromRoom.clone();
				}
				toRoom.setSession(iMergedSession);
				if (fromRoom.getEventDepartment() != null) {
					toRoom.setEventDepartment(findToDepartment(fromRoom.getEventDepartment(), defaultPrefix));			
				}
				toBuilding = Building.findByBldgAbbv(rDao.getSession(), iMergedSession.getUniqueId(), fromRoom.getBuilding().getAbbreviation());
				if (toBuilding != null) {
					toRoom.setBuilding(toBuilding);
					if (fromRoom.getManagerIds() != null && fromRoom.getManagerIds().length() != 0){
						String toManagerStr = "";
						for (StringTokenizer stk = new StringTokenizer(fromRoom.getManagerIds(),",");stk.hasMoreTokens();) {
							Long fromDeptId = Long.valueOf(stk.nextToken());
							if (fromDeptId != null){
								fromDept = dDao.get(fromDeptId);
								if (fromDept != null){
									toDept = findToDepartment(fromDept, defaultPrefix);
									if (toDept != null){
										if (toManagerStr.length() != 0){
											toManagerStr += ",";
										}
										toManagerStr += toDept.getUniqueId().toString();
									}
								}
							}
						}
						toRoom.setManagerIds(toManagerStr);
					} else {
						toRoom.setPattern(null);
					}
					copyMergeRoomFeaturesForLocation(fromRoom, toRoom, roomFeatureCache, defaultPrefix);
					copyMergeRoomGroupsForLocation(fromRoom, toRoom, roomGroupCache);
					rDao.getSession().persist(toRoom);
				}
				boolean mergeExistingRoomDepts = true;
				if (fromRoom.getExternalUniqueId() != null && iSessionRollForward.sessionHasExternalRoomDeptList(iMergedSession)){
					ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), iMergedSession);
					if (toExternalRoom.getRoomDepartments() != null && !toExternalRoom.getRoomDepartments().isEmpty()){
						for(ExternalRoomDepartment toExternalRoomDept : toExternalRoom.getRoomDepartments()){
							boolean foundDept = false;
							for(RoomDept fromRoomDept : fromRoom.getRoomDepts()){
								if (fromRoomDept.getDepartment().getDeptCode().equals(toExternalRoomDept.getDepartmentCode())){
									foundDept = true;
									break;
								}
							}
							if (!foundDept){
								mergeExistingRoomDepts = false;
							}
						}
					}
				} 
				if (mergeExistingRoomDepts){
					if (fromRoom.getRoomDepts() != null && !fromRoom.getRoomDepts().isEmpty()){
						for (RoomDept fromRoomDept : fromRoom.getRoomDepts()){
							copyMergeRoomDept(fromRoomDept, toRoom, fromRoom, defaultPrefix);
						}
					}
				} else {
					// resetting department sharing related fields
					toRoom.setPattern(null);
					toRoom.setManagerIds(null);
					ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), iMergedSession);
					for(ExternalRoomDepartment toExternalRoomDept : toExternalRoom.getRoomDepartments()){
						boolean foundDept = false;
						RoomDept foundFromRoomDept = null;
						for(RoomDept fromRoomDept :  fromRoom.getRoomDepts()){
							if (fromRoomDept.getDepartment().getDeptCode().equals(toExternalRoomDept.getDepartmentCode())){
								foundDept = true;
								foundFromRoomDept = fromRoomDept;
								break;
							}
						}
						if (foundDept){
							copyMergeRoomDept(foundFromRoomDept, toRoom, fromRoom, defaultPrefix);
						} else {
							toRoom.addExternalRoomDept(toExternalRoomDept, toExternalRoom.getRoomDepartments());
						}
					}
				}
				rDao.getSession().merge(toRoom);
				
				RoomPictureDAO rpDao = RoomPictureDAO.getInstance();
				for (RoomPicture fromPicture: fromRoom.getPictures()) {
					RoomPicture toPicture = fromPicture.clonePicture();
					toPicture.setLocation(toRoom);
					toRoom.addToPictures(toPicture);
					rpDao.getSession().persist(toPicture);
				}
				
				for (EventServiceProvider fromProvider: fromRoom.getAllowedServices()) {
					EventServiceProvider toProvider = fromProvider.findInSession(iMergedSession.getUniqueId());
					if (toProvider != null)
						toRoom.addToAllowedServices(toProvider);
				}
				rDao.getSession().merge(toRoom);
			} else {
				if (fromRoom.getRoomDepts() != null && !fromRoom.getRoomDepts().isEmpty()){
					for (RoomDept fromRoomDept : fromRoom.getRoomDepts()){
						if (!toRoom.hasRoomDept(findToDepartment(fromRoomDept.getDepartment(), defaultPrefix))) {
							copyMergeRoomDept(fromRoomDept, toRoom, fromRoom, defaultPrefix);
						}
					}
					rDao.getSession().merge(toRoom);
				}			
				copyMergeRoomFeaturesForLocation(fromRoom, toRoom, roomFeatureCache, defaultPrefix);
				copyMergeRoomGroupsForLocation(fromRoom, toRoom, roomGroupCache);
				rDao.getSession().merge(toRoom);
			}
			rDao.getSession().flush();
			rDao.getSession().evict(toRoom); // --  Previously commented out to prevent NonUniqueObjectException
			rDao.getSession().evict(fromRoom);
		} catch (Exception e) {
			//TODO: log errors
			iLog.error("Failed to merge all rooms to session.", e);
		}
	
	}
	
	private void copyMergeNonUniversityLocationToSession(Location location, String defaultPrefix) {
		NonUniversityLocation fromNonUniversityLocation = null;
		NonUniversityLocation toNonUniversityLocation = null;
		NonUniversityLocationDAO nulDao = NonUniversityLocationDAO.getInstance();
		DepartmentDAO dDao = DepartmentDAO.getInstance();
		Department toDept = null;
		Department fromDept = null;
		HashMap<RoomFeature, RoomFeature> roomFeatureCache = new HashMap<RoomFeature, RoomFeature>();
		HashMap<RoomGroup, RoomGroup> roomGroupCache = new HashMap<RoomGroup, RoomGroup>();

		try {
			fromNonUniversityLocation = (NonUniversityLocation) location;	
			toNonUniversityLocation = findNonUniversityLocationInMergeSession(fromNonUniversityLocation);
			
			if (toNonUniversityLocation == null) {
				toNonUniversityLocation = (NonUniversityLocation)fromNonUniversityLocation.clone();
				toNonUniversityLocation.setSession(iMergedSession);
				if (fromNonUniversityLocation.getEventDepartment() != null) {
					toNonUniversityLocation.setEventDepartment(findToDepartment(fromNonUniversityLocation.getEventDepartment(), defaultPrefix));
				}
				if (fromNonUniversityLocation.getManagerIds() != null && fromNonUniversityLocation.getManagerIds().length() != 0){
					String toManagerStr = "";
					for (StringTokenizer stk = new StringTokenizer(fromNonUniversityLocation.getManagerIds(),",");stk.hasMoreTokens();) {
						Long fromDeptId = Long.valueOf(stk.nextToken());
						if (fromDeptId != null){
							fromDept = dDao.get(fromDeptId);
							if (fromDept != null){
								toDept = findToDepartment(fromDept, defaultPrefix);
								if (toDept != null){
									if (toManagerStr.length() != 0){
										toManagerStr += ",";
									}
									toManagerStr += toDept.getUniqueId().toString();
								}
							}
						}
					}
					toNonUniversityLocation.setManagerIds(toManagerStr);
				} else {
					toNonUniversityLocation.setPattern(null);
				}
				copyMergeRoomFeaturesForLocation(fromNonUniversityLocation, toNonUniversityLocation, roomFeatureCache, defaultPrefix);
				copyMergeRoomGroupsForLocation(fromNonUniversityLocation, toNonUniversityLocation, roomGroupCache);
				nulDao.getSession().persist(toNonUniversityLocation);
				
				NonUniversityLocationPictureDAO nulpDao = NonUniversityLocationPictureDAO.getInstance();
				for (NonUniversityLocationPicture fromPicture: fromNonUniversityLocation.getPictures()) {
					NonUniversityLocationPicture toPicture = fromPicture.clonePicture();
					toPicture.setLocation(toNonUniversityLocation);
					toNonUniversityLocation.addToPictures(toPicture);
					nulpDao.getSession().persist(toPicture);
				}
				
				for (EventServiceProvider fromProvider: fromNonUniversityLocation.getAllowedServices()) {
					EventServiceProvider toProvider = fromProvider.findInSession(iMergedSession.getUniqueId());
					if (toProvider != null)
						toNonUniversityLocation.addToAllowedServices(toProvider);
				}
	
				if (fromNonUniversityLocation.getRoomDepts() != null && !fromNonUniversityLocation.getRoomDepts().isEmpty()){
					for (RoomDept fromRoomDept : fromNonUniversityLocation.getRoomDepts()){
						copyMergeRoomDept(fromRoomDept, toNonUniversityLocation, fromNonUniversityLocation, defaultPrefix);
					}
					nulDao.getSession().merge(toNonUniversityLocation);
				}	
			} else {
				if (fromNonUniversityLocation.getRoomDepts() != null && !fromNonUniversityLocation.getRoomDepts().isEmpty()){
					for (RoomDept fromRoomDept : fromNonUniversityLocation.getRoomDepts()){
						if (!toNonUniversityLocation.hasRoomDept(findToDepartment(fromRoomDept.getDepartment(), defaultPrefix))) {
							copyMergeRoomDept(fromRoomDept, toNonUniversityLocation, fromNonUniversityLocation, defaultPrefix);
						}
					}
					nulDao.getSession().merge(toNonUniversityLocation);
				}			
				
				copyMergeRoomFeaturesForLocation(fromNonUniversityLocation, toNonUniversityLocation, roomFeatureCache, defaultPrefix);
				copyMergeRoomGroupsForLocation(fromNonUniversityLocation, toNonUniversityLocation, roomGroupCache);
				nulDao.getSession().merge(toNonUniversityLocation);
			}
			nulDao.getSession().flush();
			nulDao.getSession().evict(toNonUniversityLocation);
			nulDao.getSession().evict(fromNonUniversityLocation);
		} catch (Exception e) {
			iLog.error("Failed to merge all non university locations to session.", e);
		}		
	}
	
	private Location findLocation(Long locationId, Long sessionId) {
		TravelTimeDAO dao = TravelTimeDAO.getInstance();
		
		Room room = dao.getSession().createQuery(
				"select r2 from Room r1, Room r2 where r1.uniqueId = :locationId and r2.building.session.uniqueId=:sessionId and " +
				"((r1.externalUniqueId is not null and r1.externalUniqueId = r2.externalUniqueId) or " +
				"(r1.externalUniqueId is null and r1.building.abbreviation = r2.building.abbreviation and r1.roomNumber = r2.roomNumber))", Room.class)
				.setParameter("sessionId", sessionId)
				.setParameter("locationId", locationId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
		
		if (room != null) return room;
				
		return dao.getSession().createQuery(
				"select r2 from NonUniversityLocation r1, NonUniversityLocation r2 where r1.uniqueId = :locationId and r2.session.uniqueId=:sessionId "
				+"and r1.name = r2.name", NonUniversityLocation.class)
				.setParameter("sessionId", sessionId)
				.setParameter("locationId", locationId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}


	private void copyMergeTravelTimesToSession(Session fromSession) {
		TravelTimeDAO dao = TravelTimeDAO.getInstance();
		for (TravelTime travel: dao.getSession().createQuery(
    			"from TravelTime where session.uniqueId = :sessionId", TravelTime.class)
    			.setParameter("sessionId", fromSession.getUniqueId()).list()) {
						
			Location from = findLocation(travel.getLocation1Id(), iMergedSession.getUniqueId());
			if (from == null) continue;
			Location to = findLocation(travel.getLocation2Id(), iMergedSession.getUniqueId());
			if (to == null) continue;
			
			TravelTime time = null;
			String query = "from TravelTime where session.uniqueId = :sessionId and location1Id = :loc1Id and location2Id = :loc2Id";
			time = dao.getSession().createQuery(query, TravelTime.class)
					.setParameter("sessionId", iMergedSession.getUniqueId()).setParameter("loc1Id", from.getUniqueId()).setParameter("loc2Id", to.getUniqueId()).uniqueResult();
			if (time == null) {
				time = new TravelTime();
				time.setSession(iMergedSession);
				time.setLocation1Id(Math.min(from.getUniqueId(), to.getUniqueId()));
				time.setLocation2Id(Math.max(from.getUniqueId(), to.getUniqueId()));
				time.setDistance(travel.getDistance());
				
				dao.getSession().persist(time);
			}
		}
		dao.getSession().flush();
	}
	
	private void mergeDatePatternOntoDepartmentsToSession(DatePattern fromDatePattern, DatePattern toDatePattern, String defaultPrefix){
		if (fromDatePattern.getDepartments() != null && !fromDatePattern.getDepartments().isEmpty()){
			for(Department fromDept : fromDatePattern.getDepartments()){
				Department toDepartment = findToDepartment(fromDept, defaultPrefix);
				if (toDepartment != null){
					if (null == toDepartment.getDatePatterns()){
						toDepartment.setDatePatterns(new java.util.HashSet<DatePattern>());
					}
					toDepartment.getDatePatterns().add(toDatePattern);
					if (null == toDatePattern.getDepartments()){
						toDatePattern.addToDepartments(toDepartment);
					}
					toDatePattern.addToDepartments(toDepartment);
				}
			}
		}		
	}

	public void copyMergeDatePatternsToSession(Session fromSession, String defaultPrefix) {
		List<DatePattern> fromDatePatterns = DatePattern.findAll(fromSession, true, null, null);
		DatePattern toDatePattern = null;
		DatePatternDAO dpDao = DatePatternDAO.getInstance();
		dpDao.getSession().refresh(fromSession);
		dpDao.getSession().refresh(iMergedSession);
		HashMap<DatePattern, DatePattern> fromToDatePatternMap = new HashMap<DatePattern, DatePattern>();
		try {
			for(DatePattern fromDatePattern : fromDatePatterns){
				if (fromDatePattern != null){
					toDatePattern = DatePattern.findByName(iMergedSession, fromDatePattern.getName());
					if (toDatePattern == null) {
						toDatePattern = (DatePattern) fromDatePattern.clone();
						toDatePattern.setSession(iMergedSession);
						mergeDatePatternOntoDepartmentsToSession(fromDatePattern, toDatePattern, defaultPrefix);
						dpDao.getSession().persist(toDatePattern);
						dpDao.getSession().flush();
					}
					fromToDatePatternMap.put(fromDatePattern, toDatePattern);
				}
			}
			
			for (DatePattern fromDp: fromToDatePatternMap.keySet()){
				DatePattern toDp = fromToDatePatternMap.get(fromDp);
				if (fromDp.getParents() != null && !fromDp.getParents().isEmpty()){
					for (DatePattern fromParent: fromDp.getParents()){
						toDp.addToParents(fromToDatePatternMap.get(fromParent));
					}
					dpDao.getSession().merge(toDp);
				}
			}
			
			if (fromSession.getDefaultDatePattern() != null){
				DatePattern defDp = DatePattern.findByName(iMergedSession, fromSession.getDefaultDatePattern().getName());
				if (defDp != null){
					iMergedSession.setDefaultDatePattern(defDp);
					SessionDAO sDao = SessionDAO.getInstance();
					sDao.getSession().merge(iMergedSession);
				}
			}
			dpDao.getSession().flush();
			dpDao.getSession().clear();
		} catch (Exception e) {
			iLog.error("Failed to merge all date patterns to session.", e);
		}		
	}

	private void mergeTimePatternOntoDepartmentsToSession(TimePattern fromTimePattern, TimePattern toTimePattern, String defaultPrefix){
		if (fromTimePattern.getDepartments() != null && !fromTimePattern.getDepartments().isEmpty()){
			for(Department fromDept : fromTimePattern.getDepartments()){
				Department toDepartment = findToDepartment(fromDept, defaultPrefix);
				if (toDepartment != null){
					if (null == toDepartment.getTimePatterns()){
						toDepartment.setTimePatterns(new java.util.HashSet<TimePattern>());
					}
					toDepartment.getTimePatterns().add(toTimePattern);
					if (null == toTimePattern.getDepartments()){
						toTimePattern.setDepartments(new java.util.HashSet<Department>());
					}
					toTimePattern.addToDepartments(toDepartment);
				}
			}
		}		
	}


	public void copyMergeTimePatternsToSession(Session fromSession, String defaultPrefix) {
		List<TimePattern> fromDatePatterns = TimePattern.findAll(fromSession, null);
		TimePattern toTimePattern = null;
		TimePatternDAO tpDao = TimePatternDAO.getInstance();
		try {
			for(TimePattern fromTimePattern : fromDatePatterns){
				if (fromTimePattern != null){
					toTimePattern = TimePattern.findByName(iMergedSession, fromTimePattern.getName());
					if (toTimePattern == null) {
						toTimePattern = (TimePattern) fromTimePattern.clone();
						toTimePattern.setSession(iMergedSession);
						mergeTimePatternOntoDepartmentsToSession(fromTimePattern, toTimePattern, defaultPrefix);
						tpDao.getSession().persist(toTimePattern);
						tpDao.getSession().flush();
					}
				}
			}
			tpDao.getSession().flush();
			tpDao.getSession().clear();
		} catch (Exception e) {
			iLog.error("Failed to merge all time patterns to session.", e);
		}		
	}

	public void copyMergeLearningManagementSystemInfoToSession(Session fromSession) {
		List<LearningManagementSystemInfo> fromLearningManagementSystems = LearningManagementSystemInfo.findAll(fromSession.getUniqueId());
		LearningManagementSystemInfo toLms = null;
		LearningManagementSystemInfoDAO lmsDao = LearningManagementSystemInfoDAO.getInstance();
		try {
			for(LearningManagementSystemInfo fromLms : fromLearningManagementSystems){
				if (fromLms != null){
					toLms = LearningManagementSystemInfo.findBySessionIdAndReference(iMergedSession.getUniqueId(), fromLms.getReference());
					if (toLms == null) {
						toLms = (LearningManagementSystemInfo) fromLms.clone();
						toLms.setSession(iMergedSession);
						lmsDao.getSession().persist(toLms);
						lmsDao.getSession().flush();
					}
				}
			}
			lmsDao.getSession().flush();
			
		} catch (Exception e) {
			iLog.error("Failed to merge all learning management system infos to session.", e);
		}		
	}
	
	private HashSet<String> findSubjectPrefixes(
			SubjectArea fromSubjectArea,
			String defaultPrefix) {
		HashSet<String> prefixes = new HashSet<String>();
		if (iUseCampusPrefixForSubjectAreas && iPrefixSeparator != null) {
			StringBuffer query = new StringBuffer();
			query.append("select distinct c.managingDept")
				 .append(" from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co")
				 .append(" where co.subjectArea.uniqueId = :subjId");
			for (Department managingDept : DepartmentDAO.getInstance().getSession().createQuery(query.toString(), Department.class)
					.setParameter("subjId", fromSubjectArea.getUniqueId()).list()) {
				String prefix = defaultPrefix;
				if (iDepartmentCodesWithDifferentPrefix != null && iDepartmentCodesWithDifferentPrefix.containsKey(managingDept.getDeptCode())) {
					prefix = iDepartmentCodesWithDifferentPrefix.get(managingDept.getDeptCode());
				}
				prefixes.add(prefix);
			}
			if (prefixes.size() == 0) {
				prefixes.add(defaultPrefix);
			}
			
		} else {
			prefixes.add(null);
		}
	

		return prefixes;
	}
	
    private SubjectArea findToSubjectArea(SubjectArea fromSubjectArea, String prefix) {
		SubjectArea toSubjectArea = null;
		if (!iUseCampusPrefixForSubjectAreas || prefix == null || prefix.isEmpty()) {
			toSubjectArea = fromSubjectArea.findSameSubjectAreaInSession(iMergedSession);
		} else {
			if (iPrefixSeparator != null) {
				toSubjectArea = SubjectArea.findByAbbv(iMergedSession.getUniqueId(), prefix + iPrefixSeparator + fromSubjectArea.getSubjectAreaAbbreviation());
			}
		}
		return toSubjectArea;
    }

	private void addPrefixToSubjectFields(SubjectArea subjectArea, String prefix) {
		if (prefix != null && !prefix.isEmpty()  && !iPrefixSeparator.isEmpty()) {
			subjectArea.setSubjectAreaAbbreviation(prefix + iPrefixSeparator + subjectArea.getSubjectAreaAbbreviation());	
			subjectArea.setTitle(prefix + iPrefixSeparator + subjectArea.getTitle());
		}	
	}


	public void copyMergeSubjectAreasToSession(Session fromSession, 
			String prefix) {
		SubjectArea toSubjectArea = null;
		Department toDepartment = null;
		SubjectAreaDAO sDao = SubjectAreaDAO.getInstance();
		sDao.getSession().refresh(iMergedSession);
		try {
			if (iSessionRollForward.sessionHasCourseCatalog(iMergedSession)) {
				SubjectArea fromSubjectArea = null;
				CourseCatalogDAO ccDao = CourseCatalogDAO.getInstance();
				List<Object[]> subjects = ccDao.getSession().createQuery(
						"select distinct cc.subject, cc.previousSubject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject != null",
						Object[].class)
					.setParameter("sessionId", iMergedSession.getUniqueId())
					.list();
				if (subjects != null){
					String toSubject = null;
					String fromSubject = null;
					for (Object[] subjectInfo :  subjects){
						if (subjectInfo != null && subjectInfo.length == 2){
							toSubject = (String) subjectInfo[0];
							fromSubject = (String) subjectInfo[1];							
							fromSubjectArea = SubjectArea.findByAbbv(fromSession.getUniqueId(), fromSubject);
							if (fromSubjectArea == null){
								continue;
							}
							HashSet<String> subjectAreaPrefixes = findSubjectPrefixes(fromSubjectArea, prefix);
							for (String subjectPrefix : subjectAreaPrefixes){
								toSubjectArea = findToSubjectArea(fromSubjectArea, subjectPrefix);
								if (toSubjectArea != null) {
									continue;
								}
								toSubjectArea = (SubjectArea)fromSubjectArea.clone();
								toSubjectArea.setDepartment(null);
								toSubjectArea.setSubjectAreaAbbreviation(toSubject);									
								if (subjectPrefix != null) {
									addPrefixToSubjectFields(toSubjectArea, subjectPrefix);
								}
								if (fromSubjectArea.getFundingDept() != null){
									Department toFundingDept = findToDepartment(fromSubjectArea.getFundingDept(), prefix);
									toSubjectArea.setFundingDept(toFundingDept);
								}
								toSubjectArea.setSession(iMergedSession);
								iMergedSession.addToSubjectAreas(toSubjectArea);
								if (fromSubjectArea.getDepartment() != null) {
									toDepartment = findToDepartment(fromSubjectArea.getDepartment(), prefix);
									if (toDepartment != null){
										toSubjectArea.setDepartment(toDepartment);
										toDepartment.addToSubjectAreas(toSubjectArea);
										sDao.getSession().persist(toSubjectArea);
										sDao.getSession().flush();
										sDao.getSession().evict(toSubjectArea);
										sDao.getSession().evict(fromSubjectArea);
									}
								}
								
							}
						}
					}
				}

				List<String> newSubjects = ccDao.getSession().createQuery("select distinct subject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject = null and cc.subject not in (select sa.subjectAreaAbbreviation from SubjectArea sa where sa.session.uniqueId=:sessionId)", String.class)
					.setParameter("sessionId", iMergedSession.getUniqueId())
					.list();
				toDepartment = Department.findByDeptCode("TEMP", iMergedSession.getUniqueId());
				if (toDepartment == null){
					toDepartment = new Department();
					toDepartment.setAbbreviation("TEMP");
					toDepartment.setAllowReqRoom(Boolean.valueOf(false));
					toDepartment.setAllowReqTime(Boolean.valueOf(false));
					toDepartment.setAllowReqDistribution(Boolean.valueOf(false));
					toDepartment.setDeptCode("TEMP");
					toDepartment.setExternalManager(Boolean.valueOf(false));
					toDepartment.setExternalUniqueId(null);
					toDepartment.setName("Temp Department For New Subjects");
					toDepartment.setSession(iMergedSession);
					toDepartment.setDistributionPrefPriority(Integer.valueOf(0));
					toDepartment.setInheritInstructorPreferences(true);
					toDepartment.setAllowEvents(false);
					toDepartment.setAllowStudentScheduling(true);
					iMergedSession.addToDepartments(toDepartment);
					DepartmentDAO.getInstance().getSession().persist(toDepartment);
				}
				for (String toSubject : newSubjects){
					if (toSubject != null){
						toSubjectArea = new SubjectArea();
						toSubjectArea.setDepartment(toDepartment);
						toSubjectArea.setTitle("New Subject");
						toSubjectArea.setSession(iMergedSession);
						toSubjectArea.setSubjectAreaAbbreviation(toSubject);
						toDepartment.addToSubjectAreas(toSubjectArea);
						iMergedSession.addToSubjectAreas(toSubjectArea);
						sDao.getSession().persist(toSubjectArea);
						sDao.getSession().flush();
						sDao.getSession().evict(toSubjectArea);
						sDao.getSession().evict(fromSubjectArea);
					}
				}
			} else {
				List<SubjectArea> fromSubjects = sDao.findBySession(sDao.getSession(), fromSession.getUniqueId());
				sDao.getSession().refresh(iMergedSession);
				if (fromSubjects != null && !fromSubjects.isEmpty()){
					for(SubjectArea fromSubjectArea : fromSubjects){
						if (fromSubjectArea != null){
							HashSet<String> subjectAreaPrefixes = findSubjectPrefixes(fromSubjectArea, prefix);
							for (String subjectPrefix : subjectAreaPrefixes){
								toSubjectArea = findToSubjectArea(fromSubjectArea, subjectPrefix);
								if (toSubjectArea != null) {
									continue;
								}
								toSubjectArea = (SubjectArea)fromSubjectArea.clone();
								if (fromSubjectArea.getFundingDept() != null){
									Department toFundingDept = findToDepartment(fromSubjectArea.getFundingDept(), prefix);
									toSubjectArea.setFundingDept(toFundingDept);
								}
								if (subjectPrefix != null) {
									addPrefixToSubjectFields(toSubjectArea, subjectPrefix);
								}
								toSubjectArea.setDepartment(null);
								toSubjectArea.setSession(iMergedSession);
								iMergedSession.addToSubjectAreas(toSubjectArea);
								if (fromSubjectArea.getDepartment() != null) {
									toDepartment = findToDepartment(fromSubjectArea.getDepartment(), prefix);
								if (toDepartment != null){
										toSubjectArea.setDepartment(toDepartment);
										toDepartment.addToSubjectAreas(toSubjectArea);
										sDao.getSession().persist(toSubjectArea);
										sDao.getSession().flush();
										sDao.getSession().evict(toSubjectArea);
										sDao.getSession().evict(fromSubjectArea);									
									}
								}
							}
						}
					}
				}
			}
			sDao.getSession().flush();
			sDao.getSession().clear();
		} catch (Exception e) {
			iLog.error("Failed to merge all subject areas to session.", e);
		}
	}
	
	public HashMap<Object, Set<Location>> getRoomList() {
		if (iRoomList == null){
			iRoomList = new HashMap<Object, Set<Location>>();
		}
		return iRoomList;
	}
	
	private Department findManagingDepartmentForPrefGroup(PreferenceGroup prefGroup){
		Department toDepartment = null;
		if (prefGroup instanceof DepartmentalInstructor) {
			DepartmentalInstructor toInstructor = (DepartmentalInstructor) prefGroup;
			toDepartment = toInstructor.getDepartment();
		} else if (prefGroup instanceof SchedulingSubpart) {
			SchedulingSubpart toSchedSubpart = (SchedulingSubpart) prefGroup;
			if (toSchedSubpart.getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering() != null){
				toDepartment = toSchedSubpart.getManagingDept();	
			} 	
		} else if (prefGroup instanceof Class_) {
			Class_ toClass_ = (Class_) prefGroup;
			toDepartment = toClass_.getManagingDept();
			if (toDepartment == null){
				toDepartment = toClass_.getSchedulingSubpart().getControllingDept();
			}
		}
		return(toDepartment);
	}
	
	private Department findToManagingDepartmentForPrefGroup(PreferenceGroup toPrefGroup, PreferenceGroup fromPrefGroup, String defaultPrefix){
		Department toDepartment = findManagingDepartmentForPrefGroup(toPrefGroup);
		if (toDepartment == null){
			Department fromDepartment = findManagingDepartmentForPrefGroup(fromPrefGroup);
			if (fromDepartment != null){
				toDepartment = findToDepartment(fromDepartment, defaultPrefix);
				return(toDepartment);
			}
		}
		
		return(toDepartment);
	}
	
	private static String buildRoomQueryForDepartment(Department dept, Session sess, String locType){
		StringBuffer sb = new StringBuffer();
		sb.append("select l from " + locType + " as l inner join l.roomDepts as rd where l.session.uniqueId = ");
		sb.append(sess.getUniqueId().toString());
		sb.append(" and rd.department.uniqueId = ");
		sb.append(dept.getUniqueId().toString());
		return(sb.toString());
	}
	
	private static Set<Location> buildRoomListForDepartment(Department department, Session session){
		TreeSet<Location> ts = new TreeSet<Location>();
		for (Room r: RoomDAO.getInstance().getSession().createQuery(buildRoomQueryForDepartment(department, session, "Room"), Room.class).list()) {
			for (RoomDept rd : r.getRoomDepts()){
				rd.getDepartment();
			}
			ts.add(r);
		}
		for (NonUniversityLocation l: NonUniversityLocationDAO.getInstance().getSession().createQuery(buildRoomQueryForDepartment(department, session, "NonUniversityLocation"), NonUniversityLocation.class).list()) {
			for (RoomDept rd : l.getRoomDepts()){
				rd.getDepartment();
			}
			ts.add(l);
		}
		return ts;
	}

	
	private Set<Location> getLocationsFor(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, String defaultPrefix){
		if (fromPrefGroup instanceof Exam) {
			Exam exam = (Exam)fromPrefGroup;
			if (!getRoomList().containsKey(exam.getExamType()))
				getRoomList().put(exam.getExamType(), Location.findAllExamLocations(iMergedSession.getUniqueId(), exam.getExamType()));
			return getRoomList().get(exam.getExamType());
		}
		Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, defaultPrefix);
		if (toDepartment == null){
			return(null);
		}
		if (!getRoomList().containsKey(toDepartment)){
			getRoomList().put(toDepartment, buildRoomListForDepartment(toDepartment, iMergedSession));
		} 
		return ((Set<Location>)getRoomList().get(toDepartment));
	}
	
	private void createToBuildingPref(BuildingPref fromBuildingPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Set<Location> locations, boolean isExamPref, boolean isClassMerge) throws Exception{
		if (fromPrefGroup instanceof Class_ && !isClassMerge) return;
		BuildingPref toBuildingPref = null;
		Building toBuilding = fromBuildingPref.getBuilding().findSameBuildingInSession(iMergedSession);
		if (toBuilding != null){
			boolean deptHasRoomInBuilding = false;
			if(!isExamPref){
				for (Location loc : locations) {
					if (loc instanceof Room) {
						Room r = (Room) loc;
						if (r.getBuilding() != null && r.getBuilding().getUniqueId().equals(toBuilding.getUniqueId())){
							deptHasRoomInBuilding = true;
							break;
						}
					}
				}
			}
			
			if (isExamPref || deptHasRoomInBuilding){
				toBuildingPref = new BuildingPref();
				toBuildingPref.setBuilding(toBuilding);
				toBuildingPref.setPrefLevel(fromBuildingPref.getPrefLevel());
				toBuildingPref.setDistanceFrom(fromBuildingPref.getDistanceFrom());
				toBuildingPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toBuildingPref);
			}
		}
	
	}
	
	protected void mergeBuildingPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, boolean isClassMerge, boolean isSubpartMerge, boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction, String defaultPrefix) throws Exception{
		Set<Location> locations = null;
		boolean isExamPref = false;
		if (fromPrefGroup instanceof Exam) {
			isExamPref = true;
		}
		if (fromPrefGroup.getBuildingPreferences() != null 
				&& !fromPrefGroup.getBuildingPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartMerge)){
			locations = getLocationsFor(fromPrefGroup, toPrefGroup, defaultPrefix);
			if (!isExamPref && locations == null){
				return;
			}
			for (Iterator it = fromPrefGroup.getBuildingPreferences().iterator(); it.hasNext(); ){
				createToBuildingPref((BuildingPref) it.next(), fromPrefGroup, toPrefGroup, locations, isExamPref, isClassMerge);
			}
		}		
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp && (toPrefGroup.getBuildingPreferences() == null || toPrefGroup.getBuildingPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (locations == null){
				locations = getLocationsFor(fromPrefGroup, toPrefGroup, defaultPrefix);
			}
			if (locations != null && locations.size() >0  && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, BuildingPref> prefMap = new HashMap<String, BuildingPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					if (c.getBuildingPreferences() != null && !c.getBuildingPreferences().isEmpty()){
						for (Iterator rfpIt = c.getBuildingPreferences().iterator(); rfpIt.hasNext();){
							BuildingPref rfp = (BuildingPref) rfpIt.next();
							key = rfp.getPrefLevel().getPrefName() + rfp.getBuilding().getUniqueId().toString();
							prefMap.put(key, rfp);
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						createToBuildingPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, locations, isExamPref, isClassMerge);
					}
				}
			}				
		}
	}
	
	
	private void mergeDepartmentalInstructorAttributesToSession(Department fromDepartment, Department toDepartment) {
		Map<Long, InstructorAttribute> attributes = new HashMap<Long, InstructorAttribute>();
		
		List<InstructorAttribute> departmentalAttributes = InstructorAttribute.getAllDepartmentalAttributes(fromDepartment.getUniqueId());
		HashMap<String, InstructorAttribute> existingToDepartmentalAttributes = new HashMap<String, InstructorAttribute>();
		for (InstructorAttribute ia : InstructorAttribute.getAllDepartmentalAttributes(toDepartment.getUniqueId())) {
			existingToDepartmentalAttributes.put(ia.getNameWithType(), ia);
		}

		for (InstructorAttribute fromAttribute: departmentalAttributes) {
			InstructorAttribute toAttribute = existingToDepartmentalAttributes.get(fromAttribute.getNameWithType());
			if (toAttribute == null) {
				toAttribute = new InstructorAttribute();
				toAttribute.setSession(toDepartment.getSession());
				toAttribute.setDepartment(toDepartment);
				toAttribute.setCode(fromAttribute.getCode());
				toAttribute.setName(fromAttribute.getName());
				toAttribute.setType(fromAttribute.getType());
				toAttribute.setInstructors(new HashSet<DepartmentalInstructor>());
				toAttribute.setChildAttributes(new HashSet<InstructorAttribute>());
				attributes.put(fromAttribute.getUniqueId(), toAttribute);
				InstructorAttributeDAO.getInstance().getSession().persist(toAttribute);
			}
			attributes.put(fromAttribute.getUniqueId(), toAttribute);
		}
		for (InstructorAttribute fromChildAttribute: departmentalAttributes) {
			if (fromChildAttribute.getParentAttribute() != null) {
				InstructorAttribute toChildAttribute = attributes.get(fromChildAttribute.getUniqueId());
				InstructorAttribute toParentAttribute = attributes.get(fromChildAttribute.getParentAttribute().getUniqueId());
				if (toParentAttribute != null) {
					toChildAttribute.setParentAttribute(toParentAttribute);
					toParentAttribute.getChildAttributes().add(toChildAttribute);
					InstructorAttributeDAO.getInstance().getSession().merge(toChildAttribute);
				}
			}
		}
	}
	
	public HashSet<Department> findToDepartmentsForInstructor(DepartmentalInstructor fromDepartmentalInstructor, String defaultPrefix){
		HashSet<Department> departments = new HashSet<Department>();
		
		Department d = null;
		d = findToDepartment(fromDepartmentalInstructor.getDepartment(), defaultPrefix);
		if (d != null) {
			departments.add(d);
		}
		for (ClassInstructor ci : fromDepartmentalInstructor.getClasses()) {
			if (ci.getClassInstructing().getManagingDept() != null && iDepartmentCodesWithDifferentPrefix.containsKey(ci.getClassInstructing().getManagingDept().getDeptCode())) {
				d= findToDepartment(ci.getClassInstructing().getManagingDept(), defaultPrefix);
				if (d != null) {
					departments.add(d);
				}
			}
		}
		
		return departments;
	}
	
	private void createToRoomPref(RoomPref fromRoomPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Set<Location> locations, boolean isClassMerge){
		if (fromPrefGroup instanceof Class_ && !isClassMerge) return;
		RoomPref toRoomPref = new RoomPref();
		if (fromRoomPref.getRoom() instanceof Room) {
			Room fromRoom = (Room) fromRoomPref.getRoom();
			Room toRoom = null;
			for (Location loc : locations){
				if (loc instanceof Room) {
					toRoom = (Room) loc;
					if (((toRoom.getBuilding().getExternalUniqueId() != null && fromRoom.getBuilding().getExternalUniqueId() != null
							&& toRoom.getBuilding().getExternalUniqueId().equals(fromRoom.getBuilding().getExternalUniqueId())) 
							|| ((toRoom.getBuilding().getExternalUniqueId() == null || fromRoom.getBuilding().getExternalUniqueId() == null)
									&& toRoom.getBuilding().getAbbreviation().equals(fromRoom.getBuilding().getAbbreviation())))
							&& toRoom.getRoomNumber().equals(fromRoom.getRoomNumber())){
						break;
					}								
				}
			}
			if (toRoom != null && ((toRoom.getBuilding().getExternalUniqueId() != null && fromRoom.getBuilding().getExternalUniqueId() != null
					&& toRoom.getBuilding().getExternalUniqueId().equals(fromRoom.getBuilding().getExternalUniqueId())) 
					|| ((toRoom.getBuilding().getExternalUniqueId() == null || fromRoom.getBuilding().getExternalUniqueId() == null)
							&& toRoom.getBuilding().getAbbreviation().equals(fromRoom.getBuilding().getAbbreviation())))
					&& toRoom.getRoomNumber().equals(fromRoom.getRoomNumber())){
				toRoomPref.setRoom(toRoom);
				toRoomPref.setPrefLevel(fromRoomPref.getPrefLevel());
				toRoomPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomPref);
			}	
		} else if (fromRoomPref.getRoom() instanceof NonUniversityLocation) {
			NonUniversityLocation fromNonUniversityLocation = (NonUniversityLocation) fromRoomPref.getRoom();
			NonUniversityLocation toNonUniversityLocation = null;
			for (Location loc : locations){
				if (loc instanceof NonUniversityLocation) {
					toNonUniversityLocation = (NonUniversityLocation) loc;
					if (toNonUniversityLocation.getName().equals(fromNonUniversityLocation.getName())){
						break;
					}								
				}
			}
			if (toNonUniversityLocation != null && toNonUniversityLocation.getName().equals(fromNonUniversityLocation.getName())){
				toRoomPref.setRoom(toNonUniversityLocation);
				toRoomPref.setPrefLevel(fromRoomPref.getPrefLevel());
				toRoomPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomPref);
			}	
		}				
	}

	
	protected void mergeRoomPrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, boolean isClassMerge, boolean isSubpartMerge, boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction, String defaultPrefix){
		Set<Location> locations = null;
		if (fromPrefGroup.getRoomPreferences() != null 
				&& !fromPrefGroup.getRoomPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartMerge)){
			locations = getLocationsFor(fromPrefGroup, toPrefGroup, defaultPrefix);
			if (locations != null && locations.size() >0 ){					
				for (Iterator it = fromPrefGroup.getRoomPreferences().iterator(); it.hasNext();){
					createToRoomPref((RoomPref) it.next(), fromPrefGroup, toPrefGroup, locations, isClassMerge);
				}
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp && (toPrefGroup.getRoomPreferences() == null || toPrefGroup.getRoomPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (locations == null){
				locations = getLocationsFor(fromPrefGroup, toPrefGroup, defaultPrefix);
			}
			if (locations != null && locations.size() >0  && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomPref> prefMap = new HashMap<String, RoomPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					if (c.getRoomPreferences() != null && !c.getRoomPreferences().isEmpty()){
						for (Iterator rfpIt = c.getRoomPreferences().iterator(); rfpIt.hasNext();){
							RoomPref rfp = (RoomPref) rfpIt.next();
							key = rfp.getPrefLevel().getPrefName() + rfp.getRoom().getUniqueId().toString();
							prefMap.put(key, rfp);
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						createToRoomPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, locations, isClassMerge);
					}
				}
			}				
		}
	}
	
	private void createToRoomFeaturePref(RoomFeaturePref fromRoomFeaturePref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, 
			boolean isClassMerge, String defaultPrefix){
		if (fromPrefGroup instanceof Class_ && !isClassMerge) return;
		RoomFeaturePref toRoomFeaturePref = new RoomFeaturePref();
		if (fromRoomFeaturePref.getRoomFeature() instanceof GlobalRoomFeature) {
			GlobalRoomFeature grf = GlobalRoomFeature.findGlobalRoomFeatureForLabel(iMergedSession, fromRoomFeaturePref.getRoomFeature().getLabel());
			if (grf != null) {
				toRoomFeaturePref.setRoomFeature(grf);
				toRoomFeaturePref.setPrefLevel(fromRoomFeaturePref.getPrefLevel());
				toRoomFeaturePref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomFeaturePref);
			}
		} else {
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, defaultPrefix);
			if (toDepartment == null){
				return;
			}
			Collection<DepartmentRoomFeature> l = DepartmentRoomFeature.getAllDepartmentRoomFeatures(toDepartment);
			DepartmentRoomFeature fromDepartmentRoomFeature = (DepartmentRoomFeature) fromRoomFeaturePref.getRoomFeature();
			if (l != null && l.size() > 0){
				DepartmentRoomFeature toDepartmentRoomFeature = null;
				for (Iterator<DepartmentRoomFeature> rfIt = l.iterator(); rfIt.hasNext();){
					toDepartmentRoomFeature = (DepartmentRoomFeature) rfIt.next();
					if (toDepartmentRoomFeature.getLabel().equals(fromDepartmentRoomFeature.getLabel())){
						break;
					}
				}
				if (toDepartmentRoomFeature.getLabel().equals(fromDepartmentRoomFeature.getLabel())){
					toRoomFeaturePref.setRoomFeature(toDepartmentRoomFeature);
					toRoomFeaturePref.setPrefLevel(fromRoomFeaturePref.getPrefLevel());
					toRoomFeaturePref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toRoomFeaturePref);
				}
			}
		}

	}
	

	protected void mergeRoomFeaturePrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup,
			boolean isClassMerge, boolean isSubpartMerge, boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction,
			String defaultPrefix
			){
		if (fromPrefGroup.getRoomFeaturePreferences() != null 
				&& !fromPrefGroup.getRoomFeaturePreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartMerge)){
			for (Iterator it = fromPrefGroup.getRoomFeaturePreferences().iterator(); it.hasNext(); ){
				createToRoomFeaturePref((RoomFeaturePref) it.next(), fromPrefGroup, toPrefGroup, isClassMerge, defaultPrefix);
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp && (toPrefGroup.getRoomFeaturePreferences() == null || toPrefGroup.getRoomFeaturePreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomFeaturePref> prefMap = new HashMap<String, RoomFeaturePref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					if (c.getRoomFeaturePreferences() != null && !c.getRoomFeaturePreferences().isEmpty()){
						for (Iterator rfpIt = c.getRoomFeaturePreferences().iterator(); rfpIt.hasNext();){
							RoomFeaturePref rfp = (RoomFeaturePref) rfpIt.next();
							key = rfp.getPrefLevel().getPrefName() + rfp.getRoomFeature().getUniqueId().toString();
							prefMap.put(key, rfp);
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						createToRoomFeaturePref(prefMap.get(pref), fromPrefGroup, toPrefGroup, isClassMerge, defaultPrefix);
					}
				}
			}				
		}
	}

	private void createToRoomGroupPref(RoomGroupPref fromRoomGroupPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup,
			boolean isClassMerge, String defaultPrefix){
		if (fromPrefGroup instanceof Class_ && !isClassMerge) return;
		RoomGroupPref toRoomGroupPref = new RoomGroupPref();
		RoomGroup toDefaultRoomGroup = RoomGroup.getGlobalDefaultRoomGroup(iMergedSession);
		if (fromRoomGroupPref.getRoomGroup().isDefaultGroup() && toDefaultRoomGroup != null){
			toRoomGroupPref.setRoomGroup(toDefaultRoomGroup);
			toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
			toRoomGroupPref.setOwner(toPrefGroup);
			toPrefGroup.addToPreferences(toRoomGroupPref);
		} else if (fromRoomGroupPref.getRoomGroup().isGlobal()) {
			RoomGroup toRoomGroup = RoomGroup.findGlobalRoomGroupForName(iMergedSession, fromRoomGroupPref.getRoomGroup().getName());
			if (toRoomGroup != null) {
				toRoomGroupPref.setRoomGroup(toRoomGroup);
				toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
				toRoomGroupPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomGroupPref);
			}
		} else {
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, defaultPrefix);
			if (toDepartment == null){
				return;
			}
			Collection<RoomGroup> l = RoomGroup.getAllDepartmentRoomGroups(toDepartment);
			if (l != null && l.size() > 0) {
				RoomGroup toRoomGroup = null;
				for (Iterator<RoomGroup> itRg = l.iterator(); itRg.hasNext();){
					toRoomGroup = (RoomGroup) itRg.next();
					if (toRoomGroup.getName().equals(fromRoomGroupPref.getRoomGroup().getName())){
						break;
					}
				}
				if (toRoomGroup.getName().equals(fromRoomGroupPref.getRoomGroup().getName())){
					toRoomGroupPref.setRoomGroup(toRoomGroup);
					toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
					toRoomGroupPref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toRoomGroupPref);
				}						
			}
		}
	}

	
	protected void mergeRoomGroupPrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup,
			boolean isClassMerge, boolean isSubpartMerge, boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction,
			String defaultPrefix
			){
		if (fromPrefGroup.getRoomGroupPreferences() != null 
				&& !fromPrefGroup.getRoomGroupPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartMerge)){
			for (Iterator it = fromPrefGroup.getRoomGroupPreferences().iterator(); it.hasNext();){
				createToRoomGroupPref((RoomGroupPref) it.next(), fromPrefGroup, toPrefGroup, isClassMerge, defaultPrefix);
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp && (toPrefGroup.getRoomGroupPreferences() == null || toPrefGroup.getRoomGroupPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomGroupPref> prefMap = new HashMap<String, RoomGroupPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					if (c.getRoomGroupPreferences() != null && !c.getRoomGroupPreferences().isEmpty()){
						for (Iterator rfpIt = c.getRoomGroupPreferences().iterator(); rfpIt.hasNext();){
							RoomGroupPref rfp = (RoomGroupPref) rfpIt.next();
							key = rfp.getPrefLevel().getPrefName() + rfp.getRoomGroup().getUniqueId().toString();
							prefMap.put(key, rfp);
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						createToRoomGroupPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, isClassMerge, defaultPrefix);
					}
				}
			}				
		}
	}

	protected void mergeTimePrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup,
			boolean isClassMerge, boolean isSubpartMerge, boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction
			){
		if (fromPrefGroup.getTimePreferences() != null 
				&& !fromPrefGroup.getTimePreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartMerge)){
			TimePref fromTimePref = null;
			TimePref toTimePref = null;
			for (Iterator it = fromPrefGroup.getTimePreferences().iterator(); it.hasNext();){
				fromTimePref = (TimePref) it.next();
				if (fromTimePref.getTimePattern() == null) {
					toTimePref = (TimePref)fromTimePref.clone();
				} else {
					toTimePref = TimePattern.getMatchingTimePreference(iMergedSession.getUniqueId(), fromTimePref);
					if (toTimePref == null){
						iLog.warn("To Time Pattern not found:  " + fromTimePref.getTimePattern().getName() + " for " + fromPrefGroup.htmlLabel());						
					}
				}
				if (toTimePref != null){
					toTimePref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toTimePref);
				}
			}
		}
		// If subpart time preferences are not to be merged, make sure any subpart time patterns are merged without their time preferences. 
		if (fromPrefGroup instanceof SchedulingSubpart && !isSubpartMerge){
			TimePref fromTimePref = null;
			TimePref toTimePref = null;
			for (Iterator it = fromPrefGroup.getTimePreferences().iterator(); it.hasNext();){
				fromTimePref = (TimePref) it.next();
				if (fromTimePref.getTimePattern() == null) {
					toTimePref = (TimePref)fromTimePref.clone();
				} else {
					toTimePref = TimePattern.getMatchingTimePreference(iMergedSession.getUniqueId(), fromTimePref);
					if (toTimePref == null){
						iLog.warn("To Time Pattern not found:  " + fromTimePref.getTimePattern().getName() + " for " + fromPrefGroup.htmlLabel());						
					}
				}
				if (toTimePref != null){
					toTimePref.setPreference(null);
					toTimePref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toTimePref);
				}
			}			
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if ((ss.getTimePreferences() == null || ss.getTimePreferences().isEmpty()) && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, TimePref> prefMap = new HashMap<String, TimePref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				HashSet<TimePattern> timePatterns = new HashSet<TimePattern>();
				String key;
				int clsCnt = 0;
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					if (c.getTimePreferences() != null && !c.getTimePreferences().isEmpty()){
						for (Iterator tpIt = c.getTimePreferences().iterator(); tpIt.hasNext();){
							TimePref tp = (TimePref) tpIt.next();
							key = tp.getPrefLevel().getPrefName() + tp.getTimePattern().getUniqueId().toString() + tp.getPreference();
							prefMap.put(key, tp);
							timePatterns.add(tp.getTimePattern());
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						TimePref fromTimePref = prefMap.get(pref);
						TimePref toTimePref = null;
						if (fromTimePref.getTimePattern() == null) {
							toTimePref = (TimePref)fromTimePref.clone();
						} else {
							if (fromTimePref.getTimePattern().getType().intValue() == (TimePattern.TimePatternType.ExactTime.ordinal())){
								continue;
							}
							toTimePref = TimePattern.getMatchingTimePreference(iMergedSession.getUniqueId(), fromTimePref);
							if (toTimePref == null){
								iLog.warn("To Time Pattern not found:  " + fromTimePref.getTimePattern().getName() + " for " + fromPrefGroup.htmlLabel());						
							}
						}
						if (toTimePref != null){
							toTimePref.setOwner(toPrefGroup);
							toPrefGroup.addToPreferences(toTimePref);
							if (toTimePref.getPreference().contains(""+PreferenceLevel.sCharLevelRequired) || toTimePref.getPreference().contains(""+PreferenceLevel.sCharLevelProhibited)){
								toTimePref.setPreference(null);
							}
						}
						timePatterns.remove(fromTimePref.getTimePattern());
					}
				}

				for(TimePattern fromTp : timePatterns){
					if (fromTp.getType().intValue() == (TimePattern.TimePatternType.ExactTime.ordinal())){
						continue;
					}			
					TimePattern toTp = TimePattern.getMatchingTimePattern(iMergedSession.getUniqueId(), fromTp);
					TimePref toTimePref = null;
					if (toTp != null){
						toTimePref = new TimePref();
						toTimePref.setOwner(toPrefGroup);
						toTimePref.setTimePattern(toTp);
						toTimePref.setPrefLevel(PreferenceLevel.getPreferenceLevel(""+PreferenceLevel.sCharLevelRequired));
						toPrefGroup.addToPreferences(toTimePref);
					} else {
						iLog.warn("To Time Pattern not found:  " + fromTp.getName() + " for " + fromPrefGroup.htmlLabel());						
					}
				}
			}
		}
	}

	protected void mergeInstructorDistributionPrefsToSession(DepartmentalInstructor fromInstructor, DepartmentalInstructor toInstructor){
		if (fromInstructor.getDistributionPreferences() != null && fromInstructor.getDistributionPreferences().size() > 0){
			DistributionPref fromDistributionPref = null;
			DistributionPref toDistributionPref = null;
			for (Iterator it = fromInstructor.getDistributionPreferences().iterator(); it.hasNext();){
				fromDistributionPref = (DistributionPref) it.next();
				toDistributionPref = new DistributionPref();
				if(fromDistributionPref.getDistributionType() != null) {
					toDistributionPref.setDistributionType(fromDistributionPref.getDistributionType());
				}
				if(fromDistributionPref.getGrouping() != null) {
					toDistributionPref.setGrouping(fromDistributionPref.getGrouping());
				}
				toDistributionPref.setPrefLevel(fromDistributionPref.getPrefLevel());
				toDistributionPref.setOwner(toInstructor);
				toInstructor.addToPreferences(toDistributionPref);
			}
		}
	}
	
	public void copyMergeInstructorDataToSession(Session fromSession, String defaultPrefix) {
		DepartmentalInstructor toInstructor = null;
		DepartmentalInstructorDAO iDao = DepartmentalInstructorDAO.getInstance();
		try {
			if (fromSession.getDepartments() != null){
				Department primaryToDepartment = null;
				String existingQuery = "select di.department.deptCode || di.externalUniqueId from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId and di.externalUniqueId is not null";
				List<String> existingInstructors = iDao.getSession()
						.createQuery(existingQuery, String.class)
						.setParameter("sessionId", iMergedSession.getUniqueId().longValue())
						.list();
				
				String existingNoExtIdQuery = "select di.department.deptCode || di.lastName || ',' || di.firstName || ',' || di.middleName from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId and di.externalUniqueId is null";
				List<String> existingNoExtIdInstructors = iDao.getSession()
						.createQuery(existingNoExtIdQuery, String.class)
						.setParameter("sessionId", iMergedSession.getUniqueId().longValue())
						.list();
				
				List<Department> departments = DepartmentDAO.getInstance().findBySession(iDao.getSession(), fromSession.getUniqueId());
				iDao.getSession().refresh(iMergedSession);

				for(Department fromDepartment: departments){
					if (fromDepartment != null && fromDepartment.getInstructors() != null && !fromDepartment.getInstructors().isEmpty()){
						primaryToDepartment = findToDepartment(fromDepartment, defaultPrefix);							
						if (primaryToDepartment != null){
							mergeDepartmentalInstructorAttributesToSession(fromDepartment, primaryToDepartment);							
							for (DepartmentalInstructor fromInstructor : fromDepartment.getInstructors()){
								for (Department toDepartment : findToDepartmentsForInstructor(fromInstructor, defaultPrefix)) {
									if (!(fromInstructor.getExternalUniqueId() == null) && !fromInstructor.getExternalUniqueId().isEmpty() && existingInstructors.contains(toDepartment.getDeptCode()+fromInstructor.getExternalUniqueId())){
										iLog.info(fromInstructor.toString() + ": already exists in term, not merging");
										continue;
									}
									if ((fromInstructor.getExternalUniqueId() == null || fromInstructor.getExternalUniqueId().isEmpty()) && existingNoExtIdInstructors.contains(toDepartment.getDeptCode()+fromInstructor.getLastName()+","+fromInstructor.getFirstName()+","+fromInstructor.getMiddleName())){
										iLog.info(fromInstructor.toString() + ": already exists in term, not merging");
										continue;
									}
	
									toInstructor = (DepartmentalInstructor) fromInstructor.clone();
									toInstructor.setDepartment(toDepartment);
									toInstructor.setAttributes(new HashSet<InstructorAttribute>());
									for (InstructorAttribute fromAttribute: fromInstructor.getAttributes()) {
										InstructorAttribute toAttribute = fromAttribute.findSameAttributeInSession(iMergedSession);
										if (toAttribute != null) {
											toAttribute.addToInstructors(toInstructor);
											toInstructor.addToAttributes(toAttribute);
										}
									}
									mergeBuildingPrefs(fromInstructor, toInstructor, false, false, false, null, defaultPrefix);
									mergeRoomPrefsToSession(fromInstructor, toInstructor, false, false, false, null, defaultPrefix);
									mergeRoomFeaturePrefsToSession(fromInstructor, toInstructor, false, false, false, null, defaultPrefix);
									mergeRoomGroupPrefsToSession(fromInstructor, toInstructor, false, false, false, null, defaultPrefix);
									mergeTimePrefsToSession(fromInstructor, toInstructor, false, false, false, null);
									mergeInstructorDistributionPrefsToSession(fromInstructor, toInstructor);
									iDao.getSession().persist(toInstructor);
									iDao.getSession().flush();
									iDao.getSession().evict(toInstructor);
									iDao.getSession().evict(fromInstructor);
								}
							}
						} else {
							iLog.info("Primary To Department Not Found For:  " + fromDepartment.getLabel());
						}
					}
				}
				iDao.getSession().flush();
				iDao.getSession().clear();
			}
			
		} catch (Exception e) {
			iLog.error("Failed to merge all instructors to session.", e);
		}
	}
	
	
	private HashMap<Long, HashSet<String>> iOfferingPrefixesCache = new HashMap<Long, HashSet<String>>();

	private HashSet<String> getPrefixesForOffering(InstructionalOffering instructionalOffering,
			String defaultPrefix){
		HashSet<String> prefixList = new HashSet<String>();
		if (iOfferingPrefixesCache.containsKey(instructionalOffering.getUniqueId())) {
			return iOfferingPrefixesCache.get(instructionalOffering.getUniqueId());
		}
		
		if (iUseCampusPrefixForSubjectAreas) {
			for (InstrOfferingConfig ioc : instructionalOffering.getInstrOfferingConfigs()) {
				prefixList.add(getPrefixForInstrOfferingConfig(ioc, defaultPrefix));
			}
			if (prefixList.isEmpty()) {
				prefixList.add(defaultPrefix);
			}
		}
		if (prefixList.isEmpty()) {
			prefixList.add(null);
		}
		
		iOfferingPrefixesCache.put(instructionalOffering.getUniqueId(), prefixList);
		return prefixList;
	}
	
	private String getPrefixForInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig, String defaultPrefix) {
		String iocPrefix = null;
		for (SchedulingSubpart ss : instrOfferingConfig.getSchedulingSubparts()) {
			Department managingDept = ss.getManagingDept();
			if (iDepartmentCodesWithDifferentPrefix.containsKey(managingDept.getDeptCode())){
				if (iocPrefix == null) {
					iocPrefix = iDepartmentCodesWithDifferentPrefix.get(managingDept.getDeptCode());
				} else if (!iocPrefix.equals(iDepartmentCodesWithDifferentPrefix.get(managingDept.getDeptCode()))){
						iocPrefix = defaultPrefix;
						break;
				}
			} else {
				if (iocPrefix == null) {
					iocPrefix = defaultPrefix;
					break;
				}
			}
		}
		return iocPrefix;
	}
	
	
//	private InstructionalOffering createToInstructionalOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session){
//		if (courseCatalogEntry == null || session == null){
//			return(null);
//		}
//		InstructionalOffering toInstructionalOffering = new InstructionalOffering();
//		toInstructionalOffering.setNotOffered(Boolean.valueOf(false));
//		toInstructionalOffering.setSession(session);
//		toInstructionalOffering.setByReservationOnly(false);
//		return(toInstructionalOffering);
//	}
	
//	private CourseOffering createToCourseOfferingFromCourseCatalog(CourseCatalog courseCatalogEntry, Session session, String prefix){
//		if (courseCatalogEntry == null || session == null){
//			return(null);
//		}
//		CourseOffering toCourseOffering = new CourseOffering();
//		toCourseOffering.setSubjectArea(SubjectArea.findByAbbv(session.getUniqueId(), ((prefix != null? prefix + iPrefixSeparator : "") + courseCatalogEntry.getSubject())));
//		toCourseOffering.setCourseNbr(courseCatalogEntry.getCourseNumber());
//		toCourseOffering.setIsControl(Boolean.valueOf(true));
//		toCourseOffering.setExternalUniqueId(courseCatalogEntry.getExternalUniqueId());
//		toCourseOffering.setPermId(courseCatalogEntry.getPermanentId());
//		toCourseOffering.setTitle(courseCatalogEntry.getTitle());
//		toCourseOffering.setNbrExpectedStudents(Integer.valueOf(0));
//		toCourseOffering.setDemand(Integer.valueOf(0));
//		toCourseOffering.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(courseCatalogEntry.getApprovalType()));
//		if (courseCatalogEntry.getCreditFormat() != null) {
//			CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseCatalogEntry.getCreditFormat(), courseCatalogEntry.getCreditType(), courseCatalogEntry.getCreditUnitType(), courseCatalogEntry.getFixedMinimumCredit(), courseCatalogEntry.getMaximumCredit(), courseCatalogEntry.isFractionalCreditAllowed(), Boolean.valueOf(true));
//			if (ccuc instanceof ArrangeCreditUnitConfig) {					
//				ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)ccuc;
//				toAcuc.setOwner(toCourseOffering);
//				toCourseOffering.addToCreditConfigs(toAcuc);
//			} else if (ccuc instanceof FixedCreditUnitConfig) {
//				FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) ccuc;
//				toFcuc.setOwner(toCourseOffering);
//				toCourseOffering.addToCreditConfigs(toFcuc);
//			} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
//				VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) ccuc;
//				toVrcuc.setOwner(toCourseOffering);
//				toCourseOffering.addToCreditConfigs(toVrcuc);
//			} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
//				VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) ccuc;
//				toVfcuc.setOwner(toCourseOffering);
//				toCourseOffering.addToCreditConfigs(toVfcuc);
//			}		
//		}
//
//		return(toCourseOffering);
//	}
	
//	private HashMap<String, InstructionalOffering> createToInstructionalOfferingsBasedOnCourseCatalog(InstructionalOffering fromInstructionalOffering, 
//			String defaultPrefix) {
//		HashMap<String, InstructionalOffering> offeringMap = new HashMap<String, InstructionalOffering>();
//		
//		for (String prefix : getPrefixesForOffering(fromInstructionalOffering, defaultPrefix)) {
//			offeringMap.put(prefix, createToInstructionalOfferingBasedOnCourseCatalog(fromInstructionalOffering, prefix));
//		}
//		
//		return offeringMap;
//	}

	
//	private InstructionalOffering createToInstructionalOfferingBasedOnCourseCatalog(InstructionalOffering fromInstructionalOffering, String prefix){
//		if (fromInstructionalOffering == null) {
//			return(null);
//		}
//		
//		CourseCatalog controllingCourseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromInstructionalOffering.getControllingCourseOffering(), iMergedSession);
//
//		if (controllingCourseCatalogEntry == null){
//			return(null);
//		}
//		InstructionalOffering toInstructionalOffering = createToInstructionalOfferingFromCourseCatalog(controllingCourseCatalogEntry, iMergedSession);
//		toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
//		toInstructionalOffering.setInstrOfferingPermId(fromInstructionalOffering.getInstrOfferingPermId());
//		CourseOffering fromCourseOffering = null;
//		CourseOffering toCourseOffering = null;
//		CourseCatalog courseCatalogEntry = null;
//		for(Iterator<CourseOffering> coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
//			fromCourseOffering = (CourseOffering) coIt.next();
//			courseCatalogEntry = CourseCatalog.findCourseFromPreviousSessionInCatalogForSession(fromCourseOffering, iMergedSession);
//			if (courseCatalogEntry != null){
//				toCourseOffering = createToCourseOfferingFromCourseCatalog(courseCatalogEntry, iMergedSession, prefix);
//				toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
//				toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
//				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
//				toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
//				toCourseOffering.setDemand(fromCourseOffering.getDemand());
//				toCourseOffering.setInstructionalOffering(toInstructionalOffering);
//				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
//				toInstructionalOffering.addToCourseOfferings(toCourseOffering);
//				if(courseCatalogEntry.getCreditType() != null){
//					CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(courseCatalogEntry.getCreditFormat(), courseCatalogEntry.getCreditType(), courseCatalogEntry.getCreditUnitType(), courseCatalogEntry.getFixedMinimumCredit(), courseCatalogEntry.getMaximumCredit(), courseCatalogEntry.isFractionalCreditAllowed(), Boolean.valueOf(true));
//					if (ccuc instanceof ArrangeCreditUnitConfig) {					
//						ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)ccuc;
//						toAcuc.setOwner(toCourseOffering);
//						toCourseOffering.addToCreditConfigs(toAcuc);
//					} else if (ccuc instanceof FixedCreditUnitConfig) {
//						FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) ccuc;
//						toFcuc.setOwner(toCourseOffering);
//						toCourseOffering.addToCreditConfigs(toFcuc);
//					} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
//						VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) ccuc;
//						toVrcuc.setOwner(toCourseOffering);
//						toCourseOffering.addToCreditConfigs(toVrcuc);
//					} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
//						VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) ccuc;
//						toVfcuc.setOwner(toCourseOffering);
//						toCourseOffering.addToCreditConfigs(toVfcuc);
//					}
//				}				
//			}
//		}
//		if (toInstructionalOffering.getCourseOfferings().size() == 1){
//			toCourseOffering.setIsControl(Boolean.valueOf(true));
//		}
//
//		if (toInstructionalOffering.getInstrOfferingPermId() == null){
//			toInstructionalOffering.generateInstrOfferingPermId();
//		}
//		toInstructionalOffering.setUniqueId(InstructionalOfferingDAO.getInstance().getSession().persist(toInstructionalOffering)); 
//		return(toInstructionalOffering);		
//	}
	
	private HashMap<String, InstructionalOffering> createToInstructionalOfferingsFromFromInstructionalOffering(InstructionalOffering fromInstructionalOffering, 
			String defaultPrefix, boolean mergeWaitListsProhibitedOverrides) {
		HashMap<String, InstructionalOffering> offeringMap = new HashMap<String, InstructionalOffering>();
		
		for (String prefix : getPrefixesForOffering(fromInstructionalOffering, defaultPrefix)) {
			offeringMap.put(prefix, createToInstructionalOfferingFromFromInstructionalOffering(fromInstructionalOffering, 
					prefix, mergeWaitListsProhibitedOverrides));
		}
		
		return offeringMap;
	}

	private InstructionalOffering createToInstructionalOfferingFromFromInstructionalOffering(InstructionalOffering fromInstructionalOffering, 
			String prefix, boolean mergeWaitListsProhibitedOverrides){
		if (fromInstructionalOffering == null) {
			return(null);
		}

		InstructionalOffering toInstructionalOffering = null;
		CourseOffering fromControlingCourse = fromInstructionalOffering.getControllingCourseOffering();
		CourseOffering toControllingCourse = null;
		String subject = (prefix != null ? prefix + iPrefixSeparator : "") + fromControlingCourse.getSubjectArea().getSubjectAreaAbbreviation();
		toControllingCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(iMergedSession.getUniqueId(), subject, fromControlingCourse.getCourseNbr());
		if (toControllingCourse != null) {
			toInstructionalOffering = toControllingCourse.getInstructionalOffering();
		} else {
			toInstructionalOffering = new InstructionalOffering();
			toInstructionalOffering.setSession(iMergedSession);
		}
		toInstructionalOffering.setNotOffered(fromInstructionalOffering.isNotOffered());
		toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
		toInstructionalOffering.setInstrOfferingPermId(fromInstructionalOffering.getInstrOfferingPermId());
		toInstructionalOffering.setByReservationOnly(fromInstructionalOffering.isByReservationOnly());
		toInstructionalOffering.setLastWeekToEnroll(fromInstructionalOffering.getLastWeekToEnroll());
		toInstructionalOffering.setLastWeekToChange(fromInstructionalOffering.getLastWeekToChange());
		toInstructionalOffering.setLastWeekToDrop(fromInstructionalOffering.getLastWeekToDrop());
		toInstructionalOffering.setNotes(fromInstructionalOffering.getNotes());
		if (mergeWaitListsProhibitedOverrides) {
			toInstructionalOffering.setWaitlistMode(fromInstructionalOffering.getWaitlistMode());
		} else {
			toInstructionalOffering.setWaitlistMode(null);
		}
		CourseOffering fromCourseOffering = null;
		CourseOffering toCourseOffering = null;
		for(Iterator<CourseOffering> coIt = fromInstructionalOffering.getCourseOfferings().iterator(); coIt.hasNext();){
			fromCourseOffering = (CourseOffering) coIt.next();
			SubjectArea toSa = findToSubjectArea(fromCourseOffering.getSubjectArea(), prefix);
			if (toSa == null) {
				continue;
			}
			toCourseOffering = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(iMergedSession.getUniqueId(), subject, fromControlingCourse.getCourseNbr());

			if (toCourseOffering == null) {
				toCourseOffering = new CourseOffering();
				toCourseOffering.setSubjectArea(toSa);
				toCourseOffering.setCourseNbr(fromCourseOffering.getCourseNbr());
				if (fromInstructionalOffering.getCourseOfferings().size() == 1){
					toCourseOffering.setIsControl(Boolean.valueOf(true));
				} else {
					toCourseOffering.setIsControl(fromCourseOffering.isIsControl());
				}
				toCourseOffering.setExternalUniqueId(fromCourseOffering.getExternalUniqueId());
				toCourseOffering.setNbrExpectedStudents(fromCourseOffering.getNbrExpectedStudents());
				toCourseOffering.setDemand(fromCourseOffering.getDemand());
				toCourseOffering.setPermId(fromCourseOffering.getPermId());
				toCourseOffering.setScheduleBookNote(fromCourseOffering.getScheduleBookNote());
				toCourseOffering.setTitle(fromCourseOffering.getTitle());
				toCourseOffering.setUniqueIdRolledForwardFrom(fromCourseOffering.getUniqueId());
				toCourseOffering.setInstructionalOffering(toInstructionalOffering);
				toCourseOffering.setReservation(fromCourseOffering.getReservation());
				toCourseOffering.setConsentType(fromCourseOffering.getConsentType());
				toCourseOffering.setCourseType(fromCourseOffering.getCourseType());
				if (fromCourseOffering.getFundingDept() != null) {
					Department toFundingDept = findToDepartment(fromCourseOffering.getDepartment(), prefix);
					if (toFundingDept != null) {
						toCourseOffering.setFundingDept(toFundingDept);
					}
				}
				toInstructionalOffering.addToCourseOfferings(toCourseOffering);
				if (mergeWaitListsProhibitedOverrides && fromCourseOffering.getDisabledOverrides() != null) {
					toCourseOffering.setDisabledOverrides(new HashSet<OverrideType>(fromCourseOffering.getDisabledOverrides()));
				}
				if(fromCourseOffering.getCreditConfigs() != null && !fromCourseOffering.getCreditConfigs().isEmpty()){
					CourseCreditUnitConfig ccuc = null;
					for(Iterator ccIt = fromCourseOffering.getCreditConfigs().iterator(); ccIt.hasNext();){
						ccuc = (CourseCreditUnitConfig) ccIt.next();
						if (ccuc instanceof ArrangeCreditUnitConfig) {
							ArrangeCreditUnitConfig fromAcuc = (ArrangeCreditUnitConfig) ccuc;
							ArrangeCreditUnitConfig toAcuc = (ArrangeCreditUnitConfig)fromAcuc.clone();
							toAcuc.setOwner(toCourseOffering);
							toCourseOffering.addToCreditConfigs(toAcuc);
						} else if (ccuc instanceof FixedCreditUnitConfig) {
							FixedCreditUnitConfig fromFcuc = (FixedCreditUnitConfig) ccuc;
							FixedCreditUnitConfig toFcuc = (FixedCreditUnitConfig) fromFcuc.clone();
							toFcuc.setOwner(toCourseOffering);
							toCourseOffering.addToCreditConfigs(toFcuc);
						} else if (ccuc instanceof VariableRangeCreditUnitConfig) {
							VariableRangeCreditUnitConfig fromVrcuc = (VariableRangeCreditUnitConfig) ccuc;
							VariableRangeCreditUnitConfig toVrcuc = (VariableRangeCreditUnitConfig) fromVrcuc.clone();
							toVrcuc.setOwner(toCourseOffering);
							toCourseOffering.addToCreditConfigs(toVrcuc);
						} else if (ccuc instanceof VariableFixedCreditUnitConfig) {
							VariableFixedCreditUnitConfig fromVfcuc = (VariableFixedCreditUnitConfig) ccuc;
							VariableFixedCreditUnitConfig toVfcuc = (VariableFixedCreditUnitConfig) fromVfcuc.clone();
							toVfcuc.setOwner(toCourseOffering);
							toCourseOffering.addToCreditConfigs(toVfcuc);
						}
					}
				}
			}
		}
		if (toInstructionalOffering.getInstrOfferingPermId() == null){
			toInstructionalOffering.generateInstrOfferingPermId();
		}
		if (toInstructionalOffering.getUniqueId() == null) {
			InstructionalOfferingDAO.getInstance().getSession().persist(toInstructionalOffering); 
		} else {
			int ctrlCount = 0;
			for (CourseOffering co : toInstructionalOffering.getCourseOfferings()) {
				if (co.isIsControl()) {
					ctrlCount = ctrlCount + 1;
				}
			}
			if (ctrlCount != 1) {
				boolean first = true;
				for (CourseOffering co : toInstructionalOffering.getCourseOfferings()) {
					if (first) {
						co.setIsControl(true);
					} else {
						co.setIsControl(false);
					}
				}
			}
			InstructionalOfferingDAO.getInstance().getSession().merge(toInstructionalOffering);
		}
		return(toInstructionalOffering);
		
	}

	
	private HashMap<String, InstructionalOffering> findToInstructionalOffering(InstructionalOffering fromInstructionalOffering, String defaultPrefix, boolean mergeWaitListsProhibitedOverrides){
		if (fromInstructionalOffering == null) {
			return(null);
		}
		HashMap<String, InstructionalOffering> offeringMap = new HashMap<String, InstructionalOffering>();
		if (iDepartmentCodesWithDifferentPrefix == null || iDepartmentCodesWithDifferentPrefix.isEmpty()) {
			CourseOffering co = CourseOffering.findByIdRolledForwardFrom(iMergedSession.getUniqueId(), fromInstructionalOffering.getControllingCourseOffering().getUniqueId());
			if (co == null && ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {				
				co = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(iMergedSession.getUniqueId(), fromInstructionalOffering.getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation(), fromInstructionalOffering.getControllingCourseOffering().getCourseNbr());
			}
			
			if (co != null){
				InstructionalOffering toInstructionalOffering = co.getInstructionalOffering();
				if (toInstructionalOffering != null){
					toInstructionalOffering.deleteAllClasses(InstructionalOfferingDAO.getInstance().getSession());
					toInstructionalOffering.deleteAllDistributionPreferences(InstructionalOfferingDAO.getInstance().getSession());
					toInstructionalOffering.getInstrOfferingConfigs().clear();
					offeringMap.put(defaultPrefix, toInstructionalOffering);
					return(offeringMap);
				}
			}
		}
//		if (iSessionRollForward.sessionHasCourseCatalog(iMergedSession)){
//			return(createToInstructionalOfferingsBasedOnCourseCatalog(fromInstructionalOffering, defaultPrefix));
//		} else {
		return(createToInstructionalOfferingsFromFromInstructionalOffering(fromInstructionalOffering, defaultPrefix, mergeWaitListsProhibitedOverrides));
//		}
		
	}
	
	@SuppressWarnings("unused")
	private class MergeSchedSubpart{
		private SchedulingSubpart fromParentSubpart;
		private SchedulingSubpart toParentSubpart;
		private SchedulingSubpart fromSubpart;
		private SchedulingSubpart toSubpart;
		private List<SchedulingSubpart> fromChildSubparts;
		private List<SchedulingSubpart> toChildSubparts;
		private List<MergeClass> mergeClasses;
		;
		
		public List<SchedulingSubpart> getFromChildSubparts() {
			return fromChildSubparts;
		}
		public Class_ findParentClassMatchingFromParentClass(Class_ fromParentClass) {
			if (getMergeClasses() != null && getMergeClasses().size() > 0){
				for(MergeClass rfc : getMergeClasses()){
					if (rfc.getFromClass().equals(fromParentClass)){
						return(rfc.getToClass());
					}
				}
			}
			return null;
		}
		public void setFromChildSubparts(List<SchedulingSubpart> fromChildSubparts) {
			this.fromChildSubparts = fromChildSubparts;
		}
		public void addToFromChildSubparts(SchedulingSubpart fromChildSubpart){
			if (fromChildSubparts == null){
				fromChildSubparts = new ArrayList<SchedulingSubpart>();
			}
			fromChildSubparts.add(fromChildSubpart);
		}
		public SchedulingSubpart getFromParentSubpart() {
			return fromParentSubpart;
		}
		public void setFromParentSubpart(SchedulingSubpart fromParentSubpart) {
			this.fromParentSubpart = fromParentSubpart;
		}
		public SchedulingSubpart getFromSubpart() {
			return fromSubpart;
		}
		public void setFromSubpart(SchedulingSubpart fromSubpart) {
			this.fromSubpart = fromSubpart;
		}
		public List<SchedulingSubpart> getToChildSubparts() {
			return toChildSubparts;
		}
		public void setToChildSubparts(List<SchedulingSubpart> toChildSubparts) {
			this.toChildSubparts = toChildSubparts;
		}
		public void addToToChildSubparts(SchedulingSubpart toChildSubpart){
			if (toChildSubparts == null){
				toChildSubparts = new ArrayList<SchedulingSubpart>();
			}
			toChildSubparts.add(toChildSubpart);
		}
		public SchedulingSubpart getToParentSubpart() {
			return toParentSubpart;
		}
		public void setToParentSubpart(SchedulingSubpart toParentSubpart) {
			this.toParentSubpart = toParentSubpart;
		}
		public SchedulingSubpart getToSubpart() {
			return toSubpart;
		}
		public void setToSubpart(SchedulingSubpart toSubpart) {
			this.toSubpart = toSubpart;
		}
		public List<MergeClass> getMergeClasses() {
			return mergeClasses;
		}
		public void setMergeClasses(List<MergeClass> mergeClasses) {
			this.mergeClasses = mergeClasses;
		}
		public void addToMergeClasses(MergeClass mergeClasses){
			if (this.mergeClasses == null){
				this.mergeClasses = new ArrayList<MergeClass>();
			}
			this.mergeClasses.add(mergeClasses);
		}
	}
	
	@SuppressWarnings("unused")
	private class MergeClass {
		private Class_ fromParentClass;
		private Class_ toParentClass;
		private Class_ fromClass;
		private Class_ toClass;
		private List<Object> fromChildClasses;
		private List<Class_> toChildClasses;
		private MergeSchedSubpart parentSubpart;

		public List<Object> getFromChildClasses() {
			return fromChildClasses;
		}
		public void setFromChildClasses(List<Object> lastLikeChildClasses) {
			this.fromChildClasses = lastLikeChildClasses;
		}
		public void addToLastLikeChildClasses(Object fromChildClass){
			if (fromChildClasses == null){
				fromChildClasses = new ArrayList<Object>();
			}
			fromChildClasses.add(fromChildClass);
		}
		public Class_ getFromClass() {
			return fromClass;
		}
		public void setFromClass(Class_ fromClass) {
			this.fromClass = fromClass;
		}
		public Class_ getFromParentClass() {
			return fromParentClass;
		}
		public void setFromParentClass(Class_ fromParentClass) {
			this.fromParentClass = fromParentClass;
		}
		public List<Class_> getToChildClasses() {
			return toChildClasses;
		}
		public void setToChildClasses(List<Class_> newChildClasses) {
			this.toChildClasses = newChildClasses;
		}
		public void addToNewChildClasses(Class_ toChildClass){
			if (toChildClasses == null){
				toChildClasses = new ArrayList<Class_>();
			}
			toChildClasses.add(toChildClass);
		}
		public Class_ getToClass() {
			return toClass;
		}
		public void setToClass(Class_ toClass) {
			this.toClass = toClass;
		}
		public Class_ getToParentClass() {
			return toParentClass;
		}
		public void setToParentClass(Class_ toParentClass) {
			this.toParentClass = toParentClass;
		}
		public MergeSchedSubpart getParentSubpart() {
			return parentSubpart;
		}
		public void setParentSubpart(MergeSchedSubpart parentSubpart) {
			this.parentSubpart = parentSubpart;
		}
		
	}
	
	protected void mergeDatePatternPrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, 
		boolean isClassMerge,  boolean isClassPrefsPushUp, CancelledClassAction cancelledClassAction, String defaultPrefix){
		if (fromPrefGroup.getDatePatternPreferences() != null 
				&& !fromPrefGroup.getDatePatternPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassMerge)
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isClassMerge)){
			DatePatternPref fromDatePatternPref = null;
			DatePatternPref toDatePatternPref = null;
			for (Iterator it = fromPrefGroup.getDatePatternPreferences().iterator(); it.hasNext();){
				fromDatePatternPref = (DatePatternPref) it.next();
				DatePattern toDatePattern = DatePattern.findByName(iMergedSession, fromDatePatternPref.getDatePattern().getName());
				if (toDatePattern == null){
					iLog.warn("To Date Pattern not found:  " + fromDatePatternPref.getDatePattern().getName() + " for " + fromPrefGroup.htmlLabel());
					continue;
				}
				toDatePatternPref = (DatePatternPref)fromDatePatternPref.clone();
				toDatePatternPref.setDatePattern(toDatePattern);
				toDatePatternPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toDatePatternPref);
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp && (toPrefGroup.getDatePatternPreferences() == null || toPrefGroup.getDatePatternPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			Class_DAO cDao = Class_DAO.getInstance();
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, DatePatternPref> prefMap = new HashMap<String, DatePatternPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				DatePattern firstDp = null; int dpCount = 0; 
				for (Class_ c : ss.getClasses()){
					if (CancelledClassAction.SKIP == cancelledClassAction && c.isCancelled()) continue;
					clsCnt ++;
					DatePattern dp = c.effectiveDatePattern();
					if (dp != null) {
						if (firstDp == null) { firstDp = dp; dpCount ++; }
						else if (firstDp.equals(dp)) dpCount ++;
					}
					if (c.getDatePatternPreferences() != null && !c.getDatePatternPreferences().isEmpty()){
						for (Iterator rfpIt = c.getDatePatternPreferences().iterator(); rfpIt.hasNext();){
							DatePatternPref dfp = (DatePatternPref) rfpIt.next();
							key = dfp.getPrefLevel().getPrefName() + dfp.getDatePattern().getUniqueId().toString();
							prefMap.put(key, dfp);
							int cnt = 0;
							if (prefCount.containsKey(key)){
								cnt = prefCount.get(key).intValue();
							}
							cnt++;
							prefCount.put(key, Integer.valueOf(cnt));
						}
					}
				}
				if (firstDp != null && dpCount == clsCnt && !firstDp.equals(ss.effectiveDatePattern())) {
					DatePattern toDatePattern = DatePattern.findByName(iMergedSession, firstDp.getName());
					if (toDatePattern == null){
						iLog.warn("To Date Pattern not found:  " + firstDp.getName() + " for " + fromPrefGroup.htmlLabel());
					} else {
						((SchedulingSubpart)toPrefGroup).setDatePattern(toDatePattern);
						for (Class_ c: ((SchedulingSubpart)toPrefGroup).getClasses()) {
							c.setDatePattern(null);
							cDao.getSession().merge(c);
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						DatePatternPref fromDatePatternPref = prefMap.get(pref);
						DatePattern toDatePattern = DatePattern.findByName(iMergedSession, fromDatePatternPref.getDatePattern().getName());
						if (toDatePattern == null){
							iLog.warn("To Date Pattern not found:  " + fromDatePatternPref.getDatePattern().getName() + " for " + fromPrefGroup.htmlLabel());
							continue;
						}
						DatePatternPref toDatePatternPref = (DatePatternPref)fromDatePatternPref.clone();
						toDatePatternPref.setDatePattern(toDatePattern);
						toDatePatternPref.setOwner(toPrefGroup);
						toPrefGroup.addToPreferences(toDatePatternPref);
					}
				}
			}				
		}
	}

	public boolean isMergeDistributionsToSession(DistributionPref dp, DistributionMode distributionPrefMode) {
		switch (distributionPrefMode) {
		case ALL:
			return true;
		case NONE:
			return false;
		case SUBPART: // there are no classes
			for (DistributionObject distObj: dp.getDistributionObjects())
				if (distObj.getPrefGroup() instanceof Class_) return false;
			return true;
		case MIXED: // there is at least one subpart
			for (DistributionObject distObj: dp.getDistributionObjects())
				if (distObj.getPrefGroup() instanceof SchedulingSubpart) return true;
			return false;
		default:
			return false;
		}
	}

	
	protected void mergeDistributionPrefsToSession(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, 
			DistributionMode distributionPrefMode, String defaultPrefix){
		if (fromPrefGroup.getDistributionObjects() != null && !fromPrefGroup.getDistributionObjects().isEmpty()){
			DistributionObject fromDistObj = null;
			DistributionObject toDistObj = null;
			DistributionPref fromDistributionPref = null;
			DistributionPref toDistributionPref = null;
			DistributionPrefDAO dpDao = DistributionPrefDAO.getInstance();
			for (Iterator<DistributionObject> it = fromPrefGroup.getDistributionObjects().iterator(); it.hasNext(); ){
				fromDistObj = (DistributionObject) it.next();
				toDistObj = new DistributionObject();
				fromDistributionPref = fromDistObj.getDistributionPref();
				if (!isMergeDistributionsToSession(fromDistributionPref, distributionPrefMode)) continue;
				toDistributionPref = DistributionPref.findByIdRolledForwardFrom(fromDistributionPref.getUniqueId(), iMergedSession.getUniqueId());
				if (toDistributionPref == null){
					toDistributionPref = new DistributionPref();
					toDistributionPref.setDistributionType(fromDistributionPref.getDistributionType());
					toDistributionPref.setGrouping(fromDistributionPref.getGrouping());
					toDistributionPref.setPrefLevel(fromDistributionPref.getPrefLevel());
					toDistributionPref.setUniqueIdRolledForwardFrom(fromDistributionPref.getUniqueId());
					Department toDept = findToDepartment((Department)fromDistributionPref.getOwner(), defaultPrefix);
					if (toDept != null){
						toDistributionPref.setOwner(toDept);
						toDept.addToPreferences(toDistributionPref);
					} else {
						continue;
					}
				}
				toDistObj.setDistributionPref(toDistributionPref);
				toDistObj.setPrefGroup(toPrefGroup);
				toDistObj.setSequenceNumber(fromDistObj.getSequenceNumber());
				toPrefGroup.addToDistributionObjects(toDistObj);
				if (toDistributionPref.getUniqueId() == null)
					dpDao.getSession().persist(toDistributionPref);
				else
					dpDao.getSession().merge(toDistributionPref);
			}
		}		
	}

	
	private void mergeCourseCreditUnitConfigForSchedSubpart(SchedulingSubpart fromSubpart, SchedulingSubpart toSubpart){
		if (iSessionRollForward.sessionHasCourseCatalog(toSubpart.getSession())){
			if (fromSubpart.getParentSubpart() != null && !fromSubpart.getParentSubpart().getItype().getItype().equals(fromSubpart.getItype().getItype())){
				CourseCatalog courseCatalogEntry = CourseCatalog.findCourseInCatalogForSession(toSubpart.getControllingCourseOffering(), toSubpart.getSession());
				if (courseCatalogEntry != null && courseCatalogEntry.getSubparts() != null){
					CourseSubpartCredit csc = null;
					boolean found = false;
					for (Iterator<CourseSubpartCredit> cscIt = courseCatalogEntry.getSubparts().iterator(); (cscIt.hasNext() && !found);){
						csc = (CourseSubpartCredit) cscIt.next();
						if (csc.getSubpartId().equals(toSubpart.getItype().getItype().toString())){
							found = true;
						}
					}
					if (found){
						CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(csc.getCreditFormat(), csc.getCreditType(), csc.getCreditUnitType(), csc.getFixedMinimumCredit(), csc.getMaximumCredit(), csc.isFractionalCreditAllowed(), Boolean.valueOf(true));
						ccuc.setOwner(toSubpart);
						toSubpart.setCredit(ccuc);
					}
				}
			}	
		} else if (fromSubpart.getCredit() != null){
			Float units = null;
			Float maxUnits = null;
			Boolean fractionalIncrementsAllowed = null;
			if (fromSubpart.getCredit() instanceof FixedCreditUnitConfig) {
				FixedCreditUnitConfig fcuc = (FixedCreditUnitConfig) fromSubpart.getCredit();
				units = fcuc.getFixedUnits();
			} else if (fromSubpart.getCredit() instanceof VariableFixedCreditUnitConfig) {
				VariableFixedCreditUnitConfig vfcuc = (VariableFixedCreditUnitConfig) fromSubpart.getCredit();
				units = vfcuc.getMinUnits();
				maxUnits = vfcuc.getMaxUnits();
				
			} else if (fromSubpart.getCredit() instanceof VariableRangeCreditUnitConfig) {
				VariableRangeCreditUnitConfig vrcuc = (VariableRangeCreditUnitConfig) fromSubpart.getCredit();
				units = vrcuc.getMinUnits();
				maxUnits = vrcuc.getMaxUnits();
				fractionalIncrementsAllowed = vrcuc.isFractionalIncrementsAllowed();
			}
			CourseCreditUnitConfig ccuc = CourseCreditUnitConfig.createCreditUnitConfigOfFormat(fromSubpart.getCredit().getCreditFormat(), fromSubpart.getCredit().getCreditType(), fromSubpart.getCredit().getCreditUnitType(), units, maxUnits, fractionalIncrementsAllowed, Boolean.valueOf(false));
			ccuc.setOwner(toSubpart);
			toSubpart.setCredit(ccuc);
		}	
	}

	private Class_ mergeClassToSession(Class_ fromClass,SchedulingSubpart toSubpart,
			boolean isClassMerge, boolean isSubpartTimePrefMerge, boolean isSubpartLocationPrefMerge,  
			boolean isClassPrefsPushUp, DistributionMode distributionPrefMode,
			CancelledClassAction cancelledClassAction, String defaultPrefix) throws Exception{
		Class_ toClass = new Class_();

		toClass.setEnabledForStudentScheduling(fromClass.isEnabledForStudentScheduling());
		toClass.setDisplayInstructor(fromClass.isDisplayInstructor());
		toClass.setExpectedCapacity(fromClass.getExpectedCapacity());
		toClass.setMaxExpectedCapacity(fromClass.getMaxExpectedCapacity());
		toClass.setNbrRooms(fromClass.getNbrRooms());
		toClass.setNotes(fromClass.getNotes());
		toClass.setRoomRatio(fromClass.getRoomRatio());
		toClass.setSchedulePrintNote(fromClass.getSchedulePrintNote());
		toClass.setSchedulingSubpart(toSubpart);
		toClass.setUniqueIdRolledForwardFrom(fromClass.getUniqueId());
		if (fromClass.getFundingDept() != null) {
			Department toFundingDept = findToDepartment(fromClass.getFundingDept(), defaultPrefix);
			if (toFundingDept != null) {
				toClass.setFundingDept(toFundingDept);
			}
		}
		if (fromClass.getLmsInfo() != null) {
			LearningManagementSystemInfo lms = LearningManagementSystemInfo.findBySessionIdAndReference(iMergedSession.getUniqueId(), fromClass.getLmsInfo().getReference());
			toClass.setLms(lms);
		}
		if (!iResetClassSuffix) {
			toClass.setClassSuffix(fromClass.getClassSuffix());
			toClass.setExternalUniqueId(fromClass.getExternalUniqueId());
		}
		if (CancelledClassAction.KEEP == cancelledClassAction) {
			toClass.setCancelled(fromClass.isCancelled());
		} else {
			toClass.setCancelled(false);
		}
		toSubpart.addToClasses(toClass);
		if (fromClass.getManagingDept() != null && !fromClass.getManagingDept().equals(fromClass.getControllingDept())){
			toClass.setManagingDept(findToDepartment(fromClass.getManagingDept(), defaultPrefix));
		}
		if (fromClass.getDatePattern() != null){
			DatePattern toDp = DatePattern.findByName(iMergedSession, fromClass.getDatePattern().getName());
			if (toDp == null){
				toDp = fromClass.getDatePattern().findCloseMatchDatePatternInSession(iMergedSession);
			}
			toClass.setDatePattern(toDp);
		}
		if (isClassMerge) {
			mergeTimePrefsToSession(fromClass, toClass, isClassMerge, isSubpartTimePrefMerge, isClassPrefsPushUp, cancelledClassAction);
			mergeBuildingPrefs(fromClass, toClass, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
			mergeRoomPrefsToSession(fromClass, toClass, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
			mergeRoomGroupPrefsToSession(fromClass, toClass, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
			mergeRoomFeaturePrefsToSession(fromClass, toClass, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
			mergeDatePatternPrefsToSession(fromClass, toClass, isClassMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		}
		if (distributionPrefMode != null && distributionPrefMode != DistributionMode.NONE)
			mergeDistributionPrefsToSession(fromClass, toClass, distributionPrefMode, defaultPrefix);

		if (fromClass.getCommittedAssignment() != null) {
			RoomFeature rf = getCampusRoomFeature(findPrefix(fromClass.getManagingDept().getDeptCode(), defaultPrefix));
			if (rf != null) {
				RoomFeaturePref rfp = new RoomFeaturePref();
				rfp.setOwner(toClass);
				rfp.setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sRequired));
				rfp.setRoomFeature(rf);
				toClass.addToPreferences(rfp);
			}
		}
		return(toClass);
	}


	private void mergeSchedulingSubpartToSession(InstrOfferingConfig toInstrOffrConfig, SchedulingSubpart fromSubpart, MergeSchedSubpart parentSubpart, 
			boolean isClassMerge, boolean isSubpartTimePrefMerge, boolean isSubpartLocationPrefMerge,  boolean isClassPrefsPushUp, DistributionMode distributionPrefMode,
			CancelledClassAction cancelledClassAction, String defaultPrefix) throws Exception{
		SchedulingSubpart toSubpart = new SchedulingSubpart();
		toSubpart.setAutoSpreadInTime(fromSubpart.isAutoSpreadInTime());
		toSubpart.setStudentAllowOverlap(fromSubpart.isStudentAllowOverlap());
		toSubpart.setInstrOfferingConfig(toInstrOffrConfig);
		toInstrOffrConfig.addToschedulingSubparts(toSubpart);
		toSubpart.setMinutesPerWk(fromSubpart.getMinutesPerWk());
		toSubpart.setItype(fromSubpart.getItype());
		toSubpart.setUniqueIdRolledForwardFrom(fromSubpart.getUniqueId());
		
		mergeCourseCreditUnitConfigForSchedSubpart(fromSubpart, toSubpart);
		if (fromSubpart.getDatePattern() != null){
			DatePattern toDp = null;
			toDp = DatePattern.findByName(iMergedSession, fromSubpart.getDatePattern().getName());
			if (toDp == null){
				toDp = fromSubpart.getDatePattern().findCloseMatchDatePatternInSession(iMergedSession);
			}
			toSubpart.setDatePattern(toDp);
		}
		
		MergeSchedSubpart rfSs = new MergeSchedSubpart();
		rfSs.setFromSubpart(fromSubpart);
		rfSs.setToSubpart(toSubpart);

		if (parentSubpart != null){
			rfSs.setFromParentSubpart(parentSubpart.getFromSubpart());
			parentSubpart.addToFromChildSubparts(fromSubpart);
			rfSs.setToParentSubpart(parentSubpart.getToSubpart());
			parentSubpart.addToToChildSubparts(toSubpart);			
			toSubpart.setParentSubpart(parentSubpart.getToSubpart());
			parentSubpart.getToSubpart().addToChildSubparts(toSubpart);
		}
				
		SchedulingSubpartDAO.getInstance().getSession().persist(toSubpart);
		InstrOfferingConfigDAO iocDao = InstrOfferingConfigDAO.getInstance();
		iocDao.getSession().merge(toInstrOffrConfig);
		if (fromSubpart.getClasses() != null && fromSubpart.getClasses().size() > 0){
			List<Class_> classes = new ArrayList<Class_>(fromSubpart.getClasses());
			Collections.sort(classes, new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY));
			Class_ toClass = null;
			for (Class_ fromClass : classes){
				if (CancelledClassAction.SKIP == cancelledClassAction && fromClass.isCancelled()) continue;
				toClass = mergeClassToSession(fromClass, toSubpart, isClassMerge, isSubpartTimePrefMerge, isSubpartLocationPrefMerge, 
						isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, defaultPrefix);
				MergeClass rfc = new MergeClass();
				rfc.setToClass(toClass);
				rfc.setFromClass(fromClass);
				rfc.setFromParentClass(fromClass.getParentClass());
				rfc.setParentSubpart(rfSs);
				rfSs.addToMergeClasses(rfc);
				if (fromClass.getChildClasses() != null && fromClass.getChildClasses().size() > 0){
					for (Iterator<Class_> ccIt = fromClass.getChildClasses().iterator(); ccIt.hasNext();){
						rfc.addToLastLikeChildClasses(ccIt.next());
					}
				}
				if (parentSubpart != null){
					Class_ parentClass = parentSubpart.findParentClassMatchingFromParentClass(fromClass.getParentClass());
					toClass.setParentClass(parentClass);
					parentClass.addToChildClasses(toClass);
				}
				Class_DAO.getInstance().getSession().persist(toClass);
			}
		}
		iocDao.getSession().merge(toInstrOffrConfig);

		mergeTimePrefsToSession(fromSubpart, toSubpart, isClassMerge, isSubpartTimePrefMerge, isClassPrefsPushUp, cancelledClassAction);
		mergeBuildingPrefs(fromSubpart, toSubpart, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		mergeRoomPrefsToSession(fromSubpart, toSubpart, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		mergeRoomGroupPrefsToSession(fromSubpart, toSubpart, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		mergeRoomFeaturePrefsToSession(fromSubpart, toSubpart, isClassMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		mergeDatePatternPrefsToSession(fromSubpart, toSubpart, isClassMerge, isClassPrefsPushUp, cancelledClassAction, defaultPrefix);
		if (distributionPrefMode != null) mergeDistributionPrefsToSession(fromSubpart, toSubpart, distributionPrefMode, defaultPrefix);
		if (fromSubpart.getChildSubparts() != null && fromSubpart.getChildSubparts().size() > 0){
			List<SchedulingSubpart> childSubparts = new ArrayList<SchedulingSubpart>(fromSubpart.getChildSubparts());
			Collections.sort(childSubparts, new SchedulingSubpartComparator());
			for(SchedulingSubpart childSubpart : childSubparts){
				mergeSchedulingSubpartToSession(toInstrOffrConfig, childSubpart, rfSs, isClassMerge, isSubpartTimePrefMerge, isSubpartLocationPrefMerge, 
						isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, defaultPrefix);
			}
		}
		iocDao.getSession().merge(toInstrOffrConfig);
	}

	private void mergeSchedSubpartsForAConfigToSession(InstrOfferingConfig ioc, InstrOfferingConfig newIoc,
			boolean isClassMerge, boolean isSubpartTimePrefMerge, boolean isSubpartLocationPrefMerge, boolean isClassPrefsPushUp, 
			DistributionMode distributionPrefMode, CancelledClassAction cancelledClassAction, String defaultPrefix) throws Exception{
		if (ioc.getSchedulingSubparts() != null && ioc.getSchedulingSubparts().size() > 0){
			List<SchedulingSubpart> subparts = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
			Collections.sort(subparts, new SchedulingSubpartComparator());
			for(SchedulingSubpart ss : subparts){
				if (ss.getParentSubpart() == null){
					mergeSchedulingSubpartToSession(newIoc, ss, null, isClassMerge, isSubpartTimePrefMerge, isSubpartLocationPrefMerge,
							isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, defaultPrefix);
				}
			}
		}
	}

	
	public void mergeInstructionalOfferingToSession(InstructionalOffering fromInstructionalOffering, Session fromSession, boolean mergeWaitListsProhibitedOverrides,
			boolean isClassMerge, boolean isSubpartTimePrefMerge, boolean isSubpartLocationPrefMerge, boolean isClassPrefsPushUp, 
			DistributionMode distributionPrefMode, CancelledClassAction cancelledClassAction, String defaultPrefix){
		InstructionalOfferingDAO ioDao = InstructionalOfferingDAO.getInstance();
		InstrOfferingConfigDAO iocDao = InstrOfferingConfigDAO.getInstance();
		iLog.info("Merging " + fromInstructionalOffering.getCourseNameWithTitle());
		Transaction trns = null;
		try {
			if (ioDao.getSession().getTransaction()==null || !ioDao.getSession().getTransaction().isActive())
				trns = ioDao.getSession().beginTransaction();
			HashMap<String, InstructionalOffering> toInstructionalOfferingMap = findToInstructionalOffering(fromInstructionalOffering, defaultPrefix, mergeWaitListsProhibitedOverrides);
			if (toInstructionalOfferingMap == null){
				return;
			}
			
			for (InstructionalOffering toInstructionalOffering : toInstructionalOfferingMap.values()) {
				if (toInstructionalOffering == null) {
					continue;
				}
				if (toInstructionalOffering.getInstrOfferingConfigs() != null && toInstructionalOffering.getInstrOfferingConfigs().size() > 0){
					toInstructionalOffering.getInstrOfferingConfigs().clear();
				}
				toInstructionalOffering.setNotOffered(fromInstructionalOffering.isNotOffered());
				toInstructionalOffering.setUniqueIdRolledForwardFrom(fromInstructionalOffering.getUniqueId());
				
			}
			
			
			
			InstrOfferingConfig fromInstrOffrConfig = null;
			InstrOfferingConfig toInstrOffrConfig = null;
			if (fromInstructionalOffering.getInstrOfferingConfigs() != null && fromInstructionalOffering.getInstrOfferingConfigs().size() > 0){
				List<InstrOfferingConfig> fromInstrOffrConfigs = new ArrayList<InstrOfferingConfig>(fromInstructionalOffering.getInstrOfferingConfigs());
				Collections.sort(fromInstrOffrConfigs, new InstrOfferingConfigComparator(null));
				for (Iterator<InstrOfferingConfig> it = fromInstrOffrConfigs.iterator(); it.hasNext();){
					fromInstrOffrConfig = (InstrOfferingConfig) it.next();
					InstructionalOffering toInstructionalOffering = toInstructionalOfferingMap.get(getPrefixForInstrOfferingConfig(fromInstrOffrConfig, defaultPrefix));
					if (toInstructionalOffering == null) {
						continue;
					}
					toInstrOffrConfig = new InstrOfferingConfig();
					toInstrOffrConfig.setLimit(fromInstrOffrConfig.getLimit());
					toInstrOffrConfig.setInstructionalOffering(toInstructionalOffering);
					toInstrOffrConfig.setName(fromInstrOffrConfig.getName());
					toInstrOffrConfig.setUnlimitedEnrollment(fromInstrOffrConfig.isUnlimitedEnrollment());
					toInstrOffrConfig.setUniqueIdRolledForwardFrom(fromInstrOffrConfig.getUniqueId());
					toInstrOffrConfig.setClassDurationType(fromInstrOffrConfig.getClassDurationType());
					toInstrOffrConfig.setInstructionalMethod(fromInstrOffrConfig.getInstructionalMethod());
					toInstructionalOffering.addToInstrOfferingConfigs(toInstrOffrConfig);
					iocDao.getSession().persist(toInstrOffrConfig);
					ioDao.getSession().merge(toInstructionalOffering);
					mergeSchedSubpartsForAConfigToSession(fromInstrOffrConfig, toInstrOffrConfig, isClassMerge, isSubpartTimePrefMerge, isSubpartLocationPrefMerge,
							isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, defaultPrefix);
					ioDao.getSession().merge(toInstructionalOffering);
				}
			}
			if (trns != null && trns.isActive()) {
				trns.commit();
			}
			ioDao.getSession().flush();
			for (InstructionalOffering toInstructionalOffering : toInstructionalOfferingMap.values()) {
				if (toInstructionalOffering == null) {
					continue;
				}
				ioDao.getSession().evict(toInstructionalOffering);
			}
			ioDao.getSession().evict(fromInstructionalOffering);
		} catch (Exception e){
			iLog.error("Failed to merge " + fromInstructionalOffering.getCourseName(), e);
			if (trns != null){
				if (trns.isActive()){
					trns.rollback();
				}
			}
		}		
	}

	
	public void mergeInstructionalOfferingsForASubjectAreaToSession(SubjectArea subjectArea, Session fromSession,
			boolean mergeWaitListsProhibitedOverrides,
			boolean isClassMerge, 
			boolean isSubpartTimePrefMerge, 
			boolean isSubpartLocationPrefMerge, 
			boolean isClassPrefsPushUp, 
			DistributionMode distributionPrefMode, 
			CancelledClassAction cancelledClassAction,
			String prefix){
		CourseOfferingDAO coDao = CourseOfferingDAO.getInstance();
		String query = "from CourseOffering as co where co.subjectArea.subjectAreaAbbreviation = '" + subjectArea.getSubjectAreaAbbreviation()
			+ "' and co.isControl = true"
			+ " and co.subjectArea.session.uniqueId = " + fromSession.getUniqueId();
		List<CourseOffering> l = coDao.getSession().createQuery(query, CourseOffering.class).list();
		if (l != null){
			CourseOffering co = null;
			for (Iterator<CourseOffering> it = l.iterator(); it.hasNext();){
				co = (CourseOffering) it.next();
				mergeInstructionalOfferingToSession(co.getInstructionalOffering(), fromSession, mergeWaitListsProhibitedOverrides, isClassMerge, 
						isSubpartTimePrefMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, prefix);
				coDao.getSession().flush();
			}
		}
	}
	
	public void copyMergeCourseOfferingsToSession(Session fromSession, 
			String classPrefsAction,
			String subpartLocationPrefsAction,
			String subpartTimePrefsAction,
			boolean mergeWaitListsProhibitedOverrides,
			DistributionMode distributionPrefMode, 
			CancelledClassAction cancelledClassAction,
			String prefix) {

		boolean isClassMerge = (classPrefsAction != null && classPrefsAction.equalsIgnoreCase(SessionRollForward.ROLL_PREFS_ACTION) ? true : false);
		boolean isClassPrefsPushUp = (classPrefsAction != null && classPrefsAction.equalsIgnoreCase(SessionRollForward.PUSH_UP_ACTION) ? true : false);
		boolean isSubpartTimePrefMerge = (subpartTimePrefsAction != null && subpartTimePrefsAction.equalsIgnoreCase(SessionRollForward.DO_NOT_ROLL_ACTION) ? false : true);
		boolean isSubpartLocationPrefMerge = (subpartLocationPrefsAction != null && subpartLocationPrefsAction.equalsIgnoreCase(SessionRollForward.DO_NOT_ROLL_ACTION) ? false : true);
		
		if (iMergedSession.getSubjectAreas() != null) {
			List<SubjectArea> fromSubjectAreas = SubjectAreaDAO.getInstance().findBySession(SubjectAreaDAO.getInstance().getSession(), fromSession.getUniqueId());
			for (SubjectArea subjectArea : fromSubjectAreas){
				mergeInstructionalOfferingsForASubjectAreaToSession(subjectArea, fromSession,
						mergeWaitListsProhibitedOverrides, isClassMerge, 
						isSubpartTimePrefMerge, isSubpartLocationPrefMerge, isClassPrefsPushUp, distributionPrefMode, cancelledClassAction, prefix);
			}
		}
	}

	public void copyMergeClassInstructorsToSession(Session fromSession, 
			String prefix) {
		SubjectAreaDAO sDao = SubjectAreaDAO.getInstance();
		List<SubjectArea> subjects = sDao.findBySession(sDao.getSession(), fromSession.getUniqueId());
		sDao.getSession().refresh(iMergedSession);
		
		if (iMergedSession.getSubjectAreas() != null) {
			for (SubjectArea subjectArea : subjects){
				copyMergeClassInstructorsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), prefix);
			}
		}		
	}

	private void copyMergeClassInstructorsForASubjectArea(
			String subjectAreaAbbreviation, String defaultPrefix) {
		iLog.info("Rolling forward class instructors for:  " + subjectAreaAbbreviation);
		Class_DAO clsDao = new Class_DAO();
		org.hibernate.Session hibSession = clsDao.getSession();
		hibSession.clear();
		List<Class_> classes = Class_.findAllForControllingSubjectArea(subjectAreaAbbreviation, iMergedSession.getUniqueId(), hibSession);
		if (classes != null && !classes.isEmpty()){
			Class_ toClass = null;
			Class_ fromClass = null;
			for (Iterator<Class_> cIt = classes.iterator(); cIt.hasNext();){
				toClass = (Class_) cIt.next();
				if (toClass.getUniqueIdRolledForwardFrom() != null){
					
					fromClass = clsDao.get(toClass.getUniqueIdRolledForwardFrom(), hibSession);
					if (fromClass != null){
						if (fromClass.getClassInstructors() != null && !fromClass.getClassInstructors().isEmpty()) {
							ClassInstructor fromClassInstr = null;
							ClassInstructor toClassInstr = null;
							DepartmentalInstructor toDeptInstr = null;
							for (Iterator<ClassInstructor> ciIt = fromClass.getClassInstructors().iterator(); ciIt.hasNext();){
								fromClassInstr = (ClassInstructor) ciIt.next();
								if (fromClassInstr.getTeachingRequest() != null) continue;
								toDeptInstr = DepartmentalInstructor.findByPuidDepartmentId(fromClassInstr.getInstructor().getExternalUniqueId(), toClass.getControllingDept().getUniqueId());
								if (toDeptInstr != null){
									toClassInstr = new ClassInstructor();
									toClassInstr.setClassInstructing(toClass);
									toClassInstr.setInstructor(toDeptInstr);
									toClassInstr.setLead(fromClassInstr.isLead());
									toClassInstr.setPercentShare(fromClassInstr.getPercentShare());
									toClassInstr.setResponsibility(fromClassInstr.getResponsibility());
									
									toClassInstr.setUniqueId(null);
									toClass.addToClassInstructors(toClassInstr);
									toDeptInstr.addToClasses(toClassInstr);
									hibSession.evict(fromClassInstr);
								}
							}
							hibSession.evict(fromClass);
							Transaction t = hibSession.beginTransaction();
							hibSession.merge(toClass);
							t.commit();
						} else {
							hibSession.evict(fromClass);
						}
					}
				}
				hibSession.evict(toClass);
			}	
		}
	}

	
}
