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

alter table course_offering add reservation bigint(10);

update course_offering c set c.reservation = 
		(select r.reserved from course_reservation r where r.course_offering = c.uniqueid)
	where exists
		(select r.reserved from course_reservation r where r.course_offering = c.uniqueid);

drop table acad_area_reservation;

drop table individual_reservation;

drop table student_group_reservation;

drop table pos_reservation;

drop table course_reservation;

drop table reservation_type;

create table reservation (
	uniqueid decimal(20,0) not null primary key,
	reservation_type bigint(10) not null,
	expiration_date datetime,
	reservation_limit bigint(10),
	offering_id decimal(20,0) not null,
	group_id decimal(20,0),
	area_id decimal(20,0),
	course_id decimal(20,0)
) engine = INNODB;

create table reservation_config (
	reservation_id decimal(20,0) not null,
	config_id decimal(20,0) not null,
	primary key (reservation_id, config_id)
) engine = INNODB;

create table reservation_class (
	reservation_id decimal(20,0) not null,
	class_id decimal(20,0) not null,
	primary key (reservation_id, class_id)
) engine = INNODB;

create table reservation_student (
	reservation_id decimal(20,0) not null,
	student_id decimal(20,0) not null,
	primary key (reservation_id, student_id)
) engine = INNODB;

alter table reservation add constraint fk_reservation_offering foreign key (offering_id)
references instructional_offering (uniqueid) on delete cascade;

alter table reservation add constraint fk_reservation_student_group foreign key (group_id)
references student_group (uniqueid) on delete cascade;

alter table reservation add constraint fk_reservation_area foreign key (area_id)
references academic_area (uniqueid) on delete cascade;

alter table reservation add constraint fk_reservation_course foreign key (course_id)
references course_offering (uniqueid) on delete cascade;

alter table reservation_config add constraint fk_res_config_config foreign key (config_id)
references instr_offering_config (uniqueid) on delete cascade;

alter table reservation_config add constraint fk_res_config_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

alter table reservation_class add constraint fk_res_class_class foreign key (class_id)
references class_ (uniqueid) on delete cascade;

alter table reservation_class add constraint fk_res_class_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

alter table reservation_student add constraint fk_res_student_student foreign key (student_id)
references student (uniqueid) on delete cascade;

alter table reservation_student add constraint fk_res_student_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

create table reservation_clasf (
	reservation_id decimal(20,0) not null,
	acad_clasf_id decimal(20,0) not null,
	primary key (reservation_id, acad_clasf_id)
) engine = INNODB;

alter table reservation_clasf add constraint fk_res_clasf_clasf foreign key (acad_clasf_id)
references academic_classification (uniqueid) on delete cascade;

alter table reservation_clasf add constraint fk_res_clasf_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

create table reservation_major (
	reservation_id decimal(20,0) not null,
	major_id decimal(20,0) not null,
	primary key (reservation_id, major_id)
) engine = INNODB;

alter table reservation_major add constraint fk_res_majors_major foreign key (major_id)
references pos_major (uniqueid) on delete cascade;

alter table reservation_major add constraint fk_res_majors_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='65' where name='tmtbl.db.version';

commit;
		