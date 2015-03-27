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
package org.unitime.timetable.solver.interactive;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cpsolver.coursett.constraint.FlexibleConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solver.Solver;
import org.hibernate.Session;
import org.unitime.commons.Debug;
import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.dao.AssignmentDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.BtbInstructorConstraintInfo;
import org.unitime.timetable.solver.ui.GroupConstraintInfo;
import org.unitime.timetable.solver.ui.JenrlInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.timegrid.SolutionGridModel;
import org.unitime.timetable.webutil.timegrid.SolverGridModel;


/**
 * @author Tomas Muller
 */
public class ClassAssignmentDetails implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	public static DecimalFormat sDF = new DecimalFormat("0.###",new java.text.DecimalFormatSymbols(Locale.US));
	public static DecimalFormat sJenrDF = new DecimalFormat("0",new java.text.DecimalFormatSymbols(Locale.US));
	public static NaturalOrderComparator sCmp = new NaturalOrderComparator();
	private ClassInfo iClass = null;
	private TimeInfo iTime = null;
	private RoomInfo[] iRoom = null;
	private InstructorInfo[] iInstructor = null;
	private TimeInfo iInitialTime = null;
	private RoomInfo[] iInitialRoom = null;
	private TimeInfo iAssignedTime = null;
	private RoomInfo[] iAssignedRoom = null;
	private AssignmentPreferenceInfo iAssignmentInfo = null;
	private AssignmentPreferenceInfo iAssignedAssignmentInfo = null;
	private RoomInfos iRooms = new RoomInfos();
	private TimeInfos iTimes = new TimeInfos();
	private Vector iStudentConflicts = new Vector();
	private Vector iGroupConstraintInfos = new Vector();
	private Vector iBtbInstructorInfos = new Vector();
	
	public ClassInfo getClazz() { return iClass; }
	public TimeInfo getTime() { return iTime; }
	public RoomInfo[] getRoom() { return iRoom; }
	public InstructorInfo[] getInstructor() { return iInstructor; }
	public TimeInfo getInitialTime() { return iInitialTime; }
	public RoomInfo[] getInitialRoom() { return iInitialRoom; }
	public TimeInfo getAssignedTime() { return (iAssignedTime!=null?iAssignedTime:iTime); }
	public RoomInfo[] getAssignedRoom() { return (iAssignedRoom!=null?iAssignedRoom:iRoom); }
	public boolean isInitial() {
		return getAssignedTime()!=null && 
			getAssignedRoom()!=null && 
			getAssignedTime().equals(getInitialTime()) && 
			getAssignedRoom().equals(getInitialRoom());
	}
	public void setAssigned(AssignmentPreferenceInfo info, List<Long> roomIds, int days, int slot, Long patternId, Long datePatternId) {
		iAssignedTime = null; 
		if (days>=0 && slot>=0) {
			for (Enumeration e=iTimes.elements();e.hasMoreElements();) {
				TimeInfo time = (TimeInfo)e.nextElement();
				if (time.getDays()==days && time.getStartSlot()==slot && (patternId == null || patternId.equals(time.getPatternId())) && (datePatternId == null || datePatternId.equals(time.getDatePatternId()))) {
					iAssignedTime = time; break;
				}
			}
		}
		iAssignedRoom = null;
		if (roomIds!=null) {
			int idx = 0;
			iAssignedRoom = new RoomInfo[roomIds.size()];
			for (Long roomId: roomIds) {
				for (Enumeration f=iRooms.elements();f.hasMoreElements();) {
					RoomInfo room = (RoomInfo)f.nextElement();
					if (room.getId().equals(roomId)) {
						iAssignedRoom[idx++] = room; break;
					}
				}
			}
		}
		iAssignedAssignmentInfo = info;
	}
	public AssignmentPreferenceInfo getAssignmentInfo() { return iAssignmentInfo; }
	public RoomInfos getRooms() { return iRooms; }
	public TimeInfos getTimes() { return iTimes; }
	public Vector getStudentConflicts() { return iStudentConflicts; }
	public Vector getGroupConstraints() { return iGroupConstraintInfos; }
	public Vector getBtbInstructors() { return iBtbInstructorInfos; }
	public boolean hasViolatedGroupConstraint() {
		for (Enumeration e=iGroupConstraintInfos.elements();e.hasMoreElements();) {
			DistributionInfo info = (DistributionInfo)e.nextElement();
			if (!info.getInfo().isSatisfied()) return true;
		}
		return false;
	}
	
	public ClassAssignmentDetails(Solver solver, Lecture lecture, boolean includeConstraints) {
		this(solver, lecture, (Placement)solver.currentSolution().getAssignment().getValue(lecture), includeConstraints);
	}
	
	public ClassAssignmentDetails(Solver solver, Lecture lecture, Placement placement, boolean includeConstraints) {
		org.cpsolver.ifs.assignment.Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		iClass = new ClassInfo(lecture.getName(),lecture.getClassId(),lecture.getNrRooms(),SolverGridModel.hardConflicts2pref(solver.currentSolution().getAssignment(),lecture,placement),lecture.minRoomSize(),lecture.getOrd(),lecture.getNote());
		if (placement!=null) {
			if (placement.isMultiRoom()) {
				iRoom = new RoomInfo[placement.getRoomLocations().size()];
				int idx = 0;
				for (Iterator<RoomLocation> e=placement.getRoomLocations().iterator();e.hasNext();idx++) {
					RoomLocation room = e.next(); 
					iRoom[idx] = new RoomInfo(room.getName(),room.getId(),room.getRoomSize(),(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference()));
				}
			} else {
				RoomLocation room = placement.getRoomLocation();
				iRoom = new RoomInfo[]{new RoomInfo(room.getName(),room.getId(),room.getRoomSize(),(room.getPreference()==0 && lecture.nrRoomLocations()==1?PreferenceLevel.sIntLevelRequired:room.getPreference()))};
			}
			TimeLocation time = placement.getTimeLocation();
			int min = Constants.SLOT_LENGTH_MIN*time.getNrSlotsPerMeeting()-time.getBreakTime();
			iTime = new TimeInfo(time.getDayCode(),time.getStartSlot(),(time.getPreference()==0 && lecture.nrTimeLocations()==1?PreferenceLevel.sIntLevelRequired:time.getPreference()), min,time.getDatePatternName(),time.getTimePatternId(),time.getDatePatternId(),time.getDatePatternPreference());
			if (!lecture.getInstructorConstraints().isEmpty()) {
				iInstructor = new InstructorInfo[lecture.getInstructorConstraints().size()];
				for (int i=0;i<lecture.getInstructorConstraints().size();i++) {
					InstructorConstraint ic = (InstructorConstraint)lecture.getInstructorConstraints().get(i);
					iInstructor[i] = new InstructorInfo(ic.getName(),ic.getResourceId());
				}
			}
			iAssignmentInfo = new AssignmentPreferenceInfo(solver, placement);
		}
		Placement initialPlacement = (Placement)lecture.getInitialAssignment();
		if (initialPlacement!=null) {
			if (initialPlacement.isMultiRoom()) {
				iInitialRoom = new RoomInfo[initialPlacement.getRoomLocations().size()];
				int idx = 0;
				for (Iterator<RoomLocation> e=initialPlacement.getRoomLocations().iterator();e.hasNext();idx++) {
					RoomLocation room = e.next(); 
					iInitialRoom[idx] = new RoomInfo(room.getName(),room.getId(),room.getRoomSize(),(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference()));
				}
			} else {
				RoomLocation room = initialPlacement.getRoomLocation();
				iInitialRoom = new RoomInfo[]{new RoomInfo(room.getName(),room.getId(),room.getRoomSize(),(room.getPreference()==0 && lecture.nrRoomLocations()==1?PreferenceLevel.sIntLevelRequired:room.getPreference()))};
			}
			TimeLocation time = initialPlacement.getTimeLocation();
			int min = Constants.SLOT_LENGTH_MIN*time.getNrSlotsPerMeeting()-time.getBreakTime();
			iInitialTime = new TimeInfo(time.getDayCode(),time.getStartSlot(),(time.getPreference()==0 && lecture.nrTimeLocations()==1?PreferenceLevel.sIntLevelRequired:time.getPreference()), min,time.getDatePatternName(),time.getTimePatternId(),time.getDatePatternId(),time.getDatePatternPreference());
		}
		for (TimeLocation time: lecture.timeLocations()) {
			int min = Constants.SLOT_LENGTH_MIN*time.getNrSlotsPerMeeting()-time.getBreakTime();
			iTimes.add(new TimeInfo(time.getDayCode(),time.getStartSlot(),(time.getPreference()==0 && lecture.nrTimeLocations()==1?PreferenceLevel.sIntLevelRequired:time.getPreference()),min,time.getDatePatternName(),time.getTimePatternId(),time.getDatePatternId(),time.getDatePatternPreference()));
		}
		for (RoomLocation room: lecture.roomLocations()) {
			iRooms.add(new RoomInfo(room.getName(),room.getId(),room.getRoomSize(),(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference())));
		}
		if (includeConstraints) {
			for (Iterator e=lecture.activeJenrls(assignment).iterator();e.hasNext();) {
				JenrlConstraint jenrl = (JenrlConstraint)e.next();
				Lecture another = (Lecture)jenrl.another(lecture);
				if (!jenrl.isToBeIgnored())
					iStudentConflicts.add(new StudentConflictInfo(another.getClassId(),new JenrlInfo(solver, jenrl), StudentConflictInfo.CLASS_CONFLICT_TYPE));
			}
			if (placement!=null) {
				Hashtable infos = JenrlInfo.getCommitedJenrlInfos(solver, lecture);
    			for (Iterator i2=infos.entrySet().iterator();i2.hasNext();) {
    				Map.Entry entry = (Map.Entry)i2.next();
    				Long assignmentId = (Long)entry.getKey();
    				JenrlInfo jInfo = (JenrlInfo)entry.getValue();
    				iStudentConflicts.add(new StudentConflictInfo(assignmentId,jInfo,StudentConflictInfo.OTHER_ASSIGNMENT_CONFLICT_TYPE));
    			}
			}
			for (Constraint c: lecture.constraints()) {
				if (c instanceof GroupConstraint) {
					GroupConstraint gc = (GroupConstraint)c;
					DistributionInfo dist = new DistributionInfo(new GroupConstraintInfo(assignment, gc));
					for (Lecture another: gc.variables()) {
						if (another.equals(lecture)) continue;
						dist.addClass(another.getClassId());
					}
					iGroupConstraintInfos.add(dist);
				}
				if (c instanceof FlexibleConstraint) {
					FlexibleConstraint gc = (FlexibleConstraint)c;
					DistributionInfo dist = new DistributionInfo(new GroupConstraintInfo(assignment, gc));
					for (Lecture another: gc.variables()) {
						if (another.equals(lecture)) continue;
						dist.addClass(another.getClassId());
					}
					iGroupConstraintInfos.add(dist);
				}
			}
			if (!lecture.getInstructorConstraints().isEmpty() && placement!=null) {
				for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
				    for (Lecture other: ic.variables()) {
				        if (other.equals(lecture) || assignment.getValue(other)==null) continue;
				        int pref = ic.getDistancePreference(placement, assignment.getValue(other));
				        if (pref==PreferenceLevel.sIntLevelNeutral) continue;
				        iBtbInstructorInfos.add(new BtbInstructorInfo(other.getClassId(),pref));
				    }
				}
			}
		}
	}
	
	public ClassAssignmentDetails(Solution solution, Assignment assignment, boolean includeConstraints, Session hibSession, String instructorNameFormat) throws Exception {
		if (assignment!=null) {
			iAssignmentInfo = (AssignmentPreferenceInfo)assignment.getAssignmentInfo("AssignmentInfo");
			if (iAssignmentInfo==null) iAssignmentInfo = new AssignmentPreferenceInfo();
			if (!assignment.getRooms().isEmpty()) {
				iRoom = new RoomInfo[assignment.getRooms().size()];
				int idx = 0;
				for (Iterator i=assignment.getRooms().iterator();i.hasNext();idx++) {
					Location room = (Location)i.next();
					iRoom[idx] = new RoomInfo(room.getLabel(), room.getUniqueId(), room.getCapacity().longValue(), iAssignmentInfo.getRoomPreference(room.getUniqueId()));
				}
			} else {
				iRoom = new RoomInfo[0];
			}
			int length = assignment.getTimePattern().getSlotsPerMtg().intValue();
			int breakTime = assignment.getTimePattern().getBreakTime().intValue();
			if (assignment.getTimePattern().getType().intValue()==TimePattern.sTypeExactTime) {
				DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
				int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays()); 
				length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
				breakTime = ExactTimeMins.getBreakTime(minsPerMeeting);
			}
			int min = Constants.SLOT_LENGTH_MIN*length-breakTime;
			DatePattern datePattern = assignment.getDatePattern();
			iTime = new TimeInfo(assignment.getDays().intValue(),assignment.getStartSlot().intValue(),iAssignmentInfo.getTimePreference(),min,(datePattern==null?"not set":datePattern.getName()),assignment.getTimePattern().getUniqueId(),(datePattern==null?null:datePattern.getUniqueId()),iAssignmentInfo.getDatePatternPref());
			if (!assignment.getInstructors().isEmpty()) {
				iInstructor = new InstructorInfo[assignment.getInstructors().size()];
				int idx = 0;
				for (Iterator i=assignment.getInstructors().iterator();i.hasNext();idx++) {
					DepartmentalInstructor di = (DepartmentalInstructor)i.next(); 
					iInstructor[idx] = new InstructorInfo(di.getName(instructorNameFormat),di.getUniqueId());
				}
			}
			if (includeConstraints) {
				for (Iterator i=assignment.getConstraintInfoTable("JenrlInfo").entrySet().iterator();i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
					JenrlInfo info = (JenrlInfo)entry.getValue();
					Assignment another = null;
					for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
						Assignment x = (Assignment)j.next();
						if (x.getUniqueId().equals(assignment.getUniqueId())) continue;
						another = x; break;
					}
					iStudentConflicts.add(new StudentConflictInfo(another.getUniqueId(),info,StudentConflictInfo.OTHER_ASSIGNMENT_CONFLICT_TYPE));
				}
				for (Iterator i=assignment.getConstraintInfoTable("DistributionInfo").entrySet().iterator();i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
					GroupConstraintInfo info = (GroupConstraintInfo)entry.getValue();
					DistributionInfo dist = new DistributionInfo(info);
					for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
						Assignment another = (Assignment)j.next();
						if (another.getUniqueId().equals(assignment.getUniqueId())) continue;
						dist.addClass(another.getClazz().getUniqueId());
					}
					iGroupConstraintInfos.add(dist);
				}
				for (Iterator i=assignment.getConstraintInfoTable("BtbInstructorInfo").entrySet().iterator();i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
					BtbInstructorConstraintInfo info = (BtbInstructorConstraintInfo)entry.getValue();
					Assignment another = null;
					for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
						Assignment x = (Assignment)j.next();
						if (x.getUniqueId().equals(assignment.getUniqueId())) continue;
						another = x; break;
					}
					iBtbInstructorInfos.add(new BtbInstructorInfo(another.getClazz().getUniqueId(),info.getPreference()));
				}
			}
		}
		iClass = new ClassInfo(assignment.getClassName(), assignment.getClassId(), assignment.getRooms().size(), SolutionGridModel.hardConflicts2pref(iAssignmentInfo), -1, assignment.getClazz());
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetailsFromAssignment(SessionContext context, Long assignmentId, boolean includeConstraints) throws Exception {
		AssignmentDAO dao = new AssignmentDAO();
		org.hibernate.Session hibSession = dao.getSession();
		Assignment assignment = dao.get(assignmentId, hibSession);
		if (assignment==null) return null;
		String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		ClassAssignmentDetails ret = new ClassAssignmentDetails(assignment.getSolution(), assignment, includeConstraints, hibSession, instructorNameFormat);
			
		return ret;
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetails(SessionContext context, SolverProxy solver, Long classId, boolean includeConstraints) throws Exception {
		String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
		if (solver != null) {
			ClassAssignmentDetails details = solver.getClassAssignmentDetails(classId, includeConstraints);
			if (details!=null) return details;
    		try {
    			Class_DAO dao = new Class_DAO();
    			org.hibernate.Session hibSession = dao.getSession();
    			Class_ clazz = dao.get(classId, hibSession);
    			if (clazz==null) return null;
    			Assignment assignment = solver.getAssignment(clazz);
    			if (assignment==null || assignment.getSolution()==null) return null;
    			return new ClassAssignmentDetails(assignment.getSolution(),assignment,false,hibSession, instructorNameFormat);
    		} catch (Exception e) {
    			return null;
    		}
		}

		Class_ clazz = (new Class_DAO()).get(classId);
		if (clazz==null) return null;

		SolutionDAO dao = new SolutionDAO();
		org.hibernate.Session hibSession = dao.getSession();

		String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
		if (solutionIdsStr!=null && solutionIdsStr.length()>0) {
			for (StringTokenizer s=new StringTokenizer(solutionIdsStr,",");s.hasMoreTokens();) {
				Long solutionId = Long.valueOf(s.nextToken());
				Solution solution = dao.get(solutionId, hibSession);
				if (solution==null || !solution.getOwner().equals(clazz.getManagingDept().getSolverGroup())) continue;
				
				for (Iterator i=clazz.getAssignments().iterator();i.hasNext();) {
					Assignment assignment = (Assignment)i.next();
					if (solution.equals(assignment.getSolution()))
						return new ClassAssignmentDetails(assignment.getSolution(), assignment, includeConstraints, hibSession, instructorNameFormat);
				}
				
				return null;
			}
		}
		
		if (clazz.getCommittedAssignment()!=null) {
			return new ClassAssignmentDetails(clazz.getCommittedAssignment().getSolution(), clazz.getCommittedAssignment(), includeConstraints, hibSession, instructorNameFormat);
		}
		
		return null;
	}
	
	public class ClassInfo implements Serializable, Comparable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iClassId;
		private String iPref;
		private int iRoomCap;
		private int iNrRooms;
		private int iOrd = -1;
		private String iNote;
		private transient Class_ iClazz;
		
		public ClassInfo(String name, Long classId, int nrRooms, String pref, int roomCapacity, int ord, String note) {
			iName = name;
			iClassId = classId;
			iPref = pref;
			iNrRooms = nrRooms;
			iRoomCap = roomCapacity;
			iOrd = ord;
			iNote = note;
		}
		
		public ClassInfo(String name, Long classId, int nrRooms, String pref, int roomCapacity, Class_ clazz) {
			iName = name;
			iClassId = classId;
			iPref = pref;
			iNrRooms = nrRooms;
			iRoomCap = roomCapacity;
			iClazz = clazz;
			iNote = (iClazz==null?null:iClazz.getNotes());
		}
		
		public String getName() { return iName; }
		public Long getClassId() { return iClassId; }
		public int getRoomCapacity() { return iRoomCap; }
		public String getPref() { return iPref; }
		public String getNote() { return iNote; }
		public void setPref(String pref) { iPref = pref; }
		public int nrRooms() { return iNrRooms; }
		public boolean equals(Object o) {
			if (o==null || !(o instanceof ClassInfo)) return false;
			return getClassId().equals(((ClassInfo)o).getClassId());
		}
		public String toHtml(boolean link) {
			return toHtml(link,false);
		}
		public String toHtml(boolean link, boolean newWindow) {
			if (link) {
				if (newWindow) {
					return "<a class='noFancyLinks' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" onClick=\"showGwtDialog('Suggestions', 'suggestions.do?id="+iClassId+"&op=Reset','900','90%');\"><font color='"+PreferenceLevel.prolog2color(iPref)+"'>"+iName+"</font></a>";
				} else {
					return "<a class='noFancyLinks' href='suggestions.do?id="+iClassId+"&op=Select&noCacheTS=" + new Date().getTime()+"'><font color='"+PreferenceLevel.prolog2color(iPref)+"'>"+iName+"</font></a>";
				}
			} else
				return "<font color='"+PreferenceLevel.prolog2color(iPref)+"'>"+iName+"</font>";
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof ClassInfo)) return -1;
			ClassInfo ci = (ClassInfo)o;
			if (iOrd>=0 && ci.iOrd>=0) {
				int cmp = Double.compare(iOrd, ci.iOrd);
				if (cmp!=0) return cmp;
			} else if (iClazz!=null && ci.iClazz!=null) {
				int cmp = (new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)).compare(iClazz, ci.iClazz);
				if (cmp!=0) return cmp;
			}
			return getName().compareTo(((ClassInfo)o).getName());
		}
	}
	
	public class TimeInfo implements Serializable, Comparable {
		private static final long serialVersionUID = 1L;
		private int iDays;
		private int iStartSlot;
		private int iMin;
		private String iDatePatternName;
		private int iDatePatternPref;
		private int iPref;
		private boolean iStrike = false;
		private Long iPatternId = null, iDatePatternId;
		
		public TimeInfo(int days, int startSlot, int pref, int min, String datePatternName, Long patternId, Long datePatternId, int datePatternPref) {
			iDays = days;
			iStartSlot = startSlot;
			iPref = pref;
			iStrike = (iPref > 500);
			iMin = min;
			iDatePatternName = datePatternName;
			iPatternId = patternId;
			iDatePatternId = datePatternId;
			iDatePatternPref = datePatternPref;
		}
		
		public int getDays() { return iDays; }
		public int getStartSlot() { return iStartSlot; }
		public int getPref() { return iPref; }
		public int getMin() { return iMin; }
		public String getDatePatternName() { return iDatePatternName; }
		public String getDatePatternHtml() { return "<span style='color:"+PreferenceLevel.int2color(iDatePatternPref)+";'>" +  iDatePatternName + "</span>"; }
		public boolean isStriked() { return iStrike; }
		public Long getPatternId() { return iPatternId; }
		public Long getDatePatternId() { return iDatePatternId; }
		public int getDatePatternPreference() { return iDatePatternPref; }
		public String getDaysName() {
			StringBuffer ret = new StringBuffer();
			for (int i=0;i<Constants.DAY_NAMES_SHORT.length;i++)
				if ((Constants.DAY_CODES[i] & iDays)!=0) ret.append(Constants.DAY_NAMES_SHORT[i]);
			return ret.toString(); 
		}
		public String getStartTime() {
			return Constants.toTime(iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
		}
		public String getEndTime() {
		    return Constants.toTime(iStartSlot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN + iMin);
		}
		public boolean equals(Object o) {
			if (o==null || !(o instanceof TimeInfo)) return false;
			TimeInfo t = (TimeInfo)o;
			return t.getDays()==getDays() && t.getStartSlot()==getStartSlot() && t.getPatternId().equals(getPatternId()) && t.getDatePatternId().equals(getDatePatternId());
		}
		public String getName(boolean endTime) {
			return getDaysName()+" "+getStartTime()+(endTime?" - "+getEndTime():"");
		}
		public String toHtml(boolean link, boolean showSelected, boolean endTime, boolean showHint) {
			boolean uline = (showSelected && this.equals(iTime));
			return 
				(link?"<a id='time_"+getDays()+"_"+getStartSlot()+"_"+getPatternId()+"' onclick=\"selectTime(event, '"+getDays()+"', '"+getStartSlot()+"', '"+getPatternId()+"');\" onmouseover=\"this.style.cursor='pointer';\" class='noFancyLinks' title='"+getDaysName()+" "+getStartTime()+" - "+getEndTime()+"'>":"<a class='noFancyLinks' title='"+getDaysName()+" "+getStartTime()+" - "+getEndTime()+"'>")+
				"<span style='color:"+PreferenceLevel.int2color(iPref)+";' " +
				(showHint ? "onmouseover=\"showGwtTimeHint(this, '" + getClazz().getClassId() + "," + iDays + "," + iStartSlot + "');\" onmouseout=\"hideGwtTimeHint();\"" : "") + ">"+
				(uline?"<u>":"")+
				(iStrike?"<s>":"")+
				getDaysName()+" "+getStartTime()+
				(endTime?" - "+getEndTime():"")+
				(iStrike?"</s>":"")+
				(uline?"</u>":"")+
				"</span>"+
				"</a>";
		}
		public String toDatesHtml(boolean link, boolean showSelected, boolean endTime) {
			boolean uline = (showSelected && this.equals(iTime));
			return 
				(link?"<a id='dates_"+getDatePatternId()+"' onclick=\"selectDates(event, '"+getDatePatternId()+"');\" onmouseover=\"this.style.cursor='pointer';\" class='noFancyLinks' title='"+getDatePatternName()+"'>":"<a class='noFancyLinks' title='"+getDatePatternName()+"'>")+
				(uline?"<u>":"")+
				(iStrike?"<s>":"")+
				getDatePatternHtml()+
				(iStrike?"</s>":"")+
				(uline?"</u>":"")+
				"</a>";
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof TimeInfo)) return -1;
			TimeInfo t = (TimeInfo)o;
			if (isStriked() && !t.isStriked())
				return 1;
			if (!isStriked() && t.isStriked())
				return -1;
			int cmp = sCmp.compare(getDatePatternName(), t.getDatePatternName());
			if (cmp!=0) return cmp;
			cmp = -Double.compare(getDays(),t.getDays());
			if (cmp!=0) return cmp;
			cmp = Double.compare(getStartSlot(),t.getStartSlot());
			if (cmp!=0) return cmp;
			cmp = Double.compare(getMin(),t.getMin());
			return cmp;
		}
	}

	public class RoomInfo implements Serializable, Comparable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iRoomId;
		private int iPref;
		private long iSize;
		private boolean iStrike;
		
		public RoomInfo(String name, Long roomId, long size, int pref) {
			iName = name;
			iRoomId = roomId;
			iPref = pref;
			iSize = size;
			iStrike = (iPref > 500);
		}
		
		public String getName() { return iName; }
		public Long getId() { return iRoomId; }
		public int getPref() { return iPref; }
		public boolean isStriked() { return iStrike; }
		public long getSize() { return iSize; }
		public String getColor() { return PreferenceLevel.int2color(iPref); }
		public boolean equals(Object o) {
			if (o==null || !(o instanceof RoomInfo)) return false;
			return getId().equals(((RoomInfo)o).getId());
		}

		public String toHtml(boolean link, boolean showSelected, boolean showHint) {
			boolean uline = false;
			if (showSelected && iRoom!=null) {
				for (int i=0;i<iRoom.length;i++)
					if (iRoom[i].equals(this)) uline=true;
			}
			if (showHint) {
				return
					(link?"<a id='room_"+getId()+"' onclick=\"selectRoom(event, '"+getId()+"');\" onmouseover=\"this.style.cursor='pointer';\" class='noFancyLinks' title='"+iSize+" seats'>":"<a class='noFancyLinks' title='"+iSize+" seats'>")+
					"<span style='color:"+PreferenceLevel.int2color(iPref)+";' "+
					"onmouseover=\"showGwtRoomHint(this, '" + getId() + "', '" + PreferenceLevel.int2string(iPref) + "');\" onmouseout=\"hideGwtRoomHint();\">" +
					(uline?"<u>":"")+
					(iStrike?"<s>":"")+
					iName+
					(iStrike?"</s>":"")+
					(uline?"</u>":"")+
					"</span>"+
					"</a>";
			}
			return 
				(link?"<a id='room_"+getId()+"' onclick=\"selectRoom(event, '"+getId()+"');\" onmouseover=\"this.style.cursor='pointer';\" class='noFancyLinks' title='"+iSize+" seats'>":"<a class='noFancyLinks' title='"+iSize+" seats'>")+
				"<font color='"+PreferenceLevel.int2color(iPref)+"'>"+
				(uline?"<u>":"")+
				(iStrike?"<s>":"")+
				iName+
				(iStrike?"</s>":"")+
				(uline?"</u>":"")+
				"</font>"+
				"</a>";
		}
		public int compareTo(Object o) {
			if (o==null || !(o instanceof RoomInfo)) return -1;
			//int cmp = -(new Long(getSize())).compareTo(new Long(((RoomInfo)o).getSize()));
			//if (cmp!=0) return cmp;
			return getName().compareTo(((RoomInfo)o).getName());
		}
	}

	public class InstructorInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private Long iInstructorId;
		
		public InstructorInfo(String name, Long instructorId) {
			iName = name;
			iInstructorId = instructorId;
		}
		
		public String getName() { return iName; }
		public Long getId() { return iInstructorId; }
		public String getColor() { return "black"; }
		public boolean equals(Object o) {
			if (o==null || !(o instanceof InstructorInfo)) return false;
			return getId().equals(((InstructorInfo)o).getId());
		}
		public String toHtml() {
			return "<a href='instructorDetail.do?instructorId=" + getId() + "' target='_blank' class='noFancyLinks' " +
					"title='Open Instructor Detail for " + getName() + " in a new window.'>" + getName() + "</a>";
		}
	}
	
	public static class StudentConflictInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		public static final String CLASS_CONFLICT_TYPE = "class";
		public static final String OTHER_ASSIGNMENT_CONFLICT_TYPE = "otherAssignment";
		private JenrlInfo iInfo;
		private Long iOtherClassId = null;
		private Long iOtherAssignmentId = null;
		private ClassAssignmentDetails iOther = null;
		public StudentConflictInfo(Long id, JenrlInfo jenrl,String type) {
			iInfo = jenrl;
			if (CLASS_CONFLICT_TYPE.equals(type)){
				iOtherClassId = id;
			} else if (OTHER_ASSIGNMENT_CONFLICT_TYPE.equals(type)){
				iOtherAssignmentId = id;				
			}
		}
		public Long getOtherClassId() { return iOtherClassId; }
		public void createOther(SessionContext context, SolverProxy solver) throws Exception {
			if (iOtherAssignmentId!=null) {
				iOther = ClassAssignmentDetails.createClassAssignmentDetailsFromAssignment(context, iOtherAssignmentId, false);
			} else {
				iOther = ClassAssignmentDetails.createClassAssignmentDetails(context, solver, iOtherClassId, false);
			}
		}
		public ClassAssignmentDetails getOther() {
			return iOther;
		}
		public JenrlInfo getInfo() { return iInfo; }
		public String toHtml(SessionContext context, SolverProxy solver, boolean link) {
			try {
				if (getOther()==null) createOther(context, solver);
				Vector props = new Vector();
				if (iInfo.isCommited()) props.add("committed");
		        if (iInfo.isFixed()) props.add("fixed");
		        else if (iInfo.isHard()) props.add("hard");
		        if (iInfo.isDistance()) props.add("distance");
		        if (iInfo.isCommited())
		        	iOther.getClazz().setPref(PreferenceLevel.sRequired);
		        if (iInfo.isImportant()) props.add("important");
		        if (iInfo.isInstructor()) props.add("instructor");
		        StringBuffer sb = new StringBuffer();
		        sb.append(sJenrDF.format(iInfo.getJenrl()));
		        sb.append("&times; ");
		        sb.append(getOther().getClazz().toHtml(link && !iInfo.isCommited())+" ");
		        sb.append(getOther().getTime().toHtml(false,false,true,false)+" ");
		        if (getOther().getRoom()!=null)
		        	for (int i=0;i<getOther().getRoom().length;i++) {
		        		if (i>0) sb.append(", ");
		        		sb.append(getOther().getRoom()[i].toHtml(false,false,false));
		        	}
		        sb.append(props.isEmpty()?"":" <i>"+props+"</i>");
		        sb.append(" <i>" + iInfo.getCurriculumText() + "</i>");
		        return sb.toString();
			} catch (Exception e) {
				Debug.error(e);
				return "<font color='red'>ERROR:"+e.getMessage()+"</font>";
			}
		}
	}
	
	public static class StudentConflictInfoComparator implements Comparator {
		SessionContext iContext = null;
		SolverProxy iSolver = null;
		public StudentConflictInfoComparator(SessionContext context, SolverProxy solver) {
			iContext = context; iSolver = solver;
		}
		public int compare(Object o1, Object o2) {
			try {
				StudentConflictInfo i1 = (StudentConflictInfo)o1;
				StudentConflictInfo i2 = (StudentConflictInfo)o2;
				int cmp = Double.compare(i1.getInfo().getJenrl(),i2.getInfo().getJenrl());
				if (cmp!=0) return -cmp;
				if (i1.getInfo()==null) i1.createOther(iContext, iSolver);
				if (i2.getInfo()==null) i2.createOther(iContext, iSolver);
				return i1.getOther().compareTo(i2.getOther());
			} catch (Exception e) {
				return 0;
			}
		}
	}
	
	public class BtbInstructorInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long iOtherClassId;
		private int iPref;
		public BtbInstructorInfo(Long otherClassId, int pref) {
			iOtherClassId = otherClassId;
			iPref = pref;
		}
		public Long getOtherClassId() { return iOtherClassId; }
		public int getPreference() { return iPref; }
		public String toHtml(SessionContext context, SolverProxy solver) {
			try {
				ClassAssignmentDetails other = ClassAssignmentDetails.createClassAssignmentDetails(context, solver, iOtherClassId, false);
				StringBuffer sb = new StringBuffer();
				sb.append("<font color='"+PreferenceLevel.int2color(getPreference())+"'>");
				sb.append(PreferenceLevel.int2string(getPreference()));
				sb.append("</font>&nbsp;&nbsp;&nbsp;");
				sb.append(other.getClazz().toHtml(true)+" ");
				sb.append(other.getTime().toHtml(false,false,true,false)+" ");
				sb.append(other.getTime().toDatesHtml(false,false,true)+" ");
		        for (int i=0;i<other.getRoom().length;i++) {
		        	if (i>0) sb.append(", ");
		        	sb.append(other.getRoom()[i].toHtml(false,false,false));
		        }
		        return sb.toString();
			} catch (Exception e) {
				Debug.error(e);
				return "<font color='red'>ERROR:"+e.getMessage()+"</font>";
			}
		}
	}
	
	public class RoomInfos extends Vector implements Serializable {
		private static final long serialVersionUID = 1L;
		public RoomInfos() {
			super();
		}
		public String toHtml(boolean link, boolean showSelected, Hint selection) {
			if (isEmpty()) return "";
			int idx=0;
			StringBuffer sb = new StringBuffer();
			int count = 0;
			Collections.sort(this);
			if (link) {
				sb.append("<input type='hidden' name='nrRooms' value='"+getClazz().nrRooms()+"'/>");
				sb.append("<input type='hidden' name='curRoom' value='0'/>");
				sb.append("<input type='hidden' name='roomState' value='0'/>");
				for (int i=0;i<getClazz().nrRooms();i++)
					sb.append("<input type='hidden' name='room"+i+"' value='"+(selection==null?iRoom==null?"":iRoom[i].getId().toString():selection.getRoomIds().get(i).toString())+"'/>");
			}
			for (Enumeration e=elements();e.hasMoreElements();) {
				RoomInfo room = (RoomInfo)e.nextElement();
				if (room.isStriked()) continue;
				if (!PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(room.getPref()))) count++;
				if (idx>0) sb.append(", ");
				if (idx==4)
					sb.append("<span id='room_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('room_dots').style.display='none';document.getElementById('room_rest').style.display='inline';\">...</a></span><span id='room_rest' style='display:none'>");
				sb.append(room.toHtml(link,showSelected,true));
				idx++;
			}
			int sidx = 0;
			for (Enumeration e=elements();e.hasMoreElements();) {
				RoomInfo room = (RoomInfo)e.nextElement();
				if (!room.isStriked()) continue;
				if (idx+sidx>0) sb.append(", ");
				if (sidx==0)
					sb.append("<span id='sroom_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('sroom_dots').style.display='none';document.getElementById('sroom_rest').style.display='inline';\">...</a></span><span id='sroom_rest' style='display:none'>");
				sb.append(room.toHtml(link,showSelected,true));
				sidx++;
			}
			if (sidx>0) sb.append("</span>");
			if (idx>=4) sb.append("</span>");
			if (link) {
				sb.append("<script language='JavaScript'>initRooms();</script>");
			}
			return count+" ("+sb.toString()+")";
		}
	}

	public class TimeInfos extends Vector implements Serializable {
		private static final long serialVersionUID = 1L;
		public TimeInfos() {
			super();
		}
		public String toHtml(boolean link, boolean showSelected, Hint selection) {
			if (isEmpty()) return "";
			int idx=0;
			StringBuffer sb = new StringBuffer();
			if (link) {
				sb.append("<input type='hidden' name='days' value='"+(selection==null?iTime==null?"":String.valueOf(iTime.getDays()):String.valueOf(selection.getDays()))+"'/>");
				sb.append("<input type='hidden' name='slot' value='"+(selection==null?iTime==null?"":String.valueOf(iTime.getStartSlot()):String.valueOf(selection.getStartSlot()))+"'/>");
				sb.append("<input type='hidden' name='pattern' value='"+(selection==null?iTime==null?"":iTime.getPatternId().toString():selection.getPatternId().toString())+"'/>");
			}
			int count = 0;
			Collections.sort(this);
			Set<String> added = new HashSet<String>();
			for (Enumeration e=elements();e.hasMoreElements();) {
				TimeInfo time = (TimeInfo)e.nextElement();
				if (time.isStriked()) continue;
				if (!added.add(time.getDays()+":"+time.getStartSlot()+":"+time.getPatternId())) continue;
				if (!PreferenceLevel.sProhibited.equals(PreferenceLevel.int2prolog(time.getPref()))) count++;
				if (idx>0) sb.append(", ");
				if (idx==4)
					sb.append("<span id='time_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('time_dots').style.display='none';document.getElementById('time_rest').style.display='inline';\">...</a></span><span id='time_rest' style='display:none'>");
				sb.append(time.toHtml(link,showSelected,false,true));
				idx++;
			}
			int sidx = 0;
			for (Enumeration e=elements();e.hasMoreElements();) {
				TimeInfo time = (TimeInfo)e.nextElement();
				if (!time.isStriked()) continue;
				if (!added.add(time.getDays()+":"+time.getStartSlot()+":"+time.getPatternId())) continue;
				if (idx+sidx>0) sb.append(", ");
				if (sidx==0)
					sb.append("<span id='stime_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('stime_dots').style.display='none';document.getElementById('stime_rest').style.display='inline';\">...</a></span><span id='stime_rest' style='display:none'>");
				sb.append(time.toHtml(link,showSelected,false,true));
				sidx++;
			}
			if (sidx>0) sb.append("</span>");
			if (idx>=4) sb.append("</span>");
			if (link) {
				sb.append("<script language='JavaScript'>initTime();</script>");
			}
			return count+" ("+sb.toString()+")";
		}
		public int getNrDates() {
			Set<Long> dates = new HashSet<Long>();
			for (Enumeration e=elements();e.hasMoreElements();) {
				TimeInfo time = (TimeInfo)e.nextElement();
				dates.add(time.getDatePatternId());
			}
			return dates.size();
		}
		public String toDatesHtml(boolean link, boolean showSelected, Hint selection) {
			if (isEmpty()) return "";
			int idx=0;
			StringBuffer sb = new StringBuffer();
			if (link) {
				sb.append("<input type='hidden' name='dates' value='"+(selection==null?iTime==null?String.valueOf(((TimeInfo)elementAt(0)).getDatePatternId()):String.valueOf(iTime.getDatePatternId()):String.valueOf(selection.getDatePatternId()))+"'/>");
			}
			int count = 0;
			Collections.sort(this);
			Set<Long> added = new HashSet<Long>();
			for (Enumeration e=elements();e.hasMoreElements();) {
				TimeInfo time = (TimeInfo)e.nextElement();
				if (!added.add(time.getDatePatternId())) continue;
				if (idx>0) sb.append(", ");
				if (idx==4)
					sb.append("<span id='dates_dots' onMouseOver=\"this.style.cursor='hand';this.style.cursor='pointer';\" style='display:inline'><a onClick=\"document.getElementById('dates_dots').style.display='none';document.getElementById('dates_rest').style.display='inline';\">...</a></span><span id='dates_rest' style='display:none'>");
				sb.append(time.toDatesHtml(link,showSelected,false));
				idx++; count++;
			}
			if (idx>=4) sb.append("</span>");
			if (link) {
				sb.append("<script language='JavaScript'>initDates();</script>");
			}
			return count+" ("+sb.toString()+")";
		}
	}

	public static class DistributionInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private GroupConstraintInfo iInfo;
		private Vector iClassIds = new Vector();
		public DistributionInfo(GroupConstraintInfo info) {
			iInfo = info;
		}
		public void addClass(Long classId) {
			iClassIds.add(classId);
		}
		public Vector getClassIds() { return iClassIds; }
		public GroupConstraintInfo getInfo() { return iInfo; }
		public String toHtml(SessionContext context, SolverProxy solver, boolean link) {
			StringBuffer sb = new StringBuffer();
			try {
				for (Enumeration e=iClassIds.elements();e.hasMoreElements();) {
					Long classId = (Long)e.nextElement();
					ClassAssignmentDetails other = ClassAssignmentDetails.createClassAssignmentDetails(context, solver, classId, false);
					sb.append("<br>&nbsp;&nbsp;&nbsp;");
					sb.append(other.getClazz().toHtml(link)+" ");
					if (other.getTime()!=null)
						sb.append(other.getTime().toHtml(false,false,true,false)+" ");
					if (other.getRoom()!=null)
						for (int i=0;i<other.getRoom().length;i++) {
							if (i>0) sb.append(", ");
							sb.append(other.getRoom()[i].toHtml(false,false,false));
						}
				}
			} catch (Exception e) {
				Debug.error(e);
				sb.append("<font color='red'>ERROR:"+e.getMessage()+"</font>");
			}
			return "<font color='"+PreferenceLevel.prolog2color(iInfo.getPreference())+"'>"+
				PreferenceLevel.prolog2string(iInfo.getPreference())+
				"</font> "+iInfo.getName()+sb.toString();
		}
	}
	
	public static String dispPref(String oldPref, String newPref) {
		if (newPref==null)
			return "<font color='"+PreferenceLevel.prolog2color(oldPref)+"'><u>"+PreferenceLevel.prolog2string(oldPref)+"</u></font>";
		if (oldPref.equals(newPref))
			return "<font color='"+PreferenceLevel.prolog2color(newPref)+"'>"+PreferenceLevel.prolog2string(newPref)+"</font>";
		return "<font color='"+PreferenceLevel.prolog2color(oldPref)+"'><s>"+PreferenceLevel.prolog2string(oldPref)+"</s></font><br><font color='"+PreferenceLevel.prolog2color(newPref)+"'><u>"+PreferenceLevel.prolog2string(newPref)+"</u></font>";
	}


	public static String dispNumber(int number) {
		return dispNumber("",number);
	}
	
	public static String dispNumber(String prefix, int number) {
		if (number>0) return "<font color='red'>"+prefix+"+"+number+"</font>";
	    if (number<0) return "<font color='green'>"+prefix+number+"</font>";
	    return prefix+"0";
	}
	
	public static String dispNumber(int n1, int n2) {
		return dispNumber(n1-n2)+"</td><td nowrap>("+n2+(n1==n2?"":" &rarr; "+n1)+")";
	}
	
	public static String dispNumber(double n1, double n2) {
		return dispNumber(n1-n2)+"</td><td nowrap>("+sDF.format(n2)+(n1==n2?"":" &rarr; "+sDF.format(n1))+")";
	}
	
	public static String dispNumberShort(boolean rem, int n1, int n2) {
		if (n1==0 && n2==0) return "";
		if (rem) return dispNumber(-n1);
		int dif = n2-n1;
		if (dif==0)
			return n1+"&rarr;"+n2;
		else if (dif<0)
			return "<font color='green'>"+n1+"&rarr;"+n2+"</font>";
		else
			return "<font color='red'>"+n1+"&rarr;"+n2+"</font>";
	}
	
	public static String dispNumberShort(boolean rem, double n1, double n2) {
		return dispNumberShort(rem,"",n1,n2);
	}
	
	public static String dispNumberShort(boolean rem, String prefix, double n1, double n2) {
		if (n1==0 && n2==0) return "";
		if (rem) return dispNumber(prefix,-n1);
		double dif = n2-n1;
		if (dif==0)
			return prefix+sDF.format(n1)+"&rarr;"+sDF.format(n2);
		else if (dif<0)
			return "<font color='green'>"+prefix+sDF.format(n1)+"&rarr;"+sDF.format(n2)+"</font>";
		else
			return "<font color='red'>"+prefix+sDF.format(n1)+"&rarr;"+sDF.format(n2)+"</font>";
	}

	public static String dispTime(TimeInfo oldTime, TimeInfo newTime) {
		if (oldTime==null) {
			if (newTime==null) return "";
			else return newTime.toHtml(false,false,false,false);
		} else if (newTime==null)
			return oldTime.toHtml(false,false,false,false)+" &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
		if (oldTime.equals(newTime))
			return newTime.toHtml(false,false,false,false);
		return oldTime.toHtml(false,false,false,false)+" &rarr; "+newTime.toHtml(false,false,false,false);
	}
	
	public static String dispTime2(TimeInfo oldTime, TimeInfo newTime) {
		if (oldTime==null) {
			if (newTime==null) return "";
			else return "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; "+newTime.toHtml(false,false,false,false);
		} else if (newTime==null)
			return oldTime.toHtml(false,false,false,false)+" &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
		if (oldTime.equals(newTime))
			return newTime.toHtml(false,false,false,false);
		return oldTime.toHtml(false,false,false,false)+" &rarr; "+newTime.toHtml(false,false,false,false);
	}	

	public static String dispTimeNoHtml(TimeInfo oldTime, TimeInfo newTime) {
		if (oldTime==null) {
			if (newTime==null) return "";
			else return "not-assigned -> "+newTime.getDaysName()+" "+newTime.getStartTime();
		} else if (newTime==null)
			return oldTime.getDaysName()+" "+oldTime.getStartTime()+" -> not-assigned";
		if (oldTime.equals(newTime))
			return newTime.getDaysName()+" "+newTime.getStartTime();
		return oldTime.getDaysName()+" "+oldTime.getStartTime()+" -> "+newTime.getDaysName()+" "+newTime.getStartTime();
	}	

	public static String dispRoom(RoomInfo oldRoom, RoomInfo newRoom) {
		if (oldRoom==null)
			return newRoom.toHtml(false,false,false);
		if (newRoom==null)
			return oldRoom.toHtml(false,false,false)+" &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
		if (oldRoom.equals(newRoom))
			return newRoom.toHtml(false,false,false);
		return oldRoom.toHtml(false,false,false)+" &rarr; "+newRoom.toHtml(false,false,false);
	}
	
	public static String dispRoom2(RoomInfo oldRoom, RoomInfo newRoom) {
		if (oldRoom==null)
			return "<font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font> &rarr; "+newRoom.toHtml(false,false,false);
		if (newRoom==null)
			return oldRoom.toHtml(false,false,false)+" &rarr; <font color='"+PreferenceLevel.prolog2color("P")+"'><i>not-assigned</i></font>";
		if (oldRoom.equals(newRoom))
			return newRoom.toHtml(false,false,false);
		return oldRoom.toHtml(false,false,false)+" &rarr; "+newRoom.toHtml(false,false,false);
	}

	public static String dispRoomNoHtml(RoomInfo oldRoom, RoomInfo newRoom) {
		if (oldRoom==null)
			return "not-assigned -> "+newRoom.getName();
		if (newRoom==null)
			return oldRoom.getName()+" -> not-assigned";
		if (oldRoom.equals(newRoom))
			return newRoom.getName();
		return oldRoom.getName()+" -> "+newRoom.getName();
	}

	public static String dispNumber(double number) {
		return dispNumber("",number);
	}
	
	public static String dispNumber(String prefix, double number) {
		if (number>0) return "<font color='red'>"+prefix+"+"+sDF.format(number)+"</font>";
	    if (number<0) return "<font color='green'>"+prefix+sDF.format(number)+"</font>";
	    return prefix+"0";
	}
	
	public static String dispNumberNoHtml(double number) {
		return dispNumberNoHtml("",number);
	}
	
	public static String dispNumberNoHtml(String prefix, double number) {
		if (number>0) return prefix+"+"+sDF.format(number);
	    if (number<0) return prefix+sDF.format(number);
	    return prefix+"0";
	}

	public String getDistributionPreference() {
		if (iGroupConstraintInfos.isEmpty()) return "0";
		PreferenceCombination constrPreference = PreferenceCombination.getDefault();
		for (Enumeration i2=iGroupConstraintInfos.elements();i2.hasMoreElements();) {
			GroupConstraintInfo cInfo = ((DistributionInfo)i2.nextElement()).getInfo();
			if (!cInfo.isSatisfied())
				constrPreference.addPreferenceProlog(cInfo.getPreference());
		}
		return constrPreference.getPreferenceProlog();
	}
	
	public String getClassName() { return getClazz().getName(); }
	public String getClassHtml() { return getClazz().toHtml(true); }
	public String getTimeName() { return (getTime()==null?getAssignedTime()==null?"":getAssignedTime().getDaysName()+" "+getAssignedTime().getStartTime():getTime().getDaysName()+" "+getTime().getStartTime()); }
	public String getTimeHtml() { return dispTime(iTime,iAssignedTime); }
	public String getTimeNoHtml() { return dispTimeNoHtml(iTime,iAssignedTime); }
	public String getDaysName() { return (getTime()==null?getAssignedTime()==null?"":getAssignedTime().getDatePatternName():getTime().getDatePatternName()); }
	public String getDaysHtml() { 
		return (getTime()==null?getAssignedTime()==null?"":getAssignedTime().getDatePatternHtml():
			getAssignedTime().getDatePatternName().equals(getTime().getDatePatternName()) ? getTime().getDatePatternHtml() :
			getTime().getDatePatternHtml() + " &rarr; " + getAssignedTime().getDatePatternHtml());
	}
	public String getRoomName() {
		RoomInfo[] r = (getRoom()==null?getAssignedRoom():getRoom());
		if (r==null) return null;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<r.length;i++) {
			if (i>0) sb.append(", ");
			sb.append(r[i].getName());
		}
		return sb.toString(); 
	}
	public String getRoomHtml() {
		RoomInfo[] r = (getRoom()==null?getAssignedRoom():getRoom());
		if (r==null) return null;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<r.length;i++) {
			if (i>0) sb.append(", ");
			sb.append(dispRoom((iRoom==null?null:iRoom[i]),(iAssignedRoom==null?null:iAssignedRoom[i])));
		}
		return sb.toString(); 
		 
	}
	public String getRoomNoHtml() {
		RoomInfo[] r = (getRoom()==null?getAssignedRoom():getRoom());
		if (r==null) return null;
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<r.length;i++) {
			if (i>0) sb.append(", ");
			sb.append(dispRoomNoHtml((iRoom==null?null:iRoom[i]),(iAssignedRoom==null?null:iAssignedRoom[i])));
		}
		return sb.toString(); 
		 
	}
	public String getPertPenalty() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0.0:iAssignmentInfo.getPerturbationPenalty()),(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getPerturbationPenalty()));
	}
	public Comparable getPertPenaltyCmp() {
		return new Double((iAssignmentInfo==null?0.0:iAssignmentInfo.getPerturbationPenalty())-(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getPerturbationPenalty()));
	}
	public String getNrStudentConflicts() {
		String s = dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getNrStudentConflicts()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getNrStudentConflicts()));
		String c = dispNumberShort(iAssignedAssignmentInfo==null,"c",(iAssignmentInfo==null?0:iAssignmentInfo.getNrCommitedStudentConflicts()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getNrCommitedStudentConflicts()));
		String d = dispNumberShort(iAssignedAssignmentInfo==null,"d",(iAssignmentInfo==null?0:iAssignmentInfo.getNrDistanceStudentConflicts()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getNrDistanceStudentConflicts()));
		String h = dispNumberShort(iAssignedAssignmentInfo==null,"h",(iAssignmentInfo==null?0:iAssignmentInfo.getNrHardStudentConflicts()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getNrHardStudentConflicts()));
		if (c.length()==0 && d.length()==0 && h.length()==0) return s;
		StringBuffer sb = new StringBuffer();
		if (c.length()>0) {
			sb.append(c);
		}
		if (d.length()>0) {
			if (sb.length()>0) sb.append(",");
			sb.append(d);
		}
		if (h.length()>0) {
			if (sb.length()>0) sb.append(",");
			sb.append(h);
		}
		return s+" ("+sb+")";
	}
	public Comparable getNrStudentConflictsCmp() {
		return new Integer((iAssignmentInfo==null?0:iAssignmentInfo.getNrStudentConflicts())-(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getNrStudentConflicts()));
	}
	public String getTimePreference() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0.0:iAssignmentInfo.getNormalizedTimePreference()),(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getNormalizedTimePreference()));
	}
	public Comparable getTimePreferenceCmp() {
		return new Double((iAssignmentInfo==null?0.0:iAssignmentInfo.getNormalizedTimePreference())-(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getNormalizedTimePreference()));
	}
	public String getRoomPreference() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0.0:iAssignmentInfo.sumRoomPreference()),(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.sumRoomPreference()));
	}
	public Comparable getRoomPreferenceCmp() {
		return new Double((iAssignmentInfo==null?0.0:iAssignmentInfo.sumRoomPreference())-(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.sumRoomPreference()));
	}
	public String getBtbInstructorPreference() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getBtbInstructorPreference()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getBtbInstructorPreference()));
	}
	public Comparable getBtbInstructorPreferenceCmp() {
		return new Double((iAssignmentInfo==null?0.0:iAssignmentInfo.getBtbInstructorPreference())-(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getBtbInstructorPreference()));
	}
	public String getIsTooBig() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getTooBigRoomPreference()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getTooBigRoomPreference()));
	}
	public Comparable getIsTooBigCmp() {
		return new Integer((iAssignmentInfo==null?0:iAssignmentInfo.getTooBigRoomPreference())-(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getTooBigRoomPreference()));
	}
	public String getUselessHalfHours() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getUselessHalfHours()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getUselessHalfHours()));
	}
	public Comparable getUselessHalfHoursCmp() {
		return new Integer((iAssignmentInfo==null?0:iAssignmentInfo.getUselessHalfHours())-(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getUselessHalfHours()));
	}
	public String getDeptBalancPenalty() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getDeptBalancPenalty()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getDeptBalancPenalty()));
	}
	public Comparable getDeptBalancPenaltyCmp() {
		return new Double((iAssignmentInfo==null?0.0:iAssignmentInfo.getDeptBalancPenalty())-(iAssignedAssignmentInfo==null?0.0:iAssignedAssignmentInfo.getDeptBalancPenalty()));
	}
	public String getSpreadPenalty() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getSpreadPenalty()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getSpreadPenalty()));
	}
	public Comparable getSpreadPenaltyCmp() {
		return new Double((iAssignmentInfo==null?0:iAssignmentInfo.getSpreadPenalty())-(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getSpreadPenalty()));
	}
	public String getGroupConstraintPref() {
		return dispNumberShort(iAssignedAssignmentInfo==null,(iAssignmentInfo==null?0:iAssignmentInfo.getGroupConstraintPref()),(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getGroupConstraintPref()));
	}
	public Comparable getGroupConstraintPrefCmp() {
		return new Integer((iAssignmentInfo==null?0:iAssignmentInfo.getGroupConstraintPref())-(iAssignedAssignmentInfo==null?0:iAssignedAssignmentInfo.getGroupConstraintPref()));
	}
	public AssignmentPreferenceInfo getInfo() {
		return iAssignmentInfo;
	}
	public Hint getHint() {
		if (getAssignedTime()==null || getAssignedRoom()==null) return null;
		Vector roomIds = new Vector();
		for (int i=0;i<getAssignedRoom().length;i++)
			roomIds.add(getAssignedRoom()[i].getId());
		return new Hint(getClazz().getClassId(),getAssignedTime().getDays(), getAssignedTime().getStartSlot(), roomIds,getAssignedTime().getPatternId(),getAssignedTime().getDatePatternId());
	}
	public boolean equals(Object o) {
		if (o==null || !(o instanceof ClassAssignmentDetails)) return false;
		return iClass.equals(((ClassAssignmentDetails)o).iClass);
	}
	public int hashCode() {
		return iClass.getClassId().hashCode();
	}
	public int compareTo(Object o) {
		if (o==null || !(o instanceof ClassAssignmentDetails)) return -1;
		return iClass.compareTo(((ClassAssignmentDetails)o).iClass);
	}
	public String getInstructorName() {
		if (iInstructor==null || iInstructor.length==0) return "";
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<iInstructor.length;i++) {
			if (i>0) sb.append(", ");
			sb.append(iInstructor[i].getName());
		}
		return sb.toString();
	}
	public String getInstructorHtml() {
		if (iInstructor==null || iInstructor.length==0) return "";
		StringBuffer sb = new StringBuffer();
		for (int i=0;i<iInstructor.length;i++) {
			if (i>0) sb.append(", ");
			sb.append(iInstructor[i].toHtml());
		}
		return sb.toString();
	}
	
    public String toString() {
        return getClassName()+" "+getTimeNoHtml()+" "+getRoomNoHtml()+" "+getDaysName()+" "+getInstructorName();
    }

}
