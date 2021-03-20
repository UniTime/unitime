package org.unitime.timetable.gwt.client.events;

import org.unitime.timetable.gwt.client.sectioning.SectioningStatusCookie;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.TextArea;

public class EventStudentEmail extends UniTimeDialogBox{
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private UniTimeTextBox iSubject, iCC;
	private TextArea iMessage;
	private UniTimeHeaderPanel iButtons;
	private SimpleForm iForm;
	private Command iCommand;
	
	public EventStudentEmail() {
		super(true, false);
		addStyleName("unitime-StudentStatusDialog");
		setEscapeToHide(true);
		
		iSubject = new UniTimeTextBox(512, 473);

		iCC = new UniTimeTextBox(512, 473);
		if (SectioningStatusCookie.getInstance().hasEmailCC())
			iCC.setText(SectioningStatusCookie.getInstance().getEmailCC());

		iMessage = new TextArea();
		iMessage.setStyleName("unitime-TextArea");
		iMessage.setVisibleLines(10);
		iMessage.setCharacterWidth(80);
		
		iButtons = new UniTimeHeaderPanel();
		iButtons.addButton("send-email", MESSAGES.emailSend(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
				SectioningStatusCookie.getInstance().setEmailCC(getCC());
				iCommand.execute();
			}
		});
		
		iForm  = new SimpleForm();
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iForm.addRow(MESSAGES.emailSubject(), iSubject);
		iForm.addRow(MESSAGES.emailCC(), iCC);
		iForm.addRow(MESSAGES.emailBody(), iMessage);

		iForm.addBottomRow(iButtons);
		setText(MESSAGES.sendStudentEmail());
		
		setWidget(iForm);
	}
	
	public void setCommand(Command command) {
		iCommand = command;
	}
	
	public String getSubject() {
		return iSubject.getText();
	}
	
	public void setSubject(String subject) {
		iSubject.setText(subject);
	}
	
	public String getCC() {
		return iCC.getText();
	}
	
	public void setCC(String cc) {
		iCC.setText(cc);
	}
	
	public String getMessage() {
		return iMessage.getText();
	}
}
