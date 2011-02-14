/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
