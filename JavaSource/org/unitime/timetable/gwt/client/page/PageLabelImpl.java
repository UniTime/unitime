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
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class PageLabelImpl extends P implements PageLabelDisplay {
	private static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	
	private P iName;
	private Image iHelp;
	private String iUrl = null;
	
	public PageLabelImpl() {
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
				UniTimeFrameDialog.openDialog(MESSAGES.pageHelp(getText()), iUrl);
			}
		});
	}

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
