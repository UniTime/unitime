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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.user.client.ui.Button;

/**
 * @author Tomas Muller
 */
public class AriaButton extends Button implements HasAriaLabel {
	
	public AriaButton() {
		super();
	}
	
	public AriaButton(String html) {
		super(html);
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html).replace("&nbsp;", " ").replace("&#8209;","-"));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
		ToolBox.setMinWidth(getElement().getStyle(), "75px");
	}
	
	@Override
	public void setHTML(String html) {
		super.setHTML(html);
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
	}

	@Override
	public void setAriaLabel(String text) {
		if (text == null || text.isEmpty())
			Roles.getButtonRole().removeAriaLabelledbyProperty(getElement());
		else
			Roles.getButtonRole().setAriaLabelProperty(getElement(), text);
	}

	@Override
	public String getAriaLabel() {
		return Roles.getButtonRole().getAriaLabelProperty(getElement());
	}
}
