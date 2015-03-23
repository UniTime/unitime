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
	  uniqueid decimal(20,0) primary key not null,
	  name varchar(20) not null,
	  color varchar(60),
	  type bigint(10) not null
	) engine = INNODB;

create table curriculum_course_group (
		group_id decimal(20,0) not null,
		cur_course_id decimal(20,0) not null,
		primary key (group_id, cur_course_id)
	) engine = INNODB;

alter table curriculum_course_group add constraint fk_cur_course_group_group foreign key (group_id)
	references curriculum_group (uniqueid) on delete cascade;

alter table curriculum_course_group add constraint fk_cur_course_group_course foreign key (cur_course_id)
	references curriculum_course (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='51' where name='tmtbl.db.version';

commit;
