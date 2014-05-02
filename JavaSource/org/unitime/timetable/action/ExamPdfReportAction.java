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

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.queue.PdfExamReportQueueItem;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;

/** 
 * @author Tomas Muller
 */
@Service("/examPdfReport")
public class ExamPdfReportAction extends Action {
    protected static Logger sLog = Logger.getLogger(ExamPdfReportAction.class);
    
    @Autowired SessionContext sessionContext;
    
    @Autowired SolverService<ExamSolverProxy> examinationSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamPdfReportForm myForm = (ExamPdfReportForm) form;
		
		sessionContext.checkPermission(Right.ExaminationPdfReports);
        
        ExamSolverProxy examSolver = examinationSolverService.getSolver();
        
        if (examSolver!=null) {
            if (ApplicationProperty.ExaminationPdfReportsCanUseSolution.isTrue()) 
                request.setAttribute(Constants.REQUEST_WARN, "Examination PDF reports are generated from the current solution (in-memory solution taken from the solver).");
            else
                request.setAttribute(Constants.REQUEST_WARN, "Examination PDF reports are generated from the saved solution (solver assignments are ignored).");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if ("Generate".equals(op)) myForm.save(sessionContext);
        myForm.load(sessionContext);
        myForm.setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser(), false));
        if (myForm.getAddress() == null)
        	myForm.setAddress(sessionContext.getUser().getEmail());
        
        if ("Generate".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
            } else {

                QueueProcessor.getInstance().add(new PdfExamReportQueueItem(
                		SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()),
                		sessionContext.getUser(),
                		(ExamPdfReportForm) myForm.clone(), request, examSolver));
            }
        }
        
        if (request.getParameter("remove") != null) {
        	QueueProcessor.getInstance().remove(Long.valueOf(request.getParameter("remove")));
        }
        
        WebTable table = getQueueTable(request);
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"examPdfReport.ord")));
        }
        
        LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
        
        return mapping.findForward("show");
	}
	
	private WebTable getQueueTable(HttpServletRequest request) {
        WebTable.setOrder(sessionContext,"examPdfReport.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.TIME_SHORT);
		String ownerId = null;
		if (!sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))
			ownerId = sessionContext.getUser().getExternalUserId();
		List<QueueItem> queue = QueueProcessor.getInstance().getItems(ownerId, null, PdfExamReportQueueItem.TYPE);
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, "Reports in progress", "examPdfReport.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
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
				delete = "<img src='images/Delete16.gif' border='0' onClick=\"if (confirm('Do you really want to remove this report?')) document.location='examPdfReport.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine(item.log().isEmpty() ? null : "onClick=\"document.location='examPdfReport.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.output() == null ? "" : "<A href='temp/"+item.output().getName()+"'>"+item.output().getName().substring(item.output().getName().lastIndexOf('.') + 1).toUpperCase()+"</A>"
					},
					new Comparable[] {
						item.getId(),
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

