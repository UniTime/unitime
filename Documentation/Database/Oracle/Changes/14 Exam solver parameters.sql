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

alter table SOLVER_PARAMETER_GROUP add PARAM_TYPE number(10) default 0;

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'ExamBasic', 'Basic Parameters', '', -1,1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='ExamBasic';
	
insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'ExamBasic.Mode' as NAME, 
		'Initial' as DEFAULT_VALUE, 
		'Solver mode' as DESCRIPTION, 
		'enum(Initial,MPP)' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamBasic');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'ExamBasic.WhenFinished' as NAME, 
		'No Action' as DEFAULT_VALUE, 
		'When finished' as DESCRIPTION, 
		'enum(No Action,Save,Save and Unload)' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamBasic');
		
insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'ExamWeights', 'Examination Weights', '', -1,1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='ExamWeights';
	

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.MaxRooms' as NAME, 
		'4' as DEFAULT_VALUE, 
		'Default number of room splits per exam' as DESCRIPTION, 
		'integer' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');
		
insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.IsDayBreakBackToBack' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Consider back-to-back over day break' as DESCRIPTION, 
		'boolean' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.DirectConflictWeight' as NAME, 
		'1000.0' as DEFAULT_VALUE, 
		'Direct conflict weight' as DESCRIPTION, 
		'double' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.MoreThanTwoADayWeight' as NAME, 
		'100.0' as DEFAULT_VALUE, 
		'Three or more exams a day conflict weight' as DESCRIPTION, 
		'double' as TYPE, 
		3 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.BackToBackConflictWeight' as NAME, 
		'10.0' as DEFAULT_VALUE, 
		'Back-to-back conflict weight' as DESCRIPTION, 
		'double' as TYPE, 
		4 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.DistanceBackToBackConflictWeight' as NAME, 
		'25.0' as DEFAULT_VALUE, 
		'Distance back-to-back conflict weight' as DESCRIPTION, 
		'double' as TYPE, 
		5 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.BackToBackDistance' as NAME, 
		'-1' as DEFAULT_VALUE, 
		'Back-to-back distance (-1 means disabled)' as DESCRIPTION, 
		'double' as TYPE, 
		6 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.PeriodWeight' as NAME, 
		'1.0' as DEFAULT_VALUE, 
		'Period preference weight' as DESCRIPTION, 
		'double' as TYPE, 
		7 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.RoomWeight' as NAME, 
		'1.0' as DEFAULT_VALUE, 
		'Room preference weight' as DESCRIPTION, 
		'double' as TYPE, 
		8 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');


insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.DistributionWeight' as NAME, 
		'1.0' as DEFAULT_VALUE, 
		'Distribution preference weight' as DESCRIPTION, 
		'double' as TYPE, 
		9 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.RoomSplitWeight' as NAME, 
		'10.0' as DEFAULT_VALUE, 
		'Room split weight' as DESCRIPTION, 
		'double' as TYPE, 
		10 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.RoomSizeWeight' as NAME, 
		'0.001' as DEFAULT_VALUE, 
		'Excessive room size weight' as DESCRIPTION, 
		'double' as TYPE, 
		11 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.NotOriginalRoomWeight' as NAME, 
		'1.0' as DEFAULT_VALUE, 
		'Not original room weight' as DESCRIPTION, 
		'double' as TYPE, 
		12 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exams.RotationWeight' as NAME, 
		'0.001' as DEFAULT_VALUE, 
		'Exam rotation weight' as DESCRIPTION, 
		'double' as TYPE, 
		13 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamWeights');

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'Exam', 'General Parameters', '', -1,1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='Exam';

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Neighbour.Class' as NAME, 
		'net.sf.cpsolver.exam.heuristics.ExamNeighbourSelection' as DEFAULT_VALUE, 
		'Examination timetabling neighbour selection class' as DESCRIPTION, 
		'text' as TYPE, 
		0 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Termination.TimeOut' as NAME, 
		'1800' as DEFAULT_VALUE, 
		'Maximal solver time (in sec)' as DESCRIPTION, 
		'integer' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Exam.Algorithm' as NAME, 
		'Great Deluge' as DEFAULT_VALUE, 
		'Used heuristics' as DESCRIPTION, 
		'enum(Great Deluge,Simulated Annealing)' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'HillClimber.MaxIdle' as NAME, 
		'25000' as DEFAULT_VALUE, 
		'Hill Climber: maximal idle iteration' as DESCRIPTION, 
		'integer' as TYPE, 
		3 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');
		
insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'Termination.StopWhenComplete' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Stop when a complete solution if found' as DESCRIPTION, 
		'boolean' as TYPE, 
		4 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'General.SaveBestUnassigned' as NAME, 
		'-1' as DEFAULT_VALUE, 
		'Save best when x unassigned' as DESCRIPTION, 
		'integer' as TYPE, 
		5 as ORD, 
		0 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='Exam');

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'ExamGD', 'Great Deluge Parameters', '', -1,1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='ExamGD';

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'GreatDeluge.CoolRate' as NAME, 
		'0.99999995' as DEFAULT_VALUE, 
		'Cooling rate' as DESCRIPTION, 
		'double' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamGD');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'GreatDeluge.UpperBoundRate' as NAME, 
		'1.05' as DEFAULT_VALUE, 
		'Upper bound rate' as DESCRIPTION, 
		'double' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamGD');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'GreatDeluge.LowerBoundRate' as NAME, 
		'0.95' as DEFAULT_VALUE, 
		'Lower bound rate' as DESCRIPTION, 
		'double' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamGD');

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD, PARAM_TYPE) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'ExamSA', 'Simulated Annealing Parameters', '', -1,1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='ExamSA';

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'SimulatedAnnealing.InitialTemperature' as NAME, 
		'1.5' as DEFAULT_VALUE, 
		'Initial temperature' as DESCRIPTION, 
		'double' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamSA');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'SimulatedAnnealing.CoolingRate' as NAME, 
		'0.95' as DEFAULT_VALUE, 
		'Cooling rate' as DESCRIPTION, 
		'double' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamSA');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'SimulatedAnnealing.TemperatureLength' as NAME, 
		'25000' as DEFAULT_VALUE, 
		'Temperature length' as DESCRIPTION, 
		'integer' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamSA');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'SimulatedAnnealing.ReheatLengthCoef' as NAME, 
		'5' as DEFAULT_VALUE, 
		'Reheat length coefficient' as DESCRIPTION, 
		'double' as TYPE, 
		3 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='ExamSA');


insert into SOLVER_PREDEF_SETTING (uniqueid, name, description, appearance)
	values(Solver_Predef_Setting_Seq.Nextval, 'Exam.Default', 'Default', 2);
	
/*
 * Update database version
 */

update application_config set value='14' where name='tmtbl.db.version';

commit;
