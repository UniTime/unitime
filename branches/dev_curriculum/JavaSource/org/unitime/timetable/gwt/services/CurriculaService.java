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
package org.unitime.timetable.gwt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("curriculaService")
public interface CurriculaService extends RemoteService {
	public TreeSet<CurriculumInterface> findCurricula(String filter) throws CurriculaException;
	public List<CurriculumInterface.CurriculumClassificationInterface> loadClassifications(List<Long> curriculumIds) throws CurriculaException;
	public TreeSet<CurriculumInterface.CurriculumClassificationInterface> makupClassifications(Long acadAreaId, List<Long> majors, boolean includeCourses) throws CurriculaException;
	public HashMap<String, Integer[][]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors) throws CurriculaException;
	public TreeSet<CurriculumInterface.AcademicAreaInterface> loadAcademicAreas() throws CurriculaException;
	public TreeSet<CurriculumInterface.AcademicClassificationInterface> loadAcademicClassifications() throws CurriculaException;
	public TreeSet<CurriculumInterface.DepartmentInterface> loadDepartments() throws CurriculaException;
	public TreeSet<CurriculumInterface.MajorInterface> loadMajors(Long curriculumId, Long academicAreaId) throws CurriculaException;
	public String lastCurriculaFilter() throws CurriculaException;
	public CurriculumInterface loadCurriculum(Long curriculumId) throws CurriculaException;
	public Boolean saveCurriculum(CurriculumInterface curriculum) throws CurriculaException;
	public Boolean deleteCurriculum(Long curriculumId) throws CurriculaException;
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit) throws CurriculaException;
	public String retrieveCourseDetails(String course) throws CurriculaException;
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(String course) throws CurriculaException;
	public String[] getAppliationProperty(String[] name) throws CurriculaException;
}
