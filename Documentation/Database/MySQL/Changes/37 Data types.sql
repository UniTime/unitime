/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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

alter table class_ modify room_ratio float;
alter table course_subpart_credit modify fixed_min_credit float;
alter table course_subpart_credit modify max_credit float;
alter table course_credit_unit_config modify fixed_units float;
alter table course_credit_unit_config modify min_units float;
alter table course_credit_unit_config modify max_units float;
alter table course_catalog modify fixed_min_credit float;
alter table course_catalog modify max_credit float;

/**
 * Update database version
 */

update application_config set value='37' where name='tmtbl.db.version';

commit;
