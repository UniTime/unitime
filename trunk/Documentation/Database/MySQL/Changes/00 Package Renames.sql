/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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

update `timetable`.`solver_info_def` set `implementation`=replace(`implementation`,'edu.purdue.smas','org.unitime');
update `timetable`.`solver_parameter_def` set `default_value`=replace(`default_value`,'edu.purdue.smas','org.unitime') where `default_value` like 'edu.purdue.smas%';
update `timetable`.`change_log` set `obj_type`=replace(`obj_type`,'edu.purdue.smas','org.unitime');

commit;
