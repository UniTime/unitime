/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.Collection;
import java.util.Vector;

import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AcademicSessionSelector extends Composite implements AcademicSessionProvider {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);

	private Label iSessionLabel;
	private Hidden iSessionId;
	
	private DialogBox iDialog;
	private WebTable iSessions;
	private String iName = null;
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private Vector<AcademicSessionChangeHandler> iAcademicSessionChangeHandlers = new Vector<AcademicSessionChangeHandler>();
	
	public AcademicSessionSelector() {
		iSessionLabel = new Label(MESSAGES.sessionSelectorNoSession(), false);
		iSessionLabel.setStyleName("unitime-SessionSelector");
		
		iSessionId = new Hidden("sessionId");
		
		VerticalPanel vertical = new VerticalPanel();
		vertical.add(iSessionLabel);
		vertical.add(iSessionId);
		
		Label hint = new Label(MESSAGES.sessionSelectorHint());
		hint.setStyleName("unitime-Hint");
		vertical.add(hint);
		
		iDialog = new MyDialogBox();
		iDialog.setText(MESSAGES.sessionSelectorSelect());
		iDialog.setAnimationEnabled(true);
		iDialog.setAutoHideEnabled(false);
		iDialog.setGlassEnabled(true);
		iDialog.setModal(true);
		
		iSessions = new WebTable();
		iSessions.setHeader(
				new WebTable.Row(
						new WebTable.Cell(MESSAGES.colYear(), 1, "80"),
						new WebTable.Cell(MESSAGES.colTerm(), 1, "80"),
						new WebTable.Cell(MESSAGES.colCampus(), 1, "100")
						));
		iSessions.setEmptyMessage(MESSAGES.sessionSelectorLoading());

		
		iDialog.add(iSessions);
		
		ClickHandler ch = new ClickHandler() {
			public void onClick(ClickEvent event) {
				selectSession();
			}
		};
		
		iSessionLabel.addClickHandler(ch);
		hint.addClickHandler(ch);
		
		iSessions.addRowClickHandler(new WebTable.RowClickHandler() {
			public void onRowClick(WebTable.RowClickEvent event) {
				rowSelected(event.getRow());
			}
		});
		
		
		initWidget(vertical);
	}
	
	private void rowSelected(WebTable.Row row) {
		iDialog.hide();
		iSessionLabel.setText(MESSAGES.sessionSelectorLabel(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(2).getValue()));
		iName = MESSAGES.sessionName(row.getCell(0).getValue(), row.getCell(1).getValue(), row.getCell(2).getValue());
		boolean changed = !row.getId().equals(iSessionId.getValue());
		iSessionId.setValue(row.getId());
		if (changed) {
			AcademicSessionChangeEvent changeEvent = new AcademicSessionChangeEvent(Long.valueOf(row.getId()));
			for (AcademicSessionChangeHandler handler: iAcademicSessionChangeHandlers)
				handler.onAcademicSessionChange(changeEvent);
		}
		iSessions.setSelectedRow(row.getRowIdx());
	}
	
	public void selectSession() {
		iDialog.setAutoHideEnabled(getAcademicSessionId()!=null);
		iSectioningService.listAcademicSessions(new AsyncCallback<Collection<String[]>>() {
			public void onSuccess(Collection<String[]> result) {
				WebTable.Row[] records = new WebTable.Row[result.size()];
				int idx = 0;
				int lastSession = -1;
				for (String[] record: result) {
					WebTable.Row row = new WebTable.Row(record[1], record[2], record[3]);
					row.setId(record[0]);
					if (row.getId().equals(iSessionId.getValue())) lastSession = idx;
					records[idx++] = row;
				}
				iSessions.setData(records);
				if (records.length == 1) iSessions.setSelectedRow(0);
				if (lastSession >= 0) iSessions.setSelectedRow(lastSession);
				if (records.length == 1)
					rowSelected(iSessions.getRows()[0]);
				else
					iDialog.center();
			}
			
			public void onFailure(Throwable caught) {
				iSessions.clearData(true);
				iSessions.setEmptyMessage(caught.getMessage());
				iDialog.center();
			}
		});
	}
	
	public void selectSession(final Long sessionId, final AsyncCallback<Boolean> callback) {
		if (sessionId == null) {
			selectSession();
			callback.onSuccess(false);
		} else if (sessionId.equals(getAcademicSessionId())) {
			callback.onSuccess(true);
		} else {
			iSectioningService.listAcademicSessions(new AsyncCallback<Collection<String[]>>() {
				public void onSuccess(Collection<String[]> result) {
					for (String[] record: result) {
						if (sessionId.toString().equals(record[0])) {
							selectSession(record);
							callback.onSuccess(true);
							return;
						}
					}
					selectSession();
					callback.onSuccess(false);
				}
				public void onFailure(Throwable caught) {
					iSessions.clearData(true);
					iSessions.setEmptyMessage(caught.getMessage());
					iDialog.center();
					callback.onSuccess(false);
				}
			});
		}
	}
	
	public void selectSession(String[] session) {
		if (session == null) {
			iSessionLabel.setText(MESSAGES.sessionSelectorNoSession());
			iSessionId.setValue(null);
			iName = null;
			return;
		}
		iSessionLabel.setText(MESSAGES.sessionSelectorLabel(session[1], session[2], session[3]));
		iName = MESSAGES.sessionName(session[1], session[2], session[3]);
		iSessionId.setValue(session[0]);
	}
	
	public Long getAcademicSessionId() {
		try {
			return Long.valueOf(iSessionId.getValue());
		} catch (Exception e) {
			return null;
		}
	}
	
	public String getAcademicSessionName() {
		return iName;
	}
	
	public static class AcademicSessionChangeEvent implements AcademicSessionProvider.AcademicSessionChangeEvent {
		private Long iSessionId;
		private AcademicSessionChangeEvent(Long sessionId) {
			iSessionId = sessionId;
		}
		public Long getNewAcademicSessionId() {
			return iSessionId;
		}
	}
	
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
		iAcademicSessionChangeHandlers.add(handler);
	}
	
	private class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetType((Event) event.getNativeEvent()) == Event.ONKEYUP) {
				if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_DOWN) {
					iSessions.setSelectedRow(iSessions.getSelectedRow()+1);
				} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_UP) {
					iSessions.setSelectedRow(iSessions.getSelectedRow()==0?iSessions.getRowsCount()-1:iSessions.getSelectedRow()-1);
				} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ENTER && iSessions.getSelectedRow()>=0) {
					rowSelected(iSessions.getRows()[iSessions.getSelectedRow()]);
				}  else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE && getAcademicSessionId()!=null) {
					iDialog.hide();
				}
			}
		}
	}
}
