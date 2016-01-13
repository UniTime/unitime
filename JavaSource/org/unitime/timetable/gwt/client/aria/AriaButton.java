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
package org.unitime.timetable.gwt.client.aria;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;

/**
 * @author Tomas Muller
 */
public class AriaButton extends Button implements HasAriaLabel {
	private boolean iFocused = false;
	
	public AriaButton() {
		super();
	}
	
	public AriaButton(String html) {
		super(html);
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html).replace("&nbsp;", " ").replace("&#8209;","-"));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
		ToolBox.setMinWidth(getElement().getStyle(), "75px");
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iFocused = true;
			}
		});
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				iFocused = false;
			}
		});
	}
	
	public AriaButton(ImageResource image, String html) {
		super(html);
		super.setHTML("<img src='" + image.getSafeUri().asString() + "' class='gwt-Image'></img><span class='gwt-Label' style='padding-left: 5px;'>" + html + "</span>");
		addStyleName("unitime-ImageButton");
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
		ToolBox.setMinWidth(getElement().getStyle(), "75px");
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iFocused = true;
			}
		});
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				iFocused = false;
			}
		});
	}
	
	public AriaButton(String html, ImageResource image) {
		super(html);
		super.setHTML("<span class='gwt-Label' style='padding-right: 5px;'>" + html + "</span><img src='" + image.getSafeUri().asString() + "' class='gwt-Image'></img>");
		addStyleName("unitime-ImageButton");
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
		ToolBox.setMinWidth(getElement().getStyle(), "75px");
		addFocusHandler(new FocusHandler() {
			@Override
			public void onFocus(FocusEvent event) {
				iFocused = true;
			}
		});
		addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				iFocused = false;
			}
		});
	}
	
	@Override
	public void setHTML(String html) {
		super.setHTML(html);
		setAriaLabel(UniTimeHeaderPanel.stripAccessKey(html));
		Character accessKey = UniTimeHeaderPanel.guessAccessKey(html);
		if (accessKey != null)
			setAccessKey(accessKey);
	}

	@Override
	public void setAriaLabel(String text) {
		if (text == null || text.isEmpty())
			Roles.getButtonRole().removeAriaLabelledbyProperty(getElement());
		else
			Roles.getButtonRole().setAriaLabelProperty(getElement(), text);
	}

	@Override
	public String getAriaLabel() {
		return Roles.getButtonRole().getAriaLabelProperty(getElement());
	}

	public boolean isFocused() {
		return iFocused;
	}
}
