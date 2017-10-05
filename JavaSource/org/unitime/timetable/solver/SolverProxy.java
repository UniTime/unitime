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

import java.util.BitSet;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.cpsolver.ifs.util.CSVFile;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.gwt.shared.SuggestionsInterface;
import org.unitime.timetable.gwt.shared.TimetableGridInterface.TimetableGridModel;
import org.unitime.timetable.server.solver.SuggestionsContext;
import org.unitime.timetable.solver.TimetableSolver.AssignmentRecord;
import org.unitime.timetable.solver.TimetableSolver.RecordedAssignment;
import org.unitime.timetable.solver.interactive.ClassAssignmentDetails;
import org.unitime.timetable.solver.interactive.Hint;
import org.unitime.timetable.solver.interactive.Suggestions;
import org.unitime.timetable.solver.interactive.SuggestionsModel;
import org.unitime.timetable.solver.ui.AssignmentPreferenceInfo;
import org.unitime.timetable.solver.ui.ConflictStatisticsInfo;
import org.unitime.timetable.solver.ui.DeptBalancingReport;
import org.unitime.timetable.solver.ui.DiscouragedInstructorBtbReport;
import org.unitime.timetable.solver.ui.PerturbationReport;
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
	public String getNote();
	public void setNote(String note);
	
	public void save(boolean createNewSolution, boolean commitSolution);

    public boolean hasFinalSectioning();
	public void finalSectioning();

	public SolverUnassignedClassesModel getUnassignedClassesModel(String... prefix);
	public Vector getTimetableGridTables(TimetableGridContext context);
	public List<TimetableGridModel> getTimetableGridTables(org.unitime.timetable.server.solver.TimetableGridContext context);
	public ClassAssignmentDetails getClassAssignmentDetails(Long classId, boolean includeConstraints);
	public Suggestions getSuggestions(SuggestionsModel model);
	public void assign(Collection hints);
	public Hashtable conflictInfo(Collection hints);
	public ConflictStatisticsInfo getCbsInfo();
	public ConflictStatisticsInfo getCbsInfo(Long classId);
	
	public AssignmentPreferenceInfo getInfo(Hint hint);
	public String getNotValidReason(Hint hint);
	public List<AssignmentRecord> getAssignmentRecords();
	public List<RecordedAssignment> getChangesToInitial();
	public List<RecordedAssignment> getChangesToBest();
	public List<RecordedAssignment> getChangesToSolution(Long solutionId);
	public List<ClassAssignmentDetails> getAssignedClasses(String... prefix);
	
	public RoomReport getRoomReport(BitSet sessionDays, int startDayDayOfWeek, Long roomType, Float nrWeeks);
	public DeptBalancingReport getDeptBalancingReport();
	public ViolatedDistrPreferencesReport getViolatedDistrPreferencesReport();
	public DiscouragedInstructorBtbReport getDiscouragedInstructorBtbReport();
	public StudentConflictsReport getStudentConflictsReport();
	public SameSubpartBalancingReport getSameSubpartBalancingReport();
	public PerturbationReport getPerturbationReport();
	public CSVFile export(boolean useAmPm);
	
	public Set getDepartmentIds();

    public Hashtable getAssignmentTable2(Collection classesOrClassIds);
    public Hashtable getAssignmentInfoTable2(Collection classesOrClassIds);
    
	public SuggestionsInterface.ClassAssignmentDetails getClassAssignmentDetails(SuggestionsContext context, Long classId, boolean includeDomain, boolean includeConstraints);
	public SuggestionsInterface.Suggestion getSelectedSuggestion(SuggestionsContext context, SuggestionsInterface.SelectedAssignmentsRequest request);
	public void assignSelectedAssignments(List<SuggestionsInterface.SelectedAssignment> assignments);
	public SuggestionsInterface.Suggestions computeSuggestions(SuggestionsContext context, SuggestionsInterface.ComputeSuggestionsRequest request);
	public List<SuggestionsInterface.ClassAssignmentDetails> computeConfTable(SuggestionsContext context, SuggestionsInterface.ComputeConflictTableRequest request);
	public Map<String, Collection<Entity>> loadSuggestionFilter(Long classId);
}
