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
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcImplementedBy;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class AcademicSessionSelectionBox extends Composite implements AcademicSessionProvider {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private TextBox iFilter;
	private PopupPanel iPopup;
	private SessionMenu iSessionMenu;
	private ScrollPanel iSessionScroll;
	private AbsolutePanel iPanel;
	private Button iPrev, iNext;
	private List<AcademicSessionChangeHandler> iChangeHandlers = new ArrayList<AcademicSessionChangeHandler>();
	private List<AcademicSession> iSessions = new ArrayList<AcademicSession>();
	private AcademicSession iSession = null;
	private Long iLastSessionId = null;
	private UniTimeWidget<AbsolutePanel> iWidget;
	
	public AcademicSessionSelectionBox() {
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-AcademicSessionSelector");
		
		AbsolutePanel row = new AbsolutePanel();
		row.addStyleName("row");
		iPanel.add(row);
		
		iPrev = new Button("&laquo;");
		iPrev.setEnabled(false);
		iPrev.setTitle("Previous academic session");
		iPrev.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (iSession != null && iSession.getPreviousId() != null)
					selectSession(iSession.getPreviousId(), null);
			}
		});
		
		iNext = new Button("&raquo;");
		iNext.setEnabled(false);
		iNext.setTitle("Next academic session");
		iNext.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				if (iSession != null && iSession.getNextId() != null)
					selectSession(iSession.getNextId(), null);
			}
		});
		
		iFilter = new TextBox();
		/*(new SuggestOracle() {
			@Override
			public void requestDefaultSuggestions(Request request, Callback callback) {
				requestSuggestions(request, callback);
			}
			@Override
			public void requestSuggestions(final Request request, final Callback callback) {
				String query = request.getQuery();
				ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
				iFilter.setAutoSelectEnabled(true);
				if (iSessions == null || iSessions.isEmpty()) {
					suggestions.add(new Suggestion() {
						@Override
						public String getDisplayString() {
							return "<font color='red'>No academic sessions.</font>";
						}
						@Override
						public String getReplacementString() {
							return "";
						}
						
					});
				} else {
					sessions: for (final AcademicSession session: iSessions) {
						for (String c: query.split("[ \\(\\),]"))
							if (!session.getName().toLowerCase().contains(c.trim().toLowerCase())) continue sessions;
						suggestions.add(session);
					}
					Collections.reverse(suggestions);
				}
				if (suggestions.isEmpty() && iSessions != null) {
					for (final AcademicSession session: iSessions) {
						suggestions.add(session);
					}
					Collections.reverse(suggestions);
				}
				callback.onSuggestionsReady(request, new Response(suggestions));
			}
			@Override
			public boolean isDisplayStringHTML() { return true; }
			});
		iFilter.getTextBox().addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iFilter.showSuggestionList();
			}
		});
		iFilter.addStyleName("selection");
		iFilter.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() instanceof AcademicSession)
					selectSession(((AcademicSession)event.getSelectedItem()).getUniqueId(), null);
			}
		});*/
		iFilter.addStyleName("selection");
		
		iFilter.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (isSuggestionsShowing()) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_DOWN:
						iSessionMenu.selectItem(iSessionMenu.getSelectedItemIndex() + 1);
						break;
					case KeyCodes.KEY_UP:
						if (iSessionMenu.getSelectedItemIndex() == -1) {
							iSessionMenu.selectItem(iSessionMenu.getNumItems() - 1);
						} else {
							iSessionMenu.selectItem(iSessionMenu.getSelectedItemIndex() - 1);
						}
						break;
					case KeyCodes.KEY_TAB:
					case KeyCodes.KEY_ENTER:
						iSessionMenu.executeSelected();
						hideSuggestions();
						break;
					case KeyCodes.KEY_ESCAPE:
						hideSuggestions();
						break;
					}
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_DOWN:
					case KeyCodes.KEY_UP:
					case KeyCodes.KEY_ENTER:
					case KeyCodes.KEY_ESCAPE:
						event.preventDefault();
						event.stopPropagation();
					}
				} else {
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN && (event.getNativeEvent().getAltKey() || iFilter.getCursorPos() == iFilter.getText().length())) {
						showSuggestions();
						event.preventDefault();
						event.stopPropagation();
					}
				}
			}
		});
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (selectASuggestion() && !isSuggestionsShowing())
					showSuggestions();
			}
		});
        
		iFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iSessions != null)
					for (AcademicSession session: iSessions) {
						if (session.getName().equals(event.getValue())) {
							selectSession(session.getUniqueId(), null);
							break;
						}
					}
			}
		});
        
		iFilter.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				showSuggestions();
			}
		});
		
		iSessionMenu = new SessionMenu();
		
		iSessionScroll = new ScrollPanel(iSessionMenu);
		iSessionScroll.addStyleName("scroll");
		
		iPopup = new PopupPanel(true, false);
		iPopup.setPreviewingAllNativeEvents(true);
		iPopup.setStyleName("unitime-AcademicSessionSelectorPopup");
		iPopup.setWidget(iSessionScroll);
		
		row.add(iPrev);
		row.add(iFilter);
		row.add(iNext);
		
		iWidget = new UniTimeWidget<AbsolutePanel>(iPanel);
		iWidget.setHint("Loading academic sessions...");
		RPC.execute(new ListAcademicSessions(Window.Location.getParameter("term")), new AsyncCallback<GwtRpcResponseList<AcademicSession>>() {

			@Override
			public void onFailure(Throwable caught) {
				iWidget.setErrorHint(caught.getMessage());
				ToolBox.checkAccess(caught);
				onInitializationFailure(caught);
			}

			@Override
			public void onSuccess(GwtRpcResponseList<AcademicSession> result) {
				iWidget.clearHint();
				iSessions.clear(); iSessions.addAll(result);
				iSession = null;
				iPrev.setEnabled(false); iNext.setEnabled(false);
				for (final AcademicSession session: result) {
					if (session.isSelected()) {
						iSession = session;
						iPrev.setEnabled(session.getPreviousId() != null);
						iNext.setEnabled(session.getNextId() != null);
					}
					Command command = new Command() {
						@Override
						public void execute() {
							hideSuggestions();
							selectSession(session.getUniqueId(), null);
						}
					};
					MenuItem item = new MenuItem(session.getName(), true, command);
					item.setStyleName("item");
					DOM.setStyleAttribute(item.getElement(), "whiteSpace", "nowrap");
					iSessionMenu.addItem(item);
				}
				onInitializationSuccess(result);
				if (iSession != null) {
					iFilter.setText(iSession.getName());
					fireAcademicSessionChanged();
				}
				selectASuggestion();
			}
		});
		
		initWidget(iWidget);
	}
	
	private void hideSuggestions() {
		if (iPopup.isShowing()) iPopup.hide();
	}
	
	private void showSuggestions() {
		iPopup.showRelativeTo(iFilter);
		iSessionMenu.scrollToView();
	}
	
	private boolean isSuggestionsShowing() {
		return iPopup.isShowing();
	}
	
	private String iLastSelected = null;
	private boolean selectASuggestion() {
		if (iFilter.getText().equals(iLastSelected)) return false;
		iLastSelected = iFilter.getText();
		int selected = -1;
		if (iSessions != null) {
			sessions: for (int i = 0; i < iSessions.size(); i++) {
				AcademicSession session = iSessions.get(i);
				if (selected < 0 && session.isSelected()) { selected = i; }
				for (String c: iLastSelected.split("[ \\(\\),]"))
					if (!session.getName().toLowerCase().contains(c.trim().toLowerCase())) continue sessions;
				selected = i;
				break;
			}
		}
		if (selected >= 0) iSessionMenu.selectItem(selected);
		return true;
	}
	
	protected void onInitializationSuccess(List<AcademicSession> sessions) {
	}
	
	protected void onInitializationFailure(Throwable caught) {
	}
	
	public static class Button extends AbsolutePanel implements HasMouseDownHandlers {
		private boolean iEnabled = true;
		
		private Button(String caption) {
			getElement().setInnerHTML(caption);
			addStyleName("enabled");
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		public boolean isEnabled() { return iEnabled; }
		public void setEnabled(boolean enabled) {
			if (iEnabled == enabled) return;
			iEnabled = enabled;
			if (iEnabled) {
				addStyleName("enabled");
				removeStyleName("disabled");
			} else {
				addStyleName("disabled");
				removeStyleName("enabled");
			}
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	MouseDownEvent.fireNativeEvent(event, this);
		    	event.stopPropagation();
		    	event.preventDefault();
		    	break;
			}
		}
		
		@Override
		public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
			return addHandler(handler, MouseDownEvent.getType());
		}
	}


	@Override
	public Long getAcademicSessionId() {
		return (iSession == null ? null : iSession.getUniqueId());
	}

	@Override
	public String getAcademicSessionName() {
		return (iSession == null ? null : iSession.getName());
	}

	@Override
	public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
		iChangeHandlers.add(handler);
	}

	@Override
	public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
		if (iSession != null && iSession.getUniqueId().equals(sessionId)) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (iSession == null && sessionId == null) {
			if (callback != null) callback.onSuccess(true);
			return;
		}
		if (sessionId == null) {
			iSession = null;
			iFilter.setText("");
			if (callback != null) callback.onSuccess(true);
			iPrev.setEnabled(false);
			iNext.setEnabled(false);
		} else {
			iSession = null;
			if (iSessions != null)
				for (AcademicSession session: iSessions) {
					if (sessionId.equals(session.getUniqueId())) { iSession = session; break; }
				}
			if (iSession != null) {
				iFilter.setText(iSession.getName());
				iPrev.setEnabled(iSession.getPreviousId() != null);
				iNext.setEnabled(iSession.getNextId() != null);
				if (callback != null)  callback.onSuccess(true);
			} else {
				iFilter.setText("");
				iPrev.setEnabled(false);
				iNext.setEnabled(false);
				if (callback != null) callback.onSuccess(false);
			}
		}
		selectASuggestion();
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
	
	private class SessionMenu extends MenuBar {
		SessionMenu() {
			super(true);
			setStyleName("");
			setFocusOnHoverEnabled(false);
		}
		
		public int getNumItems() {
			return getItems().size();
		}
		
		public int getSelectedItemIndex() {
			MenuItem selectedItem = getSelectedItem();
			if (selectedItem != null)
				return getItems().indexOf(selectedItem);
			return -1;
		}
		
		public void selectItem(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				selectItem(items.get(index));
				iSessionScroll.ensureVisible(items.get(index));
			}
		}
		
		public void scrollToView() {
			List<MenuItem> items = getItems();
			int index = getSelectedItemIndex();
			if (index > -1 && index < items.size()) {
				iSessionScroll.ensureVisible(items.get(index));
			}
		}
		
		public void executeSelected() {
			MenuItem selected = getSelectedItem();
			if (selected != null)
				selected.getCommand().execute();
		}
	}

	public static class AcademicSession implements IsSerializable, Suggestion {
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

		@Override
		public String getDisplayString() {
			return getName();
		}

		@Override
		public String getReplacementString() {
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
}
