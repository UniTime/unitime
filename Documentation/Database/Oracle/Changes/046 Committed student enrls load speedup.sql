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

/**
 * Add a new index
 */

create index idx_student_enrl_assignment on student_enrl(solution_id,class_id);

/**
 * Add new solver parameters
 */

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.IgnoreCommittedStudentConflicts' as name, 'false' as default_vale, 'Do not load committed student conflicts' as description,
	'boolean' as type, 13 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.WeightStudents' as name, 'true' as default_vale, 'Weight last-like students' as description,
	'boolean' as type, 14 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

/*
 * Update database version
 */

update application_config set value='46' where name='tmtbl.db.version';

commit;
