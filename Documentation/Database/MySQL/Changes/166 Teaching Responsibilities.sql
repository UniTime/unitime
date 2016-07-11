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

create table teaching_responsibility (
	uniqueid decimal(20,0) primary key not null,
	reference varchar(20) not null,
	label varchar(60) not null,
	coordinator int(1) not null,
	instructor int(1) not null,
	abbreviation varchar(40)
) engine = INNODB;

alter table class_instructor add assign_index bigint(10) default null;
alter table class_instructor add responsibility_id decimal(20,0) default null;

alter table class_instructor 
	add constraint fk_class_teaching_responsibility foreign key (responsibility_id)
	references teaching_responsibility (uniqueid) on delete set null;

alter table offering_coordinator add responsibility_id decimal(20,0) default null;

alter table offering_coordinator 
	add constraint fk_offering_teaching_responsibility foreign key (responsibility_id)
	references teaching_responsibility (uniqueid) on delete set null;

insert into rights (role_id, value)
	select distinct r.role_id, 'TeachingResponsibilities'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevels';

insert into rights (role_id, value)
	select distinct r.role_id, 'TeachingResponsibilityEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'PreferenceLevelEdit';

/*
 * Update database version
 */

update application_config set value='166' where name='tmtbl.db.version';

commit;
