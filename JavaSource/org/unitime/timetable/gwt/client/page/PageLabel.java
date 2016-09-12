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
package org.unitime.timetable.gwt.client.page;

import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class PageLabel extends Composite implements PageLabelDisplay {
	private PageLabelDisplay iDisplay = null;
	
	public PageLabel() {
		if (Window.getClientWidth() <= 800)
			iDisplay = GWT.create(PageLabelDisplay.Mobile.class);
		else
			iDisplay = GWT.create(PageLabelDisplay.class);
		initWidget(iDisplay.asWidget());
	}

	@Override
	public PageNameInterface getValue() {
		return iDisplay.getValue();
	}

	@Override
	public void setValue(PageNameInterface value) {
		iDisplay.setValue(value);
	}

	@Override
	public void setValue(PageNameInterface value, boolean fireEvents) {
		iDisplay.setValue(value, fireEvents);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PageNameInterface> handler) {
		return iDisplay.addValueChangeHandler(handler);
	}
}