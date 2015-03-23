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

alter table curriculum_course drop column group_nr;

create table curriculum_group
	(
	  uniqueid number(20) constraint nn_curriculum_group_id not null,
	  name varchar2(20) constraint nn_curriculum_group_name not null,
	  color varchar2(20),
	  type number(10) constraint nn_curriculum_group_type not null,
	  constraint pk_curriculum_group primary key (uniqueid)
	);

create table curriculum_course_group (
		group_id number(20,0) constraint nn_curriculum_course_id not null,
		cur_course_id number(20,0) constraint nn_cur_course_groups_course not null
	);

alter table curriculum_course_group add constraint pk_curriculum_course_groups primary key (group_id, cur_course_id);

alter table curriculum_course_group add constraint fk_cur_course_group_group foreign key (group_id)
	references curriculum_group (uniqueid) on delete cascade;

alter table curriculum_course_group add constraint fk_cur_course_group_course foreign key (cur_course_id)
	references curriculum_course (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='51' where name='tmtbl.db.version';

commit;
