/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
