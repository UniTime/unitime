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


-- Add/modify columns 
alter table room add permanent_id decimal(20,0); 
alter table non_university_location add permanent_id decimal(20,0);

-- Create table
create table temp_perm_id
(
  external_uid varchar(40) not null,
  perm_id      decimal(20,0) not null 
) engine memory;

-- populate temp_perm_id table
insert into temp_perm_id 
select s.eid, @id := @id + 1 from (select distinct r.external_uid as eid 
from room r, (select @id := 32767 * next_hi from hibernate_unique_key) x
where r.external_uid is not null
union (select distinct cast(r1.uniqueid as char) as eid from room r1 where r1.external_uid is null)
union (select distinct cast(nul.uniqueid as char) as eid from non_university_location nul)) s;

update hibernate_unique_key set next_hi = next_hi + 1;

-- populate  permanent_id for rooms with external ids
update room r
set r.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = r.external_uid)
where r.external_uid is not null;

-- populate  permanent_id for rooms with out external ids
update room r
set r.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = cast(r.uniqueid as char))
where r.external_uid is null;

-- populate  permanent_id for non_university_locations
update non_university_location nul
set nul.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = cast(nul.uniqueid as char));

-- drop temp_perm_id table
drop table temp_perm_id;

-- Add/modify columns 
alter table room modify permanent_id decimal(20,0) not null;
alter table non_university_location modify permanent_id decimal(20,0) not null;
  
/*
 * Update database version
 */

update application_config set value='18' where name='tmtbl.db.version';

commit;
  
