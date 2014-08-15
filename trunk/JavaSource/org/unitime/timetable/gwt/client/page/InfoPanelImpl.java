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
				iInfo.setText(row, 0, pair.getName());
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
