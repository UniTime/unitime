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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.ClassSetupInterface;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.ClassLine;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.ClassSetupColumn;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.Operation;
import org.unitime.timetable.gwt.shared.ClassSetupInterface.Reference;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomas Muller
 */
public class MultipleClassSetupPage extends Composite {
	protected static final GwtMessages GWTMSG = GWT.create(GwtMessages.class);
	protected static final CourseMessages MESSAGES = GWT.create(CourseMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	
	private ListBox iInstructionalMethod;
	private CheckBox iUnlimited;
	private NumberBox iLimit;
	private Map<Long, SubpartLimit> iSubpartLimits;
	private Map<Long, SnapshotLimit> iSnapshotLimits;
	private ClassSetupTable iTable;
	private CheckBox iVariableLimits;

	private int iLimitRow;
	private P iError;
	
	private ClassSetupInterface iData;
	
	public MultipleClassSetupPage() {
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-MultipleClassSetup");
		
		
		LoadingWidget.getInstance().show(GWTMSG.waitLoadingData());
		ClassSetupInterface request = new ClassSetupInterface(Operation.LOAD, Long.valueOf(Location.getParameter("id")));
		RPC.execute(request, new AsyncCallback<ClassSetupInterface>() {
			@Override
			public void onSuccess(ClassSetupInterface result) {
				initPage(result);
				LoadingWidget.getInstance().hide();
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
	
	protected void initPage(ClassSetupInterface data) {
		iData = data;
		iHeader = new UniTimeHeaderPanel(data.getName());
		iForm.addHeaderRow(iHeader);
		
		iHeader.addButton("update", GWTMSG.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (validate()) {
					LoadingWidget.getInstance().show(GWTMSG.waitSavingData());
					iData.setOperation(Operation.SAVE);
					RPC.execute(iData, new AsyncCallback<ClassSetupInterface>() {
						@Override
						public void onSuccess(ClassSetupInterface result) {
							ToolBox.open("instructionalOfferingDetail.action?op=view&io=" + iData.getOfferingId() + "#ioc" + iData.getConfigId());
						}
						
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							UniTimeNotifications.error(GWTMSG.failedSave(caught.getMessage()), caught);
							ToolBox.checkAccess(caught);
						}
					});
				}
			}
		});
		iHeader.addButton("back", GWTMSG.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ToolBox.open("instructionalOfferingDetail.action?op=view&io=" + iData.getOfferingId() + "#ioc" + iData.getConfigId());
			}
		});
		
		iError = new P("error-table");
		iForm.addRow(iError);
		iError.setVisible(false);
		
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
		
		if (iData.isEditUnlimited()) {
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
		} else if (Boolean.TRUE.equals(iData.isUnlimited())) {
			Image on = new Image(RESOURCES.on()); on.setTitle(MESSAGES.titleUnlimitedEnrollment());
			iForm.addRow(MESSAGES.propertyUnlimitedEnrollment(), on);
		}
		iLimit = new NumberBox();
		iLimit.setValue(Boolean.TRUE.equals(iData.isUnlimited()) ? null : iData.getLimit()); 
		iLimit.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				iData.setLimit(iLimit.toInteger());
				updateCounts();
			}
		});
		iLimitRow = iForm.addRow(MESSAGES.propertyConfigurationLimit(), iLimit);
		
		P topSubpartLimits = new P("subpart-limits");
		P bottomSubpartLimits = new P("subpart-limits");
		P topSnapshotLimits = new P("subpart-limits");
		P bottomSnapshotLimits = new P("subpart-limits");

		iSubpartLimits = new HashMap<>();
		iSnapshotLimits = new HashMap<>();
		for (ClassLine line: iData.getClassLines()) {
			SubpartLimit limit = iSubpartLimits.get(line.getSubpartId());
			if (limit == null) {
				limit = new SubpartLimit(line.getSubpartId());
				iSubpartLimits.put(line.getSubpartId(), limit);
				topSubpartLimits.add(limit.iTop);
				bottomSubpartLimits.add(limit.iBottom);
			}
			if (iData.isDisplaySnapshotLimit() && iData.isEditSnapshotLimits()) {
				SnapshotLimit snapshot = iSnapshotLimits.get(line.getSubpartId());
				if (snapshot == null) {
					snapshot = new SnapshotLimit(line.getSubpartId());
					iSnapshotLimits.put(line.getSubpartId(), snapshot);
					topSnapshotLimits.add(snapshot.iTop);
					bottomSnapshotLimits.add(snapshot.iBottom);
				}
			}
		}
		iForm.addRow(MESSAGES.propertySchedulingSubpartLimits(), topSubpartLimits);
		if (iData.isDisplaySnapshotLimit() && iData.isEditSnapshotLimits())
			iForm.addRow(MESSAGES.propertySchedulingSubpartSnapshotLimits(), topSnapshotLimits);

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

		iTable = new ClassSetupTable(data) {
			@Override
			protected void onLimitChange() {
				updateCounts();
			}
		};
		iForm.addRow(iTable);
		
		iForm.addRow(MESSAGES.propertySchedulingSubpartLimits(), bottomSubpartLimits);
		if (iData.isDisplaySnapshotLimit() && iData.isEditSnapshotLimits())
			iForm.addRow(MESSAGES.propertySchedulingSubpartSnapshotLimits(), bottomSnapshotLimits);
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		
		updateCounts();
	}
	
	protected void updateCounts() {
		iForm.getRowFormatter().setVisible(iLimitRow, !iData.isUnlimited());
		if (iVariableLimits != null) {
			iVariableLimits.setVisible(!iData.isUnlimited());
			boolean hasVariable = false;
			for (ClassLine line: iData.getClassLines()) {
				if (line.getMinClassLimit() != null && line.getMaxClassLimit() != null && line.getMinClassLimit() != line.getMaxClassLimit()) {
					hasVariable = true;
				}
			}
			iVariableLimits.setEnabled(!hasVariable);
		}
		for (SubpartLimit limit: iSubpartLimits.values())
			limit.update();
		for (SnapshotLimit snapshot: iSnapshotLimits.values())
			snapshot.update();
		iTable.setUnlimited(iData.isUnlimited());
	}
	
	protected void updateTable() {
		int instIdx = iTable.getIndex(ClassSetupColumn.DISPLAY_INSTRUCTOR);
		int schdIdx = iTable.getIndex(ClassSetupColumn.STUDENT_SCHEDULING);
		int limitIdx = iTable.getIndex(ClassSetupColumn.LIMIT);
		for (int i = 0; i < iTable.getRowCount(); i++) {
			ClassLine line = iTable.getData(i);
			if (line != null && !line.getCancelled()) {
				if (instIdx >= 0) {
					Widget w = iTable.getWidget(i, instIdx);
					if (w != null && w instanceof CheckBox)
						((CheckBox)w).setValue(line.getDisplayInstructors());
				}
				if (schdIdx >= 0) {
					Widget w = iTable.getWidget(i, schdIdx);
					if (w != null && w instanceof CheckBox)
						((CheckBox)w).setValue(line.getEnabledForStudentScheduling());
				}
				if (limitIdx >= 0) {
					Widget w = iTable.getWidget(i, limitIdx);
					if (w != null && w instanceof ClassSetupTable.ClassLimit)
						((ClassSetupTable.ClassLimit)w).setMaxVisible(iData.isDisplayMaxLimit());
				}
			}
		}
		UniTimeTableHeader h = iTable.getHeader(iTable.getIndex(ClassSetupColumn.LIMIT));
		h.setHTML(iTable.getColumnName(ClassSetupColumn.LIMIT));
	}
	
	private boolean validate() {
		Set<String> errors = new TreeSet<>();
		for (ClassLine line: iData.getClassLines()) {
			line.setError(null);
			if (line.getMinClassLimit() != null && line.getMaxClassLimit() != null && line.getMinClassLimit() > line.getMaxClassLimit()) {
				line.addError(MESSAGES.errorMaxLessThanMinLimit(line.getLabel()));
				errors.add(MESSAGES.errorMaxLessThanMinLimit(line.getLabel()));
			}
		}
		// check limits
		if (!iData.isUnlimited()) {
			Long parentSubpartId = null;
			for (Reference subpart: iData.getSubparts()) {
				int maxLimit = 0;
				Map<Long, Integer> maxLimitPerParent = new HashMap<>();
				for (ClassLine line: iData.getClassLines()) {
					if (subpart.getId().equals(line.getSubpartId())) {
						if (parentSubpartId == null && line.getParentId() != null) {
							ClassLine parent = iData.getClassLine(line.getParentId());
							parentSubpartId = (parent == null ? null : parent.getSubpartId());
						}
						if (line.getMaxClassLimit() != null) {
							if (line.getParentId() != null) {
								Integer l = maxLimitPerParent.get(line.getParentId());
								maxLimitPerParent.put(line.getParentId(), (l == null ? 0 : l.intValue()) + line.getMaxClassLimit());
							}
							maxLimit += line.getMaxClassLimit();
						}
					}
				}
				for (ClassLine line: iData.getClassLines()) {
					if (subpart.getId().equals(line.getSubpartId())) {
						ClassLine parent = iData.getClassLine(line.getParentId());
						if (parent != null) {
							Integer limit = maxLimitPerParent.get(line.getParentId());
							if (parent.getMaxClassLimit() != null && parent.getMaxClassLimit() > (limit == null ? 0 : limit)) {
								if (iData.isDisplayMaxLimit()) {
									line.addError(MESSAGES.errorTotalMaxChildrenAtLeastMaxParent());
									errors.add(MESSAGES.errorTotalMaxChildrenAtLeastMaxParent());
								} else {
									line.addError(MESSAGES.errorLimitsChildClasses());
									errors.add(MESSAGES.errorLimitsChildClasses());
								}
									
							}
						}
					}
				}
				if (iData.isValidateLimits()) {
					if (parentSubpartId == null) {
						if (maxLimit < iLimit.toInteger()) {
							if (iData.isDisplayMaxLimit()) {
								errors.add(MESSAGES.errorMaxLimitsTotalTooLow());
								for (ClassLine line: iData.getClassLines()) {
									if (subpart.getId().equals(line.getSubpartId()))
										line.addError(MESSAGES.errorMaxLimitsTotalTooLow());
								}
							} else {
								errors.add(MESSAGES.errorLimitsForTopLevelClassesTooLow());
								for (ClassLine line: iData.getClassLines()) {
									if (subpart.getId().equals(line.getSubpartId()))
										line.addError(MESSAGES.errorLimitsForTopLevelClassesTooLow());
								}
							}
						}
					}
				}
			}
		}
		for (Reference subpart: iData.getSubparts()) {
			int count = 0;
			Long parentSubpartId = null;
			Map<Long, Integer> classesPerParent = new HashMap<>();
			for (ClassLine line: iData.getClassLines()) {
				if (subpart.getId().equals(line.getSubpartId())) {
					if (parentSubpartId == null && line.getParentId() != null) {
						ClassLine parent = iData.getClassLine(line.getParentId());
						parentSubpartId = (parent == null ? null : parent.getSubpartId());
					}
					count ++;
					if (line.getParentId() != null) {
						Integer l = classesPerParent.get(line.getParentId());
						classesPerParent.put(line.getParentId(), (l == null ? 0 : l.intValue()) + 1);
					}
				}
			}
			if (count == 0)
				errors.add(MESSAGES.errorEachSubpartMustHaveClass());
			if (parentSubpartId != null) {
				for (ClassLine line: iData.getClassLines()) {
					if (subpart.getId().equals(parentSubpartId)) {
						Integer children = classesPerParent.get(line.getClassId());
						if (children == null) {
							errors.add(MESSAGES.errorClassMustHaveChildClasses(line.getLabel()));
							line.addError(MESSAGES.errorClassMustHaveChildClasses(line.getLabel()));
						}
					}
				}
			}
		}
		iTable.setData(iData.getClassLines());
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
	
	private class SubpartLimit {
		private Long iSubpartId;
		private int iIndent;
		private String iName = "";
		private Label iTopCounter;
		private Label iBottomCounter;
		private CheckBox iTopDisplayInstructors;
		private CheckBox iBottomDisplayInstructors;
		private CheckBox iTopStudentScheduling;
		private CheckBox iBottomStudentScheduling;
		private P iTop, iBottom;
		
		SubpartLimit(Long subpartId) {
			iSubpartId = subpartId;
			iTopCounter = new Label("0"); iTopCounter.addStyleName("subpart-counter");
			iBottomCounter = new Label("0"); iBottomCounter.addStyleName("subpart-counter");
			iTopStudentScheduling = new CheckBox();
			iBottomStudentScheduling = new CheckBox();
			iBottomStudentScheduling.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iTopStudentScheduling.setValue(event.getValue(), true);
				}
			});
			iTopStudentScheduling.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iBottomStudentScheduling.setValue(event.getValue());
					for (ClassLine line: iData.getClassLines())
						if (line.isEditable() && line.getSubpartId().equals(iSubpartId))
							line.setEnabledForStudentScheduling(event.getValue());
					updateTable();
				}
			});
			iTopDisplayInstructors = new CheckBox();
			iBottomDisplayInstructors = new CheckBox();
			iBottomDisplayInstructors.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iTopDisplayInstructors.setValue(event.getValue(), true);
				}
			});
			iTopDisplayInstructors.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					iBottomDisplayInstructors.setValue(event.getValue());
					for (ClassLine line: iData.getClassLines())
						if (line.isEditable() && line.getSubpartId().equals(iSubpartId))
							line.setDisplayInstructors(event.getValue());
					updateTable();
				}
			});
			iTop = new P("subpart-limit");
			iBottom = new P("subpart-limit");
			update();
		}
		
		public void update() {
			int minLimit = 0, maxLimit = 0;
			int canceled = 0;
			int nbrUnchecked = 0;
			int nbrHidden = 0;
			int nbrEditable = 0;
			Map<Long, Integer> limitPerParent = new HashMap<>();
			Map<Long, Integer> limitPerParentAll = new HashMap<>();
			for (ClassLine line: iData.getClassLines()) {
				if (iSubpartId.equals(line.getSubpartId())) {
					iName = line.getSubpartLabel() + ":";
					iIndent = line.getIndent();
					if (line.getParentId() != null && line.getMaxClassLimit() != null) {
						Integer la = limitPerParentAll.get(line.getParentId());
						limitPerParentAll.put(line.getParentId(), (la == null ? 0 : la.intValue()) + line.getMaxClassLimit());
						Integer l = limitPerParent.get(line.getParentId());
						if (!line.getCancelled())
							limitPerParent.put(line.getParentId(), (l == null ? 0 : l.intValue()) + line.getMaxClassLimit());
					}
					if (line.getCancelled()) {
						if (line.getMaxClassLimit() != null)
							canceled +=line.getMaxClassLimit();
						continue;
					} else {
						if (line.getMinClassLimit() != null)
							minLimit += line.getMinClassLimit();
						if (line.getMaxClassLimit() != null)
							maxLimit += line.getMaxClassLimit();
						if (!Boolean.TRUE.equals(line.getEnabledForStudentScheduling()))
							nbrUnchecked ++;
						if (!Boolean.TRUE.equals(line.getDisplayInstructors()))
							nbrHidden ++;
						if (line.isEditable()) nbrEditable ++;
					}
				}
			}
			if (iData.isUnlimited()) {
				iTopCounter.setText("\u221e");
				iBottomCounter.setText("\u221e");
			} else if (minLimit == maxLimit || !iData.isDisplayMaxLimit()) {
				iTopCounter.setText(String.valueOf(minLimit));
				iBottomCounter.setText(String.valueOf(minLimit));
			} else {
				iTopCounter.setText(minLimit + " - " + maxLimit);
				iBottomCounter.setText(minLimit + " - " + maxLimit);
			}
			if (iLimit.toInteger() == null || iLimit.toInteger() > maxLimit + canceled) {
				iTopCounter.addStyleName("limit-too-low");
				iBottomCounter.addStyleName("limit-too-low");
				iTopCounter.removeStyleName("limit-too-low-cancel");
				iBottomCounter.removeStyleName("limit-too-low-cancel");
			} else if (iLimit.toInteger() == null || iLimit.toInteger() > maxLimit) {
				iTopCounter.addStyleName("limit-too-low-cancel");
				iBottomCounter.addStyleName("limit-too-low-cancel");
				iTopCounter.removeStyleName("limit-too-low");
				iBottomCounter.removeStyleName("limit-too-low");
			} else {
				iTopCounter.removeStyleName("limit-too-low");
				iBottomCounter.removeStyleName("limit-too-low");
				iTopCounter.removeStyleName("limit-too-low-cancel");
				iBottomCounter.removeStyleName("limit-too-low-cancel");
			}
			if (iTable != null) {
				for (int row = 1; row < iTable.getRowCount(); row++) {
					ClassLine line = iTable.getData(row);
					if (iSubpartId.equals(line.getSubpartId())) {// && !line.getCancelled()) {
						boolean badLimit = false;
						boolean badLimitCancel = false;
						if (!iData.isUnlimited()) {
							if (iLimit.toInteger() == null || iLimit.toInteger() > maxLimit + canceled)
								badLimit = true;
							if (iLimit.toInteger() == null || iLimit.toInteger() > maxLimit)
								badLimitCancel = true;
							if (line.getParentId() != null) {
								Integer allLimit = limitPerParentAll.get(line.getParentId());
								Integer limit = limitPerParent.get(line.getParentId());
								ClassLine parent = iData.getClassLine(line.getParentId());
								if ((parent.getMaxClassLimit() == null ? 0 : parent.getMaxClassLimit()) > (allLimit == null ? 0 : allLimit))
									badLimit = true;
								else if ((parent.getMaxClassLimit() == null ? 0 : parent.getMaxClassLimit()) > (limit == null ? 0 : limit))
									badLimitCancel = true;
							}
						}
						Widget w = iTable.getWidget(row, iTable.getIndex(ClassSetupColumn.LIMIT));
						if (badLimit) {
							w.addStyleName("bad-limit");
							w.removeStyleName("bad-limit-cancel");
						} else if (badLimitCancel) {
							w.removeStyleName("bad-limit");
							if (!line.getCancelled())
								w.addStyleName("bad-limit-cancel");
						} else {
							w.removeStyleName("bad-limit");
							w.removeStyleName("bad-limit-cancel");
						}
					}
				}
			}
			if (nbrHidden == 0) {
				iTopDisplayInstructors.setValue(true);
				iBottomDisplayInstructors.setValue(true);
			} else {
				iTopDisplayInstructors.setValue(false);
				iBottomDisplayInstructors.setValue(false);
			}
			if (nbrUnchecked == 0) {
				iTopStudentScheduling.setValue(true);
				iBottomStudentScheduling.setValue(true);
			} else {
				iTopStudentScheduling.setValue(false);
				iTopStudentScheduling.setValue(false);
			}
			iTopDisplayInstructors.setEnabled(nbrEditable > 0);
			iBottomDisplayInstructors.setEnabled(nbrEditable > 0);
			iTopStudentScheduling.setEnabled(nbrEditable > 0);
			iBottomStudentScheduling.setEnabled(nbrEditable > 0);
			iTop.clear();
			P label = new P("subpart-label"); label.setText(iName);
			label.getElement().getStyle().setPaddingLeft(iIndent * 20, Unit.PX);
			iTop.add(label);
			iTop.add(iTopCounter);
			if (iData.isDisplayInstructors() && nbrEditable > 0) {
				label = new P("display-instructors-label"); label.setText(MESSAGES.propertyDisplayInstructors());
				iTop.add(label);
				iTop.add(iTopDisplayInstructors);
			}
			if (iData.isDisplayEnabledForStudentScheduling() && nbrEditable > 0) {
				label = new P("student-scheduling-label"); label.setText(MESSAGES.propertyEnabledForStudentScheduling());
				iTop.add(label);
				iTop.add(iTopStudentScheduling);
			}

			iBottom.clear();
			label = new P("subpart-label"); label.setText(iName);
			label.getElement().getStyle().setPaddingLeft(iIndent * 20, Unit.PX);
			iBottom.add(label);
			iBottom.add(iBottomCounter);
			if (iData.isDisplayInstructors() && nbrEditable > 0) {
				label = new P("display-instructors-label"); label.setText(MESSAGES.propertyDisplayInstructors());
				iBottom.add(label);
				iBottom.add(iBottomDisplayInstructors);
			}
			if (iData.isDisplayEnabledForStudentScheduling() && nbrEditable > 0) {
				label = new P("student-scheduling-label"); label.setText(MESSAGES.propertyEnabledForStudentScheduling());
				iBottom.add(label);
				iBottom.add(iBottomStudentScheduling);
			}
		}
	}
	
	private class SnapshotLimit {
		private Long iSubpartId;
		private int iIndent;
		private String iName = "";
		private Label iTopCounter;
		private Label iBottomCounter;
		private P iTop, iBottom;
		
		SnapshotLimit(Long subpartId) {
			iSubpartId = subpartId;
			iTopCounter = new Label("0"); iTopCounter.addStyleName("subpart-counter");
			iBottomCounter = new Label("0"); iBottomCounter.addStyleName("subpart-counter");
			iTop = new P("subpart-limit");
			iBottom = new P("subpart-limit");
			update();
		}
		
		public void update() {
			int snapshot = 0;
			for (ClassLine line: iData.getClassLines()) {
				if (iSubpartId.equals(line.getSubpartId())) {
					iName = line.getSubpartLabel() + ":";
					iIndent = line.getIndent();
					if (line.getCancelled()) continue;
					if (line.getSnapshotLimit() != null)
						snapshot += line.getSnapshotLimit();
				}
			}
			if (iData.isUnlimited()) {
				iTopCounter.setText("\u221e");
				iBottomCounter.setText("\u221e");
			} else {
				iTopCounter.setText(String.valueOf(snapshot));
				iBottomCounter.setText(String.valueOf(snapshot));
			}
			
			iTop.clear();
			P label = new P("subpart-label"); label.setText(iName);
			label.getElement().getStyle().setPaddingLeft(iIndent * 20, Unit.PX);
			iTop.add(label);
			iTop.add(iTopCounter);

			iBottom.clear();
			label = new P("subpart-label"); label.setText(iName);
			label.getElement().getStyle().setPaddingLeft(iIndent * 20, Unit.PX);
			iBottom.add(label);
			iBottom.add(iBottomCounter);
		}
	}

}
