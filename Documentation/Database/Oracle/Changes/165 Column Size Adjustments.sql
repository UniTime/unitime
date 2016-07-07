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

alter table subject_area modify subject_area_abbreviation varchar2(40 char);
alter table pos_major modify code varchar2(40 char);
alter table pos_minor modify code varchar2(40 char);
alter table course_offering modify course_nbr varchar2(40 char);
alter table course_offering modify title varchar2(200 char);
alter table curriculum modify abbv varchar2(40 char);
alter table curriculum modify name varchar2(100 char);
alter table academic_area modify academic_area_abbreviation varchar2(40 char);
alter table academic_classification modify code varchar2(40 char);
alter table pos_major modify name varchar2(100 char);
alter table pos_minor modify name varchar2(100 char);
alter table academic_classification modify name varchar2(100 char);
alter table building modify abbreviation varchar2(20 char);
alter table room modify room_number varchar2(40 char);
alter table non_university_location modify name varchar2(40 char);
alter table external_building modify abbreviation varchar2(20 char);
alter table external_room modify room_number varchar2(40 char);

/*
 * Update database version
 */

update application_config set value='165' where name='tmtbl.db.version';

commit;
