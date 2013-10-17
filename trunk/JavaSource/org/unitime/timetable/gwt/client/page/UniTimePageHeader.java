/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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

import java.util.HashMap;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.sectioning.AcademicSessionSelector;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Tomas Muller
 */
public class UniTimePageHeader extends Composite {
	private final MenuServiceAsync iService = GWT.create(MenuService.class);

	private HorizontalPanel iPanel = new HorizontalPanel();
	private VerticalPanelWithHint iSolverInfo, iSessionInfo, iUserInfo;
	
	public UniTimePageHeader() {
		iSolverInfo = new VerticalPanelWithHint(new Callback() {
			public void execute(Callback callback) {
				reloadSolverInfo(true, callback);
			}
		});
		iPanel.add(iSolverInfo);
		iPanel.setCellHorizontalAlignment(iSolverInfo, HasHorizontalAlignment.ALIGN_LEFT);
		iSolverInfo.getElement().getStyle().setPaddingRight(30, Unit.PX);
		
		iUserInfo = new VerticalPanelWithHint(new Callback() {
			public void execute(Callback callback) {
				if (callback != null) callback.execute(null);
			}
		});
		iPanel.add(iUserInfo);
		iPanel.setCellHorizontalAlignment(iUserInfo, HasHorizontalAlignment.ALIGN_CENTER);
		iUserInfo.getElement().getStyle().setPaddingRight(30, Unit.PX);
		
		iSessionInfo = new VerticalPanelWithHint(new Callback() {
			public void execute(Callback callback) {
				if (callback != null) callback.execute(null);
			}
		});
		iPanel.add(iSessionInfo);
		iPanel.setCellHorizontalAlignment(iSessionInfo, HasHorizontalAlignment.ALIGN_RIGHT);
		
		initWidget(iPanel);
		
		reloadSessionInfo();
		reloadUserInfo();
		reloadSolverInfo(false, null);
	}
	
	public void setSessionSelector(AcademicSessionSelector selector) {
		iPanel.remove(iSessionInfo);
		iPanel.add(selector);
		iPanel.setCellHorizontalAlignment(iSessionInfo, HasHorizontalAlignment.ALIGN_RIGHT);
	}
	
	public void insert(final RootPanel panel) {
		if (panel.getWidgetCount() > 0) return;
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void hideSessionInfo() {
		iSessionInfo.setVisible(false);
	}
	
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
				Anchor hint = new Anchor("Click here to change the session / role.", true);
				hint.setStyleName("unitime-Hint");
				iSessionInfo.add(hint);
				ClickHandler c = new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						ToolBox.open(GWT.getHostPageBaseURL() + "selectPrimaryRole.do?list=Y");
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
				hint.setStyleName(result.containsKey("Chameleon") ? "unitime-Hint" : "unitime-NotClickableHint");
				iUserInfo.add(hint);
				if (result.containsKey("Chameleon")) {
					ClickHandler c = new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							ToolBox.open(GWT.getHostPageBaseURL() + "chameleon.do");
						}
					};
					userLabel.addClickHandler(c);
					hint.addClickHandler(c);
				} else {
					ClickHandler c = new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							ToolBox.open(GWT.getHostPageBaseURL() + "selectPrimaryRole.do?list=Y");
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
	
	public void reloadSolverInfo(boolean includeSolutionInfo, final Callback callback) {
		iService.getSolverInfo(includeSolutionInfo, new AsyncCallback<HashMap<String,String>>() {
			@Override
			public void onSuccess(HashMap<String, String> result) {
				iSolverInfo.clear();
				boolean hasSolver = false;
				try {
					iSolverInfo.setHint(result);
					if (result != null) {
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
									ToolBox.open(GWT.getHostPageBaseURL() + "solver.do");
								else if (type.equals("Examinations Solver"))
									ToolBox.open(GWT.getHostPageBaseURL() + "examSolver.do");
								else
									ToolBox.open(GWT.getHostPageBaseURL() + "studentSolver.do");
							}
						};
						userLabel.addClickHandler(c);
						hint.addClickHandler(c);
						hasSolver = true;
					}
				} catch (Exception e) {}
				Timer t = new Timer() {
					@Override
					public void run() {
						reloadSolverInfo(iSolverInfo.iHintPanel.isShowing(), null);
					}
				};
				t.schedule(hasSolver ? 1000 : 60000);
				if (callback != null) 
					callback.execute(null);
			}
			@Override
			public void onFailure(Throwable caught) {
				Timer t = new Timer() {
					@Override
					public void run() {
						reloadSolverInfo(iSolverInfo.iHintPanel.isShowing(), null);
					}
				};
				t.schedule(5000);
				if (callback != null) 
					callback.execute(null);
			}
		});
	}
	
	public static interface Callback {
		public void execute(Callback callback);
	}
	
	public static class VerticalPanelWithHint extends VerticalPanel {
		private PopupPanel iHintPanel = null;
		private Timer iShowHint, iHideHint = null;
		private HTML iHint = null;
		private int iX, iY;
		private Callback iUpdateInfo = null;
		
		public VerticalPanelWithHint(Callback updateInfo) {
			super();
			iUpdateInfo = updateInfo;
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
					iUpdateInfo.execute(new Callback() {
						public void execute(Callback callback) {
							iHintPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
								@Override
								public void setPosition(int offsetWidth, int offsetHeight) {
									int maxX = Window.getScrollLeft() + Window.getClientWidth() - offsetWidth - 10;
									iHintPanel.setPopupPosition(Math.min(iX, maxX), iY);
								}
							});		
							if (callback != null) callback.execute(null);
						}
					});
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
				TreeSet<String> keys = new TreeSet<String>(hint.keySet());
				for (String key: keys) {
					String val = hint.get(key);
					if (val.isEmpty()) continue;
					String style = "";
					if (key.startsWith("A")) 
						style = "border-top: 1px dashed #AB8B00;";
					html += "<tr><td style=\"" + style + "\">" + key.substring(1) + ":</td><td style=\"" + style + "\">" + val + "</td></tr>";
				}
				html += "</table>";
			}
			iHint.setHTML(html);
		}
		
		public void onBrowserEvent(Event event) {
			if (iHint.getText().isEmpty()) return;
			iX = 10 + event.getClientX() + getElement().getOwnerDocument().getScrollLeft();
			iY = 10 + event.getClientY() + getElement().getOwnerDocument().getScrollTop();
			
			switch (DOM.eventGetType(event)) {
			case Event.ONMOUSEMOVE:
				if (iHintPanel.isShowing()) {
					int maxX = Window.getScrollLeft() + Window.getClientWidth() - iHintPanel.getOffsetWidth() - 10;
					iHintPanel.setPopupPosition(Math.min(iX, maxX), iY);
				} else {
					iShowHint.cancel();
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
