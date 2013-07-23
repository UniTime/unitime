/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.rooms;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingDisplayMode;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingModel;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

public class RoomSharingWidget extends Composite implements HasValue<RoomSharingModel> {
	private static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private AbsolutePanel iPanel;
	private ListBox iModeSelection;
	private RoomSharingDisplayMode iMode = null;
	private CheckBox iHorizontal;
	private RoomSharingOption iOption = null;
	private P iSelectedIcon = null, iSelectedTitle = null;
	private RoomSharingModel iModel;
	protected boolean iEditable = true;
	private TextArea iNote = null;
	
	public RoomSharingWidget(boolean editable) {
		iEditable = editable;
		VerticalPanel container = new VerticalPanel();
		
		HorizontalPanel hp = new HorizontalPanel();
		
		iModeSelection = new ListBox();
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
		
		if (iEditable) {
			iNote = new TextArea();
			iNote.setStyleName("unitime-TextArea");
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
	
	public AbsolutePanel getPanel() { return iPanel; }
	
	public boolean isEditable() {
		return iEditable && iModel != null && iModel.isEditable();
	}
	
	public boolean isEditable(RoomSharingOption option) {
		return isEditable() && option.isEditable();
	}
	
	public boolean isEditable(int day, int slot) {
		return isEditable() && iModel.isEditable(day, slot, iMode.getStep());
	}
	
	public RoomSharingModel getModel() {
		return iModel;
	}
	
	public void setModel(RoomSharingModel model) {
		iModel = model;
		
		if (iEditable && iModel.isNoteEditable())
			iNote.setValue(iModel.hasNote() ? iModel.getNote() : "");
		
		iModeSelection.clear();
		for (RoomSharingDisplayMode mode: iModel.getModes())
			iModeSelection.addItem(mode.getName());
		
		iModeSelection.setSelectedIndex(0);
		
		String cookie = Cookies.getCookie("UniTime:RoomSharing");
		if (cookie != null) {
			try {
				for (int i = 0; i < iModel.getModes().size(); i++)
					if (cookie.startsWith(iModel.getModes().get(i).toString())) {
						iModeSelection.setSelectedIndex(i); break;
					}
				iHorizontal.setValue(cookie.endsWith("|1"));
			} catch (Exception e) {}
		} else {
			iHorizontal.setValue(model.isDefaultHorizontal());
			iModeSelection.setSelectedIndex(iModel.getDefaultMode());
		}
		
		iOption = iModel.getDefaultOption();
		if (iOption == null) iOption = iModel.getOptions().get(0);

		setMode(iModel.getModes().get(iModeSelection.getSelectedIndex()), iHorizontal.getValue());
	}
	
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
	
	private class Cell extends P {
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
				setHTML(option.getCode() == null ? "" : option.getCode());
				setTitle(CONSTANTS.longDays()[day] + " " + slot2short(slot) + " - " + slot2short(slot + iMode.getStep()) + ": " + option.getName());
			}
			if (isEditable(day, slot))
				addMouseDownHandler(new MouseDownHandler() {
					@Override
					public void onMouseDown(MouseDownEvent event) {
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
				setHTML(option.getCode() == null ? "" : option.getCode());
				setTitle(CONSTANTS.longDays()[iDay] + " " + slot2short(iSlot) + " - " + slot2short(iSlot + iMode.getStep()) + ": " + option.getName());
			}
			ValueChangeEvent.fire(RoomSharingWidget.this, getValue());
		}
	}
	
	public void setMode(final RoomSharingDisplayMode mode, final boolean horizontal) {
		iMode = mode;
		Cookies.setCookie("UniTime:RoomSharing", mode.toString() + "|" + (horizontal ? "1" : "0"));
		iPanel.clear();
		
		if (horizontal) {
			for (int page = 0; iMode.getFirstSlot() + 24 * page * iMode.getStep() < iMode.getLastSlot(); page++) {
				P table = new P("table");
				iPanel.add(table);
				P box = new P("box");
				table.add(box);
				P header = new P("row");
				box.add(header);
				P corner = new P("cell", "corner", isEditable() ? "clickable" : null);
				corner.setHTML(MESSAGES.roomSharingCorner());
				header.add(corner);
				
				int first = iMode.getFirstSlot() + 24 * page * iMode.getStep();
				int last = Math.min(first + 24 * iMode.getStep() * (page + 1), iMode.getLastSlot());
				
				final List<List<Cell>> thisTime = new ArrayList<List<Cell>>();
				for (int slot = first; slot < last; slot += iMode.getStep()) {
					P p = new P("cell", "time", isEditable() ? "clickable" : null); p.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iMode.getStep())));
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
				for (int day = iMode.getFirstDay(); day <= iMode.getLastDay(); day++) {
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
			for (int day = iMode.getFirstDay(); day <= iMode.getLastDay(); day++) {
				P p = new P("cell", "time", isEditable() ? "clickable" : null); p.setHTML(CONSTANTS.days()[day % 7]);
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
			for (int slot = iMode.getFirstSlot(); slot < iMode.getLastSlot(); slot += iMode.getStep()) {
				P line = new P("row");
				box.add(line);
				P d = new P("cell", "day", isEditable() ? "clickable" : null);
				d.setHTML(MESSAGES.roomSharingTimeHeader(slot2short(slot), slot2short(slot + iMode.getStep())));
				line.add(d);
				final List<Cell> thisSlot = new ArrayList<Cell>();
				for (int day = iMode.getFirstDay(); day <= iMode.getLastDay(); day++) {
					Cell p = new Cell(day, slot);
					line.add(p);
					thisSlot.add(p);
					thisPage.add(p);
					thisDay.get(day - iMode.getFirstDay()).add(p);
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
			line.add(icon);
			
			final P title = new P("title", isEditable(option) ? "editable-title" : null); title.setHTML(option.getName());
			line.add(title);
			
			if (option.getId() >= 0 && isEditable(option)) {
				Image remove = new Image(RESOURCES.delete());
				remove.addStyleName("remove");
				line.add(remove);
				remove.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (option.equals(iOption)) iOption = iModel.getDefaultOption();
						box.remove(line);
						iModel.getOptions().remove(option);
						setMode(mode, horizontal);
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
					}
				};
				
				icon.addMouseDownHandler(md);
				title.addMouseDownHandler(md);
			}
			
			box.add(line);
		}
		
		if (iEditable && iModel.isNoteEditable()) {
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
					line.add(icon);
					
					final P title = new P("title", "editable-title"); title.setHTML(option.getName());
					line.add(title);
					
					
					Image add = new Image(RESOURCES.add());
					add.addStyleName("remove");
					line.add(add);
					add.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iModel.getOptions().add(option);
							iOption = option;
							setMode(mode, horizontal);
						}
					});
					
					MouseDownHandler md = new MouseDownHandler() {
						@Override
						public void onMouseDown(MouseDownEvent event) {
							iModel.getOptions().add(option);
							iOption = option;
							setMode(mode, horizontal);
						}
					};
					
					icon.addMouseDownHandler(md);
					title.addMouseDownHandler(md);
					
					box.add(line);
				}
			}
			if (other.size() >= 20 || removable.size() >= 10) {
				final P line = new P("row");
				line.add(new P("blank"));
				P p = new P("button"); 
				if (other.size() >= 20) {
					Button button = new Button(MESSAGES.buttonAddDepartment(), new ClickHandler() {
						
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
							P legend = new P("legend");
							P box = new P("box");
							legend.add(box);
							for (final RoomSharingOption option: other) {
								final P line = new P("row");
								
								final P icon = new P("cell", "clickable");
								if (option.getCode() != null && !option.getCode().isEmpty()) icon.setHTML(option.getCode());
								if (box.getWidgetCount() == 0) icon.addStyleName("first");
								icon.getElement().getStyle().setBackgroundColor(option.getColor());
								line.add(icon);
								
								final P title = new P("title", "editable-title"); title.setHTML(option.getName());
								line.add(title);
								
								MouseDownHandler md = new MouseDownHandler() {
									@Override
									public void onMouseDown(MouseDownEvent event) {
										iModel.getOptions().add(option);
										dialog.hide();
										iOption = option;
										setMode(mode, horizontal);
									}
								};
								
								icon.addMouseDownHandler(md);
								title.addMouseDownHandler(md);
								
								box.add(line);
							}
							
							final P line = new P("row");
							line.add(new P("blank"));
							Button button = new Button(MESSAGES.buttonAddAllDepartments(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent event) {
									iModel.getOptions().addAll(other);
									dialog.hide();
									setMode(mode, horizontal);
								}
							});
							P p = new P("button"); p.add(button);
							line.add(p);
							box.add(line);
							
							ScrollPanel w = new ScrollPanel();
							w.addStyleName("scroll");
							w.addStyleName("unitime-RoomSharingWidget");
							w.add(legend);
							if (other.size() >= 12)
								w.setHeight("300px");
							dialog.setWidget(w);
							
							RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
							dialog.center();
						}
					});
					Character ch = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonAddDepartment());
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
}
