/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
