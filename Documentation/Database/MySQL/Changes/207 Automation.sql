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

create table task (
	uniqueid decimal(20,0) primary key not null,
	name varchar(128) not null,
	session_id decimal(20,0) not null,
	script_id decimal(20,0) not null,
	owner_id decimal(20,0) not null,
	email varchar(1000) null,
	input_file longblob null
) engine = INNODB;

alter table task add constraint fk_task_sesssion foreign key (session_id) references sessions (uniqueid) on delete cascade;
alter table task add constraint fk_task_script foreign key (script_id) references script (uniqueid) on delete cascade;
alter table task add constraint fk_task_owner foreign key (owner_id) references timetable_manager (uniqueid) on delete cascade;

create table task_parameter (
	task_id decimal(20,0) not null,
	name varchar(128) not null,
	value varchar(2048),
	primary key (task_id, name)
) engine = INNODB;

alter table task_parameter add constraint fk_taskparam_task foreign key (task_id) references task (uniqueid) on delete cascade;

create table task_execution (
	uniqueid decimal(20,0) primary key not null,
	task_id decimal(20,0) not null,
	exec_date bigint(10) not null,
	exec_period bigint(10) not null,
	status bigint(10) not null,
	created_date datetime not null,
	scheduled_date datetime not null,
	queued_date datetime null,
	started_date datetime null,
	finished_date datetime null,
	log_file longtext binary null,
	output_file longblob null,
	output_name varchar(260) null,
	output_content varchar(260) null,
	status_message varchar(200) null
) engine = INNODB;

alter table task_execution add constraint fk_taskexec_task foreign key (task_id) references task (uniqueid) on delete cascade;

create index idx_taskexe_schdstatus on task_execution(status, scheduled_date);

insert into rights (role_id, value)
	select distinct r.role_id, 'Tasks'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Scripts';

insert into rights (role_id, value)
	select distinct r.role_id, 'TaskDetail'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Scripts';

insert into rights (role_id, value)
	select distinct r.role_id, 'TaskEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'ScriptEdit';

/*
 * Update database version
 */

update application_config set value='207' where name='tmtbl.db.version';

commit;
