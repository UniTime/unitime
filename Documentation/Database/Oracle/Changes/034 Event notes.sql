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
 * Drop link EventNote -> StandardNote
 **/
alter table event_note drop constraint fk_event_note_std_note;
alter table event_note drop column note_id;

/**
 * Add EventNote -> TimeStamp
 **/
alter table event_note add time_stamp date;

update event_note set time_stamp = CURRENT_TIMESTAMP;

alter table event_note add constraint nn_event_note_ts check  (time_stamp is not null);

/**
 * Add EventNote -> NoteType
 **/

alter table event_note add note_type number(10) default 0 constraint nn_event_note_type not null;

/**
 * Add EventNote -> User
 **/

alter table event_note add uname varchar2(100);

/**
 * Add EventNote -> Meetings
 **/

alter table event_note add meetings varchar2(1000);

/**
 * Update database version
 */

update application_config set value='34' where name='tmtbl.db.version';

commit;
