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
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionEditRequest.Operation;
import org.unitime.timetable.gwt.client.admin.HolidayDatesSelector.SelectedDate;
import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.NaturalOrderComparator;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class AcademicSessionsPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private UniTimeHeaderPanel iHeader, iFooter;
	private AcademicSessionInterface iSession;
	private List<SelectedDate> iLastDates;
	private int iNrExcessDays = 0;
	
	public AcademicSessionsPage() {
		iPanel = new SimpleForm(3);
		initWidget(iPanel);
		iPanel.addStyleName("unitime-AcademicSessionsPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddAcademicSession(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editSession(null);
			}
		});
		iListHeader.getButton("add").setAccessKey(COURSE.accessAddAcademicSession().charAt(0));
		iListHeader.getButton("add").setTitle(COURSE.titleAddAcademicSession(COURSE.accessAddAcademicSession()));
		iListHeader.setEnabled("add", false);
		iListFooter = iListHeader.clonePanel();

		iHeader = new UniTimeHeaderPanel(COURSE.sectEditAcademicSession());
		iHeader.addButton("save", COURSE.actionSaveAcademicSession(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSession();
			}
		});
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveAcademicSession().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveAcademicSession(COURSE.accessSaveAcademicSession()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateAcademicSession(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateSession();
			}
		});
		iHeader.getButton("update").setAccessKey(COURSE.accessUpdateAcademicSession().charAt(0));
		iHeader.getButton("update").setTitle(COURSE.titleUpdateAcademicSession(COURSE.accessUpdateAcademicSession()));
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteAcademicSession(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteSession();
			}
		});
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteAcademicSession().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteAcademicSession(COURSE.accessDeleteAcademicSession()));
		iHeader.setEnabled("delete", false);
		
		iHeader.addButton("back", COURSE.actionBackToAcademicSessions(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showSessions(iSession == null ? null : iSession.getSessionId());
			}
		});
		iHeader.getButton("back").setAccessKey(COURSE.accessBackToAcademicSessions().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackToAcademicSessions(COURSE.accessBackToAcademicSessions()));
		iHeader.setEnabled("back", false);
		
		iFooter = iHeader.clonePanel("");
		
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				tokenChanged(event.getValue());
			}
		});
		tokenChanged(History.getToken());
	}
	
	protected void showSessions() {
		showSessions(null);
	}
	
	protected void showSessions(final Long sessionId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageAcademicSessions());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new AcademicSessionsRequest(), new AsyncCallback<AcademicSessionsResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(AcademicSessionsResponse result) {
				LoadingWidget.getInstance().hide();
				TableWidget table = new TableWidget(result.getSessionsTable());
				iPanel.addRow(table);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setEnabled("add", result.isCanAdd());
				if (sessionId != null)
					for (int row = 1; row < table.getRowCount(); row ++) {
						LineInterface line = table.getData(row);
						if (line != null && sessionId.equals(line.getId())) {
							Element el = table.getRowFormatter().getElement(row);
							ToolBox.scrollToElement(el);
							ToolBox.focusOnRow(el);
						}
					}
			}
		});		
	}
	
	protected void tokenChanged(String token) {
		if (token == null || token.isEmpty())
			showSessions();
		else if ("add".equals(token))
			editSession(null);
		else {
			try {
				editSession(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showSessions();
			}
		}
	}
	
	private TextBox iInitiative, iTerm;
	private NumberBox iYear, iWkEnroll, iWkChange, iWkDrop;
	private ListBox iDatePattern, iSessionStatus, iClassDuration, iStudentStatus, iInstructionalMethod;
	private SingleDateSelector iSessionStart, iClassEnd, iExamStart, iSessionEnd, iEventStart, iEventEnd, iNotificationStart, iNotificationEnd;
	private HolidayDatesSelector iHolidays;
	
	protected void editSession(final Long sessionId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new AcademicSessionEditRequest(sessionId == null ? Operation.ADD : Operation.EDIT, sessionId), new AsyncCallback<AcademicSessionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(AcademicSessionEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getSession() == null ? MSG.pageAddAcademicSession() : MSG.pageEditAcademicSession());
				iHeader.setEnabled("save", result.getSession() == null);
				iHeader.setEnabled("update", result.getSession() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getSession() != null);
				iHeader.setEnabled("back", true);
				iHeader.setHeaderTitle(result.getSession() == null ? COURSE.sectAddAcademicSession() : COURSE.sectEditAcademicSession());
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iSession = result.getSession();
				iNrExcessDays = result.getNrExcessDays();
				if (iSession == null) iSession = new AcademicSessionInterface();
				
				iInitiative = new TextBox(); iInitiative.setMaxLength(20); iInitiative.setWidth("140px");
				if (iSession.hasInitiative()) iInitiative.setValue(iSession.getInitiative());
				iPanel.addRow(COURSE.columnAcademicInitiative() + ":", iInitiative);
				iInitiative.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setInitiative(event.getValue());
					}
				});
				
				iTerm = new TextBox(); iTerm.setMaxLength(20); iTerm.setWidth("140px");
				if (iSession.hasTerm()) iTerm.setValue(iSession.getTerm());
				iPanel.addRow(COURSE.columnAcademicTerm() + ":", iTerm);
				iTerm.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setTerm(event.getValue());
					}
				});
				
				iYear = new NumberBox(); iYear.setMaxLength(4); iYear.setDecimal(false); iYear.setNegative(false); iYear.setWidth("40px");
				if (iSession.hasYear()) iYear.setText(iSession.getYear());
				iPanel.addRow(COURSE.columnAcademicYear() + ":", iYear);
				iYear.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setYear(event.getValue());
					}
				});
				
				if (result.hasDatePatterns()) {
					iDatePattern = new ListBox();
					iDatePattern.addItem(COURSE.itemSelect(), "");
					for (IdLabel item: result.getDatePatterns()) {
						iDatePattern.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iSession.getDefaultDatePatternId()))
							iDatePattern.setSelectedIndex(iDatePattern.getItemCount() - 1);
					}
					iDatePattern.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (iDatePattern.getSelectedIndex() == 0)
								iSession.setDefaultDatePatternId(null);
							else
								iSession.setDefaultDatePatternId(Long.valueOf(iDatePattern.getSelectedValue()));
						}
					});
					iPanel.addRow(COURSE.columnDefaultDatePattern() + ":", iDatePattern);
				} else if (result.getSession() != null) {
					iDatePattern = null;
					iPanel.addRow(COURSE.columnDefaultDatePattern() + ":", new Label(COURSE.infoNoDatePatternsAvailable()));
				} else {
					iDatePattern = null;
				}
				
				iSessionStart = new SingleDateSelector(null, false);
				iSessionStart.setValue(iSession.getSessionStart());
				iPanel.addRow(COURSE.columnSessionStartDate() + ":", iSessionStart);
				iSessionStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setSessionStart(event.getValue());
						datesChanged();
					}
				});
				iSessionStart.getWidget().getElement().getStyle().setBorderColor("#0c0");
				iSessionStart.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);
				
				iClassEnd = new SingleDateSelector(null, false);
				iClassEnd.setValue(iSession.getClassEnd());
				iPanel.addRow(COURSE.columnClassesEndDate() + ":", iClassEnd);
				iClassEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setClassEnd(event.getValue());
						datesChanged();
					}
				});
				iClassEnd.getWidget().getElement().getStyle().setBorderColor("#c00");
				iClassEnd.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);
				
				iExamStart = new SingleDateSelector(null, false);
				iExamStart.setValue(iSession.getExamStart());
				iPanel.addRow(COURSE.columnExamStartDate() + ":", iExamStart);
				iExamStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setExamStart(event.getValue());
						datesChanged();
					}
				});
				iExamStart.getWidget().getElement().getStyle().setBorderColor("#00c");
				iExamStart.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);

				iSessionEnd = new SingleDateSelector(null, false);
				iSessionEnd.setValue(iSession.getSessionEnd());
				iPanel.addRow(COURSE.columnSessionEndDate() + ":", iSessionEnd);
				iSessionEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setSessionEnd(event.getValue());
						datesChanged();
					}
				});
				iSessionEnd.getWidget().getElement().getStyle().setBorderColor("#cc0");
				iSessionEnd.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);

				iEventStart = new SingleDateSelector(null, false);
				iEventStart.setValue(iSession.getEventStart());
				iPanel.addRow(COURSE.columnEventStartDate() + ":", iEventStart);
				iEventStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setEventStart(event.getValue());
						datesChanged();
					}
				});
				iEventStart.getWidget().getElement().getStyle().setBorderColor("#0cc");
				iEventStart.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);

				iEventEnd = new SingleDateSelector(null, false);
				iEventEnd.setValue(iSession.getEventEnd());
				iPanel.addRow(COURSE.columnClassesEndDate() + ":", iEventEnd);
				iEventEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setEventEnd(event.getValue());
						datesChanged();
					}
				});
				iEventEnd.getWidget().getElement().getStyle().setBorderColor("#c0c");
				iEventEnd.getWidget().getElement().getStyle().setBorderWidth(2, Unit.PX);
				
				iSessionStatus = new ListBox();
				iSessionStatus.addItem(COURSE.itemSelect(), "");
				if (result.hasSessionStatues())
					for (IdLabel item: result.getSessionStatues()) {
						iSessionStatus.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iSession.getSessionStatusId()))
							iSessionStatus.setSelectedIndex(iSessionStatus.getItemCount() - 1);
					}
				iSessionStatus.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						if (iSessionStatus.getSelectedIndex() == 0)
							iSession.setSessionStatusId(null);
						else
							iSession.setSessionStatusId(Long.valueOf(iSessionStatus.getSelectedValue()));
					}
				});
				iPanel.addRow(COURSE.columnSessionStatus() + ":", iSessionStatus);
				
				iClassDuration = new ListBox();
				iClassDuration.addItem(COURSE.itemDefaultClassDuration(), "");
				if (result.hasClassDurations())
					for (IdLabel item: result.getClassDurations()) {
						iClassDuration.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iSession.getDefaultClassDurationId()))
							iClassDuration.setSelectedIndex(iClassDuration.getItemCount() - 1);
					}
				iClassDuration.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						if (iClassDuration.getSelectedIndex() == 0)
							iSession.setDefaultClassDurationId(null);
						else
							iSession.setDefaultClassDurationId(Long.valueOf(iClassDuration.getSelectedValue()));
					}
				});
				iPanel.addRow(COURSE.columnDefaultClassDuration() + ":", iClassDuration);
				
				if (result.hasInstructionalMethods()) {
					iInstructionalMethod = new ListBox();
					iInstructionalMethod.addItem(COURSE.itemNoDefault(), "");
					if (result.hasClassDurations())
						for (IdLabel item: result.getInstructionalMethods()) {
							iInstructionalMethod.addItem(item.getLabel(), item.getId().toString());
							if (item.getId().equals(iSession.getInstructionalMethodId()))
								iInstructionalMethod.setSelectedIndex(iInstructionalMethod.getItemCount() - 1);
						}
					iInstructionalMethod.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							if (iInstructionalMethod.getSelectedIndex() == 0)
								iSession.setInstructionalMethodId(null);
							else
								iSession.setInstructionalMethodId(Long.valueOf(iInstructionalMethod.getSelectedValue()));
						}
					});
					iPanel.addRow(COURSE.columnDefailtInstructionalMethod() + ":", iInstructionalMethod);
				}
				
				// holidays
				iHolidays = new HolidayDatesSelector();
				iPanel.addRow(COURSE.columnHolidays() + ":", iHolidays);

				iPanel.addHeaderRow(COURSE.sectOnlineStudentSchedulingDefaultSettings());
				iWkEnroll = new NumberBox(); iWkEnroll.setMaxLength(4); iWkEnroll.setDecimal(false); iWkEnroll.setNegative(false); iWkEnroll.setWidth("40px");
				iWkEnroll.setValue(iSession.getNewEnrollmentDeadline());
				Label wkEnrollDesc = new Label(COURSE.descNewEnrollmentDeadline(), false);
				wkEnrollDesc.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
				wkEnrollDesc.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
				iPanel.addRow(COURSE.propNewEnrollmentDeadline(), iWkEnroll, wkEnrollDesc);
				iWkEnroll.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setNewEnrollmentDeadline(iWkEnroll.toInteger());
					}
				});
				
				iWkChange = new NumberBox(); iWkChange.setMaxLength(4); iWkChange.setDecimal(false); iWkChange.setNegative(false); iWkChange.setWidth("40px");
				iWkChange.setValue(iSession.getClassChangesDeadline());
				Label wkChangeDesc = new Label(COURSE.descClassChangesDeadline(), false);
				wkChangeDesc.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
				wkChangeDesc.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
				iPanel.addRow(COURSE.propClassChangesDeadline(), iWkChange, wkChangeDesc);
				iWkChange.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setClassChangesDeadline(iWkChange.toInteger());
					}
				});
				
				iWkDrop = new NumberBox(); iWkDrop.setMaxLength(4); iWkDrop.setDecimal(false); iWkDrop.setNegative(false); iWkDrop.setWidth("40px");
				iWkDrop.setValue(iSession.getCourseDropDeadline());
				Label wkDropDesc = new Label(COURSE.descCourseDropDeadline(), false);
				wkDropDesc.getElement().getStyle().setWhiteSpace(WhiteSpace.PRE_WRAP);
				wkDropDesc.getElement().getStyle().setFontStyle(FontStyle.ITALIC);
				iPanel.addRow(COURSE.propCourseDropDeadline(), iWkDrop, wkDropDesc);
				iWkDrop.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iSession.setCourseDropDeadline(iWkDrop.toInteger());
					}
				});
				
				iStudentStatus = new ListBox();
				iStudentStatus.addItem(COURSE.itemDefaultStudentStatus(), "");
				if (result.hasStudentStatuses())
					for (IdLabel item: result.getStudentStatuses()) {
						iStudentStatus.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iSession.getStudentStatusId()))
							iStudentStatus.setSelectedIndex(iStudentStatus.getItemCount() - 1);
					}
				iStudentStatus.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						if (iStudentStatus.getSelectedIndex() == 0)
							iSession.setStudentStatusId(null);
						else
							iSession.setStudentStatusId(Long.valueOf(iStudentStatus.getSelectedValue()));
					}
				});
				iPanel.addRow(COURSE.propDefaultStudentStatus(), iStudentStatus);
				
				iNotificationStart = new SingleDateSelector(null, false);
				iNotificationStart.setValue(iSession.getNotificationStart());
				iPanel.addRow(COURSE.columnNotificationsBeginDate() + ":", iNotificationStart);
				iNotificationStart.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setNotificationStart(event.getValue());
					}
				});
				
				iNotificationEnd = new SingleDateSelector(null, false);
				iNotificationEnd.setValue(iSession.getNotificationEnd());
				iPanel.addRow(COURSE.columnNotificationsEndDate() + ":", iNotificationEnd);
				iNotificationEnd.addValueChangeHandler(new ValueChangeHandler<Date>() {
					@Override
					public void onValueChange(ValueChangeEvent<Date> event) {
						iSession.setNotificationEnd(event.getValue());
					}
				});

				iPanel.addBottomRow(iFooter);
				datesChanged();
				if (iSession.hasHolidays())
					iHolidays.setPattern(iSession.getHolidays());				
			}
		});
	}
	
	protected Date first(Date... dates) {
		Date ret = null;
		for (Date d: dates) {
			if (d == null) continue;
			if (ret == null || ret.after(d))
				ret = d;
		}
		return ret;
	}
	
	protected Date last(Date... dates) {
		Date ret = null;
		for (Date d: dates) {
			if (d == null) continue;
			if (ret == null || ret.before(d))
				ret = d;
		}
		return ret;
	}
	
	@SuppressWarnings("deprecation")
	protected void check(SessionMonth m, SessionMonth.Flag f, Date d) {
		if (d == null) return;
		if (m.getMonth() == d.getMonth() && m.getYear() == 1900 + d.getYear())
			m.setFlag(d.getDate() - 1, f);
	}
	
	@SuppressWarnings("deprecation")
	protected void datesChanged() {
		validateDates(null);
		if (iHolidays.isVisible())
			iLastDates = iHolidays.getValue();
		Date fd = first(iSession.getSessionStart(), iSession.getEventStart());
		Date ld = last(iSession.getSessionEnd(), iSession.getEventEnd());
		if (fd == null || ld == null || ld.before(fd)) {
			iHolidays.setVisible(false);
		} else {
			fd = CalendarUtil.copyDate(fd);
			ld = CalendarUtil.copyDate(ld);
			CalendarUtil.addDaysToDate(fd, -iNrExcessDays);
			CalendarUtil.addDaysToDate(ld, +iNrExcessDays);
			iHolidays.setVisible(true);
			List<SessionMonth> months = new ArrayList<SessionMonth>();
			Date d = new Date(fd.getYear(), fd.getMonth(), 1);
			while (!d.after(ld)) {
				SessionMonth m = new SessionMonth(1900 + d.getYear(), d.getMonth());
				months.add(m);
				check(m, SessionMonth.Flag.SESSION_START, iSession.getSessionStart());
				check(m, SessionMonth.Flag.SESSION_END, iSession.getSessionEnd());
				check(m, SessionMonth.Flag.CLASS_END, iSession.getClassEnd());
				check(m, SessionMonth.Flag.EXAM_START, iSession.getExamStart());
				check(m, SessionMonth.Flag.EVENT_START, iSession.getEventStart());
				check(m, SessionMonth.Flag.EVENT_END, iSession.getEventEnd());
				CalendarUtil.addMonthsToDate(d, 1);
			}
			iHolidays.init(months);
			if (iLastDates != null)
				iHolidays.setValue(iLastDates);
		}
	}
	
	protected void saveOrUpdateSession() {
		if (validateSession()) {
			iSession.setHolidays(iHolidays.getPattern());
			RPC.execute(new AcademicSessionEditRequest(Operation.SAVE, iSession), new AsyncCallback<AcademicSessionEditResponse>() {

				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);					
				}

				@Override
				public void onSuccess(AcademicSessionEditResponse result) {
					History.newItem(null, false);
					showSessions(result.getSession() == null ? null : result.getSession().getSessionId());
				}
			});
		}
	}
	
	protected boolean validateSession() {
		List<String> errors = new ArrayList<String>();
		if (!iSession.hasInitiative())
			errors.add(COURSE.errorRequiredField(COURSE.columnAcademicInitiative()));
		if (!iSession.hasTerm())
			errors.add(COURSE.errorRequiredField(COURSE.columnAcademicTerm()));
		if (!iSession.hasYear())
			errors.add(COURSE.errorRequiredField(COURSE.columnAcademicYear()));
		else {
			try {
				Integer.parseInt(iSession.getYear()); 
			} catch (Exception e) {
				errors.add(COURSE.errorNotNumber(COURSE.columnAcademicYear()));
			}
		}
		
		validateDates(errors);
		
		if (iSession.getSessionStatusId() == null)
			errors.add(COURSE.errorRequiredField(COURSE.columnSessionStatus()));
		
		if (errors.isEmpty())
			iHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iHeader.setErrorMessage(message);
		}

		return errors.isEmpty();
	}

	protected void validateDates(List<String> errors) {
		if (iSession.getSessionStart() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnSessionStartDate()));
		}
		if (iSession.getSessionEnd() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnSessionEndDate()));
		}
		if (iSession.getSessionStart() != null && iSession.getSessionEnd() != null && !iSession.getSessionEnd().after(iSession.getSessionStart())) {
			iSessionEnd.setErrorHint(COURSE.errorSessionEndDateNotAfterSessionStartDate());
			if (errors != null)
				errors.add(COURSE.errorSessionEndDateNotAfterSessionStartDate());
		} else iSessionEnd.clearHint();
		if (iSession.getClassEnd() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnClassesEndDate()));
		}
		if (iSession.getSessionStart() != null && iSession.getClassEnd() != null && !iSession.getClassEnd().after(iSession.getSessionStart())) {
			iClassEnd.setErrorHint(COURSE.errorClassesEndDateNotAfterSessionStartDate());
			if (errors != null)
				errors.add(COURSE.errorClassesEndDateNotAfterSessionStartDate());
		} else iClassEnd.clearHint();
		if (iSession.getExamStart() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnExamStartDate()));
		}
		if (iSession.getEventStart() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnEventStartDate()));
		}
		if (iSession.getEventEnd() == null) {
			if (errors != null)
				errors.add(COURSE.errorNotValidDate(COURSE.columnEventStartDate()));
		}
		if (iSession.getEventStart() != null && iSession.getEventEnd() != null && !iSession.getEventStart().before(iSession.getEventEnd())) {
			iEventEnd.setErrorHint(COURSE.errorEventEndDateNotAfterEventStartDate());
			if (errors != null)
				errors.add(COURSE.errorEventEndDateNotAfterEventStartDate());
		} else iEventEnd.clearHint();
		if (errors != null) {
			Date fd = first(iSession.getSessionStart(), iSession.getEventStart());
			Date ld = last(iSession.getSessionEnd(), iSession.getEventEnd());
			if (fd != null && ld != null && CalendarUtil.getDaysBetween(fd, ld) > 366)
				errors.add(COURSE.errorSessionDatesOverAYear());
		}
		if (iSession.getNotificationStart() != null && iSession.getNotificationEnd() != null && !iSession.getNotificationStart().before(iSession.getNotificationEnd())) {
			iNotificationEnd.setErrorHint(COURSE.errorNotificationsEndDateNotAfterNotificationsStartDate());
			if (errors != null)
				errors.add(COURSE.errorNotificationsEndDateNotAfterNotificationsStartDate());
		} else iNotificationEnd.clearHint();
	}
	
	protected void deleteSession() {
		UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteAcademicSession(), new Command() {
			@Override
			public void execute() {
				RPC.execute(new AcademicSessionEditRequest(Operation.DELETE, iSession.getSessionId()), new AsyncCallback<AcademicSessionEditResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(AcademicSessionEditResponse result) {
						History.newItem(null, false);
						showSessions(null);
					}
				});
			}
		});
	}
	
	public static class AcademicSessionsRequest implements GwtRpcRequest<AcademicSessionsResponse>{
	}
	
	public static class AcademicSessionsResponse implements GwtRpcResponse {
		private TableInterface iSessionsTable;
		private boolean iCanAdd = false;
		
		public TableInterface getSessionsTable() { return iSessionsTable; }
		public void setSessionsTable(TableInterface table) { iSessionsTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class AcademicSessionEditRequest implements GwtRpcRequest<AcademicSessionEditResponse> {
		private Long iSessionId;
		private AcademicSessionInterface iSession;
		private Operation iOperation;
		
		public AcademicSessionEditRequest() {}
		public AcademicSessionEditRequest(Operation operation, Long sessionId) {
			iOperation = operation; iSessionId = sessionId;
		}
		public AcademicSessionEditRequest(Operation operation, AcademicSessionInterface session) {
			iOperation = operation;
			iSession = session;
			iSessionId = (session == null ? null : session.getSessionId());
		}
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public AcademicSessionInterface getSession() { return iSession; }
		public void setSession(AcademicSessionInterface session) { iSession = session; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
		public static enum Operation {
			ADD, EDIT, SAVE, DELETE,
		}
	}
	
	public static class AcademicSessionInterface implements IsSerializable {
		private Long iSessionId;
		private String iInitiative, iTerm, iYear;
		private Long iDefaultDatePatternId, iSessionStatusId, iDefaultClassDurationId, iStudentStatusId, iInstructionalMethodId;
		private Date iSessionStart, iClassEnd, iExamStart, iSessionEnd, iEventStart, iEventEnd, iNotificationStart, iNotificationEnd;
		private String iHolidays;
		private Integer iNewEnrollmentDeadline = 1, iClassChangesDeadline = 1, iCourseDropDeadline = 4;
		
		public Long getSessionId() { return iSessionId; }
		public void setSessionId(Long sessionId) { iSessionId = sessionId; }
		public String getInitiative() { return iInitiative; }
		public void setInitiative(String initiative) { iInitiative = initiative; }
		public boolean hasInitiative() { return iInitiative != null && !iInitiative.isEmpty(); }
		public String getTerm() { return iTerm; }
		public void setTerm(String term) { iTerm = term; }
		public boolean hasTerm() { return iTerm != null && !iTerm.isEmpty(); }
		public String getYear() { return iYear; }
		public void setYear(String year) { iYear = year; }
		public boolean hasYear() { return iYear != null && !iYear.isEmpty(); }
		
		public Long getDefaultDatePatternId() { return iDefaultDatePatternId; }
		public void setDefaultDatePatternId(Long defaultDatePatternId) { iDefaultDatePatternId = defaultDatePatternId; }
		public Long getSessionStatusId() { return iSessionStatusId; }
		public void setSessionStatusId(Long sessionStatusId) { iSessionStatusId = sessionStatusId; }
		public Long getDefaultClassDurationId() { return iDefaultClassDurationId; }
		public void setDefaultClassDurationId(Long classDurationId) { iDefaultClassDurationId = classDurationId; }
		public Long getStudentStatusId() { return iStudentStatusId; }
		public void setStudentStatusId(Long studentStatusId) { iStudentStatusId = studentStatusId; }
		public Long getInstructionalMethodId() { return iInstructionalMethodId; }
		public void setInstructionalMethodId(Long instructionalMethodId) { iInstructionalMethodId = instructionalMethodId; }
		
		public Date getSessionStart() { return iSessionStart; }
		public void setSessionStart(Date sessionStart) { iSessionStart = sessionStart; }
		public Date getClassEnd() { return iClassEnd; }
		public void setClassEnd(Date classEnd) { iClassEnd = classEnd; }
		public Date getExamStart() { return iExamStart; }
		public void setExamStart(Date examStart) { iExamStart = examStart; }
		public Date getSessionEnd() { return iSessionEnd; }
		public void setSessionEnd(Date sessionEnd) { iSessionEnd = sessionEnd; }
		public Date getEventStart() { return iEventStart; }
		public void setEventStart(Date eventStart) { iEventStart = eventStart; }
		public Date getEventEnd() { return iEventEnd; }
		public void setEventEnd(Date eventEnd) { iEventEnd = eventEnd; }
		public Date getNotificationStart() { return iNotificationStart; }
		public void setNotificationStart(Date notificationStart) { iNotificationStart = notificationStart; }
		public Date getNotificationEnd() { return iNotificationEnd; }
		public void setNotificationEnd(Date notificationEnd) { iNotificationEnd = notificationEnd; }
		
		public String getHolidays() { return iHolidays; }
		public void setHolidays(String holidays) { iHolidays = holidays; }
		public boolean hasHolidays() { return iHolidays != null && !iHolidays.isEmpty(); }
		
		public Integer getNewEnrollmentDeadline() { return iNewEnrollmentDeadline; }
		public void setNewEnrollmentDeadline(Integer deadline) { iNewEnrollmentDeadline = deadline; }
		public Integer getClassChangesDeadline() { return iClassChangesDeadline; }
		public void setClassChangesDeadline(Integer deadline) { iClassChangesDeadline = deadline; }
		public Integer getCourseDropDeadline() { return iCourseDropDeadline; }
		public void setCourseDropDeadline(Integer deadline) { iCourseDropDeadline = deadline; }
	}
	
	public static class AcademicSessionEditResponse implements GwtRpcResponse {
		private AcademicSessionInterface iSession;
		private List<IdLabel> iDatePatterns, iSessionStatues, iClassDurations, iStudentStatuses, iInstructionalMethods;
		private Integer iNrExcessDays;
		private boolean iCanDelete = false;
		
		public AcademicSessionEditResponse() {}
		
		public AcademicSessionInterface getSession() { return iSession; }
		public void setSession(AcademicSessionInterface session) { iSession = session; }
		
		public void addDatePattern(Long id, String label) {
			if (iDatePatterns == null) iDatePatterns = new ArrayList<IdLabel>();
			iDatePatterns.add(new IdLabel(id, label));
		}
		public List<IdLabel> getDatePatterns() { return iDatePatterns; }
		public boolean hasDatePatterns() { return iDatePatterns != null && !iDatePatterns.isEmpty(); }
		
		public void addSessionStatus(Long id, String label) {
			if (iSessionStatues == null) iSessionStatues = new ArrayList<IdLabel>();
			iSessionStatues.add(new IdLabel(id, label));
		}
		public List<IdLabel> getSessionStatues() { return iSessionStatues; }
		public boolean hasSessionStatues() { return iSessionStatues != null && !iSessionStatues.isEmpty(); }
		public boolean hasSessionStatus(Long id) {
			if (iSessionStatues == null || id == null) return false;
			for (IdLabel status: iSessionStatues)
				if (id.equals(status.getId())) return true;
			return false;
		}

		public void addClassDuration(Long id, String label) {
			if (iClassDurations == null) iClassDurations = new ArrayList<IdLabel>();
			iClassDurations.add(new IdLabel(id, label));
		}
		public List<IdLabel> getClassDurations() { return iClassDurations; }
		public boolean hasClassDurations() { return iClassDurations != null && !iClassDurations.isEmpty(); }
		
		public void addStudentStatus(Long id, String label) {
			if (iStudentStatuses == null) iStudentStatuses = new ArrayList<IdLabel>();
			iStudentStatuses.add(new IdLabel(id, label));
		}
		public List<IdLabel> getStudentStatuses() { return iStudentStatuses; }
		public boolean hasStudentStatuses() { return iStudentStatuses != null && !iStudentStatuses.isEmpty(); }

		public void addInstructionalMethod(Long id, String label) {
			if (iInstructionalMethods == null) iInstructionalMethods = new ArrayList<IdLabel>();
			iInstructionalMethods.add(new IdLabel(id, label));
		}
		public List<IdLabel> getInstructionalMethods() { return iInstructionalMethods; }
		public boolean hasInstructionalMethods() { return iInstructionalMethods != null && !iInstructionalMethods.isEmpty(); }
		
		public int getNrExcessDays() { return iNrExcessDays == null ? 0 : iNrExcessDays.intValue(); }
		public void setNrExcessDays(Integer nrExcessDays) { iNrExcessDays = nrExcessDays; }
		
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}
	
	public static class IdLabel implements IsSerializable, Comparable<IdLabel> {
		private Long iId;
		private String iLabel;

		public IdLabel() {}
		public IdLabel(Long id, String label) {
			iId = id; iLabel = label;
		}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public int hashCode() { return getId().hashCode(); }
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof IdValue)) return false;
			return getId().equals(((IdValue)o).getId());
		}
		
		@Override
		public int compareTo(IdLabel other) {
			return NaturalOrderComparator.compare(getLabel(), other.getLabel());
		}
		
		@Override
		public String toString() {
			return "{ id : " + iId + ", label : " + (iLabel == null ? "null" : "'" + iLabel + "'") + "}";
		}
	}

}
