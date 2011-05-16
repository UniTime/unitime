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

alter table curriculum_course drop column group_nr;

create table curriculum_group
	(
	  uniqueid number(20) constraint nn_curriculum_group_id not null,
	  name varchar2(20) constraint nn_curriculum_group_name not null,
	  color varchar2(20),
	  type number(10) constraint nn_curriculum_group_type not null,
	  constraint pk_curriculum_group primary key (uniqueid)
	);

create table curriculum_course_group (
		group_id number(20,0) constraint nn_curriculum_course_id not null,
		cur_course_id number(20,0) constraint nn_cur_course_groups_course not null
	);

alter table curriculum_course_group add constraint pk_curriculum_course_groups primary key (group_id, cur_course_id);

alter table curriculum_course_group add constraint fk_cur_course_group_group foreign key (group_id)
	references curriculum_group (uniqueid) on delete cascade;

alter table curriculum_course_group add constraint fk_cur_course_group_course foreign key (cur_course_id)
	references curriculum_course (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='51' where name='tmtbl.db.version';

commit;
