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

import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EventDetail extends Composite {
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private EventInterface iEvent = null;
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private UniTimeTable<ContactInterface> iContacts;
	private MeetingTable iMeetings;
	private UniTimeTable<NoteInterface> iNotes;
	private UniTimeTable<RelatedObjectInterface> iOwners;
	private EnrollmentTable iEnrollments;
	
	public EventDetail() {
		iForm = new SimpleForm();
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("back", "<u>B</u>ack", 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		iContacts = new UniTimeTable<ContactInterface>();
		iContacts.setStyleName("unitime-EventContacts");
		
		List<Widget> contactHeader = new ArrayList<Widget>();
		contactHeader.add(new UniTimeTableHeader("Name"));
		contactHeader.add(new UniTimeTableHeader("Email"));
		contactHeader.add(new UniTimeTableHeader("Phone"));
		iContacts.addRow(null, contactHeader);
		
		iMeetings = new MeetingTable();
		
		iOwners = new UniTimeTable<RelatedObjectInterface>();
		iOwners.setStyleName("unitime-EventOwners");

		List<Widget> ownersHeader = new ArrayList<Widget>();
		ownersHeader.add(new UniTimeTableHeader("Course"));
		ownersHeader.add(new UniTimeTableHeader("Section"));
		ownersHeader.add(new UniTimeTableHeader("Type"));
		ownersHeader.add(new UniTimeTableHeader("Date"));
		ownersHeader.add(new UniTimeTableHeader("Time"));
		ownersHeader.add(new UniTimeTableHeader("Room"));
		ownersHeader.add(new UniTimeTableHeader("Instructor"));
		iOwners.addRow(null, ownersHeader);
		
		iEnrollments = new EnrollmentTable(false, true);
		iEnrollments.getTable().setStyleName("unitime-Enrollments");
		
		iNotes = new UniTimeTable<NoteInterface>();
		iNotes.setStyleName("unitime-EventNotes");

		List<Widget> notesHeader = new ArrayList<Widget>();
		notesHeader.add(new UniTimeTableHeader("Date"));
		notesHeader.add(new UniTimeTableHeader("User"));
		notesHeader.add(new UniTimeTableHeader("Action"));
		notesHeader.add(new UniTimeTableHeader("Meetings"));
		notesHeader.add(new UniTimeTableHeader("Note"));
		iNotes.addRow(null, notesHeader);
		
		iFooter = iHeader.clonePanel();
		
		initWidget(iForm);
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName("Event Detail");
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
	}
	
	public void hide() {
		setVisible(false);
		onHide();
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	protected void onHide() {
	}
	
	protected void onShow() {
	}

	public void setEvent(EventInterface event) {
		iEvent = event;
		
		iForm.clear();

		iHeader.clearMessage();
		iHeader.setHeaderTitle(iEvent.getName() + " (" + iEvent.getType().getName() + ")");
		iForm.addHeaderRow(iHeader);
		
		iForm.addRow("Event Type:", new Label(iEvent.getType().getName()));
		
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
		if (iContacts.getRowCount() > 1)
			iForm.addRow("Contact:", iContacts);
		
		if (iEvent.hasEmail()) {
			iForm.addRow("Additional Emails:", new Label(iEvent.getEmail()));
		}

		if (iEvent.hasSponsor()) {
			iForm.addRow("Sponsoring Organization:", new Label(iEvent.getSponsor().getName()));
		}

		if (iEvent.hasEnrollments()) {
			iForm.addRow("Enrollment:", new Label(String.valueOf(iEvent.getEnrollments().size())));
			int conf = 0;
			for (Enrollment enrollment: iEvent.getEnrollments()) {
				if (enrollment.hasConflict()) { conf ++; }
			}
			if (conf > 0) {
				iForm.addRow("Student Conflicts:", new Label(String.valueOf(conf)));
			}
		}
		
		if (iEvent.hasMaxCapacity()) {
			iForm.addRow("Event Attendance:", new Label(iEvent.getMaxCapacity().toString()));
		}
		
		if (iEvent.hasLastChange()) {
			iForm.addRow("Last Change:", new Label(iEvent.getLastChange()));
		}
		
		iMeetings.clearTable(1);
		for (MeetingInterface meeting: iEvent.getMeetings()) {
			iMeetings.add(meeting);
		}
		if (iMeetings.getRowCount() > 1) {
			iForm.addHeaderRow("Meetings");
			iForm.addRow(iMeetings);
		}
		
		iNotes.clearTable(1);
		if (iEvent.hasNotes()) {
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
		}
		if (iNotes.getRowCount() > 1) {
			iForm.addHeaderRow("Notes");
			iForm.addRow(iNotes);
		}

		iOwners.clearTable(1);
		if (iEvent.hasRelatedObjects()) {
			for (RelatedObjectInterface obj: iEvent.getRelatedObjects()) {
				List<Widget> row = new ArrayList<Widget>();
				String course = "";
				if (obj.hasCourseNames()) {
					for (String cn: obj.getCourseNames()) {
						if (course.isEmpty()) {
							course += cn;
						} else {
							course += "<span class='cross-list'>" + cn + "</span>";
						}
					}
				} else {
					course = obj.getName();
				}
				row.add(new HTML(course, false));
				
				String section = "";
				if (obj.hasExternalIds()) {
					for (String ex: obj.getExternalIds()) {
						if (section.isEmpty()) {
							section += ex;
						} else {
							section += "<span class='cross-list'>" + ex + "</span>";
						}
					}
				} else if (obj.hasSectionNumber()) {
					section = obj.getSectionNumber();
				}
				row.add(new HTML(section, false));
				
				String type = (obj.hasInstruction() ? obj.getInstruction() : obj.getType().name());
				row.add(new Label(type, false));
				
				if (obj.hasDate()) {
					row.add(new Label(obj.getDate(), false));
				} else {
					row.add(new Label());
				}
				
				if (obj.hasTime()) {
					row.add(new Label(obj.getTime(), false));
				} else {
					row.add(new Label());
				}
				
				String location = "";
				if (obj.hasLocations()) {
					for (ResourceInterface loc: obj.getLocations()) {
						location += (location.isEmpty() ? "" : "<br>") + loc.getName();
					}
				}
				row.add(new HTML(location, false));

				if (obj.hasInstructors()) {
					row.add(new HTML(obj.getInstructorNames("<br>"), false));
				} else {
					row.add(new HTML());
				}
				
				iOwners.addRow(obj, row);
			}
		}
		if (iOwners.getRowCount() > 1) {
			iForm.addHeaderRow("Relations");
			iForm.addRow(iOwners);
		}
		
		iEnrollments.clear();
		if (iEvent.hasEnrollments()) {
			iEnrollments.populate(iEvent.getEnrollments(), false);
			iForm.addHeaderRow("Enrollments");
			iForm.addRow(iEnrollments.getTable());
		}

		iForm.addNotPrintableBottomRow(iFooter);
	}

}
