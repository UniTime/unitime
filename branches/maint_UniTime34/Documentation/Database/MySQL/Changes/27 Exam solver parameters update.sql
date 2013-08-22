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
 * Delete parameter Exams.NotOriginalRoomWeight
 **/
select uniqueid into @gid from solver_parameter_group where name='ExamWeights';

select ord into @ord from solver_parameter_def where name='Exams.NotOriginalRoomWeight';

delete from solver_parameter_def where name='Exams.NotOriginalRoomWeight';

update solver_parameter_def set ord = ord - 1 where solver_param_group_id=@gid and ord>@ord;

/**
 * Insert instructor conflict weight parameter and Exams.PerturbationWeight parameter
 **/
 
select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;
 
select 32767 * next_hi into @id from hibernate_unique_key;
 
insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
 			(@id+0, 'Exams.InstructorDirectConflictWeight', '0.0', 'Direct instructor conflict weight', 'double', @ord+1, 1, @gid),
			(@id+1, 'Exams.InstructorMoreThanTwoADayWeight', '0.0', 'Three or more exams a day instructor conflict weight', 'double', @ord+2, 1, @gid),
			(@id+2, 'Exams.InstructorBackToBackConflictWeight', '0.0', 'Back-to-back instructor conflict weight', 'double', @ord+3, 1, @gid),
			(@id+3, 'Exams.InstructorDistanceBackToBackConflictWeight', '0.0', 'Distance back-to-back instructor conflict weight', 'double', @ord+4, 1, @gid),
			(@id+4, 'Exams.PerturbationWeight', '0.01', 'Perturbation penalty weight', 'double', @ord+5, 1, @gid);
			
update hibernate_unique_key set next_hi=next_hi+1;
 
/*
 * Update database version
 */

update application_config set value='27' where name='tmtbl.db.version';

commit;
