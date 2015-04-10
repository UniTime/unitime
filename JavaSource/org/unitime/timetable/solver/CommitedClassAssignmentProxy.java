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

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public class CommitedClassAssignmentProxy implements ClassAssignmentProxy {
	/*
	private HashSet iCachedCommitedSubjectsAndOwners =  new HashSet();
	private Hashtable iCachedCommitedAssignments = new Hashtable();
	*/
	private static AssignmentPreferenceInfo sCommitedAssignmentPreferenceInfo = new AssignmentPreferenceInfo();
	
	public CommitedClassAssignmentProxy() {}
	
	public Assignment getAssignment(Long classId) throws Exception {
		return getAssignment((new Class_DAO()).get(classId));
	}
	
	public Assignment getAssignment(Class_ clazz) throws Exception {
		return clazz.getCommittedAssignment();
		/*
		Iterator i=null;
		try {
			i = clazz.getAssignments().iterator();
		} catch (ObjectNotFoundException e) {
			Debug.error("Exception "+e.getMessage()+" seen for "+clazz);
    		new _RootDAO().getSession().refresh(clazz);
   			i = clazz.getAssignments().iterator();
		}
		while (i.hasNext()) {
			Assignment a = (Assignment)i.next();
			if (a.getSolution().isCommited().booleanValue()) return a;
		}
		return null;
		*/
		/*
		Department department = clazz.getManagingDept();
		if (department==null) return null;
    	String subjectName = clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectAreaAbbv();
    	if (iCachedCommitedSubjectsAndOwners.add(subjectName+"-"+department.getUniqueId())) {
        	Query q = (new AssignmentDAO()).getSession().createQuery(
    				"select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o where " +
    				"a.solution.commited=true and " +
    				":departmentId in a.solution.owner.departments.uniqueId and " +
    				"o.isControl=true and o.subjectAreaAbbv=:subjectName");
        	q.setLong("departmentId",department.getUniqueId().longValue());
    		q.setString("subjectName",subjectName);
    		for (Iterator i=q.list().iterator();i.hasNext();) {
    			Assignment a = (Assignment)i.next();
    			iCachedCommitedAssignments.put(a.getClassId(),a);
    		}
    	}
		return (Assignment)iCachedCommitedAssignments.get(clazz.getUniqueId());
		*/
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) throws Exception {
		return sCommitedAssignmentPreferenceInfo;
	}
    
    public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception {
    	return sCommitedAssignmentPreferenceInfo;
    }	
    
	public Hashtable getAssignmentTable(Collection classesOrClassIds) throws Exception {
		Hashtable assignments = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Assignment assignment = (classOrClassId instanceof Class_ ? getAssignment((Class_)classOrClassId) : getAssignment((Long)classOrClassId));
			if (assignment!=null)
				assignments.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, assignment);
		}
		return assignments;
	}
	
	public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) throws Exception {
		Hashtable infos = new Hashtable();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			AssignmentPreferenceInfo info = (classOrClassId instanceof Class_ ? getAssignmentInfo((Class_)classOrClassId) : getAssignmentInfo((Long)classOrClassId));
			if (info!=null)
				infos.put(classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId, info);
		}
		return infos;
	}

	@Override
	public Set<Assignment> getConflicts(Class_ clazz) throws Exception {
		if (clazz == null || clazz.isCancelled()) return null;
		Assignment assignment = getAssignment(clazz);
		if (assignment == null) return null;
		Set<Assignment> conflicts = new HashSet<Assignment>();
		if (assignment.getRooms() != null)
			for (Location room : assignment.getRooms()) {
				if (!room.isIgnoreRoomCheck()) {
					for (Assignment a : room.getCommitedAssignments())
						if (!assignment.equals(a) && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareRoom(a.getClazz()))
							conflicts.add(a);
            	}
            }
		
		if (clazz.getClassInstructors() != null)
			for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (!instructor.isLead()) continue;
				for (DepartmentalInstructor di: DepartmentalInstructor.getAllForInstructor(instructor.getInstructor())) {
					for (ClassInstructor ci : di.getClasses()) {
	            		if (ci.equals(instructor)) continue;
	            		Assignment a = getAssignment(ci.getClassInstructing());
	            		if (a != null && !a.getClazz().isCancelled() && assignment.overlaps(a) && !clazz.canShareInstructor(a.getClazz()))
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
    
}
