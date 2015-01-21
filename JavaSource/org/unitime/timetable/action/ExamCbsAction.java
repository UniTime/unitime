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
import org.unitime.timetable.form.ExamCbsForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.service.SolverService;


/** 
 * @author Tomas Muller
 */
@Service("/ecbs")
public class ExamCbsAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamCbsForm myForm = (ExamCbsForm) form;
        // Check Access
		sessionContext.checkPermission(Right.ExaminationConflictStatistics);
		
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null) op="Refresh";
        
        if ("Change".equals(op)) {
        	sessionContext.getUser().setProperty("Ecbs.limit", String.valueOf(myForm.getLimit()));
        	sessionContext.getUser().setProperty("Ecbs.type", String.valueOf(myForm.getTypeInt()));
        } else {
        	myForm.reset(mapping,request);
        	myForm.setTypeInt(Integer.parseInt(sessionContext.getUser().getProperty("Ecbs.type", String.valueOf(ExamCbsForm.sDefaultType))));
        	myForm.setLimit(Double.parseDouble(sessionContext.getUser().getProperty("Ecbs.limit", String.valueOf(ExamCbsForm.sDefaultLimit))));
        }
        
        ExamConflictStatisticsInfo cbs = null;
    	if (examinationSolverService.getSolver() != null)
    		cbs = examinationSolverService.getSolver().getCbsInfo();
    	
    	if (cbs != null) {
    		request.setAttribute("cbs", cbs);
    	} else {
    		if (examinationSolverService.getSolver() == null)
    			request.setAttribute("warning", "No examination data are loaded into the solver, conflict-based statistics is not available.");
    		else
    			request.setAttribute("warning", "Conflict-based statistics is not available at the moment.");
    	}

        return mapping.findForward("show");
	}

}

