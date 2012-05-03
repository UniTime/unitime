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
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventRoomAvailabilityRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectLookupRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SelectionInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
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

		iCourses = new CourseRelatedObjectsTable();
		
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
		
		iForm.addRow(iMeetings);
		
		iFooter = iHeader.clonePanel("");
		
		iForm.addNotPrintableBottomRow(iFooter);
		
		initWidget(iForm);
	}
	
	protected void addMeetings(List<MeetingInterface> meetings) {
		if (meetings != null && !meetings.isEmpty())
			for (MeetingInterface meeting: meetings)
				if (!iMeetings.hasMeeting(meeting))
					iMeetings.add(meeting);
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
		iCourses.reset();
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
	
	class CourseRelatedObjectLine {
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
				if (r.getUniqueId() != null && r.getUniqueId().toString().equals(value)) return r;
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

	class CourseRelatedObjectsTable extends UniTimeTable<CourseRelatedObjectLine> {
		
		public CourseRelatedObjectsTable() {
			setStyleName("unitime-EventOwners");
			
			List<Widget> header = new ArrayList<Widget>();
			header.add(new UniTimeTableHeader("Subject"));
			header.add(new UniTimeTableHeader("Course Number"));
			header.add(new UniTimeTableHeader("Config / Subpart"));
			header.add(new UniTimeTableHeader("Class Number"));
			header.add(new UniTimeTableHeader("&nbsp;"));
			
			addRow(null, header);
		}
		
		public void reset() {
			clearTable(1);
			addBlankLine();
			addBlankLine();
		}
		
		public void addBlankLine() {
			List<Widget> row = new ArrayList<Widget>();
			
			final CourseRelatedObjectLine line = new CourseRelatedObjectLine();
			
			final ListBox subject = new ListBox();
			subject.addStyleName("subject");
			subject.addItem("-", "");
			subject.setSelectedIndex(0);
			RPC.execute(RelatedObjectLookupRpcRequest.getChildren(RelatedObjectLookupRpcRequest.Level.SESSION, iSession.getAcademicSessionId()), new AsyncCallback<GwtRpcResponseList<RelatedObjectLookupRpcResponse>>() {

				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(caught.getMessage());
				}

				@Override
				public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
					line.setSubjects(result);
					for (RelatedObjectLookupRpcResponse r: result) {
						subject.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
					}
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
								iHeader.setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
								if (!rSubject.equals(res)) return;
								course.clear();
								line.setCourses(result);
								if (result.size() > 1)
									course.addItem("-", "");
								for (RelatedObjectLookupRpcResponse r: result) {
									course.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
								}
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), course);
							}
						});
					}
					
					if (line.equals(getData(getRowCount() - 1)))
						addBlankLine();
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
								iHeader.setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
								if (!rCourse.equals(res)) return;
								subpart.clear();
								line.setSubparts(result);
								for (RelatedObjectLookupRpcResponse r: result) {
									subpart.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
								}
								DomEvent.fireNativeEvent(Document.get().createChangeEvent(), subpart);
							}
						});
					}
					
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
								iHeader.setErrorMessage(caught.getMessage());
							}
							@Override
							public void onSuccess(GwtRpcResponseList<RelatedObjectLookupRpcResponse> result) {
								RelatedObjectLookupRpcResponse res = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
								if (!rSubpart.equals(res)) return;
								clazz.clear();
								line.setClasses(result);
								if (result.size() > 1)
									clazz.addItem("-", "");
								for (RelatedObjectLookupRpcResponse r: result) {
									clazz.addItem(r.getLabel(), r.getUniqueId() == null ? "" : r.getUniqueId().toString());
								}
							}
						});
					}
					
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
					if (getRowCount() <= 1) addBlankLine();
				}
			});
			row.add(remove);
			
			addRow(line, row);
		}
		
	}
	
	public List<RelatedObjectInterface> getRelatedObjects() {
		List<RelatedObjectInterface> objects = new ArrayList<RelatedObjectInterface>();
		for (int row = 1; row < iCourses.getRowCount(); row ++) {
			CourseRelatedObjectLine line = iCourses.getData(row);
			ListBox subject = (ListBox)iCourses.getWidget(row, 0);
			RelatedObjectLookupRpcResponse rSubject = (subject.getSelectedIndex() < 0 ? null : line.getSubject(subject.getValue(subject.getSelectedIndex())));
			ListBox course = (ListBox)iCourses.getWidget(row, 1);
			RelatedObjectLookupRpcResponse rCourse = (course.getSelectedIndex() < 0 ? null : line.getCourse(course.getValue(course.getSelectedIndex())));
			ListBox subpart = (ListBox)iCourses.getWidget(row, 2);
			RelatedObjectLookupRpcResponse rSubpart = (subpart.getSelectedIndex() < 0 ? null : line.getSubpart(subpart.getValue(subpart.getSelectedIndex())));
			ListBox clazz = (ListBox)iCourses.getWidget(row, 3);
			RelatedObjectLookupRpcResponse rClazz = (clazz.getSelectedIndex() < 0 ? null : line.getSubpart(clazz.getValue(clazz.getSelectedIndex())));
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

}
