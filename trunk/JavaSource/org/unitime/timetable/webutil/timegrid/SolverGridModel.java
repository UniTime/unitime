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
package org.unitime.timetable.webutil.timegrid;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cpsolver.coursett.constraint.DepartmentSpreadConstraint;
import org.cpsolver.coursett.constraint.DiscouragedRoomConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.constraint.RoomConstraint;
import org.cpsolver.coursett.criteria.TooBigRooms;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomSharingModel;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solver.Solver;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.util.Constants;



/**
 * @author Tomas Muller
 */
public class SolverGridModel extends TimetableGridModel implements Serializable {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	private static final long serialVersionUID = 1L;
	private transient Long iRoomId = null;
	
	public SolverGridModel() {
		super();
	}
	
	public SolverGridModel(Solver solver, RoomConstraint room, int firstDay, int bgMode, boolean showEvents) {
		super(sResourceTypeRoom, room.getResourceId());
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		if (room instanceof DiscouragedRoomConstraint)
			setName("<span style='color:"+PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged)+"'>"+
					room.getRoomName()+
					"</span>");
		else
			setName(room.getRoomName());
		setSize(room.getCapacity());
        setType(room.getType());
		setFirstDay(firstDay);
		iRoomId = room.getResourceId();
		if (firstDay<0) {
			Vector placements = new Vector();
			for (Lecture lecture: room.variables()) {
				Placement placement = assignment.getValue(lecture);
				if (placement == null) continue;
				if (placement.hasRoomLocation(iRoomId))
						placements.add(placement);
			}
			init(solver, placements, bgMode, firstDay);
		} else {
			init(solver, room.getResourceOfWeek(assignment, firstDay), bgMode);
		}
		HashSet deptIds = new HashSet();
		String deptIdsStr = solver.getProperties().getProperty("General.DepartmentIds");
		if (deptIdsStr!=null) {
			for (StringTokenizer stk=new StringTokenizer(deptIdsStr,",");stk.hasMoreTokens();) {
				deptIds.add(Long.valueOf(stk.nextToken()));
			}
		}
		HashSet done = new HashSet();
		RoomSharingModel sharing = room.getSharingModel();
		for (int i=0;i<Constants.DAY_CODES.length;i++)
			for (int j=0;j<Constants.SLOTS_PER_DAY;j++) {
				if (sharing != null) {
					if (sharing.isNotAvailable(i, j)) {
						setAvailable(i,j,false);
					} else {
						Long dept = sharing.getDepartmentId(i*Constants.SLOTS_PER_DAY+j);
	                    if (dept!=null && !deptIds.contains(dept)) {
	                        setAvailable(i,j,false);
	                    }
					}
				}
                List<Placement> placements = (room.getAvailableArray()==null?null:room.getAvailableArray()[i*Constants.SLOTS_PER_DAY+j]);
                if (placements!=null && !placements.isEmpty()) {
                    for (Placement p: placements) {
                    	if ((showEvents || p.getAssignmentId() != null) && done.add(p))
                            init(solver, p, sBgModeNotAvailable, firstDay);
                    }
                }
			}
	}
	
	public SolverGridModel(Solver solver, InstructorConstraint instructor, int firstDay, int bgMode, boolean showEvents) {
		super(sResourceTypeInstructor, instructor.getResourceId());
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		setName(instructor.getName());
        setType(instructor.getType());
		setFirstDay(firstDay);
		if (firstDay<0) {
			Vector placements = new Vector();
			for (Lecture lecture: instructor.variables()) {
				Placement placement = assignment.getValue(lecture);
				if (placement != null)
					placements.add(placement);
			}
			init(solver, placements, bgMode, firstDay);
		} else {
			init(solver, instructor.getContext(assignment).getResourceOfWeek(firstDay), bgMode);
		}
		if (instructor.getUnavailabilities()!=null) {
			for (Placement p: instructor.getUnavailabilities()) {
				if (showEvents || p.getAssignmentId() != null)
					init(solver, p, sBgModeNotAvailable, firstDay);
			}
		}
		for (Student student: ((TimetableModel)solver.currentSolution().getModel()).getAllStudents()) {
			if (instructor.equals(student.getInstructor())) {
				for (Lecture lecture: student.getLectures()) {
					Placement placement = assignment.getValue(lecture);
					if (placement != null && !instructor.variables().contains(lecture)) {
						TimetableGridCell cell = init(solver, placement, bgMode, firstDay);
						while (cell != null) {
							cell.setName("<i>" + cell.getName() + "</i>");
							cell.setRoomName("<i>" + cell.getRoomName() + "</i>");
							cell = cell.getParent();
						}
					}
				}
			}
		}
	}
	
	public SolverGridModel(Solver solver, DepartmentSpreadConstraint dept, int firstDay, int bgMode) {
		super(sResourceTypeInstructor, dept.getDepartmentId().longValue());
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		setName(dept.getName());
		setSize(dept.variables().size());
		setFirstDay(firstDay);
		Vector placements = new Vector();
		for (Lecture lecture: dept.variables()) {
			Placement placement = assignment.getValue(lecture);
			if (placement != null)
				placements.add(placement);
		}
		init(solver, placements, bgMode, firstDay);
	}

	public SolverGridModel(Solver solver, String name, List<Student> students, int firstDay, int bgMode) {
		super(sResourceTypeCurriculum, -1l);
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		setName(name);
		setFirstDay(firstDay);
		Hashtable<Long, String> groups = new Hashtable<Long, String>();
		for (Object[] o: (List<Object[]>)CurriculumDAO.getInstance().getSession().createQuery(
				"select c.course.instructionalOffering.uniqueId, g.name from CurriculumCourse c inner join c.groups g where " +
				"c.classification.curriculum.abbv || ' ' || c.classification.academicClassification.code = :name and " + 
				"c.classification.curriculum.department.session.uniqueId = :sessionId")
				.setString("name", name)
				.setLong("sessionId", solver.getProperties().getPropertyLong("General.SessionId", null))
				.setCacheable(true).list()) {
			Long courseId = (Long)o[0];
			String group = (String)o[1];
			String old = groups.get(courseId);
			groups.put(courseId, (old == null ? "" : old + ", ") + group);
		}
		double size = 0;
		Hashtable<Placement, Double> placements = new Hashtable<Placement, Double>();
		for (Student student: students) {
			int cnt = 0; double w = 0;
			for (Lecture lecture: student.getLectures()) {
				w += student.getOfferingWeight(lecture.getConfiguration()); cnt ++;
				Placement placement = assignment.getValue(lecture);
				if (placement != null)  {
					Double old = placements.get(placement);
					placements.put(placement, student.getOfferingWeight(lecture.getConfiguration()) + (old == null ? 0 : old));
				}
			}
			if (student.getCommitedPlacements() != null)
				for (Placement placement: student.getCommitedPlacements()) {
					w += student.getOfferingWeight(placement.variable().getConfiguration()); cnt ++;
					Double old = placements.get(placement);
					placements.put(placement, student.getOfferingWeight(placement.variable().getConfiguration()) + (old == null ? 0 : old));
				}
			if (cnt > 0)
				size += w / cnt;
		}
		setSize((int) Math.round(size));
		for (Map.Entry<Placement, Double> entry: placements.entrySet()) {
			TimetableGridCell cell = init(solver, entry.getKey(), (entry.getKey().variable().isCommitted() ? sBgModeNotAvailable : bgMode), firstDay);
			String group = (entry.getKey().variable().getConfiguration() == null ? null : groups.get(entry.getKey().variable().getConfiguration().getOfferingId()));
			while (cell != null) {
				cell.setRoomName(cell.getRoomName() + " (" + Math.round(entry.getValue()) + (group == null ? "" : ", " + group) + ")");
				cell = cell.getParent();
			}
		}
	}

	private void init(Solver solver, Placement[] resource, int bgMode) {
		Hashtable processed = new Hashtable();
		for (int i=0;i<Constants.DAY_CODES.length;i++) {
			for (int j=0;j<Constants.SLOTS_PER_DAY;j++) {
				Placement placement = resource[i*Constants.SLOTS_PER_DAY+j];
				if (placement==null) continue;
				Lecture lecture = (Lecture)placement.variable();
				if (lecture.isCommitted()) continue;
				TimetableGridCell cell = (TimetableGridCell)processed.get(lecture);
				if (cell==null)
					cell = createCell(solver, i,j,lecture, placement, bgMode);
				else
					cell = cell.copyCell(i,cell.getMeetingNumber()+1);
				processed.put(lecture,cell);
				addCell(i,j,cell);
				j+=placement.getTimeLocation().getNrSlotsPerMeeting()-1;
			}
		}
	}
	
	private void init(Solver solver, Collection<Placement> placements, int bgMode, int firstDay) {
		for (Placement placement: placements) {
			if (placement.variable().isCommitted()) continue;
			init(solver, placement, bgMode, firstDay);
		}
	}
	
	private TimetableGridCell init(Solver solver, Placement placement, int bgMode, int firstDay) {
		TimetableGridCell cell = null;
		for (Enumeration<Integer> f=placement.getTimeLocation().getStartSlots();f.hasMoreElements();) {
			int slot = f.nextElement();
			if (firstDay>=0 && !placement.getTimeLocation().getWeekCode().get(firstDay+(slot/Constants.SLOTS_PER_DAY))) continue;
			if (cell==null) {
				cell = createCell(solver, slot/Constants.SLOTS_PER_DAY,slot%Constants.SLOTS_PER_DAY,(Lecture)placement.variable(), placement, bgMode);
			} else {
				cell = cell.copyCell(slot/Constants.SLOTS_PER_DAY,cell.getMeetingNumber()+1);
			}
			addCell(slot,cell);
		}
		return cell;
	}
	
	public static class CachedHardConflictPreference implements Serializable {
		private static final long serialVersionUID = 1L;
		private String iPreference = null;
		private Long iCreated = null;
		public CachedHardConflictPreference(String preference) {
			iPreference = preference;
			iCreated = System.currentTimeMillis();
		}
		
		public void setPreference(String preference) { iPreference = preference; }
		public String getPreference() { return iPreference; }
		
		public boolean isValid() {
			return (System.currentTimeMillis() - iCreated) < 900000l;
		}
		
		@Override
		public String toString() { return iPreference; }
	}
	
	@SuppressWarnings("deprecation")
	public static String hardConflicts2pref(Assignment<Lecture, Placement> assignment, Lecture lecture, Placement placement) {
		if (placement != null) {
			if (placement.getExtra() == null || placement.getExtra() instanceof CachedHardConflictPreference) {
				CachedHardConflictPreference cached = (placement.getExtra() == null ? null : (CachedHardConflictPreference)placement.getExtra());
				if (cached != null && cached.isValid()) return cached.getPreference();
				String preference = hardConflicts2prefNoCache(assignment, lecture, placement);
				placement.setExtra(new CachedHardConflictPreference(preference));
				return preference;
			} else {
				return hardConflicts2prefNoCache(assignment, lecture, placement);
			}
		} else {
			if (lecture.getExtra() == null || lecture.getExtra() instanceof CachedHardConflictPreference) {
				CachedHardConflictPreference cached = (lecture.getExtra() == null ? null : (CachedHardConflictPreference)lecture.getExtra());
				if (cached != null && cached.isValid()) return cached.getPreference();
				String preference = hardConflicts2prefNoCache(assignment, lecture, placement);
				lecture.setExtra(new CachedHardConflictPreference(preference));
				return preference;
			} else {
				return hardConflicts2prefNoCache(assignment, lecture, placement);
			}
		}
	}

	public static String hardConflicts2prefNoCache(Assignment<Lecture, Placement> assignment, Lecture lecture, Placement placement) {
    	if (lecture.isCommitted()) return PreferenceLevel.sRequired;
    	List<Placement> values = lecture.values(assignment);
        if (placement==null) {
        	boolean hasNoConf = false;
            for (Placement p: values) {
                if (p.isHard(assignment)) continue;
                if (lecture.getModel().conflictValues(assignment, p).isEmpty()) {
                	hasNoConf=true; break;
                }
            }
        	if (lecture.nrTimeLocations()==1) {
        		if (lecture.nrRoomLocations()==1)
        			return PreferenceLevel.sRequired;
        		else
        			return (hasNoConf?PreferenceLevel.sDiscouraged:PreferenceLevel.sStronglyDiscouraged);
        	} else {
        		if (lecture.nrRoomLocations()==1)
        			return (hasNoConf?PreferenceLevel.sStronglyPreferred:PreferenceLevel.sNeutral);
        		else
        			return (hasNoConf?PreferenceLevel.sStronglyPreferred:PreferenceLevel.sPreferred);
        	}
        }
        if (values.size()==1)
            return PreferenceLevel.sRequired;
        boolean hasTime = false;
        boolean hasRoom = false;
        boolean hasTimeNoConf = false;
        boolean hasRoomNoConf = false;
        for (Placement p: values) {
            if (p.equals(placement)) continue;
            if (p.isHard(assignment)) continue; 
            if (p.getTimeLocation().equals(placement.getTimeLocation())) {
                hasTime = true;
                if (!hasTimeNoConf) {
                	Set conf = lecture.getModel().conflictValues(assignment, p);
                	if (conf.isEmpty() || (conf.size()==1 && conf.contains(placement)))
                		hasTimeNoConf = true;
                }
            }
            if (p.sameRooms(placement)) {
            	hasRoom = true;
            	if (!hasRoomNoConf) {
                	Set conf = lecture.getModel().conflictValues(assignment, p);
                	if (conf.isEmpty() || (conf.size()==1 && conf.contains(placement)))
                		hasRoomNoConf = true;
            	}
            }
            if (hasRoomNoConf && hasTimeNoConf) break;
        }
        if (hasTimeNoConf) return PreferenceLevel.sStronglyPreferred;
        if (hasTime && hasRoomNoConf) return PreferenceLevel.sPreferred;
        if (hasTime) return PreferenceLevel.sNeutral;
        if (hasRoomNoConf) return PreferenceLevel.sDiscouraged;
        if (hasRoom) return PreferenceLevel.sStronglyDiscouraged;
        return PreferenceLevel.sRequired;
    }
    
	private TimetableGridCell createCell(Solver solver, int day, int slot, Lecture lecture, Placement placement, int bgMode) {
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		String name = lecture.getName();
		String title = "";
		int length = placement.getTimeLocation().getNrSlotsPerMeeting();
		int nrMeetings = placement.getTimeLocation().getNrMeetings();
		String shortComment = null;
		String shortCommentNoColor = null;
		String onClick = "showGwtDialog('Suggestions', 'suggestions.do?id="+lecture.getClassId()+"&op=Reset','900','90%');";
		String background = TimetableGridCell.sBgColorNeutral;
		DecimalFormat df = new DecimalFormat("0.0");
		
		if (bgMode==sBgModeNotAvailable)
			background = TimetableGridCell.sBgColorNotAvailable;
		
		int studConf = lecture.countStudentConflicts(assignment, placement) + lecture.getCommitedConflicts(placement);
		double penalty = 0.0;
		if (solver.getPerturbationsCounter()!=null) {
			penalty = solver.getPerturbationsCounter().getPerturbationPenalty(assignment, solver.currentSolution().getModel(),placement,new Vector());
		}
		DepartmentSpreadConstraint deptConstraint = null;
		for (Constraint c: lecture.constraints()) {
			if (c instanceof DepartmentSpreadConstraint) {
				deptConstraint = (DepartmentSpreadConstraint)c;
				break;
			}
		}
		
		if (bgMode==sBgModeTimePref) {
			int pref = placement.getTimeLocation().getPreference();
			if (PreferenceLevel.sNeutral.equals(PreferenceLevel.int2prolog(pref)) && lecture.nrTimeLocations()==1) pref = PreferenceLevel.sIntLevelRequired;
			background = TimetableGridCell.pref2color(pref);
		} else if (bgMode==sBgModeRoomPref) {
			int pref = (iRoomId==null?placement.getRoomPreference():placement.getRoomLocation(iRoomId).getPreference());
			if (PreferenceLevel.sNeutral.equals(PreferenceLevel.int2prolog(pref)) && lecture.nrRoomLocations()==lecture.getNrRooms()) pref = PreferenceLevel.sIntLevelRequired;
			background = TimetableGridCell.pref2color(pref);
		} else if (bgMode==sBgModeStudentConf) {
			background = TimetableGridCell.conflicts2color(studConf);
			if (getResourceType() == sResourceTypeInstructor)
				jenrl: for (JenrlConstraint jenrl: lecture.jenrlConstraints())
					if (jenrl.getNrInstructors() > 0 && jenrl.isInConflict(assignment)) { 
						for (Student student: jenrl.getInstructors()) {
							if (getResourceId() == student.getInstructor().getResourceId() && !student.getInstructor().variables().contains(lecture)) {
								background = TimetableGridCell.sBgColorRequired;
								break jenrl;
							}
						}
					}
		} else if (bgMode==sBgModeInstructorBtbPref) {
			int pref = 0;
	       	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
	       		pref += ic.getPreferenceCombination(assignment, placement);
	       	}
			background = TimetableGridCell.pref2color(pref);
		} else if (bgMode==sBgModePerturbations) {
			String pref = PreferenceLevel.sNeutral;
			if (lecture.getInitialAssignment()!=null) {
				if (placement.equals(lecture.getInitialAssignment())) pref = PreferenceLevel.sStronglyPreferred;
				else if (placement.sameTime((Placement)lecture.getInitialAssignment())) pref = PreferenceLevel.sDiscouraged;
				else if (placement.sameRooms((Placement)lecture.getInitialAssignment())) pref = PreferenceLevel.sStronglyDiscouraged;
				else pref=PreferenceLevel.sProhibited;
			}
			background = TimetableGridCell.pref2color(pref);
		} else if (bgMode==sBgModePerturbationPenalty) {
			background = TimetableGridCell.conflicts2color((int)Math.ceil(penalty));
		} else if (bgMode==sBgModeHardConflicts) {
			background = TimetableGridCell.pref2color(hardConflicts2pref(assignment, lecture, placement));
		} else if (bgMode==sBgModeDepartmentalBalancing) {
			if (deptConstraint!=null)
				background = TimetableGridCell.conflicts2colorFast(deptConstraint.getMaxPenalty(assignment, placement));
		} else if (bgMode==sBgModeTooBigRooms) {
			long minRoomSize = lecture.minRoomSize();
			int roomSize = placement.getRoomSize();
			if (roomSize < lecture.minRoomSize())
				background = TimetableGridCell.pref2color(PreferenceLevel.sRequired);
			else
				background = TimetableGridCell.pref2color(TooBigRooms.getTooBigRoomPreference(placement));
			if (lecture.getNrRooms()>0) {
				shortComment = "<span style='color:rgb(200,200,200)'>"+(lecture.nrRoomLocations()==1?"<u>":"")+lecture.minRoomUse()+(lecture.maxRoomUse()!=lecture.minRoomUse()?" - "+lecture.maxRoomUse():"")+" / "+minRoomSize+" / "+roomSize+(lecture.nrRoomLocations()==1?"</u>":"")+"</span>";
				shortCommentNoColor = lecture.minRoomUse()+(lecture.maxRoomUse()!=lecture.minRoomUse()?" - "+lecture.maxRoomUse():"")+" / "+minRoomSize+" / "+roomSize;
			}
		}
		
		if (bgMode!=sBgModeNotAvailable) {
			int roomPref = (iRoomId==null?placement.getRoomPreference():placement.getRoomLocation(iRoomId).getPreference());
			
			if (shortComment==null)
				shortComment = "<span style='color:rgb(200,200,200)'>"+
					(lecture.getBestTimePreference()<placement.getTimeLocation().getNormalizedPreference()?"<span style='color:red'>"+(int)(placement.getTimeLocation().getNormalizedPreference()-lecture.getBestTimePreference())+"</span>":""+(int)(placement.getTimeLocation().getNormalizedPreference()-lecture.getBestTimePreference())) + ", " +
					(studConf>0?"<span style='color:rgb(20,130,10)'>"+studConf+"</span>":""+studConf) + ", " +
					(lecture.getBestRoomPreference()<roomPref?"<span style='color:blue'>"+(roomPref-lecture.getBestRoomPreference())+"</span>":""+(roomPref-lecture.getBestRoomPreference()))+
					"</span>";
			if (shortCommentNoColor==null)
				shortCommentNoColor = 
				(int)(placement.getTimeLocation().getNormalizedPreference()-lecture.getBestTimePreference())+", " +
				studConf + ", " +
				(roomPref-lecture.getBestRoomPreference());
				
			
			int btbInstrPref = 0;
	       	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
	       		btbInstrPref += ic.getPreferenceCombination(assignment, placement);
	       	}
	       	
	       	title = "Time preference: "+(int)placement.getTimeLocation().getNormalizedPreference() +"<br>"+
				"Student conflicts: " + (lecture.countStudentConflicts(assignment, placement) + lecture.getCommitedConflicts(placement)) +
					" [committed:" + (lecture.countCommittedStudentConflicts(assignment, placement) + lecture.getCommitedConflicts(placement)) +
					", distance:" + lecture.countDistanceStudentConflicts(assignment, placement) +
					", hard:" + lecture.countHardStudentConflicts(assignment, placement) + "]<br>"+
				"Room preference: "+roomPref+
				(lecture.getInstructorConstraints().isEmpty()?"":"<br>Back-to-back instructor pref.: "+btbInstrPref)+
				(lecture.getInitialAssignment()!=null?"<br>Initial assignment: "+(lecture.getInitialAssignment().equals(placement)?"<i>current assignment</i>":lecture.getInitialAssignment().getName()):"")+
				(lecture.getInitialAssignment()!=null?"<br>Perturbation penalty: "+df.format(penalty):"")+
				(deptConstraint==null?"":"<br>Department balance: "+deptConstraint.getMaxPenalty(assignment, placement));
			
			int gcPref = 0;
			for (Constraint c: lecture.constraints()) {
				if (!(c instanceof GroupConstraint)) continue;
				GroupConstraint gc = (GroupConstraint)c;
				if (gc.isHard()) continue;
				if (gc.getPreference()>0 && gc.getCurrentPreference(assignment)==0) continue;
				if (gc.getPreference()<0 && gc.getCurrentPreference(assignment)<0) continue;
				gcPref = Math.max(gcPref,Math.abs(gc.getPreference()));
			}
			title = title+"<br>Distribution preference: "+gcPref;
			if (bgMode==sBgModeDistributionConstPref)
				background = TimetableGridCell.pref2color(gcPref);
		}
		
		return new TimetableGridCell(
				day,
				slot,
				placement.getId(), 
				(iRoomId==null?0:iRoomId),
				(placement.getNrRooms() == 0 ? null : placement.getRoomName(",")),
				name, 
				shortComment,
				shortCommentNoColor,
				(bgMode==sBgModeNotAvailable?null:onClick), 
				title, 
				background, 
				length, 
				0, 
				nrMeetings,
				placement.getTimeLocation().getDatePatternName(),
				placement.getTimeLocation().getWeekCode(),
				lecture.getInstructorName(),
				placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
	}
}
