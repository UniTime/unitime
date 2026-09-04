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
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class StatusTypesPage extends Composite {
	protected static GwtMessages MSG = GWT.create(GwtMessages.class);
	protected static CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iListHeader, iListFooter;
	private UniTimeHeaderPanel iHeader, iFooter;
	private StatusTypeInterface iStatusType;
	
	public StatusTypesPage() {
		iPanel = new SimpleForm();
		initWidget(iPanel);
		iPanel.addStyleName("unitime-StatusTypesPage");
		iListHeader = new UniTimeHeaderPanel();
		iListHeader.addButton("add", COURSE.actionAddStatusType(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem("add", false);
				editStatusType(null);
			}
		});
		iListHeader.setEnabled("add", false);
		iListFooter = iListHeader.clonePanel();

		iHeader = new UniTimeHeaderPanel(COURSE.sectEditStatusType());
		iHeader.addButton("save", COURSE.actionSaveStatusType(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateStatusType();
			}
		});
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateStatusType(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveOrUpdateStatusType();
			}
		});
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteStatusType(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				deleteStatusType();
			}
		});
		iHeader.setEnabled("delete", false);
		
		iHeader.addButton("back", COURSE.actionBackToStatusTypes(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				History.newItem(null, false);
				showStatusTypes(iStatusType == null ? null : iStatusType.getUniqueId());
			}
		});
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
	
	protected void showStatusTypes() {
		showStatusTypes(null);
	}
	
	protected void showStatusTypes(final Long uniqueId) {
		UniTimePageLabel.getInstance().setPageName(MSG.pageStatusTypes());
		iPanel.clear();
		iListHeader.setEnabled("add", false);
		iPanel.addHeaderRow(iListHeader);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new StatusTypesRequest(), new AsyncCallback<StatusTypesResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(StatusTypesResponse result) {
				LoadingWidget.getInstance().hide();
				TableWidget table = new TableWidget(result.getStatusTypesTable());
				iPanel.addRow(table);
				iPanel.addBottomRow(iListFooter);
				iListHeader.setEnabled("add", result.isCanAdd());
				if (uniqueId != null)
					for (int row = 1; row < table.getRowCount(); row ++) {
						LineInterface line = table.getData(row);
						if (line != null && uniqueId.equals(line.getId())) {
							Element el = table.getRowFormatter().getElement(row);
							ToolBox.scrollToElement(el);
							ToolBox.focusOnRow(el);
						}
					}
			}
		});		
	}
	
	protected void tokenChanged(final String token) {
		if (token == null || token.isEmpty())
			showStatusTypes();
		else if ("add".equals(token))
			editStatusType(null);
		else if (token.startsWith("up-")) {
			RPC.execute(new StatusTypeEditRequest(Operation.UP, Long.valueOf(token.substring(3))), new AsyncCallback<StatusTypeEditResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);					
				}

				@Override
				public void onSuccess(StatusTypeEditResponse result) {
					History.newItem(String.valueOf(result.getUniqueId()), false);
					showStatusTypes(result.getUniqueId());
				}
			});
		} else if (token.startsWith("dw-")) {
				RPC.execute(new StatusTypeEditRequest(Operation.DOWN, Long.valueOf(token.substring(3))), new AsyncCallback<StatusTypeEditResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(StatusTypeEditResponse result) {
						History.newItem(String.valueOf(result.getUniqueId()), false);
						showStatusTypes(result.getUniqueId());
					}
				});
		} else {
			try {
				editStatusType(Long.valueOf(token));
			} catch (NumberFormatException e) {
				showStatusTypes();
			}
		}
	}
	
	private TextBox iReference, iLabel;
	private ListBox iApplyTo;
	
	protected void editStatusType(final Long uniqueId) {
		Window.scrollTo(0, 0);
		LoadingWidget.getInstance().show(MSG.waitPlease());
		RPC.execute(new StatusTypeEditRequest(uniqueId == null ? Operation.ADD : Operation.EDIT, uniqueId), new AsyncCallback<StatusTypeEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListHeader.setErrorMessage(MSG.failedToLoadData(caught.getMessage()));
				UniTimeNotifications.error(MSG.failedToLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);				
			}

			@Override
			public void onSuccess(StatusTypeEditResponse result) {
				LoadingWidget.getInstance().hide();
				UniTimePageLabel.getInstance().setPageName(result.getUniqueId() == null ? MSG.pageAddStatusType() : MSG.pageEditStatusType());
				iHeader.setEnabled("save", result.getStatusType() == null);
				iHeader.setEnabled("update", result.getStatusType() != null);
				iHeader.setEnabled("delete", result.isCanDelete() && result.getStatusType() != null);
				iHeader.setEnabled("back", true);
				iHeader.setHeaderTitle(result.getStatusType() == null ? COURSE.sectAddStatusType() : COURSE.sectEditStatusType());
				iPanel.clear();
				iHeader.clearMessage();
				iPanel.addHeaderRow(iHeader);
				
				iStatusType = result.getStatusType();
				if (iStatusType == null) iStatusType = new StatusTypeInterface();
				
				iReference = new TextBox(); iReference.setMaxLength(20); iReference.setWidth("140px");
				if (iStatusType.hasReference()) iReference.setValue(iStatusType.getReference());
				iPanel.addRow(COURSE.fieldReference() + ":", iReference);
				iReference.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iStatusType.setReference(event.getValue());
					}
				});
				
				iLabel = new TextBox(); iLabel.setMaxLength(60); iLabel.setWidth("400px");
				if (iStatusType.hasLabel()) iLabel.setValue(iStatusType.getLabel());
				iPanel.addRow(COURSE.fieldLabel() + ":", iLabel);
				iLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						iStatusType.setLabel(event.getValue());
					}
				});
				
				iApplyTo = new ListBox();
				iApplyTo.addItem(COURSE.applyToSession(), String.valueOf(Apply.Session.toInt()));
				iApplyTo.addItem(COURSE.applyToDepartment(), String.valueOf(Apply.Department.toInt()));
				iApplyTo.addItem(COURSE.applyToExaminations(), String.valueOf(Apply.ExamStatus.toInt()));
				iApplyTo.addItem(COURSE.applyToSessionAndDepartment(), String.valueOf(Apply.Session.toInt() | Apply.Department.toInt()));
				iApplyTo.addItem(COURSE.applyToAll(), String.valueOf(Apply.Session.toInt() | Apply.Department.toInt() | Apply.ExamStatus.toInt()));
				for (int i = 0; i < iApplyTo.getItemCount(); i++)
					if (iStatusType.getApplyTo() == Integer.valueOf(iApplyTo.getValue(i))) {
						iApplyTo.setSelectedIndex(i);
						break;
					}
				iApplyTo.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						iStatusType.setApplyTo(Integer.valueOf(iApplyTo.getSelectedValue()));
					}
				});
				iPanel.addRow(COURSE.fieldApply() + ":", iApplyTo);
				
				iPanel.addHeaderRow(COURSE.sectCourseTimetabling());
				addToggleRow(Status.InstructorSurvey, COURSE.propInstructorSurvey(), COURSE.descInstructorSurvey());
				addToggleRow(Status.OwnerView, COURSE.propOwnerView(), COURSE.descOwnerView());
				addToggleRow(Status.OwnerLimitedEdit, COURSE.propOwnerLimitedEdit(), COURSE.descOwnerLimitedEdit());
				addToggleRow(Status.OwnerEdit, COURSE.propOwnerEdit(), COURSE.descOwnerEdit());
				addToggleRow(Status.ManagerView, COURSE.propManagerView(), COURSE.descManagerView());
				addToggleRow(Status.ManagerLimitedEdit, COURSE.propManagerLimitedEdit(), COURSE.descManagerLimitedEdit());
				addToggleRow(Status.ManagerEdit, COURSE.propManagerEdit(), COURSE.descManagerEdit());
				addToggleRow(Status.Audit, COURSE.propAudit(), COURSE.descAudit());
				addToggleRow(Status.Timetable, COURSE.propTimetable(), COURSE.descTimetable());
				addToggleRow(Status.Commit, COURSE.propCommit(), COURSE.descCommit());
				
				iPanel.addHeaderRow(COURSE.sectExaminationTimetabling());
				addToggleRow(Status.ExamView, COURSE.propExamView(), COURSE.descExamView());
				addToggleRow(Status.ExamEdit, COURSE.propExamEdit(), COURSE.descExamEdit());
				addToggleRow(Status.ExamTimetable, COURSE.propExamTimetable(), COURSE.descExamTimetable());
				
				iPanel.addHeaderRow(COURSE.sectStudentSectioning());
				addToggleRow(Status.StudentsPreRegister, COURSE.propRegistration(), COURSE.descRegistration());
				addToggleRow(Status.StudentsAssistant, COURSE.propAssistant(), COURSE.descAssistant());
				addToggleRow(Status.StudentsOnline, COURSE.propOnlineSectioning(), COURSE.descOnlineSectioning());
				
				iPanel.addHeaderRow(COURSE.sectEventManagement());
				addToggleRow(Status.EventManagement, COURSE.propEvents(), COURSE.descEvents());
				addToggleRow(Status.ReportClasses, COURSE.propClassSchedule(), COURSE.descClassSchedule());
				addToggleRow(Status.ReportExamsFinal, COURSE.propFinalExaminationSchedule(), COURSE.descFinalExaminationSchedule());
				addToggleRow(Status.ReportExamsMidterm, COURSE.propMidtermExaminationSchedule(), COURSE.descMidtermExaminationSchedule());
				
				iPanel.addHeaderRow(COURSE.sectOther());
				addToggleRow(Status.AllowRollForward, COURSE.propAllowRollForward(), COURSE.descAllowRollForward());
				addToggleRow(Status.AllowNoRole, COURSE.propAllowNoRole(), COURSE.descAllowNoRole());
				addToggleRow(Status.TestSession, COURSE.propTestSession(), COURSE.descTestSession());

				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected int addToggleRow(final Status status, String label, String desc) {
		CheckBox ch = new CheckBox(desc);
		ch.setValue(status.has(iStatusType.getStatus()));
		ch.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if (event.getValue())
					iStatusType.setStatus(status.set(iStatusType.getStatus()));
				else
					iStatusType.setStatus(status.reset(iStatusType.getStatus()));
			}
		});
		return iPanel.addRow(label, ch);
	}
	
	protected void saveOrUpdateStatusType() {
		if (validateStatusType()) {
			RPC.execute(new StatusTypeEditRequest(Operation.SAVE, iStatusType), new AsyncCallback<StatusTypeEditResponse>() {

				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MSG.failedToSaveData(caught.getMessage()));
					UniTimeNotifications.error(MSG.failedToSaveData(caught.getMessage()), caught);
					ToolBox.checkAccess(caught);					
				}

				@Override
				public void onSuccess(StatusTypeEditResponse result) {
					History.newItem(null, false);
					showStatusTypes(result.getStatusType() == null ? null : result.getStatusType().getUniqueId());
				}
			});
		}
	}
	
	protected boolean validateStatusType() {
		List<String> errors = new ArrayList<String>();
		if (!iStatusType.hasReference())
			errors.add(COURSE.errorRequiredField(COURSE.fieldReference()));
		if (!iStatusType.hasLabel())
			errors.add(COURSE.errorRequiredField(COURSE.fieldLabel()));
		
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

	protected void deleteStatusType() {
		UniTimeConfirmationDialog.confirm(COURSE.confirmStatusTypeDelete(), new Command() {
			@Override
			public void execute() {
				RPC.execute(new StatusTypeEditRequest(Operation.DELETE, iStatusType.getUniqueId()), new AsyncCallback<StatusTypeEditResponse>() {

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MSG.failedToDeleteData(caught.getMessage()));
						UniTimeNotifications.error(MSG.failedToDeleteData(caught.getMessage()), caught);
						ToolBox.checkAccess(caught);					
					}

					@Override
					public void onSuccess(StatusTypeEditResponse result) {
						History.newItem(null, false);
						showStatusTypes(null);
					}
				});
			}
		});
	}
	
	public static class StatusTypesRequest implements GwtRpcRequest<StatusTypesResponse>{
	}
	
	public static class StatusTypesResponse implements GwtRpcResponse {
		private TableInterface iStatusTypesTable;
		private boolean iCanAdd = false;
		
		public TableInterface getStatusTypesTable() { return iStatusTypesTable; }
		public void setStatusTypesTable(TableInterface table) { iStatusTypesTable = table; }
		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
	}
	
	public static class StatusTypeEditRequest implements GwtRpcRequest<StatusTypeEditResponse> {
		private StatusTypeInterface iStatusType;
		private Long iUniqueId = null;
		private Operation iOperation;
		
		public StatusTypeEditRequest() {}
		public StatusTypeEditRequest(Operation operation) {
			iOperation = operation;
		}
		public StatusTypeEditRequest(Operation operation, Long uniqueId) {
			iOperation = operation;
			iUniqueId = uniqueId;
		}
		public StatusTypeEditRequest(Operation operation, StatusTypeInterface statusType) {
			iOperation = operation;
			iStatusType = statusType;
			iUniqueId = (statusType == null ? null : statusType.getUniqueId());
		}
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		public StatusTypeInterface getStatusType() { return iStatusType; }
		public void setStatusType(StatusTypeInterface statusType) { iStatusType = statusType; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }
		
	}

	public static enum Operation implements IsSerializable {
		ADD, EDIT, SAVE, DELETE, UP, DOWN,
	}
	
	public static class StatusTypeInterface implements IsSerializable {
		private Long iUniqueId;
		private String iReference, iLabel;
		private int iApply = Apply.Session.toInt();
		private int iStatus = 0;
		
		public Long getUniqueId() { return iUniqueId; }
		public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }
		
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		public boolean hasReference() { return iReference != null && !iReference.isEmpty(); }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public boolean hasLabel() { return iLabel != null && !iLabel.isEmpty(); }
		
		public int getApplyTo() { return iApply; }
		public void setApplyTo(int applyTo) { iApply = applyTo; }
		
		public int getStatus() { return iStatus; }
		public void setStatus(int status) { iStatus = status; }
	}

	public static enum Status {
		ManagerView,
		ManagerEdit,
		ManagerLimitedEdit,
		OwnerView,
		OwnerEdit,
		OwnerLimitedEdit,
		Audit,
		Timetable,
		Commit,
		ExamView,
		ExamEdit,
		ExamTimetable,
		ReportExamsFinal,
		ReportExamsMidterm,
		ReportClasses,
		StudentsAssistant,
		StudentsPreRegister,
		StudentsOnline,
		TestSession,
		AllowNoRole,
		AllowRollForward,
		EventManagement,
		InstructorSurvey,
		;
		
		public int toInt() { return 1 << ordinal(); }
		public boolean has(int rights) { return (rights & toInt()) == toInt(); }
		public int set(int rights) { return has(rights) ? rights : rights + toInt(); }
		public int reset(int rights) { return has(rights) ? rights - toInt() : rights; }
	}
	
	public static enum Apply {
		Session,
		Department,
		ExamStatus,
		;
		
		public int toInt() { return 1 << ordinal(); }
		public boolean has(int apply) { return (apply & toInt()) == toInt(); }
		public int set(int apply) { return has(apply) ? apply : apply + toInt(); }
		public int reset(int apply) { return has(apply) ? apply - toInt() : apply; }
	}
	
	public static class StatusTypeEditResponse implements GwtRpcResponse {
		private StatusTypeInterface iStatusType;
		private boolean iCanDelete = false;
		
		public StatusTypeEditResponse() {}
		
		public StatusTypeInterface getStatusType() { return iStatusType; }
		public void setStatusType(StatusTypeInterface statusType) { iStatusType = statusType; }
		
		public Long getUniqueId() { return iStatusType == null ? null : iStatusType.getUniqueId(); }
				
		public boolean isCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }
	}
}
