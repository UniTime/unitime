/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class UniTimeDialogBox extends AriaDialogBox implements HasOpenHandlers<UniTimeDialogBox> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private FlowPanel iContainer, iControls;
	private Anchor iClose;
	private boolean iEscapeToHide = false;
	private Command iSubmitHandler = null;
	    
    public UniTimeDialogBox(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        
		setAnimationEnabled(true);
		setGlassEnabled(true);
	
        iContainer = new FlowPanel();
        iContainer.addStyleName("dialogContainer");
        
        iClose = new Anchor();
    	iClose.setTitle(MESSAGES.hintCloseDialog());
        iClose.setStyleName("close");
        iClose.addClickHandler(new ClickHandler() {
        	@Override
            public void onClick(ClickEvent event) {
                onCloseClick(event);
            }
        });
        iClose.setVisible(autoHide);

        iControls = new FlowPanel();
        iControls.setStyleName("dialogControls");        
        iControls.add(iClose);
    }
    
    @Override
    public void center() {
    	super.center();
    	AriaStatus.getInstance().setText(ARIA.dialogOpened(getText()));
    }
    
    public void setAutoHideEnabled(boolean autoHide) {
    	super.setAutoHideEnabled(autoHide);
    	iClose.setVisible(autoHide);
    }
    
    public void setEscapeToHide(boolean escapeToHide) { iEscapeToHide = escapeToHide; }
    public boolean isEscapeToHide() { return iEscapeToHide; }
    
    public void setEnterToSubmit(Command submitCommand) { iSubmitHandler = submitCommand; }
    public boolean isEnterToSubmit() { return iSubmitHandler != null; }

    public void setWidget(Widget widget) {
        if (iContainer.getWidgetCount() == 0) {
            iContainer.add(iControls);
            super.setWidget(iContainer);
        } else {
            while (iContainer.getWidgetCount() > 1) {
                iContainer.remove(1);
            }
        }
        iContainer.add(widget);
    }

    protected void onCloseClick(ClickEvent event) {
        hide();
    }

    public HandlerRegistration addOpenHandler(OpenHandler<UniTimeDialogBox> handler) {
        return addHandler(handler, OpenEvent.getType());
    }
    
    @Override
    public void show() {
        boolean fireOpen = !isShowing();
        super.show();
        if (fireOpen) {
            OpenEvent.fire(this, this);
        }
    }
    
    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
		if (isEscapeToHide() && DOM.eventGetKeyCode((Event)event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
			hide();
		if (isEnterToSubmit() && DOM.eventGetKeyCode((Event)event.getNativeEvent()) == KeyCodes.KEY_ENTER)
	    	iSubmitHandler.execute();
	}
}
