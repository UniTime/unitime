/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
package org.unitime.timetable.test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.reports.exam.PdfLegacyExamReport;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;

public class UpdateExamConflicts {
    private static Log sLog = LogFactory.getLog(UpdateExamConflicts.class);
    
    private static int sCreate = 0;
    private static int sUpdate = 1;
    private static int sDelete = 2;
    private static int sStudents = 0;
    private static int sInstructors = 1;
    private int[][][] iCnt;
    private int[][] iTotal;
    
    public void updateConflicts(ExamAssignmentInfo assignment, org.hibernate.Session hibSession) throws Exception {
        Transaction tx = null;
        try {
            if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                tx = hibSession.beginTransaction();
            
            Exam exam = assignment.getExam(hibSession);
            
            HashSet<Exam> otherExams = new HashSet();
            
            HashSet<ExamConflict> conflicts = new HashSet(exam.getConflicts());
            
            for (DirectConflict dc : assignment.getDirectConflicts()) {
                if (dc.getOtherExam()==null) continue;
                ExamConflict conf = null;
                for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=ExamConflict.sConflictTypeDirect) continue;
                    if (c.getNrStudents()==0) continue;
                    Exam other = null;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam x = (Exam)i.next();
                        if (x.getUniqueId().equals(dc.getOtherExam().getExamId())) { other = x; break; }
                    }
                    if (other==null) continue;
                    conf = c; break;
                }
                HashSet<Student> students = getStudents(hibSession, dc.getStudents());
                if (exam.getUniqueId().compareTo(dc.getOtherExam().getExamId())<0) 
                    iTotal[sStudents][ExamConflict.sConflictTypeDirect]+=students.size();
                if (conf==null) {
                    sLog.debug("    new direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+students.size()+" students)");
                    conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeDirect);
                    conf.setStudents(students);
                    conf.setNrStudents(students.size());
                    exam.getConflicts().add(conf);
                    Exam other = dc.getOtherExam().getExam(hibSession);
                    other.getConflicts().add(conf);
                    otherExams.add(other);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    conf.getExams().add(other);
                    hibSession.save(conf);
                    iCnt[sStudents][conf.getConflictType()][sCreate]+=conf.getNrStudents();
                } else {
                    conflicts.remove(conf);
                    boolean change = (students.size()!=conf.getStudents().size() && !students.containsAll(conf.getStudents()));
                    if (change) {
                        sLog.debug("    update direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
                        conf.setStudents(students);
                        conf.setNrStudents(students.size());
                        hibSession.update(conf);
                        iCnt[sStudents][conf.getConflictType()][sUpdate]+=conf.getNrStudents();
                    } 
                }
            }
            for (DirectConflict dc : assignment.getInstructorDirectConflicts()) {
                if (dc.getOtherExam()==null) continue;
                ExamConflict conf = null;
                for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=ExamConflict.sConflictTypeDirect) continue;
                    if (c.getNrInstructors()==0) continue;
                    Exam other = null;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam x = (Exam)i.next();
                        if (x.getUniqueId().equals(dc.getOtherExam().getExamId())) { other = x; break; }
                    }
                    if (other==null) continue;
                    conf = c; break;
                }
                HashSet<DepartmentalInstructor> instructors = getInstructors(hibSession, dc.getStudents());
                if (exam.getUniqueId().compareTo(dc.getOtherExam().getExamId())<0) 
                    iTotal[sInstructors][ExamConflict.sConflictTypeDirect]+=instructors.size();
                if (conf==null) {
                    sLog.debug("    new direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+instructors.size()+" instructors)");
                    conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeDirect);
                    conf.setInstructors(instructors);
                    conf.setNrInstructors(instructors.size());
                    exam.getConflicts().add(conf);
                    Exam other = dc.getOtherExam().getExam(hibSession);
                    other.getConflicts().add(conf);
                    otherExams.add(other);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    conf.getExams().add(other);
                    hibSession.save(conf);
                    iCnt[sInstructors][conf.getConflictType()][sCreate]+=conf.getNrInstructors();
                } else {
                    conflicts.remove(conf);
                    boolean change = (instructors.size()!=conf.getStudents().size() && !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        sLog.debug("    update direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
                        conf.setInstructors(instructors);
                        conf.setNrInstructors(instructors.size());
                        hibSession.update(conf);
                        iCnt[sInstructors][conf.getConflictType()][sUpdate]+=conf.getNrInstructors();
                    } 
                }
            }
            
            for (BackToBackConflict btb : assignment.getBackToBackConflicts()) {
                int type = (btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                if (btb.getOtherExam()==null) continue;
                ExamConflict conf = null;
                for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=type) continue;
                    if (c.getNrStudents()==0) continue;
                    Exam other = null;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam x = (Exam)i.next();
                        if (x.getUniqueId().equals(btb.getOtherExam().getExamId())) { other = x; break; }
                    }
                    if (other==null) continue;
                    conf = c; break;
                }
                HashSet<Student> students = getStudents(hibSession, btb.getStudents());
                if (exam.getUniqueId().compareTo(btb.getOtherExam().getExamId())<0) 
                    iTotal[sStudents][type]+=students.size();
                if (conf==null) {
                    sLog.debug("    new btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+students.size()+" students)");
                    conf = new ExamConflict();
                    conf.setConflictType(type);
                    conf.setStudents(students);
                    conf.setNrStudents(students.size());
                    conf.setDistance(btb.getDistance());
                    exam.getConflicts().add(conf);
                    Exam other = btb.getOtherExam().getExam(hibSession);
                    other.getConflicts().add(conf);
                    otherExams.add(other);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    conf.getExams().add(other);
                    hibSession.save(conf);
                    iCnt[sStudents][conf.getConflictType()][sCreate]+=conf.getNrStudents();
                } else {
                    conflicts.remove(conf);
                    boolean change = (students.size()!=conf.getStudents().size() && !students.containsAll(conf.getStudents()));
                    if (change) {
                        sLog.debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
                        conf.setStudents(students);
                        conf.setNrStudents(students.size());
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sStudents][conf.getConflictType()][sUpdate]+=conf.getNrStudents();
                    } else if (conf.getDistance()==null || Math.abs(conf.getDistance()-btb.getDistance())>1.0) {
                        sLog.debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" (distance "+conf.getDistance()+" -> "+btb.getDistance()+")");
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sStudents][conf.getConflictType()][sUpdate]+=conf.getNrStudents();
                    }
                }
            }
            for (BackToBackConflict btb : assignment.getInstructorBackToBackConflicts()) {
                int type = (btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                if (btb.getOtherExam()==null) continue;
                ExamConflict conf = null;
                for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=type) continue;
                    if (c.getNrInstructors()==0) continue;
                    Exam other = null;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam x = (Exam)i.next();
                        if (x.getUniqueId().equals(btb.getOtherExam().getExamId())) { other = x; break; }
                    }
                    if (other==null) continue;
                    conf = c; break;
                }
                HashSet<DepartmentalInstructor> instructors = getInstructors(hibSession, btb.getStudents());
                iTotal[sInstructors][type]+=instructors.size();
                if (conf==null) {
                    sLog.debug("    new btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+instructors.size()+" instructors)");
                    conf = new ExamConflict();
                    conf.setConflictType(type);
                    conf.setInstructors(instructors);
                    conf.setNrInstructors(instructors.size());
                    if (btb.isDistance()) conf.setDistance(btb.getDistance());
                    exam.getConflicts().add(conf);
                    Exam other = btb.getOtherExam().getExam(hibSession);
                    other.getConflicts().add(conf);
                    otherExams.add(other);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    conf.getExams().add(other);
                    hibSession.save(conf);
                    iCnt[sInstructors][conf.getConflictType()][sCreate]+=conf.getNrInstructors();
                } else {
                    conflicts.remove(conf);
                    boolean change = (instructors.size()!=conf.getStudents().size() && !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        sLog.debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
                        conf.setInstructors(instructors);
                        conf.setNrInstructors(instructors.size());
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sInstructors][conf.getConflictType()][sUpdate]+=conf.getNrInstructors();
                    } else if (conf.getDistance()==null || Math.abs(conf.getDistance()-btb.getDistance())>1.0) {
                        sLog.debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" (distance "+conf.getDistance()+" -> "+btb.getDistance()+")");
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sInstructors][conf.getConflictType()][sUpdate]+=conf.getNrInstructors();
                    }
                }
            }
            
            for (MoreThanTwoADayConflict m2d : assignment.getMoreThanTwoADaysConflicts()) {
                if (m2d.getOtherExams()==null || m2d.getOtherExams().isEmpty()) continue;
                ExamConflict conf = null;
                conf: for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=ExamConflict.sConflictTypeMoreThanTwoADay) continue;
                    if (c.getNrStudents()==0) continue;
                    if (c.getExams().size()!=1+m2d.getOtherExams().size()) continue;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam other = (Exam)i.next();
                        if (other.getUniqueId().equals(exam.getUniqueId())) continue;
                        boolean contain = false;
                        for (ExamAssignment x:m2d.getOtherExams()) {
                            if (x.getExamId().equals(other.getUniqueId())) { contain = true; break; }
                        }
                        if (!contain) continue conf;
                    }
                    conf = c; break;
                }
                HashSet<Student> students = getStudents(hibSession, m2d.getStudents());
                boolean smallest = true;
                for (ExamAssignment x:m2d.getOtherExams())
                    if (exam.getUniqueId().compareTo(x.getExamId())>0) {smallest=false; break;}
                if (smallest)
                    iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]+=students.size();
                String name = assignment.getExamName();
                for (ExamAssignment x:m2d.getOtherExams()) { name += " "+x.getExamName(); }
                if (conf==null) {
                    sLog.debug("    new m2d "+name+" ("+students.size()+" students)");
                    conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                    conf.setStudents(students);
                    conf.setNrStudents(students.size());
                    exam.getConflicts().add(conf);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    for (ExamAssignment x:m2d.getOtherExams()) { 
                        Exam other = x.getExam(hibSession);
                        other.getConflicts().add(conf);
                        otherExams.add(other);
                        conf.getExams().add(other);
                    }
                    hibSession.save(conf);
                    iCnt[sStudents][conf.getConflictType()][sCreate]+=conf.getNrStudents();
                } else {
                    conflicts.remove(conf);
                    boolean change = (students.size()!=conf.getStudents().size() && !students.containsAll(conf.getStudents()));
                    if (change) {
                        sLog.debug("    update m2d "+name+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
                        conf.setStudents(students);
                        conf.setNrStudents(students.size());
                        hibSession.update(conf);
                        iCnt[sStudents][conf.getConflictType()][sUpdate]+=conf.getNrStudents();
                    } 
                }
            }
            for (MoreThanTwoADayConflict m2d : assignment.getInstructorMoreThanTwoADaysConflicts()) {
                if (m2d.getOtherExams()==null || m2d.getOtherExams().isEmpty()) continue;
                ExamConflict conf = null;
                conf: for (ExamConflict c: conflicts) {
                    if (c.getConflictType()!=ExamConflict.sConflictTypeMoreThanTwoADay) continue;
                    if (c.getNrInstructors()==0) continue;
                    if (c.getExams().size()!=1+m2d.getOtherExams().size()) continue;
                    for (Iterator i=c.getExams().iterator();i.hasNext();) {
                        Exam other = (Exam)i.next();
                        if (other.getUniqueId().equals(exam.getUniqueId())) continue;
                        boolean contain = false;
                        for (ExamAssignment x:m2d.getOtherExams()) {
                            if (x.getExamId().equals(other.getUniqueId())) { contain = true; break; }
                        }
                        if (!contain) continue conf;
                    }
                    conf = c; break;
                }
                HashSet<DepartmentalInstructor> instructors = getInstructors(hibSession, m2d.getStudents());
                boolean smallest = true;
                for (ExamAssignment x:m2d.getOtherExams())
                    if (exam.getUniqueId().compareTo(x.getExamId())>0) {smallest=false; break;}
                if (smallest)
                    iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]+=instructors.size();
                String name = assignment.getExamName();
                for (ExamAssignment x:m2d.getOtherExams()) { name += " "+x.getExamName(); }
                if (conf==null) {
                    sLog.debug("    new btb "+name+" ("+instructors.size()+" instructors)");
                    conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                    conf.setInstructors(instructors);
                    conf.setNrInstructors(instructors.size());
                    exam.getConflicts().add(conf);
                    conf.setExams(new HashSet());
                    conf.getExams().add(exam);
                    for (ExamAssignment x:m2d.getOtherExams()) { 
                        Exam other = x.getExam(hibSession);
                        other.getConflicts().add(conf);
                        otherExams.add(other);
                        conf.getExams().add(other);
                    }
                    hibSession.save(conf);
                    iCnt[sInstructors][conf.getConflictType()][sCreate]+=conf.getNrInstructors();
                } else {
                    conflicts.remove(conf);
                    boolean change = (instructors.size()!=conf.getStudents().size() && !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        sLog.debug("    update btb "+name+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
                        conf.setInstructors(instructors);
                        conf.setNrInstructors(instructors.size());
                        hibSession.update(conf);
                        iCnt[sInstructors][conf.getConflictType()][sUpdate]+=conf.getNrInstructors();
                    } 
                }
            }
            
            for (ExamConflict conf : conflicts) {
                String name = "";
                if (conf.getConflictType()==ExamConflict.sConflictTypeDirect) name="direct";
                else if (conf.getConflictType()==ExamConflict.sConflictTypeMoreThanTwoADay) name="m2d";
                else if (conf.getConflictType()==ExamConflict.sConflictTypeBackToBack) name="btb";
                else if (conf.getConflictType()==ExamConflict.sConflictTypeBackToBackDist) name="btb";
                if (conf.getNrInstructors()!=null)
                    iCnt[sInstructors][conf.getConflictType()][sDelete]+=conf.getNrInstructors();
                if (conf.getNrStudents()!=null)
                    iCnt[sStudents][conf.getConflictType()][sDelete]+=conf.getNrStudents();
                for (Iterator i=conf.getExams().iterator();i.hasNext();) {
                    Exam other = (Exam)i.next();
                    name+=" "+other.getLabel();
                    other.getConflicts().remove(conf);
                    if (!other.equals(exam)) otherExams.add(other);
                }
                sLog.debug("  delete "+name+" ("+(conf.getNrStudents()!=null && conf.getNrStudents()>0?conf.getNrStudents()+" students":"")+(conf.getNrInstructors()!=null && conf.getNrInstructors()>0?conf.getNrInstructors()+" instructors":"")+")");
                hibSession.delete(conf);
            }
            
            hibSession.update(exam);
            for (Exam other : otherExams)
                hibSession.update(other);
            
            //hibSession.flush();
            if (tx!=null) tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
    }
    
    protected static HashSet<Student> getStudents(org.hibernate.Session hibSession, Collection studentIds) {
        HashSet<Student> students = new HashSet();
        if (studentIds==null || studentIds.isEmpty()) return students;
        for (Iterator i=studentIds.iterator();i.hasNext();) {
            Long studentId = (Long)i.next();
            Student student = new StudentDAO().get(studentId, hibSession);
            if (student!=null) students.add(student);
        }
        return students;
    }

    protected static HashSet<DepartmentalInstructor> getInstructors(org.hibernate.Session hibSession, Collection instructorIds) {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        if (instructorIds==null || instructorIds.isEmpty()) return instructors;
        for (Iterator i=instructorIds.iterator();i.hasNext();) {
            Long instructorId = (Long)i.next();
            DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(instructorId, hibSession);
            if (instructor!=null) instructors.add(instructor);
        }
        return instructors;
    }
    
    public void update(Long sessionId, Integer examType, org.hibernate.Session hibSession) throws Exception {
        iCnt = new int[][][] {{{0,0,0},{0,0,0},{0,0,0},{0,0,0}},{{0,0,0},{0,0,0},{0,0,0},{0,0,0}}};
        iTotal = new int[][] {{0,0,0,0},{0,0,0,0}};
        TreeSet<ExamAssignmentInfo> exams = PdfLegacyExamReport.loadExams(sessionId, examType, true, false, false);
        for (ExamAssignmentInfo exam : exams) {
            sLog.info("Checking "+exam.getExamName()+" ...");
            try {
                updateConflicts(exam, hibSession);
            } catch (Exception e) {
                sLog.error("Update of "+exam.getExamName()+" failed, reason: "+e.getMessage(),e);
                throw e;
            }
        }
        
        if (iTotal[sStudents][ExamConflict.sConflictTypeDirect]>0) {
            sLog.info("Direct student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeDirect]);
            sLog.info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sCreate]);
            sLog.info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sUpdate]);
            sLog.info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeDirect]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]>0) {
            sLog.info(">2 exams a day student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]);
            sLog.info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]);
            sLog.info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]);
            sLog.info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]>0) {
            sLog.info("Back-to-back student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]);
            sLog.info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sCreate]);
            sLog.info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sUpdate]);
            sLog.info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]>0) {
            sLog.info("Distance back-to-back student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]);
            sLog.info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sCreate]);
            sLog.info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sUpdate]);
            sLog.info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sDelete]));
        }
        
        if (iTotal[sInstructors][ExamConflict.sConflictTypeDirect]>0) {
            sLog.info("Direct instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeDirect]);
            sLog.info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sCreate]);
            sLog.info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sUpdate]);
            sLog.info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeDirect]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]>0) {
            sLog.info(">2 exams a day instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]);
            sLog.info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]);
            sLog.info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]);
            sLog.info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]>0) {
            sLog.info("Back-to-back instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]);
            sLog.info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sCreate]);
            sLog.info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sUpdate]);
            sLog.info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]>0) {
            sLog.info("Distance back-to-back instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]);
            sLog.info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sCreate]);
            sLog.info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sUpdate]);
            sLog.info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sDelete]);
            sLog.info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sDelete]));
        }
    }
    
    public static void main(String args[]) {
        try {
            Properties props = new Properties();
            props.setProperty("log4j.rootLogger", "DEBUG, A1");
            props.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
            props.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
            props.setProperty("log4j.appender.A1.layout.ConversionPattern","%-5p %c{2}: %m%n");
            props.setProperty("log4j.logger.org.hibernate","INFO");
            props.setProperty("log4j.logger.org.hibernate.cfg","WARN");
            props.setProperty("log4j.logger.org.hibernate.cache.EhCacheProvider","ERROR");
            props.setProperty("log4j.logger.org.unitime.commons.hibernate","INFO");
            props.setProperty("log4j.logger.net","INFO");
            PropertyConfigurator.configure(props);
            
            HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
            
            Session session = Session.getSessionUsingInitiativeYearTerm(
                    ApplicationProperties.getProperty("initiative", "PWL"),
                    ApplicationProperties.getProperty("year","2008"),
                    ApplicationProperties.getProperty("term","Fal")
                    );
            if (session==null) {
                sLog.error("Academic session not found, use properties initiative, year, and term to set academic session.");
                System.exit(0);
            } else {
                sLog.info("Session: "+session);
            }
            
            int examType = (ApplicationProperties.getProperty("type","final").equalsIgnoreCase("final")?Exam.sExamTypeFinal:Exam.sExamTypeMidterm);
            
            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();
                
                new UpdateExamConflicts().update(session.getUniqueId(), examType, hibSession);
                
                tx.commit();
            } catch (Exception e) {
                sLog.error(e);
                if (tx!=null) tx.rollback();
            }

        } catch (Exception e) {
            sLog.error(e);
        }
    }
    
}
