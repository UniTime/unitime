/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
