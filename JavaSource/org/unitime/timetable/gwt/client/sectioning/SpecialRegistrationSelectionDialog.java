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
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ErrorMessage;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.RetrieveSpecialRegistrationResponse;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
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
		iTable.addStyleName("registrations-table");
		iTable.setAllowSelection(true);
		iTable.setAllowMultiSelect(true);
		iForm.addRow(iTable);
		
		iFooter = new UniTimeHeaderPanel();
		iForm.addBottomRow(iFooter);
		
		setWidget(iForm);

		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegSubmitted()));
		header.add(new UniTimeTableHeader(MESSAGES.colSubject()));
		header.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		header.add(new UniTimeTableHeader(MESSAGES.colSubpart()));
		header.add(new UniTimeTableHeader(MESSAGES.colClass()));
		header.add(new UniTimeTableHeader(MESSAGES.colLimit()));
		header.add(new UniTimeTableHeader(MESSAGES.colCredit()));
		header.add(new UniTimeTableHeader(MESSAGES.colSpecRegErrors()));
		header.add(new UniTimeTableHeader(""));
		iTable.addRow(null, header);
		
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
		iTable.clearTable(1);
		Collections.sort(registrations);
		for (RetrieveSpecialRegistrationResponse reg: registrations) {
			P p = new P("icons");
			if (reg.getStatus() != null) {
				switch (reg.getStatus()) {
				case Approved:
					p.add(new Icon(RESOURCES.specRegApproved(), MESSAGES.hintSpecRegApproved()));
					break;
				case Cancelled:
					p.add(new Icon(RESOURCES.specRegCancelled(), MESSAGES.hintSpecRegCancelled()));
					break;
				case Pending:
					p.add(new Icon(RESOURCES.specRegPending(), MESSAGES.hintSpecRegPending()));
					break;
				case Rejected:
					p.add(new Icon(RESOURCES.specRegRejected(), MESSAGES.hintSpecRegRejected()));
					break;
				case Draft:
					p.add(new Icon(RESOURCES.specRegDraft(), MESSAGES.hintSpecRegDraft()));
					break;
				}
			} 
			if (reg.hasChanges()) {
				Long lastCourseId = null;
				for (ClassAssignment ca: reg.getChanges()) {
					List<Widget> row = new ArrayList<Widget>();
					if (lastCourseId == null) {
						row.add(p);
						row.add(new DateAndNoteCell(reg.getSubmitDate(), reg.getNote()));
					} else {
						row.add(new P("icons"));
						row.add(new Label());
					}
					if (lastCourseId == null || !lastCourseId.equals(ca.getCourseId())) {
						row.add(new Label(ca.getSubject()));
						row.add(new Label(ca.getCourseNbr()));
					} else {
						row.add(new Label());
						row.add(new Label());
					}
					row.add(new Label(ca.getSubpart()));
					row.add(new Label(ca.getSection()));
					row.add(new HTML(ca.getLimitString()));
					row.add(new CreditCell(ca.getCredit()));
					Label errorsLabel = new Label(ca.hasError() ? ca.getError() : ""); errorsLabel.addStyleName("registration-errors");
					row.add(errorsLabel);
					P s = new P("icons");
					if (ca.isCourseAssigned()) {
						s.add(new Icon(RESOURCES.assignment(), MESSAGES.assignment(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
					} else {
						s.add(new Icon(RESOURCES.unassignment(), MESSAGES.unassignment(ca.getSubject() + " " + ca.getCourseNbr() + " " + ca.getSubpart() + " " + ca.getSection())));
					}
					row.add(s);
					int idx = iTable.addRow(reg, row);
					if (reg.getRequestId().equals(iSpecReg.getRequestId()))
						iTable.setSelected(idx, true);
					if (idx > 1 && lastCourseId == null)
						for (int c = 0; c < iTable.getCellCount(idx); c++)
							iTable.getCellFormatter().addStyleName(idx, c, "top-border-solid");
					if (lastCourseId != null && !lastCourseId.equals(ca.getCourseId()))
						for (int c = 0; c < iTable.getCellCount(idx); c++)
							iTable.getCellFormatter().addStyleName(idx, c, "top-border-dashed");
					if (!ca.isCourseAssigned()) {
						for (int c = 0; c < iTable.getCellCount(idx); c++)
							iTable.getCellFormatter().addStyleName(idx, c, "change-drop");
					} else  {
						for (int c = 0; c < iTable.getCellCount(idx); c++)
							iTable.getCellFormatter().addStyleName(idx, c, "change-add");
					}
					lastCourseId = ca.getCourseId();
				}
			} else {
				List<Widget> row = new ArrayList<Widget>();
				row.add(p);
				row.add(new DateAndNoteCell(reg.getSubmitDate(), reg.getNote()));
				row.add(new DescriptionCell(reg.getDescription()));
				String errors = "";
				if (reg.hasClassAssignments() && reg.getClassAssignments().hasErrors()) {
					for (ErrorMessage err: reg.getClassAssignments().getErrors()) {
						if (!errors.isEmpty()) errors += "\n";
						errors += err.getCourse() + (err.getSection() == null ? "" : " " + err.getSection()) + ": " + err.getMessage();
					}
				}
				Label errorsLabel = new Label(errors); errorsLabel.addStyleName("registration-errors");
				row.add(errorsLabel);
				row.add(new Label());
				int idx = iTable.addRow(reg, row);
				if (reg.getRequestId().equals(iSpecReg.getRequestId()))
					iTable.setSelected(idx, true);
				if (idx > 1) iTable.getRowFormatter().addStyleName(idx, "top-border-solid");
			}
		}
		center();
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
	
	protected class DateAndNoteCell extends Label {
		
		public DateAndNoteCell(Date date, String note) {
			super(date == null ? note == null ? "" : note : sModifiedDateFormat.format(date) + (note == null || note.isEmpty() ? "" : "\n" + note));
			addStyleName("date-and-note");
		}
	}
	
	protected class DescriptionCell extends Label implements UniTimeTable.HasColSpan {
		
		public DescriptionCell(String text) {
			super(text == null ? "" : text);
		}
	
		@Override
		public int getColSpan() {
			return 6;
		}
		
	}
	
	protected class Icon extends Image {
		public Icon(ImageResource image, final String text) {
			super(image);
			if (text != null && !text.isEmpty()) {
				setAltText(text);
				setTitle(text);
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						event.preventDefault(); event.stopPropagation();
						UniTimeConfirmationDialog.info(text);
					}
				});
			}
		}
	}
	
	protected class CreditCell extends HTML {
		public CreditCell(String text) {
			if (text != null && text.indexOf('|') >= 0) {
				setHTML(text.substring(0, text.indexOf('|')));
				setTitle(text.substring(text.indexOf('|') + 1).replace("\n", "<br>"));
			} else {
				setHTML(text == null ? "" : text.replace("\n", "<br>"));
				if (text != null) setTitle(text);
			}
		}
	}
}