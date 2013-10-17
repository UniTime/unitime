/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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

import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class NumberBox extends TextBox {
	private boolean iDecimal = false, iNegative = false;

	public NumberBox() {
		setStyleName("gwt-SuggestBox");
		setWidth("100px");
		getElement().getStyle().setTextAlign(TextAlign.RIGHT);
		addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (!isEnabled() || isReadOnly()) return;
				
				int keyCode = event.getNativeEvent().getKeyCode();
				
				switch (keyCode) {
				case KeyCodes.KEY_BACKSPACE:
				case KeyCodes.KEY_DELETE:
				case KeyCodes.KEY_ESCAPE:
				case KeyCodes.KEY_RIGHT:
				case KeyCodes.KEY_LEFT:
				case KeyCodes.KEY_TAB:
					return;
				}

	            if (isDecimal() && event.getCharCode() == '.' && !getValue().contains(".")) return;
	            if (isNegative() && event.getCharCode() == '-' && !getValue().contains("-") && (getCursorPos() == 0 || getSelectionLength() == getValue().length()))
	            	return;

	            if (Character.isDigit(event.getCharCode()))
	                return;

	            cancelKey( );
	        }
	    } );
	}
	
	public boolean isDecimal() { return iDecimal; }
	public void setDecimal(boolean decimal) { iDecimal = decimal; }

	public boolean isNegative() { return iNegative; }
	public void setNegative(boolean negative) { iNegative = negative; }

	public Double toDouble() {
		try {
			return Double.parseDouble(getValue());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public Integer toInteger() {
		try {
			return Integer.parseInt(getValue());
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void setValue(Number number) {
		super.setValue(number == null ? "" : number.toString());
	}

}