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

update solver_parameter_def set
	description = 'Number of instructors which are teaching a class which is placed to a different location than initial'
	where name = 'Perturbations.AffectedInstructorWeight';
update solver_parameter_def set
	default_value = '-1'
	where name = 'SimulatedAnnealing.InitialTemperature' and default_value = '1.5';
update solver_parameter_def set
	default_value = '100.0'
	where name = 'Placement.NrConflictsWeight1' and default_value = '1.0';
update solver_parameter_def set
	default_value = '200.0'
	where name = 'Placement.WeightedConflictsWeight1' and default_value = '2.0';
update solver_parameter_def set
	type = 'boolean'
	where name = 'Precedence.ConsiderDatePatterns' and type = 'text';

do $$
declare
  id bigint;
  ggen bigint;
  ogen bigint;
begin
  select 32767 * next_hi into id from hibernate_unique_key;
  select uniqueid into ggen from solver_parameter_group where name='General';
  select max(ord) into ogen from solver_parameter_def where solver_param_group_id=(select uniqueid from solver_parameter_group where name='General');
  insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id +  0, 'Search.MaxIdleIterations', '1000', 'Maximum number of non-improving iterations after which the construction phase is halted (-1 to never halt)', 'integer', @ogen + 1, true, @ggen),
	(@id +  1, 'Search.MinConstructionTime', '10%', 'Minimum construction time when a complete solution cannot be reached', 'text', @ogen + 2, true, @ggen),
	(@id +  2, 'Parallel.NrSolvers', '1', 'Number of parallel solver threads', 'integer', @ogen + 3, true, @ggen),
	(@id +  3, 'ForwardCheck.MaxDepth', '2', 'Forward Checking: Max Depth', 'integer', @ogen + 4, true, @ggen),
	(@id +  4, 'ForwardCheck.MaxDomainSize', '1000', 'Forward Checking: Max Domain Size', 'integer', @ogen + 5, true, @ggen),
	(@id +  5, 'General.AutomaticInstructorConstraints', '', 'Automatic instructor constraints (separated by comma)', 'text', @ogen + 6, true, @ggen),
	(@id +  6, 'General.LoadCommittedReservations', 'false', 'Load: Consider Reservations for Committed Classes', 'boolean', @ogen + 7, true, @ggen),
	(@id +  7, 'General.ApplyInstructorDistributionsAcrossAllDepartments', 'false', 'Load: Apply Instructor Distributions Across All Departments', 'boolean', @ogen + 8, true, @ggen),
	(@id +  8, 'General.StudentGroupCourseDemands', 'false', 'Create additional student demands based on student group reservations', 'boolean', @ogen + 9, true, @ggen),
	(@id +  9, 'General.MPP.FixedTimes', 'false', 'MPP: Fix All Assigned Times', 'boolean', @ogen + 10, true, @ggen),
	(@id + 10, 'General.WeakenDistributions','false', 'Weaken Hard Distributions (useful for fixed-time MPP)', 'boolean', @ogen + 11, true, @ggen),
	(@id + 11, 'General.SoftInstructorConstraints', 'false', 'Soft Instructor Constraints (useful for fixed-time MPP)', 'boolean', @ogen + 12, true, @ggen),
	(@id + 12, 'General.AllowProhibitedRooms', 'false', 'Allow Prohibited Rooms (useful for fixed-time MPP)', 'boolean', @ogen + 13, true, @ggen);
  update hibernate_unique_key set next_hi=next_hi+1;
end; $$;

delete from solver_parameter_def where uniqueid in (select d1.uniqueid from
    solver_parameter_def d1 inner join solver_parameter_group g1 on d1.solver_param_group_id = g1.uniqueid,
    solver_parameter_def d2 inner join solver_parameter_group g2 on d2.solver_param_group_id = g2.uniqueid
    where d2.uniqueid < d1.uniqueid and d1.name = d2.name and g1.param_type = g2.param_type);

/*
 * Update database version
 */
  
update application_config set value='271' where name='tmtbl.db.version';

commit;
