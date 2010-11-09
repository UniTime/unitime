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
 * Add settings to control the display of different notes on the classes
 **/

select 32767 * next_hi into @id from hibernate_unique_key;

insert into settings 
    (uniqueid, name, default_value, allowed_values, description) values
			(@id + 0, 'printNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a schedule print note.'),
            (@id + 1, 'crsOffrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a course offering has a schedule note.'),
            (@id + 2, 'mgrNoteDisplay', 'icon', 'icon,shortened text,full text', 'Display an icon or shortened text when a class has a note to the schedule manager.');

update hibernate_unique_key set next_hi=next_hi+1;

/**
 * Increase size of sched_print_note column in class_ table
 **/
alter table class_ modify sched_print_note varchar(2000);


/*
 * Update database version
 */

update application_config set value='45' where name='tmtbl.db.version';

commit;
