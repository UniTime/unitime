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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;
import java.util.List;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.comparators.ClassComparator;

/**
 * @author Tomas Muller
 */
public class UnassignedClassRow implements Serializable, Comparable<UnassignedClassRow> {
	private static final long serialVersionUID = 1L;
	private Long iId = null;
	private String iName = null;
	private List<String> iInstructors = null;
	private int iNrStudents = 0;
	private String iInitial = null;
	private int iOrd = -1;
	private transient Class_ iClazz = null;
	
	public UnassignedClassRow(Long id, String name, List<String> instructors, int nrStudents, String initial, int ord) {
		iId = id;
		iName = name;
		iNrStudents = nrStudents;
		iInstructors = instructors;
		iInitial = initial;
		iOrd = ord;
	}
	
	public UnassignedClassRow(Long id, String name, List<String> instructors, int nrStudents, String initial, Class_ clazz) {
		iId = id;
		iName = name;
		iNrStudents = nrStudents;
		iInstructors = instructors;
		iInitial = initial;
		iClazz = clazz;
	}
	
	public Long getId() { return iId; }
	public String getOnClick() { return iId == null ? null : "showGwtDialog('Suggestions', 'suggestions.do?id="+iId+"&op=Reset','900','90%');"; }
	public String getName() { return iName; }
	public String getInstructor() {
		String instructors = "";
		if (iInstructors != null)
			for (String instructor: iInstructors)
				instructors += (instructors.isEmpty() ? "" : ", ") + instructor;
		return instructors;
	}
	public List<String> getInstructors() { return iInstructors; }
	public int getNrStudents() { return iNrStudents; }
	public String getInitial() { return iInitial; }

	public int compareTo(UnassignedClassRow ucr) {
		if (iOrd>=0 && ucr.iOrd>=0) {
			int cmp = Double.compare(iOrd, ucr.iOrd);
			if (cmp!=0) return cmp;
		} else if (iClazz!=null && ucr.iClazz!=null) {
			int cmp = (new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)).compare(iClazz, ucr.iClazz);
			if (cmp!=0) return cmp;
		}
		return getName().compareTo(ucr.getName());
	}
}
