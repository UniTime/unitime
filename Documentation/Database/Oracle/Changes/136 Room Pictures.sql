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

create table room_picture (
	uniqueid number(20,0) constraint nn_room_pic_id not null,
	location_id number(20,0) constraint nn_room_pic_loc not null,
	data_file blob constraint nn_room_pic_file not null,
	file_name varchar2(260 char) constraint nn_room_pic_name not null,
	content_type varchar2(260 char) constraint nn_room_pic_type not null,
	time_stamp date constraint nn_room_pic_time not null
);
alter table room_picture add constraint pk_room_picture primary key (uniqueid);

create table location_picture (
	uniqueid number(20,0) constraint nn_loc_pic_id not null,
	location_id number(20,0) constraint nn_loc_pic_loc not null,
	data_file blob constraint nn_loc_pic_file not null,
	file_name varchar2(260 char) constraint nn_loc_pic_name not null,
	content_type varchar2(260 char) constraint nn_loc_pic_type not null,
	time_stamp date constraint nn_loc_pic_time not null
);
alter table location_picture add constraint pk_location_picture primary key (uniqueid);

alter table room_picture 
	add constraint fk_room_picture foreign key (location_id)
	references room (uniqueid) on delete cascade;

alter table location_picture 
	add constraint fk_location_picture foreign key (location_id)
	references non_university_location (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'RoomEditChangePicture'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'RoomEditChangeCapacity';

/*
 * Update database version
 */

update application_config set value='136' where name='tmtbl.db.version';

commit;
