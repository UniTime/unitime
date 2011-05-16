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
	  uniqueid decimal(20,0) primary key not null,
	  name varchar(20) not null,
	  color varchar(60),
	  type bigint(10) not null
	) engine = INNODB;

create table curriculum_course_group (
		group_id decimal(20,0) not null,
		cur_course_id decimal(20,0) not null,
		primary key (group_id, cur_course_id)
	) engine = INNODB;

alter table curriculum_course_group add constraint fk_cur_course_group_group foreign key (group_id)
	references curriculum_group (uniqueid) on delete cascade;

alter table curriculum_course_group add constraint fk_cur_course_group_course foreign key (cur_course_id)
	references curriculum_course (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='51' where name='tmtbl.db.version';

commit;
