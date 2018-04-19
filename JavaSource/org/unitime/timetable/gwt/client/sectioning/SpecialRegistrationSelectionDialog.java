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
import java.util.Collections;
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
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

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
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class SpecialRegistrationSelectionDialog extends UniTimeDialogBox {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);
	private static DateTimeFormat sModifiedDateFormat = ServerDateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private SimpleForm iForm;
	private UniTimeTable<RetrieveSpecialRegistrationResponse> iTable;
	private UniTimeHeaderPanel iFooter;
	private SpecialRegistrationContext iSpecReg;

	public SpecialRegistrationSelectionDialog(SpecialRegistrationContext specReg) {
		super(true, true);
		iSpecReg = specReg;
		setEscapeToHide(true);
		setEnterToSubmit(new Command() {
			@Override
			public void execute() {
				if (iTable.getSelectedRow() > 0)
					doSubmit(iTable.getData(iTable.getSelectedRow()));
			}
		});
		setText(MESSAGES.dialogSpecialRegistrations());
		
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-SpecialRegistrations");
		
		iTable = new UniTimeTable<RetrieveSpecialRegistrationResponse>();
		iTable.addStyleName("plans-table");
		iTable.setAllowSelection(true);
		iTable.setAllowMultiSelect(false);
		iForm.addRow(iTable);
		
		iFooter = new UniTimeHeaderPanel();
		iForm.addBottomRow(iFooter);
		
		setWidget(iForm);

		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegSubmitted()));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegName()));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegNote()));
		iTable.addRow(null, header);
		
		
		iFooter.addButton("create", MESSAGES.buttonSpecRegCreateNew(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doSubmit(null);
			}
		});
		
		iFooter.addButton("select", MESSAGES.buttonSpecRegSelect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iTable.getSelectedRow() > 0)
					doSubmit(iTable.getData(iTable.getSelectedRow()));
			}
		});
		
		iFooter.addButton("cancel", MESSAGES.buttonSpecRegCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();				
			}
		});
		
		iTable.addMouseClickListener(new UniTimeTable.MouseClickListener<RetrieveSpecialRegistrationResponse>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<RetrieveSpecialRegistrationResponse> event) {
				if (event.getData() != null)
					doSubmit(event.getData());
			}
		});
	}
	
	public void open(List<RetrieveSpecialRegistrationResponse> registrations) {
		iFooter.setEnabled("create", iSpecReg.isSpecRegMode() && iSpecReg.hasRequestKey() && iSpecReg.isSpecRegRequestKeyValid());
		iTable.clearTable(1);
		int select = -1;
		Collections.sort(registrations);
		for (RetrieveSpecialRegistrationResponse reg: registrations) {
			List<Widget> row = new ArrayList<Widget>();
			P p = new P("icons");
			if (reg.getStatus() != null) {
				switch (reg.getStatus()) {
				case Approved:
					Image approved = new Image(RESOURCES.specRegApproved());
					approved.setTitle(MESSAGES.hintSpecRegApproved());
					p.add(approved);
					break;
				case Cancelled:
					Image cancelled = new Image(RESOURCES.specRegCancelled());
					cancelled.setTitle(MESSAGES.hintSpecRegCancelled());
					p.add(cancelled);
					break;
				case Pending:
					Image pending = new Image(RESOURCES.specRegPending());
					pending.setTitle(MESSAGES.hintSpecRegPending());
					p.add(pending);
					break;
				case Rejected:
					Image denied = new Image(RESOURCES.specRegRejected());
					denied.setTitle(MESSAGES.hintSpecRegRejected());
					p.add(denied);
					break;
				case Draft:
					Image draft = new Image(RESOURCES.specRegDraft());
					draft.setTitle(MESSAGES.hintSpecRegDraft());
					p.add(draft);
					break;
				}
			} else if (reg.isCanEnroll()) {
				Image canEnroll = new Image(RESOURCES.specRegCanEnroll());
				canEnroll.setTitle(MESSAGES.hintSpecRegCanEnroll());
				p.add(canEnroll);
			} else if (!reg.isCanSubmit()) {
				Image canNotSubmit = new Image(RESOURCES.specRegCanNotSubmit());
				canNotSubmit.setTitle(MESSAGES.hintSpecRegCanNotSubmit());
				p.add(canNotSubmit);
			} 
			row.add(p);
			row.add(new Label(reg.getSubmitDate() == null ? "" : sModifiedDateFormat.format(reg.getSubmitDate())));
			row.add(new Label(reg.getDescription() == null ? "" : reg.getDescription()));
			row.add(new HTML(reg.getNote() == null ? "" : reg.getNote()));
			if (reg.getRequestId().equals(iSpecReg.getRequestId()))
				select = iTable.getRowCount();
			iTable.addRow(reg, row);
			
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
	
	public void doSubmit(RetrieveSpecialRegistrationResponse reg) {
		if (reg != null)
			AriaStatus.getInstance().setText(ARIA.selectedSpecReg(reg.getDescription()));
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
				iTable.getRowFormatter().getElement(row).scrollIntoView();
				updateAriaStatus(false);
			} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_DOWN) {
				int row = iTable.getSelectedRow();
				if (row >= 0)
					iTable.setSelected(row, false);
				row ++;
				if (row >= iTable.getRowCount()) row = 1;
				iTable.setSelected(row, true);
				iTable.getRowFormatter().getElement(row).scrollIntoView();
				updateAriaStatus(false);
			}
		}
	}
	
	protected void updateAriaStatus(boolean justOpened) {
		String text = "";
		if (justOpened)
			text = ARIA.showingSpecRegs(iTable.getRowCount() - 1);
		int row = iTable.getSelectedRow();
		RetrieveSpecialRegistrationResponse reg = iTable.getData(row);
		if (row >= 0 && reg != null) {
			text += (text.isEmpty() ? "" : " ") + ARIA.showingSpecReg(row, iTable.getRowCount() - 1, reg.getDescription(), reg.getSubmitDate());
		}
		AriaStatus.getInstance().setText(text);
	}
}