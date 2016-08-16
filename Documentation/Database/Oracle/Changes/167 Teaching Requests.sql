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
	uniqueid number(20,0) constraint nn_teachreq_uniqueid not null,
	offering_id number(20,0) constraint nn_teachreq_offering not null,
	nbr_instructors number(10,0) constraint nn_teachreq_nbrinstr not null,
	teaching_load float constraint nn_teachreq_load not null,
	same_course_pref number(20,0),
	same_common_pref number(20,0),
	responsibility_id number(20,0),
	assign_coordinator number(1) constraint nn_teachreq_assign not null
);
alter table teaching_request add constraint pk_teaching_request primary key (uniqueid);

create table teachreq_class (
	uniqueid number(20,0) constraint nn_clsteachreq_uniqueid not null,
	percent_share number(3,0) constraint nn_clsteachreq_share not null,
	is_lead number(1) constraint nn_clsteachreq_lead not null,
	can_overlap number(1) constraint nn_clsteachreq_overlap not null,
	request_id number(20,0) constraint nn_clsteachreq_request not null,
	class_id number(20,0) constraint nn_clsteachreq_class not null,
	assign_instructor number(1) constraint nn_clsteachreq_assign not null,
	common number(1) constraint nn_clsteachreq_common not null
);
alter table teachreq_class add constraint pk_teachreq_class primary key (uniqueid);

create table teachreq_instructor (
	request_id number(20,0) constraint nn_instrteachreq_request not null,
	instructor_id number(20,0) constraint nn_instrteachreq_instr not null
);
alter table teachreq_instructor add constraint pk_teachreq_instructor primary key (request_id, instructor_id);
alter table offering_coordinator add request_id number(20,0);
alter table class_instructor add request_id number(20,0);

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

alter table scheduling_subpart drop column teaching_load;
alter table scheduling_subpart drop column nbr_instructors;
alter table class_ drop column teaching_load;
alter table class_ drop column nbr_instructors;
alter table class_instructor drop column tentative;
alter table class_instructor drop column assign_index;

/*
 * Update database version
 */

update application_config set value='167' where name='tmtbl.db.version';

commit;
