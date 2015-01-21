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
import java.util.Set;

import org.hibernate.ObjectNotFoundException;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;

/**
 * @author Tomas Muller
 */
public class SolutionClassAssignmentProxy extends CommitedClassAssignmentProxy {
	private Set iSolutionIds = new HashSet();
	private Hashtable iDepartmentIds = new Hashtable();
	/*
	private Hashtable iCachedAssignments = new Hashtable();
	private HashSet iCachedSubjects =  new HashSet();
	*/
	
	
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
	
    public Assignment getAssignment(Class_ clazz) throws Exception {
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
		/*
    	String subjectName = clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectAreaAbbv();
    	if (iCachedSubjects.add(subjectName+"."+solutionId)) {
        	Query q = (new AssignmentDAO()).getSession().createQuery(
    				"select distinct a from Assignment as a inner join a.clazz.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as o where " +
    				"a.solution.uniqueId=:solutionId and " +
    				"o.isControl=true and o.subjectAreaAbbv=:subjectName");
    		q.setLong("solutionId",solutionId);
    		q.setString("subjectName",subjectName);
    		for (Iterator i=q.list().iterator();i.hasNext();) {
    			Assignment a = (Assignment)i.next();
    			a.getAssignmentInfo("AssignmentInfo"); //force loading assignment info
    			iCachedAssignments.put(a.getClassId(),a);
    		}
    	}
		return (Assignment)iCachedAssignments.get(clazz.getUniqueId());
		*/
    }
 
    public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception {
        Long solutionId = getSolutionId(clazz);
		if (solutionId==null) return super.getAssignmentInfo(clazz);
    	Assignment a = getAssignment(clazz);
    	return (a==null?null:(AssignmentPreferenceInfo)a.getAssignmentInfo("AssignmentInfo"));
    }
    
    public Set getSolutionIds() {
    	return iSolutionIds;
    }
    
    public boolean equals(Collection solutionIds) {
    	if (solutionIds.size()!=iSolutionIds.size()) return false;
    	for (Iterator i=solutionIds.iterator();i.hasNext();) {
            Long solutionId = (Long)i.next();
    		if (!iSolutionIds.contains(solutionId)) return false;
    	}
    	return true;
    }
}
