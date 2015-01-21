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
import java.util.TreeSet;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCourseIdSet.XCourseIdSetSerializer.class)
public class XCourseIdSet extends TreeSet<XCourseId> implements Externalizable {
	private static final long serialVersionUID = 1L;
	
	public XCourseIdSet() {
		super();
	}
	
	public XCourseIdSet(ObjectInput in) throws IOException, ClassNotFoundException {
		super();
		readExternal(in);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int count = in.readInt();
		if (!isEmpty()) clear();
		for (int i = 0; i < count; i++)
			add(new XCourseId(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(size());
		for (XCourseId courseId: this)
			courseId.writeExternal(out);
	}

	public static class XCourseIdSetSerializer implements Externalizer<XCourseIdSet> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCourseIdSet object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCourseIdSet readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCourseIdSet(input);
		}		
	}
}
