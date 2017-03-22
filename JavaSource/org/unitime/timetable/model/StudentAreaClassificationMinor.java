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

import org.unitime.timetable.model.base.BaseStudentAreaClassificationMinor;

public class StudentAreaClassificationMinor extends BaseStudentAreaClassificationMinor implements Comparable<BaseStudentAreaClassificationMinor> {
	private static final long serialVersionUID = 1L;

	public StudentAreaClassificationMinor() {
		super();
	}
	
	@Override
	public int compareTo(BaseStudentAreaClassificationMinor m) {
		int cmp = getAcademicArea().getAcademicAreaAbbreviation().compareTo(m.getAcademicArea().getAcademicAreaAbbreviation());
		if (cmp != 0) return cmp;
		cmp = getAcademicClassification().getCode().compareTo(m.getAcademicClassification().getCode());
		if (cmp != 0) return cmp;
		cmp = getMinor().getCode().compareTo(m.getMinor().getCode());
		if (cmp != 0) return cmp;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(m.getUniqueId() == null ? -1 : m.getUniqueId());
	}

}
