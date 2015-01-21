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
select 32767 * next_hi into @id from hibernate_unique_key;

select uniqueid into @gid_basic from solver_parameter_group where name='Basic';
select max(ord) into @ord_basic from solver_parameter_def where solver_param_group_id=@gid_basic;
select uniqueid into @gid_general from solver_parameter_group where name='General';
select max(ord) into @ord_general from solver_parameter_def where solver_param_group_id=@gid_general;
select uniqueid into @gid_distance from solver_parameter_group where name='Distance';
select max(ord) into @ord_distance from solver_parameter_def where solver_param_group_id=@gid_distance;

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id, 'Curriculum.StudentCourseDemadsClass', 'Projected Student Course Demands', 'Student course demands', 'enum(Last Like Student Course Demands,Weighted Last Like Student Course Demands,Projected Student Course Demands,Curricula Course Demands,Curricula Last Like Course Demands,Enrolled Student Course Demands)', @ord_general+1, 1, @gid_general),
	(@id+1, 'General.CommittedStudentConflicts', 'Load', 'Committed student conflicts', 'enum(Load,Compute,Ignore)',  @ord_general+2, 1, @gid_general),
	(@id+2, 'Distances.Ellipsoid', 'DEFAULT', 'Ellipsoid to be used to compute distances', 'enum(DEFAULT,LEGACY,WGS84,GRS80,Airy1830,Intl1924,Clarke1880,GRS67)', @ord_distance+1, 1, @gid_distance),
	(@id+3, 'Distances.Speed', '67.0', 'Student speed in meters per minute', 'double', @ord_distance+2, 1, @gid_distance),
	(@id+4, 'General.LoadCommittedAssignments', 'false', 'Load committed assignments', 'boolean', @ord_basic+1, 1, @gid_basic);

update hibernate_unique_key set next_hi=next_hi+1;
		
/**
 * Update database version
 */

update application_config set value='57' where name='tmtbl.db.version';

commit;
		