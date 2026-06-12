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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm;
import org.unitime.timetable.form.EnrollmentAuditPdfReportForm.RegisteredReport;
import org.unitime.timetable.form.ExamPdfReportForm;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.EnrollmentAuditPdfReportFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequesponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(EnrollmentAuditPdfReportFilterRequest.class)
public class EnrollmentAuditPdfReportFilterBackend implements GwtRpcImplementation<EnrollmentAuditPdfReportFilterRequest, ExaminationPdfReportFilterRequesponse>{
	protected static ExaminationMessages MESSAGES = Localization.create(ExaminationMessages.class);

	@Override
	public ExaminationPdfReportFilterRequesponse execute(EnrollmentAuditPdfReportFilterRequest request, SessionContext context) {
		context.checkPermission(Right.EnrollmentAuditPDFReports);
		ExaminationPdfReportFilterRequesponse filter = new ExaminationPdfReportFilterRequesponse();
		
		FilterParameterInterface allSubjects = new FilterParameterInterface();
		allSubjects.setName("all");
		allSubjects.setLabel(MESSAGES.filterSubjectAreas());
		allSubjects.setSuffix(MESSAGES.checkReportAllSubjectAreas());
		allSubjects.setType("boolean");
		allSubjects.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.all", "1"));
		allSubjects.setCollapsible(false);
		filter.addInput(allSubjects);
		
		FilterParameterInterface subjectArea = new FilterParameterInterface();
		subjectArea.setName("subjects");
		subjectArea.setType("list");
		subjectArea.setMultiSelect(true);
		subjectArea.setCollapsible(false);
		subjectArea.setLabel(MESSAGES.filterSubjectAreas());
		if (ApplicationProperty.OfferingsFilterSubjectTitle.isTrue())
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getLabel());
		else
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectArea.addOption(subject.getUniqueId().toString(), subject.getSubjectAreaAbbreviation());
		subjectArea.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.subjects"));
		filter.addInput(subjectArea);
		
		FilterParameterInterface reports = new FilterParameterInterface();
		reports.setName("reports");
		reports.setMultiSelect(true);
		reports.setType("checkboxlist");
		reports.setLabel(MESSAGES.filterReport());
		for (RegisteredReport report: RegisteredReport.values())
			reports.addOption(report.name(), EnrollmentAuditPdfReportForm.getReportName(report));
		reports.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.reports"));
		reports.setCollapsible(false);
		filter.addReport(reports);
		
		filter.addParameter(createToggle(context, "externalId", MESSAGES.checkDisplayStudentId(), true, MESSAGES.propAllReports()));
		filter.addParameter(createToggle(context, "studentName", MESSAGES.checkDisplayStudentName(), false, MESSAGES.propAllReports()));

		FilterParameterInterface mode = new FilterParameterInterface();
		mode.setName("mode");
		mode.setType("list");
		mode.setLabel(MESSAGES.propReportFormat());
		for (PdfLegacyExamReport.Mode m: PdfLegacyExamReport.Mode.values()) {
			mode.addOption(m.name(), ExamPdfReportForm.getModeLabel(m));
		}
		mode.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.mode", PdfLegacyExamReport.Mode.LegacyPdfLetter.name()));
		mode.setCollapsible(false);
		filter.addOutput(mode);
		filter.addOutput(createToggle(context, "email", MESSAGES.checkReportDeliveryEmail(), false, MESSAGES.propReportDelivery()));
		FilterParameterInterface address = new FilterParameterInterface();
		address.setName("addr");
		address.setType("textarea");
		address.setPrefix(MESSAGES.propEmailSubject());
		address.setLabel(MESSAGES.propReportDelivery());
		address.setMaxLength(4);
		address.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.addr"));
		address.setCollapsible(false);
		filter.addOutput(address);
		FilterParameterInterface cc = new FilterParameterInterface();
		cc.setName("cc");
		cc.setType("textarea");
		cc.setPrefix(MESSAGES.propEmailCC());
		cc.setLabel(MESSAGES.propReportDelivery());
		cc.setMaxLength(2);
		cc.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.cc"));
		cc.setCollapsible(false);
		filter.addOutput(cc);
		FilterParameterInterface bcc = new FilterParameterInterface();
		bcc.setName("bcc");
		bcc.setType("textarea");
		bcc.setPrefix(MESSAGES.propEmailBCC());
		bcc.setLabel(MESSAGES.propReportDelivery());
		bcc.setMaxLength(3);
		bcc.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.bcc"));
		bcc.setCollapsible(false);
		filter.addOutput(bcc);
		FilterParameterInterface subject = new FilterParameterInterface();
		subject.setName("subject");
		subject.setType("text");
		subject.setPrefix(MESSAGES.propEmailSubject());
		subject.setLabel(MESSAGES.propReportDelivery());
		subject.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.subject"));
		subject.setCollapsible(false);
		filter.addOutput(subject);
		FilterParameterInterface message = new FilterParameterInterface();
		message.setName("message");
		message.setType("textarea");
		message.setPrefix(MESSAGES.propEmailMessage());
		message.setLabel(MESSAGES.propReportDelivery());
		message.setMaxLength(10);
		message.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport.message"));
		message.setCollapsible(false);
		filter.addOutput(message);
		
		return filter;
	}

	protected FilterParameterInterface createToggle(SessionContext context, String name, String label, Boolean defaultValue, String section) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(section);
		toggle.setType("boolean");
		toggle.setSuffix(label);
		toggle.setDefaultValue(context.getUser().getProperty("EnrollmentAuditPdfReport." + name, defaultValue == null ? null : defaultValue ? "1" : "0"));
		toggle.setCollapsible(false);
		return toggle;
	}

}
