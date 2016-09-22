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

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class FlowForm extends P {

	public FlowForm() {
		super("unitime-FlowForm");
		addStyleName("unitime-NotPrintableBottomLine");
	}
	
	public int addHeaderRow(Widget widget) {
		P row = new P(DOM.createSpan(), "row-cell", "unitime-MainTableHeader");
		row.add(widget);
		add(row);
		return getWidgetCount() - 1;
	}
	
	public int addHeaderRow(String text) {
		return addHeaderRow(new Label(text, false));
	}

	public int addRow(Widget widget) {
		P row = new P(DOM.createSpan(), "row-cell");
		row.add(widget);
		add(row);
		return getWidgetCount() - 1;
	}
	
	protected int addBottomRow(Widget widget, boolean printable) {
		P row = new P(DOM.createSpan(), "row-cell", "unitime-MainTableBottomHeader", "unitime-TopLine");
		if (!printable)
			row.addStyleName("unitime-NoPrint");
		removeStyleName("unitime-NotPrintableBottomLine");
		row.add(widget);
		add(row);
		return getWidgetCount() - 1;
	}
	
	public int addBottomRow(Widget widget) {
		return addBottomRow(widget, true);
	}

	public int addNotPrintableBottomRow(Widget widget) {
		return addBottomRow(widget, false);
	}
	
	public int addRow(String text, Widget... widgets) {
		return addRow(new Label(text), widgets);
	}

	public int addRow(Widget header, Widget... widgets) {
		if (header.getElement().getId() == null || header.getElement().getId().isEmpty())
			header.getElement().setId(DOM.createUniqueId());
		P head = new P(DOM.createSpan(), "header-cell");
		head.add(header);
		add(head);
		for (Widget widget: widgets) {
			P body = new P(DOM.createSpan(), "content-cell");
			add(body);
			if (widget instanceof UniTimeTable) {
				ScrollPanel scroll = new ScrollPanel(widget);
				scroll.addStyleName("scroll");
				body.add(scroll);
			} else {
				body.add(widget);
			}
		}
		if (widgets.length > 0) {
			if (widgets[0] instanceof UniTimeWidget)
				Roles.getTextboxRole().setAriaLabelledbyProperty(((UniTimeWidget)widgets[0]).getWidget().getElement(), Id.of(header.getElement()));
			else
				Roles.getTextboxRole().setAriaLabelledbyProperty(widgets[0].getElement(), Id.of(header.getElement()));			
		}
		return getWidgetCount() - widgets.length;
	}
	
	public int getCell(String text) {
		for (int i = 0; i < getWidgetCount(); i++) {
			Widget w = getWidget(i);
			if ("header-cell".equals(w.getStylePrimaryName()) && w instanceof HasText && text.equals(((HasText)w).getText())) {
				return 1 + i;
			}
		}
		return -1;
	}
	
	public int getCellForWidget(Widget w) {
		for (Element e = w.getElement(); e != null; e = DOM.getParent(e)) {
			if (e.getPropertyString("tagName").equalsIgnoreCase("span")) {
				if (DOM.getParent(e) == getElement())
					return DOM.getChildIndex(getElement(), e);
			}
			if (e == getElement()) { return -1; }
		}
		return -1;
	}
	
	public void setVisible(int cell, boolean visible) {
		if (cell < 0 || cell >= getWidgetCount()) return;
		getWidget(cell).setVisible(false);
		if ("content-cell".equals(getWidget(cell).getStylePrimaryName()))
			getWidget(cell - 1).setVisible(false);
	}
}
