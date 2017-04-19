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
package org.unitime.timetable.solver.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Solution;


/**
 * @author Tomas Muller
 */
public class SolutionUnassignedClassesModel extends UnassignedClassesModel {
	
	private static final long serialVersionUID = 8222974077941239586L;

	public SolutionUnassignedClassesModel(Collection<Solution> solutions, org.hibernate.Session hibSession, String instructorFormat, String... prefix) {
		super();
		boolean checkDisplay = ApplicationProperty.TimetableGridUseClassInstructorsCheckClassDisplayInstructors.isTrue();
		boolean checkLead = ApplicationProperty.TimetableGridUseClassInstructorsCheckLead.isTrue();
		for (Iterator i=solutions.iterator();i.hasNext();) {
			Solution solution = (Solution)i.next();
			for (Iterator j=solution.getOwner().getNotAssignedClasses(solution).iterator();j.hasNext();) {
				Class_ clazz = (Class_)j.next();
				String name = clazz.getClassLabel(ApplicationProperty.SolverShowClassSufix.isTrue());
				if (prefix != null && prefix.length > 0) {
					boolean hasPrefix = false;
					for (String p: prefix)
						if (p == null || name.startsWith(p)) { hasPrefix = true; break; }
					if (!hasPrefix) continue;
				}
				List<String> instructors = new ArrayList<String>();
				if (!checkDisplay || clazz.isDisplayInstructor()) {
					for (ClassInstructor ci: clazz.getClassInstructors()) {
						if (ci.isLead() || !checkLead)
							instructors.add(ci.getInstructor().getName(instructorFormat));
					}
				}	
				int nrStudents = ((Number)hibSession.
						createQuery("select count(s) from StudentEnrollment as s where s.clazz.uniqueId=:classId and s.solution.uniqueId=:solutionId").
						setLong("classId",clazz.getUniqueId().longValue()).
						setInteger("solutionId",solution.getUniqueId().intValue()).
						uniqueResult()).intValue();
				rows().add(new UnassignedClassRow(clazz.getUniqueId(), name, instructors, nrStudents, null, clazz));
			}
		}
	}
}
