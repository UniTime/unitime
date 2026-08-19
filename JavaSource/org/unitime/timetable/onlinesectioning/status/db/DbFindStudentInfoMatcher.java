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
package org.unitime.timetable.onlinesectioning.status.db;

import java.util.Set;

import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction.FindStudentInfoMatcher;
import org.unitime.timetable.util.NameFormat;

public class DbFindStudentInfoMatcher extends FindStudentInfoMatcher {
	private static final long serialVersionUID = 1L;
	protected NameFormat iFormat;
	
	public DbFindStudentInfoMatcher(AcademicSessionInfo session, Query query, NameFormat format, Set<Long> myStudents) {
		super(session, query, myStudents);
		iFormat = format;
	}
	
	public boolean isMyStudent(Student student) {
		return iMyStudents != null && iMyStudents.contains(student.getUniqueId());
	}

	public boolean match(Student student) {
		return student != null && iQuery.match(new DbStudentMatcher(student, iDefaultSectioningStatus, iFormat, isMyStudent(student)));
	}
}