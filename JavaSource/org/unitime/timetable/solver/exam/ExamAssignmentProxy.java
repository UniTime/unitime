package org.unitime.timetable.solver.exam;

public interface ExamAssignmentProxy {
    public ExamAssignment getAssignment(long examId);
    public ExamAssignmentInfo getAssignmentInfo(long examId);
}
