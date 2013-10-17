/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.hibernate.Query;
import org.unitime.timetable.model.base.BaseStudent;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.util.Constants;




/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class Student extends BaseStudent implements Comparable<Student> {
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
    
    public static Student findByExternalIdBringBackEnrollments(org.hibernate.Session hibSession, Long sessionId, String externalId) {
        return (Student)hibSession.
            createQuery("select s from Student s " +
            		"left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch s.academicAreaClassifications " +
                    "left join fetch s.posMajors " +
                    "left join fetch s.posMinors " +
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
    
    public String getName(String instructorNameFormat) {
        if (DepartmentalInstructor.sNameFormatLastFist.equals(instructorNameFormat))
            return Constants.toInitialCase((getLastName()==null?"":getLastName().trim())+", "+(getFirstName()==null?"":getFirstName().trim()));
        else if (DepartmentalInstructor.sNameFormatFirstLast.equals(instructorNameFormat))
            return Constants.toInitialCase((getFirstName()==null?"":getFirstName().trim())+" "+(getLastName()==null?"":getLastName().trim()));
        else if (DepartmentalInstructor.sNameFormatInitialLast.equals(instructorNameFormat))
            return (getFirstName()==null?"":getFirstName().trim().substring(0, 1).toUpperCase())+
                (getMiddleName()==null || getMiddleName().isEmpty()?"":" "+getMiddleName().trim().substring(0, 1).toUpperCase())+" "+
                (Constants.toInitialCase(getLastName()==null?"":getLastName().trim()));
        else if (DepartmentalInstructor.sNameFormatLastInitial.equals(instructorNameFormat))
            return Constants.toInitialCase(getLastName()==null?"":getLastName().trim())+", "+
                (getFirstName()==null?"":getFirstName().trim().substring(0, 1).toUpperCase())+
                (getMiddleName()==null || getMiddleName().isEmpty()?"":" "+getMiddleName().trim().substring(0, 1).toUpperCase());
        else if (DepartmentalInstructor.sNameFormatLastFirstMiddle.equals(instructorNameFormat))
            return Constants.toInitialCase((getLastName()==null?"":getLastName().trim())+", "+(getFirstName()==null?"":getFirstName().trim()) + (getMiddleName()==null?"":" "+getMiddleName().trim()));
        else if (DepartmentalInstructor.sNameFormatShort.equals(instructorNameFormat)) {
            StringBuffer sb = new StringBuffer();
            if (getFirstName()!=null && getFirstName().length()>0) {
                sb.append(getFirstName().substring(0,1).toUpperCase());
                sb.append(". ");
            }
            if (getLastName()!=null && getLastName().length()>0) {
                sb.append(getLastName().substring(0,1).toUpperCase());
                sb.append(getLastName().substring(1,Math.min(10,getLastName().length())).toLowerCase().trim());
            }
            return sb.toString();
        } else 
            return Constants.toInitialCase((getFirstName()==null?"":getFirstName().trim())+" "+
                (getMiddleName()==null?"":getMiddleName().trim())+" "+
                (getLastName()==null?"":getLastName().trim()));
    }
    
    public int compareTo(Student student) {
        int cmp = getName(DepartmentalInstructor.sNameFormatLastFist).compareTo(student.getName(DepartmentalInstructor.sNameFormatLastFist));
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(student.getUniqueId() == null ? -1 : student.getUniqueId());
    }
    
    public static Hashtable<Long,Set<Long>> findConflictingStudents(Long classId, int startSlot, int length, Vector<Date> dates) {
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
            		"m.meetingDate in ("+datesStr+")")
            .setLong("classId",classId)
            .setInteger("startSlot", startSlot)
            .setInteger("endSlot", startSlot + length);
    	for (int i=0; i<dates.size(); i++) {
    		q.setDate("date"+i, dates.elementAt(i));
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

}
