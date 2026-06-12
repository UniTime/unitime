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
package org.unitime.timetable.server.exams;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.queue.PdfExamReportQueueItem;
import org.unitime.timetable.util.queue.QueueItem;

@GwtRpcImplements(ExaminationPdfReportRequest.class)
public class ExaminationPdfReportBackend implements GwtRpcImplementation<ExaminationPdfReportRequest, ExaminationPdfReportResponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	
	@Autowired SolverServerService solverServerService;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public ExaminationPdfReportResponse execute(ExaminationPdfReportRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationPdfReports);
		
		String examType = request.getParameter("examType");
		ExamType type = null;
		try {
			type = ExamTypeDAO.getInstance().get(Long.valueOf(examType));
		} catch (Exception e) {}
		if (type == null)
			type = ExamType.findByReference(examType);
		if (type == null)
			throw new GwtRpcException(MESSAGES.messageNoExamType());
		
		for (Map.Entry<String, String> e: request.getParameters().entrySet()) {
			if ("examType".equals(e.getKey()))
				context.setAttribute(SessionAttribute.ExamType, Long.valueOf(e.getValue()));
			else
				context.getUser().setProperty("ExamPdfReport." + e.getKey(), e.getValue());
		}
		
		ExamSolverProxy examSolver = null;
		if (ApplicationProperty.ExaminationPdfReportsCanUseSolution.isTrue()) {
			examSolver = examinationSolverService.getSolver();
			if (examSolver != null && !examSolver.getExamTypeId().equals(type.getUniqueId()))
				examSolver = null;
		}
		
    	ExamPdfReportForm x = new ExamPdfReportForm();
        x.setAll("1".equals(request.getParameter("all")));
        x.setReports(request.getParameter("reports").split(","));
        x.setMode(request.getParameter("mode", PdfLegacyExamReport.Mode.LegacyPdfLetter.name()));
        x.setSubjects(request.getParameter("subjects","-1").split(","));
        x.setDispRooms("1".equals(request.getParameter("dispRooms")));
        x.setNoRoom(request.getParameter("noRoom"));
        x.setDirect("1".equals(request.getParameter("direct")));
        x.setM2d("1".equals(request.getParameter("m2d")));
        x.setBtb("1".equals(request.getParameter("btb")));
        x.setLimit(request.getParameter("limit"));
        x.setTotals("1".equals(request.getParameter("totals")));
        x.setRoomCodes(request.getParameter("roomCodes"));
        x.setEmail("1".equals(request.getParameter("email")));
        x.setAddress(request.getParameter("addr"));
        x.setCc(request.getParameter("cc"));
        x.setBcc(request.getParameter("bcc"));
        x.setEmailDeputies("1".equals(request.getParameter("emailDeputies")));
        x.setMessage(request.getParameter("message"));
        x.setSubject(request.getParameter("subject"));
        x.setDispLimit("1".equals(request.getParameter("dispLimit")));
        x.setSince(request.getParameter("since"));
        x.setEmailInstructors("1".equals(request.getParameter("emailInstructors")));
        x.setEmailStudents("1".equals(request.getParameter("emailStudents")));
        x.setItype("1".equals(request.getParameter("itype")));
        x.setClassSchedule("1".equals(request.getParameter("classSchedule")));
        x.setIgnoreEmptyExams("1".equals(request.getParameter("ignempty")));
	    x.setExamType(type.getUniqueId());
	    x.setDispNote("1".equals(request.getParameter("dispNote")));
	    x.setCompact("1".equals(request.getParameter("compact")));
	    x.setRoomDispNames("1".equals(request.getParameter("roomDispNames")));
		
	    QueueItem item = solverServerService.getQueueProcessor().add(new PdfExamReportQueueItem(
        		SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()),
        		context.getUser(), x, null, examSolver));
		
	    ExaminationPdfReportResponse response = new ExaminationPdfReportResponse();
	    if (item != null)
	    	response.setLogId(item.getId());
		return response;
	}

}
