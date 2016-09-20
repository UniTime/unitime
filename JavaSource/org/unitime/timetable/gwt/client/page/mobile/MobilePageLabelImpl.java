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
package org.unitime.timetable.gwt.client.page.mobile;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.PageLabelDisplay;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class MobilePageLabelImpl extends P implements PageLabelDisplay {
	private static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private P iName;
	private Image iHelp;
	private Image iClose = null;
	private String iUrl = null;
	
	public MobilePageLabelImpl() {
        iName = new P("text");
        
        iHelp = new Image(RESOURCES.help());
		iHelp.addStyleName("icon");
		iHelp.setVisible(false);
		
		add(iName);
		add(iHelp);
		
		iHelp.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iUrl == null || iUrl.isEmpty()) return;
				if (Window.getClientWidth()>=600)
					UniTimeFrameDialog.openDialog(MESSAGES.pageHelp(getText()), iUrl);
				else
					ToolBox.open(iUrl);
			}
		});
		
		if (hasParentWindow()) {
			iClose = new Image(RESOURCES.close());
			iClose.addStyleName("icon");
			iClose.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					tellParentToCloseThisWindo();
				}
			});
			add(iClose);
		}
	}

	@Override
	public String getText() {
		return iName.getText();
	}

	@Override
	public void setText(String text) {
		iName.setText(text);
		iHelp.setTitle(MESSAGES.pageHelp(text));
		if (iClose != null)
			iClose.setTitle(MESSAGES.pageClose(text));
	}

	@Override
	public PageNameInterface getValue() {
		return new PageNameInterface(getText(), iUrl);
	}

	@Override
	public void setValue(PageNameInterface value) {
		setValue(value, false);
	}

	@Override
	public void setValue(PageNameInterface value, boolean fireEvents) {
		iUrl = value.getHelpUrl();
		iHelp.setVisible(iUrl != null && !iUrl.isEmpty());
		setText(value.getName());
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<PageNameInterface> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
	
	public static native boolean hasParentWindow()/*-{
		return ($wnd.parent && $wnd.parent.hasGwtDialog());
	}-*/;

	public static native boolean tellParentToCloseThisWindo()/*-{
		$wnd.parent.hideGwtDialog();
	}-*/;
}
