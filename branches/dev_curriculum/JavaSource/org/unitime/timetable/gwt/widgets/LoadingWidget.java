/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class LoadingWidget extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	
	private static LoadingWidget sInstance = null;

	private AbsolutePanel iPanel = null;
	private Image iImage = null;
	private int iCount = 0;
	
	public LoadingWidget() {
		iPanel = new AbsolutePanel();
		iPanel.setStyleName("unitime-LoadingPanel");
		iImage = new Image(RESOURCES.loading());
		iImage.setStyleName("unitime-LoadingIcon");
		initWidget(iPanel);
	}
	
	public void show() {
		if (iCount == 0) {
			RootPanel.get().add(this, Window.getScrollLeft(), Window.getScrollTop());
			RootPanel.get().add(iImage, Window.getScrollLeft() + Window.getClientWidth() / 2, Window.getScrollTop() + Window.getClientHeight() / 2);
		}
		iCount ++;
	}
	
	public void hide() {
		if (iCount > 0) iCount --;
		if (iCount == 0) {
			RootPanel.get().remove(iImage);
			RootPanel.get().remove(this);
		}
	}

	public static LoadingWidget getInstance() {
		if (sInstance == null) sInstance = new LoadingWidget();
		return sInstance;
	}
}
