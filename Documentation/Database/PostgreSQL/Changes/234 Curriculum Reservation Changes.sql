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

create table reservation_minor (
	reservation_id bigint not null,
	minor_id bigint not null
);
alter table reservation_minor add constraint pk_reservation_minor primary key (reservation_id, minor_id);

alter table reservation_minor add constraint fk_res_minors_minor foreign key (minor_id)
	references pos_minor (uniqueid) on delete cascade;
alter table reservation_minor add constraint fk_res_minors_reservation foreign key (reservation_id)
	references reservation (uniqueid) on delete cascade;

create table reservation_acad_area (
	reservation_id bigint not null,
	area_id bigint not null
);
alter table reservation_acad_area add constraint pk_reservation_acad_area primary key (reservation_id, area_id);

alter table reservation_acad_area add constraint fk_res_acad_areas_area foreign key (area_id)
	references academic_area (uniqueid) on delete cascade;
alter table reservation_acad_area add constraint fk_res_acad_areas_reservation foreign key (reservation_id)
	references reservation (uniqueid) on delete cascade;

insert into reservation_acad_area
	select uniqueid, area_id from reservation where area_id is not null;

alter table reservation drop constraint fk_reservation_area;
alter table reservation drop column area_id;

/*
 * Update database version
 */

update application_config set value='234' where name='tmtbl.db.version';

commit;
