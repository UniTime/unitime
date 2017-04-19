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
import java.util.List;

import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.ListItem;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
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
	private UniTimeHeaderPanel iHeader, iFooter;
	private FilterInterface iFilter;
	private int iFilterHeaderRow = -1, iFilterLastRow = -1;
	private List<Integer> iCollapsibleRows = new ArrayList<Integer>();
	
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
			if (param.getDefaultValue() != null)textarea.setText(param.getDefaultValue());
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
			if (param.getDefaultValue() != null)
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
			if (param.getDefaultValue() != null)
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
			if (param.getDefaultValue() != null)
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
		TextBox text = new TextBox();
		text.setStyleName("unitime-TextBox");
		text.setWidth("400px");
		if (param.getDefaultValue() != null)
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
	
	@Override
	public void setValue(FilterInterface filter, boolean fireEvents) {
		iFilter = filter;
		for (int row = getRowCount() - 1; row > iFilterHeaderRow; row--)
			removeRow(row);
		iCollapsibleRows.clear();
		for (final FilterParameterInterface param: filter.getParameters()) {
			Widget w = getWidget(param);
			int row = addRow(param.getLabel(), w);
			if (param.isCollapsible()) iCollapsibleRows.add(row);
			iFilterLastRow = row;
			if (iHeader.isCollapsible() != null && !iHeader.isCollapsible() && param.isCollapsible())
				getRowFormatter().setVisible(iFilterLastRow, false);
		}
		if (iCollapsibleRows.isEmpty())
			iHeader.setCollapsible(null);
		else if (iHeader.isCollapsible() == null)
			iHeader.setCollapsible(false);
		addBottomRow(iFooter);
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
}
