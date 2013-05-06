/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.gwt.client.sectioning;

import java.util.Collection;
import java.util.Vector;

import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.ClickableHint;
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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class AcademicSessionSelector extends Composite implements AcademicSessionProvider {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);

	private Label iSessionLabel;
	private ClickableHint iSessionHint;
	private AcademicSessionInfo iSession = null;
	
	private DialogBox iDialog;
	private UniTimeTable<AcademicSessionInfo> iSessions;
	
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private Vector<AcademicSessionChangeHandler> iAcademicSessionChangeHandlers = new Vector<AcademicSessionChangeHandler>();
	private StudentSectioningPage.Mode iMode;
	
	public AcademicSessionSelector(StudentSectioningPage.Mode mode) {
		iMode = mode;
		iSessionLabel = new Label(MESSAGES.sessionSelectorNoSession(), false);
		iSessionLabel.setStyleName("unitime-SessionSelector");
		
		VerticalPanel vertical = new VerticalPanel();
		vertical.add(iSessionLabel);
		
		iSessionHint = new ClickableHint(MESSAGES.sessionSelectorHint());
		iSessionHint.setAriaLabel(ARIA.sessionNoSession());
		vertical.add(iSessionHint);
		
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
		
		iSessionLabel.addClickHandler(ch);
		iSessionHint.addClickHandler(ch);
		
		iSessions.addMouseClickListener(new UniTimeTable.MouseClickListener<AcademicSessionInfo>() {

			@Override
			public void onMouseClick(UniTimeTable.TableEvent<AcademicSessionInfo> event) {
				if (event.getData() != null)
					rowSelected(event.getRow(), event.getData());
			}
			
		});
		
		initWidget(vertical);
	}
	
	private void rowSelected(int row, AcademicSessionInfo session) {
		iDialog.hide();
		iSessionLabel.setText(MESSAGES.sessionSelectorLabel(session.getYear(), session.getTerm(), session.getCampus()));
		iSessionHint.setAriaLabel(ARIA.sessionCurrent(session.getYear(), session.getTerm(), session.getCampus()));
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
		if (sessionId == null) {
			selectSession();
			callback.onSuccess(false);
		} else if (sessionId.equals(getAcademicSessionId())) {
			callback.onSuccess(true);
		} else {
			iSectioningService.listAcademicSessions(iMode.isSectioning(), new AsyncCallback<Collection<AcademicSessionInfo>>() {
				public void onSuccess(Collection<AcademicSessionInfo> result) {
					for (AcademicSessionInfo session: result) {
						if (session.getSessionId().equals(sessionId)) {
							selectSession(session, false);
							callback.onSuccess(true);
							return;
						}
					}
					selectSession();
					callback.onSuccess(false);
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
			iSessionLabel.setText(MESSAGES.sessionSelectorNoSession());
			iSessionHint.setAriaLabel(ARIA.sessionNoSession());
		} else {
			iSessionLabel.setText(MESSAGES.sessionSelectorLabel(iSession.getYear(), iSession.getTerm(), iSession.getCampus()));
			iSessionHint.setAriaLabel(ARIA.sessionCurrent(iSession.getYear(), iSession.getTerm(), iSession.getCampus()));
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
				if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_DOWN) {
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
				} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_UP) {
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
				} else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ENTER && iSessions.getSelectedRow()>=0) {
					int row = iSessions.getSelectedRow();
					if (row >= 0 && iSessions.getData(row) != null)
						rowSelected(row, iSessions.getData(row));
				}  else if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE && getAcademicSessionId()!=null) {
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
