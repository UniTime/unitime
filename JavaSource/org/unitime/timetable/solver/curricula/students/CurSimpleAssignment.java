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

import java.util.HashMap;
import java.util.Map;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.Neighbour;


/**
 * @author Tomas Muller
 */
public class CurSimpleAssignment implements Neighbour<CurVariable, CurValue> {
	private CurValue iNewValue;
	
	public CurSimpleAssignment(CurValue newValue) {
		iNewValue = newValue;
	}

	@Override
	public void assign(Assignment<CurVariable, CurValue> assignment, long iteration) {
		assignment.assign(iteration, iNewValue);
	}

	@Override
	public double value(Assignment<CurVariable, CurValue> assignment) {
		CurValue old = assignment.getValue(iNewValue.variable());
		return iNewValue.variable().getCourse().penalty(assignment, iNewValue.getStudent(), old == null ? null : old.getStudent());
	}
	
	public String toString() {
		return iNewValue.variable().getCourse().getCourseName() + " = " + iNewValue.getStudent().getStudentId();
	}

	@Override
	public Map<CurVariable, CurValue> assignments() {
		Map<CurVariable, CurValue> ret = new HashMap<CurVariable, CurValue>();
		ret.put(iNewValue.variable(), iNewValue);
		return ret;
	}
}
