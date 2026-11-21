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

import org.unitime.timetable.gwt.command.client.XsrfBaseWrapper;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CurriculaException;
import org.unitime.timetable.gwt.shared.CurriculumInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicAreaInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.AcademicClassificationInterface;
import org.unitime.timetable.gwt.shared.CurriculumInterface.MajorInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.HasRpcToken;

public class XsrfCurriculaService extends XsrfBaseWrapper implements CurriculaServiceAsync {
	CurriculaServiceAsync iRPC;
	
	public XsrfCurriculaService(CurriculaServiceAsync rpc) {
		super((HasRpcToken) rpc);
		iRPC = rpc;
	}

	@Override
	public void findCurricula(final CurriculumInterface.CurriculumFilterRpcRequest filter, final AsyncCallback<TreeSet<CurriculumInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findCurricula(filter, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadClassifications(final List<Long> curriculumIds, final AsyncCallback<List<CurriculumInterface.CurriculumClassificationInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadClassifications(curriculumIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void computeEnrollmentsAndLastLikes(final Long acadAreaId, final List<Long> majors, final boolean multipleMajors, final AsyncCallback<HashMap<String, CurriculumInterface.CurriculumStudentsInterface[]>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.computeEnrollmentsAndLastLikes(acadAreaId, majors, multipleMajors, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadAcademicAreas(final AsyncCallback<TreeSet<CurriculumInterface.AcademicAreaInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadAcademicAreas(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadAcademicClassifications(final AsyncCallback<TreeSet<CurriculumInterface.AcademicClassificationInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadAcademicClassifications(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadDepartments(final AsyncCallback<TreeSet<CurriculumInterface.DepartmentInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadDepartments(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadMajors(final Long curriculumId, final Long academicAreaId, final boolean multipleMajors, final AsyncCallback<TreeSet<CurriculumInterface.MajorInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadMajors(curriculumId, academicAreaId, multipleMajors, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void lastCurriculaFilter(final AsyncCallback<String> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.lastCurriculaFilter(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadCurriculum(final Long curriculumId, final AsyncCallback<CurriculumInterface> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadCurriculum(curriculumId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void saveCurriculum(final CurriculumInterface curriculum, final AsyncCallback<Long> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.saveCurriculum(curriculum, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void deleteCurriculum(final Long curriculumId, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.deleteCurriculum(curriculumId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void deleteCurricula(final Set<Long> curriculumIds, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.deleteCurricula(curriculumIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void mergeCurricula(final Set<Long> curriculumIds, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.mergeCurricula(curriculumIds, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void findCurriculaForACourse(final String courseName, final AsyncCallback<TreeSet<CurriculumInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findCurriculaForACourse(courseName, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void findCurriculaForAnInstructionalOffering(final Long offeringId, final AsyncCallback<TreeSet<CurriculumInterface>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.findCurriculaForAnInstructionalOffering(offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void saveClassifications(final List<CurriculumInterface> curricula, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.saveClassifications(curricula, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
	
	@Override
	public void listCourseOfferings(final String query, final Integer limit, final boolean includeNotOffered, final boolean checkDepartment, final AsyncCallback<Collection<ClassAssignmentInterface.CourseAssignment>> callback) throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listCourseOfferings(query, limit, includeNotOffered, checkDepartment, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});

	}

	@Override
	public void retrieveCourseDetails(final String course, final AsyncCallback<String> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.retrieveCourseDetails(course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void listClasses(final String course, final AsyncCallback<Collection<ClassAssignmentInterface.ClassAssignment>> callback) throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.listClasses(course, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void getCourseWidth(final AsyncCallback<String> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.getCourseWidth(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void canAddCurriculum(final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.canAddCurriculum(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void isAdmin(final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.isAdmin(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void makeupCurriculaFromLastLikeDemands(final boolean lastLike, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.makeupCurriculaFromLastLikeDemands(lastLike, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void updateCurriculaByProjections(final Set<Long> curriculumIds, final boolean updateCurriculumCourses, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.updateCurriculaByProjections(curriculumIds, updateCurriculumCourses, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void populateCourseProjectedDemands(final boolean includeOtherStudents, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.populateCourseProjectedDemands(includeOtherStudents, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void populateCourseProjectedDemands(final boolean includeOtherStudents, final Long offeringId, final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.populateCourseProjectedDemands(includeOtherStudents, offeringId, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	

	@Override
	public void loadProjectionRules(final AsyncCallback<HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>>> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadProjectionRules(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void saveProjectionRules(final HashMap<AcademicAreaInterface, HashMap<MajorInterface, HashMap<AcademicClassificationInterface, Number[]>>> rules, AsyncCallback<Boolean> callback) throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.saveProjectionRules(rules, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void canEditProjectionRules(final AsyncCallback<Boolean> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.canEditProjectionRules(callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}

	@Override
	public void loadTemplate(final Long acadAreaId, final List<Long> majors, final AsyncCallback<CurriculumInterface> callback)  throws CurriculaException, PageAccessException {
		checkToken(new AsyncCallback<Boolean>() {
			@Override
			public void onSuccess(Boolean result) {
				iRPC.loadTemplate(acadAreaId, majors, callback);
			}
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
