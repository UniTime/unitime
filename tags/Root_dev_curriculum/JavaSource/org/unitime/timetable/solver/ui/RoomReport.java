/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.unitime.timetable.model.PreferenceLevel;

import net.sf.cpsolver.coursett.constraint.GroupConstraint;
import net.sf.cpsolver.coursett.constraint.RoomConstraint;
import net.sf.cpsolver.coursett.model.Lecture;
import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.coursett.model.TimetableModel;
import net.sf.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class RoomReport implements Serializable {
	public static int[] sGroupSizes = new int[] {0, 10, 20, 40, 60, 80, 100, 150, 200, 400, Integer.MAX_VALUE};
	private static final long serialVersionUID = 1L;
	private HashSet iGroups = new HashSet();
	private int iStartDay, iEndDay, iNrWeeks;
    private Long iRoomType = null;

	public RoomReport(TimetableModel model, int startDay, int endDay, int nrWeeks, Long roomType) {
		iStartDay = startDay; iEndDay = endDay;
		iNrWeeks = nrWeeks;
        iRoomType = roomType;
		for (int i=0;i<sGroupSizes.length-1;i++) {
			iGroups.add(new RoomAllocationGroup(sGroupSizes[i],sGroupSizes[i+1]));
		}
		for (Enumeration e=model.getRoomConstraints().elements();e.hasMoreElements();) {
			RoomConstraint rc = (RoomConstraint)e.nextElement();
			if (!ToolBox.equals(iRoomType,rc.getType())) continue;
			for (Iterator i=iGroups.iterator();i.hasNext();) {
				RoomAllocationGroup g = (RoomAllocationGroup)i.next();
				g.add(rc);
			}
		}
		for (Enumeration e=model.variables().elements();e.hasMoreElements();) {
			Lecture lecture = (Lecture)e.nextElement();
			for (Iterator i=iGroups.iterator();i.hasNext();) {
				RoomAllocationGroup g = (RoomAllocationGroup)i.next();
				g.add(lecture);
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
		public void add(Lecture lecture) {
			if (lecture.getNrRooms()==0) return;
			boolean skip = false;
			if (lecture.canShareRoom()) {
				for (Iterator i=lecture.canShareRoomConstraints().iterator();i.hasNext();) {
					GroupConstraint gc = (GroupConstraint)i.next();
					for (Enumeration e=gc.variables().elements();e.hasMoreElements();) {
						Lecture other = (Lecture)e.nextElement();
						if (other.getClassId().compareTo(lecture.getClassId())<0)
							skip=true;
					}
				}
			}
			if (skip) return;
            
            skip = true;
			boolean canUse = false, mustUse = true, mustUseThisSizeOrBigger = true;
			for (Enumeration e=lecture.roomLocations().elements();e.hasMoreElements();) {
				RoomLocation r = (RoomLocation)e.nextElement();
                if (r.getRoomConstraint()==null) continue;
                if (!ToolBox.equals(iRoomType,r.getRoomConstraint().getType())) continue;
				if (PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(r.getPreference()))) continue;
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
				TimeLocation t = (TimeLocation)lecture.timeLocations().firstElement();
				iSlotsCanUse += (((double)t.getNrWeeks(iStartDay,iEndDay))/iNrWeeks)*lecture.getNrRooms()*t.getNrMeetings()*t.getNrSlotsPerMeeting();
				iLecturesCanUse += lecture.getNrRooms();
			}
			if (mustUse) {
				TimeLocation t = (TimeLocation)lecture.timeLocations().firstElement();
				iSlotsMustUse += (((double)t.getNrWeeks(iStartDay,iEndDay))/iNrWeeks)*lecture.getNrRooms()*t.getNrMeetings()*t.getNrSlotsPerMeeting();
				iLecturesMustUse += lecture.getNrRooms();
			}
			if (mustUseThisSizeOrBigger) {
				TimeLocation t = (TimeLocation)lecture.timeLocations().firstElement();
				iSlotsMustUseThisSizeOrBigger += (((double)t.getNrWeeks(iStartDay,iEndDay))/iNrWeeks)*lecture.getNrRooms()*t.getNrMeetings()*t.getNrSlotsPerMeeting();
				iLecturesMustUseThisSizeOrBigger += lecture.getNrRooms();
			}
			if (shouldUse) {
				TimeLocation t = (TimeLocation)lecture.timeLocations().firstElement();
				iSlotsShouldUse += (((double)t.getNrWeeks(iStartDay,iEndDay))/iNrWeeks)*lecture.getNrRooms()*t.getNrMeetings()*t.getNrSlotsPerMeeting();
				iLecturesShouldUse += lecture.getNrRooms();
			}

			int use = 0;
			if (lecture.getAssignment()!=null) {
				Placement placement = (Placement)lecture.getAssignment();
				if (placement.isMultiRoom()) {
					for (Enumeration e=placement.getRoomLocations().elements();e.hasMoreElements();) {
						RoomLocation r = (RoomLocation)e.nextElement();
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
					iSlotsUse += (((double)t.getNrWeeks(iStartDay,iEndDay))/iNrWeeks)*use*t.getNrMeetings()*t.getNrSlotsPerMeeting();
					iLecturesUse += use;
				}
			}
			
		}
		
	}

}
