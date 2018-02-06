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

/**
 * @author Tomas Muller
 */
@SerializeWith(XOverride.XOverrideSerializer.class)
public class XOverride implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	
	private String iExternalId = null;
	private Date iTimeStamp = null;
	private Integer iStatus = null;
	
	public XOverride(String externalId, Date timeStamp, Integer status) {
		iExternalId = externalId;
		iTimeStamp = timeStamp;
		iStatus = status;
	}
	
	public XOverride(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public String getExternalId() { return iExternalId; }
	
	public Date getTimeStamp() { return iTimeStamp; }
	
	public Integer getStatus() { return iStatus; }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iExternalId = (String)in.readObject();
		iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		int status = in.readInt();
		iStatus = (status < 0 ? null : new Integer(status));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iExternalId);
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		out.writeInt(iStatus == null ? -1 : iStatus.intValue());
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
