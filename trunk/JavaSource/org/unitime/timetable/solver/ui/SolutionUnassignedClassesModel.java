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
package org.unitime.timetable.solver.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Solution;


/**
 * @author Tomas Muller
 */
public class SolutionUnassignedClassesModel extends UnassignedClassesModel {
	
	private static final long serialVersionUID = 8222974077941239586L;

	public SolutionUnassignedClassesModel(Collection solutions, org.hibernate.Session hibSession, String instructorFormat, String prefix) {
		super();
		for (Iterator i=solutions.iterator();i.hasNext();) {
			Solution solution = (Solution)i.next();
			for (Iterator j=solution.getOwner().getNotAssignedClasses(solution).iterator();j.hasNext();) {
				Class_ clazz = (Class_)j.next();
				String name = clazz.getClassLabel();
				if (prefix != null && !name.startsWith(prefix)) continue;
				String onClick = "showGwtDialog('Suggestions', 'suggestions.do?id="+clazz.getUniqueId()+"&op=Reset','900','90%');";
				List<DepartmentalInstructor> leads = clazz.getLeadInstructors();
				StringBuffer leadsSb = new StringBuffer();
				for (Iterator<DepartmentalInstructor> e=leads.iterator();e.hasNext();) {
					DepartmentalInstructor instructor = (DepartmentalInstructor)e.next();
					leadsSb.append(instructor.getName(instructorFormat));
					if (e.hasNext()) leadsSb.append(";");
				}
				String instructorName = leadsSb.toString();
				int nrStudents = ((Number)hibSession.
						createQuery("select count(s) from StudentEnrollment as s where s.clazz.uniqueId=:classId and s.solution.uniqueId=:solutionId").
						setLong("classId",clazz.getUniqueId().longValue()).
						setInteger("solutionId",solution.getUniqueId().intValue()).
						uniqueResult()).intValue();
				rows().addElement(new UnassignedClassRow(onClick, name, instructorName, nrStudents, null, clazz));
			}
		}
	}
}
