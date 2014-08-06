/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.aria;

import org.unitime.timetable.gwt.resources.GwtAriaMessages;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.aria.client.SelectedValue;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.TabBar;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class AriaTabBar extends TabBar {
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static RegExp sStripAcessKeyRegExp = RegExp.compile("(.*)<u>(\\w)</u>(.*)", "i");
	
	public AriaTabBar() {
		super();
		getElement().setId(DOM.createUniqueId());
	}
	
	public Element getTabElement(int index) {
		return ((Widget)getTab(index)).getElement();
	}
	
	@Override
	protected void insertTabWidget(Widget widget, int beforeIndex) {
		super.insertTabWidget(widget, beforeIndex);
		Roles.getTabRole().setAriaSelectedState(getTabElement(beforeIndex), SelectedValue.FALSE);
		getTabElement(beforeIndex).setId(DOM.createUniqueId());
		Id ids[] = new Id[getTabCount()];
		for (int i = 0; i < getTabCount(); i++) {
			Roles.getTabRole().setAriaLabelProperty(getTabElement(i), ARIA.tabNotSelected(1 + i, getTabCount(), getTabLabel(i)));
			ids[i] = Id.of(getTabElement(i));
		}
		Roles.getTablistRole().setAriaOwnsProperty(getElement(), ids);
	}
	
	@Override
	public void removeTab(int index) {
		super.removeTab(index);
		Id ids[] = new Id[getTabCount()];
		for (int i = 0; i < getTabCount(); i++) {
			Roles.getTabRole().setAriaLabelProperty(getTabElement(i), ARIA.tabNotSelected(1 + i, getTabCount(), getTabLabel(i)));
			ids[i] = Id.of(getTabElement(i));
		}
		Roles.getTablistRole().setAriaOwnsProperty(getElement(), ids);
		if (getSelectedTab() >= 0) {
			Roles.getTabRole().setAriaSelectedState(getTabElement(getSelectedTab()), SelectedValue.TRUE);
			Roles.getTabRole().setAriaLabelProperty(getTabElement(getSelectedTab()), ARIA.tabSelected(1 + getSelectedTab(), getTabCount(), getTabLabel(getSelectedTab())));
		}
	}
	

	@Override
	public boolean selectTab(int index, boolean fireEvents) {
		if (getSelectedTab() >= 0) {
			Roles.getTabRole().setAriaSelectedState(getTabElement(getSelectedTab()), SelectedValue.FALSE);
			Roles.getTabRole().setAriaLabelProperty(getTabElement(getSelectedTab()), ARIA.tabNotSelected(1 + getSelectedTab(), getTabCount(), getTabLabel(getSelectedTab())));
		}
		boolean ret = super.selectTab(index, fireEvents);
		if (getSelectedTab() >= 0) {
			Roles.getTabRole().setAriaSelectedState(getTabElement(getSelectedTab()), SelectedValue.TRUE);
			Roles.getTabRole().setAriaLabelProperty(getTabElement(getSelectedTab()), ARIA.tabSelected(1 + getSelectedTab(), getTabCount(), getTabLabel(getSelectedTab())));
			if (fireEvents)
				AriaStatus.getInstance().setHTML(ARIA.onTabSelected(getTabLabel(getSelectedTab())));
		}
		return ret;
	}
	
	public String getTabLabel(int index) {
		String html = getTabHTML(index);
		if (html == null || html.isEmpty()) return "";
		MatchResult result = sStripAcessKeyRegExp.exec(html);
		return (result == null ? html : result.getGroup(1) + result.getGroup(2) + result.getGroup(3));
	}
}
