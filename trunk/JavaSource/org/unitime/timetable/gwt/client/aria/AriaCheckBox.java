/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.aria;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;

/**
 * @author Tomas Muller
 */
public class AriaCheckBox extends CheckBox implements HasAriaLabel {
	Element iAriaLabel;
	
	public AriaCheckBox() {
		this(DOM.createInputCheck());
	}
	
	public AriaCheckBox(Element elem) {
		super(elem);
		
		iAriaLabel = DOM.createLabel();
		iAriaLabel.setId(DOM.createUniqueId());
		iAriaLabel.setClassName("unitime-AriaLabel");
		DOM.appendChild(getElement(), iAriaLabel);
		Roles.getCheckboxRole().setAriaLabelledbyProperty(elem, Id.of(iAriaLabel));
	}
	
	@Override
	public void setAriaLabel(String text) {
		iAriaLabel.setInnerText(text == null ? "" : text);
	}
	
	@Override
	public String getAriaLabel() {
		return iAriaLabel.getInnerText();
	}
}
