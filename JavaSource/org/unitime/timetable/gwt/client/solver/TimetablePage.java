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

import java.util.ArrayList;
import java.util.List;

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
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridFilterResponse;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridLegend;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridRequest;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Tomas Muller
 */
public class TimetablePage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private PageFilter iFilter;
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private FilterInterface iLastFilter;
	private TimetableGridResponse iLastResponse;

	public TimetablePage() {
		iFilter = new PageFilter();
		iFilter.getHeader().setCollapsible(SolverCookie.getInstance().isTimeGridFilter());
		iFilter.getHeader().addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SolverCookie.getInstance().setTimeGridFilter(event.getValue());
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
		iFilter.getFooter().addButton("print", MESSAGES.buttonPrint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				print();
			}
		});
		iFilter.getFooter().setEnabled("print", false);
		iFilter.getFooter().addButton("export", MESSAGES.buttonExportPDF(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("pdf");
			}
		});
		iFilter.getFooter().setEnabled("export", false);
		iFilter.getFooter().addButton("export-xls", MESSAGES.buttonExportXLS(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				exportData("xls");
			}
		});
		iFilter.getFooter().setEnabled("export-xls", false);
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-TimetablePage");
		initWidget(iRootPanel);
		init();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iFilter.setQuery(event.getValue(), true);
				if (iPanel.getRowCount() > 1)
					search(null);
			}
		});
	}

	protected void init() {
		iFilter.getFooter().showLoading();
		RPC.execute(new TimetableGridFilterRequest(), new AsyncCallback<TimetableGridFilterResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(TimetableGridFilterResponse result) {
				iFilter.getFooter().clearMessage();
				iFilter.setValue(result);
				iFilter.getFooter().setEnabled("search", true);
				createTriggers();
				if (!iFilter.getHeader().isCollapsible() || "1".equals(Location.getParameter("search")))
					search(null);
			}
		});
	}
	
	public static native void createTriggers()/*-{
		$wnd.refreshPage = function() {
			@org.unitime.timetable.gwt.client.solver.TimetablePage::__search()();
		};
	}-*/;
	
	public static void __search() {
		final int left = Window.getScrollLeft();
		final int top = Window.getScrollTop();
		TimetablePage page = (TimetablePage)RootPanel.get("UniTimeGWT:Body").getWidget(0);
		page.search(new AsyncCallback<Boolean>() {
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
	
	protected void search(final AsyncCallback<Boolean> callback) {
		final TimetableGridRequest request = new TimetableGridRequest();
		request.setFilter(iFilter.getValue());
		iFilter.getFooter().clearMessage();
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		iFilter.getFooter().showLoading();
		iFilter.getFooter().setEnabled("search", false);
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<TimetableGridResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().setErrorMessage(MESSAGES.failedToLoadTimetableGrid(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadTimetableGrid(caught.getMessage()), caught);
				iFilter.getFooter().setEnabled("search", true);
				if (callback != null)
					callback.onFailure(caught);
			}

			@Override
			public void onSuccess(TimetableGridResponse result) {
				LoadingWidget.hideLoading();
				iFilter.getFooter().clearMessage();
				populate(request.getFilter(), result);
				iFilter.getFooter().setEnabled("search", true);
				if (callback != null)
					callback.onSuccess(!result.getModels().isEmpty());
			}
		});
	}
	
	protected void print() {
		List<Page> pages = new ArrayList<Page>();
		int index = 0;
		for (final TimetableGridModel model: iLastResponse.getModels()) {
			final TimetableGrid grid = new TimetableGrid(iLastFilter, model, index++, 1000, iLastResponse.getWeekOffset());
			pages.add(new Page() {
				@Override
				public String getName() {
					return model.getName();
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
					return grid.getElement();
				}
				
			});
		}
		ToolBox.print(pages);
	}
	
	private void exportData(String format) {
		String query = "output=timetable." + format + iFilter.getQuery();
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
	
	protected void populate(FilterInterface filter, TimetableGridResponse response) {
		iLastFilter = filter;
		iLastResponse = response;
		iFilter.getFooter().setEnabled("print", false);
		iFilter.getFooter().setEnabled("export", false);
		iFilter.getFooter().setEnabled("export-xls", false);
		for (int row = iPanel.getRowCount() - 1; row > 0; row--)
			iPanel.removeRow(row);
		
		if (response.getModels().isEmpty()) {
			iFilter.getFooter().setMessage(MESSAGES.errorTimetableGridNoDataReturned());
			iFilter.getFooter().setEnabled("print", false);
			iFilter.getFooter().setEnabled("export", false);
			iFilter.getFooter().setEnabled("export-xls", false);
			return;
		}
		
		UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.sectTimetables());
		iPanel.addHeaderRow(header);
		
		int index = 0;
		int width = ToolBox.getClientWidth() - 20;
		P timetables = new P("timetables");
		timetables.getElement().getStyle().clearOverflow();
		for (TimetableGridModel model: response.getModels()) {
			timetables.add(new TimetableGrid(filter, model, index++, width, response.getWeekOffset()));
		}
		ScrollPanel scroll = new ScrollPanel(timetables);
		scroll.addStyleName("scroll-panel");
		iPanel.addRow(scroll);
		iFilter.getFooter().setEnabled("print", true);
		iFilter.getFooter().setEnabled("export", true);
		iFilter.getFooter().setEnabled("export-xls", true);
		
		if (!response.getAssignedLegend().isEmpty() || !response.getNotAssignedLegend().isEmpty()) {
			iPanel.addHeaderRow(new UniTimeHeaderPanel(MESSAGES.sectLegend()));
			P legend = new P("legend");
			if (!response.getAssignedLegend().isEmpty()) {
				P hl = new P("header-line");
				P h = new P("text"); h.setText(MESSAGES.legendAssignedClasses());
				hl.add(h);legend.add(hl);
				for (TimetableGridLegend lg: response.getAssignedLegend()) {
					P ll = new P("legend-line");
					P b = new P("box"); b.getElement().getStyle().setBackgroundColor(lg.getColor());
					P t = new P("text"); t.setHTML(lg.getLabel());
					ll.add(b); ll.add(t);legend.add(ll);
				}
			}
			if (!response.getNotAssignedLegend().isEmpty()) {
				P hl = new P("header-line");
				P h = new P("text"); h.setText(MESSAGES.legendFreeTimes());
				hl.add(h);legend.add(hl);
				for (TimetableGridLegend lg: response.getNotAssignedLegend()) {
					P ll = new P("legend-line");
					P b = new P("box"); b.getElement().getStyle().setBackgroundColor(lg.getColor());
					P t = new P("text"); t.setHTML(lg.getLabel());
					ll.add(b); ll.add(t);legend.add(ll);
				}
			}
			iPanel.addRow(legend);
		}
		
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
	}
}
