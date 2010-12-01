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

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.PeriodSizeWeight' as name,
		  '1.0' as default_vale,
		  'Examination period x examination size weight' as description,
		  'double' as type,
		  22 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.PeriodIndexWeight' as name,
		  '0.0000001' as default_vale,
		  'Examination period index weight' as description,
		  'double' as type,
		  23 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.RoomPerturbationWeight' as name,
		  '0.1' as default_vale,
		  'Room perturbation penalty (change of room) weight' as description,
		  'double' as type,
		  24 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
 
insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Comparator.Class' as name,
		  'net.sf.cpsolver.ifs.solution.GeneralSolutionComparator' as default_vale,
		  'Examination solution comparator class' as description,
		  'text' as type,
		  6 as ord,
		  0 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='Exam');

/*
 * Update database version
 */

update application_config set value='42' where name='tmtbl.db.version';

commit;
