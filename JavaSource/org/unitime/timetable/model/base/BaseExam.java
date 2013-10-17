/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public abstract class BaseExam extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private String iName;
	private String iNote;
	private Integer iLength;
	private Integer iExamSize;
	private Integer iPrintOffset;
	private Integer iMaxNbrRooms;
	private Integer iSeatingType;
	private String iAssignedPreference;
	private Integer iAvgPeriod;
	private Long iUniqueIdRolledForwardFrom;

	private Session iSession;
	private ExamPeriod iAssignedPeriod;
	private ExamType iExamType;
	private Set<ExamOwner> iOwners;
	private Set<Location> iAssignedRooms;
	private Set<DepartmentalInstructor> iInstructors;
	private Set<ExamConflict> iConflicts;

	public static String PROP_NAME = "name";
	public static String PROP_NOTE = "note";
	public static String PROP_LENGTH = "length";
	public static String PROP_EXAM_SIZE = "examSize";
	public static String PROP_PRINT_OFFSET = "printOffset";
	public static String PROP_MAX_NBR_ROOMS = "maxNbrRooms";
	public static String PROP_SEATING_TYPE = "seatingType";
	public static String PROP_ASSIGNED_PREF = "assignedPreference";
	public static String PROP_AVG_PERIOD = "avgPeriod";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseExam() {
		initialize();
	}

	public BaseExam(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	public Integer getLength() { return iLength; }
	public void setLength(Integer length) { iLength = length; }

	public Integer getExamSize() { return iExamSize; }
	public void setExamSize(Integer examSize) { iExamSize = examSize; }

	public Integer getPrintOffset() { return iPrintOffset; }
	public void setPrintOffset(Integer printOffset) { iPrintOffset = printOffset; }

	public Integer getMaxNbrRooms() { return iMaxNbrRooms; }
	public void setMaxNbrRooms(Integer maxNbrRooms) { iMaxNbrRooms = maxNbrRooms; }

	public Integer getSeatingType() { return iSeatingType; }
	public void setSeatingType(Integer seatingType) { iSeatingType = seatingType; }

	public String getAssignedPreference() { return iAssignedPreference; }
	public void setAssignedPreference(String assignedPreference) { iAssignedPreference = assignedPreference; }

	public Integer getAvgPeriod() { return iAvgPeriod; }
	public void setAvgPeriod(Integer avgPeriod) { iAvgPeriod = avgPeriod; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public ExamPeriod getAssignedPeriod() { return iAssignedPeriod; }
	public void setAssignedPeriod(ExamPeriod assignedPeriod) { iAssignedPeriod = assignedPeriod; }

	public ExamType getExamType() { return iExamType; }
	public void setExamType(ExamType examType) { iExamType = examType; }

	public Set<ExamOwner> getOwners() { return iOwners; }
	public void setOwners(Set<ExamOwner> owners) { iOwners = owners; }
	public void addToowners(ExamOwner examOwner) {
		if (iOwners == null) iOwners = new HashSet<ExamOwner>();
		iOwners.add(examOwner);
	}

	public Set<Location> getAssignedRooms() { return iAssignedRooms; }
	public void setAssignedRooms(Set<Location> assignedRooms) { iAssignedRooms = assignedRooms; }
	public void addToassignedRooms(Location location) {
		if (iAssignedRooms == null) iAssignedRooms = new HashSet<Location>();
		iAssignedRooms.add(location);
	}

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public Set<ExamConflict> getConflicts() { return iConflicts; }
	public void setConflicts(Set<ExamConflict> conflicts) { iConflicts = conflicts; }
	public void addToconflicts(ExamConflict examConflict) {
		if (iConflicts == null) iConflicts = new HashSet<ExamConflict>();
		iConflicts.add(examConflict);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Exam)) return false;
		if (getUniqueId() == null || ((Exam)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Exam)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Exam["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "Exam[" +
			"\n	AssignedPeriod: " + getAssignedPeriod() +
			"\n	AssignedPreference: " + getAssignedPreference() +
			"\n	AvgPeriod: " + getAvgPeriod() +
			"\n	ExamSize: " + getExamSize() +
			"\n	ExamType: " + getExamType() +
			"\n	Length: " + getLength() +
			"\n	MaxNbrRooms: " + getMaxNbrRooms() +
			"\n	Name: " + getName() +
			"\n	Note: " + getNote() +
			"\n	PrintOffset: " + getPrintOffset() +
			"\n	SeatingType: " + getSeatingType() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
