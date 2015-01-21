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
		 'Exams.RoomSplitDistanceWeight' as name,
		  '0.01' as default_vale,
		  'If an examination in split between two or more rooms, weight for an average distance between these rooms' as description,
		  'double' as type,
		  18 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargeSize' as name,
		  '-1' as default_vale,
		  'Large Exam Penalty: minimal size of a large exam (disabled if -1)' as description,
		  'integer' as type,
		  19 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
		   
insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargePeriod' as name,
		  '0.67' as default_vale,
		  'Large Exam Penalty: first discouraged period = number of periods x this factor' as description,
		  'double' as type,
		  20 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');

insert into solver_parameter_def
	(select solver_parameter_def_seq.nextval as uniqueid,
		 'Exams.LargeWeight' as name,
		  '1.0' as default_vale,
		  'Large Exam Penalty: weight of a large exam that is assigned on or after the first discouraged period' as description,
		  'double' as type,
		  21 as ord,
		  1 as visible,
		  uniqueid as solver_param_group_id from solver_parameter_group where name='ExamWeights');
 
/*
 * Update database version
 */

update application_config set value='28' where name='tmtbl.db.version';

commit;
