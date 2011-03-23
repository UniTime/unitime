/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

alter table course_offering add reservation number(10);

delete from course_reservation where uniqueid in
	(select r2.uniqueid from course_reservation r1, course_reservation r2 where r1.course_offering = r2.course_offering and r1.uniqueid < r2.uniqueid);

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
	uniqueid number(20,0) constraint nn_reservation_uniqueid not null,
	reservation_type decimal(10,0) constraint nn_reservation_type not null,
	expiration_date date,
	reservation_limit decimal(10,0),
	offering_id number(20,0) constraint nn_reservation_offering not null,
	group_id number(20,0),
	area_id number(20,0),
	course_id number(20,0)
);
alter table reservation add constraint pk_reservation primary key (uniqueid);

create table reservation_config (
	reservation_id number(20,0) constraint nn_res_config_id not null,
	config_id number(20,0) constraint nn_res_config_config not null
);
alter table reservation_config add constraint pk_reservation_config primary key (reservation_id, config_id);

create table reservation_class (
	reservation_id number(20,0) constraint nn_res_class_id not null,
	class_id number(20,0) constraint nn_res_class_class not null
);
alter table reservation_class add constraint pk_reservation_class primary key (reservation_id, class_id);

create table reservation_student (
	reservation_id number(20,0) constraint nn_res_student_id not null,
	student_id number(20,0) constraint nn_res_student_student not null
);
alter table reservation_student add constraint pk_reservation_students primary key (reservation_id, student_id);

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
	reservation_id number(20,0) constraint nn_res_clasf_id not null,
	acad_clasf_id number(20,0) constraint nn_res_clasf_clasf not null
);
alter table reservation_clasf add constraint pk_reservation_clasf primary key (reservation_id, acad_clasf_id);

alter table reservation_clasf add constraint fk_res_clasf_clasf foreign key (acad_clasf_id)
references academic_classification (uniqueid) on delete cascade;

alter table reservation_clasf add constraint fk_res_clasf_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

create table reservation_major (
	reservation_id number(20,0) constraint nn_res_major_id not null,
	major_id number(20,0) constraint nn_res_major_major not null
);
alter table reservation_major add constraint pk_reservation_major primary key (reservation_id, major_id);

alter table reservation_major add constraint fk_res_majors_major foreign key (major_id)
references pos_major (uniqueid) on delete cascade;

alter table reservation_major add constraint fk_res_majors_reservation foreign key (reservation_id)
references reservation (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='65' where name='tmtbl.db.version';

commit;
		