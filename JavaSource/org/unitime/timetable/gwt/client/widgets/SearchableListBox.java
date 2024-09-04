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
package org.unitime.timetable.gwt.client.widgets;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

/**
 * @author Tomas Muller
 */
public class SearchableListBox extends Composite {
	private ListBox iListBox;
	private AriaSuggestBox iSuggestBox;
	
	public SearchableListBox() {
		iSuggestBox = new AriaSuggestBox(new SearchableListBoxOracle());
		initWidget(iSuggestBox);
		iSuggestBox.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<Suggestion> event) {
				if (event.getSelectedItem() != null)
					iListBox.setSelectedIndex(((ListItem)event.getSelectedItem()).getIndex());
				else
					iListBox.setSelectedIndex(0);
				ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), iListBox);
			}
		});
		iSuggestBox.getTextBox().addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iSuggestBox.showSuggestionList();
			}
		});
		iSuggestBox.getTextBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				iSuggestBox.setText(iListBox.getSelectedItemText());
			}
		});
	}
	
	public SearchableListBox(ListBox box) {
		this();
		setListBox(box);
		iSuggestBox.getElement().getStyle().clearWidth();
	}
	
	public void setListBox(ListBox box) {
		iListBox = box;
		iSuggestBox.getElement().getStyle().setWidth(iListBox.getOffsetWidth(), Unit.PX);
		iListBox.setVisible(false);
		iListBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iSuggestBox.setValue(iListBox.getSelectedItemText());
			}
		});
		iSuggestBox.setValue(iListBox.getSelectedItemText());
	}
	
	ListItem item(int index) {
		if (index < 0 || index > iListBox.getItemCount()) return null;
		return new ListItem(index, iListBox.getValue(index), iListBox.getItemText(index));
	}
	
	public void insert(final RootPanel panel) {
		setListBox(ListBox.wrap(panel.getElement().getFirstChildElement()));
		panel.add(this);
		final String onchange = panel.getElement().getFirstChildElement().getAttribute("onchange");
		if (onchange != null)
			iListBox.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					ToolBox.eval(onchange);
				}
			});
	}
	
	public static class ListItem implements IsSerializable, Comparable<ListItem>, Suggestion {
		private String iValue, iText;
		private int iIndex;
		public ListItem() {}
		public ListItem(int index, String value, String text) {
			iIndex = index;
			iValue = value; iText = text;
		}
		public int getIndex() { return iIndex; }
		public String getValue() { return iValue; }
		public String getText() { return iText; }
		@Override
		public String toString() { return iText; }
		@Override
		public int compareTo(ListItem o) {
			return getText().compareTo(o.getText());
		}
		@Override
		public int hashCode() { return getText().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ListItem)) return false;
			ListItem i = (ListItem)o;
			return getValue().equals(i.getValue());
		}
		@Override
		public String getDisplayString() { return getText(); }
		@Override
		public String getReplacementString() { return getText(); }
	}
	
	class SearchableListBoxOracle extends SuggestOracle {
		@Override
		public void requestSuggestions(Request request, Callback callback) {
			if (iListBox.getSelectedItemText().equals(request.getQuery())) {
				requestDefaultSuggestions(request, callback);
				return;
			}
			List<ListItem> items = new ArrayList<ListItem>();
			for (int i = 0; i < iListBox.getItemCount(); i++) {
				if (iListBox.getItemText(i).toLowerCase().contains(request.getQuery().toLowerCase())) {
					items.add(item(i));
				}
			}
			Response response = new Response(items);
			callback.onSuggestionsReady(request, response);
		}
		
		@Override
		public void requestDefaultSuggestions(Request request, Callback callback) {
			List<ListItem> items = new ArrayList<ListItem>();
			for (int i = 0; i < iListBox.getItemCount(); i++) {
				items.add(item(i));
			}
			Response response = new Response(items);
			callback.onSuggestionsReady(request, response);
		}
	}
}
