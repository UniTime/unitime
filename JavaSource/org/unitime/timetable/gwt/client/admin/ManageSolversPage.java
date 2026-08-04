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
package org.unitime.timetable.gwt.client.admin;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;

public class ManageSolversPage extends Composite {
	protected static final CourseMessages MSG = GWT.create(CourseMessages.class);
	protected static final GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	SimpleForm iPanel;
	UniTimeHeaderPanel iHeaderSolversCourse, iHeaderSolversExam, iHeaderSolversStudent, iHeaderSolversInstructor, iHeaderSolversOnline, iHeaderServers, iFooter;
	TableWidget iSolversCourse, iSolversExam, iSolversStudent, iSolversInstructor, iSolversOnline, iServers;
	
	public ManageSolversPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-ManageSolversPage");
		initWidget(iPanel);
		
		iHeaderSolversCourse = new UniTimeHeaderPanel(GWT_MSG.sectManageSolversCourse());
		iHeaderSolversCourse.addButton("deselect", GWT_MSG.actionSolverDeselect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh(iHeaderSolversCourse, Operation.DESELECT_SOLVER_COURSE, null, iSolversCourse);
			}
		});
		iPanel.addHeaderRow(iHeaderSolversCourse);
		iSolversCourse = new TableWidget();
		iPanel.addRow(iSolversCourse);
		refresh(iHeaderSolversCourse, Operation.COURSE_SOLVERS, null, iSolversCourse);

		iHeaderSolversExam = new UniTimeHeaderPanel(GWT_MSG.sectManageSolversExam());
		iHeaderSolversExam.addButton("deselect", GWT_MSG.actionSolverDeselect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh(iHeaderSolversExam, Operation.DESELECT_SOLVER_EXAM, null, iSolversExam);
			}
		});
		iPanel.addHeaderRow(iHeaderSolversExam);
		iSolversExam = new TableWidget();
		iPanel.addRow(iSolversExam);
		refresh(iHeaderSolversExam, Operation.EXAM_SOLVERS, null, iSolversExam);

		iHeaderSolversStudent = new UniTimeHeaderPanel(GWT_MSG.sectManageSolversStudent());
		iHeaderSolversStudent.addButton("deselect", GWT_MSG.actionSolverDeselect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh(iHeaderSolversStudent, Operation.DESELECT_SOLVER_STUDENT, null, iSolversStudent);
			}
		});
		iPanel.addHeaderRow(iHeaderSolversStudent);
		iSolversStudent = new TableWidget();
		iPanel.addRow(iSolversStudent);
		refresh(iHeaderSolversStudent, Operation.STUDENT_SOLVERS, null, iSolversStudent);

		iHeaderSolversInstructor = new UniTimeHeaderPanel(GWT_MSG.sectManageSolversInstructor());
		iHeaderSolversInstructor.addButton("deselect", GWT_MSG.actionSolverDeselect(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh(iHeaderSolversInstructor, Operation.DESELECT_SOLVER_INSTRUCTOR, null, iSolversInstructor);
			}
		});
		iPanel.addHeaderRow(iHeaderSolversInstructor);
		iSolversInstructor = new TableWidget();
		iPanel.addRow(iSolversInstructor);
		refresh(iHeaderSolversInstructor, Operation.INSTRUCTOR_SOLVERS, null, iSolversInstructor);

		iHeaderSolversOnline = new UniTimeHeaderPanel(GWT_MSG.sectManageSolversOnline());
		iPanel.addHeaderRow(iHeaderSolversOnline);
		iSolversOnline = new TableWidget();
		iPanel.addRow(iSolversOnline);
		refresh(iHeaderSolversOnline, Operation.ONLINE_SOLVERS, null, iSolversOnline);

		iHeaderServers = new UniTimeHeaderPanel(GWT_MSG.sectAvailableServers());
		iPanel.addHeaderRow(iHeaderServers);
		iServers = new TableWidget();
		iPanel.addRow(iServers);
		refresh(iHeaderServers, Operation.SERVERS, null, iServers);
		
		iFooter = new UniTimeHeaderPanel();
		iFooter.addButton("refresh", GWT_MSG.buttonRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				refresh(iHeaderSolversCourse, Operation.COURSE_SOLVERS, null, iSolversCourse);
				refresh(iHeaderSolversExam, Operation.EXAM_SOLVERS, null, iSolversExam);
				refresh(iHeaderSolversStudent, Operation.STUDENT_SOLVERS, null, iSolversStudent);
				refresh(iHeaderSolversInstructor, Operation.INSTRUCTOR_SOLVERS, null, iSolversInstructor);
				refresh(iHeaderSolversOnline, Operation.ONLINE_SOLVERS, null, iSolversOnline);
				refresh(iHeaderServers, Operation.SERVERS, null, iServers);
			}
		});
		iPanel.addBottomRow(iFooter);
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue() != null && !event.getValue().isEmpty());
				int split = event.getValue().indexOf(':');
				Operation op = Operation.valueOf(event.getValue().substring(0, split));
				String id = event.getValue().substring(split + 1);
				UniTimeHeaderPanel header = null;
				TableWidget widget = null;
				switch (op) {
				case SERVERS:
				case SERVER_DISABLE:
				case SERVER_ENABLE:
				case SERVER_RECONNECT:
				case SERVER_RESET:
				case SERVER_SHUTDOWN:
				case SERVER_HIBERNATE:
					widget = iServers; header = iHeaderServers;
					break;
				case COURSE_SOLVERS:
				case SELECT_SOLVER_COURSE:
				case DESELECT_SOLVER_COURSE:
				case UNLOAD_SOLVER_COURSE:
					widget = iSolversCourse; header = iHeaderSolversCourse;
					break;
				case EXAM_SOLVERS:
				case SELECT_SOLVER_EXAM:
				case DESELECT_SOLVER_EXAM:
				case UNLOAD_SOLVER_EXAM:
					widget = iSolversExam; header = iHeaderSolversExam;
					break;
				case STUDENT_SOLVERS:
				case SELECT_SOLVER_STUDENT:
				case DESELECT_SOLVER_STUDENT:
				case UNLOAD_SOLVER_STUDENT:
					widget = iSolversStudent; header = iHeaderSolversStudent;
					break;
				case INSTRUCTOR_SOLVERS:
				case SELECT_SOLVER_INSTRUCTOR:
				case DESELECT_SOLVER_INSTRUCTOR:
				case UNLOAD_SOLVER_INSTRUCTOR:
					widget = iSolversInstructor; header = iHeaderSolversInstructor;
					break;
				case ONLINE_SOLVERS:
				case ONLINE_RELOAD:
				case ONLINE_SHUTDOWN:
					widget = iSolversOnline; header = iHeaderSolversOnline;
					break;
				}
				History.replaceItem("", false);
				refresh(header, op, id, widget);
			}
		});
	}
	
	public void refresh(final UniTimeHeaderPanel panel, Operation op, String id, final TableWidget table) {
		panel.showLoading();
		panel.setEnabled("deselect", false);
		RPC.execute(new ManageSolversRequest(op, id), new AsyncCallback<ManageSolversResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				panel.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}
			@Override
			public void onSuccess(ManageSolversResponse result) {
				panel.clearMessage();
				if (result.hasUrl())
					ToolBox.open(GWT.getHostPageBaseURL() + result.getUrl());
				if (result.getTable() != null)
					table.setData(result.getTable());
				panel.setEnabled("deselect", result.isCanDeselect());
			}
		});
	}
	
	public static class ManageSolversRequest implements GwtRpcRequest<ManageSolversResponse> {
		private String iId;
		private Operation iOperation;
		
		public ManageSolversRequest() {}
		public ManageSolversRequest(Operation op) {
			iOperation = op;
		}
		public ManageSolversRequest(Operation op, String id) {
			iOperation = op;
			iId = id;
		}
		
		public String getId() { return iId; }
		public void setId(String id) { iId = id; }
		public boolean hasId() { return iId != null && !iId.isEmpty(); }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation op) { iOperation = op; }
	}
	
	public static class ManageSolversResponse implements GwtRpcResponse {
		private TableInterface iTable;
		private boolean iCanDeselect = false;
		private String iUrl = null;
		
		public ManageSolversResponse() {}
		public ManageSolversResponse(String url) {
			iUrl = url;
		}
		
		public TableInterface getTable() { return iTable; }
		public void setTable(TableInterface table) { iTable = table; }
		
		public boolean isCanDeselect() { return iCanDeselect; }
		public void setCanDeselect(boolean canDeselect) { iCanDeselect = canDeselect; }
		
		public String getUrl() { return iUrl; }
		public void setUrl(String url) { iUrl = url; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
	}
	
	public static enum Operation implements IsSerializable {
		COURSE_SOLVERS,
		EXAM_SOLVERS,
		STUDENT_SOLVERS,
		INSTRUCTOR_SOLVERS,
		ONLINE_SOLVERS,
		SERVERS,
		UNLOAD_SOLVER_COURSE,
		UNLOAD_SOLVER_EXAM,
		UNLOAD_SOLVER_STUDENT,
		UNLOAD_SOLVER_INSTRUCTOR,
		SELECT_SOLVER_COURSE,
		SELECT_SOLVER_EXAM,
		SELECT_SOLVER_STUDENT,
		SELECT_SOLVER_INSTRUCTOR,
		DESELECT_SOLVER_COURSE,
		DESELECT_SOLVER_EXAM,
		DESELECT_SOLVER_STUDENT,
		DESELECT_SOLVER_INSTRUCTOR,
		ONLINE_RELOAD,
		ONLINE_SHUTDOWN,
		SERVER_ENABLE,
		SERVER_DISABLE,
		SERVER_RESET,
		SERVER_RECONNECT,
		SERVER_HIBERNATE,
		SERVER_SHUTDOWN,
	}

}
