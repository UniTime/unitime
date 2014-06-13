/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.service;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.CachedClassAssignmentProxy;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.SolutionClassAssignmentProxy;
import org.unitime.timetable.solver.SolverProxy;

/**
 * @author Tomas Muller
 */
@Service("classAssignmentService")
public class ClassAssignmentService implements AssignmentService<ClassAssignmentProxy> {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public ClassAssignmentProxy getAssignment() {
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		if (solver!=null) return new CachedClassAssignmentProxy(solver);
		
		String solutionIdsStr = (String)sessionContext.getAttribute(SessionAttribute.SelectedSolution);
		HashSet<Long> solutionIds = new HashSet<Long>();
		if (solutionIdsStr != null) {
			for (StringTokenizer s = new StringTokenizer(solutionIdsStr, ","); s.hasMoreTokens(); )
				solutionIds.add(Long.valueOf(s.nextToken()));
		}
		
		ProxyHolder<HashSet<Long>, SolutionClassAssignmentProxy> h = (ProxyHolder<HashSet<Long>, SolutionClassAssignmentProxy>)sessionContext.getAttribute(SessionAttribute.ClassAssignment);
		if (h != null && h.isValid(solutionIds))
			return h.getProxy();
		
		SolutionClassAssignmentProxy newProxy = new SolutionClassAssignmentProxy(solutionIds);
		sessionContext.setAttribute(SessionAttribute.ClassAssignment, new ProxyHolder<HashSet<Long>, SolutionClassAssignmentProxy>(solutionIds, newProxy));
		return newProxy;
	}
}
