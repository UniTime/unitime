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

create table course_division (
	uniqueid number(20) constraint nn_coursediv_id not null,
	offering_id number(20) constraint nn_coursediv_offering not null,
	config_id number(20) constraint nn_coursediv_config not null,
	itype number(2) constraint nn_coursediv_itype not null,
	attribute_id number(20) null,
	name varchar2(100 char),
	nr_groups number(10) constraint nn_coursediv_groups not null,
	nr_hours number(10) constraint nn_coursediv_hours not null,
	nr_parallels number(10) constraint nn_coursediv_paral not null,
	ord number(10) not null
);

alter table course_division add constraint pk_course_division primary key (uniqueid);

create table course_div_meeting (
	uniqueid number(20) constraint nn_coursedivm_id not null,
	division_id number(20) constraint nn_coursedivm_division not null,
	meeting_id number(20) constraint nn_coursedivm_meeting not null,
	first_hour number(10) constraint nn_coursedivm_firsth not null,
	last_hour number(10) constraint nn_coursedivm_lasth not null,
	class_idx number(10) constraint nn_coursedivm_class not null,
	group_idx number(10) constraint nn_coursedivm_group not null,
	note varchar2(200 char)
);

alter table course_div_meeting add constraint pk_course_meeting primary key (uniqueid);

create table course_div_meeting_instructor (
	course_meeting_id number(20) constraint nn_coursedivin_meeting not null,
	instructor_id number(20) constraint nn_coursedivin_instr not null
);

alter table course_div_meeting_instructor add constraint pk_course_meeting_instructor primary key (course_meeting_id, instructor_id);

alter table course_division add constraint fk_coursediv_offering foreign key (offering_id) references instructional_offering (uniqueid) on delete cascade;

alter table course_division add constraint fk_coursediv_config foreign key (config_id) references instr_offering_config (uniqueid) on delete cascade;

alter table course_division add constraint fk_coursediv_itype foreign key (itype) references itype_desc (itype) on delete cascade;

alter table course_division add constraint fk_coursediv_attribute foreign key (attribute_id) references attribute (uniqueid) on delete cascade;

alter table course_div_meeting add constraint fk_coursedivm_div foreign key (division_id) references course_division (uniqueid) on delete cascade;

alter table course_div_meeting add constraint fk_coursedivm_meeting foreign key (meeting_id) references meeting (uniqueid) on delete cascade;

alter table course_div_meeting_instructor add constraint fk_coursedivin_meeting foreign key (course_meeting_id) references course_div_meeting (uniqueid) on delete cascade;

alter table course_div_meeting_instructor add constraint fk_coursedivin_instructor foreign key (instructor_id) references departmental_instructor (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='234' where name='tmtbl.db.version';

commit;
