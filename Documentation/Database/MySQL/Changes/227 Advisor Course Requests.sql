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
	uniqueid decimal(20,0) primary key not null,
	student_id decimal(20,0) not null,
	priority bigint(10) not null,
	substitute int(1) not null,
	alternative bigint(10) not null,
	time_stamp datetime not null,
	changed_by varchar(40),
	credit varchar(10),
	course varchar(1024),
	course_offering_id decimal(20,0),
	notes varchar(2048),
	free_time_id decimal(20,0)
) engine = INNODB;

alter table advisor_crsreq add constraint fk_advisor_crsreq_student foreign key (student_id)
	references student (uniqueid) on delete cascade;

alter table advisor_crsreq add constraint fk_advisor_crsreq_course foreign key (course_offering_id)
	references course_offering (uniqueid) on delete set null;

alter table advisor_crsreq add constraint fk_advisor_crsreq_free foreign key (free_time_id)
	references free_time (uniqueid) on delete cascade;

create table advisor_sect_pref (
	uniqueid decimal(20,0) primary key not null,
	preference_type decimal(10,0) not null,
	request_id decimal(20,0) not null,
	required int(1) not null,
	class_id decimal(20,0),
	instr_mthd_id decimal(20,0),
	label varchar(60)
) engine = INNODB;

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
