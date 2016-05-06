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

import java.util.Collection;
import java.util.Vector;

import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamProposedChange;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamSuggestionsInfo;


/**
 * @author Tomas Muller
 */
public interface ExamSolverProxy extends ExamAssignmentProxy, CommonSolverInterface {
    public Collection<ExamAssignmentInfo> getAssignedExams();
    public Collection<ExamInfo> getUnassignedExams();
    public Collection<ExamAssignmentInfo> getAssignedExams(Long subjectAreaId);
    public Collection<ExamInfo> getUnassignedExams(Long subjectAreaId);
    public Collection<ExamAssignmentInfo> getAssignedExamsOfRoom(Long roomId);
    public Collection<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId);
    
    public ExamAssignmentInfo getAssignment(Long examId, Long periodId, Collection<Long> roomIds);
    public String assign(ExamAssignment assignment);
    public String unassign(ExamInfo exam);
    public ExamProposedChange update(ExamProposedChange change);
    public Vector<ExamRoomInfo> getRooms(long examId, long periodId, ExamProposedChange change, int minRoomSize, int maxRoomSize, String filter, boolean allowConflicts);
    public Collection<ExamAssignmentInfo> getPeriods(long examId, ExamProposedChange change);
    public ExamSuggestionsInfo getSuggestions(long examId, ExamProposedChange change, String filter, int depth, int limit, long timeOut);
    
    public Collection<ExamAssignmentInfo[]> getChangesToInitial(Long subjectAreaId);
    public Collection<ExamAssignmentInfo[]> getChangesToBest(Long subjectAreaId);
    
    public ExamConflictStatisticsInfo getCbsInfo();
    public ExamConflictStatisticsInfo getCbsInfo(Long examId);
}
