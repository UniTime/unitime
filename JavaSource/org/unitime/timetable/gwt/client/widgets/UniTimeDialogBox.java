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

import org.unitime.timetable.gwt.client.Components;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaDialogBox;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasOpenHandlers;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class UniTimeDialogBox extends AriaDialogBox implements HasOpenHandlers<UniTimeDialogBox> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private FlowPanel iContainer, iControls;
	private Anchor iClose, iMaximize;
	private boolean iEscapeToHide = false;
	private Command iSubmitHandler = null;
	private String iWidth = null, iHeight = null;
	private boolean iMaximized = false;
	
	public UniTimeDialogBox(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        
		setAnimationEnabled(true);
		setGlassEnabled(true);
	
        iContainer = new FlowPanel();
        iContainer.addStyleName("dialogContainer");
        
        iMaximize = new Anchor();
        iMaximize.setTitle(MESSAGES.hintMaximizeDialog());
        iMaximize.setStyleName("maximize");
        iMaximize.addClickHandler(new ClickHandler() {
        	@Override
            public void onClick(ClickEvent event) {
        		onMaximizeClick(event);
            }
        });
        iMaximize.setVisible(false);

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
        iControls.add(iMaximize);
    }
	
	public void setMaximizeEnabled(boolean enabled) {
		iMaximize.setVisible(enabled);
	}
	
	public boolean isMaximizeEnabled() {
		return iMaximize.isVisible();
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
    
    protected boolean isHasMenu() {
    	return (RootPanel.get(Components.menubar_static.id()) != null || RootPanel.get(Components.menubar_dynamic.id()) != null);
    }
    
    protected void maximize() {
		boolean hasMenu = isHasMenu();
    	getElement().getStyle().setLeft(Window.getScrollLeft(), Unit.PX);
    	getElement().getStyle().setTop(Window.getScrollTop() + (hasMenu ? 22 : 0), Unit.PX);
    	iContainer.getWidget(1).setWidth("calc(100vw - 15px)");
    	iContainer.getWidget(1).setHeight(hasMenu ? "calc(100vh - 72px)" : "calc(100vh - 50px)");
    	iMaximize.setTitle(MESSAGES.hintDemaximizeDialog());
    	ToolBox.setSessionCookie("UniTimeDialogBox.Maximize", "1");
    	iMaximized = true;
    }
    
    protected void minimize() {
    	if (iWidth != null)
			iContainer.getWidget(1).getElement().getStyle().setProperty("width", iWidth);
		else
			iContainer.getWidget(1).getElement().getStyle().clearWidth();
		if (iHeight != null)
			iContainer.getWidget(1).getElement().getStyle().setProperty("height", iHeight);
		else
			iContainer.getWidget(1).getElement().getStyle().clearHeight();
		
		center();
		iMaximize.setTitle(MESSAGES.hintMaximizeDialog());
		ToolBox.setSessionCookie("UniTimeDialogBox.Maximize", "0");
		iMaximized = false;
    }
    
    protected void onMaximizeClick(ClickEvent event) {
    	if (!iMaximized) {
    		maximize();
    	} else {
    		minimize();
    	}
    }

    public HandlerRegistration addOpenHandler(OpenHandler<UniTimeDialogBox> handler) {
        return addHandler(handler, OpenEvent.getType());
    }
    
    @Override
    public void show() {
        boolean fireOpen = !isShowing();
        if (iMaximize != null && iMaximize.isVisible()) {
        	if (!iMaximized) {
        		iHeight = iContainer.getWidget(1).getElement().getStyle().getProperty("height");
        		iWidth = iContainer.getWidget(1).getElement().getStyle().getProperty("width");
        	}
        	iMaximized = "1".equals(ToolBox.getSessionCookie("UniTimeDialogBox.Maximize"));
        	super.show();
        	if (iMaximized)
        		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        			@Override
    				public void execute() {
    					maximize();
    				}
    			});
        } else {
        	super.show();
        }
        if (fireOpen) {
            OpenEvent.fire(this, this);
        }
    }
    
    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
		switch (event.getTypeInt()) {
	    case Event.ONKEYDOWN:
			if (isEscapeToHide() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
				AriaStatus.getInstance().setText(ARIA.dialogClosed(getText()));
				hide();
			} if (isEnterToSubmit() && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
				iSubmitHandler.execute();
			}
			break;
		}
	}
}
