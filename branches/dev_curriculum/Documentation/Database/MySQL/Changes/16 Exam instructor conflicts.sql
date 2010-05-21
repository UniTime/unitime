/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/


/**
 * Add relation between exam-conflict table and instructors (M:N)
 **/
 
create table xconflict_instructor (
	conflict_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key(conflict_id, instructor_id)
) engine = INNODB;

alter table xconflict_instructor
  add constraint fk_xconflict_in_conf foreign key (conflict_id)
  references xconflict (uniqueid) on delete cascade;

alter table xconflict_instructor
  add constraint fk_xconflict_in_instructor foreign key (instructor_id)
  references departmental_instructor (uniqueid) on delete cascade;
  
alter table exam add assigned_pref varchar(100);
  
/*
 * Update database version
 */

update application_config set value='16' where name='tmtbl.db.version';

commit;
