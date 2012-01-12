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
alter table ROOM add permanent_id number(20);
-- Create table
create table temp_perm_id
(
  external_uid varchar(40) not null,
  perm_id      number(20) not null
);
-- Create sequence 
create sequence loc_perm_id_seq
minvalue 1
maxvalue 99999999999999999999
start with 1
increment by 1
cache 20;
-- populate temp_perm_id table
insert into temp_perm_id tpi
select s.eid, loc_perm_id_seq.nextval from (select distinct r.external_uid as eid 
from room r
where r.external_uid is not null
order by r.external_uid) s;
-- populate room permanent_id for rooms with external ids
update room r
set r.permanent_id = (select tpi.perm_id from temp_perm_id tpi where tpi.external_uid = r.external_uid)
where r.external_uid is not null;
-- drop temp_perm_id table
drop table temp_perm_id;
-- assign permanent ids to rooms that do not have external ids
update room r
set r.permanent_id = loc_perm_id_seq.nextval
where r.external_uid is null;
-- Add/modify columns 
alter table ROOM modify PERMANENT_ID not null;
-- Add/modify columns 
alter table NON_UNIVERSITY_LOCATION add permanent_id number(20);
-- assign permanent ids to non university locations
update non_university_location nul
set nul.permanent_id = loc_perm_id_seq.nextval;
-- Add/modify columns 
alter table NON_UNIVERSITY_LOCATION modify PERMANENT_ID not null;

/*
 * Update database version
 */

update application_config set value='18' where name='tmtbl.db.version';


commit;
