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
	
	
	private List<ClickHandler> iAssignInstructorsClickHandlers = new ArrayList<ClickHandler>();
	
	private Button findButtonAndFixMargins(P panel) {
		panel.getElement().getStyle().setMargin(0, Unit.PX);
		panel.getElement().getStyle().setPadding(0, Unit.PX);
		Button b = null;
		for (int i = 0; i < panel.getWidgetCount(); i++) {
			Widget w = (Widget) panel.getWidget(i);
			if (w instanceof P) {
				P np = (P) w;
				b = findButtonAndFixMargins(np);
			} else if (w instanceof Button) {
				b = (Button) w;
				b.getElement().getStyle().setMargin(0, Unit.PX);
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
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		
		
		if (editable) {
			iHeader.addButton("add", MESSAGES.buttonAssignInstructors(), new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=assignClassInstructors&configId=" + iConfigId);
				}
			});

			findButtonAndFixMargins(iHeader);
			
		}

		

		iAssignInstructorsPanel.addRow(iHeader);

		initWidget(iAssignInstructorsPanel);
		
	}
	
	public void insert(final RootPanel panel) {
		iConfigId = Long.valueOf(panel.getElement().getInnerText());

		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		addAssignInstructorsClickHandler(new  ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=assignClassInstructors&configId=" + iConfigId);
			}
		});
	}


	public void addAssignInstructorsClickHandler(ClickHandler h) {
		iAssignInstructorsClickHandlers.add(h);
	}

			
}
