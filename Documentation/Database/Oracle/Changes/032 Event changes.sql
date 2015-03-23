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
 
alter table event_contact drop constraint nn_event_contact_phone;
 
alter table event_contact drop constraint nn_event_contact_email;

/**
 * Email address can be 200 characters through the application
 */

alter table event_contact modify email varchar2(200);

alter table timetable_manager modify email_address varchar2(200);

/**
 * Add Event Manager role
 */

insert into roles (role_id, reference, abbv) values (role_seq.nextval, 'Event Mgr', 'Event Manager');

/**
 * Added column for additional email(s) on an event 
 */
 
alter table event add email varchar2(1000);

/**
 * Create table for event's sponsoring organizations
 */
 
create table sponsoring_organization (
	uniqueid number(20,0) constraint nn_sponsor_org_id not null,
	name varchar2(100) constraint nn_sponsor_org_name not null,
	email varchar2(200)
);

alter table sponsoring_organization
  add constraint pk_sponsoring_organization primary key (uniqueid);


alter table event add sponsor_org_id number(20,0);

alter table event 
  add constraint fk_event_sponsor_org foreign key (sponsor_org_id)
  references sponsoring_organization (uniqueid) on delete set null;


/*
 * Update database version
 */

update application_config set value='32' where name='tmtbl.db.version';

commit;  
