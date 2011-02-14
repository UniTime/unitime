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

/******************************************************************************
 * For all classes without a managing department (Class_.managingDept is null), 
 * managing department is set to the controlling department (i.e., department
 * of the appropriate controlling course).
**/
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

/******************************************************************************
 * List all classes that has a wrong managing department set, i.e., all classes 
 * that are managed by a non-external department, other than the department of 
 * the subject area of the controlling course.
 **/
select 
       sa.subject_area_abbreviation || ' ' || co.course_nbr || ' ' || it.smas_abbv || ' ' || c.section_number || ss.subpart_suffix as class,
       subj_dept.dept_code as subj_dept,
       mg_dept.dept_code as manage_dept,
       sa.session_id
from
       class_ c,
       scheduling_subpart ss,
       instr_offering_config ioc,
       instructional_offering io,
       course_offering co,
       subject_area sa,
       itype_desc it,
       department subj_dept,
       department mg_dept
where 
      c.subpart_id = ss.uniqueid and
      ss.config_id = ioc.uniqueid and
      ioc.instr_offr_id = io.uniqueid and
      co.instr_offr_id = io.uniqueid and
      co.is_control = 1 and
      co.subject_area_id = sa.uniqueid and
      ss.itype = it.itype and
      sa.department_uniqueid = subj_dept.uniqueid and
      c.managing_dept = mg_dept.uniqueid and
      subj_dept.uniqueid != mg_dept.uniqueid and
      mg_dept.external_manager = 0
order by
      sa.subject_area_abbreviation, co.course_nbr, it.smas_abbv, ss.uniqueid, c.section_number;

/******************************************************************************
 * Fix the managing departments of the classes listed by the above query, i.e.,
 * set the managing department to the department of the subject area of the
 * controlling course.
 */
update class_ x set x.managing_dept = 
(
       select sa.department_uniqueid from
              scheduling_subpart ss,
              instr_offering_config ioc,
              instructional_offering io,
              course_offering co,
              subject_area sa
       where
            x.subpart_id = ss.uniqueid and
            ss.config_id = ioc.uniqueid and
            ioc.instr_offr_id = io.uniqueid and
            co.instr_offr_id = io.uniqueid and
            co.is_control = 1 and
            co.subject_area_id = sa.uniqueid
)
where x.uniqueid in
(
      select c.uniqueid from
             class_ c,
             scheduling_subpart ss,
             instr_offering_config ioc,
             instructional_offering io,
             course_offering co,
             subject_area sa,
             department d
      where
           c.subpart_id = ss.uniqueid and
           ss.config_id = ioc.uniqueid and
           ioc.instr_offr_id = io.uniqueid and
           co.instr_offr_id = io.uniqueid and
           co.is_control = 1 and
           co.subject_area_id = sa.uniqueid and
           c.managing_dept = d.uniqueid and
           d.external_manager = 0 and d.uniqueid != sa.department_uniqueid
);
commit;
