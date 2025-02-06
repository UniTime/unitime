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

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.queue.PdfExamReportQueueItem;
import org.unitime.timetable.util.queue.QueueItem;

/** 
 * @author Tomas Muller
 */
@Action(value = "examPdfReport", results = {
		@Result(name = "show", type = "tiles", location = "examPdfReport.tiles")
	})
@TilesDefinition(name = "examPdfReport.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination PDF Reports"),
		@TilesPutAttribute(name = "body", value = "/exam/pdfReport.jsp"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})

public class ExamPdfReportAction extends UniTimeAction<ExamPdfReportForm> {
	private static final long serialVersionUID = -2736074007763182603L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	private String remove;
	
	public String getRemove() { return remove; }
	public void setRemove(String remove) { this.remove = remove; }

	public String execute() throws Exception {
		sessionContext.checkPermission(Right.ExaminationPdfReports);
		
		ExamSolverProxy examSolver = getExaminationSolverService().getSolver();

    	if (form == null) {
	    	form = new ExamPdfReportForm();
	    	form.reset();
	    	if (examSolver != null) form.setExamType(examSolver.getExamTypeId());
	    }
	    
    	if (form.getOp() != null) op = form.getOp();
        
        if (examSolver!=null) {
            if (ApplicationProperty.ExaminationPdfReportsCanUseSolution.isTrue()) 
                request.setAttribute(Constants.REQUEST_WARN, MSG.warnExamPdfReportsUsingSolution());
            else
                request.setAttribute(Constants.REQUEST_WARN, MSG.warnEamPdfReportsUsingSaved());
        }
        
        // Read operation to be performed
        String op = (form.getOp()!=null?form.getOp():request.getParameter("op"));
        if (MSG.actionGenerateReport().equals(op)) form.save(sessionContext);
        form.load(sessionContext);
        if (form.getAddress() == null)
        	form.setAddress(sessionContext.getUser().getEmail());
        
        if (MSG.actionGenerateReport().equals(op)) {
            form.validate(this);
            if (!hasFieldErrors()) {
            	getSolverServerService().getQueueProcessor().add(new PdfExamReportQueueItem(
                		SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()),
                		sessionContext.getUser(),
                		(ExamPdfReportForm) form.clone(), request, examSolver));
            }
        }
        
        if (remove != null && !remove.isEmpty()) {
        	getSolverServerService().getQueueProcessor().remove(remove);
        }
        
        WebTable table = getQueueTable(request);
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"examPdfReport.ord")));
        }
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable);
        
        return "show";
	}
	
	private WebTable getQueueTable(HttpServletRequest request) {
        WebTable.setOrder(sessionContext,"examPdfReport.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		String ownerId = null;
		if (!sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
			ownerId = sessionContext.getUser().getExternalUserId();
		List<QueueItem> queue = getSolverServerService().getQueueProcessor().getItems(ownerId, null, PdfExamReportQueueItem.TYPE);
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, MSG.sectReportsInProgress(), "examPdfReport.action?ord=%%",
				new String[] {
						MSG.colTaskName(),
						MSG.colTaskStatus(),
						MSG.colTaskProgress(),
						MSG.colTaskOwner(),
						MSG.colTaskSession(),
						MSG.colTaskCreated(),
						MSG.colTaskStarted(),
						MSG.colTaskFinished(),
						MSG.colTaskOutput()},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			if (item.getSession() == null) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (sessionContext.getUser().getExternalUserId().equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('" + MSG.questionDeleteReportInProgress() + "')) document.location='examPdfReport.action?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine(item.log().isEmpty() ? null : "onClick=\"document.location='examPdfReport.action?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() ? "<A href='"+item.getOutputLink()+"'>"+item.getOutputName().substring(item.getOutputName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.created().getTime(),
						item.status(),
						item.progress(),
						item.getOwnerName(),
						item.getSession(),
						item.created().getTime(),
						item.started() == null ? Long.MAX_VALUE : item.started().getTime(),
						item.finished() == null ? Long.MAX_VALUE : item.finished().getTime(),
						null
					});
			if (log != null && log.equals(item.getId().toString())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
			if (log == null && item.started() != null && item.finished() == null && sessionContext.getUser().getExternalUserId().equals(item.getOwnerId())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
}

