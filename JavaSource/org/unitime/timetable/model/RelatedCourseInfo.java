/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */ 
 
package org.unitime.timetable.model;

import java.util.List;

import org.unitime.timetable.model.base.BaseRelatedCourseInfo;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;



public class RelatedCourseInfo extends BaseRelatedCourseInfo {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public RelatedCourseInfo () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public RelatedCourseInfo (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public RelatedCourseInfo (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.CourseEvent event,
		org.unitime.timetable.model.CourseOffering course,
		java.lang.Long ownerId,
		java.lang.Integer ownerType) {

		super (
			uniqueId,
			event,
			course,
			ownerId,
			ownerType);
	}

/*[CONSTRUCTOR MARKER END]*/
	public static List findByOwnerIdType(org.hibernate.Session hibSession, Long ownerId, Integer ownerType) {
	    return (hibSession.
	        createQuery("select o from RelatedCourseInfo o where o.ownerId=:ownerId and o.ownerType=:ownerType").
	        setLong("ownerId", ownerId).
	        setInteger("ownerType", ownerType).
	        setCacheable(true).list());
	}
	
	public static List findByOwnerIdType(Long ownerId, Integer ownerType) {
	    return (findByOwnerIdType(new RelatedCourseInfoDAO().getSession(), ownerId, ownerType));
	}
	
	
	private Object iOwnerObject = null;
	public Object getOwnerObject() {
	    if (iOwnerObject!=null) return iOwnerObject;
	    switch (getOwnerType()) {
	        case ExamOwner.sOwnerTypeClass : 
	            iOwnerObject = new Class_DAO().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeConfig : 
	            iOwnerObject = new InstrOfferingConfigDAO().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeCourse : 
	            iOwnerObject = new CourseOfferingDAO().get(getOwnerId());
	            return iOwnerObject;
	        case ExamOwner.sOwnerTypeOffering : 
	            iOwnerObject = new InstructionalOfferingDAO().get(getOwnerId());
	            return iOwnerObject;
	        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
	    }
	}
	
    public void setOwner(Class_ clazz) {
        setOwnerId(clazz.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeClass);
        setCourse(clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering());
    }

    public void setOwner(InstrOfferingConfig config) {
        setOwnerId(config.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeConfig);
        setCourse(config.getControllingCourseOffering());
    }

    public void setOwner(CourseOffering course) {
        setOwnerId(course.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeCourse);
        setCourse(course);
    }

    public void setOwner(InstructionalOffering offering) {
        setOwnerId(offering.getUniqueId());
        setOwnerType(ExamOwner.sOwnerTypeOffering);
        setCourse(offering.getControllingCourseOffering());
    }
    
    public CourseOffering computeCourse() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering();
            case ExamOwner.sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering();
            case ExamOwner.sOwnerTypeCourse : 
                return (CourseOffering)owner;
            case ExamOwner.sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int compareTo(RelatedCourseInfo owner) {
        CourseOffering c1 = getCourse();
        CourseOffering c2 = owner.getCourse();
        int cmp = 0;
        
        cmp = c1.getSubjectAreaAbbv().compareTo(c2.getSubjectAreaAbbv());
        if (cmp!=0) return cmp;
        
        cmp = c1.getCourseNbr().compareTo(c2.getCourseNbr());
        if (cmp!=0) return cmp;
        
        cmp = getOwnerType().compareTo(owner.getOwnerType());
        if (cmp!=0) return cmp;
        
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : return new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY).compare(getOwnerObject(), owner.getOwnerObject());
            case ExamOwner.sOwnerTypeConfig : return new InstrOfferingConfigComparator(null).compare(getOwnerObject(), owner.getOwnerObject());
        }
           
        return getOwnerId().compareTo(owner.getOwnerId());
    }
    
    public List getStudents() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public List getStudentIds() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public List getInstructors() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.uniqueId = :eventOwnerId and ci.lead=true")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
                    "where co.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return new RelatedCourseInfoDAO().getSession().createQuery(
                    "select distinct i from " +
                    "Class_ c inner join c.classInstructors ci inner join ci.instructor i " +
                    "where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }

    public int countStudents() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return ((Number)new RelatedCourseInfoDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case ExamOwner.sOwnerTypeConfig : 
            return ((Number)new RelatedCourseInfoDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case ExamOwner.sOwnerTypeCourse : 
            return ((Number)new RelatedCourseInfoDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case ExamOwner.sOwnerTypeOffering : 
            return ((Number)new RelatedCourseInfoDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :eventOwnerId")
                    .setLong("eventOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String getLabel() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getClassLabel();
            case ExamOwner.sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).toString();
            case ExamOwner.sOwnerTypeCourse : 
                return ((CourseOffering)owner).getCourseName();
            case ExamOwner.sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getCourseName();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }


}
