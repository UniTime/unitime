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
import org.unitime.timetable.webutil.timegrid.TimetableGridContext;


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
	public Vector getTimetableGridTables(TimetableGridContext context) throws Exception;
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
	
	public RoomReport getRoomReport(BitSet sessionDays, int startDayDayOfWeek, Long roomType, Float nrWeeks) throws Exception;
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
