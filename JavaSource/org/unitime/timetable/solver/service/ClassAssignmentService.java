package org.unitime.timetable.solver.service;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolutionClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;

@Service("classAssignmentService")
public class ClassAssignmentService implements AssignmentService<ClassAssignmentProxy> {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public ClassAssignmentProxy getAssignment() {
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver!=null) return new CachedClassAssignmentProxy(solver);
		
		String solutionIdsStr = (String)sessionContext.getAttribute(SessionAttribute.SelectedSolution);
		Set<Long> solutionIds = new HashSet<Long>();
		if (solutionIdsStr != null) {
			for (StringTokenizer s = new StringTokenizer(solutionIdsStr, ","); s.hasMoreTokens(); )
				solutionIds.add(Long.valueOf(s.nextToken()));
		}
		
		SolutionClassAssignmentProxy cachedProxy = (SolutionClassAssignmentProxy)sessionContext.getAttribute(SessionAttribute.ClassAssignment);
		if (cachedProxy != null && cachedProxy.equals(solutionIds)) {
			return cachedProxy;
		}
		
		SolutionClassAssignmentProxy newProxy = new SolutionClassAssignmentProxy(solutionIds);
		sessionContext.setAttribute(SessionAttribute.ClassAssignment, newProxy);
		return newProxy;
	}
}
