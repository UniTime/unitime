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
	(solver_parameter_group_seq.nextval, 'StudentSctOnline', 'Online Student Scheduling', -1, 2);
update solver_parameter_group g set g.ord = ( select max(x.ord)+1 from solver_parameter_group x ) where g.name='StudentSctOnline';

insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Load.StudentQuery' as name,
		'' as default_value,
		'Student Filter' as description,
		'text' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctBasic') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctBasic');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Interactive.UpdateCourseRequests' as name,
		'true' as default_value,
		'Update course requests' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Load.RequestGroups' as name,
		'false' as default_value,
		'Load request groups' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameGroup' as name,
		'0.1000' as default_value,
		'Same request group' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Sectioning.KeepInitialAssignments' as name,
		'false' as default_value,
		'MPP: Initial enrollment must be assigned' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct'); 
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.Perturbation' as name,
		'0.1000' as default_value,
		'MPP: Perturbation weight' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameChoice' as name,
		'0.900' as default_value,
		'MPP: Different section, but same time and instructor' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameTime' as name,
		'0.700' as default_value,
		'MPP: Different section, but same time' as description,
		'double' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSctWeights') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctWeights');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Load.CheckEnabledForScheduling' as name,
		'true' as default_value,
		'Check enabled for scheduling toggle' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Load.CheckForNoBatchStatus' as name,
		'true' as default_value,
		'Check no-batch student status' as description,
		'boolean' as type,
		(select count(*) from solver_parameter_def d, solver_parameter_group g where d.solver_param_group_id = g.uniqueid and g.name = 'StudentSct') as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSct');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.NoTimeFactor' as name,
		'0.050' as default_value,
		'Additional Weights: Section with no time' as description,
		'double' as type,
		0 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SelectionFactor' as name,
		'0.125' as default_value,
		'Additional Weights: Section selected' as description,
		'double' as type,
		1 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.PenaltyFactor' as name,
		'0.250' as default_value,
		'Additional Weights: Section over-expected' as description,
		'double' as type,
		2 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.AvgPenaltyFactor' as name,
		'0.001' as default_value,
		'Additional Weights: Average penalty' as description,
		'double' as type,
		3 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.AvailabilityFactor' as name,
		'0.050' as default_value,
		'Additional Weights: Section availability' as description,
		'double' as type,
		4 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.Class' as name,
		'org.cpsolver.studentsct.online.selection.StudentSchedulingAssistantWeights' as default_value,
		'Student weights model' as description,
		'text' as type,
		5 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'OverExpectedCriterion.Class' as name,
		'org.cpsolver.studentsct.online.expectations.AvoidUnbalancedWhenNoExpectations' as default_value,
		'Over-expected criterion' as description,
		'text' as type,
		6 as ord,
		0 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Suggestions.Timeout' as name,
		'1000' as default_value,
		'Suggestions: Time limit in milliseconds' as description,
		'integer' as type,
		7 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Suggestions.MaxDepth' as name,
		'4' as default_value,
		'Suggestions: Maximal search depth' as description,
		'integer' as type,
		8 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Suggestions.MaxSuggestions' as name,
		'20' as default_value,
		'Suggestions: Number of results' as description,
		'integer' as type,
		9 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.MultiCriteria' as name,
		'true' as default_value,
		'Use multi-criterion selection' as description,
		'boolean' as type,
		10 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'OverExpected.Disbalance' as name,
		'0.100' as default_value,
		'Expectations: Allowed dis-balance' as description,
		'double' as type,
		11 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'General.BalanceUnlimited' as name,
		'false' as default_value,
		'Expectations: Balance unlimited sections' as description,
		'boolean' as type,
		12 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'OverExpected.Percentage' as name,
		'1.000' as default_value,
		'Expectations: Expectation multiplicator' as description,
		'double' as type,
		13 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'OverExpected.Rounding' as name,
		'ROUND' as default_value,
		'Expectations: rounding' as description,
		'enum(NONE,CEIL,FLOOR,ROUND)' as type,
		14 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'OnlineStudentSectioning.TimesToAvoidHeuristics' as name,
		'true' as default_value,
		'Online Selection: avoid times needed by other courses' as description,
		'boolean' as type,
		15 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameChoiceFactor' as name,
		'0.125' as default_value,
		'Resectioning: Same choice (time and instructor)' as description,
		'double' as type,
		16 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameRoomsFactor' as name,
		'0.007' as default_value,
		'Resectioning: Same room' as description,
		'double' as type,
		17 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameTimeFactor' as name,
		'0.070' as default_value,
		'Resectioning: Same time' as description,
		'double' as type,
		18 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.SameNameFactor' as name,
		'0.014' as default_value,
		'Resectioning: Same section name' as description,
		'double' as type,
		19 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'StudentWeights.PreferenceFactor' as name,
		'0.500' as default_value,
		'Suggestions: Preferred section' as description,
		'double' as type,
		20 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');
insert into solver_parameter_def
	(select
		solver_parameter_def_seq.nextval as uniqueid,
		'Enrollment.CanKeepCancelledClass' as name,
		'false' as default_value,
		'Can a student keep cancelled class' as description,
		'boolean' as type,
		21 as ord,
		1 as visible,
		uniqueid as solver_param_group_id from solver_parameter_group where name = 'StudentSctOnline');

/*
 * Update database version
 */

update application_config set value='154' where name='tmtbl.db.version';

commit;
