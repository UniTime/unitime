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
  
