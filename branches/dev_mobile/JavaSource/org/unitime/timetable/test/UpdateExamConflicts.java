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
package org.unitime.timetable.test;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Transaction;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.Parameters;

/**
 * @author Tomas Muller
 */
public class UpdateExamConflicts {
    private static Log sLog = LogFactory.getLog(UpdateExamConflicts.class);
    private static DecimalFormat sDF = new DecimalFormat("0.0");
    private static boolean sDebug = false;
    
    private static int sCreate = 0;
    private static int sUpdate = 1;
    private static int sDelete = 2;
    private static int sStudents = 0;
    private static int sInstructors = 1;
    private int[][][] iCnt;
    private int[][] iTotal;
    
    private DataExchangeHelper iHelper = null;
    
    public UpdateExamConflicts() {}
    
    public UpdateExamConflicts(DataExchangeHelper helper) { iHelper = helper; }
    
    private void debug(String message) {
        if (!sDebug) return;
        if (iHelper==null) 
            sLog.debug(message);
        else
            iHelper.debug(message);
    }
    
    private void info(String message) {
        if (iHelper==null) 
            sLog.info(message);
        else
            iHelper.info(message);
    }

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
                    debug("    new direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+students.size()+" students)");
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
                    boolean change = (students.size()!=conf.getStudents().size() || !students.containsAll(conf.getStudents()));
                    if (change) {
                        debug("    update direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
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
                    debug("    new direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+instructors.size()+" instructors)");
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
                    boolean change = (instructors.size()!=conf.getInstructors().size() || !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        debug("    update direct "+assignment.getExamName()+" "+dc.getOtherExam().getExamName()+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
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
                    debug("    new btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+students.size()+" students)");
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
                    boolean change = (students.size()!=conf.getStudents().size() || !students.containsAll(conf.getStudents()));
                    if (change) {
                        debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
                        conf.setStudents(students);
                        conf.setNrStudents(students.size());
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sStudents][conf.getConflictType()][sUpdate]+=conf.getNrStudents();
                    } else if (conf.getDistance()==null || Math.abs(conf.getDistance()-btb.getDistance())>1.0) {
                        debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" (distance "+conf.getDistance()+" -> "+btb.getDistance()+")");
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
                if (exam.getUniqueId().compareTo(btb.getOtherExam().getExamId())<0)
                    iTotal[sInstructors][type]+=instructors.size();
                if (conf==null) {
                    debug("    new btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+instructors.size()+" instructors)");
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
                    boolean change = (instructors.size()!=conf.getStudents().size() || !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
                        conf.setInstructors(instructors);
                        conf.setNrInstructors(instructors.size());
                        conf.setDistance(btb.getDistance());
                        hibSession.update(conf);
                        iCnt[sInstructors][conf.getConflictType()][sUpdate]+=conf.getNrInstructors();
                    } else if (conf.getDistance()==null || Math.abs(conf.getDistance()-btb.getDistance())>1.0) {
                        debug("    update btb "+assignment.getExamName()+" "+btb.getOtherExam().getExamName()+" (distance "+conf.getDistance()+" -> "+btb.getDistance()+")");
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
                    debug("    new m2d "+name+" ("+students.size()+" students)");
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
                    boolean change = (students.size()!=conf.getStudents().size() || !students.containsAll(conf.getStudents()));
                    if (change) {
                        debug("    update m2d "+name+" ("+conf.getNrStudents()+"->"+students.size()+" students)");
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
                    debug("    new btb "+name+" ("+instructors.size()+" instructors)");
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
                    boolean change = (instructors.size()!=conf.getStudents().size() || !instructors.containsAll(conf.getInstructors()));
                    if (change) {
                        debug("    update btb "+name+" ("+conf.getNrInstructors()+"->"+instructors.size()+" instructors)");
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
                debug("  delete "+name+" ("+(conf.getNrStudents()!=null && conf.getNrStudents()>0?conf.getNrStudents()+" students":"")+(conf.getNrInstructors()!=null && conf.getNrInstructors()>0?conf.getNrInstructors()+" instructors":"")+")");
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
            else sLog.warn("Student "+studentId+" not found.");
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
            else sLog.warn("Instructor "+instructorId+" not found.");
        }
        return instructors;
    }
    
    public void simpleCheck(ExamAssignmentInfo e1, ExamAssignmentInfo e2) {
        if (e1.getNrDirectConflicts()!=e2.getNrDirectConflicts()) {
            sLog.warn("Wrong number of direct student conflicts for "+e1.getExamName()+" ("+e1.getNrDirectConflicts()+"!="+e2.getNrDirectConflicts()+")");
        }
        if (e1.getNrBackToBackConflicts()!=e2.getNrBackToBackConflicts()) {
            sLog.warn("Wrong number of back-to-back student conflicts for "+e1.getExamName()+" ("+e1.getNrBackToBackConflicts()+"!="+e2.getNrBackToBackConflicts()+")");
        }
        if (e1.getNrMoreThanTwoConflicts()!=e2.getNrMoreThanTwoConflicts()) {
            sLog.warn("Wrong number of >2 exams a day student conflicts for "+e1.getExamName()+" ("+e1.getNrMoreThanTwoConflicts()+"!="+e2.getNrMoreThanTwoConflicts()+")");
        }
        if (e1.getNrInstructorDirectConflicts()!=e2.getNrInstructorDirectConflicts()) {
            sLog.warn("Wrong number of direct instructor conflicts for "+e1.getExamName()+" ("+e1.getNrInstructorDirectConflicts()+"!="+e2.getNrInstructorDirectConflicts()+")");
        }
        if (e1.getNrInstructorBackToBackConflicts()!=e2.getNrInstructorBackToBackConflicts()) {
            sLog.warn("Wrong number of back-to-back instructor conflicts for "+e1.getExamName()+" ("+e1.getNrInstructorBackToBackConflicts()+"!="+e2.getNrInstructorBackToBackConflicts()+")");
        }
        if (e1.getNrInstructorMoreThanTwoConflicts()!=e2.getNrInstructorMoreThanTwoConflicts()) {
            sLog.warn("Wrong number of >2 exams a day instructor conflicts for "+e1.getExamName()+" ("+e1.getNrInstructorMoreThanTwoConflicts()+"!="+e2.getNrInstructorMoreThanTwoConflicts()+")");
        }
    }
    
    public TreeSet<ExamAssignmentInfo> loadExams(Long sessionId, Long examTypeId) throws Exception {
        info("Loading exams...");
        long t0 = System.currentTimeMillis();
        Hashtable<Long, Exam> exams = new Hashtable();
        for (Iterator i=new ExamDAO().getSession().createQuery(
                "select x from Exam x where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId"
                ).setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
            Exam exam = (Exam)i.next();
            exams.put(exam.getUniqueId(), exam);
        }
        info("  Fetching related objects (class)...");
        new ExamDAO().getSession().createQuery(
                "select c from Class_ c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:classType and c.uniqueId=o.ownerId")
                .setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setInteger("classType", ExamOwner.sOwnerTypeClass).setCacheable(true).list();
        info("  Fetching related objects (config)...");
        new ExamDAO().getSession().createQuery(
                "select c from InstrOfferingConfig c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:configType and c.uniqueId=o.ownerId")
                .setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setInteger("configType", ExamOwner.sOwnerTypeConfig).setCacheable(true).list();
        info("  Fetching related objects (course)...");
        new ExamDAO().getSession().createQuery(
                "select c from CourseOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:courseType and c.uniqueId=o.ownerId")
                .setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setInteger("courseType", ExamOwner.sOwnerTypeCourse).setCacheable(true).list();
        info("  Fetching related objects (offering)...");
        new ExamDAO().getSession().createQuery(
                "select c from InstructionalOffering c, ExamOwner o where o.exam.session.uniqueId=:sessionId and o.exam.examType.uniqueId=:examTypeId and o.ownerType=:offeringType and c.uniqueId=o.ownerId")
                .setLong("sessionId", sessionId)
                .setLong("examTypeId", examTypeId)
                .setInteger("offeringType", ExamOwner.sOwnerTypeOffering).setCacheable(true).list();
        Hashtable<Long,Set<Long>> owner2students = new Hashtable();
        Hashtable<Long,Set<Exam>> student2exams = new Hashtable();
        Hashtable<Long,Hashtable<Long,Set<Long>>> owner2course2students = new Hashtable();
        info("  Loading students (class)...");
        for (Iterator i=
            new ExamDAO().getSession().createQuery(
            "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
            "Exam x inner join x.owners o, "+
            "StudentClassEnrollment e inner join e.clazz c "+
            "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
            "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeClass+" and "+
            "o.ownerId=c.uniqueId").setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
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
        info("  Loading students (config)...");
        for (Iterator i=
            new ExamDAO().getSession().createQuery(
                    "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                    "Exam x inner join x.owners o, "+
                    "StudentClassEnrollment e inner join e.clazz c " +
                    "inner join c.schedulingSubpart.instrOfferingConfig ioc " +
                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeConfig+" and "+
                    "o.ownerId=ioc.uniqueId").setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
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
        info("  Loading students (course)...");
        for (Iterator i=
            new ExamDAO().getSession().createQuery(
                    "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                    "Exam x inner join x.owners o, "+
                    "StudentClassEnrollment e inner join e.courseOffering co " +
                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeCourse+" and "+
                    "o.ownerId=co.uniqueId").setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
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
        info("  Loading students (offering)...");
        for (Iterator i=
            new ExamDAO().getSession().createQuery(
                    "select x.uniqueId, o.uniqueId, e.student.uniqueId, e.courseOffering.uniqueId from "+
                    "Exam x inner join x.owners o, "+
                    "StudentClassEnrollment e inner join e.courseOffering.instructionalOffering io " +
                    "where x.session.uniqueId=:sessionId and x.examType.uniqueId=:examTypeId and "+
                    "o.ownerType="+org.unitime.timetable.model.ExamOwner.sOwnerTypeOffering+" and "+
                    "o.ownerId=io.uniqueId").setLong("sessionId", sessionId).setLong("examTypeId", examTypeId).setCacheable(true).list().iterator();i.hasNext();) {
            Object[] o = (Object[])i.next();
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
        Hashtable<Long, Set<Meeting>> period2meetings = new Hashtable();
        Parameters p = new Parameters(sessionId, examTypeId);
        info("  Creating exam assignments...");
        TreeSet<ExamAssignmentInfo> ret = new TreeSet();
        for (Enumeration<Exam> e = exams.elements(); e.hasMoreElements();) {
            Exam exam = (Exam)e.nextElement();
            ExamAssignmentInfo info = new ExamAssignmentInfo(exam, owner2students, owner2course2students, student2exams, period2meetings, p);
            ret.add(info);
        }
        long t1 = System.currentTimeMillis();
        info("Exams loaded in "+sDF.format((t1-t0)/1000.0)+"s.");
        return ret;
    }

    public void update(Long sessionId, Long examTypeId, org.hibernate.Session hibSession) throws Exception {
        iCnt = new int[][][] {{{0,0,0},{0,0,0},{0,0,0},{0,0,0}},{{0,0,0},{0,0,0},{0,0,0},{0,0,0}}};
        iTotal = new int[][] {{0,0,0,0},{0,0,0,0}};
        TreeSet<ExamAssignmentInfo> exams = loadExams(sessionId, examTypeId);
        for (ExamAssignmentInfo exam : exams) {
            debug("Checking "+exam.getExamName()+" ...");
            updateConflicts(exam, hibSession);
            //simpleCheck(exam, new ExamAssignmentInfo(exam.getExam(),true));
        }
        
        if (iTotal[sStudents][ExamConflict.sConflictTypeDirect]>0) {
            info("Direct student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeDirect]);
            info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sCreate]);
            info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sUpdate]);
            info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeDirect][sDelete]);
            info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeDirect]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeDirect][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]>0) {
            info(">2 exams a day student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]);
            info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]);
            info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]);
            info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]);
            info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]>0) {
            info("Back-to-back student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]);
            info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sCreate]);
            info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sUpdate]);
            info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sDelete]);
            info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeBackToBack]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBack][sDelete]));
        }
        if (iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]>0) {
            info("Distance back-to-back student conflicts: "+iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]);
            info("    created: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sCreate]);
            info("    updated: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sUpdate]);
            info("    deleted: "+iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sDelete]);
            info("  unchanged: "+(iTotal[sStudents][ExamConflict.sConflictTypeBackToBackDist]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sCreate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sUpdate]-iCnt[sStudents][ExamConflict.sConflictTypeBackToBackDist][sDelete]));
        }
        
        if (iTotal[sInstructors][ExamConflict.sConflictTypeDirect]>0) {
            info("Direct instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeDirect]);
            info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sCreate]);
            info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sUpdate]);
            info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sDelete]);
            info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeDirect]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeDirect][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]>0) {
            info(">2 exams a day instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]);
            info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]);
            info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]);
            info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]);
            info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeMoreThanTwoADay][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]>0) {
            info("Back-to-back instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]);
            info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sCreate]);
            info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sUpdate]);
            info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sDelete]);
            info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeBackToBack]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBack][sDelete]));
        }
        if (iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]>0) {
            info("Distance back-to-back instructor conflicts: "+iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]);
            info("    created: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sCreate]);
            info("    updated: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sUpdate]);
            info("    deleted: "+iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sDelete]);
            info("  unchanged: "+(iTotal[sInstructors][ExamConflict.sConflictTypeBackToBackDist]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sCreate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sUpdate]-iCnt[sInstructors][ExamConflict.sConflictTypeBackToBackDist][sDelete]));
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
            
            ExamType examType = ExamType.findByReference(ApplicationProperties.getProperty("type","final"));
            
            org.hibernate.Session hibSession = new _RootDAO().getSession();
            
            Transaction tx = null;
            try {
                tx = hibSession.beginTransaction();
                
                new UpdateExamConflicts().update(session.getUniqueId(), examType.getUniqueId(), hibSession);
                
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
