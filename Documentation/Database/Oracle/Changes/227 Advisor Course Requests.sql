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

create table advisor_crsreq (
	uniqueid number(20,0) constraint nn_advisor_crsreq_id not null,
	student_id number(20,0) constraint nn_advisor_crsreq_student not null,
	priority number(10) constraint nn_advisor_crsreq_priority not null,
	substitute number(1) constraint nn_advisor_crsreq_substitute not null,
	alternative number(10) constraint nn_advisor_crsreq_alternative not null,
	time_stamp timestamp constraint nn_advisor_crsreq_timestamp not null,
	changed_by varchar2(40 char),
	credit varchar2(10 char),
	course varchar2(1024 char),
	course_offering_id number(20,0),
	notes varchar2(2048 char),
	free_time_id number(20,0)
);

alter table advisor_crsreq add constraint pk_advisor_crsreq primary key (uniqueid);

alter table advisor_crsreq add constraint fk_advisor_crsreq_student foreign key (student_id)
	references student (uniqueid) on delete cascade;

alter table advisor_crsreq add constraint fk_advisor_crsreq_course foreign key (course_offering_id)
	references course_offering (uniqueid) on delete set null;

alter table advisor_crsreq add constraint fk_advisor_crsreq_free foreign key (free_time_id)
	references free_time (uniqueid) on delete cascade;

create table advisor_sect_pref (
	uniqueid number(20,0) constraint nn_adv_sect_pref_id not null,
	preference_type number(10,0) constraint nn_adv_sect_pref_type not null,
	request_id number(20,0) constraint nn_adv_sect_pref_request not null,
	required number(1) constraint nn_adv_sect_pref_requred not null,
	class_id number(20,0),
	instr_mthd_id number(20,0),
	label varchar2(60 char)
);

alter table advisor_sect_pref add constraint pk_advisor_sect_pref primary key (uniqueid);

alter table advisor_sect_pref add constraint fk_adv_pref_request foreign key (request_id)
	references advisor_crsreq (uniqueid) on delete cascade;

alter table advisor_sect_pref add constraint fk_adv_pref_class foreign key (class_id)
	references class_ (uniqueid) on delete cascade;

alter table advisor_sect_pref add constraint fk_adv_pref_im foreign key (instr_mthd_id)
	references instructional_method (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='227' where name='tmtbl.db.version';

commit;
