package org.unitime.timetable.solver.exam;

import java.util.Set;

import net.sf.cpsolver.exam.model.ExamPeriod;

public class ExamResourceUnavailability {
    protected ExamPeriod iPeriod;
    protected Long iId;
    protected String iType;
    protected String iName;
    protected String iDate;
    protected String iTime;
    protected String iRoom;
    protected int iSize;
    protected Set<Long> iStudentIds;
    protected Set<Long> iInstructorIds;
    
    public ExamResourceUnavailability(ExamPeriod period, Long id, String type, String name, String date, String time, String room, int size, Set<Long> studentIds, Set<Long> instructorIds) {
        iPeriod = period;
        iId = id;
        iType = type;
        iName = name;
        iDate = date;
        iTime = time;
        iRoom = room;
        iSize = size;
        iStudentIds = studentIds;
        iInstructorIds = instructorIds;
    }
    
    public ExamPeriod getPeriod() { return iPeriod; }
    public Long getId() { return iId; }
    public String getType() { return iType; }
    public String getName() { return iName; }
    public String getDate() { return iDate; }
    public String getTime() { return iTime; }
    public String getRoom() { return iRoom; }
    public int getSize() { return iSize; }
    public Set<Long> getStudentIds() { return iStudentIds; }
    public Set<Long> getInstructorIds() { return iInstructorIds; }
    protected void addRoom(String room) { iRoom += (iRoom.length()>0?", ":"")+room; }
}
