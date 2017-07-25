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
package org.unitime.timetable.solver;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.hibernate.ObjectNotFoundException;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlockComparator;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.course.ui.ClassTimeInfo;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class SolutionClassAssignmentProxy extends CommitedClassAssignmentProxy {
	private Set<Long> iSolutionIds = new HashSet<Long>();
	private Hashtable<Long, Long> iDepartmentIds = new Hashtable<Long, Long>();
	
	public SolutionClassAssignmentProxy(Collection solutionIds) {
		super();
		for (Iterator i=solutionIds.iterator();i.hasNext();) {
			Solution solution = (new SolutionDAO()).get((Long)i.next());
			if (solution==null) continue;
			iSolutionIds.add(solution.getUniqueId());
			for (Iterator j=solution.getOwner().getDepartments().iterator();j.hasNext();)
				iDepartmentIds.put(((Department)j.next()).getUniqueId(), solution.getUniqueId());
		}
	}
	
	public SolutionClassAssignmentProxy(Solution solution) {
		super();
		iSolutionIds.add(solution.getUniqueId());
		for (Iterator j=solution.getOwner().getDepartments().iterator();j.hasNext();)
			iDepartmentIds.put(((Department)j.next()).getUniqueId(), solution.getUniqueId());
	}

	public Long getSolutionId(Class_ clazz) {
		Department department = clazz.getManagingDept();
		if (department==null) return null;
		return (Long)iDepartmentIds.get(department.getUniqueId());
	}
	
    public Assignment getAssignment(Class_ clazz) {
        Long solutionId = getSolutionId(clazz);
		if (solutionId==null) return super.getAssignment(clazz);
        Iterator i = null;
        try {
            i = clazz.getAssignments().iterator();
        } catch (ObjectNotFoundException e) {
            new _RootDAO().getSession().refresh(clazz);
            i = clazz.getAssignments().iterator();
        }
        while (i.hasNext()) {
			Assignment a = (Assignment)i.next();
			if (solutionId.equals(a.getSolution().getUniqueId())) return a;
		}
		return null;
    }
 
    public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) {
        Long solutionId = getSolutionId(clazz);
		if (solutionId==null) return super.getAssignmentInfo(clazz);
    	Assignment a = getAssignment(clazz);
    	return (a==null?null:(AssignmentPreferenceInfo)a.getAssignmentInfo("AssignmentInfo"));
    }
    
    public Set<Long> getSolutionIds() {
    	return iSolutionIds;
    }
    
    public boolean equals(Collection<Long> solutionIds) {
    	if (solutionIds.size()!=iSolutionIds.size()) return false;
    	for (Iterator<Long> i=solutionIds.iterator();i.hasNext();) {
            Long solutionId = i.next();
    		if (!iSolutionIds.contains(solutionId)) return false;
    	}
    	return true;
    }
    
	@Override
	public boolean hasConflicts(Long offeringId) {
		InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(offeringId);
		if (offering == null || offering.isNotOffered()) return false;
		
		for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
			for (SchedulingSubpart subpart: config.getSchedulingSubparts())
				for (Class_ clazz: subpart.getClasses()) {
					if (clazz.isCancelled()) continue;
					Assignment assignment = getAssignment(clazz);
					if (assignment == null) continue;
					if (assignment.getRooms() != null)
						for (Location room : assignment.getRooms()) {
							if (!room.isIgnoreRoomCheck()) {
								for (Assignment a : room.getAssignments(iSolutionIds))
									if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
										return true;
			            	}
			            }
					
					if (clazz.getClassInstructors() != null)
						for (ClassInstructor instructor: clazz.getClassInstructors()) {
							if (!instructor.isLead()) continue;
							for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor())) {
								for (ClassInstructor ci : di.getClasses()) {
									if (ci.equals(instructor) || ci.getClassInstructing().equals(clazz) || !ci.isLead()) continue;
				            		Assignment a = getAssignment(ci.getClassInstructing());
				            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareInstructor(a.getClazz()))
				            			return true;
				            	}
			            	}
							if (instructor.getInstructor().getExternalUniqueId() != null) {
								for (Class_ c: (List<Class_>)Class_DAO.getInstance().getSession().createQuery(
									"select e.clazz from StudentClassEnrollment e where e.student.externalUniqueId = :externalId and e.student.session.uniqueId = :sessionId")
									.setLong("sessionId", instructor.getInstructor().getDepartment().getSessionId())
									.setString("externalId", instructor.getInstructor().getExternalUniqueId())
									.setCacheable(true).list()) {
									Assignment a = getAssignment(c);
				            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a)) return true;
								}
							}
						}
					
			        Class_ parent = clazz.getParentClass();
			        while (parent!=null) {
			        	Assignment a = getAssignment(parent);
			        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
			        		return true;
			        	parent = parent.getParentClass();
			        }
			        
			        for (Iterator<SchedulingSubpart> i = clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator(); i.hasNext();) {
			        	SchedulingSubpart ss = i.next();
			        	if (ss.getClasses().size() == 1) {
			        		Class_ child = ss.getClasses().iterator().next();
			        		if (clazz.equals(child)) continue;
			        		Assignment a = getAssignment(child);
			        		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
			        			return true;
			        	}
			        }
				}
		
		if (RoomAvailability.getInstance() != null) {
 			boolean changePast = ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue();
 			boolean ignorePast = ApplicationProperty.ClassAssignmentIgnorePastMeetings.isTrue();
        	Date[] bounds = DatePattern.getBounds(offering.getSessionId());
 			Calendar cal = Calendar.getInstance(Locale.US);
    		cal.setTime(new Date());
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		Date today = cal.getTime();
			for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
				for (SchedulingSubpart subpart: config.getSchedulingSubparts())
					for (Class_ clazz: subpart.getClasses()) {
						Assignment assignment = getAssignment(clazz);
						if (assignment != null && assignment.getRooms() != null && !assignment.getRooms().isEmpty()) {
				    		ClassTimeInfo period = new ClassTimeInfo(assignment);
				    		for (Location room : assignment.getRooms()) {
								if (!room.isIgnoreRoomCheck()) {
						    		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
						                    room.getUniqueId(),
						                    bounds[0], bounds[1], 
						                    RoomAvailabilityInterface.sClassType);
						    		if (times != null && !times.isEmpty()) {
						    			Collection<TimeBlock> timesToCheck = null;
						    			if (!changePast || ignorePast) {
						        			timesToCheck = new Vector();
						        			for (TimeBlock time: times) {
						        				if (!time.getEndTime().before(today))
						        					timesToCheck.add(time);
						        			}
						        		} else {
						        			timesToCheck = times;
						        		}
						        		if (period.overlaps(timesToCheck) != null) return true;
						    		}
								}
				    		}
						}
					}
		}
		
		return false;
	}
    
	@Override
	public Set<Assignment> getConflicts(Long classId) {
		if (classId == null) return null;
		Class_ clazz = Class_DAO.getInstance().get(classId);
		if (clazz == null || clazz.isCancelled()) return null;
		Assignment assignment = getAssignment(clazz);
		if (assignment == null) return null;
		Set<Assignment> conflicts = new HashSet<Assignment>();
		if (assignment.getRooms() != null)
			for (Location room : assignment.getRooms()) {
				if (!room.isIgnoreRoomCheck()) {
					for (Assignment a : room.getAssignments(iSolutionIds))
						if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
							conflicts.add(a);
            	}
            }
		
		if (clazz.getClassInstructors() != null)
			for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (!instructor.isLead()) continue;
				for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor())) {
					for (ClassInstructor ci : di.getClasses()) {
						if (ci.equals(instructor) || ci.getClassInstructing().equals(clazz) || !ci.isLead()) continue;
	            		Assignment a = getAssignment(ci.getClassInstructing());
	            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareInstructor(a.getClazz()))
	            			conflicts.add(a);
	            	}
            	}
				if (instructor.getInstructor().getExternalUniqueId() != null) {
					for (Class_ c: (List<Class_>)Class_DAO.getInstance().getSession().createQuery(
						"select e.clazz from StudentClassEnrollment e where e.student.externalUniqueId = :externalId and e.student.session.uniqueId = :sessionId")
						.setLong("sessionId", instructor.getInstructor().getDepartment().getSessionId())
						.setString("externalId", instructor.getInstructor().getExternalUniqueId())
						.setCacheable(true).list()) {
						Assignment a = getAssignment(c);
	            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
	            			conflicts.add(a);
					}
				}
			}
		
        Class_ parent = clazz.getParentClass();
        while (parent!=null) {
        	Assignment a = getAssignment(parent);
        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
    			conflicts.add(a);
        	parent = parent.getParentClass();
        }
        
        Queue<Class_> children = new LinkedList(clazz.getChildClasses());
        Class_ child = null;
        while ((child=children.poll())!=null) {
        	Assignment a = getAssignment(child);
        	if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
    			conflicts.add(a);
        	if (!child.getChildClasses().isEmpty())
        		children.addAll(child.getChildClasses());
        }
        
        for (Iterator<SchedulingSubpart> i = clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts().iterator(); i.hasNext();) {
        	SchedulingSubpart ss = i.next();
        	if (ss.getClasses().size() == 1) {
        		child = ss.getClasses().iterator().next();
        		if (clazz.equals(child)) continue;
        		Assignment a = getAssignment(child);
        		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a))
        			conflicts.add(a);
        	}
        }
        
        return conflicts;
	}
	
	@Override
	public Set<TimeBlock> getConflictingTimeBlocks(Long classId) {
		if (classId == null) return null;
		Class_ clazz = Class_DAO.getInstance().get(classId);
		if (clazz == null || clazz.isCancelled()) return null;
        Long solutionId = getSolutionId(clazz);
		if (solutionId==null) return super.getConflictingTimeBlocks(classId);
		
		Set<TimeBlock> conflicts = new TreeSet<TimeBlock>(new TimeBlockComparator());
		Assignment assignment = getAssignment(clazz);
		if (assignment != null && assignment.getRooms() != null && !assignment.getRooms().isEmpty() && RoomAvailability.getInstance() != null) {
        	Date[] bounds = DatePattern.getBounds(clazz.getSessionId());
 			boolean changePast = ApplicationProperty.ClassAssignmentChangePastMeetings.isTrue();
 			boolean ignorePast = ApplicationProperty.ClassAssignmentIgnorePastMeetings.isTrue();
 			Calendar cal = Calendar.getInstance(Locale.US);
    		cal.setTime(new Date());
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		Date today = cal.getTime();
    		ClassTimeInfo period = new ClassTimeInfo(assignment);
    		
			for (Location room : assignment.getRooms()) {
				if (!room.isIgnoreRoomCheck()) {
		    		Collection<TimeBlock> times = RoomAvailability.getInstance().getRoomAvailability(
		                    room.getUniqueId(),
		                    bounds[0], bounds[1], 
		                    RoomAvailabilityInterface.sClassType);
		    		if (times != null && !times.isEmpty()) {
		    			Collection<TimeBlock> timesToCheck = null;
		    			if (!changePast || ignorePast) {
		        			timesToCheck = new Vector();
		        			for (TimeBlock time: times) {
		        				if (!time.getEndTime().before(today))
		        					timesToCheck.add(time);
		        			}
		        		} else {
		        			timesToCheck = times;
		        		}
		        		List<TimeBlock> overlaps = period.allOverlaps(timesToCheck);
		        		if (overlaps != null)
		    				conflicts.addAll(overlaps);
		    		}
				}
    		}
        }
		
		return conflicts;
	}
}
