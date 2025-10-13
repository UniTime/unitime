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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefGroupEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefLevel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PreferenceType;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Preferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.Selection;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePreferences;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimeSelection;
import org.unitime.timetable.gwt.client.rooms.PeriodPreferencesWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;

public class PreferenceEditWidget extends SimpleForm implements TakesValue<PrefGroupEditResponse>{
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private UniTimeHeaderPanel iHeader;
	
	private PrefGroupEditResponse iResponse;
	private List<PreferencesTable> iRoomPrefs;
	private PreferencesTable iDatePrefs;
	private PreferencesTable iDistributionPrefs;
	private PreferencesTable iCoursePrefs;
	private TimePreferenceTable iTimePrefs;
	private InstructorAvailabilityWidget iInstructorAvailability;
	private SessionDatesSelector iInstructorUnavailability;
	private PeriodPreferencesWidget iPeriodPrefs;
	
	public PreferenceEditWidget() {
		this(true);
	}

	public PreferenceEditWidget(boolean header) {
		addStyleName("unitime-PreferenceEdit");
		removeStyleName("unitime-NotPrintableBottomLine");
		if (header) {
			iHeader = new UniTimeHeaderPanel(COURSE.sectionTitlePreferences());
			addHeaderRow(iHeader);
		}
	}

	@Override
	public PrefGroupEditResponse getValue() {
		update();
		return iResponse;
	}
	
	public void update() {
		if (iTimePrefs != null)
			iTimePrefs.update();
		if (iDatePrefs != null)
			iDatePrefs.update();
		if (iRoomPrefs != null)
			for (PreferencesTable rp: iRoomPrefs)
				rp.update();
		if (iCoursePrefs != null)
			iCoursePrefs.update();
		if (iDistributionPrefs != null)
			iDistributionPrefs.update();
		if (iInstructorAvailability != null)
			iResponse.setInstructorTimePrefereneces(((InstructorAvailabilityModel)iInstructorAvailability.getModel()).getPattern());
		if (iInstructorUnavailability != null)
			iResponse.setInstructorUnavailability(iInstructorUnavailability.getPattern());
	}
	
	public static String getName(PreferenceType type) {
		switch (type) {
		case TIME: return COURSE.propertyTime();
		case DATE: return COURSE.propertyDatePatterns();
		case ROOM: return COURSE.propertyRooms();
		case BUILDING: return COURSE.propertyBuildings();
		case ROOM_FEATURE: return COURSE.propertyRoomFeatures();
		case ROOM_GROUP: return COURSE.propertyRoomGroups();
		case DISTRIBUTION: return COURSE.propertyDistribution();
		case COURSE: return COURSE.propertyCoursePrefs();
		default: return type.name();
		}
	}

	@Override
	public void setValue(PrefGroupEditResponse response) {
		iResponse = response;
		clear();
		if (iHeader != null)
			addHeaderRow(iHeader);
		
		iPeriodPrefs = null;
		if (response.hasPeriodPreferences()) {
			iPeriodPrefs = new PeriodPreferencesWidget(true);
			iPeriodPrefs.setModel(response.getPeriodPreferences());
			addRow(COURSE.propertyPeriodPrefs(), iPeriodPrefs);
		}
		
		iTimePrefs = null;
		if (response.hasTimePreferences()) {
			iTimePrefs = new TimePreferenceTable(response);
			addRow(response.getTimePreferences().getType(), iTimePrefs);
		}
		iInstructorAvailability = null;
		if (response.hasInstructorTimePrefereneces()) {
			iInstructorAvailability = new InstructorAvailabilityWidget();
			iInstructorAvailability.forPattern(response.getInstructorTimePrefereneces(), true);
			addRow(COURSE.propertyTime(), iInstructorAvailability);
		}

		iDatePrefs = null;
		if (response.hasDatePreferences()) {
			iDatePrefs = new PreferencesTable(response.getDatePreferences(), null, response);
			addRow(getName(response.getDatePreferences().getType()), iDatePrefs);
		}
		
		iRoomPrefs = new ArrayList<PreferencesTable>();
		if (response.hasRoomPreferences()) {
			for (Preferences p: response.getRoomPreferences()) {
				if (!p.hasItems()) continue;
				PreferencesTable tab = new PreferencesTable(p, response.getNbrRooms(), response);
				iRoomPrefs.add(tab);
				addRow(getName(p.getType()), tab);
			}
		}
		
		iDistributionPrefs = null;
		if (response.hasDistributionPreferences()) {
			iDistributionPrefs = new PreferencesTable(response.getDistributionPreferences(), null, response);
			addRow(getName(response.getDistributionPreferences().getType()), iDistributionPrefs);
		}
		
		iCoursePrefs = null;
		if (response.hasCoursePreferences()) {
			iCoursePrefs = new PreferencesTable(response.getCoursePreferences(), null, response);
			addRow(getName(response.getCoursePreferences().getType()), iCoursePrefs);
		}
		
		if (response.hasInstructorUnavailability()) {
			iInstructorUnavailability = new SessionDatesSelector();
			iInstructorUnavailability.forPattern(response.getInstructorUnavailability(), true);
			addRow(COURSE.propertyUnavailableDates(), iInstructorUnavailability);
		}
	}

	class TimePreferenceTable extends P {
		private Map<Long, TimePreferenceWidget> iTimePrefs;
		private PrefGroupEditResponse iResponse;
		private ListBox iTimePatterns;
		
		TimePreferenceTable(PrefGroupEditResponse response) {
			super("time-preferences");
			iResponse = response;
			TimePreferences timePrefs = response.getTimePreferences();
			iTimePatterns = new ListBox();
			final P tpSelection = new P("pattern-selection");
			tpSelection.add(iTimePatterns);
			ImageButton img = new ImageButton(RESOURCES.add());
			tpSelection.add(img);
			iTimePrefs = new HashMap<Long, TimePreferenceWidget>();
			add(tpSelection);
			if (timePrefs.hasSelections()) {
				for (TimeSelection sel: timePrefs.getSelections()) {
					TimePatternModel model = timePrefs.getItem(sel.getItem());
					if (model != null) {
						model.setPreference(sel.getPreference());
						final TimePreferenceWidget tpw = new TimePreferenceWidget(true, iResponse.getPrefLevels(), timePrefs.isHorizontal());
						tpw.setRemove(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								remove(tpw);
								iTimePrefs.remove(tpw.getModel().getId());
								updateTimePatternSelection();
							}
						});
						tpw.setModel(model);
						iTimePrefs.put(sel.getItem(), tpw);
						add(tpw);
					}
				}
			}
			
			img.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String value = iTimePatterns.getSelectedValue();
					if (!value.isEmpty()) {
						TimePatternModel model = iResponse.getTimePreferences().getItem(Long.valueOf(value));
						if (model != null) {
							if (model.isExactTime()) {
								clear();
								add(tpSelection);
								iTimePrefs.clear();
							} else {
								for (TimePreferenceWidget tpw: iTimePrefs.values()) {
									if (tpw.getModel().isExactTime()) {
										remove(tpw);
										iTimePrefs.remove(tpw.getModel().getId());
									}
								}
							}
							model.setPreference(null);
							final TimePreferenceWidget tpw = new TimePreferenceWidget(true, iResponse.getPrefLevels(), timePrefs.isHorizontal());
							tpw.setRemove(new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									remove(tpw);
									iTimePrefs.remove(tpw.getModel().getId());
									updateTimePatternSelection();
								}
							});
							tpw.setModel(model);
							iTimePrefs.put(model.getId(), tpw);
							add(tpw);
							tpw.getElement().scrollIntoView();
						}
						updateTimePatternSelection();
					}
				}
			});
			updateTimePatternSelection();
		}
		
		public void update() {
			iResponse.getTimePreferences().clearSelections();
			for (TimePreferenceWidget tpw: iTimePrefs.values()) {
				iResponse.getTimePreferences().addSelection(tpw.getValue());
			}
		}
		
		public void updateTimePatternSelection() {
			iTimePatterns.clear();
			iTimePatterns.addItem(MESSAGES.itemSelect(), "");
			for (TimePatternModel model: iResponse.getTimePreferences().getItems()) {
				if (!iTimePrefs.containsKey(model.getId()) && model.isValid())
					iTimePatterns.addItem(model.getName(), model.getId().toString());
			}
		}
		
		public String validate() {
			for (TimePreferenceWidget tpw: iTimePrefs.values()) {
				TimePatternModel model = tpw.getModel();
				if (model.isExactTime()) {
					if (model.getPreference() == null || model.getPreference().isEmpty())
						return COURSE.errorInvalidTimePreference();
					Set<String> times = new HashSet<String>();
					for (String p: model.getPreference().split(";"))
						if (!times.add(p))
							return COURSE.errorInvalidTimePreference();
				}
			}
			return null;
		}
	}
	
	static class PreferencesTable extends P {
		static int lastId = 0;
		ChangeHandler iChangeHandler;
		HandlerRegistration iHandlerRegistration;
		Preferences iPreferences;
		
		PreferencesTable(Preferences preferences, final Integer nbrRooms, final PrefGroupEditResponse response) {
			super("preference-table");
			iPreferences = preferences;
			if (preferences.hasSelections()) {
				for (Selection selection: preferences.getSelections()) {
					PreferenceLine p = new PreferenceLine(iPreferences.getItems(), response, preferences.isAllowHard(), nbrRooms);
					IdLabel item = preferences.getItem(selection.getItem());
					PrefLevel level = response.getPrefLevel(selection.getLevel());
					if (item != null && level != null && !preferences.isAllowHard()) {
						if ("P".equals(level.getCode()) || "R".equals(level.getCode()))
							p.setEditable(false);
					}
					p.setValue(selection);
					add(p);
				}
			}
			add(new PreferenceLine(iPreferences.getItems(), response, preferences.isAllowHard(), nbrRooms));
			
			iChangeHandler = new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					if (((PreferenceLine)getWidget(getWidgetCount() - 1)).hasSelection()) {
						PreferenceLine p = new PreferenceLine(iPreferences.getItems(), response, preferences.isAllowHard(), nbrRooms);
						add(p);
						iHandlerRegistration.removeHandler();
						iHandlerRegistration = p.addChangeHandler(iChangeHandler);
						fixButtons();
					}
				}
			};
			fixButtons();
		}
		
		protected void update() {
			iPreferences.clearSelections();
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = (PreferenceLine)getWidget(i);
				Selection selection = p.getValue();
				if (selection != null)
					iPreferences.addSelection(selection);
			}
		}
		
		protected String errorMessage() {
			switch (iPreferences.getType()) {
			case ROOM: return COURSE.errorInvalidRoomPreference();
			case BUILDING: return COURSE.errorInvalidBuildingPreference();
			case ROOM_FEATURE: return COURSE.errorInvalidRoomFeaturePreference();
			case ROOM_GROUP: return COURSE.errorInvalidRoomGroup();
			case DATE: return COURSE.errorInvalidDatePatternPreference();
			case DISTRIBUTION: return COURSE.errorInvalidDistributionPreference();
			case COURSE: return COURSE.errorInvalidCoursePreference();
			default: return null;
			}
		}
			
		
		public String validate() {
			List<Selection> selections = new ArrayList<Selection>(getWidgetCount());
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = (PreferenceLine)getWidget(i);
				IdLabel item = p.getItem();
				if (item == null) continue;
				PrefLevel pref = p.getSelection();
				if (pref == null || "0".equals(pref.getCode())) return errorMessage();
				Selection s = p.getValue();
				for (Selection other: selections)
					if (other.getItem().equals(s.getItem()) && 
							PrefGroupEditInterface.equals(other.getRoomIndex(), s.getRoomIndex()))
						return errorMessage();
				selections.add(s);
			}
			return null;
		}
		
		protected void fixButtons() {
			if (iHandlerRegistration != null) 
				iHandlerRegistration.removeHandler();
			for (int i = 0; i < getWidgetCount(); i++) {
				PreferenceLine p = ((PreferenceLine)getWidget(i)); 
				if (i < getWidgetCount() - 1) {
					p.setButtonAdd(false);
				} else {
					p.setButtonAdd(true);
					iHandlerRegistration = p.addChangeHandler(iChangeHandler);		
				}
			}
		}
		
		class PreferenceLine extends P implements HasChangeHandlers, TakesValue<Selection> {
			ListBox iList;
			ListBox iRoomIndex;
			List<RadioButton> iRadios;
			ImageButton iButton;
			boolean iButtonAdd = false;
			List<PrefLevel> iOptions;
			Collection<IdLabel> iItems;
			P iDescription;
			boolean iAllowHard;
			boolean iEditable;
			PrefLevel iNeutral = null;
			
			PreferenceLine(Collection<IdLabel> items, final PrefGroupEditResponse response, final boolean allowHard, final Integer nbrRooms) {
				super("preference-line");
				P line1 = new P("first-line");
				P line2 = new P("second-line");
				add(line1); add(line2);
				
				iOptions = new ArrayList<PrefLevel>();
				iItems = items;
				iAllowHard = allowHard;

				iList = new ListBox();
				iList.addItem("-", "");
				for (IdLabel item: items)
					iList.addItem(item.getLabel(), item.getId().toString());
				iList.addStyleName("preference-cell");
				iList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						fixOptions();
						PreferenceLine.this.fireEvent(event);
					}
				});
				line1.add(iList);

				iRadios = new ArrayList<RadioButton>();
				for (final PrefLevel option: response.getPrefLevels()) {
					if ("0".equals(option.getCode())) {
						iNeutral = option;
						continue;
					}
					iOptions.add(option);
					RadioButton opt = new RadioButton("pref" + lastId, option.getLabel());
					opt.setTitle(option.getTitle());
					opt.getElement().getStyle().setColor(option.getColor());
					opt.addStyleName("preference-cell");
					iRadios.add(opt);
					line1.add(opt);
					opt.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue() && !iAllowHard) fixOptions();
							ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), PreferenceLine.this);
						}
					});
				}
				
				if (nbrRooms != null && nbrRooms > 1) {
					iRoomIndex = new ListBox();
					iRoomIndex.addItem(COURSE.itemAllRooms(), "");
					for (int i = 0; i < nbrRooms; i++)
						iRoomIndex.addItem(COURSE.itemOnlyRoom(1 + i), String.valueOf(i));	
					iRoomIndex.addStyleName("preference-cell");
					iRoomIndex.addStyleName("room-index");
					iRoomIndex.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							PreferenceLine.this.fireEvent(event);
						}
					});
					line1.add(iRoomIndex);
					iRoomIndex.setVisible(false);
				}
				
				iButton = new ImageButton(RESOURCES.delete());
				iButton.setTitle(MESSAGES.titleDeleteRow());
				iButton.addStyleName("preference-cell");
				iButton.getElement().getStyle().setCursor(Cursor.POINTER);
				iButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iButtonAdd) {
							PreferencesTable.this.add(new PreferenceLine(iItems, response, iAllowHard, nbrRooms));
						} else {
							PreferencesTable.this.remove(PreferenceLine.this);
						}
						PreferencesTable.this.fixButtons();
					}
				});
				line1.add(iButton);
				
				iDescription = new P("description");
				iDescription.setVisible(false);
				line2.add(iDescription);
				
				fixOptions();
				lastId ++;
			}
			
			public void setEditable(boolean editable) {
				iList.setEnabled(editable);
				if (iRoomIndex != null)
					iRoomIndex.setEnabled(editable);
			}
			
			protected IdLabel getItem() {
				String id = iList.getSelectedValue();
				if (id == null || id.isEmpty()) return null;
				for (IdLabel item: iItems)
					if (item.getId().toString().equals(id)) return item;
				return null;
			}
			
			protected void fixOptions() {
				IdLabel item = getItem();
				if (item != null && item.hasDescription()) {
					iDescription.setVisible(true);
					iDescription.setHTML(item.getDescription());
				} else {
					iDescription.setVisible(false);
				}
				if (!iList.isEnabled() && !getSelection().isHard())
					setEditable(true);
				int nbrVisible = 0;
				for (int i = 0; i < iOptions.size(); i++) {
					RadioButton opt = iRadios.get(i);
					PrefLevel option = iOptions.get(i);
					if (item == null || (!iAllowHard && ("P".equals(option.getCode()) || "R".equals(option.getCode())) && !opt.getValue()) || !item.isAllowed(option)) {
						opt.setEnabled(opt.getValue());
						opt.setVisible(opt.getValue());
					} else {
						opt.setEnabled(iList.isEnabled());
						opt.setVisible(iList.isEnabled() || opt.getValue());
						nbrVisible ++;
					}
				}
				if (iRoomIndex != null) 
					iRoomIndex.setVisible(nbrVisible > 0);
			}
			
			public PrefLevel getSelection() {
				for (int i = 0; i < iOptions.size(); i++) {
					RadioButton opt = iRadios.get(i);
					if (Boolean.TRUE.equals(opt.getValue()))
						return iOptions.get(i);
				}
				return iNeutral;
			}
			
			public Integer getRoomIndex() {
				if (iRoomIndex == null || iRoomIndex.getSelectedIndex() == 0) return null;
				return Integer.valueOf(iRoomIndex.getSelectedValue());
			}
			
			public Long getId() {
				String id = iList.getSelectedValue();
				if (id == null || id.isEmpty()) return null;
				if (getSelection() == null) return null;
				return Long.valueOf(id);
			}
			
			public boolean hasSelection() {
				String id = iList.getSelectedValue();
				if (id == null || id.isEmpty()) return false;
				PrefLevel selection = getSelection();
				if (selection == null || "0".equals(selection.getCode())) return false;
				return true;
			}

			public void setId(Long id) {
				if (id == null) {
					iList.setSelectedIndex(0);
				} else {
					for (int i = 1; i < iList.getItemCount(); i++) {
						if (iList.getValue(i).equals(id.toString())) {
							iList.setSelectedIndex(i);
							break;
						}
					}
				}
			}

			@Override
			public HandlerRegistration addChangeHandler(ChangeHandler handler) {
				return addDomHandler(handler, ChangeEvent.getType());
			}
			
			public void setButtonAdd(boolean add) {
				iButtonAdd = add;
				if (add) {
					iButton.setImage(RESOURCES.add());
					iButton.setTitle(MESSAGES.titleAddRow());
				} else {
					iButton.setImage(RESOURCES.delete());
					iButton.setTitle(MESSAGES.titleDeleteRow());
				}
			}

			@Override
			public void setValue(Selection value) {
				if (value == null) {
					iList.setSelectedIndex(0);
					for (RadioButton b: iRadios)
						b.setValue(false);
				} else {
					for (int i = 0; i < iList.getItemCount(); i++) {
						if (value.getItem().toString().equals(iList.getValue(i))) {
							iList.setSelectedIndex(i); break;
						}
					}
					for (int i = 0; i < iOptions.size(); i++) {
						RadioButton opt = iRadios.get(i);
						opt.setValue(iOptions.get(i).getId().equals(value.getLevel()));
						if (!iList.isEnabled())
							opt.setText(iOptions.get(i).getTitle());
					}
				}
				if (iRoomIndex != null) {
					if (value.getRoomIndex() == null)
						iRoomIndex.setSelectedIndex(0);
					else
						iRoomIndex.setSelectedIndex(1 + value.getRoomIndex());
				}
				fixOptions();
			}

			@Override
			public Selection getValue() {
				IdLabel item = getItem();
				PrefLevel pref = getSelection();
				if (item != null && pref != null) {
					Selection ret = new Selection(item.getId(), pref.getId());
					ret.setRoomIndex(getRoomIndex());
					return ret;
				}
				return null;
			}
		}
		
	}
	
	public String validate() {
		if (iTimePrefs != null) {
			String error = iTimePrefs.validate();
			if (error != null) return error;
		}
		if (iDatePrefs != null) {
			String error = iDatePrefs.validate();
			if (error != null) return error;
		}
		if (iRoomPrefs != null)
			for (PreferencesTable p: iRoomPrefs) {
				String error = p.validate();
				if (error != null) return error;
			}
		if (iDistributionPrefs != null) {
			String error = iDistributionPrefs.validate();
			if (error != null) return error;
		}
		if (iCoursePrefs != null) {
			String error = iCoursePrefs.validate();
			if (error != null) return error;
		}
		return null;
	}
}
