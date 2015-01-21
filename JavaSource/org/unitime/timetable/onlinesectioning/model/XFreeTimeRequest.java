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
import java.util.BitSet;


import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
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