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
 * Table add course id to exam owner
 */

alter table exam_owner add course_id number(20,0);

alter table exam_owner
  add constraint fk_exam_owner_course foreign key (course_id)
  references course_offering (uniqueid) on delete cascade;
  
update exam_owner o set o.course_id = 
  (select co.uniqueid from course_offering co where co.instr_offr_id=o.owner_id and co.is_control=1)
  where o.owner_type=0;

update exam_owner set course_id = owner_id where owner_type=1;

update exam_owner o set o.course_id = 
	(select co.uniqueid from instr_offering_config ioc, course_offering co 
   where ioc.uniqueid=o.owner_id and ioc.instr_offr_id=co.instr_offr_id and co.is_control=1)
	where o.owner_type=2;

update exam_owner o set o.course_id = 
	(select co.uniqueid from instr_offering_config ioc, course_offering co, scheduling_subpart ss, class_ c 
   where c.uniqueid=o.owner_id and c.subpart_id=ss.uniqueid and
   ss.config_id=ioc.uniqueid and ioc.instr_offr_id=co.instr_offr_id and co.is_control=1)
  where o.owner_type=3;

alter table exam_owner add constraint nn_exam_owner_course check (course_id is not null);

create index idx_exam_owner_course on exam_owner(course_id);

/*
 * Update database version
 */

update application_config set value='13' where name='tmtbl.db.version';

commit;
