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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaListBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.SimpleForm.HasMobileScroll;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.OptionElement;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.impl.FocusImpl;

/**
 * @author Tomas Muller
 */
public class RoomSharingWidget extends Composite implements HasValue<RoomSharingModel>, HasMobileScroll {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private AbsolutePanel iPanel;
	private AriaListBox iModeSelection;
	private RoomSharingDisplayMode iMode = null;
	private CheckBox iHorizontal;
	private RoomSharingOption iOption = null;
	private P iSelectedIcon = null; FP iSelectedTitle = null;
	private FP iLast = null, iSelected = null;
	private RoomSharingModel iModel;
	protected boolean iEditable = true;
	private TextArea iNote = null;
	private Set<Long> iAddedOptions = new HashSet<Long>();
	private int iSplit = 24;
	private boolean iShowLegend = true;
	
	public RoomSharingWidget(boolean editable) {
		this(editable, true);
	}
	
	public RoomSharingWidget(boolean editable, boolean includeNote) {
		iEditable = editable;
		VerticalPanel container = new VerticalPanel();
		
		HorizontalPanel hp = new HorizontalPanel();
		
		iModeSelection = new AriaListBox();
		iModeSelection.setStyleName("unitime-TextBox");
		iModeSelection.setAriaLabel(ARIA.inputModeSelection());
		hp.add(iModeSelection);
		hp.setCellHorizontalAlignment(iModeSelection, HasHorizontalAlignment.ALIGN_LEFT);
		
		iHorizontal = new CheckBox(MESSAGES.roomSharingHorizontal());
		hp.add(iHorizontal);
		hp.setCellHorizontalAlignment(iHorizontal, HasHorizontalAlignment.ALIGN_RIGHT);
		hp.setCellVerticalAlignment(iHorizontal, HasVerticalAlignment.ALIGN_MIDDLE);
		hp.setWidth("100%");
		
		container.add(hp);
		
		iModeSelection.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				setMode(iModel.getModes().get(iModeSelection.getSelectedIndex()), iHorizontal.getValue());
			}
		});
		
		iHorizontal.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				setMode(iModel.getModes().get(iModeSelection.getSelectedIndex()), iHorizontal.getValue());
			}
		});
		
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-RoomSharingWidget");
		container.add(iPanel);
		
		if (iEditable && includeNote) {
			iNote = new TextArea();
			iNote.setStyleName("unitime-TextArea");
			Roles.getTextboxRole().setAriaLabelProperty(iNote.getElement(), MESSAGES.propRoomAvailabilityNote());
			iNote.setVisibleLines(10);
			iNote.setCharacterWidth(50);
			iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (iModel != null)
						iModel.setNote(event.getValue());
				}
			});
		}
		
		initWidget(container);
	}
	
	public int getSplit() { return iSplit; }
	public void setSplit(int split) { iSplit = split; }
	
	public AbsolutePanel getPanel() { return iPanel; }
	
	public boolean isEditable() {
		return iEditable && iModel != null && iModel.isEditable();
	}
	
	public void setEditable(boolean editable) {
		iEditable = editable;
	}
	
	public boolean isEditable(RoomSharingOption option) {
		return isEditable() && option.isEditable();
	}
	
	public boolean isDeletable(RoomSharingOption option) {
		return isEditable() && option.isDeletable();
	}
	
	public boolean isEditable(int day, int slot) {
		return isEditable() && iModel.isEditable(day, slot, iMode.getStep());
	}
	
	public RoomSharingModel getModel() {
		return iModel;
	}
	
	public void setModel(RoomSharingModel model) {
		iModel = model;
		
		if (iEditable && iModel.isNoteEditable() && iNote != null)
			iNote.setValue(iModel.hasNote() ? iModel.getNote() : "");
		
		iModeSelection.clear();
		for (RoomSharingDisplayMode mode: iModel.getModes())
			iModeSelection.addItem(mode.getName());
		
		iModeSelection.setSelectedIndex(iModel.getDefaultMode());
		iHorizontal.setValue(model.isDefaultHorizontal());
		
		if (iModeSelection.getItemCount() > 1) {
			RoomCookie cookie = RoomCookie.getInstance();
			if (cookie.hasMode()) {
				for (int i = 0; i < iModel.getModes().size(); i++)
					if (cookie.getMode().equals(iModel.getModes().get(i).toHex())) {
						iModeSelection.setSelectedIndex(i); break;
					}
				iHorizontal.setValue(cookie.areRoomsHorizontal());
			} else {
				iHorizontal.setValue(model.isDefaultHorizontal());
				iModeSelection.setSelectedIndex(iModel.getDefaultMode());
			}
		}
		
		iOption = iModel.getDefaultOption();
		if (iOption == null) iOption = iModel.getOptions().get(0);

		setMode(iModel.getModes().get(iModeSelection.getSelectedIndex()), iHorizontal.getValue());
	}
	
	public void setShowLegend(boolean showLegend) { iShowLegend = showLegend; }
	
	public void insert(final RootPanel panel, boolean eventAvailability) {
		Long locationId = Long.valueOf(panel.getElement().getInnerHTML().trim());
		RPC.execute(RoomInterface.RoomSharingRequest.load(locationId, eventAvailability), new AsyncCallback<RoomSharingModel>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MESSAGES.failedToLoadRoomAvailability(caught.getMessage()));
			}

			@Override
			public void onSuccess(RoomSharingModel result) {
				panel.getElement().setInnerText(null);
				setModel(result);
				panel.add(RoomSharingWidget.this);
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
	
	public class Cell extends P {
		private int iDay, iSlot;
		
		Cell(int day, int slot) {
			super("cell", "item", isEditable(day, slot) ? "clickable" : null);
			iDay = day; iSlot = slot;
			RoomSharingOption option = iModel.getOption(day, slot);
			if (option == null) {
				getElement().getStyle().clearBackgroundColor();
				setHTML("");
				setTitle("");
			} else {
				getElement().getStyle().setBackgroundColor(option.getColor());
				if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
					getElement().getStyle().setColor("#000000");
				else
					getElement().getStyle().setColor("#ffffff");
				setHTML(option.getCode() == null ? "" : option.getCode());
				setTitle(CONSTANTS.longDays()[day] + " " + slot2short(slot) + " - " + slot2short(slot + iMode.getStep()) + ": " + option.getName());
			}
			if (isEditable(day, slot))
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						setOption(iOption);
					}
				});
		}
		
		public void setOption(RoomSharingOption option) {
			if (!isEditable(iDay, iSlot)) return;
			iModel.setOption(iDay, iSlot, iMode.getStep(), option);
			if (option == null) {
				getElement().getStyle().clearBackgroundColor();
				setHTML("");
				setTitle("");
			} else {
				getElement().getStyle().setBackgroundColor(option.getColor());
				if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
					getElement().getStyle().setColor("#000000");
				else
					getElement().getStyle().setColor("#ffffff");
				setHTML(option.getCode() == null ? "" : option.getCode());
				setTitle(CONSTANTS.longDays()[iDay] + " " + slot2short(iSlot) + " - " + slot2short(iSlot + iMode.getStep()) + ": " + option.getName());
			}
			ValueChangeEvent.fire(RoomSharingWidget.this, getValue());
		}
		
		public int getDay() { return iDay; }
		public int getSlot() { return iSlot; }
	}
	
	protected void addPreferenceIfNeeded(P line, final RoomSharingOption option) {
		if (option.hasPreference() && iModel.getPreferences() != null) {
			final AriaListBox pref = new AriaListBox();
			pref.setStyleName("unitime-TextBox");
			pref.addStyleName("preference");
			pref.setAriaLabel(ARIA.listSelectPreferenceLevelFor(option.getName()));
			for (PreferenceInterface p: iModel.getPreferences()) {
				pref.addItem(p.getName(), p.getId().toString());
			}
			SelectElement selectElement = SelectElement.as(pref.getElement());
			NodeList<OptionElement> items = selectElement.getOptions();
			for (int i = 0; i < items.getLength(); i++) {
				PreferenceInterface p = iModel.getPreferences().get(i);
				if (p.getColor() != null) {
					items.getItem(i).getStyle().setBackgroundColor(p.getColor());
					if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
						items.getItem(i).getStyle().setColor("#000000");
					else
						items.getItem(i).getStyle().setColor("#ffffff");
				}
				if (items.getItem(i).getValue().equals(option.getPreference().toString())) pref.setSelectedIndex(i);
			}
			pref.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					option.setPreference(iModel.getPreferences().get(pref.getSelectedIndex()).getId());
				}
			});
			line.add(pref);
		}
	}
	
	public void setMode(final RoomSharingDisplayMode mode, final boolean horizontal) {
		iMode = mode;
		RoomCookie.getInstance().setMode(horizontal, mode.toHex());
		iPanel.clear();
		
		if (horizontal) {
			P previousTable = null;
			for (int page = 0; page == 0 || (iSplit > 0 && iMode.getFirstSlot() + iSplit * page * iMode.getStep() < iMode.getLastSlot()); page++) {
				P table = new P("table");
				iPanel.add(table);
				P box = new Box();
				table.add(box);
				P header = new P("row");
				box.add(header);
				P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
				corner.setHTML(MESSAGES.roomSharingCorner());
				header.add(corner);
				
				int first = iMode.getFirstSlot();
				int last = iMode.getLastSlot();
				if (iSplit > 0) {
					first = iMode.getFirstSlot() + iSplit * page * iMode.getStep();
					last = Math.min(first + iSplit * iMode.getStep(), iMode.getLastSlot());
					if (previousTable != null)
						previousTable.getElement().getStyle().setDisplay(Display.BLOCK);
					previousTable = table;
				}
				
				final List<List<Cell>> thisTime = new ArrayList<List<Cell>>();
				for (int slot = first; slot < last; slot += iMode.getStep()) {
					P p = new P("cell", "time", isEditable() ? "clickable" : null); p.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iMode.getStep())));
					final List<Cell> t = new ArrayList<Cell>();
					thisTime.add(t);
					header.add(p);
					if (isEditable())
						p.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								for (Cell d: t)
									d.setOption(iOption);
							}
						});

				}
				
				final List<Cell> thisPage = new ArrayList<Cell>();
				int day = iMode.getFirstDay();
				while (true) {
					P line = new P("row");
					box.add(line);
					P d = new P("cell", "day", isEditable() ? "clickable" : null);
					d.setHTML(CONSTANTS.days()[day % 7]);
					line.add(d);
					final List<Cell> thisDay = new ArrayList<Cell>();
					for (int slot = first; slot < last; slot += iMode.getStep()) {
						Cell p = new Cell(day, slot);
						line.add(p);
						thisDay.add(p);
						thisPage.add(p);
						thisTime.get((slot - first) / iMode.getStep()).add(p);
					}
					if (isEditable())
						d.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								for (Cell d: thisDay)
									d.setOption(iOption);
							}
						});
					if (day == iMode.getLastDay()) break;
					day = (1 + day) % 7;
				}
				
				if (isEditable())
					corner.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							for (Cell d: thisPage)
								d.setOption(iOption);
						}
					});
			}	
		} else {
			P table = new P("table");
			iPanel.add(table);
			P box = new Box();
			table.add(box);
			P header = new P("row");
			box.add(header);
			P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
			corner.setHTML(MESSAGES.roomSharingCorner());
			header.add(corner);
			
			final List<List<Cell>> thisDay = new ArrayList<List<Cell>>();
			int day = iMode.getFirstDay();
			int idx = 0;
			while (true) {
				P p = new P("cell", "time", isEditable() ? "clickable" : null); p.setHTML(CONSTANTS.days()[day % 7]);
				final List<Cell> t = new ArrayList<Cell>();
				thisDay.add(t);
				header.add(p);
				if (isEditable())
					p.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							for (Cell d: t)
								d.setOption(iOption);
						}
					});
				if (day == iMode.getLastDay()) break;
				day = (1 + day) % 7;
				idx ++;				
			}

			
			final List<Cell> thisPage = new ArrayList<Cell>();
			for (int slot = iMode.getFirstSlot(); slot < iMode.getLastSlot(); slot += iMode.getStep()) {
				P line = new P("row");
				box.add(line);
				P d = new P("cell", "day", isEditable() ? "clickable" : null);
				d.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iMode.getStep())));
				line.add(d);
				final List<Cell> thisSlot = new ArrayList<Cell>();
				day = iMode.getFirstDay(); idx = 0;
				while (true) {
					Cell p = new Cell(day, slot);
					line.add(p);
					thisSlot.add(p);
					thisPage.add(p);
					thisDay.get(idx).add(p);
					if (day == iMode.getLastDay()) break;
					day = (1 + day) % 7;
					idx ++;
				}
				if (isEditable())
					d.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							for (Cell d: thisSlot)
								d.setOption(iOption);
						}
					});
			}
			
			if (isEditable())
				corner.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						for (Cell d: thisPage)
							d.setOption(iOption);
					}
				});
		}
				
		if (!iShowLegend && !isEditable()) return;
		
		P legend = new P("legend");
		iPanel.add(legend);
		
		final P box = new P("box");
		legend.add(box);
		
		iSelectedIcon = null; iSelectedTitle = null;
		for (final RoomSharingOption option: iModel.getOptions()) {
			final P line = new P("row");
			
			final P icon = new P("cell", isEditable(option) ? "clickable" : null);
			if (box.getWidgetCount() == 0) icon.addStyleName("first");
			if (option.getCode() != null && !option.getCode().isEmpty()) icon.setHTML(option.getCode());
			icon.getElement().getStyle().setBackgroundColor(option.getColor());
			if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
				icon.getElement().getStyle().setColor("#000000");
			else
				icon.getElement().getStyle().setColor("#ffffff");
			line.add(icon);
			
			final FP title = new FP("title", isEditable(option) ? "editable-title" : null); title.setHTML(option.getName());
			if (isEditable(option)) {
				if (iLast != null) {
					iLast.setNext(title);
					title.setPrevious(iLast);
				}
				iLast = title;
			} else {
				title.setTabIndex(-1);
			}
			line.add(title);
			
			addPreferenceIfNeeded(line, option);
			
			if (option.getId() >= 0 && isDeletable(option)) {
				Image remove = new ImageButton(RESOURCES.delete());
				remove.setAltText(ARIA.iconRemoveItem(option.getName()));
				remove.addStyleName("remove");
				line.add(remove);
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (option.equals(iOption)) iOption = iModel.getDefaultOption();
						box.remove(line);
						iModel.getOptions().remove(option);
						setMode(mode, horizontal);
						if (iSelectedTitle != null) iSelectedTitle.setFocus(true);
					}
				});
			}

			if (isEditable(option) && option.equals(iOption)) {
				icon.addStyleName("selected");
				title.addStyleName("selected-title");
				iSelectedIcon = icon;
				iSelectedTitle = title;
			}

			if (isEditable(option)) {
				ClickHandler md = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						iOption = option;
						if (iSelectedIcon != null)
							iSelectedIcon.removeStyleName("selected");
						if (iSelectedTitle != null)
							iSelectedTitle.removeStyleName("selected-title");
						icon.addStyleName("selected");
						title.addStyleName("selected-title");
						iSelectedIcon = icon;
						iSelectedTitle = title;
						iSelectedTitle.setFocus(true);
					}
				};
				
				icon.addClickHandler(md);
				title.addClickHandler(md);
			}
			
			box.add(line);
		}
		
		if (iEditable && iModel.isNoteEditable() && iNote != null) {
			P note = new P("note");
			legend.add(note);
			P label = new P("label"); label.setText(MESSAGES.propRoomAvailabilityNote());
			note.add(label);
			P value = new P("value"); value.add(iNote);
			note.add(value);
		} else if (iModel.hasNote()) {
			P note = new P("note");
			legend.add(note);
			P label = new P("label"); label.setText(MESSAGES.propRoomAvailabilityNote());
			note.add(label);
			P value = new P("value"); value.setHTML(iModel.getNote());
			note.add(value);
		}
		
		final List<RoomSharingOption> other = iModel.getAdditionalOptions();
		final List<RoomSharingOption> removable = iModel.getRemovableOptions();
		if (isEditable() && (!other.isEmpty() || !removable.isEmpty())) {
			if (!other.isEmpty() && other.size() <= 20) {
				P separator = new P("row");
				separator.add(new P("blank"));
				P message = new P("other"); message.setHTML(MESSAGES.separatorAddDepartment());
				separator.add(message);
				box.add(separator);
				
				boolean first = true;
				for (final RoomSharingOption option: other) {
					final P line = new P("row");
					
					final P icon = new P("cell", "clickable");
					if (option.getCode() != null && !option.getCode().isEmpty()) icon.setHTML(option.getCode());
					if (first) icon.addStyleName("first");
					first = false;
					icon.getElement().getStyle().setBackgroundColor(option.getColor());
					if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
						icon.getElement().getStyle().setColor("#000000");
					else
						icon.getElement().getStyle().setColor("#ffffff");
					line.add(icon);
					
					final FP title = new FP("title", "editable-title"); title.setHTML(option.getName());
					if (iLast != null) {
						iLast.setNext(title);
						title.setPrevious(iLast);
					}
					iLast = title;
					line.add(title);
					
					Image add = new ImageButton(RESOURCES.add());
					add.setAltText(ARIA.iconAddItem(option.getName()));
					add.addStyleName("remove");
					line.add(add);
					add.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iModel.getOptions().add(option);
							iOption = option;
							setMode(mode, horizontal);
							if (iSelectedTitle != null) iSelectedTitle.setFocus(true);
						}
					});
					
					ClickHandler md = new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iModel.getOptions().add(option);
							iOption = option;
							setMode(mode, horizontal);
							if (iSelectedTitle != null) iSelectedTitle.setFocus(true);
						}
					};
					
					icon.addClickHandler(md);
					title.addClickHandler(md);
					
					box.add(line);
				}
			}
			if (other.size() >= 20 || removable.size() >= 10) {
				final P line = new P("row");
				line.add(new P("blank"));
				P p = new P("button"); 
				if (other.size() >= 20) {
					Button button = new Button(MESSAGES.buttonRoomSharingAddDepartment(), new ClickHandler() {
						
						@Override
						public void onClick(ClickEvent event) {
							final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
							dialog.setText(MESSAGES.dialogAddDepartment());
							dialog.setAnimationEnabled(false);
							dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
								@Override
								public void onClose(CloseEvent<PopupPanel> event) {
									RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
								}
							});
							SimpleForm form = new SimpleForm();
							final TextBox text = new TextBox();
							text.setStyleName("unitime-TextArea"); text.setWidth("300px");
							form.addRow(MESSAGES.propFilter(), text);
							final P box = new P("box");
							final Timer filterTimer = new Timer() {
					            @Override
					            public void run() {
					            	String f = text.getText();
					            	boolean first = true;
					            	for (int i = 0; i < other.size(); i++) {
					            		P line = (P)box.getWidget(i);
					            		line.setVisible(match(f, other.get(i)));
					            		if (first && line.isVisible()) {
					            			((P)line.getWidget(0)).addStyleName("first");
					            			first = false;
					            		} else {
					            			((P)line.getWidget(0)).removeStyleName("first");
					            		}
					            	}
					            }
							};
							text.addKeyUpHandler(new KeyUpHandler() {
								@Override
								public void onKeyUp(KeyUpEvent event) {
									filterTimer.schedule(250);
									if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
										String f = text.getText();
						            	for (RoomSharingOption option: other) {
						            		if (match(f, option)) {
						            			iModel.getOptions().add(option);
						            			iOption = option;
						            			break;
						            		}
						            	}
										dialog.hide();
										setMode(mode, horizontal);
										return;
									} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
										dialog.hide();
											return;
									}
								}
							});
							P legend = new P("legend");
							legend.add(box);
							FP last = null;
							for (final RoomSharingOption option: other) {
								final P line = new P("row");
								
								final P icon = new P("cell", "clickable");
								if (option.getCode() != null && !option.getCode().isEmpty()) icon.setHTML(option.getCode());
								if (box.getWidgetCount() == 0) icon.addStyleName("first");
								icon.getElement().getStyle().setBackgroundColor(option.getColor());
								if (ToolBox.contrast("#000000", option.getColor()) > ToolBox.contrast("#ffffff", option.getColor()))
									icon.getElement().getStyle().setColor("#000000");
								else
									icon.getElement().getStyle().setColor("#ffffff");
								line.add(icon);
								
								final FP title = new FP("title", "editable-title"); title.setHTML(option.getName());
								if (last != null) {
									last.setNext(title);
									title.setPrevious(last);
								}
								last = title;
								title.getElement().setTabIndex(0);
								line.add(title);
								
								ClickHandler md = new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										iModel.getOptions().add(option);
										dialog.hide();
										iOption = option;
										setMode(mode, horizontal);
										if (iSelectedTitle != null) iSelectedTitle.setFocus(true);
									}
								};								
								icon.addClickHandler(md);
								title.addClickHandler(md);
								
								box.add(line);
							}
							
							ScrollPanel w = new ScrollPanel();
							w.addStyleName("scroll");
							w.addStyleName("unitime-RoomSharingWidget");
							w.add(legend);
							if (other.size() >= 12)
								w.setHeight("300px");
							int r = form.addRow(w);
							form.getCellFormatter().addStyleName(r, 0, "unitime-TopLine");
							
							UniTimeHeaderPanel footer = new UniTimeHeaderPanel();
							footer.addButton("add", MESSAGES.buttonAddAllDepartments(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									String f = text.getText();
					            	for (RoomSharingOption option: other) {
					            		if (match(f, option)) {
					            			iModel.getOptions().add(option);
					            			iOption = option;
					            		}
					            	}
									dialog.hide();
									setMode(mode, horizontal);
									if (iSelectedTitle != null) iSelectedTitle.setFocus(true);
								}
							});
							footer.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									dialog.hide();
								}
							});
							form.addBottomRow(footer);
							dialog.setWidget(form);
							
							RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
							dialog.center();
							text.setFocus(true);
						}
					});
					Character ch = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonRoomSharingAddDepartment());
					if (ch != null) button.setAccessKey(ch);
					p.add(button);
				}
				if (removable.size() >= 10) {
					Button button = new Button(MESSAGES.buttonRemoveAll(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iModel.getOptions().removeAll(removable);
							if (removable.contains(iOption))
								iOption = iModel.getDefaultOption();
							setMode(mode, horizontal);
						}
					});
					Character ch = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonRemoveAll());
					if (ch != null) button.setAccessKey(ch);
					button.getElement().getStyle().setMarginLeft(4, Unit.PX);
					p.add(button);
				}
				line.add(p);
				box.add(line);
			}
		}
	}
	
	protected boolean match(String filter, RoomSharingOption option) {
		if (filter == null || filter.isEmpty()) return true;
		fw: for (String fw: filter.split(" ")) {
			if (option.getCode() != null && option.getCode().startsWith(fw)) continue;
			if (option.getName() != null && !option.getName().isEmpty()) {
				for (String w: option.getName().split(" ")) {
					if (w.toLowerCase().startsWith(fw.toLowerCase())) continue fw;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RoomSharingModel> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public RoomSharingModel getValue() {
		return getModel();
	}

	@Override
	public void setValue(RoomSharingModel value) {
		setValue(value, false);
	}

	@Override
	public void setValue(RoomSharingModel value, boolean fireEvents) {
		setModel(value);
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
	
	public boolean removeOption(Long id) {
		if (!isEditable() || iModel == null) return false;
		RoomSharingOption option = iModel.getOption(id);
		if (option != null && option.getId() > 0 && option.isEditable() && iAddedOptions.remove(id) && iModel.countOptions(id) == 0) {
			iModel.getOptions().remove(option);
			iOption = iModel.getDefaultOption();
			setMode(iMode, iHorizontal.getValue());
			return true;
		}
		return false;
	}
	
	public boolean addOption(Long optionId) {
		if (!isEditable() || iModel == null) return false;
		for (RoomSharingOption option : iModel.getAdditionalOptions()) {
			if (option.getId().equals(optionId) && option.isEditable()) {
				iAddedOptions.add(optionId);
				iModel.getOptions().add(option);
				iOption = option;
				setMode(iMode, iHorizontal.getValue());
				return true;
			}
		}
		return false;
	}
	
	public static class FP extends P implements Focusable{
		FP iPrevious, iNext;
		
		public FP(String... styles) {
			super(styles);
			getElement().setTabIndex(0);
			sinkEvents(Event.ONKEYDOWN);
		}
		
		public void setPrevious(FP prev) { iPrevious = prev; }
		public void setNext(FP next) { iNext = next; }
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_SPACE) {
					clickElement(getElement());
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP) {
					if (iPrevious != null) iPrevious.setFocus(true);
					else if (iNext != null) {
						FP next = iNext;
						while (next.iNext != null)
							next = next.iNext;
						next.setFocus(true);
					}
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
					if (iNext != null) iNext.setFocus(true);
					else if (iPrevious != null) {
						FP prev = iPrevious;
						while (prev.iPrevious != null)
							prev = prev.iPrevious;
						prev.setFocus(true);
					}
					event.stopPropagation();
			    	event.preventDefault();
				}
				break;
			}
			super.onBrowserEvent(event);
		}
		
		@Override
		public int getTabIndex() {
			return getElement().getTabIndex();
		}

		@Override
		public void setTabIndex(int index) {
			getElement().setTabIndex(index);
		}

		@Override
		public void setAccessKey(char key) {
			FocusImpl.getFocusImplForWidget().setAccessKey(getElement(), key);
		}

		@Override
		public void setFocus(boolean focused) {
			if (focused)
				getElement().focus();
			else
				getElement().blur();
		}
	}

	public static class Box extends P implements Focusable, HasAllFocusHandlers {
		int iRow = -1, iCol = -1;
		
		public Box() {
			super("box");
			getElement().setTabIndex(0);
			sinkEvents(Event.ONKEYDOWN);	
			addFocusHandler(new FocusHandler() {
				@Override
				public void onFocus(FocusEvent event) {
					P cell = getSelectedCell();
					if (cell != null)
						cell.addStyleName("cursor");
					else
						setCursor(0, 0);
				}
			});
			addBlurHandler(new BlurHandler() {
				@Override
				public void onBlur(BlurEvent event) {
					P cell = getSelectedCell();
					if (cell != null)
						cell.removeStyleName("cursor");
				}
			});
		}
		
		@Override
		public void onBrowserEvent(Event event) {
			switch (DOM.eventGetType(event)) {
			case Event.ONKEYDOWN:
				if (event.getKeyCode() == KeyCodes.KEY_ENTER || event.getKeyCode() == KeyCodes.KEY_SPACE) {
					click();
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_UP) {
					setCursor(iRow - 1, iCol);
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_DOWN) {
					setCursor(iRow + 1, iCol);
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_LEFT) {
					setCursor(iRow, iCol - 1);
					event.stopPropagation();
			    	event.preventDefault();
				}
				if (event.getKeyCode() == KeyCodes.KEY_RIGHT) {
					setCursor(iRow, iCol + 1);
					event.stopPropagation();
			    	event.preventDefault();
				}
		    	break;
			case Event.ONCLICK:
				Element cell = DOM.eventGetTarget(event);
				while (cell != null) {
					if (cell.getPropertyString("tagName").equalsIgnoreCase("div")) break;
					cell = cell.getParentElement();
				}
				if (cell != null) {
					Element row = DOM.getParent(cell);
					setCursor(DOM.getChildIndex(getElement(), row), DOM.getChildIndex(row, cell));
				}
				break;
			}
			super.onBrowserEvent(event);
		}
		
		@Override
		public void clear() {
			super.clear();
			iRow = -1; iCol = -1;
		}
		
		public P getSelectedCell() {
			if (iRow < 0 || getWidgetCount() <= iRow) return null;
			P r = (P)getWidget(iRow);
			if (iCol < 0 || r.getWidgetCount() <= iCol) return null;
			return (P)r.getWidget(iCol);
		}
		
		public void setCursor(int row, int col) {
			P old = getSelectedCell();
			if (old != null) old.removeStyleName("cursor");
			iRow = row % getWidgetCount();
			P r = (P)getWidget(iRow);
			iCol = col % r.getWidgetCount();
			P c = (P)r.getWidget(iCol);
			c.addStyleName("cursor");
		}
		
		@Override
		public int getTabIndex() {
			return getElement().getTabIndex();
		}

		@Override
		public void setTabIndex(int index) {
			getElement().setTabIndex(index);
		}

		@Override
		public void setAccessKey(char key) {
			FocusImpl.getFocusImplForWidget().setAccessKey(getElement(), key);
		}
		
		@Override
		public HandlerRegistration addFocusHandler(FocusHandler handler) {
			return addDomHandler(handler, FocusEvent.getType());
		}

		@Override
		public HandlerRegistration addBlurHandler(BlurHandler handler) {
			return addDomHandler(handler, BlurEvent.getType());
		}

		@Override
		public void setFocus(boolean focused) {
			if (focused)
				getElement().focus();
			else
				getElement().blur();
		}
		
		public void click() {
			P cell = getSelectedCell();
			if (cell != null)
				clickElement(cell.getElement());
		}
		
	}

	public static native void clickElement(Element elem) /*-{
		elem.click();
	}-*/;
}
