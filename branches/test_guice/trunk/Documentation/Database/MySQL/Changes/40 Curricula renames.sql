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

-- rename curricula tables to curriculum

alter table curricula rename to curriculum;
alter table curricula_clasf rename to curriculum_clasf;
alter table curricula_course rename to curriculum_course;

alter table curriculum drop foreign key fk_curricula_dept; 
alter table curriculum drop foreign key fk_curricula_acad_area;
alter table curriculum_clasf drop foreign key fk_curricula_clasf_curricula;
alter table curriculum_clasf drop foreign key fk_curricula_clasf_acad_clasf;
alter table curriculum_course drop foreign key fk_curricula_course_clasf;
alter table curriculum_course drop foreign key fk_curricula_course_course;
	
alter table curriculum_clasf change column curricula_id curriculum_id decimal(20,0) not null;

alter table curriculum
	add constraint fk_curriculum_dept foreign key (dept_id)
	references department (uniqueid) on delete cascade; 
alter table curriculum
	add constraint fk_curriculum_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete set null;
alter table curriculum_clasf
   add constraint fk_curriculum_clasf_curriculum foreign key (curriculum_id)
   references curriculum (uniqueid) on delete cascade;
alter table curriculum_clasf
	add constraint fk_curriculum_clasf_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete set null;
alter table curriculum_course
	add constraint fk_curriculum_course_clasf foreign key (cur_clasf_id)
	references curriculum_clasf (uniqueid) on delete cascade;
alter table curriculum_course
	add constraint fk_curriculum_course_course foreign key (course_id)
	references course_offering (uniqueid) on delete cascade;
	
--  Update database version
 
update application_config set value='40' where name='tmtbl.db.version';

commit;
