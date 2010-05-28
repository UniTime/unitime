/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.queue.PdfExamReportQueueItem;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;

/** 
 * @author Tomas Muller
 */
public class ExamPdfReportAction extends Action {
    protected static Logger sLog = Logger.getLogger(ExamPdfReportAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamPdfReportForm myForm = (ExamPdfReportForm) form;
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        ExamSolverProxy examSolver = WebSolver.getExamSolver(request.getSession());
        
        if (examSolver!=null) {
            if ("true".equals(ApplicationProperties.getProperty("tmtbl.exam.pdfReports.canUseSolution","false"))) 
                request.setAttribute(Constants.REQUEST_WARN, "Examination PDF reports are generated from the current solution (in-memory solution taken from the solver).");
            else
                request.setAttribute(Constants.REQUEST_WARN, "Examination PDF reports are generated from the saved solution (solver assignments are ignored).");
        }
        
        TimetableManager mgr = TimetableManager.getManager(Web.getUser(request.getSession()));

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if ("Generate".equals(op)) myForm.save(request.getSession());
        myForm.load(request.getSession());
        
        if ("Generate".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (!errors.isEmpty()) {
                saveErrors(request, errors);
            } else {
                QueueProcessor.getInstance().add(new PdfExamReportQueueItem(
                		Session.getCurrentAcadSession(Web.getUser(request.getSession())),
                		TimetableManager.getManager(Web.getUser(request.getSession())),
                		(ExamPdfReportForm) myForm.clone(), request, examSolver));
            }                        
        }
        
        if (request.getParameter("remove") != null) {
        	QueueProcessor.getInstance().remove(Long.valueOf(request.getParameter("remove")));
        }
        
        WebTable table = getQueueTable(request, mgr.getUniqueId());
        if (table != null) {
        	request.setAttribute("table", table.printTable(WebTable.getOrder(request.getSession(),"examPdfReport.ord")));
        }
        
        return mapping.findForward("show");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, Long managerId) {
        WebTable.setOrder(request.getSession(),"examPdfReport.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = QueueProcessor.getInstance().getItems(null, null, PdfExamReportQueueItem.TYPE);
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, "Reports in progress", "examPdfReport.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (managerId.equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/Delete16.gif' border='0' onClick=\"if (confirm('Do you really want to remove this report?')) document.location='examPdfReport.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine(item.log().isEmpty() ? null : "onClick=\"document.location='examPdfReport.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwner().getName(),
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
						item.getOwner().getName(),
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
			if (log == null && item.started() != null && item.finished() == null && managerId.equals(item.getOwnerId())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				line.setBgColor("rgb(168,187,225)");
			}
		}
		return table;
	}
}

