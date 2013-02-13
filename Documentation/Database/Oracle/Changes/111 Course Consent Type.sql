/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC
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

alter table course_offering add consent_type number(20,0);

alter table course_offering add constraint fk_course_consent_type foreign key (consent_type)
	references offr_consent_type (uniqueid) on delete cascade;

update course_offering set consent_type = (select o.consent_type from instructional_offering o where o.uniqueid = instr_offr_id);
		
alter table instructional_offering drop constraint fk_instr_offr_consent_type;

alter table instructional_offering drop column consent_type;

/*
 * Update database version
 */

update application_config set value='111' where name='tmtbl.db.version';

commit;
