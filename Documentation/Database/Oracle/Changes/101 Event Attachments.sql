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

alter table event_note add attached_file blob;
alter table event_note add attached_name varchar2(260 char);
alter table event_note add attached_content varchar2(260 char);
alter table event_note add user_id varchar2(40 char);

create table event_note_meeting (
	note_id number(20,0) constraint nn_event_note_note not null,
	meeting_id number(20,0) constraint nn_event_note_mtg not null
);
alter table event_note_meeting add constraint pk_event_note_meeting primary key (note_id, meeting_id);

alter table event_note_meeting add constraint fk_event_note_note foreign key (note_id)
	references event_note (uniqueid) on delete cascade;

alter table event_note_meeting add constraint fk_event_note_mtg foreign key (meeting_id)
	references meeting (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='101' where name='tmtbl.db.version';

commit;
