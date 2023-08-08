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
package org.unitime.timetable.solver;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashMap;

import org.unitime.timetable.interfaces.RoomAvailabilityInterface.TimeBlock;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public class CachedClassAssignmentProxy implements ClassAssignmentProxy {
	private static Object sNULL = Boolean.FALSE;
	private ClassAssignmentProxy iProxy;
	private Map<Long, Object> iAssignmentTable = new HashMap<Long, Object>();
	private Map<Long, Object> iAssignmentInfoTable = new HashMap<Long, Object>();
	
	public CachedClassAssignmentProxy(ClassAssignmentProxy proxy) {
		iProxy = proxy;
	}
	
	public AssignmentInfo getAssignment(Long classId) {
		Object cached = iAssignmentTable.get(classId);
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentInfo)cached);
		}
		AssignmentInfo assignment = iProxy.getAssignment(classId);
		iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
		return assignment;
	}
	
	public AssignmentInfo getAssignment(Class_ clazz) {
		Object cached = iAssignmentTable.get(clazz.getUniqueId());
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentInfo)cached);
		}
		AssignmentInfo assignment = iProxy.getAssignment(clazz);
		iAssignmentTable.put(clazz.getUniqueId(), (assignment==null?sNULL:assignment));
		return assignment;
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) {
		Object cached = iAssignmentInfoTable.get(classId);
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentPreferenceInfo)cached);
		}
		AssignmentPreferenceInfo info = iProxy.getAssignmentInfo(classId);
		iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
		return info;
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) {
		Object cached = iAssignmentInfoTable.get(clazz.getUniqueId());
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentPreferenceInfo)cached);
		}
		AssignmentPreferenceInfo info = iProxy.getAssignmentInfo(clazz);
		iAssignmentInfoTable.put(clazz.getUniqueId(), (info==null?sNULL:info));
		return info;
	}
	
	public Map<Long, AssignmentInfo> getAssignmentTable(Collection classesOrClassIds) {
		Map<Long, AssignmentInfo> assignments = new HashMap<Long, AssignmentInfo>();
		List<Object> unknown = new ArrayList<Object>();
		for (Object classOrClassId: classesOrClassIds) {
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Object cached = iAssignmentTable.get(classId);
			if (cached!=null) {
				if (!sNULL.equals(cached)) assignments.put(classId, (AssignmentInfo)cached);
			} else {
				unknown.add(classOrClassId);
			}
		}
		Map<Long, AssignmentInfo> newAssignments = iProxy.getAssignmentTable(unknown);
		for (Object classOrClassId: unknown) {
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			AssignmentInfo assignment = newAssignments.get(classId);
			iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
			if (assignment!=null)
				assignments.put(classId, assignment);
		}
		return assignments;
	}
	
	public Map<Long, AssignmentPreferenceInfo> getAssignmentInfoTable(Collection classesOrClassIds) {
		Map<Long, AssignmentPreferenceInfo> infos = new HashMap<Long, AssignmentPreferenceInfo>();
		List<Object> unknown = new ArrayList<Object>();
		for (Object classOrClassId: classesOrClassIds) {
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Object cached = iAssignmentInfoTable.get(classId);
			if (cached!=null) {
				if (!sNULL.equals(cached)) infos.put(classId, (AssignmentPreferenceInfo)cached);
			} else {
				unknown.add(classOrClassId);
			}
		}
		Map<Long, AssignmentPreferenceInfo> newInfos = iProxy.getAssignmentInfoTable(unknown);
		for (Object classOrClassId: unknown) {
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			AssignmentPreferenceInfo info = newInfos.get(classId);
			iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
			if (info!=null)
				infos.put(classId, info);
		}
		return infos;
	}
	
	public void setCache(Collection classesOrClassIds) {
		Map<Long, AssignmentInfo> newAssignments = iProxy.getAssignmentTable(classesOrClassIds);
		Map<Long, AssignmentPreferenceInfo> newInfos = iProxy.getAssignmentInfoTable(classesOrClassIds);
		for (Object classOrClassId: classesOrClassIds) {
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			AssignmentInfo assignment = (AssignmentInfo) newAssignments.get(classId);
			iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
			AssignmentPreferenceInfo info = (AssignmentPreferenceInfo) newInfos.get(classId);
			iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
		}
	}
	
	@Override
	public boolean hasConflicts(Long offeringId) {
		return iProxy.hasConflicts(offeringId);
	}

	@Override
	public Set<AssignmentInfo> getConflicts(Long classId) {
		return iProxy.getConflicts(classId);
	}
	
	@Override
	public Set<TimeBlock> getConflictingTimeBlocks(Long classId) {
		return iProxy.getConflictingTimeBlocks(classId);
	}
}
