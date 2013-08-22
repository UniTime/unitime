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
package org.unitime.timetable.solver.curricula;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

/**
 * @author Tomas Muller
 */
public class LastLikeStudentCourseDemands implements StudentCourseDemands {
	protected org.hibernate.Session iHibSession;
	protected Hashtable<String, Set<WeightedStudentId>> iDemandsForPemId = new Hashtable<String, Set<WeightedStudentId>>();
	protected Hashtable<Long, Hashtable<String, Set<WeightedStudentId>>> iDemandsForSubjectCourseNbr = new Hashtable<Long, Hashtable<String,Set<WeightedStudentId>>>();
	protected Hashtable<Long, Set<WeightedCourseOffering>> iStudentRequests = null;
	protected Long iSessionId = null;
	
	public LastLikeStudentCourseDemands(DataProperties properties) {
	}
	
	@Override
	public boolean isMakingUpStudents() { return false; }
	
	@Override
	public boolean canUseStudentClassEnrollmentsAsSolution() { return false; }

	@Override
	public boolean isWeightStudentsToFillUpOffering() { return false; }
	
	@Override
	public void init(org.hibernate.Session hibSession, Progress progress, Session session, Collection<InstructionalOffering> offerings) {
		iHibSession = hibSession;
		iSessionId = session.getUniqueId();
	}
	
	public float getProjection(String areaAbbv, String clasfCode, String majorCode) {
		return 1.0f;
	}
	
	protected Hashtable<String, Set<WeightedStudentId>> loadSubject(SubjectArea subject) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = new Hashtable<String, Set<WeightedStudentId>>();
		iDemandsForSubjectCourseNbr.put(subject.getUniqueId(), demandsForCourseNbr);
		for (Object[] d: (List<Object[]>)iHibSession.createQuery("select d.courseNbr, s.uniqueId, d.coursePermId, "+
				"a.academicAreaAbbreviation, f.code, m.code " +
				"from LastLikeCourseDemand d inner join d.student s left outer join s.academicAreaClassifications c left outer join s.posMajors m " +
				"left outer join c.academicArea a left outer join c.academicClassification f where " +
				"d.subjectArea.uniqueId=:subjectAreaId")
				.setLong("subjectAreaId", subject.getUniqueId()).setCacheable(true).list()) {
			String courseNbr = (String)d[0];
			String coursePermId = (String)d[2];
			String areaAbbv = (String)d[3];
			String clasfCode = (String)d[4];
			String majorCode = (String)d[5];
			WeightedStudentId studentId = new WeightedStudentId((Long)d[1], getProjection(areaAbbv, clasfCode, majorCode));
			studentId.setStats(areaAbbv, clasfCode, majorCode);
			studentId.setCurriculum(areaAbbv == null ? null : majorCode == null ? areaAbbv : areaAbbv + "/" + majorCode);
			Set<WeightedStudentId> studentIds = demandsForCourseNbr.get(courseNbr);
			if (studentIds == null) {
				studentIds = new HashSet<WeightedStudentId>();
				demandsForCourseNbr.put(courseNbr, studentIds);
			}
			studentIds.add(studentId);
			
			if (coursePermId!=null) {
			    studentIds = iDemandsForPemId.get(coursePermId);
			    if (studentIds==null) {
                    studentIds = new HashSet<WeightedStudentId>();
                    iDemandsForPemId.put(coursePermId, studentIds);
                }
                studentIds.add(studentId);
			}
		}
		return demandsForCourseNbr;
	}
	
	@Override
	public Set<WeightedStudentId> getDemands(CourseOffering course) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = iDemandsForSubjectCourseNbr.get(course.getSubjectArea().getUniqueId());
		if (demandsForCourseNbr == null) {
			demandsForCourseNbr = loadSubject(course.getSubjectArea());
		}
		Set<WeightedStudentId> studentIds = null;
		if (course.getPermId() != null)
			studentIds = iDemandsForPemId.get(course.getPermId());
		if (studentIds == null)
			studentIds = demandsForCourseNbr.get(course.getCourseNbr());

		if (course.getDemandOffering() != null && !course.getDemandOffering().equals(course)) {
			if (studentIds == null)
				studentIds = getDemands(course.getDemandOffering());
			else {
				studentIds = new HashSet<WeightedStudentId>(studentIds);
				studentIds.addAll(getDemands(course.getDemandOffering()));
			}
		}
		
		if (studentIds == null)
			studentIds = new HashSet<WeightedStudentId>();
		
		return studentIds;
	}
	
	@Override
	public Set<WeightedCourseOffering> getCourses(Long studentId) {
		if (iStudentRequests == null) {
			iStudentRequests = new Hashtable<Long, Set<WeightedCourseOffering>>();
			for (Object[] o : (List<Object[]>)iHibSession.createQuery(
					"select s.uniqueId, co, " +
					"a.academicAreaAbbreviation, f.code, m.code " +
					"from LastLikeCourseDemand x inner join x.student s left outer join s.academicAreaClassifications c left outer join s.posMajors m " +
					"left outer join c.academicArea a left outer join c.academicClassification f, CourseOffering co where " +
					"x.subjectArea.session.uniqueId = :sessionId and "+
					"co.subjectArea.uniqueId = x.subjectArea.uniqueId and " +
					"((x.coursePermId is not null and co.permId=x.coursePermId) or (x.coursePermId is null and co.courseNbr=x.courseNbr))")
					.setLong("sessionId", iSessionId)
					.setCacheable(true).list()) {
				Long sid = (Long)o[0];
				CourseOffering co = (CourseOffering)o[1];
				String areaAbbv = (String)o[2];
				String clasfCode = (String)o[3];
				String majorCode = (String)o[4];
				Set<WeightedCourseOffering> courses = iStudentRequests.get(sid);
				if (courses == null) {
					courses = new HashSet<WeightedCourseOffering>();
					iStudentRequests.put(sid, courses);
				}
				courses.add(new WeightedCourseOffering(co, getProjection(areaAbbv, clasfCode, majorCode)));
			}
		}
		return iStudentRequests.get(studentId);
	}

	@Override
	public Double getEnrollmentPriority(Long studentId, Long courseId) {
		return null;
	}
}
