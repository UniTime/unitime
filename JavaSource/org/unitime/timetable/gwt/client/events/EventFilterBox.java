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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class EventFilterBox extends UniTimeFilterBox<EventFilterRpcRequest> {
	private ListBox iSponsors;
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.filterDateFormat());
	private static DateTimeFormat sLocalDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private FilterBox.CustomFilter iOther = null;
	private AriaSuggestBox iRequested;
	private Chip iLastRequested = null;
	private ListBox iServices;
	
	public EventFilterBox(AcademicSessionProvider session) {
		super(session);
		
		addFilter(new FilterBox.StaticSimpleFilter("type", MESSAGES.tagEventType()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("class".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[0];
				else if ("final exam".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[1];
				else if ("midterm exam".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[2];
				else if ("course".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[3];
				else if ("special".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[4];
				else if ("not available".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventTypeShort()[5];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		iSponsors = new ListBox();
		iSponsors.setMultipleSelect(true);
		iSponsors.setWidth("100%"); iSponsors.setVisibleItemCount(3);
		
		addFilter(new FilterBox.CustomFilter("sponsor", MESSAGES.tagSponsor(), iSponsors) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iSponsors.getItemCount(); i++) {
						Chip chip = new Chip("sponsor", iSponsors.getValue(i)).withTranslatedCommand(MESSAGES.tagSponsor());
						String name = iSponsors.getItemText(i);
						if (iSponsors.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						} else if (text.length() > 2 && name.toLowerCase().contains(" " + text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
			@Override
			public boolean isVisible() {
				return iSponsors.getItemCount() > 0;
			}
		});
		iSponsors.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean changed = false;
				for (int i = 0; i < iSponsors.getItemCount(); i++) {
					Chip chip = new Chip("sponsor", iSponsors.getValue(i)).withTranslatedCommand(MESSAGES.tagSponsor());
					if (iSponsors.isItemSelected(i)) {
						if (!hasChip(chip)) {
							addChip(chip, false); changed = true;
						}
					} else {
						if (hasChip(chip)) {
							removeChip(chip, false); changed = true;
						}
					}
				}
				if (changed)
					fireValueChangeEvent();
			}
		});
		
		FilterBox.StaticSimpleFilter mode = new FilterBox.StaticSimpleFilter("mode", MESSAGES.tagEventMode()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("all".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[0];
				else if ("my".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[1];
				else if ("approved".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[2];
				else if ("unapproved".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[3];
				else if ("awaiting".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[4];
				else if ("conflicting".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[5];
				else if ("my awaiting".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[6];
				else if ("cancelled".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[7];
				else if ("expiring".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventModeAbbv()[8];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		};
		mode.setMultipleSelection(false);
		addFilter(mode);
		
		addFilter(new FilterBox.StaticSimpleFilter("role", MESSAGES.tagEventRole()) {
			@Override
			public void getPopupWidget(final FilterBox box, final AsyncCallback<Widget> callback) {
				callback.onSuccess(null);
			}
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("all".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventRole()[0];
				else if ("student".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventRole()[1];
				else if ("instructor".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventRole()[2];
				else if ("coordinator".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventRole()[3];
				else if ("contact".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.eventRole()[4];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		iServices = new ListBox();
		iServices.setMultipleSelect(false);
		iServices.setWidth("100%");
		
		addFilter(new FilterBox.CustomFilter("service", MESSAGES.tagService(), iServices) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iServices.getItemCount(); i++) {
						Chip chip = new Chip("service", iServices.getValue(i)).withTranslatedCommand(MESSAGES.tagService());
						String name = iServices.getItemText(i);
						if (iServices.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						} else if (text.length() > 2 && name.toLowerCase().contains(" " + text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
			@Override
			public boolean isVisible() {
				return iServices.getItemCount() > 0;
			}
		});
		iServices.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean changed = false;
				for (int i = 1; i < iServices.getItemCount(); i++) {
					Chip chip = new Chip("service", iServices.getValue(i)).withTranslatedCommand(MESSAGES.tagService());
					if (iServices.isItemSelected(i)) {
						if (!hasChip(chip)) {
							addChip(chip, false); changed = true;
						}
					} else {
						if (hasChip(chip)) {
							removeChip(chip, false); changed = true;
						}
					}
				}
				if (changed)
					fireValueChangeEvent();
			}
		});
		
		Label reqLab = new Label(MESSAGES.propRequestedBy());

		iRequested = new AriaSuggestBox(new RequestedByOracle());
		iRequested.setStyleName("unitime-TextArea");
		iRequested.setWidth("200px");
		
		final CheckBox conflicts = new CheckBox(MESSAGES.checkDisplayConflicts());
		conflicts.getElement().getStyle().setMarginLeft(10, Unit.PX);
		
		final CheckBox sessions = new CheckBox(MESSAGES.checkSpanMultipleSessions());
		sessions.getElement().getStyle().setMarginLeft(10, Unit.PX);
		
		iOther = new FilterBox.CustomFilter("other", MESSAGES.tagOther(), reqLab, iRequested, conflicts, sessions) {
			@Override
			public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
					if ("conflicts".startsWith(text.toLowerCase()) || MESSAGES.checkDisplayConflicts().toLowerCase().startsWith(text.toLowerCase())) {
						suggestions.add(new Suggestion(MESSAGES.checkDisplayConflicts(), new Chip("flag", "Conflicts").withTranslatedCommand(MESSAGES.tagEventFlag()).withTranslatedValue(MESSAGES.attrFlagShowConflicts())));
					}
					if ("sessinons".startsWith(text.toLowerCase()) || MESSAGES.checkSpanMultipleSessions().toLowerCase().startsWith(text.toLowerCase())) {
						suggestions.add(new Suggestion(MESSAGES.checkSpanMultipleSessions(), new Chip("flag", "All Sessions").withTranslatedCommand(MESSAGES.tagEventFlag()).withTranslatedValue(MESSAGES.attrFlagAllSessions())));
					}
					callback.onSuccess(suggestions);
				}
			}
		};
		addFilter(iOther);
		
		addFilter(new FilterBox.StaticSimpleFilter("requested", MESSAGES.tagRequested()));
		addFilter(new FilterBox.StaticSimpleFilter("flag", MESSAGES.tagEventFlag()){
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("conflicts".equalsIgnoreCase(text))
					translatedValue = MESSAGES.attrFlagShowConflicts();
				else if ("all sessions".equalsIgnoreCase(text))
					translatedValue = MESSAGES.attrFlagAllSessions();
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});

		iRequested.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				requestedChanged(true);
			}
		});
		iRequested.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						requestedChanged(false);
					}
				});
			}
		});
		iRequested.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							requestedChanged(false);
						}
					});
			}
		});
		iRequested.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				requestedChanged(true);
			}
		});
		iRequested.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				requestedChanged(true);
			}
		});
		conflicts.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Chip chip = new Chip("flag", "Conflicts").withTranslatedCommand(MESSAGES.tagEventFlag()).withTranslatedValue(MESSAGES.attrFlagShowConflicts());
				if (event.getValue()) {
					if (!hasChip(chip)) addChip(chip, true);
				} else {
					if (hasChip(chip)) removeChip(chip, true);
				}
			}
		});
		conflicts.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
			}
		});
		
		sessions.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Chip chip = new Chip("flag", "All Sessions").withTranslatedCommand(MESSAGES.tagEventFlag()).withTranslatedValue(MESSAGES.attrFlagAllSessions());
				if (event.getValue()) {
					if (!hasChip(chip)) addChip(chip, true);
				} else {
					if (hasChip(chip)) removeChip(chip, true);
				}
			}
		});
		sessions.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
			}
		});
		
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("unitime-DateSelector");
		final SingleDateSelector.SingleMonth m1 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateFrom());
		m1.setAllowDeselect(true);
		m.add(m1);
		final SingleDateSelector.SingleMonth m2 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateTo());
		m2.setAllowDeselect(true);
		m.add(m2);
		addFilter(new FilterBox.CustomFilter("date", MESSAGES.tagDate(), m) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
				Chip chFrom = null, chTo = null;
				for (Chip c: chips) {
					if (c.getCommand().equals("from")) chFrom = c;
					if (c.getCommand().equals("to")) chTo = c;
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd/yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM.yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				callback.onSuccess(suggestions);
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("from", MESSAGES.tagDateFrom()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("to", MESSAGES.tagDateTo()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});

		session.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				if (event.isChanged() && event.getNewAcademicSessionId() != null) {
					RPC.execute(new RequestSessionDetails(event.getNewAcademicSessionId()), new AsyncCallback<GwtRpcResponseList<SessionMonth>>() {

						@Override
						public void onFailure(Throwable caught) {
						}

						@Override
						public void onSuccess(GwtRpcResponseList<SessionMonth> result) {
							m1.setMonths(result);
							m2.setMonths(result);
						}
					});
				}
			}
		});
		
		m1.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("from");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("from", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});
		m2.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("to");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("to", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});
		
		List<Chip> days = new ArrayList<Chip>();
		days.add(new Chip("day", "Monday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[0]));
		days.add(new Chip("day", "Tuesday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[1]));
		days.add(new Chip("day", "Wednesday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[2]));
		days.add(new Chip("day", "Thursday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[3]));
		days.add(new Chip("day", "Friday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[4]));
		days.add(new Chip("day", "Saturday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[5]));
		days.add(new Chip("day", "Sunday").withTranslatedCommand(MESSAGES.tagDayOfWeek()).withTranslatedValue(CONSTANTS.longDays()[6]));
		addFilter(new FilterBox.StaticSimpleFilter("day", MESSAGES.tagDayOfWeek(), days));
		
		final TimeSelector st = new TimeSelector(null);
		final TimeSelector et = new TimeSelector(st);
		st.setStyleName("unitime-TextArea"); st.addStyleName("unitime-TimeSelector");
		et.setStyleName("unitime-TextArea"); et.addStyleName("unitime-TimeSelector");
		addFilter(new FilterBox.CustomFilter("time", MESSAGES.tagTime(), new Label(MESSAGES.propAfter()), st, new Label(" " + MESSAGES.propBefore()), et) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
				Chip chStart = null, chStop = null;
				for (Chip c: chips) {
					if (c.getCommand().equals("after")) chStart = c;
					if (c.getCommand().equals("before")) chStop = c;
				}
				Integer start = TimeSelector.TimeUtils.parseTime(CONSTANTS, text, null);
				Integer stop = TimeSelector.TimeUtils.parseTime(CONSTANTS, text, chStart == null ? null : TimeSelector.TimeUtils.parseMilitary(chStart.getValue()));
				if (chStart == null) {
					if (start != null) {
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+3))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+3)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+6))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+6)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+9))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+9)), chStart));
					}
					if (stop != null) {
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+3))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+3)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+6))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+6)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+9))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+9)), chStop));
					}					
				} else {
					if (stop != null) {
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+3))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+3)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+6))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+6)), chStop));
						suggestions.add(new FilterBox.Suggestion(new Chip("before", TimeUtils.slot2military(stop+9))
								.withTranslatedCommand(MESSAGES.tagTimeBefore())
								.withTranslatedValue(TimeUtils.slot2time(stop+9)), chStop));
					}					
					if (start != null) {
						suggestions.add(new FilterBox.Suggestion(new Chip("after",TimeUtils.slot2military(start))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+3))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+3)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+6))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+6)), chStart));
						suggestions.add(new FilterBox.Suggestion(new Chip("after", TimeUtils.slot2military(start+9))
								.withTranslatedCommand(MESSAGES.tagTimeAfter())
								.withTranslatedValue(TimeUtils.slot2time(start+9)), chStart));
					}
				}
				callback.onSuccess(suggestions);
			}
		});
		st.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				Chip ch = getChip("after");
				Integer start = event.getValue();
				if (start == null) {
					if (ch != null) removeChip(ch, true);
				} else {
					if (ch != null) {
						if (ch.getCommand().equals(TimeUtils.slot2military(start))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("after", TimeUtils.slot2military(start)).withTranslatedCommand(MESSAGES.tagTimeAfter()).withTranslatedValue(TimeUtils.slot2time(start)), true);
				}
				Chip ch2 = getChip("before");
				Integer stop = et.getValue();
				if (stop == null) {
					if (ch2 != null) removeChip(ch2, true);
				} else {
					if (ch2 != null) {
						if (ch2.getCommand().equals(TimeUtils.slot2military(stop))) return;
						removeChip(ch2, false);
					}
					addChip(new Chip("before", TimeUtils.slot2military(stop)).withTranslatedCommand(MESSAGES.tagTimeBefore()).withTranslatedValue(TimeUtils.slot2time(stop)), true);
				}
			}
		});
		et.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				Chip ch = getChip("before");
				Integer stop = event.getValue();
				if (stop == null) {
					if (ch != null) removeChip(ch, true);
				} else {
					if (ch != null) {
						if (ch.getCommand().equals(TimeUtils.slot2military(stop))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("before", TimeUtils.slot2military(stop)).withTranslatedCommand(MESSAGES.tagTimeBefore()).withTranslatedValue(TimeUtils.slot2time(stop)), true);
				}
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("after", MESSAGES.tagTimeAfter()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				Integer slot = TimeUtils.parseTime2(CONSTANTS, text, null);
				if (slot != null)
					translatedValue = TimeUtils.slot2time(slot);
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("before", MESSAGES.tagTimeBefore()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				Integer slot = TimeUtils.parseTime2(CONSTANTS, text, null);
				if (slot != null)
					translatedValue = TimeUtils.slot2time(slot);
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iLastRequested = getChip("requested");
				if (!isFilterPopupShowing()) {
					conflicts.setValue(hasChip(new Chip("flag", "Conflicts")));
					sessions.setValue(hasChip(new Chip("flag", "All Sessions")));
					Chip req = getChip("requested");
					if (req == null)
						iRequested.setText("");
					else
						iRequested.setText(req.getValue());
					for (int i = 0; i < iSponsors.getItemCount(); i++) {
						String value = iSponsors.getValue(i);
						iSponsors.setItemSelected(i, hasChip(new Chip("sponsor", value)));
					}
					for (int i = 1; i < iServices.getItemCount(); i++) {
						String value = iServices.getValue(i);
						if (hasChip(new Chip("service", value))) {
							iServices.setSelectedIndex(i); break;
						}
					}
					Chip chFrom = getChip("from");
					if (chFrom != null)
						m1.setDate(sDateFormat.parse(chFrom.getValue()));
					else
						m1.clearSelection();
					Chip chTo = getChip("to");
					if (chTo != null)
						m2.setDate(sDateFormat.parse(chTo.getValue()));
					else
						m2.clearSelection();
					Chip chStart = getChip("after");
					if (chStart != null)
						st.setValue(TimeSelector.TimeUtils.parseMilitary(chStart.getValue()));
					else
						st.setValue(null);
					Chip chStop = getChip("before");
					if (chStop != null)
						et.setValue(TimeSelector.TimeUtils.parseMilitary(chStop.getValue()));
					else
						et.setValue(null);
				}
				if (getAcademicSessionId() != null)
					init(false, getAcademicSessionId(), new Command() {
						@Override
						public void execute() {
							if (isFilterPopupShowing())
								showFilterPopup();
						}
					});
				setAriaLabel(ARIA.eventFilter(toAriaString()));
			}
		});
		
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				setAriaLabel(ARIA.eventFilter(toAriaString()));
			}
		});
	}
	
	public void setOtherVisible(boolean visible) { iOther.setVisible(visible); }
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if ("sponsor".equals(filter.getCommand())) {
			iSponsors.clear();
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iSponsors.addItem(entity.getName() + (entity.getCount() <= 0 ? "" : " (" + entity.getCount() + ")"), entity.getAbbreviation());
			for (int i = 0; i < iSponsors.getItemCount(); i++) {
				String value = iSponsors.getValue(i);
				iSponsors.setItemSelected(i, hasChip(new Chip("sponsor", value)));
			}
			return true;
		} else if ("service".equals(filter.getCommand())) {
			iServices.clear();
			if (entities != null) {
				iServices.addItem(MESSAGES.itemSelect(), "");
				for (FilterRpcResponse.Entity entity: entities)
					iServices.addItem(entity.getName() + (entity.getCount() <= 0 ? "" : " (" + entity.getCount() + ")"), entity.getAbbreviation());
			}
			for (int i = 1; i < iServices.getItemCount(); i++) {
				String value = iServices.getValue(i);
				if (hasChip(new Chip("service", value))) {
					iServices.setSelectedIndex(i); break;
				}
			}
			return true;
		} else return super.populateFilter(filter, entities);
	}
	
	private void requestedChanged(boolean fireChange) {
		Chip oldChip = getChip("requested");
		if (iRequested.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("requested", iRequested.getText()).withTranslatedCommand(MESSAGES.tagRequested());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastRequested)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}

	@Override
	public EventFilterRpcRequest createRpcRequest() {
		return new EventFilterRpcRequest();
	}

	public class RequestedByOracle extends SuggestOracle {
		
		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<RequestedBySuggestion> suggestions = new ArrayList<RequestedBySuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "requested".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new RequestedBySuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
		
	}
	
	public class RequestedBySuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		RequestedBySuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getLabel();
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getLabel();
		}
	}
}
