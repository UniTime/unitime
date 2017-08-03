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

alter table service_provider drop column options;

alter table service_provider add session_id number(20,0);
alter table service_provider add department_id number(20,0);
alter table service_provider add all_rooms number(1) default 1;
alter table service_provider add visible number(1) default 1;

alter table service_provider add constraint fk_service_provider_session foreign key (session_id)
	references sessions (uniqueid) on delete cascade;

alter table service_provider add constraint fk_service_provider_dept foreign key (department_id)
	references department (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'EventServiceProviderEditGlobal'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'StandardEventNotesGlobalEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'EventServiceProviderEditSession'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'SponsoringOrganizationEdit';

insert into rights (role_id, value)
	select distinct r.role_id, 'EventServiceProviderEditDepartment'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'EventMeetingApprove';
	
delete from rights where value = 'EventServiceProviderEdit';

create table room_service_provider (
	location_id number(20,0) constraint nn_room_service_location not null,
	provider_id number(20,0) constraint nn_room_service_provider not null
);
alter table room_service_provider add constraint pk_room_service_provider primary key (location_id, provider_id);

alter table room_service_provider add constraint fk_room_service_loc foreign key (location_id)
	references room (uniqueid) on delete cascade;

alter table room_service_provider add constraint fk_room_service_provider foreign key (provider_id)
	references service_provider (uniqueid) on delete cascade;

create table location_service_provider (
	location_id number(20,0) constraint nn_loc_service_location not null,
	provider_id number(20,0) constraint nn_loc_service_provider not null
);
alter table location_service_provider add constraint pk_location_service_provider primary key (location_id, provider_id);

alter table location_service_provider add constraint fk_location_service_loc foreign key (location_id)
	references non_university_location (uniqueid) on delete cascade;

alter table location_service_provider add constraint fk_location_service_provider foreign key (provider_id)
	references service_provider (uniqueid) on delete cascade;

/*
 * Update database version
 */

update application_config set value='188' where name='tmtbl.db.version';

commit;
