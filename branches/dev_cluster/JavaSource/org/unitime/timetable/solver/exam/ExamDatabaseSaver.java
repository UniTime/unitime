/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.exam;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamEvent;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.remote.RemoteSolver;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoomPlacement;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Progress;
import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class ExamDatabaseSaver extends ExamSaver {
    private Long iSessionId;
    private Long iExamTypeId;
    private Progress iProgress = null;

    public ExamDatabaseSaver(Solver solver) {
        super(solver);
        iProgress = Progress.getInstance(getModel());
        iSessionId = getModel().getProperties().getPropertyLong("General.SessionId",(Long)null);
        iExamTypeId = getModel().getProperties().getPropertyLong("Exam.Type", null);
    }
    
    public void save() {
        iProgress.setStatus("Saving solution ...");
        org.hibernate.Session hibSession = new ExamDAO().getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            saveSolution(hibSession);
            tx.commit();
            
            iProgress.setPhase("Refreshing solution ...", 1);
            try {
            	RemoteSolver.refreshExamSolution(iSessionId, iExamTypeId);
            	iProgress.incProgress();
            } catch (Exception e) {
                iProgress.warn("Unable to refresh solution, reason:"+e.getMessage(),e);
            }
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            iProgress.fatal("Unable to save a solution, reason: "+e.getMessage(),e);
        }
    }
    
    private String getExamLabel(org.unitime.timetable.model.Exam exam) {
        return "<A href='examDetail.do?examId="+exam.getUniqueId()+"'>"+exam.getLabel()+"</A>";
    }

    protected void saveSolution(org.hibernate.Session hibSession) {
        TimetableManager owner = TimetableManager.findByExternalId(getModel().getProperties().getProperty("General.OwnerPuid"));
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId, iExamTypeId);
        Hashtable<Long,ExamEvent> examEvents = new Hashtable();
        for (Iterator i=hibSession.createQuery(
                "select e from ExamEvent e where e.exam.session.uniqueId=:sessionId and e.exam.examType.uniqueId=:examTypeId")
                .setLong("sessionId",iSessionId)
                .setLong("examTypeId", iExamTypeId)
                .iterate(); i.hasNext();) {
            ExamEvent e = (ExamEvent)i.next();
            examEvents.put(e.getExam().getUniqueId(),e);
        }
        iProgress.setPhase("Saving assignments...", exams.size());
        Hashtable examTable = new Hashtable();
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            ExamAssignment oldAssignment = new ExamAssignment(exam);
            exam.setAssignedPeriod(null);
            exam.setAssignedPreference(null);
            exam.getAssignedRooms().clear();
            ExamEvent event = examEvents.get(exam.getUniqueId());
            if (event!=null) hibSession.delete(event);
            for (Iterator j=exam.getConflicts().iterator();j.hasNext();) {
                ExamConflict conf = (ExamConflict)j.next();
                hibSession.delete(conf);
                j.remove();
            }
            Exam examVar = null;
            for (Exam x: getModel().variables()) {
                if (exam.getUniqueId().equals(x.getId())) { examVar=x;break; }
            }
            if (examVar==null) {
                iProgress.warn("Exam "+getExamLabel(exam)+" was not loaded.");
                if (oldAssignment.getPeriodId()!=null) {
                    SubjectArea subject = null;
                    Department dept = null;
                    for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                        ExamOwner xo = (ExamOwner)j.next();
                        subject = xo.getCourse().getSubjectArea();
                        dept = subject.getDepartment();
                        break;
                    }
                    ChangeLog.addChange(hibSession,
                            owner,
                            exam.getSession(),
                            exam,
                            exam.getName()+" ("+oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", ")+" &rarr; N/A)",
                            ChangeLog.Source.EXAM_SOLVER,
                            ChangeLog.Operation.UNASSIGN,
                            subject,
                            dept);
                }
                continue;
            }
            examTable.put(examVar.getId(), exam);
            ExamPlacement placement = (ExamPlacement)examVar.getAssignment();
            if (placement==null) {
                iProgress.warn("Exam "+getExamLabel(exam)+" has no assignment.");
                if (oldAssignment.getPeriodId()!=null) {
                    SubjectArea subject = null;
                    Department dept = null;
                    for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                        ExamOwner xo = (ExamOwner)j.next();
                        subject = xo.getCourse().getSubjectArea();
                        dept = subject.getDepartment();
                        break;
                    }
                    ChangeLog.addChange(hibSession,
                            owner,
                            exam.getSession(),
                            exam,
                            exam.getName()+" ("+oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", ")+" &rarr; N/A)",
                            ChangeLog.Source.EXAM_SOLVER,
                            ChangeLog.Operation.UNASSIGN,
                            subject,
                            dept);
                }
                continue;
            }
            ExamPeriod period = new ExamPeriodDAO().get(placement.getPeriod().getId());
            if (period==null) {
                iProgress.warn("Examination period "+placement.getPeriod().getDayStr()+" "+placement.getPeriod().getTimeStr()+" not found.");
                if (oldAssignment.getPeriodId()!=null) {
                    SubjectArea subject = null;
                    Department dept = null;
                    for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                        ExamOwner xo = (ExamOwner)j.next();
                        subject = xo.getCourse().getSubjectArea();
                        dept = subject.getDepartment();
                        break;
                    }
                    ChangeLog.addChange(hibSession,
                            owner,
                            exam.getSession(),
                            exam,
                            exam.getName()+" ("+oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", ")+" &rarr; N/A)",
                            ChangeLog.Source.EXAM_SOLVER,
                            ChangeLog.Operation.UNASSIGN,
                            subject,
                            dept);
                }
                continue;
            }
            exam.setAssignedPeriod(period);
            for (Iterator j=placement.getRoomPlacements().iterator();j.hasNext();) {
                ExamRoomPlacement room = (ExamRoomPlacement)j.next();
                Location location = new LocationDAO().get(room.getId());
                if (location==null) {
                    iProgress.warn("Location "+room.getName()+" (id:"+room.getId()+") not found.");
                    continue;
                }
                exam.getAssignedRooms().add(location);
            }
            exam.setAssignedPreference(new ExamAssignment(placement).getAssignedPreferenceString());
            
            hibSession.saveOrUpdate(exam);
            ExamAssignment newAssignment = new ExamAssignment(exam);
            if (!ToolBox.equals(newAssignment.getPeriodId(), oldAssignment.getPeriodId()) || !ToolBox.equals(newAssignment.getRooms(), oldAssignment.getRooms())) {
                SubjectArea subject = null;
                Department dept = null;
                for (Iterator j=new TreeSet(exam.getOwners()).iterator();j.hasNext();) {
                    ExamOwner xo = (ExamOwner)j.next();
                    subject = xo.getCourse().getSubjectArea();
                    dept = subject.getDepartment();
                    break;
                }
                ChangeLog.addChange(hibSession,
                        owner,
                        exam.getSession(),
                        exam,
                        exam.getName()+" ("+
                            (oldAssignment.getPeriod()==null?"N/A":oldAssignment.getPeriodAbbreviation()+" "+oldAssignment.getRoomsName(", "))+
                            " &rarr; "+newAssignment.getPeriodAbbreviation()+" "+newAssignment.getRoomsName(", ")+")",
                        ChangeLog.Source.EXAM_SOLVER,
                        ChangeLog.Operation.ASSIGN,
                        subject,
                        dept);
            }
        }
        iProgress.setPhase("Saving conflicts...", getModel().assignedVariables().size());
        for (Exam examVar: getModel().assignedVariables()) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)examTable.get(examVar.getId());
            if (exam==null) continue;
            ExamPlacement placement = (ExamPlacement)examVar.getAssignment();
            ExamAssignmentInfo info = new ExamAssignmentInfo(placement);
            for (Iterator i=info.getDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                if (dc.getOtherExam()==null) continue;
                if (examVar.getId()<dc.getOtherExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(dc.getOtherExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+dc.getOtherExam().getExamName()+" (id:"+dc.getOtherExam().getExamId()+") not found.");
                        continue;
                    }
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeDirect);
                    conf.setStudents(getStudents(hibSession, dc.getStudents()));
                    conf.setNrStudents(conf.getStudents().size());
                    hibSession.save(conf);
                    exam.getConflicts().add(conf);
                    otherExam.getConflicts().add(conf);
                    iProgress.debug("Direct conflict of "+dc.getStudents().size()+" students between "+exam.getLabel()+" and "+otherExam.getLabel());
                }
            }
            for (Iterator i=info.getBackToBackConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.BackToBackConflict btb = (ExamAssignmentInfo.BackToBackConflict)i.next();
                if (examVar.getId()<btb.getOtherExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(btb.getOtherExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+btb.getOtherExam().getExamName()+" (id:"+btb.getOtherExam().getExamId()+") not found.");
                        continue;
                    }
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                    conf.setDistance(btb.getDistance());
                    conf.setStudents(getStudents(hibSession, btb.getStudents()));
                    conf.setNrStudents(conf.getStudents().size());
                    exam.getConflicts().add(conf);
                    otherExam.getConflicts().add(conf);
                    hibSession.save(conf);
                    iProgress.debug("Back-to-back conflict of "+btb.getStudents().size()+" students between "+exam.getLabel()+" and "+otherExam.getLabel());
                }
            }
            m2d: for (Iterator i=info.getMoreThanTwoADaysConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.MoreThanTwoADayConflict m2d = (ExamAssignmentInfo.MoreThanTwoADayConflict)i.next();
                HashSet confExams = new HashSet();
                confExams.add(exam);
                for (Iterator j=m2d.getOtherExams().iterator();j.hasNext();) {
                    ExamAssignment otherExamAsg = (ExamAssignment)j.next();
                    if (examVar.getId()>=otherExamAsg.getExamId().longValue()) continue m2d;
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(otherExamAsg.getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+otherExamAsg.getExamName()+" (id:"+otherExamAsg.getExamId()+") not found.");
                        continue;
                    }
                    confExams.add(otherExam);
                }
                if (confExams.size()>=3) {
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                    conf.setStudents(getStudents(hibSession, m2d.getStudents()));
                    conf.setNrStudents(conf.getStudents().size());
                    hibSession.save(conf);
                    for (Iterator j=confExams.iterator();j.hasNext();)
                        ((org.unitime.timetable.model.Exam)j.next()).getConflicts().add(conf);
                    iProgress.debug("More than 2 a day conflict of "+m2d.getStudents().size()+" students between "+exam.getLabel()+" and "+m2d.getOtherExams());
                }
            }

            for (Iterator i=info.getInstructorDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                if (dc.getOtherExam()==null) continue;
                if (examVar.getId()<dc.getOtherExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(dc.getOtherExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+dc.getOtherExam().getExamName()+" (id:"+dc.getOtherExam().getExamId()+") not found.");
                        continue;
                    }
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeDirect);
                    conf.setInstructors(getInstructors(hibSession, dc.getStudents()));
                    conf.setNrInstructors(conf.getInstructors().size());
                    hibSession.save(conf);
                    exam.getConflicts().add(conf);
                    otherExam.getConflicts().add(conf);
                    iProgress.debug("Direct conflict of "+dc.getStudents().size()+" instructors between "+exam.getLabel()+" and "+otherExam.getLabel());
                }
            }
            for (Iterator i=info.getInstructorBackToBackConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.BackToBackConflict btb = (ExamAssignmentInfo.BackToBackConflict)i.next();
                if (examVar.getId()<btb.getOtherExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(btb.getOtherExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+btb.getOtherExam().getExamName()+" (id:"+btb.getOtherExam().getExamId()+") not found.");
                        continue;
                    }
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(btb.isDistance()?ExamConflict.sConflictTypeBackToBackDist:ExamConflict.sConflictTypeBackToBack);
                    conf.setDistance(btb.getDistance());
                    conf.setInstructors(getInstructors(hibSession, btb.getStudents()));
                    conf.setNrInstructors(conf.getInstructors().size());
                    exam.getConflicts().add(conf);
                    otherExam.getConflicts().add(conf);
                    hibSession.save(conf);
                    iProgress.debug("Back-to-back conflict of "+btb.getStudents().size()+" instructors between "+exam.getLabel()+" and "+otherExam.getLabel());
                }
            }
            m2d: for (Iterator i=info.getInstructorMoreThanTwoADaysConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.MoreThanTwoADayConflict m2d = (ExamAssignmentInfo.MoreThanTwoADayConflict)i.next();
                HashSet confExams = new HashSet();
                confExams.add(exam);
                for (Iterator j=m2d.getOtherExams().iterator();j.hasNext();) {
                    ExamAssignment otherExamAsg = (ExamAssignment)j.next();
                    if (examVar.getId()>=otherExamAsg.getExamId().longValue()) continue m2d;
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(otherExamAsg.getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+otherExamAsg.getExamName()+" (id:"+otherExamAsg.getExamId()+") not found.");
                        continue;
                    }
                    confExams.add(otherExam);
                }
                if (confExams.size()>=3) {
                    ExamConflict conf = new ExamConflict();
                    conf.setConflictType(ExamConflict.sConflictTypeMoreThanTwoADay);
                    conf.setInstructors(getInstructors(hibSession, m2d.getStudents()));
                    conf.setNrInstructors(conf.getInstructors().size());
                    hibSession.save(conf);
                    for (Iterator j=confExams.iterator();j.hasNext();)
                        ((org.unitime.timetable.model.Exam)j.next()).getConflicts().add(conf);
                    iProgress.debug("More than 2 a day conflict of "+m2d.getStudents().size()+" instructors between "+exam.getLabel()+" and "+m2d.getOtherExams());
                }
            }
        }
        iProgress.setPhase("Saving events...", getModel().assignedVariables().size());
        String ownerPuid = getModel().getProperties().getProperty("General.OwnerPuid");
        EventContact contact = EventContact.findByExternalUniqueId(ownerPuid);
        if (contact==null) {
            TimetableManager manager = TimetableManager.findByExternalId(ownerPuid);
            contact = new EventContact();
            contact.setFirstName(manager.getFirstName());
            contact.setMiddleName(manager.getMiddleName());
            contact.setLastName(manager.getLastName());
            contact.setExternalUniqueId(manager.getExternalUniqueId());
            contact.setEmailAddress(manager.getEmailAddress());
            hibSession.save(contact);
        }
        for (Exam examVar: getModel().assignedVariables()) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)examTable.get(examVar.getId());
            if (exam==null) continue;
            ExamEvent event = exam.generateEvent(null,true);
            if (event!=null) {
                event.setEventName(examVar.getName());
                event.setMinCapacity(examVar.getSize());
                event.setMaxCapacity(examVar.getSize());
                event.setMainContact(contact);
                hibSession.saveOrUpdate(event);
            }
            if (event!=null || !exam.getConflicts().isEmpty()) hibSession.saveOrUpdate(exam);
        }
    }
    
    protected HashSet getStudents(org.hibernate.Session hibSession, Collection studentIds) {
        HashSet students = new HashSet();
        if (studentIds==null || studentIds.isEmpty()) return students;
        for (Iterator i=studentIds.iterator();i.hasNext();) {
            Long studentId = (Long)i.next();
            Student student = new StudentDAO().get(studentId, hibSession);
            if (student!=null) students.add(student);
        }
        return students;
    }

    protected HashSet getInstructors(org.hibernate.Session hibSession, Collection instructorIds) {
        HashSet instructors = new HashSet();
        if (instructorIds==null || instructorIds.isEmpty()) return instructors;
        for (Iterator i=instructorIds.iterator();i.hasNext();) {
            Long instructorId = (Long)i.next();
            DepartmentalInstructor instructor = new DepartmentalInstructorDAO().get(instructorId, hibSession);
            if (instructor!=null) instructors.add(instructor);
        }
        return instructors;
    }
}
