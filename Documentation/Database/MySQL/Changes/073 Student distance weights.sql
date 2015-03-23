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

select uniqueid into @gcmp from solver_parameter_group where name='Comparator';
select uniqueid into @gvar from solver_parameter_group where name='Variable';
select uniqueid into @gval from solver_parameter_group where name='Value';
select max(ord)+1 into @ocmp from solver_parameter_group where name='Comparator';
select max(ord)+1 into @ovar from solver_parameter_group where name='Variable';
select max(ord)+1 into @oval from solver_parameter_group where name='Value';
select uniqueid into @sid from solver_predef_setting where name='Default.Check';

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+0, 'Comparator.DistStudentConflictWeight', '0.2', 'Weight of distance student conflict', 'double', @ocmp, 1, @gcmp),
	(@id+1, 'Lecture.DistStudentConflictWeight', '%Comparator.DistStudentConflictWeight%', 'Distance student conflict weight', 'double', @ovar, 0, @gvar),
	(@id+2, 'Placement.NrDistStudConfsWeight1', '0.05', 'Distance student conflict weight (level 1)', 'double', @oval, 1, @gval), 
	(@id+3, 'Placement.NrDistStudConfsWeight2', '%Comparator.DistStudentConflictWeight%', 'Distance student conflict weight (level 2)', 'double', @oval+1, 0, @gval), 
	(@id+4, 'Placement.NrDistStudConfsWeight3', '0.0', 'Distance student conflict weight (level 3)', 'double', @oval+2, 0, @gval);

insert into solver_parameter
	(uniqueid, value, solver_param_def_id, solution_id, solver_predef_setting_id) values
	(@id+5, '0.0', @id+0, null, @sid),
	(@id+6, '0.0', @id+2, null, @sid);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='73' where name='tmtbl.db.version';

commit;
