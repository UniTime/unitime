package org.unitime.timetable.solver.exam;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Transaction;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.model.dao.LocationDAO;

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
        Collection exams = org.unitime.timetable.model.Exam.findAll(iSessionId);
        iProgress.setPhase("Saving solution...", exams.size());
        for (Iterator i=exams.iterator();i.hasNext();) {
            iProgress.incProgress();
            org.unitime.timetable.model.Exam exam = (org.unitime.timetable.model.Exam)i.next();
            exam.setAssignedPeriod(null);
            exam.getAssignedRooms().clear();
            Exam examVar = null;
            for (Enumeration e=getModel().variables().elements();e.hasMoreElements();) {
                Exam x = (Exam)e.nextElement();
                if (exam.getUniqueId().equals(x.getId())) { examVar=x;break; }
            }
            if (examVar==null) {
                iProgress.warn("Exam "+getExamLabel(exam)+" was not loaded.");
                continue;
            }
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
    }
}
