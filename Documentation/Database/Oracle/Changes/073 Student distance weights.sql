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

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Comparator.DistStudentConflictWeight' as name,
	'0.2' as default_vale,
	'Weight of distance student conflict' as description,
	'double' as type,
	12 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Comparator');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Lecture.DistStudentConflictWeight' as name,
	'%Comparator.DistStudentConflictWeight%' as default_vale,
	'Distance student conflict weight' as description,
	'double' as type,
	20 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Variable');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight1' as name,
	'0.05' as default_vale,
	'Distance student conflict weight (level 1)' as description,
	'double' as type,
	58 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight2' as name,
	'%Comparator.DistStudentConflictWeight%' as default_vale,
	'Distance student conflict weight (level 2)' as description,
	'double' as type,
	59 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight3' as name,
	'0.0' as default_vale,
	'Distance student conflict weight (level 3)' as description,
	'double' as type,
	60 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter (select solver_parameter_seq.nextval as uniqueid,
	'0.0' as value,
	d.uniqueid as solver_param_def_id,
	null as solution_id,
	s.uniqueid as solver_predef_setting_id 
	from solver_parameter_def d, solver_predef_setting s
	where d.name = 'Comparator.DistStudentConflictWeight' and s.name='Default.Check');

insert into solver_parameter (select solver_parameter_seq.nextval as uniqueid,
	'0.0' as value,
	d.uniqueid as solver_param_def_id,
	null as solution_id,
	s.uniqueid as solver_predef_setting_id 
	from solver_parameter_def d, solver_predef_setting s
	where d.name = 'Comparator.DistStudentConflictWeight' and s.name='Default.Check');
/*
 * Update database version
 */

update application_config set value='73' where name='tmtbl.db.version';

commit;
