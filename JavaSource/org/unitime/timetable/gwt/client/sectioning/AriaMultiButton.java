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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Tomas Muller
 */
public class AriaMultiButton extends AriaButton {
	List<AriaButton> iClones = new ArrayList<AriaButton>();
	
	public AriaMultiButton(String html) {
		super(html);
	}
	
	public AriaMultiButton(ImageResource image, String html) {
		super(image, html);
	}
	
	public AriaMultiButton(String html, ImageResource image) {
		super(html, image);
	}
	
	public AriaButton createClone() {
		AriaButton button = new AriaButton(getHTML());
		button.setAriaLabel(getAriaLabel());
		button.setStyleName(getStyleName());
		ToolBox.setMinWidth(button.getElement().getStyle(), "75px");
		button.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AriaMultiButton.this.click();
			}
		});
		iClones.add(button);
		button.setVisible(isVisible());
		button.setEnabled(isEnabled());
		String ml = getElement().getStyle().getMarginLeft();
		if (ml != null && !ml.isEmpty())
			button.getElement().getStyle().setProperty("margin-left", ml);
		String mr = getElement().getStyle().getMarginRight();
		if (mr != null && !mr.isEmpty())
			button.getElement().getStyle().setProperty("margin-right", mr);
		return button;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for (AriaButton clone: iClones)
			clone.setEnabled(enabled);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		for (AriaButton clone: iClones)
			clone.setVisible(visible);
	}

}
