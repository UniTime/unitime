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

insert into SOLVER_PARAMETER_GROUP 
	(UNIQUEID, NAME, DESCRIPTION, CONDITION, ORD) values 
	(SOLVER_PARAMETER_GROUP_SEQ.nextval, 'OnFlySectioning', 'On Fly Student Sectioning', '', -1);
	
update SOLVER_PARAMETER_GROUP g set g.ord = ( select max(x.ord)+1 from SOLVER_PARAMETER_GROUP x )
	where g.name='OnFlySectioning';
	
insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.Enabled' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Enable on fly sectioning (if enabled, students will be resectioned after each iteration)' as DESCRIPTION, 
		'boolean' as TYPE, 
		0 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.Recursive' as NAME, 
		'true' as DEFAULT_VALUE, 
		'Recursively resection lectures affected by a student swap' as DESCRIPTION, 
		'boolean' as TYPE, 
		1 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');

insert into SOLVER_PARAMETER_DEF
	(select SOLVER_PARAMETER_DEF_SEQ.nextval as UNIQUEID, 
		'OnFlySectioning.ConfigAsWell' as NAME, 
		'false' as DEFAULT_VALUE, 
		'Resection students between configurations as well' as DESCRIPTION, 
		'boolean' as TYPE, 
		2 as ORD, 
		1 as VISIBLE, 
		UNIQUEID as SOLVER_PARAM_GROUP_ID from SOLVER_PARAMETER_GROUP where NAME='OnFlySectioning');
		
/** 
-- Uncomment this insert to enable on fly student sectioning in the default solver configuration
insert into SOLVER_PARAMETER
	(select SOLVER_PARAMETER_SEQ.nextval as UNIQUEID,
	 'on' as VALUE,
	 d.UNIQUEID as SOLVER_PARAM_DEF_ID,
	 NULL as SOLUTION_ID,
	 s.UNIQUEID as SOLVER_PREDEF_SETTING_ID
	 from SOLVER_PARAMETER_DEF d, SOLVER_PREDEF_SETTING s where d.NAME='OnFlySectioning.Enabled' and s.NAME='Default.Solver');
*/

commit;
