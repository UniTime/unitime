package org.unitime.timetable.gwt.client.events;

import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Widget;

public abstract class DummyFilter<T> extends Widget implements FilterBoxInterface<T> {
	private String iValue = "";

	@Override
	public String getValue() { return iValue; }

	@Override
	public void setValue(String value) { iValue = value; }

	@Override
	public void setValue(String value, boolean fireEvents) {
		iValue = value;
		if (fireEvents)
			ValueChangeEvent.fire(DummyFilter.this, getValue());
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Chip getChip(String command) { return null; }

	@Override
	public boolean hasChip(Chip chip) { return false; }

	@Override
	public void setDefaultValueProvider(TakesValue<String> defaultValue) {}
}
