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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.Campus;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DatePatternPref;
import org.unitime.timetable.model.Degree;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamPeriodPref;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.LastLikeCourseDemand;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PeriodicTask;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Program;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.SavedHQL;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.ScriptParameter;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SessionConfig;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.StandardEventNoteDepartment;
import org.unitime.timetable.model.StandardEventNoteSession;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.TaskParameter;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.AcademicClassificationDAO;
import org.unitime.timetable.model.dao.CampusDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DegreeDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;
import org.unitime.timetable.model.dao.PosMajorDAO;
import org.unitime.timetable.model.dao.PosMinorDAO;
import org.unitime.timetable.model.dao.ProgramDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.script.SaveTaskBackend;


/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 *
 */
public class SessionRollForward {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected Log iLog;
	private org.hibernate.Session iHibSession;
	
	private HashMap roomList;
	private HashMap sessionHasCourseCatalogList;
	private HashMap sessionHasExternalBuildingList;
	private HashMap sessionHasExternalRoomList;
	private HashMap sessionHasExternalRoomDeptList;
	private HashMap sessionHasExternalRoomFeatureList;
	
	private boolean subpartTimeRollForward;
	private boolean subpartLocationRollForward;
	
	private boolean classPrefsPushUp;
	private boolean classRollForward;
	private DistributionMode rollForwardDistributions = DistributionMode.MIXED;
	
	private CancelledClassAction cancelledClassAction = CancelledClassAction.REOPEN;
	
	private boolean resetClassSuffix;
	
	private boolean waitListsAndProhibitedOverrides;

	public static String ROLL_PREFS_ACTION = "rollUnchanged";
	public static String DO_NOT_ROLL_ACTION = "doNotRoll";
	public static String PUSH_UP_ACTION = "pushUp";
	public static String EXAMS_NO_PREF = "doNotRoll";
	public static String EXAMS_ROOM_PREFS = "rollRoomPrefs"; 
	public static String EXAMS_ALL_PREF = "rollAllPrefs";
	
	public static enum CancelledClassAction {
		REOPEN,
		KEEP,
		SKIP
	}
	
	public static enum DistributionMode {
		ALL,
		MIXED,
		SUBPART,
		NONE,
	}
	
	public static enum StudentEnrollmentMode {
		LAST_LIKE,
		STUDENT_CLASS_ENROLLMENTS,
		STUDENT_COURSE_REQUESTS,
		POINT_IN_TIME_CLASS_ENROLLMENTS
	}
	
	public org.hibernate.Session getHibSession() {
		if (iHibSession == null)
			iHibSession = HibernateUtil.getSession();
		return iHibSession;
	}
	
	
	public SessionRollForward(Log log) {
		iLog = log;
		resetClassSuffix = ApplicationProperty.RollForwardResetClassSuffix.isTrue();
	}

	public void setSubpartLocationPrefRollForwardParameters(String subpartLocationPrefsAction){
		if (subpartLocationPrefsAction == null || subpartLocationPrefsAction.equalsIgnoreCase(ROLL_PREFS_ACTION)){
			subpartLocationRollForward = true;
		} else if (subpartLocationPrefsAction.equalsIgnoreCase(DO_NOT_ROLL_ACTION)) {
			subpartLocationRollForward = false;
		} else {
			subpartLocationRollForward = true;
		}
	}
	
	public void setSubpartTimePrefRollForwardParameters(String subpartTimePrefsAction){
		if (subpartTimePrefsAction == null || subpartTimePrefsAction.equalsIgnoreCase(ROLL_PREFS_ACTION)){
			subpartTimeRollForward = true;
		} else if (subpartTimePrefsAction.equalsIgnoreCase(DO_NOT_ROLL_ACTION)) {
			subpartTimeRollForward = false;
		} else {
			subpartTimeRollForward = true;
		}
	}
	
	public void setClassPrefRollForwardParameter(String classPrefsAction){
		if (classPrefsAction == null || classPrefsAction.equalsIgnoreCase(DO_NOT_ROLL_ACTION)){
			classPrefsPushUp = false;
			classRollForward = false;
		} else if (classPrefsAction.equalsIgnoreCase(PUSH_UP_ACTION)){
			classPrefsPushUp = true;
			classRollForward = false;
		} else if (classPrefsAction.equalsIgnoreCase(ROLL_PREFS_ACTION)){
			classRollForward = true;
			classPrefsPushUp = false;
		} else {
			classPrefsPushUp = false;
			classRollForward = false;
		}
	}
	
	public void setRollForwardDistributions(String rollForwardDistributions) {
		this.rollForwardDistributions = (rollForwardDistributions == null ? DistributionMode.MIXED : DistributionMode.valueOf(rollForwardDistributions));
	}
	
	public void setCancelledClassActionRollForwardParameter(String cancelledClassAction){
		this.cancelledClassAction = (cancelledClassAction == null ? CancelledClassAction.REOPEN : CancelledClassAction.valueOf(cancelledClassAction));
	}
	
	public void setWaitListsAndProhibitedOverrides(Boolean waitListsAndProhibitedOverrides) {
		this.waitListsAndProhibitedOverrides = (waitListsAndProhibitedOverrides == null ? false : waitListsAndProhibitedOverrides.booleanValue());
	}
	
	public boolean isWaitListsAndProhibitedOverrides() { return waitListsAndProhibitedOverrides; }
	
	public void rollBuildingAndRoomDataForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollRoomDataForwardFrom());

		rollRoomFeaturesForward(errors, fromSession, toSession);
		rollRoomGroupsForward(errors, fromSession, toSession);
		rollBuildingsForward(errors, fromSession, toSession);
		rollLocationsForward(errors, fromSession, toSession);
		rollTravelTimesForward(errors, fromSession, toSession);
		rollRoomPartitionsForward(errors, fromSession, toSession);
	}

	private void rollRoomGroupsForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		RoomGroup fromRoomGroup = null;
		RoomGroup toRoomGroup = null;
		Collection fromRoomGroups = RoomGroup.getAllRoomGroupsForSession(fromSession);
		if (fromRoomGroups != null && !fromRoomGroups.isEmpty()){
			for (Iterator it = fromRoomGroups.iterator(); it.hasNext();){
				fromRoomGroup = (RoomGroup) it.next();
				if (fromRoomGroup != null){
					toRoomGroup = (RoomGroup) fromRoomGroup.clone();
					toRoomGroup.setSession(toSession);
					if (fromRoomGroup.getDepartment() != null)
						toRoomGroup.setDepartment(fromRoomGroup.getDepartment().findSameDepartmentInSession(toSession));
					getHibSession().persist(toRoomGroup);
				}
			}
		}
		getHibSession().flush();
	}

	private void rollRoomFeaturesForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		DepartmentRoomFeature fromRoomFeature = null;
		DepartmentRoomFeature toRoomFeature = null;
		Collection fromRoomFeatures = DepartmentRoomFeature.getAllRoomFeaturesForSession(fromSession);
		if (fromRoomFeatures != null && !fromRoomFeatures.isEmpty()){
			for(Iterator it = fromRoomFeatures.iterator(); it.hasNext();){
				fromRoomFeature = (DepartmentRoomFeature) it.next();
				if (fromRoomFeature != null){
					toRoomFeature = (DepartmentRoomFeature)fromRoomFeature.clone();
					toRoomFeature.setDepartment(fromRoomFeature.getDepartment().findSameDepartmentInSession(toSession));
					getHibSession().persist(toRoomFeature);
				}
			}
		}
		Set<String> globalFeatures = new HashSet<String>();
		for (GlobalRoomFeature fromRoomFeatureGlobal: GlobalRoomFeature.getAllGlobalRoomFeatures(fromSession)) {
			GlobalRoomFeature toRoomFeatureGlobal = (GlobalRoomFeature)fromRoomFeatureGlobal.clone();
			toRoomFeatureGlobal.setSession(toSession);
			getHibSession().persist(toRoomFeatureGlobal);
			globalFeatures.add(fromRoomFeatureGlobal.getLabel());
		}
		if (sessionHasExternalRoomFeatureList(toSession)){
			GlobalRoomFeature grf = null;
			List<Object[]> newGlobalFeatures = getHibSession().createQuery("select distinct erf.value, erf.name from ExternalRoomFeature erf" +
				" where erf.room.building.session.uniqueId=:sessionId", Object[].class)
				.setParameter("sessionId", toSession.getUniqueId())
				.list();
			if (newGlobalFeatures != null){
				String newLabel = null;
				String newSisReference = null;
				for (Iterator<Object[]> nrfIt = newGlobalFeatures.iterator(); nrfIt.hasNext();){
					Object[] o = nrfIt.next();
					newLabel = (String)o[0];
					if (globalFeatures.contains(newLabel)) continue;
					newSisReference = (String)o[1];
					grf = new GlobalRoomFeature();
					grf.setLabel(newLabel);
					grf.setSisReference(newSisReference);
					grf.setSisValue(null);
					grf.setSession(toSession);
					getHibSession().persist(grf);
				}
			}
		}
		getHibSession().flush();
	}

	private void rollRoomFeaturesForLocationForward(Location fromLocation, Location toLocation, Session toSession, HashMap roomFeatureCache){
		if(fromLocation.getFeatures() != null && !fromLocation.getFeatures().isEmpty()){
			RoomFeature fromFeature = null;
			GlobalRoomFeature toGlobalFeature = null;
			RoomFeature toFeature = null;
			boolean rollGlobalFeaturesFromFromLocation = true;
			if (toLocation instanceof Room) {
				Room toRoom = (Room) toLocation;
				if (toRoom.getExternalUniqueId() != null){
					ExternalRoom er = ExternalRoom.findExternalRoomForSession(toRoom.getExternalUniqueId(), toSession);
					if (er != null){
						rollGlobalFeaturesFromFromLocation = false;
						if (er.getRoomFeatures() != null){
							ExternalRoomFeature erf = null;
							for (Iterator erfIt = er.getRoomFeatures().iterator(); erfIt.hasNext();){
								erf = (ExternalRoomFeature) erfIt.next();
								toGlobalFeature = GlobalRoomFeature.findGlobalRoomFeatureForLabel(toSession, erf.getValue());
								toLocation.addTofeatures(toGlobalFeature);
							}
						}
					}
				}
			}
			for(Iterator rfIt = fromLocation.getFeatures().iterator(); rfIt.hasNext();){
				fromFeature = (RoomFeature) rfIt.next();
				if (fromFeature instanceof GlobalRoomFeature && !rollGlobalFeaturesFromFromLocation) continue;
				toFeature = (RoomFeature) roomFeatureCache.get(fromFeature);
				if (toFeature == null){
					toFeature = fromFeature.findSameFeatureInSession(toSession);
					if (toFeature != null){
						roomFeatureCache.put(fromFeature, toFeature);
						toLocation.addTofeatures(toFeature);
						if (toFeature.getRooms() == null){
							toFeature.setRooms(new java.util.HashSet());
						}
						toFeature.getRooms().add(toLocation);
					}
				}
			}
		}
	}
	
	private void rollRoomForward(RollForwardErrors errors, Session fromSession, Session toSession, Location location) {
		Room fromRoom = (Room) location;
		Room toRoom = null;
		DepartmentDAO dDao = DepartmentDAO.getInstance();
		Building toBuilding = null;
		RoomDept fromRoomDept = null;
		Department toDept = null;
		Department fromDept = null;
		HashMap roomFeatureCache = new HashMap();
		HashMap roomGroupCache = new HashMap();
		
		if (fromRoom.getExternalUniqueId() != null && sessionHasExternalRoomList(toSession)){
			ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), toSession);
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
		toRoom.setSession(toSession);
		if (fromRoom.getEventDepartment() != null) {
			toRoom.setEventDepartment(fromRoom.getEventDepartment().findSameDepartmentInSession(toSession));
		}
		toBuilding = fromRoom.getBuilding().findSameBuildingInSession(toSession);
		if (toBuilding != null) {
			toRoom.setBuilding(toBuilding);
			if (fromRoom.getManagerIds() != null && fromRoom.getManagerIds().length() != 0){
				String toManagerStr = "";
				for (StringTokenizer stk = new StringTokenizer(fromRoom.getManagerIds(),",");stk.hasMoreTokens();) {
					Long fromDeptId = Long.valueOf(stk.nextToken());
					if (fromDeptId != null){
						fromDept = dDao.get(fromDeptId);
						if (fromDept != null){
							toDept = fromDept.findSameDepartmentInSession(toSession);
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
			rollRoomFeaturesForLocationForward(fromRoom, toRoom, toSession, roomFeatureCache);
			rollRoomGroupsForLocationForward(fromRoom, toRoom, toSession, roomGroupCache);
			getHibSession().persist(toRoom);
			boolean rollForwardExistingRoomDepts = true;
			if (fromRoom.getExternalUniqueId() != null && sessionHasExternalRoomDeptList(toSession)){
				ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), toSession);
				if (toExternalRoom.getRoomDepartments() != null && !toExternalRoom.getRoomDepartments().isEmpty()){
					ExternalRoomDepartment toExternalRoomDept = null;
					fromRoomDept = null;
					for(Iterator erdIt = toExternalRoom.getRoomDepartments().iterator(); (erdIt.hasNext() && rollForwardExistingRoomDepts);){
						boolean foundDept = false;
						toExternalRoomDept = (ExternalRoomDepartment) erdIt.next();
						for(Iterator rdIt = fromRoom.getRoomDepts().iterator(); (rdIt.hasNext() && !foundDept);){
							fromRoomDept = (RoomDept) rdIt.next();
							if (fromRoomDept.getDepartment().getDeptCode().equals(toExternalRoomDept.getDepartmentCode())){
								foundDept = true;
							}
						}
						if (!foundDept){
							rollForwardExistingRoomDepts = false;
						}
					}
				}
			} 
			if (rollForwardExistingRoomDepts){
				if (fromRoom.getRoomDepts() != null && !fromRoom.getRoomDepts().isEmpty()){
					for (Iterator deptIt = fromRoom.getRoomDepts().iterator(); deptIt.hasNext();){
						fromRoomDept = (RoomDept)deptIt.next();
						rollForwardRoomDept(fromRoomDept, toRoom, toSession, fromRoom);
					}
				}
			} else {
				// resetting department sharing related fields
				toRoom.setPattern(null);
				toRoom.setManagerIds(null);
				ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), toSession);
				ExternalRoomDepartment toExternalRoomDept = null;
				fromRoomDept = null;
				for(Iterator erdIt = toExternalRoom.getRoomDepartments().iterator(); erdIt.hasNext();){
					boolean foundDept = false;
					toExternalRoomDept = (ExternalRoomDepartment) erdIt.next();
					for(Iterator rdIt = fromRoom.getRoomDepts().iterator(); (rdIt.hasNext() && !foundDept);){
						fromRoomDept = (RoomDept) rdIt.next();
						if (fromRoomDept.getDepartment().getDeptCode().equals(toExternalRoomDept.getDepartmentCode())){
							foundDept = true;
						}
					}
					if (foundDept){
						rollForwardRoomDept(fromRoomDept, toRoom, toSession, fromRoom);
					} else {
						toRoom.addExternalRoomDept(toExternalRoomDept, toExternalRoom.getRoomDepartments());
					}
				}
			}
			getHibSession().merge(toRoom);
			
			for (RoomPicture fromPicture: fromRoom.getPictures()) {
				RoomPicture toPicture = fromPicture.clonePicture();
				toPicture.setLocation(toRoom);
				toRoom.addToPictures(toPicture);
				getHibSession().persist(toPicture);
			}
			
			for (EventServiceProvider fromProvider: fromRoom.getAllowedServices()) {
				EventServiceProvider toProvider = fromProvider.findInSession(toSession.getUniqueId());
				if (toProvider != null)
					toRoom.addToAllowedServices(toProvider);
			}
		}
	}

	private void rollForwardRoomDept(RoomDept fromRoomDept, Location toLocation, Session toSession, Location fromLocation){		
		Department toDept = fromRoomDept.getDepartment().findSameDepartmentInSession(toSession);
		RoomDept toRoomDept = null;
		if (toDept != null){
			toRoomDept = new RoomDept();
			toRoomDept.setRoom(toLocation);
			toRoomDept.setControl(fromRoomDept.isControl());
			toRoomDept.setDepartment(toDept);
			toLocation.addToRoomDepts(toRoomDept);
			toDept.addToRoomDepts(toRoomDept);
			getHibSession().persist(toRoomDept);
			PreferenceLevel fromRoomPrefLevel = fromLocation.getRoomPreferenceLevel(fromRoomDept.getDepartment());
			if (!fromRoomPrefLevel.getPrefProlog().equals(PreferenceLevel.sNeutral)){
				RoomPref toRoomPref = new RoomPref();
				toRoomPref.setOwner(toDept);
				toRoomPref.setPrefLevel(fromRoomPrefLevel);
				toRoomPref.setRoom(toLocation);
				toDept.addToPreferences(toRoomPref);
				getHibSession().persist(toDept);
			}
		}
	}

	
	private void rollRoomGroupsForLocationForward(Location fromLocation, Location toLocation, Session toSession, HashMap roomGroupCache) {
		if(fromLocation.getRoomGroups() != null && !fromLocation.getRoomGroups().isEmpty()){
			RoomGroup fromRoomGroup = null;
			RoomGroup toRoomGroup = null;
			for(Iterator rfIt = fromLocation.getRoomGroups().iterator(); rfIt.hasNext();){
				fromRoomGroup = (RoomGroup) rfIt.next();
				toRoomGroup = (RoomGroup) roomGroupCache.get(fromRoomGroup);
				if (toRoomGroup == null)
					toRoomGroup = fromRoomGroup.findSameRoomGroupInSession(toSession);
				if (toRoomGroup != null) {
					roomGroupCache.put(fromRoomGroup, toRoomGroup);
					if (toLocation.getRoomGroups() == null)
						toLocation.setRoomGroups(new java.util.HashSet());
					toLocation.getRoomGroups().add(toRoomGroup);
					if (toRoomGroup.getRooms() == null)
						toRoomGroup.setRooms(new java.util.HashSet());
					toRoomGroup.getRooms().add(toLocation);
				}
			}
		}
		
	}

	private void rollNonUniversityLocationsForward(RollForwardErrors errors, Session fromSession, Session toSession, Location location) {
		NonUniversityLocation fromNonUniversityLocation = (NonUniversityLocation) location;
		NonUniversityLocation toNonUniversityLocation = null;
		DepartmentDAO dDao = DepartmentDAO.getInstance();
		RoomDept fromRoomDept = null;
		Department toDept = null;
		Department fromDept = null;
		HashMap roomFeatureCache = new HashMap();
		HashMap roomGroupCache = new HashMap();
		toNonUniversityLocation = (NonUniversityLocation)fromNonUniversityLocation.clone();
		toNonUniversityLocation.setSession(toSession);
		if (fromNonUniversityLocation.getEventDepartment() != null) {
			toNonUniversityLocation.setEventDepartment(fromNonUniversityLocation.getEventDepartment().findSameDepartmentInSession(toSession));
		}
		if (fromNonUniversityLocation.getManagerIds() != null && fromNonUniversityLocation.getManagerIds().length() != 0){
			String toManagerStr = "";
			for (StringTokenizer stk = new StringTokenizer(fromNonUniversityLocation.getManagerIds(),",");stk.hasMoreTokens();) {
				Long fromDeptId = Long.valueOf(stk.nextToken());
				if (fromDeptId != null){
					fromDept = dDao.get(fromDeptId);
					if (fromDept != null){
						toDept = fromDept.findSameDepartmentInSession(toSession);
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
		rollRoomFeaturesForLocationForward(fromNonUniversityLocation, toNonUniversityLocation, toSession, roomFeatureCache);
		rollRoomGroupsForLocationForward(fromNonUniversityLocation, toNonUniversityLocation, toSession, roomGroupCache);
		getHibSession().persist(toNonUniversityLocation);
		
		for (NonUniversityLocationPicture fromPicture: fromNonUniversityLocation.getPictures()) {
			NonUniversityLocationPicture toPicture = fromPicture.clonePicture();
			toPicture.setLocation(toNonUniversityLocation);
			toNonUniversityLocation.addToPictures(toPicture);
			getHibSession().persist(toPicture);
		}
		
		for (EventServiceProvider fromProvider: fromNonUniversityLocation.getAllowedServices()) {
			EventServiceProvider toProvider = fromProvider.findInSession(toSession.getUniqueId());
			if (toProvider != null)
				toNonUniversityLocation.addToAllowedServices(toProvider);
		}

		if (fromNonUniversityLocation.getRoomDepts() != null && !fromNonUniversityLocation.getRoomDepts().isEmpty()){
			for (Iterator deptIt = fromNonUniversityLocation.getRoomDepts().iterator(); deptIt.hasNext();){
				fromRoomDept = (RoomDept)deptIt.next();
				rollForwardRoomDept(fromRoomDept, toNonUniversityLocation, toSession, fromNonUniversityLocation);
			}
			getHibSession().merge(toNonUniversityLocation);
		}	
	}
	

	private void rollLocationsForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		if (fromSession.getRooms() != null && !fromSession.getRooms().isEmpty()){
			Location location = null;
			for (Iterator it = fromSession.getRooms().iterator(); it.hasNext();){
				location = (Location) it.next();
				if (location instanceof Room) {
					rollRoomForward(errors, fromSession, toSession, location);
				} else if (location instanceof NonUniversityLocation){
					rollNonUniversityLocationsForward(errors, fromSession, toSession, location);
				}
			}
		}
		if (sessionHasExternalRoomList(toSession)){
			Room.addNewExternalRoomsToSession(toSession);
		}
		getHibSession().flush();
	}
	
	private void rollTravelTimesForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		for (TravelTime time: getHibSession().createQuery(
				"from TravelTime where session.uniqueId = :sessionId", TravelTime.class)
				.setParameter("sessionId", toSession.getUniqueId())
				.list()) {
			getHibSession().remove(time);
		}
		for (TravelTime travel: getHibSession().createQuery(
    			"from TravelTime where session.uniqueId = :sessionId", TravelTime.class)
    			.setParameter("sessionId", fromSession.getUniqueId()).list()) {
			Location from = findLocation(travel.getLocation1Id(), toSession.getUniqueId());
			if (from == null) continue;
			Location to = findLocation(travel.getLocation2Id(), toSession.getUniqueId());
			if (to == null) continue;
			
			TravelTime time = new TravelTime();
			time.setSession(toSession);
			time.setLocation1Id(Math.min(from.getUniqueId(), to.getUniqueId()));
			time.setLocation2Id(Math.max(from.getUniqueId(), to.getUniqueId()));
			time.setDistance(travel.getDistance());
			
			getHibSession().persist(time);
		}
		getHibSession().flush();
	}

	private void rollRoomPartitionsForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		for (Room original: getHibSession().createQuery(
    			"from Room where session.uniqueId = :sessionId and parentRoom is not null", Room.class)
    			.setParameter("sessionId", fromSession.getUniqueId()).list()) {
			Room room = findRoom(original.getUniqueId(), toSession.getUniqueId());
			if (room == null) continue;
			Room parent = findRoom(original.getParentRoom().getUniqueId(), toSession.getUniqueId());
			if (parent == null) continue;
			
			room.setParentRoom(parent);
			
			getHibSession().merge(room);
		}
		getHibSession().flush();
	}

	private Location findLocation(Long locationId, Long sessionId) {
		Room room = getHibSession().createQuery(
				"select r2 from Room r1, Room r2 where r1.uniqueId = :locationId and r2.building.session.uniqueId=:sessionId and " +
				"((r1.externalUniqueId is not null and length(r1.externalUniqueId) > 0 and r1.externalUniqueId = r2.externalUniqueId) or " +
				"((r1.externalUniqueId is null or length(r1.externalUniqueId) = 0) and r1.building.abbreviation = r2.building.abbreviation and r1.roomNumber = r2.roomNumber))", Room.class)
				.setParameter("sessionId", sessionId)
				.setParameter("locationId", locationId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
		
		if (room != null) return room;
				
		return getHibSession().createQuery(
				"select r2 from NonUniversityLocation r1, NonUniversityLocation r2 where r1.uniqueId = :locationId and r2.session.uniqueId=:sessionId "
				+"and r1.name = r2.name", NonUniversityLocation.class)
				.setParameter("sessionId", sessionId)
				.setParameter("locationId", locationId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}
	
	private Room findRoom(Long locationId, Long sessionId) {
		return getHibSession().createQuery(
				"select r2 from Room r1, Room r2 where r1.uniqueId = :locationId and r2.building.session.uniqueId=:sessionId and " +
				"((r1.externalUniqueId is not null and length(r1.externalUniqueId) > 0 and r1.externalUniqueId = r2.externalUniqueId) or " +
				"((r1.externalUniqueId is null or length(r1.externalUniqueId) = 0) and r1.building.abbreviation = r2.building.abbreviation and r1.roomNumber = r2.roomNumber))", Room.class)
				.setParameter("sessionId", sessionId)
				.setParameter("locationId", locationId)
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
	}


	private void rollBuildingsForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		if (fromSession.getBuildings() != null && !fromSession.getBuildings().isEmpty()){
			Building fromBldg = null;
			Building toBldg = null;
			ExternalBuilding toExternalBuilding = null;
			for (Iterator it = fromSession.getBuildings().iterator(); it.hasNext();){
				fromBldg = (Building)it.next();
				if (fromBldg.getExternalUniqueId() != null && sessionHasExternalBuildingList(toSession)){
					toExternalBuilding = ExternalBuilding.findExternalBuildingForSession(fromBldg.getExternalUniqueId(), toSession);
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
				if (toSession.getBuildings() == null){
					toSession.setBuildings(new java.util.HashSet());
				}
				toBldg.setSession(toSession);
				toSession.getBuildings().add(toBldg);
				getHibSession().persist(toBldg);
			}				
		}
		getHibSession().flush();
	}

	public void rollManagersForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollManagersForwardFrom());
		Department fromDepartment = null;
		Department toDepartment = null;
		for(Iterator it = fromSession.getDepartments().iterator(); it.hasNext();){
			fromDepartment = (Department) it.next();
			if (fromDepartment != null && fromDepartment.getTimetableManagers() != null){
				toDepartment = fromDepartment.findSameDepartmentInSession(toSession);
				if (toDepartment != null){
					if (toDepartment.getTimetableManagers() == null){
						toDepartment.setTimetableManagers(new java.util.HashSet());
					}
					TimetableManager tm = null;
					for (Iterator tmIt = fromDepartment.getTimetableManagers().iterator(); tmIt.hasNext();){
						tm = (TimetableManager) tmIt.next();
						if (tm != null){
							toDepartment.getTimetableManagers().add(tm);
							tm.getDepartments().add(toDepartment);
							getHibSession().merge(tm);
							if (tm.getSolverGroups(toSession).isEmpty()){
								for(Iterator sgIt = tm.getSolverGroups(fromSession).iterator(); sgIt.hasNext();){
									SolverGroup fromSg = (SolverGroup) sgIt.next();
									SolverGroup toSg = SolverGroup.findBySessionIdAbbv(toSession.getUniqueId(), fromSg.getAbbv());
									if (toSg != null && !tm.getSolverGroups().contains(toSg)){
										toSg.addToTimetableManagers(tm);
										tm.addToSolverGroups(toSg);
									}
								}
							}
						}
					}
				}
			}
		}
		getHibSession().flush();
	}

	public void rollDepartmentsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDeptsFowardFrom());
		Department fromDepartment = null;
		Department toDepartment = null;
		for(Iterator it = fromSession.getDepartments().iterator(); it.hasNext();){
			fromDepartment = (Department) it.next();
			if (fromDepartment != null){
				toDepartment = (Department) fromDepartment.clone();
				toDepartment.setStatusType(null);
				toDepartment.setSession(toSession);
				toSession.addToDepartments(toDepartment);
				getHibSession().persist(toDepartment);
				if(fromDepartment.getSolverGroup() != null) {
					SolverGroup sg = SolverGroup.findBySessionIdName(toSession.getUniqueId(), fromDepartment.getSolverGroup().getName());
					if (sg == null){
						sg = (SolverGroup)fromDepartment.getSolverGroup().clone();
						sg.setSession(toSession);
					}
					if (sg != null){
						if (null == sg.getDepartments()){
							sg.setDepartments(new java.util.HashSet());
						}
						sg.getDepartments().add(toDepartment);
						toDepartment.setSolverGroup(sg);
						if (sg.getUniqueId() == null)
							getHibSession().persist(sg);
						else
							getHibSession().merge(sg);
					}
				}

				getHibSession().merge(toDepartment);
				List l = getHibSession().createQuery("select dt from DistributionType dt inner join dt.departments as d where d.uniqueId = " + fromDepartment.getUniqueId().toString(), DistributionType.class).list();
				if (l != null && !l.isEmpty()){
					DistributionType distributionType = null;
					for (Iterator dtIt = l.iterator(); dtIt.hasNext();){
						distributionType = (DistributionType) dtIt.next();
						distributionType.getDepartments().add(toDepartment);
						getHibSession().merge(distributionType);
					}
				}
			}
		}
		getHibSession().flush();
	}
	
	private void rollDatePatternOntoDepartments(DatePattern fromDatePattern, DatePattern toDatePattern){
		if (fromDatePattern.getDepartments() != null && !fromDatePattern.getDepartments().isEmpty()){
			for(Department fromDept : fromDatePattern.getDepartments()){
				Department toDepartment = Department.findByDeptCode(fromDept.getDeptCode(), toDatePattern.getSession().getSessionId());
				if (toDepartment != null){
					if (null == toDepartment.getDatePatterns()){
						toDepartment.setDatePatterns(new java.util.HashSet());
					}
					toDepartment.getDatePatterns().add(toDatePattern);
					if (null == toDatePattern.getDepartments()){
						toDatePattern.setDepartments(new java.util.HashSet());
					}
					toDatePattern.addToDepartments(toDepartment);
				}
			}
		}		
	}

	public void rollDatePatternsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDatePatternsForwardFrom());
		List<DatePattern> fromDatePatterns = DatePattern.findAll(fromSession, true, null, null);
		DatePattern fromDatePattern = null;
		DatePattern toDatePattern = null;
		HashMap<DatePattern, DatePattern> fromToDatePatternMap = new HashMap<DatePattern, DatePattern>();
		for(Iterator it = fromDatePatterns.iterator(); it.hasNext();){
			fromDatePattern = (DatePattern) it.next();
			if (fromDatePattern != null){
				toDatePattern = (DatePattern) fromDatePattern.clone();
				toDatePattern.setSession(toSession);
				rollDatePatternOntoDepartments(fromDatePattern, toDatePattern);
				getHibSession().persist(toDatePattern);
				fromToDatePatternMap.put(fromDatePattern, toDatePattern);
			}
		}
		
		for (DatePattern fromDp: fromToDatePatternMap.keySet()){
			DatePattern toDp = fromToDatePatternMap.get(fromDp);
			if (fromDp.getParents() != null && !fromDp.getParents().isEmpty()){
				for (DatePattern fromParent: fromDp.getParents()){
					DatePattern toParent = fromToDatePatternMap.get(fromParent);
					if (toParent != null) {
						toDp.addToParents(toParent);
						toParent.addToChildren(toDp);
					}
				}
				getHibSession().merge(toDp);
			}
		}
		
		if (fromSession.getDefaultDatePattern() != null){
			DatePattern defDp = DatePattern.findByName(toSession, fromSession.getDefaultDatePattern().getName());
			if (defDp != null){
				toSession.setDefaultDatePattern(defDp);
				getHibSession().merge(toSession);
			}
		}
		getHibSession().flush();
	}

	public void rollSubjectAreasForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollSubjectAreasForwardFrom());
		SubjectArea toSubjectArea = null;
		SubjectArea fromSubjectArea = null;
		Department toDepartment = null;
		if (sessionHasCourseCatalog(toSession)) {
			List<Object[]> subjects = getHibSession().createQuery(
					"select distinct cc.subject, cc.previousSubject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject is not null",
					Object[].class).setParameter("sessionId", toSession.getUniqueId()).list();
			if (subjects != null){
				String toSubject = null;
				String fromSubject = null;
				Object[] subjectInfo = null;
				for (Iterator<Object[]> saIt = subjects.iterator(); saIt.hasNext();){
					subjectInfo = saIt.next();
					if (subjectInfo != null && subjectInfo.length == 2){
						toSubject = (String) subjectInfo[0];
						fromSubject = (String) subjectInfo[1];							
						fromSubjectArea = SubjectArea.findByAbbv(fromSession.getUniqueId(), fromSubject);
						if (fromSubjectArea == null){
							continue;
						}
						toSubjectArea = (SubjectArea)fromSubjectArea.clone();
						toSubjectArea.setDepartment(null);
						if (!toSubject.equals(fromSubject)){
							toSubjectArea.setSubjectAreaAbbreviation(toSubject);
						}
						if (fromSubjectArea.getFundingDept() != null){
							Department toFundingDept = fromSubjectArea.getFundingDept().findSameDepartmentInSession(toSession);
							toSubjectArea.setFundingDept(toFundingDept);
						}
						toSubjectArea.setSession(toSession);
						toSession.addToSubjectAreas(toSubjectArea);
						if (fromSubjectArea.getDepartment() != null) {
							toDepartment = fromSubjectArea.getDepartment().findSameDepartmentInSession(toSession);
							if (toDepartment != null){
								toSubjectArea.setDepartment(toDepartment);
								toDepartment.addToSubjectAreas(toSubjectArea);
								getHibSession().persist(toSubjectArea);
							}
						}
						
					}
				}
			}
			List<String> newSubjects = getHibSession().createQuery(
					"select distinct subject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject is null and cc.subject not in (select sa.subjectAreaAbbreviation from SubjectArea sa where sa.session.uniqueId=:sessionId)",
					String.class).setParameter("sessionId", toSession.getUniqueId()).list();
			toDepartment = Department.findByDeptCode("TEMP", toSession.getUniqueId());
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
				toDepartment.setSession(toSession);
				toDepartment.setDistributionPrefPriority(Integer.valueOf(0));
				toDepartment.setInheritInstructorPreferences(true);
				toDepartment.setAllowEvents(false);
				toDepartment.setAllowStudentScheduling(true);
				toSession.addToDepartments(toDepartment);
				getHibSession().persist(toDepartment);
			}
			String toSubject = null;
			for (Iterator<String> saIt = newSubjects.iterator(); saIt.hasNext();){
				toSubject = saIt.next();
				if (toSubject != null){
					toSubjectArea = new SubjectArea();
					toSubjectArea.setDepartment(toDepartment);
					toSubjectArea.setTitle("New Subject");
					toSubjectArea.setSession(toSession);
					toSubjectArea.setSubjectAreaAbbreviation(toSubject);
					toDepartment.addToSubjectAreas(toSubjectArea);
					toSession.addToSubjectAreas(toSubjectArea);
					getHibSession().persist(toSubjectArea);
				}
			}
		} else if (fromSession.getSubjectAreas() != null && !fromSession.getSubjectAreas().isEmpty()){
			for(Iterator it = fromSession.getSubjectAreas().iterator(); it.hasNext();){
				fromSubjectArea = (SubjectArea) it.next();
				if (fromSubjectArea != null){
					toSubjectArea = (SubjectArea)fromSubjectArea.clone();
					if (fromSubjectArea.getFundingDept() != null){
						Department toFundingDept = fromSubjectArea.getFundingDept().findSameDepartmentInSession(toSession);
						toSubjectArea.setFundingDept(toFundingDept);
					}
					toSubjectArea.setDepartment(null);
					toSubjectArea.setSession(toSession);
					toSession.addToSubjectAreas(toSubjectArea);
					if (fromSubjectArea.getDepartment() != null) {
						toDepartment = fromSubjectArea.getDepartment().findSameDepartmentInSession(toSession);
						if (toDepartment != null){
							toSubjectArea.setDepartment(toDepartment);
							toDepartment.addToSubjectAreas(toSubjectArea);
							getHibSession().persist(toSubjectArea);
						}
					}
				}
			}
		}
		getHibSession().flush();
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

	private Department findToManagingDepartmentForPrefGroup(PreferenceGroup toPrefGroup, PreferenceGroup fromPrefGroup, Session toSession){
		Department toDepartment = findManagingDepartmentForPrefGroup(toPrefGroup);
		if (toDepartment == null){
			Department fromDepartment = findManagingDepartmentForPrefGroup(fromPrefGroup);
			if (fromDepartment != null){
				toDepartment = Department.findByDeptCode(fromDepartment.getDeptCode(), toSession.getUniqueId());
				return(toDepartment);
			}
		}
		
		return(toDepartment);
	}
	
	private void createToBuildingPref(BuildingPref fromBuildingPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession, Set locations, boolean isExamPref) {
		if (fromPrefGroup instanceof Class_ && !isClassRollForward()) return;
		BuildingPref toBuildingPref = null;
		Building toBuilding = fromBuildingPref.getBuilding().findSameBuildingInSession(toSession);
		if (toBuilding != null){
			boolean deptHasRoomInBuilding = false;
			if(!isExamPref){
				Location loc = null;
				Room r = null;
				Iterator rIt = locations.iterator();
				while(rIt.hasNext() && !deptHasRoomInBuilding){
					loc = (Location)rIt.next();
					if (loc instanceof Room) {
						r = (Room) loc;
						if (r.getBuilding() != null && r.getBuilding().getUniqueId().equals(toBuilding.getUniqueId())){
							deptHasRoomInBuilding = true;
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
	protected void rollForwardBuildingPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession) {
		Set locations = null;
		boolean isExamPref = false;
		if (fromPrefGroup instanceof Exam) {
			isExamPref = true;
		}
		if (fromPrefGroup.getBuildingPreferences() != null 
				&& !fromPrefGroup.getBuildingPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartLocationRollForward())){
			locations = getLocationsFor(fromPrefGroup, toPrefGroup, toSession);
			if (!isExamPref && locations == null){
				return;
			}
			for (Iterator it = fromPrefGroup.getBuildingPreferences().iterator(); it.hasNext(); ){
				createToBuildingPref((BuildingPref) it.next(), fromPrefGroup, toPrefGroup, toSession, locations, isExamPref);
			}
		}		
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp() && (toPrefGroup.getBuildingPreferences() == null || toPrefGroup.getBuildingPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (locations == null){
				locations = getLocationsFor(fromPrefGroup, toPrefGroup, toSession);
			}
			if (locations != null && locations.size() >0  && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, BuildingPref> prefMap = new HashMap<String, BuildingPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
						createToBuildingPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, toSession, locations, isExamPref);
					}
				}
			}				
		}
	}
	
	private void createToRoomPref(RoomPref fromRoomPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession, Set locations){
		if (fromPrefGroup instanceof Class_ && !isClassRollForward()) return;
		RoomPref toRoomPref = new RoomPref();
		if (fromRoomPref.getRoom() instanceof Room) {
			Room fromRoom = (Room) fromRoomPref.getRoom();
			Location loc = null;
			Room toRoom = null;
			for (Iterator rmIt = locations.iterator(); rmIt.hasNext();){
				loc = (Location) rmIt.next();
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
			Location loc = null;
			NonUniversityLocation toNonUniversityLocation = null;
			for (Iterator rmIt = locations.iterator(); rmIt.hasNext();){
				loc = (Location) rmIt.next();
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
	
	private Set getLocationsFor(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup instanceof Exam) {
			Exam exam = (Exam)fromPrefGroup;
			if (!getRoomList().containsKey(exam.getExamType()))
				getRoomList().put(exam.getExamType(), Location.findAllExamLocations(toSession.getUniqueId(), exam.getExamType()));
			return (Set)getRoomList().get(exam.getExamType());
		}
		Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
		if (toDepartment == null){
			return(null);
		}
		if (!getRoomList().containsKey(toDepartment)){
			getRoomList().put(toDepartment, buildRoomListForDepartment(getHibSession(), toDepartment, toSession));
		} 
		return ((Set)getRoomList().get(toDepartment));
	}
	protected void rollForwardRoomPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		Set locations = null;
		if (fromPrefGroup.getRoomPreferences() != null 
				&& !fromPrefGroup.getRoomPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartLocationRollForward())){
			locations = getLocationsFor(fromPrefGroup, toPrefGroup, toSession);
			if (locations != null && locations.size() >0 ){					
				for (Iterator it = fromPrefGroup.getRoomPreferences().iterator(); it.hasNext();){
					createToRoomPref((RoomPref) it.next(), fromPrefGroup, toPrefGroup, toSession, locations);
				}
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp() && (toPrefGroup.getRoomPreferences() == null || toPrefGroup.getRoomPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (locations == null){
				locations = getLocationsFor(fromPrefGroup, toPrefGroup, toSession);
			}
			if (locations != null && locations.size() >0  && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomPref> prefMap = new HashMap<String, RoomPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
						createToRoomPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, toSession, locations);
					}
				}
			}				
		}
	}
	
	protected void rollForwardPeriodPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		for (Iterator<ExamPeriodPref> i = fromPrefGroup.getExamPeriodPreferences().iterator(); i.hasNext(); ) {
			ExamPeriodPref fromPref = i.next();
			ExamPeriod toPeriod = fromPref.getExamPeriod().findSameExamPeriodInSession(toSession);
			if (toPeriod != null) {
				ExamPeriodPref toPref = new ExamPeriodPref();
				toPref.setExamPeriod(toPeriod);
				toPref.setOwner(toPrefGroup);
				toPref.setPrefLevel(fromPref.getPrefLevel());
				toPrefGroup.addToPreferences(toPref);
			}
		}
	}
	
	private void createToRoomFeaturePref(RoomFeaturePref fromRoomFeaturePref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup instanceof Class_ && !isClassRollForward()) return;
		RoomFeaturePref toRoomFeaturePref = new RoomFeaturePref();
		if (fromRoomFeaturePref.getRoomFeature() instanceof GlobalRoomFeature) {
			GlobalRoomFeature grf = GlobalRoomFeature.findGlobalRoomFeatureForLabel(toSession, fromRoomFeaturePref.getRoomFeature().getLabel());
			if (grf != null) {
				toRoomFeaturePref.setRoomFeature(grf);
				toRoomFeaturePref.setPrefLevel(fromRoomFeaturePref.getPrefLevel());
				toRoomFeaturePref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomFeaturePref);
			}
		} else {
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
			if (toDepartment == null){
				return;
			}
			Collection l = DepartmentRoomFeature.getAllDepartmentRoomFeatures(toDepartment);
			DepartmentRoomFeature fromDepartmentRoomFeature = (DepartmentRoomFeature) fromRoomFeaturePref.getRoomFeature();
			if (l != null && l.size() > 0){
				DepartmentRoomFeature toDepartmentRoomFeature = null;
				for (Iterator rfIt = l.iterator(); rfIt.hasNext();){
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
	
	protected void rollForwardRoomFeaturePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getRoomFeaturePreferences() != null 
				&& !fromPrefGroup.getRoomFeaturePreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartLocationRollForward())){
			for (Iterator it = fromPrefGroup.getRoomFeaturePreferences().iterator(); it.hasNext(); ){
				createToRoomFeaturePref((RoomFeaturePref) it.next(), fromPrefGroup, toPrefGroup, toSession);
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp() && (toPrefGroup.getRoomFeaturePreferences() == null || toPrefGroup.getRoomFeaturePreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomFeaturePref> prefMap = new HashMap<String, RoomFeaturePref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
						createToRoomFeaturePref(prefMap.get(pref), fromPrefGroup, toPrefGroup, toSession);
					}
				}
			}				
		}
	}
	
	private void createToRoomGroupPref(RoomGroupPref fromRoomGroupPref, PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup instanceof Class_ && !isClassRollForward()) return;
		RoomGroupPref toRoomGroupPref = new RoomGroupPref();
		RoomGroup toDefaultRoomGroup = RoomGroup.getGlobalDefaultRoomGroup(toSession);
		if (fromRoomGroupPref.getRoomGroup().isDefaultGroup() && toDefaultRoomGroup != null){
			toRoomGroupPref.setRoomGroup(toDefaultRoomGroup);
			toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
			toRoomGroupPref.setOwner(toPrefGroup);
			toPrefGroup.addToPreferences(toRoomGroupPref);
		} else if (fromRoomGroupPref.getRoomGroup().isGlobal()) {
			RoomGroup toRoomGroup = RoomGroup.findGlobalRoomGroupForName(toSession, fromRoomGroupPref.getRoomGroup().getName());
			if (toRoomGroup != null) {
				toRoomGroupPref.setRoomGroup(toRoomGroup);
				toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
				toRoomGroupPref.setOwner(toPrefGroup);
				toPrefGroup.addToPreferences(toRoomGroupPref);
			}
		} else {
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
			if (toDepartment == null){
				return;
			}
			Collection l = RoomGroup.getAllDepartmentRoomGroups(toDepartment);
			if (l != null && l.size() > 0) {
				RoomGroup toRoomGroup = null;
				for (Iterator itRg = l.iterator(); itRg.hasNext();){
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
	
	protected void rollForwardRoomGroupPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getRoomGroupPreferences() != null 
				&& !fromPrefGroup.getRoomGroupPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartLocationRollForward())){
			for (Iterator it = fromPrefGroup.getRoomGroupPreferences().iterator(); it.hasNext();){
				createToRoomGroupPref((RoomGroupPref) it.next(), fromPrefGroup, toPrefGroup, toSession);
			}
		}
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp() && (toPrefGroup.getRoomGroupPreferences() == null || toPrefGroup.getRoomGroupPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, RoomGroupPref> prefMap = new HashMap<String, RoomGroupPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
						createToRoomGroupPref(prefMap.get(pref), fromPrefGroup, toPrefGroup, toSession);
					}
				}
			}				
		}
	}
	
	protected void rollForwardTimePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getTimePreferences() != null 
				&& !fromPrefGroup.getTimePreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartTimeRollForward())){
			TimePref fromTimePref = null;
			TimePref toTimePref = null;
			for (Iterator it = fromPrefGroup.getTimePreferences().iterator(); it.hasNext();){
				fromTimePref = (TimePref) it.next();
				if (fromTimePref.getTimePattern() == null) {
					toTimePref = (TimePref)fromTimePref.clone();
				} else {
					toTimePref = TimePattern.getMatchingTimePreference(toSession.getUniqueId(), fromTimePref);
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
		// If subpart time preferences are not to be rolled forward, make sure any subpart time patterns are rolled forward without their time preferences. 
		if (fromPrefGroup instanceof SchedulingSubpart && !isSubpartTimeRollForward()){
			TimePref fromTimePref = null;
			TimePref toTimePref = null;
			for (Iterator it = fromPrefGroup.getTimePreferences().iterator(); it.hasNext();){
				fromTimePref = (TimePref) it.next();
				if (fromTimePref.getTimePattern() == null) {
					toTimePref = (TimePref)fromTimePref.clone();
				} else {
					toTimePref = TimePattern.getMatchingTimePreference(toSession.getUniqueId(), fromTimePref);
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
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp()) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if ((ss.getTimePreferences() == null || ss.getTimePreferences().isEmpty()) && ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, TimePref> prefMap = new HashMap<String, TimePref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				HashSet<TimePattern> timePatterns = new HashSet<TimePattern>();
				String key;
				int clsCnt = 0;
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
							if (fromTimePref.getTimePattern().isExactTime()){
								continue;
							}
							toTimePref = TimePattern.getMatchingTimePreference(toSession.getUniqueId(), fromTimePref);
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
					if (fromTp.isExactTime()){
						continue;
					}			
					TimePattern toTp = TimePattern.getMatchingTimePattern(toSession.getUniqueId(), fromTp);
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
	
	protected void rollForwardDatePatternPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getDatePatternPreferences() != null 
				&& !fromPrefGroup.getDatePatternPreferences().isEmpty() 
				&& (!(fromPrefGroup instanceof Class_) || isClassRollForward())
				&& (!(fromPrefGroup instanceof SchedulingSubpart) || isSubpartTimeRollForward())){
			DatePatternPref fromDatePatternPref = null;
			DatePatternPref toDatePatternPref = null;
			for (Iterator it = fromPrefGroup.getDatePatternPreferences().iterator(); it.hasNext();){
				fromDatePatternPref = (DatePatternPref) it.next();
				DatePattern toDatePattern = DatePattern.findByName(toSession, fromDatePatternPref.getDatePattern().getName());
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
		if (fromPrefGroup instanceof SchedulingSubpart && isClassPrefsPushUp() && (toPrefGroup.getDatePatternPreferences() == null || toPrefGroup.getDatePatternPreferences().isEmpty())) {
			SchedulingSubpart ss = (SchedulingSubpart) fromPrefGroup;
			if (ss.getClasses() != null && !ss.getClasses().isEmpty()){
				HashMap<String, DatePatternPref> prefMap = new HashMap<String, DatePatternPref>();
				HashMap<String, Integer> prefCount = new HashMap<String, Integer>();
				String key;
				int clsCnt = 0;
				DatePattern firstDp = null; int dpCount = 0; 
				for (Iterator cIt = ss.getClasses().iterator(); cIt.hasNext();){
					Class_ c = (Class_)cIt.next();
					if (CancelledClassAction.SKIP == getCancelledClassAction() && c.isCancelled()) continue;
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
					DatePattern toDatePattern = DatePattern.findByName(toSession, firstDp.getName());
					if (toDatePattern == null){
						iLog.warn("To Date Pattern not found:  " + firstDp.getName() + " for " + fromPrefGroup.htmlLabel());
					} else {
						((SchedulingSubpart)toPrefGroup).setDatePattern(toDatePattern);
						for (Class_ c: ((SchedulingSubpart)toPrefGroup).getClasses()) {
							c.setDatePattern(null);
							getHibSession().merge(c);
						}
					}
				}
				for (String pref : prefCount.keySet()){
					if (prefCount.get(pref).intValue() == clsCnt){
						DatePatternPref fromDatePatternPref = prefMap.get(pref);
						DatePattern toDatePattern = DatePattern.findByName(toSession, fromDatePatternPref.getDatePattern().getName());
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

	protected void rollForwardDistributionPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getDistributionObjects() != null && !fromPrefGroup.getDistributionObjects().isEmpty()){
			DistributionObject fromDistObj = null;
			DistributionObject toDistObj = null;
			DistributionPref fromDistributionPref = null;
			DistributionPref toDistributionPref = null;
			for (Iterator it = fromPrefGroup.getDistributionObjects().iterator(); it.hasNext(); ){
				fromDistObj = (DistributionObject) it.next();
				toDistObj = new DistributionObject();
				fromDistributionPref = fromDistObj.getDistributionPref();
				if (!isRollForwardDistributions(fromDistributionPref)) continue;
				toDistributionPref = DistributionPref.findByIdRolledForwardFrom(fromDistributionPref.getUniqueId(), toSession.getUniqueId());
				if (toDistributionPref == null){
					toDistributionPref = new DistributionPref();
					toDistributionPref.setDistributionType(fromDistributionPref.getDistributionType());
					toDistributionPref.setGrouping(fromDistributionPref.getGrouping());
					toDistributionPref.setPrefLevel(fromDistributionPref.getPrefLevel());
					toDistributionPref.setUniqueIdRolledForwardFrom(fromDistributionPref.getUniqueId());
					Department toDept = Department.findByDeptCode(((Department)fromDistributionPref.getOwner()).getDeptCode(), toSession.getUniqueId());
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
					getHibSession().persist(toDistributionPref);
				else
					getHibSession().merge(toDistributionPref);
			}
		}		
	}
	
	protected void rollForwardInstructorCoursePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		Set preferences = fromPrefGroup.getPreferences(InstructorCoursePref.class);
		if (toPrefGroup.getPreferences() != null)
			for (Iterator<Preference> i = toPrefGroup.getPreferences().iterator(); i.hasNext(); ) {
				Preference p = i.next();
				if (p instanceof InstructorCoursePref) i.remove();
			}
		if (preferences != null && !preferences.isEmpty()) {
			for (Iterator it = preferences.iterator(); it.hasNext();){
				InstructorCoursePref fromCoursePref = (InstructorCoursePref) it.next();
				CourseOffering course = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromCoursePref.getCourse().getUniqueId());
				if (course != null) {
					InstructorCoursePref toCoursePref = (InstructorCoursePref) fromCoursePref.clone();
					toCoursePref.setCourse(course);
					toCoursePref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toCoursePref);
				}
			}
		}
	}
	
	private void rollGlobalInstructorAttributesForward(RollForwardErrors errors, Session fromSession, Session toSession) {
		Map<Long, InstructorAttribute> attributes = new HashMap<Long, InstructorAttribute>();
		for (InstructorAttribute oldAttribute: InstructorAttribute.getAllGlobalAttributes(toSession.getUniqueId())) {
			getHibSession().remove(oldAttribute);
		}
		List<InstructorAttribute> globalAttributes = InstructorAttribute.getAllGlobalAttributes(fromSession.getUniqueId());
		for (InstructorAttribute fromAttribute: globalAttributes) {
			InstructorAttribute toAttribute = new InstructorAttribute();
			toAttribute.setSession(toSession);
			toAttribute.setCode(fromAttribute.getCode());
			toAttribute.setName(fromAttribute.getName());
			toAttribute.setType(fromAttribute.getType());
			toAttribute.setInstructors(new HashSet<DepartmentalInstructor>());
			toAttribute.setChildAttributes(new HashSet<InstructorAttribute>());
			attributes.put(fromAttribute.getUniqueId(), toAttribute);
			getHibSession().persist(toAttribute);
		}
		for (InstructorAttribute fromChildAttribute: globalAttributes) {
			if (fromChildAttribute.getParentAttribute() != null) {
				InstructorAttribute toChildAttribute = attributes.get(fromChildAttribute.getUniqueId());
				InstructorAttribute toParentAttribute = attributes.get(fromChildAttribute.getParentAttribute().getUniqueId());
				if (toParentAttribute != null) {
					toChildAttribute.setParentAttribute(toParentAttribute);
					toParentAttribute.getChildAttributes().add(toChildAttribute);
					getHibSession().merge(toChildAttribute);
				}
			}
		}
		getHibSession().flush();
	}
	
	private void rollDepartmentalInstructorAttributesForward(RollForwardErrors errors, Department fromDepartment, Department toDepartment) {
		Map<Long, InstructorAttribute> attributes = new HashMap<Long, InstructorAttribute>();
		for (InstructorAttribute oldAttribute: InstructorAttribute.getAllDepartmentalAttributes(toDepartment.getUniqueId())) {
			getHibSession().remove(oldAttribute);
		}
		List<InstructorAttribute> departmentalAttributes = InstructorAttribute.getAllDepartmentalAttributes(fromDepartment.getUniqueId());
		for (InstructorAttribute fromAttribute: departmentalAttributes) {
			InstructorAttribute toAttribute = new InstructorAttribute();
			toAttribute.setSession(toDepartment.getSession());
			toAttribute.setDepartment(toDepartment);
			toAttribute.setCode(fromAttribute.getCode());
			toAttribute.setName(fromAttribute.getName());
			toAttribute.setType(fromAttribute.getType());
			toAttribute.setInstructors(new HashSet<DepartmentalInstructor>());
			toAttribute.setChildAttributes(new HashSet<InstructorAttribute>());
			attributes.put(fromAttribute.getUniqueId(), toAttribute);
			getHibSession().persist(toAttribute);
		}
		for (InstructorAttribute fromChildAttribute: departmentalAttributes) {
			if (fromChildAttribute.getParentAttribute() != null) {
				InstructorAttribute toChildAttribute = attributes.get(fromChildAttribute.getUniqueId());
				InstructorAttribute toParentAttribute = attributes.get(fromChildAttribute.getParentAttribute().getUniqueId());
				if (toParentAttribute != null) {
					toChildAttribute.setParentAttribute(toParentAttribute);
					toParentAttribute.getChildAttributes().add(toChildAttribute);
					getHibSession().merge(toChildAttribute);
				}
			}
		}
		getHibSession().flush();
	}
	
	protected void rollForwardInstructorAttributePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession) {
		Set preferences = fromPrefGroup.getPreferences(InstructorAttributePref.class);
		if (preferences != null && !preferences.isEmpty()) {
			for (Iterator it = preferences.iterator(); it.hasNext();){
				InstructorAttributePref fromAttributePref = (InstructorAttributePref) it.next();
				InstructorAttribute attribute = fromAttributePref.getAttribute().findSameAttributeInSession(toSession);
				if (attribute != null) {
					InstructorAttributePref toAttributePref = (InstructorAttributePref) fromAttributePref.clone();
					toAttributePref.setAttribute(attribute);
					toAttributePref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toAttributePref);
				}
			}
		}
	}
	
	protected void rollForwardInstructorPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession) {
		Set preferences = fromPrefGroup.getPreferences(InstructorPref.class);
		if (preferences != null && !preferences.isEmpty()) {
			for (Iterator it = preferences.iterator(); it.hasNext();){
				InstructorPref fromInstrPref = (InstructorPref) it.next();
				DepartmentalInstructor instructor = fromInstrPref.getInstructor().findThisInstructorInSession(toSession.getUniqueId());
				if (instructor != null) {
					InstructorPref toInstrPref = (InstructorPref) fromInstrPref.clone();
					toInstrPref.setInstructor(instructor);
					toInstrPref.setOwner(toPrefGroup);
					toPrefGroup.addToPreferences(toInstrPref);
				}
			}
		}
	}
	
	private void rollForwardExamPeriods(Session toSession, Session fromSession){	
		ExamPeriod fromExamPeriod = null;
		ExamPeriod toExamPeriod = null;
		TreeSet examPeriods = ExamPeriod.findAll(fromSession.getUniqueId(), (Long)null);
		for(Iterator examPeriodIt = examPeriods.iterator(); examPeriodIt.hasNext();){
			fromExamPeriod = (ExamPeriod)examPeriodIt.next();
			toExamPeriod = (ExamPeriod)fromExamPeriod.clone();
			toExamPeriod.setSession(toSession);
			if (toExamPeriod.getEventStartOffset()==null) toExamPeriod.setEventStartOffset(0);
			if (toExamPeriod.getEventStopOffset()==null) toExamPeriod.setEventStopOffset(0);
			getHibSession().persist(toExamPeriod);
		}
		getHibSession().flush();
	}
	
	private void rollForwardExamLocationPrefs(Session toSession, Session fromSession) {
		List<Room> rooms = getHibSession().createQuery(
				"select distinct r from Room r inner join r.examPreferences as ep where r.session.uniqueId = :sessionId", Room.class)
				.setParameter("sessionId", fromSession.getUniqueId().longValue()).list();
		Room fromRoom = null;
		Room toRoom = null;
		ExamLocationPref fromPref = null;
		ExamPeriod toPeriod = null;
		for (Iterator rIt = rooms.iterator(); rIt.hasNext();){
			fromRoom = (Room) rIt.next();
			toRoom = fromRoom.findSameRoomInSession(toSession);
			if (toRoom != null){
				for(Iterator elpIt = fromRoom.getExamPreferences().iterator(); elpIt.hasNext();){
					fromPref = (ExamLocationPref) elpIt.next();
					toPeriod = fromPref.getExamPeriod().findSameExamPeriodInSession(toSession);
					if (toPeriod != null){
						toRoom.addExamPreference(toPeriod, fromPref.getPrefLevel());
					}
				}
			}
		}
		List<NonUniversityLocation> nonUniversityLocations = (NonUniversityLocationDAO.getInstance()).getSession().createQuery(
				"select distinct nul from NonUniversityLocation nul inner join nul.examPreferences as ep where nul.session.uniqueId = :sessionId", NonUniversityLocation.class)
				.setParameter("sessionId", fromSession.getUniqueId().longValue()).list();
		NonUniversityLocation fromNonUniversityLocation = null;	
		NonUniversityLocation toNonUniversityLocation = null;
		for (Iterator nulIt = nonUniversityLocations.iterator(); nulIt.hasNext();){
			fromNonUniversityLocation = (NonUniversityLocation) nulIt.next();
			toNonUniversityLocation = fromNonUniversityLocation.findSameNonUniversityLocationInSession(toSession);
			if (toNonUniversityLocation != null){
				for(Iterator elpIt = fromNonUniversityLocation.getExamPreferences().iterator(); elpIt.hasNext();){
					fromPref = (ExamLocationPref) elpIt.next();
					toPeriod = fromPref.getExamPeriod().findSameExamPeriodInSession(toSession);
					if (toPeriod != null){
						toNonUniversityLocation.addExamPreference(toPeriod, fromPref.getPrefLevel());
					}
				}
			}
		}		
	}
	
	private void rollForwardExaminationManagers(Session toSession, Session fromSession) {
		Set<TimetableManager> managers = new HashSet<TimetableManager>(getHibSession().createQuery(
				"select distinct m from TimetableManager m inner join m.departments d inner join m.managerRoles mr " +
				"where d.session.uniqueId = :sessionId and mr.role.enabled = true "+
				"and :prmExMgr in elements(mr.role.rights) and :prmAdmin not in elements(mr.role.rights) " +
				"order by m.lastName, m.firstName", TimetableManager.class)
				.setParameter("sessionId", toSession.getUniqueId())
				.setParameter("prmExMgr", Right.ExaminationSolver.name())
				.setParameter("prmAdmin", Right.StatusIndependent.name())
				.list());
		List<ExamStatus> statuses = getHibSession().createQuery("from ExamStatus s where s.session.uniqueId = :sessionId", ExamStatus.class).setParameter("sessionId", fromSession.getUniqueId()).list();
		for (ExamStatus fromStatus: statuses) {
			ExamStatus toStatus = ExamStatus.findStatus(toSession.getUniqueId(), fromStatus.getType().getUniqueId());
			if (toStatus == null) {
				toStatus = new ExamStatus();
				toStatus.setSession(toSession);
				toStatus.setType(fromStatus.getType());
				toStatus.setManagers(new HashSet<TimetableManager>());
				getHibSession().persist(toStatus);
			} else {
				toStatus.getManagers().clear();
			}
			for (TimetableManager m: fromStatus.getManagers())
				if (managers.contains(m))
					toStatus.getManagers().add(m);
			getHibSession().merge(toStatus);
		}
	}
	
	private void rollForwardExam(Exam fromExam, Session toSession, String prefOption) {
		Exam toExam = new Exam();
		toExam.setExamType(fromExam.getExamType());
		toExam.setLength(fromExam.getLength());
		toExam.setMaxNbrRooms(fromExam.getMaxNbrRooms());
		toExam.setNote(fromExam.getNote());
		toExam.setSeatingType(fromExam.getSeatingType());
		toExam.setSession(toSession);
		toExam.setUniqueIdRolledForwardFrom(fromExam.getUniqueId());
		if (fromExam.getAveragePeriod() != null && fromExam.getAssignedPeriod() != null){
			toExam.setAvgPeriod(Integer.valueOf((fromExam.getAvgPeriod().intValue() + fromExam.getAssignedPeriod().getIndex())/2));
		} else if (fromExam.getAveragePeriod() != null){
			toExam.setAvgPeriod(fromExam.getAvgPeriod());
		} else if (fromExam.getAssignedPeriod() != null){
			toExam.setAvgPeriod(fromExam.getAssignedPeriod().getIndex());
		}
		for(Iterator oIt = fromExam.getOwners().iterator(); oIt.hasNext();){
			ExamOwner fromOwner = (ExamOwner) oIt.next();
			ExamOwner toOwner = new ExamOwner();
			if(fromOwner.getOwnerType().equals(ExamOwner.sOwnerTypeClass)){
				Class_ fromClass = (Class_)fromOwner.getOwnerObject();
				Class_ toClass = Class_.findByIdRolledForwardFrom(toSession.getUniqueId(), fromClass.getUniqueId());
				if (toClass != null){
					toOwner.setOwner(toClass);
				}
			} else if (fromOwner.getOwnerType().equals(ExamOwner.sOwnerTypeConfig)){
				InstrOfferingConfig fromIoc = (InstrOfferingConfig) fromOwner.getOwnerObject();
				InstrOfferingConfig toIoc = InstrOfferingConfig.findByIdRolledForwardFrom(toSession.getUniqueId(), fromIoc.getUniqueId());
				if (toIoc != null){
					toOwner.setOwner(toIoc);
				}
			} else if (fromOwner.getOwnerType().equals(ExamOwner.sOwnerTypeOffering)){
				InstructionalOffering fromIo = (InstructionalOffering) fromOwner.getOwnerObject();
				InstructionalOffering toIo = InstructionalOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromIo.getUniqueId());
				if (toIo != null){
					toOwner.setOwner(toIo);
				}
			} else if (fromOwner.getOwnerType().equals(ExamOwner.sOwnerTypeCourse)){
				CourseOffering fromCo = (CourseOffering) fromOwner.getOwnerObject();
				CourseOffering toCo = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromCo.getUniqueId());
				if (toCo != null){
					toOwner.setOwner(toCo);
				}
			}
			if (toOwner.getOwnerType() != null){
				toOwner.setExam(toExam);
				toExam.addToOwners(toOwner);
			}
		}
		if (toExam.getOwners() != null || toExam.getOwners().size() > 0){
			getHibSession().persist(toExam);
			if (EXAMS_ROOM_PREFS.equals(prefOption) || EXAMS_ALL_PREF.equals(prefOption)) {
				rollForwardBuildingPrefs(fromExam, toExam, toSession);
				rollForwardRoomGroupPrefs(fromExam, toExam, toSession);
				rollForwardRoomFeaturePrefs(fromExam, toExam, toSession);
			}
			if (EXAMS_ALL_PREF.equals(prefOption)) {
				rollForwardRoomPrefs(fromExam, toExam, toSession);
				rollForwardPeriodPrefs(fromExam, toExam, toSession);
			}
			getHibSession().merge(toExam);
		}
	}
	
	private List<Exam> findExamToRollForward(Session toSession, int examType){
		return(getHibSession().createQuery("select distinct e from ExamOwner as eo inner join eo.exam as e where e.examType.type = :examType " +
				" and ((eo.ownerType=:ownerTypeClass and eo.ownerId in (select c.uniqueIdRolledForwardFrom from Class_ as c where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = :toSessionId)) " +
				" or (eo.ownerType=:ownerTypeCourse and eo.ownerId in (select co.uniqueIdRolledForwardFrom from CourseOffering as co where co.subjectArea.session.uniqueId = :toSessionId)) " +
				" or (eo.ownerType=:ownerTypeOffering and eo.ownerId in (select io.uniqueIdRolledForwardFrom from InstructionalOffering as io where io.session.uniqueId = :toSessionId)) " +
				" or (eo.ownerType=:ownerTypeConfig and eo.ownerId in (select ioc.uniqueIdRolledForwardFrom from InstrOfferingConfig as ioc where ioc.instructionalOffering.session.uniqueId = :toSessionId)))",
				Exam.class)
				.setParameter("toSessionId", toSession.getUniqueId().longValue())
				.setParameter("examType", examType)
				.setParameter("ownerTypeClass", ExamOwner.sOwnerTypeClass)
				.setParameter("ownerTypeCourse", ExamOwner.sOwnerTypeCourse)
				.setParameter("ownerTypeOffering", ExamOwner.sOwnerTypeOffering)
				.setParameter("ownerTypeConfig", ExamOwner.sOwnerTypeConfig)
				.list());
	}
	
	public void rollMidtermExamsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm){
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		List<Exam> exams = findExamToRollForward(toSession, ExamType.sExamTypeMidterm);
		for (Exam exam: exams) {
			rollForwardExam(exam, toSession, rollForwardSessionForm.getMidtermExamsPrefsAction());
		}
		getHibSession().flush();
	}

	public void rollFinalExamsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm){
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		List<Exam> exams = findExamToRollForward(toSession, ExamType.sExamTypeFinal);
		for (Exam exam: exams) {
			rollForwardExam(exam, toSession, rollForwardSessionForm.getFinalExamsPrefsAction());
		}
		getHibSession().flush();
	}
	
	public void rollExamConfigurationDataForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm){
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollExamConfigurationForwardFrom());
		rollForwardExamPeriods(toSession, fromSession);
		rollForwardExamLocationPrefs(toSession, fromSession);
		rollForwardExaminationManagers(toSession, fromSession);
		getHibSession().flush();
	}
	
	protected void rollInstructorDistributionPrefs(DepartmentalInstructor fromInstructor, DepartmentalInstructor toInstructor){
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
	
	
	public void rollInstructorDataForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollInstructorDataForwardFrom());
		DepartmentalInstructor toInstructor = null;
		Department toDepartment = null;
		ArrayList<String> deptsToRollDataFor = new ArrayList<String>();
		for(String dept : rollForwardSessionForm.getRollForwardDepartmentIds()){
			deptsToRollDataFor.add(dept);
		}
		
		if (fromSession.getDepartments() != null){
			String existingQuery = "select di.department.deptCode || di.externalUniqueId from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId and di.externalUniqueId is not null";
			List<String> existingInstructors = getHibSession()
					.createQuery(existingQuery, String.class)
					.setParameter("sessionId", toSession.getUniqueId().longValue())
					.list();
			
			String existingNoExtIdQuery = "select di.department.deptCode || di.lastName || ',' || di.firstName || ',' || di.middleName from DepartmentalInstructor di where di.department.session.uniqueId = :sessionId and di.externalUniqueId is null";
			List<String> existingNoExtIdInstructors = getHibSession()
					.createQuery(existingNoExtIdQuery, String.class)
					.setParameter("sessionId", toSession.getUniqueId().longValue())
					.list();
			
			for(Department fromDepartment: fromSession.getDepartments()){
				if (fromDepartment != null && fromDepartment.getInstructors() != null && !fromDepartment.getInstructors().isEmpty()){
					toDepartment = fromDepartment.findSameDepartmentInSession(toSession);
					if (toDepartment != null && deptsToRollDataFor.contains(toDepartment.getUniqueId().toString())){
						iLog.info("Rolling instructors for " + fromDepartment);
						rollDepartmentalInstructorAttributesForward(errors, fromDepartment, toDepartment);
						for (DepartmentalInstructor fromInstructor : fromDepartment.getInstructors()){
							if (!(fromInstructor.getExternalUniqueId() == null) && !fromInstructor.getExternalUniqueId().isEmpty() && existingInstructors.contains(toDepartment.getDeptCode()+fromInstructor.getExternalUniqueId())){
								iLog.info(fromInstructor.toString() + ": already exists in term, not rolling forward");
								continue;
							}
							if ((fromInstructor.getExternalUniqueId() == null || fromInstructor.getExternalUniqueId().isEmpty()) && existingNoExtIdInstructors.contains(toDepartment.getDeptCode()+fromInstructor.getLastName()+","+fromInstructor.getFirstName()+","+fromInstructor.getMiddleName())){
								iLog.info(fromInstructor.toString() + ": already exists in term, not rolling forward");
								continue;
							}

							toInstructor = (DepartmentalInstructor) fromInstructor.clone();
							toInstructor.setDepartment(toDepartment);
							toInstructor.setAttributes(new HashSet<InstructorAttribute>());
							for (InstructorAttribute fromAttribute: fromInstructor.getAttributes()) {
								InstructorAttribute toAttribute = fromAttribute.findSameAttributeInSession(toSession);
								if (toAttribute != null) {
									toAttribute.addToInstructors(toInstructor);
									toInstructor.addToAttributes(toAttribute);
								}
							}
							rollForwardBuildingPrefs(fromInstructor, toInstructor, toSession);
							rollForwardRoomPrefs(fromInstructor, toInstructor, toSession);
							rollForwardRoomFeaturePrefs(fromInstructor, toInstructor, toSession);
							rollForwardRoomGroupPrefs(fromInstructor, toInstructor, toSession);
							rollForwardTimePrefs(fromInstructor, toInstructor, toSession);
							rollInstructorDistributionPrefs(fromInstructor, toInstructor);
							getHibSession().persist(toInstructor);
						}
					}
				}
			}
		}
		getHibSession().flush();
	}

	public void rollCourseOfferingsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) throws Exception {
		for (String subjectId: rollForwardSessionForm.getRollForwardSubjectAreaIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.parseLong(subjectId));
				iLog.info("Rolling " + subjectArea.getLabel() + " course offerings forward...");
				Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
				if (rollForwardSessionForm.validateCourseOfferingRollForward(toSession, subjectArea, errors)) {
					Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollCourseOfferingsForwardFrom());
					InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward(iLog);
					instrOffrRollFwd.setClassPrefRollForwardParameter(rollForwardSessionForm.getClassPrefsAction());
					instrOffrRollFwd.setSubpartLocationPrefRollForwardParameters(rollForwardSessionForm.getSubpartLocationPrefsAction());
					instrOffrRollFwd.setSubpartTimePrefRollForwardParameters(rollForwardSessionForm.getSubpartTimePrefsAction());
					instrOffrRollFwd.setRollForwardDistributions(rollForwardSessionForm.getRollForwardDistributions());
					instrOffrRollFwd.setCancelledClassActionRollForwardParameter(rollForwardSessionForm.getCancelledClassAction());
					instrOffrRollFwd.setWaitListsAndProhibitedOverrides(rollForwardSessionForm.getRollForwardWaitListsProhibitedOverrides());
					instrOffrRollFwd.rollForwardInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), fromSession, toSession);
				}
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardCourseOfferings()), e);
				errors.addFieldError("rollForward", e.getMessage());
				break;
			}
			getHibSession().clear();
		}
		if (Boolean.TRUE.equals(rollForwardSessionForm.getRoolForwardParentOfferings())) {
			iLog.info("Checking for prerequisite courses...");
			Transaction tx = getHibSession().beginTransaction();
			try {
				for (Object[] courses: getHibSession().createQuery(
						"select co, parent " +
						"from CourseOffering co, CourseOffering orig, CourseOffering parent where " +
						"co.subjectArea.session.uniqueId = :targetSessionId and parent.subjectArea.session.uniqueId = :targetSessionId and " +
						"co.uniqueIdRolledForwardFrom = orig.uniqueId and " +
						"parent.uniqueIdRolledForwardFrom = orig.parentOffering.uniqueId and " +
						"cast(co.subjectArea.uniqueId as string) in :subjectAreaIds", Object[].class)
						.setParameter("targetSessionId", rollForwardSessionForm.getSessionToRollForwardTo())
						.setParameterList("subjectAreaIds", rollForwardSessionForm.getRollForwardSubjectAreaIds())
						.list()) {
					CourseOffering co = (CourseOffering)courses[0];
					CourseOffering parent = (CourseOffering)courses[1];
					iLog.info("Setting " + parent.getCourseName() + " as a prerequisite for " + co.getCourseName());
					co.setParentOffering(parent);
					getHibSession().merge(co);
				}
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardParentCourseOfferings()), e);
				errors.addFieldError("rollForward", e.getMessage());
			}
			getHibSession().clear();
		}
	}
	
	public void addNewCourseOfferings(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		for (String subjectId: rollForwardSessionForm.getAddNewCourseOfferingsSubjectIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.parseLong(subjectId));
				iLog.info("Rolling " + subjectArea.getLabel() + " new courses forward...");
				Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
				InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward(iLog);
				instrOffrRollFwd.addNewInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), toSession);
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardOfferingCoordinators()), e);
				errors.addFieldError("rollForward", e.getMessage());
				break;
			}
			getHibSession().clear();
		}
	}

	private static String buildRoomQueryForDepartment(Department dept, Session sess, String locType){
		StringBuffer sb = new StringBuffer();
		sb.append("select l from " + locType + " as l inner join l.roomDepts as rd where l.session.uniqueId = ");
		sb.append(sess.getUniqueId().toString());
		sb.append(" and rd.department.uniqueId = ");
		sb.append(dept.getUniqueId().toString());
		return(sb.toString());
	}
	
	private static Set<Location> buildRoomListForDepartment(org.hibernate.Session hibSession, Department department, Session session){
		Set<Location> ts = new TreeSet<Location>();
		for (Room r: hibSession.createQuery(buildRoomQueryForDepartment(department, session, "Room"), Room.class).list()) {
			RoomDept rd = null;
			for (Iterator it2 = r.getRoomDepts().iterator(); it2.hasNext();){
				rd = (RoomDept) it2.next();
				rd.getDepartment();
			}
			ts.add(r);
		}
		for (NonUniversityLocation l: hibSession.createQuery(buildRoomQueryForDepartment(department, session, "NonUniversityLocation"), NonUniversityLocation.class).list()) {
			RoomDept rd = null;
			for (Iterator it2 = l.getRoomDepts().iterator(); it2.hasNext();){
				rd = (RoomDept) it2.next();
				rd.getDepartment();
			}
			ts.add(l);
		}
		return(ts);
	}

	public HashMap getRoomList() {
		if (roomList == null){
			roomList = new HashMap();
		}
		return roomList;
	}

	public boolean sessionHasCourseCatalog(Session session){
		if (session == null){
			return(false);
		}
		if (!getSessionHasCourseCatalogList().containsKey(session)){
			int cnt = getHibSession().createQuery(
					"select count(cc) from CourseCatalog cc where cc.session.uniqueId =" + session.getUniqueId(), Number.class).uniqueResult().intValue();
			getSessionHasCourseCatalogList().put(session, Boolean.valueOf(cnt != 0));	
		}
		return(((Boolean)getSessionHasCourseCatalogList().get(session)).booleanValue());
	}
	
	public HashMap getSessionHasCourseCatalogList() {
		if (sessionHasCourseCatalogList == null){
			sessionHasCourseCatalogList = new HashMap();
		}
		return(sessionHasCourseCatalogList);
	}
	
	public boolean sessionHasExternalBuildingList(Session session) {
		if (!getSessionHasExternalBuildingList().containsKey(session)){
			int cnt = getHibSession().createQuery(
					"select count(eb) from ExternalBuilding eb where eb.session.uniqueId =" + session.getUniqueId(), Number.class).uniqueResult().intValue();
			getSessionHasExternalBuildingList().put(session, Boolean.valueOf(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalBuildingList().get(session)).booleanValue());
	}
	
	public HashMap getSessionHasExternalBuildingList(){
		if (sessionHasExternalBuildingList == null){
			sessionHasExternalBuildingList = new HashMap();
		}
		return(sessionHasExternalBuildingList);
	}

	public boolean sessionHasExternalRoomList(Session session){
		if (!getSessionHasExternalRoomList().containsKey(session)){
			int cnt = getHibSession().createQuery(
					"select count(er) from ExternalRoom er where er.building.session.uniqueId =" + session.getUniqueId(), Number.class).uniqueResult().intValue();
			getSessionHasExternalRoomList().put(session, Boolean.valueOf(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomList().get(session)).booleanValue());
	}
	
	public HashMap getSessionHasExternalRoomList(){
		if (sessionHasExternalRoomList == null){
			sessionHasExternalRoomList = new HashMap();
		}
		return(sessionHasExternalRoomList);
	}

	public boolean sessionHasExternalRoomDeptList(Session session){
		if (!getSessionHasExternalRoomDeptList().containsKey(session)){
			int cnt = getHibSession().createQuery(
					"select count(erd) from ExternalRoomDepartment erd where erd.room.building.session.uniqueId =" + session.getUniqueId(), Number.class).uniqueResult().intValue();
			getSessionHasExternalRoomDeptList().put(session, Boolean.valueOf(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomDeptList().get(session)).booleanValue());
	}
	
	public HashMap getSessionHasExternalRoomDeptList(){
		if (sessionHasExternalRoomDeptList == null){
			sessionHasExternalRoomDeptList = new HashMap();
		}
		return(sessionHasExternalRoomDeptList);
	}

	public boolean sessionHasExternalRoomFeatureList(Session session){
		if (!getSessionHasExternalRoomFeatureList().containsKey(session)){
			int cnt = getHibSession().createQuery(
					"select count(erf) from ExternalRoomFeature erf where erf.room.building.session.uniqueId =" + session.getUniqueId(), Number.class).uniqueResult().intValue();
			getSessionHasExternalRoomFeatureList().put(session, Boolean.valueOf(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomFeatureList().get(session)).booleanValue());
	}
	
	public HashMap getSessionHasExternalRoomFeatureList(){
		if (sessionHasExternalRoomFeatureList == null){
			sessionHasExternalRoomFeatureList = new HashMap();
		}
		return(sessionHasExternalRoomFeatureList);
	}
	
	private void rollTimePatternOntoDepartments(TimePattern fromTimePattern, TimePattern toTimePattern){
		if (fromTimePattern.getDepartments() != null && !fromTimePattern.getDepartments().isEmpty()){
			for(Department fromDept : fromTimePattern.getDepartments()){
				Department toDepartment = Department.findByDeptCode(fromDept.getDeptCode(), toTimePattern.getSession().getSessionId());
				if (toDepartment != null){
					if (null == toDepartment.getTimePatterns()){
						toDepartment.setTimePatterns(new java.util.HashSet());
					}
					toDepartment.getTimePatterns().add(toTimePattern);
					if (null == toTimePattern.getDepartments()){
						toTimePattern.setDepartments(new java.util.HashSet());
					}
					toTimePattern.addToDepartments(toDepartment);
				}
			}
		}		
	}


	public void rollTimePatternsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollTimePatternsForwardFrom());
		List<TimePattern> fromDatePatterns = TimePattern.findAll(fromSession, null);
		TimePattern fromTimePattern = null;
		TimePattern toTimePattern = null;
		for(Iterator<TimePattern> it = fromDatePatterns.iterator(); it.hasNext();){
			fromTimePattern = it.next();
			if (fromTimePattern != null){
				toTimePattern = (TimePattern) fromTimePattern.clone();
				toTimePattern.setSession(toSession);
				rollTimePatternOntoDepartments(fromTimePattern, toTimePattern);
				getHibSession().persist(toTimePattern);
			}
		}
		getHibSession().flush();
	}
		
	public void rollClassInstructorsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		for (String subjectId: rollForwardSessionForm.getRollForwardClassInstrSubjectIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.parseLong(subjectId));
				iLog.info("Rolling " + subjectArea.getLabel() + " class instructors forward...");
				Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
				if (rollForwardSessionForm.validateClassInstructorRollForward(toSession, subjectArea, errors)) {
					rollForwardClassInstructorsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), toSession);
				} else {
					break;
				}
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

	private void rollForwardClassInstructorsForASubjectArea(String subjectAreaAbbreviation, Session toSession) {
		iLog.info("Rolling forward class instructors for:  " + subjectAreaAbbreviation);
		Class_DAO clsDao = new Class_DAO();
		List classes = Class_.findAllForControllingSubjectArea(subjectAreaAbbreviation, toSession.getUniqueId(), getHibSession());
		if (classes != null && !classes.isEmpty()){
			Class_ toClass = null;
			Class_ fromClass = null;
			for (Iterator cIt = classes.iterator(); cIt.hasNext();){
				toClass = (Class_) cIt.next();
				if (toClass.getUniqueIdRolledForwardFrom() != null){
					
					fromClass = clsDao.get(toClass.getUniqueIdRolledForwardFrom(), getHibSession());
					if (fromClass != null){
						if (fromClass.getClassInstructors() != null && !fromClass.getClassInstructors().isEmpty()) {
							ClassInstructor fromClassInstr = null;
							ClassInstructor toClassInstr = null;
							DepartmentalInstructor toDeptInstr = null;
							for (Iterator ciIt = fromClass.getClassInstructors().iterator(); ciIt.hasNext();){
								fromClassInstr = (ClassInstructor) ciIt.next();
								if (fromClassInstr.getTeachingRequest() != null) continue;
								toDeptInstr = fromClassInstr.getInstructor().findThisInstructorInSession(toSession.getUniqueId(), getHibSession());
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
									getHibSession().persist(toClassInstr);
								}
							}
						}
					}
				}
			}	
		}
		getHibSession().flush();
	}
	
	public void rollOfferingCoordinatorsForward(RollForwardErrors errors,RollForwardSessionForm rollForwardSessionForm) {
		for (String subjectId: rollForwardSessionForm.getRollForwardOfferingCoordinatorsSubjectIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.parseLong(subjectId));
				iLog.info("Rolling " + subjectArea.getLabel() + " offering coordinators forward...");
				Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
				if (rollForwardSessionForm.validateOfferingCoordinatorsRollForward(toSession, subjectArea, errors)) {
					rollForwardOfferingCoordinatorsForASubjectArea(subjectArea, toSession);
				} else {
					break;
				}
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardOfferingCoordinators()), e);
				errors.addFieldError("rollForward", e.getMessage());
				break;
			}
			getHibSession().clear();
		}
	}
	
	private void rollForwardOfferingCoordinatorsForASubjectArea(SubjectArea subjectArea, Session toSession) {
		iLog.info("Rolling forward offering coordinators for:  " + subjectArea.getSubjectAreaAbbreviation());
		for (InstructionalOffering toInstructionalOffering: getHibSession().createQuery(
				"select co.instructionalOffering from CourseOffering co where co.isControl = true and co.subjectArea.uniqueId = :subjectAreaId and co.instructionalOffering.uniqueIdRolledForwardFrom is not null", InstructionalOffering.class)
				.setParameter("subjectAreaId", subjectArea.getUniqueId()).list()) {
			InstructionalOffering fromInstructionalOffering = InstructionalOfferingDAO.getInstance().get(toInstructionalOffering.getUniqueIdRolledForwardFrom());
			if (fromInstructionalOffering != null) {
				for (Iterator coIt = fromInstructionalOffering.getOfferingCoordinators().iterator(); coIt.hasNext();){
					OfferingCoordinator fromOfferingCoordinator = (OfferingCoordinator) coIt.next();
					if (fromOfferingCoordinator.getTeachingRequest() != null) continue;
					DepartmentalInstructor toInstructor = fromOfferingCoordinator.getInstructor().findThisInstructorInSession(toSession.getUniqueId());
					if (toInstructor != null) {
						OfferingCoordinator toOfferingCoordinator = new OfferingCoordinator();
						toOfferingCoordinator.setInstructor(toInstructor);
						toOfferingCoordinator.setOffering(toInstructionalOffering);
						toOfferingCoordinator.setResponsibility(fromOfferingCoordinator.getResponsibility());
						toOfferingCoordinator.setPercentShare(fromOfferingCoordinator.getPercentShare());
						toInstructionalOffering.addToOfferingCoordinators(toOfferingCoordinator);
						getHibSession().persist(toOfferingCoordinator);
					}
				}
			}
		}
		getHibSession().flush();
	}

	public void rollStudentsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm){
        Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
        
        String[] query = null;
        
        if (rollForwardSessionForm.getRollForwardStudentsMode().equals(StudentEnrollmentMode.LAST_LIKE.name())) {
            query = new String[] {
                     "select distinct d.student, co, d.priority from LastLikeCourseDemand d, CourseOffering co, CourseOffering last "+
                     "where co.subjectArea.session.uniqueId=:toSessionId and co.uniqueIdRolledForwardFrom=last.uniqueId and "+
                     "((d.coursePermId is null and d.subjectArea.uniqueId = last.subjectArea.uniqueId and d.courseNbr=last.courseNbr) or " +
                     "(d.coursePermId is not null and d.coursePermId=last.permId))"};
        } else if (rollForwardSessionForm.getRollForwardStudentsMode().equals(StudentEnrollmentMode.STUDENT_CLASS_ENROLLMENTS.name())) {
            query = new String[] {
                    "select distinct e.student, co, e.courseRequest.courseDemand.priority from StudentClassEnrollment e, CourseOffering co "+
                    "where co.subjectArea.session.uniqueId=:toSessionId and co.uniqueIdRolledForwardFrom=e.courseOffering.uniqueId",
                    "select distinct e.student, co, -1 from StudentClassEnrollment e, CourseOffering co "+
                    "where co.subjectArea.session.uniqueId=:toSessionId and co.uniqueIdRolledForwardFrom=e.courseOffering.uniqueId and "+
                    "e.courseRequest is null"};
        } else if (rollForwardSessionForm.getRollForwardStudentsMode().equals(StudentEnrollmentMode.STUDENT_COURSE_REQUESTS.name())) {
            query = new String[] {
                    "select r.courseDemand.student, co, r.courseDemand.priority from CourseRequest r, CourseOffering co "+
                    "where co.subjectArea.session.uniqueId=:toSessionId and co.uniqueIdRolledForwardFrom=r.courseOffering.uniqueId and " +
                    "r.order=0 and r.courseDemand.alternative=false"};
        } else if (rollForwardSessionForm.getRollForwardStudentsMode().equals(StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS.name())) {
            query = new String[] {
                    "select distinct psce.pitStudent.student, co, -1 from PitStudentClassEnrollment psce, CourseOffering co "+
                    "where co.subjectArea.session.uniqueId=:toSessionId and co.uniqueIdRolledForwardFrom=psce.pitCourseOffering.courseOffering.uniqueId and "+
                    "psce.pitStudent.pointInTimeData.uniqueId = " + rollForwardSessionForm.getPointInTimeSnapshotToRollCourseEnrollmentsForwardFrom().toString() };
        }
        
        getHibSession().createMutationQuery("delete LastLikeCourseDemand d where d.subjectArea.uniqueId in " +
        		"(select s.uniqueId from SubjectArea s where s.session.uniqueId=:toSessionId)")
        		.setParameter("toSessionId", toSession.getUniqueId().longValue()).executeUpdate();;
        
        for (int i=0;i<query.length;i++) {
            for (Object[] o: getHibSession().createQuery(query[i], Object[].class)
            		.setParameter("toSessionId", toSession.getUniqueId()).list()) {
                Student s = (Student)o[0];
                CourseOffering co = (CourseOffering)o[1];
                Number priority = (Number)o[2];
                LastLikeCourseDemand d = new LastLikeCourseDemand();
                d.setPriority(priority.intValue());
                d.setSubjectArea(co.getSubjectArea());
                d.setCourseNbr(co.getCourseNbr());
                d.setCoursePermId(co.getPermId());
                d.setStudent(s);
                getHibSession().persist(d);
            }
        }
        getHibSession().flush();
        if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
	        getHibSession().createMutationQuery("update CourseOffering c set c.demand="+
	                "(select count(distinct d.student) from LastLikeCourseDemand d where "+
	                "(c.subjectArea=d.subjectArea and c.courseNbr=d.courseNbr)) where "+
	                "c.subjectArea.uniqueId in (select sa.uniqueId from SubjectArea sa where sa.session.uniqueId=:sessionId)").
	                setParameter("sessionId", toSession.getUniqueId().longValue()).executeUpdate();
        } else {
        	getHibSession().createMutationQuery("update CourseOffering c set c.demand="+
                    "(select count(distinct d.student) from LastLikeCourseDemand d where "+
                    "(c.subjectArea=d.subjectArea and c.courseNbr=d.courseNbr)) where "+
                    "c.permId is null and c.subjectArea.uniqueId in (select sa.uniqueId from SubjectArea sa where sa.session.uniqueId=:sessionId)").
                    setParameter("sessionId", toSession.getUniqueId().longValue()).executeUpdate();

        	getHibSession().createMutationQuery("update CourseOffering c set c.demand="+
	                "(select count(distinct d.student) from LastLikeCourseDemand d where "+
	                "d.student.session=c.subjectArea.session and c.permId=d.coursePermId) where "+
	                "c.permId is not null and c.subjectArea.uniqueId in (select sa.uniqueId from SubjectArea sa where sa.session.uniqueId=:sessionId)").
	                setParameter("sessionId", toSession.getUniqueId().longValue()).executeUpdate();

        }
        getHibSession().flush();
    }
	
	public void rollCurriculaForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
        Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
        Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollCurriculaForwardFrom());
        
        // roll forward academic areas, if needed
        Hashtable<String, AcademicArea> areas = new Hashtable<String, AcademicArea>();
        for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	areas.put(area.getAcademicAreaAbbreviation(), area);
        }
        if (areas.isEmpty()) {
        	for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
        		AcademicArea newArea = (AcademicArea)area.clone();
        		newArea.setSession(toSession);
        		newArea.setPosMajors(new HashSet<PosMajor>());
        		newArea.setPosMinors(new HashSet<PosMinor>());
        		getHibSession().persist(newArea);
        		areas.put(newArea.getAcademicAreaAbbreviation(), newArea);
        	}
        }
        
        // roll forward academic classifications, if needed
        Hashtable<String, AcademicClassification> classifications = new Hashtable<String, AcademicClassification>();
        for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	classifications.put(clasf.getCode(), clasf);
        }
        if (classifications.isEmpty()) {
        	for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
        		AcademicClassification newClasf = (AcademicClassification)clasf.clone();
        		newClasf.setSession(toSession);
        		getHibSession().persist(newClasf);
        		classifications.put(newClasf.getCode(), newClasf);
        	}
        }
        
        // roll forward degrees, if needed
        Hashtable<String, Degree> degrees = new Hashtable<String, Degree>();
        for (Degree degree: DegreeDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	degrees.put(degree.getReference(), degree);
        }
        if (degrees.isEmpty()) {
        	for (Degree degree: DegreeDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
        		Degree newDegree = (Degree)degree.clone();
        		newDegree.setSession(toSession);
        		getHibSession().persist(newDegree);
        		degrees.put(newDegree.getReference(), degree);
        	}
        }
        
        // roll forward majors, if needed
        Hashtable<String, Hashtable<String, PosMajor>> majors = new Hashtable<String, Hashtable<String,PosMajor>>();
        for (PosMajor major: PosMajorDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	for (AcademicArea area: major.getAcademicAreas()) {
        		Hashtable<String, PosMajor> code2major = majors.get(area.getAcademicAreaAbbreviation());
        		if (code2major == null) {
        			code2major = new Hashtable<String, PosMajor>();
        			majors.put(area.getAcademicAreaAbbreviation(), code2major);
        		}
        		code2major.put(major.getCode(), major);
        	}
        }
        if (majors.isEmpty()) {
            for (PosMajor major: PosMajorDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
            	Set<AcademicArea> newAreas = new HashSet<AcademicArea>();
            	for (AcademicArea area: major.getAcademicAreas()) {
            		AcademicArea newArea = areas.get(area.getAcademicAreaAbbreviation());
            		if (newArea != null) newAreas.add(newArea);
            	}
            	if (newAreas.isEmpty()) continue;
            	PosMajor newMajor = (PosMajor)major.clone();
            	newMajor.setSession(toSession);
            	newMajor.setAcademicAreas(newAreas);
            	newMajor.setConcentrations(new HashSet<PosMajorConcentration>());
            	for (AcademicArea newArea: newAreas) {
            		newArea.getPosMajors().add(newMajor);
            		Hashtable<String, PosMajor> code2major = majors.get(newArea.getAcademicAreaAbbreviation());
            		if (code2major == null) {
            			code2major = new Hashtable<String, PosMajor>();
            			majors.put(newArea.getAcademicAreaAbbreviation(), code2major);
            		}
            		code2major.put(newMajor.getCode(), newMajor);
            	}
            	getHibSession().persist(newMajor);
            	for (PosMajorConcentration conc: major.getConcentrations()) {
            		PosMajorConcentration newConc = (PosMajorConcentration)conc.clone();
            		newConc.setMajor(newMajor);
            		newMajor.getConcentrations().add(newConc);
            		getHibSession().persist(newConc);
            	}
            }
        }
        
        // roll forward programs, if needed
        Hashtable<String, Program> programs = new Hashtable<String, Program>();
        for (Program program: ProgramDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	programs.put(program.getReference(), program);
        }
        if (programs.isEmpty()) {
        	for (Program program: ProgramDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
        		Program newProgram = (Program)program.clone();
        		newProgram.setSession(toSession);
        		getHibSession().persist(newProgram);
        		programs.put(newProgram.getReference(), newProgram);
        	}
        }
        
     // roll forward campuses, if needed
        Hashtable<String, Campus> campuses = new Hashtable<String, Campus>();
        for (Campus campus: CampusDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	campuses.put(campus.getReference(), campus);
        }
        if (campuses.isEmpty()) {
        	for (Campus campus: CampusDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
        		Campus newCampus = (Campus)campus.clone();
        		newCampus.setSession(toSession);
        		getHibSession().persist(newCampus);
        		campuses.put(newCampus.getReference(), newCampus);
        	}
        }
        
        // roll forward minors, if needed
        Hashtable<String, Hashtable<String, PosMinor>> minors = new Hashtable<String, Hashtable<String,PosMinor>>();
        for (PosMinor minor: PosMinorDAO.getInstance().findBySession(getHibSession(), toSession.getUniqueId())) {
        	for (AcademicArea area: minor.getAcademicAreas()) {
        		Hashtable<String, PosMinor> code2minor = minors.get(area.getAcademicAreaAbbreviation());
        		if (code2minor == null) {
        			code2minor = new Hashtable<String, PosMinor>();
        			minors.put(area.getAcademicAreaAbbreviation(), code2minor);
        		}
        		code2minor.put(minor.getCode(), minor);
        	}
        }
        if (minors.isEmpty()) {
            for (PosMinor minor: PosMinorDAO.getInstance().findBySession(getHibSession(), fromSession.getUniqueId())) {
            	Set<AcademicArea> newAreas = new HashSet<AcademicArea>();
            	for (AcademicArea area: minor.getAcademicAreas()) {
            		AcademicArea newArea = areas.get(area.getAcademicAreaAbbreviation());
            		if (newArea != null) newAreas.add(newArea);
            	}
            	if (newAreas.isEmpty()) continue;
            	PosMinor newMinor = (PosMinor)minor.clone();
            	newMinor.setSession(toSession);
            	newMinor.setAcademicAreas(newAreas);
            	for (AcademicArea newArea: newAreas) {
            		newArea.getPosMinors().add(newMinor);
            		Hashtable<String, PosMinor> code2minor = minors.get(newArea.getAcademicAreaAbbreviation());
            		if (code2minor == null) {
            			code2minor = new Hashtable<String, PosMinor>();
            			minors.put(newArea.getAcademicAreaAbbreviation(), code2minor);
            		}
            		code2minor.put(newMinor.getCode(), newMinor);
            	}
            	getHibSession().persist(newMinor);
            }        	
        }
        
        // course translation table
        Hashtable<Long, CourseOffering> courses = new Hashtable<Long, CourseOffering>();
        for (CourseOffering course: getHibSession().createQuery("select co from CourseOffering co " +
        		"where co.uniqueIdRolledForwardFrom is not null and " +
        		"co.subjectArea.session.uniqueId = :sessionId", CourseOffering.class)
        		.setParameter("sessionId", toSession.getUniqueId()).list()) {
        	courses.put(course.getUniqueIdRolledForwardFrom(), course);
        }
        
        // cleanup all curricula
        for (Iterator<Curriculum> i = getHibSession().createQuery(
        		"select c from Curriculum c where c.department.session.uniqueId=:sessionId", Curriculum.class).
            	setParameter("sessionId", toSession.getUniqueId()).list().iterator(); i.hasNext(); ) {
        	getHibSession().remove(i.next());
    	}
        getHibSession().flush();
    	
    	// roll forward curricula
		Department tempDept = null;
    	curricula: for (Curriculum curriculum: getHibSession().createQuery("select c from Curriculum c where c.department.session.uniqueId=:sessionId", Curriculum.class).
            	setParameter("sessionId", fromSession.getUniqueId()).list()) {
    		Curriculum newCurriculum = new Curriculum();
    		newCurriculum.setAbbv(curriculum.getAbbv());
    		newCurriculum.setName(curriculum.getName());
    		newCurriculum.setMultipleMajors(curriculum.isMultipleMajors());
    		AcademicArea area = areas.get(curriculum.getAcademicArea().getAcademicAreaAbbreviation());
    		if (area == null) continue;
    		newCurriculum.setAcademicArea(area);
    		Department dept = curriculum.getDepartment().findSameDepartmentInSession(toSession);
    		if (dept == null) {
    			if (tempDept == null) {
    				tempDept = Department.findByDeptCode("TEMP", toSession.getUniqueId());
    				if (tempDept == null){
    					tempDept = new Department();
    					tempDept.setAbbreviation("TEMP");
    					tempDept.setAllowReqRoom(Boolean.valueOf(false));
    					tempDept.setAllowReqTime(Boolean.valueOf(false));
    					tempDept.setAllowReqDistribution(Boolean.valueOf(false));
    					tempDept.setDeptCode("TEMP");
    					tempDept.setExternalManager(Boolean.valueOf(false));
    					tempDept.setExternalUniqueId(null);
    					tempDept.setName("Temp Department For New Curricula");
    					tempDept.setSession(toSession);
    					tempDept.setDistributionPrefPriority(Integer.valueOf(0));
    					tempDept.setInheritInstructorPreferences(true);
    					tempDept.setAllowEvents(false);
    					tempDept.setAllowStudentScheduling(false);
    					toSession.addToDepartments(tempDept);
    					getHibSession().persist(tempDept);
    				}
    			}
    			dept = tempDept;
    		}
    		newCurriculum.setDepartment(dept);
    		newCurriculum.setMajors(new HashSet<PosMajor>());
    		Hashtable<String, PosMajor> code2major = majors.get(area.getAcademicAreaAbbreviation());
    		for (PosMajor major: curriculum.getMajors()) {
    			PosMajor newMajor = (code2major == null ? null : code2major.get(major.getCode()));
    			if (newMajor == null) continue curricula;
    			newCurriculum.getMajors().add(newMajor);
    		}
    		newCurriculum.setClassifications(new HashSet<CurriculumClassification>());
            Hashtable<Long, CurriculumCourseGroup> createdGroups = new Hashtable<Long, CurriculumCourseGroup>();
    		for (CurriculumClassification clasf: curriculum.getClassifications()) {
    			CurriculumClassification newClasf = new CurriculumClassification();
    			AcademicClassification f = classifications.get(clasf.getAcademicClassification().getCode());
    			if (f == null) continue;
    			newClasf.setAcademicClassification(f);
    			newClasf.setCurriculum(newCurriculum);
    			newClasf.setName(clasf.getName());
    			newClasf.setNrStudents(clasf.getNrStudents());
    			newClasf.setOrd(clasf.getOrd());
    			newClasf.setCourses(new HashSet<CurriculumCourse>());
    			newCurriculum.getClassifications().add(newClasf);
    			for (CurriculumCourse course: clasf.getCourses()) {
    				CurriculumCourse newCourse = new CurriculumCourse();
    				newCourse.setOrd(course.getOrd());
    				newCourse.setPercShare(course.getPercShare());
    				CourseOffering co = courses.get(course.getCourse().getUniqueId());
    				if (co == null) continue;
    				newCourse.setCourse(co);
    				newCourse.setClassification(newClasf);
    				newClasf.getCourses().add(newCourse);
    				newCourse.setGroups(new HashSet<CurriculumCourseGroup>());
    				for (CurriculumCourseGroup group: course.getGroups()) {
    					CurriculumCourseGroup newGroup = createdGroups.get(group.getUniqueId());
    					if (newGroup == null) {
    						newGroup = new CurriculumCourseGroup();
    						newGroup.setColor(group.getColor());
    						newGroup.setName(group.getName());
    						newGroup.setType(group.getType());
    						newGroup.setCurriculum(newCurriculum);
    						createdGroups.put(group.getUniqueId(), newGroup);
    					}
    					newCourse.getGroups().add(newGroup);
    				}
    			}
    		}
    		
    		getHibSession().persist(newCurriculum);
            for (CurriculumCourseGroup g: createdGroups.values())
            	getHibSession().persist(g);
    	}
		
		// roll forward projection rules (if empty)
		if (getHibSession().createQuery("select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId = :sessionId", CurriculumProjectionRule.class)
				.setParameter("sessionId", toSession.getUniqueId()).list().isEmpty()) {
			rules: for (CurriculumProjectionRule rule: getHibSession().createQuery("select r from CurriculumProjectionRule r " +
					"where r.academicArea.session.uniqueId = :sessionId", CurriculumProjectionRule.class).setParameter("sessionId", fromSession.getUniqueId()).list()) {
				CurriculumProjectionRule newRule = new CurriculumProjectionRule();
	    		AcademicArea area = areas.get(rule.getAcademicArea().getAcademicAreaAbbreviation());
	    		if (area == null) continue;
				newRule.setAcademicArea(area);
				AcademicClassification clasf = classifications.get(rule.getAcademicClassification().getCode());
				if (clasf == null) continue;
				newRule.setAcademicClassification(clasf);
				if (rule.getMajor() != null) {
		    		Hashtable<String, PosMajor> code2major = majors.get(area.getAcademicAreaAbbreviation());
					PosMajor major = (code2major == null ? null : code2major.get(rule.getMajor().getCode()));
					if (major == null) continue rules;
					newRule.setMajor(major);
				}
				newRule.setProjection(rule.getProjection());
				getHibSession().persist(newRule);
			}
		}
		
        getHibSession().flush();
	}
	
	public void rollSessionConfigurationForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
        Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
        Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollSessionConfigForwardFrom());
        
        // remove old configuration
        for (SessionConfig config: getHibSession().createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId", SessionConfig.class
        		).setParameter("sessionId", toSession.getUniqueId()).list()) {
        	getHibSession().remove(config);
        }
        
        // create new configuration
        for (SessionConfig config: getHibSession().createQuery(
        		"from SessionConfig where session.uniqueId = :sessionId", SessionConfig.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	
        	SessionConfig newConfig = new SessionConfig();
        	newConfig.setKey(config.getKey());
        	newConfig.setDescription(config.getDescription());
        	newConfig.setValue(config.getValue());
        	newConfig.setSession(toSession);
        	
        	getHibSession().persist(newConfig);
        }
        
        // remove old notes
        for (StandardEventNoteSession note: getHibSession().createQuery(
        		"from StandardEventNoteSession where session.uniqueId = :sessionId", StandardEventNoteSession.class
        		).setParameter("sessionId", toSession.getUniqueId()).list()) {
        	getHibSession().remove(note);
        }
        
        for (StandardEventNoteDepartment note: getHibSession().createQuery(
        		"from StandardEventNoteDepartment where department.session.uniqueId = :sessionId", StandardEventNoteDepartment.class
        		).setParameter("sessionId", toSession.getUniqueId()).list()) {
        	getHibSession().remove(note);
        }
        
        // create new notes
        for (StandardEventNoteSession note: getHibSession().createQuery(
        		"from StandardEventNoteSession where session.uniqueId = :sessionId", StandardEventNoteSession.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	StandardEventNoteSession newNote = new StandardEventNoteSession();
        	newNote.setNote(note.getNote());
        	newNote.setReference(note.getReference());
        	newNote.setSession(toSession);
        	getHibSession().persist(newNote);
        }
        
        for (StandardEventNoteDepartment note: getHibSession().createQuery(
        		"from StandardEventNoteDepartment where department.session.uniqueId = :sessionId", StandardEventNoteDepartment.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	Department newDepartment = note.getDepartment().findSameDepartmentInSession(toSession);
        	if (newDepartment != null) {
            	StandardEventNoteDepartment newNote = new StandardEventNoteDepartment();
            	newNote.setNote(note.getNote());
            	newNote.setReference(note.getReference());
            	newNote.setDepartment(newDepartment);
            	getHibSession().persist(newNote);
        	}
        }
        
        // remove room type options
        for (RoomTypeOption option: getHibSession().createQuery(
        		"from RoomTypeOption where department.session.uniqueId = :sessionId", RoomTypeOption.class
        		).setParameter("sessionId", toSession.getUniqueId()).list()) {
        	getHibSession().remove(option);
        }
        
        // create new room type options
        for (RoomTypeOption option: getHibSession().createQuery(
        		"from RoomTypeOption where department.session.uniqueId = :sessionId", RoomTypeOption.class
        		).setParameter("sessionId", fromSession.getUniqueId()).list()) {
        	Department newDepartment = option.getDepartment().findSameDepartmentInSession(toSession);
        	if (newDepartment != null) {
        		RoomTypeOption newOption = new RoomTypeOption();
        		newOption.setBreakTime(option.getBreakTime());
        		newOption.setDepartment(newDepartment);
        		newOption.setMessage(option.getMessage());
        		newOption.setRoomType(option.getRoomType());
        		newOption.setStatus(RoomTypeOption.getDefaultStatus());
        		getHibSession().persist(newOption);
        	}
        }
        
        // remove old service providers
        for (EventServiceProvider provider: getHibSession().createQuery(
        		"from EventServiceProvider where session.uniqueId = :sessionId", EventServiceProvider.class
        		).setParameter("sessionId", toSession.getUniqueId()).list()) {
        	getHibSession().remove(provider);
        }
        
        // create new service providers
        for (EventServiceProvider provider: getHibSession().createQuery(
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
        	newProvider.setSession(toSession);
        	if (provider.getDepartment() != null) {
        		Department newDepartment = provider.getDepartment().findSameDepartmentInSession(toSession);
        		if (newDepartment == null) continue;
        		newProvider.setDepartment(newDepartment);
        	}
        	getHibSession().persist(newProvider);
        }
        
        // roll forward global instructor attributes
        rollGlobalInstructorAttributesForward(errors, fromSession, toSession);
        
        getHibSession().flush();
        
        ApplicationProperties.clearSessionProperties(toSession.getUniqueId());
	}

	public void rollReservationsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT);
		List<SubjectArea> subjects = new ArrayList<SubjectArea>();
		List<Long> subjectIds = new ArrayList<Long>();
		List<String> subjectAbbvs = new ArrayList<String>();
		for (String subjectId: rollForwardSessionForm.getRollForwardReservationsSubjectIds()) {
			SubjectArea subject = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectId));
			subjectIds.add(subject.getUniqueId());
			subjectAbbvs.add(subject.getSubjectAreaAbbreviation());
			subjects.add(subject);
		}
		if (subjects.isEmpty()) return;
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		
		if (rollForwardSessionForm.getRollForwardCourseReservations()) {
			Date expiration = null;
			if (rollForwardSessionForm.getExpirationCourseReservations() != null && !rollForwardSessionForm.getExpirationCourseReservations().isEmpty()) {
				try {
					expiration = df.parse(rollForwardSessionForm.getExpirationCourseReservations());
				} catch (ParseException e) {}
			}
			Date startDate = null;
			if (rollForwardSessionForm.getStartDateCourseReservations() != null && !rollForwardSessionForm.getStartDateCourseReservations().isEmpty()) {
				try {
					startDate = df.parse(rollForwardSessionForm.getStartDateCourseReservations());
				} catch (ParseException e) {}
			}
			getHibSession().createMutationQuery("delete CourseReservation r where r.instructionalOffering.uniqueId in (select c.instructionalOffering.uniqueId from CourseOffering c where c.subjectArea.uniqueId in :subjectIds and c.isControl = true)"
					).setParameterList("subjectIds", subjectIds, Long.class).executeUpdate();
			for (CourseReservation reservation: getHibSession().createQuery(
					"select distinct r from CourseReservation r inner join r.instructionalOffering.courseOfferings c where " +
					"c.isControl = true and c.subjectArea.subjectAreaAbbreviation in :subjectAbbvs and c.subjectArea.department.session.uniqueId = :sessionId", CourseReservation.class)
					.setParameterList("subjectAbbvs", subjectAbbvs, String.class)
					.setParameter("sessionId", rollForwardSessionForm.getSessionToRollReservationsForwardFrom())
					.list()) {
				CourseReservation toReservation = rollCourseReservationForward(reservation, toSession, startDate, expiration);
				if (toReservation != null)
					getHibSession().persist(toReservation);
			}
		}
		
		if (rollForwardSessionForm.getRollForwardCurriculumReservations()) {
			Date expiration = null;
			if (rollForwardSessionForm.getExpirationCurriculumReservations() != null && !rollForwardSessionForm.getExpirationCurriculumReservations().isEmpty()) {
				try {
					expiration = df.parse(rollForwardSessionForm.getExpirationCurriculumReservations());
				} catch (ParseException e) {}
			}
			Date startDate = null;
			if (rollForwardSessionForm.getStartDateCurriculumReservations() != null && !rollForwardSessionForm.getStartDateCurriculumReservations().isEmpty()) {
				try {
					startDate = df.parse(rollForwardSessionForm.getStartDateCurriculumReservations());
				} catch (ParseException e) {}
			}

			Map<String, AcademicArea> areas = new Hashtable<String, AcademicArea>();
	        for (AcademicArea area: AcademicAreaDAO.getInstance().findBySession(getHibSession(), rollForwardSessionForm.getSessionToRollForwardTo()))
	        	areas.put(area.getAcademicAreaAbbreviation(), area);

	        Map<String, AcademicClassification> classifications = new Hashtable<String, AcademicClassification>();
	        for (AcademicClassification clasf: AcademicClassificationDAO.getInstance().findBySession(getHibSession(), rollForwardSessionForm.getSessionToRollForwardTo()))
	        	classifications.put(clasf.getCode(), clasf);

	        Map<String, Map<String, PosMajor>> majors = new Hashtable<String, Map<String,PosMajor>>();
	        Map<String, Map<String, Map<String, PosMajorConcentration>>> concentrations = new Hashtable<String, Map<String, Map<String, PosMajorConcentration>>>();
	        for (PosMajor major: PosMajorDAO.getInstance().findBySession(getHibSession(), rollForwardSessionForm.getSessionToRollForwardTo())) {
	        	for (AcademicArea area: major.getAcademicAreas()) {
	        		Map<String, PosMajor> code2major = majors.get(area.getAcademicAreaAbbreviation());
	        		if (code2major == null) {
	        			code2major = new Hashtable<String, PosMajor>();
	        			majors.put(area.getAcademicAreaAbbreviation(), code2major);
	        		}
	        		code2major.put(major.getCode(), major);
	        		if (!major.getConcentrations().isEmpty()) {
		        		Map<String, Map<String, PosMajorConcentration>> majorCode2concentration = concentrations.get(area.getAcademicAreaAbbreviation());
		        		if (majorCode2concentration == null) {
		        			majorCode2concentration = new Hashtable<String, Map<String, PosMajorConcentration>>();
		        			concentrations.put(area.getAcademicAreaAbbreviation(), majorCode2concentration);
		        		}
		        		Map<String, PosMajorConcentration> code2concentration = new Hashtable<String, PosMajorConcentration>();
		        		majorCode2concentration.put(major.getCode(), code2concentration);
		        		for (PosMajorConcentration conc: major.getConcentrations()) {
		        			code2concentration.put(conc.getCode(), conc);
		        		}
	        		}
	        	}
	        }
	        
	        Map<String, Map<String, PosMinor>> minors = new Hashtable<String, Map<String, PosMinor>>();
	        for (PosMinor minor: PosMinorDAO.getInstance().findBySession(getHibSession(), rollForwardSessionForm.getSessionToRollForwardTo())) {
	        	for (AcademicArea area: minor.getAcademicAreas()) {
	        		Map<String, PosMinor> code2minor = minors.get(area.getAcademicAreaAbbreviation());
	        		if (code2minor == null) {
	        			code2minor = new Hashtable<String, PosMinor>();
	        			minors.put(area.getAcademicAreaAbbreviation(), code2minor);
	        		}
	        		code2minor.put(minor.getCode(), minor);
	        	}
	        }
	        
			getHibSession().createMutationQuery("delete CurriculumReservation r where r.instructionalOffering.uniqueId in (select c.instructionalOffering.uniqueId from CourseOffering c where c.subjectArea.uniqueId in :subjectIds and c.isControl = true)"
					).setParameterList("subjectIds", subjectIds, Long.class).executeUpdate();
			
			for (CurriculumReservation reservation: getHibSession().createQuery(
					"select distinct r from CurriculumReservation r inner join r.instructionalOffering.courseOfferings c where " +
					"c.isControl = true and c.subjectArea.subjectAreaAbbreviation in :subjectAbbvs and c.subjectArea.department.session.uniqueId = :sessionId", CurriculumReservation.class)
					.setParameterList("subjectAbbvs", subjectAbbvs, String.class)
					.setParameter("sessionId", rollForwardSessionForm.getSessionToRollReservationsForwardFrom()).list()) {
				CurriculumReservation toReservation = rollCurriculumReservationForward(reservation, toSession, startDate, expiration, areas, classifications, majors, concentrations, minors);
				if (toReservation != null)
					getHibSession().persist(toReservation);
			}
		}
		
		if (rollForwardSessionForm.getRollForwardGroupReservations()) {
			Date expiration = null;
			if (rollForwardSessionForm.getExpirationGroupReservations() != null && !rollForwardSessionForm.getExpirationGroupReservations().isEmpty()) {
				try {
					expiration = df.parse(rollForwardSessionForm.getExpirationGroupReservations());
				} catch (ParseException e) {}
			}
			Date startDate = null;
			if (rollForwardSessionForm.getStartDateGroupReservations() != null && !rollForwardSessionForm.getStartDateGroupReservations().isEmpty()) {
				try {
					startDate = df.parse(rollForwardSessionForm.getStartDateGroupReservations());
				} catch (ParseException e) {}
			}

			Hashtable<String, StudentGroup> groups = new Hashtable<String, StudentGroup>();
	        for (StudentGroup group: StudentGroupDAO.getInstance().findBySession(getHibSession(), rollForwardSessionForm.getSessionToRollForwardTo()))
	        	groups.put(group.getGroupAbbreviation(), group);

			getHibSession().createMutationQuery("delete StudentGroupReservation r where r.instructionalOffering.uniqueId in (select c.instructionalOffering.uniqueId from CourseOffering c where c.subjectArea.uniqueId in :subjectIds and c.isControl = true)"
					).setParameterList("subjectIds", subjectIds, Long.class).executeUpdate();
			
			for (StudentGroupReservation reservation: getHibSession().createQuery(
					"select distinct r from StudentGroupReservation r inner join r.instructionalOffering.courseOfferings c where " +
					"c.isControl = true and c.subjectArea.subjectAreaAbbreviation in :subjectAbbvs and c.subjectArea.department.session.uniqueId = :sessionId", StudentGroupReservation.class)
					.setParameterList("subjectAbbvs", subjectAbbvs, String.class)
					.setParameter("sessionId", rollForwardSessionForm.getSessionToRollReservationsForwardFrom()).list()) {
				StudentGroupReservation toReservation = rollGroupReservationForward(reservation, toSession, startDate, expiration, groups, rollForwardSessionForm.getCreateStudentGroupsIfNeeded());
				if (toReservation != null)
					getHibSession().persist(toReservation);
			}
		}
		
		if (rollForwardSessionForm.getRollForwardUniversalReservations()) {
			Date expiration = null;
			if (rollForwardSessionForm.getExpirationUniversalReservations() != null && !rollForwardSessionForm.getExpirationUniversalReservations().isEmpty()) {
				try {
					expiration = df.parse(rollForwardSessionForm.getExpirationUniversalReservations());
				} catch (ParseException e) {}
			}
			Date startDate = null;
			if (rollForwardSessionForm.getStartDateUniversalReservations() != null && !rollForwardSessionForm.getStartDateUniversalReservations().isEmpty()) {
				try {
					startDate = df.parse(rollForwardSessionForm.getStartDateUniversalReservations());
				} catch (ParseException e) {}
			}
			getHibSession().createMutationQuery("delete UniversalOverrideReservation r where r.instructionalOffering.uniqueId in (select c.instructionalOffering.uniqueId from CourseOffering c where c.subjectArea.uniqueId in :subjectIds and c.isControl = true)"
					).setParameterList("subjectIds", subjectIds, Long.class).executeUpdate();
			for (UniversalOverrideReservation reservation: getHibSession().createQuery(
					"select distinct r from UniversalOverrideReservation r inner join r.instructionalOffering.courseOfferings c where " +
					"c.isControl = true and c.subjectArea.subjectAreaAbbreviation in :subjectAbbvs and c.subjectArea.department.session.uniqueId = :sessionId", UniversalOverrideReservation.class)
					.setParameterList("subjectAbbvs", subjectAbbvs, String.class)
					.setParameter("sessionId", rollForwardSessionForm.getSessionToRollReservationsForwardFrom())
					.list()) {
				UniversalOverrideReservation toReservation = rollUniversalReservationForward(reservation, toSession, startDate, expiration);
				if (toReservation != null)
					getHibSession().persist(toReservation);
			}
		}
		
		getHibSession().flush();
	}
	
	protected boolean rollReservationForward(Reservation fromReservation, Reservation toReservation, Session toSession, Date start, Date expiration) {
		InstructionalOffering toOffering = InstructionalOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromReservation.getInstructionalOffering().getUniqueId());
		if (toOffering == null) {
			CourseOffering toCourse = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromReservation.getInstructionalOffering().getControllingCourseOffering().getUniqueId());
			if (toCourse == null){
		    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
		    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(
		    				toSession.getUniqueId(),
		    				fromReservation.getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation(),
		    				fromReservation.getInstructionalOffering().getControllingCourseOffering().getCourseNbr());
		    	} else {
		    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(
		    				toSession.getUniqueId(),
		    				fromReservation.getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getSubjectAreaAbbreviation(),
		    				fromReservation.getInstructionalOffering().getControllingCourseOffering().getCourseNbr(),
		    				fromReservation.getInstructionalOffering().getControllingCourseOffering().getTitle());
		    	}
			}
			if (toCourse != null) toOffering = toCourse.getInstructionalOffering();
		}
		if (toOffering == null) return false;
		toReservation.setInstructionalOffering(toOffering);
		
		if (fromReservation.getExpirationDate() != null) {
			if (expiration != null) {
				toReservation.setExpirationDate(expiration);
			} else {
				int nrDays = (int)Math.round((fromReservation.getExpirationDate().getTime() - fromReservation.getInstructionalOffering().getSession().getSessionBeginDateTime().getTime()) / (1000.0 * 60 * 60 * 24));
				Calendar cal = Calendar.getInstance();
				cal.setTime(toSession.getSessionBeginDateTime());
				cal.add(Calendar.DAY_OF_YEAR, nrDays);
				toReservation.setExpirationDate(cal.getTime());
			}
		}
		if (fromReservation.getStartDate() != null) {
			if (start != null) {
				toReservation.setStartDate(start);
			} else {
				int nrDays = (int)Math.round((fromReservation.getStartDate().getTime() - fromReservation.getInstructionalOffering().getSession().getSessionBeginDateTime().getTime()) / (1000.0 * 60 * 60 * 24));
				Calendar cal = Calendar.getInstance();
				cal.setTime(toSession.getSessionBeginDateTime());
				cal.add(Calendar.DAY_OF_YEAR, nrDays);
				toReservation.setStartDate(cal.getTime());
			}
		}
		toReservation.setInclusive(fromReservation.isInclusive());;
		
		toReservation.setConfigurations(new HashSet<InstrOfferingConfig>());
		toReservation.setClasses(new HashSet<Class_>());
		for (InstrOfferingConfig fromConfig: fromReservation.getConfigurations()) {
			InstrOfferingConfig toConfig = InstrOfferingConfig.findByIdRolledForwardFrom(toSession.getUniqueId(), fromConfig.getUniqueId());
			if (toConfig == null) {
				for (InstrOfferingConfig c: toOffering.getInstrOfferingConfigs()) {
					if (c.getName().equals(fromConfig.getName())) {
						toConfig = c; break;
					}
				}
			}
			if (toConfig != null)
				toReservation.getConfigurations().add(toConfig);
		}
		for (Class_ fromClass: fromReservation.getClasses()) {
			Class_ toClass = Class_.findByIdRolledForwardFrom(toSession.getUniqueId(), fromClass.getUniqueId());
			if (toClass == null && fromClass.getExternalUniqueId() != null) {
				Class_ c = Class_.findByExternalId(toSession.getUniqueId(), fromClass.getExternalUniqueId());
				if (c != null && c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().equals(toOffering)) {
					toClass = c;
				}
			}
			if (toClass == null) {
				configs: for (InstrOfferingConfig toConfig: toOffering.getInstrOfferingConfigs()) {
					for (SchedulingSubpart toSubpart: toConfig.getSchedulingSubparts()) {
						if (toSubpart.getSchedulingSubpartSuffix().equals(fromClass.getSchedulingSubpart().getSchedulingSubpartSuffix())) {
							for (Class_ c: toSubpart.getClasses()) {
								if (c.getSectionNumber().equals(fromClass.getSectionNumber())) {
									toClass = c; break configs;
								}
							}
						}
					}
				}
			}
			if (toClass != null)
				toReservation.getClasses().add(toClass);
		}
		
		toReservation.setLimit(fromReservation.getLimit());
		return true;
	}
	
	protected CourseReservation rollCourseReservationForward(CourseReservation fromReservation, Session toSession, Date startDate, Date expiration) {
		CourseReservation toReservation = new CourseReservation();
		
		if (!rollReservationForward(fromReservation, toReservation, toSession, startDate, expiration)) return null;
		
		CourseOffering toCourse = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromReservation.getCourse().getUniqueId());
		if (toCourse == null){
	    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
	    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(
	    				toSession.getUniqueId(),
	    				fromReservation.getCourse().getSubjectArea().getSubjectAreaAbbreviation(),
	    				fromReservation.getCourse().getCourseNbr());
	    	} else {
	    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(
	    				toSession.getUniqueId(),
	    				fromReservation.getCourse().getSubjectArea().getSubjectAreaAbbreviation(),
	    				fromReservation.getCourse().getCourseNbr(),
	    				fromReservation.getCourse().getTitle());
	    	}
		}
		
		if (toCourse == null || !toCourse.getInstructionalOffering().equals(toReservation.getInstructionalOffering())) return null;
		toReservation.setCourse(toCourse);
		
		return toReservation;
	}
	
	protected CurriculumReservation rollCurriculumReservationForward(CurriculumReservation fromReservation, Session toSession, Date startDate, Date expiration,
			Map<String, AcademicArea> areas,
			Map<String, AcademicClassification> classifications,
			Map<String, Map<String, PosMajor>> majors,
			Map<String, Map<String, Map<String, PosMajorConcentration>>> concentrations,
			Map<String, Map<String, PosMinor>> minors) {
		CurriculumReservation toReservation = new CurriculumReservation();
		if (fromReservation instanceof CurriculumOverrideReservation) {
			toReservation = new CurriculumOverrideReservation();
			((CurriculumOverrideReservation)toReservation).setFlags(((CurriculumOverrideReservation)fromReservation).getFlags());
		}
	
		if (!rollReservationForward(fromReservation, toReservation, toSession, startDate, expiration)) return null;
		
		toReservation.setAreas(new HashSet<AcademicArea>());
		for (AcademicArea fromArea: fromReservation.getAreas()) {
			AcademicArea toArea = areas.get(fromArea.getAcademicAreaAbbreviation());
			if (toArea != null) toReservation.getAreas().add(toArea);
		}
		if (toReservation.getAreas().isEmpty()) return null;
		
		toReservation.setClassifications(new HashSet<AcademicClassification>());
		for (AcademicClassification fromClasf: fromReservation.getClassifications()) {
			AcademicClassification toClasf = classifications.get(fromClasf.getCode());
			if (toClasf != null) toReservation.getClassifications().add(toClasf);
		}
		
		toReservation.setMajors(new HashSet<PosMajor>());
		toReservation.setConcentrations(new HashSet<PosMajorConcentration>());
		toReservation.setMinors(new HashSet<PosMinor>());
		for (AcademicArea area: fromReservation.getAreas()) {
			Map<String, PosMajor> mj = majors.get(area.getAcademicAreaAbbreviation());
			Map<String, Map<String, PosMajorConcentration>> mj2cc = concentrations.get(area.getAcademicAreaAbbreviation());
			if (mj != null)
				for (PosMajor fromMajor: fromReservation.getMajors()) {
					PosMajor toMajor = mj.get(fromMajor.getCode());
					if (toMajor != null) toReservation.getMajors().add(toMajor);
				}
			if (mj2cc != null)
				for (PosMajorConcentration fromConc: fromReservation.getConcentrations()) {
					Map<String, PosMajorConcentration> cc = mj2cc.get(fromConc.getMajor().getCode());
					PosMajorConcentration toConc = (cc == null ? null : cc.get(fromConc.getCode()));
					if (toConc != null) toReservation.getConcentrations().add(toConc);
				}
			Map<String, PosMinor> mn = minors.get(area.getAcademicAreaAbbreviation());
			if (mn != null)
				for (PosMinor fromMinor: fromReservation.getMinors()) {
					PosMinor toMinor = mn.get(fromMinor.getCode());
					if (toMinor != null) toReservation.getMinors().add(toMinor);
				}
		}
		
		return toReservation;
	}
	
	protected UniversalOverrideReservation rollUniversalReservationForward(UniversalOverrideReservation fromReservation, Session toSession, Date startDate, Date expiration) {
		UniversalOverrideReservation toReservation = new UniversalOverrideReservation();
		toReservation.setFlags(fromReservation.getFlags());
		toReservation.setFilter(fromReservation.getFilter());

		if (!rollReservationForward(fromReservation, toReservation, toSession, startDate, expiration)) return null;
		
		return toReservation;
	}
	
	protected StudentGroupReservation rollGroupReservationForward(StudentGroupReservation fromReservation, Session toSession, Date startDate, Date expiration, Map<String, StudentGroup> groups, boolean createStudentGroupIfNeeded) {
		StudentGroupReservation toReservation = new StudentGroupReservation();
		if (fromReservation instanceof GroupOverrideReservation) {
			toReservation = new GroupOverrideReservation();
			((GroupOverrideReservation)toReservation).setFlags(((GroupOverrideReservation)fromReservation).getFlags());
		}
		if (fromReservation instanceof LearningCommunityReservation) {
			CourseOffering fromCourse = ((LearningCommunityReservation)fromReservation).getCourse();
			CourseOffering toCourse = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromCourse.getUniqueId());
			if (toCourse == null){
		    	if (ApplicationProperty.CourseOfferingNumberMustBeUnique.isTrue()) {
		    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbr(
		    				toSession.getUniqueId(),
		    				fromCourse.getSubjectArea().getSubjectAreaAbbreviation(),
		    				fromCourse.getCourseNbr());
		    	} else {
		    		toCourse = CourseOffering.findBySessionSubjAreaAbbvCourseNbrTitle(
		    				toSession.getUniqueId(),
		    				fromCourse.getSubjectArea().getSubjectAreaAbbreviation(),
		    				fromCourse.getCourseNbr(),
		    				fromCourse.getTitle());
		    	}
			}
			
			if (toCourse == null) return null;
			toReservation = new LearningCommunityReservation();
			((LearningCommunityReservation)toReservation).setCourse(toCourse);
		}
		
		if (!rollReservationForward(fromReservation, toReservation, toSession, startDate, expiration)) return null;
		
		StudentGroup group = groups.get(fromReservation.getGroup().getGroupAbbreviation());
		if (group == null) {
			if (createStudentGroupIfNeeded) {
				group = new StudentGroup();
				group.setSession(toSession);
				group.setExternalUniqueId(fromReservation.getGroup().getExternalUniqueId());
				group.setGroupAbbreviation(fromReservation.getGroup().getGroupAbbreviation());
				group.setGroupName(fromReservation.getGroup().getGroupName());
				group.setExpectedSize(fromReservation.getGroup().getExpectedSize());
				group.setType(fromReservation.getGroup().getType());
				getHibSession().persist(group);
				groups.put(group.getGroupAbbreviation(), group);
			} else {
				return null;
			}
		}
		toReservation.setGroup(group);
		
		return toReservation;
	}
	
	public void rollTeachingRequestsForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		for (String subjectId: rollForwardSessionForm.getRollForwardTeachingRequestsSubjectIds()) {
			Transaction tx = getHibSession().beginTransaction();
			try {
				SubjectArea toSubjectArea = SubjectAreaDAO.getInstance().get(Long.parseLong(subjectId));
				iLog.info("Rolling forward teaching requests for:  " + toSubjectArea.getSubjectAreaAbbreviation());
				Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
				if (rollForwardSessionForm.validateTeachingRequestsRollForward(toSession, toSubjectArea, errors)) {
					for (InstructionalOffering toInstructionalOffering: getHibSession().createQuery(
							"select co.instructionalOffering from CourseOffering co where co.isControl = true and co.subjectArea.uniqueId = :subjectAreaId and co.instructionalOffering.uniqueIdRolledForwardFrom is not null", InstructionalOffering.class)
							.setParameter("subjectAreaId", toSubjectArea.getUniqueId()).list()) {
						InstructionalOffering fromInstructionalOffering = InstructionalOfferingDAO.getInstance().get(toInstructionalOffering.getUniqueIdRolledForwardFrom());
						if (fromInstructionalOffering == null) continue;
						if (toInstructionalOffering.getTeachingRequests() != null)
							for (Iterator<TeachingRequest> it = toInstructionalOffering.getTeachingRequests().iterator(); it.hasNext(); ) {
								getHibSession().remove(it.next()); it.remove();
							}
						for (TeachingRequest fromRequest: fromInstructionalOffering.getTeachingRequests()) {
							TeachingRequest toRequest = new TeachingRequest();
							toRequest.setOffering(toInstructionalOffering);
							toRequest.setAssignCoordinator(fromRequest.isAssignCoordinator());
							toRequest.setPercentShare(fromRequest.getPercentShare());
							toRequest.setClassRequests(new HashSet<TeachingClassRequest>());
							toRequest.setNbrInstructors(fromRequest.getNbrInstructors());
							toRequest.setResponsibility(fromRequest.getResponsibility());
							toRequest.setSameCommonPart(fromRequest.getSameCommonPart());
							toRequest.setSameCoursePreference(fromRequest.getSameCoursePreference());
							toRequest.setTeachingLoad(fromRequest.getTeachingLoad());
							boolean valid = toRequest.isAssignCoordinator();
							for (TeachingClassRequest fromTCR: fromRequest.getClassRequests()) {
								Class_ toClass = Class_.findByIdRolledForwardFrom(toSubjectArea.getDepartment().getSessionId(), fromTCR.getTeachingClass().getUniqueId());
								if (toClass == null) continue;
								TeachingClassRequest toTCR = new TeachingClassRequest();
								toTCR.setAssignInstructor(fromTCR.isAssignInstructor());
								toTCR.setCanOverlap(fromTCR.isCanOverlap());
								toTCR.setCommon(fromTCR.isCommon());
								toTCR.setLead(fromTCR.isLead());
								toTCR.setPercentShare(fromTCR.getPercentShare());
								toTCR.setTeachingRequest(toRequest);
								toTCR.setTeachingClass(toClass);
								toRequest.addToClassRequests(toTCR);
								if (toTCR.isAssignInstructor()) valid = true;
							}
							if (valid) {
								toInstructionalOffering.addToTeachingRequests(toRequest);
								getHibSession().persist(toRequest);
								rollForwardInstructorPrefs(fromRequest, toRequest, toSubjectArea.getDepartment().getSession());
								rollForwardInstructorAttributePrefs(fromRequest, toRequest, toSubjectArea.getDepartment().getSession());
							}
						}
					}
				} else {
					break;
				}
				tx.commit();
			} catch (Exception e) {
				tx.rollback();
				iLog.error(MSG.errorRollForwardFailedAll(MSG.rollForwardOfferingCoordinators()), e);
				errors.addFieldError("rollForward", e.getMessage());
				break;
			}
			getHibSession().clear();
		}
	}
	
	private String convertParameter(String type, String value, RollForwardSessionForm form) {
		if (value == null || value.isEmpty()) return value;
		for (SavedHQL.Option option: SavedHQL.Option.values()) {
			if (type.equalsIgnoreCase(option.name()))
				return option.rollForward(getHibSession(), value, form.getSessionToRollPeriodicTasksFrom(), form.getSessionToRollForwardTo());
		}
		return value;
	}
	
	public void rollPeriodicTasksForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Set<String> existing = new HashSet<String>();
		for (PeriodicTask task: getHibSession().createQuery("from PeriodicTask where session.uniqueId = :sessionId", PeriodicTask.class)
				.setParameter("sessionId", rollForwardSessionForm.getSessionToRollForwardTo()).list()) {
			existing.add(task.getName());
		}

		Date now = new Date();
		Date firstDate = DateUtils.getDate(1, toSession.getStartMonth() - ApplicationProperty.DatePatternNrExessMonth.intValue(), toSession.getSessionStartYear());
		Date lastDate = DateUtils.getDate(1, toSession.getEndMonth() + ApplicationProperty.DatePatternNrExessMonth.intValue(), toSession.getSessionStartYear());
		for (PeriodicTask original: getHibSession().createQuery("from PeriodicTask where session.uniqueId = :sessionId", PeriodicTask.class)
				.setParameter("sessionId", rollForwardSessionForm.getSessionToRollPeriodicTasksFrom()).list()) {
			if (existing.contains(original.getName())) continue;
			PeriodicTask task = new PeriodicTask();
			task = new PeriodicTask();
			task.setName(original.getName());
			task.setInputFile(original.getInputFile());
			task.setEmail(original.getEmail());
			task.setOwner(original.getOwner());
			task.setScript(original.getScript());
			task.setSession(toSession);
			task.setParameters(new HashSet<TaskParameter>());
			task.setSchedule(new HashSet<TaskExecution>());
			for (TaskParameter originalParameter: original.getParameters()) {
				TaskParameter parameter = new TaskParameter();
				parameter.setName(originalParameter.getName());
				parameter.setValue(originalParameter.getValue());
				for (ScriptParameter sp: task.getScript().getParameters()) {
					if (sp.getName().equals(originalParameter.getName()))  {
						parameter.setValue(convertParameter(sp.getType(), originalParameter.getValue(), rollForwardSessionForm));
					}
				}
				if (parameter.getValue() == null) continue;
				parameter.setTask(task);
				task.getParameters().add(parameter);
			}
			for (TaskExecution originalExecution: original.getSchedule()) {
				Date date = SaveTaskBackend.getScheduleDate(toSession, originalExecution.getExecutionDate(), originalExecution.getExecutionPeriod());
				if (date.before(now)) continue;
				if (date.before(firstDate) || !date.before(lastDate)) continue;
				TaskExecution execution = new TaskExecution();
				execution.setCreatedDate(now);
				execution.setExecutionDate(originalExecution.getExecutionDate());
				execution.setExecutionPeriod(originalExecution.getExecutionPeriod());
				execution.setExecutionStatus(ExecutionStatus.CREATED.ordinal());
				execution.setScheduledDate(date);
				execution.setTask(task);
				task.getSchedule().add(execution);
			}
			if (task.getSchedule().isEmpty()) continue;
			getHibSession().persist(task);
		}
		getHibSession().flush();
	}
	
	public void rollLearningManagementSystemInfoForward(RollForwardErrors errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDatePatternsForwardFrom());
		List<LearningManagementSystemInfo> fromLearningManagementSystems = LearningManagementSystemInfo.findAll(fromSession.getUniqueId());
		LearningManagementSystemInfo fromLms = null;
		LearningManagementSystemInfo toLms = null;
		for(Iterator it = fromLearningManagementSystems.iterator(); it.hasNext();){
			fromLms = (LearningManagementSystemInfo) it.next();
			if (fromLms != null){
				toLms = (LearningManagementSystemInfo) fromLms.clone();
				toLms.setSession(toSession);
				getHibSession().persist(toLms);
			}
		}
		getHibSession().flush();		
	}


	/**
	 * @return the subpartTimeRollForward
	 */
	public boolean isSubpartTimeRollForward() {
		return subpartTimeRollForward;
	}

	/**
	 * @return the subpartLocationRollForward
	 */
	public boolean isSubpartLocationRollForward() {
		return subpartLocationRollForward;
	}

	/**
	 * @return the classPrefsPushUp
	 */
	public boolean isClassPrefsPushUp() {
		return classPrefsPushUp;
	}
	
	public boolean isClassRollForward() {
		return classRollForward;
	}
	
	public boolean isRollForwardDistributions() {
		return rollForwardDistributions != DistributionMode.NONE;
	}
	
	public boolean isRollForwardDistributions(DistributionPref dp) {
		switch (rollForwardDistributions) {
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
	
	public CancelledClassAction getCancelledClassAction() {
		return cancelledClassAction;
	}
	
	public boolean isResetClassSuffix() {
		return resetClassSuffix;
	}
}
