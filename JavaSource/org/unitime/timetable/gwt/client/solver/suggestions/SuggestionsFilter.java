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
package org.unitime.timetable.gwt.client.solver.suggestions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.solver.SolverCookie;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.client.widgets.TimeSelector.TimeUtils;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SuggestionsInterface.SuggestionsFilterRpcRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class SuggestionsFilter extends UniTimeFilterBox<SuggestionsFilterRpcRequest> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private Long iClassId = null;
	private TextBox iMin, iMax;
	private Chip iLastSize = null;
	
	public SuggestionsFilter() {
		super(null);
		
		setShowSuggestionsOnFocus(false);
		
		setDefaultValueProvider(new TakesValue<String>() {
			@Override
			public void setValue(final String value) {
				SolverCookie.getInstance().setSuggestionsFilter(value == null ? "" : value);
			}

			@Override
			public String getValue() {
				return SolverCookie.getInstance().getSuggestionsFilter();
			}
		});
		
		List<Chip> modes = new ArrayList<Chip>();
		modes.add(new Chip("mode", "Suggestions").withTranslatedCommand(MESSAGES.tagMode()).withTranslatedValue(MESSAGES.suggestionsSuggestions()));
		modes.add(new Chip("mode", "Placements").withTranslatedCommand(MESSAGES.tagMode()).withTranslatedValue(MESSAGES.suggestionsPlacements()));
		FilterBox.StaticSimpleFilter modeFilter = new FilterBox.StaticSimpleFilter("mode", MESSAGES.tagMode(), modes);
		modeFilter.setMultipleSelection(false);
		addFilter(modeFilter);

		List<Chip> flags = new ArrayList<Chip>();
		flags.add(new Chip("flag", "Same Room").withTranslatedCommand(MESSAGES.tagFlag()).withTranslatedValue(MESSAGES.suggestionsSameRoom()));
		flags.add(new Chip("flag", "Same Time").withTranslatedCommand(MESSAGES.tagFlag()).withTranslatedValue(MESSAGES.suggestionsSameTime()));
		flags.add(new Chip("flag", "Allow Break Hard").withTranslatedCommand(MESSAGES.tagFlag()).withTranslatedValue(MESSAGES.suggestionsAllowBreakHard()));
		addFilter(new FilterBox.StaticSimpleFilter("flag", MESSAGES.tagFlag(), flags) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				try {
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					if (text.startsWith("D") || text.startsWith("d")) {
						Integer value = Integer.valueOf(text.substring(1));
						suggestions.add(new Suggestion(new Chip("depth", "D" + value).withTranslatedCommand(MESSAGES.tagSuggestionsDepth()).withLabel(value.toString()), getChip("depth")));
					} else if (text.startsWith("R") || text.startsWith("r")) {
						Integer value = Integer.valueOf(text.substring(1));
						suggestions.add(new Suggestion(new Chip("results", "R" + value).withTranslatedCommand(MESSAGES.tagSuggestionsResults()).withLabel(value.toString()), getChip("results")));
					} else if (text.startsWith("T") || text.startsWith("t")) {
						Integer value = Integer.valueOf(text.substring(1));
						suggestions.add(new Suggestion(new Chip("timeout", "T" + value).withTranslatedCommand(MESSAGES.tagSuggestionsTimeLimit()).withLabel(value.toString()), getChip("timeout")));
					} else { 
						Integer value = Integer.valueOf(text);
						if (value > 0) {
							if (value < 10)
								suggestions.add(new Suggestion(new Chip("depth", "D" + value).withTranslatedCommand(MESSAGES.tagSuggestionsDepth()).withLabel(value.toString()), getChip("depth")));
							suggestions.add(new Suggestion(new Chip("timeout", "T" + value).withTranslatedCommand(MESSAGES.tagSuggestionsTimeLimit()).withLabel(value.toString()), getChip("timeout")));
							suggestions.add(new Suggestion(new Chip("results", "R" + value).withTranslatedCommand(MESSAGES.tagSuggestionsResults()).withLabel(value.toString()), getChip("results")));
						}
					}
					callback.onSuccess(suggestions);
				} catch (Exception e) {
					super.getSuggestions(chips, text, callback);
				}
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("date", MESSAGES.tagDate()));
		addFilter(new FilterBox.StaticSimpleFilter("time", MESSAGES.tagTime()));
		addFilter(new FilterBox.StaticSimpleFilter("room", MESSAGES.tagRoom()));
		addFilter(new FilterBox.StaticSimpleFilter("depth", MESSAGES.tagSuggestionsDepth()));
		addFilter(new FilterBox.StaticSimpleFilter("timeout", MESSAGES.tagSuggestionsTimeLimit()));
		addFilter(new FilterBox.StaticSimpleFilter("results", MESSAGES.tagSuggestionsResults()));
		
		
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
		
		Label l1 = new Label(MESSAGES.propMin());

		iMin = new TextBox();
		iMin.setStyleName("unitime-TextArea");
		iMin.setMaxLength(10); iMin.getElement().getStyle().setWidth(50, Unit.PX);
		
		Label l2 = new Label(MESSAGES.propMax());
		l2.getElement().getStyle().setMarginLeft(10, Unit.PX);

		iMax = new TextBox();
		iMax.setMaxLength(10); iMax.getElement().getStyle().setWidth(50, Unit.PX);
		iMax.setStyleName("unitime-TextArea");

		addFilter(new FilterBox.CustomFilter("size", MESSAGES.tagRoomSize(), l1, iMin, l2, iMax) {
			@Override
			public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
					Chip old = null;
					for (Chip c: chips) { if (c.getCommand().equals("size")) { old = c; break; } }
					try {
						String number = text;
						String prefix = "";
						if (text.startsWith("<=") || text.startsWith(">=")) { number = number.substring(2); prefix = text.substring(0, 2); }
						else if (text.startsWith("<") || text.startsWith(">")) { number = number.substring(1); prefix = text.substring(0, 1); }
						Integer.parseInt(number);
						suggestions.add(new Suggestion(new Chip("size", text).withTranslatedCommand(MESSAGES.tagRoomSize()), old));
						if (prefix.isEmpty()) {
							suggestions.add(new Suggestion(new Chip("size", "<=" + text).withTranslatedCommand(MESSAGES.tagRoomSize()), old));
							suggestions.add(new Suggestion(new Chip("size", ">=" + text).withTranslatedCommand(MESSAGES.tagRoomSize()), old));
						}
					} catch (Exception e) {}
					if (text.contains("..")) {
						try {
							String first = text.substring(0, text.indexOf('.'));
							String second = text.substring(text.indexOf("..") + 2);
							Integer.parseInt(first); Integer.parseInt(second);
							suggestions.add(new Suggestion(new Chip("size", text).withTranslatedCommand(MESSAGES.tagRoomSize()), old));
						} catch (Exception e) {}
					}
					callback.onSuccess(suggestions);
				}
			}

		}); 

		iMin.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				sizeChanged(true);
			}
		});
		iMax.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				sizeChanged(true);
			}
		});
		
		iMin.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						sizeChanged(false);
					}
				});
			}
		});
		iMax.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						sizeChanged(false);
					}
				});
			}
		});
		
		iMin.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							sizeChanged(false);
						}
					});
			}
		});
		iMax.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							sizeChanged(false);
						}
					});
			}
		});
		iMin.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				sizeChanged(true);
			}
		});
		iMax.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				sizeChanged(true);
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iLastSize = getChip("size");
				if (!isFilterPopupShowing()) {
					Chip size = getChip("size");
					if (size != null) {
						if (size.getValue().startsWith("<=")) {
							iMin.setText(""); iMax.setText(size.getValue().substring(2));
						} else if (size.getValue().startsWith("<")) {
							try {
								iMax.setText(String.valueOf(Integer.parseInt(size.getValue().substring(1)) - 1)); iMin.setText("");							
							} catch (Exception e) {}
						} else if (size.getValue().startsWith(">=")) {
							iMin.setText(size.getValue().substring(2)); iMax.setText("");
						} else if (size.getValue().startsWith(">")) {
							try {
								iMin.setText(String.valueOf(Integer.parseInt(size.getValue().substring(1)) + 1)); iMax.setText("");							
							} catch (Exception e) {}
						} else if (size.getValue().contains("..")) {
							iMin.setText(size.getValue().substring(0, size.getValue().indexOf(".."))); iMax.setText(size.getValue().substring(size.getValue().indexOf("..") + 2));
						} else {
							iMin.setText(size.getValue()); iMax.setText(size.getValue());
						}
					} else {
						iMin.setText(""); iMax.setText("");
					}
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
			}
		});
	}
	
	private void sizeChanged(boolean fireChange) {
		Chip oldChip = getChip("size");
		if (iMin.getText().isEmpty() && iMax.getText().isEmpty()) {
			if (oldChip != null) {
				removeChip(oldChip, fireChange);
			}
		} else {
			Chip newChip = new Chip("size", iMin.getText().isEmpty() ? "<=" + iMax.getText() : iMax.getText().isEmpty() ? ">=" + iMin.getText() : iMin.getText() + ".." + iMax.getText()).withTranslatedCommand(MESSAGES.tagRoomSize());
			if (newChip.equals(oldChip)) {
				if (fireChange && !newChip.equals(iLastSize)) fireValueChangeEvent();
				return;
			} else {
				if (oldChip != null)
					removeChip(oldChip, false);
				addChip(newChip, fireChange);
			}
		}
	}
	
	public void setClassId(Long classId) {
		if (iClassId == null || !iClassId.equals(classId)) {
			iClassId = classId;
			init(false, getAcademicSessionId(), new Command() {
				@Override
				public void execute() {
					if (isFilterPopupShowing())
						showFilterPopup();
				}
			});
		}
	}
	
	@Override
	protected SuggestionsFilterRpcRequest createRpcRequest() {
		SuggestionsFilterRpcRequest request = new SuggestionsFilterRpcRequest();
		request.setClassId(iClassId);
		return request;
	}

}
