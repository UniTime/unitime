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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditRequest;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditRequest.Operation;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionEditResponse;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionObjectInterface;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupClasses;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupCourses;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.DistributionsLookupSubparts;
import org.unitime.timetable.gwt.client.offerings.PrefGroupEditInterface.IdLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class DistributionsEditPage extends Composite {
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private DistributionEditResponse iData;
	private UniTimeTable<DistributionObjectInterface> iClasses;
	
	public DistributionsEditPage() {
		Long id = null;
		if (Window.Location.getParameter("id") != null) {
			id = Long.valueOf(Window.Location.getParameter("id"));
		} else if (Window.Location.getParameter("distPrefId") != null) {
			id = Long.valueOf(Window.Location.getParameter("distPrefId"));
		} else if (Window.Location.getParameter("dp") != null) {
			id = Long.valueOf(Window.Location.getParameter("dp"));
		}
		
		iPanel = new SimpleForm();
		iHeader = new UniTimeHeaderPanel(id == null ? COURSE.sectionTitleAddDistributionPreference() : COURSE.sectionTitleEditDistributionPreference());
		
		iHeader.addButton("save", COURSE.actionSaveNewDistributionPreference(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				String error = validate();
				if (error == null) {
					iHeader.clearMessage();
					update();
				} else {
					iHeader.setErrorMessage(error);
				}
			}
		});
		iHeader.getButton("save").setAccessKey(COURSE.accessSaveNewDistributionPreference().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleSaveNewDistributionPreference(COURSE.accessSaveNewDistributionPreference()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", COURSE.actionUpdateDistributionPreference(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				String error = validate();
				if (error == null) {
					iHeader.clearMessage();
					update();
				} else {
					iHeader.setErrorMessage(error);
				}
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.getButton("save").setAccessKey(COURSE.accessUpdateDistributionPreference().charAt(0));
		iHeader.getButton("save").setTitle(COURSE.titleUpdateDistributionPreference(COURSE.accessUpdateDistributionPreference()));
		iHeader.addButton("delete", COURSE.actionDeleteDistributionPreference(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				if (iData.isConfirms()) {
					UniTimeConfirmationDialog.confirm(COURSE.confirmDeleteDistributionPreference(), new Command() {
						@Override
						public void execute() {
							delete();
						}
					});
				} else {
					delete();
				}
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteDistributionPreference().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteDistributionPreference(COURSE.accessDeleteDistributionPreference()));
		iHeader.addButton("back", COURSE.actionBackDistributionPreference(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				if (iData.getBackUrl() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl()); 
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "distributions" +
							(iData.getPreferenceId() == null ? "" : "?backId=" + iData.getPreferenceId() + "&backType=DistributionPref"));
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessBackDistributionPreference().charAt(0));
		iHeader.getButton("back").setTitle(COURSE.titleBackDistributionPreference(COURSE.accessBackDistributionPreference()));
		
		iPanel.addHeaderRow(iHeader);
		iFooter = iHeader.clonePanel();
		iFooter.setHeaderTitle("");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-DistributionsEditPage");
		initWidget(iRootPanel);
		
		DistributionEditRequest request = new DistributionEditRequest();
		request.setPreferenceId(id);
		if (Window.Location.getParameter("classId") != null)
			request.setClassId(Long.valueOf(Window.Location.getParameter("classId")));
		if (Window.Location.getParameter("subpartId") != null)
			request.setSubpartId(Long.valueOf(Window.Location.getParameter("subpartId")));
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<DistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionEditResponse response) {
				LoadingWidget.hideLoading();
				iData = response;
				populate();
			}
		});
	}
	
	protected String validate() {
		if (iData.getDistTypeId() == null)
			return COURSE.errorSelectDistributionType();
		if (iData.getStructureId() == null)
			return COURSE.errorSelectDistributionStructure();
		if (iData.getPrefLevelId() == null)
			return COURSE.errorSelectDistributionPreferenceLevel();
		int count = 0;
		int classes = 0;
		Set<String> selections = new HashSet<String>();
		for (int row = 0; row < iClasses.getRowCount(); row++) {
			DistributionObjectInterface doi = iClasses.getData(row);
			if (doi.isValid()) {
				if (!selections.add(doi.getId()))
					return COURSE.errorInvalidClassSelectionDP();
				count ++;
				if (doi.getClassId() > 0) classes ++;
			} else if (row + 1 < iClasses.getRowCount())
				return COURSE.errorInvalidClassSelectionDP();
		}
		if (count <= 0)
			return COURSE.errorInvalidClassSelectionDPSubpart();
		if (count == 1 && classes > 0)
			return COURSE.errorInvalidClassSelectionDPMinTwoClasses();
		return null;
	}
	
	protected void update() {
		DistributionEditRequest request = new DistributionEditRequest();
		request.setPreferenceId(iData.getPreferenceId());
		if (iData.hasDistributionObjects()) iData.getDistributionObjects().clear();
		for (DistributionObjectInterface doi: iClasses.getData())
			if (doi.isValid())
				iData.addDistributionObject(doi);
		request.setData(iData);
		request.setOperation(Operation.SAVE);
		LoadingWidget.showLoading(MESSAGES.waitSavingRecord());
		RPC.execute(request, new AsyncCallback<DistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionEditResponse response) {
				LoadingWidget.hideLoading();
				if (iData.getBackUrl() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl() +
							(iData.getBackUrl().contains("?") ? "&" : "?") + "backId=" + response.getPreferenceId() + "&backType=DistributionPref"); 
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "distributions?backId=" + response.getPreferenceId() + "&backType=DistributionPref");
			}
		});
	}
	
	protected void delete() {
		DistributionEditRequest request = new DistributionEditRequest();
		request.setPreferenceId(iData.getPreferenceId());
		iData.setDistributionObjects(iClasses.getData());
		request.setOperation(Operation.DELETE);
		LoadingWidget.showLoading(MESSAGES.waitDeletingRecord());
		RPC.execute(request, new AsyncCallback<DistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DistributionEditResponse response) {
				LoadingWidget.hideLoading();
				if (iData.getBackUrl() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl());
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "distributions");
			}
		});
	}
	
	private ListBox iDistrubutionType, iStructure, iPreference;
	private HTML iDistributionDesc, iStructureDesc;

	protected void populate() {
		iDistrubutionType = new ListBox();
		iDistrubutionType.addItem(COURSE.itemSelect(), "");
		iDistrubutionType.getElement().getStyle().setProperty("min-width", "300px");
		iDistributionDesc = new HTML(); iDistributionDesc.addStyleName("unitime-Description");
		for (IdLabel dt: iData.getDistTypes()) {
			iDistrubutionType.addItem(dt.getLabel(), dt.getId().toString());
			if (dt.getId().equals(iData.getDistTypeId())) {
				iDistrubutionType.setSelectedIndex(iDistrubutionType.getItemCount() - 1);
				iDistributionDesc.setHTML(dt.getDescription() == null ? "" : dt.getDescription());
			}
		}
		iDistrubutionType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				Long id = (iDistrubutionType.getSelectedValue().isEmpty() ? null : Long.valueOf(iDistrubutionType.getSelectedValue()));
				iData.setDistTypeId(id);
				IdLabel dt = (id == null ? null : iData.getDistType(id));
				if (dt != null)
					iDistributionDesc.setHTML(dt.getDescription() == null ? "" : dt.getDescription());
				else
					iDistributionDesc.setHTML("");
				iPreference.clear();
				iPreference.addItem(COURSE.itemSelect(), "");
				for (IdLabel st: iData.getPrefLevels()) {
					if (!dt.getAllowedPrefs().contains(st.getDescription())) continue;
					iPreference.addItem(st.getLabel(), st.getId().toString());
					if (st.getId().equals(iData.getPrefLevelId())) {
						iPreference.setSelectedIndex(iPreference.getItemCount() - 1);
					}
				}
				iData.setPrefLevelId(iPreference.getSelectedValue().isEmpty() ? null : Long.valueOf(iPreference.getSelectedValue()));
			}
		});
		P dtp = new P(); dtp.add(iDistrubutionType); dtp.add(iDistributionDesc);
		iPanel.addRow(COURSE.propertyDistributionType(), dtp);
		
		iStructure = new ListBox();
		iStructure.getElement().getStyle().setProperty("min-width", "300px");
		iStructure.addItem(COURSE.itemSelect(), "");
		iStructureDesc = new HTML(); iStructureDesc.addStyleName("unitime-Description");
		for (IdLabel st: iData.getStructures()) {
			iStructure.addItem(st.getLabel(), st.getId().toString());
			if (st.getId().equals(iData.getStructureId())) {
				iStructure.setSelectedIndex(iStructure.getItemCount() - 1);
				iStructureDesc.setHTML(st.getDescription() == null ? "" : st.getDescription());
			}
		}
		iStructure.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				Long id = (iStructure.getSelectedValue().isEmpty() ? null : Long.valueOf(iStructure.getSelectedValue()));
				iData.setStructureId(id);
				IdLabel st = (id == null ? null : iData.getStructure(id));
				if (st != null)
					iStructureDesc.setHTML(st.getDescription() == null ? "" : st.getDescription());
				else
					iStructureDesc.setHTML("");
			}
		});
		P stp = new P(); stp.add(iStructure); stp.add(iStructureDesc);
		iPanel.addRow(COURSE.propertyDistributionStructure(), stp);
		
		iPreference = new ListBox();
		iPreference.getElement().getStyle().setProperty("min-width", "300px");
		iPreference.addItem(COURSE.itemSelect(), "");
		IdLabel dt = (iData.getDistTypeId() == null ? null : iData.getDistType(iData.getDistTypeId()));
		for (IdLabel st: iData.getPrefLevels()) {
			if (dt != null && !dt.getAllowedPrefs().contains(st.getDescription())) continue;
			iPreference.addItem(st.getLabel(), st.getId().toString());
			if (st.getId().equals(iData.getPrefLevelId())) {
				iPreference.setSelectedIndex(iPreference.getItemCount() - 1);
			}
		}
		iPreference.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				iData.setPrefLevelId(iPreference.getSelectedValue().isEmpty() ? null : Long.valueOf(iPreference.getSelectedValue()));
			}
		});
		iPanel.addRow(COURSE.propertyDistributionPreference(), iPreference);
		
		iClasses = new UniTimeTable<PrefGroupEditInterface.DistributionObjectInterface>();
		UniTimeHeaderPanel header = new UniTimeHeaderPanel(COURSE.sectionTitleClassesInDistribution());
		iPanel.addHeaderRow(header);
		header.addButton("add", COURSE.actionAddClassToDistribution(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				DistributionObjectInterface doi = new DistributionObjectInterface();
				iClasses.addRow(doi, toClassRow(doi));
				updateButtons();
			}
		});
		iPanel.addRow(iClasses);
		
		if (iData.hasDistributionObjects()) {
			for (DistributionObjectInterface doi: iData.getDistributionObjects())
				iClasses.addRow(doi, toClassRow(doi));
		}
		if (iClasses.getRowCount() == 0) {
			DistributionObjectInterface doi = new DistributionObjectInterface();
			if (Window.Location.getParameter("subjectId") != null)
				doi.setSubjectId(Long.valueOf(Window.Location.getParameter("subjectId")));
			iClasses.addRow(doi, toClassRow(doi));
		}
		updateButtons();
		
		iPanel.addBottomRow(iFooter);
		
		iHeader.setEnabled("save", iData.getPreferenceId() == null);
		iHeader.setEnabled("update", iData.getPreferenceId() != null);
		iHeader.setEnabled("delete", iData.getPreferenceId() != null && iData.isCanDelete());
		iHeader.setEnabled("back", true);
	}
	
	protected void updateButtons() {
		for (int i = 0; i < iClasses.getRowCount(); i++) {
			ImageButton up = (ImageButton)iClasses.getWidget(i, 4);
			ImageButton down = (ImageButton)iClasses.getWidget(i, 5);
			ImageButton delete = (ImageButton)iClasses.getWidget(i, 6);
			up.setVisible(i > 0);
			down.setVisible(i + 1 < iClasses.getRowCount());
			delete.setVisible(iClasses.getRowCount() > 1);
		}
	}
	
	protected List<Widget> toClassRow(final DistributionObjectInterface doi) {
		final List<Widget> row = new ArrayList<Widget>();
		final ListBox subject = new ListBox();
		subject.addItem("-", ""); subject.setWidth("90px");
		row.add(subject);
		final ListBox course = new ListBox();
		course.addItem("-", ""); course.setWidth("470px");
		row.add(course);		
		final ListBox subpart = new ListBox();
		subpart.addItem("-", ""); subpart.setWidth("150px");
		row.add(subpart);
		final ListBox clazz = new ListBox();
		clazz.addItem("-", ""); clazz.setWidth("150px");
		row.add(clazz);
		for (IdLabel s: iData.getSubjects()) {
			subject.addItem(s.getLabel(), s.getId().toString());
			if (s.getId().equals(doi.getSubjectId()))
				subject.setSelectedIndex(subject.getItemCount() - 1);
		}
		if (doi.getSubjectId() != null && subject.getSelectedIndex() <= 0) {
			subject.addItem(doi.getSubject(), doi.getSubjectId().toString());
			subject.setSelectedIndex(subject.getItemCount() - 1);
			subject.setEnabled(false);
			course.setEnabled(false);
			subject.setEnabled(false);
			clazz.setEnabled(false);
		}
		subject.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setSubjectId(subject.getSelectedIndex() <= 0 ? null : Long.valueOf(subject.getSelectedValue()));
				subjectChanged(row, doi);
			}
		});
		course.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setCourseId(course.getSelectedIndex() <= 0 ? null : Long.valueOf(course.getSelectedValue()));
				courseChanged(row, doi);
			}
		});
		subpart.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setSubpartId(subpart.getSelectedIndex() <= 0 ? null : Long.valueOf(subpart.getSelectedValue()));
				subpartChanged(row, doi);
			}
		});
		clazz.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setClassId(clazz.getSelectedIndex() <= 0 ? null : Long.valueOf(clazz.getSelectedValue()));
				if (doi.getClassId() != null && iClasses.getRowForWidget(clazz) == iClasses.getRowCount() - 1) {
					DistributionObjectInterface doi = new DistributionObjectInterface();
					iClasses.addRow(doi, toClassRow(doi));
					updateButtons();
				}
			}
		});
		if (subject.getSelectedIndex() <= 0 && subject.getItemCount() == 2) {
			subject.setSelectedIndex(1);
			doi.setSubjectId(subject.getSelectedIndex() <= 0 ? null : Long.valueOf(subject.getSelectedValue()));
		}
		if (subject.getSelectedIndex() > 0)
			subjectChanged(row, doi);
		
		final ImageButton up = new ImageButton(RESOURCES.orderUp());
		up.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iClasses.getRowForWidget(up);
				if (row > 0) iClasses.swapRows(row - 1, row);
				updateButtons();
			}
		});
		row.add(up);
		
		ImageButton down = new ImageButton(RESOURCES.orderDown());
		down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iClasses.getRowForWidget(down);
				if (row + 1 < iClasses.getRowCount()) iClasses.swapRows(row, row + 1);
				updateButtons();
			}
		});
		row.add(down);
		
		ImageButton delete = new ImageButton(RESOURCES.delete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iClasses.getRowForWidget(delete);
				iClasses.removeRow(row);
				updateButtons();
			}
		});
		row.add(delete);
		
		return row;
	}
	
	protected void subjectChanged(List<Widget> row, DistributionObjectInterface doi) {
		final ListBox course = (ListBox) row.get(1);
		final ListBox subpart = (ListBox) row.get(2);
		final ListBox clazz = (ListBox) row.get(3);
		course.clear();
		course.addItem("-", "");
		subpart.clear();
		subpart.addItem("-", "");
		clazz.clear();
		clazz.addItem("-", "");
		if (doi.getSubjectId() != null) {
			DistributionsLookupCourses req = new DistributionsLookupCourses();
			req.setSubjectId(doi.getSubjectId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					course.clear();
					course.addItem("-", "");
					for (IdLabel item: list) {
						course.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getCourseId())) {
							course.setSelectedIndex(course.getItemCount() - 1);
							courseChanged(row, doi);
						}
					}
					if (course.getSelectedIndex() <= 0 && course.getItemCount() == 2) {
						course.setSelectedIndex(1);
						doi.setCourseId(course.getSelectedIndex() <= 0 ? null : Long.valueOf(course.getSelectedValue()));
						courseChanged(row, doi);
					}
				}
			});
		}
	}
	
	protected void courseChanged(List<Widget> row, DistributionObjectInterface doi) {
		final ListBox subpart = (ListBox) row.get(2);
		final ListBox clazz = (ListBox) row.get(3);
		subpart.clear();
		subpart.addItem("-", "");
		clazz.clear();
		clazz.addItem("-", "");
		if (doi.getCourseId() != null) {
			DistributionsLookupSubparts req = new DistributionsLookupSubparts();
			req.setCourseId(doi.getCourseId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					subpart.clear();
					subpart.addItem("-", "");
					for (IdLabel item: list) {
						subpart.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getSubpartId())) {
							subpart.setSelectedIndex(subpart.getItemCount() - 1);
							subpartChanged(row, doi);
						}
					}
					if (subpart.getSelectedIndex() <= 0 && subpart.getItemCount() == 2) {
						subpart.setSelectedIndex(1);
						doi.setSubpartId(subpart.getSelectedIndex() <= 0 ? null : Long.valueOf(subpart.getSelectedValue()));
						subpartChanged(row, doi);
					}
				}
			});
		}
	}
	
	protected void subpartChanged(List<Widget> row, DistributionObjectInterface doi) {
		final ListBox clazz = (ListBox) row.get(3);
		clazz.clear();
		clazz.addItem("-", "");
		if (doi.getSubpartId() != null) {
			DistributionsLookupClasses req = new DistributionsLookupClasses();
			req.setSubpartId(doi.getSubpartId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					clazz.clear();
					clazz.addItem("-", "");
					for (IdLabel item: list) {
						clazz.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getClassId()))
							clazz.setSelectedIndex(clazz.getItemCount() - 1);
					}
					if (clazz.getSelectedIndex() <= 0) {
						clazz.setSelectedIndex(1);
						doi.setClassId(clazz.getSelectedIndex() <= 0 ? null : Long.valueOf(clazz.getSelectedValue()));
						if (doi.getClassId() != null && iClasses.getRowForWidget(clazz) == iClasses.getRowCount() - 1) {
							DistributionObjectInterface doi = new DistributionObjectInterface();
							iClasses.addRow(doi, toClassRow(doi));
							updateButtons();
						}
					}
				}
			});
		}
	}
}
