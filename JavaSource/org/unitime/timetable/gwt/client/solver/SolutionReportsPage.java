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
package org.unitime.timetable.gwt.client.solver;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.ToolBox.Page;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.TableInterface;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolverReportsResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class SolutionReportsPage extends P {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	public SolutionReportsPage() {
		super("unitime-SolutionReportsPage");
		createTriggers();
		init(null);
	}

	protected void init(final AsyncCallback<Boolean> callback) {
		clear();
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(new SolverReportsRequest(), new AsyncCallback<SolverReportsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
				if (callback != null) callback.onFailure(caught);
			}

			@Override
			public void onSuccess(SolverReportsResponse result) {
				LoadingWidget.hideLoading();
				populate(result);
				if (callback != null) callback.onSuccess(result.hasTables());
			}
		});
	}
	
	public static native void createTriggers()/*-{
		$wnd.refreshPage = function() {
			@org.unitime.timetable.gwt.client.solver.SolutionReportsPage::__search()();
		};
	}-*/;
	
	public static void __search() {
		final int left = Window.getScrollLeft();
		final int top = Window.getScrollTop();
		SolutionReportsPage page = (SolutionReportsPage)RootPanel.get("UniTimeGWT:Body").getWidget(0);
		page.init(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				if (result)
					Window.scrollTo(left, top);
			}
		});
	}
	
	protected void print(final TableInterface data) {
		final DataTable table = new DataTable(data);
		Element headerRow = table.getRowFormatter().getElement(0);
		Element tableElement = table.getElement();
		Element thead = DOM.createTHead();
		tableElement.insertFirst(thead);
		headerRow.getParentElement().removeChild(headerRow);
		thead.appendChild(headerRow);
		Page page = new Page() {
			@Override
			public String getName() {
				return data.getName();
			}
			@Override
			public String getUser() {
				return "";
			}
			@Override
			public String getSession() {
				return "";
			}
			@Override
			public Element getBody() {
				return table.getElement();
			}
		};
		ToolBox.print(page);
	}
	
	protected void populate(SolverReportsResponse response) {
		clear();
		
		RootPanel cpm = RootPanel.get("UniTimeGWT:CustomPageMessages");
		if (cpm != null) {
			cpm.clear();
			if (response.hasPageMessages()) {
				for (final PageMessage pm: response.getPageMessages()) {
					P p = new P(pm.getType() == PageMessageType.ERROR ? "unitime-PageError" : pm.getType() == PageMessageType.WARNING ? "unitime-PageWarn" : "unitime-PageMessage");
					p.setHTML(pm.getMessage());
					if (pm.hasUrl()) {
						p.addStyleName("unitime-ClickablePageMessage");
						p.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								if (pm.hasUrl()) ToolBox.open(GWT.getHostPageBaseURL() + pm.getUrl());
							}
						});
					}
					cpm.add(p);
				}
			}
		}
		
		if (response.hasTables()) {
			for (final TableInterface data: response.getTables()) {
				SimpleForm form = new SimpleForm();
				form.removeStyleName("unitime-NotPrintableBottomLine");
				form.addStyleName("single-report");
				UniTimeHeaderPanel header = new UniTimeHeaderPanel(data.getName());
				header.addButton("print", MESSAGES.buttonPrint(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						print(data);
					}
				});
				form.addHeaderRow(header);
				final DataTable table = new DataTable(data);
				header.addButton("exportCSV", MESSAGES.buttonExportCSV(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						exportData("csv", data.getTableId(), table.getValue());
					}
				});
				header.addButton("exportPDF", MESSAGES.buttonExportPDF(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						exportData("pdf", data.getTableId(), table.getValue());
					}
				});

				form.addRow(table);
				if (data.hasErrorMessage()) header.setErrorMessage(data.getErrorMessage());
				if (data.isShowPrefLegend())
					form.addRow(new PreferenceLegend(response.getPreferences()));
				if (data.hasColumnDescriptions()) {
					for (TableInterface.TableHeaderIterface h: data.getHeader()) {
						if (h.hasDescription())
							form.addRow(h.getName(), new HTML(h.getDescription()));
					}
				}
				add(form);
			}
		}
	}
	
	public void exportData(String format, String tableId, Integer sort) {
		String query = "output=solution-reports." + format + "&sort=" + (sort == null ? 0 : sort.intValue()) + "&table=" + tableId;
		RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
			}
		});
	}
}
