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

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Comparator.DistStudentConflictWeight' as name,
	'0.2' as default_vale,
	'Weight of distance student conflict' as description,
	'double' as type,
	12 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Comparator');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Lecture.DistStudentConflictWeight' as name,
	'%Comparator.DistStudentConflictWeight%' as default_vale,
	'Distance student conflict weight' as description,
	'double' as type,
	20 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Variable');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight1' as name,
	'0.05' as default_vale,
	'Distance student conflict weight (level 1)' as description,
	'double' as type,
	58 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight2' as name,
	'%Comparator.DistStudentConflictWeight%' as default_vale,
	'Distance student conflict weight (level 2)' as description,
	'double' as type,
	59 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Placement.NrDistStudConfsWeight3' as name,
	'0.0' as default_vale,
	'Distance student conflict weight (level 3)' as description,
	'double' as type,
	60 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Value');

insert into solver_parameter (select solver_parameter_seq.nextval as uniqueid,
	'0.0' as value,
	d.uniqueid as solver_param_def_id,
	null as solution_id,
	s.uniqueid as solver_predef_setting_id 
	from solver_parameter_def d, solver_predef_setting s
	where d.name = 'Comparator.DistStudentConflictWeight' and s.name='Default.Check');

insert into solver_parameter (select solver_parameter_seq.nextval as uniqueid,
	'0.0' as value,
	d.uniqueid as solver_param_def_id,
	null as solution_id,
	s.uniqueid as solver_predef_setting_id 
	from solver_parameter_def d, solver_predef_setting s
	where d.name = 'Comparator.DistStudentConflictWeight' and s.name='Default.Check');
/*
 * Update database version
 */

update application_config set value='73' where name='tmtbl.db.version';

commit;
