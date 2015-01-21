/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.solver.exam;

import java.util.Set;

import org.cpsolver.exam.model.ExamPeriod;


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
