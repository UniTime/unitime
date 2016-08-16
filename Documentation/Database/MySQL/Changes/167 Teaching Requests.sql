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

create table teaching_request (
	uniqueid decimal(20,0) primary key not null,
	offering_id decimal(20,0) not null,
	nbr_instructors decimal(10,0) not null,
	teaching_load float not null,
	same_course_pref decimal(20,0) null,
	same_common_pref decimal(20,0) null,
	responsibility_id decimal(20,0) null,
	assign_coordinator int(1) not null
) engine = INNODB;

create table teachreq_class (
	uniqueid decimal(20,0) primary key not null,
	percent_share int(3) not null,
	is_lead int(1) not null,
	can_overlap int(1) not null,
	request_id decimal(20,0) not null,
	class_id decimal(20,0) not null,
	assign_instructor int(1) not null,
	common int(1) not null
) engine = INNODB;

create table teachreq_instructor (
	request_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key(request_id, instructor_id)
) engine = INNODB;

alter table offering_coordinator add request_id decimal(20,0) default null;
alter table class_instructor add request_id decimal(20,0) default null;

alter table teaching_request add constraint fk_teachreq_offering foreign key (offering_id)
	references instructional_offering(uniqueid) on delete cascade;

alter table teaching_request add constraint fk_teachreq_same_course foreign key (same_course_pref)
	references preference_level(uniqueid) on delete set null;

alter table teaching_request add constraint fk_teachreq_same_common foreign key (same_common_pref)
	references preference_level(uniqueid) on delete set null;

alter table teachreq_instructor add constraint fk_teachreq_instructor foreign key (instructor_id)
	references departmental_instructor(uniqueid) on delete cascade;

alter table teachreq_instructor add constraint fk_teachreq_request foreign key (request_id)
	references teaching_request(uniqueid) on delete cascade;

alter table teaching_request add constraint fk_teachreq_responsibility foreign key (responsibility_id)
	references teaching_responsibility(uniqueid) on delete set null;

alter table teachreq_class add constraint fk_teachreq_crequest foreign key (request_id)
	references teaching_request(uniqueid) on delete cascade;

alter table teachreq_class add constraint fk_teachreq_class foreign key (class_id)
	references class_(uniqueid) on delete cascade;

alter table offering_coordinator add constraint fk_coord_request foreign key (request_id)
	references teaching_request (uniqueid) on delete set null;

alter table class_instructor add constraint fk_instr_request foreign key (request_id)
	references teaching_request (uniqueid) on delete set null;

alter table scheduling_subpart drop teaching_load;
alter table scheduling_subpart drop nbr_instructors;
alter table class_ drop teaching_load;
alter table class_ drop nbr_instructors;
alter table class_instructor drop tentative;
alter table class_instructor drop assign_index;

/*
 * Update database version
 */

update application_config set value='167' where name='tmtbl.db.version';

commit;
