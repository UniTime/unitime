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

alter table offering_coordinator add uniqueid decimal(20,0);
select 32767 * next_hi into @id from hibernate_unique_key;
update offering_coordinator o join (
	select @uid:=@uid+1 uid, offering_id, instructor_id from offering_coordinator cross join (select @uid:=@id) x
	) as r on (o.offering_id = r.offering_id and o.instructor_id = r.instructor_id) set o.uniqueid = r.uid;
alter table offering_coordinator modify uniqueid decimal(20,0) not null;
alter table offering_coordinator drop foreign key fk_offering_coord_instructor;
alter table offering_coordinator drop foreign key fk_offering_coord_offering;
alter table offering_coordinator drop primary key;
alter table offering_coordinator add primary key (uniqueid);
alter table offering_coordinator add constraint fk_offering_coord_offering foreign key (offering_id)
	references instructional_offering (uniqueid) on delete cascade;
alter table offering_coordinator add constraint fk_offering_coord_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;
update hibernate_unique_key set next_hi=next_hi+1;

alter table offering_coordinator add percent_share int(3);
update offering_coordinator set percent_share = 0;
alter table offering_coordinator modify percent_share int(3) not null;

alter table teaching_request add percent_share int(3);
update teaching_request set percent_share = 0;
alter table teaching_request modify percent_share int(3) not null;

/*
 * Update database version
 */

update application_config set value='174' where name='tmtbl.db.version';

commit;
