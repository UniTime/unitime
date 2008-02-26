package org.unitime.timetable.solver.exam;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.timetable.model.ExamConflict;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.StudentDAO;

import net.sf.cpsolver.exam.model.Exam;
import net.sf.cpsolver.exam.model.ExamPlacement;
import net.sf.cpsolver.exam.model.ExamRoom;
import net.sf.cpsolver.ifs.solver.Solver;
import net.sf.cpsolver.ifs.util.Progress;

public class ExamDatabaseSaver extends ExamSaver {
    private static Log sLog = LogFactory.getLog(ExamDatabaseLoader.class);
    private Long iSessionId;
    private Progress iProgress = null;

    public ExamDatabaseSaver(Solver solver) {
        super(solver);
        iProgress = Progress.getInstance(getModel());
        iSessionId = getModel().getProperties().getPropertyLong("General.SessionId",(Long)null);
    }
    
    public void save() {
        iProgress.setStatus("Saving solution ...");
        org.hibernate.Session hibSession = new ExamDAO().getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            saveSolution(hibSession);
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            iProgress.fatal("Unable to save a solution, reason: "+e.getMessage(),e);
        }
    }
    
    private String getExamLabel(org.unitime.timetable.model.Exam exam) {
        return "<A href='examDetail.do?examId="+exam.getUniqueId()+"'>"+exam.getLabel()+"</A>";
    }

    protected void saveSolution(org.hibernate.Session hibSession) {
        /*
        hibSession.createQuery(
                "delete ExamConflict c where c.uniqueId in "+
                "(select c.uniqueId from Exam x inner join x.conflicts c where x.session.uniqueId=:sessionId)")
            .setLong("sessionId", iSessionId)
            .executeUpdate();
            */
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId);
        iProgress.setPhase("Saving assignments...", exams.size());
        Hashtable examTable = new Hashtable();
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            exam.setAssignedPeriod(null);
            exam.getAssignedRooms().clear();
            for (Iterator j=exam.getConflicts().iterator();j.hasNext();) {
                ExamConflict conf = (ExamConflict)j.next();
                hibSession.delete(conf);
                j.remove();
            }
            Exam examVar = null;
            for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
                Exam x = (Exam)e.nextElement();
                if (exam.getUniqueId().equals(x.getId())) { examVar=x;break; }
            }
            if (examVar==null) {
                iProgress.warn("Exam "+getExamLabel(exam)+" was not loaded.");
                continue;
            }
            examTable.put(examVar.getId(), exam);
            ExamPlacement placement = (ExamPlacement)examVar.getAssignment();
            if (placement==null) {
                iProgress.warn("Exam "+getExamLabel(exam)+" has no assignment.");
                continue;
            }
            ExamPeriod period = new ExamPeriodDAO().get(placement.getPeriod().getId());
            if (period==null) {
                iProgress.warn("Examination period "+placement.getPeriod().getDayStr()+" "+placement.getPeriod().getTimeStr()+" not found.");
                continue;
            }
            exam.setAssignedPeriod(period);
            for (Iterator j=placement.getRooms().iterator();j.hasNext();) {
                ExamRoom room = (ExamRoom)j.next();
                Location location = new LocationDAO().get(room.getId());
                if (location==null) {
                    iProgress.warn("Location "+room.getName()+" (id:"+room.getId()+") not found.");
                    continue;
                }
                exam.getAssignedRooms().add(location);
            }
            hibSession.saveOrUpdate(exam);
        }
        iProgress.setPhase("Saving conflicts...", getModel().assignedVariables().size());
        for (Enumeration e=getModel().assignedVariables().elements();e.hasMoreElements();) {
            Exam examVar = (Exam)e.nextElement();
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)examTable.get(examVar.getId());
            if (exam==null) continue;
            ExamPlacement placement = (ExamPlacement)examVar.getAssignment();
            ExamAssignmentInfo info = new ExamAssignmentInfo(placement);
            for (Iterator i=info.getDirectConflicts().iterator();i.hasNext();) {
                ExamAssignmentInfo.DirectConflict dc = (ExamAssignmentInfo.DirectConflict)i.next();
                if (examVar.getId()<dc.getOtherExam().getExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(dc.getOtherExam().getExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+dc.getOtherExam().getExam().getExamName()+" (id:"+dc.getOtherExam().getExam().getExamId()+") not found.");
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
                if (examVar.getId()<btb.getOtherExam().getExam().getExamId().longValue()) {
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(btb.getOtherExam().getExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+btb.getOtherExam().getExam().getExamName()+" (id:"+btb.getOtherExam().getExam().getExamId()+") not found.");
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
                    if (examVar.getId()>=otherExamAsg.getExam().getExamId().longValue()) continue m2d;
                    org.unitime.timetable.model.Exam otherExam = (org.unitime.timetable.model.Exam)examTable.get(otherExamAsg.getExam().getExamId());
                    if (otherExam==null) {
                        iProgress.warn("Exam "+otherExamAsg.getExam().getExamName()+" (id:"+otherExamAsg.getExam().getExamId()+") not found.");
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
        }
        for (Iterator i=exams.iterator();i.hasNext();) {
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            if (!exam.getConflicts().isEmpty()) hibSession.saveOrUpdate(exam);
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
}
