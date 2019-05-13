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

create table map_tiles (
	z number(10,0) constraint nn_map_tiles_z not null,
	x number(10,0) constraint nn_map_tiles_x not null,
	y number(10,0) constraint nn_map_tiles_y not null,
	data blob constraint nn_map_tiles_data not null,
	time_stamp timestamp constraint nn_map_tiles_ts not null
);

alter table map_tiles add constraint pk_map_tiles primary key (z, x, y);

/*
 * Update database version
 */

update application_config set value='220' where name='tmtbl.db.version';

commit;
