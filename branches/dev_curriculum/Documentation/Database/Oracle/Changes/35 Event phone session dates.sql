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
