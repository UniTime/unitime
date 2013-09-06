/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2009 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.course.ui;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao.Class_DAO;

/**
 * @author Tomas Muller
 */
public class ClassInfo implements Serializable, Comparable<ClassInfo> {
	private static final long serialVersionUID = 7324981486913342471L;
	protected String iClassName = null;
    protected String iClassTitle = null;
    protected Long iClassId = null;
    protected transient Class_ iClass = null;
    protected int iNrRooms;
    protected int iLimit;
    protected int iEnrollment;
    protected TreeSet<ClassInstructorInfo> iInstructors = new TreeSet();
    protected Set<Long> iParents = new HashSet();
    protected Set<Long> iStudents = new HashSet();
    protected Long iConfigId = null;
    protected boolean iSingleClass = false;
    
    public ClassInfo(Class_ clazz) {
    	iClassId = clazz.getUniqueId();
    	iClassName = clazz.getClassLabel();
    	iClassTitle = clazz.getClassLabelWithTitle();
    	iClass = clazz;
    	iNrRooms = (clazz.getNbrRooms()==null?1:clazz.getNbrRooms().intValue());
    	iLimit = clazz.getClassLimit();
    	iEnrollment = (clazz.getEnrollment()==null?0:clazz.getEnrollment().intValue());
    	for (Iterator i=clazz.getClassInstructors().iterator(); i.hasNext();) {
    		iInstructors.add(new ClassInstructorInfo((ClassInstructor)i.next()));
    	}
    	Class_ parent = clazz.getParentClass();
    	while (parent!=null) {
    		iParents.add(parent.getUniqueId());
    		parent = parent.getParentClass();
    	}
    	iStudents.addAll(clazz.getEnrolledStudentIds());
    	iSingleClass = (clazz.getSchedulingSubpart().getClasses().size()==1);
    	iConfigId = clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId();
    }
    
    public Set<Long> getParents() {
    	return iParents;
    }
    
    public Set<Long> getStudents() {
    	return iStudents;
    }
    
    public Set<ClassInstructorInfo> getInstructors() {
    	return iInstructors;
    }
    
    public boolean isSingleClass() {
    	return iSingleClass;
    }
    
    public Long getConfligId() {
    	return iConfigId;
    }
    
    public String getLeadingInstructorNames(String delim) {
    	if (getInstructors().isEmpty()) return "";
    	String s = "";
    	for (ClassInstructorInfo inst: getInstructors()) {
    		if (!inst.isLead()) continue;
    		if (s.length()>0) s+=delim;
    		s+=inst.getName();
    	}
    	return s;
    }
    
    public String getClassName() {
    	return iClassName;
    }
    
    public String getClassNameHtml() {
    	return "<span title='"+iClassTitle+"'>"+iClassName+"</span>";
    }
    
    public Long getClassId() {
    	return iClassId;
    }
    
    public int hashCode() {
    	return getClassId().hashCode();
    }
    
    public boolean equals(Object o) {
    	if (o==null || !(o instanceof ClassInfo)) return false;
    	return getClassId().equals(((ClassInfo)o).getClassId());
    }
	
	public int compareTo(ClassInfo classInfo) {
		int cmp = getClassName().compareTo(classInfo.getClassName());
		if (cmp!=0) return cmp;
		return getClassId().compareTo(classInfo.getClassId());
	}
	
	public Class_ getClazz() {
		if (iClass==null)
			iClass = Class_DAO.getInstance().get(getClassId());
		return iClass;
	}
	
	public Class_ getClazz(org.hibernate.Session hibSession) {
		return new Class_DAO().get(getClassId(), hibSession);
	}

	public int getNumberOfRooms() {
		return iNrRooms;
	}
	
	public int getClassLimit() {
		return iLimit;
	}
	
	public int getEnrollment() {
		return iEnrollment;
	}
	
	public String getClassDivSec() {
		return getClazz().getDivSecNumber();
	}
	
	public String getRoomRatio() {
		return new DecimalFormat("0.0").format(getClazz().getRoomRatio()==null?1.0f:getClazz().getRoomRatio().floatValue());
	}
	
	public int getMinRoomCapacity() {
		return getClazz().getMinRoomLimit();
	}
	
	public String getManager() {
		return getClazz().getManagingDept().getLabel();
	}
	
	public boolean shareInstructor(ClassInfo info) {
		for (ClassInstructorInfo i1 : getInstructors()) {
			if (!i1.isLead()) continue;
			for (ClassInstructorInfo i2 : info.getInstructors()){
				if (i2.isLead() && i1.equals(i2)) return true;
			}
		}
		return false;
	}
}