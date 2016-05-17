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
package org.unitime.timetable.action;

import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.ifs.util.Progress.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.SolverLogForm;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.LogInfo;


/** 
 * @author Tomas Muller
 */
@Service("/solverLog")
public class SolverLogAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		SolverLogForm myForm = (SolverLogForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.SolverLog);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        // Change log level
        if ("Change".equals(op)) {
        	if (myForm.getLevelNoDefault()!=null)
        		sessionContext.getUser().setProperty("SolverLog.level", myForm.getLevelNoDefault());
        } else {
        	myForm.setLevel(sessionContext.getUser().getProperty("SolverLog.level"));
        }

        SolverProxy solver = courseTimetablingSolverService.getSolver();
        if (solver!=null) {
        	request.setAttribute("log", solver.getLog(myForm.getLevelInt(), true, null));
        } else {
			String solutionIdsStr = (String)sessionContext.getAttribute(SessionAttribute.SelectedSolution);
			if (solutionIdsStr!=null && !solutionIdsStr.isEmpty()) {
				StringTokenizer s = new StringTokenizer(solutionIdsStr,",");
				LogInfo[] logInfo = new LogInfo[s.countTokens()];
				String[] ownerName = new String[s.countTokens()];
				for (int i=0;i<logInfo.length;i++) {
					Solution solution = (new SolutionDAO()).get(Long.valueOf(s.nextToken()));
					if (solution!=null) {
						logInfo[i] = (LogInfo)solution.getInfo("LogInfo");
						ownerName[i] = solution.getOwner().getName();
					}
				}
				myForm.setLogs(logInfo);
				myForm.setOwnerNames(ownerName);
			}
        }
                
        return mapping.findForward("showLog");
	}
}

