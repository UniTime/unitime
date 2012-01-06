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

/*
 * Add relation (class) assignment -> event
 */ 
alter table assignment add event_id decimal(20,0);

alter table assignment
  add constraint fk_assignment_event foreign key (event_id)
  references event (uniqueid) on delete set null;

/*
 * Add relation exam -> event
 */ 
alter table exam add event_id decimal(20,0);

alter table exam
  add constraint fk_exam_event foreign key (event_id)
  references event (uniqueid) on delete set null;
  
/*
 * Update database version
 */

update application_config set value='22' where name='tmtbl.db.version';


commit;
  
