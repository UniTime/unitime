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
package org.unitime.timetable.gwt.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author Tomas Muller
 */
@RemoteServiceRelativePath("curricula.gwt")
public interface CurriculaService extends RemoteService {
	public TreeSet<CurriculumInterface> findCurricula(CurriculumInterface.CurriculumFilterRpcRequest filter) throws CurriculaException, PageAccessException;
	public List<CurriculumInterface.CurriculumClassificationInterface> loadClassifications(List<Long> curriculumIds) throws CurriculaException, PageAccessException;
	public HashMap<String, CurriculumInterface.CurriculumStudentsInterface[]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.AcademicAreaInterface> loadAcademicAreas() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.AcademicClassificationInterface> loadAcademicClassifications() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.DepartmentInterface> loadDepartments() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.MajorInterface> loadMajors(Long curriculumId, Long academicAreaId) throws CurriculaException, PageAccessException;
	public String lastCurriculaFilter() throws CurriculaException, PageAccessException;
	public CurriculumInterface loadCurriculum(Long curriculumId) throws CurriculaException, PageAccessException;
	public Long saveCurriculum(CurriculumInterface curriculum) throws CurriculaException, PageAccessException;
	public Boolean deleteCurriculum(Long curriculumId) throws CurriculaException, PageAccessException;
	public Boolean deleteCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException;
	public Boolean mergeCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface> findCurriculaForACourse(String courseName) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface> findCurriculaForAnInstructionalOffering(Long offeringId) throws CurriculaException, PageAccessException;
	public Boolean saveClassifications(List<CurriculumInterface> curricula) throws CurriculaException, PageAccessException;
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit) throws CurriculaException, PageAccessException;
	public String retrieveCourseDetails(String course) throws CurriculaException, PageAccessException;
	public Collection<ClassAssignmentInterface.ClassAssignment> listClasses(String course) throws CurriculaException, PageAccessException;
	public String[] getApplicationProperty(String[] name) throws CurriculaException, PageAccessException;
	public Boolean canAddCurriculum() throws CurriculaException, PageAccessException;
	public Boolean isAdmin() throws CurriculaException, PageAccessException;
	public Boolean makeupCurriculaFromLastLikeDemands(boolean lastLike) throws CurriculaException, PageAccessException;
	public Boolean updateCurriculaByProjections(Set<Long> curriculumIds, boolean updateCurriculumCourses) throws CurriculaException, PageAccessException;
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents) throws CurriculaException, PageAccessException;
	public Boolean populateCourseProjectedDemands(boolean includeOtherStudents, Long offeringId) throws CurriculaException, PageAccessException;
	
	public HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> loadProjectionRules() throws CurriculaException, PageAccessException;
	public Boolean saveProjectionRules(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules) throws CurriculaException, PageAccessException;
	public Boolean canEditProjectionRules() throws CurriculaException, PageAccessException;
}
