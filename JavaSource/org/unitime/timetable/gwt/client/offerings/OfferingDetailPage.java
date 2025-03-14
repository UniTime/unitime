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
import org.unitime.timetable.gwt.client.curricula.CourseCurriculaTable;
import org.unitime.timetable.gwt.client.instructor.TeachingRequestsWidget;
import org.unitime.timetable.gwt.client.instructor.survey.OfferingDetailWidget;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingConfigInterface;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingDetailRequest;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingDetailResponse;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.reservations.ReservationTable;
import org.unitime.timetable.gwt.client.sectioning.EnrollmentTable;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class OfferingDetailPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private int iTeachingRequestsRow = -1, iReservationsRow = -1;
	private OfferingDetailResponse iResponse;
	
	public OfferingDetailPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-OfferingDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		if (Window.Location.getParameter("io") == null) {
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoOfferingId());
		} else {
			load(Long.valueOf(Window.Location.getParameter("io")), null);
		}
		
		iHeader.addButton("lock", COURSE.actionLockIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getOfferingId(), OfferingDetailRequest.Action.Lock);
			}
		});
		iHeader.setEnabled("lock", false);
		iHeader.getButton("lock").setAccessKey(COURSE.accessLockIO().charAt(0));
		iHeader.getButton("lock").setTitle(COURSE.titleLockIO(COURSE.accessLockIO()));
		
		iHeader.addButton("unlock", COURSE.actionUnlockIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getOfferingId(), OfferingDetailRequest.Action.Unlock);
			}
		});
		iHeader.setEnabled("unlock", false);
		iHeader.getButton("unlock").setAccessKey(COURSE.accessUnlockIO().charAt(0));
		iHeader.getButton("unlock").setTitle(COURSE.titleUnlockIO(COURSE.accessUnlockIO()));

		iHeader.addButton("add-config", COURSE.actionAddConfiguration(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.action?page=instrOfferingConfig&op=" + URL.encodeQueryString(COURSE.actionAddConfiguration()) + "&offering=" + iResponse.getOfferingId());
			}
		});
		iHeader.setEnabled("add-config", false);
		iHeader.getButton("add-config").setAccessKey(COURSE.accessAddConfiguration().charAt(0));
		iHeader.getButton("add-config").setTitle(COURSE.titleAddConfiguration(COURSE.accessAddConfiguration()));
		
		iHeader.addButton("cross-list", COURSE.actionCrossLists(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "crossListsModify.action?instrOfferingId=" + iResponse.getOfferingId() + "&uid=" + iResponse.getCourseId() + "&op=" + URL.encodeQueryString(COURSE.actionCrossLists()));
			}
		});
		iHeader.setEnabled("cross-list", false);
		iHeader.getButton("cross-list").setAccessKey(COURSE.accessCrossLists().charAt(0));
		iHeader.getButton("cross-list").setTitle(COURSE.titleCrossLists(COURSE.accessCrossLists()));

		iHeader.addButton("make-offered", COURSE.actionMakeOffered(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getOfferingId(), OfferingDetailRequest.Action.MakeOffered);
			}
		});
		iHeader.setEnabled("make-offered", false);
		iHeader.getButton("make-offered").setAccessKey(COURSE.accessMakeOffered().charAt(0));
		iHeader.getButton("make-offered").setTitle(COURSE.titleMakeOffered(COURSE.accessMakeOffered()));

		iHeader.addButton("make-not-offered", COURSE.actionMakeNotOffered(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getOfferingId(), OfferingDetailRequest.Action.MakeNotOffered);
			}
		});
		iHeader.setEnabled("make-not-offered", false);
		iHeader.getButton("make-not-offered").setAccessKey(COURSE.accessMakeNotOffered().charAt(0));
		iHeader.getButton("make-not-offered").setTitle(COURSE.titleMakeNotOffered(COURSE.accessMakeNotOffered()));

		iHeader.addButton("delete", COURSE.actionDeleteIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getOfferingId(), OfferingDetailRequest.Action.MakeOffered);
			}
		});
		iHeader.setEnabled("delete", false);
		iHeader.getButton("delete").setAccessKey(COURSE.accessDeleteIO().charAt(0));
		iHeader.getButton("delete").setTitle(COURSE.titleDeleteIO(COURSE.accessDeleteIO()));

		iHeader.addButton("previous", COURSE.actionPreviousIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.action?page=offering&io=" + iResponse.getPreviousId());
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousIO().charAt(0));
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousIO(COURSE.accessPreviousIO()));
		
		iHeader.addButton("next", COURSE.actionNextIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "gwt.action?page=offering&io=" + iResponse.getNextId());
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.getButton("next").setAccessKey(COURSE.accessNextIO().charAt(0));
		iHeader.getButton("next").setTitle(COURSE.titleNextIO(COURSE.accessNextIO()));
		
		iHeader.addButton("back", COURSE.actionBackIODetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "back.action?uri=" + URL.encodeQueryString(iResponse.getBackUrl()) +
						"&backId=" + iResponse.getOfferingId() + "&backType=InstructionalOffering");
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setAccessKey(COURSE.accessBackIODetail().charAt(0));

		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long offeringId, OfferingDetailRequest.Action action) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		OfferingDetailRequest req = new OfferingDetailRequest();
		req.setOfferingId(offeringId);
		req.setAction(action);
		req.setBackId(Window.Location.getParameter("backId"));
		req.setBackType(Window.Location.getParameter("backType"));
		req.setExamId(Window.Location.getParameter("examId"));
		RPC.execute(req, new AsyncCallback<OfferingDetailResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final OfferingDetailResponse response) {
				iResponse = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.getHeaderTitlePanel().clear();
				Anchor anchor = new Anchor(response.getName());
				anchor.setHref("gwt.action?page=offerings&subjectArea=" + response.getSubjectAreaId() + "&courseNbr=" + response.getCoruseNumber() + "#A" + response.getOfferingId());
				anchor.setAccessKey(COURSE.accessBackToIOList().charAt(0));
				anchor.setTitle(COURSE.titleBackToIOList(COURSE.accessBackToIOList()));
				anchor.setStyleName("l8");
				iHeader.getHeaderTitlePanel().add(anchor);
				
				TableWidget coursesTable = new TableWidget(response.getCourses());
				coursesTable.setStyleName("unitime-InnerTable");
				coursesTable.getElement().getStyle().setWidth(100.0, Unit.PCT);
				iPanel.addRow(COURSE.propertyCourseOfferings(), coursesTable);
				for (PropertyInterface property: response.getProperties().getProperties())
					iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell()));
				if (response.hasOperation("instructor-survey"))
					iTeachingRequestsRow = iPanel.addRow(new OfferingDetailWidget().forOfferingId(response.getOfferingId()));
				if (response.hasOperation("curricula"))
					iPanel.addRow(new CourseCurriculaTable(true, true).forOfferingId(response.getOfferingId()));
				if (response.hasOperation("reservations"))
					iReservationsRow = iPanel.addRow(new ReservationTable(response.hasOperation("reservations-editable"), true).forOfferingId(response.getOfferingId()));
				if (response.hasConfigs()) {
					for (final OfferingConfigInterface config: response.getConfigs()) {
						UniTimeHeaderPanel hp = new UniTimeHeaderPanel(config.getName());
						iPanel.addHeaderRow(hp);
						iPanel.addRow(new TableWidget(config));
						if (config.hasAnchor()) {
							Anchor a = new Anchor(); a.setName(config.getAnchor()); a.getElement().setId(config.getAnchor());
							hp.insertLeft(a, false);
						}
						if (config.hasOperation("config-edit")) {
							hp.addButton("config-edit", COURSE.actionEditConfiguration(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent evt) {
									ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=instrOfferingConfig&id=" + config.getConfigId());
								}
							});
							hp.getButton("config-edit").setTitle(COURSE.titleEditConfiguration());
						}
						if (config.hasOperation("class-setup")) {
							hp.addButton("class-setup", COURSE.actionClassSetup(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent evt) {
									ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=multipleClassSetup&id=" + config.getConfigId());
								}
							});
							hp.getButton("class-setup").setTitle(COURSE.titleClassSetup());
							
						}
						if (config.hasOperation("assign-instructors")) {
							hp.addButton("assign-instructors", COURSE.actionAssignInstructors(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent evt) {
									ToolBox.open(GWT.getHostPageBaseURL() + "gwt.jsp?page=assignClassInstructors&configId=" + config.getConfigId());
								}
							});
							hp.getButton("assign-instructors").setTitle(COURSE.titleAssignInstructors());
						}
					}
				}
				if (response.hasOperation("teaching-requests"))
					iTeachingRequestsRow = iPanel.addRow(new TeachingRequestsWidget().forOfferingId(response.getOfferingId()));
				
				if (response.hasDistributions()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getDistributions().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getDistributions()));
				}
								
				if (response.hasExaminations()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getExaminations().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getExaminations()));
					if (response.hasOperation("add-exam"))
						hp.addButton("add-exam", COURSE.actionAddExamination(), new ClickHandler() {
							@Override
							public void onClick(ClickEvent evt) {
								ToolBox.open(GWT.getHostPageBaseURL() + "examEdit.action?firstType=InstructionalOffering&firstId=" + response.getOfferingId());
							}
						});
				}
				
				if (response.hasLastChanges()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getLastChanges().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getLastChanges()));
				}

				if (response.isOffered())
					iPanel.addRow(new EnrollmentTable(true, true, true).forOfferingId(response.getOfferingId()));
				
				iPanel.addBottomRow(iFooter);
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String token = Window.Location.getHash();
						if (token != null && (token.startsWith("#A") || token.equals("#back")) || token.startsWith("#ioc")) {
							Element e = Document.get().getElementById(token.substring(1));
							if (e != null) ToolBox.scrollToElement(e);
						}
						Element e = Document.get().getElementById("back");
						if (e != null)
							ToolBox.scrollToElement(e);
						if (token.equals("#reservations") && iReservationsRow >= 0)
							ToolBox.scrollToElement(iPanel.getRowFormatter().getElement(iReservationsRow));
						if (token.equals("#instructors") && iTeachingRequestsRow >= 0)
							ToolBox.scrollToElement(iPanel.getRowFormatter().getElement(iTeachingRequestsRow));
					}
				});
				
				for (String op: iHeader.getOperations())
					iHeader.setEnabled(op, response.hasOperation(op));
				if (response.hasBackTitle()) {
					iHeader.getButton("back").setTitle(COURSE.titleBackIODetail(COURSE.accessBackIODetail()).replace("%%", response.getBackTitle()));
					iFooter.getButton("back").setTitle(COURSE.titleBackIODetail(COURSE.accessBackIODetail()).replace("%%", response.getBackTitle()));
				}
				UniTimeNavigation.getInstance().refresh();
			}
		});
	}

}
