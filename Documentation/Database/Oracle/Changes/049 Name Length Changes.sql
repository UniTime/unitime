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
 * Increase the length of all person name fields to 100 characters
 **/

alter table departmental_instructor  modify lname varchar2(100);
alter table departmental_instructor modify fname varchar2(100);
alter table departmental_instructor modify mname varchar2(100);

alter table event_contact modify firstname varchar2(100);
alter table event_contact modify middlename varchar2(100);
alter table event_contact modify lastname varchar2(100);

alter table staff modify fname varchar2(100);
alter table staff modify mname varchar2(100);
 
alter table student modify first_name varchar2(100);
alter table student modify middle_name varchar2(100);

alter table timetable_manager modify first_name varchar2(100);
alter table timetable_manager modify middle_name varchar2(100);
alter table timetable_manager modify last_name varchar2(100);

/**
 * Update database version
 */

update application_config set value='49' where name='tmtbl.db.version';

commit;
