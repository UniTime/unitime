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

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.form.ExamCbsForm;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;


/** 
 * @author Tomas Muller
 */
@Action(value = "ecbs", results = {
		@Result(name = "show", type = "tiles", location = "ecbs.tiles")
	})
@TilesDefinition(name = "ecbs.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Conflict-Based Statistics"),
		@TilesPutAttribute(name = "body", value = "/exam/cbs.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamCbsAction extends UniTimeAction<ExamCbsForm> {
	private static final long serialVersionUID = 6759587563290713198L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);

	public String execute() throws Exception {
        // Check Access
		sessionContext.checkPermission(Right.ExaminationConflictStatistics);
		
		if (form == null) form = new ExamCbsForm();

		if (form.getOp() != null) op = form.getOp();
        if (op==null) op = MSG.buttonRefresh();
        
        if (MSG.buttonChange().equals(op)) {
        	sessionContext.getUser().setProperty("Ecbs.limit", String.valueOf(form.getLimit()));
        	sessionContext.getUser().setProperty("Ecbs.type", String.valueOf(form.getTypeInt()));
        } else {
        	form.reset();
        	form.setTypeInt(Integer.parseInt(sessionContext.getUser().getProperty("Ecbs.type", String.valueOf(ExamCbsForm.sDefaultType.ordinal()))));
        	form.setLimit(Double.parseDouble(sessionContext.getUser().getProperty("Ecbs.limit", String.valueOf(ExamCbsForm.sDefaultLimit))));
        }
        
        ExamConflictStatisticsInfo cbs = null;
    	if (getExaminationSolverService().getSolver() != null)
    		cbs = getExaminationSolverService().getSolver().getCbsInfo();
    	
    	if (cbs != null) {
    		request.setAttribute("cbs", cbs);
    	} else {
    		if (getExaminationSolverService().getSolver() == null)
    			request.setAttribute("warning", MSG.warnCbsNoSolver());
    		else
    			request.setAttribute("warning", MSG.warnNoCbs());
    	}

        return "show";
	}
	
	public void printTable() {
		ExamConflictStatisticsInfo.printHtmlHeader(getPageContext().getOut());
		ExamConflictStatisticsInfo cbs = (ExamConflictStatisticsInfo)request.getAttribute("cbs");
		cbs.printHtml(getPageContext().getOut(), form.getLimit() / 100.0, form.getTypeInt(), true); 
	}
}

