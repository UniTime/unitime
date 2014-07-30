/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cpsolver.coursett.Constants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomSharingModel;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.Formats;

/**
 * 
 * @author Tomas Muller
 *
 */
public class RoomSharingExport extends BaseExport {
	protected static Formats.Format<Number> sTwoNumbersDF = Formats.getNumberFormat("00");

	@Override
	public void saveXml(Document document, Session session, Properties parameters) throws Exception {
		try {
			beginTransaction();

			Element root = document.addElement("roomSharing");
			root.addAttribute("campus", session.getAcademicInitiative());
			root.addAttribute("year", session.getAcademicYear());
			root.addAttribute("term", session.getAcademicTerm());
			root.addAttribute("created", new Date().toString());
			root.addAttribute("timeFormat", "HHmm");
			

			document.addDocType("roomSharing", "-//UniTime//UniTime Room Sharing DTD/EN", "http://www.unitime.org/interface/RoomSharing.dtd");

			List<Location> locations = Location.findAll(session.getUniqueId());
			Collections.sort(locations);

			for (Location location : locations) {
				Element locEl = root.addElement("location");
				fillLocationData(location, locEl);
				if (location.getShareNote() != null)
					locEl.addAttribute("note", location.getShareNote());
				Map<Long, Department> id2dept = new HashMap<Long, Department>();
				for (RoomDept rd : location.getRoomDepts()) {
					Element deptEl = locEl.addElement("department");
					fillDepartmentData(rd, deptEl);
					id2dept.put(rd.getDepartment().getUniqueId(), rd.getDepartment());
				}
				RoomSharingModel model = location.getRoomSharingModel();
				if (model != null && !model.allAvailable(null)) {
					Element sharingEl = locEl.addElement("sharing");
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
							if (dept != null)
								fillDepartmentData(dept, el);
						}
				}
			}

			commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
		}
	}

	protected void fillLocationData(Location location, Element element) {
		if (location instanceof Room) {
			Room room = (Room) location;
			element.addAttribute("building", room.getBuilding().getAbbreviation());
			element.addAttribute("roomNbr", room.getRoomNumber());
		} else {
			element.addAttribute("name", location.getLabel());
		}
		if (location.getExternalUniqueId() != null && !location.getExternalUniqueId().isEmpty())
			element.addAttribute("id", location.getExternalUniqueId());
	}

	protected void fillDepartmentData(RoomDept rd, Element element) {
		fillDepartmentData(rd.getDepartment(), element);
		if (rd.isControl())
			element.addAttribute("control", "true");
	}

	protected void fillDepartmentData(Department dept, Element element) {
		if (dept.getExternalUniqueId() != null)
			element.addAttribute("id", dept.getExternalUniqueId());
		element.addAttribute("code", dept.getDeptCode());
	}

	protected String slot2time(int slot) {
		int minutesSinceMidnight = Constants.SLOT_LENGTH_MIN * slot + Constants.FIRST_SLOT_TIME_MIN;
		return sTwoNumbersDF.format(minutesSinceMidnight / 60) + sTwoNumbersDF.format(minutesSinceMidnight % 60);
	}
}