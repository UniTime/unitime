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
package org.unitime.timetable.gwt.client.hql;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.sectioning.StudentStatusDialog;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.services.SectioningService;
import org.unitime.timetable.gwt.services.SectioningServiceAsync;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.SectioningProperties;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.gwt.shared.SavedHQLInterface.Table;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

public class ResultsTable extends UniTimeTable<String[]> {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final StudentSectioningMessages SCT_MSG = GWT.create(StudentSectioningMessages.class);
	private final SectioningServiceAsync iSectioningService = GWT.create(SectioningService.class);
	
	private UniTimeHeaderPanel iTableHeader;

	private String iFirstField;
	
	private SectioningProperties iSectioningProperties = null;
	private Set<Long> iSelectedStudentIds = new HashSet<Long>();
	private Set<StudentStatusInfo> iStates = null;
	private StudentStatusDialog iStudentStatusDialog = null;
	private Map<Long, List<CheckBox>> iStudentId2Checks = new HashMap<Long, List<CheckBox>>();
	
	private int iMaxRows = 100;
	
	public ResultsTable(UniTimeHeaderPanel header) {
		iTableHeader  = header;
		addMouseClickListener(new UniTimeTable.MouseClickListener<String[]>() {
			@Override
			public void onMouseClick(UniTimeTable.TableEvent<String[]> event) {
				if (event.getRow() > 0 && event.getData() != null) {
					if ("__Class".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "classDetail.action?cid=" + event.getData()[0]);
					else if ("__Offering".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructionalOfferingDetail.action?op=view&io=" + event.getData()[0]);
					else if ("__Subpart".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "schedulingSubpartDetail.action?ssuid=" + event.getData()[0]);
					else if ("__Room".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "rooms?back=1&id=" + event.getData()[0]);
					else if ("__Instructor".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "instructorDetail.action?instructorId=" + event.getData()[0]);
					else if ("__Exam".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "examDetail.action?examId=" + event.getData()[0]);
					else if ("__Event".equals(iFirstField))
						ToolBox.open(GWT.getHostPageBaseURL() + "events#event=" + event.getData()[0]);
					else if ("__Student".equals(iFirstField)) {
						EnrollmentTable et = new EnrollmentTable(false, true);
						et.setAdvisorRecommendations(iSectioningProperties != null && iSectioningProperties.isAdvisorCourseRequests());
						et.setEmail(iSectioningProperties != null && iSectioningProperties.isEmail());
						et.showStudentSchedule(Long.valueOf(event.getData()[0]));
					}
				}
			}
		});
		
		iSectioningService.getProperties(null, new AsyncCallback<SectioningProperties>() {
			@Override
			public void onSuccess(SectioningProperties result) {
				iSectioningProperties = result;
				iSectioningService.lookupStudentSectioningStates(new AsyncCallback<List<StudentStatusInfo>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(List<StudentStatusInfo> result) {
						iStates = new TreeSet<StudentStatusInfo>(result);
						iStudentStatusDialog = new StudentStatusDialog(iStates, new StudentStatusDialog.StudentStatusConfirmation() {
							@Override
							public boolean isAllMyStudents() {
								return false;
							}
							@Override
							public int getStudentCount() {
								return iSelectedStudentIds.size();
							}
						});
					}
				});
			}
			@Override
			public void onFailure(Throwable caught) {}
		});
	}
	
	protected void onSort(int lastSort) {
		
	}
	
	public void setMaxRows(int maxRows) { iMaxRows = maxRows; }
	public int getMaxRows() { return iMaxRows; }
	
	public String getFirstField() { return iFirstField; }
	
	@Override
	public void clearTable(int rows) {
		iSelectedStudentIds.clear();
		super.clearTable(rows);
	}
	
	public void setData(Table result) {
		clearTable();
		if (result == null || result.size() <= 1) return;
		for (int i = 0; i < result.size(); i++) {
			String[] row = result.get(i);
			List<Widget> line = new ArrayList<Widget>();
			if (i == 0) {
				iFirstField = row[0];
				for (String x: row) {
					final String name = x.replace('_', ' ').trim();
					final UniTimeTableHeader h = new UniTimeTableHeader(name, 1);
					final int col = line.size();
					h.addOperation(new UniTimeTableHeader.Operation() {
						@Override
						public void execute() {
							sort(col, new Comparator<String[]>() {
								@Override
								public int compare(String[] o1, String[] o2) {
									return SavedHQLPage.compare(o1, o2, col);
								}
							});
							onSort(h.getOrder() != null && h.getOrder() ? (1 + col) : -1 - col);
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
							return MESSAGES.opSortBy(name);
						}
					});
					line.add(h);
				}
			} else if (i <= iMaxRows) {
				for (String x: row) {
					line.add(new HTML(x == null ? "" : x.replace("\\n", "<br>")));
				}
			} else {
				break;
			}
			if (iSectioningProperties != null && "__Student".equals(iFirstField) && iSectioningProperties.isCanSelectStudent()) {
				if (i == 0) {
					UniTimeTableHeader hSelect = new UniTimeTableHeader("&otimes;", HasHorizontalAlignment.ALIGN_CENTER);
					line.set(0, hSelect);
					hSelect.setWidth("10px");
					hSelect.addAdditionalStyleName("unitime-CheckBoxColumn");
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.selectAll();
						}
						@Override
						public boolean hasSeparator() {
							return false;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() != getRowCount() - 1;
						}
						@Override
						public void execute() {
							iSelectedStudentIds.clear();
							for (int row = 0; row < getRowCount(); row++) {
								Widget w = getWidget(row, 0);
								if (w instanceof CheckBox) {
									((CheckBox)w).setValue(true);
									iSelectedStudentIds.add(Long.valueOf(getData(row)[0]));
								}
							}
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.clearAll();
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
							for (int row = 0; row < getRowCount(); row++) {
								Widget w = getWidget(row, 0);
								if (w instanceof CheckBox) {
									((CheckBox)w).setValue(false);
								}
							}
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.sendStudentEmail();
						}
						@Override
						public boolean hasSeparator() {
							return true;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iStudentStatusDialog != null && iSectioningProperties.isEmail();
						}
						@Override
						public void execute() {
							iStudentStatusDialog.sendStudentEmail(new Command() {
								@Override
								public void execute() {
									List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
									LoadingWidget.getInstance().show(MESSAGES.waitSendingEmail());
									sendEmail(studentIds.iterator(), iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), 0,
											iStudentStatusDialog.getIncludeCourseRequests(), iStudentStatusDialog.getIncludeClassSchedule(), iStudentStatusDialog.getIncludeAdvisorRequests(),
											iStudentStatusDialog.isOptionalEmailToggle());
								}
							}, iSectioningProperties.getEmailOptionalToggleCaption(), iSectioningProperties.getEmailOptionalToggleDefault());
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.massCancel();
						}
						@Override
						public boolean hasSeparator() {
							return false;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isMassCancel() && iStudentStatusDialog != null;
						}
						@Override
						public void execute() {
							iStudentStatusDialog.massCancel(new Command() {
								@Override
								public void execute() {
									UniTimeConfirmationDialog.confirmFocusNo(SCT_MSG.massCancelConfirmation(), new Command() {
										@Override
										public void execute() {
											final List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
											LoadingWidget.getInstance().show(SCT_MSG.massCanceling());
											iSectioningService.massCancel(studentIds, iStudentStatusDialog.getStatus(),
													iStudentStatusDialog.getSubject(), iStudentStatusDialog.getMessage(), iStudentStatusDialog.getCC(), new AsyncCallback<Boolean>() {
												@Override
												public void onFailure(Throwable caught) {
													LoadingWidget.getInstance().hide();
													UniTimeNotifications.error(caught);
												}

												@Override
												public void onSuccess(Boolean result) {
													LoadingWidget.getInstance().hide();
												}
											});									
										}
									});
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.reloadStudent();
						}
						@Override
						public boolean hasSeparator() {
							return true;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isReloadStudent();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(SCT_MSG.reloadingStudent());
							iSectioningService.reloadStudent(studentIds, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.info(SCT_MSG.reloadStudentSuccess());
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.requestStudentUpdate();
						}
						@Override
						public boolean hasSeparator() {
							return !iSectioningProperties.isReloadStudent();
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isRequestUpdate();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(SCT_MSG.requestingStudentUpdate());
							iSectioningService.requestStudentUpdate(studentIds, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.info(SCT_MSG.requestStudentUpdateSuccess());
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.checkOverrideStatus();
						}
						@Override
						public boolean hasSeparator() {
							return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isReloadStudent();
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isCheckStudentOverrides();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(SCT_MSG.checkingOverrideStatus());
							iSectioningService.checkStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.info(SCT_MSG.checkStudentOverridesSuccess());
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.validateStudentOverrides();
						}
						@Override
						public boolean hasSeparator() {
							return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isCheckStudentOverrides() && !iSectioningProperties.isReloadStudent();
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isValidateStudentOverrides();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(SCT_MSG.validatingStudentOverrides());
							iSectioningService.validateStudentOverrides(studentIds, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.info(SCT_MSG.validateStudentOverridesSuccess());
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.validateReCheckCriticalCourses();
						}
						@Override
						public boolean hasSeparator() {
							return !iSectioningProperties.isRequestUpdate() && !iSectioningProperties.isCheckStudentOverrides() && !iSectioningProperties.isValidateStudentOverrides() && !iSectioningProperties.isReloadStudent();
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isRecheckCriticalCourses();
						}
						@Override
						public void execute() {
							List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
							LoadingWidget.getInstance().show(SCT_MSG.recheckingCriticalCourses());
							iSectioningService.recheckCriticalCourses(studentIds, new AsyncCallback<Boolean>() {
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(caught);
								}

								@Override
								public void onSuccess(Boolean result) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.info(SCT_MSG.recheckCriticalCoursesSuccess());
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.setStudentStatus();
						}
						@Override
						public boolean hasSeparator() {
							return true;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isChangeStatus() && iStudentStatusDialog != null;
						}
						@Override
						public void execute() {
							iStudentStatusDialog.setStatus(new Command() {
								@Override
								public void execute() {
									final String statusRef = iStudentStatusDialog.getStatus();
									if ("-".equals(statusRef)) return;
									List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
									LoadingWidget.getInstance().show(SCT_MSG.changingStatusTo(statusRef));
									iSectioningService.changeStatus(studentIds, null, statusRef, new AsyncCallback<Boolean>() {

										@Override
										public void onFailure(Throwable caught) {
											LoadingWidget.getInstance().hide();
											UniTimeNotifications.error(caught);
										}

										@Override
										public void onSuccess(Boolean result) {
											LoadingWidget.getInstance().hide();
										}
									});
									
								}
							});
						}
					});
					hSelect.addOperation(new Operation() {
						@Override
						public String getName() {
							return SCT_MSG.setStudentNote();
						}
						@Override
						public boolean hasSeparator() {
							return false;
						}
						@Override
						public boolean isApplicable() {
							return iSelectedStudentIds.size() > 0 && iSectioningProperties != null && iSectioningProperties.isChangeStatus() && iStudentStatusDialog != null;
						}
						@Override
						public void execute() {
							iStudentStatusDialog.setStudentNote(new Command() {
								@Override
								public void execute() {
									LoadingWidget.getInstance().show(SCT_MSG.changingStudentNote());
									List<Long> studentIds = new ArrayList<Long>(iSelectedStudentIds);
									final String statusRef = iStudentStatusDialog.getStatus();
									final String note = iStudentStatusDialog.getNote();
									iSectioningService.changeStatus(studentIds, note, statusRef, new AsyncCallback<Boolean>() {
										@Override
										public void onFailure(Throwable caught) {
											LoadingWidget.getInstance().hide();
											UniTimeNotifications.error(caught);
										}

										@Override
										public void onSuccess(Boolean result) {
											LoadingWidget.getInstance().hide();
										}
									});
								}
							});
						}
					});
				} else {
					Toggle ch = new Toggle();
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					ch.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							event.stopPropagation();
						}
					});
					final Long sid = Long.valueOf(row[0]);
					List<CheckBox> toggles = iStudentId2Checks.get(sid);
					if (toggles == null) {
						toggles = new ArrayList<CheckBox>();
						iStudentId2Checks.put(sid, toggles);
					}
					toggles.add(ch);
					ch.setValue(iSelectedStudentIds.contains(sid));
					ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(ValueChangeEvent<Boolean> event) {
							if (event.getValue())
								iSelectedStudentIds.add(sid);
							else
								iSelectedStudentIds.remove(sid);
							if (iStudentId2Checks.get(sid).size() > 1)
								for (CheckBox ch: iStudentId2Checks.get(sid))
									ch.setValue(event.getValue());
						}
					});
					line.set(0, ch);
				}
			}
			addRow(i == 0 ? null : row, line);
		}
		if (iFirstField != null && iFirstField.startsWith("__"))
			setColumnVisible(0, iSectioningProperties != null && "__Student".equals(iFirstField) && iSectioningProperties.isCanSelectStudent());			
	}

	private void sendEmail(final Iterator<Long> studentIds, final String subject, final String message, final String cc, final int fails, final boolean courseRequests, final boolean classSchedule, final boolean advisorRequests, final Boolean toggle) {
		if (!studentIds.hasNext()) {
			LoadingWidget.getInstance().hide();
			if (fails == 0) UniTimeNotifications.info(MESSAGES.emailSent());
			return;
		}
		final Long studentId = studentIds.next();
		iSectioningService.sendEmail(null, studentId, subject, message, cc, courseRequests, classSchedule, advisorRequests, toggle, "user-reports", new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				iTableHeader.setErrorMessage(MESSAGES.failedEmail(caught.getMessage()));
				sendEmail(studentIds, subject, message, cc, fails + 1, courseRequests, classSchedule, advisorRequests, toggle);
			}
			@Override
			public void onSuccess(Boolean result) {
				sendEmail(studentIds, subject, message, cc, fails, courseRequests, classSchedule, advisorRequests, toggle);
			}
		});
	}
	
	private static class Toggle extends CheckBox implements HasStyleName {
		@Override
		public String getStyleName() {
			return "unitime-CheckBoxColumn";
		}
	}	
}
