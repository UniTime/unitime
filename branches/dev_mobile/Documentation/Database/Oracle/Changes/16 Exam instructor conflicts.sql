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
 * Add relation between exam-conflict table and instructors (M:N)
 **/
 
create table xconflict_instructor (
	conflict_id number(20,0) constraint nn_xconflict_in_conf  not null,
	instructor_id number(20,0) constraint nn_xconflict_in_student  not null
);

alter table xconflict_instructor
  add constraint pk_xconflict_instructor primary key (conflict_id, instructor_id);

alter table xconflict_instructor
  add constraint fk_xconflict_in_conf foreign key (conflict_id)
  references xconflict (uniqueid) on delete cascade;

alter table xconflict_instructor
  add constraint fk_xconflict_in_instructor foreign key (instructor_id)
  references departmental_instructor (uniqueid) on delete cascade;
  
alter table exam add assigned_pref varchar2(100);
  
/*
 * Update database version
 */

update application_config set value='16' where name='tmtbl.db.version';

commit;
