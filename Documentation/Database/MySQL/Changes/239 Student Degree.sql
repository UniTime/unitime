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


create table degree (
	uniqueid decimal(20,0) primary key not null,
	session_id decimal(20,0) not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	external_uid varchar(40)
) engine = INNODB;

create unique index uk_degree on degree(session_id, reference);

alter table degree add constraint fk_degree_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table student_area_clasf_major add degree_id decimal(20,0);

alter table student_area_clasf_major add constraint fk_student_acmaj_deg foreign key (degree_id)
	references degree (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'Degrees'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Majors';

insert into rights (role_id, value)
	select distinct r.role_id, 'DegreeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'MajorEdit';

/*
 * Update database version
 */

update application_config set value='239' where name='tmtbl.db.version';

commit;
