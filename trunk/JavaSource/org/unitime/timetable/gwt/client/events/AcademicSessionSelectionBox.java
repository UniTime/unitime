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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.IntervalSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

public class AcademicSessionSelectionBox extends IntervalSelector<AcademicSessionSelectionBox.AcademicSession> implements AcademicSessionProvider {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	private List<AcademicSessionChangeHandler> iChangeHandlers = new ArrayList<AcademicSessionChangeHandler>();
	
	public AcademicSessionSelectionBox() {
		super(false);
		
		setHint(MESSAGES.waitLoadingSessions());
		RPC.execute(new ListAcademicSessions(Window.Location.getParameter("term")), new AsyncCallback<GwtRpcResponseList<AcademicSession>>() {

			@Override
			public void onFailure(Throwable caught) {
				setErrorHint(caught.getMessage());
				ToolBox.checkAccess(caught);
				onInitializationFailure(caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<AcademicSession> result) {
				clearHint();
				setValues(result);
				for (final AcademicSession session: result) {
					if (session.isSelected()) {
						setDefaultValue(new Interval(session));
						break;
					}
				}
				if (getDefaultValue() != null)
					setValue(getDefaultValue(), true);
				onInitializationSuccess(result);
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<Interval> event) {
				fireAcademicSessionChanged();
			}
		});
	}
	
	protected void onInitializationSuccess(List<AcademicSession> sessions) {
	}
	
	protected void onInitializationFailure(Throwable caught) {
	}
	

	@Override
	public Long getAcademicSessionId() {
		return (getValue() == null ? null : getValue().getFirst().getUniqueId());
	}

	@Override
	public String getAcademicSessionName() {
		return (getValue() == null ? null : getValue().getFirst().getName());
	}

	public String getAcademicSessionAbbreviation() {
		return (getValue() == null ? null : getValue().getFirst().getAbbv());
	}

	@Override
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
		iChangeHandlers.add(handler);
	}

	@Override
	public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
		if (sessionId != null && sessionId.equals(getAcademicSessionId())) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (sessionId == null && getAcademicSessionId() == null) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (sessionId == null) {
			setValue(null);
			if (callback != null) callback.onSuccess(true);
		} else {
			boolean found = false;
			if (getValues() != null) {
				for (AcademicSession session: getValues()) {
					if (session.getUniqueId().equals(sessionId)) {
						setValue(new Interval(session));
						if (callback != null) callback.onSuccess(true);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				setValue(null);
				if (callback != null) callback.onSuccess(false);
			}
		}
		fireAcademicSessionChanged();
	}
	
	private Long iLastSessionId = null;
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
		private String iName, iAbbv;
		private boolean iSelected;
		private Long iPreviousId, iNextId;
		
		public AcademicSession() {}
		
		public AcademicSession(Long uniqueId, String name, String abbv, boolean selected) {
			iUniqueId = uniqueId; iName = name; iAbbv = abbv; iSelected = selected;
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public String getName() { return iName; }
		public String getAbbv() { return iAbbv; }
		public boolean isSelected() { return iSelected; }
		
		public Long getPreviousId() { return iPreviousId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		
		public Long getNextId() { return iNextId; }
		public void setNextId(Long id) { iNextId = id; }

		@Override
		public String toString() {
			return getName();
		}
	}

	@GwtRpcImplementedBy("org.unitime.timetable.events.ListAcademicSessions")
	public static class ListAcademicSessions implements GwtRpcRequest<GwtRpcResponseList<AcademicSession>> {
		private String iTerm = null;
		
		public ListAcademicSessions() {}
		public ListAcademicSessions(String term) { iTerm = term; }
		
		public boolean hasTerm() { return iTerm != null && !iTerm.isEmpty(); }
		public String getTerm() { return iTerm; }
		
		@Override
		public String toString() {
			return (hasTerm() ? getTerm() : "");
		}
	}
	
	@Override
	public void setValue(Interval value, boolean fire) {
		if (value == null && getValues() != null) {
			for (AcademicSession session: getValues())
				if (session.isSelected()) {
					value = new Interval(session); break;
				}
		}
		super.setValue(value, fire);
	}
	
	protected AcademicSession session(Long id) {
		if (getValues() != null && id != null)
			for (AcademicSession session: getValues())
				if (session.getUniqueId().equals(id)) return session;
		return null;
	}
	
	@Override
	protected Interval previous(Interval interval) {
		if (interval.isOne()) {
			AcademicSession prev = session(interval.getFirst().getPreviousId());
			if (prev != null) return new Interval(prev);
		}
		return null;
	}

	@Override
	protected Interval next(Interval interval) {
		if (interval.isOne()) {
			AcademicSession next = session(interval.getFirst().getNextId());
			if (next != null) return new Interval(next);
		}
		return null;
	}
}
