/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

/**
 * @author Tomas Muller
 */
public class CurStudent {
	private static DecimalFormat sDF = new DecimalFormat("0.###");
	private Set<CurCourse> iCourses = new HashSet<CurCourse>();
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
	
	public Set<CurCourse> getCourses() {
		return iCourses;
	}
	
	public String toString() {
		return getStudentId() + (getWeight() != 1.f ? "@" + sDF.format(getWeight()): "" );
	}
	
	
}
