/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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
package org.unitime.timetable.dataexchange;

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.timetable.model.ExternalBuilding;
import org.unitime.timetable.model.ExternalRoom;
import org.unitime.timetable.model.ExternalRoomDepartment;
import org.unitime.timetable.model.ExternalRoomFeature;
import org.unitime.timetable.model.Session;


/**
 * 
 * @author Timothy Almon
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

	        Session session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
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
		building.setCoordinateX(Integer.decode(element.attributeValue("locationX")));
		building.setCoordinateY(Integer.decode(element.attributeValue("locationY")));
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
		room.setCoordinateX(Integer.decode(element.attributeValue("locationX")));
		room.setCoordinateY(Integer.decode(element.attributeValue("locationY")));
		room.setRoomNumber(element.attributeValue("roomNumber"));
		room.setClassification(element.attributeValue("roomClassification"));
		room.setCapacity(Integer.decode(element.attributeValue("capacity")));
		room.setIsInstructional(Boolean.valueOf(element.attributeValue("instructional")));
		room.setScheduledRoomType(element.attributeValue("scheduledRoomType"));
		room.setBuilding(building);
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
		feature.setRoom(room);
		room.addToroomFeatures(feature);
		getHibSession().save(feature);
	}
}
