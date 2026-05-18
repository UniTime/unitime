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
package org.unitime.timetable.server.exams;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.Parameters;

public class CacheAssignedExams {
	Long sessionId, examTypeId;
    Hashtable<Long, Exam> exams = new Hashtable<Long, Exam>();
    Hashtable<Long,Set<Long>> owner2students = new Hashtable<Long,Set<Long>>();
    Hashtable<Long,Set<Exam>> student2exams = new Hashtable<Long,Set<Exam>>();
    Hashtable<Long, Set<Meeting>> period2meetings = new Hashtable<Long, Set<Meeting>>();
    Hashtable<Long,Hashtable<Long,Set<Long>>> owner2course2students = new Hashtable<Long,Hashtable<Long,Set<Long>>>();
    Parameters p;
	
    public CacheAssignedExams(Long sessionId, Long examTypeId) {
		this.sessionId = sessionId;
		this.examTypeId = examTypeId;
        for (Iterator i=ExamDAO.getInstance().getSession().createQuery(
                "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId", Exam.class
                ).setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            exams.put(exam.getUniqueId(), exam);
        }
        ExamDAO.getInstance().getSession().createQuery(
                "select c from Class_ c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:classType and c.uniqueId=o.ownerId", Class_.class)
                .setParameter("sessionId", sessionId)
                .setParameter("examTypeId", examTypeId)
                .setParameter("classType", ExamOwner.sOwnerTypeClass).setCacheable(true).list();
        ExamDAO.getInstance().getSession().createQuery(
                "select c from InstrOfferingConfig c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:configType and c.uniqueId=o.ownerId", InstrOfferingConfig.class)
                .setParameter("sessionId", sessionId)
                .setParameter("examTypeId", examTypeId)
                .setParameter("configType", ExamOwner.sOwnerTypeConfig).setCacheable(true).list();
        ExamDAO.getInstance().getSession().createQuery(
                "select c from CourseOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:courseType and c.uniqueId=o.ownerId", CourseOffering.class)
                .setParameter("sessionId", sessionId)
                .setParameter("examTypeId", examTypeId)
                .setParameter("courseType", ExamOwner.sOwnerTypeCourse).setCacheable(true).list();
        ExamDAO.getInstance().getSession().createQuery(
                "select c from InstructionalOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:offeringType and c.uniqueId=o.ownerId", InstructionalOffering.class)
                .setParameter("sessionId", sessionId)
                .setParameter("examTypeId", examTypeId)
                .setParameter("offeringType", ExamOwner.sOwnerTypeOffering).setCacheable(true).list();
        
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                "Exam x inner join x.owners o, "+
                "StudentClassEnrollment e inner join e.clazz c "+
                "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
                "o.ownerId=c.uniqueId", Object[].class)
                .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
            Long examId = (Long)o[0];
            Long ownerId = (Long)o[1];
            Long studentId = (Long)o[2];
            Set<Long> studentsOfOwner = owner2students.get(ownerId);
            if (studentsOfOwner==null) {
                studentsOfOwner = new HashSet<Long>();
                owner2students.put(ownerId, studentsOfOwner);
            }
            studentsOfOwner.add(studentId);
            Set<Exam> examsOfStudent = student2exams.get(studentId);
            if (examsOfStudent==null) { 
                examsOfStudent = new HashSet<Exam>();
                student2exams.put(studentId, examsOfStudent);
            }
            examsOfStudent.add(exams.get(examId));
            Long courseId = (Long)o[3];
            Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
            if (course2students == null) {
            	course2students = new Hashtable<Long, Set<Long>>();
            	owner2course2students.put(ownerId, course2students);
            }
            Set<Long> studentsOfCourse = course2students.get(courseId);
            if (studentsOfCourse == null) {
            	studentsOfCourse = new HashSet<Long>();
            	course2students.put(courseId, studentsOfCourse);
            }
            studentsOfCourse.add(studentId);
        }
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                        "Exam x inner join x.owners o, "+
                        "StudentClassEnrollment e inner join e.clazz c " +
                        "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
                        "o.ownerId=ioc.uniqueId", Object[].class)
                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
            Long examId = (Long)o[0];
            Long ownerId = (Long)o[1];
            Long studentId = (Long)o[2];
            Set<Long> studentsOfOwner = owner2students.get(ownerId);
            if (studentsOfOwner==null) {
                studentsOfOwner = new HashSet<Long>();
                owner2students.put(ownerId, studentsOfOwner);
            }
            studentsOfOwner.add(studentId);
            Set<Exam> examsOfStudent = student2exams.get(studentId);
            if (examsOfStudent==null) { 
                examsOfStudent = new HashSet<Exam>();
                student2exams.put(studentId, examsOfStudent);
            }
            examsOfStudent.add(exams.get(examId));
            Long courseId = (Long)o[3];
            Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
            if (course2students == null) {
            	course2students = new Hashtable<Long, Set<Long>>();
            	owner2course2students.put(ownerId, course2students);
            }
            Set<Long> studentsOfCourse = course2students.get(courseId);
            if (studentsOfCourse == null) {
            	studentsOfCourse = new HashSet<Long>();
            	course2students.put(courseId, studentsOfCourse);
            }
            studentsOfCourse.add(studentId);
        }
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                        "Exam x inner join x.owners o, "+
                        "StudentClassEnrollment e inner join e.courseOffering co " +
                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
                        "o.ownerId=co.uniqueId", Object[].class)
                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
            Long examId = (Long)o[0];
            Long ownerId = (Long)o[1];
            Long studentId = (Long)o[2];
            Set<Long> studentsOfOwner = owner2students.get(ownerId);
            if (studentsOfOwner==null) {
                studentsOfOwner = new HashSet<Long>();
                owner2students.put(ownerId, studentsOfOwner);
            }
            studentsOfOwner.add(studentId);
            Set<Exam> examsOfStudent = student2exams.get(studentId);
            if (examsOfStudent==null) { 
                examsOfStudent = new HashSet<Exam>();
                student2exams.put(studentId, examsOfStudent);
            }
            examsOfStudent.add(exams.get(examId));
            Long courseId = (Long)o[3];
            Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
            if (course2students == null) {
            	course2students = new Hashtable<Long, Set<Long>>();
            	owner2course2students.put(ownerId, course2students);
            }
            Set<Long> studentsOfCourse = course2students.get(courseId);
            if (studentsOfCourse == null) {
            	studentsOfCourse = new HashSet<Long>();
            	course2students.put(courseId, studentsOfCourse);
            }
            studentsOfCourse.add(studentId);
        }
        
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                        "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                        "Exam x inner join x.owners o, "+
                        "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                        "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                        "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
                        "o.ownerId=io.uniqueId", Object[].class)
                		.setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId).setCacheable(true).list()) {
            Long examId = (Long)o[0];
            Long ownerId = (Long)o[1];
            Long studentId = (Long)o[2];
            Set<Long> studentsOfOwner = owner2students.get(ownerId);
            if (studentsOfOwner==null) {
                studentsOfOwner = new HashSet<Long>();
                owner2students.put(ownerId, studentsOfOwner);
            }
            studentsOfOwner.add(studentId);
            Set<Exam> examsOfStudent = student2exams.get(studentId);
            if (examsOfStudent==null) { 
                examsOfStudent = new HashSet<Exam>();
                student2exams.put(studentId, examsOfStudent);
            }
            examsOfStudent.add(exams.get(examId));
            Long courseId = (Long)o[3];
            Hashtable<Long, Set<Long>> course2students = owner2course2students.get(ownerId);
            if (course2students == null) {
            	course2students = new Hashtable<Long, Set<Long>>();
            	owner2course2students.put(ownerId, course2students);
            }
            Set<Long> studentsOfCourse = course2students.get(courseId);
            if (studentsOfCourse == null) {
            	studentsOfCourse = new HashSet<Long>();
            	course2students.put(courseId, studentsOfCourse);
            }
            studentsOfCourse.add(studentId);
        }
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                    "select p.uniqueId, m from ClassEvent ce inner join ce.meetings m, ExamPeriod p " +
                    "where p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeClass.intValue())
                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
                    .setCacheable(true).list()) {
            Long periodId = (Long)o[0];
            Meeting meeting = (Meeting)o[1];
            Set<Meeting> meetings  = period2meetings.get(periodId);
            if (meetings==null) {
                meetings = new HashSet(); period2meetings.put(periodId, meetings);
            }
            meetings.add(meeting);
        }
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                    "select p.uniqueId, m from CourseEvent ce inner join ce.meetings m, ExamPeriod p " +
                    "where ce.reqAttendance=true and m.approvalStatus = 1 and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeCourse.intValue())
                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
                    .setCacheable(true).list()) {
            Long periodId = (Long)o[0];
            Meeting meeting = (Meeting)o[1];
            Set<Meeting> meetings  = period2meetings.get(periodId);
            if (meetings==null) {
                meetings = new HashSet(); period2meetings.put(periodId, meetings);
            }
            meetings.add(meeting);
        }
        for (Object[] o: ExamDAO.getInstance().getSession().createQuery(
                    "select p.uniqueId, m from ExamEvent ce inner join ce.meetings m, ExamPeriod p " +
                    "where ce.exam.examType.uniqueId != :examTypeId and m.approvalStatus = 1 and p.startSlot - :travelTime < m.stopPeriod and m.startPeriod < p.startSlot + p.length + :travelTime and "+
                    HibernateUtil.addDate("p.session.examBeginDate","p.dateOffset")+" = m.meetingDate and p.session.uniqueId=:sessionId and p.examType.uniqueId=:examTypeId", Object[].class)
                    .setParameter("travelTime", ApplicationProperty.ExaminationTravelTimeCourse.intValue())
                    .setParameter("sessionId", sessionId).setParameter("examTypeId", examTypeId)
                    .setCacheable(true).list()) {
            Long periodId = (Long)o[0];
            Meeting meeting = (Meeting)o[1];
            Set<Meeting> meetings  = period2meetings.get(periodId);
            if (meetings==null) {
                meetings = new HashSet(); period2meetings.put(periodId, meetings);
            }
            meetings.add(meeting);
        }
        p = new Parameters(sessionId, examTypeId);			
	}
	

    public TreeSet<ExamAssignmentInfo> getAssignedExams() {
		TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                "select x from Exam x where " +
                "x.examType.uniqueId=:examTypeId and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod is not null", Exam.class).
                setParameter("sessionId", sessionId).
                setParameter("examTypeId", examTypeId).
                setCacheable(true).list()) {
        	ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        }
        return ret;
	}
	
	public TreeSet<ExamAssignmentInfo> getAssignedExams(Collection<Long> subjectAreaIds) {
		if (subjectAreaIds == null || subjectAreaIds.isEmpty() || subjectAreaIds.contains(-1l))
			return getAssignedExams();
		TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId in :subjectAreaIds and "+
                "x.examType.uniqueId=:examTypeId and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod is not null", Exam.class).
                setParameter("sessionId", sessionId).
                setParameter("examTypeId", examTypeId).
                setParameterList("subjectAreaIds", subjectAreaIds, Long.class).
                setCacheable(true).list()) {
            ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        }
        return ret;
    }
	
	public TreeSet<ExamAssignmentInfo> getAssignedExamsOfSubjectArea(Long subjectAreaId) {
		TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                "select distinct x from Exam x inner join x.owners o where " +
                "o.course.subjectArea.uniqueId=:subjectAreaId and "+
                "x.examType.uniqueId=:examTypeId and "+
                "x.session.uniqueId=:sessionId and x.assignedPeriod is not null", Exam.class).
                setParameter("sessionId", sessionId).
                setParameter("examTypeId", examTypeId).
                setParameter("subjectAreaId", subjectAreaId).
                setCacheable(true).list()) {
            ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        }
        return ret;
    }
	
	public TreeSet<ExamAssignmentInfo> getAssignedExamsOfLocation(Long locationId) throws Exception {
		TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                "select distinct x from Exam x inner join x.assignedRooms r where " +
                "r.uniqueId=:locationId and x.assignedPeriod is not null and "+
                "x.examType.uniqueId=:examTypeId", Exam.class).
                setParameter("locationId", locationId).
                setParameter("examTypeId", examTypeId).
                setCacheable(true).list()) {
            ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        } 
        return ret;
    }
	
	public TreeSet<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) throws Exception {
		TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Exam exam: ExamDAO.getInstance().getSession().createQuery(
                "select distinct x from Exam x inner join x.instructors i where " +
                "i.uniqueId=:instructorId and x.assignedPeriod is not null and "+
                "x.examType.uniqueId=:examTypeId", Exam.class).
                setParameter("instructorId", instructorId).
                setParameter("examTypeId", examTypeId).
                setCacheable(true).list()) {
        	ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        } 
        return ret;
    }
}
