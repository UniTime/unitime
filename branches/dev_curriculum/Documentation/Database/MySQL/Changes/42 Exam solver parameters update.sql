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
 * Add Exams.RoomSplitDistanceWeight and Exam.Large weights
 **/
select uniqueid into @gid from solver_parameter_group where name='ExamWeights';

select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;
 
select 32767 * next_hi into @id from hibernate_unique_key;
 
insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
 			(@id+0, 'Exams.PeriodSizeWeight', '1.0', 'Examination period x examination size weight', 'double', @ord+1, 1, @gid),
			(@id+1, 'Exams.PeriodIndexWeight', '0.0000001', 'Examination period index weight', 'integer', @ord+2, 1, @gid),
			(@id+2, 'Exams.RoomPerturbationWeight', '0.01', 'Room perturbation penalty (change of room) weight', 'double', @ord+3, 1, @gid);
			
select uniqueid into @gid from solver_parameter_group where name='Exam';

select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;

insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
 			(@id+3, 'Comparator.Class', 'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator', 'Examination solution comparator class', 'text', @ord+1, 0, @gid);

update hibernate_unique_key set next_hi=next_hi+1;
 
/*
 * Update database version
 */

update application_config set value='42' where name='tmtbl.db.version';

commit;
