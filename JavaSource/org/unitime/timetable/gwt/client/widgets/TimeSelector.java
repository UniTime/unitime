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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.resources.GwtConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class TimeSelector extends Composite implements HasValue<Integer>{
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private TimeSelector iStart;
	
	private TimeMenu iTimes;
	private ScrollPanel iTimeScroll;
	
	private PopupPanel iPopup;
	private TextBox iText;
	private Integer iDiff = null;
	private List<Integer> iSuggestions = new ArrayList<Integer>();
	
	public TimeSelector() {
		this(null);
	}
	
	public TimeSelector(TimeSelector start) {
		iStart = start;
		iText = new TextBox();
		iText.setStyleName("gwt-SuggestBox");
		iText.addStyleName("unitime-TimeSelector");
		
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
						break;
					case KeyCodes.KEY_UP:
						if (iTimes.getSelectedItemIndex() == -1) {
							iTimes.selectItem(iTimes.getNumItems() - 1);
						} else {
							iTimes.selectItem(iTimes.getSelectedItemIndex() - 1);
						}
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
						event.preventDefault();
						event.stopPropagation();
					}
				}
			}
		});
		
        iText.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (selectASuggestion() && !isSuggestionsShowing())
					showSuggestions();
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
	MenuItem iInsertedSuggestion = null;
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
		for (int i = 0; i < iSuggestions.size(); i++) {
			int suggestion = iSuggestions.get(i);
			if (select < 0 || Math.abs(slot - suggestion) < diff && slot >= suggestion) {
				diff = Math.abs(slot - suggestion); select = i;
			}
		}
		if (getValue() != null && diff != 0) {
			Command command = new Command() {
				@Override
				public void execute() {
					hideSuggestions();
					setValue(getValue(), true);
					iLastSelected = iText.getText(); 
				}
			};
			iInsertedSuggestion = new MenuItem(slot2time(slot, iStart == null || iStart.getValue() == null ? 0 : slot - iStart.getValue()), true, command);
			iInsertedSuggestion.setStyleName("item");
			DOM.setStyleAttribute(iInsertedSuggestion.getElement(), "whiteSpace", "nowrap");
			if (select == 0 && iStart != null && slot < iSuggestions.get(0)) select --;
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
		iSuggestions.clear();
		if (iStart == null) {
			for (int t = 0; t < 288; t += CONSTANTS.eventSlotIncrement()) {
				final int slot = t;
				Command command = new Command() {
					@Override
					public void execute() {
						hideSuggestions();
						setValue(slot, true);
						iLastSelected = iText.getText(); 
					}
				};
				MenuItem item = new MenuItem(TimeUtils.slot2time(t), true, command);
				item.setStyleName("item");
				DOM.setStyleAttribute(item.getElement(), "whiteSpace", "nowrap");
				iTimes.addItem(item);
				iSuggestions.add(t);
			}
			iTimeScroll.setWidth("77px");
		} else {
			Integer prev = iStart.getValue();
			for (int t = CONSTANTS.eventSlotIncrement() + (prev == null ? 0 : prev); t <= 288; t += CONSTANTS.eventSlotIncrement()) {
				final int slot = t;
				Command command = new Command() {
					@Override
					public void execute() {
						hideSuggestions();
						setValue(slot, true);
						iLastSelected = iText.getText();
					}
				};
				MenuItem item = new MenuItem(slot2time(t, prev == null ? 0 : t - prev), true, command);
				item.setStyleName("item");
				DOM.setStyleAttribute(item.getElement(), "whiteSpace", "nowrap");
				iTimes.addItem(item);
				iSuggestions.add(t);
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
		iTimes.scrollToView();
	}
	
	private boolean isSuggestionsShowing() {
		return iPopup.isShowing();
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
	
	public Integer parseTime(String text) {
		if (!text.isEmpty() && "midnight".startsWith(text.toLowerCase())) return (iStart == null ? 0 : 288);
		return TimeUtils.parseTime(text, (iStart == null ? null : iStart.getValue()));
	}
	
	private class TimeMenu extends MenuBar {
		TimeMenu() {
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
			if (selected != null)
				selected.getCommand().execute();
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
	
	
	public static class TimeUtils {
		private static String[] HR = new String[] { "h", "hr", "hrs", "hour", "hours" };
		private static String[] MN = new String[] { "m", "min", "mins", "minute", "minutes" };
		
		public static Integer parseTime(String text, Integer start) {
			if (!text.isEmpty() && "noon".startsWith(text.toLowerCase())) return 144;
			if (!text.isEmpty() && "midnight".startsWith(text.toLowerCase())) return (start == null ? 0 : 288);
			if (start != null) {
				for (String h: HR) {
					if (text.toLowerCase().endsWith(h)) {
						try {
							return start + Math.round(12 * Float.parseFloat(text.substring(0, text.length() - h.length()).trim()));
						} catch (Exception e) {}
					}
				}
				for (String m: MN) {
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
				startHour = Integer.parseInt(number) / 100;
				startMin = Integer.parseInt(number) % 100;
			} else {
				startHour = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":") || token.startsWith(".") || (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9')) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (!number.isEmpty())
					startMin = Integer.parseInt(number);
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
			if (token.toLowerCase().startsWith("am")) {
				token = token.substring(2); hasAmOrPm = true;
				if (startHour == 12) startHour = 24;
			}
			if (token.toLowerCase().startsWith("a")) {
				token = token.substring(1); hasAmOrPm = true;
				if (startHour == 12) startHour = 24;
			}
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (!token.isEmpty()) return null;
			// if (startHour < 7 && !hasAmOrPm) startHour += 12;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			// if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			int slot = (60 * startHour + startMin) / 5;
			if (start != null && slot <= start && slot <= 144 && !hasAmOrPm) slot += 144;
			if (start != null && slot <= start) return null;
			if (slot < 0 || slot > 288) return parseTime(text + "0", start);
			return slot;
		}
		
		public static String slot2time(int slot) {
			if (CONSTANTS.useAmPm()) {
				if (slot == 0 || slot == 288) return CONSTANTS.timeMidnight();
				if (slot == 144) return CONSTANTS.timeNoon();
			}
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        if (CONSTANTS.useAmPm())
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? " am" : h >= 12 ? " pm" : " am");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
		
		public static String slot2short(int slot) {
			if (CONSTANTS.useAmPm()) {
				if (slot == 0 || slot == 288) return CONSTANTS.timeMidnight();
				if (slot == 144) return CONSTANTS.timeNoon();
			}
			int h = slot / 12;
	        int m = 5 * (slot % 12);
	        if (CONSTANTS.useAmPm())
	        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
	        else
				return h + ":" + (m < 10 ? "0" : "") + m;
		}
	}

}
