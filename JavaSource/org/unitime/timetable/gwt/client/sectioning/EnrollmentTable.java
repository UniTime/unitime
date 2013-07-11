/*
 * UniTime 3.3 (University Timetabling Application)
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
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.WebTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasColSpan;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasStyleName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasVerticalCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningResources;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.AcademicSessionProvider;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Conflict;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.UserAuthenticationProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class EnrollmentTable extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private static DateTimeFormat sTSF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private Long iOfferingId = null;

	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private SimpleForm iEnrollmentPanel;
	private UniTimeTable<ClassAssignmentInterface.Enrollment> iEnrollments;
	private UniTimeHeaderPanel iHeader;
	private Operation iApprove, iReject;
	
	private boolean iOnline;
	
	public EnrollmentTable(final boolean showHeader, boolean online) {
		iOnline = online;
		iEnrollmentPanel = new SimpleForm();
		
		iHeader = new UniTimeHeaderPanel(showHeader ? MESSAGES.enrollmentsTable() : "&nbsp;");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				SectioningCookie.getInstance().setEnrollmentCoursesDetails(event.getValue());
				if (iEnrollments.getRowCount() > 2) {
					for (int row = 1; row < iEnrollments.getRowCount() - 1; row++) {
						iEnrollments.getRowFormatter().setVisible(row, event.getValue());
					}
				}
				if (iEnrollments.getRowCount() == 0)
					refresh();
			}
		});
		iHeader.setCollapsible(showHeader ? SectioningCookie.getInstance().getEnrollmentCoursesDetails() : null);
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iEnrollmentPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		if (showHeader) {
			iEnrollmentPanel.addHeaderRow(iHeader);
			iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
		}
		
		iEnrollments = new UniTimeTable<ClassAssignmentInterface.Enrollment>();
		iEnrollmentPanel.addRow(iEnrollments);
		
		if (!showHeader)
			iEnrollmentPanel.addBottomRow(iHeader);
		
		iHeader.addButton("approve", MESSAGES.buttonApproveSelectedEnrollments(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iApprove != null && iApprove.isApplicable())
					iApprove.execute();
			}
		});
		
		iHeader.addButton("reject", MESSAGES.buttonRejectSelectedEnrollments(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iReject != null && iReject.isApplicable())
					iReject.execute();
			}
		});
		
		iHeader.setEnabled("approve", false);
		iHeader.setEnabled("reject", false);
		
		initWidget(iEnrollmentPanel);
		
		iEnrollments.addMouseClickListener(new UniTimeTable.MouseClickListener<ClassAssignmentInterface.Enrollment>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<ClassAssignmentInterface.Enrollment> event) {
				if (event.getData() == null) return;
				LoadingWidget.getInstance().show(MESSAGES.loadingEnrollment(event.getData().getStudent().getName()));
				iHeader.clearMessage();
				showStudentSchedule(event.getData().getStudent(), new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						if (showHeader) {
							UniTimeNotifications.error(MESSAGES.failedToLoadEnrollments(caught.getMessage()), caught);
						} else {
							iHeader.setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
						}
					}

					@Override
					public void onSuccess(Boolean result) {
						LoadingWidget.getInstance().hide();
					}
				});
			}
		});
	}
	
	public UniTimeTable<ClassAssignmentInterface.Enrollment> getTable() { return iEnrollments; }
	
	public UniTimeHeaderPanel getHeader() { return iHeader; }
	
	public void showStudentSchedule(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
		iSectioningService.getEnrollment(iOnline, student.getId(), new AsyncCallback<ClassAssignmentInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(ClassAssignmentInterface result) {
				callback.onSuccess(true);
				WebTable assignments = new WebTable();
				assignments.setHeader(new WebTable.Row(
						new WebTable.Cell(MESSAGES.colSubject(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colCourse(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colSubpart(), 1, "50px"),
						new WebTable.Cell(MESSAGES.colClass(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colLimit(), 1, "60px"),
						new WebTable.Cell(MESSAGES.colDays(), 1, "50px"),
						new WebTable.Cell(MESSAGES.colStart(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colEnd(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colDate(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colRoom(), 1, "100px"),
						new WebTable.Cell(MESSAGES.colInstructor(), 1, "100px"),
						new WebTable.Cell(MESSAGES.colParent(), 1, "75px"),
						new WebTable.Cell(MESSAGES.colNoteIcon(), 1, "10px")
					));
				
				ArrayList<WebTable.Row> rows = new ArrayList<WebTable.Row>();
				for (ClassAssignmentInterface.CourseAssignment course: result.getCourseAssignments()) {
					if (course.isAssigned()) {
						boolean firstClazz = true;
						for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
							String style = (firstClazz && !rows.isEmpty() ? "top-border-dashed": "");
							final WebTable.Row row = new WebTable.Row(
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
									new WebTable.Cell(clazz.getParentSection()),
									clazz.hasNote() ? new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), "") : new WebTable.Cell(""));
							rows.add(row);
							for (WebTable.Cell cell: row.getCells())
								cell.setStyleName(style);
							firstClazz = false;
						}
					} else {
						String style = "text-red" + (!rows.isEmpty() ? " top-border-dashed": "");
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
								if (i.hasNext()) unassignedMessage += ", ";
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
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeSubject() : course.getSubject()),
									new WebTable.Cell(course.isFreeTime() ? MESSAGES.freeTimeCourse() : course.getCourseNbr()),
									new WebTable.Cell(clazz.getSubpart()),
									new WebTable.Cell(clazz.getSection()),
									new WebTable.Cell(clazz.getLimitString()),
									new WebTable.Cell(clazz.getDaysString(CONSTANTS.shortDays())),
									new WebTable.Cell(clazz.getStartString(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getEndString(CONSTANTS.useAmPm())),
									new WebTable.Cell(clazz.getDatePattern()),
									new WebTable.Cell(unassignedMessage, 3, null),
									clazz.getNote() == null ? new WebTable.Cell("") : new WebTable.IconCell(RESOURCES.note(), clazz.getNote(), ""));
							break;
						}
						if (row == null) {
							row = new WebTable.Row(
									new WebTable.Cell(course.getSubject()),
									new WebTable.Cell(course.getCourseNbr()),
									new WebTable.Cell(unassignedMessage, 11, null));
						}
						for (WebTable.Cell cell: row.getCells())
							cell.setStyleName(style);
						row.getCell(row.getNrCells() - 1).setStyleName("text-gray" + (!rows.isEmpty() ? " top-border-dashed": ""));
						rows.add(row);
					}
				}
				WebTable.Row[] rowArray = new WebTable.Row[rows.size()];
				int idx = 0;
				for (WebTable.Row row: rows) rowArray[idx++] = row;
				assignments.setData(rowArray);
				SimpleForm form = new SimpleForm();
				form.addRow(assignments);
				final UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
				form.addBottomRow(buttons);
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
				dialog.setWidget(form);
				dialog.setText(MESSAGES.dialogEnrollments(student.getName()));
				dialog.setEscapeToHide(true);
				buttons.addButton("assistant", MESSAGES.buttonAssistant(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						LoadingWidget.getInstance().show(MESSAGES.loadingAssistant(student.getName()));
						showStudentAssistant(student, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								UniTimeNotifications.error(caught);
							}
							@Override
							public void onSuccess(Boolean result) {
								LoadingWidget.getInstance().hide();
								if (result)
									dialog.hide();
							}
						});
					}
				});
				buttons.setEnabled("assistant", false);
				if (iOnline) {
					buttons.addButton("log", MESSAGES.buttonChangeLog(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							LoadingWidget.getInstance().show(MESSAGES.loadingChangeLog(student.getName()));
							showChangeLog(student, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}
								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									if (result)
										dialog.hide();
								}
							});
						}
					});					
				}
				buttons.addButton("close", MESSAGES.buttonClose(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						dialog.hide();
					}
				});
				dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						iEnrollments.clearHover();
					}
				});
				buttons.showLoading();
				if (iOnline) {
					iSectioningService.canEnroll(iOnline, student.getId(), new AsyncCallback<Long>() {
						@Override
						public void onFailure(Throwable caught) {
							buttons.clearMessage();
						}

						@Override
						public void onSuccess(Long result) {
							buttons.clearMessage();
							buttons.setEnabled("assistant", result != null);
						}
					});					
				} else {
					buttons.setEnabled("assistant", true);
					buttons.clearMessage();
				}
				dialog.center();
			}
		});
	}
	
	public void showStudentAssistant(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
		iSectioningService.canEnroll(iOnline, student.getId(), new AsyncCallback<Long>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(final Long sessionId) {
				if (sessionId == null) {
					callback.onSuccess(false);
					return;
				}

				UserAuthenticationProvider user = new UserAuthenticationProvider() {
					@Override
					public String getUser() {
						return student.getName();
					}
					@Override
					public void setUser(String user, AsyncCallback<Boolean> callback) {
					}
				};
				
				AcademicSessionProvider session = new AcademicSessionProvider() {
					@Override
					public Long getAcademicSessionId() {
						return sessionId;
					}
					@Override
					public String getAcademicSessionName() {
						return "Current Session";
					}
					@Override
					public void addAcademicSessionChangeHandler(AcademicSessionChangeHandler handler) {
					}
					@Override
					public void selectSession(Long sessionId, AsyncCallback<Boolean> callback) {
					}
					@Override
					public AcademicSessionInfo getAcademicSessionInfo() {
						return null;
					}
				};
												
				final StudentSectioningWidget widget = new StudentSectioningWidget(iOnline, session, user, StudentSectioningPage.Mode.SECTIONING, false);
				
				iSectioningService.logIn(iOnline ? "LOOKUP" : "BATCH", iOnline ? student.getExternalId() : String.valueOf(student.getId()), new AsyncCallback<String>() {
					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}
					@Override
					public void onSuccess(String result) {
						iSectioningService.savedRequest(iOnline, student.getId(), new AsyncCallback<CourseRequestInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								callback.onFailure(caught);
							}
							@Override
							public void onSuccess(final CourseRequestInterface request) {
								iSectioningService.savedResult(iOnline, student.getId(), new AsyncCallback<ClassAssignmentInterface>() {
									@Override
									public void onFailure(Throwable caught) {
										callback.onFailure(caught);
									}

									@Override
									public void onSuccess(ClassAssignmentInterface result) {
										widget.setData(request, result);
										final UniTimeDialogBox d = new UniTimeDialogBox(true, false);
										d.setWidget(widget);
										d.setText(MESSAGES.dialogAssistant(student.getName()));
										d.setEscapeToHide(true);
										callback.onSuccess(true);
										d.center();
										widget.addResizeHandler(new ResizeHandler() {
											@Override
											public void onResize(ResizeEvent event) {
												d.center();
											}
										});
									}
								});
							}
						});
					}
				});				
			}
		});
	}
	
	public void showChangeLog(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
		iSectioningService.changeLog("id:" + student.getExternalId(), new AsyncCallback<List<ClassAssignmentInterface.SectioningAction>>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(List<ClassAssignmentInterface.SectioningAction> logs) {
				if (logs == null || logs.isEmpty()) {
					callback.onSuccess(false);
					return;
				}
				
				final UniTimeTable<ClassAssignmentInterface.SectioningAction> table = new UniTimeTable<ClassAssignmentInterface.SectioningAction>();
				
				table.addRow(null,
						new UniTimeTableHeader(MESSAGES.colOperation()),
						new UniTimeTableHeader(MESSAGES.colTimeStamp()),
						new UniTimeTableHeader(MESSAGES.colResult()),
						new UniTimeTableHeader(MESSAGES.colUser()),
						new UniTimeTableHeader(MESSAGES.colMessage()));
				
				for (ClassAssignmentInterface.SectioningAction log: logs) {
					table.addRow(log,
							new TopCell(log.getOperation()),
							new TopCell(sTSF.format(log.getTimeStamp())),
							new TopCell(log.getResult()),
							new TopCell(log.getUser() == null ? "" : log.getUser()),
							new HTML(log.getMessage() == null ? "" : log.getMessage())
					);
				}
				table.addMouseClickListener(new MouseClickListener<ClassAssignmentInterface.SectioningAction>() {
					@Override
					public void onMouseClick(TableEvent<SectioningAction> event) {
						if (event.getData() != null && event.getData().getProto() != null) {
							final HTML widget = new HTML(event.getData().getProto());
							final ScrollPanel scroll = new ScrollPanel(widget);
							scroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
							scroll.setStyleName("unitime-ScrollPanel");
							final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
							dialog.setWidget(scroll);
							dialog.setText(MESSAGES.dialogChangeMessage(student.getName()));
							dialog.setEscapeToHide(true);
							dialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
								@Override
								public void onOpen(OpenEvent<UniTimeDialogBox> event) {
									RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
									scroll.setHeight(Math.min(widget.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
									dialog.setPopupPosition(
											Math.max(Window.getScrollLeft() + (Window.getClientWidth() - dialog.getOffsetWidth()) / 2, 0),
											Math.max(Window.getScrollTop() + (Window.getClientHeight() - dialog.getOffsetHeight()) / 2, 0));
								}
							});
							dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
								@Override
								public void onClose(CloseEvent<PopupPanel> event) {
									table.clearHover();
									RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
								}
							});
							dialog.center();
						}
					}
				});
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false);
				final ScrollPanel scroll = new ScrollPanel(table);
				scroll.setHeight(((int)(0.8 * Window.getClientHeight())) + "px");
				scroll.setStyleName("unitime-ScrollPanel");
				dialog.setWidget(scroll);
				dialog.setText(MESSAGES.dialogChangeLog(student.getName()));
				dialog.setEscapeToHide(true);
				dialog.addOpenHandler(new OpenHandler<UniTimeDialogBox>() {
					@Override
					public void onOpen(OpenEvent<UniTimeDialogBox> event) {
						RootPanel.getBodyElement().getStyle().setOverflow(Overflow.HIDDEN);
						scroll.setHeight(Math.min(table.getElement().getScrollHeight(), Window.getClientHeight() * 80 / 100) + "px");
						dialog.setPopupPosition(
								Math.max(Window.getScrollLeft() + (Window.getClientWidth() - dialog.getOffsetWidth()) / 2, 0),
								Math.max(Window.getScrollTop() + (Window.getClientHeight() - dialog.getOffsetHeight()) / 2, 0));
					}
				});
				dialog.addCloseHandler(new CloseHandler<PopupPanel>() {
					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						table.clearHover();
						RootPanel.getBodyElement().getStyle().setOverflow(Overflow.AUTO);
					}
				});
				callback.onSuccess(true);
				dialog.center();
			}
		});
	}
	
	private static class Number extends HTML implements HasCellAlignment {
		public Number(String text) {
			super(text, false);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	protected void refresh() {
		clear();
		iHeader.showLoading();
		iHeader.setEnabled("approve", false);
		iHeader.setEnabled("reject", false);
		if (iOfferingId != null) {
			iSectioningService.canApprove(iOfferingId, new AsyncCallback<List<Long>>() {
				@Override
				public void onFailure(Throwable caught) {
					iHeader.setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
				}
				@Override
				public void onSuccess(final List<Long> courseIdsToApprove) {
					iSectioningService.listEnrollments(iOfferingId, new AsyncCallback<List<ClassAssignmentInterface.Enrollment>>() {
						@Override
						public void onFailure(Throwable caught) {
							iHeader.setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
							iHeader.setCollapsible(null);
							SectioningCookie.getInstance().setEnrollmentCoursesDetails(false);
						}
						@Override
						public void onSuccess(List<ClassAssignmentInterface.Enrollment> result) {
							if (result.isEmpty()) {
								iHeader.setMessage(iOfferingId >= 0 ? MESSAGES.offeringHasNoEnrollments() : MESSAGES.classHasNoEnrollments());
								iHeader.setCollapsible(null);
							} else {
								iHeader.clearMessage();
								iHeader.setCollapsible(true);
								populate(result, courseIdsToApprove);
								if (iEnrollments.getRowCount() > 2) {
									for (int row = 1; row < iEnrollments.getRowCount() - 1; row++) {
										iEnrollments.getRowFormatter().setVisible(row, SectioningCookie.getInstance().getEnrollmentCoursesDetails());
									}
								}
								iHeader.setEnabled("approve", courseIdsToApprove != null && !courseIdsToApprove.isEmpty() && iApprove != null && iApprove.isApplicable());
								iHeader.setEnabled("reject", courseIdsToApprove != null && !courseIdsToApprove.isEmpty() && iReject != null && iReject.isApplicable());
							}
						}
					});					
				}
			});
		}
	}
	
	public void clear() {
		for (int row = iEnrollments.getRowCount() - 1; row >= 0; row--) {
			iEnrollments.removeRow(row);
		}
		iEnrollments.clear(true);
	}


	public void populate(List<ClassAssignmentInterface.Enrollment> enrollments, List<Long> courseIdsCanApprove) {
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		Collections.sort(enrollments, new Comparator<ClassAssignmentInterface.Enrollment>() {
			@Override
			public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
				int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
				if (cmp != 0) return cmp;
				return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
			}
		});
		
		final UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		//hStudent.setWidth("100px");
		header.add(hStudent);
		hStudent.addOperation(new Operation() {
			@Override
			public void execute() {
				iEnrollments.sort(hStudent, new Comparator<ClassAssignmentInterface.Enrollment>() {
					@Override
					public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
						int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
						if (cmp != 0) return cmp;
						return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
					}
				});
			}
			@Override
			public boolean isApplicable() {
				return true;
			}
			@Override
			public boolean hasSeparator() {
				return false;
			}
			@Override
			public String getName() {
				return MESSAGES.sortBy(MESSAGES.colStudent());
			}
		});
		
		boolean crosslist = false;
		Long courseId = null;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (courseId == null) courseId = e.getCourseId();
			else if (e.getCourseId() != courseId) { crosslist = true; break; }
		}
		
		if (crosslist) {
			final UniTimeTableHeader hCourse = new UniTimeTableHeader(MESSAGES.colCourse());
			//hCourse.setWidth("100px");
			header.add(hCourse);
			hCourse.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hCourse, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getCourseName().compareTo(e2.getCourseName());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colCourse());
				}
			});			
		}
		
		boolean hasPriority = false, hasArea = false, hasMajor = false, hasGroup = false, hasAlternative = false, hasReservation = false, hasRequestedDate = false, hasEnrolledDate = false, hasConflict = false;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.getPriority() > 0) hasPriority = true;
			if (e.isAlternative()) hasAlternative = true;
			if (e.getStudent().hasArea()) hasArea = true;
			if (e.getStudent().hasMajor()) hasMajor = true;
			if (e.getStudent().hasGroup()) hasGroup = true;
			if (e.getReservation() != null) hasReservation = true;
			if (e.getRequestedDate() != null) hasRequestedDate = true;
			if (e.getEnrolledDate() != null) hasEnrolledDate = true;
			if (e.hasConflict()) hasConflict = true;
		}

		if (hasPriority) {
			final UniTimeTableHeader hPriority = new UniTimeTableHeader(MESSAGES.colPriority());
			//hPriority.setWidth("100px");
			hPriority.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hPriority, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = new Integer(e1.getPriority()).compareTo(e2.getPriority());
							if (cmp != 0) return cmp;
							cmp = e1.getAlternative().compareTo(e2.getAlternative());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colPriority());
				}
			});
			header.add(hPriority);			
		}

		if (hasAlternative) {
			final UniTimeTableHeader hAlternative = new UniTimeTableHeader(MESSAGES.colAlternative());
			//hAlternative.setWidth("100px");
			hAlternative.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hAlternative, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getAlternative().compareTo(e2.getAlternative());
							if (cmp != 0) return cmp;
							cmp = new Integer(e1.getPriority()).compareTo(e2.getPriority());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colAlternative());
				}
			});
			header.add(hAlternative);
		}
		
		if (hasArea) {
			final UniTimeTableHeader hArea = new UniTimeTableHeader(MESSAGES.colArea());
			//hArea.setWidth("100px");
			header.add(hArea);
			hArea.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hArea, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colArea());
				}
			});
			
			final UniTimeTableHeader hClasf = new UniTimeTableHeader(MESSAGES.colClassification());
			//hClasf.setWidth("100px");
			header.add(hClasf);
			hClasf.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hClasf, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getClassification("|").compareTo(e2.getStudent().getClassification("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getArea("|").compareTo(e2.getStudent().getArea("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colClassification());
				}
			});
		}

		if (hasMajor) {
			final UniTimeTableHeader hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			//hMajor.setWidth("100px");
			header.add(hMajor);
			hMajor.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hMajor, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colMajor());
				}
			});
		}
		
		if (hasGroup) {
			final UniTimeTableHeader hGroup = new UniTimeTableHeader(MESSAGES.colGroup());
			//hGroup.setWidth("100px");
			header.add(hGroup);
			hGroup.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hGroup, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getStudent().getGroup("|").compareTo(e2.getStudent().getGroup("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colGroup());
				}
			});
		}

		if (hasReservation) {
			final UniTimeTableHeader hReservation = new UniTimeTableHeader(MESSAGES.colReservation());
			//hReservation.setWidth("100px");
			hReservation.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hReservation, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = (e1.getReservation() == null ? "" : e1.getReservation()).compareTo(e2.getReservation() == null ? "" : e2.getReservation());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colReservation());
				}
			});
			header.add(hReservation);			
		}
		
		final TreeSet<String> subparts = new TreeSet<String>();
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (e.hasClasses())
				for (ClassAssignmentInterface.ClassAssignment c: e.getClasses())
					subparts.add(c.getSubpart());
		}
		
		for (final String subpart: subparts) {
			final UniTimeTableHeader hSubpart = new UniTimeTableHeader(subpart);
			//hSubpart.setWidth("100px");
			final int col = 1 + (crosslist ? 1 : 0) + (hasPriority ? 1 : 0) + (hasAlternative ? 1 : 0) + (hasArea ? 2 : 0) + (hasMajor ? 1 : 0) + (hasGroup ? 1 : 0) + (hasReservation ? 1 : 0);
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					SectioningCookie.getInstance().setShowClassNumbers(false);
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						ClassAssignmentInterface.Enrollment e = iEnrollments.getData(row);
						if (e == null || !e.hasClasses()) continue;
						int idx = 0;
						for (String subpart: subparts) {
							((HTML)iEnrollments.getWidget(row, col + idx)).setHTML(e.getClasses(subpart, ", ", false));
							idx ++;
						}
					}
				}
				@Override
				public boolean isApplicable() {
					return SectioningCookie.getInstance().getShowClassNumbers();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.showExternalIds();
				}
			});
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					SectioningCookie.getInstance().setShowClassNumbers(true);
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						ClassAssignmentInterface.Enrollment e = iEnrollments.getData(row);
						if (e == null || !e.hasClasses()) continue;
						int idx = 0;
						for (String subpart: subparts) {
							((HTML)iEnrollments.getWidget(row, col + idx)).setHTML(e.getClasses(subpart, ", ", true));
							idx ++;
						}
					}
				}
				@Override
				public boolean isApplicable() {
					return !SectioningCookie.getInstance().getShowClassNumbers();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.showClassNumbers();
				}
			});
			hSubpart.addOperation(new Operation() {
				@Override
				public void execute() {
					final boolean showClassNumbers = SectioningCookie.getInstance().getShowClassNumbers();
					iEnrollments.sort(hSubpart, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = e1.getClasses(subpart, "|", showClassNumbers).compareTo(e2.getClasses(subpart, "|", showClassNumbers));
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(subpart);
				}
			});
			header.add(hSubpart);
		}
		
		if (hasRequestedDate) {
			final UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colRequestTimeStamp());
			//hTimeStamp.setWidth("100px");
			hTimeStamp.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hTimeStamp, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = (e1.getRequestedDate() == null ? new Date(0) : e1.getRequestedDate()).compareTo(e2.getRequestedDate() == null ? new Date(0) : e2.getRequestedDate());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colRequestTimeStamp());
				}
			});
			header.add(hTimeStamp);			
		}
		
		if (hasEnrolledDate) {
			final UniTimeTableHeader hTimeStamp = new UniTimeTableHeader(MESSAGES.colEnrollmentTimeStamp());
			//hTimeStamp.setWidth("100px");
			hTimeStamp.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hTimeStamp, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = (e1.getEnrolledDate() == null ? new Date(0) : e1.getEnrolledDate()).compareTo(e2.getEnrolledDate() == null ? new Date(0) : e2.getEnrolledDate());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colEnrollmentTimeStamp());
				}
			});
			header.add(hTimeStamp);			
		}
		
		if (hasConflict) {
			final UniTimeTableHeader hConflictType = new UniTimeTableHeader(MESSAGES.colConflictType());
			hConflictType.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hConflictType, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							if (e1.hasConflict()) {
								if (e2.hasConflict()) {
									Iterator<Conflict> i1 = e1.getConflicts().iterator();
									Iterator<Conflict> i2 = e2.getConflicts().iterator();
									while (i1.hasNext() && i2.hasNext()) {
										Conflict c1 = i1.next();
										Conflict c2 = i2.next();
										int cmp = c1.getType().compareTo(c2.getType());
										if (cmp != 0) return cmp;
										cmp = c1.getName().compareTo(c2.getName());
										if (cmp != 0) return cmp;
									}
									if (i1.hasNext()) return -1;
									if (i2.hasNext()) return 1;
								} else {
									return -1;
								}
							} else if (e2.hasConflict()) {
								return 1;
							}
							int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colConflictType());
				}
			});
			header.add(hConflictType);
			
			final UniTimeTableHeader hConflictName = new UniTimeTableHeader(MESSAGES.colConflictName());
			hConflictName.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hConflictName, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							if (e1.hasConflict()) {
								if (e2.hasConflict()) {
									Iterator<Conflict> i1 = e1.getConflicts().iterator();
									Iterator<Conflict> i2 = e2.getConflicts().iterator();
									while (i1.hasNext() && i2.hasNext()) {
										Conflict c1 = i1.next();
										Conflict c2 = i2.next();
										int cmp = c1.getName().compareTo(c2.getName());
										if (cmp != 0) return cmp;
									}
									if (i1.hasNext()) return -1;
									if (i2.hasNext()) return 1;
								} else {
									return -1;
								}
							} else if (e2.hasConflict()) {
								return 1;
							}
							int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colConflictName());
				}
			});
			header.add(hConflictName);

			final UniTimeTableHeader hConflictDate = new UniTimeTableHeader(MESSAGES.colConflictDate());
			hConflictDate.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hConflictDate, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							if (e1.hasConflict()) {
								if (e2.hasConflict()) {
									Iterator<Conflict> i1 = e1.getConflicts().iterator();
									Iterator<Conflict> i2 = e2.getConflicts().iterator();
									while (i1.hasNext() && i2.hasNext()) {
										Conflict c1 = i1.next();
										Conflict c2 = i2.next();
										int cmp = c1.getDate().compareTo(c2.getDate());
										if (cmp != 0) return cmp;
										cmp = c1.getName().compareTo(c2.getName());
										if (cmp != 0) return cmp;
									}
									if (i1.hasNext()) return -1;
									if (i2.hasNext()) return 1;
								} else {
									return -1;
								}
							} else if (e2.hasConflict()) {
								return 1;
							}
							int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colConflictDate());
				}
			});
			header.add(hConflictDate);
			final UniTimeTableHeader hConflictTime = new UniTimeTableHeader(MESSAGES.colConflictTime());
			hConflictTime.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hConflictTime, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							if (e1.hasConflict()) {
								if (e2.hasConflict()) {
									Iterator<Conflict> i1 = e1.getConflicts().iterator();
									Iterator<Conflict> i2 = e2.getConflicts().iterator();
									while (i1.hasNext() && i2.hasNext()) {
										Conflict c1 = i1.next();
										Conflict c2 = i2.next();
										int cmp = c1.getTime().compareTo(c2.getTime());
										if (cmp != 0) return cmp;
										cmp = c1.getName().compareTo(c2.getName());
										if (cmp != 0) return cmp;
									}
									if (i1.hasNext()) return -1;
									if (i2.hasNext()) return 1;
								} else {
									return -1;
								}
							} else if (e2.hasConflict()) {
								return 1;
							}
							int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colConflictTime());
				}
			});
			header.add(hConflictTime);
			final UniTimeTableHeader hConflictRoom = new UniTimeTableHeader(MESSAGES.colConflictRoom());
			hConflictRoom.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hConflictRoom, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							if (e1.hasConflict()) {
								if (e2.hasConflict()) {
									Iterator<Conflict> i1 = e1.getConflicts().iterator();
									Iterator<Conflict> i2 = e2.getConflicts().iterator();
									while (i1.hasNext() && i2.hasNext()) {
										Conflict c1 = i1.next();
										Conflict c2 = i2.next();
										int cmp = c1.getRoom().compareTo(c2.getRoom());
										if (cmp != 0) return cmp;
										cmp = c1.getName().compareTo(c2.getName());
										if (cmp != 0) return cmp;
									}
									if (i1.hasNext()) return -1;
									if (i2.hasNext()) return 1;
								} else {
									return -1;
								}
							} else if (e2.hasConflict()) {
								return 1;
							}
							int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colConflictRoom());
				}
			});
			header.add(hConflictRoom);
		}
		
		if (courseIdsCanApprove != null && !courseIdsCanApprove.isEmpty()) {
			final UniTimeTableHeader hApproved = new UniTimeTableHeader(MESSAGES.colApproved());
			//hTimeStamp.setWidth("100px");
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox)
							((CheckBox)w).setValue(true);
					}
					iHeader.setEnabled("approve", iApprove != null && iApprove.isApplicable());
					iHeader.setEnabled("reject", iReject != null && iReject.isApplicable());
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && !((CheckBox)w).getValue())
							return true;
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.selectAll();
				}
			});
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox)
							((CheckBox)w).setValue(false);
					}
					iHeader.setEnabled("approve", iApprove != null && iApprove.isApplicable());
					iHeader.setEnabled("reject", iReject != null && iReject.isApplicable());
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							return true;
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.clearAll();
				}
			});
			
			iApprove = new Operation() {
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>();
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							studentIds.add(iEnrollments.getData(row).getStudent().getId());
					}
					iHeader.showLoading();
					iSectioningService.approveEnrollments(iOfferingId, studentIds, new AsyncCallback<String>() {
						@Override
						public void onSuccess(String result) {
							iHeader.clearMessage();
							String[] approval = result.split(":");
							for (int row = 0; row < iEnrollments.getRowCount(); row++) {
								Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
								if (w instanceof CheckBox && ((CheckBox)w).getValue())
									iEnrollments.replaceWidget(row, iEnrollments.getCellCount(row) - 1,
											new HTML(MESSAGES.approval(sDF.format(new Date(Long.valueOf(approval[0]))), approval[2]), false));
							}
							iHeader.setEnabled("approve", iApprove != null && iApprove.isApplicable());
							iHeader.setEnabled("reject", iReject != null && iReject.isApplicable());
						}
						@Override
						public void onFailure(Throwable caught) {
							iHeader.setErrorMessage(MESSAGES.failedToApproveEnrollments(caught.getMessage()));
						}
					});
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							return true;
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.approveSelectedEnrollments();
				}
			};
			hApproved.addOperation(iApprove);
			
			iReject = new Operation() {
				@Override
				public void execute() {
					List<Long> studentIds = new ArrayList<Long>();
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							studentIds.add(iEnrollments.getData(row).getStudent().getId());
					}
					iHeader.showLoading();
					iSectioningService.rejectEnrollments(iOfferingId, studentIds, new AsyncCallback<Boolean>() {
						@Override
						public void onSuccess(Boolean result) {
							iHeader.clearMessage();
							for (int row = 0; row < iEnrollments.getRowCount(); ) {
								Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
								if (w instanceof CheckBox && ((CheckBox)w).getValue())
									iEnrollments.removeRow(row);
								else
									row++;
							}
							iHeader.setEnabled("approve", iApprove != null && iApprove.isApplicable());
							iHeader.setEnabled("reject", iReject != null && iReject.isApplicable());
						}
						@Override
						public void onFailure(Throwable caught) {
							iHeader.setErrorMessage(MESSAGES.failedToApproveEnrollments(caught.getMessage()));
						}
					});
				}
				@Override
				public boolean isApplicable() {
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, iEnrollments.getCellCount(row) - 1);
						if (w instanceof CheckBox && ((CheckBox)w).getValue())
							return true;
					}
					return false;
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public String getName() {
					return MESSAGES.rejectSelectedEnrollments();
				}
			};
			hApproved.addOperation(iReject);
			
			hApproved.addOperation(new Operation() {
				@Override
				public void execute() {
					iEnrollments.sort(hApproved, new Comparator<ClassAssignmentInterface.Enrollment>() {
						@Override
						public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
							int cmp = new Long(e1.getApprovedDate() == null ? 0 : e1.getApprovedDate().getTime()).compareTo(e2.getApprovedDate() == null ? 0 : e2.getApprovedDate().getTime());
							if (cmp != 0) return cmp;
							cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
							if (cmp != 0) return cmp;
							return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
						}
					});
				}
				@Override
				public boolean isApplicable() {
					return true;
				}
				@Override
				public boolean hasSeparator() {
					return true;
				}
				@Override
				public String getName() {
					return MESSAGES.sortBy(MESSAGES.colApproved());
				}
			});
			header.add(hApproved);	
		} else {
			iApprove = null;
			iReject = null;
		}
		
				
		iEnrollments.addRow(null, header);
		
		int enrolled = 0; int waitlisted = 0; int unassigned = 0;
		boolean suffix = SectioningCookie.getInstance().getShowClassNumbers();
		for (ClassAssignmentInterface.Enrollment enrollment: enrollments) {
			List<Widget> line = new ArrayList<Widget>();
			line.add(new Label(enrollment.getStudent().getName(), false));
			if (crosslist)
				line.add(new Label(enrollment.getCourseName(), false));
			if (hasPriority)
				line.add(new Number(enrollment.getPriority() <= 0 ? "&nbsp;" : MESSAGES.priority(enrollment.getPriority())));
			if (hasAlternative)
				line.add(new Label(enrollment.getAlternative(), false));
			if (hasArea) {
				line.add(new HTML(enrollment.getStudent().getArea("<br>"), false));
				line.add(new HTML(enrollment.getStudent().getClassification("<br>"), false));
			}
			if (hasMajor)
				line.add(new HTML(enrollment.getStudent().getMajor("<br>"), false));
			if (hasGroup)
				line.add(new HTML(enrollment.getStudent().getGroup("<br>"), false));
			if (hasReservation)
				line.add(new HTML(enrollment.getReservation() == null ? "&nbsp;" : enrollment.getReservation(), false));
			if (!subparts.isEmpty()) {
				if (!enrollment.hasClasses()) {
					line.add(new WarningLabel(enrollment.isWaitList() ? MESSAGES.courseWaitListed() : MESSAGES.courseNotEnrolled(), subparts.size()));
				} else for (String subpart: subparts) {
					line.add(new HTML(enrollment.getClasses(subpart, ", ", suffix), false));
				}
			}
			if (hasRequestedDate)
				line.add(new HTML(enrollment.getRequestedDate() == null ? "&nbsp;" : sDF.format(enrollment.getRequestedDate()), false));
			if (hasEnrolledDate)
				line.add(new HTML(enrollment.getEnrolledDate() == null ? "&nbsp;" : sDF.format(enrollment.getEnrolledDate()), false));
			if (hasConflict) {
				if (enrollment.hasConflict()) {
					String name = "", type = "", date = "", time = "", room = "";
					for (Conflict conflict: enrollment.getConflicts()) {
						if (!name.isEmpty()) { name += "<br>"; type += "<br>"; date += "<br>"; time += "<br>"; room += "<br>"; }
						if (conflict.hasStyle()) {
							name += "<span class='" + conflict.getStyle() + "'>";
							type += "<span class='" + conflict.getStyle() + "'>";
							date += "<span class='" + conflict.getStyle() + "'>";
							time += "<span class='" + conflict.getStyle() + "'>";
							room += "<span class='" + conflict.getStyle() + "'>";
						}
						name += conflict.getName();
						type += conflict.getType();
						date += conflict.getDate();
						time += conflict.getTime();
						room += conflict.getRoom();
						if (conflict.hasStyle()) {
							name += "</span>";
							type += "</span>";
							date += "</span>";
							time += "</span>";
							room += "</span>";
						}
					}
					HTML html = new HTML(type, false); html.addStyleName("conflict"); line.add(html);
					html = new HTML(name, false); html.addStyleName("conflict"); line.add(html);
					html = new HTML(date, false); html.addStyleName("conflict"); line.add(html);
					html = new HTML(time, false); html.addStyleName("conflict"); line.add(html);
					html = new HTML(room, false); html.addStyleName("conflict"); line.add(html);
				} else {
					line.add(new HTML("&nbsp;", false));
					line.add(new HTML("&nbsp;", false));
					line.add(new HTML("&nbsp;", false));
					line.add(new HTML("&nbsp;", false));
					line.add(new HTML("&nbsp;", false));
				}
			}
			if (courseIdsCanApprove != null && !courseIdsCanApprove.isEmpty()) {
				if (!enrollment.hasClasses()) { // not enrolled
					line.add(new HTML("&nbsp;"));
				} else if (!courseIdsCanApprove.contains(enrollment.getCourseId())) { // cannot approve this course
					line.add(new HTML("&nbsp;"));
				} else if (enrollment.getApprovedDate() == null) { // not yet approved
					CheckBox ch = new CheckBox();
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
							iHeader.setEnabled("approve", iApprove != null && iApprove.isApplicable());
							iHeader.setEnabled("reject", iReject != null && iReject.isApplicable());
						}
					});
					line.add(ch);
				} else { // already approved
					line.add(new HTML(MESSAGES.approval(sDF.format(enrollment.getApprovedDate()), enrollment.getApprovedBy()), false));
				}
			}
			iEnrollments.addRow(enrollment, line);
			iEnrollments.getRowFormatter().setVerticalAlign(iEnrollments.getRowCount() - 1, HasVerticalAlignment.ALIGN_TOP);
			if (enrollment.hasClasses())
				enrolled++;
			else if (enrollment.isWaitList())
				waitlisted++;
			else
				unassigned++;
		}
		
		List<TotalLabel> footer = new ArrayList<TotalLabel>();
		if (enrolled > 0)
			footer.add(new TotalLabel(MESSAGES.totalEnrolled(enrolled), header.size()));
		if (waitlisted > 0)
			footer.add(new TotalLabel(MESSAGES.totalWaitListed(waitlisted), header.size()));
		if (unassigned > 0)
			footer.add(new TotalLabel(MESSAGES.totalNotEnrolled(unassigned + waitlisted), header.size()));
		if (footer.size() == 2) {
			footer.get(0).setColSpan(header.size() / 2);
			footer.get(1).setColSpan(header.size() - (header.size() / 2));
		} else if (footer.size() == 3) {
			footer.get(0).setColSpan(header.size() / 3);
			footer.get(1).setColSpan(header.size() / 3);
			footer.get(2).setColSpan(header.size() - 2 * (header.size() / 3));
		}
		if (!footer.isEmpty())
			iEnrollments.addRow(null, footer);
	}
	
	private static class TotalLabel extends HTML implements HasColSpan, HasStyleName {
		private int iColSpan;
		
		public TotalLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		public void setColSpan(int colSpan) {
			iColSpan = colSpan;
			
		}
		
		@Override
		public String getStyleName() {
			return "unitime-TotalRow";
		}
		
	}
	
	private static class WarningLabel extends HTML implements HasColSpan, HasStyleName {
		private int iColSpan;
		
		public WarningLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public String getStyleName() {
			return "text-red";
		}
		
	}
	
	public void insert(final RootPanel panel) {
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		if (SectioningCookie.getInstance().getEnrollmentCoursesDetails()) {
			refresh();
		} else {
			clear();
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	public void setId(Long id) { iOfferingId = id; }
	
	public Long getId() { return iOfferingId; }

	public void scrollIntoView(Long studentId) {
		for (int r = 1; r < iEnrollments.getRowCount(); r++) {
			if (iEnrollments.getData(r) != null && iEnrollments.getData(r).getStudent().getId() == studentId) {
				iEnrollments.getRowFormatter().getElement(r).scrollIntoView();
			}
		}
	}

	public static class ReservationClickedEvent {
		private ReservationInterface iReservation;
		
		public ReservationClickedEvent(ReservationInterface reservation) {
			iReservation = reservation;
		}
		
		public ReservationInterface getReservation() {
			return iReservation;
		}
	}
	
	public interface ReservationClickHandler {
		public void onClick(ReservationClickedEvent evt);
	}
	
	public void select(Long studentId) {
		for (int i = 0; i < iEnrollments.getRowCount(); i++) {
			ClassAssignmentInterface.Enrollment e = iEnrollments.getData(i);
			if (e == null) continue;
			if (e.getStudent().getId() == studentId)
				iEnrollments.getRowFormatter().setStyleName(i, "unitime-TableRowSelected");
			else if ("unitime-TableRowSelected".equals(iEnrollments.getRowFormatter().getStyleName(i)))
				iEnrollments.getRowFormatter().removeStyleName(i, "unitime-TableRowSelected");
		}
	}
	
	public static class TopCell extends Label implements HasVerticalCellAlignment {
		TopCell(String cell) {
			super(cell);
		}

		@Override
		public VerticalAlignmentConstant getVerticalCellAlignment() {
			return HasVerticalAlignment.ALIGN_TOP;
		}
	}
}
