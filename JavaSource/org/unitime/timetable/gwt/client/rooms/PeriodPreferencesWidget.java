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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PeriodPreferenceModel;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

/**
 * @author Tomas Muller
 */
public class PeriodPreferencesWidget extends Composite implements HasValue<PeriodPreferenceModel> {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private AbsolutePanel iPanel;
	private PreferenceInterface iPreference = null;
	private P iSelectedIcon = null, iSelectedTitle = null;
	private PeriodPreferenceModel iModel;
	protected boolean iEditable = true;
	private CheckBox iHorizontal;
	
	public PeriodPreferencesWidget(boolean editable) {
		iEditable = editable;

		iPanel = new AbsolutePanel();
		
		iHorizontal = new CheckBox(MESSAGES.periodPreferenceHorizontal());
		iHorizontal.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				RoomCookie.getInstance().setHorizontal(iHorizontal.getValue());
				render();
			}
		});
		
		initWidget(iPanel);
	}
	
	public AbsolutePanel getPanel() { return iPanel; }
	
	public boolean isEditable() {
		return iEditable && iModel != null;
	}
	
	public void setEditable(boolean editable) {
		iEditable = editable;
	}
	
	public boolean isEditable(PreferenceInterface preference) {
		return isEditable() && preference.isEditable();
	}
	
	public boolean isEditable(int day, int slot) {
		return isEditable() && iModel.getPeriod(day, slot) != null;
	}
	
	public PeriodPreferenceModel getModel() {
		return iModel;
	}
	
	public boolean isHorizontal() {
		return iHorizontal.getValue();
	}
	
	public void setHorizontal(boolean horizontal) {
		iHorizontal.setValue(horizontal);
		RoomCookie.getInstance().setHorizontal(horizontal);
		render();
	}
	
	public void setModel(PeriodPreferenceModel model) {
		iModel = model;
		if (RoomCookie.getInstance().hasOrientation())
			iHorizontal.setValue(RoomCookie.getInstance().areRoomsHorizontal());
		else
			iHorizontal.setValue(model.isDefaultHorizontal());
		
		iPreference = iModel.getDefaultPreference();
		if (iPreference == null) iPreference = iModel.getPreferences().get(0);
		render();
	}
	
	public void insert(final RootPanel panel, Long eventAvailability) {
		String[] ids = panel.getElement().getInnerHTML().trim().split(":");
		Long locationId = Long.valueOf(ids[0]);
		Long examTypeId = Long.valueOf(ids[1]);
		RPC.execute(RoomInterface.PeriodPreferenceRequest.load(locationId, examTypeId), new AsyncCallback<PeriodPreferenceModel>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedToLoadPeriodPreferences(caught.getMessage()));
			}

			@Override
			public void onSuccess(PeriodPreferenceModel result) {
				panel.getElement().setInnerText(null);
				setModel(result);
				panel.add(PeriodPreferencesWidget.this);
				panel.setVisible(true);
			}
		});
	}
	
	public static String slot2short(int slot) {
		int h = slot / 12;
        int m = 5 * (slot % 12);
        if (CONSTANTS.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
        else
			return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	private class Cell extends P {
		private int iDay, iSlot;
		private Date iDate;
		private PeriodInterface iPeriod;
		
		Cell(int day, int slot, Date date, PeriodInterface period) {
			super("cell", "item", isEditable() && period != null ? "clickable" : null, period == null ? "disabled" : null);
			iDay = day;
			iSlot = slot;
			iDate = date;
			iPeriod = period;
			if (period != null) {
				PreferenceInterface preference = iModel.getPreference(day, slot);
				if (preference == null) {
					getElement().getStyle().clearBackgroundColor();
					setHTML("");
					setTitle("");
				} else {
					getElement().getStyle().setBackgroundColor(preference.getColor());
					setTitle(DateTimeFormat.getFormat(CONSTANTS.examPeriodDateFormat()).format(date) + " " + slot2short(slot) + (period == null ? "" : " - " + slot2short(slot + period.getLength())) + ": " + preference.getName());
				}
				if (isEditable())
					addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							setOption(iPreference);
						}
					});				
			}
		}
		
		public void setOption(PreferenceInterface preference) {
			if (!isEditable() || iPeriod == null) return;
			iModel.setPreference(iDay, iSlot, preference);
			if (preference == null) {
				getElement().getStyle().clearBackgroundColor();
				setHTML("");
				setTitle("");
			} else {
				getElement().getStyle().setBackgroundColor(preference.getColor());
				setTitle(DateTimeFormat.getFormat(CONSTANTS.examPeriodDateFormat()).format(iDate) + " " + slot2short(iSlot) + " - " + slot2short(iSlot + iPeriod.getLength()) + ": " + preference.getName());
			}
			ValueChangeEvent.fire(PeriodPreferencesWidget.this, getValue());
		}
	}
	
	private class D extends P {
		private int iDay, iSlot;
		private Date iDate;
		private PeriodInterface iPeriod;
		
		private D(int day, int slot, Date date, PeriodInterface period) {
			super("cell", "day", isEditable() && period != null ? "clickable" : null, period == null ? "unavailable" : null);
			iDay = day;
			iSlot = slot;
			iDate = date;
			iPeriod = period;
			setText(DateTimeFormat.getFormat("d").format(date));
			if (period != null) {
				PreferenceInterface preference = iModel.getPreference(day, slot);
				if (preference == null) {
					getElement().getStyle().clearBackgroundColor();
					setTitle("");
				} else {
					getElement().getStyle().setBackgroundColor(preference.getColor());
					setTitle(DateTimeFormat.getFormat(CONSTANTS.examPeriodDateFormat()).format(date) + " " + slot2short(slot) + (period == null ? "" : " - " + slot2short(slot + period.getLength())) + ": " + preference.getName());
				}
				if (isEditable())
					addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							setOption(iPreference);
						}
					});				
			}
		}
		
		public void setOption(PreferenceInterface preference) {
			if (!isEditable() || iPeriod == null) return;
			iModel.setPreference(iDay, iSlot, preference);
			if (preference == null) {
				getElement().getStyle().clearBackgroundColor();
				setTitle("");
			} else {
				getElement().getStyle().setBackgroundColor(preference.getColor());
				setTitle(DateTimeFormat.getFormat(CONSTANTS.examPeriodDateFormat()).format(iDate) + " " + slot2short(iSlot) + " - " + slot2short(iSlot + iPeriod.getLength()) + ": " + preference.getName());
			}
			ValueChangeEvent.fire(PeriodPreferencesWidget.this, getValue());
		}
	}
	
	@SuppressWarnings("deprecation")
	static int firstDayOfWeek(int year, int month) {
		return (6 + new Date(year - 1900, month - 1, 1).getDay()) % 7;
	}
	
	@SuppressWarnings("deprecation")
	static int daysInMonth(int year, int month) {
		return new Date(year + (month == 12 ? 1 : 0) - 1900, (month == 12 ? 1 : month + 1) - 1, 0).getDate();
	}
	
	@SuppressWarnings("deprecation")
	static int weekNumber(int year, int month) {
		Date d = new Date(year - 1900, month - 1, 1);
		while (d.getDay() != CalendarUtil.getStartingDayOfWeek()) d.setDate(d.getDate() - 1);
		int y = d.getYear();
		int week = 0;
		while (d.getYear() == y) { d.setDate(d.getDate() - 7); week += 1; }
		return week;
	}
	
	static int startingDayOfWeek() {
		return (6 + CalendarUtil.getStartingDayOfWeek()) % 7;
	}
	
	private class SingleMonth extends AbsolutePanel {
		@SuppressWarnings("deprecation")
		SingleMonth(String title, Date date, int slot) {
			date.setDate(1);
			int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(date));
			int month = Integer.parseInt(DateTimeFormat.getFormat("MM").format(date));
			int firstDayOfWeek = (6 + date.getDay()) % 7;
			int nrDays = daysInMonth(year, month);
			int firstWeekNumber = weekNumber(year, month);
			
			addStyleName("month");
						
			if (title != null) {
				P p = new P("title"); p.setText(title);
				add(p);
			}
			
			P box = new P("box");
			add(box);

			P header = new P("row");
			box.add(header);
			P corner = new P("cell", "corner");
			header.add(corner);
			
			for (int i = 0; i < 7; i++) {
				P h = new P("cell", "dow");
				h.setText(CONSTANTS.days()[(i + startingDayOfWeek()) % 7]);
				header.add(h);
			}

			int weekNumber = firstWeekNumber;
			P line = new P("row");
			box.add(line);
			P week = new P("cell", "week");
			week.setText(String.valueOf(weekNumber ++));
			line.add(week);
			
			int idx = 0;
			int blanks = (firstDayOfWeek + 7 - startingDayOfWeek()) % 7;
			for (int i = 0; i < blanks; i++) {
				line.add(new P("cell", (i + 1 == blanks ? "last-blank": "blank")));
				idx++;
			}
			
			for (int i = 1; i <= nrDays; i++) {
				if (i > 1 && idx % 7 == 0) {
					if (idx == 7 && month == 1 && weekNumber > 50) weekNumber = 1;
					line = new P("row");
					box.add(line);
					week = new P("cell", "week");
					week.setText(String.valueOf(weekNumber ++));
					line.add(week);
				}
				int day = CalendarUtil.getDaysBetween(iModel.getFirstDate(), date);
				PeriodInterface period = iModel.getPeriod(day, slot);
				D d = new D(day, slot, date, period);
				line.add(d);
				idx++;
				date = CalendarUtil.copyDate(date);
				CalendarUtil.addDaysToDate(date, 1);
			}
		}
	}
	
	static Date getDate(Date firstDate, int offset) {
		Date d = new Date(firstDate.getTime());
		if (offset != 0) CalendarUtil.addDaysToDate(d, offset);
		return d;
	}

	protected void render() {
		iPanel.clear();
	
		if (iModel.getExamType().isFinal() || !CONSTANTS.displayMidtermPeriodPreferencesAsCalendar()) {
			iPanel.setStyleName("unitime-RoomSharingWidget");
			DateTimeFormat f1 = DateTimeFormat.getFormat(CONSTANTS.examPeriodPreferenceDateFormat()[0]);
			DateTimeFormat f2 = DateTimeFormat.getFormat(CONSTANTS.examPeriodPreferenceDateFormat()[1]);
			DateTimeFormat fm = DateTimeFormat.getFormat("MMMM yyyy");
			P horizonal = new P("horizontal"); horizonal.add(iHorizontal);
			iPanel.add(horizonal);
			
			boolean splitByMonths = iModel.getDays().size() > 15;
			P container = null;
			if (splitByMonths) {
				container = new P("container");
				iPanel.add(container);
			}
			Set<String> months = new HashSet<String>();
			while (true) {
				TreeSet<Integer> days = iModel.getDays();
				String month = null;
				if (splitByMonths) {
					days = new TreeSet<Integer>();
					for (int day: iModel.getDays()) {
						Date date = getDate(iModel.getFirstDate(), day);
						String m = fm.format(date);
						if (m.equals(month)) {
							days.add(day);
						} else if (month == null && months.add(m)) {
							month = m;
							days.add(day);
						}
					}
					if (month == null) break;
				}
				
				P table = new P(splitByMonths ? "intable" : "table");
				if (splitByMonths) {
					container.add(table);
					P title = new P("title");
					title.setText(month);
					table.add(title);
				} else {
					iPanel.add(table);
				}
				P box = new P("box");
				table.add(box);
				P header = new P("row");
				box.add(header);
				P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
				corner.setHTML(MESSAGES.roomSharingCorner());
				header.add(corner);
				
				final List<Cell> thisPage = new ArrayList<Cell>();
				if (isHorizontal()) {
					if (container != null)
						container.addStyleName("orientation-horizontal");
					
					final Map<Integer, List<Cell>> thisSlot = new HashMap<Integer, List<Cell>>();
					for (int slot: iModel.getSlots()) {
						P p = new P("cell", "time", isEditable() ? "clickable" : null);
						p.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iModel.getLength(slot))));
						final List<Cell> t = new ArrayList<Cell>();
						thisSlot.put(slot, t);
						header.add(p);
						if (isEditable())
							p.addMouseDownHandler(new MouseDownHandler() {
								@Override
								public void onMouseDown(MouseDownEvent event) {
									for (Cell d: t)
										d.setOption(iPreference);
								}
							});
					}
					for (int day: days) {
						P line = new P("row");
						box.add(line);
						P d = new P("cell", "day", isEditable() ? "clickable" : null);
						Date date = getDate(iModel.getFirstDate(), day);
						d.setHTML(f1.format(date) + "<br>" + f2.format(date));
						line.add(d);
						final List<Cell> thisDay = new ArrayList<Cell>();
						for (int slot: iModel.getSlots()) {
							Cell p = new Cell(day, slot, date, iModel.getPeriod(day, slot));
							line.add(p);
							thisSlot.get(slot).add(p);
							thisPage.add(p);
							thisDay.add(p);
						}
						if (isEditable())
							d.addMouseDownHandler(new MouseDownHandler() {
								@Override
								public void onMouseDown(MouseDownEvent event) {
									for (Cell d: thisDay)
										d.setOption(iPreference);
								}
							});
					}
				} else {
					if (container != null)
						container.addStyleName("orientation-vertical");
					
					final Map<Integer, List<Cell>> thisDay = new HashMap<Integer, List<Cell>>();
					for (int day: days) {
						P p = new P("cell", "time", isEditable() ? "clickable" : null);
						Date date = getDate(iModel.getFirstDate(), day);
						p.setHTML(f1.format(date) + "<br>" + f2.format(date));
						final List<Cell> t = new ArrayList<Cell>();
						thisDay.put(day, t);
						header.add(p);
						if (isEditable())
							p.addMouseDownHandler(new MouseDownHandler() {
								@Override
								public void onMouseDown(MouseDownEvent event) {
									for (Cell d: t)
										d.setOption(iPreference);
								}
							});
					}
					
					for (int slot: iModel.getSlots()) {
						P line = new P("row");
						box.add(line);
						P d = new P("cell", "day", isEditable() ? "clickable" : null);
						d.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iModel.getLength(slot))));
						line.add(d);
						final List<Cell> thisSlot = new ArrayList<Cell>();
						for (int day: days) {
							Date date = getDate(iModel.getFirstDate(), day);
							Cell p = new Cell(day, slot, date, iModel.getPeriod(day, slot));
							line.add(p);
							thisSlot.add(p);
							thisPage.add(p);
							thisDay.get(day).add(p);
						}
						if (isEditable())
							d.addMouseDownHandler(new MouseDownHandler() {
								@Override
								public void onMouseDown(MouseDownEvent event) {
									for (Cell d: thisSlot)
										d.setOption(iPreference);
								}
							});
					}
				}

				
				if (isEditable())
					corner.addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							for (Cell d: thisPage)
								d.setOption(iPreference);
						}
					});	
				
				if (!splitByMonths) break;
			}
		} else {
			iPanel.setStyleName("unitime-DateSelector");
			DateTimeFormat format = DateTimeFormat.getFormat("MMMM yyyy");
			P table = new P(); table.getElement().getStyle().setProperty("display", "inline-table");
			iPanel.add(table);
			for (int slot: iModel.getSlots()) {
				String last = null;
				P row = new P(); row.getElement().getStyle().setProperty("display", "table-row");
				table.add(row);
				SingleMonth m = null;
				for (int day: iModel.getDays()) {
					Date d = getDate(iModel.getFirstDate(), day);
					if (format.format(d).equals(last)) continue;
					last = format.format(d);
					m = new SingleMonth(format.format(d) + " (" + slot2short(slot) + " - " + slot2short(slot + iModel.getLength(slot)) + ")", d, slot);
					m.getElement().getStyle().setProperty("display", "table-cell");
					row.add(m);
				}
			}
		}

		P legend = new P("legend");
		iPanel.add(legend);
		
		P box = new P("box");
		legend.add(box);
		
		iSelectedIcon = null; iSelectedTitle = null;
		for (final PreferenceInterface option: iModel.getPreferences()) {
			final P line = new P("row");
			
			final P icon = new P("cell", isEditable(option) ? "clickable" : null);
			if (box.getWidgetCount() == 0) icon.addStyleName("first");
			icon.getElement().getStyle().setBackgroundColor(option.getColor());
			line.add(icon);
			
			final P title = new P("title", isEditable(option) ? "editable-title" : null); title.setHTML(option.getName());
			line.add(title);
			
			if (isEditable(option) && option.equals(iPreference)) {
				icon.addStyleName("selected");
				title.addStyleName("selected-title");
				iSelectedIcon = icon;
				iSelectedTitle = title;
			}

			if (isEditable(option)) {
				MouseDownHandler md = new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						iPreference = option;
						if (iSelectedIcon != null)
							iSelectedIcon.removeStyleName("selected");
						if (iSelectedTitle != null)
							iSelectedTitle.removeStyleName("selected-title");
						icon.addStyleName("selected");
						title.addStyleName("selected-title");
						iSelectedIcon = icon;
						iSelectedTitle = title;
					}
				};
				
				icon.addMouseDownHandler(md);
				title.addMouseDownHandler(md);
			}
			
			box.add(line);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PeriodPreferenceModel> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public PeriodPreferenceModel getValue() {
		return getModel();
	}

	@Override
	public void setValue(PeriodPreferenceModel value) {
		setValue(value, false);
	}

	@Override
	public void setValue(PeriodPreferenceModel value, boolean fireEvents) {
		setModel(value);
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
}
