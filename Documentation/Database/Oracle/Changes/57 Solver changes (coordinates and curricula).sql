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

/* deprecated parameters */
update solver_parameter_def set visible = 0, description = 'Weight last-like students (deprecated)' where name = 'General.WeightStudents';
update solver_parameter_def set visible = 0, description = 'Student Conflict: Distance Limit (after 75min class, deprecated)' where name = 'Student.DistanceLimit75min';
update solver_parameter_def set visible = 0, description = 'Student Conflict: Distance Limit (deprecated)' where name = 'Student.DistanceLimit';
update solver_parameter_def set visible = 0, description = 'Do not load committed student conflicts (deprecated)' where name = 'General.IgnoreCommittedStudentConflicts';
update solver_parameter_def set visible = 0, description = 'Students sectioning' where name = 'General.SwitchStudents';

/* cleanup test defs */
delete from solver_parameter_def where name in ('Curriculum.StudentCourseDemadsClass', 'Distances.Ellipsoid', 'Distances.Speed', 'General.LoadCommittedAssignments', 'General.CommittedStudentConflicts');

/* insert new parameters */

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Curriculum.StudentCourseDemadsClass' as name, 'Projected Student Course Demands' as default_value,
	'Student course demands' as description,
	'enum(Last Like Student Course Demands,Weighted Last Like Student Course Demands,Projected Student Course Demands,Curricula Course Demands,Curricula Last Like Course Demands,Enrolled Student Course Demands)' as type,
	15 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.CommittedStudentConflicts' as name, 'Load' as default_value,
	'Committed student conflicts' as description,
	'enum(Load,Compute,Ignore)' as type, 16 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='General');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'General.LoadCommittedAssignments' as name, 'false' as default_value,
	'Load committed assignments' as description,
	'boolean' as type, 4 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Basic');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Distances.Ellipsoid' as name, 'DEFAULT' as default_value,
	'Ellipsoid to be used to compute distances' as description,
	'enum(DEFAULT,LEGACY,WGS84,GRS80,Airy1830,Intl1924,Clarke1880,GRS67)' as type, 5 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Distance');

insert into solver_parameter_def (select solver_parameter_def_seq.nextval as uniqueid,
	'Distances.Speed' as name, '67.0' as default_value,
	'Student speed in meters per minute' as description,
	'double' as type, 6 as ord, 1 as visible,
	uniqueid as solver_param_group_id from solver_parameter_group where name='Distance');
		
/**
 * Update database version
 */

update application_config set value='57' where name='tmtbl.db.version';

commit;
		