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
 
update solver_parameter_def x set x.ord = x.ord - 1 where 
	x.solver_param_group_id = (select g.uniqueid from solver_parameter_group g where g.name='ExamWeights') and
	x.ord > (select p.ord from solver_parameter_def p where p.name='Exams.NotOriginalRoomWeight');
	
delete solver_parameter_def where name='Exams.NotOriginalRoomWeight';

/**
 * Insert instructor conflict weight parameter and Exams.PerturbationWeight parameter
 **/

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.InstructorDirectConflictWeight' as name,
		  '0.0' as default_vale,
		  'Direct instructor conflict weight' as description,
		  'double' as type,
		  13 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
		  
insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.InstructorMoreThanTwoADayWeight' as name,
		  '0.0' as default_vale,
		  'Three or more exams a day instructor conflict weight' as description,
		  'double' as type,
		  14 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.InstructorBackToBackConflictWeight' as name,
		  '0.0' as default_vale,
		  'Back-to-back instructor conflict weight' as description,
		  'double' as type,
		  15 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.InstructorDistanceBackToBackConflictWeight' as name,
		  '0.0' as default_vale,
		  'Distance back-to-back instructor conflict weight' as description,
		  'double' as type,
		  16 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
		  
insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.PerturbationWeight' as name,
		  '0.001' as default_vale,
		  'Perturbation penalty weight' as description,
		  'double' as type,
		  17 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

/*
 * Update database version
 */

update application_config set value='27' where name='tmtbl.db.version';

commit;
