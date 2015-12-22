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
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author Tomas Muller
 */
public class MultiSelect<T> extends ScrollPanel implements HasValue<List<T>> {
	private P iPanel = null;
	private List<Item> iItems = new ArrayList<Item>();
	
	public MultiSelect() {
		setStyleName("unitime-MultiSelect");
		iPanel = new P("content");
		setWidget(iPanel);
	}
	
	public void addItem(T id, String text, boolean asHTML) {
		Item item = new Item(id, text, asHTML);
		iItems.add(item);
		iPanel.add(item);
	}
	
	public void addItem(T id, String text) {
		addItem(id, text, false);
	}
	
	public boolean isSelected(T id) {
		for (Item item: iItems) {
			if (item.getId().equals(id))
				return item.getValue();
		}
		return false;
	}
	
	public void setSelected(T id, boolean value) {
		for (Item item: iItems) {
			if (item.getId().equals(id))
				item.setValue(value);
		}
	}
	
	public List<T> getSelectedIds() {
		List<T> ret = new ArrayList<T>();
		for (Item item: iItems) {
			if (item.getValue())
				ret.add(item.getId());
		}
		return ret;
	}
	
	public List<Item> getSelectedItems() {
		List<Item> ret = new ArrayList<Item>();
		for (Item item: iItems) {
			if (item.getValue())
				ret.add(item);
		}
		return ret;
	}
	
	public List<Item> getItems() {
		return iItems;
	}
	
	class Item extends CheckBox implements HasClickHandlers {
		private T iId;
		
		Item(T id, String name, boolean asHTML) {
			super(name, asHTML);
			iId = id;
			addStyleName("item");
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					ValueChangeEvent.fire(MultiSelect.this, MultiSelect.this.getValue());
				}
			});
		}
		
		public T getId() { return iId; }
		public void setId(T id) { iId = id; }
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<T>> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public List<T> getValue() {
		return getSelectedIds();
	}

	@Override
	public void setValue(List<T> value) {
		setValue(value, false);
	}

	@Override
	public void setValue(List<T> value, boolean fireEvents) {
		for (Item item: iItems)
			item.setValue(value.contains(item.getId()));
		if (fireEvents)
			ValueChangeEvent.fire(this, getValue());
	}
}
