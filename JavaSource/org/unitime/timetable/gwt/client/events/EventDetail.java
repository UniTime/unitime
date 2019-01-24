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
package org.unitime.timetable.gwt.client.events;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.GwtHint;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.events.EventAdd.EventPropertiesProvider;
import org.unitime.timetable.gwt.client.events.EventMeetingTable.EventMeetingRow;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.sectioning.SectioningCookie;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget.CourseDetailsRpcRequest;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget.CourseDetailsRpcResponse;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.ServerDateTimeFormat;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ApproveEventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventEnrollmentsRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MessageInterface;
import org.unitime.timetable.gwt.shared.EventInterface.NoteInterface;
import org.unitime.timetable.gwt.shared.EventInterface.RelatedObjectInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SaveOrApproveEventRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class EventDetail extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	private static DateTimeFormat sTimeStampFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private static DateTimeFormat sEventDateFormat = ServerDateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private EventInterface iEvent = null;
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter, iEnrollmentHeader;
	
	private UniTimeTable<ContactInterface> iContacts;
	private EventMeetingTable iMeetings;
	private UniTimeTable<NoteInterface> iNotes;
	private UniTimeTable<RelatedObjectInterface> iOwners;
	private EnrollmentTable iEnrollments;
	private ApproveDialog iApproveDialog;
	private CheckBox iShowDeleted;
	
	private EventPropertiesProvider iProperties;
	
	public EventDetail(EventPropertiesProvider properties) {
		iForm = new SimpleForm();
		iProperties = properties;
		
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("edit", MESSAGES.buttonEditEvent(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				edit();
			}
		});
		iHeader.addButton("previous", MESSAGES.buttonPrevious(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EventInterface prev = getPrevious(getEvent().getId());
				if (prev != null) previous(prev);
			}
		});
		iHeader.addButton("next", MESSAGES.buttonNext(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				EventInterface next = getNext(getEvent().getId());
				if (next != null) next(next);
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		iContacts = new UniTimeTable<ContactInterface>();
		iContacts.setStyleName("unitime-EventContacts");
		
		List<Widget> contactHeader = new ArrayList<Widget>();
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colNamePerson()));
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colEmail()));
		contactHeader.add(new UniTimeTableHeader(MESSAGES.colPhone()));
		iContacts.addRow(null, contactHeader);
		
		iApproveDialog = new ApproveDialog(iProperties) {
			@Override
			protected void onSubmit(final ApproveEventRpcRequest.Operation operation, List<EventMeetingRow> items, String message, boolean emailConfirmation){
				switch (operation) {
				case APPROVE: LoadingWidget.getInstance().show(MESSAGES.waitForApproval(iEvent.getName())); break;
				case INQUIRE: LoadingWidget.getInstance().show(MESSAGES.waitForInquiry(iEvent.getName())); break;
				case REJECT: LoadingWidget.getInstance().show(MESSAGES.waitForRejection(iEvent.getName())); break;
				case CANCEL: LoadingWidget.getInstance().show(MESSAGES.waitForCancellation(iEvent.getName())); break;
				}
				List<MeetingInterface> meetings = new ArrayList<MeetingInterface>();
				for (EventMeetingRow item: items)
					meetings.add(item.getMeeting());
				RPC.execute(ApproveEventRpcRequest.createRequest(operation, iProperties.getSessionId(), iEvent, meetings, message, emailConfirmation), new AsyncCallback<SaveOrApproveEventRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(caught.getMessage(), caught);
					}

					@Override
					public void onSuccess(SaveOrApproveEventRpcResponse result) {
						LoadingWidget.getInstance().hide();
						if (result.hasMessages())
							for (MessageInterface m: result.getMessages()) {
								if (m.isError())
									UniTimeNotifications.warn(m.getMessage());
								else if (m.isWarning())
									UniTimeNotifications.error(m.getMessage());
								else
									UniTimeNotifications.info(m.getMessage());
							}
						switch (operation) {
						case APPROVE:
							onApprovalOrReject(iEvent.getId(), result.getEvent());
							setEvent(result.getEvent());
							break;
						case REJECT:
						case CANCEL:
							onApprovalOrReject(iEvent.getId(), result.getEvent());
							if (result.hasEventWithId())
								setEvent(result.getEvent());
							else
								EventDetail.this.hide();
							break;
						case INQUIRE:
							setEvent(result.getEvent());
							break;
						}
					}
				});
			}
		};
		
		iMeetings = new EventMeetingTable(EventMeetingTable.Mode.MeetingsOfAnEvent, true, iProperties);
		iMeetings.setOperation(EventMeetingTable.OperationType.Approve, iApproveDialog);
		iMeetings.setOperation(EventMeetingTable.OperationType.Reject, iApproveDialog);
		iMeetings.setOperation(EventMeetingTable.OperationType.Inquire, iApproveDialog);
		iMeetings.setOperation(EventMeetingTable.OperationType.Cancel, iApproveDialog);
		iMeetings.setEditable(false);
		
		iShowDeleted = new CheckBox("<i>" + MESSAGES.showDeletedMeetings() + "</i>", true);
		iShowDeleted.setValue(EventCookie.getInstance().isShowDeletedMeetings());
		iShowDeleted.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iMeetings.setMeetings(iEvent, iMeetings.getMeetings());
				EventCookie.getInstance().setShowDeletedMeetings(event.getValue());
				if (event.getValue())
					iMeetings.removeStyleName("unitime-EventMeetingsHideDeleted");
				else
					iMeetings.addStyleName("unitime-EventMeetingsHideDeleted");
			}
		});
		if (!iShowDeleted.getValue())
			iMeetings.addStyleName("unitime-EventMeetingsHideDeleted");
		
		iOwners = new UniTimeTable<RelatedObjectInterface>();
		iOwners.setStyleName("unitime-EventOwners");

		List<Widget> ownersHeader = new ArrayList<Widget>();
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colSection()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colType()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colTitle()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colTime()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colLocation()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colInstructor()));
		ownersHeader.add(new UniTimeTableHeader(MESSAGES.colNote()));
		iOwners.addRow(null, ownersHeader);
		iOwners.addMouseClickListener(new UniTimeTable.MouseClickListener<EventInterface.RelatedObjectInterface>() {
			@Override
			public void onMouseClick(TableEvent<RelatedObjectInterface> event) {
				if (event.getData() != null && event.getData().hasDetailPage())
					UniTimeFrameDialog.openDialog(MESSAGES.dialogDetailsOf(event.getData().getName()), event.getData().getDetailPage() + "&menu=hide");
			}
		});
		
		iEnrollmentHeader = new UniTimeHeaderPanel(MESSAGES.sectEnrollments());
		iEnrollments = new EnrollmentTable(false, true);
		iEnrollments.getTable().setStyleName("unitime-Enrollments");
		iEnrollmentHeader.addButton("export", MESSAGES.opExportCSV(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RPC.execute(EncodeQueryRpcRequest.encode("output=event-enrollments.csv&sid=" + iProperties.getSessionId() + "&event=" + iEvent.getId() +
						"&suffix=" + (SectioningCookie.getInstance().getShowClassNumbers() ? "1" : "0") +
						"&sort=" + SectioningCookie.getInstance().getEnrollmentSortBy() +
						"&group=" + SectioningCookie.getInstance().getEnrollmentSortByGroup() +
						"&subpart=" + SectioningCookie.getInstance().getEnrollmentSortBySubpart()), new AsyncCallback<EncodeQueryRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {}
					@Override
					public void onSuccess(EncodeQueryRpcResponse result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
					}
				});
			}
		});
		iEnrollmentHeader.addButton("export-pdf", MESSAGES.opExportPDF(), 75, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				RPC.execute(EncodeQueryRpcRequest.encode("output=event-enrollments.pdf&sid=" + iProperties.getSessionId() + "&event=" + iEvent.getId() +
						"&suffix=" + (SectioningCookie.getInstance().getShowClassNumbers() ? "1" : "0") +
						"&sort=" + SectioningCookie.getInstance().getEnrollmentSortBy() +
						"&subpart=" + SectioningCookie.getInstance().getEnrollmentSortBySubpart()), new AsyncCallback<EncodeQueryRpcResponse>() {
					@Override
					public void onFailure(Throwable caught) {}
					@Override
					public void onSuccess(EncodeQueryRpcResponse result) {
						ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
					}
				});
			}
		});
		
		iNotes = new UniTimeTable<NoteInterface>();
		iNotes.setStyleName("unitime-EventNotes");

		List<Widget> notesHeader = new ArrayList<Widget>();
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colDate()));
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colUser()));
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colAction()));
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colMeetings()));
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colNote()));
		notesHeader.add(new UniTimeTableHeader(MESSAGES.colAttachment()));
		iNotes.addRow(null, notesHeader);
		
		iFooter = iHeader.clonePanel();
		
		initWidget(iForm);
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(MESSAGES.pageEventDetail());
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
	
	protected void edit() {
	}
	
	protected EventInterface getNext(Long eventId) { return null; }
	protected void next(EventInterface event) {}
	
	protected EventInterface getPrevious(Long eventId) { return null; }
	protected void previous(EventInterface previous) {}
	
	protected void onApprovalOrReject(Long eventId, EventInterface event) {}
	
	public void setEvent(EventInterface event) {
		iEvent = event;
		
		iApproveDialog.reset(iProperties.getProperties());
		iMeetings.setShowMeetingContacts(iProperties.getProperties().isCanViewMeetingContacts());
		
		iForm.clear();

		iHeader.clearMessage();
		iHeader.setHeaderTitle(iEvent.getName() + " (" + iEvent.getType().getName(CONSTANTS) + ")");
		iHeader.setEnabled("edit", iEvent.isCanEdit());
		iHeader.setEnabled("previous", getPrevious(iEvent.getId()) != null);
		iHeader.setEnabled("next", getNext(iEvent.getId()) != null);
		iForm.addHeaderRow(iHeader);
		
		iForm.addRow(MESSAGES.propEventType(), new Label(iEvent.getType().getName(CONSTANTS)));
		
		iContacts.clearTable(1);
		if (iEvent.hasContact()) {
			List<Label> row = new ArrayList<Label>();
			row.add(new Label(iEvent.getContact().getName(MESSAGES), false));
			row.add(new Label(iEvent.getContact().hasEmail() ? iEvent.getContact().getEmail() : "", false));
			row.add(new Label(iEvent.getContact().hasPhone() ? iEvent.getContact().getPhone() : "", false));
			int rowNum = iContacts.addRow(iEvent.getContact(), row);
			for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
				iContacts.getCellFormatter().addStyleName(rowNum, col, "main-contact");
		}
		if (iEvent.hasAdditionalContacts()) {
			for (ContactInterface contact: iEvent.getAdditionalContacts()) {
				List<Label> row = new ArrayList<Label>();
				row.add(new Label(contact.getName(MESSAGES), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new Label(contact.hasPhone() ? contact.getPhone() : "", false));
				int rowNum = iContacts.addRow(contact, row);
				for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
					iContacts.getCellFormatter().addStyleName(rowNum, col, "additional-contact");

			}
		}
		if (iEvent.hasInstructors()) {
			for (ContactInterface contact: iEvent.getInstructors()) {
				List<Label> row = new ArrayList<Label>();
				row.add(new Label(contact.getName(MESSAGES, false), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new HTML(contact.hasPhone() ? contact.getPhone() : contact.hasResponsibility() ? contact.getResponsibility() : MESSAGES.eventContactInstructorPhone(), false));
				int rowNum = iContacts.addRow(contact, row);
				for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
					iContacts.getCellFormatter().addStyleName(rowNum, col, "instructor-contact");
			}			
		}
		if (iEvent.hasCoordinators()) {
			for (ContactInterface contact: iEvent.getCoordinators()) {
				List<Label> row = new ArrayList<Label>();
				row.add(new Label(contact.getName(MESSAGES, false), false));
				row.add(new Label(contact.hasEmail() ? contact.getEmail() : "", false));
				row.add(new HTML(contact.hasPhone() ? contact.getPhone() : contact.hasResponsibility() ? contact.getResponsibility() : MESSAGES.eventContactCoordinatorPhone(), false));
				int rowNum = iContacts.addRow(contact, row);
				for (int col = 0; col < iContacts.getCellCount(rowNum); col++)
					iContacts.getCellFormatter().addStyleName(rowNum, col, "coordinator-contact");
			}			
		}
		if (iContacts.getRowCount() > 1)
			iForm.addRow(MESSAGES.propContacts(), iContacts);
		
		if (iEvent.hasEmail()) {
			iForm.addRow(MESSAGES.propAdditionalEmails(), new Label(iEvent.getEmail()));
		}

		if (iEvent.hasSponsor()) {
			iForm.addRow(MESSAGES.propSponsor(), new Label(iEvent.getSponsor().getName()));
		}
		
		if (iEvent.hasEnrollment()) {
			iForm.addRow(MESSAGES.propEnrollment(), new Label(String.valueOf(iEvent.getEnrollment().toString())));
			int r = iForm.addRow(MESSAGES.propStudentConflicts(), new Label(""));
			iForm.getRowFormatter().setVisible(r, false);
		}
		
		if (iEvent.hasMaxCapacity()) {
			iForm.addRow(MESSAGES.propAttendance(), new Label(iEvent.getMaxCapacity().toString()));
		}
		
		if (iEvent.hasExpirationDate() && iEvent.hasPendingMeetings()) {
			iForm.addRow(MESSAGES.propExpirationDate(), new Label(sEventDateFormat.format(iEvent.getExpirationDate())));
		}
		
		if (iEvent.hasRequestedServices()) {
			P providers = new P("unitime-EventRequestedServices");
			for (EventServiceProviderInterface p: iEvent.getRequestedServices()) {
				P label = new P("label"); label.setText(p.getLabel());
				providers.add(label);
				if (p.hasMessage()) {
					P msg = new P("description"); msg.setHTML(p.getMessage());
					providers.add(msg);
				}
			}
			iForm.addRow(MESSAGES.propEventRequestedServices(), providers); 
		}
		
		if (iEvent.hasLastChange()) {
			iForm.addRow(MESSAGES.propLastChange(), new Label(iEvent.getLastChange()));
		}
		
		iMeetings.clearTable(1);
		iMeetings.resetColumnVisibility();
		for (MeetingInterface meeting: iEvent.getMeetings()) {
			iMeetings.add(new EventMeetingRow(iEvent, meeting));
		}
		if (iMeetings.getRowCount() > 1) {
			UniTimeHeaderPanel header = new UniTimeHeaderPanel(MESSAGES.sectMeetings());
			header.addButton("operations", MESSAGES.buttonMoreOperations(), 75, new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new PopupPanel(true);
					iMeetings.getHeader(0).setMenu(popup);
					popup.showRelativeTo((UIObject)event.getSource());
					((MenuBar)popup.getWidget()).focus();
				}
			});
			iForm.addHeaderRow(header);
			iForm.addRow(iMeetings);
			
			iForm.addRow(iShowDeleted);
			iForm.getCellFormatter().setHorizontalAlignment(iForm.getRowCount() - 1, 0, HasHorizontalAlignment.ALIGN_RIGHT);
			iShowDeleted.setValue(EventCookie.getInstance().isShowDeletedMeetings(), true);
		}
		
		iNotes.clearTable(1);
		if (iEvent.hasNotes()) {
			for (NoteInterface note: iEvent.getNotes()) {
				List<Widget> row = new ArrayList<Widget>();
				row.add(new HTML(note.getDate() == null ? "<i>" + MESSAGES.notApplicable() + "</i>" : sTimeStampFormat.format(note.getDate())));
				row.add(new HTML(note.getUser() == null ? "<i>" + MESSAGES.notApplicable() + "</i>" : note.getUser()));
				row.add(new HTML(note.getType() == null ? "<i>" + MESSAGES.notApplicable() + "</i>" : note.getType().getName()));
				row.add(new HTML(note.getMeetings() == null ? "<i>" + MESSAGES.notApplicable() + "</i>" : note.getMeetings()));
				row.add(new HTML(note.getNote() == null ? "" : note.getNote().replace("\n", "<br>")));
				if (note.hasAttachment()) {
					ImageLink link = new ImageLink(new Image(RESOURCES.download()), note.hasLink() ?
							GWT.getHostPageBaseURL() + "upload?q=" + note.getLink() :
							GWT.getHostPageBaseURL() + "upload?event=" + iEvent.getId() + (note.getId() == null ? "&name=" + note.getAttachment() : "&note=" + note.getId()));
					link.setTitle(note.getAttachment());
					link.setText(note.getAttachment());
					row.add(link);
				} else {
					row.add(new HTML(""));
				}
				int r = iNotes.addRow(note, row);
				if (note.getType() != null)
					iNotes.getRowFormatter().addStyleName(r, note.getType().getName().toLowerCase());
			}
		}
		if (iNotes.getRowCount() > 1) {
			iForm.addHeaderRow(MESSAGES.sectNotes());
			iForm.addRow(iNotes);
		}

		iOwners.clearTable(1);
		if (iEvent.hasRelatedObjects()) {
			for (RelatedObjectInterface obj: iEvent.getRelatedObjects()) {
				List<Widget> row = new ArrayList<Widget>();
				P course = new P("multiple-lines");
				if (obj.hasCourseNames()) {
					int idx = 0;
					for (String cn: obj.getCourseNames()) {
						Long courseId = obj.getCourseId(idx++);
						if (course.getWidgetCount() == 0) {
							course.add(new CourseName(courseId, cn));
						} else {
							course.add(new CourseName(courseId, cn, "cross-list"));
						}
					}
				} else {
					course.add(new CourseName(null, obj.getName()));
				}
				row.add(course);
				
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
				row.add(new Label(type));
				
				String title = "";
				if (obj.hasCourseTitles()) {
					String last = null;
					for (String ct: obj.getCourseTitles()) {
						if (last != null && !last.isEmpty() && last.equals(ct))
							ct = "";
						else
							last = ct;
						if (title.isEmpty()) {
							title += ct;
						} else {
							title += "<span class='cross-list'>" + ct + "</span>";
						}
					}
				} else {
					title = "";
				}
				row.add(new HTML(title));
				
				if (obj.hasDate()) {
					row.add(new Label(obj.getDate()));
				} else {
					row.add(new Label());
				}
				
				if (obj.hasTime()) {
					row.add(new Label(obj.getTime()));
				} else {
					row.add(new Label());
				}
				
				String location = "";
				if (obj.hasLocations()) {
					for (ResourceInterface loc: obj.getLocations()) {
						location += (location.isEmpty() ? "" : "<br>") + loc.getNameWithHint(MESSAGES);
					}
				}
				row.add(new HTML(location, false));

				if (obj.hasInstructors()) {
					row.add(new HTML(obj.getInstructorNames("<br>", MESSAGES)));
				} else {
					row.add(new HTML());
				}
				
				if (obj.hasNote()) {
					P note = new P("note");
					note.setHTML(obj.getNote().replace("\n", "<br>"));
					note.setTitle(obj.getNote());
					row.add(note);
				} else {
					row.add(new HTML());
				}
				
				int rowNumber = iOwners.addRow(obj, row);
				iOwners.getRowFormatter().addStyleName(rowNumber, "owner-row");
				for (int i = 0; i < iOwners.getCellCount(rowNumber); i++)
					iOwners.getCellFormatter().addStyleName(rowNumber, i, "owner-cell");
			}
		}
		if (iOwners.getRowCount() > 1) {
			iForm.addHeaderRow(MESSAGES.sectRelations());
			iForm.addRow(iOwners);
		}
		
		iEnrollments.clear();
		if (iEvent.hasEnrollment()) {
			final int enrollmentsRow = iForm.addHeaderRow(iEnrollmentHeader);
			iForm.addRow(iEnrollments.getTable());
			iEnrollmentHeader.setEnabled("export", false);
			iEnrollmentHeader.setEnabled("export-pdf", false);
			iEnrollmentHeader.showLoading();
			final Long eventId = iEvent.getId();
			RPC.execute(EventEnrollmentsRpcRequest.getEnrollmentsForEvent(eventId, iProperties.getSessionId()), new AsyncCallback<GwtRpcResponseList<ClassAssignmentInterface.Enrollment>>() {
				@Override
				public void onFailure(Throwable caught) {
					if (eventId.equals(iEvent.getId())) {
						iEnrollmentHeader.clearMessage();
						UniTimeNotifications.error(MESSAGES.failedNoEnrollments(caught.getMessage()), caught);
						iForm.getRowFormatter().setVisible(enrollmentsRow, false);
						iForm.getRowFormatter().setVisible(enrollmentsRow + 1, false);
					}
				}

				@Override
				public void onSuccess(GwtRpcResponseList<Enrollment> result) {
					if (eventId.equals(iEvent.getId())) {
						if (result == null) result = new GwtRpcResponseList<Enrollment>();
						iEnrollmentHeader.clearMessage();
						iEnrollments.clear();
						iEnrollments.populate(result, null);
						int conf = 0;
						for (Enrollment e: result)
							if (e.hasConflict()) conf ++;
						if (conf != 0) {
							int row = iForm.getRow(MESSAGES.propStudentConflicts());
							((Label)iForm.getWidget(row, 1)).setText(String.valueOf(conf));
							iForm.getRowFormatter().setVisible(row, true);
						}
						iEnrollmentHeader.setEnabled("export", !result.isEmpty());
						iEnrollmentHeader.setEnabled("export-pdf", !result.isEmpty());
					}
				}
			});
		}

		iForm.addNotPrintableBottomRow(iFooter);
	}
	
	public EventInterface getEvent() { return iEvent; }
	
	private class CourseName extends P {
		private CourseDetailsRpcResponse iDetails = null;

		public CourseName(final Long id, String name, String... styles) {
			super(styles);
			setText(name);
			if (id != null) {
				addMouseOverHandler(new MouseOverHandler() {
					@Override
					public void onMouseOver(MouseOverEvent event) {
						if (iDetails == null) {
							RPC.execute(new CourseDetailsRpcRequest(id), new AsyncCallback<CourseDetailsRpcResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									iDetails = new CourseDetailsRpcResponse();
								}

								@Override
								public void onSuccess(CourseDetailsRpcResponse result) {
									iDetails = result; showDetails();
								}
							});
						} else {
							showDetails();
						}
					}
				});
				addMouseOutHandler(new MouseOutHandler() {
					@Override
					public void onMouseOut(MouseOutEvent event) {
						hideDetails();
					}
				});
				addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iDetails == null) {
							RPC.execute(new CourseDetailsRpcRequest(id), new AsyncCallback<CourseDetailsRpcResponse>() {
								@Override
								public void onFailure(Throwable caught) {
									iDetails = new CourseDetailsRpcResponse();
								}

								@Override
								public void onSuccess(CourseDetailsRpcResponse result) {
									iDetails = result; openLink();
								}
							});
						} else {
							openLink();
						}
						event.stopPropagation();
					}
				});
			}
		}
		
		protected void showDetails() {
			if (iDetails != null && iDetails.hasDetails()) {
				HTML details = new HTML(iDetails.getDetails());
				details.setStyleName("unitime-CourseDetailsPopup");
				GwtHint.showHint(getElement(), details);
			}
		}
		
		protected void hideDetails() {
			if (iDetails != null && iDetails.hasDetails())
				GwtHint.hideHint();
		}
		
		protected void openLink() {
			if (iDetails != null && iDetails.hasLink()) {
				iOwners.clearHover();
				GwtHint.hideHint();
				UniTimeFrameDialog.openDialog(MESSAGES.courseCatalogDialog(getText()), iDetails.getLink(), null, null, false);
			}
		}
	}	

}
