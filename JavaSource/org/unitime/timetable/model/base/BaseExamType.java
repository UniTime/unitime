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
package org.unitime.timetable.model.base;

import java.io.Serializable;

import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.RefTableEntry;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseExamType extends RefTableEntry implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iType;
	private Boolean iHighlightInEvents;


	public static String PROP_XTYPE = "type";
	public static String PROP_EVENTS = "highlightInEvents";

	public BaseExamType() {
		initialize();
	}

	public BaseExamType(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Boolean isHighlightInEvents() { return iHighlightInEvents; }
	public Boolean getHighlightInEvents() { return iHighlightInEvents; }
	public void setHighlightInEvents(Boolean highlightInEvents) { iHighlightInEvents = highlightInEvents; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExamType)) return false;
		if (getUniqueId() == null || ((ExamType)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ExamType)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ExamType["+getUniqueId()+" "+getLabel()+"]";
	}

	public String toDebugString() {
		return "ExamType[" +
			"\n	HighlightInEvents: " + getHighlightInEvents() +
			"\n	Label: " + getLabel() +
			"\n	Reference: " + getReference() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
