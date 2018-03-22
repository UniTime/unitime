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
import org.unitime.timetable.util.duration.DurationModel;

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
			Debug.info("Unable to retrieve assignment info for "+assignment.getPlacement().getLongName(true));
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
		DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
		iTime = new ClassTimeInfo(
				assignment.getClassId(),
				assignment.getDays().intValue(),
				assignment.getStartSlot().intValue(),
				assignment.getSlotPerMtg(),
				assignment.getMinutesPerMeeting(),
				info.getTimePreference(),
				assignment.getTimePattern(),
				iDate,
				assignment.getBreakTime(),
				dm.getDates(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays(), assignment.getMinutesPerMeeting()));
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
		return (hasTime()?getTime().getNameHtml():"<i>" + MSG.timeNotApplicable() + "</i>");
	}
	
	public String getTimeLongNameHtml() {
		return (hasTime()?getTime().getLongNameHtml():"<i>" + MSG.timeNotApplicable() + "</i>");
	}

	public int getRoomSize() {
		int count = 0;
		for (ClassRoomInfo room: getRooms()) count += room.getCapacity();
		return count;
	}
	
	public String getDateNameHtml() {
		return (hasDate()?getDate().toHtml():"<i>" + MSG.dateNotApplicable() + "</i>");
	}
	
	public String getDateLongNameHtml() {
		return (hasDate()?getDate().toLongHtml():"<i>" + MSG.dateNotApplicable() + "</i>");
	}
}