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
package org.unitime.timetable.gwt.client.solver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.rooms.RoomFilterBox;
import org.unitime.timetable.gwt.client.widgets.CourseNumbersSuggestBox;
import org.unitime.timetable.gwt.client.widgets.DayCodeSelector;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class PageFilter extends SimpleForm implements HasValue<FilterInterface> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final CourseMessages MSG = GWT.create(CourseMessages.class);
	private UniTimeHeaderPanel iHeader, iFooter;
	private FilterInterface iFilter;
	private int iFilterHeaderRow = -1, iFilterLastRow = -1;
	private List<Integer> iCollapsibleRows = new ArrayList<Integer>();
	private Map<String, Widget> iWidgets = new HashMap<String, Widget>();
	private Command iSubmitCommand;
	
	public PageFilter() {
		addStyleName("unitime-PageFilter");
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectFilter());
		iHeader.setCollapsible(true);
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				for (int row: iCollapsibleRows) {
					getRowFormatter().setVisible(row, event.getValue());
				}
			}
		});
		iFilterHeaderRow = addHeaderRow(iHeader);
		iFilterLastRow = iFilterHeaderRow;
		iFooter = iHeader.clonePanel(null);
	}
	
	public UniTimeHeaderPanel getHeader() { return iHeader; }
	public UniTimeHeaderPanel getFooter() { return iFooter; }
	
	public Command getSubmitCommand() { return iSubmitCommand; }
	public void setSubmitCommand(Command command) { iSubmitCommand = command; }
	
	protected Widget getWidget(final FilterParameterInterface param) {
		if (param.hasOptions()) {
			final ListBox list = new ListBox();
			list.setMultipleSelect(param.isMultiSelect());
			if (!param.isMultiSelect()) list.addItem(MESSAGES.itemSelect());
			for (ListItem item: param.getOptions()) {
				list.addItem(item.getText(), item.getValue());
				if (param.isMultiSelect())
					list.setItemSelected(list.getItemCount() - 1, param.isDefaultItem(item));
				else if (param.isDefaultItem(item))
					list.setSelectedIndex(list.getItemCount() - 1);
			}
			if (list.isMultipleSelect())
				list.getElement().setAttribute("size", String.valueOf(Math.min(param.getMaxLinesToShow(), param.getOptions().size())));
			list.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					if (param.isMultiSelect()) {
						String value = "";
						for (int i = 0; i < list.getItemCount(); i++)
							if (list.isItemSelected(i))
								value += (value.isEmpty() ? "" : ",") + list.getValue(i);
						param.setValue(value);
					} else {
						if (list.getSelectedIndex() <= 0)
							param.setValue(null);
						else
							param.setValue(list.getValue(list.getSelectedIndex()));
					}
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return list;
		}
		if ("roomFilter".equals(param.getName())) {
			AcademicSessionProvider session = new AcademicSessionProvider() {
				@Override
				public Long getAcademicSessionId() {
					return param.getSessionId();
				}
				@Override
				public String getAcademicSessionName() {
					return "Current Session";
				}
				@Override
				public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
				}
				@Override
				public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
				}
				@Override
				public AcademicSessionInfo getAcademicSessionInfo() {
					return null;
				}
			};
			RoomFilterBox rf = new RoomFilterBox(session);
			if (param.hasDefaultValue())
				rf.setValue(param.getDefaultValue());
			rf.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return rf;
		}
		if ("boolean".equalsIgnoreCase(param.getType())) {
			CheckBox ch = new CheckBox();
			ch.setValue("1".equalsIgnoreCase(param.getDefaultValue()));
			ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue() ? "1" : "0");
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return ch;
		}
		if ("file".equalsIgnoreCase(param.getType())) {
			UniTimeFileUpload upload = new UniTimeFileUpload(); upload.reset();
			upload.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return upload;
		}
		if ("textarea".equalsIgnoreCase(param.getType())) {
			TextArea textarea = new TextArea();
			textarea.setStyleName("unitime-TextArea");
			textarea.setVisibleLines(5);
			textarea.setCharacterWidth(80);
			if (param.hasDefaultValue())
				textarea.setText(param.getDefaultValue());
			textarea.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return textarea;
		}
		if ("integer".equalsIgnoreCase(param.getType()) || "int".equalsIgnoreCase(param.getType()) || "long".equalsIgnoreCase(param.getType()) || "short".equalsIgnoreCase(param.getType()) || "byte".equalsIgnoreCase(param.getType())) {
			NumberBox text = new NumberBox();
			text.setDecimal(false); text.setNegative(true);
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			text.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return text;
		}
		if ("number".equalsIgnoreCase(param.getType()) || "float".equalsIgnoreCase(param.getType()) || "double".equalsIgnoreCase(param.getType())) {
			NumberBox text = new NumberBox();
			text.setDecimal(true); text.setNegative(true);
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			text.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return text;
		}
		if ("date".equalsIgnoreCase(param.getType())) {
			SingleDateSelector text = new SingleDateSelector();
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			final DateTimeFormat format = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
			text.addValueChangeHandler(new ValueChangeHandler<Date>() {
				@Override
				public void onValueChange(ValueChangeEvent<Date> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(format.format(event.getValue()));
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return text;
		}
		if ("time".equalsIgnoreCase(param.getType())) {
			TimeSelector text = new TimeSelector((TimeSelector)iWidgets.get("startTime"));
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			text.addValueChangeHandler(new ValueChangeHandler<Integer>() {
				@Override
				public void onValueChange(ValueChangeEvent<Integer> event) {
					if (event.getValue() == null)
						param.setValue("");
					else
						param.setValue(event.getValue().toString());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return text;
		}
		if ("dayCode".equalsIgnoreCase(param.getType())) {
			DayCodeSelector text = new DayCodeSelector();
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			text.addValueChangeHandler(new ValueChangeHandler<Integer>() {
				@Override
				public void onValueChange(ValueChangeEvent<Integer> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue().toString());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return text;
		}
		if ("courseNumber".equalsIgnoreCase(param.getType())) {
			AriaTextBox text = new AriaTextBox();
			text.setWidth("200px");
			text.getElement().setAttribute("autocomplete", "off");
			text.setTitle(MSG.tooltipCourseNumber());
			if (param.hasDefaultValue())
				text.setText(param.getDefaultValue());
			AriaSuggestBox box = new AriaSuggestBox(text, new CourseNumbersSuggestBox(param.getConfig()).withFilter(iFilter));
			text.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> event) {
					if (event.getValue() == null)
						param.setValue(null);
					else
						param.setValue(event.getValue());
					ValueChangeEvent.fire(PageFilter.this, iFilter);
				}
			});
			return box;
		}
		TextBox text = new TextBox();
		text.setStyleName("unitime-TextBox");
		text.setWidth("400px");
		if (param.hasDefaultValue())
			text.setText(param.getDefaultValue());
		text.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() == null)
					param.setValue(null);
				else
					param.setValue(event.getValue());
				ValueChangeEvent.fire(PageFilter.this, iFilter);
			}
		});
		return text;
	}
	
	public FilterInterface getValue() {
		return iFilter;
	}
	
	public void setValue(FilterInterface filter) {
		setValue(filter, true);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<FilterInterface> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	public Widget getFilterWidget(String name) {
		return iWidgets.get(name);
	}
	
	@Override
	public void setValue(FilterInterface filter, boolean fireEvents) {
		iFilter = filter;
		iWidgets.clear();
		for (int row = getRowCount() - 1; row > iFilterHeaderRow; row--)
			removeRow(row);
		iCollapsibleRows.clear();
		Set<String> parents = new HashSet<String>();
		String lastLabel = null;
		FilterParameterInterface previous = null;
		for (final FilterParameterInterface param: filter.getParameters()) {
			String value = Location.getParameter(param.getName());
			if (value != null) param.setDefaultValue(value);
			Widget w = getWidget(param);
			if (param.isEnterToSubmit() && w instanceof HasKeyUpHandlers)
				((HasKeyUpHandlers)w).addKeyUpHandler(new KeyUpHandler() {
					@Override
					public void onKeyUp(KeyUpEvent event) {
						if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && iSubmitCommand != null) {
							Scheduler.get().scheduleDeferred(new ScheduledCommand() {
								@Override
								public void execute() {
									iSubmitCommand.execute();
								}
							});
						}
					}
				});
						
			w.getElement().setId(param.getName());
			iWidgets.put(param.getName(), w);
			int row;
			String label = param.getLabel();
			if (label.equals(lastLabel)) {
				label = "";
			} else {
				lastLabel = label;
			}
			P panel = new P("panel");
			if (param.hasSuffix() || param.hasPrefix()) {
				if (param.hasPrefix()) {
					Label prefix = new Label(param.getPrefix()); prefix.addStyleName("prefix");
					panel.add(prefix);
				}
			}
			panel.add(w);
			if (param.hasSuffix()) {
				if (w instanceof CheckBox) {
					((CheckBox)w).setText(param.getSuffix());
				} else {
					Label suffix = new Label(param.getSuffix()); suffix.addStyleName("suffix");
					panel.add(suffix);
				}
			}
			if (label.isEmpty() && param.isComposite() && previous != null && previous.isComposite()) {
				row = iFilterLastRow;
				Widget widget = getWidget(row, 1);
				P prev = null;
				if (widget instanceof P && ((P)widget).getStyleName().contains("panel")) {
					prev = (P) widget;
				} else {
					prev = new P("panel");
					prev.add(widget);
				}
				P composite = new P("composite");
				composite.add(prev);
				composite.add(panel);
				setWidget(row, 1, composite);
			} else if (panel.getWidgetCount() == 1)
				row = addRow(label, w);
			else
				row = addRow(label, panel);
			if (param.getParent() != null) {
				getWidget(row, 1).getElement().getStyle().setPaddingLeft(20, Unit.PX);
				parents.add(param.getParent());
			}
			if (param.isCollapsible()) iCollapsibleRows.add(row);
			iFilterLastRow = row;
			previous = param;
			if (iHeader.isCollapsible() != null && !iHeader.isCollapsible() && param.isCollapsible())
				getRowFormatter().setVisible(iFilterLastRow, false);
		}
		if (iCollapsibleRows.isEmpty())
			iHeader.setCollapsible(null);
		else if (iHeader.isCollapsible() == null)
			iHeader.setCollapsible(false);
		addBottomRow(iFooter);
		for (final String parent: parents) {
			Widget w = iWidgets.get(parent);
			if (w != null && w instanceof CheckBox) {
				((CheckBox)w).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> e) {
						for (FilterParameterInterface p: iFilter.getParameters()) {
							if (parent.equals(p.getParent()))
								setValue(p.getName(), e.getValue() ? "1" : "0");
						}
					}
				}); 
			}
		}
		if (fireEvents)
			ValueChangeEvent.fire(this, iFilter);
	}
	
	public String getQuery() {
		String query = "";
		for (FilterParameterInterface param: iFilter.getParameters()) {
			String value = param.getValue();
			if (value != null)
				query += "&" + param.getName() + "=" + URL.encodeQueryString(value);
		}
		return query;
	}
	
	public String getFullQuery() {
		String query = "";
		for (FilterParameterInterface param: iFilter.getParameters()) {
			String value = param.getValue();
			if (value == null) value = param.getDefaultValue();
			if (value != null)
				query += "&" + param.getName() + "=" + URL.encodeQueryString(value);
		}
		return query;
	}
	
	public void setValue(String name, String value) {
		Widget w = iWidgets.get(name);
		FilterParameterInterface param = iFilter.getParameter(name);
		if (param != null) param.setValue(value);
		if (w == null) return;
		if (w instanceof CheckBox) {
			((CheckBox)w).setValue("1".equals(value));
		} else if (w instanceof ListBox) {
			ListBox list = (ListBox)w;
			if (param != null && param.isMultiSelect()) {
				for (int i = 0; i < list.getItemCount(); i++) {
					boolean selected = false;
					for (String val: value.split(","))
						if (val.equalsIgnoreCase(list.getValue(i))) selected = true;
					list.setItemSelected(i, selected);
				}
			} else {
				for (int i = 0; i < list.getItemCount(); i++) {
					if (value.equalsIgnoreCase(list.getValue(i))) {
						list.setSelectedIndex(i); break;
					}
				}
			}
		} else if (w instanceof HasText) {
			((HasText)w).setText(value);
		}
	}
	
	public void setQuery(String query, boolean fireEvents) {
		if (query != null) {
			Map<String, String> params = new HashMap<String, String>();
			for (String pair: query.split("\\&")) {
				int idx = pair.indexOf('=');
				if (idx >= 0)
					params.put(pair.substring(0, idx), URL.decodeQueryString(pair.substring(idx + 1)));
			}
			for (FilterParameterInterface param: iFilter.getParameters()) {
				String value = params.get(param.getName());
				if (value != null)
					setValue(param.getName(), value);
				else if (param.getValue() != null && param.getDefaultValue() != null) {
					setValue(param.getName(), param.getDefaultValue());
				}
			}
		}
	}
}
