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

create table advisor (
	uniqueid decimal(20,0) primary key not null,
	external_uid varchar(40) not null,
    first_name varchar(100),
    middle_name varchar(100),
    last_name varchar(100),
    acad_title varchar(50),
    email varchar(200),
    session_id decimal(20,0) not null,
    role_id decimal(20,0) not null
	) engine = INNODB;

alter table advisor add constraint fk_advisor_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table advisor add constraint fk_advisor_role foreign key (role_id)
	references roles (role_id) on delete cascade;

create table student_advisor (
	student_id decimal(20,0) not null,
	advisor_id decimal(20,0) not null,
	primary key (student_id, advisor_id)
) engine = INNODB;

alter table student_advisor add constraint fk_std_adv_student foreign key (student_id)
	references student (uniqueid) on delete cascade;

alter table student_advisor add constraint fk_std_adv_advisor foreign key (advisor_id)
	references advisor (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentAdvisorEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentGroupEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentAdvisors'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StudentGroups';

create index idx_advisor on advisor(external_uid, role_id);

/*
 * Update database version
 */

update application_config set value='193' where name='tmtbl.db.version';

commit;
