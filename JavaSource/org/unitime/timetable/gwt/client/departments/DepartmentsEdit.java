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
package org.unitime.timetable.gwt.client.departments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseBoolean;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.UpdateDepartmentAction;
//import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentCheckCanDeleteRequest;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentPropertiesRequest;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.model.ExternalDepartmentStatusType;
import org.unitime.timetable.model.RefTableEntry;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.context.HttpSessionContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DepartmentsEdit extends Composite implements TakesValue<DepartmentInterface>{
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeWidget<TextBox> iName;
	private UniTimeWidget<TextBox> iAbbreviation;
	private UniTimeWidget<TextBox> iExternalManagerAbbreviation;
	private UniTimeWidget<TextBox> iExternalManagerName;
	private  UniTimeWidget<CheckBox>  iExternalManager;
	
	private UniTimeWidget<Label>   iAcademicSession ;
	private UniTimeWidget<TextBox>   iDeptCode ;
	private UniTimeWidget<ListBox>  iStatusType; 
    private ListBox  [] iCurrentDependentOptions;
    private ListBox [] iCurrentStatusTypeOptions;
    private List iDependentDepartments;
    private List iDependentStatuses;
    private List<String> iCurrentDependentDepartments = new ArrayList<String>();
    private List<String> iCurrentDependentStatuses = new ArrayList<String>();
	private FlexTable iCurrentControlDeptFlexTable;
	private VerticalPanel iCurrentControlDeptMainPanel ;

	private TextBox  iExternalId ;
	private TextBox  iDistPrefPriority ;
	private UniTimeWidget<CheckBox> iAllowReqTime;
	private UniTimeWidget<CheckBox> iAllowReqRoom;
	private UniTimeWidget<CheckBox> iAllowReqDist;
	private UniTimeWidget<CheckBox> iAllowEvents;
	private UniTimeWidget<CheckBox> iInheritInstructorPreferences;
	private UniTimeWidget<CheckBox> iAllowStudentScheduling ;
	private UniTimeWidget<CheckBox> iExternalFundingDept ;
	
	private UniTimeHeaderPanel controlDeptHeaderPanel;
	private UniTimeWidget<Button> iAddStatus ;
	private UniTimeWidget<Button> iDeleteAll;

	private VerticalPanel iControlDeptMainPanel ;
	private FlexTable iControlDeptFlexTable;
	  
	private DepartmentInterface iDepartment = null;
	private DepartmentPropertiesInterface iProperties;
	
	public DepartmentsEdit() {
		/*create the UI */
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-DepartmentEdit");
		
		iHeader = new UniTimeHeaderPanel();
	
		iHeader.addButton("save", MESSAGES.buttonSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateDepartmentRequest request = new UpdateDepartmentRequest();
				request.setAction(UpdateDepartmentAction.CREATE);
				request.setDepartment(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<DepartmentInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectDepartment(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectDepartment(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(DepartmentInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("update", MESSAGES.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateDepartmentRequest request = new UpdateDepartmentRequest();
				request.setAction(UpdateDepartmentAction.UPDATE);
				request.setDepartment(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<DepartmentInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						//Logger log = Logger.getLogger(DepartmentsPage.class.getName());
						//log.info("onFailure)");
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedUpdate(MESSAGES.objectDepartment(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedUpdate(MESSAGES.objectDepartment(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(DepartmentInterface result) {
						//Logger log = Logger.getLogger(DepartmentsPage.class.getName());
						//log.info("onSuccess)");
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("delete", MESSAGES.buttonDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmDepartmentDelete(), new Command() {
					@Override
					public void execute() {
						UpdateDepartmentRequest request = new UpdateDepartmentRequest();
						request.setAction(UpdateDepartmentAction.DELETE);
						request.setDepartment(getValue());
						LoadingWidget.getInstance().show(MESSAGES.waitPlease());
						RPC.execute(request, new AsyncCallback<DepartmentInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MESSAGES.failedDelete(MESSAGES.objectDepartment(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedDelete(MESSAGES.objectDepartment(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(DepartmentInterface result) {
								LoadingWidget.getInstance().hide();
								onBack(true, null);
							}
						});
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onBack(false, iDepartment.getId());
			}
		});
		iForm.addHeaderRow(iHeader);
		
		//AcademicSession		
		iAcademicSession = new UniTimeWidget<Label>(new Label());
		iAcademicSession.getWidget().setStyleName("unitime-Label");;	
		iForm.addRow(MESSAGES.propAcademicSession(), iAcademicSession);
		
		//DeptCode		
		iDeptCode = new UniTimeWidget<TextBox>(new TextBox());
		iDeptCode.getWidget().setStyleName("unitime-TextBox");
		iDeptCode.getWidget().setMaxLength(50);
		iDeptCode.getWidget().setWidth("200px");
		iDeptCode.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iDeptCode.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.propDeptCode(), iDeptCode);
		
		//abbrev		
		iAbbreviation = new UniTimeWidget<TextBox>(new TextBox());
		iAbbreviation.getWidget().setStyleName("unitime-TextBox");
		iAbbreviation.getWidget().setMaxLength(20);
		iAbbreviation.getWidget().setWidth("100px");
		iAbbreviation.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iAbbreviation.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.propAbbreviation(), iAbbreviation);
		
		//name
		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(100);
		iName.getWidget().setWidth("600px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.propName(), iName);

		//StatusType
		iStatusType = new UniTimeWidget<ListBox>(new ListBox());
		iStatusType.getWidget().setMultipleSelect(false);
		iStatusType.getWidget().setWidth("300px");
		iStatusType.getWidget().setStyleName("gwt-SuggestBox");
		iStatusType.getWidget().setVisibleItemCount(1);
		iForm.addRow(MESSAGES.optionDepartmentStatus(), iStatusType);
		
		//ExternalID		
		iExternalId = new TextBox();
		iExternalId.setStyleName("unitime-TextBox");
		iExternalId.setMaxLength(40);
		iExternalId.setWidth("200px");
		iForm.addRow(MESSAGES.propExternalId(), iExternalId);

		//ExternalManager		
		iExternalManager = new UniTimeWidget<CheckBox>(new CheckBox()); 
		iForm.addRow(MESSAGES.propExternalManager(), iExternalManager);

		//ExternalManagerAbbreviation		
		iExternalManagerAbbreviation	 = new UniTimeWidget<TextBox>(new TextBox());
		iExternalManagerAbbreviation.getWidget().setStyleName("unitime-TextBox");
		iExternalManagerAbbreviation.getWidget().setMaxLength(10);
		iExternalManagerAbbreviation.getWidget().setWidth("200px");
		iForm.addRow(MESSAGES.propExternalManagerAbbreviation(), iExternalManagerAbbreviation	);

		//ExternalManagerName		
		iExternalManagerName  = new UniTimeWidget<TextBox>(new TextBox());
		iExternalManagerName.getWidget().setStyleName("unitime-TextBox");
		iExternalManagerName.getWidget().setMaxLength(30);
		iExternalManagerName.getWidget().setWidth("200px");
		iForm.addRow(MESSAGES.propExternalManagerName(), iExternalManagerName);

		//DistPrefPriority		
		iDistPrefPriority = new TextBox();
		iDistPrefPriority.setStyleName("unitime-TextBox");
		iDistPrefPriority.setMaxLength(100);
		iDistPrefPriority.setWidth("100px");
		iForm.addRow(MESSAGES.propPrefPriority(), iDistPrefPriority);
		
		//AllowReqTime
		iAllowReqTime = new UniTimeWidget<CheckBox>(new CheckBox());
		iAllowReqTime.getWidget().setValue(false);		
		iForm.addRow(MESSAGES.propAllowReqTime().replace("<br>", ""), iAllowReqTime);

		//AllowReqRoom
		iAllowReqRoom = new UniTimeWidget<CheckBox>(new CheckBox());
		iAllowReqRoom.getWidget().setValue(false);	
		iForm.addRow(MESSAGES.propAllowReqRoom().replace("<br>", ""), iAllowReqRoom);

		//AllowReqDestribution
		iAllowReqDist = new UniTimeWidget<CheckBox>(new CheckBox());
		iAllowReqDist.getWidget().setValue(false);
		iForm.addRow(MESSAGES.propAllowReqDist().replace("<br>", ""), iAllowReqDist);
		
		//Instruc Pref
		iInheritInstructorPreferences = new UniTimeWidget<CheckBox>(new CheckBox());
		iInheritInstructorPreferences.getWidget().setValue(false);
		iForm.addRow(MESSAGES.propInheritInstructorPref().replace("<br>", ""), iInheritInstructorPreferences);
		
		//Allow Events
		iAllowEvents = new UniTimeWidget<CheckBox>(new CheckBox());
		iAllowEvents.getWidget().setValue(false);
		iForm.addRow(MESSAGES.propAllowEvents().replace("<br>", ""), iAllowEvents);	

		//Allow Scheduling
		iAllowStudentScheduling = new UniTimeWidget<CheckBox>(new CheckBox());
		iAllowStudentScheduling.getWidget().setValue(false);
		iForm.addRow(MESSAGES.propAllowStudentScheduling().replace("<br>", ""), iAllowStudentScheduling);	

		//External Funding Department
		iExternalFundingDept = new UniTimeWidget<CheckBox>(new CheckBox());
		iExternalFundingDept.getWidget().setValue(false);
		iForm.addRow(MESSAGES.propExternalFundingDept().replace("<br>", ""), iExternalFundingDept);	
		
		
		//Controlling Department Statuses 
		iAddStatus = new UniTimeWidget<Button>(new Button("Add Status"));		
		iDeleteAll  = new UniTimeWidget<Button>(new Button("Delete All")); 


		controlDeptHeaderPanel = new UniTimeHeaderPanel("Controlling Department Statuses");
		controlDeptHeaderPanel.insertWidget(iDeleteAll);
	    controlDeptHeaderPanel.insertWidget(iAddStatus);  
	    HTML seperator = new HTML("<hr  style=\"width:100%;\" /><br>");
	    controlDeptHeaderPanel.add(seperator);
	     
		iForm.addRow(controlDeptHeaderPanel);		

		iControlDeptMainPanel = new VerticalPanel();
		iControlDeptFlexTable = new FlexTable();
		iControlDeptMainPanel.add(iControlDeptFlexTable); 
		
	    iForm.addRow(iControlDeptMainPanel);  
	    
		iForm.addRow(new HTML("<br><br>"));
		
		iFooter = iHeader.clonePanel();
		iForm.addBottomRow(iFooter);
	  	initWidget(iForm);
	   }

	/*onBack and isAbbreviationUnique are redefined in DepartmentsPage*/	
	protected void onBack(boolean refresh, Long DepartmentId) {}
	
	protected boolean isAbbreviationUnique(DepartmentInterface Department) {
		return true;
	}

	/*
	 * Set values in UI
	 */
	@Override
	public void setValue(DepartmentInterface department) {
		Logger log = Logger.getLogger(DepartmentsEdit.class.getName());
		log.info("setValue");
		iDeptCode.getWidget().setText("");
		iAbbreviation.getWidget().setText("");
		iName.getWidget().setText("");
		iExternalId.setText("");
		iDistPrefPriority.setText("");
		iExternalManagerAbbreviation.getWidget().setText("");
		iExternalManagerName.getWidget().setText("");	
		iStatusType.getWidget().clear();
		iAbbreviation.clearHint();
		iHeader.clearMessage();
	
		iAllowReqTime.getWidget().setValue(false);	
		iAllowReqRoom.getWidget().setValue(false);	
		iAllowReqDist.getWidget().setValue(false);
		iInheritInstructorPreferences.getWidget().setValue(false);
		iAllowEvents.getWidget().setValue(false);
		iAllowStudentScheduling.getWidget().setValue(false);
		iExternalFundingDept.getWidget().setValue(false);
		
		iControlDeptFlexTable.removeAllRows();
		iControlDeptFlexTable.clear();	
		iCurrentDependentDepartments.clear();
		iCurrentDependentStatuses.clear();
		iControlDeptFlexTable.setText(0, 0, MESSAGES.propControllingDepartment());
		FlexCellFormatter cellFormatter = iControlDeptFlexTable.getFlexCellFormatter();
		cellFormatter.setStyleName(0, 0, "department-StatusItalics");
		cellFormatter.setStyleName(0, 1, "department-StatusItalics");
		
		
		if (department == null) {
			log.info("department is equal to null" );
			controlDeptHeaderPanel.setVisible(false);
			iControlDeptMainPanel.setVisible(false);
			iExternalManager.getWidget().setEnabled(true);
			iExternalManager.getWidget().setValue(false);
			iHeader.setHeaderTitle(MESSAGES.sectAddDepartment());
			iHeader.setEnabled("save", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
			iHeader.setEnabled("back", true);
			iDepartment = new DepartmentInterface();
			RPC.execute(new DepartmentPropertiesRequest(),new AsyncCallback<DepartmentPropertiesInterface>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectDepartment(), caught.getMessage()));
					UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectDepartment(), caught.getMessage()), caught);
				}
				@Override
				public void onSuccess(DepartmentPropertiesInterface result) {
					iStatusType.getWidget().addItem(MESSAGES.departmentStatusDefault(),"-1");
					for (Entry<String, String> entry : result.getSatusOptions().entrySet()) {    
						iStatusType.getWidget().addItem(entry.getValue().toString() , entry.getKey().toString());
					}
					iAcademicSession.getWidget().setText((result.getAcademicSessionName() == null ? "" : result.getAcademicSessionName()));
					if (result.isCoursesFundingDepartmentsEnabled() == false)
					iExternalFundingDept.getWidget().setEnabled(false);
				}
			});
		
		} else {
			iControlDeptFlexTable.setText(0, 1, MESSAGES.propStatusManagedBy() + ((department.getDeptCode() == null) ? "" : department.getDeptCode()) + " - " + ((department.getName() == null) ? "" : department.getName()));
			iHeader.setHeaderTitle(MESSAGES.sectEditDepartment());
			iHeader.setEnabled("save", false);
			iHeader.setEnabled("update", department.getCanEdit());
			iHeader.setEnabled("delete", department.getCanDelete());
			iHeader.setEnabled("back", true); 
			iDeptCode.getWidget().setText(department.getDeptCode() == null ? "" : department.getDeptCode());
			iAbbreviation.getWidget().setText(department.getAbbreviation() == null ? "" : department.getAbbreviation());
			iName.getWidget().setText(department.getName() == null ? "" : department.getName());
			if (department.isCanChangeExtManager() == false)
				iExternalManager.getWidget().setEnabled(false);	
								
			iAcademicSession.getWidget().setText((department.getAcademicSessionName() == null ? "" : department.getAcademicSessionName()));
			iExternalId.setText(department.getExternalId() == null ? "" : department.getExternalId());
			iDistPrefPriority.setText(department.getDistributionPrefPriority() == null ? "0" : department.getDistributionPrefPriority().toString());
			iExternalManager.getWidget().setValue(department.getExternalManager());
			iExternalManagerAbbreviation.getWidget().setValue(department.getExternalMgrAbbv());
			iExternalManagerName.getWidget().setValue(department.getExternalMgrLabel());
			
			iStatusType.getWidget().clear();
			iStatusType.getWidget().addItem(MESSAGES.departmentStatusDefault(),"-1");
			for (Entry<String, String> entry : department.getSatusOptions().entrySet()) {    
				iStatusType.getWidget().addItem(entry.getValue().toString() , entry.getKey().toString());
			}
		    if(department.getStatusTypeCode() != null){
		    	 setSelectedValue(iStatusType.getWidget(), department.getStatusTypeCode());
		    }
		    
			iAllowReqTime.getWidget().setValue(department.getAllowReqTime());
			iAllowReqRoom.getWidget().setValue(department.getAllowReqRoom());
			iAllowReqDist.getWidget().setValue(department.getAllowReqDistribution());
			iInheritInstructorPreferences.getWidget().setValue(department.getInheritInstructorPreferences());
			iAllowEvents.getWidget().setValue(department.getAllowEvents());		
			iAllowStudentScheduling.getWidget().setValue(department.getAllowStudentScheduling());
			iExternalFundingDept.getWidget().setValue(department.getExternalFundingDept());
			
			//if(department.getExternalManager().booleanValue() == false){
			if(department.isExternalManager ()== false){				
				controlDeptHeaderPanel.setVisible(false);
				iControlDeptMainPanel.setVisible(false);
				
			}else{
				controlDeptHeaderPanel.setVisible(true);
				iControlDeptMainPanel.setVisible(true);
				
			}
			
			if (department.isCoursesFundingDepartmentsEnabled() == false)
			iExternalFundingDept.getWidget().setEnabled(false);
			
			//Controlling department section
			if(department.getExternalManager()){
				for (String entry : department.getDependentDepartments()) {
					iCurrentDependentDepartments.add(entry);
				}

				iCurrentDependentStatuses.clear();
				for (String entry : department.getDependentStatuses()) {
					iCurrentDependentStatuses.add(entry);
				}			
				
				//dependent departments and their status 
				if (!iCurrentDependentDepartments.isEmpty()){
					//number of rows showing current dependent depts
					iCurrentDependentOptions = new ListBox[iCurrentDependentDepartments.size()] ;
					iCurrentStatusTypeOptions = new ListBox[iCurrentDependentDepartments.size()] ;
					iCurrentControlDeptMainPanel = new VerticalPanel();
					iCurrentControlDeptFlexTable = new FlexTable();
					//Logger log1 = Logger.getLogger(DepartmentsEdit.class.getName());
					//log1.info("iCurrentDependentDepartments.isEmpty" + iCurrentDependentDepartments.size());
					for (int i = 0; i < iCurrentDependentDepartments.size(); i++) {
						iCurrentStatusTypeOptions[i] = statusOptions(department);//new ListBox();
						iCurrentDependentOptions[i] = departmentOptions(department);//new ListBox();
						setSelectedValue(iCurrentStatusTypeOptions[i], iCurrentDependentStatuses.get(i));	
						setSelectedValue(iCurrentDependentOptions[i], iCurrentDependentDepartments.get(i));

						int numRows = iControlDeptFlexTable.getRowCount();
						iControlDeptFlexTable.setWidget(numRows, 0, iCurrentDependentOptions[i]);
						iControlDeptFlexTable.setWidget(numRows, 1, iCurrentStatusTypeOptions[i]);

						Button deleteStatusButton = new Button("Delete");
						deleteStatusButton.addClickHandler(new ClickHandler() {
							public void onClick(ClickEvent event) {
								int receiverRowIndex = iControlDeptFlexTable.getCellForEvent(event).getRowIndex();
								iControlDeptFlexTable.removeRow(receiverRowIndex);
							}
						});
						iControlDeptFlexTable.setWidget(i+1, 2, deleteStatusButton);
					}
				}

				iDeleteAll.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						deleteAllDependentDepartments();
					}
				});	

			}
			iDepartment = department;
			
			//Add row for control dept
			addNewRow(iDepartment);
			addNewRow(iDepartment); 
			iAddStatus.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					addNewRow(iDepartment);
				}
			});	
		}
	}
	
	/*
	 * Get values from UI 
	 */
	@Override
	public DepartmentInterface getValue() {
		//Logger log = Logger.getLogger(DepartmentsEdit.class.getName());
		//log.info("getValue");
		iDepartment.setName(iName.getWidget().getText());
		iDepartment.setDeptCode(iDeptCode.getWidget().getText());
		iDepartment.setAbbreviation(iAbbreviation.getWidget().getText());	
		iDepartment.setExternalId(iExternalId.getValue());
		iDepartment.setExternalManager(iExternalManager.getWidget().getValue());
		iDepartment.setExternalMgrLabel(iExternalManagerName.getWidget().getValue());
		iDepartment.setExternalMgrAbbv(iExternalManagerAbbreviation.getWidget().getValue());
		iDepartment.setStatusTypeStr(iStatusType.getWidget().getSelectedValue());
		iDepartment.setAllowReqRoom(iAllowReqRoom.getWidget().getValue());
		iDepartment.setAllowReqDistribution(iAllowReqDist.getWidget().getValue().booleanValue());
		iDepartment.setAllowReqTime(iAllowReqTime.getWidget().getValue().booleanValue());
		iDepartment.setAllowReqRoom(iAllowReqRoom.getWidget().getValue().booleanValue());
		iDepartment.setExternalFundingDept(iExternalFundingDept.getWidget().getValue().booleanValue());
		iDepartment.setAllowEvents(iAllowEvents.getWidget().getValue().booleanValue());
		iDepartment.setInheritInstructorPreferences(iInheritInstructorPreferences.getWidget().getValue().booleanValue());
		iDepartment.setAllowEvents(iAllowEvents.getWidget().getValue().booleanValue());
		iDepartment.setAllowStudentScheduling(iAllowStudentScheduling.getWidget().getValue().booleanValue());

		List dependentDepartmentIds  = new ArrayList();
		List dependentStatuses = new ArrayList();

	    int rows = iControlDeptFlexTable.getRowCount();
	    for (int row = 1; row < rows; row++) { //first row is header
	    	ListBox dept = (ListBox) iControlDeptFlexTable.getWidget(row, 0);
	    	ListBox status = (ListBox) iControlDeptFlexTable.getWidget(row, 1);	    	 
	    	if(dept != null){
	    		if(dept.getSelectedValue() != null && dept.getSelectedValue() != "")
	    			dependentDepartmentIds.add(dept.getSelectedValue());
	    		if(status.getSelectedValue() != null && status.getSelectedValue() != "")
	    			dependentStatuses.add(status.getSelectedValue());

	    	}
	    }
	    iDepartment.setDependentDepartments(dependentDepartmentIds);
	    iDepartment.setDependentStatuses(dependentStatuses); 
		return iDepartment;
	}
	
	/*
	 * validate UI
	 */
	protected boolean validate() {
		boolean ok = true;
		if (iAbbreviation.getWidget().getText().isEmpty()) {
			iAbbreviation.setErrorHint(MESSAGES.errorAbbreviationIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationIsEmpty());
			ok = false;
		} else if (!isAbbreviationUnique(getValue())) {
			iAbbreviation.setErrorHint(MESSAGES.errorAbbreviationMustBeUnique());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationMustBeUnique());
			ok = false;
		}

		if (iName.getWidget().getText().trim().isEmpty()) {
			iName.setErrorHint(MESSAGES.errorNameIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorNameIsEmpty());
			ok = false;
		}

		if (iDeptCode.getWidget().getText().isEmpty()) {
			iAbbreviation.setErrorHint(MESSAGES.errorAbbreviationIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationIsEmpty());
			ok = false;
		} else if (!isAbbreviationUnique(getValue())) {
			iDeptCode.setErrorHint(MESSAGES.errorAbbreviationMustBeUnique());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorAbbreviationMustBeUnique());
			ok = false;
		}

        if (iExternalManager.getWidget().getValue() == true && (iExternalManagerName.getWidget().getText().isEmpty()|| iExternalManagerName.getWidget().getText().length() ==0)) {
        	iExternalManagerName.setErrorHint(MESSAGES.errorRequired("External Manager Name"));
			if (ok) iHeader.setErrorMessage(MESSAGES.errorRequired("External Manager Name"));
			ok = false;
        }
 
        if (iExternalManager.getWidget().getValue() == false && (!iExternalManagerName.getWidget().getText().isEmpty()|| iExternalManagerName.getWidget().getText().trim().length() >0)) {
        	iExternalManagerName.setErrorHint(MESSAGES.errorGeneric("External Manager Name should only be used when the department is marked as 'External Manager'"));
			if (ok) iHeader.setErrorMessage(MESSAGES.errorGeneric("External Manager Name should only be used when the department is marked as 'External Manager'"));
			ok = false;
        }
 
        if (iExternalManager.getWidget().getValue() == true && (iExternalManagerAbbreviation.getWidget().getText().isEmpty()|| iExternalManagerAbbreviation.getWidget().getText().length() ==0)) {
        	iExternalManagerAbbreviation.setErrorHint(MESSAGES.errorRequired("External Manager Abbreviation"));
			if (ok) iHeader.setErrorMessage(MESSAGES.errorRequired("External Manager Abbreviation"));
			ok = false;
        }
        if (iExternalManager.getWidget().getValue() == false && (!iExternalManagerAbbreviation.getWidget().getText().isEmpty() || iExternalManagerAbbreviation.getWidget().getText().trim().length() >0)) {
        	iExternalManagerAbbreviation.setErrorHint(MESSAGES.errorGeneric("External Manager Abbreviation should only be used when the department is marked as 'External Manager'"));
			if (ok) iHeader.setErrorMessage(MESSAGES.errorGeneric("External Manager Abbreviation should only be used when the department is marked as 'External Manager'"));
			ok = false;
        }
 
        
		return ok;
	}

	public DepartmentInterface getiDepartment() {
		return iDepartment;
	}

	public void setiDepartment(DepartmentInterface iDepartment) {
		this.iDepartment = iDepartment;
	}

	public void show() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * execute add, update, delete
	 */
	public static class UpdateDepartmentRequest implements GwtRpcRequest<DepartmentInterface> {
		private UpdateDepartmentAction iAction;
		private DepartmentInterface iDepartment;
			
		public UpdateDepartmentAction getAction() { return iAction; }
		public void setAction(UpdateDepartmentAction action) { iAction = action; }
		public DepartmentInterface getDepartment() { return iDepartment; }
		public void setDepartment(DepartmentInterface department) { iDepartment = department; }

	}	
	
	/*
	 * UI for status options in editing department and controlling department
	 */
	public ListBox statusOptions(DepartmentInterface department ) {
		ListBox statusTypeOptions = new ListBox();
		statusTypeOptions.setMultipleSelect(false);
		statusTypeOptions.setWidth("300px");
		statusTypeOptions.setStyleName("gwt-SuggestBox");
		statusTypeOptions.setVisibleItemCount(1);		
		statusTypeOptions.addItem(MESSAGES.defaultDependentStatus(), "");

	for (Entry<String, String> entry : department.getSatusOptions().entrySet()) {    
			statusTypeOptions.addItem(entry.getValue().toString() , entry.getKey().toString());
		}	
		return statusTypeOptions;
	}
	
	private void setSelectedValue(ListBox lBox, String str) {
	    String text = str;
	    int indexToFind = -1;
	    for (int i = 0; i < lBox.getItemCount(); i++) {
	        if (lBox.getValue(i).equals(text)) {
	            indexToFind = i;
	            break;
	        }
	    }
	    lBox.setSelectedIndex(indexToFind);
	}
	
	/*
	 * UI for department options in controlling department
	 */
	public ListBox departmentOptions(DepartmentInterface department ) {
		ListBox departmentOptions = new ListBox();
		departmentOptions.setMultipleSelect(false);
		departmentOptions.setWidth("300px");
		departmentOptions.setStyleName("unitime-TextBox");
		departmentOptions.setVisibleItemCount(1);
		departmentOptions.addItem(MESSAGES.defaultDependentDepartment(), "");
		for (Entry<Long, String> entry : department.getExtDepartmentOptions().entrySet()) {
			departmentOptions.addItem(entry.getValue().toString() , entry.getKey().toString());
		}	
		return departmentOptions;
	}
	
	/*
	 * Add  row in controlling department 
	 */
	public void addNewRow(DepartmentInterface department ) {
		// Add a button to delete
		Button deleteStatusButton = new Button("Delete");
		int row = iControlDeptFlexTable.getRowCount();
		
		//delete a row
		deleteStatusButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int receiverRowIndex = iControlDeptFlexTable.getCellForEvent(event).getRowIndex();
				iControlDeptFlexTable.removeRow(receiverRowIndex);
			}
		});
		
		//delete btn
		iControlDeptFlexTable.setWidget(row, 2, deleteStatusButton);

		//status
		iControlDeptFlexTable.setWidget(row, 1, statusOptions(department));

		//department drop down
		iControlDeptFlexTable.setWidget(row, 0, departmentOptions(department));
	}

	/*
	 * Delete all dependent departments
	 */
	public void deleteAllDependentDepartments() {
		while (iControlDeptFlexTable.getRowCount() > 1)
			iControlDeptFlexTable.removeRow(1);
	}
}
