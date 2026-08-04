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
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;

public class QueryLogStatsPage extends Composite {
	protected static final GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static QueryLogStatsPage sInstance;
	private SimpleForm iPage;
	
	public QueryLogStatsPage() {
		iPage = new SimpleForm();
		iPage.addStyleName("unitime-QueryLogStatsPage");
		iPage.removeStyleName("unitime-NotPrintableBottomLine");
		initWidget(iPage);
		init();
	}
	
	public void pageLoaded() {
		LoadingWidget.getInstance().show(GWT_MSG.waitPlease());
		RPC.execute(new QueryLogStatsRequest(), new AsyncCallback<QueryLogStatsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(GWT_MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(QueryLogStatsResponse result) {
				LoadingWidget.getInstance().hide();
				iPage.clear();
				if (result.hasCharts())
					for (Charts ch: result.getCharts()) {
						iPage.addHeaderRow(ch.getTitle());
						P left = new P("chart", "left-chart");
						P right = new P("chart", "right-chart");
						iPage.addRow(left, right);
						drawChart(ch.getLeftChartAxeTitles(), eval(ch.getLeftChartData()), left.getElement());
						drawChart(ch.getRightChartAxeTitles(), eval(ch.getRightChartData()), right.getElement());
					}
				if (result.getTable() != null) {
					iPage.addHeaderRow(result.getTable().getName());
					iPage.addRow(new TableWidget(result.getTable()));
				}
			}
		});
	}
	
	protected void init() {
		ScriptInjector.fromUrl("https://www.gstatic.com/charts/loader.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(
				new Callback<Void, Exception>() {
					@Override
					public void onSuccess(Void result) {
						sInstance = QueryLogStatsPage.this;
						loadGoogleCharts();
					}
					@Override
					public void onFailure(Exception e) {
						UniTimeNotifications.error(GWT_MSG.failedToInitialize(e.getMessage()), e);
					}
				}).inject();
	}
	
	public static void __pageLoaded() {
		sInstance.pageLoaded();
	}
	
	public static native <T extends JavaScriptObject> T eval(String json) /*-{
		return eval(json);
  	}-*/;
	
	protected native void loadGoogleCharts() /*-{
		$wnd.google.charts.load('current', {'packages':['corechart']});
		$wnd.google.charts.setOnLoadCallback(@org.unitime.timetable.gwt.client.admin.QueryLogStatsPage::__pageLoaded());
	}-*/;
	
	protected native void drawChart(String[] vAxeTitles, JavaScriptObject chartData, Element chartElement) /*-{
		var data = $wnd.google.visualization.arrayToDataTable(chartData);
		var options = {
			legend: { position: 'top', maxLines: 2, },
			series: {
	          0: {targetAxisIndex: 0},
	          1: {targetAxisIndex: 0},
	          2: {targetAxisIndex: 1},
	          3: {targetAxisIndex: 1}
	        },
	        vAxes: {
	          // Adds titles to each axis.
	          0: {title: vAxeTitles[0]},
	          1: {title: vAxeTitles[1]}
	        },
	        hAxis: {slantedText: true, slantedTextAngle: 90},
			vAxis: {gridlines: {color: '#9CB0CE'}},
	      };
		var chart = new $wnd.google.visualization.LineChart(chartElement);
		chart.draw(data, options);
	}-*/;
	
	public static class QueryLogStatsRequest implements GwtRpcRequest<QueryLogStatsResponse>{
	}
	
	public static class QueryLogStatsResponse implements GwtRpcResponse {
		private List<Charts> iCharts;
		private TableInterface iTable;
		
		public boolean hasCharts() { return iCharts != null && !iCharts.isEmpty(); }
		public Charts addCharts(String title) {
			if (iCharts == null) iCharts = new ArrayList<Charts>();
			Charts ch = new Charts();
			ch.setTitle(title);
			iCharts.add(ch);
			return ch;
		}
		public List<Charts> getCharts() { return iCharts; }
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
	}
	
	public static class Charts implements IsSerializable {
		private String iTitle, iLeftChartData, iRightChartData;
		private String[] iLeftChartAxeTitles, iRightChartAxeTitles;
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		public String getLeftChartData() { return iLeftChartData; }
		public void setLeftChartData(String data) { iLeftChartData = data; }
		public String getRightChartData() { return iRightChartData; }
		public void setRightChartData(String data) { iRightChartData = data; }
		public String[] getRightChartAxeTitles() { return iRightChartAxeTitles; }
		public void setRightChartAxeTitles(String... titles) { iRightChartAxeTitles = titles; }
		public String[] getLeftChartAxeTitles() { return iLeftChartAxeTitles; }
		public void setLeftChartAxeTitles(String... titles) { iLeftChartAxeTitles = titles; }
	}
}
