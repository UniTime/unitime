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
package org.unitime.timetable.solver.curricula.students;

import java.util.ArrayList;
import java.util.List;

import org.cpsolver.ifs.model.Variable;


/**
 * @author Tomas Muller
 */
public class CurVariable extends Variable<CurVariable, CurValue> {
	private CurCourse iCourse;

	public CurVariable(CurModel model, CurCourse course, int first, int size) {
		super();
		iCourse = course;
		List<CurValue> values = new ArrayList<CurValue>();
		for (int i = 0; i < size; i++)
			values.add(new CurValue(this, model.getStudents().get(first + i)));
		setValues(values);
	}
	
	public CurCourse getCourse() { return iCourse; }
	
	public String toString() {
		return getCourse().getCourseName();
	}
}
