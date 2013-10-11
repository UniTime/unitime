/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

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
