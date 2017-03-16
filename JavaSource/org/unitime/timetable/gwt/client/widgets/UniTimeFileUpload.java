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
package org.unitime.timetable.gwt.client.widgets;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;

/**
 * @author Tomas Muller
 */
public class UniTimeFileUpload extends FormPanel implements HasValueChangeHandlers<String> {
	private static final GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private FileUpload iUpload;
	
	public UniTimeFileUpload() {
		setEncoding(ENCODING_MULTIPART);
		setMethod(METHOD_POST);
		setAction(GWT.getHostPageBaseURL() + "upload");
		
		iUpload = new FileUpload();
		iUpload.setName("file");
		setWidget(iUpload);
				
		iUpload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				submit();
			}
		});
		
		addSubmitCompleteHandler(new SubmitCompleteHandler() {
			@Override
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String message = event.getResults();
				if (message.startsWith("ERROR:")) {
					UniTimeNotifications.error(message.substring("ERROR:".length()));
					reset();
				} else {
					UniTimeNotifications.info(message);
					ValueChangeEvent.fire(UniTimeFileUpload.this, iUpload.getFilename());
				}
			}
		});
		
		addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				String name = iUpload.getFilename();
				if (name.indexOf('/') >= 0) name = name.substring(name.lastIndexOf('/') + 1);
				if (name.indexOf('\\') >= 0) name = name.substring(name.lastIndexOf('\\') + 1);
				if (!name.isEmpty())
					UniTimeNotifications.info("Uploading " + name + " ...");
			}
		});
	}
	
	@Override
	public void reset() {
		super.reset();
		RPC.execute(new FileUploadRpcRequest(true), new AsyncCallback<FileUploadRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught);
			}

			@Override
			public void onSuccess(FileUploadRpcResponse result) {
			}
		});
	}
	
	public void check() {
		RPC.execute(new FileUploadRpcRequest(), new AsyncCallback<FileUploadRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(caught.getMessage(), caught);
			}
			@Override
			public void onSuccess(FileUploadRpcResponse result) {
				if (result.hasFile()) {
					if (!result.getFileName().equals(getFileName())) reset();
				} else {
					if (getFileName() != null && !getFileName().isEmpty()) UniTimeFileUpload.super.reset();
				}
			}
		});
	}
	
	public String getFileName() {
		return iUpload.getFilename();
	}

	public static class FileUploadRpcRequest implements GwtRpcRequest<FileUploadRpcResponse>{
		private boolean iReset = false;
		
		public FileUploadRpcRequest() {}
		public FileUploadRpcRequest(boolean reset) { iReset = reset; }
		
		public boolean isReset() { return iReset; }
		
		public String toString() { return iReset ? "reset" : "request"; }
	}
	
	public static class FileUploadRpcResponse implements GwtRpcResponse {
		private String iName;
		
		public FileUploadRpcResponse() {}
		public FileUploadRpcResponse(String name) { iName = name; }
		
		public boolean hasFile() { return iName != null; }
		public String getFileName() { return iName; }
		
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}
}
