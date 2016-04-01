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

import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.GetInstructorAttributeParentsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.UpdateInstructorAttributeRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class InstructorAttributeEdit extends Composite {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);

	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private InstructorAttributePropertiesInterface iProperties;
	private AttributeInterface iAttribute;
	
	private UniTimeWidget<TextBox> iName;
	private UniTimeWidget<TextBox> iCode;
	private UniTimeWidget<ListBox> iType;
	private ListBox iParent;
	private Label iDepartment;
	private int iDepartmentRow;
	private CheckBox iGlobal;
	private DepartmentInterface iSelectedDepartment;

	private InstructorsTable iInstructors = null;
	
	public InstructorAttributeEdit(InstructorAttributePropertiesInterface properties) {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-InstructorAttributeEdit");
		iProperties = properties;
		
		iHeader = new UniTimeHeaderPanel();
		ClickHandler createOrUpdateFeature = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (validate()) {
					UpdateInstructorAttributeRequest request = new UpdateInstructorAttributeRequest();
					request.setAttribute(iAttribute);
					for (int i = 1; i < iInstructors.getRowCount(); i++) {
						InstructorInterface instructor = iInstructors.getData(i);
						boolean wasSelected = instructor.hasAttribute(iAttribute.getId());
						boolean selected = iInstructors.isInstructorSelected(i);
						if (selected != wasSelected) {
							if (selected)
								request.addInstructor(instructor.getId());
							else
								request.dropInstructor(instructor.getId());
						}
					}
					LoadingWidget.getInstance().show(iAttribute.getId() == null ? MESSAGES.waitSavingInstructorAttribute() : MESSAGES.waitUpdatingInstructorAttribute());
					RPC.execute(request, new AsyncCallback<AttributeInterface>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							String message = (iAttribute.getId() == null ? MESSAGES.errorFailedToSaveInstructorAttribute(caught.getMessage()) : MESSAGES.errorFailedToUpdateInstructorAttribute(caught.getMessage()));
							iHeader.setErrorMessage(message);
							UniTimeNotifications.error(message);
						}

						@Override
						public void onSuccess(AttributeInterface result) {
							LoadingWidget.getInstance().hide();
							hide(true, result);
						}
					});
				} else {
					iHeader.setErrorMessage(MESSAGES.failedValidationCheckForm());
					UniTimeNotifications.error(MESSAGES.failedValidationCheckForm());
				}
			}
		};
		iHeader.addButton("create", MESSAGES.buttonCreateInstructorAttribute(), 100, createOrUpdateFeature);
		iHeader.addButton("update", MESSAGES.buttonUpdateInstructorAttribute(), 100, createOrUpdateFeature);
		iHeader.addButton("delete", MESSAGES.buttonDeleteInstructorAttribute(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UpdateInstructorAttributeRequest request = new UpdateInstructorAttributeRequest();
				request.setDeleteAttributeId(iAttribute.getId());
				LoadingWidget.getInstance().show(MESSAGES.waitDeletingInstructorAttribute());
				RPC.execute(request, new AsyncCallback<AttributeInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						String message = MESSAGES.errorFailedToDeleteInstructorAttribute(caught.getMessage());
						iHeader.setErrorMessage(message);
						UniTimeNotifications.error(message);
					}

					@Override
					public void onSuccess(AttributeInterface result) {
						LoadingWidget.getInstance().hide();
						hide(true, result);
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), 100, new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide(false, iAttribute);
			}
		});

		iForm.addHeaderRow(iHeader);
		
		iCode = new UniTimeWidget<TextBox>(new TextBox());
		iCode.getWidget().setStyleName("unitime-TextBox");
		iCode.getWidget().setMaxLength(20);
		iCode.getWidget().setWidth("170px");
		iCode.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCode.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propAbbreviation(), iCode);

		iName = new UniTimeWidget<TextBox>(new TextBox());
		iName.getWidget().setStyleName("unitime-TextBox");
		iName.getWidget().setMaxLength(60);
		iName.getWidget().setWidth("370px");
		iName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iName.clearHint();
				iHeader.clearMessage();
			}
		});
		iForm.addRow(MESSAGES.propName(), iName);
		
		iType = new UniTimeWidget<ListBox>(new ListBox());
		iType.getWidget().setStyleName("unitime-TextBox");
		iForm.addRow(MESSAGES.propInstructorAttributeType(), iType);
		iType.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iType.clearHint();
				iHeader.clearMessage();
				setupParents();
			}
		});
		
		iParent = new ListBox();
		iParent.setStyleName("unitime-TextBox");
		iForm.addRow(MESSAGES.propInstructorAttributeParent(), iParent);

		iGlobal = new CheckBox();
		iForm.addRow(MESSAGES.propGlobalInstructorAttribute(), iGlobal);
		iGlobal.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iForm.getRowFormatter().setVisible(iDepartmentRow, !event.getValue());
				setupParents();
			}
		});
		
		iDepartment = new Label();
		iDepartmentRow = iForm.addRow(MESSAGES.propDepartment(), iDepartment);
		iForm.addHeaderRow(MESSAGES.headerInstructors());
		
		iInstructors = new InstructorsTable(iProperties, true);
		iInstructors.setWidth("100%");
		iForm.addRow(iInstructors);
		iInstructors.addMouseClickListener(new MouseClickListener<InstructorInterface>() {
			@Override
			public void onMouseClick(TableEvent<InstructorInterface> event) {
				iHeader.clearMessage();
			}
		});
		
		iFooter = iHeader.clonePanel();
		iForm.addBottomRow(iFooter);
		
		initWidget(iForm);

		iType.getWidget().clear();
		if (!iProperties.getAttributeTypes().isEmpty()) {
			iType.getWidget().addItem(MESSAGES.itemSelect(), "-1");
			for (AttributeTypeInterface type: iProperties.getAttributeTypes())
				iType.getWidget().addItem(type.getLabel(), type.getId().toString());
		}
	}
	
	private void hide(boolean refresh, AttributeInterface attribute) {
		setVisible(false);
		onHide(refresh, attribute);
		Window.scrollTo(iLastScrollLeft, iLastScrollTop);
	}
	
	protected void onHide(boolean refresh, AttributeInterface feature) {
	}
	
	protected void onShow() {
	}
	
	private int iLastScrollTop, iLastScrollLeft;
	public void show() {
		UniTimePageLabel.getInstance().setPageName(iAttribute.getId() == null ? MESSAGES.pageAddInstructorAttribute() : MESSAGES.pageEditInstructorAttribute());
		setVisible(true);
		iLastScrollLeft = Window.getScrollLeft();
		iLastScrollTop = Window.getScrollTop();
		onShow();
		Window.scrollTo(0, 0);
	}
	
	public void hide() {
		hide(true, iAttribute);
	}
	
	protected void setupParents() {
		iParent.clear();
		GetInstructorAttributeParentsRequest request = new GetInstructorAttributeParentsRequest();
		request.setAttributeId(iAttribute.getId());
		request.setDepartmentId(iGlobal.getValue() ? null : iSelectedDepartment == null ? null : iSelectedDepartment.getId());
		request.setTypeId(Long.valueOf(iType.getWidget().getValue(iType.getWidget().getSelectedIndex())));
		RPC.execute(request, new AsyncCallback<GwtRpcResponseList<AttributeInterface>>() {
			@Override
			public void onFailure(Throwable caught) {}
			@Override
			public void onSuccess(GwtRpcResponseList<AttributeInterface> result) {
				iParent.clear();
				iParent.addItem(MESSAGES.itemInstructorAttributeNoParent(), "-1");
				int select = 0;
				for (AttributeInterface a: result) {
					iParent.addItem(a.getName(), a.getId().toString());
					if (a.getId().equals(iAttribute.getParentId())) select = iParent.getItemCount() - 1;
				}
				iParent.setSelectedIndex(select);
			}
		});
	}

	public void setAttribute(AttributeInterface attribute, DepartmentInterface department) {
		iHeader.clearMessage();
		iName.clearHint(); 
		iCode.clearHint();
		iType.clearHint();
		iSelectedDepartment = department;
		iDepartment.setText(department == null ? "" : department.getDeptCode() + " - " + department.getLabel());
		if (attribute == null) {
			iAttribute = new AttributeInterface();
			iHeader.setEnabled("create", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
			iName.getWidget().setText("");
			iName.getWidget().setEnabled(true);
			iCode.getWidget().setText("");
			iCode.getWidget().setEnabled(true);
			iGlobal.setValue(iSelectedDepartment == null, true);
			iGlobal.setEnabled(iProperties.isCanAddGlobalAttribute() && iSelectedDepartment != null);
			iType.getWidget().setSelectedIndex(0);
			iType.getWidget().setEnabled(true);
		} else {
			iAttribute = new AttributeInterface(attribute);
			iHeader.setEnabled("create", false);
			iHeader.setEnabled("update", attribute.canEdit() || attribute.canAssign());
			iHeader.setEnabled("delete", attribute.canDelete());
			iName.getWidget().setText(attribute.getName() == null ? "" : attribute.getName());
			iName.getWidget().setEnabled(attribute.canEdit());
			iCode.getWidget().setText(attribute.getCode() == null ? "" : attribute.getCode());
			iCode.getWidget().setEnabled(attribute.canEdit());
			if (attribute.getType() == null) {
				iType.getWidget().setSelectedIndex(0);
			} else {
				iType.getWidget().setSelectedIndex(1 + iProperties.getAttributeTypes().indexOf(attribute.getType()));
			}
			iType.getWidget().setEnabled(attribute.canEdit() && attribute.canChangeType());
			iGlobal.setValue(!attribute.isDepartmental(), true);
			iGlobal.setEnabled(false);
		}
		setupParents();
	}
	
	public void setInstructors(List<InstructorInterface> instructors) {
		iInstructors.clearTable(1);
		iInstructors.resetVisibility();
		iHeader.clearMessage();
		ValueChangeHandler<Boolean> clearErrorMessage = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iHeader.clearMessage();
			}
		};
		if (instructors != null)
			for (InstructorInterface i: instructors) {
				int row = iInstructors.addInstructor(i);
				boolean selected = i.hasAttribute(iAttribute.getId());
				iInstructors.selectInstructor(row, selected);
				iInstructors.setSelected(row, selected);
				iInstructors.getInstructorSelection(row).addValueChangeHandler(clearErrorMessage);
			}
		int sort = InstructorCookie.getInstance().getSortInstructorsBy();
		if (sort != 0)
			iInstructors.setSortBy(sort);
		iInstructors.setVisible(iInstructors.getRowCount() > 1);
	}
	
	public boolean validate() {
		boolean result = true;
		iAttribute.setName(iName.getWidget().getText());
		if (iAttribute.getName().isEmpty()) {
			iName.setErrorHint(MESSAGES.errorNameIsEmpty());
			result = false;
		}
		iAttribute.setCode(iCode.getWidget().getText());
		if (iAttribute.getCode().isEmpty()) {
			iCode.setErrorHint(MESSAGES.errorAbbreviationIsEmpty());
			result = false;
		}
		iAttribute.setType(iProperties.getAttributeType(Long.valueOf(iType.getWidget().getValue(iType.getWidget().getSelectedIndex()))));
		if (iAttribute.getType() == null) {
			iType.setErrorHint(MESSAGES.errorNoAttributeTypeSelected());
			result = false;
		}
		if (!iGlobal.getValue()) {
			iAttribute.setDepartment(iSelectedDepartment);
		} else {
			iAttribute.setDepartment(null);
		}
		if (iParent.getSelectedIndex() > 0) {
			iAttribute.setParentId(Long.valueOf(iParent.getValue(iParent.getSelectedIndex())));
			iAttribute.setParentName(iParent.getItemText(iParent.getSelectedIndex()));
		} else {
			iAttribute.setParentId(null);
			iAttribute.setParentName(null);
		}
		return result;
	}
	
	public AttributeInterface getAttribute() { return iAttribute; }
}