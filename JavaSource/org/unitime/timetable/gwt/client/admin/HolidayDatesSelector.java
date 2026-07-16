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
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.aria.HasAriaLabel;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.FocusImpl;

public class HolidayDatesSelector extends Composite implements HasValue<List<HolidayDatesSelector.SelectedDate>>, Focusable {
	private static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages CMSG = GWT.create(CourseMessages.class);
	UniTimeWidget<DatesPanel> iPanel;
	private int iSessionYear = 1900;
	private static Option sSelectedOption = Option.HOLIDAY;
	
	public HolidayDatesSelector() {
		iPanel = new UniTimeWidget<DatesPanel>(new DatesPanel());
		initWidget(iPanel);
	}
	
	public String getPattern() {
		String ret = "";
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays())
					ret += (d.getValue() == null ? '0' : d.getValue().getCode());
			}
		}
		return ret;
	}
	
	public void setPattern(String pattern) {
		int index = 0;
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					try {
						char c = pattern.charAt(index ++);
						d.setValue(Option.getOption(c));
					} catch (StringIndexOutOfBoundsException e) {
						break;
					}
				}
			}
		}
	}
	
	public void init(List<SessionMonth> months) {
		init(months, MESSAGES.legendSelected(), MESSAGES.legendNotSelected());
	}
	
	public void init(List<SessionMonth> months, String selected, String notSelected) {
		iPanel.getWidget().clear();
		int firstOutside = -1, start = -1, end = -1, finals = -1, midterms = -1, firstHoliday = - 1, firstBreak = -1, firstPast = -1, firstEventDate = -1, firstClassDate = -1;
		int idx = 0;
		P lastWeek = null;
		for (SessionMonth month: months) {
			SingleMonth m = new SingleMonth(month, idx++, lastWeek);
			lastWeek = m.getWeeks().get(m.getWeeks().size() - 1);
			if (lastWeek.getDays().size() == 7) lastWeek = null;
			iPanel.getWidget().add(m);
			if (start < 0) start = month.getFirst(SessionMonth.Flag.START);
			if (end < 0) end = month.getFirst(SessionMonth.Flag.END);
			if (finals < 0) finals = month.getFirst(SessionMonth.Flag.FINALS);
			if (midterms < 0) midterms = month.getFirst(SessionMonth.Flag.MIDTERMS);
			if (firstHoliday < 0) firstHoliday = month.getFirst(SessionMonth.Flag.HOLIDAY);
			if (firstBreak < 0) firstBreak = month.getFirst(SessionMonth.Flag.BREAK);
			if (firstOutside < 0) firstOutside = month.getFirst(SessionMonth.Flag.DISABLED);
			if (firstPast < 0) firstPast = month.getFirst(SessionMonth.Flag.PAST);
			if (firstEventDate < 0) firstEventDate = month.getFirst(SessionMonth.Flag.DATE_MAPPING_EVENT);
			if (firstClassDate < 0) firstClassDate = month.getFirst(SessionMonth.Flag.DATE_MAPPING_CLASS);
			if (month.getFirst(SessionMonth.Flag.START) >= 0) iSessionYear = month.getYear();
		}
		iPanel.getWidget().add(new Legend());
		iPanel.getWidget().setCursor(new Date());
	}
	
	public static enum SelectionFlag {
		IncludePast,
		IncludeWeekend,
		IncludeVacation,
		IncludeNoClasses,
		;
		
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
	
	public class P extends AbsolutePanel implements HasAriaLabel {
		private String iCaption;
		private int[] iCursor = null;
		private List<D> iDays = new ArrayList<D>();
		
		private P(String caption, int[] cursor, String... styles) {
			iCaption = caption;
			iCursor = cursor;
			if (caption != null)
				getElement().setInnerHTML(caption);
			for (String style: styles)
				if (style != null && !style.isEmpty())
					addStyleName(style);
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		private P(String caption, String... styles) {
			this(caption, null, styles);			
		}
		
		public void addDay(D d) { iDays.add(d); }
		
		public boolean hasUnselectedDays(Option selected) {
			for (D d: iDays) {
				if (!d.isEnabled()) continue;
				if (d.getValue() != selected) return true;
			}
			return false;
		}
		
		public void setAllSelected(Option selected) {
			for (D d: iDays) {
				if (!d.isEnabled()) continue;
				d.setValue(selected, true);
			}
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	if (sSelectedOption != null && hasUnselectedDays(sSelectedOption))
	    			setAllSelected(sSelectedOption);
	    		else
	    			setAllSelected(null);
		    	if (iCursor != null) iPanel.getWidget().setCursor(iCursor);
		    	event.preventDefault();
		    	break;
			}
			super.onBrowserEvent(event);
		}
		
		public String getCaption() { return iCaption; }
		
		public List<D> getDays() { return iDays; }

		public String toString() {
			return getCaption() + " " + iDays;
		}

		@Override
		public String getAriaLabel() {
			return Roles.getTextboxRole().getAriaLabelProperty(getElement());
		}

		@Override
		public void setAriaLabel(String text) {
			if (text == null || text.isEmpty())
				Roles.getTextboxRole().removeAriaLabelProperty(getElement());
			else
				Roles.getTextboxRole().setAriaLabelProperty(getElement(), text);
			
		}
	}
	
	public class D extends AbsolutePanel implements HasValue<Option>, HasAriaLabel {
		private boolean iEnabled = true;
		private Option iSelected = null;
		private int iFlag;
		private int iNumber;
		private int[] iCursor;
		
		private D(int number, int flag, int[] cursor, Option selected, String... styles) {
			iNumber = number;
			iFlag = flag;
			iCursor = cursor;
			iSelected = selected;
			getElement().setInnerHTML(String.valueOf(1 + number));
			for (String style: styles)
				if (style != null && !style.isEmpty())
					addStyleName(style);
			if (iSelected != null) addStyleName(iSelected.getStyleName());
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		public boolean isEnabled() { return true; }
		public void setEnabled(boolean enabled) {
			if (iEnabled == enabled) return;
			iEnabled = enabled;
			if (iEnabled) {
				addStyleName("clickable");
				removeStyleName("disabled");
			} else {
				removeStyleName("clickable");
				addStyleName("disabled");
			}
		}
		
		public boolean hasFlag(SessionMonth.Flag flag) {
			return (iFlag & flag.flag()) != 0;
		}
		
		public boolean isVacation() {
			return hasFlag(SessionMonth.Flag.BREAK) || hasFlag(SessionMonth.Flag.HOLIDAY);
		}
		
		public boolean isPast() {
			return hasFlag(SessionMonth.Flag.PAST);
		}
		
		public boolean isClassDay() {
			return hasFlag(SessionMonth.Flag.CLASSES);
		}
		
		public boolean isWeekend() {
			return SingleDateSelector.isWeekend(getDow());
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	if (isEnabled()) {
		    		setValue(getValue() != sSelectedOption ? sSelectedOption : null, true);
		    		iPanel.getWidget().setCursor(iCursor);
		    	}
		    	event.preventDefault();
		    	break;
			}
			super.onBrowserEvent(event);
		}
		
		public int getNumber() { return iNumber; }
		
		public int getDow() { return iCursor[1]; }
		
		public int getWeek() { return iCursor[2]; }
		
		public int getMonth() { return iCursor[0]; }
		
		public String toString() {
			return String.valueOf(1 + getNumber());
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Option> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public Option getValue() {
			return iSelected;
		}

		@Override
		public void setValue(Option value) {
			setValue(value, false);
		}

		@Override
		public void setValue(Option value, boolean fireEvents) {
			if (iSelected == value || !isEnabled()) return;
			if (iSelected != null)
				removeStyleName(iSelected.getStyleName());
			iSelected = value;
	    	if (iSelected != null)
	    		addStyleName(iSelected.getStyleName());
			if (fireEvents)
				ValueChangeEvent.fire(this, getValue());
		}

		@Override
		public String getAriaLabel() {
			return Roles.getTextboxRole().getAriaLabelProperty(getElement());
		}

		@Override
		public void setAriaLabel(String text) {
			if (text == null || text.isEmpty())
				Roles.getTextboxRole().removeAriaLabelProperty(getElement());
			else
				Roles.getTextboxRole().setAriaLabelProperty(getElement(), text);
			
		}
	}
	
	public class SingleMonth extends AbsolutePanel {
		private SessionMonth iSessionMonth;
		private List<D> iDays = new ArrayList<D>();
		private P[] iWeekDays = new P[7];
		private List<P> iWeeks = new ArrayList<P>();
		private P iCorner = null;
		
		public SingleMonth(SessionMonth month, int index, P previousWeek) {
			iSessionMonth = month;
			addStyleName("month");
			P name = new P(SingleDateSelector.monthName(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1), "label");
			add(name);
			name.getElement().setId(DOM.createUniqueId());
			Roles.getGridRole().set(getElement());
			Roles.getGridRole().setAriaLabelledbyProperty(getElement(), Id.of(name.getElement()));
			
			P box = new P(null, "box");
			add(box);
			P header = new P(null, "row");
			Roles.getRowRole().set(header.getElement());
			box.add(header);
			iCorner = new P(null, new int[] {index, -1, -1}, "cell", "corner", "clickable");
			iCorner.setAriaLabel(SingleDateSelector.monthName(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1));
			header.add(iCorner);
			Roles.getRowheaderRole().set(iCorner.getElement());
			
			int firstDayOfWeek = SingleDateSelector.firstDayOfWeek(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			int weekNumber = SingleDateSelector.weekNumber(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			int nrDays = SingleDateSelector.daysInMonth(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			
			int startDayOfWeek = SingleDateSelector.startingDayOfWeek();
			for (int i = 0; i < 7; i++) {
				iWeekDays[i] = new P(CONSTANTS.days()[(i + startDayOfWeek) % 7], new int[] {index, (i + startDayOfWeek) % 7, -1}, "cell", "dow", "clickable");
				iWeekDays[i].setAriaLabel(ARIA.datesDayOfWeekSelection(CONSTANTS.longDays()[(i + startDayOfWeek) % 7], SingleDateSelector.monthName(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1)));
				Roles.getColumnheaderRole().set(iWeekDays[i].getElement());
				header.add(iWeekDays[i]);
			}

			P line = new P(null, "row");
			Roles.getRowRole().set(line.getElement());
			box.add(line);
			P week = new P(String.valueOf(weekNumber++), new int[] {index, -1, iWeeks.size()}, "cell", "week", "clickable");
			Roles.getRowheaderRole().set(week.getElement());
			boolean weekAria = false;
			line.add(week);
			iWeeks.add(week);
			if (previousWeek != null) {
				for (D d: previousWeek.getDays())
					week.addDay(d);
				week.setAriaLabel(previousWeek.getAriaLabel());
				weekAria = true;
			}
			
			int idx = 0;
			int blanks = (firstDayOfWeek + 7 - startDayOfWeek) % 7;
			for (int i = 0; i < blanks; i++) {
				line.add(new P(null, "cell", (i + 1 == blanks ? "last-blank": "blank")));
				idx++;
			}
			
			int today = -1;
			if (month.getYear() == Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(new Date())) &&
				month.getMonth() + 1 == Integer.parseInt(DateTimeFormat.getFormat("MM").format(new Date())))
				today = Integer.parseInt(DateTimeFormat.getFormat("dd").format(new Date())) - 1;
			
			ValueChangeHandler<Option> onChange = new ValueChangeHandler<Option>() {
				@Override
				public void onValueChange(ValueChangeEvent<Option> event) {
					fireDaySelected(((D)event.getSource()).getNumber());
				}
			};
			
			DateTimeFormat df = DateTimeFormat.getFormat("yyyy/MM/dd");
			for (int i = 0; i < nrDays; i++) {
				if (i > 0 && idx % 7 == 0) {
					if (idx == 7 && iSessionMonth.getMonth() == 0 && weekNumber > 50) weekNumber = 1;
					line = new P(null, "row");
					Roles.getRowRole().set(line.getElement());
					box.add(line);
					week = new P(String.valueOf(weekNumber ++), new int[] {index, -1, iWeeks.size()}, "cell", "week", "clickable");
					Roles.getRowheaderRole().set(week.getElement());
					weekAria = false;
					line.add(week);
					iWeeks.add(week);
					previousWeek = null;
				}
				Date date = df.parse(getYear() + "/" + (1 + getMonth()) + "/" + (1 + i));
				D d = new D(i, iSessionMonth.getFlags(i), new int[] {index, (idx + startDayOfWeek) % 7, iWeeks.size() - 1}, null, "cell", (SingleDateSelector.isWeekend((idx + startDayOfWeek) % 7) ? "weekend" : "day"), "clickable");
				Roles.getGridcellRole().set(d.getElement());
				line.add(d);
				d.addValueChangeHandler(onChange);
				iCorner.addDay(d);
				iWeekDays[idx % 7].addDay(d);
				if (!weekAria) {
					week.setAriaLabel(ARIA.datesWeekSelection(weekNumber - 1, DateTimeFormat.getFormat(CONSTANTS.weekSelectionDateFormat()).format(date)));
					weekAria = true;
				}
				d.setAriaLabel(DateTimeFormat.getFormat(CONSTANTS.dateSelectionDateFormat()).format(date));
				week.addDay(d);
				if (previousWeek != null) previousWeek.addDay(d);
				iDays.add(d);
				idx++;
				if (today == i)
					d.addStyleName("today");
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.START)) {
					d.addStyleName("start");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendClassesStart());
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.END)) {
					d.addStyleName("start");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendClassesEnd());
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.FINALS)) {
					d.addStyleName("exam");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendFinals());
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.MIDTERMS)) {
					d.addStyleName("midterm");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendMidterms());
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.HOLIDAY)) {
					d.addStyleName("holiday");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendHoliday());
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.BREAK)) {
					d.addStyleName("break");
					d.setAriaLabel(d.getAriaLabel() + " " + MESSAGES.legendBreak());
				}
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.SESSION_START)) {
					d.addStyleName("session-start");
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.SESSION_END)) {
					d.addStyleName("session-end");
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.CLASS_END)) {
					d.addStyleName("class-end");
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.EXAM_START)) {
					d.addStyleName("exam-start");
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.EVENT_START)) {
					d.addStyleName("event-start");
				} else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.EVENT_END)) {
					d.addStyleName("event-end");
				}
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DATE_MAPPING_CLASS))
					d.addStyleName("classDate");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DATE_MAPPING_EVENT))
					d.addStyleName("eventDate");
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.HOLIDAY))
					d.setValue(Option.HOLIDAY);
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.BREAK))
					d.setValue(Option.BREAK);
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DISABLED))
					d.setEnabled(false);
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.PAST)) {
					d.addStyleName("past");
				}
					
			}
		}
		
		public D get(int day) {
			return iDays.get(day);
		}
		
		protected void fireDaySelected(int day) {}
		
		public List<P> getWeeks() { return iWeeks; }
		public P getDays(int dayOfWeek) { return iWeekDays[dayOfWeek]; }
		public List<D> getDays() { return iDays; }
		public P getCorner() { return iCorner; }
		
		public int getYear() { return iSessionMonth.getYear(); }
		public int getMonth() { return iSessionMonth.getMonth(); }
		public String getName() { return SingleDateSelector.monthName(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1); }
	}
	
	public class L extends org.unitime.timetable.gwt.client.widgets.P {
		public L(Option option, String... styles) {
			super(styles);
			addStyleName("clickable");
			if (option != null)
				addStyleName(option.getStyleName());
		}
	}
	
	public static class FP extends org.unitime.timetable.gwt.client.widgets.P implements Focusable{
		FP iPrevious, iNext;
		
		public FP(String... styles) {
			super(styles);
			getElement().setTabIndex(0);
			sinkEvents(Event.ONKEYDOWN);
		}
		
		public void setPrevious(FP prev) { iPrevious = prev; }
		public void setNext(FP next) { iNext = next; }
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_SPACE) {
					clickElement(getElement());
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP) {
					if (iPrevious != null) iPrevious.setFocus(true);
					else if (iNext != null) {
						FP next = iNext;
						while (next.iNext != null)
							next = next.iNext;
						next.setFocus(true);
					}
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
					if (iNext != null) iNext.setFocus(true);
					else if (iPrevious != null) {
						FP prev = iPrevious;
						while (prev.iPrevious != null)
							prev = prev.iPrevious;
						prev.setFocus(true);
					}
					event.stopPropagation();
			    	event.preventDefault();
				}
				break;
			}
			super.onBrowserEvent(event);
		}
		
		@Override
		public int getTabIndex() {
			return getElement().getTabIndex();
		}

		@Override
		public void setTabIndex(int index) {
			getElement().setTabIndex(index);
		}

		@Override
		public void setAccessKey(char key) {
			FocusImpl.getFocusImplForWidget().setAccessKey(getElement(), key);
		}

		@Override
		public void setFocus(boolean focused) {
			if (focused)
				getElement().focus();
			else
				getElement().blur();
		}
	}
	
	public class Legend extends AbsolutePanel {
		private P iBox;
		private L iSelected;
		private FP iSelectedText;
		
		public Legend() {
			addStyleName("legend");
			iBox = new P(null, "box");
			add(iBox);
			FP first = addOption(null);
			FP last = first;
			for (final Option option: Option.values()) {
				FP op = addOption(option);
				op.setPrevious(last);
				last.setNext(op);
				last = op;
			}
			first.setPrevious(last);
			last.setNext(first);
		}
		
		protected FP addOption(final Option option) {
			P line = new P(null, "row");
			final L l = new L(option, "cell", "first");
			line.add(l);
			final FP p = new FP("title");
			p.setText(option == null ? CMSG.legendNoHoliday() : option.getLabel()); 
			line.add(p);
			if (sSelectedOption == option) {
				iSelected = l; iSelectedText  = p;
				iSelected.addStyleName("selected-box");
				iSelectedText.addStyleName("selected-title");
			}
			ClickHandler cl = new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (iSelected != null) {
						iSelected.removeStyleName("selected-box");
						iSelectedText.removeStyleName("selected-title");
					}
					sSelectedOption = option;
					iSelected = l; iSelectedText = p;
					iSelected.addStyleName("selected-box");
					iSelectedText.addStyleName("selected-title");
				}
			};
			l.addClickHandler(cl); p.addClickHandler(cl);
			iBox.add(line);
			return p;
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<SelectedDate>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	private int dayOfYear(int year, int month, int day) {
		int dayOfYear = SingleDateSelector.dayOfYear(year, month, day);
		if (year < iSessionYear) {
			dayOfYear -= SingleDateSelector.dayOfYear(year,12,31);
		} else if (year > iSessionYear) {
			dayOfYear += SingleDateSelector.dayOfYear(iSessionYear, 12, 31);
		}
		return dayOfYear;
	}

	@Override
	public List<SelectedDate> getValue() {
		List<SelectedDate> ret = new ArrayList<SelectedDate>();
		DateTimeFormat df = DateTimeFormat.getFormat("yyyy/MM/dd");
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					if (d.getValue() != null) ret.add(new SelectedDate(df.parse(s.getYear() + "/" + (1 + s.getMonth()) + "/" + (1 + d.getNumber())), d.getValue()));
				}
			}
		}
		return ret;
	}
	
	public int getSelectedDaysCount() {
		int ret = 0;
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					if (d.getValue() != null) ret ++;
				}
			}
		}
		return ret;
	}
	
	public List<Integer> getSelectedDays(Option option) {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					if (d.getValue() == option) ret.add(dayOfYear(s.getYear(), 1 + s.getMonth(), 1 + d.getNumber()));
				}
			}
		}
		return ret;
	}
	
	public Date getDate(int day) {
		return SingleDateSelector.dayOfYear(iSessionYear, day);
	}

	@Override
	public void setValue(List<SelectedDate> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<SelectedDate> value, boolean fireEvents) {
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays())
					d.setValue(null);
				if (value != null) {
					for (SelectedDate d: value) {
						if (s.getYear() == Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(d.getDate())) &&
							s.getMonth() + 1 == Integer.parseInt(DateTimeFormat.getFormat("MM").format(d.getDate()))) {
							s.get(Integer.parseInt(DateTimeFormat.getFormat("dd").format(d.getDate())) - 1).setValue(d.getOption());
						}
					}
				}
			}
		}
		if (fireEvents) {
			ValueChangeEvent.fire(this, getValue());
		}
	}
	
	public void clearMessage() { iPanel.clearHint(); }
	public void setMessage(String message) { iPanel.setHint(message); }
	public void setError(String message) { iPanel.setErrorHint(message); }
	
	public boolean isEnabled(Date date) {
		int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
		int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
		int day = Integer.parseInt(DateTimeFormat.getFormat("dd").format(date));
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				if (s.getYear() == year && s.getMonth() + 1 == month)
					return s.get(day - 1).isEnabled();
			}
		}
		return false;
	}
	
	public boolean hasFlag(Date date, SessionMonth.Flag flag) {
		int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
		int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
		int day = Integer.parseInt(DateTimeFormat.getFormat("dd").format(date));
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				if (s.getYear() == year && s.getMonth() + 1 == month)
					return s.get(day - 1).hasFlag(flag);
			}
		}
		return false;
	}
	
	public static class DatesPanel extends AbsolutePanel implements Focusable, HasAllFocusHandlers {
		private int iMonth = -1, iDow = -1, iWeek = -1;
		private AriaTextBox iText = null;
		private boolean iHasFocus = false;
		
		public DatesPanel() {
			super();
			
			setStyleName("unitime-DateSelector");
			
			iText = new AriaTextBox();
			iText.addStyleName("text");
			iText.addKeyUpHandler(new KeyUpHandler() {
				@Override
				public void onKeyUp(KeyUpEvent event) {
					switch (event.getNativeKeyCode()) {
					case KeyCodes.KEY_RIGHT:
	            		moveLeftOrRight(false);
	            		break;
	            	case KeyCodes.KEY_LEFT:
	            		moveLeftOrRight(true);
	            		break;
	            	case KeyCodes.KEY_UP:
	            		moveUpOrDown(true);
	            		break;
	            	case KeyCodes.KEY_DOWN:
	            		moveUpOrDown(false);
	            		break;
	            	case 32:
	            		if (iText.getValue().isEmpty()) select(sSelectedOption);
            			break;
	            	case KeyCodes.KEY_ENTER:
	            		if (iText.getValue().isEmpty() || !parseText(iText.getValue(), sSelectedOption))
	            			select(sSelectedOption);
	            		break;
					}
				}
			});
			iText.addKeyDownHandler(new KeyDownHandler() {
				@Override
				public void onKeyDown(KeyDownEvent event) {
					switch (event.getNativeEvent().getKeyCode()) {
					case KeyCodes.KEY_RIGHT:
					case KeyCodes.KEY_LEFT:
					case KeyCodes.KEY_UP:
					case KeyCodes.KEY_DOWN:
						event.preventDefault();
					}
				}
			});
			iText.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					switch (event.getNativeEvent().getKeyCode()) {
	            	case 32:
	            		if (iText.getValue().isEmpty())
	            			event.preventDefault();
	            		return;
					case KeyCodes.KEY_ENTER:
	            		event.preventDefault();
	            		return;
					}
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							parseText(iText.getValue(), null);
						}
					});
				}
			});
			iText.addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					// addStyleName("unitime-DateSelectorFocus");
					iHasFocus = true;
					String selection = toAriaString();
					if (iMonth >= 0) {
						addCursorStyleName(iMonth, iDow, iWeek);
						if (selection.isEmpty())
							iText.setAriaLabel(ARIA.datesSelectionNoSelection(iText.getAriaLabel()));
						else
							iText.setAriaLabel(ARIA.datesSelectionWithSelection(selection, iText.getAriaLabel()));
					} else {
						if (selection.isEmpty())
							iText.setAriaLabel(ARIA.datesSelection());
						else
							iText.setAriaLabel(ARIA.datesSelectionWithSelectionNoCursor(selection));
					}
				}
			});
			iText.addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					// removeStyleName("unitime-DateSelectorFocus");
					if (iMonth >= 0)
						removeCursorStyleName(iMonth, iDow, iWeek);
					iHasFocus = false;
				}
			});
			
			add(iText);
			
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	if (!iHasFocus) iText.setFocus(true);
		    	break;
			}
		}
		
		@Override
		public void clear() {
			super.clear();
			iMonth = -1; iDow = -1; iWeek = -1;
			add(iText);
		}
		
		private void setCursor(Date date) {
			int dateYear = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
			int dateMonth = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date)) - 1;
			int dateDay = Integer.parseInt(DateTimeFormat.getFormat("dd").format(new Date())) - 1;
			
			SingleMonth lastMonth = null;
			for (int i = 0; i < getWidgetCount() - 2; i++) {
				if (getWidget(1 + i) instanceof SingleMonth) {
					SingleMonth m = (SingleMonth)getWidget(1 + i);
					// before
					if (i == 0 && dateYear < m.getYear() || (dateYear == m.getYear() && dateMonth < m.getMonth())) {
						D last = null;
						for (D d: m.getDays())
							if (d.isEnabled()) last = d;
						if (last != null) {
							iMonth = i; iDow = last.getDow(); iWeek = last.getWeek();
							last.addStyleName("cursor");
							return;
						}
					}
					// match
					if (m.getYear() == dateYear && m.getMonth() == dateMonth) {
						D d = m.getDays().get(dateDay);
						if (d.isEnabled()) {
							iMonth = i; iDow = d.getDow(); iWeek = d.getWeek();
							d.addStyleName("cursor");
							return;
						} else {
							D first = null, last = null;
							for (D x: m.getDays())
								if (x.isEnabled()) {
									if (first == null) first = x;
									last = x;
								}
							if (first != null && dateDay < first.getNumber()) {
								iMonth = i; iDow = first.getDow(); iWeek = first.getWeek();
								first.addStyleName("cursor");
								return;
							} else if (last != null) {
								iMonth = i; iDow = last.getDow(); iWeek = last.getWeek();
								last.addStyleName("cursor");
								return;
							}
						}
					}
					lastMonth = m;
				}
			}
			
			D last = null;
			for (D d: lastMonth.getDays())
				if (d.isEnabled()) last = d;
			if (last != null) {
				iMonth = getWidgetIndex(lastMonth) - 1; iDow = last.getDow(); iWeek = last.getWeek();
				last.addStyleName("cursor");
				return;
			}
		}
		
		public void setCursor(int... cursor) {
			if (iMonth >= 0)
				removeCursorStyleName(iMonth, iDow, iWeek);
			if (cursor == null) {
				iMonth = -1; iDow = -1; iWeek = -1;
			} else {
				iMonth = cursor[0]; iDow = cursor[1]; iWeek = cursor[2];
				if (iMonth >= 0)
					addCursorStyleName(iMonth, iDow, iWeek);
			}
		}
		
		private void addCursorStyleName(int month, int dow, int week) {
			if (!iHasFocus) return;
			SingleMonth m = (SingleMonth)getWidget(1 + month);
			if (dow >= 0) {
				if (week >= 0) {
					for (D d: m.getDays())
						if (d.getWeek() == week && d.getDow() == dow) {
							d.addStyleName("cursor");
							iText.setAriaLabel(d.getValue() != null ? ARIA.datesSelected(d.getAriaLabel()) : d.getAriaLabel());
						}
				} else {
					P p = m.getDays((7 - SingleDateSelector.startingDayOfWeek() + dow) % 7);
					p.addStyleName("cursor");
					iText.setAriaLabel(p.getAriaLabel()); 
				}
			} else {
				if (week >= 0) {
					P p = m.getWeeks().get(week);
					p.addStyleName("cursor");
					iText.setAriaLabel(p.getAriaLabel());
				} else {
					P p = m.getCorner();
					p.addStyleName("cursor");
					iText.setAriaLabel(p.getAriaLabel());
				}
			}
		}
		
		private String getAriaString(int month, int dow, int week) {
			SingleMonth m = (SingleMonth)getWidget(1 + month);
			if (dow >= 0) {
				if (week >= 0) {
					for (D d: m.getDays())
						if (d.getWeek() == week && d.getDow() == dow)
							return d.getAriaLabel();
				} else {
					P p = m.getDays((7 - SingleDateSelector.startingDayOfWeek() + dow) % 7);
					return p.getAriaLabel(); 
				}
			} else {
				if (week >= 0) {
					P p = m.getWeeks().get(week);
					return p.getAriaLabel(); 
				} else {
					P p = m.getCorner();
					return p.getAriaLabel(); 
				}
			}
			return "";
		}
		
		private void removeCursorStyleName(int month, int dow, int week) {
			SingleMonth m = (SingleMonth)getWidget(1 + month);
			if (dow >= 0) {
				if (week >= 0) {
					for (D d: m.getDays())
						if (d.getWeek() == week && d.getDow() == dow) {
							d.removeStyleName("cursor");
						}
				} else {
					m.getDays((7 - SingleDateSelector.startingDayOfWeek() + dow) % 7).removeStyleName("cursor");					
				}
			} else {
				if (week >= 0) {
					m.getWeeks().get(week).removeStyleName("cursor");					
				} else {
					m.getCorner().removeStyleName("cursor");
				}
			}
		}
		
		private void moveUpOrDown(boolean up) {
			if (iMonth >= 0)
				removeCursorStyleName(iMonth, iDow, iWeek);
			else {
				months: for (int i = 0; i < getWidgetCount() - 2; i++)
					if (getWidget(1 + i) instanceof SingleMonth) {
						SingleMonth m = (SingleMonth)getWidget(1 + i);
						for (D d: m.getDays())
							if (d.isEnabled()) {
								iDow = d.getDow();
								if (!up) break months;
							}
					}
			}
			
			int[] first = null, last = null, current = null, previous = null, next = null;
			int startingDayOfWeek = SingleDateSelector.startingDayOfWeek();
			for (int x = -1; x < 7; x++) {
				int k = (x < 0 ? x : (x + startingDayOfWeek) % 7);
				for (int i = 0; i < getWidgetCount() - 2; i++) {
					if (getWidget(1 + i) instanceof SingleMonth) {
						SingleMonth m = (SingleMonth)getWidget(1 + i);
						
						for (int j = -1; j < m.getWeeks().size(); j++) {
							boolean enabled = false;
							for (D d: m.getDays()) {
								if ((j == -1 || j == d.getWeek()) && (k == -1 || k == d.getDow()) && d.isEnabled()) {
									enabled = true;
									break;
								}
							}
							if (enabled && first == null) first = new int[] {i, j, k};
							if (enabled) last = new int[] {i, j, k};
							if (i== iMonth && j == iWeek && k == iDow )
								current = new int[] {i, j, k};
							else if (current == null) {
								if (enabled) previous = new int[] {i, j, k};
							} else if (next == null) {
								if (enabled) next = new int[] {i, j, k};
							}
						}
					}
				}
			}
			if (up) {
				if (previous != null) {
					iMonth = previous[0]; iWeek = previous[1]; iDow = previous[2];
				} else if (last != null) {
					iMonth = last[0]; iWeek = last[1]; iDow = last[2];
				}
			} else {
				if (next != null) {
					iMonth = next[0]; iWeek = next[1]; iDow = next[2];
				} else if (first != null) {
					iMonth = first[0]; iWeek = first[1]; iDow = first[2];
				}
			}
			addCursorStyleName(iMonth, iDow, iWeek);
		}
		
		private void moveLeftOrRight(boolean left) {
			if (iMonth >= 0)
				removeCursorStyleName(iMonth, iDow, iWeek);
			else {
				months: for (int i = 0; i < getWidgetCount() - 2; i++)
					if (getWidget(i) instanceof SingleMonth) {
						SingleMonth m = (SingleMonth)getWidget(1 + i);
						boolean enabled = false;
						for (D d: m.getDays())
							if (d.isEnabled()) { enabled = true; break; }
						if (enabled) {
							iMonth = i;
							if (!left) break months;
						}
					}
			}
			
			int[] first = null, last = null, current = null, previous = null, next = null;
			int startingDayOfWeek = SingleDateSelector.startingDayOfWeek();
			for (int i = 0; i < getWidgetCount() - 2; i++) {
				if (getWidget(1 + i) instanceof SingleMonth) {
					SingleMonth m = (SingleMonth)getWidget(1 + i);
					for (int j = -1; j < m.getWeeks().size(); j++) {
						for (int x = -1; x < 7; x++) {
						int k = (x < 0 ? x : (x + startingDayOfWeek) % 7);
							boolean enabled = false;
							for (D d: m.getDays()) {
								if ((j == -1 || j == d.getWeek()) && (k == -1 || k == d.getDow()) && d.isEnabled()) {
									enabled = true;
									break;
								}
							}
							if (enabled && first == null) first = new int[] {i, j, k};
							if (enabled) last = new int[] {i, j, k};
							if (i== iMonth && j == iWeek && k == iDow )
								current = new int[] {i, j, k};
							else if (current == null) {
								if (enabled) previous = new int[] {i, j, k};
							} else if (next == null) {
								if (enabled) next = new int[] {i, j, k};
							}
						}
					}
				}
			}
			if (left) {
				if (previous != null) {
					iMonth = previous[0]; iWeek = previous[1]; iDow = previous[2];
				} else if (last != null) {
					iMonth = last[0]; iWeek = last[1]; iDow = last[2];
				}
			} else {
				if (next != null) {
					iMonth = next[0]; iWeek = next[1]; iDow = next[2];
				} else if (first != null) {
					iMonth = first[0]; iWeek = first[1]; iDow = first[2];
				}
			}
			addCursorStyleName(iMonth, iDow, iWeek);
		}
		
		private boolean hasUnselectedDays(Option selection) {
			if (iMonth < 0) return false;
			SingleMonth m = (SingleMonth)getWidget(1 + iMonth);
			for (D d: m.getDays())
				if ((iWeek == -1 || iWeek == d.getWeek()) && (iDow == -1 || iDow == d.getDow()) && d.isEnabled()) {
					if (selection != d.getValue()) return true;
				}
			return false;
		}
		
		public void setAllSelected(Option selected) {
			if (iMonth < 0) return;
			SingleMonth m = (SingleMonth)getWidget(1 + iMonth);
			if (iWeek < 0) {
				for (D d: m.getDays())
					if ((iWeek == -1 || iWeek == d.getWeek()) && (iDow == -1 || iDow == d.getDow()) && d.isEnabled()) {
						d.setValue(selected, true);
					}
			} else {
				for (D d: m.getWeeks().get(iWeek).getDays()) {
					if ((iDow == -1 || iDow == d.getDow()) && d.isEnabled()) {
						d.setValue(selected, true);
					}
				}
			}
			String aria = getAriaString(iMonth, iDow, iWeek);
			if (selected != null) {
				AriaStatus.getInstance().setHTML(ARIA.datesSelected(aria));
			} else {
				AriaStatus.getInstance().setHTML(ARIA.datesUnselected(aria));
			}
		}
		
		private void select(Option select) {
	    	iText.setValue("");
	    	if (select != null && hasUnselectedDays(select))
	    		setAllSelected(select);
	    	else
	    		setAllSelected(null);
		}
		
		private void select(List<D> days, Option option) {
	    	iText.setValue("");
	    	String aria = "";
	    	for (D d: days) {
	    		d.setValue(option, true);
	    		if (!aria.isEmpty()) aria += ", ";
	    		aria += d.getAriaLabel();
	    	}
	    	if (option != null)
	    		AriaStatus.getInstance().setHTML(ARIA.datesSelected(aria));
	    	else
	    		AriaStatus.getInstance().setHTML(ARIA.datesUnselected(aria));
		}
		
		private String toAriaString() {
			String aria = "";
			for (int i = 0; i < getWidgetCount() - 2; i++) {
				if (getWidget(1 + i) instanceof SingleMonth) {
					SingleMonth m = (SingleMonth)getWidget(1 + i);
					for (D d: m.getDays()) {
						if (d.getValue() != null) {
				    		if (!aria.isEmpty()) aria += ", ";
				    		aria += d.getAriaLabel();
						}
					}
				}
			}
			return aria;
		}
		
		private boolean parseText(String text, Option select) {
			if (text == null || text.isEmpty()) return false;
			
			if (text.endsWith("?")) {
				String aria = toAriaString();
				if (aria.isEmpty()) {
					AriaStatus.getInstance().setHTML(ARIA.datesNothingSelected());
				} else {
					AriaStatus.getInstance().setHTML(ARIA.datesSelected(aria));
				}				
				iText.setValue("");
				return false;
			}
			
			int pMonth = -1, pDow = -1, pWeek = -1, dCnt = -1;
			boolean wk = false;
			boolean dayFirst = CONSTANTS.firstDayThenMonth();
			if (text.contains(".")) dayFirst = true;
			if (text.contains("/")) dayFirst = false;
			Integer day = null;
			
			String[] tokens = text.split("[\\./\\- ]");
			tokens: for (String token: tokens) {
				if (token.isEmpty()) continue;
				
				// Is it a day of week
				if (pDow < 0)
					for (int d = 0; d < 7; d++) {
						if (CONSTANTS.longDays()[d].toLowerCase().startsWith(token.toLowerCase())) {
							pDow = d;
							continue tokens;
						}
					}

				// Is it a number?
				Integer number = null;
				try {
					number = Integer.parseInt(token);
				} catch (NumberFormatException e) {}
				
				// Is it a day of month?
				if (day == null && number != null && tokens.length > 1 && dayFirst && pMonth < 0 && number >= 1 && number <= 31) {
					day = number;
					continue tokens;
				}
				
				// Is it a month?
				if (pMonth < 0) {
					for (int i = 0; i < getWidgetCount() - 2; i++) {
						if (getWidget(1 + i) instanceof SingleMonth) {
							SingleMonth m = (SingleMonth)getWidget(1 + i);
							if ((number != null && m.getMonth() + 1 == number) || m.getName().toLowerCase().startsWith(token.toLowerCase())) {
								pMonth = i;
								if (day != null && (pWeek < 0 || pDow < 0)) {
									int[] first = null, last = null;
									for (D d: m.getDays()) {
										if (!d.isEnabled()) continue;
										if (first == null) first = new int[] {d.getNumber(), d.getDow(), d.getWeek()};
										last = new int[] {d.getNumber(), d.getDow(), d.getWeek()};
										if (d.getNumber() + 1 == day) { pWeek = d.getWeek(); pDow = d.getDow(); day = null; continue tokens; }
									}
									if (first != null) {
										if (day < first[0]) { pDow = first[2]; pWeek = first[3]; } else { pDow = last[2]; pWeek = last[3]; }
									}
								}
								continue tokens;
							}
						}
					}
				}
				
				// Is it a week?
				if (pMonth < 0 && number != null && day == null) {
					for (int i = 0; i < getWidgetCount() - 2; i++) {
						if (getWidget(1 + i) instanceof SingleMonth) {
							SingleMonth m = (SingleMonth)getWidget(1 + i);
							for (P p: m.getWeeks())
								if (p.getCaption().equals(token)) { pMonth = i; pWeek = m.getWeeks().indexOf(p); wk = true; continue tokens; }
						}
					}
				}
				
				// Is it a day?
				if (pMonth >= 0 && (pWeek < 0 || pDow < 0) && number != null) {
					SingleMonth m = (SingleMonth)getWidget(1 + pMonth);
					int[] first = null, last = null;
					for (D d: m.getDays()) {
						if (!d.isEnabled()) continue;
						if (first == null) first = new int[] {d.getNumber(), d.getDow(), d.getWeek()};
						last = new int[] {d.getNumber(), d.getDow(), d.getWeek()};
						if (d.getNumber() + 1 == number) { pWeek = d.getWeek(); pDow = d.getDow(); continue tokens; }
					}
					if (first != null) {
						if (number < first[0]) { pDow = first[2]; pWeek = first[3]; } else { pDow = last[2]; pWeek = last[3]; }
						continue tokens;
					}
				}
				
				// Is it a count?
				if (number != null && number >= 1) {
					dCnt = number;
					continue tokens;
				} else if (token.length() >= 2) {
					try {
						char last = token.charAt(token.length() - 1);
						number = Integer.parseInt(token.substring(0, token.length() - 1));
						if (number >= 1) {
							switch (last) {
							case 'x':
								dCnt = number; continue tokens;
							case 'w':
								dCnt = number; wk = true; continue tokens;
							case 'd':
								dCnt = number; wk = false; continue tokens;
							}
						}
					} catch (NumberFormatException e) {}
				}
			}
			
			boolean cursor = false;
			if (pMonth >= 0) {
				setCursor(pMonth, pDow, pWeek);
				cursor = true;
			} else if (pDow >= 0 && iMonth >= 0) {
				setCursor(iMonth, pDow, pWeek);
				cursor = true;
			} 
			
			if (select != null && cursor) {
				if (dCnt < 0) select(select);
				else if (dCnt > 0) {
					List<D> days = new ArrayList<D>();
					boolean match = false;
					for (int i = iMonth; i < getWidgetCount() - 2; i++) {
						if (getWidget(1 + i) instanceof SingleMonth) {
							SingleMonth m = (SingleMonth)getWidget(1 + i);
							for (D d: m.getDays()) {
								if (!match && (iDow < 0 || d.getDow() == iDow) && (iWeek < 0 || d.getWeek() == iWeek)) { match = true; }
								if (match && d.isEnabled() && dCnt > 0) {
									if (wk && d.getDow() != iDow) continue;
									days.add(d); dCnt --;
								}
							}
						}
					}
					select(days, select);
				}
			}
			
			return cursor;
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
	}

	@Override
	public int getTabIndex() {
		return iPanel.getWidget().getTabIndex();
	}

	@Override
	public void setAccessKey(char key) {
		iPanel.getWidget().setAccessKey(key);
	}

	@Override
	public void setFocus(boolean focused) {
		iPanel.getWidget().setFocus(focused);
	}

	@Override
	public void setTabIndex(int index) {
		iPanel.getWidget().setTabIndex(index);
	}
	
	public static enum Option {
		HOLIDAY('1', "selected-holiday", CMSG.legendHoliday()),
		BREAK('2', "selected-break", CMSG.legendBreak()),
		;
		private char iCode;
		private String iStyleName;
		private String iLabel;
		Option(char code, String styleName, String label) {
			iCode = code;
			iStyleName = styleName;
			iLabel = label;
		}

		public char getCode() { return iCode; }
		public String getStyleName() { return iStyleName; }
		public String getLabel() { return iLabel; }
		public static Option getOption(char code) {
			for (Option o: Option.values())
				if (code == o.getCode()) return o;
			return null;
		}
	};

	public static class SelectedDate {
		private Date iDate;
		private Option iOption;
		
		public SelectedDate(Date date, Option option) {
			iDate = date; iOption = option;
		}
		
		public Date getDate() { return iDate; }
		public Option getOption() { return iOption; }
	}
	
	public static native void clickElement(Element elem) /*-{
		elem.click();
	}-*/;

}
