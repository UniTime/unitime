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

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.dom.DOMCDATA;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.ExamLocationPref;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.LocationPicture;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.RoomTypeOption;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.Formats;

public class RoomExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat("yyyy/M/d");
	protected static Formats.Format<Date> sTimeFormat = Formats.getDateFormat("HHmm");
	protected boolean iBuildings, iSharing, iPictures, iEvents, iExams;

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();
			
			Element root = document.addElement("rooms");
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("campus", session.getAcademicInitiative());
			iBuildings = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.buildings", "true")));
			iSharing = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.sharing", "true")));
			iPictures = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.pictures", "true")));
			iEvents = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.events", "true")));
			iExams = (parameters == null || "true".equalsIgnoreCase(parameters.getProperty("tmtbl.export.exams", "true")));
	        
	        root.addAttribute("incremental", "false");
	        root.addAttribute("buildings", iBuildings ? "true" : "false");
	        root.addAttribute("sharing", iSharing ? "true" : "false");
	        root.addAttribute("pictures", iPictures ? "true" : "false");
	        root.addAttribute("events", iEvents ? "true" : "false");
	        root.addAttribute("exams", iExams ? "true" : "false");
			root.addAttribute("dateFormat", sDateFormat.toPattern());
	        root.addAttribute("timeFormat", sTimeFormat.toPattern());
	        root.addAttribute("created", new Date().toString());
	        
	        if (iBuildings)
	        	for (Building building: new TreeSet<Building>(getHibSession().createQuery(
		        		"from Building b where b.session.uniqueId = :sessionId order by b.abbreviation", Building.class)
		        		.setParameter("sessionId", session.getUniqueId()).list())) {
	        		exportBuilding(root, building);
		        }	
	        
	        for (Room room: getHibSession().createQuery(
	        		"from Room r where r.session.uniqueId = :sessionId " +
	        		"order by r.building.abbreviation, r.roomNumber", Room.class)
	        		.setParameter("sessionId", session.getUniqueId()).list()) {
	        	exportRoom(root, room);
	        }

	        for (NonUniversityLocation loc: getHibSession().createQuery(
	        		"from NonUniversityLocation loc where loc.session.uniqueId = :sessionId order by loc.name", NonUniversityLocation.class)
	        		.setParameter("sessionId", session.getUniqueId()).list()) {
	        	exportLocation(root, loc);
	        }
	        commitTransaction();
        } catch (Exception e) {
            fatal("Exception: "+e.getMessage(),e);
            rollbackTransaction();
        }
	}
	
	protected void exportBuilding(Element root, Building b) {
		Element e = root.addElement("building");
		if (b.getExternalUniqueId() != null)
			e.addAttribute("externalId", b.getExternalUniqueId());
		if (b.getAbbreviation() != null)
			e.addAttribute("abbreviation", b.getAbbreviation());
		if (b.getAbbrName() != null)
			e.addAttribute("name", b.getName());
		if (b.getExternalUniqueId() != null)
			e.addAttribute("externalId", b.getExternalUniqueId());
		if (b.getCoordinateX() != null)
			e.addAttribute("latitude", b.getCoordinateX().toString());
		if (b.getCoordinateY() != null)
			e.addAttribute("longitude", b.getCoordinateY().toString());
	}
	
	protected void exportGeneric(Element e, Location l) {
		if (l.getExternalUniqueId() != null)
			e.addAttribute("externalId", l.getExternalUniqueId());
		if (iPictures)
			for (LocationPicture p: l.getRoomPictures()) {
				Element pe = e.addElement("picture");
				if (p.getFileName() != null)
					pe.addAttribute("fileName", p.getFileName());
				if (p.getContentType() != null)
					pe.addAttribute("contentType", p.getContentType());
				if (p.getType() != null)
					pe.addAttribute("type", p.getType().getReference());
				if (p.getDataFile() != null)
					pe.setText(Base64.getEncoder().encodeToString(p.getDataFile()));
			}
		if (iEvents)
			for (EventServiceProvider service: l.getAllowedServices()) {
				Element se = e.addElement("service");
				se.addAttribute("reference", service.getReference());
				if (service.getLabel() != null)
					se.addAttribute("name", service.getLabel());
				if (Boolean.FALSE.equals(service.isVisible()))
					se.addAttribute("visible", "false");
				if (service.getNote() != null)
					se.addAttribute("note", service.getNote());
				if (service.getEmail() != null)
					se.addAttribute("email", service.getEmail());
				if (service.getDepartment() != null)
					se.addAttribute("department", service.getDepartment().getDeptCode());
			}
		if (l.getCoordinateX() != null)
			e.addAttribute("latitude", l.getCoordinateX().toString());
		if (l.getCoordinateY() != null)
			e.addAttribute("longitude", l.getCoordinateY().toString());
		if (l.getCapacity() != null)
			e.addAttribute("capacity", l.getCapacity().toString());
		if (Boolean.TRUE.equals(l.getIgnoreTooFar()))
			e.addAttribute("ignoreTooFar", "true");
		if (Boolean.TRUE.equals(l.getIgnoreRoomCheck()))
			e.addAttribute("ignoreRoomCheck", "true");
		if (l.getArea() != null)
			e.addAttribute("area", l.getArea().toString());
		if (iEvents) {
			if (l.getEventStatus() != null)
				e.addAttribute("eventStatus", RoomTypeOption.Status.values()[l.getEventStatus()].name());
			if (l.getNote() != null)
				e.addElement("eventNote").add(new DOMCDATA(l.getNote()));
			if (l.getBreakTime() != null)
				e.addAttribute("breakTime", l.getBreakTime().toString());
			if (l.getEventEmail() != null)
				e.addAttribute("eventEmail", l.getEventEmail());
			if (l.getEventDepartment() != null)
				e.addAttribute("eventDepartment", l.getEventDepartment().getDeptCode());
		}
		if (l.getExamCapacity() != null)
			e.addAttribute("examCapacity", l.getExamCapacity().toString());
		if (l.getDisplayName() != null)
			e.addAttribute("displayName", l.getDisplayName());
		if (l.getRoomType() != null)
			e.addAttribute("type", l.getRoomType().getReference());
		for (RoomFeature f: l.getFeatures()) {
			Element fe = e.addElement("feature");
			fe.addAttribute("code", f.getAbbv());
			fe.addAttribute("name", f.getLabel());
			if (f.getDescription() != null)
				fe.addAttribute("description", f.getDescription());
			if (f instanceof DepartmentRoomFeature)
				fe.addAttribute("department", ((DepartmentRoomFeature)f).getDepartment().getDeptCode());
			if (f.getFeatureType() != null)
				fe.addAttribute("type", f.getFeatureType().getReference());
		}
		for (RoomGroup g: l.getGlobalRoomGroups()) {
			Element ge = e.addElement("group");
			ge.addAttribute("code", g.getAbbv());
			ge.addAttribute("name", g.getName());
			if (g.getDescription() != null)
				ge.addAttribute("description", g.getDescription());
			if (!g.isGlobal() && g.getDepartment() != null)
				ge.addAttribute("department", g.getDepartment().getDeptCode());
		}
		Map<Long, Department> id2dept = new HashMap<Long, Department>();
		for (RoomDept rd : l.getRoomDepts()) {
			Element de = e.addElement("department");
			de.addAttribute("code", rd.getDepartment().getDeptCode());
			if (rd.getPreference() != null)
				de.addAttribute("preference", rd.getPreference().getPrefProlog());
			if (rd.isControl())
				de.addAttribute("control", "true");
			id2dept.put(rd.getDepartment().getUniqueId(), rd.getDepartment());
		}
		if (iSharing) {
			RoomSharingModel model = l.getRoomSharingModel();
			if (model != null && !model.allAvailable(null)) {
				Element sharingEl = e.addElement("sharing");
				exportRoomSharing(model, sharingEl, id2dept);
				if (l.getShareNote() != null)
					sharingEl.addAttribute("note", l.getShareNote());
			}
		}
		if (iExams) {
			for (ExamType et: l.getExamTypes()) {
				Element xe = e.addElement("exam");
				xe.addAttribute("type", et.getReference());
				for (ExamLocationPref pref: l.getExamPreferences()) {
					if (pref.getExamPeriod().getExamType().equals(et)) {
						Element pe = xe.addElement("preference");
						pe.addAttribute("date", sDateFormat.format(pref.getExamPeriod().getStartDate()));
						pe.addAttribute("start", sTimeFormat.format(pref.getExamPeriod().getStartTime()));
						pe.addAttribute("level", pref.getPrefLevel().getPrefProlog());
					}
				}
			}
		}
		if (iEvents && l.getEventAvailability() != null && !l.getEventAvailability().isEmpty()) {
			RoomSharingModel model = new RoomSharingModel(l, null, null);
			model.setEventAvailabilityPreference(l.getEventAvailability());
			if (!model.allAvailable(null))
				exportRoomSharing(model, e.addElement("availability"), id2dept);
		}
	}
	
	protected void exportRoomSharing(RoomSharingModel model, Element sharingEl, Map<Long, Department> id2dept) {
		boolean out[][] = new boolean[model.getNrDays()][model.getNrTimes()];
		for (int i = 0; i < model.getNrDays(); i++)
			for (int j = 0; j < model.getNrTimes(); j++)
				out[i][j] = false;
		for (int i = 0; i < model.getNrDays(); i++)
			for (int j = 0; j < model.getNrTimes(); j++) {
				if (out[i][j])
					continue;
				out[i][j] = true;
				if (model.isFreeForAll(i, j))
					continue;
				int endDay = i, endTime = j;
				String p = model.getPreference(i, j);
				while (endTime + 1 < model.getNrTimes() && !out[i][endTime + 1] && model.getPreference(i, endTime + 1).equals(p))
					endTime++;
				while (endDay + 1 < model.getNrDays()) {
					boolean same = true;
					for (int x = j; x <= endTime; x++)
						if (!out[endDay + 1][x] && !model.getPreference(endDay + 1, x).equals(p)) {
							same = false;
							break;
						}
					if (!same)
						break;
					endDay++;
				}
				for (int a = i; a <= endDay; a++)
					for (int b = j; b <= endTime; b++)
						out[a][b] = true;
				Element el = null;
				Department dept = null;
				if (model.isNotAvailable(i, j)) {
					el = sharingEl.addElement("unavailable");
				} else {
					dept = id2dept.get(model.getDepartmentId(i, j));
					if (dept == null)
						continue;
					el = sharingEl.addElement("assigned");
					el.addAttribute("code", dept.getDeptCode());
				}
				if (i == 0 && endDay + 1 == model.getNrDays()) {
					// all week
				} else {
					String day = "";
					for (int a = i; a <= endDay; a++)
						day += Constants.DAY_NAMES_SHORT[a];
					el.addAttribute("days", day);
				}
				if (j == 0 && endTime + 1 == model.getNrTimes()) {
					// all day
				} else {
					el.addAttribute("start", slot2time(j));
					el.addAttribute("end", slot2time(endTime + 1));
				}
			}
	}
	
	protected String slot2time(int slot) {
		int minutesSinceMidnight = Constants.SLOT_LENGTH_MIN * slot + Constants.FIRST_SLOT_TIME_MIN;
		return sTwoNumbersDF.format(minutesSinceMidnight / 60) + sTwoNumbersDF.format(minutesSinceMidnight % 60);
	}

	protected void exportRoom(Element root, Room r) {
		Element e = root.addElement("room");
		e.addAttribute("building", r.getBuildingAbbv());
		e.addAttribute("roomNumber", r.getRoomNumber());
		if (r.getClassification() != null)
			e.addAttribute("classification", r.getClassification());
		if (r.getParentRoom() != null)
			e.addAttribute("parent", r.getParentRoom().getLabel());
		exportGeneric(e, r);
	}
	
	protected void exportLocation(Element root, NonUniversityLocation loc) {
		Element e = root.addElement("location");
		e.addAttribute("name", loc.getName());
		exportGeneric(e, loc);
	}

}
