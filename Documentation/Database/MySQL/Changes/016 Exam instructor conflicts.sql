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
 * Add relation between exam-conflict table and instructors (M:N)
 **/
 
create table xconflict_instructor (
	conflict_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key(conflict_id, instructor_id)
) engine = INNODB;

alter table xconflict_instructor
  add constraint fk_xconflict_in_conf foreign key (conflict_id)
  references xconflict (uniqueid) on delete cascade;

alter table xconflict_instructor
  add constraint fk_xconflict_in_instructor foreign key (instructor_id)
  references departmental_instructor (uniqueid) on delete cascade;
  
alter table exam add assigned_pref varchar(100);
  
/*
 * Update database version
 */

update application_config set value='16' where name='tmtbl.db.version';

commit;
