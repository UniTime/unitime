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
import java.util.BitSet;

import net.sf.cpsolver.studentsct.model.FreeTimeRequest;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;
import org.unitime.timetable.model.CourseDemand;

/**
 * @author Tomas Muller
 */
@SerializeWith(XFreeTimeRequest.XFreeTimeRequestSerializer.class)
public class XFreeTimeRequest extends XRequest {
	private static final long serialVersionUID = 1L;
	private XTime iTime;
	
	public XFreeTimeRequest() {}
	
	public XFreeTimeRequest(ObjectInput in) throws IOException, ClassNotFoundException {
		readExternal(in);
	}
	
	public XFreeTimeRequest(CourseDemand demand, BitSet freeTimePattern) {
		super(demand);
		iTime = new XTime(demand.getFreeTime(), freeTimePattern);
	}
	
	public XFreeTimeRequest(FreeTimeRequest request) {
		super(request);
		iTime = new XTime(request.getTime());
	}
	
	public XTime getTime() { return iTime; }

	@Override
	public String toString() {
		return super.toString() + " Free " + getTime();
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		iTime = new XTime(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		iTime.writeExternal(out);
	}
	
	public static class XFreeTimeRequestSerializer implements Externalizer<XFreeTimeRequest> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XFreeTimeRequest object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XFreeTimeRequest readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XFreeTimeRequest(input);
		}
	}
}