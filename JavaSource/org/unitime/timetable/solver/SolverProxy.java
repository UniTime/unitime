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
public interface SolverProxy extends ClassAssignmentProxy, CommonSolverInterface {
	
    public String getHost();
    public String getUser();

	public void load(DataProperties properties);
	public void reload(DataProperties properties);
	public void save(boolean createNewSolution, boolean commitSolution);
	public void finalSectioning();

	public String getNote();
	public void setNote(String note);
	public int getDebugLevel();
	public void setDebugLevel(int level);

	public SolverUnassignedClassesModel getUnassignedClassesModel(String prefix);
	public Vector getTimetableGridTables(TimetableGridContext context);
	public ClassAssignmentDetails getClassAssignmentDetails(Long classId, boolean includeConstraints);
	public Suggestions getSuggestions(SuggestionsModel model);
	public void assign(Collection hints);
	public Hashtable conflictInfo(Collection hints);
	public PropertiesInfo getGlobalInfo();
	public ConflictStatisticsInfo getCbsInfo();
	public ConflictStatisticsInfo getCbsInfo(Long classId);
	
	public AssignmentPreferenceInfo getInfo(Hint hint);
	public String getNotValidReason(Hint hint);
	public Vector getAssignmentRecords();
	public Vector getChangesToInitial();
	public Vector getChangesToBest();
	public Vector getChangesToSolution(Long solutionId);
	public Vector getAssignedClasses();
	public Vector getAssignedClasses(String prefix);
	
	public void dispose();
	
	public RoomReport getRoomReport(BitSet sessionDays, int startDayDayOfWeek, Long roomType, Float nrWeeks);
	public DeptBalancingReport getDeptBalancingReport();
	public ViolatedDistrPreferencesReport getViolatedDistrPreferencesReport();
	public DiscouragedInstructorBtbReport getDiscouragedInstructorBtbReport();
	public StudentConflictsReport getStudentConflictsReport();
	public SameSubpartBalancingReport getSameSubpartBalancingReport();
	public PerturbationReport getPerturbationReport();
	public CSVFile export(boolean useAmPm);
	
	public Set getDepartmentIds();

	public long timeFromLastUsed();
	public boolean isPassivated();
	public boolean activateIfNeeded();
	public boolean passivate(File folder, String puid);
	public boolean passivateIfNeeded(File folder, String puid);
	public Date getLastUsed();
    
    public Hashtable getAssignmentTable2(Collection classesOrClassIds);
    public Hashtable getAssignmentInfoTable2(Collection classesOrClassIds);
    
    public void interrupt();
    
    public boolean backup(File folder, String puid);
    public boolean restore(File folder, String puid);
    
    public boolean hasFinalSectioning();
}
