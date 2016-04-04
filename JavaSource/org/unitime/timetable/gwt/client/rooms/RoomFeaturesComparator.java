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
package org.unitime.timetable.gwt.client.rooms;

import java.util.Comparator;
import java.util.Iterator;

import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.FeatureInterface;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomFeaturesColumn;

/**
 * @author Tomas Muller
 */
public class RoomFeaturesComparator implements Comparator<FeatureInterface> {
	private RoomFeaturesColumn iColumn;
	private boolean iAsc;
	
	public RoomFeaturesComparator(RoomFeaturesColumn column, boolean asc) {
		iColumn = column;
		iAsc = asc;
	}

	public int compareById(FeatureInterface r1, FeatureInterface r2) {
		return compare(r1.getId(), r2.getId());
	}
	
	public int compareByName(FeatureInterface r1, FeatureInterface r2) {
		return compare(r1.getLabel(), r2.getLabel());
	}

	public int compareByAbbreviation(FeatureInterface r1, FeatureInterface r2) {
		return compare(r1.getAbbreviation(), r2.getAbbreviation());
	}

	
	public int compareByDepartment(FeatureInterface r1, FeatureInterface r2) {
		return compare(r1.getDepartment() == null ? null : r1.getDepartment().getExtAbbreviationWhenExist(),
				r2.getDepartment() == null ? null : r2.getDepartment().getExtAbbreviationWhenExist());
	}
	
	public int compareByType(FeatureInterface r1, FeatureInterface r2) {
		return compare(r1.getType() == null ? null : r1.getType().getAbbreviation(), r2.getType() == null ? null : r2.getType().getAbbreviation());
	}

	
	public int compareByRooms(FeatureInterface r1, FeatureInterface r2) {
		if (r1.hasRooms()) {
			if (r2.hasRooms()) {
				Iterator<FilterRpcResponse.Entity> i1 = r1.getRooms().iterator();
				Iterator<FilterRpcResponse.Entity> i2 = r2.getRooms().iterator();
				while (i1.hasNext() && i2.hasNext()) {
					int cmp = compare(i1.next().getName(), i2.next().getName());
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
			return r2.hasRooms() ? -1 : 0;
		}
	}

	protected int compareByColumn(FeatureInterface r1, FeatureInterface r2) {
		switch (iColumn) {
		case NAME: return compareByName(r1, r2);
		case ABBREVIATION: return compareByAbbreviation(r1, r2);
		case TYPE: return compareByType(r1, r2);
		case DEPARTMENT: return compareByDepartment(r1, r2);
		case ROOMS: return compareByRooms(r1, r2);
		default: return compareByName(r1, r2);
		}
	}
	
	public static boolean isApplicable(RoomFeaturesColumn column) {
		switch (column) {
		case NAME:
		case ABBREVIATION:
		case DEPARTMENT:
		case TYPE:
		case ROOMS:
			return true;
		default:
			return false;
		}
	}
	
	@Override
	public int compare(FeatureInterface r1, FeatureInterface r2) {
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
		return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
	}
	
	protected int compare(Boolean b1, Boolean b2) {
		return (b1 == null ? b2 == null ? 0 : -1 : b2 == null ? 1 : (b1.booleanValue() == b2.booleanValue()) ? 0 : (b1.booleanValue() ? 1 : -1));
	}
}
