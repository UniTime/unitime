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
