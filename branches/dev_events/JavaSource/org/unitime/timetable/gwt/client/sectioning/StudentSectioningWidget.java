/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Iterator;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.TimeGrid.Meeting;
import org.unitime.timetable.gwt.client.widgets.ImageLink;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTabPanel;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class StudentSectioningWidget extends Composite implements HasResizeHandlers {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionProvider iSessionSelector;
	private UserAuthenticationProvider iUserAuthentication;
	
	private VerticalPanel iPanel;
	private HorizontalPanel iFooter;
	private Button iRequests, iReset, iSchedule, iEnroll, iPrint, iExport, iSave;
	private HTML iErrorMessage;
	private UniTimeTabPanel iAssignmentPanel;
	private FocusPanel iAssignmentPanelWithFocus;
	private ImageLink iCalendar;
	
	private CourseRequestsTable iCourseRequests;
	private WebTable iAssignments;
	private TimeGrid iAssignmentGrid;
	private SuggestionsBox iSuggestionsBox;
	private CheckBox iShowUnassignments;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iLastResult;
	private ClassAssignmentInterface iLastAssignment, iSavedAssignment = null;
	private ArrayList<HistoryItem> iHistory = new ArrayList<HistoryItem>();
	private int iAssignmentTab = 0;
	private boolean iInRestore = false;
	private boolean iTrackHistory = true;
	private boolean iOnline;

	public StudentSectioningWidget(boolean online, AcademicSessionProvider sessionSelector, UserAuthenticationProvider userAuthentication, StudentSectioningPage.Mode mode, boolean history) {
		iOnline = online;
		iSessionSelector = sessionSelector;
		iUserAuthentication = userAuthentication;
		iTrackHistory = history;
		
		iPanel = new VerticalPanel();
		
		iCourseRequests = new CourseRequestsTable(iSessionSelector, iOnline);
		
		iPanel.add(iCourseRequests);
		
		iFooter = new HorizontalPanel();
		iFooter.setStyleName("unitime-MainTableBottomHeader");
		iFooter.setWidth("100%");
		
		HorizontalPanel leftFooterPanel = new HorizontalPanel();
		iRequests = new Button(MESSAGES.buttonRequests());
		// iRequests.setWidth("75");
		iRequests.setAccessKey('r');
		iRequests.setVisible(false);
		leftFooterPanel.add(iRequests);

		iReset = new Button(MESSAGES.buttonReset());
		// iReset.setWidth("95");
		iReset.setVisible(false);
		iReset.getElement().getStyle().setMarginLeft(4, Unit.PX);
		leftFooterPanel.add(iReset);
		iFooter.add(leftFooterPanel);

		iErrorMessage = new HTML();
		iErrorMessage.setWidth("100%");
		iErrorMessage.setStyleName("unitime-ErrorMessage");
		iFooter.add(iErrorMessage);
		
		HorizontalPanel rightFooterPanel = new HorizontalPanel();
		iFooter.add(rightFooterPanel);
		iFooter.setCellHorizontalAlignment(rightFooterPanel, HasHorizontalAlignment.ALIGN_RIGHT);


		iSchedule = new Button(MESSAGES.buttonSchedule());
		// iSchedule.setWidth("75");
		iSchedule.setAccessKey('s');
		if (mode.isSectioning())
			rightFooterPanel.add(iSchedule);
		iSchedule.setVisible(mode.isSectioning());
		
		iSave = new Button(MESSAGES.buttonSave());
		// iSave.setWidth("75");
		iSave.setAccessKey('s');
		if (!mode.isSectioning())
			rightFooterPanel.add(iSave);
		iSave.setVisible(!mode.isSectioning());

		iEnroll = new Button(MESSAGES.buttonEnroll());
		// iEnroll.setWidth("75");
		iEnroll.setAccessKey('e');
		iEnroll.setVisible(false);
		rightFooterPanel.add(iEnroll);


		iPrint = new Button(MESSAGES.buttonPrint());
		// iPrint.setWidth("75");
		iPrint.setAccessKey('p');
		iPrint.setVisible(false);
		iPrint.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iPrint);

		iExport = new Button(MESSAGES.buttonExport());
		// iExport.setWidth("75");
		iExport.setAccessKey('x');
		iExport.setVisible(false);
		iExport.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iExport);

		iPanel.add(iFooter);
		
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
		iCalendar = new ImageLink();
		iCalendar.setImage(new Image(RESOURCES.calendar()));
		iCalendar.setTarget(null);
		iCalendar.setTitle(MESSAGES.exportICalendar());

		iAssignments = new WebTable();
		iAssignments.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colLock(), 1, "15"),
				new WebTable.Cell(MESSAGES.colSubject(), 1, "40"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "40"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "30"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "50"),
				new WebTable.Cell(MESSAGES.colLimit(), 1, "30"),
				new WebTable.Cell(MESSAGES.colDays(), 1, "30"),
				new WebTable.Cell(MESSAGES.colStart(), 1, "40"),
				new WebTable.Cell(MESSAGES.colEnd(), 1, "40"),
				new WebTable.Cell(MESSAGES.colDate(), 1, "50"),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "80"),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "80"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "70"),
				new WebTable.Cell(MESSAGES.colNote(), 1, "50"),
				new WebTable.WidgetCell(iCalendar, MESSAGES.colSaved(), 1, "10"),
				new WebTable.Cell(MESSAGES.colHighDemand(), 1, "10")
			));
		iAssignments.setWidth("100%");
		
		VerticalPanel vp = new VerticalPanel();
		vp.add(iAssignments);

		iShowUnassignments = new CheckBox(MESSAGES.showUnassignments());
		iShowUnassignments.getElement().getStyle().setMarginTop(2, Unit.PX);
		vp.add(iShowUnassignments);
		vp.setCellHorizontalAlignment(iShowUnassignments, HasHorizontalAlignment.ALIGN_RIGHT);
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
				iErrorMessage.setHTML("");
				LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
				iSectioningService.section(iOnline, iCourseRequests.getRequest(), null, new AsyncCallback<ClassAssignmentInterface>() {
					public void onFailure(Throwable caught) {
						iErrorMessage.setHTML(caught.getMessage());
						iErrorMessage.setVisible(true);
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
				iErrorMessage.setHTML("");
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
							if (iOnline) {
								iSectioningService.saveRequest(iCourseRequests.getRequest(), new AsyncCallback<Boolean>() {
									public void onSuccess(Boolean result) {
										if (result) {
											iErrorMessage.setHTML("<font color='blue'>" + MESSAGES.saveRequestsOK() + "</font>");
											iErrorMessage.setVisible(true);
										}
									}
									public void onFailure(Throwable caught) {
										iErrorMessage.setHTML(MESSAGES.saveRequestsFail(caught.getMessage()));
										iErrorMessage.setVisible(true);
									}
								});
							}
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsScheduling());
							iSectioningService.section(iOnline, iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
								public void onFailure(Throwable caught) {
									iErrorMessage.setHTML(caught.getMessage());
									iErrorMessage.setVisible(true);
									LoadingWidget.getInstance().hide();
									updateHistory();
								}
								public void onSuccess(ClassAssignmentInterface result) {
									fillIn(result);
									addHistory();
								}
							});								
						} else {
							iErrorMessage.setHTML(MESSAGES.validationFailed());
							iErrorMessage.setVisible(true);
							LoadingWidget.getInstance().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						iErrorMessage.setHTML(MESSAGES.validationFailed());
						iErrorMessage.setVisible(true);
						LoadingWidget.getInstance().hide();
						updateHistory();
					}
				});
			}
		});
		
		iAssignmentPanelWithFocus.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
					do {
						iAssignments.setSelectedRow(iAssignments.getSelectedRow()+1);
					} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
				}
				if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
					do {
						iAssignments.setSelectedRow(iAssignments.getSelectedRow()==0?iAssignments.getRowsCount()-1:iAssignments.getSelectedRow()-1);
					} while (iAssignments.getRows()[iAssignments.getSelectedRow()] != null && !iAssignments.getRows()[iAssignments.getSelectedRow()].isSelectable());
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
				LoadingWidget.getInstance().show("Enrolling...");
				iSectioningService.enroll(iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
					public void onSuccess(ClassAssignmentInterface result) {
						LoadingWidget.getInstance().hide();
						iSavedAssignment = result;
						fillIn(result);
						iErrorMessage.setHTML("<font color='blue'>" + MESSAGES.enrollOK() + "</font>");
						iErrorMessage.setVisible(true);
						updateHistory();
					}
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(MESSAGES.enrollFailed(caught.getMessage()));
						iErrorMessage.setHTML(MESSAGES.enrollFailed(caught.getMessage()));
						iErrorMessage.setVisible(true);
						updateHistory();
					}
				});
			}
		});
		
		iPrint.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				boolean allSaved = true;
				for (ClassAssignmentInterface.ClassAssignment clazz: iLastResult) {
					if (clazz != null && !clazz.isFreeTime() && clazz.isAssigned() && !clazz.isSaved()) allSaved = false;
				}
				Widget w = iAssignments.getPrintWidget(0, 5, 14, 15);
				w.setWidth("100%");
				ToolBox.print((allSaved ? MESSAGES.studentSchedule() : MESSAGES.studentScheduleNotEnrolled()),
						(CONSTANTS.printReportShowUserName() ? iUserAuthentication.getUser() : ""),
						iSessionSelector.getAcademicSessionName(),
						iAssignmentGrid.getPrintWidget(),
						w,
						iErrorMessage);
			}
		});
		
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
					} else {
						iCourseRequests.clear();
						if (!iSchedule.isVisible()) prev();
					}
				}
			});
		
			addHistory();
		}
		
		iSessionSelector.addAcademicSessionChangeHandler(new AcademicSessionSelector.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				addHistory();
			}
		});
		
		iSave.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCourseRequests.changeTip();
				iErrorMessage.setHTML("");
				iCourseRequests.validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
							LoadingWidget.getInstance().show(MESSAGES.courseRequestsSaving());
							iSectioningService.saveRequest(iCourseRequests.getRequest(), new AsyncCallback<Boolean>() {
								public void onSuccess(Boolean result) {
									if (result) {
										iErrorMessage.setHTML("<font color='blue'>" + MESSAGES.saveRequestsOK() + "</font>");
										iErrorMessage.setVisible(true);
									}
									LoadingWidget.getInstance().hide();
								}
								public void onFailure(Throwable caught) {
									iErrorMessage.setHTML(MESSAGES.saveRequestsFail(caught.getMessage()));
									iErrorMessage.setVisible(true);
									LoadingWidget.getInstance().hide();
								}
							});
						} else {
							iErrorMessage.setHTML(MESSAGES.validationFailed());
							iErrorMessage.setVisible(true);
							LoadingWidget.getInstance().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						iErrorMessage.setHTML(MESSAGES.validationFailed());
						iErrorMessage.setVisible(true);
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
			iSuggestionsBox = new SuggestionsBox(iOnline);

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
					fillIn(result);
					addHistory();
				}
			});
		}		
		iAssignments.setSelectedRow(rowIndex);
		iErrorMessage.setVisible(false);
		iSuggestionsBox.open(iCourseRequests.getRequest(), iLastResult, rowIndex);
	}
	
	private void fillIn(ClassAssignmentInterface result) {
		iLastResult.clear();
		iLastAssignment = result;
		String calendarUrl = GWT.getHostPageBaseURL() + "calendar?sid=" + iSessionSelector.getAcademicSessionId() + "&cid=";
		String ftParam = "&ft=";
		if (!result.getCourseAssignments().isEmpty()) {
			ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
			iAssignmentGrid.clear();
			for (ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
				if (course.isAssigned()) {
					boolean firstClazz = true;
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						if (clazz.getClassId() != null)
							calendarUrl += clazz.getCourseId() + "-" + clazz.getClassId() + ",";
						else if (clazz.isFreeTime())
							ftParam += clazz.getDaysString(CONSTANTS.shortDays()) + "-" + clazz.getStart() + "-" + clazz.getLength() + ",";
						String style = "unitime-ClassRow" + (firstClazz && !rows.isEmpty() ? "First": "");
						final WebTable.Row row = new WebTable.Row(
								new WebTable.CheckboxCell(clazz.isPinned()),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject() : ""),
								new WebTable.Cell(firstClazz ? course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr() : ""),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getLimitString()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
								new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
								new WebTable.Cell(clazz.getParentSection(), true),
								new WebTable.NoteCell(clazz.getNote()),
								(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), MESSAGES.saved(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()), null) : 
								 clazz.isFreeTime() || !result.isCanEnroll() ? new WebTable.Cell("") : new WebTable.IconCell(RESOURCES.assignment(), MESSAGES.assignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()), null)),
								(course.isLocked() ? new WebTable.IconCell(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()), null) : clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
						final ArrayList<TimeGrid.Meeting> meetings = (clazz.isFreeTime() ? null : iAssignmentGrid.addClass(clazz, rows.size()));
						// row.setId(course.isFreeTime() ? "Free " + clazz.getDaysString() + " " +clazz.getStartString() + " - " + clazz.getEndString() : course.getCourseId() + ":" + clazz.getClassId());
						final int index = rows.size();
						((CheckBox)row.getCell(0).getWidget()).addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								Boolean checked = Boolean.valueOf(row.getCell(0).getValue());
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
					String style = "unitime-ClassRowRed" + (!rows.isEmpty() ? "First": "");
					WebTable.Row row = null;
					String unassignedMessage = MESSAGES.courseNotAssigned();
					if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
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
					for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
						row = new WebTable.Row(
								new WebTable.Cell(null),
								new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
								new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getLimitString()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								new WebTable.Cell(unassignedMessage, 4, null),
								new WebTable.Cell(clazz.getNote(), true),
								(course.isLocked() ? new WebTable.IconCell(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()), null) : new WebTable.Cell("")));
						row.setId(course.isFreeTime() ? CONSTANTS.freePrefix() + clazz.getDaysString(CONSTANTS.shortDays()) + " " +clazz.getStartString(CONSTANTS.useAmPm()) + " - " + clazz.getEndString(CONSTANTS.useAmPm()) : course.getCourseId() + ":" + clazz.getClassId());
						iLastResult.add(clazz);
						break;
					}
					if (row == null) {
						row = new WebTable.Row(
								new WebTable.Cell(null),
								new WebTable.Cell(course.getSubject()),
								new WebTable.Cell(course.getCourseNbr()),
								new WebTable.Cell(unassignedMessage, 12, null),
								(course.isLocked() ? new WebTable.IconCell(RESOURCES.courseLocked(), MESSAGES.courseLocked(course.getSubject() + " " + course.getCourseNbr()), null) : new WebTable.Cell("")));
						row.setId(course.getCourseId().toString());
						iLastResult.add(course.addClassAssignment());
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					row.getCell(row.getNrCells() - 1).setStyleName("unitime-ClassRowProblem" + (!rows.isEmpty() ? "First": ""));
					rows.add(row);
				}
				if (iSavedAssignment != null && !course.isFreeTime() && iShowUnassignments.getValue()) {
					for (ClassAssignmentInterface.CourseAssignment saved: iSavedAssignment.getCourseAssignments()) {
						if (!saved.isAssigned() || saved.isFreeTime() || !course.getCourseId().equals(saved.getCourseId())) continue;
						classes: for (ClassAssignmentInterface.ClassAssignment clazz: saved.getClassAssignments()) {
							for (ClassAssignmentInterface.ClassAssignment x: course.getClassAssignments())
								if (clazz.getClassId().equals(x.getClassId())) continue classes;
							String style = "unitime-ClassRowUnused";
							WebTable.Row row = new WebTable.Row(
									new WebTable.Cell(null),
									new WebTable.Cell(""),
									new WebTable.Cell(""),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
									new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
									new WebTable.Cell(clazz.getParentSection(), true),
									new WebTable.Cell(clazz.getNote(), true),
									(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()), null) : new WebTable.Cell("")),
									(clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
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
						String style = "unitime-ClassRowUnused" + (firstClazz && !rows.isEmpty() ? "First": "");
						WebTable.Row row = new WebTable.Row(
								new WebTable.Cell(null),
								new WebTable.Cell(firstClazz ? course.getSubject() : ""),
								new WebTable.Cell(firstClazz ? course.getCourseNbr() : ""),
								new WebTable.Cell(clazz.getSubpart()),
								new WebTable.Cell(clazz.getSection()),
								new WebTable.Cell(clazz.getLimitString()),
								new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
								new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
								new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
								new WebTable.Cell(clazz.getParentSection(), true),
								new WebTable.Cell(clazz.getNote(), true),
								(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.unassignment(), MESSAGES.unassignment(course.getSubject() + " " + course.getCourseNbr() + " " + clazz.getSubpart() + " " + clazz.getSection()), null) : new WebTable.Cell("")),
								(clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
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
			iRequests.setVisible(true);
			iReset.setVisible(true);
			iEnroll.setVisible(result.isCanEnroll());
			iPrint.setVisible(true);
			iExport.setVisible(true);
			iSchedule.setVisible(false);
			iAssignmentGrid.scrollDown();
			Scheduler.get().scheduleDeferred(new ScheduledCommand() {
				@Override
				public void execute() {
					iAssignmentPanelWithFocus.setFocus(true);
				}
			});
			if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
			calendarUrl += ftParam;
			if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
			iAssignmentGrid.setCalendarUrl(calendarUrl);
			iCalendar.setUrl(calendarUrl);
			ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
		} else {
			iErrorMessage.setHTML(MESSAGES.noSchedule());
			if (LoadingWidget.getInstance().isShowing())
				LoadingWidget.getInstance().hide();
			UniTimeNotifications.error(MESSAGES.noSchedule());
		}
	}
	
	public void prev() {
		iPanel.remove(iAssignmentPanelWithFocus);
		iPanel.insert(iCourseRequests, 0);
		iRequests.setVisible(false);
		iReset.setVisible(false);
		iEnroll.setVisible(false);
		iPrint.setVisible(false);
		iExport.setVisible(false);
		iSchedule.setVisible(true);
		iErrorMessage.setVisible(false);
		ResizeEvent.fire(this, getOffsetWidth(), getOffsetHeight());
	}
	
	public void clear() {
		if (iShowUnassignments != null)
			iShowUnassignments.setVisible(false);
		iSavedAssignment = null;
		iCourseRequests.clear();
		iLastResult.clear();
		if (iRequests.isVisible()) {
			prev();
		}
	}
	
	public void lastRequest(Long sessionId) {
		LoadingWidget.getInstance().show(MESSAGES.courseRequestsLoading());
		iSectioningService.lastRequest(iOnline, sessionId, new AsyncCallback<CourseRequestInterface>() {
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
					iSectioningService.lastResult(iOnline, request.getAcademicSessionId(), new AsyncCallback<ClassAssignmentInterface>() {
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
						}
						public void onSuccess(final ClassAssignmentInterface saved) {
							iSavedAssignment = saved;
							iShowUnassignments.setVisible(true);
							if (request.isSaved()) {
								fillIn(saved);
								addHistory();
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
													addHistory();
												}
											});
										} else {
											LoadingWidget.getInstance().hide();
										}
									}
								});
							}
						}
					});
				} else {
					LoadingWidget.getInstance().hide();
				}
			}
		});
	}
	
	public void showSuggestionsAsync(final int rowIndex) {
		if (rowIndex < 0) return;
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openSuggestionsBox(rowIndex);
			}
			public void onFailure(Throwable reason) {
				Label error = new Label(MESSAGES.failedToLoadTheApp(reason.getMessage()));
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("loading").setVisible(false);
				RootPanel.get("body").add(error);
			}
		});
	}
	
	public class HistoryItem {
		private CourseRequestInterface iRequest;
		private ClassAssignmentInterface iAssignment;
		private boolean iFirstPage;
		private Long iSessionId;
		private String iUser;
		private String iError = null;
		private int iTab = 0;
		
		private HistoryItem() {
			iRequest = iCourseRequests.getRequest();
			iAssignment = iLastAssignment;
			iFirstPage = iSchedule.isVisible();
			iSessionId = iSessionSelector.getAcademicSessionId();
			iUser = iUserAuthentication.getUser();
			if (iErrorMessage.isVisible()) iError = iErrorMessage.getHTML();
			iTab = iAssignmentTab;
		}
		
		public void restore() {
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
									if (iError != null) {
										iErrorMessage.setHTML(iError);
										iErrorMessage.setVisible(true);
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
}
