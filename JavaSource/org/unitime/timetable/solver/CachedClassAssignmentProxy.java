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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Collection;
import java.util.Vector;
import java.util.Enumeration;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;


/**
 * @author Tomas Muller
 */
public class CachedClassAssignmentProxy implements ClassAssignmentProxy {
	private static Object sNULL = Boolean.FALSE;
	private ClassAssignmentProxy iProxy;
	private Hashtable iAssignmentTable = new Hashtable();
	private Hashtable iAssignmentInfoTable = new Hashtable();
	
	public CachedClassAssignmentProxy(ClassAssignmentProxy proxy) {
		iProxy = proxy;
	}
	
	public Assignment getAssignment(Long classId) throws Exception {
		Object cached = iAssignmentTable.get(classId);
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(Assignment)cached);
		}
		Assignment assignment = iProxy.getAssignment(classId);
		iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
		return assignment;
	}
	
	public Assignment getAssignment(Class_ clazz) throws Exception {
		Object cached = iAssignmentTable.get(clazz.getUniqueId());
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(Assignment)cached);
		}
		Assignment assignment = iProxy.getAssignment(clazz);
		iAssignmentTable.put(clazz.getUniqueId(), (assignment==null?sNULL:assignment));
		return assignment;
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Long classId) throws Exception {
		Object cached = iAssignmentInfoTable.get(classId);
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentPreferenceInfo)cached);
		}
		AssignmentPreferenceInfo info = iProxy.getAssignmentInfo(classId);
		iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
		return info;
	}
	
	public AssignmentPreferenceInfo getAssignmentInfo(Class_ clazz) throws Exception {
		Object cached = iAssignmentInfoTable.get(clazz.getUniqueId());
		if (cached!=null) {
			return (sNULL.equals(cached)?null:(AssignmentPreferenceInfo)cached);
		}
		AssignmentPreferenceInfo info = iProxy.getAssignmentInfo(clazz);
		iAssignmentInfoTable.put(clazz.getUniqueId(), (info==null?sNULL:info));
		return info;
	}
	
	public Hashtable getAssignmentTable(Collection classesOrClassIds) throws Exception {
		Hashtable assignments = new Hashtable();
		Vector unknown = new Vector();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Object cached = iAssignmentTable.get(classId);
			if (cached!=null) {
				if (!sNULL.equals(cached)) assignments.put(classId, cached);
			} else {
				unknown.add(classOrClassId);
			}
		}
		Hashtable newAssignments = iProxy.getAssignmentTable(unknown);
		for (Enumeration e=unknown.elements();e.hasMoreElements();) {
			Object classOrClassId = e.nextElement();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Assignment assignment = (Assignment) newAssignments.get(classId);
			iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
			if (assignment!=null)
				assignments.put(classId, assignment);
		}
		return assignments;
	}
	
	public Hashtable getAssignmentInfoTable(Collection classesOrClassIds) throws Exception {
		Hashtable infos = new Hashtable();
		Vector unknown = new Vector();
		for (Iterator i=classesOrClassIds.iterator();i.hasNext();) {
			Object classOrClassId = i.next();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Object cached = iAssignmentInfoTable.get(classId);
			if (cached!=null) {
				if (!sNULL.equals(cached)) infos.put(classId, cached);
			} else {
				unknown.add(classOrClassId);
			}
		}
		Hashtable newInfos = iProxy.getAssignmentInfoTable(unknown);
		for (Enumeration e=unknown.elements();e.hasMoreElements();) {
			Object classOrClassId = e.nextElement();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			AssignmentPreferenceInfo info = (AssignmentPreferenceInfo) newInfos.get(classId);
			iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
			if (info!=null)
				infos.put(classId, info);
		}
		return infos;
	}
	
	public void setCache(Collection classesOrClassIds) throws Exception {
		Vector classesOrClassIdsVect = (classesOrClassIds instanceof Vector ? (Vector)classesOrClassIds : new Vector(classesOrClassIds));
		Hashtable newAssignments = iProxy.getAssignmentTable(classesOrClassIdsVect);
		Hashtable newInfos = iProxy.getAssignmentInfoTable(classesOrClassIdsVect);
		for (Enumeration e=classesOrClassIdsVect.elements();e.hasMoreElements();) {
			Object classOrClassId = e.nextElement();
			if (classOrClassId instanceof Object[]) classOrClassId = ((Object[])classOrClassId)[0];
			Long classId = (classOrClassId instanceof Class_ ? ((Class_)classOrClassId).getUniqueId() : (Long)classOrClassId);
			Assignment assignment = (Assignment) newAssignments.get(classId);
			iAssignmentTable.put(classId, (assignment==null?sNULL:assignment));
			AssignmentPreferenceInfo info = (AssignmentPreferenceInfo) newInfos.get(classId);
			iAssignmentInfoTable.put(classId, (info==null?sNULL:info));
		}
	}
}
