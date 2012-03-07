/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class AcademicSessionSelectionBox extends Composite implements AcademicSessionProvider {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private UniTimeWidget<ListBox> iList;
	private HorizontalPanel iPanel;
	private Button iPrev, iNext;
	private List<AcademicSessionChangeHandler> iChangeHandlers = new ArrayList<AcademicSessionChangeHandler>();
	private List<AcademicSession> iSessions = new ArrayList<AcademicSession>();
	private Long iLastSessionId = null;
	
	public AcademicSessionSelectionBox() {
		iPanel = new HorizontalPanel();
		iPanel.setSpacing(2);
		iPanel.setStyleName("unitime-AcademicSessionSelector");
		
		iPrev = new Button("&laquo;");
		iPrev.setEnabled(false);
		iPrev.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Long id = getAcademicSessionId();
				for (AcademicSession s: iSessions)
					if (s.getUniqueId().equals(id) && s.getPreviousId() != null)
						selectSession(s.getPreviousId(), null);
			}
		});
		
		iNext = new Button("&raquo;");
		iNext.setEnabled(false);
		iNext.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				Long id = getAcademicSessionId();
				for (AcademicSession s: iSessions)
					if (s.getUniqueId().equals(id) && s.getNextId() != null)
						selectSession(s.getNextId(), null);
			}
		});
		
		iList = new UniTimeWidget<ListBox>(new ListBox());
		iList.getWidget().addItem("Plase select...", "0");
		
		iList.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				fireAcademicSessionChanged();
			}
		});
		
		iPanel.add(iPrev);
		iPanel.add(iList);
		iPanel.add(iNext);
		
		RPC.execute(new ListAcademicSessions(), new AsyncCallback<GwtRpcResponseList<AcademicSession>>() {

			@Override
			public void onFailure(Throwable caught) {
				iList.setErrorHint(caught.getMessage());
			}

			@Override
			public void onSuccess(GwtRpcResponseList<AcademicSession> result) {
				iSessions.clear(); iSessions.addAll(result);
				int select = -1;
				iPrev.setEnabled(false); iNext.setEnabled(false);
				for (AcademicSession session: result) {
					iList.getWidget().addItem(session.getName(), session.getUniqueId().toString());
					if (session.isSelected()) {
						select = iList.getWidget().getItemCount() - 1;
						iPrev.setEnabled(session.getPreviousId() != null);
						iNext.setEnabled(session.getNextId() != null);
					}
				}
				if (select >= 0) {
					iList.getWidget().setSelectedIndex(select);
					fireAcademicSessionChanged();
				}
			}
		});
		
		initWidget(iPanel);
	}

	@Override
	public Long getAcademicSessionId() {
		if (iList.getWidget().getSelectedIndex() <= 0) return null;
		return Long.valueOf(iList.getWidget().getValue(iList.getWidget().getSelectedIndex()));
	}

	@Override
	public String getAcademicSessionName() {
		if (iList.getWidget().getSelectedIndex() <= 0) return null;
		return iList.getWidget().getItemText(iList.getWidget().getSelectedIndex());
	}

	@Override
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
		iChangeHandlers.add(handler);
	}

	@Override
	public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
		if (sessionId == null) {
			iList.getWidget().setSelectedIndex(0);
			if (callback != null) callback.onSuccess(true);
			iPrev.setEnabled(false);
			iNext.setEnabled(false);
		} else {
			int index = -1;
			for (int i = 1; i < iList.getWidget().getItemCount(); i++) {
				if (sessionId.equals(Long.valueOf(iList.getWidget().getValue(i)))) { index = i; break; }
			}
			if (index >= 0) {
				iList.getWidget().setSelectedIndex(index);
				AcademicSession session = iSessions.get(index - 1);
				iPrev.setEnabled(session.getPreviousId() != null);
				iNext.setEnabled(session.getNextId() != null);
				if (callback != null)  callback.onSuccess(true);
			} else {
				iPrev.setEnabled(false);
				iNext.setEnabled(false);
				if (callback != null) callback.onSuccess(false);
			}
		}
		fireAcademicSessionChanged();
	}
	
	public void fireAcademicSessionChanged() {
		final Long oldSession = iLastSessionId;
		iLastSessionId = getAcademicSessionId();
		AcademicSessionChangeEvent event = new AcademicSessionChangeEvent() {
			@Override
			public boolean isChanged() {
				if (oldSession == null)
					return getAcademicSessionId() != null;
				else
					return !oldSession.equals(getAcademicSessionId());
			}
			@Override
			public Long getOldAcademicSessionId() {
				return oldSession;
			}
			@Override
			public Long getNewAcademicSessionId() {
				return getAcademicSessionId();
			}
		};
		for (AcademicSessionChangeHandler handler: iChangeHandlers)
			handler.onAcademicSessionChange(event);
	}
	

	public static class AcademicSession implements IsSerializable {
		private Long iUniqueId;
		private String iName;
		private boolean iSelected;
		private Long iPreviousId, iNextId;
		
		public AcademicSession() {}
		
		public AcademicSession(Long uniqueId, String name, boolean selected) {
			iUniqueId = uniqueId; iName = name; iSelected = selected;
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public String getName() { return iName; }
		public boolean isSelected() { return iSelected; }
		
		public Long getPreviousId() { return iPreviousId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		
		public Long getNextId() { return iNextId; }
		public void setNextId(Long id) { iNextId = id; }
	}

	@GwtRpcImplementedBy("org.unitime.timetable.events.ListAcademicSessions")
	public static class ListAcademicSessions implements GwtRpcRequest<GwtRpcResponseList<AcademicSession>> {
		@Override
		public String toString() {
			return "";
		}
	}
}
