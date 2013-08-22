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

alter table instructional_offering add req_reservation int(1) not null default 0;

create table offering_coordinator (
	offering_id decimal(20,0) not null,
	instructor_id decimal(20,0) not null,
	primary key (offering_id, instructor_id)
) engine = INNODB;

alter table offering_coordinator add constraint fk_offering_coord_offering foreign key (offering_id)
	references instructional_offering (uniqueid) on delete cascade;

alter table offering_coordinator add constraint fk_offering_coord_instructor foreign key (instructor_id)
	references departmental_instructor (uniqueid) on delete cascade;

alter table student_class_enrl add approved_date datetime null;

alter table student_class_enrl add approved_by varchar(40) null;

/*
 * Update database version
 */

update application_config set value='76' where name='tmtbl.db.version';

commit;
