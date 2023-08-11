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

create table std_sched_rules (
	uniqueid bigint not null,
	ord integer not null,
	name varchar(255) not null,
	student_filter varchar(2048),
	initiative varchar(1024),
	term varchar(1024),
	first_year integer,
	last_year integer,
	instr_method varchar(2048),
	course_name varchar(2048),
	apply_filter boolean not null,
	apply_online boolean not null,
	apply_batch boolean not null,
	admin_override boolean not null,
	advisor_override boolean not null
);

alter table std_sched_rules add constraint pk_std_sched_rules primary key (uniqueid);

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingRules'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentSchedulingStatusTypes';
	
insert into rights (role_id, value)
	select distinct r.role_id, 'StudentSchedulingRuleEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentSchedulingStatusTypeEdit';
  
/*
 * Update database version
 */
  
update application_config set value='260' where name='tmtbl.db.version';

commit;
