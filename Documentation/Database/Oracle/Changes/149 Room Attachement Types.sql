/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/


create table attachment_type (
	uniqueid number(20,0) constraint nn_attchtype_id not null,
	reference varchar2(20 char) constraint nn_attchtype_ref not null,
	abbreviation varchar2(20 char) constraint nn_attchtype_abbv not null,
	label varchar2(60 char) constraint nn_attchtype_label not null,
	visibility number(10) constraint nn_attchtype_visibility not null
);
alter table attachment_type add constraint pk_attachment_type primary key (uniqueid);

alter table room_picture add type_id number(20,0);
alter table location_picture add type_id number(20,0);

insert into attachment_type
	(uniqueid, reference, abbreviation, label, visibility) values
	(ref_table_seq.nextval, 'OTHER', 'Other', 'Not Specified', 6);
update room_picture set type_id = ref_table_seq.currval where content_type not like 'image/%';
update location_picture set type_id = ref_table_seq.currval where content_type not like 'image/%';
insert into attachment_type
	(uniqueid, reference, abbreviation, label, visibility) values
	(ref_table_seq.nextval, 'PICTURE', 'Picture', 'Room Picture', 15);
update room_picture set type_id = ref_table_seq.currval where content_type like 'image/%';
update location_picture set type_id = ref_table_seq.currval where content_type like 'image/%';

alter table room_picture add constraint fk_room_picture_type foreign key (type_id)
	references attachment_type (uniqueid) on delete set null;
alter table location_picture add constraint fk_location_picture_type foreign key (type_id)
	references attachment_type (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'AttachementTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'AttachementTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTypeEdit';

/*
 * Update database version
 */

update application_config set value='149' where name='tmtbl.db.version';

commit;
