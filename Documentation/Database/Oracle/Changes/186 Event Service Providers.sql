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

create table service_provider (
	uniqueid number(20,0) constraint nn_service_provider_id not null,
	reference varchar2(20 char) constraint nn_service_provider_ref not null,
	label varchar2(60 char) constraint nn_service_provider_label not null,
	note varchar2(1000 char),
	email varchar2(200 char),
	options number(10,0) constraint nn_service_provider_opt not null default 0
);
alter table service_provider add constraint pk_service_provider primary key (uniqueid);

create table event_service_provider (
	event_id number(20,0) constraint nn_evt_service_event not null,
	provider_id number(20,0) constraint nn_evt_service_provider not null
);
alter table event_service_provider add constraint pk_event_service_provider primary key (exam_id, location_id);

alter table event_service_provider add constraint fk_evt_service_event foreign key (event_id)
	references event (uniqueid) on delete cascade;

alter table event_service_provider add constraint fk_evt_service_provider foreign key (provider_id)
	references service_provider (uniqueid) on delete cascade;

insert into rights (role_id, value)
	select distinct r.role_id, 'EventServiceProviders'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'SponsoringOrganizations';

insert into rights (role_id, value)
	select distinct r.role_id, 'EventServiceProviderEdit'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'SponsoringOrganizationEdit';

/*
 * Update database version
 */

update application_config set value='186' where name='tmtbl.db.version';

commit;
