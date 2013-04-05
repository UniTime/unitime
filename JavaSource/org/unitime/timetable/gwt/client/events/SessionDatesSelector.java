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
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class SessionDatesSelector extends Composite implements HasValue<List<Date>> {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	AcademicSessionProvider iAcademicSession;
	UniTimeWidget<AbsolutePanel> iPanel;
	private int iSessionYear = 1900;
	private boolean iCanSelectPast = false;
	
	public SessionDatesSelector(AcademicSessionProvider session) {
		iAcademicSession = session;
		
		iPanel = new UniTimeWidget<AbsolutePanel>(new AbsolutePanel());
		iPanel.getWidget().setStyleName("unitime-DateSelector");
		
		initWidget(iPanel);
		
		iAcademicSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				if (event.isChanged()) init(event.getNewAcademicSessionId());
			}
		});
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				init(iAcademicSession.getAcademicSessionId());
			}
		});
	}
	
	public boolean isCanSelectPast() { return iCanSelectPast; }
	public void setCanSelectPast(boolean canSelectPast) { iCanSelectPast = canSelectPast; }
	
	public void init(Long sessionId) {
		if (sessionId == null) {
			iPanel.setHint(MESSAGES.hintNoSession());
		} else {
			iPanel.setHint(MESSAGES.waitLoadingDataForSession(iAcademicSession.getAcademicSessionName()));
			RPC.execute(new RequestSessionDetails(sessionId), new AsyncCallback<GwtRpcResponseList<SessionMonth>>() {

				@Override
				public void onFailure(Throwable caught) {
					iPanel.setErrorHint(caught.getMessage());
				}

				@Override
				public void onSuccess(GwtRpcResponseList<SessionMonth> result) {
					iPanel.clearHint();
					init(result);
				}
			});
		}
	}
	
	public void init(List<SessionMonth> months) {
		iPanel.getWidget().clear();
		int firstOutside = -1, start = -1, end = -1, exam = -1, firstHoliday = - 1, firstBreak = -1, today = -1, firstPast = -1, firstEventDate = -1, firstClassDate = -1; 
		for (SessionMonth month: months) {
			iPanel.getWidget().add(new SingleMonth(month, isCanSelectPast()));
			if (start < 0) start = month.getFirst(SessionMonth.Flag.START);
			if (end < 0) end = month.getFirst(SessionMonth.Flag.END);
			if (exam < 0) exam = month.getFirst(SessionMonth.Flag.FINALS);
			if (firstHoliday < 0) firstHoliday = month.getFirst(SessionMonth.Flag.HOLIDAY);
			if (firstBreak < 0) firstBreak = month.getFirst(SessionMonth.Flag.BREAK);
			if (firstOutside < 0) firstOutside = month.getFirst(SessionMonth.Flag.DISABLED);
			if (firstPast < 0) firstPast = month.getFirst(SessionMonth.Flag.PAST);
			if (firstEventDate < 0) firstEventDate = month.getFirst(SessionMonth.Flag.DATE_MAPPING_EVENT);
			if (firstClassDate < 0) firstClassDate = month.getFirst(SessionMonth.Flag.DATE_MAPPING_CLASS);
			if (month.getYear() == Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(new Date())) &&
				month.getMonth() + 1 == Integer.parseInt(DateTimeFormat.getFormat("MM").format(new Date())))
				today = Integer.parseInt(DateTimeFormat.getFormat("dd").format(new Date()));
			if (month.getFirst(SessionMonth.Flag.START) >= 0) iSessionYear = month.getYear();
		}
		iPanel.getWidget().add(new Legend(firstOutside, start, exam, firstHoliday, firstBreak, firstPast, today, firstClassDate, firstEventDate));
	}
	
	public static class P extends AbsolutePanel {
		private String iCaption;
		private List<D> iDays = new ArrayList<D>();
		
		private P(String caption, String... styles) {
			iCaption = caption;
			if (caption != null)
				getElement().setInnerHTML(caption);
			for (String style: styles)
				if (style != null && !style.isEmpty())
					addStyleName(style);
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		public void addDay(D d) { iDays.add(d); }
		
		public boolean hasUnselectedDays(boolean includeVacations, boolean includeWeekend) {
			for (D d: iDays) {
				if (!d.isEnabled()) continue;
				if (!includeVacations && d.isVacation()) continue;
				if (!includeWeekend && d.isWeekend()) continue;
				if (!d.getValue()) return true;
			}
			return false;
		}
		
		public void setAllSelected(boolean selected, boolean includeVacations, boolean includeWeekend) {
			for (D d: iDays) {
				if (!d.isEnabled()) continue;
				if (!includeVacations && d.isVacation()) continue;
				if (!includeWeekend && d.isWeekend()) continue;
				d.setValue(selected, true);
			}
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	if (hasUnselectedDays(false, false))
		    		setAllSelected(true, false, false);
		    	else if (hasUnselectedDays(false, true))
		    		setAllSelected(true, false, true);
		    	else if (hasUnselectedDays(true, true))
		    		setAllSelected(true, true, true);
		    	else
		    		setAllSelected(false, true, true);
		    	event.stopPropagation();
		    	event.preventDefault();
		    	break;
			}
		}
		
		public String getCaption() { return iCaption; }
		
		public List<D> getDays() { return iDays; }

		public String toString() {
			return getCaption() + " " + iDays;
		}
	}
	
	public static class D extends AbsolutePanel implements HasValue<Boolean> {
		private boolean iSelected = false, iEnabled = true;
		private int iFlag, iDow;
		private int iNumber;
		
		private D(int number, int flag, int dow, boolean selected, String... styles) {
			iNumber = number;
			iFlag = flag;
			iDow = dow;
			iSelected = selected;
			getElement().setInnerHTML(String.valueOf(1 + number));
			for (String style: styles)
				if (style != null && !style.isEmpty())
					addStyleName(style);
			if (iSelected) addStyleName("selected");
			sinkEvents(Event.ONMOUSEDOWN);
		}
		
		public boolean isEnabled() { return iEnabled; }
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
		
		public boolean isWeekend() {
			return iDow >= 5;
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
		    case Event.ONMOUSEDOWN:
		    	if (isEnabled())
		    		setValue(!getValue(), true);
		    	event.stopPropagation();
		    	event.preventDefault();
		    	break;
			}
		}
		
		public int getNumber() { return iNumber; }
		
		public String toString() {
			return String.valueOf(1 + getNumber());
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public Boolean getValue() {
			return iSelected;
		}

		@Override
		public void setValue(Boolean value) {
			setValue(value, false);
		}

		@Override
		public void setValue(Boolean value, boolean fireEvents) {
			if (iSelected == value || value == null || !isEnabled()) return;
			iSelected = value;
	    	if (iSelected)
	    		addStyleName("selected");
	    	else
	    		removeStyleName("selected");
			if (fireEvents)
				ValueChangeEvent.fire(this, getValue());
		}

	}
	
	public static class SingleMonth extends AbsolutePanel {
		private SessionMonth iSessionMonth;
		private List<D> iDays = new ArrayList<D>();
		private P[] iWeekDays = new P[7];
		private List<P> iWeeks = new ArrayList<P>();
		
		public SingleMonth(SessionMonth month, boolean canSelectPast) {
			iSessionMonth = month;
			addStyleName("month");
			add(new P(SingleDateSelector.monthName(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1), "label"));
			
			P box = new P(null, "box");
			add(box);
			P header = new P(null, "row");
			box.add(header);
			P corner = new P(null, "cell", "corner", "clickable");
			header.add(corner);
			
			int firstDayOfWeek = SingleDateSelector.firstDayOfWeek(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			int weekNumber = SingleDateSelector.weekNumber(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			int nrDays = SingleDateSelector.daysInMonth(iSessionMonth.getYear(), iSessionMonth.getMonth() + 1);
			
			int startDayOfWeek = SingleDateSelector.startingDayOfWeek();
			for (int i = 0; i < 7; i++) {
				iWeekDays[i] = new P(CONSTANTS.days()[(i + startDayOfWeek) % 7], "cell", "dow", "clickable");
				header.add(iWeekDays[i]);
			}

			P line = new P(null, "row");
			box.add(line);
			P week = new P(String.valueOf(weekNumber ++), "cell", "week", "clickable");
			line.add(week);
			iWeeks.add(week);
			
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
			
			ValueChangeHandler<Boolean> onChange = new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					fireDaySelected(((D)event.getSource()).getNumber());
				}
			};
			
			for (int i = 0; i < nrDays; i++) {
				if (i > 0 && idx % 7 == 0) {
					if (idx == 7 && iSessionMonth.getMonth() == 0 && weekNumber > 50) weekNumber = 1;
					line = new P(null, "row");
					box.add(line);
					week = new P(String.valueOf(weekNumber ++), "cell", "week", "clickable");
					line.add(week);
					iWeeks.add(week);
				}
				D d = new D(i, iSessionMonth.getFlags(i), (idx + startDayOfWeek) % 7, false, "cell", (((idx + startDayOfWeek) % 7) < 5 ? "day" : "weekend"), "clickable");
				line.add(d);
				d.addValueChangeHandler(onChange);
				corner.addDay(d);
				iWeekDays[idx % 7].addDay(d);
				week.addDay(d);
				iDays.add(d);
				idx++;
				if (today == i)
					d.addStyleName("today");
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.START))
					d.addStyleName("start");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.END))
					d.addStyleName("start");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.FINALS))
					d.addStyleName("exam");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.HOLIDAY))
					d.addStyleName("holiday");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.BREAK))
					d.addStyleName("break");
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DATE_MAPPING_CLASS))
					d.addStyleName("classDate");
				else if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DATE_MAPPING_EVENT))
					d.addStyleName("eventDate");
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.SELECTED))
					d.setValue(true);
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.DISABLED))
					d.setEnabled(false);
				if (iSessionMonth.hasFlag(i, SessionMonth.Flag.PAST)) {
					d.addStyleName("past");
					if (!canSelectPast) d.setEnabled(false);
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
		
		public int getYear() { return iSessionMonth.getYear(); }
		public int getMonth() { return iSessionMonth.getMonth(); }
	}
	
	public static class Legend extends AbsolutePanel {
		public Legend(int firstOutside, int start, int exam, int firstHoliday, int firstBreak, int firstPast, int today, int firstClassDate, int firstEventDate) {
			addStyleName("legend");
			P box = new P(null, "box");
			add(box);
			
			P line = new P(null, "row");
			line.add(new P(null, "cell", "selected", "first"));
			line.add(new P(MESSAGES.legendSelected(), "title"));
			box.add(line);

			line = new P(null, "row");
			line.add(new P(null, "cell"));
			line.add(new P(MESSAGES.legendNotSelected(), "title"));
			box.add(line);
			
			if (firstOutside >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstOutside + 1), "cell", "disabled"));
				line.add(new P(MESSAGES.legendNotInSession(), "title"));
				box.add(line);
			}
			
			if (firstPast >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstPast + 1), "cell", "disabled", "past"));
				line.add(new P(MESSAGES.legendPast(), "title"));
				box.add(line);
			}
			
			if (start >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(start + 1), "cell", "start"));
				line.add(new P(MESSAGES.legendClassesStartOrEnd(), "title"));
				box.add(line);
			}
			
			if (exam >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(exam + 1), "cell", "exam"));
				line.add(new P(MESSAGES.legendFinals(), "title"));
				box.add(line);
			}
			
			if (firstHoliday >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstHoliday + 1), "cell", "holiday"));
				line.add(new P(MESSAGES.legendHoliday(), "title"));
				box.add(line);
			}

			if (firstBreak >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstBreak + 1), "cell", "break"));
				line.add(new P(MESSAGES.legendBreak(), "title"));
				box.add(line);
			}
			
			if (firstClassDate >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstClassDate + 1), "cell", "classDate"));
				line.add(new P(MESSAGES.legendDateMappingClassDate(), "title"));
				box.add(line);
			}

			if (firstEventDate >= 0) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(firstEventDate + 1), "cell", "eventDate"));
				line.add(new P(MESSAGES.legendDateMappingEventDate(), "title"));
				box.add(line);
			}

			if (today >= 1) {
				line = new P(null, "row");
				line.add(new P(String.valueOf(today), "cell", "today"));
				line.add(new P(MESSAGES.legendToday(), "title"));
				box.add(line);
			}
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<Date>> handler) {
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
	public List<Date> getValue() {
		List<Date> ret = new ArrayList<Date>();
		DateTimeFormat df = DateTimeFormat.getFormat("yyyy/MM/dd");
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					if (d.getValue()) ret.add(df.parse(s.getYear() + "/" + (1 + s.getMonth()) + "/" + (1 + d.getNumber())));
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
					if (d.getValue()) ret ++;
				}
			}
		}
		return ret;
	}
	
	public List<Integer> getSelectedDays() {
		List<Integer> ret = new ArrayList<Integer>();
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays()) {
					if (d.getValue()) ret.add(dayOfYear(s.getYear(), 1 + s.getMonth(), 1 + d.getNumber()));
				}
			}
		}
		return ret;
	}
	
	public Date getDate(int day) {
		return SingleDateSelector.dayOfYear(iSessionYear, day);
	}

	@Override
	public void setValue(List<Date> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<Date> value, boolean fireEvents) {
		for (int i = 0; i < iPanel.getWidget().getWidgetCount(); i ++) {
			Widget w = iPanel.getWidget().getWidget(i);
			if (w instanceof SingleMonth) {
				SingleMonth s = (SingleMonth)w;
				for (D d: s.getDays())
					d.setValue(false);
				if (value != null) {
					for (Date d: value) {
						if (s.getYear() == Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(d)) &&
							s.getMonth() + 1 == Integer.parseInt(DateTimeFormat.getFormat("MM").format(d))) {
							s.get(Integer.parseInt(DateTimeFormat.getFormat("dd").format(d)) - 1).setValue(true);
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

}
