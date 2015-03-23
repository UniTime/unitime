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

-- Create table curricula
create table curricula
(
  uniqueid  number(20) constraint nn_curricula_uniqueid not null, 
  abbv varchar2(20) constraint nn_curricula_abbv not null,
  name varchar2(60) constraint nn_curricula_name not null,
  acad_area_id  number(20),
  dept_id number(20) constraint nn_curricula_dept not null,
  constraint pk_curricula primary key (uniqueid)
);

 -- Create table curricula classification
create table curricula_clasf
(
  uniqueid    number(20) constraint nn_curricula_clasf_uniqueid not null,
  curricula_id  number(20) constraint nn_curricula_clasf_cur_id not null,
  name   varchar2(20) constraint nn_curricula_clasf_name not null,
  acad_clasf_id     number(20) ,
  nr_students   number(10) constraint nn_curricula_clasf_nr_students not null,
  ll_students   number(10),
  ord    number(10) constraint nn_curricula_clasf_ord not null,
  constraint pk_curricula_clasf primary key (uniqueid)
);

-- Create table curricula course projection
create table curricula_course
(
  uniqueid       number(20) constraint nn_curricula_course_uniqueid not null,
  course_id           number(20) constraint nn_curricula_course_course_id not null,
  cur_clasf_id          number(20) constraint nn_curricula_cur_clasf_id not null,
  pr_share  float constraint nn_curricula_course_prsh not null,
  ll_share   float,
  group_nr number(10) constraint nn_curricula_course_group_nr not null,
  ord         number(10) constraint nn_curricula_course_ord not null,
  constraint pk_curricula_course primary key (uniqueid)
);

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
