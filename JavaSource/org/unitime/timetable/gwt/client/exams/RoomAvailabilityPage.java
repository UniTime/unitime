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
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityFilterRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.RoomAvailabilityResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassesFilterResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class RoomAvailabilityPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private PageFilter iFilter;
	
	private ClassesFilterResponse iConfig;
	
	public RoomAvailabilityPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible("1".equals(ToolBox.getSessionCookie("RoomAvailability.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("RoomAvailability.Filter", event.getValue() ? "1" : "0");
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
				search();
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
				export("room-availability.csv");
			}
		});
		iFilter.getFooter().setEnabled("exportCsv", false);
		
		iFilter.getFooter().addButton("exportXls", EXAM.buttonExportXLS(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("room-availability.xls");
			}
		});
		iFilter.getFooter().setEnabled("exportXls", false);
		
		iFilter.getFooter().addButton("exportPdf", EXAM.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("room-availability.pdf");
			}
		});
		iFilter.getFooter().setEnabled("exportPdf", false);
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-RoomAvailabilityPage");
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
					search();
			}
		});
	}

	protected void init() {
		RPC.execute(new RoomAvailabilityFilterRequest(), new AsyncCallback<ClassesFilterResponse>() {
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
				iFilter.setValue(result);
				String token = History.getToken();
				if (token != null && !(token.startsWith("A") || token.equals("back")))
					iFilter.setQuery(token, true);	
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("exportCsv", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportXls", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportPdf", iConfig.isCanExport());
			}
		});
	}
	
	protected void search() {
		final RoomAvailabilityRequest request = new RoomAvailabilityRequest();
		request.setFilter(iFilter.getValue());
		iFilter.getFooter().clearMessage();
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		iFilter.getFooter().showLoading();
		iFilter.getFooter().setEnabled("search", false);
		iFilter.getFooter().setEnabled("exportCsv", false);
		iFilter.getFooter().setEnabled("exportXls", false);
		iFilter.getFooter().setEnabled("exportPdf", false);
		iFilter.getFooter().setEnabled("add-exam", false);
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<RoomAvailabilityResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				iFilter.getFooter().setEnabled("search", true);
			}

			@Override
			public void onSuccess(RoomAvailabilityResponse table) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				UniTimeHeaderPanel header = new UniTimeHeaderPanel(table.getName());
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
										ToolBox.focusOnRow(el);
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
				iFilter.getFooter().setEnabled("search", true);
				iFilter.getFooter().setEnabled("exportCsv", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportXls", iConfig.isCanExport());
				iFilter.getFooter().setEnabled("exportPdf", iConfig.isCanExport());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String token = Window.Location.getHash();
						if (token != null && (token.startsWith("#A") || token.equals("#back"))) {
							Element e = Document.get().getElementById(token.substring(1));
							if (e != null) {
								ToolBox.scrollToElement(e);
								ToolBox.focusOnRow(e);
							}
						}
						Element e = Document.get().getElementById("back");
						if (e != null) {
							ToolBox.scrollToElement(e);
							ToolBox.focusOnRow(e);
						}
					}
				});
				RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
				if (cpm != null) {
					cpm.clear();
					if (table.hasInfoMessage()) {
						P m = new P("unitime-PageMessage");
						m.setHTML(table.getInfoMessage());
						cpm.add(m);
					}
				}
			}
		});
	}
	
	protected void export(String format) {
		String sort = ToolBox.getSessionCookie("RoomAvailability.Sort");
		ToolBox.open(GWT.getHostPageBaseURL() + "export?output=" + format + "&sid=" + iConfig.getSessionId()
			+ (sort == null ? "" : "&sort=" + sort)
			+ iFilter.getFullQuery());
	}

}
