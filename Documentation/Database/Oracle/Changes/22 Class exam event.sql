/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
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

/*
 * Add relation (class) assignment -> event
 */ 
alter table assignment add event_id number(20,0);

alter table assignment
  add constraint fk_assignment_event foreign key (event_id)
  references event (uniqueid) on delete set null;

/*
 * Add relation exam -> event
 */ 
alter table exam add event_id number(20,0);

alter table exam
  add constraint fk_exam_event foreign key (event_id)
  references event (uniqueid) on delete set null;
  
/*
 * Update database version
 */

update application_config set value='22' where name='tmtbl.db.version';


commit;
  
