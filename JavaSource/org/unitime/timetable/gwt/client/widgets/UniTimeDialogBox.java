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
		if (isEscapeToHide() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
			AriaStatus.getInstance().setText(ARIA.dialogClosed(getText()));
			hide();
		} if (isEnterToSubmit() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
			event.getNativeEvent().stopPropagation();
			event.getNativeEvent().preventDefault();
	    	iSubmitHandler.execute();
		}
	}
}
