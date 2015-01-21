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
package org.unitime.timetable.solver.curricula.students;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.context.AbstractClassWithContext;
import org.cpsolver.ifs.assignment.context.AssignmentContext;
import org.cpsolver.ifs.model.Model;


/**
 * @author Tomas Muller
 */
public class CurStudent extends AbstractClassWithContext<CurVariable, CurValue, CurStudent.CurStudentContext> {
	private static DecimalFormat sDF = new DecimalFormat("0.###");
	private Long iStudentId;
	private double iWeight;
	
	public CurStudent(Long studentId, double weight) {
		iStudentId = studentId;
		iWeight = weight;
	}
	
	public double getWeight() {
		return iWeight;
	}
	
	public void setWeight(double weight) {
		iWeight = weight;
	}
	
	public Long getStudentId() {
		return iStudentId;
	}
	
	public Set<CurCourse> getCourses(Assignment<CurVariable, CurValue> assignment) {
		return getContext(assignment).getCourses();
	}
	
	public String toString() {
		return getStudentId() + (getWeight() != 1.f ? "@" + sDF.format(getWeight()): "" );
	}
	
	public class CurStudentContext implements AssignmentContext {
		private Set<CurCourse> iCourses = new HashSet<CurCourse>();
		
		public CurStudentContext(Assignment<CurVariable, CurValue> assignment) {
		}
		
		public Set<CurCourse> getCourses() {
			return iCourses;
		}
	}

	@Override
	public CurStudentContext createAssignmentContext(Assignment<CurVariable, CurValue> assignment) {
		return new CurStudentContext(assignment);
	}
	
	public void setModel(CurModel model) {
		setAssignmentContextReference(model.createReference(this));
	}

	@Override
	public Model<CurVariable, CurValue> getModel() {
		return null;
	}
}
