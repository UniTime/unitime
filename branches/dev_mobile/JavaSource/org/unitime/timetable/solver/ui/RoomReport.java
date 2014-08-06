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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.RoomConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.util.Constants;



/**
 * @author Tomas Muller
 */
public class RoomReport implements Serializable {
	public static int[] sGroupSizes = new int[] {0, 10, 20, 40, 60, 80, 100, 150, 200, 400, Integer.MAX_VALUE};
	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();
    private Long iRoomType = null;
    private BitSet iSessionDays = null;
    private int iStartDayDayOfWeek = 0;
    private double iNrWeeks = 0.0;

	public RoomReport(Solver<Lecture, Placement> solver, BitSet sessionDays, int startDayDayOfWeek, Long roomType) {
		TimetableModel model = (TimetableModel) solver.currentSolution().getModel();
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		iSessionDays = sessionDays;
		iStartDayDayOfWeek = startDayDayOfWeek;
        iRoomType = roomType;
        
		// count number of weeks as a number of working days / 5
        // (this is to avoid problems when the default date pattern does not contain Saturdays and/or Sundays)
		int dow = iStartDayDayOfWeek;
		int nrDays[] = new int[] {0, 0, 0, 0, 0, 0, 0};
		for (int day = iSessionDays.nextSetBit(0); day < iSessionDays.length(); day++) {
			if (iSessionDays.get(day)) nrDays[dow]++;
			dow = (dow + 1) % 7;
		}
		iNrWeeks = 0.2 * (
				nrDays[Constants.DAY_MON] +
				nrDays[Constants.DAY_TUE] +
				nrDays[Constants.DAY_WED] +
				nrDays[Constants.DAY_THU] +
				nrDays[Constants.DAY_FRI] );
		
		for (int i=0;i<sGroupSizes.length-1;i++) {
			iGroups.add(new RoomAllocationGroup(sGroupSizes[i],sGroupSizes[i+1]));
		}
		for (RoomConstraint rc: model.getRoomConstraints()) {
			if (!ToolBox.equals(iRoomType,rc.getType())) continue;
			for (Iterator i=iGroups.iterator();i.hasNext();) {
				RoomAllocationGroup g = (RoomAllocationGroup)i.next();
				g.add(rc);
			}
		}
		for (Lecture lecture: model.variables()) {
			for (Iterator i=iGroups.iterator();i.hasNext();) {
				RoomAllocationGroup g = (RoomAllocationGroup)i.next();
				g.add(lecture, assignment.getValue(lecture));
			}
		}
	}
	
	public Set getGroups() {
		return iGroups;
	}
	
	public class RoomAllocationGroup implements Serializable {
		private static final long serialVersionUID = 1L;
		private int iMinRoomSize = 0;
		private int iMaxRoomSize = 0;
		private int iNrRooms = 0;
		private int iNrRoomsThisSizeOrBigger = 0;
		private double iSlotsUse = 0;
		private double iSlotsMustUse= 0;
		private double iSlotsMustUseThisSizeOrBigger= 0;
		private double iSlotsCanUse = 0;
		private int iLecturesUse = 0;
		private int iLecturesMustUse= 0;
		private int iLecturesMustUseThisSizeOrBigger= 0;
		private int iLecturesCanUse = 0;
		private int iRealMinRoomSize = 0;
		private int iRealMaxRoomSize = 0;
		private int iLecturesShouldUse = 0;
		private double iSlotsShouldUse = 0;
		
		public RoomAllocationGroup(int minSize, int maxSize) {
			iMinRoomSize = minSize;
			iMaxRoomSize = maxSize;
			iRealMinRoomSize = maxSize;
			iRealMaxRoomSize = minSize;
		}
		
		public int getMinRoomSize() { return iMinRoomSize; }
		public int getMaxRoomSize() { return iMaxRoomSize; }
		public int getActualMinRoomSize() { return iRealMinRoomSize; }
		public int getActualMaxRoomSize() { return iRealMaxRoomSize; }
		public int getNrRooms() { return iNrRooms; }
		public int getNrRoomsThisSizeOrBigger() { return iNrRoomsThisSizeOrBigger; }
		
		public double getSlotsUse() { return iSlotsUse; }
		public double getSlotsCanUse() { return iSlotsCanUse; }
		public double getSlotsMustUse() { return iSlotsMustUse; }
		public double getSlotsMustUseThisSizeOrBigger() { return iSlotsMustUseThisSizeOrBigger; }
		public double getSlotsShouldUse() { return iSlotsShouldUse; }
		
		public int getLecturesUse() { return iLecturesUse; }
		public int getLecturesCanUse() { return iLecturesCanUse; }
		public int getLecturesMustUse() { return iLecturesMustUse; }
		public int getLecturesMustUseThisSizeOrBigger() { return iLecturesMustUseThisSizeOrBigger; }
		public int getLecturesShouldUse() { return iLecturesShouldUse; }
		
		public void add(RoomConstraint rc) {
			if (iMinRoomSize<=rc.getCapacity() && rc.getCapacity()<iMaxRoomSize) {
				iNrRooms++;
				iRealMinRoomSize = Math.min(iRealMinRoomSize,rc.getCapacity());
				iRealMaxRoomSize = Math.max(iRealMaxRoomSize,rc.getCapacity());
			}
			if (iMinRoomSize<=rc.getCapacity())
				iNrRoomsThisSizeOrBigger++;
		}
		public void add(Lecture lecture, Placement placement) {
			if (lecture.getNrRooms()==0) return;
			boolean skip = false;
			if (lecture.canShareRoom()) {
				for (Iterator i=lecture.canShareRoomConstraints().iterator();i.hasNext();) {
					GroupConstraint gc = (GroupConstraint)i.next();
					for (Lecture other: gc.variables()) {
						if (other.getClassId().compareTo(lecture.getClassId())<0)
							skip=true;
					}
				}
			}
			if (skip) return;
            
            skip = true;
			boolean canUse = false, mustUse = true, mustUseThisSizeOrBigger = true;
			for (RoomLocation r: lecture.roomLocations()) {
                if (r.getRoomConstraint()==null) continue;
				if (PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(r.getPreference()))) continue;
                if (!ToolBox.equals(iRoomType,r.getRoomConstraint().getType())) {
                	mustUse = false;
					mustUseThisSizeOrBigger = false;
                	continue;
                }
                skip = false;
				if (iMinRoomSize<=r.getRoomSize() && r.getRoomSize()<iMaxRoomSize)
					canUse = true;
				else
					mustUse = false;
				if (r.getRoomSize()<iMinRoomSize)
					mustUseThisSizeOrBigger = false;
			}
            if (skip) return;
            
			boolean shouldUse = canUse && mustUseThisSizeOrBigger;
			if (canUse) {
				iSlotsCanUse += getSlotsAWeek(lecture.timeLocations()) * lecture.getNrRooms();
				iLecturesCanUse += lecture.getNrRooms();
			}
			if (mustUse) {
				iSlotsMustUse += getSlotsAWeek(lecture.timeLocations()) * lecture.getNrRooms();
				iLecturesMustUse += lecture.getNrRooms();
			}
			if (mustUseThisSizeOrBigger) {
				iSlotsMustUseThisSizeOrBigger += getSlotsAWeek(lecture.timeLocations()) * lecture.getNrRooms();
				iLecturesMustUseThisSizeOrBigger += lecture.getNrRooms();
			}
			if (shouldUse) {
				iSlotsShouldUse += getSlotsAWeek(lecture.timeLocations()) * lecture.getNrRooms();
				iLecturesShouldUse += lecture.getNrRooms();
			}

			int use = 0;
			if (placement!=null) {
				if (placement.isMultiRoom()) {
					for (RoomLocation r: placement.getRoomLocations()) {
                        if (r.getRoomConstraint()==null) continue;
                        if (!ToolBox.equals(iRoomType,r.getRoomConstraint().getType())) continue;
						if (iMinRoomSize<=r.getRoomSize() && r.getRoomSize()<iMaxRoomSize)
							use++;
					}
				} else {
                    if (placement.getRoomLocation().getRoomConstraint()!=null &&
                            ToolBox.equals(iRoomType,placement.getRoomLocation().getRoomConstraint().getType()) &&
                            iMinRoomSize<=placement.getRoomLocation().getRoomSize() && 
                            placement.getRoomLocation().getRoomSize()<iMaxRoomSize
                            )
                        use++;
				}
				if (use>0) {
					TimeLocation t = placement.getTimeLocation(); 
					iSlotsUse += getSlotsAWeek(t) * use;
					iLecturesUse += use;
				}
			}
		}
		
		public double getSlotsAWeek(Collection<TimeLocation> times) {
			if (times.isEmpty()) return 0;
			double totalHoursAWeek = 0;
			for (TimeLocation t: times)
				totalHoursAWeek += getSlotsAWeek(t);
			return ((double)totalHoursAWeek) / times.size();
		}
		
		public double getSlotsAWeek(TimeLocation t) {
			return getAverageDays(t) * t.getNrSlotsPerMeeting();
		}
		
		public double getAverageDays(TimeLocation t) {
			int nrDays = 0;
			int dow = iStartDayDayOfWeek;
			for (int day = iSessionDays.nextSetBit(0); day < iSessionDays.length(); day++) {
				if (iSessionDays.get(day) && t.getWeekCode().get(day) && (t.getDayCode() & Constants.DAY_CODES[dow]) != 0) nrDays++;
				dow = (dow + 1) % 7;
			}
			return ((double)nrDays) / iNrWeeks;
		}
	}

}
