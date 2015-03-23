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
