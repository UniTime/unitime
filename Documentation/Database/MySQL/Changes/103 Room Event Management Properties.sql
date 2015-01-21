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

alter table room add break_time bigint(10) null;
alter table non_university_location add break_time bigint(10) null;
		
alter table room add event_status bigint(10) null;
alter table non_university_location add event_status bigint(10) null;
		
alter table room add note varchar(2048) binary null;
alter table non_university_location add note varchar(2048) binary null;
		
alter table roles add instructor int(1) default 0;
		
alter table departmental_instructor add role_id decimal(20,0) null;

alter table departmental_instructor add constraint fk_instructor_role foreign key (role_id)
	references roles (role_id) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorRoles'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypes';
insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorRoleEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypeEdit';
insert into rights (role_id, value)
	select distinct r.role_id, 'RoomEditChangeEventProperties'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'EventRoomTypeEdit';

/*
 * Update database version
 */

update application_config set value='103' where name='tmtbl.db.version';

commit;
