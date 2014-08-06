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

create table curriculum_major (
	curriculum_id number(20,0) constraint nn_curriculum_major_cur_id not null,
	major_id number(20,0) constraint nn_curriculum_major_maj_id not null
);

alter table curriculum_major add constraint pk_curriculum_major primary key (curriculum_id, major_id);

alter table curriculum_major add constraint fk_curriculum_major_curriculum foreign key (curriculum_id)
	references curriculum (uniqueid) on delete cascade;

alter table curriculum_major add constraint fk_curriculum_major_major foreign key (major_id)
	references pos_major (uniqueid) on delete cascade;

/**
 * Update database version
 */

update application_config set value='50' where name='tmtbl.db.version';

commit;
