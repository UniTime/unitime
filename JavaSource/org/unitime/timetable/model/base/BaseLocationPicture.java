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

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.timetable.model.AttachmentType;
import org.unitime.timetable.model.LocationPicture;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseLocationPicture implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private byte[] iDataFile;
	private String iFileName;
	private String iContentType;
	private Date iTimeStamp;

	private AttachmentType iType;

	public BaseLocationPicture() {
	}

	public BaseLocationPicture(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "room_pict_id", strategy = "org.unitime.commons.hibernate.id.UniqueIdGenerator", parameters = {
		@Parameter(name = "sequence", value = "room_seq")
	})
	@GeneratedValue(generator = "room_pict_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "data_file", nullable = false)
	public byte[] getDataFile() { return iDataFile; }
	public void setDataFile(byte[] dataFile) { iDataFile = dataFile; }

	@Column(name = "file_name", nullable = false, length = 260)
	public String getFileName() { return iFileName; }
	public void setFileName(String fileName) { iFileName = fileName; }

	@Column(name = "content_type", nullable = false, length = 260)
	public String getContentType() { return iContentType; }
	public void setContentType(String contentType) { iContentType = contentType; }

	@Column(name = "time_stamp", nullable = false)
	public Date getTimeStamp() { return iTimeStamp; }
	public void setTimeStamp(Date timeStamp) { iTimeStamp = timeStamp; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "type_id", nullable = true)
	public AttachmentType getType() { return iType; }
	public void setType(AttachmentType type) { iType = type; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof LocationPicture)) return false;
		if (getUniqueId() == null || ((LocationPicture)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((LocationPicture)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
