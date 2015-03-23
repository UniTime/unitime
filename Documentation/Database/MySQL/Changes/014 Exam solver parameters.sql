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

alter table solver_parameter_group add param_type bigint(10) default 0;

select 32767 * next_hi into @id from hibernate_unique_key;

select max(ord)+1 into @ord from solver_parameter_group;

insert into solver_parameter_group (uniqueid, name, description, ord, param_type) values
			(@id, 'ExamBasic', 'Basic Parameters', @ord, 1),
			(@id+1, 'ExamWeights', 'Examination Weights', @ord+1, 1),
			(@id+2, 'Exam', 'General Parameters', @ord+2, 1),
			(@id+3, 'ExamGD', 'Great Deluge Parameters', @ord+3, 1),
			(@id+4, 'ExamSA', 'Simulated Annealing Parameters', @ord+4, 1);

insert into solver_parameter_def
			(uniqueid, name, default_value, description, type, ord, visible, solver_param_group_id) values
			(@id+5, 'ExamBasic.Mode', 'Initial', 'Solver mode', 'enum(Initial,MPP)', 0, 1, @id),
			(@id+6, 'ExamBasic.WhenFinished', 'No Action', 'When finished', 'enum(No Action,Save,Save and Unload)', 1, 1, @id),
			(@id+7, 'Exams.MaxRooms', '4', 'Default number of room splits per exam', 'integer', 0, 1, @id+1),
			(@id+8, 'Exams.IsDayBreakBackToBack', 'false', 'Consider back-to-back over day break', 'boolean', 1, 1, @id+1),
			(@id+9, 'Exams.DirectConflictWeight', '1000.0', 'Direct conflict weight', 'double', 2, 1, @id+1),
			(@id+10, 'Exams.MoreThanTwoADayWeight', '100.0', 'Three or more exams a day conflict weight', 'double', 3, 1, @id+1),
			(@id+11, 'Exams.BackToBackConflictWeight', '10.0', 'Back-to-back conflict weight', 'double', 4, 1, @id+1),
			(@id+12, 'Exams.DistanceBackToBackConflictWeight', '25.0', 'Distance back-to-back conflict weight', 'double', 5, 1, @id+1),
			(@id+13, 'Exams.BackToBackDistance', '-1', 'Back-to-back distance (-1 means disabled)', 'double', 6, 1, @id+1),
			(@id+14, 'Exams.PeriodWeight', '1.0', 'Period preference weight', 'double', 7, 1, @id+1),
			(@id+15, 'Exams.RoomWeight', '1.0', 'Room preference weight', 'double', 8, 1, @id+1),
			(@id+16, 'Exams.DistributionWeight', '1.0', 'Distribution preference weight', 'double', 9, 1, @id+1),
			(@id+17, 'Exams.RoomSplitWeight', '10.0', 'Room split weight', 'double', 10, 1, @id+1),
			(@id+18, 'Exams.RoomSizeWeight', '0.001', 'Excessive room size weight', 'double', 11, 1, @id+1),
			(@id+19, 'Exams.NotOriginalRoomWeight', '1.0', 'Not original room weight', 'double', 12, 1, @id+1),
			(@id+20, 'Exams.RotationWeight', '0.001', 'Exam rotation weight', 'double', 13, 1, @id+1),
			(@id+21, 'Neighbour.Class', 'net.sf.cpsolver.exam.heuristics.ExamNeighbourSelection', 'Examination timetabling neighbour selection class', 'text', 0, 0, @id+2),
			(@id+22, 'Termination.TimeOut', '1800', 'Maximal solver time (in sec)', 'integer', 1, 1, @id+2),
			(@id+23, 'Exam.Algorithm', 'Great Deluge', 'Used heuristics', 'enum(Great Deluge,Simulated Annealing)', 2, 1, @id+2),
			(@id+24, 'HillClimber.MaxIdle', '25000', 'Hill Climber: maximal idle iteration', 'integer', 3, 1, @id+2),
			(@id+25, 'Termination.StopWhenComplete', 'false', 'Stop when a complete solution if found', 'boolean', 4, 0, @id+2),
			(@id+26, 'General.SaveBestUnassigned', '-1', 'Save best when x unassigned', 'integer', 5, 0, @id+2),
			(@id+27, 'GreatDeluge.CoolRate', '0.99999995', 'Cooling rate', 'double', 0, 1, @id+3),
			(@id+28, 'GreatDeluge.UpperBoundRate', '1.05', 'Upper bound rate', 'double', 1, 1, @id+3),
			(@id+29, 'GreatDeluge.LowerBoundRate', '0.95', 'Lower bound rate', 'double', 2, 1, @id+3),
			(@id+30, 'SimulatedAnnealing.InitialTemperature', '1.5', 'Initial temperature', 'double', 0, 1, @id+4),
			(@id+31, 'SimulatedAnnealing.CoolingRate', '0.95', 'Cooling rate', 'double', 1, 1, @id+4),
			(@id+32, 'SimulatedAnnealing.TemperatureLength', '25000', 'Temperature length', 'integer', 2, 1, @id+4),
			(@id+33, 'SimulatedAnnealing.ReheatLengthCoef', '5', 'Reheat length coefficient', 'double', 3, 1, @id+4);

insert into solver_predef_setting (uniqueid, name, description, appearance) values 
			(@id+34, 'Exam.Default', 'Default', 2);

update hibernate_unique_key set next_hi=next_hi+1

/*
 * Update database version
 */

update application_config set value='14' where name='tmtbl.db.version';

commit;
