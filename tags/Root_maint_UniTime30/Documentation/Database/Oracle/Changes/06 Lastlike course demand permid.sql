/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

/** Add index on course permanent id of LASTLIKE_COURSE_DEMAND table */
create index IDX_LL_COURSE_DEMAND_PERMID on LASTLIKE_COURSE_DEMAND(course_perm_id);
--tablespace smas_index;

/** Set course permanent id to NULL whenever set to -1 for all last-like course requests */ 
update LASTLIKE_COURSE_DEMAND set course_perm_id = null where course_perm_id = '-1';

/** Allow course permanent id of a course offering to be NULL */
alter table COURSE_OFFERING modify perm_id VARCHAR2(20) NULL;

/** Set course permanent id to null whenever set to -1 for all course offerings */
update COURSE_OFFERING set perm_id = null where perm_id = '-1';

commit;