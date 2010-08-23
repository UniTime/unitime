/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.curricula;

import java.util.Set;

import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.solver.curricula.students.CurModel;

import net.sf.cpsolver.ifs.util.DataProperties;

public class CurriculaLastLikeCourseDemands extends CurriculaCourseDemands {

	public CurriculaLastLikeCourseDemands(DataProperties properties) {
		super(properties);
	}
		
	protected void computeTargetShare(CurriculumClassification clasf, CurModel model) {
		for (CurriculumCourse c1: clasf.getCourses()) {
			int x1 = Math.round(clasf.getNrStudents() * c1.getPercShare());
			for (CurriculumCourse c2: clasf.getCourses()) {
				int x2 = Math.round(clasf.getNrStudents() * c2.getPercShare());
				if (c1.getUniqueId() >= c2.getUniqueId()) continue;
				int share = 0;
				Set<WeightedStudentId> s1 = iFallback.getDemands(c1.getCourse());
				Set<WeightedStudentId> s2 = iFallback.getDemands(c2.getCourse());
				if (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty()) {
					int sharedStudents = 0, lastLike = 0;
					for (WeightedStudentId s: s1) {
						if (s.match(clasf)) {
							lastLike++;
							if (s2.contains(s)) sharedStudents++;
						}
					}
					float requested = c1.getPercShare() * clasf.getNrStudents();
					share = Math.round((requested / lastLike) * sharedStudents); 
				} else {
					share = Math.round(c1.getPercShare() * c2.getPercShare() * clasf.getNrStudents());
				}
				CurriculumCourseGroup group = null;
				groups: for (CurriculumCourseGroup g1: c1.getGroups()) {
					for (CurriculumCourseGroup g2: c2.getGroups()) {
						if (g1.equals(g2)) { group = g1; break groups; }
					}
				}
				if (group != null) {
					share = (group.getType() == 0 ? 0 : Math.min(x1, x2));
				}
				model.setTargetShare(c1.getUniqueId(), c2.getUniqueId(), share);
			}
		}
	}
	
}
