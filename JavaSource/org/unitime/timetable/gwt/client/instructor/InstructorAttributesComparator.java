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
import java.util.Iterator;

import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributesColumn;

/**
 * @author Tomas Muller
 */
public class InstructorAttributesComparator implements Comparator<AttributeInterface> {
	private AttributesColumn iColumn;
	private boolean iAsc;
	
	public InstructorAttributesComparator(AttributesColumn column, boolean asc) {
		iColumn = column;
		iAsc = asc;
	}

	public int compareById(AttributeInterface r1, AttributeInterface r2) {
		return compare(r1.getId(), r2.getId());
	}
	
	public int compareByName(AttributeInterface r1, AttributeInterface r2) {
		return compare(r1.getName(), r2.getName());
	}

	public int compareByCode(AttributeInterface r1, AttributeInterface r2) {
		return compare(r1.getCode(), r2.getCode());
	}

	
	public int compareByParent(AttributeInterface r1, AttributeInterface r2) {
		return compare(r1.getParentName(), r2.getParentName());
	}
	
	public int compareByType(AttributeInterface r1, AttributeInterface r2) {
		return compare(r1.getType() == null ? null : r1.getType().getAbbreviation(), r2.getType() == null ? null : r2.getType().getAbbreviation());
	}

	
	public int compareByInstructors(AttributeInterface r1, AttributeInterface r2) {
		if (r1.hasInstructors()) {
			if (r2.hasInstructors()) {
				Iterator<InstructorInterface> i1 = r1.getInstructors().iterator();
				Iterator<InstructorInterface> i2 = r2.getInstructors().iterator();
				while (i1.hasNext() && i2.hasNext()) {
					int cmp = compare(i1.next().getOrderName(), i2.next().getOrderName());
					if (cmp != 0) return cmp;
				}
				if (i1.hasNext()) {
					return i2.hasNext() ? 0 : 1;
				} else {
					return i2.hasNext() ? -1 : 0;
				}
			} else {
				return 1;
			}
		} else {
			return r2.hasInstructors() ? -1 : 0;
		}
	}

	protected int compareByColumn(AttributeInterface r1, AttributeInterface r2) {
		switch (iColumn) {
		case NAME: return compareByName(r1, r2);
		case CODE: return compareByCode(r1, r2);
		case TYPE: return compareByType(r1, r2);
		case PARENT: return compareByParent(r1, r2);
		case INSTRUCTORS: return compareByInstructors(r1, r2);
		default: return compareByName(r1, r2);
		}
	}
	
	public static boolean isApplicable(AttributesColumn column) {
		switch (column) {
		case NAME:
		case CODE:
		case PARENT:
		case TYPE:
		case INSTRUCTORS:
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public int compare(AttributeInterface r1, AttributeInterface r2) {
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