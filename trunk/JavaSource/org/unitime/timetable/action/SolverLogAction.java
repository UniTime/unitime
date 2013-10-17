/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.action;

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
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
        	solver.setDebugLevel(myForm.getLevelInt());
        	request.setAttribute("log", solver.getLog());
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

