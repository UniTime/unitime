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


create table campus (
	uniqueid bigint not null,
    session_id bigint not null,
    reference varchar(20) not null,
    label varchar(60) not null,
	external_uid varchar(40)
);
alter table campus add constraint pk_campus primary key (uniqueid);
create unique index uk_campus on campus using btree (session_id, reference);

alter table campus add constraint fk_campus_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table student_area_clasf_major add campus_id bigint;

alter table student_area_clasf_major add constraint fk_student_acmaj_campus foreign key (campus_id)
	references campus (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'Campuses'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Majors';

insert into rights (role_id, value)
	select distinct r.role_id, 'CampusEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'MajorEdit';

/*
 * Update database version
 */

update application_config set value='247' where name='tmtbl.db.version';

commit;
