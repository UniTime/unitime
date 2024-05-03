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
package org.unitime.timetable.gwt.client.offerings;

import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaTextBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.InstrOfferingConfigColumn;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.Operation;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.Reference;
import org.unitime.timetable.gwt.shared.InstrOfferingConfigInterface.SubpartLine;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class InstrOfferingConfigPage extends Composite {
	protected static final GwtMessages GWTMSG = GWT.create(GwtMessages.class);
	protected static final CourseMessages MESSAGES = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private TextBox iConfigName;
	private ListBox iDurationType;
	private ListBox iInstructionalMethod;
	private CheckBox iUnlimited;
	private NumberBox iLimit;
	private InstrOfferingConfigTable iTable;
	private CheckBox iVariableLimits;
	private ListBox iInstructionalType;
	private Button iAdd;
	private boolean iInstructionalTypeAllOptions = false;

	private int iLimitRow;
	private P iError;
	
	private InstrOfferingConfigInterface iData;
	
	public InstrOfferingConfigPage() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-InstrOfferingConfig");
		
		
		LoadingWidget.getInstance().show(GWTMSG.waitLoadingData());
		String configId = Location.getParameter("id");
		String offeringId = Location.getParameter("offering");
		InstrOfferingConfigInterface request = new InstrOfferingConfigInterface(Operation.LOAD, offeringId == null ? null : Long.valueOf(offeringId), configId == null ? null : Long.valueOf(configId));
		request.setOp(Location.getParameter("op"));
		RPC.execute(request, new AsyncCallback<InstrOfferingConfigInterface>() {
			@Override
			public void onSuccess(InstrOfferingConfigInterface result) {
				initPage(result);
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						iHeader.setEnabled("back", true);
						iHeader.setEnabled("save", iData.getConfigId() == null && !iData.getSubpartLines().isEmpty());
						iHeader.setEnabled("update", iData.getConfigId() != null && !iData.getSubpartLines().isEmpty());
						iHeader.setEnabled("delete", iData.isCanDelete());
						LoadingWidget.getInstance().hide();
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(GWTMSG.failedLoadData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}
		});
		
		
		initWidget(iForm);
	}
	
	protected void initPage(InstrOfferingConfigInterface data) {
		iData = data;
		iHeader = new UniTimeHeaderPanel(data.getCourseName());
		iForm.addHeaderRow(iHeader);
		
		final ClickHandler saveOrUpdateClick = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.setEnabled("back", false);
				iHeader.setEnabled("save", false);
				iHeader.setEnabled("update", false);
				iHeader.setEnabled("delete", false);
				LoadingWidget.getInstance().show(
						iData.getConfigId() == null ?
						GWTMSG.waitCreate(GWTMSG.labelConfiguration(iData.getConfigName())) :
						GWTMSG.waitUpdate(GWTMSG.labelConfiguration(iData.getConfigName())));
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						if (validate()) {
							iData.setOperation(Operation.SAVE);
							RPC.execute(iData, new AsyncCallback<InstrOfferingConfigInterface>() {
								@Override
								public void onSuccess(InstrOfferingConfigInterface result) {
									ToolBox.open("instructionalOfferingDetail.action?op=view&io=" + result.getOfferingId() + "#ioc" + result.getConfigId());
								}
								
								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(GWTMSG.failedSave(caught.getMessage()), caught);
									ToolBox.checkAccess(caught);
									iHeader.setEnabled("save", iData.getConfigId() == null && !iData.getSubpartLines().isEmpty());
									iHeader.setEnabled("update", iData.getConfigId() != null && !iData.getSubpartLines().isEmpty());
									iHeader.setEnabled("delete", iData.isCanDelete());
									iHeader.setEnabled("back", true);
									iError.clear();
									P eh = new P("error-header"); eh.setText(GWTMSG.failedSave("")); iError.add(eh);
									P em = new P("error-message"); em.setText(caught.getMessage()); iError.add(em);
									iError.setVisible(true);									
								}
							});
						} else {
							LoadingWidget.getInstance().hide();
							iHeader.setEnabled("save", iData.getConfigId() == null);
							iHeader.setEnabled("update", iData.getConfigId() != null);
							iHeader.setEnabled("delete", iData.isCanDelete());
							iHeader.setEnabled("back", true);
						}
					}
				});
			}
		};
		
		iHeader.addButton("save", GWTMSG.buttonSave(), saveOrUpdateClick);
		iHeader.addButton("update", GWTMSG.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(final ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmMayDeleteSubpartsClasses(), new Command() {
					@Override
					public void execute() {
						saveOrUpdateClick.onClick(event);
					}
				});
			}
		});
		iHeader.addButton("delete", GWTMSG.buttonDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmDeleteExistingSubpartsClasses(), new Command() {
					@Override
					public void execute() {
						iHeader.setEnabled("back", false);
						iHeader.setEnabled("save", false);
						iHeader.setEnabled("update", false);
						iHeader.setEnabled("delete", false);
						LoadingWidget.getInstance().show(GWTMSG.waitDelete(MESSAGES.labelConfiguration(iData.getConfigName())));
						Scheduler.get().scheduleDeferred(new ScheduledCommand() {
							@Override
							public void execute() {
								if (validate()) {
									iData.setOperation(Operation.DELETE);
									RPC.execute(iData, new AsyncCallback<InstrOfferingConfigInterface>() {
										@Override
										public void onSuccess(InstrOfferingConfigInterface result) {
											ToolBox.open("instructionalOfferingDetail.action?op=view&io=" + result.getOfferingId());
										}
										
										@Override
										public void onFailure(Throwable caught) {
											LoadingWidget.getInstance().hide();
											UniTimeNotifications.error(GWTMSG.failedDelete(MESSAGES.labelConfiguration(iData.getConfigName()), caught.getMessage()), caught);
											ToolBox.checkAccess(caught);
											iHeader.setEnabled("save", iData.getConfigId() == null && !iData.getSubpartLines().isEmpty());
											iHeader.setEnabled("update", iData.getConfigId() != null && !iData.getSubpartLines().isEmpty());
											iHeader.setEnabled("delete", iData.isCanDelete());
											iHeader.setEnabled("back", true);
											iError.clear();
											P eh = new P("error-header"); eh.setText(GWTMSG.failedSave("")); iError.add(eh);
											P em = new P("error-message"); em.setText(caught.getMessage()); iError.add(em);
											iError.setVisible(true);									
										}
									});
								} else {
									LoadingWidget.getInstance().hide();
									iHeader.setEnabled("save", iData.getConfigId() == null && !iData.getSubpartLines().isEmpty());
									iHeader.setEnabled("update", iData.getConfigId() != null && !iData.getSubpartLines().isEmpty());
									iHeader.setEnabled("delete", iData.isCanDelete());
									iHeader.setEnabled("back", true);
								}
							}
						});
					}
				});
			}	
		});
		iHeader.addButton("back", GWTMSG.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.setEnabled("back", false);
				iHeader.setEnabled("save", false);
				iHeader.setEnabled("update", false);
				iHeader.setEnabled("delete", false);
				ToolBox.open("instructionalOfferingDetail.action?op=view&io=" + iData.getOfferingId() + (iData.getConfigId() == null ? "" : "#ioc" + iData.getConfigId()));
			}
		});

		iHeader.setEnabled("save", false);
		iHeader.setEnabled("update", false);
		iHeader.setEnabled("back", false);
		iHeader.setEnabled("delete", false);
		
		iError = new P("error-table");
		iForm.addRow(iError);
		iError.setVisible(false);
		
		iConfigName = new AriaTextBox();
		iConfigName.setStyleName("gwt-SuggestBox");
		iConfigName.setMaxLength(20);
		iConfigName.setWidth("140px");
		iConfigName.setText(iData.getConfigName() == null ? "" : iData.getConfigName());
		iForm.addRow(MESSAGES.propertyConfigurationName(), iConfigName);
		iConfigName.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> evt) {
				iData.setConfigName(evt.getValue());
			}
		});
		
		iUnlimited = new CheckBox();
		iUnlimited.setValue(iData.isUnlimited());
		iForm.addRow(MESSAGES.propertyUnlimitedEnrollment(), iUnlimited);
		iUnlimited.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setUnlimited(event.getValue());
				updateCounts();
			}
		});
		
		iLimit = new NumberBox();
		iLimit.setValue(Boolean.TRUE.equals(iData.isUnlimited()) ? null : iData.getLimit()); 
		iLimit.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iData.setLimit(iLimit.toInteger());
				if (iData.getLimit() == null) {
					iData.setLimit(0);
					iLimit.setValue(iData.getLimit());
				}
				updateCounts();
			}
		});
		iLimitRow = iForm.addRow(MESSAGES.propertyConfigurationLimit(), iLimit);
		
		if (iData.isDisplayCourseLink()) {
			CourseDetailsWidget link = new CourseDetailsWidget(true);
			link.reload(iData.getCourseId());
			iForm.addRow(MESSAGES.propertyCourseCatalog(), link);
		}
		
		if (iData.hasDurationTypes()) {
			if (iData.isDurationTypeEditable()) {
				iDurationType = new ListBox();
				for (Reference im: iData.getDurationTypes()) {
					iDurationType.addItem(im.getLabel(), im.getId().toString());
					if (im.getId().equals(iData.getDurationTypeId()))
						iDurationType.setSelectedIndex(iDurationType.getItemCount() - 1);
				}
				iForm.addRow(MESSAGES.propertyClassDurationType(), iDurationType);
				iDurationType.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						String id = iDurationType.getValue(iDurationType.getSelectedIndex());
						if (id == null || id.isEmpty())
							iData.setDurationTypeId(null);
						else
							iData.setDurationTypeId(Long.valueOf(id));
						updateTable();
					}
				});
			} else {
				Reference dt = iData.getDurationType(iData.getDurationTypeId());
				if (dt != null)
					iForm.addRow(MESSAGES.propertyClassDurationType(), new Label(dt.getLabel()));
			}
		}
		
		if (iData.hasInstructionalMethods()) {
			if (iData.isInstructionalMethodEditable()) {
				iInstructionalMethod = new ListBox();
				for (Reference im: iData.getInstructionalMethods()) {
					iInstructionalMethod.addItem(im.getLabel(), im.getId().toString());
					if (im.getId().equals(iData.getInstructionalMethodId()))
						iInstructionalMethod.setSelectedIndex(iInstructionalMethod.getItemCount() - 1);
				}
				iForm.addRow(MESSAGES.propertyInstructionalMethod(), iInstructionalMethod);
				iInstructionalMethod.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						String id = iInstructionalMethod.getValue(iInstructionalMethod.getSelectedIndex());
						if (id == null || id.isEmpty())
							iData.setInstructionalMethodId(null);
						else
							iData.setInstructionalMethodId(Long.valueOf(id));
					}
				});
			} else {
				Reference im = iData.getInstructionalMethod(iData.getInstructionalMethodId());
				if (im != null)
					iForm.addRow(MESSAGES.propertyInstructionalMethod(), new Label(im.getLabel()));	
			}
		}
		
		iInstructionalType = new ListBox();
		populateInstructionalTypes();
		iInstructionalType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String id = iInstructionalType.getValue(iInstructionalType.getSelectedIndex());
				if ("less".equals(id)) {
					iInstructionalTypeAllOptions = false;
					populateInstructionalTypes();
				} else if ("more".equals(id)) {
					iInstructionalTypeAllOptions = true;
					populateInstructionalTypes();
				}
			}
		});
		iAdd = new Button(MESSAGES.actionAddInstructionalTypeToConfig());
		iAdd.setAccessKey(MESSAGES.accessAddInstructionalTypeToConfig().charAt(0));
		iAdd.setTitle(MESSAGES.titleAddInstructionalTypeToConfig(MESSAGES.accessAddInstructionalTypeToConfig()));
		P it = new P("instructional-type-selection");
		it.add(iInstructionalType);
		it.add(iAdd);
		iForm.addRow(MESSAGES.filterInstructionalType(), it);
		iAdd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String id = iInstructionalType.getValue(iInstructionalType.getSelectedIndex());
				try {
					iTable.addSubpartLine(Long.valueOf(id));
				} catch (NumberFormatException e) {}
			}
		});

		if (iData.isDisplayOptionForMaxLimit()) {
			iVariableLimits = new CheckBox(MESSAGES.labelAllowVariableLimits());
			iVariableLimits.setValue(iData.isDisplayMaxLimit());
			iVariableLimits.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iData.setDisplayMaxLimit(event.getValue());
					updateTable();
				}
			});
			iForm.addRow("", iVariableLimits);
		}

		iTable = new InstrOfferingConfigTable(data) {
			@Override
			protected void onLimitChange() {
				updateCounts();
				iHeader.setEnabled("back", true);
				iHeader.setEnabled("save", iData.getConfigId() == null && !iData.getSubpartLines().isEmpty());
				iHeader.setEnabled("update", iData.getConfigId() != null && !iData.getSubpartLines().isEmpty());
				iHeader.setEnabled("delete", iData.isCanDelete());
			}
		};
		iForm.addRow(iTable);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		
		updateCounts();
	}
	
	protected void populateInstructionalTypes() {
		iInstructionalType.clear();
		iInstructionalType.addItem(MESSAGES.itemSelect(), "-");
		for (Reference it: iData.getInstructionalTypes()) {
			if (iInstructionalTypeAllOptions || it.isSelectable()) {
				iInstructionalType.addItem(it.getLabel(), it.getId().toString());
			}
		}
		if (iInstructionalTypeAllOptions) {
			iInstructionalType.addItem(MESSAGES.selectLessOptions(), "less");
		} else {
			iInstructionalType.addItem(MESSAGES.selectMoreOptions(), "more");
		}
	}
	
	protected void updateCounts() {
		iForm.getRowFormatter().setVisible(iLimitRow, !iData.isUnlimited());
		if (iVariableLimits != null) {
			iVariableLimits.setVisible(!iData.isUnlimited());
			boolean hasVariable = false;
			int row = 1;
			for (SubpartLine line: iData.getSubpartLines()) {
				if (line.getMinClassLimit() != null && line.getMaxClassLimit() != null && line.getMinClassLimit() != line.getMaxClassLimit()) {
					hasVariable = true;
				}
				boolean badLimit = false;
				boolean badNbrClasses = false;
				if (!iData.isUnlimited()) {
					if (line.getMaxClassLimit() != null && line.getNumberOfClasses() != null) {
						SubpartLine parent = iData.getSubpartLine(line.getParentId());
						if (parent != null && parent.getNumberOfClasses() != null && parent.getMaxClassLimit() != null) {
							if (line.getNumberOfClasses() <= 0 || parent.getNumberOfClasses() <= 0 || (line.getNumberOfClasses() % parent.getNumberOfClasses()) != 0)
								badNbrClasses = true; // not divisible # children
							if (line.getMaxClassLimit() <= 0 || parent.getMaxClassLimit() <= 0)
								badLimit = true; // class too small
							if (line.getNumberOfClasses() * line.getMaxClassLimit() < parent.getNumberOfClasses() * parent.getMaxClassLimit())
								badLimit = true; // class too small
							else if (line.getMaxClassLimit() * (line.getNumberOfClasses() / parent.getNumberOfClasses()) < parent.getMaxClassLimit())
								badLimit = true; // class too small
							else if (line.getMaxClassLimit() > parent.getMaxClassLimit())
								badLimit = true; // class too small
						} else if (parent == null && iData.getLimit() != null) {
							if (line.getNumberOfClasses() <= 0)
								badNbrClasses = true;
							if (line.getMaxClassLimit() <= 0)
								badLimit = true;
							if (iData.isCheckLimits()) {
								 if (line.getNumberOfClasses() * line.getMaxClassLimit() < iData.getLimit())
										badLimit = true; // below configuration limit
									else if (line.getNumberOfClasses() == 1 && line.getMaxClassLimit() != iData.getLimit())
										badLimit = true;
									else if (line.getNumberOfClasses() > 1 && line.getMaxClassLimit() > iData.getLimit())
										badLimit = true;
							}
						}
					}
				} else {
					if (line.getNumberOfClasses() != null) {
						SubpartLine parent = iData.getSubpartLine(line.getParentId());
						if (parent != null) {
							if (line.getNumberOfClasses() <= 0 || parent.getNumberOfClasses() <= 0 || (line.getNumberOfClasses() % parent.getNumberOfClasses()) != 0)
								badNbrClasses = true; // not divisible # children
						} else {
							if (line.getNumberOfClasses() <= 0)
								badNbrClasses = true;
						}
					}
				}
				if (badLimit)
					iTable.getRowFormatter().addStyleName(row, "bad-limit");
				else
					iTable.getRowFormatter().removeStyleName(row, "bad-limit");
				if (badNbrClasses)
					iTable.getRowFormatter().addStyleName(row, "bad-nbr-classes");
				else
					iTable.getRowFormatter().removeStyleName(row, "bad-nbr-classes");
				row++;
			}
			iVariableLimits.setEnabled(!hasVariable);
		}
		iTable.setUnlimited(iData.isUnlimited());
	}
	
	protected void updateTable() {
		int limitIdx = iTable.getIndex(InstrOfferingConfigColumn.LIMIT);
		for (int i = 0; i < iTable.getRowCount(); i++) {
			SubpartLine line = iTable.getData(i);
			if (line != null) {
				if (limitIdx >= 0) {
					Widget w = iTable.getWidget(i, limitIdx);
					if (w != null && w instanceof InstrOfferingConfigTable.ClassLimit)
						((InstrOfferingConfigTable.ClassLimit)w).setMaxVisible(iData.isDisplayMaxLimit());
				}
			}
		}
		UniTimeTableHeader h = iTable.getHeader(iTable.getIndex(InstrOfferingConfigColumn.LIMIT));
		h.setHTML(iTable.getColumnName(InstrOfferingConfigColumn.LIMIT));
		UniTimeTableHeader h2 = iTable.getHeader(iTable.getIndex(InstrOfferingConfigColumn.MINS_PER_WK));
		h2.setHTML(iTable.getColumnName(InstrOfferingConfigColumn.MINS_PER_WK));
	}
	
	private boolean validate() {
		Set<String> errors = new TreeSet<>();

		if (iData.getConfigName() == null || iData.getConfigName().isEmpty()) {
			errors.add(MESSAGES.errorRequiredField(MESSAGES.propertyConfigurationName().replace(":", "")));
		} else if (iData.hasConfigs()) {
			for (Reference config: iData.getConfigs()) {
				if (config.getLabel().equalsIgnoreCase(iData.getConfigName()) && !config.getId().equals(iData.getConfigId()))
					errors.add(MESSAGES.errorConfigurationAlreadyExists());
			}
		}
		if (!iData.isUnlimited() && iData.getLimit() == null) {
			errors.add(MESSAGES.errorRequiredField(MESSAGES.propertyConfigurationLimit().replace(":", "")));
		}
		
        String lblMax = MESSAGES.columnLimit();
        if (iData.isDisplayMaxLimit()) lblMax = MESSAGES.columnMaxLimit();
		
		for (SubpartLine line: iData.getSubpartLines()) {
			line.setError(null);
			if (line.getNumberOfClasses() == null || line.getNumberOfClasses() <= 0) {
				String error = MESSAGES.errorIntegerGt(MESSAGES.messageNumberOfClassesForIType(line.getLabel()), "0");
				errors.add(error);
				line.addError(error);
			} else if (iData.getMaxNumberOfClasses() != null && line.getNumberOfClasses() > iData.getMaxNumberOfClasses()) {
				String error = MESSAGES.errorIntegerLtEq(MESSAGES.messageNumberOfClassesForIType(line.getLabel()), iData.getMaxNumberOfClasses().toString());
				errors.add(error);
				line.addError(error);
			}
			if (iData.isUnlimited() && (line.getMinClassLimit() == null || line.getMaxClassLimit() == null || line.getMinClassLimit() <= 0 || line.getMaxClassLimit() <= 0)) {
				String error = MESSAGES.errorIntegerGtEq(MESSAGES.messageLimitPerClassForIType(lblMax, line.getLabel()), "0");
				errors.add(error);
				line.addError(error);
			}
			if (!iData.isUnlimited() && line.getNumberOfRooms() == null || line.getNumberOfRooms() < 0) {
				String error = MESSAGES.errorIntegerGtEq(MESSAGES.messageNumberOfRoomsForIType(line.getLabel()), "0");
				errors.add(error);
				line.addError(error);
			}
			if (line.getMinutesPerWeek() == null || line.getMinutesPerWeek() < 0) {
				String error = MESSAGES.errorIntegerGtEq(MESSAGES.messageMinsPerWeekForIType(line.getLabel()), "0");
				errors.add(error);
				line.addError(error);
			} else if (!iData.isUnlimited() && line.getMinutesPerWeek() == 0 && line.getNumberOfRooms() != null && line.getNumberOfRooms() > 0) {
				String error = MESSAGES.messageMinsPerWeekForITypeCanBeZeroWhenNbrRoomsIsZero(line.getLabel());
				errors.add(error);
				line.addError(error);
			}
			if (!iData.isUnlimited() && line.getRoomRatio() == null || line.getRoomRatio() < 0f) {
				String error = MESSAGES.errorIntegerGtEq(MESSAGES.messageRoomRatioForIType(line.getLabel()), "0");
				errors.add(error);
				line.addError(error);
			}

			if (!iData.isUnlimited() && iData.isCheckLimits() && iData.getLimit() != null && !line.hasError()) {
				if (line.getNumberOfClasses() == 1 && line.getMaxClassLimit() != iData.getLimit()) {
					String error = MESSAGES.errorEqual(MESSAGES.messageLimitPerClassForIType(lblMax, line.getLabel()), MESSAGES.messageConfigurationLimit(iData.getLimit()));
					errors.add(error);
					line.addError(error);
                }
                if (line.getNumberOfClasses() > 1 && (line.getMaxClassLimit() * line.getNumberOfClasses()) < iData.getLimit()) {
                	String error = MESSAGES.errorIntegerGtEq(MESSAGES.messageSumClassLimitsForIType(line.getLabel()), MESSAGES.messageConfigurationLimit(iData.getLimit()));
					errors.add(error);
					line.addError(error);
                }
                if (line.getNumberOfClasses() > 1 && line.getMaxClassLimit() > iData.getLimit()) {
                	String error = MESSAGES.errorIntegerLtEq(MESSAGES.messageLimitPerClassOfLimitForIType(lblMax, line.getMaxClassLimit(), line.getLabel()), MESSAGES.messageConfigurationLimit(iData.getLimit()));
					errors.add(error);
					line.addError(error);
                }
            }
			
			SubpartLine parent = iData.getSubpartLine(line.getParentId());
			if (parent != null && !line.hasError() && !parent.hasError()) {
            	if (parent.getNumberOfClasses() > 0 && (line.getNumberOfClasses() % parent.getNumberOfClasses()) != 0) {
            		String error = MESSAGES.errorConfigurationNC(parent.getLabel(), parent.getNumberOfClasses());
					errors.add(error);
					line.addError(error);
	            }
                if (!iData.isUnlimited()) {
	                if (line.getMaxClassLimit() > parent.getMaxClassLimit()) {
                		String error = MESSAGES.errorConfigurationCL(parent.getLabel(), lblMax, parent.getMaxClassLimit());
    					errors.add(error);
    					line.addError(error);
	                }
	                if (line.getNumberOfClasses() * line.getMaxClassLimit() < parent.getNumberOfClasses() * parent.getMaxClassLimit()) {
                		String error = MESSAGES.errorConfigurationLS(parent.getLabel());
    					errors.add(error);
    					line.addError(error);
	                }
	                if (line.getMaxClassLimit() * (line.getNumberOfClasses() / parent.getNumberOfClasses()) < parent.getMaxClassLimit()) {
	                	String error = MESSAGES.errorConfigurationLS(parent.getLabel());
    					errors.add(error);
    					line.addError(error);
	                }
                }
			}
		}
		
		
		
		iTable.updateButtons();
		updateCounts();
		if (errors.isEmpty()) {
			iError.clear(); iError.setVisible(false);
		} else {
			iError.clear();
			P eh = new P("error-header"); eh.setText(MESSAGES.formValidationErrors()); iError.add(eh);
			for (String error: errors) {
				P em = new P("error-message"); em.setHTML(error); iError.add(em);
			}
			iError.setVisible(true);
		}
		return errors.isEmpty();
	}
}
