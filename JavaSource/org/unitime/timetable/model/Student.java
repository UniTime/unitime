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
package org.unitime.timetable.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.unitime.timetable.model.base.BaseStudent;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.util.NameFormat;
import org.unitime.timetable.util.NameInterface;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class Student extends BaseStudent implements Comparable<Student>, NameInterface, Qualifiable {
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

/*[CONSTRUCTOR MARKER END]*/

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
    
    public static Student findByExternalIdBringBackEnrollments(org.hibernate.Session hibSession, Long sessionId, String externalId) {
        return (Student)hibSession.
            createQuery("select s from Student s " +
            		"left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.areaClasfMajors " +
                    "left join fetch s.areaClasfMinors " +
            		"where "+
                    "s.session.uniqueId=:sessionId and "+
                    "s.externalUniqueId=:externalId").
            setLong("sessionId", sessionId.longValue()).
            setString("externalId",externalId).
            setCacheable(true).
            uniqueResult();
    }
    
    public void removeAllEnrollments(org.hibernate.Session hibSession){
		HashSet<StudentClassEnrollment> enrollments = new HashSet<StudentClassEnrollment>();
    	if (getClassEnrollments() != null){
    		enrollments.addAll(getClassEnrollments());
    	}
       	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments) {
    			getClassEnrollments().remove(enrollment);
    			hibSession.delete(enrollment);
    		}
    	}

    }

    @Deprecated
    public Set<Exam> getExams(Integer examType) {
        HashSet exams = new HashSet();
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.instructionalOffering.uniqueId and o.exam.examType.type=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                .setInteger("examType", examType)
                .setCacheable(true)
                .list());
        return exams;
    }
    
    public Set<Exam> getExams(ExamType examType) {
        HashSet exams = new HashSet();
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeClass)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.clazz.schedulingSubpart.instrOfferingConfig.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeConfig)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeCourse)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        exams.addAll(new StudentDAO().getSession().createQuery(
                "select distinct o.exam from ExamOwner o, StudentClassEnrollment e "+
                "where e.student.uniqueId=:studentId and o.ownerType=:ownerType and o.ownerId=e.courseOffering.instructionalOffering.uniqueId and o.exam.examType.uniqueId=:examType")
                .setLong("studentId", getUniqueId())
                .setInteger("ownerType", ExamOwner.sOwnerTypeOffering)
                .setLong("examType", examType.getUniqueId())
                .setCacheable(true)
                .list());
        return exams;
    }
    
    public String getName(String instructorNameFormat) {
    	return NameFormat.fromReference(instructorNameFormat).format(this);
    }
    
    public int compareTo(Student student) {
        int cmp = NameFormat.LAST_FIRST.format(this).compareTo(NameFormat.LAST_FIRST.format(student));
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(student.getUniqueId() == null ? -1 : student.getUniqueId());
    }
    
    public static Hashtable<Long,Set<Long>> findConflictingStudents(Long classId, int startSlot, int length, List<Date> dates) {
    	Hashtable<Long,Set<Long>> table = new Hashtable();
    	if (dates.isEmpty()) return table;
    	String datesStr = "";
    	for (int i=0; i<dates.size(); i++) {
    		if (i>0) datesStr += ", ";
    		datesStr += ":date"+i;
    	}
    	Query q = LocationDAO.getInstance().getSession()
    	    .createQuery("select distinct e.clazz.uniqueId, e.student.uniqueId "+
    	        	"from StudentClassEnrollment e, ClassEvent c inner join c.meetings m, StudentClassEnrollment x "+
    	        	"where x.clazz.uniqueId=:classId and x.student=e.student and " + // only look among students of the given class 
    	        	"e.clazz=c.clazz and " + // link ClassEvent c with StudentClassEnrollment e
            		"m.stopPeriod>:startSlot and :endSlot>m.startPeriod and " + // meeting time within given time period
            		"m.meetingDate in ("+datesStr+") and m.approvalStatus = 1")
            .setLong("classId",classId)
            .setInteger("startSlot", startSlot)
            .setInteger("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setDate("date"+i, dates.get(i));
    	}
        for (Iterator i = q.setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
            Set<Long> set = table.get((Long)o[0]);
            if (set==null) {
            	set = new HashSet<Long>();
            	table.put((Long)o[0], set);
            }
            set.add((Long)o[1]);
        }
        return table;
    }
    
    public boolean hasSectioningStatusOption(StudentSectioningStatus.Option option) {
    	if (getSectioningStatus() != null)
    		return getSectioningStatus().hasOption(option);
    	if (getSession().getDefaultSectioningStatus() != null)
    		return getSession().getDefaultSectioningStatus().hasOption(option);
    	return false;
    }

	@Override
	public String getAcademicTitle() { return null; }
	
	@Override
	public Serializable getQualifierId() {
		return getUniqueId();
	}

	@Override
	public String getQualifierType() {
		return getClass().getSimpleName();
	}

	@Override
	public String getQualifierReference() {
		return getExternalUniqueId();
	}

	@Override
	public String getQualifierLabel() {
		return NameFormat.LAST_FIRST_MIDDLE.format(this);
	}
}
