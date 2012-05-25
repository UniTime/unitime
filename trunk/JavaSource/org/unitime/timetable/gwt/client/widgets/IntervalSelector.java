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
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;

public class IntervalSelector<T> extends Composite implements HasValue<IntervalSelector<T>.Interval> {
	private boolean iAllowMultiSelection;
	private Interval iValue;
	private Interval iDefaultValue;
	private List<T> iValues = null;
	
	private TextBox iFilter;
	private PopupPanel iPopup;
	private Menu iPopupMenu;
	private ScrollPanel iPopupScroll;
	private P iPanel;
	private Button iPrev, iNext;
	private UniTimeWidget<AbsolutePanel> iWidget;
	
	public IntervalSelector(boolean allowMultiSelection) {
		iAllowMultiSelection = allowMultiSelection;
		
		iPanel = new P("unitime-IntervalSelector");
		
		P row = new P("row");
		iPanel.add(row);
		
		iPrev = new Button("&laquo;");
		iPrev.setEnabled(false);
		iPrev.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Interval prev = (getValue() == null ? null : previous(getValue()));
				if (prev != null)
					setValue(prev, true);
			}
		});
		
		iNext = new Button("&raquo;");
		iNext.setEnabled(false);
		iNext.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Interval next = (getValue() == null ? null : next(getValue()));
				if (next != null)
					setValue(next, true);
			}
		});
		
		iFilter = new TextBox();
		iFilter.addStyleName("selection");
		
		iFilter.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (isSuggestionsShowing()) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_DOWN:
						iPopupMenu.selectItem(iPopupMenu.getSelectedItemIndex() + 1);
						break;
					case KeyCodes.KEY_UP:
						if (iPopupMenu.getSelectedItemIndex() == -1) {
							iPopupMenu.selectItem(iPopupMenu.getNumItems() - 1);
						} else {
							iPopupMenu.selectItem(iPopupMenu.getSelectedItemIndex() - 1);
						}
						break;
					case KeyCodes.KEY_TAB:
					case KeyCodes.KEY_ENTER:
						iPopupMenu.executeSelected();
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
				if (iValues != null) {
					if (isAllowMultiSelection() && event.getValue().equals(getDisplayString(new Interval()))) {
						setValue(new Interval());
					} else {
						Interval value = null;
						iterator: for (int i = 0; i < iValues.size(); i++) {
							if (event.getValue().equals(getDisplayString(new Interval(iValues.get(i))))) {
								value = new Interval(iValues.get(i));
								break iterator;
							}
							if (isAllowMultiSelection()) {
								for (int j = i + 1; j < iValues.size(); j++) {
									if (event.getValue().equals(getDisplayString(new Interval(iValues.get(i), iValues.get(j))))) {
										value = new Interval(iValues.get(i), iValues.get(j));
										break iterator;
									}
								}
							}
						}
						setValue(value);
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
		
		iPopupMenu = new Menu();
		
		iPopupScroll = new ScrollPanel(iPopupMenu);
		iPopupScroll.addStyleName("scroll");
		
		iPopup = new PopupPanel(true, false);
		iPopup.setPreviewingAllNativeEvents(true);
		iPopup.setStyleName("unitime-IntervalSelectorPopup");
		iPopup.setWidget(iPopupScroll);
		
		row.add(iPrev);
		row.add(iFilter);
		row.add(iNext);
		
		iWidget = new UniTimeWidget<AbsolutePanel>(iPanel);
		
		initWidget(iWidget);
		
	}
	
	protected void hideSuggestions() {
		if (iPopup.isShowing()) iPopup.hide();
	}
	
	protected void showSuggestions() {
		if (iValues == null || iValues.isEmpty()) return;
		iPopup.showRelativeTo(iFilter);
		iPopupMenu.scrollToView();
	}
	
	protected boolean isSuggestionsShowing() {
		return iPopup.isShowing();
	}
	
	protected void createSuggestions() {
		iPopupMenu.clearItems();
		if (isAllowMultiSelection()) {
			Interval all = new Interval();
			iPopupMenu.addItem(new IntervalMenuItem(all));
			if (all.equals(iDefaultValue) || getDisplayString(all).equals(iFilter.getText()))
				iPopupMenu.selectItem(0);
		}
		int select = -1;
		for (int i = 0; i < iValues.size(); i++) {
			Interval one = new Interval(iValues.get(i));
			iPopupMenu.addItem(new IntervalMenuItem(one));
			if ((one.equals(iDefaultValue) && select < 0) || getReplaceString(one).equals(iFilter.getText()) || one.equals(iValue)) select = iPopupMenu.getNumItems() - 1;
			if (isAllowMultiSelection() && getValue() != null && getValue().getFirst() != null && getValue().getFirst().equals(one.getFirst())) {
				for (int j = i + 1; j < iValues.size(); j++) {
					Interval multi = new Interval(iValues.get(i), iValues.get(j));
					iPopupMenu.addItem(new IntervalMenuItem(multi));
					if ((multi.equals(iDefaultValue) && select < 0) || getReplaceString(multi).equals(iFilter.getText()) || multi.equals(iValue)) select = iPopupMenu.getNumItems() - 1;
				}
			}
		}
		if (select >= 0) iPopupMenu.selectItem(select);
		iLastSelected = iFilter.getText();
	}
	
	private String iLastSelected = null;
	protected boolean selectASuggestion() {
		if (iFilter.getText().equals(iLastSelected)) return false;
		iLastSelected = iFilter.getText();
		
		Interval parsed = parse(iLastSelected);
		if (parsed != null) {

			if (!isAllowMultiSelection())
				return iPopupMenu.selectItem(parsed) >= 0;
			
			if (parsed.isAll())
				return iPopupMenu.selectItem(0);
			
			int idx = iPopupMenu.selectItem(parsed);
			if (idx >= 0 && !parsed.isOne()) return true;
			
			idx = iPopupMenu.indexOf(new Interval(parsed.getFirst()));
			
			for (int i = iPopupMenu.getNumItems() - 1; i >= 0; i --) {
				IntervalMenuItem item = (IntervalMenuItem)iPopupMenu.itemAt(i);
				if (item.getInterval().getLast() != null)
					iPopupMenu.removeItem(item);
			}
			for (int j = iValues.size() - 1; j > iValues.indexOf(parsed.getFirst()); j --) {
				Interval multi = new Interval(parsed.getFirst(), iValues.get(j));
				iPopupMenu.insertItem(new IntervalMenuItem(multi), idx + 1);
			}
			
			return iPopupMenu.selectItem(parsed) >= 0;
		} else {
			
			suggestions: for (int i = 0; i < iPopupMenu.getNumItems(); i ++) {
				
				IntervalMenuItem item = (IntervalMenuItem)iPopupMenu.itemAt(i);
				String text = getDisplayString(item.getInterval()).toLowerCase();
				
				for (String c: iLastSelected.split("[ \\(\\),]"))
					if (!text.contains(c.trim().toLowerCase())) continue suggestions;
				
				iPopupMenu.selectItem(i);
				return true;
			}
		
		}

		if (iDefaultValue != null)
			return iPopupMenu.selectItem(iDefaultValue) >= 0;
			
		return false;
	}
	
	public Interval parse(String name) { 
		if (iValues == null) return null;
		if (name == null || name.isEmpty()) return (iAllowMultiSelection ? new Interval() : null);
		for (int i = 0; i < iValues.size(); i++) {
			if (iValues.get(i).toString().toLowerCase().startsWith(name.toLowerCase()))
				return new Interval(iValues.get(i));
			if (iAllowMultiSelection)
				for (int j = i + 1; j < iValues.size(); j++) {
					if ((iValues.get(i) + " - " + iValues.get(j)).toLowerCase().startsWith(name.toLowerCase()))
						return new Interval(iValues.get(i), iValues.get(j));
					if ((iValues.get(i) + "-" + iValues.get(j)).toLowerCase().startsWith(name.toLowerCase()))
						return new Interval(iValues.get(i), iValues.get(j));
				}
		}
		return null;
	}
	
	public boolean isAllowMultiSelection() { return iAllowMultiSelection; }
	public void setAllowMultiSelection(boolean allowMultiSelection) { iAllowMultiSelection = false; }
	
	public void setValues(List<T> values) {
		iValues = values;
		if (iValue != null && !iValue.isAll()) {
			if (iValue.isOne()) {
				if (!iValues.contains(iValue.getFirst()))
					setValue(iDefaultValue, true);
			} else {
				if (!iValues.contains(iValue.getFirst()) || !iValues.contains(iValue.getLast()))
					setValue(iDefaultValue, true);
			}
		}
		createSuggestions();
	}
	public boolean hasValues() { return iValues != null && !iValues.isEmpty(); }
	public List<T> getValues() { return iValues; }
	public void setValues(T... values) {
		List<T> valuesAsList = new ArrayList<T>(values == null ? 0 : values.length);
		if (values != null)
			for (T t: values)
				valuesAsList.add(t);
		setValues(valuesAsList);
	}
	
	public Interval createInterval(T first, T last) { return new Interval(first, last); }
	public Interval createInterval(T first) { return new Interval(first); }
	public Interval createInterval() { return new Interval(); }
	
	public class Interval {
		private T iFirst = null, iLast = null;
		
		public Interval() {
			this(null, null);
		}
		
		public Interval(T first) {
			this(first, null);
		}
		
		public Interval(T first, T last) {
			iFirst = first;
			if (last != null && !last.equals(first)) {
				if (iValues.indexOf(first) < iValues.indexOf(last)) {
					iLast = last;
				}
			}
		}
		
		public T getFirst() { return iFirst; }
		public T getLast() { return iLast; }
		
		public boolean isAll() { return iFirst == null; }
		public boolean isOne() { return iFirst != null && iLast == null; }
		
		public int getNrSelected() {
			return (isAll() ? iValues.size() : isOne() ? 1 : iValues.indexOf(iLast) - iValues.indexOf(iFirst) + 1);
		}
		
		public List<T> getSelected() {
			List<T> ret = new ArrayList<T>();
			if (isAll()) {
				for (T t: iValues)
					ret.add(t);
			} else if (isOne()) {
				ret.add(iFirst);
			} else {
				for (int i = iValues.indexOf(iFirst); i <= iValues.indexOf(iLast); i++)
					ret.add(iValues.get(i));
			}
			return ret;
		}
		
		@Override
		public String toString() {
			return getReplaceString(this);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IntervalSelector.Interval)) return false;
			Interval i = (Interval)o;
			return (getFirst() == null ? i.getFirst() == null : getFirst().equals(i.getFirst())) && (getLast() == null ? i.getLast() == null : getLast().equals(i.getLast()));
		}

	}
	
	protected Interval previous(Interval interval) {
		if (interval.isOne()) {
			int idx = iValues.indexOf(interval.getFirst());
			return (idx <= 0 ? isAllowMultiSelection() ? new Interval() : null : new Interval(iValues.get(idx - 1)));
		} else {
			return null;
		}
	}
	
	protected Interval next(Interval interval) {
		if (interval.isOne()) {
			int idx = (iValues == null ? -1 : iValues.indexOf(interval.getFirst()));
			return (iValues == null ? null : idx + 1 < iValues.size() ? new Interval(iValues.get(idx + 1)) : null);
		} else if (interval.isAll()) {
			return (iValues == null || iValues.isEmpty() ? null : new Interval(iValues.get(0)));
		} else {
			return null;
		}
	}

	protected String getDisplayString(Interval interval) {
		if (interval.isAll())
			return "All";
		if (interval.isOne())
			return interval.getFirst().toString();
		return "&nbsp;&nbsp;&nbsp;" + interval.getFirst().toString() + " - " + interval.getLast().toString();
	}
	
	protected String getReplaceString(Interval interval) {
		if (interval.isAll())
			return "";
		if (interval.isOne())
			return interval.getFirst().toString();
		return interval.getFirst().toString() + " - " + interval.getLast().toString();
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<IntervalSelector<T>.Interval> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public IntervalSelector<T>.Interval getValue() {
		return iValue;
	}
	
	public boolean isOne() {
		return (iValues != null && iValues.size() == 1) || (iValue != null && iValue.isOne());
	}
	
	public List<T> getSelected() {
		return (iValue == null ? iValues : iValue.getSelected());
	}
	
	public Interval getDefaultValue() {
		return iDefaultValue;
	}
	
	public void setDefaultValue(Interval defaultValue) {
		iDefaultValue = defaultValue;
	}

	@Override
	public void setValue(IntervalSelector<T>.Interval value) {
		setValue(value, false);
	}

	@Override
	public void setValue(IntervalSelector<T>.Interval value, boolean fireEvents) {
		iValue = value;
		if (iValue == null) {
			iFilter.setText("");
			iPrev.setEnabled(false);
			iNext.setEnabled(false);
		} else {
			iFilter.setText(getReplaceString(iValue));
			iPrev.setEnabled(previous(iValue) != null);
			iNext.setEnabled(next(iValue) != null);
		}
		createSuggestions();
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
	
	private class Menu extends MenuBar {
		Menu() {
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
		
		public boolean selectItem(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size()) {
				selectItem(items.get(index));
				iPopupScroll.ensureVisible(items.get(index));
				return true;
			}
			return false;
		}
		
		public int selectItem(Interval interval) {
			List<MenuItem> items = getItems();
			for (int i = 0; i < items.size(); i++) {
				if (((IntervalMenuItem)items.get(i)).getInterval().equals(interval)) {
					selectItem(items.get(i));
					iPopupScroll.ensureVisible(items.get(i));
					return i;
				}
			}
			return -1;
		}
		
		public int indexOf(Interval interval) {
			List<MenuItem> items = getItems();
			for (int i = 0; i < items.size(); i++) {
				if (((IntervalMenuItem)items.get(i)).getInterval().equals(interval))
					return i;
			}
			return -1;
		}
		
		public void scrollToView() {
			List<MenuItem> items = getItems();
			int index = getSelectedItemIndex();
			if (index > -1 && index < items.size()) {
				iPopupScroll.ensureVisible(items.get(index));
			}
		}
		
		public void executeSelected() {
			MenuItem selected = getSelectedItem();
			if (selected != null)
				selected.getCommand().execute();
		}
		
		public MenuItem itemAt(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size())
				return items.get(index);
			return null;
		}
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
	
	private class IntervalCommand implements Command {
		private Interval iInterval;
		
		private IntervalCommand(Interval interval) { iInterval = interval; }
		
		public Interval getInterval() { return iInterval; }
		
		@Override
		public String toString() { return getDisplayString(getInterval()); }

		@Override
		public void execute() {
			hideSuggestions();
			setValue(getInterval(), true);
			iLastSelected = getReplaceString(getInterval());
		}
	}
	
	private class IntervalMenuItem extends MenuItem {
		
		private Interval iInterval;
		
		private IntervalMenuItem(Interval interval) {
			super(getDisplayString(interval), true, new IntervalCommand(interval));
			setStyleName("item");
			DOM.setStyleAttribute(getElement(), "whiteSpace", "nowrap");
			iInterval = interval;
		}
		
		public Interval getInterval() { return iInterval; }
		
		
	}
	
	public void setErrorHint(String hint) {
		iWidget.setErrorHint(hint);
	}

	public void setHint(String hint) {
		iWidget.setHint(hint);
	}
	
	public void clearHint() {
		iWidget.clearHint();
	}

}
