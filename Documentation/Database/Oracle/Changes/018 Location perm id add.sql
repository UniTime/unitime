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
