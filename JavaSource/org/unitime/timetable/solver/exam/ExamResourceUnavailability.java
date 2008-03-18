package org.unitime.timetable.solver.exam;

import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.exam.model.ExamPeriod;

public class ExamResourceUnavailability {
    protected ExamPeriod iPeriod;
    protected Long iId;
    protected String iType;
    protected String iName;
    protected String iTime;
    protected String iRoom;
    protected Set<Long> iStudentIds = new HashSet<Long>();
    protected Set<Long> iInstructorIds = new HashSet<Long>();
    
    public ExamResourceUnavailability(ExamPeriod period, Long id, String type, String name, String time, String room) {
        iPeriod = period;
        iId = id;
        iType = type;
        iName = name;
        iTime = time;
        iRoom = room;
    }
    
    public ExamPeriod getPeriod() { return iPeriod; }
    public Long getId() { return iId; }
    public String getType() { return iType; }
    public String getName() { return iName; }
    public String getTime() { return iTime; }
    public String getRoom() { return iRoom; }
    public Set<Long> getStudentIds() { return iStudentIds; }
    public Set<Long> getInstructorIds() { return iInstructorIds; }
}
