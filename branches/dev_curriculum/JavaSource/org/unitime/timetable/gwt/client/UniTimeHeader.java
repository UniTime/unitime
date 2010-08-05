/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client;

import java.util.HashMap;
import java.util.TreeSet;

import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class UniTimeHeader extends Composite {
	private final MenuServiceAsync iService = GWT.create(MenuService.class);

	private HorizontalPanel iPanel = new HorizontalPanel();
	private VerticalPanelWithHint iSolverInfo, iSessionInfo, iUserInfo;
	
	public UniTimeHeader() {
		iSolverInfo = new VerticalPanelWithHint();
		iPanel.add(iSolverInfo);
		iPanel.setCellHorizontalAlignment(iSolverInfo, HasHorizontalAlignment.ALIGN_LEFT);
		iSolverInfo.getElement().getStyle().setPaddingRight(30, Unit.PX);
		
		iUserInfo = new VerticalPanelWithHint();
		iPanel.add(iUserInfo);
		iPanel.setCellHorizontalAlignment(iUserInfo, HasHorizontalAlignment.ALIGN_CENTER);
		iUserInfo.getElement().getStyle().setPaddingRight(30, Unit.PX);
		
		iSessionInfo = new VerticalPanelWithHint();
		iPanel.add(iSessionInfo);
		iPanel.setCellHorizontalAlignment(iSessionInfo, HasHorizontalAlignment.ALIGN_RIGHT);
		
		initWidget(iPanel);
		
		reloadSessionInfo();
		reloadUserInfo();
		reloadSolverInfo();
	}
	
	public void insert(final RootPanel panel) {
		if (panel.getWidgetCount() > 0) return;
		panel.add(this);
		panel.setVisible(true);
	}
	
	private native void open(String url) /*-{
		$wnd.location = url;
	}-*/;

	public void reloadSessionInfo() {
		iService.getSessionInfo(new AsyncCallback<HashMap<String,String>>() {
			@Override
			public void onSuccess(HashMap<String, String> result) {
				iSessionInfo.clear();
				iSessionInfo.setHint(result);
				if (result == null) return;
				HTML sessionLabel = new HTML(result.get("0Session"), false);
				sessionLabel.setStyleName("unitime-SessionSelector");
				iSessionInfo.add(sessionLabel);
				HTML hint = new HTML("Click here to change the session / role.", false);
				hint.setStyleName("unitime-Hint");
				iSessionInfo.add(hint);
				ClickHandler c = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						open(GWT.getHostPageBaseURL() + "selectPrimaryRole.do?list=Y");
					}
				};
				sessionLabel.addClickHandler(c);
				hint.addClickHandler(c);
			}
			@Override
			public void onFailure(Throwable caught) {
				iSessionInfo.clear();
				iSessionInfo.setHint(null);
			}
		});
	}

	public void reloadUserInfo() {
		iService.getUserInfo(new AsyncCallback<HashMap<String,String>>() {
			@Override
			public void onSuccess(HashMap<String, String> result) {
				iUserInfo.clear();
				iUserInfo.setHint(result);
				if (result == null) return;
				HTML userLabel = new HTML(result.get("0Name"), false);
				userLabel.setStyleName("unitime-SessionSelector");
				iUserInfo.add(userLabel);
				HTML hint = new HTML(result.get("2Role"), false);
				hint.setStyleName("unitime-Hint");
				iUserInfo.add(hint);
				if (result.containsKey("Chameleon")) {
					ClickHandler c = new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							open(GWT.getHostPageBaseURL() + "chameleon.do");
						}
					};
					userLabel.addClickHandler(c);
					hint.addClickHandler(c);
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				iUserInfo.clear();
				iUserInfo.setHint(null);
			}
		});
	}
	
	public void reloadSolverInfo() {
		iService.getSolverInfo(new AsyncCallback<HashMap<String,String>>() {
			@Override
			public void onSuccess(HashMap<String, String> result) {
				iSolverInfo.clear();
				iSolverInfo.setHint(result);
				if (result == null) {
					Timer t = new Timer() {
						@Override
						public void run() {
							reloadSolverInfo();
						}
					};
					t.schedule(1000);
					return;
				}
				HTML userLabel = new HTML(result.get("1Solver"), false);
				userLabel.setStyleName("unitime-SessionSelector");
				iSolverInfo.add(userLabel);
				HTML hint = new HTML(result.get("0Type"), false);
				hint.setStyleName("unitime-Hint");
				iSolverInfo.add(hint);
				final String type = result.get("0Type");
				ClickHandler c = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (type.equals("Course Timetabling Solver"))
							open(GWT.getHostPageBaseURL() + "solver.do");
						else if (type.equals("Examinations Solver"))
							open(GWT.getHostPageBaseURL() + "examSolver.do");
						else
							open(GWT.getHostPageBaseURL() + "studentSolver.do");
					}
				};
				userLabel.addClickHandler(c);
				hint.addClickHandler(c);
				Timer t = new Timer() {
					@Override
					public void run() {
						reloadSolverInfo();
					}
				};
				t.schedule(1000);
			}
			@Override
			public void onFailure(Throwable caught) {
				iSolverInfo.clear();
				iSolverInfo.setHint(null);
			}
		});
	}
	
	public static class VerticalPanelWithHint extends VerticalPanel {
		private PopupPanel iHintPanel = null;
		private Timer iShowHint, iHideHint = null;
		private HTML iHint = null;
		
		public VerticalPanelWithHint() {
			super();
			iHint = new HTML("", false);
			iHintPanel = new PopupPanel();
			iHintPanel.setWidget(iHint);
			iHintPanel.setStyleName("unitime-PopupHint");
			sinkEvents(Event.ONMOUSEOVER);
			sinkEvents(Event.ONMOUSEOUT);
			sinkEvents(Event.ONMOUSEMOVE);
			iShowHint = new Timer() {
				@Override
				public void run() {
					iHintPanel.show();
				}
			};
			iHideHint = new Timer() {
				@Override
				public void run() {
					iHintPanel.hide();
				}
			};
		}
		
		public void setHint(HashMap<String,String> hint) {
			String html = "";
			if (hint != null && !hint.isEmpty()) {
				html += "<table cellspacing=\"0\" cellpadding=\"3\">";
				try {
					TreeSet<String> keys = new TreeSet<String>(hint.keySet());
					for (String key: keys) {
						String val = hint.get(key);
						if (val.isEmpty()) continue;
						String style = "";
						if (key.startsWith("A")) 
							style = "border-top: 1px dashed #AB8B00;";
						html += "<tr><td style=\"" + style + "\">" + key.substring(1) + ":</td><td style=\"" + style + "\">" + val + "</td></tr>";
					}
				} catch (Exception e) {}
				html += "</table>";
			}
			iHint.setHTML(html);
		}
		
		public void onBrowserEvent(Event event) {
			if (iHint.getText().isEmpty()) return;
			int x = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
			int y = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
			
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEMOVE:
				if (iHintPanel.isShowing()) {
					iHintPanel.setPopupPosition(x, y);
				} else {
					iShowHint.cancel();
					iHintPanel.setPopupPosition(x, y);
					iShowHint.schedule(1000);
				}
				break;
			case Event.ONMOUSEOUT:
				iShowHint.cancel();
				if (iHintPanel.isShowing())
					iHideHint.schedule(1000);
				break;
			}
		}		
		
	}
}
