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
