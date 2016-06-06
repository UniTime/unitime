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

select 32767 * next_hi into @id from hibernate_unique_key;
select max(ord) + 1 into @ord from solver_parameter_group;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type) values
	(@id + 0, 'InstrSchd.Basic', 'Instructor Scheduling: Basic', @ord + 0, 3),
	(@id + 1, 'InstrSchd.General', 'Instructor Scheduling: General', @ord + 1, 3),
	(@id + 2, 'InstrSchd.Weight', 'Instructor Scheduling: Weights', @ord + 2, 3),
	(@id + 3, 'InstrSchd.Implementation', 'Instructor Scheduling: Implementation', @ord + 3, 3);

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id +  4, 'Basic.Mode', 'Initial', 'Solver Mode', 'enum(Initial,MPP)', 0, 1, @id),
	(@id +  5, 'Basic.WhenFinished', 'No Action', 'When Finished', 'enum(No Action,Save,Save as New,Save and Unload,Save as New and Unload)', 1, 1, @id),
	(@id +  6, 'General.CBS', 'true', 'Use conflict-based statistics', 'boolean', 0, 1, @id + 1),
	(@id +  7, 'General.CommonItypes', 'lec', 'Common Instructional Types (comma separated)', 'text', 1, 1, @id + 1),
	(@id +  8, 'General.IgnoreOtherInstructors', 'false', 'Ignore Other Instructors', 'boolean', 2, 1, @id + 1),
	(@id +  9, 'General.SaveBestUnassigned', '-1', 'Minimal number of unassigned variables to save best solution found (-1 always save)', 'integer', 3, 0, @id + 1),
	(@id + 10, 'Termination.StopWhenComplete', 'false', 'Stop computation when a complete solution is found', 'boolean', 4, 1, @id + 1),
	(@id + 11, 'Termination.TimeOut', '300', 'Maximal solver time (in sec)', 'integer', 5, 1, @id + 1),
	(@id + 12, 'Value.RandomWalkProb', '0.02', 'Randon Walk Probability', 'double', 6, 1, @id + 1),
	(@id + 13, 'Value.WeightConflicts', '1000.0', 'Conflict Weight', 'double', 0, 1, @id + 2),
	(@id + 14, 'Weight.TeachingPreferences', '10.0', 'Teaching Preference Weight', 'double', 1, 1, @id + 2),
	(@id + 15, 'Weight.AttributePreferences', '1000.0', 'Attribute Preference Weight', 'double', 2, 1, @id + 2),
	(@id + 16, 'Weight.CoursePreferences', '1.0', 'Course Preference Weight', 'double', 3, 1, @id + 2),
	(@id + 17, 'Weight.TimePreferences', '1.0', 'Time Preference Weight', 'double', 4, 1, @id + 2),
	(@id + 18, 'Weight.InstructorPreferences', '1.0', 'Instructor Preference Weight', 'double', 5, 1, @id + 2),
	(@id + 19, 'Weight.BackToBack', '1.0', 'Back-to-Back Weight', 'double', 6, 1, @id + 2),
	(@id + 20, 'BackToBack.DifferentRoomWeight', '0.8', 'Back-to-Back Different Room', 'double', 7, 1, @id + 2),
	(@id + 21, 'BackToBack.DifferentTypeWeight', '0.6', 'Back-to-Back Different Type', 'double', 8, 1, @id + 2),
	(@id + 22, 'Weight.DifferentLecture', '1000.0', 'Different Lecture Weight', 'double', 9, 1, @id + 2),
	(@id + 23, 'Weight.SameInstructor', '10.0', 'Same Instructor Weight', 'double', 10, 1, @id + 2),
	(@id + 24, 'Weight.SameLink', '100.0', 'Same Link Weight', 'double', 11, 1, @id + 2),
	(@id + 25, 'Weight.TimeOverlaps', '1000.0', 'Allowed Time Overlap Weight', 'double', 12, 1, @id + 2),
	(@id + 26, 'Weight.OriginalInstructor', '100.0', 'Original Instructor Weight (MPP)', 'double', 13, 1, @id + 2),
	(@id + 27, 'Termination.Class', 'org.cpsolver.ifs.termination.GeneralTerminationCondition', 'Termination Class', 'text', 0, 0, @id + 3),
	(@id + 28, 'Comparator.Class', 'org.cpsolver.ifs.solution.GeneralSolutionComparator', 'Comparator Class', 'text', 1, 0, @id + 3),
	(@id + 29, 'Value.Class', 'org.cpsolver.ifs.heuristics.GeneralValueSelection', 'Value Selection Class', 'text', 2, 0, @id + 3),
	(@id + 30, 'Variable.Class', 'org.cpsolver.ifs.heuristics.GeneralVariableSelection', 'Variable Selection Class', 'text', 3, 0, @id + 3),
	(@id + 31, 'Neighbour.Class', 'org.cpsolver.ifs.algorithms.SimpleSearch', 'Neighbour Selection Class', 'text', 4, 0, @id + 3);

insert into solver_predef_setting
	(uniqueid, name, description, appearance) values
	(@id + 32, 'InstrSchd.Default', 'Default', 4);

update hibernate_unique_key set next_hi=next_hi+1;		

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
