/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.dataexchange;

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.RoomType;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon, Tomas Muller, Stephanie Schluttenhofer
 *
 */
public class BuildingRoomImport extends BaseImport {
	private static int BATCH_SIZE = 100;

	public BuildingRoomImport() {
	}
	
	public void loadXml(Element root) throws Exception{
		try {
			beginTransaction();
	        String campus = root.attributeValue("campus");
	        String year   = root.attributeValue("year");
	        String term   = root.attributeValue("term");
	        String created = root.attributeValue("created");

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	        if (created != null) {
				ChangeLog.addChange(getHibSession(), getManager(), session, session, created, ChangeLog.Source.DATA_IMPORT_EXT_BUILDING_ROOM, ChangeLog.Operation.UPDATE, null, null);
	        }
           /* 
             * Remove all buildings and rooms for the given session and reload them using the xml 
             */
            
            getHibSession().createQuery("delete ExternalBuilding eb where eb.session.uniqueId=:sessionId").setLong("sessionId", session.getUniqueId()).executeUpdate();
            
            flush(true);
            
			for (Iterator i = root.elementIterator(); i.hasNext();) {
				importBuildings((Element) i.next(), session);
			}
			commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}

	public void importBuildings(Element element, Session session) throws Exception {
		int batchIdx = 0;
		ExternalBuilding building = new ExternalBuilding();
		building.setExternalUniqueId(element.attributeValue("externalId"));
		building.setAbbreviation(element.attributeValue("abbreviation"));
		building.setCoordinateX(element.attributeValue("locationX") == null ? null : Double.valueOf(element.attributeValue("locationX")));
		building.setCoordinateY(element.attributeValue("locationY") == null ? null : Double.valueOf(element.attributeValue("locationY")));
		building.setDisplayName(element.attributeValue("name"));
		building.setSession(session);
		getHibSession().save(building);
		for (Iterator i = element.elementIterator("room"); i.hasNext();)
			importRoom((Element) i.next(), building);
		getHibSession().save(building);
		if (++batchIdx == BATCH_SIZE) {
			getHibSession().flush(); getHibSession().clear(); batchIdx = 0;
		}
	}

	private void importRoom(Element element, ExternalBuilding building) throws Exception {
		ExternalRoom room = new ExternalRoom();
		room.setExternalUniqueId(element.attributeValue("externalId"));
		room.setCoordinateX(element.attributeValue("locationX") == null ? building.getCoordinateX() : Double.valueOf(element.attributeValue("locationX")));
		room.setCoordinateY(element.attributeValue("locationY") == null ? building.getCoordinateY() : Double.valueOf(element.attributeValue("locationY")));
		room.setArea(element.attributeValue("area") == null ? null : Double.valueOf(element.attributeValue("area")));
		room.setRoomNumber(element.attributeValue("roomNumber"));
		room.setClassification(element.attributeValue("roomClassification", ""));
		if (room.getClassification().length() > 20) {
			warn("Room classification " + room.getClassification() + " is too long, truncated to " + room.getClassification().substring(0, 20));
			room.setClassification(room.getClassification().substring(0, 20));
		}
		room.setCapacity(Integer.decode(element.attributeValue("capacity", "0")));
		String examCapacityStr = element.attributeValue("examCapacity");
		if (examCapacityStr != null && examCapacityStr.trim().length() > 0){
			room.setExamCapacity(Integer.decode(examCapacityStr));
		} else {
			room.setExamCapacity(new Integer(0));
		}
		room.setIsInstructional(Boolean.valueOf(element.attributeValue("instructional")));
		room.setRoomType(RoomType.findByReference(element.attributeValue("scheduledRoomType")));
		if (room.getRoomType() == null) {
			room.setRoomType(RoomType.findByReference(element.attributeValue("roomClassification")));
			if (room.getRoomType() == null) {
				warn("Invalid scheduled room type '" + element.attributeValue("scheduledRoomType", element.attributeValue("roomClassification")) + "' for room " + building.getAbbreviation() + " " + room.getRoomNumber() + ", using " + RoomType.findAll(true).first().getReference() + " instead.");
				room.setRoomType(RoomType.findAll(true).first());
			}
		}
		room.setBuilding(building);
		room.setDisplayName(element.attributeValue("displayName"));
		building.addTorooms(room);
		getHibSession().save(room);
		if(element.element("roomDepartments") != null) {
			for (Iterator i = element.element("roomDepartments").elementIterator(); i.hasNext();)
				importDepts((Element) i.next(), room);
		}
		if(element.element("roomFeatures") != null) {
			for (Iterator i = element.element("roomFeatures").elementIterator(); i.hasNext();)
				importFeatures((Element) i.next(), room);
		}
	}

	private void importDepts(Element element, ExternalRoom room) throws Exception {
		ExternalRoomDepartment dept = new ExternalRoomDepartment();
		dept.setAssignmentType(element.getName());
		String deptCode = element.attributeValue("departmentNumber");
		if(deptCode == null) {
			deptCode = "0000";
		}
		dept.setDepartmentCode(deptCode);
		String percent = element.attributeValue("percent");
		if(percent == null) {
			dept.setPercent(new Integer(100));
		}
		else {
			dept.setPercent(Integer.decode(percent));
		}
		dept.setRoom(room);
		room.addToroomDepartments(dept);
		getHibSession().save(dept);
	}

	private void importFeatures(Element element, ExternalRoom room) throws Exception {
		ExternalRoomFeature feature = new ExternalRoomFeature();
		feature.setName(element.attributeValue("feature"));
		if(feature.getName() == null)
			throw new Exception("Room feature name for room " + room.getExternalUniqueId() + " not present.");
		feature.setValue(element.attributeValue("value"));
		if (feature.getValue() != null && feature.getValue().length() > 20) {
			warn("Feature value " + feature.getValue() + " is too long, truncated to " + feature.getValue().substring(0, 20));
			feature.setValue(feature.getValue().substring(0, 20));
		}
		feature.setRoom(room);
		room.addToroomFeatures(feature);
		getHibSession().save(feature);
	}
	

}
