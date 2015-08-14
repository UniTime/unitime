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

create table attachment_type (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	abbreviation varchar(20) not null,
	label varchar(60) not null,
	visibility bigint(10) not null
);

alter table room_picture add type_id decimal(20,0);
alter table location_picture add type_id decimal(20,0);

select 32767 * next_hi into @id from hibernate_unique_key;
insert into attachment_type
	(uniqueid, reference, abbreviation, label, visibility) values
	(@id, 'OTHER', 'Other', 'Not Specified', 6),
	(@id + 1, 'PICTURE', 'Picture', 'Room Picture', 15);
update room_picture set type_id = @id where content_type not like 'image/%';
update location_picture set type_id = @id where content_type not like 'image/%';
update room_picture set type_id = @id + 1 where content_type like 'image/%';
update location_picture set type_id = @id + 1 where content_type like 'image/%';
update hibernate_unique_key set next_hi=next_hi+1;

alter table room_picture add constraint fk_room_picture_type foreign key (type_id)
	references attachment_type (uniqueid) on delete set null;
alter table location_picture add constraint fk_location_picture_type foreign key (type_id)
	references attachment_type (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'AttachementTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'AttachementTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';

/*
 * Update database version
 */

update application_config set value='149' where name='tmtbl.db.version';

commit;
