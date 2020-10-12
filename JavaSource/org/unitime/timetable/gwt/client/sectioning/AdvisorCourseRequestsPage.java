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
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.widgets.CourseFinderClasses;
import org.unitime.timetable.gwt.client.widgets.CourseFinderDetails;
import org.unitime.timetable.gwt.client.widgets.DataProvider;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.DegreePlanInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeHandler;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionInfo;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionMatcher;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.ClassAssignment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.FreeTime;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Preference;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourseStatus;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisorCourseRequestSubmission;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentSectioningContext;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.SpecialRegistrationInterface.SpecialRegistrationContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;

/**
 * @author Tomas Muller
 */
public class AdvisorCourseRequestsPage extends SimpleForm implements TakesValue<CourseRequestInterface> {
	private static final SectioningServiceAsync sSectioningService = GWT.create(SectioningService.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private UniTimeHeaderPanel header, footer;
	private Lookup iLookupDialog = null;
	private Label iStudentName, iStudentExternalId, iTerm, iStudentEmail;
	private Label iAdvisorEmail = null;
	private AdvisorAcademicSessionSelector iSession = null;
	private SpecialRegistrationContext iSpecRegCx = null;
	private Label iTotalCredit = null;
	private ListBox iStatus = null;
	private TextArea iNotes = null;
	private CheckBox iPinReleased = null;
	private Label iPin = null;
	
	private ArrayList<AdvisorCourseRequestLine> iCourses;
	private ArrayList<AdvisorCourseRequestLine> iAlternatives;
	private AdvisingStudentDetails iDetails;

	private DegreePlansSelectionDialog iDegreePlansSelectionDialog = null;
	private DegreePlanDialog iDegreePlanDialog = null;
	private AriaMultiButton iDegreePlan = null;
	
	private int iStudentRequestHeaderLine = 0;
	private int iAdisorRequestsHeaderLine = 0;
	private int iPinLine = 0;
	private int iStatusLine = 0;
	
	private ScheduleStatus iStatusBox = null;
	private ScheduleStatus iStudentStatus = null;
	private WebTable iRequests;
	private AdvisorCourseRequestsTable iAdvisorRequests;
	private StudentSectioningContext iContext;
	
	private CheckBox iEmailConfirmationHeader, iEmailConfirmationFooter;
	
	public AdvisorCourseRequestsPage() {
		super(6);
		UniTimePageHeader.getInstance().getLeft().setVisible(false);
		UniTimePageHeader.getInstance().getLeft().setPreventDefault(true);
		addStyleName("unitime-AdvisorCourseRequests");
		iContext = new StudentSectioningContext(); iContext.setOnline(true);
		header = new UniTimeHeaderPanel();
		header.addStyleName("unitime-PageHeaderFooter");
		header.addButton("lookup", MESSAGES.buttonLookupStudent(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		header.addButton("print", MESSAGES.buttonExportPdf(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		header.setEnabled("print", false);
		header.addButton("submit", MESSAGES.buttonSubmitPrint(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		header.setEnabled("submit", false);
		
		addHeaderRow(header);
		
		iStudentName = new Label(); iStudentName.addStyleName("student-name");
		iStudentExternalId = new Label(); iStudentExternalId.addStyleName("student-id");
		iStudentEmail = new Label(); iStudentEmail.addStyleName("student-email");
		
		addDoubleRow(MESSAGES.propStudentName(), iStudentName, 1,
				MESSAGES.propStudentExternalId(), iStudentExternalId, 3);
		
		iSession = new AdvisorAcademicSessionSelector();
		
		iAdvisorEmail = new Label(); iAdvisorEmail.addStyleName("advisor-email");
		
		iTerm = new Label(); iTerm.addStyleName("term");
		addDoubleRow(MESSAGES.propStudentEmail(), iStudentEmail, 1,
				MESSAGES.propAcademicSession(), iTerm, 3);
		
		iSession.addAcademicSessionChangeHandler(new AcademicSessionChangeHandler() {
			@Override
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				iTerm.setText(iSession.getAcademicSessionName() == null ? "" : iSession.getAcademicSessionName());
				iContext.setSessionId(event.getNewAcademicSessionId());
				iContext.setStudentId(null);
				iLookupDialog.setOptions("mustHaveExternalId,source=students,session=" + event.getNewAcademicSessionId());
				header.setEnabled("submit", false);
				header.setEnabled("print", false);
				iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
				iEmailConfirmationHeader.setVisible(false);
				iEmailConfirmationFooter.setVisible(false);
				iStatusBox.clear();
				LoadingWidget.getInstance().show(MESSAGES.loadingAdvisorRequests(iStudentName.getText()));
				sSectioningService.getStudentAdvisingDetails(iSession.getAcademicSessionId(), iStudentExternalId.getText(), new AsyncCallback<AdvisingStudentDetails>() {
					@Override
					public void onSuccess(AdvisingStudentDetails result) {
						LoadingWidget.getInstance().hide();
						iDetails = result;
						iContext.setStudentId(iDetails == null ? null : iDetails.getStudentId());
						header.setEnabled("submit", result.isCanUpdate());
						header.setEnabled("print", !result.isCanUpdate());
						iDegreePlan.setVisible(result.isDegreePlan()); iDegreePlan.setEnabled(result.isDegreePlan());
						iEmailConfirmationHeader.setVisible(result.isCanUpdate() && result.isCanEmail());
						iEmailConfirmationFooter.setVisible(result.isCanUpdate() && result.isCanEmail());
						iAdvisorEmail.setText(result.getAdvisorEmail() == null ? "" : result.getAdvisorEmail());
						iStudentName.setText(result.getStudentName());
						iStudentEmail.setText(result.getStudentEmail() == null ? "" : result.getStudentEmail());
						iStudentExternalId.setText(result.getStudentExternalId());
						fillInStudentRequests();
						if (result != null && result.getStudentRequest() != null && result.getRequest().hasErrorMessage())
							iStudentStatus.error(result.getRequest().getErrorMessaeg(), false);
						if (result.isCanUpdate()) {
							clearAdvisorRequests();
							setRequest(result.getRequest());
							iStatus.clear();
							if (result.getStatus() != null) {
								iStatus.addItem(result.getStatus().getLabel(), result.getStatus().getReference());
							} else {
								iStatus.addItem("", "");
							}
							iStatus.setSelectedIndex(0);
							if (result.hasStatuses()) {
								for (StudentStatusInfo status: result.getStatuses()) {
									if (!status.equals(result.getStatus()))
										iStatus.addItem(status.getLabel(), status.getReference());
								}
								iStatus.setEnabled(true);
							} else {
								iStatus.setEnabled(false);
							}
							getRowFormatter().setVisible(iStatusLine, true);
						} else {
							setAdvisorRequests(result.getRequest());
							getRowFormatter().setVisible(iStatusLine, false);
						}
						History.newItem(String.valueOf(result.getStudentId()), false);
					}
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iStatusBox.error(MESSAGES.advisorRequestsLoadFailed(caught.getMessage()), caught);
					}
				});
			}
		});
		iTerm.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iSession.selectSession();
			}
		});
		iStudentName.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		iStudentExternalId.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (isPageChanged()) {
					UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
						@Override
						public void execute() {
							lookupStudent();
						}
					});
				} else {
					lookupStudent();
				}
			}
		});
		
		iStatus = new ListBox();
		iStatus.addStyleName("status");
		iStatusLine = addDoubleRow(MESSAGES.propAdvisorEmail(), iAdvisorEmail, 1,
				MESSAGES.propStudentStatus(), iStatus, 3);
		
		iPinReleased = new CheckBox(MESSAGES.propStudentPin()); iPinReleased.addStyleName("unitime-PinToggle");
		iPin = new Label(); iPin.addStyleName("unitime-Pin");
		iPin.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iPinReleased.setValue(!iPinReleased.getValue());
				pinReleaseChanged();
			}
		});
		iPinLine = addDoubleRow(new Label(), new Label(), 1, iPinReleased, iPin, 3);
		getRowFormatter().setVisible(iPinLine, true);
		iPinReleased.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				pinReleaseChanged();
			}
		});
		
		iStudentStatus = new ScheduleStatus();
		addRow(iStudentStatus);
		
		UniTimeHeaderPanel studentReqs = new UniTimeHeaderPanel(MESSAGES.studentCourseRequests());
		iStudentRequestHeaderLine = addHeaderRow(studentReqs);
		iRequests = new WebTable();
		iRequests.setEmptyMessage(MESSAGES.emptyRequests());
		iRequests.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colPriority(), 1, "25px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
				new WebTable.Cell(MESSAGES.colTitle(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colPreferences(), 1, "100px"),
				new WebTable.Cell(MESSAGES.colWarnings(), 1, "200px"),
				new WebTable.Cell(MESSAGES.colStatus(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colCritical(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colWaitList(), 1, "20px"),
				new WebTable.Cell(MESSAGES.colRequestTimeStamp(), 1, "50px")));
		addRow(iRequests);
		
		UniTimeHeaderPanel advisorReqs = new UniTimeHeaderPanel(MESSAGES.advisorCourseRequests());
		iAdisorRequestsHeaderLine = addHeaderRow(advisorReqs);
		iAdvisorRequests = new AdvisorCourseRequestsTable();
		addRow(iAdvisorRequests);
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine, false);
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine + 1, false);
		
		iSpecRegCx = new SpecialRegistrationContext();
		
		iCourses = new ArrayList<AdvisorCourseRequestLine>();
		iAlternatives = new ArrayList<AdvisorCourseRequestLine>();
		
		UniTimeHeaderPanel requests = new UniTimeHeaderPanel(MESSAGES.advisorRequestsCourses());
		requests.setMessage(MESSAGES.headCreditHoursNotes());
		requests.addStyleName("requests-header");
		addHeaderRow(requests);
		
		for (int i = 0; i < 9; i++) {
			final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iContext, i, false, null, iSpecRegCx);
			line.insert(this, getRowCount());
			iCourses.add(line);
			if (i > 0) {
				AdvisorCourseRequestLine prev = iCourses.get(i - 1);
				line.setPrevious(prev); prev.setNext(line);
			}
			line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
				@Override
				public void onValueChange(ValueChangeEvent<Request> event) {
					if (event.getValue() != null && iCourses.indexOf(line) + 1 == iCourses.size())
						addCourseLine();
					updateTotalCredits();
				}
			});
		}
		
		Label l = new Label(MESSAGES.labelTotalPriorityCreditHours()); l.addStyleName("total-credit-label");
		int row = getRowCount();
		setWidget(row, 0, l);
		getFlexCellFormatter().setColSpan(row, 0, 2);
		iTotalCredit = new Label(MESSAGES.credit(0f)); iTotalCredit.addStyleName("total-credit-value");
		setWidget(row, 1, iTotalCredit);
		
		UniTimeHeaderPanel alternatives = new UniTimeHeaderPanel(MESSAGES.advisorRequestsAlternatives());
		alternatives.setMessage(MESSAGES.courseRequestsAlternativesNote());
		addHeaderRow(alternatives);
		for (int i = 0; i < 2; i++) {
			final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iContext, i, true, null, iSpecRegCx);
			line.insert(this, getRowCount());
			iAlternatives.add(line);
			if (i == 0) {
				AdvisorCourseRequestLine prev = iCourses.get(iCourses.size() - 1);
				line.setPrevious(prev); prev.setNext(line);
			} else {
				AdvisorCourseRequestLine prev = iAlternatives.get(i - 1);
				line.setPrevious(prev); prev.setNext(line);
			}
			line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
				@Override
				public void onValueChange(ValueChangeEvent<Request> event) {
					if (event.getValue() != null && iAlternatives.indexOf(line) + 1 == iAlternatives.size())
						addAlternativeLine();
				}
			});
		}
		
		iCourses.get(1).getCourses().get(0).setHint(MESSAGES.courseRequestsHint1());
		iCourses.get(3).getCourses().get(0).setHint(MESSAGES.courseRequestsHint3());
		iCourses.get(4).getCourses().get(0).setHint(MESSAGES.courseRequestsHint4());
		iCourses.get(iCourses.size()-1).getCourses().get(0).setHint(MESSAGES.courseRequestsHint8());
		iAlternatives.get(0).getCourses().get(0).setHint(MESSAGES.courseRequestsHintA0());
		
		iNotes = new TextArea();
		iNotes.setStyleName("unitime-TextArea");
		iNotes.addStyleName("advisor-notes");
		iNotes.setText("");
		iNotes.getElement().setAttribute("maxlength", "2048");
		int notesRow = addRow(MESSAGES.propAdvisorNotes(), iNotes);
		for (int i = 0; i < getCellCount(notesRow); i++)
			getCellFormatter().addStyleName(notesRow, i, "advisor-notes-line");
		final Timer timer = new Timer() {
			@Override
			public void run() {
				resizeNotes();
			}
		};
		iNotes.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				timer.schedule(10);
			}
		});
		iNotes.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				timer.schedule(10);
			}
		});
		
		footer = header.clonePanel();
		footer.addStyleName("unitime-PageHeaderFooter");
		addBottomRow(footer);
		
		iEmailConfirmationHeader = new CheckBox(MESSAGES.checkSendEmailConfirmation(), true);
		iEmailConfirmationHeader.addStyleName("toggle");
		header.insertRight(iEmailConfirmationHeader, true);
		iEmailConfirmationHeader.setVisible(false);
		
		iEmailConfirmationFooter = new CheckBox(MESSAGES.checkSendEmailConfirmation(), true);
		iEmailConfirmationFooter.addStyleName("toggle");
		footer.insertRight(iEmailConfirmationFooter, true);
		iEmailConfirmationFooter.setVisible(false);
		
		iEmailConfirmationHeader.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iEmailConfirmationFooter.setValue(event.getValue(), false);
				SectioningStatusCookie.getInstance().setAdvisorRequestsEmailStudent(event.getValue());
			}
		});
		iEmailConfirmationFooter.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iEmailConfirmationHeader.setValue(event.getValue(), false);
				SectioningStatusCookie.getInstance().setAdvisorRequestsEmailStudent(event.getValue());
			}
		});
		iEmailConfirmationHeader.setValue(SectioningStatusCookie.getInstance().isAdvisorRequestsEmailStudent());
		iEmailConfirmationFooter.setValue(iEmailConfirmationHeader.getValue());
		
		iStatusBox = new ScheduleStatus();
		addRow(iStatusBox);
		
		iDegreePlan = new AriaMultiButton(MESSAGES.buttonDegreePlan());
		iDegreePlan.setTitle(MESSAGES.hintDegreePlan());
		iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
		header.insertLeft(iDegreePlan, true);
		footer.insertLeft(iDegreePlan.createClone(), true);
		
		
		Window.addWindowClosingHandler(new Window.ClosingHandler() {
			@Override
			public void onWindowClosing(ClosingEvent event) {
				if (isPageChanged()) {
					if (LoadingWidget.getInstance().isShowing())
						LoadingWidget.getInstance().hide();
					event.setMessage(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave());
				}
			}
		});
		
		iLookupDialog = new Lookup();
		iLookupDialog.setText(MESSAGES.dialogStudentLookup());
		iLookupDialog.setOptions("mustHaveExternalId,source=students");
		iLookupDialog.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				studentSelected(event.getValue());
			}
		});

		if (Location.getParameter("student") != null) {
			iStudentExternalId.setText(Location.getParameter("student"));
			iSession.selectSession(new AcademicSessionMatcher() {
				protected boolean matchCampus(AcademicSessionInfo info, String campus) {
					if (info.hasExternalCampus() && campus.equalsIgnoreCase(info.getExternalCampus())) return true;
					return campus.equalsIgnoreCase(info.getCampus());
				}

				protected boolean matchTerm(AcademicSessionInfo info, String term) {
					if (info.hasExternalTerm() && term.equalsIgnoreCase(info.getExternalTerm())) return true;
					return term.equalsIgnoreCase(info.getTerm() + info.getYear()) || term.equalsIgnoreCase(info.getYear() + info.getTerm()) || term.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus());
				}

				protected boolean matchSession(AcademicSessionInfo info, String session) {
					if (info.hasExternalTerm() && info.hasExternalCampus() && session.equalsIgnoreCase(info.getExternalTerm() + info.hasExternalCampus())) return true;
					return session.equalsIgnoreCase(info.getTerm() + info.getYear() + info.getCampus()) || session.equalsIgnoreCase(info.getTerm() + info.getYear()) || session.equals(info.getSessionId().toString());
				}

				@Override
				public boolean match(AcademicSessionInfo info) {
					String campus = Location.getParameter("campus");
					if (campus != null && !matchCampus(info, campus)) return false;
					String term = Location.getParameter("term");
					if (term != null && !matchTerm(info, term)) return false;
					String session = Location.getParameter("session");
					if (session != null && !matchSession(info, session)) return false;
					return true;
				}
			}, new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {}
				@Override
				public void onSuccess(Boolean result) {}
			});
		} else if (Window.Location.getHash() != null && !Window.Location.getHash().isEmpty()) {
			loadStudent(Window.Location.getHash().substring(1));
		} else {
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					lookupStudent();
				}
			});
		}
		
		iDegreePlan.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				LoadingWidget.getInstance().show(MESSAGES.waitListDegreePlans());
				sSectioningService.listDegreePlans(iContext, new AsyncCallback<List<DegreePlanInterface>>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						if (caught instanceof SectioningException) {
							SectioningException s = (SectioningException)caught;
							if (s.isInfo())
								iStatusBox.info(s.getMessage());
							else if (s.isWarning())
								iStatusBox.warning(s.getMessage());
							else if (s.isError())
								iStatusBox.error(s.getMessage());
							else
								iStatusBox.error(MESSAGES.failedListDegreePlans(s.getMessage()), s);
						} else {
							iStatusBox.error(MESSAGES.failedListDegreePlans(caught.getMessage()), caught);
						}
					}
					@Override
					public void onSuccess(List<DegreePlanInterface> result) {
						LoadingWidget.getInstance().hide();
						if (result == null || result.isEmpty()) {
							iStatusBox.info(MESSAGES.failedNoDegreePlans());
						} else {
							CourseFinderDetails details = new CourseFinderDetails();
							details.setDataProvider(new DataProvider<CourseAssignment, String>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<String> callback) {
									sSectioningService.retrieveCourseDetails(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							CourseFinderClasses classes = new CourseFinderClasses(false, iSpecRegCx);
							classes.setDataProvider(new DataProvider<CourseAssignment, Collection<ClassAssignment>>() {
								@Override
								public void getData(CourseAssignment source, AsyncCallback<Collection<ClassAssignment>> callback) {
									sSectioningService.listClasses(iContext, source.hasUniqueName() ? source.getCourseName() : source.getCourseNameWithTitle(), callback);
								}
							});
							if (iDegreePlanDialog == null) {
								iDegreePlanDialog = new DegreePlanDialog(StudentSectioningPage.Mode.REQUESTS, AdvisorCourseRequestsPage.this, null, details, classes) {
									protected void doBack() {
										super.doBack();
										iDegreePlansSelectionDialog.show();
									}
								};
							}
							if (iDegreePlansSelectionDialog == null) {
								iDegreePlansSelectionDialog = new DegreePlansSelectionDialog() {
									public void doSubmit(DegreePlanInterface plan) {
										super.doSubmit(plan);
										iDegreePlanDialog.open(plan, true);
									}
								};
							}
							if (result.size() == 1)
								iDegreePlanDialog.open(result.get(0), false);
							else
								iDegreePlansSelectionDialog.open(result);
						}
					}
				});
				
			}
		});
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(final ValueChangeEvent<String> event) {
				if (event.getValue() != null && !event.getValue().isEmpty()) {
					if (isPageChanged()) {
						UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
							@Override
							public void execute() {
								loadStudent(event.getValue());
							}
						});
					} else {
						loadStudent(event.getValue());
					}
				}
			}
		});
	}
	
	protected void pinReleaseChanged() {
		if (iPinReleased.getValue()) {
			iPin.setText(iDetails == null || iDetails.getRequest() == null || !iDetails.getRequest().hasPin() ? "" : iDetails.getRequest().getPin());
			iPin.removeStyleName("unitime-PinNotReleased");
		} else {
			iPin.setText(MESSAGES.pinNotReleasedToStudent());
			iPin.addStyleName("unitime-PinNotReleased");
		}
	}
	
	protected void loadStudent(String studentId) {
		header.setEnabled("submit", false);
		header.setEnabled("print", false);
		iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
		iEmailConfirmationHeader.setVisible(false);
		iEmailConfirmationFooter.setVisible(false);
		iStatusBox.clear();
		LoadingWidget.getInstance().show(MESSAGES.loadingData());
		sSectioningService.getStudentInfo(Long.valueOf(studentId), new AsyncCallback<StudentInfo>() {
			@Override
			public void onSuccess(StudentInfo result) {
				LoadingWidget.getInstance().hide();
				iTerm.setText(result.getSessionName());
				iContext.setSessionId(result.getSessionId());
				iContext.setStudentId(result.getStudentId());
				iLookupDialog.setOptions("mustHaveExternalId,source=students,session=" + result.getSessionId());
				iStudentName.setText(result.getStudentName());
				iStudentExternalId.setText(result.getStudentExternalId());
				iStudentEmail.setText(result.getStudentEmail() == null ? "" : result.getStudentEmail());
				if (result.getSessionId().equals(iSession.getAcademicSessionId())) {
					iSession.selectSession(iSession.getAcademicSessionInfo(), true);
				} else {
					iSession.selectSession(result.getSessionId(), new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {}
						@Override
						public void onFailure(Throwable caught) {}
					});
				}
			}
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iStatusBox.error(MESSAGES.advisorRequestsLoadFailed(caught.getMessage()) + "\n" + MESSAGES.sessionExpiredClickToLogin(), caught, new Command() {
					@Override
					public void execute() {
						Window.open("selectPrimaryRole.do?list=Y&m=" + URL.encodeQueryString(MESSAGES.sessionExpiredClickToLogin())
						+"&target=" + URL.encodeQueryString(Window.Location.getHref()), "_self", "");
					}
				});
			}
		});
	}
	
	private void resizeNotes() {
		iNotes.setHeight("50px");
		if (!iNotes.getText().isEmpty()) {
			iNotes.setHeight(Math.max(50, iNotes.getElement().getScrollHeight()) + "px");
		}
	}
	
	private void updateTotalCredits() {
		float min = 0, max = 0;
		for (AdvisorCourseRequestLine line: iCourses) {
			min += line.getCreditMin();
			max += line.getCreditMax();
		}
		if (min < max)
			iTotalCredit.setText(MESSAGES.creditRange(min, max));
		else
			iTotalCredit.setText(MESSAGES.credit(min));
	}
	
	private void clearRequests() {
		for (AdvisorCourseRequestLine line: iCourses)
			line.setValue(null);
		for (AdvisorCourseRequestLine line: iAlternatives)
			line.setValue(null);
		iStatusBox.clear();
		iNotes.setText("");
		iPinReleased.setValue(false);
		pinReleaseChanged();
		getRowFormatter().setVisible(iPinLine, false);
		resizeNotes();
		updateTotalCredits();
	}
	
	private void addCourseLine() {
		int i = iCourses.size();
		final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iContext, i, false, null, iSpecRegCx);
		iCourses.add(line);
		AdvisorCourseRequestLine prev = iCourses.get(i - 1);
		prev.getCourses().get(0).setHint("");
		line.getCourses().get(0).setHint(MESSAGES.courseRequestsHint8());
		AdvisorCourseRequestLine next = (iAlternatives.isEmpty() ? null : iAlternatives.get(0));
		line.setPrevious(prev); prev.setNext(line);
		if (next != null) {
			line.setNext(next); next.setPrevious(line);
		}
		line.insert(this, insertRow(8 + iCourses.size()));
		line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
			@Override
			public void onValueChange(ValueChangeEvent<Request> event) {
				if (event.getValue() != null && iCourses.indexOf(line) + 1 == iCourses.size())
					addCourseLine();
				updateTotalCredits();
			}
		});
	}
	
	private void addAlternativeLine() {
		int i = iAlternatives.size();
		final AdvisorCourseRequestLine line = new AdvisorCourseRequestLine(iContext, i, true, null, iSpecRegCx);
		iAlternatives.add(line);
		AdvisorCourseRequestLine prev = (i == 0 ? iCourses.get(iCourses.size() - 1) : iAlternatives.get(i - 1));
		if (prev != null) {
			line.setPrevious(prev); prev.setNext(line);
		}
		line.insert(this, insertRow(8 + iCourses.size() + 2 + iAlternatives.size()));
		line.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface.Request>() {
			@Override
			public void onValueChange(ValueChangeEvent<Request> event) {
				if (event.getValue() != null && iAlternatives.indexOf(line) + 1 == iAlternatives.size())
					addAlternativeLine();
			}
		});
	}
	
	protected boolean isPageChanged() {
		if (iDetails != null && !iDetails.isCanUpdate()) return false;
		if (!iNotes.getText().equals(iDetails == null || iDetails.getRequest() == null || !iDetails.getRequest().hasCreditNote() ? "" : iDetails.getRequest().getCreditNote()))
			return true;
		if (iDetails == null || iDetails.getRequest() == null)
			return !getRequest().isEmpty();
		else {
			return !iDetails.getRequest().equals(getRequest());
		}
	}
	
	protected void lookupStudent() {
		iLookupDialog.center();
	}
	
	protected void studentSelected(PersonInterface person) {
		iContext.setSessionId(null);
		iContext.setStudentId(null);
		if (person == null || person.getId() == null || person.getId().isEmpty()) {
			iStudentName.setText("");
			iStudentExternalId.setText("");
			iStudentEmail.setText("");
			iTerm.setText("");
			header.setEnabled("submit", false);
			header.setEnabled("print", false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			iEmailConfirmationHeader.setVisible(false);
			iEmailConfirmationFooter.setVisible(false);
			iStudentStatus.clear();
			clearRequests();
			clearStudentRequests();
			clearAdvisorRequests();
		} else {
			iStudentName.setText(person.getName());
			iStudentExternalId.setText(person.getId());
			iStudentEmail.setText(person.getEmail() == null ? "" : person.getEmail());
			iStudentStatus.clear();
			clearRequests();
			iSession.selectSessionNoCheck();
			clearStudentRequests();
			clearAdvisorRequests();
		}
	}
	
	private class AdvisorAcademicSessionSelector extends AcademicSessionSelector {
		public AdvisorAcademicSessionSelector() {
			super(UniTimePageHeader.getInstance().getRight(), StudentSectioningPage.Mode.REQUESTS);
		}

		public void selectSession() {
			if (isPageChanged()) {
				UniTimeConfirmationDialog.confirm(MESSAGES.queryLeaveAdvisorsCourseRequestsNotSave(), new Command() {
					@Override
					public void execute() {
						selectSessionNoCheck();
					}
				});
			} else {
				selectSessionNoCheck();
			}
		}
		
		@Override
		protected void listAcademicSessions(AsyncCallback<Collection<AcademicSessionInfo>> callback) {
			sSectioningService.getStudentSessions(iStudentExternalId.getText(), callback);
		}
		
		public void selectSessionNoCheck() {
			header.setEnabled("submit", false);
			header.setEnabled("print", false);
			iDegreePlan.setVisible(false); iDegreePlan.setEnabled(false);
			iEmailConfirmationHeader.setVisible(false);
			iEmailConfirmationFooter.setVisible(false);
			super.selectSession();
		}		
	}
	
	public void fillInCourses(CourseRequestInterface cr) {
		for (AdvisorCourseRequestLine line: iCourses) {
			CourseRequestInterface.Request req = line.getValue();
			if (req != null) cr.getCourses().add(req);
		}
	}
	
	public void fillInAlternatives(CourseRequestInterface cr) {
		for (AdvisorCourseRequestLine line: iAlternatives) {
			CourseRequestInterface.Request req = line.getValue();
			if (req != null) cr.getAlternatives().add(req);
		}
	}
	
	public CourseRequestInterface getRequest() {
		CourseRequestInterface cr = new CourseRequestInterface(iContext);
		fillInCourses(cr);
		fillInAlternatives(cr);
		cr.setCreditNote(iNotes.getText());
		return cr;
	}
	
	public void setRequest(CourseRequestInterface request) {
		clearRequests();
		if (request != null) {
			while (iCourses.size() < request.getCourses().size()) addCourseLine();
			for (int idx = 0; idx < request.getCourses().size(); idx++)
				iCourses.get(idx).setValue(request.getCourses().get(idx), true);
			while (iAlternatives.size() < request.getAlternatives().size()) addAlternativeLine();;
			for (int idx = 0; idx < request.getAlternatives().size(); idx++)
				iAlternatives.get(idx).setValue(request.getAlternatives().get(idx), true);
			if (request.hasCreditNote()) {
				iNotes.setText(request.getCreditNote());
				resizeNotes();
			}
			if (request.hasPin()) {
				iPin.setText(request.getPin());
				iPinReleased.setValue(request.isPinReleased());
				pinReleaseChanged();
				getRowFormatter().setVisible(iPinLine, true);
			}
		}
		updateTotalCredits();
	}
	
	public static final native String download(byte[] bytes, String name) /*-{
		var data = new Uint8Array(bytes);
		var blob = new Blob([data], {type: "application/pdf"});
		if ($wnd.navigator && $wnd.navigator.msSaveOrOpenBlob) {
			$wnd.navigator.msSaveOrOpenBlob(blob, name + ".pdf");
		} else {
			var link = $doc.createElement("a");
			link.href = $wnd.URL.createObjectURL(blob);
			link.download = name + ".pdf";
			link.target = "_blank";
			$doc.body.appendChild(link);
			link.click();
			$doc.body.removeChild(link);
			$wnd.URL.revokeObjectURL(link.href);
		}
	}-*/;
	
	protected void submit() {
		final AdvisingStudentDetails details = new AdvisingStudentDetails(iDetails);
		details.setRequest(details.isCanUpdate() ? getRequest() : iAdvisorRequests.getValue());
		details.setStatus(iDetails.getStatus(iStatus.getSelectedValue()));
		if (getRowFormatter().isVisible(iPinLine) && details.getRequest() != null) {
			details.getRequest().setPinReleased(iPinReleased.getValue());
			details.getRequest().setPin(iDetails.getRequest().getPin());
		}
		LoadingWidget.getInstance().show(details.isCanUpdate() ? MESSAGES.advisorCourseRequestsSaving() : MESSAGES.advisorCourseRequestsExporting());
		sSectioningService.submitAdvisingDetails(details, false, new AsyncCallback<AdvisorCourseRequestSubmission>() {
			@Override
			public void onSuccess(AdvisorCourseRequestSubmission result) {
				LoadingWidget.getInstance().hide();
				iDetails = details;
				iContext.setStudentId(iDetails == null ? null : iDetails.getStudentId());
				download(result.getPdf(), result.hasName() ? result.getName() : "crf-" + iTerm.getText() + "-" + iStudentName.getText() + "-" + iStudentExternalId.getText());
				final String statusLink = (result.hasLink() ?  "\n" + MESSAGES.advisorRequestsPdfLink(GWT.getHostPageBaseURL() + result.getLink()) : "");
				if (result.isUpdated()) {
					iStatusBox.info(MESSAGES.advisorRequestsSubmitOK() + statusLink);
					if (isSendEmailConformation()) {
						final StudentStatusDialog dialog = new StudentStatusDialog(new HashSet<StudentStatusInfo>(), null);
						if (SectioningStatusCookie.getInstance().hasEmailCC())
							dialog.setCC(SectioningStatusCookie.getInstance().getEmailCC());
						else
							dialog.setCC(iDetails.hasAdvisorEmail() ? iDetails.getAdvisorEmail().replace("\n", ", ") : "");
						dialog.setSubject(MESSAGES.defaulSubjectAdvisorRequests().replace("%session%", iTerm.getText()));
						dialog.setIncludeAdvisorRequests(true);
						dialog.setIncludeClassSchedule(false);
						dialog.setIncludeCourseRequests(false);
						dialog.sendStudentEmail(new Command() {
							@Override
							public void execute() {
								sSectioningService.sendEmail(iDetails.getSessionId(), iDetails.getStudentId(),
										dialog.getSubject(), dialog.getMessage(), dialog.getCC(),
										dialog.getIncludeCourseRequests(), dialog.getIncludeClassSchedule(), dialog.getIncludeAdvisorRequests(),
										dialog.isOptionalEmailToggle(),
										new AsyncCallback<Boolean>() {
											@Override
											public void onFailure(Throwable caught) {
												iStatusBox.error(MESSAGES.advisorRequestsEmailFailed(caught.getMessage()) + statusLink, caught);
											}
											@Override
											public void onSuccess(Boolean result) {
												iStatusBox.info(MESSAGES.advisorRequestsEmailSent() + statusLink);
											}
								});
							}
						}, iDetails.getEmailOptionalToggleCaption(), iDetails.getEmailOptionalToggleDefault());
					}
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				if (caught instanceof PageAccessException) {
					iStatusBox.error(MESSAGES.advisorRequestsSubmitFailed(caught.getMessage()) + "\n" + MESSAGES.sessionExpiredClickToLogin(), caught, new Command() {
						@Override
						public void execute() {
							Window.open("selectPrimaryRole.do?list=Y&target=close.jsp&menu=hide", "", 
							"toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, " +
							"width=720px, height=400px, top=" + ((Window.getClientHeight() - 400) / 2) + "px, left=" + ((Window.getClientWidth() - 720) / 2) + "px");
						}
					});
				} else {
					iStatusBox.error(MESSAGES.advisorRequestsSubmitFailed(caught.getMessage()), caught);
				}
			}
		});
	}

	@Override
	public void setValue(CourseRequestInterface value) {
		setRequest(value);
	}

	@Override
	public CourseRequestInterface getValue() {
		return getRequest();
	}
	
	protected void clearStudentRequests() {
		iRequests.setData(new WebTable.Row[] {});
		getRowFormatter().setVisible(iStudentRequestHeaderLine, false);
		getRowFormatter().setVisible(iStudentRequestHeaderLine + 1, false);
	}
	
	protected void clearAdvisorRequests() {
		iAdvisorRequests.setValue(new CourseRequestInterface());
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine, false);
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine + 1, false);
		for (int i = iAdisorRequestsHeaderLine + 2; i < getRowCount() - 2; i ++)
			getRowFormatter().setVisible(i, true);
	}
	
	protected void setAdvisorRequests(CourseRequestInterface requests) {
		iAdvisorRequests.setValue(requests);
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine, true);
		getRowFormatter().setVisible(iAdisorRequestsHeaderLine + 1, true);
		for (int i = iAdisorRequestsHeaderLine + 2; i < getRowCount() - 2; i ++)
			getRowFormatter().setVisible(i, false);
	}
	
	protected void fillInStudentRequests() {
		ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
		boolean hasPref = false, hasWarn = false, hasWait = false, hasCrit = false;
		NumberFormat df = NumberFormat.getFormat("0.#");
		if (iDetails != null && iDetails.hasStudentRequest()) {
			CheckCoursesResponse check = new CheckCoursesResponse(iDetails.getStudentRequest().getConfirmations());
			hasWarn = iDetails.getStudentRequest().hasConfirmations();
			int priority = 1;
			for (Request request: iDetails.getStudentRequest().getCourses()) {
				if (!request.hasRequestedCourse()) continue;
				boolean first = true;
				if (request.isWaitList()) hasWait = true;
				if (request.isCritical() || request.isImportant()) hasCrit = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						ImageResource icon = null; String iconText = null;
						String msg = check.getMessage(rc.getCourseName(), "\n");
						if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
							icon = RESOURCES.requestError(); iconText = (msg);
						} else if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED:
								icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
								break;
							case OVERRIDE_NEEDED:
								icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
								break;
							case SAVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
								break;				
							case OVERRIDE_REJECTED:
								icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
								break;
							case OVERRIDE_PENDING:
								icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
								break;
							case OVERRIDE_CANCELLED:
								icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
								break;
							case OVERRIDE_APPROVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
								break;
							default:
								if (check.isError(rc.getCourseName()))
									icon = RESOURCES.requestError(); iconText = (msg);
							}
						}
						if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
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
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						if (prefs != null) hasPref = true;
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT"); noteTitle = check.getMessage(rc.getCourseName(), "\n", "CREDIT"); }
						if (rc.hasRequestorNote()) { note = (note == null ? "" : note + "<br>") + rc.getRequestorNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getRequestorNote(); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit, 
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
									first && request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell("")),
								(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell("")),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : "")
								);
						if (priority > 1 && first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
						rows.add(row);
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsPriority(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : ""));
						if (priority > 1 && first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-dashed");
						rows.add(row);
					}
					first = false;
				}
				priority ++;
			}
			priority = 1;
			for (Request request: iDetails.getStudentRequest().getAlternatives()) {
				if (!request.hasRequestedCourse()) continue;
				boolean first = true;
				if (request.isWaitList()) hasWait = true;
				if (request.isCritical() || request.isImportant()) hasCrit = true;
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isCourse()) {
						ImageResource icon = null; String iconText = null;
						String msg = check.getMessage(rc.getCourseName(), "\n");
						if (check.isError(rc.getCourseName()) && (rc.getStatus() == null || rc.getStatus() != RequestedCourseStatus.OVERRIDE_REJECTED)) {
							icon = RESOURCES.requestError(); iconText = (msg);
						} else if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED:
								icon = RESOURCES.requestEnrolled(); iconText = (MESSAGES.enrolled(rc.getCourseName()));
								break;
							case OVERRIDE_NEEDED:
								icon = RESOURCES.requestNeeded(); iconText = (MESSAGES.overrideNeeded(msg));
								break;
							case SAVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.requested(rc.getCourseName()));
								break;				
							case OVERRIDE_REJECTED:
								icon = RESOURCES.requestRejected(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideRejected(rc.getCourseName()));
								break;
							case OVERRIDE_PENDING:
								icon = RESOURCES.requestPending(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overridePending(rc.getCourseName()));
								break;
							case OVERRIDE_CANCELLED:
								icon = RESOURCES.requestCancelled(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideCancelled(rc.getCourseName()));
								break;
							case OVERRIDE_APPROVED:
								icon = RESOURCES.requestSaved(); iconText = ((msg == null ? "" : MESSAGES.requestWarnings(msg) + "\n\n") + MESSAGES.overrideApproved(rc.getCourseName()));
								break;
							default:
								if (check.isError(rc.getCourseName()))
									icon = RESOURCES.requestError(); iconText = (msg);
							}
						}
						if (rc.hasRequestorNote()) iconText += "\n" + MESSAGES.requestNote(rc.getRequestorNote());
						if (rc.hasStatusNote()) iconText += "\n" + MESSAGES.overrideNote(rc.getStatusNote());
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
						if (prefs != null) hasPref = true;
						String status = "";
						if (rc.getStatus() != null) {
							switch (rc.getStatus()) {
							case ENROLLED: status = MESSAGES.reqStatusEnrolled(); break;
							case OVERRIDE_APPROVED: status = MESSAGES.reqStatusApproved(); break;
							case OVERRIDE_CANCELLED: status = MESSAGES.reqStatusCancelled(); break;
							case OVERRIDE_PENDING: status = MESSAGES.reqStatusPending(); break;
							case OVERRIDE_REJECTED: status = MESSAGES.reqStatusRejected(); break;
							}
						}
						if (status.isEmpty()) status = MESSAGES.reqStatusRegistered();
						WebTable.Cell credit = new WebTable.Cell(rc.hasCredit() ? (rc.getCreditMin().equals(rc.getCreditMax()) ? df.format(rc.getCreditMin()) : df.format(rc.getCreditMin()) + " - " + df.format(rc.getCreditMax())) : "");
						credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
						String note = null, noteTitle = null;
						if (check != null) { note = check.getMessageWithColor(rc.getCourseName(), "<br>", "CREDIT"); noteTitle = check.getMessage(rc.getCourseName(), "\n", "CREDIT"); }
						if (rc.hasRequestorNote()) { note = (note == null ? "" : note + "<br>") + rc.getRequestorNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getRequestorNote(); }
						if (rc.hasStatusNote()) { note = (note == null ? "" : note + "<br>") + rc.getStatusNote(); noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + rc.getStatusNote(); }
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsAlternate(priority) : ""),
								new WebTable.Cell(rc.getCourseName()),
								new WebTable.Cell(rc.hasCourseTitle() ? rc.getCourseTitle() : ""),
								credit,
								new WebTable.Cell(ToolBox.toString(prefs)),
								new WebTable.NoteCell(note, noteTitle),
								(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
								(first && request.isCritical() ? new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), "") :
									first && request.isImportant() ? new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), "") : new WebTable.Cell("")),
								(first && request.isWaitList() ? new WebTable.IconCell(RESOURCES.requestsWaitList(), MESSAGES.descriptionRequestWaitListed(), "") : new WebTable.Cell("")),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : "")
								);
						if (first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
						rows.add(row);
					} else if (rc.isFreeTime()) {
						String  free = "";
						for (FreeTime ft: rc.getFreeTime()) {
							if (!free.isEmpty()) free += ", ";
							free += ft.toString(CONSTANTS.shortDays(), CONSTANTS.useAmPm());
						}
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(first ? MESSAGES.courseRequestsAlternate(priority) : ""),
								new WebTable.Cell(CONSTANTS.freePrefix() + free, 3, null),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.IconCell(RESOURCES.requestSaved(), MESSAGES.requested(free), MESSAGES.reqStatusRegistered()),
								new WebTable.Cell(""),
								new WebTable.Cell(""),
								new WebTable.Cell(first && request.hasTimeStamp() ? sDF.format(request.getTimeStamp()) : ""));
						if (first)
							for (WebTable.Cell cell: row.getCells()) cell.setStyleName(priority == 1 ? "top-border-solid" : "top-border-dashed");
						rows.add(row);
					}
					first = false;
				}
				priority ++;
			}
		}
		
		if (iDetails != null && iDetails.hasStudentRequest() && iDetails.getStudentRequest().getMaxCreditOverrideStatus() != null) {
			ImageResource icon = null;
			String status = "";
			String note = null, noteTitle = null;
			String iconText = null;
			if (iDetails.getStudentRequest().hasCreditWarning()) {
				note = iDetails.getStudentRequest().getCreditWarning().replace("\n", "<br>");
				noteTitle = iDetails.getStudentRequest().getCreditWarning();
				iconText = iDetails.getStudentRequest().getCreditWarning();
				hasWarn = true;
			} else if (iDetails.getStudentRequest().getMaxCreditOverrideStatus() != RequestedCourseStatus.SAVED) {
				note = noteTitle = iconText = MESSAGES.creditWarning(iDetails.getStudentRequest().getCredit());
			}
			switch (iDetails.getStudentRequest().getMaxCreditOverrideStatus()) {
			case CREDIT_HIGH:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
				note = "<span class='text-red'>" + note + "</span>";
				iconText += "\n" + MESSAGES.creditStatusTooHigh();
				break;
			case OVERRIDE_REJECTED:
				icon = RESOURCES.requestError();
				status = MESSAGES.reqStatusRejected();
				note = "<span class='text-red'>" + note + "</span>";
				iconText += "\n" + MESSAGES.creditStatusDenied();
				break;
			case CREDIT_LOW:
			case OVERRIDE_NEEDED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusWarning();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_CANCELLED:
				icon = RESOURCES.requestNeeded();
				status = MESSAGES.reqStatusCancelled();
				iconText += "\n" + MESSAGES.creditStatusCancelled();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_PENDING:
				icon = RESOURCES.requestPending();
				status = MESSAGES.reqStatusPending();
				iconText += "\n" + MESSAGES.creditStatusPending();
				note = "<span class='text-orange'>" + note + "</span>";
				break;
			case OVERRIDE_APPROVED:
				icon = RESOURCES.requestSaved();
				status = MESSAGES.reqStatusApproved();
				iconText += (iconText == null ? "" : iconText + "\n") + MESSAGES.creditStatusApproved();
				break;
			case SAVED:
				icon = null;
				status = "";
				break;
			}
			if (iDetails.getStudentRequest().hasRequestorNote()) {
				note = (note == null ? "" : note + "<br>") + iDetails.getStudentRequest().getRequestorNote().replace("\n", "<br>");
				noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + MESSAGES.requestNote(iDetails.getStudentRequest().getRequestorNote());
				iconText = (iconText == null ? "" : iconText + "\n") + iDetails.getStudentRequest().getRequestorNote();
				hasWarn = true;
			}
			if (iDetails.getStudentRequest().hasCreditNote()) {
				note = (note == null ? "" : note + "<br>") + iDetails.getStudentRequest().getCreditNote().replace("\n", "<br>");
				noteTitle = (noteTitle == null ? "" : noteTitle + "\n") + MESSAGES.overrideNote(iDetails.getStudentRequest().getCreditNote());
				iconText = (iconText == null ? "" : iconText + "\n") + iDetails.getStudentRequest().getCreditNote();
				hasWarn = true;
			}
			float[] range = iDetails.getStudentRequest().getCreditRange();
			WebTable.Cell credit = new WebTable.Cell(range != null ? range[0] < range[1] ? df.format(range[0]) + " - " + df.format(range[1]) : df.format(range[0]) : "");
			credit.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
			WebTable.Row row = new WebTable.Row(
					new WebTable.Cell(MESSAGES.rowRequestedCredit(), 2, null),
					new WebTable.Cell(""),
					credit,
					new WebTable.Cell(""),
					new WebTable.NoteCell(note, noteTitle),
					(icon == null ? new WebTable.Cell(status) : new WebTable.IconCell(icon, iconText, status)),
					new WebTable.Cell(""),
					new WebTable.Cell("")
					);
			for (WebTable.Cell cell: row.getCells()) cell.setStyleName("top-border-solid");
			row.getCell(0).setStyleName("top-border-solid text-bold");
			rows.add(row);
		}

		WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
		int idx = 0;
		for (WebTable.Row row: rows) rowArray[idx++] = row;
		
		iRequests.setData(rowArray);
		iRequests.setColumnVisible(4, hasPref);
		iRequests.setColumnVisible(5, hasWarn);
		iRequests.setColumnVisible(7, hasCrit);
		iRequests.setColumnVisible(8, hasWait);
		
		getRowFormatter().setVisible(iStudentRequestHeaderLine, iDetails != null && iDetails.hasStudentRequest());
		getRowFormatter().setVisible(iStudentRequestHeaderLine + 1, iDetails != null && iDetails.hasStudentRequest());
	}
	
	public boolean isSendEmailConformation() {
		return iEmailConfirmationHeader.isVisible() && iEmailConfirmationHeader.getValue();
	}
}
