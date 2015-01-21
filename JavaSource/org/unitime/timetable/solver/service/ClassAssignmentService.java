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
