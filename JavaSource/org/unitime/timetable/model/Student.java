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
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.base.BaseStudent;
import org.unitime.timetable.model.dao.StudentDAO;




public class Student extends BaseStudent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Student () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Student (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public Student (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String firstName,
		java.lang.String lastName,
		java.lang.Integer freeTimeCategory,
		java.lang.Integer schedulePreference) {

		super (
			uniqueId,
			session,
			firstName,
			lastName,
			freeTimeCategory,
			schedulePreference);
	}

/*[CONSTRUCTOR MARKER END]*/

	public void addToPosMajors (org.unitime.timetable.model.PosMajor major) {
		if (null == getPosMajors()) setPosMajors(new java.util.HashSet());
		getPosMajors().add(major);
	}
	
	public void addToPosMinors (org.unitime.timetable.model.PosMinor minor) {
		if (null == getPosMinors()) setPosMinors(new java.util.HashSet());
		getPosMinors().add(minor);
	}
    
    public static List findAll(Long sessionId) {
        return new StudentDAO().
            getSession().
            createQuery("select s from Student s where s.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
            list();
    }
    
    public static Student findByExternalId(Long sessionId, String externalId) {
        return (Student)new StudentDAO().
            getSession().
            createQuery("select s from Student s where "+
                    "s.session.uniqueId=:sessionId and "+
                    "s.externalUniqueId=:externalId").
            setLong("sessionId", sessionId.longValue()).
            setString("externalId",externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public Set getExams(Integer examType) {
        HashSet exams = new HashSet();
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.uniqueId and o.exam.examType=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.uniqueId and o.exam.examType=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.instructionalOffering.uniqueId and o.exam.examType=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        return exams;
    }
}