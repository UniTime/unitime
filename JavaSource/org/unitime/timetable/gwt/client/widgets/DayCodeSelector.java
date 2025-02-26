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

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.resources.GwtConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

public class DayCodeSelector extends Composite implements HasValue<Integer>, HasText {
	protected static GwtConstants CONST = GWT.create(GwtConstants.class);
	private P iPanel;
	private List<Bit> iBits;
	protected static int DAY_CODES[] = new int[] { 64, 32, 16, 8, 4, 2, 1 };
	

	public DayCodeSelector() {
		iPanel = new P("unitime-DayCodeSector");
		iBits = new ArrayList<Bit>();
		for (int i = 0; i < CONST.days().length; i++) {
			Bit bit = new Bit(DAY_CODES[i], CONST.days()[i]);
			iPanel.add(bit);
			iBits.add(bit);
		}
		initWidget(iPanel);
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Integer getValue() {
		int ret = 0;
		for (Bit bit: iBits) {
			if (bit.getValue()) ret += bit.value();
		}
		return ret;
	}

	@Override
	public void setValue(Integer value) {
		setValue(value, false);
	}

	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if (value == null)
			value = 0;
		for (Bit bit: iBits)
			bit.setValue((value & bit.value()) != 0);
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	

	
	class Bit extends CheckBox {
		private int iValue;
		
		Bit(int value, String text) {
			super(text);
			addStyleName("bit");
			iValue = value;
			addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> e) {
					ValueChangeEvent.fire(DayCodeSelector.this, DayCodeSelector.this.getValue());
				}
			});
		}
		
		public int value() { return iValue; }
	}



	@Override
	public String getText() {
		Integer value = getValue();
		return (value == null ? "" : value.toString());
	}

	@Override
	public void setText(String text) {
		if (text == null || text.isEmpty()) setValue(null);
		else {
			try {
				setValue(Integer.parseInt(text));
			} catch (NumberFormatException e) {
				setValue(null);
			}
		}
	}
}
