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


/**
 * Add an enrollment column to the class table.
 **/
alter table class_ add enrollment number(4);

/**
 * Initialize enrollment column in the class table.
 **/

update class_ c
set c.enrollment = (select count(distinct sce.student_id) 
        from student_class_enrl sce
        where sce.class_id = c.uniqueid);

/**
 * Increase size of meetings column in event note table
 **/
alter table event_note modify meetings varchar2(2000);

/**
 * Add an enrollment column to the course_offering table.
 **/
alter table course_offering add enrollment number(10);

/**
 * Initialize enrollment column in the course_offering table.
 **/
 
update course_offering co
set co.enrollment = (select count(distinct sce.student_id) 
        from student_class_enrl sce
        where sce.course_offering_id = co.uniqueid);

/*
 * Update database version
 */

update application_config set value='44' where name='tmtbl.db.version';

commit;
