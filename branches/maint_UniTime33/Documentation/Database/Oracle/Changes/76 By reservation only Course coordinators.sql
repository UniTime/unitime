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

alter table instructional_offering add req_reservation number(1) default 0;
alter table instructional_offering add constraint nn_instr_offering_by_reserv check (req_reservation is not null);

create table offering_coordinator (
 	offering_id number(20,0) constraint nn_offering_coord_offering not null,
 	instructor_id number(20,0) constraint nn_offering_coord_instructor not null
);
alter table offering_coordinator add constraint pk_offering_coordinator primary key (offering_id, instructor_id);

alter table offering_coordinator add constraint fk_offering_coord_offering foreign key (offering_id)
	references instructional_offering (uniqueid) on delete cascade;

alter table offering_coordinator add constraint fk_offering_coord_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

alter table student_class_enrl add approved_date date;

alter table student_class_enrl add approved_by varchar2(40);

/*
 * Update database version
 */

update application_config set value='76' where name='tmtbl.db.version';

commit;
