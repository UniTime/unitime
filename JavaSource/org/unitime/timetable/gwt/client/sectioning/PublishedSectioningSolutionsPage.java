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
package org.unitime.timetable.gwt.client.sectioning;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface.Operation;
import org.unitime.timetable.gwt.shared.PublishedSectioningSolutionInterface.PublishedSectioningSolutionsRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;

/**
 * @author Tomas Muller
 */
public class PublishedSectioningSolutionsPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private PublishedSectioningSolutionsTable iTable;
	
	public PublishedSectioningSolutionsPage() {
		iForm = new SimpleForm();
		iHeader = new UniTimeHeaderPanel(MESSAGES.titlePublishedScheduleRuns());
		iHeader.addButton("refresh", MESSAGES.opRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iHeader.showLoading();
				RPC.execute(new PublishedSectioningSolutionsRequest(Operation.LIST), new AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught);
					}
					@Override
					public void onSuccess(GwtRpcResponseList<PublishedSectioningSolutionInterface> result) {
						iHeader.clearMessage();
						iTable.setValue(result);
					}
				});
			}
		});
		iForm.addHeaderRow(iHeader);
		
		iTable = new PublishedSectioningSolutionsTable();
		iForm.addRow(iTable);
		
		iFooter = iHeader.clonePanel("");
		iForm.addBottomRow(iFooter);
		
		LoadingWidget.getInstance().show(MESSAGES.waitPlease());
		RPC.execute(new PublishedSectioningSolutionsRequest(Operation.LIST), new AsyncCallback<GwtRpcResponseList<PublishedSectioningSolutionInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				UniTimeNotifications.error(caught);
			}
			@Override
			public void onSuccess(GwtRpcResponseList<PublishedSectioningSolutionInterface> result) {
				LoadingWidget.getInstance().hide();
				iTable.setValue(result);
			}
		});
		
		initWidget(iForm);
	}
}
