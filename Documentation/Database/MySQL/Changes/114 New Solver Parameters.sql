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

select 32767 * next_hi into @id from hibernate_unique_key;
select uniqueid into @gdist from solver_parameter_group where name='Distance';
select max(ord) into @odist from solver_parameter_def where solver_param_group_id=@gdist;
select uniqueid into @ggen from solver_parameter_group where name='General';
select max(ord) into @ogen from solver_parameter_def where solver_param_group_id=@ggen;
select uniqueid into @gcmp from solver_parameter_group where name='Comparator';
select max(ord) into @ocmp from solver_parameter_def where solver_param_group_id=@gcmp;
select uniqueid into @gval from solver_parameter_group where name='Value';
select max(ord) into @oval from solver_parameter_def where solver_param_group_id=@gval;
select uniqueid into @gxw from solver_parameter_group where name='ExamWeights';
select max(ord) into @oxw from solver_parameter_def where solver_param_group_id=@gwx;
select uniqueid into @gex from solver_parameter_group where name='Exam';
select max(ord) into @oxe from solver_parameter_def where solver_param_group_id=@gex;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id + 0, 'Distances.ComputeDistanceConflictsBetweenNonBTBClasses', 'false', 'Compute Distance Conflicts Between Non BTB Classes', 'boolean', @odist, 1, @gdist),
	(@id + 1, 'Instructor.InstructorLongTravelInMinutes', '30.0', 'Instructor Long Travel in Minutes (only when computing distances between non-BTB classes is enabled)', 'double', @odist + 1, 1, @gdist),
	(@id + 2, 'General.AutoPrecedence', 'Neutral', 'Automatic precedence constraint', 'enum(Required,Strongly Preferred,Preferred,Neutral)', @ogen, 1, @ggen),
	(@id + 3, 'DiscouragedRoom.Unassignments2Weaken', '1000', 'Number of unassignments for the discouraged room constraint to weaken', 'integer', @ogen + 1, 1, @ggen),
	(@id + 4, 'CurriculaCourseDemands.IncludeOtherStudents', 'true', 'Curriculum Course Demands: Include Other Students', 'boolean', @ogen + 2, 1, @ggen),
	(@id + 5, 'General.AdditionalCriteria', 'net.sf.cpsolver.coursett.criteria.additional.ImportantStudentConflict;net.sf.cpsolver.coursett.criteria.additional.ImportantStudentHardConflict', 'Additional Criteria (semicolon separated list of class names)', 'text', @ogen + 3, 0, @ggen),
	(@id + 6, 'General.PurgeInvalidPlacements', 'true', 'Purge invalid placements during the data load', 'boolean', @ogen + 4, 0, @ggen),
	(@id + 7, 'CurriculumEnrollmentPriority.GroupMatch', '.*', 'Important Curriculum Groups (regexp matching the group name -- all courses of a matching group are marked as important)', 'text', @ogen + 5, 0, @ggen),
	(@id + 8, 'Precedence.ConsiderDatePatterns', 'true', 'Precedence Constraint: consider date patterns', 'text', @ogen + 6, 1, @ggen),
	(@id + 9, 'General.JenrlMaxConflicts', '1.0', 'Joint Enrollment Constraint: conflict limit (% limit of the smaller class)', 'double', @ogen + 7, 1, @ggen),
	(@id +10, 'General.JenrlMaxConflictsWeaken', '0.001', 'Joint Enrollment Constraint: limit weakening', 'double', @ogen + 8, 1, @ggen),
	(@id +11, 'Comparator.ImportantStudentConflictWeight', '0.0', 'Weight of important student conflict', 'double', @ocmp, 1, @gcmp),
	(@id +12, 'Comparator.ImportantHardStudentConflictWeight', '0.0', 'Weight of important hard student conflict', 'double', @ocmp + 1, 1, @gcmp),
	(@id +13, 'Placement.NrImportantStudConfsWeight1', '0.0', 'Important student conflict weight (level 1)', 'double', @oval, 1, @gval),
	(@id +14, 'Placement.NrImportantStudConfsWeight2', '%Comparator.ImportantStudentConflictWeight%', 'Important student conflict weight (level 2)', 'double', @oval + 1, 0, @gval),
	(@id +15, 'Placement.NrImportantStudConfsWeight3', '0.0', 'Important student conflict weight (level 3)', 'double', @oval + 2, 0, @gval),
	(@id +16, 'Placement.NrImportantHardStudConfsWeight1', '0.0', 'Important hard student conflict weight (level 1)', 'double', @oval + 3, 1, @gval),
	(@id +17, 'Placement.NrImportantHardStudConfsWeight2', '%Comparator.ImportantHardStudentConflictWeight%', 'Important hard student conflict weight (level 2)', 'double', @oval + 4, 0, @gval),
	(@id +18, 'Placement.NrImportantHardStudConfsWeight3', '0.0', 'Important hard student conflict weight (level 3)', 'double', @oval + 5, 0, @gval),
	(@id +19, 'Exams.RoomSizeFactor', '1.0', 'Excessive room size factor', 'double', @owx, 1, @gwx),
	(@id +20, 'Exams.DistanceToStronglyPreferredRoomWeight', '0.0', 'Distance to strongly preferred room weight', 'double', @owx + 1, 1, @gwx),
	(@id +21, 'Exams.AdditionalCriteria', 'net.sf.cpsolver.exam.criteria.additional.DistanceToStronglyPreferredRoom', 'Additional Criteria (semicolon separated list of class names)', 'text', @oex, 1, @gex);
		
update hibernate_unique_key set next_hi=next_hi+1;

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
