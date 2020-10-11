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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.List;

import org.unitime.timetable.gwt.client.instructor.InstructorCookie;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.sectioning.ScheduleStatus;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.GetInstructorTeachingSchedule;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.Instructor;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.InstructorMeetingAssignment;
import org.unitime.timetable.gwt.client.teachingschedule.TeachingScheduleAPI.ValidationError;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class InstructorTeachingScheduleWidget extends SimpleForm {
	protected static final TeachingScheduleMessages MESSAGES = GWT.create(TeachingScheduleMessages.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private UniTimeHeaderPanel iHeader;
	private InstructorTeachingSchedule iTable;
	private String iInstructorId;
	private ScheduleStatus iStatus;
	
	public InstructorTeachingScheduleWidget() {
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectInstructorTeachingSchedule());
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				InstructorCookie.getInstance().setShowTeachingSchedule(event.getValue());
				if (iTable != null) {
					iTable.setVisible(event.getValue());
					iStatus.setVisible(true);
				} else if (event.getValue()) {
					refresh();
				}
			}
		});
		iHeader.setCollapsible(InstructorCookie.getInstance().isShowTeachingSchedule());
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		removeStyleName("unitime-NotPrintableBottomLine");
		
		addHeaderRow(iHeader);
		iHeader.getElement().getStyle().setMarginTop(10, Unit.PX);
	}
	
	public void insert(final RootPanel panel) {
		iInstructorId = panel.getElement().getInnerText();
		if (InstructorCookie.getInstance().isShowTeachingSchedule()) {
			refresh();
		}
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
	}
	
	protected void refresh() {
		iHeader.showLoading();
		if (iTable == null)
			init();
		else
			populate();
	}
	
	protected void init() {
		iTable = new InstructorTeachingSchedule();
		iTable.setVisible(false);
		addRow(iTable);
		iStatus = new ScheduleStatus();
		iStatus.setVisible(false);
		addRow(iStatus);
		populate();
	}
	
	protected void populate() {
		iTable.clearTable(1);
		iStatus.clear();
		if (iInstructorId == null || iInstructorId.isEmpty()) return;
		RPC.execute(new GetInstructorTeachingSchedule(Long.valueOf(iInstructorId)), new AsyncCallback<Instructor>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setCollapsible(null);
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
			}

			@Override
			public void onSuccess(Instructor result) {
				iHeader.clearMessage();
				LoadingWidget.getInstance().hide();
				for (InstructorMeetingAssignment a: result.getAssignmetns())
					if (a.getDivision() != null)
						iTable.addInstructorMeetingAssignment(a);
				iTable.setVisible(true);
				List<ValidationError> errors = result.getAssignmetnErrors();
				if (!errors.isEmpty()) {
					String message = "";
					for (ValidationError error: errors) {
						String m = error.toString(MESSAGES, CONSTANTS);
						if (!message.contains(m))
							message += (message.isEmpty() ? "" : "\n") + m;
					}
					iStatus.error(message, false);
					iStatus.setVisible(true);
					for (int i = 1; i < iTable.getRowCount(); i++) {
						InstructorMeetingAssignment a = iTable.getData(i);
						for (ValidationError error: errors)
							if (a.equals(error.getInstructorAssignment())) {
								iTable.getRowFormatter().addStyleName(i, "error");
								break;
							}
					}
				} else {
					iStatus.setVisible(false);
				}
			}
		});
	}

}
