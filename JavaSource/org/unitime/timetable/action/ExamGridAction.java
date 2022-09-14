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

import java.io.OutputStream;
import java.util.Date;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.form.ExamGridForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.timegrid.PdfExamGridTable;


/** 
 * @author Tomas Muller
 */
@Action(value = "examGrid", results = {
		@Result(name = "showGrid", type = "tiles", location = "examGrid.tiles")
	})
@TilesDefinition(name = "examGrid.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Timetable"),
		@TilesPutAttribute(name = "body", value = "/exam/examGrid.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamGridAction extends UniTimeAction<ExamGridForm> {
	private static final long serialVersionUID = 7694694407236929358L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	private String resource;
	
	public String getResource() { return resource; }
	public void setResource(String resource) { this.resource = resource; }

	public String execute() throws Exception {
        // Check Access
		sessionContext.checkPermission(Right.ExaminationTimetable);
		
        ExamSolverProxy solver = getExaminationSolverService().getSolver();

    	if (form == null) {
	    	form = new ExamGridForm();
	    	form.reset();
	    	if (solver != null) form.setExamType(solver.getExamTypeId());
	    }
	    
    	if (form.getOp() != null) op = form.getOp();
    	if (op == null && resource != null) op = MSG.buttonChange();

        if (MSG.buttonChange().equals(op) || MSG.actionExportPdf().equals(op)) {
        	form.save(sessionContext);
        }
        
        form.load(sessionContext);
        
        if (form.getExamType() == null) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty())
				form.setExamType(types.first().getUniqueId());
        }
        
        if ("Cbs".equals(op)) {
            if (request.getParameter("resource")!=null)
                form.setResource(Integer.parseInt(request.getParameter("resource")));
            if (request.getParameter("filter")!=null)
                form.setFilter(request.getParameter("filter"));
        }
        
        if (RoomAvailability.getInstance()!=null && form.getExamType() != null) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = ExamPeriod.getBounds(session, form.getExamType());
            String exclude = ExamTypeDAO.getInstance().get(form.getExamType()).getReference();
            if (bounds != null) {
            	RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],exclude,false);
            	RoomAvailability.setAvailabilityWarning(request, session, form.getExamType(), true, false);
            }
        }
        
        PdfExamGridTable table = new PdfExamGridTable(form, sessionContext, getExaminationSolverService().getSolver());
        
        request.setAttribute("table", table);

        if (MSG.actionExportPdf().equals(op)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "timetable");
        	table.export(out);
        	out.flush(); out.close();
        	return null;
        }

        form.setOp(MSG.buttonChange());
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable);
        
        return "showGrid";
	}
	
	public void printTable() {
		PdfExamGridTable table = (PdfExamGridTable)request.getAttribute("table");
		table.printToHtml(getPageContext().getOut());
	}
	
	public void printLegend() {
		PdfExamGridTable table = (PdfExamGridTable)request.getAttribute("table");
		table.printLegend(getPageContext().getOut());
	}
}

