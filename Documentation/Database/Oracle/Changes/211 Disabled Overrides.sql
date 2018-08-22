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
	uniqueid number(20,0) constraint nn_override_type_id not null,
	reference varchar2(20 char) constraint nn_override_type_ref not null,
	label varchar2(60 char) constraint nn_override_type_label not null
);
alter table override_type add constraint pk_override_type primary key (uniqueid);

create table disabled_override (
	course_id number(20,0) constraint nn_disb_override_course not null,
	type_id number(20,0) constraint nn_disb_override_type not null
);
alter table disabled_override add constraint pk_disabled_overrides primary key (course_id, type_id);

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
