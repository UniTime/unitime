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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.page.InfoPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Tomas Muller
 */
public class AcademicSessionSelector implements AcademicSessionProvider {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);

	private InfoPanel iPanel;
	private AcademicSessionInfo iSession = null;
	
	private DialogBox iDialog;
	private UniTimeTable<AcademicSessionInfo> iSessions;
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private Vector<AcademicSessionChangeHandler> iAcademicSessionChangeHandlers = new Vector<AcademicSessionChangeHandler>();
	private StudentSectioningPage.Mode iMode;
	
	public AcademicSessionSelector(InfoPanel panel, StudentSectioningPage.Mode mode) {
		iMode = mode;
		iPanel = panel;
		iPanel.setPreventDefault(true);
		iPanel.setVisible(true);
		iPanel.setText(MESSAGES.sessionSelectorNoSession());
		iPanel.setHint(MESSAGES.sessionSelectorHint());
		iPanel.setAriaLabel(ARIA.sessionNoSession());
		iPanel.setInfo(null);
		
		iDialog = new MyDialogBox();
		iDialog.setText(MESSAGES.sessionSelectorSelect());
		iDialog.setAnimationEnabled(true);
		iDialog.setAutoHideEnabled(false);
		iDialog.setGlassEnabled(true);
		iDialog.setModal(true);
		
		iSessions = new UniTimeTable<AcademicSessionInfo>();
		iSessions.addRow(null,
				new UniTimeTableHeader(MESSAGES.colYear()),
				new UniTimeTableHeader(MESSAGES.colTerm()),
				new UniTimeTableHeader(MESSAGES.colCampus()));
		iSessions.addRow(null, new EmptyTableCell(MESSAGES.sessionSelectorLoading()));
		iSessions.setAllowSelection(true);
		iSessions.setWidth("100%");
		
		iDialog.add(iSessions);
		
		ClickHandler ch = new ClickHandler() {
			public void onClick(ClickEvent event) {
				selectSession();
			}
		};
		
		iPanel.setClickHandler(ch);
		
		iSessions.addMouseClickListener(new UniTimeTable.MouseClickListener<AcademicSessionInfo>() {

			@Override
			public void onMouseClick(UniTimeTable.TableEvent<AcademicSessionInfo> event) {
				if (event.getData() != null)
					rowSelected(event.getRow(), event.getData());
			}
			
		});
	}
	
	private void rowSelected(int row, AcademicSessionInfo session) {
		iDialog.hide();
		iPanel.setText(MESSAGES.sessionSelectorLabel(session.getYear(), session.getTerm(), session.getCampus()));
		iPanel.setAriaLabel(ARIA.sessionCurrent(session.getYear(), session.getTerm(), session.getCampus()));
		selectSession(session, true);
		if (iSessions.getSelectedRow() >= 0)
			iSessions.setSelected(iSessions.getSelectedRow(), false);
		iSessions.setSelected(row, true);
		AriaStatus.getInstance().setText(ARIA.sessionSelectorDialogSelected(session.getYear(), session.getTerm(), session.getCampus()));
	}
	
	public void selectSession() {
		iDialog.setAutoHideEnabled(getAcademicSessionId()!=null);
		iSectioningService.listAcademicSessions(iMode.isSectioning(), new AsyncCallback<Collection<AcademicSessionInfo>>() {
			public void onSuccess(Collection<AcademicSessionInfo> result) {
				iSessions.clearTable(1);
				int row = 1;
				int lastSession = -1;
				for (AcademicSessionInfo session: result) {
					iSessions.addRow(session, 
							new Label(session.getYear()),
							new Label(session.getTerm()),
							new Label(session.getCampus()));
					if (session.equals(iSession)) lastSession = row;
					row++;
				}
				if (result.size() == 1) iSessions.setSelected(1, true);
				else if (lastSession >= 0) iSessions.setSelected(lastSession, true);
				if (result.size() == 1)
					rowSelected(1, iSessions.getData(1));
				else {
					if (iSessions.getSelectedRow() >= 0) {
						AcademicSessionInfo session = iSessions.getData(iSessions.getSelectedRow());
						AriaStatus.getInstance().setText(ARIA.sessionSelectorDialogOpenedWithSelection(iSessions.getSelectedRow(), result.size(), session.getYear(), session.getTerm(), session.getCampus()));
					} else {
						AriaStatus.getInstance().setText(ARIA.sessionSelectorDialogOpened());
					}
					iDialog.center();
				}
			}
			
			public void onFailure(Throwable caught) {
				AriaStatus.getInstance().setText(caught.getMessage());
				iSessions.clearTable(1);
				iSessions.addRow(null, new EmptyTableCell(caught.getMessage()));
				iDialog.center();
			}
		});
	}
	
	public void selectSession(final Long sessionId, final AsyncCallback<Boolean> callback) {
		selectSession(new AcademicSessionMatchSessionId(sessionId), callback);
	}
	
	public void selectSession(final AcademicSessionMatcher matcher, final AsyncCallback<Boolean> callback) {
		if (matcher == null) {
			selectSession();
			callback.onSuccess(false);
		} else if (iSession != null && matcher.match(iSession)) {
			callback.onSuccess(true);
		} else {
			iSectioningService.listAcademicSessions(iMode.isSectioning(), new AsyncCallback<Collection<AcademicSessionInfo>>() {
				public void onSuccess(Collection<AcademicSessionInfo> result) {
					List<AcademicSessionInfo> match = new ArrayList<AcademicSessionInfo>();
					for (AcademicSessionInfo session: result) {
						if (matcher.match(session)) { match.add(session); }
					}
					if (match.size() == 1) {
						selectSession(match.get(0), false);
						callback.onSuccess(true);
					} else {
						selectSession();
						callback.onSuccess(false);
					}
				}
				public void onFailure(Throwable caught) {
					iSessions.clearTable(1);
					iSessions.addRow(null, new EmptyTableCell(caught.getMessage()));
					iDialog.center();
					callback.onSuccess(false);
				}
			});
		}
	}
	
	public void selectSession(AcademicSessionInfo session, boolean fireChangeEvent) {
		Long oldSessionId = (iSession == null ? null : iSession.getSessionId());
		iSession = session;
		if (iSession == null) {
			iPanel.setText(MESSAGES.sessionSelectorNoSession());
			iPanel.setAriaLabel(ARIA.sessionNoSession());
		} else {
			iPanel.setText(MESSAGES.sessionSelectorLabel(iSession.getYear(), iSession.getTerm(), iSession.getCampus()));
			iPanel.setAriaLabel(ARIA.sessionCurrent(iSession.getYear(), iSession.getTerm(), iSession.getCampus()));
			if (fireChangeEvent || !iSession.getSessionId().equals(oldSessionId)) {
				AcademicSessionChangeEvent changeEvent = new AcademicSessionChangeEvent(oldSessionId, iSession.getSessionId());
				for (AcademicSessionChangeHandler handler: iAcademicSessionChangeHandlers)
					handler.onAcademicSessionChange(changeEvent);
			}
		}
	}
	
	public Long getAcademicSessionId() {
		return iSession == null ? null : iSession.getSessionId();
	}
	
	public String getAcademicSessionName() {
		return iSession == null ? null : iSession.getName();
	}
	
	@Override
	public AcademicSessionInfo getAcademicSessionInfo() {
		return iSession;
	}
	
	public static class AcademicSessionChangeEvent implements AcademicSessionProvider.AcademicSessionChangeEvent {
		private Long iSessionId;
		private Long iOldSessionId;
		private AcademicSessionChangeEvent(Long oldSessionId, Long sessionId) {
			iOldSessionId = oldSessionId;
			iSessionId = sessionId;
		}
		public Long getOldAcademicSessionId() {
			return iOldSessionId;
		}
		public Long getNewAcademicSessionId() {
			return iSessionId;
		}
		public boolean isChanged() {
			return (iSessionId == null ? iOldSessionId != null : !iSessionId.equals(iOldSessionId));
		}
	}
	
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
		iAcademicSessionChangeHandlers.add(handler);
	}
	
	private class MyDialogBox extends AriaDialogBox {
		private MyDialogBox() {
		}
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetType((Event) event.getNativeEvent()) == Event.ONKEYUP) {
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
					int row = iSessions.getSelectedRow();
					if (row >= 0) iSessions.setSelected(row, false);
					if (row < 0)
						row = 1;
					else {
						row ++;
						if (row >= iSessions.getRowCount()) row = 1;
					}
					AcademicSessionInfo session = iSessions.getData(row);
					if (session != null)
						AriaStatus.getInstance().setText(ARIA.sessionSelectorShowingSession(row, iSessions.getRowCount() - 1, session.getYear(), session.getTerm(), session.getCampus()));
					iSessions.setSelected(row, true);
				} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
					int row = iSessions.getSelectedRow();
					if (row >= 0) iSessions.setSelected(row, false);
					if (row < 0)
						row = iSessions.getRowCount() - 1;
					else {
						row --;
						if (row <= 0) row = iSessions.getRowCount() - 1;
					}
					AcademicSessionInfo session = iSessions.getData(row);
					if (session != null)
						AriaStatus.getInstance().setText(ARIA.sessionSelectorShowingSession(row, iSessions.getRowCount() - 1, session.getYear(), session.getTerm(), session.getCampus()));
					iSessions.setSelected(row, true);
				} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && iSessions.getSelectedRow()>=0) {
					int row = iSessions.getSelectedRow();
					if (row >= 0 && iSessions.getData(row) != null)
						rowSelected(row, iSessions.getData(row));
				}  else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE && getAcademicSessionId()!=null) {
					iDialog.hide();
				}
			}
		}
	}
	
	private class EmptyTableCell extends Label implements UniTimeTable.HasColSpan {
		EmptyTableCell(String text) {
			super(text, false);
			getElement().getStyle().setFontStyle(FontStyle.ITALIC);
		}

		@Override
		public int getColSpan() {
			return 3;
		}
	}
}
