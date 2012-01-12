/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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

-- Create table curricula
create table curricula
(
  uniqueid  decimal(20,0) primary key not null,
  abbv varchar(20) not null,
  name varchar(60) not null,
  acad_area_id  decimal(20,0),
  dept_id decimal(20,0) not null
) engine = INNODB;

 -- Create table curricula classification
create table curricula_clasf
(
  uniqueid    decimal(20,0) primary key not null,
  curricula_id  decimal(20,0) not null,
  name   varchar(20)  not null,
  acad_clasf_id     decimal(20,0),
  nr_students   bigint(10) not null,
  ll_students   bigint(10),
  ord    bigint(10) not null
) engine = INNODB;

-- Create table curricula course projection
create table curricula_course
(
  uniqueid       decimal(20,0) not null primary key,
  course_id           decimal(20,0) not null,
  cur_clasf_id          decimal(20,0) not null,
  pr_share  float not null,
  ll_share   float,
  group_nr bigint(10) not null,
  ord         bigint(10) not null
) engine = INNODB;

-- Create foreign key constraints
alter table curricula
	add constraint fk_curricula_dept foreign key (dept_id)
	references department (uniqueid) on delete cascade; 
alter table curricula
	add constraint fk_curricula_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete set null;
alter table curricula_clasf
   add constraint fk_curricula_clasf_curricula foreign key (curricula_id)
   references curricula (uniqueid) on delete cascade;
alter table curricula_clasf
	add constraint fk_curricula_clasf_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete set null;
alter table curricula_course
	add constraint fk_curricula_course_clasf foreign key (cur_clasf_id)
	references curricula_clasf (uniqueid) on delete cascade;
alter table curricula_course
	add constraint fk_curricula_course_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;


--  Update database version
 
 update application_config set value='38' where name='tmtbl.db.version';

commit;
