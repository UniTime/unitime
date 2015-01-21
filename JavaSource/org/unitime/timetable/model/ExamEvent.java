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
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.Set;

import org.unitime.timetable.model.base.BaseExamEvent;



/**
 * @author Tomas Muller
 */
public abstract class ExamEvent extends BaseExamEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        return getExam().getStudents();
  
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        return getExam().getInstructors();
    }
    
    public Session getSession() { return getExam().getSession(); }
    
    public Collection<Long> getStudentIds() {
        return getExam().getStudentIds();
    }
    
	@Override
	public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
		return getExam().getStudentClassEnrollments();
	}
}