/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.events;

import org.unitime.timetable.gwt.client.widgets.TimeSelector;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public class StartEndTimeSelector extends Composite implements HasValue<StartEndTimeSelector.StartEndTime> {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private UniTimeWidget<HorizontalPanel> iPanel;
	private TimeSelector iStart, iEnd;
	
	public StartEndTimeSelector() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSpacing(2);
		panel.setStyleName("unitime-TimeSelectorPanel");
		Label labelFrom = new Label(MESSAGES.propFrom());
		panel.add(labelFrom);
		panel.setCellVerticalAlignment(labelFrom, HasVerticalAlignment.ALIGN_MIDDLE);
		iStart = new TimeSelector(null);
		panel.add(iStart);
		Label labelTo = new Label(MESSAGES.propTo());
		labelTo.getElement().getStyle().setPaddingLeft(5, Unit.PX);
		panel.add(labelTo);
		panel.setCellVerticalAlignment(labelTo, HasVerticalAlignment.ALIGN_MIDDLE);
		iEnd = new TimeSelector(iStart);
		panel.add(iEnd);
		
		iPanel = new UniTimeWidget<HorizontalPanel>(panel);
		
		initWidget(iPanel);
		
		iStart.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				ValueChangeEvent.fire(StartEndTimeSelector.this, getValue()); 
			}
		});
		
		iEnd.addValueChangeHandler(new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> event) {
				ValueChangeEvent.fire(StartEndTimeSelector.this, getValue()); 
			}
		});
	}
	
	
	public static class StartEndTime implements IsSerializable {
		private Integer iStart, iEnd;
		public StartEndTime() {}
		public StartEndTime(Integer start, Integer end) { iStart = start; iEnd = end; }
		
		public boolean hasStart() { return iStart != null; }
		public Integer getStart() { return iStart; }
		
		public boolean hasEnd() { return iEnd != null; }
		public Integer getEnd() { return iEnd; }
		
	}


	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<StartEndTime> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}


	@Override
	public StartEndTime getValue() {
		return new StartEndTime(iStart.getValue(), iEnd.getValue());
	}


	@Override
	public void setValue(StartEndTime value) {
		setValue(value, false);
	}


	@Override
	public void setValue(StartEndTime value, boolean fireEvents) {
		iStart.setValue(value == null ? null : value.getStart(), fireEvents);
		iEnd.setValue(value == null ? null : value.getEnd(), fireEvents);
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}
	
	public void setDiff(Integer diff) {
		iEnd.setDiff(diff);
	}
}
