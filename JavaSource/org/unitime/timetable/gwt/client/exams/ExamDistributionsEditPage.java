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
package org.unitime.timetable.gwt.client.exams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditRequest;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditRequest.Operation;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionEditResponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionObjectInterface;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsLookupCourses;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDistributionsLookupExams;
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

public class ExamDistributionsEditPage extends Composite {
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter, iExamsHeader;
	private ExamDistributionEditResponse iData;
	private UniTimeTable<ExamDistributionObjectInterface> iExams;
	
	public ExamDistributionsEditPage() {
		Long id = null;
		if (Window.Location.getParameter("id") != null) {
			id = Long.valueOf(Window.Location.getParameter("id"));
		} else if (Window.Location.getParameter("distPrefId") != null) {
			id = Long.valueOf(Window.Location.getParameter("distPrefId"));
		} else if (Window.Location.getParameter("dp") != null) {
			id = Long.valueOf(Window.Location.getParameter("dp"));
		}
		
		iPanel = new SimpleForm();
		iHeader = new UniTimeHeaderPanel(id == null ? EXAM.sectionAddDistributionPreference() : EXAM.sectionEditDistributionPreference());
		
		iHeader.addButton("save", EXAM.actionSaveNewDistributionPreference(), new ClickHandler() {
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
		iHeader.getButton("save").setAccessKey(EXAM.accessSaveNewDistributionPreference().charAt(0));
		iHeader.getButton("save").setTitle(EXAM.titleSaveNewDistributionPreference(EXAM.accessSaveNewDistributionPreference()));
		iHeader.setEnabled("save", false);
		iHeader.addButton("update", EXAM.actionUpdateDistributionPreference(), new ClickHandler() {
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
		iHeader.getButton("save").setAccessKey(EXAM.accessUpdateDistributionPreference().charAt(0));
		iHeader.getButton("save").setTitle(EXAM.titleUpdateDistributionPreference(EXAM.accessUpdateDistributionPreference()));
		iHeader.addButton("delete", EXAM.actionDeleteDistributionPreference(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				if (iData.isConfirms()) {
					UniTimeConfirmationDialog.confirm(EXAM.confirmDeleteDistributionPreference(), new Command() {
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
		iHeader.getButton("delete").setAccessKey(EXAM.accessDeleteDistributionPreference().charAt(0));
		iHeader.getButton("delete").setTitle(EXAM.titleDeleteDistributionPreference(EXAM.accessDeleteDistributionPreference()));
		iHeader.addButton("back", EXAM.actionBackDistributionPreference(), new ClickHandler() {
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
		iHeader.getButton("back").setAccessKey(EXAM.accessBackDistributionPreference().charAt(0));
		iHeader.getButton("back").setTitle(EXAM.titleBackDistributionPreference(EXAM.accessBackDistributionPreference()));
		
		iPanel.addHeaderRow(iHeader);
		iFooter = iHeader.clonePanel();
		iFooter.setHeaderTitle("");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-DistributionsEditPage");
		initWidget(iRootPanel);
		
		ExamDistributionEditRequest request = new ExamDistributionEditRequest();
		request.setPreferenceId(id);
		if (Window.Location.getParameter("typeId") != null)
			request.setTypeId(Long.valueOf(Window.Location.getParameter("type")));
		if (Window.Location.getParameter("examId") != null)
			request.setExamId(Long.valueOf(Window.Location.getParameter("examId")));
		LoadingWidget.showLoading(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<ExamDistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExamDistributionEditResponse response) {
				LoadingWidget.hideLoading();
				iData = response;
				populate();
			}
		});
	}
	
	protected String validate() {
		if (iData.getExamTypeId() == null)
			return EXAM.messageNoExamType();
		if (iData.getDistTypeId() == null)
			return EXAM.errorSelectDistributionType();
		if (iData.getPrefLevelId() == null)
			return EXAM.errorSelectPreferenceLevel();
		int count = 0;
		Set<Long> selections = new HashSet<Long>();
		for (int row = 0; row < iExams.getRowCount(); row++) {
			ExamDistributionObjectInterface doi = iExams.getData(row);
			if (doi.getExamId() != null) {
				if (!selections.add(doi.getExamId()))
					return EXAM.errorInvalidClassSelectionDP();
				count ++;
			} else if (row + 1 < iExams.getRowCount())
				return EXAM.errorInvalidClassSelectionDP();
		}
		if (count <= 1)
			return EXAM.errorInvalidClassSelectionDPMinTwoExams();
		return null;
	}
	
	protected void update() {
		ExamDistributionEditRequest request = new ExamDistributionEditRequest();
		request.setPreferenceId(iData.getPreferenceId());
		if (iData.hasDistributionObjects()) iData.getDistributionObjects().clear();
		for (ExamDistributionObjectInterface doi: iExams.getData())
			if (doi.getExamId() != null)
				iData.addDistributionObject(doi);
		request.setData(iData);
		request.setOperation(Operation.SAVE);
		LoadingWidget.showLoading(MESSAGES.waitSavingRecord());
		RPC.execute(request, new AsyncCallback<ExamDistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExamDistributionEditResponse response) {
				LoadingWidget.hideLoading();
				if (iData.getBackUrl() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl() +
							(iData.getBackUrl().contains("?") ? "&" : "?") + "backId=" + response.getPreferenceId() + "&backType=DistributionPref"); 
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "examDistributions?backId=" + response.getPreferenceId() + "&backType=DistributionPref");
			}
		});
	}
	
	protected void delete() {
		ExamDistributionEditRequest request = new ExamDistributionEditRequest();
		request.setPreferenceId(iData.getPreferenceId());
		iData.setDistributionObjects(iExams.getData());
		request.setOperation(Operation.DELETE);
		LoadingWidget.showLoading(MESSAGES.waitDeletingRecord());
		RPC.execute(request, new AsyncCallback<ExamDistributionEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.hideLoading();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(ExamDistributionEditResponse response) {
				LoadingWidget.hideLoading();
				if (iData.getBackUrl() != null)
					ToolBox.open(GWT.getHostPageBaseURL() + iData.getBackUrl());
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "examDistributions");
			}
		});
	}
	
	private ListBox iDistrubutionType, iExamType, iPreference;
	private HTML iDistributionDesc;

	protected void populate() {
		iExamType = new ListBox();
		iExamType.getElement().getStyle().setProperty("min-width", "30px");
		for (IdLabel st: iData.getExamTypes()) {
			iExamType.addItem(st.getLabel(), st.getId().toString());
			if (st.getId().equals(iData.getExamTypeId())) {
				iExamType.setSelectedIndex(iExamType.getItemCount() - 1);
			}
		}
		if (iData.getExamTypeId() == null)
			iData.setExamTypeId(Long.valueOf(iExamType.getSelectedValue()));
		if (iData.getExamTypes().size() == 1)
			iExamType.setEnabled(false);
		iExamType.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				Long id = (iExamType.getSelectedValue().isEmpty() ? null : Long.valueOf(iExamType.getSelectedValue()));
				iData.setExamTypeId(id);
				while (iExams.getRowCount() > 0)
					iExams.removeRow(0);
				ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
				iExams.addRow(doi, toClassRow(doi));
				iExamsHeader.setHeaderTitle(EXAM.sectionExaminationsInDistribution(iExamType.getSelectedItemText()));
			}
		});
		iPanel.addRow(EXAM.propExamType(), iExamType);

		iDistrubutionType = new ListBox();
		iDistrubutionType.addItem(EXAM.itemSelect(), "");
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
				iPreference.addItem(EXAM.itemSelect(), "");
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
		iPanel.addRow(EXAM.propertyDistributionType(), dtp);
		
		iPreference = new ListBox();
		iPreference.getElement().getStyle().setProperty("min-width", "300px");
		iPreference.addItem(EXAM.itemSelect(), "");
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
		iPanel.addRow(EXAM.propertyDistributionPreference(), iPreference);
		
		iExams = new UniTimeTable<ExamDistributionObjectInterface>();
		iExamsHeader = new UniTimeHeaderPanel(EXAM.sectionExaminationsInDistribution(iExamType.getSelectedItemText()));
		iPanel.addHeaderRow(iExamsHeader);
		iExamsHeader.addButton("add", EXAM.actionAddExamToDistribution(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
				iExams.addRow(doi, toClassRow(doi));
				updateButtons();
			}
		});
		iPanel.addRow(iExams);
		
		if (iData.hasDistributionObjects()) {
			for (ExamDistributionObjectInterface doi: iData.getDistributionObjects())
				iExams.addRow(doi, toClassRow(doi));
		}
		if (iExams.getRowCount() == 0) {
			ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
			if (Window.Location.getParameter("subjectId") != null)
				doi.setSubjectId(Long.valueOf(Window.Location.getParameter("subjectId")));
			iExams.addRow(doi, toClassRow(doi));
		}
		updateButtons();
		
		iPanel.addBottomRow(iFooter);
		
		iHeader.setEnabled("save", iData.getPreferenceId() == null);
		iHeader.setEnabled("update", iData.getPreferenceId() != null);
		iHeader.setEnabled("delete", iData.getPreferenceId() != null && iData.isCanDelete());
		iHeader.setEnabled("back", true);
	}
	
	protected void updateButtons() {
		for (int i = 0; i < iExams.getRowCount(); i++) {
			ImageButton up = (ImageButton)iExams.getWidget(i, 3);
			ImageButton down = (ImageButton)iExams.getWidget(i, 4);
			ImageButton delete = (ImageButton)iExams.getWidget(i, 5);
			up.setVisible(i > 0);
			down.setVisible(i + 1 < iExams.getRowCount());
			delete.setVisible(iExams.getRowCount() > 1);
		}
	}
	
	protected List<Widget> toClassRow(final ExamDistributionObjectInterface doi) {
		final List<Widget> row = new ArrayList<Widget>();
		final ListBox subject = new ListBox();
		subject.addItem("-", ""); subject.setWidth("100px");
		row.add(subject);
		final ListBox course = new ListBox();
		course.addItem("-", ""); course.setWidth("400px");
		row.add(course);		
		final ListBox exam = new ListBox();
		exam.addItem("-", ""); exam.setWidth("400px");
		row.add(exam);
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
			exam.setEnabled(false);
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
		exam.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent e) {
				doi.setExamId(Long.valueOf(exam.getSelectedValue()));
				if (doi.getExamId() != null && iExams.getRowForWidget(exam) == iExams.getRowCount() - 1) {
					ExamDistributionObjectInterface doi = new ExamDistributionObjectInterface();
					iExams.addRow(doi, toClassRow(doi));
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
				int row = iExams.getRowForWidget(up);
				if (row > 0) iExams.swapRows(row - 1, row);
				updateButtons();
			}
		});
		row.add(up);
		
		ImageButton down = new ImageButton(RESOURCES.orderDown());
		down.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iExams.getRowForWidget(down);
				if (row + 1 < iExams.getRowCount()) iExams.swapRows(row, row + 1);
				updateButtons();
			}
		});
		row.add(down);
		
		ImageButton delete = new ImageButton(RESOURCES.delete());
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent e) {
				int row = iExams.getRowForWidget(delete);
				iExams.removeRow(row);
				updateButtons();
			}
		});
		row.add(delete);
		
		return row;
	}
	
	protected void subjectChanged(List<Widget> row, ExamDistributionObjectInterface doi) {
		final ListBox course = (ListBox) row.get(1);
		final ListBox exam = (ListBox) row.get(2);
		course.clear();
		course.addItem("-", "");
		exam.clear();
		exam.addItem("-", "");
		if (doi.getSubjectId() != null) {
			ExamDistributionsLookupCourses req = new ExamDistributionsLookupCourses();
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
	
	protected void courseChanged(List<Widget> row, ExamDistributionObjectInterface doi) {
		final ListBox exam = (ListBox) row.get(2);
		exam.clear();
		exam.addItem("-", "");
		if (doi.getCourseId() != null) {
			ExamDistributionsLookupExams req = new ExamDistributionsLookupExams();
			req.setExamTypeId(iData.getExamTypeId());
			req.setCourseId(doi.getCourseId());
			RPC.execute(req, new AsyncCallback<GwtRpcResponseList<IdLabel>>() {
				@Override
				public void onFailure(Throwable e) {
					UniTimeNotifications.error(e.getMessage(), e);
				}
				@Override
				public void onSuccess(GwtRpcResponseList<IdLabel> list) {
					exam.clear();
					exam.addItem("-", "");
					for (IdLabel item: list) {
						exam.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(doi.getExamId())) {
							exam.setSelectedIndex(exam.getItemCount() - 1);
						}
					}
					if (exam.getSelectedIndex() <= 0 && exam.getItemCount() == 2) {
						exam.setSelectedIndex(1);
						doi.setExamId(exam.getSelectedIndex() <= 0 ? null : Long.valueOf(exam.getSelectedValue()));
					}
				}
			});
		}
	}
}
