/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


-- Add/modify columns 
alter table room add permanent_id decimal(20,0); 
alter table non_university_location add permanent_id decimal(20,0);

-- Create table
create table temp_perm_id
(
  external_uid varchar(40) not null,
  perm_id      decimal(20,0) not null
);

-- populate temp_perm_id table
insert into temp_perm_id tpi
select s.eid, rownum from (select distinct r.external_uid as eid 
from room r
where r.external_uid is not null
union (select distinct to_char(r1.uniqueid) as eid from room r1 where r1.external_uid is null)
union (select distinct to_char(nul.uniqueid) as eid from non_university_location nul)) s;

-- populate  permanent_id for rooms with external ids
update room r
set r.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = r.external_uid)
where r.external_uid is not null;

-- populate  permanent_id for rooms with out external ids
update room r
set r.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = to_char(r.uniqueid))
where r.external_uid is null;

-- populate  permanent_id for non_university_locations
update non_university_location nul
set nul.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = to_char(nul.uniqueid));

-- drop temp_perm_id table
drop table temp_perm_id;

-- Add/modify columns 
alter table room modify permanent_id not null;
alter table non_university_location modify permanent_id not null;

  
/*
 * Update database version
 */

update application_config set value='17' where name='tmtbl.db.version';

commit;
  