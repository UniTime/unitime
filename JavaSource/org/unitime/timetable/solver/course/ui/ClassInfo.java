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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Query;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.LocationDAO;

/**
 * @author Tomas Muller
 */
public class ClassInfo implements Serializable, Comparable<ClassInfo> {
	private static final long serialVersionUID = 7324981486913342471L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
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
    	iClassName = clazz.getClassLabel(ApplicationProperty.SolverShowClassSufix.isTrue(), ApplicationProperty.SolverShowConfiguratioName.isTrue());
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
	
	public static Map<ClassAssignment, Set<Long>> findAllRelatedAssignments(Long classId, boolean useRealStudents) {
		Map<Long, ClassAssignment> assignments = new HashMap<Long, ClassAssignment>();
		Map<ClassAssignment, Set<Long>> conflicts = new HashMap<ClassAssignment, Set<Long>>();
		Query q = null;
		if (!useRealStudents) {			
			q = LocationDAO.getInstance().getSession()
		    	    .createQuery("select e.clazz.committedAssignment, e.studentId "+
		    	        	"from StudentEnrollment e, StudentEnrollment x "+
		    	        	"where x.clazz.uniqueId = :classId and x.studentId = e.studentId and e.clazz != x.clazz and " + 
		    	        	"e.solution.commited = true and x.solution.commited = true")
		            .setLong("classId", classId);
		} else {
			q = LocationDAO.getInstance().getSession()
		    	    .createQuery("select e.clazz.committedAssignment, e.student.uniqueId "+
		    	        	"from StudentClassEnrollment e, StudentClassEnrollment x "+
		    	        	"where x.clazz.uniqueId = :classId and x.student = e.student and e.clazz != x.clazz ")
		            .setLong("classId", classId);
		}
		for (Object[] line:(List<Object[]>) q.setCacheable(true).list()) {
			Assignment assignment = (Assignment) line[0];
			Long studentId = (Long) line[1];
			ClassAssignment ca = assignments.get(assignment.getClassId());
			if (ca == null) { ca = new ClassAssignment(assignment); assignments.put(assignment.getClassId(), ca); }
			Set<Long> studentIds = conflicts.get(ca);
			if (studentIds == null) { studentIds = new HashSet<Long>(); conflicts.put(ca, studentIds); }
			studentIds.add(studentId);
		}
		return conflicts;
	}
}