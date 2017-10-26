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
package org.unitime.timetable.util.queue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;

import org.apache.commons.io.IOUtils;

/**
 * @author Tomas Muller
 */
public class ByteArrayDataSource implements DataSource, Serializable {
	private static final long serialVersionUID = 1L;
	private String iContentType;
	private String iName;
	private byte[] iData;
	
	public ByteArrayDataSource(File file) throws FileNotFoundException, IOException {
		iName = file.getName();
		iContentType = FileTypeMap.getDefaultFileTypeMap().getContentType(file);
		FileInputStream is = new FileInputStream(file);
		try {
			iData = IOUtils.toByteArray(is);
		} finally {
			is.close();
		}
	}
	
	public ByteArrayDataSource(DataSource ds) throws IOException {
		iName = ds.getName();
		iContentType = ds.getContentType();
		InputStream is = ds.getInputStream();
		try {
			iData = IOUtils.toByteArray(is);
		} finally {
			 is.close();
		}			
	}

	@Override
	public String getContentType() {
		return iContentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(iData);
	}

	@Override
	public String getName() {
		return iName;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}
}
