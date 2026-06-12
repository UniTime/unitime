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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ReportQueueTable;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.EnrollmentAuditPdfReportFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.EnrollmentAuditPdfReportRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportFilterRequesponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExaminationPdfReportResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class EnrollmentAuditPdfReportPage extends Composite implements ValueChangeHandler<FilterInterface> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private ExaminationPdfReportFilterRequesponse iConfig;
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	
	private PageFilter iInput, iReports, iOutput, iParameters;
	
	private ReportQueueTable iQueue;
	
	public EnrollmentAuditPdfReportPage() {
		iPanel = new SimpleForm(3);
		iPanel.addStyleName("unitime-PageFilter");
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iQueue = new ReportQueueTable(QueueType.EnrollmentPdfReport).attach(iPanel, EXAM.sectReportsInProgress());
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamPdfReportsPage");
		initWidget(iRootPanel);
		
		init();
	}

	protected void init() {
		RPC.execute(new EnrollmentAuditPdfReportFilterRequest(), new AsyncCallback<ExaminationPdfReportFilterRequesponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExaminationPdfReportFilterRequesponse result) {
				iConfig = result;
				if (result.getInput() != null)
					iInput = addSection(result.getInput(), EXAM.sectInputData());
				if (result.getReports() != null)
					iReports = addSection(result.getReports(), EXAM.sectReport());
				if (result.getParameters() != null)
					iParameters = addSection(result.getParameters(), EXAM.sectParameters());
				if (result.getOutput() != null)
					iOutput = addSection(result.getOutput(), EXAM.sectOutput());
				ValueChangeEvent.fire(iInput, iInput.getValue());
				ValueChangeEvent.fire(iReports, iReports.getValue());
				ValueChangeEvent.fire(iParameters, iParameters.getValue());
				ValueChangeEvent.fire(iOutput, iOutput.getValue());
				if (iOutput.getFilterWidget("subject") != null)
					iPanel.getRowFormatter().addStyleName(iOutput.getFilterRow("subject"), "subject-line");
				
				iInput.getHeader().addButton("generate", EXAM.actionGenerateReport(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						String valid = validate();
						if (valid != null) {
							iInput.getHeader().setErrorMessage(valid);
							return;
						}
						EnrollmentAuditPdfReportRequest request = new EnrollmentAuditPdfReportRequest();
						for (FilterParameterInterface p: iConfig.getInput().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getReports().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getParameters().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						for (FilterParameterInterface p: iConfig.getOutput().getParameters())
							request.setParameter(p.getName(), (p.getValue() == null ? p.getDefaultValue() : p.getValue()));
						iInput.getHeader().showLoading();
						RPC.execute(request, new AsyncCallback<ExaminationPdfReportResponse>() {

							@Override
							public void onFailure(Throwable caught) {
								iInput.getHeader().clearMessage();
								iInput.getHeader().setErrorMessage(caught.getMessage());
								UniTimeNotifications.error(caught.getMessage(), caught);
								ToolBox.checkAccess(caught);
							}

							@Override
							public void onSuccess(ExaminationPdfReportResponse response) {
								iInput.getHeader().clearMessage();
								iQueue.refreshQueue(response.getLogId());
							}
						});
					}
				});
				iPanel.addBottomRow(iInput.getHeader().clonePanel(""));
				
				if (result.hasSolverWarning()) {
					RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
					if (cpm != null) {
						P p = new P("unitime-PageWarn");
						p.setHTML(result.getSolverWarning());
						cpm.add(p);
					}
				}
			}
		});
	}
	
	protected String validate() {
		if (iReports.getValue().getParameterValue("reports", "").isEmpty())
			return EXAM.errorNoReportSelected();
		if (!("1".equals(iInput.getValue().getParameterValue("all"))) && iInput.getValue().getParameterValue("subjects", "").isEmpty())
			return EXAM.errorNoSubjectAreaSelected();
		return null;
	}
	
	protected PageFilter addSection(FilterInterface filter, String label) {
		for (FilterParameterInterface p: filter.getParameters()) {
			String v = Window.Location.getParameter(p.getName());
			if (v != null) p.setValue(v);
		}
		PageFilter ret = new PageFilter(label, false);
		ret.addValueChangeHandler(this);
		ret.populate(iPanel, filter);
		return ret;
	}
	
	@Override
	public void onValueChange(ValueChangeEvent<FilterInterface> event) {
		FilterInterface filter = event.getValue();
		if (filter == null) return;
		if (filter.hasParameter("all")) {
			((ListBox)iInput.getFilterWidget("subjects")).setEnabled(!"1".equals(filter.getParameterValue("all")));
		}
		if (filter.hasParameter("email")) {
			boolean visible = "1".equals(filter.getParameterValue("email"));
			for (int row = iOutput.getFilterRow("addr"); row <= iOutput.getFilterRow("message"); row++)
				iPanel.getRowFormatter().setVisible(row, visible);
		}
	}
}
