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

/** Add index on course permanent id of LASTLIKE_COURSE_DEMAND table */
create index `timetable`.`idx_ll_course_demand_permid` on `timetable`.`lastlike_course_demand`(`course_perm_id`);

/** Set course permanent id to NULL whenever set to -1 for all last-like course requests */ 
update `timetable`.`lastlike_course_demand` set `course_perm_id` = null where `course_perm_id` = '-1';

/** Allow course permanent id of a course offering to be NULL */
alter table `timetable`.`course_offering` modify `perm_id` VARCHAR(20) BINARY NULL;

/** Set course permanent id to null whenever set to -1 for all course offerings */
update `timetable`.`course_offering` set `perm_id` = null where `perm_id` = '-1';

commit;
