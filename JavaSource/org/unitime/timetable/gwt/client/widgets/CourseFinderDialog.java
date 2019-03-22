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

import java.util.HashMap;
import java.util.Map;

import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class CourseFinderDialog extends UniTimeDialogBox implements CourseFinder {
	protected static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	protected static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	
	private AriaTextBox iFilter = null;
	private AriaButton iFilterSelect;
	private P iFilterPanel;
	private boolean iEnabled = true;
	
	private CourseFinderTab[] iTabs = null;
	
	private UniTimeTabPanel iTabPanel = null;
	private VerticalPanel iDialogPanel = null;
	private Map<Character, Integer> iTabAccessKeys = new HashMap<Character, Integer>();
	private String iLastFilter = null;
	
	public CourseFinderDialog() {
		super(true, false);
		addStyleName("unitime-CourseFinderDialog");
		setText(MESSAGES.courseSelectionDialog());
		
		iFilter = new AriaTextBox();
		iFilter.setStyleName("gwt-SuggestBox");

		iFilterSelect = new AriaButton(MESSAGES.buttonSelect());
		iFilterSelect.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CourseFinderTab tab = getSelectedTab();
				RequestedCourse rc = (RequestedCourse)(tab == null ? null : tab.getValue());
				if (rc != null)
					iFilter.setValue(rc.toString(CONSTANTS));
				hide();
				SelectionEvent.fire(CourseFinderDialog.this, getValue());
			}
		});
		
		iFilterPanel = new P("filter");
		P filterText = new P("text");
		P filterButton = new P("button");
		iFilterPanel.add(filterButton);
		iFilterPanel.add(filterText);
		filterText.add(iFilter);
		filterButton.add(iFilterSelect);
		
		iDialogPanel = new VerticalPanel();
		iDialogPanel.setSpacing(5);
		iDialogPanel.add(iFilterPanel);
		iDialogPanel.setCellHorizontalAlignment(iFilterPanel, HasHorizontalAlignment.ALIGN_CENTER);

		addCloseHandler(new CloseHandler<PopupPanel>() {
			public void onClose(CloseEvent<PopupPanel> event) {
				RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
			}
		});
		
		final Timer finderTimer = new Timer() {
            @Override
            public void run() {
            	if (iTabs != null) {
            		if (iLastFilter != null && iLastFilter.equals(iFilter.getValue())) return;
            		RequestedCourse rc = new RequestedCourse(); rc.setCourseName(iFilter.getValue());
					for (CourseFinderTab tab: iTabs)
						tab.setValue(rc, false);
					iLastFilter = iFilter.getValue();
				}
            }
		};
		
		iFilter.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				finderTimer.schedule(250);
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					CourseFinderTab tab = getSelectedTab();
					RequestedCourse rc = (RequestedCourse)(tab == null ? null : tab.getValue());
					if (rc != null)
						iFilter.setValue(rc.toString(CONSTANTS));
					hide();
					SelectionEvent.fire(CourseFinderDialog.this, getValue());
					return;
				} else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
					hide();
					return;
				}
				if (event.isControlKeyDown() || event.isAltKeyDown()) {
					for (Map.Entry<Character, Integer> entry: iTabAccessKeys.entrySet())
						if (event.getNativeKeyCode() == Character.toLowerCase(entry.getKey()) || event.getNativeKeyCode() == Character.toUpperCase(entry.getKey())) {
							iTabPanel.selectTab(entry.getValue());
							event.preventDefault();
							event.stopPropagation();
						}
				}
				if (iTabs != null) {
					for (CourseFinderTab tab: iTabs)
						tab.onKeyUp(event);
				}
			}
		});
		
		iFilter.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				RequestedCourse value = new RequestedCourse(); value.setCourseName(event.getValue());
				if (iTabs != null) {
					for (CourseFinderTab tab: iTabs)
						tab.setValue(value, true);
				}
			}
		});
		addValueChangeHandler(new ValueChangeHandler<RequestedCourse>() {
			@Override
			public void onValueChange(ValueChangeEvent<RequestedCourse> event) {
				if (iTabs != null) {
					for (CourseFinderTab tab: iTabs)
						tab.setValue(event.getValue(), true);
				}
			}
		});
		
		iFilter.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				if (isShowing()) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iFilter.setFocus(true);
						}
					});
				}
			}
		});
		
		setWidget(iDialogPanel);
	}
	
	@Override
	public void setValue(RequestedCourse value) {
		setValue(value, false);
	}

	@Override
	public RequestedCourse getValue() {
		RequestedCourse ret = (RequestedCourse)getSelectedTab().getValue();
		if (ret != null) return ret;
		RequestedCourse rc = new RequestedCourse();
		rc.setCourseName(iFilter.getValue());
		return rc;
	}

	@Override
	public void findCourse() {
		iFilter.setAriaLabel(isAllowFreeTime() ? ARIA.courseFinderFilterAllowsFreeTime() : ARIA.courseFinderFilter());
		AriaStatus.getInstance().setText(ARIA.courseFinderDialogOpened());
		if (iTabs != null)
			for (CourseFinderTab tab: iTabs)
				tab.changeTip();
		center();
		RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			public void execute() {
				iFilter.setFocus(true);
			}
		});
	}
	
	protected boolean isAllowFreeTime() {
		if (iTabs == null) return false;
		for (CourseFinderTab tab: iTabs)
			if (!tab.isCourseSelection()) return true;
		return false;
	}

	@Override
	public void setTabs(CourseFinderTab... tabs) {
		iTabs = tabs;
		if (iTabs.length == 1) {
			if (iTabs[0].asWidget() instanceof VerticalPanel) {
				VerticalPanel vp = (VerticalPanel)iTabs[0].asWidget();
				while (vp.getWidgetCount() > 0) {
					Widget w = vp.getWidget(0);
					vp.remove(w);
					iDialogPanel.add(w);
				}
			} else {
				iDialogPanel.add(iTabs[0].asWidget());
			}
		} else {
			iTabPanel = new UniTimeTabPanel();
			int tabIndex = 0;
			for (CourseFinderTab tab: iTabs) {
				iTabPanel.add(tab.asWidget(), tab.getName(), true);
				Character ch = UniTimeHeaderPanel.guessAccessKey(tab.getName());
				if (ch != null)
					iTabAccessKeys.put(ch, tabIndex);
				tabIndex ++;
			}
			iTabPanel.selectTab(0);
			iDialogPanel.add(iTabPanel);
		}
		for (final CourseFinderTab tab: iTabs) {
			tab.addValueChangeHandler(new ValueChangeHandler<RequestedCourse>() {
				@Override
				public void onValueChange(ValueChangeEvent<RequestedCourse> event) {
					if (event.getSource().equals(tab))
						selectTab(tab);
					else
						tab.setValue(event.getValue());
					iFilter.setValue(event.getValue() == null ? "" : event.getValue().toString(CONSTANTS));
				}
			});
			tab.addSelectionHandler(new SelectionHandler<RequestedCourse>() {
				@Override
				public void onSelection(SelectionEvent<RequestedCourse> event) {
					iFilter.setValue(event.getSelectedItem() == null ? "" : event.getSelectedItem().toString(CONSTANTS));
					hide();
					SelectionEvent.fire(CourseFinderDialog.this, getValue());
				}
			});
			tab.addResponseHandler(new ResponseHandler() {
				@Override
				public void onResponse(ResponseEvent event) {
					if (event.isValid()) {
						CourseFinderTab selected = getSelectedTab();
						if (selected != null && selected.isCourseSelection() && selected.getValue() != null) return;
						selectTab(tab);
					}
				}
			});
		}
	}
	
	protected void selectTab(CourseFinderTab tab) {
		if (iTabs != null && iTabs.length > 1) {
			for (int i = 0; i < iTabs.length; i++) {
				if (tab.equals(iTabs[i])) {
					iTabPanel.selectTab(i);
					break;
				}
			}
		}
	}

	@Override
	public HandlerRegistration addSelectionHandler(SelectionHandler<RequestedCourse> handler) {
		return addHandler(handler, SelectionEvent.getType());
	}
	
	protected CourseFinderTab getSelectedTab() {
		if (iTabs == null) return null;
		if (iTabs.length == 1) return iTabs[0];
		else return iTabs[iTabPanel.getSelectedTab()];
	}
	
	@Override
	public void setValue(final RequestedCourse value, boolean fireEvents) {
		iFilter.setValue(value == null ? "" : value.toString(CONSTANTS), false);
		iLastFilter = iFilter.getValue();
		if (fireEvents)
			ValueChangeEvent.fire(this, value);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RequestedCourse> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public boolean isEnabled() {
		return iEnabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		iEnabled = enabled;
		iFilter.setReadOnly(!iEnabled);
		iFilterSelect.setVisible(iEnabled);
		if (iTabPanel != null) {
			for (int i = 0; i < iTabs.length; i++) {
				iTabs[i].setEnabled(enabled);
				iTabPanel.getTabBar().setTabEnabled(i, iEnabled || iTabPanel.getSelectedTab() == i);
			}
		}
		if (iEnabled)
			setText(MESSAGES.courseSelectionDialog());
		else
			setText(MESSAGES.courseSelectionDialogDisabled());
	}
}