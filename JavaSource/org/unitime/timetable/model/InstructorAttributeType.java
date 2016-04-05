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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseInstructorAttributeType;

public class InstructorAttributeType extends BaseInstructorAttributeType implements Comparable<InstructorAttributeType> {
	private static final long serialVersionUID = 1L;

	public InstructorAttributeType() {
		super();
	}

	@Override
	public int compareTo(InstructorAttributeType o) {
		int cmp = getLabel().compareTo(o.getLabel());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(o.getUniqueId() == null ? -1 : o.getUniqueId());
	}

}
