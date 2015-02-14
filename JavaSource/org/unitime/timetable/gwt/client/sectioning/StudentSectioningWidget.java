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
import java.util.Iterator;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.aria.AriaStatus;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.TimeGrid.Meeting;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.GwtAriaMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck.EligibilityFlag;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface;
import org.unitime.timetable.gwt.shared.UserAuthenticationProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class StudentSectioningWidget extends Composite implements HasResizeHandlers {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	public static final GwtAriaMessages ARIA = GWT.create(GwtAriaMessages.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionProvider iSessionSelector;
	private UserAuthenticationProvider iUserAuthentication;
	
	private VerticalPanel iPanel;
	private HorizontalPanel iFooter;
	private AriaButton iRequests, iReset, iSchedule, iEnroll, iPrint, iExport = null, iSave, iStartOver;
	private UniTimeTabPanel iAssignmentPanel;
	private FocusPanel iAssignmentPanelWithFocus;
	private ImageLink iCalendar = null;
	
	private CourseRequestsTable iCourseRequests;
	private WebTable iAssignments;
	private TimeGrid iAssignmentGrid;
	private SuggestionsBox iSuggestionsBox;
	private CheckBox iShowUnassignments;
	private Label iTotalCredit;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iLastResult;
	private ClassAssignmentInterface iLastAssignment, iSavedAssignment = null;
	private ArrayList<HistoryItem> iHistory = new ArrayList<HistoryItem>();
	private int iAssignmentTab = 0;
	private boolean iInRestore = false;
	private boolean iTrackHistory = true;
	private boolean iOnline;
	private StudentSectioningPage.Mode iMode = null;
	private OnlineSectioningInterface.EligibilityCheck iEligibilityCheck = null;
	private PinDialog iPinDialog = null;
	private boolean iScheduleChanged = false;
	private HTML iMessage = null;

	public StudentSectioningWidget(boolean online, AcademicSessionProvider sessionSelector, UserAuthenticationProvider userAuthentication, StudentSectioningPage.Mode mode, boolean history) {
		iMode = mode;
		iOnline = online;
		iSessionSelector = sessionSelector;
		iUserAuthentication = userAuthentication;
		iTrackHistory = history;
		
		iPanel = new VerticalPanel();
		
		iCourseRequests = new CourseRequestsTable(iSessionSelector, iOnline);
		iCourseRequests.addValueChangeHandler(new ValueChangeHandler<CourseRequestInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<CourseRequestInterface> event) {
				if (iLastAssignment == null || !iLastAssignment.isCanEnroll() || iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL))
					return;
				if (!iScheduleChanged || MESSAGES.warnScheduleChanged().equals(getMessage())) {
					courses: for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
						if (!course.isAssigned() || course.isFreeTime()) continue;
						for (CourseRequestInterface.Request r: event.getValue().getCourses()) {
							if (r.hasRequestedCourse()  && course.getCourseName().equalsIgnoreCase(r.getRequestedCourse())) continue courses;
							if (r.hasFirstAlternative()  && course.getCourseName().equalsIgnoreCase(r.getFirstAlternative())) continue courses;
							if (r.hasSecondAlternative()  && course.getCourseName().equalsIgnoreCase(r.getSecondAlternative())) continue courses;
						}
						for (CourseRequestInterface.Request r: event.getValue().getAlternatives()) {
							if (r.hasRequestedCourse()  && course.getCourseName().equalsIgnoreCase(r.getRequestedCourse())) continue courses;
							if (r.hasFirstAlternative()  && course.getCourseName().equalsIgnoreCase(r.getFirstAlternative())) continue courses;
							if (r.hasSecondAlternative()  && course.getCourseName().equalsIgnoreCase(r.getSecondAlternative())) continue courses;
						}
						iScheduleChanged = true;
						setWarning(MESSAGES.warnScheduleChanged(), false);
						iEnroll.addStyleName("unitime-EnrollButton");
						return;
					}
					iScheduleChanged = false;
					clearMessage();
				}
			}
		});
		
		iPanel.add(iCourseRequests);
		
		iFooter = new HorizontalPanel();
		iFooter.setStyleName("unitime-MainTableBottomHeader");
		iFooter.setWidth("100%");
		
		HorizontalPanel leftFooterPanel = new HorizontalPanel();
		iRequests = new AriaButton(MESSAGES.buttonRequests());
		iRequests.setTitle(MESSAGES.hintRequests());
		iRequests.setVisible(false);
		iRequests.setEnabled(false);
		leftFooterPanel.add(iRequests);

		iReset = new AriaButton(MESSAGES.buttonReset());
		iReset.setTitle(MESSAGES.hintReset());
		iReset.setVisible(false);
		iReset.setEnabled(false);
		iReset.getElement().getStyle().setMarginLeft(4, Unit.PX);
		iReset.getElement().getStyle().setMarginRight(4, Unit.PX);
		leftFooterPanel.add(iReset);
		iFooter.add(leftFooterPanel);

		HorizontalPanel rightFooterPanel = new HorizontalPanel();
		iFooter.add(rightFooterPanel);
		iFooter.setCellHorizontalAlignment(rightFooterPanel, HasHorizontalAlignment.ALIGN_RIGHT);

		iStartOver = new AriaButton(MESSAGES.buttonStartOver());
		iStartOver.setTitle(MESSAGES.hintStartOver());
		if (mode.isSectioning())
			leftFooterPanel.add(iStartOver);
		iStartOver.setVisible(false);
		iStartOver.setEnabled(false);

		iSchedule = new AriaButton(MESSAGES.buttonSchedule());
		iSchedule.setTitle(MESSAGES.hintSchedule());
		if (mode.isSectioning())
			rightFooterPanel.add(iSchedule);
		iSchedule.setVisible(mode.isSectioning());
		iSchedule.setEnabled(mode.isSectioning());
		
		iSave = new AriaButton(MESSAGES.buttonSave());
		iSave.setTitle(MESSAGES.hintSave());
		if (!mode.isSectioning())
			rightFooterPanel.add(iSave);
		iSave.setVisible(!mode.isSectioning());
		iSave.setEnabled(!mode.isSectioning());

		iEnroll = new AriaButton(MESSAGES.buttonEnroll());
		iEnroll.setTitle(MESSAGES.hintEnroll());
		iEnroll.setVisible(false);
		iEnroll.setEnabled(false);
		rightFooterPanel.add(iEnroll);

		iPrint = new AriaButton(MESSAGES.buttonPrint());
		iPrint.setTitle(MESSAGES.hintPrint());
		iPrint.setVisible(false);
		iPrint.setEnabled(false);
		iPrint.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iPrint);

		if (CONSTANTS.allowCalendarExport()) {
			iExport = new AriaButton(MESSAGES.buttonExport());
			iExport.setTitle(MESSAGES.hintExport());
			iExport.setVisible(false);
			iExport.setEnabled(false);
			iExport.getElement().getStyle().setMarginLeft(4, Unit.PX);
			rightFooterPanel.add(iExport);
		}
		
		iPanel.add(iFooter);
		
		iMessage = new HTML();
		iMessage.setStyleName("unitime-ScheduleBlankMessage");
		iPanel.add(iMessage);
		
		iLastResult = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		
		initWidget(iPanel);
		
		init();
	}
	
	/*
	private void initAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				init();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});
	}
	*/
	
	private void addHistory() {
		if (iInRestore || !iTrackHistory) return;
		iHistory.add(new HistoryItem());
		History.newItem(String.valueOf(iHistory.size() - 1), false);
	}
	
	private void updateHistory() {
		if (iInRestore || !iTrackHistory) return;
		if (!iHistory.isEmpty())
			iHistory.remove(iHistory.size() - 1);
		addHistory();
	}
	
	private void init() {
		if (CONSTANTS.allowCalendarExport()) {
			iCalendar = new ImageLink();
			iCalendar.setImage(new Image(RESOURCES.calendar()));
			iCalendar.setTarget(null);
			iCalendar.setTitle(MESSAGES.exportICalendar());
			iCalendar.setAriaLabel(MESSAGES.exportICalendar());
		}

		iAssignments = new WebTable();
		iAssignments.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colLock(), 1, "15px"),
				new WebTable.Cell(MESSAGES.colSubject(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "30px"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colLimit(), 1, "30px").aria(ARIA.colLimit()),
				new WebTable.Cell(MESSAGES.colDays(), 1, "30px"),
				new WebTable.Cell(MESSAGES.colStart(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colEnd(), 1, "40px"),
				new WebTable.Cell(MESSAGES.colDate(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "80px"),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "80px"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "80px"),
				new WebTable.Cell(MESSAGES.colNote(), 1, "50px"),
				new WebTable.Cell(MESSAGES.colCredit(), 1, "30px"),
				(iCalendar != null ? new WebTable.WidgetCell(iCalendar, MESSAGES.colIcons(), 1, "1px") : new WebTable.Cell(MESSAGES.colIcons(), 1, "1px"))
			));
		iAssignments.setWidth("100%");
		iAssignments.setEmptyMessage(MESSAGES.emptySchedule());
		
		FlexTable vp = new FlexTable();
		vp.setCellPadding(0); vp.setCellSpacing(0);
		vp.setWidget(0, 0, iAssignments);
		vp.getFlexCellFormatter().setColSpan(0, 0, 2);
		
		iTotalCredit = new Label();
		iTotalCredit.getElement().getStyle().setMarginTop(2, Unit.PX);
		vp.setWidget(1, 0, iTotalCredit);

		iShowUnassignments = new CheckBox(MESSAGES.showUnassignments());
		iShowUnassignments.getElement().getStyle().setMarginTop(2, Unit.PX);
		vp.setWidget(1, 1, iShowUnassignments);
		vp.getFlexCellFormatter().setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
		iShowUnassignments.setVisible(false);		
		String showUnassignments = Cookies.getCookie("UniTime:Unassignments");
		iShowUnassignments.setValue(showUnassignments == null || "1".equals(showUnassignments));
				
		
		iAssignmentPanel = new UniTimeTabPanel();
		iAssignmentPanel.add(vp, MESSAGES.tabClasses(), true);
		iAssignmentPanel.selectTab(0);
		
		iAssignmentGrid = new TimeGrid();
		iAssignmentPanel.add(iAssignmentGrid, MESSAGES.tabTimetable(), true);
		iAssignmentPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				iAssignmentTab = event.getSelectedItem();
				if (event.getSelectedItem() == 1)
					iAssignmentGrid.scrollDown();
				addHistory();
				if (iAssignmentTab == 0) {
					AriaStatus.getInstance().setHTML(ARIA.listOfClasses());
				} else {
					AriaStatus.getInstance().setHTML(ARIA.timetable());
				}
				ResizeEvent.fire(StudentSectioningWidget.this, StudentSectioningWidget.this.getOffsetWidth(), StudentSectioningWidget.this.getOffsetHeight());
			}
		});

		iAssignmentPanelWithFocus = new FocusPanel(iAssignmentPanel);
		iAssignmentPanelWithFocus.setStyleName("unitime-FocusPanel");
		
		iRequests.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				prev();
				addHistory();
			}
		});
		
		iReset.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearMessage();
				LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
				iSectioningService.section(iOnline, iCourseRequests.getRequest(), null, new AsyncCallback<ClassAssignmentInterface>() {
					public void onFailure(Throwable caught) {
						setError(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
					public void onSuccess(ClassAssignmentInterface result) {
						fillIn(result);
						addHistory();
					}
				});
			}
		});
		
		iSchedule.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCourseRequests.changeTip();
				clearMessage();
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
							if (iOnline) {
								iSectioningService.saveRequest(iCourseRequests.getRequest(), new AsyncCallback<Boolean>() {
									public void onSuccess(Boolean result) {
										if (result) {
											setMessage(MESSAGES.saveRequestsOK());
										}
									}
									public void onFailure(Throwable caught) {
										setError(MESSAGES.saveRequestsFail(caught.getMessage()), caught);
									}
								});
							}
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
							iSectioningService.section(iOnline, iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
								public void onFailure(Throwable caught) {
									setError(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
								public void onSuccess(ClassAssignmentInterface result) {
									fillIn(result);
									addHistory();
								}
							});								
						} else {
							String error = iCourseRequests.getFirstError();
							setError(error == null ? MESSAGES.validationFailed() : MESSAGES.validationFailedWithMessage(error));
							LoadingWidget.getInstance().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						setError(MESSAGES.validationFailedWithMessage(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
				});
			}
		});
		
		iStartOver.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearMessage();
				clear();
				addHistory();
				lastRequest(iSessionSelector.getAcademicSessionId(), null, true);
			}
		});
		
		iAssignmentPanelWithFocus.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
					do {
						iAssignments.setSelectedRow(iAssignments.getSelectedRow()+1);
					} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
					
					if (iAssignments.getSelectedRow() >= 0 && iAssignments.getSelectedRow() < iAssignments.getRows().length && iAssignments.getRows()[iAssignments.getSelectedRow()] != null)
						AriaStatus.getInstance().setHTML(ARIA.classSelected(1 + iAssignments.getSelectedRow(), iAssignments.getRowsCount(), iAssignments.getRows()[iAssignments.getSelectedRow()].getAriaLabel()));
				}
				if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
					do {
						iAssignments.setSelectedRow(iAssignments.getSelectedRow()==0?iAssignments.getRowsCount()-1:iAssignments.getSelectedRow()-1);
					} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
					
					if (iAssignments.getSelectedRow() >= 0 && iAssignments.getSelectedRow() < iAssignments.getRows().length && iAssignments.getRows()[iAssignments.getSelectedRow()] != null)
						AriaStatus.getInstance().setHTML(ARIA.classSelected(1 + iAssignments.getSelectedRow(), iAssignments.getRowsCount(), iAssignments.getRows()[iAssignments.getSelectedRow()].getAriaLabel()));
				}
				if (event.getNativeKeyCode()==KeyCodes.KEY_ENTER) {
					updateHistory();
					showSuggestionsAsync(iAssignments.getSelectedRow());
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='l' || event.getNativeKeyCode()=='L')) {
					iAssignmentPanel.selectTab(0);
					event.preventDefault();
				}
				if (event.getNativeEvent().getCtrlKey() && (event.getNativeKeyCode()=='t' || event.getNativeKeyCode()=='T')) {
					iAssignmentPanel.selectTab(1);
					event.preventDefault();
				}
			}
		});
		
		iAssignments.addRowClickHandler(new WebTable.RowClickHandler() {
			public void onRowClick(WebTable.RowClickEvent event) {
				if (iLastResult.get(event.getRowIdx()) == null) return;
				updateHistory();
				showSuggestionsAsync(event.getRowIdx());
			}
		});
		
		iAssignmentGrid.addMeetingClickHandler(new TimeGrid.MeetingClickHandler() {
			public void onMeetingClick(TimeGrid.MeetingClickEvent event) {
				updateHistory();
				showSuggestionsAsync(event.getRowIndex());
			}
		});
		
		iAssignmentGrid.addPinClickHandler(new TimeGrid.PinClickHandler() {
			public void onPinClick(TimeGrid.PinClickEvent event) {
				((CheckBox)iAssignments.getRows()[event.getRowIndex()].getCell(0).getWidget()).setValue(event.isPinChecked());
				iLastResult.get(event.getRowIndex()).setPinned(event.isPinChecked());
				updateHistory();
			}
		});
		
		iEnroll.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				clearMessage();
				LoadingWidget.getInstance().show(MESSAGES.waitEnroll());
				iSectioningService.enroll(iOnline, iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
					public void onSuccess(ClassAssignmentInterface result) {
						LoadingWidget.getInstance().hide();
						iSavedAssignment = result;
						fillIn(result);
						if (!result.hasMessages())
							setMessage(MESSAGES.enrollOK());
						updateHistory();
						if (iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.RECHECK_AFTER_ENROLLMENT)) {
							iSectioningService.checkEligibility(iOnline, iSessionSelector.getAcademicSessionId(),
									iEligibilityCheck.getStudentId(), (String)null,
									new AsyncCallback<OnlineSectioningInterface.EligibilityCheck>() {
										@Override
										public void onFailure(Throwable caught) {
										}

										@Override
										public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
											if (!result.hasFlag(EligibilityFlag.CAN_ENROLL)) {
												iEligibilityCheck.setFlag(EligibilityFlag.CAN_ENROLL, false);
												iEnroll.setEnabled(false);
												iEnroll.setVisible(false);
											}
										}
									});
						}
					}
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						setError(MESSAGES.enrollFailed(caught.getMessage()), caught);
						updateHistory();
					}
				});
			}
		});
		
		iPrint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean allSaved = true;
				for (ClassAssignmentInterface.ClassAssignment clazz: iLastResult) {
					if (clazz != null && !clazz.isFreeTime() && !clazz.isSaved()) allSaved = false;
				}
				Widget w = iAssignments.getPrintWidget(0, 5, 15);
				w.setWidth("100%");
				ToolBox.print((allSaved && !isChanged() ? MESSAGES.studentSchedule() : MESSAGES.studentScheduleNotEnrolled()),
						(CONSTANTS.printReportShowUserName() ? iUserAuthentication.getUser() : ""),
						iSessionSelector.getAcademicSessionName(),
						iAssignmentGrid.getPrintWidget(),
						w,
						iMessage);
			}
		});
		
		if (iExport != null)
			iExport.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					ToolBox.open(iCalendar.getUrl());
				}
			});
		
		if (iTrackHistory) {
			History.addValueChangeHandler(new ValueChangeHandler<String>() {
				public void onValueChange(ValueChangeEvent<String> event) {
					if (!event.getValue().isEmpty()) {
						int item = iHistory.size() - 1;
						try {
							item = Integer.parseInt(event.getValue());
						} catch (NumberFormatException e) {}
						if (item < 0) item = 0;
						if (item >= iHistory.size()) item = iHistory.size() - 1;
						if (item >= 0) iHistory.get(item).restore();
					} else if (isChanged() && Window.confirm(MESSAGES.queryLeaveChanges())) {
						iCourseRequests.clear();
						if (!iSchedule.isVisible()) prev();
					}
				}
			});
		}
		
		iSessionSelector.addAcademicSessionChangeHandler(new AcademicSessionSelector.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				addHistory();
			}
		});
		
		iSave.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCourseRequests.changeTip();
				clearMessage();
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsSaving());
							iSectioningService.saveRequest(iCourseRequests.getRequest(), new AsyncCallback<Boolean>() {
								public void onSuccess(Boolean result) {
									if (result) {
										setMessage(MESSAGES.saveRequestsOK());
									}
									LoadingWidget.getInstance().hide();
								}
								public void onFailure(Throwable caught) {
									setError(MESSAGES.saveRequestsFail(caught.getMessage()), caught);
									LoadingWidget.getInstance().hide();
								}
							});
						} else {
							String error = iCourseRequests.getFirstError();
							setError(error == null ? MESSAGES.validationFailed() : MESSAGES.validationFailedWithMessage(error));
							LoadingWidget.getInstance().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						setError(MESSAGES.validationFailedWithMessage(caught.getMessage()), caught);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
				});
			}
		});
		
		iShowUnassignments.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				Cookies.setCookie("UniTime:Unassignments", "1");
				fillIn(iLastAssignment);
			}
		});
	}
	
	public void openSuggestionsBox(int rowIndex) {
		if (iSuggestionsBox == null) {
			iSuggestionsBox = new SuggestionsBox(iAssignmentGrid.getColorProvider(), iOnline);

			iSuggestionsBox.addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							iAssignmentPanelWithFocus.setFocus(true);
						}
					});
				}
			});
			
			iSuggestionsBox.addSuggestionSelectedHandler(new SuggestionsBox.SuggestionSelectedHandler() {
				public void onSuggestionSelected(SuggestionsBox.SuggestionSelectedEvent event) {
					ClassAssignmentInterface result = event.getSuggestion();
					clearMessage();
					fillIn(result);
					addHistory();
				}
			});
		}		
		iAssignments.setSelectedRow(rowIndex);
		clearMessage();
		iSuggestionsBox.open(iCourseRequests.getRequest(), iLastResult, rowIndex);
	}
	
	private void fillIn(ClassAssignmentInterface result) {
		iLastResult.clear();
		iLastAssignment = result;
		String calendarUrl = GWT.getHostPageBaseURL() + "calendar?sid=" + iSessionSelector.getAcademicSessionId() + "&cid=";
		String ftParam = "&ft=";
		boolean hasError = false;
		float totalCredit = 0f;
		if (!result.getCourseAssignments().isEmpty() || CONSTANTS.allowEmptySchedule()) {
			ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
			iAssignmentGrid.clear(true);
			for (final ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				if (course.isAssigned()) {
					boolean firstClazz = true;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.getClassId() != null)
							calendarUrl += clazz.getCourseId() + "-" + clazz.getClassId() + ",";
						else if (clazz.isFreeTime())
							ftParam += clazz.getDaysString(CONSTANTS.shortDays()) + "-" + clazz.getStart() + "-" + clazz.getLength() + ",";
						String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
						WebTable.Row row = null;

						WebTable.IconsCell icons = new WebTable.IconsCell();
						if (clazz.isSaved())
							icons.add(RESOURCES.saved(), MESSAGES.saved(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						else if (!clazz.isFreeTime() && result.isCanEnroll())
							icons.add(RESOURCES.assignment(), MESSAGES.assignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						if (clazz.hasError()) {
							icons.add(RESOURCES.error(), clazz.getError());
							style += " text-red";
							hasError = true;
						}
						if (course.isLocked())
							icons.add(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
						if (clazz.isOfHighDemand())
							icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
						if (clazz != null && clazz.hasOverlapNote())
							icons.add(RESOURCES.overlap(), clazz.getOverlapNote());

						totalCredit += clazz.guessCreditCount();
						if (clazz.isAssigned()) {
							row = new WebTable.Row(
								new WebTable.CheckboxCell(clazz.isPinned(), course.isFreeTime() ? ARIA.freeTimePin(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())) : ARIA.classPin(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection())), MESSAGES.hintLocked(), MESSAGES.hintUnlocked()),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : "").aria(firstClazz ? "" : course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr() : "").aria(firstClazz ? "" : course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getLimitString()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
								new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
								new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
								new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
								new WebTable.AbbvTextCell(clazz.getCredit()),
								icons);
						} else {
							row = new WebTable.Row(
									new WebTable.CheckboxCell(clazz.isPinned() , course.isFreeTime() ? ARIA.freeTimePin(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())) : ARIA.classPin(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection())), MESSAGES.hintLocked(), MESSAGES.hintUnlocked()),
									new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : ""),
									new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr() : ""),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 4, null),
									(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						}
						if (course.isFreeTime())
							row.setAriaLabel(ARIA.freeTimeAssignment(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours())));
						else
							row.setAriaLabel(ARIA.classAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
								clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
						final WebTable.Row finalRow = row;
						final ArrayList<TimeGrid.Meeting> meetings = (clazz.isFreeTime() ? null : iAssignmentGrid.addClass(clazz, rows.size()));
						// row.setId(course.isFreeTime() ? "Free " + clazz.getDaysString() + " " +clazz.getStartString() + " - " + clazz.getEndString() : course.getCourseId() + ":" + clazz.getClassId());
						final int index = rows.size();
						((CheckBox)row.getCell(0).getWidget()).addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								Boolean checked = Boolean.valueOf(finalRow.getCell(0).getValue());
								if (meetings == null) {
									iLastResult.get(index).setPinned(checked);
								} else {
									for (Meeting m: meetings) {
										m.setPinned(checked);
										iLastResult.get(m.getIndex()).setPinned(checked);
									}
								}
							}
						});
						rows.add(row);
						iLastResult.add(clazz);
						for (WebTable.Cell cell: row.getCells())
							cell.setStyleName(style);
						firstClazz = false;
					}
				} else {
					String style = "text-red" + (!rows.isEmpty() ? " top-border-dashed": "");
					WebTable.Row row = null;
					String unassignedMessage = MESSAGES.courseNotAssigned();
					if (course.hasEnrollmentMessage())
						unassignedMessage = course.getEnrollmentMessage();
					else if (course.getNote() != null)
						unassignedMessage = course.getNote();
					else if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
						unassignedMessage = "";
						for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
							String x = i.next();
							if (unassignedMessage.isEmpty())
								unassignedMessage += MESSAGES.conflictWithFirst(x);
							else if (!i.hasNext())
								unassignedMessage += MESSAGES.conflictWithLast(x);
							else
								unassignedMessage += MESSAGES.conflictWithMiddle(x);
						}
						if (course.getInstead() != null)
							unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
						unassignedMessage += ".";
					} else if (course.isNotAvailable()) {
						unassignedMessage = MESSAGES.classNotAvailable();
					} else if (course.isLocked()) {
						unassignedMessage = MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr());
					}
					
					WebTable.IconsCell icons = new WebTable.IconsCell();
					if (course.isLocked())
						icons.add(RESOURCES.courseLocked(), course.getNote() != null ? course.getNote() : MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()));
					
					WebTable.CheckboxCell waitList = null;
					Boolean w = iCourseRequests.getWaitList(course.getCourseName());
					if (w != null) {
						waitList = new WebTable.CheckboxCell(w, MESSAGES.toggleWaitList(), ARIA.titleRequestedWaitListForCourse(MESSAGES.course(course.getSubject(), course.getCourseNbr())));
						waitList.getWidget().setStyleName("toggle");
						((CheckBox)waitList.getWidget()).addValueChangeHandler(new ValueChangeHandler<Boolean>() {
							@Override
							public void onValueChange(ValueChangeEvent<Boolean> event) {
								clearMessage();
								iCourseRequests.setWaitList(course.getCourseName(), event.getValue());
								LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
								CourseRequestInterface r = iCourseRequests.getRequest(); r.setNoChange(true);
								iSectioningService.section(iOnline, r, iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
									public void onFailure(Throwable caught) {
										setError(MESSAGES.exceptionSectioningFailed(caught.getMessage()), caught);
										LoadingWidget.getInstance().hide();
										updateHistory();
									}
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										addHistory();
									}
								});
							}
						});
					}

					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.isAssigned()) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									new WebTable.Cell(unassignedMessage, 3, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment(clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()), unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						} else if (clazz.getClassId() != null) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 4, null),
									new WebTable.Cell(unassignedMessage, 3, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment("", unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(unassignedMessage, 7, null),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									(waitList != null ? waitList : new WebTable.AbbvTextCell(clazz.getCredit())),
									icons);
							if (course.isFreeTime())
								row.setAriaLabel(ARIA.freeTimeUnassignment("", unassignedMessage));
							else
								row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						}
						row.setId(course.isFreeTime() ? CONSTANTS.freePrefix() + clazz.getDaysString(CONSTANTS.shortDays()) + " " +clazz.getStartString(CONSTANTS.useAmPm()) + " - " + clazz.getEndString(CONSTANTS.useAmPm()) : course.getCourseId() + ":" + clazz.getClassId());
						iLastResult.add(clazz);
						break;
					}
					if (row == null) {
						if (waitList != null) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr()),
									new WebTable.Cell(unassignedMessage, 11, null),
									waitList,
									icons);
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr()),
									new WebTable.Cell(unassignedMessage, 12, null),
									icons);
						}
						row.setId(course.getCourseId().toString());
						row.setAriaLabel(ARIA.courseUnassginment(MESSAGES.course(course.getSubject(), course.getCourseNbr()), unassignedMessage));
						iLastResult.add(course.addClassAssignment());
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					row.getCell(row.getNrCells() - 1).setStyleName("text-red-centered" + (!rows.isEmpty() ? " top-border-dashed": ""));
					rows.add(row);
				}
				if (iSavedAssignment != null && !course.isFreeTime() && iShowUnassignments.getValue()) {
					for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || !course.getCourseId().equals(saved.getCourseId())) continue;
						classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							String style = "text-gray";
							WebTable.Row row = null;
							
							WebTable.IconsCell icons = new WebTable.IconsCell();
							if (clazz.hasError())
								icons.add(RESOURCES.error(), clazz.getError());
							if (clazz.isSaved())
								icons.add(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
							if (clazz.isOfHighDemand())
								icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));
							
							if (clazz.isAssigned()) {
								row = new WebTable.Row(
										new WebTable.Cell(null),
										new WebTable.Cell("").aria(course.getSubject()),
										new WebTable.Cell("").aria(course.getCourseNbr()),
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
										new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
										new WebTable.Cell(clazz.getDatePattern()),
										(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
										new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
										new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										new WebTable.AbbvTextCell(clazz.getCredit()),
										icons);								
							} else {
								row = new WebTable.Row(
										new WebTable.Cell(null),
										new WebTable.Cell("").aria(course.getSubject()),
										new WebTable.Cell("").aria(course.getCourseNbr()),
										new WebTable.Cell(clazz.getSubpart()),
										new WebTable.Cell(clazz.getSection()),
										new WebTable.Cell(clazz.getLimitString()),
										new WebTable.Cell(MESSAGES.arrangeHours(), 4, null),
										(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
										new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
										new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
										new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
										new WebTable.AbbvTextCell(clazz.getCredit()),
										icons);
							}
							row.setAriaLabel(ARIA.previousAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
									clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
							rows.add(row);
							row.setSelectable(false);
							iLastResult.add(null);
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(style);
						}
					}
				}
			}
			if (iSavedAssignment != null && iShowUnassignments.getValue()) {
				courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
					if (!course.isAssigned() || course.isFreeTime()) continue;
					for (ClassAssignmentInterface.CourseAssignment x: result.getCourseAssignments())
						if (course.getCourseId().equals(x.getCourseId())) continue courses;
					boolean firstClazz = true;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						String style = "text-gray" + (firstClazz && !rows.isEmpty() ? " top-border-dashed": "");
						WebTable.Row row = null;
						
						WebTable.IconsCell icons = new WebTable.IconsCell();
						if (clazz.hasError())
							icons.add(RESOURCES.error(), clazz.getError());
						if (clazz.isSaved())
							icons.add(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()));
						if (clazz.isOfHighDemand())
							icons.add(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()));

						if (clazz.isAssigned()) {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(firstClazz ? "" : course.getSubject()),
									new WebTable.Cell(firstClazz ? course.getCourseNbr() : "").aria(firstClazz ? "" : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())).aria(clazz.getDaysString(CONSTANTS.longDays(), " ")),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())).aria(clazz.getStartStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())).aria(clazz.getEndStringAria(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						} else {
							row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(firstClazz ? course.getSubject() : "").aria(firstClazz ? "" : course.getSubject()),
									new WebTable.Cell(firstClazz ? course.getCourseNbr() : "").aria(firstClazz ? "" : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(MESSAGES.arrangeHours(), 4, null),
									(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), clazz.getParentSection() == null || clazz.getParentSection().length() > 10),
									new WebTable.NoteCell(clazz.getOverlapAndNote("text-red"), clazz.getOverlapAndNote(null)),
									new WebTable.AbbvTextCell(clazz.getCredit()),
									icons);
						}
						row.setAriaLabel(ARIA.previousAssignment(MESSAGES.clazz(course.getSubject(), course.getCourseNbr(), clazz.getSubpart(), clazz.getSection()),
								clazz.isAssigned() ? clazz.getTimeStringAria(CONSTANTS.longDays(), CONSTANTS.useAmPm(), ARIA.arrangeHours()) + " " + clazz.getRooms(",") : ARIA.arrangeHours()));
						rows.add(row);
						row.setSelectable(false);
						iLastResult.add(null);
						for (WebTable.Cell cell: row.getCells())
							cell.setStyleName(style);
						firstClazz = false;
					}
				}
			}
			for (ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isFreeTime()) {
						CourseRequestInterface.FreeTime ft = new CourseRequestInterface.FreeTime();
						ft.setLength(clazz.getLength());
						ft.setStart(clazz.getStart());
						for (int d: clazz.getDays()) ft.addDay(d);
						iAssignmentGrid.addFreeTime(ft);
					}
				}
			}

			WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
			int idx = 0;
			for (WebTable.Row row: rows) rowArray[idx++] = row;
			iAssignmentGrid.shrink();
			iAssignmentPanel.setWidth(iAssignmentGrid.getWidth() + "px");
			iAssignments.setData(rowArray);
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
			iPanel.remove(iCourseRequests);
			iPanel.insert(iAssignmentPanelWithFocus, 0);
			iRequests.setVisible(true); iRequests.setEnabled(true);
			iReset.setVisible(true); iReset.setEnabled(true);
			iEnroll.setVisible(result.isCanEnroll() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL));
			iEnroll.setEnabled(result.isCanEnroll() && iEligibilityCheck != null && iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL));
			iPrint.setVisible(true); iPrint.setEnabled(true);
			iStartOver.setVisible(iSavedAssignment != null); iStartOver.setEnabled(iSavedAssignment != null);
			if (iExport != null) {
				iExport.setVisible(true); iExport.setEnabled(true);
			}
			iSchedule.setVisible(false); iSchedule.setEnabled(false);
			iAssignmentGrid.scrollDown();
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					iAssignmentPanelWithFocus.setFocus(true);
				}
			});
			if (iCalendar != null) {
				if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
				calendarUrl += ftParam;
				if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
				iAssignmentGrid.setCalendarUrl(calendarUrl);
				iCalendar.setUrl(calendarUrl);
			}
			ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
			if (iAssignmentTab == 0) {
				AriaStatus.getInstance().setHTML(ARIA.listOfClasses());
			} else {
				AriaStatus.getInstance().setHTML(ARIA.timetable());
			}
			if (result.hasMessages()) {
				if (hasError) {
					setError(result.getMessages("<br>"));
				} else { 
					setWarning(result.getMessages("<br>"));
				}
			} else {
				updateScheduleChangedNoteIfNeeded();
			}
			iTotalCredit.setVisible(totalCredit > 0f);
			iTotalCredit.setText(MESSAGES.totalCredit(totalCredit));
		} else {
			iTotalCredit.setVisible(false);
			setError(MESSAGES.noSchedule());
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
		}
	}
	
	public void prev() {
		iPanel.remove(iAssignmentPanelWithFocus);
		iPanel.insert(iCourseRequests, 0);
		iRequests.setVisible(false); iRequests.setEnabled(false);
		iReset.setVisible(false); iReset.setEnabled(false);
		iEnroll.setVisible(false); iEnroll.setEnabled(false);
		iPrint.setVisible(false); iPrint.setEnabled(false);
		iStartOver.setVisible(false); iStartOver.setVisible(false);
		if (iExport != null) {
			iExport.setVisible(false); iExport.setEnabled(false);
		}
		iSchedule.setVisible(true); iSchedule.setEnabled(true);
		clearMessage();
		ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
		AriaStatus.getInstance().setHTML(ARIA.courseRequests());
		updateScheduleChangedNoteIfNeeded();
	}
	
	public void clear() {
		if (iShowUnassignments != null)
			iShowUnassignments.setVisible(false);
		iSavedAssignment = null; iLastAssignment = null;
		iCourseRequests.clear();
		iLastResult.clear();
		if (iRequests.isVisible()) {
			prev();
		}
	}
	
	public void checkEligibility(final Long sessionId, final Long studentId, final boolean saved, final AsyncCallback<OnlineSectioningInterface.EligibilityCheck> ret) {
		if (!iMode.isSectioning()) {
			lastRequest(sessionId, studentId, saved);
			if (ret != null) ret.onSuccess(null);
			return;
		}
		LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		iStartOver.setVisible(false); iStartOver.setEnabled(false);
		iSectioningService.checkEligibility(iOnline, sessionId, studentId, null, new AsyncCallback<OnlineSectioningInterface.EligibilityCheck>() {
			@Override
			public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
				clearMessage();
				iEligibilityCheck = result;
				iCourseRequests.setCanWaitList(result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
				if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_USE_ASSISTANT)) {
					if (result.hasMessage()) {
						setWarning(result.getMessage());
					} else {
						clearMessage();
					}
					if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
						if (iPinDialog == null) iPinDialog = new PinDialog();
						LoadingWidget.getInstance().hide();
						PinDialog.PinCallback callback = new PinDialog.PinCallback() {
							@Override
							public void onFailure(Throwable caught) {
								setError(MESSAGES.exceptionFailedEligibilityCheck(caught.getMessage()), caught);
								iSchedule.setVisible(true); iSchedule.setEnabled(true);
								lastRequest(sessionId, studentId, saved);
								if (ret != null) ret.onSuccess(iEligibilityCheck);
							}
							@Override
							public void onSuccess(OnlineSectioningInterface.EligibilityCheck result) {
								iCourseRequests.setCanWaitList(result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.CAN_WAITLIST));
								iEligibilityCheck = result;
								iSchedule.setVisible(true); iSchedule.setEnabled(true);
								lastRequest(sessionId, studentId, saved);
								if (ret != null) ret.onSuccess(iEligibilityCheck);
							}
							@Override
							public void onMessage(OnlineSectioningInterface.EligibilityCheck result) {
								if (result.hasMessage()) {
									setError(result.getMessage());
								} else if (result.hasFlag(OnlineSectioningInterface.EligibilityCheck.EligibilityFlag.PIN_REQUIRED)) {
									setWarning(MESSAGES.exceptionAuthenticationPinRequired());
								} else {
									clearMessage();
								}
							}
						};
						iPinDialog.checkEligibility(iOnline, sessionId, null, callback);
					} else {
						iSchedule.setVisible(true); iSchedule.setEnabled(true);
						lastRequest(sessionId, studentId, saved);
						if (ret != null) ret.onSuccess(iEligibilityCheck);
					}
				} else {
					iCourseRequests.setCanWaitList(false);
					LoadingWidget.getInstance().hide();
					if (result.hasMessage()) {
						setError(result.getMessage());
					}
					iSchedule.setVisible(false);  iSchedule.setEnabled(false);
					if (ret != null) ret.onFailure(new SectioningException(result.getMessage()));
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iEligibilityCheck = null;
				if (ret != null) ret.onFailure(caught);
			}
		});
	}
	
	private void lastResult(final CourseRequestInterface request, boolean saved) {
		AsyncCallback<ClassAssignmentInterface> callback = new AsyncCallback<ClassAssignmentInterface>() {
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
			}
			public void onSuccess(final ClassAssignmentInterface saved) {
				iSavedAssignment = saved;
				iShowUnassignments.setVisible(true);
				if (request.isSaved()) {
					fillIn(saved);
					updateHistory();
				} else {
					iCourseRequests.validate(new AsyncCallback<Boolean>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
						}
						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								ArrayList<ClassAssignmentInterface.ClassAssignment> classes = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
								for (ClassAssignmentInterface.CourseAssignment course: saved.getCourseAssignments())
									classes.addAll(course.getClassAssignments());
								iSectioningService.section(iOnline, request, classes, new AsyncCallback<ClassAssignmentInterface>() {
									public void onFailure(Throwable caught) {
										LoadingWidget.getInstance().hide();
									}
									public void onSuccess(ClassAssignmentInterface result) {
										fillIn(result);
										updateHistory();
									}
								});
							} else {
								LoadingWidget.getInstance().hide();
							}
						}
					});
				}
			}
		};
		if (saved)
			iSectioningService.savedResult(iOnline, request.getAcademicSessionId(), request.getStudentId(), callback);
		else
			iSectioningService.lastResult(iOnline, request.getAcademicSessionId(), callback);
	}
	
	public void lastRequest(Long sessionId, Long studentId, final boolean saved) {
		if (!LoadingWidget.getInstance().isShowing())
			LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		
		AsyncCallback<CourseRequestInterface> callback =  new AsyncCallback<CourseRequestInterface>() {
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
			}
			public void onSuccess(final CourseRequestInterface request) {
				if (request.isSaved() && request.getCourses().isEmpty()) {
					LoadingWidget.getInstance().hide();
					return;
				}
				clear();
				iCourseRequests.setRequest(request);
				if (iSchedule.isVisible()) {
					lastResult(request, saved);
				} else {
					LoadingWidget.getInstance().hide();
				}
			}
		};
		
		if (saved)
			iSectioningService.savedRequest(iOnline, sessionId, studentId, callback);
		else
			iSectioningService.lastRequest(iOnline, sessionId, callback);
	}

	public void showSuggestionsAsync(final int rowIndex) {
		if (rowIndex < 0) return;
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openSuggestionsBox(rowIndex);
			}
			public void onFailure(Throwable caught) {
				setError(MESSAGES.exceptionSuggestionsFailed(caught.getMessage()), caught);
			}
		});
	}
	
	public class HistoryItem {
		private CourseRequestInterface iRequest;
		private ClassAssignmentInterface iAssignment;
		private boolean iFirstPage;
		private Long iSessionId;
		private String iUser;
		private String iError = null, iErrorStyle = null;;
		private int iTab = 0;
		
		private HistoryItem() {
			iRequest = iCourseRequests.getRequest();
			iAssignment = iLastAssignment;
			iFirstPage = iSchedule.isVisible();
			iSessionId = iSessionSelector.getAcademicSessionId();
			iUser = iUserAuthentication.getUser();
			iError = getMessage(); iErrorStyle = iMessage.getStyleName();
			iTab = iAssignmentTab;
		}
		
		public void restore() {
			if (isChanged() && ((iUser != null && !iUser.equals(iUserAuthentication.getUser())) || (iSessionId != null && !iSessionId.equals(iSessionSelector.getAcademicSessionId()))) && !Window.confirm(MESSAGES.queryLeaveChanges()))
				return;
			iInRestore = true;
			iUserAuthentication.setUser(iUser, new AsyncCallback<Boolean>() {
				public void onSuccess(Boolean result) {
					if (result) {
						iSessionSelector.selectSession(iSessionId, new AsyncCallback<Boolean>() {
							public void onSuccess(Boolean result) {
								if (result) {
									iCourseRequests.setRequest(iRequest);
									if (iTab != iAssignmentTab)
										iAssignmentPanel.selectTab(iTab);
									if (iFirstPage) {
										if (!iSchedule.isVisible()) prev();
										iCourseRequests.changeTip();
									} else {
										if (iAssignment != null) fillIn(iAssignment);
									}
									if (iError != null && !iError.isEmpty()) {
										if ("unitime-ScheduleErrorMessage".equals(iErrorStyle))
											setError(iError);
										else if ("unitime-ScheduleWarningMessage".equals(iErrorStyle))
											setWarning(iError);
										else
											setMessage(iError);
									} else {
										clearMessage();
									}
								}
								iInRestore = false;
								ResizeEvent.fire(StudentSectioningWidget.this, getOffsetWidth(), getOffsetHeight());
							}
							public void onFailure(Throwable reason) {
								iInRestore = false;
							}
						});
					} else {
						iInRestore = false;
					}
				}
				public void onFailure(Throwable reason) {
					iInRestore = false;
				}
			});
		}
	}
	
	public void setData(CourseRequestInterface request, ClassAssignmentInterface response) {
		clear();
		iCourseRequests.setRequest(request);
		if (response != null) {
			if (request.isSaved()) {
				iSavedAssignment = response;
				iShowUnassignments.setVisible(true);
			}
			fillIn(response);
		}
	}

	@Override
	public HandlerRegistration addResizeHandler(ResizeHandler handler) {
		return addHandler(handler, ResizeEvent.getType());
	}
	
	public void updateScheduleChangedNoteIfNeeded() {
		if (iScheduleChanged) {
			iScheduleChanged = false;
			clearMessage();
		}
		if (iLastAssignment == null || !iLastAssignment.isCanEnroll() || iEligibilityCheck == null || !iEligibilityCheck.hasFlag(EligibilityFlag.CAN_ENROLL))
			return;
		boolean empty = true;
		if (iSavedAssignment != null)
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime()) continue;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isSaved()) { empty = false; break courses; }
				}
			}
		for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
			if (!course.isAssigned() || course.isFreeTime()) continue;
			for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
				if (!clazz.isSaved() && !clazz.hasError()) {
					iScheduleChanged = true;
					iEnroll.addStyleName("unitime-EnrollButton");
					setWarning(empty ? MESSAGES.warnScheduleEmpty() : MESSAGES.warnScheduleChanged(), false);
					return;
				}
			}
			if (iSavedAssignment != null)
				for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
					if (!saved.isAssigned() || saved.isFreeTime() || !course.getCourseId().equals(saved.getCourseId())) continue;
					classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
						for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments()) {
							if (clazz.getClassId().equals(x.getClassId())) continue classes;
						}
						if (clazz.isSaved() && !clazz.hasError()) {
							iScheduleChanged = true;
							iEnroll.addStyleName("unitime-EnrollButton");
							setWarning(MESSAGES.warnScheduleChanged(), false);
						}
					}
				}
		}
		if (iSavedAssignment != null)
			courses: for (ClassAssignmentInterface.CourseAssignment course: iSavedAssignment.getCourseAssignments()) {
				if (!course.isAssigned() || course.isFreeTime()) continue;
				for (ClassAssignmentInterface.CourseAssignment x: iLastAssignment.getCourseAssignments())
					if (course.getCourseId().equals(x.getCourseId())) continue courses;
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
					if (clazz.isSaved() && !clazz.hasError()) {
						iScheduleChanged = true;
						iEnroll.addStyleName("unitime-EnrollButton");
						setWarning(MESSAGES.warnScheduleChanged(), false);
					}
				}
			}
		CourseRequestInterface request = iCourseRequests.getRequest();
		courses: for (ClassAssignmentInterface.CourseAssignment course: iLastAssignment.getCourseAssignments()) {
			if (!course.isAssigned() || course.isFreeTime()) continue;
			for (CourseRequestInterface.Request r: request.getCourses()) {
				if (r.hasRequestedCourse()  && course.getCourseName().equalsIgnoreCase(r.getRequestedCourse())) continue courses;
				if (r.hasFirstAlternative()  && course.getCourseName().equalsIgnoreCase(r.getFirstAlternative())) continue courses;
				if (r.hasSecondAlternative()  && course.getCourseName().equalsIgnoreCase(r.getSecondAlternative())) continue courses;
			}
			for (CourseRequestInterface.Request r: request.getAlternatives()) {
				if (r.hasRequestedCourse()  && course.getCourseName().equalsIgnoreCase(r.getRequestedCourse())) continue courses;
				if (r.hasFirstAlternative()  && course.getCourseName().equalsIgnoreCase(r.getFirstAlternative())) continue courses;
				if (r.hasSecondAlternative()  && course.getCourseName().equalsIgnoreCase(r.getSecondAlternative())) continue courses;
			}
			iScheduleChanged = true;
			iEnroll.addStyleName("unitime-EnrollButton");
			setWarning(MESSAGES.warnScheduleChanged(), false);
		}
	}
	
	public boolean isChanged() {
		return iScheduleChanged;
	}
	
	public void clearMessage() {
		iMessage.setHTML("");
		iMessage.setStyleName("unitime-ScheduleBlankMessage");
		iEnroll.removeStyleName("unitime-EnrollButton");
		if (isChanged())
			updateScheduleChangedNoteIfNeeded();
	}
	
	public void setError(String message) {
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-ScheduleErrorMessage");
		iMessage.setVisible(true);
		UniTimeNotifications.error(message);
	}
	
	public void setError(String message, Throwable t) {
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-ScheduleErrorMessage");
		iMessage.setVisible(true);
		UniTimeNotifications.error(message, t);
	}
	
	public void setError(Throwable t) {
		setError(t.getMessage(), t);
	}
	
	public void setWarning(String message) {
		setWarning(message, true);
	}
	
	public void setWarning(String message, boolean popup) {
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-ScheduleWarningMessage");
		iMessage.setVisible(true);
		if (popup)
			UniTimeNotifications.warn(message);
	}
	
	public void setMessage(String message) {
		iMessage.setHTML(message);
		iMessage.setStyleName("unitime-ScheduleMessage");
		iMessage.setVisible(true);
		UniTimeNotifications.info(message);
	}
	
	public String getMessage() {
		return iMessage.getHTML();
	}
}
