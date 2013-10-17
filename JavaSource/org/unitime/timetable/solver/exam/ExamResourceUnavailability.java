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
package org.unitime.timetable.solver.exam;

import java.util.Set;

import net.sf.cpsolver.exam.model.ExamPeriod;

/**
 * @author Tomas Muller
 */
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
