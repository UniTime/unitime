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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.Progress;

import org.unitime.timetable.model.CurriculumProjectionRule;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

public class ProjectedStudentCourseDemands extends LastLikeStudentCourseDemands {
	private Hashtable<String,Hashtable<String,Hashtable<String, Float>>> iAreaClasfMajor2Proj = new Hashtable<String, Hashtable<String,Hashtable<String,Float>>>();
	private Progress iProgress;
	
	public ProjectedStudentCourseDemands(DataProperties properties) {
		super(properties);
	}
	
	public void init(org.hibernate.Session hibSession, Progress progress, Session session) {
		super.init(hibSession, progress, session);
		iProgress = progress;
		progress.setPhase("Loading curriculum projections", 1);
		for (CurriculumProjectionRule rule: (List<CurriculumProjectionRule>)hibSession.createQuery(
				"select r from CurriculumProjectionRule r where r.academicArea.session.uniqueId=:sessionId")
				.setLong("sessionId", session.getUniqueId()).setCacheable(true).list()) {
			String areaAbbv = rule.getAcademicArea().getAcademicAreaAbbreviation();
			String majorCode = (rule.getMajor() == null ? "" : rule.getMajor().getCode());
			String clasfCode = rule.getAcademicClassification().getCode();
			Float projection = rule.getProjection();
			Hashtable<String,Hashtable<String, Float>> clasf2major2proj = iAreaClasfMajor2Proj.get(areaAbbv);
			if (clasf2major2proj == null) {
				clasf2major2proj = new Hashtable<String, Hashtable<String,Float>>();
				iAreaClasfMajor2Proj.put(areaAbbv, clasf2major2proj);
			}
			Hashtable<String, Float> major2proj = clasf2major2proj.get(clasfCode);
			if (major2proj == null) {
				major2proj = new Hashtable<String, Float>();
				clasf2major2proj.put(clasfCode, major2proj);
			}
			major2proj.put(majorCode, projection);
		}
		progress.incProgress();
	}
	
	public float getProjection(String areaAbbv, String clasfCode, String majorCode) {
		if (iAreaClasfMajor2Proj.isEmpty()) return 1.0f;
		Hashtable<String,Hashtable<String, Float>> clasf2major2proj = iAreaClasfMajor2Proj.get(areaAbbv);
		if (clasf2major2proj == null || clasf2major2proj.isEmpty()) return 1.0f;
		Hashtable<String, Float> major2proj = clasf2major2proj.get(clasfCode);
		if (major2proj == null) return 1.0f;
		Float projection = major2proj.get(majorCode);
		if (projection == null)
			projection = major2proj.get("");
		return (projection == null ? 1.0f : projection);
	}
	
	@Override
	protected Hashtable<String, Set<WeightedStudentId>> loadSubject(SubjectArea subject) {
		Hashtable<String, Set<WeightedStudentId>> demandsForCourseNbr = new Hashtable<String, Set<WeightedStudentId>>();
		iDemandsForSubjectCourseNbr.put(subject.getUniqueId(), demandsForCourseNbr);
		for (Object[] d: (List<Object[]>)iHibSession.createQuery("select d.courseNbr, s.uniqueId, d.coursePermId, "+
				"a.academicAreaAbbreviation, f.code, m.code " +
				"from LastLikeCourseDemand d inner join d.student s inner join s.academicAreaClassifications c inner join s.posMajors m " +
				"inner join c.academicArea a inner join c.academicClassification f where " +
				"d.subjectArea.uniqueId=:subjectAreaId")
				.setLong("subjectAreaId", subject.getUniqueId()).setCacheable(true).list()) {
			String courseNbr = (String)d[0];
			String coursePermId = (String)d[2];
			String areaAbbv = (String)d[3];
			String clasfCode = (String)d[4];
			String majorCode = (String)d[5];
			WeightedStudentId studentId = new WeightedStudentId((Long)d[1], getProjection(areaAbbv, clasfCode, majorCode));
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
		if (demandsForCourseNbr.isEmpty()) {
			iProgress.warn("There are no projected demands for " + subject.getSubjectAreaAbbreviation()+ ", using last-like course demands instead.");
			return super.loadSubject(subject);
		}

		return demandsForCourseNbr;
	}
}
