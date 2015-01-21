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
package org.unitime.timetable.solver.exam.ui;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


import org.cpsolver.exam.model.ExamInstructor;
import org.cpsolver.exam.model.ExamModel;
import org.cpsolver.exam.model.ExamStudent;
import org.cpsolver.ifs.model.Constraint;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;

/**
 * @author Tomas Muller
 */
public class ExamInfo implements Serializable, Comparable<ExamInfo> {
	private static final long serialVersionUID = -4407299089673481581L;
	protected String iExamLabel = null;
    protected Long iExamId = null;
    protected transient Exam iExam = null;
    protected Long iExamTypeId;
    protected transient ExamType iExamType = null;
    protected int iNrStudents;
    protected int iLength;
    protected int iMaxRooms;
    protected int iSeatingType;
    protected int iPrintOffset;
    protected Vector<ExamSectionInfo> iSections = null;
    protected Vector<ExamInstructorInfo> iInstructors = null;
    protected Vector<ExamSectionInfo> iSectionsIncludeCrosslistedDummies = null;
    
    private ExamInfo() {
    }
    
    public ExamInfo(org.cpsolver.exam.model.Exam exam) {
    	iExamTypeId = ((ExamModel)exam.getModel()).getProperties().getPropertyLong("Exam.Type", null);
        iExamId = exam.getId();
        iExamLabel = exam.getName();
        iLength = exam.getLength();
        iMaxRooms = exam.getMaxRooms();
        iNrStudents = exam.getSize();//Students().size();
        iSeatingType = (exam.hasAltSeating()?Exam.sSeatingTypeExam:Exam.sSeatingTypeNormal);
        if (!exam.getOwners().isEmpty()) {
            iSections = new Vector();
            for (org.cpsolver.exam.model.ExamOwner ecs: exam.getOwners()) {
                HashSet<Long> studentIds = new HashSet<Long>();
                for (Iterator i=ecs.getStudents().iterator();i.hasNext();) 
                    studentIds.add(((ExamStudent)i.next()).getId());
                iSections.add(new ExamSectionInfo(ecs.getId(), ecs.getName(), studentIds));
            }
        }
        iInstructors = new Vector();
        for (Constraint c: exam.constraints()) {
            if (c instanceof ExamInstructor) {
                ExamInstructor instructor = (ExamInstructor)c;
                iInstructors.add(new ExamInstructorInfo(instructor.getId(), null, instructor.getName()));
            }
        }
        iPrintOffset = (exam.getPrintOffset()==null?0:exam.getPrintOffset());
    }

    public ExamInfo(Exam exam) {
    	iExamType = exam.getExamType();
    	iExamTypeId = exam.getExamType().getUniqueId();
        iExamId = exam.getUniqueId();
        iExamLabel = exam.getLabel();
        iMaxRooms = exam.getMaxNbrRooms();
        iExam = exam;
        iLength = exam.getLength();
        iNrStudents = (exam.getExamSize() == null ? -1 : exam.getExamSize());
        iSeatingType = exam.getSeatingType().intValue();
        iPrintOffset = exam.examOffset();
    }
    
    public Long getExamTypeId() {
    	return iExamTypeId;
    }
    
    public String getExamTypeLabel() {
    	if (iExamType == null && iExamTypeId != null) {
    		iExamType = ExamTypeDAO.getInstance().get(iExamTypeId);
    	}
    	return (iExamType == null ? "Unknown" : iExamType.getLabel());
    }
    
    public ExamType getExamType() {
    	if (iExamType == null && iExamTypeId != null) {
    		iExamType = ExamTypeDAO.getInstance().get(iExamTypeId);
    	}
    	return iExamType;
    }

    public Long getExamId() {
        return iExamId;
    }
    
    public int getPrintOffset() {
        return iPrintOffset;
    }
    
    public Exam getExam() {
        if (iExam==null)
            iExam = new ExamDAO().get(iExamId);
        return iExam;
    }
    
    public Exam getExam(org.hibernate.Session hibSession) {
        return new ExamDAO().get(iExamId, hibSession);
    }
    
    public String getExamName() {
        return (iExamLabel==null?getExam().getLabel():iExamLabel);
    }
    
    public String getExamNameHtml() {
        String name = getExamName();
        if (name.length()>50)
            return "<span title='"+name+"'>"+name.substring(0,50)+"...</span>";
        else
            return name;
    }

    public int getNrStudents() {
        if (iNrStudents<0) {
        	Set<Long> studentIds = new HashSet<Long>();
        	for (ExamSectionInfo section: getSections())
        		studentIds.addAll(section.getStudentIds());
        	iNrStudents = studentIds.size();
        }
        return iNrStudents;
    }
    
    public int getSeatingType() {
        return iSeatingType;
    }
    
    public String getSeatingTypeLabel() {
        return Exam.sSeatingTypes[iSeatingType];
    }

    public int getLength() {
        return iLength;
    }

    public int getMaxRooms() {
        return iMaxRooms;
    }
    
    public void createSections(Hashtable<Long,Set<Long>> students) {
        iSections = new Vector();
        for (Iterator i=new TreeSet(getExam().getOwners()).iterator();i.hasNext();) {
        	ExamSectionInfo section = new ExamSectionInfo((ExamOwner)i.next());
        	if (students != null) {
        		Set<Long> studentsOfOwner = students.get(section.getOwner().getUniqueId());
        		section.setStudentIds(studentsOfOwner == null ? new HashSet<Long>() : studentsOfOwner);
        	}
        	iSections.add(section);
        }
    }

    public Vector<ExamSectionInfo> getSections() {
        if (iSections==null) createSections(null);
        return iSections;
    }
    
    public void createSectionsIncludeCrosslistedDummies(Hashtable<Long,Hashtable<Long,Set<Long>>> students) {
    	iSectionsIncludeCrosslistedDummies = new Vector();
    	for (ExamSectionInfo original: getSections()) {
        	ExamOwner owner = original.getOwner();
            if (owner.getCourse().getInstructionalOffering().getCourseOfferings().size()>1) {
            	Hashtable<Long, Set<Long>> studentsOfOwner = (students == null ? null : students.get(owner.getUniqueId()));
            	ExamSectionInfo section = new ExamSectionInfo(owner);
            	iSectionsIncludeCrosslistedDummies.add(section);
            	for (Iterator j=owner.getCourse().getInstructionalOffering().getCourseOfferings().iterator();j.hasNext();) {
            		CourseOffering course = (CourseOffering)j.next();
            		if (course.isIsControl()) continue;
            		ExamOwner dummy = new ExamOwner();
            		dummy.setOwnerId(owner.getOwnerId());
            		dummy.setOwnerType(owner.getOwnerType());
            		dummy.setCourse(course);
            		ExamSectionInfo dummySection = new ExamSectionInfo(dummy);
            		dummySection.setMaster(section);
            		if (students != null) {
                		if (studentsOfOwner != null) {
                			Set<Long> studentsOfCourse = studentsOfOwner.get(course.getUniqueId());
                			dummySection.setStudentIds(studentsOfCourse == null ? new HashSet<Long>() : studentsOfCourse);
                		} else dummySection.setStudentIds(new HashSet<Long>());
            		}
            		iSectionsIncludeCrosslistedDummies.add(dummySection);
            	}
        		if (students != null) {
            		if (studentsOfOwner != null) {
            			Set<Long> studentsOfCourse = studentsOfOwner.get(owner.getCourse().getUniqueId());
            			section.setStudentIds(studentsOfCourse == null ? new HashSet<Long>() : studentsOfCourse);
            		} else section.setStudentIds(new HashSet<Long>());
        		}
            	section.setMaster(section);
            } else {
            	iSectionsIncludeCrosslistedDummies.add(original);
            }
        }
    }

    
    public Vector<ExamSectionInfo> getSectionsIncludeCrosslistedDummies() {
    	if (iSectionsIncludeCrosslistedDummies == null) createSectionsIncludeCrosslistedDummies(null);
        return iSectionsIncludeCrosslistedDummies;
    }

    public Set<Long> getStudentIds() {
        HashSet<Long> studentIds = new HashSet();
        for (ExamSectionInfo section : getSections()) {
            studentIds.addAll(section.getStudentIds());
        }
        return studentIds;
    }
    
    public String getSectionName(String delim) {
        String name = "";
        for (Enumeration e=getSections().elements();e.hasMoreElements();) {
            ExamSectionInfo info = (ExamSectionInfo)e.nextElement();
            name += info.getName();
            if (e.hasMoreElements()) name += delim;
        }
        return name;
    }

    public Vector<ExamInstructorInfo> getInstructors() {
        if (iInstructors==null) {
            iInstructors = new Vector();
            for (Iterator i=new TreeSet(getExam().getInstructors()).iterator();i.hasNext();)
                iInstructors.add(new ExamInstructorInfo((DepartmentalInstructor)i.next()));
        }
        return iInstructors;
    }
    
    public boolean hasInstructor(Long instructorId) {
        if (iInstructors!=null) {
            for (ExamInstructorInfo instructor : iInstructors)
                if (instructor.getId().equals(instructorId)) return true;
        } else {
            for (Iterator i=getExam().getInstructors().iterator();i.hasNext();)
                if (((DepartmentalInstructor)i.next()).getUniqueId().equals(instructorId)) return true;
        }
        return false;
    }
    
    public String getInstructorName(String delim) {
        String name = "";
        for (Enumeration e=getInstructors().elements();e.hasMoreElements();) {
            ExamInstructorInfo info = (ExamInstructorInfo)e.nextElement();
            name += info.getName();
            if (e.hasMoreElements()) name += delim;
        }
        return name;
    }

    public int hashCode() {
        return getExamId().hashCode();
    }
    
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ExamInfo)) return false;
        return ((ExamInfo)o).getExamId().equals(getExamId());
    }
    
    public int compareTo(ExamInfo info) {
        int cmp = getExamName().compareTo(info.getExamName());
        if (cmp!=0) return cmp;
        return getExamId().compareTo(info.getExamId());
    }
    
    public String toString() {
        return getExamName();
    }
    
    public class ExamSectionInfo implements Serializable, Comparable<ExamSectionInfo> {
		private static final long serialVersionUID = 6052996415395186994L;
		protected Long iId;
        protected String iName;
        protected int iNrStudents = -1;
        protected transient ExamOwner iOwner = null;
        protected Set<Long> iStudentIds = null;
        protected ExamSectionInfo iMaster = null;
        public ExamSectionInfo(Long id, String name, Set<Long> studentIds) {
            iId = id;
            iName = name;
            iNrStudents = studentIds.size();
            iStudentIds = studentIds;
        }
        public ExamSectionInfo(ExamOwner owner) {
            iOwner = owner;
            iId = owner.getUniqueId();
            iName = owner.getLabel();
            iStudentIds = null;
        }
        public ExamSectionInfo(ExamOwner owner, Set<Long> studentIds) {
            this(owner);
            if (studentIds!=null) {
                iNrStudents = studentIds.size();
                iStudentIds = studentIds;
            }
        }
        public Set<Long> getStudentIds() {
            if (iStudentIds==null) {
            	if (getMaster()!=null)
                	iStudentIds = new HashSet<Long>(getMaster().getOwner().getStudentIds(getOwner().getCourse()));
            	else
                	iStudentIds = new HashSet<Long>(getOwner().getStudentIds());
            }
            iNrStudents = iStudentIds.size();
            return iStudentIds;
        }
        public void setStudentIds(Set<Long> studentIds) {
        	iStudentIds = studentIds;
        	iNrStudents = studentIds.size();
        }
        public Long getId() { return iId; }
        public Long getOwnerId() { return getOwner().getOwnerId(); }
        public Integer getOwnerType() { return getOwner().getOwnerType(); }
        public ExamOwner getOwner() {
            if (iOwner==null)
                iOwner = new ExamOwnerDAO().get(getId());
            return iOwner;
        }
        public String getName() { return iName; }
        public int getNrStudents() {
            if (iNrStudents<0) {
            	if (getMaster()!=null)
            		iNrStudents = getMaster().getOwner().getSize(getOwner().getCourse());
            	else
                    iNrStudents = getOwner().getSize();
            }
            return iNrStudents;
        }
        public ExamInfo getExam() {
            return ExamInfo.this;
        }
        public ExamAssignment getExamAssignment() {
            if (ExamInfo.this instanceof ExamAssignment)
                return (ExamAssignment)ExamInfo.this;
            return null;
        }
        public ExamAssignmentInfo getExamAssignmentInfo() {
            if (ExamInfo.this instanceof ExamAssignmentInfo)
                return (ExamAssignmentInfo)ExamInfo.this;
            return null;
        }
        public String getSubject() {
            return getOwner().getSubject();
        }
        public String getCourseNbr() {
            return getOwner().getCourseNbr();
        }
        public String getItype() {
            return getOwner().getItype();
        }
        public String getSection() {
            return getOwner().getSection();
        }
        public int compareTo(ExamSectionInfo info) {
            int cmp = getOwner().compareTo(info.getOwner());
            if (cmp!=0) return cmp;
            return getExam().compareTo(info.getExam());
        }
        public int hasCode() {
            return getId().hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof ExamSectionInfo)) return false;
            return getId().equals(((ExamSectionInfo)o).getId());
        }
        public String toString() {
            return getName();
        }
        public ExamSectionInfo getMaster() {
        	return iMaster;
        }
        public void setMaster(ExamSectionInfo master) {
        	iMaster = master;
        }
    }
    
    public class ExamInstructorInfo implements Serializable, Comparable<ExamInstructorInfo> {
		private static final long serialVersionUID = -6843290015053081071L;
		protected Long iId;
        protected String iExternalUniqueId = null;
        protected String iName = null;
        protected transient DepartmentalInstructor iInstructor;
        public ExamInstructorInfo(Long id, String externalUniqueId, String name) {
            iId = id;
            iExternalUniqueId = externalUniqueId;
            iName = name;
        }
        public ExamInstructorInfo(DepartmentalInstructor instructor) {
            iId = instructor.getUniqueId();
            iName = instructor.getNameLastFirst();
            iExternalUniqueId = instructor.getExternalUniqueId();
            iInstructor = instructor;
        }
        public Long getId() { return iId; }
        public String getName() { return iName; }
        public String getExternalUniqueId() {
            if (iExternalUniqueId==null && iInstructor==null)
                iExternalUniqueId = getInstructor().getExternalUniqueId();
            return iExternalUniqueId; 
        }
        public DepartmentalInstructor getInstructor() {
            if (iInstructor==null)
                iInstructor = new DepartmentalInstructorDAO().get(getId());
            return iInstructor;
        }
        public ExamInfo getExam() {
            return ExamInfo.this;
        }
        public int compareTo(ExamInstructorInfo i) {
            int cmp = getName().compareTo(i.getName());
            if (cmp!=0) return cmp;
            return getId().compareTo(i.getId());
        }
        public int hashCode() {
            if (getExternalUniqueId()!=null) return getExternalUniqueId().hashCode();
            return getId().hashCode();
        }
        public boolean equals(Object o) {
            if (o==null || !(o instanceof ExamInstructorInfo)) return false;
            ExamInstructorInfo i = (ExamInstructorInfo)o;
            if (getExternalUniqueId()!=null && getExternalUniqueId().equals(i.getExternalUniqueId())) return true;
            return getId().equals(i.getId());
        }
    }
    
    private ExamInstructorInfo instructorInfo(DepartmentalInstructor i) {
        return new ExamInstructorInfo(i);
    }
    
    public static ExamInstructorInfo createInstructorInfo(DepartmentalInstructor i) {
        return new ExamInfo().instructorInfo(i);
    }
    
    public boolean isOfSubjectArea(SubjectArea subject) {
        if (subject==null) return true;
        for (ExamSectionInfo section: getSections())
            if (subject.equals(section.getOwner().getCourse().getSubjectArea())) return true;
        return false;
    }
}
