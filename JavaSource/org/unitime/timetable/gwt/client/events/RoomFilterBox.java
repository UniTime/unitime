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
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;

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
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class RoomFilterBox extends UniTimeFilterBox<RoomFilterRpcRequest> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private ListBox iBuildings, iDepartments;
	private TextBox iMin, iMax;
	private Chip iLastSize = null;
	
	public RoomFilterBox(AcademicSessionProvider session) {
		super(session);
		
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
						Chip chip = new Chip("department", iDepartments.getValue(i)).withTranslatedCommand(MESSAGES.tagDepartment());
						String name = iDepartments.getItemText(i);
						if (iDepartments.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					if ("department".startsWith(text.toLowerCase()) && text.toLowerCase().length() >= 5) {
						for (int i = 0; i < iDepartments.getItemCount(); i++) {
							Chip chip = new Chip("department", iDepartments.getValue(i)).withTranslatedCommand(MESSAGES.tagDepartment());
							String name = iDepartments.getItemText(i);
							if (!chip.equals(oldChip))
								suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
			
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("managed".equalsIgnoreCase(value))
					translatedValue = MESSAGES.attrDepartmentManagedRooms();
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		iDepartments.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
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
		
		addFilter(new FilterBox.StaticSimpleFilter("type", MESSAGES.tagRoomType()));
		addFilter(new FilterBox.StaticSimpleFilter("feature", MESSAGES.tagRoomFeature()));
		addFilter(new FilterBox.StaticSimpleFilter("group", MESSAGES.tagRoomGroup()));
		addFilter(new FilterBox.StaticSimpleFilter("size", MESSAGES.tagRoomSize()));
		addFilter(new FilterBox.StaticSimpleFilter("flag", MESSAGES.tagRoomFlag()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("all".equalsIgnoreCase(text))
					translatedValue = MESSAGES.attrFlagAllRooms();
				else if ("event".equalsIgnoreCase(text))
					translatedValue = MESSAGES.attrFlagEventRooms();
				else if ("nearby".equalsIgnoreCase(text))
					translatedValue = MESSAGES.attrFlagNearbyRooms();
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		iBuildings = new ListBox();
		iBuildings.setMultipleSelect(true);
		iBuildings.setWidth("100%"); iBuildings.setVisibleItemCount(3);
		
		addFilter(new FilterBox.CustomFilter("building", MESSAGES.tagBuilding(), iBuildings) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iBuildings.getItemCount(); i++) {
						Chip chip = new Chip("building", iBuildings.getValue(i)).withTranslatedCommand(MESSAGES.tagBuilding());
						String name = iBuildings.getItemText(i);
						if (iBuildings.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						} else if (text.length() > 2 && name.toLowerCase().contains(" " + text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip));
						}
					}
					if ("building".startsWith(text.toLowerCase()) && text.toLowerCase().length() >= 5) {
						for (int i = 0; i < iBuildings.getItemCount(); i++) {
							Chip chip = new Chip("building", iBuildings.getValue(i)).withTranslatedCommand(MESSAGES.tagBuilding());
							String name = iBuildings.getItemText(i);
							suggestions.add(new Suggestion(name, chip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
			@Override
			public boolean isVisible() {
				return iBuildings.getItemCount() > 0;
			}
		});
		iBuildings.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean changed = false;
				for (int i = 0; i < iBuildings.getItemCount(); i++) {
					Chip chip = new Chip("building", iBuildings.getValue(i)).withTranslatedCommand(MESSAGES.tagBuilding());
					if (iBuildings.isItemSelected(i)) {
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
		
		Label l1 = new Label(MESSAGES.propMin());

		iMin = new TextBox();
		iMin.setStyleName("unitime-TextArea");
		iMin.setMaxLength(10); iMin.getElement().getStyle().setWidth(50, Unit.PX);
		
		Label l2 = new Label(MESSAGES.propMax());
		l2.getElement().getStyle().setMarginLeft(10, Unit.PX);

		iMax = new TextBox();
		iMax.setMaxLength(10); iMax.getElement().getStyle().setWidth(50, Unit.PX);
		iMax.setStyleName("unitime-TextArea");
		
		final CheckBox events = new CheckBox(MESSAGES.checkOnlyEventLocations());
		events.getElement().getStyle().setMarginLeft(10, Unit.PX);
		
		final CheckBox nearby = new CheckBox(MESSAGES.checkIncludeNearby());
		nearby.getElement().getStyle().setMarginLeft(10, Unit.PX);
		
		addFilter(new FilterBox.CustomFilter("other", MESSAGES.tagOther(), l1, iMin, l2, iMax, events, nearby) {
			@Override
			public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
					if (MESSAGES.attrFlagNearbyRooms().toLowerCase().startsWith(text.toLowerCase()) || "nearby".startsWith(text.toLowerCase()) || MESSAGES.checkIncludeNearby().toLowerCase().startsWith(text.toLowerCase())) {
						suggestions.add(new Suggestion(MESSAGES.checkIncludeNearby(), new Chip("flag", "Nearby").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagNearbyRooms())));
					} else if (MESSAGES.attrFlagAllRooms().toLowerCase().startsWith(text.toLowerCase()) || "all".startsWith(text.toLowerCase()) || MESSAGES.checkAllLocations().toLowerCase().startsWith(text.toLowerCase())) {
						suggestions.add(new Suggestion(MESSAGES.checkAllLocations(),
								new Chip("flag", "All").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagAllRooms()),
								new Chip("flag", "Event").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagEventRooms())
								));
					} else if (MESSAGES.attrFlagEventRooms().toLowerCase().startsWith(text.toLowerCase()) || "event".startsWith(text.toLowerCase()) || MESSAGES.checkOnlyEventLocations().toLowerCase().startsWith(text.toLowerCase())) {
						suggestions.add(new Suggestion(MESSAGES.checkOnlyEventLocations(),
								new Chip("flag", "Event").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagEventRooms()),
								new Chip("flag", "All").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagAllRooms())
								));
					} else {
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
		
		nearby.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Chip chip = new Chip("flag", "Nearby").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagNearbyRooms());
				if (event.getValue()) {
					if (!hasChip(chip)) addChip(chip, true);
				} else {
					if (hasChip(chip)) removeChip(chip, true);
				}
			}
		});
		nearby.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
			}
		});
		
		events.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Chip eventChip = new Chip("flag", "Event").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagEventRooms());
				Chip allChip = new Chip("flag", "All").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagAllRooms());
				if (event.getValue()) {
					if (!hasChip(eventChip)) addChip(eventChip, true);
					if (hasChip(allChip)) removeChip(allChip, true);
				} else {
					if (hasChip(eventChip)) removeChip(eventChip, true);
					if (!hasChip(allChip)) addChip(allChip, true);
				}
			}
		});
		events.addMouseDownHandler(new MouseDownHandler() {
			@Override
			public void onMouseDown(MouseDownEvent event) {
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
			}
		});

		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iLastSize = getChip("size");
				if (!isFilterPopupShowing()) {
					nearby.setValue(hasChip(new Chip("flag", "Nearby").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagNearbyRooms())));
					events.setValue(hasChip(new Chip("flag", "Event").withTranslatedCommand(MESSAGES.tagRoomFlag()).withTranslatedValue(MESSAGES.attrFlagEventRooms())));
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
					for (int i = 0; i < iBuildings.getItemCount(); i++) {
						String value = iBuildings.getValue(i);
						iBuildings.setItemSelected(i, hasChip(new Chip("building", value).withTranslatedCommand(MESSAGES.tagBuilding())));
					}
					iDepartments.setSelectedIndex(0);
					for (int i = 1; i < iDepartments.getItemCount(); i++) {
						String value = iDepartments.getValue(i);
						if (hasChip(new Chip("department", value).withTranslatedCommand(MESSAGES.tagDepartment()))) {
							iDepartments.setSelectedIndex(i);
							break;
						}
					}
				}
				if (getAcademicSessionId() != null)
					init(false, getAcademicSessionId(), new Command() {
						@Override
						public void execute() {
							if (isFilterPopupShowing())
								showFilterPopup();
						}
					});
				setAriaLabel(ARIA.roomFilter(toAriaString()));
			}
		});
		
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				setAriaLabel(ARIA.roomFilter(toAriaString()));
			}
		});
	}
	
	@Override
	protected void onLoad(FilterRpcResponse result) {
		if (!result.hasEntities()) return;
		boolean added = false;
		types: for (String type: result.getTypes()) {
			for (FilterBox.Filter filter: iFilter.getWidget().getFilters()) {
				if (filter.getCommand().equals(type)) continue types;
			}
			iFilter.getWidget().getFilters().add(iFilter.getWidget().getFilters().size() - 5, new FilterBox.StaticSimpleFilter(type, null));
			added = true;
		}
		if (added) setValue(getValue(), false);
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if ("building".equals(filter.getCommand())) {
			iBuildings.clear();
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iBuildings.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
			for (int i = 0; i < iBuildings.getItemCount(); i++) {
				String value = iBuildings.getValue(i);
				iBuildings.setItemSelected(i, hasChip(new Chip("building", value)));
			}
			return true;
		} else if ("department".equals(filter.getCommand())) {
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
		} else 
			return super.populateFilter(filter, entities);
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
	
	@Override
	public RoomFilterRpcRequest createRpcRequest() {
		return new RoomFilterRpcRequest();
	}
}