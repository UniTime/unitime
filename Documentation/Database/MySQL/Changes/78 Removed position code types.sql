/*
 * UniTime 3.3 (University Timetabling Application)
 * Copyright (C) 2008 - 2011, UniTime LLC
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

alter table staff add pos_type decimal(20,0) null;

alter table staff add constraint fk_staff_pos_type foreign key (pos_type)
	references position_type(uniqueid) on delete set null;

update staff s set s.pos_type =
	(select c.pos_code_type from position_code_to_type c where s.pos_code = c.position_code)
	where s.pos_type is not null;

drop table position_code_to_type;

alter table offr_consent_type add abbv varchar(20);

update offr_consent_type set reference = 'IN' where reference = 'instructor';
update offr_consent_type set reference = 'DP' where reference = 'department';
update offr_consent_type set abbv = 'Instructor' where reference = 'instructor' or reference = 'IN';
update offr_consent_type set abbv = 'Department' where reference = 'department' or reference = 'DP';

alter table offr_consent_type add constraint nn_offr_consent_abbv check (abbv is not null);

/*
 * Update database version
 */

update application_config set value='78' where name='tmtbl.db.version';

commit;
