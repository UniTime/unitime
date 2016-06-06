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

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'InstrSchd.Basic', 'Instructor Scheduling: Basic', '', -1, 3);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='InstrSchd.Basic';

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'InstrSchd.General', 'Instructor Scheduling: General', '', -1, 3);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='InstrSchd.General';

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'InstrSchd.Weight', 'Instructor Scheduling: Weights', '', -1, 3);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='InstrSchd.Weight';

insert into solver_parameter_group (uniqueid, name, description, condition, ord, param_type) values
	(solver_parameter_group_seq.nextval, 'InstrSchd.Implementation', 'Instructor Scheduling: Implementation', '', -1, 3);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='InstrSchd.Implementation';

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Basic.Mode' as name,
		'Initial' as default_value,
		'Solver Mode' as description,
		'enum(Initial,MPP)' as type,
		0 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Basic');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Basic.WhenFinished' as name,
		'No Action' as default_value,
		'When Finished' as description,
		'enum(No Action,Save,Save as New,Save and Unload,Save as New and Unload)' as type,
		1 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Basic');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.CBS' as name,
		'true' as default_value,
		'Use conflict-based statistics' as description,
		'boolean' as type,
		0 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.CommonItypes' as name,
		'lec' as default_value,
		'Common Instructional Types (comma separated)' as description,
		'text' as type,
		1 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.IgnoreOtherInstructors' as name,
		'false' as default_value,
		'Ignore Other Instructors' as description,
		'boolean' as type,
		2 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.SaveBestUnassigned' as name,
		'-1' as default_value,
		'Minimal number of unassigned variables to save best solution found (-1 always save)' as description,
		'integer' as type,
		3 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Termination.StopWhenComplete' as name,
		'false' as default_value,
		'Stop computation when a complete solution is found' as description,
		'boolean' as type,
		4 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Termination.TimeOut' as name,
		'300' as default_value,
		'Maximal solver time (in sec)' as description,
		'integer' as type,
		5 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Value.RandomWalkProb' as name,
		'0.02' as default_value,
		'Randon Walk Probability' as description,
		'double' as type,
		6 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.General');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Value.WeightConflicts' as name,
		'1000.0' as default_value,
		'Conflict Weight' as description,
		'double' as type,
		0 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.TeachingPreferences' as name,
		'10.0' as default_value,
		'Teaching Preference Weight' as description,
		'double' as type,
		1 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.AttributePreferences' as name,
		'1000.0' as default_value,
		'Attribute Preference Weight' as description,
		'double' as type,
		2 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.CoursePreferences' as name,
		'1.0' as default_value,
		'Course Preference Weight' as description,
		'double' as type,
		3 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.TimePreferences' as name,
		'1.0' as default_value,
		'Time Preference Weight' as description,
		'double' as type,
		4 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.InstructorPreferences' as name,
		'1.0' as default_value,
		'Instructor Preference Weight' as description,
		'double' as type,
		5 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.BackToBack' as name,
		'1.0' as default_value,
		'Back-to-Back Weight' as description,
		'double' as type,
		6 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'BackToBack.DifferentRoomWeight' as name,
		'0.8' as default_value,
		'Back-to-Back Different Room' as description,
		'double' as type,
		7 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'BackToBack.DifferentTypeWeight' as name,
		'0.6' as default_value,
		'Back-to-Back Different Type' as description,
		'double' as type,
		8 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.DifferentLecture' as name,
		'1000.0' as default_value,
		'Different Lecture Weight' as description,
		'double' as type,
		9 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.SameInstructor' as name,
		'10.0' as default_value,
		'Same Instructor Weight' as description,
		'double' as type,
		0 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.SameLink' as name,
		'100.0' as default_value,
		'Same Link Weight' as description,
		'double' as type,
		1 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.TimeOverlaps' as name,
		'1000.0' as default_value,
		'Allowed Time Overlap Weight' as description,
		'double' as type,
		2 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Weight.OriginalInstructor' as name,
		'100.0' as default_value,
		'Original Instructor Weight (MPP)' as description,
		'double' as type,
		3 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Weight');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Termination.Class' as name,
		'org.cpsolver.ifs.termination.GeneralTerminationCondition' as default_value,
		'Termination Class' as description,
		'text' as type,
		0 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Implementation');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Comparator.Class' as name,
		'org.cpsolver.ifs.solution.GeneralSolutionComparator' as default_value,
		'Comparator Class' as description,
		'text' as type,
		1 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Implementation');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Value.Class' as name,
		'org.cpsolver.ifs.heuristics.GeneralValueSelection' as default_value,
		'Value Selection Class' as description,
		'text' as type,
		2 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Implementation');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Variable.Class' as name,
		'org.cpsolver.ifs.heuristics.GeneralVariableSelection' as default_value,
		'Variable Selection Class' as description,
		'text' as type,
		3 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Implementation');


insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Neighbour.Class' as name,
		'org.cpsolver.ifs.algorithms.SimpleSearch' as default_value,
		'Neighbour Selection Class' as description,
		'text' as type,
		4 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'InstrSchd.Implementation');

insert into solver_predef_setting (uniqueid, name, description, appearance) values
	(solver_predef_setting_seq.nextval, 'InstrSchd.Default', 'Default', 4);

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorScheduling'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'CourseTimetabling';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorSchedulingSolver'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'Solver';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorSchedulingSolverLog'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'SolverLog';

insert into rights (role_id, value)
	select distinct r.role_id, 'InstructorSchedulingSolutionExportXml'
	from roles r, rights g where g.role_id = r.role_id and g.value = 'SolverSolutionExportXml';

/*
 * Update database version
 */

update application_config set value='164' where name='tmtbl.db.version';

commit;
