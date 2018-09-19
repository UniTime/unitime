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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Date;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;

/**
 * @author Tomas Muller
 */
@SerializeWith(XOverride.XOverrideSerializer.class)
public class XOverride implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	
	private String iExternalId = null;
	private Date iTimeStamp = null;
	private Integer iStatus = null;
	private Float iValue = null;
	
	public XOverride(String externalId, Date timeStamp, Integer status, Float value) {
		iExternalId = externalId;
		iTimeStamp = timeStamp;
		iStatus = status;
		iValue = value;
	}
	
	public XOverride(String externalId, Date timeStamp, Integer status) {
		this(externalId, timeStamp, status, null);
	}
	
	public XOverride(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public String getExternalId() { return iExternalId; }
	
	public Date getTimeStamp() { return iTimeStamp; }
	
	public Integer getStatus() { return iStatus; }
	
	public void setStatus(Integer status) { iStatus = status; }
	
	public Float getValue() { return iValue; }
	
	public boolean isPending() { return iStatus == null || iStatus == CourseRequestOverrideStatus.PENDING.ordinal(); };
	public boolean isApproved() { return iStatus != null && iStatus == CourseRequestOverrideStatus.APPROVED.ordinal(); };
	public boolean isCancelled() { return iStatus != null && iStatus == CourseRequestOverrideStatus.CANCELLED.ordinal(); };
	public boolean isRejected() { return iStatus != null && iStatus == CourseRequestOverrideStatus.REJECTED.ordinal(); };
	public boolean isCancelledOrRejected() { return iStatus != null && (iStatus == CourseRequestOverrideStatus.CANCELLED.ordinal() || iStatus == CourseRequestOverrideStatus.REJECTED.ordinal()); };

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iExternalId = (String)in.readObject();
		iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		int status = in.readInt();
		iStatus = (status < 0 ? null : new Integer(status));
		iValue = (in.readBoolean() ? new Float(in.readFloat()) : null);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iExternalId);
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		out.writeInt(iStatus == null ? -1 : iStatus.intValue());
		out.writeBoolean(iValue != null);
		if (iValue != null)
			out.writeFloat(iValue);
	}
	
	public static class XOverrideSerializer implements Externalizer<XOverride> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XOverride object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XOverride readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XOverride(input);
		}
	}

}
