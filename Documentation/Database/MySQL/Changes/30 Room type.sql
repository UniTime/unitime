/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
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


/**
 * Add room type table
 **/
 
create table room_type (
	uniqueid decimal(20,0) not null primary key,
	reference varchar(20) not null,
	label varchar(60) not null,
	ord int(10) not null
) engine = INNODB;

/**
 * Populate room types
 **/

select 32767 * next_hi into @id from hibernate_unique_key;

insert into room_type(uniqueid, reference, label, ord) values
(@id+0, 'genClassroom', 'Classrooms', 0),
(@id+1, 'computingLab', 'Computing Laboratories', 1),
(@id+2, 'departmental', 'Additional Instructional Rooms', 2),
(@id+3, 'specialUse', 'Special Use Rooms', 3); 


update hibernate_unique_key set next_hi=next_hi+1;

/**
 * Create room_type attribute of room table
 **/
 
alter table room add room_type decimal(20,0);
 
update room r set r.room_type = (select t.uniqueid from room_type t where t.reference=r.scheduled_room_type);
 
alter table room drop column scheduled_room_type;

alter table room add constraint nn_room_type check  (room_type is not null);
 
alter table room add constraint fk_room_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
/**
 * Create room_type attribute of external_room table
 **/
 
alter table external_room add room_type decimal(20,0);
 
update external_room r set r.room_type = (select t.uniqueid from room_type t where t.reference=r.scheduled_room_type);
 
alter table external_room drop column scheduled_room_type;

alter table external_room add constraint nn_external_room_type check  (room_type is not null);
 
alter table external_room add constraint fk_external_room_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
/**
 * Add room_type_options table (session related)
 **/
 
create table room_type_option (
	room_type decimal(20,0) not null,
	session_id decimal(20,0) not null,
	status int(10) not null,
	message varchar(200),
	primary key (room_type, session_id)
) engine = INNODB;

alter table room_type_option add constraint fk_rtype_option_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
alter table room_type_option add constraint fk_rtype_option_session foreign key (session_id)
  references sessions (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='30' where name='tmtbl.db.version';

commit;
