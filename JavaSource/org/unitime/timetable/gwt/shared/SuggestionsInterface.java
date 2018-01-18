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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class SuggestionsInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	public static int[] sDayCode2Order = new int[] {
			  0, 127, 125, 126, 121, 124, 122, 123,
			113, 120, 118, 119, 114, 117, 115, 116,
			 97, 112, 110, 111, 106, 109, 107, 108,
			 98, 105, 103, 104,  99, 102, 100, 101,
			 65,  96,  94,  95,  90,  93,  91,  92,
			 82,  89,  87,  88,  83,  86,  84,  85,
			 66,  81,  79,  80,  75,  78,  76,  77,
			 67,  74,  72,  73,  68,  71,  69,  70,
			  1,  64,  62,  63,  58,  61,  59,  60,
			 50,  57,  55,  56,  51,  54,  52,  53,
			 34,  49,  47,  48,  43,  46,  44,  45,
			 35,  42,  40,  41,  36,  39,  37,  38,
			  2,  33,  31,  32,  27,  30,  28,  29,
			 19,  26,  24,  25,  20,  23,  21,  22,
			  3,  18,  16,  17,  12,  15,  13,  14,
			  4,  11,   9,  10,   5,   8,   6,   7};
	
	public static class ClassInfo implements IsSerializable, Serializable, Comparable<ClassInfo> {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iClassId;
		private String iPref;
		private int iRoomCap;
		private int iNrRooms;
		private int iOrd = -1;
		private String iNote;
		
		public ClassInfo() {}
		
		public ClassInfo(String name, Long classId, int nrRooms, String pref, int roomCapacity, int ord, String note) {
			iName = name;
			iClassId = classId;
			iPref = pref;
			iNrRooms = nrRooms;
			iRoomCap = roomCapacity;
			iOrd = ord;
			iNote = note;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public void setRoomCapacity(int roomCap) { iRoomCap = roomCap; }
		public int getRoomCapacity() { return iRoomCap; }
		
		public void setPref(String pref) { iPref = pref; }
		public String getPref() { return iPref; }
		
		public void setNote(String note) { iNote = note; }
		public String getNote() { return iNote; }

		public void setNrRooms(int nrRooms) { iNrRooms = nrRooms; }
		public int nrRooms() { return iNrRooms; }
		
		@Override
		public boolean equals(Object o) {
			if (o==null || !(o instanceof ClassInfo)) return false;
			return getClassId().equals(((ClassInfo)o).getClassId());
		}
		
		@Override
		public int compareTo(ClassInfo ci) {
			if (iOrd >= 0 && ci.iOrd >= 0) {
				int cmp = Double.compare(iOrd, ci.iOrd);
				if (cmp!=0) return cmp;
			}
			int cmp = TableInterface.NaturalOrderComparator.compare(getName(), ci.getName());
			if (cmp != 0) return cmp;
			return getClassId().compareTo(ci.getClassId());
		}
		
		@Override
		public String toString() {
			return getName() == null ? getClassId().toString() : getName();
		}
	}
	
	public static class RoomInfo implements IsSerializable, Serializable, Comparable<RoomInfo> {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iRoomId;
		private int iPref;
		private int iSize;
		private boolean iStrike;
		
		public RoomInfo() {}
		public RoomInfo(String name, Long roomId, int size, int pref) {
			iName = name;
			iRoomId = roomId;
			iPref = pref;
			iSize = size;
			iStrike = (iPref > 500);
		}
		public RoomInfo(Long roomId) {
			iRoomId = roomId;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setId(Long id) { iRoomId = id; }
		public Long getId() { return iRoomId; }
		
		public void setPref(int pref) { iPref = pref; }
		public int getPref() { return iPref; }
		
		public void setStriked(boolean striked) { iStrike = striked; }
		public boolean isStriked() { return iStrike; }
		
		public void setSize(int size) { iSize = size; }
		public int getSize() { return iSize; }
		
		@Override
		public boolean equals(Object o) {
			if (o==null || !(o instanceof RoomInfo)) return false;
			return getId().equals(((RoomInfo)o).getId());
		}

		@Override
		public int compareTo(RoomInfo r) {
			if (isStriked() && !r.isStriked()) return 1;
			if (!isStriked() && r.isStriked()) return -1;
			return TableInterface.NaturalOrderComparator.compare(getName(), r.getName());
		}
		
		@Override
		public String toString() {
			return (getName() == null ? getId().toString() : getName());
		}
	}
	
	public static enum DayCode {
		MON(64),
		TUE(32),
		WED(16),
		THU(8),
		FRI(4),
		SAT(2),
		SUN(1),
		;
		DayCode(int code) { iCode = code; }
		private int iCode;
		public int getCode() { return iCode; }
	}
	
	public static class DateInfo implements IsSerializable, Serializable, Comparable<DateInfo> {
		private static final long serialVersionUID = 1L;
		private String iDatePatternName;
		private int iDatePatternPref;
		private Long iDatePatternId;
		
		public DateInfo() {}
		public DateInfo(Long id, String name, int pref) {
			iDatePatternId = id;
			iDatePatternName = name;
			iDatePatternPref = pref;
		}
		public DateInfo(SelectedAssignment assignment) {
			iDatePatternId = assignment.getDatePatternId();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof DateInfo)) return false;
			DateInfo d = (DateInfo)o;
			if (getDatePatternId() != null && d.getDatePatternId() != null)
				return getDatePatternId().equals(d.getDatePatternId());
			return getDatePatternName().equals(d.getDatePatternName());
		}
		
		@Override
		public int hashCode() {
			return (getDatePatternId() == null ? getDatePatternName().hashCode() : getDatePatternId().hashCode());
		}
		
		public void setDatePatternName(String dpName) { iDatePatternName = dpName; }
		public String getDatePatternName() { return iDatePatternName; }

		public void setDatePatternId(Long dpId) { iDatePatternId = dpId; }
		public Long getDatePatternId() { return iDatePatternId; }
		
		public void setDatePatternPreference(int dpPref) { iDatePatternPref = dpPref; }
		public int getDatePatternPreference() { return iDatePatternPref; }
		
		@Override
		public int compareTo(DateInfo d) {
			int cmp = TableInterface.NaturalOrderComparator.compare(getDatePatternName(), d.getDatePatternName());
			if (cmp != 0) return cmp;
			return getDatePatternId().compareTo(d.getDatePatternId());
		}
	}
	
	public static class TimeInfo implements IsSerializable, Serializable, Comparable<TimeInfo> {
		private static final long serialVersionUID = 1L;
		private int iDays;
		private int iStartSlot;
		private int iMin;
		private int iPref;
		private boolean iStrike = false;
		private Long iPatternId = null;
		private DateInfo iDatePattern = null;
		
		public TimeInfo() {}
		public TimeInfo(int days, int startSlot, int pref, int min, String datePatternName, Long patternId, Long datePatternId, int datePatternPref) {
			iDays = days;
			iStartSlot = startSlot;
			iPref = pref;
			iStrike = (iPref > 500);
			iMin = min;
			iDatePattern = new DateInfo(datePatternId, datePatternName, datePatternPref);
			iPatternId = patternId;
		}
		public TimeInfo(SelectedAssignment assignment) {
			iDays = assignment.getDays();
			iStartSlot = assignment.getStartSlot();
			iPatternId = assignment.getPatternId();
			iDatePattern = new DateInfo(assignment);
		}
		
		public void setDays(int days) { iDays = days; }
		public int getDays() { return iDays; }
		public int getDaysOrder(Integer firstWorkDay) {
			int days = iDays;
			if (firstWorkDay != null && firstWorkDay.intValue() != 0) {
				days = iDays << firstWorkDay;
				days = (days & 127) + (days >> 7);
			}
			return sDayCode2Order[days];
		}
		
		public void setStartSlot(int startSlot) { iStartSlot = startSlot; }
		public int getStartSlot() { return iStartSlot; }
		
		public void setPref(int pref) { iPref = pref; }
		public int getPref() { return iPref; }
		
		public void setMin(int min) { iMin = min; }
		public int getMin() { return iMin; }
		
		public void setDatePatternName(String dpName) {
			if (iDatePattern == null) iDatePattern = new DateInfo();
			iDatePattern.setDatePatternName(dpName);
		}
		public String getDatePatternName() { return (iDatePattern == null ? null : iDatePattern.getDatePatternName()); }

		public void setStriked(boolean striked) { iStrike = striked; }
		public boolean isStriked() { return iStrike; }
		
		public void setPatternId(Long patternId) { iPatternId = patternId; }
		public Long getPatternId() { return iPatternId; }
		
		public void setDatePatternId(Long dpId) {
			if (iDatePattern == null) iDatePattern = new DateInfo();
			iDatePattern.setDatePatternId(dpId);
		}
		public Long getDatePatternId() { return (iDatePattern == null ? null : iDatePattern.getDatePatternId()); }
		
		public void setDatePatternPreference(int dpPref) {
			if (iDatePattern == null) iDatePattern = new DateInfo();
			iDatePattern.setDatePatternPreference(dpPref);
		}
		public int getDatePatternPreference() { return (iDatePattern == null ? null : iDatePattern.getDatePatternPreference()); }
		
		public DateInfo getDatePattern() { return iDatePattern; }
		public boolean hasDatePattern() { return iDatePattern != null; }
		
		public String getDaysName(Integer firstDay, GwtConstants CONSTANTS) {
			return getDaysName(firstDay, CONSTANTS.shortDays());
		}
		
		public String getDaysName(Integer firstDay, String[] shortDays) {
			if (shortDays == null) shortDays = new String[] {"M", "T", "W", "Th", "F", "S", "Su"};
			String ret = "";
			for (int i = 0; i < DayCode.values().length; i++) {
				int idx = (firstDay == null ? i : (i + firstDay) % 7);
				DayCode dc = DayCode.values()[idx];
				if ((dc.getCode() & iDays)!=0) ret += shortDays[dc.ordinal()];
			}
			return ret;
		}
		
		public String getDaysName() {
			return getDaysName(null, new String[] {"M", "T", "W", "Th", "F", "S", "Su"});
		}
		
		public static String slot2time(int timeSinceMidnight, boolean useAmPm) {
			int hour = timeSinceMidnight / 60;
		    int min = timeSinceMidnight % 60;
		    if (useAmPm)
		    	return (hour==0?12:hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour<24 && hour>=12?"p":"a");
		    else
		    	return hour + ":" + (min < 10 ? "0" : "") + min;
		}
		
		public static String slot2time(int timeSinceMidnight) {
			int hour = timeSinceMidnight / 60;
		    int min = timeSinceMidnight % 60;
		    return hour + (min < 10 ? "0" : "") + min;
		}
			
		public String getStartTime(GwtConstants CONSTANTS) {
			return slot2time(5 * iStartSlot, CONSTANTS.useAmPm());
		}
		
		public String getEndTime(GwtConstants CONSTANTS) {
			return slot2time(5 * iStartSlot + iMin, CONSTANTS.useAmPm());
		}
		
		@Override
		public boolean equals(Object o) {
			if (o==null || !(o instanceof TimeInfo)) return false;
			TimeInfo t = (TimeInfo)o;
			return t.getDays()==getDays() && t.getStartSlot()==getStartSlot() && t.getPatternId().equals(getPatternId()) && t.getDatePatternId().equals(getDatePatternId());
		}
		
		public String getName(Integer firstDay, boolean endTime, GwtConstants CONSTANTS) {
			return getDaysName(firstDay, CONSTANTS) + " " + getStartTime(CONSTANTS) + (endTime ? " - " + getEndTime(CONSTANTS) : "");
		}
		
		public int compareTo(TimeInfo t, Integer firstDay) {
			if (isStriked() && !t.isStriked()) return 1;
			if (!isStriked() && t.isStriked()) return -1;
			int cmp = TableInterface.NaturalOrderComparator.compare(getDatePatternName(), t.getDatePatternName());
			if (cmp!=0) return cmp;
			cmp = Double.compare(getDaysOrder(firstDay),t.getDaysOrder(firstDay));
			if (cmp!=0) return cmp;
			cmp = Double.compare(getStartSlot(),t.getStartSlot());
			if (cmp!=0) return cmp;
			cmp = Double.compare(getMin(),t.getMin());
			return cmp;
		}
		
		@Override
		public int compareTo(TimeInfo t) {
			return compareTo(t, null);
		}
		
		@Override
		public String toString() {
			return (hasDatePattern() ? getDatePatternName() + " " : "") + getDaysName(null, new String[] {"A", "B", "C", "D", "E", "F", "G"}) + " " + slot2time(5 * iStartSlot) + " - " + slot2time(5 * iStartSlot + iMin);
		}
	}
	
	public static class InstructorInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iInstructorId;
		
		public InstructorInfo() {}
		public InstructorInfo(String name, Long instructorId) {
			iName = name;
			iInstructorId = instructorId;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public void setId(Long id) { iInstructorId = id; }
		public Long getId() { return iInstructorId; }

		@Override
		public boolean equals(Object o) {
			if (o==null || !(o instanceof InstructorInfo)) return false;
			return getId().equals(((InstructorInfo)o).getId());
		}
	}
	
	public static class CurriculumInfo implements IsSerializable, Serializable, Comparable<CurriculumInfo> {
		private static final long serialVersionUID = 1L;
		private String iName;
		private int iNrStudents;
		
		public CurriculumInfo() {}
		public CurriculumInfo(String name, int nrStudents) {
			iName = name;
			iNrStudents = nrStudents;
		}
		
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		public void setNrStudents(int nrStudents) { iNrStudents = nrStudents; }
		public int getNrStudents() { return iNrStudents; }
		
		public int compareTo(CurriculumInfo i) {
			if (getNrStudents() != i.getNrStudents())
				return (i.getNrStudents() > getNrStudents() ? -1 : 1);
			return getName().compareTo(i.getName());
		}
	}
	
	public static class JenrlInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		public int iJenrl = 0;
		public boolean iIsSatisfied = false;
		public boolean iIsHard = false;
		public boolean iIsDistance = false;
		public boolean iIsFixed = false;
		public boolean iIsCommited = false;
		public boolean iIsImportant = false;
		public boolean iIsInstructor = false;
		public boolean iIsWorkDay = false;
		public double iDistance = 0.0;
		private TreeSet<CurriculumInfo> iCurriculum2nrStudents = null;
	
		public int getJenrl() { return iJenrl; }
		public void setJenrl(int jenrl) { iJenrl = jenrl; }
		public boolean isSatisfied() { return iIsSatisfied; }
		public void setIsSatisfied(boolean isSatisfied) { iIsSatisfied = isSatisfied; }
		public boolean isHard() { return iIsHard; }
		public void setIsHard(boolean isHard) { iIsHard = isHard; }
		public boolean isDistance() { return iIsDistance; }
		public void setIsDistance(boolean isDistance) { iIsDistance = isDistance; }
		public boolean isFixed() { return iIsFixed; }
		public void setIsFixed(boolean isFixed) { iIsFixed = isFixed; }
		public boolean isCommited() { return iIsCommited; }
		public void setIsCommited(boolean isCommited) { iIsCommited = isCommited; }
		public boolean isImportant() { return iIsImportant; }
		public void setIsImportant(boolean isImportant) { iIsImportant = isImportant; }
		public boolean isWorkDay() { return iIsWorkDay; }
		public void setIsWorkDay(boolean isWorkDay) { iIsWorkDay = isWorkDay; }
		public boolean isInstructor() { return iIsInstructor; }
		public void setIsInstructor(boolean isInstructor) { iIsInstructor = isInstructor; }
		public double getDistance() { return iDistance; }
		public void setDistance(double distance) { iDistance = distance; }
		
		public boolean hasCurricula() { return iCurriculum2nrStudents != null; }
		public void addCurriculum(CurriculumInfo info) {
			if (iCurriculum2nrStudents == null) iCurriculum2nrStudents = new TreeSet<CurriculumInfo>();
			iCurriculum2nrStudents.add(info);
		}
		public Set<CurriculumInfo> getCurricula() { return iCurriculum2nrStudents; }
	}
	
	public static class StudentConflictInfo implements IsSerializable, Serializable, Comparable<StudentConflictInfo> {
		private static final long serialVersionUID = 1L;
		private JenrlInfo iInfo;
		private ClassAssignmentDetails iOther = null;
		private ClassAssignmentDetails iAnother = null;
		
		public StudentConflictInfo() {}
		public StudentConflictInfo(JenrlInfo jenrl, ClassAssignmentDetails other) {
			iInfo = jenrl;
			iOther = other;
		}
		
		public void setOther(ClassAssignmentDetails other) { iOther = other; }
		public ClassAssignmentDetails getOther() { return iOther; }
		public void setAnother(ClassAssignmentDetails another) { iAnother = another; }
		public ClassAssignmentDetails getAnother() { return iAnother; }
		public void setInfo(JenrlInfo info) { iInfo = info; }
		public JenrlInfo getInfo() { return iInfo; }

		@Override
		public int compareTo(StudentConflictInfo o) {
			if (getInfo().getJenrl() != o.getInfo().getJenrl())
				return getInfo().getJenrl() > o.getInfo().getJenrl() ? -1 : 1;
			int cmp = getOther().getClazz().getName().compareTo(o.getOther().getClazz().getName());
			if (cmp != 0) return cmp;
			return getOther().getClazz().compareTo(o.getOther().getClazz());
		}
	}
	
	public static class GroupConstraintInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		public String iPreference = "0";
		public boolean iIsSatisfied = false;
		public String iName = null;
	    public String iType = null;
	    public Double iValue = null;
		
		public GroupConstraintInfo() { }
		
		public String getPreference() { return iPreference; }
		public void setPreference(String preference) { iPreference = preference; }
		public boolean isSatisfied() { return iIsSatisfied; }
		public void setIsSatisfied(boolean isSatisfied) { iIsSatisfied = isSatisfied; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
	    public String getType() { return iType; }
	    public void setType(String type) { iType = type; }
	    public boolean hasValue() { return iValue != null; }
	    public Double getValue() { return iValue; }
	    public void setValue(Double value) { iValue = value; }
	}
	
	public static class DistributionInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private GroupConstraintInfo iInfo;
		private List<ClassAssignmentDetails> iOther = new ArrayList<ClassAssignmentDetails>();
		
		public DistributionInfo() {}
		public DistributionInfo(GroupConstraintInfo info) {
			iInfo = info;
		}
		
		public void setInfo(GroupConstraintInfo info) { iInfo = info; }
		public GroupConstraintInfo getInfo() { return iInfo; }
		
		public void addClass(ClassAssignmentDetails other) { iOther.add(other); }
		public List<ClassAssignmentDetails> getOtherClasses() { return iOther; }
	}
	
	public static class BtbInstructorInfo implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private ClassAssignmentDetails iOther = null;
		private ClassAssignmentDetails iAnother = null;
		private int iPref;
		
		public BtbInstructorInfo() {}
		public BtbInstructorInfo(ClassAssignmentDetails other, int pref) {
			iOther = other;
			iPref = pref;
		}
		
		public void setOther(ClassAssignmentDetails other) { iOther = other; }
		public ClassAssignmentDetails getOther() { return iOther; }
		public void setAnother(ClassAssignmentDetails another) { iAnother = another; }
		public ClassAssignmentDetails getAnother() { return iAnother; }
		public void setPreference(int preference) { iPref = preference; }
		public int getPreference() { return iPref; }
	}
	
	public static class ClassAssignmentDetailsRequest implements GwtRpcRequest<ClassAssignmentDetails>, Serializable{
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		private List<SelectedAssignment> iAssignments;
		
		public ClassAssignmentDetailsRequest() {}
		public ClassAssignmentDetailsRequest(Long classId) {
			setClassId(classId);
		}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public boolean hasAssignments() { return iAssignments != null && !iAssignments.isEmpty(); }
		public void addAssignment(SelectedAssignment a) {
			if (iAssignments == null) iAssignments = new ArrayList<SelectedAssignment>();
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.equals(a)) i.remove();
			}
			iAssignments.add(a);
		}
		public SelectedAssignment removeAssignment(Long classId) {
			if (iAssignments == null) return null;
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.getClassId().equals(classId)) {
					i.remove();
					return other;
				}
			}
			return null;
		}
	}
	
	public static class ClassAssignmentDetails implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private ClassInfo iClass = null;
		private TimeInfo iTime = null;
		private List<RoomInfo> iRoom = null;
		private List<InstructorInfo> iInstructor = null;
		private TimeInfo iInitialTime = null;
		private List<RoomInfo> iInitialRoom = null;
		private TimeInfo iAssignedTime = null;
		private List<RoomInfo> iAssignedRoom = null;
		private List<RoomInfo> iRooms = null;
		private List<TimeInfo> iTimes = null;
		private List<StudentConflictInfo> iStudentConflicts = null;
		private List<DistributionInfo> iDistributionConflicts = null;
		private List<BtbInstructorInfo> iBtbInstructorConflicts = null;
		private String iConflict = null;
		private Map<String, Double> iObjectives = null;
		private Map<String, Double> iAssignedObjectives = null;
		private boolean iCanUnassign = false;
		
		public ClassAssignmentDetails() {}
		
		public void setClazz(ClassInfo clazz) { iClass = clazz; }
		public ClassInfo getClazz() { return iClass; }
		public void setTime(TimeInfo time) { iTime = time; }
		public TimeInfo getTime() { return iTime; }
		public int getNrRooms() { return iRoom == null ? 0 : iRoom.size(); }
		public void setRoom(RoomInfo room) {
			if (iRoom == null) iRoom = new ArrayList<RoomInfo>();
			iRoom.add(room);
		}
		public List<RoomInfo> getRoom() { return iRoom; }
		public int getNrInstructors() { return iInstructor == null ? 0 : iInstructor.size(); }
		public void setInstructor(InstructorInfo instructor) {
			if (iInstructor == null) iInstructor = new ArrayList<InstructorInfo>();
			iInstructor.add(instructor);
		}
		public List<InstructorInfo> getInstructor() { return iInstructor; }
		public void setInitialTime(TimeInfo time) { iInitialTime = time; }
		public TimeInfo getInitialTime() { return iInitialTime; }
		public int getNrInitialRooms() { return iInitialRoom == null ? 0 : iInitialRoom.size(); }
		public void setInitialRoom(RoomInfo room) {
			if (iInitialRoom == null) iInitialRoom = new ArrayList<RoomInfo>();
			iInitialRoom.add(room);
		}
		public List<RoomInfo> getInitialRoom() { return iInitialRoom; }
		public void setAssignedTime(TimeInfo time) { iAssignedTime = time; }
		public TimeInfo getAssignedTime() { return iAssignedTime; }
		public int getNrAssignedRooms() { return iAssignedRoom == null ? 0 : iAssignedRoom.size(); }
		public void setAssignedRoom(RoomInfo room) {
			if (iAssignedRoom == null) iAssignedRoom = new ArrayList<RoomInfo>();
			iAssignedRoom.add(room);
		}
		public List<RoomInfo> getAssignedRoom() { return iAssignedRoom; }
		
		public boolean hasRooms() { return iRooms != null && !iRooms.isEmpty(); }
		public void addRoom(RoomInfo room) {
			if (iRooms == null) iRooms = new ArrayList<RoomInfo>();
			iRooms.add(room);
		}
		public List<RoomInfo> getRooms() { return iRooms; }
		
		public boolean hasTimes() { return iTimes != null && !iTimes.isEmpty(); }
		public void addTime(TimeInfo time) {
			if (iTimes == null) iTimes = new ArrayList<TimeInfo>();
			iTimes.add(time);
		}
		public List<TimeInfo> getTimes() { return iTimes; }
		
		public boolean isCanUnassign() { return iCanUnassign; }
		public void setCanUnassign(boolean canUnassign) { iCanUnassign = canUnassign; }
		
		public SelectedAssignment getSelection() {
			if (getTime() == null) return null;
			SelectedAssignment selection = new SelectedAssignment();
			selection.setClassId(getClazz().getClassId());
			selection.setDatePatternId(getTime().getDatePatternId());
			selection.setDays(getTime().getDays());
			selection.setPatternId(getTime().getPatternId());
			selection.setStartSlot(getTime().getStartSlot());
			if (getRoom() != null)
				for (RoomInfo room: getRoom())
					selection.addRoomId(room.getId());
			return selection;
		}
		
		public SelectedAssignment getAssignedSelection() {
			if (getAssignedTime() == null) return null;
			SelectedAssignment selection = new SelectedAssignment();
			selection.setClassId(getClazz().getClassId());
			selection.setDatePatternId(getAssignedTime().getDatePatternId());
			selection.setDays(getAssignedTime().getDays());
			selection.setPatternId(getAssignedTime().getPatternId());
			selection.setStartSlot(getAssignedTime().getStartSlot());
			if (getAssignedRoom() != null)
				for (RoomInfo room: getAssignedRoom())
					selection.addRoomId(room.getId());
			return selection;
		}

		public boolean hasStudentConflicts() { return iStudentConflicts != null && !iStudentConflicts.isEmpty(); }
		public void addStudentConflict(StudentConflictInfo conf) {
			if (iStudentConflicts == null) iStudentConflicts = new ArrayList<StudentConflictInfo>();
			iStudentConflicts.add(conf);
		}
		public List<StudentConflictInfo> getStudentConflicts() { return iStudentConflicts; }
		public int countStudentConflicts() {
			if (iStudentConflicts == null) return 0;
			int ret = 0;
			for (StudentConflictInfo c: iStudentConflicts)
				ret += c.getInfo().getJenrl();
			return ret;
		}
		
		public boolean hasDistributionConflicts() { return iDistributionConflicts != null && !iDistributionConflicts.isEmpty(); }
		public void addDistributionConflict(DistributionInfo conf) {
			if (iDistributionConflicts == null) iDistributionConflicts = new ArrayList<DistributionInfo>();
			iDistributionConflicts.add(conf);
		}
		public List<DistributionInfo> getDistributionConflicts() { return iDistributionConflicts; }
		public boolean hasViolatedDistributionConflicts() {
			if (iDistributionConflicts == null) return false;
			for (DistributionInfo di: iDistributionConflicts)
				if (!di.getInfo().isSatisfied()) return true;
			return false;
		}
		public double countDistributionConflicts() {
			if (iDistributionConflicts == null) return 0;
			double ret = 0;
			for (DistributionInfo c: iDistributionConflicts)
				if (c.getInfo().hasValue())
					ret += c.getInfo().getValue();
			return ret;
		}

		
		public boolean hasBtbInstructorConflicts() { return iBtbInstructorConflicts != null && !iBtbInstructorConflicts.isEmpty(); }
		public void addBtbInstructorConflict(BtbInstructorInfo conf) {
			if (iBtbInstructorConflicts == null) iBtbInstructorConflicts = new ArrayList<BtbInstructorInfo>();
			iBtbInstructorConflicts.add(conf);
		}
		public List<BtbInstructorInfo> getBtbInstructorConflicts() { return iBtbInstructorConflicts; }
		
		public boolean isInitial() {
			return getAssignedTime()!=null && getAssignedRoom()!=null && getAssignedTime().equals(getInitialTime()) && getAssignedRoom().equals(getInitialRoom());
		}
		public int getNrDates() {
			Set<Long> dates = new HashSet<Long>();
			if (getTimes() != null)
				for (TimeInfo time: getTimes())
					dates.add(time.getDatePatternId());
			return dates.size();
		}
		
		public String getConflict() { return iConflict; }
		public void setConflict(String conflict) { iConflict = conflict; }
		public boolean hasConflict() { return iConflict != null && !iConflict.isEmpty(); }
		
		@Override
		public String toString() {
			String ret = (getClazz() == null ? "" : getClazz().toString());
			if (getAssignedTime() != null) {
				ret += ": " + getAssignedTime();
			} else if (getTime() != null) {
				ret += ": " + getTime();
			} else {
				ret += ": Not Assigned";
			}
			if (getAssignedRoom() != null) {
				for (int i = 0; i < getAssignedRoom().size(); i++)
					ret += (i == 0 ? "" : ", ") + getAssignedRoom().get(i);
			} else if (getRoom() != null) {
				for (int i = 0; i < getRoom().size(); i++)
					ret += (i == 0 ? "" : ", ") + getRoom().get(i);
			}
			return ret; 
		}
		
		public boolean hasObjectives() { return iObjectives != null; }
		public void setObjective(String name, Double value) {
			if (iObjectives == null) iObjectives = new HashMap<String, Double>();
			iObjectives.put(name, value);
		}
		public Map<String, Double> getObjectives() { return iObjectives; }
		public double getObjective(String name) {
			if (iObjectives == null) return 0.0;
			Double value = iObjectives.get(name);
			return (value == null ? 0.0 : value.doubleValue());
		}
		
		public boolean hasAssignedObjectives() { return iAssignedObjectives != null; }
		public void setAssignedObjective(String name, Double value) {
			if (iAssignedObjectives == null) iAssignedObjectives = new HashMap<String, Double>();
			iAssignedObjectives.put(name, value);
		}
		public Map<String, Double> getAssignedObjectives() { return iAssignedObjectives; }
		public double getAssignedObjective(String name) {
			if (iAssignedObjectives == null) return 0.0;
			Double value = iAssignedObjectives.get(name);
			return (value == null ? 0.0 : value.doubleValue());
		}
	}
	
	public static class PreferenceInterface extends RoomInterface.PreferenceInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private int iPreference;
		
		public PreferenceInterface() {}
		public PreferenceInterface(Long id, String color, String code, String name, String abbv, int preference) {
			super(id, color, code, name, abbv, true);
			iPreference = preference;
		}
		
		public int getPreference() { return iPreference; }
		public void setPreference(int preference) { iPreference = preference; }
	}
	
	public static class SuggestionProperties implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private List<PreferenceInterface> iPreferences = new ArrayList<PreferenceInterface>();
		private boolean iSolver = false;
		private List<SelectedAssignment> iSelectedAssignments = null;
		private Integer iFirstDay;
		
		public void addPreference(PreferenceInterface preference) { iPreferences.add(preference); }
		public List<PreferenceInterface> getPreferences() { return iPreferences; }
		public PreferenceInterface getPreference(String prefProlog) {
			for (PreferenceInterface p: iPreferences)
				if (p.getCode().equals(prefProlog)) return p;
			return null;
		}
		public PreferenceInterface getPreference(int intPref) {
			if (intPref >= 50)
				return getPreference("P");
			if (intPref >= 4)
				return getPreference("2");
			if (intPref > 0)
				return getPreference("1");
			if (intPref <= -50)
				return getPreference("R");
			if (intPref <= -4)
				return getPreference("-2");
			if (intPref < 0)
				return getPreference("-1");
			return getPreference("0");
		}
		
		public boolean isSolver() { return iSolver; }
		public void setSolver(boolean solver) { iSolver = solver; }
		
		public boolean hasSelectedAssignments() { return iSelectedAssignments != null && !iSelectedAssignments.isEmpty(); }
		public List<SelectedAssignment> getSelectedAssignments() { return iSelectedAssignments; }
		public void addSelectedAssignment(SelectedAssignment assignment) {
			if (iSelectedAssignments == null) iSelectedAssignments = new ArrayList<SelectedAssignment>();
			iSelectedAssignments.add(assignment);
		}
		public void setFirstDay(Integer firstDay) { iFirstDay = firstDay; }
		public Integer getFirstDay() { return iFirstDay; }
	}
	
	public static class SuggestionPropertiesRequest implements GwtRpcRequest<SuggestionProperties>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iHistoryId;
		
		public SuggestionPropertiesRequest() {}
		public SuggestionPropertiesRequest(Long historyId) {
			iHistoryId = historyId;
		}
		
		public boolean hasHistoryId() { return iHistoryId != null; }
		public Long getHistoryId() { return iHistoryId; }
		public void setHistoryId(Long id) { iHistoryId = id; }
	}
	
	public static class SelectedAssignment implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		private int iDays = 0;
		private int iStartSlot = 0;
		private List<Long> iRoomIds;
		private Long iPatternId;
		private Long iDatePatternId;
		
		public SelectedAssignment() {}
		public SelectedAssignment(Long classId) {
			iClassId = classId;
		}
		public SelectedAssignment(Long classId, int days, int startSlot, List<Long> roomIds, Long patternId, Long datePatternId) {
			iClassId = classId; iDays = days; iStartSlot = startSlot; iRoomIds = roomIds; iPatternId = patternId; iDatePatternId = datePatternId;
		}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		public void setDays(int days) { iDays = days; }
		public int getDays() { return iDays; }
		public void setStartSlot(int slot) { iStartSlot = slot; }
		public int getStartSlot() { return iStartSlot; }
		public void setRoomIds(List<Long> roomIds) { iRoomIds = roomIds; }
		public void addRoomId(Long roomId) {
			if (iRoomIds == null) iRoomIds = new ArrayList<Long>();
			iRoomIds.add(roomId);
		}
		public List<Long> getRoomIds() { return iRoomIds; }
		public String getRoomIds(String separator) {
			if (iRoomIds == null) return "";
			String ret = "";
			for (Long id: iRoomIds)
				ret += (ret.isEmpty() ? "" : separator) + id;
			return ret;
		}
		public Long getRoomId(int index) {
			if (iRoomIds == null || index >= iRoomIds.size()) return null;
			return iRoomIds.get(index);
		}
		public void setPatternId(Long patternId) { iPatternId = patternId; }
		public Long getPatternId() { return iPatternId; }
		public void setDatePatternId(Long patternId) { iDatePatternId = patternId; }
		public Long getDatePatternId() { return iDatePatternId; }
		
		@Override
		public boolean equals(Object o) {
			if (o==null || !(o instanceof SelectedAssignment)) return false;
			return iClassId.equals(((SelectedAssignment)o).getClassId());
		}
		
		@Override
		public int hashCode() { return iClassId.hashCode(); }
		
		@Override
		public String toString() {
			return "SelectedAssignment{class=" + getClassId() + ", days=" + getDays() + ", slot=" + getStartSlot() + ", rooms=" + getRoomIds() + "}";
		}
	}

	public static class SelectedAssignmentsRequest implements GwtRpcRequest<Suggestion>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		private List<SelectedAssignment> iAssignments;
		
		public SelectedAssignmentsRequest() {}
		public SelectedAssignmentsRequest(Long classId) {
			setClassId(classId);
		}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public boolean hasAssignments() { return iAssignments != null && !iAssignments.isEmpty(); }
		public void addAssignment(SelectedAssignment a) {
			if (iAssignments == null) iAssignments = new ArrayList<SelectedAssignment>();
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.equals(a)) i.remove();
			}
			iAssignments.add(a);
		}
		public SelectedAssignment removeAssignment(Long classId) {
			if (iAssignments == null) return null;
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.getClassId().equals(classId)) {
					i.remove();
					return other;
				}
			}
			return null;
		}
		public List<SelectedAssignment> getAssignments() { return iAssignments; }
	}
	
	public static class Suggestion implements GwtRpcResponse, Serializable, Comparable<Suggestion> {
		private static final long serialVersionUID = 1L;
	    private double iValue = 0;
	    private int iUnassignedVariables = 0;
	    private List<ClassAssignmentDetails> iUnresolvedConflicts = null;
	    private List<ClassAssignmentDetails> iDifferentAssignments = null;
	    private List<DistributionInfo> iDistributionConflicts = null;
	    private List<BtbInstructorInfo> iBtbInstructorConflicts = null;
	    private List<StudentConflictInfo> iStudentConflicts = null;
	    private boolean iCanAssign = true;
	    private ClassAssignmentDetails iPlacement = null;
	    private ClassAssignmentDetails iSelectedPlacement = null;
	    private double iBaseValue = 0;
	    private int iBaseUnassignedVariables = 0;
	    private TableInterface.TableCellMulti iStudentConflictSummary = null;
	    private Map<String, Double> iCriteria = new HashMap<String, Double>();
	    private Map<String, Double> iBaseCriteria = new HashMap<String, Double>();
		
		public Suggestion() {}
		
		public boolean hasUnresolvedConflicts() { return iUnresolvedConflicts != null && !iUnresolvedConflicts.isEmpty(); }
		public List<ClassAssignmentDetails> getUnresolvedConflicts() { return iUnresolvedConflicts; }
		public void addUnresolvedConflict(ClassAssignmentDetails conflict) {
			if (iUnresolvedConflicts == null)
				iUnresolvedConflicts = new ArrayList<ClassAssignmentDetails>();
			iUnresolvedConflicts.add(conflict);
		}
		
		public boolean hasDifferentAssignments() { return iDifferentAssignments != null && !iDifferentAssignments.isEmpty(); }
		public List<ClassAssignmentDetails> getDifferentAssignments() { return iDifferentAssignments; }
		public void addDifferentAssignment(ClassAssignmentDetails assignment) {
			if (iDifferentAssignments == null)
				iDifferentAssignments = new ArrayList<ClassAssignmentDetails>();
			iDifferentAssignments.add(assignment);
		}
		
		public boolean hasBtbInstructorConflicts() { return iBtbInstructorConflicts != null && !iBtbInstructorConflicts.isEmpty(); }
		public void addBtbInstructorConflict(BtbInstructorInfo conf) {
			if (iBtbInstructorConflicts == null) iBtbInstructorConflicts = new ArrayList<BtbInstructorInfo>();
			iBtbInstructorConflicts.add(conf);
		}
		public List<BtbInstructorInfo> getBtbInstructorConflicts() { return iBtbInstructorConflicts; }
		
		public boolean hasStudentConflicts() { return iStudentConflicts != null && !iStudentConflicts.isEmpty(); }
		public void addStudentConflict(StudentConflictInfo conf) {
			if (iStudentConflicts == null) iStudentConflicts = new ArrayList<StudentConflictInfo>();
			iStudentConflicts.add(conf);
		}
		public List<StudentConflictInfo> getStudentConflicts() { return iStudentConflicts; }
		public int countStudentConflicts() {
			if (iStudentConflicts == null) return 0;
			int ret = 0;
			for (StudentConflictInfo c: iStudentConflicts)
				ret += c.getInfo().getJenrl();
			return ret;
		}
		
		public boolean hasDistributionConflicts() { return iDistributionConflicts != null && !iDistributionConflicts.isEmpty(); }
		public void addDistributionConflict(DistributionInfo conf) {
			if (iDistributionConflicts == null) iDistributionConflicts = new ArrayList<DistributionInfo>();
			iDistributionConflicts.add(conf);
		}
		public List<DistributionInfo> getDistributionConflicts() { return iDistributionConflicts; }
		public boolean hasViolatedDistributionConflicts() {
			if (iDistributionConflicts == null) return false;
			for (DistributionInfo di: iDistributionConflicts)
				if (!di.getInfo().isSatisfied()) return true;
			return false;
		}
		
		public boolean isCanAssign() { return iCanAssign; }
		public void setCanAssign(boolean canAssign) { iCanAssign = canAssign; }
		
		public double getValue() { return iValue; }
		public void setValue(double value) { iValue = value; }

		public int getUnassignedVariables() { return iUnassignedVariables; }
		public void setUnassignedVariables(int unassignedVariables) { iUnassignedVariables = unassignedVariables; }

		public ClassAssignmentDetails getPlacement() { return iPlacement; }
		public void setPlacement(ClassAssignmentDetails placement) { iPlacement = placement; }
		
		public ClassAssignmentDetails getSelectedPlacement() { return iSelectedPlacement; }
		public void setSelectedPlacement(ClassAssignmentDetails placement) { iSelectedPlacement = placement; }

		public double getBaseValue() { return iBaseValue; }
		public void setBaseValue(double value) { iBaseValue = value; }

		public int getBaseUnassignedVariables() { return iBaseUnassignedVariables; }
		public void setBaseUnassignedVariables(int unassignedVariables) { iBaseUnassignedVariables = unassignedVariables; }

		public void setCriterion(String name, Double value) { iCriteria.put(name, value); }
		public Map<String, Double> getCriteria() { return iCriteria; }
		public double getCriterion(String name) {
			Double value = iCriteria.get(name);
			return (value == null ? 0.0 : value.doubleValue());
		}
		
		public void setBaseCriterion(String name, Double value) { iBaseCriteria.put(name, value); }
		public Map<String, Double> getBaseCriteria() { return iBaseCriteria; }
		public double getBaseCriterion(String name) {
			Double value = iBaseCriteria.get(name);
			return (value == null ? 0.0 : value.doubleValue());
		}
		
		public SelectedAssignment getAssignment(Long classId) {
			if (classId == null) return null;
			if (hasDifferentAssignments())
				for (ClassAssignmentDetails detail: getDifferentAssignments())
					if (classId.equals(detail.getClazz().getClassId())) return detail.getAssignedSelection();
			return null;
		}
		
		public List<SelectedAssignment> getAssignment(boolean conflicts) {
			List<SelectedAssignment> ret = new ArrayList<SelectedAssignment>();
			if (hasDifferentAssignments())
				for (ClassAssignmentDetails detail: getDifferentAssignments())
					ret.add(detail.getAssignedSelection());
			if (conflicts && hasUnresolvedConflicts())
				for (ClassAssignmentDetails detail: getUnresolvedConflicts())
					ret.add(new SelectedAssignment(detail.getClazz().getClassId()));
			return ret;
		}

		@Override
		public int compareTo(Suggestion other) {
			int cmp = Double.compare(getValue(), other.getValue());
			if (cmp != 0) return cmp;
			return NaturalOrderComparator.compare(
					getDifferentAssignments() == null ? "" : getDifferentAssignments().toString(),
					other.getDifferentAssignments() == null ? "" : other.getDifferentAssignments().toString());
		}
		
		public boolean hasStudentConflictSummary() { return iStudentConflictSummary != null; }
		public void setStudentConflictSummary(TableInterface.TableCellMulti cell) { iStudentConflictSummary = cell; }
		public TableInterface.TableCellMulti getStudentConflictSummary() { return iStudentConflictSummary; }
	}
	
	public static class MakeAssignmentRequest implements GwtRpcRequest<GwtRpcResponseNull>, Serializable {
		private static final long serialVersionUID = 1L;
		private List<SelectedAssignment> iAssignments;
		
		public MakeAssignmentRequest() {}
		public MakeAssignmentRequest(List<SelectedAssignment> assignments) {
			iAssignments = assignments;
		}

		public boolean hasAssignments() { return iAssignments != null && !iAssignments.isEmpty(); }
		public void addAssignment(SelectedAssignment a) {
			if (iAssignments == null) iAssignments = new ArrayList<SelectedAssignment>();
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.equals(a)) i.remove();
			}
			iAssignments.add(a);
		}
		public SelectedAssignment removeAssignment(Long classId) {
			if (iAssignments == null) return null;
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.getClassId().equals(classId)) {
					i.remove();
					return other;
				}
			}
			return null;
		}
		public List<SelectedAssignment> getAssignments() { return iAssignments; }
	}
	
	public static class ComputeSuggestionsRequest implements GwtRpcRequest<Suggestions>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		private List<SelectedAssignment> iAssignments;
		private int iDepth = 2;
		private int iLimit = 20;
		private int iTimeLimit = 5000;
		private boolean iAllowBreakHard = false, iSameRoom = false, iSameTime = false, iPlacements = false;
		private String iFilter = null;
		
		public ComputeSuggestionsRequest() {}
		public ComputeSuggestionsRequest(Long classId) {
			setClassId(classId);
		}
		public ComputeSuggestionsRequest(Long classId, List<SelectedAssignment> assignments) {
			setClassId(classId);
			iAssignments = assignments;
		}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public boolean hasAssignments() { return iAssignments != null && !iAssignments.isEmpty(); }
		public void addAssignment(SelectedAssignment a) {
			if (iAssignments == null) iAssignments = new ArrayList<SelectedAssignment>();
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.equals(a)) i.remove();
			}
			iAssignments.add(a);
		}
		public SelectedAssignment removeAssignment(Long classId) {
			if (iAssignments == null) return null;
			for (Iterator<SelectedAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				SelectedAssignment other = i.next();
				if (other.getClassId().equals(classId)) {
					i.remove();
					return other;
				}
			}
			return null;
		}
		public List<SelectedAssignment> getAssignments() { return iAssignments; }
		
		public void setDepth(int depth) { iDepth = depth; }
		public int getDepth() { return iDepth; }
		
		public void setLimit(int limit) { iLimit = limit; }
		public int getLimit() { return iLimit; }
		
		public void setTimeLimit(int timeLimit) { iTimeLimit = timeLimit; }
		public int getTimeLimit() { return iTimeLimit; }
		
		public void setAllowBreakHard(boolean allowBreakHard) { iAllowBreakHard = allowBreakHard; }
		public boolean isAllowBreakHard() { return iAllowBreakHard; }
		public void setSameRoom(boolean sameRoom) { iSameRoom = sameRoom; }
		public boolean isSameRoom() { return iSameRoom; }
		public void setSameTime(boolean sameTime) { iSameTime = sameTime; }
		public boolean isSameTime() { return iSameTime; }
		public void setPlacements(boolean placements) { iPlacements = placements; }
		public boolean isPlacements() { return iPlacements; }
		
		public void setFilter(String filter) { iFilter = filter; }
		public String getFilter() { return iFilter; }
		public boolean hasFilter() { return iFilter != null && !iFilter.isEmpty(); }
	}
	
	public static class Suggestions implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private TreeSet<Suggestion> iSuggestions = new TreeSet<Suggestion>();
		private boolean iTimeoutReached = false;
	    private int iNrCombinationsConsidered = 0;
	    private int iNrSolutions = 0;
	    private int iDepth = 2;
	    private int iLimit = 20;
	    private int iTimeLimit = 5000;
	    private Long iClassId = null;
	    private boolean iAllowBreakHard = false, iSameRoom = false, iSameTime = false, iPlacements = false;
	    private Suggestion iBaseSuggestion;
	    private String iFilter = null;
		
		public Suggestions() {}
		
		public Suggestions(ComputeSuggestionsRequest request) {
			iLimit = request.getLimit();
			iTimeLimit = request.getTimeLimit();
			iDepth = request.getDepth();
			iClassId = request.getClassId();
			iAllowBreakHard = request.isAllowBreakHard();
			iSameRoom = request.isSameRoom();
			iSameTime = request.isSameTime();
			iPlacements = request.isPlacements();
			iFilter = request.getFilter();
		}
		
		public TreeSet<Suggestion> getSuggestions() { return iSuggestions; }
		public int size() { return iSuggestions.size(); }
		public Suggestion last() { return iSuggestions.last(); }
		public void addSuggestion(Suggestion suggestion) {
			iSuggestions.add(suggestion);
			if (iSuggestions.size() > iLimit) iSuggestions.remove(iSuggestions.last());
		}
		
		public void setTimeoutReached(boolean timeoutReached) { iTimeoutReached = timeoutReached; }
	    public boolean isTimeoutReached() { return iTimeoutReached; }
	    
	    public void setNrCombinationsConsidered(int nrCombinationsConsidered) { iNrCombinationsConsidered = nrCombinationsConsidered; }
	    public int getNrCombinationsConsidered() { return iNrCombinationsConsidered; }
	    
	    public void setNrSolutions(int nrSolutions) { iNrSolutions = nrSolutions; }
	    public int getNrSolutions() { return iNrSolutions; }
	    
	    public void setLimit(int limit) { iLimit = limit; }
	    public int getLimit() { return iLimit; }
	    
		public void setTimeLimit(int timeLimit) { iTimeLimit = timeLimit; }
		public int getTimeLimit() { return iTimeLimit; }
		
		public void setDepth(int depth) { iDepth = depth; }
		public int getDepth() { return iDepth; }
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public void setAllowBreakHard(boolean allowBreakHard) { iAllowBreakHard = allowBreakHard; }
		public boolean isAllowBreakHard() { return iAllowBreakHard; }
		public void setSameRoom(boolean sameRoom) { iSameRoom = sameRoom; }
		public boolean isSameRoom() { return iSameRoom; }
		public void setSameTime(boolean sameTime) { iSameTime = sameTime; }
		public boolean isSameTime() { return iSameTime; }
		public void setPlacements(boolean placements) { iPlacements = placements; }
		public boolean isPlacements() { return iPlacements; }
		
		public Suggestion getBaseSuggestion() { return iBaseSuggestion; }
		public void setBaseSuggestion(Suggestion suggestion) { iBaseSuggestion = suggestion; }
		
		public void setFilter(String filter) { iFilter = filter; }
		public String getFilter() { return iFilter; }
		public boolean hasFilter() { return iFilter != null && !iFilter.isEmpty(); }
	}
	
	public static class SuggestionsFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;
		private Long iClassId = null;
		
		public SuggestionsFilterRpcRequest() {}
		
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
	}
	
	public static class ComputeConflictTableRequest implements GwtRpcRequest<GwtRpcResponseList<ClassAssignmentDetails>>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		
		public ComputeConflictTableRequest() {}
		public ComputeConflictTableRequest(Long classId) {
			setClassId(classId);
		}
				
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
	}
	
	public static class CBSNode implements IsSerializable, Serializable {
		private static final long serialVersionUID = 1L;
		private int iCount = 0;
		private String iName, iHTML, iLink, iPref;
		private List<CBSNode> iNodes = null;
		private Long iClassId = null;
		private SelectedAssignment iSelection = null;
		
		public CBSNode() {}
		
		public int getCount() { return iCount; }
		public void setCount(int count) { iCount = count; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getHTML() { return iHTML; }
		public void setHTML(String html) { iHTML = html; }
		public String getPref() { return iPref; }
		public void setPref(String pref) { iPref = pref; }
		public boolean hasLink() { return iLink != null && !iLink.isEmpty(); }
		public String getLink() { return iLink; }
		public void setLink(String link) { iLink = link; }
		public boolean hasClassId() { return iClassId != null; }
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		public boolean hasSelection() { return iSelection != null; }
		public void setSelection(SelectedAssignment selection) { iSelection = selection; }
		public SelectedAssignment getSelection() { return iSelection; }
		
		public boolean hasNodes() { return iNodes != null && !iNodes.isEmpty(); }
		public List<CBSNode> getNodes() { return iNodes; }
		public void addNode(CBSNode node) {
			if (iNodes == null) iNodes = new ArrayList<CBSNode>();
			iNodes.add(node);
		}
		
		@Override
		public String toString() {
			return toString("");
		}
		
		public String toString(String indent) {
			String ret = indent + getCount() + "x " + getName();
			if (hasNodes()) {
				ret += " [";
				for (CBSNode node: getNodes())
					ret += "\n" + node.toString(indent + "  ");
				ret += "\n" + indent + "]";
			}
			return ret;
		}
	}
	
	public static class ConflictBasedStatisticsRequest implements GwtRpcRequest<GwtRpcResponseList<CBSNode>>, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iClassId;
		private boolean iVariableOriented = true;
		private double iLimit = 100.0;
		
		public ConflictBasedStatisticsRequest() {}
		public ConflictBasedStatisticsRequest(Long classId) {
			setClassId(classId);
		}
		
		public boolean hasClassId() { return iClassId != null; }
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassId() { return iClassId; }
		
		public boolean isVariableOriented() { return iVariableOriented; }
		public void setVariableOriented(boolean variableOriented) { iVariableOriented = variableOriented; }
		
		public double getLimit() { return iLimit; }
		public void setLimit(double limit) { iLimit = limit; }
	}
}