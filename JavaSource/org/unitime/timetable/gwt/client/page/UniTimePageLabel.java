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
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class UniTimePageLabel implements HasValue<PageNameInterface> {
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
			iLabel.setValue(new PageNameInterface(title));
		panel.getElement().setInnerText("");
		panel.add(iLabel);
		setPageName(title);
	}
	
	public void setPageName(final String title) {
		if (title == null || title.isEmpty()) return;
		RPC.execute(new MenuInterface.PageNameRpcRequest(title), new AsyncCallback<PageNameInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				setValue(new PageNameInterface(title), true);
			}
			@Override
			public void onSuccess(PageNameInterface result) {
				if (!result.hasName()) result.setName(title);
				setValue(result, true);
			}
		});
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PageNameInterface> handler) {
		return iLabel.addValueChangeHandler(handler);
	}

	@Override
	public void fireEvent(GwtEvent<?> event) {
		iLabel.fireEvent(event);
	}

	@Override
	public PageNameInterface getValue() {
		return iLabel.getValue();
	}

	@Override
	public void setValue(PageNameInterface value) {
		Window.setTitle("UniTime " + CONSTANTS.version() + "| " + value.getName());
		iLabel.setValue(value);
	}

	@Override
	public void setValue(PageNameInterface value, boolean fireEvents) {
		Window.setTitle("UniTime " + CONSTANTS.version() + "| " + value.getName());
		iLabel.setValue(value, fireEvents);
	}

}
