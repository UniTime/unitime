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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Curriculum;
import org.unitime.timetable.model.CurriculumClassification;
import org.unitime.timetable.model.CurriculumCourse;
import org.unitime.timetable.model.CurriculumCourseGroup;
import org.unitime.timetable.model.Session;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ToolBox;

public class CurriculaCourseDemands implements StudentCourseDemands {
	private static Log sLog = LogFactory.getLog(CurriculaCourseDemands.class);
	private Hashtable<Long, Set<WeightedStudentId>> iDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	private long lastStudentId = -1;
	private int iGenMoveLastIndexIn = 0;
	private int iGenMoveLastIndexOut = 0;
	protected LastLikeStudentCourseDemands iLastLikeStudentCourseDemands = null;

	public CurriculaCourseDemands(DataProperties properties) {
		iLastLikeStudentCourseDemands = new LastLikeStudentCourseDemands(properties);
	}

	public void init(org.hibernate.Session hibSession, Progress progress, Session session) {
		iLastLikeStudentCourseDemands.init(hibSession, progress, session);

		List<Curriculum> curricula = hibSession.createQuery(
				"select c from Curriculum c where c.academicArea.session.uniqueId = :sessionId")
				.setLong("sessionId", session.getUniqueId()).list();

		progress.setPhase("Loading curricula", curricula.size());
		for (Curriculum curriculum: curricula) {
			for (CurriculumClassification clasf: curriculum.getClassifications()) {
				init(clasf);
			}
			progress.incProgress();
		}
		
		if (iDemands.isEmpty()) {
			progress.warn("There are no curricula, using last-like course demands instead.");
		}
	}
	
	private void init(CurriculumClassification clasf) {
		if (clasf.getNrStudents() <= 0) return;
		
		sLog.info("Processing " + clasf.getCurriculum().getAbbv() + " " + clasf.getName() + " ... (" + clasf.getNrStudents() + " students, " + clasf.getCourses().size() + " courses)");
		
		// Makeup students
		List<WeightedStudentId> students = new ArrayList<WeightedStudentId>();
		for (int i = 0; i < clasf.getNrStudents(); i++) {
			students.add(new WeightedStudentId(lastStudentId--));
		}
		
		// Generate buckets
		List<Bucket> buckets = new ArrayList<Bucket>();
		for (CurriculumCourse course: clasf.getCourses()) {
			int nrStudents = Math.round(clasf.getNrStudents() * course.getPercShare());
			if (nrStudents <= 0) continue;
			Bucket bucket = new Bucket(course);
			if (nrStudents >= students.size()) {
				bucket.getStudents().addAll(students);
			} else {
				bucket.getStudents().addAll(ToolBox.subSet(students, 0.0, nrStudents));
			}
			buckets.add(bucket);
		}
		
		//  Compute target share
		computeTargetShare(clasf, buckets);
		
		List<Bucket> halfFullBuckets = new ArrayList<Bucket>();
		for (Bucket bucket: buckets) {
			if (bucket.getStudents().size() == 0) continue;
			if (bucket.getStudents().size() == students.size()) continue;
			halfFullBuckets.add(bucket);
		}
		
		// Run simple local search
		//TODO: Do something much more elaborate, e.g., try to move the worst student around
		int value = value(buckets);
		int idle = 0, it = 0;
		sLog.info("  -- initial value: " + value);
		while (!halfFullBuckets.isEmpty() && value > 0 && idle < 1000) {
			Move move = generateMove(halfFullBuckets, students);
			move.perform();
			int newValue = value(buckets);
			if (newValue < value) {
				value = newValue;
				idle = 0;
			} else if (newValue == value) {
			} else {
				move.undo();
			}
			it++; idle++;
		}
		sLog.info("  -- final value: " + value);
		
		// Save results
		for (Bucket bucket: buckets) {
			Set<WeightedStudentId> courseStudents = iDemands.get(bucket.getCourse().getCourse().getUniqueId());
			if (courseStudents == null) {
				courseStudents = new HashSet<WeightedStudentId>();
				iDemands.put(bucket.getCourse().getCourse().getUniqueId(), courseStudents);
			}
			courseStudents.addAll(bucket.getStudents());
		}
	}
	
	protected void computeTargetShare(CurriculumClassification clasf, List<Bucket> buckets) {
		for (Bucket c1: buckets) {
			for (Bucket c2: buckets) {
				if (c1.getCourse().getUniqueId() >= c2.getCourse().getUniqueId()) continue;
				int share = Math.round(c1.getCourse().getPercShare() * c2.getCourse().getPercShare() * clasf.getNrStudents());
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
	
	protected int value(List<Bucket> buckets) {
		int value = 0;
		for (Bucket c1: buckets) {
			for (Bucket c2: buckets) {
				if (c1.getCourse().getUniqueId() >= c2.getCourse().getUniqueId()) continue;
				int bucketValue = Math.abs(c1.share(c2) - c1.getTargetShare(c2));
				value += bucketValue;
			}
		}
		return value;
	}
	
	protected Move generateMove(List<Bucket> adepts, List<WeightedStudentId> students) {
		Bucket bucket = ToolBox.random(adepts);
		WeightedStudentId studentIn = null, studentOut = null;
		int idx = ToolBox.random(students.size());
		if (bucket.getStudents().contains(students.get(idx))) {
			studentOut = students.get(idx);
		} else {
			studentIn = students.get(idx);
		}
		idx = ToolBox.random(students.size());
		while (true) {
			WeightedStudentId student = students.get(idx);
			idx = (idx + 1) % students.size();
			if (bucket.getStudents().contains(student)) {
				if (studentOut == null) {
					studentOut = student;
					break;
				}
			} else {
				if (studentIn == null) {
					studentIn = student;
					break;
				}
			}
		}
		return new Swap(bucket, studentIn, studentOut);
	}

	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		if (iDemands.isEmpty()) return iLastLikeStudentCourseDemands.getDemands(course);
		return iDemands.get(course.getUniqueId());
	}

	protected class Bucket {
		private CurriculumCourse iCourse;
		private Set<WeightedStudentId> iStudents = new HashSet<WeightedStudentId>();
		private Hashtable<Bucket, Integer> iTargetShare = new Hashtable<Bucket, Integer>();
		
		protected Bucket(CurriculumCourse course) {
			iCourse = course;
		}
	
		public Set<WeightedStudentId> getStudents() { return iStudents; }
		public CurriculumCourse getCourse() { return iCourse; }
		public int share(Bucket bucket) {
			int share = 0;
			for (WeightedStudentId s: getStudents())
				if (bucket.getStudents().contains(s)) share++;
			return share;
		}
		
		public void setTargetShare(Bucket bucket, int targetShare) {
			iTargetShare.put(bucket, targetShare);
		}
		
		public int getTargetShare(Bucket bucket) {
			Integer targetShare = iTargetShare.get(bucket);
			return (targetShare == null ? 0 : targetShare);
		}
	}
	
	protected interface Move {
		public void perform();
		public void undo();
	}
	
	protected class Swap implements Move {
		private Bucket iBucket;
		private WeightedStudentId iStudentIn, iStudentOut;
		
		protected Swap(Bucket bucket, WeightedStudentId studentIn, WeightedStudentId studentOut) {
			iBucket = bucket;
			iStudentIn = studentIn;
			iStudentOut = studentOut;
		}
		
		public void perform() {
			iBucket.getStudents().add(iStudentIn);
			iBucket.getStudents().remove(iStudentOut);
		}
		
		public void undo() {
			iBucket.getStudents().remove(iStudentIn);
			iBucket.getStudents().add(iStudentOut);
		}
	}
}
