/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC
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

alter table course_offering drop constraint fk_course_offering_demand_offr;

alter table course_offering add constraint fk_course_offering_demand_offr foreign key (demand_offering_id)
	references course_offering (uniqueid) on delete set null;

		
/*
 * Update database version
 */

update application_config set value='96' where name='tmtbl.db.version';

commit;
