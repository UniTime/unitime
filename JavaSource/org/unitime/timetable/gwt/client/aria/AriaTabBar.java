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

import org.unitime.timetable.gwt.resources.GwtAriaMessages;

import com.google.gwt.aria.client.Id;
import com.google.gwt.aria.client.Roles;
import com.google.gwt.aria.client.SelectedValue;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	
	public void setRestWidget(Widget rest) {
		HorizontalPanel panel = (HorizontalPanel)getWidget();
		rest.addStyleName("gwt-TabBarRest");
		rest.setHeight("100%");
		panel.remove(panel.getWidgetCount() - 1);
		panel.add(rest);
		panel.setCellWidth(rest, "100%");
		setStyleName(rest.getElement().getParentElement(), "gwt-TabBarRest-wrapper");
	}
}
