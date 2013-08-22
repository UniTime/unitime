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

alter table assignment add date_pattern_id decimal(20,0) null;

alter table assignment add constraint fk_assignment_date_pattern foreign key (date_pattern_id)
	references date_pattern(uniqueid) on delete set null;

update assignment a, class_ c
	set a.date_pattern_id = c.date_pattern_id
	where c.date_pattern_id is not null and c.uniqueid = a.class_id and a.date_pattern_id is null; 

update assignment a, scheduling_subpart s, class_ c
	set a.date_pattern_id = s.date_pattern_id
	where s.date_pattern_id is not null and s.uniqueid = c.subpart_id and c.uniqueid = a.class_id and a.date_pattern_id is null; 

update assignment a, solution x, solver_group g, sessions s
	set a.date_pattern_id = s.def_datepatt_id
	where a.solution_id = x.uniqueid and x.owner_id = g.uniqueid and g.session_id = s.uniqueid and a.date_pattern_id is null; 


/*
 * Update database version
 */

update application_config set value='74' where name='tmtbl.db.version';

commit;
