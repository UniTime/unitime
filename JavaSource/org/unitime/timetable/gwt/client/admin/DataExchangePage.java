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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.exams.ReportQueueTable;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class DataExchangePage extends Composite {
	private static final GwtMessages MSG = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iPanel;
	private ReportQueueTable iQueue;
	private DataExchangeConfigResponse iConfig;
	private UniTimeFileUpload iUpload;
	
	public DataExchangePage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-DataExchangePage");
		initWidget(iPanel);
		
		iQueue = new ReportQueueTable(QueueType.DataExchange).attach(iPanel, COURSE.sectionDataExchangeQueue());
		iUpload = new UniTimeFileUpload();
		
		init();

	}
	
	private UniTimeHeaderPanel iImportHeader, iExportHeader, iOptionsHeader, iFooter;
	private ListBox iExportTypes;
	private CheckBox iEmail;
	private TextBox iAddress;
	
	protected void init() {
		RPC.execute(new DataExchangeConfigRequest(), new AsyncCallback<DataExchangeConfigResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(DataExchangeConfigResponse result) {
				iConfig = result;
				iFooter = new UniTimeHeaderPanel();
				iImportHeader = new UniTimeHeaderPanel(COURSE.sectioDateImport());
				iImportHeader.addButton("import", COURSE.actionImport(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						doImport();
					}
				});
				iFooter.addButton("import", COURSE.actionImport(), new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						doImport();
					}
				});
				iPanel.addHeaderRow(iImportHeader);
				iPanel.addRow(COURSE.fieldImportFile() + ":", iUpload);
				if (iConfig.hasExportTypes()) {
					iExportHeader = new UniTimeHeaderPanel(COURSE.sectioDateExport());
					iExportHeader.addButton("export", COURSE.actionExport(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							doExport();
						}
					});
					iFooter.addButton("export", COURSE.actionExport(), new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							doExport();
						}
					});
					iPanel.addHeaderRow(iExportHeader);
					iExportTypes = new ListBox();
					iExportTypes.addItem(COURSE.itemSelect(), "");
					for (ExportTypeInterface item: iConfig.getExportTypes()) {
						iExportTypes.addItem(item.getExportName(), item.getExportType());
					}
					iPanel.addRow(COURSE.fieldExportType() + ":", iExportTypes);
				}
				iOptionsHeader = new UniTimeHeaderPanel(COURSE.sectionDataExchangeOptions());
				P em = new P("email-line");
				iEmail = new CheckBox();
				iEmail.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
					@Override
					public void onValueChange(ValueChangeEvent<Boolean> event) {
						iAddress.setVisible(event.getValue());
					}
				});
				iAddress = new TextBox();
				iAddress.setWidth("500px");
				Roles.getTextboxRole().setAriaLabelProperty(iAddress.getElement(), COURSE.columnEmailAddress());
				iAddress.setVisible(false);
				if (iConfig.hasEmail())
					iAddress.setText(iConfig.getEmail());
				em.add(iEmail);
				em.add(iAddress);
				iPanel.addHeaderRow(iOptionsHeader);
				iPanel.addRow(COURSE.fieldDataExchangeEmail() + ":", em);
				iPanel.addBottomRow(iFooter);
			}
		});
	}
	
	protected void doImport() {
		if (validateImport()) {
			DataImportRequest request = new DataImportRequest();
			request.setEmail(iEmail.getValue() ? iAddress.getText() : null);
			RPC.execute(request, new AsyncCallback<DataImportExportResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iImportHeader.setErrorMessage(caught.getMessage());
					UniTimeNotifications.error(caught.getMessage(), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(DataImportExportResponse result) {
					iUpload.reset();
					iQueue.refreshQueue(result.getQueueId());
				}
			});
		}
	}
	
	protected boolean validateImport() {
		List<String> errors = new ArrayList<String>();
		if (iUpload.getFileName() == null)
			errors.add(COURSE.errorRequiredField(COURSE.fieldFile()));
		if (errors.isEmpty())
			iImportHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iImportHeader.setErrorMessage(message);
		}

		return errors.isEmpty();
	}
	
	protected void doExport() {
		if (validateExport()) {
			DataExportRequest request = new DataExportRequest();
			request.setExportType(iExportTypes.getSelectedValue());
			request.setEmail(iEmail.getValue() ? iAddress.getText() : null);
			RPC.execute(request, new AsyncCallback<DataImportExportResponse>() {
				@Override
				public void onFailure(Throwable caught) {
					LoadingWidget.getInstance().hide();
					iExportHeader.setErrorMessage(caught.getMessage());
					UniTimeNotifications.error(caught.getMessage(), caught);
					ToolBox.checkAccess(caught);
				}

				@Override
				public void onSuccess(DataImportExportResponse result) {
					iExportTypes.setSelectedIndex(0);
					iQueue.refreshQueue(result.getQueueId());
				}
			});
		}
	}
	
	protected boolean validateExport() {
		List<String> errors = new ArrayList<String>();
		if (iExportTypes.getSelectedIndex() <= 0)
			errors.add(COURSE.errorNothingToExport());
		if (errors.isEmpty())
			iExportHeader.clearMessage();
		else {
			String message = "";
			for (String e: errors)
				message += (message.isEmpty() ? "" : "\n") + e;
			iExportHeader.setErrorMessage(message);
		}

		return errors.isEmpty();
	}
	
	
	public static class DataExchangeConfigRequest implements GwtRpcRequest<DataExchangeConfigResponse> {
	}
	
	public static class DataExchangeConfigResponse implements GwtRpcResponse {
		private String iEmail;
		private List<ExportTypeInterface> iExportTypes = null;
		
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
		
		public boolean hasExportTypes() { return iExportTypes != null && !iExportTypes.isEmpty(); }
		public void addExportType(String type, String label) {
			if (iExportTypes == null) iExportTypes = new ArrayList<ExportTypeInterface>();
			ExportTypeInterface t = new ExportTypeInterface();
			t.setExportType(type);
			t.setExportName(label);
			iExportTypes.add(t);
		}
		public List<ExportTypeInterface> getExportTypes() { return iExportTypes; }

	}
	
	public static class ExportTypeInterface implements IsSerializable, Serializable {
		private static final long serialVersionUID = -3699386718685852812L;
		private String iExportType;
		private String iExportName;
		
		public String getExportType() { return iExportType; }
		public void setExportType(String exportType) { iExportType = exportType; }
		public String getExportName() { return iExportName; }
		public void setExportName(String exportName) { iExportName = exportName; }
	}

	public static class DataImportRequest implements GwtRpcRequest<DataImportExportResponse>, Serializable {
		private static final long serialVersionUID = -524629079331236478L;
		private String iEmail;
		
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
	}
	
	public static class DataImportExportResponse implements GwtRpcResponse {
		private String iQueueId = null;
		
		public DataImportExportResponse() {}
		public DataImportExportResponse(String queueId) { iQueueId = queueId; }
		
		public String getQueueId() { return iQueueId; }
		public void setQueueId(String queueId) { iQueueId = queueId; }
	}
	
	public static class DataExportRequest implements GwtRpcRequest<DataImportExportResponse>, Serializable {
		private static final long serialVersionUID = 2407638412380332884L;
		private String iEmail;
		private String iExportType;
		
		public String getExportType() { return iExportType; }
		public void setExportType(String exportType) { iExportType = exportType; }
		
		public String getEmail() { return iEmail; }
		public void setEmail(String email) { iEmail = email; }
		public boolean hasEmail() { return iEmail != null && !iEmail.isEmpty(); }
	}
}
