/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

select 32767 * next_hi into @id from hibernate_unique_key;

select max(ord)+1 into @ord from solver_parameter_group;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type) values
			(@id, 'StudentSctBasic', 'Basic Parameters', @ord, 2),
			(@id+1, 'StudentSct', 'General Parameters', @ord+1, 2);

insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
			(@id+2, 'StudentSctBasic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, @id),
			(@id+3, 'StudentSctBasic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save and Unload)', 1, 1, @id),
			(@id+4, 'Termination.Class', 'net.sf.cpsolver.ifs.termination.GeneralTerminationCondition','Student sectioning termination class','text', 0, 0, @id+1),
			(@id+5, 'Termination.StopWhenComplete','true', 'Stop when a complete solution if found', 'boolean', 1, 1, @id+1),
			(@id+6, 'Termination.TimeOut','28800', 'Maximal solver time (in sec)', 'integer', 2, 1, @id+1),
			(@id+7, 'Comparator.Class', 'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator', 'Student sectioning solution comparator class', 'text', 3, 0, @id+1),
			(@id+8, 'Value.Class', 'net.sf.cpsolver.studentsct.heuristics.EnrollmentSelection',  'Student sectioning value selection class', 'text', 4, 0, @id+1),
			(@id+9, 'Value.WeightConflicts', '1.0', 'CBS weight', 'double', 5, 0, @id+1),
			(@id+10, 'Value.WeightNrAssignments', '0.0', 'Number of past assignments weight', 'double', 6, 0, @id+1),
			(@id+11, 'Variable.Class', 'net.sf.cpsolver.ifs.heuristics.GeneralVariableSelection', 'Student sectioning variable selection class', 'text', 7, 0, @id+1),
			(@id+12, 'Neighbour.Class', 'net.sf.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection', 'Student sectioning neighbour selection class', 'text', 8, 0, @id+1),
			(@id+13, 'General.SaveBestUnassigned', '-1', 'Save best even when no complete solution is found', 'integer', 9, 0, @id+1),
			(@id+14, 'StudentSct.StudentDist', 'true', 'Use student distance conflicts', 'boolean', 10, 1, @id+1),
			(@id+15, 'StudentSct.CBS', 'true', 'Use conflict-based statistics', 'boolean', 11, 1, @id+1),
			(@id+16, 'Load.IncludeCourseDemands', 'true', 'Load real student requests', 'boolean', 12, 0, @id+1),
			(@id+17, 'Load.IncludeLastLikeStudents', 'true', 'Load last-like  course demands', 'boolean', 13, 0, @id+1),
			(@id+18, 'SectionLimit.PreferDummyStudents', 'true', 'Section limit constraint: favour unassignment of last-like course requests', 'boolean', 14, 0, @id+1),
			(@id+19, 'Student.DummyStudentWeight', '0.01', 'Last-like student request weight', 'double', 15, 1, @id+1),
			(@id+20, 'Neighbour.BranchAndBoundMinimizePenalty', 'false', 'Branch&bound: If true, section penalties (instead of section values) are minimized', 'boolean',16, 0, @id+1),
			(@id+21, 'Neighbour.BranchAndBoundTimeout', '5000','Branch&bound: Timeout for each neighbour selection (in milliseconds)', 'integer',17, 1, @id+1),
			(@id+22, 'Neighbour.RandomUnassignmentProb','0.5','Random Unassignment: Probability of a random selection of a student','double',18,1,@id+1),
			(@id+23, 'Neighbour.RandomUnassignmentOfProblemStudentProb','0.9','Random Unassignment: Probability of a random selection of a problematic student','double',19,1,@id+1),			
			(@id+24, 'Neighbour.SwapStudentsTimeout', '5000', 'Student Swap: Timeout for each neighbour selection (in milliseconds)','integer',20,1,@id+1),
			(@id+25, 'Neighbour.SwapStudentsMaxValues', '100', 'Student Swap: Limit for the number of considered values for each course request', 'integer', 21, 1, @id+1),
			(@id+26, 'Neighbour.MaxValues', '100', 'Backtrack: Limit on the number of enrollments to be visited of each course request', 'integer', 22, 1, @id+1),
			(@id+27, 'Neighbour.BackTrackTimeout', '5000', 'Backtrack: Timeout for each neighbour selection (in milliseconds)','integer',23,1,@id+1),
			(@id+28, 'Neighbour.BackTrackDepth', '4', 'Backtrack: Search depth','integer',24,1,@id+1),
			(@id+29, 'CourseRequest.SameTimePrecise', 'true', 'More precise (but slower) computation of enrollments of a course request while skipping enrollments with the same times', 'boolean', 25, 0, @id+1);
			
insert into solver_predef_setting (uniqueid, name, description, appearance) values 
			(@id+30, 'StudentSct.Default', 'Default', 3);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='31' where name='tmtbl.db.version';

commit;
