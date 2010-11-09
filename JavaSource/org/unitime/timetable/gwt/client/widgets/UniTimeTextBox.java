/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasFocus;

import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * @author Tomas Muller
 */
public class UniTimeTextBox extends TextBox implements HasFocus {
	
	public UniTimeTextBox() {
		super();
		setStyleName("unitime-TextBox");
	}
	
	public UniTimeTextBox(int maxWidth, int width, ValueBoxBase.TextAlignment align) {
		this();
		setWidth(width + "px");
		setMaxLength(maxWidth);
		if (align != null)
			setAlignment(align);
	}
	
	public UniTimeTextBox(int maxWidth, int width) {
		this(maxWidth, width, null);
	}

	public UniTimeTextBox(int maxWidth, ValueBoxBase.TextAlignment align) {
		this(maxWidth, 10 * maxWidth, align);
	}

	public UniTimeTextBox(int maxWidth) {
		this(maxWidth, 10 * maxWidth, null);
	}

	public UniTimeTextBox(boolean editable) {
		this();
		setReadOnly(!editable);
	}
	
	public UniTimeTextBox(int maxWidth, ValueBoxBase.TextAlignment align, boolean editable) {
		this(maxWidth, align);
		setReadOnly(!editable);
	}

	@Deprecated
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}
	
	@Deprecated
	public boolean isEnabled() {
		return super.isEnabled();
	}

	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		if (readOnly) {
			getElement().getStyle().setBorderColor("transparent");
			getElement().getStyle().setBackgroundColor("transparent");
		} else {
			getElement().getStyle().clearBorderColor();
			getElement().getStyle().clearBackgroundColor();
		}
	}
	
	@Override
	public boolean focus() {
		if (isReadOnly()) return false;
		setFocus(true);
		selectAll();
		return true;
	}
}
