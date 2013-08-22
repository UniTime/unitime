/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

