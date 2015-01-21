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
