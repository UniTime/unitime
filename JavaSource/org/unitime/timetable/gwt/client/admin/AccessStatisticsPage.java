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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;

public class AccessStatisticsPage extends Composite {
	protected static final CourseMessages MSG = GWT.create(CourseMessages.class);
	protected static final GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat("yyyyMMddHHmm");
	private static AccessStatisticsPage sInstance;
	
	protected SimpleForm iFilter;
	protected UniTimeHeaderPanel iFilterHeader, iFilterFooter;
	protected ListBox iPage, iType, iInterval;
	protected int iFromLine, iToLine;
	protected SingleDateSelector iFromDate, iToDate;
	protected TimeSelector iFromTime, iToTime;
	protected P iPanel;
	protected AccessStatisticsRequest iRequest = new AccessStatisticsRequest();
	
	public AccessStatisticsPage() {
		sInstance = this;
		iFilter = new SimpleForm(3);
		iFilterHeader = new UniTimeHeaderPanel(MSG.filterPage());
		iFilterHeader.addButton("apply", MSG.actionFilterApply(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				apply(true);
			}
		});
		iFilterHeader.setEnabled("apply", false);
		iFilter.addHeaderRow(iFilterHeader);

		iPage = new ListBox();
		iPage.addItem(MSG.itemSelect(), "");
		for (Page page: Page.values())
			iPage.addItem(getLabel(page), page.name());
		iPage.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iRequest.setPage(iPage.getSelectedIndex() <= 0 ? null : Page.valueOf(iPage.getSelectedValue()));
			}
		});
		iFilter.addRow(MSG.filterPage(), iPage);

		iType = new ListBox();
		iType.addItem(MSG.itemSelect(), "");
		for (Type type: Type.values())
			iType.addItem(getLabel(type), type.name());
		iType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iRequest.setType(iType.getSelectedIndex() <= 0 ? null : Type.valueOf(iType.getSelectedValue()));
			}
		});
		iFilter.addRow(MSG.filterChartType(), iType);

		iInterval = new ListBox();
		iInterval.addItem(MSG.itemSelect(), "");
		for (Interval interval: Interval.values())
			iInterval.addItem(getLabel(interval), interval.name());
		iInterval.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iRequest.setInterval(iInterval.getSelectedIndex() <= 0 ? null : Interval.valueOf(iInterval.getSelectedValue()));
				intervalChanged();
			}
		});
		iFilter.addRow(MSG.filterChartInterval(), iInterval);

		iFromDate = new SingleDateSelector();
		iFromDate.clearHint();
		iFromTime = new TimeSelector();
		P from = new P("custom-from-line");
		from.add(iFromDate); from.add(iFromTime);
		iFromLine = iFilter.addRow("", MSG.filterChartFrom(), from);

		iToDate = new SingleDateSelector();
		iToDate.clearHint();
		iToTime = new TimeSelector();
		P to = new P("custom-to-line");
		to.add(iToDate); to.add(iToTime);
		iToLine = iFilter.addRow("", MSG.filterChartTo(), to);

		iFilter.getRowFormatter().setVisible(iFromLine, false);
		iFilter.getRowFormatter().setVisible(iToLine, false);
		iFilterFooter = iFilterHeader.clonePanel("");
		iFilter.addBottomRow(iFilterFooter);
		
		iPanel = new P("unitime-AcessStatisticsPage");
		iPanel.add(iFilter);
		
		init();
		initWidget(iPanel);
	}
	
	protected void intervalChanged() {
		if (Interval.CUSTOM.name().equals(iInterval.getSelectedValue())) {
			iFilter.getRowFormatter().setVisible(iFromLine, true);
			iFilter.getRowFormatter().setVisible(iToLine, true);
		} else {
			iFilter.getRowFormatter().setVisible(iFromLine, false);
			iFilter.getRowFormatter().setVisible(iToLine, false);
		}
	}
	
	protected void apply(final boolean hist) {
		if (validateRequest()) {
			LoadingWidget.getInstance().show(GWT_MSG.waitPlease());
			RPC.execute(iRequest, new AsyncCallback<AccessStatisticsResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iFilterHeader.setErrorMessage(GWT_MSG.failedToLoadData(caught.getMessage()));
					UniTimeNotifications.error(GWT_MSG.failedToLoadData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(AccessStatisticsResponse result) {
					LoadingWidget.getInstance().hide();
					iPanel.clear();
					iPanel.add(iFilter);
					if (result.hasCharts()) {
						for (ChartInterface ch: result.getCharts()) {
							P chart = new P("chart");
							iPanel.add(chart);
							drawChart(ch.getName(), eval(ch.getData()), chart.getElement());
						}
					}
					if (hist) {
						String token = iRequest.getPage() + ":" + iRequest.getType() + ":" + iRequest.getInterval();
						if (iRequest.getInterval() == Interval.CUSTOM) {
							token += ":" + sTimeStampFormat.format(iRequest.getFromDate());
							if (iRequest.getToDate() != null)
								token += ":" + sTimeStampFormat.format(iRequest.getToDate());
						}
						History.newItem(token, false);
						ToolBox.setCookie("AccessStatisticsPage.filter", token);
					}
				}
			});			
		}
	}
	
	@SuppressWarnings("deprecation")
	protected boolean validateRequest() {
		List<String> errors = new ArrayList<String>();
		if (iRequest.getPage() == null)
			errors.add(MSG.errorRequiredField(MSG.filterPage()));
		if (iRequest.getType() == null)
			errors.add(MSG.errorRequiredField(MSG.filterChartType()));
		if (iRequest.getInterval() == null)
			errors.add(MSG.errorRequiredField(MSG.filterChartInterval()));
		if (iRequest.getInterval() == Interval.CUSTOM) {
			Date fromDate = iFromDate.getValue();
			Integer fromSlot = iFromTime.getValue();
			if (fromDate == null) fromDate = new Date();
			if (fromSlot == null) fromSlot = 0;
			int hour = (fromSlot * 5) / 60;
			int min = (fromSlot * 5) % 60;
			fromDate.setHours(hour); fromDate.setMinutes(min); fromDate.setSeconds(0);
			iRequest.setFromDate(ServerDateTimeFormat.toServerDate(fromDate));
			Date toDate = iToDate.getValue();
			Integer toSlot = iToTime.getValue();
			if (toDate != null || toSlot != null) {
				if (toDate == null) toDate = new Date();
				if (toSlot == null) toSlot = 288;
				hour = (toSlot * 5) / 60;
				min = (toSlot * 5) % 60;
				toDate.setHours(hour); toDate.setMinutes(min); toDate.setSeconds(0);
				iRequest.setToDate(ServerDateTimeFormat.toServerDate(toDate));
			} else {
				iRequest.setToDate(null);
			}
		} else {
			iRequest.setFromDate(null);
			iRequest.setToDate(null);
		}

		if (errors.isEmpty())
			iFilterHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iFilterHeader.setErrorMessage(message);
		}

		return errors.isEmpty();
	}
	
	@SuppressWarnings("deprecation")
	protected void tokenChanged(String token) {
		if (token == null || token.isEmpty()) {
			iPanel.clear(); iPanel.add(iFilter);
		} else {
			try {
				String[] params = token.split(":");
				iRequest.setPage(Page.valueOf(params[0]));
				iPage.setSelectedIndex(iRequest.getPage().ordinal() + 1);
				iRequest.setType(Type.valueOf(params[1]));
				iType.setSelectedIndex(iRequest.getType().ordinal() + 1);
				iRequest.setInterval(Interval.valueOf(params[2]));
				iInterval.setSelectedIndex(iRequest.getInterval().ordinal() + 1);
				intervalChanged();
				if (params.length > 3) {
					Date date = sTimeStampFormat.parse(params[3]);
					iFromDate.setValue(date);
					iFromTime.setValue(date.getHours() * 12 + date.getMinutes() / 5);
				}
				if (params.length > 4) {
					Date date = sTimeStampFormat.parse(params[4]);
					iToDate.setValue(date);
					iToTime.setValue(date.getHours() * 12 + date.getMinutes() / 5);
				}
				apply(false);
			} catch (Exception e) {}
		}
	}
	
	protected void init() {
		ScriptInjector.fromUrl("https://www.gstatic.com/charts/loader.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(
				new Callback<Void, Exception>() {
					@Override
					public void onSuccess(Void result) {
						sInstance = AccessStatisticsPage.this;
						loadGoogleCharts();
					}
					@Override
					public void onFailure(Exception e) {
						UniTimeNotifications.error(GWT_MSG.failedToInitialize(e.getMessage()), e);
						iFilterHeader.setErrorMessage(GWT_MSG.failedToInitialize(e.getMessage()));
					}
				}).inject();
	}
	
	public void pageLoaded() {
		iFilterHeader.setEnabled("apply", true);
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				tokenChanged(event.getValue());
			}
		});
		String token = History.getToken();
		if (token == null || token.isEmpty())
			token = ToolBox.getCookie("AccessStatisticsPage.filter");
		if (token != null && !token.isEmpty())
			tokenChanged(token);
	}
	
	public static void __pageLoaded() {
		sInstance.pageLoaded();
	}
	
	public static native <T extends JavaScriptObject> T eval(String json) /*-{
		return eval(json);
  	}-*/;
	
	protected native void loadGoogleCharts() /*-{
		$wnd.google.charts.load('current', {'packages':['corechart']});
		$wnd.google.charts.setOnLoadCallback(@org.unitime.timetable.gwt.client.admin.AccessStatisticsPage::__pageLoaded());
	}-*/;
	
	protected native void drawChart(String chartName, JavaScriptObject chartData, Element chartElement) /*-{
		var data = $wnd.google.visualization.arrayToDataTable(chartData);
		var options = { width: 1200, height: 600, title: chartName, legend: { position: 'bottom' },
			chartArea: { left: 80, top: 30, width: 1100, height: 400},
			hAxis: {slantedText: true, slantedTextAngle: 90},
			vAxis: {gridlines: {color: '#9CB0CE'}}
			};
		var chart = new $wnd.google.visualization.LineChart(chartElement);
		chart.draw(data, options);
	}-*/;
	
	protected String getLabel(Page page) {
		switch(page) {
		case requests: return GWT_MSG.pageStudentCourseRequests();
		case sectioning: return GWT_MSG.pageStudentSchedulingAssistant();
		default: return page.name();
		}
	}
	
	protected String getLabel(Type type) {
		switch(type) {
		case BASIC: return MSG.chartModeBasic();
		case ACTIVE: return MSG.chartModeActive();
		case TIME: return MSG.chartModeTimes();
		default: return type.name();
		}
	}
	
	public String getLabel(Interval interval) {
		switch(interval) {
		case LAST_DAY: return MSG.chartIntervalLastDay();
		case LAST_3HOURS: return MSG.chartIntervalLast3Hours();
		case LAST_HOUR: return MSG.chartIntervalLastHour();
		case LAST_WEEK: return MSG.chartIntervalLastWeek();
		case LAST_MONTH: return MSG.chartIntervalLastMonth();
		case CUSTOM: return MSG.chartIntervalCustom();
		default: return interval.name();
		}
	}
	
	public static enum Page implements IsSerializable {
		sectioning,
		requests,
		;
	}

	public static enum Type implements IsSerializable {
		BASIC,
		ACTIVE,
		TIME,
		;
	}
	
	public static enum Interval implements IsSerializable {
		LAST_HOUR,
		LAST_3HOURS,
		LAST_DAY,
		LAST_WEEK,
		LAST_MONTH,
		CUSTOM,
		;
	}
	
	public static class AccessStatisticsRequest implements GwtRpcRequest<AccessStatisticsResponse> {
		private Page iPage;
		private Type iType;
		private Interval iInterval;
		private Date iFromDate, iToDate;
		
		public Page getPage() { return iPage; }
		public void setPage(Page page) { iPage = page; }
		public Type getType() { return iType; }
		public void setType(Type type) { iType = type; }
		public Interval getInterval() { return iInterval; }
		public void setInterval(Interval interval) { iInterval = interval; }
		public Date getFromDate() { return iFromDate; }
		public void setFromDate(Date date) { iFromDate = date; }
		public Date getToDate() { return iToDate; }
		public void setToDate(Date date) { iToDate = date; }
	}
	
	public static class AccessStatisticsResponse implements GwtRpcResponse {
		List<ChartInterface> iCharts;
		
		public boolean hasCharts() { return iCharts != null && !iCharts.isEmpty(); }
		public void addChart(String name, String data) {
			if (iCharts == null) iCharts = new ArrayList<ChartInterface>();
			ChartInterface chart = new ChartInterface();
			chart.setName(name);
			chart.setData(data);
			iCharts.add(chart);
		}
		public List<ChartInterface> getCharts() { return iCharts; }
	}
	
	public static class ChartInterface implements IsSerializable {
		String iName;
		String iData;
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getData() { return iData; }
		public void setData(String data) { iData = data; }
	}
}
