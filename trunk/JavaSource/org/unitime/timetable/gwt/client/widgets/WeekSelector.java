/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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

import java.util.Date;

import org.unitime.timetable.gwt.client.widgets.IntervalSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.DateInterface;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public class WeekSelector extends IntervalSelector<WeekInterface>{
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private RegExp[] iRegExp = new RegExp[] {
			RegExp.compile("^[^0-9]*([0-9]+)[/ ]*([0-9]*)[ -]*([0-9]*)[/ ]*([0-9]*)$"),
			RegExp.compile("^[^0-9]*([0-9]+)\\.?([0-9]*)\\.?[ -]*([0-9]*)\\.?([0-9]*)\\.?$")
	};

	private AcademicSessionProvider iAcademicSession;
	
	
	public WeekSelector(AcademicSessionProvider session) {
		super(true);
		iAcademicSession = session;
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				init(iAcademicSession.getAcademicSessionId());
			}
		});
		
		session.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				init(event.getNewAcademicSessionId());
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<Interval>() {
			@Override
			public void onValueChange(ValueChangeEvent<Interval> event) {
				setAriaLabel(ARIA.weekSelection(toAriaString()));
			}
		});
		
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				setAriaLabel(ARIA.weekSelection(toAriaString()));
			}
		});
	}
	
	public void init(Long sessionId) {
		if (sessionId == null) {
			setHint("No academic session is selected.");
		} else {
			setHint("Loading data for " + iAcademicSession.getAcademicSessionName() + " ...");
			RPC.execute(new WeekSelectorRequest(sessionId), new AsyncCallback<GwtRpcResponseList<WeekInterface>>() {
				@Override
				public void onFailure(Throwable caught) {
					setErrorHint(caught.getMessage());
				}
				@Override
				public void onSuccess(GwtRpcResponseList<WeekInterface> result) {
					clearHint();
					setValues(result);
					setDefaultValue(new Interval());
					setValue(getDefaultValue());
				}
			});
		}
	}

	public static class WeekSelectorRequest implements GwtRpcRequest<GwtRpcResponseList<WeekInterface>> {
		private Long iSessionId;
		
		public WeekSelectorRequest() {}
		public WeekSelectorRequest(Long sessionId) { iSessionId = sessionId; }
		
		public Long getSessionId() { return iSessionId; }
		@Override
		public String toString() {
			return getSessionId().toString();
		}
	}
	
	@Override
	public Interval parse(String query) {
		if (query == null || getValues() == null) return new Interval();
		if ("today".equalsIgnoreCase(query)) {
			Date today = new Date();
			int m1 = Integer.parseInt(DateTimeFormat.getFormat("MM").format(today));
			int d1 = Integer.parseInt(DateTimeFormat.getFormat("dd").format(today));
			WeekInterface first = find(m1, d1, null);
			return new Interval(first,  null);
		}
		MatchResult match = iRegExp[0].exec(query);
		if (match != null) {
			int m1 = Integer.parseInt(match.getGroup(1));
			int d1 = (match.getGroup(2).isEmpty() ? 1 : Integer.parseInt(match.getGroup(2)));
			WeekInterface first = find(m1, d1, null);
			int m2 = (match.getGroup(3).isEmpty() ? -1 : Integer.parseInt(match.getGroup(3)));
			int d2 = (match.getGroup(4).isEmpty() ? 1 : Integer.parseInt(match.getGroup(4)));
			if (m2 == m1 && d2 < d1) d2 = d1;
			WeekInterface last = (match.getGroup(3).isEmpty() ? null : find(m2, d2, first));
			return new Interval(first, last);
		}
		match = iRegExp[1].exec(query);
		if (match != null) {
			int d1 = Integer.parseInt(match.getGroup(1));
			int m1 = (match.getGroup(2).isEmpty() ? -1 : Integer.parseInt(match.getGroup(2)));
			WeekInterface first = find(m1, d1, null);
			int d2 = (match.getGroup(3).isEmpty() ? 1 : Integer.parseInt(match.getGroup(3)));
			int m2 = (match.getGroup(4).isEmpty() ? -1 : Integer.parseInt(match.getGroup(4)));
			if (m2 == m1 && d2 < d1) d2 = d1;
			WeekInterface last = (match.getGroup(3).isEmpty() ? null : find(m2, d2, first));
			return new Interval(first, last);
		}

		return new Interval();
	}
	
	public WeekInterface find(int month, int day, WeekInterface after) {
		WeekInterface first = null;
		for (WeekInterface w: getValues()) {
			if (after != null && w.getDayOfYear() < after.getDayOfYear()) continue;
			if (first == null) first = w;
			for (DateInterface dayName : w.getDayNames()) {
				if (dayName.getMonth() == month && dayName.getDay() == day) return w;
			}
		}
		DateInterface firstDay = getValues().get(0).getDayNames().get(0);
		return (month < firstDay.getMonth() || (firstDay.getMonth() == month && day < firstDay.getDay()) ? first == null ? getValues().get(0) : first : getValues().get(getValues().size() - 1));
	}
	
	@Override
	public String getDisplayString(Interval interval) {
		if (interval.isAll())
			return interval.isEnableFilter() ? MESSAGES.itemAllWeeksWithFilter() : MESSAGES.itemAllWeeks();
		if (interval.isOne())
			return MESSAGES.itemWeek(interval.getFirst().getDayNames().get(0).getLabel(), interval.getFirst().getDayNames().get(interval.getFirst().getDayNames().size() - 1).getLabel());
		return "&nbsp;&nbsp;&nbsp;" + interval.getFirst().getDayNames().get(0) + " - " + interval.getLast().getDayNames().get(6);
	}

	@Override
	public String getReplaceString(Interval interval) {
		if (interval.isAll())
			return interval.isEnableFilter() ? MESSAGES.itemAllWeeksWithFilter() : MESSAGES.itemAllWeeks();
		if (interval.isOne())
			return MESSAGES.itemWeek(interval.getFirst().getDayNames().get(0).getLabel(), interval.getFirst().getDayNames().get(interval.getFirst().getDayNames().size() - 1).getLabel());
		return MESSAGES.itemWeeks(interval.getFirst().getDayNames().get(0).getLabel(), interval.getLast().getDayNames().get(6).getLabel());
	}
	
	public String getSelection() {
		if (getValue() == null || getValue().isAll()) return "";
		return (getValue().isOne() ? getValue().getFirst().getDayNames().get(0).getLabel() : getValue().getFirst().getDayNames().get(0).getLabel() + "-" + getValue().getLast().getDayNames().get(6).getLabel());
	}
	
	public int getFirstDayOfYear() {
		if (getValues() == null || getValues().isEmpty()) return 0;
		return getValues().get(0).getDayOfYear();
	}
	
	public int getLastDayOfYear() {
		if (getValues() == null || getValues().isEmpty()) return 0;
		return getValues().get(getValues().size() - 1).getDayOfYear() + 6;
	}
}