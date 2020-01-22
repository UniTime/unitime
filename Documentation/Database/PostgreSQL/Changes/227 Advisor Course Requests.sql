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
	uniqueid bigint not null,
	student_id bigint not null,
	priority bigint not null,
	substitute boolean not null,
	alternative bigint not null,
	time_stamp timestamp with time zone not null,
	changed_by varchar(40),
	credit varchar(10),
	course varchar(1024),
	course_offering_id bigint,
	notes varchar(2048),
	free_time_id bigint
);

alter table advisor_crsreq add constraint pk_advisor_crsreq primary key (uniqueid);

alter table advisor_crsreq add constraint fk_advisor_crsreq_student foreign key (student_id)
	references student (uniqueid) on delete cascade;

alter table advisor_crsreq add constraint fk_advisor_crsreq_course foreign key (course_offering_id)
	references course_offering (uniqueid) on delete set null;

alter table advisor_crsreq add constraint fk_advisor_crsreq_free foreign key (free_time_id)
	references free_time (uniqueid) on delete cascade;

create table advisor_sect_pref (
	uniqueid bigint not null,
	preference_type bigint not null,
	request_id bigint not null,
	required boolean not null,
	class_id bigint,
	instr_mthd_id bigint,
	label varchar(60)
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
