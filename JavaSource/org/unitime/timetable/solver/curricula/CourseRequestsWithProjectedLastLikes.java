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
import org.cpsolver.ifs.util.Progress;
import org.hibernate.Session;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;

/**
 * @author Tomas Muller
 */
public class CourseRequestsWithProjectedLastLikes extends ProjectedStudentCourseDemands {
	private StudentCourseRequests iCouseRequests;
	private Hashtable<String,Hashtable<String,Hashtable<String, Integer>>> iAreaClasfMajor2LastLike = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
	private Hashtable<String,Hashtable<String,Hashtable<String, Integer>>> iAreaClasfMajor2Real = new Hashtable<String, Hashtable<String,Hashtable<String,Integer>>>();
	
	public CourseRequestsWithProjectedLastLikes(DataProperties properties) {
		super(properties);
		iCouseRequests = new StudentCourseRequests(properties);
	}

	@Override
	public void init(Session hibSession, Progress progress, org.unitime.timetable.model.Session session, Collection<InstructionalOffering> offerings) {
		iCouseRequests.init(hibSession, progress, session, offerings);
		super.init(hibSession, progress, session, offerings);
		
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicAreaAbbreviation, m.code, f.code, count(distinct s) from LastLikeCourseDemand x inner join x.student s " +
				"inner join s.areaClasfMajors ac inner join ac.academicClassification f inner join ac.academicArea a " +
				"inner join ac.major m where x.subjectArea.session.uniqueId = :sessionId " +
				"group by a.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", session.getUniqueId())
				.setCacheable(true).list()) {
			String area = (String)o[0];
			String major = (String)o[1];
			String clasf = (String)o[2];
			int students = ((Number)o[3]).intValue();
			Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = iAreaClasfMajor2LastLike.get(area);
			if (clasfMajor2ll == null) {
				clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
				iAreaClasfMajor2LastLike.put(area, clasfMajor2ll);
			}
			Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf);
			if (major2ll == null) {
				major2ll = new Hashtable<String, Integer>();
				clasfMajor2ll.put(clasf, major2ll);
			}
			major2ll.put(major, students);
		}
		
		for (Object[] o : (List<Object[]>)hibSession.createQuery(
				"select a.academicAreaAbbreviation, m.code, f.code, count(distinct s) from CourseRequest x inner join x.courseDemand.student s " +
				"inner join s.areaClasfMajors ac inner join ac.academicClassification f inner join ac.academicArea a " +
				"inner join ac.major m where s.session.uniqueId = :sessionId " +
				"group by a.academicAreaAbbreviation, m.code, f.code")
				.setLong("sessionId", session.getUniqueId())
				.setCacheable(true).list()) {
			String area = (String)o[0];
			String major = (String)o[1];
			String clasf = (String)o[2];
			int students = ((Number)o[3]).intValue();
			Hashtable<String, Hashtable<String, Integer>> clasfMajor2ll = iAreaClasfMajor2Real.get(area);
			if (clasfMajor2ll == null) {
				clasfMajor2ll = new Hashtable<String, Hashtable<String,Integer>>();
				iAreaClasfMajor2Real.put(area, clasfMajor2ll);
			}
			Hashtable<String, Integer> major2ll = clasfMajor2ll.get(clasf);
			if (major2ll == null) {
				major2ll = new Hashtable<String, Integer>();
				clasfMajor2ll.put(clasf, major2ll);
			}
			major2ll.put(major, students);
		}
	}
	
	public int getLastLikes(String areaAbbv, String clasfCode, String majorCode) {
		if (iAreaClasfMajor2LastLike.isEmpty()) return 0;
		Hashtable<String,Hashtable<String, Integer>> clasf2major2ll = (areaAbbv == null ? null : iAreaClasfMajor2LastLike.get(areaAbbv));
		if (clasf2major2ll == null || clasf2major2ll.isEmpty()) return 0;
		Hashtable<String, Integer> major2ll = (clasfCode == null ? null : clasf2major2ll.get(clasfCode));
		if (major2ll == null || major2ll.isEmpty()) return 0;
		Integer lastLike = (majorCode == null ? null : major2ll.get(majorCode));
		return (lastLike == null ? 0 : lastLike);
	}
	
	public int getCourseReqs(String areaAbbv, String clasfCode, String majorCode) {
		if (iAreaClasfMajor2Real.isEmpty()) return 0;
		Hashtable<String,Hashtable<String, Integer>> clasf2major2ll = (areaAbbv == null ? null : iAreaClasfMajor2Real.get(areaAbbv));
		if (clasf2major2ll == null || clasf2major2ll.isEmpty()) return 0;
		Hashtable<String, Integer> major2ll = (clasfCode == null ? null : clasf2major2ll.get(clasfCode));
		if (major2ll == null || major2ll.isEmpty()) return 0;
		Integer lastLike = (majorCode == null ? null : major2ll.get(majorCode));
		return (lastLike == null ? 0 : lastLike);
	}
	
	@Override
	public float getProjection(String areaAbbv, String clasfCode, String majorCode) {
		int lastLikes = getLastLikes(areaAbbv, clasfCode, majorCode);
		float estimate = lastLikes * super.getProjection(areaAbbv, clasfCode, majorCode) - getCourseReqs(areaAbbv, clasfCode, majorCode);
		if (estimate >= 0)
			return estimate / lastLikes;
		return 0f;
	}
	
	protected <A> Set<A> merge(Set<A> a, Set<A> b) {
		if (a == null || a.isEmpty()) return b;
		if (b == null || b.isEmpty()) return a;
		Set<A> c = new HashSet<A>(a); c.addAll(b);
		return c;
	}

	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		return merge(iCouseRequests.getDemands(course), super.getDemands(course));
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		Double priority = iCouseRequests.getEnrollmentPriority(studentId, courseId);
		if (priority != null) return priority;
		return super.getEnrollmentPriority(studentId, courseId);
	}

	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		return merge(iCouseRequests.getCourses(studentId), super.getCourses(studentId));
	}

}
