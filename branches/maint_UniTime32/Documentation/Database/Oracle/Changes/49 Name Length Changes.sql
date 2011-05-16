/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2009 - 2010, UniTime LLC
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
