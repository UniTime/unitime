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
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.VersionInfoInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimeVersion extends Composite {
	protected static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	private Label iLabel;
	
	public UniTimeVersion() {
		iLabel = new Label();
		
		RPC.execute(new MenuInterface.VersionInfoRpcRequest(), new AsyncCallback<VersionInfoInterface>() {
			@Override
			public void onSuccess(VersionInfoInterface result) {
				iLabel.setText(MESSAGES.pageVersion(result.getVersion(), result.getReleaseDate()));
			}
			
			@Override
			public void onFailure(Throwable caught) {
			}
		});
		
		initWidget(iLabel);
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
	}


}
