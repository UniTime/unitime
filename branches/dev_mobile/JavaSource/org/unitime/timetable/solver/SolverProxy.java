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
package org.unitime.timetable.solver;

import java.io.File;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.Hint;
import org.unitime.timetable.solver.interactive.Suggestions;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;
import org.unitime.timetable.solver.ui.DeptBalancingReport;
import org.unitime.timetable.solver.ui.DiscouragedInstructorBtbReport;
import org.unitime.timetable.solver.ui.PerturbationReport;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.solver.ui.RoomReport;
import org.unitime.timetable.solver.ui.SameSubpartBalancingReport;
import org.unitime.timetable.solver.ui.SolverUnassignedClassesModel;
import org.unitime.timetable.solver.ui.StudentConflictsReport;
import org.unitime.timetable.solver.ui.ViolatedDistrPreferencesReport;


/**
 * @author Tomas Muller
 */
public interface SolverProxy extends ClassAssignmentProxy {
	
    public String getHost();
    public String getUser();

	public void load(DataProperties properties) throws Exception;
	public void reload(DataProperties properties) throws Exception;
	public Date getLoadedDate() throws Exception;
	public void save(boolean createNewSolution, boolean commitSolution) throws Exception;
	public void finalSectioning() throws Exception;

	public void start() throws Exception;
	public boolean isRunning() throws Exception;
	public void stopSolver() throws Exception;
	public void restoreBest() throws Exception;
	public void saveBest() throws Exception;
	public Map<String,String> currentSolutionInfo() throws Exception;
	public Map<String,String> bestSolutionInfo() throws Exception;
    public Map<String,String> statusSolutionInfo() throws Exception;
	public boolean isWorking() throws Exception;

	public DataProperties getProperties();
	public void setProperties(DataProperties properties) throws Exception;

	public String getNote() throws Exception;
	public void setNote(String note) throws Exception;
	public int getDebugLevel() throws Exception;
	public void setDebugLevel(int level) throws Exception;

	public Map getProgress();
	public String getLog() throws Exception;
	public String getLog(int level, boolean includeDate) throws Exception;
	public String getLog(int level, boolean includeDate, String fromStage) throws Exception;
	public SolverUnassignedClassesModel getUnassignedClassesModel(String prefix) throws Exception;
	public Vector getTimetableGridTables(String findString, int resourceType, int startDay, int bgMode, boolean showEvents) throws Exception;
	public ClassAssignmentDetails getClassAssignmentDetails(Long classId, boolean includeConstraints) throws Exception;
	public Suggestions getSuggestions(SuggestionsModel model) throws Exception;
	public void assign(Collection hints) throws Exception;
	public Hashtable conflictInfo(Collection hints) throws Exception;
	public PropertiesInfo getGlobalInfo() throws Exception;
	public ConflictStatisticsInfo getCbsInfo() throws Exception;
	public ConflictStatisticsInfo getCbsInfo(Long classId) throws Exception;
	
	public AssignmentPreferenceInfo getInfo(Hint hint) throws Exception;
	public String getNotValidReason(Hint hint) throws Exception;
	public Vector getAssignmentRecords() throws Exception;
	public Vector getChangesToInitial() throws Exception;
	public Vector getChangesToBest() throws Exception;
	public Vector getChangesToSolution(Long solutionId) throws Exception;
	public Vector getAssignedClasses() throws Exception;
	public Vector getAssignedClasses(String prefix) throws Exception;
	
	public void dispose() throws Exception;
	
	public RoomReport getRoomReport(BitSet sessionDays, int startDayDayOfWeek, Long roomType) throws Exception;
	public DeptBalancingReport getDeptBalancingReport() throws Exception;
	public ViolatedDistrPreferencesReport getViolatedDistrPreferencesReport() throws Exception;
	public DiscouragedInstructorBtbReport getDiscouragedInstructorBtbReport() throws Exception;
	public StudentConflictsReport getStudentConflictsReport() throws Exception;
	public SameSubpartBalancingReport getSameSubpartBalancingReport() throws Exception;
	public PerturbationReport getPerturbationReport() throws Exception;
	public CSVFile export(boolean useAmPm) throws Exception;
	
	public Set getDepartmentIds() throws Exception;

	public long timeFromLastUsed();
	public boolean isPassivated();
	public boolean activateIfNeeded();
	public boolean passivate(File folder, String puid);
	public boolean passivateIfNeeded(File folder, String puid);
	public Date getLastUsed();
    
    public Hashtable getAssignmentTable2(Collection classesOrClassIds) throws Exception;
    public Hashtable getAssignmentInfoTable2(Collection classesOrClassIds) throws Exception;
    
    public void interrupt();
    
    public byte[] exportXml() throws Exception;
    
    public boolean backup(File folder, String puid);
    public boolean restore(File folder, String puid);
    
    public boolean hasFinalSectioning();
}
