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

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassDetailReponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.ClassDetailRequest;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFrameDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class ClassDetailPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private ClassDetailReponse iResponse;
	
	public ClassDetailPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-ClassDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("cid");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoClassId());
		} else {
			load(Long.valueOf(id), null);
		}
		
		iHeader.addButton("edit", COURSE.actionEditClass(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "classEdit.action?cid=" + iResponse.getClassId());
			}
		});
		iHeader.setEnabled("edit", false);
		iHeader.getButton("edit").setAccessKey(COURSE.accessEditClass().charAt(0));
		iHeader.getButton("edit").setTitle(COURSE.titleEditClass(COURSE.accessEditClass()));
		
		iHeader.addButton("assign", COURSE.actionOpenClassAssignmentDialog(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				UniTimeFrameDialog.openDialog(COURSE.dialogClassAssignment(),
						"classInfo.action?classId=" + iResponse.getClassId(),
						"900", "90%");
			}
		});
		iHeader.setEnabled("assign", false);
		iHeader.getButton("assign").setAccessKey(COURSE.accessOpenClassAssignmentDialog().charAt(0));
		iHeader.getButton("assign").setTitle(COURSE.titleOpenClassAssignmentDialog(COURSE.accessOpenClassAssignmentDialog()));
		
		iHeader.addButton("previous", COURSE.actionPreviousClass(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "clazz?id=" + iResponse.getPreviousId());
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousClass().charAt(0));
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousClass(COURSE.accessPreviousClass()));
		
		iHeader.addButton("next", COURSE.actionNextClass(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "clazz?id=" + iResponse.getNextId());
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.getButton("next").setAccessKey(COURSE.accessNextClass().charAt(0));
		iHeader.getButton("next").setTitle(COURSE.titleNextClass(COURSE.accessNextClass()));
		
		iHeader.addButton("back", COURSE.actionBackClassDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "back.action?uri=" + URL.encodeQueryString(iResponse.getBackUrl()) +
						"&backId=" + iResponse.getClassId() + "&backType=PreferenceGroup");
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessBackClassDetail().charAt(0));

		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long classId, ClassDetailRequest.Action action) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		ClassDetailRequest req = new ClassDetailRequest();
		req.setClassId(classId);
		req.setAction(action);
		RPC.execute(req, new AsyncCallback<ClassDetailReponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final ClassDetailReponse response) {
				iResponse = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.getHeaderTitlePanel().clear();
				Anchor anchor = new Anchor(response.getCourseName());
				anchor.setHref("offering?io=" + response.getOfferingId());
				anchor.setAccessKey(COURSE.accessInstructionalOfferingDetail().charAt(0));
				anchor.setTitle(COURSE.titleInstructionalOfferingDetail(COURSE.accessInstructionalOfferingDetail()));
				anchor.setStyleName("l8");
				iHeader.getHeaderTitlePanel().add(anchor);
				P colon = new P(DOM.createSpan()); colon.setText(": ");
				iHeader.getHeaderTitlePanel().add(colon);
				anchor = new Anchor(response.getSubparName());
				anchor.setHref("subpart?id=" + response.getSubpartgId());
				anchor.setAccessKey(COURSE.accessSchedulingSubpartDetail().charAt(0));
				anchor.setTitle(COURSE.titleSchedulingSubpartDetail(COURSE.accessSchedulingSubpartDetail()));
				anchor.setStyleName("l8");
				iHeader.getHeaderTitlePanel().add(anchor);
				P suffix = new P(DOM.createSpan()); suffix.setText(response.getClassName());
				iHeader.getHeaderTitlePanel().add(suffix);
				
				for (PropertyInterface property: response.getProperties().getProperties())
					iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				
				if (response.hasTimetable()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getTimetable().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getTimetable()));
				}

				if (response.hasConclicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getConflicts()));
				}

				
				if (response.hasEventConclicts()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getEventConflicts().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getEventConflicts()));
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
						hp.addButton("add-distribution", COURSE.actionAddDistributionPreference(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent evt) {
								ToolBox.open(GWT.getHostPageBaseURL() + "distributionAdd?classId=" + iResponse.getClassId());
							}
						});
						hp.getButton("add-distribution").setAccessKey(COURSE.accessAddDistributionPreference().charAt(0));
						hp.getButton("add-distribution").setTitle(COURSE.titleAddDatePatternPreference(COURSE.accessAddDistributionPreference()));					
					}
					if (response.hasOperation("add-distribution-legacy")) {
						hp.addButton("add-distribution-legacy", COURSE.actionAddDistributionPreference(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent evt) {
								ToolBox.open(GWT.getHostPageBaseURL() + "distributionPrefs.action?op=" +
										URL.encodeQueryString(COURSE.actionAddDistributionPreference()) + "&classId=" + iResponse.getClassId()
										);
							}
						});
						hp.getButton("add-distribution-legacy").setAccessKey(COURSE.accessAddDistributionPreference().charAt(0));
						hp.getButton("add-distribution-legacy").setTitle(COURSE.titleAddDatePatternPreference(COURSE.accessAddDistributionPreference()));					
					}
				}
				
				if (response.hasExaminations()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getExaminations().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getExaminations()));
					if (response.hasOperation("add-exam")) {
						hp.addButton("add-exam", COURSE.actionAddExamination(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent evt) {
								ToolBox.open(GWT.getHostPageBaseURL() + "examEdit.action?firstType=Class_&firstId=" + response.getClassId());
							}
						});
						hp.getButton("add-exam").setAccessKey(COURSE.accessAddExamination().charAt(0));
						hp.getButton("add-exam").setTitle(COURSE.titleAddExamination(COURSE.accessAddExamination()));					
					}
				}
				
				iPanel.addRow(new EnrollmentTable(true, true, true).forClassId(response.getClassId()));
				
				iPanel.addBottomRow(iFooter);
				
				for (String op: iHeader.getOperations())
					iHeader.setEnabled(op, response.hasOperation(op));
				if (response.hasBackTitle()) {
					iHeader.getButton("back").setTitle(COURSE.navigationBackTitle(response.getBackTitle()));
					iFooter.getButton("back").setTitle(COURSE.navigationBackTitle(response.getBackTitle()));
				}
				UniTimeNavigation.getInstance().refresh();
			}
		});
	}

}
