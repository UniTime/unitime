/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2009 - 2010, UniTime LLC
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
 * Add a new index
 */

create index idx_student_enrl_assignment on student_enrl(solution_id,class_id);

/**
 * Add new solver parameters
 */

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.IgnoreCommittedStudentConflicts' as name, 'false' as default_vale, 'Do not load committed student conflicts' as description,
	'boolean' as type, 13 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.WeightStudents' as name, 'true' as default_vale, 'Weight last-like students' as description,
	'boolean' as type, 14 as ord, 0 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

/*
 * Update database version
 */

update application_config set value='46' where name='tmtbl.db.version';

commit;
