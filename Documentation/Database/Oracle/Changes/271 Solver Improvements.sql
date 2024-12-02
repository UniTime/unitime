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

insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'Search.MaxIdleIterations' as name,
	'1000' as default_value,
	'Maximum number of non-improving iterations after which the construction phase is halted (-1 to never halt)' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'Search.MinConstructionTime' as name,
	'10%' as default_value,
	'Minimum construction time when a complete solution cannot be reached' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'Parallel.NrSolvers' as name,
	'1' as default_value,
	'Number of parallel solver threads' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'ForwardCheck.MaxDepth' as name,
	'2' as default_value,
	'Forward Checking: Max Depth' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'ForwardCheck.MaxDomainSize' as name,
	'1000' as default_value,
	'Forward Checking: Max Domain Size' as description,
	'integer' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.AutomaticInstructorConstraints' as name,
	'' as default_value,
	'Automatic instructor constraints (separated by comma)' as description,
	'text' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.LoadCommittedReservations' as name,
	'false' as default_value,
	'Load: Consider Reservations for Committed Classes' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.ApplyInstructorDistributionsAcrossAllDepartments' as name,
	'false' as default_value,
	'Load: Apply Instructor Distributions Across All Departments' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.StudentGroupCourseDemands' as name,
	'false' as default_value,
	'Create additional student demands based on student group reservations' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.MPP.FixedTimes' as name,
	'false' as default_value,
	'MPP: Fix All Assigned Times' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.WeakenDistributions' as name,
	'false' as default_value,
	'Weaken Hard Distributions (useful for fixed-time MPP)' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.SoftInstructorConstraints' as name,
	'' as default_value,
	'Soft Instructor Constraints (useful for fixed-time MPP)' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
insert into solver_parameter_def (select
	solver_parameter_def_seq.nextval as uniqueid,
	'General.AllowProhibitedRooms' as name,
	'' as default_value,
	'Allow Prohibited Rooms (useful for fixed-time MPP)' as description,
	'boolean' as type,
	(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'General') as ord,
	1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name = 'General');
	

delete from solver_parameter_def where uniqueid in (select d1.uniqueid from
    solver_parameter_def d1 inner join solver_parameter_group g1 on d1.solver_param_group_id = g1.uniqueid,
    solver_parameter_def d2 inner join solver_parameter_group g2 on d2.solver_param_group_id = g2.uniqueid
    where d2.uniqueid < d1.uniqueid and d1.name = d2.name and g1.param_type = g2.param_type);

/*
 * Update database version
 */
  
update application_config set value='271' where name='tmtbl.db.version';

commit;
