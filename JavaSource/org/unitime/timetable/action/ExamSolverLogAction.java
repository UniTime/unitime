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
import org.unitime.timetable.form.ExamSolverLogForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;


/** 
 * @author Tomas Muller
 */
@Service("/examSolverLog")
public class ExamSolverLogAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		ExamSolverLogForm myForm = (ExamSolverLogForm) form;
        // Check Access
		sessionContext.checkPermission(Right.ExaminationSolverLog);
		        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Change".equals(op)) {
        	if (myForm.getLevelNoDefault()!=null)
        		sessionContext.getUser().setProperty("SolverLog.level", myForm.getLevelNoDefault());
        } else {
        	myForm.setLevel(sessionContext.getUser().getProperty("SolverLog.level"));
        }
        
        // Change log level
        ExamSolverProxy solver = examinationSolverService.getSolver();
        if (solver != null) {
        	request.setAttribute("log", solver.getLog(myForm.getLevelInt(), true, null));
        }
        
        return mapping.findForward("showLog");
	}

}

