/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;

public class OpenCloseSectionImage extends Image implements HasValueChangeHandlers<Boolean>, HasValue<Boolean> {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private boolean iValue = true;

	public OpenCloseSectionImage(boolean opened) {
		super(RESOURCES.treeOpen());
		getElement().getStyle().setCursor(Cursor.POINTER);
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setValue(!getValue(), true);
			}
		});
	}
		
	@Override
	public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Boolean> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Boolean getValue() {
		return iValue;
	}

	@Override
	public void setValue(Boolean value) {
		setValue(value, false);
	}
	
	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		if (value == null) return;
		iValue = value;
		setResource(iValue ? RESOURCES.treeOpen() : RESOURCES.treeClosed());
		if (fireEvents)
			ValueChangeEvent.fire(this, iValue);
	}
}
