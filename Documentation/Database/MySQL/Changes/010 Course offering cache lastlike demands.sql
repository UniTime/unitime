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

/*
 * Create column lastlike_demands in table course_offering (integer, not null, defaults to zero) 
 */
 
 alter table `timetable`.`course_offering` add `lastlike_demand` bigint(10) default 0 not null;
 
 /*
  * Compute last-like demands
  */

update `timetable`.`course_offering` co 
	set co.`lastlike_demand` = (
		select count(distinct cod.`student_id`) 
		from `timetable`.`lastlike_course_demand` cod 
		where co.`subject_area_id`=cod.`subject_area_id` and co.`course_nbr`=cod.`course_nbr`
	) where co.`perm_id` is null;

update `timetable`.`course_offering` co 
	set co.`lastlike_demand` = (
		select count(distinct cod.`student_id`) 
		from `timetable`.`lastlike_course_demand` cod, `timetable`.`subject_area` sa, `timetable`.`student` s
		where co.`perm_id`=cod.`course_perm_id` and co.`subject_area_id`=sa.`uniqueid` and cod.`student_id`=s.`uniqueid` and s.`session_id`=sa.`session_id`
	) where co.`perm_id` is not null;

/*
 * Update database version
 */

update `timetable`.`application_config` set `value`='10' where `name`='tmtbl.db.version';

commit;
