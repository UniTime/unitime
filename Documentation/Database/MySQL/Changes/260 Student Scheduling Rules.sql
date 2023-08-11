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
	uniqueid decimal(20,0) primary key not null,
	ord decimal(10,0) not null,
	name varchar(255) not null,
	student_filter varchar(2048),
	initiative varchar(1024),
	term varchar(1024),
	first_year decimal(10,0),
	last_year decimal(10,0),
	instr_method varchar(2048),
	course_name varchar(2048),
	apply_filter int(1) not null,
	apply_online int(1) not null,
	apply_batch int(1) not null,
	admin_override int(1) not null,
	advisor_override int(1) not null
) engine = INNODB;

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
