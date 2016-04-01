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
package org.unitime.timetable.gwt.client.instructor;

import java.util.Comparator;

import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorsColumn;

/**
 * @author Tomas Muller
 */
public class InstructorComparator implements Comparator<InstructorInterface> {
	private InstructorsColumn iColumn;
	private boolean iAsc;
	
	public InstructorComparator(InstructorsColumn column, boolean asc) {
		iColumn = column;
		iAsc = asc;
	}

	public int compareById(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getId(), r2.getId());
	}
	
	public int compareByExternalId(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getExternalId(), r2.getExternalId());
	}

	public int compareByName(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getOrderName(), r2.getOrderName());
	}

	public int compareByPosition(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getPosition() == null ? null : r1.getPosition().getSortOrder(), r2.getPosition() == null ? null : r2.getPosition().getSortOrder());
	}

	public int compareByMaxLoad(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getMaxLoad(), r2.getMaxLoad());
	}

	public int compareByTeachingPreference(InstructorInterface r1, InstructorInterface r2) {
		return compare(r1.getTeachingPreference() == null ? null : r1.getTeachingPreference().getId(), r2.getTeachingPreference() == null ? null : r2.getTeachingPreference().getId());
	}

	protected int compareByColumn(InstructorInterface r1, InstructorInterface r2) {
		switch (iColumn) {
		case NAME: return compareByName(r1, r2);
		case ID: return compareByExternalId(r1, r2);
		case MAX_LOAD: return compareByMaxLoad(r1, r2);
		case POSITION: return compareByPosition(r1, r2);
		case TEACHING_PREF: return compareByTeachingPreference(r1, r2);
		default: return compareByName(r1, r2);
		}
	}
	
	public static boolean isApplicable(InstructorsColumn column) {
		switch (column) {
		case SELECTION:
		case NAME:
		case ID:
		case MAX_LOAD:
		case POSITION:
		case TEACHING_PREF:
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public int compare(InstructorInterface r1, InstructorInterface r2) {
		int cmp = compareByColumn(r1, r2);
		if (cmp == 0) cmp = compareByName(r1, r2);
		if (cmp == 0) cmp = compareById(r1, r2);
		return (iAsc ? cmp : -cmp);
	}
	
	protected int compare(String s1, String s2) {
		if (s1 == null || s1.isEmpty()) {
			return (s2 == null || s2.isEmpty() ? 0 : 1);
		} else {
			return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
		}
	}
	
	protected int compare(Number n1, Number n2) {
		return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? -1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
	}
	
	protected int compare(Boolean b1, Boolean b2) {
		return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? -1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
	}
}