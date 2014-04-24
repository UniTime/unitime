/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.Date;

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
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
