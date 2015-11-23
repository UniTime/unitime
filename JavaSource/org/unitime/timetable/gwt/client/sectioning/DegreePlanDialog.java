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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ScrollPanel;

/**
 * @author Tomas Muller
 */
public class DegreePlanDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private SimpleForm iForm;
	private DegreePlanTable iDegreePlanTable;
	private ScrollPanel iDegreePlanPanel;
	private Button iBack;
	private UniTimeHeaderPanel iFooter;
	
	public DegreePlanDialog() {
		super(true, false);
		setEscapeToHide(true);
		addStyleName("unitime-DegreePlanDialog");
		
		iForm = new SimpleForm();
		
		iDegreePlanTable = new DegreePlanTable();
		iDegreePlanPanel = new ScrollPanel(iDegreePlanTable);
		iDegreePlanPanel.setStyleName("unitime-ScrollPanel");
		iDegreePlanPanel.addStyleName("plan");
		iForm.addRow(iDegreePlanPanel);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("apply", MESSAGES.buttonDegreePlanApply(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doApply();
			}
		});
		
		iFooter.addButton("close", MESSAGES.buttonDegreePlanClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iBack = new Button(MESSAGES.buttonDegreePlanBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doBack();
			}
		});
		Character backAck = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonDegreePlanBack());
		if (backAck != null)
			iBack.setAccessKey(backAck);
		ToolBox.setWhiteSpace(iBack.getElement().getStyle(), "nowrap");
		iFooter.getPanel().insert(iBack, 0);
		
		iForm.addBottomRow(iFooter);
		setWidget(iForm);
	}
	
	public void open(DegreePlanInterface plan, boolean hasBack) {
		iDegreePlanTable.setValue(plan);
		setText(MESSAGES.dialogDegreePlan(plan.getName()));
		iBack.setVisible(hasBack); iBack.setEnabled(hasBack);
		center();
	}

	protected void doBack() {
		hide();
	}
	
	protected void doApply() {
		hide();
	}
}
