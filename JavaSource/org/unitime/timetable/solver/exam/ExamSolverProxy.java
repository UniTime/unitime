/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.exam;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamProposedChange;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;
import org.unitime.timetable.solver.exam.ui.ExamSuggestionsInfo;

import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public interface ExamSolverProxy extends ExamAssignmentProxy {

    public String getHost();
    public String getHostLabel();
    public void dispose();
    
    public void load(DataProperties properties);
    public void reload(DataProperties properties);
    public Date getLoadedDate();
    public void save();
    public Date getLastUsed();
    
    public void start();
    public boolean isRunning();
    public void stopSolver();
    public void restoreBest();
    public void saveBest();
    public void clear();
    public Hashtable currentSolutionInfo();
    public Hashtable bestSolutionInfo();
    public boolean isWorking();

    public DataProperties getProperties();
    public void setProperties(DataProperties properties);

    public String getNote();
    public void setNote(String note);
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
}
