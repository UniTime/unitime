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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.CbsForm;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;


/** 
 * @author Tomas Muller
 */
@Service("/cbs")
public class CbsAction extends Action {

	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		CbsForm myForm = (CbsForm) form;
        // Check Access
        sessionContext.checkPermission(Right.ConflictStatistics);
		
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null) op="Refresh";
        
        if ("Refresh".equals(op)) {
        	myForm.reset(mapping,request);
        }

        if ("Change".equals(op)) {
        	sessionContext.getUser().setProperty("Cbs.limit", String.valueOf(myForm.getLimit()));
        	sessionContext.getUser().setProperty("Cbs.type", String.valueOf(myForm.getTypeInt()));
        } else {
        	myForm.setLimit(Double.parseDouble(sessionContext.getUser().getProperty("Cbs.limit", String.valueOf(CbsForm.sDefaultLimit))));
        	myForm.setTypeInt(Integer.parseInt(sessionContext.getUser().getProperty("Cbs.type", String.valueOf(CbsForm.sDefaultType))));
        }
        
    	ConflictStatisticsInfo cbs = null;
    	if (courseTimetablingSolverService.getSolver() != null) {
    		cbs = courseTimetablingSolverService.getSolver().getCbsInfo();
    	} else {
    		String solutionIdsStr = (String)sessionContext.getAttribute(SessionAttribute.SelectedSolution);
    		if (solutionIdsStr != null) {
    			for (String solutionId: solutionIdsStr.split(",")) {
    				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
    				if (solution != null) {
    					ConflictStatisticsInfo x = (ConflictStatisticsInfo)solution.getInfo("CBSInfo");
    					if (x != null) {
    						if (cbs==null) cbs = x; else cbs.merge(x);
    					}
    				}
    			}
    		}
    	}
    	
    	if (cbs != null)
    		request.setAttribute("cbs", cbs);

        return mapping.findForward("showCbs");
	}

}

