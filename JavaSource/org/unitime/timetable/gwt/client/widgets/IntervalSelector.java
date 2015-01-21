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

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.aria.client.AutocompleteValue;
import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
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

/**
 * @author Tomas Muller
 */
public class IntervalSelector<T> extends Composite implements HasValue<IntervalSelector<T>.Interval>, HasAriaLabel, HasAllFocusHandlers {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private boolean iAllowMultiSelection;
	private Interval iValue;
	private Interval iDefaultValue;
	private List<T> iValues = null;
	
	private boolean iFilterEnabled = false;
	private Filter<T> iItemFilter = null;
	
	private AriaTextBox iFilter;
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
		
		iFilter = new AriaTextBox();
		iFilter.addStyleName("selection");
		
		iFilter.addKeyDownHandler(new KeyDownHandler() {
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (isSuggestionsShowing()) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_DOWN:
						iPopupMenu.selectItem(iPopupMenu.getSelectedItemIndex() + 1);
						setStatus(ARIA.onSuggestion(iPopupMenu.getSelectedItemIndex() + 1, iPopupMenu.getNumItems(), iPopupMenu.getSelectedInterval().toAriaString()));
						break;
					case KeyCodes.KEY_UP:
						if (iPopupMenu.getSelectedItemIndex() == -1) {
							iPopupMenu.selectItem(iPopupMenu.getNumItems() - 1);
						} else {
							iPopupMenu.selectItem(iPopupMenu.getSelectedItemIndex() - 1);
						}
						setStatus(ARIA.onSuggestion(iPopupMenu.getSelectedItemIndex() + 1, iPopupMenu.getNumItems(), iPopupMenu.getSelectedInterval().toAriaString()));
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
				if (selectASuggestion()) {
					if (!isSuggestionsShowing())
						showSuggestions();
					else
						setStatus(ARIA.onSuggestion(iPopupMenu.getSelectedItemIndex() + 1, iPopupMenu.getNumItems(), iPopupMenu.getSelectedInterval().toAriaString()));
				}
			}
		});
        
		iFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (iValues != null) {
					if (isAllowMultiSelection() && event.getValue().equals(getReplaceString(new Interval()))) {
						setValue(new Interval());
					} else {
						Interval value = null;
						iterator: for (int i = 0; i < iValues.size(); i++) {
							if (event.getValue().equals(getReplaceString(new Interval(iValues.get(i))))) {
								value = new Interval(iValues.get(i));
								break iterator;
							}
							if (isAllowMultiSelection()) {
								for (int j = i + 1; j < iValues.size(); j++) {
									if (event.getValue().equals(getReplaceString(new Interval(iValues.get(i), iValues.get(j))))) {
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
		
		Roles.getTextboxRole().setAriaAutocompleteProperty(iFilter.getElement(), AutocompleteValue.NONE);
		
		iPopup.getElement().setAttribute("id", DOM.createUniqueId());
		Roles.getTextboxRole().setAriaOwnsProperty(iFilter.getElement(), Id.of(iPopup.getElement()));
	}
	
	protected void hideSuggestions() {
		if (iPopup.isShowing()) iPopup.hide();
	}
	
	protected void showSuggestions() {
		if (iValues == null || iValues.isEmpty()) return;
		if (iPopupMenu.getNumItems() == 1) {
			setStatus(ARIA.showingOneSuggestion(iPopupMenu.getSelectedInterval().toAriaString()));
		} else if (iPopupMenu.getSelectedItemIndex() == 0) {
			setStatus(ARIA.showingMultipleSuggestionsNoQuery(iPopupMenu.getNumItems(), (iFilter.getValue() == null ? ARIA.emptyFilter() : iFilter.getValue())));
		} if (iPopupMenu.getSelectedItemIndex() > 0) {
			setStatus(ARIA.onSuggestion(iPopupMenu.getSelectedItemIndex() + 1, iPopupMenu.getNumItems(), iPopupMenu.getSelectedInterval().toAriaString()));
		} else {
			setStatus(ARIA.showingMultipleSuggestionsNoQueryNoneSelected(iPopupMenu.getNumItems()));
		}
		iPopup.showRelativeTo(iFilter);
		iPopupMenu.scrollToView();
	}
	
	protected boolean isSuggestionsShowing() {
		return iPopup.isShowing();
	}
	
	protected void createSuggestions() {
		iPopupMenu.clearItems();
		if (isAllowMultiSelection()) {
			if (hasFilter()) {
				Interval all = new Interval(); all.setEnableFilter(!isFilterEnabled());
				iPopupMenu.addItem(new IntervalMenuItem(all));
				Interval allCurrent = new Interval(); allCurrent.setEnableFilter(isFilterEnabled());
				iPopupMenu.addItem(new IntervalMenuItem(allCurrent));
				if (allCurrent.equals(iDefaultValue) || getReplaceString(allCurrent).equals(iFilter.getText()))
						iPopupMenu.selectItem(1);
			} else {
				Interval all = new Interval();
				iPopupMenu.addItem(new IntervalMenuItem(all));
				if (all.equals(iDefaultValue) || getReplaceString(all).equals(iFilter.getText()))
					iPopupMenu.selectItem(0);
			}
		}
		int select = -1;
		for (int i = 0; i < iValues.size(); i++) {
			if (filter(iValues.get(i))) continue;
			Interval one = new Interval(iValues.get(i));
			iPopupMenu.addItem(new IntervalMenuItem(one));
			if ((one.equals(iDefaultValue) && select < 0) || getReplaceString(one).equals(iFilter.getText()) || one.equals(iValue)) select = iPopupMenu.getNumItems() - 1;
			if (isAllowMultiSelection() && getValue() != null && getValue().getFirst() != null && getValue().getFirst().equals(one.getFirst())) {
				for (int j = i + 1; j < iValues.size(); j++) {
					if (filter(iValues.get(j))) continue;
					Interval multi = new Interval(iValues.get(i), iValues.get(j));
					iPopupMenu.addItem(new IntervalMenuItem(multi));
					if ((multi.equals(iDefaultValue) && select < 0) || getReplaceString(multi).equals(iFilter.getText()) || multi.equals(iValue)) select = iPopupMenu.getNumItems() - 1;
				}
			}
		}
		if (select >= 0) iPopupMenu.selectItem(select);
		iLastSelected = iFilter.getText();
		if (iPopup.isShowing() && iPopupMenu.getSelectedItemIndex() >= 0)
			setStatus(ARIA.onSuggestion(iPopupMenu.getSelectedItemIndex() + 1, iPopupMenu.getNumItems(), iPopupMenu.getSelectedInterval().toAriaString()));
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
			
			for (int i = iPopupMenu.getNumItems() - 1; i >= 0; i --) {
				IntervalMenuItem item = (IntervalMenuItem)iPopupMenu.itemAt(i);
				if (item.getInterval().getLast() != null)
					iPopupMenu.removeItem(item);
			}

			idx = iPopupMenu.indexOf(new Interval(parsed.getFirst()));
			if (idx >= 0) {
				for (int j = iValues.size() - 1; j > iValues.indexOf(parsed.getFirst()); j --) {
					if (isFilterEnabled() && filter(iValues.get(j))) continue;
					Interval multi = new Interval(parsed.getFirst(), iValues.get(j));
					iPopupMenu.insertItem(new IntervalMenuItem(multi), idx + 1);
				}
			}
			
			return iPopupMenu.selectItem(parsed) >= 0;
		} else {
			suggestions: for (int i = 0; i < iPopupMenu.getNumItems(); i ++) {
				
				IntervalMenuItem item = (IntervalMenuItem)iPopupMenu.itemAt(i);
				String text = getReplaceString(item.getInterval()).toLowerCase();
				
				for (String c: iLastSelected.split("[ \\(\\),]"))
					if (!text.contains(c.trim().toLowerCase())) continue suggestions;
				
				iPopupMenu.selectItem(i);
				return true;
			}
		
		}

		if (iDefaultValue != null) {
			return iPopupMenu.selectItem(iDefaultValue) >= 0;
		}
			
		return false;
	}
	
	public Interval parse(String name) { 
		if (iValues == null) return null;
		if (name == null || name.isEmpty()) return (iAllowMultiSelection ? new Interval() : null);
		for (int i = 0; i < iValues.size(); i++) {
			if (isFilterEnabled() && filter(iValues.get(i))) continue;
			if (iValues.get(i).toString().toLowerCase().startsWith(name.toLowerCase()))
				return new Interval(iValues.get(i));
			if (iAllowMultiSelection)
				for (int j = i + 1; j < iValues.size(); j++) {
					if (isFilterEnabled() && filter(iValues.get(j))) continue;
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
		private boolean iEnableFilter = false;
		
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
					if (!filter(t)) ret.add(t);
			} else if (isOne()) {
				ret.add(iFirst);
			} else {
				for (int i = iValues.indexOf(iFirst); i <= iValues.indexOf(iLast); i++)
					if (!filter(iValues.get(i))) ret.add(iValues.get(i));
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
			if (isEnableFilter() != i.isEnableFilter()) return false;
			return (getFirst() == null ? i.getFirst() == null : getFirst().equals(i.getFirst())) && (getLast() == null ? i.getLast() == null : getLast().equals(i.getLast()));
		}
		
		public boolean isEnableFilter() { return iEnableFilter; }
		public void setEnableFilter(boolean enable) { iEnableFilter = enable; }
		
		public String toAriaString() {
			return getDisplayString(this);
		}
	}
	
	protected Interval previous(Interval interval) {
		if (iValues != null && interval.isOne()) {
			if (filter(interval.getFirst())) return null;
			for (int idx = iValues.indexOf(interval.getFirst()) - 1; idx >= 0; idx--)
				if (!filter(iValues.get(idx))) return new Interval(iValues.get(idx));
			if (isAllowMultiSelection()) {
				Interval all = new Interval();
				all.setEnableFilter(isFilterEnabled());
				return all;
			}
		}
		return null;
	}
	
	protected Interval next(Interval interval) {
		if (iValues == null) return null;
		if (interval.isOne()) {
			if (filter(interval.getFirst())) return null;
			for (int idx = iValues.indexOf(interval.getFirst()) + 1; idx < iValues.size(); idx++)
				if (!filter(iValues.get(idx))) return new Interval(iValues.get(idx));
		} else if (interval.isAll()) {
			for (T t: iValues)
				if (!filter(t)) return new Interval(t);
		}
		return null;
	}

	protected String getDisplayString(Interval interval) {
		if (interval.isAll())
			return interval.isEnableFilter() ? MESSAGES.itemAll() : MESSAGES.itemAllWithFilter();
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
		return getSelected().size() == 1;
	}
	
	public List<T> getSelected() {
		if (iValue == null) {
			if (hasFilter() && isFilterEnabled() && iValues != null) {
				List<T> values = new ArrayList<T>();
				for (T t: iValues)
					if (!filter(t)) values.add(t);
				return values;
			} else {
				return iValues;
			}
		} else {
			return iValue.getSelected();
		}
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
			if (iValue.isAll())
				iFilterEnabled = iValue.isEnableFilter();
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
				selected.getScheduledCommand().execute();
		}
		
		public MenuItem itemAt(int index) {
			List<MenuItem> items = getItems();
			if (index > -1 && index < items.size())
				return items.get(index);
			return null;
		}
		
		public IntervalMenuItem getSelectedInterval() {
			return (IntervalMenuItem)getSelectedItem();
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
			setValue(getInterval(), !getInterval().equals(getValue()));
			iLastSelected = getReplaceString(getInterval());
			if (getValue() != null)
				setStatus(ARIA.suggestionSelected(toAriaString()));
		}
	}
	
	private class IntervalMenuItem extends MenuItem {
		
		private Interval iInterval;
		
		private IntervalMenuItem(Interval interval) {
			super(getDisplayString(interval), true, new IntervalCommand(interval));
			setStyleName("item");
			getElement().setAttribute("whiteSpace", "nowrap");
			iInterval = interval;
		}
		
		public Interval getInterval() { return iInterval; }
		
		public String toAriaString() { return (getInterval() == null ? ARIA.emptyFilter() : getInterval().toAriaString()); }
		
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
	
	public void setFilter(Filter<T> filter) { iItemFilter = filter; }
	public boolean hasFilter() { return iItemFilter != null && !iItemFilter.isEmpty(); }
	
	public boolean isFilterEnabled() { return iFilterEnabled; }
	public void setFilterEnabled(boolean enabled) {
		iFilterEnabled = enabled;
		if (iValue != null && iValue.isAll()) {
			iValue.setEnableFilter(hasFilter() && isFilterEnabled());
			iFilter.setText(getReplaceString(iValue));
		}
		createSuggestions();
	}
	
	public boolean filter(T t) {
		return (isFilterEnabled() && hasFilter() ? iItemFilter.filter(t) : false);
	}

	public interface Filter<T> {
		public boolean filter(T t);
		public boolean isEmpty();
	}
	
	@Override
	public HandlerRegistration addFocusHandler(FocusHandler handler) {
		return iFilter.addFocusHandler(handler);
	}
	
	@Override
	public HandlerRegistration addBlurHandler(BlurHandler handler) {
		return iFilter.addBlurHandler(handler);
	}
	
	@Override
	public String getAriaLabel() {
		return iFilter.getAriaLabel();
	}

	@Override
	public void setAriaLabel(String text) {
		iFilter.setAriaLabel(text);
	}
	
	public void setStatus(String text) {
		AriaStatus.getInstance().setHTML(text);
	}
	
	public String toAriaString() {
		return (getValue() == null ? ARIA.emptyFilter() : getValue().toAriaString());
	}
}
