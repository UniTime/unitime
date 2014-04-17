/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC
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

alter table course_credit_unit_config add course_id number(20,0);
update course_credit_unit_config g set g.course_id = (select c.uniqueid from course_offering c where c.is_control = 1 and c.instr_offr_id = g.instr_offr_id) where g.instr_offr_id is not null;
alter table course_credit_unit_config add constraint fk_crs_crdt_unit_cfg_crs_own foreign key (course_id) references course_offering(uniqueid);
alter table course_credit_unit_config drop constraint fk_crs_crdt_unit_cfg_io_own;
alter table course_credit_unit_config drop column instr_offr_id;

/*
 * Update database version
 */

update application_config set value='135' where name='tmtbl.db.version';

commit;
