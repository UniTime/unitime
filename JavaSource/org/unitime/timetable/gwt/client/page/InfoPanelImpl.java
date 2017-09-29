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

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ClickableHint;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.shared.MenuInterface.InfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.InfoPairInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * @author Tomas Muller
 */
public class InfoPanelImpl extends P implements InfoPanelDisplay {
	private String iUrl = null;
	private P iText;
	private ClickableHint iHint;
	private FlexTable iInfo;
	private PopupPanel iInfoPanel = null;
	private Timer iShowInfo, iHideInfo = null;
	private int iX, iY;
	private Callback iUpdateInfo = null;
	private ClickHandler iDefaultClickHandler;
	private HandlerRegistration iTextClick, iHintClick;
	
	public InfoPanelImpl() {
		super("cell");
		iText = new P("text");
		add(iText);
		
		iHint = new ClickableHint(""); iHint.setStyleName("hint");
		add(iHint);
		
		iUpdateInfo = new Callback() {
			@Override
			public void execute(Callback callback) {
				if (callback != null) callback.execute(null);
			}
		};
		
		iInfo = new FlexTable();
		iInfo.setStyleName("unitime-InfoTable");
		// iUpdateInfo = updateInfo;
		iInfoPanel = new PopupPanel();
		iInfoPanel.setWidget(iInfo);
		iInfoPanel.setStyleName("unitime-PopupHint");
		sinkEvents(Event.ONMOUSEOVER);
		sinkEvents(Event.ONMOUSEOUT);
		sinkEvents(Event.ONMOUSEMOVE);
		iShowInfo = new Timer() {
			@Override
			public void run() {
				if (iInfo.getRowCount() == 0) return;
				iUpdateInfo.execute(new Callback() {
					public void execute(Callback callback) {
						iInfoPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
							@Override
							public void setPosition(int offsetWidth, int offsetHeight) {
								int maxX = Window.getScrollLeft() + Window.getClientWidth() - offsetWidth - 10;
								iInfoPanel.setPopupPosition(Math.min(iX, maxX), iY);
							}
						});
						if (callback != null) callback.execute(null);
					}
				});
			}
		};
		iHideInfo = new Timer() {
			@Override
			public void run() {
				iInfoPanel.hide();
			}
		};
		
		iDefaultClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iUrl != null && !iUrl.isEmpty())
					ToolBox.open(GWT.getHostPageBaseURL() + iUrl);
			}
		};
		iTextClick = iHint.addClickHandler(iDefaultClickHandler);
		iHintClick = iText.addClickHandler(iDefaultClickHandler);
		iHint.setTabIndex(-1);
	}

	@Override
	public String getHint() {
		return iHint.getText();
	}

	@Override
	public void setHint(String hint) {
		iHint.setText(hint);
	}

	@Override
	public String getText() {
		return iText.getText();
	}

	@Override
	public void setText(String text) {
		iText.setText(text);
	}

	@Override
	public void setInfo(InfoInterface info) {
		iInfo.clear(true);
		int row = 0;
		if (info != null)
			for (InfoPairInterface pair: info.getPairs()) {
				if (pair.getValue() == null || pair.getValue().isEmpty()) continue;
				iInfo.setHTML(row, 0, pair.getName());
				iInfo.setHTML(row, 1, pair.getValue());
				if (pair.hasSeparator()) {
					iInfo.getCellFormatter().addStyleName(row, 0, "separator");
					iInfo.getCellFormatter().addStyleName(row, 1, "separator");
				}
				row++;
			}
	}

	public void onBrowserEvent(Event event) {
		if (iHint.getText().isEmpty()) return;
		iX = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
		iY = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
		
		switch (DOM.eventGetType(event)) {
		case Event.ONMOUSEMOVE:
			if (iInfoPanel.isShowing()) {
				int maxX = Window.getScrollLeft() + Window.getClientWidth() - iInfoPanel.getOffsetWidth() - 10;
				iInfoPanel.setPopupPosition(Math.min(iX, maxX), iY);
			} else if (iInfo.getRowCount() > 0) {
				iShowInfo.cancel();
				iShowInfo.schedule(1000);
			}
			break;
		case Event.ONMOUSEOUT:
			iShowInfo.cancel();
			if (iInfoPanel.isShowing())
				iHideInfo.schedule(1000);
			break;
		}
	}

	@Override
	public void setCallback(Callback callback) {
		iUpdateInfo = callback;
	}

	@Override
	public void setUrl(String url) {
		iUrl = url;
		if (iUrl != null && !iUrl.isEmpty()) {
			iText.addStyleName("clickable");
			iHint.addStyleName("clickable");
			iHint.setTabIndex(0);
		}
		
	}

	@Override
	public boolean isPopupShowing() {
		return iInfoPanel.isShowing();
	}
	
	@Override
	public void setClickHandler(ClickHandler clickHandler) {
		iTextClick.removeHandler();
		iHintClick.removeHandler();
		if (clickHandler == null) {
			if (iUrl != null && !iUrl.isEmpty()) {
				iText.addStyleName("clickable");
				iHint.addStyleName("clickable");
				iHint.setTabIndex(0);
			}
			iTextClick = iHint.addClickHandler(iDefaultClickHandler);
			iHintClick = iText.addClickHandler(iDefaultClickHandler);
		} else {
			iText.addStyleName("clickable");
			iHint.addStyleName("clickable");
			iHint.setTabIndex(0);
			iTextClick = iHint.addClickHandler(clickHandler);
			iHintClick = iText.addClickHandler(clickHandler);
		}
	}

	@Override
	public String getAriaLabel() {
		return iHint.getAriaLabel();
	}

	@Override
	public void setAriaLabel(String text) {
		iHint.setAriaLabel(text);
	}
}
