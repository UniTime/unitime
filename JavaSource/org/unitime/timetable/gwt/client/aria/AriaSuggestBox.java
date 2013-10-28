/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.aria;

import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;

import com.google.gwt.aria.client.AutocompleteValue;
import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HandlesAllKeyEvents;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.SuggestOracle.Request;
import com.google.gwt.user.client.ui.SuggestOracle.Response;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class AriaSuggestBox extends Composite implements HasText, HasValue<String>, HasSelectionHandlers<Suggestion>, Focusable, HasEnabled, HasAriaLabel {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private AriaTextBox iText;
	private SuggestOracle iOracle;
	
	private PopupPanel iSuggestionPopup;
	private SuggestionMenu iSuggestionMenu;
	private ScrollPanel iPopupScroll;
	private SuggestionCallback iSuggestionCallback;
	private SuggestOracle.Callback iOracleCallback;
	
	private String iCurrentText = null;
	
	public AriaSuggestBox(SuggestOracle oracle) {
		iOracle = oracle;
		iText = new AriaTextBox();
		iText.setStyleName("gwt-SuggestBox");
		initWidget(iText);
		
		addEventsToTextBox();
		
		iSuggestionMenu = new SuggestionMenu();
		
		iPopupScroll = new ScrollPanel(iSuggestionMenu);
		iPopupScroll.addStyleName("scroll");
		
		iSuggestionPopup = new PopupPanel(true, false);
		iSuggestionPopup.setPreviewingAllNativeEvents(true);
		iSuggestionPopup.setStyleName("unitime-SuggestBoxPopup");
		iSuggestionPopup.setWidget(iPopupScroll);
		iSuggestionPopup.addAutoHidePartner(getElement());
		
		iSuggestionCallback = new SuggestionCallback() {
			@Override
			public void onSuggestionSelected(Suggestion suggestion) {
				if (!suggestion.getReplacementString().isEmpty()) {
					setStatus(ARIA.suggestionSelected(status(suggestion)));
				}
				iCurrentText = suggestion.getReplacementString();
				setText(suggestion.getReplacementString());
				hideSuggestionList();
				fireSuggestionEvent(suggestion);
			}
		};
		
		iOracleCallback = new SuggestOracle.Callback() {
			@Override
			public void onSuggestionsReady(Request request, Response response) {
				if (response.getSuggestions() == null || response.getSuggestions().isEmpty()) {
					if (iSuggestionPopup.isShowing()) iSuggestionPopup.hide();
				} else {
					iSuggestionMenu.clearItems();
					SuggestOracle.Suggestion first = null;
					for (SuggestOracle.Suggestion suggestion: response.getSuggestions()) {
						iSuggestionMenu.addItem(new SuggestionMenuItem(suggestion));
						if (first == null) first = suggestion;
					}
					iSuggestionMenu.selectItem(0);
					ToolBox.setMinWidth(iSuggestionMenu.getElement().getStyle(), (iText.getElement().getClientWidth() - 4) + "px");
					iSuggestionPopup.showRelativeTo(iText);
					iSuggestionMenu.scrollToView();
					if (response.getSuggestions().size() == 1) {
						if (first.getReplacementString().isEmpty())
							setStatus(status(first));
						else
							setStatus(ARIA.showingOneSuggestion(status(first)));
					} else {
						setStatus(ARIA.showingMultipleSuggestions(response.getSuggestions().size(), request.getQuery(), status(first)));
					}
				}
			}
		};
		
		Roles.getTextboxRole().setAriaAutocompleteProperty(iText.getElement(), AutocompleteValue.NONE);
		
		DOM.setElementAttribute(iSuggestionPopup.getElement(), "id", DOM.createUniqueId());
		Roles.getTextboxRole().setAriaOwnsProperty(iText.getElement(), Id.of(iSuggestionPopup.getElement()));
	}
	
	private String status(Suggestion suggestion) {
		return suggestion instanceof HasStatus ? ((HasStatus)suggestion).getStatusString() : suggestion.getDisplayString();
	}
	
	public void setStatus(String text) {
		AriaStatus.getInstance().setText(text);
	}
	
	private void addEventsToTextBox() {
		class TextBoxEvents extends HandlesAllKeyEvents implements ValueChangeHandler<String> {
			public void onKeyDown(KeyDownEvent event) {
				switch (event.getNativeKeyCode()) {
				case KeyCodes.KEY_DOWN:
					if (moveSelectionDown()) {
						event.preventDefault();
						return;
					}
					if (!isSuggestionListShowing() && (event.getNativeEvent().getAltKey() || iText.getCursorPos() == iText.getText().length()))
						showSuggestionList();
	            break;
	          case KeyCodes.KEY_UP:
	        	  if (moveSelectionUp()) {
	        		  event.preventDefault();
	        		  return;
	        	  }
	        	  break;
	          case KeyCodes.KEY_ENTER:
	          case KeyCodes.KEY_TAB:
	        	  if (isSuggestionListShowing())
	        		  iSuggestionMenu.executeSelected();
	        	  break;
	          case KeyCodes.KEY_ESCAPE:
	        	  if (isSuggestionListShowing())
	        		  hideSuggestionList();
				}
				delegateEvent(AriaSuggestBox.this, event);
			}
			
			public void onKeyPress(KeyPressEvent event) {
				delegateEvent(AriaSuggestBox.this, event);
			}
			
			public void onKeyUp(KeyUpEvent event) {
				refreshSuggestions();
				delegateEvent(AriaSuggestBox.this, event);
			}
			
			public void onValueChange(ValueChangeEvent<String> event) {
				delegateEvent(AriaSuggestBox.this, event);
			}
		}
		
		TextBoxEvents events = new TextBoxEvents();
	    events.addKeyHandlersTo(iText);
	    iText.addValueChangeHandler(events);
	}
	
	private boolean moveSelectionDown() {
		if (!isSuggestionListShowing()) return false;
		if (iSuggestionMenu.selectItem(iSuggestionMenu.getSelectedItemIndex() + 1)) {
			if (iSuggestionMenu.getNumItems() > 1)
				setStatus(ARIA.onSuggestion(iSuggestionMenu.getSelectedItemIndex() + 1, iSuggestionMenu.getNumItems(), status(iSuggestionMenu.getSelectedSuggestion())));
			return true;
		} else {
			return false;
		}
	}
	
	private boolean moveSelectionUp() {
		if (!isSuggestionListShowing()) return false;
		boolean selected = false;
		if (iSuggestionMenu.getSelectedItemIndex() == -1) {
			selected = iSuggestionMenu.selectItem(iSuggestionMenu.getNumItems() - 1);
		} else {
			selected = iSuggestionMenu.selectItem(iSuggestionMenu.getSelectedItemIndex() - 1);
		}
		if (selected) {
			if (iSuggestionMenu.getNumItems() > 1)
				setStatus(ARIA.onSuggestion(iSuggestionMenu.getSelectedItemIndex() + 1, iSuggestionMenu.getNumItems(), status(iSuggestionMenu.getSelectedSuggestion())));
			return true;
		} else {
			return false;
		}
	}
	
	public void showSuggestionList() {
		iCurrentText = null;
		refreshSuggestions();
	}
	
	private void refreshSuggestions() {
		String text = getText();
		if (text.equals(iCurrentText)) {
			return;
		} else {
			iCurrentText = text;
		}
		showSuggestions(text);
	}
	
	public void hideSuggestionList() {
		if (iSuggestionPopup.isShowing()) iSuggestionPopup.hide();
	}
	
	public void showSuggestions(String text) {
		if (text.isEmpty())
			iOracle.requestDefaultSuggestions(new Request(null), iOracleCallback);
		else
			iOracle.requestSuggestions(new Request(text), iOracleCallback);
	}
	
	public boolean isSuggestionListShowing() {
		return iSuggestionPopup.isShowing();
	}
	
	public SuggestionMenu getSuggestionMenu() {
		return iSuggestionMenu;
	}

	private class SuggestionMenu extends MenuBar implements HasFocusHandlers, HasBlurHandlers {
		SuggestionMenu() {
			super(true);
			setStyleName("");
			setFocusOnHoverEnabled(false);
			sinkEvents(Event.ONBLUR);
			sinkEvents(Event.ONFOCUS);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONBLUR:
		    	BlurEvent.fireNativeEvent(event, this);
		    	break;
		    case Event.ONFOCUS:
		    	FocusEvent.fireNativeEvent(event, this);
		    	break;
			}
			super.onBrowserEvent(event);
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
		
		public boolean selectItem(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				selectItem(items.get(index));
				iPopupScroll.ensureVisible(items.get(index));
				return true;
			}
			return false;
		}
		
		public void scrollToView() {
			List<MenuItem> items = getItems();
			int index = getSelectedItemIndex();
			if (index > -1 && index < items.size()) {
				iPopupScroll.ensureVisible(items.get(index));
			}
		}
		
		public boolean executeSelected() {
			MenuItem selected = getSelectedItem();
			if (selected == null) return false;
			selected.getScheduledCommand().execute();
			return true;
		}
		
		public Suggestion getSelectedSuggestion() {
			MenuItem selectedItem = getSelectedItem();
			return selectedItem == null ? null : ((SuggestionMenuItem)selectedItem).getSuggestion();
		}

		@Override
		public HandlerRegistration addBlurHandler(BlurHandler handler) {
			return addHandler(handler, BlurEvent.getType());
		}

		@Override
		public HandlerRegistration addFocusHandler(FocusHandler handler) {
			return addHandler(handler, FocusEvent.getType());
		}
	}
	
	private class SuggestionMenuItem extends MenuItem {
		private Suggestion iSuggestion = null;
		
		private SuggestionMenuItem(final Suggestion suggestion) {
			super(suggestion.getDisplayString(), iOracle.isDisplayStringHTML(), new ScheduledCommand() {
				@Override
				public void execute() {
					iSuggestionCallback.onSuggestionSelected(suggestion);
				}
			});
			setStyleName("item");
			DOM.setStyleAttribute(getElement(), "whiteSpace", "nowrap");
			iSuggestion = suggestion;
		}
		
		public Suggestion getSuggestion() {
			return iSuggestion;
		}
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return iText.getValue();
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		iText.setValue(value);
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}

	@Override
	public String getText() {
		return iText.getText();
	}

	@Override
	public void setText(String text) {
		iText.setText(text);
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<Suggestion> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}
	
	private void fireSuggestionEvent(Suggestion selectedSuggestion) {
		SelectionEvent.fire(this, selectedSuggestion);
	}

	@Override
	public int getTabIndex() {
		return iText.getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		iText.setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		iText.setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		iText.setTabIndex(index);
	}
	
	@Override
	public boolean isEnabled() {
		return iText.isEnabled();
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		iText.setEnabled(enabled);
	}
	
	public ValueBoxBase<String> getValueBox() {
		return iText;
	}
	
	public static interface SuggestionCallback {
		void onSuggestionSelected(Suggestion suggestion);
	}
	
	public static interface HasStatus {
		public String getStatusString();
	}

	@Override
	public String getAriaLabel() {
		return iText.getAriaLabel();
	}

	@Override
	public void setAriaLabel(String text) {
		iText.setAriaLabel(text);
	}
}