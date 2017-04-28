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
package org.unitime.timetable.gwt.client.limitandprojectionsnapshot;

import java.util.Date;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.FilterPanel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.LimitAndProjectionSnapshotService;
import org.unitime.timetable.gwt.services.LimitAndProjectionSnapshotServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author Stephanie Schluttenhofer
 *
 */
public class LimitAndProjectionSnapshotPage extends Composite {
	protected static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static final GwtResources RESOURCES = GWT.create(GwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(GwtConstants.class);
	private final LimitAndProjectionSnapshotServiceAsync iLimitAndProjectionSnapshotService = GWT.create(LimitAndProjectionSnapshotService.class);
	protected static DateTimeFormat sLoadDateFormat = DateTimeFormat.getFormat(CONSTANTS.timeStampFormat());
	
	private Label iCurrentSnapshotDateLabel = null;
	private Button iStoreSnapshot = null;
	
	private VerticalPanel iLimitAndProjectionSnapshotPanel = null;
	private UniTimeHeaderPanel iErrorPanel = null;
	
	private SimplePanel iPanel = null;
	private FilterPanel iCurrentSnapshotDatePanel = null;
	
	public LimitAndProjectionSnapshotPage() {
		iPanel = new SimplePanel();
		iPanel.setWidth("100%");
		
		iLimitAndProjectionSnapshotPanel = new VerticalPanel();
		
		iCurrentSnapshotDatePanel = new FilterPanel();
		
		Label currentSnapshotDateLabel = new Label(MESSAGES.labelCurrentSnapshotDate());
		iCurrentSnapshotDatePanel.addLeft(currentSnapshotDateLabel);
		
		iCurrentSnapshotDateLabel = new Label();
		iCurrentSnapshotDatePanel.addLeft(iCurrentSnapshotDateLabel);
		
		iErrorPanel = new UniTimeHeaderPanel();
		iLimitAndProjectionSnapshotPanel.add(iErrorPanel);
		

		iStoreSnapshot = new Button(MESSAGES.buttonTakeNewSnapshot());
		iStoreSnapshot.setAccessKey('t');
		iStoreSnapshot.setEnabled(false);
		iStoreSnapshot.addStyleName("unitime-NoPrint");
		iCurrentSnapshotDatePanel.addRight(iStoreSnapshot);
		iLimitAndProjectionSnapshotService.canTakeSnapshot(new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Boolean result) {
				iStoreSnapshot.setEnabled(result);
			}
		});
				
		iLimitAndProjectionSnapshotService.getCurrentSnapshotDate(new AsyncCallback<Date>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(Date result) {
				iCurrentSnapshotDateLabel.setText(sLoadDateFormat.format(result));
			}
		});
		iLimitAndProjectionSnapshotPanel.add(iCurrentSnapshotDatePanel);
		iLimitAndProjectionSnapshotPanel.setCellHorizontalAlignment(iCurrentSnapshotDatePanel, HasHorizontalAlignment.ALIGN_CENTER);
				
		
		iLimitAndProjectionSnapshotPanel.setWidth("100%");
		
		iPanel.setWidget(iLimitAndProjectionSnapshotPanel);
		
		
		initWidget(iPanel);
		
		iStoreSnapshot.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				storeLimitAndProjectionSnapshot();
			}
		});

	}
	
	private void storeLimitAndProjectionSnapshot() {
		if (!iStoreSnapshot.isEnabled()) return;
		iStoreSnapshot.setEnabled(false);
		iErrorPanel.setErrorMessage(null);
		LoadingWidget.getInstance().show(MESSAGES.waitStoringSnapshot());
		iLimitAndProjectionSnapshotService.takeSnapshot(new AsyncCallback<Date>() {
			@Override
			public void onSuccess(Date result) {
				LoadingWidget.getInstance().hide();
				iCurrentSnapshotDateLabel.setText(sLoadDateFormat.format(result));
				iStoreSnapshot.setEnabled(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
	 			iErrorPanel.setErrorMessage(MESSAGES.failedToStoreSnapshot(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToStoreSnapshot(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}
		});
	}
}
