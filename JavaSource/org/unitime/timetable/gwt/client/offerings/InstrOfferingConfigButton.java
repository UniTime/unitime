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
package org.unitime.timetable.gwt.client.offerings;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class InstrOfferingConfigButton extends AriaButton {
	protected static final CourseMessages MESSAGES = GWT.create(CourseMessages.class);
	private Long iConfigId = null;
	
	public InstrOfferingConfigButton(boolean editable) {
		super(MESSAGES.actionEditConfiguration());
		setTitle(MESSAGES.titleEditConfiguration());
		addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open(GWT.getHostPageBaseURL() + "instrOfferingConfig?id=" + iConfigId);
			}
		});
		setVisible(editable);
		setEnabled(editable);
	}
	
	public void insert(final RootPanel panel) {
		iConfigId = Long.valueOf(panel.getElement().getInnerText());
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
}
