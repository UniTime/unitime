/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimePageLabel {
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private PageLabel iLabel = new PageLabel();
	
	private static UniTimePageLabel sInstance = null;
	
	private UniTimePageLabel() {}
	
	public static UniTimePageLabel getInstance() {
		if (sInstance == null)
			sInstance = new UniTimePageLabel();
		return sInstance;
	}
	
	public void insert(RootPanel panel) {
		String title = panel.getElement().getInnerText();
		if (title != null && !title.isEmpty())
			iLabel.setText(title);
		panel.getElement().setInnerText("");
		panel.add(iLabel);
		setPageName(title);
	}
	
	public void setPageName(final String title) {
		if (title == null || title.isEmpty()) return;
		RPC.execute(new MenuInterface.PageNameRpcRequest(title), new AsyncCallback<PageNameInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.setTitle("UniTime " + CONSTANTS.version() + "| " + title);
				iLabel.setText(title);
				iLabel.setHelpUrl(null);
			}
			@Override
			public void onSuccess(PageNameInterface result) {
				iLabel.setHelpUrl(result.getHelpUrl());
				if (result.getName() != null) {
					iLabel.setText(result.getName());
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + result.getName());
				} else {
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + title);
					iLabel.setText(title);
				}
			}
		});
	}
}
