package org.unitime.timetable.solver.exam;

import java.io.Serializable;

import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.dao.ExamDAO;

public class ExamInfo implements Serializable, Comparable {
    protected String iExamLabel = null;
    protected Long iExamId = null;
    protected transient Exam iExam = null;
    
    public ExamInfo(net.sf.cpsolver.exam.model.Exam exam) {
        iExamId = exam.getId();
        iExamLabel = exam.getName();
    }

    public ExamInfo(Exam exam) {
        iExamId = exam.getUniqueId();
        iExamLabel = exam.getLabel();
        iExam = exam;
    }
    
    public Long getExamId() {
        return iExamId;
    }
    
    public Exam getExam() {
        if (iExam==null)
            iExam = new ExamDAO().get(iExamId);
        return iExam;
    }
    
    public String getExamName() {
        return (iExamLabel==null?getExam().getLabel():iExamLabel);
    }

    public int hashCode() {
        return getExamId().hashCode();
    }
    
    public boolean equals(Object o) {
        if (o==null || !(o instanceof ExamInfo)) return false;
        return ((ExamInfo)o).getExamId().equals(getExamId());
    }
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof ExamInfo)) return -1;
        ExamInfo a = (ExamInfo)o;
        int cmp = getExamName().compareTo(a.getExamName());
        if (cmp!=0) return cmp;
        return getExamId().compareTo(a.getExamId());
    }
    
    public String toString() {
        return getExamName();
    }
}
