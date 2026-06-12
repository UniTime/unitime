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

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ScriptInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ReportQueueTable extends UniTimeTable<QueueItemInterface> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private static DateTimeFormat sTS = DateTimeFormat.getFormat(CONSTANTS.timeStampFormatShort());
	
	private QueueType iType;

	private int iLastSelectedRow = -1;
	private HTML iLog;
	private int iQueueRow, iLogRow;
	private SimpleForm iPanel;	
	private UniTimeHeaderPanel iQueueHeader, iLogHeader;

	public ReportQueueTable(QueueType type) {
		iType = type;
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(MESSAGES.colName()));
		header.add(new UniTimeTableHeader(MESSAGES.colStatus()));
		header.add(new UniTimeTableHeader(MESSAGES.colProgress()));
		header.add(new UniTimeTableHeader(MESSAGES.colOwner()));
		header.add(new UniTimeTableHeader(MESSAGES.colSession()));
		header.add(new UniTimeTableHeader(MESSAGES.colCreated()));
		header.add(new UniTimeTableHeader(MESSAGES.colStarted()));
		header.add(new UniTimeTableHeader(MESSAGES.colFinished()));
		header.add(new UniTimeTableHeader(MESSAGES.colOutput()));
		header.add(new UniTimeTableHeader(""));
		addRow(null, header);
		setAllowSelection(true);
		addStyleName("unitime-QueueTable");
		
		refreshQueue(null, null);
		new Timer() {
			@Override
			public void run() {
				refreshQueue(null, null);
			}
		}.scheduleRepeating(5000);
		
		addMouseClickListener(new MouseClickListener<ScriptInterface.QueueItemInterface>() {
			@Override
			public void onMouseClick(TableEvent<QueueItemInterface> event) {
				if (iLastSelectedRow >= 1)
					setSelected(iLastSelectedRow, false);
				if (event.getData() != null && iLastSelectedRow != event.getRow()) {
					setSelected(event.getRow(), true);
					showLog(event.getData());
					iLastSelectedRow = event.getRow();
				} else {
					showLog(null);
					iLastSelectedRow = -1;
				}
			}
		});
	}
	
	public ReportQueueTable attach(SimpleForm panel, String headerLabel) {
		iPanel = panel;
		if (iQueueHeader == null) {
			iQueueHeader = new UniTimeHeaderPanel(headerLabel);
			iQueueHeader.addButton("refresh", MESSAGES.buttonRefresh(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					refreshQueue();
				}
			});
			iLogHeader = new UniTimeHeaderPanel();
			iLog = new HTML();
			iLog.addStyleName("unitime-QueueLog");
		}
		iQueueRow = iPanel.addHeaderRow(iQueueHeader);
		iPanel.addRow(this);
		
		iLogRow = iPanel.addHeaderRow(iLogHeader);
		iPanel.addRow(iLog);
		return this;
	}
	
	public void refreshQueue() {
		refreshQueue(null, null);
	}
	
	public void refreshQueue(final String selectId) {
		refreshQueue(null, selectId);
	}
	
	private void refreshQueue(String deleteId, final String selectId) {
		RPC.execute(new ScriptInterface.GetQueueTableRpcRequest(deleteId).setType(iType), new AsyncCallback<GwtRpcResponseList<QueueItemInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<QueueItemInterface> result) {
				populate(result, selectId);
			}
		});
	}
	
	private void populate(GwtRpcResponseList<QueueItemInterface> queue, String selectId) {
		if (getSelectedRow() > 0 && selectId == null) {
			QueueItemInterface q = getData(getSelectedRow());
			if (q != null) selectId = q.getId();
		}
		QueueItemInterface selectedQueue = null;
		clearTable(1);
		iLastSelectedRow = -1;
		
		for (final QueueItemInterface q: queue) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(q.getName()));
			line.add(new Label(q.getStatus()));
			line.add(new Label(q.getProgress()));
			line.add(new Label(q.getOwner()));
			line.add(new Label(q.getSession()));
			line.add(new Label(q.getCreated() == null ? "" : sTS.format(q.getCreated())));
			line.add(new Label(q.getStarted() == null ? "" : sTS.format(q.getStarted())));
			line.add(new Label(q.getFinished() == null ? "" : sTS.format(q.getFinished())));
			if (q.getOtuput() != null) {
				line.add(new Anchor(q.getOtuput().substring(1 + q.getOtuput().lastIndexOf('.')), q.getOtuputLink()));
			} else {
				line.add(new Label(""));
			}
			if (q.isCanDelete()) {
				ImageButton delete = new ImageButton(RESOURCES.delete());
				delete.setTitle(MESSAGES.titleDeleteRow());
				delete.setAltText(MESSAGES.titleDeleteRow());
				delete.getElement().getStyle().setCursor(Cursor.POINTER);
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						refreshQueue(q.getId(), null);
					}
				});
				line.add(delete);
			} else {
				line.add(new Label(""));
			}
			
			addRow(q, line);
			
			if (selectId != null && selectId.equals(q.getId())) {
				setSelected(getRowCount() - 1, true);
				iLastSelectedRow = getRowCount() - 1;
				selectedQueue = q;
			}
		}
		
		onChange(getRowCount() > 1);
		showLog(selectedQueue);
	}
	
	protected void onChange(boolean visible) {
		iPanel.getRowFormatter().setVisible(iQueueRow, visible);
		iPanel.getRowFormatter().setVisible(iQueueRow + 1, visible);
	}
	
	protected void showLog(QueueItemInterface item) {
		if (item == null || item.getLog() == null || item.getLog().isEmpty()) {
			iPanel.getRowFormatter().setVisible(iLogRow, false);
			iPanel.getRowFormatter().setVisible(iLogRow + 1, false);
		} else {
			iLogHeader.setHeaderTitle(MESSAGES.sectScriptLog(item.getName()));
			iPanel.getRowFormatter().setVisible(iLogRow, true);
			iPanel.getRowFormatter().setVisible(iLogRow + 1, true);
			iLog.setHTML(item.getLog());
		}
	}

}
