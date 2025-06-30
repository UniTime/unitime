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
package org.unitime.timetable.gwt.client.instructor;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.Lookup;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTextBox;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorEditRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorEditResponse;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public class InstructorEditPage extends Composite {
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private InstructorEditResponse iData;
	private TextBox iExternalId;
	private TextBox iAccountName;
	private TextBox iFirstName, iMiddleName, iLastName;
	private TextBox iAcadTitle, iEmail;
	private ListBox iPosition;
	private TextArea iNote;
	private CheckBox iIgnoreTooFar;
	private Lookup iLookupDialog;
	
	public InstructorEditPage() {
		iPanel = new SimpleForm();
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		iHeader.addButton("save", COURSE.actionSaveInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(InstructorEditRequest.Operation.SAVE);
			}
		});
		iHeader.getButton("save").setTitle(COURSE.titleSaveInstructor(COURSE.accessSaveInstructor()));
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveInstructor().charAt(0));
		iHeader.setEnabled("save", false);
		
		iHeader.addButton("update", COURSE.actionUpdateInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(InstructorEditRequest.Operation.SAVE);
			}
		});
		iHeader.getButton("update").setTitle(COURSE.titleUpdateInstructor(COURSE.actionUpdateInstructor()));
		iHeader.getButton("update").setAccessKey(COURSE.actionUpdateInstructor().charAt(0));
		iHeader.setEnabled("update", false);
		
		iHeader.addButton("delete", COURSE.actionDeleteInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iData.isConfirms())
					UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteInstructor(), new Command() {
						@Override
						public void execute() {
							save(InstructorEditRequest.Operation.DELETE);
						}
					});
				else
					save(InstructorEditRequest.Operation.DELETE);
			}
		});
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteInstructor(COURSE.actionDeleteInstructor()));
		iHeader.getButton("delete").setAccessKey(COURSE.actionDeleteInstructor().charAt(0));
		iHeader.setEnabled("delete", false);

		
		iHeader.addButton("lookup", COURSE.actionLookupInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String query = (iFirstName.getText() + (iMiddleName.getText().isEmpty() ? "" : " " + iMiddleName.getText()) + " " + iLastName.getText()).trim();
				if (query.isEmpty()) query = iEmail.getText();
				if (query.isEmpty()) query = iExternalId.getText();
				iLookupDialog.setQuery(query);
				iLookupDialog.center();
			}
		});
		iHeader.getButton("lookup").setTitle(COURSE.titleLookupInstructor(COURSE.accessLookupInstructor()));
		iHeader.getButton("lookup").setAccessKey(COURSE.accessLookupInstructor().charAt(0));
		iHeader.setEnabled("lookup", false);
		
		iHeader.addButton("previous", COURSE.actionPreviousInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(InstructorEditRequest.Operation.PREVIOUS);
			}
		});
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousInstructor(COURSE.accessPreviousInstructor()));
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousInstructor().charAt(0));
		iHeader.setEnabled("previous", false);
		
		iHeader.addButton("next", COURSE.actionNextInstructor(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				save(InstructorEditRequest.Operation.NEXT);
			}
		});
		iHeader.getButton("next").setTitle(COURSE.titleNextInstructor(COURSE.accessNextInstructor()));
		iHeader.getButton("next").setAccessKey(COURSE.accessNextInstructor().charAt(0));
		iHeader.setEnabled("next", false);
		
		iHeader.addButton("back", COURSE.actionBackInstructorDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iData.getInstructorId() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + iData.getInstructorId());
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "instructors");
			}
		});
		iHeader.getButton("back").setTitle(COURSE.titleBackInstructorDetail(COURSE.accessBackInstructorDetail()));
		iHeader.getButton("back").setAccessKey(COURSE.accessBackInstructorDetail().charAt(0));
		
		iFooter = iHeader.clonePanel();
		
		initWidget(iPanel);
		
		iExternalId = new UniTimeTextBox(); iExternalId.setMaxLength(40); iExternalId.setWidth("100px");
		iExternalId.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setExternalId(e.getValue());
			}
		});
		iAccountName = new TextBox(); iAccountName.setMaxLength(20); iAccountName.setWidth("100px");
		iAccountName.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setCareerAcct(e.getValue());
			}
		});
		iFirstName = new TextBox(); iFirstName.setMaxLength(100); iFirstName.setWidth("200px");
		iFirstName.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setFirstName(e.getValue());
			}
		});
		iMiddleName = new TextBox(); iMiddleName.setMaxLength(100); iMiddleName.setWidth("200px");
		iMiddleName.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setMiddleName(e.getValue());
			}
		});
		iLastName = new TextBox(); iLastName.setMaxLength(100); iLastName.setWidth("200px");
		iLastName.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setLastName(e.getValue());
			}
		});
		iAcadTitle = new TextBox(); iAcadTitle.setMaxLength(50); iAcadTitle.setWidth("100px");
		iAcadTitle.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setAcademicTitle(e.getValue());
			}
		});
		iEmail = new TextBox(); iEmail.setMaxLength(200); iEmail.setWidth("300px");
		iEmail.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setEmail(e.getValue());
			}
		});
		iPosition = new ListBox();
		iPosition.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				String id = iPosition.getSelectedValue();
				if (id == null || id.isEmpty())
					iData.setPositionId(null);
				else
					iData.setPositionId(Long.valueOf(id));
			}
		});
		iNote = new TextArea();
		iNote.setStyleName("unitime-TextArea");
		iNote.setHeight("75px");
		iNote.setWidth("500px");
		iNote.getElement().setAttribute("maxlength", "2048");
		iNote.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> e) {
				iData.setNote(e.getValue());
			}
		});
		iIgnoreTooFar = new CheckBox();
		iIgnoreTooFar.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> e) {
				iData.setIgnoteTooFar(e.getValue());
			}
		});
		
		iLookupDialog = new Lookup();
		iLookupDialog.addValueChangeHandler(new ValueChangeHandler<PersonInterface>() {
			@Override
			public void onValueChange(ValueChangeEvent<PersonInterface> event) {
				PersonInterface person = event.getValue();
				if (person != null) {
					iFirstName.setValue(person.getFirstName() == null ? "" : person.getFirstName(), true);
					iMiddleName.setValue(person.getMiddleName() == null ? "" : person.getMiddleName(), true);
					iLastName.setValue(person.getLastName() == null ? "" : person.getLastName(), true);
					iAcadTitle.setValue(person.getAcademicTitle() == null ? "" : person.getAcademicTitle(), true);
					iExternalId.setValue(person.getId() == null ? "" : person.getId(), true);
					iEmail.setValue(person.getEmail() == null ? "" : person.getEmail(), true);
				}
			}
		});
	
		String id = History.getToken();
		if (id == null || id.isEmpty())
			id = Window.Location.getParameter("id");
		if (id == null || id.isEmpty())
			id = Window.Location.getParameter("instructorId");
		if (id == null || id.isEmpty()) {
			String deptId = Window.Location.getParameter("departmentId");
			load(null, deptId == null || deptId.isEmpty() ? null : Long.valueOf(deptId));
		} else {
			load(Long.valueOf(id), null);
		}

		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				String token = event.getValue();
				if (token != null && !token.isEmpty())
					load(Long.valueOf(token), null);
				else
					load(Long.valueOf(Window.Location.getParameter("instructorId")), null);
			}
		});
	}
	
	protected void save(InstructorEditRequest.Operation op) {
		iHeader.clearMessage();
		InstructorEditRequest request = new InstructorEditRequest();
		request.setInstructorId(iData.getInstructorId());
		request.setDepartmentId(iData.getDepartmentId());
		request.setOperation(op);
		if (op != InstructorEditRequest.Operation.DELETE) {
			request.setData(iData);
			if (iData.getLastName() == null || iData.getLastName().isEmpty()) {
				iHeader.setErrorMessage(COURSE.errorRequiredLastName());
				return;
			}
		}
		if (op == InstructorEditRequest.Operation.DELETE)
			LoadingWidget.getInstance().show(MESSAGES.waitDeletingRecord());
		else
			LoadingWidget.getInstance().show(MESSAGES.waitSavingRecord());
		RPC.execute(request, new AsyncCallback<InstructorEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(InstructorEditResponse response) {
				LoadingWidget.getInstance().hide();
				if (response == null) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructors");
				} else if (op == InstructorEditRequest.Operation.SAVE) {
					ToolBox.open(GWT.getHostPageBaseURL() + "instructor?id=" + response.getInstructorId());
				} else {
					populate(response);
				}
			}
		});
	}
	
	protected void load(Long instructorId, Long departmentId) {
		InstructorEditRequest request = new InstructorEditRequest();
		request.setInstructorId(instructorId);
		request.setDepartmentId(departmentId);
		request.setOperation(InstructorEditRequest.Operation.GET);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<InstructorEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(InstructorEditResponse response) {
				LoadingWidget.getInstance().hide();
				populate(response);
			}
		});
	}
	
	protected void populate(InstructorEditResponse response) {
		iData = response;
		iPanel.clear();
		iPanel.addHeaderRow(iHeader);
		
		iExternalId.setValue(iData.getExternalId() == null ? "" : iData.getExternalId());
		iPanel.addRow(COURSE.propertyExternalId(), iExternalId);
		iExternalId.setReadOnly(!response.isCanEditExternalId());
		
		iAccountName.setValue(iData.getCareerAcct() == null ? "" : iData.getCareerAcct());
		iPanel.addRow(COURSE.propertyAccountName(), iAccountName);
		
		iFirstName.setValue(iData.getFirstName() == null ? "" : iData.getFirstName());
		iPanel.addRow(COURSE.propertyFirstName(), iFirstName);
		
		iMiddleName.setValue(iData.getMiddleName() == null ? "" : iData.getMiddleName());
		iPanel.addRow(COURSE.propertyMiddleName(), iMiddleName);
		
		iLastName.setValue(iData.getLastName() == null ? "" : iData.getLastName());
		iPanel.addRow(COURSE.propertyLastName(), iLastName);
		
		iAcadTitle.setValue(iData.getAcademicTitle() == null ? "" : iData.getAcademicTitle());
		iPanel.addRow(COURSE.propertyAcademicTitle(), iAcadTitle);
		
		iEmail.setValue(iData.getEmail() == null ? "" : iData.getEmail());
		iPanel.addRow(COURSE.propertyFirstName(), iEmail);
		
		iPanel.addRow(COURSE.propertyDepartment(), new Label(iData.getDepartment()));
		
		iPosition.clear();
		if (iData.hasPositions()) {
			iPosition.addItem(COURSE.valueNotSet(),"");
			for (IdLabel position: iData.getPositions()) {
				iPosition.addItem(position.getLabel(), position.getId().toString());
				if (position.getId().equals(iData.getPositionId()))
					iPosition.setSelectedIndex(iPosition.getItemCount() - 1);
			}
			iPanel.addRow(COURSE.propertyInstructorPosition(), iPosition);
		} else {
			iData.setPositionId(null);
			iPosition = null;
		}
		
		iNote.setText(iData.getNote() == null ? "" : iData.getNote());
		iPanel.addRow(COURSE.propertyNote(), iNote);
		
		iIgnoreTooFar.setValue(iData.isIgnoreTooFar());
		iPanel.addRow(COURSE.propertyIgnoreTooFar(), iIgnoreTooFar);
		
		iPanel.addBottomRow(iFooter);
		
		iHeader.setEnabled("delete", iData.isCanDelete());
		iHeader.setEnabled("previous", iData.hasPrevious());
		iHeader.setEnabled("next", iData.hasNext());
		iHeader.setEnabled("save", iData.getInstructorId() == null);
		iHeader.setEnabled("update", iData.getInstructorId() != null);
		iHeader.setEnabled("back", true);
		iHeader.setEnabled("lookup", true);
		if (iData.getInstructorId() != null && !iData.getInstructorId().toString().equals(Window.Location.getParameter("instructorId")))
			History.newItem(iData.getInstructorId().toString(), false);
		UniTimePageLabel.getInstance().setPageName(iData.getInstructorId() == null ? MESSAGES.pageAddInstructor() : MESSAGES.pageEditInstructor());
		UniTimeNavigation.getInstance().refresh();
	}
}
