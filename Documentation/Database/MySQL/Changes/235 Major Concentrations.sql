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

create table pos_major_conc (
	uniqueid decimal(20,0) primary key not null,
	major_id decimal(20,0) not null,
	external_uid varchar(40),
	code varchar(40) not null,
	name varchar(100) not null
) engine = INNODB;

alter table pos_major_conc add constraint fk_pos_conc_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

alter table student_area_clasf_major add concentration_id decimal(20,0);

alter table student_area_clasf_major add constraint fk_student_acmaj_conc foreign key (concentration_id)
	references pos_major_conc (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'Concentrations'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Majors';

insert into rights (role_id, value)
	select distinct r.role_id, 'ConcentrationEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'MajorEdit';

create table reservation_major_conc (
	reservation_id decimal(20,0) not null,
	concentration_id decimal(20,0) not null,
	primary key (reservation_id, concentration_id)
) engine = INNODB;

alter table reservation_major_conc add constraint fk_res_mj_conc_conc foreign key (concentration_id)
	references pos_major_conc (uniqueid) on delete cascade;

alter table reservation_major_conc add constraint fk_res_mj_conc_res foreign key (reservation_id)
	references reservation (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='235' where name='tmtbl.db.version';

commit;
