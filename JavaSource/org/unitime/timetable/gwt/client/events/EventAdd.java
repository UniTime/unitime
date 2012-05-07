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

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.GetEnrollmentsFromRelatedObjectsRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SelectionInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface.RelatedObjectType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class EventAdd extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	
	private TextBox iName, iLimit;
	private ListBox iSponsors, iEventType;
	private TextArea iNotes, iEmails;
	private TextBox iMainFName, iMainMName, iMainLName, iMainPhone, iMainEmail;
	private CheckBox iReqAttendance;
	
	private SimpleForm iCoursesForm;
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter, iMeetingsHeader;
	
	private MeetingTable iMeetings;
	
	private CourseRelatedObjectsTable iCourses;
	
	private AddMeetingsDialog iEventAddMeetings;
	private AcademicSessionProvider iSession;
	private Lookup iLookup;
	
	private EnrollmentTable iEnrollments;
	private UniTimeHeaderPanel iEnrollmentHeader;
	private int iEnrollmentRow;
			
	public EventAdd(AcademicSessionProvider session) {
		iSession = session;
		iForm = new SimpleForm();
		
		iLookup = new Lookup();
		iLookup.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				if (event.getValue() != null) {
					iMainFName.setText(event.getValue().getFirstName() == null ? "" : event.getValue().getFirstName());
					iMainMName.setText(event.getValue().getMiddleName() == null ? "" : event.getValue().getMiddleName());
					iMainLName.setText(event.getValue().getLastName() == null ? "" : event.getValue().getLastName());
					iMainPhone.setText(event.getValue().getPhone() == null ? "" : event.getValue().getPhone());
					iMainEmail.setText(event.getValue().getEmail() == null ? "" : event.getValue().getEmail());
				}
			}
		});
		iLookup.setOptions("mustHaveExternalId" + (iSession.getAcademicSessionId() == null ? "" : ",session=" + iSession.getAcademicSessionId()));
		iSession.addAcademicSessionChangeHandler(new AcademicSessionProvider.AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionProvider.AcademicSessionChangeEvent event) {
				iLookup.setOptions("mustHaveExternalId,session=" + event.getNewAcademicSessionId());
			}
		});
		
		iHeader = new UniTimeHeaderPanel("Event");
		iHeader.addButton("lookup", "<u>L</u>ookup Contact", 125, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iLookup.setQuery((iMainFName.getText() + (iMainMName.getText().isEmpty() ? "" : " " + iMainMName.getText()) + " " + iMainLName.getText()).trim()); 
				iLookup.center();
			}
		});
		iHeader.addButton("back", "<u>B</u>ack", 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		iForm.addHeaderRow(iHeader);
		
		iName = new TextBox();
		iName.setStyleName("unitime-TextBox");
		iName.setMaxLength(100);
		iName.setWidth("480px");
		iForm.addRow("Event Name:", iName);
		
		iSponsors = new ListBox();
		iForm.addRow("Sponsoring Organization:", iSponsors);
		
		iEventType = new ListBox();
		iEventType.addItem("Special Event");
		iEventType.addItem("Course Related Event");
		iForm.addRow("Event Type:", iEventType);
		
		iLimit = new TextBox();
		iLimit.setStyleName("unitime-TextBox");
		iLimit.setMaxLength(10);
		iLimit.setWidth("50px");
		iForm.addRow("Expected Attendance:", iLimit);

		iCourses = new CourseRelatedObjectsTable(iSession) {
			@Override
			public void setErrorMessage(String message) {
				iHeader.setErrorMessage(message);
			}
		};
		
		iCourses.addValueChangeHandler(new ValueChangeHandler<List<RelatedObjectInterface>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<RelatedObjectInterface>> event) {
				checkEnrollments(event.getValue(), iMeetings.getValue());
			}
		});
		
		iReqAttendance = new CheckBox("Students are required to attend this event.");
						
		SimpleForm mainContact = new SimpleForm();
		mainContact.removeStyleName("unitime-NotPrintableBottomLine");
		
		iMainFName = new TextBox();
		iMainFName.setStyleName("unitime-TextBox");
		iMainFName.setMaxLength(100);
		iMainFName.setWidth("280px");
		mainContact.addRow("First Name:", iMainFName);
		
		iMainMName = new TextBox();
		iMainMName.setStyleName("unitime-TextBox");
		iMainMName.setMaxLength(100);
		iMainMName.setWidth("280px");
		mainContact.addRow("Middle Name:", iMainMName);
		
		iMainLName = new TextBox();
		iMainLName.setStyleName("unitime-TextBox");
		iMainLName.setMaxLength(100);
		iMainLName.setWidth("280px");
		mainContact.addRow("Last Name:", iMainLName);
		
		iMainEmail = new TextBox();
		iMainEmail.setStyleName("unitime-TextBox");
		iMainEmail.setMaxLength(200);
		iMainEmail.setWidth("280px");
		mainContact.addRow("Email:", iMainEmail);
		
		iMainPhone = new TextBox();
		iMainPhone.setStyleName("unitime-TextBox");
		iMainPhone.setMaxLength(35);
		iMainPhone.setWidth("280px");
		mainContact.addRow("Phone:", iMainPhone);
		
		iForm.addRow("Main Contact:", mainContact);
		
		iEmails = new TextArea();
		iEmails.setStyleName("unitime-TextArea");
		iEmails.setVisibleLines(3);
		iEmails.setCharacterWidth(80);
		iForm.addRow("Additional Emails:", iEmails);
		
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.setVisibleLines(5);
		iNotes.setCharacterWidth(80);
		iForm.addRow("Additional Information:", iNotes);
		
		iCoursesForm = new SimpleForm();
		iCoursesForm.addHeaderRow("Courses / Classes");
		iCoursesForm.removeStyleName("unitime-NotPrintableBottomLine");
		iCoursesForm.addRow(iCourses);
		iCoursesForm.addRow(iReqAttendance);
		iForm.addRow(iCoursesForm);
		
		iEventType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				int row = iForm.getRow("Expected Attendance:");
				if (iEventType.getSelectedIndex() == 1) {
					iCoursesForm.setVisible(true);
					iForm.getRowFormatter().setVisible(row, false);
				} else {
					iCoursesForm.setVisible(false);
					iForm.getRowFormatter().setVisible(row, true);
				}
				checkEnrollments(iCourses.getValue(), iMeetings.getValue());
			}
		});
		
		iEventAddMeetings = new AddMeetingsDialog(session, new AsyncCallback<List<MeetingInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage(caught.getMessage());
			}

			@Override
			public void onSuccess(List<MeetingInterface> result) {
				LoadingWidget.getInstance().show("Checking room availability...");
				RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(result, iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(EventRoomAvailabilityRpcResponse result) {
						LoadingWidget.getInstance().hide();
						addMeetings(result.getMeetings());
					}
				});
				// addMeetings(result);
			}
		});
		
		iMeetingsHeader = new UniTimeHeaderPanel("Meetings");
		iMeetingsHeader.addButton("add", "<u>A</u>dd", 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEventAddMeetings.showDialog();
			}
		});
		iForm.addHeaderRow(iMeetingsHeader);
		
		iMeetings = new MeetingTable();
		iMeetings.setAddMeetingsCommand(new Command() {
			@Override
			public void execute() {
				iEventAddMeetings.showDialog();
			}
		});
		iMeetings.addValueChangeHandler(new ValueChangeHandler<List<MeetingInterface>>() {
			@Override
			public void onValueChange(ValueChangeEvent<List<MeetingInterface>> event) {
				checkEnrollments(iCourses.getValue(), event.getValue());
			}
		});

		
		iForm.addRow(iMeetings);
		
		iEnrollments = new EnrollmentTable(false, true);
		iEnrollments.getTable().setStyleName("unitime-Enrollments");
		iEnrollmentHeader = new UniTimeHeaderPanel("Enrollments");
		iEnrollmentRow = iForm.addHeaderRow(iEnrollmentHeader);
		iForm.addRow(iEnrollments.getTable());
		iForm.getRowFormatter().setVisible(iEnrollmentRow, false);
		iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, false);
		
		iFooter = iHeader.clonePanel("");
		
		iForm.addNotPrintableBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	protected void addMeetings(List<MeetingInterface> meetings) {
		if (meetings != null && !meetings.isEmpty())
			for (MeetingInterface meeting: meetings)
				if (!iMeetings.hasMeeting(meeting))
					iMeetings.add(meeting);
		ValueChangeEvent.fire(iMeetings, iMeetings.getValue());
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName("Add Event");
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
	
	public void setup(EventPropertiesRpcResponse properties) {
		if (properties.hasSponsoringOrganizations()) {
			iSponsors.addItem("Select...", "");
			for (SponsoringOrganizationInterface sponsor: properties.getSponsoringOrganizations())
				iSponsors.addItem(sponsor.getName(), sponsor.getUniqueId().toString());
		} else {
			iForm.getRowFormatter().setVisible(iForm.getRow("Sponsoring Organization:"), false);
		}
	}
	
	public void reset(String roomFilterValue, List<SelectionInterface> selection, ContactInterface mainContact, boolean canLookup) {
		iHeader.clearMessage();
		iName.setText("");
		iSponsors.setSelectedIndex(0);
		iEventType.setSelectedIndex(0);
		DomEvent.fireNativeEvent(Document.get().createChangeEvent(), iEventType);
		iLimit.setText("");
		iEventAddMeetings.reset(roomFilterValue == null || roomFilterValue.isEmpty() ? "department:Event" : roomFilterValue);
		iNotes.setText("");
		iEmails.setText("");
		iCourses.setValue(null);
		iHeader.setEnabled("lookup", canLookup);
		
		iMeetings.clearTable(1);
		if (selection != null && !selection.isEmpty()) {
			List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
			for (SelectionInterface s: selection) {
				for (Integer day: s.getDays()) {
					for (ResourceInterface room: s.getLocations()) {
						MeetingInterface meeting = new MeetingInterface();
						meeting.setStartSlot(s.getStartSlot());
						meeting.setEndSlot(s.getStartSlot() + s.getLength());
						meeting.setStartOffset(0);
						meeting.setEndOffset(0);
						meeting.setDayOfYear(day);
						meeting.setLocation(room);
						meetings.add(meeting);
					}
				}
			}
			if (!meetings.isEmpty()) {
				LoadingWidget.getInstance().show("Checking room availability...");
				RPC.execute(EventRoomAvailabilityRpcRequest.checkAvailability(meetings, iSession.getAcademicSessionId()), new AsyncCallback<EventRoomAvailabilityRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(caught.getMessage());
					}

					@Override
					public void onSuccess(EventRoomAvailabilityRpcResponse result) {
						LoadingWidget.getInstance().hide();
						addMeetings(result.getMeetings());
					}
				});
			}
		}
		
		if (mainContact != null) {
			iMainFName.setText(mainContact.getFirstName() == null ? "" : mainContact.getFirstName());
			iMainMName.setText(mainContact.getMiddleName() == null ? "" : mainContact.getMiddleName());
			iMainLName.setText(mainContact.getLastName() == null ? "" : mainContact.getLastName());
			iMainPhone.setText(mainContact.getPhone() == null ? "" : mainContact.getPhone());
			iMainEmail.setText(mainContact.getEmail() == null ? "" : mainContact.getEmail());
		} else {
			iMainFName.setText("");
			iMainMName.setText("");
			iMainLName.setText("");
			iMainPhone.setText("");
			iMainEmail.setText("");
		}
	}
	
	public static class CourseRelatedObjectLine {
		List<RelatedObjectLookupRpcResponse> iSubjects, iCourses, iSubparts, iClasses;
		
		public List<RelatedObjectLookupRpcResponse> getSubjects() { return iSubjects; }
		public void setSubjects(List<RelatedObjectLookupRpcResponse> subjects) { iSubjects = subjects; }
		public RelatedObjectLookupRpcResponse getSubject(String value) {
			if (value == null || value.isEmpty() || iSubjects == null) return null;
			for (RelatedObjectLookupRpcResponse r: iSubjects)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}
		
		public List<RelatedObjectLookupRpcResponse> getCourses() { return iCourses; }
		public void setCourses(List<RelatedObjectLookupRpcResponse> courses) { iCourses = courses; }
		public RelatedObjectLookupRpcResponse getCourse(String value) {
			if (value == null || value.isEmpty() || iCourses == null) return null;
			for (RelatedObjectLookupRpcResponse r: iCourses)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}

		public List<RelatedObjectLookupRpcResponse> getSubparts() { return iSubparts; }
		public void setSubparts(List<RelatedObjectLookupRpcResponse> subparts) { iSubparts = subparts; }
		public RelatedObjectLookupRpcResponse getSubpart(String value) {
			if (value == null || value.isEmpty() || iSubparts == null) return null;
			for (RelatedObjectLookupRpcResponse r: iSubparts)
				if (r.getUniqueId() != null && (r.getLevel() + ":" + r.getUniqueId()).equals(value)) return r;
			return null;
		}

		public List<RelatedObjectLookupRpcResponse> getClasses() { return iClasses; }
		public void setClasses(List<RelatedObjectLookupRpcResponse> classes) { iClasses = classes; }
		public RelatedObjectLookupRpcResponse getClass(String value) {
			if (value == null || value.isEmpty() || iClasses == null) return null;
			for (RelatedObjectLookupRpcResponse r: iClasses)
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
			return null;
		}

	}
	
	private List<RelatedObjectInterface> iLastRelatedObjects = null;
	private List<MeetingInterface> iLastMeetings = null;
	public void checkEnrollments(final List<RelatedObjectInterface> relatedObjects, final List<MeetingInterface> meetings) {
		if (relatedObjects == null || relatedObjects.isEmpty() || iEventType.getSelectedIndex() != 1) {
			iForm.getRowFormatter().setVisible(iEnrollmentRow, false);
			iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, false);
		} else {
			iForm.getRowFormatter().setVisible(iEnrollmentRow, true);
			iForm.getRowFormatter().setVisible(iEnrollmentRow + 1, true);
			if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) return;
			iEnrollmentHeader.showLoading();
			iLastMeetings = meetings; iLastRelatedObjects = relatedObjects;
			RPC.execute(new GetEnrollmentsFromRelatedObjectsRpcRequest(relatedObjects, meetings, null), new AsyncCallback<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>>() {
				@Override
				public void onFailure(Throwable caught) {
					if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) {
						iEnrollments.clear();
						iEnrollmentHeader.setErrorMessage(caught.getMessage());
					}
				}

				@Override
				public void onSuccess(GwtRpcResponseList<Enrollment> result) {
					if (relatedObjects.equals(iLastRelatedObjects) && meetings.equals(iLastMeetings)) {
						iEnrollmentHeader.clearMessage();
						iEnrollments.clear();
						iEnrollments.populate(result, false);
					}
				}
			});
		}
	}

	public static abstract class CourseRelatedObjectsTable extends UniTimeTable<CourseRelatedObjectLine> implements HasValue<List<RelatedObjectInterface>> {
		private Timer iChangeTimer = null;
		private static int sChangeWaitTime = 500;
		private AcademicSessionProvider iSession = null;
		private List<RelatedObjectInterface> iLastChange = null;
		
		public CourseRelatedObjectsTable(AcademicSessionProvider session) {
			iSession = session;
			setStyleName("unitime-EventOwners");
			
			List<Widget> header = new ArrayList<Widget>();
			header.add(new UniTimeTableHeader("Subject"));
			header.add(new UniTimeTableHeader("Course Number"));
			header.add(new UniTimeTableHeader("Config / Subpart"));
			header.add(new UniTimeTableHeader("Class Number"));
			header.add(new UniTimeTableHeader("&nbsp;"));
			
			addRow(null, header);
			
			iChangeTimer = new Timer() {
				@Override
				public void run() {
					List<RelatedObjectInterface> value = getValue();
					if (iLastChange != null && iLastChange.equals(value)) return;
					iLastChange = value;
					ValueChangeEvent.fire(CourseRelatedObjectsTable.this, value);
				}
			};
		}
		
		public abstract void setErrorMessage(String message);
		
		private void addLine(final RelatedObjectInterface data) {
			List<Widget> row = new ArrayList<Widget>();
			
			final CourseRelatedObjectLine line = new CourseRelatedObjectLine();
			
			final ListBox subject = new ListBox();
			subject.addStyleName("subject");
			subject.addItem("-", "");
			subject.setSelectedIndex(0);
			RPC.execute(RelatedObjectLookupRpcRequest.getChildren(RelatedObjectLookupRpcRequest.Level.SESSION, iSession.getAcademicSessionId()), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {

				@Override
				public void onFailure(Throwable caught) {
					setErrorMessage(caught.getMessage());
				}

				@Override
				public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
					line.setSubjects(result);
					Long selectedId = (data != null && data.hasSelection() ? data.getSelection()[0] : null);
					int selectedIdx = -1;
					for (int idx = 0; idx < result.size(); idx++) {
						RelatedObjectLookupRpcResponse r = result.get(idx);
						subject.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
						if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = idx;
					}
					if (selectedIdx >= 0)
						subject.setSelectedIndex(selectedIdx);
				}
			});
			
			row.add(subject);
			
			final ListBox course = new ListBox();
			course.addStyleName("course");
			course.addItem("N/A", "");
			course.setSelectedIndex(0);
			
			subject.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					final RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					if (rSubject == null) {
						course.clear();
						course.addItem("N/A", "");
						course.setSelectedIndex(0);
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), course);
					} else {
						course.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(rSubject), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
								if (!rSubject.equals(res)) return;
								course.clear();
								line.setCourses(result);
								if (result.size() > 1)
									course.addItem("-", "");
								Long selectedId = (data != null && data.hasSelection() ? data.getSelection()[1] : null);
								int selectedIdx = -1;
								for (int idx = 0; idx < result.size(); idx++) {
									RelatedObjectLookupRpcResponse r = result.get(idx);
									course.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
									if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = idx;
								}
								if (selectedIdx >= 0)
									subject.setSelectedIndex(selectedIdx);
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), course);
							}
						});
					}
					
					if (line.equals(getData(getRowCount() - 1)))
						addLine(null);

					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(course);
			
			final ListBox subpart = new ListBox();
			subpart.addStyleName("subpart");
			subpart.addItem("N/A", "");
			subpart.setSelectedIndex(0);
			
			course.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					final RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
					if (rCourse == null) {
						subpart.clear();
						subpart.addItem("N/A", "");
						DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subpart);
					} else {
						subpart.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(rSubject, rCourse), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
								if (!rCourse.equals(res)) return;
								subpart.clear();
								line.setSubparts(result);
								Long selectedId = (data != null && data.hasSelection() && data.getSelection().length >= 3 ? data.getSelection()[2] : null);
								int selectedIdx = -1;
								RelatedObjectLookupRpcRequest.Level selectedLevel = RelatedObjectLookupRpcRequest.Level.SUBPART;
								if (selectedId != null) {
									if (data.getType() == RelatedObjectType.Config)
										selectedLevel = RelatedObjectLookupRpcRequest.Level.CONFIG;
									if (data.getType() == RelatedObjectType.Course)
										selectedLevel = RelatedObjectLookupRpcRequest.Level.COURSE;
									if (data.getType() == RelatedObjectType.Offering)
										selectedLevel = RelatedObjectLookupRpcRequest.Level.OFFERING;
								}
								for (int idx = 0; idx < result.size(); idx++) {
									RelatedObjectLookupRpcResponse r = result.get(idx);
									subpart.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getLevel() + ":" + r.getUniqueId().toString());
									if (selectedId != null && selectedId.equals(r.getUniqueId()) && selectedLevel == r.getLevel()) selectedIdx = idx;
								}
								if (selectedIdx >= 0)
									subject.setSelectedIndex(selectedIdx);
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subpart);
							}
						});
					}
					
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(subpart);
			
			final ListBox clazz = new ListBox();
			clazz.addStyleName("class");
			clazz.addItem("N/A", "");
			clazz.setSelectedIndex(0);
			
			subpart.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
					RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
					final RelatedObjectLookupRpcResponse rSubpart = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
					if (rSubpart == null) {
						clazz.clear();
						clazz.addItem("N/A", "");
						clazz.setSelectedIndex(0);
					} else {
						clazz.clear();
						RPC.execute(RelatedObjectLookupRpcRequest.getChildren(rSubject, rCourse, rSubpart), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {
							@Override
							public void onFailure(Throwable caught) {
								setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
								if (!rSubpart.equals(res)) return;
								clazz.clear();
								line.setClasses(result);
								if (result.size() > 1)
									clazz.addItem("-", "");
								Long selectedId = (data != null && data.hasSelection() && data.getSelection().length >= 4 ? data.getSelection()[3] : null);
								int selectedIdx = -1;
								for (int idx = 0; idx < result.size(); idx++) {
									RelatedObjectLookupRpcResponse r = result.get(idx);
									clazz.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
									if (selectedId != null && selectedId.equals(r.getUniqueId())) selectedIdx = idx;
								}
								if (selectedIdx >= 0)
									subject.setSelectedIndex(selectedIdx);
							}
						});
					}
					
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			clazz.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			
			row.add(clazz);
			
			Image remove = new Image(RESOURCES.delete());
			remove.addStyleName("remove");
			remove.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					for (int row = 1; row < getRowCount(); row ++)
						if (line.equals(getData(row))) {
							removeRow(row);
							break;
						}
					if (getRowCount() <= 1) addLine(null);
					iChangeTimer.schedule(sChangeWaitTime);
				}
			});
			row.add(remove);
			
			addRow(line, row);
		}

		@Override
		public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<RelatedObjectInterface>> handler) {
			return addHandler(handler, ValueChangeEvent.getType());
		}

		@Override
		public List<RelatedObjectInterface> getValue() {
			List<RelatedObjectInterface> objects = new ArrayList<RelatedObjectInterface>();
			for (int row = 1; row < getRowCount(); row ++) {
				CourseRelatedObjectLine line = getData(row);
				ListBox subject = (ListBox)getWidget(row, 0);
				RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
				ListBox course = (ListBox)getWidget(row, 1);
				RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
				ListBox subpart = (ListBox)getWidget(row, 2);
				RelatedObjectLookupRpcResponse rSubpart = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
				ListBox clazz = (ListBox)getWidget(row, 3);
				RelatedObjectLookupRpcResponse rClazz = (clazz.getSelectedIndex() < 0 ? null : line.getClass(clazz.getValue(clazz.getSelectedIndex())));
				if (rClazz != null && rClazz.getRelatedObject() != null) {
					objects.add(rClazz.getRelatedObject()); continue;
				}
				if (rSubpart != null && rSubpart.getRelatedObject() != null) {
					objects.add(rSubpart.getRelatedObject()); continue;
				}
				if (rCourse != null && rCourse.getRelatedObject() != null) {
					objects.add(rCourse.getRelatedObject()); continue;
				}
				if (rSubject != null && rSubject.getRelatedObject() != null) {
					objects.add(rSubject.getRelatedObject()); continue;
				}
			}
			return objects;
		}

		@Override
		public void setValue(List<RelatedObjectInterface> value) {
			setValue(value, false);
		}

		@Override
		public void setValue(List<RelatedObjectInterface> value, boolean fireEvents) {
			iLastChange = null;
			clearTable(1);
			if (value != null)
				for (RelatedObjectInterface line: value)
					addLine(line);
			addLine(null);
			addLine(null);
			if (fireEvents)
				ValueChangeEvent.fire(CourseRelatedObjectsTable.this, getValue());
		}
		
	}


}
