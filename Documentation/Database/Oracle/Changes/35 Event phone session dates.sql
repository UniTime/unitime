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
 * Change event phone to 25 chars
 **/
 
alter table event_contact modify phone varchar2(25);
 
/**
 * Add session event_begin_date, event_end_date 
 **/
 
alter table sessions add event_begin_date date;

update sessions set event_begin_date = session_begin_date_time-31;

alter table sessions add constraint nn_sessions_event_begin_date check (event_begin_date is not null);

alter table sessions add event_end_date date;

update sessions set event_end_date = session_end_date_time+31;

alter table sessions add constraint nn_sessions_event_end_date check (event_end_date is not null);

/**
 * Update database version
 */

update application_config set value='35' where name='tmtbl.db.version';

commit;
