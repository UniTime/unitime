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
package org.unitime.timetable.dataexchange;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.NonUniversityLocationPicture;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeatureType;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomPicture;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.LocationPermIdGenerator;

public class RoomImport extends RoomSharingImport {
	protected Session iSession;
	protected SimpleDateFormat iDateFormat, iTimeFormat;
	protected boolean iIncremental = false;
	protected boolean iSharing = false;
	protected boolean iPictures = false;
	protected boolean iBuildings = false;
	protected boolean iEvents = false;
	protected boolean iExams = false;
	protected Map<String, RoomFeature> iFeatures;
	protected Map<String, RoomGroup> iGroups;
	
    public void loadXml(Element root) throws Exception {
        if (!root.getName().equalsIgnoreCase("rooms")) {
        	throw new Exception("Given XML file is not rooms load file.");
        }
        try {
            beginTransaction();

            String campus = root.attributeValue("campus");
            String year   = root.attributeValue("year");
            String term   = root.attributeValue("term");
            String created = root.attributeValue("created");
            iDateFormat = new SimpleDateFormat(root.attributeValue("dateFormat", "yyyy/M/d"), Locale.US);
	        iTimeFormat = new SimpleDateFormat(root.attributeValue("timeFormat", "HHmm"), Locale.US);
            iSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
            if (iSession == null)
                throw new Exception("No session found for the given campus, year, and term.");
            iIncremental = "true".equalsIgnoreCase(root.attributeValue("incremental", "true"));
            iSharing = "true".equalsIgnoreCase(root.attributeValue("sharing", "true"));
            iPictures = "true".equalsIgnoreCase(root.attributeValue("pictures", "true"));
            iBuildings = "true".equalsIgnoreCase(root.attributeValue("buildings", "true"));
            iEvents = "true".equalsIgnoreCase(root.attributeValue("events", "true"));
            iExams = "true".equalsIgnoreCase(root.attributeValue("exams", "true"));
            

            if (created != null) {
                ChangeLog.addChange(getHibSession(), getManager(), iSession, iSession, created, ChangeLog.Source.DATA_IMPORT_ROOMS, ChangeLog.Operation.UPDATE, null, null);
            }
            
            Map<String, Building> buildings = new HashMap<String, Building>();
            if (iBuildings) {
            	info("Buildings...");
            	List<Building> remaining = new ArrayList<Building>(getHibSession().createQuery(
    	        		"from Building where session.uniqueId = :sessionId", Building.class)
            			.setParameter("sessionId", iSession.getUniqueId()).list());
            	for (Iterator i = root.elementIterator("building"); i.hasNext(); ) {
            		Element element = (Element)i.next();
            		Building building = lookupBuilding(remaining, element);
            		if (building == null) {
            			building = new Building();
            			building.setSession(iSession);
            		}
            		building.setExternalUniqueId(getOptionalStringAttribute(element, "externalId"));
            		building.setAbbreviation(getRequiredStringAttribute(element, "abbreviation"));
            		building.setName(getOptionalStringAttribute(element, "name"));
            		building.setCoordinateX(getOptionalDoubleAttribute(element, "latitude"));
            		building.setCoordinateY(getOptionalDoubleAttribute(element, "longitude"));
            		if (building.getUniqueId() == null) {
            			getHibSession().persist(building);
            			debug("Created " + building.getAbbrName());
            		} else {
            			getHibSession().merge(building);
            			debug("Updated " + building.getAbbrName());
            		}
            		buildings.put(building.getAbbreviation(), building);
            	}
            	if (!iIncremental) {
            		for (Building building: remaining) {
                        for (Room r: getHibSession().createQuery(
                        		"select r from Room r where r.building.uniqueId=:buildingId", Room.class)
                        		.setParameter("buildingId", building.getUniqueId()).list()) {
                        	getHibSession().createMutationQuery("delete RoomPref p where p.room.uniqueId=:roomId").setParameter("roomId", r.getUniqueId()).executeUpdate();
                            for (Iterator<Assignment> j=r.getAssignments().iterator();j.hasNext();) {
                                Assignment a = j.next();
                                a.getRooms().remove(r);
                                getHibSession().merge(a);
                                j.remove();
                            }
                            debug("Deleted room " + r.getLabel());
                            if (r.getParentRoom() != null)
                            	r.getParentRoom().getPartitions().remove(r);
                            getHibSession().remove(r);
                        }
            			debug("Deleted " + building.getAbbrName());
                        getHibSession().createMutationQuery("delete BuildingPref p where p.building.uniqueId=:buildingId").setParameter("buildingId", building.getUniqueId()).executeUpdate();
            			getHibSession().remove(building);
            		}
            	}
            } else {
            	for (Building building: getHibSession().createQuery(
    	        		"from Building where session.uniqueId = :sessionId", Building.class)
            			.setParameter("sessionId", iSession.getUniqueId()).list()) {
            		buildings.put(building.getAbbreviation(), building);
            	}
            }
            
            iFeatures = new HashMap<String, RoomFeature>();
            for (GlobalRoomFeature rf: getHibSession().createQuery(
            		"from GlobalRoomFeature where session.uniqueId = :sessionId", GlobalRoomFeature.class)
            		.setParameter("sessionId", iSession.getUniqueId()).list()) {
            	iFeatures.put(rf.getAbbv(), rf);
            }
            for (DepartmentRoomFeature rf: getHibSession().createQuery(
            		"from DepartmentRoomFeature where department.session.uniqueId = :sessionId", DepartmentRoomFeature.class)
            		.setParameter("sessionId", iSession.getUniqueId()).list()) {
            	iFeatures.put(rf.getDeptCode() + "|" + rf.getAbbv(), rf);
            }
            iGroups = new HashMap<String, RoomGroup>();
            for (RoomGroup rg: getHibSession().createQuery(
            		"from RoomGroup where session.uniqueId = :sessionId", RoomGroup.class)
            		.setParameter("sessionId", iSession.getUniqueId()).list()) {
            	if (rg.isGlobal())
            		iGroups.put(rg.getAbbv(), rg);
            	else
            		iGroups.put(rg.getDepartment().getDeptCode() + "|" + rg.getAbbv(), rg);
            }
            
            info("Rooms...");
            Map<String, Room> rooms = new HashMap<String, Room>();
            Map<Room, String> fixParents = new HashMap<Room, String>();
            List<Room> remaining = new ArrayList<Room>(getHibSession().createQuery(
	        		"from Room where session.uniqueId = :sessionId", Room.class)
        			.setParameter("sessionId", iSession.getUniqueId()).list());
            for (Iterator i = root.elementIterator("room"); i.hasNext(); ) {
            	Element element = (Element)i.next();
            	Room room = lookupRoom(remaining, element);
            	if (room == null) {
            		room = new Room();
            		room.setSession(iSession);
            		room.setPartitions(new HashSet<Room>());
            		room.setPictures(new HashSet<RoomPicture>());
            	}
            	room.setBuilding(buildings.get(getRequiredStringAttribute(element, "building")));
            	room.setBuildingAbbv(getRequiredStringAttribute(element, "building"));
            	room.setRoomNumber(getRequiredStringAttribute(element, "roomNumber"));
            	room.setClassification(getOptionalStringAttribute(element, "classification"));
            	String parent = getOptionalStringAttribute(element, "parent");
            	if (parent != null) {
            		if (room.getParentRoom() != null) {
            			if (parent.equals(room.getParentRoom().getBuilding().getAbbreviation() + " " + room.getParentRoom().getRoomNumber())) {
            				// no change is needed
            			} else {
            				room.getParentRoom().getPartitions().remove(room);
                			room.setParentRoom(null);
                			fixParents.put(room, parent);
            			}
            		} else {
            			fixParents.put(room, parent);
            		}
            	} else {
            		if (room.getParentRoom() != null) {
            			room.getParentRoom().getPartitions().remove(room);
            			room.setParentRoom(null);
            		}
            	}
            	importGeneric(room, element);
            	if (iPictures) importRoomPictures(room, element);
            	getHibSession().merge(room);
            	rooms.put(room.getBuilding().getAbbreviation() + " " + room.getRoomNumber(), room);
            }
            
            if (iIncremental)
            	for (Room r: remaining) {
            		debug("Removing " + r.getBuilding().getAbbreviation() + " " + r.getRoomNumber());
            		getHibSession().createMutationQuery("delete RoomPref p where p.room.uniqueId=:roomId").setParameter("roomId", r.getUniqueId()).executeUpdate();
                    for (Iterator<Assignment> j=r.getAssignments().iterator();j.hasNext();) {
                        Assignment a = j.next();
                        a.getRooms().remove(r);
                        getHibSession().merge(a);
                        j.remove();
                    }
                    if (r.getParentRoom() != null)
                    	r.getParentRoom().getPartitions().remove(r);
                    getHibSession().remove(r);
            	}
            
            for (Map.Entry<Room, String> e: fixParents.entrySet()) {
            	Room room = e.getKey();
            	Room parentRoom = rooms.get(e.getValue());
            	if (parentRoom != null) {
            		room.setParentRoom(parentRoom);
            		parentRoom.addToPartitions(room);
            		getHibSession().merge(room);
            	}
            }
            
            info("Non-University Locations...");
            List<NonUniversityLocation> locations = new ArrayList<NonUniversityLocation>(getHibSession().createQuery(
	        		"from NonUniversityLocation where session.uniqueId = :sessionId", NonUniversityLocation.class)
        			.setParameter("sessionId", iSession.getUniqueId()).list());
            for (Iterator i = root.elementIterator("location"); i.hasNext(); ) {
            	Element element = (Element)i.next();
            	NonUniversityLocation room = lookupLocation(locations, element);
            	if (room == null) {
            		room = new NonUniversityLocation();
            		room.setSession(iSession);
            		room.setPictures(new HashSet<NonUniversityLocationPicture>());
            	}
            	room.setName(getRequiredStringAttribute(element, "name"));
            	importGeneric(room, element);
            	if (iPictures) importLocationPictures(room, element);
            	getHibSession().merge(room);
            }
            
            if (iIncremental)
            	for (NonUniversityLocation r: locations) {
            		debug("Removing " + r.getName());
            		getHibSession().createMutationQuery("delete RoomPref p where p.room.uniqueId=:roomId").setParameter("roomId", r.getUniqueId()).executeUpdate();
                    for (Iterator<Assignment> j=r.getAssignments().iterator();j.hasNext();) {
                        Assignment a = j.next();
                        a.getRooms().remove(r);
                        getHibSession().merge(a);
                        j.remove();
                    }
                    getHibSession().remove(r);
            	}
            
        	info("All done.");
        	
            commitTransaction();
        } catch (Exception e) {
            fatal("Exception: " + e.getMessage(), e);
            rollbackTransaction();
            throw e;
        }
	}
    
    protected void importRoomPictures(Room room, Element element) throws Exception {
    	List<RoomPicture> remaining = new ArrayList<RoomPicture>(room.getPictures());
		for (Iterator i = element.elementIterator("picture"); i.hasNext(); ) {
			Element e = (Element)i.next();
			String typeRef = getOptionalStringAttribute(e, "type");
			AttachmentType type = (typeRef == null ? null : AttachmentType.findByReference(getHibSession(), typeRef));
			RoomPicture picture = null;
			for (Iterator<RoomPicture> j = remaining.iterator(); j.hasNext(); ) {
				RoomPicture p = j.next();
				if (p.getFileName().equals(getRequiredStringAttribute(e, "fileName")) && p.getContentType().equals(getRequiredStringAttribute(e, "contentType"))) {
					picture = p;
					j.remove();
					break;
				}
			}
			if (picture == null) {
				picture = new RoomPicture();
				picture.setLocation(room);
				picture.setTimeStamp(new Date());
				room.addToPictures(picture);
			}
			picture.setFileName(getRequiredStringAttribute(e, "fileName"));
			picture.setContentType(getRequiredStringAttribute(e, "contentType"));
			picture.setType(type);
			picture.setDataFile(Base64.getDecoder().decode(e.getText()));
			if (picture.getUniqueId() == null)
				getHibSession().persist(picture);
			else
				getHibSession().merge(picture);
		}
		for (RoomPicture picture: remaining) {
			room.getPictures().remove(picture);
			getHibSession().remove(picture);
		}
    }
    
    protected void importLocationPictures(NonUniversityLocation room, Element element) throws Exception {
    	List<NonUniversityLocationPicture> remaining = new ArrayList<NonUniversityLocationPicture>(room.getPictures());
		for (Iterator i = element.elementIterator("picture"); i.hasNext(); ) {
			Element e = (Element)i.next();
			String typeRef = getOptionalStringAttribute(e, "type");
			AttachmentType type = (typeRef == null ? null : AttachmentType.findByReference(getHibSession(), typeRef));
			NonUniversityLocationPicture picture = null;
			for (Iterator<NonUniversityLocationPicture> j = remaining.iterator(); j.hasNext(); ) {
				NonUniversityLocationPicture p = j.next();
				if (p.getFileName().equals(getRequiredStringAttribute(e, "fileName")) && p.getContentType().equals(getRequiredStringAttribute(e, "contentType"))) {
					picture = p;
					j.remove();
					break;
				}
			}
			if (picture == null) {
				picture = new NonUniversityLocationPicture();
				picture.setLocation(room);
				picture.setTimeStamp(new Date());
				room.addToPictures(picture);
			}
			picture.setFileName(getRequiredStringAttribute(e, "fileName"));
			picture.setContentType(getRequiredStringAttribute(e, "contentType"));
			picture.setType(type);
			picture.setDataFile(Base64.getDecoder().decode(e.getText()));
			if (picture.getUniqueId() == null)
				getHibSession().persist(picture);
			else
				getHibSession().merge(picture);
		}
		for (NonUniversityLocationPicture picture: remaining) {
			room.getPictures().remove(picture);
			getHibSession().remove(picture);
		}
    }
    
    protected void importGeneric(Location location, Element element) throws Exception {
    	location.setExternalUniqueId(getOptionalStringAttribute(element, "externalId"));
    	if (location.getPermanentId() == null) {
        	LocationPermIdGenerator.setPermanentId(location);
    	}
    	location.setCoordinateX(getOptionalDoubleAttribute(element, "latitude"));
    	location.setCoordinateY(getOptionalDoubleAttribute(element, "longitude"));
    	location.setCapacity(getRequiredIntegerAttribute(element, "capacity"));
    	location.setIgnoreTooFar(getOptionalBooleanAttribute(element, "ignoreTooFar", false));
    	location.setIgnoreRoomCheck(getOptionalBooleanAttribute(element, "ignoreRoomCheck", false));
    	location.setArea(getOptionalDoubleAttribute(element, "area"));
    	location.setExamCapacity(getOptionalIntegerAttribute(element, "examCapacity"));
    	location.setDisplayName(getOptionalStringAttribute(element, "displayName"));
    	location.setRoomType(RoomType.findByReference(getRequiredStringAttribute(element, "type")));
    	if (location.getUniqueId() == null) {
    		debug("Creating " + location.getLabel());
    		getHibSession().persist(location);
    	} else {
    		debug("Updating " + location.getLabel());
    	}
    	
    	if (iEvents) {
    		String eventDeptCode = getOptionalStringAttribute(element, "eventDepartment");
    		if (eventDeptCode != null) {
    			location.setEventDepartment(Department.findByDeptCode(eventDeptCode, iSession.getUniqueId()));
    		} else {
    			location.setEventDepartment(null);
    		}
    		String status = getOptionalStringAttribute(element, "eventStatus");
    		location.setEventStatus(status == null ? null : RoomTypeOption.Status.valueOf(status).ordinal());
    		Element note = element.element("eventNote");
    		location.setNote(note == null ? null : note.getText());
    		location.setBreakTime(getOptionalIntegerAttribute(element, "breakTime"));
    		location.setEventEmail(getOptionalStringAttribute(element, "eventEmail"));
    		if (location.getAllowedServices() == null)
    			location.setAllowedServices(new HashSet<EventServiceProvider>());
    		List<EventServiceProvider> remaining = new ArrayList<EventServiceProvider>(location.getAllowedServices());
    		for (Iterator i = element.elementIterator("service"); i.hasNext(); ) {
    			Element e = (Element)i.next();
    			EventServiceProvider service = null;
    			for (Iterator<EventServiceProvider> j = remaining.iterator(); j.hasNext(); ) {
    				EventServiceProvider s = j.next();
    				if (getRequiredStringAttribute(e, "reference").equals(s.getReference())) {
    					j.remove();
    					service = s;
    					break;
    				}
    			}
    			if (service == null) {
    				service = EventServiceProvider.getEventServiceProvider(
    						getRequiredStringAttribute(e, "reference"),
    						iSession.getUniqueId(),
    						location.getEventDepartment() == null ? null : location.getEventDepartment().getUniqueId(),
    						iHibSession);
    				if (service != null)
    					location.getAllowedServices().add(service);
    			}
    		}
    		location.getAllowedServices().removeAll(remaining);
    	}
    	
		if (location.getRoomDepts() == null)
			location.setRoomDepts(new HashSet<RoomDept>());
		Set<RoomDept> depts = new HashSet<RoomDept>(location.getRoomDepts());
		Map<Department, RoomPref> roomPrefs = new HashMap<Department, RoomPref>();
		for (RoomDept rd: depts) { 
			for (RoomPref x: (Set<RoomPref>)rd.getDepartment().getRoomPreferences()) {
				if (x.getRoom().equals(location)) {
					roomPrefs.put(rd.getDepartment(), x);
					break;
				}
			}
		}
		for (Iterator i = element.elementIterator("department"); i.hasNext(); ) {
			Element e = (Element)i.next();
			String code = getRequiredStringAttribute(e, "code");
			Department department = Department.findByDeptCode(code, iSession.getUniqueId());
			if (department == null) {
				warn("Department " + code + " does not exist.");
				continue;
			}
			String pref = getOptionalStringAttribute(e, "preference");
			PreferenceLevel level = pref == null ? null : PreferenceLevel.getPreferenceLevel(pref);
			RoomDept roomDept = null;
			for (Iterator<RoomDept> j = depts.iterator(); j.hasNext(); ) {
				RoomDept rd = j.next();
				if (department.equals(rd.getDepartment())) {
					j.remove();
					roomDept = rd;
					break;
				}
			}
			if (roomDept == null) {
				roomDept = new RoomDept();
				roomDept.setRoom(location);
				location.addToRoomDepts(roomDept);
				roomDept.setDepartment(department);
				department.addToRoomDepts(roomDept);
			}
			roomDept.setControl(getOptionalBooleanAttribute(e, "control", false));
			if (roomDept.getUniqueId() == null)
				getHibSession().persist(roomDept);
			else
				getHibSession().merge(roomDept);
			RoomPref rp = roomPrefs.remove(department);
			if (level != null) {
				if (rp != null) {
					rp.setPrefLevel(level);
					getHibSession().merge(rp);
				} else {
					rp = new RoomPref();
					rp.setRoom(location);
					rp.setPrefLevel(level);
					rp.setOwner(department);
					department.getPreferences().add(rp);
					roomDept.setPreference(level);
					getHibSession().persist(rp);
					getHibSession().merge(department);
				}
			} else {
				if (rp != null) {
					department.getPreferences().remove(rp);
					getHibSession().remove(rp);
					getHibSession().merge(department);
				}
			}
		}
		for (RoomDept rd:depts) {
			location.getRoomDepts().remove(rd);
			rd.getDepartment().getRoomDepts().remove(rd);
			rd.setDepartment(null);
			rd.setRoom(null);
			getHibSession().remove(rd);
		}
		for (RoomPref rp: roomPrefs.values()) {
			rp.getOwner().getPreferences().remove(rp);
			getHibSession().remove(rp);
			getHibSession().merge(rp.getOwner());
		}
    	
    	if (location.getFeatures() == null) location.setFeatures(new HashSet<RoomFeature>());
    	Set<RoomFeature> features = new HashSet<RoomFeature>(location.getFeatures());
    	for (Iterator i = element.elementIterator("feature"); i.hasNext(); ) {
			Element e = (Element)i.next();
			String code = getRequiredStringAttribute(e, "code");
			String dept = getOptionalStringAttribute(e, "department");
			String type = getOptionalStringAttribute(e, "type");
			RoomFeature feature = iFeatures.get(dept == null ? code : dept + "|" + code);
			if (feature == null) {
				if (dept == null) {
					GlobalRoomFeature f = new GlobalRoomFeature();
					f.setAbbv(code);
					f.setSession(iSession);
					feature = f;
					iFeatures.put(code, f);
				} else {
					DepartmentRoomFeature f = new DepartmentRoomFeature();
					f.setAbbv(code);
					f.setDepartment(Department.findByDeptCode(dept, iSession.getUniqueId()));
					feature = f;
					iFeatures.put(dept + "|" + code, f);
				}
				feature.setLabel(getOptionalStringAttribute(e, "name"));
				feature.setDescription(getOptionalStringAttribute(e, "description"));
				feature.setFeatureType(type == null ? null : RoomFeatureType.findByRef(type));
				getHibSession().persist(feature);
			}
			if (!features.remove(feature)) {
				location.addToFeatures(feature);
				feature.addToRooms(location);
			}
    	}
		for (RoomFeature rf: features) {
			rf.getRooms().remove(location);
			location.getFeatures().remove(rf);
		}
		
		if (location.getRoomGroups() == null) location.setRoomGroups(new HashSet<RoomGroup>());
    	Set<RoomGroup> groups = new HashSet<RoomGroup>(location.getRoomGroups());
    	for (Iterator i = element.elementIterator("group"); i.hasNext(); ) {
			Element e = (Element)i.next();
			String code = getRequiredStringAttribute(e, "code");
			String dept = getOptionalStringAttribute(e, "department");
			RoomGroup group = iGroups.get(dept == null ? code : dept + "|" + code);
			if (group == null) {
				group = new RoomGroup();
				group.setSession(iSession);
				group.setDefaultGroup(false);
				if (dept != null) {
					group.setGlobal(false);
					group.setDepartment(Department.findByDeptCode(dept, iSession.getUniqueId()));
					iGroups.put(dept + "|" + code, group);
				} else {
					group.setGlobal(true);
					iGroups.put(code, group);
				}
				group.setName(getOptionalStringAttribute(e, "name"));
				group.setDescription(getOptionalStringAttribute(e, "description"));
				getHibSession().persist(group);
			}
			if (!groups.remove(group)) {
				location.addToRoomGroups(group);
				group.addToRooms(location);
			}
    	}
		for (RoomGroup rg: groups) {
			rg.getRooms().remove(location);
			location.getRoomGroups().remove(rg);
		}
		
		if (iSharing) {
			RoomSharingModel model = location.getRoomSharingModel();
            model.setPreferences(null);
			Element sharingEl = element.element("sharing");
			if (sharingEl != null) {
				location.setShareNote(getOptionalStringAttribute(sharingEl, "note"));
				for (Iterator j = sharingEl.elementIterator(); j.hasNext(); ) {
            		Element el = (Element) j.next();
            		TimeObject time = new TimeObject(el.attributeValue("start"), el.attributeValue("end"), el.attributeValue("days"));
            		String pref = null;
            		if ("unavailable".equals(el.getName())) {
            			pref = RoomSharingModel.sNotAvailablePref.toString();
            		} else if ("assigned".equals(el.getName())) {
                        Department dept = null;

                        String deptCode = el.attributeValue("code");
                        if (deptCode != null) {
                        	for (RoomDept rd: location.getRoomDepts())
                        		if (deptCode.equals(rd.getDepartment().getDeptCode())) {
                        			dept = rd.getDepartment(); break;
                        		}
                        }
                        
                        if (dept == null) continue;
                       
                        pref = dept.getUniqueId().toString();
            		}
            		
            		if (pref == null) continue;
            		
        			if (time.hasDays()) {
        				for (int d: time.getDays())
        					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
        						model.setPreference(d, t, pref);
        			} else {
        				for (int d = 0; d < model.getNrDays(); d++)
        					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
        						model.setPreference(d, t, pref); 
        			}
            	}
			} else {
				location.setShareNote(null);
			}
			location.setRoomSharingModel(model);
		}
		
		if (iExams) {
			if (location.getExamTypes() == null) location.setExamTypes(new HashSet<ExamType>());
			if (location.getExamPreferences() == null) location.setExamPreferences(new HashSet<ExamLocationPref>());
	    	Set<ExamType> types = new HashSet<ExamType>(location.getExamTypes());
	    	Set<ExamLocationPref> prefs = new HashSet<ExamLocationPref>(location.getExamPreferences());
	    	for (Iterator i = element.elementIterator("exam"); i.hasNext(); ) {
				Element e = (Element)i.next();
				ExamType type = ExamType.findByReference(getRequiredStringAttribute(e, "type"));
				if (type == null) {
					warn("Exam type " + getRequiredStringAttribute(e, "type") + " does not exist.");
					continue;
				}
				if (!types.remove(type)) {
					location.addToExamTypes(type);
				}
				for (Iterator j = e.elementIterator("preference"); j.hasNext(); ) {
					Element p = (Element)j.next();
					ExamPeriod period = lookupExamPeriod(
							getRequiredStringAttribute(p, "date"),
							getRequiredStringAttribute(p, "start"),
							type.getReference());
					PreferenceLevel level = PreferenceLevel.getPreferenceLevel(getRequiredStringAttribute(p, "level"));
					if (period == null || level == null) continue;
					ExamLocationPref pref = null;
					for (Iterator<ExamLocationPref> k = prefs.iterator(); k.hasNext(); ) {
						ExamLocationPref elp = k.next();
						if (elp.getExamPeriod().equals(period)) {
							pref = elp;
							k.remove();
							break;
						}
					}
					if (pref == null) {
						pref = new ExamLocationPref();
						pref.setExamPeriod(period);
						pref.setLocation(location);
						location.addToExamPreferences(pref);
					}
					pref.setPrefLevel(level);
					if (pref.getUniqueId() == null)
						getHibSession().persist(pref);
					else
						getHibSession().merge(pref);
				}
	    	}
			for (ExamType type: types) {
				location.getExamTypes().remove(type);
			}
			for (ExamLocationPref pref: prefs) {
				location.getExamPreferences().remove(pref);
				getHibSession().remove(pref);
			}
		}
		
		if (iEvents) {
			RoomSharingModel model = new RoomSharingModel(location, null, null);
			Element sharingEl = element.element("availability");
			if (sharingEl != null) {
				for (Iterator j = sharingEl.elementIterator(); j.hasNext(); ) {
            		Element el = (Element) j.next();
            		TimeObject time = new TimeObject(el.attributeValue("start"), el.attributeValue("end"), el.attributeValue("days"));
            		String pref = null;
            		if ("unavailable".equals(el.getName())) {
            			pref = RoomSharingModel.sNotAvailablePref.toString();
            		}
            		if (pref == null) continue;
            		
        			if (time.hasDays()) {
        				for (int d: time.getDays())
        					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
        						model.setPreference(d, t, pref);
        			} else {
        				for (int d = 0; d < model.getNrDays(); d++)
        					for (int t = time.getStartPeriod(); t < time.getEndPeriod(); t++)
        						model.setPreference(d, t, pref); 
        			}
            	}
			}
			if (!model.allAvailable(null))
				location.setEventAvailability(model.getPreferences());
			else
				location.setEventAvailability(null);
		}
    }
    
    protected Building lookupBuilding(List<Building> buildings, Element el) throws Exception {
    	String externalId = getOptionalStringAttribute(el, "externalId");
    	if (externalId != null && !externalId.isEmpty()) {
    		for (Iterator<Building> i = buildings.iterator(); i.hasNext(); ) {
    			Building b = i.next();
    			if (externalId.equals(b.getExternalUniqueId())) {
    				i.remove();
    				return b;
    			}
    		}
    	}
    	String abbreviation = getRequiredStringAttribute(el, "abbreviation");
    	for (Iterator<Building> i = buildings.iterator(); i.hasNext(); ) {
			Building b = i.next();
			if (abbreviation.equals(b.getAbbreviation())) {
				i.remove();
				return b;
			}
		}
    	return null;
    }
    
    protected Room lookupRoom(List<Room> rooms, Element el) throws Exception {
    	String externalId = getOptionalStringAttribute(el, "externalId");
    	if (externalId != null && !externalId.isEmpty()) {
    		for (Iterator<Room> i = rooms.iterator(); i.hasNext(); ) {
    			Room r = i.next();
    			if (externalId.equals(r.getExternalUniqueId())) {
    				i.remove();
    				return r;
    			}
    		}
    	}
    	String building = getRequiredStringAttribute(el, "building");
    	String roomNumber = getRequiredStringAttribute(el, "roomNumber");
    	for (Iterator<Room> i = rooms.iterator(); i.hasNext(); ) {
			Room r = i.next();
			if (building.equalsIgnoreCase(r.getBuilding().getAbbreviation()) && roomNumber.equalsIgnoreCase(r.getRoomNumber())) {
				i.remove();
				return r;
			}
		}
    	return null;
    }
    
    protected NonUniversityLocation lookupLocation(List<NonUniversityLocation> locations, Element el) throws Exception {
    	String externalId = getOptionalStringAttribute(el, "externalId");
    	if (externalId != null && !externalId.isEmpty()) {
    		for (Iterator<NonUniversityLocation> i = locations.iterator(); i.hasNext(); ) {
    			NonUniversityLocation r = i.next();
    			if (externalId.equals(r.getExternalUniqueId())) {
    				i.remove();
    				return r;
    			}
    		}
    	}
    	String name = getRequiredStringAttribute(el, "name");
    	boolean hasDept = false;
    	for (Iterator i = el.elementIterator("department"); i.hasNext(); ) {
        	Element element = (Element)i.next();
        	String deptCode = getRequiredStringAttribute(element, "code");
        	hasDept = true;
        	for (Iterator<NonUniversityLocation> j = locations.iterator(); j.hasNext(); ) {
    			NonUniversityLocation r = j.next();
    			if (name.equalsIgnoreCase(r.getName())) {
    				for (RoomDept rd: r.getRoomDepts()) {
    					if (deptCode.equals(rd.getDepartment().getDeptCode())) {
    						j.remove();
    						return r;
    					}
    				}
    			}
        	}
    	}
    	if (!hasDept)
        	for (Iterator<NonUniversityLocation> i = locations.iterator(); i.hasNext(); ) {
    			NonUniversityLocation r = i.next();
    			if (name.equals(r.getName()) && r.getRoomDepts().isEmpty()) {
    				i.remove();
    				return r;
    			}
    		}
    	return null;
    }
    
    private Map<String, ExamPeriod> iExamPeriods = null;
    protected ExamPeriod lookupExamPeriod(String date, String start, String type) {
    	if (iExamPeriods == null) {
    		iExamPeriods = new HashMap<String, ExamPeriod>();
    		for (ExamPeriod period: (List<ExamPeriod>)getHibSession().createQuery(
    				"from ExamPeriod where session.uniqueId = :sessionId",
    				ExamPeriod.class).setParameter("sessionId", iSession.getUniqueId()).list()) {
    			iExamPeriods.put(iDateFormat.format(period.getStartDate()) + "|" + iTimeFormat.format(period.getStartTime()), period);
    			iExamPeriods.put(iDateFormat.format(period.getStartDate()) + "|" + iTimeFormat.format(period.getStartTime()) + "|" + period.getExamType().getReference(), period);
    		}
    	}
    	ExamPeriod period = iExamPeriods.get(date + "|" + start + "|" + type);
    	if (period == null) period = iExamPeriods.get(date + "|" + start);
    	if (period == null) warn("Examination Period " + date + " " + start + " (" + type + ") does not exist.");
    	return period;
    }
}
