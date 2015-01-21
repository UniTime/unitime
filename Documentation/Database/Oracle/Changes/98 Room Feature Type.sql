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
