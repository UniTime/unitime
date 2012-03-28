/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EventDetail extends Composite {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private EventInterface iEvent = null;
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iMeetingsHeader, iNotesHeader;
	
	private UniTimeTable<ContactInterface> iContacts;
	private UniTimeTable<MeetingInterface> iMeetings;
	private UniTimeTable<NoteInterface> iNotes;
	
	private Label iEventType, iLastChange, iSponsor, iEmail;
	
	public EventDetail() {
		iForm = new SimpleForm();
		
		iHeader = new UniTimeHeaderPanel();
		iForm.addHeaderRow(iHeader);
		
		iEventType = new Label("", false);
		iForm.addRow("Event Type:", iEventType);
		
		iContacts = new UniTimeTable<ContactInterface>();
		iContacts.setStyleName("unitime-EventContacts");
		iForm.addRow("Contact:", iContacts); 
		
		List<Widget> contactHeader = new ArrayList<Widget>();
		contactHeader.add(new UniTimeTableHeader("Name"));
		contactHeader.add(new UniTimeTableHeader("Email"));
		contactHeader.add(new UniTimeTableHeader("Phone"));
		iContacts.addRow(null, contactHeader);
		
		iEmail = new Label("", false);
		iForm.addRow("Additional Emails:", iEmail);

		iSponsor = new Label("", false);
		iForm.addRow("Sponsoring Organization:", iSponsor);
		
		iLastChange = new Label("", false);
		iForm.addRow("Last Change:", iLastChange);
		
		iMeetingsHeader = new UniTimeHeaderPanel("Meetings");
		iForm.addHeaderRow(iMeetingsHeader);
		
		iMeetings = new UniTimeTable<MeetingInterface>();
		iMeetings.setStyleName("unitime-EventMeetings");
		iForm.addRow(iMeetings);
		
		iNotesHeader = new UniTimeHeaderPanel("Notes");
		iForm.addHeaderRow(iNotesHeader);
		
		List<Widget> meetingsHeader = new ArrayList<Widget>();
		meetingsHeader.add(new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER));
		meetingsHeader.add(new UniTimeTableHeader("Date"));
		meetingsHeader.add(new UniTimeTableHeader("Time"));
		meetingsHeader.add(new UniTimeTableHeader("Location"));
		meetingsHeader.add(new UniTimeTableHeader("Capacity"));
		meetingsHeader.add(new UniTimeTableHeader("Approved"));
		iMeetings.addRow(null, meetingsHeader);
		
		iNotes = new UniTimeTable<NoteInterface>();
		iNotes.setStyleName("unitime-EventNotes");
		iForm.addRow(iNotes);

		List<Widget> notesHeader = new ArrayList<Widget>();
		notesHeader.add(new UniTimeTableHeader("Date"));
		notesHeader.add(new UniTimeTableHeader("User"));
		notesHeader.add(new UniTimeTableHeader("Action"));
		notesHeader.add(new UniTimeTableHeader("Meetings"));
		notesHeader.add(new UniTimeTableHeader("Note"));
		iNotes.addRow(null, notesHeader);
		
		// iFooter = iHeader.clonePanel();
		// iForm.addNotPrintableBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	public void setEvent(EventInterface event) {
		iEvent = event;
		iHeader.clearMessage();
		iHeader.setHeaderTitle(iEvent.getName());
		iEventType.setText(iEvent.getType().getName());
		
		iContacts.clearTable(1);
		if (iEvent.hasContact()) {
			List<Label> row = new ArrayList<Label>();
			row.add(new Label(iEvent.getContact().getName(), false));
			row.add(new Label(iEvent.getContact().hasEmail() ? iEvent.getContact().getEmail() : "", false));
			row.add(new Label(iEvent.getContact().hasPhone() ? iEvent.getContact().getPhone() : "", false));
			iContacts.addRow(iEvent.getContact(), row);
		}
		if (iEvent.hasAdditionalContacts()) {
			for (ContactInterface contact: iEvent.getAdditionalContacts()) {
				List<Label> row = new ArrayList<Label>();
				row.add(new Label(contact.getName(), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new Label(contact.hasPhone() ? contact.getPhone() : "", false));
				for (Label label: row) label.addStyleName("main-contact");
				iContacts.addRow(contact, row);
			}
		}
		
		if (iEvent.hasLastChange()) {
			iLastChange.setText(iEvent.getLastChange());
			iForm.getRowFormatter().setVisible(iForm.getRow("Last Change:"), true);
		} else {
			iLastChange.setText("");
			iForm.getRowFormatter().setVisible(iForm.getRow("Last Change:"), false);
		}
		
		if (iEvent.hasSponsor()) {
			iSponsor.setText(iEvent.getSponsor().getName());
			iForm.getRowFormatter().setVisible(iForm.getRow("Sponsoring Organization:"), true);
		} else {
			iSponsor.setText("");
			iForm.getRowFormatter().setVisible(iForm.getRow("Sponsoring Organization:"), false);
		}
		
		if (iEvent.hasEmail()) {
			iEmail.setText(iEvent.getEmail());
			iForm.getRowFormatter().setVisible(iForm.getRow("Additional Emails:"), true);
		} else { 
			iEmail.setText("");
			iForm.getRowFormatter().setVisible(iForm.getRow("Additional Emails:"), false);
		}

		iMeetings.clearTable(1);
		for (MeetingInterface meeting: iEvent.getMeetings()) {
			List<Widget> row = new ArrayList<Widget>();
			row.add(new CheckBox());
			row.add(new Label(meeting.getMeetingDate()));
			row.add(new Label(meeting.getMeetingTime()));
			if (meeting.getLocation() == null) {
				row.add(new Label(""));
				row.add(new Label(""));
			} else {
				row.add(new Label(meeting.getLocationName()));
				row.add(new Label(meeting.getLocation().getSize() == null ? "N/A" : meeting.getLocation().getSize().toString()));
			}
			row.add(new Label(meeting.getApprovalDate() == null ? "not approved" : sDateFormat.format(meeting.getApprovalDate())));
			if (!meeting.isApproved())
				row.get(row.size() - 1).addStyleName("not-approved");
			int r = iMeetings.addRow(meeting, row);
			if (meeting.isPast())
				iMeetings.getRowFormatter().addStyleName(r, "past-meeting");
		}
		
		iNotes.clearTable(1);
		if (iEvent.hasNotes()) {
			iNotesHeader.setVisible(true);
			iNotes.setVisible(true);
			for (NoteInterface note: iEvent.getNotes()) {
				List<Widget> row = new ArrayList<Widget>();
				row.add(new Label(sTimeStampFormat.format(note.getDate()), false));
				row.add(new HTML(note.getUser() == null ? "<i>N/A</i>" : note.getUser(), false));
				row.add(new Label(note.getType().getName()));
				row.add(new HTML(note.getMeetings() == null ? "<i>N/A</i>" : note.getMeetings(), false));
				row.add(new HTML(note.getNote() == null ? "" : note.getNote().replace("\n", "<br>"), true));
				int r = iNotes.addRow(note, row);
				iNotes.getRowFormatter().addStyleName(r, note.getType().getName().toLowerCase());
			}
		} else {
			iNotesHeader.setVisible(false);
			iNotes.setVisible(false);
		}
	}

}
