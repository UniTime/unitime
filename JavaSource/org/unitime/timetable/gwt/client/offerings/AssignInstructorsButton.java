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
package org.unitime.timetable.gwt.client.offerings;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ReservationInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Stephanie Schluttenhofer
 */
public class AssignInstructorsButton extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private Long iConfigId = null;
	
	private SimpleForm iAssignInstructorsPanel;
	private UniTimeHeaderPanel iHeader;
	
	
	private List<AssignInstructorClickHandler> iAssignInstructorClickHandlers = new ArrayList<AssignInstructorClickHandler>();
	
	private Button findButtonAndFixMargins(P panel, int depth) {
		panel.getElement().getStyle().setMargin(0, Unit.PX);
		panel.getElement().getStyle().setPadding(0, Unit.PX);
		Button b = null;
		int level = depth;
		for (int i = 0; i < panel.getWidgetCount(); i++) {
			Widget w = (Widget) panel.getWidget(i);
			if (w instanceof P) {
				P np = (P) w;
				b = findButtonAndFixMargins(np, level + 1);
			} else if (w instanceof Button) {
				b = (Button) w;
				b.getElement().getStyle().setMargin(0, Unit.PX);
//				b.getElement().getStyle().setMarginLeft(1, Unit.PX);
//				b.getElement().getStyle().setMarginRight(0, Unit.PX);
//				b.getElement().getStyle().setMarginTop(0, Unit.PX);
//				b.getElement().getStyle().setMarginBottom(0, Unit.PX);
//				b.getElement().getStyle().setPadding(0, Unit.PX);
			} else if (w instanceof UIObject){
				UIObject o = (UIObject) w;
				o.getElement().getStyle().setMargin(0, Unit.PX);
				o.getElement().getStyle().setPadding(0, Unit.PX);
			} 
			if (b != null) {
				break;
			}
		}
		return b;
	}
	public AssignInstructorsButton(boolean editable) {
		iAssignInstructorsPanel = new SimpleForm();
		iAssignInstructorsPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel("");
//		iHeader.removeStyleName("unitime-HeaderPanel");
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
//		iHeader.setStyleName("unitime-ButtonPanel");
		
		
		if (editable) {
			iHeader.addButton("add", MESSAGES.buttonAssignInstructors(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=assignClassInstructors&configId=" + iConfigId);
				}
			});
			
			findButtonAndFixMargins(iHeader, 1);
			iAssignInstructorsPanel.getElement().getStyle().setMargin(0, Unit.PX);
			iAssignInstructorsPanel.getElement().getStyle().setPadding(0, Unit.PX);
			
		}

		

		iAssignInstructorsPanel.addRow(iHeader);

		initWidget(iAssignInstructorsPanel);
		
	}
	
	public void insert(final RootPanel panel) {
		iConfigId = Long.valueOf(panel.getElement().getInnerText());
//	
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		addAssignInstructorClickHandler(new AssignInstructorClickHandler() {
			@Override
			public void onClick(AssignInstructorClickedEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=assignClassInstructors&configId=" + iConfigId);
			}
		});
	}


	public static class AssignInstructorClickedEvent {
		private ReservationInterface iReservation;
		
		public AssignInstructorClickedEvent(ReservationInterface reservation) {
			iReservation = reservation;
		}
		
		public ReservationInterface getReservation() {
			return iReservation;
		}
	}
	
	public interface AssignInstructorClickHandler {
		public void onClick(AssignInstructorClickedEvent evt);
	}
	
	public void addAssignInstructorClickHandler(AssignInstructorClickHandler h) {
		iAssignInstructorClickHandlers.add(h);
	}

		
	public void setErrorMessage(String message) {
		iHeader.setErrorMessage(message);
	}
			
}
