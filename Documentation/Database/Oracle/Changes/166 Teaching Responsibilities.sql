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
	uniqueid number(20,0) constraint nn_responsibility_uniqueid not null,
	reference varchar2(20 char) constraint nn_responsibility_reference not null,
	label varchar2(60 char) constraint nn_responsibility_label not null,
	coordinator number(1,0) constraint nn_responsibility_coordinator not null,
	instructor number(1,0) constraint nn_responsibility_instructor not null,
	abbreviation varchar2(40 char)
);
alter table teaching_responsibility add constraint pk_teaching_responsibility primary key (uniqueid);

alter table class_instructor add assign_index number(10,0);
alter table class_instructor add responsibility_id number(20,0);

alter table class_instructor 
	add constraint fk_class_teaching_responsibility foreign key (responsibility_id)
	references teaching_responsibility (uniqueid) on delete set null;

alter table offering_coordinator add responsibility_id number(20,0);

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
