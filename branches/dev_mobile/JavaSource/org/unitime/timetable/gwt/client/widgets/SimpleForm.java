/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SimpleForm extends FlexTable {
	private int iColSpan;
	
	public SimpleForm(int colSpan) {
		super();
		iColSpan = colSpan;
		setStylePrimaryName("unitime-MainTable");
		addStyleName("unitime-NotPrintableBottomLine");
		setCellPadding(2);
		setCellSpacing(0);
		setWidth("100%");
	}
	
	public SimpleForm() {
		this(2);
	}
	
	public int addHeaderRow(Widget widget) {
		int row = getRowCount();
		getFlexCellFormatter().setColSpan(row, 0, iColSpan);
		getFlexCellFormatter().setStyleName(row, 0, "unitime-MainTableHeader");
		getRowFormatter().setStyleName(row, "unitime-MainTableHeaderRow");
		setWidget(row, 0, widget);
		return row;
	}
	
	public int addHeaderRow(String text) {
		return addHeaderRow(new Label(text, false));
	}

	public int addRow(Widget widget) {
		int row = getRowCount();
		getFlexCellFormatter().setColSpan(row, 0, iColSpan);
		setWidget(row, 0, widget);
		return row;
	}
	
	protected int addBottomRow(Widget widget, boolean printable) {
		int row = getRowCount();
		getFlexCellFormatter().setColSpan(row, 0, iColSpan);
		getFlexCellFormatter().setStyleName(row, 0, "unitime-MainTableBottomHeader");
		if (!printable)
			getFlexCellFormatter().addStyleName(row, 0, "unitime-NoPrint");
		getFlexCellFormatter().addStyleName(row, 0, "unitime-TopLine");
		removeStyleName("unitime-NotPrintableBottomLine");
		setWidget(row, 0, widget);
		return row;
	}
	
	public int addBottomRow(Widget widget) {
		return addBottomRow(widget, true);
	}

	public int addNotPrintableBottomRow(Widget widget) {
		return addBottomRow(widget, false);
	}
	
	public int addRow(String text, Widget widget) {
		return addRow(text, widget, iColSpan - 1);
	}

	public int addRow(String text, Widget widget, int colspan) {
		return addRow(new Label(text, false), widget, colspan);
	}
	
	public int addRow(Widget header, Widget widget) {
		return addRow(header, widget, iColSpan - 1);
	}
	
	public int addRow(Widget header, Widget widget, int colSpan) {
		int row = getRowCount();
		setWidget(row, 0, header);
		setWidget(row, 1, widget);
		if (colSpan != 1)
			getFlexCellFormatter().setColSpan(row, 1, colSpan);
		if (header.getElement().getId() == null || header.getElement().getId().isEmpty())
			header.getElement().setId(DOM.createUniqueId());
		if (widget instanceof UniTimeWidget)
			Roles.getTextboxRole().setAriaLabelledbyProperty(((UniTimeWidget)widget).getWidget().getElement(), Id.of(header.getElement()));
		else
			Roles.getTextboxRole().setAriaLabelledbyProperty(widget.getElement(), Id.of(header.getElement()));
		return row;
	}
	
	public int getRow(String text) {
		for (int row = 0; row < getRowCount(); row ++) {
			if (getCellCount(row) > 1) {
				Widget w = getWidget(row, 0);
				if (w instanceof HasText && text.equals(((HasText)w).getText())) return row;
			}
		}
		return -1;
	}
	
	@Override
	public void clear() {
		for (int row = getRowCount() - 1; row >= 0; row--)
			removeRow(row);
	}
	
}
