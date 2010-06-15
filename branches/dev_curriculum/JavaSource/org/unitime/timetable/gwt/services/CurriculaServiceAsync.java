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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CurriculaServiceAsync {
	public void findCurricula(String filter, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException;
	public void loadClassifications(List<Long> curriculumIds, AsyncCallback<List<CurriculumInterface.CurriculumClassificationInterface>> callback) throws CurriculaException;
	public void makupClassifications(Long acadAreaId, List<Long> majors, boolean includeCourses, AsyncCallback<TreeSet<CurriculumInterface.CurriculumClassificationInterface>> callback) throws CurriculaException;
	public void computeEnrollmentsAndLastLikes(Long acadAreaId, List<Long> majors, AsyncCallback<HashMap<String, Integer[][]>> callback) throws CurriculaException;
	public void loadAcademicAreas(AsyncCallback<TreeSet<CurriculumInterface.AcademicAreaInterface>> callback) throws CurriculaException;
	public void loadAcademicClassifications(AsyncCallback<TreeSet<CurriculumInterface.AcademicClassificationInterface>> callback) throws CurriculaException;
	public void loadDepartments(AsyncCallback<TreeSet<CurriculumInterface.DepartmentInterface>> callback) throws CurriculaException;
	public void loadMajors(Long curriculumId, Long academicAreaId, AsyncCallback<TreeSet<CurriculumInterface.MajorInterface>> callback) throws CurriculaException;
	public void lastCurriculaFilter(AsyncCallback<String> callback) throws CurriculaException;
	public void loadCurriculum(Long curriculumId, AsyncCallback<CurriculumInterface> callback) throws CurriculaException;
	public void saveCurriculum(CurriculumInterface curriculum, AsyncCallback<Boolean> callback) throws CurriculaException;
	public void deleteCurriculum(Long curriculumId, AsyncCallback<Boolean> callback) throws CurriculaException;
	public void findCurriculaForACourse(String courseName, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException;
	public void findCurriculaForAnInstructionalOffering(Long offeringId, AsyncCallback<TreeSet<CurriculumInterface>> callback) throws CurriculaException;

	
	public void listCourseOfferings(String query, Integer limit, AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws CurriculaException;
	public void retrieveCourseDetails(String course, AsyncCallback<String> callback) throws CurriculaException;
	public void listClasses(String course, AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws CurriculaException;
	public void getAppliationProperty(String[] name, AsyncCallback<String[]> callback) throws CurriculaException;
	public void canAddCurriculum(AsyncCallback<Boolean> callback) throws CurriculaException;
}
