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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.model.AttachementType;
import org.unitime.timetable.model.LocationPicture;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseLocationPicture implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private byte[] iDataFile;
	private String iFileName;
	private String iContentType;
	private Date iTimeStamp;

	private AttachementType iType;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_DATA_FILE = "dataFile";
	public static String PROP_FILE_NAME = "fileName";
	public static String PROP_CONTENT_TYPE = "contentType";
	public static String PROP_TIME_STAMP = "timeStamp";

	public BaseLocationPicture() {
		initialize();
	}

	public BaseLocationPicture(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public byte[] getDataFile() { return iDataFile; }
	public void setDataFile(byte[] dataFile) { iDataFile = dataFile; }

	public String getFileName() { return iFileName; }
	public void setFileName(String fileName) { iFileName = fileName; }

	public String getContentType() { return iContentType; }
	public void setContentType(String contentType) { iContentType = contentType; }

	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	public AttachementType getType() { return iType; }
	public void setType(AttachementType type) { iType = type; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof LocationPicture)) return false;
		if (getUniqueId() == null || ((LocationPicture)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((LocationPicture)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "LocationPicture["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "LocationPicture[" +
			"\n	ContentType: " + getContentType() +
			"\n	DataFile: " + getDataFile() +
			"\n	FileName: " + getFileName() +
			"\n	TimeStamp: " + getTimeStamp() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
