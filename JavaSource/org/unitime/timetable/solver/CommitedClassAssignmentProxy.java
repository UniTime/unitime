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
import java.util.Hashtable;
import java.util.Iterator;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
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
    
}
