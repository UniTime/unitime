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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class AriaHiddenLabel extends Widget implements HasHTML, HasText {

	public AriaHiddenLabel(String text, boolean asHtml) {
		setElement(DOM.createSpan());
		setStyleName("unitime-AriaHiddenLabel");
		if (text != null) {
			if (asHtml)
				setHTML(text);
			else
				setText(text);
		}
	}
	
	public AriaHiddenLabel(String text) {
		this(text, false);
	}
	
	public AriaHiddenLabel() {
		this(null, false);
	}
	
	@Override
	public String getText() {
		return getElement().getInnerText();
	}

	@Override
	public void setText(String text) {
		getElement().setInnerText(text);
	}

	@Override
	public String getHTML() {
		return getElement().getInnerHTML();
	}

	@Override
	public void setHTML(String html) {
		getElement().setInnerHTML(html);
	}
}
