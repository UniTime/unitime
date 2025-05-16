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
package org.unitime.timetable.gwt.client.instructor;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsFilterRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsFilterResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class InstructorsPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private PageFilter iFilter;
	private UniTimeHeaderPanel iFooter;
	
	private InstructorsFilterResponse iConfig;
	
	public InstructorsPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible("1".equals(ToolBox.getSessionCookie("Instructors.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("Instructors.Filter", event.getValue() ? "1" : "0");
			}
		});
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		
		iFilter.getFooter().addButton("search", COURSE.actionSearchInstructors(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String token = iFilter.getQuery();
				if (!History.getToken().equals(token))
					History.newItem(token, false);
				search(null);
			}
		});
		iFilter.getFooter().setEnabled("search", false);
		iFilter.getFooter().getButton("search").setAccessKey(COURSE.accessSearchInstructors().charAt(0));
		iFilter.getFooter().getButton("search").setTitle(COURSE.titleSearchInstructors(COURSE.accessSearchInstructors()));
		iFilter.setSubmitCommand(new Command() {
			@Override
			public void execute() {
				if (iFilter.getFooter().isEnabled("search"))
					iFilter.getFooter().getButton("search").click();
			}
		});
		
		iFilter.getFooter().addButton("exportCsv", COURSE.actionExportCsv(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("instructors.csv");
			}
		});
		
		iFilter.getFooter().getButton("exportCsv").setAccessKey(COURSE.accessExportCsv().charAt(0));
		iFilter.getFooter().getButton("exportCsv").setTitle(COURSE.titleExportCsv(COURSE.accessExportCsv()));
		iFilter.getFooter().setEnabled("exportCsv", false);
		
		iFilter.getFooter().addButton("exportXls", COURSE.actionExportXls(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("instructors.xls");
			}
		});
		iFilter.getFooter().getButton("exportXls").setAccessKey(COURSE.accessExportXls().charAt(0));
		iFilter.getFooter().getButton("exportXls").setTitle(COURSE.titleExportXls(COURSE.accessExportXls()));
		iFilter.getFooter().setEnabled("exportXls", false);
		
		iFilter.getFooter().addButton("exportPdf", COURSE.actionExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("instructors.pdf");
			}
		});
		iFilter.getFooter().getButton("exportPdf").setAccessKey(COURSE.accessExportPdf().charAt(0));
		iFilter.getFooter().getButton("exportPdf").setTitle(COURSE.titleExportPdf(COURSE.accessExportPdf()));
		iFilter.getFooter().setEnabled("exportPdf", false);
		
		iFilter.getFooter().addButton("surveyXls", COURSE.actionExportSurveysXLS(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("instructor-surveys.xls");
			}
		});
		iFilter.getFooter().setEnabled("surveyXls", false);
		
		iFilter.getFooter().addButton("manage-instructors", COURSE.actionManageInstructorList(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FilterInterface filter = iFilter.getValue();
				LoadingWidget.showLoading(MESSAGES.waitLoadingPage());
				String subjectId = filter.getParameterValue("subjectArea", "");
				if (subjectId.indexOf(',') > 0)
					subjectId = subjectId.substring(0, subjectId.indexOf(','));
				ToolBox.open(GWT.getHostPageBaseURL() + "instructorListUpdate.action");
			}
		});
		iFilter.getFooter().getButton("manage-instructors").setAccessKey(COURSE.accessManageInstructorList().charAt(0));
		iFilter.getFooter().getButton("manage-instructors").setTitle(COURSE.titleManageInstructorList(COURSE.accessManageInstructorList()));
		iFilter.getFooter().setEnabled("manage-instructors", false);
		
		iFilter.getFooter().addButton("add", COURSE.actionAddNewInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				FilterInterface filter = iFilter.getValue();
				LoadingWidget.showLoading(MESSAGES.waitLoadingPage());
				String subjectId = filter.getParameterValue("subjectArea", "");
				if (subjectId.indexOf(',') > 0)
					subjectId = subjectId.substring(0, subjectId.indexOf(','));
				ToolBox.open(GWT.getHostPageBaseURL() + "instructorAdd.action");
			}
		});
		iFilter.getFooter().getButton("add").setAccessKey(COURSE.accessAddNewInstructor().charAt(0));
		iFilter.getFooter().getButton("add").setTitle(COURSE.titleAddNewInstructor(COURSE.accessAddNewInstructor()));
		iFilter.getFooter().setEnabled("add", false);
		
		iFooter = iFilter.getFooter().clonePanel();
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-InstructorsPage");
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
	}
	
	protected void init() {
		RPC.execute(new InstructorsFilterRequest(), new AsyncCallback<InstructorsFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(InstructorsFilterResponse result) {
				iConfig = result;
				for (FilterParameterInterface p: result.getParameters()) {
					String v = Window.Location.getParameter(p.getName());
					if (v != null) p.setValue(v);
				}
				iFilter.getFooter().clearMessage();
				iFilter.setValue(result);
				String token = History.getToken();
				if (token != null && !(token.startsWith("A") || token.equals("back")))
					iFilter.setQuery(token, true);	
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("add", false);
				iFilter.getFooter().setEnabled("exportCsv", false);
				iFilter.getFooter().setEnabled("exportXls", false);
				iFilter.getFooter().setEnabled("exportPdf", false);
				iFilter.getFooter().setEnabled("surveyXls", false);
				iFilter.getFooter().setEnabled("manage-instructors", false);
				if (!iFilter.getValue().getParameterValue("deptId", "").isEmpty())
					search(null);
			}
		});
	}
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final InstructorsRequest request = new InstructorsRequest();
		request.setBackId(Window.Location.getParameter("backId"));
		request.setBackType(Window.Location.getParameter("backType"));
		request.setFilter(iFilter.getValue());
		iFilter.getFooter().clearMessage();
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		iFilter.getFooter().showLoading();
		iFilter.getFooter().setEnabled("search", false);
		iFilter.getFooter().setEnabled("add", false);
		iFilter.getFooter().setEnabled("exportCsv", false);
		iFilter.getFooter().setEnabled("exportXls", false);
		iFilter.getFooter().setEnabled("exportPdf", false);
		iFilter.getFooter().setEnabled("surveyXls", false);
		iFilter.getFooter().setEnabled("manage-instructors", false);
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<InstructorsResponse>() {
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
			public void onSuccess(InstructorsResponse result) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				if (result.getTable() != null) {
					UniTimeHeaderPanel header = new UniTimeHeaderPanel(result.getTable().getName());
					iPanel.addHeaderRow(header);
					P p = new P(iConfig.isSticky() ? "unitime-StickyTable" : "unitime-ScrollTable");
					p.getElement().getStyle().clearPosition();
					p.getElement().getStyle().clearOverflow();
					p.add(new TableWidget(result.getTable()));
					iPanel.addRow(p);
					iPanel.addBottomRow(iFooter);
				}
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("add", result.hasOperation("add-instructor"));
				iFilter.getFooter().setEnabled("exportCsv", result.hasOperation("export"));
				iFilter.getFooter().setEnabled("exportXls", result.hasOperation("export"));
				iFilter.getFooter().setEnabled("exportPdf", result.hasOperation("export"));
				iFilter.getFooter().setEnabled("surveyXls", result.hasOperation("export-surveys"));
				iFilter.getFooter().setEnabled("manage-instructors", result.hasOperation("manage-instructors"));
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String token = Window.Location.getHash();
						if (token != null && (token.startsWith("#A") || token.equals("#back"))) {
							Element e = Document.get().getElementById(token.substring(1));
							if (e != null) ToolBox.scrollToElement(e);
						}
						Element e = Document.get().getElementById("back");
						if (e != null)
							ToolBox.scrollToElement(e);
					}
				});
				if (callback != null)
					callback.onSuccess(result.getTable() != null);
			}
		});
	}
	
	protected void export(String format) {
		String sort = ToolBox.getSessionCookie("Instructors.Sort");
		ToolBox.open(GWT.getHostPageBaseURL() + "export?output=" + format + "&sid=" + iConfig.getSessionId() +
				(sort == null || sort.isEmpty() ? "" : "&sort=" + URL.encodeQueryString(sort)) +
				iFilter.getFullQuery());
	}

}
