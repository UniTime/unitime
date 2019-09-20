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

import java.util.Set;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class StudentStatusDialog extends UniTimeDialogBox{
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	public static final GwtMessages GWT_MESSAGES = GWT.create(GwtMessages.class);
	private UniTimeTextBox iSubject, iCC;
	private CheckBox iCourseRequests, iClassSchedule;
	private TextArea iMessage, iNote;
	private Set<StudentStatusInfo> iStates;
	private ListBox iStatus;
	private int iStatusRow;
	private UniTimeHeaderPanel iButtons;
	private SimpleForm iForm;
	private Command iCommand;
	
	public StudentStatusDialog(Set<StudentStatusInfo> states) {
		super(true, false);
		iStates = states;
		addStyleName("unitime-StudentStatusDialog");
		setEscapeToHide(true);
		
		iCourseRequests = new CheckBox(MESSAGES.mailIncludeCourseRequests());
		iCourseRequests.setValue(SectioningStatusCookie.getInstance().isEmailIncludeCourseRequests());
		iClassSchedule = new CheckBox(MESSAGES.mailIncludeClassSchedule());
		iClassSchedule.setValue(SectioningStatusCookie.getInstance().isEmailIncludeClassSchedule());
		
		iSubject = new UniTimeTextBox(512, 473);
		iSubject.setText(MESSAGES.defaulSubject());
		if (SectioningStatusCookie.getInstance().hasEmailSubject())
			iSubject.setText(SectioningStatusCookie.getInstance().getEmailSubject());
		iCC = new UniTimeTextBox(512, 473);
		if (SectioningStatusCookie.getInstance().hasEmailCC())
			iCC.setText(SectioningStatusCookie.getInstance().getEmailCC());
		iMessage = new TextArea();
		iMessage.setStyleName("unitime-TextArea");
		iMessage.setVisibleLines(10);
		iMessage.setCharacterWidth(80);
		iNote = new TextArea();
		iNote.setStyleName("unitime-TextArea");
		iNote.setVisibleLines(10);
		iNote.setCharacterWidth(80);
		
		iStatus = new ListBox();
		iStatus.addItem(MESSAGES.statusNoChange(), "-");
		iStatus.setSelectedIndex(0);
		for (StudentStatusInfo s: iStates)
			iStatus.addItem(s.getLabel(), s.getReference());

		iStatus.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				statusChanged();
			}
		});
		
		iButtons = new UniTimeHeaderPanel();
		iButtons.addButton("set-note", MESSAGES.buttonSetNote(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				iCommand.execute();
			}
		});
		iButtons.addButton("send-email", MESSAGES.emailSend(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				SectioningStatusCookie.getInstance().setEmailDefaults(getIncludeCourseRequests(), getIncludeClassSchedule(), getCC(), getSubject());
				iCommand.execute();
			}
		});
		iButtons.addButton("mass-cancel", MESSAGES.buttonMassCancel(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				iCommand.execute();
			}
		});
		iButtons.addButton("set-status", MESSAGES.buttonSetStatus(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				iCommand.execute();
			}
		});
		iButtons.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm  = new SimpleForm();
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		setWidget(iForm);
	}
	
	protected void statusChanged() {
		while (iForm.getRowCount() > iStatusRow + 1)
			iForm.removeRow(iStatusRow + 1);
		String statusRef = iStatus.getValue(iStatus.getSelectedIndex());
		StudentStatusInfo status = null;
		for (StudentStatusInfo s: iStates)
			if (statusRef.equals(s.getReference())) { status = s; break; }
		if (status != null) {
			WebTable table = new WebTable(); table.addStyleName("unitime-StatusAccess");
			table.setHeader(new WebTable.Row(
					new WebTable.Cell(MESSAGES.colPage(), 1, "75px"),
					new WebTable.Cell(MESSAGES.colCanOpen(), 1, "50px"),
					new WebTable.Cell(MESSAGES.colCanStudentChange(), 1, "50px"),
					new WebTable.Cell(MESSAGES.colCanAdvisorChange(), 1, "50px"),
					new WebTable.Cell(MESSAGES.colCanAdminChange(), 1, "50px")
				));
			table.setData(
					new WebTable.Row(
							new WebTable.Cell(GWT_MESSAGES.pageStudentCourseRequests()),
							new WebTable.IconCell(status.isCanAccessRequestsPage() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAccessRequestsPage() ? MESSAGES.messageStatusCanAccessCourseRequests() : MESSAGES.messageStatusCanNotAccessCourseRequests(), null),
							new WebTable.IconCell(status.isCanStudentRegister() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanStudentRegister() ? MESSAGES.messageStatusStudentsCanRegister() : MESSAGES.messageStatusStudentsCanNotRegister(), null),
							new WebTable.IconCell(status.isCanAdvisorRegister() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAdvisorRegister() ? MESSAGES.messageStatusAdvisorsCanRegister() : MESSAGES.messageStatusAdvisorsCanNotRegister(), null),
							new WebTable.IconCell(status.isCanAdminRegister() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAdminRegister() ? MESSAGES.messageStatusAdminsCanRegister() : MESSAGES.messageStatusAdminsCanNotRegister(), null)),
					new WebTable.Row(
							new WebTable.Cell(GWT_MESSAGES.pageStudentSchedulingAssistant()),
							new WebTable.IconCell(status.isCanAccessAssistantPage() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAccessAssistantPage() ? MESSAGES.messageStatusCanAccessSchedulingAssistant() : MESSAGES.messageStatusCanNotAccessSchedulingAssistant(), null),
							new WebTable.IconCell(status.isCanStudentEnroll() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanStudentEnroll() ? MESSAGES.messageStatusStudentsCanEnroll() : MESSAGES.messageStatusStudentsCanNotEnroll(), null),
							new WebTable.IconCell(status.isCanAdvisorEnroll() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAdvisorEnroll() ? MESSAGES.messageStatusAdvisorsCanEnroll() : MESSAGES.messageStatusAdvisorsCanNotEnroll(), null),
							new WebTable.IconCell(status.isCanAdminEnroll() ? RESOURCES.on() : RESOURCES.off(),
									status.isCanAdminEnroll() ? MESSAGES.messageStatusAdminsCanEnroll() : MESSAGES.messageStatusAdminsCanNotEnroll(), null)));
			iForm.addRow(MESSAGES.propPermissions(), table);
			iForm.addRow(MESSAGES.propWaitLists(), new WebTable.IconCell(status.isWaitList() ? RESOURCES.on() : RESOURCES.off(),
					status.isWaitList() ? MESSAGES.messageStatusCanWaitList() : MESSAGES.messageStatusCanNotWaitList(), null).getWidget());
			iForm.addRow(MESSAGES.propEmailNotification(), new WebTable.IconCell(status.isEmail() ? RESOURCES.on() : RESOURCES.off(),
					status.isEmail() ? MESSAGES.messageStatusCanEmail() : MESSAGES.messageStatusCanNotEmail(), null).getWidget());
			iForm.addRow(MESSAGES.propCourseRequestValidation(), new WebTable.IconCell(status.isRequestValiadtion() ? RESOURCES.on() : RESOURCES.off(),
					status.isRequestValiadtion() ? MESSAGES.messageStatusCanRequestValidation() : MESSAGES.messageStatusCanNotRequestValidation(), null).getWidget());
			iForm.addRow(MESSAGES.propSpecialRegistration(), new WebTable.IconCell(status.isSpecialRegistration() ? RESOURCES.on() : RESOURCES.off(),
					status.isSpecialRegistration() ? MESSAGES.messageStatusCanSpecialRegistration() : MESSAGES.messageStatusCanNotSpecialRegistration(), null).getWidget());
			iForm.addRow(MESSAGES.propCanRequire(), new WebTable.IconCell(status.isCanRequire() ? RESOURCES.on() : RESOURCES.off(),
					status.isCanRequire() ? MESSAGES.messageStatusCanRequire() : MESSAGES.messageStatusCanNotRequire(), null).getWidget());
			iForm.addRow(MESSAGES.propStatusSchedule(), new WebTable.IconCell(status.isNoSchedule() ? RESOURCES.off() : RESOURCES.on(),
					status.isNoSchedule() ? MESSAGES.messageStatusNoSchedule() : MESSAGES.messageStatusSchedule(), null).getWidget());
			
			if (status.hasMessage()) {
				P m = new P("status-message"); m.setText(status.getMessage());
				iForm.addRow(MESSAGES.propStatusMessage(), m);
			}
			if (status.hasEffectiveStart() || status.hasEffectiveStop()) {
				if (!status.hasEffectiveStart())
					iForm.addRow(MESSAGES.propEffectivePeriod(), new Label(MESSAGES.messageEffectivePeriodBefore(status.getEffectiveStop())));
				else if (!status.hasEffectiveStop())
					iForm.addRow(MESSAGES.propEffectivePeriod(), new Label(MESSAGES.messageEffectivePeriodAfter(status.getEffectiveStart())));
				else
					iForm.addRow(MESSAGES.propEffectivePeriod(), new Label(MESSAGES.messageEffectivePeriodBetween(status.getEffectiveStart(), status.getEffectiveStop())));
			}
			if (status.hasFallback())
				iForm.addRow(MESSAGES.propFallbackStatus(), new Label(status.getFallback()));
			if (status.hasCourseTypes())
				iForm.addRow(MESSAGES.propCourseTypes(), new Label(status.getCourseTypes()));
		}
		iForm.addBottomRow(iButtons);
	}
	
	public Set<StudentStatusInfo> getStatuses() { return iStates; }
	
	public void setStudentNote(Command command) {
		iCommand = command;
		iForm.clear();
		iForm.addRow(MESSAGES.propNote(), iNote);
		iStatusRow = iForm.addRow(MESSAGES.newStatus(), iStatus);
		iStatus.setSelectedIndex(0);
		iForm.addBottomRow(iButtons);
		iButtons.setEnabled("set-note", true);
		iButtons.setEnabled("send-email", false);
		iButtons.setEnabled("mass-cancel", false);
		iButtons.setEnabled("set-status", false);
		setText(MESSAGES.setStudentNote());
		statusChanged();
		center();
	}
	
	public void sendStudentEmail(Command command) {
		iCommand = command;
		iForm.clear();
		iForm.addRow(MESSAGES.emailSubject(), iSubject);
		if (iSubject.getText().isEmpty() || iSubject.getText().equals(MESSAGES.defaulSubjectMassCancel())) {
			iSubject.setText(MESSAGES.defaulSubject());
			if (SectioningStatusCookie.getInstance().hasEmailSubject())
				iSubject.setText(SectioningStatusCookie.getInstance().getEmailSubject());
		}
		iForm.addRow(MESSAGES.emailCC(), iCC);
		iForm.addRow(MESSAGES.emailBody(), iMessage);
		P panel = new P();
		panel.add(iCourseRequests); panel.add(iClassSchedule);
		iForm.addRow(MESSAGES.emailInclude(), panel);
		iForm.addBottomRow(iButtons);
		iButtons.setEnabled("set-note", false);
		iButtons.setEnabled("send-email", true);
		iButtons.setEnabled("mass-cancel", false);
		iButtons.setEnabled("set-status", false);
		setText(MESSAGES.sendStudentEmail());
		center();
	}
	
	public void massCancel(Command command) {
		iCommand = command;
		iForm.clear();
		if (iSubject.getText().isEmpty() || iSubject.getText().equals(MESSAGES.defaulSubject()) || iSubject.getText().equals(SectioningStatusCookie.getInstance().getEmailSubject()))
			iSubject.setText(MESSAGES.defaulSubjectMassCancel());
		iForm.addRow(MESSAGES.emailSubject(), iSubject);
		iForm.addRow(MESSAGES.emailCC(), iCC);
		iForm.addRow(MESSAGES.emailBody(), iMessage);
		iStatus.setSelectedIndex(0);
		for (int i = 0; i < iStatus.getItemCount(); i++)
			if ("Cancelled".equalsIgnoreCase(iStatus.getValue(i))) {
				iStatus.setSelectedIndex(i);
				break;
			}
		iStatusRow = iForm.addRow(MESSAGES.newStatus(), iStatus);
		iForm.addBottomRow(iButtons);
		iButtons.setEnabled("set-note", false);
		iButtons.setEnabled("send-email", false);
		iButtons.setEnabled("mass-cancel", true);
		iButtons.setEnabled("set-status", false);
		setText(MESSAGES.massCancel());
		statusChanged();
		center();
	}
	
	public void setStatus(Command command) {
		iCommand = command;
		iForm.clear();
		iStatusRow = iForm.addRow(MESSAGES.newStatus(), iStatus);
		iForm.addBottomRow(iButtons);
		iButtons.setEnabled("set-note", false);
		iButtons.setEnabled("send-email", false);
		iButtons.setEnabled("mass-cancel", false);
		iButtons.setEnabled("set-status", true);
		setText(MESSAGES.setStudentStatus());
		statusChanged();
		center();
	}
	
	public String getStatus() {
		return iStatus.getValue(iStatus.getSelectedIndex());
	}
	
	public StudentStatusInfo getStudentStatusInfo(String ref) {
		for (StudentStatusInfo status: iStates)
			if (status.getReference().equals(ref))
				return status;
		return null;
	}
	
	public String getNote() {
		return iNote.getText();
	}
	
	public String getSubject() {
		return iSubject.getText();
	}
	
	public String getCC() {
		return iCC.getText();
	}
	
	public String getMessage() {
		return iMessage.getText();
	}
	
	public Boolean getIncludeCourseRequests() {
		return iCourseRequests.getValue();
	}
	
	public Boolean getIncludeClassSchedule() {
		return iClassSchedule.getValue();
	}

}
