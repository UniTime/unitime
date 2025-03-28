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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeDialogBox;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.WebTable;
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
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.SectioningAction;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Student;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.Request;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.EligibilityCheck;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.gwt.shared.TableInterface.NaturalOrderComparator;
import org.unitime.timetable.gwt.shared.UserAuthenticationProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class EnrollmentTable extends Composite {
	public static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	public static final StudentSectioningResources RESOURCES =  GWT.create(StudentSectioningResources.class);
	public static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static DateTimeFormat sDF = DateTimeFormat.getFormat(CONSTANTS.requestDateFormat());
	private static DateTimeFormat sTSF = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	private static NumberFormat sNF = NumberFormat.getFormat(CONSTANTS.executionTimeFormat());
	private Long iOfferingId = null;

	protected static final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private SimpleForm iEnrollmentPanel;
	private UniTimeTable<ClassAssignmentInterface.Enrollment> iEnrollments;
	private UniTimeHeaderPanel iHeader;
	private Operation iApprove, iReject;
	private StudentSchedule iStudentSchedule;
	
	private boolean iOnline;
	private boolean iShowFilter = false;
	private boolean iEmail = false;
	private boolean iACR = false;
	private boolean iCanSelect = false;
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	
	public EnrollmentTable(final boolean showHeader, boolean online) {
		this(showHeader, online, false);
	}
	
	public EnrollmentTable(final boolean showHeader, boolean online, boolean showFilter) {
		iOnline = online;
		iEnrollmentPanel = new SimpleForm();
		iEnrollmentPanel.addStyleName("unitime-StudentEnrollmentsTable");
		iStudentSchedule = new StudentSchedule(online) {
			@Override
			protected void setCritical(Long studentId, Request request, Integer critical, final AsyncCallback<Integer> callback) {
				iSectioningService.changeCriticalOverride(studentId, request.getRequestedCourse(0).getCourseId(), critical, callback);
			}
		};
		iShowFilter = showFilter;
		
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
							iHeader.setErrorMessage(MESSAGES.failedToLoadEnrollments(caught.getMessage()));
						} else {
							UniTimeNotifications.error(MESSAGES.failedToLoadEnrollments(caught.getMessage()), caught);
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
	
	public boolean isCanSelect() {
		if (iCanSelect) {
			for (int row = 0; row < iEnrollments.getRowCount(); row++) {
				Enrollment e = iEnrollments.getData(row);
				if (e != null && e.getStudent().isCanSelect()) return true;
			}
			return false;
		} else {
			return false;
		}
	}
	public void setCanSelect(boolean canSelect) { iCanSelect = canSelect; }
	public Set<Long> getStudentIds() {
		if (iSelectedStudentIds == null || iSelectedStudentIds.isEmpty()) {
			Set<Long> studentIds = new HashSet<Long>();
			for (int row = 0; row < iEnrollments.getRowCount(); row++) {
				Enrollment e = iEnrollments.getData(row);
				if (e != null && e.getStudent().isCanSelect())
					studentIds.add(e.getStudent().getId());
			}
			return studentIds;
		} else {
			return iSelectedStudentIds;
		}
	}
	protected int countSelectableStudents() {
		int ret = 0;
		for (int row = 0; row < iEnrollments.getRowCount(); row++) {
			Enrollment e = iEnrollments.getData(row);
			if (e != null && e.getStudent().isCanSelect()) ret ++;
		}
		return ret;
	}
	
	public UniTimeTable<ClassAssignmentInterface.Enrollment> getTable() { return iEnrollments; }
	
	public UniTimeHeaderPanel getHeader() { return iHeader; }
	
	public void setEmail(boolean email) { iEmail = email; }
	
	public void setAdvisorRecommendations(boolean acr) { iACR = acr; }
	
	public void showStudentSchedule(final Long studentId) {
		iSectioningService.lookupStudent(iOnline, studentId, new AsyncCallback<ClassAssignmentInterface.Student>() {
			@Override
			public void onSuccess(final Student student) {
				LoadingWidget.getInstance().show(MESSAGES.pleaseWait());
				showStudentSchedule(student, new AsyncCallback<Boolean>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(caught.getMessage());
					}
					@Override
					public void onSuccess(Boolean result) {
						LoadingWidget.getInstance().hide();
					}
				});						
			}
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught.getMessage());
			}
		});
	}
	
	public void showStudentSchedule(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
		iSectioningService.getEnrollment(iOnline, student.getId(), new AsyncCallback<ClassAssignmentInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			@Override
			public void onSuccess(final ClassAssignmentInterface result) {
				callback.onSuccess(true);
				iStudentSchedule.setValue(result);
				SimpleForm form = new SimpleForm();
				form.addRow(iStudentSchedule);
				final UniTimeHeaderPanel buttons = new UniTimeHeaderPanel();
				form.addBottomRow(buttons);
				if (result.hasRequest() && result.getRequest().hasErrorMessage()) {
					ScheduleStatus status = new ScheduleStatus();
					status.error(result.getRequest().getErrorMessaeg(), false);
					form.addRow(status);
				}
				final UniTimeDialogBox dialog = new UniTimeDialogBox(true, false) {
					@Override
					protected void onPreviewNativeEvent(NativePreviewEvent event) {
						super.onPreviewNativeEvent(event);
						iStudentSchedule.checkAccessKeys(event);
					}
				};
				iStudentSchedule.setSelectionHandler(new SelectionHandler<Integer>() {
					@Override
					public void onSelection(SelectionEvent<Integer> event) {
						buttons.setMessage(iStudentSchedule.getCreditMessage());
						dialog.center();
					}
				});
				dialog.setWidget(form);
				dialog.setText(MESSAGES.dialogEnrollments(student.getName()));
				dialog.setEscapeToHide(true);
				dialog.sinkEvents(Event.ONKEYUP);
				buttons.setMessage(iStudentSchedule.getCreditMessage());
				if (iACR) {
					buttons.addButton("acrf", MESSAGES.buttonAdvisorCourseRequests(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=acrf#" + student.getId(), "_blank", "");
						}
					});
					buttons.setEnabled("acrf", student.isCanSelect() && student.getSessionId() != null);
				}
				buttons.addButton("registration", MESSAGES.buttonRegistration(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						showCourseRequests(student, iOnline, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(caught);
							}
							@Override
							public void onSuccess(Boolean result) {
								if (result)
									dialog.hide();
							}
						});
					}
				});
				buttons.setEnabled("registration", student.getSessionId() != null && student.isCanRegister());
				buttons.addButton("assistant", MESSAGES.buttonAssistant(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent e) {
						showStudentAssistant(student, iOnline, new AsyncCallback<Boolean>() {
							@Override
							public void onFailure(Throwable caught) {
								UniTimeNotifications.error(caught);
							}
							@Override
							public void onSuccess(Boolean result) {
								if (result)
									dialog.hide();
							}
						});
					}
				});
				buttons.setEnabled("assistant", !iOnline || (student.getSessionId() != null && student.isCanUseAssistant()));
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
					buttons.setEnabled("log", student.getSessionId() != null && (student.isCanUseAssistant() || student.isCanRegister()));
				}
				if (iOnline && result.getRequest() != null && result.getRequest().hasSpecRegDashboardUrl()) {
					buttons.addButton("dashboard", MESSAGES.buttonSpecRegDashboard(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.open(result.getRequest().getSpecRegDashboardUrl(), "_blank", "");
						}
					});
				}
				if (student.isCanSelect() && iEmail) {
					buttons.addButton("email", MESSAGES.buttonSendStudentEmail(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							final StudentStatusDialog dialog = new StudentStatusDialog(new HashSet<StudentStatusInfo>(), null);
							if (!iOnline)
								dialog.getClassScheduleCheckBox().setVisible(false);
							dialog.sendStudentEmail(new Command() {
								@Override
								public void execute() {
									iSectioningService.sendEmail(student.getSessionId(), student.getId(),
											dialog.getSubject(), dialog.getMessage(), dialog.getCC(),
											dialog.getIncludeCourseRequests(), iOnline && dialog.getIncludeClassSchedule(), dialog.getIncludeAdvisorRequests(),
											dialog.isOptionalEmailToggle(),
											"user-enrollments",
											new AsyncCallback<Boolean>() {
												@Override
												public void onFailure(Throwable caught) {
													UniTimeNotifications.error(MESSAGES.advisorRequestsEmailFailed(caught.getMessage()), caught);
												}
												@Override
												public void onSuccess(Boolean result) {
													UniTimeNotifications.info(MESSAGES.advisorRequestsEmailSent());
												}
									});
								}
							}, null, false);
							
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
				/*
				buttons.showLoading();
				if (iOnline) {
					iSectioningService.checkEligibility(iOnline, null, student.getId(), null, new AsyncCallback<EligibilityCheck>() {
						@Override
						public void onFailure(Throwable caught) {
							buttons.clearMessage();
						}
						@Override
						public void onSuccess(EligibilityCheck result) {
							buttons.clearMessage();
							buttons.setEnabled("assistant", result.hasFlag(EligibilityCheck.EligibilityFlag.CAN_USE_ASSISTANT));
						}
					});
				} else {
					buttons.setEnabled("assistant", true);
					buttons.clearMessage();
				}*/
				dialog.center();
			}
		});
	}
	
	public static void showStudentAssistant(final ClassAssignmentInterface.Student student, final boolean online, final AsyncCallback<Boolean> callback) {
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
				return student.getSessionId();
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
		
		final StudentSectioningWidget widget = new StudentSectioningWidget(online, session, user, StudentSectioningPage.Mode.SECTIONING, false);
		
		iSectioningService.logIn(online ? "LOOKUP" : "BATCH", online ? student.getExternalId() : String.valueOf(student.getId()), null, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(String result) {
				widget.checkEligibility(new AsyncCallback<EligibilityCheck>() {

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(EligibilityCheck result) {
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
	
	public static void showCourseRequests(final ClassAssignmentInterface.Student student, final boolean online, final AsyncCallback<Boolean> callback) {
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
				return student.getSessionId();
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
		
		final StudentSectioningWidget widget = new StudentSectioningWidget(online, session, user, StudentSectioningPage.Mode.REQUESTS, false);
		
		iSectioningService.logIn(online ? "LOOKUP" : "BATCH", online ? student.getExternalId() : String.valueOf(student.getId()), null, new AsyncCallback<String>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
			@Override
			public void onSuccess(String result) {
				widget.checkEligibility(new AsyncCallback<EligibilityCheck>() {

					@Override
					public void onFailure(Throwable caught) {
						callback.onFailure(caught);
					}

					@Override
					public void onSuccess(EligibilityCheck result) {
						//widget.lastRequest(student.getSessionId(), student.getId(), true, true);
						final UniTimeDialogBox d = new UniTimeDialogBox(true, false);
						d.setWidget(widget);
						d.setText(MESSAGES.dialogRegistration(student.getName()));
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
	
	public static void showChangeLog(final ClassAssignmentInterface.Student student, final AsyncCallback<Boolean> callback) {
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
				final Map<Long, HTML> id2message = new HashMap<Long, HTML>();
				
				table.addRow(null,
						new UniTimeTableHeader(MESSAGES.colOperation()),
						new UniTimeTableHeader(MESSAGES.colTimeStamp()),
						new UniTimeTableHeader(MESSAGES.colExecutionTime()),
						new UniTimeTableHeader(MESSAGES.colResult()),
						new UniTimeTableHeader(MESSAGES.colUser()),
						new UniTimeTableHeader(MESSAGES.colMessage()));
				
				for (ClassAssignmentInterface.SectioningAction log: logs) {
					HTML message = new HTML(log.getMessage() == null ? "" : log.getMessage());
					message.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
					table.addRow(log,
							new TopCell(log.getOperation()),
							new TopCell(sTSF.format(log.getTimeStamp())),
							new TopCell(log.getWallTime() == null ? "" : sNF.format(0.001 * log.getWallTime())),
							new TopCell(log.getResult()),
							new TopCell(log.getUser() == null ? "" : log.getUser()),
							message
					);
					id2message.put(log.getLogId(), message);
				}
				if (!id2message.isEmpty()) {
					iSectioningService.getChangeLogTexts(new ArrayList<Long>(id2message.keySet()), new AsyncCallback<Map<Long,String>>() {
						@Override
						public void onSuccess(Map<Long, String> result) {
							for (Map.Entry<Long, String> e: result.entrySet()) {
								HTML html = id2message.get(e.getKey());
								if (html != null) html.setHTML(e.getValue());
							}
						}
						@Override
						public void onFailure(Throwable caught) {}
					});
				}
				table.addMouseClickListener(new MouseClickListener<ClassAssignmentInterface.SectioningAction>() {
					@Override
					public void onMouseClick(TableEvent<SectioningAction> event) {
						if (event.getData() != null) {
							LoadingWidget.getInstance().show(MESSAGES.loadingChangeLogMessage());
							iSectioningService.getChangeLogMessage(event.getData().getLogId(), new AsyncCallback<String>() {
								@Override
								public void onSuccess(String message) {
									LoadingWidget.getInstance().hide();
									final HTML widget = new HTML(message);
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
								
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}
							});
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

	protected boolean filter(SectioningCookie.EnrollmentFilter f, ClassAssignmentInterface.Enrollment e) {
		switch (f) {
		case ALL:
			return false;
		case ENROLLED:
			return !e.hasClasses();
		case WAIT_LISTED:
			return e.hasClasses() || !(e.isWaitList() && e.getStudent().getWaitListMode() == WaitListMode.WaitList);
		case NOT_ENROLLED:
			return e.hasClasses() || (e.isWaitList() && e.getStudent().getWaitListMode() == WaitListMode.WaitList); 
		default:
			return true;
		}
	}

	public void populate(final List<ClassAssignmentInterface.Enrollment> enrollments, final List<Long> courseIdsCanApprove) {
		iSelectedStudentIds.clear();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		
		int enrolled = 0; int waitlisted = 0; int unassigned = 0;
		for (ClassAssignmentInterface.Enrollment enrollment: enrollments) {
			if (enrollment.hasClasses())
				enrolled++;
			else if (enrollment.isWaitList() && enrollment.getStudent().getWaitListMode() == WaitListMode.WaitList)
				waitlisted++;
			else
				unassigned++;
		}
		
		SectioningCookie.EnrollmentFilter f = SectioningCookie.EnrollmentFilter.ALL;
		boolean showFilter = (iShowFilter && iOfferingId != null && iOfferingId > 0);
		if (showFilter) {
			f = SectioningCookie.getInstance().getEnrollmentFilter();
			if (f == SectioningCookie.EnrollmentFilter.ALL)
				iHeader.setHeaderTitle(MESSAGES.studentsTable());
			else
				iHeader.setHeaderTitle(MESSAGES.enrollmentTableFilter(CONSTANTS.enrollmentFilterValues()[f.ordinal()]));
		}
		
		Collections.sort(enrollments, new Comparator<ClassAssignmentInterface.Enrollment>() {
			@Override
			public int compare(ClassAssignmentInterface.Enrollment e1, ClassAssignmentInterface.Enrollment e2) {
				int cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
				if (cmp != 0) return cmp;
				return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
			}
		});

		boolean hasExtId = false;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (!filter(f,e) && e.getStudent().isCanShowExternalId()) { hasExtId = true; break; }
		}
		
		boolean canSelect = false;
		if (iCanSelect) {
			for (ClassAssignmentInterface.Enrollment e: enrollments) {
				if (!filter(f,e) && e.getStudent().isCanSelect()) { canSelect = true; break; }
			}
		}
		
		UniTimeTableHeader hSelect = null;
		if (canSelect) {
			hSelect = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
			header.add(hSelect);
			hSelect.setWidth("10px");
			hSelect.addAdditionalStyleName("unitime-NoPrint");
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.selectAll();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() != countSelectableStudents();
				}
				@Override
				public void execute() {
					iSelectedStudentIds.clear();
					for (Enrollment e: iEnrollments.getData())
						if (e.getStudent() != null && e.getStudent().isCanSelect())
							iSelectedStudentIds.add(e.getStudent().getId());
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, 0);
						if (w instanceof CheckBox) {
							((CheckBox)w).setValue(true);
						}
					}
				}
			});
			hSelect.addOperation(new Operation() {
				@Override
				public String getName() {
					return MESSAGES.clearAll();
				}
				@Override
				public boolean hasSeparator() {
					return false;
				}
				@Override
				public boolean isApplicable() {
					return iSelectedStudentIds.size() > 0;
				}
				@Override
				public void execute() {
					iSelectedStudentIds.clear();
					for (int row = 0; row < iEnrollments.getRowCount(); row++) {
						Widget w = iEnrollments.getWidget(row, 0);
						if (w instanceof CheckBox) {
							((CheckBox)w).setValue(false);
						}
					}
				}
			});
		}
		
		UniTimeTableHeader hExtId = null;
		if (hasExtId) {
			hExtId = new UniTimeTableHeader(MESSAGES.colStudentExternalId());
			header.add(hExtId);
			addSortOperation(hExtId, EnrollmentComparator.SortBy.EXTERNAL_ID, MESSAGES.colStudentExternalId());
		}

		
		UniTimeTableHeader hStudent = new UniTimeTableHeader(MESSAGES.colStudent());
		header.add(hStudent);
		addSortOperation(hStudent, EnrollmentComparator.SortBy.STUDENT, MESSAGES.colStudent());
		
		boolean crosslist = false;
		Long courseId = null;
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (filter(f, e)) continue;
			if (courseId == null) courseId = e.getCourseId();
			else if (e.getCourseId() != courseId) { crosslist = true; break; }
			if (e.getCourse() != null && e.getCourse().hasCrossList()) { crosslist = true; break; }
		}
		
		UniTimeTableHeader hCourse = null;
		if (crosslist) {
			hCourse = new UniTimeTableHeader(MESSAGES.colCourse());
			header.add(hCourse);
			addSortOperation(hStudent, EnrollmentComparator.SortBy.COURSE, MESSAGES.colCourse());
		}
		
		boolean hasPriority = false, hasArea = false, hasMajor = false, hasGroup = false, hasAcmd = false, hasAlternative = false, hasReservation = false, hasRequestedDate = false, hasEnrolledDate = false, hasConflict = false, hasMessage = false;
		boolean hasAdvisor = false, hasMinor = false, hasConc = false, hasDeg = false, hasProg = false, hasCamp = false, hasWaitlistedDate = false, hasWaitListedPosition = false, hasWaitListReplacement = false, hasCritical = false;
		Set<String> groupTypes = new HashSet<String>();
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (filter(f, e)) continue;
			if (e.getPriority() > 0) hasPriority = true;
			if (e.isAlternative()) hasAlternative = true;
			if (e.getStudent().hasArea()) hasArea = true;
			if (e.getStudent().hasMajor()) hasMajor = true;
			if (e.getStudent().hasGroup()) hasGroup = true;
			if (e.getStudent().hasAccommodation()) hasAcmd = true;
			if (e.getReservation() != null) hasReservation = true;
			if (e.getRequestedDate() != null) hasRequestedDate = true;
			if (e.getEnrolledDate() != null) hasEnrolledDate = true;
			if (e.hasConflict()) hasConflict = true;
			if (e.hasEnrollmentMessage()) hasMessage = true;
			if (e.getStudent().hasGroups()) groupTypes.addAll(e.getStudent().getGroupTypes());
			if (e.getStudent().hasAdvisor()) hasAdvisor = true;
			if (e.getStudent().hasMinor()) hasMinor = true;
			if (e.getStudent().hasConcentration()) hasConc = true;
			if (e.getStudent().hasDegree()) hasDeg = true;
			if (e.getStudent().hasProgram()) hasProg = true;
			if (e.getStudent().hasCampus()) hasCamp = true;
			if (e.hasWaitListedDate()) hasWaitlistedDate = true;
			if (e.hasWaitListedPosition()) hasWaitListedPosition = true;
			if (e.hasWaitListedReplacement()) hasWaitListReplacement = true;
			if (e.getCritical() != null && e.getCritical() > 0) hasCritical = true;
		}

		UniTimeTableHeader hPriority = null;
		if (hasPriority) {
			hPriority = new UniTimeTableHeader(MESSAGES.colPriority());
			header.add(hPriority);
			addSortOperation(hPriority, EnrollmentComparator.SortBy.PRIORITY, MESSAGES.colPriority());
		}

		UniTimeTableHeader hAlternative = null;
		if (hasAlternative) {
			hAlternative = new UniTimeTableHeader(MESSAGES.colAlternative());
			header.add(hAlternative);
			addSortOperation(hAlternative, EnrollmentComparator.SortBy.ALTERNATIVE, MESSAGES.colAlternative());
		}
		
		UniTimeTableHeader hCampus = null;
		if (hasCamp) {
			hCampus = new UniTimeTableHeader(MESSAGES.colCampus());
			header.add(hCampus);
			addSortOperation(hCampus, EnrollmentComparator.SortBy.CAMPUS, MESSAGES.colCampus());
		}
		
		UniTimeTableHeader hArea = null, hClasf = null;
		if (hasArea) {
			hArea = new UniTimeTableHeader(MESSAGES.colArea());
			header.add(hArea);
			addSortOperation(hArea, EnrollmentComparator.SortBy.AREA, MESSAGES.colArea());
			
			hClasf = new UniTimeTableHeader(MESSAGES.colClassification());
			header.add(hClasf);
			addSortOperation(hClasf, EnrollmentComparator.SortBy.CLASSIFICATION, MESSAGES.colClassification());
		}
		
		UniTimeTableHeader hDegree = null;
		if (hasDeg) {
			hDegree = new UniTimeTableHeader(MESSAGES.colDegree());
			header.add(hDegree);
			addSortOperation(hDegree, EnrollmentComparator.SortBy.DEGREE, MESSAGES.colDegree());
		}
		
		UniTimeTableHeader hProgram = null;
		if (hasProg) {
			hProgram = new UniTimeTableHeader(MESSAGES.colProgram());
			header.add(hProgram);
			addSortOperation(hProgram, EnrollmentComparator.SortBy.PROGRAM, MESSAGES.colProgram());
		}

		UniTimeTableHeader hMajor = null;
		if (hasMajor) {
			hMajor = new UniTimeTableHeader(MESSAGES.colMajor());
			header.add(hMajor);
			addSortOperation(hMajor, EnrollmentComparator.SortBy.MAJOR, MESSAGES.colMajor());
		}
		
		UniTimeTableHeader hConc = null;
		if (hasConc) {
			hConc = new UniTimeTableHeader(MESSAGES.colConcentration());
			header.add(hConc);
			addSortOperation(hConc, EnrollmentComparator.SortBy.CONCENTRATION, MESSAGES.colConcentration());
		}
		
		UniTimeTableHeader hMinor = null;
		if (hasMinor) {
			hMinor = new UniTimeTableHeader(MESSAGES.colMinor());
			header.add(hMinor);
			addSortOperation(hMinor, EnrollmentComparator.SortBy.MINOR, MESSAGES.colMinor());
		}
		
		UniTimeTableHeader hGroup = null;
		if (hasGroup) {
			hGroup = new UniTimeTableHeader(MESSAGES.colGroup());
			header.add(hGroup);
			addSortOperation(hGroup, EnrollmentComparator.SortBy.GROUP, MESSAGES.colGroup());
		}
		
		Map<String, UniTimeTableHeader> hGroups = new HashMap<String, UniTimeTableHeader>();
		for (String type: groupTypes) {
			UniTimeTableHeader h = new UniTimeTableHeader(type); 
			header.add(h);
			addSortOperation(h, EnrollmentComparator.SortBy.GROUP, MESSAGES.colGroup(), type);
			hGroups.put(type, h);
		}
		
		UniTimeTableHeader hAccmd = null;
		if (hasAcmd) {
			hAccmd = new UniTimeTableHeader(MESSAGES.colAccommodation());
			header.add(hAccmd);
			addSortOperation(hAccmd, EnrollmentComparator.SortBy.ACCOMODATION, MESSAGES.colAccommodation());
		}

		UniTimeTableHeader hReservation = null;
		if (hasReservation) {
			hReservation = new UniTimeTableHeader(MESSAGES.colReservation());
			header.add(hReservation);
			addSortOperation(hReservation, EnrollmentComparator.SortBy.RESERVATION, MESSAGES.colReservation());
		}
		
		final TreeSet<String> subparts = new TreeSet<String>();
		for (ClassAssignmentInterface.Enrollment e: enrollments) {
			if (!filter(f, e) && e.hasClasses())
				for (ClassAssignmentInterface.ClassAssignment c: e.getClasses())
					subparts.add(c.getSubpart());
		}
		
		Map<String, UniTimeTableHeader> hSubparts = new HashMap<String, UniTimeTableHeader>();
		for (final String subpart: subparts) {
			UniTimeTableHeader hSubpart = new UniTimeTableHeader(subpart);
			hSubparts.put(subpart, hSubpart);
			final int col = 1 + (canSelect ? 1 : 0) + (hasExtId ? 1 : 0) + (crosslist ? 1 : 0) + (hasPriority ? 1 : 0) + (hasAlternative ? 1 : 0) + (hasArea ? 2 : 0) + (hasDeg ? 1 : 0) +  (hasProg ? 1 : 0) + (hasCamp ? 1 : 0) + (hasMajor ? 1 : 0) + (hasConc ? 1 : 0) + (hasMinor ? 1 : 0) + (hasGroup ? 1 : 0) + (hasAcmd ? 1 : 0) + (hasReservation ? 1 : 0) + groupTypes.size();
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
			header.add(hSubpart);
			addSortOperation(hSubpart, subpart);
		}
		
		UniTimeTableHeader hCritical = null;
		if (hasCritical) {
			hCritical = new UniTimeTableHeader(MESSAGES.colCritical());
			header.add(hCritical);
			addSortOperation(hCritical, EnrollmentComparator.SortBy.CRITICAL, MESSAGES.colCritical());
		}
		
		UniTimeTableHeader hRequestTS = null; 
		if (hasRequestedDate) {
			hRequestTS = new UniTimeTableHeader(MESSAGES.colRequestTimeStamp());
			header.add(hRequestTS);
			addSortOperation(hRequestTS, EnrollmentComparator.SortBy.REQUEST_TS, MESSAGES.colRequestTimeStamp());
		}
				
		UniTimeTableHeader hEnrollmentTS = null;
		if (hasEnrolledDate) {
			hEnrollmentTS = new UniTimeTableHeader(MESSAGES.colEnrollmentTimeStamp());
			header.add(hEnrollmentTS);
			addSortOperation(hEnrollmentTS, EnrollmentComparator.SortBy.ENROLLMENT_TS, MESSAGES.colEnrollmentTimeStamp());
		}
		
		UniTimeTableHeader hWaitlistTS = null; 
		if (hasWaitlistedDate) {
			hWaitlistTS = new UniTimeTableHeader(MESSAGES.colWaitListedTimeStamp());
			header.add(hWaitlistTS);
			addSortOperation(hWaitlistTS, EnrollmentComparator.SortBy.WAITLIST_TS, MESSAGES.colWaitListedTimeStamp());
		}
		
		UniTimeTableHeader hWaitlistREP = null; 
		if (hasWaitListReplacement) {
			hWaitlistREP = new UniTimeTableHeader(MESSAGES.colWaitListSwapWithCourseOffering());
			header.add(hWaitlistREP);
			addSortOperation(hWaitlistREP, EnrollmentComparator.SortBy.WAITLIST_REPLACE, MESSAGES.colWaitListSwapWithCourseOffering());
		}

		UniTimeTableHeader hWaitlistPOS = null; 
		if (hasWaitListedPosition) {
			hWaitlistPOS = new UniTimeTableHeader(MESSAGES.colWaitListPosition());
			header.add(hWaitlistPOS);
			addSortOperation(hWaitlistPOS, EnrollmentComparator.SortBy.WAITLIST_POS, MESSAGES.colWaitListPosition());
		}

		UniTimeTableHeader hMessage = null;
		if (hasMessage) {
			hMessage = new UniTimeTableHeader(MESSAGES.colMessage());
			header.add(hMessage);
			addSortOperation(hMessage, EnrollmentComparator.SortBy.MESSAGE, MESSAGES.colMessage());
		}
		
		UniTimeTableHeader hAdvisor = null;
		if (hasAdvisor) {
			hAdvisor = new UniTimeTableHeader(MESSAGES.colAdvisor());
			header.add(hAdvisor);
			addSortOperation(hAdvisor, EnrollmentComparator.SortBy.MESSAGE, MESSAGES.colAdvisor());
		}
		
		UniTimeTableHeader hConflictType = null, hConflictName = null, hConflictDate = null, hConflictTime = null, hConflictRoom = null;
		if (hasConflict) {
			hConflictType = new UniTimeTableHeader(MESSAGES.colConflictType());
			header.add(hConflictType);
			addSortOperation(hConflictType, EnrollmentComparator.SortBy.CONFLICT_TYPE, MESSAGES.colConflictType());
			
			hConflictName = new UniTimeTableHeader(MESSAGES.colConflictName());
			header.add(hConflictName);
			addSortOperation(hConflictName, EnrollmentComparator.SortBy.CONFLICT_NAME, MESSAGES.colConflictName());

			hConflictDate = new UniTimeTableHeader(MESSAGES.colConflictDate());
			header.add(hConflictDate);
			addSortOperation(hConflictDate, EnrollmentComparator.SortBy.CONFLICT_DATE, MESSAGES.colConflictDate());
			
			hConflictTime = new UniTimeTableHeader(MESSAGES.colConflictTime());
			header.add(hConflictTime);
			addSortOperation(hConflictTime, EnrollmentComparator.SortBy.CONFLICT_TIME, MESSAGES.colConflictTime());
			
			hConflictRoom = new UniTimeTableHeader(MESSAGES.colConflictRoom());
			header.add(hConflictRoom);
			addSortOperation(hConflictRoom, EnrollmentComparator.SortBy.CONFLICT_ROOM, MESSAGES.colConflictRoom());
		}
		
		UniTimeTableHeader hApproved = null;
		if (courseIdsCanApprove != null && !courseIdsCanApprove.isEmpty()) {
			hApproved = new UniTimeTableHeader(MESSAGES.colApproved());
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
			
			header.add(hApproved);
			addSortOperation(hApproved, EnrollmentComparator.SortBy.APPROVED, MESSAGES.colApproved());
		} else {
			iApprove = null;
			iReject = null;
		}
		
				
		iEnrollments.addRow(null, header);
		
		boolean suffix = SectioningCookie.getInstance().getShowClassNumbers();
		for (ClassAssignmentInterface.Enrollment enrollment: enrollments) {
			if (filter(f, enrollment)) continue;
			List<Widget> line = new ArrayList<Widget>();
			if (canSelect) {
				if (enrollment.getStudent().isCanSelect()) {
					CheckBox ch = new CheckBox();
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					final Long sid = enrollment.getStudent().getId();
					if (iSelectedStudentIds.contains(sid)) {
						ch.setValue(true);
					}
					ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue())
								iSelectedStudentIds.add(sid);
							else
								iSelectedStudentIds.remove(sid);
						}
					});
					line.add(ch);
				} else {
					line.add(new Label(""));
				}
			}
			if (hasExtId)
				line.add(new Label(enrollment.getStudent().isCanShowExternalId() ? enrollment.getStudent().getExternalId() : "", false));
			line.add(new Label(enrollment.getStudent().getName(), false));
			if (crosslist)
				line.add(new Label(enrollment.getCourseName(), false));
			if (hasPriority)
				line.add(new Label(enrollment.getPriority() <= 0 ? "" : MESSAGES.priority(enrollment.getPriority())));
			if (hasAlternative)
				line.add(new Label(enrollment.getAlternative(), false));
			if (hasCamp)
				line.add(new ACM(enrollment.getStudent().getCampuses()));
			if (hasArea) {
				line.add(new ACM(enrollment.getStudent().getAreas()));
				line.add(new ACM(enrollment.getStudent().getClassifications()));
			}
			if (hasDeg)
				line.add(new ACM(enrollment.getStudent().getDegrees()));
			if (hasProg)
				line.add(new ACM(enrollment.getStudent().getPrograms()));
			if (hasMajor)
				line.add(new ACM(enrollment.getStudent().getMajors()));
			if (hasConc)
				line.add(new ACM(enrollment.getStudent().getConcentrations()));
			if (hasMinor)
				line.add(new ACM(enrollment.getStudent().getMinors()));
			if (hasGroup)
				line.add(new Groups(enrollment.getStudent().getGroups()));
			for (String type: groupTypes)
				line.add(new Groups(enrollment.getStudent().getGroups(type)));
			if (hasAcmd)
				line.add(new ACM(enrollment.getStudent().getAccommodations()));
			if (hasReservation)
				line.add(new HTML(enrollment.getReservation() == null ? "&nbsp;" : enrollment.getReservation(), false));
			if (!subparts.isEmpty()) {
				if (!enrollment.hasClasses()) {
					line.add(new WarningLabel(enrollment.isWaitList() && enrollment.getStudent().getWaitListMode() == WaitListMode.WaitList ? MESSAGES.courseWaitListed() : MESSAGES.courseNotEnrolled(), subparts.size()));
				} else for (String subpart: subparts) {
					line.add(new HTML(enrollment.getClasses(subpart, ", ", suffix), false));
				}
			}
			if (hasCritical) {
				WebTable.IconCell i;
				if (enrollment.isCritical()) {
					i = new WebTable.IconCell(RESOURCES.requestsCritical(), MESSAGES.descriptionRequestCritical(), MESSAGES.opSetCritical());
				} else if (enrollment.isImportant()) {
					i = new WebTable.IconCell(RESOURCES.requestsImportant(), MESSAGES.descriptionRequestImportant(), MESSAGES.opSetImportant());
				} else if (enrollment.isVital()) {
					i = new WebTable.IconCell(RESOURCES.requestsVital(), MESSAGES.descriptionRequestVital(), MESSAGES.opSetVital());
				} else if (enrollment.isLC()) {
					i = new WebTable.IconCell(RESOURCES.requestsLC(), MESSAGES.descriptionRequestLC(), MESSAGES.opSetLC());
				} else if (enrollment.isVisitingLC()) {
					i = new WebTable.IconCell(RESOURCES.requestsVisitingF2F(), MESSAGES.descriptionRequestVisitingF2F(), MESSAGES.opSetVisitingF2F());
				} else {
					i = new WebTable.IconCell(RESOURCES.requestsNotCritical(), MESSAGES.descriptionRequestNotCritical(), MESSAGES.opSetNotCritical());
				}
				line.add(i.getWidget());
			}
			if (hasRequestedDate)
				line.add(new HTML(enrollment.getRequestedDate() == null ? "&nbsp;" : sDF.format(enrollment.getRequestedDate()), false));
			if (hasEnrolledDate)
				line.add(new HTML(enrollment.getEnrolledDate() == null ? "&nbsp;" : sDF.format(enrollment.getEnrolledDate()), false));
			if (hasWaitlistedDate)
				line.add(new HTML(enrollment.hasWaitListedDate() ? sTSF.format(enrollment.getWaitListedDate()) : "&nbsp;", false));
			if (hasWaitListReplacement)
				line.add(new HTML(enrollment.hasWaitListedReplacement() ? enrollment.getWaitListReplacement() : "&nbsp;", false));
			if (hasWaitListedPosition)
				line.add(new HTML(enrollment.hasWaitListedPosition() ? enrollment.getWaitListedPosition() : "&nbsp;", false));
			if (hasMessage)
				line.add(new HTML(enrollment.hasEnrollmentMessage() ? enrollment.getEnrollmentMessage().replace("\n", "<br>") : "&nbsp;", true));
			if (hasAdvisor)
				line.add(new HTML(enrollment.getStudent().hasAdvisor() ? enrollment.getStudent().getAdvisor("<br>") : "&nbsp;", true));
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
		}
		
		List<Widget> footer = new ArrayList<Widget>();
		if (enrolled > 0)
			footer.add(new TotalLabel(MESSAGES.totalEnrolled(enrolled), header.size()));
		if (waitlisted > 0)
			footer.add(new TotalLabel(MESSAGES.totalWaitListed(waitlisted), header.size()));
		if (unassigned > 0)
			footer.add(new TotalLabel(MESSAGES.totalNotEnrolled(unassigned + waitlisted), header.size()));
		
		if (showFilter) {
			FilterRow filter = new FilterRow(header.size());
			filter.add(new Label(MESSAGES.filter()));
			final ListBox box = new ListBox();
			for (int i = 0; i < SectioningCookie.EnrollmentFilter.values().length; i++) {
				SectioningCookie.EnrollmentFilter x = SectioningCookie.EnrollmentFilter.values()[i];
				box.addItem(CONSTANTS.enrollmentFilterValues()[i], x.name());
			}
			box.setSelectedIndex(f.ordinal());
			box.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent event) {
					iHeader.setCollapsible(true);
					SectioningCookie.getInstance().setEnrollmentCoursesDetails(true);
					SectioningCookie.getInstance().setEnrollmentFilter(SectioningCookie.EnrollmentFilter.valueOf(box.getValue(box.getSelectedIndex())));
					clear();
					populate(enrollments, courseIdsCanApprove);
				}
			});
			filter.add(box);
			footer.add(filter);
			
			if (iEnrollments.getRowCount() == 1) {
				if (f == SectioningCookie.EnrollmentFilter.ALL)
					iEnrollments.addRow(null, new EmptyLabel(MESSAGES.offeringHasNoEnrollments(), Math.max(header.size(), footer.size())));
				else
					iEnrollments.addRow(null, new EmptyLabel(MESSAGES.offeringHasNoEnrollmentsOfType(CONSTANTS.enrollmentFilterValues()[f.ordinal()]), Math.max(header.size(), footer.size())));
			}
		}
		
		if (!footer.isEmpty()) {
			if (footer.size() > 1) {
				int span = Math.max(1, header.size() / footer.size());
				for (Widget w: footer)
					((SetColSpan)w).setColSpan(span);
				((SetColSpan)footer.get(footer.size() - 1)).setColSpan(Math.max(1, header.size() - (footer.size() - 1) * span));
			}
			int row = iEnrollments.addRow(null, footer);
			
			if (showFilter)
				iEnrollments.getCellFormatter().setHorizontalAlignment(row, footer.size() - 1, HasHorizontalAlignment.ALIGN_RIGHT);
		}
		
		if (showFilter) {
			for (int i = 0; i < SectioningCookie.EnrollmentFilter.values().length; i++) {
				final SectioningCookie.EnrollmentFilter x = SectioningCookie.EnrollmentFilter.values()[i];
				Operation op = new Operation() {
					@Override
					public void execute() {
						SectioningCookie.getInstance().setEnrollmentFilter(x);
						clear();
						populate(enrollments, courseIdsCanApprove);
					}
					
					@Override
					public boolean isApplicable() {
						return true;
					}
					
					@Override
					public boolean hasSeparator() {
						return x.ordinal() == 0;
					}
					
					@Override
					public String getName() {
						return MESSAGES.enrollmentTableFilter(CONSTANTS.enrollmentFilterValues()[x.ordinal()]);
					}
				};
				for (UniTimeTableHeader h: header)
					h.addOperation(op);
			}
		}
		
		if (SectioningCookie.getInstance().getEnrollmentSortBy() != 0) {
			boolean asc = SectioningCookie.getInstance().getEnrollmentSortBy() > 0;
			EnrollmentComparator.SortBy sort = EnrollmentComparator.SortBy.values()[Math.abs(SectioningCookie.getInstance().getEnrollmentSortBy()) - 1];
			String group = SectioningCookie.getInstance().getEnrollmentSortByGroup();
			UniTimeTableHeader h = null;
			switch (sort) {
			case ACCOMODATION: h = hAccmd; break;
			case ALTERNATIVE: h = hAlternative; break;
			case APPROVED: h = hApproved; break;
			case AREA: h = hArea; break;
			case CLASSIFICATION: h = hClasf; break;
			case CONFLICT_DATE: h = hConflictDate; break;
			case CONFLICT_NAME: h = hConflictName; break;
			case CONFLICT_ROOM: h = hConflictRoom; break;
			case CONFLICT_TIME: h = hConflictTime; break;
			case CONFLICT_TYPE: h = hConflictType; break;
			case COURSE: h = hCourse; break;
			case ENROLLMENT_TS: h = hEnrollmentTS; break;
			case EXTERNAL_ID: h = hExtId; break;
			case GROUP:
				if (group == null || group.isEmpty())
					h = hGroup;
				else
					h = hGroups.get(group);
				break;
			case MAJOR: h = hMajor; break;
			case MESSAGE: h = hMessage; break;
			case PRIORITY: h = hPriority; break;
			case REQUEST_TS: h = hRequestTS; break;
			case RESERVATION: h = hReservation; break;
			case STUDENT: h = hStudent; break;
			case CONCENTRATION: h = hConc; break;
			case MINOR: h = hMinor; break;
			case ADVISOR: h = hAdvisor; break;
			case DEGREE: h = hDegree; break;
			case PROGRAM: h = hProgram; break;
			case CAMPUS: h = hCampus; break;
			case WAITLIST_TS: h = hWaitlistTS; break;
			case WAITLIST_POS: h = hWaitlistPOS; break;
			case WAITLIST_REPLACE: h = hWaitlistREP; break;
			case CRITICAL: h = hCritical; break;
			}
			if (h != null)
				iEnrollments.sort(h, new EnrollmentComparator(sort, group), asc);
		} else {
			String subpart = SectioningCookie.getInstance().getEnrollmentSortBySubpart();
			if (subpart != null && !subpart.isEmpty()) {
				boolean asc = !subpart.startsWith("-");
				UniTimeTableHeader h = hSubparts.get(asc ? subpart : subpart.substring(1));
				if (h != null)
					iEnrollments.sort(h, new EnrollmentComparator(asc ? subpart : subpart.substring(1), SectioningCookie.getInstance().getShowClassNumbers()), asc);
			}
		}
	}
	
	private static interface SetColSpan extends HasColSpan {
		public void setColSpan(int colSpan);
	}
	
	private static class TotalLabel extends HTML implements SetColSpan, HasStyleName {
		private int iColSpan;
		
		public TotalLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public void setColSpan(int colSpan) {
			iColSpan = colSpan;
			
		}
		
		@Override
		public String getStyleName() {
			return "unitime-TotalRow";
		}
		
	}
	
	private static class WarningLabel extends HTML implements SetColSpan, HasStyleName {
		private int iColSpan;
		
		public WarningLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}
		
		@Override
		public void setColSpan(int colSpan) {
			iColSpan = colSpan;
			
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
	
	private static class EmptyLabel extends HTML implements SetColSpan, HasStyleName {
		private int iColSpan;
		
		public EmptyLabel(String text, int colspan) {
			super(text, false);
			iColSpan = colspan;
		}
		
		@Override
		public void setColSpan(int colSpan) {
			iColSpan = colSpan;
			
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public String getStyleName() {
			return "text-gray";
		}
		
	}
	
	private static class Groups extends P {
		private Groups(Collection<ClassAssignmentInterface.Group> groups) {
			if (groups != null && !groups.isEmpty()) {
				for (ClassAssignmentInterface.Group group: groups) {
					P g = new P(); g.setText(group.getName());
					if (group.hasTitle()) g.setTitle(group.getTitle());
					add(g);
				}
			}
		}
	}
	
	private static class ACM extends P {
		private ACM(Collection<ClassAssignmentInterface.CodeLabel> groups) {
			if (groups != null && !groups.isEmpty()) {
				for (ClassAssignmentInterface.CodeLabel group: groups) {
					P g = new P();
					if (group.hasCode())
						g.setText(group.getCode());
					else
						g.setHTML("&nbsp;");
					if (group.hasLabel()) g.setTitle(group.getLabel());
					add(g);
				}
				getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
			}
		}
	}
	
	private static class FilterRow extends HorizontalPanel implements SetColSpan, HasStyleName {
		private int iColSpan;
		
		public FilterRow(int colspan) {
			super();
			iColSpan = colspan;
		}

		@Override
		public void setColSpan(int colSpan) {
			iColSpan = colSpan;
			
		}

		@Override
		public int getColSpan() {
			return iColSpan;
		}
		
		@Override
		public String getStyleName() {
			return "unitime-TotalRow";
		}
		
	}
	
	public void insert(final RootPanel panel) {
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		if (iOfferingId >= 0 && iShowFilter)
			iHeader.setHeaderTitle(MESSAGES.studentsTable());
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
	
	public EnrollmentTable forOfferingId(Long offeringId) {
		iOfferingId = offeringId;
		if (iOfferingId >= 0 && iShowFilter)
			iHeader.setHeaderTitle(MESSAGES.studentsTable());
		if (SectioningCookie.getInstance().getEnrollmentCoursesDetails()) {
			refresh();
		} else {
			clear();
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		return this;
	}
	
	public EnrollmentTable forClassId(Long classId) {
		iOfferingId = - classId;
		if (iOfferingId >= 0 && iShowFilter)
			iHeader.setHeaderTitle(MESSAGES.studentsTable());
		if (SectioningCookie.getInstance().getEnrollmentCoursesDetails()) {
			refresh();
		} else {
			clear();
			iHeader.clearMessage();
			iHeader.setCollapsible(false);
		}
		return this;
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
	
	protected void addSortOperation(final UniTimeTableHeader header, final EnrollmentComparator.SortBy sort, final String column) {
		addSortOperation(header, sort, column, "");
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final EnrollmentComparator.SortBy sort, final String column, final String type) {
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				iEnrollments.sort(header, new EnrollmentComparator(sort, type));
				SectioningCookie.getInstance().setEnrollmentSortBy(header.getOrder() ? 1 + sort.ordinal() : -1 - sort.ordinal(), type);
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
				if (sort == EnrollmentComparator.SortBy.GROUP && type != null && !type.isEmpty())
					return MESSAGES.sortBy(type);
				return MESSAGES.sortBy(column);
			}
		});
	}
	
	protected void addSortOperation(final UniTimeTableHeader header, final String subpart) {
		header.addOperation(new Operation() {
			@Override
			public void execute() {
				iEnrollments.sort(header, new EnrollmentComparator(subpart, SectioningCookie.getInstance().getShowClassNumbers()));
				SectioningCookie.getInstance().setEnrollmentSortBySubpart(header.getOrder() ? subpart : "-" + subpart);
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
				return MESSAGES.sortBy(subpart);
			}
		});
	}
	
	public static class EnrollmentComparator implements Comparator<Enrollment> {
		public static enum SortBy {
			EXTERNAL_ID,
			STUDENT,
			COURSE,
			PRIORITY,
			ALTERNATIVE,
			AREA,
			CLASSIFICATION,
			MAJOR,
			GROUP,
			ACCOMODATION,
			RESERVATION,
			REQUEST_TS,
			ENROLLMENT_TS,
			MESSAGE,
			CONFLICT_TYPE,
			CONFLICT_NAME,
			CONFLICT_DATE,
			CONFLICT_TIME,
			CONFLICT_ROOM,
			APPROVED,
			ADVISOR,
			MINOR,
			CONCENTRATION,
			DEGREE,
			PROGRAM,
			CAMPUS,
			WAITLIST_TS,
			WAITLIST_POS,
			WAITLIST_REPLACE,
			CRITICAL,
			;
		}
		
		private SortBy iSortBy = null;
		private String iSubpart = null;
		private boolean iShowClassNumbers = true;
		private String iGroupType = null;
		
		public EnrollmentComparator(SortBy sortBy, String type) {
			iSortBy = sortBy;
			iGroupType = type;
		}
		
		public EnrollmentComparator(String subpart, boolean showClassNumbers) {
			iSubpart = subpart;
			iShowClassNumbers = showClassNumbers;
			iGroupType = null;
		}
		
		protected int doCompare(Conflict c1, Conflict c2) {
			switch (iSortBy) {
			case CONFLICT_TYPE:
				return c1.getType().compareTo(c2.getType());
			case CONFLICT_NAME:
				return c1.getName().compareTo(c2.getName());
			case CONFLICT_DATE:
				return c1.getDate().compareTo(c2.getDate());
			case CONFLICT_TIME:
				return c1.getTime().compareTo(c2.getTime());
			case CONFLICT_ROOM:
				return c1.getRoom().compareTo(c2.getRoom());
			default:
				return c1.getName().compareTo(c2.getName());
			}
		}
		
		protected int doCompare(Enrollment e1, Enrollment e2) {
			if (iSubpart != null) {
				int cmp = e1.getClasses(iSubpart, "|", iShowClassNumbers).compareTo(e2.getClasses(iSubpart, "|", iShowClassNumbers));
				if (cmp != 0) return cmp;
			}
			if (iSortBy != null) {
				int cmp = 0;
				switch (iSortBy) {
				case EXTERNAL_ID:
					return (e1.getStudent().isCanShowExternalId() ? e1.getStudent().getExternalId() : "").compareTo(e2.getStudent().isCanShowExternalId() ? e2.getStudent().getExternalId() : "");
				case STUDENT:
					return e1.getStudent().getName().compareTo(e2.getStudent().getName());
				case COURSE:
					return e1.getCourseName().compareTo(e2.getCourseName());
				case PRIORITY:
					cmp = Integer.valueOf(e1.getPriority()).compareTo(e2.getPriority());
					if (cmp != 0) return cmp;
					cmp = e1.getAlternative().compareTo(e2.getAlternative());
					if (cmp != 0) return cmp;
					cmp = -Integer.compare(e1.getCritical() == null ? 0 : e1.getCritical().intValue(), e2.getCritical() == null ? 0 : e2.getCritical().intValue());
					if (cmp != 0) return cmp;
					if (e1.hasClasses() != e2.hasClasses()) return e1.hasClasses() ? -1 : 1;
					return e1.getStudent().getName().compareTo(e2.getStudent().getName());
				case ALTERNATIVE:
					cmp = e1.getAlternative().compareTo(e2.getAlternative());
					if (cmp != 0) return cmp;
					cmp = Integer.valueOf(e1.getPriority()).compareTo(e2.getPriority());
					if (cmp != 0) return cmp;
					cmp = -Integer.compare(e1.getCritical() == null ? 0 : e1.getCritical().intValue(), e2.getCritical() == null ? 0 : e2.getCritical().intValue());
					if (cmp != 0) return cmp;
					if (e1.hasClasses() != e2.hasClasses()) return e1.hasClasses() ? -1 : 1;
					return e1.getStudent().getName().compareTo(e2.getStudent().getName());
				case AREA:
					cmp = e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
				case CLASSIFICATION:
					cmp = e1.getStudent().getClassification("|").compareTo(e2.getStudent().getClassification("|"));
					if (cmp != 0) return cmp;
					cmp = e1.getStudent().getArea("|").compareTo(e2.getStudent().getArea("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
				case MAJOR:
					cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case MINOR:
					return e1.getStudent().getMinor("|").compareTo(e2.getStudent().getMinor("|"));
				case CONCENTRATION:
					cmp = e1.getStudent().getConcentration("|").compareTo(e2.getStudent().getConcentration("|"));
					if (cmp != 0) return cmp;
					cmp = e1.getStudent().getMajor("|").compareTo(e2.getStudent().getMajor("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case DEGREE:
					cmp = e1.getStudent().getDegree("|").compareTo(e2.getStudent().getDegree("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case PROGRAM:
					cmp = e1.getStudent().getProgram("|").compareTo(e2.getStudent().getProgram("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case CAMPUS:
					cmp = e1.getStudent().getCampus("|").compareTo(e2.getStudent().getCampus("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case GROUP:
					cmp = e1.getStudent().getGroup(iGroupType, "|").compareTo(e2.getStudent().getGroup(iGroupType, "|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case ACCOMODATION:
					cmp = e1.getStudent().getAccommodation("|").compareTo(e2.getStudent().getAccommodation("|"));
					if (cmp != 0) return cmp;
					return e1.getStudent().getAreaClasf("|").compareTo(e2.getStudent().getAreaClasf("|"));
				case RESERVATION:
					if (!e1.hasReservation() || !e2.hasReservation())
						return (e1.hasReservation() ? -1 : e2.hasReservation() ? 1 : 0);
					return e1.getReservation().compareTo(e2.getReservation());
				case REQUEST_TS:
					if (!e1.hasRequestedDate() || !e2.hasRequestedDate())
						return (e1.hasRequestedDate() ? -1 : e2.hasRequestedDate() ? 1 : 0);
					return e1.getRequestedDate().compareTo(e2.getRequestedDate());
				case ENROLLMENT_TS:
					if (!e1.hasEnrolledDate() || !e2.hasEnrolledDate())
						return (e1.hasEnrolledDate() ? -1 : e2.hasEnrolledDate() ? 1 : 0);
					return e1.getEnrolledDate().compareTo(e2.getEnrolledDate());
				case MESSAGE:
					return (e1.getEnrollmentMessage() == null ? "" : e1.getEnrollmentMessage()).compareTo(e2.getEnrollmentMessage() == null ? "" : e2.getEnrollmentMessage());
				case ADVISOR:
					return e1.getStudent().getAdvisor("|").compareTo(e2.getStudent().getAdvisor("|"));
				case CONFLICT_TYPE:
				case CONFLICT_NAME:
				case CONFLICT_DATE:
				case CONFLICT_TIME:
				case CONFLICT_ROOM:
					if (e1.hasConflict()) {
						if (e2.hasConflict()) {
							Iterator<Conflict> i1 = e1.getConflicts().iterator();
							Iterator<Conflict> i2 = e2.getConflicts().iterator();
							while (i1.hasNext() && i2.hasNext()) {
								Conflict c1 = i1.next();
								Conflict c2 = i2.next();
								cmp = doCompare(c1, c2);
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
					return 0;
				case APPROVED:
					if (!e1.hasApprovedDate() || !e2.hasApprovedDate())
						return (e1.hasApprovedDate() ? -1 : e2.hasApprovedDate() ? 1 : 0);
					return e1.getApprovedDate().compareTo(e2.getApprovedDate());
				case WAITLIST_TS:
					if (!e1.hasWaitListedDate() || !e2.hasWaitListedDate())
						return (e1.hasWaitListedDate() ? -1 : e2.hasWaitListedDate() ? 1 : 0);
					return e1.getWaitListedDate().compareTo(e2.getWaitListedDate());
				case WAITLIST_POS:
					if (!e1.hasWaitListedPosition() || !e2.hasWaitListedPosition())
						return (e1.hasWaitListedPosition() ? -1 : e2.hasWaitListedPosition() ? 1 : 0);
					return NaturalOrderComparator.compare(e1.getWaitListedPosition(), e2.getWaitListedPosition());
				case WAITLIST_REPLACE:
					if (!e1.hasWaitListedReplacement() || !e2.hasWaitListedReplacement())
						return (e1.hasWaitListedReplacement() ? -1 : e2.hasWaitListedReplacement() ? 1 : 0);
					return e1.getWaitListReplacement().compareTo(e2.getWaitListReplacement());
				case CRITICAL:
					cmp = -Integer.compare(e1.getCritical() == null ? 0 : e1.getCritical().intValue(), e2.getCritical() == null ? 0 : e2.getCritical().intValue());
					if (cmp != 0) return cmp;
					cmp = Integer.valueOf(e1.getPriority()).compareTo(e2.getPriority());
					if (cmp != 0) return cmp;
					cmp = e1.getAlternative().compareTo(e2.getAlternative());
					if (cmp != 0) return cmp;
					if (e1.hasClasses() != e2.hasClasses()) return e1.hasClasses() ? -1 : 1;
					return e1.getStudent().getName().compareTo(e2.getStudent().getName());
				}
			}
			return 0;
		}

		@Override
		public int compare(Enrollment e1, Enrollment e2) {
			int cmp = doCompare(e1, e2);
			if (cmp != 0) return cmp;
			cmp = e1.getStudent().getName().compareTo(e2.getStudent().getName());
			if (cmp != 0) return cmp;
			return (e1.getStudent().getId() < e2.getStudent().getId() ? -1 : 1);
		}

	}
}
