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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class UniTimeHeaderPanel extends P {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static RegExp sAcessKeyRegExp = RegExp.compile("<u>(\\w)</u>", "i");
	private static RegExp sStripAcessKeyRegExp = RegExp.compile("(.*)<u>(\\w)</u>(.*)", "i");

	private HashMap<String, Integer> iOperations = new HashMap<String, Integer>();
	private HashMap<String, ClickHandler> iClickHandlers = new HashMap<String, ClickHandler>();
	private P iMessage;
	private P iTitle;
	private P iLeft, iContent, iRight, iButtons; 
	private Image iLoadingImage;
	private OpenCloseSectionImage iOpenCloseImage;
	private boolean iRotateFocus = false;
	private KeyDownHandler iKeyDownHandler = null;
		
	private List<UniTimeHeaderPanel> iClones = new ArrayList<UniTimeHeaderPanel>();
	
	public UniTimeHeaderPanel(String title) {
		super("unitime-HeaderPanel");
		
		iLeft = new P("left");
		add(iLeft);
		
		iRight = new P("right", "unitime-NoPrint");
		add(iRight);
		
		iButtons = new P(DOM.createSpan(), "buttons");
		iRight.add(iButtons);
		
		iContent = new P("content");
		add(iContent);
		
		iOpenCloseImage = new OpenCloseSectionImage(true);
		iOpenCloseImage.setVisible(false);
		iOpenCloseImage.addStyleName("open-close");
		iOpenCloseImage.addStyleName("unitime-NoPrint");
		iLeft.add(iOpenCloseImage);

		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.addStyleName("loading");
		iLoadingImage.addStyleName("unitime-NoPrint");
		iLoadingImage.setVisible(false);
		iLeft.add(iLoadingImage);

		iTitle = new P("title");
		iTitle.setHTML(title);
		iLeft.add(iTitle);
		
		iMessage = new P("message");
		iMessage.setVisible(false);
		iContent.add(iMessage);
		
		iKeyDownHandler = new KeyDownHandler() {
			private void focus(KeyDownEvent event, final Button buttonToFocus) {
				event.preventDefault();
				event.stopPropagation();
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						buttonToFocus.setFocus(true);
					}
				});
			}
			
			@Override
			public void onKeyDown(KeyDownEvent event) {
				if (iRotateFocus && event.getNativeKeyCode() == KeyCodes.KEY_TAB && event.getSource() != null && event.getSource() instanceof Button) {
					// first button
					ComplexPanel panel = iButtons;
					Button firstButton = null;
					for (int i = 0; i < panel.getWidgetCount(); i++) {
						Button button = (Button)panel.getWidget(i);
						if (button.isEnabled()) { firstButton = button; break; }	
					}
					if (firstButton == null) return;

					// last button
					if (!iClones.isEmpty()) panel = iClones.get(iClones.size() - 1).iButtons;
					Button lastButton = null;
					for (int i = panel.getWidgetCount() - 1; i >= 0; i--) {
						Button button = (Button)panel.getWidget(i);
						if (button.isEnabled()) { lastButton = button; break; }
					}
					if (lastButton == null) return;
					
					// last to first
					if (lastButton.equals(event.getSource()) && !event.isShiftKeyDown())
						focus(event, firstButton);
					
					// first to last
					if (firstButton.equals(event.getSource()) && event.isShiftKeyDown())
						focus(event, lastButton);
				}
			}
		};
	}
	
	public void setRotateFocus(boolean rotateFocus) {
		iRotateFocus = rotateFocus;
	}
	
	public void setTitleStyleName(String styleName) {
		iTitle.setStyleName(styleName);
	}
	
	public void addCollapsibleHandler(ValueChangeHandler<Boolean> handler) {
		iOpenCloseImage.addValueChangeHandler(handler);
	}
	
	public void setCollapsible(Boolean opened) {
		iOpenCloseImage.setVisible(opened != null);
		if (opened != null)
			iOpenCloseImage.setValue(opened, false);
	}
	
	public Boolean isCollapsible() {
		return iOpenCloseImage.isVisible() ? iOpenCloseImage.getValue() : null;
	}
	
	public void setHeaderTitle(String title) {
		iTitle.setHTML(title);
	}
	
	public String getHeaderTitle() {
		return iTitle.getHTML();
	}
	
	public P getHeaderTitlePanel() { return iTitle; }

	public UniTimeHeaderPanel() {
		this("&nbsp;");
	}
	
	public void clearMessage() {
		iMessage.setHTML("");
		iMessage.setVisible(false);
		iLoadingImage.setVisible(false);
		for (UniTimeHeaderPanel clone: iClones)
			clone.clearMessage();
	}
	
	public void setErrorMessage(String message) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("error");
			iMessage.setVisible(true);
			for (UniTimeHeaderPanel clone: iClones)
				clone.setErrorMessage(message);
		}
	}
	
	public void setWarningMessage(String message) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("warning");
			iMessage.setVisible(true);
			for (UniTimeHeaderPanel clone: iClones)
				clone.setWarningMessage(message);
		}
	}
	
	public void setMessage(String message) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("message");
			iMessage.setVisible(true);
			for (UniTimeHeaderPanel clone: iClones)
				clone.setMessage(message);
		}
	}
	
	public void showLoading() {
		iMessage.setHTML("");
		iMessage.setVisible(false);
		iLoadingImage.setVisible(true);
		for (UniTimeHeaderPanel clone: iClones)
			clone.showLoading();
	}

	public void addButton(String operation, String name, ClickHandler clickHandler) {
		addButton(operation, name, null, clickHandler);
	}
	
	public void addButton(String operation, String name, Integer width, ClickHandler clickHandler) {
		addButton(operation, name, guessAccessKey(name), width == null ? null : width + "px", clickHandler);
	}
	
	public Button getButton(String operation) {
		return (Button)iButtons.getWidget(iOperations.get(operation));
	}
	
	public static Character guessAccessKey(String name) {
		if (name == null || name.isEmpty()) return null;
		MatchResult result = sAcessKeyRegExp.exec(name);
		return (result == null ? null : result.getGroup(1).toLowerCase().charAt(0));
	}
	
	public static String stripAccessKey(String name) {
		if (name == null || name.isEmpty()) return "";
		MatchResult result = sStripAcessKeyRegExp.exec(name);
		return (result == null ? name : result.getGroup(1) + result.getGroup(2) + result.getGroup(3));
	}

	private Button addButton(String operation, String name, Character accessKey, String width, ClickHandler clickHandler) {
		Button button = new AriaButton(name);
		button.addClickHandler(clickHandler);
		ToolBox.setWhiteSpace(button.getElement().getStyle(), "nowrap");
		if (accessKey != null)
			button.setAccessKey(accessKey);
		if (width != null)
			ToolBox.setMinWidth(button.getElement().getStyle(), width);
		iOperations.put(operation, iButtons.getWidgetCount());
		iClickHandlers.put(operation, clickHandler);
		iButtons.add(button);
		button.getElement().getStyle().setMarginLeft(4, Unit.PX);
		for (UniTimeHeaderPanel clone: iClones) {
			Button clonedButton = clone.addButton(operation, name, null, width, clickHandler);
			clonedButton.addKeyDownHandler(iKeyDownHandler);
		}
		button.addKeyDownHandler(iKeyDownHandler);
		return button;
	}
	
	public void setEnabled(int button, boolean enabled) {
		Button b = (Button)iButtons.getWidget(button);
		b.setVisible(enabled); b.setEnabled(enabled);
		for (UniTimeHeaderPanel clone: iClones)
			clone.setEnabled(button, enabled);
	}
	
	public void setEnabled(int button, boolean enabled, boolean visible) {
		Button b = (Button)iButtons.getWidget(button);
		b.setVisible(visible); b.setEnabled(enabled);
		for (UniTimeHeaderPanel clone: iClones)
			clone.setEnabled(button, enabled, visible);
	}
	
	public void setEnabled(String operation, boolean enabled) {
		Integer op = iOperations.get(operation);
		if (op != null)
			setEnabled(iOperations.get(operation), enabled);
	}
	
	public void setEnabled(String operation, boolean enabled, boolean visible) {
		Integer op = iOperations.get(operation);
		if (op != null)
			setEnabled(iOperations.get(operation), enabled, visible);
	}
	
	public boolean isEnabled(int button) {
		return ((Button)iButtons.getWidget(button)).isVisible();
	}

	public Boolean isEnabled(String operation) {
		Integer op = iOperations.get(operation);
		return (op == null ? null : isEnabled(op));
	}
	
	public void setFocus(int button, boolean focus) {
		Button b = (Button)iButtons.getWidget(button);
		b.setFocus(focus);
	}
	
	public void setFocus(String operation, boolean focus) {
		Integer op = iOperations.get(operation);
		if (op != null)
			setFocus(iOperations.get(operation), focus);
	}
	
	public UniTimeHeaderPanel clonePanel(String newTitle) {
		UniTimeHeaderPanel clone = new UniTimeHeaderPanel(newTitle == null ? "&nbsp;" : newTitle);
		iClones.add(clone);
		clone.iMessage.setHTML(iMessage.getHTML());
		clone.iMessage.setVisible(iMessage.isVisible());
		clone.iMessage.setStyleName(iMessage.getStyleName());
		for (int i = 0; i < iOperations.size(); i++) {
			String op = null;
			for (Map.Entry<String,Integer> entry: iOperations.entrySet())
				if (entry.getValue() == i) op = entry.getKey();
			if (op == null) continue;
			final Button button = (Button)iButtons.getWidget(i);
			ClickHandler clickHandler = iClickHandlers.get(op);
			if (clickHandler == null)
				clickHandler = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						button.click();
					}
				};
			String width = ToolBox.getMinWidth(button.getElement().getStyle());
			Button clonedButton = clone.addButton(op, button.getHTML(), null, width, clickHandler);
			clonedButton.addKeyDownHandler(iKeyDownHandler);
			if (!button.isVisible())
				clone.setEnabled(op, false);
			if (button.getTitle() != null)
				clone.setTitle(button.getTitle());
		}
		return clone;
	}
	
	public UniTimeHeaderPanel clonePanel() {
		return clonePanel(iTitle.getHTML());
	}
		
	public void setVisible(boolean visible, boolean propagate) {
		super.setVisible(visible);
		if (propagate)
			for (UniTimeHeaderPanel clone: iClones)
				clone.setVisible(visible, propagate);
	}
	
	public void setVisible(boolean visible) {
		setVisible(visible, false);
	}
	
	public void insertLeft(Widget widget, boolean first) {
		widget.addStyleName("left-widget");
		if (first)
			iLeft.insert(widget, 0);
		else
			iLeft.add(widget);
	}

	public void insertWidget(Widget widget) {
		widget.addStyleName("widget");
		iContent.insert(widget, 1);
	}
	
	public void insertRight(Widget widget, boolean first) {
		widget.addStyleName("right-widget");
		if (first)
			iRight.insert(widget, 0);
		else
			iRight.add(widget);
	}
	
	public Set<String> getOperations() { return iOperations.keySet(); }
}
