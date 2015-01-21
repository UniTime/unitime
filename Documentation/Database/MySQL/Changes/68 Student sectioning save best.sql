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

update solver_parameter_def set default_value='0' where name='General.SaveBestUnassigned' and solver_param_group_id = 
	(select uniqueid from solver_parameter_group where name='StudentSct');

select 32767 * next_hi into @id from hibernate_unique_key;
select uniqueid into @gid from solver_parameter_group where name='StudentSctWeights';
select max(ord)+1 into @ord from solver_parameter_def where solver_param_group_id=@gid;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id, 'StudentWeights.ProjectedStudentWeight', '0.0100', 'Projected student request', 'double', @ord, 1, @gid);

update hibernate_unique_key set next_hi=next_hi+1;

update solver_parameter_def set
	type = 'enum(Initial,MPP,Projection)'
	where name = 'StudentSctBasic.Mode';

/**
 * Update database version
 */

update application_config set value='68' where name='tmtbl.db.version';

commit;
		