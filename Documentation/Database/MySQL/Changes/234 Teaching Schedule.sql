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
	uniqueid decimal(20,0) primary key not null,
	offering_id decimal(20,0) not null,
	config_id decimal(20,0) not null,
	itype int(2) not null,
	attribute_id decimal(20,0) null,
	name varchar(100),
	nr_groups bigint(10) not null,
	nr_hours bigint(10) not null,
	nr_parallels bigint(10) not null,
	ord bigint(10) not null
) engine = INNODB;

create table course_div_meeting (
	uniqueid decimal(20,0) primary key not null,
	division_id decimal(20,0) not null,
	meeting_id decimal(20,0) not null,
	first_hour bigint(10) not null,
	last_hour bigint(10) not null,
	class_idx bigint(10) not null,
	group_idx bigint(10) not null,
	note varchar(200)
) engine = INNODB;

create table course_div_meeting_instructor (
	course_meeting_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key (course_meeting_id, instructor_id)
) engine = INNODB;

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