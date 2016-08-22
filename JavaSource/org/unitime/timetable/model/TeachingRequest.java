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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseTeachingRequest;

public class TeachingRequest extends BaseTeachingRequest implements Comparable<TeachingRequest> {
	private static final long serialVersionUID = 1L;

	public TeachingRequest() {
		super();
	}

	@Override
	public String htmlLabel() {
		Set<TeachingClassRequest> requests = new TreeSet<TeachingClassRequest>(getClassRequests());
		String classes = null;
		for (TeachingClassRequest r: requests) {
			if (classes == null)
				classes = r.getTeachingClass().htmlLabel();
			else
				classes += ", " + r.getTeachingClass().htmlLabel();
		}
		return getOffering().getCourseName() + (classes == null ? "" : " " + classes);
	}

	@Override
	public Session getSession() {
		return getOffering().getSession();
	}

	@Override
	public Department getDepartment() {
		return getOffering().getDepartment();
	}
	
	public Map<SchedulingSubpart, List<TeachingClassRequest>> getSubparMap() {
		Map<SchedulingSubpart, List<TeachingClassRequest>> map = new HashMap<SchedulingSubpart, List<TeachingClassRequest>>();
		for (TeachingClassRequest r: getClassRequests()) {
			List<TeachingClassRequest> requests = map.get(r.getTeachingClass().getSchedulingSubpart());
			if (requests == null) {
				requests = new ArrayList<TeachingClassRequest>();
				map.put(r.getTeachingClass().getSchedulingSubpart(), requests);
			}
			requests.add(r);
		}
		return map;
	}
	
	public TeachingClassRequest getMasterRequest(boolean checkClassRequests) {
		Map<SchedulingSubpart, List<TeachingClassRequest>> map = getSubparMap();
		TeachingClassRequest master = null;
		for (Map.Entry<SchedulingSubpart, List<TeachingClassRequest>> e: map.entrySet()) {
			if (e.getValue().size() > 1) {
				if (checkClassRequests) {
					TeachingClassRequest r1 = e.getValue().get(0);
					for (int i = 1; i < e.getValue().size(); i++) {
						TeachingClassRequest r2 = e.getValue().get(i);
						if (!ToolBox.equals(r1.isAssignInstructor(), r2.isAssignInstructor())) return null;
						if (!ToolBox.equals(r1.isCommon(), r2.isCommon())) return null;
						if (!ToolBox.equals(r1.getPercentShare(), r2.getPercentShare())) return null;
						if (!ToolBox.equals(r1.isLead(), r2.isLead())) return null;
						if (!ToolBox.equals(r1.isCanOverlap(), r2.isCanOverlap())) return null;
					}
				}
				continue;
			}
			TeachingClassRequest adept = e.getValue().get(0);
			if (master == null) {
				master = adept;
			} else if (master.isParentOf(adept)) {
				master = adept;
			} else if (!adept.isParentOf(master) && adept.getTeachingClass().getSchedulingSubpart().getClasses().size() > master.getTeachingClass().getSchedulingSubpart().getClasses().size()) {
				master = adept;
			}
		}
		return master;
	}
	
	public static List<Class_> getClasses(Class_ master, Set<SchedulingSubpart> subparts) {
		List<Class_> classes = new ArrayList<Class_>();
		for (SchedulingSubpart subpart: subparts) {
			if (subpart.equals(master.getSchedulingSubpart())) {
				classes.add(master);
			} else if (subpart.isParentOf(master.getSchedulingSubpart())) {
				for (Class_ c: subpart.getClasses())
					if (c.isParentOf(master)) classes.add(c);
			} else if (master.getSchedulingSubpart().isParentOf(subpart)) {
				for (Class_ c: subpart.getClasses())
					if (master.isParentOf(c)) classes.add(c);
			} else {
				Class_ parent = master.getParentClass();
				while (parent != null && !parent.getSchedulingSubpart().isParentOf(subpart))
					parent = parent.getParentClass();
				if (parent != null) {
					for (Class_ c: subpart.getClasses())
						if (parent.isParentOf(c)) classes.add(c);
				} else {
					classes.addAll(subpart.getClasses());
				}
			}
		}
		return classes;
	}
		
	public boolean isStandard(TeachingClassRequest master) {
		if (master == null) return false;
		Set<SchedulingSubpart> subparts = new HashSet<SchedulingSubpart>();
		Set<Class_> classes = new HashSet<Class_>();
		for (TeachingClassRequest r: getClassRequests()) {
			classes.add(r.getTeachingClass());
			subparts.add(r.getTeachingClass().getSchedulingSubpart());
		}
		List<Class_> std = getClasses(master.getTeachingClass(), subparts);
		if (std.size() != classes.size()) return false;
		for (Class_ c: std) if (!classes.contains(c)) return false;
		return true;
	}
	
	public boolean canCombine(TeachingRequest other) {
		TeachingClassRequest m1 = getMasterRequest(true);
		if (m1 == null || !isStandard(m1)) return false;
		TeachingClassRequest m2 = other.getMasterRequest(true);
		if (m2 == null || m1.getTeachingClass().equals(m2.getTeachingClass()) || !other.isStandard(m2) || !m1.getTeachingClass().getSchedulingSubpart().equals(m2.getTeachingClass().getSchedulingSubpart())) return false;
		// different properties
		if (!ToolBox.equals(getTeachingLoad(), other.getTeachingLoad())) return false;
		if (!ToolBox.equals(getResponsibility(), other.getResponsibility())) return false;
		if (!ToolBox.equals(getSameCoursePreference(), other.getSameCoursePreference())) return false;
		if (!ToolBox.equals(getSameCommonPart(), other.getSameCommonPart())) return false;
		if (!ToolBox.equals(isAssignCoordinator(), other.isAssignCoordinator())) return false;
		// different preferences
		Set<Preference> p1 = getPreferences();
		Set<Preference> p2 = other.getPreferences();
		if (p1.size() != p2.size()) return false;
		p1: for (Preference p: p1) {
			for (Preference q: p2) {
				if (p.isSame(q) && p.getPrefLevel().equals(q.getPrefLevel())) continue p1;
			}
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(TeachingRequest r) {
		int cmp = getOffering().getControllingCourseOffering().compareTo(r.getOffering().getControllingCourseOffering());
		if (cmp != 0) return cmp;
		Iterator<TeachingClassRequest> i1 = new TreeSet<TeachingClassRequest>(getClassRequests()).iterator();
		Iterator<TeachingClassRequest> i2 = new TreeSet<TeachingClassRequest>(r.getClassRequests()).iterator();
		while (i1.hasNext() && i2.hasNext()) {
			cmp = i1.next().compareTo(i2.next());
			if (cmp != 0) return cmp;
		}
		if (i2.hasNext()) return -1;
		if (i1.hasNext()) return 1;
		return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(r.getUniqueId() == null ? -1 : r.getUniqueId());
	}
	
	public boolean isCancelled() {
		if (isAssignCoordinator()) return false;
		for (TeachingClassRequest tcr: getClassRequests()) {
			if (tcr.isAssignInstructor() && !tcr.getTeachingClass().isCancelled()) return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return getOffering().getCourseName() + " " + getClassRequests();
	}

}
