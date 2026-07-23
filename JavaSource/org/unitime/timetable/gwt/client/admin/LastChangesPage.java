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
package org.unitime.timetable.gwt.client.admin;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.solver.PageFilter;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LinkInteface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
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
import com.google.gwt.user.client.ui.SimplePanel;

public class LastChangesPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private PageFilter iFilter;
	private LastChangesFilterResponse iConfig;
	private TableWidget iTable;
	
	public LastChangesPage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible(!"0".equals(ToolBox.getSessionCookie("LastChanges.Filter")));
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				ToolBox.setSessionCookie("LastChanges.Filter", event.getValue() ? "1" : "0");
			}
		});
		
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		iPanel.addRow(iFilter);
		
		iFilter.getFooter().addButton("search", MESSAGES.buttonSearch(), new ClickHandler() {
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
		
		iFilter.getFooter().addButton("exportCsv", MESSAGES.buttonExportCSV(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("last-changes.csv");
			}
		});
		
		iFilter.getFooter().setEnabled("exportCsv", false);
		
		iFilter.getFooter().addButton("exportXls", MESSAGES.buttonExportXLS(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("last-changes.xls");
			}
		});
		iFilter.getFooter().setEnabled("exportXls", false);
		
		iFilter.getFooter().addButton("exportPdf", MESSAGES.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("last-changes.pdf");
			}
		});
		iFilter.getFooter().setEnabled("exportPdf", false);
	
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-LastChangesPage");
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
		RPC.execute(new LastChangesFilterRequest(), new AsyncCallback<LastChangesFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(LastChangesFilterResponse result) {
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
			}
		});
	}
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final LastChangesRequest request = new LastChangesRequest();
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
		RPC.execute(request, new AsyncCallback<LastChangesResponse>() {
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
			public void onSuccess(LastChangesResponse table) {
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
				iTable = new TableWidget(table);
				p.add(iTable);
				iPanel.addRow(p);
				
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
				if (callback != null)
					callback.onSuccess(!table.hasLines());
			}
		});
	}
	
	protected void export(String format) {
		ToolBox.open(GWT.getHostPageBaseURL() + "export?output=" + format + "&sid=" + iConfig.getSessionId() + iFilter.getFullQuery()
			+ (iTable.getSortCookie() != null ? "&sort=" + iTable.getSortCookie() : ""));
	}

	public static class LastChangesFilterRequest implements GwtRpcRequest<LastChangesFilterResponse> {}
	
	public static class LastChangesFilterResponse extends org.unitime.timetable.gwt.shared.FilterInterface {
		private static final long serialVersionUID = 1L;
		private boolean iSticky = false;
		private Long iSessionId = null;

		public boolean isSticky() { return iSticky; }
		public void setSticky(boolean sticky) { iSticky = sticky; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public Long getSessionId() { return iSessionId; }
	}
	
	public static class LastChangesRequest implements GwtRpcRequest<LastChangesResponse> {
		private FilterInterface iFilter;
		
		public FilterInterface getFilter() { return iFilter; }
		public void setFilter(FilterInterface filter) { iFilter = filter; }
	}
	
	public static class LastChangesResponse extends TableInterface {
	}
}
