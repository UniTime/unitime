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

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'StudentSctBasic', 'Basic Parameters', '', -1,2);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='StudentSctBasic';

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'StudentSctBasic.Mode' as NAME, 
		'Initial' as DEFAULT_VALUE, 
		'Solver mode' as DESCRIPTION, 
		'enum(Initial,MPP)' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSctBasic');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'StudentSctBasic.WhenFinished' as NAME, 
		'No Action' as DEFAULT_VALUE, 
		'When finished' as DESCRIPTION, 
		'enum(No Action,Save,Save and Unload)' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSctBasic');

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'StudentSct', 'General Parameters', '', -1,2);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='StudentSct';

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Termination.Class' as NAME, 
		'net.sf.cpsolver.ifs.termination.GeneralTerminationCondition' as DEFAULT_VALUE, 
		'Student sectioning termination class' as DESCRIPTION, 
		'text' as TYPE, 
		0 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Termination.StopWhenComplete' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Stop when a complete solution if found' as DESCRIPTION, 
		'boolean' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Termination.TimeOut' as NAME, 
		'28800' as DEFAULT_VALUE, 
		'Maximal solver time (in sec)' as DESCRIPTION, 
		'integer' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Comparator.Class' as NAME, 
		'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator' as DEFAULT_VALUE, 
		'Student sectioning solution comparator class' as DESCRIPTION, 
		'text' as TYPE, 
		3 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Value.Class' as NAME, 
		'net.sf.cpsolver.studentsct.heuristics.EnrollmentSelection' as DEFAULT_VALUE, 
		'Student sectioning value selection class' as DESCRIPTION, 
		'text' as TYPE, 
		4 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Value.WeightConflicts' as NAME, 
		'1.0' as DEFAULT_VALUE, 
		'CBS weight' as DESCRIPTION, 
		'double' as TYPE, 
		5 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Value.WeightNrAssignments' as NAME, 
		'0.0' as DEFAULT_VALUE, 
		'Number of past assignments weight' as DESCRIPTION, 
		'double' as TYPE, 
		6 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Variable.Class' as NAME, 
		'net.sf.cpsolver.ifs.heuristics.GeneralVariableSelection' as DEFAULT_VALUE, 
		'Student sectioning variable selection class' as DESCRIPTION, 
		'text' as TYPE, 
		7 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.Class' as NAME, 
		'net.sf.cpsolver.studentsct.heuristics.StudentSctNeighbourSelection' as DEFAULT_VALUE, 
		'Student sectioning neighbour selection class' as DESCRIPTION, 
		'text' as TYPE, 
		8 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'General.SaveBestUnassigned' as NAME, 
		'-1' as DEFAULT_VALUE, 
		'Save best even when no complete solution is found' as DESCRIPTION, 
		'integer' as TYPE, 
		9 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'StudentSct.StudentDist' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Use student distance conflicts' as DESCRIPTION, 
		'boolean' as TYPE, 
		10 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'StudentSct.CBS' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Use conflict-based statistics' as DESCRIPTION, 
		'boolean' as TYPE, 
		11 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Load.IncludeCourseDemands' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Load real student requests' as DESCRIPTION, 
		'boolean' as TYPE, 
		12 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Load.IncludeLastLikeStudents' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Load last-like  course demands' as DESCRIPTION, 
		'boolean' as TYPE, 
		13 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'SectionLimit.PreferDummyStudents' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Section limit constraint: favour unassignment of last-like course requests' as DESCRIPTION, 
		'boolean' as TYPE, 
		14 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Student.DummyStudentWeight' as NAME, 
		'0.01' as DEFAULT_VALUE, 
		'Last-like student request weight' as DESCRIPTION, 
		'double' as TYPE, 
		15 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.BranchAndBoundMinimizePenalty' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Branch&bound: If true, section penalties (instead of section values) are minimized' as DESCRIPTION, 
		'boolean' as TYPE, 
		16 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.BranchAndBoundTimeout' as NAME, 
		'5000' as DEFAULT_VALUE, 
		'Branch&bound: Timeout for each neighbour selection (in milliseconds)' as DESCRIPTION, 
		'integer' as TYPE, 
		17 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.RandomUnassignmentProb' as NAME, 
		'0.5' as DEFAULT_VALUE, 
		'Random Unassignment: Probability of a random selection of a student' as DESCRIPTION, 
		'double' as TYPE, 
		18 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.RandomUnassignmentOfProblemStudentProb' as NAME, 
		'0.9' as DEFAULT_VALUE, 
		'Random Unassignment: Probability of a random selection of a problematic student' as DESCRIPTION, 
		'double' as TYPE, 
		19 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.SwapStudentsTimeout' as NAME, 
		'5000' as DEFAULT_VALUE, 
		'Student Swap: Timeout for each neighbour selection (in milliseconds)' as DESCRIPTION, 
		'integer' as TYPE, 
		20 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.SwapStudentsMaxValues' as NAME, 
		'100' as DEFAULT_VALUE, 
		'Student Swap: Limit for the number of considered values for each course request' as DESCRIPTION, 
		'integer' as TYPE, 
		21 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.MaxValues' as NAME, 
		'100' as DEFAULT_VALUE, 
		'Backtrack: Limit on the number of enrollments to be visited of each course request' as DESCRIPTION, 
		'integer' as TYPE, 
		22 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.BackTrackTimeout' as NAME, 
		'5000' as DEFAULT_VALUE, 
		'Backtrack: Timeout for each neighbour selection (in milliseconds)' as DESCRIPTION, 
		'integer' as TYPE, 
		23 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.BackTrackDepth' as NAME, 
		'4' as DEFAULT_VALUE, 
		'Backtrack: Search depth' as DESCRIPTION, 
		'integer' as TYPE, 
		24 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'CourseRequest.SameTimePrecise' as NAME, 
		'true' as DEFAULT_VALUE, 
		'More precise (but slower) computation of enrollments of a course request while skipping enrollments with the same times' as DESCRIPTION, 
		'boolean' as TYPE, 
		25 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='StudentSct');

insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
	values(Solver_Predef_Setting_Seq.Nextval, 'StudentSct.Default', 'Default', 3);
			
/*
 * Update database version
 */

update application_config set value='31' where name='tmtbl.db.version';

commit;
