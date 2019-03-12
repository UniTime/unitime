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
package org.unitime.timetable.gwt.client.reservations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * @author Tomas Muller
 */
public class ReservationFilterBox extends UniTimeFilterBox<ReservationInterface.ReservationFilterRpcRequest> {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static DateTimeFormat sLocalDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.filterDateFormat());
	private ListBox iDepartments, iSubjects;
	
	public ReservationFilterBox() {
		super(null);
		
		setShowSuggestionsOnFocus(false);

		FilterBox.StaticSimpleFilter mode = new FilterBox.StaticSimpleFilter("mode", MESSAGES.tagReservationMode()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("all".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.reservationModeAbbv()[0];
				else if ("expired".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.reservationModeAbbv()[1];
				else if ("not expired".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.reservationModeAbbv()[2];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		};
		mode.setMultipleSelection(false);
		addFilter(mode);
		
		addFilter(new FilterBox.StaticSimpleFilter("type", MESSAGES.tagReservationType()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("individual".equalsIgnoreCase(text))
					translatedValue = MESSAGES.reservationIndividualAbbv();
				else if ("group".equalsIgnoreCase(text))
					translatedValue = MESSAGES.reservationStudentGroupAbbv();
				else if ("curriculum".equalsIgnoreCase(text))
					translatedValue = MESSAGES.reservationCurriculumAbbv();
				else if ("course".equalsIgnoreCase(text))
					translatedValue = MESSAGES.reservationCourseAbbv();
				else if ("override".equalsIgnoreCase(text))
					translatedValue = MESSAGES.reservationOverrideAbbv();
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		FilterBox.StaticSimpleFilter override = new FilterBox.StaticSimpleFilter("override", MESSAGES.tagReservationOverride());
		override.setMultipleSelection(true);
		addFilter(override);

		iDepartments = new ListBox();
		iDepartments.setMultipleSelect(false);
		iDepartments.setWidth("100%");
		
		addFilter(new FilterBox.CustomFilter("department", MESSAGES.tagDepartment(), iDepartments) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip oldChip = getChip("department");
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iDepartments.getItemCount(); i++) {
						if (iDepartments.getItemText(i).endsWith(" (0)")) continue;
						Chip chip = new Chip("department", iDepartments.getValue(i)).withTranslatedCommand(MESSAGES.tagDepartment());
						String name = iDepartments.getItemText(i);
						if (iDepartments.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iDepartments.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				Chip subjectChip = getChip("subject");
				while (subjectChip != null && iDepartments.getSelectedIndex() > 1) {
					removeChip(subjectChip, false);
					subjectChip = getChip("subject");
				}

				Chip oldChip = getChip("department");
				Chip newChip = (iDepartments.getSelectedIndex() <= 0 ? null : new Chip("department", iDepartments.getValue(iDepartments.getSelectedIndex())).withTranslatedCommand(MESSAGES.tagDepartment()));
				if (oldChip != null) {
					if (newChip == null) {
						removeChip(oldChip, true);
					} else {
						if (!oldChip.getValue().equals(newChip.getValue())) {
							removeChip(oldChip, false);
							addChip(newChip, true);
						}
					}
				} else {
					if (newChip != null)
						addChip(newChip, true);
				}
					
			}
		});
		
		iSubjects = new ListBox();
		iSubjects.setMultipleSelect(true);
		iSubjects.setWidth("100%"); iSubjects.setVisibleItemCount(3);
		
		addFilter(new FilterBox.CustomFilter("subject", MESSAGES.tagSubjectArea(), iSubjects) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip deptChip = getChip("department");
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iSubjects.getItemCount(); i++) {
						if (iSubjects.getItemText(i).endsWith(" (0)")) continue;
						Chip chip = new Chip("subject", iSubjects.getValue(i)).withTranslatedCommand(MESSAGES.tagSubjectArea());
						String name = iSubjects.getItemText(i);
						if (iSubjects.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, deptChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, deptChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iSubjects.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean changed = false;
				Chip deptChip = getChip("department");
				for (int i = 0; i < iSubjects.getItemCount(); i++) {
					Chip chip = new Chip("subject", iSubjects.getValue(i)).withTranslatedCommand(MESSAGES.tagSubjectArea());
					if (iSubjects.isItemSelected(i)) {
						if (!hasChip(chip)) {
							addChip(chip, false); changed = true;
						}
						if (deptChip != null && iSubjects.getSelectedIndex() > 1) {
							removeChip(deptChip, false);
							deptChip = null;
							changed = true;
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
		
		addFilter(new FilterBox.StaticSimpleFilter("area", MESSAGES.tagAcademicArea()));
		addFilter(new FilterBox.StaticSimpleFilter("group", MESSAGES.tagStudentGroup()));
		
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("unitime-DateSelector");
		final SingleDateSelector.SingleMonth m1 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateBefore());
		m1.setAllowDeselect(true);
		final SingleDateSelector.SingleMonth m2 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateAfter());
		m2.setAllowDeselect(true);
		m.add(m2);
		m.add(m1);
		addFilter(new FilterBox.CustomFilter("date", MESSAGES.tagDate(), m) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
				Chip chFrom = null, chTo = null;
				for (Chip c: chips) {
					if (c.getCommand().equals("before")) chFrom = c;
					if (c.getCommand().equals("after")) chTo = c;
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd/yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM.yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("before", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("after", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				callback.onSuccess(suggestions);
			}			
		});
		addFilter(new FilterBox.StaticSimpleFilter("before", MESSAGES.tagDateBefore()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("after", MESSAGES.tagDateAfter()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		m1.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("before");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("before", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateBefore()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});
		m2.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("after");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("after", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateAfter()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!isFilterPopupShowing()) {
					iDepartments.setSelectedIndex(0);
					for (int i = 1; i < iDepartments.getItemCount(); i++) {
						String value = iDepartments.getValue(i);
						if (hasChip(new Chip("department", value))) {
							iDepartments.setSelectedIndex(i);
							break;
						}
					}
					for (int i = 0; i < iSubjects.getItemCount(); i++) {
						String value = iSubjects.getValue(i);
						iSubjects.setItemSelected(i, hasChip(new Chip("subject", value)));
					}
					Chip chFrom = getChip("before");
					if (chFrom != null)
						m1.setDate(sDateFormat.parse(chFrom.getValue()));
					else
						m1.clearSelection();
					Chip chTo = getChip("after");
					if (chTo != null)
						m2.setDate(sDateFormat.parse(chTo.getValue()));
					else
						m2.clearSelection();
				}
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("student", MESSAGES.tagStudent()));
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if ("department".equals(filter.getCommand())) {
			iDepartments.clear();
			iDepartments.addItem(MESSAGES.itemAllDepartments(), "");
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iDepartments.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
			iDepartments.setSelectedIndex(0);
			Chip dept = getChip("department");
			if (dept != null)
				for (int i = 1; i < iDepartments.getItemCount(); i++)
					if (dept.getValue().equals(iDepartments.getValue(i))) {
						iDepartments.setSelectedIndex(i);
						break;
					}
			return true;
		} else if ("subject".equals(filter.getCommand())) {
			iSubjects.clear();
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iSubjects.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
			for (int i = 0; i < iSubjects.getItemCount(); i++) {
				String value = iSubjects.getValue(i);
				iSubjects.setItemSelected(i, hasChip(new Chip("subject", value)));
			}
			return true;
		} else if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation())
							.withLabel(entity.getName())
							.withCount(entity.getCount())
							.withTranslatedCommand(filter.getLabel())
							.withTranslatedValue(entity.getProperty("translated-value", null)));
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public ReservationInterface.ReservationFilterRpcRequest createRpcRequest() {
		return new ReservationInterface.ReservationFilterRpcRequest();
	}
}