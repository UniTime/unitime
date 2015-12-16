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
	public HashMap<String, CurriculumInterface.CurriculumStudentsInterface[]> computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors, boolean multipleMajors) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.AcademicAreaInterface> loadAcademicAreas() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.AcademicClassificationInterface> loadAcademicClassifications() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.DepartmentInterface> loadDepartments() throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface.MajorInterface> loadMajors(Long curriculumId, Long academicAreaId, boolean multipleMajors) throws CurriculaException, PageAccessException;
	public String lastCurriculaFilter() throws CurriculaException, PageAccessException;
	public CurriculumInterface loadCurriculum(Long curriculumId) throws CurriculaException, PageAccessException;
	public Long saveCurriculum(CurriculumInterface curriculum) throws CurriculaException, PageAccessException;
	public Boolean deleteCurriculum(Long curriculumId) throws CurriculaException, PageAccessException;
	public Boolean deleteCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException;
	public Boolean mergeCurricula(Set<Long> curriculumIds) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface> findCurriculaForACourse(String courseName) throws CurriculaException, PageAccessException;
	public TreeSet<CurriculumInterface> findCurriculaForAnInstructionalOffering(Long offeringId) throws CurriculaException, PageAccessException;
	public Boolean saveClassifications(List<CurriculumInterface> curricula) throws CurriculaException, PageAccessException;
	
	public Collection<ClassAssignmentInterface.CourseAssignment> listCourseOfferings(String query, Integer limit, boolean includeNotOffered, boolean checkDepartment) throws CurriculaException, PageAccessException;
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
	public CurriculumInterface loadTemplate(Long acadAreaId, List<Long> majors) throws CurriculaException, PageAccessException;
}
