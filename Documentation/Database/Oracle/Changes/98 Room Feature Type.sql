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

create table feature_type (
	uniqueid number(20,0) constraint nn_feature_type_uniqueid not null,
	reference varchar2(20) constraint nn_feature_type_reference not null,
	label varchar2(60) constraint nn_feature_type_label not null,
	events number(1,0) constraint nn_feature_type_events not null
);
alter table feature_type add constraint pk_feature_type primary key (uniqueid);

alter table room_feature add feature_type_id number(20,0);

alter table room_feature
	add constraint fk_feature_type foreign key (feature_type_id)
	references feature_type (uniqueid) on delete set null;
	
insert into rights (role_id, value)
	select distinct r.role_id, 'RoomFeatureTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'ExamTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'RoomFeatureTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'ExamTypeEdit';

alter table room_feature modify label varchar2(60 char);
alter table room_feature modify abbv varchar2(60 char);
alter table room_group modify name varchar2(60 char);
alter table room_group modify abbv varchar2(60 char);

/*
 * Update database version
 */

update application_config set value='98' where name='tmtbl.db.version';

commit;
