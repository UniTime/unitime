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

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Distances.ComputeDistanceConflictsBetweenNonBTBClasses' as name,
		'false' as default_value, 
		'Compute Distance Conflicts Between Non BTB Classes' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Distance') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Distance');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Instructor.InstructorLongTravelInMinutes' as name,
		'30.0' as default_value, 
		'Instructor Long Travel in Minutes (only when computing distances between non-BTB classes is enabled)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Distance') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Distance');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'General.AutoPrecedence' as name,
		'Neutral' as default_value, 
		'Automatic precedence constraint' as description,
		'enum(Required,Strongly Preferred,Preferred,Neutral)' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'DiscouragedRoom.Unassignments2Weaken' as name,
		'1000' as default_value, 
		'Number of unassignments for the discouraged room constraint to weaken' as description,
		'integer' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'CurriculaCourseDemands.IncludeOtherStudents' as name,
		'true' as default_value, 
		'Curriculum Course Demands: Include Other Students' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'General.AdditionalCriteria' as name,
		'net.sf.cpsolver.coursett.criteria.additional.ImportantStudentConflict;net.sf.cpsolver.coursett.criteria.additional.ImportantStudentHardConflict' as default_value, 
		'Additional Criteria (semicolon separated list of class names)' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'General.PurgeInvalidPlacements' as name,
		'true' as default_value, 
		'Purge invalid placements during the data load' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'CurriculumEnrollmentPriority.GroupMatch' as name,
		'.*' as default_value, 
		'Important Curriculum Groups (regexp matching the group name -- all courses of a matching group are marked as important)' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Precedence.ConsiderDatePatterns' as name,
		'true' as default_value, 
		'Precedence Constraint: consider date patterns' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'General.JenrlMaxConflicts' as name,
		'1.0' as default_value, 
		'Joint Enrollment Constraint: conflict limit (% limit of the smaller class)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'General.JenrlMaxConflictsWeaken' as name,
		'0.001' as default_value, 
		'Joint Enrollment Constraint: limit weakening' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Comparator.ImportantStudentConflictWeight' as name,
		'0.0' as default_value, 
		'Weight of important student conflict' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Comparator') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Comparator');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Comparator.ImportantHardStudentConflictWeight' as name,
		'0.0' as default_value, 
		'Weight of important hard student conflict' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Comparator') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Comparator');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantStudConfsWeight1' as name,
		'0.0' as default_value, 
		'Important student conflict weight (level1)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantStudConfsWeight2' as name,
		'%Comparator.ImportantStudentConflictWeight%' as default_value, 
		'Important student conflict weight (level2)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantStudConfsWeight3' as name,
		'0.0' as default_value, 
		'Important student conflict weight (level3)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantHardStudConfsWeight1' as name,
		'0.0' as default_value, 
		'Important hard student conflict weight (level 1)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantHardStudConfsWeight2' as name,
		'%Comparator.ImportantHardStudentConflictWeight%' as default_value, 
		'Important hard student conflict weight (level 2)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Placement.NrImportantHardStudConfsWeight3' as name,
		'0.0' as default_value, 
		'Important hard student conflict weight (level 3)' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Value') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Value');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Exams.RoomSizeFactor' as name,
		'1.10' as default_value, 
		'Excessive room size factor' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'ExamWeights') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'ExamWeights');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Exams.DistanceToStronglyPreferredRoomWeight' as name,
		'0.0001' as default_value, 
		'Distance to strongly preferred room weight' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'ExamWeights') as ord,
		1 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'ExamWeights');

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid, 
		'Exams.AdditionalCriteria' as name,
		'net.sf.cpsolver.exam.criteria.additional.DistanceToStronglyPreferredRoom' as default_value, 
		'Additional Criteria (semicolon separated list of class names)' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'Exam') as ord,
		0 as visible, 
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'Exam');

create table param_dupl as
	select d.uniqueid from solver_parameter_def d where
	(select count(*) from solver_parameter_def x where x.name = d.name and x.solver_param_group_id = d.solver_param_group_id and x.uniqueid < d.uniqueId) > 0;
delete from solver_parameter_def where uniqueid in (select x.uniqueid from param_dupl x);
drop table param_dupl;

create table param_ord_fix as 
	select d.uniqueid as param_id, (select count(*) from solver_parameter_def x where x.solver_param_group_id = d.solver_param_group_id and (x.ord < d.ord or (x.ord = d.ord and x.uniqueid < d.uniqueid))) as new_ord
	from solver_parameter_def d order by d.solver_param_group_id, d.ord, d.uniqueid;
update solver_parameter_def set ord = (select new_ord from param_ord_fix where param_id = uniqueid);
drop table param_ord_fix;

/*
 * Update database version
 */

update application_config set value='114' where name='tmtbl.db.version';

commit;
