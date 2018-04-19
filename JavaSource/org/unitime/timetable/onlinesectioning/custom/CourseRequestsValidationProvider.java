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
package org.unitime.timetable.onlinesectioning.custom;

import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

/**
 * @author Tomas Muller
 */
public interface CourseRequestsValidationProvider {
	
	public void checkEligibility(OnlineSectioningServer server, OnlineSectioningHelper helper, EligibilityCheck check, Student student) throws SectioningException;
	
	public void check(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException;
	
	public boolean updateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, OnlineSectioningLog.Action.Builder action) throws SectioningException;
	
	public boolean revalidateStudent(OnlineSectioningServer server, OnlineSectioningHelper helper, Student student, OnlineSectioningLog.Action.Builder action) throws SectioningException;
	
	public void validate(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request, CheckCoursesResponse response) throws SectioningException;
	
	public void submit(OnlineSectioningServer server, OnlineSectioningHelper helper, CourseRequestInterface request) throws SectioningException;
	
	public Collection<Long> updateStudents(OnlineSectioningServer server, OnlineSectioningHelper helper, List<Student> students) throws SectioningException;
	
	public void dispose();
	
}
