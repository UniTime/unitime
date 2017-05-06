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

create table meeting_contact (
	meeting_id decimal(20,0) not null,
	contact_id decimal(20,0) not null,
	primary key (meeting_id, contact_id)
) engine = INNODB;

alter table meeting_contact add constraint fk_meeting_contact_mtg foreign key (meeting_id)
	references meeting (uniqueid) on delete cascade;

alter table meeting_contact add constraint fk_meeting_contact_cont foreign key (contact_id)
	references event_contact (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='182' where name='tmtbl.db.version';

commit;
