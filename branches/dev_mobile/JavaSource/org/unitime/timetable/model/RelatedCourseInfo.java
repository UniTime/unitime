/* 
 * UniTime 3.1 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC
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
 
package org.unitime.timetable.model;

import java.util.Collection;
import java.util.List;

import org.unitime.timetable.model.base.BaseRelatedCourseInfo;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;
import org.unitime.timetable.model.dao.RelatedCourseInfoDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;



/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class RelatedCourseInfo extends BaseRelatedCourseInfo implements Comparable<RelatedCourseInfo> {
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
            case ExamOwner.sOwnerTypeClass : return new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY).compare((Class_)getOwnerObject(), (Class_)owner.getOwnerObject());
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
    
    public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
        switch (getOwnerType()) {
        case ExamOwner.sOwnerTypeClass : 
            return new ExamOwnerDAO().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.uniqueId = :classId" +
        			" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student")
                    .setLong("classId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeConfig : 
            return new ExamOwnerDAO().getSession().createQuery(
            		"select distinct e from StudentClassEnrollment e, StudentClassEnrollment f where f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId = :configId" +
            		" and e.courseOffering.instructionalOffering = f.courseOffering.instructionalOffering and e.student = f.student")
                    .setLong("configId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeCourse : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select e from StudentClassEnrollment e where e.courseOffering.uniqueId = :courseId")
                    .setLong("courseId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case ExamOwner.sOwnerTypeOffering : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select e from StudentClassEnrollment e where e.courseOffering.instructionalOffering.uniqueId = :offeringId")
                    .setLong("offeringId", getOwnerId())
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
    
    public int getLimit() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getClassLimit();
            case ExamOwner.sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getLimit();
            case ExamOwner.sOwnerTypeCourse : 
                CourseOffering course = ((CourseOffering)owner);
                if (course.getReservation() != null)
                	return course.getReservation();
                return (course.getInstructionalOffering().getLimit() == null ? 0 : course.getInstructionalOffering().getLimit());
            case ExamOwner.sOwnerTypeOffering : 
                return (((InstructionalOffering)owner).getLimit()==null?0:((InstructionalOffering)owner).getLimit());
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String getLabel() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case ExamOwner.sOwnerTypeClass : 
                return ((Class_)owner).getClassLabel(getCourse());
            case ExamOwner.sOwnerTypeConfig : 
                return getCourse().getCourseName() + " [" + ((InstrOfferingConfig)owner).getName() + "]";
            case ExamOwner.sOwnerTypeCourse : 
                return ((CourseOffering)owner).getCourseName();
            case ExamOwner.sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getCourseName();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }


}
