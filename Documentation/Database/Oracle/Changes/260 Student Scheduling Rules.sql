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
	uniqueid number(20,0) constraint nn_sched_rules_uniqueid not null,
	ord number(10,0) constraint nn_sched_rules_ord not null,
	name varchar2(255 char) constraint nn_sched_rules_name not null,
	student_filter varchar2(2048 char),
	initiative varchar2(1024 char),
	term varchar2(1024 char),
	first_year number(10,0),
	last_year number(10,0),
	instr_method varchar2(2048 char),
	course_name varchar2(2048 char),
	apply_filter number(1) constraint nn_sched_rules_filter not null,
	apply_online number(1) constraint nn_sched_rules_online not null,
	apply_batch number(1) constraint nn_sched_rules_batch not null,
	admin_override number(1) constraint nn_sched_rules_admin not null,
	advisor_override number(1) constraint nn_sched_rules_advisor not null
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
