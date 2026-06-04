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
package org.unitime.timetable.gwt.client.exams;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamGridTable;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridLegend;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class ExamGridPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private PageFilter iFilter;
	private String iLastExamType = null;
	private String iLastResource = null;
	
	private ClassesFilterResponse iConfig;

	public ExamGridPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible(!"0".equals(ToolBox.getSessionCookie("ExamGrid.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("ExamGrid.Filter", event.getValue() ? "1" : "0");
			}
		});
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		
		iFilter.getFooter().addButton("search", EXAM.buttonSearch(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String token = iFilter.getQuery();
				if (!History.getToken().equals(token))
					History.newItem(token, false);
				search(null);
			}
		});
		iFilter.getFooter().setEnabled("search", false);
		iFilter.setSubmitCommand(new Command() {
			@Override
			public void execute() {
				if (iFilter.getFooter().isEnabled("search"))
					iFilter.getFooter().getButton("search").click();
			}
		});
		
		iFilter.getFooter().addButton("exportCsv", EXAM.buttonExportCSV(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exams-grid.csv");
			}
		});
		iFilter.getFooter().setEnabled("exportCsv", false);
		
		iFilter.getFooter().addButton("exportXls", EXAM.buttonExportXLS(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exams-grid.xls");
			}
		});
		iFilter.getFooter().setEnabled("exportXls", false);
		
		iFilter.getFooter().addButton("exportPdf", EXAM.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exams-grid.pdf");
			}
		});
		iFilter.getFooter().setEnabled("exportPdf", false);
	
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamGridPage");
		initWidget(iRootPanel);
		init();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String token = event.getValue();
				if (event.getValue().startsWith("A") || event.getValue().equals("back"))
					token = "";
				iFilter.setQuery(token, true);
				if (iPanel.getRowCount() > 1)
					search(null);
			}
		});
		
		iFilter.addValueChangeHandler(new ValueChangeHandler<FilterInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<FilterInterface> e) {
				String examType = e.getValue().getParameterValue("examType");
				String resource = e.getValue().getParameterValue("resource");
				if ((examType != null && !examType.isEmpty() && !examType.equals(iLastExamType)) ||
					(resource != null && !resource.isEmpty() && !resource.equals(iLastResource))) {
					iLastExamType = examType;
					iLastResource = resource;
					ExamGridFilterRequest r = new ExamGridFilterRequest();
					r.setFilter(e.getValue());
					RPC.execute(r, new AsyncCallback<ClassesFilterResponse>() {
						@Override
						public void onFailure(Throwable caught) {}
						@Override
						public void onSuccess(ClassesFilterResponse result) {
							iFilter.setValue(result);
						}
					});
				}
			}
		});
	}
	
	protected void init() {
		RPC.execute(new ExamGridFilterRequest(), new AsyncCallback<ClassesFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ClassesFilterResponse result) {
				iConfig = result;
				for (FilterParameterInterface p: result.getParameters()) {
					String v = Window.Location.getParameter(p.getName());
					if (v != null) p.setValue(v);
				}
				iFilter.getFooter().clearMessage();
				iLastExamType = result.getParameterValue("examType");
				iLastResource = result.getParameterValue("resource");
				iFilter.setValue(result);
				String token = History.getToken();
				if (token != null && !(token.startsWith("A") || token.equals("back")))
					iFilter.setQuery(token, true);	
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("exportCsv", false);
				iFilter.getFooter().setEnabled("exportXls", false);
				iFilter.getFooter().setEnabled("exportPdf", false);
			}
		});
	}
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final ExamGridRequest request = new ExamGridRequest();
		request.setFilter(iFilter.getValue());
		iFilter.getFooter().clearMessage();
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		iFilter.getFooter().showLoading();
		iFilter.getFooter().setEnabled("search", false);
		iFilter.getFooter().setEnabled("exportCsv", false);
		iFilter.getFooter().setEnabled("exportXls", false);
		iFilter.getFooter().setEnabled("exportPdf", false);
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<ExamGridTable>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				iFilter.getFooter().setEnabled("search", true);
				if (callback != null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(ExamGridTable result) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				if (result == null || !result.hasModels()) {
					iFilter.getFooter().setErrorMessage(MESSAGES.errorTimetableGridNoDataReturned());
					iFilter.getFooter().setEnabled("search", true);
					return;
				}
				ScrollPanel sp = new ScrollPanel(new ExamGrid(request.getFilter(), result));
				sp.addStyleName("grid-scroll");
				iPanel.addRow(sp);
				if (!result.getAssignedLegend().isEmpty() || !result.getNotAssignedLegend().isEmpty()) {
					iPanel.addHeaderRow(new UniTimeHeaderPanel(MESSAGES.sectLegend()));
					P legend = new P("legend");
					if (!result.getAssignedLegend().isEmpty()) {
						P hl = new P("header-line");
						P h = new P("text"); h.setText(EXAM.propAssignedExaminations());
						hl.add(h);legend.add(hl);
						for (TimetableGridLegend lg: result.getAssignedLegend()) {
							P ll = new P("legend-line");
							P b = new P("box"); b.getElement().getStyle().setBackgroundColor(lg.getColor());
							P t = new P("text"); t.setHTML(lg.getLabel());
							ll.add(b); ll.add(t);legend.add(ll);
						}
					}
					if (!result.getNotAssignedLegend().isEmpty()) {
						P hl = new P("header-line");
						P h = new P("text"); h.setText(EXAM.propFreeTimes());
						hl.add(h);legend.add(hl);
						for (TimetableGridLegend lg: result.getNotAssignedLegend()) {
							P ll = new P("legend-line");
							P b = new P("box"); b.getElement().getStyle().setBackgroundColor(lg.getColor());
							P t = new P("text"); t.setHTML(lg.getLabel());
							ll.add(b); ll.add(t);legend.add(ll);
						}
					}
					iPanel.addRow(legend);
				}
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("exportCsv", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportXls", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportPdf", iConfig.isCanExport());
			}
		});
	}
	
	protected void export(String format) {
		ToolBox.open(GWT.getHostPageBaseURL() + "export?output=" + format + "&sid=" + iConfig.getSessionId()
			+ iFilter.getFullQuery());
	}

}