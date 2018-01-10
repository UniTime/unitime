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
	uniqueid number(20,0) constraint nn_advisor_uniqueid not null,
	external_uid varchar2(40 char) constraint nn_advisor_external_uid not null,
    first_name varchar2(100 char),
    middle_name varchar2(100 char),
    last_name varchar2(100 char),
    acad_title varchar2(50 char),
    email varchar2(200 char),
    session_id number(20,0) constraint nn_advisor_session not null,
	role_id number(20,0) constraint nn_advisor_role not null
);

alter table advisor add constraint pk_advisor primary key (uniqueid);

alter table advisor add constraint fk_advisor_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table advisor add constraint fk_advisor_role foreign key (role_id)
	references roles (role_id) on delete cascade;

create table student_advisor (
	student_id number(20,0) constraint nn_std_adv_student not null,
	advisor_id number(20,0) constraint nn_std_adv_advisor not null
);

alter table student_advisor add constraint pk_student_advisor primary key (student_id, advisor_id);

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
