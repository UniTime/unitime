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
package org.unitime.timetable.events;

import org.apache.commons.fileupload.FileItem;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload.FileUploadRpcRequest;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload.FileUploadRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.server.UploadServlet;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(FileUploadRpcRequest.class)
public class FileUploadBackend implements GwtRpcImplementation<FileUploadRpcRequest, FileUploadRpcResponse> {

	@Override
	public FileUploadRpcResponse execute(FileUploadRpcRequest request, SessionContext helper) {
		if (request.isReset()) {
			helper.setAttribute(UploadServlet.SESSION_LAST_FILE, null);
			return new FileUploadRpcResponse();
		} else {
			FileItem file = (FileItem)helper.getAttribute(UploadServlet.SESSION_LAST_FILE);
			return (file == null ? new FileUploadRpcResponse() : new FileUploadRpcResponse(file.getName()));
		}
	}

}
