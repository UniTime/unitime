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

create table std_group_type (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	together int(1) not null
	) engine = INNODB;

alter table student_group add type_id decimal(20,0) default null;
alter table student_group 
	add constraint fk_std_group_type foreign key (type_id)
	references std_group_type (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'StudentGroupTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevels';
insert into rights (role_id, value)
	select distinct r.role_id, 'StudentGroupTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevelEdit';

/*
 * Update database version
 */

update application_config set value='200' where name='tmtbl.db.version';

commit;
