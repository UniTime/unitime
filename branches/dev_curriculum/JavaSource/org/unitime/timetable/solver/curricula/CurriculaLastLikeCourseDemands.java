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

import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourseGroup;

import net.sf.cpsolver.ifs.util.DataProperties;

public class CurriculaLastLikeCourseDemands extends CurriculaCourseDemands {

	public CurriculaLastLikeCourseDemands(DataProperties properties) {
		super(properties);
	}
		
	protected void computeTargetShare(CurriculumClassification clasf, List<Bucket> buckets) {
		for (Bucket c1: buckets) {
			for (Bucket c2: buckets) {
				if (c1.getCourse().getUniqueId() >= c2.getCourse().getUniqueId()) continue;
				int share = 0;
				Set<WeightedStudentId> s1 = iLastLikeStudentCourseDemands.getDemands(c1.getCourse().getCourse());
				Set<WeightedStudentId> s2 = iLastLikeStudentCourseDemands.getDemands(c2.getCourse().getCourse());
				if (s1 != null && !s1.isEmpty() && s2 != null && !s2.isEmpty()) {
					int sharedStudents = 0;
					for (WeightedStudentId s: s1)
						if (s2.contains(s)) sharedStudents++;
					float planned = c1.getCourse().getPercShare() * clasf.getNrStudents();
					int lastLike = s1.size();
					share = Math.round((planned / lastLike) * sharedStudents); 
				} else {
					share = Math.round(c1.getCourse().getPercShare() * c2.getCourse().getPercShare() * clasf.getNrStudents());
				}
				CurriculumCourseGroup group = null;
				groups: for (CurriculumCourseGroup g1: c1.getCourse().getGroups()) {
					for (CurriculumCourseGroup g2: c2.getCourse().getGroups()) {
						if (g1.equals(g2)) { group = g1; break groups; }
					}
				}
				if (group != null) {
					share = (group.getType() == 0 ? 0 : Math.min(c1.getStudents().size(), c2.getStudents().size()));
				}
				c1.setTargetShare(c2, share);
				c2.setTargetShare(c1, share);
			}
		}
	}
	
}
