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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XWaitListedSection.XWaitListedSectionSerializer.class)
public class XWaitListedSection extends XSection {
	private static final long serialVersionUID = 1L;
	private Long iWaitListId = null;
	private Date iTimeStamp = null;
	private ClassWaitList.Type iType = null;
	
	public XWaitListedSection() {}
	
	public XWaitListedSection(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XWaitListedSection(ClassWaitList clw, OnlineSectioningHelper helper) {
		super(clw.getClazz(), helper);
		iWaitListId = clw.getUniqueId();
		iTimeStamp = clw.getTimestamp();
		iType = ClassWaitList.Type.values()[clw.getType()];
	}
	
	public Long getWaitListId() { return iWaitListId; }
	public ClassWaitList.Type getType() { return iType; }
	public Date getTimeStamp() { return iTimeStamp; }
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		iWaitListId = in.readLong();
		if (iWaitListId < 0) iWaitListId = null;
		iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
		iType = ClassWaitList.Type.values()[in.readInt()];
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeLong(iWaitListId == null ? -1l : iWaitListId);
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		out.writeInt(iType.ordinal());
	}
	
	public static class XWaitListedSectionSerializer implements Externalizer<XWaitListedSection> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XWaitListedSection object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XWaitListedSection readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XWaitListedSection(input);
		}
	}
}
