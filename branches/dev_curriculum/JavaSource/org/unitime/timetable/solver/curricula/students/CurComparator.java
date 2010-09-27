package org.unitime.timetable.solver.curricula.students;

import net.sf.cpsolver.ifs.solution.Solution;
import net.sf.cpsolver.ifs.solution.SolutionComparator;
import net.sf.cpsolver.ifs.util.DataProperties;

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
