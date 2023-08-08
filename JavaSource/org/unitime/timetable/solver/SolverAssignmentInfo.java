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
package org.unitime.timetable.solver;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy.AssignmentInfo;

/**
 * @author Tomas Muller
 */
public class SolverAssignmentInfo implements AssignmentInfo {
	private static final long serialVersionUID = 6729805504556505822L;
	private Long iUniqueId;
	private Long iClassId;
	private String iClassName;
	private transient Class_ iClazz;
	private int iDays;
	private int iStartSlot;
	private int iSlotPerMtg;
	private Long iTimePatternId, iDatePatternId;
	private String iDatePatternName;
	private BitSet iWeekCode;
	private int iBreakTime;
	private boolean iCommited;
	private transient TimePattern iTimePattern;
	private transient DatePattern iDatePattern;
	private transient TimeLocation iTimeLocation;
	private List<Long> iRoomIds = null;
	private transient Set<Location> iRooms;
	private transient Placement iPlacement;
	private transient List<RoomLocation> iRoomLocations;
	
	public SolverAssignmentInfo(Lecture lecture, Placement placement) {
		iClassId = lecture.getClassId();
		iClassName = lecture.getName();
		iCommited = lecture.isCommitted();
		iPlacement = placement;
		iTimeLocation = placement.getTimeLocation();
		iStartSlot = iTimeLocation.getStartSlot();
		iDays = iTimeLocation.getDayCode();
		iSlotPerMtg = iTimeLocation.getLength();
		iTimePatternId = iTimeLocation.getTimePatternId();
		iDatePatternId = iTimeLocation.getDatePatternId();
		iDatePatternName = iTimeLocation.getDatePatternName();
		iWeekCode = iTimeLocation.getWeekCode();
		iBreakTime = iTimeLocation.getBreakTime();
		iRoomIds = new ArrayList<Long>(placement.getNrRooms());
		if (placement.getNrRooms() == 1)
			iRoomIds.add(placement.getRoomLocation().getId());
		else if (placement.getNrRooms() > 1) {
			for (RoomLocation room: placement.getRoomLocations())
				iRoomIds.add(room.getId());
		}
	}
	
	public SolverAssignmentInfo(Assignment assignment) {
		iUniqueId = assignment.getUniqueId();
		iClassId = assignment.getClassId();
		iClassName = assignment.getClassName();
		iCommited = assignment.isCommitted();
		iPlacement = assignment.getPlacement();
		iTimeLocation = iPlacement.getTimeLocation();
		iStartSlot = iTimeLocation.getStartSlot();
		iDays = iTimeLocation.getDayCode();
		iSlotPerMtg = iTimeLocation.getLength();
		iTimePatternId = iTimeLocation.getTimePatternId();
		iDatePatternId = iTimeLocation.getDatePatternId();
		iDatePatternName = iTimeLocation.getDatePatternName();
		iWeekCode = iTimeLocation.getWeekCode();
		iBreakTime = iTimeLocation.getBreakTime();
		iRoomIds = new ArrayList<Long>(iPlacement.getNrRooms());
		if (iPlacement.getNrRooms() == 1)
			iRoomIds.add(iPlacement.getRoomLocation().getId());
		else if (iPlacement.getNrRooms() > 1) {
			for (RoomLocation room: iPlacement.getRoomLocations())
				iRoomIds.add(room.getId());
		}
	}

	@Override
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Override
	public Long getClassId() { return iClassId; }

	@Override
	public String getClassName() { return iClassName; }

	@Override
	public Class_ getClazz() {
		if (iClassId == null) return null;
		if (iClazz == null)
			iClazz = Class_DAO.getInstance().get(iClassId);
		return iClazz;
	}

	@Override
	public Integer getDays() { return iDays; }
	@Override
	public Integer getStartSlot() { return iStartSlot; }
	@Override
	public int getSlotPerMtg() { return iSlotPerMtg; }

	@Override
	public TimePattern getTimePattern() {
		if (iTimePatternId == null) return null;
		if (iTimePattern == null)
			iTimePattern = TimePatternDAO.getInstance().get(iTimePatternId);
		return iTimePattern;
	}

	@Override
	public DatePattern getDatePattern() {
		if (iDatePatternId == null) return null;
		if (iDatePattern == null)
			iDatePattern = DatePatternDAO.getInstance().get(iDatePatternId);
		return iDatePattern;
	}

	@Override
	public TimeLocation getTimeLocation() {
		if (iTimeLocation == null)
			iTimeLocation = new TimeLocation(iDays, iStartSlot, iSlotPerMtg, 0, 0.0, iDatePatternId, iDatePatternName, iWeekCode, iBreakTime);
		return iTimeLocation;
	}

	@Override
	public Set<Location> getRooms() {
		if (iRooms == null) {
			iRooms = new HashSet<Location>();
			for (Long roomId: iRoomIds)
				iRooms.add(LocationDAO.getInstance().get(roomId));
		}
		return iRooms;
	}

	@Override
	public Placement getPlacement() {
		if (iPlacement == null) {
			iPlacement = new Placement(new Lecture(iClassId, null, iClassName),
					getTimeLocation(),
					getRoomLocations());
		}
		return iPlacement;
	}
	
	@Override
	public List<RoomLocation> getRoomLocations() {
		if (iRoomLocations == null) {
			iRoomLocations = new ArrayList<RoomLocation>();
			for (Location room: getRooms()) {
				iRoomLocations.add(new RoomLocation(
						room.getUniqueId(),
						room.getLabel(),
						(room instanceof Room? ((Room)room).getBuilding().getUniqueId() : null),
						0,
						room.getCapacity().intValue(),
						room.getCoordinateX(),
						room.getCoordinateY(),
						room.isIgnoreTooFar().booleanValue(),
						null));
			}
		}
		return iRoomLocations;
	}

	@Override
	public boolean isCommitted() {
		return iCommited;
	}

	@Override
	public boolean overlaps(AssignmentInfo a) {
		return getTimeLocation().hasIntersection(a.getTimeLocation());
	}
}
