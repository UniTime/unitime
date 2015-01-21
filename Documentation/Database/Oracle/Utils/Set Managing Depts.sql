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
