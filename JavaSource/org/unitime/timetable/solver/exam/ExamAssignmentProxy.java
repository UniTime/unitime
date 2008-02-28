package org.unitime.timetable.solver.exam;

import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;

public interface ExamAssignmentProxy {
    public ExamAssignment getAssignment(long examId);
    public ExamAssignmentInfo getAssignmentInfo(long examId);
}
