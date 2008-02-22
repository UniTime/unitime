package org.unitime.timetable.solver.exam;

import net.sf.cpsolver.exam.model.ExamPlacement;

public interface ExamAssignmentProxy {
    public ExamPlacement getPlacement(long examId) throws Exception;
}
