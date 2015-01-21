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

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import org.cpsolver.ifs.util.DataProperties;
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
public interface ExamSolverProxy extends ExamAssignmentProxy {

    public String getHost();
    public String getUser();
    public void dispose();
    
    public void load(DataProperties properties);
    public void reload(DataProperties properties);
    public Date getLoadedDate();
    public void save();
    
    public void start();
    public boolean isRunning();
    public void stopSolver();
    public void restoreBest();
    public void saveBest();
    public void clear();
    public Map<String,String> currentSolutionInfo();
    public Map<String,String> bestSolutionInfo();
    public Map<String,String> statusSolutionInfo() throws Exception;
    public boolean isWorking();

    public DataProperties getProperties();
    public void setProperties(DataProperties properties);

    public int getDebugLevel();
    public void setDebugLevel(int level);

    public Map getProgress();
    public String getLog();
    public String getLog(int level, boolean includeDate);
    public String getLog(int level, boolean includeDate, String fromStage);
    
    public boolean backup(File folder, String ownerId);
    public boolean restore(File folder, String ownerId);
    public boolean restore(File folder, String ownerId, boolean removeFiles);
    
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
    
    public long timeFromLastUsed();
    public boolean isPassivated();
    public boolean activateIfNeeded();
    public boolean passivate(File folder, String puid);
    public boolean passivateIfNeeded(File folder, String puid);
    public Date getLastUsed();
    
    public void interrupt();
    
    public byte[] exportXml() throws Exception;
}
