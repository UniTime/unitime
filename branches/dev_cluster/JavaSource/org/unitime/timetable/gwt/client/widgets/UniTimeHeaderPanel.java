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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.client.ToolBox;
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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * @author Tomas Muller
 */
public class UniTimeHeaderPanel extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private static RegExp sAcessKeyRegExp = RegExp.compile("<u>(\\w)</u>", "i");
	private static RegExp sStripAcessKeyRegExp = RegExp.compile("(.*)<u>(\\w)</u>(.*)", "i");

	private HashMap<String, Integer> iOperations = new HashMap<String, Integer>();
	private HashMap<String, ClickHandler> iClickHandlers = new HashMap<String, ClickHandler>();
	private HTML iMessage;
	private HTML iTitle;
	private HorizontalPanel iButtons;
	private HorizontalPanel iPanel;
	private Image iLoadingImage;
	private OpenCloseSectionImage iOpenCloseImage;
	private boolean iRotateFocus = false;
	private KeyDownHandler iKeyDownHandler = null;
		
	private List<UniTimeHeaderPanel> iClones = new ArrayList<UniTimeHeaderPanel>();
	
	public UniTimeHeaderPanel(String title) {
		iPanel = new HorizontalPanel();
		
		iOpenCloseImage = new OpenCloseSectionImage(true);
		iOpenCloseImage.setVisible(false);
		iPanel.add(iOpenCloseImage);
		iPanel.setCellHorizontalAlignment(iOpenCloseImage, HasHorizontalAlignment.ALIGN_LEFT);
		iPanel.setCellVerticalAlignment(iOpenCloseImage, HasVerticalAlignment.ALIGN_MIDDLE);

		
		iTitle = new HTML(title, false);
		iTitle.setStyleName("unitime-MainHeader");
		iPanel.add(iTitle);
		iPanel.setCellHorizontalAlignment(iTitle, HasHorizontalAlignment.ALIGN_LEFT);
		iPanel.setCellVerticalAlignment(iTitle, HasVerticalAlignment.ALIGN_MIDDLE);
		
		iMessage = new HTML("", false);
		iMessage.setStyleName("unitime-Message");
		iMessage.setVisible(false);
		iPanel.add(iMessage);
		iPanel.setCellHorizontalAlignment(iMessage, HasHorizontalAlignment.ALIGN_CENTER);
		iPanel.setCellVerticalAlignment(iMessage, HasVerticalAlignment.ALIGN_MIDDLE);
		iPanel.setCellWidth(iMessage, "100%");
		
		iLoadingImage = new Image(RESOURCES.loading_small());
		iLoadingImage.setVisible(false);
		// iLoadingImage.getElement().getStyle().setMargin(20, Unit.PX);
		iPanel.add(iLoadingImage);
		iPanel.setCellHorizontalAlignment(iLoadingImage, HasHorizontalAlignment.ALIGN_CENTER);
		iPanel.setCellVerticalAlignment(iLoadingImage, HasVerticalAlignment.ALIGN_MIDDLE);

		
		iButtons = new HorizontalPanel();
		iButtons.addStyleName("unitime-NoPrint");
		iPanel.add(iButtons);
		iPanel.setCellHorizontalAlignment(iButtons, HasHorizontalAlignment.ALIGN_RIGHT);
		
		iPanel.setWidth("100%");
		// iPanel.getElement().getStyle().setMarginTop(2, Unit.PX);
		
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
		
		initWidget(iPanel);
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
	
	public void setErrorMessage(String message, boolean wrap) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("unitime-ErrorMessage");
			iMessage.setVisible(true);
			iMessage.setWordWrap(wrap);
			for (UniTimeHeaderPanel clone: iClones)
				clone.setErrorMessage(message, wrap);
		}
	}
	
	public void setErrorMessage(String message) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("unitime-ErrorMessage");
			iMessage.setVisible(true);
			for (UniTimeHeaderPanel clone: iClones)
				clone.setErrorMessage(message);
		}
	}
	
	public void setMessage(String message) {
		if (message == null || message.isEmpty()) {
			clearMessage();
		} else {
			iLoadingImage.setVisible(false);
			iMessage.setHTML(message);
			iMessage.setStyleName("unitime-Message");
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
		Button button = new Button(name, clickHandler);
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
	
	public void setEnabled(String operation, boolean enabled) {
		Integer op = iOperations.get(operation);
		if (op != null)
			setEnabled(iOperations.get(operation), enabled);
	}
	
	public boolean isEnabled(int button) {
		return ((Button)iButtons.getWidget(button)).isVisible();
	}

	public Boolean isEnabled(String operation) {
		Integer op = iOperations.get(operation);
		return (op == null ? null : isEnabled(op));
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
	
	public HorizontalPanel getPanel() { return iPanel; }

}
