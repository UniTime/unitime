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
@SerializeWith(XApproval.XApprovalSerializer.class)
public class XApproval implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	
	private String iExternalId = null;
	private Date iTimeStamp = null;
	private String iName = null;
	
	public XApproval(String externalId, Date timeStamp, String name) {
		iExternalId = externalId;
		iTimeStamp = timeStamp;
		iName = name;
	}
	
	public XApproval(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XApproval(String[] approval) {
		iTimeStamp = new Date(Long.valueOf(approval[0]));
		iExternalId = approval[1];
		iName = approval[2];
	}
	
	public String getExternalId() { return iExternalId; }
	
	public Date getTimeStamp() { return iTimeStamp; }
	
	public String getName() { return iName; }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iExternalId = (String)in.readObject();
		iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		iName = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iExternalId);
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		out.writeObject(iName);
	}
	
	public static class XApprovalSerializer implements Externalizer<XApproval> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XApproval object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XApproval readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XApproval(input);
		}
	}

}
