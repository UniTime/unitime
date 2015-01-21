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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.util.BitSet;

import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.DatePatternDAO;

/**
 * @author Tomas Muller
 */
public class ClassDateInfo implements Serializable, Comparable<ClassDateInfo> {
	private static final long serialVersionUID = 1113308106992466641L;
	private static NaturalOrderComparator sCmp = new NaturalOrderComparator();
	private Long iId, iClassId;
	private String iName;
	private BitSet iPattern;
	private int iPreference;
	private transient DatePattern iDatePattern = null;
	
	public ClassDateInfo(Long id, Long classId, String name, BitSet pattern, int preference) {
		iId = id;
		iClassId = classId;
		iName = name;
		iPattern = pattern;
		iPreference = preference;
	}
	
	public ClassDateInfo(Assignment a, int preference) {
		iId = a.getDatePattern().getUniqueId();
		iClassId = a.getClassId();
		iName = a.getDatePattern().getName();
		iPattern = a.getDatePattern().getPatternBitSet();
		iPreference = preference;
	}
	
	public Long getId() { return iId; }
	public Long getClassId() { return iClassId; }
	public String getName() { return iName; }
	public BitSet getPattern() { return iPattern; }
	public int getPreference() { return iPreference; }
	
	public int hashCode() { return iId.hashCode(); }
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ClassDateInfo)) return false;
		return getId().equals(((ClassDateInfo)o).getId());
	}
	
	public String toString() { return getName(); }
	public String toHtml() { return "<span style='color:" + PreferenceLevel.int2color(getPreference()) + "'>" + getName() + "</span>"; }
	public String toLongHtml() {
		return "<span style='color:" + PreferenceLevel.int2color(getPreference()) + "'>" + getName() + "</span>" +
				" <img style=\"cursor: pointer;\" src=\"images/calendar.png\" border=\"0\" " +
					"onclick=\"showGwtDialog('Preview of " + getName() + "', 'user/dispDatePattern.jsp?id=" + getId() + "&class=" + getClassId() + "','840','520');\">";
	}

	public int compareTo(ClassDateInfo other) {
		int cmp = sCmp.compare(getName(), other.getName());
		return (cmp == 0 ? getId().compareTo(other.getId()) : cmp);
	}
	
	public boolean overlaps(ClassDateInfo date) {
		return getPattern().intersects(date.getPattern());
	}
	
	public DatePattern getDatePattern() {
		if (iDatePattern == null)
			iDatePattern = DatePatternDAO.getInstance().get(getId());
		return iDatePattern;
	}
}
