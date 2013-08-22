/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.curricula.students;

import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solution.SolutionComparator;
import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public class CurComparator implements SolutionComparator<CurVariable, CurValue>{
	
	public CurComparator(DataProperties config) {
	}

	@Override
	public boolean isBetterThanBestSolution(Solution<CurVariable, CurValue> currentSolution) {
        CurModel m = (CurModel)currentSolution.getModel();
		if (m.getAssignedWeight() > m.getBestWeight() + m.getMinStudentWidth() / 2.0) return true;
		if (m.getAssignedWeight() < m.getBestWeight() - m.getMinStudentWidth() / 2.0) return false;
		return m.getTotalValue() < currentSolution.getBestValue();
	}

}
