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

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class DegreePlansSelectionDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static DateTimeFormat sModifiedDateFormat = ServerDateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private SimpleForm iForm;
	private UniTimeTable<DegreePlanInterface> iTable;
	private UniTimeHeaderPanel iFooter;
	private String iLastSubmit = null;

	public DegreePlansSelectionDialog() {
		super(true, true);
		setEscapeToHide(true);
		setEnterToSubmit(new Command() {
			@Override
			public void execute() {
				if (iTable.getSelectedRow() > 0)
					doSubmit(iTable.getData(iTable.getSelectedRow()));
			}
		});
		setText(MESSAGES.dialogSelectDegreePlan());
		
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-SelectDegreePlan");
		
		iTable = new UniTimeTable<DegreePlanInterface>();
		iTable.addStyleName("plans-table");
		iTable.setAllowSelection(true);
		iTable.setAllowMultiSelect(false);
		iForm.addRow(iTable);
		
		iFooter = new UniTimeHeaderPanel();
		iForm.addBottomRow(iFooter);
		
		setWidget(iForm);

		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreePlanName()));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreePlanDegree()));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreePlanLastModified()));
		header.add(new UniTimeTableHeader(MESSAGES.colDegreePlanModifiedBy()));
		iTable.addRow(null, header);
		
		iFooter.addButton("select", MESSAGES.buttonDegreePlanSelect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iTable.getSelectedRow() > 0)
					doSubmit(iTable.getData(iTable.getSelectedRow()));
			}
		});
		
		iFooter.addButton("cancel", MESSAGES.buttonDegreePlanCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();				
			}
		});
		
		iTable.addMouseClickListener(new UniTimeTable.MouseClickListener<DegreePlanInterface>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<DegreePlanInterface> event) {
				if (event.getData() != null)
					doSubmit(event.getData());
			}
		});
	}
	
	public void open(List<DegreePlanInterface> plans) {
		iTable.clearTable(1);
		int select = -1;
		for (DegreePlanInterface plan: plans) {
			List<Widget> row = new ArrayList<Widget>();
			P p = new P("icons");
			if (plan.isLocked()) {
				Image lock = new Image(RESOURCES.locked());
				lock.setTitle(MESSAGES.hintLockedPlan());
				p.add(lock);
			} else if (plan.isActive()) {
				Image active = new Image(RESOURCES.activePlan());
				active.setTitle(MESSAGES.hintActivePlan());
				p.add(active);
			}
			row.add(p);
			row.add(new Label(plan.getName() == null ? "" : plan.getName()));
			row.add(new Label(plan.getDegree() == null ? "" : plan.getDegree()));
			row.add(new Label(plan.getLastModified() == null ? "" : sModifiedDateFormat.format(plan.getLastModified())));
			row.add(new Label(plan.getModifiedWho() == null ? "" : plan.getModifiedWho()));
			if (plan.getId().equals(iLastSubmit))
				select = iTable.getRowCount();
			else if (select < 0 && plan.isActive())
				select = iTable.getRowCount();
			iTable.addRow(plan, row);
			
		}
		iTable.setSelected(select < 0 ? 1 : select, true);
		center();
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			@Override
			public void execute() {
				iFooter.setFocus("select", true);
			}
		});
		updateAriaStatus(true);
	}
	
	@Override
	public void show() {
		super.show();
		updateAriaStatus(true);
	}
	
	public void doSubmit(DegreePlanInterface plan) {
		if (plan != null)
			AriaStatus.getInstance().setText(ARIA.selectedDegreePlan(plan.getName(), plan.getDegree()));
		iLastSubmit = plan.getId();
		hide();
	}
	
	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
		super.onPreviewNativeEvent(event);
		if (event.getTypeInt() == Event.ONKEYDOWN) {
			if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_UP) {
				int row = iTable.getSelectedRow();
				if (row >= 0)
					iTable.setSelected(row, false);
				row --;
				if (row <= 0) row = iTable.getRowCount() - 1;
				iTable.setSelected(row, true);
				updateAriaStatus(false);
			} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
				int row = iTable.getSelectedRow();
				if (row >= 0)
					iTable.setSelected(row, false);
				row ++;
				if (row >= iTable.getRowCount()) row = 1;
				iTable.setSelected(row, true);
				updateAriaStatus(false);
			}
		}
	}
	
	protected void updateAriaStatus(boolean justOpened) {
		String text = "";
		if (justOpened)
			text = ARIA.showingDegreePlans(iTable.getRowCount() - 1);
		int row = iTable.getSelectedRow();
		DegreePlanInterface plan = iTable.getData(row);
		if (row >= 0 && plan != null) {
			text += (text.isEmpty() ? "" : " ") + ARIA.showingDegreePlan(row, iTable.getRowCount() - 1, plan.getName(), plan.getDegree(), plan.getLastModified(), plan.getModifiedWho());
		}
		AriaStatus.getInstance().setText(text);
	}
}
