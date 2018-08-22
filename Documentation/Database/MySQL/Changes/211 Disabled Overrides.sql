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

create table override_type (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null
) engine = INNODB;

create table disabled_override (
	course_id decimal(20,0) not null,
	type_id decimal(20,0) not null,
	primary key (course_id, type_id)
) engine = INNODB;

alter table disabled_override add constraint fk_disb_override_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;

alter table disabled_override add constraint fk_disb_override_type foreign key (type_id)
	references override_type (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'OverrideTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'OverrideTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';

/*
 * Update database version
 */

update application_config set value='211' where name='tmtbl.db.version';

commit;
