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
 * Drop link EventNote -> StandardNote
 **/
alter table event_note drop foreign key fk_event_note_std_note;
alter table event_note drop column note_id;


/**
 * Add EventNote -> TimeStamp
 **/
alter table event_note add time_stamp datetime;

update event_note set time_stamp = now();

alter table event_note add constraint nn_event_note_ts check  (time_stamp is not null);

/**
 * Add EventNote -> NoteType
 **/

alter table event_note add note_type int(10) not null default 0;

/**
 * Add EventNote -> User
 **/

alter table event_note add uname varchar(100);

/**
 * Add EventNote -> Meetings
 **/

alter table event_note add meetings varchar(1000);

/**
 * Update database version
 */

update application_config set value='34' where name='tmtbl.db.version';

commit;
