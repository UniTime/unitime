/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
