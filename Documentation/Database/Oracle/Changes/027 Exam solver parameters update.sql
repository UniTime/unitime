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
