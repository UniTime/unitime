/**
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.Debug;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.BuildingPref;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.DistributionObject;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomGroupPref;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseCatalogDAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;
import org.unitime.timetable.model.dao.ExternalBuildingDAO;
import org.unitime.timetable.model.dao.ExternalRoomDAO;
import org.unitime.timetable.model.dao.ExternalRoomDepartmentDAO;
import org.unitime.timetable.model.dao.ExternalRoomFeatureDAO;
import org.unitime.timetable.model.dao.GlobalRoomFeatureDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.NonUniversityLocationDAO;
import org.unitime.timetable.model.dao.RoomDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomGroupDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;


/**
 * @author Stephanie Schluttenhofer
 *
 */
public class SessionRollForward {
	private static HashMap roomList;
	private static HashMap sessionHasCourseCatalogList;
	private static HashMap sessionHasExternalBuildingList;
	private static HashMap sessionHasExternalRoomList;
	private static HashMap sessionHasExternalRoomDeptList;
	private static HashMap sessionHasExternalRoomFeatureList;


	public void rollBuildingAndRoomDataForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollRoomDataForwardFrom());

		rollRoomFeaturesForward(errors, fromSession, toSession);
		rollRoomGroupsForward(errors, fromSession, toSession);
		rollBuildingsForward(errors, fromSession, toSession);
		rollLocationsForward(errors, fromSession, toSession);
		(new SessionDAO()).getSession().clear();
	}

	private void rollRoomGroupsForward(ActionMessages errors, Session fromSession, Session toSession) {
		RoomGroup fromRoomGroup = null;
		RoomGroup toRoomGroup = null;
		RoomGroupDAO rgDao = new RoomGroupDAO();
		RoomDAO rDao = new RoomDAO();
		NonUniversityLocationDAO nulDao = new NonUniversityLocationDAO();
		Collection fromRoomGroups = RoomGroup.getAllRoomGroupsForSession(fromSession);
		try {
			if (fromRoomGroups != null && !fromRoomGroups.isEmpty()){
				for (Iterator it = fromRoomGroups.iterator(); it.hasNext();){
					fromRoomGroup = (RoomGroup) it.next();
					if (fromRoomGroup != null){
						if(!fromRoomGroup.isGlobal().booleanValue()){
							toRoomGroup = (RoomGroup) fromRoomGroup.clone();
							toRoomGroup.setSession(toSession);
							toRoomGroup.setDepartment(fromRoomGroup.getDepartment().findSameDepartmentInSession(toSession));
							rgDao.saveOrUpdate(toRoomGroup);
						}
					}
				}
			}
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Room Groups", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all room groups forward."));
		}
	}

	private void rollRoomFeaturesForward(ActionMessages errors, Session fromSession, Session toSession) {
		DepartmentRoomFeature fromRoomFeature = null;
		DepartmentRoomFeature toRoomFeature = null;
		RoomFeatureDAO rfDao = new RoomFeatureDAO();
		RoomDAO rDao = new RoomDAO();
		NonUniversityLocationDAO nulDao = new NonUniversityLocationDAO();
		Collection fromRoomFeatures = DepartmentRoomFeature.getAllRoomFeaturesForSession(fromSession);
		try{
			if (fromRoomFeatures != null && !fromRoomFeatures.isEmpty()){
				for(Iterator it = fromRoomFeatures.iterator(); it.hasNext();){
					fromRoomFeature = (DepartmentRoomFeature) it.next();
					if (fromRoomFeature != null){
						toRoomFeature = (DepartmentRoomFeature)fromRoomFeature.clone();
						toRoomFeature.setDepartment(fromRoomFeature.getDepartment().findSameDepartmentInSession(toSession));
						rfDao.saveOrUpdate(toRoomFeature);
					}
				}
			}
			if (sessionHasExternalRoomFeatureList(toSession)){
				GlobalRoomFeatureDAO grfDao = new GlobalRoomFeatureDAO();
				GlobalRoomFeature grf = null;
				List newGlobalFeatures = grfDao.getQuery("select distinct erf.value, erf.name from ExternalRoomFeature erf" +
					" where erf.room.building.session.uniqueId=:sessionId" +
					"  and erf.value not in (select grf.label from GlobalRoomFeature grf)")
					.setLong("sessionId", toSession.getUniqueId())
					.list();
				if (newGlobalFeatures != null){
					String newLabel = null;
					String newSisReference = null;
					for (Iterator nrfIt = newGlobalFeatures.iterator(); nrfIt.hasNext();){
						List l = (List) nrfIt.next();
						if (l != null && l.size() == 2){
							newLabel = (String) l.get(0);
							newSisReference = (String) l.get(1);
							grf = new GlobalRoomFeature();
							grf.setLabel(newLabel);
							grf.setSisReference(newSisReference);
							grf.setSisValue(null);
							grfDao.saveOrUpdate(grf);
						}
						
					}
				}
				
			}
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Room Features", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all room features forward."));
		}	
	}

	private void rollRoomFeaturesForLocationForward(Location fromLocation, Location toLocation, Session toSession, HashMap roomFeatureCache){
		if(fromLocation.getFeatures() != null && !fromLocation.getFeatures().isEmpty()){
			RoomFeature fromFeature = null;
			GlobalRoomFeature toGlobalFeature = null;
			DepartmentRoomFeature toDepartmentFeature = null;
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
								toGlobalFeature = GlobalRoomFeature.findGlobalRoomFeatureForLabel(erf.getValue());
								toLocation.addTofeatures(toGlobalFeature);
							}
						}
					}
				}
			}
			for(Iterator rfIt = fromLocation.getFeatures().iterator(); rfIt.hasNext();){
				fromFeature = (RoomFeature) rfIt.next();
				if (fromFeature instanceof GlobalRoomFeature && rollGlobalFeaturesFromFromLocation) {
					GlobalRoomFeature fromGlobalFeature = (GlobalRoomFeature) fromFeature;
					toLocation.addTofeatures(fromGlobalFeature);
					fromGlobalFeature.getRooms().add(toLocation);
				} else if (fromFeature instanceof DepartmentRoomFeature) {
					DepartmentRoomFeature fromDepartmentFeature = (DepartmentRoomFeature) fromFeature;
					toDepartmentFeature = (DepartmentRoomFeature) roomFeatureCache.get(fromDepartmentFeature);
					if (toDepartmentFeature == null){
						toDepartmentFeature = fromDepartmentFeature.findSameFeatureInSession(toSession);
						if (toDepartmentFeature != null){
							roomFeatureCache.put(fromDepartmentFeature, toDepartmentFeature);
							toLocation.addTofeatures(toDepartmentFeature);
							if (toDepartmentFeature.getRooms() == null){
								toDepartmentFeature.setRooms(new java.util.HashSet());
							}
							toDepartmentFeature.getRooms().add(toLocation);
						}
					}
				}
			}
		}
	}
	
	private void rollRoomForward(ActionMessages errors, Session fromSession, Session toSession, Location location) {
		Room fromRoom = null;
		Room toRoom = null;
		RoomDAO rDao = new RoomDAO();
		RoomDeptDAO rdDao = new RoomDeptDAO();
		DepartmentDAO dDao = new DepartmentDAO();
		Building toBuilding = null;
		RoomDept fromRoomDept = null;
		RoomDept toRoomDept = null;
		Department toDept = null;
		Department fromDept = null;
		HashMap roomFeatureCache = new HashMap();
		HashMap roomGroupCache = new HashMap();

		try {
			fromRoom = (Room) location;		
			
			if (fromRoom.getExternalUniqueId() != null &&sessionHasExternalRoomList(toSession)){
				ExternalRoom toExternalRoom = ExternalRoom.findExternalRoomForSession(fromRoom.getExternalUniqueId(), toSession);
				if (toExternalRoom != null) {
					toRoom = new Room();
					toRoom.setCapacity(toExternalRoom.getCapacity());
					toRoom.setExamCapacity(toExternalRoom.getExamCapacity());
					toRoom.setClassification(toExternalRoom.getClassification());
					toRoom.setCoordinateX(toExternalRoom.getCoordinateX());
					toRoom.setCoordinateY(toExternalRoom.getCoordinateY());
					toRoom.setDisplayName(toExternalRoom.getDisplayName());
					toRoom.setExternalUniqueId(toExternalRoom.getExternalUniqueId());
					toRoom.setIgnoreRoomCheck(fromRoom.isIgnoreRoomCheck());
					toRoom.setIgnoreTooFar(fromRoom.isIgnoreTooFar());
					toRoom.setPattern(fromRoom.getPattern());
					toRoom.setRoomNumber(toExternalRoom.getRoomNumber());
					toRoom.setScheduledRoomType(toExternalRoom.getScheduledRoomType());
					LocationPermIdGenerator.setPermanentId(toRoom);
				} else {
					return;
				}
			} else {
				toRoom = (Room)fromRoom.clone();
			}
			toRoom.setSession(toSession);
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
				rDao.saveOrUpdate(toRoom);
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
							rollForwardRoomDept(fromRoomDept, toRoom, toSession);
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
							rollForwardRoomDept(fromRoomDept, toRoom, toSession);
						} else {
							toRoom.addExternalRoomDept(toExternalRoomDept, toExternalRoom.getRoomDepartments());
						}
					}
				}
				rDao.saveOrUpdate(toRoom);
				rDao.getSession().flush();
				rDao.getSession().evict(toRoom);
				rDao.getSession().evict(fromRoom);
			}								
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Rooms", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all rooms forward."));
		}
	}

	private void rollForwardRoomDept(RoomDept fromRoomDept, Room toRoom, Session toSession){
		Department toDept = fromRoomDept.getDepartment().findSameDepartmentInSession(toSession);
		RoomDept toRoomDept = null;
		RoomDeptDAO rdDao = new RoomDeptDAO();
		if (toDept != null){
			toRoomDept = new RoomDept();
			toRoomDept.setRoom(toRoom);
			toRoomDept.setControl(fromRoomDept.isControl());
			toRoomDept.setDepartment(toDept);
			toRoom.addToroomDepts(toRoomDept);
			toDept.addToroomDepts(toRoomDept);
			rdDao.saveOrUpdate(toRoomDept);
		}
	}

	
	private void rollRoomGroupsForLocationForward(Location fromLocation, Location toLocation, Session toSession, HashMap roomGroupCache) {
		if(fromLocation.getRoomGroups() != null && !fromLocation.getRoomGroups().isEmpty()){
			RoomGroup fromRoomGroup = null;
			RoomGroup toDepartmentRoomGroup = null;
			for(Iterator rfIt = fromLocation.getRoomGroups().iterator(); rfIt.hasNext();){
				fromRoomGroup = (RoomGroup) rfIt.next();
				if (fromRoomGroup.isGlobal().booleanValue()) {					
					if (toLocation.getRoomGroups() == null){
						toLocation.setRoomGroups(new java.util.HashSet());
					}
					toLocation.getRoomGroups().add(fromRoomGroup);
					fromRoomGroup.getRooms().add(toLocation);
				} else {
					toDepartmentRoomGroup = (RoomGroup) roomGroupCache.get(fromRoomGroup);
					if (toDepartmentRoomGroup == null){
						toDepartmentRoomGroup = fromRoomGroup.findSameRoomGroupInSession(toSession);
						if (toDepartmentRoomGroup != null){
							roomGroupCache.put(fromRoomGroup, toDepartmentRoomGroup);
							if (toLocation.getRoomGroups() == null){
								toLocation.setRoomGroups(new java.util.HashSet());
							}
							toLocation.getRoomGroups().add(toDepartmentRoomGroup);
							if (toDepartmentRoomGroup.getRooms() == null){
								toDepartmentRoomGroup.setRooms(new java.util.HashSet());
							}
							toDepartmentRoomGroup.getRooms().add(toLocation);
						}
						
					}
				}
			}		
		}
		
	}

	private void rollNonUniversityLocationsForward(ActionMessages errors, Session fromSession, Session toSession, Location location) {
		NonUniversityLocation fromNonUniversityLocation = null;
		NonUniversityLocation toNonUniversityLocation = null;
		NonUniversityLocationDAO nulDao = new NonUniversityLocationDAO();
		RoomDeptDAO rdDao = new RoomDeptDAO();
		DepartmentDAO dDao = new DepartmentDAO();
		Building toBuilding = null;
		RoomDept fromRoomDept = null;
		RoomDept toRoomDept = null;
		Department toDept = null;
		Department fromDept = null;
		HashMap roomFeatureCache = new HashMap();
		HashMap roomGroupCache = new HashMap();

		try {
			fromNonUniversityLocation = (NonUniversityLocation) location;					
			toNonUniversityLocation = (NonUniversityLocation)fromNonUniversityLocation.clone();
			toNonUniversityLocation.setSession(toSession);
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
			nulDao.saveOrUpdate(toNonUniversityLocation);
			if (fromNonUniversityLocation.getRoomDepts() != null && !fromNonUniversityLocation.getRoomDepts().isEmpty()){
				for (Iterator deptIt = fromNonUniversityLocation.getRoomDepts().iterator(); deptIt.hasNext();){
					fromRoomDept = (RoomDept)deptIt.next();
					toDept = fromRoomDept.getDepartment().findSameDepartmentInSession(toSession);
					if (toDept != null){
						toRoomDept = new RoomDept();
						toRoomDept.setRoom(toNonUniversityLocation);
						toRoomDept.setControl(fromRoomDept.isControl());
						toRoomDept.setDepartment(toDept);
						toNonUniversityLocation.addToroomDepts(toRoomDept);
						toDept.addToroomDepts(toRoomDept);
						rdDao.saveOrUpdate(toRoomDept);
					}
				}
				nulDao.saveOrUpdate(toNonUniversityLocation);
				nulDao.getSession().flush();
				nulDao.getSession().evict(toNonUniversityLocation);
				nulDao.getSession().evict(fromNonUniversityLocation);
			}					
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Non University Locations", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all non university locations forward."));
		}		
	}
	

	private void rollLocationsForward(ActionMessages errors, Session fromSession, Session toSession) {
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
	}


	private void rollBuildingsForward(ActionMessages errors, Session fromSession, Session toSession) {
		if (fromSession.getBuildings() != null && !fromSession.getBuildings().isEmpty()){
			try{
				Building fromBldg = null;
				Building toBldg = null;
				BuildingDAO bDao = new BuildingDAO();
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
					bDao.saveOrUpdate(toBldg);
					bDao.getSession().flush();
					bDao.getSession().evict(toBldg);
					bDao.getSession().evict(fromBldg);	
				}
			} catch (Exception e) {
				Debug.error(e.getStackTrace().toString());
				Debug.error(e.getMessage());
				errors.add("rollForward", new ActionMessage("errors.rollForward", "Buildings", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all buildings forward."));
			}
		}
		
	}

	public void rollManagersForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollManagersForwardFrom());
		Department fromDepartment = null;
		Department toDepartment = null;
		TimetableManagerDAO tmDao = new TimetableManagerDAO();
		try {
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
								tm.getDepartments().add(toDepartment);
								tmDao.saveOrUpdate(tm);
								tmDao.getSession().flush();
							}
						}
					}
				}
			}
			tmDao.getSession().flush();
			tmDao.getSession().clear();			
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Timetable Managers", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all timetable managers forward."));
		}
	}

	public void rollDepartmentsForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDeptsFowardFrom());
		Department fromDepartment = null;
		Department toDepartment = null;
		DepartmentDAO dDao = new DepartmentDAO();
		SolverGroup sg = null;
		try {
			for(Iterator it = fromSession.getDepartments().iterator(); it.hasNext();){
				fromDepartment = (Department) it.next();
				if (fromDepartment != null){
					toDepartment = (Department) fromDepartment.clone();
					toDepartment.setStatusType(null);
					toDepartment.setSession(toSession);
					toSession.addTodepartments(toDepartment);
					dDao.saveOrUpdate(toDepartment);
					if(fromDepartment.getSolverGroup() != null) {
						sg = SolverGroup.findBySessionIdName(toSession.getUniqueId(), fromDepartment.getSolverGroup().getName());
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
							SolverGroupDAO sgDao = new SolverGroupDAO();
							sgDao.saveOrUpdate(sg);
						}
					}

					if (fromDepartment.getDatePatterns() != null && !fromDepartment.getDatePatterns().isEmpty()){
						DatePattern fromDp = null;
						DatePattern toDp = null;
						for (Iterator dpIt = fromDepartment.getDatePatterns().iterator(); dpIt.hasNext();){
							fromDp = (DatePattern) dpIt.next();
							toDp = DatePattern.findByName(toSession, fromDp.getName());
							if (toDp == null){
								toDp = fromDp.findCloseMatchDatePatternInSession(toSession);
							}
							if (toDp != null){
								if (null == toDepartment.getDatePatterns()){
									toDepartment.setDatePatterns(new java.util.HashSet());
								}
								toDepartment.getDatePatterns().add(toDp);
							}
						}
					}
					if (fromDepartment.getTimePatterns() != null && !fromDepartment.getTimePatterns().isEmpty()){
						TimePattern fromTp = null;
						TimePattern toTp = null;
						for (Iterator dpIt = fromDepartment.getTimePatterns().iterator(); dpIt.hasNext();){
							fromTp = (TimePattern) dpIt.next();
							toTp = TimePattern.findByName(toSession, fromTp.getName());
							if (toTp == null){
								toTp = TimePattern.getMatchingTimePattern(toSession.getUniqueId(), fromTp);
							}
							if (toTp != null){
								if (null == toDepartment.getTimePatterns()){
									toDepartment.setTimePatterns(new java.util.HashSet());
								}
								toDepartment.getTimePatterns().add(toTp);
							}
						}
					}
					dDao.saveOrUpdate(toDepartment);
					DistributionTypeDAO dtDao = new DistributionTypeDAO();
					List l = dtDao.getQuery("select dt from DistributionType dt inner join dt.departments as d where d.uniqueId = " + fromDepartment.getUniqueId().toString()).list();
					if (l != null && !l.isEmpty()){
						DistributionType distributionType = null;
						for (Iterator dtIt = l.iterator(); dtIt.hasNext();){
							distributionType = (DistributionType) dtIt.next();
							distributionType.getDepartments().add(toDepartment);
							dtDao.saveOrUpdate(distributionType);
						}
					}
	
					dDao.getSession().flush();
					dDao.getSession().evict(toDepartment);
					dDao.getSession().evict(fromDepartment);
				}
			}
			dDao.getSession().flush();
			dDao.getSession().clear();
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Departments", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all departments forward."));
		}

	}

	public void rollDatePatternsForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollDatePatternsForwardFrom());
		Vector fromDatePatterns = DatePattern.findAll(fromSession, true, null, null);
		DatePattern fromDatePattern = null;
		DatePattern toDatePattern = null;
		DatePatternDAO dpDao = new DatePatternDAO();
		try {
			for(Iterator it = fromDatePatterns.iterator(); it.hasNext();){
				fromDatePattern = (DatePattern) it.next();
				if (fromDatePattern != null){
					toDatePattern = (DatePattern) fromDatePattern.clone();
					toDatePattern.setSession(toSession);
					dpDao.saveOrUpdate(toDatePattern);
					dpDao.getSession().flush();
				}
			}
			if (fromSession.getDefaultDatePattern() != null){
				DatePattern defDp = DatePattern.findByName(toSession, fromSession.getDefaultDatePattern().getName());
				if (defDp != null){
					toSession.setDefaultDatePattern(defDp);
					SessionDAO sDao = new SessionDAO();
					sDao.saveOrUpdate(toSession);
				}
			}
			dpDao.getSession().flush();
			dpDao.getSession().clear();
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Date Patterns", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all date patterns forward."));
		}		
	}

	public void rollSubjectAreasForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollSubjectAreasForwardFrom());
		SubjectArea toSubjectArea = null;
		SubjectArea fromSubjectArea = null;
		SubjectAreaDAO sDao = new SubjectAreaDAO();
		Department toDepartment = null;
		try {
			if (sessionHasCourseCatalog(toSession)) {
				CourseCatalogDAO ccDao = new CourseCatalogDAO();
				List subjects = ccDao.getQuery("select distinct cc.subject, cc.previousSubject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject != null")
					.setLong("sessionId", toSession.getUniqueId())
					.list();
				if (subjects != null){
					String toSubject = null;
					String fromSubject = null;
					Object[] subjectInfo = null;
					for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
						subjectInfo = (Object[]) saIt.next();
						if (subjectInfo != null && subjectInfo.length == 2){
							toSubject = (String) subjectInfo[0];
							fromSubject = (String) subjectInfo[1];							
							fromSubjectArea = SubjectArea.findByAbbv(fromSession.getUniqueId(), fromSubject);
							if (fromSubjectArea == null){
								continue;
							}
							toSubjectArea = (SubjectArea)fromSubjectArea.clone();
							if (!toSubject.equals(fromSubject)){
								toSubjectArea.setSubjectAreaAbbreviation(toSubject);
							}
							toSubjectArea.setSession(toSession);
							toSession.addTosubjectAreas(toSubjectArea);
							if (fromSubjectArea.getDepartment() != null) {
								toDepartment = fromSubjectArea.getDepartment().findSameDepartmentInSession(toSession);
								if (toDepartment != null){
									toSubjectArea.setDepartment(toDepartment);
									toDepartment.addTosubjectAreas(toSubjectArea);
									sDao.saveOrUpdate(toSubjectArea);
									sDao.getSession().flush();
									sDao.getSession().evict(toSubjectArea);
									sDao.getSession().evict(fromSubjectArea);
								}
							}
						}
					}
				}
				List pseudoSubjects = sDao.getQuery("from SubjectArea sa where sa.session=:fromSessionId and sa.pseudoSubjectArea = 1 and sa.subjectAreaAbbreviation not in (select cc.subject from CourseCatalog cc where cc.session.uniqueId=:toSessionId)")
					.setLong("fromSessionId", fromSession.getUniqueId())
					.setLong("toSessionId", toSession.getUniqueId())
					.list();
				if (pseudoSubjects != null){
					for(Iterator it = pseudoSubjects.iterator(); it.hasNext();){
						fromSubjectArea = (SubjectArea) it.next();
						if (fromSubjectArea != null){
							toSubjectArea = (SubjectArea)fromSubjectArea.clone();
							toSubjectArea.setSession(toSession);
							toSession.addTosubjectAreas(toSubjectArea);
							if (fromSubjectArea.getDepartment() != null) {
								toDepartment = fromSubjectArea.getDepartment().findSameDepartmentInSession(toSession);
								if (toDepartment != null){
									toSubjectArea.setDepartment(toDepartment);
									toDepartment.addTosubjectAreas(toSubjectArea);
									sDao.saveOrUpdate(toSubjectArea);
									sDao.getSession().flush();
									sDao.getSession().evict(toSubjectArea);
									sDao.getSession().evict(fromSubjectArea);
								}
							}
						}
					}
				}
				List newSubjects = ccDao.getQuery("select distinct subject from CourseCatalog cc where cc.session.uniqueId=:sessionId and cc.previousSubject = null and cc.subject not in (select sa.subjectAreaAbbreviation from SubjectArea sa where sa.session.uniqueId=:sessionId)")
					.setLong("sessionId", toSession.getUniqueId())
					.list();
				toDepartment = Department.findByDeptCode("TEMP", toSession.getUniqueId());
				if (toDepartment == null){
					toDepartment = new Department();
					toDepartment.setAbbreviation("TEMP");
					toDepartment.setAllowReqRoom(new Boolean(false));
					toDepartment.setAllowReqTime(new Boolean(false));
					toDepartment.setDeptCode("TEMP");
					toDepartment.setExternalManager(new Boolean(false));
					toDepartment.setExternalUniqueId(null);
					toDepartment.setName("Temp Department For New Subjects");
					toDepartment.setSession(toSession);
					toDepartment.setDistributionPrefPriority(new Integer(0));
					toSession.addTodepartments(toDepartment);
					DepartmentDAO.getInstance().saveOrUpdate(toDepartment);
				}
				String toSubject = null;
				for (Iterator saIt = newSubjects.iterator(); saIt.hasNext();){
					toSubject = (String) saIt.next();
					if (toSubject != null){
						toSubjectArea = new SubjectArea();
						toSubjectArea.setDepartment(toDepartment);
						toSubjectArea.setLongTitle("New Subject - Please Name Me");
						toSubjectArea.setPseudoSubjectArea(new Boolean(false));
						toSubjectArea.setScheduleBookOnly(new Boolean(false));
						toSubjectArea.setSession(toSession);
						toSubjectArea.setShortTitle("New Subject");
						toSubjectArea.setSubjectAreaAbbreviation(toSubject);
						toDepartment.addTosubjectAreas(toSubjectArea);
						toSession.addTosubjectAreas(toSubjectArea);
						sDao.saveOrUpdate(toSubjectArea);
						sDao.getSession().flush();
						sDao.getSession().evict(toSubjectArea);
						sDao.getSession().evict(fromSubjectArea);
					}
				}
			} else if (fromSession.getSubjectAreas() != null && !fromSession.getSubjectAreas().isEmpty()){
				for(Iterator it = fromSession.getSubjectAreas().iterator(); it.hasNext();){
					fromSubjectArea = (SubjectArea) it.next();
					if (fromSubjectArea != null){
						toSubjectArea = (SubjectArea)fromSubjectArea.clone();
						toSubjectArea.setSession(toSession);
						toSession.addTosubjectAreas(toSubjectArea);
						if (fromSubjectArea.getDepartment() != null) {
							toDepartment = fromSubjectArea.getDepartment().findSameDepartmentInSession(toSession);
							if (toDepartment != null){
								toSubjectArea.setDepartment(toDepartment);
								toDepartment.addTosubjectAreas(toSubjectArea);
								sDao.saveOrUpdate(toSubjectArea);
								sDao.getSession().flush();
								sDao.getSession().evict(toSubjectArea);
								sDao.getSession().evict(fromSubjectArea);
								
							}
						}
					}
				}
			}
			sDao.getSession().flush();
			sDao.getSession().clear();
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Subject Areas", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all subject areas forward."));
		}
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
	
	protected void rollForwardBuildingPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession) throws Exception{
		if (fromPrefGroup.getBuildingPreferences() != null && !fromPrefGroup.getBuildingPreferences().isEmpty()){
			BuildingPref fromBuildingPref = null;
			BuildingPref toBuildingPref = null;
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
			if (toDepartment == null){
				return;
			}
			if (!getRoomList().containsKey(toDepartment)){
				getRoomList().put(toDepartment, buildRoomListForDepartment(toDepartment, toSession));
			} 
			for (Iterator it = fromPrefGroup.getBuildingPreferences().iterator(); it.hasNext(); ){
				fromBuildingPref = (BuildingPref) it.next();	
				Building toBuilding = fromBuildingPref.getBuilding().findSameBuildingInSession(toSession);
				if (toBuilding != null){
					boolean deptHasRoomInBuilding = false;
					Location loc = null;
					Room r = null;
					Iterator rIt = ((Set)getRoomList().get(toDepartment)).iterator();
					while(rIt.hasNext() && !deptHasRoomInBuilding){
						loc = (Location)rIt.next();
						if (loc instanceof Room) {
							r = (Room) loc;
							if (r.getBuilding() != null && r.getBuilding().getUniqueId().equals(toBuilding.getUniqueId())){
								deptHasRoomInBuilding = true;
							}
						}
					}
					if (deptHasRoomInBuilding){
						toBuildingPref = new BuildingPref();
						toBuildingPref.setBuilding(toBuilding);
						toBuildingPref.setPrefLevel(fromBuildingPref.getPrefLevel());
						toBuildingPref.setDistanceFrom(fromBuildingPref.getDistanceFrom());
						toBuildingPref.setOwner(toPrefGroup);
						toPrefGroup.addTopreferences(toBuildingPref);
					}
				}
			}
		}		
	}
	
	protected void rollForwardRoomPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getRoomPreferences() != null && !fromPrefGroup.getRoomPreferences().isEmpty()){
			RoomPref fromRoomPref = null;
			RoomPref toRoomPref = null;
			Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
			if (toDepartment == null){
				return;
			}
			if (!getRoomList().containsKey(toDepartment)){
				getRoomList().put(toDepartment, buildRoomListForDepartment(toDepartment, toSession));
			} 
			Set l = (Set)getRoomList().get(toDepartment);
			if (l != null && l.size() >0 ){					
				for (Iterator it = fromPrefGroup.getRoomPreferences().iterator(); it.hasNext();){
					fromRoomPref = (RoomPref) it.next();
					toRoomPref = new RoomPref();
					if (fromRoomPref.getRoom() instanceof Room) {
						Room fromRoom = (Room) fromRoomPref.getRoom();
						Location loc = null;
						Room toRoom = null;
						for (Iterator rmIt = l.iterator(); rmIt.hasNext();){
							loc = (Location) rmIt.next();
							if (loc instanceof Room) {
								toRoom = (Room) loc;
								if (toRoom.getBuilding().getExternalUniqueId().equals(fromRoom.getBuilding().getExternalUniqueId()) && toRoom.getRoomNumber().equals(fromRoom.getRoomNumber())){
									break;
								}								
							}
						}
						if (toRoom != null && toRoom.getBuilding().getExternalUniqueId().equals(fromRoom.getBuilding().getExternalUniqueId()) && toRoom.getRoomNumber().equals(fromRoom.getRoomNumber())){
							toRoomPref.setRoom(toRoom);
							toRoomPref.setPrefLevel(fromRoomPref.getPrefLevel());
							toRoomPref.setOwner(toPrefGroup);
							toPrefGroup.addTopreferences(toRoomPref);
						}	
					} else if (fromRoomPref.getRoom() instanceof NonUniversityLocation) {
						NonUniversityLocation fromNonUniversityLocation = (NonUniversityLocation) fromRoomPref.getRoom();
						Location loc = null;
						NonUniversityLocation toNonUniversityLocation = null;
						for (Iterator rmIt = l.iterator(); rmIt.hasNext();){
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
							toPrefGroup.addTopreferences(toRoomPref);
						}	
					}				
				}
			}
		}		
	}
	protected void rollForwardRoomFeaturePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getRoomFeaturePreferences() != null && !fromPrefGroup.getRoomFeaturePreferences().isEmpty()){
			RoomFeaturePref fromRoomFeaturePref = null;
			RoomFeaturePref toRoomFeaturePref = null;
			for (Iterator it = fromPrefGroup.getRoomFeaturePreferences().iterator(); it.hasNext(); ){
				fromRoomFeaturePref = (RoomFeaturePref) it.next();
				toRoomFeaturePref = new RoomFeaturePref();
				if (fromRoomFeaturePref.getRoomFeature() instanceof GlobalRoomFeature) {
					GlobalRoomFeature grf = (GlobalRoomFeature) fromRoomFeaturePref.getRoomFeature();
					toRoomFeaturePref.setRoomFeature(grf);
					toRoomFeaturePref.setPrefLevel(fromRoomFeaturePref.getPrefLevel());
					toRoomFeaturePref.setOwner(toPrefGroup);
					toPrefGroup.addTopreferences(toRoomFeaturePref);
				} else {
					Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
					if (toDepartment == null){
						continue;
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
							toPrefGroup.addTopreferences(toRoomFeaturePref);
						}
					}
				}
			}
		}		
	}
	
	protected void rollForwardRoomGroupPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getRoomGroupPreferences() != null && !fromPrefGroup.getRoomGroupPreferences().isEmpty()){
			RoomGroupPref fromRoomGroupPref = null;
			RoomGroupPref toRoomGroupPref = null;
			for (Iterator it = fromPrefGroup.getRoomGroupPreferences().iterator(); it.hasNext();){
				fromRoomGroupPref = (RoomGroupPref) it.next();
				toRoomGroupPref = new RoomGroupPref();
				if (fromRoomGroupPref.getRoomGroup().isDefaultGroup().booleanValue()){
					toRoomGroupPref.setRoomGroup(fromRoomGroupPref.getRoomGroup());
					toRoomGroupPref.setPrefLevel(fromRoomGroupPref.getPrefLevel());
					toRoomGroupPref.setOwner(toPrefGroup);
					toPrefGroup.addTopreferences(toRoomGroupPref);
				} else {
					Department toDepartment = findToManagingDepartmentForPrefGroup(toPrefGroup, fromPrefGroup, toSession);
					if (toDepartment == null){
						continue;
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
							toPrefGroup.addTopreferences(toRoomGroupPref);
						}						
					}
				}
			}
		}		
	}
	
	protected void rollForwardTimePrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession){
		if (fromPrefGroup.getTimePreferences() != null && !fromPrefGroup.getTimePreferences().isEmpty()){
			TimePref fromTimePref = null;
			TimePref toTimePref = null;
			for (Iterator it = fromPrefGroup.getTimePreferences().iterator(); it.hasNext();){
				fromTimePref = (TimePref) it.next();
				if (fromTimePref.getTimePattern() == null) {
					toTimePref = (TimePref)fromTimePref.clone();
				} else {
					toTimePref = TimePattern.getMatchingTimePreference(toSession.getUniqueId(), fromTimePref);
				}
				toTimePref.setOwner(toPrefGroup);
				toPrefGroup.addTopreferences(toTimePref);
			}
		}
	}

	protected void rollForwardDistributionPrefs(PreferenceGroup fromPrefGroup, PreferenceGroup toPrefGroup, Session toSession, org.hibernate.Session hibSession){
		if (fromPrefGroup.getDistributionObjects() != null && !fromPrefGroup.getDistributionObjects().isEmpty()){
			DistributionObject fromDistObj = null;
			DistributionObject toDistObj = null;
			DistributionPref fromDistributionPref = null;
			DistributionPref toDistributionPref = null;
			for (Iterator it = fromPrefGroup.getDistributionObjects().iterator(); it.hasNext(); ){
				fromDistObj = (DistributionObject) it.next();
				toDistObj = new DistributionObject();
				fromDistributionPref = fromDistObj.getDistributionPref();
				toDistributionPref = DistributionPref.findByIdRolledForwardFrom(fromDistributionPref.getUniqueId());
				if (toDistributionPref == null){
					toDistributionPref = new DistributionPref();
					toDistributionPref.setDistributionType(fromDistributionPref.getDistributionType());
					toDistributionPref.setGrouping(fromDistributionPref.getGrouping());
					toDistributionPref.setPrefLevel(fromDistributionPref.getPrefLevel());
					toDistributionPref.setUniqueIdRolledForwardFrom(fromDistributionPref.getUniqueId());
					Department toDept = Department.findByDeptCode(((Department)fromDistributionPref.getOwner()).getDeptCode(), toSession.getUniqueId());
					if (toDept != null){
						toDistributionPref.setOwner(toDept);
						toDept.addTopreferences(toDistributionPref);
					} else {
						continue;
					}
				}
				toDistObj.setDistributionPref(toDistributionPref);
				toDistObj.setPrefGroup(toPrefGroup);
				toDistObj.setSequenceNumber(fromDistObj.getSequenceNumber());
				toPrefGroup.addTodistributionObjects(toDistObj);
				hibSession.saveOrUpdate(toDistributionPref);
			}
		}		
	}	
	
	private void rollInstructorDistributionPrefs(DepartmentalInstructor fromInstructor, DepartmentalInstructor toInstructor){
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
				toInstructor.addTopreferences(toDistributionPref);
			}
		}
	}
	
	public void rollInstructorDataForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollInstructorDataForwardFrom());
		DepartmentalInstructor toInstructor = null;
		DepartmentalInstructor fromInstructor = null;
		DepartmentalInstructorDAO iDao = new DepartmentalInstructorDAO();
		Department toDepartment = null;
		Department fromDepartment = null;
		
		try {
			if (fromSession.getDepartments() != null){
				for(Iterator dIt = fromSession.getDepartments().iterator(); dIt.hasNext();){
					fromDepartment = (Department) dIt.next();
					if (fromDepartment != null && fromDepartment.getInstructors() != null && !fromDepartment.getInstructors().isEmpty()){
						toDepartment = fromDepartment.findSameDepartmentInSession(toSession);
						if (toDepartment != null){
							for (Iterator iIt = fromDepartment.getInstructors().iterator(); iIt.hasNext();){
								fromInstructor = (DepartmentalInstructor) iIt.next();
								toInstructor = (DepartmentalInstructor) fromInstructor.clone();
								toInstructor.setDepartment(toDepartment);
								rollForwardBuildingPrefs(fromInstructor, toInstructor, toSession);
								rollForwardRoomPrefs(fromInstructor, toInstructor, toSession);
								rollForwardRoomFeaturePrefs(fromInstructor, toInstructor, toSession);
								rollForwardRoomGroupPrefs(fromInstructor, toInstructor, toSession);
								rollForwardTimePrefs(fromInstructor, toInstructor, toSession);
								rollInstructorDistributionPrefs(fromInstructor, toInstructor);
								if (fromInstructor.getDesignatorSubjectAreas() != null && !fromInstructor.getDesignatorSubjectAreas().isEmpty()){
									Designator fromDesignator = null;
									Designator toDesignator = null;
									for (Iterator dsIt = fromInstructor.getDesignatorSubjectAreas().iterator(); dsIt.hasNext();){
										fromDesignator = (Designator) dsIt.next();
										toDesignator = new Designator();
										toDesignator.setCode(fromDesignator.getCode());
										toDesignator.setInstructor(toInstructor);
										toDesignator.setSubjectArea(SubjectArea.findByAbbv(toSession.getUniqueId(), fromDesignator.getSubjectArea().getSubjectAreaAbbreviation()));
										if (toDesignator.getSubjectArea() != null){
											toDesignator.getSubjectArea().addTodesignatorInstructors(toDesignator);
											toInstructor.addTodesignatorSubjectAreas(toDesignator);
										}
									}
								}
								iDao.saveOrUpdate(toInstructor);
								iDao.getSession().flush();
								iDao.getSession().evict(toInstructor);
								iDao.getSession().evict(fromInstructor);
							}
						}
					}
				}
				iDao.getSession().flush();
				iDao.getSession().clear();
			}
			
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Instructors", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all instructors forward."));
		}
	}

//	public void rollCourseOfferingsForwardFromMsf(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
//		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
//		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollCourseOfferingsForwardFrom());
//		if (toSession.getSubjectAreas() != null) {
//			SubjectArea subjectArea = null;
////			edu.purdue.smas.custom.util.PopulateSessionFromMsf pop = new edu.purdue.smas.custom.util.PopulateSessionFromMsf();
//			InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward();
//			SubjectArea.loadSubjectAreas(toSession.getUniqueId());
//			for (Iterator saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
//				subjectArea = (SubjectArea) saIt.next();
////				if (subjectArea.getSubjectAreaAbbreviation().compareTo("CHM") >= 0){
////				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
////				pop.populateSubjectArea(subjectArea, toSession, fromSession);
////				}
//				instrOffrRollFwd.rollForwardInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), fromSession, toSession);
//
//			}
//		}
//	}

	public void rollCourseOfferingsForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollCourseOfferingsForwardFrom());
		ArrayList subjects = new ArrayList();
		SubjectAreaDAO saDao = new SubjectAreaDAO();
		for (int i = 0; i <	rollForwardSessionForm.getRollForwardSubjectAreaIds().length; i++){
			subjects.add(saDao.get(Long.parseLong(rollForwardSessionForm.getRollForwardSubjectAreaIds()[i])));
		}
		if (toSession.getSubjectAreas() != null) {
			SubjectArea subjectArea = null;
			InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward();
			for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
				subjectArea = (SubjectArea) saIt.next();
				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
				instrOffrRollFwd.rollForwardInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), fromSession, toSession);
			}
		}
	}
	
	public void addNewCourseOfferings(ActionMessages errors,
			RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		ArrayList subjects = new ArrayList();
		SubjectAreaDAO saDao = new SubjectAreaDAO();
		for (int i = 0; i <	rollForwardSessionForm.getAddNewCourseOfferingsSubjectIds().length; i++){
			subjects.add(saDao.get(Long.parseLong(rollForwardSessionForm.getAddNewCourseOfferingsSubjectIds()[i])));
		}
		if (toSession.getSubjectAreas() != null) {
			SubjectArea subjectArea = null;
			InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward();
			for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
				subjectArea = (SubjectArea) saIt.next();
				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
				instrOffrRollFwd.addNewInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), toSession);
			}
		}
	}

//	public void loadCoursesNoLongerInCourseCatalogForTerm(ActionMessages errors,
//			RollForwardSessionForm rollForwardSessionForm){
//		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
//		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollCourseOfferingsForwardFrom());
//		ArrayList subjects = new ArrayList();
//		SubjectAreaDAO saDao = new SubjectAreaDAO();
//		for (int i = 0; i <	rollForwardSessionForm.getRollForwardSubjectAreaIds().length; i++){
//			subjects.add(saDao.get(Long.parseLong(rollForwardSessionForm.getRollForwardSubjectAreaIds()[i])));
//		}
//		if (toSession.getSubjectAreas() != null) {
//			SubjectArea subjectArea = null;
//			InstructionalOfferingRollForward instrOffrRollFwd = new InstructionalOfferingRollForward();
//			for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
//				subjectArea = (SubjectArea) saIt.next();
//				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
//				instrOffrRollFwd.rollForwardExpiredInstructionalOfferingsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), fromSession, toSession);
//			}
//		}
//	}
	private static String buildRoomQueryForDepartment(Department dept, Session sess, String locType){
		StringBuffer sb = new StringBuffer();
		sb.append("select l from " + locType + " as l inner join l.roomDepts as rd where l.session.uniqueId = ");
		sb.append(sess.getUniqueId().toString());
		sb.append(" and rd.department.uniqueId = ");
		sb.append(dept.getUniqueId().toString());
		return(sb.toString());
	}
	
	private static Set buildRoomListForDepartment(Department department, Session session){
		TreeSet ts = new TreeSet();
		Iterator it = RoomDAO.getInstance().getQuery(buildRoomQueryForDepartment(department, session, "Room")).iterate();
		Room r = null;
		while(it.hasNext()){
			r = (Room) it.next();
			RoomDept rd = null;
			for (Iterator it2 = r.getRoomDepts().iterator(); it2.hasNext();){
				rd = (RoomDept) it2.next();
				rd.getDepartment();
			}
			ts.add(r);
		}
		it = NonUniversityLocationDAO.getInstance().getQuery(buildRoomQueryForDepartment(department, session, "NonUniversityLocation")).iterate();
		NonUniversityLocation l = null;
		while(it.hasNext()){
			l = (NonUniversityLocation) it.next();
			RoomDept rd = null;
			for (Iterator it2 = l.getRoomDepts().iterator(); it2.hasNext();){
				rd = (RoomDept) it2.next();
				rd.getDepartment();
			}
			ts.add(l);
		}
		return(ts);
	}

	public static HashMap getRoomList() {
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
			CourseCatalogDAO ccDao = new CourseCatalogDAO();
			List l = ccDao.getQuery("select count(cc) from CourseCatalog cc where cc.session.uniqueId =" + session.getUniqueId().toString()).list();
			int cnt = 0;
			if (l != null && ! l.isEmpty()){
				cnt = ((Long)l.get(0)).intValue();
			}
			getSessionHasCourseCatalogList().put(session, new Boolean(cnt != 0));	
		}
		return(((Boolean)getSessionHasCourseCatalogList().get(session)).booleanValue());
	}
	
	public static HashMap getSessionHasCourseCatalogList() {
		if (sessionHasCourseCatalogList == null){
			sessionHasCourseCatalogList = new HashMap();
		}
		return(sessionHasCourseCatalogList);
	}
	
	public boolean sessionHasExternalBuildingList(Session session){
		if (!getSessionHasExternalBuildingList().containsKey(session)){
			ExternalBuildingDAO ebDao = new ExternalBuildingDAO();
			List l = ebDao.getQuery("select count(eb) from ExternalBuilding eb where eb.session.uniqueId =" + session.getUniqueId().toString()).list();
			int cnt = 0;
			if (l != null && ! l.isEmpty()){	
				cnt = ((Long)l.get(0)).intValue();
			}
			getSessionHasExternalBuildingList().put(session, new Boolean(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalBuildingList().get(session)).booleanValue());
	}
	
	public static HashMap getSessionHasExternalBuildingList(){
		if (sessionHasExternalBuildingList == null){
			sessionHasExternalBuildingList = new HashMap();
		}
		return(sessionHasExternalBuildingList);
	}

	public boolean sessionHasExternalRoomList(Session session){
		if (!getSessionHasExternalRoomList().containsKey(session)){
			ExternalRoomDAO erDao = new ExternalRoomDAO();
			List l = erDao.getQuery("select count(er) from ExternalRoom er where er.building.session.uniqueId =" + session.getUniqueId().toString()).list();
			int cnt = 0;
			if (l != null && ! l.isEmpty()){
				cnt = ((Long)l.get(0)).intValue();
			}
			getSessionHasExternalRoomList().put(session, new Boolean(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomList().get(session)).booleanValue());
	}
	
	public static HashMap getSessionHasExternalRoomList(){
		if (sessionHasExternalRoomList == null){
			sessionHasExternalRoomList = new HashMap();
		}
		return(sessionHasExternalRoomList);
	}

	public boolean sessionHasExternalRoomDeptList(Session session){
		if (!getSessionHasExternalRoomDeptList().containsKey(session)){
			ExternalRoomDepartmentDAO erdDao = new ExternalRoomDepartmentDAO();
			List l = erdDao.getQuery("select count(erd) from ExternalRoomDepartment erd where erd.room.building.session.uniqueId =" + session.getUniqueId().toString()).list();
			int cnt = 0;
			if (l != null && ! l.isEmpty()){
				cnt = ((Long)l.get(0)).intValue();
			}
			getSessionHasExternalRoomDeptList().put(session, new Boolean(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomDeptList().get(session)).booleanValue());
	}
	
	public static HashMap getSessionHasExternalRoomDeptList(){
		if (sessionHasExternalRoomDeptList == null){
			sessionHasExternalRoomDeptList = new HashMap();
		}
		return(sessionHasExternalRoomDeptList);
	}

	public boolean sessionHasExternalRoomFeatureList(Session session){
		if (!getSessionHasExternalRoomFeatureList().containsKey(session)){
			ExternalRoomFeatureDAO erfDao = new ExternalRoomFeatureDAO();
			List l = erfDao.getQuery("select count(erf) from ExternalRoomFeature erf where erf.room.building.session.uniqueId =" + session.getUniqueId().toString()).list();
			int cnt = 0;
			if (l != null && ! l.isEmpty()){
				cnt = ((Long)l.get(0)).intValue();
			}
			getSessionHasExternalRoomFeatureList().put(session, new Boolean(cnt != 0));
		}
		return(((Boolean) getSessionHasExternalRoomFeatureList().get(session)).booleanValue());
	}
	
	public static HashMap getSessionHasExternalRoomFeatureList(){
		if (sessionHasExternalRoomFeatureList == null){
			sessionHasExternalRoomFeatureList = new HashMap();
		}
		return(sessionHasExternalRoomFeatureList);
	}

	public void rollTimePatternsForward(ActionMessages errors, RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollTimePatternsForwardFrom());
		Vector fromDatePatterns = TimePattern.findAll(fromSession, null);
		TimePattern fromTimePattern = null;
		TimePattern toTimePattern = null;
		TimePatternDAO tpDao = new TimePatternDAO();
		try {
			for(Iterator it = fromDatePatterns.iterator(); it.hasNext();){
				fromTimePattern = (TimePattern) it.next();
				if (fromTimePattern != null){
					toTimePattern = (TimePattern) fromTimePattern.clone();
					toTimePattern.setSession(toSession);
					tpDao.saveOrUpdate(toTimePattern);
					tpDao.getSession().flush();
				}
			}
			tpDao.getSession().flush();
			tpDao.getSession().clear();
		} catch (Exception e) {
			Debug.error(e.getStackTrace().toString());
			Debug.error(e.getMessage());
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Time Patterns", fromSession.getLabel(), toSession.getLabel(), "Failed to roll all time patterns forward."));
		}		
	}
	
	public void rollClassPreferencesForward(ActionMessages errors,
			RollForwardSessionForm rollForwardSessionForm) throws Exception {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		ArrayList subjects = new ArrayList();
		SubjectAreaDAO saDao = new SubjectAreaDAO();
		for (int i = 0; i <	rollForwardSessionForm.getRollForwardClassPrefsSubjectIds().length; i++){
			subjects.add(saDao.get(Long.parseLong(rollForwardSessionForm.getRollForwardClassPrefsSubjectIds()[i])));
		}
		if (toSession.getSubjectAreas() != null) {
			SubjectArea subjectArea = null;
			for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
				subjectArea = (SubjectArea) saIt.next();
				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
				rollForwardClassPreferencesForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), toSession);
			}
		}		
	}
	
	private void rollForwardClassPreferencesForASubjectArea(
			String subjectAreaAbbreviation,
			Session toSession) throws Exception {
		List classes = Class_.findAllForControllingSubjectArea(subjectAreaAbbreviation, toSession.getUniqueId());
		Class_ toClass = null;
		Class_ fromClass = null;
		Class_DAO cDao = new Class_DAO();
		if (classes != null && !classes.isEmpty()){
			for (Iterator cIt = classes.iterator(); cIt.hasNext();){
				toClass = (Class_) cIt.next();
				if (toClass.getUniqueIdRolledForwardFrom() != null){
					fromClass = cDao.get(toClass.getUniqueIdRolledForwardFrom());
					if (fromClass != null){
						rollForwardTimePrefs(fromClass, toClass, toSession);
						rollForwardBuildingPrefs(fromClass, toClass, toSession);
						rollForwardRoomPrefs(fromClass, toClass, toSession);
						rollForwardRoomGroupPrefs(fromClass, toClass, toSession);
						rollForwardRoomFeaturePrefs(fromClass, toClass, toSession);
						rollForwardDistributionPrefs(fromClass, toClass, toSession, cDao.getSession());
						cDao.update(toClass);
						cDao.getSession().evict(fromClass);
					}
				}
				cDao.getSession().evict(toClass);
			}		
		}
	}

	public void rollClassInstructorsForward(ActionMessages errors,
			RollForwardSessionForm rollForwardSessionForm) {
		Session toSession = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		ArrayList subjects = new ArrayList();
		SubjectAreaDAO saDao = new SubjectAreaDAO();
		for (int i = 0; i <	rollForwardSessionForm.getRollForwardClassInstrSubjectIds().length; i++){
			subjects.add(saDao.get(Long.parseLong(rollForwardSessionForm.getRollForwardClassInstrSubjectIds()[i])));
		}
		if (toSession.getSubjectAreas() != null) {
			SubjectArea subjectArea = null;
			for (Iterator saIt = subjects.iterator(); saIt.hasNext();){
				subjectArea = (SubjectArea) saIt.next();
				SubjectArea.loadSubjectAreas(toSession.getUniqueId());
				rollForwardClassInstructorsForASubjectArea(subjectArea.getSubjectAreaAbbreviation(), toSession);
			}
		}		
	}

	private void rollForwardClassInstructorsForASubjectArea(
			String subjectAreaAbbreviation, Session toSession) {
		List classes = Class_.findAllForControllingSubjectArea(subjectAreaAbbreviation, toSession.getUniqueId());
		if (classes != null && !classes.isEmpty()){
			Class_ toClass = null;
			Class_ fromClass = null;
			Class_DAO cDao = new Class_DAO();
			for (Iterator cIt = classes.iterator(); cIt.hasNext();){
				toClass = (Class_) cIt.next();
				if (toClass.getUniqueIdRolledForwardFrom() != null){
					fromClass = cDao.get(toClass.getUniqueIdRolledForwardFrom());
					if (fromClass != null){
						if (fromClass.getClassInstructors() != null && !fromClass.getClassInstructors().isEmpty()) {
							ClassInstructor fromClassInstr = null;
							ClassInstructor toClassInstr = null;
							DepartmentalInstructor toDeptInstr = null;
							for (Iterator ciIt = fromClass.getClassInstructors().iterator(); ciIt.hasNext();){
								fromClassInstr = (ClassInstructor) ciIt.next();
								toDeptInstr = fromClassInstr.getInstructor().findThisInstructorInSession(toSession.getUniqueId());
								if (toDeptInstr != null){
									toClassInstr = new ClassInstructor();
									toClassInstr.setClassInstructing(toClass);
									toClassInstr.setInstructor(toDeptInstr);
									toClassInstr.setLead(fromClassInstr.isLead());
									toClassInstr.setPercentShare(fromClassInstr.getPercentShare());
									toClass.addToclassInstructors(toClassInstr);
									toDeptInstr.addToclasses(toClassInstr);
									cDao.getSession().update(toDeptInstr);
								}
							}
							cDao.update(toClass);
						}
						cDao.getSession().evict(fromClass);
					}
				}
				cDao.getSession().evict(toClass);
			}		
		}
	}

	private void cloneCourses(String[] courses, String courseToCloneFrom, RollForwardSessionForm rollForwardSessionForm){
		Session session = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());
		InstructionalOfferingDAO ioDao = InstructionalOfferingDAO.getInstance();
		String cloneSubj = courseToCloneFrom.substring(0,4).trim();
		String cloneCrs = courseToCloneFrom.substring(4,8).trim();
		String crs = null;
		String subj = null;
		String crsNbr = null;
		InstructionalOffering io = null;
		InstructionalOffering cloneFromIo = null;
		String query = "select io from InstructionalOffering io inner join io.courseOfferings co " +
				" where io.session.uniqueId=:sessionId " +
				" and co.subjectArea.subjectAreaAbbreviation=:subject" +
				" and co.courseNbr=:crsNbr";
		List l = ioDao.getQuery(query)
					.setLong("sessionId", session.getUniqueId())
					.setString("subject", cloneSubj)
					.setString("crsNbr", cloneCrs).list();
		if (l.size() == 1){
			cloneFromIo = (InstructionalOffering) l.get(0);
			for (int i = 0; i < courses.length; i++){
				crs = courses[i];
				subj = crs.substring(0,4).trim();
				crsNbr = crs.substring(4,8).trim();
				l = ioDao.getQuery(query)
				.setLong("sessionId", session.getUniqueId())
				.setString("subject", subj)
				.setString("crsNbr", crsNbr).list();
				if (l.size() == 1){
					io = (InstructionalOffering) l.get(0);
					io.cloneOfferingConfigurationFrom(cloneFromIo);
					try {
						ioDao.saveOrUpdate(io);
					} catch (Exception e) {
						// do nothing
					}
					ioDao.getSession().flush();
					ioDao.getSession().clear();
				}
			}
		}

	}
	
	public void cloneCourseToCourses(RollForwardSessionForm rollForwardSessionForm) {
		Session session = Session.getSessionById(rollForwardSessionForm.getSessionToRollForwardTo());

//		//set 1 - "VCS 565D"
//		String[] courses = {
//				"CPB 584 ",
//				"CPB 585 ",
//				"CPB 586 ",
//				"CPB 586Y",
//				"CPB 586Z",
//				"CPB 587 ",
//				"CPB 588 ",
//				"CPB 589 ",
//				"CPB 589Y",
//				"V M 510 ",
//				"V M 578 ",
//				"VCS 560 ",
//				"VCS 561 ",
//				"VCS 562 ",
//				"VCS 563 ",
//				"VCS 563Y",
//				"VCS 565 ",
//				"VCS 565E",
//				"VCS 565F",
//				"VCS 565G",
//				"VCS 565M",
//				"VCS 565N",
//				"VCS 566 ",
//				"VCS 567 ",
//				"VCS 567A",
//				"VCS 567Y",
//				"VCS 568 ",
//				"VCS 571 ",
//				"VCS 571D",
//				"VCS 571M",
//				"VCS 571N",
//				"VCS 571O",
//				"VCS 571P",
//				"VCS 571R",
//				"VCS 571S",
//				"VCS 571V",
//				"VCS 571W",
//				"VCS 571Y",
//				"VCS 571Z",
//				"VCS 572Y",
//				"VCS 575 ",
//				"VCS 575D",
//				"VCS 575Y",
//				"VCS 575Z",
//				"VCS 576Y",
//				"VCS 576Z",
//				"VCS 577Y",
//				"VCS 577Z",
//				"VCS 578Y",
//				"VCS 578Z",
//				"VCS 579V",
//				"VCS 579W",
//				"VCS 579Y",
//				"VCS 579Z",
//				"VCS 580Y",
//				"VCS 580Z",
//				"VCS 582 ",
//				"VCS 583 ",
//				"VCS 585 ",
//				"VCS 585E",
//				"VCS 585F",
//				"VCS 588Y",
//				"VCS 591 ",
//				"VCS 591E",
//				"VCS 591F",
//				"VCS 591M",
//				"VCS 591N",
//				"VCS 591S",
//				"VCS 591T",
//				"VCS 594 ",
//				"VCS 594Y",
//				"VCS 594Z"
//		};
//		cloneCourses(courses, "VCS 565D", session);
//		//set 2 - LYNN G409 - "VCS 565E"
//		String[] courses2 = {
//				"VCS 575E",
//				"VCS 575F",
//				"VCS 575G",
//				"VCS 575M",
//				"VCS 575N"
//		};
//		cloneCourses(courses2, "VCS 565E", session);

//		//set 3 - LYNN 1240 - "VCS 562 "
//		String[] courses3 = {
//				"VCS 572 ",
//				"VCS 582G",
//				"VCS 582O",
//				"VCS 582S"
//		};
//		cloneCourses(courses3, "VCS 562 ", session);
//		//set 4 - LYNN G269 - "VCS 561 "
//		String[] courses4 = {
//				"VCS 581 "
//		};
//		cloneCourses(courses4, "VCS 561 ", session);
//		//set 5 - LYNN G397 - "VCS 566 "
//		String[] courses5 = {
//				"VCS 576 ",
//				"VCS 577 ",
//				"VCS 578 ",
//				"VCS 579 ",
//				"VCS 580 ",
//				"VCS 588 "
//		};
//		cloneCourses(courses5, "VCS 566 ", session);
//		//set 6 - LYNN G490A - "VCS 585F"
//		String[] courses6 = {
//				"VCS 586 ",
//				"VCS 586Y"
//		};
//		cloneCourses(courses6, "VCS 585F", session);
//		// Pharmacy Courses
//		String[] courses = {
//				"CLPH585B",
//				"CLPH585C",
//				"CLPH585D",
//				"CLPH585E",
//				"CLPH585N",
//				"CLPH585R",
//				"CLPH585S",
//				"CLPH585T",
//				"CLPH585U",
//				"CLPH588A",
//				"CLPH588B",
//				"CLPH588C",
//				"CLPH588D",
//				"CLPH588E",
//				"CLPH588N",
//				"CLPH588R",
//				"CLPH588S",
//				"CLPH588T",
//				"CLPH588U",
//				"CLPH589A",
//				"CLPH589B",
//				"CLPH589C",
//				"CLPH589D",
//				"CLPH589E",
//				"CLPH589N",
//				"CLPH589R",
//				"CLPH589S",
//				"CLPH589T",
//				"CLPH589U",
//				"PHPR498A",
//				"PHPR498B",
//				"PHPR498C",
//				"PHPR498D",
//				"PHPR498E",
//				"PHPR498N",
//				"PHPR498R",
//				"PHPR498S",
//				"PHPR498T",
//				"PHPR498U",
//				"PHPR499A",
//				"PHPR499B",
//				"PHPR499C",
//				"PHPR499D",
//				"PHPR499E",
//				"PHPR499N",
//				"PHPR499R",
//				"PHPR499S",
//				"PHPR499T",
//				"PHPR499U",
//				"NUPH595A",
//				"NUPH595B",
//				"NUPH595C",
//				"NUPH595D",
//				"NUPH595E"
//		};
//		cloneCourses(courses, "CLPH585A", rollForwardSessionForm);
//		// PPE Courses
//		String[] courses = {
//				"PPE 305 ",
//				"PPE 353 "
//		};
//		cloneCourses(courses, "PPE 151 ", rollForwardSessionForm);
//		// PPT Courses
//		String[] courses2 = {
//				"PPT 305 ",
//				"PPT 353 "
//		};
//		cloneCourses(courses2, "PPT 151 ", rollForwardSessionForm);

	}


}
