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
package org.unitime.timetable.server.solver;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
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
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.RoomSharingModel;
import org.cpsolver.coursett.model.Student;
import org.cpsolver.coursett.model.StudentGroup;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.model.TimetableModel;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Constraint;
import org.hibernate.type.LongType;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.RoomFilterBackend.LocationMatcher;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.server.Query.TermMatcher;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridBackground;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell.Property;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridCell.Type;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.TeachingResponsibilityDAO;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.ui.StudentGroupInfo;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class TimetableGridSolverHelper extends TimetableGridHelper {
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	public static TimetableGridModel createModel(TimetableSolver solver, RoomConstraint room, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.ROOM.ordinal(), room.getResourceId());
    	model.setName(room.getRoomName());
    	model.setSize(room.getCapacity());
    	model.setType(room.getType());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
		if (room instanceof DiscouragedRoomConstraint)
			model.setNameColor(PreferenceLevel.prolog2color(PreferenceLevel.sStronglyDiscouraged));
		
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		List<Placement> assignments = new ArrayList<Placement>();
		BitSet week = null;
		if (context.getFirstDay() >= 0) {
			week = new BitSet();
			for (int i = 0; i < 7; i++) {
				int d = context.getFirstDay() + i - context.getWeekOffset();
				if (d >= 0)
					week.set(d);
			}
			week = null;
		}
		for (Lecture lecture: room.variables()) {
			Placement placement = assignment.getValue(lecture);
			if (placement == null) continue;
			if (placement.hasRoomLocation(model.getResourceId())) {
				if (week == null || placement.getTimeLocation().shareWeeks(week))
					assignments.add(placement);
			}
		}
		if (ApplicationProperty.TimeGridShowClassesAcrossPartitions.isTrue()) {
			if (room.getParentRoom() != null) {
				for (Lecture lecture: room.getParentRoom().variables()) {
					Placement placement = assignment.getValue(lecture);
					if (placement == null) continue;
					if (placement.hasRoomLocation(room.getParentRoom().getResourceId())) {
						if (week == null || placement.getTimeLocation().shareWeeks(week))
							assignments.add(placement);
					}
				}
			}
			if (room.getPartitions() != null) {
				for (RoomConstraint rc: room.getPartitions()) {
					for (Lecture lecture: rc.variables()) {
						Placement placement = assignment.getValue(lecture);
						if (placement == null) continue;
						if (placement.hasRoomLocation(rc.getResourceId())) {
							if (week == null || placement.getTimeLocation().shareWeeks(week))
								assignments.add(placement);
						}
					}
				}
			}
		}
		createCells(model, solver, assignments, context, false);
		
		Set<Long> deptIds = new HashSet<Long>();
		String deptIdsStr = solver.getProperties().getProperty("General.DepartmentIds");
		if (deptIdsStr != null) {
			for (StringTokenizer stk = new StringTokenizer(deptIdsStr, ","); stk.hasMoreTokens(); ) {
				deptIds.add(Long.valueOf(stk.nextToken()));
			}
		}
		
		RoomSharingModel sharing = room.getSharingModel();
		if (sharing != null) {
			for (int i = 0; i < Constants.DAY_CODES.length; i++) {
				int start = 0; Boolean av = null;
				for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
					Boolean available;
					if (sharing.isFreeForAll(i,j)) {
						available = true;
					} else if (sharing.isNotAvailable(i,j)) {
						available = false;
					} else {
						Long dept = sharing.getDepartmentId(i,j);
						available = (dept == null || deptIds.contains(dept));
					}
					if (av == null) {
						av = available; start = j;
					} else if (!av.equals(available)) {
						if (!av) {
							TimetableGridBackground bg = new TimetableGridBackground();
							bg.setBackground(sBgColorNotAvailable);
							bg.setSlot(start);
							bg.setLength(j - start);
							bg.setDay(i);
							bg.setAvailable(false);
							model.addBackground(bg);
						}
						av = available; start = j;
					}
				}
				if (av != null && !av) {
					TimetableGridBackground bg = new TimetableGridBackground();
					bg.setBackground(sBgColorNotAvailable);
					bg.setSlot(start);
					bg.setLength(Constants.SLOTS_PER_DAY - start);
					bg.setDay(i);
					bg.setAvailable(false);
					model.addBackground(bg);
				}
			}
		}
		
		if (room.getAvailableArray() != null) {
			Set<Placement> done = new HashSet<Placement>();
			for (int i = 0; i < Constants.DAY_CODES.length; i++) {
				for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
					List<Placement> placements = room.getAvailableArray()[i * Constants.SLOTS_PER_DAY + j];
					if (placements!=null && !placements.isEmpty()) {
				        for (Placement p: placements) {
				        	if ((context.isShowEvents() || p.getAssignmentId() != null) && done.add(p) && (week == null || p.getTimeLocation().shareWeeks(week)))
				        		createCells(model, solver, p, context, true);
				        }
				    }
				}
			}
		}
		if (ApplicationProperty.TimeGridShowClassesAcrossPartitions.isTrue()) {
			Set<Placement> done = new HashSet<Placement>();
			if (room.getParentRoom() != null && room.getParentRoom().getAvailableArray() != null) {
				for (int i = 0; i < Constants.DAY_CODES.length; i++) {
					for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
						List<Placement> placements = room.getParentRoom().getAvailableArray()[i * Constants.SLOTS_PER_DAY + j];
						if (placements!=null && !placements.isEmpty()) {
					        for (Placement p: placements) {
					        	if ((context.isShowEvents() || p.getAssignmentId() != null) && done.add(p) && (week == null || p.getTimeLocation().shareWeeks(week)))
					        		createCells(model, solver, p, context, true);
					        }
					    }
					}
				}
			}
			if (room.getPartitions() != null) {
				for (RoomConstraint rc: room.getPartitions()) {
					if (rc.getAvailableArray() != null) {
						for (int i = 0; i < Constants.DAY_CODES.length; i++) {
							for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
								List<Placement> placements = rc.getAvailableArray()[i * Constants.SLOTS_PER_DAY + j];
								if (placements!=null && !placements.isEmpty()) {
							        for (Placement p: placements) {
							        	if ((context.isShowEvents() || p.getAssignmentId() != null) && done.add(p) && (week == null || p.getTimeLocation().shareWeeks(week)))
							        		createCells(model, solver, p, context, true);
							        }
							    }
							}
						}		
					}
				}
			}
		}
		
		return model;
	}
	
	public static TimetableGridModel createModel(TimetableSolver solver, InstructorConstraint instructor, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(ResourceType.INSTRUCTOR.ordinal(), instructor.getId());
    	model.setName(instructor.getName());
    	model.setExternalId(instructor.getPuid());
    	model.setType(instructor.getType());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
    	
    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
    	List<Placement> assignments = new ArrayList<Placement>();
		BitSet week = null;
		if (context.getFirstDay() >= 0) {
			week = new BitSet();
			for (int i = 0; i < 7; i++) {
				int d = context.getFirstDay() + i - context.getWeekOffset();
				if (d >= 0)
					week.set(d);
			}
		}
		for (Lecture lecture: instructor.variables()) {
			Placement placement = assignment.getValue(lecture);
			if (placement == null) continue;
			if (week == null || placement.getTimeLocation().shareWeeks(week))
				assignments.add(placement);
		}
    	createCells(model, solver, assignments, context, false);

		if (instructor.getUnavailabilities() != null) {
			for (Placement p: instructor.getUnavailabilities()) {
				if ((context.isShowEvents() || p.getAssignmentId() != null) && (week == null || p.getTimeLocation().shareWeeks(week)))
					createCells(model, solver, p, context, true);
			}
		}
		for (Student student: ((TimetableModel)solver.currentSolution().getModel()).getAllStudents()) {
			if (instructor.equals(student.getInstructor())) {
				for (Lecture lecture: student.getLectures()) {
					Placement placement = assignment.getValue(lecture);
					if (placement != null && !instructor.variables().contains(lecture) && (week == null || placement.getTimeLocation().shareWeeks(week))) {
						for (TimetableGridCell cell: createCells(model, solver, placement, context, false)) {
							cell.setItalics(true);
						}
					}
				}
			}
		}
		
		return model;
	}
	
	public static TimetableGridModel createModel(TimetableSolver solver, DepartmentSpreadConstraint department, TimetableGridContext context) {
		TimetableGridModel model = new TimetableGridModel(ResourceType.DEPARTMENT.ordinal(), department.getDepartmentId());
    	model.setName(department.getName());
    	model.setSize(department.variables().size());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
    	List<Placement> placements = new ArrayList<Placement>();
		for (Lecture lecture: department.variables()) {
			Placement placement = assignment.getValue(lecture);
			if (placement != null)
				placements.add(placement);
		}
		createCells(model, solver, placements, context, false);
		
		return model;
	}
	
	public static TimetableGridModel createModel(TimetableSolver solver, int resourceType, long resourceId, String name, int size, Collection<Placement> placements, TimetableGridContext context) {
    	TimetableGridModel model = new TimetableGridModel(resourceType, resourceId);
    	model.setName(name);
    	model.setSize(size);
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
    	createCells(model, solver, placements, context, false);
    	return model;
	}
	
	public static TimetableGridModel createModel(TimetableSolver solver, String name, List<Student> students, TimetableGridContext context) {
		TimetableGridModel model = new TimetableGridModel(ResourceType.CURRICULUM.ordinal(), -1l);
    	model.setName(name);
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());
    	
    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
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
		model.setSize((int) Math.round(size));
		model.setUtilization(countUtilization(context, placements.keySet()));
		for (Map.Entry<Placement, Double> entry: placements.entrySet()) {
			for (TimetableGridCell cell: createCells(model, solver, entry.getKey(), context, entry.getKey().variable().isCommitted())) {
				String group = (entry.getKey().variable().getConfiguration() == null ? null : groups.get(entry.getKey().variable().getConfiguration().getOfferingId()));
				cell.setGroup("(" + Math.round(entry.getValue()) + (group == null ? "" : ", " + group) + ")");
			}
		}
		
		return model;
	}
	
	public static TimetableGridModel createModel(TimetableSolver solver, StudentGroup group, TimetableGridContext context) {
		TimetableGridModel model = new TimetableGridModel(ResourceType.STUDENT_GROUP.ordinal(), group.getId());
    	model.setName(group.getName());
    	model.setFirstDay(context.getFirstDay());
    	model.setFirstSessionDay(context.getFirstSessionDay());
    	model.setFirstDate(context.getFirstDate());

    	Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		double size = 0;
		Hashtable<Placement, Double> placements = new Hashtable<Placement, Double>();
		for (Student student: group.getStudents()) {
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
		
		model.setSize((int) Math.round(size));
		model.setUtilization(StudentGroupInfo.value(group));
		for (Map.Entry<Placement, Double> entry: placements.entrySet()) {
			for (TimetableGridCell cell: createCells(model, solver, entry.getKey(), context, entry.getKey().variable().isCommitted())) {
				cell.setGroup("(" + Math.round(entry.getValue()) + ")");
			}
		}
		
		return model;
	}
	
	protected static void createCells(TimetableGridModel model, TimetableSolver solver, Collection<Placement> placements, TimetableGridContext context, boolean notAvailable) {
		model.setUtilization(countUtilization(context, placements));
		for (Placement placement: placements) {
			if (placement.variable().isCommitted()) continue;
			createCells(model, solver, placement, context, notAvailable);
		}
	}
	
	protected static void createCells(TimetableGridModel model, TimetableSolver solver, Placement[] resource, TimetableGridContext context, int firstDay, int lastDay) {
		Map<Lecture, TimetableGridCell> processed = new HashMap<Lecture, TimetableGridCell>();
		List<Placement> placements = new ArrayList<Placement>();
		for (int i = firstDay; i < lastDay; i++) {
			for (int j = 0; j < Constants.SLOTS_PER_DAY; j++) {
				Placement placement = resource[i * Constants.SLOTS_PER_DAY + j];
				if (placement == null) continue;
				Lecture lecture = placement.variable();
				if (lecture.isCommitted()) continue;
				TimetableGridCell cell = processed.get(lecture);
				if (cell == null) {
					cell = createCell(model, solver, i, j, (Lecture)placement.variable(), placement, context, false);
					processed.put(lecture, cell);
					placements.add(placement);
				} else {
					cell = new TimetableGridCell(cell, i, placement.getTimeLocation().getDatePatternName());
				}
				model.addCell(cell);
				j += placement.getTimeLocation().getNrSlotsPerMeeting() - 1;
			}
		}
		model.setUtilization(countUtilization(context, placements));
	}
	
	protected static List<TimetableGridCell> createCells(TimetableGridModel model, TimetableSolver solver, Placement placement, TimetableGridContext context, boolean notAvailable) {
		List<TimetableGridCell> cells = new ArrayList<TimetableGridCell>();
		
		if (!match(context, placement, model)) return cells;
		
		TimetableGridCell cell = null;
		
		for (Enumeration<Integer> f = placement.getTimeLocation().getStartSlots(); f.hasMoreElements(); ) {
			int slot = f.nextElement();
			int idx = (7 + slot/Constants.SLOTS_PER_DAY - context.getWeekOffset()) % 7;
			if (context.getFirstDay() >= 0 && !placement.getTimeLocation().getWeekCode().get(context.getFirstDay() + idx)) continue;
			
			if (cell==null) {
				cell = createCell(model, solver, slot / Constants.SLOTS_PER_DAY, slot % Constants.SLOTS_PER_DAY, (Lecture)placement.variable(), placement, context, notAvailable);
			} else {
				cell = new TimetableGridCell(cell, slot / Constants.SLOTS_PER_DAY, placement.getTimeLocation().getDatePatternName());
			}
			
			model.addCell(cell);
			cells.add(cell);
		}
		
		return cells;
	}
	
	protected static TimetableGridCell createCell(TimetableGridModel model, TimetableSolver solver, int day, int slot, Lecture lecture, Placement placement, TimetableGridContext context, boolean notAvailable) {
		TimetableGridCell cell = new TimetableGridCell();
		if (lecture.getClassId() < 0) {
			cell.setType(TimetableGridCell.Type.Event);
			cell.setId(-lecture.getClassId());
		} else {
			cell.setType(TimetableGridCell.Type.Class);
			cell.setId(lecture.getClassId());
			cell.setCommitted(lecture.isCommitted());
		}
		cell.addName(lecture.getName());
    	cell.setDay(day);
    	cell.setSlot(slot);
    	cell.setLength(placement.getTimeLocation().getNrSlotsPerMeeting());

    	int bgMode = context.getBgMode();
		// cell.setBackground(sBgColorNeutral);
		if (notAvailable)
			cell.setBackground(sBgColorNotAvailable);
		
		DecimalFormat df = new DecimalFormat("0.0");
		
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		
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
		
		switch (BgMode.values()[bgMode]) {
		case TimePref:
			if (notAvailable) break;
			int timePref = placement.getTimeLocation().getPreference();
			if (PreferenceLevel.sNeutral.equals(PreferenceLevel.int2prolog(timePref)) && lecture.nrTimeLocations() == 1) timePref = PreferenceLevel.sIntLevelRequired;
			cell.setBackground(pref2color(timePref));
			break;
		case RoomPref:
			if (notAvailable) break;
			int roomPref = placement.getRoomPreference();
			try {
				if (model.getResourceType() == ResourceType.ROOM.ordinal() && model.getResourceId() != null) {
					roomPref = placement.getRoomLocation(model.getResourceId()).getPreference();
				}
			} catch (NullPointerException e) {}
			if (PreferenceLevel.sNeutral.equals(PreferenceLevel.int2prolog(roomPref)) && lecture.nrRoomLocations() == lecture.getNrRooms()) roomPref = PreferenceLevel.sIntLevelRequired;
			cell.setBackground(pref2color(roomPref));
			break;
		case StudentConf:
			cell.setBackground(conflicts2color(studConf));
			if (model.getResourceType() == ResourceType.INSTRUCTOR.ordinal())
				jenrl: for (JenrlConstraint jenrl: lecture.jenrlConstraints())
					if (jenrl.getNrInstructors() > 0 && jenrl.isInConflict(assignment)) { 
						for (Student student: jenrl.getInstructors()) {
							if (model.getResourceId().equals(student.getInstructor().getResourceId()) && !student.getInstructor().variables().contains(lecture)) {
								cell.setBackground(sBgColorRequired);
								break jenrl;
							}
						}
					}
			break;
		case InstructorBtbPref:
			int instrPref = 0;
	       	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
	       		instrPref += ic.getPreferenceCombination(assignment, placement);
	       	}
	       	cell.setBackground(pref2color(instrPref));
	       	break;
		case Perturbations:
			if (notAvailable) break;
			String perPref = PreferenceLevel.sNeutral;
			if (lecture.getInitialAssignment() != null) {
				if (placement.equals(lecture.getInitialAssignment())) perPref = PreferenceLevel.sStronglyPreferred;
				else if (placement.sameTime((Placement)lecture.getInitialAssignment())) perPref = PreferenceLevel.sDiscouraged;
				else if (placement.sameRooms((Placement)lecture.getInitialAssignment())) perPref = PreferenceLevel.sStronglyDiscouraged;
				else perPref = PreferenceLevel.sProhibited;
			}
			cell.setBackground(pref2color(perPref));
			break;
		case PerturbationPenalty:
			if (notAvailable) break;
			cell.setBackground(conflicts2color((int)Math.ceil(penalty)));
			break;
		case HardConflicts:
			if (notAvailable) break;
			cell.setBackground(pref2color(hardConflicts2pref(assignment, lecture, placement)));
			break;
		case DepartmentalBalancing:
			if (notAvailable) break;
			if (deptConstraint != null)
				cell.setBackground(conflicts2colorFast(deptConstraint.getMaxPenalty(assignment, placement)));
			break;
		case TooBigRooms:
			if (notAvailable) break;
			long minRoomSize = lecture.minRoomSize();
			int roomSize = placement.getRoomSize();
			if (roomSize < lecture.minRoomSize())
				cell.setBackground(pref2color(PreferenceLevel.sRequired));
			else
				cell.setBackground(pref2color(((TooBigRooms)solver.currentSolution().getModel().getCriterion(TooBigRooms.class)).getPreference(placement)));
			if (lecture.getNrRooms() > 0) {
				cell.setPreference(
						(lecture.nrRoomLocations()==1?"<u>":"")+lecture.minRoomUse()+
						(lecture.maxRoomUse()!=lecture.minRoomUse()?" - "+lecture.maxRoomUse():"")+" / "+
						(minRoomSize == Integer.MAX_VALUE ? "-" : String.valueOf(minRoomSize))+" / "+
						roomSize+(lecture.nrRoomLocations()==1?"</u>":""));
			}
			break;
		case StudentGroups:
			TimetableModel tm = (TimetableModel)solver.currentSolution().getModel();
			if (!tm.getStudentGroups().isEmpty()) {
				int nrGroups = 0; double value = 0;
				int allAssigned = 0, grandTotal = 0;
				for (StudentGroup group: tm.getStudentGroups()) {
					int total = 0, assigned = 0;
					if (model.getResourceType() == ResourceType.STUDENT_GROUP.ordinal() && !model.getResourceId().equals(group.getId())) continue;
					for (Student student: group.getStudents()) {
						if (lecture.getConfiguration() == null || !student.hasOffering(lecture.getConfiguration().getOfferingId())) continue;
						total ++;
						if (lecture.students().contains(student)) assigned ++;
					}
					if (total > 1 && assigned > 0) {
						allAssigned += assigned; grandTotal += total;
						int limit = Math.max(lecture.students().size(), lecture.classLimit(assignment));
						if (total > limit) total = limit;
						nrGroups ++; value += ((double)assigned) / total;
					}
		        }
				if (nrGroups > 0) {
					cell.setBackground(percentage2color((int)Math.round(100.0 * value / nrGroups)));
					cell.setPreference((nrGroups == 1 ? allAssigned + " of " + grandTotal : nrGroups + " groups"));
				}
			}
			break;
		}
		
		if (!notAvailable) {
			int roomPref = placement.getRoomPreference();
			try {
				if (model.getResourceType() == ResourceType.ROOM.ordinal() && model.getResourceId() != null) {
					roomPref = placement.getRoomLocation(model.getResourceId()).getPreference();
				}
			} catch (NullPointerException e) {}
			if (!cell.hasPreference()) {
				cell.setPreference(
					(lecture.getBestTimePreference()<placement.getTimeLocation().getNormalizedPreference()?"<span style='color:red'>"+(int)(placement.getTimeLocation().getNormalizedPreference()-lecture.getBestTimePreference())+"</span>":""+(int)(placement.getTimeLocation().getNormalizedPreference()-lecture.getBestTimePreference())) + ", " +
					(studConf>0?"<span style='color:rgb(20,130,10)'>"+studConf+"</span>":""+studConf) + ", " +
					(lecture.getBestRoomPreference()<roomPref?"<span style='color:blue'>"+(roomPref-lecture.getBestRoomPreference())+"</span>":""+(roomPref-lecture.getBestRoomPreference()))
				);
			}
			
			int btbInstrPref = 0;
	       	for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
	       		btbInstrPref += ic.getPreferenceCombination(assignment, placement);
	       	}
	       	cell.setProperty(Property.TimePreference, (int)placement.getTimeLocation().getNormalizedPreference());
	       	cell.setProperty(Property.StudentConflicts, (lecture.countStudentConflicts(assignment, placement) + lecture.getCommitedConflicts(placement)));
	       	cell.setProperty(Property.StudentConflictsCommitted, (lecture.countCommittedStudentConflicts(assignment, placement) + lecture.getCommitedConflicts(placement)));
	       	cell.setProperty(Property.StudentConflictsDistance, lecture.countDistanceStudentConflicts(assignment, placement));
	       	cell.setProperty(Property.StudentConflictsHard, lecture.countHardStudentConflicts(assignment, placement));
	       	cell.setProperty(Property.RoomPreference, roomPref);
	       	if (!lecture.getInstructorConstraints().isEmpty())
	       		cell.setProperty(Property.InstructorPreference, btbInstrPref);
	       	if (lecture.getInitialAssignment() != null) {
	       		cell.setProperty(Property.InitialAssignment, (lecture.getInitialAssignment().equals(placement) ? "-" : lecture.getInitialAssignment().getName()));
	       		cell.setProperty(Property.PerturbationPenalty, df.format(penalty));
	       	}
	       	if (deptConstraint != null)
	       		cell.setProperty(Property.DepartmentBalance, deptConstraint.getMaxPenalty(assignment, placement));
	       	
			int gcPref = 0;
			for (Constraint c: lecture.constraints()) {
				if (!(c instanceof GroupConstraint)) continue;
				GroupConstraint gc = (GroupConstraint)c;
				if (gc.isHard()) continue;
				if (gc.getPreference()>0 && gc.getCurrentPreference(assignment)==0) continue;
				if (gc.getPreference()<0 && gc.getCurrentPreference(assignment)<0) continue;
				gcPref = Math.max(gcPref,Math.abs(gc.getPreference()));
			}
			cell.setProperty(Property.DistributionPreference, gcPref);
			if (bgMode == BgMode.DistributionConstPref.ordinal())
				cell.setBackground(pref2color(gcPref));
		}
		
		cell.setDays(placement.getTimeLocation().getDayHeader());
		cell.setTime(placement.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + placement.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
		cell.setDate(placement.getTimeLocation().getDatePatternName());
		cell.setWeekCode(pattern2string(placement.getTimeLocation().getWeekCode()));
		
		if (placement.isMultiRoom())
			for (RoomLocation room: placement.getRoomLocations())
				cell.addRoom(room.getName());
		else if (placement.getRoomLocation() != null)
			cell.addRoom(placement.getRoomLocation().getName());
		

		for (InstructorConstraint ic: lecture.getInstructorConstraints())
			cell.addInstructor(ic.getName());
		
    	return cell;
	}
	
	protected static double countUtilization(TimetableGridContext context, Iterable<Placement> placements) {
    	Set<Integer> slots = new HashSet<Integer>();
        for (Placement p: placements) {
        	TimeLocation t = (p == null ? null : p.getTimeLocation());
            if (t == null) continue;
            int start = Math.max(context.getFirstSlot(), t.getStartSlot());
            int stop = Math.min(context.getLastSlot(), t.getStartSlot() + t.getLength() - 1);
            if (start > stop) continue;
            if (context.getFirstDay() >= 0) {
                for (int idx = context.getFirstDay(); idx < 7 + context.getFirstDay(); idx ++) {
                	int dow = ((idx + context.getStartDayDayOfWeek()) % 7);
                	if (t.getWeekCode().get(idx) && (t.getDayCode() & Constants.DAY_CODES[dow]) != 0 && (context.getDayCode() & Constants.DAY_CODES[dow]) != 0) {
                		for (int slot = start; slot <= stop; slot ++)
                			slots.add(288 * idx + slot);
                	}
                }
            } else {
                int idx = -1;
                while ((idx = t.getWeekCode().nextSetBit(1 + idx)) >= 0) {
                	int dow = ((idx + context.getStartDayDayOfWeek()) % 7);
                	if (context.getDefaultDatePattern().get(idx) && (t.getDayCode() & Constants.DAY_CODES[dow]) != 0 && (context.getDayCode() & Constants.DAY_CODES[dow]) != 0) {
                		for (int slot = start; slot <= stop; slot ++)
                			slots.add(288 * idx + slot);
                	}
                }
            }
        }
        return slots.size() / (context.getNumberOfWeeks() * 12);
    }
	
	public static void addCrosslistedNames(TimetableGridModel model, TimetableGridContext context) {
		Map<Long, List<TimetableGridCell>> id2cells = new HashMap<Long, List<TimetableGridCell>>();
		for (TimetableGridCell cell: model.getCells()) {
			if (cell.getType() != Type.Class || cell.getId() == null || cell.getId() < 0) continue;
			List<TimetableGridCell> cells = id2cells.get(cell.getId());
			if (cells == null) {
				cells = new ArrayList<TimetableGridCell>();
				id2cells.put(cell.getId(), cells);
			}
			cells.add(cell);
		}
		if (id2cells.isEmpty()) return;
		if (id2cells.size() <= 1000) {
			for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
					"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
					"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", id2cells.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
				Class_ clazz = (Class_)o[0];
				CourseOffering course = (CourseOffering)o[1];
				for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
					cell.addName(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()));
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2cells.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
							"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
						Class_ clazz = (Class_)o[0];
						CourseOffering course = (CourseOffering)o[1];
						for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
							cell.addName(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()));
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
						"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
						"co.isControl = false and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
					Class_ clazz = (Class_)o[0];
					CourseOffering course = (CourseOffering)o[1];
					for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
						cell.addName(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()));
				}
			}
		}
	}
	
	public static void fixClassName(TimetableGridContext context, TimetableGridCell cell, Class_ clazz, CourseOffering course) {
		cell.clearName();
		if (context.isShowClassNameTwoLines()) {
    		cell.addName(clazz.getCourseName());
    		String label = clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString();
    		if (context.isShowClassSuffix()) {
    			String extId = clazz.getClassSuffix(course);
    			if (extId != null && !extId.isEmpty() && !extId.equalsIgnoreCase(clazz.getSectionNumberString()))
    				label += " - " + extId;
    		}
    		if (context.isShowConfigName() && course.getInstructionalOffering().getInstrOfferingConfigs().size() > 1) {
    			label += " (" + clazz.getSchedulingSubpart().getInstrOfferingConfig().getName() + ")";
    		}
    		cell.addName(label);
    	} else {
    		cell.addName(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()));
    	}
    	if (context.isShowCourseTitle() && course.getTitle() != null && !course.getTitle().isEmpty()) {
    		cell.addName(course.getTitle());
    	}
    	if (context.isShowCourseTitle()) {
    		cell.addTitle(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()) +
    				(course.getTitle() != null && !course.getTitle().isEmpty() ? " - " + course.getTitle() : ""));
    	} else if (context.isShowClassNameTwoLines()) {
    		cell.addTitle(clazz.getClassLabel(course, context.isShowClassSuffix(), context.isShowConfigName()));
    	}
    	if (context.isShowCrossLists()) {
    		Set<CourseOffering> courses = course.getInstructionalOffering().getCourseOfferings();
    		if (courses.size() > 1) {
    			for (CourseOffering co: new TreeSet<CourseOffering>(courses)) {
    				if (co.isIsControl()) continue;
    				cell.addName(clazz.getClassLabel(co, context.isShowClassSuffix(), context.isShowConfigName()));
    				if (context.isShowCourseTitle()) {
    					if (co.getTitle() != null && !co.getTitle().isEmpty()) {
    						cell.addName(co.getTitle());
    						cell.addTitle(clazz.getClassLabel(co, context.isShowClassSuffix(), context.isShowConfigName()) + " - " + co.getTitle());
    					} else {
    						cell.addTitle(clazz.getClassLabel(co, context.isShowClassSuffix(), context.isShowConfigName()));
    					}
    		    	} else if (context.isShowClassNameTwoLines()) {
    		    		cell.addTitle(clazz.getClassLabel(co, context.isShowClassSuffix(), context.isShowConfigName()));
    		    	}
    			}
    		}
    	}
	}
	
	public static void fixClassNames(TimetableGridModel model, TimetableGridContext context) {
		Map<Long, List<TimetableGridCell>> id2cells = new HashMap<Long, List<TimetableGridCell>>();
		for (TimetableGridCell cell: model.getCells()) {
			if (cell.getType() != Type.Class || cell.getId() == null || cell.getId() < 0) continue;
			List<TimetableGridCell> cells = id2cells.get(cell.getId());
			if (cells == null) {
				cells = new ArrayList<TimetableGridCell>();
				id2cells.put(cell.getId(), cells);
			}
			cells.add(cell);
		}
		if (id2cells.isEmpty()) return;
		if (id2cells.size() <= 1000) {
			for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
					"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
					"co.isControl = true and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", id2cells.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
				Class_ clazz = (Class_)o[0];
				CourseOffering course = (CourseOffering)o[1];
				for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
					fixClassName(context, cell, clazz, course);
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2cells.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
							"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
							"co.isControl = true and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
						Class_ clazz = (Class_)o[0];
						CourseOffering course = (CourseOffering)o[1];
						for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
							fixClassName(context, cell, clazz, course);
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(
						"select c, co from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co where " +
						"co.isControl = true and c.uniqueId in :classIds order by co.subjectAreaAbbv, co.courseNbr").setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
					Class_ clazz = (Class_)o[0];
					CourseOffering course = (CourseOffering)o[1];
					for (TimetableGridCell cell: id2cells.get(clazz.getUniqueId()))
						fixClassName(context, cell, clazz, course);
				}
			}
		}
	}
	
	public static void fixInstructors(TimetableGridModel model, TimetableGridContext context) {
		Map<Long, List<TimetableGridCell>> id2cells = new HashMap<Long, List<TimetableGridCell>>();
		for (TimetableGridCell cell: model.getCells()) {
			if (cell.getType() != Type.Class || cell.getId() == null || cell.getId() < 0 || !cell.isCommitted()) continue;
			List<TimetableGridCell> cells = id2cells.get(cell.getId());
			if (cells == null) {
				cells = new ArrayList<TimetableGridCell>();
				id2cells.put(cell.getId(), cells);
			}
			cell.resetInstructors();
			cells.add(cell);
		}
		if (id2cells.isEmpty()) return;
		
		String query = null;
		if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
			query = "select ci.classInstructing.uniqueId, ci.instructor from ClassInstructor ci " +
					(ApplicationProperty.TimetableGridUseClassInstructorsHideAuxiliary.isTrue() ? "left outer join ci.responsibility r " : "") +
					"where ci.classInstructing.uniqueId in :classIds";
			if (ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue())
				query += " and ci.classInstructing.displayInstructor = true";
			if (ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isTrue())
				query += " and ci.lead = true";
			if (ApplicationProperty.TimetableGridUseClassInstructorsHideAuxiliary.isTrue())
				query += " and (r is null or bit_and(r.options, " + TeachingResponsibility.Option.auxiliary.toggle() + ") = 0)";
		} else {
			query = "select a.clazz.uniqueId, i from Assignment a inner join a.instructors i " +
					"where a.solution.commited = true and a.clazz.uniqueId in :classIds";
		}
		
		if (id2cells.size() <= 1000) {
			for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
					.setParameterList("classIds", id2cells.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
				Long classId = (Long)o[0];
				DepartmentalInstructor instructor = (DepartmentalInstructor)o[1];
				for (TimetableGridCell cell: id2cells.get(classId)) {
					cell.addInstructor(instructor.getName(context.getInstructorNameFormat()));
				}
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2cells.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
							.setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
						Long classId = (Long)o[0];
						DepartmentalInstructor instructor = (DepartmentalInstructor)o[1];
						for (TimetableGridCell cell: id2cells.get(classId)) {
							cell.addInstructor(instructor.getName(context.getInstructorNameFormat()));
						}
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
						.setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
					Long classId = (Long)o[0];
					DepartmentalInstructor instructor = (DepartmentalInstructor)o[1];
					for (TimetableGridCell cell: id2cells.get(classId)) {
						cell.addInstructor(instructor.getName(context.getInstructorNameFormat()));
					}
				}
			}
		}
	}
	
	public static void setInstructionalTypeBackgroundColors(TimetableGridModel model, TimetableGridContext context) {
		Map<Long, List<TimetableGridCell>> id2cells = new HashMap<Long, List<TimetableGridCell>>();
		for (TimetableGridCell cell: model.getCells()) {
			if (cell.getType() != Type.Class || cell.getId() == null || cell.getId() < 0 || cell.isCommitted()) continue;
			List<TimetableGridCell> cells = id2cells.get(cell.getId());
			if (cells == null) {
				cells = new ArrayList<TimetableGridCell>();
				id2cells.put(cell.getId(), cells);
			}
			cell.resetInstructors();
			cells.add(cell);
		}
		if (id2cells.isEmpty()) return;
		
		String query = "select c.uniqueId, t.itype from Class_ c inner join c.schedulingSubpart.itype t where c.uniqueId in :classIds";
		if (id2cells.size() <= 1000) {
			for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
					.setParameterList("classIds", id2cells.keySet(), LongType.INSTANCE).setCacheable(true).list()) {
				Long classId = (Long)o[0];
				for (TimetableGridCell cell: id2cells.get(classId)) {
					cell.setBackground(context.getInstructionalTypeColor((Integer)o[1]));
				}
			}
		} else {
			List<Long> ids = new ArrayList<Long>(1000);
			for (Long id: id2cells.keySet()) {
				ids.add(id);
				if (ids.size() == 1000) {
					for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
							.setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
						Long classId = (Long)o[0];
						for (TimetableGridCell cell: id2cells.get(classId)) {
							cell.setBackground(context.getInstructionalTypeColor((Integer)o[1]));
						}
					}
					ids.clear();
				}
			}
			if (!ids.isEmpty()) {
				for (Object[] o: (List<Object[]>)Class_DAO.getInstance().getSession().createQuery(query)
						.setParameterList("classIds", ids, LongType.INSTANCE).setCacheable(true).list()) {
					Long classId = (Long)o[0];
					for (TimetableGridCell cell: id2cells.get(classId)) {
						cell.setBackground(context.getInstructionalTypeColor((Integer)o[1]));
					}
				}
			}
		}
	}
	
	private static enum Size {
		eq, lt, gt, le, ge
	};
	
	private static boolean match(TimetableGridContext context, Placement p, TimetableGridModel m) {
		if (m.getResourceType() != ResourceType.ROOM.ordinal() && !matchRooms(context, p, m)) return false;
		return matchClasses(context.getClassFilter(), p, m);
	}
	
	private static boolean matchRoom(TimetableGridContext cx, RoomLocation r, TimetableGridModel m) {
		if (r == null) return false;
		Location loc = cx.getLocation(r.getId());
		if (loc == null) return false;
		return cx.getRoomFilter().match(new LocationMatcher(loc, cx.getRoomFeatureTypes()));
	}
	
	private static boolean matchRooms(TimetableGridContext cx, Placement p, TimetableGridModel m) {
		if (cx.getRoomFilter() == null) return true;
		if (p.isMultiRoom()) {
			for (RoomLocation r: p.getRoomLocations())
				if (matchRoom(cx, r, m)) return true;
			return false;
		} else {
			return matchRoom(cx, p.getRoomLocation(), m);
		}
	}
	
	private static boolean matchClasses(Query q, final Placement p, final TimetableGridModel m) {
    	return q == null || q.match(new TermMatcher() {
			@Override
			public boolean match(String attr, String term) {
				if (term.isEmpty()) return true;
				if (attr == null) {
					for (StringTokenizer s = new StringTokenizer(p.variable().getName(), " ,"); s.hasMoreTokens(); ) {
						String token = s.nextToken();
						if (term.equalsIgnoreCase(token)) return true;
					}
					for (InstructorConstraint ic: p.variable().getInstructorConstraints()) {
						if (term.equalsIgnoreCase(ic.getPuid())) return true;
						if (term.equalsIgnoreCase(ic.getName())) return true;
						for (StringTokenizer s = new StringTokenizer(ic.getName(), " ,"); s.hasMoreTokens(); ) {
							String token = s.nextToken();
							if (term.equalsIgnoreCase(token)) return true;
						}
					}
				} else if ("regex".equals(attr) || "regexp".equals(attr) || "re".equals(attr)) {
					return p.variable().getName().matches(term);
				} else if ("find".equals(attr)) {
					return p.variable().getName().toLowerCase().indexOf(term.toLowerCase()) >= 0;
				} else if ("class".equals(attr)) {
					for (StringTokenizer s = new StringTokenizer(p.variable().getName(), " ,"); s.hasMoreTokens(); ) {
						String token = s.nextToken();
						if (term.equalsIgnoreCase(token)) return true;
					}
				} else if ("instructor".equals(attr)) {
					for (InstructorConstraint ic: p.variable().getInstructorConstraints()) {
						if (term.equalsIgnoreCase(ic.getPuid())) return true;
						if (term.equalsIgnoreCase(ic.getName())) return true;
						for (StringTokenizer s = new StringTokenizer(ic.getName(), " ,"); s.hasMoreTokens(); ) {
							String token = s.nextToken();
							if (term.equalsIgnoreCase(token)) return true;
						}
					}
				} else if ("responsibility".equals(attr)) {
					for (InstructorConstraint ic: p.variable().getInstructorConstraints()) {
						if (m.getResourceType() == ResourceType.INSTRUCTOR.ordinal() && !(
							m.getResourceId().equals(ic.getId()) ||
							(m.getExternalId() != null && m.getExternalId().equals(ic.getPuid()))))
						continue;
						TeachingResponsibility resp = null;
						if (ic.getPuid() == null)
							resp = (TeachingResponsibility) TeachingResponsibilityDAO.getInstance().getSession().createQuery(
									"select ci.responsibility from ClassInstructor ci where " +
									"ci.classInstructing.uniqueId = :classId and ci.instructor.uniqueId = :instructorId"
									).setLong("classId", p.variable().getId()).setLong("instructorId", ic.getId())
									.setMaxResults(1).setCacheable(true).uniqueResult();
						else
							resp = (TeachingResponsibility) TeachingResponsibilityDAO.getInstance().getSession().createQuery(
									"select ci.responsibility from ClassInstructor ci where " +
									"ci.classInstructing.uniqueId = :classId and ci.instructor.externalUniqueId = :extId"
									).setLong("classId", p.variable().getId()).setString("extId", ic.getPuid())
									.setMaxResults(1).setCacheable(true).uniqueResult();
						if (term.equalsIgnoreCase("null") && resp == null) return true;
						if (resp != null && (term.equalsIgnoreCase(resp.getReference()) || term.equalsIgnoreCase(resp.getLabel()))) return true;
					}
				} else if ("config".equals(attr)) {
					InstrOfferingConfig config = InstrOfferingConfigDAO.getInstance().get(p.variable().getConfiguration().getConfigId());
					return config.getName().matches(term);
				} else if ("room".equals(attr)) {
					if (p.isMultiRoom())
						for (RoomLocation l: p.getRoomLocations()) {
							if (term.equalsIgnoreCase(l.getName())) return true;
							for (StringTokenizer s = new StringTokenizer(l.getName(), " ,"); s.hasMoreTokens(); ) {
								String token = s.nextToken();
								if (term.equalsIgnoreCase(token)) return true;
							}
						}
					else if (p.getRoomLocation() != null) {
						RoomLocation l = p.getRoomLocation();
						if (term.equalsIgnoreCase(l.getName())) return true;
						for (StringTokenizer s = new StringTokenizer(l.getName(), " ,"); s.hasMoreTokens(); ) {
							String token = s.nextToken();
							if (term.equalsIgnoreCase(token)) return true;
						}
					}
				} else if ("limit".equals(attr)) {
					int min = 0, max = Integer.MAX_VALUE;
					Size prefix = Size.eq;
					String number = term;
					if (number.startsWith("<=")) { prefix = Size.le; number = number.substring(2); }
					else if (number.startsWith(">=")) { prefix = Size.ge; number = number.substring(2); }
					else if (number.startsWith("<")) { prefix = Size.lt; number = number.substring(1); }
					else if (number.startsWith(">")) { prefix = Size.gt; number = number.substring(1); }
					else if (number.startsWith("=")) { prefix = Size.eq; number = number.substring(1); }
					try {
						int a = Integer.parseInt(number);
						switch (prefix) {
							case eq: min = max = a; break; // = a
							case le: max = a; break; // <= a
							case ge: min = a; break; // >= a
							case lt: max = a - 1; break; // < a
							case gt: min = a + 1; break; // > a
						}
					} catch (NumberFormatException e) {}
					if (term.contains("..")) {
						try {
							String a = term.substring(0, term.indexOf('.'));
							String b = term.substring(term.indexOf("..") + 2);
							min = Integer.parseInt(a); max = Integer.parseInt(b);
						} catch (NumberFormatException e) {}
					}
					return min <= p.variable().maxClassLimit() && p.variable().minClassLimit() <= max;
				}
				return false;
			}
		});
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
}
