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

select uniqueid into @gid from solver_parameter_group where name='General';

select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;
 
select 32767 * next_hi into @id from hibernate_unique_key;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id+0, 'General.IgnoreCommittedStudentConflicts', 'false', 'Do not load committed student conflicts', 'boolean', @ord+1, 1, @gid),
	(@id+1, 'General.WeightStudents', 'true', 'Weight last-like students', 'boolean', @ord+2, 0, @gid);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='46' where name='tmtbl.db.version';

commit;
