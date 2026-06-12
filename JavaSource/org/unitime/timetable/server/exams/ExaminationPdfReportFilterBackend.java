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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.form.ExamPdfReportForm.RegisteredReport;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequesponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;

@GwtRpcImplements(ExaminationPdfReportFilterRequest.class)
public class ExaminationPdfReportFilterBackend implements GwtRpcImplementation<ExaminationPdfReportFilterRequest, ExaminationPdfReportFilterRequesponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);
	protected static GwtMessages GWT = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	@Override
	public ExaminationPdfReportFilterRequesponse execute(ExaminationPdfReportFilterRequest request, SessionContext context) {
		context.checkPermission(Right.ExaminationPdfReports);
		ExaminationPdfReportFilterRequesponse filter = new ExaminationPdfReportFilterRequesponse();
		
		FilterParameterInterface examType = new FilterParameterInterface();
		examType.setName("examType");
		examType.setType("list");
		examType.setMultiSelect(false);
		examType.setCollapsible(false);
		examType.setLabel(MESSAGES.propExamType());
        for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamView, DepartmentStatusType.Status.ExamTimetable))
        	examType.addOption(type.getUniqueId().toString(), type.getLabel());
        Object et = context.getAttribute(SessionAttribute.ExamType);
        examType.setDefaultValue(et == null ? null : et.toString());
        if (!examType.hasDefaultValue() && examType.hasOptions())
        	examType.setDefaultValue(examType.getOptions().get(0).getValue());
		filter.addInput(examType);
		
		FilterParameterInterface allSubjects = new FilterParameterInterface();
		allSubjects.setName("all");
		allSubjects.setLabel(MESSAGES.propExamSubject());
		allSubjects.setSuffix(MESSAGES.checkReportAllSubjectAreas());
		allSubjects.setType("boolean");
		allSubjects.setDefaultValue(context.getUser().getProperty("ExamPdfReport.all", "1"));
		allSubjects.setCollapsible(false);
		filter.addInput(allSubjects);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjects");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.propExamSubject());
		if (ApplicationProperty.OfferingsFilterSubjectTitle.isTrue())
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		else
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getSubjectAreaAbbreviation());
		subjectArea.setDefaultValue(context.getUser().getProperty("ExamPdfReport.subjects"));
		filter.addInput(subjectArea);
		
		FilterParameterInterface reports = new FilterParameterInterface();
		reports.setName("reports");
		reports.setType("checkboxlist");
		reports.setLabel(MESSAGES.filterReport());
		for (RegisteredReport report: RegisteredReport.values())
			reports.addOption(report.name(), ExamPdfReportForm.getReportName(report));
		reports.setDefaultValue(context.getUser().getProperty("ExamPdfReport.reports"));
		reports.setCollapsible(false);
		filter.addReport(reports);
		
		filter.addParameter(createToggle(context, "itype", MESSAGES.checkDisplayInstructionalType(), ApplicationProperty.ExaminationReportsShowInstructionalType.isTrue(), MESSAGES.propAllReports()));
		filter.addParameter(createToggle(context, "ignempty", MESSAGES.checkSkipExamsWithNoEnrollment(), true, MESSAGES.propAllReports()));
		filter.addParameter(createToggle(context, "roomDispNames", MESSAGES.checkUseRoomDisplayNames(), true, MESSAGES.propAllReports()));
		
		filter.addParameter(createToggle(context, "direct", MESSAGES.checkDisplayDirectConflicts(), true, MESSAGES.propConflictReports()));
		filter.addParameter(createToggle(context, "m2d", MESSAGES.checkDisplayMoreThan2ExamsADayConflicts(), true, MESSAGES.propConflictReports()));
		filter.addParameter(createToggle(context, "btb", MESSAGES.checkDisplayBackToBackConflicts(), false, MESSAGES.propConflictReports()));

		filter.addParameter(createToggle(context, "dispRooms", MESSAGES.checkDisplayRooms(), true, MESSAGES.propReportsWithRooms()));
		FilterParameterInterface noRoom = new FilterParameterInterface();
		noRoom.setName("noRoom");
		noRoom.setType("text");
		noRoom.setPrefix(MESSAGES.propNoRoomLabel());
		noRoom.setLabel(MESSAGES.propReportsWithRooms());
		noRoom.setMaxLength(11);
		noRoom.setDefaultValue(context.getUser().getProperty("ExamPdfReport.noRoom", ApplicationProperty.ExaminationsNoRoomText.value()));
		noRoom.setCollapsible(false);
		filter.addParameter(noRoom);
		
		filter.addParameter(createToggle(context, "totals", MESSAGES.checkDisplayTotals(), true, MESSAGES.propPeriodChartReport()));
		FilterParameterInterface limit = new FilterParameterInterface();
		limit.setName("limit");
		limit.setType("integer");
		limit.setPrefix(MESSAGES.propExamLimit());
		limit.setLabel(MESSAGES.propPeriodChartReport());
		limit.setMaxLength(4);
		limit.setDefaultValue(context.getUser().getProperty("ExamPdfReport.limit"));
		limit.setCollapsible(false);
		filter.addParameter(limit);
		FilterParameterInterface roomCodes = new FilterParameterInterface();
		roomCodes.setName("roomCodes");
		roomCodes.setType("text");
		roomCodes.setPrefix(MESSAGES.propRoomCodes());
		roomCodes.setLabel(MESSAGES.propPeriodChartReport());
		roomCodes.setMaxLength(200);
		roomCodes.setDefaultValue(context.getUser().getProperty("ExamPdfReport.roomCodes", ApplicationProperty.ExaminationRoomCode.value()));
		roomCodes.setCollapsible(false);
		filter.addParameter(roomCodes);
		filter.addParameter(createToggle(context, "compact", MESSAGES.checkReportCompactSize(), false, MESSAGES.propPeriodChartReport()));
		
		filter.addParameter(createToggle(context, "dispLimit", MESSAGES.checkDisplayLimitsAndEnrollments(), true, MESSAGES.propVerificationReport()));
		filter.addParameter(createToggle(context, "dispNote", MESSAGES.checkDisplayClassScheduleNotes(), false, MESSAGES.propVerificationReport()));
		
		filter.addParameter(createToggle(context, "classSchedule", MESSAGES.checkIncludeClassSchedule(), true, MESSAGES.propIndividualReports()));
		FilterParameterInterface since = new FilterParameterInterface();
		since.setName("since");
		since.setType("date");
		since.setPrefix(MESSAGES.propReportStartDate());
		since.setLabel(MESSAGES.propIndividualReports());
		since.setDefaultValue(context.getUser().getProperty("ExamPdfReport.since"));
		since.setCollapsible(false);
		filter.addParameter(since);
		
		FilterParameterInterface mode = new FilterParameterInterface();
		mode.setName("mode");
		mode.setType("list");
		mode.setLabel(MESSAGES.propReportFormat());
		for (PdfLegacyExamReport.Mode m: PdfLegacyExamReport.Mode.values()) {
			mode.addOption(m.name(), ExamPdfReportForm.getModeLabel(m));
		}
		mode.setDefaultValue(context.getUser().getProperty("ExamPdfReport.mode", PdfLegacyExamReport.Mode.LegacyPdfLetter.name()));
		mode.setCollapsible(false);
		filter.addOutput(mode);
		filter.addOutput(createToggle(context, "email", MESSAGES.checkReportDeliveryEmail(), false, MESSAGES.propReportDelivery()));
		FilterParameterInterface address = new FilterParameterInterface();
		address.setName("addr");
		address.setType("textarea");
		address.setPrefix(MESSAGES.propEmailSubject());
		address.setLabel(MESSAGES.propReportDelivery());
		address.setMaxLength(4);
		address.setDefaultValue(context.getUser().getProperty("ExamPdfReport.addr"));
		address.setCollapsible(false);
		filter.addOutput(address);
		if (context.hasPermission(Right.DepartmentIndependent)) {
			filter.addOutput(createToggle(context, "emailDeputies", MESSAGES.checkAllInvolvedDepartmentScheduleManagers(), false, MESSAGES.propReportDelivery()));
			filter.addOutput(createToggle(context, "emailInstructors", MESSAGES.checkSendIndividualInstructorReportsToInstructors(), false, MESSAGES.propReportDelivery()));
			filter.addOutput(createToggle(context, "emailStudents", MESSAGES.checkSendIndividualStudentReportsToStudents(), false, MESSAGES.propReportDelivery()));
			filter.getOutput("emailDeputies").setPrefix(" ");
			filter.getOutput("emailInstructors").setPrefix(" ");
			filter.getOutput("emailStudents").setPrefix(" ");
		}
		FilterParameterInterface cc = new FilterParameterInterface();
		cc.setName("cc");
		cc.setType("textarea");
		cc.setPrefix(MESSAGES.propEmailCC());
		cc.setLabel(MESSAGES.propReportDelivery());
		cc.setMaxLength(2);
		cc.setDefaultValue(context.getUser().getProperty("ExamPdfReport.cc"));
		cc.setCollapsible(false);
		filter.addOutput(cc);
		FilterParameterInterface bcc = new FilterParameterInterface();
		bcc.setName("bcc");
		bcc.setType("textarea");
		bcc.setPrefix(MESSAGES.propEmailBCC());
		bcc.setLabel(MESSAGES.propReportDelivery());
		bcc.setMaxLength(3);
		bcc.setDefaultValue(context.getUser().getProperty("ExamPdfReport.bcc"));
		bcc.setCollapsible(false);
		filter.addOutput(bcc);
		FilterParameterInterface subject = new FilterParameterInterface();
		subject.setName("subject");
		subject.setType("text");
		subject.setPrefix(MESSAGES.propEmailSubject());
		subject.setLabel(MESSAGES.propReportDelivery());
		subject.setDefaultValue(context.getUser().getProperty("ExamPdfReport.subject"));
		subject.setCollapsible(false);
		filter.addOutput(subject);
		FilterParameterInterface message = new FilterParameterInterface();
		message.setName("message");
		message.setType("textarea");
		message.setPrefix(MESSAGES.propEmailMessage());
		message.setLabel(MESSAGES.propReportDelivery());
		message.setMaxLength(10);
		message.setDefaultValue(context.getUser().getProperty("ExamPdfReport.message"));
		message.setCollapsible(false);
		filter.addOutput(message);
		
        if (examinationSolverService.getSolver() != null) {
            if (ApplicationProperty.ExaminationPdfReportsCanUseSolution.isTrue()) 
            	filter.setSolverWarning(MESSAGES.warnExamPdfReportsUsingSolution());
            else
            	filter.setSolverWarning(MESSAGES.warnEamPdfReportsUsingSaved());
        }

		
		return filter;
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue, String section) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(section);
		toggle.setType("boolean");
		toggle.setSuffix(label);
		toggle.setDefaultValue(context.getUser().getProperty("ExamPdfReport." + name, defaultValue == null ? null : defaultValue ? "1" : "0"));
		toggle.setCollapsible(false);
		return toggle;
	}
}
