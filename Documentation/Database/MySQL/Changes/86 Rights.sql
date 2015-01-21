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


alter table roles add manager int(1) default 1;
alter table roles add enabled int(1) default 1;

create table rights (
	role_id decimal(20,0) not null,
	value varchar(200) binary not null,
	primary key (role_id, value)
) engine = INNODB;
			
alter table rights add constraint fk_rights_role foreign key (role_id)
	references roles (role_id) on delete cascade;

select 32767 * next_hi into @id from hibernate_unique_key;

update hibernate_unique_key set next_hi=next_hi+1;

insert into roles (role_id, reference, abbv, manager, enabled) values 
			(@id + 0, 'No Role', 'No Role', 0, 1),
			(@id + 1, 'Student', 'Student', 0, 1),
			(@id + 2, 'Instructor', 'Instructor', 0, 1);

/*
 * Update database version
 */

update application_config set value='86' where name='tmtbl.db.version';

commit;
