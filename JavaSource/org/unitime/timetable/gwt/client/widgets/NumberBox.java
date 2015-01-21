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