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

alter table building modify coordinate_x double precision;
alter table building modify coordinate_y double precision;
alter table room modify coordinate_x double precision;
alter table room modify coordinate_y double precision;
alter table non_university_location modify coordinate_x double precision;
alter table non_university_location modify coordinate_y double precision;
alter table external_building modify coordinate_x double precision;
alter table external_building modify coordinate_y double precision;
alter table external_room modify coordinate_x double precision;
alter table external_room modify coordinate_y double precision;

alter table building drop constraint nn_building_coordinate_x;
alter table building drop constraint nn_building_coordinate_y;
alter table room drop constraint nn_room_coordinate_x;
alter table room drop constraint nn_room_coordinate_y;
alter table non_university_location drop constraint nn_non_univ_loc_coord_x;
alter table non_university_location drop constraint nn_non_univ_loc_coord_y;

update building set coordinate_x = null where coordinate_x = -1;
update building set coordinate_y = null where coordinate_y = -1;
update room set coordinate_x = null where coordinate_x = -1;
update room set coordinate_y = null where coordinate_y = -1;
update non_university_location set coordinate_x = null where coordinate_x = -1;
update non_university_location set coordinate_y = null where coordinate_y = -1;
update external_building set coordinate_x = null where coordinate_x = -1;
update external_building set coordinate_y = null where coordinate_y = -1;
update external_room set coordinate_x = null where coordinate_x = -1;
update external_room set coordinate_y = null where coordinate_y = -1;
		
/**
 * Update database version
 */

update application_config set value='56' where name='tmtbl.db.version';

commit;
