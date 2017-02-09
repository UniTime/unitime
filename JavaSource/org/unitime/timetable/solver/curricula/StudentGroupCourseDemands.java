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
package org.unitime.timetable.solver.curricula;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.IdGenerator;
import org.cpsolver.ifs.util.Progress;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.solver.curricula.StudentCourseDemands.NeedsStudentIdGenerator;

/**
 * @author Tomas Muller
 */
public class StudentGroupCourseDemands implements StudentCourseDemands, NeedsStudentIdGenerator {
	protected org.hibernate.Session iHibSession;
	protected Hashtable<Long, Set<WeightedStudentId>> iGroupDemands = new Hashtable<Long, Set<WeightedStudentId>>();
	protected Hashtable<Long, Set<WeightedCourseOffering>> iGroupRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
	protected Long iSessionId = null;
	protected IdGenerator iLastStudentId = null;
	protected boolean iIncludeRealStudents = true;
	
	public StudentGroupCourseDemands(DataProperties properties) {
	}
	
	@Override
	public boolean isMakingUpStudents() { return true; }
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() { return false; }

	@Override
	public boolean isWeightStudentsToFillUpOffering() { return false; }
	
	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	protected Set<WeightedStudentId> load(StudentGroup g) {
		Set<WeightedStudentId> demands = new HashSet<WeightedStudentId>();
		iGroupDemands.put(g.getUniqueId(), demands);
		
		List<StudentGroupReservation> reservations = (List<StudentGroupReservation>)iHibSession.createQuery(
				"from StudentGroupReservation r where r.group.uniqueId = :groupId").setLong("groupId", g.getUniqueId()).setCacheable(true).list();
		
		int realStudents = g.getStudents().size();
		int madeupStudents = 0;
		if (g.getExpectedSize() != null && 2 * realStudents < g.getExpectedSize())
			madeupStudents = g.getExpectedSize() - realStudents;
		if (realStudents + madeupStudents == 0 && !reservations.isEmpty()) {
			for (StudentGroupReservation r: reservations)
				if (r.getLimit() != null && madeupStudents < r.getLimit()) {
					madeupStudents = r.getLimit();
				} else if (r.getLimit() == null) {
					Integer cap = r.getLimitCap();
					if (cap != null && madeupStudents < cap)
						madeupStudents = cap;
				}
		}
		if (realStudents + madeupStudents > 0) {
			float weight = 1.0f;
			if (g.getExpectedSize() != null)
				weight = g.getExpectedSize().floatValue() / (realStudents + madeupStudents);
			for (Student s: g.getStudents()) {
				WeightedStudentId ws = new WeightedStudentId(s);
				ws.setWeight(weight);
				demands.add(ws);
			}
			for (int i = 0; i < madeupStudents; i++) {
				WeightedStudentId ws = new WeightedStudentId(-iLastStudentId.newId());
				ws.setWeight(weight); ws.getGroups().add(new Group(g.getUniqueId(), g.getGroupAbbreviation()));
				demands.add(ws);
			}
		}
		
		for (StudentGroupReservation r: reservations) {
			float weight = 1.0f;
			if (r.getLimit() != null) {
				weight = r.getLimit().floatValue() / (realStudents + madeupStudents);
			} else {
				Integer cap = r.getLimitCap();
				if (cap != null)
					weight = cap.floatValue() / (realStudents + madeupStudents);
			}
			WeightedCourseOffering w = new WeightedCourseOffering(r.getInstructionalOffering().getControllingCourseOffering(), weight);
			for (WeightedStudentId s: demands) {
				Set<WeightedCourseOffering> offerings = iGroupRequests.get(s.getStudentId());
				if (offerings == null) {
					offerings = new HashSet<WeightedCourseOffering>();
					iGroupRequests.put(s.getStudentId(), offerings);
				}
				offerings.add(w);
			}
		}
		
		return demands;
	}
	
	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		if (!course.isIsControl()) return null;
		Set<WeightedStudentId> ret = new HashSet<WeightedStudentId>();
		for (Reservation r: course.getInstructionalOffering().getReservations()) {
			if (r instanceof StudentGroupReservation) {
				StudentGroupReservation gr = (StudentGroupReservation)r;
				Set<WeightedStudentId> demands = iGroupDemands.get(gr.getGroup().getUniqueId());
				if (demands == null)
					demands = load(gr.getGroup());
				float weight = 1.0f;
				if (r.getLimit() != null) {
					weight = r.getLimit().floatValue() / demands.size();
				} else {
					Integer cap = r.getLimitCap();
					if (cap != null)
						weight = cap.floatValue() / demands.size();
				}
				if (Math.abs(weight - 1.0f) > 0.001f) {
					for (WeightedStudentId student: demands)
						ret.add(new WeightedStudentId(student, weight));
				} else {
					ret.addAll(demands);
				}
			}
		}
		return ret;
	}
	
	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		return iGroupRequests.get(studentId);
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		return null;
	}
	
	@Override
	public void setStudentIdGenerator(IdGenerator generator) {
		iLastStudentId = generator;
	}
}
