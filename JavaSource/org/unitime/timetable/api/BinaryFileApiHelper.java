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
package org.unitime.timetable.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.CacheMode;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
public class BinaryFileApiHelper extends JsonApiHelper {
	
	public BinaryFileApiHelper(HttpServletRequest request, HttpServletResponse response, SessionContext context, CacheMode cacheMode) {
		super(request, response, context, cacheMode);
	}

	@Override
	public <P> P getRequest(Type requestType) throws IOException {
		if (requestType.equals(BinaryFile.class)) {
			InputStream is = iRequest.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int read;
			byte[] data = new byte[16384];
			while ((read = is.read(data, 0, data.length)) != -1)
				buffer.write(data, 0, read);
			buffer.flush();
			BinaryFile output = new BinaryFile(buffer.toByteArray(), iRequest.getContentType(), getFileName());
			return (P)output;
		} else {
			return super.getRequest(requestType);
		}
	}
	
	protected String getFileName() {
		String param = iRequest.getHeader("Content-Disposition");
		if (param != null)
			for (String cd : param.split(";")) {
		        if (cd.trim().startsWith("filename")) {
		            String fileName = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
		            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
		        }
		    }
		return null;
	}

	@Override
	public <R> void setResponse(R response) throws IOException {
		if (response instanceof BinaryFile) {
			BinaryFile file = (BinaryFile)response;
			iResponse.setContentType(file.getContentType());
			iResponse.setHeader("Pragma", "no-cache" );
			iResponse.addHeader("Cache-Control", "must-revalidate" );
			iResponse.addHeader("Cache-Control", "no-cache" );
			iResponse.addHeader("Cache-Control", "no-store" );
			iResponse.setDateHeader("Date", new Date().getTime());
			iResponse.setDateHeader("Expires", 0);
			iResponse.setHeader( "Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"" );
			OutputStream out = iResponse.getOutputStream(); 
			out.write(file.getBytes());
			out.flush();
		} else {
			super.setResponse(response);
		}
	}
	
	public static class BinaryFile {
		byte[] iBuffer;
		public String iContentType;
		public String iFileName;
		
		public BinaryFile(byte[] buffer, String contentType, String fileName) {
			iBuffer = buffer;
			iContentType = contentType;
			iFileName = fileName;
		}
		
		public byte[] getBytes() { return iBuffer; }
		
		public String getContentType() { return iContentType; }
		
		public String getFileName() { return iFileName; }
	}

}
