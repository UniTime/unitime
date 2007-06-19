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

/******************************************************************************
 * For all classes without a managing department (Class_.managingDept is null), 
 * managing department is set to the controlling department (i.e., department
 * of the appropriate controlling course).
 */

update class_ c set c.managing_dept = 
(select sa.department_uniqueid from 
       scheduling_subpart ss,
       instr_offering_config ioc,
       instructional_offering io,
       course_offering co,
       subject_area sa
where 
      c.subpart_id = ss.uniqueid and
      ss.config_id = ioc.uniqueid and
      ioc.instr_offr_id = io.uniqueid and
      co.instr_offr_id = io.uniqueid and
      co.is_control = 1 and
      co.subject_area_id = sa.uniqueid
) where c.managing_dept is null;

commit;