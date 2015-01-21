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

import org.cpsolver.ifs.heuristics.NeighbourSelection;
import org.cpsolver.ifs.model.Neighbour;
import org.cpsolver.ifs.solution.Solution;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public class CurSimpleMove implements NeighbourSelection<CurVariable, CurValue>{
	private CurVariableSelection iVariableSelection;
	private CurValueSelection iValueSelection;

	protected CurSimpleMove(DataProperties	config) {
		iVariableSelection = new CurVariableSelection(config);
		iValueSelection = new CurValueSelection(config);
	}

	@Override
	public void init(Solver<CurVariable, CurValue> solver) {
	}

	@Override
	public Neighbour<CurVariable, CurValue> selectNeighbour(
			Solution<CurVariable, CurValue> solution) {
		CurVariable var = iVariableSelection.selectVariable(solution);
		if (var == null)
			return null;
		CurValue val = iValueSelection.selectValueFast(solution, var);
		return (val == null ? null : new CurSimpleAssignment(val));
	}
}
