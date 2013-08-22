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

alter table curriculum rename constraint nn_curricula_uniqueid to nn_curriculum_uniqueid;
alter table curriculum rename constraint nn_curricula_abbv to nn_curriculum_abbv;
alter table curriculum rename constraint nn_curricula_name to nn_curriculum_name;
alter table curriculum rename constraint nn_curricula_dept to nn_curriculum_dept;
alter table curriculum rename constraint pk_curricula to pk_curriculum;
alter table curriculum rename constraint fk_curricula_dept to fk_curriculum_dept;
alter table curriculum rename constraint fk_curricula_acad_area to fk_curriculum_acad_area;

alter table curriculum_clasf rename constraint nn_curricula_clasf_uniqueid to nn_curriculum_clasf_uniqueid;
alter table curriculum_clasf rename constraint nn_curricula_clasf_cur_id to nn_curriculum_clasf_cur_id;
alter table curriculum_clasf rename constraint nn_curricula_clasf_name to nn_curriculum_clasf_name;
alter table curriculum_clasf rename constraint nn_curricula_clasf_nr_students to nn_curriculum_clasf_nrstudents;
alter table curriculum_clasf rename constraint nn_curricula_clasf_ord to nn_curriculum_clasf_ord;
alter table curriculum_clasf rename constraint pk_curricula_clasf to pk_curriculum_clasf;
alter table curriculum_clasf rename constraint fk_curricula_clasf_curricula to fk_curriculum_clasf_curriculum;
alter table curriculum_clasf rename constraint fk_curricula_clasf_acad_clasf to fk_curriculum_clasf_acad_clasf;

alter table curriculum_course rename constraint nn_curricula_course_uniqueid to nn_curriculum_course_uniqueid;
alter table curriculum_course rename constraint nn_curricula_course_course_id to nn_curriculum_course_course_id;
alter table curriculum_course rename constraint nn_curricula_cur_clasf_id to nn_curriculum_cur_clasf_id;
alter table curriculum_course rename constraint nn_curricula_course_prsh to nn_curriculum_course_prsh;
alter table curriculum_course rename constraint nn_curricula_course_ord to nn_curriculum_course_ord;
alter table curriculum_course rename constraint pk_curricula_course to pk_curriculum_course;
alter table curriculum_course rename constraint fk_curricula_course_clasf to fk_curriculum_course_clasf;
alter table curriculum_course rename constraint fk_curricula_course_course to fk_curriculum_course_course;

alter table curriculum_clasf rename column curricula_id to curriculum_id;

--  Update database version
 
 update application_config set value='40' where name='tmtbl.db.version';

commit;
