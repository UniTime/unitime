/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class WeekSelector extends Composite {
	HorizontalPanel iWeekPanel;
	private SuggestBox iWeek;
	private Button iPrevious, iNext;
	private List<WeekChangedHandler> iWeekChangedHandlers = new ArrayList<WeekSelector.WeekChangedHandler>();
	private List<WeekInterface> iWeeks = new ArrayList<WeekInterface>();
	private WeekSelection iWeekSelection = new WeekSelection();
	private RegExp iRegExp = RegExp.compile("[^0-9]*([0-9]+)[/ ]*([0-9]*)[ -]*([0-9]*)[/ ]*([0-9]*)");
	
	public WeekSelector() {
		iWeekPanel = new HorizontalPanel();
		iWeekPanel.setSpacing(2);
		iPrevious = new Button("&larr;", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				WeekSelection prev = iWeekSelection.previous();
				if (prev != null) {
					iWeekSelection = prev;
					fireWeekChanged();
				}
			}
		});
		iPrevious.setEnabled(false);
		iPrevious.setTitle("Previous week (Alt+p)");
		iPrevious.setAccessKey('p');
		iWeekPanel.add(iPrevious);
		iWeekPanel.setCellVerticalAlignment(iPrevious, HasVerticalAlignment.ALIGN_MIDDLE);
		iWeek = new SuggestBox(new SuggestOracle() {
			@Override
			public void requestDefaultSuggestions(Request request, Callback callback) {
				requestSuggestions(request, callback);
			}
			@Override
			public void requestSuggestions(final Request request, final Callback callback) {
				String query = request.getQuery();
				ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
				WeekSelection selection = parse(query);
				iWeek.setAutoSelectEnabled(true);
				if (!selection.isAllWeeks()) {
					ArrayList<Suggestion> extra = new ArrayList<Suggestion>();
					if (iWeek.getText().equals(selection.getReplacementString())) {
						suggestions.add(new WeekSelection());
						for (int index = 0; index < iWeeks.size(); index++) {
							if (iWeeks.get(index).getDayOfYear() == selection.getFirstDayOfYear()) {
								for (int i = 3; i > 0; i--)
									if (index - i >= 0)
										suggestions.add(new WeekSelection(iWeeks.get(index - i)));
								if (!selection.isOneWeek())
									suggestions.add(new WeekSelection(iWeeks.get(index)));
							}
							if (iWeeks.get(index).getDayOfYear() == (selection.getLastWeek() == null ? selection.getFirstWeek() : selection.getLastWeek()).getDayOfYear()) {
								for (int i = 1; i <= 3; i++) {
									if (index + i < iWeeks.size())
										extra.add(new WeekSelection(iWeeks.get(index + i)));
								}
							}
							iWeek.setAutoSelectEnabled(false);
						}
					}
					if (selection.isOneWeek()) {
						suggestions.add(selection);
						for (WeekInterface week: iWeeks) {
							if (week.getDayOfYear() > selection.getFirstDayOfYear()) {
								if (selection.isPerfectMatch())
									suggestions.add(new WeekSelection(selection.getFirstWeek(), week));
								else
									suggestions.add(new WeekSelection(week));
							}
							if (suggestions.size() + extra.size() >= request.getLimit()) break;
						}
					} else {
						if (selection.isPerfectMatch()) {
							suggestions.add(selection);	
						} else {
							suggestions.add(new WeekSelection(selection.getFirstWeek()));
							suggestions.add(selection);
							for (WeekInterface week: iWeeks) {
								if (week.getDayOfYear() > selection.getLastDayOfYear())
									suggestions.add(new WeekSelection(selection.getFirstWeek(), week));
								if (suggestions.size() + extra.size() >= request.getLimit()) break;
							}
							iWeek.setAutoSelectEnabled(false);
						}
					}
					suggestions.addAll(extra);
				} else {
					suggestions.add(new WeekSelection());
					for (WeekInterface week: iWeeks) {
						suggestions.add(new WeekSelection(week));
						if (suggestions.size()  >= request.getLimit()) break;
					}
				}
				callback.onSuggestionsReady(request, new Response(suggestions));
			}
			@Override
			public boolean isDisplayStringHTML() { return true; }
			});
		iWeek.getTextBox().addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iWeek.showSuggestionList();
			}
		});
		iWeekPanel.add(iWeek);
		iWeekPanel.setCellVerticalAlignment(iWeek, HasVerticalAlignment.ALIGN_MIDDLE);
		iNext = new Button("&rarr;", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				WeekSelection next = iWeekSelection.next();
				if (next != null) {
					iWeekSelection = next;
					fireWeekChanged();
				}
			}
		});
		iNext.setTitle("Next week (Alt+n)");
		iNext.setAccessKey('n');
		iWeekPanel.add(iNext);
		iWeekPanel.setCellVerticalAlignment(iNext, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iWeek.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				iWeekSelection = (WeekSelection)event.getSelectedItem();
				fireWeekChanged();
			}
		});
		initWidget(iWeekPanel);
	}
	
	public WeekSelection parse(String query) {
		if (query == null) return new WeekSelection();
		MatchResult match = iRegExp.exec(query);
		if (match != null) {
			int m1 = Integer.parseInt(match.getGroup(1));
			int d1 = (match.getGroup(2).isEmpty() ? 1 : Integer.parseInt(match.getGroup(2)));
			WeekInterface first = find(m1, d1, null);
			int m2 = (match.getGroup(3).isEmpty() ? -1 : Integer.parseInt(match.getGroup(3)));
			int d2 = (match.getGroup(4).isEmpty() ? 1 : Integer.parseInt(match.getGroup(4)));
			if (m2 == m1 && d2 < d1) d2 = d1;
			WeekInterface last = (match.getGroup(3).isEmpty() ? null : find(m2, d2, first));
			return new WeekSelection(first, last, (match.getGroup(3).isEmpty() && !match.getGroup(2).isEmpty()) || (!match.getGroup(3).isEmpty() && !match.getGroup(4).isEmpty()));
		}
		return new WeekSelection();
	}
	
	public WeekInterface find(int month, int day, WeekInterface after) {
		WeekInterface first = null;
		for (WeekInterface w: iWeeks) {
			if (after != null && w.getDayOfYear() < after.getDayOfYear()) continue;
			if (first == null) first = w;
			for (String dayName : w.getDayNames()) {
				int m = Integer.parseInt(dayName.substring(0, dayName.indexOf('/')));
				int d = Integer.parseInt(dayName.substring(1 + dayName.indexOf('/')));
				if (m == month && d == day) return w;
			}
		}
		String firstDay = iWeeks.get(0).getDayNames().get(0);
		int m = Integer.parseInt(firstDay.substring(0, firstDay.indexOf('/')));
		int d = Integer.parseInt(firstDay.substring(1 + firstDay.indexOf('/')));
		return (month < m || (m == month && day < d) ? first == null ? iWeeks.get(0) : first : iWeeks.get(iWeeks.size() - 1));
	}
	
	public class WeekSelection implements Suggestion {
		private WeekInterface iFirstWeek, iLastWeek;
		private boolean iPerfectMatch;
		
		public WeekSelection() {
			this(null, null, true);
		}
		
		public WeekSelection(WeekInterface week) {
			this(week, null, true);
		}
		
		public WeekSelection(WeekInterface firstWeek, WeekInterface lastWeek) {
			this(firstWeek, lastWeek, true);
		}
		
		private WeekSelection(WeekInterface firstWeek, WeekInterface lastWeek, boolean perfectMatch) {
			iFirstWeek = firstWeek; iLastWeek = lastWeek;
			if (iLastWeek != null && iLastWeek.getDayOfYear() <= iFirstWeek.getDayOfYear())
				iLastWeek = null;
			iPerfectMatch = perfectMatch;
		}
		
		public boolean isPerfectMatch() { return iPerfectMatch; }
		
		public WeekInterface getFirstWeek() {
			return iFirstWeek;
		}
		
		public WeekInterface getLastWeek() {
			return iLastWeek;
		}
		
		public String getFirstDay() {
			return iFirstWeek == null ? "" : iFirstWeek.getDayNames().get(0);
		}
		
		public String getLastDay() {
			return iLastWeek == null ? 
					iFirstWeek == null ? "" : iFirstWeek.getDayNames().get(iFirstWeek.getDayNames().size() - 1) :
					iLastWeek.getDayNames().get(iLastWeek.getDayNames().size() - 1);
		}

		public boolean isAllWeeks() {
			return iFirstWeek == null;
		}

		public boolean isOneWeek() {
			return iFirstWeek != null && iLastWeek == null;
		}
		
		@Override
		public String getDisplayString() {
			if (getFirstWeek() == null)
				return "All Weeks";
			if (getLastWeek() == null)
				return "Week " + getFirstWeek().getDayNames().get(0) + " - " + getFirstWeek().getDayNames().get(getFirstWeek().getDayNames().size() - 1);
			return "&nbsp;&nbsp;&nbsp;" + getFirstWeek().getDayNames().get(0) + " - " + getLastWeek().getDayNames().get(getLastWeek().getDayNames().size() - 1);
		}

		@Override
		public String getReplacementString() {
			if (getFirstWeek() == null)
				return "All Weeks";
			if (getLastWeek() == null)
				return "Week " + getFirstWeek().getDayNames().get(0) + " - " + getFirstWeek().getDayNames().get(getFirstWeek().getDayNames().size() - 1);
			return "Weeks " + getFirstWeek().getDayNames().get(0) + " - " + getLastWeek().getDayNames().get(getLastWeek().getDayNames().size() - 1);
		}
		
		public WeekSelection previous() {
			if (!isOneWeek())  return null;
			for (int i = 0; i < iWeeks.size(); i++)
				if (iFirstWeek.getDayOfYear() == iWeeks.get(i).getDayOfYear())
					return new WeekSelection(i == 0 ? null : iWeeks.get(i - 1));
			return null;
		}

		public WeekSelection next() {
			if (isAllWeeks()) return new WeekSelection(iWeeks.get(0));
			if (!isOneWeek()) return null;
			for (int i = 0; i < iWeeks.size() - 1; i++)
				if (iFirstWeek.getDayOfYear() == iWeeks.get(i).getDayOfYear())
					return new WeekSelection(iWeeks.get(i + 1));
			return null;
		}
		
		public String getDayNames(int dayOfWeek) {
			if (iFirstWeek == null) return "";
			if (iLastWeek == null) return iFirstWeek.getDayNames().get(dayOfWeek);
			return iFirstWeek.getDayNames().get(dayOfWeek) + " - " + iLastWeek.getDayNames().get(dayOfWeek);
		}
		
		public int getFirstDayOfYear() {
			return (iFirstWeek == null ? 0 : iFirstWeek.getDayOfYear());
		}
		
		public int getLastDayOfYear() {
			WeekInterface last = (iLastWeek == null ? iFirstWeek : iLastWeek);
			return (last == null ? null : last.getDayOfYear() + last.getDayNames().size() - 1);
		}
	}
	
	public void addWeek(WeekInterface week) {
		iWeeks.add(week);
	}
	
	public void clearWeeks() {
		iWeeks.clear();
	}
	
	public void addWeek(int dayOfYear, String... dayNames) {
		WeekInterface week = new WeekInterface();
		week.setDayOfYear(dayOfYear);
		for (String dayName: dayNames) week.addDayName(dayName);
		addWeek(week);
	}
	
	public void select(String query) {
		iWeekSelection = parse(query);
		fireWeekChanged();
	}
	
	public WeekSelection getSelection() {
		return iWeekSelection;
	}
	
	private void fireWeekChanged() {
		iWeek.setText(iWeekSelection.getReplacementString());
		iPrevious.setEnabled(iWeekSelection.previous() != null);
		iNext.setEnabled(iWeekSelection.next() != null);
		WeekChangedEvent event = new WeekChangedEvent(iWeekSelection);
		for (WeekChangedHandler h: iWeekChangedHandlers)
			h.onWeekChanged(event);
	}
	
	public void addWeekChangedHandler(WeekChangedHandler h) {
		iWeekChangedHandlers.add(h);
	}

	public interface WeekChangedHandler {
		public void onWeekChanged(WeekChangedEvent event);
	}
	
	public static class WeekChangedEvent {
		private WeekSelection iSelection;
		public WeekChangedEvent(WeekSelection selection) { iSelection = selection; }
		public WeekSelection getSelection() { return iSelection; }
		
	}
	
	public static class MySuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay { 
	
		public MySuggestionDisplay() {
			super();
		}
		
	}
}
