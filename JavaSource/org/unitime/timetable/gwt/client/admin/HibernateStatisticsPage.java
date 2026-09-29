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
import org.unitime.timetable.gwt.client.admin.HibernateStatisticsPage.HibernateStatisticsRequest.Operation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;

public class HibernateStatisticsPage extends Composite {
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private boolean iDetails = false;
	
	public HibernateStatisticsPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-HibernateStatisticsPage");
		iDetails = "1".equals(ToolBox.getCookie("HibernateStatistics.Details"));
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("enable", "Enable Statistics", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(Operation.ENABLE, iDetails);
			}
		});
		iHeader.addButton("disable", "Disable Statistics", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(Operation.DISABLE, iDetails);
			}
		});
		iHeader.addButton("show-details", "Show Details", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(Operation.ENABLE, true);
			}
		});
		iHeader.addButton("hide-details", "Hide Details", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(Operation.ENABLE, false);
			}
		});
		iHeader.addButton("refresh", "Refresh", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				execute(Operation.LOAD, iDetails);
			}
		});
		iHeader.setEnabled("enable", false);
		iHeader.setEnabled("disable", false);
		iHeader.setEnabled("show-details", false);
		iHeader.setEnabled("hide-details", false);
		iHeader.setEnabled("refresh", false);
		iPanel.addHeaderRow(iHeader);
		initWidget(iPanel);
		iFooter = iHeader.clonePanel("");
		
		execute(Operation.LOAD, iDetails);
	}
	
	protected void execute(Operation op, boolean details) {
		RPC.execute(new HibernateStatisticsRequest(op, details), new AsyncCallback<HibernateStatisticsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}
			@Override
			public void onSuccess(HibernateStatisticsResponse result) {
				populate(result);
			}
		});
	}
	
	protected void populate(HibernateStatisticsResponse stats) {
		iDetails = stats.isDetails();
		ToolBox.setCookie("HibernateStatistics.Details", iDetails ? "1" : "0");
		iHeader.setEnabled("enable", !stats.isEnabled());
		iHeader.setEnabled("disable", stats.isEnabled());
		iHeader.setEnabled("show-details", stats.isEnabled() && !stats.isDetails());
		iHeader.setEnabled("hide-details", stats.isEnabled() && stats.isDetails());
		iHeader.setEnabled("refresh", stats.isEnabled());
		iHeader.setHeaderTitle(stats.isDetails() ? "Detailed Statistics" : "Summary Statistics");
		iPanel.clear();
		iHeader.clearMessage();
		iPanel.addHeaderRow(iHeader);
		if (stats.isEnabled() && stats.hasTables()) {
			boolean scroll = false;
			String token = History.getToken();
			for (TableInterface table: stats.getTables()) {
				UniTimeHeaderPanel header = null;
				if (table.hasName()) {
					header = new UniTimeHeaderPanel(table.getName());
					if (table.hasAnchor()) {
						Anchor a = new Anchor();
						a.setName(table.getAnchor());
						a.getElement().setId(table.getAnchor());
						header.insertLeft(a, true);
					}
					iPanel.addHeaderRow(header);
				}
				TableWidget w = new TableWidget(table); 
				iPanel.addRow(w);
				if (token != null && token.equals(table.getAnchor()) && header != null) {
					ToolBox.scrollToElement(header.getElement());
					scroll = true;
				}
			}
			iPanel.addBottomRow(iFooter);
			if (!scroll)
				ToolBox.scrollToElement(iPanel.getElement());
		} else {
			iHeader.setMessage("Hibernate statistics is not enabled.");
		}
	}

	public static class HibernateStatisticsRequest implements GwtRpcRequest<HibernateStatisticsResponse> {
		private Operation iOperation;
		private boolean iDetails = false;
		private boolean iPrint = false;
		
		public HibernateStatisticsRequest() {}
		public HibernateStatisticsRequest(Operation operation, boolean details) {
			iOperation = operation;
			iDetails = details;
		}
		
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		public boolean isDetails() { return iDetails; }
		public void setDetails(boolean details) { iDetails = details; }
		public boolean isPrint() { return iPrint; }
		public void setPrint(boolean print) { iPrint = print; }

		public static enum Operation {
			ENABLE, DISABLE, LOAD,
			;
		}
	}
	
	public static class HibernateStatisticsResponse implements GwtRpcResponse {
		private boolean iEnabled = false;
		private boolean iDetails = false;
		private List<TableInterface> iTables;
		
		public boolean isEnabled() { return iEnabled; }
		public void setEnabled(boolean enabled) { iEnabled = enabled; }
		public boolean isDetails() { return iDetails; }
		public void setDetails(boolean details) { iDetails = details; }
		public List<TableInterface> getTables() { return iTables; }
		public void addTable(TableInterface table) {
			if (iTables == null) iTables = new ArrayList<TableInterface>();
			iTables.add(table);
		}
		public boolean hasTables() { return iTables != null && !iTables.isEmpty(); }
	}
}
