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
