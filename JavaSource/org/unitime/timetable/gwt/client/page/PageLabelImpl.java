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

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ImageResourceRenderer;

/**
 * @author Tomas Muller
 */
public class PageLabelImpl extends P implements PageLabelDisplay {
	private static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private P iName;
	private Anchor iHelp;
	private String iUrl = null;
	
	public PageLabelImpl() {
        iName = new P("text");
        
		iHelp = new Anchor(new ImageResourceRenderer().render(RESOURCES.help()), "", "_blank");
		iHelp.addStyleName("icon");
		iHelp.setVisible(false);
		
		add(iName);
		add(iHelp);
	}

	@Override
	public String getText() {
		return iName.getText();
	}

	@Override
	public void setText(String text) {
		iName.setText(text);
		iHelp.setTitle(MESSAGES.pageHelp(text));
	}

	@Override
	public PageNameInterface getValue() {
		return new PageNameInterface(getText(), iUrl);
	}

	@Override
	public void setValue(PageNameInterface value) {
		setValue(value, false);
	}

	@Override
	public void setValue(PageNameInterface value, boolean fireEvents) {
		iUrl = value.getHelpUrl();
		iHelp.setVisible(iUrl != null && !iUrl.isEmpty());
		iHelp.setHref(iUrl != null && !iUrl.isEmpty() ? iUrl : "");
		setText(value.getName());
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PageNameInterface> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
