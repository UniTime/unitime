/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.events;

import org.apache.commons.fileupload.FileItem;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload.FileUploadRpcRequest;
import org.unitime.timetable.gwt.client.widgets.UniTimeFileUpload.FileUploadRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;

public class FileUploadBackend implements GwtRpcImplementation<FileUploadRpcRequest, FileUploadRpcResponse> {

	@Override
	public FileUploadRpcResponse execute(FileUploadRpcRequest request, GwtRpcHelper helper) {
		if (request.isReset())
			helper.clearLastUploadedFile();
		FileItem file = helper.getLastUploadedFile();
		return (file == null ? new FileUploadRpcResponse() : new FileUploadRpcResponse(file.getName()));
	}

}
