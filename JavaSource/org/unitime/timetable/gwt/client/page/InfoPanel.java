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

import org.unitime.timetable.gwt.shared.MenuInterface.InfoInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class InfoPanel extends Composite implements InfoPanelDisplay {
	private InfoPanelDisplay IMPL = GWT.create(InfoPanelDisplay.class);
	private boolean iPreventDefault = false;
	
	public InfoPanel() {
		initWidget(IMPL.asWidget());
	}

	@Override
	public String getText() {
		return IMPL.getText();
	}


	@Override
	public void setText(String text) {
		IMPL.setText(text);
	}


	@Override
	public String getHint() {
		return IMPL.getHint();
	}


	@Override
	public void setHint(String hint) {
		IMPL.setHint(hint);
	}


	@Override
	public void setUrl(String url) {
		IMPL.setUrl(url);
	}


	@Override
	public void setInfo(InfoInterface info) {
		IMPL.setInfo(info);
	}


	@Override
	public void setCallback(Callback callback) {
		IMPL.setCallback(callback);
	}


	@Override
	public boolean isPopupShowing() {
		return IMPL.isPopupShowing();
	}
	
	@Override
	public void setClickHandler(ClickHandler handler) {
		IMPL.setClickHandler(handler);
	}

	@Override
	public String getAriaLabel() {
		return IMPL.getAriaLabel();
	}

	@Override
	public void setAriaLabel(String text) {
		IMPL.setAriaLabel(text);
	}
	
	public boolean isPreventDefault() { return iPreventDefault; }
	public void setPreventDefault(boolean preventDefault) { iPreventDefault = preventDefault; }
}
