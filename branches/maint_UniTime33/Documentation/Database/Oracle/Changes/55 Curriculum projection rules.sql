/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
 * disallow null in curriculum.acad_area_id
 */
alter table curriculum drop constraint fk_curriculum_acad_area;

alter table curriculum add constraint nn_curriculum_acad_area check (acad_area_id is not null);

alter table curriculum add constraint fk_curriculum_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

/**
 * disallow null in curriculum_clasf.acad_clasf_id
 */
alter table curriculum_clasf drop constraint fk_curriculum_clasf_acad_clasf;

alter table curriculum_clasf add constraint nn_curriculum_clasf_acad_clasf check (acad_clasf_id is not null);

alter table curriculum_clasf add constraint fk_curriculum_clasf_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

/**
 * drop unused columns
 */

alter table curriculum_clasf drop column ll_students;
alter table curriculum_course drop column ll_share;

/**
 * create curriculum_rule table
 */
 
create table curriculum_rule
	(
	  uniqueid number(20) constraint nn_cur_rule_id not null,
	  acad_area_id number(20) constraint nn_cur_rule_acad_area not null,
	  major_id number(20),
	  acad_clasf_id number(20) constraint nn_cur_rule_acad_clasf not null,
	  projection float constraint nn_cur_rule_proj not null,
	  constraint pk_curriculum_rule primary key (uniqueid)
	);


create index idx_cur_rule_areadept on curriculum_rule(acad_area_id, acad_clasf_id);

alter table curriculum_rule add constraint fk_cur_rule_acad_area foreign key (acad_area_id)
	references academic_area (uniqueid) on delete cascade;

alter table curriculum_rule add constraint fk_cur_rule_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

alter table curriculum_rule add constraint fk_cur_rule_acad_clasf foreign key (acad_clasf_id)
	references academic_classification (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='55' where name='tmtbl.db.version';

commit;
