/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.widgets;

import java.util.ArrayList;
import java.util.Iterator;

import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ToolBox;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider.AcademicSessionChangeEvent;
import org.unitime.timetable.gwt.widgets.TimeGrid.Meeting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
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
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StudentSectioningWidget extends Composite {
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private AcademicSessionSelector iSessionSelector;
	private UserAuthentication iUserAuthentication;
	
	private VerticalPanel iPanel;
	private HorizontalPanel iFooter;
	private Button iPrev, iNext, iEnroll, iPrint, iExport;
	private HTML iErrorMessage;
	private TabPanel iAssignmentPanel;
	private FocusPanel iAssignmentPanelWithFocus;
	private ImageLink iCalendar;
	
	private CourseRequestsTable iCourseRequests;
	private WebTable iAssignments;
	private TimeGrid iAssignmentGrid;
	private SuggestionsBox iSuggestionsBox;
	
	private ArrayList<ClassAssignmentInterface.ClassAssignment> iLastResult;
	private ClassAssignmentInterface iLastAssignment;
	private ArrayList<HistoryItem> iHistory = new ArrayList<HistoryItem>();
	private int iAssignmentTab = 0;
	private boolean iInRestore = false;

	public StudentSectioningWidget(AcademicSessionSelector sessionSelector, UserAuthentication userAuthentication) {
		iSessionSelector = sessionSelector;
		iUserAuthentication = userAuthentication;
		
		iPanel = new VerticalPanel();
		
		iCourseRequests = new CourseRequestsTable(iSessionSelector);
		
		iPanel.add(iCourseRequests);
		
		iFooter = new HorizontalPanel();
		iFooter.setStyleName("unitime-MainTableBottomHeader");
		
		iPrev = new Button(MESSAGES.buttonPrev());
		iPrev.setWidth("75");
		iPrev.setAccessKey('p');
		iPrev.setVisible(false);
		iFooter.add(iPrev);

		iErrorMessage = new HTML();
		iErrorMessage.setWidth("100%");
		iErrorMessage.setStyleName("unitime-ErrorMessage");
		iFooter.add(iErrorMessage);
		
		HorizontalPanel rightFooterPanel = new HorizontalPanel();
		iFooter.add(rightFooterPanel);
		iFooter.setCellHorizontalAlignment(rightFooterPanel, HasHorizontalAlignment.ALIGN_RIGHT);


		iNext = new Button(MESSAGES.buttonNext());
		iNext.setWidth("75");
		iNext.setAccessKey('n');
		rightFooterPanel.add(iNext);
		
		iEnroll = new Button(MESSAGES.buttonEnroll());
		iEnroll.setWidth("75");
		iEnroll.setAccessKey('e');
		iEnroll.setVisible(false);
		rightFooterPanel.add(iEnroll);


		iPrint = new Button(MESSAGES.buttonPrint());
		iPrint.setWidth("75");
		iPrint.setAccessKey('r');
		iPrint.setVisible(false);
		iPrint.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iPrint);

		iExport = new Button(MESSAGES.buttonExport());
		iExport.setWidth("75");
		iExport.setAccessKey('x');
		iExport.setVisible(false);
		iExport.getElement().getStyle().setMarginLeft(4, Unit.PX);
		rightFooterPanel.add(iExport);

		iPanel.add(iFooter);
		
		iLastResult = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		
		initWidget(iPanel);
		
		initAsync();
	}
	
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
	
	
	private void addHistory() {
		if (iInRestore) return;
		iHistory.add(new HistoryItem());
		History.newItem(String.valueOf(iHistory.size() - 1), false);
	}
	
	private void updateHistory() {
		if (iInRestore) return;
		if (!iHistory.isEmpty())
			iHistory.remove(iHistory.size() - 1);
		addHistory();
	}

	private void init() {
		iCalendar = new ImageLink();
		iCalendar.setImage(new Image(RESOURCES.calendar()));
		iCalendar.setTarget(null);
		iCalendar.setTitle("Export in iCalendar format.");

		iAssignments = new WebTable();
		iAssignments.setHeader(new WebTable.Row(
				new WebTable.Cell(MESSAGES.colLock(), 1, "15"),
				new WebTable.Cell(MESSAGES.colSubject(), 1, "75"),
				new WebTable.Cell(MESSAGES.colCourse(), 1, "75"),
				new WebTable.Cell(MESSAGES.colSubpart(), 1, "50"),
				new WebTable.Cell(MESSAGES.colClass(), 1, "75"),
				new WebTable.Cell(MESSAGES.colLimit(), 1, "60"),
				new WebTable.Cell(MESSAGES.colDays(), 1, "50"),
				new WebTable.Cell(MESSAGES.colStart(), 1, "75"),
				new WebTable.Cell(MESSAGES.colEnd(), 1, "75"),
				new WebTable.Cell(MESSAGES.colDate(), 1, "75"),
				new WebTable.Cell(MESSAGES.colRoom(), 1, "100"),
				new WebTable.Cell(MESSAGES.colInstructor(), 1, "100"),
				new WebTable.Cell(MESSAGES.colParent(), 1, "75"),
				new WebTable.WidgetCell(iCalendar, MESSAGES.colSaved(), 1, "10"),
				new WebTable.Cell(MESSAGES.colHighDemand(), 1, "10")
			));
		
		iAssignmentPanel = new TabPanel();
		HTML tab0 = new HTML(MESSAGES.tabClasses(), false);
		iAssignmentPanel.add(iAssignments, tab0);
		iAssignmentPanel.selectTab(0);
		
		iAssignmentGrid = new TimeGrid();
		HTML tab1 = new HTML(MESSAGES.tabTimetable(), false);
		iAssignmentPanel.add(iAssignmentGrid, tab1);
		iAssignmentPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			public void onSelection(SelectionEvent<Integer> event) {
				iAssignmentTab = event.getSelectedItem();
				if (event.getSelectedItem() == 1)
					iAssignmentGrid.scrollDown();
				addHistory();
			}
		});

		iAssignmentPanelWithFocus = new FocusPanel(iAssignmentPanel);
		iAssignmentPanelWithFocus.setStyleName("unitime-FocusPanel");
		
		iPrev.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				prev();
				addHistory();
			}
		});
		
		iNext.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				iCourseRequests.changeTip();
				iErrorMessage.setHTML("");
				iCourseRequests.getValidator().validate(new AsyncCallback<Boolean>() {
					public void onSuccess(Boolean result) {
						updateHistory();
						if (result) {
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
							iSectioningService.section(iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ClassAssignmentInterface>() {
								public void onFailure(Throwable caught) {
									iErrorMessage.setHTML(caught.getMessage());
									iErrorMessage.setVisible(true);
									iCourseRequests.getValidator().hide();
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
							iCourseRequests.getValidator().hide();
							updateHistory();
						}
					}
					public void onFailure(Throwable caught) {
						iErrorMessage.setHTML(MESSAGES.validationFailed());
						iErrorMessage.setVisible(true);
						iCourseRequests.getValidator().hide();
						updateHistory();
					}
				});
			}
		});
		
		iAssignmentPanelWithFocus.addKeyUpHandler(new KeyUpHandler() {
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode()==KeyCodes.KEY_DOWN) {
					iAssignments.setSelectedRow(iAssignments.getSelectedRow()+1);
				}
				if (event.getNativeKeyCode()==KeyCodes.KEY_UP) {
					iAssignments.setSelectedRow(iAssignments.getSelectedRow()==0?iAssignments.getRowsCount()-1:iAssignments.getSelectedRow()-1);
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
				iSectioningService.enroll(iCourseRequests.getRequest(), iLastResult, new AsyncCallback<ArrayList<Long>>() {
					public void onSuccess(ArrayList<Long> result) {
						int idx = 0;
						for (ClassAssignmentInterface.ClassAssignment ca: iLastResult) {
							if (ca.getClassId() != null) {
								ca.setSaved(result.contains(ca.getClassId()));
								WebTable.Row row = iAssignments.getRows()[idx];
								WebTable.Cell c = (ca.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell(""));
								c.setStyleName(row.getCell(row.getNrCells() - 1).getStyleName());
								row.setCell(row.getNrCells() - 1, c);
								ArrayList<Meeting> meetings = iAssignmentGrid.getMeetings(idx);
								if (meetings != null)
									for (TimeGrid.Meeting m: meetings) m.setSaved(ca.isSaved());
							}
							idx++;
						}
						iErrorMessage.setHTML("<font color='blue'>" + MESSAGES.enrollOK() + "</font>");
						iErrorMessage.setVisible(true);
						updateHistory();
					}
					public void onFailure(Throwable caught) {
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
					if (!clazz.isFreeTime() && clazz.isAssigned() && !clazz.isSaved()) allSaved = false;
				}
				ToolBox.print((allSaved ? MESSAGES.studentSchedule() : MESSAGES.studentScheduleNotEnrolled()),
						(CONSTANTS.printReportShowUserName() ? iUserAuthentication.getUser() : ""),
						iSessionSelector.getAcademicSessionName(),
						iAssignmentGrid.getPrintWidget(),
						iAssignments.getPrintWidget(0, 5, 13, 14),
						iErrorMessage);
			}
		});
		
		iExport.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				ToolBox.open(iCalendar.getUrl());
			}
		});
		
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
					if (!iNext.isVisible()) prev();
				}
			}
		});
		
		addHistory();
		
		iSessionSelector.addAcademicSessionChangeHandler(new AcademicSessionSelector.AcademicSessionChangeHandler() {
			public void onAcademicSessionChange(AcademicSessionChangeEvent event) {
				addHistory();
			}
		});
	}
	
	public void openSuggestionsBox(int rowIndex) {
		if (iSuggestionsBox == null) {
			iSuggestionsBox = new SuggestionsBox();

			iSuggestionsBox.addCloseHandler(new CloseHandler<PopupPanel>() {
				public void onClose(CloseEvent<PopupPanel> event) {
					DeferredCommand.addCommand(new Command() {
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
		iSuggestionsBox.setRow(iCourseRequests.getRequest(), iLastResult, rowIndex);
		iSuggestionsBox.center();
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
								new WebTable.Cell(clazz.getStartString()),
								new WebTable.Cell(clazz.getEndString()),
								new WebTable.Cell(clazz.getDatePattern()),
								(clazz.hasDistanceConflict() ? new WebTable.IconCell(RESOURCES.distantConflict(), MESSAGES.backToBackDistance(clazz.getBackToBackRooms(), clazz.getBackToBackDistance()), clazz.getRooms(", ")) : new WebTable.Cell(clazz.getRooms(", "))),
								new WebTable.InstructorCell(clazz.getInstructors(), clazz.getInstructorEmails(), ", "),
								new WebTable.Cell(clazz.getParentSection()),
								(clazz.isSaved() ? new WebTable.IconCell(RESOURCES.saved(), null, null) : new WebTable.Cell("")),
								(clazz.isOfHighDemand() ? new WebTable.IconCell(RESOURCES.highDemand(), MESSAGES.highDemand(clazz.getExpected(), clazz.getAvailableLimit()), null) : new WebTable.Cell("")));
						final ArrayList<TimeGrid.Meeting> meetings = iAssignmentGrid.addClass(clazz, rows.size());
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
					String unassignedMessage = "";
					if (course.getOverlaps()!=null && !course.getOverlaps().isEmpty()) {
						unassignedMessage = MESSAGES.conflictWith();
						for (Iterator<String> i = course.getOverlaps().iterator(); i.hasNext();) {
							String x = i.next();
							if (course.getOverlaps().size() > 1 && !i.hasNext()) unassignedMessage += MESSAGES.conflictWithOr();
							unassignedMessage += x;
							if (i.hasNext()) unassignedMessage += ", ";
						}
						if (course.getInstead() != null)
							unassignedMessage += MESSAGES.conflictAssignedAlternative(course.getInstead());
						unassignedMessage += ".";
					} else if (course.isNotAvailable()) {
						unassignedMessage = MESSAGES.classNotAvailable();
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
								new WebTable.Cell(clazz.getStartString()),
								new WebTable.Cell(clazz.getEndString()),
								new WebTable.Cell(clazz.getDatePattern()),
								new WebTable.Cell(unassignedMessage, 4, null));
						row.setId(course.isFreeTime() ? "Free " + clazz.getDaysString(CONSTANTS.shortDays()) + " " +clazz.getStartString() + " - " + clazz.getEndString() : course.getCourseId() + ":" + clazz.getClassId());
						iLastResult.add(clazz);
						break;
					}
					if (row == null) {
						row = new WebTable.Row(
								new WebTable.Cell(null),
								new WebTable.Cell(course.getSubject()),
								new WebTable.Cell(course.getCourseNbr()),
								new WebTable.Cell(unassignedMessage, 11, null));
						row.setId(course.getCourseId().toString());
						iLastResult.add(course.addClassAssignment());
					}
					for (WebTable.Cell cell: row.getCells())
						cell.setStyleName(style);
					row.getCell(row.getNrCells() - 1).setStyleName("unitime-ClassRowProblem" + (!rows.isEmpty() ? "First": ""));
					rows.add(row);
				}
			}
			WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
			int idx = 0;
			for (WebTable.Row row: rows) rowArray[idx++] = row;
			iAssignmentGrid.shrink();
			iAssignmentPanel.setWidth(iAssignmentGrid.getWidth());
			iAssignments.setData(rowArray);
			iCourseRequests.getValidator().hide();
			iPanel.remove(iCourseRequests);
			iPanel.insert(iAssignmentPanelWithFocus, 0);
			iPrev.setVisible(true);
			iEnroll.setVisible(true);
			iPrint.setVisible(true);
			iExport.setVisible(true);
			iNext.setVisible(false);
			iAssignmentGrid.scrollDown();
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					iAssignmentPanelWithFocus.setFocus(true);
				}
			});
			if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
			calendarUrl += ftParam;
			if (calendarUrl.endsWith(",")) calendarUrl = calendarUrl.substring(0, calendarUrl.length() - 1);
			iAssignmentGrid.setCalendarUrl(calendarUrl);
			iCalendar.setUrl(calendarUrl);
		} else {
			iErrorMessage.setHTML(MESSAGES.noSchedule());
			iCourseRequests.getValidator().hide();
		}
	}
	
	public void prev() {
		iPanel.remove(iAssignmentPanelWithFocus);
		iPanel.insert(iCourseRequests, 0);
		iPrev.setVisible(false);
		iEnroll.setVisible(false);
		iPrint.setVisible(false);
		iExport.setVisible(false);
		iNext.setVisible(true);
		iErrorMessage.setVisible(false);
	}
	
	public void clear() {
		iCourseRequests.clear();
		iLastResult.clear();
		if (iPrev.isVisible()) {
			prev();
		}
	}
	
	public void lastRequest(Long sessionId) {
		iSectioningService.lastRequest(sessionId, new AsyncCallback<CourseRequestInterface>() {
			public void onFailure(Throwable caught) { }
			public void onSuccess(CourseRequestInterface result) {
				iCourseRequests.setRequest(result);
				iSectioningService.lastResult(result.getAcademicSessionId(), new AsyncCallback<ArrayList<ClassAssignmentInterface.ClassAssignment>>() {
					public void onFailure(Throwable caught) {}
					public void onSuccess(ArrayList<ClassAssignmentInterface.ClassAssignment> result) {
						if (iNext.isVisible()) {
							iLastResult.clear(); iLastResult.addAll(result);
						}
					}
				});
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
			iFirstPage = iNext.isVisible();
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
										if (!iNext.isVisible()) prev();
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
}
