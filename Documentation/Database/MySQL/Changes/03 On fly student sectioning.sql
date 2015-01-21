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

select 32767 * `next_hi` into @id from `timetable`.`hibernate_unique_key`;

select max(`ord`)+1 into @ord from `timetable`.`solver_parameter_group`;

insert into `timetable`.`solver_parameter_group`
	(`uniqueid`, `name`, `description`, `condition`, `ord`) values 
	(@id, 'OnFlySectioning', 'On Fly Student Sectioning', '', @ord);
	
insert into `timetable`.`solver_parameter_def`
	(`uniqueid`, `name`, `default_value`, `description`, `type`, `ord`, `visible`, `solver_param_group_id`) values
	(@id+1, 'OnFlySectioning.Enabled', 'false', 'Enable on fly sectioning (if enabled, students will be resectioned after each iteration)', 'boolean', 0, 1, @id),
	(@id+2, 'OnFlySectioning.Recursive', 'true', 'Recursively resection lectures affected by a student swap', 'boolean', 1, 1, @id),
	(@id+3, 'OnFlySectioning.ConfigAsWell', 'false', 'Resection students between configurations as well', 'boolean', 2, 1, @id); 

update `timetable`.`hibernate_unique_key` set `next_hi`=`next_hi`+1;
		
/* 
-- Uncomment the following lines to enable on fly student sectioning in the default solver configuration

select s.`uniqueid` into @ds from `timetable`.`solver_predef_setting` s where s.`name`='Default.Solver';
insert into `timetable`.`solver_parameter`
	(`uniqueid`, `value`, `solver_param_def_id`, `solution_id`, `solver_predef_setting_id`) values
	(@id+4, 'on', @id+1, NULL, @ds);

*/

commit;
