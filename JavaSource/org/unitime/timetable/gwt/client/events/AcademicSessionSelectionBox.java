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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.IntervalSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class AcademicSessionSelectionBox extends IntervalSelector<AcademicSessionSelectionBox.AcademicSession> implements AcademicSessionProvider {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private List<AcademicSession> iAllSessions = null;
	private AcademicSessionFilter iFilter = null;

	private List<AcademicSessionChangeHandler> iChangeHandlers = new ArrayList<AcademicSessionChangeHandler>();
	
	public AcademicSessionSelectionBox(String term) {
		super(false);
		
		setHint(MESSAGES.waitLoadingSessions());
		RPC.execute(new ListAcademicSessions(term), new AsyncCallback<GwtRpcResponseList<AcademicSession>>() {

			@Override
			public void onFailure(Throwable caught) {
				setErrorHint(caught.getMessage());
				ToolBox.checkAccess(caught);
				onInitializationFailure(caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<AcademicSession> result) {
				clearHint();
				iAllSessions = result;
				setValues(result);
				onInitializationSuccess(result);
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<Interval> event) {
				fireAcademicSessionChanged();
				setAriaLabel(ARIA.academicSession(toAriaString()));
			}
		});
		
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				setAriaLabel(ARIA.academicSession(toAriaString()));
			}
		});
	}
	
	protected void onInitializationSuccess(List<AcademicSession> sessions) {
	}
	
	protected void onInitializationFailure(Throwable caught) {
	}
	

	@Override
	public Long getAcademicSessionId() {
		return (getValue() == null ? null : getValues().isEmpty() ? null : getValue().getFirst().getUniqueId());
	}

	@Override
	public String getAcademicSessionName() {
		return (getValue() == null ? null : getValues().isEmpty() ? null : getValue().getFirst().getName());
	}

	public String getAcademicSessionAbbreviation() {
		return (getValue() == null ? null : getValues().isEmpty() ? null : getValue().getFirst().getAbbv());
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
	
	public void selectSession(String sessionAbbreviation, AsyncCallback<Boolean> callback) {
		if (sessionAbbreviation != null && sessionAbbreviation.equals(getAcademicSessionAbbreviation())) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (sessionAbbreviation == null && getAcademicSessionAbbreviation() == null) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (sessionAbbreviation == null) {
			setValue(null);
			if (callback != null) callback.onSuccess(true);
		} else {
			boolean found = false;
			if (getValues() != null) {
				for (AcademicSession session: getValues()) {
					if (sessionAbbreviation.equals(session.getAbbv())) {
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
		private String iName, iAbbv, iHint;
		private boolean iSelected;
		private Long iPreviousId, iNextId;
		private int iFlags = 0;
		
		public static enum Flag {
			HasClasses,
			HasMidtermExams,
			HasFinalExams,
			HasEvents,
			CanAddEvents;
			
			public int flag() { return 1 << ordinal(); }
			public boolean in(int flags) {
				return (flags & flag()) != 0;
			}
			public int set(int flags) {
				return (in(flags) ? flags : flags + flag());
			}
			public int clear(int flags) {
				return (in(flags) ? flags - flag() : flags);
			}
		}
		
		public AcademicSession() {}
		
		public AcademicSession(Long uniqueId, String name, String abbv, String hint, boolean selected) {
			iUniqueId = uniqueId; iName = name; iAbbv = abbv; iHint = hint; iSelected = selected;
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public String getName() { return iName; }
		public String getAbbv() { return iAbbv; }
		public String getHint() { return iHint; }
		public boolean isSelected() { return iSelected; }
		
		public Long getPreviousId() { return iPreviousId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		
		public Long getNextId() { return iNextId; }
		public void setNextId(Long id) { iNextId = id; }

		public boolean has(Flag f) { return f.in(iFlags); }
		public void set(Flag f) { iFlags = f.set(iFlags); }
		public void clear(Flag f) { iFlags = f.clear(iFlags); }
		
		@Override
		public String toString() {
			return getName();
		}
	}

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
		if (value != null && value.isOne())
			setHint(value.getFirst().getHint());
		super.setValue(value, fire);
	}
	
	protected AcademicSession session(Long id) {
		if (iAllSessions != null && id != null)
			for (AcademicSession session: iAllSessions)
				if (session.getUniqueId().equals(id)) return session;
		return null;
	}
	
	@Override
	protected Interval previous(Interval interval) {
		if (interval.isOne()) {
			AcademicSession prev = session(interval.getFirst().getPreviousId());
			while (prev != null && iFilter != null && !iFilter.accept(prev))
				prev = session(prev.getPreviousId());
			if (prev != null) return new Interval(prev);
		}
		return null;
	}

	@Override
	protected Interval next(Interval interval) {
		if (interval.isOne()) {
			AcademicSession next = session(interval.getFirst().getNextId());
			while (next != null && iFilter != null && !iFilter.accept(next))
				next = session(next.getNextId());
			if (next != null) return new Interval(next);
		}
		return null;
	}
	
	public void setFilter(AcademicSessionFilter filter) {
		iFilter = filter;
		if (iAllSessions != null) {
			setValues(iAllSessions);
			setValue(getValue(), false);
		}
	}
	
	public List<AcademicSession> getAllSessions() { return iAllSessions; }
	
	@Override
	public void setValues(List<AcademicSession> sessions) {
		List<AcademicSession> filtered = new ArrayList<AcademicSession>();
		AcademicSession selected = null;
		for (AcademicSession session: sessions)
			if (iFilter == null || iFilter.accept(session)) { 
				filtered.add(session);
				if (session.isSelected()) selected = session;
			}
		if (selected == null) selected = (filtered.isEmpty() ? null : filtered.get(filtered.size() - 1));
		setDefaultValue(new Interval(selected));
		super.setValues(filtered);
		if (filtered.isEmpty()) {
			setErrorHint(MESSAGES.noSessionAvailable());
			UniTimeNotifications.error(MESSAGES.noSessionAvailable());
		} else if (getValue() == null && getDefaultValue() != null) {
			setValue(getDefaultValue(), true);
		}
	}
	
	public static interface AcademicSessionFilter {
		public boolean accept(AcademicSession session);
	}

	@Override
	public AcademicSessionInfo getAcademicSessionInfo() {
		return null;
	}
}
