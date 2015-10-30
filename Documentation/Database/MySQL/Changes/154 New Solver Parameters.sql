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

select uniqueid into @gbas from solver_parameter_group where name='StudentSctBasic';
select max(ord) into @obas from solver_parameter_def where solver_param_group_id=@gbas;
select uniqueid into @ggen from solver_parameter_group where name='StudentSct';
select max(ord) into @ogen from solver_parameter_def where solver_param_group_id=@ggen;
select uniqueid into @gsw from solver_parameter_group where name='StudentSctWeights';
select max(ord) into @osw from solver_parameter_def where solver_param_group_id=@gsw;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type)
	values (@id + 0, 'StudentSctOnline', 'Online Student Scheduling', @ord, 2);

insert into solver_parameter_def
	(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
	(@id +  1, 'Load.StudentQuery', '', 'Student Filter', 'text', @obas + 1, 1, @gbas),
	(@id +  2, 'Interactive.UpdateCourseRequests', 'true', 'Update course requests', 'boolean', @ogen + 1, 1, @ggen),
	(@id +  3, 'Load.RequestGroups', 'false', 'Load request groups', 'boolean', @ogen + 2, 1, @ggen),
	(@id +  4, 'StudentWeights.SameGroup', '0.1000', 'Same request group', 'double', @osw + 1, 1, @gsw),
	(@id +  5, 'Sectioning.KeepInitialAssignments', 'false', 'MPP: Initial enrollment must be assigned', 'boolean', @ogen + 3, 1, @ggen), 
	(@id +  6, 'StudentWeights.Perturbation', '0.1000', 'MPP: Perturbation weight', 'double', @osw + 2, 1, @gsw),
	(@id +  7, 'StudentWeights.SameChoice', '0.900', 'MPP: Different section, but same time and instructor', 'double', @osw + 3, 1, @gsw),
	(@id +  8, 'StudentWeights.SameTime', '0.700', 'MPP: Different section, but same time', 'double', @osw + 4, 1, @gsw),
	(@id +  9, 'Load.CheckEnabledForScheduling', 'true', 'Check enabled for scheduling toggle', 'boolean', @ogen + 3, 1, @ggen),
	(@id + 10, 'Load.CheckForNoBatchStatus', 'true', 'Check no-batch student status', 'boolean', @ogen + 4, 1, @ggen),
	(@id + 11, 'StudentWeights.NoTimeFactor', '0.050', 'Additional Weights: Section with no time', 'double', 0, 1, @id),
	(@id + 12, 'StudentWeights.SelectionFactor', '0.125', 'Additional Weights: Section selected', 'double', 1, 1, @id),
	(@id + 13, 'StudentWeights.PenaltyFactor', '0.250', 'Additional Weights: Section over-expected', 'double', 2, 1, @id),
	(@id + 14, 'StudentWeights.AvgPenaltyFactor', '0.001', 'Additional Weights: Average penalty', 'double', 3, 1, @id),
	(@id + 15, 'StudentWeights.AvailabilityFactor', '0.050', 'Additional Weights: Section availability', 'double', 4, 1, @id),
	(@id + 16, 'StudentWeights.Class', 'org.cpsolver.studentsct.online.selection.StudentSchedulingAssistantWeights', 'Student weights model', 'text', 5, 0, @id),
	(@id + 17, 'OverExpectedCriterion.Class', 'org.cpsolver.studentsct.online.expectations.AvoidUnbalancedWhenNoExpectations', 'Over-expected criterion', 'text', 6, 0, @id),
	(@id + 18, 'Suggestions.Timeout', '1000', 'Suggestions: Time limit in milliseconds', 'integer', 7, 1, @id),
	(@id + 19, 'Suggestions.MaxDepth', '4', 'Suggestions: Maximal search depth', 'integer', 8, 1, @id),
	(@id + 20, 'Suggestions.MaxSuggestions', '20', 'Suggestions: Number of results', 'integer', 9, 1, @id),
	(@id + 21, 'StudentWeights.MultiCriteria', 'true', 'Use multi-criterion selection', 'boolean', 10, 1, @id),
	(@id + 22, 'OverExpected.Disbalance', '0.100', 'Expectations: Allowed dis-balance', 'double', 11, 1, @id),
	(@id + 23, 'General.BalanceUnlimited', 'false', 'Expectations: Balance unlimited sections', 'boolean', 12, 1, @id),
	(@id + 24, 'OverExpected.Percentage', '1.000', 'Expectations: Expectation multiplicator', 'double', 13, 1, @id),
	(@id + 25, 'OverExpected.Rounding', 'ROUND', 'Expectations: rounding', 'enum(NONE,CEIL,FLOOR,ROUND)', 14 , 1, @id),
	(@id + 26, 'OnlineStudentSectioning.TimesToAvoidHeuristics', 'true', 'Online Selection: avoid times needed by other courses', 'boolean', 15, 1, @id),
	(@id + 27, 'StudentWeights.SameChoiceFactor', '0.125', 'Resectioning: Same choice (time and instructor)', 'double', 16, 1, @id),
	(@id + 28, 'StudentWeights.SameRoomsFactor', '0.007', 'Resectioning: Same room', 'double', 17, 1, @id),
	(@id + 29, 'StudentWeights.SameTimeFactor', '0.070', 'Resectioning: Same time', 'double', 18, 1, @id),
	(@id + 30, 'StudentWeights.SameNameFactor', '0.014', 'Resectioning: Same section name', 'double', 19, 1, @id),
	(@id + 31, 'StudentWeights.PreferenceFactor', '0.500', 'Suggestions: Preferred section', 'double', 20, 1, @id),
	(@id + 32, 'Enrollment.CanKeepCancelledClass', 'false', 'Can a student keep cancelled class', 'boolean', 21, 1, @id);

update hibernate_unique_key set next_hi=next_hi+1;

/*
 * Update database version
 */

update application_config set value='154' where name='tmtbl.db.version';

commit;
