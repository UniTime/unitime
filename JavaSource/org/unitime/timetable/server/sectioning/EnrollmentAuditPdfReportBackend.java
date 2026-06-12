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
package org.unitime.timetable.server.sectioning;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.EnrollmentAuditPdfReportRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.EnrollmentAuditPdfReportQueueItem;
import org.unitime.timetable.util.queue.QueueItem;

@GwtRpcImplements(EnrollmentAuditPdfReportRequest.class)
public class EnrollmentAuditPdfReportBackend implements GwtRpcImplementation<EnrollmentAuditPdfReportRequest, ExaminationPdfReportResponse>{
	
	@Autowired SolverServerService solverServerService;

	@Override
	public ExaminationPdfReportResponse execute(EnrollmentAuditPdfReportRequest request, SessionContext context) {
		context.checkPermission(Right.EnrollmentAuditPDFReports);
		
		for (Map.Entry<String, String> e: request.getParameters().entrySet()) {
			context.getUser().setProperty("EnrollmentAuditPdfReport." + e.getKey(), e.getValue());
		}
		
		EnrollmentAuditPdfReportForm x = new EnrollmentAuditPdfReportForm();
        x.setAll("1".equals(request.getParameter("all")));
        x.setReports(request.getParameter("reports").split(","));
        x.setMode(request.getParameter("mode", PdfLegacyExamReport.Mode.LegacyPdfLetter.name()));
        x.setSubjects(request.getParameter("subjects","-1").split(","));
        x.setEmail("1".equals(request.getParameter("email")));
        x.setAddress(request.getParameter("addr"));
        x.setCc(request.getParameter("cc"));
        x.setBcc(request.getParameter("bcc"));
        x.setMessage(request.getParameter("message"));
        x.setSubject(request.getParameter("subject"));
        x.setStudentName("1".equals(request.getParameter("studentName")));
        x.setExternalId("1".equals(request.getParameter("externalId")));
        
        QueueItem item = solverServerService.getQueueProcessor().add(new EnrollmentAuditPdfReportQueueItem(
        		SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()),
        		context.getUser(), x, null));
		
	    ExaminationPdfReportResponse response = new ExaminationPdfReportResponse();
	    if (item != null)
	    	response.setLogId(item.getId());
		return response;
	}
	
	

}
