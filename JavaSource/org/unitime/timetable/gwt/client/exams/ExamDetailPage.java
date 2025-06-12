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

import org.unitime.localization.messages.CourseMessages;
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDetailReponse;
import org.unitime.timetable.gwt.client.exams.ExamsInterface.ExamDetailRequest;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class ExamDetailPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final ExaminationMessages EXAM = GWT.create(ExaminationMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ExamDetailReponse iResponse;
	
	public ExamDetailPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ExamDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("examId");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(EXAM.errorNoExamId());
		} else {
			load(Long.valueOf(id), null);
		}
		
		iHeader.addButton("edit", EXAM.actionExamEdit(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "examEdit.action?examId=" + iResponse.getExamId());
			}
		});
		iHeader.setEnabled("edit", false);
		iHeader.getButton("edit").setAccessKey(EXAM.accessExamEdit().charAt(0));
		iHeader.getButton("edit").setTitle(EXAM.titleExamEdit());
		
		iHeader.addButton("clone", EXAM.actionExamClone(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "examEdit.action?examId=" + iResponse.getExamId() + "&clone=true");
			}
		});
		iHeader.setEnabled("clone", false);
		iHeader.getButton("clone").setAccessKey(EXAM.accessExamClone().charAt(0));
		iHeader.getButton("clone").setTitle(EXAM.titleExamClone());
		
		iHeader.addButton("assign", EXAM.actionExamAssign(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				UniTimeFrameDialog.openDialog(EXAM.dialogExamAssign(),
						"examInfo.action?examId=" + iResponse.getExamId(),
						"900", "90%");
			}
		});
		iHeader.setEnabled("assign", false);
		iHeader.getButton("assign").setAccessKey(EXAM.accessExamAssign().charAt(0));
		iHeader.getButton("assign").setTitle(EXAM.titleExamAssign());
		
		iHeader.addButton("delete", EXAM.actionExamDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				if (iResponse.isConfirms()) {
					UniTimeConfirmationDialog.confirm(EXAM.confirmExamDelete(), new Command() {
						@Override
						public void execute() {
							load(iResponse.getExamId(), ExamDetailRequest.Action.DELETE);
						}
					});
				} else {
					load(iResponse.getExamId(), ExamDetailRequest.Action.DELETE);
				}
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.getButton("delete").setAccessKey(EXAM.accessExamDelete().charAt(0));
		iHeader.getButton("delete").setTitle(EXAM.titleExamDelete());
		
		iHeader.addButton("previous", EXAM.actionExamPrevious(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "examination?id=" + iResponse.getPreviousId());
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.getButton("previous").setAccessKey(EXAM.accessExamPrevious().charAt(0));
		iHeader.getButton("previous").setTitle(EXAM.titleExamPrevious());
		
		iHeader.addButton("next", EXAM.actionExamNext(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "examination?id=" + iResponse.getNextId());
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.getButton("next").setAccessKey(EXAM.accessExamNext().charAt(0));
		iHeader.getButton("next").setTitle(EXAM.titleExamNext());
		
		iHeader.addButton("back", EXAM.actionExamBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "back.action?uri=" + URL.encodeQueryString(iResponse.getBackUrl()) +
						"&backId=" + iResponse.getExamId() + "&backType=PreferenceGroup");
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(EXAM.accessExamBack().charAt(0));

		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long examId, ExamDetailRequest.Action action) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		ExamDetailRequest req = new ExamDetailRequest();
		req.setExamId(examId);
		req.setAction(action);
		RPC.execute(req, new AsyncCallback<ExamDetailReponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ExamDetailReponse response) {
				iResponse = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.setHeaderTitle(response.getExamName());
				
				for (PropertyInterface property: response.getProperties().getProperties())
					iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				if (response.hasOwners()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getOwners().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getOwners()));
				}

				if (response.hasAssignment()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getAssignment().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getAssignment()));
				}

				if (response.hasPreferences()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(COURSE.sectionTitlePreferences());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getPreferences()));
				}
				
				if (response.hasDistributions()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getDistributions().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getDistributions()));
					if (response.hasOperation("add-distribution")) {
						hp.addButton("add-distribution", EXAM.actionAddDistributionPreference(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent evt) {
								ToolBox.open(GWT.getHostPageBaseURL() + "examDistributionPrefs.action?examId=" + iResponse.getExamId() + "&op=" +
										URL.encodeQueryString(EXAM.actionAddDistributionPreference())
										);
							}
						});
						hp.getButton("add-distribution").setAccessKey(EXAM.accessAddDistributionPreference().charAt(0));
						hp.getButton("add-distribution").setTitle(EXAM.titleAddDistributionPreference(EXAM.accessAddDistributionPreference()));					
					}
				}
				
				iPanel.addRow(new ExaminationEnrollmentTable(true, true).forExamId(response.getExamId()));
				
				iPanel.addBottomRow(iFooter);
				
				for (String op: iHeader.getOperations())
					iHeader.setEnabled(op, response.hasOperation(op));
				if (response.hasBackTitle()) {
					iHeader.getButton("back").setTitle(EXAM.titleExamBack().replace("%%", response.getBackTitle()));
					iFooter.getButton("back").setTitle(EXAM.titleExamBack().replace("%%", response.getBackTitle()));
				}
				UniTimeNavigation.getInstance().refresh();
			}
		});
	}

}
