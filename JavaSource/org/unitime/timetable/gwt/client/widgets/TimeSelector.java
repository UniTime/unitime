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
package org.unitime.timetable.gwt.client.widgets;

import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
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
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author Tomas Muller
 */
public class TimeSelector extends Composite implements HasValue<Integer>, Focusable, HasAllKeyHandlers, HasAllFocusHandlers, HasSelectionHandlers<Integer> {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private TimeSelector iStart;
	
	private TimeMenu iTimes;
	private ScrollPanel iTimeScroll;
	
	private PopupPanel iPopup;
	private AriaTextBox iText;
	private Integer iDiff = null;
	
	public TimeSelector() {
		this(null);
	}
	
	public TimeSelector(TimeSelector start) {
		iStart = start;
		iText = new AriaTextBox();
		iText.setStyleName("gwt-SuggestBox");
		iText.addStyleName("unitime-TimeSelector");
		iText.setAriaLabel(start == null ? ARIA.startTime() : ARIA.endTime());
		
		iTimes = new TimeMenu();
		
		iTimeScroll = new ScrollPanel(iTimes);
		iTimeScroll.addStyleName("scroll");
		
		iPopup = new PopupPanel(true, false);
		iPopup.setPreviewingAllNativeEvents(true);
		iPopup.setStyleName("unitime-TimeSelectorPopup");
		iPopup.setWidget(iTimeScroll);
		
		initWidget(iText);
		
		createSuggestions();
		
		iText.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (isSuggestionsShowing()) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_DOWN:
						iTimes.selectItem(iTimes.getSelectedItemIndex() + 1);
						updateSuggestionStatus();
						break;
					case KeyCodes.KEY_UP:
						if (iTimes.getSelectedItemIndex() == -1) {
							iTimes.selectItem(iTimes.getNumItems() - 1);
						} else {
							iTimes.selectItem(iTimes.getSelectedItemIndex() - 1);
						}
						updateSuggestionStatus();
						break;
					case KeyCodes.KEY_ENTER:
						iTimes.executeSelected();
						hideSuggestions();
						break;
					case KeyCodes.KEY_TAB:
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
					if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN && (event.getNativeEvent().getAltKey() || iText.getCursorPos() == iText.getText().length())) {
						showSuggestions();
						updateSuggestionStatus();
						event.preventDefault();
						event.stopPropagation();
					}
				}
			}
		});
		
        iText.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (selectASuggestion()) {
					if (!isSuggestionsShowing()) showSuggestions();
					updateSuggestionStatus();
				}
			}
		});
        
        iText.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				setValue(parseTime(event.getValue()), true);
			}
		});
        
        if (iStart != null)
        	iStart.addValueChangeHandler(new ValueChangeHandler<Integer>() {
				@Override
				public void onValueChange(ValueChangeEvent<Integer> event) {
					createSuggestions();
					if (iDiff != null && event.getValue() != null)
						setValue(Math.min(event.getValue() + iDiff, 288));
				}
			});
        
        iText.addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				showSuggestions();
			}
		});
	}
	
	private String iLastSelected = null;
	TimeMenuItem iInsertedSuggestion = null;
	private boolean selectASuggestion() {
		if (iText.getText().equals(iLastSelected)) return false;
		if (iInsertedSuggestion != null) {
			iTimes.removeItem(iInsertedSuggestion);
			iInsertedSuggestion = null;
		}
		iLastSelected = iText.getText();
		Integer slot = getValue();
		if (slot == null) {
			if (iStart == null)
				slot = CONSTANTS.eventStartDefault(); // 730a
			else if (iStart.getValue() == null)
				slot = CONSTANTS.eventStopDefault(); // 530p
			else
				slot = iStart.getValue() + CONSTANTS.eventLengthDefault(); // 1 hour
		}
		int select = -1, diff = 0;
		for (int i = 0; i < iTimes.getNumItems(); i++) {
			int suggestion = iTimes.getSlot(i);
			if (select < 0 || Math.abs(slot - suggestion) < diff && slot >= suggestion) {
				diff = Math.abs(slot - suggestion); select = i;
			}
		}
		if (getValue() != null && diff != 0) {
			iInsertedSuggestion = new TimeMenuItem(slot);
			if (select == 0 && iStart != null && slot < iTimes.getSlot(0)) select --;
			iTimes.insertItem(iInsertedSuggestion, select + 1);
			iTimes.selectItem(select + 1);
		} else {
			iTimes.selectItem(select);	
		}
		return true;
	}
	
	private void createSuggestions() {
		iLastSelected = null;
		iTimes.clearItems();
		if (iStart == null) {
			for (int t = 0; t < 288; t += CONSTANTS.eventSlotIncrement()) {
				iTimes.addItem(new TimeMenuItem(t));
			}
			iTimeScroll.setWidth("77px");
		} else {
			Integer prev = iStart.getValue();
			for (int t = CONSTANTS.eventSlotIncrement() + (prev == null ? 0 : prev); t <= 288; t += CONSTANTS.eventSlotIncrement()) {
				iTimes.addItem(new TimeMenuItem(t));
			}
			iTimeScroll.setWidth(prev == null ? "77px" : "137px");
		}
		selectASuggestion();
	}
	
	private void hideSuggestions() {
		if (iPopup.isShowing()) iPopup.hide();
	}
	
	private void showSuggestions() {
		iPopup.showRelativeTo(iText);
		Integer value = getValue();
		if (value != null && (iTimes.getSelectedItemIndex() < 0 || iTimes.getItem(iTimes.getSelectedItemIndex()).getSlot() != value)) {
			for (int i = 0; i < iTimes.getNumItems(); i++) {
				if (iTimes.getItem(i).getSlot() == value) {
					iTimes.selectItem(i);
					break;
				}
			}
		}
		iTimes.scrollToView();
	}
	
	private boolean isSuggestionsShowing() {
		return iPopup.isShowing();
	}
	
	public TimeMenu getTimeMenu() {
		return iTimes;
	}
	
	public static String slot2time(int slot, int diff) {
		if (diff <= 0) return TimeUtils.slot2time(slot);
		if (diff < 24 && diff != 12) return TimeUtils.slot2time(slot) + " (" + (5 * diff) + " mins)";
		else if (diff == 12) return TimeUtils.slot2time(slot) + " (1 hr)";
		else if (diff % 12 == 0) return TimeUtils.slot2time(slot) + " (" + (diff/12) + " hrs)";
		else if (diff % 12 == 3) return TimeUtils.slot2time(slot) + " (" + (diff/12) + "&frac14; hrs)";
		else if (diff % 12 == 6) return TimeUtils.slot2time(slot) + " (" + (diff/12) + "&frac12; hrs)";
		else if (diff % 12 == 9) return TimeUtils.slot2time(slot) + " (" + (diff/12) + "&frac34; hrs)";
		return TimeUtils.slot2time(slot) + " (" + diff / 12 + ":" + (diff % 12 == 1 ? "0" : "") + (5 * (diff % 12)) + ")";
	}
	
	public static String slot2aria(int slot, int diff) {
		if (diff <= 0) return TimeUtils.slot2aria(slot);
		if (diff < 24 && diff != 12) return TimeUtils.slot2aria(slot) + " (" + (5 * diff) + " minutes)";
		else if (diff == 12) return TimeUtils.slot2aria(slot) + " (1 hour)";
		else if (diff % 12 == 0) return TimeUtils.slot2aria(slot) + " (" + (diff/12) + " hours)";
		else if (diff % 12 == 3) return TimeUtils.slot2aria(slot) + " (" + (diff/12) + "&frac14; hours)";
		else if (diff % 12 == 6) return TimeUtils.slot2aria(slot) + " (" + (diff/12) + "&frac12; hours)";
		else if (diff % 12 == 9) return TimeUtils.slot2aria(slot) + " (" + (diff/12) + "&frac34; hours)";
		return TimeUtils.slot2aria(slot) + " (" + diff / 12 + ":" + (diff % 12 == 1 ? "0" : "") + (5 * (diff % 12)) + ")";
	}
	
	public Integer parseTime(String text) {
		return TimeUtils.parseTime(CONSTANTS, text, (iStart == null ? null : iStart.getValue()));
	}
	
	public class TimeMenu extends MenuBar implements HasAllFocusHandlers {
		TimeMenu() {
			super(true);
			setStyleName("");
			setFocusOnHoverEnabled(false);
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
		
		public void selectItem(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				selectItem(items.get(index));
				iTimeScroll.ensureVisible(items.get(index));
			}
		}
		
		public void scrollToView() {
			List<MenuItem> items = getItems();
			int index = getSelectedItemIndex();
			if (index > -1 && index < items.size()) {
				iTimeScroll.ensureVisible(items.get(index));
			}
		}
		
		public void executeSelected() {
			MenuItem selected = getSelectedItem();
			if (selected != null) {
				selected.getScheduledCommand().execute();
				setStatus(ARIA.suggestionSelected(((TimeMenuItem)selected).toAriaString()));
			}
		}
		
		public int getSlot(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				return ((TimeMenuItem)items.get(index)).getSlot();
			} else {
				return -1;
			}
		}
		
		public TimeMenuItem getItem(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				return (TimeMenuItem)items.get(index);
			} else {
				return null;
			}
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
	
	private class TimeMenuItem extends MenuItem {
		private int iSlot;
		
		private TimeMenuItem(final int slot) {
			super(slot2time(slot, iStart == null || iStart.getValue() == null ? 0 : slot - iStart.getValue()),
				true,
				new ScheduledCommand() {
					@Override
					public void execute() {
						hideSuggestions();
						setValue(slot, true);
						iLastSelected = iText.getText();
						fireSuggestionEvent(slot);
					}
				}
			);
			setStyleName("item");
			getElement().setAttribute("whiteSpace", "nowrap");
			iSlot = slot;
		}
		
		public int getSlot() {
			return iSlot;
		}
		
		public String toAriaString() {
			return slot2aria(iSlot, iStart == null || iStart.getValue() == null ? 0 : iSlot - iStart.getValue());
		}
	}


	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Integer getValue() {
		return parseTime(iText.getText());
	}

	@Override
	public void setValue(Integer value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if (value == null)
			iText.setText("");
		else {
			if (iStart != null && iStart.getValue() != null)
				iDiff = value - iStart.getValue();
			iText.setText(TimeUtils.slot2time(value));
		}
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	
	public void setDiff(Integer diff) {
		iDiff = diff;
	}
	
	public static class TimeUtils {

		public static Integer parseTime(GwtConstants constants, String text, Integer start) {
			if (!text.isEmpty()) {
				for (String noon: constants.parseTimeNoon()) {
					if (noon.startsWith(text.toLowerCase())) return 144;
				}
				for (String midnight: constants.parseTimeMidnight()) {
					if (midnight.startsWith(text.toLowerCase())) return (start == null ? 0 : 288);
				}
			}
			if (start != null) {
				for (String h: constants.parseTimeHours()) {
					if (text.toLowerCase().endsWith(h)) {
						try {
							return start + Math.round(12 * Float.parseFloat(text.substring(0, text.length() - h.length()).trim()));
						} catch (Exception e) {}
					}
				}
				for (String m: constants.parseTimeMinutes()) {
					if (text.toLowerCase().endsWith(m)) {
						try {
							return start + Integer.parseInt(text.substring(0, text.length() - m.length()).trim()) / 5;
						} catch (Exception e) {}
					}
				}
			}
			int startHour = 0, startMin = 0;
			String token = text.trim();
			String number = "";
			boolean plus = false;
			if (token.startsWith("+")) { plus = true; token = token.substring(1).trim(); }
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) return null;
			if (number.length()>2) {
				try {
					startHour = Integer.parseInt(number) / 100;
					startMin = Integer.parseInt(number) % 100;
				} catch (NumberFormatException e) {
					return null;
				}
			} else {
				try {
					startHour = Integer.parseInt(number);
				} catch (NumberFormatException e) {
					return null;
				}
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":") || token.startsWith(".") || (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9')) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (!number.isEmpty()) {
					try {
						startMin = Integer.parseInt(number);
					} catch (NumberFormatException e) {
						return null;
					}
				}
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.isEmpty() && plus && start != null) {
				int slot = start + (60 * startHour + startMin) / 5;
				if (slot > start && slot <= 288) return slot;
				if (startMin == 0) {
					slot = start + startHour / 5;
					if (slot > start && slot <= 288) return slot;
				}
			}
			for (String am: constants.parseTimeAm()) {
				if (token.toLowerCase().startsWith(am)) {
					token = token.substring(2); hasAmOrPm = true;
					if (startHour == 12) startHour = 24;
					break;
				}
			}
			for (String pm: constants.parseTimePm()) {
				if (token.toLowerCase().startsWith(pm)) {
					token = token.substring(2); hasAmOrPm = true;
					if (startHour<12) startHour += 12;
					break;
				}
			}
			if (!token.isEmpty()) return null;
			// if (startHour < 7 && !hasAmOrPm) startHour += 12;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			// if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			int slot = (60 * startHour + startMin) / 5;
			if (start != null && slot <= start && slot <= 144 && !hasAmOrPm) slot += 144;
			if (start != null && slot <= start) return null;
			if (slot < 0 || slot > 288) {
				if (!text.endsWith("000"))
					return parseTime(constants, text + "0", start);
				else
					return null;
			}
			return slot;
		}
		
		public static Integer parseMilitary(String text) {
			if (text == null || text.isEmpty()) return null;
			try {
				int time = Integer.parseInt(text);
				int hour = time / 100;
				int min = time % 100;
				int slot = (60 * hour + min) / 5;
				return (slot < 0 || slot > 288 ? null : slot);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		
		public static Integer parseTime2(GwtConstants constants, String text, Integer start) {
			Integer slot = parseMilitary(text);
			if (slot != null) return slot;
			return parseTime(constants, text, start);
		}
		
		public static String slot2time(int slot) {
			if (CONSTANTS.useAmPm()) {
				if (slot == 0) return CONSTANTS.timeMidnight();
				if (slot == 144) return CONSTANTS.timeNoon();
				if (slot == 0) return CONSTANTS.timeMidnightEnd();
			}
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        if (CONSTANTS.useAmPm())
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + " " + (h == 24 ? CONSTANTS.timeAm() : h >= 12 ? CONSTANTS.timePm() : CONSTANTS.timeAm());
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public static String slot2military(int slot) {
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        return h + (m < 10 ? "0" : "") + m;
		}
		
		public static String slot2short(int slot) {
			if (CONSTANTS.useAmPm()) {
				if (slot == 0) return CONSTANTS.timeMidnight();
				if (slot == 144) return CONSTANTS.timeNoon();
				if (slot == 288) return CONSTANTS.timeMidnightEnd();
			}
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        if (CONSTANTS.useAmPm())
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? CONSTANTS.timeShortAm() : h >= 12 ? CONSTANTS.timeShortPm() : CONSTANTS.timeShortAm());
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public static String slot2aria(int slot) {
			if (CONSTANTS.useAmPm()) {
				if (slot == 0) return CONSTANTS.timeMidnight();
				if (slot == 144) return CONSTANTS.timeNoon();
				if (slot == 288) return CONSTANTS.timeMidnightEnd();
			}
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        if (CONSTANTS.useAmPm())
	        	return (h > 12 ? h - 12 : h) + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m) + (h == 24 ? " " + CONSTANTS.timeAm() : h >= 12 ? " " + CONSTANTS.timePm() : " " + CONSTANTS.timeAm());
	        else
	        	return h + (m == 0 ? "" : (m < 10 ? " 0" : " ") + m);
		}
	}
	
	public void setStatus(String text) {
		AriaStatus.getInstance().setText(text);
	}
	
	public void updateSuggestionStatus() {
		if (iPopup.isShowing()) {
			int index = iTimes.getSelectedItemIndex();
			if (index < 0) {
				setStatus(ARIA.showingMultipleSuggestionsNoQueryNoneSelected(iTimes.getNumItems()));
				return;
			}
			int slot = iTimes.getSlot(index);
			int count = iTimes.getNumItems();
			String text = iTimes.getItem(index).toAriaString();
			if (iInsertedSuggestion != null) {
				if (iInsertedSuggestion.getSlot() == slot) {
					setStatus(ARIA.onSuggestionNoCount(text));
					return;
				}
				if (iInsertedSuggestion.getSlot() < slot) {
					index --;
				}
				count--;
			}
			setStatus(ARIA.onSuggestion(index + 1, count, text));
		}
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
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return iText.addKeyUpHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return iText.addKeyDownHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return iText.addKeyPressHandler(handler);
	}

	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return iText.addFocusHandler(handler);
	}

	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return iText.addBlurHandler(handler);
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<Integer> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}
	
	private void fireSuggestionEvent(Integer selectedTimeSlot) {
		SelectionEvent.fire(this, selectedTimeSlot);
	}
}
