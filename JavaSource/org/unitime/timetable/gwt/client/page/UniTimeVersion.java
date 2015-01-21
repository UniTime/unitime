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
