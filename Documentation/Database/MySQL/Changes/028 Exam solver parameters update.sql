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
select uniqueid into @gid from solver_parameter_group where name='ExamWeights';

select max(ord) into @ord from solver_parameter_def where solver_param_group_id=@gid;
 
select 32767 * next_hi into @id from hibernate_unique_key;
 
insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
 			(@id+0, 'Exams.RoomSplitDistanceWeight', '0.01', 'If an examination in split between two or more rooms, weight for an average distance between these rooms', 'double', @ord+1, 1, @gid),
			(@id+1, 'Exams.LargeSize', '-1', 'Large Exam Penalty: minimal size of a large exam (disabled if -1)', 'integer', @ord+2, 1, @gid),
			(@id+2, 'Exams.LargePeriod', '0.67', 'Large Exam Penalty: first discouraged period = number of periods x this factor', 'double', @ord+3, 1, @gid),
			(@id+3, 'Exams.LargeWeight', '1.0', 'Large Exam Penalty: weight of a large exam that is assigned on or after the first discouraged period', 'double', @ord+4, 1, @gid);
			
update hibernate_unique_key set next_hi=next_hi+1;
 
/*
 * Update database version
 */

update application_config set value='28' where name='tmtbl.db.version';

commit;
