/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
