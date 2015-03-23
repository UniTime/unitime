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

select uniqueid into @gid from solver_parameter_group where name='General';

select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;
 
select 32767 * next_hi into @id from hibernate_unique_key;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+0, 'General.IgnoreCommittedStudentConflicts', 'false', 'Do not load committed student conflicts', 'boolean', @ord+1, 1, @gid),
	(@id+1, 'General.WeightStudents', 'true', 'Weight last-like students', 'boolean', @ord+2, 0, @gid);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='46' where name='tmtbl.db.version';

commit;
