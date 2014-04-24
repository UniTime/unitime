/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
