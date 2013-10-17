/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.unitime.commons.Debug;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;

/**
 * @author Tomas Muller
 */
public class ClassAssignment extends ClassInfo implements Serializable {
	private static final long serialVersionUID = -5426079388298794551L;
	protected Vector<ClassRoomInfo> iRooms = new Vector<ClassRoomInfo>();
	protected ClassTimeInfo iTime = null;
	protected ClassDateInfo iDate = null;
	
	public ClassAssignment(Assignment assignment) {
		super(assignment.getClazz());
		AssignmentPreferenceInfo info = null;
		try {
			info = (AssignmentPreferenceInfo)assignment.getAssignmentInfo("AssignmentInfo");
		} catch (Exception e) {
			Debug.info("Unable to retrieve assignment info for "+assignment.getPlacement().getLongName());
		}
		if (info==null) info = new AssignmentPreferenceInfo();
		for (Iterator i=assignment.getRooms().iterator();i.hasNext();) {
			Location room = (Location)i.next();
			iRooms.add(new ClassRoomInfo(room, info.getRoomPreference(room.getUniqueId())));
		}
		iDate = new ClassDateInfo(
				assignment.getDatePattern().getUniqueId(),
				assignment.getClassId(),
				assignment.getDatePattern().getName(),
				assignment.getDatePattern().getPatternBitSet(),
				(info == null ? 0 : info.getDatePatternPref()));
		iTime = new ClassTimeInfo(
				assignment.getDays().intValue(),
				assignment.getStartSlot().intValue(),
				assignment.getSlotPerMtg(),
				info.getTimePreference(),
				assignment.getTimePattern(),
				iDate,
				assignment.getBreakTime());
	}
	
	public ClassAssignment(Class_ clazz, ClassTimeInfo time, ClassDateInfo date, Collection<ClassRoomInfo> rooms) {
		super(clazz);
		iTime = time;
		iDate = date;
		if (rooms!=null) iRooms.addAll(rooms);
	}
	
	public boolean hasTime() {
		return iTime!=null;
	}
	
	public ClassTimeInfo getTime() {
		return iTime;
	}
	
	public String getTimeId() {
		return (hasTime()?getTime().getId():null);
	}
	
	public boolean hasDate() {
		return iDate != null;
	}
	
	public ClassDateInfo getDate() {
		return iDate;
	}
	
	public String getDateId() {
		return (iDate == null ? null : iDate.getId().toString());
	}
	
	public Collection<ClassRoomInfo> getRooms() {
		return iRooms;
	}
	
	public boolean hasRoom(Long roomId) {
		if (iRooms==null) return false;
		for (ClassRoomInfo room: iRooms)
			if (room.getLocationId().equals(roomId)) return true;
		return false;
	}
	
	public Collection<Long> getRoomIds() {
		Vector<Long> roomIds = new Vector<Long>(getNrRooms());
		for (ClassRoomInfo room: getRooms()) roomIds.add(room.getLocationId());
		return roomIds;
	}

	public int getNrRooms() {
		return iRooms.size();
	}
	
	public ClassRoomInfo getRoom(int idx) {
		return iRooms.elementAt(idx);
	}
	
	public String toString() {
		StringBuffer s = new StringBuffer(super.getClassName()+" ");
		if (hasDate()) s.append(getDate().getName() + " ");
		if (hasTime()) s.append(getTime().getName());
		if (getNrRooms()>0) s.append((hasTime()?" ":"")+getRoomNames(", "));
		return s.toString();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public int compareTo(ClassInfo a) {
		int cmp = super.compareTo(a);
		if (cmp!=0) return cmp;
		if (a instanceof ClassAssignment) {
			ClassAssignment ci = (ClassAssignment)a;
			if (hasDate() && !ci.hasDate()) return 1;
			if (!hasDate() && ci.hasDate()) return -1;
			if (hasDate()) {
				cmp = getDate().compareTo(ci.getDate());
				if (cmp != 0) return cmp;
			}
			if (hasTime() && !ci.hasTime()) return 1;
			if (!hasTime() && ci.hasTime()) return -1;
			if (hasTime()) {
				cmp = getTime().compareTo(ci.getTime());
				if (cmp!=0) return cmp;
			}
			cmp = getRooms().size() - ci.getRooms().size();
			if (cmp!=0) return cmp;
			for (int i=0; i<getNrRooms(); i++) {
				cmp = getRoom(i).compareTo(ci.getRoom(i));
				if (cmp!=0) return cmp;
			}
			return hashCode() - ci.hashCode();
		} else return 1;
	}
	
	public int getValue() {
		int value = 0;
		if (hasTime()) value += getTime().getPreference();
		for (ClassRoomInfo room: getRooms()) value += room.getPreference();
		return value;
	}
	
	public String getRoomNames(String delim) {
		if (getNrRooms()==0) return "";
		StringBuffer s = new StringBuffer();
		for (Iterator<ClassRoomInfo> i=getRooms().iterator(); i.hasNext();) {
			s.append(i.next().getName());
			if (i.hasNext()) s.append(delim);
		}
		return s.toString();
	}
	
	public String getRoomNamesHtml(String delim) {
		if (getNrRooms()==0) return "";
		StringBuffer s = new StringBuffer();
		for (Iterator<ClassRoomInfo> i=getRooms().iterator(); i.hasNext();) {
			s.append(i.next().getNameHtml());
			if (i.hasNext()) s.append(delim);
		}
		return s.toString();
	}
	
	public boolean isValid() {
		return hasTime() && getNrRooms()==getNumberOfRooms();
	}
	
	public String getTimeNameHtml() {
		return (hasTime()?getTime().getNameHtml():"<i>N/A</i>");
	}
	
	public String getTimeLongNameHtml() {
		return (hasTime()?getTime().getLongNameHtml():"<i>N/A</i>");
	}

	public int getRoomSize() {
		int count = 0;
		for (ClassRoomInfo room: getRooms()) count += room.getCapacity();
		return count;
	}
	
	public String getDateNameHtml() {
		return (hasDate()?getDate().toHtml():"<i>N/A</i>");
	}
	
	public String getDateLongNameHtml() {
		return (hasDate()?getDate().toLongHtml():"<i>N/A</i>");
	}
}