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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import org.cpsolver.coursett.constraint.FlexibleConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.constraint.InstructorConstraint;
import org.cpsolver.coursett.constraint.JenrlConstraint;
import org.cpsolver.coursett.criteria.StudentOverlapConflict;
import org.cpsolver.coursett.criteria.placement.DeltaTimePreference;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.criteria.Criterion;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.solver.Solver;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.BtbInstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetails;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassAssignmentDetailsRequest;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.CurriculumInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.DistributionInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.GroupConstraintInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.JenrlInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.RoomInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.StudentConflictInfo;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.TimeInfo;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.ExactTimeMins;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.dao.AssignmentDAO;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.BtbInstructorConstraintInfo;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.duration.DurationModel;
import org.unitime.timetable.webutil.timegrid.SolutionGridModel;
import org.unitime.timetable.webutil.timegrid.SolverGridModel;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ClassAssignmentDetailsRequest.class)
public class ClassAssignmentDetailsBackend implements GwtRpcImplementation<ClassAssignmentDetailsRequest, ClassAssignmentDetails> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class); 
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;

	@Override
	public ClassAssignmentDetails execute(ClassAssignmentDetailsRequest request, SessionContext context) {
		context.checkPermission(Right.Suggestions);
		
		SuggestionsContext cx = new SuggestionsContext();
		String instructorFormat = context.getUser().getProperty(UserProperty.NameFormat);
    	if (instructorFormat != null)
    		cx.setInstructorNameFormat(instructorFormat);
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver != null) {
			ClassAssignmentDetails details = solver.getClassAssignmentDetails(cx, request.getClassId(), true, true);
			if (details != null) return details;
			try {
				Class_ clazz = Class_DAO.getInstance().get(request.getClassId());
				if (clazz == null)
					throw new GwtRpcException(MESSAGES.errorClassDoesNotExist(request.getClassId()));
				org.unitime.timetable.model.Assignment assignment = solver.getAssignment(clazz);
				if (assignment == null || assignment.getSolution() == null) return createClassAssignmentDetailsFromClass(cx, clazz);
				return createClassAssignmentDetailsFromAssignment(cx, assignment, true);
			} catch (Exception e) {
				throw new GwtRpcException(e.getMessage(), e);
			}
		}
		
		ClassAssignmentProxy proxy = classAssignmentService.getAssignment();
		if (proxy != null) {
			org.unitime.timetable.model.Assignment assignment = proxy.getAssignment(request.getClassId());
			if (assignment != null)
				return createClassAssignmentDetailsFromAssignment(cx, assignment, true);
			Class_ clazz = Class_DAO.getInstance().get(request.getClassId());
			if (clazz == null)
				throw new GwtRpcException(MESSAGES.errorClassDoesNotExist(request.getClassId()));
			return createClassAssignmentDetailsFromClass(cx, clazz);
		}
		
		return null;
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetails(SuggestionsContext context, Solver solver, Lecture lecture, boolean includeDomain, boolean includeConstraints) {
		return createClassAssignmentDetails(context, solver, lecture, (Placement)solver.currentSolution().getAssignment().getValue(lecture), includeDomain, includeConstraints);
	}
		
	public static ClassAssignmentDetails createClassAssignmentDetails(SuggestionsContext context, Solver solver, Lecture lecture, Placement placement, boolean includeDomain, boolean includeConstraints) {
		Assignment<Lecture, Placement> assignment = solver.currentSolution().getAssignment();
		ClassAssignmentDetails details = new ClassAssignmentDetails();
		details.setCanUnassign(!lecture.isCommitted());
		details.setClazz(new ClassInfo(
				lecture.getName(),
				lecture.getClassId(),
				lecture.getNrRooms(),
				SolverGridModel.hardConflicts2pref(solver.currentSolution().getAssignment(),lecture,placement),
				lecture.minRoomSize(),
				lecture.getOrd(),
				lecture.getNote()));
		if (placement != null) {
			if (placement.isMultiRoom()) {
				for (RoomLocation room: placement.getRoomLocations()) {
					details.setRoom(new RoomInfo(
							room.getName(),
							room.getId(),
							room.getRoomSize(),
							(room.getPreference() == 0 && lecture.nrRoomLocations() == lecture.getNrRooms() ? PreferenceLevel.sIntLevelRequired : room.getPreference())
							));
				}
			} else {
				RoomLocation room = placement.getRoomLocation();
				details.setRoom(new RoomInfo(
						room.getName(),
						room.getId(),
						room.getRoomSize(),
						(room.getPreference() == 0 && lecture.nrRoomLocations() == lecture.getNrRooms() ? PreferenceLevel.sIntLevelRequired : room.getPreference())
						));
			}
			TimeLocation time = placement.getTimeLocation();
			int min = Constants.SLOT_LENGTH_MIN * time.getNrSlotsPerMeeting() - time.getBreakTime();
			details.setTime(new TimeInfo(
					time.getDayCode(),
					time.getStartSlot(),
					(time.getPreference() == 0 && lecture.nrTimeLocations() == 1 ? PreferenceLevel.sIntLevelRequired : time.getPreference()),
					min,
					time.getDatePatternName(),
					time.getTimePatternId(),
					time.getDatePatternId(),
					time.getDatePatternPreference()
					));
			if (!lecture.getInstructorConstraints().isEmpty()) {
				for (int i=0;i<lecture.getInstructorConstraints().size();i++) {
					InstructorConstraint ic = (InstructorConstraint)lecture.getInstructorConstraints().get(i);
					details.setInstructor(new InstructorInfo(ic.getName(), ic.getResourceId()));
				}
			}
			for (Criterion<Lecture, Placement> criterion: lecture.getModel().getCriteria()) {
				if (criterion instanceof StudentOverlapConflict) continue;
				if (criterion instanceof DeltaTimePreference) continue;
				details.setObjective(criterion.getName(), criterion.getValue(assignment, placement, null));
			}
		}
		Placement initialPlacement = (Placement)lecture.getInitialAssignment();
		if (initialPlacement != null) {
			if (initialPlacement.isMultiRoom()) {
				for (RoomLocation room: initialPlacement.getRoomLocations()) {
					details.setInitialRoom(new RoomInfo(
							room.getName(),
							room.getId(),
							room.getRoomSize(),
							(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference())
							));
				}
			} else {
				RoomLocation room = initialPlacement.getRoomLocation();
				details.setInitialRoom(new RoomInfo(
						room.getName(),
						room.getId(),
						room.getRoomSize(),
						(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference())
						));
			}
			TimeLocation time = initialPlacement.getTimeLocation();
			int min = Constants.SLOT_LENGTH_MIN * time.getNrSlotsPerMeeting() - time.getBreakTime();
			details.setInitialTime(new TimeInfo(
					time.getDayCode(),
					time.getStartSlot(),
					(time.getPreference()==0 && lecture.nrTimeLocations()==1?PreferenceLevel.sIntLevelRequired:time.getPreference()),
					min,
					time.getDatePatternName(),
					time.getTimePatternId(),
					time.getDatePatternId(),
					time.getDatePatternPreference()
					));
		}
		if (includeDomain) {
			for (TimeLocation time: lecture.timeLocations()) {
				int min = Constants.SLOT_LENGTH_MIN * time.getNrSlotsPerMeeting() - time.getBreakTime();
				details.addTime(new TimeInfo(
						time.getDayCode(),
						time.getStartSlot(),
						(time.getPreference()==0 && lecture.nrTimeLocations()==1?PreferenceLevel.sIntLevelRequired:time.getPreference()),
						min,
						time.getDatePatternName(),
						time.getTimePatternId(),
						time.getDatePatternId(),
						time.getDatePatternPreference()));
			}
			for (RoomLocation room: lecture.roomLocations()) {
				details.addRoom(new RoomInfo(
						room.getName(),
						room.getId(),
						room.getRoomSize(),
						(room.getPreference()==0 && lecture.nrRoomLocations()==lecture.getNrRooms()?PreferenceLevel.sIntLevelRequired:room.getPreference())
						));
			}			
		}
		if (includeConstraints) {
			for (Iterator e=lecture.activeJenrls(assignment).iterator();e.hasNext();) {
				JenrlConstraint jenrl = (JenrlConstraint)e.next();
				Lecture another = (Lecture)jenrl.another(lecture);
				if (!jenrl.isToBeIgnored())
					details.addStudentConflict(new StudentConflictInfo(toJenrlInfo(new org.unitime.timetable.solver.ui.JenrlInfo(solver, jenrl)), createClassAssignmentDetails(context, solver, another, false, false)));
			}
			if (placement!=null) {
				Hashtable infos = org.unitime.timetable.solver.ui.JenrlInfo.getCommitedJenrlInfos(solver, lecture);
    			for (Iterator i2=infos.entrySet().iterator();i2.hasNext();) {
    				Map.Entry entry = (Map.Entry)i2.next();
    				Long assignmentId = (Long)entry.getKey();
    				org.unitime.timetable.solver.ui.JenrlInfo jInfo = (org.unitime.timetable.solver.ui.JenrlInfo)entry.getValue();
    				details.addStudentConflict(new StudentConflictInfo(toJenrlInfo(jInfo), createClassAssignmentDetailsFromAssignment(context, assignmentId, false)));
    			}
			}
			for (Constraint c: lecture.constraints()) {
				if (c instanceof GroupConstraint) {
					GroupConstraint gc = (GroupConstraint)c;
					DistributionInfo dist = new DistributionInfo(); dist.setInfo(toGroupConstraintInfo(new org.unitime.timetable.solver.ui.GroupConstraintInfo(assignment, gc)));
					for (Lecture another: gc.variables()) {
						if (another.equals(lecture)) continue;
						dist.addClass(createClassAssignmentDetails(context, solver, another, false, false));
					}
					details.addDistributionConflict(dist);
				}
				if (c instanceof FlexibleConstraint) {
					FlexibleConstraint gc = (FlexibleConstraint)c;
					DistributionInfo dist = new DistributionInfo(); dist.setInfo(toGroupConstraintInfo(new org.unitime.timetable.solver.ui.GroupConstraintInfo(assignment, gc)));
					for (Lecture another: gc.variables()) {
						if (another.equals(lecture)) continue;
						dist.addClass(createClassAssignmentDetails(context, solver, another, false, false));
					}
					details.addDistributionConflict(dist);
				}
			}
			if (!lecture.getInstructorConstraints().isEmpty() && placement!=null) {
				for (InstructorConstraint ic: lecture.getInstructorConstraints()) {
				    for (Lecture other: ic.variables()) {
				        if (other.equals(lecture) || assignment.getValue(other)==null) continue;
				        int pref = ic.getDistancePreference(placement, assignment.getValue(other));
				        if (pref == PreferenceLevel.sIntLevelNeutral) continue;
				        details.addBtbInstructorConflict(new BtbInstructorInfo(createClassAssignmentDetails(context, solver, other, false, false), pref));
				    }
				}
			}
		}
		return details;
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetailsFromAssignment(SuggestionsContext context, Long assignmentId, boolean includeConstraints) {
		return createClassAssignmentDetailsFromAssignment(context, AssignmentDAO.getInstance().get(assignmentId), includeConstraints);
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetailsFromClass(SuggestionsContext context, org.unitime.timetable.model.Class_ clazz) {
		ClassAssignmentDetails details = new ClassAssignmentDetails();
		details.setClazz(new ClassInfo(
				clazz.getClassLabel(),
				clazz.getUniqueId(),
				clazz.getNbrRooms(),
				PreferenceLevel.sProhibited,
				-1,0,
				clazz.getNotes()
				));
		details.setCanUnassign(false);
		return details;
	}
	
	public static ClassAssignmentDetails createClassAssignmentDetailsFromAssignment(SuggestionsContext context, org.unitime.timetable.model.Assignment assignment, boolean includeConstraints) {
		if (assignment == null) return null;
		ClassAssignmentDetails details = new ClassAssignmentDetails();
		org.unitime.timetable.solver.ui.AssignmentPreferenceInfo assignmentInfo = (org.unitime.timetable.solver.ui.AssignmentPreferenceInfo)assignment.getAssignmentInfo("AssignmentInfo");
		if (!assignment.getRooms().isEmpty()) {
			for (Location room: assignment.getRooms())
				details.setRoom(new RoomInfo(
						room.getLabel(),
						room.getUniqueId(),
						room.getCapacity(),
						(assignmentInfo == null ? 0 : assignmentInfo.getRoomPreference(room.getUniqueId()))
						));
		}
		int length = assignment.getTimePattern().getSlotsPerMtg();
		int breakTime = assignment.getTimePattern().getBreakTime();
		if (assignment.getTimePattern().getType().intValue() == TimePattern.sTypeExactTime) {
			DurationModel dm = assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getDurationModel();
			int minsPerMeeting = dm.getExactTimeMinutesPerMeeting(assignment.getClazz().getSchedulingSubpart().getMinutesPerWk(), assignment.getDatePattern(), assignment.getDays()); 
			length = ExactTimeMins.getNrSlotsPerMtg(minsPerMeeting);
			breakTime = ExactTimeMins.getBreakTime(minsPerMeeting);
		}
		int min = Constants.SLOT_LENGTH_MIN*length-breakTime;
		DatePattern datePattern = assignment.getDatePattern();
		details.setTime(new TimeInfo(
				assignment.getDays(),
				assignment.getStartSlot(),
				(assignmentInfo == null ? 0 : assignmentInfo.getTimePreference()),
				min,
				(datePattern==null?"not set":datePattern.getName()),
				assignment.getTimePattern().getUniqueId(),
				(datePattern==null?null:datePattern.getUniqueId()),
				(assignmentInfo == null ? 0 : assignmentInfo.getDatePatternPref())
				));
		TreeSet<DepartmentalInstructor> instructors = new TreeSet<DepartmentalInstructor>();
		if (ApplicationProperty.TimetableGridUseClassInstructors.isTrue()) {
			if (!ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue() || assignment.getClazz().isDisplayInstructor()) {
				for (Iterator<ClassInstructor> i = assignment.getClazz().getClassInstructors().iterator(); i.hasNext();) {
					ClassInstructor instructor = i.next();
					if (instructor.isLead() || !ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isTrue())
						instructors.add(instructor.getInstructor());
				}
			}
		} else {
			instructors.addAll(assignment.getInstructors());
		}
		if (instructors != null && !instructors.isEmpty()) {
			for (DepartmentalInstructor di: instructors) {
				details.setInstructor(new InstructorInfo(di.getName(context.getInstructorNameFormat()), di.getUniqueId()));
			}
		}
		if (includeConstraints) {
			for (Iterator i=assignment.getConstraintInfoTable("JenrlInfo").entrySet().iterator();i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
				org.unitime.timetable.solver.ui.JenrlInfo info = (org.unitime.timetable.solver.ui.JenrlInfo)entry.getValue();
				org.unitime.timetable.model.Assignment another = null;
				for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
					org.unitime.timetable.model.Assignment x = (org.unitime.timetable.model.Assignment)j.next();
					if (x.getUniqueId().equals(assignment.getUniqueId())) continue;
					another = x; break;
				}
				details.addStudentConflict(new StudentConflictInfo(toJenrlInfo(info), createClassAssignmentDetailsFromAssignment(context, another, false)));
			}
			for (Iterator i=assignment.getConstraintInfoTable("DistributionInfo").entrySet().iterator();i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
				org.unitime.timetable.solver.ui.GroupConstraintInfo info = (org.unitime.timetable.solver.ui.GroupConstraintInfo)entry.getValue();
				DistributionInfo dist = new DistributionInfo(toGroupConstraintInfo(info));
				for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
					org.unitime.timetable.model.Assignment another = (org.unitime.timetable.model.Assignment)j.next();
					if (another.getUniqueId().equals(assignment.getUniqueId())) continue;
					dist.addClass(createClassAssignmentDetailsFromAssignment(context, another, false));
				}
				details.addDistributionConflict(dist);
			}
			for (Iterator i=assignment.getConstraintInfoTable("BtbInstructorInfo").entrySet().iterator();i.hasNext();) {
				Map.Entry entry = (Map.Entry)i.next();
				ConstraintInfo constraint = (ConstraintInfo)entry.getKey();
				BtbInstructorConstraintInfo info = (BtbInstructorConstraintInfo)entry.getValue();
				org.unitime.timetable.model.Assignment another = null;
				for (Iterator j=constraint.getAssignments().iterator();j.hasNext();) {
					org.unitime.timetable.model.Assignment x = (org.unitime.timetable.model.Assignment)j.next();
					if (x.getUniqueId().equals(assignment.getUniqueId())) continue;
					another = x; break;
				}
				details.addBtbInstructorConflict(new BtbInstructorInfo(createClassAssignmentDetailsFromAssignment(context, another, false), info.getPreference()));
			}
		}
		details.setClazz(new ClassInfo(
				assignment.getClassName(),
				assignment.getClassId(),
				assignment.getRooms().size(),
				SolutionGridModel.hardConflicts2pref(assignmentInfo),
				-1,0,
				assignment.getClazz().getNotes()
				));
		details.setCanUnassign(false);
		return details;
	}
	
	public static JenrlInfo toJenrlInfo(org.unitime.timetable.solver.ui.JenrlInfo info) {
		JenrlInfo ret = new JenrlInfo();
		ret.setJenrl((int)Math.round(info.getJenrl()));
		ret.setIsSatisfied(info.isSatisfied());
		ret.setIsHard(info.isHard());
		ret.setIsDistance(info.isDistance());
		ret.setIsFixed(info.isFixed());
		ret.setIsCommited(info.isCommited());
		ret.setIsImportant(info.isImportant());
		ret.setIsInstructor(info.isInstructor());
		ret.setDistance(info.getDistance());
		if (info.hasCurricula())
			for (org.unitime.timetable.solver.ui.JenrlInfo.CurriculumInfo cur: info.getCurricula())
				ret.addCurriculum(new CurriculumInfo(cur.getName(), (int)Math.round(cur.getNrStudents())));
		return ret;
	}
	
	public static GroupConstraintInfo toGroupConstraintInfo(org.unitime.timetable.solver.ui.GroupConstraintInfo info) {
		GroupConstraintInfo ret = new GroupConstraintInfo();
		ret.setPreference(info.getPreference());
		ret.setIsSatisfied(info.isSatisfied());
		ret.setName(info.getName());
		ret.setType(info.getType());
		return ret;
	}
}
