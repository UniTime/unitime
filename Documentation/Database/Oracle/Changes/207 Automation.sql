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
	uniqueid number(20,0) constraint nn_task_uniqueid not null,
	name varchar2(128 char) constraint nn_task_name not null,
	session_id number(20,0) constraint nn_task_session not null,
	script_id number(20,0) constraint nn_task_script not null,
	owner_id number(20,0) constraint nn_task_owner not null,
	email varchar2(1000 char),
	input_file blob
	);
alter table task add constraint pk_task primary key (uniqueid);

alter table task add constraint fk_task_sesssion foreign key (session_id) references sessions (uniqueid) on delete cascade;
alter table task add constraint fk_task_script foreign key (script_id) references script (uniqueid) on delete cascade;
alter table task add constraint fk_task_owner foreign key (owner_id) references timetable_manager (uniqueid) on delete cascade;

create table task_parameter (
	task_id number(20,0) constraint nn_taskparm_id not null,
	name varchar2(128 char) constraint nn_taskparm_name not null,
	value varchar2(2048 char)
	);
alter table task_parameter add constraint pk_task_parameter primary key (task_id, name);

alter table task_parameter add constraint fk_taskparam_task foreign key (task_id) references task (uniqueid) on delete cascade;

create table task_execution (
	uniqueid number(20,0) constraint nn_taskexe_uniqueid not null,
	task_id number(20,0) constraint nn_taskexe_task not null,
	exec_date number(10) constraint nn_taskexe_date not null,
	exec_period number(10) constraint nn_taskexe_period not null,
	status number(10) constraint nn_taskexe_status not null,
	created_date timestamp constraint nn_taskexe_created not null,
	scheduled_date timestamp constraint nn_taskexe_scheduled not null,
	queued_date timestamp,
	started_date timestamp,
	finished_date timestamp,
	log_file clob,
	output_file blob,
	output_name varchar2(260 char),
	output_content varchar2(260 char),
	status_message varchar2(200 char)
);
alter table task_execution add constraint pk_task_execution primary key (uniqueid);

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
