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


create table program (
	uniqueid number(20,0) constraint nn_program_id not null,
	session_id number(20,0) constraint nn_program_session_id not null,
	reference varchar2(20 char) constraint nn_program_reference not null,
	label varchar2(60 char) constraint nn_program_label not null,
	external_uid varchar2(40 char)
);
alter table program add constraint pk_program primary key (uniqueid);
create unique index uk_program on program(session_id, reference);

alter table program add constraint fk_program_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table student_area_clasf_major add program_id number(20,0);

alter table student_area_clasf_major add constraint fk_student_acmaj_prog foreign key (program_id)
	references program (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'Programs'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Majors';

insert into rights (role_id, value)
	select distinct r.role_id, 'ProgramEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'MajorEdit';

/*
 * Update database version
 */

update application_config set value='245' where name='tmtbl.db.version';

commit;
