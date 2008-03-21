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

import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfo;
import org.unitime.timetable.solver.exam.ui.ExamRoomInfo;

import net.sf.cpsolver.ifs.util.DataProperties;

/**
 * @author Tomas Muller
 */
public interface ExamSolverProxy extends ExamAssignmentProxy {

    public String getHost();
    public String getHostLabel();
    public void dispose() throws Exception;
    
    public void load(DataProperties properties) throws Exception;
    public void reload(DataProperties properties) throws Exception;
    public Date getLoadedDate() throws Exception;
    public void save() throws Exception;
    
    public void start() throws Exception;
    public boolean isRunning() throws Exception;
    public void stopSolver() throws Exception;
    public void restoreBest() throws Exception;
    public void saveBest() throws Exception;
    public void clear() throws Exception;
    public Hashtable currentSolutionInfo() throws Exception;
    public Hashtable bestSolutionInfo() throws Exception;
    public boolean isWorking() throws Exception;

    public DataProperties getProperties() throws Exception;
    public void setProperties(DataProperties properties) throws Exception;

    public String getNote() throws Exception;
    public void setNote(String note) throws Exception;
    public int getDebugLevel() throws Exception;
    public void setDebugLevel(int level) throws Exception;

    public Map getProgress() throws Exception;
    public String getLog() throws Exception;
    public String getLog(int level, boolean includeDate) throws Exception;
    public String getLog(int level, boolean includeDate, String fromStage) throws Exception;
    
    public boolean backup(File folder) throws Exception;
    public boolean restore(File folder) throws Exception;
    public boolean restore(File folder, boolean removeFiles) throws Exception;
    
    public Collection<ExamAssignmentInfo> getAssignedExams() throws Exception;
    public Collection<ExamInfo> getUnassignedExams() throws Exception;
    public Collection<ExamAssignmentInfo> getAssignedExams(Long subjectAreaId) throws Exception;
    public Collection<ExamInfo> getUnassignedExams(Long subjectAreaId) throws Exception;
    public Collection<ExamAssignmentInfo> getAssignedExamsOfRoom(Long roomId) throws Exception;
    public Collection<ExamAssignmentInfo> getAssignedExamsOfInstructor(Long instructorId) throws Exception;
    
    public Collection<ExamAssignmentInfo> getPeriods(long examId);
    public Collection<ExamRoomInfo> getRooms(long examId, long periodId);
    public ExamAssignmentInfo getAssignment(Long examId, Long periodId, Collection<Long> roomIds);
    public String assign(ExamAssignmentInfo assignment);
    
    public int getExamType();
    public Long getSessionId();
    
    public Collection<ExamAssignmentInfo[]> getChangesToInitial(Long subjectAreaId) throws Exception;
    public Collection<ExamAssignmentInfo[]> getChangesToBest(Long subjectAreaId) throws Exception;
}
