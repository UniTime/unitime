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
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsFilterResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.DistributionsResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LinkInteface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;

public class ExamDistributionsPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private PageFilter iFilter;
	
	private DistributionsFilterResponse iConfig;

	public ExamDistributionsPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible("1".equals(ToolBox.getSessionCookie("ExamDistributions.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("ExamDistributions.Filter", event.getValue() ? "1" : "0");
			}
		});
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		
		iFilter.getFooter().addButton("search", EXAM.actionSearchDistributionPreferences(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String token = iFilter.getQuery();
				if (!History.getToken().equals(token))
					History.newItem(token, false);
				search(null);
			}
		});
		iFilter.getFooter().setEnabled("search", false);
		iFilter.getFooter().getButton("search").setAccessKey(EXAM.accessSearchDistributionPreferences().charAt(0));
		iFilter.getFooter().getButton("search").setTitle(EXAM.titleSearchDistributionPreferences(EXAM.accessSearchDistributionPreferences()));
		iFilter.setSubmitCommand(new Command() {
			@Override
			public void execute() {
				if (iFilter.getFooter().isEnabled("search"))
					iFilter.getFooter().getButton("search").click();
			}
		});
		
		iFilter.getFooter().addButton("exportCsv", EXAM.actionExportCsv(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exam-distributions.csv");
			}
		});
		
		iFilter.getFooter().getButton("exportCsv").setAccessKey(EXAM.accessExportCsv().charAt(0));
		iFilter.getFooter().getButton("exportCsv").setTitle(EXAM.titleExportCsv(EXAM.accessExportCsv()));
		iFilter.getFooter().setEnabled("exportCsv", false);
		
		iFilter.getFooter().addButton("exportXls", EXAM.actionExportXls(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exam-distributions.xls");
			}
		});
		iFilter.getFooter().getButton("exportXls").setAccessKey(EXAM.accessExportXls().charAt(0));
		iFilter.getFooter().getButton("exportXls").setTitle(EXAM.titleExportXls(EXAM.accessExportXls()));
		iFilter.getFooter().setEnabled("exportXls", false);
		
		iFilter.getFooter().addButton("exportPdf", EXAM.actionExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("exam-distributions.pdf");
			}
		});
		iFilter.getFooter().getButton("exportPdf").setAccessKey(EXAM.accessExportPdf().charAt(0));
		iFilter.getFooter().getButton("exportPdf").setTitle(EXAM.titleExportPdf(EXAM.accessExportPdf()));
		iFilter.getFooter().setEnabled("exportPdf", false);
	
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-DistributionsPage");
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
		RPC.execute(new ExamDistributionsFilterRequest(), new AsyncCallback<DistributionsFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionsFilterResponse result) {
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
				iFilter.getFooter().setEnabled("exportCsv", true);
				iFilter.getFooter().setEnabled("exportXls", true);
				iFilter.getFooter().setEnabled("exportPdf", true);
				boolean autoSearch = false;
				if (!iFilter.getValue().getParameterValue("subjectArea", "").isEmpty()) {
					autoSearch = true;
				} else if (iConfig.getMaxSubjectsToSearchAutomatically() != null) {
					ListBox list = ((ListBox)iFilter.getFilterWidget("subjectArea"));
					if (list.getItemCount() <= iConfig.getMaxSubjectsToSearchAutomatically()) {
						autoSearch = true;
						for (int i = 0; i < list.getItemCount(); i++)
							list.setItemSelected(i, true);
					}
					DomEvent.fireNativeEvent(Document.get().createChangeEvent(), list);
				}
				if (autoSearch)
					search(null);
			}
		});
	}
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final ExamDistributionsRequest request = new ExamDistributionsRequest();
		request.setBackId(Window.Location.getParameter("backId"));
		request.setBackType(Window.Location.getParameter("backType"));
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
		RPC.execute(request, new AsyncCallback<DistributionsResponse>() {
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
			public void onSuccess(DistributionsResponse result) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				if (result.hasTables())
					for (TableInterface table: result.getTables()) {
						UniTimeHeaderPanel header = new UniTimeHeaderPanel(table.getName());
						if (iConfig.isCanAdd()) {
							header.addButton("add-distribution", EXAM.actionAddDistributionPreference(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent e) {
									ToolBox.open(GWT.getHostPageBaseURL() + "examDistributionAdd");
								}
							});
							header.getButton("add-distribution").setAccessKey(EXAM.accessAddDistributionPreference().charAt(0));
							header.getButton("add-distribution").setTitle(EXAM.titleAddDistributionPreference(EXAM.accessAddDistributionPreference()));					
						}
						if (table.hasAnchor()) {
							Anchor a = new Anchor(); a.setName(table.getAnchor()); a.getElement().setId(table.getAnchor());
							header.insertLeft(a, false);
						}
						if (table.getLinks() != null) {
							for (final LinkInteface link: table.getLinks()) {
								Anchor a = new Anchor(link.getText());
								if (link.getHref().startsWith("#")) {
									a.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent e) {
											e.preventDefault();
											Element el = Document.get().getElementById(link.getHref().substring(1));
											if (el != null) {
												ToolBox.scrollToElement(el);
												History.newItem(link.getHref().substring(1), false);
											}
										}
									});
								} else {
									a.setHref(link.getHref());
								}
								header.insertRight(a, false);
							}
						}
						iPanel.addHeaderRow(header);
						P p = new P(iConfig.isSticky() ? "unitime-StickyTable" : "unitime-ScrollTable");
						p.getElement().getStyle().clearPosition();
						p.getElement().getStyle().clearOverflow();
						p.add(new TableWidget(table));
						iPanel.addRow(p);
						UniTimeHeaderPanel footer = header.clonePanel("");
						iPanel.addBottomRow(footer);
					}
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("exportCsv", true);
				iFilter.getFooter().setEnabled("exportXls", true);
				iFilter.getFooter().setEnabled("exportPdf", true);
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
					callback.onSuccess(result.hasTables());
			}
		});
	}
	
	protected void export(String format) {
		String sort = ToolBox.getSessionCookie("ExamDistributions.Sort");
		ToolBox.open(GWT.getHostPageBaseURL() + "export?output=" + format + "&sid=" + iConfig.getSessionId() +
				(sort == null ? "" : "&sort=" + sort) +
				iFilter.getFullQuery());
	}

}
