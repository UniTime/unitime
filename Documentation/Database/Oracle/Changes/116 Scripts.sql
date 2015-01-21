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

create table script (
		uniqueid number(20,0) constraint nn_script_id not null,
		name varchar2(128 char) constraint nn_script_name not null,
		description varchar2(1024 char),
		engine varchar2(32 char) constraint nn_script_engine not null,
		permission varchar2(128 char),
		script clob
	);

alter table script add constraint pk_script primary key (uniqueid);

create table script_parameter (
		script_id number(20,0) constraint nn_script_param_id not null,
		name varchar2(128 char) constraint nn_script_param_name not null,
		label varchar2(256 char),
		type varchar2(2048 char) constraint nn_script_param_type not null,
		default_value varchar2(2048 char)
	);

alter table script_parameter add constraint pk_script_parameter primary key (script_id, name);

alter table script_parameter add constraint fk_script_parameter foreign key (script_id) references script(uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'ScriptEdit' from roles r, rights g where g.role_id = r.role_id and g.value like 'PermissionEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'Scripts' from roles r, rights g where g.role_id = r.role_id and g.value like 'Permissions';

/*
 * Update database version
 */

update application_config set value='116' where name='tmtbl.db.version';

commit;
