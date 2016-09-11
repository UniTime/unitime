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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/student-groups")
public class StudentGroupsConnector extends ApiConnector {
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		helper.getSessionContext().checkPermissionAnyAuthority(sessionId, "Session", Right.ApiRetrieveStudentGroups);
		
		List<StudentGroupInfo> response = new ArrayList<StudentGroupInfo>();
		for (StudentGroup g: (List<StudentGroup>)StudentGroupDAO.getInstance().getSession().createQuery(
				"from StudentGroup g where g.session.uniqueId = :sessionId")
				.setLong("sessionId", sessionId).list())
			response.add(new StudentGroupInfo(g));
			
		helper.setResponse(response);
	}
	
	static class StudentGroupInfo {
		Long iId;
		String iExternalId;
		String iAbbreviation;
		String iName;
		List<StudentInfo> iStudents = new ArrayList<StudentInfo>();
		
		StudentGroupInfo(StudentGroup g) {
			iId = g.getUniqueId();
			iExternalId = g.getExternalUniqueId();
			iAbbreviation = g.getGroupAbbreviation();
			iName = g.getGroupName();
			for (Student s: g.getStudents())
				iStudents.add(new StudentInfo(s));
		}
	}
	
	static class StudentInfo {
		Long iStudentId;
		String iExternalId;
		String iFirstName;
		String iMiddleName;
		String iLastName;
		String iTitle;
		String iEmail;
		String iSectioningStatus;
		List<String> iArea;
		List<String> iClassification;
		List<String> iMajor;
		List<String> iMinor;
		List<String> iGroup;
		List<String> iAccomodation;
		Long iCourseId;
		String iSubjectArea;
		String iCourseNumber;
		String iCourseTitle;
		Long iClassId;
		String iSubpart;
		String iSectionNumber;
		String iClassSuffix;
		String iClassExternalId;
		Date iRequestDate, iEnrollmentDate;
		
		StudentInfo(Student student) {
			iStudentId = student.getUniqueId();
			iExternalId = student.getExternalUniqueId();
			iFirstName = student.getFirstName();
			iMiddleName = student.getMiddleName();
			iLastName = student.getLastName();
			iTitle = student.getAcademicTitle();
			iEmail = student.getEmail();
			if (student.getSectioningStatus() != null)
				iSectioningStatus = student.getSectioningStatus().getReference();
			for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
				if (iArea == null) { iArea = new ArrayList<String>(); iClassification = new ArrayList<String>(); }
				iArea.add(aac.getAcademicArea().getAcademicAreaAbbreviation());
				iClassification.add(aac.getAcademicClassification().getCode());
			}
			for (PosMajor major: student.getPosMajors()) {
				if (iMajor == null) iMajor = new ArrayList<String>();
				iMajor.add(major.getCode());
			}
			for (PosMinor minor: student.getPosMinors()) {
				if (iMinor == null) iMinor = new ArrayList<String>();
				iMinor.add(minor.getCode());
			}
			for (StudentGroup group: student.getGroups()) {
				if (iGroup == null) iGroup = new ArrayList<String>();
				iGroup.add(group.getGroupAbbreviation());
			}
			for (StudentAccomodation accomodation: student.getAccomodations()) {
				if (iAccomodation == null) iAccomodation = new ArrayList<String>();
				iAccomodation.add(accomodation.getAbbreviation());
			}
		}
	}

	@Override
	protected String getName() {
		return "student-groups";
	}

}
