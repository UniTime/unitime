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
create index IDX_LL_COURSE_DEMAND_PERMID on LASTLIKE_COURSE_DEMAND(course_perm_id);
--tablespace smas_index;

/** Set course permanent id to NULL whenever set to -1 for all last-like course requests */ 
update LASTLIKE_COURSE_DEMAND set course_perm_id = null where course_perm_id = '-1';

/** Allow course permanent id of a course offering to be NULL */
alter table COURSE_OFFERING modify perm_id VARCHAR2(20) NULL;

/** Set course permanent id to null whenever set to -1 for all course offerings */
update COURSE_OFFERING set perm_id = null where perm_id = '-1';

commit;
