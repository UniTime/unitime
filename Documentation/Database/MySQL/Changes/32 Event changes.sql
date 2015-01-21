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
 * Event contact -- phone and email can be null 
 */
 
alter table event_contact modify phone varchar(10);

alter table event_contact modify email varchar(200);

/**
 * Email address can be 200 characters through the application
 */

alter table timetable_manager modify email_address varchar(200);

/**
 * Add Event Manager role
 */

select 32767 * next_hi into @id from hibernate_unique_key;

update hibernate_unique_key set next_hi = next_hi+1;

insert into roles (role_id, reference, abbv) values
	(@id, 'Event Mgr', 'Event Manager');

/**
 * Added column for additional email(s) on an event 
 */
 
alter table event add email varchar(1000);

/**
 * Create table for event's sponsoring organizations
 */
 
create table sponsoring_organization (
	uniqueid decimal(20,0) primary key not null,
	name varchar(100) not null,
	email varchar(200)
) engine = INNODB;

alter table event add sponsor_org_id decimal(20,0);

alter table event 
  add constraint fk_event_sponsor_org foreign key (sponsor_org_id)
  references sponsoring_organization (uniqueid) on delete set null;


/*
 * Update database version
 */

update application_config set value='32' where name='tmtbl.db.version';

commit;  
