/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.base.BaseExamOwner;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.ExamOwnerDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.util.Constants;

public class ExamOwner extends BaseExamOwner implements Comparable<ExamOwner> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamOwner () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamOwner (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ExamOwner (
	        java.lang.Long uniqueId,
	        org.unitime.timetable.model.Exam exam,
	        java.lang.Long ownerId,
	        java.lang.Integer ownerType,
	        org.unitime.timetable.model.CourseOffering course) {

		super (
			uniqueId,
			exam,
			ownerId,
			ownerType,
			course);
	}

/*[CONSTRUCTOR MARKER END]*/

	
	public static final int sOwnerTypeClass = 3;
	public static final int sOwnerTypeConfig = 2;
	public static final int sOwnerTypeCourse = 1;
	public static final int sOwnerTypeOffering = 0;
	public static String[] sOwnerTypes = new String[] {"Offering", "Course", "Config", "Class"};
	
	public static ExamOwner findByOwnerIdType(Long ownerId, Integer ownerType) {
	    return (ExamOwner)new ExamOwnerDAO().
	        getSession().
	        createQuery("select o from ExamOwner o where o.ownerId=:ownerId and o.ownerType=:ownerType").
	        setLong("ownerId", ownerId).
	        setInteger("ownerType", ownerType).
	        setCacheable(true).uniqueResult();
	}
	
	
	private Object iOwnerObject = null;
	public Object getOwnerObject() {
	    if (iOwnerObject!=null) return iOwnerObject;
	    switch (getOwnerType()) {
	        case sOwnerTypeClass : 
	            iOwnerObject = new Class_DAO().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeConfig : 
	            iOwnerObject = new InstrOfferingConfigDAO().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeCourse : 
	            iOwnerObject = new CourseOfferingDAO().get(getOwnerId());
	            return iOwnerObject;
	        case sOwnerTypeOffering : 
	            iOwnerObject = new InstructionalOfferingDAO().get(getOwnerId());
	            return iOwnerObject;
	        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
	    }
	}
	
    public void setOwner(Class_ clazz) {
        setOwnerId(clazz.getUniqueId());
        setOwnerType(sOwnerTypeClass);
        setCourse(clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering());
    }

    public void setOwner(InstrOfferingConfig config) {
        setOwnerId(config.getUniqueId());
        setOwnerType(sOwnerTypeConfig);
        setCourse(config.getControllingCourseOffering());
    }

    public void setOwner(CourseOffering course) {
        setOwnerId(course.getUniqueId());
        setOwnerType(sOwnerTypeCourse);
        setCourse(course);
    }

    public void setOwner(InstructionalOffering offering) {
        setOwnerId(offering.getUniqueId());
        setOwnerType(sOwnerTypeOffering);
        setCourse(offering.getControllingCourseOffering());
    }
    
    public CourseOffering computeCourse() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering();
            case sOwnerTypeCourse : 
                return (CourseOffering)owner;
            case sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int compareTo(ExamOwner owner) {
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
            case sOwnerTypeClass : return new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY).compare(getOwnerObject(), owner.getOwnerObject());
            case sOwnerTypeConfig : return new InstrOfferingConfigComparator(null).compare(getOwnerObject(), owner.getOwnerObject());
        }
           
        return getOwnerId().compareTo(owner.getOwnerId());
    }
    
    public List getStudents() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public List getStudentIds() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeConfig : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeCourse : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        case sOwnerTypeOffering : 
            return new ExamOwnerDAO().getSession().createQuery(
                    "select distinct e.student.uniqueId from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .list();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    protected void computeStudentExams(Hashtable<Long, Set<Exam>> studentExams) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType=:examType")
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setInteger("examType", getExam().getExamType())
                .setLong("examOwnerId", getOwnerId())
                .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeConfig :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeCourse :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and  " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        case sOwnerTypeOffering :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, o.exam from ExamOwner o, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and e.student=f.student and " +
                    "o.ownerType=:ownerType and o.ownerId=f.courseOffering.instructionalOffering.uniqueId and o.exam.examType=:examType")
                    .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                    .setInteger("examType", getExam().getExamType())
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Exam exam = (Exam)o[1];
                Set<Exam> exams  = studentExams.get(studentId);
                if (exams==null) { exams = new HashSet(); studentExams.put(studentId, exams); }
                exams.add(exam);
            }
            break;
        }
    }
    
    protected void computeStudentAssignments(Hashtable<Assignment, Set<Long>> studentAssignments) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                "where c.uniqueId = :examOwnerId and " +
                "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true")
                .setLong("examOwnerId", getOwnerId())
                .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeConfig :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeCourse :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeOffering :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, a from Assignment a, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and " +
                    "e.student=f.student and f.clazz = a.clazz and a.solution.commited = true")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Assignment assignment = (Assignment)o[1];
                Set<Long> students  = studentAssignments.get(assignment);
                if (students==null) { students = new HashSet(); studentAssignments.put(assignment, students); }
                students.add(studentId);
            }
            break;
        }
    }
    
    protected void computeOverlappingStudentMeetings(Hashtable<Meeting, Set<Long>> studentMeetings, Long periodId) {
        switch (getOwnerType()) {
        case sOwnerTypeClass :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                "select e.student.uniqueId, m from Meeting m inner join m.event.relatedCourses r, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c, ExamPeriod p " +
                "where c.uniqueId = :examOwnerId and " +
                "m.eventType.reference=:eventType and r.ownerType=:classType and "+
                "e.student=f.student and f.clazz.uniqueId = r.ownerId and "+
                "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                "p.session=co.subjectArea.session and p.session.examBeginDate+p.dateOffset = m.meetingDate")
                .setLong("examOwnerId", getOwnerId())
                .setString("eventType", EventType.sEventTypeClass)
                .setInteger("classType", ExamOwner.sOwnerTypeClass)
                .setInteger("travelTime", Constants.EXAM_TRAVEL_TIME_SLOTS)
                .setLong("periodId", periodId)
                .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeConfig :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, m from Meeting m inner join m.event.relatedCourses r, StudentClassEnrollment f, StudentClassEnrollment e inner join e.clazz c, ExamPeriod p " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId and " +
                    "m.eventType.reference=:eventType and r.ownerType=:classType and "+
                    "e.student=f.student and f.clazz.uniqueId = r.ownerId and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    "p.session=co.subjectArea.session and p.session.examBeginDate+p.dateOffset = m.meetingDate")
                    .setLong("examOwnerId", getOwnerId())
                    .setString("eventType", EventType.sEventTypeClass)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setInteger("travelTime", Constants.EXAM_TRAVEL_TIME_SLOTS)
                    .setLong("periodId", periodId)
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeCourse :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, m from Meeting m inner join m.event.relatedCourses r, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co, ExamPeriod p " +
                    "where co.uniqueId = :examOwnerId and " +
                    "m.eventType.reference=:eventType and r.ownerType=:classType and "+
                    "e.student=f.student and f.clazz.uniqueId = r.ownerId and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    "p.session=co.subjectArea.session and p.session.examBeginDate+p.dateOffset = m.meetingDate")
                    .setLong("examOwnerId", getOwnerId())
                    .setString("eventType", EventType.sEventTypeClass)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setInteger("travelTime", Constants.EXAM_TRAVEL_TIME_SLOTS)
                    .setLong("periodId", periodId)
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        case sOwnerTypeOffering :
            for (Iterator i=new ExamOwnerDAO().getSession().createQuery(
                    "select e.student.uniqueId, m from Meeting m inner join m.event.relatedCourses r, StudentClassEnrollment f, StudentClassEnrollment e inner join e.courseOffering co, ExamPeriod p " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId and " +
                    "m.eventType.reference=:eventType and r.ownerType=:classType and "+
                    "e.student=f.student and f.clazz.uniqueId = r.ownerId and "+
                    "p.uniqueId=:periodId and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    "p.session=co.subjectArea.session and p.session.examBeginDate+p.dateOffset = m.meetingDate")
                    .setLong("examOwnerId", getOwnerId())
                    .setString("eventType", EventType.sEventTypeClass)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setInteger("travelTime", Constants.EXAM_TRAVEL_TIME_SLOTS)
                    .setLong("periodId", periodId)
                    .setCacheable(true).iterate(); i.hasNext();) {
                Object[] o = (Object[])i.next();
                Long studentId = (Long)o[0];
                Meeting meeting = (Meeting)o[1];
                Set<Long> students  = studentMeetings.get(meeting);
                if (students==null) { students = new HashSet(); studentMeetings.put(meeting, students); }
                students.add(studentId);
            }
            break;
        }
    }
    
    public int countStudents() {
        switch (getOwnerType()) {
        case sOwnerTypeClass : 
            return ((Number)new ExamOwnerDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case sOwnerTypeConfig : 
            return ((Number)new ExamOwnerDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.clazz c  " +
                    "where c.schedulingSubpart.instrOfferingConfig.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case sOwnerTypeCourse : 
            return ((Number)new ExamOwnerDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        case sOwnerTypeOffering : 
            return ((Number)new ExamOwnerDAO().getSession().createQuery(
                    "select count(distinct e.student) from " +
                    "StudentClassEnrollment e inner join e.courseOffering co  " +
                    "where co.instructionalOffering.uniqueId = :examOwnerId")
                    .setLong("examOwnerId", getOwnerId())
                    .setCacheable(true)
                    .uniqueResult()).intValue();
        default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int getLimit() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getClassLimit();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getLimit();
            case sOwnerTypeCourse : 
                CourseOffering course = ((CourseOffering)owner);
                if (course.getInstructionalOffering().getCourseOfferings().size()>1) {
                    for (Iterator k=course.getInstructionalOffering().getCourseReservations().iterator();k.hasNext();) {
                        CourseOfferingReservation reservation = (CourseOfferingReservation)k.next();
                        if (reservation.getCourseOffering().equals(course)) return reservation.getReserved();
                    }
                    return 0;
                } else return (course.getInstructionalOffering().getLimit()==null?0:course.getInstructionalOffering().getLimit());
            case sOwnerTypeOffering : 
                return (((InstructionalOffering)owner).getLimit()==null?0:((InstructionalOffering)owner).getLimit());
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public int getSize() {
        if (getExam().getExamType()==Exam.sExamTypeFinal)
            return countStudents();
        else
            return Math.max(countStudents(), getLimit());
    }
    
    public String getLabel() {
        return genName(ApplicationProperties.getProperty("tmtbl.exam.name."+ExamOwner.sOwnerTypes[getOwnerType()]));
        /*
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                
                return ((Class_)owner).getClassLabel();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).toString();
            case sOwnerTypeCourse : 
                return ((CourseOffering)owner).getCourseName();
            case sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getCourseName();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
        */
    }

    public String getSubject() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering().getSubjectAreaAbbv();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering().getSubjectAreaAbbv();
            case sOwnerTypeCourse : 
                return ((CourseOffering)owner).getSubjectAreaAbbv();
            case sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering().getSubjectAreaAbbv();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String getCourseNbr() {
        Object owner = getOwnerObject();
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)owner).getSchedulingSubpart().getControllingCourseOffering().getCourseNbr();
            case sOwnerTypeConfig : 
                return ((InstrOfferingConfig)owner).getControllingCourseOffering().getCourseNbr();
            case sOwnerTypeCourse : 
                return ((CourseOffering)owner).getCourseNbr();
            case sOwnerTypeOffering : 
                return ((InstructionalOffering)owner).getControllingCourseOffering().getCourseNbr();
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String getItype() {
        switch (getOwnerType()) {
            case sOwnerTypeClass : 
                return ((Class_)getOwnerObject()).getSchedulingSubpart().getItypeDesc();
            case sOwnerTypeConfig : 
                return "["+((InstrOfferingConfig)getOwnerObject()).getName()+"]";
            case sOwnerTypeCourse : 
            case sOwnerTypeOffering : 
                return "";
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String getSection() {
        switch (getOwnerType()) {
            case sOwnerTypeClass :
                Class_ clazz = (Class_)getOwnerObject();
                return ("true".equals(ApplicationProperties.getProperty("tmtbl.exam.report.suffix","false")) && clazz.getClassSuffix()!=null?clazz.getClassSuffix():clazz.getSectionNumberString());
            case sOwnerTypeConfig : 
                if ("false".equals(ApplicationProperties.getProperty("tmtbl.exam.report.itype","true")))
                    return "["+((InstrOfferingConfig)getOwnerObject()).getName()+"]";
            case sOwnerTypeCourse : 
            case sOwnerTypeOffering : 
                return "";
            default : throw new RuntimeException("Unknown owner type "+getOwnerType());
        }
    }
    
    public String genName(String pattern) {
        String name = pattern;
        int idx = -1;
        while (name.indexOf('%',idx+1)>=0) {
            idx = name.indexOf('%',idx);
            char code = name.charAt(idx+1);
            String name4code = genName(code);
            name = name.substring(0,idx)+(name4code==null?"":name4code)+name.substring(idx+2);
        }
        return name;
    }
    
    /**
     * Basic codes
     * s ... subject area
     * c ... course number
     * i ... itype abbv
     * n ... section number
     * x ... configuration name
     * Additional codes
     * d ... department abbv
     * a ... class suffix (div-sec number)
     * y ... itype suffix (a, b etc.)
     * e ... class external id
     * f ... course external id
     * o ... offering external id
     * t ... exam type suffix (tmtbl.exam.name.type.final and tmtbl.exam.name.type.evening)
     * I ... itype code
     * p ... itype parent abbv
     * P ... itype parent code
     * _ ... space
     */
    protected String genName(char code) {
        switch (code) {
        case '_' : return " ";
        case 's' : return getCourse().getSubjectArea().getSubjectAreaAbbreviation();
        case 'c' : return getCourse().getCourseNbr();
        case 'i' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getItypeDesc().trim();
            default : return "";
            }
        case 'n' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSectionNumberString();
            default : return "";
            }
        case 'x' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getInstrOfferingConfig().getName();
            case sOwnerTypeConfig : return ((InstrOfferingConfig)getOwnerObject()).getName();
            default : return "";
            }
        case 'D' :
            return getCourse().getDepartment().getDeptCode();
        case 'd' :
            Department d = getCourse().getDepartment();
            return (d.getAbbreviation()==null || d.getAbbreviation().length()==0?d.getDeptCode():d.getAbbreviation());
        case 'a' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getClassSuffix();
            default : return "";
            }
        case 'y' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getSchedulingSubpartSuffix();
            default : return "";
            }
        case 'e' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getExternalUniqueId();
            default : return "";
            }
        case 'f' :
            return getCourse().getExternalUniqueId();
        case 'o' :
            return getCourse().getInstructionalOffering().getExternalUniqueId();
        case 't' :
            return ApplicationProperties.getProperty("tmtbl.exam.name.type."+Exam.sExamTypes[getExam().getExamType()]);
        case 'I' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : return ((Class_)getOwnerObject()).getSchedulingSubpart().getItype().getItype().toString();
            default : return "";
            }
        case 'p' :
            switch (getOwnerType()) {
            case sOwnerTypeClass :
                ItypeDesc itype = ((Class_)getOwnerObject()).getSchedulingSubpart().getItype();
                while (itype.getParent()!=null) itype = itype.getParent();
                return itype.getAbbv();
            default : return "";
            }
        case 'P' :
            switch (getOwnerType()) {
            case sOwnerTypeClass : 
                ItypeDesc itype = ((Class_)getOwnerObject()).getSchedulingSubpart().getItype();
                while (itype.getParent()!=null) itype = itype.getParent();
                return itype.getItype().toString();
            default : return "";
            }
        }
        return "";
    }
}