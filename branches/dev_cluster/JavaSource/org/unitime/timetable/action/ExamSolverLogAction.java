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
        	solver.setDebugLevel(myForm.getLevelInt());
        	request.setAttribute("log", solver.getLog());
        }
        
        return mapping.findForward("showLog");
	}

}

