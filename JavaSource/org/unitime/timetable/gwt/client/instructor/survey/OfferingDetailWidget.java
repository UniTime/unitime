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
package org.unitime.timetable.gwt.client.instructor.survey;

import org.unitime.timetable.gwt.client.instructor.InstructorCookie;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.InstructorRequirementData;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Tomas Muller
 */
public class OfferingDetailWidget extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private UniTimeHeaderPanel iHeader;
	private SimpleForm iForm;
	private Long iOfferingId;
	
	public OfferingDetailWidget() {
		iForm = new SimpleForm();
		iForm.setCellPadding(1);
		iForm.addStyleName("unitime-InstructorSurveyPage");
		iForm.removeStyleName("unitime-NotPrintableBottomLine");
		
		iHeader = new UniTimeHeaderPanel(MESSAGES.sectInstructorRequirements());
		iHeader.setCollapsible(InstructorCookie.getInstance().isShowSurveyDetails());
		iHeader.setTitleStyleName("unitime3-HeaderTitle");
		iHeader.addCollapsibleHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				InstructorCookie.getInstance().setShowSurveyDetails(event.getValue());
				if (iForm.getRowCount() <= 1) {
					open();
				} else {
					for (int row = 1; row < iForm.getRowCount(); row++) {
						iForm.getRowFormatter().setVisible(row, event.getValue());
					}
				}
			}
		});
		
		iForm.addHeaderRow(iHeader);
		
		initWidget(iForm);
	}
	
	protected void open() {
		iHeader.showLoading();
		RPC.execute(new InstructorSurveyInterface.InstructorRequirementsRequest(iOfferingId), new AsyncCallback<InstructorRequirementData>() {
			@Override
			public void onFailure(Throwable t) {
				iHeader.setErrorMessage(MESSAGES.failedToLoadPage(t.getMessage()));
			}
			@Override
			public void onSuccess(InstructorRequirementData data) {
				setValue(data);
				iHeader.clearMessage();
			}
		});
	}
	
	protected void setValue(InstructorRequirementData data) {
		if (iForm.getRowCount() > 1) {
			iForm.clear();
			iForm.addHeaderRow(iHeader);
		}
		iForm.addRow(new InstructorRequirementsTable(data));
	}
	
	public void insert(final RootPanel panel) {
		iOfferingId = Long.valueOf(panel.getElement().getInnerText());
		panel.getElement().setInnerText(null);
		panel.add(this);
		panel.setVisible(true);
		if (iHeader.isCollapsible())
			open();
	}
	
	public OfferingDetailWidget forOfferingId(Long offeringId) {
		iOfferingId = offeringId;
		if (iHeader.isCollapsible())
			open();
		return this;
	}
}
