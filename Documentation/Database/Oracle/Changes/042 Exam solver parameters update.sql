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
