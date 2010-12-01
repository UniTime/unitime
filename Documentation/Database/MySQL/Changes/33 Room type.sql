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
 * Add is_room attribute to room_type table
 **/
 
alter table room_type add is_room int(1) not null default 1;

/**
 * Add non-university location room type
 **/

select 32767 * next_hi into @id from hibernate_unique_key;

insert into room_type(uniqueid, reference, label, ord, is_room) values
(@id+0, 'nonUniversity', 'Non-University Locations', 4, 0); 

update hibernate_unique_key set next_hi=next_hi+1;

/**
 * Create room_type attribute of non_university_location table
 **/
 
alter table non_university_location add room_type decimal(20,0);
 
update non_university_location r set r.room_type = (select t.uniqueid from room_type t where t.reference='nonUniversity');
 
alter table non_university_location add constraint nn_location_type check  (room_type is not null);
 
alter table non_university_location add constraint fk_location_type foreign key (room_type)
  references room_type (uniqueid) on delete cascade;
  
/**
 * Update database version
 */

update application_config set value='33' where name='tmtbl.db.version';

commit;
