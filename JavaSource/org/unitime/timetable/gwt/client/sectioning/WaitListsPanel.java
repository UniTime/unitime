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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.widgets.OpenCloseSectionImage;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class WaitListsPanel extends P {
	protected static StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	protected static StudentSectioningResources RESOURCES = GWT.create(StudentSectioningResources.class);
	protected static StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sModifiedDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private UniTimeTable<RequestedCourse> iTable;
	private FocusPanel iPanel;
	private Image iWaiting = null;
	private OpenCloseSectionImage iOpenCloseImage;
	private CourseRequestInterface iRequests;
	private SpecialRegistrationContext iSpecReg;
	
	public WaitListsPanel(SpecialRegistrationContext specReg) {
		iSpecReg = specReg;
		addStyleName("unitime-WaitListsPanel");
		
		P title = new P("waitlists-header");
		iWaiting = new Image(RESOURCES.loading_small()); iWaiting.addStyleName("icon");
		iWaiting.setVisible(false);
		title.add(iWaiting);
		
		iOpenCloseImage = new OpenCloseSectionImage(true);
		iOpenCloseImage.addStyleName("open-close-icon");
		iOpenCloseImage.setVisible(true);
		title.add(iOpenCloseImage);

		P label = new P("title"); label.setText(MESSAGES.panelWaitListedCourses());
		title.add(label);
		add(title);

		iTable = new UniTimeTable<RequestedCourse>();
		iTable.addStyleName("waitlists-table");
		
		iPanel = new FocusPanel(iTable);
		iPanel.addStyleName("waitlists-panel");
		add(iPanel);
		
		iOpenCloseImage.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iPanel.setVisible(event.getValue() && iTable.getRowCount() > 1);
				SectioningCookie.getInstance().setWaitListsOpened(event.getValue());
			}
		});
		label.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iOpenCloseImage.isVisible()) {
					iOpenCloseImage.setValue(!iOpenCloseImage.getValue(), true);
				}
			}
		});
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		header.add(new UniTimeTableHeader(""));
		header.add(new UniTimeTableHeader(MESSAGES.colWaitListedTimeStamp()));
		header.add(new UniTimeTableHeader(MESSAGES.colCourse()));
		header.add(new UniTimeTableHeader(MESSAGES.colTitle()));
		header.add(new UniTimeTableHeader(MESSAGES.colCredit()));
		header.add(new UniTimeTableHeader(MESSAGES.colWaitListPosition()));
		header.add(new UniTimeTableHeader(MESSAGES.colRequirements()));
		header.add(new UniTimeTableHeader(MESSAGES.colWaitListErrors()));
		iTable.addRow(null, header);
		
		iOpenCloseImage.setValue(SectioningCookie.getInstance().isWaitListsOpened());
		iPanel.setVisible(iOpenCloseImage.getValue());
		setVisible(false);
	}
	
	public void showWaiting() {
		iWaiting.setVisible(true);
		iOpenCloseImage.setVisible(false);
		iPanel.setVisible(false);
		setVisible(true);
	}
	
	public void hideWaiting() {
		iWaiting.setVisible(false);
		iOpenCloseImage.setVisible(true);
		iPanel.setVisible(iOpenCloseImage.getValue() && iTable.getRowCount() > 1);
		setVisible(iTable.getRowCount() > 1);
	}

	public void populate(CourseRequestInterface value) {
		iRequests = value;
		iTable.clearTable(1);
		if (iRequests != null && iRequests.getWaitListMode() == WaitListMode.WaitList) {
			NumberFormat df = NumberFormat.getFormat("0.#");
			boolean hasPrefs = false;
			boolean hasPosition = false;
			request: for (Request request: iRequests.getCourses()) {
				if (request.isWaitList() && request.hasRequestedCourse()) {
					boolean firstLine = true;
					for (RequestedCourse rc: request.getRequestedCourse())
						if (rc.getStatus() == RequestedCourseStatus.ENROLLED) continue request;
					for (RequestedCourse rc: request.getRequestedCourse()) {
						if (rc.hasCourseId() && rc.isCanWaitList() && rc.getStatus() != RequestedCourseStatus.ENROLLED) {
							P p = new P("icons");
							String style = "pending";
							if (rc.getStatus() != null) {
								switch (rc.getStatus()) {
								case OVERRIDE_APPROVED:
									p.add(new Icon(RESOURCES.specRegApproved(), MESSAGES.hintSpecRegApproved()));
									style = "approved";
									break;
								case OVERRIDE_CANCELLED:
									p.add(new Icon(RESOURCES.specRegCancelled(), MESSAGES.hintSpecRegCancelled()));
									style = "cancelled";
									break;
								case OVERRIDE_PENDING:
									p.add(new Icon(RESOURCES.specRegPending(), MESSAGES.hintSpecRegPending()));
									style = "pending";
									break;
								case OVERRIDE_REJECTED:
									p.add(new Icon(RESOURCES.specRegRejected(), MESSAGES.hintSpecRegRejected()));
									style = "rejected";
									break;
								case OVERRIDE_NEEDED:
								case NEW_REQUEST:
									p.add(new Icon(RESOURCES.requestNeeded(), MESSAGES.reqStatusNeeded()));
									style = "needed";
									break;
								case SAVED:
									p.add(new Icon(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed()));
									style = "saved";
									break;
								}
							} else {
								p.add(new Icon(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed()));
								style = "saved";
							}
							
							List<Widget> row = new ArrayList<Widget>();
							row.add(p);
							
							String reqNote = rc.getRequestorNote();
							if ((reqNote == null || reqNote.isEmpty()) && iSpecReg.isAllowChangeRequestNote() && rc.getStatus() == RequestedCourseStatus.OVERRIDE_PENDING)
								reqNote = MESSAGES.noRequestNoteClickToChange();
							DateAndNoteCell date = new DateAndNoteCell(firstLine ? request.getWaitListedTimeStamp() : null, reqNote); 
							if (iSpecReg.isAllowChangeRequestNote() && rc.getStatus() == RequestedCourseStatus.OVERRIDE_PENDING) {
								date.getElement().getStyle().setCursor(Cursor.POINTER);
								date.addClickHandler(new ClickHandler() {
									@Override
									public void onClick(ClickEvent event) {
										iSpecReg.getChangeRequestorNoteInterface().changeRequestorNote(rc);
										event.stopPropagation();
									}
								});
							}
							row.add(date);
							
							row.add(new Label(rc.getCourseName()));
							row.add(new Label(rc.hasCourseTitle() ? rc.getCourseTitle() : ""));
							row.add(new Label(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : ""));
							
							if (rc.hasWaitListPosition() && rc.getStatus() != RequestedCourseStatus.NEW_REQUEST && rc.getStatus() != RequestedCourseStatus.OVERRIDE_NEEDED) {
								hasPosition = true;
								row.add(new Label(rc.getWaitListPosition()));
							} else {
								row.add(new Label());
							}
							
							
							Collection<Preference> prefs = null;
							if (rc.hasSelectedIntructionalMethods()) {
								if (rc.hasSelectedClasses()) {
									prefs = new ArrayList<Preference>(rc.getSelectedIntructionalMethods().size() + rc.getSelectedClasses().size());
									prefs.addAll(new TreeSet<Preference>(rc.getSelectedIntructionalMethods()));
									prefs.addAll(new TreeSet<Preference>(rc.getSelectedClasses()));
								} else {
									prefs = new TreeSet<Preference>(rc.getSelectedIntructionalMethods());
								}
							} else if (rc.hasSelectedClasses()) {
								prefs = new TreeSet<Preference>(rc.getSelectedClasses());
							}
							if (prefs != null && !prefs.isEmpty()) {
								for (Iterator<Preference> i = prefs.iterator(); i.hasNext();) {
									Preference pr = i.next();
									if (!pr.isRequired()) i.remove();
								}
							}
							row.add(new Label(ToolBox.toString(prefs)));
							if (prefs != null && !prefs.isEmpty()) hasPrefs = true;

							String note = null;
							if (iRequests.hasConfirmations()) {
								for (CourseMessage m: iRequests.getConfirmations()) {
									if ("NO_ALT".equals(m.getCode())) continue;
									if ("CREDIT".equals(m.getCode())) continue;
									if (m.hasCourse() && rc.getCourseId().equals(m.getCourseId())) {
										if (note == null) {
											note = (m.isError() ? "<span class='error'>" : "<span class='"+style+"'>") + m.getMessage() + "</span>";
										} else {
											note += "\n" + (m.isError() ? "<span class='error'>" : "<span class='"+style+"'>") + m.getMessage() + "</span>";
										}
									}
								}
							}
							if (rc.hasStatusNote()) {
								note = (note == null ? "" : note + "<br>") + "<span class='note'>" + rc.getStatusNote() + "</span>";
							}
							HTML errorsLabel = new HTML(note == null ? "" : note); errorsLabel.addStyleName("waitlists-errors");
							row.add(errorsLabel);
							int idx = iTable.addRow(rc, row);
							if (firstLine && idx > 1) {
								for (int c = 0; c < iTable.getCellCount(idx); c++)
									iTable.getCellFormatter().addStyleName(idx, c, "top-border-dashed");								
							}
							firstLine = false;
						}
					}
				}
			}
			if (iRequests.hasMaxCreditOverride()) {
				P p = new P("icons");
				String style = "pending";
				if (iRequests.getMaxCreditOverrideStatus() != null) {
					switch (iRequests.getMaxCreditOverrideStatus()) {
					case OVERRIDE_APPROVED:
						p.add(new Icon(RESOURCES.specRegApproved(), MESSAGES.hintSpecRegApproved()));
						style = "approved";
						break;
					case OVERRIDE_CANCELLED:
						p.add(new Icon(RESOURCES.specRegCancelled(), MESSAGES.hintSpecRegCancelled()));
						style = "cancelled";
						break;
					case OVERRIDE_PENDING:
						p.add(new Icon(RESOURCES.specRegPending(), MESSAGES.hintSpecRegPending()));
						style = "pending";
						break;
					case OVERRIDE_REJECTED:
						p.add(new Icon(RESOURCES.specRegRejected(), MESSAGES.hintSpecRegRejected()));
						style = "rejected";
						break;
					case OVERRIDE_NEEDED:
					case NEW_REQUEST:
						p.add(new Icon(RESOURCES.requestNeeded(), MESSAGES.reqStatusNeeded()));
						style = "needed";
						break;
					case SAVED:
						p.add(new Icon(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed()));
						style = "saved";
						break;
					}
				} else {
					p.add(new Icon(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed()));
					style = "saved";
				}
				
				List<Widget> row = new ArrayList<Widget>();
				row.add(p);
				
				String reqNote = iRequests.getRequestorNote();
				if ((reqNote == null || reqNote.isEmpty()) && iSpecReg.isAllowChangeRequestNote() && iRequests.getMaxCreditOverrideStatus() == RequestedCourseStatus.OVERRIDE_PENDING)
					reqNote = MESSAGES.noRequestNoteClickToChange();
				DateAndNoteCell date = new DateAndNoteCell(iRequests.getMaxCreditOverrideTimeStamp(), reqNote); 
				if (iSpecReg.isAllowChangeRequestNote() && iRequests.getMaxCreditOverrideStatus() == RequestedCourseStatus.OVERRIDE_PENDING) {
					date.getElement().getStyle().setCursor(Cursor.POINTER);
					date.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							iSpecReg.getChangeRequestorNoteInterface().changeRequestorCreditNote(iRequests);
							event.stopPropagation();
						}
					});
				}
				row.add(date);
				row.add(new Label(""));
				row.add(new Label(""));
				row.add(new Label(df.format(iRequests.getMaxCreditOverride())));
				row.add(new Label(""));
				row.add(new Label(""));
				String note = null;
				if (iRequests.hasCreditWarning())
					note = "<span class='"+style+"'>" + iRequests.getCreditWarning() + "</span>";
				else
					note = "<span class='"+style+"'>" + MESSAGES.creditWarning(iRequests.getMaxCredit()) + "</span>";
				if (iRequests.hasCreditNote())
					note += "\n<span class='note'>" + iRequests.getCreditNote() + "</span>";
				HTML errorsLabel = new HTML(note); errorsLabel.addStyleName("waitlists-errors");
				row.add(errorsLabel);
				int idx = iTable.addRow(null, row);
				if (idx > 1) {
					for (int c = 0; c < iTable.getCellCount(idx); c++)
						iTable.getCellFormatter().addStyleName(idx, c, "top-border-dashed");
				}
			}
			iTable.setColumnVisible(5, hasPosition);
			iTable.setColumnVisible(6, hasPrefs);
		}
		setVisible(iTable.getRowCount() > 1);
		iPanel.setVisible(iOpenCloseImage.getValue() && iTable.getRowCount() > 1);
	}

	public CourseRequestInterface getRequest() {
		return iRequests;
	}
	
	protected class DateAndNoteCell extends Label {
		public DateAndNoteCell(Date date, String note) {
			super(date == null ? note == null ? "" : note : sModifiedDateFormat.format(date) + (note == null || note.isEmpty() ? "" : "\n" + note));
			addStyleName("date-and-note");
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
}
