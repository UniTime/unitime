package org.unitime.timetable.gwt.client.events;

import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasValue;

public interface FilterBoxInterface<T> extends HasValue<String> {
	
	public Chip getChip(String command);
	public boolean hasChip(FilterBox.Chip chip);

	public T getElementsRequest();
	public void setDefaultValueProvider(TakesValue<String> defaultValue);
}
