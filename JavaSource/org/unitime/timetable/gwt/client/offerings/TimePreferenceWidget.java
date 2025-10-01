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
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.PrefLevel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimePatternModel;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.TimeSelection;
import org.unitime.timetable.gwt.client.widgets.DayCodeSelector;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;

public class TimePreferenceWidget extends Composite implements HasValue<TimeSelection>{
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages CMSG = GWT.create(CourseMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private TimePatternModel iModel;
	private List<PrefLevel> iPreferences;
	private AbsolutePanel iPanel;
	private PrefLevel iOption = null;
	private P iSelectedIcon = null, iSelectedTitle = null;
	protected boolean iEditable = true;
	private int iSplit = 14;
	private boolean iShowLegend = true;
	private boolean iHorizontal = true;
	private List<Cell> iAll = new ArrayList<Cell>();
	private ClickHandler iRemove;
	private ExactTable iExactTable;
	
	public TimePreferenceWidget(boolean editable, List<PrefLevel> preferences, boolean horizontal) {
		iEditable = editable;
		iPreferences = preferences;
		iHorizontal = horizontal;
		for (PrefLevel p: iPreferences)
			if ("0".equals(p.getCode())) iOption = p;
		
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-RoomSharingWidget");
		initWidget(iPanel);
	}
	
	public void setRemove(ClickHandler remove) { iRemove = remove; }
	
	public boolean isEditable() {
		return iEditable;
	}
	
	public void setEditable(boolean editable) {
		iEditable = editable;
	}
	
	public boolean isEditable(PrefLevel pref) {
		return isEditable() && iModel.isAllowedPref(pref.getTpCode());
	}
	
	public boolean isHorizontal() { return iHorizontal; }
	public void setHorizontal(boolean horizontal) { iHorizontal = horizontal; }
	public boolean isShowLegend() { return iShowLegend; }
	public void setShowLegend(boolean showLegend) { iShowLegend = showLegend; }
	
	public PrefLevel getPreference(int day, int slot) {
		char code = iModel.getPreference(day, slot);
		for (PrefLevel pref: iPreferences)
			if (pref.getTpCode() == code) return pref;
		for (PrefLevel pref: iPreferences)
			if ("0".equals(pref.getCode())) return pref;
		return null;
	}
	
	public PrefLevel getPreference(String code) {
		for (PrefLevel pref: iPreferences)
			if (code.equals(pref.getCode())) return pref;
		return null;
	}
	
	public TimePatternModel getModel() {
		if (iModel.isExactTime())
			iModel.setPreference(iExactTable.getValue());
		return iModel;
	}
	
	public void setModel(TimePatternModel model) {
		iModel = model;
		iPanel.clear();
		iAll.clear();
		if (iModel.getName() != null && !iModel.getName().isEmpty()) {
			P header = new P("header");
			P label = new P("name");
			label.setText(iModel.getName());
			header.add(label);
			if (iRemove != null) {
				ImageButton delete = new ImageButton(RESOURCES.delete());
				delete.addClickHandler(iRemove);
				delete.addStyleName("delete");
				header.add(delete);
			}
			if (!model.isValid()) {
				P warn = new P("warning");
				warn.setText(CMSG.warnNoMatchingDatePattern());
				header.add(warn);
			}
			iPanel.add(header);
		}
		if (iModel.isExactTime()) {
			iExactTable = new ExactTable();
			iExactTable.setValue(model.getPreference());
			iPanel.add(iExactTable);
			return;
		} else {
			iExactTable = null;
		}
		if (iHorizontal) {
			P previousTable = null;
			for (int page = 0; page == 0 || (iSplit > 0 && iSplit * page < iModel.getTimes().size()); page++) {
				P table = new P("table");
				iPanel.add(table);
				P box = new P("box");
				table.add(box);
				P header = new P("row");
				box.add(header);
				P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
				corner.setHTML(MESSAGES.roomSharingCorner());
				header.add(corner);
				
				int first = 0;
				int last = iModel.getTimes().size();
				if (iSplit > 0) {
					first = iSplit * page;
					last = Math.min(first + iSplit, iModel.getTimes().size());
					if (previousTable != null)
						previousTable.getElement().getStyle().setDisplay(Display.BLOCK);
					previousTable = table;
				}
				
				final List<List<Cell>> thisTime = new ArrayList<List<Cell>>();
				for (int slot = first; slot < last; slot ++) {
					P p = new P("cell", "time", isEditable() ? "clickable" : null);
					p.setHTML(MESSAGES.roomSharingTimeHeader(iModel.getStartTime(slot, CONSTANTS), iModel.getEndTime(slot, CONSTANTS)));
					final List<Cell> t = new ArrayList<Cell>();
					thisTime.add(t);
					header.add(p);
					if (isEditable())
						p.addMouseDownHandler(new MouseDownHandler() {
							@Override
							public void onMouseDown(MouseDownEvent event) {
								for (Cell d: t)
									d.setOption(iOption);
							}
						});
				}
				
				final List<Cell> thisPage = new ArrayList<Cell>();
				for (int day = 0; day < iModel.getDays().size(); day++) {
					P line = new P("row");
					box.add(line);
					P d = new P("cell", "day", isEditable() ? "clickable" : null);
					d.setHTML(iModel.getDaysLabel(day, CONSTANTS));
					line.add(d);
					final List<Cell> thisDay = new ArrayList<Cell>();
					for (int slot = first; slot < last; slot ++) {
						Cell p = new Cell(day, slot);
						if (iModel.isAssigned(day, slot))
							p.addStyleName("highlight");
						line.add(p);
						thisDay.add(p);
						thisPage.add(p);
						thisTime.get(slot - first).add(p);
						iAll.add(p);
					}
					if (isEditable())
						d.addMouseDownHandler(new MouseDownHandler() {
							@Override
							public void onMouseDown(MouseDownEvent event) {
								for (Cell d: thisDay)
									d.setOption(iOption);
							}
						});
				}
				
				if (isEditable())
					corner.addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							for (Cell d: thisPage)
								d.setOption(iOption);
						}
					});
			}	
		} else {
			P table = new P("table");
			iPanel.add(table);
			P box = new P("box");
			table.add(box);
			P header = new P("row");
			box.add(header);
			P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
			corner.setHTML(MESSAGES.roomSharingCorner());
			header.add(corner);
			
			final List<List<Cell>> thisDay = new ArrayList<List<Cell>>();
			for (int day = 0; day < iModel.getDays().size(); day++) {
				P p = new P("cell", "time", isEditable() ? "clickable" : null); p.setHTML(iModel.getDaysLabel(day, CONSTANTS));
				final List<Cell> t = new ArrayList<Cell>();
				thisDay.add(t);
				header.add(p);
				if (isEditable())
					p.addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							for (Cell d: t)
								d.setOption(iOption);
						}
					});
			}

			
			final List<Cell> thisPage = new ArrayList<Cell>();
			for (int slot = 0; slot < iModel.getTimes().size(); slot ++) {
				P line = new P("row");
				box.add(line);
				P d = new P("cell", "day", isEditable() ? "clickable" : null);
				d.setHTML(MESSAGES.roomSharingTimeHeader(iModel.getStartTime(slot, CONSTANTS), iModel.getEndTime(slot, CONSTANTS)));
				line.add(d);
				final List<Cell> thisSlot = new ArrayList<Cell>();
				for (int day = 0; day < iModel.getDays().size(); day++) {
					Cell p = new Cell(day, slot);
					line.add(p);
					thisSlot.add(p);
					thisPage.add(p);
					thisDay.get(day).add(p);
					iAll.add(p);
				}
				if (isEditable())
					d.addMouseDownHandler(new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							for (Cell d: thisSlot)
								d.setOption(iOption);
						}
					});
			}
			
			if (isEditable())
				corner.addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						for (Cell d: thisPage)
							d.setOption(iOption);
					}
				});
		}
		
		if (!isShowLegend() && !isEditable()) return;
		
		P legend = new P("legend");
		iPanel.add(legend);
		
		final P box = new P("box");
		legend.add(box);
		
		iSelectedIcon = null; iSelectedTitle = null;
		for (final PrefLevel option: iPreferences) {
			if (!iModel.isAllowedPref(option.getTpCode()) && iModel.getPreference().indexOf(option.getTpCode()) < 0) continue;
			final P line = new P("row");
			
			final P icon = new P("cell", isEditable() && iModel.isAllowedPref(option.getTpCode()) ? "clickable" : null);
			if (box.getWidgetCount() == 0) icon.addStyleName("first");
			icon.getElement().getStyle().setBackgroundColor(option.getColor());
			line.add(icon);
			
			final P title = new P("title", isEditable(option) ? "editable-title" : null); title.setHTML(option.getTitle());
			line.add(title);
			
			if (isEditable(option) && option.equals(iOption)) {
				icon.addStyleName("selected");
				title.addStyleName("selected-title");
				iSelectedIcon = icon;
				iSelectedTitle = title;
			}

			if (isEditable(option)) {
				MouseDownHandler md = new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						iOption = option;
						if (iSelectedIcon != null)
							iSelectedIcon.removeStyleName("selected");
						if (iSelectedTitle != null)
							iSelectedTitle.removeStyleName("selected-title");
						icon.addStyleName("selected");
						title.addStyleName("selected-title");
						iSelectedIcon = icon;
						iSelectedTitle = title;
						if (iModel.isReqConfirmation()) {
							if ("R".equals(iOption.getCode()) && iModel.hasPreference()) {
								UniTimeConfirmationDialog.alert(MESSAGES.warnPreferenceUseRequired());
							}
							if (iModel.hasRequired() && !"R".equals(iOption.getCode()) && !"0".equals(iOption.getCode())) {
								UniTimeConfirmationDialog.alert(MESSAGES.warnPreferenceUseNotRequired());
							}
						}
					}
				};
				
				icon.addMouseDownHandler(md);
				title.addMouseDownHandler(md);
			}
			
			box.add(line);
		}
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TimeSelection> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public TimeSelection getValue() {
		TimeSelection ret = new TimeSelection();
		ret.setItem(iModel.getId());
		if (iExactTable != null)
			ret.setPreference(iExactTable.getValue());
		else
			ret.setPreference(iModel.getPreference());
		return ret;
	}

	@Override
	public void setValue(TimeSelection selection) {
		setValue(selection, false);
	}

	@Override
	public void setValue(TimeSelection selection, boolean fireEvents) {
		iModel.setPreference(selection.getPreference());
		setModel(iModel);
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
	
	public class Cell extends P {
		private int iDay, iSlot;
		
		Cell(int day, int slot) {
			super("cell", "item", isEditable() ? "clickable" : null);
			iDay = day; iSlot = slot;
			PrefLevel option = getPreference(day, slot);
			getElement().getStyle().setBackgroundColor(option.getColor());
			setTitle(iModel.getDaysLabel(iDay, CONSTANTS) + " " + iModel.getStartTime(iSlot, CONSTANTS) + " - " + iModel.getEndTime(iSlot, CONSTANTS) + ": " + option.getTitle());
			if (isEditable())
				addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
						setOption(iOption);
					}
				});
		}
		
		public void setOption(PrefLevel option) {
			iModel.setPreference(iDay, iSlot, option.getTpCode());
			getElement().getStyle().setBackgroundColor(option.getColor());
			setTitle(iModel.getDaysLabel(iDay, CONSTANTS) + " " + iModel.getStartTime(iSlot, CONSTANTS) + " - " + iModel.getEndTime(iSlot, CONSTANTS) + ": " + option.getTitle());
			ValueChangeEvent.fire(TimePreferenceWidget.this, getValue());
			if ("R".equals(option.getCode()) && iModel.hasPreference()) {
				PrefLevel neutral = getPreference("0");
				for (Cell cell: iAll) {
					char code = iModel.getPreference(cell.getDay(), cell.getSlot());
					if (code != 'R' && code != '2')
						cell.setOption(neutral);
				}
			}
			if (!"R".equals(option.getCode()) && !"0".equals(option.getCode()) && iModel.hasRequired()) {
				PrefLevel neutral = getPreference("0");
				for (Cell cell: iAll) {
					char code = iModel.getPreference(cell.getDay(), cell.getSlot());
					if (code == 'R')
						cell.setOption(neutral);
				}
			}
		}
		
		public int getDay() { return iDay; }
		public int getSlot() { return iSlot; }
	}
	
	class ExactTable extends P implements TakesValue<String> {
		ChangeHandler iChangeHandler;
		HandlerRegistration iHandlerRegistration;
		
		public ExactTable() {
			super("preference-table");
			iChangeHandler = new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					if (((ExactLine)getWidget(getWidgetCount() - 1)).getValue() != null) {
						ExactLine p = new ExactLine(null, null);
						add(p);
						iHandlerRegistration.removeHandler();
						iHandlerRegistration = p.addChangeHandler(iChangeHandler);
						fixButtons();
					}
				}
			};
		}
		
		protected void fixButtons() {
			for (int i = 0; i < getWidgetCount(); i++) {
				ExactLine p = ((ExactLine)getWidget(i)); 
				if (i < getWidgetCount() - 1) {
					p.setButtonAdd(false);
				} else {
					p.setButtonAdd(true);
					iHandlerRegistration = p.addChangeHandler(iChangeHandler);		
				}
			}
		}
	
		class ExactLine extends P implements HasChangeHandlers, TakesValue<Integer[]> {
			DayCodeSelector iDays;
			TimeSelector iSlot;
			ImageButton iButton;
			boolean iButtonAdd = false;
			
			ExactLine(Integer days, Integer slot) {
				super("preference-line");
				
				iDays = new DayCodeSelector();
				if (days != null)
					iDays.setValue(days);
				iDays.addStyleName("preference-cell");
				iDays.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					@Override
					public void onValueChange(ValueChangeEvent<Integer> event) {
						ExactLine.this.fireEvent(event);
					}
				});
				add(iDays);

				iSlot = new TimeSelector();
				if (slot != null) iSlot.setValue(slot);
				iSlot.addStyleName("preference-cell");
				iSlot.addValueChangeHandler(new ValueChangeHandler<Integer>() {
					@Override
					public void onValueChange(ValueChangeEvent<Integer> event) {
						ExactLine.this.fireEvent(event);
					}
				});
				add(iSlot);
				
				iButton = new ImageButton(RESOURCES.delete());
				iButton.setTitle(MESSAGES.titleDeleteRow());
				iButton.addStyleName("preference-cell");
				iButton.getElement().getStyle().setCursor(Cursor.POINTER);
				iButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iButtonAdd) {
							ExactTable.this.add(new ExactLine(null, null));
						} else {
							ExactTable.this.remove(ExactLine.this);
						}
						ExactTable.this.fixButtons();
					}
				});
				add(iButton);
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
			public void setValue(Integer[] value) {
				if (value == null) {
					iDays.setValue(null);
					iSlot.setValue(null);
				} else {
					iDays.setValue(value[0]);
					iSlot.setValue(value[1]);
				}
			}

			@Override
			public Integer[] getValue() {
				if (iSlot.getValue() == null)
					return null;
				return new Integer[] { iDays.getValue(), iSlot.getValue() };
			}
		}

		@Override
		public String getValue() {
			String ret = null;
			for (int i = 0; i < getWidgetCount(); i++) {
				ExactLine line = (ExactLine)getWidget(i);
				Integer[] value = line.getValue();
				if (value != null) {
					if (ret == null)
						ret = value[0] + "," + value[1];
					else
						ret += ";" + value[0] + "," + value[1];
				}
			}
			return ret;
		}

		@Override
		public void setValue(String pattern) {
			clear();
			if (pattern != null)
				for (String s: pattern.split(";")) {
					if (s.indexOf(',') >= 0) {
						add(new ExactLine(
								Integer.valueOf(s.substring(0, s.indexOf(','))),
								Integer.valueOf(s.substring(1+ s.indexOf(',')))));
					}
				}
			add(new ExactLine(null, null));
			
			iChangeHandler = new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					ExactLine last = (ExactLine)getWidget(getWidgetCount() - 1);
					if (last.getValue() != null) {
						ExactLine el = new ExactLine(null, null);
						add(el);
						iHandlerRegistration.removeHandler();
						iHandlerRegistration = el.addChangeHandler(iChangeHandler);
						fixButtons();
					}
				}
			};
			fixButtons();			
		}
	}

}
