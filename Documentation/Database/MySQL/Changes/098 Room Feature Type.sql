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
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	events int(1) not null
) engine = INNODB;

alter table room_feature add feature_type_id decimal(20,0);

alter table room_feature
	add constraint fk_feature_type foreign key (feature_type_id)
	references feature_type (uniqueid) on delete set null;
	
insert into rights (role_id, value)
	select distinct r.role_id, 'RoomFeatureTypes'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'ExamTypes';

insert into rights (role_id, value)
	select distinct r.role_id, 'RoomFeatureTypeEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value like 'ExamTypeEdit';

alter table room_feature modify label varchar(60) binary null;
alter table room_feature modify abbv varchar(60) binary null;
alter table room_group modify name varchar(60) binary null;
alter table room_group modify abbv varchar(60) binary null;		

/*
 * Update database version
 */

update application_config set value='98' where name='tmtbl.db.version';

commit;
