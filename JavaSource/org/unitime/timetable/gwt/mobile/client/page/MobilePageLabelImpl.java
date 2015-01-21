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
package org.unitime.timetable.gwt.mobile.client.page;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.PageLabelDisplay;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.PageNameInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.widget.button.ImageButton;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;

/**
 * @author Tomas Muller
 */
public class MobilePageLabelImpl extends P implements PageLabelDisplay {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private P iName;
	private ImageButton iHelp;
	private ImageButton iClose = null;
	private String iUrl = null;
	
	public MobilePageLabelImpl() {
        iName = new P("text");
		add(iName);
        
        iHelp = new ImageButton(ImageHolder.get().about());
		iHelp.setVisible(false);
		P help = new P("icon");
        help.add(iHelp);
		add(help);
		
		iHelp.addTapHandler(new TapHandler() {
			@Override
			public void onTap(TapEvent event) {
				if (iUrl == null || iUrl.isEmpty()) return;
				if (MGWT.getFormFactor().isTablet())
					UniTimeFrameDialog.openDialog(MESSAGES.pageHelp(getText()), iUrl);
				else
					ToolBox.open(iUrl);
			}
		});
		
		if (hasParentWindow()) {
			iClose = new ImageButton(ImageHolder.get().remove());
			P close = new P("icon");
			close.add(iClose);
			add(close);
			
			iClose.addTapHandler(new TapHandler() {
				@Override
				public void onTap(TapEvent event) {
					tellParentToCloseThisWindo();
				}
			});
		}
	}
	
	public static native boolean hasParentWindow()/*-{
		return ($wnd.parent && $wnd.parent.hasGwtDialog());
	}-*/;
	
	public static native boolean tellParentToCloseThisWindo()/*-{
		$wnd.parent.hideGwtDialog();
	}-*/;

	@Override
	public String getText() {
		return iName.getText();
	}

	@Override
	public void setText(String text) {
		iName.setText(text);
		iHelp.setTitle(MESSAGES.pageHelp(text));
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
}