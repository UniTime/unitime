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

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface CurriculaServiceAsync {
	public void findCurricula(CurriculumInterface.CurriculumFilterRpcRequest filter, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException, PageAccessException;
	public void loadClassifications(List<Long> curriculumIds, AsyncCallback<List<CurriculumInterface.CurriculumClassificationInterface>> callback) throws CurriculaException, PageAccessException;
	public void computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors, boolean multipleMajors, AsyncCallback<HashMap<String, CurriculumInterface.CurriculumStudentsInterface[]>> callback) throws CurriculaException, PageAccessException;
	public void loadAcademicAreas(AsyncCallback<TreeSet<CurriculumInterface.AcademicAreaInterface>> callback) throws CurriculaException, PageAccessException;
	public void loadAcademicClassifications(AsyncCallback<TreeSet<CurriculumInterface.AcademicClassificationInterface>> callback) throws CurriculaException, PageAccessException;
	public void loadDepartments(AsyncCallback<TreeSet<CurriculumInterface.DepartmentInterface>> callback) throws CurriculaException, PageAccessException;
	public void loadMajors(Long curriculumId, Long academicAreaId, boolean multipleMajors, AsyncCallback<TreeSet<CurriculumInterface.MajorInterface>> callback) throws CurriculaException, PageAccessException;
	public void lastCurriculaFilter(AsyncCallback<String> callback) throws CurriculaException, PageAccessException;
	public void loadCurriculum(Long curriculumId, AsyncCallback<CurriculumInterface> callback) throws CurriculaException, PageAccessException;
	public void saveCurriculum(CurriculumInterface curriculum, AsyncCallback<Long> callback) throws CurriculaException, PageAccessException;
	public void deleteCurriculum(Long curriculumId, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void deleteCurricula(Set<Long> curriculumIds, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void mergeCurricula(Set<Long> curriculumIds, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void findCurriculaForACourse(String courseName, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException, PageAccessException;
	public void findCurriculaForAnInstructionalOffering(Long offeringId, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException, PageAccessException;
	public void saveClassifications(List<CurriculumInterface> curricula, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	
	public void listCourseOfferings(String query, Integer limit, boolean includeNotOffered, boolean checkDepartment, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws CurriculaException, PageAccessException;
	public void retrieveCourseDetails(String course, AsyncCallback<String> callback) throws CurriculaException, PageAccessException;
	public void listClasses(String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws CurriculaException, PageAccessException;
	public void getApplicationProperty(String[] name, AsyncCallback<String[]> callback) throws CurriculaException, PageAccessException;
	public void canAddCurriculum(AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void isAdmin(AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void makeupCurriculaFromLastLikeDemands(boolean lastLike, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void updateCurriculaByProjections(Set<Long> curriculumIds, boolean updateCurriculumCourses,  AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void populateCourseProjectedDemands(boolean includeOtherStudents, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void populateCourseProjectedDemands(boolean includeOtherStudents, Long offeringId, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;

	
	public void loadProjectionRules(AsyncCallback<HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>>> callback) throws CurriculaException, PageAccessException;
	public void saveProjectionRules(HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void canEditProjectionRules(AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException;
	public void loadTemplate(Long acadAreaId, List<Long> majors, AsyncCallback<CurriculumInterface> callback) throws CurriculaException, PageAccessException;
}
